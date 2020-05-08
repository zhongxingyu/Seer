 package com.syfen.zookeeper;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.netflix.config.ConcurrentCompositeConfiguration;
 import com.netflix.config.ConfigurationManager;
 import com.netflix.config.DynamicWatchedConfiguration;
 import com.netflix.config.source.ZooKeeperConfigurationSource;
 import com.netflix.curator.ensemble.exhibitor.DefaultExhibitorRestClient;
 import com.netflix.curator.ensemble.exhibitor.ExhibitorEnsembleProvider;
 import com.netflix.curator.ensemble.exhibitor.Exhibitors;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.framework.imps.CuratorFrameworkState;
 import com.netflix.curator.retry.ExponentialBackoffRetry;
 import org.apache.commons.configuration.AbstractConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 
 /**
  * User: ToneD
  * Created: 25/08/13 12:24 PM
  */
 public class ZookeeperClientFactory {
 
     private static final Logger log = LoggerFactory.getLogger(ZookeeperClientFactory.class);
     private static final AbstractConfiguration config = ConfigurationManager.getConfigInstance();
     public static final Cache<String, ZookeeperClientCacheItem> cache = CacheBuilder.newBuilder().concurrencyLevel(64)
             .build();
 
     /**
      * Get a started ZK client
      */
     public static CuratorFramework getStartedZKClient(Collection<String> exhibitorList, String namespace) {
 
         String cacheKey = namespace + "-" + exhibitorList.toString();
         ZookeeperClientCacheItem cachedItem = cache.getIfPresent(cacheKey);
         if (cachedItem != null) {
             return cachedItem.client;
         }
         return createAndStartZKClient(exhibitorList, namespace);
     }
 
     /**
      * Initialize an Archaius Zookeeper Configuration from astarted ZK client
      */
     public static void initializeAndStartZkConfigSource(Collection<String> exhibitorList, String namespace, String zkConfigRootPath) throws Exception {
 
         // ZooKeeper Dynamic Override Properties
         CuratorFramework client = ZookeeperClientFactory.getStartedZKClient(exhibitorList, namespace);
 
         if (client.getState() != CuratorFrameworkState.STARTED) {
             throw new RuntimeException("ZooKeeper located at " + exhibitorList.toString() + " is not started.");
         }
 
         // Create Zookeeper configuration source
         ZooKeeperConfigurationSource zookeeperConfigSource = new ZooKeeperConfigurationSource(
                 client, zkConfigRootPath);
         zookeeperConfigSource.start();
 
         // Create new watched configuration
         DynamicWatchedConfiguration zookeeperDynamicConfig = new DynamicWatchedConfiguration(
                 zookeeperConfigSource);
 
         // insert ZK DynamicConfig into the 2nd spot
         ((ConcurrentCompositeConfiguration) config).addConfigurationAtIndex(
                 zookeeperDynamicConfig, "zk dynamic override", 1);
     }
 
     /**
      * Create and start a zkclient if needed
      */
     private synchronized static CuratorFramework createAndStartZKClient(Collection<String> exhibitorList, String namespace) {
 
         ZookeeperClientCacheItem cachedItem = cache.getIfPresent(exhibitorList.toString());
         if (cachedItem != null) {
             return cachedItem.client;
         }
 
         String zkConfigExhibitorPathClusterList = config.getString(Constants.ZK_CONFIG_EXHIBITOR_PATH_CLUSTER_LIST);
         Integer zkConfigExhibitorPort = config.getInt(Constants.ZK_CONFIG_EXHIBITOR_PORT);
         Integer zkConfigExhibitorPollInterval = config.getInt(Constants.ZK_CONFIG_EXHIBITOR_POLL_INTERVAL);
 
         // create ensemble provider
         Exhibitors exhibitors = new Exhibitors(exhibitorList, zkConfigExhibitorPort,
                 new ZookeeperBackupConnectionStringProvider());
         ExponentialBackoffRetry rp = new ExponentialBackoffRetry(1000, 3);
         ExhibitorEnsembleProvider ep = new ExhibitorEnsembleProvider(exhibitors, new DefaultExhibitorRestClient(),
                 zkConfigExhibitorPathClusterList, zkConfigExhibitorPollInterval, rp);
         try {
             ep.pollForInitialEnsemble();
         }
         catch (Exception e) {
            log.error(e.getMessage());
         }
 
         // create curator client
         CuratorFramework client = CuratorFrameworkFactory.builder().namespace(namespace).ensembleProvider(ep)
                 .retryPolicy(rp).build();
         client.start();
 
         String cacheKey = namespace + "-" + exhibitorList.toString();
         cache.put(cacheKey, new ZookeeperClientCacheItem(cacheKey, client));
 
         log.info("Created, started, and cached zk client [{}] for ensemble [{}]", client, exhibitorList.toString());
 
         return client;
     }
 }
