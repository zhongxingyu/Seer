 /**
  * Program: Ch12Lab2.java
  * Programmer: Andrew Buskov
  * Class: CIS 249
  * Date: Jan 19, 2013
  * Purpose: To display a box that creates a series of random 
  * Xs and Os.
  */
 
 package Ch12.Lab2;
 
 import java.awt.GridLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 
 public class Ch12Lab2 extends JFrame {
 
 	private static final long serialVersionUID = 1L; // eclipse complains
 	
	// alstered image location for package clarity
 	public ImageIcon cross = new ImageIcon("archives/Ch12/image/x.gif");
 	public ImageIcon not = new ImageIcon("archives/Ch12/image/o.gif");
 	
 	public Ch12Lab2(){
 		
 		setLayout(new GridLayout(3,3));
 		
 		for (int i=0;i<9;i++){
 			int mode = (int)(Math.random()*3);
 		
 			if (mode == 0)
 				add(new JLabel(cross));
 			else if (mode == 1)
 				add(new JLabel(not));
 			else 
 				add(new JLabel());
 			
 		}
 	}
 	
 	public static void main(String[] args){
 		Ch12Lab2 frame = new Ch12Lab2();
 		frame.setTitle("Chapter 12 Lab 2");
 		frame.setSize(200,200);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 	}
 }
