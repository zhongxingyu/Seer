 package cz.vse.metric;
 
 import cz.vse.metric.gui.MainWindow;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * Created by IntelliJ IDEA.
  * User: djaara
  * Date: 2011-09-10
  * Time: 19:08
  */
 public class GUIApplication implements UserInterface {
 
 	private final MainWindow mainWindow;
	private static final String APP_NAME = "DCI Metric GUI";
 
 	public GUIApplication() {
 		if (System.getProperty("os.name").startsWith("Mac OS X")) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
 
 			URL iconURL = GUIApplication.class.getResource("gui/ico.png");
 
 			com.apple.eawt.Application macApp = com.apple.eawt.Application.getApplication();
 
 			try {
 				Image image = ImageIO.read(iconURL);
 				macApp.setDockIconImage(image);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		} else {
 			try {
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			} catch (ClassNotFoundException ignored) {
 			} catch (InstantiationException ignored) {
 			} catch (IllegalAccessException ignored) {
 			} catch (UnsupportedLookAndFeelException ignored) {
 			}
 		}
		mainWindow = new MainWindow(APP_NAME);
 	}
 
 	public void start() {
 		mainWindow.setVisible(true);
 	}
 }
