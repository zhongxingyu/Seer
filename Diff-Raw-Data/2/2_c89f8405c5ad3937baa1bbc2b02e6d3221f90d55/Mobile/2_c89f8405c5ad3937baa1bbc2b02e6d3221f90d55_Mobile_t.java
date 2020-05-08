 package base;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import models.CarsCollection;
 
 import cars.*;
 
 public class Mobile implements base.interfaces.Mobile{
 	public Cars currentCar;
 	public static void main(String[] args)
 	{
 		
 //		Begin process. Explaining something important
 //		Welcome message
 		Mobile mobile = new Mobile();
 		
 		mobile.console("Welcome. Anyway we have some cars");
 		mobile.console("This is the list of the cars. Enter the name to choose your car");
 //		Cars list
 		
 		mobile.prepareCar();
 //		Getting car type and it will check if the class is exists or not
 	}
 	
 	public void prepareCar()
 	{
 		this.console("\n");
 		Map<String, Cars> carList = this.getCarList();
 
 		Object[] carKeys = carList.keySet().toArray();
 		for(int i=0;i<carKeys.length;i++)
 		{
 			String key=(String) carKeys[i];
 			this.console("\t"+key+" \t\t"+"get "+carList.get(key).getType()+" car");
 		}
 		
		this.console("\t--all-car \t"+"get all recently saved cars");
 		this.createCar(false);
 	}
 	public void createCar(boolean tryAgain)
 	{
 		if(tryAgain)
 			this.console("\nCannot find car you type, Try again. type the car you want. ");
 		else
 			this.console("\nType your car type you want to choose");
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			String command = br.readLine();
 			if(command.equalsIgnoreCase("--all-car"))
 			{
 				this.getLastCars();
 				this.createCar(false);
 			}
 			else
 			{
 				this.currentCar = this.getCar(command);
 				if(this.currentCar instanceof Cars)
 				{
 					this.console("\nYour car: "+currentCar.getType());
 					currentCar.start();
 				}
 				else
 				{
 					this.createCar(true);
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public Map<String, Cars> getCarList()
 	{
 		Map<String, Cars> carList =new HashMap<String, Cars>();
 		carList.put("sedan", new Sedan());
 		carList.put("truck", new Truck());
 		carList.put("suv", new Suv());
 		carList.put("bus", new Bus());
 		return carList;
 	}
 	public Cars getCar(String carType)
 	{
 //		Registering cars type
 		Map<String, Cars> carList = this.getCarList();
 		if(carList.containsKey(carType))
 			return (Cars) carList.get(carType);
 		else
 			return null;
 	}
 	public void console(String message)
 	{
 		System.out.println(message);
 	}
 	public static void restart()
 	{
 		Mobile mobile = new Mobile();
 		mobile.console("Welcome back, here is the list of the cars");
 		mobile.prepareCar();
 	}
 	public void getLastCars()
 	{
 		CarsCollection cars = new CarsCollection();
 		ArrayList<Map<String, String>> carsData = cars.findAll();
 		System.out.format("%33s%n", "-");
 		System.out.format("%10s- | %20s%n", "Name","Type");
 		for(int i=0;i<carsData.size();i++)
 		{
 			Map<String, String> carData = carsData.get(i);
 			System.out.format("%10s | %20s%n", carData.get("car_type"), carData.get("car_name"));
 		}
 		System.out.format("%33s", "-");
 	}
 	public void consoleHeader()
 	{
 		
 	}
 	public void consoleContent()
 	{
 		
 	}
 }
