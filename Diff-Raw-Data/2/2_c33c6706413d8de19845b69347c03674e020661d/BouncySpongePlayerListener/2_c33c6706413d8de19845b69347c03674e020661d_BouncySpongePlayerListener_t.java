 package com.adencraft2000.bouncysponge;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 
 public class BouncySpongePlayerListener implements Listener{
 	public BouncySponge plugin;
 	/**
 	 * Constructor for PlayerListener
 	 * @param instance Grabs an instance of BouncySponge
 	 */
 	public BouncySpongePlayerListener(BouncySponge instance) {
 		plugin = instance;
 	}
 
 	/**
 	 * Calls when player moves
 	 * If a player moves on to a Sponge he is catapulted up in the air! 
 	 * @param ev A PlayerMoveEvent object
 	 */
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent ev){
 		if (!ev.getFrom().getBlock().getLocation().equals(ev.getTo().getBlock().getLocation())) {
 			Player player = ev.getPlayer();
 			if (player.hasPermission("bouncysponge.jump")) {
 				Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 				if (block.getType() == Material.SPONGE) {
 					if (player.isSneaking()){
 					}
 					else{
 					Vector dir = player.getLocation().getDirection().multiply(1.75);
 					Vector vec = new Vector(dir.getX(), plugin.getConfig().getDouble("launch"), dir.getZ());
 					player.setVelocity(vec);
					player.setNoDamageTicks(400);
 					
 				} 
 			}
 		}
 	}
 
 	}
 
 }
