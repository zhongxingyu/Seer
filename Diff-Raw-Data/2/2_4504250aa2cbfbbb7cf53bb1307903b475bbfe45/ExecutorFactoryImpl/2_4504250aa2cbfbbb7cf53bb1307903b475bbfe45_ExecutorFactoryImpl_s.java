 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.executors.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.servicemix.executors.Executor;
 import org.apache.servicemix.executors.ExecutorFactory;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ArrayBlockingQueue;
 import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
 import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
 import edu.emory.mathcs.backport.java.util.concurrent.RejectedExecutionHandler;
 import edu.emory.mathcs.backport.java.util.concurrent.SynchronousQueue;
 import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;
 import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
 import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Default implementation of the ExecutorFactory.
  * 
 * Configuration can be done heierachically.
  * When an executor is created with an id of <code>foo.bar</code>,
  * the factory will look for a configuration in the following
  * way:
  * <ul>
  *    <li>configs.get("foo.bar")</li>
  *    <li>configs.get("foo")</li>
  *    <li>defaultConfig</li>
  * </ul>
  * 
  * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
  */
 public class ExecutorFactoryImpl implements ExecutorFactory {
 
     private ExecutorConfig defaultConfig = new ExecutorConfig();
     private Map configs = new HashMap();
     
     public Executor createExecutor(String id) {
         ExecutorConfig config = getConfig(id);
         return new ExecutorImpl(createService(id, config), 
                                 config.getShutdownDelay());
     }
     
     protected ExecutorConfig getConfig(String id) {
         ExecutorConfig config = null;
         if (configs != null) {
             config = (ExecutorConfig) configs.get(id);
             while (config == null && id.indexOf('.') > 0) {
                 id = id.substring(0, id.lastIndexOf('.'));
                 config = (ExecutorConfig) configs.get(id);
             }
         }
         if (config == null) {
             config = defaultConfig;
         }
         return config;
     }
     
     protected ThreadPoolExecutor createService(String id, ExecutorConfig config) {
         if (config.getQueueSize() != 0 && config.getCorePoolSize() == 0) {
             throw new IllegalArgumentException("CorePoolSize must be > 0 when using a capacity queue");
         }
         BlockingQueue queue;
         if (config.getQueueSize() == 0) {
             queue = new SynchronousQueue();
         } else if (config.getQueueSize() < 0 || config.getQueueSize() == Integer.MAX_VALUE) {
             queue = new LinkedBlockingQueue();
         } else {
             queue = new ArrayBlockingQueue(config.getQueueSize());
         }
         ThreadFactory factory = new DefaultThreadFactory(id,
                                                          config.isThreadDaemon(), 
                                                          config.getThreadPriority());
         RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
         ThreadPoolExecutor service = new ThreadPoolExecutor(
                         config.getCorePoolSize(),
                         config.getMaximumPoolSize() < 0 ? Integer.MAX_VALUE : config.getMaximumPoolSize(),
                         config.getKeepAliveTime(),
                         TimeUnit.MILLISECONDS,
                         queue,
                         factory,
                         handler);
         if (config.isAllowCoreThreadsTimeout()) {
             service.allowCoreThreadTimeOut(true);
         }
         return service;
     }
 
     /**
      * The default thread factory
      */
     static class DefaultThreadFactory implements ThreadFactory {
         final ThreadGroup group;
         final AtomicInteger threadNumber = new AtomicInteger(1);
         final String namePrefix;
         final boolean daemon;
         final int priority;
 
         DefaultThreadFactory(String id, boolean daemon, int priority) {
             SecurityManager s = System.getSecurityManager();
             group = (s != null)? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
             namePrefix = "pool-" +
                           id +
                          "-thread-";
             this.daemon = daemon;
             this.priority = priority;
         }
 
         public Thread newThread(Runnable r) {
             Thread t = new Thread(group, r,
                                   namePrefix + threadNumber.getAndIncrement(),
                                   0);
             if (t.isDaemon() != daemon)
                 t.setDaemon(daemon);
             if (t.getPriority() != priority)
                 t.setPriority(priority);
             return t;
         }
     }
 
     /**
      * @return the configs
      */
     public Map getConfigs() {
         return configs;
     }
 
     /**
      * @param configs the configs to set
      */
     public void setConfigs(Map configs) {
         this.configs = configs;
     }
 
     /**
      * @return the defaultConfig
      */
     public ExecutorConfig getDefaultConfig() {
         return defaultConfig;
     }
 
     /**
      * @param defaultConfig the defaultConfig to set
      */
     public void setDefaultConfig(ExecutorConfig defaultConfig) {
         this.defaultConfig = defaultConfig;
     }
 
 }
