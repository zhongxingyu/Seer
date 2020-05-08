 package fi.iki.mkuokkanen.seda;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import fi.iki.mkuokkanen.seda.api.WebsocketModule;
 import fi.iki.mkuokkanen.seda.keyStore.StorageModule;
 import fi.iki.mkuokkanen.seda.queue.QueueModule;
import fi.iki.mkuokkanen.seda.timed.SchedulerModule;
 
 /**
  * Main Application to start server.
  */
 public class ServerMain {
 
     private static Logger logger = LoggerFactory.getLogger(ServerMain.class);
 
    private static Injector injector;

     public static void main(String[] args) {
         logger.info("Starting application");
 
        injector = Guice.createInjector(
                 new WebsocketModule(),
                 new QueueModule(),
                new StorageModule(),
                new SchedulerModule()
                 );
 
         ServerApp api = injector.getInstance(ServerApp.class);
         
         api.start();
 
         logger.info("Finished starting");
     }
 
 }
