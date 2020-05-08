 package org.northstar.bricks.core.orientdb;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
 import com.orientechnologies.orient.core.index.OIndexes;
 import com.orientechnologies.orient.object.db.OObjectDatabasePool;
 import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
 import org.northstar.bricks.config.BricksConstants;
 
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: 北辰
  * Date: 12-9-15
  * Time: 上午1:00
  * To change this template use File | Settings | File Templates.
  */
 public class OrientdbProvider implements Provider<OObjectDatabaseTx> {
     @Inject
     private Logger logger;
 
     public OrientdbProvider() {
     }
 
     @Override
     public OObjectDatabaseTx get() {
         int maxRetry = 100; // give it up to approx 10 seconds to recover
         int retryCount = 0;
         OObjectDatabaseTx databaseTx = null;
         ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
         while (databaseTx == null && retryCount < maxRetry) {
             retryCount++;
             try {
                 ClassLoader orientClassLoader = OIndexes.class.getClassLoader();
                 Thread.currentThread().setContextClassLoader(orientClassLoader);
                 databaseTx = OObjectDatabasePool.global().acquire(BricksConstants.ORIENTDB_URL, BricksConstants.ORIENTDB_USER, BricksConstants.ORIENTDB_PASSWORD);
                 if (retryCount > 1) {
                     logger.info("Succeeded in acquiring connection from pool in retry attempt " + retryCount);
                 }
                 retryCount = maxRetry;
             } catch (ORecordNotFoundException ex) {
                 if (retryCount == maxRetry) {
                     logger.warning("Failure reported acquiring connection from pool, retried " + retryCount + " times before giving up.");
                 } else {
                     logger.info("Pool acquire reported failure, retrying - attempt " + retryCount);
                     try {
                         Thread.sleep(100); // Give the DB time to complete what it's doing before retrying
                     } catch (InterruptedException iex) {
                         // ignore that sleep was interrupted
                     }
                 }
             } finally {
                 Thread.currentThread().setContextClassLoader(origClassLoader);
             }
 
         }
        databaseTx.getEntityManager().registerEntityClasses(BricksConstants.ENTITY_PACKAGE);
         //databaseTx.setMVCC(false);
         return databaseTx;
     }
 }
