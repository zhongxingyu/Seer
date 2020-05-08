 package org.lazydog.entry.internal.account.manager;
 
 import java.util.Properties;
 import org.lazydog.entry.spi.account.manager.EntryAccountManager;
 import org.lazydog.entry.spi.account.manager.EntryAccountManagerFactory;
 
 
 /**
  * Entry account manager factory implementation.
  *
  * @author  Ron Rickard
  */
 public class EntryAccountManagerFactoryImpl extends EntryAccountManagerFactory {
 
     /**
      * Create the Entry account manager.
      *
      * @param  environment  the environment.
      *
      * @return  the Entry account manager.
      */
     @Override
     public EntryAccountManager createEntryAccountManager(Properties environment) {
 
         // Declare.
         EntryAccountManagerImpl entryAccountManager;
 
         // Create the Entry account manager.
         entryAccountManager = new EntryAccountManagerImpl();
         entryAccountManager.setEnvironment(environment);
 
         return entryAccountManager;
     }

 }
