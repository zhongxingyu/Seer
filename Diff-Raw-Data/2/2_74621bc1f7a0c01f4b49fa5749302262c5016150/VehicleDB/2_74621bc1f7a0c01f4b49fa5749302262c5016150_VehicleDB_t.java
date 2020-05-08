 package vehicleShepard;
 
 /*
  * This class is controlling the methods containing 
  * 		methods using our database
  * This implies:
  * 		getting an available vehicle
  * 		making a new vehicle
  * 		getting a vehicle by its ID
  * 		getting a list of vehicles
  * 		getting a list of vehicles (after search)
  */
 
 import java.sql.*;
 import java.util.ArrayList;
 
 public class VehicleDB 
 {
 	
 	/////////////
 	//FUNCTIONS//
 	/////////////
 	
 	/**
 	 * This method returns a vehicle with the indicated parameters available for rent within the indicated period
 	 * @param typeID The vehicle typeID to search for
 	 * @param automatic The type of gear
 	 * @param fromDate The from date to look for
 	 * @param toDate The to date to look for
 	 * @return availableVehicle The available vehicle with the lowest value of odometer, or null if no one is available
 	 */
 public Vehicle getAvailableVehicle(int typeID, boolean automatic, java.sql.Date fromDate, java.sql.Date toDate)
 	{
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		/*
 		 * If no vehicle is added because of the soon
 		 * 		to follow code, we will return null
 		 */
 		Vehicle availableVehicle = null;
 		int auto = 0;
 		if(automatic == true)
 		{
 			auto = 1;
 		}
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			
 			/*
 			 * We select vehicles from the Vehicle database
 			 * 		and order them by odometer (becuase we 
 			 * 		want to rent out the one with the 
 			 * 		fewest km/miles driven)
 			 * Furthermore we would like the typeID 
 			 * 		and geartype (automatic) to be the 
 			 * 		one mentioned in the methods parameters
 			 * Then we check the reservation database for 
 			 * 		vehicles with the same ID as those in
 			 * 		the vehicle database
 			 * Thereafter we check the dates. For a far more
 			 * 		descriptive description look it up
 			 * 		in the report
 			 */
 			
			s.executeQuery("SELECT * FROM Vehicle WHERE typeID=" + typeID + " AND automatic=" + auto + " AND NOT EXISTS(SELECT vehicleID FROM Reservation WHERE (Reservation.vehicleID=Vehicle.vehicleID AND ((fromDate<'" + fromDate + "' AND extendedDate>'" + fromDate + "') OR (fromDate>'" + fromDate + "' AND extendedDate<'" + toDate + "') OR (fromDate<'" + toDate + "' AND extendedDate>'" + toDate + "'))))  ORDER BY odometer");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then the first line because it has
 			 * 		allready been sorted for us
 			 */
 			
 			if (rs.next()) 
 			{
 				availableVehicle = new Vehicle(rs.getString("vehicleID"), rs.getString("make"), rs.getString("model"), rs.getInt("odometer"), rs.getInt("fuelID"), rs.getBoolean("automatic"), rs.getInt("statusID"), rs.getInt("typeID"));
 			}
 			
 			//s is closed
 			s.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//Returns the available vehicle with the lowest number in odometer, or null if no one is available
 		return availableVehicle;
 	}
 	
 	////////////
 	//VEHICLES//
 	////////////
 	
 	/**
 	 * This method creates a new vehicle in our database
 	 * This happens by the help of an array
 	 * @param info
 	 */
 	public void newVehicle(Object[] info)
 	{
 		int vehicleID = getNumberOfVehicles() + 1;
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		Statement s;
 		
 		//We insert the needed data into our database
 		
 		try 
 		{
 			s = conn.createStatement();
 			
 			try 
 			{
 				/*
 				 * We add a new vehicle to our database, by
 				 * 		putting in the different kinds of
 				 * 		info from an array hat is given to 
 				 * 		us as a parameter
 				 */
 				s.executeUpdate("INSERT INTO Vehicle (`vehicleID`, `make`, `model`, `odometer`, `fuelID`, `automatic`, `statusID`, `typeID`) VALUES ('" + vehicleID + "', '" + info[0] + "', '" + info[1] + "', '" + info[2] + "', '" + info[3] + "', '" + info[4] + "', '" + info[5] + "', '" + info[6] + "')");
 				s.close();
 			} 
 			catch (SQLException e) 
 			{
 				e.printStackTrace();
 			}
 		} 
 		catch (SQLException e1) 
 		{
 			e1.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method finds a vehicle, from the given name
 	 * @param vehicleID
 	 * @return vehicle
 	 */
 	public Vehicle getVehicleByID(String vehicleID)
 	{
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		Vehicle vehicle = null;
 		
 		Statement s;
 		try 
 		{
 			s = conn.createStatement();
 			
 			/*
 			 * We get a spicifik vehicle from the database 
 			 * 		using the ID (the parameter for the 
 			 * 		method)
 			 */
 			s.executeQuery("SELECT * FROM Vehicle WHERE vehicleID='" + vehicleID + "'");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		put a result in our array
 			 */
 			
 			while(rs.next()) 
 			{
 				vehicle = new Vehicle(rs.getString("vehicleID"), rs.getString("make"), rs.getString("model"), rs.getInt("odometer"), rs.getInt("fuelID"), rs.getBoolean("automatic"), rs.getInt("StatusID"), rs.getInt("typeID"));
 			}
 			
 			s.close();
 		}
 		catch (SQLException e) 
 		{
 			e.printStackTrace();			
 		}
 		
 		return vehicle;
 	}
 	
 	/**
 	 * This method gives us the number of vehicles in 
 	 * 		our database
 	 * @return number
 	 */
 	private int getNumberOfVehicles()
 	{
 		int count = 0;
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT vehicleID FROM Vehicle");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		count +1 each time
 			 */
 			
 			while(rs.next())
 			{
 				count++;
 			}
 			
 			s.close();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return count;
 	}
 	
 	/**
 	 * This method creates a list of vehicles in our database
 	 * @return vehicles
 	 */
 	public Object[][] getList()
 	{
 		int number = getNumberOfVehicles();
 		int count = 0;
 		//We want a list of customers in a 2D Array
 		Object[][] vehicleList = new Object[number][8]; 		
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT * FROM Vehicle");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		put a result in our array
 			 */
 			
 			while(rs.next())
 			{
 				vehicleList[count][0] = rs.getString("vehicleID");
 				vehicleList[count][1] = rs.getString("make");
 				vehicleList[count][2] = rs.getString("model");
 				vehicleList[count][3] = rs.getInt("odometer");
 				vehicleList[count][4] = rs.getInt("fuelID");
 				vehicleList[count][5] = rs.getInt("automatic");
 				vehicleList[count][6] = rs.getInt("statusID");
 				vehicleList[count][7] = rs.getInt("typeID");
 				
 				count++;
 			}
 			
 			s.close();
 		} 
 		
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return vehicleList;
 	}
 	
 	/**
 	 * This method gives us a list of vehicles after a search
 	 * @param searchString
 	 * @return vehicles
 	 */
 	public Object[][] getVehicles(String searchString)
 	{		
 		//We make it easier to analyze
 		String searchTerm = searchString.toLowerCase().trim();
 		Object[][] vehicleList = getList();
 		
 		int number = getNumberOfVehicles();
 		
 		/*
 		 * We use our search method, by giving the needed parameters
 		 * 		and it returns an array
 		 */
 		
 		Object[][] vehicles = Search.stringSearch(searchTerm, vehicleList, number, 8);
 		
 		return vehicles;
 	}
 	
 	////////////////
 	//VEHICLETYPES//
 	////////////////
 	
 	/**
 	 * This method finds and returns the number 
 	 * 		of vehicle types we have in our database. 		
 	 * @return number
 	 */
 	private int getNumberOfVehicleTypes()
 	{
 		int count = 0;
 		
 		//We get the connection from Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT vehicleTypeID FROM VehicleType");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		count +1 each time
 			 */
 			
 			while(rs.next())
 			{
 				count++;
 			}
 			
 			s.close();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return count;
 	}
 	
 	/**
 	 * Returns the names of all vehicle types in an array of strings
 	 * @return vehTypeNames The names of all vehicle types in an array of strings
 	 */
 	public String[] getVehicleTypeNames()
 	{
 		int count = 0;
 		int number = getNumberOfVehicleTypes();
 		String[] vehTypeNames = new String[number];
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT name FROM VehicleType ORDER BY vehicleTypeID");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * We take the string from our resultset
 			 * 		and put it into an array.
 			 * Thereafter we count +1 so that the next
 			 * 		name is put on the next spot. 
 			 */
 			
 			while(rs.next())
 			{
 				vehTypeNames[count] = rs.getString("name");
 				count++;
 			}
 			
 			s.close();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return vehTypeNames;
 	}
 	
 	/**
 	 * Returns the price rates of all vehicle types in an array of ints
 	 * @return vehTypePrices The price rates of all vehicle types in an array of ints
 	 */
 	public int[] getVehicleTypePrices()
 	{
 		int count = 0;
 		int number = getNumberOfVehicleTypes();
 		int[] vehTypePrices = new int[number];
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			s.executeQuery("SELECT priceRate FROM VehicleType ORDER BY vehicleTypeID");
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * For each line in the resultset, we take the
 			 * 		priceRate and put it in our array
 			 * Thereafter we count +1 so that the next
 			 * 		price is put on the next spot
 			 */
 			
 			while(rs.next())
 			{
 				vehTypePrices[count] = rs.getInt("priceRate");
 				count++;
 			}
 			
 			s.close();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return vehTypePrices;
 	}
 }
