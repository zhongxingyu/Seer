 package com.titankingdoms.nodinchan.titanchat.util.displayname;
 
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 
 public class DisplayNameChanger {
 	
 	private final TitanChat plugin;
 	
 	public DisplayNameChanger() {
 		this.plugin = TitanChat.getInstance();
 	}
 	
 	/**
 	 * Applies the saved display name of the Player
 	 * 
 	 * @param player The Player to apply
 	 */
 	public void apply(Player player) {
 		DisplayName display = plugin.getDatabase().find(DisplayName.class).where().ieq("name", player.getName()).findUnique();
 		
		if (display == null && !player.getDisplayName().equals(player.getName())) {
 			display = new DisplayName();
 			display.setName(player.getName());
 			display.setDisplayName(player.getDisplayName());
 		}
 		
 		if (display.getDisplayName().length() > 16)
 			player.setPlayerListName(display.getDisplayName().substring(0, 16));
 		else
 			player.setPlayerListName(display.getDisplayName());
 		
 		player.setDisplayName(display.getDisplayName());
 	}
 	
 	/**
 	 * Saves the display name of the Player
 	 * 
 	 * @param player The Player to save
 	 */
 	public void save(Player player) {
 		DisplayName display = plugin.getDatabase().find(DisplayName.class).where().ieq("name", player.getName()).findUnique();
 		
 		if (display != null) {
 			if (player.getDisplayName().equals(player.getName())) {
 				plugin.getDatabase().delete(display);
 				return;
 				
 			} else
 				display.setDisplayName(player.getDisplayName());
 			
 		} else {
 			if (!player.getDisplayName().equals(player.getName())) {
 				display = new DisplayName();
 				display.setName(player.getName());
 				display.setDisplayName(player.getDisplayName());
 				
 			} else
 				return;
 		}
 		
 		plugin.getDatabase().save(display);
 	}
 	
 	/**
 	 * Sets the display name of the Player
 	 * 
 	 * @param player The Player to set
 	 * 
 	 * @param displayname The display name to set to
 	 */
 	public void set(Player player, String displayname) {
 		if (displayname.length() > 16)
 			player.setPlayerListName(displayname.substring(0, 16));
 		else
 			player.setPlayerListName(displayname);
 		
 		player.setDisplayName(displayname);
 	}
 	
 	/**
 	 * Unloads the DisplayNameChanger and saves the display name of all online Players
 	 */
 	public void unload() {
 		for (Player player : plugin.getServer().getOnlinePlayers())
 			save(player);
 	}
 }
