 package com.m0pt0pmatt.GUIM;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * The listener that handles all event interaction.
  * Warning: this class is a crazy beast, not a sexy one
  * @author Matthew
  *
  */
 public class MarketListener implements Listener{
 	
 	JavaPlugin plugin;
 	
 	public MarketListener(JavaPlugin plugin){
 		this.plugin = plugin;
 	}
 	
 	
 	/**
 	 * Event that is triggered when the player clicks.
 	 * Checks if the player clicked on a access block.
 	 * If so, trigger the appropriate market
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.HIGH)
 	public void marketStart(PlayerInteractEvent event) {
 		
 		
 		//get the player and the PlayerInfo
 		Player player = event.getPlayer();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		
 		//check if the player clicked a block
 		if (event.getClickedBlock() == null){
 			return;
 		}
 
 		// create first time data if needed
 		if (playerInfo == null) {
 			GUIM.addPlayerInfo(player.getName());
 			playerInfo = GUIM.getPlayerInfo(player.getName());
 		}
 		
 		//check if the player clicked on an access block
 		Market market = GUIM.marketLocations.get(event.getClickedBlock().getLocation());
 		if (market == null){
 			return;
 		}
 		
 		//add a player count if there doesn't exist one
 		if (market.getNumSales(player.getName()) == -1){
 			market.addPlayer(player.getName());
 		}
 		
 		//if there is no economy, dont try anything
 		if (GUIM.economy == null){
 			if (!GUIM.setupEconomy()){
 				event.getPlayer().sendMessage("Error. No economy plugin found. Cannot use markets.");
 				return;
 			}
 		}
 		
 		//set the playerInfo correctly
 		playerInfo.currentMarket = market.getFullName();
 		playerInfo.menu = "main";
 		
 		// paint the main menu
 		MenuPainter.paintMenu(player);
 
 		//open the inventory for the player
 		player.openInventory(playerInfo.inventory);
 		
 		//notify the player of which market he or she is in
 		player.sendMessage("Market: " + market.getFullName());
 		player.sendMessage("Main Menu");
 
 	}
 	
 	
 	/**
 	 * Event triggered when an item is clicked in the inventory
 	 * @param event
 	 */
 	@EventHandler(ignoreCancelled=true)
 	public void itemClicked(InventoryClickEvent event) {
 		//get the player and the PlayerInfo
 		Player player = (Player) event.getWhoClicked();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		
 		//If there is no PlayerInfo, then the player can't possibly be at a market
 		if (playerInfo == null){
 			return;
 		}
 		
 		// don't do anything if the player isn't in the marketplace
 		if (playerInfo.currentMarket == null) {
 			return;
 		}
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 
 		//don't allow any right clicking
 		if (event.isRightClick()){
 			event.setResult(org.bukkit.event.Event.Result.DENY);
 	        event.setCancelled(true);
 	        return;
 		}
 		
 		// make sure the slot is in bounds (not off the screen)
 		if (event.getSlot() < 0) {
 			return;
 		}
 
 		// choose the right menu
 		String[] parts = playerInfo.menu.split(":");
 		switch (parts[0]){
 		case "main":
 			mainMenuEvent(event, market);
 			break;
 		case "market":
 			switch (parts[1]){
 			case "view":
 				viewItemsMenuEvent(event, market);
 				break;
 			case "buy":
 				buyMenuEvent(event, market);
 				break;
 			case "sell":
 				sellMenuEvent(event, market);
 				break;
 			}
 			break;
 		case "request":
 			switch (parts[1]){
 			case "view":
 				viewItemsMenuEvent(event, market);
 				break;
 			case "buy":
 				buyMenuEvent(event, market);
 				break;
 			case "confirm":
 				confirmMenuEvent(event, market);
 				break;
 			}
 			break;
 		case "free":
 			switch (parts[1]){
 			case "view":
 				viewItemsMenuEvent(event, market);
 				break;
 			case "buy":
 				buyMenuEvent(event, market);
 				break;
 			case "sell":
 				sellMenuEvent(event, market);
 				break;
 			}
 			break;
 		case "options":
 			optionsMenuEvent(event, market);
 			break;
 		case "admin":
 			adminMenuEvent(event, market);
 			break;
 		}
 		
 		//cancel the event (Stop the player from picking up items)
 		event.setResult(org.bukkit.event.Event.Result.DENY);
         event.setCancelled(true);
 	}
 
 
 	/**
 	 * Event for when a player closes out a market's inventory
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void marketClosed(InventoryCloseEvent event) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(event.getPlayer().getName());
 		if (playerInfo != null){
 			playerInfo.currentMarket = null;
 			playerInfo.temp = null;
 		}
 	}
 	
 	/**
 	 * The market's main menu
 	 * Acts as a gateway for the rest of the market
 	 * @param event
 	 * @param market
 	 */
 	private void mainMenuEvent(InventoryClickEvent event, Market market) {
 		Player player = (Player) event.getWhoClicked();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = event.getInventory();
 
 		// click was made in the player's inventory
 		if (event.getRawSlot() > event.getSlot()) {
 			return;
 		}
 		
 		//if view market items was pressed
 		if (event.getSlot() == MenuPainter.getLeft(inv, 0)){
 			//set the menu
 			playerInfo.menu = "market:view";
 			playerInfo.index = 0;
 			
 			//paint the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Viewing market items");
 			player.sendMessage("Page 0");
 		}
 		
 		//if requests menu was pressed
 		else if (event.getSlot() == MenuPainter.getLeft(inv, 1)){
 			//set the menu
 			playerInfo.menu = "request:view";
 			playerInfo.index = 0;
 			
 			//paint the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Viewing requested items.");
 			player.sendMessage("Page 0");
 		}
 		
 		//if free items menu was pressed
 		else if (event.getSlot() == MenuPainter.getLeft(inv, 2)){
 			//set the menu
 			playerInfo.menu = "free:view";
 			playerInfo.index = 0;
 			
 			//paint the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Viewing free items");
 			player.sendMessage("Page 0");
 		}
 		
 		//if options button was pressed
 		else if (event.getSlot() == MenuPainter.getLeft(inv, 3)){
 			//set the menu
 			playerInfo.menu = "options";
 			
 			//paint the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Options Menu");
 		}	
 		
 		//if admin button was pressed
 		else if (event.getSlot() == MenuPainter.getLeft(inv, 4)){
 			//set the menu
 			playerInfo.menu = "admin";
 			
 			//paint the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Admin Menu");
 		}
 		
 		//if help button was pressed
 		else if (event.getSlot() == MenuPainter.getLeft(inv, 5)){
 			HelpBookCreator.mainMenuHelp(player);
 			return;
 		}
 	}
 		
 	/**
 	 * What to do if the click was made while in a view iems menu, for market, request, and free
 	 * @param event
 	 *            the InventoryClickEvent
 	 * @param item
 	 */
 	private static void viewItemsMenuEvent(InventoryClickEvent event, Market market) {
 		Player player = (Player) event.getWhoClicked();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = event.getInventory();
 		String[] menu = playerInfo.menu.split(":");
 		
 		// click was made in the player's inventory
 		if (event.getRawSlot() > event.getSlot()) {
 			
 			//if request menu and book
 			if (menu[0].equals("request") && event.getCurrentItem().getType().equals(Material.WRITTEN_BOOK)){
 				//make sure the player is allowed to 
 				if (market.getNumSales(player.getName()) >= market.maxNumber){
 					player.sendMessage("You have too many things on this market.");
 					return;
 				}
 				
 				
 				//if the book has correct syntax
 				if (checkBook(player, event)){
 					//goto the confirm trade request
 					playerInfo.menu = menu[0].concat(":confirm");
 					player.sendMessage("Are you sure you want to request these items?");
 					MenuPainter.paintMenu(player);
 					return;
 				}
 			}
 			
 			//else do nothing
 			return;
 		}
 
 		// if previous button was pressed
 		if (event.getSlot() == MenuPainter.getLeft(inv, 0)) {
 			int index = playerInfo.index;
 
 			// make sure we are not out of bounds
 			if (index == 0) {
 				return;
 			}
 			
 			// re-adjust the index for the player
 			playerInfo.index = (index - 1);
 			
 			// redraw the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Page " + playerInfo.index);
 			
 			return;
 		}
 		
 		// if next button was pressed
 		if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
 			int index = playerInfo.index;
 
 			// re-adjust the index for the player
 			playerInfo.index = (index + 1);
 			
 			// redraw the inventory
 			MenuPainter.paintMenu(player);
 			
 			//notify the player
 			player.sendMessage("Page " + playerInfo.index);
 			return;
 		}
 
 		// if sell item button was pressed
 		if (event.getSlot() == MenuPainter.getRight(inv, 1) && !menu[0].equals("request")) {
 			if (market.getNumSales(player.getName()) >= market.maxNumber){
 				player.sendMessage("You have too many things on this market.");
 				return;
 			}
 			
 			// set chest up for item menu
 			player.sendMessage("Choose items you wish to sell");
 			player.sendMessage("Choose how many of each item");
 			
 			// set the player's menu
 			playerInfo.menu = menu[0].concat(":sell:quantity");
 			
 			// paint the sell menu
 			MenuPainter.paintMenu(player);
 			return;
 		}
 		
 		// if back button was pressed
 		if (event.getSlot() == MenuPainter.getRight(inv, 2)) {
 			// set chest up for item menu
 			player.sendMessage("Main Menu");
 			
 			// set the player's menu
 			playerInfo.menu = "main";
 			
 			// paint the sell menu
 			MenuPainter.paintMenu(player);
 			return;
 		}
 		
 		//if help button was pressed
 		else if (event.getSlot() == MenuPainter.getLeft(inv, 3)){
 			//which menu
 			if (playerInfo.menu.equals("market:view")){
 				HelpBookCreator.viewMarketHelp(player);
 			}
 			else if (playerInfo.menu.equals("request:view")){
 				HelpBookCreator.viewRequestHelp(player);
 			}
 			
 			return;
 			
 		}
 
 		// if another item was pressed that wasn't air
 		if (!event.getCurrentItem().getType().equals(Material.AIR)) {
 			int index = playerInfo.index;
 			
 			//if requests menu and player is seller, pick up any items
 			if (menu[0].equals("request") && market.getItem(index * 45 + event.getSlot(), menu[0]).getSeller().equals(player.getName())){
 				playerInfo.temp = (market.getItem(index * 45 + event.getSlot(), menu[0]));
 				player.sendMessage("This is your request");
 				if (pickupItem(player)){
 					MenuPainter.paintMenu(player);
 					playerInfo.temp = null;
 					return;
 				}
 			}
 			
 			//set the item being evaluated
 			playerInfo.temp = (market.getItem(index * 45 + event.getSlot(), menu[0]));
 			
 			// set the player's menu
 			playerInfo.menu = menu[0].concat(":buy");
 			
 			//set initial quantity to 1
 			playerInfo.unitQuantity = 1;
 			
 			// paint the buy item
 			MenuPainter.paintMenu(player);
 
 			//notify the player
 			player.sendMessage("Viewing a Sale");
 			return;
 		}
 
 	}
 
 	private static boolean checkBook(Player player, InventoryClickEvent event) {
 		ItemStack book = event.getCurrentItem();
 		BookMeta meta = (BookMeta)book.getItemMeta();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		
 		if (meta.getTitle().toLowerCase().equals("request")){
 			String page = meta.getPage(1);
 			if (page.equals(null)){
 				player.sendMessage("String is null");
 			}
 			
 			String[] fields = page.split(",");
 			if (fields.length == 4){
 				playerInfo.temp = new MarketSale(player.getName(),Integer.parseInt(fields[3]),Integer.parseInt(fields[2]),Integer.parseInt(fields[1]));
 				playerInfo.temp.addItem(new ItemStack(Integer.parseInt(fields[0])));
 				player.sendMessage("Request valid");
 				return true;
 			}
 			player.sendMessage("Wrong number of fields");
 			
 		}
 		
 		player.sendMessage("wrong title");
 		
 		return false;
 	}
 
 
 	/**
 	 * The sell menu. Called when a player wants to buy an item
 	 * 
 	 * @param event
 	 *            the InventoryClickEvent
 	 * @param
 	 */
 	private static void buyMenuEvent(InventoryClickEvent event, Market market) {
 		Player player = (Player) event.getWhoClicked();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = event.getInventory();
 		MarketSale marketSale = playerInfo.temp;
 
 		// click was made in the player's inventory
 		if (event.getRawSlot() > event.getSlot()) {
 			return;
 		}
 
 		// if click was to change a place in the value
 		for (int i = 0; i < 9; i++) {
 			int newValue;
 			
 			//decrement
 			if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {
 
 				newValue = (int) (playerInfo.unitQuantity - java.lang.Math.pow(10, i));
 				
 				// make sure the value is still positive
 				if (newValue < 1) {
 					playerInfo.unitQuantity = 1;
 					MenuPainter.paintMenu(player);
 					return;
 				}
 				
 				//set price
 				playerInfo.unitQuantity = newValue;
 
 				// repaint
 				MenuPainter.paintMenu(player);
 				return;
 			}
 			
 			//increment
 			if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {
 				
 				newValue = (int) (playerInfo.unitQuantity + java.lang.Math.pow(10, i));
 				
 				//make sure there is enough of all items
 				if (marketSale.getAvailiableUnits() < newValue){
 					newValue = marketSale.getAvailiableUnits();
 				}
 				
 				//make sure the price isn't over bounds
 				if (newValue > 999999999) {
 					newValue = 999999999;
 				}
 				
 				//set price
 				playerInfo.unitQuantity = newValue;
 
 				MenuPainter.paintMenu(player);
 				return;
 			}
 		}
 		
 		if (event.getSlot() == MenuPainter.getLeft(inv, 3)){
 			//which menu
 			if (playerInfo.menu.split(":")[0].equals("market")){
 				HelpBookCreator.buyHelp(player);
 			}
 			else if (playerInfo.menu.split(":")[0].equals("request")){
 				HelpBookCreator.fulfillHelp(player);
 			}
 			return;
 			
 		}
 
 		// buy button was pressed
 		if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
 			player.sendMessage("You pressed the buy button");
 			
 			//market
 			if (playerInfo.menu.split(":")[0].equals("market")){
 				buyItem(player);
 
 				// paint the chest for the main menu
 				playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 				
 				MenuPainter.paintMenu(player);	
 				
 				//update the menu of anyone who was viewing the new change
 				GUIM.updateMenu(market.getFullName(), playerInfo.menu);
 				
 				//rid the temp
 				playerInfo.temp = null;
 				
 				return;
 			}
 			
 			//request
 			if (playerInfo.menu.split(":")[0].equals("request")){
 				if (fulfillItem(player)){
 					// paint the chest for the main menu
 					playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 					
 					MenuPainter.paintMenu(player);
 					
 					//rid the temp
 					playerInfo.temp = null;
 				}
 				
 				
 
 				return;
 			}
 			
 			//free
 			if (playerInfo.menu.split(":")[0].equals("free")){
 				freeItem(player);
 
 				// paint the chest for the main menu
 				playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 				
 				MenuPainter.paintMenu(player);
 				
 				//rid the temp
 				playerInfo.temp = null;
 				
 				return;
 			}
 			
 
 		}
 
 		// player pressed the back button
 		else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
 			player.sendMessage("You pressed the back button");
 
 			// paint the chest for the main menu
 			playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 			
 			MenuPainter.paintMenu(player);	
 			return;
 		}
 	}
 
 	
 	private void confirmMenuEvent(InventoryClickEvent event, Market market) {
 		Player player = (Player) event.getWhoClicked();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = event.getInventory();
 
 		// click was made in the player's inventory
 		if (event.getRawSlot() > event.getSlot()) {
 			return;
 		}
 
 		// buy button was pressed
 		if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
 			player.sendMessage("You pressed the confirm button");
 			
 			//make sure player has funds
 			if (GUIM.economy.getBalance(player.getName()) < (playerInfo.temp.getUnitQuantity() * playerInfo.temp.getUnitPrice())){
 				player.sendMessage("You don't have the funds to back this request");
 				return;
 			}
 			
 			//remove funds
 			GUIM.economy.withdrawPlayer(player.getName(), (playerInfo.temp.getUnitQuantity() * playerInfo.temp.getUnitPrice()));
 			
 			//place the request on the market
 			GUIM.marketNames.get(playerInfo.currentMarket).requestedItems.add(playerInfo.temp);
 			playerInfo.temp = null;
 			
 			// paint the chest for the main menu
 			playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 			
 			MenuPainter.paintMenu(player);	
 			
 			//update the menu of anyone who was viewing the new change
 			GUIM.updateMenu(market.getFullName(), playerInfo.menu);
 			
 			int num = market.numSales.get(player.getName()); 
 			market.numSales.remove(player.getName());
 			market.numSales.put(player.getName(), num + 1);
 			
 			return;
 
 		}
 
 		// player pressed the back button
 		else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
 			player.sendMessage("You pressed the back button");
 
 			// paint the chest for the main menu
 			playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 			
 			MenuPainter.paintMenu(player);	
 			return;
 		}
 		
 	}
 	
 	/**
 	 * The sell menu. Called when a player wants to sell an item
 	 * 
 	 * @param event
 	 *            the InventoryClickEvent
 	 */
 	private static void sellMenuEvent(InventoryClickEvent event, Market market) {
 		Player player = (Player) event.getWhoClicked();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = event.getInventory();
 		MarketSale marketSale = playerInfo.temp;
 		
 		if (event.getSlot() == MenuPainter.getLeft(inv, 3)){
 			//which menu
 			HelpBookCreator.sellHelp(player);
 			return;
 		}
 		
 		//choose the correct stage (3 stages)
 		switch (playerInfo.menu.split(":")[2]){
 		//First stage: players choose items and quantities
 		case "quantity":
 			// create a market sale if one does not already exists
 			if (playerInfo.temp == null){
 				playerInfo.temp = (new MarketSale(player.getName()));
 			}	
 
 			// click was made in the player's inventory
 			if (event.getRawSlot() > event.getSlot()) {
 				
 
 				//do nothing if the player clicked an empty spot
 				if (event.getCurrentItem().getType() == Material.AIR){
 					return;
 				}
 				
 				//check for the max number of items
 				if (playerInfo.temp.getItems().size() >= 9){
 					player.sendMessage("Sale is full.");
 					return;
 				}
 				
 				// create the representative item
 				ItemStack item = new ItemStack(event.getCurrentItem());
 				item.setAmount(1);
 				
 				//check if that type is already part of the sale
 				for (ItemStack saleItem: playerInfo.temp.getItems()){
 					if (saleItem.equals(item)){
 						return;
 					}
 				}
 				
 				//place the representative item in the sale
 				playerInfo.temp.addItem(item);
 				
 				//make sure the previous quantity value is still valid
 				for (ItemStack saleItem: playerInfo.temp.getItems()){
 					if (countAmount(player, saleItem) < playerInfo.temp.getTotalQuantity()){
 						playerInfo.temp.setTotalQuantity(countAmount(player, saleItem));
 					}
 				}
 				
 				player.sendMessage("Item added to sale.");
 
 				// repaint
 				MenuPainter.paintMenu(player);
 
 				return;
 			}
 
 			// if click was to change a place in the value
 			for (int i = 0; i < 9; i++) {
 				int newValue;
 				
 				//decrement
 				if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {
 
 					newValue = (int) (marketSale.getTotalQuantity() - java.lang.Math.pow(10, i));
 					
 					// make sure the value is still positive
 					if (newValue < 1) {
 						marketSale.setTotalQuantity(1);
 						MenuPainter.paintMenu(player);
 						return;
 					}
 					
 					//set price
 					marketSale.setTotalQuantity(newValue);
 
 					// repaint
 					MenuPainter.paintMenu(player);
 					return;
 				}
 				
 				//increment
 				if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {
 					
 					newValue = (int) (marketSale.getTotalQuantity() + java.lang.Math.pow(10, i));
 					
 					//make sure there is enough of all items
 					for (ItemStack item: playerInfo.temp.getItems()){
 						if (countAmount(player, item) < newValue){
 							marketSale.setTotalQuantity(countAmount(player, item));
 							MenuPainter.paintMenu(player);
 							return;
 						}
 					}
 					
 					//make sure the price isn't over bounds
 					if (newValue > 999999999) {
 						marketSale.setTotalQuantity(999999999);
 						MenuPainter.paintMenu(player);
 						player.sendMessage("Value too high");
 						return;
 					}
 					
 					//set price
 					marketSale.setTotalQuantity(newValue);
 
 					MenuPainter.paintMenu(player);
 					return;
 				}
 			}
 
 			// continue button was pressed
 			if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
 				//market
 				if (playerInfo.menu.startsWith("market")){
 					// make sure there is an item to sell
 					if (playerInfo.temp.getItems().size() == 0) {
 						return;
 					}
 					
 					//set menuB to next sub menu
 					playerInfo.menu = playerInfo.menu.replace("quantity", "bulk");
 					
 					player.sendMessage("Quantity set. Now, choose how many items will be sold in a bundle.");
 					
 					//paint
 					MenuPainter.paintMenu(player);
 					
 				}
 				
 				//free
 				else if (playerInfo.menu.startsWith("free")){
 					// make sure there is an item to sell
 					if (playerInfo.temp.getItems().size() == 0) {
 						return;
 					}
 					
 					//set defaults
 					playerInfo.temp.setNumPerUnits(1);
 					playerInfo.temp.setPrice(0);
 					
 					//add free items
 					GUIM.marketNames.get(playerInfo.currentMarket).freeItems.add(playerInfo.temp);
 					
 					//remove from inventory
 					takeItems(player, playerInfo.temp, playerInfo.temp.getUnitQuantity());
 					
 					//set menuB to next sub menu
 					playerInfo.menu = "free:view";
 					
 					player.sendMessage("Free items added.");
 					
 					//paint
 					MenuPainter.paintMenu(player);
 					
 					//rid the temp
 					playerInfo.temp = null;
 					
 				}
 
 				return;
 			}
 
 			// player pressed the back button
 			else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
 				
 				//remove the toSell
 				playerInfo.temp = null;
 
 				// paint the chest for the main menu
 				playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 				MenuPainter.paintMenu(player);
 				
 				return;
 			}
 			
 			//player clicked a possible item spot
 			else if (event.getSlot() < 18){
 				//remove the item from the market sale
 				playerInfo.temp.removeItem(inv.getItem(event.getSlot()));
 				
 				//repaint
 				MenuPainter.paintMenu(player);
 				return;
 			}
 			break;
 			
 		//Second stage: players choose how much of the item(s) are considered one bulk
 		case "bulk":
 			// if click was to change a place in the value
 			for (int i = 0; i < 9; i++) {
 				int newValue;
 				
 				//decrement
 				if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {
 
 					newValue = (int) (marketSale.getNumPerUnits() - java.lang.Math.pow(10, i));
 					
 					// make sure the value is still positive
 					if (newValue < 1) {
 						marketSale.setNumPerUnits(1);
 						MenuPainter.paintMenu(player);
 						return;
 					}
 					
 					//set price
 					marketSale.setNumPerUnits(newValue);
 
 					// repaint
 					MenuPainter.paintMenu(player);
 					return;
 				}
 				
 				//increment
 				if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {
 					
 					newValue = (int) (marketSale.getNumPerUnits() + java.lang.Math.pow(10, i));
 					
 					//make sure new value isnt above total item count
 					if (newValue > marketSale.getTotalQuantity()){
 						marketSale.setNumPerUnits(marketSale.getTotalQuantity());
 						MenuPainter.paintMenu(player);
 						return;
 					}
 					
 					//make sure the price isn't over bounds
 					if (newValue > 999999999) {
 						marketSale.setNumPerUnits(999999999);
 						MenuPainter.paintMenu(player);
 						return;
 					}
 					
 					//set price
 					marketSale.setNumPerUnits(newValue);
 
 					MenuPainter.paintMenu(player);
 					return;
 				}
 			}
 			
 			// continue button was pressed
 			if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
 				
 				//make sure the bulk value divides into the quantity
 				if (marketSale.getTotalQuantity() % marketSale.getNumPerUnits() != 0){
 					player.sendMessage("Cannot choose this value. You will be left with items");
 					return;
 				}
 				
 				//set menuB to next sub menu
 				playerInfo.menu = playerInfo.menu.replace("bulk", "price");
 				
 				//paint
 				MenuPainter.paintMenu(player);
 				return;
 			}
 
 			// player pressed the back button
 			else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
 
 				// paint the chest for the main menu
 				playerInfo.menu = playerInfo.menu.replace("bulk", "quantity");
 				MenuPainter.paintMenu(player);
 				
 				return;
 			}
 			
 			break;
 		
 		//Third stage: players set price of 1 bulk
 		case "price":
 			// if click was to change a place in the value
 			for (int i = 0; i < 9; i++) {
 				int newValue;
 				
 				//decrement
 				if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {
 
 					newValue = (int) (marketSale.getUnitPrice() - java.lang.Math.pow(10, i));
 					
 					// make sure the value is still positive
 					if (newValue < 1) {
 						marketSale.setPrice(1);
 						MenuPainter.paintMenu(player);
 						return;
 					}
 					
 					//set price
 					marketSale.setPrice(newValue);
 
 					// repaint
 					MenuPainter.paintMenu(player);
 					return;
 				}
 				
 				//increment
 				if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {
 					
 					newValue = (int) (marketSale.getUnitPrice() + java.lang.Math.pow(10, i));
 					
 					//make sure the price isn't over bounds
 					if (newValue > 999999999) {
 						marketSale.setPrice(999999999);
 						MenuPainter.paintMenu(player);
 						return;
 					}
 					
 					//set price
 					marketSale.setPrice(newValue);
 
 					MenuPainter.paintMenu(player);
 					return;
 				}
 			}
 			
 			// continue button was pressed
 			if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
 				
 				saleMarketSale(player);
 				
 				playerInfo.temp = null;
 
 				// paint the chest for the main menu
 				playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
 				MenuPainter.paintMenu(player);	
 				
 				//update the menu of anyone who was viewing the new change
 				GUIM.updateMenu(market.getFullName(), playerInfo.menu);
 				
 				//rid the temp
 				playerInfo.temp = null;
 				return;
 			}
 
 			// player pressed the back button
 			else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
 				
 				// paint the chest for the main menu
 				playerInfo.menu = playerInfo.menu.replace("price", "bulk");
 				MenuPainter.paintMenu(player);
 				
 				return;
 			}
 			
 			break;
 		}
 		
 	}
 
 	private static boolean saleMarketSale(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		
 		//sell the sale on the market
 		MarketSale sale = playerInfo.temp;
 		for (ItemStack item: sale.getItems()){
 			if (!player.getInventory().containsAtLeast(item, sale.getTotalQuantity())){
 				player.sendMessage("SALE ABORTED. You did not have enough items in your inventory.");
 				playerInfo.temp = null;
 				return false;
 			}
 		}
 
 		takeItems(player, playerInfo.temp, playerInfo.temp.getUnitQuantity());
 		
 		GUIM.marketNames.get(playerInfo.currentMarket).marketItems.add(sale);
 		
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 		int num = market.numSales.get(player.getName()); 
 		market.numSales.remove(player.getName());
 		market.numSales.put(player.getName(), num + 1);
 		return true;
 		
 	}
 
 	private static int countAmount(Player player, ItemStack item) {
 		int i = 0;
 		for (ItemStack invItem: player.getInventory().getContents()){
 			if (invItem != null){
 				ItemStack item1 = new ItemStack(invItem);
 				ItemStack item2 = new ItemStack(item);
 				item1.setAmount(1);
 				item2.setAmount(1);
 				if (item1.equals(item2)){
 					i += invItem.getAmount();
 				}
 			}
 		}
 		return i;
 	}
 
 	private void optionsMenuEvent(InventoryClickEvent event, Market market) {
 		
 	}
 	
 	private void adminMenuEvent(InventoryClickEvent event, Market market) {
 		
 	}
 
 	/**
 	 * Player buys an item. Item is taken off of the market
 	 * 
 	 * @param player
 	 *            the player buying the item
 	 * @param item
 	 *            the item to be bought
 	 * @param inv
 	 *            the inventory of the market (to be repainted)
 	 */
 	private static void buyItem(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 		MarketSale marketSale = playerInfo.temp;
 		
 		if (market == null){
 			return;
 		}
 
 		// make sure the item wasn't quickly bought buy someone else
 		if (marketSale == null) {
 			player.sendMessage("I'm sorry, but this item has already been sold");
 			return;
 		}
 
 		// make sure the item wasn't quickly bought buy someone else
 		if (marketSale.getUnitQuantity() < playerInfo.unitQuantity) {
 			player.sendMessage("I'm sorry, but this was just sold.");
 			return;
 		}
 		
 		// if the player has room and can afford the item, buy it
 		if (!playerHasSpace(player, marketSale, playerInfo.unitQuantity)) {
 			player.sendMessage("I'm sorry, but you don't have enough space.");
 			return;
 		}
 
 		if ((!playerCanAfford(player, marketSale, playerInfo.unitQuantity))
 				&& (!player.getName().equals(marketSale.getSeller()))) {
 			player.sendMessage("You can't afford that.");
 			return;
 		}
 
 		// buy the item		
 		//add the items to the player's inventory
 		addItems(player, marketSale, playerInfo.unitQuantity);
 
 		//give the seller the money
 		GUIM.economy.depositPlayer(marketSale.getSeller(), playerInfo.unitQuantity * marketSale.getUnitPrice());
 		Player seller = Bukkit.getPlayer(marketSale.getSeller());
 				
 		if (seller != null){
 			seller.sendMessage(player.getName() + " just bought " + playerInfo.unitQuantity + " units of your sale:" + marketSale.toString());
 			seller.sendMessage("You made $" + playerInfo.unitQuantity * marketSale.getUnitPrice());
 		}
 				
 		//withdraw money from the purchaser
 		GUIM.economy.withdrawPlayer(player.getName(), playerInfo.unitQuantity * marketSale.getUnitPrice());
 		player.sendMessage("you bought " + playerInfo.unitQuantity + " units of the sale:" + marketSale.toString());
 		player.sendMessage("You paid $" + playerInfo.unitQuantity * marketSale.getUnitPrice());
 		
 		//remove items from the sale
 		marketSale.setTotalQuantity(marketSale.getTotalQuantity() - (playerInfo.unitQuantity * marketSale.getNumPerUnits()));
 
 		// remove the sale if it is depleted
 		if (marketSale.getTotalQuantity() <= 0){
 			for (MarketSale item: market.marketItems){
 				if (item.equals(marketSale)){
 					market.decrementPlayer(marketSale.getSeller());
 					market.marketItems.remove(item);
 					
 					break;
 				}
 			}
 		}
 	}
 	
 	private static boolean fulfillItem(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		MarketSale marketSale = playerInfo.temp;	
 		
 		//check if player has items
 		if (!playerhasItems(player, marketSale, playerInfo.unitQuantity)){
 			player.sendMessage("You do not have the items in your inventory.");
 			return false;
 		}
 		
 		//remove items from player
 		takeItems(player, marketSale, playerInfo.unitQuantity);
 		
 		//give the player the reward
 		GUIM.economy.depositPlayer(player.getName(), playerInfo.unitQuantity * marketSale.getUnitPrice());
 		player.sendMessage("You have been rewarded $" + playerInfo.unitQuantity * marketSale.getNumPerUnits());
 		
 		//add to the sale
 		marketSale.setFulfilled(marketSale.getFulfilled() + playerInfo.unitQuantity);
 		
 		//notify the seller if online
 		
 		return true;
 	}
 	
 	private static boolean pickupItem(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 		MarketSale marketSale = playerInfo.temp;	
 		
 		//check if request has been fulfilled
 		if (marketSale.getFulfilled() <= 0){
 			return false;
 		}
 		
 		//make sure player has room to pick up items
 		int bulks = marketSale.getFulfilled();
 		while (!playerHasSpace(player, marketSale, bulks)){
 			bulks--;
 			if (bulks <= 0){
 				player.sendMessage("Not enough room to pick up items");
 				return false;
 			}
 		}
 			
 		//give items to player
 		addItems(player, marketSale, bulks);
 		
 		//remove from the sale
 		marketSale.setPickedUp(marketSale.getPickedUp() + bulks);
 		marketSale.setFulfilled(marketSale.getFulfilled() - bulks);
 		
 		player.sendMessage("You picked up " + bulks + " units of your request");
 		
 		//check if the sale is done and picked up
 		if (marketSale.getPickedUp() == marketSale.getUnitQuantity()){
 			//remove the sale
 			market.decrementPlayer(marketSale.getSeller());
 			market.requestedItems.remove(marketSale);
 			GUIM.updateMenu(playerInfo.marketName, "request:view");
 			
 			return true;
 		}
 		
 		
 		return false;
 	}
 
 
 	private static boolean freeItem(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 		MarketSale marketSale = playerInfo.temp;
 		
 		//check if player has room
 		if (!playerHasSpace(player, marketSale, playerInfo.unitQuantity)){
 			player.sendMessage("You do not have enough space in your inventory.");
 			return false;
 		}
 		
 		//remove items from market
 		marketSale.setTotalQuantity(marketSale.getNumPerUnits() * (marketSale.getUnitQuantity() - playerInfo.unitQuantity));
 		if (marketSale.getTotalQuantity() <= 0){
 			market.freeItems.remove(marketSale);
 			GUIM.updateMenu(market.getFullName(), "free:view");
 		}
 		
 		//place items
 		return addItems(player, marketSale, playerInfo.unitQuantity);
 	}
 
 	/**
 	 * Returns whether or not a player can afford an item
 	 * 
 	 * @param player
 	 *            The player
 	 * @param item
 	 *            The item the player wants to buy
 	 * @param quantity 
 	 * @return whether or not a player can afford an item
 	 */
 	private static boolean playerCanAfford(Player player, MarketSale item, int quantity) {
 		
 		double money = GUIM.economy.getBalance(player.getName());
 
 		if (money >= (item.getUnitPrice() * quantity)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns whether or not a player has an open inventory space
 	 * 
 	 * @param player
 	 *            The player
 	 * @return whether or not a player has an open inventory space
 	 */
 	private static boolean playerHasSpace(Player player, MarketSale marketSale, int amount) {
 		int i;
 		for (ItemStack item: marketSale.getItems()){
 			i = marketSale.getNumPerUnits() * amount;
 			for (ItemStack invItem: player.getInventory().getContents()){
 				if (invItem == null){
 					i -= 64;
 				}
 				else if (invItem.isSimilar(item)){
 					i -= 64 - invItem.getAmount();
 				}
 			}
 			if (i > 0){
 				return false;
 			}
 			
 		}
 		return true;
 	}
 	
 	private static boolean playerhasItems(Player player, MarketSale marketSale, int amount){
 		int i;
 		//for each item in the marketsale
 		for (ItemStack item: marketSale.getItems()){
 			//calculate the correct amount of items
 			i = marketSale.getNumPerUnits() * amount;
 			//iterate through the player's inventory
 			for (ItemStack invItem: player.getInventory().getContents()){
 				//if similar item
 				if (item.isSimilar(invItem)){
 					//subtract what the player has
 					i -= invItem.getAmount();
 				}
 			}
 			//if we still needed items, return false.
 			if (i > 0){
 				return false;
 			}
 		}
 		//all items are accounted for. return true
 		return true;
 	}
 	
 	private static boolean addItems(Player player, MarketSale marketSale, int bulkAmount) {
 		Inventory inv = player.getInventory();
 		int i;
 		ItemStack newItem;
 		
 		//add items
 		//for each item in the sale
 		for (ItemStack item: marketSale.getItems()){
 			
 			i = marketSale.getNumPerUnits() * bulkAmount;
 			
 			player.sendMessage(i + " " + item.getItemMeta().getDisplayName() + " to add");
 			int j = 0;
 			//iterate though the player's inventory
 			for (ItemStack stack : inv.getContents()){
 				//if there is a stack
 				if (stack != null){
 					//if the items are similar
 					if (item.isSimilar(stack)){
 						//finish off this stack
 						if (i + stack.getAmount() <= 64){						
 							newItem = new ItemStack(stack);
 							newItem.setAmount(i + stack.getAmount());
 							inv.setItem(j, null);
 							inv.addItem(newItem);
 							i = 0;
 						}
 						//add the whole stack and keep going
 						else{
 							i -= 64 - stack.getAmount();
 							stack.setAmount(64);
 							newItem = new ItemStack(stack);
 							inv.setItem(j, null);
 							inv.addItem(newItem);
 						}
 					}
 				}
 				//if there is not a stack
 				else{
 					//finish off this stack
 					if (i <= 64){
 						player.sendMessage(item.toString());
 						newItem = new ItemStack(item);
 						newItem.setAmount(i);
 						player.sendMessage(newItem.toString());
 						inv.addItem(newItem);
 						i = 0;
 					}
 					//add the whole stack and keep going
 					else{
 						i -= 64;
 						newItem = new ItemStack(item);
 						newItem.setAmount(64);
 						inv.addItem(newItem);
 					}
 				}
 				
 				if (i == 0){
 					//finish this item
 					break;
 				}
 				j++;
 			}
 			//something went wrong
 			if (i != 0){
 				return false;
 			}
 			
 		}
 		//all items have been added
 		return true;
 	}
 	
 	/**
 	 * Takes the items specified in marketSale away from the players inventory, in bulkAmount bulks
 	 * @param player The player who is having items taken away
 	 * @param marketSale The sale which contains which items to take away from the player
 	 * @param bulkAmount The number of bulk sales to take away from the player
 	 * @return true if successful, false if unsuccessful
 	 */
 	private static boolean takeItems(Player player, MarketSale marketSale, int bulkAmount) {
 		Inventory inv = player.getInventory();
 		int i;
 		ItemStack newItem;
 		
 		//take items
 		//for each item in the sale
 		for (ItemStack item: marketSale.getItems()){
 			i = marketSale.getNumPerUnits() * bulkAmount;
 			int j = 0;
 			//iterate though the player's inventory
 			for (ItemStack stack : inv.getContents()){
 				//if there is a stack
 				if (stack != null){
 					//if the items are similar
 					if (item.isSimilar(stack)){
 						//if we do not need to take the whole stack
 						if (i < stack.getAmount()){
 							
 							stack.setAmount(stack.getAmount() - i);
 							newItem = new ItemStack(stack);
 							inv.setItem(j, null);
 							inv.addItem(newItem);
 							i -= i;
 						}
 						//remove the whole stack and keep going
 						else{
 							i -= stack.getAmount();
 							inv.setItem(j, null);
 						}
 					}
 				}
 				
 				if (i == 0){
 					//finish this item
 					break;
 				}
 				j++;
 			}
 			//something went wrong
 			if (i != 0){
 				return false;
 			}
 			
 		}
 		//all items have been added
 		return true;
 	}
 
 
 	public void setupMarket(Player player, String name) {
 		
 		//make sure player has permission
 		if (!player.hasPermission("GUIM.create")){
 			player.sendMessage("Sorry, you do not have permission to do that.");
 			return;
 		}
 		
 		//make sure that marketname is not already in use
 		if (GUIM.marketNames.get(player.getName() + "--" + name) != null){
 			player.sendMessage("You already have a market with that name.");
 			return;
 		}
 		
 		player.sendMessage("Please right-click the block which you would like to make into a market.");
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		if (playerInfo == null){
 			GUIM.addPlayerInfo(player.getName());
 			playerInfo = GUIM.getPlayerInfo(player.getName());
 		}
 		
 		playerInfo.creatingMarket = true;
 		playerInfo.marketName = name;
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void setupMarket(PlayerInteractEvent event) {
 		//get the player and the PlayerInfo
 		Player player = event.getPlayer();
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		
 		
 		if (playerInfo == null){
 			return;
 		}
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
 			if (playerInfo.creatingMarket == true){
 				event.setCancelled(true);
 				HashSet<Location> locations = new HashSet<Location>();
 				locations.add(event.getClickedBlock().getLocation());
 				
 				Market m = new Market(player.getName(), playerInfo.marketName, locations, new HashMap<String, Integer>(), plugin);
 				GUIM.marketNames.put(m.getFullName(), m);
 				GUIM.marketLocations.put(event.getClickedBlock().getLocation(), m);
 				playerInfo.creatingMarket = false;
 				playerInfo.marketName = null;
 				playerInfo.accessBlock = null;
 				player.sendMessage("Market has been created. Right-Click the market to access");
 			}
 		}
 		
 
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void preventBlocks(org.bukkit.event.entity.EntityExplodeEvent event){
 		for (Block block: event.blockList()){
 			if (GUIM.marketLocations.containsKey(block.getLocation())){
 				event.blockList().remove(block);
 			}
 		}
 		
 		
 	}
 	
 
 }
