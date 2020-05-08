 // -------------------------------
 // adapated from Kevin T. Manley
 // CSE 593
 //
 package ResImpl;
 
 import ResInterface.*;
 
 import java.util.*;
 import java.rmi.*;
 import java.io.*;
 import java.net.*;
 
 
 import java.rmi.registry.Registry;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 public class ResourceManagerImpl
 implements ResourceManager {
 	
     protected RMHashtable m_itemHT = new RMHashtable();
     private TransactionManager tm = new TransactionManager();
     private Shutdown s = new Shutdown();
 
     public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
         String middlewareserver = "willy";
         String resource = null;
 
         int middlewareport = -1;
         int port = -1;
 
         resource = args[0];
         port = Integer.parseInt(args[1]);
         String name = "Group13ResourceManager" + resource + new Random().nextInt();
 
         try
         {
             // create a new Server object
             ResourceManagerImpl obj = new ResourceManagerImpl();
             // dynamically generate the stub (client proxy)
             ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
 
             // Bind the remote object's stub in the registry
             Registry registry = LocateRegistry.getRegistry(port);
             registry.rebind(name, rm);
 
             System.err.println("Server ready");
         } 
         catch (Exception e) 
         {
             System.err.println("Server exception: " + e.toString());
             e.printStackTrace();
         }
 
         for(int i = 1; i < (args.length + 1) / 2; i++)
         {
             middlewareserver = args[i * 2];
             middlewareport = Integer.parseInt(args[i * 2 + 1]);
         
             try {
                 Socket clientSocket = new Socket(middlewareserver, middlewareport);
                 DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
 
                 outToServer.writeBytes(name +','+port+ '\n');
                 clientSocket.close();
             } catch (IOException e) {
                 System.err.println("disaster struck while sending a message from an rm:");
                 e.printStackTrace();
             }
         }
         Trace.info("ResourceManager reporting for duty.");
         /*
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
         }
         */
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
     public boolean deleteItem(int id, String key)
     {
         Trace.info("  deleteItem(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key );
         // Check if there is such an item in the storage
         if( curObj == null ) {
             Trace.warn("  deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
             return false;
         } else {
             if(curObj.getReserved()==0){
                 removeData(id, curObj.getKey());
                 Trace.info("  deleteItem(" + id + ", " + key + ") item deleted" );
                 return true;
             }
             else{
                 Trace.info("  deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                 return false;
             }
         } // if
     }
 
 
     // query the number of available seats/rooms/cars
     public int queryNum(int id, String key) {
         Trace.info("  queryNum(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key);
         int value = 0;  
         if( curObj != null ) {
             value = curObj.getCount();
         } // else
         Trace.info("  queryNum(" + id + ", " + key + ") returns count=" + value);
         return value;
     }	
 
     // query the price of an item
     public int queryPrice(int id, String key){
         Trace.info("  queryCarsPrice(" + id + ", " + key + ") called" );
         ReservableItem curObj = (ReservableItem) readData( id, key);
         int value = 0; 
         if( curObj != null ) {
             value = curObj.getPrice();
         } // else
         Trace.info("  queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
         return value;
     }
 
     public void decrementItem(int id, String key) {
         ReservableItem curObj = (ReservableItem) readData(id, key);
         Trace.info("Room reserved: " + key);
         tm.addStock(id, key, -1);
         curObj.setCount(curObj.getCount() - 1);
         curObj.setReserved(curObj.getReserved() + 1);
     }
 
     public void incrementItem(int id, String key, int change) {
         ReservableItem curObj = (ReservableItem) readData(id, key);
         Trace.info("Room reservation cancelled: " + key);
         tm.addStock(id, key, change);
         curObj.setCount(curObj.getCount() + change);
         curObj.setReserved(curObj.getReserved() - change);
     }   
 
     // reserve an item
     protected boolean reserveItem(int id, int customerID, String key, String location){
         Trace.info("  reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
         // Read customer object if it exists (and read lock it)
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
         if( cust == null ) {
             Trace.warn("  reserveItem( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         }
 
         // check if the item is available
         ReservableItem item = (ReservableItem)readData(id, key);
         if(item==null){
             Trace.warn("  reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         }else if(item.getCount()==0){
             Trace.warn("  reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
             return false;
         }else{
             cust.reserve( key, location, item.getPrice());
             writeData( id, cust.getKey(), cust );
 
             // decrease the number of available items in the storage
            decrementItem(id, key);
            //item.setCount(item.getCount() - 1);
            //item.setReserved(item.getReserved()+1);
 
             Trace.info("  reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }		
     }
 
     // Create a new flight, or add seats to existing flight
     //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
     public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
         throws RemoteException, TransactionAbortedException
         {
             String key = Flight.getKey(flightNum);
             Trace.info("  addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
             Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
             if( curObj == null ) {
                 // doesn't exist...add it
                 tm.addCreate(id, key);
 
                 Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
                 writeData( id, newObj.getKey(), newObj );
                 Trace.info("  addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                         flightSeats + ", price=$" + flightPrice );
             } else {
                 // add seats to existing flight and update the price...
                 tm.addUpdate(id, key, curObj.getCount(), curObj.getPrice());
                 curObj.setCount( curObj.getCount() + flightSeats );
                 if( flightPrice > 0 ) {
                     curObj.setPrice( flightPrice );
                 } // if
                 writeData( id, curObj.getKey(), curObj );
                 Trace.info("  addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
             } // else
             return(true);
         }
 
 
 
     public boolean deleteFlight(int id, int flightNum)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  deleteFlight(" + id + ", " + flightNum + ") called" );
             String key = Flight.getKey(flightNum);
             if(deleteItem(id, key))
             {
                 tm.addDelete(id, key,
                     queryFlight(id, flightNum),
                     queryFlightPrice(id, flightNum));
                 return true;
             }
             return false;
         }
 
 
 
     // Create a new room location or add rooms to an existing location
     //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
     public boolean addRooms(int id, String location, int count, int price)
         throws RemoteException, TransactionAbortedException
         {
             String key = Hotel.getKey(location);
             Trace.info("  addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
             Hotel curObj = (Hotel) readData( id, key );
             if( curObj == null ) {
                 // doesn't exist...add it
                 tm.addCreate(id, Hotel.getKey(location));
 
                 Hotel newObj = new Hotel( location, count, price );
                 writeData( id, newObj.getKey(), newObj );
                 Trace.info("  addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
             } else {
                 // add count to existing object and update price...
                 tm.addUpdate(id, key, curObj.getCount(), curObj.getPrice());
                 curObj.setCount( curObj.getCount() + count );
                 if( price > 0 ) {
                     curObj.setPrice( price );
                 } // if
                 writeData( id, curObj.getKey(), curObj );
                 Trace.info("  addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
             } // else
             return(true);
         }
 
     // Delete rooms from a location
     public boolean deleteRooms(int id, String location)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  deleteRooms(" + id + ", " + location + ") called" );
             String key = Hotel.getKey(location);
             if(deleteItem(id, key))
             {
                 tm.addDelete(id, key,
                     queryRooms(id, location),
                     queryRoomsPrice(id, location));
                 return true;
             }
             return false;
 
         }
 
     // Create a new car location or add cars to an existing location
     //  NOTE: if price <= 0 and the location already exists, it maintains its current price
     public boolean addCars(int id, String location, int count, int price)
         throws RemoteException, TransactionAbortedException
         {
             String key = Car.getKey(location);
             Trace.info("  addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
             Car curObj = (Car) readData( id, key );
             if( curObj == null ) {
                 // car location doesn't exist...add it
                 tm.addCreate(id, Car.getKey(location));
 
                 Car newObj = new Car( location, count, price );
                 writeData( id, newObj.getKey(), newObj );
                 Trace.info("  addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
             } else {
                 // add count to existing car location and update price...
                 // TODO: this could be problemk
                 tm.addUpdate(id, key, curObj.getCount(), curObj.getPrice());
                 curObj.setCount( curObj.getCount() + count );
                 if( price > 0 ) {
                     curObj.setPrice( price );
                 } // if
                 writeData( id, curObj.getKey(), curObj );
                 Trace.info("  addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
             } // else
             return(true);
         }
 
 
     // Delete cars from a location
     public boolean deleteCars(int id, String location)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  deleteCars(" + id + ", " + location + ") called" );
             String key = Car.getKey(location);
             if(deleteItem(id, key))
             {
                 tm.addDelete(id, key,
                     queryCars(id, location),
                     queryCarsPrice(id, location));
                 return true;
             }
             return false;
         }
 
 
 
     // Returns the number of empty seats on this flight
     public int queryFlight(int id, int flightNum)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  queryFlight(" + id + ", " + flightNum + ") called" );
             return queryNum(id, Flight.getKey(flightNum));
         }
 
     // Returns the number of reservations for this flight. 
     //	public int queryFlightReservations(int id, int flightNum)
     //		throws RemoteException
     //	{
     //		Trace.info("  queryFlightReservations(" + id + ", #" + flightNum + ") called" );
     //		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
     //		if( numReservations == null ) {
     //			numReservations = new RMInteger(0);
     //		} // if
     //		Trace.info("  queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
     //		return numReservations.getValue();
     //	}
 
 
     // Returns price of this flight
     public int queryFlightPrice(int id, int flightNum )
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  queryFlightPrice(" + id + ", " + flightNum + ") called" );
             return queryPrice(id, Flight.getKey(flightNum));
         }
 
 
     // Returns the number of rooms available at a location
     public int queryRooms(int id, String location)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  queryRooms(" + id + ", " + location + ") called" );
             return queryNum(id, Hotel.getKey(location));
         }
 
 
 
 
     // Returns room price at this location
     public int queryRoomsPrice(int id, String location)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  queryRoomsPrice(" + id + ", " + location + ") called" );
             return queryPrice(id, Hotel.getKey(location));
         }
 
 
     // Returns the number of cars available at a location
     public int queryCars(int id, String location)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  queryCars(" + id + ", " + location + ") called" );
             return queryNum(id, Car.getKey(location));
         }
 
 
     // Returns price of cars at this location
     public int queryCarsPrice(int id, String location)
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  queryCarsPrice(" + id + ", " + location + ") called" );
             return queryPrice(id, Car.getKey(location));
         }
 
     // Returns data structure containing customer reservation info. Returns null if the
     //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
     //  reservations.
     public RMHashtable getCustomerReservations(int id, int customerID)
         throws RemoteException
         {
             Trace.info("  getCustomerReservations(" + id + ", " + customerID + ") called" );
             Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
             if( cust == null ) {
                 Trace.warn("  getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                 return null;
             } else {
                 return cust.getReservations();
             }
         }
 
     // return a bill
     public String queryCustomerInfo(int id, int customerID)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("  queryCustomerInfo(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if( cust == null ) {
             Trace.warn("  queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
         } else {
             String s = "";
             String key = null;
             RMHashtable m_Reservations = cust.getReservations();
             for (Enumeration e = m_Reservations.keys(); e.hasMoreElements(); ) {
                 key = (String) e.nextElement();
                 s += queryNum(id, key) + " " + key + " $" + queryPrice(id, key) + "\n";
             }
             Trace.info("  queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
             System.out.println( s );
             return s;
         } // if
     }
 
     // I opted to pass in customerID instead. This makes testing easier
     public boolean newCustomer(int id, int customerID )
         throws RemoteException, TransactionAbortedException
         {
             Trace.info("  newCustomer(" + id + ", " + customerID + ") called" );
             Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
             if( cust == null ) {
                 tm.addCreate(id, Customer.getKey(customerID));
                 cust = new Customer(customerID);
                 writeData( id, cust.getKey(), cust );
                 Trace.info("  newCustomer(" + id + ", " + customerID + ") created a new customer" );
                 return true;
             } else {
                 Trace.info("  newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
                 return false;
             }
         }
 
 
     // Deletes customer from the database. 
     public boolean deleteCustomer(int id, int customerID)
         throws RemoteException, TransactionAbortedException
     {
         Trace.info("  deleteCustomer(" + id + ", " + customerID + ") called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if( cust == null ) {
             Trace.warn("  deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
             return false;
         } else {            
             // Increase the reserved numbers of all reservable items which the customer reserved. 
             RMHashtable reservationHT = cust.getReservations();
             for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){        
                 String reservedkey = (String) (e.nextElement());
                 tm.addUnbook(id, reservedkey, customerID, queryPrice(id, reservedkey));
                 ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                 Trace.info("  deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
 
                 incrementItem(id, reservedkey, reserveditem.getCount());
             }
 
             // remove the customer from the storage
             tm.addDelete(id, Customer.getKey(customerID), 0, 0);
             removeData(id, cust.getKey());
 
             Trace.info("  deleteCustomer(" + id + ", " + customerID + ") succeeded" );
             return true;
         } // if
     }
 
     // Adds car reservation to this customer. 
     public boolean reserveCar(int id, int customerID, String location)
         throws RemoteException, TransactionAbortedException
     {
         String key = Car.getKey(location);
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if( cust == null ) {
             Trace.warn("  reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         }
 
         // MAKE THE CAR SERVER DO THE THINGS
         int num = queryNum(id, key);
         if (num == 0) {
             Trace.warn("  reserveCar( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         } else {
             tm.addBook(id, key, customerID);
             cust.reserve( key, location, queryPrice(id, key));
             writeData( id, cust.getKey(), cust );
 
             if(!reserveItem(id, customerID, key, location))
                 return false;
 
             Trace.info("  reserveCar( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }
     }
 
 
     // Adds room reservation to this customer. 
     public boolean reserveRoom(int id, int customerID, String location)
         throws RemoteException, TransactionAbortedException
     {
         String key = Hotel.getKey(location);
         Trace.info("  reserveHotel( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if( cust == null ) {
             Trace.warn("  reserveHotel( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
             return false;
         }
 
         // MAKE THE HOTEL SERVER DO THE THINGS
         int num = queryNum(id, key);
         if (num == 0) {
             Trace.warn("  reserveHotel( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
             return false;
         } else {
             tm.addBook(id, key, customerID);
             cust.reserve( key, location, queryPrice(id, key));
             writeData( id, cust.getKey(), cust );
 
             if(!reserveItem(id, customerID, key, location))
                 return false;
 
             Trace.info("  reserveHotel( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
             return true;
         }
     }
 
     // Adds flight reservation to this customer.  
     public boolean reserveFlight(int id, int customerID, int flightNum)
         throws RemoteException, TransactionAbortedException
     {
         String key = Flight.getKey(flightNum);
         Trace.info("  reservePlane( " + id + ", customer=" + customerID + ", " +key+ ", "+flightNum+" ) called" );
         Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
         if( cust == null ) {
             Trace.warn("  reservePlane( " + id + ", " + customerID + ", " + key + ", "+flightNum+")  failed--customer doesn't exist" );
             return false;
         }
 
         // MAKE THE PLANE SERVER DO THE THINGS
         int num = queryNum(id, key);
         if (num == 0) {
             Trace.warn("  reservePlane( " + id + ", " + customerID + ", " + key+", " +flightNum+") failed--item doesn't exist" );
             return false;
         } else {
             tm.addBook(id, key, customerID);
 
             cust.reserve( key, String.valueOf(flightNum), queryPrice(id, key));
             writeData( id, cust.getKey(), cust );
 
             if(!reserveItem(id, customerID, key, ""+flightNum))
                 return false;
 
             Trace.info("  reservePlane( " + id + ", " + customerID + ", " + key + ", " +flightNum+") succeeded" );
             return true;
         }
     }
 
     public int start() throws RemoteException
     {
         return 0;
     }
 
     public void commit(int trxnId) throws RemoteException, TransactionAbortedException
     {
         Trace.info("Committing trxn " + trxnId);
         tm.commit(trxnId);
     }
 
     public void abort(int trxnId) throws RemoteException
     {   // TM's abort
         Trace.info("Aborting trxn " + trxnId);
         String location = null;
         ReservableItem curObj = null;
         for(Transaction t : tm.getTrxns(trxnId))
         {
             RMItem newObj;
             switch(t.action)
             {   // undo em
                 case BOOK:    // Done
                     Trace.info("- Aborting booking of " + t.id + ", " + t.key);
                     newObj = (Customer) readData( t.id, Customer.getKey(t.custId()) );
                     ((Customer)newObj).unserve(t.key);
                    break;
                 case UNBOOK:  // DONE
                     Trace.info("- Aboring unbooking of " + t.id + ", " + t.key);
                     newObj = (Customer) readData( t.id, Customer.getKey(t.custId()) );
                     ((Customer)newObj).reserve(t.key, t.key.substring(t.key.indexOf("-") + 1), t.price);
                     break;
                 case CREATE:    // DONE
                     Trace.info("- Aborting creation of " + t.id + ", " + t.key);
                     deleteItem(t.id, t.key);
                     break;
                 case DELETE:    // recreate the item
                     Trace.info("- Aborting deletion of " + t.id + ", " + t.key);
                     location = t.key.substring(t.key.indexOf("-") + 1);
                     if(t.key.startsWith("car-"))
                     {
                         newObj = new Car( location, t.numDeleted(), t.price );
                         writeData( t.id, newObj.getKey(), newObj );
                     }
                     else if(t.key.startsWith("flight-"))
                     {
                         newObj = new Flight( Integer.parseInt(location), t.numDeleted(), t.price );
                         writeData( t.id, newObj.getKey(), newObj );
                     }
                     else if(t.key.startsWith("room-"))
                     {
                         newObj = new Hotel( location, t.numDeleted(), t.price );
                         writeData( t.id, newObj.getKey(), newObj );
                     }
                     else if(t.key.startsWith("customer-"))
                     {
                         newObj = new Customer(t.id);
                         writeData( t.id, newObj.getKey(), newObj);
                     }
                     break;
                 case STOCK:    // DONE
                     Trace.info("- Aborting change in stock of " + t.id + ", " + t.key);
                     curObj = (ReservableItem) readData(t.id, t.key);
                     curObj.setCount(curObj.getCount() - t.amount());
                     curObj.setReserved(curObj.getReserved() - t.amount());
                     break;
                 case UPDATE:
                     Trace.info("- Aborting update of " + t.id + ", " + t.key);
                     curObj = (ReservableItem) readData(t.id, t.key);
                     curObj.setCount(t.amount());
                     curObj.setReserved(t.price);
                     break;
             }
         }
         tm.abort(trxnId);
     }
 
     public void enlist(int trxnId) throws RemoteException
     {
         Trace.info("Enlisting to trxn " + trxnId);
         tm.start(trxnId);
     }
 
     public boolean shutdown() throws RemoteException
     {
         Trace.info("Shutting down the ResourceManager");
         s.start();
         return true;
     }
 }
