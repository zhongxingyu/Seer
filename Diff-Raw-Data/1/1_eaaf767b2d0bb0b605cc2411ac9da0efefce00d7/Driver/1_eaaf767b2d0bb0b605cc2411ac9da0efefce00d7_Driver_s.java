 /**
  * Main class
  * 
  * Class containing the main method to run the simulation
  * Phase 2 Deliverable
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 public class Driver {
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					UIManager.setLookAndFeel(
 						UIManager.getSystemLookAndFeelClassName());
 				}
 				catch(Exception e) {
 					System.err.println("L&F trouble.");
 					e.printStackTrace();
 				}
 				Gui.init();
				SimulatorGuiManager gm = SimulatorGuiManager.getInstance();
 				ScreenManager sm = Gui.getScreenManager();
 				sm.update(sm.getScreen("Title"));
 			}
 		}));
 	}
 }
