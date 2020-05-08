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
 package org.amanzi.neo.wizards;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * <p>
  * Import drive data wizard
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class TemsImportWizard extends Wizard implements IImportWizard {
 
     /** String PAGE_TITLE field */
    private static final String PAGE_TITLE = "Import Drive data File";
     /** String PAGE_DESCR field */
    private static final String PAGE_DESCR = "Import a file from the local file system into the workspace";
     private TemsImportWizardPage mainPage;
 
     @Override
     public boolean performFinish() {
         mainPage.getDialog().runLoadingJob();
         return true;
     }
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         mainPage = new TemsImportWizardPage(PAGE_TITLE, PAGE_DESCR);
     }
 
     @Override
     public void addPages() {
         super.addPages();
         addPage(mainPage);
     }
 }
