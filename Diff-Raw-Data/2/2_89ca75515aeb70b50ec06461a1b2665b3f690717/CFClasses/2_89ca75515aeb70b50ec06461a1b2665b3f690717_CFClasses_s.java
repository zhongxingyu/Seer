 package org.github.craftfortress2;
 import java.util.ArrayList;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.PlayerInventory;
 public class CFClasses implements Listener{
 	@EventHandler
 	public void onHungerBarChange(FoodLevelChangeEvent event){
 		event.setCancelled(true);
 	}
 	@EventHandler
 	public void onPickupItem(PlayerPickupItemEvent event){
 		Player player = event.getPlayer();
 		if(CFCommandExecutor.isPlaying(player) && event.getItem().equals(Material.BOOK)){
 			event.setCancelled(false);
 		}else if(CFCommandExecutor.isPlaying(player)){
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void pvp(EntityDamageByEntityEvent event){
 		Player player = (Player) event.getDamager();
 		Player pyr = (Player) event.getEntity();
 			if(CFCommandExecutor.getTeam(player) == CFCommandExecutor.getTeam(pyr)){
 				event.setCancelled(true);
 			}
 		}
 	@EventHandler
 	public void dropItem(PlayerDropItemEvent event){
 		Player player = event.getPlayer();
 		if(CFCommandExecutor.isPlaying(player)){
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		Player player = event.getPlayer();
 		if(CFCommandExecutor.isPlaying(player)){
			if(player.getClass().equals("scout")){
 				Scout.init(player);
 			}else if(player.getClass().equals("soldier")){
 				
 			}else if(player.getClass().equals("heavy")){
 				
 			}else if(player.getClass().equals("demoman")){
 				
 			}else if(player.getClass().equals("medic")){
 				
 			}else if(player.getClass().equals("pyro")){
 				
 			}else if(player.getClass().equals("sniper")){
 				
 			}else if(player.getClass().equals("engineer")){
 				
 			}else if(player.getClass().equals("spy")){
 				
 			}
 		}
 	}
 	static ArrayList<PlayerInventory> playerInventories = new ArrayList<PlayerInventory>();
 	static ArrayList<Player> players = new ArrayList<Player>(); // Use the one in CFCE later
 	public static void saveInv(Player player){
 		PlayerInventory inv = player.getInventory();
 		playerInventories.add(inv);
 		players.add(player);
 	}
 	public static void loadInv(Player player){
 		PlayerInventory inv = player.getInventory();
 		PlayerInventory savedinv = getSavedInv(player);
 		inv.setContents(savedinv.getContents());
 	}
 	public static PlayerInventory getSavedInv(Player player){
 		int index = players.lastIndexOf(player);
 		if (index == -1){
 			return null;
 		}
 		return playerInventories.get(index);
 	}
 }
