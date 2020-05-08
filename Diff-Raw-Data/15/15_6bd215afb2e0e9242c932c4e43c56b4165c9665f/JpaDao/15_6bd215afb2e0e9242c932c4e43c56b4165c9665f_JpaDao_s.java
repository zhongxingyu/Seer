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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.LockModeType;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
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
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.orm.jpa.JpaCallback;
 import org.springframework.orm.jpa.support.JpaDaoSupport;
 import org.springframework.util.Assert;
 
 public class JpaDao<T> extends JpaDaoSupport implements GenericDao<T> {
     private static final Log log = LogFactory.getLog(JpaDao.class);
 
     @Override
     public void delete(T object) {
         getJpaTemplate().remove(object);
     }
 
     @Override
     public void deleteAll(Set<T> set) {
         for (T object : set) {
             getJpaTemplate().remove(object);
         }
     }
 
     @Override
     public void save(T object) {
         getJpaTemplate().persist(object);
     }
 
     @Override
     public void saveAll(Set<T> set) {
         for (T object : set) {
             getJpaTemplate().persist(object);
         }
     }
 
     @Override
     public void update(T object) {
         getJpaTemplate().merge(object);
     }
 
     @Override
     public void updateInsert(T object) {
         getJpaTemplate().persist(object);
     }
 
     @Override
     public T get(Class<T> clazz, Serializable serializablekey) {
         return getJpaTemplate().find(clazz, serializablekey);
     }
 
     @Override
     public T getReadonly(final Class<T> clazz, final Serializable serializablekey) {
         return getJpaTemplate().execute(new JpaCallback<T>() {
             @Override
             public T doInJpa(EntityManager em) throws PersistenceException {
                 return em.find(clazz, serializablekey, LockModeType.OPTIMISTIC);
             }
         });
     }
 
     @Override
     public T getForUpdate(final Class<T> clazz, final Serializable serializablekey) {
         return getJpaTemplate().execute(new JpaCallback<T>() {
             @Override
             public T doInJpa(EntityManager em) throws PersistenceException {
                 return em.find(clazz, serializablekey, LockModeType.PESSIMISTIC_WRITE);
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<T> loadAll(Class<T> clazz) {
         return getJpaTemplate().find("select o from " + clazz.getName() + " o ");
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<T> findQueryAsList(String query, Object... objectArray) {
         query = convertJpaPositionParams(query);
         return getJpaTemplate().find(query, objectArray);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<T> findQueryNameAsList(String queryName, Object... objectArray) {
         return getJpaTemplate().findByNamedQuery(queryName, objectArray);
     }
 
     @Override
     public T findFirst(String query, Object... objectArray) {
         query = convertJpaPositionParams(query);
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
 
     @Override
     public T findUnique(String query, final Object... objectArray) {
         final String query2 = convertJpaPositionParams(query);
         return getJpaTemplate().execute(new JpaCallback<T>() {
             @SuppressWarnings("unchecked")
             @Override
             public T doInJpa(EntityManager em) throws PersistenceException {
                 Query q = em.createQuery(query2);
                 for (int i = 0; i < objectArray.length; i++) {
                     q.setParameter(i + 1, objectArray[i]);
                 }
                 return (T) q.getSingleResult();
             }
         });
     }
 
     @Override
     public T findUnique(T example) {
         QueryParams queryParams = translateExampleToQueryParams(example);
         return findFirst(queryParams.query, queryParams.params.toArray());
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<T> findBlock(String query, final int offset, final int recordCount, final Object... objectArray) {
         final String query2 = convertJpaPositionParams(query);
         return getJpaTemplate().executeFind(new JpaCallback<List<T>>() {
             @Override
             public List<T> doInJpa(EntityManager em) throws PersistenceException {
                 Query q = em.createQuery(query2);
                 for (int i = 0; i < objectArray.length; i++) {
                     q.setParameter(i + 1, objectArray[i]);
                 }
                 q.setFirstResult(offset);
                 q.setMaxResults(recordCount);
                 return q.getResultList();
             }
         });
     }
 
     @Override
     public void findForDatagrid(DatagridModel<T> datagridModel, String alias, String queryString, Object... objectArray) {
         queryString = convertJpaPositionParams(queryString);
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
 
     private QueryParams translateExampleToQueryParams(Object example, String... orderBy) {
         Object newObj = example;
         Entity entity = ((Entity) newObj.getClass().getAnnotation(Entity.class));
         if (entity == null)
             throw new DataException(newObj.getClass().getSimpleName() + " class is not valid JPA annotated bean");
 
         String entityName = StringUtils.isBlank(entity.name()) ? example.getClass().getSimpleName() : entity.name();
         String aliasName = StringUtils.isBlank(entity.name()) ? "obj" : entity.name();
 
         String hql = "SELECT " + aliasName + " FROM ";
         List<Object> params = new ArrayList<Object>();
 
         BeanWrapper beanO1 = new BeanWrapperImpl(newObj);
         PropertyDescriptor[] proDescriptorsO1 = beanO1.getPropertyDescriptors();
         int propertyLength = proDescriptorsO1.length;
 
         if (newObj != null) {
             hql += entityName + " " + aliasName;
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
 
         int paramCount = 0;
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
                             hql += " AND " + aliasName + "." + field.getName() + " = ?" + (++paramCount);
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
                                         + propertyDescriptor.getName() + " = ?" + (++paramCount);
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
                     hql += " AND " + aliasName + "." + key + "!= ?" + (++paramCount);
                     params.add(voBase.getNotConditions().get(key));
                 }
             }
         }
 
         // like condition
         if (example instanceof VoBase) {
             VoBase voBase = (VoBase) example;
             for (String key : voBase.getLikeConditions().keySet()) {
                 if (StringUtils.isNotBlank(key)) {
                     hql += " AND " + aliasName + "." + key + " LIKE ?" + (++paramCount);
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
 
     @Override
     public List<T> findByExample(T example, String... orderBy) {
         QueryParams queryParams = translateExampleToQueryParams(example, orderBy);
         return findQueryAsList(queryParams.query, queryParams.params.toArray());
     }
 
     @Override
     public List<T> findByExample(T example, List<String> orderBy) {
         return findByExample(example, orderBy.toArray(new String[0]));
     }
 
     @Override
     public void refresh(T example) {
         getJpaTemplate().refresh(example);
     }
 
     @Override
     public void flush() {
         getJpaTemplate().flush();
     }
 
     @Override
     public int bulkUpdate(String query, final Object... objectArray) {
         final String query2 = convertJpaPositionParams(query);
         return getJpaTemplate().execute(new JpaCallback<Integer>() {
             @Override
             public Integer doInJpa(EntityManager em) throws PersistenceException {
                 Query q = em.createQuery(query2);
                 for (int i = 0; i < objectArray.length; i++) {
                     q.setParameter(i + 1, objectArray[i]);
                 }
                 return q.executeUpdate();
             }
         });
     }
 
     /**
      * convert hibernate positional parameter to JPA positional parameter notation.</br> For example: convert
      * <code>select a from A a where a.id=? and a.status=?</code> to
      * <code>select a from A a where a.id=?1 and a.status=?2</code>
      * 
      * @param query
      * @return
      */
     public String convertJpaPositionParams(String query) {
         if (StringUtils.isBlank(query))
             return query;
 
         // bypass if the query is using JPA positional parameter notation
         if (query.indexOf("?1") >= 0) {
             return query;
         } else if (query.indexOf("?") >= 0) {
             StringBuffer sb = new StringBuffer();
             Pattern p = Pattern.compile(Pattern.quote("?"), Pattern.CASE_INSENSITIVE);
             Matcher matcher = p.matcher(query);
             boolean result = matcher.find();
             int count = 0;
             while (result) {
                 String g = matcher.group();
                 matcher.appendReplacement(sb, g + (++count));
                 result = matcher.find();
             }
 
             matcher.appendTail(sb);
 
             log.debug("sb.toString() = " + sb.toString());
 
             return sb.toString();
         }
 
         return query;
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
