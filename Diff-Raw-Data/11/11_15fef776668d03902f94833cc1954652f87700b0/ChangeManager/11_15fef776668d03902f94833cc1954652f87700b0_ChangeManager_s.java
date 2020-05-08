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
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 
 /**
  * Keeps track of changes made to the model.
  * 
  * There are two purposes for keeping track of changes:
  * <UL>
  * <LI>Support for the undo/redo feature</LI>
  * <LI>Changes to any underlying database can be made more efficient
  * 		by combining changes.  For example, if an object is created
  * 		and then properties are set, and a SQL database is used for storage,
  * 		what would be an insert and multiple updates can be merged into
  * 		a single insert.</LI>
  * <UL>
  * 
  * This class solves the first of the two above problems only.
  * <P>
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
  * <P>
 * The changes are made to the database by this class as changes are
  * passed to this class.  Although it may be more efficient to execute
  * the changes in the database when they are committed, this would result
  * in changes not being reflected in query statements that are submitted
  * after the changes were made by the plug-in but before the plug-in
  * committed the changes.
  * 
  *  @author Nigel Westbury
  *
  */
 public class ChangeManager {
 	
 	private final int MAXIMUM_UNDOABLE_CHANGES = 50;
 	
 	/**
 	 * Array of previous changes.  These changes may be
 	 * undone using the 'undo' action.  The last change made
 	 * is last in the array, so changes are added to the end
 	 * of the array and undone from the end of the array.
 	 */
 	private Vector previousChanges = new Vector();
 	
 	/**
 	 * Array of undone changes.  These changes may be redone
 	 * using the 'redo' action.  As a change is undone, it is
 	 * added to the end of this array.  Changes are redone starting
 	 * at the end of this array.
 	 */
 	private Vector undoneChanges = new Vector();
 	
 	private class UndoableChange {
 		private String description;
 		
 		/**
 		 * Vector of ChangeEntry objects.  Changes are added to
 		 * this vector in order.  If changes are undone, they must
 		 * be undone in reverse order, starting at the end of this
 		 * vector.
 		 */
 		private Vector changes = new Vector();	
 		
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
 				ChangeEntry changeEntry = (ChangeEntry)changes.get(i);
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
 	
 	class ChangeEntry_Update extends ChangeEntry {
 		PropertyAccessor propertyAccessor;
 		//		PropertySet actualPropertySet;
 		Object oldValue;
 		//		Object newValue;
 		void undo() {
 			ExtendableObject object = objectKeyProxy.key.getObject();  // efficient???
 			object.setPropertyValue(propertyAccessor, oldValue);
 		}
 		
 		void destroy() {
 			super.destroy();
 			if (oldValue instanceof KeyProxy) {
 				ChangeManager.this.release((KeyProxy)oldValue);
 			}
 			
 		}
 	}
 	
 	class ChangeEntry_Insert extends ChangeEntry {
 		ExtendableObject parent;
 		PropertyAccessor owningListProperty;
 		
 		void undo() {
 			// Delete the object.
 			ExtendableObject object = objectKeyProxy.key.getObject();  // efficient???
 			
 			// Delete the object from the datastore.
 			parent.deleteObject(owningListProperty, object);
 		}
 	}
 	
 	class ChangeEntry_Delete extends ChangeEntry {
 		Object [] oldValues;
 		ExtendableObject parent;
 		PropertyAccessor owningListProperty;
 		PropertySet actualPropertySet;
 		
 		void undo() {
 			// Create the object in the datastore.
 			ExtendableObject object = parent.createObject(owningListProperty, actualPropertySet);
 			
 			// Set the properties to the values that were set before
 			// the object was deleted.
 			int index = 0;
 			for (Iterator iter = actualPropertySet.getPropertyIterator3(); iter.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 				if (propertyAccessor.isScalar()) {
 					if (index != propertyAccessor.getIndexIntoScalarProperties()) {
 						throw new RuntimeException("index mismatch");
 					}
 					object.setPropertyValue(propertyAccessor, oldValues[index++]);
 				}
 			}
 			
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
	/*	
	 public ChangeManager(ISessionManager sessionManager) {
	 this.sessionManager = sessionManager;
	 }
	 */
 	
 	
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
 	
 	/**
 	 * Map IObjectKey to KeyProxy
 	 */
 	private Map keyProxyMap = new HashMap();
 	
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
 	
 	private UndoableChange currentUndoableChange = null;
 	
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
 	public void processPropertyUpdate(
 			ExtendableObject object,
 			PropertyAccessor propertyAccessor,
 			Object oldValue,
 			Object newValue) {
 		
 		ChangeEntry_Update newChangeEntry = new ChangeEntry_Update();
 		
 		newChangeEntry.objectKeyProxy = getKeyProxy(object.getObjectKey());
 		
 		// Replace any keys with proxy keys
 		if (propertyAccessor.getValueClass().isAssignableFrom(ExtendableObject.class)) {
 			newChangeEntry.oldValue = getKeyProxy((IObjectKey)oldValue);
 			//			newChangeEntry.newValue = getKeyProxy((IObjectKey)newValue);
 		} else {
 			newChangeEntry.oldValue = oldValue;
 			//			newChangeEntry.newValue = newValue;
 		}
 		
 		newChangeEntry.propertyAccessor = propertyAccessor;
 		//		newChangeEntry.actualPropertySet = PropertySet.getPropertySet(object.getClass());
 		
 		getCurrentUndoableChange().addChange(newChangeEntry);
 		
 	}
 	
 	public void processObjectCreation(
 			ExtendableObject parent,
 			PropertyAccessor owningListProperty,
 			ExtendableObject newObject) {
 		
 		ChangeEntry_Insert newChangeEntry = new ChangeEntry_Insert();
 		
 		newChangeEntry.objectKeyProxy = getKeyProxy(newObject.getObjectKey());
 		newChangeEntry.parent = parent;
 		newChangeEntry.owningListProperty = owningListProperty;
 		
 		getCurrentUndoableChange().addChange(newChangeEntry);
 	}
 	
 	public void processObjectDeletion(
 			ExtendableObject parent,
 			PropertyAccessor owningListProperty,
 			ExtendableObject oldObject) {
 		
 		ChangeEntry_Delete newChangeEntry = new ChangeEntry_Delete();
 		
 		newChangeEntry.objectKeyProxy = getKeyProxy(oldObject.getObjectKey());
 		newChangeEntry.parent = parent;
 		newChangeEntry.owningListProperty = owningListProperty;
 		newChangeEntry.actualPropertySet = PropertySet.getPropertySet(oldObject.getClass());
 		
 		// The actual key is no longer valid, so we remove the proxy
 		// from the map that maps object keys to proxies.
 		// For safety we also set this to null.
 		keyProxyMap.remove(newChangeEntry.objectKeyProxy.key);
 		newChangeEntry.objectKeyProxy.key = null;
 		
 		// Save all the property values from the deleted object.
 		// We need these to re-create the object if this change
 		// is undone.
 		int count = 0;
 		for (Iterator iter = newChangeEntry.actualPropertySet.getPropertyIterator3(); iter.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 			if (propertyAccessor.isScalar()) {
 				count++;
 			}
 		}
 		newChangeEntry.oldValues = new Object[count];
 		int index = 0;
 		for (Iterator iter = newChangeEntry.actualPropertySet.getPropertyIterator3(); iter.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 			if (propertyAccessor.isScalar()) {
 				if (index != propertyAccessor.getIndexIntoScalarProperties()) {
 					throw new RuntimeException("index mismatch");
 				}
 				newChangeEntry.oldValues[index++] = oldObject.getPropertyValue(propertyAccessor);
 			}
 		}
 		
 		getCurrentUndoableChange().addChange(newChangeEntry);
 	}
 	
 	/**
 	 * Register the end of a set of changes that constitute
 	 * a single undo/redo change.
 	 * <P>
 	 * If no changes have been made to the datastore since
 	 * the last time this method was called then this method
 	 * does nothing.  No change is registered and the change
 	 * will no appear in the undo/redo list.
 	 * 
 	 * @param description The name given to the change.
 	 * 		This name should be localized text.
 	 */
 	public void registerChanges(String description) {
 		if (currentUndoableChange != null) {
 			currentUndoableChange.setDescription(description);
 			
 			// Add to our list of changes.
 			// We must also clear out undone changes because
 			// once a new change has been made, any undone changes
 			// cannot be redone.
 			previousChanges.add(currentUndoableChange);
 			undoneChanges.clear();
 			
 			currentUndoableChange = null;
 			
 			// Check the number of undoable changes is not over the limit.
 			// If it is, delete the oldest change.
 			if (previousChanges.size() > MAXIMUM_UNDOABLE_CHANGES) {
 				((UndoableChange)previousChanges.get(0)).destroy();
 				previousChanges.remove(0);
 			}
 		}
 	}
 	
 	/**
 	 * @return If there are changes that can be undone,
 	 * 		the description of the last change,
 	 * 		otherwise null.
 	 */
 	public String getUndoDescription() {
 		if (previousChanges.size() > 0) {
 			return ((UndoableChange)previousChanges.lastElement()).getDescription();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * @return If there are undone changes that can be redone,
 	 * 		the description of the last undone change,
 	 * 		otherwise null.
 	 */
 	public String getRedoDescription() {
 		if (undoneChanges.size() > 0) {
 			return ((UndoableChange)undoneChanges.lastElement()).getDescription();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 */
 	public void undoChange() {
 		
 		UndoableChange lastChange = (UndoableChange)previousChanges.lastElement();
 		
 		if (JMoneyPlugin.DEBUG) System.out.println("start undo of changes: " + lastChange.getDescription());
 		lastChange.undoChanges();
 		if (JMoneyPlugin.DEBUG) System.out.println("finish undo of changes: " + lastChange.getDescription());
 		
 		// The above call to undoChanges sent changes to the
 		// datastore.  These changes are stored in the
 		// current UndoableChange object.  That object must be
 		// saved because, by undoing the changes in that object,
 		// we are re-doing the original changes.
 		// We put this change into a separate list.  This is the
 		// list of 'undo' changes that must be themselves 'undone'
 		// if we want to redo the change.
 		
 		previousChanges.remove(previousChanges.size()-1);
 		
 		// Add to our list of undone changes.
 		currentUndoableChange.setDescription(lastChange.getDescription());
 		undoneChanges.add(currentUndoableChange);
 		
 		currentUndoableChange = null;
 	}
 	
 	/**
 	 */
 	public void redoChange() {
 		UndoableChange nextChange = (UndoableChange)undoneChanges.lastElement();
 		nextChange.undoChanges();
 		
 		// The above call to undoChanges sent changes to the
 		// datastore.  These changes are stored in the
 		// current UndoableChange object.  That object must be
 		// saved because, by undoing the changes in the redo object,
 		// we are again undoing the original changes.
 		
 		undoneChanges.remove(undoneChanges.size()-1);
 		
 		// Add back to our list of past changes.
 		currentUndoableChange.setDescription(nextChange.getDescription());
 		previousChanges.add(currentUndoableChange);
 		
 		currentUndoableChange = null;
 	}
 	
 }
