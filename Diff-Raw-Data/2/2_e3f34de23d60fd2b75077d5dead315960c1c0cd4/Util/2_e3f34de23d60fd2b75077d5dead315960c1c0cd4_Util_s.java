 package me.naithantu.ArenaPVP.Util;
 
 import java.util.Collection;
 import java.util.List;
 
 import me.naithantu.ArenaPVP.Storage.YamlStorage;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.util.Vector;
 
 public class Util {
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
 
 	public static Location getLocationFromString(String string) {
 		String[] stringSplit = string.split(":");
 		World world = Bukkit.getServer().getWorld(stringSplit[0]);
 		Double x = Double.valueOf(stringSplit[1]);
 		Double y = Double.valueOf(stringSplit[2]);
 		Double z = Double.valueOf(stringSplit[3]);
 		Float yaw = Float.valueOf(stringSplit[4]);
 		Float pitch = Float.valueOf(stringSplit[5]);
 		Location location = new Location(world, x, y, z, yaw, pitch);
 		return location;
 	}
 
 	public static String getStringFromLocation(Location location) {
 		return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void playerLeave(Player player, YamlStorage playerStorage) {
 		Configuration playerConfig = playerStorage.getConfig();
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
		if (playerConfig.getBoolean("flying")) {
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
 
 	public static void playerJoin(Player player, YamlStorage playerStorage) {
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
