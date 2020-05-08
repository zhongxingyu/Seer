 package net.endercraftbuild.ac.listeners;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleFlightEvent;
 import org.bukkit.util.Vector;
 
 
 public class DoubleJump implements Listener {
 
 	private ACMain plugin;
 	private List<String> justJumped = new ArrayList<String>();
 	
 	public DoubleJump(ACMain instance) {
 		Bukkit.getServer().getPluginManager().registerEvents(this, instance);
 		this.plugin = instance;
 	}
 	
 	
 	@EventHandler
 	public void join(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 
 			player.setAllowFlight(true);
 			justJumped.remove(player.getName());
 		}
 	
 	
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		
 
 			player.setAllowFlight(true);
 			justJumped.remove(player.getName());
 		}
 	
 	
 	@EventHandler
 	public void setFlyOnJump(PlayerToggleFlightEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName();
 		World world = player.getWorld();
 		
 		boolean messageOnJump = plugin.getConfig().getBoolean("Message On Jump");
 		boolean sound = plugin.getConfig().getBoolean("Sound");
 		boolean effect = plugin.getConfig().getBoolean("Effect On Jump");
 		boolean wallJump = plugin.getConfig().getBoolean("Wall Jump");
 		boolean forwardOnJump = plugin.getConfig().getBoolean("Jump Forward");
 		int blocks = plugin.getConfig().getInt("Jump Height");
 		String message = plugin.getConfig().getString("Message");
 		Integer playerexp = player.getTotalExperience();
 		
 		
 		Vector jump = player.getVelocity().multiply(1).setY(0.17 * blocks);
 		Vector look = player.getLocation().getDirection().multiply(0.5);
 		
 		if(event.isFlying() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
 			
 				if(!wallJump) {
 					if(!justJumped.contains(name)) {
 						player.setFlying(false);
 						
 						if(forwardOnJump) {
 							player.setVelocity(jump.add(look));
 						} else {
 							player.setVelocity(jump);
 						}
 						
 						player.setAllowFlight(false);
 						
 						if(messageOnJump) {
 							player.sendMessage(plugin.prefix + ChatColor.RED + "**WOOSH**");
 							Utils.setEnergy(player, playerexp -= 4);
 						}
 						
 						if(sound) {
 							player.playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 10, -10);
 						}
 						
 						if(effect) {
 							for(int i = 0; i <= 10; i++) {
 								world.playEffect(player.getLocation(), Effect.SMOKE, i);
 							}
 						}
 						
 					} else {
 						player.setFlying(false);
 						player.setAllowFlight(false);
 						
 					}
 					
 					event.setCancelled(true);
 				} else {
 					Block block = player.getTargetBlock(null, 2);
 					
 					if(block.getType() != Material.AIR) {
 						if(!justJumped.contains(name)) {
 							player.setFlying(false);
 							
 							if(forwardOnJump) {
 								player.setVelocity(jump.add(look));
 							} else {
 								player.setVelocity(jump);
 							}
 							
 							player.setAllowFlight(false);
 							
 							if(messageOnJump) {
 								player.sendMessage(ChatColor.GREEN + message);
 							}
 							
 							if(sound) {
 								player.playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 10, -10);
 							}
 							
 							if(effect) {
 								for(int i = 0; i <= 10; i++) {
 									world.playEffect(player.getLocation(), Effect.SMOKE, i);
 								}
 							}
 							
 						} else {
 							player.setFlying(false);
 							player.setAllowFlight(false);
 							
 						}
 					} else {
 						player.setFlying(false);
 						player.setAllowFlight(false);
 					}
 					event.setCancelled(true);
 				}
 			}
 	    }
 	
 	
 	@EventHandler
 	public void onMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Location loc = player.getLocation();
 		Block block = loc.add(0, -1, 0).getBlock();
 		
 			if(block.getType() == Material.AIR) {
 				if(!justJumped.contains(player.getName())) {
 					justJumped.add(player.getName());
 				}
 			} else {
 				if(justJumped.contains(player.getName())) {
 					justJumped.remove(player.getName());
 					player.setAllowFlight(true);
 					player.setFlying(false);
 				}
 			
 		 else {
 			justJumped.remove(player.getName());
 			player.setAllowFlight(false);
 			player.setFlying(false);
 		}
 		
 	}
 }
 }
