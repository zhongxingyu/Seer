 package org.apache.zookeeper.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import org.apache.log4j.Logger;
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher;
 import org.apache.zookeeper.ZooKeeper;
 import org.apache.zookeeper.ZooKeeperMain;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
 
 public class ServerJoinMain implements Watcher {
     private static final Logger LOG = Logger.getLogger(QuorumPeerConfig.class);
     
     public static void main(String[] args) {
         	new ServerJoinMain(args);
     }
     
     public void process(WatchedEvent event) {
     	LOG.info("WATCHER::");
     	LOG.info(event.toString());
     }
         
     public ServerJoinMain(String[] args) {
     	if (args.length < 2) {
     		LOG.fatal("Missing parameters.");
     		LOG.info("The correct parameters are:");
     		LOG.info("Server config file.");
     		LOG.info("Server:port to connect.");
     		System.exit(1);
     	}
     	String[] remote_server_full = args[1].split(":");
     	String remote_server = remote_server_full[0];
     	int remote_port = Integer.parseInt(remote_server_full[1]);
     	String server = null;
     	int port1 = 0, port2 = 0;
 		ServerHostHash serverHost = null;
     	
     	try {
     		//Create config file
     		BufferedReader in = new BufferedReader(new FileReader(args[0]));
     		BufferedWriter out = new BufferedWriter(new FileWriter(args[0] + "_dyn"));
     		String line;
     		while (in.ready()) {
     			line = in.readLine();
     			System.out.println(line);
     			if (line.startsWith("dynserver")) {
     				String dynserver[] = line.split("=")[1].split(":");
     				server = dynserver[0];
     				port1 = Integer.parseInt(dynserver[1]);
     				port2 = Integer.parseInt(dynserver[2]);
     				serverHost = new ServerHostHash(server, port1);
     			} else
    				out.write(line + "\n");
     		}
     		
     		if (serverHost == null) {
     			LOG.fatal("dynserver hostname and ports not informed in config file.");
     			System.exit(1);
     		}
 
     		//Add himself to ServerList
     		String serverListPath = "/ServerList";
     		ZooKeeper zk = new ZooKeeper(remote_server, remote_port, this);
     		if (zk.exists(serverListPath, false) == null) {
     			zk.create(serverListPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
     		}
     		if (zk.exists(serverListPath + "/" + serverHost.hashCode(), false) != null) {
     			LOG.fatal("This hostname:port is already in zookeeper cluster");
     			System.exit(1);
     		}
     		else {
     			zk.create(serverListPath + "/" + serverHost.hashCode(), (server + ":" + port1 + ":" + port2).getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
     		}
     		
     		//finish config file
     		out.close();
     	} catch (Exception e) {
             LOG.fatal("Error. Exiting abnormally", e);
             System.exit(1);
     	}
     }
 }
