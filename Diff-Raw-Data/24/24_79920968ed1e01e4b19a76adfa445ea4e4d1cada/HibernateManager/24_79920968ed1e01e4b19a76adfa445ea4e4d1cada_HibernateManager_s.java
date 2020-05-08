 package com.pingpong.core.hibernate;
 
 import com.pingpong.domain.Entity;
 import org.hibernate.HibernateException;
 import org.hibernate.LockOptions;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 
 import java.io.Serializable;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * @author Artur Zhurat
  * @version 1.0
  * @since 25/12/2011
  */
 
 public class HibernateManager {
 	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateManager.class);
 
 	private SessionFactory sessionFactory;
 
 	public Serializable insertEntity(final Entity<? extends Serializable> entity) throws HibernateException {
 		checkNotNull(entity);
 
 		final Serializable id = getCurrentSession().save(entity);
 
 		LOGGER.info("Inserted entity with id = '{}'", id);
 
 		return id;
 	}
 
 	public Entity<? extends Serializable> getEntityById(Class<? extends Entity<? extends Serializable>> entityType, Serializable id, boolean lock) throws HibernateException {
 		checkNotNull(entityType);
 		checkNotNull(id);
 
 		final Entity<? extends Serializable> entity = (Entity<? extends Serializable>)getCurrentSession().get(entityType, id, lock ? LockOptions.UPGRADE : LockOptions.NONE);
 
 		LOGGER.info("Got entity by id = '{}'", id);
 
 		return entity;
 	}
 
 	public Entity<? extends Serializable> getEntityById(Class<? extends Entity<? extends Serializable>> entityType, Serializable id) throws HibernateException {
 		return getEntityById(entityType, id, false);
 	}
 
 	public void updateEntity(Entity<? extends Serializable> entity) throws HibernateException {
 		checkNotNull(entity);
 
 		getCurrentSession().merge(entity);
 
 		LOGGER.info("Update entity with id = '{}'", entity.getId());
 	}
 
 	public void deleteEntityById(Class<? extends Entity<? extends Serializable>> entityType, Serializable id) throws HibernateException {
 		checkNotNull(id);
 		checkNotNull(entityType);
 
 		getCurrentSession().delete(getEntityById(entityType, id));
 
 		LOGGER.info("Deleted entity with id = '{}'", id);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<? extends Entity<? extends Serializable>> list(Class<? extends Entity<? extends Serializable>> entityType) {
 		checkNotNull(entityType);
 
 		final List<? extends Entity<? extends Serializable>> entities = getCurrentSession().createCriteria(entityType).list();
 
 		LOGGER.info("List entities for type = '{}'", entityType.getName());
 
 		return entities;
 	}
 
	/**
	 * Obtain the {@link org.hibernate.LockMode#FORCE} lock level upon the given
	 * entity, implicitly checking whether the corresponding database entry
	 * still exists.
	 *
	 * @param entity entity which will be locked
	 * @throws org.springframework.dao.DataAccessException
	 *
	 * @see org.hibernate.LockMode#UPGRADE
	 */
 	public void lockEntity(final Entity<? extends Serializable> entity) throws DataAccessException {
 		checkNotNull(entity);
 
 		getCurrentSession().buildLockRequest(LockOptions.UPGRADE).lock(entity);
 	}
 
 
 	public Session getCurrentSession() {
 		LOGGER.debug("Getting current session");
 
 		return getSessionFactory().getCurrentSession();
 	}
 
 
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 
 	@Autowired
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 }
