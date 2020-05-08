 /**
  * SimulatorMenu.java
  * 
  * Class to store and manage window information for the user interface
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import edu.wheaton.simulator.gui.screen.Screen;
 
 /**
  * This class will act as the singular JFrame window for the interface, with
  * different screens being displayed on it by using the setContentPane method
  * to switch to the "current" or "active" screen.
  */
 public class Display extends JFrame {
 
 	private static Display d;
 	
 	private static final long serialVersionUID = 8240039325787217431L;
 
 	private JPanel panel;
 	
 	private GridBagConstraints c;
 
 	private Display() {
 		super("Simulator");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setLayout(new GridBagLayout());
 		panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		c = new GridBagConstraints();
 		c.insets = new Insets(5, 15, 15, 15);
 		this.add(panel, c);
 	}
 	
 	public static Display getInstance() {
 		if(d==null)
 			d = new Display();
 		return d;
 	}
 	
 	/**
 	 * Updates what screen is being displayed
 	 * @param s The screen to display
 	 */
 	public void updateDisplay(Screen s) {
 		this.remove(panel);
 		panel = s;
 		this.add(s, c);
 		this.pack();
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 	}
 }
