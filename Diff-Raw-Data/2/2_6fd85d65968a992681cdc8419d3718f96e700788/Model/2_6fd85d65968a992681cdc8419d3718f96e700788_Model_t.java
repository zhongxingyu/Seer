 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.model;
 
 import java.io.Serializable;
 
 /**
  * Specifies a persistent instance behavior.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
 * @param <T> specifies the {@code Class} of the id for the {@code Entity}
  */
 public interface Model<T extends Serializable> extends Serializable {
 
     /**
      * Provides the value of the optimistic locking column.
      *
      * @return - the value
      */
     Long getVersion();
 
     /**
      * Return the identifier of the entity.
      *
      * @return the key identifier of the entity
      */
     T getId();
 
     /**
      * Specifies the key identifier for the entity.
      *
      * @param id specifies the identifier for the entity
      */
     void setId(T id);
 }
