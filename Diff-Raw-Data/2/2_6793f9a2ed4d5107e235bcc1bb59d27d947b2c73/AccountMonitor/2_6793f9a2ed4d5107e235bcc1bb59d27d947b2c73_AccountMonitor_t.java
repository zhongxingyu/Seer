 /*
  * Copyright 2009 LugIron Software, Inc. All Rights Reserved.
  *
  * $Id$
  */
 
 package com.trustwave.transaction;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.Condition;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public class AccountMonitor implements AppConstants {
     private static final Logger logger = LoggerFactory.getLogger(AccountMonitor.class);
 
     private enum State {
         WAITING, TRANSACTING
     }
 
     private boolean verbose = false;
 
     protected final List<Account> accounts;
 
     private final AtomicBoolean exitSignal = new AtomicBoolean(false);
 
     private final ReentrantLock lock = new ReentrantLock();
 
     private final List<Condition> conditions = new ArrayList<Condition>();
 
     private final List<State> states = new ArrayList<State>();
 
     protected final Map<Account, List<AccountLockRequest>> requestMap = new HashMap<Account, List<AccountLockRequest>>();
 
     public AccountMonitor(final List<Account> accounts, final int threadCount) {
         this.accounts = accounts;
 
         for (final Account account : accounts) {
             requestMap.put(account, new ArrayList<AccountLockRequest>());
         }
 
         for (int x = 0; x < threadCount; ++x) {
             conditions.add(lock.newCondition());
             states.add(State.WAITING);
         }
 
         final String verboseString = System.getProperty(VERBOSE);
         if (verboseString != null && verboseString.equals(VERBOSE_ON)) {
             verbose = true;
         }
     }
 
     public void test(final Account source, final Account destination, final int id) {
 
         final boolean sourceAquire = source.aquireLock();
         final boolean destinationAquire = destination.aquireLock();
 
         if (sourceAquire && destinationAquire) {
             states.set(id, State.TRANSACTING);
             if (lock.hasWaiters(conditions.get(id))) {
                 conditions.get(id).signal();
             }
             return;
         }
 
         final AccountLockRequest request = new AccountLockRequest(source, destination, id);
 
         if (!sourceAquire) {
             destination.releaseLock();
             requestMap.get(source).add(request);
         }
 
         if (!destinationAquire) {
             source.releaseLock();
             requestMap.get(destination).add(request);
         }
 
        logger.debug("{} is waiting source: {}, dest: {}", new Object[]{"t" + id, source.getId(), destination.getId()});
     }
 
     public void aquire(final Account source, final Account destination, final int id) {
         lock.lock();
         try {
 
             test(source, destination, id);
 
             if (!states.get(id).equals(State.TRANSACTING)) {
                 try {
                     conditions.get(id).await();
                 }
                 catch (InterruptedException e) {
                     logger.debug("{} has been interrupted", id);
                 }
             }
         }
         finally {
             lock.unlock();
         }
     }
 
     public void release(final Account source, final Account destination, final int id) {
         lock.lock();
         try {
 
             states.set(id, State.WAITING);
 
             source.releaseLock();
             destination.releaseLock();
 
             honorRequested(source, destination);
 
         }
         finally {
             lock.unlock();
         }
     }
 
     public void transact(final Account source, final Account destination, final int transferAmount)
             throws ZeroBalanceException {
 
         if (exitSignal.get()) {
             throw new ZeroBalanceException();
         }
 
         source.withdrawl(transferAmount);
         destination.deposit(transferAmount);
 
         hasZeroBalance(source);
     }
 
     public Account findById(final int accountId) {
         return accounts.get(accountId);
     }
 
     private void honorRequested(final Account source, final Account destination) {
         final HashSet<AccountLockRequest> reqs = new HashSet<AccountLockRequest>();
 
         for (final AccountLockRequest req : requestMap.remove(source)) {
             reqs.add(req);
 
             cleanupDoubleReference(req);
         }
 
         for (final AccountLockRequest req : requestMap.remove(destination)) {
             reqs.add(req);
 
             cleanupDoubleReference(req);
         }
 
         requestMap.put(source, new ArrayList<AccountLockRequest>());
         requestMap.put(destination, new ArrayList<AccountLockRequest>());
 
         for (final AccountLockRequest req : reqs) {
             logger.debug("during release of accounts: {}, {}, testing thread: {}, source: {}, dest: {}",
                     new Object[]{source.getId(), destination.getId(), "t" + req.getId(), req.getSource().getId(),
                             req.getDestination().getId()});
             test(req.getSource(), req.getDestination(), req.getId());
         }
     }
 
     private void cleanupDoubleReference(final AccountLockRequest req) {
         if (requestMap.get(req.getSource()) != null) {
             requestMap.get(req.getSource()).remove(req);
         }
         if (requestMap.get(req.getDestination()) != null) {
             requestMap.get(req.getDestination()).remove(req);
         }
     }
 
     private void hasZeroBalance(final Account account) throws ZeroBalanceException {
         if (account.getBalance() == 0) {
             exitSignal.set(true);
             final String zeroBalanceMessage = String.format("zomg account: %d  has zero balance!", account.getId());
             if (verbose) {
                 System.out.println(zeroBalanceMessage);
             }
             logger.debug(zeroBalanceMessage);
             throw new ZeroBalanceException();
         }
     }
 
     public void print() {
         System.out.println("Account Id    |   Balance");
         System.out.println("-------------------------");
 
         for (int x = 0; x < accounts.size(); ++x) {
             System.out.println(String.format("%10d    |   %7d", x, accounts.get(x).getBalance()));
         }
     }
 
     public int getNumberOfAccount() {
         return accounts.size();
     }
 
     protected static class AccountLockRequest {
         private final Account source;
         private final Account destination;
         private final int id;
 
         public AccountLockRequest(final Account source, final Account destination, final int id) {
             this.source = source;
             this.destination = destination;
             this.id = id;
         }
 
         public int getId() {
             return id;
         }
 
         public Account getSource() {
             return source;
         }
 
         public Account getDestination() {
             return destination;
         }
     }
 }
