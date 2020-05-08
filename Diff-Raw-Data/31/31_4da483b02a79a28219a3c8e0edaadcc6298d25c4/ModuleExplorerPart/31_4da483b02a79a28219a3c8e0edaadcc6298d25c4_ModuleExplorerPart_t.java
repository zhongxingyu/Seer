 package nl.cwi.sen.metastudio.moduleview;
 
 import nl.cwi.sen.metastudio.MetastudioConnection;
 import nl.cwi.sen.metastudio.PopupMenu;
 import nl.cwi.sen.metastudio.UserInterface;
 import nl.cwi.sen.metastudio.model.Directory;
 import nl.cwi.sen.metastudio.model.Module;
 
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.part.ViewPart;
 
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermList;
 
 public class ModuleExplorerPart
 	extends ViewPart
 	implements IMenuListener, ISelectionListener {
 	private TreeViewer fViewer;
 	private Menu fContextMenu;
 	private ModuleExplorerContentProvider fContentProvider;
 	private ModuleExplorerLabelProvider fLabelProvider;
 	static private Shell shell;
 
 	private PopupMenu popupMenu = new PopupMenu();
 
 	static private Directory root;
 
 	public ModuleExplorerPart() {
 	}
 
 	public void createPartControl(Composite parent) {
 		fViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
 		fViewer.setUseHashlookup(true);
 		//fViewer.setComparer();
 
 		setProviders();
 
 		addListeners();
 		//MetastudioPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
 
 		//createMenus() for following lines
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(this);
 		fContextMenu = menuMgr.createContextMenu(fViewer.getTree());
 		fViewer.getTree().setMenu(fContextMenu);
 
 		// Register viewer with site. This must be done before making the actions.
 		IWorkbenchPartSite site = getSite();
 		site.registerContextMenu(menuMgr, fViewer);
 		site.setSelectionProvider(fViewer);
 		//site.getPage().addPartListener(fPartListener);
 		getViewSite().getPage().addSelectionListener(this);
 		//createActions();
 
 		fViewer.setInput(getInput());
 
 		shell = getSite().getShell();
 	}
 
 	private Directory getInput() {
 		if (!(root instanceof Directory)) {
 			root = new Directory();
 		}
 		return root;
 	}
 
 	private void setProviders() {
 		fContentProvider = new ModuleExplorerContentProvider();
 		fViewer.setContentProvider(fContentProvider);
 
 		fLabelProvider = new ModuleExplorerLabelProvider();
 		fViewer.setLabelProvider(fLabelProvider);
 	}
 
 	private void addListeners() {
 		ModuleExplorerMouseListener mouseListener =
 			new ModuleExplorerMouseListener();
 		fViewer.getControl().addMouseListener(mouseListener);
 	}
 
 	public void setFocus() {
 	}
 
 	public void menuAboutToShow(IMenuManager manager) {
 		MetastudioConnection connection = new MetastudioConnection();
 		ATermList actions = popupMenu.getMenu();
 
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 
 		while (!actions.isEmpty()) {
 			ATerm action = actions.getFirst();
 			ATermList actionList =
 				(ATermList) ((ATermAppl) action).getArgument(0);
 			ATermAppl actionNamePrefix = (ATermAppl) (actionList.getFirst());
 
 			actions = actions.getNext();
 
 			if (actionList.getLength() == 1) {
 				manager.add(
 					new ModuleExplorerAction(actionNamePrefix.getName(), popupMenu.getActionType(), action, fViewer));
 			} else {
 				ATermList actionRunner = actions;
 				IMenuManager nextLevel =
 					new MenuManager(actionNamePrefix.getName());
 				ATerm apifyMe =
 					connection.getFactory().make(
 						"menu(<term>)",
 						actionList.getNext());
 				ATermList subMenu = connection.getFactory().makeList(apifyMe);
 
 				// collect a list of buttons that are in the same 'menuNamePrefix'
 				for (;
 					!actionRunner.isEmpty();
 					actionRunner = actionRunner.getNext()) {
 					ATerm cur = actionRunner.getFirst();
 					ATermList curList =
 						(ATermList) ((ATermAppl) cur).getArgument(0);
 					ATerm menuNamePrefix = curList.getFirst();
 
 					if (actionNamePrefix.isEqual(menuNamePrefix)) {
 						apifyMe =
 							connection.getFactory().make(
 								"menu(<term>)",
 								curList.getNext());
 						subMenu = subMenu.insert(apifyMe);
 						actions = actions.remove(cur);
 					}
 				}
 
 				addMenuItems(
 					nextLevel,
 					popupMenu.getActionType(),
 					popupMenu.getModuleName(),
 					subMenu,
 					connection.getFactory().makeList(actionNamePrefix),
 					connection);
 				manager.add(nextLevel);
 			}
 		}
 	}
 
 	public void addMenuItems(
 		IMenuManager menu,
 		ATerm actionType,
 		String moduleName,
 		ATermList actions,
 		ATermList prefixActionName,
 		MetastudioConnection connection) {
 
 		UserInterface ui = new UserInterface();
 		actions = actions.reverse();
 
 		while (!actions.isEmpty()) {
 			ATerm action = actions.getFirst();
 			ATermList actionList =
 				(ATermList) ((ATermAppl) action).getArgument(0);
 			ATermAppl buttonNamePrefix = (ATermAppl) (actionList.getFirst());
 
 			actions = actions.getNext();
 
 			if (actionList.getLength() == 1) {
 				ATerm apifyMe =
 					connection.getFactory().make(
 						"menu(<term>)",
 						prefixActionName.concat(actionList));
 				menu.add(
 					new ModuleExplorerAction(buttonNamePrefix.getName(), popupMenu.getActionType(), action, fViewer));
 			} else {
 				ATermList actionRunner = actions;
 				IMenuManager nextLevel =
 					new MenuManager(buttonNamePrefix.getName());
 				ATermList subMenu =
 					connection.getFactory().makeList(
 						(ATerm) actionList.getNext());
 
 				// collect a list of buttons that are in the same 'menuNamePrefix'
 				while (!actionRunner.isEmpty()) {
 					ATerm cur = actionRunner.getFirst();
 					ATermList curList =
 						(ATermList) ((ATermAppl) cur).getArgument(0);
 					ATerm menuNamePrefix = curList.getFirst();
 
 					if (buttonNamePrefix.isEqual(menuNamePrefix)) {
 						ATerm apifyMe =
 							connection.getFactory().make(
 								"menu(<term>)",
 								curList.getNext());
 						subMenu = subMenu.insert(apifyMe);
 						actions = actions.remove(cur);
 					}
 
 					actionRunner = actionRunner.getNext();
 				}
 
 				addMenuItems(
 					nextLevel,
 					actionType,
 					moduleName,
 					subMenu,
 					prefixActionName.insertAt(
 						buttonNamePrefix,
 						prefixActionName.getLength()),
 					connection);
 				menu.add(nextLevel);
 			}
 		}
 	}
 
 	static public void addModule(String moduleName) {
 		Directory dir, dirChild;
 		int i = 0;
 
 		if (!(root instanceof Directory)) {
 			root = new Directory();
 		}
 
 		dir = root;
 		dirChild = root;
 		String[] splitModuleName = moduleName.split("/");
 		for (i = 0; i < splitModuleName.length - 1; i++) {
 			dirChild = dir.getChild(splitModuleName[i]);
 			if (dirChild == null) {
 				dirChild = new Directory(splitModuleName[i]);
 				dir.add(dirChild);
 			}
			dir = dirChild;
 		}
 		if (dirChild.getChildModule(splitModuleName[i]) == null) {
 			dirChild.add(new Module(moduleName, splitModuleName[i]));
 		}
 	}
 
 	static public void removeModule(String moduleName) {
 		Directory dir, dirChild;
 		int i = 0;
 		
 		dir = root;
 		dirChild = root;
 		String[] splitModuleName = moduleName.split("/");
 		for (i = 0; i < splitModuleName.length - 1; i++) {
 			dirChild = dir.getChild(splitModuleName[i]);
 			if (dirChild != null) {
 				dir = dirChild;
 			}
 		}
 		if (dir != null) {
 			dir.remove(dir.getChildModule(splitModuleName[i]));
 		}
 	}
 
 	public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			Object first = ((IStructuredSelection) selection).getFirstElement();
 			if (first instanceof Module) {
 				final String moduleName = ((Module) first).getModulePath();
 				UserInterface ui = new UserInterface();
 				MetastudioConnection factory = new MetastudioConnection();
 				ui.postEvent(
 					factory.getFactory().make(
 						"get-buttons(module-popup, <str>)",
 						moduleName));
 			}
 		}
 	}
 	
 	public static Shell getShell() {
 		return shell;
 	}
 }
