 package com.sis.util;
 
import java.io.Serializable;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @note not optimized
  * @note could be broken for smp/mt environments
  * @note complexity of O(n)
  * @note incomplete
  * 
  * @author CR
  *
  * @param <K> key type
  * @param <V> value type
  */
public class InjectableLinkedMap<K, V> implements Map<K, V>, Serializable, Cloneable {
	private static final long serialVersionUID = -2756532148353187993L;

 	private static class Link<K, V> implements Map.Entry<K, V> {
 		private K key	= null;
 		private V value	= null;
 		
 		private Link<K, V>
 			previous 	= null,
 			next 		= null;
 
 		public Link(K key, V value) {
 			setKey(key);
 			setValue(value);
 		}
 
 		public Link() {
 		}
 
 		public void clear() {
 			key = null;
 			value = null;
 
 			previous = null;
 			next = null;
 		}
 
 		@SuppressWarnings("unused")
 		public boolean equals(Link<K, V> other) {
 			return key.equals(other.key) && value.equals(other.value);
 		}
 
 		public K getKey() {
 			return key;
 		}
 
 		public void setKey(K key) {
 			this.key = key;
 		}
 
 		public V getValue() {
 			return value;
 		}
 
 		public V setValue(V value) {
 			this.value = value;
 			
 			return value;
 		}
 
 		@SuppressWarnings("unused")
 		public Link<K, V> getPrevious() {
 			return previous;
 		}
 
 		public void setPrevious(Link<K, V> previous) {
 			this.previous = previous;
 		}
 
 		public Link<K, V> getNext() {
 			return next;
 		}
 
 		public void setNext(Link<K, V> next) {
 			this.next = next;
 		}
 	}
 
	private transient final AtomicInteger	size 		= new AtomicInteger(0);
	private transient final Link<K, V> 		rootLink 	= new Link<K, V>();
	private transient Link<K, V> 			lastLink 	= rootLink;
 
 	@Override
 	public void clear() {
 		synchronized (rootLink) {
 			size.set(0);
 			rootLink.clear();
 			lastLink = rootLink;
 		}
 	}
 
 	@Override
 	public boolean containsKey(Object arg0) {
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			if (current.getKey().equals(arg0)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean containsValue(Object arg0) {
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			if (current.getValue().equals(arg0)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public Set<java.util.Map.Entry<K, V>> entrySet() {
 		LinkedHashSet<Map.Entry<K, V>> set = new LinkedHashSet<>();
 		
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			set.add(current);
 		}
 		
 		return set;
 	}
 
 	@Override
 	public V get(Object arg0) {
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			if (current.getKey().equals(arg0)) {
 				return current.getValue();
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size.get() == 0;
 	}
 
 	@Override
 	public Set<K> keySet() {
 		Set<K> temp = new LinkedHashSet<K>();
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			temp.add(current.getKey());
 		}
 
 		return temp;
 	}
 
 	private void addLinkToChain(Link<K, V> link) {
 		synchronized (lastLink) {
 			link.setPrevious(lastLink);
 			lastLink.setNext(link);
 			lastLink = link;
 
 			size.incrementAndGet();
 		}
 	}
 
 	@Override
 	public V put(K arg0, V arg1) {
 		V previousValue = null;
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			if (current.getKey().equals(arg0)) {
 				previousValue = current.getValue();
 				current.setValue(arg1);
 			}
 		}
 
 		if (previousValue == null) {
 			addLinkToChain(new Link<K, V>(arg0, arg1));
 		}
 
 		return previousValue;
 	}
 
 	@Override
 	public void putAll(Map<? extends K, ? extends V> arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public synchronized void putAfter(K needle, K arg0, V arg1) {
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			if (current.getKey().equals(needle)) {
 				synchronized (current) {
 					Link<K, V> newLink = new Link<K, V>(arg0, arg1);
 
 					newLink.setNext(current.getNext());
 					newLink.setPrevious(current);
 					current.setNext(newLink);
 
 					if (current == lastLink) {
 						lastLink = newLink;
 					}
 				}
 
 				size.incrementAndGet();
 				break;
 			}
 		}
 	}
 
 	@Override
 	public V remove(Object arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int size() {
 		return size.get();
 	}
 
 	@Override
 	public Collection<V> values() {
 		LinkedList<V> temp = new LinkedList<V>();
 		Link<K, V> current = rootLink;
 
 		while ((current = current.getNext()) != null) {
 			temp.add(current.getValue());
 		}
 
 		return temp;
 	}
 
 }
