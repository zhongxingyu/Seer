 package uk.codingbadgers.bFundamentals.backup;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 /**
  * A simple list to backup and restore players to their backed up state.
  */
 public class BackupMap {
 
 	private Map<String, PlayerBackup> backups = new HashMap<String, PlayerBackup>();
 	private final Player player;
 	
 	public BackupMap(Player player) {
 		this.player = player;
 	}
 
 	
 	public void loadBackups() {
 		String[] worlds = BackupFactory.BACKUP_DIR.list();
 		for (String world : worlds) {
 			backups.put(world, BackupFactory.readBackup(world, player.getName()));
 		}
 	}
 	
     /**
      * Clear a players armour slots
      * 
      * @param inventory	The players inventory
      */
     private void clearArmor(){
     	player.getInventory().setHelmet(null);
     	player.getInventory().setChestplate(null);
     	player.getInventory().setLeggings(null);
     	player.getInventory().setBoots(null);
     }
 	
 	/**
 	 * Backup a player.
 	 *
 	 * @param player the player to backup
 	 * @param clearInv clear the players inventory after backing up
 	 */
 	public PlayerBackup backupPlayer(World world, boolean clearInv) {
 		PlayerBackup backup = BackupFactory.createBackup(player);
 		backups.put(world.getName(), backup);
 		
 		if (clearInv) {
 			player.getInventory().clear();
 			clearArmor();
 		}
 		
 		return backup;
 	}
 	
 	/**
 	 * Restore a player to their backup either in this list or on disk.
 	 *
 	 * @param player the player to restore
 	 * @return true if successful and false otherwise
 	 */
 	public boolean restorePlayer(World world) {
 		player.getInventory().clear();
 		clearArmor();
 				
 		PlayerBackup backup = backups.get(world.getName());
 		
 		if (backup == null) {
 			backup = BackupFactory.readBackup(player.getWorld().getName(), player.getName());
 		}
 		
 		if (backup != null) {
 			backup.restore(player);
 			backup.deleteFile();
 			return true;
 		}
 		
 		return false;
 	}
 }
