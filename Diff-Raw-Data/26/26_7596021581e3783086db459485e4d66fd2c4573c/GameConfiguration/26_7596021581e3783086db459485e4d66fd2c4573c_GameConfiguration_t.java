 package com.ftwinston.KillerMinecraft;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 class GameConfiguration
 {
 	private Game game;
 	EnumMap<Menu, Inventory> inventories = new EnumMap<GameConfiguration.Menu, Inventory>(Menu.class);
 	
 	ItemStack backItem;
 	ItemStack rootGameMode, rootGameModeConfig, rootWorldGen, rootWorldGenConfig, rootPlayerNumbers, rootMonsters, rootAnimals;
 	ItemStack[] gameModeItems, worldGenItems, monsterItems, animalItems;
 	
 	public GameConfiguration(Game game)
 	{
 		this.game = game;
 		createMenus();
 	}
 	
 	enum Menu
 	{
 		ROOT,
 		GAME_MODE,
 		GAME_MODE_CONFIG,
 		WORLD_GEN,
 		WORLD_GEN_CONFIG,
 		PLAYERS,
 		MONSTERS,
 		ANIMALS,
 		SPECIFIC_OPTION_CHOICE,
 	}
 	
 	private static String /*loreStyle = "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC,*/ highlightStyle = "" + ChatColor.YELLOW + ChatColor.ITALIC;
 	Menu currentMenu = Menu.ROOT;
 	private final short playerHeadDurability = 3; 
 	
 	private void createMenus()
 	{
 		backItem = new ItemStack(Material.IRON_DOOR);
 		setNameAndLore(backItem, highlightStyle + "Go back", "Return to the previous menu");
 		
 		createGameModeMenu();
 		createWorldGenMenu();
 		createPlayersMenu();
 		createMonstersMenu();
 		createAnimalsMenu();
 		createRootMenu();
 	}
 	
 	private void createRootMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, game.getName() + " configuration");
 		inventories.put(Menu.ROOT, menu);
 		
 		rootGameMode = new ItemStack(Material.CAKE);
 		rootGameModeConfig = new ItemStack(Material.DISPENSER);
 		rootWorldGen = new ItemStack(Material.GRASS);
 		rootWorldGenConfig = new ItemStack(Material.DROPPER);
 		//rootMutators = new ItemStack(Material.EXP_BOTTLE);
 		rootPlayerNumbers = new ItemStack(Material.SKULL_ITEM, 1, playerHeadDurability); // steve head
 		rootMonsters = new ItemStack(Material.SKULL_ITEM, 1, (short)4); // creeper head
 		rootAnimals = new ItemStack(Material.EGG);
 		
 		setNameAndLore(rootGameMode, "Change Game Mode", "");
 		setNameAndLore(rootGameModeConfig, "Configure Game Mode", "Any options the current game", "mode has can be configured here");
 		setNameAndLore(rootWorldGen, "Change World Generator", "");
 		setNameAndLore(rootWorldGenConfig, "Configure World Generator", "Any options the current world", "generator has can be configured here");
 		//setNameAndLore(rootMutators, "Select Mutators", "Mutators change specific aspects", "of a game, but aren't specific", "to any particular game mode");
 		setNameAndLore(rootPlayerNumbers, "Player limits", "Specify the maximum number of", "players allowed into the game");
 		setNameAndLore(rootMonsters, "Monster Numbers", "Control the number of", "monsters that spawn");
 		setNameAndLore(rootAnimals, "Animal Numbers", "Control the number of", "animals that spawn");
 		
 		gameModeChanged(game.getGameMode());
 		worldGeneratorChanged(game.getWorldGenerator());
 		
 		menu.setItem(0, rootGameMode);
 		menu.setItem(1, rootGameModeConfig);
 		menu.setItem(2, rootWorldGen);
 		menu.setItem(3, rootWorldGenConfig);
 		//menu.setItem(4, rootMutators);
 		menu.setItem(6, rootPlayerNumbers);
 		menu.setItem(7, rootMonsters);
 		menu.setItem(8, rootAnimals);
 	}
 	
 	private void createGameModeMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, nearestNine(GameMode.gameModes.size() + 2), "Game mode selection");
 		menu.setItem(0, backItem);
 		inventories.put(Menu.GAME_MODE, menu);
 		
 		gameModeItems = new ItemStack[GameMode.gameModes.size()];
 		for ( int i=0; i<GameMode.gameModes.size(); i++ )
 		{
 			GameModePlugin mode = GameMode.get(i);
 			gameModeItems[i] = new ItemStack(mode.getMenuIcon());
 			setupGameModeItemLore(i, mode, game.getGameMode().getName().equals(mode.getName()));
 		}
 	}
 	
 	private void setupGameModeItemLore(int num, GameModePlugin mode, boolean current) 
 	{
 		ItemStack item = gameModeItems[num];
 		if ( current )
 		{
 			String[] desc = mode.getDescriptionText();
 			ArrayList<String> lore = new ArrayList<String>(desc.length + 1);
 			lore.add(highlightStyle + "Current game mode");
 			for ( int j=0; j<desc.length; j++)
 				lore.add(desc[j]);
 			setNameAndLore(item, mode.getName(), lore);
 			item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);
 		}
 		else
 			setNameAndLore(item, mode.getName(), mode.getDescriptionText());
 
 		inventories.get(Menu.GAME_MODE).setItem(num+2, item);
 	}
 	
 	private void createGameModeConfigMenu(GameMode mode)
 	{
 		Inventory menu = createModuleOptionsMenu(mode);
 		inventories.put(Menu.GAME_MODE_CONFIG, menu);
 	}
 	
 	private void createWorldGenMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, nearestNine(WorldGenerator.worldGenerators.size() + 2), "World generator selection");
 		menu.setItem(0, backItem);
 		inventories.put(Menu.WORLD_GEN, menu);
 		
 		worldGenItems = new ItemStack[WorldGenerator.worldGenerators.size()];
 		for ( int i=0; i<WorldGenerator.worldGenerators.size(); i++ )
 		{
 			WorldGeneratorPlugin world = WorldGenerator.get(i);
 			
 			ItemStack item = new ItemStack(world.getMenuIcon());
 			worldGenItems[i] = item;
 			setupWorldGenItemLore(i, world, game.getWorldGenerator().getName().equals(world.getName()));
 		}
 	}
 
 	private void setupWorldGenItemLore(int num, WorldGeneratorPlugin world, boolean current)
 	{
 		ItemStack item = worldGenItems[num];
 		if ( current )
 		{
 			String[] desc = world.getDescriptionText();
 			ArrayList<String> lore = new ArrayList<String>(desc.length + 1);
 			lore.add(highlightStyle + "Current world generator");
 			for ( int j=0; j<desc.length; j++)
 				lore.add(desc[j]);
 			setNameAndLore(item, world.getName(), lore);
 			item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);
 		}
 		else
 			setNameAndLore(item, world.getName(), world.getDescriptionText());
 
 		inventories.get(Menu.WORLD_GEN).setItem(num+2, item);
 	}
 
 	private void createWorldGenConfigMenu(WorldGenerator world)
 	{
 		Inventory menu = createModuleOptionsMenu(world);
 		inventories.put(Menu.WORLD_GEN_CONFIG, menu);
 	}
 	
 	private Inventory createModuleOptionsMenu(KillerModule module)
 	{
 		Inventory menu = Bukkit.createInventory(null, nearestNine(module.options.length + 2), module.getName() + " options");
 		generateOptionMenuItems(menu, module);
 		return menu;
 	}
 	
 	private void generateOptionMenuItems(Inventory menu, KillerModule module)
 	{
 		menu.setItem(0, backItem);
 		
 		for ( int i=0; i<module.options.length; i++ )
 		{
 			Option option = module.options[i];
 			ItemStack item = new ItemStack(option.getDisplayMaterial());
 			setNameAndLore(item, option.getName(), option.getDescription());
 			menu.setItem(i+2, item);
 		}
 	}
 
 	private void createPlayersMenu()
 	{
 		Inventory menu = null;
 		inventories.put(Menu.PLAYERS, menu);
 	}
 	
 	final int numQuantityItems = 5;
 	static final String[] animalDescriptions = new String[] { "No animals will spawn", "Reduced animal spawn rate", "Normal animal spawn rate", "High animal spawn rate", "Excessive animal spawn rate" };
 	static final String[] monsterDescriptions = new String[] { "No monsters will spawn", "Reduced monster spawn rate", "Normal monster spawn rate", "High monster spawn rate", "Excessive monster spawn rate" };
 	private void createMonstersMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, "Monster numbers");
 		menu.setItem(0, backItem);
 		
 		monsterItems = new ItemStack[numQuantityItems];
 		for ( int i=0; i<numQuantityItems; i++ )
 		{
 			ItemStack item = monsterItems[i] = createQuantityItem(i, game.monsterNumbers == i, monsterDescriptions[i]);
 			menu.setItem(i+2, item);
 		}
 		
 		inventories.put(Menu.MONSTERS, menu);
 	}
 	
 	private void createAnimalsMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, "Animal numbers");
 		menu.setItem(0, backItem);
 		
 		animalItems = new ItemStack[numQuantityItems];
 		for ( int i=0; i<numQuantityItems; i++ )
 		{
 			ItemStack item = animalItems[i] = createQuantityItem(i, game.animalNumbers == i, animalDescriptions[i]);
 			menu.setItem(i+2, item);
 		}
 		
 		inventories.put(Menu.ANIMALS, menu);
 	}
 
 	private ItemStack createQuantityItem(int quantity, boolean selected, String lore)
 	{
 		ItemStack item = new ItemStack(Material.STICK);
 		
 		if ( selected )
 			setNameAndLore(item, GameInfoRenderer.getQuantityText(quantity), highlightStyle + "Current Setting", lore);
 		else
 			setNameAndLore(item, GameInfoRenderer.getQuantityText(quantity), lore);
 		
 		switch ( quantity )
 		{
 		case 1:
 			item.setType(Material.WOOD_PICKAXE); break;
 		case 2:
 			item.setType(Material.STONE_PICKAXE); break;
 		case 3:
 			item.setType(Material.IRON_PICKAXE); break;
 		case 4:
 			item.setType(Material.DIAMOND_PICKAXE); break;
 		}
 
 		if ( selected )
 			item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);
 		return item;
 	}
 	
 	private void setNameAndLore(ItemStack item, String name, List<String> lore)
 	{
 		ItemMeta meta = item.getItemMeta();
 		meta.setDisplayName(ChatColor.RESET + name);
 		meta.setLore(lore);
 		item.setItemMeta(meta);
 	}
 	
 	private void setNameAndLore(ItemStack item, String name, String... lore)
 	{
 		setNameAndLore(item, name, Arrays.asList(lore));
 	}
 	
 	private void setLore(ItemStack item, List<String> lore)
 	{
 		ItemMeta meta = item.getItemMeta();
 		meta.setLore(lore);
 		item.setItemMeta(meta);
 	}
 	
 	private void setLore(ItemStack item, String... lore)
 	{
 		setLore(item, Arrays.asList(lore));
 	}
 	
 	private int nearestNine(int num)
 	{
 		return 9*(int)Math.ceil(num/9.0);
 	}
 
 	private int findMatch(ItemStack[] set, ItemStack item)
 	{
 		for ( int i=0; i<set.length; i++ )
 		{
 			ItemStack test = set[i];
 			if ( item.getType() == test.getType() && item.getItemMeta().getDisplayName() == test.getItemMeta().getDisplayName() )
 				return i;
 		}
 		return -1;
 	}
 	
 	public void show(Player player)
 	{
 		String configuring = game.getConfiguringPlayer();
 		if ( configuring != null )
 		{
 			if ( !configuring.equals(player.getName()) )
 				player.sendMessage(game.getName() + " is already being configured by " + configuring);
 			
 			return;
 		}
 		
 		game.setConfiguringPlayer(player.getName());
 		showMenu(player, Menu.ROOT);
 	}
 
 	private void showMenu(Player player, Menu menu)
 	{
 		Inventory inv = inventories.get(menu);
 		if ( inv != null )
 		{
 			currentMenu = menu;
 			player.openInventory(inv);
 		}
 	}
 	
 	public void gameModeChanged(GameMode gameMode)
 	{
 		setLore(rootGameMode, highlightStyle + "Current mode: " + gameMode.getName(), "The game mode is the main set of rules,", "and controls every aspect of a game.");
 		inventories.get(Menu.ROOT).setItem(0, rootGameMode);
 		createGameModeConfigMenu(gameMode);
 	}
 
 	public void worldGeneratorChanged(WorldGenerator world)
 	{
 		setLore(rootWorldGen, highlightStyle + "Current generator: "+ world.getName(), "The world generator controls", "the terrain in the game's world(s)");
 		inventories.get(Menu.ROOT).setItem(2, rootWorldGen);
 		createWorldGenConfigMenu(world);
 	}
 
 	private void rootMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == rootGameMode.getType() )
 			showMenu(player, Menu.GAME_MODE);
 		else if ( item.getType() == rootGameModeConfig.getType() )
 			showMenu(player, Menu.GAME_MODE_CONFIG);
 		else if ( item.getType() == rootWorldGen.getType() )
 			showMenu(player, Menu.WORLD_GEN);
 		else if ( item.getType() == rootWorldGenConfig.getType() )
 			showMenu(player, Menu.WORLD_GEN_CONFIG);
 		else if ( item.getType() == rootPlayerNumbers.getType() && item.getDurability() == playerHeadDurability )
 			showMenu(player, Menu.PLAYERS);
 		else if ( item.getType() == rootMonsters.getType() )
 			showMenu(player, Menu.MONSTERS);
 		else if ( item.getType() == rootAnimals.getType() )
 			showMenu(player, Menu.ANIMALS);
 	}
 
 	private void gameModeMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		int i = findMatch(gameModeItems, item);
 		if ( i < 0 )
 			return;
 
 		GameModePlugin mode = GameMode.get(i); 
 		GameModePlugin prev = (GameModePlugin)game.getGameMode().getPlugin();
 		game.setGameMode(mode);
 		showMenu(player, Menu.GAME_MODE_CONFIG);
 		
 		// update which item gets the "current game mode" lore text on the game mode menu
 		setupGameModeItemLore(GameMode.indexOf(prev), prev, false);
 		setupGameModeItemLore(i, mode, true);
 	}
 	
 	private void gameModeConfigClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		Option option = game.getGameMode().options[itemSlot-2];
 			
 		ItemStack[] choiceItems = option.optionClicked();
 		if ( choiceItems != null )
 		{
 			showChoiceOptionMenu(player, option, choiceItems, Menu.GAME_MODE_CONFIG);
 		}
 		else
 			generateOptionMenuItems(inventories.get(Menu.GAME_MODE_CONFIG), game.getGameMode());
 	}
 	
 	private void worldGenMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		int i = findMatch(worldGenItems, item);
 		if ( i < 0 )
 			return;
 
 		WorldGeneratorPlugin generator = WorldGenerator.get(i); 
 		WorldGeneratorPlugin prev = (WorldGeneratorPlugin)game.getWorldGenerator().getPlugin();
 		game.setWorldGenerator(generator);
 		showMenu(player, Menu.WORLD_GEN_CONFIG);
 		
 		// update which item gets the "current world generator" lore text
 		setupWorldGenItemLore(WorldGenerator.indexOf(prev), prev, false);
 		setupWorldGenItemLore(i, generator, true);
 	}
 
 	private void worldGenConfigClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 
 		Option option = game.getWorldGenerator().options[itemSlot-2];
 			
 		ItemStack[] choiceItems = option.optionClicked();
 		if ( choiceItems != null )
 		{
 			showChoiceOptionMenu(player, option, choiceItems, Menu.WORLD_GEN_CONFIG);
 		}
 		else
 			generateOptionMenuItems(inventories.get(Menu.WORLD_GEN_CONFIG), game.getWorldGenerator());
 	}
 	
 	private void playersMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 	}
 	
 	private void monstersMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		for ( int i=0; i<numQuantityItems; i++ )
 			if ( item.getType() == monsterItems[i].getType() && item.getItemMeta().getDisplayName() == monsterItems[i].getItemMeta().getDisplayName() )
 			{
 				if ( game.monsterNumbers == i )
 					return;
 				
 				int prev = game.monsterNumbers;
 				game.monsterNumbers = i;
 				
 				item = monsterItems[i] = createQuantityItem(i, true, monsterDescriptions[i]);
 				inventories.get(Menu.MONSTERS).setItem(i+2, item);
 				item = monsterItems[prev] = createQuantityItem(prev, false, monsterDescriptions[prev]);
 				inventories.get(Menu.MONSTERS).setItem(prev+2, item);
 				
 				game.miscRenderer.allowForChanges();
 				return;
 			}
 	}
 	
 	private void animalsMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		for ( int i=0; i<numQuantityItems; i++ )
 			if ( item.getType() == animalItems[i].getType() && item.getItemMeta().getDisplayName() == animalItems[i].getItemMeta().getDisplayName() )
 			{
 				if ( game.animalNumbers == i )
 					return;
 				
 				int prev = game.animalNumbers;
 				game.animalNumbers = i;
 				
 				item = animalItems[i] = createQuantityItem(i, true, animalDescriptions[i]);
 				inventories.get(Menu.ANIMALS).setItem(i+2, item);
 				item = animalItems[prev] = createQuantityItem(prev, false, animalDescriptions[prev]);
 				inventories.get(Menu.ANIMALS).setItem(prev+2, item);
 				
 				game.miscRenderer.allowForChanges();
 				return;
 			}
 	}
 	
 	Menu choiceOptionGoBackTo;
 	Option currentOption;
 	private void choiceOptionMenuClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			currentOption = null;
 			inventories.put(Menu.SPECIFIC_OPTION_CHOICE, null);
			
			if ( choiceOptionGoBackTo == Menu.GAME_MODE_CONFIG )
				generateOptionMenuItems(inventories.get(choiceOptionGoBackTo), game.getGameMode());
			else if ( choiceOptionGoBackTo == Menu.WORLD_GEN_CONFIG ) 
				generateOptionMenuItems(inventories.get(choiceOptionGoBackTo), game.getWorldGenerator());
			
 			showMenu(player, choiceOptionGoBackTo);
 			return;
 		}
 		
 		currentOption.setSelectedIndex(itemSlot-2);
 		populateChoiceOptionMenu(currentOption.optionClicked(), currentOption.getSelectedIndex(), inventories.get(Menu.SPECIFIC_OPTION_CHOICE));
 	
 		// which of these could have changed depends on if this is an option for the game mode or world generator 
 		game.modeRenderer.allowForChanges();
 		game.miscRenderer.allowForChanges();
 	}
 	
 	private void showChoiceOptionMenu(Player player, Option option, ItemStack[] choiceItems, Menu goBackTo)
 	{
 		choiceOptionGoBackTo = goBackTo;
 		currentOption = option;
 		Inventory menu = Bukkit.createInventory(null, nearestNine(choiceItems.length + 2), option.getName());
 		menu.setItem(0, backItem);	
 		
 		populateChoiceOptionMenu(choiceItems, option.getSelectedIndex(), menu);
 		
 		inventories.put(Menu.SPECIFIC_OPTION_CHOICE, menu);
 		currentMenu = Menu.SPECIFIC_OPTION_CHOICE;
 		player.openInventory(menu);
 	}
 
 	private void populateChoiceOptionMenu(ItemStack[] choiceItems, int selectedIndex, Inventory menu)
 	{
 		for ( int i=0; i<choiceItems.length; i++ )
 		{
 			ItemStack item = choiceItems[i];
 			if ( selectedIndex == i )
 				item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);				
 			menu.setItem(i+2, item);
 		}
 	}
 
 	private static Game getGameByConfiguringPlayer(Player player)
 	{	
 		for ( Game game : KillerMinecraft.instance.games )
 		{
 			String name = game.getConfiguringPlayer();
 			 
 			if ( name != null && name.equals(player.getName()) )
 				return game;
 		}
 		return null;
 	}
 	
 	public static void checkEvent(InventoryClickEvent event)
 	{
 		if ( !(event.getWhoClicked() instanceof Player) || event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR )
 			return;
 		
 		Player player = (Player)event.getWhoClicked();
 		Game game = getGameByConfiguringPlayer(player);
 		if ( game == null )
 			return;
 		
 		event.setCancelled(true);
 		
 		if ( event.getRawSlot() >= event.getInventory().getSize() ) // click in player's own inventory
 		{
 			player.closeInventory();
 			return;
 		}
 		
 		switch (game.configuration.currentMenu)
 		{
 		case ROOT:
 			game.configuration.rootMenuClicked(player, event.getCurrentItem()); break;
 		case GAME_MODE:
 			game.configuration.gameModeMenuClicked(player, event.getCurrentItem()); break;
 		case GAME_MODE_CONFIG:
 			game.configuration.gameModeConfigClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		case WORLD_GEN:
 			game.configuration.worldGenMenuClicked(player, event.getCurrentItem()); break;
 		case WORLD_GEN_CONFIG:
 			game.configuration.worldGenConfigClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		case PLAYERS:
 			game.configuration.playersMenuClicked(player, event.getCurrentItem()); break;
 		case MONSTERS:
 			game.configuration.monstersMenuClicked(player, event.getCurrentItem()); break;
 		case ANIMALS:
 			game.configuration.animalsMenuClicked(player, event.getCurrentItem()); break;
 		case SPECIFIC_OPTION_CHOICE:
 			game.configuration.choiceOptionMenuClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		}
 	}
 
 	public static void checkEvent(InventoryCloseEvent event)
 	{
 		if ( !(event.getPlayer() instanceof Player) )
 			return;
 		
 		Player player = (Player)event.getPlayer();
 		Game game = getGameByConfiguringPlayer(player);
 		if ( game == null )
 			return;
 		
 		// only clear the configuring player if the closed inventory is the "current" one (decided by checking the name)
 		if ( event.getInventory().getName().equals(game.configuration.inventories.get(game.configuration.currentMenu).getName()) )
 			game.setConfiguringPlayer(null);		
 	}
 	
 	private static void checkClearConfiguringPlayer(Player player)
 	{
 		Game game = getGameByConfiguringPlayer(player);
 		if ( game != null )
 		{
 			game.setConfiguringPlayer(null);
 			player.closeInventory();
 		}
 	}
 	
 	public static void checkEvent(PlayerChangedWorldEvent event)
 	{
 		checkClearConfiguringPlayer(event.getPlayer());
 	}
 	
 	public static void checkEvent(PlayerDeathEvent event)
 	{
 		checkClearConfiguringPlayer(event.getEntity());
 	}
 	
 	public static void checkEvent(PlayerQuitEvent event)
 	{
 		checkClearConfiguringPlayer(event.getPlayer());
 	}
 }
