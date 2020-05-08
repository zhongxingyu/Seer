 /*
  * Copyright 2008 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.txn;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import com.amazon.carbonado.IsolationLevel;
 import com.amazon.carbonado.Transaction;
 
 /**
  * 
  *
  * @author Brian S O'Neill
  */
 public class TestTransactionManager extends TestCase {
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static TestSuite suite() {
         return new TestSuite(TestTransactionManager.class);
     }
 
     TM mTxnMgr;
 
     public TestTransactionManager(String name) {
         super(name);
     }
 
     protected void setUp() {
         mTxnMgr = new TM();
     }
 
     public void testAttachDetach() throws Exception {
         Transaction txn = mTxnMgr.localScope().enter(null);
 
         // This batch of tests just ensures no IllegalStateExceptions are thrown.
         {
             // Should do nothing.
             txn.attach();
             // Should detach.
             txn.detach();
             // Should do nothing.
             txn.detach();
             // Should attach.
             txn.attach();
         }
 
         // Set up for multi-threaded tests.
 
         ExecutorService es = Executors.newSingleThreadExecutor();
 
         final Transaction txn2 = es.submit(new Callable<Transaction>() {
             public Transaction call() {
                 return mTxnMgr.localScope().enter(null);
             }
         }).get();
 
         try {
             txn2.attach();
             fail();
         } catch (IllegalStateException e) {
         }
 
         try {
             txn2.detach();
             fail();
         } catch (IllegalStateException e) {
         }
 
         txn.detach();
 
         try {
             txn2.attach();
             fail();
         } catch (IllegalStateException e) {
         }
 
         es.submit(new Runnable() {
             public void run() {
                 txn2.detach();
             }
         }).get();
 
         txn2.attach();
         txn2.detach();
         txn2.attach();
 
         try {
             txn.attach();
             fail();
         } catch (IllegalStateException e) {
         }
 
         txn.detach();
 
         txn2.exit();
 
        // Thread with txn2 was attached but is inactive, so txn thread can
        // steal it. Exiting a top-level transaction implicitly detaches
        // it. This can happen if a remote connection is dropped, but the
        // thread which is automatically exiting the open transaction isn't the
        // one which is attached. Detach is not allowed, so it can only exit.
        txn.attach();

         try {
            txn2.detach();
             fail();
         } catch (IllegalStateException e) {
            // Cannot detach since attach was stolen. Throwing an exception
            // here is the correct thing, since it indicates a bug in the
            // application. The txn2 thread didn't detach properly and it let
            // the txn thread try to get the transaction.
         }
 
         txn.detach();
         txn.attach();
         txn.detach();
 
         es.shutdown();
     }
 
     public void testBrokenRollback() throws Exception {
         TransactionScope<Txn> scope = mTxnMgr.localScope();
         Transaction txn = scope.enter(null);
         Txn t = scope.getTxn();
         t.doFail = true;
 
         try {
             txn.exit();
             fail();
         } catch (Exception e) {
         }
 
         Transaction txn2 = scope.enter(null);
         Txn t2 = scope.getTxn();
 
         // If not null, then exception thrown while exiting previous
         // transaction did not properly clean up. As a result, new transaction
         // is nested.
         assertNull(t2.parent);
     }
 
     private static class Txn {
         final Txn parent;
 
         volatile boolean doFail;
 
         Txn(Txn parent) {
             this.parent = parent;
         }
 
         void abort() {
             if (doFail) {
                 throw new IllegalStateException("fail");
             }
         }
     }
 
     private static class TM extends TransactionManager<Txn> {
         protected IsolationLevel selectIsolationLevel(Transaction parent, IsolationLevel level) {
             return IsolationLevel.READ_UNCOMMITTED;
         }
 
         protected boolean supportsForUpdate() {
             return true;
         }
 
         protected Txn createTxn(Txn parent, IsolationLevel level) {
             return new Txn(parent);
         }
 
         protected boolean commitTxn(Txn txn) {
             return false;
         }
 
         protected void abortTxn(Txn txn) {
             txn.abort();
         }
     }
 }
