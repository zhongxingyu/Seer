 package edu.pdx.svl.coDoc.cdc.actions;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 
 //import edu.pdx.svl.coDoc.refexp.editorcontextmenu.editors.SelectionAndCursor;
 
 import edu.pdx.svl.coDoc.cdc.editor.CDCEditor;
 import edu.pdx.svl.coDoc.cdc.preferences.PreferencesView;
 
 
 
 public class Open implements IObjectActionDelegate {
 
 	private Shell shell;
 	private ISelection selection;
 	
 	/**
 	 * Constructor for Action1.
 	 */
 	public Open() {
 		super();
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		shell = targetPart.getSite().getShell();
 		selectionChanged(action, targetPart.getSite().getPage().getSelection());
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		String projname = null;
 		IPath codepath = null;
 		IPath specpath = null;
 	    if(selection instanceof IStructuredSelection) {
 	        for(Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
 	            Object element = it.next();
 	            IFile file = null;
 	            if (element instanceof IFile) {
 					IWorkspace workspace = ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot workspaceroot = workspace.getRoot();
 					IPath workspacerootpath = workspaceroot.getLocation();
 
 	                file = (IFile) element;
 	                if(projname==null) {
 	                	projname = file.getProject().getName();
 	                } else if(!file.getProject().getName().equals(projname)) {
 	                	break;
 	                }
 	                if(file.getFileExtension().equals("pdf")) {
 	                	specpath = file.getRawLocation();
 	                	if(specpath.toString().startsWith("PARENT-")) {
 	                		specpath = file.getFullPath();
 	                		String temppath = specpath.toString();
 	                		specpath = new Path(temppath.substring(1));
 	                	} else {
		                	//specpath = specpath.makeRelativeTo(workspacerootpath);
		                	specpath = file.getFullPath();
 	                	}
 	                } else {
 	                	codepath = file.getRawLocation();
 	                	if(codepath.toString().startsWith("PARENT-")) {
 	                		codepath = file.getFullPath();
 	                	} else {
		                	//codepath = codepath.makeRelativeTo(workspacerootpath);	                		
	                		codepath = file.getFullPath();
 	                	}
 	                }
 	            }
 	        }
 	    }
 	    if(codepath==null || specpath==null) {
 	    	MessageDialog.openError(null, "Error",  "\n  Select a code and a spec in the same project!");
 	    } else {
 			CDCEditor.open(projname, codepath, specpath);	    	
 	    }
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 	}
 
 }
