 package com.github.derwisch.paperMail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
 //import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class PaperMailGUI {
 
 	public static final String RECIPIENT_TITLE = ChatColor.RED + "Recipient" + ChatColor.RESET;
 	public static final String SEND_BUTTON_ON_TITLE = ChatColor.WHITE + "Send" + ChatColor.RESET;
 	public static final String CANCEL_BUTTON_TITLE = ChatColor.WHITE + "Cancel" + ChatColor.RESET;
 	public static final String ENDERCHEST_BUTTON_TITLE = ChatColor.WHITE + "Open Enderchest" + ChatColor.RESET;
 	
 	private static ArrayList<PaperMailGUI> itemMailGUIs = new ArrayList<PaperMailGUI>();
 	private static Map<String, PaperMailGUI> openGUIs = new HashMap<String, PaperMailGUI>();
 	
 	public Inventory Inventory;
 	public Player Player;
 	public static boolean cancel = false;
 
 	private ItemStack recipientMessage; 
 	private ItemStack sendButtonEnabled;
 	private ItemStack cancelButton; 
 	private ItemStack enderChestButton;
 	private boolean paperSent;
 	
 	public SendingGUIClickResult Result = SendingGUIClickResult.CANCEL;
 	
 	public static void RemoveGUI(String playerName) {
 		openGUIs.put(playerName, null);
 		openGUIs.remove(playerName);
 	}
 	
 	public static PaperMailGUI GetOpenGUI(String playerName) {
 		return openGUIs.get(playerName);
 	}
 	
 	public PaperMailGUI(Player player) {
 		this.paperSent = false;
 		Player = player;
 		Inventory = Bukkit.createInventory(player, Settings.MailWindowRows * 9, PaperMail.NEW_MAIL_GUI_TITLE);
 		initializeButtons();
     	itemMailGUIs.add(this);
 	}
 
 	public PaperMailGUI(Player player, boolean paperSent) {
 		this.paperSent = paperSent;
 		Player = player;
 		Inventory = Bukkit.createInventory(player, Settings.MailWindowRows * 9, PaperMail.NEW_MAIL_GUI_TITLE);
 		initializeButtons();
     	itemMailGUIs.add(this);
 	}
 	
 	private void initializeButtons() {
 		recipientMessage = new ItemStack(Material.PAPER);
 		sendButtonEnabled = new ItemStack(Material.WOOL);
 		cancelButton = new ItemStack(Material.WOOL);
 		enderChestButton = new ItemStack(Material.ENDER_CHEST);
 
     	sendButtonEnabled.setDurability((short)5);
     	cancelButton.setDurability((short)14);
 
     	ItemMeta recipientMessageMeta = recipientMessage.getItemMeta();
     	ItemMeta sendButtonEnabledMeta = sendButtonEnabled.getItemMeta();
     	ItemMeta cancelButtonMeta = cancelButton.getItemMeta();
     	ItemMeta enderChestButtonMeta = enderChestButton.getItemMeta();
     	
     	ArrayList<String> recipientMessageLore = new ArrayList<String>();
     	ArrayList<String> sendButtonDisabledLore = new ArrayList<String>();
     	ArrayList<String> enderChestButtonLore = new ArrayList<String>();
 
     	recipientMessageLore.add(ChatColor.GRAY + "Add a written book named" + ChatColor.RESET);
     	recipientMessageLore.add(ChatColor.GRAY + "like a player to define" + ChatColor.RESET);
     	recipientMessageLore.add(ChatColor.GRAY + "the recipient." + ChatColor.RESET);
 
     	sendButtonDisabledLore.add(ChatColor.GRAY + "State a recipient before" + ChatColor.RESET);
     	sendButtonDisabledLore.add(ChatColor.GRAY + "sending" + ChatColor.RESET);
 
     	enderChestButtonLore.add(ChatColor.GRAY + "Grants access to your" + ChatColor.RESET);
     	enderChestButtonLore.add(ChatColor.GRAY + "enderchest." + ChatColor.RESET);
     	enderChestButtonLore.add(ChatColor.GRAY + "You return to the mail after" + ChatColor.RESET);
     	enderChestButtonLore.add(ChatColor.GRAY + "closing the enderchest" + ChatColor.RESET);
     	
     	recipientMessageMeta.setDisplayName(RECIPIENT_TITLE);
     	recipientMessageMeta.setLore(recipientMessageLore);
 
     	sendButtonEnabledMeta.setDisplayName(SEND_BUTTON_ON_TITLE);
 
     	cancelButtonMeta.setDisplayName(CANCEL_BUTTON_TITLE);
     	
     	enderChestButtonMeta.setDisplayName(ENDERCHEST_BUTTON_TITLE);
     	enderChestButtonMeta.setLore(enderChestButtonLore);
 
     	recipientMessage.setItemMeta(recipientMessageMeta);
     	sendButtonEnabled.setItemMeta(sendButtonEnabledMeta);
     	cancelButton.setItemMeta(cancelButtonMeta);
     	enderChestButton.setItemMeta(enderChestButtonMeta);
 
     	Inventory.setItem(0, recipientMessage);
     	if (Settings.EnableEnderchest) {
     		Inventory.setItem(8, enderChestButton);
     	}
     	Inventory.setItem(((Settings.MailWindowRows - 1) * 9) - 1, sendButtonEnabled);
     	Inventory.setItem((Settings.MailWindowRows * 9) - 1, cancelButton);
 	}
 	
 	public void Show() {
 		if (Settings.EnableItemMail) {
 			Player.openInventory(Inventory);
 			openGUIs.put(Player.getName(), this);
 		}
 	}
 	
 	public void SetClosed() {
 		RemoveGUI(Player.getName());
 		itemMailGUIs.remove(this);
 	}
 		
 	public void close() {
 		Player.closeInventory();
 	}
 	
 	public void SendContents() {
 		Player player = this.Player;
 		ArrayList<ItemStack> sendingContents = new ArrayList<ItemStack>();
 		String playerName = "";
 		int numItems = 0;
 		double itemCost = Settings.ItemCost;
 		for (int i = 0; i < Inventory.getSize(); i++) {
 			
 			ItemStack CraftStack = Inventory.getItem(i);
 			if (CraftStack == null)
 				continue;
 			
 			ItemMeta itemMeta = CraftStack.getItemMeta();
 			if (itemMeta.getDisplayName() != SEND_BUTTON_ON_TITLE && 
 				itemMeta.getDisplayName() != CANCEL_BUTTON_TITLE && 
 				itemMeta.getDisplayName() != ENDERCHEST_BUTTON_TITLE &&
 				CraftStack.getType() != Material.WRITTEN_BOOK) {
 				sendingContents.add(CraftStack);
 				numItems = numItems + CraftStack.getAmount();
 			}
 			if (CraftStack.getType() == Material.WRITTEN_BOOK && playerName == "") {
 				BookMeta bookMeta = (BookMeta)itemMeta;
 				Player p = Bukkit.getPlayer(bookMeta.getTitle());
 				if (p != null) {
 					playerName = p.getName();
 				    } else {
				    OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
 				    if (op != null) {
 				    	playerName = op.getName();
 				        } else {
 				        	playerName = bookMeta.getTitle();
 				        	player.sendMessage(ChatColor.DARK_RED + "Player "  + playerName + " may not exist or doesn't have an Inbox yet. Creating Inbox for player " + playerName + ChatColor.RESET);
 				        }
 				    }
 			}
 		}
 			Inbox inbox = Inbox.GetInbox(playerName);
 			inbox.AddItems(sendingContents, Player);	
 		if ((Settings.EnableMailCosts == true) && (Settings.PerItemCosts == true) && (Settings.ItemCost != 0) && (!this.Player.hasPermission(Permissions.COSTS_EXEMPT))){
 			itemCost = numItems * Settings.ItemCost;
 			PaperMailEconomy.takeMoney(itemCost, player);
 		}
 		if ((Settings.EnableMailCosts == true) && (Settings.PerItemCosts == false) && (Settings.ItemCost != 0) && (!this.Player.hasPermission(Permissions.COSTS_EXEMPT))){
 			PaperMailEconomy.takeMoney(itemCost, player);
 		}
 		if (paperSent) {
 			ItemStack itemInHand = Player.getInventory().getItemInHand();
 			itemInHand.setAmount(itemInHand.getAmount() - 1);
 			Player.setItemInHand(itemInHand);
 		}
 		Player.sendMessage(ChatColor.DARK_GREEN + "Message sent!" + ChatColor.RESET);
 	}
 	
 	public static PaperMailGUI GetGUIfromPlayer(Player player) {
 		for (PaperMailGUI gui : itemMailGUIs) {
 			if (gui.Player == player)
 				return gui;
 		}
 		return null;
 	}
 	
 	
 	
 }
