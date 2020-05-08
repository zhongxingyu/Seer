 import java.awt.*;
 import javax.swing.*;
 
 public class SplashScreen extends JWindow {
 
 	public SplashScreen() {
 		super();
 		// Get dimension of the screen
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		// Instantiate TransparentBGScreen(JComponent) and provide it with
 		// screen dimension and pointer to this SplashScreen(JWindow)
 		TransparentBGScreen bg = new TransparentBGScreen(this, dim);
 		// Add TransparentBGScreen to this SplashScreen(JWindow)
 		getContentPane().add(bg);
 		pack();
 		// Show splash screen
 		setVisible(true);
 		// Wait for a while. In this case, 1100 milliseconds
 		try {
 			Thread.sleep(1100);
 		} catch (InterruptedException e) {
 		}
 		// Dispose this SplashScreen(JWindow)
 		dispose();
 	}
 
 	class TransparentBGScreen extends JComponent {
 		private Image background;
 		private ImageIcon splash;
 		private int screenWidth;
 		private int screenHeight;
 		private int imgWidth;
 		private int imgHeight;
 
 		public TransparentBGScreen(SplashScreen window, Dimension screenDim) {
 			this.screenWidth = (int) screenDim.getWidth();
 			this.screenHeight = (int) screenDim.getHeight();
 			// Get splash screen image
			splash = new ImageIcon(getClass().getResource("/images/splash.png"));
 			this.imgWidth = splash.getIconWidth();
 			this.imgHeight = splash.getIconHeight();
 			// Set preferred size of this component(TransparentBGScreen) to be
 			// the same as image dimension
 			setPreferredSize(new Dimension(imgWidth, imgHeight));
 			// Set SplashScreen(JWindow) location to be at the center of the
 			// screen
 			window.setLocation((screenWidth - imgWidth) / 2,
 					(screenHeight - imgHeight) / 2);
 			updateBackground();
 		}
 
 		/**
 		 * This method capture the screen where the splash screen is shown and
 		 * store that screen capture to "background"(Image) (to mimic
 		 * transparent background of the window)
 		 */
 		public void updateBackground() {
 			try {
 				Robot rbt = new Robot();
 				background = rbt.createScreenCapture(new Rectangle(
 						(screenWidth - imgWidth) / 2,
 						(screenHeight - imgHeight) / 2, imgWidth, imgHeight));
 			} catch (Exception ex) {
 			}
 		}
 
 		public void paintComponent(Graphics g) {
 			// Paint background which is screen capture first so it will be
 			// behind splash screen image
 			g.drawImage(background, 0, 0, null);
 			// Paint the splash screen image
 			g.drawImage(splash.getImage(), 0, 0, null);
 		}
 	}
 }
