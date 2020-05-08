 package ceid.netcins.exo;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.lang.reflect.Constructor;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServlet;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandler;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import rice.environment.Environment;
 import rice.environment.logging.Logger;
 import rice.environment.params.Parameters;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.IdFactory;
 import rice.p2p.commonapi.rawserialization.RawMessage;
 import rice.pastry.PastryNode;
 import rice.pastry.PastryNodeFactory;
 import rice.pastry.commonapi.PastryIdFactory;
 import rice.pastry.direct.DirectNodeHandle;
 import rice.pastry.direct.DirectPastryNodeFactory;
 import rice.pastry.direct.EuclideanNetwork;
 import rice.pastry.direct.GenericNetwork;
 import rice.pastry.direct.NetworkSimulator;
 import rice.pastry.direct.SphereNetwork;
 import rice.pastry.dist.DistPastryNodeFactory;
 import rice.pastry.socket.SocketPastryNodeFactory;
 import rice.pastry.socket.nat.rendezvous.RendezvousSocketPastryNodeFactory;
 import rice.pastry.standard.RandomNodeIdFactory;
 import rice.persistence.LRUCache;
 import rice.persistence.MemoryStorage;
 import rice.persistence.PersistentStorage;
 import rice.persistence.StorageManagerImpl;
 import ceid.netcins.exo.frontend.handlers.AcceptFriendRequestHandler;
 import ceid.netcins.exo.frontend.handlers.GetContentHandler;
 import ceid.netcins.exo.frontend.handlers.GetContentIDsHandler;
 import ceid.netcins.exo.frontend.handlers.GetContentTagsHandler;
 import ceid.netcins.exo.frontend.handlers.GetFriendRequestsHandler;
 import ceid.netcins.exo.frontend.handlers.GetFriendUIDsHandler;
 import ceid.netcins.exo.frontend.handlers.GetUserProfileHandler;
 import ceid.netcins.exo.frontend.handlers.GetUserTagsHandler;
 import ceid.netcins.exo.frontend.handlers.RejectFriendRequestHandler;
 import ceid.netcins.exo.frontend.handlers.SearchContentDHTHandler;
 import ceid.netcins.exo.frontend.handlers.SearchContentPNHandler;
 import ceid.netcins.exo.frontend.handlers.SearchUserDHTHandler;
 import ceid.netcins.exo.frontend.handlers.SearchUserPNHandler;
 import ceid.netcins.exo.frontend.handlers.SendFriendRequestHandler;
 import ceid.netcins.exo.frontend.handlers.SetContentTagsHandler;
 import ceid.netcins.exo.frontend.handlers.SetUserProfileHandler;
 import ceid.netcins.exo.frontend.handlers.ShareFileHandler;
 import ceid.netcins.exo.frontend.json.Json;
 import ceid.netcins.exo.user.User;
 import ceid.netcins.exo.user.UserNodeIdFactory;
 
 /**
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  * 
  */
 public class Frontend implements Serializable {
 	private static final long serialVersionUID = -7786107116335452480L;
 	public static final int REPLICATION_FACTOR = 3;
 	public static final int LEASE_PERIOD = 10000; // 10 seconds
 	public static final int TIME_TO_FIND_FAULTY = 15000; // 15 seconds
 	public static final String INSTANCE = "Frontend";
 	public static final String PROTOCOL_DIRECT = "direct";
 	public static final String PROTOCOL_SOCKET = "socket";
 	public static final String PROTOCOL_RENDEZVOUS = "rendezvous";
 	public static final String SIMULATOR_SPHERE = "sphere";
 	public static final String SIMULATOR_EUCLIDEAN = "euclidean";
 	public static final String SIMULATOR_GT_ITM = "gt-itm";
 	public static final String StorageRootDir = "eXO_Storage_Root";
 
 	private InetSocketAddress bootstrapNodeAddress;
 	private int webServerPort = 8080;
 	private int pastryNodePort;
 
 	transient private Logger logger;
 	transient private PastryNode[] nodes = null;
 	transient private CatalogService[] apps = null;
 	transient private Environment environment = null;
 	transient private IdFactory pastryIdFactory = null;
 	transient private NetworkSimulator<DirectNodeHandle, RawMessage> simulator = null;
 	transient private Server server = null;
 
 	private User[] users = null;
 	private boolean isSimulated = false;
 	private Hashtable<String, Vector<String>> queue = null;
 	private String userName = null;
 	private String resourceName = null;
 	private boolean isBootstrap = false;
 	private String rootDir = null;
 
 	private static Random reqIdGenerator = new Random(System.currentTimeMillis());
 
 	public Frontend(Environment env, String userName, String resourceName, int jettyPort, int pastryPort, String bootstrap) throws IOException {
 		this.environment = env;
 		Parameters params = environment.getParameters();
 		this.userName = userName;
 		this.resourceName = resourceName;
 		if (jettyPort > 0 && jettyPort < 65536)
 			this.webServerPort = jettyPort;
 		this.pastryNodePort = pastryPort;
 		if (pastryPort <= 0 || pastryPort > 65535)
 			pastryNodePort = params.getInt("exo_pastry_port");
 		this.queue = new Hashtable<String, Vector<String>>();
 		String pastryNodeProtocol = params.getString("exo_pastry_protocol");
 		int numSimulatedNodes = params.getInt("exo_sim_num_nodes");
 		if (numSimulatedNodes == 0 || !pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_DIRECT))
 			numSimulatedNodes = 1;
 		apps = new CatalogService[numSimulatedNodes];
 		users = new User[numSimulatedNodes];
 
 		bootstrapNodeAddress = stringToSocketAddress(bootstrap);
 		isBootstrap = (bootstrapNodeAddress != null) ? checkIsBootstrap(bootstrapNodeAddress) : true;
 
 		Id id = UserNodeIdFactory.generateNodeId(this.userName, this.resourceName);
 		users[0] = new User(id, this.userName, this.resourceName);
 
 		for (int i = 1; i < numSimulatedNodes; i++) {
 			String uname = Long.toHexString(environment.getRandomSource().nextLong());
 			String rname = Long.toHexString(environment.getRandomSource().nextLong());
 			Id simId = UserNodeIdFactory.generateNodeId(uname, rname);
 			users[i] = new User(simId, uname, rname);
 		}
 
 		initTransientMembers(env);
 	}
 
 	private InetSocketAddress stringToSocketAddress(String bootstrap) throws UnknownHostException {
 		InetSocketAddress ret = null;
 		if (bootstrap == null) {
 			ret = environment.getParameters().getInetSocketAddress("exo_pastry_bootstrap");
 		} else {
 			InetAddress bootstrapHostAddr = InetAddress.getByName(bootstrap.substring(0, bootstrap.indexOf(":")));
 			int bootstrapPort = Integer.parseInt(bootstrap.substring(bootstrap.indexOf(":") + 1));
 			ret = new InetSocketAddress(bootstrapHostAddr, bootstrapPort);
 		}
 		return ret;
 	}
 
 	private boolean checkIsBootstrap(InetSocketAddress mine) {
 		InetSocketAddress localhost = null;
 		try {
 			localhost = new InetSocketAddress(InetAddress.getLocalHost(), pastryNodePort);
 		} catch (UnknownHostException e1) {
 			return true;
 		}
 		if (localhost == null || (localhost.getAddress().getHostAddress().equals(mine.getAddress().getHostAddress()) && pastryNodePort == mine.getPort())) {
 			return true;
 		}
 		return false;
 	}
 
 	public static int nextReqID() {
 		return reqIdGenerator.nextInt();
 	}
 
 	private int waitForNode(PastryNode node) {
 		synchronized (node) {
 			while (!node.isReady()) {
 				try {
 					node.wait(1000);
 				} catch (InterruptedException ie) {
 					logger.logException("Error booting pastry node", ie);
 					return -1;
 				}
 				if (!node.isReady()) {
 					System.out.print("waiting... ");
 				}
 			}
 		}
 		return 0;
 	}
 
 	private int startPastryNodes(InetSocketAddress bootstrapAddress) {
 		if (nodes == null)
 			return -1;
 
 		if (!isSimulated) {
 			isBootstrap = (bootstrapAddress == null);
 
 			System.out.print("Starting node... ");
 			if (isBootstrap)
 				nodes[0].boot((Object)null);
 			else
 				nodes[0].boot(bootstrapAddress);
 			waitForNode(nodes[0]);
 			System.out.println("done");
 		} else {
 			System.out.print("Starting bootstrap node... ");
 			nodes[nodes.length - 1].boot((Object)null);
 			waitForNode(nodes[nodes.length - 1]);
 			System.out.println("done");
 
 			for (int i = 0; i < nodes.length - 1; i++) {
 				System.out.print("Booting node #" + (i + 2) + "/" + nodes.length + "... ");
 				nodes[i].boot(nodes[nodes.length - 1].getLocalHandle());
 				System.out.println("done");
 			}
 
 			for (int i = 0; i < nodes.length - 1; i++) {
 				System.out.print("Waiting for node #" + (i + 2) + "/" + nodes.length + " to become ready... ");
 				waitForNode(nodes[i]);
 				System.out.println("done");
 			}
 		}
 		return 0;
 	}
 
 	private CatalogService startCatalogService(PastryNode node, User user) {
 		StorageManagerImpl storage = null;
 		try {
 			storage = new StorageManagerImpl(pastryIdFactory,
 					new PersistentStorage(pastryIdFactory, user.getUsername() + "@" + user.getResourceName(), StorageRootDir, -1, environment), new LRUCache(new MemoryStorage(pastryIdFactory), 100000, environment));
 		} catch (IOException e) {
 			logger.logException("Error initializing storage manager", e);
 			return null;
 		}
 
 		CatalogService catalogService = new CatalogService(node, storage, REPLICATION_FACTOR, INSTANCE, user);
 		catalogService.start();
 
 		return catalogService;
 	}
 
 	private int startCatalogServices() {
 		if (apps == null || apps.length < 1)
 			return -1;
 		System.out.print("Starting CatalogService... ");
 		if ((apps[0] = startCatalogService(nodes[0], users[0])) == null)
 			return -1;
 		System.out.println("done");
 		for (int i = 1; i < apps.length; i++) {
 			System.out.print("Starting CatalogService #" + (i + 2) + "/" + apps.length  + "... ");
 			if ((apps[i] = startCatalogService(nodes[i], users[i])) == null)
 				return -1;
 			System.out.println("done");
 		}
 
 		System.out.print("Queueing profile indexing... ");
 
 		System.out.println("done");
 
 		for (int i = 1; i < apps.length; i++) {
 			System.out.print("Queueing profile indexing for user #" + (i + 1) + "/" + apps.length  + "... ");
 
 			System.out.println("done");
 		}
 		return 0;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void addServletToContext(Class handlerClass, ServletContextHandler context) {
 		Constructor constructor = null;
 		Class[] params = new Class[] { CatalogService.class, Hashtable.class };
 		try {
 			constructor = handlerClass.getConstructor(params);
 		} catch (Exception e) {
 			logger.logException("Unable to find constructor", e);
 			return;
 		}
 		try {
 			context.addServlet(new ServletHolder((HttpServlet)constructor.newInstance(apps[0], queue)),  "/" + handlerClass.getSimpleName().replace("Handler", "/"));
 		} catch (Exception e) {
 			logger.logException("Unable to instantiate new handler", e);
 			return;
 		}
 	}
 
 	private ContextHandler mountFileRoute(String url, String templateName) {
 		ContextHandler plainFileContext = new ContextHandler();
 		plainFileContext.setContextPath(url);
 		ResourceHandler plainFileHandler = new ResourceHandler();
 		plainFileHandler.setDirectoriesListed(false);
 		plainFileHandler.setWelcomeFiles(new String[] { templateName});
 		plainFileHandler.setResourceBase(rootDir);
 		plainFileContext.setHandler(plainFileHandler);
 		return plainFileContext;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private int startWebServer() {
 		rootDir = environment.getParameters().getString("exo_jetty_root");
 		if (rootDir != null)
 			System.setProperty("jetty.home", rootDir);
 
 		ClassLoader cl = Frontend.class.getClassLoader();
		URL jarRootUrl = cl.getResource(rootDir.startsWith("/") ? "." + rootDir : "./" + rootDir);
 		if (jarRootUrl != null) {
 			System.err.print("Jar-in-jar");
 			rootDir = jarRootUrl.toExternalForm();
 		} else
 			System.err.print("Native");
 		System.err.println(" mode detected; loading web resources from " + rootDir);
 
 		server = new Server();
 
 		SelectChannelConnector connector = new SelectChannelConnector();
 		connector.setPort(webServerPort);
 		server.setConnectors(new Connector[] { connector });
 
 		ContextHandlerCollection handlersList = new ContextHandlerCollection();
 
 		// XXX: Watch out! Handlers are scanned in-order until baseRequest.handled = true, and matched on a String.startsWith() basis
 		Class[] handlerClasses = new Class[] {
 				ShareFileHandler.class,
 				SetUserProfileHandler.class,
 				SetContentTagsHandler.class,
 				GetUserProfileHandler.class,
 				GetUserTagsHandler.class,
 				GetFriendRequestsHandler.class,
 				GetFriendUIDsHandler.class,
 				GetContentTagsHandler.class,
 				GetContentIDsHandler.class,
 				GetContentHandler.class,
 				SendFriendRequestHandler.class,
 				AcceptFriendRequestHandler.class,
 				RejectFriendRequestHandler.class,
 				SearchUserDHTHandler.class,
 				SearchUserPNHandler.class,
 				SearchContentDHTHandler.class,
 				SearchContentPNHandler.class
 		};
 		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		servletContextHandler.setContextPath("/servlet");
 		servletContextHandler.setResourceBase(rootDir);
 		servletContextHandler.setAllowNullPathInfo(true);
 		for (Class<ContextHandler> handlerClass : handlerClasses) {
 			addServletToContext(handlerClass, servletContextHandler);
 		}
 		handlersList.addHandler(servletContextHandler);
 
 		/*
 		 * File URL routes deployment  
 		 */		
 		handlersList.addHandler(mountFileRoute("/", "index.html"));
 		handlersList.addHandler(mountFileRoute("/search", "search.html"));
 		handlersList.addHandler(mountFileRoute("/content", "content.html"));
 		handlersList.addHandler(mountFileRoute("/friends", "friends.html"));
 
 		handlersList.addHandler(new DefaultHandler());
 
 		server.setHandler(handlersList);
 		Json.init(); // Make sure Json singleton is instantiated
 
 		try {
 			server.start();
 			server.join();
 		} catch (Exception e) {
 			logger.logException("Error starting web server", e);
 			return -1;
 		}
 		return 0;
 	}
 
 	public void run(InetSocketAddress bootstrapAddress) {
 		Thread.currentThread().setName("eXO main thread");
 		System.err.println("User/Node ID: " + users[0].getUID().toStringFull());
 
 		if (startPastryNodes(bootstrapAddress) == -1 || startCatalogServices() == -1 || startWebServer() == -1)
 			return;
 	}
 
 	private static void usage() {
 		System.err.println(
 				"Usage: java ... ceid.netcins.exo.Frontend\n" +
 				"\t-u|--user <username>\n" +
 				"\t-r|--resource <resourcename>\n" +
 				"\t-w|--webport <web ui port>\n" +
 				"\t-d|--dhtport <pastry node port>\n" +
 				"\t-b|--bootstrap <bootstrap node address:port>\n" +
 				"\t-h|--help (this message)"
 		);
 	}
 
 	public static void main(String[] args) {
 		String userName = null, resourceName = null, bootstrap = null;
 		int webPort = 0, pastryPort = 0, c;
 
 		LongOpt[] longopts = new LongOpt[6];
 		longopts[0] = new LongOpt("user", LongOpt.REQUIRED_ARGUMENT, null, 'u');
 		longopts[1] = new LongOpt("resource", LongOpt.REQUIRED_ARGUMENT, null, 'r');
 		longopts[2] = new LongOpt("webport", LongOpt.REQUIRED_ARGUMENT, null, 'w');
 		longopts[3] = new LongOpt("dhtport", LongOpt.REQUIRED_ARGUMENT, null, 'd');
 		longopts[4] = new LongOpt("bootstrap", LongOpt.REQUIRED_ARGUMENT, null, 'b');
 		longopts[5] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
 		Getopt opts = new Getopt("Frontend", args, "u:r:w:d:b:h", longopts);
 
 		while ((c = opts.getopt()) != -1) {
 			switch (c) {
 				case 'u':
 					userName = opts.getOptarg();
 					break;
 				case 'r':
 					resourceName = opts.getOptarg();
 					break;
 				case 'w':
					webPort = Integer.parseInt(opts.getOptarg());
 					break;
 				case 'd':
					pastryPort = Integer.parseInt(opts.getOptarg());
 					break;
 				case 'b':
 					bootstrap = opts.getOptarg();
 					break;
 				case 'h':
 				default:
 					usage();
 					return;
 			}
 		}
 
 		InetSocketAddress bootstrapAddress = null;
 		Frontend cf = null;
 		Environment env = new Environment(new String[] { "freepastry", "eXO" }, null);
 		final String statefname = StorageRootDir + File.separator + env.getParameters().getString("exo_state_file");
 		File stateFile = null;
 		if (statefname != null && (stateFile = new File(statefname)) != null && stateFile.exists() && stateFile.isFile() && stateFile.canRead()) {
 			System.err.print("Loading saved state... ");
 			try {
 				cf = Frontend.loadStateFromFile(statefname);
 			} catch (Exception e) {
 				System.err.println("error");
 				e.printStackTrace();
 			}
 		}
 		if (cf == null) {
 			System.err.print("Starting new instance... ");
 			if (userName == null || resourceName == null || webPort < 0 || webPort > 65535 || pastryPort < 0 || pastryPort > 65535) {
 				usage();
 				return;
 			}
 			try {
 				cf = new Frontend(env, userName, resourceName, webPort, pastryPort, bootstrap);
 			} catch (IOException e) {
 				System.err.println("error");
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		System.err.println("done");
 
 		if (bootstrap != null) {
 			try {
 				bootstrapAddress = cf.stringToSocketAddress(bootstrap);
 				if (bootstrapAddress != null && cf.checkIsBootstrap(bootstrapAddress))
 					bootstrapAddress = null;
 			} catch (UnknownHostException e) {
 				System.err.println("Unknown bootstrap host. Bailing out...");
 				System.exit(1);
 			}
 		}
 
 		final Frontend cfObj = cf;
 		Runtime.getRuntime().addShutdownHook(
 				new Thread() {
 					public void run() {
 						System.err.print("Saving state... ");
 						Frontend.saveStateToFile(cfObj, statefname);
 						System.err.println("done");
 					}
 				}
 		);
 
 		cf.run(bootstrapAddress);
 	}
 
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		out.defaultWriteObject();
 	}
 
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		initTransientMembers(new Environment(new String[] { "freepastry", "eXO" }, null));
 	}
 
 	private void initTransientMembers(Environment env) throws IOException {
 		environment = env;
 		logger = environment.getLogManager().getLogger(getClass(),null);
 		pastryIdFactory = new PastryIdFactory(environment);
 		Parameters params = environment.getParameters();
 
 		InetSocketAddress localhost = null;
 		try {
 			localhost = new InetSocketAddress(InetAddress.getLocalHost(), pastryNodePort);
 		} catch (UnknownHostException e1) {
 			isBootstrap = true;
 		}
 		if (localhost == null || bootstrapNodeAddress == null || (localhost.getAddress().getHostAddress().equals(bootstrapNodeAddress.getAddress().getHostAddress()) && pastryNodePort == bootstrapNodeAddress.getPort())) {
 			isBootstrap = true;
 		}
 
 		UserNodeIdFactory nodeIdFactory = new UserNodeIdFactory(userName, resourceName);
 		PastryNodeFactory nodeFactory = null;
 		String pastryNodeProtocol = params.getString("exo_pastry_protocol");
 		String simulatorType = params.getString("direct_simulator_topology");
 		int numSimulatedNodes = params.getInt("exo_sim_num_nodes");
 		if (numSimulatedNodes == 0 || !pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_DIRECT))
 			numSimulatedNodes = 1;
 		nodes = new PastryNode[numSimulatedNodes];
 		apps = new CatalogService[numSimulatedNodes];
 		if (pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_DIRECT)) {
 			isSimulated = true;
 			if (simulatorType.equalsIgnoreCase(SIMULATOR_SPHERE)) {
 				simulator = new SphereNetwork<DirectNodeHandle, RawMessage>(environment);
 			} else if (simulatorType.equalsIgnoreCase(SIMULATOR_GT_ITM)){
 				simulator = new GenericNetwork<DirectNodeHandle, RawMessage>(environment);
 			} else {
 				simulator = new EuclideanNetwork<DirectNodeHandle, RawMessage>(environment);
 			}
 			nodeFactory = new DirectPastryNodeFactory(nodeIdFactory, simulator, environment);
 		} else if (pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_SOCKET)) {
 			nodeFactory = new SocketPastryNodeFactory(nodeIdFactory, bootstrapNodeAddress.getAddress(), pastryNodePort, environment);
 		} else if (pastryNodeProtocol.equalsIgnoreCase(PROTOCOL_RENDEZVOUS)) {
 			nodeFactory = new RendezvousSocketPastryNodeFactory(nodeIdFactory, bootstrapNodeAddress.getAddress(), pastryNodePort, environment, false);
 		}
 		if (nodeFactory == null)
 			nodeFactory = DistPastryNodeFactory.getFactory(new RandomNodeIdFactory(environment),
 					DistPastryNodeFactory.PROTOCOL_SOCKET, pastryNodePort, environment);
 
 		Id id = UserNodeIdFactory.generateNodeId(this.userName, this.resourceName);
 		try {
 			nodes[0] = nodeFactory.newNode((rice.pastry.Id)id);
 		} catch (IOException e) {
 			logger.logException("Unable to create pastry node", e);
 			throw e;
 		}
 
 		for (int i = 1; i < numSimulatedNodes; i++) {
 			try {
 				nodes[i] = nodeFactory.newNode((rice.pastry.Id)users[i].getUID());
 			} catch (IOException e) {
 				logger.logException("Unable to create pastry node", e);
 				throw e;
 			}
 		}
 	}
 
 	public static void saveStateToFile(Frontend fend, String statefname) {
 		FileOutputStream fos = null;
 		ObjectOutputStream out = null;
 		try {
 			fos = new FileOutputStream(statefname);
 			out = new ObjectOutputStream(fos);
 			out.writeObject(fend);
 			out.close();
 		} catch(IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public static Frontend loadStateFromFile(String statefname) throws IOException, ClassNotFoundException {
 		Frontend ret = null;
 		ObjectInputStream in = new ObjectInputStream(new FileInputStream(statefname));
 		ret = (Frontend)in.readObject();
 		in.close();
 		return ret;
 	}
 }
