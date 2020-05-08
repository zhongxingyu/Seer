 /*
  * Copyright 2012 Eng Kam Hon (kamhon@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.kamhon.ieagle.dao;
 
 import java.beans.PropertyDescriptor;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Transient;
 
 import net.kamhon.ieagle.datagrid.DatagridModel;
 import net.kamhon.ieagle.datagrid.Filter;
 import net.kamhon.ieagle.datagrid.Sorter;
 import net.kamhon.ieagle.exception.DataException;
 import net.kamhon.ieagle.util.CollectionUtil;
 import net.kamhon.ieagle.util.ReflectionUtil;
 import net.kamhon.ieagle.vo.VoBase;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.NonUniqueResultException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.util.Assert;
 
 public class HibernateDao<T> extends HibernateDaoSupport implements GenericDao<T> {
     private static final Log log = LogFactory.getLog(HibernateDao.class);
 
     public void delete(Object object) {
         try {
             getHibernateTemplate().delete(object);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public void deleteAll(Set<T> set) {
         try {
             getHibernateTemplate().deleteAll(set);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public void save(Object object) {
 
         try {
             getHibernateTemplate().save(object);
 
         } catch (Exception e) {
             e.printStackTrace();
             throw new DataException(e);
         }
     }
 
     public void saveAll(Set<T> set) {
         try {
             for (Object object : set)
                 getHibernateTemplate().save(object);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public void update(Object object) {
         try {
             getHibernateTemplate().update(object);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public void updateInsert(Object object) {
         try {
             getHibernateTemplate().saveOrUpdate(object);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public T get(Class<T> clazz, Serializable serializablekey) {
         try {
             return (T) getHibernateTemplate().get(clazz, serializablekey);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public T getReadonly(Class<T> clazz, Serializable serializablekey) {
         try {
             return (T) getHibernateTemplate().get(clazz, serializablekey, LockMode.READ);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public T getForUpdate(Class<T> clazz, Serializable serializablekey) {
         try {
             return getHibernateTemplate().get(clazz, serializablekey, LockMode.PESSIMISTIC_WRITE);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public List<T> loadAll(Class<T> clazz) {
         try {
             return getHibernateTemplate().loadAll(clazz);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     public List<T> findQueryAsList(String query, Object... objectArray) {
         try {
             if (objectArray == null || objectArray.length == 0)
                 return getHibernateTemplate().find(query);
             else
                 return getHibernateTemplate().find(query, objectArray);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     public List<T> findQueryNameAsList(String queryName, Object... objectArray) {
         try {
             if (objectArray == null || objectArray.length == 0)
                 return getHibernateTemplate().findByNamedQuery(queryName);
             else
                 return getHibernateTemplate().findByNamedQuery(queryName, objectArray);
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     public T findFirst(String query, Object... objectArray) throws DataException {
         List<T> list = findBlock(query, 0, 1, objectArray);
         if (list.size() > 0)
             return list.get(0);
         else
             return null;
     }
 
     @Override
     public T findFirst(T example, String... orderBy) {
         QueryParams queryParams = translateExampleToQueryParams(example, orderBy);
         return findFirst(queryParams.query, queryParams.params.toArray());
     }
 
     @Override
     public T findFirstByExample(T example, List<String> orderBy) {
         if (CollectionUtil.isNotEmpty(orderBy))
             return findFirst(example, orderBy.toArray(new String[0]));
         else
             return findFirst(example);
     }
 
     public T findUnique(final String queryString, final Object... objectArray) {
         try {
             return getHibernateTemplate().execute(new HibernateCallback<T>() {
                 @SuppressWarnings("unchecked")
                 public T doInHibernate(Session session) throws HibernateException {
                     Query query = session.createQuery(queryString);
                     if ((objectArray != null) && objectArray.length > 0) {
                         int paramCount = objectArray.length;
                         for (int i = 0; i < paramCount; i++) {
                             query.setParameter(i, objectArray[i]);
                         }
                     }
                     return (T) query.uniqueResult();
                 }
             });
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     @Override
     public T findUnique(T t) {
         List<T> results = findByExample(t);
 
         if (CollectionUtil.isEmpty(results))
             return null;
         else if (results.size() > 1)
             throw new NonUniqueResultException(results.size());
         else
             return results.get(0);
     }
 
     @SuppressWarnings("unchecked")
     public List<T> findBlock(final String queryString, final int offset, final int recordCount,
             final Object... objectArray) {
         try {
             return getHibernateTemplate().executeFind(new HibernateCallback<Object>() {
                 public Object doInHibernate(Session session) throws HibernateException {
                     Query query = session.createQuery(queryString);
                     int paramCount = 0;
                     if (objectArray != null && (paramCount = objectArray.length) > 0) {
                         for (int i = 0; i < paramCount; i++) {
                             query.setParameter(i, objectArray[i]);
                         }
                     }
                     query.setMaxResults(recordCount);
                     query.setFirstResult(offset);
                     return query.list();
                 }
             });
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     private QueryParams translateExampleToQueryParams(Object example, String... orderBy) {
         Object newObj = example;
         Entity className = ((Entity) newObj.getClass().getAnnotation(Entity.class));
         if (className == null)
             throw new DataException(newObj.getClass().getSimpleName() + " class is not valid JPA annotated bean");
         String aliasName = StringUtils.isBlank(className.name()) ? "obj" : className.name();
 
         String hql = "SELECT " + aliasName + " FROM ";
         List<Object> params = new ArrayList<Object>();
 
         BeanWrapper beanO1 = new BeanWrapperImpl(newObj);
         PropertyDescriptor[] proDescriptorsO1 = beanO1.getPropertyDescriptors();
         int propertyLength = proDescriptorsO1.length;
 
         if (newObj != null) {
             hql += aliasName;
             hql += " IN " + example.getClass();
         }
 
         if (example instanceof VoBase) {
             VoBase voBase = (VoBase) example;
             for (String key : voBase.getJoinTables().keySet()) {
                 if (StringUtils.isNotBlank(key)) {
                     hql += " JOIN " + key + " " + voBase.getJoinTables().get(key);
                 }
             }
         }
 
         hql += " WHERE 1=1";
 
         for (int i = 0; i < propertyLength; i++) {
             try {
                 Object propertyValueO1 = beanO1.getPropertyValue(proDescriptorsO1[i].getName());
 
                 if ((propertyValueO1 instanceof String && StringUtils.isNotBlank((String) propertyValueO1))
                         || propertyValueO1 instanceof Long || propertyValueO1 instanceof Double
                         || propertyValueO1 instanceof Integer || propertyValueO1 instanceof Boolean
                         || propertyValueO1 instanceof Date || propertyValueO1.getClass().isPrimitive()) {
                     Field field = null;
                     try {
                         field = example.getClass().getDeclaredField(proDescriptorsO1[i].getName());
                     } catch (NoSuchFieldException e) {
                         if (propertyValueO1 instanceof Boolean || propertyValueO1.getClass().isPrimitive()) {
                             String fieldName = "is"
                                     + StringUtils.upperCase(proDescriptorsO1[i].getName().substring(0, 1))
                                     + proDescriptorsO1[i].getName().substring(1);
                             field = example.getClass().getDeclaredField(fieldName);
                         }
                     }
 
                     if (proDescriptorsO1[i].getName() != null && field != null
                             && !field.isAnnotationPresent(Transient.class)) {
                         if (!Arrays.asList(VoBase.propertiesVer).contains(proDescriptorsO1[i].getName())) {
                             hql += " AND " + aliasName + "." + field.getName() + " = ?";
                             params.add(propertyValueO1);
                         }
                     }
                 } else if (propertyValueO1 != null) {
                     Field field = example.getClass().getDeclaredField(proDescriptorsO1[i].getName());
                     if (field.isAnnotationPresent(javax.persistence.Id.class)
                             || field.isAnnotationPresent(javax.persistence.EmbeddedId.class)) {
 
                         BeanWrapper bean = new BeanWrapperImpl(propertyValueO1);
                         PropertyDescriptor[] proDescriptors = bean.getPropertyDescriptors();
 
                         for (PropertyDescriptor propertyDescriptor : proDescriptors) {
                             Object propertyValueId = bean.getPropertyValue(propertyDescriptor.getName());
 
                             if (propertyValueId != null && ReflectionUtil.isJavaDataType(propertyValueId)) {
                                 hql += " AND " + aliasName + "." + proDescriptorsO1[i].getName() + "."
                                         + propertyDescriptor.getName() + " = ?";
                                 params.add(bean.getPropertyValue(propertyDescriptor.getName()));
                             }
                         }
                     }
                 }
             } catch (Exception e) {
             }
         }
 
         // not condition
         if (example instanceof VoBase) {
             VoBase voBase = (VoBase) example;
             for (String key : voBase.getNotConditions().keySet()) {
                 if (StringUtils.isNotBlank(key)) {
                     hql += " AND " + aliasName + "." + key + "!=? ";
                     params.add(voBase.getNotConditions().get(key));
                 }
             }
         }
 
         // like condition
         if (example instanceof VoBase) {
             VoBase voBase = (VoBase) example;
             for (String key : voBase.getLikeConditions().keySet()) {
                 if (StringUtils.isNotBlank(key)) {
                     hql += " AND " + aliasName + "." + key + " LIKE ? ";
                     params.add(voBase.getLikeConditions().get(key));
                 }
             }
         }
 
         if (orderBy != null && orderBy.length != 0) {
             hql += " ORDER BY ";
             long count = 1;
             for (String orderByStr : orderBy) {
                 if (count != 1)
                     hql += ",";
                 hql += aliasName + "." + orderByStr;
                 count += 1;
             }
         }
 
         log.debug("hql = " + hql);
 
         return new QueryParams(hql, params);
     }
 
     /**
      * consider
      * 
      * @id/primary not consider properties in voBase
      */
     public List<T> findByExample(T example, String... orderBy) {
         try {
             QueryParams queryParams = translateExampleToQueryParams(example, orderBy);
             return findQueryAsList(queryParams.query, queryParams.params.toArray());
         } catch (Exception e) {
             throw new DataException(e);
         }
     }
 
     @Override
     public List<T> findByExample(T example, List<String> orderBy) {
         if (CollectionUtil.isNotEmpty(orderBy))
             return findByExample(example, orderBy.toArray(new String[0]));
         else
             return findByExample(example);
     }
 
     public void findForDatagrid(DatagridModel<T> datagridModel, String alias, String queryString, Object... objectArray) {
         Assert.isTrue(StringUtils.isNotBlank(alias), "The alias can not be BLANK!!");
         Assert.isTrue(StringUtils.isNotBlank(queryString), "The queryString can not be BLANK!!");
 
         if (CollectionUtil.isNotEmpty(datagridModel.getFilters())) {
             Assert.doesNotContain(queryString, BasicDao.FILTER_PARAMS, "The " + BasicDao.FILTER_PARAMS
                     + " is not found in Query [" + queryString + "] if FILTERS is not EMPTY!!");
         }
         /*************************
          * END VALIDATION
          ************************/
         String finalQuery = queryString.trim();
         List<Object> params = new ArrayList<Object>();
         if (objectArray != null) {
             if (objectArray.length == 1 && objectArray[0] instanceof List)
                params = (List) objectArray[0];
             else
                Arrays.asList(objectArray);
         }
 
         if (CollectionUtil.isNotEmpty(datagridModel.getFilters())) {
             for (Filter filter : datagridModel.getFilters()) {
                 if (filter != null)
                     throw new DataException("The Filter features still not implemented yet");
             }
         }
 
         List<Object> countParams = new ArrayList<Object>(params);
         String countQuery = "SELECT COUNT(" + alias + ") ";
 
         if (finalQuery.toUpperCase().startsWith("SELECT")) {
             int ind = finalQuery.toUpperCase().indexOf("FROM");
             countQuery += finalQuery.substring(ind);
         } else {
             countQuery += finalQuery;
         }
 
         if (!datagridModel.isDisableSort() && CollectionUtil.isNotEmpty(datagridModel.getSorters())) {
             if (StringUtils.contains(finalQuery.toUpperCase(), "ORDER BY")) {
                 finalQuery += ", ";
             } else {
                 finalQuery += " ORDER BY ";
             }
 
             for (Iterator<Sorter> iter = datagridModel.getSorters().iterator(); iter.hasNext();) {
                 Sorter sorter = iter.next();
                 finalQuery += alias + "." + sorter.getColumn() + " " + sorter.getDirection();
                 if (iter.hasNext()) {
                     finalQuery += ", ";
                 }
             }
         }
 
         // log.debug("countParams = " + countParams);
         // log.debug("countQuery = " + countQuery);
 
         // log.debug("params = " + params);
         // log.debug("finalQuery = " + finalQuery);
 
         List<T> result = (List<T>) findBlock(finalQuery, datagridModel.getRecordOffset(), datagridModel.getPageSize(),
                 params.toArray());
         datagridModel.setRecords(result);
 
         Long count = (Long) this.findUnique(countQuery, countParams.toArray());
         datagridModel.setTotalRecords(count);
     }
 
     public void refresh(Object example) {
         getHibernateTemplate().refresh(example);
     }
 
     public void flush() {
         getHibernateTemplate().flush();
     }
 
     public int bulkUpdate(String query, Object... objectArray) {
         if (objectArray != null) {
             return getHibernateTemplate().bulkUpdate(query, objectArray);
         } else {
             return getHibernateTemplate().bulkUpdate(query);
         }
     }
 
     private class QueryParams {
         private String query;
         private List<Object> params;
 
         public QueryParams(String query, List<Object> params) {
             this.query = query;
             this.params = params;
         }
     }
 }
