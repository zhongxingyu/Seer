 package com.threeti.ics.server.dao.core;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.data.redis.support.atomic.RedisAtomicLong;
 import org.springframework.data.redis.support.collections.DefaultRedisList;
 import org.springframework.data.redis.support.collections.RedisList;
 
 import javax.annotation.PostConstruct;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 
 import static java.lang.String.format;
 
 /**
  * Created by IntelliJ IDEA.
  * User: johnson
  * Date: 9/16/12
  * Time: 6:28 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AbstractGenericDaoImpl<T> implements GenericDao<T> {
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGenericDaoImpl.class);
     @Autowired
     protected RedisTemplate<String, String> template;
     protected RedisAtomicLong idCounter;
     protected RedisList<String> entities;
 
     private Class<T> type;
 
     public AbstractGenericDaoImpl() {
     }
 
     protected AbstractGenericDaoImpl(Class<T> type) {
         this.type = type;
     }
 
    private String getTypeName() {
         return StringUtils.uncapitalize(type.getSimpleName());
     }
 
     protected String getAutoIncrementKey() {
         return format("global.%s:id", getTypeName());
     }
 
     protected String getEntitiesKey() {
         return format("%ss", getTypeName());
     }
 
     protected Long getId(T entity) {
         return (long) 0;
     }
 
     protected String getIdKey(final Long id) {
         return getTypeName() + ":" + id;
     }
 
     @PostConstruct
     public void init() {
         idCounter = new RedisAtomicLong(getAutoIncrementKey(), template.getConnectionFactory());
         entities = new DefaultRedisList<String>(getEntitiesKey(), template);
     }
 
     protected Map getProperties(T entity) {
         try {
             Map<String, String> result = new HashMap<String, String>();
             for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(entity)) {
                 if (pd.getName().contains("class")) continue;
                 Object v = PropertyUtils.getProperty(entity, pd.getName());
                 if (v != null && v instanceof Date) v = ((Date) v).getTime();
                 result.put(pd.getName(), v == null ? StringUtils.EMPTY : v.toString());
             }
             return result;
         } catch (InvocationTargetException ex) {
             LOGGER.error(ex.getMessage());
         } catch (NoSuchMethodException e) {
             LOGGER.error(e.getMessage());
         } catch (IllegalAccessException ex1) {
             LOGGER.error(ex1.getMessage());
         }
         return null;
     }
 
 
     @Override
     public T create(T newInstance) {
         return null;
     }
 
     @Override
     public T findBy(Long id) {
         return null;
     }
 
     @Override
     public T update(T transientObject) {
         return null;
     }
 
     @Override
     public boolean remove(T persistentObject) {
         return false;
     }
 
     @Override
     public List<T> find() {
         List<T> result = new ArrayList<T>();
         for (String k : entities) {
             result.add(findBy(Long.valueOf(k)));
         }
         return result;
     }
 
 }
 
