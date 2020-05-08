 package de.uulm.presenter.control;
 
 import java.awt.AWTException;
 
 import javax.swing.JOptionPane;
 
 import de.uulm.presenter.view.Tray;
 
 
 public class Main {
 
 	public static Control control;
 	public static void main(String[] args) {
 		
 		
 		try {
 			control = new Control();
 			Tray icon = new Tray(control);
 			control.addInfoMessageListener(icon);
 			control.addProgramStateListener(icon);
 			control.addObserver(icon);
 			control.listen();
 		} catch (AWTException e) {
 			JOptionPane.showMessageDialog(null,  "Could not apply TrayIcon","Error", JOptionPane.ERROR_MESSAGE);
 			System.exit(1);			
 		} catch (de.uulm.presenter.exceptions.TrayIconNotSupportedError e) {
 			JOptionPane.showMessageDialog(null,  "Your System does not support the tray icon","Error", JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 		
 	}
	
	
 
 }
