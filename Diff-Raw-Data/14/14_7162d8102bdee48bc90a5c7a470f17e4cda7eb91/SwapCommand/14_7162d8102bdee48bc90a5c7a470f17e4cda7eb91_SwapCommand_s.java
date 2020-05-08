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
 
 public class SwapCommand implements CommandExecutor {
     // Command: /swap <player> <item> <amount> <item2> <amount2>
 
     private sGift plugin;
     private int ID;
     Player player = null;
     String prefix = ChatColor.WHITE + "[" + ChatColor.BLUE + "sGift" + ChatColor.WHITE + "] ";
     Logger log = Logger.getLogger("Minecraft");
 
     public SwapCommand(sGift instance) {
 	plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 	int maxAmount = plugin.getConfig().getInt("Options.max-amount");
 
 	PluginDescriptionFile pdf = plugin.getDescription();
 	String logpre = "[" + pdf.getName() + " " + pdf.getVersion() + "] ";
 
 	if (sender instanceof Player) {
 
 	    player = (Player) sender;
 	} else {
 
 	    player = null;
 	}
 	if (commandLabel.equalsIgnoreCase("swap") && plugin.getPerms(player, "sgift.swap.swap")) {
 	    if (plugin.getConfig().getBoolean("Features.enable-swap")) {
 		if (player == null) {
 
 		    log.warning(logpre + "Don't send sGift commands through console!");
 
 		} else if (args.length == 1) {
 		    if (args[0].equalsIgnoreCase("auto") && plugin.getPerms(player, "sgift.swap.auto")) {
 			if (plugin.getConfig().getBoolean("Features.allow-auto.swap")) {
 			    if (!player.hasPermission("sgift.toggles.swap.accept")) {
 
 				player.sendMessage(prefix + ChatColor.YELLOW + "Auto-Accept enabled for Swapping!");
 				plugin.getPermissions().playerAdd(player, "sgift.toggles.swap.accept");
 				plugin.getPermissions().playerRemove(player, "-sgift.toggles.swap.accept");
 			    } else {
 
 				player.sendMessage(prefix + ChatColor.YELLOW + "Auto-Accept disabled for Swapping!");
 				plugin.getPermissions().playerRemove(player, "sgift.toggles.swap.accept");
 				plugin.getPermissions().playerAdd(player, "-sgift.toggles.swap.accept");
 			    }
 			} else {
 
 			    player.sendMessage(prefix + ChatColor.RED + "Auto-Features are Disabled.");
 			}
 		    } else if (args[0].equalsIgnoreCase("auto-deny") && plugin.getPerms(player, "sgift.swap.autodeny")) {
 			if (plugin.getConfig().getBoolean("Features.allow-auto.swap")) {
 			    if (!player.hasPermission("sgift.toggles.swap.deny")) {
 
 				player.sendMessage(prefix + ChatColor.YELLOW + "Auto-Deny enabled for Swapping!");
 				plugin.getPermissions().playerAdd(player, "sgift.toggles.swap.deny");
 				plugin.getPermissions().playerRemove(player, "-sgift.toggles.swap.deny");
 			    } else {
 
 				player.sendMessage(prefix + ChatColor.YELLOW + "Auto-Deny disabled for Swapping!");
 				plugin.getPermissions().playerRemove(player, "sgift.toggles.swap.deny");
 				plugin.getPermissions().playerAdd(player, "-sgift.toggles.swap.deny");
 			    }
 			} else {
 
 			    player.sendMessage(prefix + ChatColor.RED + "Auto-Features are Disabled.");
 			}
 		    } else if (args[0].equalsIgnoreCase("help") && plugin.getPerms(player, "sgift.swap.help")) {
 
 			player.sendMessage(ChatColor.DARK_GRAY + "----------------[" + ChatColor.GREEN + "sGift - Swap Help Menu" + ChatColor.DARK_GRAY + "]----------------");
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Swap"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Example"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Accept"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Deny"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Cancel"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Auto"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.AutoDeny"));
 			player.sendMessage(plugin.getConfig().getString("Help.Swap.Help"));
 
 			if (player.hasPermission("sgift.admin")) {
 
 			    player.sendMessage(plugin.getConfig().getString("Help.Swap.Stop"));
 			}
 		    } else if (args[0].equalsIgnoreCase("accept") && plugin.getPerms(player, "sgift.swap.accept")) {
 
 			Swap swap = null;
 			Timeout time = null;
 			Sender Sender1 = null;
 
 			for (Swap sw : plugin.swaps) {
 
 			    if (sw.Victim == player) {
 
 				swap = sw;
 
 				for (Sender s : plugin.senders) {
 
 				    if (s.Sender == sw.playerSender) {
 
 					Sender1 = s;
 				    }
 				}
 				for (Timeout o : plugin.timeout) {
 
 				    if (o.ID == swap.ID) {
 
 					time = o;
 				    }
 				}
 			    }
 			}
 			if (swap == null) {
 
 			    player.sendMessage(prefix + ChatColor.RED + "No Swaps to accept!");
 			} else {
 
 			    Player playerInitial = swap.playerSender;
 			    Player Victim = swap.Victim;
 			    ItemStack itemsFromSender = swap.itemSender;
 			    ItemStack itemsFromVictim = swap.itemVictim;
 
 			    if (player.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = player.getLocation();
 				player.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 
 				player.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 
 			    } else {
 
 				Victim.getInventory().addItem(itemsFromSender);
 
 				Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 			    }
 			    if (playerInitial.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = playerInitial.getLocation();
 				playerInitial.getWorld().dropItemNaturally(playerloc, itemsFromVictim);
 
 				player.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 
 			    } else {
 
 				playerInitial.getInventory().addItem(itemsFromVictim);
 
 				playerInitial.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 			    }
 			    log.info(logpre + Victim.getDisplayName() + " recieved " + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + " from " + playerInitial.getName() + " for " + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + "!");
 
 			    plugin.timeout.remove(time);
 			    plugin.swaps.remove(swap);
 			    plugin.senders.remove(Sender1);
 			}
 		    } else if (args[0].equalsIgnoreCase("deny") && plugin.getPerms(player, "sgift.swap.deny")) {
 
 			Swap swap = null;
 			Timeout time = null;
 			Sender Sender1 = null;
 
 			for (Swap sw : plugin.swaps) {
 
 			    if (sw.Victim == player) {
 
 				swap = sw;
 
 				for (Sender s : plugin.senders) {
 
 				    if (s.Sender == sw.playerSender) {
 
 					Sender1 = s;
 				    }
 				}
 				for (Timeout o : plugin.timeout) {
 
 				    if (o.ID == swap.ID) {
 
 					time = o;
 				    }
 				}
 			    }
 			}
 
 			if (swap == null) {
 
 			    player.sendMessage(prefix + ChatColor.RED + "No Swaps to deny!");
 			} else {
 
 			    Player playerSendingItems = swap.playerSender;
 			    Player Victim = swap.Victim;
 			    ItemStack itemsFromSender = swap.itemSender;
 			    ItemStack itemsFromVictim = swap.itemVictim;
 
 			    if (Victim.getInventory().firstEmpty() == -1) {
 
 				Victim.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				Location playerloc = Victim.getLocation();
 				Victim.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 			    } else {
 				Victim.sendMessage(prefix + ChatColor.YELLOW + "Your Items have been returned to you.");
 				Victim.getInventory().addItem(itemsFromVictim);
 			    }
 
 			    if (playerSendingItems.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = playerSendingItems.getLocation();
 				playerSendingItems.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 
 				playerSendingItems.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Swap request!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Swap!");
 
 
 
 			    } else if (!playerSendingItems.isOnline() || !Victim.isOnline()) {
 				player.sendMessage(prefix + ChatColor.RED + "Player sending items is not Online!");
 				player.sendMessage(prefix + ChatColor.RED + "Please wait for the Player to come back online!");
 
 			    } else {
 				playerSendingItems.getInventory().addItem(itemsFromSender);
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Swap request!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Swap!");
 			    }
 
 			    log.info(logpre + Victim.getDisplayName() + " denied " + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + " from " + playerSendingItems.getDisplayName());
 
 			    plugin.timeout.remove(time);
 			    plugin.swaps.remove(swap);
 			    plugin.senders.remove(Sender1);
 			}
 
 
 
 		    } else if (args[0].equalsIgnoreCase("stop") && plugin.getPerms(player, "sgift.sgift")) {
 			while (plugin.swaps.size() > 0) {
 
 			    Swap swap = null;
 			    Timeout time = null;
 			    Sender Sender1 = null;
 
 			    for (Swap sw : plugin.swaps) {
 
 				if (sw.itemSender != null) {
 				    swap = sw;
 
 				    for (Sender s : plugin.senders) {
 
 					if (s.Sender != null) {
 
 					    Sender1 = s;
 					}
 				    }
 				    for (Timeout o : plugin.timeout) {
 
 					if (o.ID == swap.ID) {
 
 					    time = o;
 					}
 				    }
 				}
 			    }
 
 			    if (swap == null) {
 
 				player.sendMessage(prefix + ChatColor.RED + "No Swaps to stop!");
 			    } else {
 
 				Player playerSendingItems = swap.playerSender;
 				Player Victim = swap.Victim;
 				ItemStack itemsFromSender = swap.itemSender;
 				ItemStack itemsFromVictim = swap.itemVictim;
 
 				if (playerSendingItems.getInventory().firstEmpty() == -1) {
 
 				    Location playerloc = playerSendingItems.getLocation();
 
 				    playerSendingItems.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 				    playerSendingItems.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				    playerSendingItems.sendMessage(prefix + ChatColor.RED + "Your Swap has been cancelled by an Admin!");
 				    playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.RED + " has been returned to you.");
 				    Victim.sendMessage(prefix + ChatColor.RED + "Admin cancelled your Swap.");
 				    log.info(logpre + "stopped a swap of " + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + " from " + playerSendingItems.getDisplayName() + " for " + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ".");
 
 				    plugin.timeout.remove(time);
 				    plugin.swaps.remove(swap);
 				    plugin.senders.remove(Sender1);
 
 				} else {
 
 				    playerSendingItems.getInventory().addItem(itemsFromSender);
 				    playerSendingItems.sendMessage(prefix + ChatColor.RED + "Your Swap has been cancelled by an Admin!");
 				    playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.RED + " has been returned to you.");
 				    Victim.sendMessage(prefix + ChatColor.RED + "Admin cancelled your Swap.");
 
 				    log.info(logpre + "stopped a swap of " + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + " from " + playerSendingItems.getDisplayName() + " for " + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ".");
 
 				    plugin.timeout.remove(time);
 				    plugin.swaps.remove(swap);
 				    plugin.senders.remove(Sender1);
 				}
 			    }
 			}
 			player.sendMessage(prefix + ChatColor.GREEN + "Cancelled all Swaps safely.");
 
 		    } else if (args[0].equalsIgnoreCase("cancel") && plugin.getPerms(player, "sgift.swap.cancel")) {
 
 			Swap swap = null;
 			Timeout time = null;
 			Sender Sender1 = null;
 
 			for (Swap sw : plugin.swaps) {
 
 			    if (sw.playerSender == player) {
 
 				swap = sw;
 			    }
 			}
 
 			for (Sender s : plugin.senders) {
 
 			    if (s.Sender == player) {
 
 				Sender1 = s;
 			    }
 
 			}
 			for (Timeout o : plugin.timeout) {
 
 			    if (o.ID == swap.ID) {
 
 				time = o;
 			    }
 			}
 
 			if (swap == null) {
 
 			    player.sendMessage(prefix + ChatColor.RED + "No Swaps to cancel!");
 			} else {
 
 			    Player playerSendingItems = swap.playerSender;
 			    Player Victim = swap.Victim;
 			    ItemStack itemsFromSender = swap.itemSender;
 			    ItemStack itemsFromVictim = swap.itemVictim;
 
 			    if (playerSendingItems.getInventory().firstEmpty() == -1) {
 
 				Location playerloc = playerSendingItems.getLocation();
 
 				playerSendingItems.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 				playerSendingItems.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 				playerSendingItems.sendMessage(prefix + ChatColor.RED + "Cancelled Swap!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Swap!");
 
 				plugin.timeout.remove(time);
 				plugin.senders.remove(Sender1);
 				plugin.swaps.remove(swap);
 
 			    } else {
 
 				playerSendingItems.getInventory().addItem(itemsFromSender);
 				playerSendingItems.sendMessage(prefix + ChatColor.RED + "Cancelled Swap!");
 				playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.RED + " Has been returned to you.");
 				Victim.sendMessage(prefix + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Swap!");
 
 				plugin.timeout.remove(time);
 				plugin.senders.remove(Sender1);
 				plugin.swaps.remove(swap);
 			    }
 			}
 		    } else if (Bukkit.getServer().getPlayer(args[0]) == null) {
 
 			player.sendMessage(prefix + ChatColor.RED + "Player not Online.");
 
 		    } else if (Bukkit.getServer().getPlayer(args[0]) == player) {
 
 			player.sendMessage(prefix + ChatColor.RED + "Don't swap Items with yourself!");
 
 		    } else if (plugin.getPerms(player, "sgift.swap.start")) {
 
 			player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
 			player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount>");
 
 		    }
 		} else if (args.length == 2) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount>");
 
 		} else if (args.length == 3) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount>");
 
 		} else if (args.length == 4) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount>");
 
 		} else if (args.length == 5) {
 		    if (Bukkit.getServer().getPlayer(args[0]) != player) {
 			if (Bukkit.getServer().getPlayer(args[0]) != null) {
 			    if (!plugin.inCreative(player, Bukkit.getServer().getPlayer(args[0]))) {
 				
 				Player Victim = null;
 				int amount = plugin.getInt(args[2]);
 				ItemStack Item = null;
 				ItemStack ItemFromVictim = null;
 				int amountFromVictim = plugin.getInt(args[4]);
 
 				ItemInfo ii = Items.itemByString(args[1]);
 				ItemInfo ii2 = Items.itemByString(args[3]);
 
 				if (args[1].equalsIgnoreCase("hand")) {
 				    if (player.getItemInHand() != null) {
 
 					Item = player.getItemInHand().clone();
 
 					if (amount != 0 && ((maxAmount >= amount) || (maxAmount == 0))) {
 					    if (Item.getAmount() >= amount) {
 
 						Victim = Bukkit.getServer().getPlayer(args[0]);
 
 						Item.setAmount(amount);
 
 						Location VictimLoc = Victim.getLocation();
 						Location playerLoc = player.getLocation();
 						if (!plugin.alreadyRequested(player, Victim)) {
 						    if (!plugin.differentWorlds(player, Victim)) {
 							if (plugin.rangeIsDisabled() || plugin.isWithinRange(VictimLoc, playerLoc)) {
 							    if (amountFromVictim != 0 && (((maxAmount >= amount) || (maxAmount == 0)) || player.hasPermission("sgift.overrides.max"))) {
 
 								ItemFromVictim = new ItemStack(ii2.getType(), amountFromVictim, ii2.getSubTypeId());
 
 								if (!plugin.itemsAreNull(Item, ItemFromVictim)) {
 								    if (!plugin.auto(Victim, "swap", "sgift.toggles.swap.deny")) {
 									if (new InventoryManager(Victim).contains(ItemFromVictim, true, true)) {
 
 									    plugin.ID += 1;
 
									    Swap tswap = new Swap(Victim, player, Item, ItemFromVictim, ID);
 
 									    long time = player.getWorld().getTime();
 
 									    plugin.swaps.add(tswap);
									    plugin.timeout.add(new Timeout(tswap, player, ID, time));
 									    plugin.senders.add(new Sender(player));
 
 									    new InventoryManager(player).remove(Item);
 									    new InventoryManager(Victim).remove(ItemFromVictim);
 
 									    player.sendMessage(prefix + ChatColor.WHITE + "Now Swapping " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + ItemFromVictim.getAmount() + " " + Items.itemByStack(ItemFromVictim).getName() + " with " + ChatColor.YELLOW + Victim.getName() + "!");
 									    player.sendMessage(prefix + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
 									    Victim.sendMessage(prefix + ChatColor.WHITE + "New Swap Request from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName() + " for " + ItemFromVictim.getAmount() + " " + Items.itemByStack(ItemFromVictim).getName());
 									    Victim.sendMessage(prefix + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/swap accept" + ChatColor.WHITE + " to accept this Swap or " + ChatColor.YELLOW + "/swap deny" + ChatColor.WHITE + " to deny this Swap!");
 
 									    plugin.newTimeout(player, Victim, Item, ItemFromVictim);
 
 									    if (Item.getDurability() < Item.getType().getMaxDurability()) {
 
 										Victim.sendMessage(prefix + ChatColor.RED + "Warning! This item has " + (Item.getType().getMaxDurability() - Item.getDurability()) + " uses left out of a maximum of " + Item.getType().getMaxDurability() + " uses.");
 
 									    }
 									    if (Item.getEnchantments().size() > 0) {
 
 										Victim.sendMessage(prefix + ChatColor.YELLOW + "This Item is enchanted!");
 										player.sendMessage(Item.getEnchantments().toString());
 
 									    }
 									    if (plugin.auto(Victim, "swap", "sgift.toggles.swap.accept")) {
 
 										Swap swap = null;
 										Timeout out = null;
 										Sender Sender1 = null;
 
 										for (Swap sw : plugin.swaps) {
 
 										    if (sw.Victim == Victim) {
 
 											swap = sw;
 
 											for (Sender s : plugin.senders) {
 
 											    if (s.Sender == sw.playerSender) {
 
 												Sender1 = s;
 											    }
 											}
 											for (Timeout o : plugin.timeout) {
 
 											    if (o.ID == swap.ID) {
 
 												out = o;
 											    }
 											}
 										    }
 										}
 
 										if (swap == null) {
 
 										    Victim.sendMessage(prefix + ChatColor.RED + "No Swaps to accept!");
 										} else {
 
 										    Player playerInitial = swap.playerSender;
 										    Victim = swap.Victim;
 										    ItemStack itemsFromSender = swap.itemSender;
 										    ItemStack itemsFromVictim = swap.itemVictim;
 
 										    player.sendMessage(prefix + ChatColor.YELLOW + "Other player Auto-Accepted!");
 										    Victim.sendMessage(prefix + ChatColor.YELLOW + "Auto-Accepted Swap request! use /swap auto to toggle this on or off.");
 
 										    if (Victim.getInventory().firstEmpty() == -1) {
 
 											Location playerloc = Victim.getLocation();
 											Victim.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 
 											Victim.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 											Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 
 
 
 										    } else {
 
 											Victim.getInventory().addItem(itemsFromSender);
 
 											Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 										    }
 										    if (playerInitial.getInventory().firstEmpty() == -1) {
 
 											Location playerloc = playerInitial.getLocation();
 											playerInitial.getWorld().dropItemNaturally(playerloc, itemsFromVictim);
 
 											player.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 											player.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 
 										    } else {
 
 											playerInitial.getInventory().addItem(itemsFromVictim);
 
 											playerInitial.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 										    }
 										    log.info(logpre + Victim.getDisplayName() + " recieved " + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + " from " + playerInitial.getName() + " for " + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + "!");
 
 										    plugin.swaps.remove(swap);
 										    plugin.senders.remove(Sender1);
 										}
 									    }
 									} else {
 
 									    player.sendMessage(prefix + ChatColor.RED + "Other player doesn't have enough of that Item!");
 									}
 								    } else {
 
 									player.sendMessage(prefix + ChatColor.RED + "That player doesn't want to Swap with you!");
 								    }
 								} else {
 
 								    player.sendMessage(prefix + ChatColor.RED + "Items attempting to swap are not currently supported.");
 								}
 							    } else {
 
 								player.sendMessage(prefix + ChatColor.RED + "Amount expected from other player is Invalid!");
 								if (maxAmount != 0) {
 								    player.sendMessage(prefix + ChatColor.GRAY + "Amount is too large! Max is: " + maxAmount);
 								}
 							    }
 							} else {
 
 							    player.sendMessage(prefix + ChatColor.RED + "You are out of range with that player!");
 							    player.sendMessage(prefix + ChatColor.GRAY + "You must be within " + plugin.getConfig().getInt("Options.max-distance") + " blocks of each other.");
 							}
 						    } else {
 
 							player.sendMessage(prefix + ChatColor.RED + "You are not in the same world as that player!");
 							player.sendMessage(prefix + ChatColor.GRAY + "You must be in '" + Victim.getWorld().getName() + "' to Swap with " + Victim.getName() + ".");
 						    }
 						}
 					    } else {
 
 						player.sendMessage(prefix + ChatColor.RED + "You do not have enough of that Item in your hand!");
 					    }
 					} else {
 
 					    player.sendMessage(prefix + ChatColor.RED + "Invalid amount!");
 					    if (maxAmount != 0) {
 						player.sendMessage(prefix + ChatColor.GRAY + "Amount is too large! Max is: " + maxAmount);
 					    }
 					}
 				    } else {
 
 					player.sendMessage(prefix + ChatColor.RED + "There's no Item in your Hand!");
 				    }
 				} else if (Items.itemByString(args[1]) != null) {
 
 				    Item = new ItemStack(ii.getType(), amount, ii.getSubTypeId());
 
 				    if (amount != 0 && ((maxAmount >= amount) || (maxAmount == 0))) {
 					if (new InventoryManager(player).contains(Item, true, true)) {
 
 					    Victim = Bukkit.getServer().getPlayer(args[0]);
 
 					    Location VictimLoc = Victim.getLocation();
 					    Location playerLoc = player.getLocation();
 
 					    if (!plugin.alreadyRequested(player, Victim)) {
 						if (!plugin.differentWorlds(player, Victim)) {
 						    if (plugin.rangeIsDisabled() || plugin.isWithinRange(VictimLoc, playerLoc)) {
 							if (amountFromVictim != 0 && (((maxAmount >= amount) || (maxAmount == 0)) || player.hasPermission("sgift.overrides.max"))) {
 
 							    ItemFromVictim = new ItemStack(ii2.getType(), amountFromVictim, ii2.getSubTypeId());
 
							    if (Items.itemByStack(Item).getName() != null && Items.itemByStack(ItemFromVictim) != null) {
 								if (!plugin.auto(Victim, "swap", "sgift.toggles.swap.deny")) {
 								    if (new InventoryManager(Victim).contains(ItemFromVictim, true, true)) {
 
 									plugin.ID += 1;
 
									Swap tswap = new Swap(Victim, player, Item, ItemFromVictim, ID);
 
 									long time = player.getWorld().getTime();
 
 									plugin.swaps.add(tswap);
									plugin.timeout.add(new Timeout(tswap, player, ID, time));
 									plugin.senders.add(new Sender(player));
 
 									new InventoryManager(player).remove(Item);
 									new InventoryManager(Victim).remove(ItemFromVictim);
 
 									player.sendMessage(prefix + ChatColor.WHITE + "Now Swapping " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + ItemFromVictim.getAmount() + " " + Items.itemByStack(ItemFromVictim).getName() + " with " + ChatColor.YELLOW + Victim.getName() + "!");
 									player.sendMessage(prefix + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
 									Victim.sendMessage(prefix + ChatColor.WHITE + "New Swap Request from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.itemByStack(Item).getName() + " for " + ItemFromVictim.getAmount() + " " + Items.itemByStack(ItemFromVictim));
 									Victim.sendMessage(prefix + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/swap accept" + ChatColor.WHITE + " to accept this Swap or " + ChatColor.YELLOW + "/swap deny" + ChatColor.WHITE + " to deny this Swap!");
 
 									plugin.newTimeout(player, Victim, Item, ItemFromVictim);
 
 									if (Item.getDurability() < Item.getType().getMaxDurability()) {
 
 									    Victim.sendMessage(prefix + ChatColor.RED + "Warning! This item has " + (Item.getType().getMaxDurability() - Item.getDurability()) + " uses left out of a maximum of " + Item.getType().getMaxDurability() + " uses.");
 
 									}
 									if (Item.getEnchantments().size() > 0) {
 
 									    Victim.sendMessage(prefix + ChatColor.RED + "This Item is enchanted!");
 
 									}
 									if (ItemFromVictim.getDurability() < ItemFromVictim.getType().getMaxDurability()) {
 
 									    player.sendMessage(prefix + ChatColor.RED + "Warning! Item from other player has " + (ItemFromVictim.getType().getMaxDurability() - ItemFromVictim.getDurability()) + " uses left out of a maximum of " + ItemFromVictim.getType().getMaxDurability() + " uses.");
 
 									}
 									if (ItemFromVictim.getEnchantments().size() > 0) {
 
 									    player.sendMessage(prefix + ChatColor.RED + "The Item being requested is Enchanted!");
 
 									}
 									if (plugin.auto(Victim, "swap", "sgift.toggles.swap.accept")) {
 
 									    Swap swap = null;
 									    Timeout out = null;
 									    Sender Sender1 = null;
 
 									    for (Swap sw : plugin.swaps) {
 
 										if (sw.Victim == Victim) {
 
 										    swap = sw;
 
 										    for (Sender s : plugin.senders) {
 
 											if (s.Sender == sw.playerSender) {
 
 											    Sender1 = s;
 											}
 										    }
 										    for (Timeout o : plugin.timeout) {
 
 											if (o.ID == swap.ID) {
 
 											    out = o;
 											}
 										    }
 										}
 									    }
 
 									    if (swap == null) {
 
 										Victim.sendMessage(prefix + ChatColor.RED + "No Swaps to accept!");
 									    } else {
 
 										Player playerInitial = swap.playerSender;
 										Victim = swap.Victim;
 										ItemStack itemsFromSender = swap.itemSender;
 										ItemStack itemsFromVictim = swap.itemVictim;
 
 										player.sendMessage(prefix + ChatColor.YELLOW + "Other player Auto-Accepted!");
 										Victim.sendMessage(prefix + ChatColor.YELLOW + "Auto-Accepted Swap request! use /swap auto to toggle this on or off.");
 
 										if (Victim.getInventory().firstEmpty() == -1) {
 
 										    Location playerloc = Victim.getLocation();
 										    Victim.getWorld().dropItemNaturally(playerloc, itemsFromSender);
 
 										    Victim.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 										    Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 
 
 
 										} else {
 
 										    Victim.getInventory().addItem(itemsFromSender);
 
 										    Victim.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 										}
 										if (playerInitial.getInventory().firstEmpty() == -1) {
 
 										    Location playerloc = playerInitial.getLocation();
 										    playerInitial.getWorld().dropItemNaturally(playerloc, itemsFromVictim);
 
 										    player.sendMessage(prefix + "Inventory full! Dropped Items at your feet!");
 										    player.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerInitial.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 
 										} else {
 
 										    playerInitial.getInventory().addItem(itemsFromVictim);
 
 										    playerInitial.sendMessage(prefix + ChatColor.YELLOW + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.YELLOW + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + ChatColor.WHITE + "!");
 										}
 										log.info(logpre + Victim.getDisplayName() + " recieved " + itemsFromSender.getAmount() + " " + Items.itemByStack(itemsFromSender).getName() + " from " + playerInitial.getName() + " for " + itemsFromVictim.getAmount() + " " + Items.itemByStack(itemsFromVictim).getName() + "!");
 
 										plugin.timeout.remove(out);
 										plugin.swaps.remove(swap);
 										plugin.senders.remove(Sender1);
 									    }
 									}
 								    } else {
 
 									player.sendMessage(prefix + ChatColor.RED + "Other player doesn't have enough of the Item you requested.");
 								    }
 								} else {
 
 								    player.sendMessage(prefix + ChatColor.RED + "That player doesn't want to Swap with you!");
 								}
 							    } else {
 
 								player.sendMessage(prefix + ChatColor.RED + "Items attempting to swap are not currently supported.");
 							    }
 							} else {
 
 							    player.sendMessage(prefix + ChatColor.RED + "Amount expected from other player is Invalid!");
 							    if (maxAmount != 0) {
 								player.sendMessage(prefix + ChatColor.GRAY + "Amount is too large! Max is: " + maxAmount);
 							    }
 							}
 						    } else {
 
 							player.sendMessage(prefix + ChatColor.RED + "You are out of range with that player!");
 							player.sendMessage(prefix + ChatColor.GRAY + "You must be within " + plugin.getConfig().getInt("Options.max-distance") + " blocks of each other.");
 						    }
 						} else {
 
 						    player.sendMessage(prefix + ChatColor.RED + "You are not in the same world as that player!");
 						    player.sendMessage(prefix + ChatColor.GRAY + "You must be in '" + Victim.getWorld().getName() + "' to Swap with " + Victim.getName() + ".");
 						}
 					    }
 					} else {
 
 					    player.sendMessage(prefix + ChatColor.RED + "You don't have enough of that Item, Or Item is partially damaged!");
 					}
 				    } else {
 
 					player.sendMessage(prefix + ChatColor.RED + "Invalid amount!");
 					if (maxAmount != 0) {
 					    player.sendMessage(prefix + ChatColor.GRAY + "Amount is too large! Max is: " + maxAmount);
 					}
 				    }
 				} else {
 
 				    player.sendMessage(prefix + ChatColor.RED + "Material specified is Invalid!");
 				}
 			    }
 			} else {
 
 			    player.sendMessage(prefix + ChatColor.RED + "Player not Online!");
 			}
 
 		    } else {
 
 			player.sendMessage(prefix + ChatColor.RED + "You can't Swap with yourself!");
 		    }
 
 		} else if (args.length > 5) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too Many arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount> <Item_From_Player> <Amount>");
 		} else if (args.length == 0) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount> <Item_From_Player> <Amount>");
 		} else {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Invalid command usage.");
 		    player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /swap <Player> <Item> <Amount> <Item_From_Player> <Amount>");
 		}
 	    } else {
 		if (player != null) {
 
 		    player.sendMessage(prefix + ChatColor.RED + "Swapping currently disabled.");
 		} else {
 
 		    log.warning(logpre + "Don't send sGift commands through console!");
 		}
 	    }
 	}
 	return false;
     }
 }
