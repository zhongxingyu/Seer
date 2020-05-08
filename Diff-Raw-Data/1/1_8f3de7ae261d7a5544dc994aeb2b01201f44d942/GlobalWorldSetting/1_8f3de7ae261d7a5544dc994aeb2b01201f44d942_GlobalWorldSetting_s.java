 package couk.Adamki11s.Regios.Regions;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 import couk.Adamki11s.Regios.Permissions.PermissionsCore;
 
 public class GlobalWorldSetting {
 
 	public String world;
 
 	public boolean invert_protection = false, invert_pvp = false, overridingPvp = false, lightning_enabled = true, stormEnabled = true, creeperExplodes = true, fireEnabled = true, blockForm_enabled = true;
 	
 	public ArrayList<CreatureType> creaturesWhoSpawn = new ArrayList<CreatureType>();
 
 	public GlobalWorldSetting(String world) {
 		this.world = world;
 	}
 	
 	public boolean canCreatureSpawn(CreatureType ct){
 		return creaturesWhoSpawn.contains(ct);
 	}
 	
 	public void addCreatureSpawn(CreatureType ct){
 		creaturesWhoSpawn.add(ct);
 	}
 	
 	public boolean canBypassWorldChecks(Player p){
 		return (PermissionsCore.doesHaveNode(p, "regios.worldprotection.bypass") || PermissionsCore.doesHaveNode(p, "regios." + world + ".bypass") || p.isOp());
 	}
 	
 	public static void writeWorldsToConfiguration(){
 		for(World w : Bukkit.getServer().getWorlds()){
 			String world = w.getName();
 			File root = new File("plugins" + File.separator + "Regios" + File.separator + "Configuration" + File.separator + "WorldConfigurations");
 			if(!root.exists()){ root.mkdir(); }
 			File f = new File("plugins" + File.separator + "Regios" + File.separator + "Configuration" + File.separator + "WorldConfigurations" + File.separator + world + ".rwc");
 			if(!f.exists()){
 				System.out.println("[Regios] Creating world configuration for world : " + world);
 				try {
 					f.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				Configuration c = new Configuration(f);
 				c.setProperty(world + ".Protection.ProtectionEnabledOutsideRegions", false);
 				c.setProperty(world + ".PvP.EnabledOutsideRegions", true);
 				c.setProperty(world + ".PvP.OverrideServerPvP", false);
 				c.setProperty(world + ".Protection.FireEnabled", true);
 				c.setProperty(world + ".Weather.LightningEnabled", true);
 				c.setProperty(world + ".Mobs.Spawning.Chicken", true);
 				c.setProperty(world + ".Mobs.Spawning.Cow", true);
 				c.setProperty(world + ".Mobs.Spawning.Creeper", true);
 				c.setProperty(world + ".Mobs.Spawning.Ghast", true);
 				c.setProperty(world + ".Mobs.Spawning.Giant", true);
 				c.setProperty(world + ".Mobs.Spawning.Pig", true);
 				c.setProperty(world + ".Mobs.Spawning.PigZombie", true);
 				c.setProperty(world + ".Mobs.Spawning.Sheep", true);
 				c.setProperty(world + ".Mobs.Spawning.Skeleton", true);
 				c.setProperty(world + ".Mobs.Spawning.Slime", true);
 				c.setProperty(world + ".Mobs.Spawning.Spider", true);
 				c.setProperty(world + ".Mobs.Spawning.Squid", true);
 				c.setProperty(world + ".Mobs.Spawning.Wolf", true);
 				c.setProperty(world + ".Mobs.Spawning.Zombie", true);
 				c.setProperty(world + ".Mobs.Creeper.DoesExplode", true);
 				c.setProperty(world + ".Block.BlockForm.Enabled", true);
 				c.save();
 			}
 		}
 	}
 	
 	public static void loadWorldsFromConfiguration(){
 		for(World w : Bukkit.getServer().getWorlds()){
 			System.out.println("[Regios] Loading world configuration for world : " + w.getName());
 			String world = w.getName();
 			File root = new File("plugins" + File.separator + "Regios" + File.separator + "Configuration" + File.separator + "WorldConfigurations");
 			if(!root.exists()){ root.mkdir(); }
 			File f = new File("plugins" + File.separator + "Regios" + File.separator + "Configuration" + File.separator + "WorldConfigurations" + File.separator + world + ".rwc");
 			Configuration c = new Configuration(f);
 			c.load();
 			GlobalWorldSetting gws = new GlobalWorldSetting(world);
 			gws.invert_protection = c.getBoolean(world + ".Protection.ProtectionEnabledOutsideRegions", false);
 			gws.fireEnabled = c.getBoolean(world + ".Protection.FireEnabled", false);
 			gws.invert_pvp = c.getBoolean(world + ".PvP.EnabledOutsideRegions", true);
 			gws.lightning_enabled = c.getBoolean(world + ".Weather.LightningEnabled", true);
 			gws.creeperExplodes = c.getBoolean(world + ".Mobs.Creeper.DoesExplode", true);
 			gws.overridingPvp = c.getBoolean(world + ".PvP.OverrideServerPvP", true);
 			gws.blockForm_enabled = c.getBoolean(world + ".Block.BlockForm.Enabled", true);
 			if(c.getBoolean(world + ".Mobs.Spawning.Chicken", true)){ gws.addCreatureSpawn(CreatureType.CHICKEN); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Cow", true)){ gws.addCreatureSpawn(CreatureType.COW); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Creeper", true)){ gws.addCreatureSpawn(CreatureType.CREEPER); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Ghast", true)){ gws.addCreatureSpawn(CreatureType.GHAST); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Giant", true)){ gws.addCreatureSpawn(CreatureType.GIANT); }
 			if(c.getBoolean(world + ".Mobs.Spawning.PigZombie", true)){ gws.addCreatureSpawn(CreatureType.PIG_ZOMBIE); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Sheep", true)){ gws.addCreatureSpawn(CreatureType.SHEEP); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Skeleton", true)){ gws.addCreatureSpawn(CreatureType.SKELETON); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Slime", true)){ gws.addCreatureSpawn(CreatureType.SLIME); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Spider", true)){ gws.addCreatureSpawn(CreatureType.SPIDER); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Squid", true)){ gws.addCreatureSpawn(CreatureType.SQUID); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Wolf", true)){ gws.addCreatureSpawn(CreatureType.WOLF); }
 			if(c.getBoolean(world + ".Mobs.Spawning.Zombie", true)){ gws.addCreatureSpawn(CreatureType.ZOMBIE); }
 			GlobalRegionManager.addWorldSetting(gws);
 		}
 	}
 
 }
