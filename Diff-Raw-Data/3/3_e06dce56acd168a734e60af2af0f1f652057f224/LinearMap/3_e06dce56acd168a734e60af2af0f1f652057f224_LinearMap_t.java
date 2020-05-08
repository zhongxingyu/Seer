 package util;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Die Klasse implementiert ein Verzeichnis, in dem nach
  * unter einem Schluessel gespeicherten Daten gesucht werden
  * kann.
  */
 public  class LinearMap<K,V> implements IMap<K,V> {
 
     /**
      * Anzahl der gespeicherten Adressen.
      */
     private int size = 0;
     
     /**
      * Feld mit den Daten.
      */
     private Entry<K,V>[] data = newArray(4);
     
     /**
      * Umgeht die Probleme beim Anlegen eines neuen Array mit Typparameter.
      * 
      * @param length Groesse des Array
      * @return neues Array
      */
     @SuppressWarnings("unchecked")
 	private Entry<K,V>[] newArray(int length) {
     	return new Entry[length];
     }
     
     /* TODO: Die Klasse soll richtig vervollstaendigt werden.
      */
     
 	public int size() {
 		// TODO
 		return size;
 	}
 
 	public V put(K key, V value) {
 		// TODO
 		if(key == null) {
 			throw new NullPointerException("Key == null");
 		}
		if(size == data.length) adjustArrayLength();
 		int iOf = indexOf(key);
 		if(iOf == -1) {
 			data[size++] = new Entry<K,V>(key, value);
 			return null;
 		} else {
 			V valueBefore = data[iOf].getValue();
 			data[iOf] = new Entry<K,V>(key, value);
 			return valueBefore;
 		}
 	}
 
 	public V get(K key) {
 		// TODO
 		if(key == null) {
 			throw new NullPointerException("Key == null");
 		}
 		for(Entry<K,V> e : data) {
 			if(e != null) {
 				if(e.getKey().equals(key)) {
 					return e.getValue();
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean containsKey(K key) {
 		// TODO
 		if(key == null) {
 			throw new NullPointerException("Key == null");
 		}
 		/*
 		for(Entry<K,V> e : data) {
 			if(e != null) {
 				if(e.getKey() == key) {
 					return true;
 				}
 			}
 		}
 		return false;
 		*/
 		
 		if(indexOf(key) != -1) {
 			return true;
 		}
 		return false;
 	}
 
 	public V remove(K key) {
 		// TODO
 		if(key == null) {
 			throw new NullPointerException("Key == null");
 		}
 		int i = indexOf(key);
 		if(i != -1) {
 			V value = get(key);
 			if(value != null) {
 				for(int j = i;j<data.length-1;j++) {
 					data[j] = data[j+1];
 				}
 				size--;
 			}
 			return value; 
 		}
 		return null;
 	}
 
 	public List<K> keys() {
 		// TODO
 		List<K> liste = new ArrayList<K>();
 		for(Entry<K,V> e : data) {
 			if(e != null) {
 				liste.add(e.getKey());
 			}
 		}	
 		return liste;
 	}
 	
 	/**
 	* Gibt den Index des ersten Vorkommens von
 	* <tt>gesucht</tt> zurueck.
 	* @param gesucht Objekt das gesucht wird.
 	* @return Index des ersten Vorkommens oder -1 wenn nicht gefunden.
 	*/
 	public int indexOf(K gesucht) {
 		
 		for (int i=0;i<data.length;i++) {
 			if(data[i] != null) { 
 				if(data[i].getKey().equals(gesucht)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 
 	}
 
 	private void adjustArrayLength() {
 		// TODO Auto-generated method stub
 		Entry<K,V>[] data2 = newArray(size);
 		data2 = data.clone();
 		data = newArray(size*2);
 		for(int i=0;i<data2.length;i++) {
 			data[i] = data2[i];
 		}
 	}
     
 }
