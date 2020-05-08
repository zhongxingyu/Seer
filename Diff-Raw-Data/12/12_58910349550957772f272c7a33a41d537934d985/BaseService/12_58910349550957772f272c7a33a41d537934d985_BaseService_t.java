 package com.wishop.service;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.springframework.stereotype.Service;
 
 import com.wishop.dao.BaseDAO;
 
 @Service
 public interface BaseService<X extends BaseDAO<T, Long>, T> {
 
 	/**
 	 * BaseObjectService service that gets a BaseObject by Id.
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param id Serializable
 	 * @return T BaseObject
 	 */
 	public T getById(Long id);
 	
 	/**
 	 * BaseObjectService service that gets a BaseObject by Id.
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param id Integer
 	 * @return T BaseObject
 	 */
 	public T getById(Integer id);
 	
 	/**
 	 * BaseObjectService service that gets a BaseObject by ID
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param id String
 	 * @return T BaseObject
 	 */
 	public T getById(String id);
 	
 	/**
 	 * BaseObjectService service that gets all BaseObject.<br>
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param id Integer
 	 */
 	public List<T> getAll();
 	
 	/**
 	 * BaseObjectService service that gets all BaseObject by the <b><code>deleted</code></b> flag.<br>
 	 * Uses @Cacheable to declares that the method’s return value should be cached
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param deleted boolean
 	 */
 	public List<T> getAll(boolean deleted);
 	
 	/**
 	 * BaseObjectService service that gets all BaseObject, except the <b>objectId</b>.<br>
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param id Long
 	 */
 	public List<T> getAll(Long id);
 	
 	/**
 	 * Get all objects where <code>deleted</code> is <b>false</b>, between the firstResult and N maxResults.
	 * Useful for pagination.<br>
	 * The minimun <code>firstResult</code> value is <b>1</b>. <br>
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param firstResult
 	 * @param maxResults
 	 * @return List of BaseObjects
 	 */
 	public List<T> getAll(int firstResult, int maxResults);
 	
 	/**
 	 * Get all objects based on the property deleted between the firstResult and N maxResults.
	 * Useful for pagination.<br>
	 * The minimun <code>firstResult</code> value is <b>1</b>. <br>
 	 * Uses @Cacheable to declares that the method’s return value should be cached. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param firstResult
 	 * @param maxResults
 	 * @param deleted
 	 * @return List of BaseObjects
 	 */
 	public List<T> getAll(int firstResult, int maxResults, boolean deleted);
 	
 	/**
 	 * BaseObjectService service that saves or updates a BaseObject. <br>
 	 * This method is used to provide a pointcut for the {@link com.wishop.service.aspects.AuditLog} aspect.<br>
 	 * Makes use of @CachePut to add a new value to the cache model.
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param entity BaseObject
 	 */
 	public void saveOrUpdate(T entity);
 	
 	/**
 	 * BaseObjectService service that saves or updates a BaseObject.<br>
 	 * This method is used to provide a pointcut for the {@link com.wishop.service.aspects.AuditLog} aspect.<br>
 	 * Makes use of @CachePut to add a new value to the cache model. <br>
 	 * EH Cache models involved: <b>userCache</b>
 	 * @param entity BaseObject
 	 */
 	public void save(T entity);
 	
 	/**
 	 * BaseObjectService service that update a BaseObject.<br>
 	 * This method is used to provide a pointcut for the {@link com.wishop.service.aspects.AuditLog} aspect.<br>
 	 * Makes use of @CachePut to add a new value to the cache model. <br>
 	 * EH Cache models involved: <b>userCache</b> 
 	 * @param entity BaseObject
 	 */
 	public void update(T entity);
 	
 	/**
 	 * BaseObjectService service that update a BaseObject.<br>
 	 * This method is used to provide a pointcut for the {@link com.wishop.service.aspects.AuditLog} aspect.<br>
 	 * Makes use of @CacheEvict to specify a flush action when the method is called. <br>
 	 * EH Cache models involved: <b>userCache</b> 
 	 * @param entity BaseObject
 	 * @throws ScribeException 
 	 */
	public void purge(T entity);
 	
 	/**
 	 * BaseObjectService service that update a BaseObject.<br>
 	 * This method is used to provide a pointcut for the {@link com.wishop.service.aspects.AuditLog} aspect.<br>
 	 * Makes use of @CacheEvict to specify a flush action when the method is called.<br> 
 	 * EH Cache models involved: <b>userCache</b> 
 	 * @param entity - BaseObject
 	 * @param deleted - the desired deleted state of the <b>BaseObject</b> in the database
 	 * @return result - the number of affected rows in the database
 	 */
 	public void delete(T entity, boolean deleted);
 	
 	/**
 	 * Set the BaseObjectDAO concrete class. Injected by the Spring Framework
 	 * @param dao subclass of BaseObjectDAO
 	 */
 	public void setDao(X dao);
 	
 	/**
 	 * Returns the BaseObjectDAO concrete class, that was injected by the Spring Framework
 	 */
 	public X getDao();
 	
 	/**
 	 * Logs all the actions related to an object, with the default Locale.
 	 * @param code
 	 * @param entity
 	 */
 	public void logAction(String action, T entity);
 	
 	/**
 	 * Logs all the actions related to an object, with the default Locale.
 	 * @param code
 	 * @param id
 	 */
 	public void logAction(String action, Long id);
 	
 	/**
 	 * Logs all the actions related to an object, depending on the Locale
 	 * @param code
 	 * @param entity
 	 * @param locale
 	 */
 	public void logAction(String code, T entity, Locale locale);	
 	
 	/**
 	 * Logs all the actions related to an object, depending on the Locale
 	 * @param code
 	 * @param id
 	 * @param locale
 	 */
 	public void logAction(String code, Long id, Locale locale);
 }
