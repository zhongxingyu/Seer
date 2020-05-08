 // -------------------------------
 // adapated from Kevin T. Manley
 // CSE 593
 //
 package ResImpl;
 
 import ResInterface.*;
 
 import java.util.*;
 import java.rmi.*;
 
 import java.rmi.registry.Registry;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 //public class ResourceManagerImpl extends java.rmi.server.UnicastRemoteObject
 public class ResourceManagerImpl
 	implements ResourceManager {
 	
 	protected RMHashtable m_itemHT = new RMHashtable();
 
 
 	public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
 
          if (args.length == 1) {
              server = server + ":" + args[0];
          } else if (args.length != 0 &&  args.length != 1) {
              System.err.println ("Wrong usage");
              System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
              System.exit(1);
          }
 	 
 	 try 
 	     {
 		 // create a new Server object
 		 ResourceManagerImpl obj = new ResourceManagerImpl();
 		 // dynamically generate the stub (client proxy)
 		 ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
 		 
 		 // Bind the remote object's stub in the registry
 		 Registry registry = LocateRegistry.getRegistry();
		 registry.rebind("MyGroupResourceManager", rm);
 		 
 		 System.err.println("Server ready");
 	     } 
 	 catch (Exception e) 
 	     {
 		 System.err.println("Server exception: " + e.toString());
 		 e.printStackTrace();
 	     }
 	 
          // Create and install a security manager
          if (System.getSecurityManager() == null) {
 	     System.setSecurityManager(new RMISecurityManager());
          }
 	 
 	 
 	 public ResourceManagerImpl() throws RemoteException {
 	 }
 	 
 
 	// Reads a data item
 	private RMItem readData( int id, String key )
 	{
 		synchronized(m_itemHT){
 			return (RMItem) m_itemHT.get(key);
 		}
 	}
 
 	// Writes a data item
 	private void writeData( int id, String key, RMItem value )
 	{
 		synchronized(m_itemHT){
 			m_itemHT.put(key, value);
 		}
 	}
 	
 	// Remove the item out of storage
 	protected RMItem removeData(int id, String key){
 		synchronized(m_itemHT){
 			return (RMItem)m_itemHT.remove(key);
 		}
 	}
 	
 	
 	// deletes the entire item
 	protected boolean deleteItem(int id, String key)
 	{
 		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
 		ReservableItem curObj = (ReservableItem) readData( id, key );
 		// Check if there is such an item in the storage
 		if( curObj == null ) {
 			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
 			return false;
 		} else {
 			if(curObj.getReserved()==0){
 				removeData(id, curObj.getKey());
 				Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
 				return true;
 			}
 			else{
 				Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
 				return false;
 			}
 		} // if
 	}
 	
 
 	// query the number of available seats/rooms/cars
 	protected int queryNum(int id, String key) {
 		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
 		ReservableItem curObj = (ReservableItem) readData( id, key);
 		int value = 0;  
 		if( curObj != null ) {
 			value = curObj.getCount();
 		} // else
 		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
 		return value;
 	}	
 	
 	// query the price of an item
 	protected int queryPrice(int id, String key){
 		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
 		ReservableItem curObj = (ReservableItem) readData( id, key);
 		int value = 0; 
 		if( curObj != null ) {
 			value = curObj.getPrice();
 		} // else
 		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
 		return value;		
 	}
 	
 	// reserve an item
 	protected boolean reserveItem(int id, int customerID, String key, String location){
 		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
 		// Read customer object if it exists (and read lock it)
 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
 		if( cust == null ) {
 			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
 			return false;
 		} 
 		
 		// check if the item is available
 		ReservableItem item = (ReservableItem)readData(id, key);
 		if(item==null){
 			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
 			return false;
 		}else if(item.getCount()==0){
 			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
 			return false;
 		}else{			
 			cust.reserve( key, location, item.getPrice());		
 			writeData( id, cust.getKey(), cust );
 			
 			// decrease the number of available items in the storage
 			item.setCount(item.getCount() - 1);
 			item.setReserved(item.getReserved()+1);
 			
 			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
 			return true;
 		}		
 	}
 	
 	// Create a new flight, or add seats to existing flight
 	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
 	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
 		throws RemoteException
 	{
 		Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
 		Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
 		if( curObj == null ) {
 			// doesn't exist...add it
 			Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
 			writeData( id, newObj.getKey(), newObj );
 			Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
 					flightSeats + ", price=$" + flightPrice );
 		} else {
 			// add seats to existing flight and update the price...
 			curObj.setCount( curObj.getCount() + flightSeats );
 			if( flightPrice > 0 ) {
 				curObj.setPrice( flightPrice );
 			} // if
 			writeData( id, curObj.getKey(), curObj );
 			Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
 		} // else
 		return(true);
 	}
 
 
 	
 	public boolean deleteFlight(int id, int flightNum)
 		throws RemoteException
 	{
 		return deleteItem(id, Flight.getKey(flightNum));
 	}
 
 
 
 	// Create a new room location or add rooms to an existing location
 	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
 	public boolean addRooms(int id, String location, int count, int price)
 		throws RemoteException
 	{
 		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
 		Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
 		if( curObj == null ) {
 			// doesn't exist...add it
 			Hotel newObj = new Hotel( location, count, price );
 			writeData( id, newObj.getKey(), newObj );
 			Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
 		} else {
 			// add count to existing object and update price...
 			curObj.setCount( curObj.getCount() + count );
 			if( price > 0 ) {
 				curObj.setPrice( price );
 			} // if
 			writeData( id, curObj.getKey(), curObj );
 			Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
 		} // else
 		return(true);
 	}
 
 	// Delete rooms from a location
 	public boolean deleteRooms(int id, String location)
 		throws RemoteException
 	{
 		return deleteItem(id, Hotel.getKey(location));
 		
 	}
 
 	// Create a new car location or add cars to an existing location
 	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
 	public boolean addCars(int id, String location, int count, int price)
 		throws RemoteException
 	{
 		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
 		Car curObj = (Car) readData( id, Car.getKey(location) );
 		if( curObj == null ) {
 			// car location doesn't exist...add it
 			Car newObj = new Car( location, count, price );
 			writeData( id, newObj.getKey(), newObj );
 			Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
 		} else {
 			// add count to existing car location and update price...
 			curObj.setCount( curObj.getCount() + count );
 			if( price > 0 ) {
 				curObj.setPrice( price );
 			} // if
 			writeData( id, curObj.getKey(), curObj );
 			Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
 		} // else
 		return(true);
 	}
 
 
 	// Delete cars from a location
 	public boolean deleteCars(int id, String location)
 		throws RemoteException
 	{
 		return deleteItem(id, Car.getKey(location));
 	}
 
 
 
 	// Returns the number of empty seats on this flight
 	public int queryFlight(int id, int flightNum)
 		throws RemoteException
 	{
 		return queryNum(id, Flight.getKey(flightNum));
 	}
 
 	// Returns the number of reservations for this flight. 
 //	public int queryFlightReservations(int id, int flightNum)
 //		throws RemoteException
 //	{
 //		Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
 //		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
 //		if( numReservations == null ) {
 //			numReservations = new RMInteger(0);
 //		} // if
 //		Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
 //		return numReservations.getValue();
 //	}
 
 
 	// Returns price of this flight
 	public int queryFlightPrice(int id, int flightNum )
 		throws RemoteException
 	{
 		return queryPrice(id, Flight.getKey(flightNum));
 	}
 
 
 	// Returns the number of rooms available at a location
 	public int queryRooms(int id, String location)
 		throws RemoteException
 	{
 		return queryNum(id, Hotel.getKey(location));
 	}
 
 
 	
 	
 	// Returns room price at this location
 	public int queryRoomsPrice(int id, String location)
 		throws RemoteException
 	{
 		return queryPrice(id, Hotel.getKey(location));
 	}
 
 
 	// Returns the number of cars available at a location
 	public int queryCars(int id, String location)
 		throws RemoteException
 	{
 		return queryNum(id, Car.getKey(location));
 	}
 
 
 	// Returns price of cars at this location
 	public int queryCarsPrice(int id, String location)
 		throws RemoteException
 	{
 		return queryPrice(id, Car.getKey(location));
 	}
 
 	// Returns data structure containing customer reservation info. Returns null if the
 	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
 	//  reservations.
 	public RMHashtable getCustomerReservations(int id, int customerID)
 		throws RemoteException
 	{
 		Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
 		if( cust == null ) {
 			Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
 			return null;
 		} else {
 			return cust.getReservations();
 		} // if
 	}
 
 	// return a bill
 	public String queryCustomerInfo(int id, int customerID)
 		throws RemoteException
 	{
 		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
 		if( cust == null ) {
 			Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
 			return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
 		} else {
 				String s = cust.printBill();
 				Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
 				System.out.println( s );
 				return s;
 		} // if
 	}
 
   // customer functions
   // new customer just returns a unique customer identifier
 	
   public int newCustomer(int id)
 		throws RemoteException
 	{
 		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
 		// Generate a globally unique ID for the new customer
 		int cid = Integer.parseInt( String.valueOf(id) +
 								String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
 								String.valueOf( Math.round( Math.random() * 100 + 1 )));
 		Customer cust = new Customer( cid );
 		writeData( id, cust.getKey(), cust );
 		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
 		return cid;
 	}
 
 	// I opted to pass in customerID instead. This makes testing easier
   public boolean newCustomer(int id, int customerID )
 		throws RemoteException
 	{
 		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
 		if( cust == null ) {
 			cust = new Customer(customerID);
 			writeData( id, cust.getKey(), cust );
 			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
 			return true;
 		} else {
 			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
 			return false;
 		} // else
 	}
 
 
 	// Deletes customer from the database. 
 	public boolean deleteCustomer(int id, int customerID)
 			throws RemoteException
 	{
 		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
 		if( cust == null ) {
 			Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
 			return false;
 		} else {			
 			// Increase the reserved numbers of all reservable items which the customer reserved. 
 			RMHashtable reservationHT = cust.getReservations();
 			for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
 				String reservedkey = (String) (e.nextElement());
 				ReservedItem reserveditem = cust.getReservedItem(reservedkey);
 				Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
 				ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
 				Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
 				item.setReserved(item.getReserved()-reserveditem.getCount());
 				item.setCount(item.getCount()+reserveditem.getCount());
 			}
 			
 			// remove the customer from the storage
 			removeData(id, cust.getKey());
 			
 			Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
 			return true;
 		} // if
 	}
 
 
 
 
 	// Frees flight reservation record. Flight reservation records help us make sure we
 	//  don't delete a flight if one or more customers are holding reservations
 //	public boolean freeFlightReservation(int id, int flightNum)
 //		throws RemoteException
 //	{
 //		Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
 //		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
 //		if( numReservations != null ) {
 //			numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
 //		} // if
 //		writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
 //		Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
 //				+ numReservations + " reservations" );
 //		return true;
 //	}
 //	
 
 	
 	// Adds car reservation to this customer. 
 	public boolean reserveCar(int id, int customerID, String location)
 		throws RemoteException
 	{
 		return reserveItem(id, customerID, Car.getKey(location), location);
 	}
 
 
 	// Adds room reservation to this customer. 
 	public boolean reserveRoom(int id, int customerID, String location)
 		throws RemoteException
 	{
 		return reserveItem(id, customerID, Hotel.getKey(location), location);
 	}
 	// Adds flight reservation to this customer.  
 	public boolean reserveFlight(int id, int customerID, int flightNum)
 		throws RemoteException
 	{
 		return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
 	}
 	
 	/* reserve an itinerary */
     public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
 	throws RemoteException {
     	return false;
     }
 
 }
