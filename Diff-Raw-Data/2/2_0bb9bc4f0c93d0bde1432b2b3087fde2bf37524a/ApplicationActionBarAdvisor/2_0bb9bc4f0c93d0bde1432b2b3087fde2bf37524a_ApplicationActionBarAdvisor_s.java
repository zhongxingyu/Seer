 package net.sparktank.morrigan;
 
 import net.sparktank.morrigan.actions.NewPlaylistAction;
 
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.ICoolBarManager;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ContributionItemFactory;
 import org.eclipse.ui.actions.RetargetAction;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 
 /**
  * An action bar advisor is responsible for creating, adding, and disposing of
  * the actions added to a workbench window. Each window will be populated with
  * new actions.
  */
 public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
 
 	// Actions - important to allocate these only in makeActions, and then use them
 	// in the fill methods. This ensures that the actions aren't recreated
 	// when fillActionBars is called with FILL_PROXY.
 	private IWorkbenchAction exitAction;
 	
 	// Window.
 	private IWorkbenchAction resetPerspectiveAction;
 	private MenuManager showViewMenuMgr;
 	private IContributionItem showViewItemShortList;
 	
 	// List actions.
 	private IAction newPlayListAction;
 	IWorkbenchAction saveAction;
 	private RetargetAction addAction;
 	private RetargetAction removeAction;
 	
 	public static final String ADD_ACTIONID = "morrigan.add";
 	public static final String REMOVE_ACTIONID = "morrigan.remove";
 	
 	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
 		super(configurer);
 	}
 
 	@Override
 	protected void makeActions(final IWorkbenchWindow window) {
 		// Creates the actions and registers them.
 		// Registering is needed to ensure that key bindings work.
 		// The corresponding commands keybindings are defined in the plugin.xml file.
 		// Registering also provides automatic disposal of the actions when the window is closed.
 		
 		newPlayListAction = new NewPlaylistAction(window);
 		register(newPlayListAction);
 		
 		exitAction = ActionFactory.QUIT.create(window);
 		register(exitAction);
 		
 		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
 		showViewMenuMgr = new MenuManager("Show view", "showView");
 		showViewItemShortList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
 		
 		// Editor actions.
 		saveAction = ActionFactory.SAVE.create(window);
 		register(saveAction);
 		
		addAction = new RetargetAction(ADD_ACTIONID, "&add");
 		addAction.setImageDescriptor(Activator.getImageDescriptor("icons/plus.gif"));
 		getActionBarConfigurer().registerGlobalAction(addAction);
 		register(addAction);
 		window.getPartService().addPartListener(addAction);
 		
 		removeAction = new RetargetAction(REMOVE_ACTIONID, "&remove selected...");
 		removeAction.setImageDescriptor(Activator.getImageDescriptor("icons/minus.gif"));
 		getActionBarConfigurer().registerGlobalAction(removeAction);
 		register(removeAction);
 		window.getPartService().addPartListener(removeAction);
 	}
 	
 	@Override
 	protected void fillMenuBar(IMenuManager menuBar) {
 		MenuManager fileMenu = new MenuManager("&Morrigan", IWorkbenchActionConstants.M_FILE);
 		menuBar.add(fileMenu);
 		fileMenu.add(exitAction);
 		
 		MenuManager playlistMenu = new MenuManager("&Playlist", "playlist");
 		menuBar.add(playlistMenu);
 		playlistMenu.add(newPlayListAction);
 		playlistMenu.add(new Separator());
 		playlistMenu.add(saveAction);
 		playlistMenu.add(addAction);
 		playlistMenu.add(removeAction);
 		
 		MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
 		menuBar.add(windowMenu);
 		windowMenu.add(resetPerspectiveAction);
 		showViewMenuMgr.add(showViewItemShortList);
 		windowMenu.add(showViewMenuMgr);
 	}
 	
 	@Override
 	protected void fillCoolBar(ICoolBarManager coolBar) {
 		coolBar.add(new GroupMarker("group.list"));
 		IToolBarManager fileToolBar = new ToolBarManager(coolBar.getStyle());
 		fileToolBar.add(saveAction);
 		fileToolBar.add(addAction);
 		fileToolBar.add(removeAction);
 		coolBar.add(new ToolBarContributionItem(fileToolBar));
 	}
 
 }
