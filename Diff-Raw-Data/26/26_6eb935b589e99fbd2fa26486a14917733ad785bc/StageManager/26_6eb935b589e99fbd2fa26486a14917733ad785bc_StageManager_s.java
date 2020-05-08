 package com.hopper.stage;
 
 import com.hopper.GlobalConfiguration;
 import com.hopper.lifecycle.LifecycleProxy;
 import com.hopper.server.ComponentManager;
 import com.hopper.server.ComponentManagerFactory;
 import com.hopper.sync.DataSyncThreadPool;
 
 import java.util.EnumMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 
 /**
  * Manages all stage and thread pools
  */
 public class StageManager extends LifecycleProxy {
 
     private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
 
     /**
      * Global configuration
      */
     private final GlobalConfiguration config = componentManager.getGlobalConfiguration();
 
     /**
      * Mappings between state and thread pool
      */
     private final EnumMap<Stage, ThreadPoolExecutor> states = new EnumMap<Stage, ThreadPoolExecutor>(Stage.class);
 
     /**
      * Register stage and pool
      */
     @Override
     protected void doInit() {
         states.put(Stage.S2S_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.S2S_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.RPC_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.RPC_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.CLIENT_BOSS, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.CLIENT_WORKER, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.SHUTDOWN_OUTGOING_CONNECTION, (ThreadPoolExecutor) Executors.newCachedThreadPool());
         states.put(Stage.SYNC, newDataSyncThreadPool());
     }
 
     @Override
     public String getInfo() {
         return "Stage manager";
     }
 
     public ThreadPoolExecutor getThreadPool(Stage stage) {
         return states.get(stage);
     }
 
     /**
      * Create thread pool for data synchronization
      */
     private ThreadPoolExecutor newDataSyncThreadPool() {
         ThreadPoolExecutor threadPool = new DataSyncThreadPool(config.getSyncThreadPoolCoreSize(),
                 config.getSyncThreadPoolMaxSize(), new LinkedBlockingQueue<Runnable>());
         return threadPool;
     }
 }
