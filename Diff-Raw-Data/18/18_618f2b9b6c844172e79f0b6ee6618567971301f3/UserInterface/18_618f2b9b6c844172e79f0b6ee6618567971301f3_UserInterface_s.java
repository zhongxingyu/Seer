 package net.infinitycoding.carsim;
 
 import javax.swing.JFrame;
 
 public class UserInterface extends JFrame {
 	
 	Canvas canvas;
 	
 	public UserInterface()
 	{
 		super();
 		
		this.setBounds(0, 0, 1280, 1024);
 		
 		this.canvas = new Canvas(1280, 1024);
 		this.add(canvas);
 		
 		this.setVisible(true);
 	}
 
 	public void checkCollision()
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void drawCars()
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
