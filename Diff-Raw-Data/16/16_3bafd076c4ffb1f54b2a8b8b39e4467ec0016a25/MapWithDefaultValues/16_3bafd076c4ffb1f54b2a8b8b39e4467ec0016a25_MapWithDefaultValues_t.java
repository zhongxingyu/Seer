 package ndfs.mcndfs_alg3;
 
 import java.util.HashMap;
 
 import graph.State;
 
 /*
  * This class is very much like a hashmap, but it acts as if all keys not present
  * in the hashmap actually are, having value defaultValue, which can only be set
  * at construction of this object  
  */
 public class MapWithDefaultValues<KeyType, ValueType>{
 
   private HashMap<KeyType, ValueType> hashMap;
   private ValueType defaultValue;
 
   MapWithDefaultValues(HashMap<KeyType, ValueType> hashMap, ValueType defaultValue){
     this.hashMap = hashMap;
     this.defaultValue = defaultValue;
   }
 
   // TODO: make const method
   ValueType getValue(KeyType key){
     return this.hashMap.get(key);
   }
 
   /* returns true if either
    * - (key,value) pair is in hashmap
    * - (key,value) pair is not in hashmap AND the requested value is the default value 
    */
   boolean hasKeyValuePair(KeyType key,ValueType value){
     ValueType v = getValue(key);
     if(v==null){
      return value.equals(defaultValue);
     }
     return v.equals(value);
   }
 
 
   synchronized void setValueSynchronized(KeyType key,ValueType value){
     hashMap.put(key,value);
   }
 
   void setValue(KeyType key,ValueType value){
     hashMap.put(key,value);
   }
 
 }
