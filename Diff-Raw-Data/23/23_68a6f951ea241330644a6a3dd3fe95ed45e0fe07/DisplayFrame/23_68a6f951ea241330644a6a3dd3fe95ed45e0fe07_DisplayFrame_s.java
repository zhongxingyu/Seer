 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.netprojectev.server.gui;
 
 import java.awt.Cursor;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import org.apache.logging.log4j.Logger;
 
 import de.netprojectev.exceptions.MediaDoesNotExsistException;
 import de.netprojectev.exceptions.MediaListsEmptyException;
 import de.netprojectev.misc.LoggerBuilder;
 import de.netprojectev.server.ConstantsServer;
 import de.netprojectev.server.datastructures.Countdown;
 import de.netprojectev.server.datastructures.ImageFile;
 import de.netprojectev.server.datastructures.ServerMediaFile;
 import de.netprojectev.server.datastructures.Themeslide;
 import de.netprojectev.server.datastructures.VideoFile;
 import de.netprojectev.server.networking.MessageProxyServer;
 import de.netprojectev.server.networking.MessageProxyServer.PropertyUpdateListener;
 
 /**
  * GUI class frame, to store the viewing components, as the live ticker and the
  * image and themeslide showing component.
  * 
  * @author samu
  */
 public class DisplayFrame extends javax.swing.JFrame {
 
 	public interface VideoFinishListener {
 		public void videoFinished() throws MediaDoesNotExsistException, MediaListsEmptyException;
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 863589702184282724L;
 	private static final Logger log = LoggerBuilder.createLogger(DisplayFrame.class);
 
 	private final MessageProxyServer proxy;
 	private boolean fullscreen;
 	private Timer countdownTimer;
 	private VideoFinishListener videoFinishedListener;
 
 	public DisplayFrame(MessageProxyServer proxy) {
 		this.proxy = proxy;
 		initComponents();
 		// Transparent 16 x 16 pixel cursor image.
 		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 
 		// Create a new blank cursor.
 		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
 				cursorImg, new Point(0, 0), "blank cursor");
 
 		// Set the blank cursor to the JFrame.
 		this.getContentPane().setCursor(blankCursor);
 		fullscreen = false;
 		this.proxy.setPropertyUpdateListener(new PropertyUpdateListener() {
 
 			@Override
 			public void propertyUpdate(String keyPropertyUpdated) {
 				propertyUpdated(keyPropertyUpdated);
 			}
 		});
 	}
 
 	private void propertyUpdated(String key) {
 		if (key.equals(ConstantsServer.PROP_COUNTDOWN_FONTCOLOR)) {
 			displayMainComponent.updateCountdownFontColor();
 		} else if (key.equals(ConstantsServer.PROP_COUNTDOWN_FONTSIZE)) {
 			displayMainComponent.updateCountdownFont();
 		} else if (key.equals(ConstantsServer.PROP_COUNTDOWN_FONTTYPE)) {
 			displayMainComponent.updateCountdownFont();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_BACKGROUND_COLOR)) {
 			tickerComponent.updateBackgroundColor();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_FONTCOLOR)) {
 			tickerComponent.updateFontColor();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_FONTSIZE)) {
 			tickerComponent.updateFontByClient();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_FONTTYPE)) {
 			tickerComponent.updateFontByClient();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_SEPERATOR)) {
 			updateLiveTickerString();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_SPEED)) {
 			tickerComponent.updateSpeedByClient();
 		} else if (key.equals(ConstantsServer.PROP_TICKER_BACKGROUND_ALPHA)) {
 			tickerComponent.updateBackgroundAlpha();
 		} else if (key.equals(ConstantsServer.PROP_GENERAL_BACKGROUND_COLOR)) {
 			displayMainComponent.updateBackgroundColor();
 		} else {
 			log.warn("received an unknown property key: " + key);
 		}
 
 	}
 
 	public void showMediaFileInMainComponent(ServerMediaFile fileToShow) throws IOException {
 		if (fileToShow instanceof ImageFile) {
 			showImageFile((ImageFile) fileToShow);
 		} else if (fileToShow instanceof Themeslide) {
 			showThemeslide((Themeslide) fileToShow);
 		} else if (fileToShow instanceof VideoFile) {
 			showVideoFile((VideoFile) fileToShow);
 		} else if (fileToShow instanceof Countdown) {
 			showCountdown((Countdown) fileToShow);
 		} else {
 			throw new IllegalArgumentException("The arg is no subclass of ServerMediaFile");
 		}
 	}
 
 	private void showImageFile(ImageFile image) throws IOException {
 		displayMainComponent.drawImage(image.get());
 	}
 
 	private void showVideoFile(VideoFile video) {
 
 		try {
			setAlwaysOnTop(false);
 			final Process vlc = new VlcPlayBackUtility(video.getVideoFile()).startPlay();
 
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						vlc.waitFor();
 					} catch (InterruptedException e) {
 						log.warn("vlc interrupted", e);
 					}
 					SwingUtilities.invokeLater(new Runnable() {
 
 						@Override
 						public void run() {
							exitFullscreen();
 							enterFullscreen(0);
 							try {
 								videoFinishedListener.videoFinished();
 							} catch (MediaDoesNotExsistException e) {
 								log.warn("Video does not exist.", e);
 							} catch (MediaListsEmptyException e) {
 								log.warn("Video does not exist.", e);
 							}
 						}
 					});
 
 				}
 			}).start();
 
 		} catch (Exception e) {
 			log.warn("Video could not be played.", e);
 		}
 
 	}
 
 	private void showThemeslide(Themeslide themeslide) throws IOException {
 		displayMainComponent.drawImage(themeslide.get());
 	}
 
 	private void showCountdown(final Countdown countdown) {
 		if (countdownTimer != null) {
 			countdownTimer.stop();
 		}
 
 		displayMainComponent.drawCountdown(countdown);
 		displayMainComponent.repaint();
 		countdownTimer = new Timer(1000, new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				countdown.decreaseOneSecond();
 				displayMainComponent.repaint();
 				if (countdown.getDurationInSeconds() <= 0) {
 					countdownTimer.stop();
 					displayMainComponent.countdownFinished();
 				}
 			}
 		});
 		countdownTimer.start();
 
 	}
 
 	public void startLiveTicker() {
 		tickerComponent.setTickerString(proxy.getTickerModel().generateCompleteTickerText());
 		tickerComponent.initLiveTickerAndStart();
 	}
 
 	public void updateLiveTickerString() {
 		tickerComponent.setTickerString(proxy.getTickerModel().generateCompleteTickerText());
 	}
 
 	public void stopLiveTicker() {
 		tickerComponent.stop();
 	}
 
 	public void clearDisplay() {
 		displayMainComponent.clear();
 	}
 
 	/**
 	 * Setting the display frame as fullscreen exclusive window
 	 * 
 	 * @param screenNumber
 	 *            the number of the screen to show on
 	 */
 	public void enterFullscreen(int screenNumber) {
 
 		if (!fullscreen) {
 			dispose();
 			this.setUndecorated(true);
			setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
			setResizable(false);
			setAlwaysOnTop(true);
 			setVisible(true);
			toFront();
 			
 			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 			GraphicsDevice device = env.getDefaultScreenDevice();
 			device.setFullScreenWindow(this);
 			
 			fullscreen = true;
 		}
 
 	}
 
 	/**
 	 * lets the display window exiting the fullscreen
 	 */
 	public void exitFullscreen() {
 
 		if (fullscreen) {
 			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 			GraphicsDevice[] myDevices = ge.getScreenDevices();
 			dispose();
			setAlwaysOnTop(false);
 			this.setUndecorated(false);
			setResizable(true);
			setSize(getPreferredSize());
 			setVisible(true);
 			
 			myDevices[0].setFullScreenWindow(null);
 			
			toFront();
			pack();
			
 			fullscreen = false;
 		}
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 
 	// <editor-fold defaultstate="collapsed"
 	// <editor-fold defaultstate="collapsed"
 	// desc="Generated Code">//GEN-BEGIN:initComponents
 	private void initComponents() {
 
 		displayMainComponent = new de.netprojectev.server.gui.DisplayMainComponent();
 		tickerComponent = new de.netprojectev.server.gui.TickerComponent(displayMainComponent);
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 
 		displayMainComponent.setDoubleBuffered(true);
 
 		tickerComponent.setDoubleBuffered(true);
 		tickerComponent.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
 		tickerComponent.setMinimumSize(new java.awt.Dimension(0, 0));
 		tickerComponent.setPreferredSize(new java.awt.Dimension(800, 80));
 
 		javax.swing.GroupLayout tickerComponentLayout = new javax.swing.GroupLayout(tickerComponent);
 		tickerComponent.setLayout(tickerComponentLayout);
 		tickerComponentLayout.setHorizontalGroup(
 				tickerComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						.addGap(0, 800, Short.MAX_VALUE)
 				);
 		tickerComponentLayout.setVerticalGroup(
 				tickerComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						.addGap(0, 80, Short.MAX_VALUE)
 				);
 
 		javax.swing.GroupLayout displayMainComponentLayout = new javax.swing.GroupLayout(displayMainComponent);
 		displayMainComponent.setLayout(displayMainComponentLayout);
 		displayMainComponentLayout.setHorizontalGroup(
 				displayMainComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						.addComponent(tickerComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 		displayMainComponentLayout.setVerticalGroup(
 				displayMainComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, displayMainComponentLayout.createSequentialGroup()
 								.addContainerGap(338, Short.MAX_VALUE)
 								.addComponent(tickerComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 								.addGap(32, 32, 32))
 				);
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						.addComponent(displayMainComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						.addComponent(displayMainComponent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 
 		pack();
 	}// </editor-fold>//GEN-END:initComponents
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 */
 	public static void main(String args[]) {
 		/*
 		 * Set the Nimbus look and feel
 		 */
 		// <editor-fold defaultstate="collapsed"
 		// desc=" Look and feel setting code (optional) ">
 		/*
 		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
 		 * default look and feel. For details see
 		 * http://download.oracle.com/javase
 		 * /tutorial/uiswing/lookandfeel/plaf.html
 		 */
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
 					.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (ClassNotFoundException ex) {
 			java.util.logging.Logger.getLogger(DisplayFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (InstantiationException ex) {
 			java.util.logging.Logger.getLogger(DisplayFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (IllegalAccessException ex) {
 			java.util.logging.Logger.getLogger(DisplayFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(DisplayFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		}
 		// </editor-fold>
 
 		/*
 		 * Create and display the form
 		 */
 		java.awt.EventQueue.invokeLater(new Runnable() {
 
 			public void run() {
 				// new DisplayMainFrame().setVisible(true);
 			}
 		});
 	}
 
 	// Variables declaration - do not modify//GEN-BEGIN:variables
 	private de.netprojectev.server.gui.DisplayMainComponent displayMainComponent;
 	private de.netprojectev.server.gui.TickerComponent tickerComponent;
 
 	// End of variables declaration//GEN-END:variables
 
 	public void setVideoFinishedListener(VideoFinishListener videoFinishedListener) {
 		this.videoFinishedListener = videoFinishedListener;
 	}
 
 }
