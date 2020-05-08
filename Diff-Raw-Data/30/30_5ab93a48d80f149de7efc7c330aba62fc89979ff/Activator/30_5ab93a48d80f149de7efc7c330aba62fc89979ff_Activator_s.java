 /*
  * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
  * (http://neotechnology.com) under one or more contributor license agreements.
  * See the NOTICE file distributed with this work for additional information
  * regarding copyright ownership. Neo Technology licenses this file to you under
  * the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
  * applicable law or agreed to in writing, software distributed under the
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  * OF ANY KIND, either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  */
 package org.neo4j.neoclipse;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.kernel.impl.transaction.TransactionFailureException;
 import org.neo4j.neoclipse.graphdb.GraphDbServiceManager;
 import org.neo4j.neoclipse.preference.NeoPreferenceHelper;
 import org.neo4j.neoclipse.preference.Preferences;
 import org.neo4j.neoclipse.view.NeoGraphViewPart;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle.
  */
 public class Activator extends AbstractUIPlugin
 {
     /**
      * The plug-in ID.
      */
     public static final String PLUGIN_ID = "org.neo4j.neoclipse";
     /**
      * The graphdb manager.
      */
     protected GraphDbServiceManager graphDbManager;
     /**
      * The shared instance.
      */
     private static Activator PLUGIN;
     
     /**
      * The helper to keep some default values for AWE
      */
     private static NeoPreferenceHelper helper=null;
 
     /**
      * Starts up the plug-in and initializes the neo service.
      */
     @Override
     public void start( final BundleContext context ) throws Exception
     {
         super.start( context );
         PLUGIN = this;
         graphDbManager = new GraphDbServiceManager();
     }
 
     /**
      * Stops the plug-in and shuts down the neo service.
      */
     @Override
     public void stop( final BundleContext context ) throws Exception
     {
         graphDbManager.stopGraphDbService();
         PLUGIN = null;
         super.stop( context );
     }
 
     /**
      * Returns the shared instance.
      * 
      * @return the shared instance
      */
     public static Activator getDefault()
     {
         return PLUGIN;
     }
 
     /**
      * Returns the service manager.
      */
     public GraphDbServiceManager getGraphDbServiceManager()
     {
         return graphDbManager;
     }
 
     public GraphDatabaseService getGraphDbService() throws Exception
     {
         return graphDbManager.getGraphDbService();
     }
 
     /**
      * Get the current GraphDatabaseService. Returns <code>null</code> on
      * failure, after showing appropriate error messages.
      * 
      * @return current neo service
      */
     public GraphDatabaseService getGraphDbServiceSafely()
     {
         GraphDbServiceManager sm = Activator.getDefault().getGraphDbServiceManager();
         if ( sm == null )
         {
             MessageDialog.openError( null, "Error",
                     "The Neo4j service manager is not available." );
             return null;
         }
         GraphDatabaseService ns = null;
         try
         {
             ns = sm.getGraphDbService();
        }catch (TransactionFailureException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            String problemMessage = "Unknown problem with the database. ";
            if (cause instanceof InvocationTargetException) {                
                 if (((InvocationTargetException)cause).getTargetException() instanceof IllegalStateException) {
                    problemMessage = "Database by requested location '"
                        +getPreferenceStore().getString( Preferences.DATABASE_LOCATION )+"' already uses by other program. ";
                 }
             }
             showProblemMessage(problemMessage);
        }
        catch ( Exception rte )
        {
            rte.printStackTrace();
            showProblemMessage("Unknown problem with the database. ");
            try
            {
                 ns = sm.getGraphDbService();
            }
            catch ( Exception rte2 )
            {
                 // just continue
                 rte2.printStackTrace();
             }
         }
         return ns;
     }
 
     private void showProblemMessage(String message) {
         MessageDialog.openInformation( null, "Database problem",
                 message );
     }
 
     /**
      * Get the reference node.
      * 
      * @return the reference node
      */
     public Node getReferenceNode()
     {
         GraphDatabaseService ns = getGraphDbServiceSafely();
         if ( ns == null )
         {
             return null;
         }
         return ns.getReferenceNode();
     }
     
     /**
      * Restart the Neo service from a new location.
      */
     public void restartNeo()
     {
         graphDbManager.stopGraphDbService();
         // start the service using new location
         getGraphDbServiceSafely();
     }
     
     public static NeoPreferenceHelper getHelper() {
         return helper;
     }
 
     public void setHelper(NeoPreferenceHelper helper) {
         Activator.helper = helper;
     }
 
     /**
      * Show the Neo4j preference page.
      * 
      * @param filtered only show Neo4j properties when true
      * @return
      */
     public int showPreferenceDialog( final boolean filtered )
     {
         PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn( null,
                 "org.neo4j.neoclipse.preference.NeoPreferencePage",
                 ( filtered ? new String[] {} : null ), null );
         if ( pref != null )
         {
             return pref.open();
         }
         return 1;
     }
 
     /**
      * Show the Neo4j Decorator preference page.
      * 
      * @param filtered only show Neo4j properties when true
      * @return
      */
     public int showDecoratorPreferenceDialog( final boolean filtered )
     {
         PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn( null,
                 "org.neo4j.neoclipse.preference.NeoDecoratorPreferencePage",
                 ( filtered ? new String[] {} : null ), null );
         if ( pref != null )
         {
             return pref.open();
         }
         return 1;
     }
     
     /**
      *Updates NeoGraphView
      */
     public void updateNeoGraphView() {
         Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
             @Override
             public void run() {
                 IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(NeoGraphViewPart.ID);
                 if (view == null) {
                     return;
                 }
                 boolean partVisible = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().isPartVisible(view);
                 if(!partVisible){
                     return;
                 }
                 NeoGraphViewPart viewGraph = (NeoGraphViewPart)view;
                 viewGraph.refresh();
             }
         });
 
     }
     /**
      *Updates NeoGraphView
      */
     public void updateNeoGraphView(final Node aNode) {
         Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
             @Override
             public void run() {
                 
                 IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(NeoGraphViewPart.ID);
                 if (view == null) {
                     return;
                 }
                 boolean partVisible = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().isPartVisible(view);
                 if(!partVisible){
                     return;
                 }
                 NeoGraphViewPart viewGraph = (NeoGraphViewPart)view;
                 viewGraph.showNodeOnEvent(aNode);
             }
         });
 
     }
     
     /**
      *Updates NeoGraphView
      */
     public void showNeoGraphView(final Node aNode) {
         Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
             @Override
             public void run() {
                 
                 IViewPart view = null;
                 try {
                     view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(NeoGraphViewPart.ID);
                 } catch (PartInitException e) {
                    e.printStackTrace();
                 }
                 if (view == null) {
                     return;
                 }
                 NeoGraphViewPart viewGraph = (NeoGraphViewPart)view;
                 if (aNode != null) {
                     viewGraph.showNodeOnEvent(aNode);
                 }else{
                     viewGraph.refresh();
                 }
             }
         });
 
     }
 
 }
