 package de.fraunhofer.esk.openetcs.sysml2b.popup.actions;
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.IAction;
// import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 import de.fraunhofer.esk.openetcs.sysml2b.FileGenerator;
 
 public class GenerateAction implements IObjectActionDelegate {
 
	@SuppressWarnings("unused")
 	private Shell shell;
 	private ISelection selection;
 	
 	/**
 	 * Constructor for Action1.
 	 */
 	public GenerateAction() {
 		super();
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		shell = targetPart.getSite().getShell();
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection sel = (IStructuredSelection) selection;
 			IFile file = (IFile) sel.getFirstElement();
 			
 			try {
 				runGenerator(file);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 		action.setEnabled(!this.selection.isEmpty());
 	}
 
 	public void runGenerator(final IFile file) throws CoreException, IOException {
 		Job job = new Job("Classical B Generator") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 
 				FileGenerator generator= new FileGenerator(file);
 				generator.generateAndWrite();
 
 				return Status.OK_STATUS;
 			}
 		};
 
 		job.setUser(true);
 		job.schedule();
 	}	
 }
