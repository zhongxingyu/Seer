 package me.Sk8r2K10.sGift;
 
 import java.util.logging.Logger;
 import net.milkbowl.vault.item.ItemInfo;
 import net.milkbowl.vault.item.Items;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class GiftCommand implements CommandExecutor {
 
     private sGift plugin;
     Player player = null;
     String prefix = ChatColor.WHITE + "[" + ChatColor.GREEN + "sGift" + ChatColor.WHITE + "] ";
     Logger log = Logger.getLogger("Minecraft");
 
     public GiftCommand(sGift instance) {
 	plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 	if (sender instanceof Player) {
 
 	    player = (Player) sender;
 	} else {
 
 	    player = null;
 	}
 	if (commandLabel.equalsIgnoreCase("gift") && plugin.getPerms(player, "sgift.gift.gift")) {
 
 	    PluginDescriptionFile pdf = plugin.getDescription();
 	    String logpre = "[" + pdf.getName() + " " + pdf.getVersion() + "] ";
 
 	    if (plugin.getConfig().getBoolean("Features.enable-gift")) {
 		if (player == null) {
 
 		    log.warning(logpre + "Don't send sGift commands through console!");
 
 		} else if (args.length == 1) {
 		    if (args[0].equalsIgnoreCase("help") && plugin.getPerms(player, "sgift.gift.help")) {
 
 			player.sendMessage(ChatColor.DARK_GRAY + "----------------[" + ChatColor.GREEN + "sGift - Gift Help Menu" + ChatColor.DARK_GRAY + "]-----------------");
 			player.sendMessage(plugin.getConfig().getString("Help.Gift.Gift"));
 			player.sendMessage(plugin.getConfig().getString("Help.Gift.Example"));
 			player.sendMessage(plugin.getConfig().getString("Help.Gift.Accept"));
 			player.sendMessage(plugin.getConfig().getString("Help.Gift.Deny"));
 			player.sendMessage(plugin.getConfig().getString("Help.Gift.Cancel"));
 			player.sendMessage(plugin.getConfig().getString("Help.Gift.Help"));
 
 			if (player.hasPermission("sgift.admin")) {
 
 			    player.sendMessage(plugin.getConfig().getString("Help.Gift.Stop"));
 			}
 
 		    } else if (args[0].equalsIgnoreCase("accept") && plugin.getPerms(player, "sgift.gift.accept")) {
 
 			Gift gift = null;
 			Sender Sender1 = null;
 
 			for (Gift g : plugin.gifts) {
 
 			    if (g.Victim == player) {
 
 				gift = g;
 
 				for (Sender s : plugin.senders) {
 
 				    if (s.Sender == g.playerSender) {
 
 					Sender1 = s;
 				    }
 				}
 			    }
 			}
 
 			if (gift == null) {
 
 			    player.sendMessage(prefix + ChatColor.RED + "No Gifts to accept!");
 			} else {
 
 			    Player playerSendingItems = gift.playerSender;
 			    Player Victim = gift.Victim;
 			    ItemStack items = gift.itemStack;
 
 			    if (player.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = player.getLocation();
 				player.getWorld().dropItemNaturally(playerloc, items);
 
 				player.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + "!");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerSendingItems.getDisplayName() + ChatColor.WHITE + "!");
 
 				log.info(logpre + Victim.getDisplayName() + " recieved " + items.getAmount() + " " + Items.itemByStack(items).getName() + " from " + playerSendingItems.getDisplayName());
 
 				plugin.gifts.remove(gift);
 				plugin.senders.remove(Sender1);
 
 			    } else {
 
 				Victim.getInventory().addItem(items);
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + "!");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerSendingItems.getDisplayName() + ChatColor.WHITE + "!");
 
 				log.info(logpre + Victim.getDisplayName() + " recieved " + items.getAmount() + " " + Items.itemByStack(items).getName() + " from " + playerSendingItems.getDisplayName());
 
 				plugin.gifts.remove(gift);
 				plugin.senders.remove(Sender1);
 			    }
 
 
 			}
 
 		    } else if (args[0].equalsIgnoreCase("deny") && plugin.getPerms(player, "sgift.gift.deny")) {
 
 			Gift gift = null;
 			Sender Sender1 = null;
 
 			for (Gift g : plugin.gifts) {
 
 			    if (g.Victim == player) {
 
 				gift = g;
 
 				for (Sender s : plugin.senders) {
 
 				    if (s.Sender == g.playerSender) {
 
 					Sender1 = s;
 				    }
 				}
 			    }
 			}
 
 			if (gift == null) {
 
 			    player.sendMessage(prefix + ChatColor.RED + "No Gifts to deny!");
 			} else {
 
 			    Player playerSendingItems = gift.playerSender;
 			    Player Victim = gift.Victim;
 			    ItemStack items = gift.itemStack;
 
 			    if (playerSendingItems.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = playerSendingItems.getLocation();
 
 				playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
 				playerSendingItems.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Gift request!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Gift!");
 
 				log.info(logpre + Victim.getDisplayName() + " denied " + items.getAmount() + " " + Items.itemByStack(items).getName() + " from " + playerSendingItems.getDisplayName());
 
 				plugin.gifts.remove(gift);
 				plugin.senders.remove(Sender1);
 
 			    } else if (!playerSendingItems.isOnline()) {
 				player.sendMessage(prefix + ChatColor.RED + "Player sending items is not Online!");
 				player.sendMessage(prefix + ChatColor.RED + "Please wait for " + playerSendingItems.getName() + " to come back online!");
 
 			    } else {
 				playerSendingItems.getInventory().addItem(items);
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Gift request!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Gift!");
 
 				log.info(logpre + Victim.getDisplayName() + " denied " + items.getAmount() + " " + Items.itemByStack(items).getName() + " from " + playerSendingItems.getDisplayName());
 
 				plugin.gifts.remove(gift);
 				plugin.senders.remove(Sender1);
 			    }
 			}
 
 		    } else if (args[0].equalsIgnoreCase("stop") && plugin.getPerms(player, "sgift.sgift")) {
 			while (plugin.gifts.size() > 0) {
 
 			    Gift gift = null;
 			    Sender Sender1 = null;
 
 			    for (Gift g : plugin.gifts) {
 
 				if (g.itemStack != null) {
 				    gift = g;
 
 				    for (Sender s : plugin.senders) {
 
 					if (s.Sender != null) {
 
 					    Sender1 = s;
 					}
 				    }
 				}
 			    }
 
 			    if (gift == null) {
 
 				player.sendMessage(prefix + ChatColor.RED + "No Gifts to stop!");
 			    } else {
 
 				Player playerSendingItems = gift.playerSender;
 				Player Victim = gift.Victim;
 				ItemStack items = gift.itemStack;
 
 				if (playerSendingItems.getInventory().firstEmpty() == -1) {
 
 				    Location playerloc = playerSendingItems.getLocation();
 
 				    playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
 				    playerSendingItems.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				    playerSendingItems.sendMessage(prefix + ChatColor.RED + "Your Gift has been cancelled by an Admin!");
 				    playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.RED + " has been returned to you.");
 				    Victim.sendMessage(prefix + ChatColor.RED + "Admin cancelled your Gift.");
 				    log.info(logpre + "stopped a gift of " + items.getAmount() + " " + Items.itemByStack(items).getName() + " from " + playerSendingItems.getDisplayName());
 
 				    plugin.gifts.remove(gift);
 				    plugin.senders.remove(Sender1);
 
 				} else {
 
 				    playerSendingItems.getInventory().addItem(items);
 				    playerSendingItems.sendMessage(prefix + ChatColor.RED + "Your Gift has been cancelled by an Admin!");
 				    playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.RED + " has been returned to you.");
 				    Victim.sendMessage(prefix + ChatColor.RED + "Admin cancelled your Gift.");
 
 				    log.info(logpre + "stopped a gift of " + items.getAmount() + " " + Items.itemByStack(items).getName() + " from " + playerSendingItems.getDisplayName());
 
 				    plugin.gifts.remove(gift);
 				    plugin.senders.remove(Sender1);
 				}
 
 
 			    }
 			}
 			player.sendMessage(prefix + ChatColor.GREEN + "Cancelled all Gifts safely.");
 
 		    } else if (args[0].equalsIgnoreCase("cancel") && plugin.getPerms(player, "sgift.gift.cancel")) {
 
 			Gift gift = null;
 			Sender Sender1 = null;
 
 			for (Gift g : plugin.gifts) {
 
 			    if (g.playerSender == player) {
 
 				gift = g;
 			    }
 			}
 
 			for (Sender s : plugin.senders) {
 
 			    if (s.Sender == player) {
 
 				Sender1 = s;
 			    }
 
 			}
 
 			if (gift == null) {
 
 			    player.sendMessage(prefix + ChatColor.RED + "No Gifts to cancel!");
 			} else {
 
 			    Player playerSendingItems = gift.playerSender;
 			    Player Victim = gift.Victim;
 			    ItemStack items = gift.itemStack;
 
 			    if (playerSendingItems.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = playerSendingItems.getLocation();
 
 				playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
 				playerSendingItems.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				playerSendingItems.sendMessage(prefix + ChatColor.RED + "Cancelled gift!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Gift!");
 
 				plugin.senders.remove(Sender1);
 				plugin.gifts.remove(gift);
 
 			    } else {
 
 				playerSendingItems.getInventory().addItem(items);
 				playerSendingItems.sendMessage(prefix + ChatColor.RED + "Cancelled gift!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.itemByStack(items).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Gift!");
 
 				plugin.senders.remove(Sender1);
 				plugin.gifts.remove(gift);
 			    }
 
 
 			}
 
		    } else if (args.length == 1 && plugin.getPerms(player, "sgift.gift.start")) {
			
			player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
			player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
		
 		    } else if (Bukkit.getServer().getPlayer(args[0]) == null) {
 
 			player.sendMessage(prefix + ChatColor.RED + "Player not Online.");
 
 		    } else if (Bukkit.getServer().getPlayer(args[0]) == player) {
 
 			player.sendMessage(prefix + ChatColor.RED + "Don't gift Items to yourself!");
 
 		    }
 		} else if (args.length == 2) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
 
 		} else if (args.length == 3 && plugin.getPerms(player, "sgift.gift.start")) {
 		    if (Bukkit.getServer().getPlayer(args[0]) != player) {
 			if (Bukkit.getServer().getPlayer(args[0]) != null) {
 
 			    Player Victim = Bukkit.getServer().getPlayer(args[0]);
 			    int amount = plugin.getInt(args[2]);
 			    ItemStack Item = null;
 
 			    ItemInfo ii = Items.itemByString(args[1]);
 
 			    if (args[1].equalsIgnoreCase("hand")) {
 				if (player.getItemInHand() != null) {
 
 				    Item = player.getItemInHand().clone();
 
 				    if (amount != 0) {
 					if (Item.getAmount() >= amount) {
 
 					    Item.setAmount(amount);
 
 					    plugin.gifts.add(new Gift(Victim, player, Item));
 					    plugin.senders.add(new Sender(player));
 
 					    new InventoryManager(player).remove(Item);
 
 					    player.sendMessage(prefix + ChatColor.WHITE + "Now Gifting " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName() + ChatColor.WHITE + " with " + ChatColor.YELLOW + Victim.getName());
 					    player.sendMessage(prefix + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
 					    Victim.sendMessage(prefix + ChatColor.WHITE + "New Gift from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName());
 					    Victim.sendMessage(prefix + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/Gift accept" + ChatColor.WHITE + " to accept this Gift or " + ChatColor.YELLOW + "/Gift deny" + ChatColor.WHITE + " to deny this Gift!");
 					    if (Item.getDurability() < Item.getType().getMaxDurability()) {
 
 						Victim.sendMessage(prefix + ChatColor.RED + "Warning! This item has " + (Item.getType().getMaxDurability() - Item.getDurability()) + " uses left out of a maximum of " + Item.getType().getMaxDurability() + " uses.");
 
 					    }
 					    if (Item.getEnchantments().size() > 0) {
 
 						Victim.sendMessage(prefix + ChatColor.YELLOW + "This Item is enchanted!");
 						player.sendMessage(Item.getEnchantments().toString());
 
 					    }
 					} else {
 
 					    player.sendMessage(prefix + ChatColor.RED + "You do not have enough of that Item in your hand!");
 					}
 				    } else {
 
 					player.sendMessage(prefix + ChatColor.RED + "Invalid amount!");
 				    }
 				} else {
 
 				    player.sendMessage(prefix + ChatColor.RED + "There's no Item in your Hand!");
 				}
 			    } else if (Items.itemByString(args[1]) != null) {
 
 				Item = new ItemStack(ii.getType(), amount, ii.getSubTypeId());
 
 				if (amount != 0) {
 				    if (new InventoryManager(player).contains(Item, true, true)) {
 
 					plugin.gifts.add(new Gift(Victim, player, Item));
 					plugin.senders.add(new Sender(player));
 
 					new InventoryManager(player).remove(Item);
 
 					player.sendMessage(prefix + ChatColor.WHITE + "Now Gifting " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName() + ChatColor.WHITE + " with " + ChatColor.YELLOW + Victim.getName());
 					player.sendMessage(prefix + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
 					Victim.sendMessage(prefix + ChatColor.WHITE + "New Gift from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName());
 					Victim.sendMessage(prefix + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/Gift accept" + ChatColor.WHITE + " to accept this Gift or " + ChatColor.YELLOW + "/Gift deny" + ChatColor.WHITE + " to deny this Gift!");
 
 				    } else {
 
 					player.sendMessage(prefix + ChatColor.RED + "You don't have enough " + Items.itemByStack(Item).getName() + ", or Item is partially Used/Enchanted!");
 					player.sendMessage(prefix + ChatColor.GRAY + "Check your Item ID's, For example, Orange wool would Be Orange_Wool.");
 				    }
 				} else {
 				    player.sendMessage(prefix + ChatColor.RED + "Amount specified is Invalid!");
 				}
 			    } else {
 
 				player.sendMessage(prefix + ChatColor.RED + "Material specified is Invalid!");
 			    }
 			} else {
 
 			    player.sendMessage(prefix + ChatColor.RED + "Player not Online!");
 			}
 
 		    } else {
 
 			player.sendMessage(prefix + ChatColor.RED + "You can't Gift yourself!");
 		    }
 
 		} else if (args.length == 0) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "By Sk8r2K9. /gift help for more info.");
 
 		} else if (args.length >= 4) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too many arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
 		}
 	    } else {
 		if (player != null) {
 
 		    player.sendMessage(prefix + "Gifting is currently disabled!");
 		} else {
 
 		    log.warning(logpre + "Don't send sGift commands through console!");
 		}
 	    }
 	}
 	return false;
     }
 }
