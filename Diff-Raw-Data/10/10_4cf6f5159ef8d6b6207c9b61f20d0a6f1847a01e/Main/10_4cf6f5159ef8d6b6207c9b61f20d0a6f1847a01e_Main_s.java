 package app;
 
 import gui.GuiImpl;
 
 import javax.swing.UIManager;
 
import mediator.MockupMediator;
import network.NetworkImpl;
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
 		
		MockupMediator med = new MockupMediator();
 		GuiImpl gui = new GuiImpl(med);
		NetworkImpl net = new NetworkImpl(med);
 		WebServiceClientMockup web = new WebServiceClientMockup(med);
 		
 		med.start();
 		System.out.println("Exit");
 	}
 }
