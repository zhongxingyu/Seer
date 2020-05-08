 package com.bezpredel.collections;
 
 import java.util.*;
 
 public class PseudoEnumSet<K extends PseudoEnum> extends AbstractSet<K> {
     private final Class<K> keyClass;
     private transient final BitSet bitSet;
     private short size = 0;
 
     public PseudoEnumSet(Class<K> clazz) {
         keyClass = clazz;
         bitSet = new BitSet(Math.max(16, PseudoEnum.getCurrentCapacity(keyClass)));
     }
 
     @Override
     public boolean contains(Object o) {
         K k = (K)o;
 
         return k!=null && bitSet.get(k.getOrdinal());
     }
 
     @Override
     public boolean remove(Object o) {
         if(o==null) return false;
         K k = (K)o;
         boolean retVal = bitSet.get(k.getOrdinal());
         if(retVal) {
            bitSet.set(k.getOrdinal(), false);
             size--;
         }
 
         return retVal;
     }
 
     @Override
     public boolean add(K k) {
         if(k==null) throw new NullPointerException();
         boolean retVal = !bitSet.get(k.getOrdinal());
         if(retVal) {
             bitSet.set(k.getOrdinal());
             size++;
         }
         return retVal;
     }
 
     @Override
     public void clear() {
         bitSet.clear();
         size=0;
     }
 
     @Override
     public Iterator<K> iterator() {
         return new Iterator<K>() {
             private int pos = -1;
 
             private void advance() {
                 for(;pos < bitSet.size() && (pos < 0 || !bitSet.get(pos)); pos++) {
 
                 }
             }
 
             public boolean hasNext() {
                 advance();
                return bitSet.get(pos); // this works without a range check because BitSet.get() returns false for out-of-range checks
             }
 
             public K next() {
                 advance();
                 if(!bitSet.get(pos)) {
                     throw new  NoSuchElementException();
                 } else {
                    K k = PseudoEnum.get(keyClass, pos);
                    pos++;
                    return k;
                 }
             }
 
             public void remove() {
                 if(pos < 0 || !bitSet.get(pos)) throw new IllegalStateException();
                 bitSet.set(pos, false);
             }
         };
     }
 
     @Override
     public int size() {
         return size;
     }
 }
