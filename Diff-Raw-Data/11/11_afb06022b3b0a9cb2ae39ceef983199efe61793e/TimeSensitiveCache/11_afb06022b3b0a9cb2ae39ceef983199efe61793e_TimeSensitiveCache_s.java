 package dk.statsbiblioteket.doms.authchecker;
 
 import java.util.Date;
 import java.util.LinkedHashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: Oct 7, 2010
  * Time: 3:51:19 PM
  * To change this template use File | Settings | File Templates.
  */
 public class  TimeSensitiveCache<T extends Cacheble> {
 
     private LinkedHashMap<String,T> elements;
 
     private Date lastClean;
 
     private long timeToLive;
 
     public TimeSensitiveCache(long timeToLive) {
         this.timeToLive = timeToLive;
         elements = new LinkedHashMap<String,T>();
         lastClean = new Date();
     }
 
 
     /**
      * Get the element with the associated ID. If the element is not found, return
      * null
      * @param id the element id
      * @return the element, or null if not found
      */
     public synchronized T getElement(String id){
         cleanup();
         return elements.get(id);
     }
 
 
     /**
      * Insert the element in the cache.
      * @param element the element to insert.
      */
     public synchronized void addElement(T element){
         cleanup();
         String id = element.getID();
         elements.put(id,element);
     }
 
     private synchronized void cleanup(){
         if (!isToOld(lastClean.getTime())){
             return;
         }
 
         lastClean = new Date();
         if (elements.isEmpty()){
             return;
         }
        for (T element : elements.values()) {
             if (isToOld(element.getCreationTime())){
                elements.remove(element.getID());
             } else {
                 break;
             }
         }
     }
 
     private  synchronized boolean isToOld(long event) {
         Date now = new Date();
         if (new Date(event+timeToLive).after(now)){
             return false;
         } else {
             return true;
         }
     }
 
 
 }
