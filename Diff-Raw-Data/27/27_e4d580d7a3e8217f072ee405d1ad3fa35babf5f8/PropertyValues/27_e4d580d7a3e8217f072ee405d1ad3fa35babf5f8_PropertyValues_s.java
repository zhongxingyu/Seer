 package com.versionone.common.sdk;
 
 import java.util.AbstractCollection;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.versionone.Oid;
 
 public class PropertyValues extends AbstractCollection<ValueId> {
 
     private static final long serialVersionUID = -8979996731417517341L;
 
     private final Map<Oid, ValueId> dictionary = new HashMap<Oid, ValueId>();
     private final Map<Oid, Integer> index = new HashMap<Oid, Integer>();
     private int currentIndex = 0;
 
     public PropertyValues(Collection<ValueId> valueIds) {
         for (ValueId id : valueIds) {
             addInternal(id);
         }
     }
 
     public PropertyValues() {
     }
 
     @Override
     public Iterator<ValueId> iterator() {
         return dictionary.values().iterator();
     }
 
     @Override
     public int size() {
         return dictionary.values().size();
     }
 
     @Override
     public String toString() {
         if (dictionary.isEmpty()) {
             return "";
         }
         StringBuilder res = new StringBuilder();
         Iterator<ValueId> i = iterator();
         res.append(i.next());
         while (i.hasNext()) {
             res.append(", ").append(i.next());
         }
         return res.toString();
     }
 
     ValueId find(Oid oid) {
         return dictionary.get(oid.getMomentless());
     }
    
     boolean containsOid(Oid value) {
         return dictionary.containsKey(value.getMomentless());
     }
 
     public boolean contains(ValueId valueId) {
         return dictionary.containsValue(valueId);
     }
 
     public ValueId[] toArray() {
         ValueId[] values = new ValueId[size()];
         dictionary.values().toArray(values);
         return values;
     }
 
     public boolean add(ValueId value) {
     	addInternal(value);
     	return true;
     }
     
     void addInternal(ValueId value) {
         dictionary.put(value.oid, value);
         index.put(value.oid, currentIndex);
         currentIndex++;
     }
        
     PropertyValues subset(Object[] oids) {
         PropertyValues result = new PropertyValues();
         for (Object oid : oids) {
             result.add(find((Oid) oid));
         }
         return result;
     }
     
     public String[] toStringArray() {
         String[] values = new String[size()];       
         for (ValueId data : dictionary.values()) {
             values[index.get(data.oid)] = data.toString();
         }
         return values;
     }
 
     public int getStringArrayIndex(ValueId value) {
        return index.get(value.oid);        
     }    
 
     public ValueId getValueIdByIndex(int value) {
         int i = 0;
         for (ValueId data : dictionary.values()) {
             if (value == index.get(data.oid)) {
                 return data;
             }
             i++;
         }
         
         return null;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (!(obj instanceof PropertyValues)) {
             return false;
         }
         PropertyValues other = (PropertyValues) obj;
         if (size() != other.size()){
             return false;
         }
         for (ValueId id : this){
             if (!other.contains(id)){
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int res = 0;
         for (ValueId id : this){
             res = res * 31 + id.hashCode();
         }
         return res;
     }
     
     
 }
