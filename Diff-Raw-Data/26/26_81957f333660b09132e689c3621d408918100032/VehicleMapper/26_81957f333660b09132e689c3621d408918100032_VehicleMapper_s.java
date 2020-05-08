 package db;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import factories.Factory;
 
 import vehicles.Car;
 import vehicles.Vehicle;
 import vehicles.VehicleDetail;
 import vehicles.enums.BodyType;
 import vehicles.enums.Brand;
 import vehicles.enums.Color;
 import vehicles.enums.EngineModel;
 import vehicles.enums.Model;
 
 public class VehicleMapper {
 	
 	Db db = null;
 	
 	
 	public VehicleMapper() {
 		
 		db = Db.getInstance();
 	}
 	
 	
 	
 	public Vehicle getSingle(int id) {
 		
 		String sql = "SELECT * FROM Vehicle "
 				+ "WHERE Vehicle.id='"+ id +"'";		
 
 		ResultSet resultSet = getResultSetFromQuery(sql);		
 		VehicleDetail vd = getVehicleDetail(resultSet);
 		Vehicle v = Factory.createVehicle(vd);
 		
 		return v;
 	}
 	
 	private ResultSet getResultSetFromQuery(String sql)	{
 		
 		ResultSet resultSet;
 		
 		try {
 			
 			resultSet = db.executeQuery(sql);
 			resultSet.next();
 			return resultSet;
 			
 		} catch (SQLException e) {
 			
 			System.out.println(e);
 			return null;
 		}		
 		
 	}
 	
 	private VehicleDetail getVehicleDetail(ResultSet resultSet) {		
 		
 		try {
 			
 			int id = resultSet.getInt("Vehicle.id");
 			int purchasePrice = resultSet.getInt("Vehicle.purchasePrice");
 			int priceForSale = resultSet.getInt("Vehicle.priceForSale");
 			Brand brand = Brand.valueOf(resultSet.getString("Vehicle.brand"));
 			Model model = Model.valueOf(resultSet.getString("Vehicle.model"));
 			Color color = Color.valueOf(resultSet.getString("Vehicle.color"));
 			EngineModel engine = EngineModel.valueOf(
 					resultSet.getString("Vehicle.engine"));
			BodyType bodyType = BodyType.valueOf(
					resultSet.getString("Vehicle.BodyType"));
 			
 			VehicleDetail vd = new VehicleDetail(model, engine, bodyType,
 					color, purchasePrice, brand);
 			vd.id = id;
 			vd.priceForSale = priceForSale;
 			
 			return vd;
 			
 		} catch (SQLException e) {
 			System.out.println(e);
 			return null;
 		}
 	}
 	
 	public ArrayList<Vehicle> getAll() {
 		
 		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
 		String sql = "SELECT * FROM Vehicle "
 				   + "ORDER BY Vehicle.id";		
 		
 		ResultSet resultSet = getResultSetFromQuery(sql);
 		
 		try {
 			
 			VehicleDetail vd = getVehicleDetail(resultSet);
 			Vehicle v = Factory.createVehicle(vd);
 			vehicles.add(v);
 			
 			while ( resultSet.next() ) {
 				
 				vd = getVehicleDetail(resultSet);
 				v = Factory.createVehicle(vd);
 				vehicles.add(v);
 			}
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		
 		return vehicles;		
 	}
 	
 	public void addNew(Vehicle v) {
 		
 		String sql = "INSERT INTO Vehicle VALUES(" +
 		"default, " +
 		"'" + v.getBrand().toString() + "', " +
 		"'" + v.getModel().toString() + "', " +
 		"" + v.getPurchasePrice() + ", " +
 		"" + v.getPriceForSale() + ", " +
 		"'" + v.getColor() + "', " +
 		"'" + getBodyType(v) + "', " +
 		"'" + v.getEngine() + "')";
 		
 		
 		try {
 			db.executeUpdate(sql);
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 	}
 	
 	private BodyType getBodyType(Vehicle v) {
 		
 		if (v instanceof Car) {
 			
 			Car c = (Car)(v);
 			return c.getBodyType();
 		}
 		else
 			return null;
 	}
 	
 	public void delete(int id) {
 		
 		String sql = "DELETE FROM Vehicle "
 			+ "WHERE Vehicle.id='"+ id +"'";		
 
 		try {
 			db.executeUpdate(sql);
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 	}
 	
 	public int getMaxId() {
 		
 		String sql = "select MAX(id) from Vehicle";
 		
 		ResultSet resultSet = getResultSetFromQuery(sql);
 		
 		int id = -1;
 		try {
 			
 			id = resultSet.getInt(1);
 		} catch (SQLException e) {
 			
 			System.out.println(e);
 		}
 		
 		return id;
 	}
 }
