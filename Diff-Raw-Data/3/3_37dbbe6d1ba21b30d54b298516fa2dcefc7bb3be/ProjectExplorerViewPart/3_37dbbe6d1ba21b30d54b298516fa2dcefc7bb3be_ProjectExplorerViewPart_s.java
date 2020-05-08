 package org.codefaces.ui.internal.views;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codefaces.core.events.WorkspaceChangeEvent;
 import org.codefaces.core.events.WorkspaceChangeListener;
 import org.codefaces.core.models.RepoFile;
 import org.codefaces.core.models.RepoFolder;
 import org.codefaces.core.models.RepoResource;
 import org.codefaces.core.models.RepoResourceType;
 import org.codefaces.core.models.Workspace;
 import org.codefaces.ui.internal.StatusManager;
 import org.codefaces.ui.internal.commands.CommandExecutor;
 import org.codefaces.ui.internal.commands.OpenFileCommandHandler;
 import org.codefaces.ui.internal.commons.RepoResourceComparator;
 import org.codefaces.ui.internal.commons.RepoFolderOpenListener;
 import org.codefaces.ui.internal.commons.RepoResourceLabelProvider;
 import org.codefaces.ui.internal.commons.RepoResourceContentProvider;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.IOpenListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.part.ViewPart;
 
 public class ProjectExplorerViewPart extends ViewPart {
 	public static final String ID = "org.codefaces.ui.view.projectExplorer";
 
 	private static final String VIEWER_CONTEXT_MENU_ID = ID + "#viewer";
 
 	private TreeViewer viewer;
 
 	private Workspace workspace;
 
 	private StatusManager statusManager;
 
 	private UpdateBaseDirWorkspaceChangeListener updateBaseDirListener = new UpdateBaseDirWorkspaceChangeListener();
 
 	private final class UpdateBaseDirWorkspaceChangeListener implements
 			WorkspaceChangeListener {
 		@Override
 		public void workspaceChanged(WorkspaceChangeEvent evt) {
 			update(evt.getBaseDirectory());
 		}
 	}
 
 	private class FileOpenListener implements IOpenListener {
 		@Override
 		public void open(OpenEvent event) {
 			IStructuredSelection selection = (IStructuredSelection) event
 					.getSelection();
 			if (selection.isEmpty()
 					|| !(selection.getFirstElement() instanceof RepoResource)) {
 				return;
 			}
 
 			RepoResource clickedRepoResource = (RepoResource) selection
 					.getFirstElement();
 			if (clickedRepoResource.getType() == RepoResourceType.FILE) {
 				Map<String, String> parameterMap = new HashMap<String, String>();
 				parameterMap.put(OpenFileCommandHandler.PARAM_MODE,
 						OpenFileCommandHandler.MODE_DIRECT_FILES);
 
 				Map<String, Object> variableMap = new HashMap<String, Object>();
 				variableMap.put(OpenFileCommandHandler.VARIABLE_FILES,
 						new RepoFile[] { (RepoFile) clickedRepoResource });
 
 				CommandExecutor.execute(OpenFileCommandHandler.ID,
 						parameterMap, variableMap);
 			}
 		}
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		createViewer(parent);
 
 		hookWorkspace();
 
 		statusManager = new StatusManager(getViewSite().getActionBars()
 				.getStatusLineManager(), getViewer());
 
 		registerContextMenu(viewer);
 	}
 
 	private void hookWorkspace() {
 		workspace = Workspace.getCurrent();
 		RepoFolder cachedBaseDir = workspace.getWorkingBaseDirectory();
 		if (cachedBaseDir != null) {
 			update(cachedBaseDir);
 		}
 		workspace.addWorkspaceChangeListener(updateBaseDirListener);
 	}
 
 	@Override
 	public void dispose() {
 		workspace.removeWorkspaceChangeListener(updateBaseDirListener);
 		super.dispose();
 	}
 
 	/**
 	 * create and register a context menu associate to the explorer tree viewer
 	 * 
 	 * @param viewer
 	 *            - the project explorer tree-viewer
 	 */
 	private void registerContextMenu(TreeViewer viewer) {
 		MenuManager contextMenuManager = new MenuManager();
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 		Menu menu = contextMenuManager.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(VIEWER_CONTEXT_MENU_ID,
 				contextMenuManager, viewer);
 		getSite().setSelectionProvider(viewer);
 	}
 
 	public StatusManager getStatusManager() {
 		return statusManager;
 	}
 
 	/**
 	 * Create and initialize the viewer
 	 */
 	private void createViewer(Composite parent) {
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
 				| SWT.BORDER);
 		viewer.setContentProvider(new RepoResourceContentProvider());
 		viewer.setLabelProvider(new RepoResourceLabelProvider());
 		viewer.setComparator(new RepoResourceComparator());
 		viewer.addOpenListener(new FileOpenListener());
 		viewer.addOpenListener(new RepoFolderOpenListener());
 	}
 
 	/**
 	 * Update the Explorer input to the given RepoFolder.
 	 * 
 	 * @param newBaseDirectory
 	 *            the new base directory
 	 */
 	public void update(RepoFolder newBaseDirectory) {
 		viewer.setInput(newBaseDirectory);
 	}
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 
 }
