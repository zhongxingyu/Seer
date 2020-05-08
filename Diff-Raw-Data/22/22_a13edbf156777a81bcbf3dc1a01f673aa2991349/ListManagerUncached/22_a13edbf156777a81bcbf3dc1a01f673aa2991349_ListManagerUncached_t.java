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
 
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 
 /**
  * Every datastore implementation must provide an implementation
  * of the IListManager interface.  This implementation supplies
  * an implementation that executes each method by submitting
  * an SQL statement to the database.
  *
  * @author Nigel Westbury
  */
 public class ListManagerUncached<E extends ExtendableObject> implements IListManager<E> {
 	private IDatabaseRowKey parentKey;
 	private SessionManager sessionManager;
 	private ListPropertyAccessor<E> listProperty;
 	
 	public ListManagerUncached(SessionManager sessionManager, IDatabaseRowKey parentKey, ListPropertyAccessor<E> listProperty) {
 		this.parentKey = parentKey;
 		this.sessionManager = sessionManager;
 		this.listProperty = listProperty;
 	}
 	
 	public <F extends E> F createNewElement(ExtendableObject parent, ExtendablePropertySet<F> propertySet) {
  		/*
 		 * First build the in-memory object. Even though the object is not
 		 * cached in the parent list property, the object must be constructed to
 		 * get the default values to be written to the database and the object
 		 * must be constructed so it can be returned to the caller.
 		 */
 		ObjectKey objectKey = new ObjectKey(sessionManager);
 		
 		/*
 		 * Constructing the object means constructing the object key. Both
 		 * contain a reference to the other, so they have the same lifetime.
 		 */
 
 		F extendableObject = sessionManager.constructExtendableObject(propertySet, objectKey, parent, false);
 		objectKey.setObject(extendableObject);
 		
 		// Insert the new object into the tables.
 		
 		int rowId = sessionManager.insertIntoDatabase(propertySet, extendableObject, listProperty, parent);
 		objectKey.setRowId(rowId);
 		
 		return extendableObject;
 	}
 	
 	public <F extends E> F createNewElement(ExtendableObject parent, ExtendablePropertySet<F> propertySet, Object[] values) {
  		/*
 		 * First build the in-memory object. Even though the object is not
 		 * cached in the parent list property, the object must be constructed to
 		 * get the default values to be written to the database and the object
 		 * must be constructed so it can be returned to the caller.
 		 */
 		ObjectKey objectKey = new ObjectKey(sessionManager);
 		
 		/*
 		 * Constructing the object means constructing the object key. Both
 		 * contain a reference to the other, so they have the same lifetime.
 		 */
 
 		F extendableObject = sessionManager.constructExtendableObject(propertySet, objectKey, parent, false, values);
 		objectKey.setObject(extendableObject);
 		
 		// Insert the new object into the tables.
 		
 		int rowId = sessionManager.insertIntoDatabase(propertySet, extendableObject, listProperty, parent);
 		objectKey.setRowId(rowId);
 		
 		return extendableObject;
 	}
 
 	public int size() {
 		try {
 			String tableName = listProperty.getElementPropertySet().getId().replace('.', '_');
 			String columnName = listProperty.getName().replace('.', '_');
 			ResultSet resultSet = sessionManager.getReusableStatement().executeQuery(
 					"SELECT COUNT(*) FROM " + tableName
 					+ " WHERE \"" + columnName + "\" = " + parentKey.getRowId());
 			resultSet.next();
 			int size = resultSet.getInt(1);
 			resultSet.close();
 			return size;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		}
 	}
 	
 	public boolean isEmpty() {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public boolean contains(Object o) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public Iterator<E> iterator() {
 		/*
 		 * We execute a SQL statement and pass the result set to an
 		 * UncachedObjectIterator object which will return the entries in the
 		 * result set. However, we must create a new statement because the
 		 * iterator is being returned from this method call.
 		 * 
 		 * This class only supports lists where the element type is a final
 		 * property set. Therefore we know the exact type of every element in
 		 * the list before we execute any query. This saves us from having to
 		 * iterate over the final property sets (like the ListManagerCached
 		 * object has to).
 		 * 
 		 * The UnchachedObjectIterator is reponsible for closing the result set
 		 * and the associated statement.
 		 */		
 		try {
 			ResultSet resultSet = sessionManager.executeListQuery(parentKey, listProperty, listProperty.getElementPropertySet());
 			return new UncachedObjectIterator<E>(resultSet, listProperty.getElementPropertySet(), parentKey, sessionManager);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		}
 	}
 
 	public Object[] toArray() {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public <T> T[] toArray(T[] arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public boolean add(E extendableObject) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public boolean remove(Object o) {
 		// Delete this object from the database.
 		ExtendableObject extendableObject = (ExtendableObject)o;
 		IDatabaseRowKey key = (IDatabaseRowKey)extendableObject.getObjectKey();
		return sessionManager.deleteFromDatabase(key);
 	}
 
 	public boolean containsAll(Collection<?> arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public boolean addAll(Collection<? extends E> arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public boolean removeAll(Collection<?> arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public boolean retainAll(Collection<?> arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 
 	public void clear() {
 		throw new RuntimeException("method not implemented");
 	}
 }
