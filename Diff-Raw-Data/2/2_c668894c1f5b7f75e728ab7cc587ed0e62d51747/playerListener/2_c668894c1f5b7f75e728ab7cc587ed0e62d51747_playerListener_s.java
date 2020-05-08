 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Spider;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 import com.zolli.rodolffoutilsreloaded.utils.configUtils;
 import com.zolli.rodolffoutilsreloaded.utils.webUtils;
 
 public class playerListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	public configUtils cu;
 	private webUtils wu = new webUtils();
 	private Player pl;
 	public playerListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 		cu = new configUtils(instance);
 	}
 	
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void overrideBukkitDefaults(PlayerCommandPreprocessEvent e) {
 		
 		String command = e.getMessage();
 		Player commandSender = e.getPlayer();
 		
 		if((!commandSender.isOp() || !plugin.perm.has(commandSender, "rur.allowSeeBukkitVer")) && (command.equalsIgnoreCase("/ver") || command.equalsIgnoreCase("/version"))) {
 			commandSender.sendMessage(plugin.config.getString("fakeBukkitVerString"));
 			e.setCancelled(true);
 		}
 		
 		if((!commandSender.isOp() || !plugin.perm.has(commandSender, "rur.allowSeeRealPlugins")) && (command.equalsIgnoreCase("/pl") || command.equalsIgnoreCase("/plugins"))) {
 			commandSender.sendMessage(plugin.config.getString("fakePluginsList"));
 			e.setCancelled(true);
 		}
 		
 		if(command.equalsIgnoreCase("/reload")) {
 			commandSender.sendMessage(plugin.messages.getString("common.noreload"));
 			e.setCancelled(true);
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void giveBackSaddle(PlayerInteractEntityEvent e) {
 		
 		Entity entity = e.getRightClicked();
 		Player player = e.getPlayer();
 		
 		if(entity instanceof Pig) {
 			
 			Pig entityPig = (Pig) entity;
 			
 			if(entityPig.hasSaddle() && entityPig.getPassenger() == null && plugin.perm.has(player, "rur.getBackSaddle")) {
 				
 				entityPig.setSaddle(false);
 				entityPig.getWorld().dropItem(entityPig.getLocation(), new ItemStack(Material.SADDLE, 1));
 				
 			}
 			
 		}
 		
 		if(entity instanceof Spider) {
 			
 			Spider entitySpider = (Spider) entity;
 			
 			if(plugin.perm.has(player, "rur.rideSpider") && (entitySpider.getPassenger() == null)) {
 				entitySpider.setPassenger(player);
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void giveBackSaddleOnDeath(EntityDeathEvent e) {
 		
 		Entity entity = e.getEntity();
 		
 		if(e.getEntity() instanceof Pig) {
 			
 			Pig entityPig = (Pig) entity;
 			
 			if(entityPig.getKiller()!=null && plugin.perm.has(entityPig.getKiller(), "rur.getBackSaddle") && plugin.config.getBoolean("pigDropSaddleOnDeath")) {
 				
 				e.getDrops().add(new ItemStack(Material.SADDLE, 1));
 				
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void buttonPress(PlayerInteractEvent e) {
 		
 		pl = e.getPlayer();
 			
		if(e.getClickedBlock() != null && e.getAction() != null && e.getClickedBlock().getTypeId() == 77 && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			
 			Location buttonLoc = e.getClickedBlock().getLocation();
 			String[] scanResult = cu.scanButton(buttonLoc);
 			
 			
 			if(pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use")) {
 				
 				if(scanResult != null) {
 					
 					if(scanResult[0].equalsIgnoreCase("weathersun") && (pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use.weathersun"))) {
 						
 						World currentWorld = pl.getLocation().getWorld();
 						
 						if(plugin.econ.getBalance(pl.getName()) >= Integer.parseInt(scanResult[1])) {
 							
 							plugin.econ.withdrawPlayer(pl.getName(), Integer.parseInt(scanResult[1]));
 							currentWorld.setStorm(false);
 							pl.sendMessage(plugin.messages.getString("othercommand.sunny"));
 							
 							List<Player> players = currentWorld.getPlayers();
 							
 							for(Player p : players) {
 								
 								if(p.getName() != pl.getName()) {
 									
 									p.sendMessage(pl.getName() + plugin.messages.getString("othercommand.sunnybroadcast"));
 									
 								}
 								
 							}
 							
 						} else {
 							
 							pl.sendMessage(plugin.messages.getString("common.noPerm"));
 							
 						}
 						
 					}
 					
 					
 					
 					if(scanResult[0].equalsIgnoreCase("promote") && (pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use.promote"))) {
 						
 						String introductionStatus = wu.hasIntroduction(pl);
 						
 						if(introductionStatus.equalsIgnoreCase("ok")) {
 							
 							if(plugin.perm.getPrimaryGroup(pl).equalsIgnoreCase("ujonc")) {
 								
 								plugin.perm.playerRemoveGroup(pl, "ujonc");
 								plugin.perm.playerAddGroup(pl, "Tag");
 								
 								pl.sendMessage(plugin.messages.getString("promotion.successpromotion1"));
 								
 								if(plugin.config.getBoolean("promotedtospawn")) {	
 									
 									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 										
 										public void run() {
 											
 											playerListener.this.pl.performCommand("spawn");
 											pl.sendMessage(plugin.messages.getString("promotion.successpromotion2"));
 											
 										}
 										
 									}, plugin.config.getLong("spawncooldown")*10L);
 									
 								}
 								
 							} else {
 								
 								pl.sendMessage(plugin.messages.getString("promotion.alredymember"));
 								pl.performCommand("spawn");
 								
 							}
 							
 						} else {
 							
 							pl.sendMessage(plugin.messages.getString("promotion.nointroduction1"));
 							pl.sendMessage(plugin.messages.getString("promotion.nointroduction2"));
 							
 						}
 						
 					}
 					
 					
 					if(scanResult[0].equalsIgnoreCase("spawn") && (pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use.spawn"))) {
 
 						playerListener.this.pl.performCommand("spawn");
 						
 					}
 					
 				}
 				
 			}
 			
 			if(plugin.SelectorPlayer != null) {
 				
 				cu.setLocation(buttonLoc, plugin.selectType, plugin.selectName);
 				plugin.SelectorPlayer = null;
 				plugin.saveConfiguration();
 				e.getPlayer().sendMessage(plugin.messages.getString("definebutton.successadded"));
 			
 			}
 				
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void command(PlayerCommandPreprocessEvent e) {
 		
 		if(e.getMessage().startsWith("/home")) {
 			
 			String[] part = e.getMessage().split(" ", 3);
 			
 			if (part.length == 2) {
 				
 				if (part[1].equalsIgnoreCase("bed") && !((plugin.perm.has(e.getPlayer(), "rur.bedhome")) || (e.getPlayer().isOp()))) {
 					
 					e.setCancelled(true);
 					e.getPlayer().sendMessage(plugin.messages.getString("common.noPerm"));
 					
 				} 
 				
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void getPlayers(PlayerJoinEvent e) {
 		
 		if(plugin.perm.getPrimaryGroup(e.getPlayer()).equalsIgnoreCase("ujonc")) {
 			
 			String multiUsers = wu.multiUsers(e.getPlayer());
 				
 			Player[] Players = plugin.getServer().getOnlinePlayers();
 				
 			for(Player p : Players) {
 					
 				if((plugin.perm.has(p, "rur.seemultiaccount")) || p.isOp()) {
 						
 					if(multiUsers.equalsIgnoreCase("null")) {
 						
 						p.sendMessage(plugin.messages.getString("common.nomultiple").replace("%n", e.getPlayer().getName()));
 						
 					} else {
 						
 						p.sendMessage(plugin.messages.getString("common.multiusers").replace("%n", e.getPlayer().getName()) + multiUsers);
 						
 					}
 						
 				}
 					
 			}	
 			
 		}
 		
 	}
 	
 }
