 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.model2;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 /**
  * Keeps track of changes made to the model.  This is done to enable the 
  * undo/redo feature.
  * 
  * As changes are undone and redone, the id of each object may change.
  * For example, in the serializeddatastore plug-in, the id of each object
  * is a reference to the object itself, i.e. the java identity.  Unless
  * we keep a reference to these objects, which we don't, the identity of
  * objects will not be the same when the object is re-created.  (Even if
  * we kept a reference to an object, it is not possible to insert that
  * object back into the object store for various technical reasons).
  * If the datastore is a database, for example in the jdbcdatastore plug-in,
  * the id is automatically generated as a value of a unique column.
  * The database may decide to re-use the id of a delete row.
  * Therefore, this class never stores ids of objects that have been
  * deleted.  When an object is deleted, all old values that reference
  * the object are replaced with references to the delete entry.
  * This allows the data to be re-created correctly by the undo method.
  * 
  * @author Nigel Westbury
  */
 public class ChangeManager {
 	
 	public class UndoableChange {
 		private String description;
 		
 		/**
 		 * Vector of ChangeEntry objects.  Changes are added to
 		 * this vector in order.  If changes are undone, they must
 		 * be undone in reverse order, starting at the end of this
 		 * vector.
 		 */
 		private Vector<ChangeEntry> changes = new Vector<ChangeEntry>();	
 		
 		/**
 		 * Set the description of this change.
 		 */
 		public void setDescription(String description) {
 			this.description = description;
 		}
 		
 		/**
 		 * @return
 		 */
 		public String getDescription() {
 			return description;
 		}
 		
 		/**
 		 * Submit a series of updates, which have been stored,
 		 * to the datastore.  These updates carry out the reverse
 		 * of the updates stored.
 		 */
 		public void undoChanges() {
 			// Undo the changes in reverse order.
 			for (int i = changes.size()-1; i >= 0; i--) {
 				ChangeEntry changeEntry = changes.get(i);
 				changeEntry.undo();
 			}
 		}
 		
 		/**
 		 * @param newChangeEntry
 		 */
 		void addChange(ChangeEntry newChangeEntry) {
 			changes.add(newChangeEntry);
 		}
 		
 		/**
 		 * Called to decrement reference counts for proxy objects.
 		 */
 		void destroy() {
 			for (int i = 0; i < changes.size(); i--) {
 				ChangeEntry changeEntry = (ChangeEntry)changes.get(i);
 				changeEntry.destroy();
 			}
 		}
 	}
 	
 	abstract class ChangeEntry {
 		KeyProxy objectKeyProxy;
 		abstract void undo();
 		/**
 		 * This must be called and this must be overridden by any
 		 * object that holds proxies, otherwise proxies will never
 		 * be removed from the map.
 		 */
 		void destroy() {
 			ChangeManager.this.release(objectKeyProxy);
 		}
 	}
 	
 	class ChangeEntry_Update<V> extends ChangeEntry {
 		ScalarPropertyAccessor<V> propertyAccessor;
 		V oldValue = null;  // if not an extendable object
 		KeyProxy oldValueProxy = null; // If an extendable object
 		
 		void undo() {
 			ExtendableObject object = objectKeyProxy.key.getObject();  // efficient???
 			if (propertyAccessor.getClassOfValueObject().isAssignableFrom(ExtendableObject.class)) {
 				// If IObjectKey had a type parameter, we would not need
 				// this cast.
 				object.setPropertyValue(propertyAccessor, propertyAccessor.getClassOfValueObject().cast(oldValueProxy.key.getObject()));
 			} else {
 				object.setPropertyValue(propertyAccessor, oldValue);
 			}
 		}
 		
 		void destroy() {
 			super.destroy();
 			if (oldValueProxy != null) {
 				ChangeManager.this.release(oldValueProxy);
 			}
 			
 		}
 	}
 
 	class ChangeEntry_Insert extends ChangeEntry {
 		ExtendableObject parent;
 		ListPropertyAccessor<?> owningListProperty;
 		
 		void undo() {
 			// Delete the object.
 			ExtendableObject object = objectKeyProxy.key.getObject();  // efficient???
 			
 			// Delete the object from the datastore.
 			parent.getListPropertyValue(owningListProperty).remove(object);
 		}
 	}
 	
 	/**
 	 * 
 	 * @author Nigel
 	 *
 	 * @param <E> the type of the object being deleted
 	 */
 	class ChangeEntry_Delete<E extends ExtendableObject> extends ChangeEntry {
 		Object [] oldValues;
 		ExtendableObject parent;
 		ListPropertyAccessor<? super E> owningListProperty;
 		ExtendablePropertySet<? extends E> actualPropertySet;
 		
 		void undo() {
 			// Create the object in the datastore.
 			ExtendableObject object = parent.getListPropertyValue(owningListProperty).createNewElement(actualPropertySet, oldValues, false);
 
 			// Set the new object key back into the proxy.
 			// This ensures earlier changes to this object will
 			// be undone in this object.
 			if (objectKeyProxy.key != null) {
 				throw new RuntimeException("key proxy error");
 			}
 			objectKeyProxy.key = object.getObjectKey();
 			
 		}
 		
 		void destroy() {
 			super.destroy();
 			
 			for (int index = 0; index < oldValues.length; index++) {
 				if (oldValues[index] != null && oldValues[index] instanceof KeyProxy) {
 					ChangeManager.this.release((KeyProxy)oldValues[index]);
 				}
 				
 			}
 		}
 	}
 	
 	
 	/**
 	 * When we delete an object, we know that nothing in the
 	 * datastore references it.  However, there may be old
 	 * values that referenced it.  It is important that these
 	 * old values are updated to reference this deleted object.
 	 * Otherwise, if the object is re-created with a different
 	 * id then those old values cannot be restored correctly.
 	 */
 	private class KeyProxy {
 		IObjectKey key;
 		int refCount = 0;
 		
 		KeyProxy(IObjectKey key) {
 			this.key = key;
 		}
 	}
 	
 	private Map<IObjectKey, KeyProxy> keyProxyMap = new HashMap<IObjectKey, KeyProxy>();
 	
 	private UndoableChange currentUndoableChange = null;
 	
 	private KeyProxy getKeyProxy(IObjectKey objectKey) {
 		if (objectKey != null) {
 			KeyProxy keyProxy = (KeyProxy)keyProxyMap.get(objectKey);
 			if (keyProxy == null) {
 				keyProxy = new KeyProxy(objectKey);
 				keyProxyMap.put(objectKey, keyProxy);
 			}
 			return keyProxy;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * A significant percentage of the code in this class is
 	 * there to implement reference counting in the map so that
 	 * we do not end up with an enormous map of accumulated
 	 * unused stuff.  This method must be called whenever
 	 * an object containing references to KeyProxy objects
 	 * is discarded.
 	 * 
 	 * @param proxy A KeyProxy object to which a reference
 	 * 		is being discarded.
 	 */
 	private void release(KeyProxy proxy) {
 		if (--proxy.refCount == 0) {
 			if (proxy.key != null) {
 				keyProxyMap.remove(proxy.key);
 			}
 		}
 	}
 	
 	private UndoableChange getCurrentUndoableChange() {
 		if (currentUndoableChange == null) {
 			currentUndoableChange = new UndoableChange();
 		}
 		return currentUndoableChange;
 	}
 	/**
 	 * The property may be any property in the passed object.
 	 * The property may be defined in the actual class or
 	 * any super classes which the class extends.  The property
 	 * may also be a property in any extension class which extends
 	 * the class of this object or which extends any super class
 	 * of the class of this object.
 	 */
 	public <V> void processPropertyUpdate(
 			ExtendableObject object,
 			ScalarPropertyAccessor<V> propertyAccessor,
 			V oldValue,
 			V newValue) {
 
 		ChangeEntry_Update<V> newChangeEntry = new ChangeEntry_Update<V>();
 		
 		newChangeEntry.objectKeyProxy = getKeyProxy(object.getObjectKey());
 		
 		// Replace any keys with proxy keys
 		if (propertyAccessor.getClassOfValueObject().isAssignableFrom(ExtendableObject.class)) {
 			newChangeEntry.oldValueProxy = getKeyProxy((IObjectKey)oldValue);
 		} else {
 			newChangeEntry.oldValue = oldValue;
 		}
 		
 		newChangeEntry.propertyAccessor = propertyAccessor;
 		
 		getCurrentUndoableChange().addChange(newChangeEntry);
 	}
 
 	public void processObjectCreation(
 			ExtendableObject parent,
 			ListPropertyAccessor owningListProperty,
 			ExtendableObject newObject) {
 		
 		ChangeEntry_Insert newChangeEntry = new ChangeEntry_Insert();
 		
 		newChangeEntry.objectKeyProxy = getKeyProxy(newObject.getObjectKey());
 		newChangeEntry.parent = parent;
 		newChangeEntry.owningListProperty = owningListProperty;
 		
 		getCurrentUndoableChange().addChange(newChangeEntry);
 	}
 	
 	public <E extends ExtendableObject> void processObjectDeletion(
 			ExtendableObject parent,
 			ListPropertyAccessor<E> owningListProperty,
 			E oldObject) {
 		
 		// TODO: We must also process objects owned by this object in a recursive
 		// manner.  Otherwise, undoing the deletion of an object will not restore
 		// any objects owned by that object.
 		
 		ChangeEntry_Delete<E> newChangeEntry = new ChangeEntry_Delete<E>();
 		
 		newChangeEntry.objectKeyProxy = getKeyProxy(oldObject.getObjectKey());
 		newChangeEntry.parent = parent;
 		newChangeEntry.owningListProperty = owningListProperty;
		newChangeEntry.actualPropertySet = owningListProperty.getElementPropertySet().getActualPropertySet(oldObject.getClass());
 		
 		// The actual key is no longer valid, so we remove the proxy
 		// from the map that maps object keys to proxies.
 		// For safety we also set this to null.
 		keyProxyMap.remove(newChangeEntry.objectKeyProxy.key);
 		newChangeEntry.objectKeyProxy.key = null;
 		
 		// Save all the property values from the deleted object.
 		// We need these to re-create the object if this change
 		// is undone.
 		int count = newChangeEntry.actualPropertySet.getScalarProperties3().size();
 		newChangeEntry.oldValues = new Object[count];
 		int index = 0;
 		for (ScalarPropertyAccessor<?> propertyAccessor: newChangeEntry.actualPropertySet.getScalarProperties3()) {
 			if (index != propertyAccessor.getIndexIntoScalarProperties()) {
 				throw new RuntimeException("index mismatch");
 			}
 			newChangeEntry.oldValues[index++] = oldObject.getPropertyValue(propertyAccessor);
 		}
 		
 		getCurrentUndoableChange().addChange(newChangeEntry);
 	}
 
 	public void setUndoableChange() {
 		currentUndoableChange = new UndoableChange();
 	}
 
 	public UndoableChange takeUndoableChange() {
 		UndoableChange result = currentUndoableChange;
 		currentUndoableChange = null;
 		return result;
 	}
 }
