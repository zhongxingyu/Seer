 package com.google.apphosting.api;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.datastore.dev.LocalDatastoreService;
 import com.google.appengine.tools.development.ApiProxyLocalImpl;
 import com.google.apphosting.api.ApiProxy;
 
 /**
  * Mock for the App Engine Java environment used by the JDO wrapper.
  *
  * These class has been build with information gathered on:
 * - App Engine documentation: http://code.google.com/appengine/docs/java/howto/unittesting.html
  * - App Engine Fan blog: http://blog.appenginefan.com/2009/05/jdo-and-unit-tests.html
  *
  * @author Dom Derrien
  */
 public class MockAppEngineEnvironment {
 
     private class ApiProxyEnvironment implements ApiProxy.Environment {
         public String getAppId() {
           return "test";
         }
 
         public String getVersionId() {
           return "1.0";
         }
 
         public String getEmail() {
           throw new UnsupportedOperationException();
         }
 
         public boolean isLoggedIn() {
           throw new UnsupportedOperationException();
         }
 
         public boolean isAdmin() {
           throw new UnsupportedOperationException();
         }
 
         public String getAuthDomain() {
           throw new UnsupportedOperationException();
         }
 
         public String getRequestNamespace() {
           return "";
         }
 
         public Map<String, Object> getAttributes() {
           return new HashMap<String, Object>();
         }
     };
 
     private final ApiProxy.Environment env;
     private PersistenceManagerFactory pmf;
 
     public MockAppEngineEnvironment() {
         env = new ApiProxyEnvironment();
     }
 
     /**
      * Setup the mock environment
      */
     public void setUp() throws Exception {
         // Setup the App Engine services
         ApiProxy.setEnvironmentForCurrentThread(env);
         ApiProxyLocalImpl proxy = new ApiProxyLocalImpl(new File(".")) {};
 
         // Setup the App Engine data store
         proxy.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY, Boolean.TRUE.toString());
         ApiProxy.setDelegate(proxy);
     }
 
     /**
      * Clean up the mock environment
      */
     public void tearDown() throws Exception {
         // Verify that there's no pending transaction (ie pm.close() has been called)
         Transaction transaction = DatastoreServiceFactory.getDatastoreService().getCurrentTransaction(null);
         boolean transactionPending = transaction != null;
         if (transactionPending) {
             transaction.rollback();
         }
 
         // Clean up the App Engine data store
         ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
         if (proxy != null) {
             LocalDatastoreService datastoreService = (LocalDatastoreService) proxy.getService("datastore_v3");
             datastoreService.clearProfiles();
         }
 
         // Clean up the App Engine services
         ApiProxy.setDelegate(null);
         ApiProxy.clearEnvironmentForCurrentThread();
 
         // Report the issue with the transaction still open
         if (transactionPending) {
             throw new IllegalStateException("Found a transaction nor commited neither rolled-back. Probably related to a missing PersistenceManager.close() call.");
         }
     }
 
     /**
      * Creates a PersistenceManagerFactory on the fly, with the exact same information
      * stored in the <war-dir>/WEB-INF/META-INF/jdoconfig.xml file.
      */
     public PersistenceManagerFactory getPersistenceManagerFactory() {
         if (pmf == null) {
             Properties newProperties = new Properties();
             newProperties.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.store.appengine.jdo.DatastoreJDOPersistenceManagerFactory");
             newProperties.put("javax.jdo.option.ConnectionURL", "appengine");
             newProperties.put("javax.jdo.option.NontransactionalRead", "true");
             newProperties.put("javax.jdo.option.NontransactionalWrite", "true");
             newProperties.put("javax.jdo.option.RetainValues", "true");
             newProperties.put("datanucleus.appengine.autoCreateDatastoreTxns", "true");
             newProperties.put("datanucleus.appengine.autoCreateDatastoreTxns", "true");
             pmf = JDOHelper.getPersistenceManagerFactory(newProperties);
         }
         return pmf;
     }
 
     /**
      * Gets an instance of the PersistenceManager class
      */
     public PersistenceManager getPersistenceManager() {
         return getPersistenceManagerFactory().getPersistenceManager();
     }
 }
