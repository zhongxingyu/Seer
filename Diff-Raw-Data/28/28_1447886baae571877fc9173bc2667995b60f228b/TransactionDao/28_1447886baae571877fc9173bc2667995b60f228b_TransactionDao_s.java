 package com.tapdancingmonk.payload.dao;
 
 import com.tapdancingmonk.payload.model.Transaction;
 
 public interface TransactionDao {
 
    Transaction save(Transaction product);
 
     Transaction find(String id) throws EntityNotFoundException;
 
 }
