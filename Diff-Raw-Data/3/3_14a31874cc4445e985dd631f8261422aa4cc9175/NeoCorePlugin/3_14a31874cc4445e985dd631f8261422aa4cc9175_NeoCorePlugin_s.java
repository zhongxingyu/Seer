 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.core;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.amanzi.neo.core.database.entity.NeoDataService;
 import org.amanzi.neo.core.database.listener.IUpdateViewListener;
 import org.amanzi.neo.core.database.services.AweProjectService;
 import org.amanzi.neo.core.database.services.UpdateViewManager;
 import org.amanzi.neo.core.database.services.events.ShowPreparedViewEvent;
 import org.amanzi.neo.core.database.services.events.ShowViewEvent;
 import org.amanzi.neo.core.database.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.core.database.services.events.UpdateViewEvent;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.preferences.PreferencesInitializer;
 import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.neoclipse.view.NeoGraphViewPart;
 import org.osgi.framework.BundleContext;
 
 /**
  * Plugin class for org.amanzi.neo.core
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 
 public class NeoCorePlugin extends Plugin implements IUpdateViewListener {
 
     /*
      * Plugin's ID
      */
 
     private static final String ID = "org.amanzi.neo.core";
 
     /*
      * Plugin variable
      */
 
     static private NeoCorePlugin plugin;
 
     /*
      * Initializer for AWE-specific Neo Preferences
      */
 
     private PreferencesInitializer initializer = new PreferencesInitializer();
 
     private AweProjectService aweProjectService;
     private UpdateViewManager updateBDManager;
     private NeoDataService neoDataService;
     private final Object neoDataMonitor = new Object();
     private final List<UpdateViewEventType> eventList = Arrays.asList(UpdateViewEventType.values());
 
     private UpdateViewEvent lastExetutedEvent;
 
     /**
      * Constructor for SplashPlugin.
      */
     public NeoCorePlugin() {
         super();
         plugin = this;
     }
 
     @Override
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
         updateBDManager = new UpdateViewManager();
         updateBDManager.addListener(this);
         //TODO need solution to use log4j libraries from separate plugin but not from udig libraries
         URL url = getBundle().getEntry("/logCinfig.properties");
 //        System.out.println(url);
         URL rUrl = FileLocator.toFileURL(url);
 //        System.out.println(rUrl);
 //        System.out.println(rUrl.getPath());
 //        System.out.println(new File(rUrl.toURI()).getAbsolutePath());
         
         PropertyConfigurator.configure(rUrl);
 
 //        Logger.getLogger(this.getClass()).debug("test");
     }
 
     /*
      * (non-Javadoc)
      * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
      */
     @Override
     public void stop(BundleContext context) throws Exception {
         plugin = null;
         super.stop(context);
     }
 
     /**
      * Returns the shared instance.
      */
     public static NeoCorePlugin getDefault() {
         return plugin;
     }
 
     /**
      * Returns initializer of NeoPreferences
      * 
      * @return initializer of Neo Preferences
      */
 
     public PreferencesInitializer getInitializer() {
         return initializer;
     }
 
     /**
      * @return awe project service
      */
     public AweProjectService getProjectService() {
         if (aweProjectService == null) {
             aweProjectService = new AweProjectService();
         }
         return aweProjectService;
     }
 
     /**
      * get service
      * 
      * @return awe project service
      */
     public NeoDataService getNeoDataService() {
         if (neoDataService == null) {
             synchronized (neoDataMonitor) {
                 if (neoDataService == null) {
                     neoDataService = new NeoDataService();
                 }
             }
         }
         return neoDataService;
     }
 
     /**
      * Initialize project service for tests.
      * 
      * @param aNeo NeoService
      * @author Shcharbatsevich_A
      */
     public void initProjectService(GraphDatabaseService aNeo) {
         aweProjectService = new AweProjectService(aNeo);
     }
 
     /**
      * @return UpdateBDManager
      */
     public UpdateViewManager getUpdateViewManager() {
         return updateBDManager;
     }
 
     /**
      * Sets initializer of NeoPreferences
      * 
      * @param initializer new initializer for NeoPreferences
      */
 
     public void setInitializer(PreferencesInitializer initializer) {
         this.initializer = initializer;
     }
 
     /**
      * Print a message and information about exception to Log
      * 
      * @param message message
      * @param e exception
      */
 
     public static void error(String message, Throwable e) {
         getDefault().getLog().log(new Status(IStatus.ERROR, ID, 0, message == null ? "" : message, e)); //$NON-NLS-1$
     }
 
     public String getNeoPluginLocation() {
         try {
             return FileLocator.resolve(Platform.getBundle("org.neo4j").getEntry(".")).getFile();
         } catch (IOException e) {
             error(null, e);
             return null;
         }
     }
 
     @Override
     public void updateView(UpdateViewEvent event) {
         UpdateViewEventType type = event.getType();
         if (!eventExecuted(event)) {
             // update NeoGraphViewPart
             switch (type) {
             case DRILL_DOWN:
                 UpdateDrillDownEvent ddEvent = (UpdateDrillDownEvent)event;
                 if (!ddEvent.getSource().equals(NeoGraphViewPart.ID)) {
                     Node node = ddEvent.getNodes().get(0);
                     org.neo4j.neoclipse.Activator.getDefault().updateNeoGraphView(node);
                 }
                 break;
             case SHOW_PREPARED_VIEW:
                 ShowPreparedViewEvent spvEvent = (ShowPreparedViewEvent)event;
                 if (spvEvent.isViewNeedUpdate(NeoGraphViewPart.ID)) {
                     Node node = spvEvent.getNodes().get(0);
                     org.neo4j.neoclipse.Activator.getDefault().showNeoGraphView(node);
                 }
                 break;
             case SHOW_VIEW:
                 ShowViewEvent svEvent = (ShowViewEvent)event;
                 if (svEvent.isViewNeedUpdate(NeoGraphViewPart.ID)) {
                     org.neo4j.neoclipse.Activator.getDefault().showNeoGraphView(null);
                 }
                 break;
             default:
                 org.neo4j.neoclipse.Activator.getDefault().updateNeoGraphView();
             }
             lastExetutedEvent = event;
         }
     }
 
     private boolean eventExecuted(UpdateViewEvent event) {
         if (lastExetutedEvent == null) {
             return false;
         }
         if (lastExetutedEvent.equals(event)) {
             return true;
         }
         Node last = getNodeFromEvent(lastExetutedEvent);
         if (last != null) {
             Node current = getNodeFromEvent(event);
             if (current == null) {
                 return false;
             }
             return last.equals(current);
         }
         return false;
     }
 
     private Node getNodeFromEvent(UpdateViewEvent event) {
         if (event instanceof UpdateDrillDownEvent) {
             UpdateDrillDownEvent ddEvent = (UpdateDrillDownEvent)event;
             if (!ddEvent.getSource().equals(NeoGraphViewPart.ID)) {
                 return ddEvent.getNodes().get(0);
             }
         }
         if (event instanceof ShowPreparedViewEvent) {
             ShowPreparedViewEvent spvEvent = (ShowPreparedViewEvent)event;
             if (spvEvent.isViewNeedUpdate(NeoGraphViewPart.ID)) {
                 return spvEvent.getNodes().get(0);
             }
         }
         return null;
     }
 
     @Override
     public Collection<UpdateViewEventType> getType() {
         return eventList;
     }
 
 }
