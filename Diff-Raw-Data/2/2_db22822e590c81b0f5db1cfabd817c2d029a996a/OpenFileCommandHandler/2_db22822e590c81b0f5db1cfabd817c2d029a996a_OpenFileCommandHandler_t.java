 package org.codefaces.ui.internal.commands;
 
 import java.util.Iterator;
 
 import org.codefaces.core.models.RepoFile;
 import org.codefaces.ui.internal.CodeFacesUIActivator;
 import org.codefaces.ui.internal.editors.CodeExplorer;
 import org.codefaces.ui.internal.editors.RepoFileInput;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.IEvaluationContext;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 public class OpenFileCommandHandler extends AbstractHandler {
	public static final String ID = "org.codefaces.ui.internal.commands.openFileCommand";
 
 	public static final String PARAM_MODE = "org.codefaces.ui.internal.commands.parameters.openFileCommand.mode";
 
 	public static final String MODE_SELECTION_FROM_VIEW = "selectionFromView";
 	public static final String MODE_DIRECT_FILES = "directFiles";
 
 	public static final String PARAM_VIEW_ID = "org.codefaces.ui.internal.commands.parameters.openFileCommand.viewId";
 	public static final String VARIABLE_FILES = "org.codefaces.ui.internal.commands.parameters.openFileCommand.files";
 
 	/**
 	 * Open files in the code explorer.
 	 * 
 	 * This command supports 2 modes, either by providing a view id so that the
 	 * command can get the selected files, or by providing an array of files in
 	 * the application context of the execution event.
 	 * 
 	 * To use the MODE_SELECTION_FROM_VIEW mode, the parameter PARAM_MODE should
 	 * be set to MODE_SELECTION_FROM_VIEW. An additional parameter PARAM_VIEW_ID
 	 * should also be provided.
 	 * 
 	 * To use the MODE_DIRECT_FILES mode, the parameter PARAM_MODE should be set
 	 * to MODE_DIRECT_FILES. A RepoFile array should be added with name
 	 * VARIABLE_FILES in the variables of the application context in the
 	 * execution event.
 	 */
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		String mode = event.getParameter(PARAM_MODE);
 		if (mode != null) {
 			if (mode.equals(MODE_SELECTION_FROM_VIEW)) {
 				openFilesFromViewSelection(event);
 			}
 			if (mode.equals(MODE_DIRECT_FILES)) {
 				openFilesDirectly(event);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method obtains a RepoFile array from the event's application context
 	 * and open them in the code explorer
 	 * 
 	 * @param event
 	 *            - an event that contains a VARAIBLE_FILE in type of RepoFile[]
 	 *            in its application context
 	 */
 	private void openFilesDirectly(ExecutionEvent event) {
 		IEvaluationContext context = (IEvaluationContext) event
 				.getApplicationContext();
 		if (context != null) {
 			Object obj = context.getVariable(VARIABLE_FILES);
 			if (obj instanceof RepoFile[]) {
 				RepoFile[] files = (RepoFile[]) obj;
 				for (RepoFile file : files) {
 					openFile(file);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method obtains the selection from the provided view ID. If the
 	 * selection is RepoFile(s), it tells the code explorer to open the file(s)
 	 * 
 	 * @param event
 	 */
 	private void openFilesFromViewSelection(ExecutionEvent event) {
 		String viewId = event.getParameter(PARAM_VIEW_ID);
 		// The cmd is executed by providing a view ID
 		if (viewId != null) {
 			ISelectionService selectionService = PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow().getSelectionService();
 			ISelection selection = selectionService.getSelection(viewId);
 			if (selection instanceof IStructuredSelection) {
 				Iterator<?> elements = ((IStructuredSelection) selection)
 						.iterator();
 				while (elements.hasNext()) {
 					Object element = elements.next();
 					if (element instanceof RepoFile) {
 						openFile((RepoFile) element);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Open the given file in the code explorer
 	 * 
 	 * @param repoFile
 	 *            - the file
 	 */
 	private void openFile(RepoFile repoFile) {
 		IWorkbenchPage activePage = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage();
 		try {
 			activePage.openEditor(new RepoFileInput(repoFile), CodeExplorer.ID);
 		} catch (PartInitException e) {
 			IStatus status = new Status(Status.ERROR,
 					CodeFacesUIActivator.PLUGIN_ID,
 					"Errors occurs when showing view " + CodeExplorer.ID, e);
 			CodeFacesUIActivator.getDefault().getLog().log(status);
 		}
 	}
 
 }
