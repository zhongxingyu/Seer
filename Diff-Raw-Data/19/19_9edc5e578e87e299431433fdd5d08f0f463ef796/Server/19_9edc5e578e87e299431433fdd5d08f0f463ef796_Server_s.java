 package server;
 
 import generated.VworldFactory;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.omg.CORBA.ORB;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 
 import utils.ServerUtils;
 
 public class Server {
 
 	private static ORB orb;
 	private static POA rootpoa;
 	private static VworldFactory db;
	private static final boolean SHOULD_INITIALIZE_DB = true;
 
 	public static void main(String[] args) {
 		Connection connection = null;
 		try {
 			// Initialisation de la BDD
 			Connection connexion = ServerUtils.getDbConnection();
 			db = new VworldFactory(connexion);
 			if (SHOULD_INITIALIZE_DB) {
 				ServerUtils.initializeDB(db);
 			}
 			// Enregistrement et lancement du serveur
 			orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
 			rootpoa.the_POAManager().activate();
 			ServerUtils.registerWorldManager(orb, rootpoa, new WorldManagerImpl());
 			System.out.println("Server ready and waiting ...");
 			orb.run();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			System.out.println("Server exiting ...");
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static ORB getOrb() {
 		return orb;
 	}
 
 	public static POA getRootpoa() {
 		return rootpoa;
 	}
 
 	public static VworldFactory getDb() {
 		return db;
 	}
 }
