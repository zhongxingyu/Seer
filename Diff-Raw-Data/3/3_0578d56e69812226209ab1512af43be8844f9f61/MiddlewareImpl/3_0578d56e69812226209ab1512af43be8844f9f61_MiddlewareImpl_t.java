 package ResImpl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.transaction.InvalidTransactionException;
 
 import ResInterface.ResourceManager;
 import TransactionManager.OP_CODE;
 import TransactionManager.TransactionAbortedException;
 import TransactionManager.TransactionManager;
 import TransactionManager.Vote;
 
 public class MiddlewareImpl implements ResourceManager {
 	
     //this references the RM server for cars
     private static ResourceManager cars_rm = null;
     
     //this references the RM server for flights
     private static ResourceManager flights_rm = null;
     
     //this references the RM server for rooms
     private static ResourceManager rooms_rm = null;
 
     //this references the transaction manager object
     private static TransactionManager tm = null;
 
 	protected static RMHashtable m_itemHT = new RMHashtable();
 	private static RMHashtable non_committed_items = new RMHashtable();
 	private static RMHashtable abort_items = new RMHashtable();
 	
 	private static int port;
 	private static String registry_name;
	@SuppressWarnings("unused")
 	private static CrashType crashType;
	@SuppressWarnings("unused")
 	private static String serverToCrash;
 
 	/**
 	 * Middleware takes care of all reservations - this is due to the inseparability of the 
 	 * information in the Customer class.
 	 * @param args
 	 */
 
 	public static void main(String[] args)
 	{
 		crashType = null;
 		serverToCrash = null;
 		
 		// Figure out where server is running
         String server = "teaching";
        // int port = 1099;
         /**
          * Creating our own RMI registry, global one isn't working
          */
         port = 8807;
         registry_name = "group_7_middle";
         
         /**
          * RM SERVERS
          */
         String flights_server = "lab2-1.cs.mcgill.ca";
         String cars_server = "lab2-5.cs.mcgill.ca";        
         String rooms_server = "lab2-7.cs.mcgill.ca";
         
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
         	flights_server = args[0];
         	cars_server = args[1];
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
         	flights_server = args[1];
         	cars_server = args[2];
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
             registryMiddle.rebind(registry_name, mw);
 
             System.err.println("Server ready");
             
             /**
              * CONNECT TO RM SERVERS AS A CLIENT
              * 
              * note: registry might overwrite something - may need to have two registry objects?
              */
             
             // get the proxy and the remote reference by rmiregistry lookup for the cars server
             Registry registry_cars = LocateRegistry.getRegistry(cars_server, rm_port);
             cars_rm = (ResourceManager) registry_cars.lookup("group_7_RM");
             //tell the server which one it is
             
             // get the proxy and the remote reference by rmiregistry lookup for the flights server
             Registry registry_flights = LocateRegistry.getRegistry(flights_server, rm_port);
             flights_rm = (ResourceManager) registry_flights.lookup("group_7_RM");
             
             // get the proxy and the remote reference by rmiregistry lookup for the rooms server
             Registry registry_rooms = LocateRegistry.getRegistry(rooms_server, rm_port);
             rooms_rm = (ResourceManager) registry_rooms.lookup("group_7_RM");
                    
             //create the transactionmanager object
             tm = new TransactionManager(flights_rm, cars_rm, rooms_rm, obj);
             
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
             
             /**
         	 * LOAD DATA INTO MAIN  MEMORY
         	 */
         	//read in any existing data
             System.out.println("Reading in existing data...");
             String masterPath = "/home/2011/nwebst1/comp512/data/middleware/master_record.loc";
             File f = new File(masterPath);
             //if Master Record doesn't exist we ignore all other file reads
             if (f.exists())
             {
             	System.out.println("reading from " + masterPath);
             	
             	//get path to master record
             	FileInputStream fis = new FileInputStream(masterPath);
             	ObjectInputStream ois = new ObjectInputStream(fis);
             	String masterRecordPath = (String) ois.readObject();
             	fis.close();
             	ois.close();
             	
             	//get paths to data items for this RM
             	String filePathItems = masterRecordPath + "items_table.data";
             	
             	//create file objects for these data files
             	File items_file = new File(filePathItems);
     	      	
     	      	//load items data into memory
     	    	if(items_file.exists()){
     	        	fis = new FileInputStream(items_file);
     	        	ois = new ObjectInputStream(fis);
 
     	        	m_itemHT = (RMHashtable) ois.readObject();
     	        	fis.close();
     	        	ois.close();
     	        }
             }
 	    	//load TM data into memory
 	    	tm.readFromDisk();
             
         } catch (Exception e) {
             System.err.println("Server exception: " + e.toString());
             e.printStackTrace();
         }
         
         System.out.println("Middleware Server Ready");
 
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
         }
 	}
 	
 	public MiddlewareImpl() throws RemoteException {
 		
     }
 	
 	/**
 	 * start a new transaction for the caller
 	 */
    public int start() throws RemoteException
     {
 	   int return_value = tm.start();
 	   //flushToDisk();
 	   return return_value;
     }
     
     /**
      * Commit transaction with id transaction_id
      * @throws TransactionAbortedException 
      */
     public boolean commit(int transaction_id) throws RemoteException, InvalidTransactionException, TransactionAbortedException
     {
     	return tm.prepare(transaction_id);
     }
 
     /**
      * Commit operation with ID
      */
     public boolean commitOperation(int op_id) throws RemoteException, InvalidTransactionException
     {
     	Customer item =(Customer)non_committed_items.get("" + op_id);
     	if (item != null)
     	{
         	writeData(op_id, item.getKey(), item);
         	non_committed_items.remove("" + op_id);
     	}
     	
     	return true;
     }
     
     /**
      * Abort transaction with id transaction_id
      * @throws TransactionAbortedException 
      */
     public void abort(int transaction_id) throws RemoteException, InvalidTransactionException, TransactionAbortedException
     {
     	tm.abort(transaction_id);
     }
     
     /**
      * Abort operation with ID. 
      */
     public void abortOperation(int op_id) throws RemoteException, InvalidTransactionException
     {
   	
     	//put back any old data (used for cases where the state of an object is changed
     	//instead of having been simply newly created
     	Customer cust = (Customer) abort_items.get("" + op_id);
     	if (cust != null)
     	{
     		writeData(op_id, cust.getKey(), cust);
     	}
 
     	//remove any temporary data from non_committed_items
     	cust = (Customer) non_committed_items.get("" + op_id);
     	if (cust != null)
     	{
         	non_committed_items.remove(cust.getKey());
     	}
     	
     	return;
     }   
     
     /**
      * This method is called whenever something is committed/aborted in order to flush changes to disk; 
      */
     public synchronized void flushToDisk()
     {   
     	try {
 	    	//retrieve master record file (if it doesn't exist, create it and write out string)
 	        String masterPath = "/home/2011/nwebst1/comp512/data/middleware/master_record.loc";
 			String newLocation = "/home/2011/nwebst1/comp512/data/middleware";
 	        
 	        File masterFile = new File(masterPath);
 	        
 	        //if master doesn't exist, create it and write default path
 	        if (!masterFile.exists())
 	        {
 	        	//create master record file
 	        	masterFile.getParentFile().getParentFile().mkdir();
 	        	masterFile.getParentFile().mkdir();
 	        	masterFile.createNewFile();
 	        	
 	        	//create default string
 	        	newLocation = "/home/2011/nwebst1/comp512/data/middleware/dataA/";
 				Trace.info("NEW MASTERFILE LOCATION: " + newLocation);
 
 	        	FileOutputStream fos = new FileOutputStream(masterFile);
 	        	ObjectOutputStream oos = new ObjectOutputStream(fos);
 	        	oos.writeObject(newLocation);
 	        	fos.close();
 	        	oos.close();
 	        }
 	        //otherwise, read in string file path for master record location
 	        else
 	        {
 	        	FileInputStream fis = new FileInputStream(masterFile);
 	        	ObjectInputStream ois = new ObjectInputStream(fis);
 	        	String dataPath = (String) ois.readObject();
 	        	fis.close();
 	        	ois.close();
 	        	
 	        	//update master record				
 				String[] masterPathArray = dataPath.split("/");
 				String data_location = masterPathArray[masterPathArray.length - 1];
 				
 				if (data_location.equals("dataA"))
 				{
 					newLocation = newLocation + "/dataB/";
 				}
 				else
 				{
 					newLocation = newLocation + "/dataA/";
 				}
 				
 				Trace.info("NEW MASTERFILE LOCATION: " + newLocation);
 				
 				//write new location to master_record.loc
 				masterFile = new File(masterPath);
 				FileOutputStream fos = new FileOutputStream(masterFile);
 		    	ObjectOutputStream oos = new ObjectOutputStream(fos);
 				oos.writeObject(newLocation);
 				fos.close();
 				oos.close();
 	        }
        
 	    	//create file paths for data for this RM
         	//get paths to data items for this RM
         	String filePathItems = newLocation + "items_table.data";
         	
         	//create file objects so that we can write data to disk
 	    	File items_file = new File(filePathItems);
 	    	
     		// if files don't exist, then create them
     		if (!items_file.exists()) {
     			items_file.getParentFile().mkdirs();
     			items_file.createNewFile();
     		}
     		
         	//write "persistent" items to disk
 	    	FileOutputStream fos = new FileOutputStream(items_file);
 	    	ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(m_itemHT);
 			fos.close();
 			oos.close();
 					
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}  	
     }
      
     /**
      * This method loads in the least-recently written data
      */
     public void rollback() throws RemoteException 
     {
     	try {
 		    System.out.println("Reading in existing data...");
 		    String masterPath = "/home/2011/nwebst1/comp512/data/middleware/master_record.loc";
 		    File f = new File(masterPath);
 		    
 			String newLocation = "/home/2011/nwebst1/comp512/data/middleware";
 		    
 			//if master doesn't exist, create it and write default path
 		    if (f.exists())
 		    {
 			    //get path to master record
 		    	FileInputStream fis = new FileInputStream(f);
 		    	ObjectInputStream ois = new ObjectInputStream(fis);
 		    	String dataPath = (String) ois.readObject();
 		    	fis.close();
 		    	ois.close();
 		    	
 		    	//update master record		
 				String[] masterPathArray = dataPath.split("/");
 				String data_location = masterPathArray[masterPathArray.length - 1];
 				
 				if (data_location.equals("dataA"))
 				{
 					newLocation = newLocation + "/dataB/";
 				}
 				else
 				{
 					newLocation = newLocation + "/dataA/";
 				}	
 		    }
 			
 			//get paths to data items for this RM
 			String filePathItems = newLocation + "items_table.data";
 			
 			//create file objects for these data files
 			File items_file = new File(filePathItems);
 		  	
 		  	//load items data into memory
 			if(items_file.exists()){
 		    	FileInputStream fis = new FileInputStream(items_file);
 		    	ObjectInputStream ois = new ObjectInputStream(fis);
 		    	m_itemHT = (RMHashtable) ois.readObject();
 		    	fis.close();
 		    	ois.close();
 		    }
     	}
     	catch (IOException e) 
     	{
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
     }
 	
     // Reads a data item
     private RMItem readData( int id, String key )
     {
         synchronized(m_itemHT) {
             return (RMItem) m_itemHT.get(key);
         }
     }
     
     //Reads a data item from uncomitted data
     private RMItem readNonCommittedData( int id )
     {
             return (RMItem) non_committed_items.get("" + id);
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
     @SuppressWarnings("unchecked")
 	protected boolean reserveItem(int op_id, int cid, String key, String location) {
     	try 
     	{
     		Trace.info("RM::reserveItem( " + op_id + ", customer=" + cid + ", " +key+ ", "+location+" ) called" );        
 	        // Read customer object if it exists (and read lock it)
             Customer cust;
             
             if ((cust = (Customer) readNonCommittedData( op_id ))==null)
            	{
             	cust = (Customer) readData( op_id, Customer.getKey(cid));
       		}
             if ( cust == null ) {
 	            Trace.warn("RM::reserveItem( " + op_id + ", " + cid + ", " + key + ", "+location+")  failed--customer doesn't exist" );
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
 	        	item = flights_rm.getReservableItem(op_id, key);
 	        }
 	        //else if the item is a car
 	        else if (tokens[0].equals("car"))
 	        {
 	        	item = cars_rm.getReservableItem(op_id, key);
 	        }
 	        //otherwise it's a room
 	        else
 	        {
 	        	item = rooms_rm.getReservableItem(op_id, key);
 	        }	        
 	        
 	        if ( item == null ) {
 	            Trace.warn("RM::reserveItem( " + op_id + ", " + cid + ", " + key+", " +location+") failed--item doesn't exist" );
 	            return false;
 	        } else if (item.getCount()==0) {
 	            Trace.warn("RM::reserveItem( " + op_id + ", " + cid + ", " + key+", " + location+") failed--No more items" );
 	            return false;
 	        } else {   
 	        	//create a copy of the customer to be committed or disarded on abort with changes
 	        	Customer temp = new Customer(cid);
 	        	temp.setReservations((RMHashtable)cust.getReservations().clone());
 	        	abort_items.put("" + op_id, temp);
      	        cust.reserve( key, location, item.getPrice());        
 	        	non_committed_items.put("" + op_id, cust);
 	            
 	            // decrease the number of available items in the storage
 	            boolean resource_updated = false;
 	            
 	            if (tokens[0].equals("flight"))
 		        {
 	            	resource_updated = flights_rm.itemReserved(op_id, item);
 		        }
 		        //else if the item is a car
 		        else if (tokens[0].equals("car"))
 		        {
 	            	resource_updated = cars_rm.itemReserved(op_id, item);
 		        }
 		        //otherwise it's a room
 		        else
 		        {
 	            	resource_updated = rooms_rm.itemReserved(op_id, item);
 		        }
 
 	            if (resource_updated)
 	            {
 		            Trace.info("RM::reserveItem( " + op_id + ", " + cid + ", " + key + ", " +location+") succeeded" );
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
 			
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Flight.getKey(flightNum));
 		args.put("flightNum", flightNum);
 		args.put("flightSeats", flightSeats);
 		args.put("flightPrice", flightPrice);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		boolean to_return = tm.addOperation(id, flights_rm, OP_CODE.ADD_FLIGHT, args, keys);
 		return to_return;
 	}
 
 	@Override
 	public boolean addCars(int id, String location, int numCars, int price)
 			throws RemoteException {
 		
 		long start = System.currentTimeMillis();
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Car.getKey(location));
 		args.put("location", location);
 		args.put("numCars", numCars);
 		args.put("price", price);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		boolean to_return = tm.addOperation(id, cars_rm, OP_CODE.ADD_CARS, args, keys);
 		long end = System.currentTimeMillis();
 		Trace.info("ADD CAR TIME: " + (end-start));
 		return to_return;
 	}
 
 	@Override
 	public boolean addRooms(int id, String location, int numRooms, int price)
 			throws RemoteException {
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Hotel.getKey(location));
 		args.put("location", location);
 		args.put("numRooms", numRooms);
 		args.put("price", price);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		return tm.addOperation(id, rooms_rm, OP_CODE.ADD_ROOMS, args, keys);
 	}
 
 	// customer functions
     // new customer just returns a unique customer identifier
     
     public int newCustomer(int id)
         throws RemoteException
     {
     	Hashtable<String, Object> args = new Hashtable<String, Object>();
     	
         // Generate a globally unique ID for the new customer
         int cid = Integer.parseInt( String.valueOf(id) +
                                 String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                 String.valueOf( Math.round( Math.random() * 100 + 1 )));
         args.put("key", Customer.getKey(cid));
         args.put("cid", cid);
         
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
         return tm.addOperationIntReturn(id, this, OP_CODE.NEW_CUSTOMER, args, keys);
     }
     
     /**
      * Method which executes new customer here in Middleware (not accessible to client,
      * and the changes it makes are not persistent until the operation that called it
      * has been committed
      * @param op_id
      * @param cid
      * @return
      */
     @SuppressWarnings("unchecked")
 	public int newCustomerExecute(int op_id, int cid)
     {
         Trace.info("INFO: RM::newCustomer(" + cid + ") called" );
         
         Customer cust;
         
         if ((cust = (Customer) readNonCommittedData( op_id ))==null)
        	{
         	cust = (Customer) readData( op_id, Customer.getKey(cid));
   		}
         if ( cust == null ) {
             cust = new Customer(cid);
             non_committed_items.put("" + op_id, cust);
             Trace.info("INFO: RM::newCustomer(" + op_id + ", " + cid + ") created a new customer" );
             Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
             return cid;
         }
         else
         {
             Trace.info("INFO: RM::newCustomer(" + op_id + ", " + cid + ") failed--customer already exists");
         	return -1;
         }
     }
 
     // I opted to pass in customerID instead. This makes testing easier
     public boolean newCustomer(int id, int customerID )
         throws RemoteException
     {
     	Hashtable<String, Object> args = new Hashtable<String, Object>();
         args.put("key", Customer.getKey(customerID));
         args.put("cid", customerID);
         
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
     	return tm.addOperation(id, this, OP_CODE.NEW_CUSTOMER_ID, args, keys);
     }
 
 	@Override
 	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 	
 		args.put("key",  Flight.getKey(flightNum));
 		args.put("flightNum", flightNum);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperation(id, flights_rm, OP_CODE.DELETE_FLIGHT, args, keys);
 	}
 
 	@Override
 	public boolean deleteCars(int id, String location) throws RemoteException {
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Car.getKey(location));
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperation(id, cars_rm, OP_CODE.DELETE_CARS, args, keys);
 	}
 
 	@Override
 	public boolean deleteRooms(int id, String location) throws RemoteException {
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Hotel.getKey(location));
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperation(id, rooms_rm, OP_CODE.DELETE_ROOMS, args, keys);
 	}
 
 	 // Deletes customer from the database. 
 	public boolean deleteCustomer(int id, int customerID)
         throws RemoteException
     {
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
         args.put("key", Customer.getKey(customerID));
         args.put("cid", customerID);
         args.put("customer_object", m_itemHT.get(Customer.getKey(customerID)));
         
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
     	return tm.addOperation(id, this, OP_CODE.DELETE_CUSTOMER, args, keys);
     }
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public boolean deleteCustomerExecute(int op_id, int cid) throws RemoteException 
     {
     	Trace.info("RM::deleteCustomer(" + op_id + ", " + cid + ") called" );
     	
     	Customer cust;
         if ((cust = (Customer) readNonCommittedData( op_id ))==null)
        	{
         	cust = (Customer) readData( op_id, Customer.getKey(cid));
   		}
         if ( cust == null ) {
             Trace.warn("RM::deleteCustomer(" + op_id + ", " + cid + ") failed--customer doesn't exist" );
             return false;
         } else {            
             // Increase the reserved numbers of all reservable items which the customer reserved. 
             RMHashtable reservationHT = cust.getReservations();
             for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
                 String reservedkey = (String) (e.nextElement());
                 ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                 Trace.info("RM::deleteCustomer(" + op_id + ", " + cid + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
 
                 //determine whether this item is a flight,room, or car
                 String key = reserveditem.getKey();  
                 String delims = "[-]";
     	        String[] tokens = key.split(delims);
     	        
     	
     	        // check if the item is available
     	        //if it's a flight
     	        if (tokens[0].equals("flight"))
     	        {
     	        	flights_rm.itemUnReserved(op_id, cid, key, reserveditem);
     	        }
     	        //else if the item is a car
     	        else if (tokens[0].equals("car"))
     	        {
     	        	cars_rm.itemUnReserved(op_id, cid, key, reserveditem);
     	        }
     	        //otherwise it's a room
     	        else
     	        {
     	        	rooms_rm.itemUnReserved(op_id, cid, key, reserveditem);
     	        }              
             }
             //add customer object to abort_items so that if we abort the deletion we can put
             //the customer back
             abort_items.put("" + op_id, cust);
             
             // remove the customer from the storage
             removeData(op_id, cust.getKey());
             //TODO save customer info incase of abortion
             Trace.info("RM::deleteCustomer(" + op_id + ", " + cid + ") succeeded" );
             return true;
         } // if
     }
 
 
 	@Override
 	public int queryFlight(int id, int flightNumber) throws RemoteException {
 
 		long start = System.currentTimeMillis();
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Flight.getKey(flightNumber));
 		args.put("flightNum", flightNumber);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		int to_return = tm.addOperationIntReturn(id, flights_rm, OP_CODE.QUERY_FLIGHTS, args, keys);
 		long end = System.currentTimeMillis();
 		Trace.info("QUERY FLIGHT TIME: " + (end-start));
 		return to_return;
 	}
 
 	@Override
 	public int queryCars(int id, String location) throws RemoteException {
 
 		long start = System.currentTimeMillis();
 		
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Car.getKey(location));
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		int to_return = tm.addOperationIntReturn(id, cars_rm, OP_CODE.QUERY_CARS, args, keys);
 		long end = System.currentTimeMillis();
 		Trace.info("QUERY CAR TIME: " + (end-start));
 		return to_return;
 	}
 
 	@Override
 	public int queryRooms(int id, String location) throws RemoteException {
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Hotel.getKey(location));
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperationIntReturn(id, rooms_rm, OP_CODE.QUERY_CARS, args, keys);
 	}
 
 	   // return a bill
     public String queryCustomerInfo(int id, int customerID)
         throws RemoteException
     {
     	long start = System.currentTimeMillis();
     	Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Customer.getKey(customerID));
 		args.put("cid", customerID);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		String to_return = tm.addOperationStringReturn(id, this, OP_CODE.QUERY_CUSTOMER_INFO, args, keys);
 		long end = System.currentTimeMillis();
 		Trace.info("QUERY CUSTOMER TIME: " + (end-start));
 		return to_return;
     }
     
     public String queryCustomerInfoExecute(int id, int customerID)
             throws RemoteException
         {
             Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
             Customer cust;
             
             if ((cust = (Customer) readNonCommittedData( id ))==null)
            	{
             	cust = (Customer) readData( id, Customer.getKey(customerID));
       		}           
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
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Flight.getKey(flightNumber));
 		args.put("flightNum", flightNumber);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperationIntReturn(id, flights_rm, OP_CODE.QUERY_FLIGHT_PRICE, args, keys);
 	}
 
 	@Override
 	public int queryCarsPrice(int id, String location) throws RemoteException {
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Car.getKey(location));
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperationIntReturn(id, cars_rm, OP_CODE.QUERY_CAR_PRICE, args, keys);
 	}
 
 	@Override
 	public int queryRoomsPrice(int id, String location) throws RemoteException {
 
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("key", Hotel.getKey(location));
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("key"));
 		
 		return tm.addOperationIntReturn(id, cars_rm, OP_CODE.QUERY_ROOM_PRICE, args, keys);
 	}
 
 	@Override
 	public boolean reserveFlight(int id, int customer, int flightNumber)
 			throws RemoteException {
 		
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("flight_key", Flight.getKey(flightNumber));
 		args.put("customer_key", Customer.getKey(customer));
 		args.put("cid", customer);
 		args.put("flightNum", flightNumber);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("flight_key"));
 		keys.add((String)args.get("customer_key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		boolean toReturn = tm.addOperation(id, this, OP_CODE.RESERVE_FLIGHT, args, keys);
 		
 		return toReturn;
 	}
 	
 	public boolean reserveFlightExecute(int id, int customer, int flightNumber)
 	{
         return reserveItem(id, customer, Flight.getKey(flightNumber), String.valueOf(flightNumber));
 	}
 
 	@Override
 	public boolean reserveCar(int id, int customer, String location)
 			throws RemoteException {
 		
 		long start = System.currentTimeMillis();
 		
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("car_key", Car.getKey(location));
 		args.put("customer_key", Customer.getKey(customer));
 		args.put("cid", customer);
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("car_key"));
 		keys.add((String)args.get("customer_key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		boolean to_return = tm.addOperation(id, this, OP_CODE.RESERVE_CAR, args, keys);	
 		long end = System.currentTimeMillis();
 		Trace.info("RESERVE CAR TIME: "+(end-start));
 		return to_return;
 	}
 	
 	
 	public boolean reserveCarExecute(int id, int customer, String location)
 			throws RemoteException {
 
         return reserveItem(id, customer, Car.getKey(location), location);
 	}
 
 	@Override
 	public boolean reserveRoom(int id, int customer, String location)
 			throws RemoteException {
 		
 		long start = System.currentTimeMillis();
 		
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("room_key", Hotel.getKey(location));
 		args.put("customer_key", Customer.getKey(customer));
 		args.put("cid", customer);
 		args.put("location", location);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("room_key"));
 		keys.add((String)args.get("customer_key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		boolean to_return = tm.addOperation(id, this, OP_CODE.RESERVE_ROOM, args, keys);	
 		long end = System.currentTimeMillis();
 		Trace.info("RESERVE ROOM TIME: " + (end-start));
 		return to_return;
 	}
 	
 	public boolean reserveRoomExecute(int id, int customer, String location)
 			throws RemoteException {
 
         return reserveItem(id, customer, Hotel.getKey(location), location);
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public boolean itinerary(int id, int customer, Vector flightNumbers,
 			String location, boolean car, boolean room) throws RemoteException {
 
 		long start = System.currentTimeMillis();
 		Hashtable<String, Object> args = new Hashtable<String, Object>();
 		args.put("customer_key", Customer.getKey(customer));
 		args.put("cid", customer);
 		args.put("flightNumbers", flightNumbers);
 		args.put("location", location);
 		args.put("room_key", Hotel.getKey(location));
 		args.put("car_key", Car.getKey(location));
 		args.put("car_boolean", car);
 		args.put("room_boolean", room);
 		
 		ArrayList<String> keys = new ArrayList<String>();
 		keys.add((String)args.get("room_key"));
 		keys.add((String)args.get("car_key"));
 		Iterator it = flightNumbers.iterator();
 		while (it.hasNext())
 		{
 			int flight_num = (new Integer((String)it.next())).intValue();
 			keys.add(Flight.getKey(flight_num));
 		}
 		keys.add((String)args.get("customer_key"));
 		
 		//returns true if transaction was able to acquire all locks necessary for this operation
 		boolean to_return = tm.addOperation(id, this, OP_CODE.ITINERARY, args, keys);	
 		long end = System.currentTimeMillis();
 		Trace.info("RESERVE ITINERARY TIME: " + (end-start));
 		return to_return;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public boolean itineraryExecute(int id, int customer, Vector flightNumbers,
 			String location, boolean car, boolean room) throws RemoteException {
 		boolean confirmation = true;
 		
 		
     	//for each flight in flightNumbers, reserve flight
 		Iterator i = flightNumbers.iterator();
 		while (i.hasNext())
 		{
 			Object flight_number_object = i.next();
 			int flightNumberInt = Integer.parseInt(flight_number_object.toString());
 			confirmation = reserveItem(id, customer, Flight.getKey(flightNumberInt), String.valueOf(flightNumberInt));
 			Trace.info("Flight no: " + flightNumberInt + " is booked?: " + confirmation);
 		}
 		
 		//if there was a car to be reserved as well
 		if (car)
 		{
 			confirmation = reserveItem(id, customer, Car.getKey(location), location);
 			Trace.info("Car: is booked?: " + confirmation);
 
 		}
 		
 		//if there was a room to be reserved as well
 		if (room)
 		{
 			confirmation = reserveItem(id, customer, Hotel.getKey(location), location);
 			Trace.info("Room: is booked?: " + confirmation);
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
 		return false;
 	}
 
 	@Override
 	public void itemUnReserved(int id, int customerID, String key,
 			ReservedItem reserveditem) throws RemoteException {
 		
 	}
 	
 	/**
 	 * This method simulates a crash either in an RM or in this Middleware
 	 */
 	public void crash(String which) throws RemoteException 
 	{
 		if(which.equals("middleware"))
 		{
 			try {
 				//unregister this RM from the registry
 				Naming.unbind("//localhost:" + port + "/" + registry_name);
 				
 				//Unexport; this will also remove this RM from the RMI runtime.
 				UnicastRemoteObject.unexportObject(this, true);
 				
 				Trace.info("Simulating middleware crash...");
 							
 				
 			} catch (NotBoundException e) {
 				e.printStackTrace();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 	
 	
 	/**
 	 * This method shuts down the middleware cleanly
 	 */
 	public void shutdown() throws RemoteException
 	{
 		//TODO should we abort all transactions upon shutdown?
 		flights_rm.shutdown();
 		cars_rm.shutdown();
 		rooms_rm.shutdown();
 		flushToDisk();
 		try {
 			//unregister this RM from the registry
 			Naming.unbind("//localhost:" + port + "/" + registry_name);
 
 			//Unexport; this will also remove this RM from the RMI runtime.
 			UnicastRemoteObject.unexportObject(this, true);
 			
 			Trace.info("Shutting down Middleware.");
 						
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Vote vote(int operationID, OP_CODE code) 
 	{
 		boolean voteYes = (non_committed_items.get("" + operationID) != null) || (abort_items.get("" + operationID) != null);
 		
 		boolean queryMethod = code.equals(OP_CODE.QUERY_CAR_PRICE) || code.equals(OP_CODE.QUERY_CARS) 
 				|| code.equals(OP_CODE.QUERY_CUSTOMER_INFO) || code.equals(OP_CODE.QUERY_FLIGHT_PRICE) 
 				|| code.equals(OP_CODE.QUERY_FLIGHTS)  || code.equals(OP_CODE.QUERY_ROOM_PRICE) 
 				|| code.equals(OP_CODE.QUERY_ROOMS);
 		
 		if(!voteYes && queryMethod)
 			voteYes = true;
 		
 		Vote vote = ((voteYes) ? Vote.YES : Vote.NO);
 		
 		Trace.info(" Middleware RM voted " + vote + " on Operation " + operationID);
 		
 		return vote;
 	}
 
 	@Override
 	public String getName() throws RemoteException {
 		return "middleware";
 	}
 	
 	@Override
 	public void setCrashFlags(String serverToCrash, CrashType crashType)
 	{
 		MiddlewareImpl.serverToCrash = serverToCrash;
 		MiddlewareImpl.crashType = crashType;
 		
 		if(serverToCrash.equals("middleware"))
 			tm.setCrashFlags(this, crashType, serverToCrash);
 		else if(serverToCrash.equals("flights"))
 			tm.setCrashFlags(flights_rm, crashType, serverToCrash);
 		else if(serverToCrash.equals("cars"))
 			tm.setCrashFlags(cars_rm, crashType, serverToCrash);
 		else 
 			tm.setCrashFlags(rooms_rm, crashType, serverToCrash);
 		
 	}
 
 }
