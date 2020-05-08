 package servlet;
 
 import java.util.ArrayList;
 
 import shared.Car;
 
 public class Simulation
 {
 	ArrayList<Car> cars;
 
 	public Simulation()
 	{
 		cars = new ArrayList<Car>();
		//addCar();
 	}
 
 	public ArrayList<Car> getCars()
 	{
 		return cars;
 	}
 
 	public void addCar()
 	{
 		cars.add(new Car(50, 50, 100, 100, 10, 0));
 	}
 
 	public void run()
 	{
 		for(Car c : cars)
 		{
 			c.move();
 		}
 	}
 }
