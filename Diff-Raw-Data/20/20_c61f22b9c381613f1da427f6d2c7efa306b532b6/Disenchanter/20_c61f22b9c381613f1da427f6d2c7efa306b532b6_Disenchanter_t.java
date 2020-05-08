 package com.gmail.xthompson13.Disenchanter;
 
 import java.util.Set;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.enchantments.EnchantmentWrapper;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.inventory.meta.EnchantmentStorageMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 
 
 public class Disenchanter extends JavaPlugin{
 
 	public void onEnable(){
 		getConfig().options().copyDefaults(true);
 		getConfig().addDefault("multi-mode", "false");
 		saveConfig();
 	}
 	
 	public void onDisable(){
 		saveConfig();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		
 	
 	// Filter for console sending the command
 	if(sender instanceof Player){
 		// Listen for the command
 		if(cmd.getName().equalsIgnoreCase("disenchanter")){
 			if(sender.hasPermission("disenchant.admin")){
				if(args.length > 0){
					if(args[0].equalsIgnoreCase("set")){
						if(args.length == 3){
							getConfig().set(args[1], args[2]);
							sender.sendMessage(ChatColor.GREEN+"[Disenchanter] Config option: "+ChatColor.GRAY+args[1]+ChatColor.GREEN+" set to: "+ChatColor.GRAY+args[2]);
							saveConfig();
							return true;
						}
 					}
 				}
				if(args.length == 0){
					sender.sendMessage(ChatColor.RED+"[Disenchanter] Did you mean to use /disenchant ?");
					return true;
				}
				
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("disenchant") && args.length == 0){
 		
 		// Initialise the player
 		final Player p = (Player) sender;
 		
 			// Check for permissions
 			if(p.hasPermission("disenchant.disenchant")){			
 				
 				// Check to see if the player is holding something
 				if(p.getItemInHand().getAmount() != 0) {
 					
 				// Check to see if it's night time
 				long time = getServer().getWorld(p.getWorld().getName()).getTime();
 				if(time > 13500 && time < 22500){
 					
 					// Check to see if the player has the book in his/her inventory
 					if(p.getInventory().contains(340) == true){
 						// Check to see if the player is carrying an emerald
 						if(p.getInventory().contains(388) == true){
 							
 							// Now, get the emerald and check to see what it is named
 							int loc;
 							loc = p.getInventory().getHeldItemSlot();
 							loc++;
 							String emeraldName;
 							ItemStack emeraldCheck = p.getInventory().getItem(loc);
 							if(emeraldCheck == null){
 								emeraldName = "wrong";
 							} else{
 								if (p.getInventory().getItem(loc).getItemMeta().hasDisplayName() == true){
 								emeraldName = p.getInventory().getItem(loc).getItemMeta().getDisplayName();
 								} else {
 									emeraldName = "wrong";
 								}
 							}
 														
 							int comparison;
 							comparison = emeraldName.compareToIgnoreCase("Soul");
 							if(comparison == 0){
 							
 							
 							
 							//now we can make the item, because we'll need to check durability. we'll also pull the number of enchantments it has.
 							final ItemStack item = p.getItemInHand();
 							final int checkval = item.getEnchantments().size();
 														
 							// Here we should check the durability of an item, and reject it if it's damaged.
 							ItemStack duraControl = new ItemStack(item.getTypeId());
 							int maxDura = duraControl.getDurability();
 							int currentDura = item.getDurability();
 							int remainder = maxDura-currentDura;
 							if(remainder == 0){
 	
 								// Begin bukkit runnable for the delayed task.
 								if(checkval >= 1){
 									
 									if (checkval >= 1){
 										
 										if(!p.getInventory().containsAtLeast(new ItemStack(Material.BOOK), sacrificeToRemove(item))){
 											p.sendMessage(ChatColor.RED+"[Disenchanter] You don't have enough books.");
 											return true;
 										}
 										// Populate information to remove the emerald. For some reason, a new item stack is needed.
 										ItemMeta emerMeta = emeraldCheck.getItemMeta();
 										ItemStack removeEmerald = new ItemStack(Material.EMERALD);
 										removeEmerald.setItemMeta(emerMeta);
 										if(!p.getInventory().containsAtLeast(removeEmerald, sacrificeToRemove(item))){
 											p.sendMessage(ChatColor.RED+"[Disenchanter] You don't have enough Soul Emeralds.");
 											return true;
 										}
 										p.sendMessage(ChatColor.GREEN+"[Disenchanter] The ritual will begin...");
 										p.setItemInHand(new ItemStack(Material.AIR));
 
 										removeEmerald.setAmount(sacrificeToRemove(item));
 										p.getInventory().removeItem(removeEmerald);
 										
 										//book removal
 										int bookRemoveCount = 1;
 										while (bookRemoveCount <= sacrificeToRemove(item)){									
 											p.getInventory().removeItem(new ItemStack(Material.BOOK, 1));
 											bookRemoveCount++;
 										}
 									} 
 									
 								}
 								getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Disenchanter"), new Runnable(){
 									public void run(){
 								
 										// Reject the process if the item has no enchantment. This may be the best way to prevent unwanted vaporisings.
 										
 											if(checkval >= 1){
 													unwrapEnchants(item, p);
 													p.sendMessage(ChatColor.GREEN+"[Disenchanter] The ritual is complete.");
 					
 											} else {
 												p.sendMessage(ChatColor.RED+"[Disenchanter] The ritual failed...the item had no enchantments...");
 											} // this ends the if-end statement that ensures the item can be disenchanted.
 							}
 						}, 120L);
 					} else { // this ends the durability check
 						p.sendMessage(ChatColor.RED+"[Disenchanter] You can only perform the ritual on undamaged items.");
 					}
 					} else {// this block ends the emerald NAME check
 						p.sendMessage(ChatColor.RED+"[Disenchanter] You need a Soul emerald next to your item...");
 					}
 					} else {// this block ends the emerald check
 						p.sendMessage(ChatColor.RED+"[Disenchanter] You need a Soul emerald next to your item...");
 					}
 					} else {
 						p.sendMessage(ChatColor.RED+"[Disenchanter] You must have a book to enchant.");
 					} // this block ends the book-in-inventory check
 					} else {
 						p.sendMessage(ChatColor.RED+"[Disenchanter] You can only perform arcane rituals at night.");
 					} // this block ends the time check
 					} else {
 						p.sendMessage(ChatColor.RED+"[Disenchanter] You must be holding something to perform the ritual.");
 					} // this block ends the item-in-hand check
 					
 			} else{
 				p.sendMessage(ChatColor.RED+"[Disenchanter] You do not have permission.");
 			}// ends the if-end statement denying permission
 
 			return true;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("whatname")){
 			
 			if(sender.hasPermission("disenchant.whatname")){
 			Player pn = (Player) sender;
 			String itemName = "There is nothing in your hand.";
 			if(pn.getItemInHand().getAmount() != 0){
 				itemName = (String) pn.getItemInHand().getItemMeta().getDisplayName();
 			}
 			if(pn.getItemInHand().getAmount() == 0){
 				itemName = "There is nothing in your hand.";
 			}
 			pn.sendMessage(ChatColor.GREEN+"[Disenchanter] You are wielding: "+itemName);
 			int loc; 
 			loc = pn.getInventory().getHeldItemSlot();
 			pn.sendMessage(ChatColor.GREEN+"[Disenchanter] It is in slotID: "+loc);
 			
 			return true;
 		}
 		}
 			
 	} else { sender.sendMessage("You cannot run this command from console.");}
 	return false;
 	}
 	
 	public void unwrapEnchants(ItemStack item, Player p){
 		// prep an enchanted book to return
 		ItemStack newBook = new ItemStack(Material.ENCHANTED_BOOK);
 		
 		// prep some BookMeta yo
 		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) newBook.getItemMeta();
 		
 		// Get a set of the enchantments listed for our item
 		Set<Enchantment> keySet = item.getEnchantments().keySet();
 		
 		// Get a string array that tells me crudely what was on the item
 		String enchString[] = keySet.toString().split(", E");
 		
 		// find out how many enchantments were on the item. Then we can "WHILE" it for all enchants or randomise for random enchants!
 		int numberOfEntries = keySet.size();
 		
 		// get a -1 variable to use in string searching and cutting
 		int i = numberOfEntries - 1;
 		
 		// prep a variable to receive the enchantment ID
 		int enchID = -1;
 		
 		// prep a variable to receive the enchantment level
 		int enchLevel = 1;
 		
 		//
 		
 		
 		if (numberOfEntries == 1){
 			enchID = Integer.parseInt(enchString[i].replaceAll("[\\D]",""));
 			Enchantment enchant = new EnchantmentWrapper(enchID);
 			enchLevel = item.getEnchantmentLevel(enchant); 
 			meta.addStoredEnchant(enchant, enchLevel, true);
 			newBook.setItemMeta(meta);
 			p.getInventory().addItem(newBook);
 		}
 		if (numberOfEntries > 1){
 			if(getConfig().getString("multi-mode").equalsIgnoreCase("false")){
 				Random rand = new Random();
 				i = rand.nextInt(numberOfEntries);
 				enchID = Integer.parseInt(enchString[i].replaceAll("[\\D]", ""));
 				Enchantment enchant = new EnchantmentWrapper(enchID);
 				enchLevel = item.getEnchantmentLevel(enchant); 
 				meta.addStoredEnchant(enchant, enchLevel, true);
 				newBook.setItemMeta(meta);
 				p.getInventory().addItem(newBook);
 			}
 			if(getConfig().getString("multi-mode").equalsIgnoreCase("true")){
 				// get a counter going
 				int count = 0;
 				while(count <= i){
 					enchID = Integer.parseInt(enchString[count].replaceAll("[\\D]",""));
 					Enchantment enchant = new EnchantmentWrapper(enchID);
 					enchLevel = item.getEnchantmentLevel(enchant);
 					EnchantmentStorageMeta multiMeta = freshMeta();
 					multiMeta.addStoredEnchant(enchant, enchLevel, true);
 					ItemStack multiBook = freshBook();
 					multiBook.setItemMeta(multiMeta);
 					count++;
 					p.getInventory().addItem(multiBook);
 				}
 				
 			}
 		}
 		
 
 	}
 	public ItemStack freshBook(){
 		ItemStack freshBook = new ItemStack(Material.ENCHANTED_BOOK);
 		return freshBook;
 	}
 	
 	public EnchantmentStorageMeta freshMeta(){
 		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
 		EnchantmentStorageMeta freshMeta = (EnchantmentStorageMeta) book.getItemMeta();
 		return freshMeta;
 	}
 	
 	public int sacrificeToRemove(ItemStack item){
 		if(getConfig().getString("multi-mode").equalsIgnoreCase("true") == true){
 		
 			Set<Enchantment> keySet = item.getEnchantments().keySet();
 			String enchString[] = keySet.toString().split(", E");
 			return enchString.length;
 		} else return 1;
 		
 	}
 }
