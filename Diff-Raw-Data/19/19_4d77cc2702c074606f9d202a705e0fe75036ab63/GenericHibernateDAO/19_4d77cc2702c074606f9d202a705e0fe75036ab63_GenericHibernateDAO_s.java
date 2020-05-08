 package org.safaproject.safa.dao;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Projections;
 import org.springframework.beans.factory.annotation.Required;
 
 /**
  * JPA implementation of the GenericDAO. Note that this implementation also
  * expects Hibernate as JPA implementation. That's because we like the Criteria
  * API :P.
  * 
  * @param <T>
  *            The persistent type
  * @param <ID>
  *            The primary key type
  */
 public class GenericHibernateDAO<T, ID extends Serializable> implements
 		GenericDAO<T, ID> {
 
 	private final Class<T> persistentClass;
 
 	protected EntityManager entityManager;
 
 	@SuppressWarnings("unchecked")
 	public GenericHibernateDAO() {
 		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
 				.getGenericSuperclass()).getActualTypeArguments()[0];
 	}
 
 	public GenericHibernateDAO(final Class<T> persistentClass) {
 		super();
 		this.persistentClass = persistentClass;
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#countAll
 	 */
 	public Long countAll() {
 		return countByCriteria();
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#countByExample(java.lang.Object)
 	 */
 	public Long countByExample(final T exampleInstance) {
 		Session session = (Session) getEntityManager().getDelegate();
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 		crit.add(Example.create(exampleInstance));
 
 		return (Long) crit.list().get(0);
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#findAll()
 	 */
 	public List<T> findAll() {
 		return findByCriteria();
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#findByExample(java.lang.Object)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<T> findByExample(final T exampleInstance) {
 		Session session = (Session) getEntityManager().getDelegate();
 		Example example = Example.create(exampleInstance).excludeZeroes()
 				.ignoreCase();
 		Criteria criteria = session.createCriteria(getEntityClass()).add(
 				example);
 		final List<T> result = criteria.list();
 		return result;
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#findById(java.io.Serializable)
 	 */
 	public T findById(final ID id) {
 		final T result = getEntityManager().find(persistentClass, id);
 		return result;
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#findByNamedQuery(java.lang.String,
 	 *      java.lang.Object[])
 	 */
 	@SuppressWarnings("unchecked")
 	public List<T> findByNamedQuery(final String name, Object... params) {
 		Query query = getEntityManager().createNamedQuery(name);
 
 		for (int i = 0; i < params.length; i++) {
 			query.setParameter(i + 1, params[i]);
 		}
 
 		final List<T> result = (List<T>) query.getResultList();
 		return result;
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#findByNamedQueryAndNamedParams(java.lang.String,
 	 *      java.util.Map)
 	 */
 	@SuppressWarnings("unchecked")
 	public List<T> findByNamedQueryAndNamedParams(final String name,
 			final Map<String, ? extends Object> params) {
 		Query query = getEntityManager().createNamedQuery(name);
 
 		for (final Map.Entry<String, ? extends Object> param : params
 				.entrySet()) {
 			query.setParameter(param.getKey(), param.getValue());
 		}
 
 		final List<T> result = (List<T>) query.getResultList();
 		return result;
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#getEntityClass()
 	 */
 	public Class<T> getEntityClass() {
 		return persistentClass;
 	}
 
 	/**
 	 * set the JPA entity manager to use.
 	 * 
 	 * @param entityManager
 	 */
 	@Required
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
 	protected List<T> findByCriteria(final int firstResult,
 			final int maxResults, final Criterion... criterion) {
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
 
 	protected Long countByCriteria(Criterion... criterion) {
 		Session session = (Session) getEntityManager().getDelegate();
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 
 		for (final Criterion c : criterion) {
 			crit.add(c);
 		}
 
 		return (Long) crit.list().get(0);
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#delete(java.lang.Object)
 	 */
 	public void delete(T entity) {
 		getEntityManager().remove(entity);
 
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#save(java.lang.Object)
 	 */
 	public T update(T entity) {
 		final T savedEntity = getEntityManager().merge(entity);
 		return savedEntity;
 	}
 
 	/**
 	 * @see org.safaproject.safa.dao.GenericDAO#persist(java.lang.Object)
 	 */
 	public void save(T entity) {
 		getEntityManager().persist(entity);
 	}
 }
