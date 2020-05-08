 package suncertify.client;
 
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import suncertify.AppType;
 import suncertify.db.DBMain;
 import suncertify.shared.Injection;
 
 public class DataServiceFactory {
 
 	public DBMain getService(AppType type) {
 
		if (type == AppType.Client) {
 			try {
 				Registry registry = LocateRegistry.getRegistry();
 				DBMain dataService = (DBMain) registry.lookup("Remote Database Server");
 				return dataService;
 			} catch (RemoteException | NotBoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} else if (type == AppType.StandAlone) {
 			return (DBMain) Injection.instance.get("DataServer");
 		}
 
 		return null;
 	}
 }
