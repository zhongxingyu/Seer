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
     
 	@Override
 	public int size() {
 		// TODO
 		return size;
 	}
 
 	@Override
 	public V put(K key, V value) {
 		// TODO
 		if(key == null) {
 			throw new NullPointerException("Key == null");
 		}
 		int iOf = indexOf(key);
 		if(iOf == -1) {
 			data[size++] = new Entry<K,V>(key, value);
			if(size-2 > 0) {
				return data[size-2].getValue();
			}
 		} else {
 			data[iOf] = new Entry<K,V>(key, value);
			if(iOf - 2 > 0) {
				return data[iOf-2].getValue();
			}
 		}
		return null;
 	}
 
 	@Override
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
 
 	@Override
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
 
 	@Override
 	public V remove(K key) {
 		// TODO
 		if(key == null) {
 			throw new NullPointerException("Key == null");
 		}
 		return null;
 	}
 
 	@Override
 	public List<K> keys() {
 		// TODO
 		return null;
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
 
     
     
 }
