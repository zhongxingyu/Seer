 package objective.actions;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorActionDelegate;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.internal.editors.text.EditorsPlugin;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 @SuppressWarnings("restriction")
 public class CreateTestCounterpartAction implements IObjectActionDelegate, IEditorActionDelegate {
 	private IWorkbenchWindow fWindow;
 	private IJavaElement sourceClass;
 	
 	@Override
 	public void run(IAction action) {
 		createOrOpenTestCounterpart();
 	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			this.sourceClass = getFirstElement((IStructuredSelection) selection);
 		}
 	}
 
 	@Override
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		storeWindow(targetPart);
 	}
 
 	@Override
 	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
 		storeWindow(targetEditor);
 		sourceClass = EditorUtility.getEditorInputJavaElement(targetEditor, false);
 	}
 
 	private void storeWindow(IWorkbenchPart targetPart) {
		if (targetPart == null)
			return;
		
 		IWorkbenchPartSite site = targetPart.getSite();
 		fWindow = site.getWorkbenchWindow();
 	}
 	
 	private void createOrOpenTestCounterpart() {
 			TestCounterpartCreator c = new TestCounterpartCreator();
 			try {
 				openCreatedResource(
 						c.createOrRetrieve(sourceClass), 
 						c.getCaretPositionForNewFile());
 			}catch(TestCounterpartCreator.NullElementNotAllowedException e){
 				displayElementNotAllowedMessage();
 			}
 	}
 
 	private void displayElementNotAllowedMessage() {
 		MessageDialog.openWarning(fWindow.getShell(), 
 				"Can't create test",
 				"You need to select a class file to create a test");
 	}
 	
 	private IJavaElement getFirstElement(IStructuredSelection selection) {
 		if (selection == null)
 			return null;
 		
 		IJavaElement elem = null;
 		if (selection != null && !selection.isEmpty()) {
 			Object selectedElement = selection.getFirstElement();
 			if (selectedElement instanceof IAdaptable) {
 				IAdaptable adaptable = (IAdaptable) selectedElement;
 
 				elem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
 			}
 		}
 		return elem;
 	}
 
 	private void openCreatedResource(IResource resource, int position) {
 		IWorkbenchPage activePage= fWindow.getActivePage();
 		try {
 			IDE.openEditor(activePage, (IFile) resource, true);
 			setCaretPositionIfNewFile(position, activePage);
 		} catch (PartInitException e) {
 			EditorsPlugin.log(e);
 		}
 	}
 
 	private void setCaretPositionIfNewFile(int position,
 			IWorkbenchPage activePage) {
 		if (position > -1) {
 			ISelection selection = new TextSelection(position,0);
 			((ITextEditor)activePage.getActiveEditor()).getSelectionProvider().setSelection(
 					selection);
 		}
 	}
 }
