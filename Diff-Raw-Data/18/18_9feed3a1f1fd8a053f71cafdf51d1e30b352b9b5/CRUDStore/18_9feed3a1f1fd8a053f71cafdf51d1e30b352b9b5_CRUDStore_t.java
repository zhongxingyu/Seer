 package com.hazardousholdings.unkj.db;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 
 public abstract class CRUDStore<K extends Key, V extends ModelEntity<K>> extends DBStore {
 
 	public CRUDStore(DataSource dataSource) {
 		super(dataSource);
 	}
 
 	public V getByKey(K key) {
 		return executeSelect(getByKeyQuery(key));
 	}
 
 	public V getByKey(K key, Connection connection) {
 		return executeSelect(getByKeyQuery(key), connection);
 	}
 
 	protected abstract SelectQuery<V> getByKeyQuery(K key);
 
 	public void put(V item) {
		item.beforePut();
 		if(item.getKey() != null) {
 			executeUpdate(getUpdateQuery(item));
 		} else {
 			K insertedKey = executeInsert(getInsertQuery(item));
 			item.setKey(insertedKey);
 		}
 	}
 
 	public void put(V item, Connection connection) {
		item.beforePut();
		if(item.getKey() != null) {
			executeUpdate(getUpdateQuery(item), connection);
		} else {
			K insertedKey = executeInsert(getInsertQuery(item), connection);
			item.setKey(insertedKey);
		}
 	}
 
 	protected abstract Query getUpdateQuery(V item);
 
 	protected abstract InsertQuery<K> getInsertQuery(V item);
 
 	public void delete(K key) {
 		executeUpdate(getDeleteQuery(key));
 	}
 
 	public void delete(K key, Connection connection) {
 		executeUpdate(getDeleteQuery(key), connection);
 	}
 
 	protected abstract Query getDeleteQuery(K key);
 
 }
