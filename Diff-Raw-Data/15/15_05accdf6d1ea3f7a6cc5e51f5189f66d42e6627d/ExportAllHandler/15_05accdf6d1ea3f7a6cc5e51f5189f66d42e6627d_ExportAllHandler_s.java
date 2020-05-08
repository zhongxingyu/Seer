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
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.ui.handlers.project.AbstractProjectHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ExportAllBP;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 25, 2007
  */
 public class ExportAllHandler extends AbstractProjectHandler {
     
     /** the logger */
     private static Logger log = LoggerFactory.getLogger(ExportAllHandler.class);
 
     /**
      * @author BREDEX GmbH
      * @created Jul 25, 2007
      */
     private static class ExportOperation implements IRunnableWithProgress {
         
         /** name of the directory for exporting */
         private String m_dirName;
 
         /** dialog that was used to select export directory */
         private DirectoryDialog m_dirDialog;
         
         /**
          * Constructor
          * 
          * @param dirName The name of the directory to which projects will
          *                be exported.
          * @param dirDialog The dialog that was used to select the directory
          */
         public ExportOperation(String dirName, DirectoryDialog dirDialog) {
             m_dirName = dirName;
             m_dirDialog = dirDialog;
         }
         
         /**
          * 
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) {
 
             if (m_dirName != null) {
                 // Export all projects, logging to console as we do so
                 ExportAllBP.getInstance().showStartingExport();
                 Persistor hib = Persistor.instance();
                 EntityManager exportSession = null;
                 boolean nodeCacheIsUsed = NodePM.getInstance().isUseCache();
                 if (!nodeCacheIsUsed) {
                     NodePM.getInstance().setUseCache(true);
                 }
                 try {
                     exportSession = hib.openSession();
                     List<IProjectPO> allProjects = 
                         ProjectPM.findAllProjects(exportSession);
                     
                     // export all projects from the project list
                     ExportAllBP.getInstance().exportProjectList(
                             allProjects, m_dirName,
                             exportSession, monitor, false, null);
 
                     Utils.storeLastDirPath(m_dirDialog.getFilterPath());
                     ExportAllBP.getInstance().showFinishedExport();
                 } catch (JBException gde) {
                     log.error(Messages.ExportAborted, gde);
                     ExportAllBP.getInstance().showAbortExport(gde);
                 } catch (InterruptedException ie) {
                     ExportAllBP.getInstance().showCancelExport();
                 } finally {
                     if (!nodeCacheIsUsed) {
                         NodePM.getInstance().setUseCache(false);
                     }
                     hib.dropSession(exportSession);
                     Plugin.stopLongRunning();
                     monitor.done();
                 }
             }
         }
     }
 
     /**
      * 
      */
     private void showExportDialog() {
         final DirectoryDialog dirDialog = 
             new DirectoryDialog(getActiveShell(), SWT.APPLICATION_MODAL);
         dirDialog.setText(Messages.ActionBuilderExportAll);
         dirDialog.setFilterPath(Utils.getLastDirPath());
         boolean done = false;
         String tempDirName = null;
         while (!done) {
             tempDirName = dirDialog.open();
             Plugin.startLongRunning(
                     Messages.ExportFileActionWaitWhileExporting);
             if (tempDirName == null) {
                 done = true;
                 Plugin.stopLongRunning();
             } else {
                 File dir = new File(tempDirName);
                 if (dir.list().length > 0) {
                     MessageBox mb = new MessageBox(dirDialog.getParent(), 
                         SWT.ICON_WARNING | SWT.OK);
                     mb.setMessage(NLS.bind(
                         Messages.ExportAllActionDirectoryNotEmpty, 
                         tempDirName));
                     mb.open();
                 } else {
                     done = true;
                 }
             }
             dirDialog.setFilterPath(tempDirName);
         }
         
         
         if (tempDirName != null 
                 && tempDirName.charAt(tempDirName.length() - 1) != '/') {
             tempDirName += "/"; //$NON-NLS-1$
         }
         final String dirName = tempDirName;
         
         if (dirName != null) {
             ExportOperation op = new ExportOperation(dirName, dirDialog);
             try {
                 PlatformUI.getWorkbench().getProgressService()
                     .busyCursorWhile(op);
             } catch (InvocationTargetException ite) {
                 // Exception occurred during operation
                 log.error(ite.getLocalizedMessage(), ite.getCause());
             } catch (InterruptedException ie) {
                 // Operation was canceled. This is already handled by the 
                 // operation. Do nothing.
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Object executeImpl(ExecutionEvent event) {
        if (GeneralStorage.getInstance().getProject() != null
                && Plugin.getDefault().saveAllDirtyEditors()) {
             showExportDialog();
         }
         return null;
     }
 }
