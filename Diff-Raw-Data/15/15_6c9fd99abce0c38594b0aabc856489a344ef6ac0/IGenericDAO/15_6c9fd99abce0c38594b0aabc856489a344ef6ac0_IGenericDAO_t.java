 package au.org.paperminer.dao;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import org.hibernate.Criteria;
 
 public interface IGenericDAO<T, ID extends Serializable> {
 
 	/**
 	 * Get the Class of the entity.
 	 * 
 	 * @return the class
 	 */
 	Class<T> getEntityClass();
 
 	/**
 	 * Find an entity by its primary key
 	 * 
 	 * @param id
 	 *            the primary key
 	 * @return the entity
 	 */
 	T findById(final ID id);
 
 	/**
 	 * Load all entities.
 	 * 
 	 * @return the list of entities
 	 */
 	List<T> findAll();
 
 	/**
 	 * Find entities based on an example.
 	 * 
 	 * @param exampleInstance
 	 *            the example
 	 * @return the list of entities
 	 */
 	List<T> findByExample(final T exampleInstance);
 
 	/**
 	 * Find using a named query. Support by EntityManager.
 	 * 
 	 * @param queryName
 	 *            the name of the query
 	 * @param params
 	 *            the query parameters
 	 * 
 	 * @return the list of entities
 	 */
 	List<T> findByNamedQuery(final String queryName, Object... params);
 
 	/**
 	 * Find using a named query. Support by EntityManager.
 	 * 
 	 * @param queryName
 	 *            the name of the query
 	 * @param params
 	 *            the query parameters
 	 * 
 	 * @return the list of entities
 	 */
 	List<T> findByNamedQueryAndNamedParams(final String queryName,
 			final Map<String, ? extends Object> params);
 
 	/**
 	 * Count all entities.
 	 * 
 	 * @return the number of entities
 	 */
	Long countAll();
 
 	/**
 	 * Count entities based on an example.
 	 * 
 	 * @param exampleInstance
 	 *            the search criteria
 	 * @return the number of entities
 	 */
 	int countByExample(final T exampleInstance);
 
 	/**
 	 * save an entity. This can be either a INSERT or UPDATE in the database.
 	 * 
 	 * @param entity
 	 *            the entity to save
 	 * 
 	 * @return the saved entity
 	 */
 	T save(final T entity);
 
 	/**
 	 * delete an entity from the database.
 	 * 
 	 * @param entity
 	 *            the entity to delete
 	 */
 	void delete(final T entity);
 
 	/**
 	 * Begins a transaction if it is not active
 	 */
 	void beginTransaction();
 
 	/**
 	 * Commits if transaction is active
 	 */
 	void commit();
 
 	/**
 	 * Rollback if transaction is active
 	 */
 	void rollback();
 
 	/**
 	 * Retrieve name of the persistent class
 	 */
 	@Override
 	String toString();
 
 	/**
 	 * To flush and clear the session and keep memory for hibernate small
 	 */
 	void flushAndClear();
 
 	/**
 	 * Abstract implementation of generic DAO.
 	 * 
 	 * @param <T>
 	 *            entity type, it must implements at least <code>IEntity</code>
 	 * @param <I>
 	 *            entity's primary key, it must be serializable
 	 * @see IEntity
 	 */
 	Object findUniqueByCriteria(
 			Criteria criteria)
 			throws NonUniqueResultException, NoResultException;
 
 	/**
 	 * To execute HQL.
 	 */
 	int executeHQLUpdate(String hql);
 
 	/**
 	 * To execute native SQL query.
 	 */
 	int executeSQLUpdate(String sql);
 
 	/**
 	 * This method will run the native SQL query. Also will accept parameters
 	 * 
 	 * @param sql
 	 * @param parameterLst
 	 * @return
 	 */
 	int executeSQLUpdateWithParam(String sql, List<String> parameterLst);
 }
