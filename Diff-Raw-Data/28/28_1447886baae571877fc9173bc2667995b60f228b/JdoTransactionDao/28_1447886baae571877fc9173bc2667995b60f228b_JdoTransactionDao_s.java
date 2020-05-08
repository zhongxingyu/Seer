 package com.tapdancingmonk.payload.dao;
 
 import java.util.List;
 
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.inject.Inject;
 import com.tapdancingmonk.payload.model.Transaction;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 public class JdoTransactionDao implements TransactionDao {
 
     private final PersistenceManagerFactory pmf;
 
     @Inject
     public JdoTransactionDao(PersistenceManagerFactory pmf) {
         this.pmf = pmf;
     }
 
     public Transaction save(Transaction txn) {
         PersistenceManager pm = pmf.getPersistenceManager();
         try {
             return pm.makePersistent(txn);
         } finally {
             pm.close();
         }
     }
 
     public Transaction find(String id) {
         PersistenceManager pm = pmf.getPersistenceManager();
         try {
             return pm.getObjectById(Transaction.class, KeyFactory.stringToKey(id));
         } catch (JDOObjectNotFoundException ex) {
            throw new EntityNotFoundException(String.format("entity with id $s doesn't exist", id), ex);
         } finally {
             pm.close();
         }
     }
     
     public void delete(Transaction transaction) {
         PersistenceManager pm = pmf.getPersistenceManager();
         try {
             pm.makePersistent(transaction);
             pm.deletePersistent(transaction);
         } finally {
             pm.close();
         }
     }
 
     @SuppressWarnings("unchecked")
     public Transaction findByTxId(String txId) {
         PersistenceManager pm = pmf.getPersistenceManager();
         
         Query query = pm.newQuery(Transaction.class);
         query.setFilter("transactionId == txId");
         query.declareParameters("String txId");
         
         Transaction txn = null;
         
         try {
             List<Transaction> results = (List<Transaction>) query.execute(txId);
             
             if (results.size() > 1) {
                 throw new DuplicateTransactionIdException();
             } else if (results.size() == 0) {
                throw new EntityNotFoundException(String.format("transaction with txId $s doesn't exist", txId));
             } else {
                 txn = results.get(0);
             }
         } finally {
             query.closeAll();
         }
         
         return txn;
     }
 
 }
