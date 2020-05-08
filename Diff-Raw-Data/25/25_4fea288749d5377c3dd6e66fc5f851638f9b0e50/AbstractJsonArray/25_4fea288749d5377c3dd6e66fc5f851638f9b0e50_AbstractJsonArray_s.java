 package org.getcomposer.core.entities;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.simple.JSONArray;
 
 
 public abstract class AbstractJsonArray<V> extends JsonEntity implements JsonCollection, Iterable<V> {
 	
 	protected List<V> values = new ArrayList<V>();
 	
 	private transient PropertyChangeListener propListener = new PropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent e) {
 			int index = values.indexOf(e.getSource());
 			firePropertyChange("#" + index + "." + e.getPropertyName(), e.getOldValue(), e.getNewValue());
 		}
 	};
 	
 	@SuppressWarnings("unchecked")
 	protected void parse(Object obj) {
 		clear();
 		if (obj instanceof JSONArray) {
 			for (Object item : (List<Object>)obj) {
 				add((V)item);
 			}
 //			List<V> oldValues = (List<V>) ((ArrayList<V>)values).clone();
 //			values = (List<V>)obj;
 //			firePropertyChange("values", oldValues, values);
 		}
 	}
 
 	public Object prepareJson(LinkedList<String> fields) {
 		LinkedList<Object> out = new LinkedList<Object>();
 		for (V val : values) {
 			if (val == null) {
 				continue;
 			}
 			out.add(prepareJsonValue(val));
 		}
 		return out;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.getcomposer.core.entities.JsonCollection#size()
 	 */
 	public int size() {
 		return values.size();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.getcomposer.core.entities.JsonCollection#clear()
 	 */
 	public void clear() {
 		while (values.size() > 0) {
 			remove(get(0));
 		}
 	}
 	
 	/**
 	 * Checks whether the value is present.
 	 * 
 	 * @param value the value to check
 	 * @return true if present, false if not
 	 */
 	public boolean has(V value) {
 		return values.contains(value);
 	}
 	
 	/**
 	 * Returns the passed object
 	 * 
 	 * @param packageName
 	 * @return the dependency
 	 */
 	public V get(int index) {
 		return values.get(index);
 	}
 	
 	public int indexOf(V value) {
 		return values.indexOf(value);
 	}
 	
 	/**
 	 * Adds a value to the receiver's collection
 	 * 
 	 * @param value the new value
 	 */
 	public void add(V value) {
 		values.add(value);
 		
 		if (value instanceof JsonEntity) {
 			((JsonEntity)value).addPropertyChangeListener(propListener);
 		}
 		
 		firePropertyChange("#" + (values.size() - 1), null, value);
 	}
 	
 	/**
 	 * Removes a value from the receiver's collection
 	 * 
 	 * @param value the value to remove
 	 */
 	public void remove(V value) {
 		int index = values.indexOf(value);
 		values.remove(value);
 		
 		if (value instanceof JsonEntity) {
 			((JsonEntity)value).removePropertyChangeListener(propListener);
 		}
 		
 		firePropertyChange("#" + index, value, null);
 	}
 	
 	/**
 	 * If oldValue exists, replaces with newValue
 	 * 
 	 * @param oldValue
 	 * @param newValue
 	 */
 	public void replace(V oldValue, V newValue) {
 		if (values.contains(oldValue)) {
 			int index = values.indexOf(oldValue);
 			values.remove(oldValue);
 			values.add(index, newValue);
 			
 			if (oldValue instanceof JsonEntity) {
 				((JsonEntity)oldValue).removePropertyChangeListener(propListener);
 			}
 			
 			if (newValue instanceof JsonEntity) {
 				((JsonEntity)newValue).removePropertyChangeListener(propListener);
 			}
 			
 			firePropertyChange("#" + index, oldValue, newValue);
 		}
 	}
 	
	@SuppressWarnings("unchecked")
	public V[] toArray() {
		return (V[]) values.toArray();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Iterable#iterator()
 	 */
 	public Iterator<V> iterator() {
 		return values.iterator();
 	}
 }
