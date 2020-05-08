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
 	
 	}
 	
 	public void onDisable(){
 		
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		
 		// Listen for the command
 		if(cmd.getName().equalsIgnoreCase("disenchant")){
 		
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
							
							//First confiscate the soul emerald...somehow
							// We'll try creating a new item stack with the same meta data, but with a quantity of one.
							if (checkval >= 1){
								ItemMeta emerMeta = emeraldCheck.getItemMeta();
								ItemStack removeEmerald = new ItemStack(Material.EMERALD);
								removeEmerald.setItemMeta(emerMeta);
								removeEmerald.setAmount(1);
								p.getInventory().removeItem(removeEmerald);
							} 
							
 							// Here we should check the durability of an item, and reject it if it's damaged.
 							ItemStack duraControl = new ItemStack(item.getTypeId());
 							int maxDura = duraControl.getDurability();
 							int currentDura = item.getDurability();
 							int remainder = maxDura-currentDura;
 							if(remainder == 0){
 	
 								// Begin bukkit runnable for the delayed task.
 								if(checkval >= 1){
 									p.sendMessage(ChatColor.GREEN+"[Disenchanter] The ritual will begin...");
 									p.setItemInHand(new ItemStack(Material.AIR));
 									
 									// Confiscate the soul emerald...somehow
 									// We'll try creating a new item stack with the same meta data, but with a quantity of one.
 									
 									if (checkval >= 1){
 										// Populate information to remove the emerald. For some reason, a new item stack is needed.
 										ItemMeta emerMeta = emeraldCheck.getItemMeta();
 										ItemStack removeEmerald = new ItemStack(Material.EMERALD);
 										removeEmerald.setItemMeta(emerMeta);
 										removeEmerald.setAmount(1);
 										p.getInventory().removeItem(removeEmerald);
 										// Remove the existing blank book from the player's inventory
 										p.getInventory().removeItem(new ItemStack(Material.BOOK, 1));			
 									} 
 									
 								}
 								getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Disenchanter"), new Runnable(){
 									public void run(){
 								
 										// Reject the process if the item has no enchantment. This may be the best way to prevent unwanted vaporisings.
 										
 											if(checkval >= 1){
 					
 													// Initialise the book to be given to the player
 													ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
 							
 																			
 													
 													// Retrieve a set of the enchantments and values, and convert it to a split string
 													Set<Enchantment> keySet = item.getEnchantments().keySet();
 													String enchString[] = keySet.toString().split(", E");
 													// find out how many enchantments were on the item
 													int numberOfEntries = keySet.size();
 													// this is a conversion for the index of split strings - it starts counting at zero
 													int i = numberOfEntries - 1;
 													int enchID = -1;
 													
 													// Logic to randomise which enchantment is passed on, should there be more than one.
 													if (numberOfEntries > 1){
 														Random rand = new Random();
 														i = rand.nextInt(numberOfEntries);
 														enchID = Integer.parseInt(enchString[i].replaceAll("[\\D]", ""));
 													} else{
 														enchID = Integer.parseInt(enchString[i].replaceAll("[\\D]",""));
 													}
 						
 													//Create a new Enchantment object to load with info, namely the ID found above.
 													Enchantment thisgoesonthebook = new EnchantmentWrapper(enchID);
 													//Pull the enchantment level from the original item using the enchantment above
 													int enchantLVL = item.getEnchantmentLevel(thisgoesonthebook);
 													// Clone the existing, empty meta from a blank enchanted book
 													EnchantmentStorageMeta meta = (EnchantmentStorageMeta)book.getItemMeta();
 													// Populate the meta data with the information gained above.
 													meta.addStoredEnchant(thisgoesonthebook, enchantLVL, true);
 													// Set the meta data onto our new book.
 													book.setItemMeta(meta);
 					
 													// Put the book with enchantmentmetadata loaded into the player's inventory, not hand.
 													p.getInventory().addItem(book);
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
 			return false;
 	}
 }
