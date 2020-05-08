 package org.daum.library.android.daumauth;
 
 import android.util.Log;
 import org.daum.library.ormH.persistence.PersistenceConfiguration;
 import org.daum.library.ormH.persistence.PersistenceSessionFactoryImpl;
 import org.daum.library.ormH.persistence.PersistenceSession;
 import org.daum.library.ormH.store.ReplicaStore;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.daum.library.replica.listener.SyncListener;
 import org.daum.library.replica.msg.SyncEvent;
 import org.sitac.Agent;
 
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: max
  * Date: 06/07/12
  * Time: 11:35
  * To change this template use File | Settings | File Templates.
  */
 public class DaumAuthEngine {
 
     private static final String TAG = "DaumAuthEngine";
 
     private PersistenceSessionFactoryImpl factory;
     private boolean synced = false;
 
     public DaumAuthEngine(final String nodeName, ReplicaStore store) {
         try {
             // configuring persistence
             PersistenceConfiguration configuration = new PersistenceConfiguration(nodeName);
             configuration.setStore(store);
             configuration.addPersistentClass(Agent.class);
 
             // retrieve the persistence factory
             this.factory = configuration.getPersistenceSessionFactory();
 
         } catch (PersistenceException ex) {
             Log.e(TAG, "Error while initializing persistence in engine", ex);
         }
 
         // add a callback to populate engine when sync is ready
         DaumAuthComponent.getChangeListener().addSyncListener(new SyncListener() {
             @Override
             public void sync(SyncEvent e) {
                Log.w(TAG, "sync called in engine by listener so now synced is true");
                 synced = true;
             }
         });
 
     }
 
     public boolean check(String matricule, String password) {
         PersistenceSession session = null;
         try {
             session = factory.getSession();
             Map<Object, Agent> agents = session.getAll(Agent.class);
             for (Agent a : agents.values()) {
                 if (a.getMatricule().equals(matricule) && a.getPassword().equals(password)) {
                     return true;
                 }
             }
 
         } catch (PersistenceException e) {
             Log.e(TAG, "Error while trying to authenticate user", e);
         } finally {
             if (session != null) session.close();
         }
 
         return false;
     }
 
 
     public boolean isSynced() {
         return synced;
     }
 }
