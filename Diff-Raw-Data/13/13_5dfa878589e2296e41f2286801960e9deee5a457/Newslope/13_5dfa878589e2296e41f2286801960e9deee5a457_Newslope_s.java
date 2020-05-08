 package seph;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 
 
 
 public class Newslope extends JFrame {
	public void rectangle (Graphics f) {
		f.setColor(Color.BLACK);
	    f.drawLine(getWidth() / 2, getHeight() , getWidth() / 2, getHeight() - getHeight());
 
 	  }
 	@Override
 	public void paint(Graphics g) {
 
 	  rectangle(g);
 	}
 	public static void main(String[] args) {
		JFrame frame = new JFrame();
 		
		frame.setSize(50, 50);
 		frame.setBackground(Color.WHITE);
 		frame.setVisible(true);
 	}
 }
