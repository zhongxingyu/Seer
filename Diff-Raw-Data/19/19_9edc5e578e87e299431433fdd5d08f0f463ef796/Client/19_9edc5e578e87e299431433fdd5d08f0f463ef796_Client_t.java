 package client;
 
 import static utils.ClientUtils.retrieveWorldManager;
 import interfaces.WorldManager;
 
 import org.omg.CORBA.ORB;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 
 public class Client {
 
 	private static UserManager userManager;
 	private static ORB orb;
 	private static POA rootpoa;
 
 	public static void main(String[] args) {
 		try {
 			// Lancement du client et récupération de la référence du serveur
 			orb = ORB.init(args, null);
			rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
 			rootpoa.the_POAManager().activate();
 			WorldManager worldManager = retrieveWorldManager(orb);
 			userManager = new UserManager(worldManager);
 			userManager.start();
 			orb.run();
 		} catch (Exception e) {
 			e.printStackTrace(System.out);
 		}
 
 	}
 
 	public static UserManager getUserManager() {
 		return userManager;
 	}
 
 	public static ORB getOrb() {
 		return orb;
 	}
 
 	public static POA getRootpoa() {
 		return rootpoa;
 	}
 }
