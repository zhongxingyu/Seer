 package au.org.paperminer.dao;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.EntityTransaction;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Projections;
 
 /**
  * This class is a generic DAO class to access database tables. This call will
  * be extended by each tables that require common functionality
  * 
  * This class will implement the IGenericDao Interface
  */
 @SuppressWarnings("unused")
 public class GenericDAO<T, ID extends Serializable> implements
 		IGenericDAO<T, ID> {
 
 	private final Class<T> persistentClass;
 	private EntityManager entityManager;
 	private Session session;
 
 	private static final String FINDING_UNIQUE_BY_CRITERIA = "Finding unique by criteria";
 	private static final String FINDING_BY_CRITERIA = "Finding by criteria";
 	private static final String CAUSE = ". Cause: ";
 	private static final String PERSISTING_OBJECT = "Persisting object: ";
 	private static final String REMOVING_OBJECT = "Removing object: ";
 	private static final String MERGING_OBJECT = "Merging object: ";
 	private static final String REFRESHING_OBJECT = "Refreshing object: ";
 	private static final String FAILED_TO_MERGE = "Failed to merge: ";
 	private static final String FAILED_TO_REMOVE = "Failed to remove: ";
 	private static final String FAILED_TO_PERSIST = "Failed to persist: ";
 	private static final String FAILED_TO_REFRESH = "Failed to refresh: ";
 	private static final String OBJECT_BY_ID = " object by id ";
 	private static final String FINDING = "Finding ";
 	private static final String EXECUTING_QUERY = "Executing query: ";
 	private static final String FAILED_TO_EXECUTE_QUERY = "Failed to execute query: ";
 	private static final String ERROR = "Error: ";
 	private static final String RESULT = "Result: ";
 	private static final String EMPTY_LIST = "Empty list";
 
 	@SuppressWarnings("unchecked")
 	public GenericDAO() {
 		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
 				.getGenericSuperclass()).getActualTypeArguments()[0];
 	}
 
 	public GenericDAO(final Class<T> persistentClass) {
 		super();
 		this.persistentClass = persistentClass;
 	}
 
 	@Override
 	public int countAll() {
 		return countByCriteria();
 	}
 
 	@Override
 	public int countByExample(final T exampleInstance) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 		crit.add(Example.create(exampleInstance));
 
 		return (Integer) crit.list().get(0);
 	}
 
 	@Override
 	public List<T> findAll() {
 		return findByCriteria();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> findByExample(final T exampleInstance) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.add(Example.create(exampleInstance));
 		final List<T> result = crit.list();
 		return result;
 	}
 
 	@Override
 	public T findById(final ID id) {
 		final T result = getEntityManager().find(persistentClass, id);
 		return result;
 	}
 
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
 
 	@SuppressWarnings("unchecked")
 	@Override
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
 
 	@Override
 	public Class<T> getEntityClass() {
 		return persistentClass;
 	}
 
 	/**
 	 * set the JPA entity manager to use.
 	 * 
 	 * @param entityManager
 	 */
 	//@PersistenceContext
 	public void setEntityManager(final EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 	public EntityManager getEntityManager() {
 		return entityManager;
 	}
 
 	public Session getSession() {
 		return session;
 	}
 
 	public void setSession(Session session) {
 		this.session = session;
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
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
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
 
 	protected int countByCriteria(Criterion... criterion) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
 		Criteria crit = session.createCriteria(getEntityClass());
 		crit.setProjection(Projections.rowCount());
 
 		for (final Criterion c : criterion) {
 			crit.add(c);
 		}
 
 		return (Integer) crit.list().get(0);
 	}
 
 	@Override
 	public void delete(T entity) {
 		getEntityManager().remove(entity);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public T save(T entity) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
		Transaction tx = session.beginTransaction();
 		final T savedEntity = (T) session.merge(entity);
		tx.commit();
		session.flush();
 		return savedEntity;
 	}
 
 	public void beginTransaction() {
 		if (entityManager != null) {
 			EntityManager em = getEntityManager();
 			EntityTransaction transaction = em.getTransaction();
 
 		} else {
 			Transaction transaction = getSession().getTransaction();
 			if (!transaction.isActive()) {
 				transaction.begin();
 			}
 		}
 	}
 
 	public void commit() {
 		if (entityManager != null) {
 			EntityManager em = getEntityManager();
 			EntityTransaction transaction = em.getTransaction();
 			if (transaction.isActive()) {
 				transaction.commit();
 			}
 		} else {
 			Transaction transaction = getSession().getTransaction();
 			if (!transaction.isActive()) {
 				transaction.commit();
 			}
 		}
 	}
 
 	public void rollback() {
 		if (entityManager != null) {
 			EntityManager em = getEntityManager();
 			EntityTransaction transaction = em.getTransaction();
 			if (transaction.isActive()) {
 				transaction.rollback();
 			}
 		} else {
 			Transaction transaction = getSession().getTransaction();
 			if (!transaction.isActive()) {
 				transaction.rollback();
 			}
 		}
 	}
 
 	public int executeHQLUpdate(String hql) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
 		try {
 			org.hibernate.Query q = session.createQuery(hql);
 			int result = q.executeUpdate();
 			return result;
 		} catch (Exception e) {
 			throw new DAOException(FAILED_TO_EXECUTE_QUERY + e.getMessage()
 					+ CAUSE + e.getCause());
 		}
 	}
 
 	public int executeSQLUpdate(String sql) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
 		try {
 			org.hibernate.Query q = session.createSQLQuery(sql);
 			int result = q.executeUpdate();
 			return result;
 		} catch (Exception e) {
 			throw new DAOException(FAILED_TO_EXECUTE_QUERY + e.getMessage()
 					+ CAUSE + e.getCause());
 		}
 	}
 
 	public int executeSQLUpdateWithParam(String sql, List<String> parameterLst) {
 		Session session;
 		if (getSession() == null) {
 			session = (Session) getEntityManager().getDelegate();
 		} else {
 			session = getSession();
 		}
 		try {
 			org.hibernate.Query q = session.createSQLQuery(sql);
 			int position = 0;
 			if (parameterLst.size() > 0) {
 				for (String param : parameterLst) {
 					q.setParameter(position, param);
 					position++;
 				}
 			}
 			int result = q.executeUpdate();
 			return result;
 		} catch (Exception e) {
 			throw new DAOException(FAILED_TO_EXECUTE_QUERY + e.getMessage()
 					+ CAUSE + e.getCause());
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "DAO of " + persistentClass;
 	}
 
 	@Override
 	public void flushAndClear() {
 		if (entityManager != null) {
 			entityManager.flush();
 			entityManager.clear();
 		} else {
 			session.flush();
 			session.clear();
 		}
 	}
 
 	public Object findUniqueByCriteria(
 			Criteria criteria)
 			throws NonUniqueResultException, NoResultException {
 		return criteria.uniqueResult();
 	}
 }
