 package com.example.support;
 
public class PersistenceManager {
	void begin() {}
	void commit() {}
	void rollback() {}
 	
	<T> T persist(T entity) { return entity; }
	void delete(Object entity) {}
	
	<T> T load(Class<T> type, long id) {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 }
