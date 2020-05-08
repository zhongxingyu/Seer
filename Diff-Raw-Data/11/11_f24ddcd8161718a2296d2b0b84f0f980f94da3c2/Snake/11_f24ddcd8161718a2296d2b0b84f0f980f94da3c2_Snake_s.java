 package com.optimalbyte.snake;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 
 /**
 * The bootstrap class for this game.
  *
  * @author samuraiblood2
  */
 public class Snake extends JFrame {
 
 	/**
 	 * Initializes and sets the various settings of the JFrame.
 	 */
 	public Snake() {
 		super("Snake");
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(true);
 		setLocation(getCenterPoint());
 		
 		add(new Game());
 	
 		pack();
 		setVisible(true);
 	}
 
 	/**
 	 * Gets the Point of the screen to display the game.
 	 *
 	 * @return The center point of the screen/monitor.
 	 */
 	private Point getCenterPoint() {
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		double x = ((dim.getWidth() - getWidth()) / 2);
 		double y = ((dim.getHeight() - getHeight()) / 2);
 		return new Point((int) x, (int) y);
 	}
 		
 	/**
 	 * The main method
 	 *
 	 * @param args The program arguments passed from the JVM at startup.
 	 */
 	public static void main(String[] args) {
 		new Snake();
 	}
 }
