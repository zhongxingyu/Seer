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
 
 package net.sf.jmoney.jdbcdatastore;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 
 /**
  * Every datastore implementation must provide an implementation
  * of the IListManager interface.  This implementation simply
  * uses the Vector class to keep a list of objects.
  *
  * @author Nigel Westbury
  */
 public class ListManagerCached<E extends ExtendableObject> implements IListManager<E> {
 
 	private static final long serialVersionUID = 867883048050895954L;
 
 	private SessionManager sessionManager;
 	private IDatabaseRowKey parentKey;
 	private ListPropertyAccessor<E> listProperty;
 	
 	private Vector<E> elements = null;
 	
 	public ListManagerCached(SessionManager sessionManager, IDatabaseRowKey parentKey, ListPropertyAccessor<E> listProperty) {
 		this.sessionManager = sessionManager;
 		this.parentKey = parentKey;
 		this.listProperty = listProperty;
 	}
 
 	public <F extends E> F createNewElement(ExtendableObject parent, ExtendablePropertySet<F> propertySet) {
 		// We must create the object before we persist it to the database.
 		// The reason why we must do this, and not simply write the
 		// default values, is that the constructor only uses the
 		// default values array as a guide.  For example, the constructor
 		// may replace a null timestamp with the current time, or
 		// a null currency with a default currency.
 		
  		// First we build the in-memory object.
 		// This is done here because in this case the object is always cached
 		// in memory.
 		
 		ObjectKey objectKey = new ObjectKey(sessionManager);
 		
 		F extendableObject = sessionManager.constructExtendableObject(propertySet, objectKey, parent, true);
 
 		objectKey.setObject(extendableObject);
 
 		/*
 		 * We can add elements without needed to build the list. If the list
 		 * does ultimately need to be built from the database, this object will
 		 * be included as it has been written to the database.
 		 */
 		if (elements != null) {
 			elements.add(extendableObject);
 		}
 		
 		// Now we insert the new row into the tables.
 
 		int rowId = sessionManager.insertIntoDatabase(propertySet, extendableObject, listProperty, parent);
 		objectKey.setRowId(rowId);
 
 		/*
 		 * Having created an object, set a weak reference to it in our weak refence map.
 		 */
 		// TODO: should this be done inside constructExtendableObject?
 		sessionManager.setMaterializedObject(propertySet, rowId, extendableObject);
 
 		return extendableObject;
 	}
 
 	public <F extends E> F createNewElement(ExtendableObject parent, ExtendablePropertySet<F> propertySet, Object[] values) {
 		// We must create the object before we persist it to the database.
 		// The reason why we must do this, and not simply write the
 		// default values, is that the constructor only uses the
 		// default values array as a guide.  For example, the constructor
 		// may replace a null timestamp with the current time, or
 		// a null currency with a default currency.
 		
  		// First we build the in-memory object.
 		// This is done here because in this case the object is always cached
 		// in memory.
 		
 		ObjectKey objectKey = new ObjectKey(sessionManager);
 		
 		F extendableObject = sessionManager.constructExtendableObject(propertySet, objectKey, parent, true, values);
 
 		objectKey.setObject(extendableObject);
 		
 		/*
 		 * We can add elements without needed to build the list. If the list
 		 * does ultimately need to be built from the database, this object will
 		 * be included as it has been written to the database.
 		 */
 		if (elements != null) {
 			elements.add(extendableObject);
 		}
 		
 		// Now we insert the new row into the tables.
 
 		int rowId = sessionManager.insertIntoDatabase(propertySet, extendableObject, listProperty, parent);
 		objectKey.setRowId(rowId);
 
 		/*
 		 * Having created an object, set a weak reference to it in our weak refence map.
 		 */
 		// TODO: should this be done inside constructExtendableObject?
 		sessionManager.setMaterializedObject(propertySet, rowId, extendableObject);
 
 		return extendableObject;
 	}
 	
 	public boolean remove(Object o) {
 		if (elements == null) {
 			buildCachedList();
 		}
 
 		boolean found = elements.remove(o);
 		
 		// Delete this object from the database.
 		if (found) {
 			ExtendableObject extendableObject = (ExtendableObject)o;
 			IDatabaseRowKey key = (IDatabaseRowKey)extendableObject.getObjectKey();
			boolean foundInDatabase = sessionManager.deleteFromDatabase(key.getRowId(), extendableObject);
 			if (!foundInDatabase) {
 				throw new RuntimeException("database inconsistent");
 			}
 		}
 		
 		return found;
 	}
 
 	public boolean add(E arg0) {
 		throw new RuntimeException("Method not supported");
 	}
 
 	public boolean addAll(Collection<? extends E> arg0) {
 		throw new RuntimeException("Method not supported");
 	}
 
 	public void clear() {
 		throw new RuntimeException("Method not supported");
 	}
 
 	public boolean contains(Object arg0) {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.contains(arg0);
 	}
 
 	public boolean containsAll(Collection<?> arg0) {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.containsAll(arg0);
 	}
 
 	public boolean isEmpty() {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.isEmpty();
 	}
 
 	public Iterator<E> iterator() {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.iterator();
 	}
 
 	public boolean removeAll(Collection<?> arg0) {
 		throw new RuntimeException("Method not supported");
 	}
 
 	public boolean retainAll(Collection<?> arg0) {
 		throw new RuntimeException("Method not supported");
 	}
 
 	public int size() {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.size();
 	}
 
 	public Object[] toArray() {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.toArray();
 	}
 
 	public <T> T[] toArray(T[] arg0) {
 		if (elements == null) {
 			buildCachedList();
 		}
 		return elements.toArray(arg0);
 	}
 
 
 	private void buildCachedList() {
 		elements = new Vector<E>();
 
 		/*
 		 * If the type of object held by the list is a type from which property
 		 * sets must be derived then we execute a query for each final property
 		 * set. This is necessary because different tables must be joined
 		 * depending on the actual property set.
 		 */		
 		try {
 			for (ExtendablePropertySet<? extends E> finalPropertySet: listProperty.getElementPropertySet().getDerivedPropertySets()) {
 				ResultSet resultSet = sessionManager.executeListQuery(parentKey, listProperty, finalPropertySet);
 				while (resultSet.next()) {
 					ObjectKey key = new ObjectKey(resultSet, finalPropertySet, parentKey, sessionManager);
 					E extendableObject = finalPropertySet.getImplementationClass().cast(key.getObject());
 					elements.add(extendableObject);
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		}
 	}
 }
