 
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		SimpleMap1 pools = new SimpleMap1();
 		System.out.println("***New CarPool***");
 		CarPool cp1 = new CarPool("Fuhrpark1");
 		pools.add(cp1.getName(), cp1);
 		System.out.println("CarPool "+cp1.getName()+" created.\n");
 		System.out.println("***New Cars***");
 		ElectricCar ec1 = new ElectricCar(01);
 		ElectricCar ec2 = new ElectricCar(02);
 		ElectricCar ec3 = new ElectricCar(03);
 		FuelCar fc1 = new FuelCar(04);
 		FuelCar fc2 = new FuelCar(05);
 		FuelCar fc3 = new FuelCar(06);
 				
 		ec1.increaseMileage(3000);
 		ec1.increaseUsedPower(130);
 		ec2.increaseMileage(6000);
 		ec2.increaseUsedPower(460);
 		ec3.increaseMileage(12000);
 		ec3.increaseUsedPower(8000);
 		
 		fc1.increaseMileage(10000);
 		fc1.increaseUsedFuel(20);
 		fc2.increaseMileage(15000);
 		fc2.increaseUsedFuel(30);
 		fc3.increaseMileage(20000);
 		fc3.increaseUsedFuel(40);
 		
 		cp1.addCar(ec1);
 		cp1.addCar(ec2);
 		cp1.addCar(ec3);
 		cp1.addCar(fc1);
 		cp1.addCar(fc2);
 		cp1.addCar(fc3);
 		System.out.println("------------------"+cp1.getCars().toString()+"-------------------");
 		
 //		System.out.println("Car List of "+cp1.getName()+":");
 //		System.out.println(ec1.getClass().getName()+" "+ec1.getId()+", "+ec1.getMileage()+" miles, "+ec1.getUsedPower()+"kW used Power");
 //		System.out.println(ec2.getClass().getName()+" "+ec2.getId()+", "+ec2.getMileage()+" miles, "+ec2.getUsedPower()+"kW used Power");
 //		System.out.println(ec3.getClass().getName()+" "+ec3.getId()+", "+ec3.getMileage()+" miles, "+ec3.getUsedPower()+"kW used Power");
 //		System.out.println(fc1.getClass().getName()+" "+fc1.getId()+", "+fc1.getMileage()+" miles, "+fc1.getUsedFuel()+"L used Fuel");
 //		System.out.println(fc2.getClass().getName()+" "+fc2.getId()+", "+fc2.getMileage()+" miles, "+fc2.getUsedFuel()+"L used Fuel");
 //		System.out.println(fc3.getClass().getName()+" "+fc3.getId()+", "+fc3.getMileage()+" miles, "+fc3.getUsedFuel()+"L used Fuel");
 //		System.out.println();
 
 		cp1.removeCar(fc1);
 		System.out.println("------------------"+cp1.getCars().toString()+"-------------------");
 
 		//System.out.println(fc3.getClass().getName()+" "+fc3.getId()+" removed.");
 		System.out.println();
 		
 		System.out.println("########"+cp1.getCar(01));
 		
 		
 		fc1.increaseMileage(200);
 		fc1.increaseUsedFuel(20);
 		System.out.println(fc1.getClass().getName()+" "+fc1.getId()+" used, new mileage: "+fc1.getMileage()+", current Fuel level: "+fc1.getUsedFuel());
 		ec2.increaseMileage(500);
 		ec2.increaseUsedPower(50);
 		System.out.println(ec2.getClass().getName()+" "+ec2.getId()+" used, new mileage: "+ec2.getMileage()+", current Electric level: "+ec2.getUsedPower());
 		System.out.println("\n");
 		
 /**		//---------------------------CAR POOL 2-----------------------------
 		System.out.println("**************************************");
 		System.out.println("***New CarPool***");
 		CarPool cp2 = new CarPool("Fuhrpark2");
 		pools.add(cp2.getName(), cp2);
 		System.out.println("CarPool "+cp2.getName()+" created.\n");
 		System.out.println("***New Cars***");
 		ElectricCar ec4 = new ElectricCar(40);
 		ElectricCar ec5 = new ElectricCar(50);
 		ElectricCar ec6 = new ElectricCar(60);
 		FuelCar fc4 = new FuelCar(70);
 		FuelCar fc5 = new FuelCar(80);
 		FuelCar fc6 = new FuelCar(90);
 		FuelCar fc7 = new FuelCar(101);
 		FuelCar fc8 = new FuelCar(120);
 		
 		ec4.increaseMileage(10000);
 		ec4.increaseUsedPower(300);
 		ec5.increaseMileage(6000);
 		ec5.increaseUsedPower(300);
 		ec6.increaseMileage(8000);
 		ec6.increaseUsedPower(300);
 		
 		fc4.increaseMileage(12000);
 		fc4.increaseUsedFuel(60);
 		fc5.increaseMileage(30000);
 		fc5.increaseUsedFuel(100);
 		fc6.increaseMileage(20000);
 		fc6.increaseUsedFuel(100);
 		fc7.increaseMileage(2000);
 		fc7.increaseUsedFuel(100);
 		fc8.increaseMileage(1000);
 		fc8.increaseUsedFuel(100);
 		cp2.addCar(ec4);
 		cp2.addCar(ec5);
 		cp2.addCar(ec6);
 		cp2.addCar(fc4);
 		cp2.addCar(fc5);
 		cp2.addCar(fc6);
 		cp2.addCar(fc7);
 		cp2.addCar(fc8);
 		
 		System.out.println("Car List of "+cp2.getName()+":");
 		System.out.println(ec4.getClass().getName()+" "+ec4.getId()+", "+ec4.getMileage()+" miles, "+ec4.getUsedPower()+"kW used Power");
 		System.out.println(ec5.getClass().getName()+" "+ec5.getId()+", "+ec5.getMileage()+" miles, "+ec5.getUsedPower()+"kW used Power");
 		System.out.println(ec6.getClass().getName()+" "+ec6.getId()+", "+ec6.getMileage()+" miles, "+ec6.getUsedPower()+"kW used Power");
 		System.out.println(fc4.getClass().getName()+" "+fc4.getId()+", "+fc4.getMileage()+" miles, "+fc4.getUsedFuel()+"L used Fuel");
 		System.out.println(fc5.getClass().getName()+" "+fc5.getId()+", "+fc5.getMileage()+" miles, "+fc5.getUsedFuel()+"L used Fuel");
 		System.out.println(fc6.getClass().getName()+" "+fc6.getId()+", "+fc6.getMileage()+" miles, "+fc6.getUsedFuel()+"L used Fuel");
 		System.out.println(fc7.getClass().getName()+" "+fc7.getId()+", "+fc7.getMileage()+" miles, "+fc7.getUsedFuel()+"L used Fuel");
 		System.out.println(fc8.getClass().getName()+" "+fc8.getId()+", "+fc8.getMileage()+" miles, "+fc8.getUsedFuel()+"L used Fuel");
 
 		System.out.println();
 		
 		fc6.increaseMileage(500);
 		fc6.increaseUsedFuel(40);
 		System.out.println(fc6.getClass().getName()+" "+fc6.getId()+" used, new mileage: "+fc6.getMileage()+", current Fuel level: "+fc6.getUsedFuel());
 		ec5.increaseMileage(800);
 		ec5.increaseUsedPower(90);
 		System.out.println(ec5.getClass().getName()+" "+ec5.getId()+" used, new mileage: "+ec5.getMileage()+", current Electric level: "+ec5.getUsedPower());
 		System.out.println();
 
 		//cp2.removeCar(fc7);
 		System.out.println();
 		
 		System.out.println(fc7.getClass().getName()+" "+fc7.getId()+" removed.");
 		System.out.println("\n");
 		
 		//-------------------------------CAR POOL 3----------------------------------
 		System.out.println("**************************************");
 		System.out.println("***New CarPool***");
 		CarPool cp3 = new CarPool("Fuhrpark3");
 		pools.add(cp3.getName(), cp3);
 		System.out.println("CarPool "+cp3.getName()+" created.\n");
 		System.out.println("***New Cars***");
 		ElectricCar ec7 = new ElectricCar(17);
 		ElectricCar ec8 = new ElectricCar(18);
 		ElectricCar ec9 = new ElectricCar(19);
 		ElectricCar ec10 = new ElectricCar(20);
 		ElectricCar ec11 = new ElectricCar(30);
 		ElectricCar ec12 = new ElectricCar(31);
 		
 		FuelCar fc10 = new FuelCar(41);
 		FuelCar fc11 = new FuelCar(42);
 		FuelCar fc12 = new FuelCar(43);
 		
 		ec7.increaseMileage(7000);
 		ec7.increaseUsedPower(300);
 		ec8.increaseMileage(7000);
 		ec8.increaseUsedPower(300);
 		ec9.increaseMileage(7000);
 		ec9.increaseUsedPower(300);
 		ec10.increaseMileage(7000);
 		ec10.increaseUsedPower(300);
 		ec11.increaseMileage(7000);
 		ec11.increaseUsedPower(300);
 		ec12.increaseMileage(7000);
 		ec12.increaseUsedPower(300);
 		
 		fc10.increaseMileage(12000);
 		fc10.increaseUsedFuel(60);
 		fc11.increaseMileage(30000);
 		fc11.increaseUsedFuel(100);
 		fc12.increaseMileage(20000);
 		fc12.increaseUsedFuel(100);
 		
 		cp3.addCar(ec7);
 		cp3.addCar(ec8);
 		cp3.addCar(ec9);
 		cp3.addCar(ec10);
 		cp3.addCar(ec11);
 		cp3.addCar(ec12);
 		cp3.addCar(fc10);
 		cp3.addCar(fc11);
 		cp3.addCar(fc12);
 		
 		System.out.println("Car List of "+cp3.getName()+":");
 		System.out.println(ec7.getClass().getName()+" "+ec7.getId()+", "+ec7.getMileage()+" miles, "+ec7.getUsedPower()+"kW used Power");
 		System.out.println(ec8.getClass().getName()+" "+ec8.getId()+", "+ec8.getMileage()+" miles, "+ec8.getUsedPower()+"kW used Power");
 		System.out.println(ec9.getClass().getName()+" "+ec9.getId()+", "+ec9.getMileage()+" miles, "+ec9.getUsedPower()+"kW used Power");
 		System.out.println(ec10.getClass().getName()+" "+ec10.getId()+", "+ec10.getMileage()+" miles, "+ec10.getUsedPower()+"kW used Power");
 		System.out.println(ec11.getClass().getName()+" "+ec11.getId()+", "+ec11.getMileage()+" miles, "+ec11.getUsedPower()+"kW used Power");
 		System.out.println(ec12.getClass().getName()+" "+ec12.getId()+", "+ec12.getMileage()+" miles, "+ec12.getUsedPower()+"kW used Power");
 
 		System.out.println(fc10.getClass().getName()+" "+fc10.getId()+", "+fc10.getMileage()+" miles, "+fc10.getUsedFuel()+"L used Fuel");
 		System.out.println(fc11.getClass().getName()+" "+fc11.getId()+", "+fc11.getMileage()+" miles, "+fc11.getUsedFuel()+"L used Fuel");
 		System.out.println(fc12.getClass().getName()+" "+fc12.getId()+", "+fc12.getMileage()+" miles, "+fc12.getUsedFuel()+"L used Fuel");
 		
 		System.out.println();
 */		
 	}
 
 }
