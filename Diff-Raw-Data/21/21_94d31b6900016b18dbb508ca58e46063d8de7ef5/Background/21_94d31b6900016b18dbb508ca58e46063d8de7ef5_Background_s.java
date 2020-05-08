 package gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JComponent;
 
 public class Background extends JComponent{
 	  /**
 	 * 
 	 */
 	private static final long serialVersionUID = 4976685178018421771L;
 
 	public void paint(Graphics g) {
		  g.setColor(Color.GRAY);
 		  g.fillRect(0,0,200,1024);
 		  g.fillRect(1090,0,200,1024);
 		  
 		  g.setColor(Color.BLACK);
 		  g.fillRect(1195,20,65,984);
 	  }
 }
