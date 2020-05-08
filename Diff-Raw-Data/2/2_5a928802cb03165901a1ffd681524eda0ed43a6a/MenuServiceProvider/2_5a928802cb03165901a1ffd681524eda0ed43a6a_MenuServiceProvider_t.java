 package com.m0pt0pmatt.menuservice;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 import com.m0pt0pmatt.menuservice.api.AbstractRenderer;
 import com.m0pt0pmatt.menuservice.api.ActionListener;
 import com.m0pt0pmatt.menuservice.api.Menu;
 import com.m0pt0pmatt.menuservice.api.MenuInstance;
 import com.m0pt0pmatt.menuservice.api.MenuService;
 import com.m0pt0pmatt.menuservice.api.Renderer;
 
 /**
  * The MenuServiceProvider is the provider for the Bukkit service MenuService.
  * It is the implementation of the MenuService API
  * @author Matthew Broomfield (m0pt0pmatt) <m0pt0pmatt17@gmail.com>
  *
  */
 public class MenuServiceProvider implements MenuService, Listener{
 
 	//Menus are kept for reference
 	private Map<String, Menu> menusByName;
 	
 	//Renderers are kept for reference
 	private Map<String, Renderer> renderersByName;
 	
 	//players and their current MenuInstance are stored
 	private Map<String, MenuInstance> playersToInstances;
 	
 	//The MenuServiceProvider keeps track of all of the instances for a Menu
 	private Map<Menu, List<MenuInstance>> menusToInstances;
 	
 	//Commands to menus, so the MenuService plugin know which menu to show when a registered command is ran
 	private Map<String, Menu> commandsToMenus;
 	
 	//The Map of binded Materials
 	private Map<Material, Menu> materialsToMenus;
 	
 	//The Map of binded ItemStacks
 	private Map<ItemStack, Menu> itemsToMenus;
 	
 	//The plugin which loaded the MenuServiceProvider
 	private MenuServicePlugin plugin;
 	
 	//the Yaml File loader/saver
 	private YAMLBuilder yamlBuilder;
 	
 	/**
 	 * Creates the MenuServiceProvider.
 	 * The plugin of it's creator is needed so the MenuServiceProvider can register events.
 	 * All built in Renderers and MenuProviders are added here.
 	 * 
 	 * @param plugin The Plugin which owns the MenuServiceProvider
 	 */
 	public MenuServiceProvider(MenuServicePlugin plugin){
 		
 		//set the plugin for logging purposes
 		this.plugin = plugin;
 		plugin.log(3, Level.INFO, "Starting initialization of MenuServiceProvider");
 		
 		//initialize the Yaml Builder
 		yamlBuilder = new YAMLBuilder();
 		plugin.log(3, Level.INFO, "Yaml Builder initialized");
 		
 		//initialize maps
 		menusByName = Collections.synchronizedMap(new HashMap<String, Menu>());
 		renderersByName = Collections.synchronizedMap(new HashMap<String, Renderer>());
 		playersToInstances = Collections.synchronizedMap(new HashMap<String, MenuInstance>());
 		menusToInstances = Collections.synchronizedMap(new HashMap<Menu, List<MenuInstance>>());
 		commandsToMenus = Collections.synchronizedMap(new HashMap<String, Menu>());
 		materialsToMenus = Collections.synchronizedMap(new HashMap<Material, Menu>());
 		itemsToMenus = Collections.synchronizedMap(new HashMap<ItemStack, Menu>());
 		plugin.log(3, Level.INFO, "Maps initialized");
 		
 		//add Renderers
 		addRenderer(new InventoryRenderer(this, plugin));
 		plugin.log(1, Level.INFO, "InventoryRenderer loaded");
 		
 		Bukkit.getPluginManager().registerEvents(this, plugin);
 		plugin.log(3, Level.INFO, "MenuServiceProvider registered in Bukkit");
 		
 		plugin.log(3, Level.INFO, "MenuServiceProvider initialized");
 	}
 	
 	
 	/**
 	 * Closes the MenuInstance for a player
 	 * 
 	 * Tells the MenuProvider for the MenuInstance to close the Menu.
 	 * Removes the player from the MenuInstance.
 	 * 
 	 * @param playerName the Name of the player
 	 */
 	@Override
 	public void closeMenuInstance(String playerName) {
 		
 		//get the MenuInstance
 		MenuInstance instance = playersToInstances.get(playerName);
 		
 		//check if null
 		if (instance == null){
 			plugin.log(2, Level.SEVERE, "No MenuInstance was found for the player " + playerName);
 			return;
 		}
 		
 		for (Renderer renderer: instance.getAllRenderers()){
 			renderer.closeMenu(playerName);
 		}
 		
 		//remove the player from the instance
 		instance.removePlayer(playerName);
 		
 		//if there are no more players for the instance, notify the ActionListeners
 		if (instance.getPlayers().size() == 0){
 			for (ActionListener listener: instance.getActionListeners().values()){
 				listener.playerCountZero(instance, playerName);
 			}
 		}
 		
 		//unregister the player from the MenuInstance
 		this.playersToInstances.remove(playerName);
 		
 		this.plugin.getLogger().info("MenuInstance closed for player " + playerName);
 	}
 	
 	/**
 	 * Creates a new Menu from the given Plugin and fileName.
 	 * 
 	 * Since Menus are stored based on the plugin that owns them, 
 	 * the plugin needs to be referenced so the correct file location can be found.
 	 * 
 	 * The fileName is the name of the config file for the Menu.
 	 * 
 	 * After the Menu is loaded, it is stored in the MenuService
 	 * 
 	 * @param plugin The Plugin which the Menu belongs to
 	 * @param fileName The name of the Menu's config file
 	 */
 	@Override
 	public Menu loadMenu(Plugin plugin, String fileName) {
 		
 		//check the filename
 		if (fileName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLFILENAME, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTLOADMENU, null);
 			return null;
 		}
 				
 		//check the plugin
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, fileName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTLOADMENU, fileName);
 			return null;
 		}		
 		
 		//load the file
 		Menu menu = yamlBuilder.loadYAML(plugin, fileName);
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, fileName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTLOADMENU, fileName);
 			return null;
 		}
 		
 		//add the menu
 		if (this.addMenu(plugin, menu)){
 			this.plugin.log(2, Level.INFO, "Menu " + menu.getName() + " loaded"); 
 			return menu;
 		}
 		
 		this.plugin.log(2, Level.SEVERE, LogMessage.CANTLOADMENU, fileName);
 		return menu;
 	}
 	
 	/**
 	 * Saves a Menu to file
 	 * @param plugin the Plugin which will hold the menu
 	 * @param menu the Menu to be saved
 	 * @param fileName the name of the file to store the menu in
 	 * @return true if successful, false is unsuccessful
 	 */
 	@Override
 	public boolean saveMenu(Plugin plugin, Menu menu, String fileName) {
 		
 		//check the menu
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTSAVEMENU, null);
 			return false;
 		}
 		
 		//check the plugin
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTSAVEMENU, menu.getName());
 			return false;
 		}
 		
 		//check the fileName
 		if (fileName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLFILENAME, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTSAVEMENU, menu.getName());
 			return false;
 		}
 		
 		//save the Menu
 		return this.yamlBuilder.saveYAML(plugin, menu, fileName);
 	}
 	
 	/**
 	 * Adds a Menu to the MenuService
 	 * @param plugin the Plugin which owns the Menu
 	 * @param menu the Menu
 	 */
 	@Override
 	public boolean addMenu(Plugin plugin, Menu menu) {
 		
 		//check menu
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTADDMENU, null);
 			return false;
 		}
 		
 		//check Plugin
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTADDMENU, menu.getName());
 			return false;
 		}
 		
 		//check if menu exists
 		if (menusByName.containsKey(menu.getName())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.MENUALREADYEXISTS, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTADDMENU, menu.getName());
 			return false;
 		}
 		
 		menusByName.put(menu.getName(), menu);
 		menusToInstances.put(menu, new LinkedList<MenuInstance>());
 		this.plugin.log(2, Level.INFO, "Menu " + menu.getName() + " was created and added");
 		
 		String command = (String) menu.getAttribute("openCommand");
 		if (command != null){
 			commandsToMenus.put(command, menu);
 			this.plugin.log(2, Level.INFO, "Run " + menu.getName() + " with the command: /" + command);
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Returns a Menu from the MenuService
 	 * @param plugin the Plugin which stores the Menu
 	 * @param menuName the name of the Menu
 	 * @return the Menu if it exists, null otherwise
 	 */
 	@Override
 	public Menu getMenu(Plugin plugin, String menuName) {
 		
 		//check for null menuName
 		if (menuName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENU, null);
 			return null;
 		}
 		
 		//check for null plugin
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, menuName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENU, menuName);
 		}
 		
 		//get the menu
 		Menu menu = menusByName.get(menuName);
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, menuName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENU, menuName);
 			return null;
 		}
 		
 		//if plugin is wrong
 		if (!((String)menu.getAttribute("plugin")).equals(plugin.getName())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.WRONGMENU, menuName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENU, menuName);
 			return null;
 		}
 		
 		return menu;
 	}
 
 	/**
 	 * Checks if a Menu is loaded into the MenuService
 	 * @param plugin the Plugin which owns the Menu
 	 * @param menu the Menu
 	 * @return true if the MenuService has the Menu, false otherwise
 	 */
 	@Override
 	public boolean hasMenu(Plugin plugin, Menu menu) {
 		
 		//check menu
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTHASMENU, null);
 		}
 		
 		//check plugin
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTHASMENU, menu.getName());
 		}
 		
 		//see if MenuService contains the menu
 		if (menusByName.containsValue(menu)){
 			
 			//check if same
 			Menu m = menusByName.get(menu.getName());
 			if (m.equals(menu)){
 				return true;
 			}
 		}
 		
 		//not the same
 		return false;
 	}
 	
 	/**
 	 * Removes a Menu from the MenuService. This does not save the menu to file.
 	 * @param plugin The Plugin which owns the Menu.
 	 * @param menu the Menu to remove
 	 */
 	@Override
 	public void removeMenu(Plugin plugin, Menu menu) {
 		
 		//check menu
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, null);
 			return;
 		}
 		
 		//check plugin
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, menu.getName());
 		}
 		
 		//check if plugin is correct
 		if (!plugin.getName().equals(menu.getAttribute("plugin"))){
 			this.plugin.log(2, Level.SEVERE, LogMessage.WRONGMENU, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, menu.getName());
 			return;
 		}
 		
 		//remove the menu
		menusByName.remove(menu.getName());
 		
 		//remove all of the MenuInstances for the Menu
 		List<MenuInstance> instances = menusToInstances.remove(menu);
 		for (MenuInstance instance: instances){
 			this.removeMenuInstance(instance);
 		}
 		
 		this.plugin.getLogger().warning("Removed Menu " + menu.getName());
 	}
 
 	/**
 	 * Removes a Menu from the MenuService. This does not save the menu to file.
 	 * @param plugin The Plugin which owns the Menu.
 	 * @param menuName the name of the Menu
 	 * @return The Menu if it exists, null otherwise
 	 */
 	@Override
 	public Menu removeMenu(Plugin plugin, String menuName) {
 		
 		if (menuName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, null);
 			return null;
 		}
 		
 		if (plugin == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLUGIN, menuName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, menuName);
 			return null;
 		}
 		
 		//get the menu
 		Menu menu = menusByName.get(menuName);
 		
 		if (plugin.getName().equals(menu.getAttribute("plugin"))){
 			//remove the menu
 			removeMenu(plugin, menu);
 			this.plugin.log(2, Level.SEVERE, "Removed menu " + menuName);
 			return menu;
 		}
 		
 		this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, menuName);
 		this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENU, menuName);
 		return null;
 	}
 	
 	/**
 	 * Creates a MenuInstance from the given Menu.
 	 * MenuInstances are given names so ActionListeners know when they open or close.
 	 * 
 	 * While a Menu is an abstract layout, a MenuInstance is an implementation of the Menu.
 	 * Once created, a MenuInstance can be shown to players.
 	 * 
 	 * The MenuInstance is returned after it is created.
 	 * It is also registered under it's Menu in the MenuService
 	 * @param menu The Menu which the MenuInstance will be created from
 	 * @param provider The MenuProvider which will provide for the MenuInstance
 	 */
 	@Override
 	public MenuInstance createMenuInstance(Menu menu, String instanceName) {
 		return createMenuInstance(menu, instanceName, new HashMap<String, Object>());
 	}
 	
 	/**
 	 * Creates a MenuInstance with given parameters from the given Menu.
 	 * MenuInstances are given names so ActionListeners know when they open or close.
 	 * 
 	 * While a Menu is an abstract layout, a MenuInstance is an implementation of the Menu.
 	 * Once created, a MenuInstance can be shown to players.
 	 * 
 	 * The MenuInstance is returned after it is created.
 	 * It is also registered under it's Menu in the MenuService
 	 * @param menu The Menu which the MenuInstance will be created from
 	 * @param provider The MenuProvider which will provide for the MenuInstance
 	 * @param parameters All parameters for the MenuInstance
 	 */
 	@Override
 	public MenuInstance createMenuInstance(Menu menu, String instanceName, Map<String, Object> parameters) {
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTCREATEMENUINSTANCE, null);
 			return null;
 		}
 		
 		if (instanceName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENUINSTANCENAME, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTCREATEMENUINSTANCE, menu.getName());
 			return null;
 		}
 		
 		if (parameters == null){
 			parameters = new HashMap<String, Object>();
 		}
 		
 		if (!menusByName.containsKey(menu.getName())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTCREATEMENUINSTANCE, null);
 			return null;
 		}
 		
 		//create the MenuInstance
 		MenuInstance instance = new MenuInstance(menu, instanceName, new LinkedList<String>(), parameters, new HashMap<String, Renderer>(), new HashMap<String, ActionListener>());
 		
 		//add the MenuInstance to the Menu
 		menusToInstances.get(menu).add(instance);
 		
 		this.plugin.log(2, Level.INFO, "MenuInstance " + instanceName + " was created");
 
 		return instance;
 	}
 	
 	/**
 	 * Removes a MenuInstance from the MenuService
 	 * @param instance the MenuInstance to be removed
 	 */
 	@Override
 	public void removeMenuInstance(MenuInstance instance) {
 		
 		if (instance == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENUINSTANCE, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENUINSTANCE, null);
 			return;
 		}
 		
 		if (!menusToInstances.containsKey(instance.getMenu())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, instance.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENUINSTANCE, instance.getName());
 			return;
 		}
 		
 		//unregister the instance with the menu
 		menusToInstances.get(instance.getMenu()).remove(instance);
 		
 		//unregister all players with the menu instance
 		for (String playerName: instance.getPlayers()){
 			playersToInstances.remove(playerName);
 		}
 		
 	}
 	
 	/**
 	 * Removes a MenuInstance from the MenuService
 	 * @param menu the Menu which the MenuInstance is made of
 	 * @param instanceName the name of the MenuInstance to be removed
 	 * @return the MenuInstance if it exists, null otherwise
 	 */
 	@Override
 	public MenuInstance removeMenuInstance(Menu menu, String instanceName) {
 		
 		if (instanceName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENUINSTANCE, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENUINSTANCE, null);
 			return null;
 		}
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENUINSTANCE, null);
 			return null;
 		}
 		
 		MenuInstance instance =  this.getMenuInstance(menu, instanceName);
 		if (instance == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENUINSTANCE, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVEMENUINSTANCE, null);
 			return null;
 		}
 		
 		this.removeMenuInstance(instance);
 		
 		return instance;
 	}
 
 	/**
 	 * Returns a MenuInstance from the MenuService
 	 * @param menu the Menu which the MenuInstance is made of
 	 * @param instanceName the name of the MenuInstance
 	 * @return the MenuInstance if it exists, null otherwise
 	 */
 	@Override
 	public MenuInstance getMenuInstance(Menu menu, String instanceName) {
 		
 		if (instanceName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENUINSTANCENAME, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENUINSTANCE, null);
 			return null;
 		}
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, instanceName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENUINSTANCE, instanceName);
 			return null;
 		}
 		
 
 		List<MenuInstance> instances = menusToInstances.get(menu);
 		if (instances == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENUINSTANCE, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENUINSTANCE, instanceName);
 			return null;
 		}
 		
 		for (MenuInstance instance: instances){
 			if (instance.getName().equals(instanceName)){
 				return instance;
 			}
 		}
 		
 		this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENUINSTANCE, instanceName);
 		this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETMENUINSTANCE, instanceName);
 		return null;
 	}
 
 	/**
 	 * Checks if the MenuService has a MenuInstance
 	 * @param menu the Menu which the MenuInstance is made of
 	 * @param instanceName the name of the MenuInstance
 	 * @return true if it exists, false otherwise
 	 */
 	@Override
 	public boolean hasMenuInstance(Menu menu, String instanceName) {
 		
 		if (instanceName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENUINSTANCENAME, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTHASMENUINSTANCE, null);
 			return false;
 		}
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, instanceName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTHASMENUINSTANCE, instanceName);
 			return false;
 		}
 		
 		if (this.getMenuInstance(menu, instanceName) != null){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Opens a MenuInstance to a given player.
 	 * 
 	 * The MenuService will also register that the player is currently viewing the given MenuInstance.
 	 * In order for a MenuInstance to be shown, the Menu of the MenuInstance must be provided for by a MenuProvider.
 	 * MenuInstances also need to have at least one Renderer. This can be assigned to the MenuInstance itself, or inherited from the Renderers of its Menu.
 	 * 
 	 * @param instance The MenuInstane to be shown.
 	 * @param playerName The name of the player to show the MenuInstance to.
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean openMenuInstance(MenuInstance instance, String playerName) {
 		
 		if (instance == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENUINSTANCE, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTOPENMENUINSTANCE, null);
 			return false;
 		}
 		
 		if (playerName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLAYERNAME, instance.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTOPENMENUINSTANCE, instance.getName());
 			return false;
 		}
 		
 		Menu menu = instance.getMenu();
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, instance.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTOPENMENUINSTANCE, instance.getName());
 			return false;
 		}
 		
 		Player player = Bukkit.getPlayer(playerName);
 		if (player == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHPLAYER, instance.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTOPENMENUINSTANCE, instance.getName());
 			return false;
 		}
 		
 		if (menu.hasAttribute("permissions")){
 			List<String> permissions;
 			try{
 				permissions = (List<String>) menu.getAttribute("permissions");
 			}
 			catch (ClassCastException e){
 				this.plugin.log(2, Level.SEVERE, LogMessage.CANTCASTATTRIBUTE, "permissions");
 				this.plugin.log(2, Level.SEVERE, LogMessage.CANTOPENMENUINSTANCE, instance.getName());
 				return false;
 			}
 			if (permissions != null){
 				for (String permission: permissions){
 					if (!player.hasPermission(permission)){
 						player.sendMessage(ChatColor.RED + "You do not have permission to open the menu");
 						return false;
 					}
 				}
 			}
 		}
 		
 		//add the player to the instance
 		instance.getPlayers().add(playerName);
 		for (ActionListener listener: instance.getActionListeners().values()){
 			listener.playerAdded(instance, playerName);
 		}
 		
 		//register the player
 		playersToInstances.put(playerName, instance);
 		
 		//render the MenuInstance
 		instance.renderPlayer(playerName);
 		
 		return true;
 	}
 	
 	/**
 	 * Adds a Renderer to the MenuService.
 	 * After added, a Renderer can be used to Render MenuInstances.
 	 * Renderers can be assigned to Menus (and thus every MenuInstance of a menu) or individual MenuInstances.
 	 * @param renderer The Renderer to be added to the MenuService
 	 */
 	@Override
 	public void addRenderer(Renderer renderer) {
 		
 		//check for null
 		if (renderer == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLRENDERER, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTADDRENDERER, null);
 			return;
 		}
 		
 		//check to make sure a MenuProvider of the same name isn't already loaded
 		if (renderersByName.containsKey(renderer.getName())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHRENDERER, renderer.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTADDRENDERER, renderer.getName());
 			return;
 		}
 		
 		//make sure Renderer is an extension of an AbstractRenderer
 		if (!(renderer instanceof AbstractRenderer)){
 			this.plugin.log(2, Level.SEVERE, LogMessage.RENDERERNOTABSTRACTRENDERER, renderer.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTADDRENDERER, renderer.getName());
 			return;
 		}
 		
 		//add the Renderer
 		renderersByName.put(renderer.getName(), renderer);
 		
 		this.plugin.log(2, Level.INFO, "Renderer " + renderer.getName() + " was added");
 	}
 	
 	/**
 	 * Returns a Renderer by name from the MenuService.
 	 * @param rendererName The name of the Renderer
 	 * @return The specified Renderer, or null if one does not exist
 	 */
 	@Override
 	public Renderer getRenderer(String rendererName) {
 		
 		if (rendererName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLRENDERERNAME, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTGETRENDERER, null);
 		}
 		
 		return renderersByName.get(rendererName);
 	}
 	
 	/**
 	 * Removes a Renderer from the MenuService
 	 * @param rendererName the name of the Renderer to be removed
 	 * @return the Renderer if it exists, null otherwise
 	 */
 	@Override
 	public Renderer removeRenderer(String rendererName) {
 		
 		if (rendererName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLRENDERERNAME, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVERENDERER, null);
 			return null;
 		}
 		
 		Renderer renderer = renderersByName.get(rendererName);
 		if (renderer == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHRENDERER, rendererName);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVERENDERER, rendererName);
 			return null;
 		}
 		
 		removeRenderer(renderer);
 		
 		return renderer;
 	}
 
 	/**
 	 * Removes a Renderer from the MenuService
 	 * @param renderer the Renderer to be removed
 	 */
 	@Override
 	public void removeRenderer(Renderer renderer) {
 		
 		if (renderer == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLRENDERER, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVERENDERER, null);
 			return;
 		}
 		
 		if (!renderersByName.containsKey(renderer)){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHRENDERER, renderer.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTREMOVERENDERER, renderer.getName());
 			return;
 		}
 		
 		renderersByName.remove(renderer);
 		
 	}
 	
 	/**
 	 * Checks if the MenuService has a specified Renderer
 	 * @param the Renderer
 	 * @return true if the Renderer exists, false otherwise
 	 */
 	@Override
 	public boolean hasRenderer(Renderer renderer) {
 		
 		if (renderer == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLRENDERER, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTHASRENDERER, null);
 			return false;
 		}
 		
 		return renderersByName.containsValue(renderer);
 	}
 
 	/**
 	 * Checks if the MenuService has a specified Renderer
 	 * @param rendererName the name of the Renderer
 	 * @return true if the Renderer exists, false otherwise
 	 */
 	@Override
 	public boolean hasRenderer(String rendererName) {
 		
 		if (rendererName == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLRENDERERNAME, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTHASRENDERER, null);
 			return false;
 		}
 		
 		return renderersByName.containsKey(rendererName);
 	}
 
 	/**
 	 * Binds an ItemStack to a Menu, so the Menu can be opened by right-clicking the ItemStack
 	 * @param item the ItemStack
 	 * @param menu the Menu
 	 * @return true if successful, false if unsuccessful
 	 */
 	@Override
 	public boolean bindMenu(ItemStack item, Menu menu) {
 		
 		if (item == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLITEMSTACK, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTBINDMENUITEM, null);
 			return false;
 		}
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, item.toString());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTBINDMENUITEM, item.toString());
 			return false;
 		}
 		
 		if (!menusByName.containsKey(menu.getName())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTBINDMENUITEM, item.toString());
 			return false;
 		}
 		
 		itemsToMenus.put(new ItemStack(item), menu);
 		return true;
 	}
 
 	/**
 	 * Binds a Material to a Menu, so the Menu can be opened by right-clicking the Material
 	 * @param material the Material
 	 * @param menu the Menu
 	 * @return true if successful, false if unsuccessful
 	 */
 	@Override
 	public boolean bindMenu(Material material, Menu menu) {
 		
 		if (material == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMATERIAL, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTBINDMENUMATERIAL, null);
 			return false;
 		}
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTBINDMENUMATERIAL, material.toString());
 			return false;
 		}
 		
 		if (!menusByName.containsKey(menu.getName())){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NOSUCHMENU, menu.getName());
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTBINDMENUMATERIAL, material.toString());
 			return false;
 		}
 		
 		materialsToMenus.put(material, menu);
 		return true;
 	}
 
 	/**
 	 * Unbinds any Materials and ItemStacks from a given Menu
 	 * @param menu the Menu
 	 * @return true if successful, false if unsuccessful
 	 */
 	@Override
 	public boolean unbindMenu(Menu menu) {
 		
 		if (menu == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMENU, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTUNBINDMENU, null);
 			return false;
 		}
 		
 		if (itemsToMenus.containsValue(menu)){
 			Iterator<Entry<ItemStack, Menu>> iterator = itemsToMenus.entrySet().iterator();
 			while (iterator.hasNext()){
 				if (iterator.next().getKey().equals(menu)){
 					iterator.remove();
 				}
 			}
 		}
 		
 		if (materialsToMenus.containsValue(menu)){
 			Iterator<Entry<Material, Menu>> iterator = materialsToMenus.entrySet().iterator();
 			while (iterator.hasNext()){
 				if (iterator.next().getKey().equals(menu)){
 					iterator.remove();
 				}
 			}
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Unbinds an ItemStack from a menu if it is binded
 	 * @param item the ItemStack
 	 * @return true if successful, false if unsuccessful
 	 */
 	@Override
 	public boolean unbindMenu(ItemStack item) {
 		
 		if (item == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLITEMSTACK, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTUNBINDITEM, null);
 			return false;
 		}
 		
 		itemsToMenus.remove(item);
 		return true;
 	}
 
 	/**
 	 * Unbinds a Material from a menu if it is binded
 	 * @param material the Material
 	 * @return true if successful, false if unsuccessful
 	 */
 	@Override
 	public boolean unbindMenu(Material material) {
 		
 		if (material == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLMATERIAL, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTUNBINDMATERIAL, null);
 			return false;
 		}
 		
 		materialsToMenus.remove(material);
 		return true;
 	}
 
 	/**
 	 * Saves all Menus to file
 	 */
 	@Override
 	public void saveAll() {
 		for (Menu menu: menusByName.values()){
 			
 			Plugin plugin = Bukkit.getPluginManager().getPlugin((String) menu.getAttribute("plugin"));
 			if (plugin != null){
 				String filename = null;
 				if (menu.hasAttribute("filename")){
 					filename = (String) menu.getAttribute("filename");
 				} else{
 					filename = menu.getTag() + ".yml";
 				}
 				yamlBuilder.saveYAML(plugin, menu, filename);
 			}
 			
 			
 		}
 	}
 
 	/**
 	 * Closes all Menus
 	 */
 	@Override
 	public void closeAll() {
 		for (Renderer renderer: renderersByName.values()){
 			renderer.closeAll();
 		}
 		
 	}
 
 
 	/**
 	 * Checks if a binded command is being executed. If so, opens the correct Menu.
 	 * @param command the Command which was executed
 	 * @param player the name of the player who executed the command
 	 * @return true if the command was binded, false otherwise
 	 */
 	protected boolean checkCommand(String command, Player player){
 
 		if (command == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLCOMMAND, null);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTEXECUTECOMMAND, null);
 			return false;
 		}
 		if (player == null){
 			this.plugin.log(2, Level.SEVERE, LogMessage.NULLPLAYER, command);
 			this.plugin.log(2, Level.SEVERE, LogMessage.CANTEXECUTECOMMAND, command);
 			return false;
 		}
 		
 		for (Entry<String, Menu> entry: commandsToMenus.entrySet()){
 			
 			//check command
 			if (command.equals(entry.getKey())){
 								
 				//commands match. Start Menu
 				MenuInstance instance = createMenuInstance(entry.getValue(), player.getName() + ": " + entry.getValue().getName());
 				openMenuInstance(instance, player.getName());
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Checks if the player right clicked a binded ItemStack or Material
 	 * If so, opens the binded menu.
 	 * @param event
 	 */
 	@EventHandler
 	public void itemClick(PlayerInteractEvent event){
 		
 		if (event == null){
 			return;
 		}
 		
 		ItemStack item = event.getItem();
 
 		if (event.getAction() != Action.RIGHT_CLICK_AIR){
 			return;
 		}
 
 		if (!event.isBlockInHand()){
 			return;
 		}
 
 		if (materialsToMenus.containsKey(item.getType())){
 
 			event.setCancelled(true);
 			Menu menu = materialsToMenus.get(item.getType());
 			this.openMenuInstance(this.createMenuInstance(menu, menu.getName() + ": " + event.getPlayer().getName()), event.getPlayer().getName());
 			return;
 		}
 	}
 
 	/**
 	 * Returns a List of the Menus that are currently loaded in the MenuService
 	 * @return all currently loaded Menus
 	 */
 	@Override
 	public List<Menu> getMenus() {
 		
 		return new LinkedList<Menu>(menusByName.values());
 	}
 
 }
