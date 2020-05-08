 package org.jenkinsci.plugins.jbpm;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.transaction.UserTransaction;
 
 import org.drools.persistence.jta.JtaTransactionManager;
 
 public class PluginTransactionManager {
 
     public static final String JBOSS_USER_TRANSACTION_NAME = "java:jboss/UserTransaction";
     
     private static UserTransaction findUserTransaction() {
         try {
             InitialContext context = new InitialContext();
             return (UserTransaction) context.lookup( JBOSS_USER_TRANSACTION_NAME );
         } catch ( NamingException ex ) {
             JbpmPluginLogger.debug(ex);
             throw new IllegalStateException("Unable to find transaction: " + ex.getMessage(), ex);
         }
     }
     
     public static JtaTransactionManager getTransactionManager() {
         return new JtaTransactionManager(findUserTransaction(), null, null);
     }
 }
