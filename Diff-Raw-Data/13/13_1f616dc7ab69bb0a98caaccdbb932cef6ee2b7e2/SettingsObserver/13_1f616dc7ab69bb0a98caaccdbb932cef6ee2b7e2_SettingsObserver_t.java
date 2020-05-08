 /**
  * @author: Justin Peterson, Alex Bogart
  * @email: Jmp3833@rit.edu
  * SettingsObserver.java Will listen to invoked commands from the settings
  * portion of the menu bar. These functionalities have been grouped
  * into a single class since they are rather simple. 
  */
 
 
 package Observers;
 
 import javax.swing.JOptionPane;
 
 import Text_Windows.FileReader;
 import Views.TextTabWindow;
 import Command.Receiver ;
 
 public class SettingsObserver implements Receiver{
 	
 	FileReader fr;
 	TextTabWindow mainWindow;
 	
 	public SettingsObserver(FileReader tabProxy, TextTabWindow mainWindow) {
 		this.fr = tabProxy;
 		this.mainWindow = mainWindow;
 	}
 	
 	/**
 	 * Requests a change in tab length via Integer input from the user
 	 */
 	public void changeTabLength(){
 		
 		 //Opens a dialog box for the user to select tab length. 
 		 String str = JOptionPane.showInputDialog(null, "Input desired tab length: ", 
 				 "Tab Length", 1);
 		 
 		 
 		 try{
 			  int tabLength = Integer.parseInt(str);
 			  // is an integer!
 			  if (tabLength > 0 && tabLength < 10 ){
 					fr.getSelectedTextArea(mainWindow).setTabSize(tabLength);
 			  }
 			  else{
 				  JOptionPane.showMessageDialog(null,"The tab length must be an\n " +
 			 		"integer between 1 and 9.\nPlease try again.");
 			  }
 			
 		 } catch (NumberFormatException e) {
 			  // not an integer!
 		 }
 		 
 		 
 		 
 	}
 	/**
 	 * Enables text wrapping in the HTML editor.
 	 */
 	public void enableTextWrapping(){
 		fr.getSelectedTextArea(mainWindow).setLineWrap(true);
 	}
 	
 	/**
 	 * Disables text wrapping in the HTML editor.
 	 */
 	public void disableTextWrapping(){
 		fr.getSelectedTextArea(mainWindow).setLineWrap(false);
 	}
   
 }
