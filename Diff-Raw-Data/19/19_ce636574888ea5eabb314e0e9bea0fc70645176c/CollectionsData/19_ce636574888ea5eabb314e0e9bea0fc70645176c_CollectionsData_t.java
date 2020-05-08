 package eu.pinnoo.garbagecalendar.data;
 
 import eu.pinnoo.garbagecalendar.data.caches.CollectionCache;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
public class CollectionsData implements Serializable {
 
     private static final CollectionsData instance = new CollectionsData();
     private List<Collection> collections;
 
     private CollectionsData() {
         collections = CollectionCache.getInstance().getAll();
        if (collections == null) {
             collections = new ArrayList<Collection>();
         }
     }
 
     public static CollectionsData getInstance() {
         return instance;
     }
 
     public void resetCollections() {
         collections.clear();
         CollectionCache.getInstance().clear();
     }
 
     public void addCollection(Collection col) {
         collections.add(col);
         CollectionCache.getInstance().put(col.getCollectionCode(), col);
     }
 
     public List<Collection> getCollections() {
         return collections;
     }
 
    public void addToLastCollection(Type[] types) {
        Collection last = collections.get(collections.size() - 1);
        last.addTypes(types);
        CollectionCache.getInstance().put(last.getCollectionCode(), last);
     }
 
     public boolean isSet() {
         return collections != null && !collections.isEmpty();
     }
 }
