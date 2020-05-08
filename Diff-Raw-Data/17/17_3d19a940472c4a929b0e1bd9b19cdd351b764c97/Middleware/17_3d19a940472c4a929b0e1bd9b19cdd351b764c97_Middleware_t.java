 package serversrc.resImpl;
 
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import serversrc.resInterface.*;
 
 public class Middleware implements ResourceManager {
 	
 	RMCar rmCar;
 	RMFlight rmFlight;
 	RMHotel rmHotel;
 	RMCustomer rmCustomer;
 	
 	public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
         int port = 1099;
         Registry registry;
         if (args.length == 5) {
             server = server + ":" + args[4];
             port = Integer.parseInt(args[4]); 
         } else {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.Middleware rmCar rmFlight rmHotel rmCustomer [port]");
             System.exit(1);
         }
 
         try {
             // create a new Server object
             // dynamically generate the stub (client proxy)
         	Middleware obj = new Middleware();
             ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
             // get a reference to the rmiregistry
             registry = LocateRegistry.getRegistry(args[0], port);
             // get the proxy and the remote reference by rmiregistry lookup
             obj.rmCar = (RMCar) registry.lookup("Group2RMCar");
             registry = LocateRegistry.getRegistry(args[1], port);
             obj.rmFlight = (RMFlight) registry.lookup("Group2RMFlight");
             registry = LocateRegistry.getRegistry(args[2], port);
             obj.rmHotel = (RMHotel) registry.lookup("Group2RMHotel");
             registry = LocateRegistry.getRegistry(args[3], port);
             obj.rmCustomer = (RMCustomer) registry.lookup("Group2RMCustomer");
             if(obj.rmCar!=null && obj.rmFlight != null && obj.rmHotel!=null)
             {
             	System.out.println("Successful");
             	System.out.println("Connected to RMs");
             }
             else
             {
             	System.out.println("Unsuccessful");
             }
 
             // Bind the remote object's stub in the registry
 	    registry = LocateRegistry.getRegistry(port);
             registry = LocateRegistry.getRegistry(port);
             registry.rebind("Group2Middleware", rm);
 
             System.err.println("Server ready");
         } catch (Exception e) {
             System.err.println("Server exception: " + e.toString());
             e.printStackTrace();
         }
 
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
         }
     }
 
 	@Override
 	public boolean addFlight(int id, int flightNum, int flightSeats,
 			int flightPrice) throws RemoteException {
 
 		return rmFlight.addFlight(id, flightNum, flightSeats, flightPrice);
 	}
 
 	@Override
 	public boolean addCars(int id, String location, int numCars, int price)
 			throws RemoteException {
 
 		return rmCar.addCars(id, location, numCars, price);
 	}
 
 	@Override
 	public boolean addRooms(int id, String location, int numRooms, int price)
 			throws RemoteException {
 
 		return rmHotel.addRooms(id, location, numRooms, price);
 	}
 
 	@Override
 	public int newCustomer(int id) throws RemoteException {
 		
 		return rmCustomer.newCustomer(id);
 	}
 
 	@Override
 	public boolean newCustomer(int id, int cid) throws RemoteException {
 		
 		return rmCustomer.newCustomer(id, cid);
 	}
 
 	@Override
 	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
 		
 		return rmFlight.deleteFlight(id, flightNum);
 	}
 
 	@Override
 	public boolean deleteCars(int id, String location) throws RemoteException {
 		
 		return rmCar.deleteCars(id, location);
 	}
 
 	@Override
 	public boolean deleteRooms(int id, String location) throws RemoteException {
 		
 		return rmHotel.deleteRooms(id, location);
 	}
 
 	@Override
 	public boolean deleteCustomer(int id, int customer) throws RemoteException {
 		
 		RMHashtable reservationHT = rmCustomer.deleteCustomer(id, customer);
 		 for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
              String reservedkey = (String) (e.nextElement());
              ReservedItem reserveditem = (ReservedItem) reservationHT.get( reservedkey );
              Trace.info("RM::deleteCustomer(" + id + ", " + customer + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
              
              if (reserveditem.getrType() == ReservedItem.rType.FLIGHT)
             	 rmFlight.unreserveItem(id, reserveditem);
              else if (reserveditem.getrType() == ReservedItem.rType.CAR)
             	 rmCar.unreserveItem(id, reserveditem);
              else if (reserveditem.getrType() == ReservedItem.rType.ROOM)
             	 rmHotel.unreserveItem(id, reserveditem);
              
          }
 		
 		return true;
 	}
 
 	@Override
 	public int queryFlight(int id, int flightNumber) throws RemoteException {
 
 		return rmFlight.queryFlight(id, flightNumber);
 	}
 
 	@Override
 	public int queryCars(int id, String location) throws RemoteException {
 
 		return rmCar.queryCars(id, location);
 	}
 
 	@Override
 	public int queryRooms(int id, String location) throws RemoteException {
 
 		return rmHotel.queryRooms(id, location);
 	}
 
 	@Override
 	public String queryCustomerInfo(int id, int customer)
 			throws RemoteException {
 		return rmCustomer.queryCustomerInfo(id, customer);
 	}
 
 	@Override
 	public int queryFlightPrice(int id, int flightNumber)
 			throws RemoteException {
 		
 		return rmFlight.queryFlightPrice(id, flightNumber);
 	}
 
 	@Override
 	public int queryCarsPrice(int id, String location) throws RemoteException {
 
 		return rmCar.queryCarsPrice(id, location);
 	}
 
 	@Override
 	public int queryRoomsPrice(int id, String location) throws RemoteException {
 
 		return rmHotel.queryRoomsPrice(id, location);
 	}
 
 	@Override
 	public boolean reserveFlight(int id, int customer, int flightNum)
 			throws RemoteException {
 		ReservedItem item = reserveItem(id, customer, Flight.getKey(flightNum), String.valueOf(flightNum), ReservedItem.rType.FLIGHT);
 		return (item != null);
 	}
 
 	@Override
 	public boolean reserveCar(int id, int customer, String location)
 			throws RemoteException {
 		ReservedItem item = reserveItem(id, customer, Car.getKey(location), location, ReservedItem.rType.CAR);
 		return (item != null);
 	}
 
 	@Override
 	public boolean reserveRoom(int id, int customer, String location)
 			throws RemoteException {
 		ReservedItem item = reserveItem(id, customer, Hotel.getKey(location), location, ReservedItem.rType.ROOM);
 		return (item != null);
 	}
 				
 	@Override
 	public boolean itinerary(int id, int customer, Vector flightNumbers,
 			String location, boolean car, boolean room) throws RemoteException {
 		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
 					", " + car + ", " + room + " ) called" );        
         // Read customer object if it exists (and read lock it)
         Customer cust = rmCustomer.getCustomer(id, customer);
         if ( cust == null ) {
         	Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
 					", " + car + ", " + room + " ) -- Customer non existent, adding it." );
         	return false;
         }
         ReservedItem reservedCar = null;
         ReservedItem reservedRoom = null;
         if (car){
         	reservedCar = reserveItem(id, customer, Car.getKey(location) ,location, ReservedItem.rType.CAR);
         	if (reservedCar == null) {
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
     					", " + car + ", " + room + " ) -- Car could not have been reserved." );
         		return false;
         	}
         }
         if (room){
         	reservedRoom = reserveItem(id, customer, Hotel.getKey(location) ,location, ReservedItem.rType.ROOM);
         	if (reservedRoom == null) {
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
     					", " + car + ", " + room + " ) -- Room could not have been reserved." );
        		if(reservedCar != null)
        			unreserveItem(id, customer, reservedCar, ReservedItem.rType.CAR);
         		return false;
         	}
         }
         Vector flightsDone = new Vector();
         for (Enumeration e = flightNumbers.elements(); e.hasMoreElements();) {
         	int flightnum = 0;
         	try {
         		flightnum = getInt(e.nextElement());
         	} catch(Exception ex) {
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
     					", " + car + ", " + room + " ) -- Expected FlightNumber was not a valid integer. Exception "
     					+ ex + " cached");
        		if(reservedCar != null)
        			unreserveItem(id, customer, reservedCar, ReservedItem.rType.CAR);
        		if(reservedRoom != null)
        			unreserveItem(id, customer, reservedRoom, ReservedItem.rType.ROOM);
         		for (Enumeration f = flightsDone.elements(); f.hasMoreElements();) {
         			unreserveItem(id, customer, (ReservedItem) f.nextElement(), ReservedItem.rType.ROOM);
         		}
         		return false;
         	}
         	ReservedItem reservedFlight = reserveItem(id, customer, Flight.getKey(flightnum), String.valueOf(flightnum), ReservedItem.rType.FLIGHT);
         	if (reservedFlight == null){
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightnum+ ", "+location+
     					", " + car + ", " + room + " ) -- flight could not have been reserved." );
        		if(reservedCar != null)
        			unreserveItem(id, customer, reservedCar, ReservedItem.rType.CAR);
        		if(reservedRoom != null)
        			unreserveItem(id, customer, reservedRoom, ReservedItem.rType.ROOM);
         		for (Enumeration f = flightsDone.elements(); f.hasMoreElements();) {
         			unreserveItem(id, customer, (ReservedItem) f.nextElement(), ReservedItem.rType.ROOM);
         		}
         		return false;
         	}
         	flightsDone.add(reservedFlight);	
         }
         	
 		return true;
 	}
 	
 	/*Since the client sends a Vector of objects, we need this 
 	 * unsafe function that retrieves the int from the vector.
 	 * 
 	 */ 
 	public int getInt(Object temp) throws Exception {
 	    try {
 	        return (new Integer((String)temp)).intValue();
 	        }
 	    catch(Exception e) {
 	        throw e;
 	        }
 	    }
 	
 	/*
 	 * Call RMCust to obtain customer, if it exists.
 	 * Verify if item exists and is available. (Call RM*obj*)
 	 * Reserve with RMCustomer
 	 * Tell RM*obj* to reduce the number of available
 
 	 */
 	protected ReservedItem reserveItem(int id, int customerID, String key, String location, ReservedItem.rType rtype)
 			throws RemoteException {
 		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
 		// Verifies if customer exists
 		Customer cust = rmCustomer.getCustomer(id, customerID);
 		if ( cust == null ) {
 			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
 			return null;
 		} 
 
 		RMInteger price = null;
 		// check if the item is available
 		if (rtype == ReservedItem.rType.CAR)
 			price = rmCar.reserveItem(id, customerID, key, location);
 		else if (rtype == ReservedItem.rType.FLIGHT)
 			price = rmFlight.reserveItem(id, customerID, key, location);
 		else if (rtype == ReservedItem.rType.ROOM)
 			price = rmHotel.reserveItem(id, customerID, key, location);
 
 		if ( price == null ) {
 			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed-- Object RM returned false." );
 			return null;
 		} else {  
 			// We do the following check in case the customer has been
 			// deleted between the first verification and now.
 			ReservedItem item = rmCustomer.reserve(id, customerID, key, location, price.getValue(), rtype);
 			if(item != null){
 				Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
 				return item;
 			} else {
 				Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") failed -- Customer has been deleted!" );
 				if (rtype == ReservedItem.rType.CAR)
 					rmCar.unreserveItem(id, key);
 				else if (rtype == ReservedItem.rType.FLIGHT)
 					rmFlight.unreserveItem(id, key);
 				else if (rtype == ReservedItem.rType.ROOM)
 					rmHotel.unreserveItem(id, key);
 			}
 			return null;
 
 		}        
 	}
 
 	/*
 	 * unreserveItem is used by the itinerary class to cancel a reserved item when the whole reservation failed.
 	 */
 	protected boolean unreserveItem(int id, int customerID, ReservedItem item, ReservedItem.rType rtype)
 			throws RemoteException {
 		Trace.info("RM::unreserveItem( " + id + ", customer=" + customerID + ", " +item+ " ) called" );        
 		// Verifies if customer exists
 		if(!rmCustomer.unreserve(id, customerID, item)){
 			Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " +item +" ) failed -- Customer has been deleted." );
 			return false;
 		}
 		
 		boolean done = false;
 		// check if the item is available
 		if (rtype == ReservedItem.rType.CAR)
 			done = rmCar.unreserveItem(id, item);
 		else if (rtype == ReservedItem.rType.FLIGHT)
 			done = rmFlight.unreserveItem(id, item);
 		else if (rtype == ReservedItem.rType.ROOM)
 			done = rmHotel.unreserveItem(id, item);
 
 		if (!done) {
 			Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " +item +" ) failed-- Object RM returned false." );
 			return false;
 		}
 		return true;
 	}
 	
 }
