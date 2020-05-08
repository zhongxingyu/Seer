 package nl.imesiacraft.nlundeadoverrun;
 
 import java.util.HashMap;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class UndeadPlayers {
 	
 	private static HashMap <String, OldPlayer> oldPlayers = new HashMap<String, OldPlayer>();
 	
 	public static OldPlayer getPlayer(String string) {
 		if (oldPlayers.containsKey(string)) {
 			return oldPlayers.get(string);
 		} else {
 			return null;
 		}
 	}
 	
 	public static void addPlayer(String name, OldPlayer player) {
 		oldPlayers.put(name, player);
 	}
 	
 	public static void restorePlayer(Player player) {
 		if (oldPlayers.containsKey(player.getName())) {
 			OldPlayer oPlayer = oldPlayers.get(player.getName());
 			
 			player.teleport(oPlayer.getLocation());
			player.getInventory().setContents(oPlayer.getInventory());
			player.getInventory().setArmorContents(oPlayer.getArmor());
 			player.setGameMode(oPlayer.getGameMode());
 			oldPlayers.remove(player.getName());
 		}
 	}
 	
 	public static class OldPlayer {
 		private Location location = null;
		private ItemStack[] inventory = null;
		private ItemStack[] armor = null;
		
 		private GameMode gamemode = GameMode.SURVIVAL;
 		
 		public OldPlayer (Location location, Inventory inventory, GameMode gamemode) {
 			this.location = location;
			this.inventory = inventory.getContents();
			this.armor = ((PlayerInventory)inventory).getArmorContents();
 			this.gamemode = gamemode;
 		}
 		
 		public Location getLocation() {
 			return location;
 		}
 		
		public ItemStack[] getInventory() {
 			return inventory;
 		}
 		
		public ItemStack[] getArmor() {
			return armor;
		}
		
 		public GameMode getGameMode() {
 			return gamemode;
 		}
 	}
 
 }
