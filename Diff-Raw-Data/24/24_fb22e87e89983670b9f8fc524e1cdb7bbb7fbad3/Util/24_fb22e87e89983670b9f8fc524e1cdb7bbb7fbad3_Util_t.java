 package me.naithantu.ArenaPVP.Util;
 
import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Storage.YamlStorage;
 import org.bukkit.*;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.util.Vector;
 
 import java.util.Collection;
 import java.util.List;
import java.util.logging.Level;
 
 public class Util {
 
     private Util(){
     }
 
 	public static void broadcast(String msg) {
 		msg = ChatColor.translateAlternateColorCodes('&', msg);
 		Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "[PVP] " + ChatColor.WHITE + msg);
 	}
 
 	public static void msg(CommandSender sender, String msg) {
 		msg = ChatColor.translateAlternateColorCodes('&', msg);
 		if (sender instanceof Player) {
 			sender.sendMessage(ChatColor.DARK_RED + "[PVP] " + ChatColor.WHITE + msg);
 		} else {
 			sender.sendMessage("[PVP] " + msg);
 		}
 	}
 
     public static Location getLocation(YamlStorage storage, String key) {
         FileConfiguration config = storage.getConfig();
         String worldName = config.getString(key + ".world");
         World world = Bukkit.getWorld(worldName);
         double x = config.getDouble(key + ".x");
         double y = config.getDouble(key + ".y");
         double z = config.getDouble(key + ".z");
         float yaw = (float) config.getDouble(key + ".yaw");
         float pitch = (float) config.getDouble(key + ".pitch");
         return new Location(world, x, y, z, yaw, pitch);
     }
 
     public static void saveLocation(Location location, YamlStorage storage, String key){
         saveLocation(location, storage, key, true);
     }
 
     public static void saveLocation(Location location, YamlStorage storage, String key, boolean round){
         if(round)
             location = roundLocation(location);
         FileConfiguration config = storage.getConfig();
         config.set(key + ".world", location.getWorld().getName());
         config.set(key + ".x", location.getX());
         config.set(key + ".y", location.getY());
         config.set(key + ".z", location.getZ());
         config.set(key + ".yaw", location.getYaw());
         config.set(key + ".pitch", location.getPitch());
         storage.saveConfig();
     }
 
 
     public static Location roundLocation(Location location) {
         location.setX(0.5 * Math.round(location.getX() / 0.5));
         location.setY(0.5 * Math.round(location.getY() / 0.5));
         location.setZ(0.5 * Math.round(location.getZ() / 0.5));
         location.setYaw(45 * (Math.round(location.getYaw() / 45)));
         location.setPitch(45 * (Math.round(location.getPitch() / 45)));
         return location;
     }
 
 
 	@SuppressWarnings("unchecked")
 	public static void loadPlayerConfig(Player player, YamlStorage playerStorage) {
 		Configuration playerConfig = playerStorage.getConfig();
 
         //Check if playerConfig was properly saved.
         if(!playerConfig.contains("gamemode")){
            ArenaPVP.getInstance().getLogger().log(Level.SEVERE, "Unable to load player config for player " + player.getName() +  "!");
            System.out.println();
             return;
         }
        ArenaPVP.getInstance().getLogger().log(Level.INFO, "Loading config for " + player.getName() +  "!");
        //TODO Remove debug log
 
        //Clear players inventory and then load saved inventory.
 		PlayerInventory inventory = player.getInventory();
 		inventory.clear();
 		inventory.setArmorContents(new ItemStack[4]);
 
 		List<ItemStack> inventoryContents = (List<ItemStack>) playerConfig.getList("inventory");
 		List<ItemStack> armorContents = (List<ItemStack>) playerConfig.getList("armor");
 
 		player.setLevel(playerConfig.getInt("level"));
 		player.setExp((float) playerConfig.getDouble("exp"));
 		player.setHealth(playerConfig.getDouble("health"));
 		player.setFoodLevel(playerConfig.getInt("hunger"));
 		player.setGameMode(GameMode.valueOf(playerConfig.getString("gamemode")));
 		if (playerConfig.getBoolean("flying") || GameMode.valueOf(playerConfig.getString("gamemode")) == GameMode.CREATIVE) {
 			player.setAllowFlight(true);
 			player.setFlying(true);
 		} else {
 			player.setAllowFlight(false);
 		}
 		player.addPotionEffects((Collection<PotionEffect>) playerConfig.getList("potioneffects"));
 
 		inventory.setContents(inventoryContents.toArray(new ItemStack[36]));
 		inventory.setArmorContents(armorContents.toArray(new ItemStack[4]));
 
 		playerConfig.set("inventory", null);
 		playerConfig.set("armor", null);
 		playerConfig.set("level", null);
 		playerConfig.set("exp", null);
 		playerConfig.set("health", null);
 		playerConfig.set("hunger", null);
 		playerConfig.set("gamemode", null);
 		playerConfig.set("flying", null);
 		playerConfig.set("potioneffects", null);
 		playerConfig.set("hastoleave", null);
 		playerStorage.saveConfig();
 	}
 
 	public static void savePlayerConfig(Player player, YamlStorage playerStorage) {
        ArenaPVP.getInstance().getLogger().log(Level.INFO, "Saving config for " + player.getName() +  "!");
        Configuration playerConfig = playerStorage.getConfig();
 
 		//Save players inventory and then clear it.
 		PlayerInventory inventory = player.getInventory();
 
 		playerConfig.set("inventory", inventory.getContents());
 		playerConfig.set("armor", inventory.getArmorContents());
 		playerConfig.set("level", player.getLevel());
 		playerConfig.set("exp", player.getExp());
 		playerConfig.set("health", player.getHealth());
 		playerConfig.set("hunger", player.getFoodLevel());
 		playerConfig.set("gamemode", player.getGameMode().toString());
 		playerConfig.set("flying", player.isFlying());
 		playerConfig.set("potioneffects", player.getActivePotionEffects());
 		playerStorage.saveConfig();
 
 		inventory.clear();
 		inventory.setArmorContents(new ItemStack[4]);
 		player.setLevel(0);
 		player.setExp(0);
 		player.setVelocity(new Vector(0, 0, 0));
 		player.setGameMode(GameMode.SURVIVAL);
 		player.setFoodLevel(20);
 		player.setHealth(20);
 		for (PotionEffect effect : player.getActivePotionEffects())
 			player.removePotionEffect(effect.getType());
 	}
 	
 	public static String capaltizeFirstLetter(String s) {
 		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
 	}
 }
