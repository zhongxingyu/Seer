 package com.hidden.data.db.dao.impl;
 
 import java.util.List;
 
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import com.hidden.data.db.dao.CrudDao;
 
 class CrudDaoHibernate<T> extends HibernateDaoSupport implements CrudDao<T> {
 
 	private Class<T> modelClass;
 
 	protected CrudDaoHibernate(Class<T> modelClass) {
 		this.modelClass = modelClass;
 	}
 
 	@Override
 	public T save(T item) {
 		getHibernateTemplate().save(item);
 		return item;
 	}
 
 	@Override
 	public List<T> loadAll() {
 		return getHibernateTemplate().loadAll(modelClass);
 	}
 
 	@Override
 	public void delete(T item) {
 		getHibernateTemplate().delete(item);
 	}
 
 }
