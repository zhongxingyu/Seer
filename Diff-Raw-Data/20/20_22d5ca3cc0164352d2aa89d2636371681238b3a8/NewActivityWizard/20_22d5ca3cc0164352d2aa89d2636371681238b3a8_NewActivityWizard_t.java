 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jayway.eclipse.adt.extensions.ui.wizards;
 
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
 import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 
 /**
  * Wizard class for creating a new Activity
  * @author Michael Kober
  */
 public class NewActivityWizard extends NewElementWizard {
 
     /**
      * Wizard id.
      */
     public static final String ID = "com.jayway.eclipse.adt.extensions.ui.wizard.NewActivityWizard";
 
     private NewActivityWizardPage newActivityPage = null;
 
     /**
      * Creates a new android activity project wizard.
      */
     public NewActivityWizard() {
         setWindowTitle("New Android Activity");
         setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(ImageConstants.ID,
                ImageConstants.LARGE_ACTIVITY_ICON));
         newActivityPage = new NewActivityWizardPage();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#performFinish()
      */
     @Override
     public boolean performFinish() {
     	boolean res= super.performFinish();
 		if (res) {
 			IResource resource= newActivityPage.getModifiedResource();
 			if (resource != null) {
 				// update manifest
 				Set<String> selectedActions = newActivityPage.getSelectedActions(); 
 				Set<String> selectedCategories = newActivityPage.getSelectedCategories();
 				String activityName = newActivityPage.getTypeName();
 				IJavaProject javaProject = newActivityPage.getJavaProject();
 				AndroidManifestFile manifest = new AndroidManifestFile(javaProject.getProject());
 				try {
 					manifest.update(activityName, selectedActions, selectedCategories);
 				} catch (CoreException e) {
 					Status status = new Status(IStatus.ERROR, "com.jayway.adt.extensions", e.getMessage());
 					ErrorDialog.openError(getShell(), "Error when updating manifest", e.getMessage(), status);
 				}
 				openResource((IFile) resource);
 			}
 		}
 		return res;
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.jface.wizard.Wizard#addPages()
      */
     @Override
 	public void addPages() {
         addPage(newActivityPage);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
      */
     @Override
 	 public void init(IWorkbench workbench, IStructuredSelection selection) {
     	newActivityPage.init(selection);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jface.wizard.Wizard#canFinish()
      */
     @Override
 	public boolean canFinish() {
         // only allow the user to finish if the current page is the last page.
         return super.canFinish() && getContainer().getCurrentPage() == newActivityPage;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected void finishPage(IProgressMonitor monitor)
 			throws InterruptedException, CoreException {
 		newActivityPage.createType(monitor); 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#getCreatedElement()
 	 */
 	@Override
 	public IJavaElement getCreatedElement() {
 		return newActivityPage.getCreatedType();
 	}
 }
