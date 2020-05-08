 package textmarker.actions;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.part.FileEditorInput;
 
 import textmarker.parse.ParseXMLForMarkers;
 
 public class AddMarkerAction implements IWorkbenchWindowActionDelegate {
 	private IWorkbenchWindow window;
 	
 	/**
 	 * The constructor.
 	 */
 	public AddMarkerAction() {
 	}
 
 	/**
 	 * The action has been activated. The argument of the
 	 * method represents the 'real' action sitting
 	 * in the workbench UI.
 	 * @see IWorkbenchWindowActionDelegate#run
 	 */
 	public void run(IAction action) {
 		IEditorInput ei = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
 		IProject proj = ((FileEditorInput)ei).getFile().getProject();
 		
 		//putting in lines, adding markers and forcing close with no save might work best 
 		//this happens after the previously highlighted line because of the line numbers
 		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 
 		if(editor instanceof CompilationUnitEditor){
 			CompilationUnitEditor part = (CompilationUnitEditor)editor;
 			part.doRevertToSaved();
 		} else if(editor instanceof TextEditor){
 			TextEditor part = (TextEditor)editor;
 			part.doRevertToSaved();
 		} 
		ParseXMLForMarkers.parseXML(proj, editor, null, null);
 	}
 
 	/**
 	 * Selection in the workbench has been changed. We 
 	 * can change the state of the 'real' action here
 	 * if we want, but this can only happen after 
 	 * the delegate has been created.
 	 * @see IWorkbenchWindowActionDelegate#selectionChanged
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			Object selected = ((IStructuredSelection) selection).getFirstElement();
 			
 			if (selected instanceof IResource) {
 				IResource resource = (IResource) selected;
 				//TODO refresh?
 				//ParseXMLForMarkers.parseXML(resource.getProject());
 			}
 		} else if (selection instanceof TextSelection) {
 			IEditorInput ei = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
 			IProject proj = ((FileEditorInput)ei).getFile().getProject();
 			//TODO refresh?
 			//ParseXMLForMarkers.parseXML(proj);
 		}
 	}
 	
 
 	/**
 	 * We can use this method to dispose of any system
 	 * resources we previously allocated.
 	 * @see IWorkbenchWindowActionDelegate#dispose
 	 */
 	public void dispose() {
 	}
 
 	/**
 	 * We will cache window object in order to
 	 * be able to provide parent shell for the message dialog.
 	 * @see IWorkbenchWindowActionDelegate#init
 	 */
 	public void init(IWorkbenchWindow window) {
 		this.window = window;
 	}
 }
