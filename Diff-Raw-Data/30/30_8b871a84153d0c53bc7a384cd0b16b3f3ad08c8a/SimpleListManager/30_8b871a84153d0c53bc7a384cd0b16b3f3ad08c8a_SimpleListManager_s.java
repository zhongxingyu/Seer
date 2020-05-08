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
 
 package net.sf.jmoney.serializeddatastore;
 
 import java.util.Vector;
 
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.IValues;
 import net.sf.jmoney.model2.ListKey;
 
 /**
  * Every datastore implementation must provide an implementation
  * of the IListManager interface.  This implementation simply
  * uses the Vector class to keep a list of objects.
  *
  * @author Nigel Westbury
  */
 public class SimpleListManager<E extends ExtendableObject> extends Vector<E> implements IListManager<E> {
 
 	private static final long serialVersionUID = 2090027937924066725L;
 
 	private SessionManager sessionManager;
 	
 	/**
 	 * The parent object and list property accessor for this list. This field is
 	 * never null.
 	 */
 	private ListKey<E> listKey;
 
 	public SimpleListManager(SessionManager sessionManager, ListKey<E> listKey) {
 	 	this.sessionManager = sessionManager;
 	 	this.listKey = listKey;
 	 }
 
 	public <F extends E> F createNewElement(ExtendablePropertySet<F> propertySet) {
 		SimpleObjectKey objectKey = new SimpleObjectKey(sessionManager);
 		F extendableObject = propertySet.constructDefaultImplementationObject(objectKey, listKey);
 		
 		objectKey.setObject(extendableObject);
 
 		add(extendableObject);
 		
 		// If an account is added then we
 		// must add a list that will contain the entries in the account.
 		if (extendableObject instanceof Account) {
 			Account account = (Account)extendableObject;
 			sessionManager.addAccountList(account);
 		}
 		
 		// If an entry is added then we
 		// must update the lists of entries in each account.
 		if (extendableObject instanceof Entry) {
 			Entry entry = (Entry)extendableObject;
 			if (entry.getAccount() != null) {
 				sessionManager.addEntryToList(entry.getAccount(), entry);
 			}
 		}
 		
         // This plug-in needs to know if a session has been
 		// modified so it knows whether the session needs to
 		// be saved.  Mark the session as modified now.
 		sessionManager.setModified();
 		
 		return extendableObject;
 	}
 
 	public <F extends E> F createNewElement(ExtendablePropertySet<F> propertySet, IValues values) {
 		SimpleObjectKey objectKey = new SimpleObjectKey(sessionManager);
 		F extendableObject = propertySet.constructImplementationObject(objectKey, listKey, values);
 		
 		objectKey.setObject(extendableObject);
 
 		add(extendableObject);
 		
 		// If an account is added then we
 		// must add a list that will contain the entries in the account.
 		if (extendableObject instanceof Account) {
 			Account account = (Account)extendableObject;
 			sessionManager.addAccountList(account);
 		}
 		
 		// If an entry is added then we
 		// must update the lists of entries in each account.
 		if (extendableObject instanceof Entry) {
 			Entry entry = (Entry)extendableObject;
 			if (entry.getAccount() != null) {
 				sessionManager.addEntryToList(entry.getAccount(), entry);
 			}
 		}
 		
         // This plug-in needs to know if a session has been
 		// modified so it knows whether the session needs to
 		// be saved.  Mark the session as modified now.
 		sessionManager.setModified();
 		
 		return extendableObject;
 	}
 
 	public void moveElement(E extendableObject, IListManager originalList) {
 		/*
 		 * This method moves the object in the underlying datastore.  However, the datastore
 		 * is a serialized datastore, so changes are not made to the datastore at this time.
 		 */
 	}
 
 	public boolean deleteElement(E extendableObject) {
 		// If an account is removed then we
 		// clear out the list.
 		if (extendableObject instanceof Account) {
 			Account account = (Account)extendableObject;
 			sessionManager.removeAccountList(account);
 		}
 		
 		// If an entry is removed then we
 		// must update the lists of entries in each account.
 		if (extendableObject instanceof Entry) {
 			Entry entry = (Entry)extendableObject;
 			if (entry.getAccount() != null) {
 				sessionManager.removeEntryFromList(entry.getAccount(), entry);
 			}
 		}
 		
         // This plug-in needs to know if a session has been
 		// modified so it knows whether the session needs to
 		// be saved.  Mark the session as modified now.
 		sessionManager.setModified();
 		
 		return remove(extendableObject);
 	}
 }
