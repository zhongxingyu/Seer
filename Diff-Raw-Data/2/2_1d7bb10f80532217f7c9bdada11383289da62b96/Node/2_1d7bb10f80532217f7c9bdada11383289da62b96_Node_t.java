 package edu.berkeley.grippus.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.UUID;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import jline.ConsoleReader;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.apache.log4j.lf5.util.StreamUtils;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 
 import com.caucho.hessian.client.HessianProxyFactory;
 import com.caucho.hessian.client.HessianRuntimeException;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 import edu.berkeley.grippus.Errno;
 import edu.berkeley.grippus.client.Client;
 import edu.berkeley.grippus.fs.DFile;
 import edu.berkeley.grippus.fs.DFileSpec;
 import edu.berkeley.grippus.fs.LocalVFS;
 import edu.berkeley.grippus.fs.SlaveVFS;
 import edu.berkeley.grippus.fs.VFS;
 import edu.berkeley.grippus.fs.perm.DPermission;
 import edu.berkeley.grippus.fs.perm.EveryonePermissions;
 import edu.berkeley.grippus.fs.perm.Permission;
 import edu.berkeley.grippus.map.FileMapper;
 import edu.berkeley.grippus.storage.Block;
 import edu.berkeley.grippus.storage.LocalFilesystemStorage;
 import edu.berkeley.grippus.storage.Storage;
 import edu.berkeley.grippus.util.Pair;
 
 public class Node {
 	private enum NodeState { DISCONNECTED, OFFLINE, SLAVE, MASTER, INITIALIZING }
 
 	private final Logger logger = Logger.getLogger(Node.class);
 	private volatile boolean running = false;
 	private final String name;
 	private final File serverRoot;
 	private final Configuration conf;
 	private final Storage bs;
 	private final Server jetty;
 	private UUID clusterID;
 	private String clusterName;
 	private final int port;
 	private NodeMasterRPC masterServer;
 	private String ipAddress;
 	private String myNodeURL;
 	private final NodeRef nodeRef;
 
 	private VFS vfs = new LocalVFS();
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
 		bs = new LocalFilesystemStorage(this, new File(serverRoot, "store"));
 		//System.setProperty("org.eclipse.jetty.util.log.DEBUG", "true");
 		port = Integer.parseInt(conf.getString("node.port", "11110"));
 		jetty = new Server(port);
 		try {
 			InetAddress thisIp = InetAddress.getLocalHost();
 			this.setIpAddress(thisIp.getHostAddress());
 			this.setMyNodeURL("http://"+ getIpAddress()+":"+getPort()+"/node");
 		} catch (UnknownHostException e) {
 			logger.error("Unknown host", e);
 			throw new RuntimeException("Node initialization fails", e);
 		}
 	}
 
 	Set<String> getClusterURLS(){
 		return clusterMembers.keySet();
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
 				logger.info("Launching local client...");
 				new Client().run(conf.getString("node.port"));
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
 
 	public synchronized void terminate() {
 		disconnect();
 		running = false;
 	}
 
 	public synchronized Errno addPeer(String newNodeURL) {
 		NodeRPC newNode;
 		logger.debug("New peer "+newNodeURL);
 		try {
 			newNode = nodeRPCForURL(newNodeURL);
 		} catch (MalformedURLException e) {
 			logger.error("Malformed URL exception for new node URL");
 			return Errno.ERROR_ILLEGAL_ARGUMENT;
 		}
 		clusterMembers.put(newNodeURL,newNode);
 		return Errno.SUCCESS;
 	}
 
 	private NodeRPC nodeRPCForURL(String newNodeURL)
 	throws MalformedURLException {
 		NodeRPC newNode;
 		HessianProxyFactory factory = new HessianProxyFactory();
 		factory.setUser("grippus");
 		factory.setPassword(clusterPassword);
 		newNode = (NodeRPC) factory.create(NodeRPC.class,newNodeURL);
 		return newNode;
 	}
 
 	public synchronized Errno getFileFromNode(Block block, int blockLength, String nodeURL) {
 		NodeRPC otherNode;
 		try {
 			otherNode = nodeRPCForURL(nodeURL);
 			byte[] fileData = otherNode.getFile(block, blockLength);
 			if (fileData != null) {
 				bs.createFile(block.getDigest(),fileData);
 				block.addNode(myNodeURL);
 			} else {
 				return Errno.ERROR_FILE_NOT_FOUND;
 			}
 		} catch (MalformedURLException e) {
 			logger.error("Malformed URL exception for new node URL");
 			return Errno.ERROR_ILLEGAL_ARGUMENT;
 		} catch (IOException e) {
 			logger.error("Writing the file failed");
 			return Errno.ERROR_EXISTS;
 		}
 		//			newNode = (NodeRPC) factory.create(NodeRPC.class,newNodeURL);
 
 		return Errno.SUCCESS;
 	}
 
 	public synchronized byte[] getFile(Block block, int length) {
 		try {
 			InputStream input = bs.readBlock(block);
 			byte[] data = new byte[length];
 			input.read(data);
 			return data;
 		} catch (IOException e) {
 			logger.error("Io exception from reading the block");
 		}
 		return null;
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
 			result += "Other cluster members:";
 			for (Entry<String, NodeRPC> node : getClusterMembers().entrySet())
 				result += "\n\t" + node.getKey() + " " + node.getValue().getNodeRef();
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
 		if(masterServer != null){
 			try {
 				masterServer.leaveCluster(this.getMyNodeURL());
 			} catch (HessianRuntimeException e) {
 				// TODO figure out some saner way to handle disappearing nodes
 			}
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
 		Set<String> masterMembers = masterServer.getOtherNodes();
 		for(String key : clusterMembers.keySet()){
 			if(!masterMembers.contains(key)){
 				if(key== getMasterURL()) continue;
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
 		try {
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
 				setClusterName(master.getClusterName());
 				String clusterUUIDString = master.getClusterUUID();
 				UUID clusterID = UUID.fromString(clusterUUIDString);
 				setClusterID(clusterID);
 				master.joinCluster(getMyNodeURL());
 				state = NodeState.SLAVE;
 				vfs = new SlaveVFS(this, master);
 			} catch (MalformedURLException e) {
 				logger.error("Malformed URL exception with master server url");
 			}
 			return Errno.SUCCESS_TOPOLOGY_CHANGE;
 		} catch (RuntimeException t) {
 			state = NodeState.DISCONNECTED;
 			throw t;
 		}
 	}
 
 	public Errno share(DFileSpec dfs, String realPath) {
 		return vfs.copyRecursive(realPath, dfs, getStorage());
 	}
 
 	public Permission defaultPermissions() {
 		return new DPermission(nodeRef);
 	}
 
 	public Errno masterAddSlave(String newNodeURL) {
 		for (NodeRPC node : clusterMembers.values()) {
 			node.advertiseJoiningNode(newNodeURL);
 		}
 		Set<String> otherNodes = new HashSet<String>(clusterMembers.keySet());
 		addPeer(newNodeURL);
 		NodeRPC newNode = clusterMembers.get(newNodeURL);
 		newNode.advertiseJoiningNode(getMyNodeURL());
 		for (String node : otherNodes)
 			newNode.advertiseJoiningNode(node);
 		return Errno.SUCCESS;
 	}
 
 	public String getNodeRef() {
 		return nodeRef.toString();
 	}
 
 	public Storage getStorage() {
 		return bs;
 	}
 
 	public Pair<Errno, String> cat(DFileSpec path) {
 		DFile f = getVFS().resolve(path);
 		if (f == null)
 			return new Pair<Errno, String>(Errno.ERROR_FILE_NOT_FOUND, "");
 		try {
 			return new Pair<Errno, String>(Errno.SUCCESS, new String(
 					StreamUtils.getBytes(f.open(getStorage(), path))));
 		} catch (UnsupportedOperationException e) {
 			return new Pair<Errno, String>(Errno.ERROR_NOT_SUPPORTED, "");
 		} catch (IOException e) {
 			return new Pair<Errno, String>(Errno.ERROR_IO, e.toString());
 		}
 	}
 
 	private void setMyNodeURL(String myNodeURL) {
 		this.myNodeURL = myNodeURL;
 	}
 
 	public String getMyNodeURL() {
 		return myNodeURL;
 	}
 
 	public Pair<Errno, String> digest(String algo, DFileSpec path) {
 		DFile f = getVFS().resolve(path);
 		if (f == null)
 			return new Pair<Errno, String>(Errno.ERROR_FILE_NOT_FOUND, "");
 		try {
 			MessageDigest md;
 			try {
 				md = MessageDigest.getInstance(algo.toUpperCase());
 			} catch (NoSuchAlgorithmException e) {
 				return new Pair<Errno, String>(Errno.ERROR_ILLEGAL_ARGUMENT,
 						"Bad digest algorithm " + algo);
 			}
 			InputStream is = f.open(getStorage(), path);
 			byte[] buf = new byte[8192];
 			int len;
 			while ((len = is.read(buf)) > -1)
 				md.update(buf, 0, len);
 			return new Pair<Errno, String>(Errno.SUCCESS, Hex
 					.encodeHexString(md.digest()));
 		} catch (UnsupportedOperationException e) {
 			logger.error("Unsupported", e);
 			return new Pair<Errno, String>(Errno.ERROR_NOT_SUPPORTED, "");
 		} catch (IOException e) {
 			return new Pair<Errno, String>(Errno.ERROR_IO, e.toString());
 		}
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	private class NotHidden implements Predicate<DFile> {
 		private final DFile f;
 
 		public NotHidden(DFile f) {
 			this.f = f;
 		}
 		@Override
 		public boolean apply(DFile arg) {
 			return !arg.getName().startsWith(".") && arg != f
 			&& arg != f.getParent();
 		}
 	}
 
 	private void traverse(DFile f, Collection<DFile> list) {
 		if (f.isDirectory())
 			for (DFile child : Collections2.filter(f.getChildren().values(),
 					new NotHidden(f)))
 				traverse(child, list);
 		else
 			list.add(f);
 	}
 
 	public Pair<Errno, String> map(final String className, DFileSpec toWhat) {
 		DFile f = getVFS().resolve(toWhat);
 		final DFileSpec outSpec = new DFileSpec("/mapout");
 		final DFile dest = getVFS().resolve(outSpec);
 		if (!dest.exists())
			getVFS().mkdir(outSpec, new EveryonePermissions());
 		List<DFile> list = new ArrayList<DFile>();
 		StringBuilder result = new StringBuilder();
 		traverse(f, list);
 		Queue<NodeRPC> nodes = new LinkedList<NodeRPC>();
 		nodes.addAll(getClusterMembers().values());
 		try {
 			nodes.add(nodeRPCForURL(myNodeURL));
 		} catch (MalformedURLException e) {
 			/* ignore, we'll just not use ourselves */
 		}
 		ExecutorService e = Executors.newFixedThreadPool(50);
 		ExecutorCompletionService<String> jobs = new ExecutorCompletionService<String>(e);
 		for (final DFile file : list) {
 			final NodeRPC target = nodes.poll();
 			jobs.submit(new Callable<String>() {
 				@Override
 				public String call() throws Exception {
 					return target.mapFile(file, className, outSpec);
 				}
 			});
 			nodes.offer(target);
 		}
 
 		e.shutdown();
 		while (!e.isTerminated()) {
 			try {
 				result.append(jobs.take().get() + "\n");
 			} catch (InterruptedException ie) {
 				logger.warn("Someone interrupted me!", ie);
 			} catch (ExecutionException ee) {
 				result.append("Execution exception: " + ee.toString() + "\n");
 				logger.error("Execution exception", ee);
 			}
 		}
 		return new Pair<Errno, String>(Errno.SUCCESS, result.toString());
 	}
 
 	public String mapFile(DFile file, String partialClassName, DFileSpec dest) {
 		String className = "edu.berkeley.grippus.map." + partialClassName;
 		Class<?> mapClass;
 		try {
 			mapClass = Class.forName(className);
 		} catch (ClassNotFoundException e) {
 			return "No such class: " + className;
 		}
 		if (!FileMapper.class.isAssignableFrom(mapClass))
 			return "Not a mapper class: " + className;
 		FileMapper fm;
 		try {
 			fm = FileMapper.class.cast(mapClass.newInstance());
 		} catch (InstantiationException e) {
 			return "Internal error: " + e.toString();
 		} catch (IllegalAccessException e) {
 			return "Internal error: " + e.toString();
 		}
 		getVFS().sync();
 		return fm.execute(getVFS(), getStorage(), file, dest);
 	}
 
 	public Errno propogateData(Block b, String path) {
 		try {
 			if (!this.isMaster()) {
 				masterServer.updateMetadata(b, path);
 			}
 		} catch (HessianRuntimeException e) {
 			logger.error("Something went wrong with updating metadata");
 		}
 		return Errno.SUCCESS_TOPOLOGY_CHANGE;
 	}
 }
