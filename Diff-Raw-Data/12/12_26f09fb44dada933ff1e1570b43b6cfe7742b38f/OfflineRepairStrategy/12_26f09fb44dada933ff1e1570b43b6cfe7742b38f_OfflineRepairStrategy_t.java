 package com.feedly.cassandra.dao;
 
 import java.lang.management.ManagementFactory;
 import java.util.Collection;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.management.MBeanServer;
 
 import me.prettyprint.hector.api.Keyspace;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.feedly.cassandra.entity.EntityMetadata;
 import com.feedly.cassandra.entity.IndexMetadata;
 
 /**
  * corrects index "offline", i.e. asynchronously. A thread pool is configured to handle the updates. If the thread pool gets overwhelmed,
  * updates are simply dropped.
  * 
  * @author kireet
  */
 public class OfflineRepairStrategy implements IStaleIndexValueStrategy
 {
     private static final Logger _logger = LoggerFactory.getLogger(OfflineRepairStrategy.class.getName());
     private static final AtomicInteger _threadId = new AtomicInteger();
     private InlineRepairStrategy _strategy;
     private int _threadCount = 1;
     private int _maxQueueSize = 1000;
     private ThreadPoolExecutor _executor;
     
     
     public void init()
     {
         LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(_maxQueueSize);
         _strategy = new InlineRepairStrategy();
         _strategy.init();
         
         final OfflineRepairStrategyMonitor monitor = new OfflineRepairStrategyMonitor(_strategy.stats(), queue);
         _executor = new ThreadPoolExecutor(1, _threadCount, 10, TimeUnit.SECONDS,
                                            queue,
                                            new ThreadFactory()
                                            {
                                                public Thread newThread(Runnable r)
                                                {
                                                    return new Thread(r, "offline-repair-strategy-" + _threadId.incrementAndGet());
                                                }
                                            },
                                            new RejectedExecutionHandler()
                                            {
                                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
                                                {
                                                    _logger.warn("repair queue full, dropping update");
                                                    monitor.rejectedExecution();
                                                }
                                            });
         
 
         try 
         {
             MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
             mbs.registerMBean(monitor, MBeanUtils.mBeanName(this, null, "repairStats"));
             _logger.info("monitoring registration complete for {}", getClass().getSimpleName());
         } 
         catch(Exception e) 
         {
             _logger.warn("error registering mbeans", e);
         }
 
     }
     
     public void destroy()
     {
         _executor.shutdown();
         try
         {
             _executor.awaitTermination(60, TimeUnit.SECONDS);
         }
         catch(Exception ex)
         {
             _logger.warn("repair queue not emptied after 60 seconds, continuing system shutdown");
             _executor.shutdownNow();
         }
         
         try 
         {
             MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
             mbs.unregisterMBean(MBeanUtils.mBeanName(this, null, "repairStats"));
            _logger.info("monitoring unregistration complete for {}", getClass().getSimpleName());
         } 
         catch(Exception e) 
         {
             _logger.warn("error unregistering mbeans", e);
         }
     }
 
     /**
      * set the thread pool size.
      * @param threadCount
      */
     public void setThreadCount(int threadCount)
     {
         _threadCount = threadCount;
     }
 
     /**
      * set the thread pool queue size. If the queue is full, repair operations are dropped.
      * @param queueSize
      */
     public void setMaxQueueSize(int queueSize)
     {
         _maxQueueSize = queueSize;
     }
 
 
     @Override
     public void handle(final EntityMetadata<?> entity, final IndexMetadata index, final Keyspace keyspace, final Collection<StaleIndexValue> values)
     {
         try
         {
             _executor.submit(
                              new Runnable()
                              {
                                  @Override
                                  public void run()
                                  {
                                      _strategy.handle(entity, index, keyspace, values);
                                  }
                              });
         }
         catch(RejectedExecutionException ree)
         {
             _logger.warn("system shutting down, dropping repair update");
         }
     }
 }
