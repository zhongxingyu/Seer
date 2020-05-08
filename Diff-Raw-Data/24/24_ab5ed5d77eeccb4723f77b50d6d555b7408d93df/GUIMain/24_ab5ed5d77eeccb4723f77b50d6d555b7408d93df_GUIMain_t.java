 package edu.ucsb.cs56.projects.games.towers_of_hanoi.utility;
 
 import java.awt.Toolkit;
 import java.awt.event.WindowEvent;
 
 

import javax.swing.JFrame;

 import edu.ucsb.cs56.projects.games.towers_of_hanoi.model.TowersOfHanoiState;
 /**
  * Main class that launches a GUI version of Towers of Hanoi
  * @author Aaron
  *
  */
 public class GUIMain {
 	
 public static void main (String [] args){
 				
 				DiskPrompt prompt = new DiskPrompt();//pop-up that asks for the number of disks
 				
 				//String input = prompt.getString();
 				int disks = 0;
 				while(disks < 3){//loop checks the input from prompt every second to check if it is valid (valid is if it is >= 3)
 					try {
 						Thread.sleep(1000);//1 second delay to keep CPU usage down
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if(prompt.getString()==null)continue;
 					try {
 	            		disks = Integer.parseInt(prompt.getString());
 	            	    } catch (NumberFormatException e) {
 	            		disks = 0;
 	            		continue;
 	            	    }					
 				}
				prompt.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//sets close operation so it doesn't kill the program when we close the frame
				WindowEvent wev = new WindowEvent(prompt, WindowEvent.WINDOW_CLOSING);//creates close event
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);//closes prompt
 				GameGUI gui = new GameGUI();
 				gui.setState(new TowersOfHanoiState(disks));
 	}
 
 	
 	
 }
