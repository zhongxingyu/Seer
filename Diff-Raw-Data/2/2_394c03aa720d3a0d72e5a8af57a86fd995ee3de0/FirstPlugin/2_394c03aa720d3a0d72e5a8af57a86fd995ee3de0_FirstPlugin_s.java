 // The package that contains the class
 package com.bukkthat.firstplugin;
 
 // Import everything we use from the Bukkit API or the Java language.
 // Imports are used to tell Java what Classes you're using, as more than one
 // program could have a class called "Player".
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
  * Our class is named FirstPlugin, every word capitalized by convention
  * It extends the class JavaPlugin which allows you to use all the methods
  * contained in it.  All plugins extend JavaPlugin, as they're a plugin for
  * Bukkit made in java.
  * It implements the interface Listener which allows the methods in this class
  * to be called when the events are triggered.
  */
 public class FirstPlugin extends JavaPlugin implements Listener {
 
 	/**
 	 * This method is called when the server enables the plugin.
 	 */
 	public void onEnable() {
 		// Get the plugin manager
 		PluginManager pm = getServer().getPluginManager();
 		// Register the events in this class.
 		// This looks repetitive because we extend JavaPlugin(the first argument)
 		// and implement Listener(the second argument).
 		pm.registerEvents(this, this);
 	}
 
 	/**
 	 * This method is called everytime a block is placed.
 	 * It will then tell the player what type the block was.
 	 * <p>
 	 * This method is called when Blocks are placed due to three steps we've taken:
 	 * 1) We added the @EventHandler annotation to the method.  This tells Bukkit
 	 * that this method is an EventHandler, and needs to be invoked when a specific Event happens.
 	 * 2) We registered our events.  In our onEnable() method we invoke PluginManager#registerEvents.
 	 * This method tells Bukkit to scan through this class for EventHandlers.  If you don't register
 	 * events, then your EventHandlers will never be invoked.
 	 * 3) We defined the Event that we want to listen to in the method constructor.  In the parenthesis
 	 * you can see that we have "BlockPlaceEvent event".  The first word is a the Type of parameter
 	 * that this method takes, and in the situation of EventHandlers, is the Event that we want to
 	 * listen for.  If you wanted to listen for PlayerDeathEvent, in the parenthesis you'd have
 	 * "PlayerDeathEvent event".
 	 */
 	@EventHandler // This is very important.  It tell Bukkit that this is an EventHandler method.
 	public void onBlockPlace(BlockPlaceEvent event) {
 		// Get the player who triggered the event.
 		Player player = event.getPlayer();
 		// Tell the player what type of block he placed.
 		player.sendMessage(ChatColor.GOLD + "You placed a " + event.getBlock().getType());
 	}
 
 	/**
 	 * This method is called everytime a block is broken. It will then tell the
 	 * player what type the block was.
 	 * <p>
 	 * This method is called when Blocks are broken due to three steps we've taken:
 	 * 1) We added the @EventHandler annotation to the method.  This tells Bukkit
 	 * that this method is an EventHandler, and needs to be invoked when a specific Event happens.
 	 * 2) We registered our events.  In our onEnable() method we invoke PluginManager#registerEvents.
 	 * This method tells Bukkit to scan through this class for EventHandlers.  If you don't register
 	 * events, then your EventHandlers will never be invoked.
 	 * 3) We defined the Event that we want to listen to in the method constructor.  In the parenthesis
	 * you can see that we have "BlockPlaceEvent event".  The first word is a the Type of parameter
 	 * that this method takes, and in the situation of EventHandlers, is the Event that we want to
 	 * listen for.  If you wanted to listen for PlayerDeathEvent, in the parenthesis you'd have
 	 * "PlayerDeathEvent event".
 	 */
 	@EventHandler // This is very important.  It tell Bukkit that this is an EventHandler method.
 	public void onBlockBreak(BlockBreakEvent event) {
 		// Get the player who triggered the event.
 		Player player = event.getPlayer();
 		// Tell the player what type of block he broke.
 		player.sendMessage(ChatColor.GOLD + "You broke a " + event.getBlock().getType());
 	}
 
 }
