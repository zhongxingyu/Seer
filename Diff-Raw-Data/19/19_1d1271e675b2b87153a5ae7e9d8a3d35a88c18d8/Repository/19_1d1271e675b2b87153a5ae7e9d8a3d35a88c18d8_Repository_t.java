 package org.lazydog.repository;
 
 import java.util.List;
 
 
 /**
  * Repository.
  *
  * @author  Ron Rickard
  */
 public interface Repository {
 
     /**
      * Find the entity.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      *
      * @return  the entity.
      */
     <T,U> T find(Class<T> entityClass, U id);
 
     /**
      * Find the entity.
      *
      * @param  entityClass  the entity class.
      * @param  criteria     the criteria.
      *
      * @return  the entity.
      */
    <T> T find(Class<T> entityClass, Criteria<T> criteria);
 
     /**
      * Find the list of entities.
      *
      * @param  entityClass  the entity class.
      *
      * @return  the list of entities.
      */
     <T> List<T> findList(Class<T> entityClass);
 
     /**
      * Find the list of entities.
      *
      * @param  entityClass  the entity class.
      * @param  criteria     the criteria.
      * 
      * @return  the list of entities.
      */
     <T> List<T> findList(Class<T> entityClass, Criteria<T> criteria);
 
     /**
      * Get the criteria.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the criteria.
      */
     <T> Criteria<T> getCriteria(Class<T> entityClass);
 
     /**
      * Persist the entity.
      *
      * @param  entity  the entity.
      *
      * @return  the persisted entity.
      */
     <T> T persist(T entity);
 
     /**
      * Persist the list of entities.
      *
      * @param  entities  the entities.
      *
      * @return  the persisted list of entities.
      */
     <T> List<T> persistList(List<T> entities);
 
     /**
      * Remove the entity.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      */
     <T,U> void remove(Class<T> entityClass, U id);
 
     /**
     * Remove the entities by the list of IDs.
      *
      * @param  entityClass  the entity class.
      * @param  ids          the IDs.
      */
     <T,U> void removeList(Class<T> entityClass, List<U> ids);
 }
