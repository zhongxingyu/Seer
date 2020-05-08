 package ai.ilikeplaces.jpa;
 
 import ai.ilikeplaces.doc.CONVENTION;
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.doc.NOTE;
 import ai.ilikeplaces.doc.OK;
 import ai.ilikeplaces.exception.DBDishonourCheckedException;
 import ai.ilikeplaces.exception.DBDishonourException;
 import ai.ilikeplaces.util.AbstractSLBCallbacks;
 
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.*;
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * @author Ravindranath Akila
  * @param <T>
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Stateless
 @CONVENTION(convention = "Caller assumes this class methods are of tx scope supports, " +
         "but create and updates here are of scope mandatory which will ensure caller does no mistake.")
 @OK
final public class CrudService<T> extends AbstractSLBCallbacks implements CrudServiceLocal<T> {
 
     /**
      * Please not that this is a field of Stateless session bean
      */
     @NOTE(note = "find out how the manager handles concurrent requests. same cache or different? if different, come on, this a class variable!" +
             "resolved! check out the hibernate site article on this. apparently, container does this. amazing!")
     @PersistenceContext(unitName = "adimpression_ilikeplaces_war_1.6-SNAPSHOTPU", type = PersistenceContextType.TRANSACTION)
     public EntityManager entityManager;
 
     /**
      * @param t
      * @return
      */
     @TransactionAttribute(TransactionAttributeType.MANDATORY)
     @Override
     public T create(final T t) {
         entityManager.persist(t);
         entityManager.flush();
         entityManager.refresh(t);
         return t;
     }
 
     /**
      * @param type
      * @param id
      * @return
      */
     @SuppressWarnings("unchecked")
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public T find(final Class type, final Object id) {
         return (T) entityManager.find(type, id);
     }
 
     /**
      * @param type
      * @param id
      * @return
      */
     @SuppressWarnings("unchecked")
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public T getReference(final Class type, final Object id) {
         return (T) entityManager.getReference(type, id);
     }
 
     /**
      *
      * @param typeOfEntity
      * @param idByWhichToLookup
      * @return
      */
     @SuppressWarnings("unchecked")
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public T findBadly(Class typeOfEntity, Object idByWhichToLookup) {
         final Object object = entityManager.find(typeOfEntity, idByWhichToLookup);
         if (object != null) {
             return (T) object;
         } else {
             throw DBDishonourException.QUERYING_AFFIRMATIVELY_A_NON_EXISTING_ENTITY;
         }
     }
 
     /**
      * @param type
      * @param id
      */
     @SuppressWarnings("unchecked")
     @Override
     @TransactionAttribute(TransactionAttributeType.MANDATORY)
     public void delete(final Class type, final Object id) {
         entityManager.remove(this.entityManager.getReference(type, id));
     }
 
     /**
      * @param t
      * @return
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.MANDATORY)
     public T update(final T t) {
         return entityManager.merge(t);
     }
 
     /**
      * @param namedQueryName
      * @return
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List findWithNamedQuery(final String namedQueryName) {
         return entityManager.createNamedQuery(namedQueryName).getResultList();
     }
 
     /**
      * @param namedQueryName
      * @param parameters
      * @return
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List findWithNamedQuery(final String namedQueryName, final Map parameters) {
         return findWithNamedQuery(namedQueryName, parameters, 0);
     }
 
     /**
      * @param queryName
      * @param resultLimit
      * @return
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List findWithNamedQuery(final String queryName, final int resultLimit) {
         return entityManager.createNamedQuery(queryName).
                 setMaxResults(resultLimit).
                 getResultList();
     }
 
     /**
      * @param sql
      * @param type
      * @return
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List findByNativeQuery(final String sql, final Class type) {
         return this.entityManager.createNativeQuery(sql, type).getResultList();
     }
 
     /**
      * @param namedQueryName
      * @param parameters
      * @param resultLimit
      * @return
      */
     @Override
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List findWithNamedQuery(final String namedQueryName, final Map parameters, final int resultLimit) {
         @SuppressWarnings("unchecked")
         final Set<Entry> rawParameters = parameters.entrySet();
         final Query query = entityManager.createNamedQuery(namedQueryName);
         if (resultLimit > 0) {
             query.setMaxResults(resultLimit);
         }
         for (final Entry entry : rawParameters) {
             query.setParameter((String) entry.getKey(), entry.getValue());
         }
         return query.getResultList();
     }
 
     /**
      * @return toString_
      */
     @Override
     public String toString() {
         String toString_ = getClass().getName();
         try {
             final Field[] fields = {getClass().getDeclaredField("entityManager")};
 
             for (final Field field : fields) {
                 try {
                     toString_ += "\n{" + field.getName() + "," + field.get(this) + "}";
                 } catch (IllegalArgumentException ex) {
                     INFO.info(null, ex);
                 } catch (IllegalAccessException ex) {
                     INFO.info(null, ex);
                 }
             }
         } catch (NoSuchFieldException ex) {
             INFO.info(null, ex);
         } catch (SecurityException ex) {
             INFO.info(null, ex);
         }
 
         return toString_;
     }
 
     /**
      * @param showChangeLog__
      * @return changeLog
      */
     public String toString(final boolean showChangeLog__) {
         String changeLog = toString() + "\n";
         changeLog += "20090915 Added Javadoc\n";
         return showChangeLog__ ? changeLog : toString();
     }
 }
 
