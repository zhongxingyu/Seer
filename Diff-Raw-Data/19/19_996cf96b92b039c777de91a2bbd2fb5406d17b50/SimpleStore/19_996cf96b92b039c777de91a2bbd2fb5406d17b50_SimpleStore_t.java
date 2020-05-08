 package nl.astraeus.persistence;
 
 import org.prevayler.Prevayler;
 import org.prevayler.PrevaylerFactory;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: rnentjes
  * Date: 7/24/11
  * Time: 7:24 PM
  */
 public class SimpleStore {
     public final static String SAFEMODE            = "safemode";
     public final static String AUTOCOMMIT          = "autocommit";
     public final static String DATA_DIRECTORY      = "dataDirectory";
     public final static String FILE_AGE_THRESHOLD  = "fileAgeThreshold";
     public final static String FILE_SIZE_THRESHOLD = "fileSizeThreshold";
 
     private final static SimpleStore INSTANCE = new SimpleStore();
     
     private ThreadLocal<SimpleTransaction> transactions = new ThreadLocal<SimpleTransaction>();
 
     public static SimpleStore get() {
         return INSTANCE;
     }
 
     private Prevayler prevayler;
 
     // properties
     private boolean autocommit     = false;
     private boolean safemode       = true;
     private String dataDirectory   = "persistence";
     private long fileAgeThreshold  = TimeUnit.MINUTES.toMillis(1);
     private long fileSizeThreshold = 10*1024L*1024L;
 
     private boolean started = false;
 
     // SimpleModel private field cache
     private Field _prevayler_saved = null;
     private Field _prevayler_last_update = null;
     private Field _prevayler_selected_for_update = null;
 
     public SimpleStore() {
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
 
             PrevaylerFactory factory = new PrevaylerFactory();
 
             factory.configureJournalFileAgeThreshold(fileAgeThreshold);
             factory.configureJournalFileSizeThreshold(fileSizeThreshold);
             factory.configurePrevalentSystem(new PrevalentSystem());
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
     public SimpleTransaction getTransaction() {
         return transactions.get();
     }
 
     public static boolean transactionActive() {
         return get().getTransaction() != null;
     }
     
     private void setTransaction(SimpleTransaction transaction) {
         transactions.set(transaction);
     }
     
     private void execute(SimpleTransaction transaction) {
         prevayler.execute(transaction);
     }
 
     private void beginTransaction() {
         if (getTransaction() != null) {
             throw new IllegalStateException("Transaction already in progress.");
         }
 
         setTransaction(new SimpleTransaction());
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
     protected Map<Long, SimpleModel> getModelMap(Class<? extends SimpleModel> cls) {
         PrevalentSystem ps = (PrevalentSystem)prevayler.prevalentSystem();
 
         Map<Long, SimpleModel> result = ps.getModelMap(cls);
 
         if (getTransaction() != null) {
             for (SimpleTransaction.Action action : getTransaction().getActions()) {
                if (action.isClass(cls)) {
                    if (action.remove) {
                        result.remove(action.model.getId());
                    } else {
                        result.put(action.model.getId(), action.model);
                    }
                 }
             }
         }
 
         return result;
     }
 
     public <M extends SimpleModel> M find(Class<? extends M> cls, long id) {
         PrevalentSystem ps = (PrevalentSystem)prevayler.prevalentSystem();
         
         M result = (M) ps.find(cls, id);
         
         if (getTransaction() != null) {
             for (SimpleTransaction.Action action : getTransaction().getActions()) {
                 if (cls.equals(action.model.getClass()) && id == action.model.getId()) {
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
 
     /**
      * Store functions
      */
 
     private Field getSavedField() {
         return getField(_prevayler_saved, "_prevayler_saved");
     }
 
     private Field getLastUpdateField() {
         return getField(_prevayler_last_update, "_prevayler_last_update");
     }
 
     private Field getSelectForUpdateField() {
         return getField(_prevayler_selected_for_update, "_prevayler_selected_for_update");
     }
 
     private Field getField(Field current, String name) {
         Field result = current;
 
         if (current == null) {
             try {
                 result = SimpleModel.class.getDeclaredField(name);
                 result.setAccessible(true);
             } catch (NoSuchFieldException e) {
                 throw new IllegalStateException("SimpleModel doesn't have field "+name+".");
             }
         }
 
         return result;
     }
 
     void setSavedField(SimpleModel model, boolean value) {
         Field field = getSavedField();
 
         try {
             field.set(model, value);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Can't set PrevaylerBaseModel._prevayler_saved.");
         }
     }
 
     void setLastUpdateField(SimpleModel model) {
         Field field = getLastUpdateField();
 
         try {
             field.set(model, System.currentTimeMillis());
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Can't set PrevaylerBaseModel._prevayler_last_update.");
         }
     }
 
     void setSelectForUpdateField(SimpleModel model, boolean value) {
         Field field = getSelectForUpdateField();
 
         try {
             field.set(model, value);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Can't set PrevaylerBaseModel._prevayler_selected_for_update.");
         }
     }
 
     boolean getSavedFieldValue(SimpleModel model) {
         Field field = getSavedField();
 
         try {
             return field.getBoolean(model);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Can't get PrevaylerBaseModel._prevayler_saved.");
         }
     }
 
     public Set<Class<? extends SimpleModel>> getObjectTypeList() {
         PrevalentSystem ps = (PrevalentSystem)prevayler.prevalentSystem();
 
         return ps.getDataStore().keySet();
     }
 
     public Map<Class<? extends SimpleModel>, Integer> getObjectTypeMap() {
         Map<Class<? extends SimpleModel>, Integer> result = new HashMap<Class<? extends SimpleModel>, Integer>();
         PrevalentSystem ps = (PrevalentSystem)prevayler.prevalentSystem();
 
         for (Class cls : ps.getDataStore().keySet()) {
             result.put(cls, ps.getDataStore().get(cls).size());
         }
 
         return result;
     }
 }
