 package net.sf.anathema.rcp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.ICoolBarManager;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ContributionItemFactory;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 
 public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
 
   private static final String MAIN_TOOLBAR_ID = "net.sf.anathema.rcp.MainToolbar"; //$NON-NLS-1$
   private IWorkbenchAction exitAction;
   private List<IAction> toolbarActions = new ArrayList<IAction>();
   private IWorkbenchAction saveAction;
   private IWorkbenchAction saveAllAction;
   private IWorkbenchAction closeAction;
   private IWorkbenchAction closeAllAction;
   private IContributionItem viewList;
 
   public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
     super(configurer);
   }
 
   @Override
   protected void makeActions(final IWorkbenchWindow window) {
     viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
     closeAction = ActionFactory.CLOSE.create(window);
     closeAllAction = ActionFactory.CLOSE_ALL.create(window);
     saveAction = ActionFactory.SAVE.create(window);
     saveAllAction = ActionFactory.SAVE_ALL.create(window);
     exitAction = ActionFactory.QUIT.create(window);
     register(saveAction);
     register(saveAllAction);
     register(exitAction);
     createActions(window);
   }
 
   private void createActions(IWorkbenchWindow workbenchWindows) {
     IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(MAIN_TOOLBAR_ID);
     for (IExtension extension : extensionPoint.getExtensions()) {
       for (IConfigurationElement configurationElement : extension.getConfigurationElements()) {
         try {
           IWindowAction action = (IWindowAction) configurationElement.createExecutableExtension("actionClass"); //$NON-NLS-1$
           action.setWorkbenchWindow(workbenchWindows);
           toolbarActions.add(action);
         }
         catch (CoreException e) {
          // TODO Fehlerhandling
          e.printStackTrace();
         }
       }
     }
   }
 
   @Override
   protected void fillMenuBar(IMenuManager menuBar) {
     MenuManager fileMenu = new MenuManager(
         Messages.ApplicationActionBarAdvisor_fileMenuName,
         IWorkbenchActionConstants.M_FILE);
     menuBar.add(fileMenu);
     fileMenu.add(closeAction);
     fileMenu.add(closeAllAction);
     fileMenu.add(new Separator());
     fileMenu.add(saveAction);
     fileMenu.add(saveAllAction);
     fileMenu.add(new Separator());
     fileMenu.add(exitAction);
     MenuManager viewMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_viewMenuName);
     menuBar.add(viewMenu);
     viewMenu.add(viewList);
   }
 
   @Override
   protected void fillCoolBar(ICoolBarManager coolBar) {
     IToolBarManager toolBarManager = new ToolBarManager();
     coolBar.add(toolBarManager);
     toolBarManager.add(saveAction);
     toolBarManager.add(saveAllAction);
     addActions(toolBarManager);
   }
 
   private void addActions(IContributionManager manager) {
     for (IAction action : toolbarActions) {
       manager.add(action);
     }
   }
 }
