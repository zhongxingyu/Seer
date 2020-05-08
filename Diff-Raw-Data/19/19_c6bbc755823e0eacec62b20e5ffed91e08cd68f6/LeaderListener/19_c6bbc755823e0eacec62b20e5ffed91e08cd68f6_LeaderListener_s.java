 package com.abiquo.curatortest.listener;
 
 import static java.lang.Thread.currentThread;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.apache.log4j.Logger;
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher.Event.EventType;
 
 import com.netflix.curator.RetryLoop;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.framework.api.CuratorWatcher;
 import com.netflix.curator.framework.recipes.leader.LeaderSelector;
 import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
 import com.netflix.curator.framework.state.ConnectionState;
 import com.netflix.curator.retry.RetryNTimes;
 
 public class LeaderListener implements ServletContextListener, LeaderSelectorListener,
     CuratorWatcher
 {
     protected final static Logger LOGGER = Logger.getLogger(LeaderListener.class);
 
     /** Zk-connection to server/s cluster. */
     private final static String ZK_SERVER = System.getProperty("zk.serverConnection",
         "localhost:2181");
 
     private static String getId()
     {
         try
         {
             String hostname = InetAddress.getLocalHost().toString();
            String bindport = System.getProperty("jetty.port","8080");
            return String.format("%s:%s", hostname, bindport); 
         }
         catch (UnknownHostException e)
         {
             return "hostname not configured";
         }
     }
    
     /** Zk-node used on the ''leaderSelector'' to synch participants. */
     private final static String LEADER_PATH = "/test-leader";
 
     /** Zk-recipe to select one participant in the cluster. @see {@link LeaderSelectorListener}. */
     private LeaderSelector leaderSelector;
 
     /** Zk-client connected to the cluster. */
     private CuratorFramework curatorClient;
 
     /** Test consumer. */
     private final Consumer consumer = new Consumer();
 
     public void contextInitialized(final ServletContextEvent sce)
     {
         try
         {
             startZookeeper();
         }
         catch (Exception e)
         {
             LOGGER.error("Won't start. Not connection to zk server at " + ZK_SERVER, e);
             throw new RuntimeException(e);
         }
     }
    
     public void contextDestroyed(final ServletContextEvent arg0)
     {
         if (leaderSelector.hasLeadership())
         {
             LOGGER.info("Leader shutdown");
             consumer.stop();
         }
 
         leaderSelector.close();
     }
 
     /** Connects to ZK-Server and adds as participant to {@link LeaderSelector} cluster. */
     protected void startZookeeper() throws Exception
     {
         curatorClient =
             CuratorFrameworkFactory.newClient(ZK_SERVER, 15000, 10000, new RetryNTimes(3, 1000));
         curatorClient.start();
 
         LOGGER.info("Connected to " + ZK_SERVER);
 
         leaderSelector = new LeaderSelector(curatorClient, LEADER_PATH, this);
         leaderSelector.setId(getId());
         leaderSelector.start();
     }
 
     /**
      * Only leader has the consumers started. This method should only return when leadership is
      * being relinquished.
      */
     public void takeLeadership(final CuratorFramework client) throws Exception
     {
         LOGGER.info("Current instance is the leader");
 
         try
         {
             consumer.start();
         }
         catch (Exception e)
         {
             LOGGER.error("Can't start consumers", e);
             throw e; // revoque leadership
         }
 
         try
         {
             // hold the leadership
             currentThread().join();
         }
         catch (InterruptedException e)
         {
             e.printStackTrace();
             // expected during ''contextDestroy''
         }
 
         // if interrupted by a ''stateChanged'' the handler will check if require
         // ''shutdownRegisteredConsumers''
         LOGGER.info("Current instance no longer the leader");
     }
 


     /**
      * If the SUSPENDED state is reported, the instance must assume that it might no longer be the
      * leader until it receives a RECONNECTED state. If the LOST state is reported, the instance is
      * no longer the leader and its takeLeadership method should exit.
      */
     public void stateChanged(final CuratorFramework client, final ConnectionState newState)
     {
         LOGGER.info(String.format("%s - %s", newState.name(), LEADER_PATH));
 
         switch (newState)
         {
             case SUSPENDED:
                 if (leaderSelector.hasLeadership())
                 {
                     LOGGER.info("Leader suspended. Shutting down consumer");
                     consumer.stop();
                 }
                 break;
             case RECONNECTED:
                 // theory leaderSelector.requeue();
                 // https://github.com/Netflix/curator/issues/24
                 // The method #process will create a new leaderSelector instance. Once is
                 // reconnected, watch again
                 LOGGER.info("Processing Zookeeper reconnect");
                 break;
 
             case CONNECTED:
                 LOGGER.info("Processing Zookeeper connect");
                 watchItself();
                 break;
 
             case LOST:
                 // already disconnected by SUSPENDED state
                 break;
         }
     }
 
     /**
      * Process the NodeDeleted event.
      */
     public void process(final WatchedEvent event) throws Exception
     {
         if (event.getType() == EventType.NodeDeleted)
         {
             LOGGER.info("Processing deletion of Zookeeper path " + event.getPath());
 
             if (leaderSelector.hasLeadership())
             {
                 LOGGER.info("Leader deleted. Shutting down consumer");
                 consumer.stop();
             }
 
             leaderSelector.close();
 
             // reinitialize the leader selector
             leaderSelector = new LeaderSelector(curatorClient, LEADER_PATH, this);
             leaderSelector.setId(getId());
             leaderSelector.start();
 
             LOGGER.info("Starting Leader selector instance");
         }
     }
 
     private void watchItself()
     {
         // Due the asynchronous nature of zookeeper, the getChildren can raise an
         // exception because the last znode could not be yet created when we are
         // looking for it. So the best solution is use the RetryLoop as Curator
         // recommends.
         // (Retry Policy is configured at initialization time)
         RetryLoop rl = curatorClient.getZookeeperClient().newRetryLoop();
         List<String> child;
         int i = 0;
 
         while (rl.shouldContinue())
         {
             LOGGER.info("Trying to put a watcher in the last node created. (" + i + " attempts )");
             try
             {
                 Thread.sleep(3000);
 
                 child = curatorClient.getChildren().forPath(LEADER_PATH);
 
                 Collections.sort(child, new Comparator<String>()
                 {
                     // names of the znodes are like 'generateduuid'-lock-seq
                     // we need to compare only the seq
                     public int compare(final String o1, final String o2)
                     {
 
                         Integer seq1 = Integer.valueOf(o1.substring(o1.indexOf("lock-") + 5));
                         Integer seq2 = Integer.valueOf(o2.substring(o2.indexOf("lock-") + 5));
 
                         // we want ordered descending
                         return seq2.compareTo(seq1);
 
                     }
                 });
 
                 String path = LEADER_PATH + "/" + child.get(0);
                 LOGGER.info("Starting watcher for path " + path);
                 curatorClient.checkExists().usingWatcher(this).forPath(path);
 
                 rl.markComplete();
             }
             catch (Exception e)
             {
                 try
                 {
                     LOGGER.warn("Node could not execute the 'watchItself' in Zookeeper");
                     rl.takeException(e);
                 }
                 catch (Exception e1)
                 {
                     LOGGER.error("Could not Watch 'itself' node.");
                 }
             }
         }
 
     }
 }
