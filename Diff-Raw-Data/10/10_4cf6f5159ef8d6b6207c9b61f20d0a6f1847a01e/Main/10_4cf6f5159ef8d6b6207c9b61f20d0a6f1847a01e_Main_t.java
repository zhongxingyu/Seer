 package app;
 
 import gui.GuiImpl;
 
 import javax.swing.UIManager;
 
import mediator.MediatorMockup;
import network.NetworkMockup;
 import webServiceClient.WebServiceClientMockup;
 
 public class Main {
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		} catch (Exception e) {
 			try {
 				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 			} catch (Exception e1) {}
 		}
 		
		MediatorMockup med = new MediatorMockup();
 		GuiImpl gui = new GuiImpl(med);
		NetworkMockup net = new NetworkMockup(med);
 		WebServiceClientMockup web = new WebServiceClientMockup(med);
 		
 		med.start();
 		System.out.println("Exit");
 	}
 }
