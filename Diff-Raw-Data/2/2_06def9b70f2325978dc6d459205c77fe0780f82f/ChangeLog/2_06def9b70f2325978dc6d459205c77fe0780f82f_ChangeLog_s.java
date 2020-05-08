 package ch.x42.terye.persistence;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import ch.x42.terye.ItemImpl;
 import ch.x42.terye.persistence.id.ItemId;
 
 public class ChangeLog {
 
     private Map<ItemId, ItemState> addedStates = new LinkedHashMap<ItemId, ItemState>();
     private Map<ItemId, ItemState> modifiedStates = new LinkedHashMap<ItemId, ItemState>();
     private Map<ItemId, ItemState> removedStates = new LinkedHashMap<ItemId, ItemState>();
 
     public void added(ItemImpl item) {
         addedStates.put(item.getId(), item.getState());
     }
 
     public void modified(ItemImpl item) {
         if (!addedStates.containsKey(item.getId())) {
             modifiedStates.put(item.getId(), item.getState());
         }
     }
 
     public void removed(ItemImpl item) {
         if (addedStates.remove(item.getId()) == null) {
             modifiedStates.remove(item.getId());
             removedStates.put(item.getId(), item.getState());
         }
     }
 
     public Collection<ItemState> getAddedStates() {
         return addedStates.values();
     }
 
     public Collection<ItemState> getModifiedStates() {
         return modifiedStates.values();
     }
 
     public Collection<ItemState> getRemovedStates() {
         return removedStates.values();
     }
 
     public boolean isEmpty() {
        return !(addedStates.isEmpty() && modifiedStates.isEmpty() && removedStates
                 .isEmpty());
     }
 
     public void purge() {
         addedStates.clear();
         modifiedStates.clear();
         removedStates.clear();
     }
 
 }
