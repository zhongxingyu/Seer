 package com.legit2.Demigods.Listeners;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.legit2.Demigods.DDivineBlocks;
 import com.legit2.Demigods.Demigods;
 import com.legit2.Demigods.DTributeValue;
 import com.legit2.Demigods.Database.DDatabase;
 import com.legit2.Demigods.Libraries.DivineBlock;
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DConfigUtil;
 import com.legit2.Demigods.Utilities.DDataUtil;
 import com.legit2.Demigods.Utilities.DDeityUtil;
 import com.legit2.Demigods.Utilities.DObjUtil;
 import com.legit2.Demigods.Utilities.DPlayerUtil;
 import com.legit2.Demigods.Utilities.DMiscUtil;
 import com.legit2.Demigods.Utilities.DZoneUtil;
 
 public class DDivineBlockListener implements Listener
 {
 	static Demigods plugin;
 	public static double FAVOR_MULTIPLIER = DConfigUtil.getSettingDouble("global_favor_multiplier");
 	
 	public DDivineBlockListener(Demigods instance)
 	{
 		plugin = instance;
 	}
 	
 	/* --------------------------------------------
 	 *  Handle DivineBlock Interactions
 	 * --------------------------------------------
 	 */
 	@EventHandler(priority = EventPriority.HIGH)
 	public void shrineBlockInteract(PlayerInteractEvent event)
 	{
 		// Return if the player is mortal
 		if(!DCharUtil.isImmortal(event.getPlayer())) return;
 		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 		// Define variables
 		Location location = event.getClickedBlock().getLocation();
 		Player player = event.getPlayer();
 		int charID = DPlayerUtil.getCurrentChar(player);
 		String charAlliance = DCharUtil.getAlliance(charID);
 		String charDeity = DCharUtil.getDeity(charID);
 		
 		if(event.getClickedBlock().getType().equals(Material.GOLD_BLOCK) && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.BOOK)
 		{						
 			try
 			{
 				// Shrine created!
 				DDivineBlocks.createShrine(charID, location);
 				
 				if(player.getItemInHand().getAmount() > 1)
 				{
 					ItemStack books = new ItemStack(player.getItemInHand().getType(), player.getInventory().getItemInHand().getAmount() - 1);
 					player.setItemInHand(books);
 				}
 				else player.getInventory().remove(Material.BOOK);
 
 				player.sendMessage(ChatColor.GRAY + "The " + ChatColor.YELLOW + charAlliance + "s" + ChatColor.GRAY + " are pleased...");
 				player.sendMessage(ChatColor.GRAY + "You have created a Shrine in the name of " + ChatColor.YELLOW + charDeity + ChatColor.GRAY + "!");
 			}
 			catch(Exception e)
 			{
 				// Creation of shrine failed...
 				e.printStackTrace();
 			}
 		}
 		
 		useShrine(player, location);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void shrineEntityInteract(PlayerInteractEntityEvent event)
 	{
 		// Define variables
 		Location location = event.getRightClicked().getLocation().subtract(0.5, 1.0, 0.5);
 		Player player = event.getPlayer();
 		
 		// First handle admin wand
 		if(DMiscUtil.hasPermissionOrOP(player, "demigods.admin") && DDataUtil.hasPlayerData(player, "temp_admin_wand") && DDataUtil.getPlayerData(player, "temp_admin_wand").equals(true) && player.getItemInHand().getTypeId() == DConfigUtil.getSettingInt("admin_wand_tool"))
 		{
 			if(DDataUtil.hasPlayerData(player, "temp_destroy_shrine") && System.currentTimeMillis() < DObjUtil.toLong(DDataUtil.getPlayerData(player, "temp_destroy_shrine")))
 			{
 				// We can destroy the Shrine
 				DDivineBlocks.removeShrine(location);
 				
 				// Drop the block of gold and book
 				location.getWorld().dropItemNaturally(location, new ItemStack(Material.GOLD_BLOCK, 1));
 				location.getWorld().dropItemNaturally(location, new ItemStack(Material.BOOK, 1));
 				
 				// Save Divine Blocks
 				DDatabase.saveDivineBlocks();
 				player.sendMessage(ChatColor.GREEN + "Shrine removed!");
 				return;
 			}
 			else
 			{
 				DDataUtil.savePlayerData(player, "temp_destroy_shrine", System.currentTimeMillis() + 5000);
 				player.sendMessage(ChatColor.RED + "Right-click this Shrine again to remove it.");
 				return;
 			}
 		}
 		
 		// Return if the player is mortal
 		if(!DCharUtil.isImmortal(event.getPlayer()))
 		{
 			event.getPlayer().sendMessage(ChatColor.RED + "You must be immortal to use that!");
 			return;
 		}
 		
 		useShrine(player, location);
 	}
 	
 	public void useShrine(Player player, Location location)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		try
 		{
 			// Check if block is divine
 			int shrineOwner = DDivineBlocks.getShrineOwner(location);
 			String shrineDeity = DDivineBlocks.getShrineDeity(location);
 			if(shrineDeity == null) return;
 						
 			if(DDivineBlocks.isShrineBlock(location))
 			{
 				// Check if character has deity
 				if(DCharUtil.hasDeity(charID, shrineDeity))
 				{
 					// Open the tribute inventory
 					Inventory ii = DMiscUtil.getPlugin().getServer().createInventory(player, 27, "Shrine of " + shrineDeity);
 					player.openInventory(ii);
 					DDataUtil.saveCharData(charID, "temp_tributing", shrineOwner);
 					return;
 				}
 				player.sendMessage(ChatColor.YELLOW + "You must be allied to " + shrineDeity + " in order to tribute here.");
 			}
 		}
 		catch(Exception e)
 		{
 			// Print error for debugging
 			e.printStackTrace();
 		}
 	}
 	
 	/* --------------------------------------------
 	 *  Handle Player Tributing
 	 * --------------------------------------------
 	 */	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void playerTribute(InventoryCloseEvent event)
 	{
 		try
 		{
 			if(!(event.getPlayer() instanceof Player)) return;
 			Player player = (Player)event.getPlayer();
 			int charID = DPlayerUtil.getCurrentChar(player);
 			String charDeity = DCharUtil.getDeity(charID);
 
 			if(!DCharUtil.isImmortal(player)) return;
 			
 			// If it isn't a tribute chest then break the method
 			if(!event.getInventory().getName().contains("Shrine")) return;
 			
 			// Get the creator of the shrine
 			int shrineOwner = DObjUtil.toInteger(DDataUtil.getCharData(charID, "temp_tributing"));
 			DDataUtil.removeCharData(charID, "temp_tributing"); 
 			
 			// Calculate value of chest
 			int tributeValue = 0, items = 0;
 			for(ItemStack ii : event.getInventory().getContents())
 			{
 				if(ii != null)
 				{
 					tributeValue += DTributeValue.getTributeValue(ii);
 					items++;
 				}
 			}
 			
 			tributeValue *= FAVOR_MULTIPLIER;
 			
 			// Process tributes and send messages
 			int favorBefore = DCharUtil.getMaxFavor(charID);
 			int devotionBefore = DCharUtil.getDevotion(charID);
 			
 			// Update the character's favor and devotion
 			DCharUtil.addMaxFavor(charID, tributeValue / 5);
 			DCharUtil.giveDevotion(charID, tributeValue);
 			
 			if(DCharUtil.getDevotion(charID) > devotionBefore) player.sendMessage(ChatColor.GRAY + "Your devotion to " + ChatColor.YELLOW +  charDeity + ChatColor.GRAY + " has increased to " + ChatColor.GREEN +  DCharUtil.getDevotion(charID) + ChatColor.GRAY + ".");
 			if(DCharUtil.getMaxFavor(charID) > favorBefore) player.sendMessage(ChatColor.GRAY + "Your favor cap has increased to " + ChatColor.GREEN +  DCharUtil.getMaxFavor(charID) + ChatColor.GRAY + ".");
 			
 			if(favorBefore != DCharUtil.getMaxFavor(charID) && devotionBefore != DCharUtil.getDevotion(charID) && items > 0)
 			{
 				// Update the shrine owner's devotion and let them know
				OfflinePlayer shrineOwnerPlayer = DCharUtil.getOwner(shrineOwner).getPlayer();
 				if(!DCharUtil.getOwner(charID).equals(shrineOwnerPlayer))
 				{
 					DCharUtil.giveDevotion(shrineOwner, tributeValue / 7);
 					if(shrineOwnerPlayer.isOnline())
 					{
 						((Player) shrineOwnerPlayer).sendMessage(ChatColor.YELLOW + "Someone just tributed at your shrine!");
 						((Player) shrineOwnerPlayer).sendMessage(ChatColor.GRAY + "Your devotion has increased to " + DCharUtil.getDevotion(shrineOwner) + "!");
 					}
 				}
 			}
 			else
 			{
 				// If they aren't good enough let them know
 				if(items > 0) player.sendMessage(ChatColor.RED + "Your tributes were insufficient for " + charDeity + "'s blessings.");
 			}
 			
 			// Clear the tribute case
 			event.getInventory().clear();
 		}
 		catch(Exception e)
 		{
 			// Print error for debugging
 			e.printStackTrace();
 		}
 	}
 	
 	/* --------------------------------------------
 	 *  Handle Altar Interactions
 	 * --------------------------------------------
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void altarInteract(PlayerInteractEvent event)
 	{
 		// Define variables
 		Player player = event.getPlayer();
 		Location location = player.getLocation();
 
 		// First we check if the player is in an Altar and return if not
 		if(DZoneUtil.zoneAltar(location) != null)
 		{
 			// Player is in an altar, let's do this
 			if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 			if(event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE) && !DPlayerUtil.isPraying(player))
 			{
 				DMiscUtil.togglePlayerChat(player, false);
 				DMiscUtil.togglePlayerStuck(player, true);
 				DPlayerUtil.togglePraying(player, true);
 				
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.AQUA + "-- Now Praying ----------------------------------------");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " To begin, please choose an option by entering it in the chat:");
 				player.sendMessage(" ");
 				
 				if(DDataUtil.hasPlayerData(player, "temp_createchar_finalstep") && DDataUtil.getPlayerData(player, "temp_createchar_finalstep").equals(true))
 				{
 					player.sendMessage(ChatColor.GRAY + "   [1a.] " + ChatColor.GREEN + "Confirm New Character");	
 				}
 				else player.sendMessage(ChatColor.GRAY + "   [1.] " + ChatColor.GREEN + "Create New Character");
 				
 				player.sendMessage(ChatColor.GRAY + "   [2.] " + ChatColor.RED + "Remove Character");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " While using an Altar you are unable to move or chat.");
 				player.sendMessage(ChatColor.GRAY + " You can return to the main menu at anytime by typing \"menu\".");
 				player.sendMessage(ChatColor.GRAY + " Right-click the Altar again to stop Praying.");
 				player.sendMessage(" ");
 
 				event.setCancelled(true);
 				return;
 			}
 			else if(event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE) && DPlayerUtil.isPraying(player))
 			{
 				DMiscUtil.togglePlayerChat(player, true);
 				DMiscUtil.togglePlayerStuck(player, false);
 				DPlayerUtil.togglePraying(player, false);
 				
 				// Clear whatever is being worked on in this Pray session
 				DDataUtil.removePlayerData(player, "temp_createchar");
 				
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " Your movement and chat has been re-enabled.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.AQUA + "-- No Longer Praying ----------------------------------");
 				player.sendMessage(" ");
 
 				event.setCancelled(true);
 				return;
 			}
 		}
 		return;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void altarChatEvent(AsyncPlayerChatEvent event)
 	{
 		// Define variables
 		Player player = event.getPlayer();
 		Location location = player.getLocation();
 		
 		// First we check if the player is in an Altar and currently praying, if not we'll return
 		if(DZoneUtil.zoneAltar(location) != null && DPlayerUtil.isPraying(player))
 		{
 			// Cancel their chat
 			event.setCancelled(true);
 			
 			// Define variables
 			String message = event.getMessage();
 			String chosenName = (String) DDataUtil.getPlayerData(player, "temp_createchar_name");
 			String chosenDeity = (String) DDataUtil.getPlayerData(player, "temp_createchar_deity");
 			
 			// Return to main menu
 			if(message.equalsIgnoreCase("menu"))
 			{
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.AQUA + "-- Now Praying ----------------------------------------");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " To begin, please choose an option by entering it in the chat:");
 				player.sendMessage(" ");
 				
 				if(DDataUtil.hasPlayerData(player, "temp_createchar_finalstep") && DDataUtil.getPlayerData(player, "temp_createchar_finalstep").equals(true))
 				{
 					player.sendMessage(ChatColor.GRAY + "   [1a.] " + ChatColor.GREEN + "Confirm New Character");	
 				}
 				else player.sendMessage(ChatColor.GRAY + "   [1.] " + ChatColor.GREEN + "Create New Character");
 				
 				player.sendMessage(ChatColor.GRAY + "   [2.] " + ChatColor.RED + "Remove Character");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " While using an Altar you are unable to move or chat.");
 				player.sendMessage(ChatColor.GRAY + " You can return to the main menu at anytime by typing \"menu\".");
 				player.sendMessage(ChatColor.GRAY + " Right-click the Altar again to stop Praying.");
 				player.sendMessage(" ");
 				
 				// Remove now useless data
 				DDataUtil.removePlayerData(player, "temp_createchar");
 				return;	
 			}
 			
 			// Next check for temporary data to determine what they are doing
 			if(DDataUtil.hasPlayerData(player, "temp_createchar"))
 			{
 				// Step 1 of character creation
 				if(DDataUtil.getPlayerData(player, "temp_createchar").equals("choose_name"))
 				{
 					if(message.length() >= 15 || !StringUtils.isAlphanumeric(message) || DPlayerUtil.hasCharName(player, message))
 					{
 						// They didn't accept the name, let them re-choose
 						DDataUtil.savePlayerData(player, "temp_createchar", "choose_name");
 						if(message.length() >= 15) player.sendMessage(ChatColor.RED + " That name is too long.");
 						if(!StringUtils.isAlphanumeric(message)) player.sendMessage(ChatColor.RED + " You can only use Alpha-Numeric characters.");
 						if(DPlayerUtil.hasCharName(player, message)) player.sendMessage(ChatColor.RED + " You already have a character with that name.");
 						player.sendMessage(ChatColor.AQUA + " Enter a different name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 						player.sendMessage(" ");
 						return;
 					}
 					else
 					{
 						chosenName = message.replace(" ", "");
 						player.sendMessage(ChatColor.AQUA + " Are you sure you want to use " + ChatColor.YELLOW + chosenName + ChatColor.AQUA + "?" + ChatColor.GRAY + " (y/n)");
 						player.sendMessage(" ");
 						DDataUtil.savePlayerData(player, "temp_createchar_name", chosenName);
 						DDataUtil.savePlayerData(player, "temp_createchar", "confirm_name");
 						return;
 					}
 
 				}
 				
 				// Step 2 of character creation
 				if(DDataUtil.getPlayerData(player, "temp_createchar").equals("confirm_name"))
 				{
 					if(message.equalsIgnoreCase("y") || message.contains("yes"))
 					{
 						// They accepted the name, let's continue
 						player.sendMessage(ChatColor.AQUA + " Please choose a Deity: " + ChatColor.GRAY + "(Type in the name of the Deity)");
 						for(String alliance : DDeityUtil.getLoadedDeityAlliances())
 						{
 							for(String deity : DDeityUtil.getAllDeitiesInAlliance(alliance)) player.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.YELLOW + DObjUtil.capitalize(deity)  + ChatColor.GRAY + " (" + alliance + ")");	
 						}
 						player.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.YELLOW + "_Alex" + ChatColor.GRAY + " (Boss)");	
 						player.sendMessage(" ");
 
 						DDataUtil.savePlayerData(player, "temp_createchar", "choose_deity");
 						return;
 					}
 					else
 					{
 						// They didn't accept the name, let them re-choose
 						DDataUtil.savePlayerData(player, "temp_createchar", "choose_name");
 						player.sendMessage(ChatColor.AQUA + " Enter a name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 						player.sendMessage(" ");
 						return;
 					}
 				}
 				
 				if(DDataUtil.getPlayerData(player, "temp_createchar").equals("choose_deity"))
 				{
 					// Check their chosen Deity
 					for(String alliance : DDeityUtil.getLoadedDeityAlliances())
 					{
 						for(String deity : DDeityUtil.getAllDeitiesInAlliance(alliance))
 						{
 							if(message.equalsIgnoreCase(deity))
 							{
 								// Their chosen deity matches an existing deity, ask for confirmation
 								chosenDeity = message.replace(" ", "");
 								player.sendMessage(ChatColor.AQUA + " Are you sure you want to use " + ChatColor.YELLOW + DObjUtil.capitalize(chosenDeity) + ChatColor.AQUA + "?" + ChatColor.GRAY + " (y/n)");
 								player.sendMessage(" ");
 								DDataUtil.savePlayerData(player, "temp_createchar_deity", chosenDeity);
 								DDataUtil.savePlayerData(player, "temp_createchar", "confirm_deity");
 								return;
 							}
 						}
 					}
 					if(message.equalsIgnoreCase("_Alex"))
 					{
 						player.sendMessage(ChatColor.AQUA + " Well you can't be _Alex... but he is awesome!");
 						player.sendMessage(" ");
 
 						// They can't be _Alex silly! Make them re-choose
 						player.sendMessage(ChatColor.AQUA + " Choose a different Deity: " + ChatColor.GRAY + "(Type in the name of the Deity)");
 						for(String alliance : DDeityUtil.getLoadedDeityAlliances())
 						{
 							for(String deity : DDeityUtil.getAllDeitiesInAlliance(alliance)) player.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.YELLOW + DObjUtil.capitalize(deity)  + ChatColor.GRAY + " (" + alliance + ")");	
 						}
 						player.sendMessage(" ");
 						DDataUtil.savePlayerData(player, "temp_createchar", "choose_deity");
 						return;
 					}
 				}
 				
 				if(DDataUtil.getPlayerData(player, "temp_createchar").equals("confirm_deity"))
 				{
 					if(message.equalsIgnoreCase("y") || message.contains("yes"))
 					{
 						// They accepted the Deity choice, now ask them to input their items so they can be accepted
 						player.sendMessage(ChatColor.AQUA + " Before you can confirm your lineage with " + ChatColor.YELLOW + chosenDeity + ChatColor.AQUA + ", you must");
 						player.sendMessage(ChatColor.AQUA + " first sacrifice the following items:");
 						player.sendMessage(" ");
 						for(Material item : (ArrayList<Material>) DDataUtil.getPluginData("temp_deity_claim_items", chosenDeity))
 						{
 							player.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.YELLOW + item.name());
 						}
 						player.sendMessage(" ");
 						player.sendMessage(ChatColor.GRAY + " After you obtain these items, return to an Altar and select");
 						player.sendMessage(ChatColor.GRAY + " the option to confirm your new character.");
 						player.sendMessage(" ");
 
 						DDataUtil.savePlayerData(player, "temp_createchar_finalstep", true);
 						return;
 					}
 					else
 					{
 						// They didn't accept the name, let them re-choose
 						player.sendMessage(ChatColor.AQUA + " Choose a different Deity: ");
 						for(String alliance : DDeityUtil.getLoadedDeityAlliances())
 						{
 							for(String deity : DDeityUtil.getAllDeitiesInAlliance(alliance)) player.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.YELLOW + DObjUtil.capitalize(deity)  + ChatColor.GRAY + " (" + alliance + ")");	
 						}
 						player.sendMessage(" ");
 						DDataUtil.savePlayerData(player, "temp_createchar", "choose_deity");
 						return;
 					}
 				}
 				
 				if(DDataUtil.getPlayerData(player, "temp_createchar").equals("confirm_all"))
 				{
 					if(message.equalsIgnoreCase("y") || message.contains("yes"))
 					{
 						Inventory ii = DMiscUtil.getPlugin().getServer().createInventory(player, 27, "Place Your Tributes Here");
 						player.openInventory(ii);
 					}
 					else
 					{
 						
 					}
 				}
 			}
 			
 			// Create Character
 			if(message.equals("1") || message.contains("create new character"))
 			{
 				player.sendMessage(ChatColor.GREEN + " Now creating a new character...");
 				player.sendMessage(" ");
 				DDataUtil.savePlayerData(player, "temp_createchar", "choose_name");
 				player.sendMessage(ChatColor.AQUA + " Enter a name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 				player.sendMessage(" ");
 				return;
 			}
 			
 			// Finish Create Character
 			if(message.equals("1a") || message.contains("confirm new character") && DDataUtil.hasPlayerData(player, "temp_createchar_finalstep"))
 			{
 				DDataUtil.savePlayerData(player, "temp_createchar_finalstep", true);
 				DDataUtil.savePlayerData(player, "temp_createchar", "confirm_all");
 				
 				player.sendMessage(ChatColor.GREEN + " Now confirming your new character...");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.AQUA + " Do you have the following items in your inventory?" + ChatColor.GRAY + " (y/n)");
 				player.sendMessage(" ");
 				for(Material item : (ArrayList<Material>) DDataUtil.getPluginData("temp_deity_claim_items", chosenDeity))
 				{
 					player.sendMessage(ChatColor.GRAY + "  -> " + ChatColor.YELLOW + item.name());
 				}
 				player.sendMessage(" ");
 				return;
 			}
 						
 			// Remove Character
 			else if(message.equals("2") || message.contains("remove character"))
 			{
 				player.sendMessage(ChatColor.GRAY + "Currently Unavailable. Use /removechar <name>");
 				return;	
 			}
 		}
 		return;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void altarDeityConfirmation(InventoryCloseEvent event)
 	{
 		try
 		{
 			if(!(event.getPlayer() instanceof Player)) return;
 			Player player = (Player) event.getPlayer();
 
 			// If it isn't a confirmation chest then exit
 			if(!event.getInventory().getName().contains("Place Your Tributes Here")) return;
 						
 			// Exit if this isn't for character creation
 			if(!DPlayerUtil.isPraying(player) || !DDataUtil.hasPlayerData(player, "temp_createchar_finalstep") || DDataUtil.getPlayerData(player, "temp_createchar_finalstep").equals(false))
 			{
 				player.sendMessage(ChatColor.RED + "(ERR: 2003) Please report this to an admin immediately.");
 				return;
 			}
 			
 			// Define variables
 			String chosenName = (String) DDataUtil.getPlayerData(player, "temp_createchar_name");
 			String chosenDeity = (String) DDataUtil.getPlayerData(player, "temp_createchar_deity");
 			String deityAlliance = DObjUtil.capitalize(DDeityUtil.getDeityAlliance(chosenDeity));
 			
 			// Check the chest items
 			int items = 0;
 			int neededItems = ((ArrayList<Material>) DDataUtil.getPluginData("temp_deity_claim_items", chosenDeity)).size();
 		
 			for(ItemStack ii : event.getInventory().getContents())
 			{
 				if(ii != null)
 				{
 					for(Material item : (ArrayList<Material>) DDataUtil.getPluginData("temp_deity_claim_items", chosenDeity))
 					{
 						if(ii.getType().equals(item))
 						{
 							items++;
 						}
 					}
 				}
 			}
 			
 			player.sendMessage(ChatColor.YELLOW + "The " + deityAlliance + "s are pondering your offerings...");
 			if(neededItems == items)
 			{
 				// They were accepted, finish everything up!
 				DCharUtil.createChar(player, chosenName, chosenDeity);
 				DDataUtil.removePlayerData(player, "temp_createchar");
 				player.sendMessage(ChatColor.GREEN + "You have been accepted into the lineage of " + chosenDeity + "!");
 				player.getWorld().strikeLightningEffect(player.getLocation());
 				for (int i=0;i<20;i++) player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
 				
 				// Stop their praying, enable movement, enable chat
 				DMiscUtil.togglePlayerChat(player, true);
 				DMiscUtil.togglePlayerStuck(player, false);
 				DPlayerUtil.togglePraying(player, false);
 				
 				// Remove old data now
 				DDataUtil.removePlayerData(player, "temp_createchar_finalstep");
 				DDataUtil.removePlayerData(player, "temp_createchar_name");
 				DDataUtil.removePlayerData(player, "temp_createchar_deity");
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + "You have been denied entry into the lineage of " + chosenDeity + "!");
 			}
 			
 			// Clear the confirmation case
 			event.getInventory().clear();
 		}
 		catch(Exception e)
 		{
 			// Print error for debugging
 			e.printStackTrace();
 		}
 	}
 	
 	/* --------------------------------------------
 	 *  Handle Miscellaneous Divine Block Events
 	 * --------------------------------------------
 	 */	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void demigodsAdminWand(PlayerInteractEvent event)
 	{
 		if(event.getClickedBlock() == null) return;
 		
 		// Define variables
 		Block clickedBlock = event.getClickedBlock();
 		Location location = clickedBlock.getLocation();
 		Player player = event.getPlayer();
 
 		// Return if the player does not qualify for use of the admin wand
 		if(!DMiscUtil.hasPermissionOrOP(player, "demigods.admin") || !DDataUtil.hasPlayerData(player, "temp_admin_wand") || DDataUtil.getPlayerData(player, "temp_admin_wand").equals(false) || player.getItemInHand().getTypeId() != DConfigUtil.getSettingInt("admin_wand_tool")) return;
 		
 		if(clickedBlock.getType().equals(Material.EMERALD_BLOCK))
 		{
 			player.sendMessage(ChatColor.GRAY + "Generating new Altar...");
 			DDivineBlocks.createAltar(location.add(0, 2, 0));
 			player.sendMessage(ChatColor.GREEN + "Altar created!");
 		}
 		
 		if(DDivineBlocks.isAltarBlock(location) && DDivineBlocks.isDivineBlock(location))
 		{
 			if(DDataUtil.hasPlayerData(player, "temp_destroy_altar") && System.currentTimeMillis() < DObjUtil.toLong(DDataUtil.getPlayerData(player, "temp_destroy_altar")))
 			{
 				// We can destroy the Shrine
 				DDivineBlocks.removeAltar(location);
 				
 				// Save Divine Blocks
 				DDatabase.saveDivineBlocks();
 				player.sendMessage(ChatColor.GREEN + "Altar removed!");
 				return;
 			}
 			else
 			{
 				DDataUtil.savePlayerData(player, "temp_destroy_altar", System.currentTimeMillis() + 5000);
 				player.sendMessage(ChatColor.RED + "Right-click this Altar again to remove it.");
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void divineBlockAlerts(PlayerMoveEvent event)
 	{
 		if(event.getFrom().distance(event.getTo()) < 0.1) return;
 		
 		// Define variables
 		Player player = event.getPlayer();
 		Location to = event.getTo();
 		Location from = event.getFrom();
 		DivineBlock divineBlock = null;
 		OfflinePlayer charOwner = null;
 		
 		/* ------------------------------------
 		 * Altar Zone Messages
 		 * -----------------------------------
 		 * -> Entering Altar
 		 */
 		if(DZoneUtil.enterZoneAltar(to, from))
 		{
 			player.sendMessage(ChatColor.GRAY + "You have entered an Altar.");
 			return;
 		}
 		
 		// Leaving Altar
 		else if(DZoneUtil.exitZoneAltar(to, from))
 		{
 			player.sendMessage(ChatColor.GRAY + "You have left an Altar.");
 			return;
 		}
 		
 		/* ------------------------------------
 		 * Shrine Zone Messages
 		 * -----------------------------------
 		 * -> Entering Shrine
 		 */
 		if(DZoneUtil.enterZoneShrine(to, from) && DZoneUtil.zoneShrineOwner(to) != -1)
 		{
 			divineBlock = DZoneUtil.zoneShrine(to);
 			charOwner = DCharUtil.getOwner(DZoneUtil.zoneShrineOwner(to));
 			player.sendMessage(ChatColor.GRAY + "You have entered " + charOwner.getName() + "'s shrine to " + ChatColor.YELLOW + DDivineBlocks.getShrineDeity(divineBlock.getLocation()) + ChatColor.GRAY + ".");
 			return;
 		}
 		
 		// Leaving Shrine
 		else if(DZoneUtil.exitZoneShrine(to, from))
 		{
 			player.sendMessage(ChatColor.GRAY + "You have left a holy area.");
 			return;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void stopDestroyEnderCrystal(EntityDamageEvent event)
 	{
 		try
 		{
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(event.getEntity().getLocation().subtract(0.5, 1.0, 0.5).equals(divineBlock))
 				{
 					 event.setDamage(0);
 					 event.setCancelled(true);
 					 return;
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void stopDestroyDivineBlock(BlockBreakEvent event)
 	{
 		try
 		{
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(event.getBlock().getLocation().equals(divineBlock))
 				{
 					event.getPlayer().sendMessage(ChatColor.YELLOW + "Divine blocks cannot be broken by hand.");
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void stopDivineBlockDamage(BlockDamageEvent event)
 	{
 		try
 		{
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(event.getBlock().getLocation().equals(divineBlock))
 				{
 					event.setCancelled(true);
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void stopDivineBlockIgnite(BlockIgniteEvent event)
 	{
 		try
 		{
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(event.getBlock().getLocation().equals(divineBlock))
 				{
 					event.setCancelled(true);
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void stopDivineBlockBurn(BlockBurnEvent event)
 	{
 		try
 		{
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(event.getBlock().getLocation().equals(divineBlock))
 				{
 					event.setCancelled(true);
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void stopDivineBlockPistonExtend(BlockPistonExtendEvent event)
 	{		
 		List<Block> blocks = event.getBlocks();
 		
 		CHECKBLOCKS:
 		for(Block block : blocks)
 		{
 			try
 			{
 				for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 				{
 					if(block.getLocation().equals(divineBlock))
 					{
 						event.setCancelled(true);
 						break CHECKBLOCKS;
 					}
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void stopDivineBlockPistonRetract(BlockPistonRetractEvent event)
 	{
 		// Define variables
 		final Block block = event.getBlock().getRelative(event.getDirection(), 2);
 		
 		try
 		{
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(block.getLocation().equals((divineBlock)) && event.isSticky())
 				{
 					event.setCancelled(true);
 				}
 			}
 		}
 		catch(Exception e) {}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void divineBlockExplode(final EntityExplodeEvent event)
 	{
 		// Remove divineBlock blocks from explosions
 		final ArrayList<Block> savedBlocks = new ArrayList<Block>();
 		final ArrayList<Material> savedMaterials = new ArrayList<Material>();
 		final ArrayList<Byte> savedBytes = new ArrayList<Byte>();
 		
 		List<Block> blocks = event.blockList();
 		for(Block block : blocks)
 		{
 			if(DZoneUtil.zoneNoPVP(block.getLocation()))
 			{
 				savedBlocks.add(block);
 				savedMaterials.add(block.getType());
 				savedBytes.add(block.getData());
 				continue;
 			}
 			
 			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
 			{
 				if(block.getLocation().equals(divineBlock))
 				{
 					savedBlocks.add(block);
 					savedMaterials.add(block.getType());
 					savedBytes.add(block.getData());
 					break;
 				}
 			}
 		}
 		
 		DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// Regenerate blocks
 				int i = 0;
 				for(Block block : savedBlocks)
 				{
 						block.setTypeIdAndData(savedMaterials.get(i).getId(), savedBytes.get(i), true);
 						i++;
 				}
 				
 				// Remove all drops from explosion zone
 				for(Item drop : event.getLocation().getWorld().getEntitiesByClass(Item.class))
 				{
 				    Location location = drop.getLocation();
 				    if(DZoneUtil.zoneAltar(location) != null)
 					{
 						drop.remove();
 						continue;
 					}
 					
 					if(DZoneUtil.zoneShrine(location) != null)
 					{
 						drop.remove();
 						continue;
 					}
 				}
 			}
 		}, 1);
 	}
 }
