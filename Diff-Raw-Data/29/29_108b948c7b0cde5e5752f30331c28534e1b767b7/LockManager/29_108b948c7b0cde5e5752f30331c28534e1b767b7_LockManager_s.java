 
 package distributedTransactions;
 
 import java.util.HashMap;
 import java.util.concurrent.ArrayBlockingQueue;
 
 
 interface LockCallback {
     void resourceIsAvailable();
 }
 
 class LockRequestQueue<T> extends ArrayBlockingQueue<T> {
     public boolean isEmpty() {
         return (this.size() < 1);
     }
 }
 
 
 public class LockManager {
     Resource r = null;
     HashMap<Resource,Byte> locktable = new HashMap<Resource,Byte>();
    LockRequestQueue<LockCallback> waitQueue = new LockRequestQueue<LockCallback>();
 
     public LockManager(Resource resource) {
         this.r = resource;
     }
 
     public boolean lock(LockCallback callback) {
         if (locktable.containsKey(r)) {
            waitQueue.put(callback);
             return false;
         } else {
             locktable.put(r,null);
             return true;
         }
     }
 
     public void release() {
         if (!waitQueue.isEmpty()) {
            LockCallback callback = waitQueue.take();
            callback.resourceIsAvailable();
             return;
         }
         locktable.remove(r);
     }
 }
 
