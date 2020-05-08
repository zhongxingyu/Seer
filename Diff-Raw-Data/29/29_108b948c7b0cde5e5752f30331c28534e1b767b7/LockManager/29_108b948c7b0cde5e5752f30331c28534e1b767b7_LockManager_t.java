 
 package distributedTransactions;
 
 import java.util.HashMap;
import java.util.Collection;
 import java.util.concurrent.ArrayBlockingQueue;
 
 
 interface LockCallback {
     void resourceIsAvailable();
 }
 
 class LockRequestQueue<T> extends ArrayBlockingQueue<T> {
    public LockRequestQueue(int capacity) {
        super(capacity);
    }
    public LockRequestQueue(int capacity, boolean fair) {
        super(capacity, fair);
    }
    public LockRequestQueue(int capacity, boolean fair, Collection<T> c) {
        super(capacity, fair, c);
    }

     public boolean isEmpty() {
         return (this.size() < 1);
     }
 }
 
 
 public class LockManager {
     Resource r = null;
     HashMap<Resource,Byte> locktable = new HashMap<Resource,Byte>();
    LockRequestQueue<LockCallback> waitQueue = new LockRequestQueue<LockCallback>(10);
 
     public LockManager(Resource resource) {
         this.r = resource;
     }
 
     public boolean lock(LockCallback callback) {
         if (locktable.containsKey(r)) {
            try { waitQueue.put(callback); } catch(InterruptedException e) {}
             return false;
         } else {
             locktable.put(r,null);
             return true;
         }
     }
 
     public void release() {
        if (locktable.isEmpty()) {
            return;
        }
         if (!waitQueue.isEmpty()) {
            try {
                LockCallback callback = waitQueue.take();
                callback.resourceIsAvailable();
            } catch(InterruptedException e) {}
             return;
         }
         locktable.remove(r);
     }
 }
 
