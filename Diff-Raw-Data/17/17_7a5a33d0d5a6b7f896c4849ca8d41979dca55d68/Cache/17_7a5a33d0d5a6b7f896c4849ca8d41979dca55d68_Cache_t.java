 package org.daum.library.replica.cache;
 
 import org.daum.library.replica.msg.Update;
 import org.daum.library.replica.utils.SystemTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Created by jed
  * User: jedartois@gmail.com
  * Date: 23/05/12
  * Time: 15:43
  */
 public class Cache extends DHashMap<Object,VersionedValue> {
     private Logger logger = LoggerFactory.getLogger(this.getClass());
     private String name = "";
     private CacheManager cacheManger=null;
     private SystemTime systemTime = new SystemTime();
     private int MaxEntriesLocalHeap=1000;
 
 
     public Cache(String cachename,CacheManager cacheManger)
     {
         this.name = cachename;
         this.cacheManger = cacheManger;
     }
 
     public void setMaxEntriesLocalHeap(int max)
     {
         this.MaxEntriesLocalHeap = max;
     }
 
     public VersionedValue put(Object key, Object value) {
 
         if(this.size() > MaxEntriesLocalHeap)
         {
             // todo
             logger.warn("The Max Entries Local Heap is reached ");
             return null;
         }else
         {
 
             Update e = new Update();
             VersionedValue version = new VersionedValue(value);
            if(super.get(key) == null)
            {
                e.op = StoreEvent.ADD;
            } else {
                e.op = StoreEvent.UPDATE;
            }
             e.key = key;
             e.setVersionedValue(version);
             e.cache = name;
             // local
             VersionedValue last = super.put(key, version);
 
             // remote
             cacheManger.remoteDisptach(e);
             return  last;
         }
     }
 
 
     @Override
     public VersionedValue get(Object key) {
         VersionedValue versionedValue = (VersionedValue) super.get(key);
         return versionedValue;
     }
 
 
     public void localDispatch(Update replica)
     {
         logger.debug("Local dispatch "+name);
 
        if (replica.op.equals(StoreEvent.ADD) || replica.op.equals(StoreEvent.UPDATE))
         {
             VersionedValue old = (VersionedValue) super.get(replica.key);
             if(old == null)
             {
                 super.put(replica.key, replica.getVersionedValue());
             }
             else
             {
                 // compare version
                 if(old.before(replica.getVersionedValue()))
                 {
                     super.put(replica.key, replica.getVersionedValue());
                 } else
                 {
                     logger.debug("receive old version");
                 }
             }
 
        } else if (replica.op.equals(StoreEvent.DELETE))
         {
             super.remove(replica.key);
         }
     }
 
 
     @Override
     public VersionedValue remove(Object key) {
         //logger.warn("WARNING !! TODO backup remove until synchronize is finish");
         Update e = new Update();
        e.op = StoreEvent.DELETE;
         e.key =  key;
         e.cache = name;
         VersionedValue last = super.remove(key);
         cacheManger.remoteDisptach(e);
         return   last;
     }
 
 }
