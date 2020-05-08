 package distributed.hash.table;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class PersistentStorageManager extends Thread {
     private HashMap<String, Object> mStore;
    private int mPeriodMilliSecs = 0;
     private PersistentStorage mPersistentStorage;
 
     public PersistentStorageManager(int periodSecs) {
         mStore = new HashMap<String,Object>();
        mPeriodMilliSecs = periodSecs * 1000;
         mPersistentStorage = new PersistentStorage();
     }
 
     public void register(String objName, Object obj) {
         synchronized(this.mStore) {
             if (mStore.containsKey(objName)) {
                 return;
             }
             mStore.put(objName, obj);
         }
     }
 
     /**
      * the storage manager thread
      */
     public void run() {
         while(true) {
             synchronized(mStore) {
                 for (Map.Entry<String, Object> entry : mStore.entrySet()) {
                     Object value = entry.getValue();
                     synchronized(value) {
                         try {
                             mPersistentStorage.save(entry.getKey(), value);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 }
             }
             try {
                sleep(mPeriodMilliSecs);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 }
