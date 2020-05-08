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
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.EntryInfo;
 import net.sf.jmoney.fields.SessionInfo;
 import net.sf.jmoney.fields.TransactionInfo;
 import net.sf.jmoney.model2.AbstractDataOperation;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.DataManager;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.ISessionChangeFirer;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 import net.sf.jmoney.model2.ObjectCollection;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeListener;
 import net.sf.jmoney.model2.Transaction;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
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
 public class TransactionManager extends DataManager {
 	private DataManager baseDataManager;
 
 	// TODO: At some time, review this and ensure that we
 	// really do need the session object here.
 	private Session uncommittedSession;
 	
 	/**
 	 * Maps IObjectKey to Map, where IObjectKey is the key in the comitted
 	 * datastore and each Map maps PropertyAccessor to the value of that
 	 * property. Every object that has been modified by this transaction manager
 	 * (a scalar property in the object has been changed) will have an entry in
 	 * this map. The map keys are objects from the datastore and will contain
 	 * the property values that are currently committed in the datastore. The
 	 * map values are objects that contain details of the changes (changed
 	 * scalar property values, or an indication that the object has been
 	 * deleted).
 	 * <P>
 	 * If a value of a property is a reference to another object then an
 	 * UncommittedObjectKey is stored as the value. By doing this, the
 	 * referenced object does not need to be materialized unless necessary.
 	 * <P>
 	 * Deleted objects will also have an entry in this map. If an object
 	 * contains list properties and the object is deleted then all the objects
 	 * in the lists will also be added to this map with a ModifiedObject that
 	 * has a 'deleted' indication. This is necessary because this list is used
 	 * to determine if an object has been deleted, to ensure that we do not
 	 * attempt to modify a property value in an object that has in fact been
 	 * deleted.
 	 */
 	// TODO: Now we have the allObjects map, this map can be removed.  The code
 	// should be significantly simplified now that we don't have to re-apply
 	// the changes each time an object is requested.
 	// (Of course, the complications now need to be added to the listener code
 	// that needs to keep the objects in allObjects map up to date
 	// and tell our listeners.
 	Map<IObjectKey, ModifiedObject> modifiedObjects = new HashMap<IObjectKey, ModifiedObject>();
 	
 	/**
 	 * Every extendable object that has ever been created in this transaction and passed
 	 * to the user is in this map.  This is required because all DataManager
 	 * objects must guarantee that there is only ever a single instance of an
 	 * object in existence.
 	 * 
 	 * Objects that were created in this transaction are not in this map.
 	 * There is only one instance of such objects in any case, so only that one
 	 * instance would be returned.
 	 * 
 	 * The object key is the key in the committed datastore.  Only objects
 	 * that exist in the underlying datastore are in this map, and it is
 	 * just easier to use the committed key than to use an uncommitted key.  
 	 */
 	Map<IObjectKey, ExtendableObject> allObjects = new HashMap<IObjectKey, ExtendableObject>();
 	
 	/**
 	 * Every list that has been modified by this transaction manager
 	 * (objects added to the list or objects removed from the list)
 	 * will have an entry in this set.  This enables the transaction
 	 * manager to easily find the added and deleted objects when committing
 	 * the changes.
 	 */
 	Set<DeltaListManager> modifiedLists = new HashSet<DeltaListManager>();
 
 	/**
 	 * true if a nested transaction is in the process of applying its
 	 * changes to our data
 	 */
 	// TODO: use this flag to ensure that performRefresh is called correctly.
 	private boolean insideTransaction = false;
 
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
 	public TransactionManager(DataManager baseDataManager) {
 		this.baseDataManager = baseDataManager;
 		this.uncommittedSession = getCopyInTransaction(baseDataManager.getSession());
 		
 		/*
 		 * Listen for changes to the base data.  Note that
 		 * a weak reference is maintained to this listener
 		 * so we don't have to worry about removing the listener,
 		 * which is just as well because this object may be left
 		 * for the garbage collector without knowing when it is
 		 * no longer being used.
 		 */
 		baseDataManager.addChangeListenerWeakly(new MySessionChangeListener());
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
     public <E extends ExtendableObject> E getCopyInTransaction(E committedObject) {
 
 		ExtendableObject objectInTransaction = allObjects.get(committedObject.getObjectKey());
 		if (objectInTransaction != null) {
 			// TODO: decide if and how we check for deleted objects.
 //			if (objectInTransaction.isDeleted()) {
 //				throw new RuntimeException("Attempt to get copy of object, but that object has been deleted in the transaction");
 //			}
 			return committedObject.getClass().cast(objectInTransaction);
 		}
 			
     	
     	
     	
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
 
 		PropertySet<?> propertySet = PropertySet.getPropertySet(committedObject.getClass());
     	
 		Collection<PropertyAccessor> constructorProperties = propertySet.getConstructorProperties();
 		
 		IObjectKey committedParentKey = committedObject.getParentKey();
 		UncommittedObjectKey key = new UncommittedObjectKey(this, committedObject.getObjectKey());
 		UncommittedObjectKey parentKey = (committedParentKey==null)?null:new UncommittedObjectKey(this, committedParentKey);
 		Map<PropertySet, Object[]> extensionMap = new HashMap<PropertySet, Object[]>();
 		
 		Object[] constructorParameters = new Object[3 + constructorProperties.size()];
 		constructorParameters[0] = key;
 		constructorParameters[1] = extensionMap;
 		constructorParameters[2] = parentKey;
 		
 		// Get the values from this object
 
 		// Set the remaining parameters to the constructor.
 		for (PropertyAccessor propertyAccessor: constructorProperties) {
 
 			Object value;
 			if (propertyAccessor.isList()) {
 				ListPropertyAccessor<?> listAccessor = (ListPropertyAccessor)propertyAccessor;
 				value = createDeltaListManager(committedObject, listAccessor);
 			} else {
 				ScalarPropertyAccessor<?> scalarAccessor = (ScalarPropertyAccessor)propertyAccessor;
 				Class valueClass = scalarAccessor.getClassOfValueObject(); 
 				if (ExtendableObject.class.isAssignableFrom(valueClass)) {
 					IObjectKey committedObjectKey = scalarAccessor.invokeObjectKeyField(committedObject);
 					value = committedObjectKey == null
 					? null
 							: new UncommittedObjectKey(this, committedObjectKey);
 				} else {
 					value = committedObject.getPropertyValue(scalarAccessor);
 				}
 			}
 			
 			constructorParameters[propertyAccessor.getIndexIntoConstructorParameters()] = value;
 		}
 		
 		// Now copy the extensions.  This is done by looping through the extensions
 		// in the old object and, for every extension that exists in the old object,
 		// copy the properties to the new object.
 		for (Iterator extensionIter = committedObject.getExtensionIterator(); extensionIter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)extensionIter.next();
 			PropertySet<?> extensionPropertySet = (PropertySet)mapEntry.getKey();
 			int count = extensionPropertySet.getProperties1().size();
 			Object[] extensionValues = new Object[count];
 			int i = 0;
     		for (PropertyAccessor propertyAccessor: extensionPropertySet.getProperties1()) {
 
 				Object value;
 				if (propertyAccessor.isList()) {
 					ListPropertyAccessor<?> listAccessor = (ListPropertyAccessor)propertyAccessor;
 					value = createDeltaListManager(committedObject, listAccessor);
 				} else {
 					ScalarPropertyAccessor<?> scalarAccessor = (ScalarPropertyAccessor)propertyAccessor;
 					Class valueClass = scalarAccessor.getClassOfValueObject(); 
 					if (ExtendableObject.class.isAssignableFrom(valueClass)) {
 
 						IObjectKey committedObjectKey = scalarAccessor.invokeObjectKeyField(committedObject);
 						value = committedObjectKey == null
 						? null
 								: new UncommittedObjectKey(this, committedObjectKey);
 					} else {
 						value = committedObject.getPropertyValue(scalarAccessor);
 					}
 				}
 				
 				extensionValues[i++] = value;
 			}
 			extensionMap.put(extensionPropertySet, extensionValues);
 		}
 
 /* No longer needed.  If the object has been modified by this transaction,
  * it would be in the allObjects map (which contains all objects ever returned
  * by this transaction
 
 		ModifiedObject modifiedValues = modifiedObjects.get(committedObject.getObjectKey());
 		if (modifiedValues != null) {
 			if (modifiedValues.isDeleted()) {
 				throw new RuntimeException("Attempt to get copy of object, but that object has been deleted in the transaction");
 			}
 			
 			// Overwrite any values from modifiedValues
 			for (Map.Entry<ScalarPropertyAccessor, Object> mapEntry2: modifiedValues.getMap().entrySet()) {
 				ScalarPropertyAccessor accessor = mapEntry2.getKey();
 				Object newValue = mapEntry2.getValue();
 				if (!accessor.getPropertySet().isExtension()) {
 					constructorParameters[accessor.getIndexIntoConstructorParameters()] = newValue;
 				} else {
 					PropertySet<?> extensionPropertySet = accessor.getPropertySet();
 //					ExtensionObject extension = (ExtensionObject)mapEntry.getValue();
 					Object[] extensionValues = extensionMap.get(extensionPropertySet);
 					if (extensionValues == null) {
 						int count = extensionPropertySet.getProperties1().size();
 						extensionValues = new Object[count];
 						int i = 0;
 			    		for (PropertyAccessor propertyAccessor: extensionPropertySet.getProperties1()) {
 
 							Object value;
 							if (propertyAccessor.isList()) {
 								ListPropertyAccessor<?> listAccessor = (ListPropertyAccessor)propertyAccessor;
 								value = createDeltaListManager(committedObject, listAccessor);
 							} else {
 								ScalarPropertyAccessor<?> scalarAccessor = (ScalarPropertyAccessor)propertyAccessor;
 								Class valueClass = scalarAccessor.getClassOfValueObject(); 
 								if (ExtendableObject.class.isAssignableFrom(valueClass)) {
 
 									IObjectKey committedObjectKey = scalarAccessor.invokeObjectKeyField(committedObject);
 									value = committedObjectKey == null
 									? null
 											: new UncommittedObjectKey(this, committedObjectKey);
 								} else {
 									value = committedObject.getPropertyValue(scalarAccessor);
 								}
 							}
 							
 							extensionValues[i++] = value;
 						}
 						extensionMap.put(extensionPropertySet, extensionValues);
 					}
 					
 					// Now we can set the extension property value into the array
 					extensionValues[accessor.getIndexIntoConstructorParameters()] = newValue;
 				}
 			}
 		}
 */        	
 		// We can now create the object.
     	E copyInTransaction = committedObject.getClass().cast(propertySet.constructImplementationObject(constructorParameters));
 
     	/*
     	 * Now we have created a version of this object that is valid in this datastore,
     	 * put it in the map so that we can guarantee that we always return the same
     	 * instance in future.
     	 */
     	allObjects.put(committedObject.getObjectKey(), copyInTransaction);
     	
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
 
     private <E extends ExtendableObject> DeltaListManager<E> createDeltaListManager(ExtendableObject committedParent, ListPropertyAccessor<E> listProperty) {
 		return new DeltaListManager<E>(this, committedParent, listProperty);
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
 	public Collection<Entry> getEntries(Account account) {
 		return new ModifiedAccountEntriesList(account);
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
 		baseDataManager.startTransaction();
 		
 		// Add all the new objects, but set references to other
 		// new objects to null because the other new object may
 		// not have yet been added to the database and thus no
 		// reference can be set to the other object.
 		for (DeltaListManager<?> modifiedList: modifiedLists) {
 			commitObjectsInList(modifiedList);
 		}
 
 		// Update all the updated objects
 		for (Map.Entry<IObjectKey, ModifiedObject> mapEntry: modifiedObjects.entrySet()) {
 			IObjectKey committedKey = mapEntry.getKey();
 			ModifiedObject newValuesMap = mapEntry.getValue();
 			
 			if (!newValuesMap.isDeleted()) {
 				Map<ScalarPropertyAccessor, Object> propertyMap = newValuesMap.getMap();
 /* Actually this does not work.  We must go through the setters because we are 'outside' the datastore.
  * The values would not otherwise be set in the objects themselves.
  
 				ExtendableObject committedObject = committedKey.getObject();
 				PropertySet<?> actualPropertySet = PropertySet.getPropertySet(committedObject.getClass());
 
 				int count = actualPropertySet.getScalarProperties3().size();
 				Object [] newValues = new Object[count];
 				Object [] oldValues = new Object[count];
 				
 				// TODO: It may be better if we save the data from the committed
 				// object early on, before any changes.  This really depends on
 				// how we decide to cope with conflicts between the committed and
 				// this data manager.
 				int index = 0;
 				for (ScalarPropertyAccessor<?> accessor: actualPropertySet.getScalarProperties3()) {
 					Object value = committedObject.getPropertyValue(accessor);
 					if (value instanceof ExtendableObject) {
 						ExtendableObject referencedObject = (ExtendableObject)value;
 						oldValues[index] = referencedObject.getObjectKey();
 					} else {
 						oldValues[index] = value;
 					}
 					
 					if (propertyMap.containsKey(accessor)) {
 						Object newValue = propertyMap.get(accessor);
 						if (newValue instanceof UncommittedObjectKey) {
 							UncommittedObjectKey referencedKey = (UncommittedObjectKey)newValue;
 							newValues[index] = referencedKey.getCommittedObjectKey();
 						} else {
 							newValues[index] = value;
 						}
 					} else {
 						// No change in the property value
 						newValues[index] = oldValues[index];
 					}
 					
 					index++;
 				}
 */				
 				
 				
 				
 				for (Map.Entry<ScalarPropertyAccessor, Object> mapEntry2: propertyMap.entrySet()) {
 					ScalarPropertyAccessor<?> accessor = mapEntry2.getKey();
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
 						newValue = referencedKey.getCommittedObjectKey().getObject();
 					}
 					setProperty(committedObject, accessor, newValue);
 				}
 			}
 		}
 		
 		/*
 		 * Delete all object marked for deletion. This is a two-step process.
 		 * The first step involves iterating over all the objects marked for
 		 * deletion and seeing if they contain any references to other objects
 		 * that are also marked for deletion. If any such references are found,
 		 * the reference is set to null. The second step involves actually
 		 * deleting the objects. The reason why we must do this two-step process
 		 * is that there may be circular references between objects marked for
 		 * deletion, and the underlying database may raise a reference constaint
 		 * violation if an object is deleted while other objects contain
 		 * references to it.
 		 * 
 		 * This code is not perfect and needs more work to make it perfect.
 		 * Firstly, there may be a problem if the underlying database does not
 		 * allow null values for a particular reference. In that case, we should
 		 * ignore the failure to set the value to null. We set what we can to
 		 * null and then go on to delete all the values. Secondly, we may have
 		 * to make multiple passes while attempting to delete all the objects
 		 * because if one contains a non-nullable reference to the other then it
 		 * must be deleted first. It should be theoretically possible to delete
 		 * the objects, because the database got into this state in the first
 		 * case, and we can remove the objects by reversing the steps taken to
 		 * get to this state.
 		 */
 
 		/*
 		 * Step 1: Update all the objects marked for deletion, setting any
 		 * references to other objects also marked for deletion to be null
 		 * references.
 		 */
 		for (Map.Entry<IObjectKey, ModifiedObject> mapEntry: modifiedObjects.entrySet()) {
 			IObjectKey committedKey = mapEntry.getKey();
 			ModifiedObject newValues = mapEntry.getValue();
 			
 			if (newValues.isDeleted()) {
 				ExtendableObject deletedObject = committedKey.getObject();
 
 				ExtendablePropertySet<?> propertySet = PropertySet.getPropertySet(deletedObject.getClass());
 				for (ScalarPropertyAccessor<?> accessor: propertySet.getScalarProperties3()) {
 					Object value = deletedObject.getPropertyValue(accessor);
 					if (value instanceof ExtendableObject) {
 						ExtendableObject referencedObject = (ExtendableObject)value;
 						IObjectKey committedReferencedKey = referencedObject.getObjectKey();
 						ModifiedObject referencedNewValues = modifiedObjects.get(committedReferencedKey);
 						if (referencedNewValues != null && referencedNewValues.isDeleted()) {
 							// This is a reference to an object that is marked for deletion.
 							// Set the reference to null
 							deletedObject.setPropertyValue(accessor, null);
 						}
 					}
 				}
 			}
 		}
 		
 		/*
 		 * Step 2: Delete the deleted objects
 		 */
 		for (DeltaListManager<?> modifiedList: modifiedLists) {
 
 			ExtendableObject parent = modifiedList.parentKey.getObject();
 			
 			for (IObjectKey objectToDelete: modifiedList.getDeletedObjects()) {
 				parent.getListPropertyValue(modifiedList.listAccessor).remove(objectToDelete.getObject());
 			}
 		}
 		
 		baseDataManager.commitTransaction();
 		
 		// Clear out the changes in the object. These changes are the
 		// delta between the datastore and the uncommitted view.
 		// Now that the changes have been committed, these changes
 		// must be cleared.
 		for (DeltaListManager<?> modifiedList: modifiedLists) {
 			modifiedList.addedObjects.clear();
 			modifiedList.deletedObjects.clear();
 		}
 		modifiedLists.clear();
 		modifiedObjects.clear();
 	}
 
 	/**
 	 * This method does the same as the commit() method but the changes
 	 * may be undone and redone.  This support is available with no
 	 * coding needed by the caller other than to pass the label to be
 	 * used to describe the operation.
 	 * 
 	 * @param label the label to be used to describe this operation
 	 * 			for undo/redo purposes
 	 */
 	public void commit(String label) {
 		IUndoContext undoContext = baseDataManager.getSession().getUndoContext();
 		IOperationHistory history = JMoneyPlugin.getDefault().getWorkbench().getOperationSupport().getOperationHistory();
 		
 		IUndoableOperation operation = new AbstractDataOperation(baseDataManager.getSession(), label) {
 			@Override
 			public IStatus execute() throws ExecutionException {
 				commit();
 				return Status.OK_STATUS;
 			}
 		};
 		
 		operation.addContext(undoContext);
 		try {
 			history.execute(operation, null, null);
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private <V> void setProperty(ExtendableObject committedObject, ScalarPropertyAccessor<V> accessor, Object newValue) {
 		committedObject.setPropertyValue(accessor, accessor.getClassOfValueObject().cast(newValue));
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
 	private <E extends ExtendableObject> E commitNewObject(E newObject, ExtendableObject parent, ListPropertyAccessor<E> listAccessor, boolean isDescendentInsert) {
		ExtendablePropertySet<? extends E> actualPropertySet = listAccessor.getElementPropertySet().getActualPropertySet((Class<? extends E>)newObject.getClass());
 		
 		/**
 		 * Maps PropertyAccessor to property value
 		 * <P>
 		 * Holds references to new objects that have never been committed,
 		 * so that such references can be set later after
 		 * all the new objects have been committed.
 		 */
 		ModifiedObject propertyChangeMap = new ModifiedObject();
 		
 		int count = actualPropertySet.getScalarProperties3().size();
 		Object [] values = new Object[count];
 		
 		int index = 0;
 		for (ScalarPropertyAccessor<?> accessor: actualPropertySet.getScalarProperties3()) {
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
 		
 		// Create the object with the appropriate property values
 		ObjectCollection<? super E> propertyValues = parent.getListPropertyValue(listAccessor); 
 		final E newCommittedObject = propertyValues.createNewElement(actualPropertySet, values, isDescendentInsert);
 
 		// Update the uncommitted object key to indicate that there is now a committed
 		// version of the object in the datastore
 		((UncommittedObjectKey)newObject.getObjectKey()).setCommittedObject(newCommittedObject);
 
 		// Commit all the child objects in the list properties.
 		for (ListPropertyAccessor<?> subListAccessor: actualPropertySet.getListProperties3()) {
 			commitChildren(newObject, newCommittedObject, subListAccessor);
 		}
 		
 		// If there are property changes that must be applied later, add the property
 		// change map to the list
 		if (!propertyChangeMap.isEmpty()) {
 			modifiedObjects.put(newCommittedObject.getObjectKey(), propertyChangeMap);
 		}
 		
 		return newCommittedObject;
 	}
 
 	
 	private <E extends ExtendableObject> void commitChildren(ExtendableObject newObject, ExtendableObject newCommittedObject, ListPropertyAccessor<E> subListAccessor) {
 		for (E childObject: newObject.getListPropertyValue(subListAccessor)) {
 			commitNewObject(childObject, newCommittedObject, subListAccessor, true);
 		}
 	}
 
 	private <E extends ExtendableObject> void commitObjectsInList(DeltaListManager<E> modifiedList) {
 //		ModifiedList<E> modifiedList = (ModifiedList<E>)untypedModifiedList;
 		
 		ExtendableObject parent = modifiedList.parentKey.getObject();
 		
 		for (ExtendableObject newUntypedObject: modifiedList.getAddedObjects()) {
 			E newObject = modifiedList.listAccessor.getElementPropertySet().getImplementationClass().cast(newUntypedObject);
 
 			final ExtendableObject newCommittedObject = commitNewObject(newObject, parent, modifiedList.listAccessor, false);
 
 			// Fire the event.
 			// Note that this must be done after the committed object is set above.  This allows
 			// listeners to connect the event to an uncommitted object.
 			// TODO: decide if this should be here.  The fireEvent should be package protected,
 			// and we are calling it from outside the package.  Also, what are the event firing
 			// rules?
 			// Also we should be firing the event after the null references to the new objects
 			// have been set....
 			baseDataManager.fireEvent(
 					new ISessionChangeFirer() {
 						public void fire(SessionChangeListener listener) {
 							listener.objectInserted(newCommittedObject);
 						}
 					}
 			);
 		}
 	}
 	
 	class DeletedObject {
 		private ExtendableObject parent;
 		private ListPropertyAccessor<?> owningListProperty;
 		
 		DeletedObject(ExtendableObject parent, ListPropertyAccessor owningListProperty) {
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
 /*	
 	public <E extends ExtendableObject> ModifiedList<E> createModifiedList(ModifiedListKey<E> key) {
 		ModifiedList<E> modifiedList = modifiedLists.get(key);
 		if (modifiedList == null) {
 			modifiedList = new ModifiedList<E>();
 			modifiedLists.put(key, modifiedList);
 		}
 		return modifiedList;
 	}
 */
 /*	
 	public <E extends ExtendableObject> void putModifiedList(ModifiedListKey<E> key, DeltaListManager<E> list) {
 		modifiedLists.put(key, list);
 	}
 */
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
 /*	
 	public <E extends ExtendableObject> ModifiedList<E> getModifiedList(ModifiedListKey<E> key) {
 		return modifiedLists.get(key);
 	}
 */
 	public Object getAdapter(Class adapter) {
 		// It is possible to implement query interfaces that execute
 		// an optimized query against the committed datastore and then
 		// adjusts the results with the uncommitted changes.
 		// However, none are currently implemented.
 		return null;
 	}
 
 	private class ModifiedAccountEntriesList extends AbstractCollection<Entry> {
 		
 		Account account;
 		
 		ModifiedAccountEntriesList(Account account) {
 			this.account = account;
 		}
 		
 		public int size() {
 			throw new RuntimeException("not implemented");
 		}
 
 		public Iterator<Entry> iterator() {
 			// Build the list of differences between the committed
 			// list and the list in this transaction.
 			
 			// This is done each time an iterator is requested.
 			
 			Vector<Entry> addedEntries = new Vector<Entry>();
 			Vector<IObjectKey> removedEntries = new Vector<IObjectKey>();
 			
 			// Process all the new objects added within this transaction
 			for (DeltaListManager<?> modifiedList: modifiedLists) {
 				
 				// Find all entries added to existing transactions
 				if (modifiedList.listAccessor == TransactionInfo.getEntriesAccessor()) {
 					for (ExtendableObject newObject: modifiedList.getAddedObjects()) {
 						Entry newEntry = (Entry)newObject;
 						if (account.equals(newEntry.getAccount())) {
 							addedEntries.add(newEntry);
 						}
 					}
 				}
 
 				// Find all entries in new transactions.
 				if (modifiedList.listAccessor == SessionInfo.getTransactionsAccessor()) {
 					for (ExtendableObject newObject: modifiedList.getAddedObjects()) {
 						Transaction newTransaction = (Transaction)newObject;
 						for (Entry newEntry: newTransaction.getEntryCollection()) {
 							if (account.equals(newEntry.getAccount())) {
 								addedEntries.add(newEntry);
 							}
 						}
 					}
 				}
 			}
 			
 			/*
 			 * Process all the changed and deleted objects. (Deleted objects are
 			 * processed here and not from the deletedObjects list in modified
 			 * lists in the above code. This ensures that objects that are
 			 * deleted due to the deletion of the parent are also processed).
 			 */
 			for (Map.Entry<IObjectKey, ModifiedObject> mapEntry: modifiedObjects.entrySet()) {
 				IObjectKey committedKey = mapEntry.getKey();
 				ModifiedObject newValues = mapEntry.getValue();
 				
 				ExtendableObject committedObject = committedKey.getObject();
 				
 				if (committedObject instanceof Entry) {
 					Entry entry = (Entry)committedObject;
 					if (!newValues.isDeleted()) {
 						Map<ScalarPropertyAccessor, Object> propertyMap = newValues.getMap();
 						
 						// Object has changed property values.
 						if (propertyMap.containsKey(EntryInfo.getAccountAccessor())) {
 							boolean wasInIndex = account.equals(entry.getAccount());
 							boolean nowInIndex = account.equals(((IObjectKey)propertyMap.get(EntryInfo.getAccountAccessor())).getObject());
 							if (wasInIndex) {
 								if (!nowInIndex) {
 									removedEntries.add(entry.getObjectKey());
 								}
 							} else {
 								if (nowInIndex) {
 									// Note that addedEntries must contain objects that
 									// are being managed by the transaction manager
 									// (not the committed versions).
 									addedEntries.add((Entry)getCopyInTransaction(entry));
 								}
 							}
 						}
 					} else {
 						// Object has been deleted.
 						if (entry.getAccount().equals(account)) {
 							removedEntries.add(entry.getObjectKey());
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
 				Collection<Entry> committedCollection = committedAccount.getEntries();
 				return new DeltaListIterator<Entry>(TransactionManager.this, committedCollection.iterator(), addedEntries, removedEntries);
 			}
 		}
 	}
 
 	/**
 	 * This method is called when a nested transaction manager is about to apply its
 	 * changes to our data.
 	 */
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
 		
 		insideTransaction = true;
 	}
 
 	/**
 	 * This method is called when a nested transaction manager has completed
 	 * making changes to our data.
 	 */
 	public void commitTransaction() {
 		/*
 		 * This method is called only when transaction are nested.
 		 * The nested transaction will call this method after it has
 		 * applied the changes to this transaction.
 		 * 
 		 * Changes are applied to this object's data as the changes are
 		 * made and there is nothing we need do to 'commit' the changes.
 		 * However, we do need to fire the performRefresh event method
 		 * at this time.  This event notifies our listeners that a batch
 		 * of changes has completed and now is a good time to refresh
 		 * views.
 		 */
 		fireEvent(
 				new ISessionChangeFirer() {
 					public void fire(SessionChangeListener listener) {
 						listener.performRefresh();
 					}
 				});
 		
 		insideTransaction = false;
 	}
 
 	/**
 	 * This class contains the methods that merge changes
 	 * from the base data into the data for this object,
 	 * and also tell our listeners of such changes.
 	 */
 	private class MySessionChangeListener implements SessionChangeListener {
 
 		public void objectInserted(ExtendableObject newObject) {
 			/*
 			 * The object may contain references to objects
 			 * that have been deleted in this view.  
 			 * 
 			 * In such a situation, the object deleted in this view
 			 * could be first 'undeleted'.  If the undeleted object references
 			 * other objects that were deleted by this view then those
 			 * objects are in turn undeleted in a recursive manner.
 			 * 
 			 * A simpler approach may be to ignore the new object until
 			 * the transaction is committed.  At that time, the commit fails
 			 * with a conflict exception.  
 			 */
 			// TODO Implement this method
 		}
 
 		public void objectCreated(ExtendableObject newObject) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void objectChanged(ExtendableObject changedObject, ScalarPropertyAccessor changedProperty, Object oldValue, Object newValue) {
 			/*
 			 * The property may have been changed to reference an
 			 * object that has been deleted in this view.
 			 * 
 			 * In such a situation, the object could be 
 			 * first 'undeleted' in this view.  If the undeleted object references
 			 * other objects that were deleted by this view then those
 			 * objects are in turn undeleted in a recursive manner.
 			 * 
 			 * A simpler approach may be to ignore the new object until
 			 * the transaction is committed.  At that time, the commit fails
 			 * with a conflict exception.
 			 * 
 			 * We also need to consider what happens if this property change made
 			 * another property inapplicable, and that other property was changed
 			 * in this view, or if this property has been made inapplicable as
 			 * a result of a change in this view of another property.
 			 * All sorts of possibilities to consider. 
 			 */
 			// TODO Implement this method
 		}
 
 		public void objectRemoved(ExtendableObject deletedObject) {
 			/*
 			 * If an object is deleted from the base data then we
 			 * know there are no references to the object from the
 			 * base data.  However, it is possible that a reference
 			 * was created to this object in this version of the data.
 			 * 
 			 * If that is the case then we could just not remove the object
 			 * from this view of the data.  The object is removed from
 			 * this view of the data only if all references to it are
 			 * removed from this view.  If references still remain when
 			 * this view is committed then a conflict error occurs.
 			 * The user should be told that the object no longer exists
 			 * and the user must remove references to it before attempting
 			 * again to commit.
 			 * 
 			 * A simpler approach may be to ignore the deletion of the object until
 			 * the transaction is committed.  At that time, the commit fails
 			 * with a conflict exception.  
 			 */
 			// TODO Implement this method
 		}
 
 		public void objectDestroyed(ExtendableObject deletedObject) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void performRefresh() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void sessionReplaced(Session oldSession, Session newSession) {
 			// TODO This method is not applicable here.
 			// Do we need a version of this listener that does
 			// not have this method???
 		}
 	}
 }
