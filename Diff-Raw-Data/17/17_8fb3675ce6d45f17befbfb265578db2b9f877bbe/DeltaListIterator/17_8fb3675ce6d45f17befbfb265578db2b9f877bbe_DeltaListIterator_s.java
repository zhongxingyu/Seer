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
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IObjectKey;
 
 /**
  * Class imlementing an iterator that wraps a iterator of committed
  * objects in the datastore,
  * but adjusting the elements by applying changes made in
  * the transaction.  New elements are added and deleted elements
  * are removed from the iteration.  Furthermore, the elements
  * returned by the given iterator are adjusted to get the
  * uncommitted versions.
  * 
  * @author Nigel Westbury
  */
 class DeltaListIterator<E extends ExtendableObject> implements Iterator<E> {
 	TransactionManager transactionManager;
 	boolean processingCommittedObjects = true;
 
 	/**
 	 * This collection contains the uncommitted version
 	 * of objects that have been added
 	 */
 	Collection<E> addedObjects;
 	
 	/**
 	 * The object keys in this collection are the keys for
 	 * the committed version of the objects.
 	 */
 	Collection<IObjectKey> deletedObjects;
 	
 	Iterator<E> subIterator;
 	
 	/**
 	 * Always non-null if processingCommittedObjects = true
 	 * Not applicable if processingCommittedObjects = false
 	 */
 	E nextObject;
 	
 	/**
 	 * The last object returned by this iterator if the last such
 	 * object has a committed version, or null if the last such object
 	 * is a new object added in this delta
 	 */
 	E lastObject;
 	
 	/**
 	 * Construct an iterator that iterates the given iterator,
 	 * but adjusting the elements by adding and removing elements
 	 * in the given lists.
 	 * 
 	 * @param committedListIterator
 	 * @param addedObjects list of objects that have been added
 	 * 			in the transaction.  These objects may be either
 	 * 			objects newly created in the transation or may be
 	 * 			uncommitted versions of objects that exist in the
 	 * 			committed datastore (the latter being possible only
 	 * 			if this iterator is being used for an indexed value
 	 * 			list).
 	 * @param deletedObjects list of objects in the committed datastore
 	 * 			that have been deleted in the transaction.  All objects
 	 * 			in this list should be also in the set returned by
 	 * 			committedListIterator.  This list contains the committed
 	 * 			object keys.
 	 */
 	DeltaListIterator(TransactionManager transactionManager, Iterator<E> committedListIterator, Collection<E> addedObjects, Collection<IObjectKey> deletedObjects) {
 		this.transactionManager = transactionManager;
 		subIterator = committedListIterator;
 		this.addedObjects = addedObjects;
 		this.deletedObjects = deletedObjects;
 		setNextObject();
 	}
 	
 	public boolean hasNext() {
 		if (processingCommittedObjects) {
 			return true;
 		} else {
 			return subIterator.hasNext();
 		}
 	}
 	
 	/*
 	 * When processing the list of committed objects (the first sub-iteration),
 	 * we always leave the sub-iterator positioned at the next object to be
 	 * returned. That is, we pass any objects in the set that are marked for
 	 * deletion. Doing this enables the hasNext() method to easily return the
 	 * correct result. This does mean we must save the next object to be
 	 * returned because it will have already been fetched from the sub-iterator.
 	 */
 	public E next() {
 		if (processingCommittedObjects) {
 			E objectToReturn = nextObject;
 			setNextObject();
 			lastObject = objectToReturn;
 			return objectToReturn;
 		} else {
 			lastObject = null;
 			return subIterator.next();
 		}
 	}
 	
 	public void remove() {
 		if (lastObject != null) {
			// Add to the deleted list
			deletedObjects.add(lastObject.getObjectKey());
 		} else {
 			// The last object returned was not a committed object but was
 			// added in this delta.
 			// Remove it from the added list.
 			subIterator.remove();
 		}
 	}
 
 	/**
 	 * Set nextObject to the first/next object from the committed list that has
 	 * not been marked for deletion, or, if there is no more such objects, set
 	 * up for returning the newly added objects by setting the flag and setting
 	 * the sub-iterator to be an iterator that iterates the newly added objects.
 	 */
 	private void setNextObject() {
 		E committedObject;
 		do {
 			if (!subIterator.hasNext()) {
 				processingCommittedObjects = false;
 				subIterator = addedObjects.iterator();
 				return;
 			}
 			committedObject = subIterator.next();
 		} while (deletedObjects.contains(committedObject.getObjectKey()));
 		
 		nextObject = transactionManager.getCopyInTransaction(committedObject);
 	}
 }
