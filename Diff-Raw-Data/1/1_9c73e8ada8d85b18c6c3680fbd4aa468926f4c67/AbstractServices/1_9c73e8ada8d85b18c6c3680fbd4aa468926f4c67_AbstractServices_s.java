 package com.mjeanroy.springhub.services;
 
 import com.mjeanroy.springhub.commons.reflections.ReflectionUtils;
 import com.mjeanroy.springhub.dao.GenericDao;
 import com.mjeanroy.springhub.models.entities.AbstractEntity;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.inject.Inject;
 import java.util.List;
 
 /**
  * Abstract services, implement crud operations on a given entity.
  *
  * @param <T> Entity class.
  */
 @Service
 public abstract class AbstractServices<T extends AbstractEntity> {
 
 	/** Generic type implemented with concrete service */
 	protected Class<T> type = null;
 
 	@Inject
 	private GenericDao genericDao;
 
 	public AbstractServices() {
 		this.type = (Class<T>) ReflectionUtils.getGenericType(getClass(), 0);
 	}
 
 	/**
 	 * Find item in database from its id.
 	 *
 	 * @param id Id to look for.
 	 * @return Item or null if id does not exist.
 	 */
 	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 	public T get(Long id) {
 		return genericDao.find(type, id);
 	}
 
 	/**
 	 * Find all items in database.
 	 *
 	 * @return All items.
 	 */
 	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 	public List<T> getAll() {
 		return genericDao.findAll(type);
 	}
 
 	/**
 	 * Count all items in database.
 	 *
 	 * @return Count.
 	 */
 	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 	public long count() {
 		return genericDao.count(type);
 	}
 
 	/**
 	 * Persist entity in database.
 	 *
 	 * @param entity Entity to persist.
 	 * @return Persisted entity.
 	 */
 	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
 	public T save(T entity) {
 		genericDao.persist(entity);
 		return entity;
 	}
 
 	/**
 	 * Delete entity in database.
 	 *
 	 * @param entity Entity to delete.
 	 */
 	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
 	public void delete(T entity) {
 		genericDao.remove(entity);
 	}
 }
