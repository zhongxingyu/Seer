 package com.github.derwisch.paperMail;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
  
 public class PaperMailListener implements Listener {
 	
     @EventHandler
     public void playerJoin(PlayerJoinEvent event) {
 		Inbox inbox = Inbox.GetInbox(event.getPlayer().getName());
 		if (inbox == null) {
 			Inbox.AddInbox(event.getPlayer().getName());
 		}
     }
     
     @EventHandler
     public void onClick(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         ItemStack itemInHand = player.getItemInHand();
         ItemMeta inHandMeta = itemInHand.getItemMeta();
         if (itemInHand != null && inHandMeta != null && itemInHand.getType() == Material.getMaterial(Settings.MailItemID) && itemInHand.getDurability() == Settings.MailItemDV && inHandMeta.getDisplayName().equals((ChatColor.WHITE + Settings.MailItemName + ChatColor.RESET)) && player.hasPermission(Permissions.SEND_ITEM_PERM)) {
         	new PaperMailGUI(player, true).Show();
         }
         
         Block clickedBlock = event.getClickedBlock(); 
         if(clickedBlock != null && clickedBlock.getState() instanceof Sign) {
             Sign s = (Sign)event.getClickedBlock().getState();
             if(s.getLine(1).toLowerCase().contains("[mailbox]")) {
                 if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                 	new PaperMailGUI(player).Show();
                 } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                 	Inbox inbox = Inbox.GetInbox(player.getName());
                 	inbox.openInbox();
                 	event.setCancelled(true);
                 }
             }
         }
     }
     
     @EventHandler
     public void onInventoryClick_MailGUI_Send(InventoryClickEvent event) {
     	if (event.getInventory().getName() != PaperMail.NEW_MAIL_GUI_TITLE)
     		return;
     	double ItemCost = Settings.ItemCost;
     	Inventory inventory = event.getInventory();
     	Player player = ((Player)inventory.getHolder());
     	double numItems = 0;
     	PaperMailGUI itemMailGUI = PaperMailGUI.GetGUIfromPlayer((Player)inventory.getHolder());
     	ItemStack currentItem = event.getCurrentItem();
     	ItemMeta currentItemMeta = (currentItem == null) ? null : currentItem.getItemMeta();
     	ItemStack recipientItem = inventory.getItem(0);
     	boolean writtenBookBoolean = (recipientItem != null && recipientItem.getType() == Material.WRITTEN_BOOK);
     	for (int i = 0; i < inventory.getSize(); i++) {
     		ItemStack CraftStack = inventory.getItem(i);
     		if (CraftStack != null)
     		{
     		ItemMeta itemMeta = CraftStack.getItemMeta();
     		if (itemMeta.getDisplayName() != PaperMailGUI.SEND_BUTTON_ON_TITLE && 
     				itemMeta.getDisplayName() != PaperMailGUI.CANCEL_BUTTON_TITLE && 
     				itemMeta.getDisplayName() != PaperMailGUI.ENDERCHEST_BUTTON_TITLE &&
     				CraftStack.getType() != Material.WRITTEN_BOOK && 
     				CraftStack != null) {
     				numItems++;
     		}
     	}}
     	if(Settings.PerItemCosts == true)
     		ItemCost = numItems * Settings.ItemCost;
     	if (currentItemMeta != null && currentItemMeta.getDisplayName() == PaperMailGUI.SEND_BUTTON_ON_TITLE) {
     		if ((Settings.EnableMailCosts == true) && (writtenBookBoolean) && (ItemCost != 0) && (!player.hasPermission(Permissions.COSTS_EXEMPT))){
                 if (PaperMailEconomy.hasMoney(ItemCost, player) == true){
                	sendMail(itemMailGUI, event, inventory);
             }else if(PaperMailEconomy.hasMoney(ItemCost, player) == false){
                     player.sendMessage(ChatColor.RED + "Not enough money to send your mail, items not sent!");
                     itemMailGUI.Result = SendingGUIClickResult.CANCEL;
             		itemMailGUI.close();
             		itemMailGUI.SetClosed();
             		event.setCancelled(true);
     		}}  
             if((writtenBookBoolean) && (Settings.EnableMailCosts == false)) {
            	sendMail(itemMailGUI, event, inventory);
     		}
             if (!writtenBookBoolean) {
     			((Player)inventory.getHolder()).sendMessage(ChatColor.RED + "No recipient defined" + ChatColor.RESET);
 	    		event.setCancelled(true);
 	    		}
             if(player.hasPermission(Permissions.COSTS_EXEMPT) && (writtenBookBoolean)){
            	sendMail(itemMailGUI, event, inventory);
             }
     }
  }
     
     @EventHandler
     public void onInventoryClick_MailGUI_Cancel(InventoryClickEvent event) {
     	if (event.getInventory().getName() != PaperMail.NEW_MAIL_GUI_TITLE)
     		return;
     	
     	Inventory inventory = event.getInventory();
     	PaperMailGUI itemMailGUI = PaperMailGUI.GetGUIfromPlayer((Player)inventory.getHolder());
     	ItemStack currentItem = event.getCurrentItem();
     	ItemMeta currentItemMeta = (currentItem == null) ? null : currentItem.getItemMeta();
 
     	if (currentItemMeta != null && currentItemMeta.getDisplayName() == PaperMailGUI.CANCEL_BUTTON_TITLE) {
     		itemMailGUI.Result = SendingGUIClickResult.CANCEL;
     		itemMailGUI.close();
     		event.setCancelled(true);
     	}
     }
     
     @EventHandler
     public void onInventoryClick_MailGUI_Enderchest(InventoryClickEvent event) {
     	if (event.getInventory().getName() != PaperMail.NEW_MAIL_GUI_TITLE)
     		return;
     	
     	ItemStack currentItem = event.getCurrentItem();
     	ItemMeta currentItemMeta = (currentItem == null) ? null : currentItem.getItemMeta();
     	
     	if (currentItemMeta != null && currentItemMeta.getDisplayName() == PaperMailGUI.ENDERCHEST_BUTTON_TITLE) {
     		Player player = (Player)event.getInventory().getHolder();
     		PaperMailGUI itemMailGUI = PaperMailGUI.GetGUIfromPlayer(player);
     		itemMailGUI.Result = SendingGUIClickResult.OPEN_ENDERCHEST;
     		event.setCancelled(true);
     		OpenInventory(player, player.getEnderChest());
     	}
     }
 
     private static Player player = null;
     private static Inventory inventory = null;    
     public static void OpenInventory(Player player, Inventory inventory) {
     	PaperMailListener.player = player;
     	PaperMailListener.inventory = inventory;
     	PaperMail.server.getScheduler().scheduleSyncDelayedTask(PaperMail.instance, new Runnable() {
     	    public void run() {
     	    	openInventory(PaperMailListener.player, PaperMailListener.inventory);
     	    }
     	}, 0L);
     }
     
     private static void openInventory(Player player, Inventory inventory) {
 		player.closeInventory();
     	player.openInventory(inventory);
     }
     
     @EventHandler
     public void onInventoryClick_MailGUI_Recipient(InventoryClickEvent event) {
     	if (event.getInventory().getName() != PaperMail.NEW_MAIL_GUI_TITLE)
     		return;
     	
     	ItemStack currentItem = event.getCurrentItem();
     	ItemStack cursorItem = event.getCursor();
     	ItemMeta currentItemMeta = (currentItem == null) ? null : currentItem.getItemMeta();
 
     	if (currentItemMeta != null && currentItemMeta.getDisplayName() == PaperMailGUI.RECIPIENT_TITLE) {
     		if (cursorItem.getType() == Material.WRITTEN_BOOK) {
 	    		event.setCurrentItem(cursorItem);
 	    		event.setCursor(new ItemStack(Material.AIR));
 	    		event.setCancelled(true);
     		} else {
         		event.setCancelled(true);
         	}
     	}
     }
 
     @EventHandler
     public void onInventoryClose(InventoryCloseEvent event) {
     	Inventory inventory = event.getInventory();
     	
     	if (inventory.getName() == PaperMail.NEW_MAIL_GUI_TITLE) {
     		Player player = (Player) event.getPlayer();
     		PaperMailGUI gui = PaperMailGUI.GetGUIfromPlayer(player);
     		World world = player.getWorld();
     		Location playerLocation = player.getLocation();
     		if ((gui.Result == SendingGUIClickResult.CANCEL) && (gui.Result != null)) {
     			gui.SetClosed();
     			ItemStack[] inventoryContents = inventory.getContents();
     			if (inventoryContents != null) {
 		    		for (ItemStack itemStack : inventoryContents) {
 		    			if (itemStack == null)
 		    				continue;
 		    			
 		    			ItemMeta itemMeta = itemStack.getItemMeta();
 		    			if (itemMeta != null &&
 		    					(itemMeta.getDisplayName() == PaperMailGUI.CANCEL_BUTTON_TITLE ||
 		    					itemMeta.getDisplayName() == PaperMailGUI.ENDERCHEST_BUTTON_TITLE ||
 		    					itemMeta.getDisplayName() == PaperMailGUI.RECIPIENT_TITLE ||
 		    					itemMeta.getDisplayName() == PaperMailGUI.SEND_BUTTON_ON_TITLE)) {
 		    				continue;
 		    			}
 		    			world.dropItemNaturally(playerLocation, itemStack);    			
 		    		}
     			}
     		}
     		
     	}
 
     	if (inventory.getName() == PaperMail.INBOX_GUI_TITLE) {
     		Player player = (Player) event.getPlayer();
     		Inbox inbox = Inbox.GetInbox(player.getName());
     		inbox.SaveInbox();
     	}
     	
     	
     	if (inventory.getType().toString() == "ENDER_CHEST") {
         	Player player = (Player)event.getPlayer();
         	PaperMailGUI gui = PaperMailGUI.GetOpenGUI(player.getName());
         	if (gui != null) {
         		OpenInventory(player, gui.Inventory);
         		gui.Result = SendingGUIClickResult.CANCEL;
         	}
     		
     	}
     }
    public void sendMail(PaperMailGUI itemMailGUI, InventoryClickEvent event, Inventory inventory){
     	itemMailGUI.Result = SendingGUIClickResult.SEND;
 		itemMailGUI.SendContents();
 		itemMailGUI.close();
 		itemMailGUI.SetClosed();
 		event.setCancelled(true);
     	itemMailGUI = null;
    	PaperMailGUI.RemoveGUI(((Player) inventory.getHolder()).getName());
    	
     }
 }
