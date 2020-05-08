 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane.server.config;
 
 import com.janrain.backplane.server.MessageProcessor;
 import com.janrain.backplane.server.dao.DaoFactory;
 import com.janrain.utils.BackplaneSystemProps;
 import com.janrain.cache.CachedL1;
 import com.janrain.commons.util.AwsUtility;
 import com.janrain.commons.util.InitSystemProps;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.CuratorFrameworkFactory;
 import com.netflix.curator.framework.recipes.leader.LeaderSelector;
 import com.netflix.curator.retry.ExponentialBackoffRetry;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.MetricName;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.context.annotation.Scope;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.TimeZone;
 import java.util.concurrent.*;
 
 
 /**
  * Holds configuration settings for the Backplane server
  * 
  * @author Jason Cowley, Johnny Bufu
  */
 @Scope(value="singleton")
 public class Backplane1Config {
 
     // - PUBLIC
 
     // http://fahdshariff.blogspot.ca/2010/08/dateformat-with-multiple-threads.html
     public static final ThreadLocal<DateFormat> ISO8601 = new ThreadLocal<DateFormat>() {
         @Override
         protected DateFormat initialValue() {
             return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") {{
                 setTimeZone(TimeZone.getTimeZone("GMT"));
             }};
         }
     };
 
     /**
 	 * @return the debugMode
 	 */
 	public boolean isDebugMode() {
         return Boolean.valueOf(cachedGet(BpServerConfig.Field.DEBUG_MODE));
 	}
 
     /**
      * @return the server default max message value per channel
      */
 
     public long getDefaultMaxMessageLimit() {
         Long max = Long.valueOf(cachedGet(BpServerConfig.Field.DEFAULT_MESSAGES_MAX));
         return max == null ? Backplane1Config.BP_MAX_MESSAGES_DEFAULT : max;
     }
 
     public Exception getDebugException(Exception e) {
         return isDebugMode() ? e: null;
     }
 
     public String getInstanceId() {
         return bpInstanceId;
     }
 
     public String getBuildVersion() {
         return buildProperties.getProperty(BUILD_VERSION_PROPERTY);
     }
 
     /**
      * Retrieve the server instance id Amazon assigned
      * @return
      */
 
     public static String getEC2InstanceId() {
         return EC2InstanceId;
     }
 
     // - PACKAGE
 
 
     Backplane1Config(String instanceId) {
         this.bpInstanceId = instanceId;
     }
 
     /**
      * Load system property
      * @param propParamName
      * @return
      */
 
     static String getAwsProp(String propParamName) {
         String result = System.getProperty(propParamName);
         if (StringUtils.isBlank(result)) {
             throw new RuntimeException("Required system property configuration missing: " + propParamName);
         }
         return result;
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(Backplane1Config.class);
 
     private static final String BUILD_PROPERTIES = "/build.properties";
     private static final String BUILD_VERSION_PROPERTY = "build.version";
     private static final String BP_CONFIG_ENTRY_NAME = "bpserverconfig";
     private static final long BP_MAX_MESSAGES_DEFAULT = 100;
     private static final Properties buildProperties = new Properties();
 
     private final String bpInstanceId;
     private final List<ExecutorService> backgroundServices = new ArrayList<ExecutorService>();
 
     // Amazon specific instance-id value
     private static String EC2InstanceId = AwsUtility.retrieveEC2InstanceId();
 
     private final com.yammer.metrics.core.Timer getMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "cleanup_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     @SuppressWarnings({"UnusedDeclaration"})
     private Backplane1Config() {
         this.bpInstanceId = getAwsProp(InitSystemProps.AWS_INSTANCE_ID);
         try {
             buildProperties.load(Backplane1Config.class.getResourceAsStream(BUILD_PROPERTIES));
         } catch (IOException e) {
             String err = "Error loading build properties from " + BUILD_PROPERTIES;
             logger.error(err, e);
             throw new RuntimeException(err, e);
         }
 
         logger.info("Configured Backplane Server instance: " + bpInstanceId);
     }
 
     private ScheduledExecutorService createMessageWorker() {
 
         logger.info("calling createMessageWorker()");
 
         final MessageProcessor messageProcessor = new MessageProcessor();
 
         ScheduledExecutorService messageWorkerTask = Executors.newScheduledThreadPool(1);
 
 //        messageWorkerTask.scheduleAtFixedRate(new Runnable() {
 //            @Override
 //            public void run() {
 //                logger.info("creating subscriber thread");
 //                messageProcessor.subscribe();
 //            }
 //        }, 0, 1, TimeUnit.MINUTES);
 
         messageWorkerTask.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                 logger.info("creating message cleanup thread");
                 messageProcessor.cleanupMessages();
             }
         }, 0, 1, TimeUnit.MINUTES);
 
         return messageWorkerTask;
 
     }
 
     @PostConstruct
     private void init() {
         backgroundServices.add(createMessageWorker());
 
         try {
             String zkServerConfig = System.getProperty(BackplaneSystemProps.ZOOKEEPER_SERVERS);
             if (StringUtils.isEmpty(zkServerConfig)) {
                 logger.error("Cannot find configuration entry for ZooKeeper server");
                 System.exit(1);
             }
             CuratorFramework client = CuratorFrameworkFactory.newClient(zkServerConfig, new ExponentialBackoffRetry(50, 20));
             client.start();
             LeaderSelector leaderSelector = new LeaderSelector(client, "/v1_worker", new MessageProcessor());
             leaderSelector.start();
         } catch (Exception e) {
             logger.error(e);
         }
     }
 
     @PreDestroy
     private void cleanup() {
         for (ExecutorService executor : backgroundServices) {
             try {
                 executor.shutdown();
                 if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                     logger.info("Background thread shutdown properly");
                 } else {
                     executor.shutdownNow();
                     if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                         logger.error("Background thread did not terminate");
                     }
                 }
             } catch (InterruptedException e) {
                 logger.error("error shutting down background service", e);
                 executor.shutdownNow();
                 Thread.currentThread().interrupt();
             }
         }
     }
 
     private String cachedGet(BpServerConfig.Field property) {
 
         BpServerConfig bpServerConfigCache = (BpServerConfig) CachedL1.getInstance().getObject(BackplaneSystemProps.BPSERVER_CONFIG_KEY);
         if (bpServerConfigCache == null) {
             // pull from db if not found in cache
            try {
                bpServerConfigCache = DaoFactory.getConfigDAO().get(BackplaneSystemProps.BPSERVER_CONFIG_KEY);
            } catch (Exception e) {
                bpServerConfigCache = null;
            }

             if (bpServerConfigCache == null) {
                 // no instance found in cache or the db, so let's use the default record
                 bpServerConfigCache = new BpServerConfig();
             }
             // add it to the L1 cache
             CachedL1.getInstance().setObject(BackplaneSystemProps.BPSERVER_CONFIG_KEY, -1, bpServerConfigCache);
         }
 
         return bpServerConfigCache.get(property);
 
     }
 
 
 }
