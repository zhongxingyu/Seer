 package net.D3GN.MiracleM4n.SetSpeed;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.util.Vector;
 
 public class SSPlayerListener extends PlayerListener {
     
 	private final SetSpeed plugin;
 	
     public SSPlayerListener(SetSpeed callbackPlugin) {
         plugin = callbackPlugin;
     }
     
 	public void onPlayerMove(PlayerMoveEvent event) {
     	Player player = event.getPlayer();
     	Double players = plugin.players.get(player);
     	if (plugin.isSpeedOn.get(player) == null) {
 			plugin.isSpeedOn.put(player, false);
 		}
     	if (player.isSneaking()) {
     		if (plugin.sneakAble == true) {
         		if (plugin.players.get(player) != null) {
         			if ((players) != 1) {
         				int material = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getTypeId();
         				if (material != 0 && material != 8 && material != 9 && material != 50 && material != 65) {
         					if (plugin.defaSpeed) {
         						Vector dir = player.getLocation().getDirection().multiply(((plugin.defSpeed)*(0.3))/2).setY(0.1);
         						player.setVelocity(dir);
         					} else {
         						Vector dir = player.getLocation().getDirection().multiply(((plugin.players.get(player))*(0.3))/2).setY(0.1);
         						player.setVelocity(dir);
         					}
         				}
         			}
         		} if (plugin.players.get(player) != null) {
         			return;
         		} else {
         			return;
         		}
     		} else {
     			return;
     		}
     	} else if (!(player.isSneaking())) {
     		if (plugin.isSpeedOn.get(player) == true) {
     			if (plugin.players.get(player) != null) {
     				if ((players) != 1) {
     					int material = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getTypeId();
     					if (material != 0 && material != 8 && material != 9 && material != 50 && material != 65) {
         					if (plugin.defaSpeed) {
         						Vector dir = player.getLocation().getDirection().multiply(((plugin.defSpeed)*(0.3))/2).setY(0.1);
         						player.setVelocity(dir);
         					} else {
         						Vector dir = player.getLocation().getDirection().multiply(((plugin.players.get(player))*(0.3))/2).setY(0.1);
         						player.setVelocity(dir);
         					}
         				}
         			}
         		} else  {
         			return;
         		}
     		} else {
     			return;
     		}
     	}
     	if ((players) != 1) {
     		if ((player.getInventory().getBoots().getTypeId() == (plugin.bootItem)) ||
     				(player.getInventory().getLeggings().getTypeId() == (plugin.legItem)) ||
     				(player.getInventory().getChestplate().getTypeId() == (plugin.chestItem)) ||
     				(player.getInventory().getHelmet().getTypeId() == (plugin.helmItem))) {
     			if (plugin.isSpeedOn.get(player) == false) {
     				player.performCommand("speedon");
     				plugin.isSpeedOn.put(player, true);
     			} else {
     				return;
     			} 
     		}
     	} else {
     		return;
     	}
     }
     
 	public void onPlayerInteract(PlayerInteractEvent event) {
     	Player player = event.getPlayer();
     	Action action = event.getAction();
     	Double players = plugin.players.get(player);
     	
    	if ((players) == null) {
    		plugin.players.put(player, 1.0);
    	}
     	if ((players) != 1) {
     		if (plugin.isSpeedOn.get(player) == true) {
             	if (((action == Action.LEFT_CLICK_AIR) || 
             			(action == Action.LEFT_CLICK_BLOCK)) && 
             			(player.getItemInHand().getTypeId() == (plugin.speedItem))) {
             		player.performCommand("speedoff");
             	}
     		} else if (plugin.isSpeedOn.get(player) == false) { 
         		if (((action == Action.LEFT_CLICK_AIR) || 
         			(action == Action.LEFT_CLICK_BLOCK)) && 
         			(player.getItemInHand().getTypeId() == (plugin.speedItem))) {
         		player.performCommand("speedon");
         		}
         	}
         } else {
         	return;
     	}
     }
 	
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		Double players = plugin.players.get(player);
 		
 		if ((players) != 1) {
 			player.performCommand("speedoff");
 		}
 	}
 	
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		Double players = plugin.players.get(player);
 		
 		if ((players) != 1) {
 			player.performCommand("speedoff");
 		}
 	}
 	
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		plugin.players.put(player,(double) 1);
 		for(int i = 0; i < 501; i++) {
 			if ((SetSpeed.Permissions == null && player.isOp()) || 
 					(SetSpeed.Permissions != null && SetSpeed.Permissions.has(player, ("setspeed.perm." + i)))) {
 				plugin.players.put(player,(double)(i));
 			}
 		}
 	}
 	
 	public void onVehicleExit(VehicleExitEvent event) {
 		Player player = (Player) event.getExited();
 		for(int i = 0; i < 501; i++) {
 			if ((SetSpeed.Permissions == null && player.isOp()) || 
 					(SetSpeed.Permissions != null && SetSpeed.Permissions.has(player, ("setspeed.perm." + i)))) {
 				plugin.players.put(player,(double)(i));
 			}
 		}
 	}
 }
