 package ar.uba.fi.tppro.core.service;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.util.Collection;
 
 import org.apache.thrift.server.TServer;
 import org.apache.thrift.server.TThreadPoolServer;
 import org.apache.thrift.transport.TServerSocket;
 import org.apache.thrift.transport.TServerTransport;
 import org.apache.zookeeper.CreateMode;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.retry.RetryOneTime;
 
 import ar.uba.fi.tppro.core.index.IndexNodeDescriptor;
 import ar.uba.fi.tppro.core.index.IndexPartitionsGroup;
 import ar.uba.fi.tppro.core.index.RemoteIndexNodeDescriptor;
 import ar.uba.fi.tppro.core.index.lock.LockManager;
import ar.uba.fi.tppro.core.index.lock.NullLockManager;
 import ar.uba.fi.tppro.core.index.versionTracker.ShardVersionTracker;
 import ar.uba.fi.tppro.core.index.versionTracker.ZkShardVersionTracker;
 import ar.uba.fi.tppro.core.service.thrift.IndexNode;
 import ar.uba.fi.tppro.partition.PartitionResolver;
 import ar.uba.fi.tppro.partition.ZookeeperPartitionResolver;
 import ar.uba.fi.tppro.util.NetworkUtils;
 
 public class IndexServer implements Runnable {
 
 	final static Logger logger = LoggerFactory.getLogger(IndexPartitionsGroup.class);
 
 	protected int port;
 	protected File dataDir;
 	protected IndexPartitionsGroup handler;
 	protected IndexNode.Processor<IndexNode.Iface> processor;
 	protected TServer server;
 	
 	protected static CuratorFramework curator;
 
 	public static void main(String[] args) throws NumberFormatException,
 			Exception {
 
 		String port = System.getProperty("port", "9090");
 		String dataDir = System.getProperty("dataDir", "data");
 		String zookeeperHost = System.getProperty("zookeeper", "localhost:2181");
 		
 		if(zookeeperHost == null){
 			System.out.println("Zookeeper server not specified. Specify one with -Dzookeeper");
 			return;
 		}
 		
 		curator = createZookeeperClient(zookeeperHost);
 		curator.start();
 		
 		logger.info("Registering node in zookeeper");
 		String localhostname = java.net.InetAddress.getLocalHost().getHostAddress();
 	
 		curator.create().creatingParentsIfNeeded()
 		.withMode(CreateMode.EPHEMERAL)
 		.forPath("/cluster/" + localhostname + "_" + port, null);
 		
 		PartitionResolver partitionResolver = new ZookeeperPartitionResolver(curator);
 		ShardVersionTracker versionTracker = new ZkShardVersionTracker(curator);
		LockManager lockManager = new NullLockManager();
 		
 		new Thread(new IndexServer(Integer.parseInt(port), new File(dataDir), partitionResolver, versionTracker, lockManager))
 				.start();
 
 	}
 
 	private static CuratorFramework createZookeeperClient(String zookeeperHost) {
 		return CuratorFrameworkFactory.newClient(zookeeperHost, new RetryOneTime(1));
 	}
 
 	public IndexServer(int port, File dataDir, PartitionResolver partitionResolver, ShardVersionTracker versionTracker, LockManager lockManager) throws Exception {
 		this.port = port;
 		this.dataDir = dataDir;
 
 		Collection<InetAddress> ips = NetworkUtils.getAllLocalIPs();
 		if (ips.size() == 0) {
 			throw new Exception("Could not retrieve the local ip");
 		}
 		String address = ips.iterator().next().getHostAddress();
 		IndexNodeDescriptor localNodeDescriptor = new RemoteIndexNodeDescriptor(
 				address, port);
 		
 		
 		this.handler = new IndexPartitionsGroup(localNodeDescriptor, partitionResolver, versionTracker, lockManager);
 		this.handler.open(this.dataDir, true);
 	}
 
 	public void stop() {
 		logger.info("INDEX CORE: Stoping server");
 		if (server != null) {
 			server.stop();
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 
 			if (!this.dataDir.exists()) {
 				this.dataDir.mkdir();
 			}
 
 			processor = new IndexNode.Processor<IndexNode.Iface>(handler);
 
 			Server webServer = new Server(this.port + 1);
 			ResourceHandler resource_handler = new ResourceHandler();
 			resource_handler.setDirectoriesListed(true);
 			resource_handler.setWelcomeFiles(new String[] { "index.html" });
 			resource_handler.setResourceBase(this.dataDir.getAbsolutePath());
 
 			HandlerList handlers = new HandlerList();
 			handlers.setHandlers(new Handler[] { resource_handler,
 					new DefaultHandler() });
 			webServer.setHandler(handlers);
 
 			logger.info("Starting http server on port port " + (this.port + 1)
 					+ "...");
 			webServer.start();
 
 			TServerTransport serverTransport = new TServerSocket(this.port);
 			server = new TThreadPoolServer(new TThreadPoolServer.Args(
 					serverTransport).processor(processor));
 		
 
 			logger.info("Starting the Index server on port " + this.port
 					+ "...");
 			server.serve();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public IndexPartitionsGroup getHandler() {
 		return handler;
 	}
 }
