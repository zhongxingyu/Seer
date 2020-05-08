 package me.menexia.dynafish;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DynaFish extends JavaPlugin {
 	public static DynaFish plugin;
 	
 	public final Logger logger = Logger.getLogger("Minecraft");
 	private final DFEntityListener entityListener = new DFEntityListener(this);
 	public Set<Player> user = new HashSet<Player>();
 	// HashSet is a command tracker.
 
 @Override
 public void onDisable() {
 	this.logger.info("DynaFish disabled!");
 }
 
 @Override
 public void onEnable() {
 PluginManager pm = this.getServer().getPluginManager();
 pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Monitor, this);
 pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Monitor, this);
 PluginDescriptionFile pdf = this.getDescription();
 this.logger.info( pdf.getName() + " version " + pdf.getVersion() + " by MeneXia is enabled!" );
 }
 
 public boolean hasUser(Player player) {
 	return user.contains(player);
 }
 
 public void setUser(Player player, boolean enabled) {
 	if (enabled) {
 		user.add(player);
		player.sendMessage(ChatColor.RED + "[DynaFish]" + ChatColor.WHITE + " enabled.");
 	} else {
 		user.remove(player);
		player.sendMessage(ChatColor.RED + "[DynaFish]" + ChatColor.WHITE + " disabled.");
 	}
 }
 
 public boolean onCommand(CommandSender sender, Command cmd, String zhf, String[] args) {
 		if (!(sender instanceof Player)) {
			return false;
 			}
 		if(sender.hasPermission("dynafish.use")) {
 			if (zhf.equalsIgnoreCase("dynafish") || zhf.equalsIgnoreCase("df")) {
 				if (sender instanceof Player) {
 				Player player = (Player)sender;
 				setUser(player, !hasUser(player));
 					}
 				}
			}
 		return true;
 		}
 
 }
