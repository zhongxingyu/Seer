 package uk.codingbadgers.bportalshelper;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.onarandombox.MultiverseCore.api.MVWorldManager;
 import com.onarandombox.MultiversePortals.MultiversePortals;
 import com.onarandombox.MultiversePortals.utils.PortalManager;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 import uk.codingbadgers.bportalshelper.listeners.PlayerListener;
 
 public class bPortalsHelper extends Module {
 	
 	/** The modules name. */
 	public final static String NAME = "bPortals";
 	
 	/** The modules version. */
 	public final static String VERSION = "1.0.0";
 	
 	/** The module instance. */
 	private static bPortalsHelper m_instance = null;
 	
 	/** The plugin. */
 	private static Plugin m_pluginInstance = null;
 	
 	/** The player listener. */
 	private final PlayerListener m_playerListener = new PlayerListener();
 	
 	/** An array of stored players. */
 	private static ArrayList<PortalPlayer> m_players = new ArrayList<PortalPlayer>();
 	
 	/** Access to the world edit api. */
 	private static WorldEditPlugin m_worldEdit = null;
 	
 	/** Access to the world guard api. */
 	private static WorldGuardPlugin m_worldGuard = null;
 	
 	/** The portal manager. */
 	private static MultiversePortals m_multiversePortals = null;
 
 	/**
 	 * Called when the module is disabled.
 	 */
 	public void onDisable() {
 		log(Level.INFO, NAME + " disabled");
 	}
 
 	/**
 	 * Called when the module is enabled.
 	 */
 	public void onEnable() {
 		
 		m_pluginInstance = m_plugin;
 		m_instance = this;
 
 		m_worldEdit = (WorldEditPlugin)m_plugin.getServer().getPluginManager().getPlugin("WorldEdit");
 		if (m_worldEdit == null) {
 			log(Level.SEVERE, "Could not find world edit plugin. This module requires world edit.");
 		}
 		
 		m_worldGuard = (WorldGuardPlugin)m_plugin.getServer().getPluginManager().getPlugin("WorldGuard");
 		if (m_worldGuard == null) {
 			log(Level.SEVERE, "Could not find world guard plugin. This module requires world guard.");
 		}
 		
 		m_multiversePortals = (MultiversePortals)m_plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
 		if (m_multiversePortals == null) {
 			log(Level.SEVERE, "Could not find multi-verse portal manager. This module requires multi-verse portals.");
 		}
 		
 		register(m_playerListener);
 		
 		log(Level.INFO, NAME + " enabled");
 	}
 	
 	
 	/**
 	 * Gets the module instance
 	 * 
 	 * @return The module instance
 	 */
 	public static bPortalsHelper getInstance() {
 		return m_instance;
 	}
 	
 	/**
 	 * Gets the plugin.
 	 *
 	 * @return the plugin
 	 */
 	public static Plugin getPlugin() {
 		return m_pluginInstance;
 	}
 	
 	/**
 	 * Get access to the the world edit plugin
 	 * 
 	 * @return Access to the world edit plugin
 	 */
 	public static WorldEditPlugin getWorldEdit() {
 		return m_worldEdit;
 	}
 	
 	/**
 	 * Get access to the the world guard plugin
 	 * 
 	 * @return Access to the world guard plugin
 	 */
 	public static WorldGuardPlugin getWorldGuard() {
 		return m_worldGuard;
 	}
 
 	/**
 	 * Gets or creates a portal player.
 	 *
 	 * @param player the player
 	 * @return The pre-existing or created portal player
 	 */
 	public static PortalPlayer getOrCreatePlayer(Player player) {
 		
 		for (PortalPlayer pPlayer : m_players) {
 			if (pPlayer.equals(player))
 				return pPlayer;
 		}
 		
 		PortalPlayer pPlayer = new PortalPlayer(player);
 		m_players.add(pPlayer);
 		
 		return pPlayer;
 		
 	}
 
 	/**
 	 * Gets the multiverse world manager.
 	 *
 	 * @return the multiverse world manager
 	 */
 	public static MVWorldManager getMVWorldManager() {
 		return m_multiversePortals.getCore().getMVWorldManager();
 	}
 
 	/**
 	 * Gets the portal manager.
 	 *
 	 * @return the portal manager
 	 */
 	public static PortalManager getPortalManager() {
 		return m_multiversePortals.getPortalManager();
 	}
 
 }
