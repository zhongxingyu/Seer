 package edu.berkeley.grippus.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
import java.util.Map.Entry;

 import jline.ConsoleReader;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 
 import com.caucho.hessian.client.HessianProxyFactory;
 
 import edu.berkeley.grippus.Errno;
 import edu.berkeley.grippus.fs.DFileSpec;
 import edu.berkeley.grippus.fs.DPermission;
 import edu.berkeley.grippus.fs.Permission;
 import edu.berkeley.grippus.fs.VFS;
 import edu.berkeley.grippus.util.Logging;
 import edu.berkeley.grippus.util.log.Log4JLogger;
 
 public class Node {
 	private enum NodeState { DISCONNECTED, OFFLINE, SLAVE, MASTER, INITIALIZING }
 
 	public final Logging log = new Log4JLogger();
 	private final Logger logger = log.getLogger(Node.class);
 	private volatile boolean running = false;
 	private final String name;
 	private final File serverRoot;
 	private final Configuration conf;
 	private final BackingStore bs;
 	private final Server jetty;
 	private UUID clusterID;
 	private String clusterName;
 	private final int port;
 	private NodeMasterRPC masterServer;
 	private String ipAddress;
 	private String myNodeURL;
 	private final NodeRef nodeRef;
 
 	private final VFS vfs = new VFS();
 	private final HessianProxyFactory factory = new HessianProxyFactory();
 
 	private final HashMap<String, NodeRPC> clusterMembers = new HashMap<String, NodeRPC>();
 
 	private String masterURL = null;
 
 	private NodeState state = NodeState.DISCONNECTED;
 	private String clusterPassword;
 
 	public Node(String name) {
 		this.name = name;
 		serverRoot = new File(System.getProperty("user.home"),".grippus/"+name);
 		if (!serverRoot.exists()) serverRoot.mkdirs();
 		if (!serverRoot.isDirectory())
 			throw new RuntimeException("Server root " + serverRoot + " is not a directory!");
 		conf = new Configuration(this, new File(serverRoot, "config"));
 		conf.set("node.name", name);
 		nodeRef = conf.get("node.ref", new NodeRef());
 		maybeInitializeConfig(conf);
 		bs = new BackingStore(this, new File(serverRoot, "store"));
 		//System.setProperty("org.eclipse.jetty.util.log.DEBUG", "true");
 		port = Integer.parseInt(conf.getString("node.port", "11110"));
 		jetty = new Server(port);
 		try {
 			InetAddress thisIp = InetAddress.getLocalHost();
 	    	this.setIpAddress(thisIp.getHostAddress());
 	    	this.myNodeURL = "http://"+ getIpAddress()+":"+getPort()+"/node";
 		} catch (UnknownHostException e) {
 			logger.error("Unknown host", e);
 			throw new RuntimeException("Node initialization fails", e);
 		}
 	}
 	
 	HashSet<String> getClusterURLS(){
 		return (HashSet<String>) clusterMembers.keySet();
 	}
 	
 	public static void main(String[] args) {
 		BasicConfigurator.configure();
 		if (args.length < 1 || args[0] == null || args[0].isEmpty()) {
 			System.err.println("You must supply a node instance name on the command line");
 			System.exit(1);
 		}
 		new Node(args[0]).run();
 	}
 	
 	public void run() {
 		logger.info("Server starting up...");
 		
 		configureJetty();
 		
 		try {
 			jetty.start();
 		} catch(Exception e) {
 			logger.error("Could not start jetty", e);
 		}
 		
 		running = true;
 
 		while(running) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) { /* don't bother */ }
 		}
 		
 		try {
 			jetty.stop();
 		} catch(Exception e) {
 			logger.error("Could not stop jetty", e);
 		}
 
 		logger.info("Server shutting down...");
 		logger.info("Server exiting!");
 	}
 
 	private void configureJetty() {
 		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		/*Constraint constraint = new Constraint();
 		constraint.setName(Constraint.__BASIC_AUTH);
 		constraint.setRoles(new String[] {"grippus"});
 		constraint.setAuthenticate(true);
 		ConstraintMapping cm = new ConstraintMapping();
 		cm.setConstraint(constraint);
 		cm.setPathSpec("/*");
 		ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
 		sh.setConstraintMappings(new ConstraintMapping[] {cm});
 		File tempFile;
 		try {
 			tempFile = File.createTempFile("realm", "passwd");
 			tempFile.deleteOnExit();
 			FileWriter w = new FileWriter(tempFile);
 			w.write("grippus: " + conf.getString("mgmt.password")+", grippus");
 			w.close();
 		} catch (IOException e) {
 			logger.error("IO exception while setting up authentication: ", e);
 			throw new RuntimeException("initialization failed");
 		}
 		sh.setLoginService(new HashLoginService("grippus", tempFile.getAbsolutePath()));
 		context.setSecurityHandler(sh);*/
 		context.setContextPath("/");
 		context.setAttribute("node", this);
 		context.addServlet(NodeRPCImpl.class, "/node/*");
 		context.addServlet(NodeManagementRPCImpl.class, "/mgmt/*");
 		context.addServlet(NodeMasterRPCImpl.class, "/master/*");
 		jetty.setHandler(context);
 	}
 
 	public Configuration getConf() {
 		return conf;
 	}
 	
 	private void maybeInitializeConfig(Configuration conf) {
 		ConsoleReader inp;
 		try {
 			inp = new ConsoleReader();
 			maybeInitialize(conf, inp, "node.name", "Node name: ");
 			maybeInitialize(conf, inp, "node.port", "Node port [11110]: ");
 			//maybeInitialize(conf, inp, "node.mgmtport", "Node management port [11111]: ");
 			maybeInitialize(conf, inp, "store.maxsize", "Maximum size: ");
 			maybeInitialize(conf, inp, "mgmt.password", "Management password: ");
 			
 		} catch (IOException e) {
 			logger.error("Could not read from console; cannot configure, dying");
 			throw new RuntimeException("I/O problem", e);
 		}
 	}
 	
 	private void maybeInitialize(Configuration conf, ConsoleReader inp, String key, String prompt) throws IOException {
 		if (conf.getString(key) == null)
 			conf.set(key, inp.readLine(prompt));
 	}
 
 	public BackingStore getBackingStore() {
 		return bs;
 	}
 
 	public synchronized void terminate() {
 		disconnect();
 		running = false;
 	}
 
 	public synchronized Errno addPeer(String newNodeURL) {
 		NodeRPC newNode;
 		logger.debug("New peer "+newNodeURL);
 		try {
 			HessianProxyFactory factory = new HessianProxyFactory();
 			factory.setUser("grippus");
 			factory.setPassword(clusterPassword);
 			newNode = (NodeRPC) factory.create(NodeRPC.class,newNodeURL);
 		} catch (MalformedURLException e) {
 			logger.error("Malformed URL exception for new node URL");
 			return Errno.ERROR_ILLEGAL_ARGUMENT;
 		}
 		clusterMembers.put(newNodeURL,newNode);
 		return Errno.SUCCESS;
 	}
 	
 	public Boolean isMaster() {
 		if (state == NodeState.MASTER) {
 			return true;
 		}
 		return false;
 	}
 
 	public String status() {
 		String result = "Node " + name + ": " + state + " " + nodeRef + "\n";
 		if (state == NodeState.SLAVE || state == NodeState.MASTER)
 			result += "Member of: " + clusterName + " (" + clusterID + ")\n";
 		if (state == NodeState.MASTER)
 			result += "Advertise url: " + getMasterURL() + "\n";
 		if (state == NodeState.SLAVE || state == NodeState.MASTER) {
 			result += "Cluster members:";
 			for (String name : getClusterMembers().keySet())
 				result += "\n\t" + name + "";
 		}
 		return result;
 	}
 
 	public synchronized Errno initCluster(String clusterName) {
 		if (state != NodeState.DISCONNECTED) return Errno.ERROR_ILLEGAL_ACTION;
 		disconnect();
 		state = NodeState.MASTER;
 		setMasterURL("http://"+ipAddress+":"+port+"/master");
 		masterServer = new NodeMasterRPCImpl(this);
 		this.setClusterName(clusterName);
 		setClusterID(UUID.randomUUID());
 		return Errno.SUCCESS_TOPOLOGY_CHANGE;
 	}
 	
 	/** Contacts the master node if it exists and removes self from the
 	 * canonical cluster member list. Sets master to null, clears the local
 	 * cluster list and sets the state to DISCONNECTED.
 	 */
 	public synchronized void disconnect() {
 		if(masterServer!= null){
 			masterServer.leaveCluster(this.myNodeURL);
 		}
 		masterServer = null;
 		masterURL = null;
 		setClusterID(null);
 		setClusterName(null);
 		getClusterMembers().clear();
 		state = NodeState.DISCONNECTED;
 	}
 
 	public HashMap<String, NodeRPC> getClusterMembers() {
 		return clusterMembers;
 	}
 
 	private void setMasterServer(NodeMasterRPC masterServer) {
 		this.masterServer = masterServer;
 	}
 
 	public NodeMasterRPC getMasterServer() {
 		return masterServer;
 	}
 
 	private void setMasterURL(String masterURL) {
 		this.masterURL = masterURL;
 	}
 
 	public String getMasterURL() {
 		return masterURL;
 	}
 
 	public void setClusterID(UUID clusterID) {
 		this.clusterID = clusterID;
 	}
 
 	public String getClusterID() {
 		return clusterID.toString();
 	}
 
 	public void setClusterName(String clusterName) {
 		this.clusterName = clusterName;
 	}
 
 	public String getClusterName() {
 		return clusterName;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public VFS getVFS() {
 		return vfs;
 	}
 	
 	/** Asks the master for the canonical cluster member list and checks it against
 	 *  our own; removes any excess and adds any unlisted.
 	 */
 	public void checkClusterMembers(){
 		Set<String> masterMembers = masterServer.getClusterList();
 		for(String key : clusterMembers.keySet()){
 			if(!masterMembers.contains(key)){
 				clusterMembers.remove(key);
 			}
 		}
 		try {
 			for(String m_key : masterMembers){
 				if(!clusterMembers.containsKey(m_key)){				
 						clusterMembers.put(m_key, (NodeRPC) factory.create(NodeRPC.class, m_key));				
 				}
 			}
 		} catch (MalformedURLException e) {
 			logger.error("badly formed URL", e);
 		}
 	}
 	
 	/** Checks that Node is in fact the master of the cluster. If so,
 	 *  removes the leaving node from the clusterSet and informs all other
 	 *  members of the departure.
 	 */
 	protected synchronized boolean removeNodeAsMaster(String url){
 		if(state!= NodeState.MASTER){
 			logger.warn("non-master node called to remove a node form the cluster");
 			return false;
 		}
 		if( clusterMembers.containsKey(url)){
 			clusterMembers.remove(url);
 			for(NodeRPC member : clusterMembers.values()){
 				member.advertiseLeavingNode(url);
 			}
 		}
 		return true;
 	}
 	
 	/** This method should only be called by the master through the NodeRPC.
 	 *  It removes the departed NodeRPC from the local set.
 	 */
 	protected synchronized void removeNodeLocal(String url){
 		if(clusterMembers.containsKey(url)){
 			clusterMembers.remove(url);
 		}
 	}
 
 	public NodeMasterRPC getMaster(){
 		return masterServer;
 	}
 
 	public void setIpAddress(String ipAddress) {
 		this.ipAddress = ipAddress;
 	}
 
 	public String getIpAddress() {
 		return ipAddress;
 	}
 
 	public Errno connectToServer(String masterServerURL, String clusterPassword) {
 		if (state != NodeState.DISCONNECTED) return Errno.ERROR_ILLEGAL_ACTION;
 		conf.set("cluster.password", clusterPassword);
 		this.clusterPassword = clusterPassword;
 		try {
 			state = NodeState.INITIALIZING;
 			HessianProxyFactory factory = new HessianProxyFactory();	
 			factory.setUser("grippus");
 			factory.setPassword(clusterPassword);
 			NodeMasterRPC master = (NodeMasterRPC) factory.create(NodeMasterRPC.class, masterServerURL);
 			setMasterServer(master);
 			setMasterURL(masterServerURL);
 			setClusterName(master.getMasterClusterName());
 			String clusterUUIDString = master.getMasterClusterUUID();
 			UUID clusterID = UUID.fromString(clusterUUIDString);
 			setClusterID(clusterID);
 			master.joinCluster(myNodeURL);
 			state = NodeState.SLAVE;
 		} catch (MalformedURLException e) {
 			logger.error("Malformed URL exception with master server url");
 		}
 		return Errno.SUCCESS_TOPOLOGY_CHANGE;
 	}
 
 	public Errno share(DFileSpec dfs, String realPath) {
 		throw new AssertionError("Not implemented");
 	}
 
 	public Permission defaultPermissions() {
 		return new DPermission(nodeRef);
 	}
 
 	public Errno masterAddSlave(String newNodeURL) {
 		for (NodeRPC node : clusterMembers.values()) {
 			node.advertiseJoiningNode(newNodeURL);
 		}
 		Set<String> otherNodes = clusterMembers.keySet();
 		addPeer(newNodeURL);
 		NodeRPC newNode = clusterMembers.get(newNodeURL);
 		for (String node : otherNodes)
 			newNode.advertiseJoiningNode(node);
 		return Errno.SUCCESS;
 	}
 }
