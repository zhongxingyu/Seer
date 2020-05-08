 package jDistsim.utils.collection.observable;
 
import jDistsim.utils.pattern.observer.IObservable;
import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 10.2.13
  * Time: 13:37
  */
public class ObservableHashMap<Key, Value extends IObservable> extends Observable implements IObserver {
 
     private HashMap<Key, Value> hashMap;
 
     public ObservableHashMap() {
         hashMap = new HashMap<>();
     }
 
     public void put(Key key, Value value) {
         hashMap.put(key, value);
        value.addObserver(this);
        notifyObservers("put");
     }
 
     public Value get(Key key) {
         return hashMap.get(key);
     }
 
     public Value remove(Key key) {
         Value value = hashMap.remove(key);
        value.removeObserver(this);
        notifyObservers("remove");
         return value;
     }
 
     public boolean containsKey(Key key) {
         return hashMap.containsKey(key);
     }
 
     public Collection<Value> values() {
         return hashMap.values();
     }

    @Override
    public void update(Observable observable, Object arguments) {
        notifyObservers("itemChanges");
    }
 }
