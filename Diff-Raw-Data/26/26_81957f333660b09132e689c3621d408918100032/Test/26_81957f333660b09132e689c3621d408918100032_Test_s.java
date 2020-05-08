 import java.util.ArrayList;
 
 import vehicles.Car;
 import vehicles.Quad;
 import vehicles.Vehicle;
 import vehicles.VehicleDetail;
 import vehicles.enums.BodyType;
 import vehicles.enums.Brand;
 import vehicles.enums.Color;
 import vehicles.enums.EngineModel;
 import vehicles.enums.Model;
 import db.Db;
 import db.VehicleMapper;
 import factories.Factory;
 
 
 public class Test {
 	
 	public static void main (String arg []) {
 		
 		Db db = Db.getInstance();
 		
 		
		VehicleDetail vd = new VehicleDetail(Model.A3, EngineModel._3000TDI,
				BodyType.SEDAN, Color.GRANATOWY, 50000, Brand.AUDI);
		vd.priceForSale = 55000;
 		
 		Vehicle v = Factory.createVehicle(vd);
 		
 		
 		VehicleMapper vm = new VehicleMapper();
 //		Vehicle v2 = vm.getSingle(1);
 		ArrayList<Vehicle> vehicles = vm.getAll();
 		
 		
 		for (Vehicle vehicle : vehicles) {
 			System.out.println(vehicle);
 			System.out.println();
 		}
 		
 		Vehicle vehicel = Factory.createVehicle(vd);
 		
 		System.out.println(vm.getMaxId());
 		
 		
 		System.out.println("ok");
 	}
 
 }
