 package ndfs.mcndfs_alg2;
 
 
 
 import java.util.HashMap;
 
 import graph.State;
 
 
 
 public class MapWithDefaultValues<KeyType, ValueType> {
 
     private HashMap<KeyType, ValueType> hashmap;
     ValueType defaultValue;
 
     MapWithDefaultValues(HashMap<KeyType, ValueType> hashmap, ValueType defaultValue){
         this.hashmap = hashmap;
 	this.defaultValue = defaultValue;
     }
 
     // TODO: make const method
     ValueType getValue(KeyType key) {
     	return this.hashmap.get(key);
     }
 
     boolean hasKeyValuePair(KeyType key,ValueType value){
        ValueType v = getValue(key);
       // If key does not map to any value (null), then value should be default value
       // OR it must match the value 'v'
       return ( (v == null && value.equals(defaultValue)) || v.equals(value) );
     }
 
 
     synchronized void setValueSynchronized(KeyType key,ValueType value){
         hashmap.put(key,value);
     }
 
     void setValue(KeyType key,ValueType value){
         hashmap.put(key,value);
     }
 
 }
