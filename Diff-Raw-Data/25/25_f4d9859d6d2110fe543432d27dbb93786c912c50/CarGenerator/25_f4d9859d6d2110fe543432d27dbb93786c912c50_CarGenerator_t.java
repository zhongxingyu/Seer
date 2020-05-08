 package net.infinitycoding.carsim.util;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.infinitycoding.carsim.modules.Car;
 import net.infinitycoding.carsim.modules.Level;
 
 public class CarGenerator
 {
 
 	public Car genNewCars(ArrayList<Car> cars, Level level) throws IOException
 	{
 		if(cars.size() <= level.MAX_CARS)
 		{
 			double zahl = Math.random();
 			if(zahl <= level.CAR_RATIO)
 			{
				System.out.println("new Car");
 				int strasse = (int) (Math.random() * level.streetcount);
 				Car neu = new Car(strasse);
 				return neu;
 			}
 		}
 		return null;
 	}
 
 }
