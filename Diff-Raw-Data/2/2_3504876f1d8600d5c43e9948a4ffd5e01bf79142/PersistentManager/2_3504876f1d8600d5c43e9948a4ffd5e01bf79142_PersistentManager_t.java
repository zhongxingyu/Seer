 package nl.astraeus.persistence;
 
 import org.prevayler.Prevayler;
 import org.prevayler.PrevaylerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: rnentjes
  * Date: 7/24/11
  * Time: 7:24 PM
  */
 public class PersistentManager {
     private final static Logger logger = LoggerFactory.getLogger(PersistentManager.class);
 
     public final static String SAFEMODE            = "safemode";
     public final static String AUTOCOMMIT          = "autocommit";
     public final static String DATA_DIRECTORY      = "dataDirectory";
     public final static String FILE_AGE_THRESHOLD  = "fileAgeThreshold";
     public final static String FILE_SIZE_THRESHOLD = "fileSizeThreshold";
 
     private final static PersistentManager INSTANCE = new PersistentManager();
 
     private ThreadLocal<PersistentTransaction> transactions = new ThreadLocal<PersistentTransaction>();
 
     public static PersistentManager get() {
         return INSTANCE;
     }
 
     private Prevayler prevayler;
 
     // properties
     private boolean autocommit     = false;
     private boolean safemode       = true;
     private String dataDirectory   = "persistent";
     private long fileAgeThreshold  = TimeUnit.MINUTES.toMillis(1);
     private long fileSizeThreshold = 10*1024L*1024L;
 
     private boolean started = false;
     private boolean nodes = false;
 
     public PersistentManager() {
         started = true;
 
         try {
             if ("false".equals(System.getProperty(SAFEMODE))) {
                 safemode = false;
             }
 
             if ("true".equals(System.getProperty(AUTOCOMMIT))) {
                 autocommit = true;
             }
 
             if (System.getProperty(DATA_DIRECTORY) != null) {
                 dataDirectory = System.getProperty(DATA_DIRECTORY);
             }
 
             if (System.getProperty(FILE_AGE_THRESHOLD) != null) {
                 fileAgeThreshold = Long.parseLong(System.getProperty(FILE_AGE_THRESHOLD));
             }
 
             if (System.getProperty(FILE_SIZE_THRESHOLD) != null) {
                 fileSizeThreshold = Long.parseLong(System.getProperty(FILE_SIZE_THRESHOLD));
             }
 
             try {
                 SimpleNodeManager.get().init(
                         System.getProperty("simple.node.ip"),
                         Integer.parseInt(System.getProperty("simple.node.port")),
                         Integer.parseInt(System.getProperty("simple.node.divider")),
                         Integer.parseInt(System.getProperty("simple.node.remainder")));
 
                 nodes = true;
             } catch (NumberFormatException e) {
                 logger.warn(e.getMessage(), e);
             } catch (IllegalStateException e) {
                 logger.warn(e.getMessage(), e);
             }
 
             PrevaylerFactory factory = new PrevaylerFactory();
 
             factory.configureJournalFileAgeThreshold(fileAgeThreshold);
             factory.configureJournalFileSizeThreshold(fileSizeThreshold);
             factory.configurePrevalentSystem(new PersistentObjectStore());
             factory.configurePrevalenceDirectory(dataDirectory);
 
             //factory.configureJournalSerializer("journal", new SimpleJournalSerializer());
             //factory.configureSnapshotSerializer("snapshot", new SimpleSnapshotSerializer());
 
             prevayler = factory.create();
         } catch (IOException e) {
             throw new IllegalStateException("Can't start Prevayler!", e);
         } catch (ClassNotFoundException e) {
             throw new IllegalStateException("Can't start Prevayler!", e);
         }
     }
 
     public boolean isSafemode() {
         return safemode;
     }
 
     public boolean isAutocommit() {
         return autocommit;
     }
 
     // transactions
     public PersistentTransaction getTransaction() {
         return transactions.get();
     }
 
     public static boolean transactionActive() {
         return get().getTransaction() != null;
     }
     
     private void setTransaction(PersistentTransaction transaction) {
         transactions.set(transaction);
     }
     
     private void execute(PersistentTransaction transaction) {
         prevayler.execute(transaction);
     }
 
     private void beginTransaction() {
         if (getTransaction() != null) {
             throw new IllegalStateException("Transaction already in progress.");
         }
 
         setTransaction(new PersistentTransaction());
     }
     
     private void commitCurrentTransaction() {
         if (getTransaction() == null) {
             throw new IllegalStateException("No transaction to commit.");
         }
 
         // todo: add unstored references to the transaction
         /* to much magic, probably better to let developer handle this
         try {
             for (SimpleModel m : getTransaction().getStored()) {
                 for (Field field : ReflectHelper.get().getReferenceFieldsFromClass(m.getClass())) {
                     SimpleReference ref = (SimpleReference)field.get(m);
                     SimpleModel model = ref.get();
                     if (model != null && !getSavedFieldValue(model)) {
                         // add to stored
                     }
                 }
                 for (Field field : ReflectHelper.get().getListFieldsFromClass(m.getClass())) {
                     SimpleList list = (SimpleList)field.get(m);
                     for (SimpleModel model : list) {
                         if (model != null && !getSavedFieldValue(model)) {
                             // add to stored
                         }
                     }
                 }
             }
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }*/
 
         if (getTransaction().hasChanges()) {
             prevayler.execute(getTransaction());
         }
         transactions.remove();
     }
     
     private void rollbackCurrentTransaction() {
         if (getTransaction() == null) {
             throw new IllegalStateException("No transaction to rollback.");
         }
 
         transactions.remove();
     }
 
     public static void begin() {
         get().beginTransaction();
     }
     
     public static void commit() {
         get().commitCurrentTransaction();
     }
     
     public static void rollback() {
         get().rollbackCurrentTransaction();
     }
 
     public void snapshot() {
         try {
             prevayler.takeSnapshot();
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 
     // retrieval functions
     protected <K, M extends Persistent<K>> Map<K, M> getModelMap(Class<M> cls) {
         PersistentObjectStore pos = (PersistentObjectStore)prevayler.prevalentSystem();
 
         Map result = pos.getModelMap(cls);
 
         if (getTransaction() != null) {
             for (PersistentTransaction.Action action : getTransaction().getActions()) {
                 if (action.isClass(cls)) {
                     if (action.remove) {
                         result.remove(action.model.getId());
                     } else {
                         result.put((K)action.model.getId(), (M)action.model);
                     }
                 }
             }
         }
 
         return result;
     }
 
     public <K, M extends Persistent<K>> M find(Class<? extends M> cls, K id) {
         PersistentObjectStore ps = (PersistentObjectStore)prevayler.prevalentSystem();
 
         M result = (M) ps.find(cls, id);
 
         if (getTransaction() != null) {
             for (PersistentTransaction.Action action : getTransaction().getActions()) {
                if (cls.equals(action.model.getClass()) && id.equals(action.model.getId())) {
                     if (action.remove) {
                         result = null;
                     } else {
                         result = (M)action.model;
                     }
                 }
             }
         }
 
         if (safemode && result != null) {
             try {
                 result = cls.cast(result.clone());
             } catch (CloneNotSupportedException e) {
                 throw new IllegalStateException(e);
             }
         }
 
         return result;
     }
 
     public Set<Class<? extends Persistent>> getObjectTypeList() {
         PersistentObjectStore ps = (PersistentObjectStore)prevayler.prevalentSystem();
 
         return ps.getPersistentStore().keySet();
     }
 
     public Map<Class<? extends Persistent>, Integer> getObjectTypeMap() {
         Map<Class<? extends Persistent>, Integer> result = new HashMap<Class<? extends Persistent>, Integer>();
         PersistentObjectStore ps = (PersistentObjectStore)prevayler.prevalentSystem();
 
         for (Class cls : ps.getPersistentStore().keySet()) {
             result.put(cls, ps.getPersistentStore().get(cls).size());
         }
 
         return result;
     }
 
     public <K, M extends Persistent<K>> Collection<Persistent> findAll(Class<M> clazz) {
         PersistentObjectStore ps = (PersistentObjectStore)prevayler.prevalentSystem();
 
         return ps.getPersistentStore().get(clazz).values();
     }
 
     public <K, M extends Persistent<K>> void createIndex(Class<M> cls, String property) {
         PersistentIndexTransaction cit = new PersistentIndexTransaction(cls, property);
 
         prevayler.execute(cit);
     }
 
     public <K, M extends Persistent<K>>  PersistentIndex getIndex(Class<M> cls, String name) {
         PersistentObjectStore pos = (PersistentObjectStore)prevayler.prevalentSystem();
 
         return pos.getIndex(cls, name);
     }
 
 }
