 package org.tptp.model;
 
 /**
  * A transaction is a way to group changes to the backend.
  *
  * @see WorkManaager
  */
 public interface Transaction {
     /**
      *
      */
     public void beginTransaction();
     
     /**
     * Close this transaction and free underlying resources. 
      */
     public void endTransaction();
     
     /**
      * Saves all changes made in the current transaction.
      */
     public void commit();
     
     /**
      * Rollback all changes made in the current transaction.
      */
     public void rollback();
 }
