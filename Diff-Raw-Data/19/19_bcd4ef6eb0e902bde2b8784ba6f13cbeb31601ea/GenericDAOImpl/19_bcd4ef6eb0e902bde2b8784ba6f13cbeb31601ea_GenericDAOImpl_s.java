 package com.tda.persistence.dao;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import com.tda.persistence.paginator.Order;
 import com.tda.persistence.paginator.Paginator;
 
 public abstract class GenericDAOImpl<T> extends HibernateDaoSupport implements
 		GenericDAO<T> {
 	protected Class<T> persistentClass = getDomainClass();
 
 	protected abstract Class<T> getDomainClass();
 
 	public void save(final T entity) {
 		getHibernateTemplate().saveOrUpdate(entity);
 	}
 
 	public void delete(final T entity) {
 		getHibernateTemplate().delete(entity);
 	}
 
 	public void update(final T entity) {
 		getHibernateTemplate().update(entity);
 	}
 
 	public T findById(final Long id) {
 		return (T) getHibernateTemplate().get(this.persistentClass, id);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<T> findAll() {
 		return getHibernateTemplate().find(
 				"from " + this.persistentClass.getName() + " o");
 	}
 
 	public void deleteById(final Long id) {
 		T obj = this.findById(id);
 		getHibernateTemplate().delete(obj);
 	}
 
 	@SuppressWarnings("unchecked")
 	public int count() {
 		List<Long> list = getHibernateTemplate().find(
 				"select count(*) from " + persistentClass.getName() + " o");
 		Long count = list.get(0);
 		return count.intValue();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<T> findByExample(final T exampleObject) {
 		return getHibernateTemplate().findByExample(exampleObject);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<T> findAllPaged(final Paginator paginator) {
 
 		paginator.setTotalResultsCount(count());
 
 		final Class<T> localClass = this.persistentClass;
 
 		HibernateCallback<List<T>> callback = new HibernateCallback<List<T>>() {
 			public List<T> doInHibernate(org.hibernate.Session session)
 					throws HibernateException, SQLException {
 
 				String queryString = "from " + localClass.getName();
 
 				if (paginator.getOrder() != null
 						&& paginator.getOrderField() != null) {
 					queryString += " order by "
 							+ paginator.getOrderField()
 							+ " "
 							+ (paginator.getOrder() == Order.asc ? "ASC"
 									: "DESC");
 				}
 				return session
 						.createQuery(queryString)
 						.setFirstResult(
 								paginator.getResultsPerPage()
										* paginator.getPageIndex())
 						.setMaxResults(paginator.getResultsPerPage()).list();
 			}
 		};
 
 		return getHibernateTemplate().execute(callback);
 	}
 }
