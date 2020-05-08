 package com.webkonsept.bukkit.simplechestlock.listener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.webkonsept.bukkit.simplechestlock.SCL;
 import com.webkonsept.bukkit.simplechestlock.locks.SCLItem;
 
 public class SCLPlayerListener implements Listener {
 	final SCL plugin;
 	
 	public SCLPlayerListener(SCL instance) {
 		plugin = instance;
 	}
 	private String ucfirst(String string){
 		return string.substring(0,1).toUpperCase() + string.substring(1);
 	}
 	
 	// TODO: Break up this monster method, plx!
 	@EventHandler
 	public void onPlayerInteract (final PlayerInteractEvent event){
 		if (! plugin.isEnabled() ) return;
 		if ( event.isCancelled() ) return;
 		Block block = event.getClickedBlock();
 		
 		
 		ItemStack toolUsed = null;
 		
 		if (event.getItem() != null){
 			toolUsed = event.getItem().clone();
 			toolUsed.setAmount(1);
 		}
 		Player player = event.getPlayer();
 		
 		if (block == null) return;  // We don't care about non-block (air) interactions.
 		if (plugin.canLock(block)){
 			String typeName = block.getType().toString().replaceAll("_", " ").toLowerCase();
 			if(
 					event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
 					|| ( 
 						event.getAction().equals(Action.LEFT_CLICK_BLOCK)
 						&& plugin.leftLocked.contains(block.getType())
 						&& !(plugin.toolMatch(toolUsed,plugin.cfg.key()) || plugin.toolMatch(toolUsed,plugin.cfg.comboKey()))
 					)
 					|| event.getAction().equals(Action.PHYSICAL)
 			){
 				if (plugin.chests.isLocked(block)){
 				    SCLItem lockedItem = plugin.chests.getItem(block);
 					String owner = lockedItem.getOwner();
 					SCL.verbose(player.getName() + " wants to use " + owner + "'s " + typeName);
 					boolean ignoreOwner = SCL.permit(player, "simplechestlock.ignoreowner");
 					boolean comboLocked = lockedItem.isComboLocked();
 					if (comboLocked){
 						SCL.verbose("This block is locked with a combination lock!");
 					}
 					else {
 						SCL.verbose("This block is locked with a normal key");
 					}
 					
 					if ( comboLocked && ! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
 						Inventory inv = player.getInventory();
 						if (
								inv.getItem(0).getType().equals(Material.WOOL)
								&& inv.getItem(1).getType().equals(Material.WOOL)
								&& inv.getItem(2).getType().equals(Material.WOOL)
 						){
 							DyeColor tumbler1 = DyeColor.getByData(inv.getItem(0).getData().getData());
 							DyeColor tumbler2 = DyeColor.getByData(inv.getItem(1).getData().getData());
 							DyeColor tumbler3 = DyeColor.getByData(inv.getItem(2).getData().getData());
 							DyeColor[] combo = {tumbler1,tumbler2,tumbler3};
 							if (!lockedItem.correctCombo(combo)){
 								SCL.verbose(player.getName() + " provided the wrong combo for " + owner + "'s " + typeName);
 								plugin.messaging.throttledMessage(player,ChatColor.RED+owner+"'s "+typeName+" has a different combination...");
 								event.setCancelled(true);
 							}
 						}
 						else {
 							SCL.verbose(player.getName() + " provided no combo for " + owner + "'s " + typeName);
 							plugin.messaging.throttledMessage(player,ChatColor.RED+owner+"'s "+typeName+" is locked with a combination lock.");
 							event.setCancelled(true);
 						}
 					}
 					else if (! owner.equalsIgnoreCase(player.getName()) && lockedItem.trusts(player)){
 					    player.sendMessage(ChatColor.GREEN+owner+" trusts you with access to this "+typeName);
 					}
 					else if (! owner.equalsIgnoreCase(player.getName()) && ! ignoreOwner){
 						event.setCancelled(true);
 						plugin.messaging.throttledMessage(player,ChatColor.RED+"Access denied to "+owner+"'s "+typeName);
 					}
 					else if (! owner.equalsIgnoreCase(player.getName()) && ignoreOwner){
 						SCL.verbose(player.getName() + " was let into " + owner + "'s " + typeName + ", ignoring owner.");
 						if (plugin.cfg.openMessage()){
 						    player.sendMessage(ChatColor.GREEN+"Access granted to "+owner+"'s "+typeName);
 						}
 					}
 					else {
 						SCL.verbose(player.getName() + " was let into the " + typeName);
 						if (plugin.cfg.openMessage()){
 							if (comboLocked){
 								String comboString = plugin.chests.getComboString(block);
 								player.sendMessage(ChatColor.GREEN+"Lock combination is "+comboString);
 							}
 							else {
 								player.sendMessage(ChatColor.GREEN+"Access granted to "+typeName);
 							}
 							String trustedNames = lockedItem.trustedNames();
 							if (trustedNames != null && trustedNames.length() > 0){
 							    player.sendMessage(ChatColor.GREEN+"Trusted for access: "+trustedNames);
 							}
 						}
 					}
 				}
 				else {
 				    SCL.verbose("Access granted to unlocked " + typeName);
 				}
 			}
 			else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
 			    
 				ItemStack inHand = event.getItem();
 				if (inHand == null) return;
 				ItemStack tool = inHand.clone();
 				tool.setAmount(1);
 				
 				if (plugin.toolMatch(tool,plugin.cfg.key()) || plugin.toolMatch(tool,plugin.cfg.comboKey())){
 					event.setCancelled(true);
 					if (SCL.permit(player,"simplechestlock.lock")){
 						if (plugin.chests.isLocked(block)){
 							String owner = plugin.chests.getOwner(block);
 							if (owner.equalsIgnoreCase(player.getName())){
 								Integer unlockedChests = plugin.chests.unlock(block);
 								if (unlockedChests == 1){
 									player.sendMessage(ChatColor.GREEN+ucfirst(typeName)+" unlocked");
 								}
 								else if (unlockedChests > 1){
 									player.sendMessage(ChatColor.GREEN+unlockedChests.toString()+" "+typeName+"s unlocked");
 								}
 								else {
 									player.sendMessage(ChatColor.RED+"Error while unlocking your "+typeName);
 								}
 							}
 							else if (SCL.permit(player, "simplechestlock.ignoreowner")){
 								Integer unlockedChests = plugin.chests.unlock(block);
 								Player ownerObject = Bukkit.getServer().getPlayer(owner);
 								if (unlockedChests == 1){
 									if (ownerObject != null){
 										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+typeName+", and taddle-taled on you for it.");
 										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked your "+typeName+" using mystic powers!");
 									}
 									else {
 										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+typeName+", but that user is offline, and was not notified.");
 									}
 								}
 								else if (unlockedChests > 1){
 									if (ownerObject != null){
 										player.sendMessage(ChatColor.YELLOW+"Unlocked "+owner+"'s "+unlockedChests.toString()+" "+typeName+"s, and taddle-taled on you for it.");
 										ownerObject.sendMessage(ChatColor.YELLOW+player.getName()+" unlocked "+unlockedChests.toString()+" of your "+typeName+"s using mystic powers!");
 									}
 								}
 								else {
 									player.sendMessage(ChatColor.RED+"Error while unlocking "+owner+"'s "+typeName);
 								}
 								
 							}
 							else {
 								player.sendMessage(ChatColor.RED+"Locked by "+owner+":  You can't use it!");
 							}
 							
 						}
 						else {
 							if (
 								!(plugin.cfg.usePermissionsWhitelist())
 								|| ( 
 										plugin.cfg.usePermissionsWhitelist()
 										// Just checking for the indevidual block now, as the parent .* permission will grant them all.
 										&& SCL.permit(player,"simplechestlock.locktype."+block.getType().toString().toLowerCase())
 									)
 							){
 								boolean lockForSomeone = false;
 								String locksFor = player.getName();
 								if (plugin.locksAs.containsKey(player.getName())){
 									locksFor = plugin.locksAs.get(player.getName());
 									lockForSomeone = true;
 								}
 								if (plugin.toolMatch(tool,plugin.cfg.comboKey())){
 									if (SCL.permit(player, "simplechestlock.usecombo")){
 										Inventory inv = player.getInventory();
 										if (
												inv.getItem(0).getType().equals(Material.WOOL)
												&& inv.getItem(1).getType().equals(Material.WOOL)
												&& inv.getItem(2).getType().equals(Material.WOOL)
 										){
 											DyeColor tumbler1 = DyeColor.getByData(inv.getItem(0).getData().getData());
 											DyeColor tumbler2 = DyeColor.getByData(inv.getItem(1).getData().getData());
 											DyeColor tumbler3 = DyeColor.getByData(inv.getItem(2).getData().getData());
 											DyeColor[] combo = {tumbler1,tumbler2,tumbler3};
 											String comboString = tumbler1.toString()+","+tumbler2.toString()+","+tumbler3.toString();
 											Integer itemsLocked = plugin.chests.lock(player,block,combo);
                                             if (itemsLocked >= 1){
 												if (lockForSomeone){
 													player.sendMessage(ChatColor.GREEN+itemsLocked.toString()+" "+typeName+SCL.plural(itemsLocked)+" locked for "+locksFor+"!  Combo is "+comboString);
 												}
 												else {
													player.sendMessage(ChatColor.GREEN+itemsLocked.toString()+" "+typeName+SCL.plural(itemsLocked)+"s locked!  Combo is "+comboString);
 												}
                                                 if (plugin.cfg.consumeKey() && !SCL.permit(player,"simplechestlock.forfree")){
                                                     if (inHand.getAmount() > 1){
                                                         inHand.setAmount(inHand.getAmount()-1);
                                                     }
                                                     else if (inHand.getAmount() == 1){
                                                         player.setItemInHand(new ItemStack(Material.AIR));
                                                     }
                                                     else {
                                                         SCL.crap(player.getName()+" is locking stuff without being charged for it!");
                                                     }
                                                 }
 											}
 											else if (itemsLocked < 0){
 											    player.sendMessage(ChatColor.RED+"Something horrible happened while trying to lock!");
 											}
 											
 										}
 										else {
 											player.sendMessage(ChatColor.RED+"First three hotbar slots must be wool for the combo!");
 										}
 									}
 									else {
 										player.sendMessage(ChatColor.RED+"Sorry, permission denied for combination locking!");
 									}
 								}
 								else {
 									Integer itemsLocked = plugin.chests.lock(player, block);
 									String trustReminder = plugin.trustHandler.trustList(locksFor);
 									if (itemsLocked >= 1){
 										if (lockForSomeone){
 											player.sendMessage(ChatColor.GREEN+itemsLocked.toString()+" "+typeName+SCL.plural(itemsLocked)+" locked for "+locksFor+"!");
 											if (trustReminder != null){
                                                 player.sendMessage(ChatColor.GREEN+trustReminder);
                                             }
 										}
 										else {
 											player.sendMessage(ChatColor.GREEN+itemsLocked.toString()+" "+typeName+SCL.plural(itemsLocked)+" locked!");
 											if (trustReminder != null){
                                                 player.sendMessage(ChatColor.GREEN+trustReminder);
                                             }
 										}
                                         if (plugin.cfg.consumeKey() && !SCL.permit(player,"simplechestlock.forfree")){
                                             if (inHand.getAmount() > 1){
                                                 inHand.setAmount(inHand.getAmount()-1);
                                             }
                                             else if (inHand.getAmount() == 1){
                                                 player.setItemInHand(new ItemStack(Material.AIR));
                                             }
                                             else {
                                                 SCL.crap(player.getName()+" is locking stuff without being charged for it!");
                                             }
                                         }
 									}
 									else if (itemsLocked < 0){
 									    player.sendMessage(ChatColor.RED+"Strange and horrible error encountered while locking!");
 									}
 								}
 							}
 							else if (plugin.cfg.usePermissionsWhitelist() && plugin.cfg.whitelistMessage()){
 								player.sendMessage(ChatColor.RED+"Sorry, you are not allowed to lock "+block.getType().toString());
 							}
 						}
 					}
 					else {
 						player.sendMessage(ChatColor.RED+"You can't lock or unlock blocks!  Permission denied!");
 					}
 				}
 			}
 		}
 	}
 }
