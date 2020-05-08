 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package stockexserver;
 
 import stockEx.ConfigManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import stockEx.IAuthentication;
 import stockEx.IStockQuery;
import stockexserver.dataAccess.DataAccess;
 
 /**
  *
  * @author harsimran.maan
  */
 public class StockExServer
 {
 
     private static void printMessage(String name)
     {
         System.out.println("Registered " + name);
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws RemoteException
     {
 
         // Bind the remote object in the registry
         Registry registry = LocateRegistry.createRegistry(Integer.parseInt(ConfigManager.getInstance().getPropertyValue("port")));
 
         registry.rebind(IStockQuery.class.getSimpleName(), new StockQuery());
         printMessage(IStockQuery.class.getSimpleName());
 
         registry.rebind(IAuthentication.class.getSimpleName(), new Authentication());
 
         printMessage(IAuthentication.class.getSimpleName());
        DataAccess.Connect();
         System.out.println("Server started");
         while (true);
 
     }
 }
