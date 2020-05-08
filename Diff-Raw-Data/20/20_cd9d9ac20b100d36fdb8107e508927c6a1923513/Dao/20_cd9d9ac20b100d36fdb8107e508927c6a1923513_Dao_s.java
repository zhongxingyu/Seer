 package com.thingtrack.konekti.dao.template;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.metamodel.EntityType;
 
 /**
  * Generic Repository, providing basic CRUD operations
  *
  * @author Thingtrack S.L.
  *
  * @param <T> the entity type
  * @param <ID> the primary key type
  */
 public interface Dao<T, ID extends Serializable> {
     /**
      * Get the Class of the entity.
      *
      * @return the class
      */
     Class<T> getEntityClass();
 
     /**
      * Get the Name of the entity.
      *
      * @return the class
      */
     String getEntityName();
 
     /**
      * get Entity Type Metamodel.
      * 
      */
     EntityType<T> getEntityType() throws Exception;
     
     /**
      * Find an entity by its primary key
      *
      * @param id the primary key
      * @return the entity
      */
     T get(final ID id);
     
     /**
      * Load all entities.
      *
      * @return the list of entities
      */
     List<T> getAll() throws Exception;
     
     /**
      * save an entity. This can be either a INSERT or UPDATE in the database.
      * 
      * @param entity the entity to save
      * 
      * @return the saved entity
      */
     T save(final T entity) throws Exception;
     
     /**
      * delete an entity from the database. This can be DELETE in the database.
      * 
      * @param entity the entity to delete
      */
     void delete(final T entity) throws Exception;
  
     /**
      * Find filtered entities
      * @param <K>
      *
      * @param list of filters
      * @return the list entities
      */
     List<T> getFiltered(List<DaoFilter> filters) throws Exception;
 }
