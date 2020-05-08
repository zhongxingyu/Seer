 package org.motechproject.carereporting.dao.impl;
 
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.motechproject.carereporting.dao.GenericDao;
 import org.motechproject.carereporting.domain.AbstractEntity;
 import org.motechproject.carereporting.exception.CareNullArgumentRuntimeException;
 import org.motechproject.carereporting.exception.CareResourceNotFoundRuntimeException;
 import org.motechproject.carereporting.exception.CareRuntimeException;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 @SuppressWarnings("unchecked")
 public abstract class GenericDaoHibernateImpl<T extends AbstractEntity> implements GenericDao<T> {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     private final Class<T> type;
 
     protected SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     protected Session getCurrentSession() {
         return sessionFactory.getCurrentSession();
     }
 
     public GenericDaoHibernateImpl() {
         type = (Class<T>) ((ParameterizedType)
                 getClass().getGenericSuperclass())
                 .getActualTypeArguments()[0];
     }
 
     @Override
     public Set<T> getAll() {
         return new HashSet<T>(sessionFactory.getCurrentSession()
                 .createCriteria(type).list());
     }
 
     @Override
     public Set<T> getAllWithFields(String... fieldNames) {
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(type);
         if (fieldNames.length > 0) {
             for (String fieldName : fieldNames) {
                 criteria = criteria.setFetchMode(fieldName, FetchMode.JOIN);
             }
         }
 
         return new HashSet<T>(criteria.list());
     }
 
     @Override
     public T getById(Integer id) {
         if (id == null) {
             throw new CareNullArgumentRuntimeException();
         }
 
         T entity = (T) sessionFactory.getCurrentSession().get(type, id);
 
         if (entity == null) {
             throw new CareResourceNotFoundRuntimeException(type, id);
         }
 
         return entity;
     }
 
     @Override
     public T getByIdWithFields(Integer id, String... fieldNames) {
         if (id == null) {
             throw new CareNullArgumentRuntimeException();
         }
 
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(type);
         if (fieldNames.length > 0) {
             for (String fieldName : fieldNames) {
                 criteria = criteria.setFetchMode(fieldName, FetchMode.JOIN);
             }
         }
         criteria.add(Restrictions.eq("id", id));
 
         T entity = (T) criteria.list().get(0);
 
         if (entity == null) {
             throw new CareResourceNotFoundRuntimeException(type, id);
         }
 
         return entity;
     }
 
     @Override
     public void save(T entity) {
         sessionFactory.getCurrentSession().save(entity);
     }
 
     @Override
     public void update(T entity) {
         Session session = sessionFactory.getCurrentSession();
         session.update(entity);
         session.flush();
     }
 
     @Override
     public void remove(T entity) {
         sessionFactory.getCurrentSession().delete(entity);
     }
 
     @Override
     public void removeAll() {
         sessionFactory.getCurrentSession()
                 .createQuery("delete from " + type)
                 .executeUpdate();
     }
 
     @Override
     public Object executeNamedQuery(final String queryName, final Map<String, ?> queryParams) {
         try {
             Query query = getCurrentSession().getNamedQuery(queryName);
             query.setProperties(queryParams);
             return query.list();
         } catch (Exception e) {
             throw new CareRuntimeException(e);
         }
     }
 
     @Override
     public Object executeNamedQueryWithUniqueResult(final String queryName, final Map<String, ?> queryParams) {
         try {
             Query query = getCurrentSession().getNamedQuery(queryName);
             query.setProperties(queryParams);
             return query.uniqueResult();
         } catch (Exception e) {
             throw new CareRuntimeException(e);
         }
     }
 }
