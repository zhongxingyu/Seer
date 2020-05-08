 /**
  * Created on May 31st, 2006
  * 
  * 
  * Title: DxSetOfResource.java 
  *
  * Copyright (c) 2001 by rgr.
  * All rights reserved.
  *
  *
  * This software is the confidential and proprietary information
  * of rgr. ("Confidential Information").  You
  * shall not disclose such Confidential Information and shall use
  * it only in accordance with the terms of the license agreement
  * you entered into with rgr.
  */
 package dInternal.dData;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * Nicolas Calderon
  * 
  * Description: DxSetOfResource is a class used to:
  * <p>
  * Contain a set of ressource and manage keys attribution. This class will
  * usually be inherited to add properties. This set only manage comonly used
  * attributes: keys and names.
  * <p>
  * 
  */
 public abstract class DxSetOfResources implements Iterable <DxResource>{
 
 	private Vector<DxResource> _vResourceSortedByKey;
 
 	private Vector<DxResource> _vResourceSortedByName;
 
 	private boolean _bSorted;
 
 	/**
	 * Default constructor. Initialize vectors.
 	 */
 	public DxSetOfResources() {
 		_vResourceSortedByKey = new Vector<DxResource>();
 		_vResourceSortedByName = new Vector<DxResource>();
 		_bSorted = false;
 	}
 
 	/**
 	 * Adds a ressource to the set if it's key was not already in use within the
 	 * set.
 	 * 
 	 * Function is protected to avoid misuse by user. For instance, if
 	 * addResource was public, a Site could be added to a SetOfInstructor
 	 * 
 	 * @param dxrRes
 	 *            Resource that has to be added to the set
 	 */
 	protected final void addResource(DxResource dxrRes) {
 		if (getResource(dxrRes.getKey()) == null) {
 			_vResourceSortedByKey.add(dxrRes);
 			_vResourceSortedByName.add(dxrRes);
 			_bSorted = false;
 		}
 	}
 	
 //	protected final DxResource getElement(int i) {
 //		return _vResourceSortedByName.elementAt(i);
 //	}
 //
 //	protected final void setElementAt(DxResource dr, int i) {
 //		 _vResourceSortedByName.setElementAt(dr, i);
 //	}
 	/**
 	 * Merge a new set of ressources into the current one. Function is protected
 	 * to allow type control from child (avoid adding a SetOfInstructor to a
 	 * SetOfCategory).
 	 * 
 	 * @param dxrRes
 	 *            Resource that has to be added to the set
 	 */
 	protected final void addSetOfResources(DxSetOfResources dxsorNew) {
 		Iterator<DxResource> itNew = dxsorNew.iterator();
 		DxResource dxrNew;
 		DxResource dxrFound;
 
 		while (itNew.hasNext()) {
 			dxrNew =  itNew.next();
 			dxrFound = this.findEquivalent(dxrNew);
 			if (dxrFound == null) {
 				this.addResource(dxrNew);
 			} else {
 				merge(dxrFound, dxrNew);
 			}
 		}
 		_bSorted = false;
 	}
 
 	/**
 	 * This method is used for tests only, to make sure add and remove will
 	 * correctly manage both vectors
 	 * 
 	 * @return true if name ordered vector and key ordered vector are the same
 	 *         size
 	 */
 	public final boolean areVectorsSync() {
 		return _vResourceSortedByKey.size() == _vResourceSortedByName.size();
 	}
 
 	/**
 	 * This function must be implemented in subclasses so addSetOfRessources
 	 * knows what Ressource is already in the set
 	 * 
 	 * @param dxrSearch
 	 *            Resource that should be searched in the current set. If an
 	 *            equivalent is found, it must be returned. If none is found,
 	 *            null will be returned.
 	 */
 	protected abstract DxResource findEquivalent(DxResource dxrSearch);
 
 	public final boolean isEqual(DxSetOfResources dxsorOther) {
 		this.sort();
 		dxsorOther.sort();
 
 		if (this.size() != dxsorOther.size()) {
 			return false;
 		}
 		Iterator<DxResource> itThis = this._vResourceSortedByName.iterator();
 		Iterator <DxResource>itOther = dxsorOther._vResourceSortedByName.iterator();
 		DxResource dxrThis;
 		DxResource dxrOther;
 
 		while (itThis.hasNext()) {
 			dxrThis = itThis.next();
 			dxrOther =  itOther.next();
 			if (!dxrThis.isEqual(dxrOther)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Retreives a ressource in the set
 	 * 
 	 * @param lKey
 	 *            Key of the resource that has to be returned
 	 * @return String The name of the instructor, null if the index was invalid
 	 */
 	public final DxResource getResource(long lKey) {
 		int nIndex = getSortedKeyIndex(lKey);
 		if (nIndex >= 0) {
 			return _vResourceSortedByKey.get(nIndex);
 		}
 		return null;
 	}
 
 	/**
 	 * Retreives a ressource by its name. If there are two ressources with the
 	 * same name, there is no guaranty on which ressource will be returned
 	 * 
 	 * @param sName
 	 *            Name of the ressource we need
 	 * @return The ressource
 	 */
 	public final DxResource getResource(String sName) {
 		sort();
 		DxResource dxrTemp = new DxResource(0, sName);
 		int nIndex = Collections.binarySearch(_vResourceSortedByName, dxrTemp,
 				DxResource.NameComparator);
 		if (nIndex >= 0) {
 			return _vResourceSortedByName.get(nIndex);
 		}
 		return null;
 	}
 
 	/**
 	 * Retreives the name of a ressource in the set
 	 * 
 	 * @param lKey
 	 *            Key of the instructor whose name is wanted
 	 * @return The name of the instructor, null if the index was invalid
 	 */
 	public final String getResourceName(long lKey) {
 
 		DxResource dxrTemp = getResource(lKey);
 		if (dxrTemp != null) {
 			return dxrTemp.getName();
 		}
 		return null;
 	}
 
 	/**
 	 * Retreives the key of a ressource by its name. If there are two ressources
 	 * with the same name, there is no guaranty on which ressource key will be
 	 * returned
 	 * 
 	 * @param sName
 	 *            Name of the ressource we need the key
 	 * @return The key of ressour sName, -1 if instructor not found
 	 */
 	public final long getResourceKey(String sName) {
 		DxResource dxrTemp = getResource(sName);
 		if (dxrTemp != null) {
 			return dxrTemp.getKey();
 		}
 		return -1;
 	}
 
 	// // TODO: Should be removed
 	// public final DxResource[] getResourcesSortedByName() {
 	// sort();
 	// return _vResourceSortedByName.toArray(new DxResource[this.size()]);
 	// }
 	//
 	// // TODO: Should be removed
 	// public final DxResource[] getResourcesSortedByKey() {
 	// sort();
 	// return _vResourceSortedByKey.toArray(new DxResource[this.size()]);
 	// }
 
 	public final Vector<DxResource> getNameSortedRessources() {
 		sort();
 		return _vResourceSortedByName;
 	}
 
 	public  final Vector<DxResource> getKeySortedRessources() {
 		sort();
 		return _vResourceSortedByKey;
 	}
 
 	// TODO: Should be removed
 	public Vector<String> getNamesVector() {
 		sort();
 		Vector<String> vReturn = new Vector<String>();
 		Iterator<DxResource> itNames = _vResourceSortedByName.iterator();
 		while (itNames.hasNext()) {
 			vReturn.add(itNames.next().toString());
 		}
 
 		return vReturn;
 	}
 
 	/**
 	 * Return an iterator that will iterate on all resources. It iterates
 	 * thhrough resource in ascending key order.
 	 */
 	public Iterator<DxResource> iterator() {
 		sort();
 		return _vResourceSortedByKey.iterator();
 	}
 
 	/**
 	 * This function must be implemented in subclasses. When an equivalent
 	 * resource is found during addSetOfResources, this function will be called
 	 * with current resource and found resource. Client class must merge
 	 * pertinent informations from dxrNew in dxrModify.
 	 * 
 	 * @param dxrModify
 	 *            Resource that was in the original set (the one on which
 	 *            addSetOfResource was called). This resource is currently in
 	 *            the set and every change to this object will be reflected in
 	 *            the set. If this resource is not modified, it will remain
 	 *            intact, losing any data that should be added from dxrNew.
 	 * @param dxrNew
 	 *            Resource found that has an equivalent in the original set of
 	 *            resource. If this resource is modified, it will be in the new
 	 *            set of resource, but hose changes wont appear in the old one.
 	 * 
 	 */
 	protected abstract void merge(DxResource dxrModify, DxResource dxrNew);
 
 	/**
 	 * Removes an instructor from the set
 	 * 
 	 * @param lKey
 	 *            Key of the instructor to be removed
 	 */
 	public final void removeResource(long lKey) {
 		int nIndexKey = getSortedKeyIndex(lKey);
 		int nIndexName = getSortedNameIndex(lKey);
 		if (nIndexKey != -1 && nIndexName != -1) {
 			_vResourceSortedByKey.remove(nIndexKey);
 			_vResourceSortedByName.remove(nIndexName);
 //		} else {
 //			System.out
 //					.println("dInternal.dData.DxSetOfResources: A ressource was found in a vector but not the other");
 		}
 	}
 
 	/**
 	 * Return the number of ressource currently in the set
 	 * 
 	 * @return The number of instructor currently in the set
 	 */
 	public final int size() {
 		return _vResourceSortedByKey.size();
 	}
 
 	/**
 	 * Searches the index of a ressource in the key sorted vector given it's
 	 * key. We use Collection.binarySearch as Resources are sorted
 	 * 
 	 * @param lKey
 	 *            Key of the resource we want the index
 	 * @return Index of the resource if found, -1 if not
 	 */
 	private int getSortedKeyIndex(long lKey) {
 		sort();
 		DxResource dxrTemp = new DxResource(lKey, null);
 		int nIndex = Collections.binarySearch(_vResourceSortedByKey, dxrTemp,
 				DxResource.KeyComparator);
 		if (nIndex >= 0) {
 			return nIndex;
 		}
 		return -1;
 	}
 
 	/**
 	 * Searches the index of a resource in the name sorted vector given it's
 	 * key. We have to iterate through all resource to find the one that matches
 	 * the key since keys are out of order in th name vector.
 	 * 
 	 * @param lKey
 	 *            Key of the resource we want the index
 	 * @return Index of the resource if found, -1 if not
 	 */
 	private int getSortedNameIndex(long lKey) {
 		sort();
 		Iterator<DxResource> it = _vResourceSortedByName.iterator();
 
 		for (int i = 0; it.hasNext(); i++) {
 			if (it.next().getKey() == lKey)
 				return i;
 		}
 		return -1;
 	}
 
 	/**
 	 * Sort vectors
 	 */
 	private void sort() {
 		if (!_bSorted) {
 			Collections.sort( _vResourceSortedByName,
 					DxResource.NameComparator);
 			Collections.sort( _vResourceSortedByKey,
 					DxResource.KeyComparator);
 		}
 		_bSorted = true;
 	}
 }
