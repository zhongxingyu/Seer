 package fr.cg95.cvq.dao.jpa;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
 import javax.persistence.Query;
 
 import fr.cg95.cvq.dao.hibernate.SimpleQuery;
 
 public abstract class JpaTemplate<T, ID extends Serializable> implements IJpaTemplate<T, ID> {
 
     protected final Class<T> entityClass;
     protected EntityManager entityManager;
 
     public Class<T> getEntityClass() {
         return entityClass;
     }
 
     @SuppressWarnings("unchecked")
     private Class<T> getByReflexionEntityClass(){
         Type type = getClass().getGenericSuperclass();
         if (type instanceof ParameterizedType){
             ParameterizedType paramType = (ParameterizedType) type;
             return (Class<T>) paramType.getActualTypeArguments()[0];
         }else{
             type = getClass().getSuperclass().getGenericSuperclass();
             ParameterizedType paramType = (ParameterizedType) type;
             return (Class<T>) paramType.getActualTypeArguments()[0];
         }
     }
 
     public JpaTemplate() {
         this.entityClass = getByReflexionEntityClass();
     }
 
     public EntityManager getEntityManager() {
         return JpaUtil.getEntityManager();
     }
 
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
    public PersistenceUnitUtil getPersistenceUnitUtil(){
       return JpaUtil.getPersistenceUnitUtil();
    }
     /* -- CRUD -- */
 
     @Override
     public T create(T object) {
         getEntityManager().persist(object);
         return object; // can't return the id smoothly
     }
 
     @Override
     public T saveOrUpdate(T object) {
         // Not sure that this is a good idea..
         if (getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(object) == null) {
             return create(object);
         } else {
             update(object);
             return object;
         }
     }
 
     @Override
     public void update(T object) {
         getEntityManager().merge(object);
     }
 
     @Override
     public void delete(T object) {
        T objectManaged = (T) getEntityManager().find(object.getClass(), getPersistenceUnitUtil().getIdentifier(object));
        getEntityManager().remove(objectManaged);
     }
 
     /* -- Payload -- */
 
     @SuppressWarnings("unchecked")
     @Override
     public List<T> findBy(String query, Object... params) {
         Query q = getEntityManager().createQuery(
                 createFindByQuery(getEntityClass().getName(), query, params));
         return bindParameters(q, params).getResultList();
     }
 
     @Override
     public T findById(ID id) {
         return getEntityManager().find(getEntityClass(), id);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<T> findAll() {
         return getEntityManager().createQuery("select e from " + getEntityClass().getName() + " e").getResultList();
     }
 
     @Override
     public Long count() {
         return Long.parseLong(getEntityManager()
                 .createQuery("select count(e) from " + getEntityClass().getName() + " e").getSingleResult().toString());
     }
 
     @Override
     public SimpleQuery simpleSelect(Class<?> clazz) {
         return new SimpleQuery(clazz);
     }
 
     @Override
     public SimpleQuery simpleSelect() {
         SimpleQuery simpleQuery = new SimpleQuery(getEntityClass());
         return simpleQuery;
     }
 
     /* -- copied from playframework (http://www.playframework.org) */
     @SuppressWarnings("unchecked")
     protected Query bindParameters(Query q, Object... params) {
         if (params == null) {
             return q;
         }
         if (params.length == 1 && params[0] instanceof Map) {
             return bindParameters(q, (Map<String, Object>) params[0]);
         }
         for (int i = 0; i < params.length; i++) {
             q.setParameter(i + 1, params[i]);
         }
         return q;
     }
 
     private Query bindParameters(Query q, Map<String, Object> params) {
         if (params == null) {
             return q;
         }
         for (String key : params.keySet()) {
             q.setParameter(key, params.get(key));
         }
         return q;
     }
 
     protected String createFindByQuery(String entityName, String query, Object... params) {
         if (query == null || query.trim().length() == 0) {
             return "from " + entityName;
         }
         if (query.matches("^by[A-Z].*$")) {
             return "from " + entityName + " where " + findByToJPQL(query);
         }
         if (query.trim().toLowerCase().startsWith("select ")) {
             return query;
         }
         if (query.trim().toLowerCase().startsWith("from ")) {
             return query;
         }
         if (query.trim().toLowerCase().startsWith("order by ")) {
             return "from " + entityName + " " + query;
         }
         if (query.trim().indexOf(" ") == -1 && query.trim().indexOf("=") == -1 && params != null && params.length == 1) {
             query += " = ?1";
         }
         if (query.trim().indexOf(" ") == -1 && query.trim().indexOf("=") == -1 && params == null) {
             query += " = null";
         }
         return "from " + entityName + " where " + query;
     }
 
     private String findByToJPQL(String findBy) {
         findBy = findBy.substring(2);
         StringBuffer jpql = new StringBuffer();
         String[] parts = findBy.split("And");
         for (int i = 0; i < parts.length; i++) {
             String part = parts[i];
             if (part.endsWith("NotEqual")) {
                 String prop = extractProp(part, "NotEqual");
                 jpql.append(prop + " <> ?");
             } else if (part.endsWith("Equal")) {
                 String prop = extractProp(part, "Equal");
                 jpql.append(prop + " = ?");
             } else if (part.endsWith("IsNotNull")) {
                 String prop = extractProp(part, "IsNotNull");
                 jpql.append(prop + " is not null");
             } else if (part.endsWith("IsNull")) {
                 String prop = extractProp(part, "IsNull");
                 jpql.append(prop + " is null");
             } else if (part.endsWith("LessThan")) {
                 String prop = extractProp(part, "LessThan");
                 jpql.append(prop + " < ?");
             } else if (part.endsWith("LessThanEquals")) {
                 String prop = extractProp(part, "LessThanEquals");
                 jpql.append(prop + " <= ?");
             } else if (part.endsWith("GreaterThan")) {
                 String prop = extractProp(part, "GreaterThan");
                 jpql.append(prop + " > ?");
             } else if (part.endsWith("GreaterThanEquals")) {
                 String prop = extractProp(part, "GreaterThanEquals");
                 jpql.append(prop + " >= ?");
             } else if (part.endsWith("Between")) {
                 String prop = extractProp(part, "Between");
                 jpql.append(prop + " < ? AND " + prop + " > ?");
             } else if (part.endsWith("Like")) {
                 String prop = extractProp(part, "Like");
                 jpql.append("LOWER(" + prop + ") like ?");
             } else if (part.endsWith("Ilike")) {
                 String prop = extractProp(part, "Ilike");
                 jpql.append("LOWER(" + prop + ") like LOWER(?)");
             } else if (part.endsWith("Elike")) {
                 String prop = extractProp(part, "Ilike");
                 jpql.append(prop + " like LOWER(?)");
             } else {
                 String prop = extractProp(part, "");
                 jpql.append(prop + " = ?");
             }
             if (i < parts.length - 1) {
                 jpql.append(" AND ");
             }
         }
         return jpql.toString();
     }
 
     private static String extractProp(String part, String end) {
         String prop = part.substring(0, part.length() - end.length());
         prop = (prop.charAt(0) + "").toLowerCase() + prop.substring(1);
         return prop;
     }
 
 }
