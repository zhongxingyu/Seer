 /*
  * $Id$
  * Created on Mar 27, 2011 by beardj 
  */
 package org.firebyte.generic.dao.jpa;
 
 import org.firebyte.generic.dao.GenericDAO;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Projections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * JPA Hibernate implementation of the GenericDAO
  *
  * @author <a href="mailto:jeff@firebyte.org">Jeff Beard</a>
  *
  */
 public class GenericDAOJPA<T, ID extends Serializable> implements GenericDAO<T, ID>  {
 
     /** logback Logger for class GenericDAOJPA */
     private static Logger logger = LoggerFactory.getLogger(GenericDAOJPA.class.getName());
 
 	private final Class<T> persistentClass;
 	
 	@PersistenceContext
 	private EntityManager entityManager;
 
 	@SuppressWarnings("unchecked")
 	public GenericDAOJPA() {
 		this.persistentClass = (Class<T>) ((ParameterizedType) 
 			getClass().getGenericSuperclass()).getActualTypeArguments()[0];
 	}
 
 	public GenericDAOJPA(final Class<T> persistentClass) {
 		super();
 		this.persistentClass = persistentClass;
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#countAll()
 	 */
 	@Override
 	public int countAll() {
 		return countByCriteria();
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#countByExample(java.lang.Object)
 	 */
 	@Override
 	public int countByExample(final T exampleInstance) {
 		
 		Session session = (Session) getEntityManager().getDelegate();
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 		crit.add(Example.create(exampleInstance));
 
 		return (Integer) crit.list().get(0);
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#findAll()
 	 */
 	@Override
 	public List<T> findAll() {
 		return findByCriteria();
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#findByExample(java.lang.Object)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> findByExample(final T exampleInstance) {
 		Session session = (Session) getEntityManager().getDelegate();
 		Criteria crit = session.createCriteria(getEntityClass());
 		final List<T> result = crit.list();
 		return result; 
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#findById(java.io.Serializable)
 	 */
 	@Override
 	public T get(final ID id) {
 		final T result = getEntityManager().find(persistentClass, id);
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.ç#findByNamedQuery(java.lang.String, java.lang.Object[])
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> findByNamedQuery(final String name, Object... params) {
 		
 		Query query = getEntityManager().createNamedQuery(name);
 
 		for (int i = 0; i < params.length; i++) {
 			query.setParameter(i + 1, params[i]);
 		}
 
 		final List<T> result = (List<T>) query.getResultList();
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#findByNamedQueryAndNamedParams(java.lang.String, java.util.Map)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> findByNamedQueryAndNamedParams(final String name, final Map<String, ? extends Object> params) {
 		
 		Query query = getEntityManager().createNamedQuery(name);
 
 		for (final Map.Entry<String, ? extends Object> param : params.entrySet()) {
 			query.setParameter(param.getKey(), param.getValue());
 		}
 
 		final List<T> result = (List<T>) query.getResultList();
 		
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#save(java.lang.Object)
 	 */
 	@Override
 	@Transactional
 	public T save(T entity) {
 
         T savedEntity;
 
         // Approximate the Hibernate saveOrUpdate functionality
         if (getEntityManager().contains(entity) ) {
             savedEntity = getEntityManager().merge(entity);
         } else {
             getEntityManager().persist(entity);
             savedEntity = entity;
         }
 		return savedEntity;
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#delete(java.lang.Object)
 	 */
 	@Override
 	public void remove(T entity) {
 		getEntityManager().remove(entity);
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.repository.GenericDAO#remove(java.io.Serializable)
 	 */
 	@Override
 	public void remove(ID id) {
 		getEntityManager().remove(this.get(id));
 		
 	}
 
 	/*
 	 * (non-Javadoc)
	 * @see org.firebyte.dao.GenericDAO#getEntityClass()
 	 */
 	@Override
 	public Class<T> getEntityClass() {
 		return persistentClass;
 	}
 
 	/**
 	 * Set the JPA entity manager to use.
 	 *
 	 * @param entityManager
 	 */
 	@PersistenceContext
 	public void setEntityManager(final EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 	public EntityManager getEntityManager() {
 		return entityManager;
 	}
 
 	/**
 	 * Use this inside subclasses as a convenience method.
 	 */
 	protected List<T> findByCriteria(final Criterion... criterion) {
 		return findByCriteria(-1, -1, criterion);
 	}
 
 	/**
 	 * Use this inside subclasses as a convenience method.
 	 */
 	@SuppressWarnings("unchecked")
 	protected List<T> findByCriteria(final int firstResult, final int maxResults, final Criterion... criterion) {
 		
 		Session session = (Session) getEntityManager().getDelegate();
 		Criteria crit = session.createCriteria(getEntityClass());
 
 		for (final Criterion c : criterion) {
 			crit.add(c);
 		}
 
 		if (firstResult > 0) {
 			crit.setFirstResult(firstResult);
 		}
 
 		if (maxResults > 0) {
 			crit.setMaxResults(maxResults);
 		}
 
 		final List<T> result = crit.list();
 		return result;
 	}
 
 	/**
 	 * Get counts by Criteria
 	 * 
 	 * @param criterion
 	 * @return int
 	 */
 	protected int countByCriteria(Criterion... criterion) {
 		
 		Session session = (Session) getEntityManager().getDelegate();
 		
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 
 		for (final Criterion c : criterion) {
 			crit.add(c);
 		}
 
 		return (Integer) crit.list().get(0);
 	}
 }
