 
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SimpleMap pools = new SimpleMap();
 		System.out.println("***New CarPool***");
 		CarPool cp1 = new CarPool("Fuhrpark1");
 		pools.add(cp1.getName(), cp1);
 		System.out.println("CarPool "+cp1.getName()+" created.\n");
 
 		ElectricCar ec1 = new ElectricCar(01, new PassengerTransport(10));
 		ElectricCar ec2 = new ElectricCar(02, new PassengerTransport(2));
 		ElectricCar ec3 = new ElectricCar(03, new CargoTransport(10, 100));
 		FuelCar fc1 = new FuelCar(04, new CargoTransport(10, 100));
 		FuelCar fc2 = new FuelCar(05, new CargoTransport(2, 50));
 		FuelCar fc3 = new FuelCar(06, new PassengerTransport(5));
 				
 		ec1.increaseMileage(3000);
 		ec1.increaseConsumption(130);
 		ec2.increaseMileage(6000);
 		ec2.increaseConsumption(460);
 		ec3.increaseMileage(12000);
 		ec3.increaseConsumption(8000);
 		
 		fc1.increaseMileage(10000);
 		fc1.increaseConsumption(20);
 		fc2.increaseMileage(15000);
 		fc2.increaseConsumption(30);
 		fc3.increaseMileage(20000);
 		fc3.increaseConsumption(40);
 		
 		cp1.addCar(ec1);
 		cp1.addCar(ec2);
 		cp1.addCar(ec3);
 		cp1.addCar(fc1);
 		cp1.addCar(fc2);
 		cp1.addCar(fc3);
 		
 		System.out.println("***"+cp1.getName()+" "+cp1.getCars().toString());
 		System.out.println("Average Usage of Electric Cars: "+cp1.getAverageElectricUsage(CarPool.Role.ALL)+"kW");
 		System.out.println("Average Usage of Electric Cars for CargoTransport: "+cp1.getAverageElectricUsage(CarPool.Role.CARGOTRANSPORT)+"kW");
 		System.out.println("Average Usage of Electric Cars for PassengerTransport: "+cp1.getAverageElectricUsage(CarPool.Role.PASSENGERTRANSPORT)+"kW");
 		System.out.println("Average Usage of Fuel Cars: "+cp1.getAverageFuelUsage(CarPool.Role.ALL)+"L");
 		System.out.println("Average Usage of Fuel Cars for CargoTransport: "+cp1.getAverageFuelUsage(CarPool.Role.CARGOTRANSPORT)+"L");
 		System.out.println("Average Usage of Fuel Cars for PassengerTransport: "+cp1.getAverageFuelUsage(CarPool.Role.PASSENGERTRANSPORT)+"L");
 
 		cp1.removeCar(fc2);
 		System.out.println("\n***"+fc2.getClass().getName()+" "+fc2.getId()+" removed.***");
 		System.out.println("\n***"+cp1.getName()+" new "+cp1.getCars().toString());
 		
 		
 		fc1.increaseMileage(200);
 		fc1.increaseConsumption(20);
 		System.out.println(fc1.getClass().getName()+" "+fc1.getId()+" used, new mileage: "+fc1.getMileage()+", current Fuel level: "+fc1.getConsumption());
 		ec2.increaseMileage(500);
 		ec2.increaseConsumption(50);
 		System.out.println(ec2.getClass().getName()+" "+ec2.getId()+" used, new mileage: "+ec2.getMileage()+", current Electric level: "+ec2.getConsumption());
 		System.out.println();		
 		
 		
 		//---------------------------CAR POOL 2-----------------------------
 		System.out.println("---------------------------------------------------------\n");
 		System.out.println("***New CarPool***");
 		CarPool cp2 = new CarPool("Fuhrpark2");
 		pools.add(cp2.getName(), cp2);
 		System.out.println("CarPool "+cp2.getName()+" created.\n");
 		ElectricCar ec4 = new ElectricCar(44, new PassengerTransport(5));
 		ElectricCar ec5 = new ElectricCar(51, new PassengerTransport(4));
 		ElectricCar ec6 = new ElectricCar(60, new PassengerTransport(9));
 		FuelCar fc4 = new FuelCar(70,new CargoTransport(10, 100));
 		FuelCar fc5 = new FuelCar(80,new CargoTransport(20, 500));
 		FuelCar fc6 = new FuelCar(90, new CargoTransport(10, 300));
 		FuelCar fc7 = new FuelCar(101, new CargoTransport(20, 400));
 		FuelCar fc8 = new FuelCar(120, new PassengerTransport(2));
 		
 		ec4.increaseMileage(10000);
 		ec4.increaseConsumption(300);
 		ec5.increaseMileage(6000);
 		ec5.increaseConsumption(300);
 		ec6.increaseMileage(8000);
 		ec6.increaseConsumption(300);
 		
 		fc4.increaseMileage(12000);
 		fc4.increaseConsumption(60);
 		fc5.increaseMileage(30000);
 		fc5.increaseConsumption(100);
 		fc6.increaseMileage(20000);
 		fc6.increaseConsumption(100);
 		fc7.increaseMileage(2000);
 		fc7.increaseConsumption(100);
 		fc8.increaseMileage(1000);
 		fc8.increaseConsumption(100);
 		cp2.addCar(ec4);
 		cp2.addCar(ec5);
 		cp2.addCar(ec6);
 		cp2.addCar(fc4);
 		cp2.addCar(fc5);
 		cp2.addCar(fc6);
 		cp2.addCar(fc7);
 		cp2.addCar(fc8);
 				
 		System.out.println("***"+cp2.getName()+" "+cp2.getCars().toString());
 		System.out.println("Average Usage of Electric Cars: "+cp2.getAverageElectricUsage(CarPool.Role.ALL)+"kW");
 		System.out.println("Average Usage of Electric Cars for CargoTransport: "+cp2.getAverageElectricUsage(CarPool.Role.CARGOTRANSPORT)+"kW");
 		System.out.println("Average Usage of Electric Cars for PassengerTransport: "+cp2.getAverageElectricUsage(CarPool.Role.PASSENGERTRANSPORT)+"kW");
 		System.out.println("Average Usage of Fuel Cars: "+cp2.getAverageFuelUsage(CarPool.Role.ALL)+"L");
 		System.out.println("Average Usage of Fuel Cars for CargoTransport: "+cp2.getAverageFuelUsage(CarPool.Role.CARGOTRANSPORT)+"L");
 		System.out.println("Average Usage of Fuel Cars for PassengerTransport: "+cp2.getAverageFuelUsage(CarPool.Role.PASSENGERTRANSPORT)+"L");
 
 		
 		fc6.increaseMileage(500);
 		fc6.increaseConsumption(40);
 		System.out.println("\n"+fc6.getClass().getName()+" "+fc6.getId()+" used, new mileage: "+fc6.getMileage()+", current Fuel level: "+fc6.getConsumption());
 		ec5.increaseMileage(800);
 		ec5.increaseConsumption(90);
 		System.out.println(ec5.getClass().getName()+" "+ec5.getId()+" used, new mileage: "+ec5.getMileage()+", current Electric level: "+ec5.getConsumption());
 		System.out.println();
 
 		cp2.removeCar(fc7);
 		
 		System.out.println(fc7.getClass().getName()+" "+fc7.getId()+" removed.\n");
 		System.out.println("***"+cp2.getName()+" new "+cp2.getCars().toString());
 		
 		//-------------------------------CAR POOL 3----------------------------------
 		System.out.println("---------------------------------------------------------\n");
 		System.out.println("***New CarPool***");
 		CarPool cp3 = new CarPool("Fuhrpark3");
 		pools.add(cp3.getName(), cp3);
 		System.out.println("CarPool "+cp3.getName()+" created.");
 
 		ElectricCar ec7 = new ElectricCar(17, new PassengerTransport(4));
 		ElectricCar ec8 = new ElectricCar(18, new PassengerTransport(4));
 		ElectricCar ec9 = new ElectricCar(19, new PassengerTransport(2));
 		ElectricCar ec10 = new ElectricCar(20, new CargoTransport(20, 400));
 		ElectricCar ec11 = new ElectricCar(30, new CargoTransport(50, 700));
 		ElectricCar ec12 = new ElectricCar(31, new CargoTransport(40, 500));
 		
 		FuelCar fc10 = new FuelCar(41, new CargoTransport(10, 100));
 		FuelCar fc11 = new FuelCar(42, new PassengerTransport(2));
 		FuelCar fc12 = new FuelCar(43, new PassengerTransport(5));
 		
 		ec7.increaseMileage(1000);
 		ec7.increaseConsumption(300);
 		ec8.increaseMileage(4500);
 		ec9.increaseMileage(3600);
 		ec9.increaseConsumption(300);
 		ec10.increaseMileage(10200);
 		ec10.increaseConsumption(600);
 		ec11.increaseMileage(11500);
 		ec11.increaseConsumption(400);
 		ec12.increaseMileage(7000);
 		ec12.increaseConsumption(300);
 		
 		fc10.increaseMileage(12000);
 		fc10.increaseConsumption(60);
 		fc11.increaseMileage(30000);
 		fc11.increaseConsumption(100);
 		fc12.increaseMileage(20000);
 		fc12.increaseConsumption(100);
 		
 		cp3.addCar(ec7);
 		cp3.addCar(ec8);
 		cp3.addCar(ec9);
 		cp3.addCar(ec10);
 		cp3.addCar(ec11);
 		cp3.addCar(ec12);
 		cp3.addCar(fc10);
 		cp3.addCar(fc11);
 		cp3.addCar(fc12);
 		
 		System.out.println("\n***"+cp3.getName()+" "+cp3.getCars().toString());
 		System.out.println("Average Usage of Electric Cars: "+cp3.getAverageElectricUsage(CarPool.Role.ALL)+"kW");
 		System.out.println("Average Usage of Electric Cars for CargoTransport: "+cp3.getAverageElectricUsage(CarPool.Role.CARGOTRANSPORT)+"kW");
 		System.out.println("Average Usage of Electric Cars for PassengerTransport: "+cp3.getAverageElectricUsage(CarPool.Role.PASSENGERTRANSPORT)+"kW");
 		System.out.println("Average Usage of Fuel Cars: "+cp3.getAverageFuelUsage(CarPool.Role.ALL)+"L");
 		System.out.println("Average Usage of Fuel Cars for CargoTransport: "+cp3.getAverageFuelUsage(CarPool.Role.CARGOTRANSPORT)+"L");
 		System.out.println("Average Usage of Fuel Cars for PassengerTransport: "+cp3.getAverageFuelUsage(CarPool.Role.PASSENGERTRANSPORT)+"L");
 
 	}
 
 }
