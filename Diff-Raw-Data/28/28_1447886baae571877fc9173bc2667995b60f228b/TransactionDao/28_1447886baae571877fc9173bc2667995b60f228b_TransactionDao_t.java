 package com.tapdancingmonk.payload.dao;
 
 import com.tapdancingmonk.payload.model.Transaction;
 
 public interface TransactionDao {
 
    Transaction save(Transaction txn);
 
     Transaction find(String id) throws EntityNotFoundException;
    
    void delete(Transaction txn);
    
    Transaction findByTxId(String txId) throws EntityNotFoundException, DuplicateTransactionIdException;
 
 }
