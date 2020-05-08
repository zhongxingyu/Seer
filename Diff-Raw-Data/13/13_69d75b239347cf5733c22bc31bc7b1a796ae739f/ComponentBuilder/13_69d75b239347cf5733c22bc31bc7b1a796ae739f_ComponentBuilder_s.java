 package ghm.follow.gui;
 
 import ghm.follow.FollowApp;
 import ghm.follow.config.Configure;
 import ghm.follow.nav.Bottom;
 import ghm.follow.nav.NextTab;
 import ghm.follow.nav.PreviousTab;
 import ghm.follow.nav.Top;
 import ghm.follow.search.ClearAllHighlights;
 import ghm.follow.search.ClearHighlights;
 import ghm.follow.search.Find;
 
 import java.util.HashMap;
 import java.util.ResourceBundle;
 
 import javax.swing.JMenuBar;
 
 public class ComponentBuilder
 {
 	public static Menu fileMenu;
 	public static Menu editMenu;
 	public static Menu toolsMenu;
 	public static Menu windowMenu;
 	public static Menu helpMenu;
 
 	// file menu items
 	public static Menu recentFilesMenu;
 
 	private ComponentBuilder()
 	{
 	}
 
 	/**
 	 * Builds the menu bar for the application
 	 * 
 	 * @return reference the constructed menu bar
 	 */
 	public static JMenuBar buildMenuBar(ResourceBundle resources,
 			HashMap<String, FollowAppAction> actions)
 	{
 		// create menu bar and add menus
 		JMenuBar jMenuBar = new JMenuBar();
 
 		// file menu
 		fileMenu = ComponentBuilder.buildFileMenu(resources, actions);
 		jMenuBar.add(fileMenu);
 		// edit menu
 		editMenu = ComponentBuilder.buildEditMenu(resources, actions);
 		jMenuBar.add(editMenu);
 		// tool menu
 		toolsMenu = ComponentBuilder.buildToolsMenu(resources, actions);
 		jMenuBar.add(toolsMenu);
 		// window menu
 		windowMenu = ComponentBuilder.buildWindowMenu(resources, actions);
 		jMenuBar.add(windowMenu);
 		// help menu
 		helpMenu = ComponentBuilder.buildHelpMenu(resources, actions);
 		jMenuBar.add(helpMenu);
 
 		return jMenuBar;
 	}
 
 	public static Menu buildFileMenu(ResourceBundle resources,
 			HashMap<String, FollowAppAction> actions)
 	{
 		Menu fileMenu = new Menu(resources.getString("menu.File.name"), resources
 				.getString("menu.File.mnemonic"));
 		fileMenu.addFollowAppAction(actions.get(Open.NAME));
 		fileMenu.addFollowAppAction(actions.get(Close.NAME));
 		fileMenu.addSeparator();
 		fileMenu.addFollowAppAction(actions.get(Reset.NAME));
 		fileMenu.addFollowAppAction(actions.get(Pause.NAME));
 		fileMenu.addSeparator();
 		recentFilesMenu = new Menu(resources.getString("menu.RecentFiles.name"), resources
 				.getString("menu.RecentFiles.mnemonic"), FollowApp.getIcon(ComponentBuilder.class, "menu.RecentFiles.icon"));
 		fileMenu.add(recentFilesMenu);
 		fileMenu.addSeparator();
 		fileMenu.addFollowAppAction(actions.get(Exit.NAME));
 		return fileMenu;
 	}
 
 	public static Menu buildEditMenu(ResourceBundle resources,
 			HashMap<String, FollowAppAction> actions)
 	{
 		Menu editMenu = new Menu(resources.getString("menu.Edit.name"), resources
 				.getString("menu.Edit.mnemonic"));
 		editMenu.addFollowAppAction(actions.get(Find.NAME));
		editMenu.addSeparator();
 		editMenu.addFollowAppAction(actions.get(ClearHighlights.NAME));
 		editMenu.addFollowAppAction(actions.get(ClearAllHighlights.NAME));
 		return editMenu;
 	}
 
 	public static Menu buildToolsMenu(ResourceBundle resources,
 			HashMap<String, FollowAppAction> actions)
 	{
 		Menu toolsMenu = new Menu(resources.getString("menu.Tools.name"), resources
 				.getString("menu.Tools.mnemonic"));
 		toolsMenu.addFollowAppAction(actions.get(Top.NAME));
 		toolsMenu.addFollowAppAction(actions.get(Bottom.NAME));
 		toolsMenu.addSeparator();
 		toolsMenu.addFollowAppAction(actions.get(Clear.NAME));
 		toolsMenu.addFollowAppAction(actions.get(ClearAll.NAME));
 		toolsMenu.addFollowAppAction(actions.get(Delete.NAME));
 		toolsMenu.addFollowAppAction(actions.get(DeleteAll.NAME));
 		toolsMenu.addSeparator();
		toolsMenu.addFollowAppAction(actions.get(Configure.NAME));
 		toolsMenu.addFollowAppAction(actions.get(Edit.NAME));
 		return toolsMenu;
 	}
 
 	public static Menu buildWindowMenu(ResourceBundle resources,
 			HashMap<String, FollowAppAction> actions)
 	{
 		Menu windowMenu = new Menu(resources.getString("menu.Window.name"), resources
 				.getString("menu.Window.mnemonic"));
 		windowMenu.addFollowAppAction(actions.get(NextTab.NAME));
 		windowMenu.addFollowAppAction(actions.get(PreviousTab.NAME));
 		return windowMenu;
 	}
 
 	public static Menu buildHelpMenu(ResourceBundle resources,
 			HashMap<String, FollowAppAction> actions)
 	{
 		Menu helpMenu = new Menu(resources.getString("menu.Help.name"), resources
 				.getString("menu.Help.mnemonic"));
 		helpMenu.addFollowAppAction(actions.get(About.NAME));
 		return helpMenu;
 	}
 
 	/**
 	 * Builds the popup menu shown when right clicking in a text area.
 	 * 
 	 * @return
 	 */
 	public static PopupMenu buildPopupMenu(HashMap<String, FollowAppAction> actions)
 	{
 		PopupMenu popupMenu = new PopupMenu();
 		popupMenu.addFollowAppAction(actions.get(Open.NAME));
 		popupMenu.addFollowAppAction(actions.get(Close.NAME));
 		popupMenu.addSeparator();
 		popupMenu.addFollowAppAction(actions.get(Reset.NAME));
 		popupMenu.addFollowAppAction(actions.get(Pause.NAME));
 		popupMenu.addSeparator();
 		popupMenu.addFollowAppAction(actions.get(Top.NAME));
 		popupMenu.addFollowAppAction(actions.get(Bottom.NAME));
 		popupMenu.addSeparator();
 		popupMenu.addFollowAppAction(actions.get(Clear.NAME));
 		popupMenu.addFollowAppAction(actions.get(Delete.NAME));
 		popupMenu.addSeparator();
 		popupMenu.addFollowAppAction(actions.get(Configure.NAME));
 		popupMenu.addFollowAppAction(actions.get(Edit.NAME));
 		return popupMenu;
 	}
 
 	/**
 	 * Builds the toolbar shown at the top of the application
 	 * 
 	 * @return
 	 */
 	public static ToolBar buildToolBar(HashMap<String, FollowAppAction> actions)
 	{
 		ToolBar toolBar = new ToolBar();
 		toolBar.addFollowAppAction(actions.get(Open.NAME));
 		toolBar.addSeparator();
 		toolBar.addFollowAppAction(actions.get(Top.NAME));
 		toolBar.addFollowAppAction(actions.get(Bottom.NAME));
 		toolBar.addSeparator();
 		toolBar.addFollowAppAction(actions.get(Clear.NAME));
 		toolBar.addFollowAppAction(actions.get(ClearAll.NAME));
 		toolBar.addFollowAppAction(actions.get(Delete.NAME));
 		toolBar.addFollowAppAction(actions.get(DeleteAll.NAME));
 		toolBar.addSeparator();
 		toolBar.addFollowAppAction(actions.get(Reset.NAME));
 		toolBar.addFollowAppAction(actions.get(Pause.NAME));
 		toolBar.addSeparator();
 		toolBar.addFollowAppAction(actions.get(Configure.NAME));
 		return toolBar;
 	}
 }
