 package uk.co.oliwali.WhoAreYou;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WhoAreYou extends JavaPlugin {
 	
 	public static String name;
 	public static String version;
 	private Permission permissions;
 	public Config config;
 	private WAYPlayerListener playerListener = new WAYPlayerListener(this);
 
 	public void onDisable() {
 		Util.info("Version " + version + " disabled!");
 	}
 
 	public void onEnable() {
 		name = this.getDescription().getName();
         version = this.getDescription().getVersion();
         config = new Config(this);
         permissions = new Permission(this);
         
         // Register events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
         
         Util.info("Version " + version + " enabled!");
 	}
 	
 	private void sendPlayerList(Player sender, String message, List<Player> players) {
 		for (Player player : players.toArray(new Player[0]))
 			message = message + " " + permissions.getPrefix(player) + player.getName();
 		Util.sendMessage(sender, message);
 	}
 	
 	public void who(Player player) {
 		List<Player> players = new ArrayList<Player>();
 		for (World world : getServer().getWorlds().toArray(new World[0]))
 			players.addAll(world.getPlayers());
 		sendPlayerList(player, "&aServer player list &7(" + players.size() + "/" + getServer().getMaxPlayers() + ")&a:&f", players);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
 		
 		String prefix = cmd.getName();
 		Player player = (Player) sender;
 		
 		if (prefix.equalsIgnoreCase("who")) {
 			if (args.length > 0) {
 				String name = args[0];
 				if (permissions.world(player)) {
 					for (World world : getServer().getWorlds().toArray(new World[0])) {
 						if (name.equalsIgnoreCase(config.getAliasFromWorld(world))) {
 							sendPlayerList(player, "&aPlayer list for &c" + world.getName() + " &7(" + world.getPlayers().size() + "/" + getServer().getMaxPlayers() + ")&a:&f", world.getPlayers());
 							return true;
 						}
 					}
 					if (!permissions.player(player))
 						Util.sendMessage(player, "&cNo worlds found that match &7" + name);
 				}
 				if (permissions.player(player)) {
 					List<Player> matchPlayers = getServer().matchPlayer(name);
 					if (matchPlayers.size() == 1) {
 						Player playerInfo = matchPlayers.get(0);
 						Location loc = Util.getSimpleLocation(playerInfo.getLocation());
						Util.sendMessage(player, "&a------------ &7Who &a-------------");
 						Util.sendMessage(player, "&aPlayer: &7" + playerInfo.getName());
 						Util.sendMessage(player, "&aIP: &7" + playerInfo.getAddress().getAddress().getHostAddress().toString());
 						Util.sendMessage(player, "&aLocation: &7" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
 						Util.sendMessage(player, "&aWorld: &7" + config.getAliasFromWorld(playerInfo.getWorld()));
 						Util.sendMessage(player, "&aHealth: &7" + playerInfo.getHealth() + "/20");
 						Util.sendMessage(player, "&aGroup: &7" + permissions.getPrefix(playerInfo) + permissions.getGroup(playerInfo));
 						Util.sendMessage(player, "&aOp: &7" + (playerInfo.isOp()?"yes":"no"));
 						return true;
 					}
 					if (!permissions.world(player))
 						Util.sendMessage(player, "&cNo unique players found that match &7" + name);
 					else
 						Util.sendMessage(player, "&cNo unique players or worlds found that match &7" + name);
 				}
 			}
 			else if (permissions.list(player)) {
 				who(player);
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
