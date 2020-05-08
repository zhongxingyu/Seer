 package com.hceris.datastructures;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 public class HashMap<K,V> implements Map<K,V> {
 
    private Entry<K,V>[] elements;
     private int size;
 
 
    @SuppressWarnings("unchecked")
	public HashMap(int size) {
        elements = (Entry<K,V>[]) new Entry[adjustSize(size)];
         this.size = 0;
     }
 
     public HashMap(Map<? extends K, ? extends V> m) {
         this(m.size());
         putAll(m);
     }
 
     private int adjustSize(int baseSize) {
         int validSize = 1;
         while(baseSize > validSize) {
             validSize <<= 1;
         }
         return validSize;
     }
 
     public void clear() {
         for(int i = 0; i < elements.length; i++) {
             elements[i] = null;
         }
         size = 0;
     }
 
     private int indexForKey(Object key) {
         return key.hashCode() & (elements.length - 1);
     }
 
     private Entry<K,V> entryForKey(Object key) {
        for(Entry<K,V> e = elements[indexForKey(key)]; e != null; e = e.next) {
             if(e.key.equals(key)) {
                return e;
             }
         }
         return null;
     }
 
     public boolean containsKey(Object arg0) {
         return entryForKey(arg0) != null;
     }
 
     public boolean containsValue(Object arg0) {
        for(Entry<K,V> e : elements) {
             while(e != null) {
                 if(e.value.equals(arg0)) {
                     return true;
                 }
                 e = e.next;
             }
         }
         return false;
     }
 
     public Set<java.util.Map.Entry<K, V>> entrySet() {
         throw new UnsupportedOperationException();
     }
 
     public V get(Object key) {
         Entry<K,V> e = entryForKey(key);
         return e != null ? e.value : null;
     }
 
     public boolean isEmpty() {
         return size == 0;
     }
 
     public Set<K> keySet() {
         throw new UnsupportedOperationException();
     }
 
     public V put(K key, V value) {
         Entry<K,V> e = entryForKey(key);
 
         if(e != null) {
             V oldValue = e.value;
             e.value = value;
             return oldValue;
         }
 
         int index = indexForKey(key);
         size++;
        elements[index] = new Entry<K,V>(key, value, elements[index]);
         return null;
     }
 
     public void putAll(Map<? extends K, ? extends V> arg0) {
         for(Map.Entry<? extends K,? extends V> e : arg0.entrySet()) {
             put(e.getKey(), e.getValue());
         }
     }
 
     public V remove(Object key) {
         int index = indexForKey(key);
 
         if(elements[index] == null) {
             return null;
         }
 
         if(elements[index].key.equals(key)) {
             V value = (V) elements[index].value;
             elements[index] = elements[index].next;
             size--;
             return value;
         }
 
        Entry<K,V> prev = elements[index];
        Entry<K,V> current = elements[index].next;
 
         while(current != null) {
             if(current.key.equals(key)) {
                V value = current.value;
 
                 if(prev == elements[index]) {
                     elements[index].next = current.next;
                 } else {
                     prev.next = current.next;
                 }
 
                 size--;
                 return value;
             }
             prev = current;
             current = current.next;
         }
         return null;
     }
 
     public int size() {
         return size;
     }
 
     public Collection<V> values() {
         throw new UnsupportedOperationException();
     }
 
 
     static class Entry<K,V> {
         final K key;
         V value;
         Entry<K,V> next;
 
         Entry(K key, V value, Entry<K,V> next) {
             this.key = key;
             this.value = value;
             this.next = next;
         }
     }
 }
