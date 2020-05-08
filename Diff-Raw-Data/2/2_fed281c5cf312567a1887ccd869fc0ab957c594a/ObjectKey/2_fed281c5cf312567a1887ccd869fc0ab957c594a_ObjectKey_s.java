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
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 
 import net.sf.jmoney.jdbcdatastore.SessionManager.DatabaseListKey;
 import net.sf.jmoney.model2.DataManager;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.ListKey;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.PropertySetNotFoundException;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionInfo;
 
 /**
  * This class provides an IObjectKey implementation for objects
  * that are persisted in a JDBC database.
  * 
  * There are three constructors, one is used when iterating a list and
  * a result set with all the required columns is available.  This constructor
  * materializes the object in the constructor.  Another constructor is used
  * when one object contains a reference to another object.  In this situation,
  * the id of the object is stored in this object key but the object is not
  * materialized until needed.  It is important to delay construction of the object
  * in this situation because otherwise the construction of an object could create
  * endless other objects to be constucted through references.
  * 
  * The third constructor is used when a new object is being created.
  * The row id is not known at construction time, but must be set once
  * the object has been inserted into the database.
  * 
  * Regardless of which constructor is used, once an object has been materialized,
  * this key will keep a strong reference to it.  The object is thus kept as long as
  * the object key is also kept.
  * 
  * Regardless of which constructor is called, the weak reference map must be consulted
  * first.  Otherwise we might have two instances of the same object, and datastore
  * implemetations are not allowed to do that.
  *
  * NOTE: Either the rowId AND propertySet are set, or the extendableObject
  * will contain a reference to the object.
  * 
  * @author Nigel Westbury
  */
 public class ObjectKey implements IDatabaseRowKey {
 	private ExtendablePropertySet<? extends ExtendableObject> basemostPropertySet;
 
 	/**
 	 * The key for the row in the database, or -1 if this object
 	 * has been constructed but not yet written to the database.
 	 * This value will be -1 only while a new object is being written
 	 * to the database and, as this application is single threaded,
 	 * users can assume that this value is never -1.
 	 */
 	private int rowId;
 	
 	/**
 	 * PropertySet for the type of this reference.
 	 * WARNING: This is not the actual property set for the object.
 	 * The object may have a property set that is derived from
 	 * the property set for the type of the reference.
 	 * The actual property set of the object cannot be determined
 	 * until the object is read from the database and therefore
 	 * is not passed to the constructor.
 	 */
 	private ExtendablePropertySet<? extends ExtendableObject> typedPropertySet;
 
 	private SessionManager sessionManager;
 	
 	// TODO: Should this be a weak reference?
 	// Or perhaps we should not have this field at all and always look it
 	// up in the weak reference map.
 	// Currently the code assumes that once we have the object, we don't need
 	// the information needed to obtain the object, so this would need a little
 	// work.
 	private ExtendableObject extendableObject = null;
 
 	/**
 	 * Construct an object key when used as a reference from
 	 * an extendable object.
 	 * 
 	 * @param PropertySet 
 	 * 		The property set for the type of this reference.
 	 * 		WARNING: This is not the actual property set for the object.
 	 * 		The object may have a property set that is derived from
 	 * 		the property set for the type of the reference.
 	 * 		The actual property set of the object cannot be determined
 	 * 		until the object is read from the database and therefore
 	 * 		is not passed to the constructor.
 	 */
 	ObjectKey(int rowId, ExtendablePropertySet<? extends ExtendableObject> typedPropertySet, SessionManager sessionManager) {
 		this.rowId = rowId;
 		this.typedPropertySet = typedPropertySet;
 		if (typedPropertySet == null) {
 			System.out.println("");
 		}
 		this.sessionManager = sessionManager;
 
 		// TODO: it may be more efficient for the caller to do this????
 		basemostPropertySet = SessionManager.getBasemostPropertySet(typedPropertySet);
 	}
 
 	/**
 	 * @param resultSet
 	 * @param finalPropertySet
 	 * @param listKey the key of the containing list, never null unless this is the
 	 * 			key to the session object
 	 * @param sessionManager
 	 * @throws SQLException
 	 */
 	<E extends ExtendableObject> ObjectKey(ResultSet resultSet, ExtendablePropertySet<E> finalPropertySet, DatabaseListKey<? super E> databaseListKey, SessionManager sessionManager) throws SQLException {
 		this.sessionManager = sessionManager;
 		rowId = resultSet.getInt("_ID");
 
 		// TODO: it may be more efficient for the caller to do this????
 		ExtendablePropertySet<?> basePropertySet = finalPropertySet; 
 		while (basePropertySet.getBasePropertySet() != null) {
 			basePropertySet = basePropertySet.getBasePropertySet();
 		}
 		basemostPropertySet = basePropertySet;
 
 		// TODO: Should materializeObject do this???
 		extendableObject = sessionManager.getObjectIfMaterialized(basemostPropertySet, rowId);
 		if (extendableObject == null) {
 			ListKey<? super E> listKey = (databaseListKey == null) ? null : sessionManager.constructListKey(databaseListKey);
 			extendableObject = sessionManager.materializeObject(resultSet, finalPropertySet, this, listKey);
 		}
 	}
 	
 	/**
 	 * This version of the constructor is used only when
 	 * a new object is being created.  The row id is not
 	 * known at this time and the object cannot be created
 	 * until after the key is created.
 	 * 
 	 * TODO: It may be possible to improve this by passing everything
 	 * to this constructor that is needed to construct the object,
 	 * and writing to the database first to get the row id.
 	 * 
 	 * For time being, this key will not be fully usable until
 	 * both setObject and setRowId have been called.
 	 * 
 	 * @param sessionManager
 	 */
 	ObjectKey(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 		this.rowId = -1;
 		this.extendableObject = null;
 		
 		// basemost property set is set when object is set.
 	}
 	
 	public ExtendableObject getObject() {
 
 		if (extendableObject != null) {
 			return extendableObject;
 		}
 		
 		/* 
 		 * See if the object is in our weak reference map.
 		 */
 		extendableObject = sessionManager.getObjectIfMaterialized(basemostPropertySet, rowId);
 		if (extendableObject != null) {
 			return extendableObject;
 		}
 		
 		if (extendableObject == null) {
 			/*
 			 * The object must be constructed.
 			 * 
 			 * If the class of extendable objects that may be referenced by this key
 			 * is an abstract class then the process is a two step process. The
 			 * first step is to read the base table. That table will have a column
 			 * that indicates the actual derived class of the object. A second
 			 * select statement must then be submitted that selects the columns from
 			 * the appropriate tables of additional properties for the derived
 			 * class.
 			 */
 			ExtendablePropertySet<?> finalPropertySet = null;
 			try {
 				
 				if (typedPropertySet.isDerivable()) {
 					/*
 					 * Get the base-most property set, because only the tables
 					 * for the base-most property sets contain the _PROPERTY_SET
 					 * columns from which we can determine the exact class of the object.
 					 */
 					ExtendablePropertySet<?> basemostPropertySet = typedPropertySet;
 					while (basemostPropertySet.getBasePropertySet() != null) {
 						basemostPropertySet = basemostPropertySet.getBasePropertySet();
 					}
 					
					String sql = "SELECT _PROPERTY_SET FROM "
 						+ basemostPropertySet.getId().replace('.', '_')
 						+ " WHERE \"_ID\" = ?";
 					System.out.println(sql);
 					PreparedStatement stmt = sessionManager.getConnection().prepareStatement(sql);
 					try {
 						stmt.setInt(1, rowId);
 						ResultSet rs = stmt.executeQuery();
 
 						// Get the final property set.
 						rs.next();
 						String id = rs.getString("_PROPERTY_SET");  // TODO: use index, faster
 
 						try {
 							finalPropertySet = PropertySet.getExtendablePropertySet(id);
 						} catch (PropertySetNotFoundException e1) {
 							// TODO: The most probable cause is that an object
 							// is stored in the database, but the plug-in that supports
 							// the object has now gone.
 							// We need to think about the proper way of
 							// handling this scenario.
 							e1.printStackTrace();
 							throw new RuntimeException("Property set stored in database is no longer supported by the installed plug-ins.");
 						}
 					} finally {
 						stmt.close();
 					}
 				} else {
 					finalPropertySet = typedPropertySet;
 				}
 
 				// Build the SQL statement that will return all
 				// the rows from the base and derived tables.
 				String sql = sessionManager.buildJoins(finalPropertySet);
 				sql += " WHERE " + finalPropertySet.getId().replace('.', '_') + ".\"_ID\"=?";
 
 				System.out.println(sql + " : " + rowId);
 				PreparedStatement stmt = sessionManager.getConnection().prepareStatement(sql);
 				stmt.setInt(1, rowId);
 				ResultSet rs = stmt.executeQuery();
 
 				rs.next();
 
 				extendableObject = sessionManager.materializeObject(rs, finalPropertySet, this);
 				
 				rs.close();
 			} catch (SQLException e) {
 				if (e.getSQLState().equals("24000") && finalPropertySet != null) {
 					/*
 					 * This error indicates there is no current row in the
 					 * result set which almost certainly means that the result
 					 * set contained no rows. This in turn implies a corrupted
 					 * database. The most likely situation is that a row exists
 					 * in a base table but the row does not exist in the table
 					 * of derived objects as would be expected by the value of
 					 * _PROPERTY_SET.
 					 * 
 					 * We build a message giving details of this possible
 					 * problem.
 					 */
 					String description = MessageFormat.format(
 							"The database may be corrupted.  A row exists in the {1} table with _ID of {0} and _PROPERTY_SET of {2} but no row exists with an _ID of {0} in one of the derived tables.",
 							rowId,
 							basemostPropertySet.getId().replace('.', '_'),
 							finalPropertySet.getId());
 					throw new RuntimeException(description, e);
 				} else {
 					e.printStackTrace();
 					throw new RuntimeException(e.getMessage(), e);
 				}
 			}
 		}
 		return extendableObject;
 	}
 
 	public int getRowId() {
 		return rowId;
 	}
 
 	/**
 	 * Although there will never be two instances of an ExtendableObject that
 	 * represent the same object, there may be two ObjectKey objects that are
 	 * keys to the same extendable object. This is because when one object
 	 * references another, the referenced object is not immediately constructed.
 	 * Instead we construct an object key that can create the object when
 	 * needed. Multiple instances of an object key are allowed as long as 1) the
 	 * getObject method returns the same instance of the extendable object when
 	 * called on all keys, and 2) all the object keys are 'equal' and return the
 	 * same hashcode.
 	 * <P>
 	 * Regardless of whether a direct reference to the instantiated object is
 	 * available or not, the row id and the base-most property set will also be
 	 * available. We therefore use these two fields to test for equality. We
 	 * also check the session manager because objects managed by different
 	 * session managers are considered different even if they represent the same
 	 * object. (In practice if the caller is comparing objects from different
 	 * session managers then there is probably a bug).
 	 */
 	@Override
 	public boolean equals(Object object) {
 		if (object instanceof ObjectKey) {
 			ObjectKey otherKey = (ObjectKey)object; 
 			return this.rowId == otherKey.getRowId()
 			    && this.basemostPropertySet == otherKey.basemostPropertySet
 			    && this.sessionManager == otherKey.sessionManager;
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public int hashCode() {
 		/*
 		 * We could include the basemostPropertySet, and even the
 		 * sessionManager, in the hash. However there are only 4 or so basemost
 		 * property sets and the session manager is most likely always the same
 		 * in any collection so it is not worth including these.
 		 */
 		return rowId;
 	}
 	
 	public void updateProperties(ExtendablePropertySet<?> actualPropertySet, Object[] oldValues, Object[] newValues) {
 		sessionManager.updateProperties(actualPropertySet, rowId, oldValues, newValues);
 	}
 
 	public Session getSession() {
 		return sessionManager.getSession();
 	}
 
 	public DataManager getDataManager() {
 		return sessionManager;
 	}
 
 	public <E extends ExtendableObject> IListManager<E> constructListManager(ListPropertyAccessor<E> listAccessor) {
 		DatabaseListKey<E> listKey = new SessionManager.DatabaseListKey<E>(this, listAccessor);
 		if (listAccessor == SessionInfo.getTransactionsAccessor()) {
 			return new ListManagerUncached<E>(sessionManager, listKey);
 		} else {
 			return new ListManagerCached<E>(sessionManager, listKey, false);
 		}
 	}
 
 	/**
 	 * This method is used only when a new object is
 	 * being created.  TODO: It may be possible to
 	 * restructure the code to do away with this.
 	 * 
 	 * @param extendableObject
 	 */
 	void setObject(ExtendableObject extendableObject) {
 		this.extendableObject = extendableObject;
 
 		// TODO: it may be more efficient for the caller to do this????
 		ExtendablePropertySet<?> basePropertySet = PropertySet.getPropertySet(extendableObject.getClass()); 
 		while (basePropertySet.getBasePropertySet() != null) {
 			basePropertySet = basePropertySet.getBasePropertySet();
 		}
 		basemostPropertySet = basePropertySet;
 	}
 
 	/**
 	 * Until an object has been persisted to the database, no
 	 * row id is available.  The row id is set to -1 initially.
 	 * When the object is persisted, this method must be called
 	 * to set the row id to the actual row id.
 	 * <P>
 	 * Although objects must be written to the database
 	 * immediately, because the getObject method reads the object
 	 * from the database, we cannot write the object to the database
 	 * until the object has been created, hence we need this method.
 	 *  
 	 * @param rowId The row id obtained when the object is
 	 * 			persisted in the database.
 	 */
 	public void setRowId(int rowId) {
 		this.rowId = rowId;
 	}
 }
