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
 package org.eclipse.jubula.client.ui.rcp.handlers.project;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
 import org.eclipse.jubula.client.core.businessprocess.progress.ProgressMonitorTracker;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.IUsedToolkitPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.core.persistence.TestResultPM;
 import org.eclipse.jubula.client.core.utils.StringHelper;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.handlers.project.AbstractProjectHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ProjectUIBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ToolkitBP;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.dialogs.NagDialog;
 import org.eclipse.jubula.client.ui.rcp.dialogs.ProjectDialog;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.ToolkitConstants;
 import org.eclipse.jubula.tools.exception.GDConfigXmlException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author BREDEX GmbH
  * @created 18.04.2005
  */
 public class OpenProjectHandler extends AbstractProjectHandler {
     /**
      * @author BREDEX GmbH
      * @created Jan 2, 2008
      */
     public static class OpenProjectOperation implements IRunnableWithProgress {
         /**
          * <code>TESTRESULT_DETAIL_JOB_CLEANUP_DELAY</code> is 10 minutes
          */
         private static final int TESTRESULT_DETAIL_JOB_CLEANUP_DELAY = 600000;
 
         /** The project to open */
         private IProjectPO m_selectedProject;
 
         /**
          * @param selectedProject
          *            The project that is being opened.
          */
         public OpenProjectOperation(IProjectPO selectedProject) {
             m_selectedProject = selectedProject;
         }
 
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) throws InterruptedException {
 
             Utils.clearClient();
 
             int totalWork = getTotalWork();
             ProgressMonitorTracker.getInstance().setProgressMonitor(monitor);
 
             monitor.beginTask(NLS.bind(
                     Messages.OpenProjectOperationOpeningProject,
                     new Object[] { m_selectedProject.getName(),
                             m_selectedProject.getMajorProjectVersion(),
                             m_selectedProject.getMinorProjectVersion() }),
                     totalWork);
             DataEventDispatcher ded = DataEventDispatcher.getInstance();
             try {
                 if (!checkProjectToolkits(m_selectedProject)) {
                     throw new InterruptedException();
                 }
                 checkToolkitAvailable(m_selectedProject.getToolkit());
                 try {
                     NodePM.getInstance().setUseCache(true);
                     load(m_selectedProject, monitor);
                     if (monitor.isCanceled()) {
                         throw new InterruptedException();
                     }
 
                 } catch (GDConfigXmlException ce) {
                     handleCapDataNotFound(ce);
                 }
                 ded.fireProjectLoadedListener(monitor);
                 // re-init the string helper in case of a toolkit change during
                 // load
                 StringHelper.getInstance();
                 startCleanTestresultsJob(GeneralStorage.getInstance()
                         .getProject());
                 if (monitor.isCanceled()) {
                     throw new InterruptedException();
                 }
             } catch (final JBException e) {
                 errorOccured();
                 Display.getDefault().asyncExec(new Runnable() {
                     public void run() {
                         ErrorHandlingUtil.createMessageDialog(e, null, null);
                     }
                 });
             } finally {
                 ProgressMonitorTracker.getInstance().setProgressMonitor(null);
                 NodePM.getInstance().setUseCache(false);
                 monitor.done();
             }
         }
 
         /**
          * set persisted values for cleaning testresults in database
          * 
          * @param project
          *            the project to clean the test results for
          */
         public static void startCleanTestresultsJob(final IProjectPO project) {
             final int cleanupInterval = project.getTestResultCleanupInterval();
             final String projGUID = String.valueOf(project.getGuid());
             final int projMajVer = project.getMajorProjectVersion().intValue();
             final int projMinVer = project.getMinorProjectVersion().intValue();
             if (cleanupInterval > 0) {
                 Job job = new Job(NLS.bind(
                         Messages.UIJobCleaningTestResultFromDB,
                         project.getName())) {
                     public IStatus run(IProgressMonitor monitor) {
                         TestResultPM.cleanTestresultDetails(cleanupInterval,
                                 projGUID, projMajVer, projMinVer);
                         monitor.done();
                         return Status.OK_STATUS;
                     }
                 };
                 job.schedule(TESTRESULT_DETAIL_JOB_CLEANUP_DELAY);
             }
         }
 
         /**
          * Checks whether the given toolkit is available. If the given toolkit
          * is not available, a warning is displayed.
          * 
          * @param toolkitId
          *            The id of the toolkit to check.
          */
         private void checkToolkitAvailable(String toolkitId) {
             try {
                 if (!ComponentBuilder.getInstance().getLevelToolkitIds()
                         .contains(toolkitId)
                         && ToolkitConstants.LEVEL_TOOLKIT
                                 .equals(ToolkitSupportBP
                                         .getToolkitLevel(toolkitId))) {
                     ErrorHandlingUtil.createMessageDialog(MessageIDs.
                             W_PROJECT_TOOLKIT_NOT_AVAILABLE);
                 }
             } catch (ToolkitPluginException e) {
                 ErrorHandlingUtil.createMessageDialog(MessageIDs.
                         W_PROJECT_TOOLKIT_NOT_AVAILABLE);
             }
         }
 
         /**
          * @return the amount of work required to complete the operation. This
          *         value can then be used when creating a progress monitor.
          */
         private int getTotalWork() {
             int totalWork = 0;
             EntityManager masterSession = GeneralStorage.getInstance()
                     .getMasterSession();
             long selectedProjectId = m_selectedProject.getId();
 
             // (node=1)
             totalWork += NodePM.getNumNodes(selectedProjectId, masterSession);
 
             // (tdMan=1)
             totalWork += NodePM.getNumTestDataManagers(selectedProjectId,
                     masterSession);
 
             // (execTC=1 [each corresponding specTC needs to be fetched])
             totalWork += NodePM.getNumExecTestCases(selectedProjectId,
                     masterSession);
 
             for (IReusedProjectPO reused : m_selectedProject.
                     getUsedProjects()) {
                 try {
                     IProjectPO reusedProject = ProjectPM
                             .loadReusedProject(reused);
                     if (reusedProject != null) {
                         long reusedId = reusedProject.getId();
 
                         // (node=1)
                         totalWork += NodePM
                                 .getNumNodes(reusedId, masterSession);
 
                         // (tdMan=1)
                         totalWork += NodePM.getNumTestDataManagers(reusedId,
                                 masterSession);
 
                         // (execTC=1 [each corresponding specTC needs to be
                         // fetched])
                         totalWork += NodePM.getNumExecTestCases(reusedId,
                                 masterSession);
                     }
                 } catch (JBException e) {
                     // Do nothing
                 }
             }
             return totalWork;
         }
 
         /**
          * 
          * @param proj
          *            the project to open.
          * @param monitor
          *            The progress monitor for this operation.
          * @throws InterruptedException
          *             if the operation was canceled.
          */
         private void load(IProjectPO proj, IProgressMonitor monitor)
             throws InterruptedException {
             if (proj == null) {
                 Plugin.stopLongRunning();
                 showErrorDialog(Messages.OpenProjectActionInternalError);
                 return;
             }
 
             try {
                 Plugin.getDisplay().syncExec(new Runnable() {
                     public void run() {
                         Plugin.closeAllOpenedJubulaEditors(false);
                     }
                 });
                 IProjectPO prevProj = GeneralStorage.getInstance().getProject();
                 ProjectPM.loadProjectInROSession(proj);
                 try {
                     final IProjectPO project = GeneralStorage.getInstance()
                             .getProject();
                     try {
                         UsedToolkitBP.getInstance().refreshToolkitInfo(project);
                     } catch (PMException e1) {
                         PMExceptionHandler
                                 .handlePMExceptionForMasterSession(e1);
                     } catch (ProjectDeletedException e1) {
                         PMExceptionHandler.handleGDProjectDeletedException();
                     }
                     if (monitor.isCanceled()) {
                         throw new InterruptedException();
                     }
                 } catch (GDConfigXmlException ce) {
                     if (prevProj == null) {
                         GeneralStorage.getInstance().reset();
                     } else {
                         ProjectPM.loadProjectInROSession(prevProj);
                     }
 
                     throw ce;
                 }
             } catch (PMReadException e) {
                 showErrorDialog(Messages.ErrorMessageCantReadProject);
             } catch (OperationCanceledException oce) {
                 Utils.clearClient();
             } finally {
                 Plugin.stopLongRunning();
             }
         }
 
         /**
          * Handles an exception thrown while opening a project.
          */
         public void handleOperationException() {
             // Clear all current project data
             IProjectPO clearedProject = GeneralStorage.getInstance()
                     .getProject();
             if (clearedProject != null) {
                 Utils.clearClient();
                 GeneralStorage.getInstance().setProject(null);
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         clearedProject, DataState.Deleted, UpdateState.all);
             }
         }
 
         /**
          * Checks that the toolkits and toolkit versions are correct enough to
          * be able to load the project.
          * 
          * @param proj
          *            the project for which to check the toolkits
          * @return <code>true</code> if project can be loaded. Otherwise
          *         <code>false</code>.
          */
         private boolean checkProjectToolkits(IProjectPO proj)
             throws PMException {
             final Set<IUsedToolkitPO> usedToolkits = UsedToolkitBP
                     .getInstance().readUsedToolkitsFromDB(proj);
             return ToolkitBP.getInstance().checkXMLVersion(usedToolkits);
         }
 
         /**
          * Create an appropriate error dialog.
          * 
          * @param ce
          *            The exception that prevented the loading of project.
          */
         private void handleCapDataNotFound(GDConfigXmlException ce) {
             ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.E_LOAD_PROJECT_CONFIG_CONFLICT, null,
                     new String[] { ce.getMessage() });
         }
 
         /**
          * Opens an error dialog.
          * 
          * @param message
          *            the message to show in the dialog.
          */
         private void showErrorDialog(String message) {
             ErrorHandlingUtil.createMessageDialog(new JBException(message,
                     MessageIDs.E_UNEXPECTED_EXCEPTION), null,
                     new String[] { message });
         }
 
         /**
          * {@inheritDoc}
          */
         protected void errorOccured() {
             Plugin.stopLongRunning();
         }
 
     }
     
     /**
      * Checks all available projects
      * 
      * @return list of all projects
      */
     private List<IProjectPO> checkAllAvailableProjects() {
         List<IProjectPO> projList = null;
         try {
             projList = ProjectPM.findAllProjects();
             if (projList.isEmpty()) {
                 Display.getDefault().asyncExec(new Runnable() {
                     public void run() {
                         ErrorHandlingUtil.createMessageDialog(
                             MessageIDs.I_NO_PROJECT_IN_DB);
                     }
                 });
                 Plugin.stopLongRunning();
             } else {
                 SortedMap<String, List<String>> projNameToVersionMap = 
                         new TreeMap<String, List<String>>();
                 for (IProjectPO proj : projList) {
                     String projName = proj.getName();
                     String projVersion = proj.getVersionString();
                     if (!projNameToVersionMap.containsKey(projName)) {
                         projNameToVersionMap.put(projName,
                                 new ArrayList<String>());
                     }
                     projNameToVersionMap.get(projName).add(projVersion);
                 }
             }
         } catch (final JBException e) {
             Display.getDefault().asyncExec(new Runnable() {
                 public void run() {
                     ErrorHandlingUtil.createMessageDialog(e, null, null);
                 }
             });
         }
         return projList;
     }
 
     /**
      * opens the project open dialog
      * @param projList the project list
      * @return the open project dialog
      */
     public ProjectDialog openProjectOpenDialog(List<IProjectPO> projList) {
         ProjectDialog dialog = null;
         if (GeneralStorage.getInstance().getProject() != null
                 && Plugin.getDefault().anyDirtyStar()
                 && !Plugin.getDefault().showSaveEditorDialog()) {
 
             Plugin.stopLongRunning();
         } else {
             dialog = openProjectSelectionDialog(projList);
             if (dialog.getReturnCode() == Window.CANCEL) {
                 Plugin.stopLongRunning();
             }
         }
         return dialog;
     }
     
     /**
      * open the selected or the default project 
      * 
      * @param projectData the project data
      * @param projList list which contains all available projects
      */
     public void loadProject(ProjectDialog.ProjectData projectData,
             List<IProjectPO> projList) {
         IProjectPO projectToOpen = null;
         for (IProjectPO project : projList) {
             if (project.getGuid().equals(projectData.getGUID())
                     && project.getVersionString().equals(
                             projectData.getVersionString())) {
                 projectToOpen = project;
                 break;
             }
         }
         if (projectToOpen == null) {
             openProjectOpenDialog(projList);
         } else {
             OpenProjectOperation openOperation = new OpenProjectOperation(
                     projectToOpen);
             try {
                 PlatformUI.getWorkbench().getProgressService()
                         .busyCursorWhile(openOperation);
                 DataEventDispatcher.getInstance().fireProjectOpenedListener();
                 checkAndNagForMissingProjects();
             } catch (InvocationTargetException ite) {
                 openOperation.handleOperationException();
             } catch (InterruptedException ie) {
                 openOperation.handleOperationException();
             }
         }
     }
 
     /**
      * checks for missing reused projects after project loading
      */
     private void checkAndNagForMissingProjects() {
         List<String> missingProjects = new LinkedList<String>();
         final IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project != null) {
             final Set<IReusedProjectPO> usedProjects = project
                     .getUsedProjects();
             if (usedProjects != null) {
                 for (IReusedProjectPO rProjects : usedProjects) {
                     if (!ProjectPM.doesProjectVersionExist(
                             rProjects.getProjectGuid(),
                             rProjects.getMajorNumber(),
                             rProjects.getMinorNumber())) {
                         missingProjects.add(rProjects.getProjectGuid());
                     }
                 }
             }
         }
         if (!missingProjects.isEmpty()) {
             NagDialog.runNagDialog(getActiveShell(),
                     "InfoNagger.ImportAllRequiredProjects", //$NON-NLS-1$
                     ContextHelpIds.IMPORT_ALL_REQUIRED_PROJECTS);
         }
     }
 
     /**
      * @param projList
      *            the list of projects in the database
      * @return the dialog
      */
     private ProjectDialog openProjectSelectionDialog(
             List<IProjectPO> projList) {
         final ProjectDialog dialog = new ProjectDialog(getActiveShell(),
                 projList, Messages.OpenProjectActionMessage,
                 Messages.OpenProjectActionTitle,
                 IconConstants.OPEN_PROJECT_DIALOG_IMAGE,
                 Messages.OpenProjectActionCaption, false);
         // set up help for dialog, with link
         dialog.setHelpAvailable(true);
         dialog.create();
         DialogUtils.setWidgetNameForModalDialog(dialog);
         Plugin.getHelpSystem().setHelp(dialog.getShell(),
                 ContextHelpIds.OPEN_PROJECT);
         Display.getDefault().syncExec(new Runnable() {
             public void run() {
                 Plugin.startLongRunning(Messages.
                         OpenProjectActionLoadProjectWaitMessage);
                 dialog.open();
             }
         });
         return dialog;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Object executeImpl(ExecutionEvent event) {
         ProjectDialog.ProjectData project = null;
         boolean cancelPressed = false;
         ProjectDialog dialog = null;
         List<IProjectPO> projList = checkAllAvailableProjects();
 
         if (ProjectUIBP.getInstance().shouldPerformAutoProjectLoad()) {
             project = ProjectUIBP.getMostRecentProjectData();
         } else {
             dialog = openProjectOpenDialog(projList);
             if (dialog.getReturnCode() == Window.CANCEL) {
                 cancelPressed = true;
             } else {
                 project = dialog.getSelection();
             }
         }
        if (!cancelPressed) {
             loadProject(project, projList);
         }
         return null;
     }
 }
