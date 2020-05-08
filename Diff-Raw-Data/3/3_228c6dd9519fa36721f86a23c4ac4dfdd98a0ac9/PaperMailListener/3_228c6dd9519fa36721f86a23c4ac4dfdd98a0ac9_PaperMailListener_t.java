 package com.github.derwisch.paperMail;
 
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
  
 public class PaperMailListener implements Listener {
 	
 	public double sendAmount = 0;
 	
     @EventHandler
     public void playerJoin(PlayerJoinEvent event) throws IOException, InvalidConfigurationException {
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
                 	Inbox inbox = null;
 					try {
 						inbox = Inbox.GetInbox(player.getName());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (InvalidConfigurationException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
                 	inbox.openInbox();
                 	event.setCancelled(true);
                 }
             }
         }
     }
     
     @EventHandler
     public void onInventoryClick_MailGUI_Send(InventoryClickEvent event) throws IOException, InvalidConfigurationException {
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
     	boolean cancel = false;
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
     		if ((Settings.EnableMailCosts == true) && (writtenBookBoolean) && (ItemCost != 0) && (!player.hasPermission(Permissions.COSTS_EXEMPT)) && (sendAmount > 1)){
                 if (PaperMailEconomy.hasMoney(ItemCost, player) == true){
                 	cancel = false;
                 }else if(PaperMailEconomy.hasMoney(ItemCost, player) == false){
                     player.sendMessage(ChatColor.RED + "Not enough money to send your mail, items not sent!");
                     itemMailGUI.Result = SendingGUIClickResult.CANCEL;
             		itemMailGUI.close();
             		itemMailGUI.SetClosed();
             		event.setCancelled(true);
             		cancel = true;
                 }
             }
     		if((Settings.EnableSendMoney == true) && (PaperMailEconomy.hasMoney(sendAmount, player) == false) && (sendAmount > 1))
     		{
     			player.sendMessage(ChatColor.DARK_RED + "You are trying to send more money than you have! Sending cancelled!");
     			itemMailGUI.Result = SendingGUIClickResult.CANCEL;
         		itemMailGUI.close();
         		itemMailGUI.SetClosed();
         		event.setCancelled(true);
         		cancel = true;
     		}
             if (!writtenBookBoolean) {
     			((Player)inventory.getHolder()).sendMessage(ChatColor.RED + "No recipient defined" + ChatColor.RESET);
     			itemMailGUI.Result = SendingGUIClickResult.CANCEL;
         		itemMailGUI.close();
         		itemMailGUI.SetClosed();
         		event.setCancelled(true);
         		cancel = true;
 	    		}
             if(writtenBookBoolean && ((Settings.EnableMailCosts == false) || (Settings.ItemCost == 0) || player.hasPermission(Permissions.COSTS_EXEMPT)) && (cancel != true)) {
             	cancel = false;
     		}
             if (cancel != true)
             {
             	sendMail(itemMailGUI, event, inventory);
             	itemMailGUI.close();
         		itemMailGUI.SetClosed();
         		event.setCancelled(true);
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
     	Player player = (Player) event.getView().getPlayer();
     	if (currentItemMeta != null && currentItemMeta.getDisplayName() == PaperMailGUI.RECIPIENT_TITLE) {
     		event.setCurrentItem(null);
     		if (cursorItem.getType() == Material.WRITTEN_BOOK) {
     			event.setCurrentItem(cursorItem);
 	    		event.setCursor(new ItemStack(Material.AIR));
 	    		player.updateInventory();
 	    		event.setCancelled(true);
     		} else {
         		event.setCancelled(true);
         	}
     	}
     }
 
     @EventHandler
     public void onInventoryClose(InventoryCloseEvent event) throws IOException, InvalidConfigurationException {
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
 		    					itemMeta.getDisplayName() == PaperMailGUI.SEND_BUTTON_ON_TITLE ||
 		    					itemMeta.getDisplayName() == PaperMailGUI.RECIPIENT_TITLE) ||
 		    					itemMeta.getDisplayName() == PaperMailGUI.MONEY_SEND_BUTTON_TITLE) {
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
     @SuppressWarnings("deprecation")
 	@EventHandler
     public void onInventoryClick_MailGUI_sendMoney(InventoryClickEvent event) {
     	Inventory inv = event.getInventory();
     	inv.setMaxStackSize(127);
     	Player player = ((Player)inv.getHolder());
     	ItemStack currentItem = event.getCurrentItem();
     	ItemMeta currentItemMeta = (currentItem == null) ? null : currentItem.getItemMeta();
     	if (event.getInventory().getName() != PaperMail.NEW_MAIL_GUI_TITLE)
     		return;
     	if ((inv.getName() != null) && (inv.getName() == PaperMail.NEW_MAIL_GUI_TITLE)) {
     		if ((currentItemMeta != null) && (currentItemMeta.getDisplayName() == PaperMailGUI.MONEY_SEND_BUTTON_TITLE) && (event.getClick().isLeftClick())) {
     			if(currentItem.getAmount() == 0){
     				currentItem.setAmount(1);
     			}else{
     			currentItem.setAmount(currentItem.getAmount() + Settings.Increments);
     			}
     			player.updateInventory();           
     			event.setCancelled(true);
     		}
     		if ((currentItemMeta != null) && (currentItemMeta.getDisplayName() == PaperMailGUI.MONEY_SEND_BUTTON_TITLE) && (event.getClick().isRightClick())) {
     			if(currentItem.getAmount() == 0){
     				currentItem.setAmount(1);
     			}else{
     			currentItem.setAmount(currentItem.getAmount() - Settings.Increments);
     			}
     			player.updateInventory();
     			event.setCancelled(true);
     		}
     	}
     	sendAmount = currentItem.getAmount();
     }
     
     //Create Method On Right Click Item Bank Note Deposit
     @EventHandler
     public void onPlayerUse(PlayerInteractEvent event){
     	Player p = event.getPlayer();
    	if ((p.getItemInHand().hasItemMeta()) && (p.getItemInHand().getItemMeta().hasDisplayName())){
     	if((p.getItemInHand() != null) && (p != null) && (p.getItemInHand().getItemMeta().getDisplayName() != null) && (event.getAction() != null) && (p.getItemInHand() != new ItemStack(Material.AIR)) && (!event.getAction().equals(Action.LEFT_CLICK_AIR)) && (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && (event != null))
         {
         	if((p.getItemInHand().getItemMeta().getDisplayName().contains(PaperMailGUI.BANK_NOTE_DISPLAY)) && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (event.getAction().equals(Action.RIGHT_CLICK_AIR))){
         		ItemStack bankNote = p.getItemInHand();
         		ItemMeta noteMeta = bankNote.getItemMeta();
         		String noteTitle = noteMeta.getDisplayName();
         		String stringCash = noteTitle;
         		stringCash = stringCash.replace(PaperMailGUI.BANK_NOTE_DISPLAY + ChatColor.RED + "(" + ChatColor.GREEN + "$" + ChatColor.GOLD + ChatColor.RED +  ")" + ChatColor.RESET, "");
         		stringCash = stringCash.replaceAll("[^0-9]", "");
         		p.sendMessage(stringCash);
         		int fromString = java.lang.Integer.parseInt(stringCash);
         		p.sendMessage(Integer.toString(fromString));
         		double deposit = (double)fromString;
         		PaperMailEconomy.cashBankNote(p, deposit);
         		if(bankNote.getAmount() < 2)
         			{
         			p.setItemInHand(new ItemStack(Material.AIR));
         			}else{
         				bankNote.setAmount(bankNote.getAmount() - 1);
         				p.setItemInHand(bankNote);
         			}
         		event.setCancelled(true);
         		}
         	else if((p.getItemInHand() == null) || (p.getItemInHand() == new ItemStack(Material.AIR)) || (event.getAction().equals(Action.LEFT_CLICK_AIR)) || (event.getAction().equals(Action.LEFT_CLICK_BLOCK)));
         	{
         		event.setCancelled(true);
         	}
         }else {
         	event.setCancelled(true);
         }
    	}
         event.setCancelled(true);
     }
     
     public void sendMail(PaperMailGUI itemMailGUI, InventoryClickEvent event, Inventory inventory) throws IOException, InvalidConfigurationException{
     	itemMailGUI.Result = SendingGUIClickResult.SEND;
 		itemMailGUI.SendContents();
 		itemMailGUI.close();
 		itemMailGUI.SetClosed();
 		event.setCancelled(true);
     	itemMailGUI = null;
     	PaperMailGUI.RemoveGUI(((Player) inventory.getHolder()).getName());
     	
     }
 }
