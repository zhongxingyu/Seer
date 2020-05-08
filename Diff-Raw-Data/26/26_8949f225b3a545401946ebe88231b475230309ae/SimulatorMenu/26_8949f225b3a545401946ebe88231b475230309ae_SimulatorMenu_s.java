 /**
  * SimulatorMenu.java
  * 
  * Class to store and manage window information for the user interface
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 /**
  * This class will act as the singular JFrame window for the interface, with
  * different screens being displayed on it by using the setContentPane method
  * to switch to the "current" or "active" screen. 
  */
 public class SimulatorMenu extends JFrame {
 	
 	private HashMap<String, Screen> screens;
 	
 	private boolean simulationIsRunning;
 	
 	//references to facades for internal code
 	
 	public SimulatorMenu() {
 		super("Simulator");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		//continued frame setup
 		
 		//create and add screens to HashMap
		//example: screens.add("title", new TitleScreen());
 		
		//setContentPane to the title/welcome screen
 		
 	}
 	
 	public Screen getScreen(String s) {
 		return screens.get(s);
 	}
 	
	//getter methods for facades?
 }
