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
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.businessprocess.progress.ProgressMonitorTracker;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.TestresultState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.core.persistence.TestResultSummaryPM;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.handlers.project.AbstractProjectHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.dialogs.ProjectDialog;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.PlatformUI;
 
 
 /**
  * @author BREDEX GmbH
  * @created 06.07.2005
  */
 public class DeleteProjectHandler extends AbstractProjectHandler {
 
     /** number of Persistence (JPA / EclipseLink) event types with progress listeners */
     // Event types:
     // postRemove
     private static final int NUM_HBM_PROGRESS_EVENT_TYPES = 1;
 
     /**
      * Operation for deleting a project.
      * 
      * @author BREDEX GmbH
      * @created Jan 7, 2008
      */
     private class DeleteProjectOperation 
         implements IRunnableWithProgress {
 
         /** the project to delete */
         private IProjectPO m_project;
         
         /** whether the currently opened project is the project to delete */
         private boolean m_deleteCurrentProject;
         
         /** true, if test result summary of project should not be deleted */
         private boolean m_keepTestresultSummary = false;
         
         /**
          * Constructor
          * 
          * @param project The project to delete.
          * @param deleteCurrentProject <code>true</code> if the project to 
          *                             delete is the currently opened project.
          *                             Otherwise, <code>false</code>.
          * @param keepTestresultSummary true, if test result summary should not be deleted
          */
         public DeleteProjectOperation(IProjectPO project, 
             boolean deleteCurrentProject, boolean keepTestresultSummary) {
 
             m_project = project;
             m_deleteCurrentProject = deleteCurrentProject;
             m_keepTestresultSummary = keepTestresultSummary;
         }
         
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) throws InterruptedException {
             monitor.beginTask(NLS.bind(Messages.DeleteProjectActionDeleting,
                     new Object[] {m_project.getName(),
                                   m_project.getMajorProjectVersion(),
                                   m_project.getMinorProjectVersion()}),  
                     getTotalWork());
             try {
                 boolean isRefreshRequired = false;
                 if (!m_deleteCurrentProject) {
                     isRefreshRequired = isRefreshRequired(m_project.getGuid(),
                         m_project.getMajorProjectVersion(), 
                         m_project.getMinorProjectVersion());
                 }
                 if (m_deleteCurrentProject) {
                     Plugin.getDisplay().syncExec(new Runnable() {
                         public void run() {
                             Plugin.closeAllOpenedJubulaEditors(false);
                         }
                     });
                 }
                 
                 // Register Persistence (JPA / EclipseLink) progress listeners
                 ProgressMonitorTracker.getInstance().setProgressMonitor(
                         monitor);
                 ProjectPM.deleteProject(m_project, m_deleteCurrentProject);
                 final String jobName = Messages.UIJobDeletingTestResultDetails;
                 Job job = new Job(jobName) {
                     public IStatus run(IProgressMonitor mon) {
                         mon.beginTask(jobName, IProgressMonitor.UNKNOWN);
                         if (m_keepTestresultSummary) {
                             TestResultSummaryPM.deleteTestrunsByProject(
                                     m_project.getGuid(),
                                     m_project.getMajorProjectVersion(),
                                     m_project.getMinorProjectVersion(), true);
                         } else {
                             TestResultSummaryPM.deleteTestrunsByProject(
                                     m_project.getGuid(),
                                     m_project.getMajorProjectVersion(),
                                     m_project.getMinorProjectVersion(), false);
                         }
                         mon.done();
                         DataEventDispatcher.getInstance()
                             .fireTestresultChanged(TestresultState.Refresh);
                         return Status.OK_STATUS;
                     }
                 };
                job.schedule();
                 if (m_deleteCurrentProject) {
                     Utils.clearClient();
                     GeneralStorage.getInstance().setProject(null);
                     DataEventDispatcher.getInstance()
                         .fireDataChangedListener(m_project, DataState.Deleted,
                             UpdateState.all);
                 } else if (isRefreshRequired) {
                     GeneralStorage.getInstance().reloadMasterSession(
                             new NullProgressMonitor());
                 }
             } catch (PMException e) {
                 PMExceptionHandler.handlePMExceptionForMasterSession(e);
                 return;
             } catch (ProjectDeletedException e) {
                 PMExceptionHandler.handleGDProjectDeletedException();
                 return;
             } catch (JBException e) {
                 ErrorHandlingUtil.createMessageDialog(e, null, null);
             } finally {
                 // Remove JPA progress listeners
                 ProgressMonitorTracker.getInstance().setProgressMonitor(null);
                 monitor.done();
                 Plugin.stopLongRunning();
             }
         }
         
         /**
          * 
          * @return the total units of work required for this operation.
          */
         private int getTotalWork() {
             int totalWork = 0;
             
             // (project_node=1)
             totalWork++;
             EntityManager getNumNodesSession = 
                 Persistor.instance().openSession();
             totalWork += 
                 NodePM.getNumNodes(m_project.getId(), getNumNodesSession);
             Persistor.instance().dropSessionWithoutLockRelease(
                 getNumNodesSession);
             
             totalWork *= NUM_HBM_PROGRESS_EVENT_TYPES;
             
             return totalWork;
         }
     }
 
     /**
      * Opens a dialog to select a project to delete.
      */
     void selectProject() {
         boolean deleteCurrentProject = false;
         List<IProjectPO> projList;
         try {
             projList = ProjectPM.findAllProjects();
             if (projList.isEmpty()) {
                 ErrorHandlingUtil.createMessageDialog(
                         MessageIDs.I_NO_PROJECT_IN_DB);
                 return;
             }
             ProjectDialog dialog = 
                 getComboSelectionDialog(projList);
             if (dialog.getReturnCode() == Window.CANCEL) {
                 return;
             }
             IProjectPO project = dialog.getSelection();
             if (project == null) {
                 Plugin.stopLongRunning();
                 return;
             }
             boolean keepTestresults = dialog.keepTestresultSummary();
             Integer questionID = null;
             Object[] param = null;
             IProjectPO actProj = GeneralStorage.getInstance().getProject();
             if (actProj != null && actProj.getId().equals(project.getId())) {
                 questionID = MessageIDs.Q_DELETE_ACTUAL_PROJECT;
                 deleteCurrentProject = true;
             } else {
                 questionID = MessageIDs.Q_DELETE_PROJECT;
                 param = new Object[]{project.getName()};
             }
             Plugin.startLongRunning(
                    Messages.DeleteProjectActionWaitWhileDeleting);
             Dialog qDialog = ErrorHandlingUtil.createMessageDialog(
                     questionID, param, null);
             if (qDialog.getReturnCode() == 0) {
                 try {
                     PlatformUI.getWorkbench().getProgressService()
                         .busyCursorWhile(new DeleteProjectOperation(
                             project, deleteCurrentProject, keepTestresults));
                 } catch (InvocationTargetException e) {
                     // Exception occurred during the operation.
                     // The exception was already handled by the operation.
                     // Do nothing.
                 } catch (InterruptedException e) {
                     // Operation was canceled.
                     // Do nothing.
                 }
             }
         } catch (JBException e) {
             ErrorHandlingUtil.createMessageDialog(e, null, null);
         } finally {
             Plugin.stopLongRunning();
         }
     }
 
     /**
      * Returns <code>true</code> if deletion of the project with the given 
      * attributes will require a refresh of the current project in order to 
      * see changes in the reused project set.
      * 
      * @param deletedGuid The GUID of the deleted project.
      * @param deletedMajor The major version number of the deleted project.
      * @param deletedMinor The minor version number of the deleted project.
      * @return <code>true</code> if the deleted GUID and major and minor version
      *         numbers are present in the current project's set of reused
      *         projects. Otherwise <code>false</code>.
      */
     private boolean isRefreshRequired(String deletedGuid, Integer deletedMajor, 
         Integer deletedMinor) {
         
         IProjectPO currentProject = GeneralStorage.getInstance().getProject();
         
         if (currentProject == null) {
             return false;
         }
         
         for (IReusedProjectPO reused : currentProject.getUsedProjects()) {
             
             String guid = reused.getProjectGuid();
             Integer majorVersion = reused.getMajorNumber();
             Integer minorVersion = reused.getMinorNumber();
             if (deletedGuid.equals(guid) 
                 && deletedMajor.equals(majorVersion) 
                 && deletedMinor.equals(minorVersion)) {
                     
                 return true;
             }
         }
         
         return false;
     }
     
     /**
      * @param projList
      *            The input list.
      * @return The dialog when closed.
      */
     private ProjectDialog getComboSelectionDialog(
         List <IProjectPO> projList) {
         ProjectDialog dialog = new ProjectDialog(getActiveShell(),
             projList,
             Messages.DeleteProjectActionMessage,
             Messages.OpenProjectActionTitle,
             IconConstants.DELETE_PROJECT_DIALOG_IMAGE, 
             Messages.DeleteProjectActionCaption, true);
         // set up help for the dialog
         dialog.setHelpAvailable(true);
         dialog.create();
         DialogUtils.setWidgetNameForModalDialog(dialog);
         Plugin.getHelpSystem().setHelp(dialog.getShell(),
             ContextHelpIds.DELETE_PROJECT);
         dialog.open();
         return dialog;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object executeImpl(ExecutionEvent event) {
         selectProject();
         return null;
     }
 }
