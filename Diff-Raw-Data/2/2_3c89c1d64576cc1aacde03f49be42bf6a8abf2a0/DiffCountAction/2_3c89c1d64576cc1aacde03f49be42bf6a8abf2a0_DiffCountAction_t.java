 package jp.sf.amateras.stepcounter;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * JEg̃ANVB
  *
  * @author takanori
  */
 public class DiffCountAction implements IObjectActionDelegate {
 
 	private ISelection	selection;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void run(IAction action) {
 
 		String initialPath = null;
 		Object obj = ((IStructuredSelection) this.selection).getFirstElement();
 
 		if(obj instanceof IResource){
 			try {
 				initialPath = ((IResource) obj).getPersistentProperty(
 						new QualifiedName(StepCounterPlugin.PLUGIN_ID, "comparePath"));
 
 			} catch(CoreException ex){
 				ex.printStackTrace();
 			}
 		}
 		if(initialPath == null){
 			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 			initialPath = wsRoot.getLocation().toOSString();
 		}
 
 		String targetPath = getComparePath(action, initialPath);
 
 		if(targetPath != null){
 			try {
 				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 				window.getActivePage().showView(
						"jp.sf.amateras.stepcounter.DiffCountView");
 				IViewReference[] viewReferences = window.getActivePage().getViewReferences();
 				for (IViewReference viewReference : viewReferences) {
 					IViewPart view = viewReference.getView(false);
 					if (view instanceof DiffCountView) {
 						((DiffCountView)view).count(this.selection, targetPath);
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * ̔rΏۂ̃pX擾܂B
 	 *
 	 * @param action
 	 * @return
 	 */
 	private String getComparePath(IAction action, String initialPath) {
 		// TODO ActionIDɂāApX̎擾@ύX
 
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 
 //		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 //		String wsRootPath = wsRoot.getLocation().toOSString();
 
 		DirectoryDialog dialog = new DirectoryDialog(window.getShell());
 		dialog.setText("̔rw");
 		dialog.setMessage("̔rw肵ĂB");
 		dialog.setFilterPath(initialPath);
 
 		String comparePath = dialog.open();
 
 		return comparePath;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
 }
