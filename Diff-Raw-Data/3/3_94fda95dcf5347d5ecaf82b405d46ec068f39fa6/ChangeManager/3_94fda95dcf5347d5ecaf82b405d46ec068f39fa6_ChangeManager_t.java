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
 
 import java.util.Collection;
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
 
 		KeyProxy(IObjectKey key) {
 			this.key = key;
 		}
 	}
 
 	/*
 	 * If there are no references to the KeyProxy then this means there are no changes that
 	 * need the key proxy to undo the change.  The entry can be removed from the map.
 	 * Thus we use a map with weak value references.
 	 */
 	private WeakValuedMap<IObjectKey, KeyProxy> keyProxyMap = new WeakValuedMap<IObjectKey, KeyProxy>();
 
 	private UndoableChange currentUndoableChange = null;
 
 	public class UndoableChange {
 		/**
 		 * Vector of ChangeEntry objects.  Changes are added to
 		 * this vector in order.  If changes are undone, they must
 		 * be undone in reverse order, starting at the end of this
 		 * vector.
 		 */
 		private Vector<ChangeEntry> changes = new Vector<ChangeEntry>();
 
 		/**
 		 * Submit a series of updates, which have been stored,
 		 * to the datastore.  These updates carry out the reverse
 		 * of the updates stored.
 		 */
 		public void undoChanges() {
 			// Undo the changes in reverse order.
 			for (int i = changes.size() - 1; i >= 0; i--) {
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
 	}
 
 	/**
 	 * Base class for all objects that represent a component of
 	 * a change.  Derived classes represent property changes,
 	 * insertion of new objects, and deletion of objects.
 	 * 
 	 * These objects have only a constructor and the <code>undo</code> method.
 	 * Once the <code>undo</code> is called the object is dead.
 	 */
 	abstract class ChangeEntry {
 		abstract void undo();
 	}
 
 	/**
 	 * A ChangeEntry object for an update to a scalar property (excluding
 	 * scalar properties that are references to extendable objects).
 	 *
 	 * @param <V>
 	 */
 	class ChangeEntry_UpdateScalar<V> extends ChangeEntry {
 		private KeyProxy objectKeyProxy;
 		private ScalarPropertyAccessor<V> propertyAccessor;
 		private V oldValue = null;
 
 		ChangeEntry_UpdateScalar(KeyProxy objectKeyProxy,
 				ScalarPropertyAccessor<V> propertyAccessor, V oldValue) {
 			this.objectKeyProxy = objectKeyProxy;
 			this.propertyAccessor = propertyAccessor;
 			this.oldValue = oldValue;
 		}
 
 		void undo() {
 			ExtendableObject object = objectKeyProxy.key.getObject(); // efficient???
 			object.setPropertyValue(propertyAccessor, oldValue);
 		}
 	}
 
 	/**
 	 * A ChangeEntry object for an update to a scalar property that is a
 	 * reference to an extendable object.
 	 *
 	 * @param <E>
 	 */
 	// TODO: E should be bounded to classes that extend ExtendableObject.
 	// However, this method does not currently make use of such bounding,
 	// and to do that we would have to push back seperate methods for
 	// reference properties and other scalar properties.
 	class ChangeEntry_UpdateReference<E> extends ChangeEntry {
 		private KeyProxy objectKeyProxy;
 		private ScalarPropertyAccessor<E> propertyAccessor;
 		private KeyProxy oldValueProxy = null;
 
 		ChangeEntry_UpdateReference(KeyProxy objectKeyProxy,
 				ScalarPropertyAccessor<E> propertyAccessor,
 				KeyProxy oldValueProxy) {
 			this.objectKeyProxy = objectKeyProxy;
 			this.propertyAccessor = propertyAccessor;
 			this.oldValueProxy = oldValueProxy;
 		}
 
 		void undo() {
 			ExtendableObject object = objectKeyProxy.key.getObject(); // efficient???
 			// If IObjectKey had a type parameter, we would not need
 			// this cast.
 			object.setPropertyValue(propertyAccessor, propertyAccessor
 					.getClassOfValueObject()
 					.cast(oldValueProxy.key.getObject()));
 		}
 	}
 
 	class ChangeEntry_Insert extends ChangeEntry {
 		private KeyProxy parentKeyProxy;
 		private ListPropertyAccessor<?> owningListProperty;
 		private KeyProxy objectKeyProxy;
 
 		ChangeEntry_Insert(KeyProxy parentKeyProxy,
 				ListPropertyAccessor<?> owningListProperty,
 				KeyProxy objectKeyProxy) {
 			this.parentKeyProxy = parentKeyProxy;
 			this.owningListProperty = owningListProperty;
 			this.objectKeyProxy = objectKeyProxy;
 		}
 
 		void undo() {
 			// Delete the object.
 			ExtendableObject object = objectKeyProxy.key.getObject(); // efficient???
 			ExtendableObject parent = parentKeyProxy.key.getObject();
 
 			// Delete the object from the datastore.
 			parent.getListPropertyValue(owningListProperty).remove(object);
 		}
 	}
 
 	/**
 	 * @param <E> the type of the object being deleted
 	 */
 	class ChangeEntry_Delete<E extends ExtendableObject> extends ChangeEntry {
 		private Object[] oldValues;
 		private Collection<ExtensionPropertySet<?>> nonDefaultExtensions;
 		
 		private KeyProxy parentKeyProxy;
 		private ListPropertyAccessor<E> owningListProperty;
 		private KeyProxy objectKeyProxy;
 		private ExtendablePropertySet<? extends E> actualPropertySet;
 
 		ChangeEntry_Delete(KeyProxy parentKeyProxy,
 				ListPropertyAccessor<E> owningListProperty, E oldObject) {
 			this.parentKeyProxy = parentKeyProxy;
 			this.owningListProperty = owningListProperty;
 
 			this.objectKeyProxy = getKeyProxy(oldObject.getObjectKey());
 			this.actualPropertySet = owningListProperty.getElementPropertySet()
 					.getActualPropertySet(
 							(Class<? extends E>) oldObject.getClass());
 
 			/*
 			 * Save all the property values from the deleted object. We need
 			 * these to re-create the object if this change is undone.
 			 */
 			nonDefaultExtensions = oldObject.getExtensions();
 
 			int count = actualPropertySet.getScalarProperties3().size();
 			oldValues = new Object[count];
 			int index = 0;
 			for (ScalarPropertyAccessor<?> propertyAccessor : actualPropertySet
 					.getScalarProperties3()) {
 				if (index != propertyAccessor.getIndexIntoScalarProperties()) {
 					throw new RuntimeException("index mismatch");
 				}
 
 				Object value = oldObject.getPropertyValue(propertyAccessor);
 				if (value instanceof ExtendableObject) {
 					/*
 					 * We can't store extendable objects or even the object keys
 					 * because those may not remain valid (the referenced object may
 					 * be deleted). We store instead a KeyProxy. If the referenced
 					 * object is later deleted, then un-deleted using an undo
 					 * operation, then this change is also undone, the key proxy
 					 * will give us the new object key for the referenced object.
 					 */
 					IObjectKey objectKey = ((ExtendableObject) value)
 							.getObjectKey();
 					oldValues[index++] = getKeyProxy(objectKey);
 				} else {
 					oldValues[index++] = value;
 				}
 			}
 		}
 
 		void undo() {
 			/* Create the object in the datastore.
 			 * However, we must first convert the key proxies back to keys before passing
 			 * on to the constructor.
 			 */
 			IValues oldValues2 = new IValues() {
 
 				public <V> V getScalarValue(ScalarPropertyAccessor<V> propertyAccessor) {
 					return propertyAccessor.getClassOfValueObject().cast(oldValues[propertyAccessor.getIndexIntoScalarProperties()]);
 				}
 
 				public IObjectKey getReferencedObjectKey(
 						ScalarPropertyAccessor<? extends ExtendableObject> propertyAccessor) {
					KeyProxy keyProxy = (KeyProxy)oldValues[propertyAccessor.getIndexIntoScalarProperties()];
					return keyProxy == null ? null : keyProxy.key;
 				}
 
 				public <E2 extends ExtendableObject> IListManager<E2> getListManager(
 						IObjectKey listOwnerKey,
 						ListPropertyAccessor<E2> listAccessor) {
 					return listOwnerKey.constructListManager(listAccessor);
 				}
 
 				public Collection<ExtensionPropertySet<?>> getNonDefaultExtensions() {
 					return nonDefaultExtensions;
 				}
 			};
 			
 			ExtendableObject parent = parentKeyProxy.key.getObject();
 			ExtendableObject object = parent.getListPropertyValue(
 					owningListProperty).createNewElement(actualPropertySet,
 					oldValues2, false);
 
 			/*
 			 * Set the new object key back into the proxy. This ensures that
 			 * earlier changes to this object will be undone in this object. We
 			 * must also add to our map so that if further changes are made that
 			 * reference this object key, they will be using the same proxy.
 			 */
 			if (objectKeyProxy.key != null) {
 				throw new RuntimeException("internal error - key proxy error");
 			}
 			objectKeyProxy.key = object.getObjectKey();
 			keyProxyMap.put(objectKeyProxy.key, objectKeyProxy);
 		}
 	}
 
 	private KeyProxy getKeyProxy(IObjectKey objectKey) {
 		if (objectKey != null) {
 			KeyProxy keyProxy = keyProxyMap.get(objectKey);
 			if (keyProxy == null) {
 				keyProxy = new KeyProxy(objectKey);
 				keyProxyMap.put(objectKey, keyProxy);
 			}
 			return keyProxy;
 		} else {
 			return null;
 		}
 	}
 
 	private void addUndoableChangeEntry(ChangeEntry changeEntry) {
 		if (currentUndoableChange != null) {
 			currentUndoableChange.addChange(changeEntry);
 		}
 		
 		/*
 		 * If changes are made while currentUndoableChange is set to null then
 		 * the changes are not undoable. This is supported but is not common. It
 		 * is typically used for very large transactions such as imports of
 		 * entire databases by the copier plug-in.
 		 */
 		// TODO: We should really clear out the change history as
 		// prior changes are not likely to be undoable after this
 		// change has been applied. 
 	}
 
 	/**
 	 * The property may be any property in the passed object.
 	 * The property may be defined in the actual class or
 	 * any super classes which the class extends.  The property
 	 * may also be a property in any extension class which extends
 	 * the class of this object or which extends any super class
 	 * of the class of this object.
 	 */
 	public <V> void processPropertyUpdate(ExtendableObject object,
 			ScalarPropertyAccessor<V> propertyAccessor, V oldValue, V newValue) {
 
 		// Replace any keys with proxy keys
 		if (propertyAccessor.getClassOfValueObject().isAssignableFrom(ExtendableObject.class)) {
 			ChangeEntry newChangeEntry = new ChangeEntry_UpdateReference<V>(
 					getKeyProxy(object.getObjectKey()), propertyAccessor, getKeyProxy((IObjectKey) oldValue));
 
 			addUndoableChangeEntry(newChangeEntry);
 		} else {
 			ChangeEntry newChangeEntry = new ChangeEntry_UpdateScalar<V>(
 					getKeyProxy(object.getObjectKey()), propertyAccessor,
 					oldValue);
 
 			addUndoableChangeEntry(newChangeEntry);
 		}
 	}
 
 	public void processObjectCreation(ExtendableObject parent,
 			ListPropertyAccessor<?> owningListProperty, ExtendableObject newObject) {
 
 		ChangeEntry newChangeEntry = new ChangeEntry_Insert(getKeyProxy(parent
 				.getObjectKey()), owningListProperty, getKeyProxy(newObject
 				.getObjectKey()));
 
 		addUndoableChangeEntry(newChangeEntry);
 	}
 
 	/**
 	 * Processes the deletion of an object. This involves adding the property
 	 * values to the change list so that the deletion can be undone.
 	 * <P>
 	 * Also we must call this method recursively on any objects contained in any
 	 * list properties in the object. This is because this object 'owns' such
 	 * objects, and so those objects will also be deleted and must be restored
 	 * if this operation is undone.
 	 * 
 	 * @param <E>
 	 * @param parent
 	 * @param owningListProperty
 	 * @param oldObject
 	 */
 	public <E extends ExtendableObject> void processObjectDeletion(
 			ExtendableObject parent,
 			ListPropertyAccessor<E> owningListProperty, E oldObject) {
 
 		/*
 		 * We must also process objects owned by this object in a recursive
 		 * manner. Otherwise, undoing the deletion of an object will not restore
 		 * any objects owned by that object.
 		 */
 		for (ListPropertyAccessor<?> subList : PropertySet.getPropertySet(oldObject.getClass()).getListProperties3()) {
 			processObjectListDeletion(oldObject, subList);
 		}
 
 		ChangeEntry_Delete<E> newChangeEntry = new ChangeEntry_Delete<E>(
 				getKeyProxy(parent.getObjectKey()), owningListProperty,
 				oldObject);
 
 		/*
 		 * The actual key is no longer valid, so we remove the proxy from the
 		 * map that maps object keys to proxies. For safety we also set this to
 		 * null.
 		 * 
 		 * Note that the proxy itself still exists.  If this deletion is later
 		 * undone then the object is re-inserted and will be given a new object
 		 * key by the underlying datastore.  That new object key will then be set in
 		 * the proxy and the proxy will be added back to the map with the new
 		 * object key.   
 		 */
 
 		// Remove from the map.
 		keyProxyMap.remove(newChangeEntry.objectKeyProxy.key);
 
 		// This line may not be needed, as the key should never
 		// be accessed if the proxy represents a key that currently
 		// does not exist in the datastore.  This line is here for
 		// safety only.
 		newChangeEntry.objectKeyProxy.key = null;
 
 		addUndoableChangeEntry(newChangeEntry);
 	}
 
 	/**
 	 * Helper function to process the deletion of all objects in a list
 	 * property.
 	 * 
 	 * @param <E>
 	 * @param parent the object containing the list
 	 * @param listProperty the property accessor for the list
 	 */
 	private <E extends ExtendableObject> void processObjectListDeletion(ExtendableObject parent, ListPropertyAccessor<E> listProperty) {
 		for (E childObject : parent.getListPropertyValue(listProperty)) {
 			processObjectDeletion(parent, listProperty, childObject);
 		}
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
