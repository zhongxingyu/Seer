/*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.core.persistence;
 
 import org.gridlab.gridsphere.core.persistence.castor.Query;
 
 public interface PersistenceInterface {

     /**
      * Begins a transaction
      *
      * @throws PersistenceException of something went wrong
      */
     public void begin() throws PersistenceException;
 
     /**
      * Commits changes to the storage
      *
      * @throws PersistenceException of something went wrong
      */
     public void commit() throws PersistenceException;
 
     /**
      * Rollback the changes since begin()
      *
      * @throws PersistenceException of something went wrong
      */
     public void rollback() throws PersistenceException;
 
     /**
      * Closes the transaction
      */
     public void close();
 
     /**
      * Updates the object in the storage
      *
      * @param object object to be updated
      * @throws UpdateException if something went wrong with the update
      */
     public void update(Object object) throws PersistenceException;
 
     /**
      * Creates an object in the persistence storage
      *
      * @param object object to be created in the storage
      * @throws CreateException if something went wrong with the creation
      */
     public void create(Object object) throws PersistenceException;
 
     /**
      * Deletes an object in the persistence storage
      *
      * @param object object to be deleted
      * @throws DeleteException if something went wrong with the deletion of the object
      */
     public void delete(Object object) throws PersistenceException;
 
     /**
      * Gets a query object
      *
      * @returns Query query object
      * @see Query
      */
     public Query getQuery();
 }
