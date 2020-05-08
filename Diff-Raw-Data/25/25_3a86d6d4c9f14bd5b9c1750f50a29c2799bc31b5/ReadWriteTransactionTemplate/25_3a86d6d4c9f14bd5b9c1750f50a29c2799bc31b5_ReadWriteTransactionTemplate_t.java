package com.atlassian.sal.spring.transaction;
 
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

 /**
  * An implementation that executes an action in a ReadWrite transaction.  Requires spring.
  */
 public class ReadWriteTransactionTemplate implements TransactionTemplate
 {
     public Object execute(final TransactionCallback action)
     {
         final PlatformTransactionManager transactionManager = ComponentLocator.getComponent(PlatformTransactionManager.class);
         final org.springframework.transaction.support.TransactionTemplate txTemplate =
                 new org.springframework.transaction.support.TransactionTemplate(transactionManager, getTransactionDefinition());
         return txTemplate.execute(new org.springframework.transaction.support.TransactionCallback()
         {
             public Object doInTransaction(TransactionStatus transactionStatus)
             {
                 try
                 {
                     return action.doInTransaction();
                 }
                 catch (RuntimeException e)
                 {
                     //rollback if something weird happened.
                     transactionStatus.setRollbackOnly();
                     throw e;
                 }
             }
         });
     }
 
     protected DefaultTransactionDefinition getTransactionDefinition()
     {
         DefaultTransactionDefinition def = new DefaultTransactionDefinition();
         def.setName("SAALReadWriteTx");
         def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
         def.setReadOnly(false);
         return def;
     }
 }
