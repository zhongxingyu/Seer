 /**
  * 
  */
 package com.jynxdaddy.wolfspawn;
 
 import java.util.logging.Logger;
 
 import net.minecraft.server.EntityWolf;
 
 import org.bukkit.craftbukkit.entity.CraftWolf;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 /**
  * @author Ashton
  * 
  */
 public class WolfListener extends EntityListener {
 
 	private WolfSpawn plugin;
 	@SuppressWarnings("unused")
 	private static Logger log;
 
 	public WolfListener(WolfSpawn plugin) {
 		this.plugin = plugin;
 		WolfListener.log = WolfSpawn.log;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.bukkit.event.entity.EntityListener#onEntityDeath(org.bukkit.event
 	 * .entity.EntityDeathEvent)
 	 */
 	@Override
 	public void onEntityDeath(EntityDeathEvent event) {
 		if (event.getEntity() instanceof Wolf) {
 			Wolf wolf = (Wolf) event.getEntity();
 			
 			//Workaround
 			EntityWolf mcwolf = ((CraftWolf)  wolf).getHandle();
 			String owner = mcwolf.v();
 			
 			Player player = plugin.getServer().getPlayer(owner);
			
			if (player == null || !plugin.getPermission(player, "WolfSpawn.respawn")) return;
 			
 			if (plugin.isReleasePlayer(owner)) {
 				plugin.sendMessage(player, WolfSpawn.Message.WOLF_RELEASE);
 			}
 			else if (owner != null && owner.length() > 0) {		
 				plugin.sendMessage(player, WolfSpawn.Message.WOLF_DEATH);
 				plugin.respawnWolf(player, wolf.getWorld(), owner);
 			}
 		}
 	}
 
 }
