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
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.Iterator;
 
 import net.sf.jmoney.fields.EntryInfo;
 import net.sf.jmoney.model2.Entry;
 
 /**
  * This class is used to get the list of entries in a given account. Entries are
  * primarily listed under the transaction in which they belong. However, we want
  * a quick way to find all the entries in a given account.
  * <P>
  * This class provides the list by submitting a query to the database. A
  * suitable index should be created in the database.
  * 
  * @author Nigel Westbury
  */
 public class AccountEntriesList implements Collection<Entry> {
 	private SessionManager sessionManager;
 	private IDatabaseRowKey keyOfRequiredPropertyValue;
 	private String tableName;
 	private String columnName;
 	
 	public AccountEntriesList(SessionManager sessionManager, IDatabaseRowKey keyOfRequiredPropertyValue) {
 		this.sessionManager = sessionManager;
 		this.keyOfRequiredPropertyValue = keyOfRequiredPropertyValue;
 		
 		tableName = EntryInfo.getPropertySet().getId().replace('.', '_');
 		columnName = EntryInfo.getAccountAccessor().getLocalName();
 	}
 	
 	public int size() {
 		try {
 			ResultSet resultSet = sessionManager.getReusableStatement().executeQuery(
 					"SELECT COUNT(*) FROM " + tableName
 					+ " WHERE \"" + columnName + "\" = " + keyOfRequiredPropertyValue.getRowId());
 			resultSet.next();
 			int size = resultSet.getInt(1);
 			resultSet.close();
 			return size;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("SQL Exception: " + e.getMessage());
 		}
 	}
 	
 	public boolean isEmpty() {
 		try {
 			ResultSet resultSet = sessionManager.getReusableStatement().executeQuery(
 					"SELECT * FROM " + tableName
 					+ " WHERE \"" + columnName + "\" = " + keyOfRequiredPropertyValue.getRowId());
 			boolean hasNext = resultSet.next();
 			return !hasNext;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("SQL Exception: " + e.getMessage());
 		}
 	}
 	
 	public boolean contains(Object arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 	
 	public Iterator<Entry> iterator() {
 		try {
 			// TODO: This code will not work if the index is indexing
 			// objects of a derivible property set.  Table joins would
 			// be required in such a situation.
 			Statement stmt = sessionManager.getConnection().createStatement();
			ResultSet resultSet = stmt.executeQuery(
					"SELECT * FROM " + tableName 
					+ " WHERE \"" + columnName + "\" = " + keyOfRequiredPropertyValue.getRowId());
 			return new UncachedObjectIterator<Entry>(resultSet, EntryInfo.getPropertySet(), null, sessionManager);
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
 
 	public boolean add(Entry arg0) {
 		// The list is not cached.  
 		// We read from the database every time.
 		// There is therefore nothing to do here.
 		return true;
 	}
 	
 	public boolean remove(Object arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	public boolean containsAll(Collection<?> arg0) {
 		throw new RuntimeException("method not implemented");
 	}
 	
 	public boolean addAll(Collection<? extends Entry> arg0) {
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
