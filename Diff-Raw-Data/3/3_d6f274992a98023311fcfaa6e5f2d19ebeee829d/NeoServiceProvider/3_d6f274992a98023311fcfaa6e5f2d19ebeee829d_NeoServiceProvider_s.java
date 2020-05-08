 package org.amanzi.neo.core.service;
 
 import java.util.ArrayList;
 
 import org.amanzi.neo.core.service.listener.INeoServiceProviderListener;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.neoclipse.Activator;
 import org.neo4j.neoclipse.neo.NeoServiceEvent;
 import org.neo4j.neoclipse.neo.NeoServiceEventListener;
 import org.neo4j.neoclipse.neo.NeoServiceManager;
 import org.neo4j.neoclipse.preference.NeoDecoratorPreferences;
 import org.neo4j.neoclipse.preference.NeoPreferences;
 
 /**
  * Provider that give access to NeoService
  * 
  * @author Lagutko_N
  * @since 1.1.0
  */
 
 public class NeoServiceProvider implements IPropertyChangeListener{
     
     /*
      * Instance of NeoServiceProvider
      */
     private static NeoServiceProvider provider;
     
     /*
      * NeoService
      */
     private NeoService neoService;
     
     /*
      * NeoServiceManager
      */
     private NeoServiceManager neoManager;
     
     /*
      * Listener for NeoServiceManager events
      */
     private NeoServiceEventListener defaultListener = new DefaultServiceListener();
     
     /*
      * Listeners of NeoServiceProvider
      */
     private ArrayList<INeoServiceProviderListener> listeners = new ArrayList<INeoServiceProviderListener>(0);
     
     /*
      * Location of Neo-database
      */
     private String databaseLocation;
 
     private Display display;
     
     /**
      * Creates an instance of NeoServiceProvider
      *
      * @return instance of NeoServiceProvider
      */
     
     public static NeoServiceProvider getProvider() {
         if (provider == null) {
             provider = new NeoServiceProvider();
         }
         
         return provider;
     }
     
     /**
      * Protected constructor
      */
     
     protected NeoServiceProvider() {
         try {
             this.display = PlatformUI.getWorkbench().getDisplay();
         } catch (RuntimeException e) {
             //We are probably running unit tests, log and error and continue
             System.err.println("Failed to get display: "+e);
         }
     }
     
     /**
      * Returns NeoService
      *
      * @return
      */
     
     public NeoService getService() {
         init();
         
         return neoService;
     }
     
     /**
      * Initializes NeoService and NeoServiceManager
      */
     
     private void init() {
         if (neoService == null) {
             neoService = Activator.getDefault().getNeoServiceSafely();
         }
         if (neoManager == null) {
             neoManager = Activator.getDefault().getNeoServiceManager();
             neoManager.addServiceEventListener(defaultListener);
         }
     }
     
     /**
      * Returns current location of Neo-database 
      *
      * @return
      */
     
     public String getDefaultDatabaseLocation() {
         if (databaseLocation == null) {
             IPreferenceStore store = Activator.getDefault().getPreferenceStore();
             databaseLocation = store.getString(NeoPreferences.DATABASE_RESOURCE_URI);
             if ((databaseLocation == null) || (databaseLocation.trim().isEmpty())) {
                 databaseLocation = store.getString(NeoPreferences.DATABASE_LOCATION);
             }
         }
         
         return databaseLocation;
     }
     
     /**
      * Add listener to Provider 
      *
      * @param listener listener for NeoServiceProvider
      */
     
     public void addServiceProviderListener(INeoServiceProviderListener listener) {
         listeners.add(listener);       
         
     }
     
     /**
      * Removes listener from Provider 
      *
      * @param listener listener of NeoServiceProvider
      */
     
     public void removeServiceProviderListener(INeoServiceProviderListener listener) {
         listeners.remove(listener);
     }
     
     /**
      * Shutdown NeoServiceManager
      */
     
     public void shutdown() {        
         if (neoManager != null) {
             neoManager.commit();
             neoManager.removeServiceEventListener(defaultListener);            
             neoManager = null;            
         }
     }
     
     /**
      * Commits changes
      */
     public void commit() {
         //Lagutko, 12.10.2009, also check is Workbech closing?
         boolean currentThread = PlatformUI.getWorkbench().isClosing() || 
                                 (display == null) || 
                                 Thread.currentThread().equals(display.getThread());
         if (currentThread) {
             neoManager.commit();
         } else {
             display.syncExec(new Runnable() {
 
                 @Override
                 public void run() {
                     neoManager.commit();
                 }
             });
         }
     }
     
     /**
      * Rollback changes
      */
     public void rollback() {
         //Lagutko, 12.10.2009, also check is Workbech closing?
         boolean currentThread = PlatformUI.getWorkbench().isClosing() || 
                                 (display == null) || 
                                 Thread.currentThread().equals(display.getThread());
         if (currentThread) {
             neoManager.rollback();
         } else {
             display.syncExec(new Runnable() {
 
                 @Override
                 public void run() {
                     neoManager.rollback();
                 }
             });
         }
     }
     
     /**
      * Listener that listens for event of NeoServiceManager and provides them
      * to listeners of NeoServiceProvides
      * 
      * @author Lagutko_N
      * @since 1.1.0
      */
     
     private class DefaultServiceListener implements NeoServiceEventListener {
 
         public void serviceChanged(NeoServiceEvent event) {
             //Lagutko, 12.10.2009, copy list of listeners to local variable
             ArrayList<INeoServiceProviderListener> copiedListener = (ArrayList<INeoServiceProviderListener>)listeners.clone();
             for (INeoServiceProviderListener listener : copiedListener) {
                 Object source = event.getSource();
                 switch (event.getStatus()) {
                case STOPPED:
                    copiedListener.remove(listener);
                     listener.onNeoStop(source);
                     shutdown();
                     break;
                 case STARTED:
                     listener.onNeoStart(source);
                     break;
                 case COMMIT:
                     listener.onNeoCommit(source);
                     break;
                 case ROLLBACK:
                     listener.onNeoRollback(source);
                     break;
                 }                    
             }
         }
         
     }
 
     /**
      * Listens for changing properties
      */
 
     public void propertyChange(PropertyChangeEvent event) {
         //if location of icons was changes than we must re-initialize UserIcons
         if (event.getProperty().equals(NeoDecoratorPreferences.DATABASE_LOCATION) ||
             event.getProperty().equals(NeoPreferences.DATABASE_RESOURCE_URI) ||
             event.getProperty().equals(NeoPreferences.DATABASE_LOCATION)) {
             //if location of Database was changed than restart Neo
             Activator.getDefault().restartNeo();
             databaseLocation = null;
         }        
     }
     
     /**
      * Stops Neo Service 
      * 
      */
     
     public void stopNeo() {
     	if (neoManager != null) {
     	    neoManager.commit();
     		neoManager.stopNeoService();
     	}
     	else if (neoService != null) {
     		neoService.shutdown();
     	}
     	neoManager = null;
     	neoService = null;
     }
 
 }
