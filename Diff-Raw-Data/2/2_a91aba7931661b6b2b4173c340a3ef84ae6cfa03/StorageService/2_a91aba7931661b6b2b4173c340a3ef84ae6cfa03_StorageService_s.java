 package org.ogreg.ase4j;
 
 import org.ogreg.ostore.ObjectStore;
 import org.ogreg.ostore.ObjectStoreManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import gnu.cajo.invoke.Remote;
 import gnu.cajo.utils.ItemServer;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 /**
  * Association storage service entry point.
  *
  * @author  Gergely Kiss
  */
 public class StorageService {
     private static final Logger log = LoggerFactory.getLogger(StorageService.class);
 
     private AssociationStoreManager storeManager;
     private boolean running = false;
 
     public void start() throws Exception {
         running = true;
 
         log.info("Starting up storage...");
 
         // TODO PerformanceTimer - CJC maybe?
         long before = System.currentTimeMillis();
 
         // Reading configuration
         String dataPath = System.getProperty("dataDir", "data");
         String schemaPath = System.getProperty("schema", "schema.xml");
         int rmiPort = Integer.getInteger("rmiPort", 1198);
 
         File dataDir = new File(dataPath);
         File schemaFile = new File(schemaPath);
 
         // TODO check permissions?
         log.debug("Using data dir: {}", dataDir);
 
         // Initializing Cajo
         Remote.config(null, rmiPort, null, 0);
 
         // Initializing storage
         // TODO Load multiple configs?
         storeManager = new AssociationStoreManager();
         storeManager.add(schemaFile);
         storeManager.setDataDir(dataDir);
 
         // Publishing association storage
         log.debug("Configuring association storage");
         storeManager.configureAll();
 
         int assocStores = 0;
 
         for (Entry<String, AssociationStore<?, ?>> en :
             storeManager.getConfiguredStores().entrySet()) {
             String serviceLoc = "assocs/" + en.getKey();
 
             try {
                 ItemServer.bind(en.getValue(), serviceLoc);
                 assocStores++;
                 log.debug("Successfully initialized assoc store at: {}", serviceLoc);
             } catch (Exception e) {
                 log.error("Failed to initialize assoc store: {} ({})", en.getKey(),
                     e.getLocalizedMessage());
                 log.debug("Failure trace", e);
             }
         }
 
         log.info("Initialized {}/{} assoc stores successfully", assocStores,
             storeManager.getConfiguredStores().size());
 
         int groupedAssocStores = 0;
 
         for (Entry<String, GroupedAssociationStore<?, ?>> en :
             storeManager.getConfiguredGroupedStores().entrySet()) {
             String serviceLoc = "groupedAssocs/" + en.getKey();
 
             try {
                 ItemServer.bind(en.getValue(), serviceLoc);
                 groupedAssocStores++;
                 log.debug("Successfully initialized grouped assoc store at: {}", serviceLoc);
             } catch (Exception e) {
                 log.error("Failed to initialize grouped assoc store: {} ({})", en.getKey(),
                     e.getLocalizedMessage());
                 log.debug("Failure trace", e);
             }
         }
 
         log.info("Initialized {}/{} grouped assoc stores successfully", groupedAssocStores,
             storeManager.getConfiguredGroupedStores().size());
 
         // Publishing object storage
         log.debug("Configuring object storage");
 
         int objectStores = 0;
         Map<String, ObjectStore<?>> ostores = storeManager.getObjectManager().getObjectStores();
 
         for (Entry<String, ObjectStore<?>> en : ostores.entrySet()) {
             String id = en.getKey();
             String serviceLoc = "objects/" + id;
 
             try {
                 ItemServer.bind(en.getValue(), serviceLoc);
                 objectStores++;
                 log.debug("Successfully initialized object store at: {}", serviceLoc);
             } catch (Exception e) {
                 log.error("Failed to initialize object store: {} ({})", id,
                     e.getLocalizedMessage());
                 log.debug("Failure trace", e);
             }
         }
 
         // TODO PerformanceTimer
         long time = System.currentTimeMillis() - before;
         log.info("Initialized {}/{} object stores successfully", objectStores, ostores.size());
 
         log.info("Startup completed in {} ms, waiting for requests", time);
 
         // Adding shutdown hook (Ctrl+C)
         Runtime.getRuntime().addShutdownHook(new Thread() {
                 @Override public void run() {
                     shutdown();
                 }
             });
 
         // Starting console
         Thread console = new Thread("Console") {
                 @Override public void run() {
 
                     try {
                         System.out.println("Enter 'q' or Ctrl+C to quit");
 
                         while (running) {
                             int b = System.in.read();
 
                            if ((b < 0) || (b == 'q')) {
                                 shutdown();
                             }
                         }
                     } catch (IOException e) {
                         log.error("Failed to read from System.in");
                     }
                 }
             };
 
         console.setDaemon(true);
         console.start();
 
         while (running) {
             Thread.sleep(100);
         }
     }
 
     public synchronized void shutdown() {
 
         if (!running) {
             return;
         }
 
         // TODO PerformanceTimer
         long before = System.currentTimeMillis();
 
         log.debug("Shutting down assoc storage");
 
         Set<String> storeIds = new HashSet<String>(storeManager.getConfiguredStores().keySet());
 
         for (String id : storeIds) {
 
             try {
                 storeManager.flushStore(id);
                 log.debug("Successfully flushed assoc storage: {}", id);
             } catch (Exception e) {
                 log.error("Failed to flush: {} ({}), trying to close", id);
                 log.debug("Failure trace", e);
             }
 
             try {
                 storeManager.closeStore(id);
                 log.debug("Successfully closed assoc storage: {}", id);
             } catch (Exception e) {
                 log.error("Failed to close: {} ({})", id);
                 log.debug("Failure trace", e);
             }
         }
 
         log.debug("Shutting down object storage");
 
         ObjectStoreManager objectManager = storeManager.getObjectManager();
         Set<String> objectStoreIds = new HashSet<String>(objectManager.getObjectStores().keySet());
 
         for (String id : objectStoreIds) {
 
             try {
                 objectManager.flushStore(id);
                 log.debug("Successfully flushed object storage: {}", id);
             } catch (Exception e) {
                 log.error("Failed to flush: {} ({}), trying to close", id);
                 log.debug("Failure trace", e);
             }
 
             try {
                 objectManager.closeStore(id);
                 log.debug("Successfully closed object storage: {}", id);
             } catch (Exception e) {
                 log.error("Failed to close: {} ({})", id);
                 log.debug("Failure trace", e);
             }
         }
 
         // TODO PerformanceTimer
         long time = System.currentTimeMillis() - before;
         log.info("Shutdown completed in {} ms. Bye!", time);
 
         // Shutting down RMI
         Remote.shutdown();
 
         running = false;
     }
 
     public static void main(String[] args) {
 
         try {
             new StorageService().start();
         } catch (Throwable e) {
             log.error("Unexpected storage failure", e);
         }
     }
 }
