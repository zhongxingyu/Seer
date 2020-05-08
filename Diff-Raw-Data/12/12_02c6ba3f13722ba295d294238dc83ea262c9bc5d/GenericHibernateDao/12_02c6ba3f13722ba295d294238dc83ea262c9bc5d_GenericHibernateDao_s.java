 package com.satish.hibernate.dao;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Criterion;
 
 public abstract class GenericHibernateDao<T, ID extends Serializable> 
 implements HibernateDao<T, ID> {
 	
 	private Class<T> m_persistentClass;
 	private SessionFactory m_sessionFactory;
 	
 	
 	@SuppressWarnings("unchecked")
 	public GenericHibernateDao(SessionFactory sessionFactory) {
 		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
 		m_persistentClass = (Class<T>) type.getActualTypeArguments()[0];
 		m_sessionFactory = sessionFactory;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public T findById(ID id, boolean lock) {
		return (T) getSession().load(getPersistentClass(), id);
 	}
 	
 	public T makePersistent(T entity) {
 		getSession().saveOrUpdate(entity);
 		return entity;
 	}
 	
 	public void makeTransient(T entity) {
 		getSession().delete(entity);
 	}
 	
 	@Override
 	public List<T> findAll() {		
 		return findByCriteria();
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected List<T> findByCriteria(Criterion ...criterias) {
 		Criteria crit = getSession().createCriteria(getPersistentClass());
 		
 		for(Criterion criteria : criterias) {
 			crit.add(criteria);
 		}
 		
 		return crit.list();
 	}
 	
 	@Override
 	public void clear() {
 		getSession().clear();
 		
 	}
 	
 	@Override
 	public void flush() {
 		getSession().flush();
 		
 	}
 	
 	public Class<T> getPersistentClass() {
 		return m_persistentClass;
 	}
 	
 	
 	public Session getSession() {
		return m_sessionFactory.openSession();
 	}
 
 }
