 package de.fraunhofer.esk.openetcs.sysml2b.transformation.wizard;
 
 import org.eclipse.core.internal.resources.Project;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.dialogs.NewWizard;
 
 import de.fraunhofer.esk.openetcs.sysml2b.transformation.FileGenerator;
 
 
 public class TransformationWizard extends Wizard implements StringConstants {
 
 	private TransformationWizardPage page;
 	private IFile model;
 	
 	@Override
 	public boolean performFinish() {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject project = root.getProject(page.getProjectName());
 		IProgressMonitor monitor = new NullProgressMonitor();
 		
 		// Display Message Dialog of project path already exists and create project if not existing
 		if(project.exists())	{
 			final MessageDialog dg = new MessageDialog(
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 					UI_MESSAGE_TITLE,
 					null,
 					UI_MESSAGE_PROJECT_EXISTS,
 					MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
 					0
 					);
 
 			// If no return false
 			if(dg.open() == 1)
 				return false;
 		} else {
 			
 			
 			try {
 				project.create(monitor);
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		// Generate the Classical B source
 		FileGenerator generator= new FileGenerator(model, project);
		generator.generateAndWrite();
 		
 		try {
 			project.open(monitor);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		monitor.done();		
 		
 		return true;
 	}
 
 	@Override
 	public void addPages() {
 		page = new TransformationWizardPage(UI_WIZARDPAGE_NAME);
 		page.setModelName(model.getFullPath().toOSString());
 		addPage(page);
 	}
 
 	public void setModel(IFile model) {
 		this.model = model;
 	}
 
 }
