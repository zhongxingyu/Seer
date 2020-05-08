 package ch.nonameweb.bukkit.plugins.minepvp.listener;
 
 import net.sacredlabyrinth.phaed.simpleclans.Helper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import ch.nonameweb.bukkit.plugins.minepvp.Clan;
 import ch.nonameweb.bukkit.plugins.minepvp.MinePvP;
 import ch.nonameweb.bukkit.plugins.minepvp.manager.ClanManager;
 
 public class MinePvPPlayerListener extends PlayerListener{
 
 	private MinePvP plugin;
 	private ClanManager clanManager;
 	
 	/**
 	 * 
 	 */
 	public MinePvPPlayerListener() {
 		plugin = MinePvP.getInstance();
 		clanManager = plugin.getClanManager();
 	}
 	
 	/**
 	 * 
 	 */
 	public void onPlayerCommandPreprocess( PlayerCommandPreprocessEvent event ) {
 		
 		if ( event.isCancelled() ) {
 			return;
 		}
 		
 		Player player = event.getPlayer();
 		
 		if ( player == null ) {
 			return;
 		}
 		
 		String[] split = event.getMessage().substring(1).split(" ");
 		
 		if ( split.length == 0 ) {
 			return;
 		}
 		
 		String command = split[0];
 		
 		if ( command.equalsIgnoreCase("minepvp") ) {			
 			plugin.getCommandManager().processMinePvP(player, Helper.removeFirst(split));
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public void onPlayerMove ( PlayerMoveEvent event ) {
 		
 		if ( event.isCancelled() ) {
 			return;
 		}
 		
 		Player player = event.getPlayer();
 		
 		if ( player == null ) {
 			return;
 		}
 		
 		Clan playerClan = clanManager.getClanByPlayer(player);
 		Clan clanFrom = clanManager.getClanByLocation( event.getFrom() );
 		Clan clanTo = clanManager.getClanByLocation( event.getTo() );
 		
 		if ( clanFrom == null ) {
 			
 			if ( clanTo != null ) {
 				
 				// Ist mindestens Spieler Online vom Clan
 				if ( clanManager.isMinPlayerOnline(clanTo) ) {
 					// AlarmSystem
 					if ( clanTo.getName().equalsIgnoreCase( playerClan.getName() ) != true ) {
 						
 						if ( clanTo.getAlertsystem() == 1 ) {
 							
 							clanManager.sendClanMessage(clanTo, ChatColor.RED + "Ein feindlicher Spieler betritt euer Gebiet!");
 							
 						} else if ( clanTo.getAlertsystem() == 2 ) {
 							
 							clanManager.sendClanMessage(clanTo, ChatColor.RED + "Ein feindlicher Spieler vom Clan " + playerClan.getName() + " betritt euer Gebiet!");
 							
 						} else if ( clanTo.getAlertsystem() == 3 ) {
 							
 							clanManager.sendClanMessage(clanTo, ChatColor.RED + "Ein feindlicher Spieler " + player.getName() + " vom Clan " + playerClan.getName() + " betritt euer Gebiet!");
 							
 						}
 						
 					}
 					
 					player.sendMessage("Du betrittst das gebiebt von " + clanTo.getName() + "!" );
 				} else {
 					player.teleport( event.getFrom() );
 					player.sendMessage("Es ist kein Spieler aus dem Clan " + clanTo.getName() + " Online");
 				}
 					
 			}
 			
 		} else {
 			
 			if ( clanTo == null ) {
 				player.sendMessage("Du verlsst das gebiet von " + clanFrom.getName() + "!");
 			} else {
 				
 				// Wenn in Wasser von einem feindlichen Clan kriegt er schaden // moat / wassergraben
 				if ( player.getLocation().getBlock().isLiquid() ) {
 					
 					// Wenn der Spieler sich in einem Feindlichen Clangebiet bewegt
 					if ( clanFrom.getName().equalsIgnoreCase( playerClan.getName() ) != true && clanTo.getName().equalsIgnoreCase( playerClan.getName() ) != true  ) {
 						
 						// Wenn der Clan einen Wassergraben hat
 						if ( clanFrom.getMoat() == true ) {
 							clanManager.playerMoatDamage(player);
 						}
 						
 					}
 					
 				}
 				
 				
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * 
 	 */
 	public void onPlayerQuit ( PlayerQuitEvent event ) {
 		
 		Player player = event.getPlayer();
 		
 		if ( player == null ) {
 			return;
 		}
 		
 		// Wenn jemand noch eine Flagge hat wird diese Resettet
 		if ( player.getInventory().getHelmet().getTypeId() == 35  ) {
 			plugin.getClanManager().resetFlag(player);
 		}
 		
 	}
 	
 	public void onPlayerKick( PlayerKickEvent event) {
 		
 		Player player = event.getPlayer();
 		
 		if ( player == null ) {
 			return;
 		}
 		
 		// Wenn jemand noch eine Flagge hat wird diese Resettet
 		if ( player.getInventory().getHelmet().getTypeId() == 35  ) {
 			plugin.getClanManager().resetFlag(player);
 		}
 		
 		
 	}
 	
 	/**
 	 * 
 	 */
 	public void onPlayerTeleport ( PlayerTeleportEvent event ) {
 		
 		Player player = event.getPlayer();
 		
 		if ( player == null ) {
 			return;
 		}
 		
 		// Wenn jemand noch eine Flagge hat wird diese Resettet
 		if ( player.getInventory().getHelmet().getTypeId() == 35  ) {
 			plugin.getClanManager().resetFlag(player);
 		}
 		
 	}
 	
 	public void onPlayerBucketEmpty (PlayerBucketEmptyEvent event) {
 		
 		
 		Player player = event.getPlayer();
 		Material bucket = event.getBucket();
 		
 		Clan clanLand = clanManager.getClanByLocation( event.getBlockClicked().getLocation() );
 		
 		// Ist es in einen gebit von einem Clan?
 		if ( clanLand != null ) {
 		
 			// Hat der Spieler einen Clan?
 			if ( clanManager.hasPlayerAClan(player) ) {
 				
 				// Ist er im gleichen Clan wie das Land?
 				if ( clanLand.getName().equalsIgnoreCase( clanManager.getClanNameByPlayer(player) ) ) {
 					
 					
 					
 				} else {
 					
 					// Wasser / Lava unterbinden
 					if ( bucket.getId() == 326 || bucket.getId() == 327 ) {
 						event.setCancelled(true);
 					}					
 					
 					clanManager.playerDamage(player);
 				}
 				
 			} else {
 				clanManager.playerDamage(player);			
 			}
 			
 		} else {
 			
 		}
 		
 		
 		
 	}
 	
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		
 		try {
 			
 			Clan clanLand = clanManager.getClanByLocation( event.getClickedBlock().getLocation() );
 			
 			// Ist es in einen gebit von einem Clan?
 			if ( clanLand != null ) {
 			
 				// Hat der Spieler einen Clan?
 				if ( clanManager.hasPlayerAClan(player) ) {
 					
 					// Ist er im gleichen Clan wie das Land?
 					if ( clanLand.getName().equalsIgnoreCase( clanManager.getClanNameByPlayer(player) ) ) {
 						
 					} else {
 						
 						// Chests and Furnances
 						if ( block.getTypeId() == 54 || block.getTypeId() == 61 || block.getTypeId() == 62 ) {
 							clanManager.playerDamage(player);
 							event.setCancelled(true);
 						}					
 						
 					}
 					
 				} else {
 					clanManager.playerDamage(player);			
 				}
 				
 			} else {
 				
 			}
 			
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 		
 		
 	}
 	
 	
 }
