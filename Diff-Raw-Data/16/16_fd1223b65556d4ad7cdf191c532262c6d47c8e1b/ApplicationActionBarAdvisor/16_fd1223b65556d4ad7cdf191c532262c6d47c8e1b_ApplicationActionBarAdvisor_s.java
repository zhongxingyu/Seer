 package de.objectcode.time4u.client.ui;
 
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.ICoolBarManager;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 
 /**
  * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
  * Each window will be populated with new actions.
  */
 public class ApplicationActionBarAdvisor extends ActionBarAdvisor
 {
 
   // Actions - important to allocate these only in makeActions, and then use them
   // in the fill methods.  This ensures that the actions aren't recreated
   // when fillActionBars is called with FILL_PROXY.
   private IWorkbenchAction exitAction;
   private IWorkbenchAction aboutAction;
   private IWorkbenchAction newWindowAction;
 
   public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer)
   {
     super(configurer);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void makeActions(final IWorkbenchWindow window)
   {
     // Creates the actions and registers them.
     // Registering is needed to ensure that key bindings work.
     // The corresponding commands keybindings are defined in the plugin.xml file.
     // Registering also provides automatic disposal of the actions when
     // the window is closed.
 
     exitAction = ActionFactory.QUIT.create(window);
     register(exitAction);
 
     aboutAction = ActionFactory.ABOUT.create(window);
     register(aboutAction);
 
     newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
     register(newWindowAction);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void fillMenuBar(final IMenuManager menuBar)
   {
     final MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
     final MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
 
     menuBar.add(fileMenu);
     // Add a group marker indicating where action set menus will appear.
     menuBar.add(new GroupMarker("projectAdditions"));
     menuBar.add(new GroupMarker("todoAdditions"));
     menuBar.add(new GroupMarker("workItemAdditions"));
     menuBar.add(new GroupMarker("synchronizeAdditions"));
     menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
     menuBar.add(helpMenu);
 
     // File
     fileMenu.add(newWindowAction);
     fileMenu.add(new Separator());
     fileMenu.add(exitAction);
 
     // Help
     helpMenu.add(aboutAction);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void fillCoolBar(final ICoolBarManager coolBar)
   {
    //    final IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    //    coolBar.add(new ToolBarContributionItem(toolbar, "main"));
     //    toolbar.add(openViewAction);
     //    toolbar.add(messagePopupAction);
   }
 }
