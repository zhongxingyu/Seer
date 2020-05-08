 package serversrc.resImpl;
 
 
 import java.rmi.registry.Registry;
 import serversrc.resInterface.*;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.rmi.RMISecurityManager;
 
 public class RMCarImpl extends RMBaseImpl implements RMCar {
 
 	//protected RMHashtable m_itemHT = new RMHashtable();
 	
 	public static void main(String args[]) {
         // Figure out where server is running
         String server = "localhost";
         int port = 1099;
 
         if (args.length == 1) {
             server = server + ":" + args[0];
             port = Integer.parseInt(args[0]);
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.RMCarImpl [port]");
             System.exit(1);
         }
 
         try {
             // create a new Server object
             RMCarImpl obj = new RMCarImpl();
             // dynamically generate the stub (client proxy)
             RMCar rm = (RMCar) UnicastRemoteObject.exportObject(obj, 0);
 
             // Bind the remote object's stub in the registry
             Registry registry = LocateRegistry.getRegistry(port);
             registry.rebind("Group2RMCar", rm);
 
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
 	
 	// Reads a data item
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
     
     public RMCarImpl() throws RemoteException {
     
     }
 	
 	@Override
 	public boolean addCars(int id, String location, int count, int price)
 			throws RemoteException {
 
 		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
 		Car curObj = (Car) readData( id, Car.getKey(location) );
 		if ( curObj == null ) {
 			// car location doesn't exist...add it
 			Car newObj = new Car( location, count, price );
 			writeData( id, newObj.getKey(), newObj );
 			Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
 		} else {
 			// add count to existing car location and update price...
 			curObj.setCount( curObj.getCount() + count );
 			if ( price > 0 ) {
 				curObj.setPrice( price );
 			} // if
 			writeData( id, curObj.getKey(), curObj );
 			Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
 		} // else
 		return(true);
 	}
 
 	@Override
 	public boolean deleteCars(int id, String location) throws RemoteException {
 		
 		return deleteItem(id, Car.getKey(location));
 	}
 
 	@Override
 	public int queryCars(int id, String location) throws RemoteException {
 		
 		return queryNum(id, Car.getKey(location));
 	}
 
 	@Override
 	public int queryCarsPrice(int id, String location) throws RemoteException {
 
 		return queryPrice(id, Car.getKey(location));
 	}
 
 
 }
