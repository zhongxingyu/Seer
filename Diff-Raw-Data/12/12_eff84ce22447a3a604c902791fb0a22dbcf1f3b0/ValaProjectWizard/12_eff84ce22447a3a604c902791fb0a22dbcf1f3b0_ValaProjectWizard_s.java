 /* ValaProjectWizard.java
  *
  * Copyright (C) 2008  Johann Prieur <johann.prieur@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.wizard.project;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SubProgressMonitor;
 
 import valable.nature.ValaNature;
 
 
 
 public class ValaProjectWizard extends Wizard implements INewWizard {
 	
 	public static final String ID = "valable.wizard.project.ValaProject";
 	
 	private IWorkbench workbench;
 	private IStructuredSelection selection;
 
 	private ValaProjectWizardPage projectPage;
 
 	public ValaProjectWizard() {
 		super();
 		setNeedsProgressMonitor(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.Wizard#addPages()
 	 */
 	@Override
 	public void addPages() {
 		projectPage = new ValaProjectWizardPage();
 		this.addPage(projectPage);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	@Override
 	public boolean performFinish() {
 		IProject project = createProject();
 		if(project == null) {
 			return false;
 		}
 		
 		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
 		try {
 			workbench.showPerspective("valable.perspective.ValaPerspective", activeWindow);
 		} catch (WorkbenchException e) {}
 		
 		return true;
 	}
 
 	private IProject createProject() {
 		final IProject project = projectPage.getProjectHandle();
 		IPath projectPath = projectPage.getProjectPath();
        /*IPath defaultPath = Platform.getLocation();
 
         if (defaultPath.equals(projectPath)){
         	projectPath = null;
        }*/
 
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         final IProjectDescription description = workspace.newProjectDescription(project.getName());
         description.setLocation(projectPath);
 		
 		// Instanciate a project creation operation
 		WorkspaceModifyOperation projectCreation = new WorkspaceModifyOperation() {
 			
 			@Override
 			protected void execute(IProgressMonitor monitor)
 					throws CoreException, InvocationTargetException,
 					InterruptedException {
 				createProject(project, description, monitor);
 			}
 			
 		};
 		
 		// Run the project creation operation
 		try {
 			getContainer().run(true, true, projectCreation);
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 			return null;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return project;
 	}
 	
 	private void createProject(IProject project, IProjectDescription description, 
 			IProgressMonitor monitor) throws CoreException {
 		try {
 			monitor.beginTask("valaProjectCreation", 3);
 			
 			project.create(description, new SubProgressMonitor(monitor, 1));
 			project.open(IResource.BACKGROUND_REFRESH, 
 					new SubProgressMonitor(monitor, 1));
 			
 			// TODO : create generated files
 			
 			ValaNature.addNature(project, new SubProgressMonitor(monitor, 1));
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 	}
 
 }
