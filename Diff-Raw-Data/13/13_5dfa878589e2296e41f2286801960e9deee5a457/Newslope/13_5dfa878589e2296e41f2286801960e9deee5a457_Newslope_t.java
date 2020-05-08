 package seph;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 
 
 
 public class Newslope extends JFrame {
	public void rectangle (Graphics g) {
		g.setColor(Color.BLACK);
	    g.drawLine(getWidth() / 2, getHeight() , getWidth() / 2,0);
	    
 
 	  }
 	@Override
 	public void paint(Graphics g) {
 
 	  rectangle(g);
 	}
 	public static void main(String[] args) {
		Newslope frame = new Newslope();
 		
		frame.setSize(500, 500);
 		frame.setBackground(Color.WHITE);
 		frame.setVisible(true);
 	}
 }
