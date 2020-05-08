 package jDistsim.utils.collection.observable;
 
 import jDistsim.utils.pattern.observer.Observable;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 10.2.13
  * Time: 13:37
  */
public class ObservableHashMap<Key, Value> extends Observable {
 
     private HashMap<Key, Value> hashMap;
 
     public ObservableHashMap() {
         hashMap = new HashMap<>();
     }
 
     public void put(Key key, Value value) {
         hashMap.put(key, value);
        notifyObservers(value);
     }
 
     public Value get(Key key) {
         return hashMap.get(key);
     }
 
     public Value remove(Key key) {
         Value value = hashMap.remove(key);
        notifyObservers(value);
         return value;
     }
 
     public boolean containsKey(Key key) {
         return hashMap.containsKey(key);
     }
 
     public Collection<Value> values() {
         return hashMap.values();
     }
 }
