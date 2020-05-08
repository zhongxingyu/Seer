 package me.rotzloch.listener;
 
 import me.rotzloch.Classes.Helper;
 import me.rotzloch.Classes.Reward;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class RewardListener implements Listener{
 
 	@EventHandler (priority = EventPriority.HIGHEST) 
     public void onSignChangeEvent(SignChangeEvent ev) {
         Reward reward = new Reward(ev, ev.getPlayer());
        if(reward != null && !ev.isCancelled() && ev.getLine(0).equalsIgnoreCase("[Reward]")) {
         	Helper.SendMessageInfo(ev.getPlayer(), "Reward erstellt");
         }
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST) 
 	public void onSignClick(PlayerInteractEvent ev) {
 		try {
 			if(ev.getAction() != Action.RIGHT_CLICK_BLOCK) {
 				return;
 			}
 			Block evBlock = ev.getClickedBlock();
 			if(evBlock == null) {
 				return;
 			}
 			if(evBlock.getType() == Material.SIGN_POST || evBlock.getType() == Material.WALL_SIGN) {
 				Sign sign = (Sign)evBlock.getState();
 				if(sign.getLine(0).equalsIgnoreCase("[REWARD]")) {
 					if(!ev.getPlayer().hasPermission("marocraft.reward.use")) {
 						Helper.SendMessageNoPermission(ev.getPlayer());
 						ev.setCancelled(true);
 						return;
 					}				
 					Reward reward = new Reward(sign, ev.getPlayer());
 					if(reward != null && !ev.isCancelled()) {
 						reward.GetReward(ev.getPlayer());
 					} else {
 						Helper.SendMessageError(ev.getPlayer(), "Fehler");
 					}
 				}
 			}
 		} catch(Exception e) {
 			Helper.LogError(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
