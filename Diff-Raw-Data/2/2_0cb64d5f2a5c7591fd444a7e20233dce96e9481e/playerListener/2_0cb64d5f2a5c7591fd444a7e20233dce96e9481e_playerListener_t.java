 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Chunk;
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
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.zolli.rodolffoutilsreloaded.DelaydMessage;
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 import com.zolli.rodolffoutilsreloaded.utils.configUtils;
 import com.zolli.rodolffoutilsreloaded.utils.webUtils;
 
 public class playerListener implements Listener {
 	
 	private rodolffoUtilsReloaded plugin;
 	public configUtils cu;
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
 				e.setCancelled(true);
 				
 			}
 			
 		}
 		
 		if(entity instanceof Spider) {
 			
 			Spider entitySpider = (Spider) entity;
 			
 			if(plugin.perm.has(player, "rur.rideSpider") && (entitySpider.getPassenger() == null)) {
 				entitySpider.setPassenger(player);
 			} else if(plugin.perm.has(player, "rur.rideSpider") && (entitySpider.getPassenger() == e.getPlayer())) {
 				entitySpider.eject();
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
 			
 		if(e.getClickedBlock() != null && e.getAction() != null && e.getClickedBlock().getTypeId() == 77 && (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
 			
 			Location buttonLoc = e.getClickedBlock().getLocation();
 			String[] scanResult = cu.scanButton(buttonLoc);
 			
 			
 			if(pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use")) {
 				
 				if(scanResult != null) {
 					
 					if(scanResult[0].equalsIgnoreCase("weathersun") && (pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use.weathersun"))) {
 						
 						World currentWorld = pl.getLocation().getWorld();
 						
 						if(plugin.econ.hasEnought(pl, Integer.parseInt(scanResult[1]))) {
 							
 							plugin.econ.withdraw(pl, Integer.parseInt(scanResult[1]));
 							currentWorld.setStorm(false);
 							pl.sendMessage(plugin.messages.getString("othercommand.sunny"));
 							
 							List<Player> players = currentWorld.getPlayers();
 							
 							for(Player p : players) {
 								
 								if(p.getName() != pl.getName()) {
 									
 									p.sendMessage(pl.getName() + plugin.messages.getString("othercommand.sunnybroadcast"));
 									
 								}
 								
 							}
 							
 						} else {
 							
							pl.sendMessage(plugin.messages.getString("common.noMoney"));
 							
 						}
 						
 					}
 					
 					
 					
 					if(scanResult[0].equalsIgnoreCase("promote") && (pl.isOp() || plugin.perm.has(pl, "rur.specialButton.use.promote"))) {
 						
 						String introductionStatus = webUtils.hasIntroduction(pl);
 						
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
 											pl.getInventory().setItemInHand(new ItemStack(38, 1));
 											
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
 			
 			String multiUsers = webUtils.multiUsers(e.getPlayer());
 				
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
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	  public void getSlimeChunk(PlayerInteractEvent e) {
 	    
 	    if((e.getClickedBlock() != null)) {
 	    
 	      Player pl = e.getPlayer();
 	      ItemStack handItem = e.getPlayer().getItemInHand();
 	      int handAmount = handItem.getAmount();
 	      int newHandAmount = handAmount - 1;
 	      long seedCode = pl.getLocation().getWorld().getSeed();
 	      Chunk playerOnChunk = pl.getWorld().getChunkAt(e.getClickedBlock());
 	      int chunkX = playerOnChunk.getX();
 	      int chunkZ = playerOnChunk.getZ();
 	      
 	      if((e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (handItem.getTypeId() == plugin.config.getInt("slimeChunkItem")) && (e.getClickedBlock() != null && e.getAction() != null)) {
 	        
 	        if(handAmount == 1) {
 	          pl.setItemInHand(null);
 	        } else {
 	          handItem.setAmount(newHandAmount);
 	        }
 	        
 	        Random rand = new Random(seedCode + chunkX * chunkX * 4987142 + chunkX * 5947611 + 
 	                    chunkZ * chunkZ * 4392871L + chunkZ * 389711 ^ 0x3AD8025F);
 	        
 	        if(rand.nextInt(10) == 0) {
 	          
 	          pl.sendMessage(plugin.messages.getString("slimeChunk.isSlimeChunk"));
 	          
 	        } else {
 	          
 	          pl.sendMessage(plugin.messages.getString("slimeChunk.isNotSlimeChunk"));
 	          
 	        }
 	        
 	      }
 	    
 	    }
 	    
 	  }
 	
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void whoIs(PlayerCommandPreprocessEvent e) {
 		if(e.getMessage().startsWith("/whois ")) {
 			String[] args = e.getMessage().split(" ");
 			Player p = e.getPlayer();
 			if(p.isOp() || plugin.perm.has(p, "essentials.whois")) {
 				List<Player> pl = plugin.getServer().matchPlayer(args[1]);
 				if(pl.size()>0 && pl.get(0).getName().equalsIgnoreCase(args[1])) {
 					String multiUsers = webUtils.multiUsers(pl.get(0));
 					if(multiUsers.equalsIgnoreCase("null")) multiUsers = "senki";
 					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new DelaydMessage(p, plugin.messages.getString("common.multiUsersWhois")+multiUsers),2L);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void playerListNameForJoin(PlayerJoinEvent e) {
 		setPlayerListName(e.getPlayer());
 	}
 
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void playerListNameForChat(AsyncPlayerChatEvent e) {
 		setPlayerListName(e.getPlayer());
 	}
 
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void playerListNameForCommand(PlayerCommandPreprocessEvent e) {
 		setPlayerListName(e.getPlayer());
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void playerListName(PlayerChangedWorldEvent e) {
 		setPlayerListName(e.getPlayer());
 	}
 	
 	private void setPlayerListName(Player p) {
 		if(!p.getPlayerListName().equalsIgnoreCase(p.getName())) return;
 		try
 		{
 			String prefix = PermissionsEx.getUser(p).getPrefix();
 			String name = prefix.split("] ",2)[1];
 			name += p.getName();
 			if(name.length()>16) name = name.substring(0, 15);
 			p.setPlayerListName(name.replace("&", "§"));
 		}
 		catch (Exception ex) 
 		{
 			plugin.log.warning("[" + plugin.pdfile.getName() + "] No PermissionsEx found, no use prefix");
 		}
 	}
 	
 }
