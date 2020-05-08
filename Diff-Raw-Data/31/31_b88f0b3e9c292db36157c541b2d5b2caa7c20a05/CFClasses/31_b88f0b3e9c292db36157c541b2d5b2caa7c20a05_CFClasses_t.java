 package org.github.craftfortress2;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
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
 			if(CFCommandExecutor.getClass(player).equals("scout")){
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
 	public static void loadInv(Player player){
 		player.getInventory().clear();
		player.getInventory().setContents(getSavedInv(player));
 	}
	public static ItemStack[] getSavedInv(Player player){
 		int index = CFCommandExecutor.players.lastIndexOf(player);
 		if (index == -1){
 			return null;
 		}
		return CFCommandExecutor.saveItemStack.get(index);
 	}
 }
