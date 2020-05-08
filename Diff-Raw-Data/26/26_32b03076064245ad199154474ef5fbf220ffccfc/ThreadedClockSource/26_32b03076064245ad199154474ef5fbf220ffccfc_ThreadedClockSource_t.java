 package org.commonjava.shelflife.clock;
 
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.PreDestroy;
 import javax.enterprise.inject.Alternative;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.commonjava.cdi.util.weft.ExecutorConfig;
 import org.commonjava.cdi.util.weft.StoppableRunnable;
 import org.commonjava.shelflife.ExpirationManager;
 import org.commonjava.shelflife.ExpirationManagerException;
 import org.commonjava.util.logging.Logger;
 
 @Alternative
 @Singleton
 public class ThreadedClockSource
     implements ExpirationClockSource
 {
     private final Logger logger = new Logger( getClass() );
 
     @Inject
     @ExecutorConfig( threads = 1, priority = 9, daemon = true, named = "shelflife-clock" )
     private ScheduledExecutorService executor;
 
     private Clock clock;
 
     private long period;
 
     public ThreadedClockSource()
     {
         this.period = ExpirationManager.NEXT_EXPIRATION_BATCH_OFFSET;
     }
 
     public ThreadedClockSource( final ScheduledExecutorService executor, final long period )
     {
         this.executor = executor;
         this.period = period;
     }
 
     public void setPeriod( final long period )
     {
         logger.debug( "\n\n\n\nCLOCK PERIOD RESET TO: %d ms", period );
         this.period = period;
     }
 
     public long getPeriod()
     {
         return period;
     }
 
     @Override
     public void start( final ExpirationManager manager )
         throws ExpirationManagerException
     {
         if ( clock != null )
         {
             throw new RuntimeException( "Cannot start with expiration manager: " + manager + ". This clock source is already started!" );
         }
 
         logger.debug( "Starting clock for manager: %s, period: %s", manager, period );
 
         clock = new Clock( manager );
         executor.scheduleAtFixedRate( clock, 0, period, TimeUnit.MILLISECONDS );
     }
 
     private final class Clock
         extends StoppableRunnable
     {
         private final Logger logger = new Logger( getClass() );
 
         private final ExpirationManager manager;
 
         private Clock( final ExpirationManager manager )
         {
             this.manager = manager;
         }
 
         @Override
         protected void doExecute()
         {
             logger.debug( "Clearing expired from: %s", manager );
             manager.clearExpired();
         }
     }
 
     @Override
     @PreDestroy
     public void stop()
     {
         clock.stop();
        executor.shutdown();
     }
 
 }
