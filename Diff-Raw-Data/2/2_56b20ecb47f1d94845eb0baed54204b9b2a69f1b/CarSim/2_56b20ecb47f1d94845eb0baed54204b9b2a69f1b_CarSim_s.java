 package net.infinitycoding.carsim;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import net.infinitycoding.carsim.exceptions.LevelFormatException;
 import net.infinitycoding.carsim.modules.Car;
 import net.infinitycoding.carsim.modules.Level;
 import net.infinitycoding.carsim.modules.io.LevelLoader;
 import net.infinitycoding.carsim.util.CarGenerator;
 
 public class CarSim
 {
 	private boolean run = true;
 	private ArrayList<Car> cars = new ArrayList<Car>();
 	private CarGenerator generator;
 	private UserInterface userInterface;
 	private Level level;
 
 	public static void main(String[] args)
 	{
 		try {
 			new CarSim().start();
 		} catch (IOException e) {
 			System.out.println("IOFehler");
 			e.printStackTrace();
 		}
 	}
 	
 	public void start() throws IOException
 	{
 		long beforeTime = 0;
 		long afterTime = 0;
 		long difTime = 0;
 		
 		generator = new CarGenerator();
 		
 		try
 		{
 			this.level = LevelLoader.loadLevel("res/test.lvl");
 		}
 		catch (LevelFormatException ex)
 		{
 			JOptionPane.showMessageDialog(null, ex.getMessage(), "Leveldatei-Fehler", JOptionPane.ERROR_MESSAGE);
 			ex.printStackTrace();
 			System.exit(-1);
 		}
 		
 		userInterface = new UserInterface(level.streetPic);
 		
 		while(this.run)
 		{
 			//Hauptschleife
 			difTime = afterTime - beforeTime;
 			beforeTime = System.currentTimeMillis();
 			Car temp = this.generator.genNewCars(this.cars,this.level);
 			if(temp != null)
 			{
 				
 				this.cars.add(temp);
 			}
 			this.moveCars(difTime);
 			this.userInterface.drawCars(this.cars);
 			this.userInterface.checkCollision();
 			
 			afterTime = System.currentTimeMillis();
 		}
 	}
 	
 
 	private void moveCars(long difTime)
 	{
 		boolean collision;
 		for(Car car : cars)
 		{
 			collision = false;
 			if(car.isDriving)
 			{				
				for(Car other_car : (Car[]) cars.toArray())
 				{
 					if(car.collisionBox.intersects(other_car.collisionBox))
 					{
 						if(other_car.isDriving)
 						{
 							this.gameOver();
 						}
 						else
 						{
 							collision = true;
 						}
 					}
 				}
 				if(!collision)
 				{
 					switch(car.direction)
 					{
 						case 1:
 							car.y += 1;
 							break;
 						case 2:
 							car.x += 1;
 							break;
 						case 3:
 							car.y -= 1;
 							break;
 						case 4:
 							car.x -= 1;
 							break;
 						}
 				}
 			}
 		}
 	}
 
 	private void gameOver() {
 		System.out.println("Gameover!!!");
 		
 	}
 
 	public boolean isRunning()
 	{
 		return run;
 	}
 
 	public void setRunning(boolean run)
 	{
 		this.run = run;
 	}
 }
