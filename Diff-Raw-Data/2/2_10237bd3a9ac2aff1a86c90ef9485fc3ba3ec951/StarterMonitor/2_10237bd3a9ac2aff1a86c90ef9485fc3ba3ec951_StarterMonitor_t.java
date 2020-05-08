 package ch.uzh.ddis.katts.monitoring;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.apache.zookeeper.ZooKeeper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.uzh.ddis.katts.RunXmlQueryLocally;
 import ch.uzh.ddis.katts.utils.Cluster;
 
 public class StarterMonitor {
 
 	private static StarterMonitor instance;
 	@SuppressWarnings("rawtypes")
 	private Map stormConfiguration;
 	private ZooKeeper zooKeeper;
 
 	public static final String KATTS_STARTING_TIME_ZK_PATH = "/katts_starting_time";
 	private Logger logger = LoggerFactory.getLogger(StarterMonitor.class);
 
 	private StarterMonitor(@SuppressWarnings("rawtypes") Map stormConf) {
 		stormConfiguration = stormConf;
 
 		try {
 			zooKeeper = Cluster.createZooKeeper(stormConfiguration);
 		} catch (IOException e) {
 			throw new RuntimeException("Can't create ZooKeeper instance for monitoring the start time.", e);
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static StarterMonitor getInstance(Map stormConf) {
 		if (instance == null) {
 			instance = new StarterMonitor(stormConf);
 		}
 
 		return instance;
 	}
 
 	public void start() {
 
 		if (!(Boolean) stormConfiguration.get(RunXmlQueryLocally.RUN_TOPOLOGY_LOCALLY_CONFIG_KEY)) {
 
 			try {
 				zooKeeper.create(KATTS_STARTING_TIME_ZK_PATH, Long.toString(System.currentTimeMillis()).getBytes(),
 						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 			} catch (KeeperException e) {
				if (e.code().equals(KeeperException.Code.NODEEXISTS)) {
 					logger.info("The starting time entry was set already by another instance.");
 				} else {
 					throw new RuntimeException("Can't create the starting time ZooKeeper entry.", e);
 				}
 
 			} catch (InterruptedException e) {
 				throw new RuntimeException(
 						"Can't create the starting time ZooKeeper entry, because the thread was interrupted.", e);
 			}
 
 		}
 	}
 
 }
