 package net.othercraft.steelsecurity.listeners;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import net.othercraft.steelsecurity.Main;
 import net.othercraft.steelsecurity.utils.SSCmdExe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class SpectateManager extends SSCmdExe {
 
 	public SpectateManager(String name, Main plugin) {
 		super("SpectateManager", true);
 	}
 	static Map<String, Boolean> spectators = new HashMap<String, Boolean>();//if some is a spectating someone else.
 	static Map<String, Boolean> spectatees = new HashMap<String, Boolean>();//if someone is being spectated.
 	static Map<String, String> spectating = new HashMap<String, String>();//Who a player is spectating.
 	static Map<String, HashSet<String>> speclist = new HashMap<String, HashSet<String>>();//Who a player is being spectated by.
 	static Map<String, Location> origion = new HashMap<String, Location>();//Where a player was before beginning spectate
 	static HashSet<String> spectates = new HashSet<String>();//Who is specating other people
 
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		spectators.put(player.getName(), false);
 		spectatees.put(player.getName(), false);
 		speclist.put(player.getName(), new HashSet<String>());
 	}
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		if (spectatees.get(player.getName())){
 			HashSet<String> tostops = speclist.get(player.getName());
 			for (String tostop : tostops) {
 				stop(Bukkit.getPlayerExact(tostop));
 				Bukkit.getPlayerExact(tostop).sendMessage("Spectating ended because player logged off.");
 			}
 		}
 		speclist.remove(player);
 		spectators.remove(player);
 		spectatees.remove(player);
 	}
 	private static void start(Player tostart, Player tostarton) {
 		spectates.add(tostart.getName());
 		spectators.put(tostart.getName(), true);
 		spectatees.put(tostarton.getName(), true);
 		spectating.put(tostart.getName(), tostarton.getName());
 		origion.put(tostart.getName(), tostart.getLocation());
 		HashSet<String> thenew = speclist.get(tostarton.getName());
 		thenew.add(tostart.getName());
 		speclist.put(tostarton.getName(), thenew);
 		for (Player player : Bukkit.getOnlinePlayers()) {
 			player.hidePlayer(tostart);
 		}
 		tostart.hidePlayer(tostarton);
		System.out.println(tostarton);
 	}
 	private static void stop(Player tostop) {
 		spectates.remove(tostop);
 		Player tostopon = Bukkit.getPlayerExact(spectating.get(tostop.getName()));
 		spectators.put(tostop.getName(), false);
 		for (Player player : Bukkit.getOnlinePlayers()) {
 			player.showPlayer(tostop);
 		}
		tostop.showPlayer(tostopon);
 		spectating.remove(tostop);
 		HashSet<String> thenew = speclist.get(tostopon.getName());
 		thenew.remove(tostop);
 		speclist.put(tostopon.getName(), thenew);
 	}
 	public static void stopAll() {
 		for (String player : spectates) {
 			stop(Bukkit.getPlayerExact(player));
 		}
 	}
 	public static void specCmd(CommandSender sender, String[] args) {
 		Player player = Bukkit.getPlayerExact(sender.getName());
 		if (!(args.length>2)){
			if (spectators.get(player.getName())) {
 				stop(player);
 			}
 			if (args.length==2) {
 				start(player, Bukkit.getPlayer(args[1]));
 			}
 		}
 		else {
 			player.sendMessage("Too many arguments!");
 			player.sendMessage("Usage: /sts spectate <player>");
 		}
 	}
 	public static void registerAll() {
 		for (Player player : Bukkit.getOnlinePlayers()){
 			spectators.put(player.getName(), false);
 			spectatees.put(player.getName(), false);
 			speclist.put(player.getName(), new HashSet<String>());
 		}
 	}
 }
