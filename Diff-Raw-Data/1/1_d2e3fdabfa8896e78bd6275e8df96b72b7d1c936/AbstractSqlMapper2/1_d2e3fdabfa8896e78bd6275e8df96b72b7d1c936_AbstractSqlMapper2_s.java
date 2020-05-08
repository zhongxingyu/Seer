 package com.gushuley.utils.orm.sql;
 
 import java.sql.*;
 import java.util.*;
 
 import com.gushuley.utils.orm.*;
 import com.gushuley.utils.orm.ORMObject.State;
 import com.gushuley.utils.orm.impl.AbstractMapper2;
 
 
 public abstract class AbstractSqlMapper2<T extends ORMObject<K>, K, C extends ORMContext>
 extends AbstractMapper2<T, K, C> 
 {
 	public AbstractSqlMapper2() {
 		this(false, false);
 	}
 
 	public AbstractSqlMapper2(boolean _short) {
 		this(_short, false);
 	}
 	
 	public AbstractSqlMapper2(boolean _short, boolean ordered) {
 		super(ordered);
 		this._short = _short;
 	}
 	
 	private final boolean _short;
 	
 	protected abstract String getConnectionKey();
 
 	public abstract K createKey(ResultSet rs) throws SQLException, ORMException;
 
 	protected T loadObject(ResultSet rs) throws SQLException, ORMException {
 		K key = createKey(rs);
 		synchronized (getRegistry()) {
 			T obj = getRegistry().get(key);
 			if (obj == null) {
 				obj = createInstance(key, rs);
 				obj.setORMState(State.LOADING);
 				getRegistry().put(obj.getKey(), obj);
 				try {
 					loadInstance(obj, rs);
 					obj.setORMState(State.CLEAN);
 				} catch (SQLException e) {
 					getRegistry().remove(key);
 					throw e;
 				} catch (Error e) {
 					getRegistry().remove(key);
 					throw e;
 				} catch (RuntimeException e) {
 					getRegistry().remove(key);
 					throw e;
 				}
 			}
 			return obj;
 		}
 	}
 
 	protected abstract void loadInstance(T obj, ResultSet rs)
 			throws SQLException, ORMException;
 
 	protected abstract T createInstance(K key, ResultSet rs)
 			throws SQLException;
 
 	private class GetForKeyCb implements GetQueryCallback<T> {
 		public GetForKeyCb(K id) {
 			this.id = id;
 		}
 		private K id;
 
 		public String getSql() throws ORMException {
 			return getSelectSql();
 		}
 
 		public void setParams(PreparedStatement stm, T obj) throws SQLException, ORMException {
 			setSelectStatementParams(stm, id);
 		}
 
 		public void executeStep(Connection cnn, T obj) throws SQLException {
 		}
 	}
 	
 	public T getById(K id) throws ORMException {
 		synchronized (getRegistry()) {
 			if (_short) {
 				if (getRegistry().size() == 0) {
 					getAll();
 				}
 			}
 
 			if (getRegistry().containsKey(id)) {
 				return getRegistry().get(id);
 			}
 		}
 
 		for (T o : getCollectionForCb(new GetForKeyCb(id))) {
 			return o;
 		}
 		return null;
 	}
 
 	public void refresh(T object) throws ORMException {
 		Connection cnn = ctx.getConnection(getConnectionKey(), false);
 		try {
 			PreparedStatement stm = cnn.prepareStatement(
 					getSelectSql());
 			try {
 				setSelectStatementParams(stm, object.getKey());
 				ResultSet rs = stm.executeQuery();
 				if (rs.next()) {
 					State oldState = object.getORMState();
 					try {							
 						object.setORMState(State.LOADING);
 						loadInstance(object, rs);
 					}
 					finally {
 						object.setORMState(oldState);
 					}
 					object.setORMState(State.CLEAN);
 				}
 			} finally {
 				stm.close();
 			}
 		} catch (SQLException e) {
 			throw new ORMException(e);
 		} finally {
 			ctx.releaseConnection(cnn);
 		}
 	}
 
 	protected abstract void setSelectStatementParams(PreparedStatement stm, K id)
 			throws SQLException;
 
 	protected abstract String getSelectSql();
 
 	protected Comparator<T> getComparator() {
 		return null;
 	}
 
 	public Collection<T> getAll() throws ORMException {
 		synchronized (getRegistry()) {
 			if (!_short || getRegistry().size() == 0) {
 				getCollectionForCb(new GetQueryCallback<T>() {
 					public String getSql() throws ORMException {
 						return getSelectAllSql();
 					}
 		
 					public void setParams(PreparedStatement stm, T obj) throws SQLException, ORMException {
 						setSelectAllStatementParams(stm);
 					}			
 		
 					public void executeStep(Connection cnn, T obj) throws SQLException {
 					}
 				});
 			}
 			List<T> all = new ArrayList<T>();
 			all.addAll(getRegistry().values());
 			if (getComparator() != null) {
 				Collections.sort(all, getComparator());
 			}
 			return all;
 		}
 	}
 	
 	public Collection<T> getCollectionForCb(GetQueryCallback<T> cb) throws ORMException {
 		Connection cnn = ctx.getConnection(getConnectionKey(), false);
 		try {
 			PreparedStatement stm = cnn.prepareStatement(cb.getSql());
 			try {
 				cb.setParams(stm, null);
 				ResultSet rs = stm.executeQuery();
 				List<T> all = new ArrayList<T>();
 				while (rs.next()) {
 					all.add(loadObject(rs));
 				}
 				if (getComparator() != null) {
 					Collections.sort(all, getComparator());
 				}
 				return all;
 			} finally {
 				stm.close();
 			}
 		} catch (SQLException e) {
 			throw new ORMException(e);
 		} finally {
 			ctx.releaseConnection(cnn);
 		}		
 	}
 
 	protected abstract void setSelectAllStatementParams(PreparedStatement stm)
 			throws ORMException;
 
 	protected abstract String getSelectAllSql();
 
 	abstract protected GetQueryCallback<T> getInsertQueryCB();
 
 	abstract protected GetQueryCallback<T> getUpdateQueryCB();
 
 	abstract protected GetQueryCallback<T> getDeleteQueryCB();
 
 	public void commit() throws ORMException {
 		try {
 			executeBatchForObjects(getInsertQueryCB(), State.NEW);
 		} catch (SQLException e) {
 			throw new ORMException("DB error inserting objects at mapper " + getClass().getName() + ": " + e.getMessage(), e);
 		}
 		try {
 			executeBatchForObjects(getDeleteQueryCB(), State.DELETED);
 		} catch (SQLException e) {
 			throw new ORMException("DB error deleting objects at mapper " + getClass().getName() + ": " + e.getMessage(), e);
 		}
 		try {
 			executeBatchForObjects(getUpdateQueryCB(), State.DIRTY);
 		} catch (SQLException e) {
 			throw new ORMException("DB error updating objects at mapper " + getClass().getName() + ": " + e.getMessage(), e);
 		}
 	}
 
 	public void setClean() {
 		Collection<K> toRemove = new ArrayList<K>();
 		for (T obj : getRegistry().values()) {
 			if (obj.getORMState() == State.DELETED) {
 				toRemove.add(obj.getKey());
 			}
 		}
 		for (K key : toRemove) {
 			getRegistry().remove(key);
 		}
 	}
 
 	private void executeBatchForObjects(GetQueryCallback<T> queryCB, State state)
 			throws SQLException, ORMException 
 	{
 		Connection cnn = null;
 		try {
 			PreparedStatement stm = null;
 			try {
 				for (T obj : getRegistry().values()) {
 					if (obj.getORMState() == state) {
 						if (stm == null) {
 							cnn = ctx.getConnection(getConnectionKey(), true);
 							stm = cnn.prepareStatement(queryCB.getSql());
 						}
 						queryCB.setParams(stm, obj);
 						stm.executeUpdate();
 						queryCB.executeStep(cnn, obj);
 					}
 				}
 			} finally {
 				if (stm != null) {
 					stm.close();
 				}
 			}
 		} finally {
 			if (cnn != null)
 				getContext().releaseConnection(cnn);
 		}
 	}
 	
 	protected Connection getConnection(boolean isMutable) throws ORMException {
 		return ctx.getConnection(getConnectionKey(), isMutable);		
 	}
 
 	protected void releaseConnection(Connection cnn) throws ORMException {
 		ctx.releaseConnection(cnn);		
 	}
 
 	protected int getSqNextNumberInt(String sqName, SqlDialect dialect) throws ORMException {
 		Connection cnn = getConnection(false);		
 		try {
 			final String text;
 			if (dialect == SqlDialect.ORACLE) {
 				text = "SELECT " + sqName + ".NEXTVAL id FROM dual";
 			} else if (dialect == SqlDialect.POSTGRES) {
 				text = "SELECT nextval('" + sqName + "') AS id";				
 			} else {
 				throw new ORMException("Unknown SQL dialect " + dialect);
 			}
 			final PreparedStatement stm = cnn.prepareStatement(text);
 			try {
 				ResultSet set = stm.executeQuery();
 				if (set.next()) {
 					return set.getInt("id");
 				}
 				throw new ORMException("Cannot allocate value for sequence: " + sqName);
 			}
 			finally {
 				stm.close();
 			}
 		} catch (SQLException e) {
 			throw new ORMException("Cannot allocate value for sequence: " + sqName, e);
 		} finally {
 			releaseConnection(cnn);
 		}
 	}
 
 	protected int getSqNextNumberInt(String sqName) throws ORMException {
 		return getSqNextNumberInt(sqName, SqlDialect.ORACLE);
 	}
 
 	protected long getSqNextNumberLong(String sqName, SqlDialect dialect) throws ORMException {
 		Connection cnn = getConnection(false);		
 		try {			
 			final String text;
 			if (dialect == SqlDialect.ORACLE) {
 				text = "SELECT " + sqName + ".NEXTVAL id FROM dual";
 			} else if (dialect == SqlDialect.POSTGRES) {
 				text = "SELECT nextval('" + sqName + "') AS id";				
 			} else {
 				throw new ORMException("Unknown SQL dialect " + dialect);
 			}
 			final PreparedStatement stm = cnn.prepareStatement(text);
 			try {
 				ResultSet set = stm.executeQuery();
 				if (set.next()) {
 					return set.getLong("id");
 				}
 				throw new ORMException("Cannot allocate value for sequence: " + sqName);
 			}
 			finally {
 				stm.close();
 			}
 		} catch (SQLException e) {
 			throw new ORMException("Cannot allocate value for sequence: " + sqName, e);
 		} finally {
 			releaseConnection(cnn);
 		}
 	}
 
 	protected long getSqNextNumberLong(String sqName) throws ORMException {
 		return getSqNextNumberLong(sqName, SqlDialect.ORACLE);
 	}
 }
