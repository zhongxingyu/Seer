 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.jta;
 
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.transaction.UserTransaction;
 
 import com.google.common.base.Preconditions;
 
 /**
  * Decorator for {@link UserTransaction} which counts pending, committed and rolled back
  * transaction using the associated {@link TransactionCounter}.
  * 
  * @author Tobias Sarnowski
  * @author Willi Schoenborn
  */
 public class UserTransactionCounter extends ForwardingUserTransaction {
 
     private static final long serialVersionUID = 4813218251170299613L;
 
     private final TransactionCounter counter;
     
     private final UserTransaction transaction;
 
     protected UserTransactionCounter(UserTransaction transaction, TransactionCounter counter) {
         this.transaction = Preconditions.checkNotNull(transaction, "Transaction");
         this.counter = Preconditions.checkNotNull(counter, "Counter");
     }
     
     @Override
     protected UserTransaction delegate() {
         return transaction;
     }
 
     @Override
     public void begin() throws NotSupportedException, SystemException {
         super.begin();
         counter.getPending().incrementAndGet();
     }
 
     @Override
    public void commit() throws SystemException, RollbackException, 
        HeuristicRollbackException, HeuristicMixedException {
         super.commit();
         counter.getPending().decrementAndGet();
         counter.getCommitted().incrementAndGet();
     }
 
     @Override
     public void rollback() throws SystemException {
         try {
             super.rollback();
             counter.getRolledbackSuccess().incrementAndGet();
         } catch (SystemException e) {
             counter.getRolledbackFailed().incrementAndGet();
             throw e;
         } finally {
             counter.getPending().decrementAndGet();
         }
     }
 
     @Override
     public String toString() {
         return String.format("{%s COUNTED p%s c%s r%s}",
                 transaction.toString(),
             counter.getPending(), counter.getCommitted(), counter.getRolledback());
     }
     
 }
