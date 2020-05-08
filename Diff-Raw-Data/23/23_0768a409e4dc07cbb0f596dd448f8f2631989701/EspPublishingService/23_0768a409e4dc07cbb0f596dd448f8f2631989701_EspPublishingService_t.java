 package pegasus.esp;
 
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.client.EventManager;
 
 public class EspPublishingService implements PublishingService, Broker {
 
     private static final Logger LOG = LoggerFactory.getLogger(EspPublishingService.class);
 
     private static final int PERIOD = 1000;
     private static final TimeUnit UNIT = TimeUnit.MILLISECONDS;
     private static final int POOL_SIZE = 10;
 
     private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);
    private List<Publisher> publishers = new ArrayList<Publisher>();
     private EventManager eventManager;
     private boolean started = false;
 
     public EspPublishingService() {
 
     }
 
     @Override
     public void addPublisher(Publisher publisher) {
         publisher.setBroker(this);
         publishers.add(publisher);
         if (started) {
             schedule(publisher);
         }
     }
 
     @Override
     public void addPublishers(Collection<Publisher> publishers) {
         for (Publisher publisher : publishers) {
             addPublisher(publisher);
         }
     }
 
     @Override
     public void start() {
 
         LOG.info("EspPublishingService starting...");
 
         started = true;
        
        if(publishers.size() > 0){
	        int delay = 0;
	        int intervalBetweenMonitors = PERIOD / publishers.size();
	
	        for (Publisher publisher : publishers) {
	            schedule(publisher, delay);
	            delay += intervalBetweenMonitors;
	        }
         }
        
         LOG.info("EspPublishingService started.");
     }
     
     private void schedule(Publisher publisher) {
         schedule(publisher, 0);
     }
     
     private void schedule(Publisher publisher, int delay) {
         scheduler.scheduleAtFixedRate(publisher, delay, PERIOD, UNIT);
     }
 
     @Override
     public void stop() {
 
         LOG.info("EspPublishingService stopping...");
 
         if (scheduler != null && !scheduler.isShutdown()) {
             scheduler.shutdownNow();
             scheduler = null;
             eventManager = null;
         }
         started = false;
         
         LOG.info("EspPublishingService stopped.");
     }
 
     @Override
     public synchronized void publish(Object message) {
         eventManager.publish(message);
     }
 
     public void setEventManager(EventManager eventManager) {
         this.eventManager = eventManager;
     }
 
     @Override
     protected void finalize() throws Throwable {
         try {
             stop();
         } finally {
             super.finalize();
         }
     }
 }
