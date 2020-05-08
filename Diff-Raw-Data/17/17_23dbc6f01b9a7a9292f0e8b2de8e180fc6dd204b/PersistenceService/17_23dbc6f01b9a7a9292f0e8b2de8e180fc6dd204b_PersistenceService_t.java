 // Copyright (c) 2010, www.andrew-eells.com. All rights reserved.
 
 package com.andrew_eells.persistence.service;
 
 import com.andrew_eells.persistence.infrastructure.query.QuerySpecification;
 
 import java.util.List;
 
 /**
  * Standardised database repository access.
  */
public interface PersistenceService<PersistenceStrategy>
 {
     /**
      * Persist new object.
      *
      * @param object New object.
      */
    void create(final PersistenceStrategy object);
 
     /**
      * Read unique result object.
      *
      * @param querySpecification Query specification.
      * @return Unique result or <code>null</code> if none exists.
      */
    PersistenceStrategy readUnique(final QuerySpecification querySpecification);
 
     /**
      * Read collection of result objects.
      *
      * @param querySpecification Query specification.
      * @return Collection of results or empty collection if none exists.
      */
    List<PersistenceStrategy> readList(final QuerySpecification querySpecification);
 
     /**
      * Update existing object.
      *
      * @param object Object to be updated.
      */
    void update(final PersistenceStrategy object);
 
     /**
      * Delete existing object.
      *
      * @param object Object to be deleted.
      */
    void delete(final PersistenceStrategy object);
 }
