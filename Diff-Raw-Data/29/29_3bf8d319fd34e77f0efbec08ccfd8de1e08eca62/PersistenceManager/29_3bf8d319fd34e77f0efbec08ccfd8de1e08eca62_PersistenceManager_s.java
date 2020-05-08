 package com.example.support;
 
public interface PersistenceManager {
	void begin();
	void commit();
	void rollback();
 	
	<T> T persist(T entity);
	void delete(Object entity);
	<T> T load(Class<T> type, long id);
 }
