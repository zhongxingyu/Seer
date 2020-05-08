 package serversrc.resImpl;
 
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import serversrc.resInterface.*;
 
 
 public class RMFlightImpl extends RMBaseImpl implements RMFlight {
 
 	
 	public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
         int port = 1099;
 
         if (args.length == 1) {
             server = server + ":" + args[0];
             port = Integer.parseInt(args[0]);
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.RMFlightImpl [port]");
             System.exit(1);
         }
 
         try {
             // create a new Server object
             RMFlightImpl obj = new RMFlightImpl();
             // dynamically generate the stub (client proxy)
             RMFlight rm = (RMFlight) UnicastRemoteObject.exportObject(obj, 0);
 
             // Bind the remote object's stub in the registry
             Registry registry = LocateRegistry.getRegistry(port);
             registry.rebind("Group2RMFlight", rm);
 
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
 	
 	public RMFlightImpl() throws RemoteException {
 		
 	}
 	
 	private RMItem readData( int id, String key )
     {
         synchronized(m_itemHT) {
             return (RMItem) m_itemHT.get(key);
         }
     }
 
     // Writes a data item
     private void writeData( int id, String key, RMItem value )
     {
         synchronized(m_itemHT) {
             m_itemHT.put(key, value);
         }
     }
 	
     // Create a new flight, or add seats to existing flight
     //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
     public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
         throws RemoteException
     {
         Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
         Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
         if ( curObj == null ) {
             // doesn't exist...add it
             Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
             writeData( id, newObj.getKey(), newObj );
             Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                     flightSeats + ", price=$" + flightPrice );
         } else {
             // add seats to existing flight and update the price...
             curObj.setCount( curObj.getCount() + flightSeats );
             if ( flightPrice > 0 ) {
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
 
     // Returns the number of empty seats on this flight
     public int queryFlight(int id, int flightNum)
         throws RemoteException
     {
         return queryNum(id, Flight.getKey(flightNum));
     }
 
     // Returns price of this flight
     public int queryFlightPrice(int id, int flightNum )
         throws RemoteException
     {
         return queryPrice(id, Flight.getKey(flightNum));
     }
 
 
 
 }
