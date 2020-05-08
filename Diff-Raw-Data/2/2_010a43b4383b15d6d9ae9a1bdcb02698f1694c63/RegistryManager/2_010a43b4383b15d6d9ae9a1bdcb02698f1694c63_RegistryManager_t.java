 package sell;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 public class RegistryManager {
 
 	private static Methods stub = null;
 	private static Registry registry = null;
	private static String host = "192.168.1.117";
 	private static Integer port = 3001;
 	
 	protected RegistryManager() {
 		
 	}
 
 	public static Methods getStub() {
 		try {
 			if (registry == null) {
 				registry = LocateRegistry.getRegistry(host, port);
 			}
 			if (stub == null) {
 				stub = (Methods) registry.lookup("Hello");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return stub;
 	}
 	
 	public static Registry getRegistry() {
 		try {
 			if (registry == null) {
 				registry = LocateRegistry.getRegistry(host, port);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return registry;
 	}
 }
