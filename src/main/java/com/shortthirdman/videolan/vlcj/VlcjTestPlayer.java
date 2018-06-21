/**
 * 
 */
package com.shortthirdman.videolan.vlcj;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author atcsinc
 *
 */
public class VlcjTestPlayer extends Frame {

	private static final long serialVersionUID = 1L;

	private JFrame jframe;

	private JButton pauseButton;

	private JButton rewindButton;

	private JButton skipButton;

	private Frame aframe;

	private EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private static final Logger LOGGER = LoggerFactory.getLogger(VlcjTestPlayer.class);

	public static void main(final String[] args) {
		execute();
		try {
			new NativeDiscovery().discover();
		} catch (Exception e) {
			LOGGER.warn("Exception occured at libVlc discovery.", e.getMessage());
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new VlcjTestPlayer();
			}
		});
	}

	public static void execute() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Test: TRACE level message.");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Test: DEBUG level message.");
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Test: INFO level message.");
		}
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Test: WARN level message.");
		}
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error("Test: ERROR level message.");
		}
	}

	public VlcjTestPlayer() {
		// initJFramePanel();
		initAWTFramePanel();
	}

	public void controls() {
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

		JPanel controlsPane = new JPanel();

		pauseButton = new JButton("Pause");
		controlsPane.add(pauseButton);

		rewindButton = new JButton("Rewind");
		controlsPane.add(rewindButton);

		skipButton = new JButton("Skip");
		controlsPane.add(skipButton);

		contentPane.add(controlsPane, BorderLayout.SOUTH);

		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().pause();
			}
		});

		rewindButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(-10000);
			}
		});

		skipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().skip(10000);
			}

		});

		jframe.setContentPane(contentPane);
		jframe.setVisible(true);
	}

	public void initJFramePanel() {
		jframe = new JFrame("My First Media Player");
		jframe.setBounds(100, 100, 600, 400);
		jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mediaPlayerComponent.release();
				System.exit(0);
			}
		});
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		jframe.setContentPane(mediaPlayerComponent);
		jframe.setVisible(true);
		mediaPlayerComponent.getMediaPlayer().playMedia("D:\\Test.mp4");
	}

	public void initAWTFramePanel() {
		aframe = new Frame("Test Player");
		aframe.setSize(800, 600);

		aframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mediaPlayerComponent.release();
				System.exit(0);
				dispose();
			}
		});
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		Canvas videoSurface = mediaPlayerComponent.getVideoSurface();

		aframe.setLayout(new BorderLayout());
		aframe.add(videoSurface, BorderLayout.CENTER);
		aframe.setVisible(true);

		mediaPlayerComponent.getMediaPlayer().playMedia("D:\\Test.mp4");
	}

	/**
	 * Important: Notice where is the libvlc, which contains all native
	 * functions to manipulate the player
	 * 
	 * Windows: libvlc.dll Linux: libvlc.so
	 */
	public void registerLibrary() {
		boolean found = new NativeDiscovery().discover();
		System.out.println(found);
		if (found) {
			System.out.println(LibVlc.INSTANCE.libvlc_get_version());
		} else {
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VlcjConstants.NATIVE_LIBRARY_SEARCH_PATH);
			System.out.println(LibVlc.INSTANCE.libvlc_get_version());
		}
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		LibXUtil.initialise();
	}

	/**
	 * Configure VLC parameters
	 */
	public void configureParameters(final List<String> vlcArgs) {
		vlcArgs.add("--no-plugins-cache");
		vlcArgs.add("--no-video-title-show");
		vlcArgs.add("--no-snapshot-preview");

		// Important, if this parameter would not be set on Windows, the app
		// won't work
		if (RuntimeUtil.isWindows()) {
			vlcArgs.add("--plugin-path=" + VlcjConstants.PLUGINS_DIR_64);
		}
	}

	/**
	 * Build the player
	 */
	public EmbeddedMediaPlayer createPlayer(final List<String> vlcArgs, final Canvas videoSurface) {
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerComponent.getMediaPlayer();

		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(vlcArgs.toArray(new String[vlcArgs.size()]));
		mediaPlayerFactory.setUserAgent("vlcj Test Player");
		embeddedMediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
		embeddedMediaPlayer.setPlaySubItems(true);

		return embeddedMediaPlayer;
	}
}
