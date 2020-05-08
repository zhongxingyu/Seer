 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2005 Nigel Westbury <westbury@users.sourceforge.net>
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
 
 package net.sf.jmoney.isolation;
 
 import java.util.AbstractCollection;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.EntryInfo;
 import net.sf.jmoney.fields.SessionInfo;
 import net.sf.jmoney.fields.TransactionInfo;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IDataManager;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.ISessionChangeFirer;
 import net.sf.jmoney.model2.ObjectCollection;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeListener;
 import net.sf.jmoney.model2.Transaction;
 
 /**
  * A transaction manager must be set before the datastore can be modified.
  * An exception will be throw if an attempt is made to modify the datastore
  * (setting a property, or creating or deleting an extendable object) when
  * no transaction manager is set in the session.
  * <P>
  * Changes to the datastore are stored in the transaction manager.  They
  * are not applied to the underlying datastore until the transaction is
  * committed.  Read accesses (property getters and queries) are passed on
  * to any transaction manager which will modify the results to reflect
  * changes stored in the transaction.
  * 
  * @author Nigel Westbury
  */
 public class TransactionManager implements IDataManager {
 	// TODO: At some time, review this and ensure that we
 	// really do need both the session objects here.
 	private Session committedSession;
 	private Session uncommittedSession;
 	
 	/**
 	 * Maps IObjectKey to Map, where IObjectKey is the key in the comitted datastore
 	 * and each Map maps PropertyAccessor to the
 	 * value of that property. Every object that has been modified by this
 	 * transaction manager (a scalar property in the object has been changed)
 	 * will have an entry in this map. The map keys are objects from the
 	 * datastore and will contain the property values that are currently
 	 * committed in the datastore. The map values are maps that contain an entry
 	 * for each scalar property in the object that has been changed.
 	 * <P>
 	 * If a value of a property is a reference to another object then an
 	 * UncommittedObjectKey is stored as the value. By doing this, the
 	 * referenced object does not need to be materialized unless necessary.
 	 * <P>
 	 * Deleted objects will also have an entry in this map (the value will be a
 	 * Boolean object with a value of false). If an object contains list
 	 * properties and the object is deleted then all the objects in the lists
 	 * will also be added to this with a Boolean false value. This is necessary
 	 * because this list is used to determine if an object has been deleted, to
 	 * ensure that we do not attempt to modify a property value in an object
 	 * that has in fact been deleted. (These Boolean false entries are NOT used
 	 * to perform the deletions when the transaction is committed - that is done
 	 * by the list deltas).
 	 */
 	Map modifiedObjects = new HashMap();
 	
 	/**
 	 * Maps ParentListPair to ModifiedList.
 	 * <P>
 	 * Every list that has been modified by this transaction manager
 	 * (objects added to the list or objects removed from the list)
 	 * will have an entry in this map.
 	 * <P>
 	 * Given a parent object and a property accessor for
 	 * a list property, we can look up an object that contains
 	 * the list of objects added to and objects removed from
 	 * that list. 
 	 */
 	Map modifiedLists = new HashMap();
 
     private Vector sessionChangeListeners = new Vector();
 
     /**
 	 * Construct a transaction manager for use with the given session.
 	 * The transaction manager does not become the active transaction
 	 * manager for the session until it is specifically set as the
 	 * active transaction manager.  By separating the construction of
 	 * the transaction manager from the activating of the transaction manager,
 	 * a transaction manager can be created and listeners can be set up to
 	 * listen to session changes within the transaction manager during an
 	 * initialization stage even though the transaction is not made active
 	 * until changes are made.
 	 *  
 	 * @param session the session object from the committed datastore
 	 */
 	public TransactionManager(Session committedSession) {
 		this.committedSession = committedSession;
 		this.uncommittedSession = (Session)getCopyInTransaction(committedSession);
 	}
 
 	/**
 	 * @return a session object representing an uncommitted
 	 * 			session object managed by this transaction manager
 	 */
 	public Session getSession() {
 		return uncommittedSession;
 	}
 
 	/**
 	 * Indicate whether there are any uncommitted changes being held
 	 * by this transaction manager.  This method is useful when the user
 	 * does something like selecting another transaction or closing a
 	 * dialog box and it is not clear whether the user wants to commit
 	 * or to cancel changes.
 	 *
 	 * @return true if property values have been changed or objects
 	 * 			have been created or deleted within the context of this
 	 * 			transaction manager since the last commit
 	 */
 	public boolean hasChanges() {
 		return !modifiedObjects.isEmpty() 
 		|| !modifiedLists.isEmpty();
 	}
 
 	/**
      * Given an instance of an object in the datastore
      * (i.e. committed), obtain a copy of the object that 
      * is in the version of the datastore managed by this
      * transaction manager.
      * <P>
      * Updates to property values in the returned object will
      * not be applied to the datastore until the changes held
      * by this transaction manager are committed to the datastore.
      * 
      * @param an object that exists in the datastore (committed)
      * @return a copy of the given object, being an uncommitted version
      * 			of the object in this transaction, or null if the
      * 			given object has been deleted in this transaction 
      */
     public ExtendableObject getCopyInTransaction(ExtendableObject committedObject) {
     	// First look in our map to see if this object has already been
     	// modified within the context of this transaction manager.
     	// If it has, return the modified version.
     	// Also check to see if the object has been deleted within the
     	// context of this transaction manager.  If it has then we raise
     	// an error.  
     	
     	// Both these situations are not likely to happen
     	// because usually one object is copied into the transaction
     	// manager and all other objects are obtained by traversing
     	// from that object.  However, it is good to check.
 
 		PropertySet propertySet = PropertySet.getPropertySet(committedObject.getClass());
     	
 		Collection constructorProperties = propertySet.getConstructorProperties();
 		
 		IObjectKey committedParentKey = committedObject.getParentKey();
 		UncommittedObjectKey key = new UncommittedObjectKey(this, committedObject.getObjectKey());
 		UncommittedObjectKey parentKey = (committedParentKey==null)?null:new UncommittedObjectKey(this, committedParentKey);
 		Map extensionMap = new HashMap();
 		
 		Object[] constructorParameters = new Object[3 + constructorProperties.size()];
 		constructorParameters[0] = key;
 		constructorParameters[1] = extensionMap;
 		constructorParameters[2] = parentKey;
 		
 		// Get the values from this object
 		
 		// Set the remaining parameters to the constructor.
 		for (Iterator iter = constructorProperties.iterator(); iter.hasNext(); ) {
 			PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();

 			Class valueClass = propertyAccessor.getValueClass(); 
 			Object value;
 			if (propertyAccessor.isList()) {
 				value = new DeltaListManager(this, committedObject, propertyAccessor);
 			} else if (ExtendableObject.class.isAssignableFrom(valueClass)) {
 				IObjectKey committedObjectKey = propertyAccessor.invokeObjectKeyField(committedObject);
 				value = committedObjectKey == null
 				? null
 						: new UncommittedObjectKey(this, committedObjectKey);
 			} else {
 				value = committedObject.getPropertyValue(propertyAccessor);
 			}
 			
 			constructorParameters[propertyAccessor.getIndexIntoConstructorParameters()] = value;
 		}
 		
 		// Now copy the extensions.  This is done by looping through the extensions
 		// in the old object and, for every extension that exists in the old object,
 		// copy the properties to the new object.
 		for (Iterator extensionIter = committedObject.getExtensionIterator(); extensionIter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)extensionIter.next();
 			PropertySet extensionPropertySet = (PropertySet)mapEntry.getKey();
 			int count = 0;
 			for (Iterator propertyIter = extensionPropertySet.getPropertyIterator1(); propertyIter.hasNext(); propertyIter.next()) {
 				count++;
 			}
 			Object[] extensionValues = new Object[count];
 			int i = 0;
 			for (Iterator propertyIter = extensionPropertySet.getPropertyIterator1(); propertyIter.hasNext(); ) {
 				PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIter.next();

				Class valueClass = propertyAccessor.getValueClass(); 
				Object value;
				if (propertyAccessor.isList()) {
					value = new DeltaListManager(this, committedObject, propertyAccessor);
				} else if (ExtendableObject.class.isAssignableFrom(valueClass)) {
					IObjectKey committedObjectKey = propertyAccessor.invokeObjectKeyField(committedObject);
					value = committedObjectKey == null
					? null
							: new UncommittedObjectKey(this, committedObjectKey);
				} else {
					value = committedObject.getPropertyValue(propertyAccessor);
				}
				
				extensionValues[i++] = value;
 			}
 			extensionMap.put(extensionPropertySet, extensionValues);
 		}
 
 		Object modifiedValues = modifiedObjects.get(committedObject.getObjectKey());
 		if (modifiedValues != null) {
 			if (modifiedValues instanceof Boolean) {
 				throw new RuntimeException("Attempt to get copy of object, but that object has been deleted in the transaction");
 			}
 			
 			Map propertyMap = (Map)modifiedValues;
 			
 			// Overwrite any values from modifiedValues
 			for (Iterator iter2 = propertyMap.entrySet().iterator(); iter2.hasNext(); ) {
 				Map.Entry mapEntry2 = (Map.Entry)iter2.next();
 				PropertyAccessor accessor = (PropertyAccessor)mapEntry2.getKey();
 				Object newValue = mapEntry2.getValue();
 				if (!accessor.getPropertySet().isExtension()) {
 					constructorParameters[accessor.getIndexIntoConstructorParameters()] = newValue;
 				} else {
 					PropertySet extensionPropertySet = accessor.getPropertySet();
 //					ExtensionObject extension = (ExtensionObject)mapEntry.getValue();
 					Object[] extensionValues = (Object[])extensionMap.get(extensionPropertySet);
 					if (extensionValues == null) {
 						int count = 0;
 						for (Iterator propertyIter = extensionPropertySet.getPropertyIterator1(); propertyIter.hasNext(); propertyIter.next()) {
 							count++;
 						}
 						extensionValues = new Object[count];
 						int i = 0;
 						for (Iterator propertyIter = extensionPropertySet.getPropertyIterator1(); propertyIter.hasNext(); ) {
 							PropertyAccessor propertyAccessor = (PropertyAccessor)propertyIter.next();
 							extensionValues[i++] = committedObject.getPropertyValue(propertyAccessor);
 						}
 						extensionMap.put(extensionPropertySet, extensionValues);
 					}
 					
 					// Now we can set the extension property value into the array
 					extensionValues[accessor.getIndexIntoConstructorParameters()] = newValue;
 				}
 			}
 		}
         	
 		// We can now create the object.
     	ExtendableObject copyInTransaction = (ExtendableObject)propertySet.constructImplementationObject(constructorParameters);
     	
     	// We do not copy lists owned by the object at this time.
     	// If a list is iterated, we must return copies in this transaction.
     	// Originals may never be materialized.
 
     	// If a list is iterated multiple times, a new set
     	// is instantiated.  This is consistent with list processing
     	// when objects are stored in database - do not cache the
     	// list because it must then be kept up to date and the
     	// list may no longer be needed, plus lists are not in general
     	// iterated more than once because the consumer should keep its
     	// own data up to date using listeners.
     	
     	// This is ok because the API contract states
     	// that if the user gets the same object
     	// twice then the user must listen for changes and refresh
     	// the other objects.  This is something the user has to
     	// do anyway because objects could be out of date due to
     	// changes to the committed version.
 
     	// Therefore, a list is really a delta from the committed list.
     	
     	// How does the delta get the committed list?
     	// We could pass an iterator here, but that is a once
     	// off.  We must pass a dynamic collection (a collection
     	// that is a view of the committed items in the list)
 
     	return copyInTransaction;
     }
     
 	/**
 	 * @param account
 	 * @return
 	 */
 	public boolean hasEntries(Account account) {
 		return !new ModifiedAccountEntriesList(account).isEmpty();
 	}
 
 	/**
 	 * @param account
 	 * @return
 	 */
 	public Collection getEntries(Account account) {
 		return new ModifiedAccountEntriesList(account);
 	}
 
     /**
      * Adds the listener to the collection of listeners who will be notified
      * when a change is made to the version of the datastore as seen through
      * this transaction manager.  Notifications will be sent when either a
      * change is committed to the datastore by another transaction manager
      * or an uncommitted change is made through this transaction manager.
      * <P>
      * When listening for changes to a datastore, there are two options.
      * If the listener is interested only in receiving committed changes
      * then the listener should listen to the Session object or the JMoneyPlugin
      * object.  However, if a listener wants to be notified of changes
      * made through a transaction manager, even though those changes are
      * not committed to the datastore, then the listener should add the
      * listener to the transaction manager using this method.
      * <P>
      * The listener will not recieve any notification at the time a transaction
      * is committed as the listener will already have been notified of the
      * changes.  However, if the transaction is rolled back then the
      * listener recieves an appropriate notification reversing the change
      * (for example, if a property value was changed but the transaction
      * was rolled back, listeners will receive a property change notification
      * at the time of the rollback with the original property value as the
      * new value, and the uncommitted property value as the old value).
      * <P>
      */
     public void addSessionChangeListener(SessionChangeListener l) {
         sessionChangeListeners.add(l);
     }
     
     /**
      * Removes the listener from the collection of listeners who will be notified
      * when a change is made to the version of the datastore as seen through
      * this transaction manager.
      */
     public void removeSessionChangeListener(SessionChangeListener l) {
         sessionChangeListeners.remove(l);
     }
     
     /**
      * Send change notifications to all listeners who are listening for
      * changes to the version of the datastore as seen through this
      * transaction manager.
      * <P>
      */
     void fireEvent(ISessionChangeFirer firer) {
     	// TODO: decide if we need to do this here.
     	//sessionFiring = true;
     	
     	// Notify listeners who are listening to us using the
     	// SessionChangeListener interface.
         if (!sessionChangeListeners.isEmpty()) {
         	// Take a copy of the listener list.  By doing this we
         	// allow listeners to safely add or remove listeners.
         	SessionChangeListener listenerArray[] = new SessionChangeListener[sessionChangeListeners.size()];
         	sessionChangeListeners.copyInto(listenerArray);
         	for (int i = 0; i < listenerArray.length; i++) {
         		firer.fire(listenerArray[i]);
         	}
         }
 
         //sessionFiring = false;
     }
 
 	/**
 	 * Apply the changes that are stored in this transaction manager.
 	 * <P>
 	 * When changes are committed, they are seen in the datastore
 	 * and also in the version of the datastore as seen through other
 	 * transaction managers.
 	 * <P>
 	 * All datastore listeners and all listeners which are listening
 	 * for changes ......
 	 * 
 	 */
 	public void commit() {
 		IDataManager sessionManager = committedSession.getObjectKey().getSessionManager();
 		sessionManager.startTransaction();
 		
 		// Add all the new objects, but set references to other
 		// new objects to null because the other new object may
 		// not have yet been added to the database and thus no
 		// reference can be set to the other object.
 		for (Iterator iter = modifiedLists.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)iter.next();
 			ModifiedListKey parentListPair = (ModifiedListKey)mapEntry.getKey();
 			ModifiedList modifiedList = (ModifiedList)mapEntry.getValue();
 
 			ExtendableObject parent = parentListPair.parentKey.getObject();
 			
 			for (Iterator iter2 = modifiedList.getAddedObjectIterator(); iter2.hasNext(); ) {
 				ExtendableObject newObject = (ExtendableObject)iter2.next();
 				
 				final ExtendableObject newCommittedObject = commitNewObject(newObject, parent, parentListPair.listAccessor);
 
 				// Fire the event.
 				// Note that this must be done after the committed object is set above.  This allows
 				// listeners to connect the event to an uncommitted object.
 				// TODO: decide if this should be here.  The fireEvent should be package protected,
 				// and we are calling it from outside the package.  Also, what are the event firing
 				// rules?
 				parent.getSession().fireEvent(
 						new ISessionChangeFirer() {
 							public void fire(SessionChangeListener listener) {
 								listener.objectAdded(newCommittedObject);
 							}
 						}
 				);
 			}
 		}
 
 		// Update all the updated objects
 		for (Iterator iter = modifiedObjects.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)iter.next();
 			IObjectKey committedKey = (IObjectKey)mapEntry.getKey();
 			Object newValues = mapEntry.getValue();
 			
 			if (newValues instanceof Map) {
 				Map propertyMap = (Map)newValues;
 				
 				for (Iterator iter2 = propertyMap.entrySet().iterator(); iter2.hasNext(); ) {
 					Map.Entry mapEntry2 = (Map.Entry)iter2.next();
 					PropertyAccessor accessor = (PropertyAccessor)mapEntry2.getKey();
 					Object newValue = mapEntry2.getValue();
 			
 					// TODO: If we create a method in IObjectKey for updating properties
 					// then we don't have to instantiate the committed object here.
 					// The advantages are small, however, because the key is likely to
 					// need to read the old properties from the database before setting
 					// the new property values.
 					ExtendableObject committedObject = committedKey.getObject();
 					
 					if (newValue instanceof UncommittedObjectKey) {
 						UncommittedObjectKey referencedKey = (UncommittedObjectKey)newValue;
 						// TODO: We should not have to instantiate an instance of the
 						// referenced object in the committed datastore.  However, there
 						// is no method to set the key as the value.
 						// We should add such a mechanism.
 						committedObject.setPropertyValue(accessor, referencedKey.getCommittedObjectKey().getObject());
 					} else {
 						committedObject.setPropertyValue(accessor, newValue);
 					}
 				}
 			}
 		}
 		
 		// Update all the objects marked for deletion, setting
 		// any references to other objects also marked for deletion
 		// to be null references.  This allows the other object
 		// to be deleted.
 		for (Iterator iter = modifiedObjects.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)iter.next();
 			IObjectKey committedKey = (IObjectKey)mapEntry.getKey();
 			Object newValues = mapEntry.getValue();
 			
 			if (newValues instanceof Boolean) {
 				ExtendableObject deletedObject = committedKey.getObject();
 				
 				PropertySet propertySet = PropertySet.getPropertySet(deletedObject.getClass());
 				for (Iterator iter2 = propertySet.getPropertyIterator3(); iter2.hasNext(); ) {
 					PropertyAccessor accessor = (PropertyAccessor)iter.next();
 					if (accessor.isScalar()) {
 						Object value = deletedObject.getPropertyValue(accessor);
 						if (value instanceof ExtendableObject) {
 							ExtendableObject referencedObject = (ExtendableObject)value;
 							UncommittedObjectKey referencedKey = (UncommittedObjectKey)referencedObject.getObjectKey();
 							IObjectKey committedReferencedKey = referencedKey.getCommittedObjectKey();
 							Object referencedNewValues = modifiedObjects.get(committedReferencedKey);
 							if (referencedNewValues instanceof Boolean) {
 								// This is a reference to an object that is marked for deletion.
 								// Set the reference to null
 								deletedObject.setPropertyValue(accessor, null);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// Delete the deleted objects
 		for (Iterator iter = modifiedLists.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)iter.next();
 			ModifiedListKey parentListPair = (ModifiedListKey)mapEntry.getKey();
 			ModifiedList modifiedList = (ModifiedList)mapEntry.getValue();
 
 			ExtendableObject parent = parentListPair.parentKey.getObject();
 			
 			for (Iterator iter2 = modifiedList.getDeletedObjectIterator(); iter2.hasNext(); ) {
 				IObjectKey objectToDelete = (IObjectKey)iter2.next();
 				
 				parent.getListPropertyValue(parentListPair.listAccessor).remove(objectToDelete.getObject());
 			}
 		}
 		
 		sessionManager.commitTransaction();
 		
 		// Clear out the changes in the object. These changes are the
 		// delta between the datastore and the uncommitted view.
 		// Now that the changes have been committed, these changes
 		// must be cleared.
 		modifiedLists.clear();
 		modifiedObjects.clear();
 	}
 
 	/**
 	 * Add a new uncommitted object to the committed datastore.
 	 * 
 	 * All objects in any list properties in the object are also added, hence
 	 * this method is recursive.
 	 * 
 	 * @param newObject
 	 * @param parent
 	 * @param listAccessor
 	 * @return the committed version of the object
 	 */
 	private ExtendableObject commitNewObject(ExtendableObject newObject, ExtendableObject parent, PropertyAccessor listAccessor) {
 		PropertySet actualPropertySet = PropertySet.getPropertySet(newObject.getClass());
 		
 		/**
 		 * Maps PropertyAccessor to property value
 		 * <P>
 		 * Holds references to new objects that have never been committed,
 		 * so that such references can be set later after
 		 * all the new objects have been committed.
 		 */
 		Map propertyChangeMap = new HashMap();
 		
 		int count = 0;
 		
 		for (Iterator iter3 = actualPropertySet.getPropertyIterator3(); iter3.hasNext(); ) {
 			PropertyAccessor accessor = (PropertyAccessor)iter3.next();
 			if (accessor.isScalar()) {
 				count++;
 			}
 		}
 		
 		Object [] values = new Object[count];
 		
 		int index = 0;
 		for (Iterator iter3 = actualPropertySet.getPropertyIterator3(); iter3.hasNext(); ) {
 			PropertyAccessor accessor = (PropertyAccessor)iter3.next();
 			if (accessor.isScalar()) {
 
 				Object value = newObject.getPropertyValue(accessor);
 				if (value instanceof ExtendableObject) {
 					ExtendableObject referencedObject = (ExtendableObject)value;
 					UncommittedObjectKey key = (UncommittedObjectKey)referencedObject.getObjectKey();
 					
 					// TODO: We do not really have to instantiate the object here.
 					// We can set an object reference just from the key.  However,
 					// we don't have the methods available to do that at this time.
 					
 					IObjectKey committedKey = key.getCommittedObjectKey();
 					if (committedKey != null) {
 						values[index] = committedKey;
 					} else {
 						// The property value is a reference to an object that has not have yet been committed to
 						// the datastore.  Such values cannot be set in the datastore.  We therefore avoid such
 						// properties here and set them later when all the new objects have been committed.
 						
 						// Add this property change to a map of property changes that we
 						// must do later.
 						values[index] = null;
 						propertyChangeMap.put(accessor, key);
 					}
 				} else {
 					values[index] = value;
 				}
 				index++;
 			}
 		}
 		
 		// Create the object with the appropriate property values
 		ObjectCollection propertyValues = parent.getListPropertyValue(listAccessor); 
 		final ExtendableObject newCommittedObject = propertyValues.createNewElement(actualPropertySet, values);
 
 		// Update the uncommitted object key to indicate that there is now a committed
 		// version of the object in the datastore
 		((UncommittedObjectKey)newObject.getObjectKey()).setCommittedObject(newCommittedObject);
 
 		// Commit all the child objects in the list properties.
 		for (Iterator iter3 = actualPropertySet.getPropertyIterator3(); iter3.hasNext(); ) {
 			PropertyAccessor accessor = (PropertyAccessor)iter3.next();
 			if (accessor.isList()) {
 				for (Iterator iter = newObject.getListPropertyValue(accessor).iterator(); iter.hasNext(); ) {
 					ExtendableObject childObject = (ExtendableObject)iter.next(); 
 					commitNewObject(childObject, newCommittedObject, accessor);
 				}
 			}
 		}
 		
 		// If there are property changes that must be applied later, add the property
 		// change map to the list
 		if (!propertyChangeMap.isEmpty()) {
 			modifiedObjects.put(newCommittedObject.getObjectKey(), propertyChangeMap);
 		}
 		
 		return newCommittedObject;
 	}
 
 	class DeletedObject {
 		private ExtendableObject parent;
 		private PropertyAccessor owningListProperty;
 		
 		DeletedObject(ExtendableObject parent, PropertyAccessor owningListProperty) {
 			this.parent = parent;
 			this.owningListProperty = owningListProperty;
 		}
 		
 		void deleteObject(ExtendableObject object) {
 			parent.getListPropertyValue(owningListProperty).remove(object);
 		}
 	}
 
 	/**
 	 * Given a list property in an object, create an object that maintains the
 	 * changes that have been made to that list within a transaction, or return
 	 * the object if one already exists. The modified list objects are not
 	 * created unless a change is made to the list (objects added or objects
 	 * removed).
 	 * <P>
 	 * It is important that callers do not keep a copy of the modified list
 	 * across method calls. This is because it may have changed from null to
 	 * non-null if someone else added to or deleted from the list, and it may
 	 * have changed from non-null to null if someone else committed the
 	 * transaction.
 	 * 
 	 * @param parentKey
 	 *            the object key (in the committed datastore) for the object
 	 *            containing the list property
 	 * @param listProperty
 	 * @return an object containing the changes to the given list. This object
 	 *         may be empty but is never null
 	 */
 	public ModifiedList createModifiedList(ModifiedListKey key) {
 		ModifiedList modifiedList = (ModifiedList)modifiedLists.get(key);
 		if (modifiedList == null) {
 			modifiedList = new ModifiedList();
 			modifiedLists.put(key, modifiedList);
 		}
 		return modifiedList;
 	}
 
 	/**
 	 * Given a list property in an object, get the object that maintains the
 	 * changes that have been made to that list within a transaction. The
 	 * modified list objects are not created unless a change is made to the list
 	 * (objects added or objects removed).
 	 * <P>
 	 * It is important that callers do not keep a copy of the modified list
 	 * across method calls. This is because it may have changed from null to
 	 * non-null if someone else added to or deleted from the list, and it may
 	 * have changed from non-null to null if someone else committed the
 	 * transaction.
 	 * 
 	 * @param parentKey
 	 *            the object key (in the committed datastore) for the object
 	 *            containing the list property
 	 * @param listProperty
 	 * @return an object containing the changes to the given list, or null if no
 	 *         changes have been made to the given list
 	 */
 	public ModifiedList getModifiedList(ModifiedListKey key) {
 		return (ModifiedList)modifiedLists.get(key);
 	}
 
 	public Object getAdapter(Class adapter) {
 		// It is possible to implement query interfaces that execute
 		// an optimized query against the committed datastore and then
 		// adjusts the results with the uncommitted changes.
 		// However, none are currently implemented.
 		return null;
 	}
 
 	private class ModifiedAccountEntriesList extends AbstractCollection {
 		
 		Account account;
 		
 		ModifiedAccountEntriesList(Account account) {
 			this.account = account;
 		}
 		
 		public int size() {
 			throw new RuntimeException("not implemented");
 		}
 
 		public Iterator iterator() {
 			// Build the list of differences between the committed
 			// list and the list in this transaction.
 			
 			// This is done each time an iterator is requested.
 			
 			Vector addedEntries = new Vector();
 			Vector removedEntries = new Vector();
 			
 			// Process all the new objects added within this transaction
 			for (Iterator iter = modifiedLists.entrySet().iterator(); iter.hasNext(); ) {
 				Map.Entry mapEntry = (Map.Entry)iter.next();
 				ModifiedListKey parentListPair = (ModifiedListKey)mapEntry.getKey();
 				ModifiedList modifiedList = (ModifiedList)mapEntry.getValue();
 				
 				// Find all entries added to existing transactions
 				if (parentListPair.listAccessor == TransactionInfo.getEntriesAccessor()) {
 					for (Iterator iter2 = modifiedList.getAddedObjectIterator(); iter2.hasNext(); ) {
 						Entry newEntry = (Entry)iter2.next();
 						if (account.equals(newEntry.getAccount())) {
 							addedEntries.add(newEntry);
 						}
 					}
 				}
 
 				// Find all entries in new transactions.
 				if (parentListPair.listAccessor == SessionInfo.getTransactionsAccessor()) {
 					for (Iterator iter2 = modifiedList.getAddedObjectIterator(); iter2.hasNext(); ) {
 						Transaction newTransaction = (Transaction)iter2.next();
 						for (Iterator iter3 = newTransaction.getEntryCollection().iterator(); iter3.hasNext(); ) {
 							Entry newEntry = (Entry)iter3.next();
 							if (account.equals(newEntry.getAccount())) {
 								addedEntries.add(newEntry);
 							}
 						}
 					}
 				}
 			}
 			
 			// Process all the changed and deleted objects.
 			// (Deleted objects are processed here and not from
 			// the deletedObjects list in modified lists in the
 			// above code.  This ensures that objects that are
 			// deleted due to the deletion of the parent are also
 			// processed).
 			for (Iterator iter = modifiedObjects.entrySet().iterator(); iter.hasNext(); ) {
 				Map.Entry mapEntry = (Map.Entry)iter.next();
 				IObjectKey committedKey = (IObjectKey)mapEntry.getKey();
 				Object newValues = mapEntry.getValue();
 				
 				ExtendableObject committedObject = committedKey.getObject();
 				
 				if (committedObject instanceof Entry) {
 					Entry entry = (Entry)committedObject;
 					if (newValues instanceof Map) {
 						Map propertyMap = (Map)newValues;
 						
 						// Object has changed property values.
 						if (propertyMap.containsKey(EntryInfo.getAccountAccessor())) {
 							boolean wasInIndex = account.equals(entry.getAccount());
 							boolean nowInIndex = account.equals(((IObjectKey)propertyMap.get(EntryInfo.getAccountAccessor())).getObject());
 							if (wasInIndex) {
 								if (!nowInIndex) {
 									removedEntries.add(committedObject);
 								}
 							} else {
 								if (nowInIndex) {
 									// Note that addedEntries must contain objects that
 									// are being managed by the transaction manager
 									// (not the committed versions).
 									addedEntries.add(getCopyInTransaction(committedObject));
 								}
 							}
 						}
 					} else {
 						// Object has been deleted.
 						if (entry.getAccount().equals(account)) {
 							removedEntries.add(committedObject);
 						}
 					}
 				}
 			}
 			
 			IObjectKey committedAccountKey = ((UncommittedObjectKey)account.getObjectKey()).getCommittedObjectKey();
 			if (committedAccountKey == null) {
 				// This is a new account created in this transaction
 				JMoneyPlugin.myAssert(removedEntries.isEmpty());
 				return addedEntries.iterator();
 			} else {
 				Account committedAccount = (Account)committedAccountKey.getObject();
 				Collection committedCollection = committedAccount.getEntries();
 				return new DeltaListIterator(TransactionManager.this, committedCollection.iterator(), addedEntries, removedEntries);
 			}
 		}
 	}
 
 	public void startTransaction() {
 		/*
 		 * This method is called only when transaction are nested.
 		 * The nested transaction will call this method before it
 		 * applies the changes to this transaction.
 		 * 
 		 * An implementation of this method must be provided because
 		 * this class implements the IDataManager interface.
 		 * However, this method does not need to do anything.
 		 */
 	}
 
 	public void commitTransaction() {
 		/*
 		 * This method is called only when transaction are nested.
 		 * The nested transaction will call this method before it
 		 * applies the changes to this transaction.
 		 * 
 		 * An implementation of this method must be provided because
 		 * this class implements the IDataManager interface.
 		 * However, this method does not need to do anything.
 		 */
 	}
 }
