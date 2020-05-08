 package org.infinispan.mvcc;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author pedro
  *         Date: 25-07-2011
  */
 public class VersionVC implements Externalizable, Serializable {
     public static transient final long EMPTY_POSITION = -1;
     public static transient final VersionVC EMPTY_VERSION = new VersionVC();
 
 
     private Map<Integer,Long> vectorClock;
 
     public VersionVC() {
         vectorClock = new HashMap<Integer, Long>();
     }
 
     /**
      * Compares two vector clocks ands returns true if *this* is less or equals than the *other*.
      * For any two vector clocks, v1 and v2,v1 is less or equals than v2 iff for each position i,
      * v1[i] <= v2[i] || v1[i] == null || v2[i] == null
      * @param other vector clock to compare
      * @return true if *this* vector clock is less or equals to the *other* vector clock
      */
     public boolean isLessOrEquals(VersionVC other) {
         if(other == null || other.vectorClock.isEmpty() || this.vectorClock.isEmpty()) {
             return true;
         }
 
         for(Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
             Long otherValue = other.vectorClock.get(entry.getKey());
            if(otherValue != null && entry.getValue() > otherValue) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Compares two vector clocks ands returns true if *this* is less than the *other*.
      * For any two vector clocks, v1 and v2,v1 is less than v2 iff for each position i,
      * v1[i] < v2[i] || v1[i] == null || v2[i] == null
      * @param other vector clock to compare
      * @return true if *this* vector clock is less to the *other* vector clock
      */
     public boolean isLessThan(VersionVC other) {
         if(other == null || other.vectorClock.isEmpty() || this.vectorClock.isEmpty()) {
             return true;
         }
 
         for(Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
             Long otherValue = other.vectorClock.get(entry.getKey());
            if(otherValue != null && entry.getValue() >= otherValue) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean isGreaterThan(VersionVC other) {
         if(other == null || other.vectorClock.isEmpty() || this.vectorClock.isEmpty()) {
             return true;
         }
 
         for(Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
             Long otherValue = other.vectorClock.get(entry.getKey());
            if(otherValue != null && entry.getValue() <= otherValue) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Compares two vector clocks ands returns true if *this* is equals than the *other*.
      * For any two vector clocks, v1 and v2,v1 is equals than v2 iff for each position i,
      * v1[i] == v2[i] || v1[i] == null || v2[i] == null
      * @param other vector clock to compare
      * @return true if *this* vector clock is equals to the *other* vector clock
      */
     public boolean isEquals(VersionVC other) {
         if(other == null || other.vectorClock.isEmpty() || this.vectorClock.isEmpty()) {
             return true;
         }
 
         for(Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
             Long otherValue = other.vectorClock.get(entry.getKey());
            if(otherValue != null && entry.getValue().longValue() != otherValue.longValue()) {
                 return false;
             }
         }
         return true;
     }
 
     public long get(Integer position) {
         Long l = vectorClock.get(position);
         return l != null ? l : -1;
     }
 
     public void set(Integer position, long value) {
         vectorClock.put(position, value);
     }
 
     /**
      * change this vector clock to the maximum between this and the other.
      * The maximum is defined this way:
      *   i) if the position exists in both vector clocks, then it is the maximum value
      *   ii) if the position exists only in *this*, then it remains unchanged
      *   iii) if the position exists only in *other*, then it is putted in *this* vector clock
      * @param other the other vector clock
      */
     public void setToMaximum(VersionVC other) {
         if(other == null) {
             return;
         }
         for(Map.Entry<Integer, Long> entry : other.vectorClock.entrySet()) {
             Integer key = entry.getKey();
             Long otherValue = entry.getValue();
             Long myValue = this.vectorClock.get(key);
             if(myValue == null || myValue < otherValue) {
                 this.vectorClock.put(key, otherValue);
             }
         }
     }
 
     public VersionVC copy() {
         VersionVC copy = new VersionVC();
         copy.vectorClock.putAll(this.vectorClock);
         return copy;
     }
 
     @Override
     public String toString() {
         return "Version{vc=" + vectorClock + "}";
     }
 
     public void incrementPositions(Integer... positions) {
         for(Integer p : positions) {
             Long value = vectorClock.get(p);
             if(value != null) {
                 vectorClock.put(p, value + 1);
             } else {
                 vectorClock.put(p, 1L);
             }
         }
     }
 
     @Override
     public void writeExternal(ObjectOutput objectOutput) throws IOException {
         if(vectorClock == null || vectorClock.isEmpty()) {
             objectOutput.writeInt(0);
         }
         objectOutput.writeInt(vectorClock.size());
         for(Map.Entry<Integer, Long> e : vectorClock.entrySet()) {
             objectOutput.writeInt(e.getKey());
             objectOutput.writeLong(e.getValue());
         }
     }
 
     @Override
     public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
         int size = objectInput.readInt();
         if(size == 0) {
             return;
         }
 
         if(vectorClock == null) {
             vectorClock = new HashMap<Integer, Long>();
         }
 
         while(size-- > 0) {
             int k = objectInput.readInt();
             long v = objectInput.readLong();
             vectorClock.put(k, v);
         }
     }
 
     /*private void writeObject(ObjectOutputStream out) throws IOException {
         writeExternal(out);
     }
 
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         readExternal(in);
     }*/
 }
