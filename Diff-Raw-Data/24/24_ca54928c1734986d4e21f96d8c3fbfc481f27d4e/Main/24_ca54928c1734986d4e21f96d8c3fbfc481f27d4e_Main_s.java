 package app;
 
 import gui.GuiImpl;
 
 import javax.swing.UIManager;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import mediator.MediatorMockup;
 import network.NetworkImpl;
 import webClient.WebClientMockup;
 
 public class Main {
 	static Logger	logger	= Logger.getLogger(Main.class);
 
	public static void main(String[] args) {
 		PropertyConfigurator.configure("log4j.properties");
 
 		logger.setLevel(Level.OFF);
 		logger.error("Begin");
 
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		} catch (Exception e) {
 			try {
 				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 			} catch (Exception e1) {
 			}
 		}
 
 		MediatorMockup med = new MediatorMockup();
 		GuiImpl gui = new GuiImpl(med);
 		NetworkImpl net = new NetworkImpl(med);
 		WebClientMockup web = new WebClientMockup(med);
 
 		med.start();
 		logger.error("End");
 	}
 }
