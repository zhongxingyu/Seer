 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.core;
 
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.ICoolBarManager;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jubula.app.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 import org.eclipse.ui.actions.ContributionItemFactory;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.ide.IDEActionFactory;
 import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
 import org.eclipse.ui.internal.WorkbenchMessages;
 import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 
 /**
  * @author BREDEX GmbH
  * @created 23.08.2005
  */
 public class JubulaActionBarAdvisor extends ActionBarAdvisor {
     /** WorkbenchWindow */
     private IWorkbenchWindow m_window;
 
     /** Action: quit application */
     private ActionFactory.IWorkbenchAction m_quitAction;
 
     /** Action: preferences */
     private ActionFactory.IWorkbenchAction m_prefAction;
 
     /** Action: help */
     private ActionFactory.IWorkbenchAction m_helpCont;
     
     /** Action: welcome screen */
     private ActionFactory.IWorkbenchAction m_intro;
 
     /** Action: save */
     private ActionFactory.IWorkbenchAction m_fileSave;
     
     /** Action: save_all */
     private ActionFactory.IWorkbenchAction m_fileSaveAll;
     
     /** Action: redo */
     private IWorkbenchAction m_redo;
 
     /** Action: undo */
     private IWorkbenchAction m_undo;
 
     /** Action: choose workspace */
     private IWorkbenchAction m_openWorkspaceAction;
     
     /**
      * @param configurer IActionBarConfigurer
      * @param windowConfigurer IWorkbenchWindowConfigurer
      */
     public JubulaActionBarAdvisor(IActionBarConfigurer configurer,
         IWorkbenchWindowConfigurer windowConfigurer) {
         super(configurer);
         m_window = configurer.getWindowConfigurer().getWindow();
     }
     
     /**
      * {@inheritDoc}
      */
     public void fillActionBars(int flags) {            
         if ((flags & ActionBarAdvisor.FILL_PROXY) != 0) {
             return;
         }
         fillActionBars(getActionBarConfigurer(), flags);
     }
     
     /**
      * 
      * @param configurer
      *            IactionBarConfigurer
      * @param flags
      *            int
      */
     public void fillActionBars(IActionBarConfigurer configurer, int flags) {
         if ((flags & ActionBarAdvisor.FILL_PROXY) == 0) {
             makeActions(configurer);
         }
         if ((flags & ActionBarAdvisor.FILL_MENU_BAR) != 0) {
             fillMenuBar(configurer.getMenuManager());
         }
         if ((flags & ActionBarAdvisor.FILL_COOL_BAR) != 0) {
             fillCoolBar(configurer.getCoolBarManager());
         }
     }
 
     /**
      * actions of the menu entries
      * @param configurer
      *            IactionBarConfigurer 
      */
     private void makeActions(IActionBarConfigurer configurer) {
         m_intro = ActionFactory.INTRO.create(m_window);
         register(m_intro);
         
         m_helpCont = ActionFactory.HELP_CONTENTS.create(m_window);
         register(m_helpCont);
         
         m_quitAction = ActionFactory.QUIT.create(m_window);
         register(m_quitAction);
         
         m_prefAction = ActionFactory.PREFERENCES.create(m_window);
         register(m_prefAction);
         
         m_fileSaveAll = ActionFactory.SAVE_ALL.create(m_window);
         register(m_fileSaveAll);
         
         m_fileSave = ActionFactory.SAVE.create(m_window);
         register(m_fileSave);
 
         m_openWorkspaceAction = IDEActionFactory.OPEN_WORKSPACE
                 .create(m_window);
         register(m_openWorkspaceAction);
 
         m_redo = ActionFactory.REDO.create(m_window);
         register(m_redo);
 
         m_undo = ActionFactory.UNDO.create(m_window);
         register(m_undo);
     }
 
     /**
      * designs the menubar
      * 
      * @param menuBar
      *            IMenuManager
      */
     public void fillMenuBar(IMenuManager menuBar) {
         menuBar.add(createFileMenu());
         menuBar.add(createEditMenu());
         menuBar.add(createSearchMenu());
         menuBar.add(createRunMenu());
         menuBar.add(createWindowMenu());
         menuBar.add(createHelpMenu()); 
     }
     
     /**
      * Creates Edit menu.
      * 
      * @return IMenuManager.
      */
     private IMenuManager createEditMenu() {
         IMenuManager editMenu = new MenuManager(Messages.ActionBuilderEdit,
                 IWorkbenchActionConstants.M_EDIT);
         /* 
          * JubulaEditSeparator1 = Create>
          * JubulaEditSeparator1 = Add>
          * JubulaEditSeparator3 = Rename/Delete
          */
         editMenu.add(m_redo);
         editMenu.add(m_undo);
         editMenu.add(new Separator()); 
         editMenu.add(getCutItem());
         editMenu.add(getPasteItem());
         editMenu.add(new Separator()); 
         editMenu.add(new GroupMarker("JubulaEditSeparator1")); //$NON-NLS-1$
         editMenu.add(new Separator()); 
         editMenu.add(new GroupMarker("JubulaEditSeparator2")); //$NON-NLS-1$
         editMenu.add(new Separator()); 
         editMenu.add(new GroupMarker("JubulaEditSeparator3")); //$NON-NLS-1$
         return editMenu;
     }
 
     /**
      * Creates a Search menu.
      * 
      * @return the menu manager for the created menu.
      */
     private IMenuManager createSearchMenu() {
         IMenuManager searchMenu = new MenuManager(Messages.ActionBuilderSearch,
                 "org.eclipse.search.menu"); //$NON-NLS-1$
         return searchMenu;
     }
 
     /**
      * Creates a Run menu.
      * 
      * @return the menu manager for the created menu.
      */
     private IMenuManager createRunMenu() {
         IMenuManager runMenu = new MenuManager(Messages.ActionBuilderRun,
                 "org.eclipse.ui.run"); //$NON-NLS-1$
         return runMenu;
     }
 
     /**
      * Creates a File menu.
      * 
      * @return the menu manager for the created menu.
      */
     private IMenuManager createFileMenu() {
         IMenuManager fileMenu = new MenuManager(
                 Messages.ActionBuilderMyFileEntry,
                 IWorkbenchActionConstants.M_FILE);
         fileMenu.add(m_fileSave);
         fileMenu.add(m_fileSaveAll);
         fileMenu.add(new Separator());
         CommandHelper.createContributionPushItem(fileMenu,
                 IWorkbenchCommandConstants.FILE_RENAME);
         fileMenu.add(getRefreshItem());
         fileMenu.add(new Separator());
         fileMenu.add(m_openWorkspaceAction);
         CommandHelper.createContributionPushItem(fileMenu,
                 IWorkbenchCommandConstants.FILE_RESTART);
         fileMenu.add(new Separator());
         CommandHelper.createContributionPushItem(fileMenu,
                 ActionFactory.IMPORT.getCommandId());
         CommandHelper.createContributionPushItem(fileMenu,
                 ActionFactory.EXPORT.getCommandId());
         fileMenu.add(new Separator()); 
         fileMenu.add(m_quitAction);
         return fileMenu;
     }
 
     /**
      * Creates the Window menu.
      * @return IMenuManager.
      */
     private IMenuManager createWindowMenu() {
         IMenuManager windowMenu = new MenuManager(Messages
                 .ActionBuilderWindowEntry,
                 IWorkbenchActionConstants.M_WINDOW);
         windowMenu.add(createOpenPersp());
         windowMenu.add(createShowView());
         windowMenu.add(new Separator());
         windowMenu.add(getResetPerspectiveItem());
         windowMenu.add(new Separator());
         windowMenu.add(m_prefAction);
         return windowMenu;
     }
 
     /**
      * Creates Show View menu.
      * @return IMenuManager.
      */
     private IMenuManager createShowView() {
         IMenuManager showView = new MenuManager(Messages.ActionBuildershowView);
         showView.add(
                 ContributionItemFactory.VIEWS_SHORTLIST.create(m_window));
         return showView;
     }
         
     /**
      * Creates Open Perspective
      * 
      * @return IMenuManager.
      */
     private IMenuManager createOpenPersp() {
         IMenuManager openPersp = new MenuManager(
                 Messages.ActionBuilderopenPerspective);
         openPersp.add(ContributionItemFactory.PERSPECTIVES_SHORTLIST
                 .create(m_window));
         return openPersp;
     }
     
     /**
      * Creates the help menu.
      * @return an IMenuManager.
      */
     private IMenuManager createHelpMenu() {
         IMenuManager helpMenu = new MenuManager(Messages
                 .ActionBuilderMyHelpEntry, 
                 IWorkbenchActionConstants.M_HELP);
         helpMenu.add(m_intro);
         helpMenu.add(m_helpCont);
 
         helpMenu.add(new Separator("helpEnd")); //$NON-NLS-1$
         helpMenu.add(new Separator());
         helpMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
         helpMenu.add(new Separator());
 
         CommandHelper.createContributionPushItem(
                 helpMenu, IWorkbenchCommandConstants.HELP_ABOUT);
 
         return helpMenu;
     }
     
     
     /**
      * designs the toolbar
      * 
      * @param coolBar
      *            ICoolBarManager
      */
     public void fillCoolBar(ICoolBarManager coolBar) {
         IToolBarManager toolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
         toolBar.add(new ActionContributionItem(m_fileSave));
         coolBar.add(new ToolBarContributionItem(toolBar, "main")); //$NON-NLS-1$
         coolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
     }
 
     /**
      * 
      * disposes the actions
      */
     public void dispose() {
         if (m_fileSave != null) {
             m_fileSave.dispose();
         }
         if (m_helpCont != null) {
             m_helpCont.dispose();
         } 
         if (m_prefAction != null) {
             m_prefAction.dispose();
         }   
         if (m_quitAction != null) {
             m_quitAction.dispose();
         }
         if (m_openWorkspaceAction != null) {
             m_openWorkspaceAction.dispose();
         }
     }
     
     /**
      * @return the reset perspective item
      */
     private IContributionItem getResetPerspectiveItem() {
         return getItem(
                 ActionFactory.RESET_PERSPECTIVE.getId(),
                 ActionFactory.RESET_PERSPECTIVE.getCommandId(),
                 null,
                 null,
                 WorkbenchMessages.ResetPerspective_text,
                 WorkbenchMessages.ResetPerspective_toolTip, 
                 IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
     }
     
     /**
      * @return the refresh item
      */
     private IContributionItem getRefreshItem() {
         return getItem(ActionFactory.REFRESH.getId(),
                 ActionFactory.REFRESH.getCommandId(), null, null,
                 WorkbenchMessages.Workbench_refresh,
                 WorkbenchMessages.Workbench_refreshToolTip, null);
     }
     
     /**
      * @return the paste item
      */
     private IContributionItem getPasteItem() {
         return getItem(
                 ActionFactory.PASTE.getId(),
                 ActionFactory.PASTE.getCommandId(),
                 ISharedImages.IMG_TOOL_PASTE,
                 ISharedImages.IMG_TOOL_PASTE_DISABLED,
                 WorkbenchMessages.Workbench_paste,
                 WorkbenchMessages.Workbench_pasteToolTip, null);
     }
     
     /**
      * @return the cut item
      */
     private IContributionItem getCutItem() {
         return getItem(
                 ActionFactory.CUT.getId(),
                 ActionFactory.CUT.getCommandId(),
                 ISharedImages.IMG_TOOL_CUT,
                 ISharedImages.IMG_TOOL_CUT_DISABLED,
                 WorkbenchMessages.Workbench_cut,
                 WorkbenchMessages.Workbench_cutToolTip, null);
     }
     
     /**
      * @param actionId
      *            the action id
      * @param commandId
      *            the command id
      * @param image
      *            the image
      * @param disabledImage
      *            the disabled image
      * @param label
      *            the label
      * @param tooltip
      *            the tooltip
      * @param helpContextId
      *            the help id
      * @return an item instance
      */
     private IContributionItem getItem(String actionId, String commandId,
         String image, String disabledImage, String label, String tooltip,
         String helpContextId) {
         IWorkbenchWindow window = m_window;
         ISharedImages sharedImages = window.getWorkbench().getSharedImages();
 
         IActionCommandMappingService acms = 
                 (IActionCommandMappingService) window
                 .getService(IActionCommandMappingService.class);
         acms.map(actionId, commandId);
 
         CommandContributionItemParameter commandParm = 
                 new CommandContributionItemParameter(
                 window, actionId, commandId, null,
                 sharedImages.getImageDescriptor(image),
                 sharedImages.getImageDescriptor(disabledImage), null, label,
                 null, tooltip, CommandContributionItem.STYLE_PUSH, null, false);
         return new CommandContributionItem(commandParm);
     }
 }
