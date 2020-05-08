 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.core;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jubula.app.Activator;
 import org.eclipse.jubula.app.i18n.Messages;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.utils.DatabaseStateDispatcher;
 import org.eclipse.jubula.client.core.utils.DatabaseStateEvent;
 import org.eclipse.jubula.client.core.utils.IDatabaseStateListener;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.handlers.project.AbstractSelectDatabaseHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ProjectUIBP;
 import org.eclipse.jubula.client.ui.rcp.constants.RCPCommandIDs;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.JobUtils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 
 /**
  * @author BREDEX GmbH
  * @created 23.08.2005
  */
 public class JubulaWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
     /**
      * the delay used to schedule the auto logon job
      */
     private static final int AUTO_LOGON_JOB_SCHEDULE_DELAY = 1000;
 
     /**
      * @author BREDEX GmbH
      * @created 09.04.2011
      */
     private static class ApplicationWindowTitleUpdater implements
         IProjectLoadedListener, IDatabaseStateListener, IDataChangedListener {
         /** {@inheritDoc} */
         public void reactOnDatabaseEvent(DatabaseStateEvent e) {
             updateProjectNameInTitlebar();
         }
         
         /** {@inheritDoc} */
         public void handleProjectLoaded() {
             updateProjectNameInTitlebar();
         }
         
         /** {@inheritDoc} */
         public void handleDataChanged(DataChangedEvent... events) {
             for (DataChangedEvent e : events) {
                 handleDataChanged(e.getPo(), e.getDataState());
             }
         }
         
         /** {@inheritDoc} */
         public void handleDataChanged(IPersistentObject po,
                 DataState dataState) {
             if (po instanceof IProjectPO
                     && (dataState == DataState.Renamed 
                             || dataState == DataState.Deleted)) {
                 updateProjectNameInTitlebar();
             }
         }
         
         /**
          * updates the project name shown in the titlebar
          */
         public static void updateProjectNameInTitlebar() {
             Plugin.getDisplay().asyncExec(new Runnable() {
                 public void run() {
                     StringBuilder sb = new StringBuilder(Plugin.getDefault()
                             .getRunningApplicationTitle());
 
                     Persistor persistor = Persistor.instance();
                     if (persistor != null) {
                         String user = persistor.getCurrentDBUser();
                         if (user != null && user.length() != 0) {
                             sb.append(StringConstants.SPACE)
                                 .append(StringConstants.MINUS)
                                 .append(StringConstants.SPACE).append(user);
                         }
                     }
                     IProjectPO currentProject = GeneralStorage.getInstance()
                             .getProject();
                     if (currentProject != null
                             && currentProject.getName() != null
                             && currentProject.getName().length() > 0) {
                         sb.append(StringConstants.SPACE)
                             .append(StringConstants.MINUS)
                             .append(StringConstants.SPACE)
                             .append(currentProject.getName())
                             .append(StringConstants.SPACE)
                             .append(currentProject.getMajorProjectVersion())
                             .append(StringConstants.DOT)
                             .append(currentProject.getMinorProjectVersion());
                     }
                     Plugin.getActiveWorkbenchWindowShell().setText(
                             sb.toString());
                 }
             });
         }
     }
 
     /** 
      * all basic action sets that should be hidden when running Jubula as 
      * a stand-alone client 
      */
     private static final String[] ACTION_SETS_TO_HIDE = new String [] {
         "org.eclipse.ui.actionSet.openFiles", //$NON-NLS-1$
         "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo", //$NON-NLS-1$
        "org.eclipse.search.OpenSearchDialogPage", //$NON-NLS-1$
         "org.eclipse.ui.edit.text.actionSet.navigation", //$NON-NLS-1$
         "org.eclipse.ui.edit.text.actionSet.annotationNavigation" //$NON-NLS-1$
     };
     
     /**
      * @param configurer IWorkbenchWindowConfigurer 
      */
     public JubulaWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
         super(configurer);
     }
     
     /**
      * {@inheritDoc}
      */
     public ActionBarAdvisor createActionBarAdvisor(
             IActionBarConfigurer configurer) {
         return new JubulaActionBarAdvisor(
                 configurer, getWindowConfigurer());
     }
     
     /**
      * {@inheritDoc}
      */
     public void preWindowOpen() {
         getWindowConfigurer().setTitle(Messages
                 .JubulaWorkbenchWindowAdvisorWindowTitle);
         getWindowConfigurer().setShowMenuBar(true);
         getWindowConfigurer().setShowPerspectiveBar(true);
         getWindowConfigurer().setShowCoolBar(true);
         getWindowConfigurer().setShowStatusLine(true);
         getWindowConfigurer().setShowProgressIndicator(true);
         getWindowConfigurer().setShowFastViewBars(false);
     }
     
     /**
      * {@inheritDoc}
      */
     public void postWindowOpen() {
         for (IWorkbenchWindow window 
                 : PlatformUI.getWorkbench().getWorkbenchWindows()) {
             IWorkbenchPage page = window.getActivePage();
             if (page != null) {
                 for (String actionSetToHide : ACTION_SETS_TO_HIDE) {
                     page.hideActionSet(actionSetToHide);
                 }
             }
         }
 
         AbstractUIPlugin plugin = Activator.getDefault();
         ImageRegistry imageRegistry = plugin.getImageRegistry();
         getWindowConfigurer().getWindow().getShell().setImages(
                 new Image [] {
                         imageRegistry.get(Activator.IMAGE_GIF_JB_16_16_ID),
                         imageRegistry.get(Activator.IMAGE_GIF_JB_32_32_ID),
                         imageRegistry.get(Activator.IMAGE_GIF_JB_48_48_ID),
                         imageRegistry.get(Activator.IMAGE_GIF_JB_64_64_ID),
                         imageRegistry.get(Activator.IMAGE_GIF_JB_128_128_ID),
                         imageRegistry.get(Activator.IMAGE_PNG_JB_16_16_ID),
                         imageRegistry.get(Activator.IMAGE_PNG_JB_32_32_ID),
                         imageRegistry.get(Activator.IMAGE_PNG_JB_48_48_ID),
                         imageRegistry.get(Activator.IMAGE_PNG_JB_64_64_ID),
                         imageRegistry.get(Activator.IMAGE_PNG_JB_128_128_ID)
                 });
                 
         Plugin.createStatusLineItems();
         Plugin.showStatusLine((IWorkbenchPart)null);
         addMainWindowTitleUpdater();
         checkAndPerformStartupHooks();
     }
 
     /**
      * invoke implicit startup hooks
      */
     private void checkAndPerformStartupHooks() {
         boolean performAutoDBConnect = AbstractSelectDatabaseHandler
                 .shouldAutoConnectToDB();
         boolean performAutoProjectLoad = ProjectUIBP.getInstance()
                 .shouldPerformAutoProjectLoad();
         if (performAutoDBConnect) {
             final String commandID;
             if (performAutoProjectLoad) {
                 commandID = RCPCommandIDs.OPEN_PROJECT;
             } else {
                 commandID = CommandIDs.SELECT_DATABASE_COMMAND_ID;
             }
             JobUtils.executeJob(new Job(Messages.AutoLogonJob) {
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     Plugin.getDisplay().asyncExec(new Runnable() {
                         public void run() {
                             CommandHelper.executeCommand(commandID);
                         }
                     });
                     return Status.OK_STATUS;
                 }
             }, null, AUTO_LOGON_JOB_SCHEDULE_DELAY);
         }
     }
 
     /**
      * add a permanent listener to update the main window title
      */
     protected void addMainWindowTitleUpdater() {
         ApplicationWindowTitleUpdater updater = 
             new ApplicationWindowTitleUpdater();
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addProjectLoadedListener(updater, true);
         ded.addDataChangedListener(updater, true);
         DatabaseStateDispatcher
             .addDatabaseStateListener(updater);
     }
 
     /**
      * @throws WorkbenchException 
      * {@inheritDoc}
      */
     public void postWindowRestore() throws WorkbenchException {
         super.postWindowRestore();
         Plugin.showStatusLine((IWorkbenchPart)null);
     }
 }
