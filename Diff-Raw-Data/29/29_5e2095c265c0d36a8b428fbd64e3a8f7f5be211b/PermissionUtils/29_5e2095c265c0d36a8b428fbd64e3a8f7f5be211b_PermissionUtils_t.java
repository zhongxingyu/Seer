 package kr.kkiro.projects.bukkit.EntityProtect.utils;
 
 import kr.kkiro.projects.bukkit.EntityProtect.bukkit.EntityProtect;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.config.Config;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.DatabaseUtils;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.EntitySet;
 import kr.kkiro.projects.bukkit.EntityProtect.utils.database.PlayerSet;
 
 import org.bukkit.entity.Player;
 
 public class PermissionUtils {
 	
 	public static boolean canBypass(String activity, Player player, Boolean hasOwner) {
 		if(player.hasPermission("entityprotect.bypass-protect."+activity)) return true;
 		if(hasOwner && Config.getString("protect-entities."+activity).equals("false")) return true;
 		if(Config.getString("protect-entities."+activity).equals("nonowner")) return false;
 		return false;
 	}
 	public static boolean canBypass(String activity, Player player, EntitySet entity) {
 		if(player.hasPermission("entityprotect.bypass-protect."+activity)) return true;
 		if(entity == null) return !(Config.getString("protect-entities."+activity).equals("nonowner"));
		if(entity != null && Config.getString("protect-entities."+activity).equals("false")) return true;
 		if(entity.getOwnerItem().getPlayer().equals(player.getName())) return true;
 		return false;
 	}
 	public static boolean canBypass(String activity, Boolean hasOwner) {
 		if(!hasOwner && Config.getString("protect-entities."+activity).equals("false")) return true;
 		if(Config.getString("protect-entities."+activity).equals("nonowner")) return false;
 		return false;
 	}
 	public static boolean canBypassLimit(Player player) {
 		if(!Config.getBoolean("general.allow-bypass")) return false;
 		if(player.hasPermission("entityprotect.bypass-limit")) return true;
 		return false;
 	}
 	public static boolean canBreed(Player player, EntitySet entity) {
 		if(!canBypass(EntityActivity.BREED, player, entity)) {
 			ChatUtils.sendLang(player, "access-denied");
 			return false;
 		}
 		PlayerSet playerset = DatabaseUtils.searchPlayer(player.getName());
 		if (playerset == null) {
 			playerset = new PlayerSet();
 			playerset.setPlayer(player.getName());
 			playerset.setBreedCount(0);
 			DatabaseUtils.save(playerset);
 		}
 		if(playerset.getBreedCount() < Config.getInt("general.max-entities-per-player")) {
 			return true;
 		} else {
 			if(canBypassLimit(player)) {
 				return true;
 			} else {
 				ChatUtils.sendLang(player, "breed-fail");
 				ChatUtils.sendLang(EntityProtect.getInstance().getServer().getConsoleSender(), "console.breed-fail", player.getName()); 
 				return false;
 			}
 		}
 	}
 	public static boolean canBreed(String player) {
 		PlayerSet playerset = DatabaseUtils.searchPlayer(player);
 		if (playerset == null) {
 			playerset = new PlayerSet();
 			playerset.setPlayer(player);
 			playerset.setBreedCount(0);
 			DatabaseUtils.save(playerset);
 		}
 		if(playerset.getBreedCount() < Config.getInt("general.max-entities-per-player")) {
 			return true;
 		} else {
 			ChatUtils.sendLang(EntityProtect.getInstance().getServer().getConsoleSender(), "console.breed-fail", player); 
 			return false;
 		}
 	}
 }
