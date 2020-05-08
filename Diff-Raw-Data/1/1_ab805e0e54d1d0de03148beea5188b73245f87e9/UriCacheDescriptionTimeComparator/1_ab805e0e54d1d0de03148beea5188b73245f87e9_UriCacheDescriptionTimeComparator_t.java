 package org.hackystat.sensorbase.uricache;
 
 import java.util.Comparator;
 
 /**
  * Compares two caches creation time by using time saved in the description.
  * 
  * @author Pavel Senin.
  * 
  */
 public class UriCacheDescriptionTimeComparator implements Comparator<UriCacheDescription> {
 
   /**
    * Compares two caches creation time by using time saved in the description.
    * 
    * @param desc1 UriCacheDescription instance #1.
    * @param desc2 UriCacheDescription instance #2.
    * 
    * @return 0 if both caches created in the same tome, value greater than 0 if cache2 creation time
    *         is less than cache #1 creation time and returns value less than 0 if cache #1 creation
    *         time is less than cache #2 creation time.
    */
   public int compare(UriCacheDescription desc1, UriCacheDescription desc2) {
     Long cache1CreationTime = desc1.getCreationTime();
     Long cache2CreationTime = desc2.getCreationTime();
     return cache1CreationTime.compareTo(cache2CreationTime);
   }
 
 }
