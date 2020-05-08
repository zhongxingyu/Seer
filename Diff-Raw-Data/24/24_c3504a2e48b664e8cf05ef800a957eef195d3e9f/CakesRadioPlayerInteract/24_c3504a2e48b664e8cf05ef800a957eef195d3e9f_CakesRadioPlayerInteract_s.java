 package me.cakenggt.CakesRadio;
 
 import java.io.*;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.inventory.ItemStack;
 
 
 public class CakesRadioPlayerInteract implements Listener {
 	CakesRadio p;
 	public CakesRadioPlayerInteract(CakesRadio plugin) {
 		p = plugin;
 	}
 
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		//System.out.println("Player interact event");
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			return;
 		}
 		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
 			return;
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		//System.out.println(block.getBlockPower(BlockFace.NORTH) > 0);
 		//System.out.println(block.getBlockPower(BlockFace.SOUTH) > 0);
 		//System.out.println(block.getBlockPower(BlockFace.EAST) > 0);
 		//System.out.println(block.getBlockPower(BlockFace.WEST) > 0);
		if (player.getItemInHand() == null){
			return;
		}
 		
 		/* Set compass center point to a radio location if you right-click on it while it's powered */
		if (block.getType() == Material.getMaterial(p.getRadioBlock()) && player.getItemInHand().getType() == Material.COMPASS && event.getAction() == Action.RIGHT_CLICK_BLOCK && (block.getBlockPower(BlockFace.NORTH) > 0 || block.getBlockPower(BlockFace.SOUTH) > 0 || block.getBlockPower(BlockFace.EAST) > 0 || block.getBlockPower(BlockFace.WEST) > 0) ){
 			player.setCompassTarget(block.getLocation());
 		}
 		else
 			return;
 	}
 	
 
  
 }
      
