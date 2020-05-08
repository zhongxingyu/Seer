 package me.furt.CraftEssence.listener;
 
 import java.io.File;
 
 import me.furt.CraftEssence.CraftEssence;
 import me.furt.CraftEssence.ceConfig;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class ceEntityListener extends EntityListener implements Cancellable {
 
 	CraftEssence plugin;
 
 	public ceEntityListener(CraftEssence instance) {
 		this.plugin = instance;
 	}
	
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (ceConfig.deathMsg) {
			String deathMsg = event.getDeathMessage();
			event.setDeathMessage(ChatColor.YELLOW + deathMsg);
		} else {
			event.setDeathMessage(null);
 		}
 	}
 
 	@Override
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		World world = event.getLocation().getWorld();
 		if (!new File("plugins" + File.separator + "CraftEssence"
 				+ File.separator + "MobBlackList", world.getName() + ".txt")
 				.exists()) {
 			plugin.createMobBlacklist(world.getName());
 		}
 		String[] mobList = plugin.getMobs(world.getName());
 		for (String creature : mobList) {
 			if (creature.equalsIgnoreCase("Chicken")) {
 				if (event.getEntity() instanceof Chicken)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Cow")) {
 				if (event.getEntity() instanceof Cow)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Creeper")) {
 				if (event.getEntity() instanceof Creeper)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Enderman")) {
 				if (event.getEntity() instanceof Enderman)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Ghast")) {
 				if (event.getEntity() instanceof Ghast)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Giant")) {
 				if (event.getEntity() instanceof Giant)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Pig")) {
 				if (event.getEntity() instanceof Pig)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("PigZombie")) {
 				if (event.getEntity() instanceof PigZombie)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Sheep")) {
 				if (event.getEntity() instanceof Sheep)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Skeleton")) {
 				if (event.getEntity() instanceof Skeleton)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Slime")) {
 				if (event.getEntity() instanceof Slime)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Spider")) {
 				if (event.getEntity() instanceof Spider)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Squid")) {
 				if (event.getEntity() instanceof Squid)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Wolf")) {
 				if (event.getEntity() instanceof Wolf)
 					event.setCancelled(true);
 			} else if (creature.equalsIgnoreCase("Zombie")) {
 				if (event.getEntity() instanceof Zombie)
 					event.setCancelled(true);
 			}
 		}
 	}
 
 	@Override
 	public boolean isCancelled() {
 		return false;
 	}
 
 	@Override
 	public void setCancelled(boolean arg0) {
 
 	}
 }
