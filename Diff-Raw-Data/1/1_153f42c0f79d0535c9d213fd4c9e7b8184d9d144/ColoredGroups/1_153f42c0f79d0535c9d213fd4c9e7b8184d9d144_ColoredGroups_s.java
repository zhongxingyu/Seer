 package net.milkycraft;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import net.krinsoft.privileges.Privileges;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ColoredGroups extends JavaPlugin implements Listener {
 
 	private Privileges priv;
 	protected ConfigSettings conf;
 	protected Set<ChatProfile> profiles = new HashSet<ChatProfile>();
 
 	@Override
 	public void onEnable() {
 		hook();
 		conf = new ConfigSettings(this, "config.yml");
 		conf.load();
 		getServer().getPluginManager().registerEvents(this, this);
 		log("Loaded " + profiles.size() + " groups from config");
 	}
 
 	@Override
 	public void onDisable() {
 		profiles.clear();
 		profiles = null;
 	}
 
 	private void hook() {
 		Plugin perms = this.getServer().getPluginManager()
 				.getPlugin("Privileges");
 		if (perms != null && perms.isEnabled()) {
 			this.priv = (Privileges) perms;
 			log("Found " + perms.getDescription().getName() + " v"
 					+ perms.getDescription().getVersion() + " by "
 					+ perms.getDescription().getAuthors().get(0));
 		} else {
 			log("Could not hook into privileges! Disabling...");
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onChat(AsyncPlayerChatEvent e) {
 		final String group = getGroup(e.getPlayer());
 		for (ChatProfile c : profiles) {
 			if (c.getGroup().equalsIgnoreCase(group)) {
 				e.setFormat(c.getFormat());
 				break;
 			}
 		}
 		if (e.getFormat() == "<%1$s> %2$s") {
 			e.setFormat(ChatColor.GRAY + "[" + getGroup(e.getPlayer()) + "]"
 					+ "%s:" + ChatColor.WHITE + " %s");
 		}
 	}
 
 	/*
 	 * Below are Internally and possible externally accessed methods
 	 */
 
 	@SuppressWarnings("deprecation")
 	public String getGroup(Player player) {
 		return this.priv.getGroupManager().getGroup(player).getName();
 	}
 
 	public void log(String log) {
 		this.getLogger().info(log);
 	}
 
 	public void reload() {
 		profiles.clear();
 		conf.reload();
 	}
 
 	public Privileges getPrivileges() {
 		return priv;
 	}
 
 	public Set<ChatProfile> getChatProfiles() {
 		return this.profiles;
 	}
 
 }
