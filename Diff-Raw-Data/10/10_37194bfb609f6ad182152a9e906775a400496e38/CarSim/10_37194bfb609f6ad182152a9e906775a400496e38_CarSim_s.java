 package net.infinitycoding.carsim;
 
 import java.util.ArrayList;
 
 import net.infinitycoding.carsim.modules.Car;
 import net.infinitycoding.carsim.util.CarGenerator;
 
 public class CarSim {
 	
 	private boolean run = false;
 	private ArrayList<Car> cars = new ArrayList<Car>();
 	private CarGenerator generator;
 	private UserInterface userInterface;
 
 	public static void main(String[] args) {
 		
 		new CarSim().start();
 		
 	}
 	
 	public void start(){
 		long beforeTime = 0;
 		long afterTime = 0;
 		long difTime = 0;
 		
 		userInterface = new UserInterface();
 		
 		generator = new CarGenerator();
 		
 		while(this.run){
 			//Hauptschleife
 			difTime = afterTime - beforeTime;
 			beforeTime = System.currentTimeMillis();
 			
 			this.cars.addAll(this.generator.genNewCars());
 			this.moveCars(difTime);
 			this.userInterface.drawCars();
 			this.userInterface.checkCollision();
 			
 			afterTime = System.currentTimeMillis();
 		}
 	}
 	
 
 	private void moveCars(long difTime)
 	{
		
 		
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
