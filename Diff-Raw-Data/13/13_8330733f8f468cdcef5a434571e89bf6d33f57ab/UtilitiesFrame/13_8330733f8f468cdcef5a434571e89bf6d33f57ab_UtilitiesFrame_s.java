 /*
  * Program Name: Recipe Application
 * author: Vairon Mendoza 
  * date: Feb/06/2012 
 * School: South Lakes High School
  * Computer used: HP Compaq 6000 Pro Microtower
  * IDE used: Eclipse IDE
  * purpose: Provide the user with a simple/flexible recipe organizer application 
  */
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import javax.swing.*;
 
 public class UtilitiesFrame
 {
 	private static final int WIDTH = 800;
 	private static final int HEIGHT = 600;
 	private static JFrame frame = new JFrame("Recipe Application");
 	private static final UtilitiesPanel panel = new UtilitiesPanel();
 
 	public static void main(String[] args)
 	{
 		frame.setSize(WIDTH, HEIGHT);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		frame.add(panel, gbc);
 		frame.setResizable(false);
 		/*
 		 * Change the position to the middle of the screen
 		 */
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 }
