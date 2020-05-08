 package net.infinitycoding.carsim;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 
 import net.infinitycoding.carsim.modules.Car;
 
 public class UserInterface extends JFrame
 {	
 	Canvas canvas;
 	
 	public UserInterface(ImageIcon background)
 	{
 		super();
 		
 		this.setBounds(0, 0, 1280, 1024);
 		
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setResizable(false);
 		this.setTitle("CarSimulator by InfinityCoding");
 		this.setLayout(new GridLayout(1, 1));
 		
 		this.canvas = new Canvas(1280, 1024, background);
 		this.add(canvas);
 		
 		this.setVisible(true);
 	}
 
 	public void checkCollision()
 	{
 		// TODO Auto-generated method stub
 	}
 
 	public void drawCars(ArrayList<Car> cars)
 	{
		for(Car car : cars)
 		{
 			
 			this.canvas.getGraphics().drawImage(car.bild, car.x, car.y, car.x+128, car.y, car.x, car.y+114, car.x+128, car.y+114, null);
 		}
 	}
 	
 }
