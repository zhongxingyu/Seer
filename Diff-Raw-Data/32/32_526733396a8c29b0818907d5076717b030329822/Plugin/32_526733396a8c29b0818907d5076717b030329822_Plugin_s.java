 package net.marcuswhybrow.minecraft.law;
 
 import net.marcuswhybrow.minecraft.law.listeners.BlockListener;
 import net.marcuswhybrow.minecraft.law.listeners.ImprisonmentListener;
 import net.marcuswhybrow.minecraft.law.listeners.PlayerListener;
 import net.marcuswhybrow.minecraft.law.utilities.MessageDispatcher;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Plugin extends JavaPlugin {
 	private Law law = null;
 	
 	@Override
 	public void onEnable() {
 		law = Law.get();
 		law.setPlugin(this);
 		
 		// Register in-game command
 		getCommand("law").setExecutor(new LawCommandExecutor());
 		
 		// Setup event registrations
 		PluginManager pluginManager = getServer().getPluginManager();
 		PlayerListener playerListener = new PlayerListener();
 		BlockListener blockListener = new BlockListener();
 		pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Highest, this);
 		pluginManager.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Highest, this);
 		pluginManager.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Highest, this);
 		
 		// Setup custom event listeners
 		law.addImprisonmentListener(new ImprisonmentListener());
 		
 		// Setup the Law instance
 		law.setup();
 		
 		// Log that everything is complete
 		MessageDispatcher.consoleInfo(Law.ON_ENABLE_MESSAGE);
 	}
 	
 	@Override
 	public void onDisable() {
 		// Saves all objects with state to file
 		law.save();
 		
 		// Log that everything has been disabled
 		MessageDispatcher.consoleInfo(Law.ON_DISABLE_MESSAGE);
 	}
 }
