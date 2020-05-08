 package app;
 
 import gui.GuiImpl;
 
 import javax.swing.UIManager;
 
 import mediator.MediatorMockup;
 import network.NetworkImpl;
 import webClient.WebClientMockup;
 
 public class Main {
 	public static void main(String[] args) {
 		try {
 			UIManager
 					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		} catch (Exception e) {
 			try {
 				UIManager
 						.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 			} catch (Exception e1) {
 			}
 		}
 
 		MediatorMockup med = new MediatorMockup();
 		GuiImpl gui = new GuiImpl(med);
 		NetworkImpl net = new NetworkImpl(med);
 		WebClientMockup web = new WebClientMockup(med);
 
 		med.start();
 		System.out.println("Exit");
 	}
 }
