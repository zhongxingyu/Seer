 package me.furt.CraftEssence.listener;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import me.furt.CraftEssence.CraftEssence;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Snowman;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class ceEntityListener implements Listener {
 
 	CraftEssence plugin;
 
 	public ceEntityListener(CraftEssence instance) {
 		this.plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDeath(final EntityDeathEvent event) {
 		if (event instanceof PlayerDeathEvent) {
 			PlayerDeathEvent e = (PlayerDeathEvent) event;
 			if (plugin.getConfig().getBoolean("DEATH_MSG")) {
 				e.setDeathMessage(ChatColor.YELLOW + e.getDeathMessage());
 			} else {
 				e.setDeathMessage(null);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onCreatureSpawn(final CreatureSpawnEvent event) {
 		World world = event.getLocation().getWorld();
		if (!new File(plugin.getDataFolder() + File.separator + "MobBlacklist",
				world.getName() + ".txt").exists()) {
 			plugin.createMobBlacklist(world.getName());
 			plugin.getLogger().log(
 					Level.INFO,
					"MobBlacklist for " + world.getName()
 							+ " has been created.");
 		} else {
 			String[] mobList = plugin.getMobs(world.getName());
 			for (String creature : mobList) {
 				if (creature.equalsIgnoreCase("CaveSpider")) {
 					if (event.getEntity() instanceof CaveSpider)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Chicken")) {
 					if (event.getEntity() instanceof Chicken)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Cow")) {
 					if (event.getEntity() instanceof Cow)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Creeper")) {
 					if (event.getEntity() instanceof Creeper)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("EnderDragon")) {
 					if (event.getEntity() instanceof EnderDragon)
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
 				} else if (creature.equalsIgnoreCase("Magmacube")) {
 					if (event.getEntity() instanceof MagmaCube)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("MushroomCow")) {
 					if (event.getEntity() instanceof MushroomCow)
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
 				} else if (creature.equalsIgnoreCase("Silverfish")) {
 					if (event.getEntity() instanceof Silverfish)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Skeleton")) {
 					if (event.getEntity() instanceof Skeleton)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Slime")) {
 					if (event.getEntity() instanceof Slime)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Snowman")) {
 					if (event.getEntity() instanceof Snowman)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Spider")) {
 					if (event.getEntity() instanceof Spider)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Squid")) {
 					if (event.getEntity() instanceof Squid)
 						event.setCancelled(true);
 				} else if (creature.equalsIgnoreCase("Villager")) {
 					if (event.getEntity() instanceof Villager)
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
 	}
 }
