 
 
 
 package overwatch.db;
 
 import java.sql.Connection;
 
 
 
 
 
 /**
  * Database <-> Vehicle interactions
  * 
  * @author  Lee Coakley
  * @version 2
  */
 
 
 
 
 
 public class Vehicles
 {
 	
 	/**
 	 * Check if a vehicle exists.
 	 * @param personNo
 	 * @return name or null
 	 */
 	public static boolean exists( Integer vehicleNo )
 	{
 		EnhancedResultSet ers = Database.query(
 			"select vehicleNo  " +
 		    "from Vehicles     " +
 		    "where vehicleNo = " + vehicleNo + ";"
 		);
 		
 		return ! ers.isEmpty();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Create a new vehicle.
 	 * @return Integer vehicleNo
 	 * TODO: this code may still be useful, generalise it and put it somewhere else
 	 */
 	public static Integer createOLD()
 	{	
 		Connection conn = Database.getConnection();
 		
 		try {
 			Database.lockWrite( conn, "Vehicles" );
 			
 			 EnhancedResultSet ers = Database.query( conn,
 				"select max(vehicleNo)+1 " +
 				"from Vehicles;"
 			);
 			 
 			Long    vehicleNoKey = ers.getElemAs( 0, Long.class );
 			Integer vehicleNo    = (int) (long) vehicleNoKey;
 			
 			Database.update( conn,
 				"insert into Vehicles values (" +
 				    vehicleNo + ", " +
 					"'new vehicle #" + vehicleNo + "'," +
 					"null" +
 				");"
 			);
 			
 			return vehicleNo;
 		}
 		finally {
 			try     { Database.unlock( conn );           }
 			finally { Database.returnConnection( conn ); }
 		}
 	}
 	
 	
 	
 	
 	
 	public static Integer create()
 	{	
 		Database.update(
 			"insert into Vehicles " +
 			"values( default, 'new vehicle', null );"
 		);
 		
		return (int) (long) Database.querySingle( Long.class,
 			"select max(vehicleNo)" +
 			"from Vehicles;"
 		);
 	}
 	
 	
 	
 	
 	
 	public static void delete( Integer vehicleNo )
 	{
 		// TODO: check if vehicle is in use in a squad, and throw an exception or show a warning message if so
 		// TODO: create Squads.usesVehicle( squadNo, vehicleNo )
 		
 		Database.update( 
 			"delete from Vehicles " +
 			"where vehicleNo =    " + vehicleNo + ";"
 		);
 	}
 	
 }
