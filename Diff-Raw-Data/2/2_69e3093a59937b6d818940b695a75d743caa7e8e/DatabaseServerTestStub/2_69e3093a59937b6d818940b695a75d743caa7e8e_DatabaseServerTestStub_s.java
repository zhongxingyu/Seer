 package oodles.DBTest;
 
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 import oodles.RMICommon.*;
 
 /**
  * <p>
  * 	This is a Testing-stub implementation of the RemoteDatabaseServer. Running the main
  * 	method within this class will attempt create a new registry on the local machine, and
  * 	pre load it with some test databases.
  * </p>
  * 
  * <strong>RMI Security Policy</strong>
  * 
  * <p>
  * 	RMI needs a security policy to be specified, or it will throw exceptions about
  * 	access privileges when you try to access the remote objects from the registry.
  * </p>
  * 
  * <p>
  * To specify a security policy, you need to launch the Java VM with a special argument:
  * </p>
  * 
 * <code>-Djava.security.policy=<var>[path to security policy]></var></code>
  * 
  * @author mitch
  *
  */
 public class DatabaseServerTestStub extends UnicastRemoteObject  implements RemoteDatabaseServer {
 	
 	private static final long serialVersionUID = 5489330319053253738L;
 
 	protected DatabaseServerTestStub() throws RemoteException {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public int createDatabase(String databaseName) throws SQLException {
 		
 		System.out.println("CREATE DATABASE: " + databaseName);
 		
 		return 0;
 		
 	}
 
 	public int dropDatabase(String databaseName) throws SQLException {
 		
 		System.out.println("DROP DATABASE: " + databaseName);
 		
 		return 0;
 	}
 
 	public ResultSet showDatabases() throws SQLException {
 		
 		System.out.println("SHOW DATABASES");
 		
 		return null;
 	}
 	
 	
 	/**
      * This method will start the server, and register
      * some default DBs in the default local registry.
      * 
      * 
      * "The application never quits, it must be killed manually.
      * (The application will quit if there are no more references to the printers
      * either in the registry or on client side)." from RMI Printer Documentation
      */
     public static void main(String[] args) {
     	
     	try {
 			LocateRegistry.createRegistry(1099);
 		} catch (RemoteException e1) {
 			System.err.println("Error starting registry.");
 			e1.printStackTrace();
 		}
     	
     	if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
     	}
     	try {
         	
             Registry registry = LocateRegistry.getRegistry();
             
             /* Register the server object */
             
             registry.rebind("OodleDB", new DatabaseServerTestStub());
 
             /* Register the individual Database objects */
             
             registry.rebind("OodleDB.DB1", new DatabaseTestStub("OodleDB.DB1"));
             registry.rebind("OodleDB.DB2", new DatabaseTestStub("OodleDB.DB2"));
             registry.rebind("OodleDB.DB3", new DatabaseTestStub("OodleDB.DB3"));
             
         } catch (RemoteException e) {
             
         	System.err.println("Something wrong happended on the remote end");
             e.printStackTrace();
             
             System.exit(-1); // can't just return, rmi threads may not exit
         } 
         System.out.println("OodleDB TestStub Server ready...");
     }
 
 }
