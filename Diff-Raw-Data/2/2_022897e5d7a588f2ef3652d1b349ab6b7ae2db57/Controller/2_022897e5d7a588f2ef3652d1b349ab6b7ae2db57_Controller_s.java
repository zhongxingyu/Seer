 package vehicleShepard;
 
 import java.util.ArrayList;
 import java.sql.*;
 import java.io.IOException;
 import java.net.*;
 
 public class Controller {
 	
 	public static void main(String[] args) {
 		new Controller();
 	}
 	
 	//The Controller
 	private static Connection connection;
 	
 	//DB Objects
 	private static final UserDB USER = new UserDB();
 	private static final ReservationDB RESV = new ReservationDB();
 	private static final VehicleDB VEHC = new VehicleDB();
 	
 	
 	public Controller()
 	{
 		/*
 		 * We would like to check if our user had connection
 		 * 		to the web, before trying to connect to the
 		 * 		database.
 		 */
 		try 
 		{
 			final URL url = new URL("http://itu.dk/mysql");
 			url.openConnection();
 		} 
 		catch (MalformedURLException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		//Make connection to the database
 		connection = ConnectDB.initConn();
 		
 		//Create the view
 		new View(this);
 		
 		//Makes the connection close at exiting the program
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() 
 		{
 		    public void run() 
 		    {
 		        closeConnection();
 		    }
 		}));
 	}
 	
 	///////////////////////
 	//DATABASE CONNECTION//
 	///////////////////////
 	
 	/**
 	 * Returns the current connection for calls
 	 * @return connection
 	 */
 	public static Connection getConnection()
 	{
 		return connection;
 	}
 	
 	/**
 	 * Close the current connection
 	 */
 	public static void closeConnection()
 	{
 		ConnectDB.closeConn(connection);
 	}
 	
 	public static void checkDBConn(int timeOut)
 	{
 		boolean valid = false;
 		try {
 			//Check if the connection is still valid
 			valid = connection.isValid(timeOut);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		//If the connection is not valid, restart the connection
 		if (valid == false) {
 			closeConnection();
 			connection = ConnectDB.initConn();
 		}
 	}
 	
 	public boolean testConn()
 	{
 		/*
 		 * We would like to check if our user had connection
 		 * 		to the web, before trying to connect to the
 		 * 		database.
 		 */
 		
 		boolean testConn = false;
 		try 
 		{
 			final URL url = new URL("http://itu.dk/mysql");
 			url.openConnection();
 			
 			testConn = true;
 			
 		} 
 		catch (MalformedURLException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return testConn;
 	}
 	
 	/////////////
 	//FUNCTIONS//
 	/////////////
 	
 	public static Vehicle findAvailableVehicle(int typeID, boolean automatic, String fromDate, String toDate)
 	{
 		checkDBConn(3);
 		return VEHC.getAvailableVehicle(typeID, automatic, fromDate, toDate);
 	}
 	
 	/////////
 	//USERS//
 	/////////
 	
 	//CUSTOMER
 	public static Customer getCustomer(int userID)
 	{
 		checkDBConn(3);
 		Object[] info = USER.getUserByID(true, userID);
 		Customer customer = new Customer((int)info[0], (int)info[1], (int)info[2], (String)info[3], (String)info[4], (String)info[5], (String)info[6], (String)info[7], (String)info[8]);
 		return customer;
 	}
 	
 	public static Object[][] getCustomerList()
 	{
 		checkDBConn(3);
 		return USER.getList(true);
 	}
 	
 	public static Object[][] searchCustomers(String searchString)
 	{
 		checkDBConn(3);
 		return USER.getUsers(true, searchString);
 	}
 	
 	/**
 	 * Creates a new user by giving an array of info 
 	 * 		to create this new customer
 	 * @param phone
 	 * @param phoneCode
 	 * @param adress
 	 * @param country
 	 * @param firstName
 	 * @param lastName
 	 * @param licenceNumber
 	 * @param licenceExpDate
 	 */
 	public static void newCustomer(int phone, int phoneCode, String address, String country, String firstName, String lastName, String licenceNumber, String licenceExpDate)
 	{
 		checkDBConn(3);
		Object[] info = new String[8];
 		
 		info[0] = phone;
 		info[1] = phoneCode;
 		info[2] = address;
 		info[3] = country;
 		info[4] = firstName;
 		info[5] = lastName;
 		info[6] = licenceNumber;
 		info[7] = licenceExpDate;
 		
 		//This creates a new 
 		USER.newUser(true, info, new Object[10]);
 	}
 	
 	/**
 	 * Changes information about the customer with the given userID
 	 * @param userID
 	 * @param phone
 	 * @param phoneCode
 	 * @param address
 	 * @param country
 	 * @param firstName
 	 * @param lastName
 	 * @param licenceNumber
 	 * @param licenceExpDate
 	 */
 	public static void updateCustomer(int userID, int phone, int phoneCode, String address, String country, String firstName, String lastName, String licenceNumber, String licenceExpDate)
 	{
 		checkDBConn(3);
 		Object[] info = new Object[8];
 		
 		info[0] = phone;
 		info[1] = phoneCode;
 		info[2] = address;
 		info[3] = country;
 		info[4] = firstName;
 		info[5] = lastName;
 		info[6] = licenceNumber;
 		info[7] = licenceExpDate;
 		
 		USER.updateUserByID(true, userID, info, new Object[10]);
 	}
 	
 	//MECHANIC
 	public static Object[] getMechanic(int userID)
 	{
 		checkDBConn(3);
 		return USER.getUserByID(false, userID);
 	}
 	
 	public static Object[][] getMechanicList()
 	{
 		checkDBConn(3);
 		return USER.getList(false);
 	}
 	
 	public static Object[][] searchMechanics(String searchString)
 	{
 		checkDBConn(3);
 		return USER.getUsers(false, searchString);
 	}
 	
 	/**
 	 * Creates a new mechanic by giving an array of info 
 	 * 		to create this new mechanic
 	 * @param phone
 	 * @param phoneCode
 	 * @param address
 	 * @param country
 	 * @param firmName
 	 */
 	public static void newMechanic(int phone, int phoneCode, String address, String country, String firmName)
 	{
 		checkDBConn(3);
 		Object[] info = new Object[5];
 		
 		info[0] = phone;
 		info[1] = phoneCode;
 		info[2] = address;
 		info[3] = country;
 		info[4] = firmName;
 		
 		USER.newUser(false, new Object[10], info);
 	}
 	
 	/**
 	 * Changes information about the mechanic with the given userID
 	 * @param userID
 	 * @param phone
 	 * @param phoneCode
 	 * @param address
 	 * @param country
 	 * @param firstName
 	 * @param lastName
 	 * @param licenceNumber
 	 * @param licenceExpDate
 	 */
 	public static void updateMechanic(int userID, String phone, String phoneCode, String address, String country, String firmName)
 	{
 		checkDBConn(3);
 		Object[] info = new Object[5];
 		
 		info[0] = phone;
 		info[1] = phoneCode;
 		info[2] = address;
 		info[3] = country;
 		info[4] = firmName;
 		
 		USER.updateUserByID(false, userID, new Object[10], info);
 	}
 	
 	////////////////
 	//RESERVATIONS//
 	////////////////
 	
 	/**
 	 * Returns the reservation with the given resID
 	 * @param resID
 	 * @return reservation
 	 */
 	public static Reservation getReservation(int resID)
 	{
 		checkDBConn(3);
 		return RESV.getReservationByID(resID);
 	}
 	
 	/**
 	 * Returns an ArrayList full of arrayLists containing Reservations. Each inner arrayList contains reservations for a specific vehicle.
 	 * The outer arrayList contains the inner arrayLists, which represents each individual car.
 	 * @param fromDate
 	 * @param toDate
 	 * @return ArrayList<ArrayList<Reservation>> The outer arrayList containing the inner arrayLists with reservations.
 	 */
 	public static ArrayList<ArrayList<Reservation>> getReservationArrayList()
 	{
 		checkDBConn(3);
 		return RESV.getArrayList();
 	}
 	
 	public static Object[][] getReservationList()
 	{
 		checkDBConn(3);
 		return RESV.getList();
 	}
 	
 	public static Object[][] searchReservations(String searchString)
 	{
 		checkDBConn(3);
 		return RESV.getReservation(searchString);
 	}
 	
 	/**
 	 * Creates a new reservation by giving an array of info 
 	 * 		to create this new reservation
 	 * @param userID
 	 * @param typeID
 	 * @param vehicleID
 	 * @param fromDate
 	 * @param toDate
 	 * @param service
 	 */
 	public static void newReservation(int userID, int userType, int typeID, String vehicleID, String fromDate, String toDate, int service)
 	{
 		checkDBConn(3);
 		
 		//Create a new info array of type Object
 		Object[] info = new Object[8];
 		
 		//Fill the newly made array
 		info[0] = userType;
 		info[1] = userID;
 		info[2] = typeID;
 		info[3] = vehicleID;
 		info[4] = fromDate;
 		info[5] = toDate;
 		info[6] = toDate;
 		info[7] = service;
 		
 		//Give the method in ReservationDB the information
 		RESV.newReservation(-1, false, info);
 	}
 	
 	public static void newReservationByID(int resID, int userID, int userType, int typeID, String vehicleID, String fromDate, String toDate, int service)
 	{
 checkDBConn(3);
 		
 		//Create a new info array of type Object
 		Object[] info = new Object[8];
 		
 		//Fill the newly made array
 		info[0] = userType;
 		info[1] = userID;
 		info[2] = typeID;
 		info[3] = vehicleID;
 		info[4] = fromDate;
 		info[5] = toDate;
 		info[6] = toDate;
 		info[7] = service;
 		
 		//Give the method in ReservationDB the information
 		RESV.newReservation(resID, true, info);
 	}
 	
 	public static void removeReservation(int resID)
 	{
 		checkDBConn(3);
 		RESV.removeReservation(resID);
 	}
 	
 	////////////
 	//VEHICLES//
 	////////////
 	
 	public static Vehicle getVehicle(String vehicleID)
 	{
 		checkDBConn(3);
 		return VEHC.getVehicleByID(vehicleID);
 	}
 	
 	public static Object[][] getVehicleList()
 	{
 		checkDBConn(3);
 		return VEHC.getList();
 	}
 	
 	public static Object[][] searchVehicles(String searchString)
 	{
 		checkDBConn(3);
 		return VEHC.getVehicles(searchString);
 	}
 	
 	/**
 	 * Creates a new vehicle by giving an array of info 
 	 * 		to create this new vehicle
 	 * @param vehicleID
 	 * @param make
 	 * @param model
 	 * @param odometer
 	 * @param fuel
 	 * @param automatic
 	 * @param statusID
 	 * @param typeID
 	 */
 	public static void newVehicle(String vehicleID, String make, String model, int odometer, int fuel, boolean automatic, int statusID, int typeID)
 	{
 		checkDBConn(3);
 		Object[] info = new Object[8];
 		
 		info[0] = vehicleID;
 		info[1] = make;
 		info[2] = model;
 		info[3] = odometer;
 		info[4] = fuel;
 		info[5] = automatic;
 		info[6] = statusID;
 		info[7] = typeID;
 		
 		VEHC.newVehicle(info);
 	}
 	
 	////////////////
 	//VEHICLETYPES//
 	////////////////
 	
 	public static String[] getVehTypeNames()
 	{
 		checkDBConn(3);
 		return VEHC.getVehicleTypeNames();
 	}
 	
 	public static int[] getVehTypePrices()
 	{
 		checkDBConn(3);
 		return VEHC.getVehicleTypePrices();
 	}
 }
