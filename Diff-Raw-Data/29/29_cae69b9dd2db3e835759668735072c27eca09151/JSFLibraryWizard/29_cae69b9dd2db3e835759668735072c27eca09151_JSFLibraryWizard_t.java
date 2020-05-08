 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.ui.internal.classpath;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.jst.jsf.core.internal.jsflibraryconfig.JSFLibraryRegistryUtil;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryFactory;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFVersion;
 import org.eclipse.jst.jsf.ui.internal.JSFUiPlugin;
 import org.eclipse.jst.jsf.ui.internal.Messages;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * Dialog for creating or editing a JSF Library or Implementation.
  * <br>
  * If the selection passed in init is not null then the item will be edit mode.
  * 
  * @author Gerry Kessler - Oracle
  */
 public class JSFLibraryWizard extends Wizard implements INewWizard {
 
 	/**
 	 * Implementation libaries to be included
 	 */
	public final static int IMPLS = 1;
 	/**
 	 * Non-Implementation libaries to be included
 	 */
	public final static int NONIMPLS = 2;
 	
 	private JSFLibraryEditControl jsfLibraryEditControl;
 	
 	private boolean isNew = false;
 	private boolean modified = false;
 
 	private JSFLibrary curLibrary;
 	private JSFLibrary workingCopyLibrary;
 
 	private JSFLibraryWizardPage page;
 
 	private boolean _impls;	
 	private boolean _nonimpls;
 	
     private List<IProject>        _projectsWithV1JSFLibraries = new ArrayList<IProject>();
 	
 	/**
 	 * Constructor
 	 * see IMPLS
 	 * see NONIMPLS
 	 * @param libTypes 
 	 */
 	public JSFLibraryWizard(int libTypes) {
 		super();
 		if ((libTypes & IMPLS) == IMPLS)
 			_impls = true;
 
 		if ((libTypes & NONIMPLS) == NONIMPLS)
 			_nonimpls = true;
 	}
 
 	
 	/**
 	 * Constructor.   List will include all JSF Libraries.
 	 */
 	public JSFLibraryWizard() {
 		super();
 		_impls = true;
 		_nonimpls = true;
 	}
 
 	private boolean isImplsOnly(){
 		if (_impls && ! _nonimpls)
 			return true;
 		return false;		
 	}
 	
 	private boolean isNonImplsOnly(){
 		if (_nonimpls && ! _impls)
 			return true;
 		return false;		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) 
 	{
 	    initV1LibrariesList();
 	    
 		if (selection != null
 				&& selection.getFirstElement() instanceof JSFLibrary) {
 			curLibrary = (JSFLibrary) selection.getFirstElement();
 			workingCopyLibrary = curLibrary.getWorkingCopy();
 		} else {
 			isNew = true;
 			workingCopyLibrary = JSFLibraryRegistryFactory.eINSTANCE.createJSFLibrary();
 		}
 		if (isImplsOnly()) {
 			setWindowTitle(Messages.JSFLibraryWizard_CreateImplementation);
 		} else {
 			setWindowTitle(isNew ? Messages.JSFLibraryWizard_CreateJSFLibrary : Messages.JSFLibraryWizard_EditJSFLibrary);
 		}
 	}
 
 	private void initV1LibrariesList()
 	{
         final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
         
         // loop through the workspace and look for projects that are still using the V1 way
         // of doing JSF libraries
         for (int i = 0; i < projects.length; i++)
         {
             final IProject project = projects[i];
             if (JSFLibraryRegistryUtil.doesProjectHaveV1JSFLibraries(project))
             {
                 _projectsWithV1JSFLibraries.add(project);
             }
         }
     }
 
 	/**
 	 * Updates the JSF Library instance with the values from the working copy and 
 	 * persists the registry.
 	 * 
 	 * If editing a library reference, referencing java models will be updated.
 	 *  
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	public boolean performFinish() 
 	{
 	    // on init, if we detected any projects with old library
 	    // refs, we need to warn the user
 	    if (!isNew &&  // can ignore new libraries, since old projects can't possibly have dependencies on them
 	            _projectsWithV1JSFLibraries.size() > 0)
 	    {
 	        String  projectNames = ""; //$NON-NLS-1$
 	        for (Iterator<IProject> it = _projectsWithV1JSFLibraries.iterator(); it.hasNext();)
 	        {
 	            IProject project = it.next();
 	            projectNames += project.getName() + ","; //$NON-NLS-1$
 	        }
 	        // trim trailing comma
 	        if (projectNames.length() > 0)
 	        {
 	            projectNames = projectNames.substring(0, projectNames.length()-1);
 	        }
 	        
 	        final String messageText = MessageFormat.format(Messages.JSFLibraryWizard_V1JSFLibrary_DialogMessage,
 	                                        new Object[] {projectNames});
 	        MessageDialogWithToggle dialog = 
 	            WarningMessageDialogWithToggle.openOkCancelConfirm
 	                (getShell(), Messages.JSFLibraryWizard_V1JSFLibrary_DialogTitle, messageText, Messages.JSFLibraryWizard_JSFLibraryWizard_DontShowThisAgain_CheckBoxLabel, false, null, null);
 	        if (dialog.getReturnCode() != IDialogConstants.OK_ID)
 	        {
 	            // abort if the user doesn't really want to commit this change
 	            return false;
 	        }
 
 	        // if user accepted and asked not be warned again, clear the 
 	        // the project properties.
 	        if (dialog.getToggleState())
             {
 	            JSFLibraryRegistryUtil.removeV1JSFLibraryProperty(_projectsWithV1JSFLibraries);
             }
 	    }
 	    
 		final String name = jsfLibraryEditControl.getJSFLibraryName();
 		final boolean isDeployed = jsfLibraryEditControl.getIsDeployed();
 		final boolean isImplementation = jsfLibraryEditControl.getIsImplementation();
 		final JSFVersion version = jsfLibraryEditControl.getJSFVersion();
 		
 		workingCopyLibrary.setName(name);
 		workingCopyLibrary.setDeployed(isDeployed);
 		workingCopyLibrary.setImplementation(isImplementation);
 		workingCopyLibrary.setJSFVersion(version);
 		
 		final String originalID = curLibrary != null ? curLibrary.getID() :workingCopyLibrary.getID();
 		
 		if (isNew){
 			JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry().addJSFLibrary(workingCopyLibrary);
 		}
 		else {
 			curLibrary.updateValues(workingCopyLibrary);
 			try {
				JSFLibraryRegistryUtil.rebindClasspathContainerEntries(originalID, workingCopyLibrary.getID(), null);
 			} catch (JavaModelException e) {
 				JSFUiPlugin.log(IStatus.ERROR, "Exception while updating JSF Library containers", e); //$NON-NLS-1$
 			}
 		}
 		JSFLibraryRegistryUtil.getInstance().saveJSFLibraryRegistry();
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.Wizard#addPages()
 	 */
 	public void addPages() {
 		page = new JSFLibraryWizardPage(Messages.JSFLibraryWizard_JSFLibrary);
 		super.addPage(page);
 		page.setWizard(this);
 	}
 
 	/**
 	 * @return the JSFLibrary being modified by this wizard
 	 */
 	public JSFLibrary getJSFLibrary() {
 		return workingCopyLibrary;
 	}
 
 	private class JSFLibraryWizardPage extends WizardPage {
 
 		/**
 		 * @param pageName
 		 */
 		protected JSFLibraryWizardPage(String pageName) {
 			super(pageName);
			setDescription(isImplsOnly() 
			        ? Messages.JSFLibraryWizard_IMPLS_ONLY_DESC
			        : Messages.JSFLibraryWizard_DESCRIPTION);
 			setTitle(Messages.JSFLibraryWizard_JSFLibrary);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
 		 */
 		public boolean isPageComplete() {
 			if (modified == false) {
 				return false;
 			}
 			return super.isPageComplete();
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 		 */
 		public void createControl(Composite parent) {
 			initializeDialogUnits(parent);
 
 			jsfLibraryEditControl = new JSFLibraryEditControl(workingCopyLibrary, parent);
 			jsfLibraryEditControl.setImplOnly(isImplsOnly());
 			jsfLibraryEditControl.setNonImplOnly(isNonImplsOnly());
 			jsfLibraryEditControl.setLayout(new GridLayout(2, false));
 			jsfLibraryEditControl.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 			jsfLibraryEditControl.addValidationListener(new JSFLibraryValidationListener(){
 				public void notifyValidation(JSFLibraryValidationEvent e) {
 					setErrorMessage(e.getMessage());
 					modified = true;
 					setPageComplete(getErrorMessage()==null);
 				}				
 			});
 
 			setControl(jsfLibraryEditControl);
 			setPageComplete(false);
 		}
 
 	}
 
 }
