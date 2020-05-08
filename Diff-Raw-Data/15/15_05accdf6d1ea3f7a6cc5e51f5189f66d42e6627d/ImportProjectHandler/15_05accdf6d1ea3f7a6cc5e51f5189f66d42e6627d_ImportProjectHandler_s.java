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
 
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.handlers.project.AbstractProjectHandler;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ImportFileBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ImportFileBP.IProjectImportInfoProvider;
 import org.eclipse.jubula.client.ui.rcp.dialogs.ImportProjectDialog;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * @author BREDEX GmbH
  * @created 08.11.2004
  */
 public class ImportProjectHandler extends AbstractProjectHandler {
     /**
      * {@inheritDoc}
      */
     public Object executeImpl(ExecutionEvent event) {
        if (GeneralStorage.getInstance().getProject() != null
                && Plugin.getDefault().saveAllDirtyEditors()) {
             showImportDialog();
         }
         Plugin.stopLongRunning();
         return null;
     }
 
     /**
      * brings up the ImportDiaog
      */
     void showImportDialog() {
         ImportProjectDialog importProjectWizard = new ImportProjectDialog();
         WizardDialog dialog = new WizardDialog(getActiveShell(),
                 importProjectWizard) {
             protected void createButtonsForButtonBar(Composite parent) {
                 super.createButtonsForButtonBar(parent);
                 Button finishButton = getButton(IDialogConstants.FINISH_ID);
                 finishButton.setText(IDialogConstants.OK_LABEL);
             }
         };
         importProjectWizard.setWindowTitle(
                 Messages.ImportProjectDialogTitle);
         dialog.setHelpAvailable(true);
 
         int val = dialog.open();
         if (val == Window.OK) {
             importProjects(importProjectWizard.getImportInfoProvider());
         }
     }
 
     /**
      * Performs an import using the information provided by the argument.
      * 
      * @param importInfo
      *            Provides information relevant to the import.
      */
     public void importProjects(IProjectImportInfoProvider importInfo) {
         List<URL> fileURLs = importInfo.getFileURLs();
         boolean openProject = importInfo.getIsOpenProject();
         ImportFileBP.getInstance().importProject(0, fileURLs, openProject);
     }
 }
