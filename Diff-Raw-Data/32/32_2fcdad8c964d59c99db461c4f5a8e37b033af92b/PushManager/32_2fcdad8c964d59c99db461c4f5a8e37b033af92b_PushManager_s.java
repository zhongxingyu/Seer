 package org.geogit.web.api.repo;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.ObjectId;
 import org.geogit.api.RevObject;
 
 public class PushManager {
 
    private static Map<String, List<ObjectId>> incomingData;
 
     private static PushManager instance = new PushManager();
 
     private PushManager() {
         incomingData = new HashMap<String, List<ObjectId>>();
     }
 
     public static PushManager get() {
         return instance;
     }
 
     public void connectionBegin(String ipAddress) {
         if (incomingData.containsKey(ipAddress)) {
             incomingData.remove(ipAddress);
         }
         List<ObjectId> newList = new LinkedList<ObjectId>();
         incomingData.put(ipAddress, newList);
     }
 
     public void connectionSucceeded(GeoGIT geogit, String ipAddress) {
         // Add objects to the repository
         List<ObjectId> objectsToMove = incomingData.remove(ipAddress);
         for (ObjectId oid : objectsToMove) {
             RevObject toAdd = geogit.getRepository().getIndex().getDatabase().get(oid);
             geogit.getRepository().getObjectDatabase().put(toAdd);
         }
     }
 
     public boolean alreadyPushed(String ipAddress, ObjectId oid) {
         return incomingData.get(ipAddress).contains(oid);
     }
 
     public void addObject(String ipAddress, ObjectId oid) {
         if (incomingData.containsKey(ipAddress)) {
             incomingData.get(ipAddress).add(oid);
         }
     }
 
 }
