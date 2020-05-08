 package me.coolblinger.swordsgame.classes;
 
 import me.coolblinger.swordsgame.SwordsGame;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 //This file contains the smaller classes
 
 public class SwordsGamePlayerRestore {
 	public Location location;
 	private final ItemStack[] inventory;
 	private final Player player;
 	private final SwordsGame plugin;
 	public final String arena;
 	public boolean noDamage = false; // Used in the entity listener, to prevent weapon spamming.
 	public boolean noMovement = false; // Used in the player listener, to prevent the PLAYER_MOVE event from getting fired twice.
 
 	public SwordsGamePlayerRestore(Player _player, String _arena, SwordsGame _plugin) {
 		player = _player;
 		location = player.getLocation();
 		inventory = player.getInventory().getContents();
 		arena = _arena;
 		plugin = _plugin;
 		player.setHealth(20);
 		player.getInventory().clear();
 	}
 
 	public void restore() {
 		player.getInventory().setContents(inventory);
 		player.teleport(location);
 		plugin.players.remove(player);
 		plugin.games.get(arena).removePlayer(player);
 	}
 }
