 package ResImpl;
 
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Vector;
 
 import ResInterface.ResourceManager;
 
 public class MiddlewareImpl implements ResourceManager {
 	
     //this references the RM server for cars
     static ResourceManager cars_rm = null;
     
     //this references the RM server for flights
     static ResourceManager flights_rm = null;
     
     //this references the RM server for rooms
     static ResourceManager rooms_rm = null;
 
 
 	protected RMHashtable m_itemHT = new RMHashtable();
 	
 	/**
 	 * Middleware takes care of all reservations - this is due to the inseparability of the 
 	 * information in the Customer class.
 	 * @param args
 	 */
 
 	public static void main(String[] args)
 	{
 		// Figure out where server is running
         String server = "teaching";
        // int port = 1099;
         /**
          * Creating our own RMI registry, global one isn't working
          */
         int port = 8807;
         
         /**
          * RM SERVERS
          */
         String cars_server = "lab2-10.cs.mcgill.ca";        
         String flights_server = "lab2-11.cs.mcgill.ca";
         String rooms_server = "lab2-12.cs.mcgill.ca";
         
         int rm_port = 7707;
 
 
         //if one args, this should be a port #
         if (args.length == 1) {
             server = server + ":" + args[0];
             try {
             	rm_port = Integer.parseInt(args[0]);
             }
             //if the above fails, it probably wasn't a number
             catch(NumberFormatException e)
             {
             	System.err.println("\nWrong usage:");
             	System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
             	System.exit(1);
             }
         }
         //if 3 args, assume that the three arguments are the RM servers
         else if (args.length == 3) {
         	cars_server = args[0];
         	flights_server = args[1];
         	rooms_server = args[2];
         }
         //if 4 args, assume that the first argument is a port number and the other 3
         //are RM servers
         else if (args.length == 4)
         {
         	server = server + ":" + args[0];
         	try {
         		rm_port = Integer.parseInt(args[0]);
         	}
         	catch(NumberFormatException e)
         	{
         		System.err.println("\nWrong usage:");
         		System.out.println("Usage: java ResImpl.ResourceManagerImpl [port] [cars_rm_server] [flights_rm_server] [rooms_rm_server]");
         		System.exit(1);
         	}
         	cars_server = args[1];
         	flights_server = args[2];
         	rooms_server = args[3];
         }
         //unless there were no args (which is okay, this will then use default values)
         else if (args.length != 0) {
             System.err.println ("\nWrong usage:");
             System.out.println("Use case 1: java ResImpl.ResourceManagerImpl");
             System.out.println("Use case 2: java ResImpl.ResourceManagerImpl [port]");
             System.out.println("Use case 3: java ResImpl.ResourceManagerImpl [port] [cars_rm_server] [flights_rm_server] [rooms_rm_server]");
             System.exit(1);
         }
         
 
         try {
         	/**
         	 * CONNECT TO RMIREGISTRY AS SERVER TO BE CONNECTED TO FROM CLIENT
         	 */
             // create a new Server object
             MiddlewareImpl obj = new MiddlewareImpl();
             // dynamically generate the stub (client proxy)
             ResourceManager mw = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
 
             // Bind the remote object's stub in the registry
             //Registry registryMiddle = LocateRegistry.getRegistry(port);
             Registry registryMiddle = LocateRegistry.createRegistry(port);
             registryMiddle.rebind("group_7_middle", mw);
 
             System.err.println("Server ready");
             
             /**
              * CONNECT TO RM SERVERS AS A CLIENT
              * 
              * note: registry might overwrite something - may need to have two registry objects?
              */
             
             // get the proxy and the remote reference by rmiregistry lookup for the cars server
             Registry registry_cars = LocateRegistry.getRegistry(cars_server, rm_port);
             cars_rm = (ResourceManager) registry_cars.lookup("group_7_RM");
             
             // get the proxy and the remote reference by rmiregistry lookup for the flights server
             Registry registry_flights = LocateRegistry.getRegistry(flights_server, rm_port);
             flights_rm = (ResourceManager) registry_flights.lookup("group_7_RM");
             
             // get the proxy and the remote reference by rmiregistry lookup for the rooms server
             Registry registry_rooms = LocateRegistry.getRegistry(rooms_server, rm_port);
             rooms_rm = (ResourceManager) registry_rooms.lookup("group_7_RM");
             
             if(cars_rm!=null && flights_rm!=null && rooms_rm!=null)
             {
                 System.out.println("Successful");
                 System.out.println("Connected to RMs");
             }
             else
             {
                 System.out.println("Unsuccessful");
             }
             // make call on remote method
             
         } catch (Exception e) {
             System.err.println("Server exception: " + e.toString());
             e.printStackTrace();
         }
 
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
         }
 	}
 	
 	public MiddlewareImpl() throws RemoteException {
     }
 	
     // Reads a data item
     private RMItem readData( int id, String key )
     {
         synchronized(m_itemHT) {
             return (RMItem) m_itemHT.get(key);
         }
     }
 
     // Writes a data item
     @SuppressWarnings("unchecked")
     private void writeData( int id, String key, RMItem value )
     {
         synchronized(m_itemHT) {
             m_itemHT.put(key, value);
         }
     }
     
     // Remove the item out of storage
     protected RMItem removeData(int id, String key) {
         synchronized(m_itemHT) {
             return (RMItem)m_itemHT.remove(key);
         }
     }
     
     // reserve an item
     protected boolean reserveItem(int id, int customerID, String key, String location) {
     	try 
     	{
     		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
 	        // Read customer object if it exists (and read lock it)
 	        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
 	        if ( cust == null ) {
 	            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
 	            return false;
 	        } 
 	        
 	        //parse key to find out if item is a car, flight, or room
 	        String delims = "[-]";
 	        String[] tokens = key.split(delims);
 	        
 	        ReservableItem item = null;
 	        // check if the item is available
 	        //if it's a flight
 	        if (tokens[0].equals("flight"))
 	        {
 	        	item = flights_rm.getReservableItem(id, key);
 	        }
 	        //else if the item is a car
 	        else if (tokens[0].equals("car"))
 	        {
 	        	item = cars_rm.getReservableItem(id, key);
 	        }
 	        //otherwise it's a room
 	        else
 	        {
 	        	item = rooms_rm.getReservableItem(id, key);
 	        }
 	        
 	        if ( item == null ) {
 	            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
 	            return false;
 	        } else if (item.getCount()==0) {
 	            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
 	            return false;
 	        } else {            
 	            cust.reserve( key, location, item.getPrice());        
 	            writeData( id, cust.getKey(), cust );
 	            
 	            // decrease the number of available items in the storage
 	            boolean resource_updated = false;
 	            
 	            if (tokens[0].equals("flight"))
 		        {
 	            	resource_updated = flights_rm.itemReserved(id, item);
 		        }
 		        //else if the item is a car
 		        else if (tokens[0].equals("car"))
 		        {
 	            	resource_updated = cars_rm.itemReserved(id, item);
 		        }
 		        //otherwise it's a room
 		        else
 		        {
 	            	resource_updated = rooms_rm.itemReserved(id, item);
 		        }
 
 	            if (resource_updated)
 	            {
 		            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
 		            return true;
 	            }
 	            else 
 	            {
 	            	return false;
 	            }
 	        } 
     	} catch (RemoteException e) {
     		e.printStackTrace();
     		return false;
     	}
     }
     
     @SuppressWarnings("unused")
 	private void checkItem(ReservableItem item)
     {
     	
     }
 	
 	@Override
 	public boolean addFlight(int id, int flightNum, int flightSeats,
 			int flightPrice) throws RemoteException {
 			
 		return flights_rm.addFlight(id, flightNum, flightSeats, flightPrice);
 	}
 
 	@Override
 	public boolean addCars(int id, String location, int numCars, int price)
 			throws RemoteException {
 		
 		return cars_rm.addCars(id, location, numCars, price);
 	}
 
 	@Override
 	public boolean addRooms(int id, String location, int numRooms, int price)
 			throws RemoteException {
 
 		return rooms_rm.addRooms(id, location, numRooms, price);
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
         if ( cust == null ) {
             cust = new Customer(customerID);
             writeData( id, cust.getKey(), cust );
             Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
             return true;
         } else {
             Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
             return false;
         } // else
     }
 
 	@Override
 	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
 
 		return flights_rm.deleteFlight(id, flightNum);
 	}
 
 	@Override
 	public boolean deleteCars(int id, String location) throws RemoteException {
 
 		return cars_rm.deleteCars(id, location);
 	}
 
 	@Override
 	public boolean deleteRooms(int id, String location) throws RemoteException {
 
 		return rooms_rm.deleteRooms(id, location);
 	}
 
 	 // Deletes customer from the database. 
     @SuppressWarnings("rawtypes")
 	public boolean deleteCustomer(int id, int customerID)
         throws RemoteException
     {
         Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if ( cust == null ) {
             Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             return false;
         } else {            
             // Increase the reserved numbers of all reservable items which the customer reserved. 
             RMHashtable reservationHT = cust.getReservations();
             for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
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
 
 
 	@Override
 	public int queryFlight(int id, int flightNumber) throws RemoteException {
 
 		return flights_rm.queryFlight(id, flightNumber);
 	}
 
 	@Override
 	public int queryCars(int id, String location) throws RemoteException {
 
 		return cars_rm.queryCars(id, location);
 	}
 
 	@Override
 	public int queryRooms(int id, String location) throws RemoteException {
 
 		return rooms_rm.queryRooms(id, location);
 	}
 
 	   // return a bill
     public String queryCustomerInfo(int id, int customerID)
         throws RemoteException
     {
         Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if ( cust == null ) {
             Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
         } else {
                 String s = cust.printBill();
                 Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                 System.out.println( s );
                 return s;
         } // if
     }
 
 
 	@Override
 	public int queryFlightPrice(int id, int flightNumber)
 			throws RemoteException {
 
 		return flights_rm.queryFlightPrice(id, flightNumber);
 	}
 
 	@Override
 	public int queryCarsPrice(int id, String location) throws RemoteException {
 
 		return cars_rm.queryCarsPrice(id, location);
 	}
 
 	@Override
 	public int queryRoomsPrice(int id, String location) throws RemoteException {
 
 		return rooms_rm.queryRoomsPrice(id, location);
 	}
 
 	@Override
 	public boolean reserveFlight(int id, int customer, int flightNumber)
 			throws RemoteException {
 			
         return reserveItem(id, customer, Flight.getKey(flightNumber), String.valueOf(flightNumber));
 	}
 
 	@Override
 	public boolean reserveCar(int id, int customer, String location)
 			throws RemoteException {
 
         return reserveItem(id, customer, Car.getKey(location), location);
 	}
 
 	@Override
 	public boolean reserveRoom(int id, int customer, String location)
 			throws RemoteException {
 
         return reserveItem(id, customer, Hotel.getKey(location), location);
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public boolean itinerary(int id, int customer, Vector flightNumbers,
 			String location, boolean car, boolean room) throws RemoteException {
 
 		boolean confirmation = true;
 		
     	//for each flight in flightNumbers, reserve flight
 		Iterator i = flightNumbers.iterator();
 		while (i.hasNext())
 		{
 			Object flight_number_object = i.next();
 			int flightNumberInt = Integer.parseInt(flight_number_object.toString());
 			confirmation = reserveItem(id, customer, Flight.getKey(flightNumberInt), String.valueOf(flightNumberInt));
 		}
 		
 		//if there was a car to be reserved as well
 		if (car)
 		{
 			confirmation = reserveItem(id, customer, Car.getKey(location), location);
 		}
 		
 		//if there was a room to be reserved as well
 		if (room)
 		{
 			confirmation = reserveItem(id, customer, Hotel.getKey(location), location);
 		}
 		
 		return confirmation;
 	}
 	
     // Returns data structure containing customer reservation info. Returns null if the
     //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
     //  reservations.
     public RMHashtable getCustomerReservations(int id, int customerID)
         throws RemoteException
     {
         Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if ( cust == null ) {
             Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             return null;
         } else {
             return cust.getReservations();
         } // if
     }
 
 	@Override
 	public ReservableItem getReservableItem(int id, String key)
 			throws RemoteException {
 		return null;
 	}
 
 	@Override
 	public boolean itemReserved(int id, ReservableItem item) throws RemoteException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 }
