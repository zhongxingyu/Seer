 package ch.hsr.bieridee;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.neo4j.server.WrappingNeoServerBootstrapper;
 import org.neo4j.server.configuration.EmbeddedServerConfigurator;
 import org.restlet.Component;
 import org.restlet.data.Protocol;
 
 import ch.hsr.bieridee.config.Config;
 import ch.hsr.bieridee.utils.Cypher;
 import ch.hsr.bieridee.utils.DBUtil;
 import ch.hsr.bieridee.utils.Testdb;
 
 /**
  * Main Class to start the application.
  * 
  */
 public final class Main {
 	private static EmbeddedGraphDatabase GRAPHDB;
 	private static WrappingNeoServerBootstrapper SRV;
 	private static boolean FILL_TEST_DB;
 	private static Component RESTLET_SERVER;
 
 	private Main() {
 		// do not instantiate.
 	}
 
 	/**
 	 * The main!
 	 * 
 	 * @param args
 	 *            ARGH
 	 * */
 	public static void main(String[] args) {
 
		if (args.length >= 1 && "fillTestDB".equals(args[0])) {
 			FILL_TEST_DB = true;
 		} else {
 			FILL_TEST_DB = false;
 		}
 
 		startDB();
 		startAPI();
 
 		registerShutdownHook(GRAPHDB);
 
 	}
 
 	/**
 	 * Starts the neo4j database.
 	 */
 	public static void startDB() {
 		GRAPHDB = getGraphDb();
 		DBUtil.setDB(GRAPHDB);
 		Cypher.setDB(GRAPHDB);
 	}
 
 	/**
 	 * Starts the neo4j database.
 	 * 
 	 * @param fillWithTestData
 	 *            if true, flushs the database and fills it with testdata.
 	 */
 	public static void startDB(boolean fillWithTestData) {
 		FILL_TEST_DB = fillWithTestData;
 		startDB();
 	}
 
 	/**
 	 * Stops the neo4j database.
 	 */
 	public static void stopDB() {
 		GRAPHDB.shutdown();
 		GRAPHDB = null;
 		SRV.stop();
 	}
 
 	/**
 	 * starts the RESTLET API server.
 	 */
 	public static void startAPI() {
 		RESTLET_SERVER = new Component();
 		RESTLET_SERVER.getServers().add(Protocol.HTTP, Config.API_HOST, Config.API_PORT);
 		RESTLET_SERVER.getDefaultHost().attach(new Dispatcher());
 		try {
 			RESTLET_SERVER.start();
 			// SUPPRESS CHECKSTYLE: dumb restlet programmers.
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * stops the RESTLET API server.
 	 */
 	public static void stopAPI() {
 		try {
 			RESTLET_SERVER.stop();
 			// SUPPRESS CHECKSTYLE: dumb restlet programmers.
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Gets the current instance of the running graphdb or starts the instance.
 	 * 
 	 * @return The GraphDB
 	 */
 	private static EmbeddedGraphDatabase getGraphDb() {
 		if (GRAPHDB == null) {
 			if (FILL_TEST_DB) {
 				GRAPHDB = Testdb.createDB(Config.DB_PATH);
 				Testdb.fillDB(GRAPHDB);
 			} else {
 				GRAPHDB = new EmbeddedGraphDatabase(Config.DB_PATH);
 			}
 
 			EmbeddedServerConfigurator config;
 			config = new EmbeddedServerConfigurator(Main.GRAPHDB);
 			config.configuration().setProperty("org.neo4j.server.webserver.address", Config.DB_HOST);
 			config.configuration().setProperty("org.neo4j.server.webserver.port", Config.NEO4J_WEBADMIN_PORT);
 			SRV = new WrappingNeoServerBootstrapper(GRAPHDB, config);
 			SRV.start();
 			registerShutdownHook(GRAPHDB);
 		}
 		return GRAPHDB;
 	}
 
 	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
 		// Registers a shutdosrvwn hook for the Neo4j instance so that it
 		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
 		// running example before it's completed)
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				graphDb.shutdown();
 				SRV.stop();
 				System.out.println("Shutdown hook called");
 			}
 		});
 	}
 }
