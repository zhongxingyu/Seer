 package REALDrummer;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.World;
 
 public class myUltraWarps extends JavaPlugin implements Listener {
 	public static Server server;
 	public static ConsoleCommandSender console;
 	private static final String[] enable_messages = { "Scotty can now beam you up.", "The warps have entered the building.",
 			"These ARE the warps you're looking for.", "May the warps be with you.", "Let's rock these warps.", "Warp! Warp! Warp! Warp! Warp! Warp!",
 			"What warp through yonder server breaks?", "Wanna see me warp to that mountain and back?\nWanna see me do it again?",
 			"/jump is much less lethal now!", "/top used to take people above the Nether's ceiling!" }, disable_messages = { "Ta ta for now!", "See you soon!",
 			"I'll miss you!", "Don't forget me!", "Don't forget to write!", "Don't leave me here all alone!", "Hasta la vista, baby.", "Wait for me!" },
 			yeses = { "yes", "yeah", "yea", "yep", "sure", "why not", "okay", "do it", "fine", "whatever", "very well", "accept", "tpa", "cool", "hell yeah",
 					"hells yeah", "hells yes", "come" }, nos = { "no", "nah", "nope", "no thanks", "no don't", "hell no", "shut up", "ignore", "it's not",
 					"its not", "creeper", "unsafe", "wait", "one ", "1 " };
 	private static String[] parameters = new String[0], color_color_code_chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
 			"f" }, formatting_color_code_chars = { "k", "l", "m", "n", "o", "r" };
 	// owner and name are important so that methods can access the name and owner specified by a command as extrapolated by locateWarp() even if locateWarp
 	// returns a null UltraWarp
 	private String owner, name;
 	// index is the index of the warp that locateWarp() found
 	private int index;
 	private static ArrayList<UltraWarp> warps = new ArrayList<UltraWarp>();
 	public static ArrayList<UltraSwitch> switches = new ArrayList<UltraSwitch>();
 	// default settings: 0=boolean must request teleportation for /to, 1=boolean must request teleportation for /from, 2=String default warp message format,
 	// 3=String default no warp message format, 4=int maximum number of warps, 5=int cool down time, 6=int warp history length, 7=int death history length
 	private static Object[] default_settings = new Object[8];
 	private ArrayList<Object[]> help_pages = new ArrayList<Object[]>();
 	private static boolean use_group_settings = true, autosave_warps = false, autosave_switches = false, autosave_config = true, auto_update = true;
 	private boolean parsing_warp_message = false, parsing_no_warp_message = false;
 	private static HashMap<String, Object[]> group_settings = new HashMap<String, Object[]>(), per_player_settings = new HashMap<String, Object[]>();
 	private static HashMap<World, String> spawn_messages_by_world = new HashMap<World, String>();
 	private static HashMap<String, Boolean> full_list_organization_by_user = new HashMap<String, Boolean>();
 	// [...]_teleport_requests = HashMap<player who would be teleported, ArrayList<player(s) who sent the request(s)>>
 	private static HashMap<String, ArrayList<String>> info_messages_for_players = new HashMap<String, ArrayList<String>>(),
 			to_teleport_requests = new HashMap<String, ArrayList<String>>(), from_teleport_requests = new HashMap<String, ArrayList<String>>(),
 			blocked_players = new HashMap<String, ArrayList<String>>(), trusted_players = new HashMap<String, ArrayList<String>>();
 	private static HashMap<String, ArrayList<UltraWarp>> warp_histories = new HashMap<String, ArrayList<UltraWarp>>();
 	private static HashMap<String, ArrayList<Location>> death_histories = new HashMap<String, ArrayList<Location>>();
 	private static HashMap<String, Integer> last_warp_indexes = new HashMap<String, Integer>(), last_warp_to_death_indexes = new HashMap<String, Integer>();
 	// cooling_down_players = HashMap<player's name, time in ms that they last warped>
 	private static HashMap<String, Long> cooling_down_players = new HashMap<String, Long>();
 	private static Plugin Vault = null;
 	private static Permission permissions = null;
 	private static Economy economy = null;
 
 	// TODO: make /warps near me comapre the number of blocks it would have to compare with the number of warps and determine which way to search would be
 	// easier. Then, either search by each block in the radius or just search through the warps list
 	// TODO: make /trust
 	// TODO: make anti-spam filters for /to and /from
 	// TODO: make on-login info messages rollover
 	// TODO: make /back and /fwd display "(warp [index]/[warp_history.size()])"
 	// TODO: if a player blocks a myUltraWarps admin while he's offline, unblock the admin when he logs on and inform the blocker
 	// TODO: make teleportation to your home on death configurable
 	// TODO: /send request system
 	// TODO: make messages informing of non-default characteristics in a warp in /create
 
 	// FIXED: error that made getFullName() not work properly and use the first username it found rather than the shortest match
 	// FIXED: error that made a NullPointerException when someone tried to name a warp an illegal name like "list"
 	// FIXED: error that made listing people while creating a warp not work
 
 	// plugin enable/disable and the command operator
 	public void onEnable() {
 		server = getServer();
 		console = server.getConsoleSender();
 		// register this class as a listener
 		server.getPluginManager().registerEvents(this, this);
 		// set up the warp_histories list for /back
 		for (Player player : server.getOnlinePlayers())
 			warp_histories.put(player.getName(), new ArrayList<UltraWarp>());
 		// load the saved data
 		loadTheConfig(console);
 		loadTheWarps(console);
 		loadTheSwitches(console);
 		loadTheTemporaryData();
 		if (auto_update)
 			checkForUpdates(console);
 		// load the help pages
 		for (int i = 0; i < 43; i++) {
 			Object[] help_line = new Object[4];
 			if (i == 0) {
 				help_line[0] =
 						"&a&o/create (\"warp\") [warp name] (settings) &fcreates a warp called \"[warp name]\". You can also use &a&o/make warp &for &a&o/set warp.&f\nFor the \"(settings)&f\" parameter, you can put in one or more of these settings to customize the warp:\n&a&otype:[type] &fallows you to decide whether the warp private (restricted and unlisted), secret (unlisted but unrestricted), advertised (listed but restricted), or open (listed and unrestricted).";
 				help_line[1] = "myultrawarps.create";
 				help_line[2] = true;
 				help_line[3] = 10;
 			} else if (i == 1) {
 				help_line[0] = "&a&ogiveto:[player] &fallows you to give the warp to another player.";
 				help_line[1] = "myultrawarps.create";
 				help_line[2] = true;
 				help_line[3] = 1;
 			} else if (i == 2) {
 				help_line[0] = "&f&oYou &fcan give warps to other players even if they already have a warp with the same name.";
 				help_line[1] = "myultrawarps.create.other";
 				help_line[2] = false;
 				help_line[3] = 2;
 			} else if (i == 3) {
 				help_line[0] =
 						"&a&owarp:[message] &fallows you to customize the message that appears when someone warps to your warp. The message can be as long as you like and may have spaces and you can use color codes! I love colors.\n&a&onowarp:[message] &fallows you to customize the message that appears when someone tries to warp to your warp, but is not allowed to.";
 				help_line[1] = "myultrawarps.create";
 				help_line[2] = true;
 				help_line[3] = 7;
 			} else if (i == 4) {
 				help_line[0] =
 						"&a&olist:[player1],[player2] &fallows you to add players to the warp's list. The warp's list works both as a blacklist for unrestricted warps and as a whitelist for restricted warps. You may list as many people as you want at once by separating usernames with commas and no spaces.\n&a&ounlist:[player1],[player2] &fallows you to remove people from the warp's list.";
 				help_line[1] = "myultrawarps.create";
 				help_line[2] = true;
 				help_line[3] = 7;
 			} else if (i == 5) {
 				help_line[0] = "&a&o/warp (owner\"'s\") [warp name] &fwarps you to the specified warp.";
 				help_line[1] = "myultrawarps.warptowarp";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 6) {
 				help_line[0] = "&f&oYou &fcan warp to other people's warps, too--even restricted ones.";
 				help_line[1] = "myultrawarps.warptowarp.other";
 				help_line[2] = false;
 				help_line[3] = 2;
 			} else if (i == 7) {
 				help_line[0] =
 						"&a&o/warp (world) [x] [y] [z] (world) &fwarps you to the specified coordinates in (world). You do not need to type the world name both before and after the coordinates; one or the other will do. If (world) is left blank, you will warp to those coordinates in your current world.";
 				help_line[1] = "myultrawarps.warptocoord";
 				help_line[2] = true;
 				help_line[3] = 5;
 			} else if (i == 8) {
 				help_line[0] =
 						"&a&o/change (\"warp\") (owner\"'s\") [warp name] [settings] &fchanges the settings of an existing warp. The settings are the same as the ones for &a&o/create&f, but you can also use &a&oname:[new name] &fto change the name of a warp. You can also use &a&o/modify&f.";
 				help_line[1] = "myultrawarps.change";
 				help_line[2] = true;
 				help_line[3] = 4;
 			} else if (i == 9) {
 				help_line[0] = "&f&oYou &fcan also change other players' warps.";
 				help_line[1] = "myultrawarps.change.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 10) {
 				help_line[0] =
 						"&a&o/move (\"warp\") (owner\"'s\") [warp name] &fmoves the warp to your current location. You can also use &a&o/translate warp&f.";
 				help_line[1] = "myultrawarps.move";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 11) {
 				help_line[0] = "&f&oYou &fcan also move other players' warps.";
 				help_line[1] = "myultrawarps.move.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 12) {
 				help_line[0] = "&a&o/delete (\"warp\") (owner\"'s\") [warp name] &fdeletes the specified warp. You can also use &a&o/remove warp&f.";
 				help_line[1] = "myultrawarps.delete";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 13) {
 				help_line[0] = "&f&oYou &fcan also delete other players' warps.";
 				help_line[1] = "myultrawarps.move.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 14) {
 				help_line[0] =
 						"&a&o/warp(\"s\") list &fdisplays all of your warps and all listed warps with color coding. White warps are open, red warps are advertised, gray warps are secret, and dark red warps are private.";
 				help_line[1] = "myultrawarps.list";
 				help_line[2] = true;
 				help_line[3] = 4;
 			} else if (i == 15) {
 				help_line[0] =
 						"&a&o/full warp list (\"page\" [#]) (\"by owner\"/\"by name\") (\"owner:\"[owner]) (\"type:\"[type]) &flists all of the warps for the entire server whether they're listed or not. You can use the parameters above to go page by page, organize the list by owner or by name, or create filters on your search. You can also use &a&o/entire warp list &for &a&o/complete warp list &fand you can put an \"s\" at the end of \"warp\" if you like.";
 				help_line[1] = "myultrawarps.list.full";
 				help_line[2] = false;
 				help_line[3] = 7;
 			} else if (i == 16) {
 				help_line[0] = "&a&o/warp info (owner\"'s\") [warp name] &fdisplays all the information about the specified warp.";
 				help_line[1] = "myultrawarps.warpinfo";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 17) {
 				help_line[0] = "&f&oYou &fcan also see information on other players' warps.";
 				help_line[1] = "myultrawarps.warpinfo.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 18) {
 				help_line[0] = "&a&o/back &fwarps you back to the last place you warped. You can also use &a&o/return &for &a&o/last&f.";
 				help_line[1] = "myultrawarps.back";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 19) {
 				help_line[0] = "&a&o/set home &fcreates a special unlisted, restricted warp called \"home\" with special default messages.";
 				help_line[1] = "myultrawarps.sethome";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 20) {
 				help_line[0] = "&f&oYou &fcan also add the parameter (owner\"'s\") to set other players' home warps.";
 				help_line[1] = "myultrawarps.sethome.other";
 				help_line[2] = false;
 				help_line[3] = 2;
 			} else if (i == 21) {
 				help_line[0] = "&a&o/home &fwarps you to your home.";
 				help_line[1] = "myultrawarps.home";
 				help_line[2] = true;
 				help_line[3] = 1;
 			} else if (i == 22) {
 				help_line[0] = "&f&oYou &fcan also add the parameter (owner\"'s\") to warp to other players' homes.";
 				help_line[1] = "myultrawarps.home.other";
 				help_line[2] = false;
 				help_line[3] = 2;
 			} else if (i == 23) {
 				help_line[0] = "&a&o/set spawn &fsets the spawn point for the world you're in.";
 				help_line[1] = "myultrawarps.admin";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 24) {
 				help_line[0] = "&a&o/spawn &fteleports you to your world's spawn point.";
 				help_line[1] = "myultrawarps.spawn";
 				help_line[2] = true;
 				help_line[3] = 1;
 			} else if (i == 25) {
 				help_line[0] = "&a&o/jump &fteleports you to the spot you're pointing at.";
 				help_line[1] = "myultrawarps.jump";
 				help_line[2] = true;
 				help_line[3] = 1;
 			} else if (i == 26) {
 				help_line[0] = "&a&o/top &fteleports you to the highest solid block directly above or below you.";
 				help_line[1] = "myultrawarps.top";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 27) {
 				help_line[0] =
 						"&a&o/link (owner\"'s\") [warp name] (settings) &flinks a warp to a button, lever, or pressure plate that you are pointing at. Once a warp is linked to one of these switches, right-clicking that button, lever, or sign or stepping on the pressure plate will warp you to the warp that the switch is linked to.";
 				help_line[1] = "myultrawarps.link";
 				help_line[2] = true;
 				help_line[3] = 5;
 			} else if (i == 28) {
 				help_line[0] = "&f&oYou &fcan also link other players' warps to your switches.";
 				help_line[1] = "myultrawarps.link.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 29) {
 				help_line[0] =
 						"&a&o/unlink (owner\"'s\") (warp name) &funlinks a warp from a button, pressure plate, sign, or lever that you are pointing at or unlinks all switches from the specified warp if a warp is specified.";
 				help_line[1] = "myultrawarps.unlink";
 				help_line[2] = true;
 				help_line[3] = 4;
 			} else if (i == 30) {
 				help_line[0] = "&f&oYou &fcan also unlink other players' switches.";
 				help_line[1] = "myultrawarps.unlink.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 31) {
 				help_line[0] =
 						"&a&o/switch(\"es\") list &flists all your switches by warp that they're linked to and how many switches are linked to each warp.";
 				help_line[1] = "myultrawarps.list";
 				help_line[2] = true;
 				help_line[3] = 2;
 			} else if (i == 32) {
 				help_line[0] =
 						"&a&o/switch info (owner\"'s\") (warp name) &fdisplays the information on the switch that you are pointing at or displays the information on all the switches that are linked to the specified warp if a warp is specified.";
 				help_line[1] = "myultrawarps.switchinfo";
 				help_line[2] = true;
 				help_line[3] = 4;
 			} else if (i == 33) {
 				help_line[0] = "&f&oYou &fcan also see information on other players' switches.";
 				help_line[1] = "myultrawarps.switchinfo.other";
 				help_line[2] = false;
 				help_line[3] = 1;
 			} else if (i == 34) {
 				help_line[0] =
 						"&a&o/to [player] &fteleports you to the designated player. If the admins configure it so that you have to ask the person if you can teleport to them before doing so, &a&o/to &fwill ask them if it's okay. The target player can then just type their answer into the chat box. It's that easy. You can also use &a&o/find&f.";
 				help_line[1] = "myultrawarps.to";
 				help_line[2] = true;
 				help_line[3] = 5;
 			} else if (i == 35) {
 				help_line[0] =
 						"&a&o/from [player] &fforcibly teleports the designated player to you. You can also use &a&o/pull&f, &a&o/yank&f, &a&o/bring&f, or &a&o/get&f.";
 				help_line[1] = "myultrawarps.from";
 				help_line[2] = false;
 				help_line[3] = 2;
 			} else if (i == 36) {
 				help_line[0] =
 						"&a&o/warp all (\"to\") [\"here\"/\"there\"/\"warp\" [warp]/\"player\" [player]] &fwarps everyone on the server to your current location, the spot you're pointing at, or the designated warp or player.";
 				help_line[1] = "myultrawarps.warpall";
 				help_line[2] = false;
 				help_line[3] = 4;
 			} else if (i == 37) {
 				help_line[0] =
 						"&a&o/send [player] (\"to\") [\"there\"/\"warp\" [warp]/\"player\" [player]] &fwarps the designated player to the spot you're pointing at or the designated warp or player.";
 				help_line[1] = "myultrawarps.send";
 				help_line[2] = false;
 				help_line[3] = 3;
 			} else if (i == 38) {
 				help_line[0] =
 						"&a&o/warps [\"around\"/\"near\"] [\"here\"/\"there\"/\"warp\" [warp]/\"player\" [player]] (search radius) &flists all the warps within the search radius of the designated warp, player, or other specified location. By default, the search radius is 20 blocks.";
 				help_line[1] = "myultrawarps.warpsaround";
 				help_line[2] = true;
 				help_line[3] = 5;
 			} else if (i == 39) {
 				help_line[0] =
 						"&a&o/default [\"warp\"/\"no warp\"] (\"message\") (\"for\" [player]/\"group:\"[group]/\"server\") [message] &fchanges the default warp or no warp message for a player, a group, or the entire server.";
 				help_line[1] = "myultrawarps.config";
 				help_line[2] = true;
 				help_line[3] = 4;
 			} else if (i == 40) {
 				help_line[0] =
 						"&a&o/max warps(\"for\" [player]/\"group:\"[group]/\"server\") [max warps] &fallows admins to change the maximum number of warps that a player, a group, or the entire server can make.";
 				help_line[1] = "myultrawarps.admin";
 				help_line[2] = false;
 				help_line[3] = 3;
 			} else if (i == 41) {
 				help_line[0] =
 						"&a&o/myUltraWarps load (\"the\") [\"warps\"/\"switches\"/\"config\"] &freloads all the data on the server for the warps, switches, or configurations straight from the warps.txt, switches.txt, or config.txt file and formats the file. You can also use &a&o/mUW&f.";
 				help_line[1] = "myultrawarps.admin";
 				help_line[2] = false;
 				help_line[3] = 4;
 			} else if (i == 42) {
 				help_line[0] =
 						"&a&o/myUltraWarps save (\"the\") [\"warps\"/\"switches\"/\"config\"] &fsaves all the data on the server and updates and formats the warps.txt, switches.txt, or config.txt file. You can also use &a&o/mUW&f.";
 				help_line[1] = "myultrawarps.admin";
 				help_line[2] = false;
 				help_line[3] = 4;
 			}
 			help_pages.add(help_line);
 		}
 		// done enabling
 		String enable_message = enable_messages[(int) (Math.random() * enable_messages.length)];
 		console.sendMessage(ChatColor.GREEN + enable_message);
 		for (Player player : server.getOnlinePlayers())
 			if (player.hasPermission("myultrawarps.admin"))
 				player.sendMessage(ChatColor.GREEN + enable_message);
 	}
 
 	public void onDisable() {
 		// forcibly enable the permissions plugin
 		if (permissions != null) {
 			Plugin permissions_plugin = server.getPluginManager().getPlugin(permissions.getName());
 			if (permissions_plugin != null && !permissions_plugin.isEnabled())
 				server.getPluginManager().enablePlugin(permissions_plugin);
 		}
 		saveTheWarps(console, true);
 		saveTheSwitches(console, true);
 		saveTheConfig(console, true);
 		saveTheTemporaryData();
 		// done disabling
 		String disable_message = disable_messages[(int) (Math.random() * disable_messages.length)];
 		console.sendMessage(ChatColor.GREEN + disable_message);
 		for (Player player : server.getOnlinePlayers())
 			if (player.hasPermission("myultrawarps.admin"))
 				player.sendMessage(ChatColor.GREEN + disable_message);
 		// forcibly disable the permissions plugin
 		if (permissions != null) {
 			Plugin permissions_plugin = server.getPluginManager().getPlugin(permissions.getName());
 			if (permissions_plugin != null && permissions_plugin.isEnabled())
 				server.getPluginManager().disablePlugin(permissions_plugin);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] my_parameters) {
 		parameters = my_parameters;
 		boolean success = false;
 		if (command.equalsIgnoreCase("setspawn") || (command.equalsIgnoreCase("set") && parameters.length > 0 && parameters[0].equalsIgnoreCase("spawn"))) {
 			success = true;
 			if (sender instanceof Player && sender.hasPermission("myultrawarps.admin"))
 				setSpawn(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You can't decide where the spawn point goes. You can't point it out to me. Sorry.");
 			else {
 				if (command.equalsIgnoreCase("set") && parameters.length > 0 && parameters[0].equalsIgnoreCase("spawn"))
 					sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/set spawn" + ChatColor.RED + ".");
 				else
 					sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/setspawn" + ChatColor.RED + ".");
 			}
 		} else if (command.equalsIgnoreCase("sethome") || (command.equalsIgnoreCase("set") && parameters.length > 0 && parameters[0].equalsIgnoreCase("home"))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.sethome") || sender.hasPermission("myultrawarps.sethome.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				setHome(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You can't have a home! YOU...ARE...A...CONSOLE!");
 			else if (parameters.length == 0 || !parameters[0].equalsIgnoreCase("home"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/sethome" + ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/set home" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("warplist") || command.equalsIgnoreCase("warpslist"))
 				|| ((command.equalsIgnoreCase("warp") || command.equalsIgnoreCase("warps")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("list"))) {
 			success = true;
 			if (!(sender instanceof Player)
 					|| (sender.hasPermission("myultrawarps.list") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				warpList(sender);
 			else if (parameters.length == 0 || !parameters[0].equalsIgnoreCase("list"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " list"
 						+ ChatColor.RED + ".");
 		} else if (((command.equalsIgnoreCase("full") || command.equalsIgnoreCase("entire") || command.equalsIgnoreCase("complete")) && parameters.length > 1
 				&& (parameters[0].equalsIgnoreCase("warp") || parameters[0].equalsIgnoreCase("warps")) && parameters[1].equalsIgnoreCase("list"))
 				|| (command.equalsIgnoreCase("fullwarplist") || command.equalsIgnoreCase("entirewarplist") || command.equalsIgnoreCase("completewarplist")
 						|| command.equalsIgnoreCase("fullwarpslist") || command.equalsIgnoreCase("entirewarpslist") || command
 							.equalsIgnoreCase("completewarpslist"))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.list.full") || sender.hasPermission("myultrawarps.admin"))
 				fullWarpList(sender);
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ " warps list" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("switchlist") || command.equalsIgnoreCase("switcheslist"))
 				|| ((command.equalsIgnoreCase("switch") || command.equalsIgnoreCase("switches")) && parameters.length > 0 && parameters[0]
 						.equalsIgnoreCase("list"))) {
 			success = true;
 			if (!(sender instanceof Player)
 					|| (sender.hasPermission("myultrawarps.list") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				switchList(sender);
 			else if (parameters.length == 0 || !parameters[0].equalsIgnoreCase("list"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " list"
 						+ ChatColor.RED + ".");
 		} else if (((command.equalsIgnoreCase("full") || command.equalsIgnoreCase("entire") || command.equalsIgnoreCase("complete")) && parameters.length > 1
 				&& (parameters[0].equalsIgnoreCase("switch") || parameters[0].equalsIgnoreCase("switches")) && parameters[1].equalsIgnoreCase("list"))
 				|| (command.equalsIgnoreCase("fullswitchlist") || command.equalsIgnoreCase("entireswitchlist")
 						|| command.equalsIgnoreCase("completeswitchlist") || command.equalsIgnoreCase("fullswitcheslist")
 						|| command.equalsIgnoreCase("entireswitcheslist") || command.equalsIgnoreCase("completeswitcheslist"))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.list.full") || sender.hasPermission("myultrawarps.admin"))
 				fullSwitchList(sender);
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ " switches list" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("spawn")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.spawn") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				spawn(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You cannot warp! Stop trying to warp! You have no body! Stop trying to warp!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/spawn" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("createwarp")
 				|| command.equalsIgnoreCase("makewarp")
 				|| command.equalsIgnoreCase("setwarp")
 				|| ((command.equalsIgnoreCase("create") || command.equalsIgnoreCase("make") || command.equalsIgnoreCase("set")) && (parameters.length == 0 || !parameters[0]
 						.equalsIgnoreCase("warp")))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.create") || sender.hasPermission("myultrawarps.create.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")) && parameters.length > 0)
 				createWarp(0, sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "Silly console! You can't make a warp! You have no body! :P");
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want to name the warp!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("create") || command.equalsIgnoreCase("make") || command.equalsIgnoreCase("set")) && parameters.length > 0
 				&& parameters[0].equalsIgnoreCase("warp")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.create") || sender.hasPermission("myultrawarps.create.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")) && parameters.length > 1)
 				createWarp(1, sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "Silly console! You can't make a warp! You have no body! :P");
 			else if (parameters.length <= 1)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want to name the warp!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " warp"
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("warpinfo")) {
 			success = true;
 			if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.warpinfo") || sender.hasPermission("myultrawarps.warpinfo.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 					&& parameters.length > 0)
 				warpInfo(0, sender);
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You need to tell me the name of the warp you want info on!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/warpinfo" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("warp") && parameters.length > 0 && parameters[0].equalsIgnoreCase("info")) {
 			success = true;
 			if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.warpinfo") || sender.hasPermission("myultrawarps.warpinfo.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 					&& parameters.length > 1)
 				warpInfo(1, sender);
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You need to tell me the name of the warp you want info on!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/warp info" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("warpall")) {
 			success = true;
 			if (parameters.length > 0
 					&& (!(sender instanceof Player) || sender.hasPermission("myultrawarps.warpall") || sender.hasPermission("myultrawarps.admin")))
 				warpAll(0, sender);
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want all the players warped!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/warpall" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("warp") && parameters.length > 0 && parameters[0].equalsIgnoreCase("all")) {
 			success = true;
 			if (parameters.length > 1
 					&& (!(sender instanceof Player) || sender.hasPermission("myultrrawarps.warpall") || sender.hasPermission("myultrawarps.admin")))
 				warpAll(1, sender);
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want all the players warped!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/warpall" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("warp")) {
 			success = true;
 			if (parameters.length == 0) {
 				if (sender instanceof Player)
 					sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp you want to warp to!");
 				else
 					console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
 			} else if (parameters.length < 3) {
 				if (sender instanceof Player
 						&& (sender.hasPermission("myultrawarps.warptowarp") || sender.hasPermission("myultrawarps.warptowarp.other")
 								|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 					warp(sender);
 				else if (!(sender instanceof Player))
 					console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
 				else
 					sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to warp to preset warps.");
 			} else {
 				if (sender instanceof Player
 						&& (sender.hasPermission("myultrawarps.warptocoord") || sender.hasPermission("myultrawarps.user") || sender
 								.hasPermission("myultrawarps.admin")))
 					warpToCoordinate(sender);
 				else if (!(sender instanceof Player))
 					console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
 				else
 					sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to warp to specific coordinates.");
 			}
 		} else if (command.equalsIgnoreCase("warptocoord")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.warptocoord") || sender.hasPermission("myultrawarps.user") || sender
 							.hasPermission("myultrawarps.admin")) && parameters.length >= 3)
 				warpToCoordinate(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
 			else if (parameters.length < 3)
 				sender.sendMessage("You forgot to tell me where you want to warp to!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to warp to specific coordinates.");
 		} else if (command.equalsIgnoreCase("default")
 				&& parameters.length > 0
 				&& (parameters[0].equalsIgnoreCase("warp") || parameters[0].toLowerCase().startsWith("warp:") || parameters[0].equalsIgnoreCase("nowarp")
 						|| parameters[0].toLowerCase().startsWith("nowarp:") || (parameters.length > 1 && parameters[0].equalsIgnoreCase("no") && (parameters[1]
 						.equalsIgnoreCase("warp") || parameters[1].toLowerCase().startsWith("warp:"))))) {
 			success = true;
 			int extra_param = 1;
 			if (parameters[0].equalsIgnoreCase("no"))
 				extra_param++;
 			if (parameters.length > extra_param && parameters[extra_param].equalsIgnoreCase("message"))
 				extra_param++;
 			if (parameters.length >= extra_param
 					&& (!(sender instanceof Player) || sender.hasPermission("myultrawarps.config") || sender.hasPermission("myultrawarps.user") || sender
 							.hasPermission("myultrawarps.admin")))
 				changeDefaultMessage(extra_param, sender);
 			else if (sender instanceof Player && !sender.hasPermission("myultrawarps.config") && !sender.hasPermission("myultrawarps.user")
 					&& !sender.hasPermission("myultrawarps.admin"))
 				if (parameters[0].equalsIgnoreCase("no"))
 					sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + ChatColor.GREEN + "/default no " + parameters[1].toLowerCase()
 							+ ChatColor.RED + ".");
 				else
 					sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + ChatColor.GREEN + "/default " + parameters[0].toLowerCase()
 							+ ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me the new default message!");
 
 		} else if (command.equalsIgnoreCase("changewarp")
 				|| command.equalsIgnoreCase("modifywarp")
 				|| ((command.equalsIgnoreCase("change") || command.equalsIgnoreCase("modify")) && (parameters.length == 0 || !parameters[0]
 						.equalsIgnoreCase("warp")))) {
 			success = true;
 			if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 1)
 				changeWarp(0, sender);
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You didn't tell me what warp to change or how to change it!");
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You didn't tell me what to change!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("change") || command.equalsIgnoreCase("modify")) && parameters.length > 0
 				&& parameters[0].equalsIgnoreCase("warp")) {
 			success = true;
 			if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 					&& parameters.length > 2)
 				changeWarp(1, sender);
 			else if (parameters.length < 2)
 				sender.sendMessage(ChatColor.RED + "You didn't tell me what warp to change or how to change it!");
 			else if (parameters.length == 2)
 				sender.sendMessage(ChatColor.RED + "You didn't tell me what to change!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " warp"
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("deletewarp")
 				|| command.equalsIgnoreCase("removewarp")
 				|| ((command.equalsIgnoreCase("delete") || command.equalsIgnoreCase("remove")) && (parameters.length == 0 || !parameters[0]
 						.equalsIgnoreCase("warp")))) {
 			success = true;
 			if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.delete") || sender.hasPermission("myultrawarps.delete.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 					&& parameters.length > 0)
 				deleteWarp(0, sender);
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp to delete!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("delete") || command.equalsIgnoreCase("remove")) && parameters.length > 0
 				&& parameters[0].equalsIgnoreCase("warp")) {
 			success = true;
 			if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.delete") || sender.hasPermission("myultrawarps.delete.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 					&& parameters.length > 1)
 				deleteWarp(1, sender);
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp to delete!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " warp"
 						+ ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("back") || command.equalsIgnoreCase("return") || command.equalsIgnoreCase("last"))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.back") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				back(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "How exactly can you go back to your last warp if you can't warp in the first place?");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("jump") || command.equalsIgnoreCase("j")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.jump") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				jump(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.GREEN + "You jumped! " + ChatColor.RED + "Just kidding. You're a console and you have no body.");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/jump" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("linkwarp")
 				|| (command.equalsIgnoreCase("link") && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.link") || sender.hasPermission("myultrawarps.link.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")) && parameters.length > 0)
 				linkWarp(0, sender);
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp I should use!");
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "Point out the switch you want to link \"" + parameters[0]
 						+ "\" to. Oh, wait. You can't. You're a console.");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("link") && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.link") || sender.hasPermission("myultrawarps.link.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")) && parameters.length > 1)
 				linkWarp(1, sender);
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp I should use!");
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "Point out the switch you want to link \"" + parameters[0]
 						+ "\" to. Oh, wait. You can't. You're a console.");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/link warp" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("unlinkwarp")
 				|| (command.equalsIgnoreCase("unlink") && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.unlink") || sender.hasPermission("myultrawarps.unlink.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 				unlinkWarp(0, sender);
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("unlink") && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.unlink") || sender.hasPermission("myultrawarps.unlink.other")
 					|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 				unlinkWarp(1, sender);
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/unlink warp" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("movewarp")
 				|| command.equalsIgnoreCase("translatewarp")
 				|| (((command.equalsIgnoreCase("move") || command.equalsIgnoreCase("translate"))) && (parameters.length == 0 || !parameters[0]
 						.equalsIgnoreCase("warp")))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")) && parameters.length > 0)
 				moveWarp(0, sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You can't move any warps because you can't point out a new location for the warp! You have no body!");
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me which warp to move!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("move") || command.equalsIgnoreCase("translate")) && parameters.length > 0
 				&& parameters[0].equalsIgnoreCase("warp")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")) && parameters.length > 1)
 				moveWarp(1, sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You can't move any warps because you can't point out a new location for the warp! You have no body!");
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me which warp to move!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " warp"
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("home") || command.equalsIgnoreCase("h")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.home") || sender.hasPermission("myultrawarps.home.other")
 							|| sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				home(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You can't have a home! YOU...ARE...A...CONSOLE!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/home" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
 				&& (parameters.length == 0 || (parameters.length > 0 && parameters[0].equalsIgnoreCase("help")))) {
 			success = true;
 			displayHelp(sender);
 		} else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
 				&& parameters.length > 1
 				&& parameters[0].equalsIgnoreCase("save")
 				&& (parameters[1].equalsIgnoreCase("warps") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
 						.equalsIgnoreCase("warps")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				saveTheWarps(sender, true);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps save" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW save" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
 				&& parameters.length > 1
 				&& parameters[0].equalsIgnoreCase("save")
 				&& (parameters[1].equalsIgnoreCase("switches") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
 						.equalsIgnoreCase("switches")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				saveTheSwitches(sender, true);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps save" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW save" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
 				&& parameters.length > 1
 				&& parameters[0].equalsIgnoreCase("save")
 				&& (parameters[1].equalsIgnoreCase("config") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
 						.equalsIgnoreCase("config")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				saveTheConfig(sender, true);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps save" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW save" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps")) && parameters.length == 1
 				&& parameters[0].equalsIgnoreCase("save")) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")) {
 				saveTheWarps(sender, true);
 				saveTheSwitches(sender, true);
 				saveTheConfig(sender, true);
 			} else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps save" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW save" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW"))
 				&& parameters.length > 1
 				&& parameters[0].equalsIgnoreCase("load")
 				&& (parameters[1].equalsIgnoreCase("warps") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
 						.equalsIgnoreCase("warps")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				loadTheWarps(sender);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps load" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW load" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW"))
 				&& parameters.length > 1
 				&& parameters[0].equals("load")
 				&& (parameters[1].equalsIgnoreCase("switches") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
 						.equalsIgnoreCase("switches")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				loadTheSwitches(sender);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps load" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW load" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW"))
 				&& parameters.length > 1
 				&& parameters[0].equalsIgnoreCase("load")
 				&& (parameters[1].equalsIgnoreCase("config") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
 						.equalsIgnoreCase("config")))) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				loadTheConfig(sender);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps load" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW load" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW")) && parameters.length == 1
 				&& parameters[0].equalsIgnoreCase("load")) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")) {
 				loadTheWarps(sender);
 				loadTheSwitches(sender);
 				loadTheConfig(sender);
 			} else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps load" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW load" + ChatColor.RED + ".");
 		} else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW")) && parameters.length >= 1
 				&& parameters[0].toLowerCase().startsWith("update")) {
 			success = true;
 			if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
 				checkForUpdates(sender);
 			else if (command.equalsIgnoreCase("myUltraWarps"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myUltraWarps update" + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mUW update" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps")) {
 			String[] new_parameters = new String[0];
 			if (parameters.length > 0) {
 				new_parameters = new String[parameters.length - 1];
 				for (int i = 1; i < parameters.length; i++)
 					new_parameters[i - 1] = parameters[i];
 			}
 			success = onCommand(sender, cmd, parameters[0], new_parameters);
 		} else if (command.equalsIgnoreCase("top") || command.equalsIgnoreCase("t")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.top") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				top(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "You don't have a body! Stop trying to warp!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/top" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("switchinfo")) {
 			success = true;
 			if (!(sender instanceof Player)
 					|| (sender.hasPermission("myultrawarps.switchinfo") || sender.hasPermission("myultrawarps.user") || sender
 							.hasPermission("myultrawarps.admin"))) {
 				switchInfo(0, sender);
 			} else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/switchinfo" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("switch") && parameters.length > 0 && parameters[0].equalsIgnoreCase("info")) {
 			success = true;
 			if (!(sender instanceof Player)
 					|| (sender.hasPermission("myultrawarps.switchinfo") || sender.hasPermission("myultrawarps.user") || sender
 							.hasPermission("myultrawarps.admin"))) {
 				switchInfo(1, sender);
 			} else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/switch info" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("to") || command.equalsIgnoreCase("find")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.to") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 0)
 				to(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "For the last time: You cannot warp! YOU HAVE NO BODY!");
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me who I should teleport you to!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("from") || command.equalsIgnoreCase("pull") || command.equalsIgnoreCase("yank")
 				|| command.equalsIgnoreCase("bring") || command.equalsIgnoreCase("get")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.from") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 0)
 				from(sender);
 			else if (!(sender instanceof Player))
 				console.sendMessage(ChatColor.RED + "No more trying to warp! It's not going to work! You're a CONSOLE!");
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me who I should teleport to you!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("send")) {
 			success = true;
 			if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.send") || sender.hasPermission("myultrawarps.admin"))
 					&& parameters.length >= 2)
 				send(sender);
 			else if (parameters.length == 0)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want me to send where!");
 			else if (parameters.length == 1)
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me where to send " + parameters[0] + "!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/send" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("warps") && parameters.length > 0
 				&& (parameters[0].equalsIgnoreCase("around") || parameters[0].equalsIgnoreCase("near"))) {
 			success = true;
 			if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.warpsaround") || sender.hasPermission("myultrawarps.user") || sender
 					.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 1)
 				warpsAround(1, sender, command);
 			else if (parameters.length > 1)
 				sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + ChatColor.GREEN + "/warps " + parameters[0].toLowerCase()
 						+ ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want the search to be centered!");
 		} else if (command.equalsIgnoreCase("warpsaround") || command.equalsIgnoreCase("warpsnear")) {
 			success = true;
 			if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.warpsaround") || sender.hasPermission("myultrawarps.user") || sender
 					.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 0)
 				warpsAround(0, sender, command);
 			else if (parameters.length > 0)
 				sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + ChatColor.GREEN + "/" + command.toLowerCase() + ChatColor.RED
 						+ ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want the search to be centered!");
 		} else if (command.equalsIgnoreCase("maxwarps") || command.equalsIgnoreCase("maximumwarps")) {
 			success = true;
 			if (parameters.length > 0 && (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")))
 				changeMaxWarps(0, sender);
 			else if (sender instanceof Player && !sender.hasPermission("myultrawarps.admin"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want me to change the max warps to!");
 		} else if ((command.equalsIgnoreCase("max") || command.equalsIgnoreCase("maximum")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("warps")) {
 			success = true;
 			if (parameters.length > 1 && (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")))
 				changeMaxWarps(1, sender);
 			else if (sender instanceof Player && !sender.hasPermission("myultrawarps.admin"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase() + " warps"
 						+ ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want me to change the max warps to!");
 		} else if (command.equalsIgnoreCase("forward") || command.equalsIgnoreCase("fwd")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.admin") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.back")))
 				forward(sender);
 			else if (!(sender instanceof Player))
 				sender.sendMessage(ChatColor.RED
 						+ "You're a console!! How can I warp you somewhere you've already warped if you can't warp at all in the first place?!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("deathfwd")
 				|| command.equals("fwddeath")
 				|| command.equalsIgnoreCase("dfwd")
 				|| command.equalsIgnoreCase("fwdd")
 				|| (command.equalsIgnoreCase("death") && parameters.length > 0 && (parameters[0].equalsIgnoreCase("fwd") || parameters[0]
 						.equalsIgnoreCase("forward")))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.admin") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.death")))
 				deathForward(sender);
 			else if (!(sender instanceof Player))
 				sender.sendMessage(ChatColor.RED
 						+ "You can't die, you can't warp, and you can't warp to your death location. How do you suppose I would move you forward in your death history?");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("death")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.death") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				death(sender);
 			else if (!(sender instanceof Player))
 				sender.sendMessage(ChatColor.RED + "You can't go back to your last death location! You can't warp! You can't even die!");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/" + command.toLowerCase()
 						+ ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("blocklist")
 				|| (command.equalsIgnoreCase("block") && parameters.length > 0 && parameters[0].equalsIgnoreCase("list"))) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.block") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
 				if (command.equalsIgnoreCase("blocklist"))
 					blockList(0, sender);
 				else
 					blockList(1, sender);
 			else if (!(sender instanceof Player))
 				sender.sendMessage(ChatColor.RED + "You can't even block people!");
 			else if (command.equalsIgnoreCase("block"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/block list" + ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/blocklist" + ChatColor.RED + ".");
 		} else if (command.equalsIgnoreCase("block")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.block") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 0)
 				block(sender);
 			else if (!(sender instanceof Player))
 				sender.sendMessage(ChatColor.RED + "You can't block anyone! People can't even send you requests in the first place!");
 			else if (parameters.length > 0)
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/block" + ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want to block!");
 		} else if (command.equalsIgnoreCase("unblock")) {
 			success = true;
 			if (sender instanceof Player
 					&& (sender.hasPermission("myultrawarps.block") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
 					&& parameters.length > 0)
 				unblock(sender);
 			else if (!(sender instanceof Player))
 				sender.sendMessage(ChatColor.RED + "Do I even need to explain why you can't use this command?");
 			else if (parameters.length > 0)
 				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/unblock" + ChatColor.RED + ".");
 			else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want to unblock!");
 		}
 		return success;
 	}
 
 	// intra-command methods
 	private UltraWarp locateWarp(int extra_param, CommandSender sender) {
 		// establish some objects
 		index = -1;
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		// extract the name of the player and the name of the warp
 		if (parameters[extra_param].toLowerCase().endsWith("'s")) {
 			name = parameters[extra_param + 1];
 			owner = parameters[extra_param].substring(0, parameters[extra_param].length() - 2);
 			extra_param++;
 		} else {
 			if (player != null)
 				owner = player.getName();
 			else
 				owner = null;
 			name = parameters[extra_param];
 		}
 		// locate the owner's full true name
 		if (owner != null && (player == null || !owner.equals(player.getName())))
 			owner = getFullName(owner);
 		// weed out the definitely wrong warps
 		ArrayList<UltraWarp> possible_warps = new ArrayList<UltraWarp>();
 		ArrayList<Integer> possible_indexes = new ArrayList<Integer>();
 		for (int i = 0; i < warps.size(); i++)
 			if (owner == null || (player != null && owner.equals(player.getName())) || warps.get(i).getOwner().equals(owner))
 				if (warps.get(i).getName().toLowerCase().startsWith(name.toLowerCase())) {
 					possible_warps.add(warps.get(i));
 					possible_indexes.add(i);
 				}
 		// prioritize the possible warps
 		int[] priorities = new int[possible_warps.size()];
 		for (int i = 0; i < possible_warps.size(); i++) {
 			int priority;
 			boolean user_is_listed = false;
 			if (player == null)
 				user_is_listed = true;
 			else
 				for (String listed_user : possible_warps.get(i).getListedUsers())
 					if (listed_user.equals(player.getName()))
 						user_is_listed = true;
 			if (player != null && owner.equals(possible_warps.get(i).getOwner()))
 				priority = 3;
 			else if (possible_warps.get(i).isListed())
 				if ((!possible_warps.get(i).isRestricted() || !user_is_listed) || (possible_warps.get(i).isRestricted() && user_is_listed))
 					priority = 6;
 				else
 					priority = 12;
 			else if ((!possible_warps.get(i).isRestricted() && !user_is_listed) || (possible_warps.get(i).isRestricted() && user_is_listed))
 				priority = 9;
 			else
 				priority = 15;
 			if (possible_warps.get(i).getName().equalsIgnoreCase(name))
 				priority--;
 			if (possible_warps.get(i).getName().equals(name))
 				priority--;
 			priorities[i] = priority;
 		}
 		// find the highest priority warp
 		UltraWarp winner_winner = null;
 		index = -1;
 		if (possible_warps.size() > 0) {
 			winner_winner = possible_warps.get(0);
 			index = possible_indexes.get(0);
 			if (possible_warps.size() > 1) {
 				int first_priority_index = 0;
 				for (int i = 1; i < priorities.length; i++) {
 					if (priorities[i] < priorities[first_priority_index]) {
 						winner_winner = possible_warps.get(i);
 						index = possible_indexes.get(i);
 						first_priority_index = i;
 					}
 				}
 			}
 		}
 		return winner_winner;
 	}
 
 	public static String colorCode(String text) {
 		text = "&f" + text;
 		// put color codes in the right order if they're next to each other
 		for (int i = 0; i < text.length() - 3; i++)
 			if (isColorCode(text.substring(i, i + 2), false, true) && isColorCode(text.substring(i + 2, i + 4), true, true))
 				text = text.substring(0, i) + text.substring(i + 2, i + 4) + text.substring(i, i + 2) + text.substring(i + 4);
 		// replace all anti color codes with non antis
 		String current_color_code = "";
 		for (int i = 0; i < text.length() - 1; i++) {
 			if (isColorCode(text.substring(i, i + 2), null, true))
 				current_color_code = current_color_code + text.substring(i, i + 2);
 			else if (isColorCode(text.substring(i, i + 2), null, false)) {
 				while (text.length() > i + 2 && isColorCode(text.substring(i, i + 2), null, false)) {
 					current_color_code = replaceAll(current_color_code, "&" + text.substring(i + 1, i + 2), "");
 					if (current_color_code.equals(""))
 						current_color_code = "&f";
 					text = text.substring(0, i) + text.substring(i + 2);
 				}
 				text = text.substring(0, i) + current_color_code + text.substring(i);
 			}
 		}
 		String colored_text = ChatColor.translateAlternateColorCodes('&', text);
 		return colored_text;
 	}
 
 	private static Boolean isColorCode(String text, Boolean true_non_formatting_null_either, Boolean true_non_anti_null_either) {
 		if (!text.startsWith("&") && !text.startsWith("%"))
 			return false;
 		if (true_non_anti_null_either != null)
 			if (true_non_anti_null_either && text.startsWith("%"))
 				return false;
 			else if (!true_non_anti_null_either && text.startsWith("&"))
 				return false;
 		if (true_non_formatting_null_either == null || true_non_formatting_null_either)
 			for (String color_color_code_char : color_color_code_chars)
 				if (text.substring(1, 2).equalsIgnoreCase(color_color_code_char))
 					return true;
 		if (true_non_formatting_null_either == null || !true_non_formatting_null_either)
 			for (String formatting_color_code_char : formatting_color_code_chars)
 				if (text.substring(1, 2).equalsIgnoreCase(formatting_color_code_char))
 					return true;
 		return false;
 	}
 
 	private static Boolean getResponse(CommandSender sender, String unformatted_response, String current_status_line, String current_status_is_true_message) {
 		boolean said_yes = false, said_no = false;
 		String formatted_response = unformatted_response;
 		// elimiate unnecessary spaces and punctuation
 		while (formatted_response.startsWith(" "))
 			formatted_response = formatted_response.substring(1);
 		while (formatted_response.endsWith(" "))
 			formatted_response = formatted_response.substring(0, formatted_response.length() - 1);
 		formatted_response = formatted_response.toLowerCase();
 		// check their response
 		for (String yes : yeses)
 			if (formatted_response.startsWith(yes))
 				said_yes = true;
 		if (said_yes)
 			return true;
 		else {
 			for (String no : nos)
 				if (formatted_response.startsWith(no))
 					said_no = true;
 			if (said_no)
 				return false;
 			else if (current_status_line != null) {
 				if (!formatted_response.equals("")) {
 					if (unformatted_response.substring(0, 1).equals(" "))
 						unformatted_response = unformatted_response.substring(1);
 					sender.sendMessage(ChatColor.RED + "I don't know what \"" + unformatted_response + "\" means.");
 				}
 				while (current_status_line.startsWith(" "))
 					current_status_line = current_status_line.substring(1);
 				if (current_status_line.startsWith(current_status_is_true_message))
 					return true;
 				else
 					return false;
 			} else
 				return null;
 		}
 	}
 
 	private String stopParsingMessages(String warp_message, String no_warp_message, String true_warp_name, String true_owner_name, boolean player_is_owner,
 			CommandSender sender, String result_message) {
 		if (parsing_warp_message) {
 			parsing_warp_message = false;
 			if (!result_message.equals(""))
 				result_message = result_message + "\n";
 			if (player_is_owner)
 				if (warp_message.endsWith(".") || warp_message.endsWith("!") || warp_message.endsWith("?"))
 					result_message =
 							result_message + ChatColor.GREEN + "Now people who successfully warp to \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
 									+ colorCode(warp_message) + ChatColor.GREEN + "\"";
 				else if (warp_message.equals(""))
 					result_message = result_message + ChatColor.GREEN + "Now people who successfully warp to \"" + true_warp_name + "\" won't see a message.";
 				else
 					result_message =
 							result_message + ChatColor.GREEN + "Now people who successfully warp to \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
 									+ colorCode(warp_message) + ChatColor.GREEN + ".\"";
 			else if (warp_message.endsWith(".") || warp_message.endsWith("!") || warp_message.endsWith("?"))
 				result_message =
 						result_message + ChatColor.GREEN + "Now people who successfully warp to " + true_owner_name + "'s \"" + true_warp_name
 								+ "\" will see \"" + ChatColor.WHITE + colorCode(warp_message) + ChatColor.GREEN + "\"";
 			else if (warp_message.equals(""))
 				result_message =
 						result_message + ChatColor.GREEN + "Now people who successfully warp to " + true_owner_name + "'s \"" + true_warp_name
 								+ "\" won't see a message.";
 			else
 				result_message =
 						result_message + ChatColor.GREEN + "Now people who successfully warp to " + true_owner_name + "'s \"" + true_warp_name
 								+ "\" will see \"" + ChatColor.WHITE + colorCode(warp_message) + ChatColor.GREEN + ".\"";
 		} else if (parsing_no_warp_message) {
 			parsing_no_warp_message = false;
 			if (!result_message.equals(""))
 				result_message = result_message + "\n";
 			if (player_is_owner)
 				if (warp_message.endsWith(".") || warp_message.endsWith("!") || warp_message.endsWith("?"))
 					result_message =
 							result_message + ChatColor.GREEN + "Now people who aren't allowed to warp to \"" + true_warp_name + "\" will see \""
 									+ ChatColor.WHITE + colorCode(no_warp_message) + ChatColor.GREEN + "\"";
 				else if (no_warp_message.equals(""))
 					result_message =
 							result_message + ChatColor.GREEN + "Now people who aren't allowed to warp to \"" + true_warp_name + "\" won't see a message.";
 				else
 					result_message =
 							result_message + ChatColor.GREEN + "Now people who aren't allowed to warp to \"" + true_warp_name + "\" will see \""
 									+ ChatColor.WHITE + colorCode(no_warp_message) + ChatColor.GREEN + ".\"";
 			else if (no_warp_message.endsWith(".") || no_warp_message.endsWith("!") || no_warp_message.endsWith("?"))
 				result_message =
 						result_message + ChatColor.GREEN + "Now people who aren't allowed to warp to " + true_owner_name + "'s \"" + true_warp_name
 								+ "\" will see \"" + ChatColor.WHITE + colorCode(no_warp_message) + ChatColor.GREEN + "\"";
 			else if (no_warp_message.equals(""))
 				result_message =
 						result_message + ChatColor.GREEN + "Now people who aren't allowed to warp to " + true_owner_name + "'s \"" + true_warp_name
 								+ "\" won't see a message.";
 			else
 				result_message =
 						result_message + ChatColor.GREEN + "Now people who aren't allowed to warp to " + true_owner_name + "'s \"" + true_warp_name
 								+ "\" will see \"" + ChatColor.WHITE + colorCode(no_warp_message) + ChatColor.GREEN + ".\"";
 		}
 		return result_message;
 	}
 
 	private static String replaceAll(String to_return, String to_change, String to_change_to) {
 		int index = 0;
 		while (to_return.contains(to_change) && to_return.length() >= index + to_change.length()) {
 			if (to_return.substring(index, index + to_change.length()).equals(to_change))
 				to_return = to_return.substring(0, index) + to_change_to + to_return.substring(index + to_change.length());
 			index++;
 		}
 		return to_return;
 	}
 
 	private static String getFullName(String name) {
 		String full_name = null;
 		for (Player possible_owner : server.getOnlinePlayers())
 			// if this player's name also matches and it shorter, return it instead becuase if someone is using an autocompleted command, we need to make sure
 			// to get the shortest name because if they meant to use the longer username, they can remedy this by adding more letters to the parameter; however,
 			// if they meant to do a shorter username and the auto-complete finds the longer one first, they're screwed
 			if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase())
 					&& (full_name == null || full_name.length() > possible_owner.getName().length()))
 				full_name = possible_owner.getName();
 		for (OfflinePlayer possible_owner : server.getOfflinePlayers())
 			if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase())
 					&& (full_name == null || full_name.length() > possible_owner.getName().length()))
 				full_name = possible_owner.getName();
 		return full_name;
 	}
 
 	public static int translateStringtoTimeInms(String written) {
 		int time = 0;
 		String[] temp = written.split(" ");
 		ArrayList<String> words = new ArrayList<String>();
 		for (String word : temp)
 			if (!word.equalsIgnoreCase("and") && !word.equalsIgnoreCase("&"))
 				words.add(replaceAll(word.toLowerCase(), ",", ""));
 		while (words.size() > 0) {
 			// for formats like "2 days 3 minutes 5.57 seconds" or "3 d 5 m 12 s"
 			try {
 				double amount = Double.parseDouble(words.get(0));
 				if (words.get(0).contains("d") || words.get(0).contains("h") || words.get(0).contains("m") || words.get(0).contains("s"))
 					throw new NumberFormatException();
 				int factor = 0;
 				if (words.size() > 1) {
 					if (words.get(1).startsWith("d"))
 						factor = 86400000;
 					else if (words.get(1).startsWith("h"))
 						factor = 3600000;
 					else if (words.get(1).startsWith("m"))
 						factor = 60000;
 					else if (words.get(1).startsWith("s"))
 						factor = 1000;
 					if (factor > 0)
 						// since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
 						time = time + (int) (amount * factor + 0.1);
 					words.remove(0);
 					words.remove(0);
 				} else
 					words.remove(0);
 			} catch (NumberFormatException exception) {
 				// if there's no space between the time and units, e.g. "2h, 5m, 25s" or "4hours, 3min, 2.265secs"
 				double amount = 0;
 				int factor = 0;
 				try {
 					if (words.get(0).contains("d") && (!words.get(0).contains("s") || words.get(0).indexOf("s") > words.get(0).indexOf("d"))) {
 						amount = Double.parseDouble(words.get(0).split("d")[0]);
 						console.sendMessage("amount should=" + words.get(0).split("d")[0]);
 						factor = 86400000;
 					} else if (words.get(0).contains("h")) {
 						amount = Double.parseDouble(words.get(0).split("h")[0]);
 						factor = 3600000;
 					} else if (words.get(0).contains("m")) {
 						amount = Double.parseDouble(words.get(0).split("m")[0]);
 						factor = 60000;
 					} else if (words.get(0).contains("s")) {
 						amount = Double.parseDouble(words.get(0).split("s")[0]);
 						factor = 1000;
 					}
 					if (factor > 0)
 						// since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
 						time = time + (int) (amount * factor + 0.1);
 				} catch (NumberFormatException exception2) {
 				}
 				words.remove(0);
 			}
 		}
 		return time;
 	}
 
 	public static String translateTimeInmsToString(int time, boolean round_seconds) {
 		// get the values (e.g. "2 days" or "55.7 seconds")
 		ArrayList<String> values = new ArrayList<String>();
 		if (time > 86400000) {
 			values.add((int) (time / 86400000) + " days");
 			time = time % 86400000;
 		}
 		if (time > 3600000) {
 			values.add((int) (time / 3600000) + " hours");
 			time = time % 3600000;
 		}
 		if (time > 60000) {
 			values.add((int) (time / 60000) + " minutes");
 			time = time % 60000;
 		}
 		// add a seconds value if there is still time remaining or if there are no other values
 		if (time > 0 || values.size() == 0)
 			// if you have partial seconds and !round_seconds, it's written as a double so it doesn't truncate the decimals
 			if ((time / 1000.0) != (time / 1000) && !round_seconds)
 				values.add((time / 1000.0) + " seconds");
 			// if seconds are a whole number, just write it as a whole number (integer)
 			else
 				values.add(Math.round(time / 1000) + " seconds");
 		// if there are two or more values, add an "and"
 		if (values.size() >= 2)
 			values.add(values.size() - 1, "and");
 		// assemble the final String
 		String written = "";
 		for (int i = 0; i < values.size(); i++) {
 			// add spaces as needed
 			if (i > 0)
 				written = written + " ";
 			written = written + values.get(i);
 			// add commas as needed
 			if (values.size() >= 4 && i < values.size() - 1 && !values.get(i).equals("and"))
 				written = written + ",";
 		}
 		if (!written.equals(""))
 			return written;
 		else
 			return null;
 	}
 
 	public boolean teleport(Player player, UltraWarp from, UltraWarp to, boolean send_warp_message, CommandSender non_teleporting_player) {
 		// get the player's cooldown time
 		int cooldown_time = (Integer) default_settings[5];
 		if (use_group_settings && permissions != null && permissions.getPrimaryGroup(player) != null
 				&& group_settings.get(permissions.getPrimaryGroup(player)) != null)
 			cooldown_time = (Integer) group_settings.get(permissions.getPrimaryGroup(player))[5];
 		if (per_player_settings.get(player.getName()) != null)
 			cooldown_time = (Integer) per_player_settings.get(player.getName())[5];
 		// stop here if the cooldown timer has not finished
 		if (cooling_down_players.containsKey(player.getName())
 				&& !player.hasPermission("myultrawarps.admin")
 				&& (non_teleporting_player == null || (non_teleporting_player instanceof Player && !non_teleporting_player.hasPermission("myultrawarps.admin")))) {
 			player.sendMessage(ChatColor.RED
 					+ "Sorry, but you still have to wait "
 					+ translateTimeInmsToString(cooldown_time - (int) (Calendar.getInstance().getTimeInMillis() - cooling_down_players.get(player.getName())),
 							true) + " before you can teleport again.");
 			if (non_teleporting_player != null)
 				// in some instances like /to or /from, other players are involved in the telportation. These players need to be informed of cool down timer
 				// restrictions
 				non_teleporting_player.sendMessage(ChatColor.RED
 						+ "Sorry, but "
 						+ player.getName()
 						+ " can't teleport for another "
 						+ translateTimeInmsToString(cooldown_time
 								- (int) (Calendar.getInstance().getTimeInMillis() - cooling_down_players.get(player.getName())), true) + ".");
 			return false;
 		}
 		// teleport the player
 		to.getLocation().getChunk().load();
 		player.teleport(to.getLocation());
 		if (send_warp_message && !to.getWarpMessage().equals(""))
 			player.sendMessage(colorCode(replaceAll(to.getWarpMessage(), "[player]", player.getName())));
 		if (from != null) {
 			// save the player's location before warping
 			ArrayList<UltraWarp> replacement = warp_histories.get(player.getName());
 			Integer last_warp_index = last_warp_indexes.get(player.getName());
 			if (replacement != null && last_warp_index != null)
 				while (replacement.size() > last_warp_index + 1)
 					replacement.remove(replacement.size() - 1);
 			else if (replacement == null)
 				replacement = new ArrayList<UltraWarp>();
 			replacement.add(from);
 			// save the player's location after warping
 			replacement.add(to);
 			int warp_history_length = (Integer) default_settings[6];
 			if (permissions != null && permissions.getPrimaryGroup(player) != null && use_group_settings
 					&& group_settings.containsKey(permissions.getPrimaryGroup(player)))
 				warp_history_length = (Integer) group_settings.get(permissions.getPrimaryGroup(player))[6];
 			if (per_player_settings.containsKey(player.getName()))
 				warp_history_length = (Integer) per_player_settings.get(player.getName())[6];
 			while (replacement.size() > warp_history_length)
 				replacement.remove(0);
 			warp_histories.put(player.getName(), replacement);
 			last_warp_indexes.put(player.getName(), replacement.size() - 1);
 		}
 		// start the cool down timer if necessary
 		if (cooldown_time > 0) {
 			// record the time that the timer starts
 			cooling_down_players.put(player.getName(), Calendar.getInstance().getTimeInMillis());
 			// the Bukkit scheduler is timed using a tick 20 times/second; therefore, the cooldown time (which is in ms) is divided by 50: /1000 to convert
 			// it to seconds and *20 to account for the 20 ticks/second
 			final String player_name = player.getName();
 			server.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				@Override
 				public void run() {
 					cooling_down_players.remove(player_name);
 				}
 			}, cooldown_time / 50);
 		}
 		return true;
 	}
 
 	// listeners
 	@EventHandler
 	public void informPlayersOfStuffAndRemoveTheirCoolingDownStatusIfNecessary(PlayerJoinEvent event) {
 		if (!event.getPlayer().hasPlayedBefore())
 			event.getPlayer().sendMessage(
 					colorCode(replaceAll(spawn_messages_by_world.get(event.getPlayer().getWorld()), "[player]", event.getPlayer().getName())));
 		else {
 			// tell admins that myUltraWarps has updated
 			File new_myUltraWarps = new File(this.getDataFolder(), "myUltraWarps.jar");
 			if (event.getPlayer().hasPermission("myultrawarps.admin") && new_myUltraWarps.exists())
 				event.getPlayer()
 						.sendMessage(
 								ChatColor.GREEN
 										+ "myUltraWarps has been updated! You should put in the new version right now! I already downloaded it into the myUltraWarps plugin data folder (the place where you find the config.txt and stuff). All you have to do is stop the server and replace the myUltraWarps in the plugins folder with the new one.\nDo it now!");
 			// send people their messages
 			else if (info_messages_for_players.get(event.getPlayer().getName()) != null)
 				for (String message : info_messages_for_players.get(event.getPlayer().getName()))
 					event.getPlayer().sendMessage(message);
 			// remove a player's cooling down status if necessary
 			// I would have it check when it's loading the temporary data concerning cooling down players, but the player needs to be online for me to check all
 			// their permissions, so I have to check it when the y log on instead
 			if (cooling_down_players.containsKey(event.getPlayer().getName())) {
 				// get the player's cooldown time
 				int cooldown_time = (Integer) default_settings[5];
 				if (use_group_settings && permissions != null && permissions.getPrimaryGroup(event.getPlayer()) != null
 						&& group_settings.get(permissions.getPrimaryGroup(event.getPlayer())) != null)
 					cooldown_time = (Integer) group_settings.get(permissions.getPrimaryGroup(event.getPlayer()))[5];
 				if (per_player_settings.get(event.getPlayer().getName()) != null)
 					cooldown_time = (Integer) per_player_settings.get(event.getPlayer().getName())[5];
 				if (cooling_down_players.get(event.getPlayer().getName()) + cooldown_time < Calendar.getInstance().getTimeInMillis())
 					cooling_down_players.remove(event.getPlayer().getName());
 			}
 		}
 	}
 
 	@EventHandler
 	public void teleportToHomeOnRespawn(PlayerRespawnEvent event) {
 		UltraWarp home = null;
 		for (UltraWarp warp : warps)
 			if (warp.getOwner().equals(event.getPlayer().getName()) && warp.getName().equals("home"))
 				home = warp;
 		if (home != null) {
 			event.setRespawnLocation(home.getLocation());
 			event.getPlayer().sendMessage(colorCode(home.getWarpMessage()));
 		} else
 			event.getPlayer().sendMessage(ChatColor.RED + "I would teleport you to your home, but you haven't set one yet!");
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void warpViaSwitch(PlayerInteractEvent event) {
 		if (warps != null && warps.size() > 0 && switches != null && switches.size() > 0) {
 			Block target_block = event.getClickedBlock();
 			UltraSwitch target = null;
 			UltraWarp warp_target = null;
 			// sign post=63, wall sign=68, lever=69, stone pressure plate=70,
 			// wooden pressure plate=72, stone button=77, wooden button = 143
 			if (target_block != null
 					&& (target_block.getTypeId() == 63 || target_block.getTypeId() == 68 || target_block.getTypeId() == 69 || target_block.getTypeId() == 70
 							|| target_block.getTypeId() == 72 || target_block.getTypeId() == 77 || target_block.getTypeId() == 143)) {
 				for (UltraSwitch my_switch : switches)
 					if (target_block.getLocation().equals(my_switch.getLocation())
 							&& ((my_switch.getSwitchType().equals("pressure plate") && event.getAction().equals(Action.PHYSICAL)) || (!my_switch
 									.getSwitchType().equals("pressure plate") && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))) {
 						target = my_switch;
 						break;
 					}
 				if (target != null) {
 					// cancel the interaction event if you right-clicked a sign to make sure it doesn't make you place the block in your hand
 					if (target_block.getTypeId() == 63 || target_block.getTypeId() == 68)
 						event.setCancelled(true);
 					for (UltraWarp warp : warps)
 						if (warp.getOwner().equals(target.getWarpOwner()) && warp.getName().equals(target.getWarpName()))
 							warp_target = warp;
 					if (warp_target != null) {
 						boolean listed = false;
 						for (String listed_user : warp_target.getListedUsers())
 							if (listed_user.equals(event.getPlayer().getName()))
 								listed = true;
 						if (event.getPlayer().getName().equals(warp_target.getOwner()) || (!warp_target.isRestricted() && !listed)
 								|| (warp_target.isRestricted() && listed) || event.getPlayer().hasPermission("myultrawarps.warptowarp.other")
 								|| event.getPlayer().hasPermission("myultrawarps.admin")) {
 							String warp_name = warp_target.getName();
 							if (!warp_target.getOwner().equals(event.getPlayer().getName()))
 								warp_name = warp_target.getOwner() + "'s " + warp_target.getName();
 							teleport(event.getPlayer(), new UltraWarp("God", "coordinates", false, false,
 									"&aThis is the spot you were at before you warped to " + warp_name + ".", "", null, event.getPlayer().getLocation()),
 									warp_target, true, null);
 						} else
 							event.getPlayer().sendMessage(colorCode(replaceAll(warp_target.getNoWarpMessage(), "[player]", event.getPlayer().getName())));
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void playerBrokeASwitch(BlockBreakEvent event) {
 		double x = event.getBlock().getLocation().getX();
 		double y = event.getBlock().getLocation().getY();
 		double z = event.getBlock().getLocation().getZ();
 		World world = event.getBlock().getLocation().getWorld();
 		String type = "";
 		if (event.getBlock().getTypeId() == 63 || event.getBlock().getTypeId() == 68)
 			type = "sign";
 		else if (event.getBlock().getTypeId() == 69)
 			type = "lever";
 		else if (event.getBlock().getTypeId() == 70 || event.getBlock().getTypeId() == 72)
 			type = "pressure plate";
 		else if (event.getBlock().getTypeId() == 77 || event.getBlock().getTypeId() == 143)
 			type = "button";
 		if (!type.equals("")) {
 			for (int i = 0; i < switches.size(); i++)
 				if (switches.get(i).getX() == x && switches.get(i).getY() == y && switches.get(i).getZ() == z && switches.get(i).getWorld().equals(world)
 						&& switches.get(i).getSwitchType().equals(type)) {
 					// if the user broke their own switch
 					if ((event.getPlayer().hasPermission("myultrawarps.unlink") || event.getPlayer().hasPermission("myultrawarps.user"))
 							&& switches.get(i).getWarpOwner().equals(event.getPlayer().getName())) {
 						event.getPlayer().sendMessage(ChatColor.GREEN + "You unlinked \"" + switches.get(i).getWarpName() + "\" from this " + type + ".");
 						switches.remove(i);
 					} // if the switch was broken by an admin
 					else if (event.getPlayer().hasPermission("myultrawarps.unlink.other") || event.getPlayer().hasPermission("myultrawarps.admin")) {
 						event.getPlayer().sendMessage(
 								ChatColor.GREEN + "You unlinked " + switches.get(i).getWarpOwner() + "'s " + switches.get(i).getSwitchType()
 										+ " that was linked to \"" + switches.get(i).getWarpName() + ".\"");
 						boolean owner_found = false;
 						for (Player player : server.getOnlinePlayers())
 							if (player.getName().equals(switches.get(i).getWarpOwner())) {
 								owner_found = true;
 								player.sendMessage(ChatColor.RED + event.getPlayer().getName() + " broke your " + switches.get(i).getSwitchType() + " at ("
 										+ (int) switches.get(i).getX() + ", " + (int) switches.get(i).getY() + ", " + (int) switches.get(i).getZ() + ") in \""
 										+ switches.get(i).getWorld().getName() + ".\"");
 							}
 						if (!owner_found) {
 							ArrayList<String> messages = info_messages_for_players.get(switches.get(i).getWarpOwner());
 							if (messages == null)
 								messages = new ArrayList<String>();
 							messages.add(ChatColor.RED + event.getPlayer().getName() + " broke your " + switches.get(i).getSwitchType() + " at ("
 									+ (int) switches.get(i).getX() + ", " + (int) switches.get(i).getY() + ", " + (int) switches.get(i).getZ() + ") in \""
 									+ switches.get(i).getWorld().getName() + ".\"");
 							info_messages_for_players.put(switches.get(i).getWarpOwner(), messages);
 						}
 						switches.remove(i);
 					} else {
 						event.setCancelled(true);
 						event.getPlayer().sendMessage(ChatColor.RED + "This switch doesn't belong to you. You're not allowed to break it.");
 					}
 				}
 		}
 	}
 
 	@EventHandler
 	public void explosionBrokeASwitch(EntityExplodeEvent event) {
 		for (int i = 0; i < event.blockList().size(); i++) {
 			double x = event.blockList().get(i).getLocation().getX();
 			double y = event.blockList().get(i).getLocation().getY();
 			double z = event.blockList().get(i).getLocation().getZ();
 			World world = event.blockList().get(i).getLocation().getWorld();
 			String type = "";
 			if (event.blockList().get(i).getTypeId() == 63 || event.blockList().get(i).getTypeId() == 68)
 				type = "sign";
 			else if (event.blockList().get(i).getTypeId() == 69)
 				type = "lever";
 			else if (event.blockList().get(i).getTypeId() == 70 || event.blockList().get(i).getTypeId() == 72)
 				type = "pressure plate";
 			else if (event.blockList().get(i).getTypeId() == 77 || event.blockList().get(i).getTypeId() == 143)
 				type = "button";
 			if (!type.equals("")) {
 				for (int j = 0; j < switches.size(); j++)
 					if (switches.get(j).getX() == x && switches.get(j).getY() == y && switches.get(j).getZ() == z && switches.get(j).getWorld().equals(world)
 							&& switches.get(j).getSwitchType().equals(type)) {
 						String cause;
 						if (event.getEntityType() == null)
 							cause = "Some genius trying to use a bed in the Nether";
 						else if (event.getEntityType().getName().equals("Creeper"))
 							cause = "A creeper";
 						else if (event.getEntityType().getName().equals("Fireball"))
 							cause = "A Ghast";
 						else if (event.getEntityType().getName().equals("PrimedTnt"))
 							cause = "A T.N.T. blast";
 						else
 							cause = "An explosion of some sort";
 						boolean owner_found = false;
 						for (Player player : server.getOnlinePlayers())
 							if (player.getName().equals(switches.get(j).getWarpOwner())) {
 								owner_found = true;
 								player.sendMessage(ChatColor.RED + "Your " + switches.get(j).getSwitchType() + " at (" + (int) switches.get(j).getX() + ", "
 										+ (int) switches.get(j).getY() + ", " + (int) switches.get(j).getZ() + ") in \"" + switches.get(j).getWorld().getName()
 										+ "\" linked to \"" + switches.get(j).getWarpName() + "\" was broken by " + cause + "!");
 							}
 						if (!owner_found) {
 							ArrayList<String> messages = info_messages_for_players.get(switches.get(j).getWarpOwner());
 							if (messages == null)
 								messages = new ArrayList<String>();
 							messages.add(ChatColor.RED + cause + " broke your " + switches.get(j).getSwitchType() + " at (" + (int) switches.get(j).getX()
 									+ ", " + (int) switches.get(j).getY() + ", " + (int) switches.get(j).getZ() + ") in \""
 									+ switches.get(j).getWorld().getName() + ".\"");
 							info_messages_for_players.put(switches.get(j).getWarpOwner(), messages);
 						}
 						switches.remove(j);
 					}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void readTeleportAcceptancesOrRefusals(AsyncPlayerChatEvent event) {
 		if (to_teleport_requests.get(event.getPlayer().getName()) != null && to_teleport_requests.get(event.getPlayer().getName()).size() > 0) {
 			Player teleporting_player = null;
 			for (Player player : server.getOnlinePlayers())
 				if (player.getName().equals(to_teleport_requests.get(event.getPlayer().getName()).get(0))) {
 					teleporting_player = player;
 					break;
 				}
 			if (teleporting_player == null) {
 				event.getPlayer().sendMessage(ChatColor.RED + to_teleport_requests.get(event.getPlayer().getName()).get(0) + " must have gone offline. Sorry.");
 				event.setCancelled(true);
 				ArrayList<String> requesting_players = to_teleport_requests.get(event.getPlayer().getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				else {
 					String remove = requesting_players.get(0);
 					while (requesting_players.contains(remove))
 						requesting_players.remove(remove);
 				}
 				to_teleport_requests.put(event.getPlayer().getName(), requesting_players);
 				return;
 			}
 			Boolean accepted = getResponse(event.getPlayer(), event.getMessage(), null, null);
 			if (accepted != null && accepted) {
 				event.setCancelled(true);
 				if (teleport(teleporting_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you teleported to "
 						+ event.getPlayer().getName() + ".", "", null, teleporting_player.getLocation()),
 						new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at when you teleported to "
 								+ event.getPlayer().getName() + ".", "", null, event.getPlayer().getLocation()), false, event.getPlayer())) {
 					teleporting_player.sendMessage(ChatColor.GREEN + event.getPlayer().getName() + " said \"" + event.getMessage() + "\"!");
 					event.getPlayer().sendMessage(ChatColor.GREEN + "Cool. I'll go get " + teleporting_player.getName() + ".");
 				}
 				// remove the teleportation request
 				ArrayList<String> requesting_players = to_teleport_requests.get(event.getPlayer().getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				else {
 					String remove = requesting_players.get(0);
 					while (requesting_players.contains(remove))
 						requesting_players.remove(remove);
 				}
 				to_teleport_requests.put(event.getPlayer().getName(), requesting_players);
 			} else if (accepted != null) {
 				event.setCancelled(true);
 				if (!event.getMessage().endsWith(".") && !event.getMessage().endsWith("!") && !event.getMessage().endsWith("?")) {
 					event.getPlayer().sendMessage(
 							ChatColor.GREEN + "Okay. I'll tell " + teleporting_player.getName() + " that you said \"" + event.getMessage() + ".\"");
 					teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + ".\"");
 				} else {
 					event.getPlayer().sendMessage(
 							ChatColor.GREEN + "Okay. I'll tell " + teleporting_player.getName() + " that you said \"" + event.getMessage() + "\"");
 					teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + "\"");
 				}
 				ArrayList<String> requesting_players = to_teleport_requests.get(event.getPlayer().getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				else {
 					String remove = requesting_players.get(0);
 					while (requesting_players.contains(remove))
 						requesting_players.remove(remove);
 				}
 				to_teleport_requests.put(event.getPlayer().getName(), requesting_players);
 			}
 		} else if (from_teleport_requests.get(event.getPlayer().getName()) != null && from_teleport_requests.get(event.getPlayer().getName()).size() > 0) {
 			// event.getPlayer() is the one teleporting here!
 			Player non_teleporting_player = null;
 			for (Player player : server.getOnlinePlayers())
 				if (from_teleport_requests.get(event.getPlayer().getName()).get(0).equals(player.getName())) {
 					non_teleporting_player = player;
 					break;
 				}
 			if (non_teleporting_player == null) {
 				event.getPlayer().sendMessage(
 						ChatColor.RED + from_teleport_requests.get(event.getPlayer().getName()).get(0) + " must have gone offline. Sorry.");
 				event.setCancelled(true);
 				ArrayList<String> requesting_players = from_teleport_requests.get(event.getPlayer().getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				else {
 					String remove = requesting_players.get(0);
 					while (requesting_players.contains(remove))
 						requesting_players.remove(remove);
 				}
 				from_teleport_requests.put(event.getPlayer().getName(), requesting_players);
 				return;
 			}
 			Boolean accepted = getResponse(event.getPlayer(), event.getMessage(), null, null);
 			if (accepted != null && accepted) {
 				event.setCancelled(true);
 				if (teleport(event.getPlayer(), new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before "
 						+ non_teleporting_player.getName() + " teleported you to them.", "", null, event.getPlayer().getLocation()), new UltraWarp("God",
 						"coordinates", false, false, "&aThis is the spot you were at when you were teleported to " + non_teleporting_player.getName() + ".",
 						"", null, non_teleporting_player.getLocation()), false, non_teleporting_player)) {
 					event.getPlayer().sendMessage(ChatColor.GREEN + "Here's your " + non_teleporting_player.getName() + "!");
 					non_teleporting_player.sendMessage(ChatColor.GREEN + "Look! I brought you a " + event.getPlayer().getName() + "!");
 				}
 			} else if (accepted != null) {
 				event.setCancelled(true);
 				if (!event.getMessage().endsWith(".") && !event.getMessage().endsWith("!") && !event.getMessage().endsWith("?")) {
 					event.getPlayer().sendMessage(
 							ChatColor.GREEN + "Okay. I'll tell " + non_teleporting_player.getName() + " that you said \"" + event.getMessage() + ".\"");
 					non_teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + ".\"");
 				} else {
 					event.getPlayer().sendMessage(
 							ChatColor.GREEN + "Okay. I'll tell " + non_teleporting_player.getName() + " that you said \"" + event.getMessage() + "\"");
 					non_teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + "\"");
 				}
 				// remove the teleportation request
 				ArrayList<String> requesting_players = from_teleport_requests.get(event.getPlayer().getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				else {
 					String remove = requesting_players.get(0);
 					while (requesting_players.contains(remove))
 						requesting_players.remove(remove);
 				}
 				from_teleport_requests.put(event.getPlayer().getName(), requesting_players);
 			}
 		}
 	}
 
 	@EventHandler
 	public void trackDeathHistories(PlayerDeathEvent event) {
 		ArrayList<Location> replacement = death_histories.get(event.getEntity().getName());
 		if (replacement == null)
 			replacement = new ArrayList<Location>();
 		replacement.add(event.getEntity().getLocation());
 		int death_history_length = (Integer) default_settings[7];
 		if (permissions != null && permissions.getPrimaryGroup(event.getEntity()) != null && use_group_settings
 				&& group_settings.containsKey(permissions.getPrimaryGroup(event.getEntity())))
 			death_history_length = (Integer) group_settings.get(permissions.getPrimaryGroup(event.getEntity()))[7];
 		if (per_player_settings.containsKey(event.getEntity().getName()))
 			death_history_length = (Integer) per_player_settings.get(event.getEntity().getName())[7];
 		while (replacement.size() > death_history_length)
 			replacement.remove(0);
 		console.sendMessage("death history length=" + replacement.size());
 		death_histories.put(event.getEntity().getName(), replacement);
 		last_warp_to_death_indexes.put(event.getEntity().getName(), replacement.size() - 1);
 	}
 
 	// loading
 	private void loadTheWarps(CommandSender sender) {
 		boolean failed = false;
 		warps = new ArrayList<UltraWarp>();
 		// check the warps file
 		File warps_file = new File(getDataFolder(), "warps.txt");
 		if (!warps_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				console.sendMessage(ChatColor.GREEN + "I couldn't find a warps.txt file. I'll make a new one.");
 				warps_file.createNewFile();
 			} catch (IOException exception) {
 				console.sendMessage(ChatColor.DARK_RED + "I couldn't create a warps.txt file! Oh nos!");
 				exception.printStackTrace();
 				failed = true;
 			}
 		}
 		// read the warps.txt file
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(warps_file));
 			String save_line = in.readLine();
 			while (save_line != null) {
 				warps.add(new UltraWarp(save_line));
 				save_line = in.readLine();
 				continue;
 			}
 			in.close();
 		} catch (FileNotFoundException exception) {
 			console.sendMessage(ChatColor.DARK_RED + "The warps.txt I created a few milliseconds ago doesn't exist. -_-");
 			exception.printStackTrace();
 		} catch (IOException exception) {
 			console.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your warps.");
 			exception.printStackTrace();
 			failed = true;
 		}
 		if (!failed) {
 			if (warps.size() > 1) {
 				// alphabetize the warps by name (by owner secondarily)
 				ArrayList<UltraWarp> temp_warps = new ArrayList<UltraWarp>();
 				for (UltraWarp warp : warps)
 					temp_warps.add(warp);
 				warps = new ArrayList<UltraWarp>();
 				UltraWarp first_warp;
 				int delete_index;
 				while (temp_warps.size() > 0) {
 					first_warp = temp_warps.get(0);
 					delete_index = 0;
 					for (int j = 0; j < temp_warps.size(); j++) {
 						if (temp_warps.get(j).getName().compareToIgnoreCase(first_warp.getName()) < 0
 								|| (temp_warps.get(j).getName().compareToIgnoreCase(first_warp.getName()) == 0 && temp_warps.get(j).getOwner()
 										.compareToIgnoreCase(first_warp.getOwner()) < 0)) {
 							first_warp = temp_warps.get(j);
 							delete_index = j;
 						}
 					}
 					// rename warps named "info", "all", or "list"
 					if (!first_warp.getName().equalsIgnoreCase("info") && !first_warp.getName().equalsIgnoreCase("all")
 							&& !first_warp.getName().equalsIgnoreCase("list"))
 						warps.add(first_warp);
 					else {
 						UltraWarp renamed_first_warp =
 								new UltraWarp(first_warp.getOwner(), "my" + first_warp.getName(), first_warp.isListed(), first_warp.isRestricted(), first_warp
 										.getWarpMessage(), first_warp.getNoWarpMessage(), first_warp.getListedUsers(), first_warp.getLocation());
 						warps.add(renamed_first_warp);
 						boolean found = false;
 						for (Player renamed_warp_owner : server.getOnlinePlayers())
 							if (renamed_warp_owner.getName().equals(first_warp.getOwner())) {
 								renamed_warp_owner.sendMessage(ChatColor.RED + "I found a warp of yours that was named \"" + first_warp.getName()
 										+ ".\" Unfortunately, it interferes with the command " + ChatColor.GREEN + "/warp " + first_warp.getName()
 										+ ChatColor.RED + ", so I had to rename it \"my" + first_warp.getName() + ".\" Sorry for the inconvenience.");
 								found = true;
 							}
 						if (!found) {
 							// info the player that his/her warp has been renamed
 							ArrayList<String> messages = info_messages_for_players.get(first_warp.getOwner());
 							if (messages == null)
 								messages = new ArrayList<String>();
 							messages.add(ChatColor.RED + "I found a warp of yours that was named \"" + first_warp.getName()
 									+ ".\" Unfortunately, it interferes with the command " + ChatColor.GREEN + "/warp " + first_warp.getName() + ChatColor.RED
 									+ ", so I had to rename it \"my" + first_warp.getName() + ".\" Sorry for the inconvenience.");
 							info_messages_for_players.put(first_warp.getOwner(), messages);
 						}
 					}
 					temp_warps.remove(delete_index);
 				}
 			}
 			saveTheWarps(sender, false);
 			// send the sender a confirmation message
 			if (warps.size() > 1)
 				sender.sendMessage(ChatColor.GREEN + "Your " + warps.size() + " warps have been loaded.");
 			else if (warps.size() == 1)
 				sender.sendMessage(ChatColor.GREEN + "Your 1 warp has been loaded.");
 			else
 				sender.sendMessage(ChatColor.GREEN + "You have no warps to load!");
 			if (sender instanceof Player)
 				if (warps.size() > 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " loaded " + warps.size() + " warps from file.");
 				else if (warps.size() == 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " loaded the server's 1 warp from file.");
 				else
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName()
 							+ " loaded the server's warps from file, but there were no warps on file.");
 		}
 	}
 
 	private void loadTheSwitches(CommandSender sender) {
 		// check the switches file
 		boolean failed = false;
 		switches = new ArrayList<UltraSwitch>();
 		File switches_file = new File(getDataFolder(), "switches.txt");
 		if (!switches_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				sender.sendMessage(ChatColor.GREEN + "I couldn't find a switches.txt file. I'll make a new one.");
 				switches_file.createNewFile();
 			} catch (IOException exception) {
 				sender.sendMessage(ChatColor.DARK_RED + "I couldn't create a switches.txt file! Oh nos!");
 				exception.printStackTrace();
 			}
 		}
 		// read the switches.txt file
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(switches_file));
 			String save_line = in.readLine();
 			while (save_line != null) {
 				switches.add(new UltraSwitch(save_line));
 				save_line = in.readLine();
 			}
 			in.close();
 		} catch (FileNotFoundException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "The switches.txt I created a few milliseconds ago doesn't exist. -_-");
 			exception.printStackTrace();
 		} catch (IOException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "I got you a present. It's an IOEcxeption in config.txt.");
 			exception.printStackTrace();
 		}
 		if (!failed) {
 			if (switches.size() > 1) {
 				// alphabetize the switches by warp name (by owner secondarily)
 				ArrayList<UltraSwitch> temp_switches = new ArrayList<UltraSwitch>();
 				for (UltraSwitch my_switch : switches)
 					temp_switches.add(my_switch);
 				switches = new ArrayList<UltraSwitch>();
 				UltraSwitch first_switch;
 				int delete_index;
 				while (temp_switches.size() > 0) {
 					first_switch = temp_switches.get(0);
 					delete_index = 0;
 					for (int j = 0; j < temp_switches.size(); j++) {
 						if (temp_switches.get(j).getWarpName().compareToIgnoreCase(first_switch.getWarpName()) < 0
 								|| (temp_switches.get(j).getWarpName().compareToIgnoreCase(first_switch.getWarpName()) == 0 && temp_switches.get(j)
 										.getWarpOwner().compareToIgnoreCase(first_switch.getWarpOwner()) < 0)) {
 							first_switch = temp_switches.get(j);
 							delete_index = j;
 						}
 					}
 					switches.add(first_switch);
 					temp_switches.remove(delete_index);
 				}
 			}
 			saveTheSwitches(sender, false);
 			if (switches.size() > 1)
 				sender.sendMessage(ChatColor.GREEN + "Your " + switches.size() + " switches have been loaded.");
 			else if (switches.size() == 1)
 				sender.sendMessage(ChatColor.GREEN + "Your 1 switch has been loaded.");
 			else
 				sender.sendMessage(ChatColor.GREEN + "You have no switches to load!");
 			if (sender instanceof Player)
 				if (switches.size() > 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " loaded " + warps.size() + " switches from file.");
 				else if (switches.size() == 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " loaded the server's 1 switch from file.");
 				else
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName()
 							+ " loaded the server's switches from file, but there were no switches on file.");
 		}
 	}
 
 	private void loadTheConfig(CommandSender sender) {
 		// link up with Vault
 		Vault = server.getPluginManager().getPlugin("Vault");
 		if (Vault != null) {
 			// locate the permissions and economy plugins
 			try {
 				permissions = server.getServicesManager().getRegistration(Permission.class).getProvider();
 			} catch (NullPointerException exception) {
 				permissions = null;
 			}
 			try {
 				economy = server.getServicesManager().getRegistration(Economy.class).getProvider();
 			} catch (NullPointerException exception) {
 				economy = null;
 			}
 			// forcibly enable the permissions plugin
 			if (permissions != null) {
 				Plugin permissions_plugin = server.getPluginManager().getPlugin(permissions.getName());
 				if (permissions_plugin != null && !permissions_plugin.isEnabled())
 					server.getPluginManager().enablePlugin(permissions_plugin);
 			}
 			// send confirmation messages
 			console.sendMessage(ChatColor.GREEN + "I see your Vault...");
 			if (permissions == null && economy == null)
 				console.sendMessage(ChatColor.RED + "...but I can't find any economy or permissions plugins.");
 			else if (permissions != null) {
 				console.sendMessage(ChatColor.GREEN + "...and raise you a " + permissions.getName() + "...");
 				if (economy != null)
 					console.sendMessage(ChatColor.GREEN + "...as well as a " + economy.getName() + ".");
 				else
 					console.sendMessage(ChatColor.RED + "...but I can't find your economy plugin.");
 			} else if (permissions == null && economy != null) {
 				console.sendMessage(ChatColor.GREEN + "...and raise you a " + economy.getName() + "...");
 				console.sendMessage(ChatColor.RED + "...but I can't find your permissions plugin.");
 			}
 		}
 		default_settings[0] = true;
 		default_settings[1] = true;
 		default_settings[2] = "&aWelcome to the [warp].";
 		default_settings[3] = "&cYou're not allowed to warp to [owner]'s [warp].";
 		default_settings[4] = -1;
 		default_settings[5] = 0;
 		default_settings[6] = 20;
 		default_settings[7] = 5;
 		per_player_settings = new HashMap<String, Object[]>();
 		group_settings = new HashMap<String, Object[]>();
 		// check the config file
 		File config_file = new File(getDataFolder(), "config.txt");
 		if (!config_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				sender.sendMessage(ChatColor.GREEN + "I couldn't find a config.txt file. I'll make a new one.");
 				config_file.createNewFile();
 				saveTheConfig(sender, false);
 			} catch (IOException exception) {
 				sender.sendMessage(ChatColor.DARK_RED + "I couldn't create a config.txt file! Oh nos!");
 				exception.printStackTrace();
 			}
 		}
 		// read the config.txt file
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(config_file));
 			String save_line = in.readLine(), parsing = "";
 			boolean already_progressed;
 			while (save_line != null) {
 				already_progressed = false;
 				// eliminate preceding spaces
 				while (save_line.startsWith(" "))
 					save_line = save_line.substring(1);
 				// get the configurations
 				if (save_line.startsWith("Do you want to be able to change settings for permissions-based groups of users?"))
 					use_group_settings = getResponse(sender, save_line.substring(80), in.readLine(), "Group settings are enabled");
 				else if (save_line.startsWith("Do you want myUltraWarps to check for updates every time it is enabled?"))
 					auto_update = getResponse(sender, save_line.substring(71), in.readLine(), "Right now, myUltraWarps will auto-update.");
 				else if (save_line.startsWith("Do you want myUltraWarps to automatically save the warps file every time a change is made?"))
 					autosave_warps = getResponse(sender, save_line.substring(90), in.readLine(), "Right now, autosave is on for warps.");
 				else if (save_line.startsWith("Do you want myUltraWarps to automatically save the switches file every time a change is made?"))
 					autosave_switches = getResponse(sender, save_line.substring(93), in.readLine(), "Right now, autosave is on for switches.");
 				else if (save_line.startsWith("Do you want myUltraWarps to automatically save the config file every time a change is made?"))
 					autosave_config = getResponse(sender, save_line.substring(91), in.readLine(), "Right now, autosave is on for the config.");
 				else if (save_line.startsWith("You can set the messages that appear when someone teleports to the spawn point for each world.")) {
 					parsing = "spawn messages";
 					save_line = in.readLine();
 					while (save_line.startsWith(" "))
 						save_line = save_line.substring(1);
 				} else if (save_line.startsWith("global settings:")) {
 					parsing = "global";
 					save_line = save_line.substring(16);
 					while (save_line.startsWith(" "))
 						save_line = save_line.substring(1);
 				} else if (save_line.startsWith("group settings:")) {
 					parsing = "group";
 					save_line = save_line.substring(15);
 					while (save_line.startsWith(" "))
 						save_line = save_line.substring(1);
 				} else if (save_line.startsWith("individual settings:")) {
 					parsing = "individual";
 					save_line = save_line.substring(20);
 					while (save_line.startsWith(" "))
 						save_line = save_line.substring(1);
 				}
 				if (parsing.equals("spawn messages")) {
 					String[] temp = save_line.split(":");
 					String world_name = temp[0], spawn_message = temp[1];
 					// eliminate preceding spaces in the spawn message
 					while (spawn_message.startsWith(" "))
 						spawn_message = spawn_message.substring(1);
 					if (!world_name.equals("")) {
 						// format the Nether and End world names to cooordinate with the server's world naming system
 						if (world_name.endsWith(" (The Nether)"))
 							world_name = world_name.substring(0, world_name.length() - 13) + "_nether";
 						else if (world_name.endsWith(" (The End)"))
 							world_name = world_name.substring(0, world_name.length() - 10) + "_the_end";
 						World world = server.getWorld(world_name);
 						if (world != null)
 							spawn_messages_by_world.put(world, spawn_message);
 						else
 							sender.sendMessage(ChatColor.RED + "I've never heard of a world called \"" + temp[0] + ".\"");
 					}
 				} else if (parsing.equals("global")) {
 					if (save_line.startsWith("Do you want players to be able to teleport to one another without asking permission?"))
 						default_settings[0] =
 								!getResponse(sender, save_line.substring(84), in.readLine(), "Right now, players can teleport to each other freely.");
 					else if (save_line.startsWith("Do you want players to be able teleport other players places without asking permission?"))
 						default_settings[1] =
 								!getResponse(sender, save_line.substring(87), in.readLine(), "Right now, players can teleport each other to them freely.");
 					else if (save_line.toLowerCase().startsWith("default warp message: "))
 						default_settings[2] = save_line.substring(22);
 					else if (save_line.toLowerCase().startsWith("default no warp message: "))
 						default_settings[3] = save_line.substring(25);
 					else if (save_line.toLowerCase().startsWith("max warps: ")) {
 						try {
 							default_settings[4] = Integer.parseInt(save_line.substring(11));
 						} catch (NumberFormatException exception) {
 							default_settings[4] = -1;
 							if (!save_line.substring(11).equalsIgnoreCase("infinite")) {
 								sender.sendMessage(ChatColor.RED + "There was an error in your global settings.");
 								sender.sendMessage(ChatColor.RED + "The maximum number of warps that someone can have has to be an integer or \"infinite.\"");
 								sender.sendMessage(ChatColor.RED + "I'm setting the global max number of warps to \"infinite.\"");
 							}
 						}
 					} else if (save_line.toLowerCase().startsWith("cool down time: "))
 						default_settings[5] = translateStringtoTimeInms(save_line.substring(16));
 					else if (save_line.toLowerCase().startsWith("warp history length: ")) {
 						try {
 							default_settings[6] = Integer.parseInt(save_line.substring(21));
 						} catch (NumberFormatException exception) {
 							default_settings[6] = -1;
 							if (!save_line.substring(21).equalsIgnoreCase("infinite")) {
 								sender.sendMessage(ChatColor.RED + "There was an error in your global settings.");
 								sender.sendMessage(ChatColor.RED + "The warp history length has to be an integer or \"infinite.\"");
 								sender.sendMessage(ChatColor.RED + "I'm setting the global warp history length to \"infinite.\"");
 							}
 						}
 					} else if (save_line.toLowerCase().startsWith("death history length: ")) {
 						try {
 							default_settings[7] = Integer.parseInt(save_line.substring(22));
 						} catch (NumberFormatException exception) {
 							default_settings[7] = -1;
 							if (!save_line.substring(22).equalsIgnoreCase("infinite")) {
 								sender.sendMessage(ChatColor.RED + "There was an error in your global settings.");
 								sender.sendMessage(ChatColor.RED + "The death history length has to be an integer or \"infinite.\"");
 								sender.sendMessage(ChatColor.RED + "I'm setting the global death history length to \"infinite.\"");
 							}
 						}
 					}
 				} else if (parsing.equals("group") && use_group_settings && permissions != null) {
 					if (save_line.split(":").length <= 1 || !save_line.split(":")[0].contains(" ")) {
 						String group_name = "";
 						for (int i = 0; i < save_line.length(); i++) {
 							if (save_line.substring(i, i + 1).equals(":")) {
 								group_name = save_line.substring(0, i);
 								int temp_i = i;
 								i = save_line.length();
 								save_line = save_line.substring(temp_i + 1);
 							}
 						}
 						if (!group_name.equals("")) {
 							Object[] data = new Object[default_settings.length];
 							for (int i = 0; i < default_settings.length; i++)
 								data[i] = default_settings[i];
 							boolean first_line = true;
 							already_progressed = true;
 							while (save_line != null) {
 								// eliminate preceding spaces
 								while (save_line.startsWith(" "))
 									save_line = save_line.substring(1);
 								if (save_line.startsWith("Do you want players in this group to be able to teleport to one another without asking permission?"))
 									data[0] =
 											!getResponse(sender, save_line.substring(99), in.readLine(),
 													"Right now, players in this group can teleport to each other freely.");
 								else if (save_line
 										.startsWith("Do you want players in this group to be able to teleport other players places without asking permission?"))
 									data[1] =
 											!getResponse(sender, save_line.substring(104), in.readLine(),
 													"Right now, players can teleport each other to them freely.");
 								else if (save_line.toLowerCase().startsWith("default warp message: "))
 									data[2] = save_line.substring(22);
 								else if (save_line.toLowerCase().startsWith("default no warp message: "))
 									data[3] = save_line.substring(25);
 								else if (save_line.toLowerCase().startsWith("max warps: ")) {
 									try {
 										data[4] = Integer.parseInt(save_line.substring(11));
 									} catch (NumberFormatException exception) {
 										data[4] = -1;
 										if (!save_line.substring(11).equalsIgnoreCase("infinite")) {
 											sender.sendMessage(ChatColor.RED + "There was an error in your group settings.");
 											sender.sendMessage(ChatColor.RED
 													+ "The maximum number of warps that someone can have has to be an integer or \"infinite.\"");
 											sender.sendMessage(ChatColor.RED + "I'm setting the max number of warps for players in the \"" + group_name
 													+ "\" group to \"infinite.\"");
 										}
 									}
 								} else if (save_line.toLowerCase().startsWith("cool down time: "))
 									data[5] = translateStringtoTimeInms(save_line.substring(16));
 								else if (save_line.toLowerCase().startsWith("warp history length: ")) {
 									try {
 										data[6] = Integer.parseInt(save_line.substring(21));
 									} catch (NumberFormatException exception) {
 										data[6] = -1;
 										if (!save_line.substring(21).equalsIgnoreCase("infinite")) {
 											sender.sendMessage(ChatColor.RED + "There was an error in your group settings.");
 											sender.sendMessage(ChatColor.RED + "The warp history length to be an integer or \"infinite.\"");
 											sender.sendMessage(ChatColor.RED + "I'm setting the warp history length for players in the \"" + group_name
 													+ "\" group to \"infinite.\"");
 										}
 									}
 								} else if (save_line.toLowerCase().startsWith("death history length: ")) {
 									try {
 										data[7] = Integer.parseInt(save_line.substring(22));
 									} catch (NumberFormatException exception) {
 										data[7] = -1;
 										if (!save_line.substring(22).equalsIgnoreCase("infinite")) {
 											sender.sendMessage(ChatColor.RED + "There was an error in your group settings.");
 											sender.sendMessage(ChatColor.RED + "The death history length has to be an integer or \"infinite.\"");
 											sender.sendMessage(ChatColor.RED + "I'm setting the death history length for players in the \"" + group_name
 													+ "\" group to \"infinite.\"");
 										}
 									}
 								} else if (!first_line)
 									break;
 								if (first_line)
 									first_line = false;
 								save_line = in.readLine();
 							}
 							group_settings.put(group_name, data);
 						}
 					}
 				} else if (parsing.equals("individual")) {
 					if (save_line.split(":").length <= 1 || !save_line.split(":")[0].contains(" ")) {
 						String player_name = "";
 						for (int i = 0; i < save_line.length(); i++) {
 							if (save_line.substring(i, i + 1).equals(":")) {
 								player_name = save_line.substring(0, i);
 								int temp_i = i;
 								i = save_line.length();
 								save_line = save_line.substring(temp_i + 1);
 							}
 						}
 						if (!player_name.equals("") && !player_name.equals("1mAnExampl3")) {
 							Object[] player_data = new Object[default_settings.length];
 							if (use_group_settings && permissions != null) {
 								Object[] group_data = group_settings.get(permissions.getPrimaryGroup((World) null, player_name));
 								if (group_data != null)
 									for (int i = 0; i < group_data.length; i++)
 										player_data[i] = group_data[i];
 								else
 									for (int i = 0; i < default_settings.length; i++)
 										player_data[i] = default_settings[i];
 							}
 							boolean first_line = true;
 							already_progressed = true;
 							while (save_line != null) {
 								if (!first_line)
 									save_line = in.readLine();
 								if (save_line != null) {
 									// eliminate preceding spaces
 									while (save_line.startsWith(" "))
 										save_line = save_line.substring(1);
 									if (save_line.startsWith("Do you want " + player_name + " to be able to teleport to others without asking permission?"))
 										player_data[0] =
 												!getResponse(sender, save_line.substring(72 + player_name.length()), in.readLine(), "Right now, " + player_name
 														+ " can teleport freely.");
 									else if (save_line.startsWith("Do you want " + player_name
 											+ " to be able to teleport other players places without asking permission?"))
 										player_data[1] =
 												!getResponse(sender, save_line.substring(83 + player_name.length()), in.readLine(), "Right now, " + player_name
 														+ " can teleport others to them freely.");
 									else if (save_line.toLowerCase().startsWith("default warp message: "))
 										player_data[2] = save_line.substring(22);
 									else if (save_line.toLowerCase().startsWith("default no warp message: "))
 										player_data[3] = save_line.substring(25);
 									else if (save_line.toLowerCase().startsWith("max warps: ")) {
 										try {
 											player_data[4] = Integer.parseInt(save_line.substring(11));
 										} catch (NumberFormatException exception) {
 											player_data[4] = -1;
 											if (!save_line.substring(11).equalsIgnoreCase("infinite")) {
 												sender.sendMessage(ChatColor.RED + "There was an error in your individual settings.");
 												sender.sendMessage(ChatColor.RED
 														+ "The maximum number of warps that someone can have has to be an integer or \"infinite.\"");
 												sender.sendMessage(ChatColor.RED + "I'm setting the max number of warps for " + player_name
 														+ " to \"infinite.\"");
 											}
 										}
 									} else if (save_line.toLowerCase().startsWith("cool down time: "))
 										player_data[5] = translateStringtoTimeInms(save_line.substring(16));
 									else if (save_line.toLowerCase().startsWith("warp history length: ")) {
 										try {
 											player_data[6] = Integer.parseInt(save_line.substring(21));
 										} catch (NumberFormatException exception) {
 											player_data[6] = -1;
 											if (!save_line.substring(21).equalsIgnoreCase("infinite")) {
 												sender.sendMessage(ChatColor.RED + "There was an error in your individual settings.");
 												sender.sendMessage(ChatColor.RED + "The warp history length has to be an integer or \"infinite.\"");
 												sender.sendMessage(ChatColor.RED + "I'm setting the warp history length for " + player_name
 														+ " to \"infinite.\"");
 											}
 										}
 									} else if (save_line.toLowerCase().startsWith("death history length: ")) {
 										try {
 											player_data[7] = Integer.parseInt(save_line.substring(22));
 										} catch (NumberFormatException exception) {
 											player_data[7] = -1;
 											if (!save_line.substring(22).equalsIgnoreCase("infinite")) {
 												sender.sendMessage(ChatColor.RED + "There was an error in your individual settings.");
 												sender.sendMessage(ChatColor.RED + "The death history length has to be an integer or \"infinite.\"");
 												sender.sendMessage(ChatColor.RED + "I'm setting the death history length for " + player_name
 														+ " to \"infinite.\"");
 											}
 										}
 									} else if (!first_line)
 										break;
 								} else
 									break;
 								if (first_line)
 									first_line = false;
 							}
 							per_player_settings.put(player_name, player_data);
 						}
 					}
 				}
 				if (!already_progressed)
 					save_line = in.readLine();
 				else
 					already_progressed = false;
 			}
 			in.close();
 		} catch (FileNotFoundException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "The config.txt I created a few milliseconds ago doesn't exist. -_-");
 			exception.printStackTrace();
 		} catch (IOException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "I got you a present. It's an IOEcxeption in config.txt.");
 			exception.printStackTrace();
 		}
 		// if a world's spawn message is not configured, use the default
 		for (World world : server.getWorlds()) {
 			if (!spawn_messages_by_world.containsKey(world)) {
 				String world_name = world.getWorldFolder().getName();
 				if (world_name.endsWith("_nether"))
 					world_name = "The Nether";
 				else if (world_name.endsWith("_the_end"))
 					world_name = "The End";
 				spawn_messages_by_world.put(world, "&aWelcome to " + world_name + ", [player].");
 			}
 		}
 		saveTheConfig(sender, false);
 		sender.sendMessage(ChatColor.GREEN + "Your configurations have been loaded.");
 		if (sender instanceof Player)
 			console.sendMessage(ChatColor.GREEN + sender.getName() + " loaded the myUltraWarps config from file.");
 	}
 
 	private void loadTheTemporaryData() {
 		boolean failed = false;
 		// check the temporary file
 		File temp_file = new File(getDataFolder(), "temp.txt");
 		if (temp_file.exists())
 			// read the temp.txt file
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(temp_file));
 				String save_line = in.readLine(), data_type = "", player = "";
 				while (save_line != null) {
 					if (save_line.startsWith("==== "))
 						data_type = save_line.substring(5, save_line.length() - 5);
 					else if (save_line.startsWith("== "))
 						player = save_line.split(" ")[1];
 					else if (data_type.equals("warp histories")) {
 						// if it's a last warp index, save it as that
 						try {
 							last_warp_indexes.put(player, Integer.parseInt(save_line));
 						} catch (NumberFormatException exception) {
 							// otherwise, read it as a warp save line
 							ArrayList<UltraWarp> warp_history = warp_histories.get(player);
 							if (warp_history == null)
 								warp_history = new ArrayList<UltraWarp>();
 							warp_history.add(new UltraWarp(save_line));
 							warp_histories.put(player, warp_history);
 						}
 					} else if (data_type.equals("death histories")) {
 						// if it's a last warp to death index, save it as that
 						try {
 							last_warp_to_death_indexes.put(player, Integer.parseInt(save_line));
 						} catch (NumberFormatException exception) {
 							// otherwise, read it as a death location
 							ArrayList<Location> death_history = death_histories.get(player);
 							if (death_history == null)
 								death_history = new ArrayList<Location>();
 							try {
 								String[] temp = save_line.split(",");
 								World world = null;
 								for (World my_world : server.getWorlds())
 									if (my_world.getWorldFolder().getName().equals(temp[0])) {
 										world = my_world;
 										break;
 									}
 								death_history.add(new Location(world, Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]),
 										Float.parseFloat(temp[4]), Float.parseFloat(temp[5])));
 								death_histories.put(player, death_history);
 							} catch (NumberFormatException exception2) {
 								console.sendMessage(ChatColor.DARK_RED + "There was a problem reading the death history from the temporary data file!");
 							}
 						}
 					} else if (data_type.equals("blocked players")) {
 						ArrayList<String> replacement = blocked_players.get(player);
 						if (replacement == null)
 							replacement = new ArrayList<String>();
 						replacement.add(save_line);
 						blocked_players.put(player, replacement);
 					} else if (data_type.equals("cool down times")) {
 						try {
 							cooling_down_players.put(player, Long.parseLong(save_line));
 						} catch (NumberFormatException exception) {
 							console.sendMessage(ChatColor.DARK_RED + "There was an error in loading the cool down time data for " + player
 									+ " from the temporary file!");
 						}
 					}
 					save_line = in.readLine();
 				}
 				in.close();
 			} catch (FileNotFoundException exception) {
 				console.sendMessage(ChatColor.DARK_RED + "The temp.txt I created a few milliseconds ago doesn't exist. -_-");
 				exception.printStackTrace();
 			} catch (IOException exception) {
 				console.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save the temporary data.");
 				exception.printStackTrace();
 				failed = true;
 			}
 		if (!failed)
 			temp_file.delete();
 	}
 
 	// saving
 	private void saveTheWarps(CommandSender sender, boolean display_message) {
 		boolean failed = false;
 		// check the warps file
 		File warps_file = new File(getDataFolder(), "warps.txt");
 		if (!warps_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				sender.sendMessage(ChatColor.GREEN + "I couldn't find a warps.txt file. I'll make a new one.");
 				warps_file.createNewFile();
 			} catch (IOException exception) {
 				sender.sendMessage(ChatColor.DARK_RED + "I couldn't create a warps.txt file! Oh nos!");
 				exception.printStackTrace();
 				failed = true;
 			}
 		}
 		// save the warps
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(warps_file));
 			for (int i = 0; i < warps.size(); i++) {
 				out.write(warps.get(i).getSaveLine());
 				if (i < warps.size() - 1)
 					out.newLine();
 			}
 			out.flush();
 			out.close();
 		} catch (IOException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your warps.");
 			exception.printStackTrace();
 			failed = true;
 		}
 		if (!failed && display_message) {
 			if (warps.size() > 1)
 				sender.sendMessage(ChatColor.GREEN + "Your " + warps.size() + " warps have been saved.");
 			else if (warps.size() == 1)
 				sender.sendMessage(ChatColor.GREEN + "Your 1 warp has been saved.");
 			else
 				sender.sendMessage(ChatColor.GREEN + "You have no warps to save!");
 			if (sender instanceof Player)
 				if (warps.size() > 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " saved " + warps.size() + " warps to file.");
 				else if (warps.size() == 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " saved the server's 1 warp to file.");
 				else
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName()
 							+ " tried to save the server's warps to file, but there were no warps on the server to save.");
 		}
 	}
 
 	private void saveTheSwitches(CommandSender sender, boolean display_message) {
 		boolean failed = false;
 		// check the switches file
 		File switches_file = new File(getDataFolder(), "switches.txt");
 		if (!switches_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				sender.sendMessage(ChatColor.GREEN + "I couldn't find a switches.txt file. I'll make a new one.");
 				switches_file.createNewFile();
 			} catch (IOException exception) {
 				sender.sendMessage(ChatColor.DARK_RED + "I couldn't create a switches.txt file! Oh nos!");
 				exception.printStackTrace();
 				failed = true;
 			}
 		}
 		// save the switches
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(switches_file));
 			for (UltraSwitch my_switch : switches) {
 				out.write(my_switch.getSaveLine());
 				out.newLine();
 			}
 			out.flush();
 			out.close();
 		} catch (IOException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your switches.");
 			exception.printStackTrace();
 			failed = true;
 		}
 		if (!failed && display_message) {
 			if (switches.size() > 1)
 				sender.sendMessage(ChatColor.GREEN + "Your " + switches.size() + " switches have been saved.");
 			else if (switches.size() == 1)
 				sender.sendMessage(ChatColor.GREEN + "Your 1 switch has been saved.");
 			else
 				sender.sendMessage(ChatColor.GREEN + "You have no switches to save!");
 			if (sender instanceof Player)
 				if (switches.size() > 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " saved " + switches.size() + " switches to file.");
 				else if (switches.size() == 1)
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " saved the server's 1 switch to file.");
 				else
 					console.sendMessage(ChatColor.GREEN + ((Player) sender).getName()
 							+ " tried to save the server's warps to file, but there were no switches on the server to save.");
 		}
 	}
 
 	private void saveTheConfig(CommandSender sender, boolean display_message) {
 		boolean failed = false;
 		// check the config file
 		File config_file = new File(getDataFolder(), "config.txt");
 		if (!config_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				sender.sendMessage(ChatColor.GREEN + "I couldn't find a config.txt file. I'll make a new one.");
 				config_file.createNewFile();
 			} catch (IOException exception) {
 				sender.sendMessage(ChatColor.DARK_RED + "I couldn't create a config.txt file! Oh nos!");
 				exception.printStackTrace();
 				failed = true;
 			}
 		}
 		// save the configurations
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(config_file));
 			out.write("Remember to use /myUltraWarps save the config before you modify this file if autosave is off and use /myUltraWarps load the config when you're done.");
 			out.newLine();
 			out.newLine();
 			out.write("Do you want to be able to change settings for permissions-based groups of users? ");
 			out.newLine();
 			if (use_group_settings)
 				if (Vault == null)
 					out.write("   Group settings are enabled, but you don't have Vault! You need Vault and a compatible permissions plugin for group settings to work.");
 				else if (permissions == null)
 					out.write("   Group settings are enabled, but you don't have a permissions plugin compatible with Vault! You need one for group settings to work.");
 				else
 					out.write("   Group settings are enabled right now.");
 			else {
 				out.write("   Group settings are disabled right now.");
 				out.newLine();
 				out.write("   You need Vault and a compatible permissions plugin in order for this to work.");
 			}
 			out.newLine();
 			out.newLine();
 			out.write("Do you want myUltraWarps to check for updates every time it is enabled? ");
 			out.newLine();
 			if (auto_update)
 				out.write("   Right now, myUltraWarps will auto-update.");
 			else
 				out.write("   Right now, myUltraWarps will not auto-update! I REALLY think you should let it auto-update!");
 			out.newLine();
 			out.newLine();
 			out.write("Do you want myUltraWarps to automatically save the warps file every time a change is made? ");
 			out.newLine();
 			if (autosave_warps)
 				out.write("   Right now, autosave is on for warps.");
 			else
 				out.write("   Right now, autosave is off for warps.");
 			out.newLine();
 			out.write("Do you want myUltraWarps to automatically save the switches file every time a change is made? ");
 			out.newLine();
 			if (autosave_switches)
 				out.write("   Right now, autosave is on for switches.");
 			else
 				out.write("   Right now, autosave is off for switches.");
 			out.newLine();
 			out.write("Do you want myUltraWarps to automatically save the config file every time a change is made? ");
 			out.newLine();
 			if (autosave_config)
 				out.write("   Right now, autosave is on for the config.");
 			else
 				out.write("   Right now, autosave is off for the config.");
 			out.newLine();
 			out.newLine();
 			out.write("You can set the messages that appear when someone teleports to the spawn point for each world here.");
 			out.newLine();
 			for (World world : server.getWorlds()) {
 				String spawn_message = spawn_messages_by_world.get(world);
 				if (spawn_message != null) {
 					String world_name = world.getWorldFolder().getName();
 					if (world_name.endsWith("_nether"))
 						world_name = world_name.substring(0, world_name.length() - 7) + " (The Nether)";
 					else if (world_name.endsWith("_the_end"))
 						world_name = world_name.substring(0, world_name.length() - 8) + " (The End)";
 					out.write("     " + world_name + ": " + spawn_message);
 					out.newLine();
 				}
 			}
 			out.newLine();
 			out.write("global settings:");
 			out.newLine();
 			out.write("     Do you want players to be able to teleport to one another without asking permission? ");
 			out.newLine();
 			if (!(Boolean) default_settings[0])
 				out.write("        Right now, players can teleport to each other freely.");
 			else
 				out.write("        Right now, players normally have to request teleportation to the target player.");
 			out.newLine();
 			out.write("     Do you want players to be able to teleport other players places without asking permission? ");
 			out.newLine();
 			if (!(Boolean) default_settings[1])
 				out.write("        Right now, players can teleport each other to them freely.");
 			else
 				out.write("        Right now, players normally have to request that other players teleport to them.");
 			out.newLine();
 			out.write("     default warp message: " + default_settings[2]);
 			out.newLine();
 			out.write("     default no warp message: " + default_settings[3]);
 			out.newLine();
 			if ((Integer) default_settings[4] != -1)
 				out.write("     max warps: " + default_settings[4]);
 			else
 				out.write("     max warps: infinite");
 			out.newLine();
 			out.write("     cool down time: " + translateTimeInmsToString((Integer) default_settings[5], false));
 			out.newLine();
 			if ((Integer) default_settings[6] != -1)
 				out.write("     warp history length: " + default_settings[6]);
 			else
 				out.write("     warp history length: infinite");
 			out.newLine();
 			if ((Integer) default_settings[7] != -1)
 				out.write("     death history length: " + default_settings[7]);
 			else
 				out.write("     death history length: infinite");
 			out.newLine();
 			out.newLine();
 			if (use_group_settings && permissions != null) {
 				out.write("group settings:");
 				out.newLine();
 				if (permissions.getGroups() != null && permissions.getGroups().length > 0) {
 					for (int i = 0; i < permissions.getGroups().length; i++) {
 						if (!permissions.getGroups()[i].equals("default")) {
 							out.write("     " + permissions.getGroups()[i] + ":");
 							out.newLine();
 							Object[] data = group_settings.get(permissions.getGroups()[i]);
 							if (data == null)
 								data = default_settings;
 							out.write("          Do you want players in this group to be able to teleport to one another without asking permission? ");
 							out.newLine();
 							if (!(Boolean) data[0])
 								out.write("             Right now, players in this group can teleport to each other freely.");
 							else
 								out.write("             Right now, players in this group normally have to request teleportation to the target player.");
 							out.newLine();
 							out.write("          Do you want players in this group to be able to teleport other players places without asking permission? ");
 							out.newLine();
 							if (!(Boolean) data[1])
 								out.write("             Right now, players in this group can teleport each other to them freely.");
 							else
 								out.write("             Right now, players in this group normally have to request that other players teleport to them.");
 							out.newLine();
 							out.write("          default warp message: " + data[2]);
 							out.newLine();
 							out.write("          default no warp message: " + data[3]);
 							out.newLine();
 							if ((Integer) (data[4]) != -1)
 								out.write("          max warps: " + data[4]);
 							else
 								out.write("          max warps: infinite");
 							out.newLine();
 							out.write("          cool down time: " + translateTimeInmsToString((Integer) data[5], false));
 							out.newLine();
 							if ((Integer) (data[6]) != -1)
 								out.write("          warp history length: " + data[6]);
 							else
 								out.write("          warp history length: infinite");
 							out.newLine();
 							if ((Integer) (data[7]) != -1)
 								out.write("          death history length: " + data[7]);
 							else
 								out.write("          death history length: infinite");
 							out.newLine();
 						}
 					}
 				}
 				out.newLine();
 			} else if (use_group_settings && permissions == null) {
 				out.write("You need Vault to change group settings!");
 				out.newLine();
 				out.newLine();
 			}
 			out.write("individual settings:");
 			out.newLine();
 			if (per_player_settings.size() == 0)
 				per_player_settings.put("1mAnExampl3", default_settings);
 			else if (per_player_settings.containsKey("1mAnExampl3") && per_player_settings.size() > 1)
 				per_player_settings.remove("1mAnExampl3");
 			for (int i = 0; i < per_player_settings.size(); i++) {
 				String player_name = (String) per_player_settings.keySet().toArray()[i];
 				out.write("     " + player_name + ":");
 				out.newLine();
 				Object[] player_data = per_player_settings.get(player_name);
 				out.write("          Do you want " + player_name + " to be able to teleport to others without asking permission? ");
 				out.newLine();
 				if (!(Boolean) player_data[0])
 					out.write("             Right now, " + player_name + " can teleport freely.");
 				else
 					out.write("             Right now, " + player_name + " normally has to request teleportation from the target player.");
 				out.newLine();
 				out.write("          Do you want " + player_name + " to be able to teleport other players places without asking permission?");
 				out.newLine();
 				if (!(Boolean) player_data[1])
 					out.write("             Right now, " + player_name + " can teleport others to them freely.");
 				else
 					out.write("             Right now, " + player_name + " normally has to request that other players teleport to them.");
 				out.newLine();
 				out.write("          default warp message: " + player_data[2]);
 				out.newLine();
 				out.write("          default no warp message: " + player_data[3]);
 				out.newLine();
 				if ((Integer) player_data[4] != -1)
 					out.write("          max warps: " + player_data[4]);
 				else
 					out.write("          max warps: infinite");
 				out.newLine();
 				out.write("          cool down time: " + translateTimeInmsToString((Integer) player_data[5], false));
 				out.newLine();
 				if ((Integer) player_data[6] != -1)
 					out.write("          warp history length: " + player_data[6]);
 				else
 					out.write("          warp history length: infinite");
 				out.newLine();
 				if ((Integer) player_data[7] != -1)
 					out.write("          death history length: " + player_data[7]);
 				else
 					out.write("          death history length: infinite");
 				out.newLine();
 			}
 			out.flush();
 			out.close();
 		} catch (IOException exception) {
 			sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your configurations.");
 			exception.printStackTrace();
 			failed = true;
 		}
 		if (!failed && display_message) {
 			sender.sendMessage(ChatColor.GREEN + "Your configurations have been saved.");
 			if (sender instanceof Player)
 				console.sendMessage(ChatColor.GREEN + ((Player) sender).getName() + " saved the server's configurations to file.");
 		}
 	}
 
 	private void saveTheTemporaryData() {
 		boolean failed = false;
 		// check the temporary file
 		File temp_file = new File(getDataFolder(), "temp.txt");
 		if (!temp_file.exists()) {
 			getDataFolder().mkdir();
 			try {
 				temp_file.createNewFile();
 			} catch (IOException exception) {
 				console.sendMessage(ChatColor.DARK_RED + "I couldn't create a temp.txt file! Oh nos!");
 				exception.printStackTrace();
 				failed = true;
 			}
 		}
 		if (!failed)
 			// save the warp and death histories
 			try {
 				BufferedWriter out = new BufferedWriter(new FileWriter(temp_file));
 				out.write("==== warp histories ====");
 				out.newLine();
 				for (String key : warp_histories.keySet()) {
 					out.write("== " + key + " ==");
 					out.newLine();
 					for (UltraWarp warp : warp_histories.get(key)) {
 						if (warp == null)
 							console.sendMessage(ChatColor.DARK_RED + "WTF! Warp is null!");
 						out.write(warp.getSaveLine());
 						out.newLine();
 					}
 					if (last_warp_indexes.get(key) != null) {
 						out.write("" + (Integer) last_warp_indexes.get(key));
 						out.newLine();
 					}
 				}
 				out.write("==== death histories ====");
 				out.newLine();
 				for (String key : death_histories.keySet()) {
 					out.write("== " + key + " ==");
 					out.newLine();
 					for (Location death : death_histories.get(key)) {
 						out.write(death.getWorld().getName() + "," + death.getX() + "," + death.getY() + "," + death.getZ() + "," + death.getYaw() + ","
 								+ death.getPitch());
 						out.newLine();
 					}
 					if (last_warp_to_death_indexes.get(key) != null) {
 						out.write(last_warp_to_death_indexes.get(key));
 						out.newLine();
 					}
 				}
 				out.write("==== blocked players ====");
 				out.newLine();
 				for (String key : blocked_players.keySet())
 					if (blocked_players.get(key) != null && blocked_players.get(key).size() > 0) {
 						out.write("== " + key + " ==");
 						out.newLine();
 						for (String blocked_player : blocked_players.get(key)) {
 							out.write(blocked_player);
 							out.newLine();
 						}
 					}
 				out.write("==== cool down times ====");
 				out.newLine();
 				for (String key : cooling_down_players.keySet())
 					if (cooling_down_players.get(key) != null) {
 						out.write("== " + key + " ==");
 						out.newLine();
 						out.write("" + cooling_down_players.get(key));
 						out.newLine();
 					}
 				out.flush();
 				out.close();
 			} catch (IOException exception) {
 				console.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your temporary data.");
 				exception.printStackTrace();
 				failed = true;
 			}
 	}
 
 	// plugin commands
 	private void displayHelp(CommandSender sender) {
 		// establish the number of characters in a page
 		int lines_per_page = 27;
 		if (sender instanceof Player)
 			lines_per_page = 10;
 		// read the requested page number
 		int page_number = 0;
 		int extra_param = 0;
 		if (parameters.length > 0 && parameters[0].equalsIgnoreCase("help"))
 			extra_param++;
 		if (parameters.length > extra_param)
 			try {
 				page_number = Integer.parseInt(parameters[extra_param]);
 			} catch (NumberFormatException exception) {
 				sender.sendMessage(ChatColor.RED + "How long has \"" + parameters[extra_param] + "\" been an integer? I must really be out of the loop.");
 			}
 		if (page_number == 0) {
 			sender.sendMessage(colorCode("&f\"()\" indicate an optional parameter while \"[]\" indicate a required parameter. If a parameter is in quotes, it means that that word itself is the parameter; otherwise, substitute the piece of data for the parameter. Almost everything is not case-sensitive and you can use even just a single letter to search for it. For example, instead of typing &a&o/warp Play3r's house&f, you can just type &a&o/warp p's h &fif you like. Use &o&a/mUW help [page #] &fto go through the help pages. &cT&fh&ca&ft&c'&fs &ca&fl&cl&f, &cf&fo&cl&fk&cs&f!"));
 		} else if (page_number < 0) {
 			sender.sendMessage(ChatColor.RED + "Last time I checked, negative page numbers don't make sense.");
 		} else {
 			// do it
 			int number_of_lines = 0;
 			ArrayList<String> pages = new ArrayList<String>();
 			String current_page = "";
 			for (int i = 0; i < help_pages.size(); i++) {
 				String help_line = (String) help_pages.get(i)[0];
 				String basic_permission_node = (String) help_pages.get(i)[1];
 				boolean included_with_user = (Boolean) help_pages.get(i)[2];
 				int chat_lines_in_this_line = (Integer) help_pages.get(i)[3];
 				if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")
 						|| (included_with_user && sender.hasPermission("myultrawarps.user")) || sender.hasPermission(basic_permission_node)
 						|| sender.hasPermission(basic_permission_node + ".other")) {
 					if (i != 0)
 						current_page = current_page + "\n";
 					number_of_lines = number_of_lines + chat_lines_in_this_line;
 					if (number_of_lines > lines_per_page) {
 						pages.add(current_page);
 						current_page = "";
 						number_of_lines = chat_lines_in_this_line;
 					}
 					current_page = current_page + help_line;
 				}
 			}
 			pages.add(current_page);
 			if (page_number > pages.size())
 				if (pages.size() == 1)
 					sender.sendMessage(ChatColor.RED + "There is only one help page for you.");
 				else
 					sender.sendMessage(ChatColor.RED + "There are only " + pages.size() + " help pages for you.");
 			else
 				sender.sendMessage(colorCode(pages.get(page_number - 1)));
 		}
 	}
 
 	private void back(CommandSender sender) {
 		Player player = (Player) sender;
 		int amount = 1;
 		if (parameters.length > 0)
 			try {
 				amount = Integer.parseInt(parameters[0]);
 				if (amount == 0) {
 					sender.sendMessage(ChatColor.GREEN + "Well, here you are. You went back 0 warps through your history.");
 					return;
 				} else if (amount < 0) {
 					sender.sendMessage(ChatColor.RED + "Going back backwards.... Sorry, but I can't see into the future. At least...not that far ahead.");
 					return;
 				}
 			} catch (NumberFormatException exception) {
 				sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
 				return;
 			}
 		ArrayList<UltraWarp> my_warp_histories = warp_histories.get(player.getName());
 		Integer last_warp_index = last_warp_indexes.get(player.getName());
 		UltraWarp last_warp = null;
 		if (my_warp_histories != null && last_warp_index >= amount)
 			last_warp = my_warp_histories.get(last_warp_index - amount);
 		else {
 			if (my_warp_histories == null || my_warp_histories.size() == 0)
 				sender.sendMessage(ChatColor.RED + "You haven't warped anywhere yet!");
 			else if (last_warp_index > 1)
 				sender.sendMessage(ChatColor.RED + "You can only go back " + last_warp_index + " more warps.");
 			else if (last_warp_index == 1)
 				sender.sendMessage(ChatColor.RED + "You can only go back one more warp.");
 			else
 				sender.sendMessage(ChatColor.RED + "Sorry, but I don't keep track of that many warps. This is as far back as you can go.");
 			return;
 		}
 		if (last_warp != null) {
 			boolean player_is_listed = false;
 			if (last_warp.getListedUsers() != null)
 				for (String listed_player : last_warp.getListedUsers())
 					if (listed_player.equals(player.getName()))
 						player_is_listed = true;
 			if (player.hasPermission("myultrawarps.admin") || player.hasPermission("myultrawarps.warptowarp.other")
 					|| last_warp.getOwner().equals(player.getName()) || (!last_warp.isRestricted() && !player_is_listed)
 					|| (last_warp.isRestricted() && player_is_listed)) {
 				teleport(player, null, last_warp, true, null);
 				last_warp_indexes.put(player.getName(), last_warp_index - amount);
 			} else
 				player.sendMessage(colorCode(replaceAll(last_warp.getNoWarpMessage(), "[player]", player.getName())));
 		}
 	}
 
 	private void block(CommandSender sender) {
 		Player player = (Player) sender;
 		String blocked_player = null;
 		Player target_player = null;
 		for (Player my_player : server.getOnlinePlayers())
 			if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()) && !my_player.getName().equals(player.getName())) {
 				target_player = my_player;
 				blocked_player = my_player.getName();
 				break;
 			}
 		if (blocked_player == null)
 			for (OfflinePlayer my_player : server.getOfflinePlayers())
 				if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()) && !my_player.getName().equals(player.getName())) {
 					blocked_player = my_player.getName();
 					break;
 				}
 		if (blocked_player == null)
 			blocked_player = parameters[0];
 		if (target_player == null || !target_player.hasPermission("myultrawarps.admin")) {
 			ArrayList<String> my_blocked_players = blocked_players.get(player.getName());
 			if (my_blocked_players == null)
 				my_blocked_players = new ArrayList<String>();
 			my_blocked_players.add(blocked_player);
 			blocked_players.put(player.getName(), my_blocked_players);
 			sender.sendMessage(ChatColor.GREEN + blocked_player + " can no longer send you teleportation requests.");
 			// remove all to and from teleportation requests to this player from the now blocked player
 			ArrayList<String> requesting_players = from_teleport_requests.get(blocked_player);
 			if (requesting_players == null)
 				requesting_players = new ArrayList<String>();
 			else {
 				String remove = requesting_players.get(0);
 				while (requesting_players.contains(remove))
 					requesting_players.remove(remove);
 			}
 			from_teleport_requests.put(blocked_player, requesting_players);
 			requesting_players = to_teleport_requests.get(blocked_player);
 			if (requesting_players == null)
 				requesting_players = new ArrayList<String>();
 			else {
 				String remove = requesting_players.get(0);
 				while (requesting_players.contains(remove))
 					requesting_players.remove(remove);
 			}
 			to_teleport_requests.put(blocked_player, requesting_players);
 		} else if (blocked_player != null && target_player != null)
 			player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName()
 					+ " is a myUltraWarps admin. They have power over everything myUltraWarps-related and you're not allowed to block them.");
 		else if (player.getName().toLowerCase().startsWith(blocked_player))
 			player.sendMessage(ChatColor.RED + "Now why would you want to block yourself?");
 	}
 
 	private void blockList(int extra_param, CommandSender sender) {
 		String target = sender.getName();
 		if (parameters.length > extra_param)
 			target = getFullName(parameters[extra_param]);
 		if (target != null && blocked_players.get(target) != null && blocked_players.get(target).size() > 0
 				&& (target.equals(sender.getName()) || sender.hasPermission("myultrawarps.blocklist.other") || sender.hasPermission("myultrawarps.admin"))) {
 			ArrayList<String> players = blocked_players.get(target);
 			String list = players.get(0);
 			if (players.size() == 2)
 				list = list + " and " + players.get(1);
 			else if (players.size() > 2)
 				for (int i = 1; i < players.size(); i++) {
 					list = list + ", ";
 					if (i == players.size() - 1)
 						list = list + "and ";
 					list = list + players.get(i);
 				}
 			if (target.equals(sender.getName()))
 				sender.sendMessage(ChatColor.GREEN + "You've blocked " + list + ".");
 			else
 				sender.sendMessage(ChatColor.GREEN + target + " blocked " + list + ".");
 		} else if (!target.equals(sender.getName()) && !sender.hasPermission("myultrawarps.blocklist.other") && !sender.hasPermission("myultrawarps.admin")) {
 			sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to see who other people blocked.");
 		} else if (target.equals(sender.getName()))
 			sender.sendMessage(ChatColor.GREEN + "You haven't blocked anyone yet.");
 		else
 			sender.sendMessage(ChatColor.GREEN + target + " hasn't blocked anyone yet.");
 	}
 
 	private void createWarp(int extra_param, CommandSender sender) {
 		Player player = (Player) sender;
 		// establish all the default values
 		boolean listed = false, restricted = true;
 		// find the user's global, group-specific, or individual settings
 		Object[] data = default_settings;
 		if (permissions != null && permissions.getPrimaryGroup(player) != null && group_settings.containsKey(permissions.getPrimaryGroup(player)))
 			data = group_settings.get(permissions.getPrimaryGroup(player));
 		if (per_player_settings.containsKey(player.getName()))
 			data = per_player_settings.get(player.getName());
 		String owner = player.getName(), warp_message =
 				replaceAll(replaceAll((String) data[2], "[warp]", replaceAll(parameters[extra_param], "_", " ")), "[owner]", owner), no_warp_message =
 				replaceAll(replaceAll((String) data[3], "[warp]", replaceAll(parameters[extra_param], "_", " ")), "[owner]", owner);
 		String[] listed_users = null;
 		boolean player_is_owner = true;
 		parsing_warp_message = false;
 		parsing_no_warp_message = false;
 		// search the parameters for non-default characteristics
 		for (int j = extra_param + 1; j < parameters.length; j++) {
 			if (parameters[j].toLowerCase().startsWith("type:o")) {
 				listed = true;
 				restricted = false;
 			} else if (parameters[j].toLowerCase().startsWith("type:s")) {
 				listed = false;
 				restricted = false;
 			} else if (parameters[j].toLowerCase().startsWith("type:a")) {
 				listed = true;
 				restricted = true;
 				stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
 			} else if (parameters[j].toLowerCase().startsWith("type:p")) {
 				listed = false;
 				restricted = true;
 				stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
 			} else if (parameters[j].toLowerCase().startsWith("warp:")) {
 				warp_message = parameters[j].substring(5);
 				stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
 				parsing_warp_message = true;
 			} else if (parameters[j].toLowerCase().startsWith("nowarp:")) {
 				no_warp_message = parameters[j].substring(7);
 				stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
 			} else if (parameters[j].toLowerCase().startsWith("giveto:")) {
 				String temp_old_owner = owner;
 				owner = getFullName(parameters[j].substring(7));
 				player_is_owner = player != null && player.getName().equals(owner);
 				// update the warp and no warp messages
 				if (warp_message.contains(temp_old_owner))
 					warp_message = replaceAll(warp_message, temp_old_owner, owner);
 				if (no_warp_message.contains(temp_old_owner))
 					no_warp_message = replaceAll(no_warp_message, temp_old_owner, owner);
 				stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
 			} else if (parameters[j].toLowerCase().startsWith("list:")) {
 				stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
 				listed_users = parameters[j].substring(5).split(",");
 				if (listed_users.length > 0 && !(listed_users.length == 1 && listed_users[0].equals("")))
 					// retrieve full player names
 					for (int i = 0; i < listed_users.length; i++)
 						listed_users[i] = getFullName(listed_users[i]);
 			} else if (parsing_warp_message)
 				warp_message = warp_message + " " + parameters[j];
 			else if (parsing_no_warp_message)
 				no_warp_message = no_warp_message + " " + parameters[j];
 		}
 		if (listed_users == null)
 			listed_users = new String[0];
 		// see if the user has reached the maximum number of warps they're
 		// allowed to have
 		boolean maxed_out = false;
 		int specific_max_warps = (Integer) data[4];
 		if (specific_max_warps != -1) {
 			int number_of_warps = 0;
 			for (UltraWarp warp : warps) {
 				if (warp.getOwner().equalsIgnoreCase(player.getName()))
 					number_of_warps++;
 			}
 			if (number_of_warps >= specific_max_warps)
 				maxed_out = true;
 		}
 		if ((!maxed_out || player.hasPermission("myultrawarps.admin"))
 				&& !parameters[extra_param].equalsIgnoreCase("info")
 				&& !parameters[extra_param].equalsIgnoreCase("all")
 				&& !parameters[extra_param].equalsIgnoreCase("list")
 				&& !parameters[extra_param].toLowerCase().endsWith("'s")
 				&& (player.getName().toLowerCase().startsWith(owner.toLowerCase()) || player.hasPermission("myultrawarps.create.other") || player
 						.hasPermission("myultrawarps.admin"))) {
 			// delete the old warp if it exists
 			for (int i = 0; i < warps.size(); i++)
 				if (warps.get(i).getOwner().equals(owner) && warps.get(i).getName().equals(parameters[extra_param]))
 					warps.remove(i);
 			// find out where the new warp needs to be in the list to be
 			// properly alphabetized
 			int insertion_index = 0;
 			for (UltraWarp warp : warps)
 				if (warp.getName().compareToIgnoreCase(parameters[extra_param]) < 0
 						|| (warp.getName().compareToIgnoreCase(parameters[extra_param]) == 0 && warp.getOwner().compareToIgnoreCase(owner) <= 0))
 					insertion_index++;
 			// create the warp
 			warps.add(insertion_index, new UltraWarp(owner, parameters[extra_param], listed, restricted, warp_message, no_warp_message, listed_users, player
 					.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getPitch(), player.getLocation()
 					.getYaw(), player.getLocation().getWorld()));
 			if (autosave_warps)
 				saveTheWarps(sender, false);
 			if (player.getName().toLowerCase().startsWith(owner.toLowerCase()))
 				player.sendMessage(ChatColor.GREEN + "You made a warp called \"" + parameters[extra_param] + ".\"");
 			else
 				player.sendMessage(ChatColor.GREEN + "You made a warp called \"" + parameters[extra_param] + "\" for " + owner + ".");
 		} else if (parameters[extra_param].equalsIgnoreCase("info"))
 			sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"info\" because it interferes with the command " + ChatColor.GREEN
 					+ "/warp info" + ChatColor.RED + ".");
 		else if (parameters[extra_param].equalsIgnoreCase("all"))
 			sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"all\" because it interferes with the command " + ChatColor.GREEN
 					+ "/warp all" + ChatColor.RED + ".");
 		else if (parameters[extra_param].equalsIgnoreCase("list"))
 			sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"list\" because it interferes with the command " + ChatColor.GREEN
 					+ "/warp list" + ChatColor.RED + ".");
 		else if (parameters[extra_param].toLowerCase().endsWith("'s"))
 			sender.sendMessage(ChatColor.RED
 					+ "Sorry, but you can't make a warp with a name ending in \"'s\" because I check for that to see whether you're specifying an owner or just giving a warp name alone and if the warp name has that \"'s\", I get very confused.");
 		else if (!player.getName().equalsIgnoreCase(owner)
 				&& !(player.hasPermission("myultrawarps.create.other") || player.hasPermission("myultrawarps.admin"))) {
 			// check if the player receiving the warp already has that warp
 			boolean warp_already_exists = false;
 			for (int i = 0; i < warps.size(); i++)
 				if (warps.get(i).getOwner().toLowerCase().startsWith(owner.toLowerCase())
 						&& warps.get(i).getName().toLowerCase().startsWith(parameters[extra_param].toLowerCase()))
 					warp_already_exists = true;
 			if (!warp_already_exists) {
 				// find out where the new warp needs to be in the list to be
 				// properly alphabetized
 				int insertion_index = 0;
 				for (UltraWarp warp : warps)
 					if (warp.getName().compareToIgnoreCase(parameters[extra_param]) < 0
 							|| (warp.getName().compareToIgnoreCase(parameters[extra_param]) == 0 && warp.getOwner().compareToIgnoreCase(owner) <= 0))
 						insertion_index++;
 				// create the warp
 				warps.add(insertion_index, new UltraWarp(owner, parameters[extra_param], listed, restricted, warp_message, no_warp_message, listed_users,
 						player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getPitch(), player
 								.getLocation().getYaw(), player.getLocation().getWorld()));
 				player.sendMessage(ChatColor.GREEN + "You made a warp called \"" + parameters[extra_param] + "\" for " + owner + ".");
 			} else
 				player.sendMessage(ChatColor.RED + owner + " already has a warp called \"" + parameters[extra_param]
 						+ "\" and you're not allowed to overwrite it.");
 		} else if (maxed_out)
 			player.sendMessage(ChatColor.RED + "Sorry, but you're only allowed to create " + (Integer) data[4]
 					+ " warps and you've already reached your limit.");
 	}
 
 	private void changeWarp(int extra_param, CommandSender sender) {
 		// [changewarp/change warp/modifywarp/modify warp]
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		UltraWarp warp = locateWarp(extra_param, sender);
 		if (warp != null) {
 			// search through the parameters for info changes
 			boolean listed = warp.isListed(), restricted = warp.isRestricted(), old_listed = warp.isListed(), old_restricted = warp.isRestricted();
 			String warp_message = warp.getWarpMessage(), no_warp_message = warp.getNoWarpMessage(), old_warp_message = warp.getWarpMessage(), old_no_warp_message =
 					warp.getNoWarpMessage(), owner = warp.getOwner(), name = warp.getName(), old_owner = warp.getOwner(), old_name = warp.getName(), result_message =
 					"";
 			String[] listed_users = warp.getListedUsers(), old_listed_users = warp.getListedUsers();
 			boolean player_is_owner = false;
 			if (player != null && player.getName().toLowerCase().startsWith(owner.toLowerCase()))
 				player_is_owner = true;
 			parsing_warp_message = false;
 			parsing_no_warp_message = false;
 			for (int j = extra_param + 1; j < parameters.length; j++) {
 				if (parameters[j].toLowerCase().startsWith("type:o")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					listed = true;
 					restricted = false;
 					if (!result_message.equals(""))
 						result_message = result_message + "\n";
 					if (player_is_owner)
 						result_message =
 								result_message + ChatColor.GREEN + "\"" + name + "\" is now an " + ChatColor.WHITE + "open " + ChatColor.GREEN + "warp.";
 					else
 						result_message =
 								result_message + ChatColor.GREEN + owner + "'s \"" + name + "\" is now an " + ChatColor.WHITE + "open " + ChatColor.GREEN
 										+ "warp.";
 				} else if (parameters[j].toLowerCase().startsWith("type:s")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					listed = false;
 					restricted = false;
 					if (!result_message.equals(""))
 						result_message = result_message + "\n";
 					if (player_is_owner)
 						result_message =
 								result_message + ChatColor.GREEN + "\"" + name + "\" is now a " + ChatColor.GRAY + "secret " + ChatColor.GREEN + "warp.";
 					else
 						result_message =
 								result_message + ChatColor.GREEN + owner + "'s \"" + name + "\" is now a " + ChatColor.GRAY + "secret " + ChatColor.GREEN
 										+ "warp.";
 				} else if (parameters[j].toLowerCase().startsWith("type:a")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					listed = true;
 					restricted = true;
 					if (!result_message.equals(""))
 						result_message = result_message + "\n";
 					if (player_is_owner)
 						result_message =
 								result_message + ChatColor.GREEN + "\"" + name + "\" is now an " + ChatColor.RED + "advertised " + ChatColor.GREEN + "warp.";
 					else
 						result_message =
 								result_message + ChatColor.GREEN + owner + "'s \"" + name + "\" is now an " + ChatColor.RED + "advertised " + ChatColor.GREEN
 										+ "warp.";
 				} else if (parameters[j].toLowerCase().startsWith("type:p")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					listed = false;
 					restricted = true;
 					if (!result_message.equals(""))
 						result_message = result_message + "\n";
 					if (player_is_owner)
 						result_message =
 								result_message + ChatColor.GREEN + "\"" + name + "\" is now a " + ChatColor.DARK_RED + "private " + ChatColor.GREEN + "warp.";
 					else
 						result_message =
 								result_message + ChatColor.GREEN + owner + "'s \"" + name + "\" is now a " + ChatColor.DARK_RED + "private " + ChatColor.GREEN
 										+ "warp.";
 				} else if (parameters[j].toLowerCase().startsWith("name:")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					String temp_old_name = name;
 					name = parameters[j].substring(5);
 					if (!name.equalsIgnoreCase("info") && !name.equalsIgnoreCase("all") && !name.equalsIgnoreCase("list")) {
 						if (!result_message.equals(""))
 							result_message = result_message + "\n";
 						if (player_is_owner)
 							result_message = result_message + ChatColor.GREEN + "\"" + old_name + "\" has been renamed \"" + name + ".\"";
 						else
 							result_message = result_message + ChatColor.GREEN + owner + "'s \"" + old_name + "\" has been renamed \"" + name + ".\"";
 						// update the warp and no warp messages
 						boolean updated_warp_message = false, updated_no_warp_message = false;
 						String temp_old_message_name = replaceAll(temp_old_name, "_", " "), message_name = replaceAll(name, "_", " ");
 						if (warp_message.contains(temp_old_message_name)) {
 							warp_message = replaceAll(warp_message, temp_old_message_name, message_name);
 							updated_warp_message = true;
 						}
 						if (no_warp_message.contains(temp_old_message_name)) {
 							no_warp_message = replaceAll(no_warp_message, temp_old_message_name, message_name);
 							updated_no_warp_message = true;
 						}
 						if (!result_message.equals(""))
 							result_message = result_message + "\n";
 						if (updated_warp_message)
 							if (updated_no_warp_message)
 								result_message = result_message + ChatColor.GREEN + "I also updated the warp and no warp messages.";
 							else
 								result_message = result_message + ChatColor.GREEN + "I also updated the warp message.";
 						else if (updated_no_warp_message)
 							result_message = result_message + ChatColor.GREEN + "I also updated the no warp message.";
 					} else {
 						if (!result_message.equals(""))
 							result_message = result_message + "\n";
 						result_message =
 								result_message + ChatColor.RED + "Sorry, but you can't make a warp called \"" + name
 										+ "\" because it interferes with the command " + ChatColor.GREEN + "/warp " + name + ChatColor.RED + ".";
 						name = temp_old_name;
 					}
 				} else if (parameters[j].toLowerCase().startsWith("warp:")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					warp_message = parameters[j].substring(5);
 					parsing_warp_message = true;
 				} else if (parameters[j].toLowerCase().startsWith("nowarp:")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					no_warp_message = parameters[j].substring(7);
 					parsing_no_warp_message = true;
 				} else if (parameters[j].toLowerCase().startsWith("giveto:")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					String temp_old_owner = owner;
 					owner = getFullName(parameters[j].substring(7));
 					if (!result_message.equals(""))
 						result_message = result_message + "\n";
 					if (player != null && player.getName().toLowerCase().startsWith(temp_old_owner.toLowerCase()))
 						result_message = result_message + ChatColor.GREEN + "You gave \"" + name + "\" to " + owner + ".";
 					else
 						result_message = result_message + ChatColor.GREEN + "You gave " + temp_old_owner + "'s \"" + name + "\" to " + owner + ".";
 					// update the warp and no warp messages
 					boolean updated_warp_message = false, updated_no_warp_message = false;
 					if (warp_message.contains(temp_old_owner)) {
 						warp_message = replaceAll(warp_message, temp_old_owner, owner);
 						updated_warp_message = true;
 					}
 					if (no_warp_message.contains(temp_old_owner)) {
 						no_warp_message = replaceAll(no_warp_message, temp_old_owner, owner);
 						updated_no_warp_message = true;
 					}
 					if (!result_message.equals(""))
 						result_message = result_message + "\n";
 					if (updated_warp_message)
 						if (updated_no_warp_message)
 							result_message = result_message + ChatColor.GREEN + "I also updated the warp and no warp messages.";
 						else
 							result_message = result_message + ChatColor.GREEN + "I also updated the warp message.";
 					else if (updated_no_warp_message)
 						result_message = result_message + ChatColor.GREEN + "I also updated the no warp message.";
 				} else if (parameters[j].toLowerCase().startsWith("list:")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					String[] listed_users_list = parameters[j].substring(5).split(",");
 					if (listed_users_list.length > 0 && !(listed_users_list.length == 1 && listed_users_list[0].equals(""))) {
 						// retrieve full player names
 						for (int i = 0; i < listed_users_list.length; i++)
 							listed_users_list[i] = getFullName(listed_users_list[i]);
 						// state the change
 						if (!result_message.equals(""))
 							result_message = result_message + "\n";
 						if (restricted) {
 							if (player_is_owner)
 								if (listed_users_list.length == 1)
 									result_message = result_message + ChatColor.GREEN + listed_users_list[0] + " is now allowed to use \"" + name + ".\"";
 								else if (listed_users_list.length == 2)
 									result_message =
 											result_message + ChatColor.GREEN + listed_users_list[0] + " and " + listed_users_list[1]
 													+ " are now allowed to use \"" + name + ".\"";
 								else {
 									String message = ChatColor.GREEN + "";
 									for (int i = 0; i < listed_users_list.length - 1; i++)
 										message = message + listed_users_list[i] + ", ";
 									result_message =
 											result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are now allowed to use \""
 													+ name + ".\"";
 								}
 							else if (listed_users_list.length == 1)
 								result_message =
 										result_message + ChatColor.GREEN + listed_users_list[0] + " is now allowed to use " + owner + "'s \"" + name + ".\"";
 							else if (listed_users_list.length == 2)
 								result_message =
 										result_message + ChatColor.GREEN + listed_users_list[0] + " and " + listed_users_list[1] + " are now allowed to use "
 												+ owner + "'s \"" + name + ".\"";
 							else {
 								String message = ChatColor.GREEN + "";
 								for (int i = 0; i < listed_users_list.length - 1; i++)
 									message = message + listed_users_list[i] + ", ";
 								result_message =
 										result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are now allowed to use "
 												+ owner + "'s \"" + name + ".\"";
 							}
 						} else {
 							if (player_is_owner)
 								if (listed_users_list.length == 1)
 									result_message = result_message + ChatColor.GREEN + listed_users_list[0] + " is no longer allowed to use \"" + name + ".\"";
 								else if (listed_users_list.length == 2)
 									result_message =
 											result_message + ChatColor.GREEN + listed_users_list[0] + " and " + listed_users_list[1]
 													+ " are no longer allowed to use \"" + name + ".\"";
 								else {
 									String message = ChatColor.GREEN + "";
 									for (int i = 0; i < listed_users_list.length - 1; i++)
 										message = message + listed_users_list[i] + ", ";
 									result_message =
 											result_message + message + " and " + listed_users_list[listed_users_list.length - 1]
 													+ " are no longer allowed to use \"" + name + ".\"";
 								}
 							else if (listed_users_list.length == 1)
 								result_message =
 										result_message + ChatColor.GREEN + listed_users_list[0] + " is no longer allowed to use " + owner + "'s \"" + name
 												+ ".\"";
 							else if (listed_users_list.length == 2)
 								result_message =
 										result_message + ChatColor.GREEN + listed_users_list[0] + " and " + listed_users_list[1]
 												+ " are no longer allowed to use " + owner + "'s \"" + name + ".\"";
 							else {
 								String message = ChatColor.GREEN + "";
 								for (int i = 0; i < listed_users_list.length - 1; i++)
 									message = message + listed_users_list[i] + ", ";
 								result_message =
 										result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are no longer allowed to use "
 												+ owner + "'s \"" + name + ".\"";
 							}
 						}
 						String[] temp_listed_users = listed_users;
 						listed_users = new String[listed_users_list.length + temp_listed_users.length];
 						for (int i = 0; i < listed_users.length; i++) {
 							if (i < temp_listed_users.length)
 								listed_users[i] = temp_listed_users[i];
 							else
 								listed_users[i] = listed_users_list[i - temp_listed_users.length];
 						}
 					}
 				} else if (parameters[j].toLowerCase().startsWith("unlist:")) {
 					result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 					String[] unlisted_users_list = parameters[j].substring(7).split(",");
 					if (unlisted_users_list.length > 0 && !(unlisted_users_list.length == 1 && unlisted_users_list[0].equals(""))) {
 						// retrieve full player names
 						for (int i = 0; i < unlisted_users_list.length; i++)
 							unlisted_users_list[i] = getFullName(unlisted_users_list[i]);
 						// state the change
 						if (!result_message.equals(""))
 							result_message = result_message + "\n";
 						if (restricted) {
 							if (player_is_owner)
 								if (unlisted_users_list.length == 1)
 									result_message =
 											result_message + ChatColor.GREEN + unlisted_users_list[0] + " is no longer allowed to use \"" + name + ".\"";
 								else if (unlisted_users_list.length == 2)
 									result_message =
 											result_message + ChatColor.GREEN + unlisted_users_list[0] + " and " + unlisted_users_list[1]
 													+ " are no longer allowed to use \"" + name + ".\"";
 								else {
 									String message = ChatColor.GREEN + "";
 									for (int i = 0; i < unlisted_users_list.length - 1; i++)
 										message = message + unlisted_users_list[i] + ", ";
 									result_message =
 											result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1]
 													+ " are no longer allowed to use \"" + name + ".\"";
 								}
 							else if (unlisted_users_list.length == 1)
 								result_message =
 										result_message + ChatColor.GREEN + unlisted_users_list[0] + " is no longer allowed to use " + owner + "'s \"" + name
 												+ ".\"";
 							else if (unlisted_users_list.length == 2)
 								result_message =
 										result_message + ChatColor.GREEN + unlisted_users_list[0] + " and " + unlisted_users_list[1]
 												+ " are no longer allowed to use " + owner + "'s \"" + name + ".\"";
 							else {
 								String message = ChatColor.GREEN + "";
 								for (int i = 0; i < unlisted_users_list.length - 1; i++)
 									message = message + unlisted_users_list[i] + ", ";
 								result_message =
 										result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1]
 												+ " are no longer allowed to use " + owner + "'s \"" + name + ".\"";
 							}
 						} else {
 							if (player_is_owner)
 								if (unlisted_users_list.length == 1)
 									result_message = result_message + ChatColor.GREEN + unlisted_users_list[0] + " is now allowed to use \"" + name + ".\"";
 								else if (unlisted_users_list.length == 2)
 									result_message =
 											result_message + ChatColor.GREEN + unlisted_users_list[0] + " and " + unlisted_users_list[1]
 													+ " are now allowed to use \"" + name + ".\"";
 								else {
 									String message = ChatColor.GREEN + "";
 									for (int i = 0; i < unlisted_users_list.length - 1; i++)
 										message = message + unlisted_users_list[i] + ", ";
 									result_message =
 											result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1]
 													+ " are now allowed to use \"" + name + ".\"";
 								}
 							else if (unlisted_users_list.length == 1)
 								result_message =
 										result_message + ChatColor.GREEN + unlisted_users_list[0] + " is now allowed to use " + owner + "'s \"" + name + ".\"";
 							else if (unlisted_users_list.length == 2)
 								result_message =
 										result_message + ChatColor.GREEN + unlisted_users_list[0] + " and " + unlisted_users_list[1]
 												+ " are now allowed to use " + owner + "'s \"" + name + ".\"";
 							else {
 								String message = ChatColor.GREEN + "";
 								for (int i = 0; i < unlisted_users_list.length - 1; i++)
 									message = message + unlisted_users_list[i] + ", ";
 								result_message =
 										result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1] + " are now allowed to use "
 												+ owner + "'s \"" + name + ".\"";
 							}
 						}
 					}
 					// convert to ArrayList
 					ArrayList<String> listed_users_list = new ArrayList<String>();
 					for (String user : listed_users)
 						listed_users_list.add(user);
 					// remove unlisted users
 					for (String user : unlisted_users_list)
 						while (listed_users_list.contains(user))
 							listed_users_list.remove(user);
 					// convert back to array
 					listed_users = new String[listed_users_list.size()];
 					for (int i = 0; i < listed_users_list.size(); i++) {
 						if (listed_users_list.get(i) != null && !listed_users_list.get(i).equals(""))
 							listed_users[i] = listed_users_list.get(i);
 					}
 				} else if (parsing_warp_message)
 					warp_message = warp_message + " " + parameters[j];
 				else if (parsing_no_warp_message)
 					no_warp_message = no_warp_message + " " + parameters[j];
 			}
 			result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
 			if (!name.equalsIgnoreCase("info")
 					&& !name.equalsIgnoreCase("list")
 					&& !name.equals("all")
 					&& (player == null || owner.equalsIgnoreCase(player.getName()) || player.hasPermission("myultrawarps.change.other") || player
 							.hasPermission("myultrawarps.admin"))) {
 				if (name.equals(old_name) && owner.equals(old_owner) && listed == old_listed && restricted == old_restricted
 						&& warp_message.equals(old_warp_message) && no_warp_message.equals(old_no_warp_message) && listed_users.equals(old_listed_users))
 					sender.sendMessage(ChatColor.RED + "You didn't change anything!");
 				else {
 					UltraWarp new_warp = new UltraWarp(owner, name, listed, restricted, warp_message, no_warp_message, listed_users, warp.getLocation());
 					// change the warp's info
 					warps.remove(index);
 					// find out where the new warp needs to be in the list to be
 					// properly alphabetized
 					int insertion_index = 0;
 					for (UltraWarp my_warp : warps)
 						if (my_warp.getName().compareToIgnoreCase(name) < 0
 								|| (my_warp.getName().compareToIgnoreCase(name) == 0 && my_warp.getOwner().compareToIgnoreCase(owner) <= 0))
 							insertion_index++;
 					warps.add(insertion_index, new_warp);
 					sender.sendMessage(result_message);
 					if (autosave_warps)
 						saveTheWarps(sender, false);
 					if (!name.equals(old_name) || !owner.equals(old_owner)) {
 						// change the info for any switches linked to that warp
 						int number_of_affected_switches = 0;
 						for (int i = 0; i < switches.size(); i++)
 							if (switches.get(i).getWarpName().equals(old_name) && switches.get(i).getWarpOwner().equals(old_owner)) {
 								number_of_affected_switches++;
 								UltraSwitch new_switch =
 										new UltraSwitch(name, owner, switches.get(i).getSwitchType(), switches.get(i).getCooldownTime(), switches.get(i)
 												.getMaxUses(), switches.get(i).hasAGlobalCooldown(), switches.get(i).getCost(), switches.get(i)
 												.getExemptedPlayers(), switches.get(i).getX(), switches.get(i).getY(), switches.get(i).getZ(), switches.get(i)
 												.getWorld());
 								switches.remove(i);
 								// find out where the new switch needs to be in
 								// the list to be properly alphabetized
 								insertion_index = 0;
 								for (UltraSwitch my_switch : switches)
 									if (my_switch.getWarpName().compareToIgnoreCase(warp.getName()) < 0
 											|| (my_switch.getWarpName().compareToIgnoreCase(warp.getName()) == 0 && my_switch.getWarpOwner()
 													.compareToIgnoreCase(warp.getOwner()) <= 0))
 										insertion_index++;
 								switches.add(insertion_index, new_switch);
 							}
 						if (number_of_affected_switches == 1)
 							sender.sendMessage(ChatColor.GREEN + "The switch that was linked to \"" + old_name + "\" has also been updated.");
 						else if (number_of_affected_switches > 1)
 							sender.sendMessage(ChatColor.GREEN + "The " + number_of_affected_switches + " switches that were linked to \"" + old_name
 									+ "\" have also been updated.");
 					}
 				}
 			} else if (name.equalsIgnoreCase("info"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"info\" because it interferes with the command " + ChatColor.GREEN
 						+ "/warp info" + ChatColor.RED + ".");
 			else if (name.equalsIgnoreCase("all"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"all\" because it interferes with the command " + ChatColor.GREEN
 						+ "/warp all" + ChatColor.RED + ".");
 			else if (name.equalsIgnoreCase("list"))
 				sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"list\" because it interferes with the command " + ChatColor.GREEN
 						+ "/warp list" + ChatColor.RED + ".");
 			else {
 				// check if the player receiving the warp already has that
 				// warp
 				boolean warp_already_exists = false;
 				for (int i = 0; i < warps.size(); i++)
 					if (warps.get(i).getOwner().toLowerCase().startsWith(owner.toLowerCase())
 							&& warps.get(i).getName().toLowerCase().startsWith(parameters[extra_param].toLowerCase()))
 						warp_already_exists = true;
 				if (!warp_already_exists || !(sender instanceof Player) || sender.hasPermission("myultrawarps.change.other")
 						|| sender.hasPermission("myultrawarps.admin")) {
 					warps.remove(index);
 					// find out where the new warp needs to be in the list to be
 					// properly alphabetized
 					int insertion_index = 0;
 					for (UltraWarp my_warp : warps)
 						if (my_warp.getName().compareToIgnoreCase(name) < 0
 								|| (my_warp.getName().compareToIgnoreCase(name) == 0 && my_warp.getOwner().compareToIgnoreCase(owner) < 0))
 							insertion_index++;
 					// create the changed warp
 					warps.add(insertion_index, new UltraWarp(owner, name, listed, restricted, warp_message, no_warp_message, listed_users, warp.getLocation()));
 					sender.sendMessage(result_message);
 					if (autosave_warps)
 						saveTheWarps(sender, false);
 				} else
 					player.sendMessage(ChatColor.RED + "You're not allowed to modify " + owner + "'s \"" + name + ".\"");
 			}
 		} else if (player != null && player.getName().equals(owner))
 			player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 		else if (owner != null)
 			sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 		else
 			sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 	}
 
 	private void changeDefaultMessage(int extra_param, CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		Player online_target_player = null;
 		// determine if the warp or no warp message is being changed
 		boolean change_warp_message = true;
 		if (parameters[0].toLowerCase().startsWith("no"))
 			change_warp_message = false;
 		// get the change's target
 		String config_target = "server";
 		if (player != null)
 			config_target = player.getName();
 		boolean target_is_group = false;
 		if (parameters[extra_param].equalsIgnoreCase("for")) {
 			config_target = parameters[extra_param + 1];
 			if (config_target.toLowerCase().startsWith("group:")) {
 				config_target = config_target.substring(6);
 				target_is_group = true;
 			} else if (config_target.equalsIgnoreCase("global"))
 				config_target = "server";
 			else if (!config_target.equals("server")) {
 				for (Player my_player : server.getOnlinePlayers())
 					if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase())) {
 						config_target = my_player.getName();
 						online_target_player = my_player;
 					}
 				if (online_target_player == null)
 					for (OfflinePlayer my_player : server.getOfflinePlayers())
 						if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase()))
 							config_target = my_player.getName();
 			}
 			extra_param = extra_param + 2;
 		}
 		// read the new message
 		String new_message = "";
 		if (parameters[extra_param - 1].toLowerCase().startsWith("warp:"))
 			new_message = parameters[extra_param - 1].substring(5);
 		else if (parameters[extra_param - 1].toLowerCase().startsWith("nowarp:"))
 			new_message = parameters[extra_param - 1].substring(7);
 		else
 			new_message = null;
 		for (int i = extra_param; i < parameters.length; i++) {
 			if (new_message != null)
 				new_message = new_message + " " + parameters[i];
 			else
 				new_message = parameters[i];
 		}
 		if ((player != null && config_target.equals(player.getName())) || player == null || player.hasPermission("myultrawarps.admin")) {
 			if (config_target.equals("server")) {
 				if (change_warp_message) {
 					default_settings[2] = new_message;
 					if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 						sender.sendMessage(ChatColor.GREEN + "You changed the default warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 								+ ChatColor.GREEN + "\"");
 					else
 						sender.sendMessage(ChatColor.GREEN + "You changed the default warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 								+ ChatColor.GREEN + ".\"");
 				} else {
 					default_settings[3] = new_message;
 					if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 						sender.sendMessage(ChatColor.GREEN + "You changed the default no warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 								+ ChatColor.GREEN + "\"");
 					else
 						sender.sendMessage(ChatColor.GREEN + "You changed the default no warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 								+ ChatColor.GREEN + ".\"");
 				}
 			} else if (target_is_group) {
 				boolean group_exists = false;
 				for (String group : permissions.getGroups())
 					if (group.toLowerCase().startsWith(config_target.toLowerCase())) {
 						config_target = group;
 						group_exists = true;
 					}
 				if (group_exists) {
 					Object[] data = group_settings.get(config_target);
 					if (data == null) {
 						data = new Object[default_settings.length];
 						for (int i = 0; i < default_settings.length; i++)
 							data[i] = default_settings[i];
 					}
 					if (change_warp_message) {
 						data[1] = new_message;
 						if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 							sender.sendMessage(ChatColor.GREEN + "You changed the default warp message for the " + config_target + " group to \""
 									+ ChatColor.WHITE + colorCode(new_message) + ChatColor.GREEN + "\"");
 						else
 							sender.sendMessage(ChatColor.GREEN + "You changed the default warp message for the " + config_target + " group to \""
 									+ ChatColor.WHITE + colorCode(new_message) + ChatColor.GREEN + ".\"");
 					} else {
 						data[2] = new_message;
 						if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 							sender.sendMessage(ChatColor.GREEN + "You changed the default no warp message for the " + config_target + " group to \""
 									+ ChatColor.WHITE + colorCode(new_message) + ChatColor.GREEN + "\"");
 						else
 							sender.sendMessage(ChatColor.GREEN + "You changed the default no warp message for the " + config_target + "mgroup to \""
 									+ ChatColor.WHITE + colorCode(new_message) + ChatColor.GREEN + ".\"");
 					}
 					group_settings.put(config_target, data);
 				} else
 					sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find a group called \"" + config_target + ".\"");
 			} else {
 				Object[] data = per_player_settings.get(config_target);
 				if (data == null) {
 					String group = null;
 					if (online_target_player != null)
 						group = permissions.getPrimaryGroup(online_target_player);
 					else
 						group = permissions.getPrimaryGroup((World) null, config_target);
 					if (group != null)
 						data = group_settings.get(group);
 				}
 				if (data == null) {
 					data = new Object[default_settings.length];
 					for (int i = 0; i < default_settings.length; i++)
 						data[i] = default_settings[i];
 				}
 				if (change_warp_message) {
 					data[2] = new_message;
 					if (player != null && player.getName().equals(config_target))
 						if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 							sender.sendMessage(ChatColor.GREEN + "You changed your default warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 									+ ChatColor.GREEN + "\"");
 						else
 							sender.sendMessage(ChatColor.GREEN + "You changed your default warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 									+ ChatColor.GREEN + ".\"");
 					else if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 						sender.sendMessage(ChatColor.GREEN + "You changed " + config_target + "'s default warp message to \"" + ChatColor.WHITE
 								+ colorCode(new_message) + ChatColor.GREEN + "\"");
 					else
 						sender.sendMessage(ChatColor.GREEN + "You changed " + config_target + "'s default warp message to \"" + ChatColor.WHITE
 								+ colorCode(new_message) + ChatColor.GREEN + ".\"");
 				} else {
 					data[3] = new_message;
 					if (player != null && player.getName().equals(config_target))
 						if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 							sender.sendMessage(ChatColor.GREEN + "You changed your default no warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 									+ ChatColor.GREEN + "\"");
 						else
 							sender.sendMessage(ChatColor.GREEN + "You changed your default no warp message to \"" + ChatColor.WHITE + colorCode(new_message)
 									+ ChatColor.GREEN + ".\"");
 					else if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?"))
 						sender.sendMessage(ChatColor.GREEN + "You changed " + config_target + "'s default no warp message to \"" + ChatColor.WHITE
 								+ colorCode(new_message) + ChatColor.GREEN + "\"");
 					else
 						sender.sendMessage(ChatColor.GREEN + "You changed " + config_target + "'s default no warp message to \"" + ChatColor.WHITE
 								+ colorCode(new_message) + ChatColor.GREEN + ".\"");
 				}
 				per_player_settings.put(config_target, data);
 			}
 			saveTheConfig(sender, false);
 		} else
 			player.sendMessage(ChatColor.RED + "Sorry, but you're only allowed to change your own default messages.");
 	}
 
 	private void changeMaxWarps(int extra_param, CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		// get the change's target
 		String config_target = "server";
 		if (player != null)
 			config_target = player.getName();
 		boolean target_is_group = false;
 		Player online_target_player = null;
 		if (parameters[extra_param].equalsIgnoreCase("for")) {
 			config_target = parameters[extra_param + 1];
 			if (config_target.toLowerCase().startsWith("group:")) {
 				config_target = config_target.substring(6);
 				target_is_group = true;
 			} else if (config_target.equalsIgnoreCase("global"))
 				config_target = "server";
 			else if (!config_target.equals("server")) {
 				for (Player my_player : server.getOnlinePlayers())
 					if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase())) {
 						config_target = my_player.getName();
 						online_target_player = my_player;
 					}
 				if (online_target_player == null)
 					for (OfflinePlayer my_player : server.getOfflinePlayers())
 						if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase()))
 							config_target = my_player.getName();
 			}
 			extra_param = extra_param + 2;
 		}
 		// read the new max warps setting
 		int new_max_warps = -2;
 		if (parameters.length > extra_param) {
 			try {
 				new_max_warps = Integer.parseInt(parameters[extra_param]);
 			} catch (NumberFormatException exception) {
 				if (parameters[extra_param].equalsIgnoreCase("infinite") || parameters[extra_param].equalsIgnoreCase("infinity"))
 					new_max_warps = -1;
 				else
 					sender.sendMessage(ChatColor.RED + "I don't know what \"" + parameters[extra_param]
 							+ "\" means, but I know it's not the word \"infinite\" or the word \"infinity\" or an integer.");
 			}
 		} else
 			sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want me to change the max warps to!");
 		if (new_max_warps != -2) {
 			if (config_target.equals("server")) {
 				default_settings[4] = new_max_warps;
 				for (int i = 0; i < group_settings.size(); i++) {
 					String key = (String) group_settings.keySet().toArray()[i];
 					Object[] data = group_settings.get(key);
 					data[4] = new_max_warps;
 					group_settings.put(key, data);
 				}
 				for (int i = 0; i < per_player_settings.size(); i++) {
 					String key = (String) per_player_settings.keySet().toArray()[i];
 					Object[] data = per_player_settings.get(key);
 					data[4] = new_max_warps;
 					per_player_settings.put(key, data);
 				}
 				if (new_max_warps != -1)
 					sender.sendMessage(ChatColor.GREEN + "You changed the default maximum number of warps to " + new_max_warps + ".");
 				else
 					sender.sendMessage(ChatColor.GREEN + "Everyone can now make as many warps as they want.");
 			} else if (target_is_group) {
 				boolean group_exists = false;
 				for (String group : permissions.getGroups())
 					if (group.toLowerCase().startsWith(config_target.toLowerCase())) {
 						config_target = group;
 						group_exists = true;
 					}
 				if (group_exists) {
 					Object[] data = group_settings.get(config_target);
 					if (data == null) {
 						data = new Object[default_settings.length];
 						for (int i = 0; i < default_settings.length; i++)
 							data[i] = default_settings[i];
 					}
 					data[4] = new_max_warps;
 					if (new_max_warps != -1)
 						sender.sendMessage(ChatColor.GREEN + "You changed the default maximum number of warps for the " + config_target + " group to "
 								+ new_max_warps + ".");
 					else
 						sender.sendMessage(ChatColor.GREEN + "Everyone in the " + config_target + " group can now make as many warps as they want.");
 					group_settings.put(config_target, data);
 				} else
 					sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find a group called \"" + config_target + ".\"");
 			} else {
 				Object[] data = per_player_settings.get(config_target);
 				if (data == null) {
 					String group = null;
 					if (online_target_player != null)
 						group = permissions.getPrimaryGroup(online_target_player);
 					else
 						group = permissions.getPrimaryGroup((World) null, config_target);
 					if (group != null)
 						data = group_settings.get(group);
 				}
 				if (data == null) {
 					data = new Object[default_settings.length];
 					for (int i = 0; i < default_settings.length; i++)
 						data[i] = default_settings[i];
 				}
 				data[4] = new_max_warps;
 				if (player != null && player.getName().equals(config_target))
 					if (new_max_warps != -1) {
 						sender.sendMessage(ChatColor.GREEN + "You can now make a maximum of " + new_max_warps + " warps.");
 						sender.sendMessage(ChatColor.GREEN + "...but, uh...you're a myUltraWarps admin, so you can still make as many warps as you want....");
 					} else {
 						sender.sendMessage(ChatColor.GREEN + "You can now make as many warps as you want.");
 						sender.sendMessage(ChatColor.GREEN
 								+ "...but, uh...you're a myUltraWarps admin, so you could already make as many warps as you want....");
 					}
 				else if (new_max_warps != -1) {
 					sender.sendMessage(ChatColor.GREEN + config_target + " can now make a maximum of " + new_max_warps + " warps.");
 					if ((online_target_player != null && online_target_player.hasPermission("myultrawarps.admin"))
 							|| (permissions != null && permissions.has((World) null, config_target, "myultrawraps.admin")))
 						sender.sendMessage(ChatColor.GREEN + "...but, uh..." + config_target
 								+ " is a myUltraWarps admin, so they can still make as many warps as they want....");
 				} else {
 					sender.sendMessage(ChatColor.GREEN + config_target + " can now make a maximum of " + new_max_warps + " warps.");
 					if ((online_target_player != null && online_target_player.hasPermission("myultrawarps.admin"))
 							|| (permissions != null && permissions.has((World) null, config_target, "myultrawraps.admin")))
 						sender.sendMessage(ChatColor.GREEN + "...but, uh..." + config_target
 								+ " is a myUltraWarps admin, so they could already make as many warps as they want....");
 				}
 				per_player_settings.put(config_target, data);
 			}
 			saveTheConfig(sender, false);
 		}
 	}
 
 	private void checkForUpdates(CommandSender sender) {
 		// check for updates
 		URL url = null;
 		try {
 			url = new URL("http://dev.bukkit.org/server-mods/myultrawarps-v0/files.rss/");
 		} catch (MalformedURLException exception) {
 			sender.sendMessage("Nooo! Bad U.R.L.! Bad U.R.L.! The updater screwed up!");
 		}
 		if (url != null) {
 			String new_version_name = null, new_version_link = null;
 			try {
 				// Set header values intial to the empty string
 				String title = "";
 				String link = "";
 				// First create a new XMLInputFactory
 				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
 				// Setup a new eventReader
 				InputStream in = null;
 				try {
 					in = url.openStream();
 				} catch (IOException e) {
 					sender.sendMessage(ChatColor.DARK_RED + "The myUltraWarps updater can't connect to BukkitDev!");
 					return;
 				}
 				XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
 				// Read the XML document
 				while (eventReader.hasNext()) {
 					XMLEvent event = eventReader.nextEvent();
 					if (event.isStartElement()) {
 						if (event.asStartElement().getName().getLocalPart().equals("title")) {
 							event = eventReader.nextEvent();
 							title = event.asCharacters().getData();
 							continue;
 						}
 						if (event.asStartElement().getName().getLocalPart().equals("link")) {
 							event = eventReader.nextEvent();
 							link = event.asCharacters().getData();
 							continue;
 						}
 					} else if (event.isEndElement()) {
 						if (event.asEndElement().getName().getLocalPart().equals("item")) {
 							new_version_name = title;
 							new_version_link = link;
 							// All done, we don't need to know about older
 							// files.
 							break;
 						}
 					}
 				}
 			} catch (XMLStreamException exception) {
 				sender.sendMessage(ChatColor.DARK_RED + "Gah! XMLStreamExceptionThing! Come quick! Tell REALDrummer!");
 				return;
 			}
 			boolean new_version_is_out = false;
 			String version = getDescription().getVersion(), newest_online_version = "";
 			if (new_version_name.split("v").length == 2) {
 				newest_online_version = new_version_name.split("v")[new_version_name.split("v").length - 1].split(" ")[0];
 				// get the newest file's version number
 				if (!version.contains("-DEV") && !version.contains("-PRE") && !version.equalsIgnoreCase(newest_online_version))
 					try {
 						if (Double.parseDouble(version) < Double.parseDouble(newest_online_version))
 							new_version_is_out = true;
 					} catch (NumberFormatException exception) {
 					}
 			} else
 				sender.sendMessage(ChatColor.RED
 						+ "Oh, no! REALDrummer forgot to put the version number in the title of the plugin on BukkitDev! The updater won't work! Quick! Tell him he messed up and he needs to fix it right away!");
 			if (new_version_is_out) {
 				String fileLink = null;
 				try {
 					// Open a connection to the page
 					BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(new_version_link).openConnection().getInputStream()));
 					String line;
 					while ((line = reader.readLine()) != null)
 						// Search for the download link
 						if (line.contains("<li class=\"user-action user-action-download\">"))
 							// Get the raw link
 							fileLink = line.split("<a href=\"")[1].split("\">Download</a>")[0];
 					reader.close();
 					reader = null;
 				} catch (Exception exception) {
 					sender.sendMessage(ChatColor.DARK_RED + "Uh-oh! The myUltraWarps updater couldn't contact bukkitdev.org!");
 					exception.printStackTrace();
 					return;
 				}
 				if (fileLink != null) {
 					if (!new File(this.getDataFolder(), "myUltraWarps.jar").exists()) {
 						BufferedInputStream in = null;
 						FileOutputStream fout = null;
 						try {
 							// Download the file
 							url = new URL(fileLink);
 							in = new BufferedInputStream(url.openStream());
 							fout = new FileOutputStream(this.getDataFolder().getAbsolutePath() + "/myUltraWarps.jar");
 							byte[] data = new byte[1024];
 							int count;
 							while ((count = in.read(data, 0, 1024)) != -1)
 								fout.write(data, 0, count);
 							if (!(sender instanceof Player))
 								sender.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "'DING!' Your myUltraWarps v" + newest_online_version
 										+ " is ready and it smells AWESOME!! I downloaded it to your myUltraWarps folder! Go get it!");
 							for (Player player : server.getOnlinePlayers())
 								if (player.hasPermission("myultrawarps.admin") && (!(sender instanceof Player) || !sender.getName().equals(player.getName())))
 									player.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "'DING!' Your myUltraWarps v" + newest_online_version
 											+ " is ready and it smells AWESOME!! I downloaded it to your myUltraWarps folder! Go get it!");
 						} catch (Exception ex) {
 							sender.sendMessage(ChatColor.DARK_RED + "Shoot. myUltraWarps v" + newest_online_version
 									+ " is out, but something messed up the download. You're gonna have to go to BukkitDev and get it yourself. Sorry.");
 						} finally {
 							try {
 								if (in != null)
 									in.close();
 								if (fout != null)
 									fout.close();
 							} catch (Exception ex) {
 							}
 						}
 					} else
 						sender.sendMessage(ChatColor.RED
 								+ "O_O Why is the newest version of myUltraWarps still sitting in your plugin folder?! Hurry up and put it on your server!");
 				}
 			} else
 				sender.sendMessage(ChatColor.GREEN + "Sorry, but no new versions of myUltraWarps are out yet.");
 		}
 	}
 
 	private void death(CommandSender sender) {
 		Player player = (Player) sender;
 		int amount = 1;
 		if (parameters.length > 0)
 			try {
 				amount = Integer.parseInt(parameters[0]);
 				if (amount == 0) {
 					sender.sendMessage(ChatColor.GREEN + "Well, here you are. You went back 0 deaths through your history.");
 					return;
 				} else if (amount < 0) {
 					sender.sendMessage(ChatColor.RED + "Sorry, but I can't see into the future. At least...not that far ahead.");
 					return;
 				}
 			} catch (NumberFormatException exception) {
 				sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
 				return;
 			}
 		ArrayList<Location> death_history = death_histories.get(player.getName());
 		Integer last_warp_to_death_index = last_warp_to_death_indexes.get(player.getName());
 		Location last_death = null;
 		if (death_history != null) {
 			if (last_warp_to_death_index == null)
 				last_warp_to_death_index = death_history.size() - 1;
 			if (last_warp_to_death_index + 1 >= amount)
 				last_death = death_history.get(last_warp_to_death_index + 1 - amount);
 			else {
 				if (death_history == null || death_history.size() == 0)
 					sender.sendMessage(ChatColor.RED + "You haven't died yet!");
 				else if (last_warp_to_death_index > 1)
 					sender.sendMessage(ChatColor.RED + "You can only go back " + last_warp_to_death_index + " more deaths.");
 				else if (last_warp_to_death_index == 1)
 					sender.sendMessage(ChatColor.RED + "You can only go back one more death.");
 				else
 					sender.sendMessage(ChatColor.RED + "Sorry, but I don't keep track of that many deaths. This is as far back as you can go.");
 				return;
 			}
 		}
 		if (last_death != null) {
 			teleport(player, null, new UltraWarp(ChatColor.GREEN + "HERE LIES " + player.getName() + " (death #"
 					+ (death_history.size() - last_warp_to_death_index) + "/" + death_history.size() + ")", last_death), true, null);
 			last_warp_to_death_indexes.put(player.getName(), last_warp_to_death_index - amount);
 		}
 	}
 
 	private void deathForward(CommandSender sender) {
 		Player player = (Player) sender;
 		ArrayList<Location> death_history = death_histories.get(player.getName());
 		Integer last_warp_to_death_index = last_warp_to_death_indexes.get(player.getName());
 		int amount = 1;
 		if (parameters.length > 0)
 			try {
 				amount = Integer.parseInt(parameters[0]);
 				if (amount == 0) {
 					player.sendMessage(ChatColor.RED + "You're already at the place you were at 0 deaths ago.");
 					return;
 				} else if (amount < 0) {
 					player.sendMessage(ChatColor.RED + "Uh...negative forward? Can you just give me a positive integer, please?");
 					return;
 				}
 			} catch (NumberFormatException exception) {
 				player.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
 				return;
 			}
 		if (death_history == null || death_history.size() == 0 || last_warp_to_death_index == null) {
 			player.sendMessage(ChatColor.RED + "You haven't died yet!");
 			death_histories.put(player.getName(), new ArrayList<Location>());
 		} else if (death_history.size() <= last_warp_to_death_index + 1 + amount) {
 			if (death_history.size() - last_warp_to_death_index - 2 > 1)
 				player.sendMessage(ChatColor.RED + "You can only go forward " + (death_history.size() - last_warp_to_death_index - 2) + " deaths.");
 			else if (death_history.size() - last_warp_to_death_index - 2 == 1)
 				player.sendMessage(ChatColor.RED + "You can only go forward one death.");
 			else
 				player.sendMessage(ChatColor.RED + "You're already at the last death in your history.");
 		} else {
 			Location death = death_history.get(last_warp_to_death_index + 1 + amount);
 			if (death != null) {
 				teleport(player, null, new UltraWarp(ChatColor.GREEN + "HERE LIES " + player.getName() + " (death #"
 						+ (death_history.size() - last_warp_to_death_index - amount - 1) + "/" + death_history.size() + ")", death), true, null);
 				last_warp_to_death_indexes.put(player.getName(), last_warp_to_death_index + amount);
 			}
 		}
 	}
 
 	private void deleteWarp(int extra_param, CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		UltraWarp warp = locateWarp(extra_param, sender);
 		// delete the warp or tell the player it can't be done
 		if (warp != null
 				&& (player == null || player.getName().equals(warp.getOwner()) || player.hasPermission("myultrawarps.delete.other") || player
 						.hasPermission("myultrawarps.admin"))) {
 			if (player != null && warp.getOwner().equals(player.getName()))
 				player.sendMessage(ChatColor.GREEN + "You deleted \"" + warp.getName() + ".\"");
 			else
 				sender.sendMessage(ChatColor.GREEN + "You deleted " + warp.getOwner() + "'s warp \"" + warp.getName() + ".\"");
 			int switches_deleted = 0;
 			for (int i = 0; i < switches.size(); i++)
 				if (warp.getName().equals(switches.get(i).getWarpName()) && warp.getOwner().equals(switches.get(i).getWarpOwner())) {
 					switches.remove(i);
 					i--;
 					switches_deleted++;
 				}
 			if (switches_deleted > 0) {
 				if (autosave_switches)
 					saveTheSwitches(sender, false);
 				if (player != null && warp.getOwner().equals(player.getName()))
 					if (switches_deleted == 1)
 						player.sendMessage(ChatColor.GREEN + "You also unlinked your switch that was linked to it.");
 					else
 						player.sendMessage(ChatColor.GREEN + "You also unlinked your " + switches_deleted + " switches that were linked to it.");
 				else if (switches_deleted == 1)
 					sender.sendMessage(ChatColor.GREEN + "You also unlinked a switch that was linked to it.");
 				else
 					sender.sendMessage(ChatColor.GREEN + "You also unlinked " + switches_deleted + " switches that were linked to it.");
 			}
 			warps.remove(index);
 			if (autosave_warps)
 				saveTheWarps(sender, false);
 		} else if (warp != null)
 			player.sendMessage(ChatColor.RED + "You don't have permission to delete " + warp.getOwner() + "'s \"" + warp.getName() + ".\"");
 		else if (player != null && (owner == null || player.getName().equals(owner)))
 			player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 		else if (owner != null)
 			sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 	}
 
 	private void forward(CommandSender sender) {
 		Player player = (Player) sender;
 		ArrayList<UltraWarp> warp_history = warp_histories.get(player.getName());
 		Integer last_warp_index = last_warp_indexes.get(player.getName());
 		int amount = 1;
 		if (parameters.length > 0)
 			try {
 				amount = Integer.parseInt(parameters[0]);
 				if (amount == 0) {
 					player.sendMessage(ChatColor.RED + "You're already at the place you were at 0 warps ago.");
 					return;
 				} else if (amount < 0) {
 					player.sendMessage(ChatColor.RED + "Uh...negative forward? Can you just give me a positive integer, please?");
 					return;
 				}
 			} catch (NumberFormatException exception) {
 				player.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
 				return;
 			}
 		if (warp_history == null || warp_history.size() == 0 || last_warp_index == null) {
 			player.sendMessage(ChatColor.RED + "You haven't warped anywhere yet!");
 			warp_histories.put(player.getName(), new ArrayList<UltraWarp>());
 		} else if (warp_history.size() <= last_warp_index + amount) {
 			if (warp_history.size() - last_warp_index - 2 > 1)
 				player.sendMessage(ChatColor.RED + "You can only go forward " + (warp_history.size() - last_warp_index - 2) + " warps.");
 			else if (warp_history.size() - last_warp_index - 2 == 1)
 				player.sendMessage(ChatColor.RED + "You can only go forward one warp.");
 			else
 				player.sendMessage(ChatColor.RED + "You're already at the last warp in your history.");
 		} else {
 			UltraWarp warp = warp_history.get(last_warp_index + amount);
 			if (warp != null) {
 				boolean player_is_listed = false;
 				for (String listed_player : warp.getListedUsers())
 					if (listed_player.equals(player.getName()))
 						player_is_listed = true;
 				if (player.hasPermission("myultrawarps.admin") || player.hasPermission("myultrawarps.warptowarp.other")
 						|| warp.getOwner().equals(player.getName()) || (!warp.isRestricted() && !player_is_listed) || (warp.isRestricted() && player_is_listed)) {
 					teleport(player, null, warp, true, null);
 					last_warp_indexes.put(player.getName(), last_warp_index + amount);
 				} else
 					player.sendMessage(colorCode(replaceAll(warp.getNoWarpMessage(), "[player]", player.getName())));
 			}
 		}
 	}
 
 	private void from(CommandSender sender) {
 		Player player = (Player) sender;
 		Object[] data = default_settings;
 		if (permissions != null && permissions.getPrimaryGroup(player) != null && group_settings.containsKey(permissions.getPrimaryGroup(player)))
 			data = group_settings.get(permissions.getPrimaryGroup(player));
 		if (per_player_settings.containsKey(player.getName()))
 			data = per_player_settings.get(player.getName());
 		// find the target player
 		Player target_player = null;
 		for (Player my_player : server.getOnlinePlayers())
 			if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
 				target_player = my_player;
		// make sure the target was found, isn't blocked, and has permission to teleport to other people (or the person teleporting them is an admin since
		// admins are supreme)
		if (target_player != null
				&& !target_player.equals(player)
				&& (target_player.hasPermission("myultrawarps.to") || target_player.hasPermission("myultrawarps.user")
						|| target_player.hasPermission("myultrawarps.admin") || player.hasPermission("myultrawarps.admin"))
 				&& (blocked_players.get(target_player.getName()) == null || !blocked_players.get(target_player.getName()).contains(player.getName()))) {
 			if (!(Boolean) data[1] || player.hasPermission("myultrawarps.admin")
 					|| (to_teleport_requests.get(player.getName()) != null && to_teleport_requests.get(player.getName()).contains(target_player.getName()))) {
 				// remove any to teleportation requests from the target player
 				ArrayList<String> requesting_players = to_teleport_requests.get(player.getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				while (requesting_players.contains(target_player.getName()))
 					requesting_players.remove(target_player.getName());
 				to_teleport_requests.put(player.getName(), requesting_players);
				if (teleport(target_player, new UltraWarp("&aThis is the spot you were at before " + player.getName() + " teleported you to them.",
						target_player.getLocation()), new UltraWarp("&aThis is the spot you were at when you were teleported to " + player.getName() + ".",
						player.getLocation()), false, player)) {
 					target_player.sendMessage(ChatColor.GREEN + "Here's your " + player.getName() + "!");
 					player.sendMessage(ChatColor.GREEN + "Look! I brought you a " + target_player.getName() + "!");
 				}
 			} else {
 				player.sendMessage(ChatColor.GREEN + "Hang on. Let me ask " + target_player.getName() + " if it's okay.");
 				target_player.sendMessage(ChatColor.GREEN + player.getName() + " would like to teleport you to them. Is that okay?");
 				ArrayList<String> requesting_players = from_teleport_requests.get(target_player.getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				requesting_players.add(player.getName());
 				from_teleport_requests.put(target_player.getName(), requesting_players);
 			}
 		} else if (target_player != null && blocked_players.get(target_player.getName()) != null
 				&& blocked_players.get(target_player.getName()).contains(player.getName()))
 			player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName()
 					+ " has blocked you. You can't send them teleportation requests anymore.");
 		else if (target_player != null && target_player.equals(player))
 			player.sendMessage(ChatColor.RED + "Can you explain to me how I'm supposed to teleport you to yourself?");
		else if (target_player != null && !target_player.hasPermission("myultrawarps.to") && !target_player.hasPermission("myultrawarps.user")
				&& !target_player.hasPermission("myultrawarps.admin") && !player.hasPermission("myultrawarps.admin"))
			player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName() + " doesn't have permission to teleport to other poeple.");
 		else
 			player.sendMessage(ChatColor.RED + "I couldn't find \"" + parameters[0] + "\" anywhere.");
 	}
 
 	private void fullSwitchList(CommandSender sender) {
 		// TODO
 		sender.sendMessage(ChatColor.GOLD + "Coming soon to a server near you!");
 	}
 
 	private void fullWarpList(CommandSender sender) {
 		Player player = null;
 		String sender_name = ",";
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			sender_name = player.getName();
 		}
 		boolean by_name = false;
 		if (full_list_organization_by_user.containsKey(sender_name))
 			by_name = full_list_organization_by_user.get(sender_name);
 		int extra_param = 0;
 		if (parameters.length > 1 && parameters[0].equalsIgnoreCase("warp") && parameters[1].equalsIgnoreCase("list"))
 			extra_param = 2;
 		int characters_per_page = 575;
 		if (player == null)
 			characters_per_page = 1725;
 		String owner = null, type = null;
 		int page_number = 1;
 		boolean screwup = false;
 		for (int i = extra_param; i < parameters.length; i++) {
 			if (parameters[i].toLowerCase().startsWith("type:o"))
 				type = "open";
 			else if (parameters[i].toLowerCase().startsWith("type:a"))
 				type = "advertised";
 			else if (parameters[i].toLowerCase().startsWith("type:s"))
 				type = "secret";
 			else if (parameters[i].toLowerCase().startsWith("type:p"))
 				type = "private";
 			else if (parameters[i].toLowerCase().startsWith("owner:")) {
 				owner = getFullName(parameters[i].substring(6));
 				by_name = false;
 			} else if (parameters[i].equalsIgnoreCase("page")) {
 				if (parameters.length > i + 1) {
 					try {
 						page_number = Integer.parseInt(parameters[i + 1]);
 					} catch (NumberFormatException exception) {
 						sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[i + 1] + "\" an integer?");
 						screwup = true;
 					}
 					if (page_number == 0) {
 						sender.sendMessage(ChatColor.RED + "I think you know very well that there is no page 0, you little trouble maker. Nice try.");
 						screwup = true;
 					} else if (page_number < 0) {
 						sender.sendMessage(ChatColor.RED + "Negative page numbers? Really? Try again.");
 						screwup = true;
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You forgot to tell me which page you want me to show you!");
 					screwup = true;
 				}
 			} else if (parameters[i].equals("by") && parameters.length > i + 1)
 				if (parameters[i + 1].equalsIgnoreCase("owner"))
 					by_name = false;
 				else if (parameters[i + 1].equalsIgnoreCase("name"))
 					by_name = true;
 		}
 		if (screwup)
 			return;
 		// save the user's organization preference
 		full_list_organization_by_user.put(sender_name, by_name);
 		ArrayList<String> pages = new ArrayList<String>();
 		// if the list is organized by warp name
 		if (by_name) {
 			String output = ChatColor.GREEN + "ALL the server's warps! (page 1 of [number of pages]): ";
 			int number_of_characters = 41;
 			for (int i = 0; i < warps.size(); i++) {
 				String warp_type = "private";
 				if (warps.get(i).isListed() && !warps.get(i).isRestricted())
 					warp_type = "open";
 				else if (warps.get(i).isListed())
 					warp_type = "advertised";
 				else if (!warps.get(i).isRestricted())
 					warp_type = "secret";
 				if ((owner == null || warps.get(i).getOwner().equals(owner)) && (type == null || warp_type.equals(type))) {
 					// paginate
 					if (number_of_characters + warps.get(i).getName().length() + 2 > characters_per_page) {
 						output = output + ChatColor.WHITE + ",...";
 						pages.add(output);
 						output = ChatColor.GREEN + "ALL the server's warps! (page " + (pages.size() + 1) + " of [number of pages]): " + ChatColor.WHITE + "...";
 						number_of_characters = 44;
 					} else if (i != 0) {
 						output = output + ChatColor.WHITE + ", ";
 						number_of_characters = number_of_characters + 2;
 						if (i == warps.size() - 1) {
 							output = output + "and ";
 							number_of_characters = number_of_characters + 4;
 						}
 					}
 					output = output + warps.get(i).getColoredName();
 					number_of_characters = number_of_characters + warps.get(i).getName().length();
 				}
 			}
 			pages.add(output);
 		}
 		// if the list is organized by owner
 		else {
 			// gather each player's list of warps
 			HashMap<String, ArrayList<UltraWarp>> warps_by_owner = new HashMap<String, ArrayList<UltraWarp>>();
 			for (UltraWarp warp : warps) {
 				String warp_type = "private";
 				if (warp.isListed() && !warp.isRestricted())
 					warp_type = "open";
 				else if (warp.isListed())
 					warp_type = "advertised";
 				else if (!warp.isRestricted())
 					warp_type = "secret";
 				if ((owner == null || warp.getOwner().startsWith(owner)) && (type == null || warp_type.equals(type))) {
 					ArrayList<UltraWarp> player_warp_list = warps_by_owner.get(warp.getOwner());
 					if (player_warp_list == null)
 						player_warp_list = new ArrayList<UltraWarp>();
 					player_warp_list.add(warp);
 					warps_by_owner.put(warp.getOwner(), player_warp_list);
 				}
 			}
 			// convert the lists of warps to formatted Strings
 			String output = ChatColor.GREEN + "ALL the server's warps! (page " + (pages.size() + 1) + " of [number of pages]): \n";
 			int number_of_characters = characters_per_page / 10;
 			while (warps_by_owner.size() > 0) {
 				String first_username = (String) warps_by_owner.keySet().toArray()[0];
 				for (String username : warps_by_owner.keySet())
 					if (username.compareToIgnoreCase(first_username) < 0)
 						first_username = username;
 				ArrayList<UltraWarp> player_warps = warps_by_owner.get(first_username);
 				warps_by_owner.remove(first_username);
 				if (number_of_characters + first_username.length() + 20 > characters_per_page) {
 					pages.add(output);
 					output = ChatColor.GREEN + "ALL the server's warps! (page " + (pages.size() + 1) + " of [number of pages]): \n";
 					number_of_characters = characters_per_page / 10;
 				}
 				if (player != null && player.getName().equals(first_username)) {
 					output = output + ChatColor.GREEN + "your warps: ";
 					number_of_characters = number_of_characters + 12;
 				} else {
 					output = output + ChatColor.GREEN + first_username + "'s warps: ";
 					number_of_characters = number_of_characters + first_username.length() + 10;
 				}
 				for (int i = 0; i < player_warps.size(); i++) {
 					if (number_of_characters + player_warps.get(i).getName().length() > characters_per_page) {
 						output = output + ChatColor.WHITE + ",...";
 						pages.add(output);
 						output = ChatColor.GREEN + "ALL the server's warps! (page " + (pages.size() + 1) + " of [number of pages]): \n";
 						number_of_characters = characters_per_page / 10;
 						if (player != null && player.getName().equals(first_username)) {
 							output = output + ChatColor.GREEN + "your warps (continued): " + ChatColor.WHITE + "...";
 							number_of_characters = number_of_characters + 26;
 						} else {
 							output = output + ChatColor.GREEN + first_username + "'s warps (continued): " + ChatColor.WHITE + "...";
 							number_of_characters = number_of_characters + first_username.length() + 24;
 						}
 					} else if (i != 0) {
 						output = output + ChatColor.WHITE + ", ";
 						number_of_characters = number_of_characters + 2;
 						if (i == player_warps.size() - 1) {
 							output = output + "and ";
 							number_of_characters = number_of_characters + 4;
 						}
 					}
 					output = output + player_warps.get(i).getColoredName();
 					number_of_characters = number_of_characters + player_warps.get(i).getName().length();
 					if (i == player_warps.size() - 1 && warps_by_owner.size() > 0) {
 						output = output + "\n";
 						number_of_characters = ((int) (number_of_characters / (characters_per_page / 10)) + 1) * characters_per_page / 10;
 					}
 				}
 			}
 			pages.add(output);
 		}
 		if (page_number > pages.size())
 			if (pages.size() == 1)
 				sender.sendMessage(ChatColor.RED + "There is only one page of warps.");
 			else
 				sender.sendMessage(ChatColor.RED + "There are only " + pages.size() + " pages of warps.");
 		else {
 			// replace "[number of pages]" with the number of pages
 			for (int i = 0; i < pages.size(); i++) {
 				pages.set(i, pages.get(i).substring(0, pages.get(i).indexOf("[number of pages]")) + pages.size()
 						+ pages.get(i).substring(pages.get(i).indexOf("[number of pages]") + 17));
 			}
 			sender.sendMessage(pages.get(page_number - 1));
 		}
 	}
 
 	private void jump(CommandSender sender) {
 		Player player = (Player) sender;
 		Location target_location = player.getTargetBlock(null, 1024).getLocation();
 		if (target_location.getBlock().getTypeId() != 0)
 			teleport(player, null, new UltraWarp(ChatColor.GREEN + "You jumped!", new Location(player.getWorld(), target_location.getX() + 0.5, target_location
 					.getY() + 1, target_location.getZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch())), true, null);
 		else
 			player.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
 	}
 
 	private void home(CommandSender sender) {
 		Player player = (Player) sender;
 		UltraWarp warp = null;
 		String owner;
 		// extract the name of the player and the name of the warp
 		if (parameters.length > 0 && parameters[0].toLowerCase().endsWith("'s"))
 			owner = getFullName(parameters[0].substring(0, parameters[0].length() - 2));
 		else
 			owner = player.getName();
 		// locate the warp in the list of warps
 		for (int i = 0; i < warps.size(); i++)
 			if (warps.get(i).getName().equals("home") && warps.get(i).getOwner().toLowerCase().startsWith(owner.toLowerCase()))
 				warp = warps.get(i);
 		if (warp != null) {
 			if (player.getName().equals(owner) || player.hasPermission("myultrawarps.home.other") || player.hasPermission("myultrawarps.admin")) {
 				if (teleport(player, new UltraWarp("&aThis is the spot you were at before you teleported home.", player.getLocation()), warp, false, null))
 					if (player.getName().equals(owner) && !warp.getWarpMessage().equals(""))
 						player.sendMessage(colorCode(replaceAll(warp.getWarpMessage(), "[player]", player.getName())));
 					else if (!warp.getWarpMessage().equals(""))
 						player.sendMessage(colorCode("&aWelcome home...wait, you're not " + warp.getOwner() + "! &o" + warp.getOwner().toUpperCase() + "!!!!"));
 			} else
 				player.sendMessage(colorCode(replaceAll(warp.getNoWarpMessage(), "[player]", player.getName())));
 		} else {
 			// tell the player the warp wasn't found
 			if (player.getName().toLowerCase().startsWith(owner.toLowerCase()))
 				player.sendMessage(ChatColor.RED + "You need to set your home before you can warp to it!");
 			else
 				player.sendMessage(ChatColor.RED + "I couldn't find " + owner + "'s home.");
 		}
 	}
 
 	private void linkWarp(int extra_param, CommandSender sender) {
 		Player player = (Player) sender;
 		// sign post=63, wall sign=68, lever=69, stone pressure plate=70, wooden
 		// pressure plate=72, stone button=77, wooden button = 143
 		Block target_block = player.getTargetBlock(null, 1024);
 		UltraWarp warp = null;
 		if (target_block != null
 				&& (target_block.getTypeId() == 63 || target_block.getTypeId() == 68 || target_block.getTypeId() == 69 || target_block.getTypeId() == 70
 						|| target_block.getTypeId() == 72 || target_block.getTypeId() == 77 || target_block.getTypeId() == 143)) {
 			warp = locateWarp(extra_param, sender);
 			if (warp != null
 					&& (player.getName().equals(warp.getOwner()) || player.hasPermission("myultrawarps.link.other") || player
 							.hasPermission("myultrawarps.admin"))) {
 				// search for non-default settings changes
 				boolean parse_cooldown_time = false, error = false, global = false;
 				double cost = 0, previous_number = -1;
 				int max_uses = 0, cooldown_time = 0;
 				String[] exempted_players = new String[0];
 				for (int j = 0; j < parameters.length; j++) {
 					if (parameters[j].toLowerCase().startsWith("cooldown:")) {
 						parse_cooldown_time = true;
 						try {
 							previous_number = Double.parseDouble(parameters[j].substring(9));
 						} catch (NumberFormatException exception) {
 							error = true;
 							j = parameters.length;
 						}
 					} else if (parameters[j].toLowerCase().startsWith("uses:")) {
 						parse_cooldown_time = false;
 						try {
 							max_uses = Integer.parseInt(parameters[j].substring(5));
 						} catch (NumberFormatException exception) {
 							error = true;
 							j = parameters.length;
 						}
 					} else if (parameters[j].equalsIgnoreCase("global:true")) {
 						parse_cooldown_time = false;
 						global = true;
 					} else if (parse_cooldown_time) {
 						try {
 							previous_number = Double.parseDouble(parameters[j]);
 						} catch (NumberFormatException exception) {
 							if (parameters[j].equalsIgnoreCase("days"))
 								cooldown_time = (int) (cooldown_time + previous_number * 86400000);
 							else if (parameters[j].equalsIgnoreCase("hours"))
 								cooldown_time = (int) (cooldown_time + previous_number * 3600000);
 							else if (parameters[j].equalsIgnoreCase("minutes"))
 								cooldown_time = (int) (cooldown_time + previous_number * 60000);
 							else if (parameters[j].equalsIgnoreCase("seconds"))
 								cooldown_time = (int) (cooldown_time + previous_number * 1000);
 							else
 								error = true;
 						}
 					}
 				}
 				if (!error) {
 					// figure out the switch type
 					String switch_type = null;
 					if (target_block.getTypeId() == 63 || target_block.getTypeId() == 68)
 						switch_type = "sign";
 					else if (target_block.getTypeId() == 69)
 						switch_type = "lever";
 					else if (target_block.getTypeId() == 70 || target_block.getTypeId() == 72)
 						switch_type = "pressure plate";
 					else
 						switch_type = "button";
 					// find out where the new switch needs to be in the list to
 					// be properly alphabetized
 					int insertion_index = 0;
 					for (UltraSwitch my_switch : switches)
 						if (my_switch.getWarpName().compareToIgnoreCase(warp.getName()) < 0
 								|| (my_switch.getWarpName().compareToIgnoreCase(warp.getName()) == 0 && my_switch.getWarpOwner().compareToIgnoreCase(
 										warp.getOwner()) <= 0))
 							insertion_index++;
 					// make the switch
 					switches.add(insertion_index, new UltraSwitch(warp.getName(), warp.getOwner(), switch_type, cooldown_time, max_uses, global, cost,
 							exempted_players, target_block.getLocation().getX(), target_block.getLocation().getY(), target_block.getLocation().getZ(),
 							target_block.getLocation().getWorld()));
 					if (autosave_switches)
 						saveTheSwitches(sender, false);
 					if (player.getName().toLowerCase().startsWith(warp.getOwner().toLowerCase()))
 						player.sendMessage(ChatColor.GREEN + "You linked \"" + warp.getName() + "\" to this " + switch_type + ".");
 					else
 						player.sendMessage(ChatColor.GREEN + "You linked " + warp.getOwner() + "'s \"" + warp.getName() + "\" to this " + switch_type + ".");
 				} else
 					player.sendMessage(ChatColor.RED + "Don't mix numbers with letters. Try again please.");
 			} else if (warp != null)
 				player.sendMessage(ChatColor.RED + "You're not allowed to link warps that don't belong to you!");
 			else {
 				if (player.getName().equals(owner))
 					player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 				else
 					player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 			}
 		} else if (target_block != null)
 			player.sendMessage(ChatColor.RED + "You can only link warps to buttons, pressure plates, or levers.");
 		else
 			player.sendMessage(ChatColor.RED + "Please point at the switch you want to link your warp to and try " + ChatColor.GREEN + "/link " + ChatColor.RED
 					+ "again.");
 	}
 
 	private void moveWarp(int extra_param, CommandSender sender) {
 		Player player = (Player) sender;
 		UltraWarp warp = locateWarp(extra_param, sender);
 		// change the location of the warp or tell the player it can't be done
 		if (warp != null
 				&& (player.getName().equals(warp.getOwner()) || player.hasPermission("myultrawarps.change.other") || player.hasPermission("myultrawarps.admin"))) {
 			warps.set(index, new UltraWarp(warp.getOwner(), warp.getName(), warp.isListed(), warp.isRestricted(), warp.getWarpMessage(), warp
 					.getNoWarpMessage(), warp.getListedUsers(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player
 					.getLocation().getPitch(), player.getLocation().getYaw(), player.getLocation().getWorld()));
 			if (autosave_warps)
 				saveTheWarps(sender, false);
 			if (player.getName().equals(warp.getOwner()))
 				player.sendMessage(ChatColor.GREEN + "You moved \"" + warps.get(index).getName() + ".\"");
 			else
 				player.sendMessage(ChatColor.GREEN + "You moved " + warps.get(index).getOwner() + "'s warp \"" + warps.get(index).getName() + ".\"");
 		} else if (warp != null)
 			player.sendMessage(ChatColor.RED + "You don't have permission to modify this warp.");
 		else if (player.getName().equals(owner))
 			player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 		else
 			player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 	}
 
 	private void send(CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		Player target_player = null;
 		for (Player my_player : server.getOnlinePlayers())
 			if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
 				target_player = my_player;
 		int extra_param = 0;
 		if (parameters[1].equalsIgnoreCase("to"))
 			extra_param++;
 		if (target_player != null) {
 			if (parameters[1 + extra_param].equalsIgnoreCase("there")) {
 				if (player != null) {
 					Block target_block = player.getTargetBlock(null, 1024);
 					if (target_block != null) {
 						Location target_location = target_block.getLocation();
 						target_location.setY(target_location.getY() + 1);
 						if (teleport(target_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before "
 								+ player.getName() + " teleported you elsewhere.", "", null, target_player.getLocation()),
 								new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at when you were teleported by "
 										+ player.getName() + ".", "", null, target_location), false, player)) {
 							target_player.sendMessage(ChatColor.GREEN + player.getName() + " teleported you here.");
 							if (target_player.getName().toLowerCase().startsWith("a") || target_player.getName().toLowerCase().startsWith("e")
 									|| target_player.getName().toLowerCase().startsWith("i") || target_player.getName().toLowerCase().startsWith("o")
 									|| target_player.getName().toLowerCase().startsWith("u"))
 								player.sendMessage(ChatColor.GREEN + "Hark! Over yonder! An " + target_player.getName() + " cometh!");
 							else
 								player.sendMessage(ChatColor.GREEN + "Hark! Over yonder! A " + target_player.getName() + " cometh!");
 						}
 					} else
 						player.sendMessage(ChatColor.RED + "The block you targeted is too far away.");
 				} else
 					sender.sendMessage(ChatColor.RED + "Please point out the place you want to teleport " + target_player.getName()
 							+ ". Oh, yeah. You still can't. You're still a console.");
 			} else if (parameters[1 + extra_param].equalsIgnoreCase("warp")) {
 				if (parameters.length >= 3 + extra_param) {
 					UltraWarp warp = locateWarp(2 + extra_param, sender);
 					if (warp != null) {
 						// save the player's location before warping
 						String sender_name = "someone";
 						if (player != null)
 							sender_name = player.getName();
 						if (teleport(target_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before " + sender_name
 								+ " teleported you elsewhere.", "", null, target_player.getLocation()), warp, false, player)) {
 							if (sender_name.equals("someone"))
 								sender_name = "Someone";
 							target_player
 									.sendMessage(ChatColor.GREEN + sender_name + " telported you to " + warp.getOwner() + "'s \"" + warp.getName() + ".\"");
 							if (player != null && warp.getOwner().equals(player.getName()))
 								player.sendMessage(ChatColor.GREEN + "I sent " + target_player.getName() + " to \"" + warp.getName() + ".\"");
 							else
 								sender.sendMessage(ChatColor.GREEN + "I sent " + target_player.getName() + " to " + warp.getOwner() + "'s \"" + warp.getName()
 										+ ".\"");
 						}
 					} else if (player != null && (owner == null || player.getName().equals(owner)))
 						player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 					else if (owner != null)
 						sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 				} else
 					sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp you want to warp " + target_player.getName() + " to!");
 			} else if (parameters[1 + extra_param].equalsIgnoreCase("player")) {
 				if (parameters.length >= 3 + extra_param) {
 					Player final_destination_player = null;
 					for (Player online_player : server.getOnlinePlayers()) {
 						if (online_player.getName().toLowerCase().startsWith(parameters[2 + extra_param].toLowerCase()))
 							final_destination_player = online_player;
 					}
 					if (final_destination_player != null) {
 						// save the player's location before warping
 						String sender_name = "someone";
 						if (player != null)
 							sender_name = player.getName();
 						if (teleport(target_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before " + sender_name
 								+ " teleported you elsewhere.", "", null, target_player.getLocation()), new UltraWarp("God", "coordinates", false, false,
 								"&aThis is the spot you were at when you were teleported by " + player.getName() + ".", "", null, target_player.getLocation()),
 								false, sender)) {
 							target_player.sendMessage(ChatColor.GREEN + sender_name + " teleported you to " + final_destination_player.getName() + ".");
 							sender.sendMessage(ChatColor.GREEN + "I sent " + target_player.getName() + " to " + final_destination_player.getName() + ".");
 						}
 					} else {
 						for (OfflinePlayer offline_player : server.getOfflinePlayers())
 							if (offline_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase())) {
 								sender.sendMessage(ChatColor.RED + offline_player.getName() + " is not online right now.");
 								return;
 							}
 						sender.sendMessage(ChatColor.RED + "Sorry, but I've never seen anyone with a name starting with \"" + parameters[0]
 								+ "\" come on this server.");
 					}
 				} else
 					sender.sendMessage(ChatColor.RED + "You forgot to tell me which player you want me to warp " + target_player.getName() + " to!");
 			} else
 				sender.sendMessage("/send [player] (\"to\") [\"there\"/\"warp\" [warp]/\"player\" [player]]");
 		} else {
 			for (OfflinePlayer offline_player : server.getOfflinePlayers())
 				if (offline_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase())) {
 					sender.sendMessage(ChatColor.RED + offline_player.getName() + " is not online right now.");
 					return;
 				}
 			sender.sendMessage(ChatColor.RED + "Sorry, but I've never seen anyone with a name starting with \"" + parameters[0] + "\" come on this server.");
 		}
 	}
 
 	private void setHome(CommandSender sender) {
 		Player player = (Player) sender;
 		int extra_param = 0;
 		if (parameters != null && parameters.length > 0 && parameters[0].equalsIgnoreCase("home"))
 			extra_param++;
 		String owner = null;
 		// check if the home is for someone else
 		if (parameters != null && parameters.length > extra_param && parameters[extra_param].toLowerCase().endsWith("'s"))
 			owner = getFullName(parameters[extra_param].substring(0, parameters[extra_param].length() - 2));
 		else
 			owner = player.getName();
 		if ((player.getName().toLowerCase().startsWith(owner.toLowerCase())) || player.hasPermission("myultrawarps.sethome.other")
 				|| player.hasPermission("myultrawarps.admin")) {
 			// delete the old home if it exists
 			for (int i = 0; i < warps.size(); i++)
 				if (warps.get(i).getName().equals("home") && warps.get(i).getOwner().toLowerCase().startsWith(owner.toLowerCase()))
 					warps.remove(i);
 			// set the new home
 			warps.add(new UltraWarp(owner, "home", false, true, "&aWelcome home, " + owner + ". We have awaited your return.",
 					"&cYou're not allowed to just warp to other people's homes! The nerve!", new String[0], player.getLocation().getX(), player.getLocation()
 							.getY(), player.getLocation().getZ(), player.getLocation().getPitch(), player.getLocation().getYaw(), player.getWorld()));
 			if (autosave_warps)
 				saveTheWarps(sender, false);
 			if (owner.equals(player.getName()))
 				player.sendMessage(ChatColor.GREEN + "Henceforth, this shall be your new home.");
 			else
 				player.sendMessage(ChatColor.GREEN + "Henceforth, this shall be " + owner + "'s new home.");
 		} else
 			player.sendMessage(ChatColor.RED + "You can't set someone else's home!");
 	}
 
 	private void setSpawn(CommandSender sender) {
 		Player player = (Player) sender;
 		player.getWorld().setSpawnLocation(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
 		String world_name;
 		if (player.getWorld().getWorldFolder().getName().endsWith("_nether"))
 			world_name = "The Nether";
 		else if (player.getWorld().getWorldFolder().getName().endsWith("_the_end"))
 			world_name = "The End";
 		else
 			world_name = player.getWorld().getWorldFolder().getName();
 		player.sendMessage(ChatColor.GREEN + "Henceforth, this shall be " + world_name + "'s new spawn point.");
 	}
 
 	private void spawn(CommandSender sender) {
 		Player player = (Player) sender;
 		String world_name = player.getWorld().getWorldFolder().getName();
 		if (world_name.endsWith("_nether"))
 			world_name = "The Nether";
 		else if (world_name.endsWith("_the_end"))
 			world_name = "The End";
 		String warp_message = spawn_messages_by_world.get(player.getWorld());
 		if (warp_message == null)
 			warp_message = ChatColor.GREEN + "Welcome to " + player.getWorld().getWorldFolder().getName() + ", " + player.getName() + ".";
 		teleport(player, new UltraWarp(ChatColor.GREEN + "This is the spot you were at before you teleported to " + world_name + "'s spawn point.", player
 				.getLocation()), new UltraWarp(warp_message, player.getWorld().getSpawnLocation()), true, null);
 	}
 
 	private void switchList(CommandSender sender) {
 		Player player = null;
 		String output = "";
 		if (sender instanceof Player)
 			player = (Player) sender;
 		if (switches.size() > 0 && player == null)
 			fullSwitchList(sender);
 		else if (switches.size() > 0) {
 			ArrayList<UltraWarp> switch_warps = new ArrayList<UltraWarp>();
 			ArrayList<Integer> switch_warp_quantities = new ArrayList<Integer>();
 			for (int i = 0; i < switches.size(); i++) {
 				if (switches.get(i).getWarpOwner().equals(player.getName())) {
 					// locate the warp
 					UltraWarp warp = null;
 					for (UltraWarp my_warp : warps)
 						if (my_warp.getName().equals(switches.get(i).getWarpName()) && my_warp.getOwner().equals(switches.get(i).getWarpOwner()))
 							warp = my_warp;
 					if (warp == null) {
 						switches.remove(i);
 						i--;
 					} else {
 						int counter = 0;
 						int index = -1;
 						for (int j = 0; j < switch_warps.size(); j++)
 							if (switch_warps.get(j).equals(warp)) {
 								counter = switch_warp_quantities.get(j);
 								index = j;
 								j = switch_warps.size();
 							}
 						if (counter == 0) {
 							switch_warps.add(warp);
 							switch_warp_quantities.add(1);
 						} else {
 							switch_warp_quantities.remove(index);
 							switch_warp_quantities.add(index, counter + 1);
 						}
 					}
 				}
 			}
 			if (switch_warps.size() > 0) {
 				output = ChatColor.GREEN + "your switches: ";
 				for (int i = 0; i < switch_warps.size(); i++) {
 					UltraWarp warp = switch_warps.get(i);
 					if (i > 0)
 						output = output + ", ";
 					output = output + warp.getColoredName();
 					output = output + ChatColor.WHITE + " x" + switch_warp_quantities.get(i);
 				}
 			} else
 				output = ChatColor.RED + "You don't have any switches yet!";
 			player.sendMessage(output);
 		} else
 			sender.sendMessage(ChatColor.RED + "No one has made any switches yet!");
 	}
 
 	private void switchInfo(int extra_param, CommandSender sender) {
 		// sign post=63, wall sign=68, lever=69, stone pressure plate=70, wooden
 		// pressure plate=72, stone button=77, wooden button = 143
 		Player player = null;
 		Block target_block = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		if (player != null)
 			target_block = player.getTargetBlock(null, 1024);
 		if (parameters.length > extra_param) {
 			locateWarp(extra_param, sender);
 			if (player == null || (player.getName().toLowerCase().startsWith(owner.toLowerCase())) || player.hasPermission("myultrawarps.switchinfo.other")
 					|| player.hasPermission("myultrawarps.admin")) {
 				// find all the switches linked to the specified warp
 				ArrayList<UltraSwitch> temp = new ArrayList<UltraSwitch>();
 				for (UltraSwitch my_switch : switches)
 					if (my_switch.getWarpOwner().equals(owner) && my_switch.getWarpName().toLowerCase().startsWith(name.toLowerCase()))
 						temp.add(my_switch);
 				if (temp.size() == 0) {
 					if (player == null)
 						console.sendMessage(ChatColor.GREEN + "There are no switches linked to " + owner + "'s warp \"" + name + ".\"");
 					else if (player.getName().equals(owner))
 						player.sendMessage(ChatColor.GREEN + "There are no switches linked to \"" + name + ".\"");
 					else
 						player.sendMessage(ChatColor.GREEN + "There are no switches linked to " + owner + "'s warp \"" + name + ".\"");
 				} else if (temp.size() > 0) {
 					for (UltraSwitch my_switch : temp) {
 						if (player == null)
 							console.sendMessage(ChatColor.WHITE + colorCode(my_switch.getSaveLine()));
 						else
 							player.sendMessage(ChatColor.WHITE + colorCode(my_switch.getSaveLine()));
 					}
 				}
 			} else
 				player.sendMessage(ChatColor.RED + "You don't have permission to see info on other people's switches.");
 		} else if (target_block != null
 				&& (target_block.getTypeId() == 63 || target_block.getTypeId() == 68 || target_block.getTypeId() == 69 || target_block.getTypeId() == 70
 						|| target_block.getTypeId() == 72 || target_block.getTypeId() == 77 || target_block.getTypeId() == 143)) {
 			// get information by the switch the player is pointing at
 			UltraSwitch switch_found = null;
 			String block_type;
 			if (target_block.getTypeId() == 63 || target_block.getTypeId() == 68)
 				block_type = "sign";
 			else if (target_block.getTypeId() == 69)
 				block_type = "lever";
 			else if (target_block.getTypeId() == 70 || target_block.getTypeId() == 72)
 				block_type = "pressure plate";
 			else
 				block_type = "button";
 			for (UltraSwitch my_switch : switches)
 				if (my_switch.getX() == target_block.getX() && my_switch.getY() == target_block.getY() && my_switch.getZ() == target_block.getZ()
 						&& my_switch.getWorld().equals(target_block.getWorld()) && my_switch.getSwitchType().equals(block_type))
 					switch_found = my_switch;
 			if (switch_found != null
 					&& (player == null || switch_found.getWarpOwner().equals(player.getName()) || player.hasPermission("myultrawarps.switchinfo.other") || player
 							.hasPermission("myultrawarps.admin"))) {
 				if (player == null)
 					console.sendMessage(ChatColor.WHITE + switch_found.getSaveLine());
 				else
 					player.sendMessage(ChatColor.WHITE + switch_found.getSaveLine());
 			} else if (switch_found == null) {
 
 				if (player == null)
 					console.sendMessage(ChatColor.GREEN + "There are no warps linked to this " + block_type + ".");
 				else
 					player.sendMessage(ChatColor.GREEN + "There are no warps linked to this " + block_type + ".");
 			} else
 				player.sendMessage(ChatColor.RED + "You don't have permission to see info on other people's switches.");
 		} else if (!(sender instanceof Player))
 			console.sendMessage(ChatColor.RED + "You must specify a warp for me to check if any switches are linked to it.");
 		else
 			player.sendMessage(ChatColor.RED + "You must either specify a warp for me to check or point at a switch for me to check.");
 	}
 
 	private void to(CommandSender sender) {
 		Player player = (Player) sender;
 		Object[] data = default_settings;
 		if (permissions != null && permissions.getPrimaryGroup(player) != null && group_settings.containsKey(permissions.getPrimaryGroup(player)))
 			data = group_settings.get(permissions.getPrimaryGroup(player));
 		if (per_player_settings.containsKey(player.getName()))
 			data = per_player_settings.get(player.getName());
 		// find the target player
 		Player target_player = null;
 		for (Player my_player : server.getOnlinePlayers())
 			if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()) && !my_player.equals(player))
 				target_player = my_player;
 		// teleport the player to him/her or say it can't be done
 		if (target_player != null
 				&& (blocked_players.get(target_player.getName()) == null || !blocked_players.get(target_player.getName()).contains(player.getName()))) {
 			if (!(Boolean) data[0] || player.hasPermission("myultrawarps.admin")
 					|| (from_teleport_requests.get(player.getName()) != null && from_teleport_requests.get(player.getName()).contains(target_player.getName()))) {
 				// remove any to teleportation requests from the target player
 				ArrayList<String> requesting_players = from_teleport_requests.get(player.getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				while (requesting_players.contains(target_player.getName()))
 					requesting_players.remove(target_player.getName());
 				from_teleport_requests.put(player.getName(), requesting_players);
 				if (teleport(player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you teleported to "
 						+ target_player.getName() + ".", "", null, player.getLocation()), new UltraWarp("God", "coordinates", false, false,
 						"&aThis is the spot you were at when you teleported to " + target_player.getName() + ".", "", null, target_player.getLocation()),
 						false, target_player)) {
 					if (player.getName().toLowerCase().startsWith("a") || player.getName().toLowerCase().startsWith("e")
 							|| player.getName().toLowerCase().startsWith("i") || player.getName().toLowerCase().startsWith("o")
 							|| player.getName().toLowerCase().startsWith("u"))
 						player.sendMessage(ChatColor.GREEN + "You found an " + target_player.getName() + "!");
 					else
 						player.sendMessage(ChatColor.GREEN + "You found a " + target_player.getName() + "!");
 					target_player.sendMessage(ChatColor.GREEN + player.getName() + " has come to visit you.");
 				}
 			} else {
 				player.sendMessage(ChatColor.GREEN + "Hang on. Let me ask " + target_player.getName() + " if it's okay.");
 				target_player.sendMessage(ChatColor.GREEN + player.getName() + " would like to teleport to you. Is that okay?");
 				ArrayList<String> requesting_players = to_teleport_requests.get(target_player.getName());
 				if (requesting_players == null)
 					requesting_players = new ArrayList<String>();
 				requesting_players.add(player.getName());
 				to_teleport_requests.put(target_player.getName(), requesting_players);
 			}
 		} else if (target_player != null && blocked_players.get(target_player.getName()).contains(player.getName()))
 			player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName()
 					+ " has blocked you. You can't send them teleportation requests anymore.");
 		else if (target_player == null)
 			if (player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
 				player.sendMessage(ChatColor.RED + "You can't teleport to yourself! That makes no sense!");
 			else
 				player.sendMessage(ChatColor.RED + "I couldn't find \"" + parameters[0] + "\" anywhere.");
 	}
 
 	private void top(CommandSender sender) {
 		Player player = (Player) sender;
 		Location target_location = null, lower_location = null;
 		for (int i = 0; i < 257; i++) {
 			Location temp1 =
 					new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, i, player.getLocation().getBlockZ() + 0.5, player.getLocation()
 							.getYaw(), player.getLocation().getPitch());
 			Location temp2 =
 					new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, i + 1, player.getLocation().getBlockZ() + 0.5, player.getLocation()
 							.getYaw(), player.getLocation().getPitch());
 			// non-solid blocks: air=0, sapling=6, bed=26, powered rail=27, detector rail=28, cobweb=30, tall grass=31, dead bush=32, flower=37, rose=38, brown
 			// mushroom=39, red mushroom=40, torch=50, fire=51, redstone wire=55, wheat=59, floor sign=63, wooden door=64, ladder=65, unpowered rail=66, wall
 			// sign=68, lever=69, stone pressure plate=70, iron door=71, wooden pressure plate=72, "off" redstone torch=75, "on" redstone torch=76, stone
 			// button=77, snow=78, sugar cane=83, portal=90, "off" redstone repeater=93, "on" redstone repeater=94, pumpkin stem=104, melon stem=105, lily
 			// pad=111, Nether wart=115, End portal=119, tripwire hook=131, tripwire=132, flower pot=140, carrots=141, potatoes=142, wooden button=143, monster
 			// head=144
 			if (temp1.getBlock() != null
 					&& temp2.getBlock() != null
 					&& !(temp1.getBlock().getTypeId() == 0 || temp1.getBlock().getTypeId() == 6 || temp1.getBlock().getTypeId() == 26
 							|| temp1.getBlock().getTypeId() == 27 || temp1.getBlock().getTypeId() == 28 || temp1.getBlock().getTypeId() == 30
 							|| temp1.getBlock().getTypeId() == 31 || temp1.getBlock().getTypeId() == 32 || temp1.getBlock().getTypeId() == 37
 							|| temp1.getBlock().getTypeId() == 38 || temp1.getBlock().getTypeId() == 39 || temp1.getBlock().getTypeId() == 40
 							|| temp1.getBlock().getTypeId() == 50 || temp1.getBlock().getTypeId() == 64 || temp1.getBlock().getTypeId() == 65
 							|| temp1.getBlock().getTypeId() == 66 || temp1.getBlock().getTypeId() == 68 || temp1.getBlock().getTypeId() == 69
 							|| temp1.getBlock().getTypeId() == 70 || temp1.getBlock().getTypeId() == 71 || temp1.getBlock().getTypeId() == 72
 							|| temp1.getBlock().getTypeId() == 75 || temp1.getBlock().getTypeId() == 76 || temp1.getBlock().getTypeId() == 77
 							|| temp1.getBlock().getTypeId() == 78 || temp1.getBlock().getTypeId() == 83 || temp1.getBlock().getTypeId() == 90
 							|| temp1.getBlock().getTypeId() == 93 || temp1.getBlock().getTypeId() == 94 || temp1.getBlock().getTypeId() == 104
 							|| temp1.getBlock().getTypeId() == 105 || temp1.getBlock().getTypeId() == 111 || temp1.getBlock().getTypeId() == 115
 							|| temp1.getBlock().getTypeId() == 119 || temp1.getBlock().getTypeId() == 131 || temp1.getBlock().getTypeId() == 132
 							|| temp1.getBlock().getTypeId() == 140 || temp1.getBlock().getTypeId() == 141 || temp1.getBlock().getTypeId() == 142
 							|| temp1.getBlock().getTypeId() == 143 || temp1.getBlock().getTypeId() == 144)
 					&& (temp2.getBlock().getTypeId() == 0 || temp2.getBlock().getTypeId() == 6 || temp2.getBlock().getTypeId() == 26
 							|| temp2.getBlock().getTypeId() == 27 || temp2.getBlock().getTypeId() == 28 || temp2.getBlock().getTypeId() == 30
 							|| temp2.getBlock().getTypeId() == 31 || temp2.getBlock().getTypeId() == 32 || temp2.getBlock().getTypeId() == 37
 							|| temp2.getBlock().getTypeId() == 38 || temp2.getBlock().getTypeId() == 39 || temp2.getBlock().getTypeId() == 40
 							|| temp2.getBlock().getTypeId() == 50 || temp2.getBlock().getTypeId() == 64 || temp2.getBlock().getTypeId() == 65
 							|| temp2.getBlock().getTypeId() == 66 || temp2.getBlock().getTypeId() == 68 || temp2.getBlock().getTypeId() == 69
 							|| temp2.getBlock().getTypeId() == 70 || temp2.getBlock().getTypeId() == 71 || temp2.getBlock().getTypeId() == 72
 							|| temp2.getBlock().getTypeId() == 75 || temp2.getBlock().getTypeId() == 76 || temp2.getBlock().getTypeId() == 77
 							|| temp2.getBlock().getTypeId() == 78 || temp2.getBlock().getTypeId() == 83 || temp2.getBlock().getTypeId() == 90
 							|| temp2.getBlock().getTypeId() == 93 || temp2.getBlock().getTypeId() == 94 || temp2.getBlock().getTypeId() == 104
 							|| temp2.getBlock().getTypeId() == 105 || temp2.getBlock().getTypeId() == 111 || temp2.getBlock().getTypeId() == 115
 							|| temp2.getBlock().getTypeId() == 119 || temp2.getBlock().getTypeId() == 131 || temp2.getBlock().getTypeId() == 132
 							|| temp2.getBlock().getTypeId() == 140 || temp2.getBlock().getTypeId() == 141 || temp2.getBlock().getTypeId() == 142
 							|| temp2.getBlock().getTypeId() == 143 || temp2.getBlock().getTypeId() == 144)) {
 				lower_location = target_location;
 				target_location = temp2;
 			}
 		}
 		// make sure /top in the Nether doesn't put you above the ceiling
 		if (player.getWorld().getWorldFolder().getName().endsWith("_nether"))
 			target_location = lower_location;
 		if (target_location != null)
 			teleport(player, null, new UltraWarp(ChatColor.GREEN + "You've reached the top!", target_location), true, null);
 		else
 			player.sendMessage(ChatColor.RED + "I can't find any solid blocks anywhere above or below you! What gives?");
 	}
 
 	private void unblock(CommandSender sender) {
 		Player player = (Player) sender;
 		String blocked_player = getFullName(parameters[0]);
 		if (blocked_players.get(player.getName()) != null && blocked_players.get(player.getName()).contains(blocked_player)) {
 			ArrayList<String> my_blocked_players = blocked_players.get(player.getName());
 			my_blocked_players.remove(blocked_player);
 			blocked_players.put(player.getName(), my_blocked_players);
 		} else
 			player.sendMessage(ChatColor.RED + "You never blocked " + blocked_player + ".");
 	}
 
 	private void unlinkWarp(int extra_param, CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		// sign post=63, wall sign=68, lever=69, stone pressure plate=70, wooden
 		// pressure plate=72, stone button=77, wooden button=143
 		Block target_block = null;
 		if (player != null)
 			target_block = player.getTargetBlock(null, 1024);
 		if (parameters.length > extra_param) {
 			// unlink all switches associated with a warp
 			locateWarp(extra_param, sender);
 			if (owner != null) {
 				if (player == null || (player.getName().equals(owner)) || player.hasPermission("myultrawarps.unlink.other")
 						|| player.hasPermission("myultrawarps.admin")) {
 					// locate the switches as specified and delete them
 					for (int i = 0; i < switches.size(); i++) {
 						if (switches.get(i).getWarpName().toLowerCase().startsWith(name.toLowerCase()) && switches.get(i).getWarpOwner().equals(owner)) {
 							switches.remove(i);
 							i--;
 						}
 					}
 					if (autosave_switches)
 						saveTheSwitches(sender, false);
 					if (player == null)
 						console.sendMessage(ChatColor.GREEN + "I unlinked all of the switches linked to " + owner + "'s warp \"" + name + ".\"");
 					else if (player.getName().equals(owner))
 						player.sendMessage(ChatColor.GREEN + "I unlinked all the switches linked to \"" + name + ".\"");
 					else
 						player.sendMessage(ChatColor.GREEN + "I unlinked all of the switches linked to " + owner + "'s warp \"" + name + ".\"");
 				} else
 					player.sendMessage(ChatColor.RED + "You don't have permission to unlink other people's switches.");
 			} else
 				console.sendMessage(ChatColor.RED + "You need to specify the owner's name. I can't look through your own warps! You're a console!");
 		} else if (target_block != null
 				&& (target_block.getTypeId() == 63 || target_block.getTypeId() == 68 || target_block.getTypeId() == 69 || target_block.getTypeId() == 70
 						|| target_block.getTypeId() == 72 || target_block.getTypeId() == 77 || target_block.getTypeId() == 143)) {
 			if (player != null) {
 				// unlink a single switch
 				int index = -1;
 				String block_type;
 				if (target_block.getTypeId() == 63 || target_block.getTypeId() == 68)
 					block_type = "sign";
 				else if (target_block.getTypeId() == 69)
 					block_type = "lever";
 				else if (target_block.getTypeId() == 77 || target_block.getTypeId() == 143)
 					block_type = "button";
 				else
 					block_type = "pressure plate";
 				for (int i = 0; i < switches.size(); i++)
 					if (target_block.getX() == switches.get(i).getX() && target_block.getY() == switches.get(i).getY()
 							&& target_block.getZ() == switches.get(i).getZ() && switches.get(i).getWorld().equals(target_block.getWorld())
 							&& switches.get(i).getSwitchType().equals(block_type))
 						index = i;
 				if (index != -1
 						&& (player.getName().equals(switches.get(index).getWarpOwner()) || player.hasPermission("myultrawarps.unlink.other") || player
 								.hasPermission("myultrawarps.admin"))) {
 					if (player.getName().equals(switches.get(index).getWarpOwner()))
 						player.sendMessage(ChatColor.GREEN + "You unlinked \"" + switches.get(index).getWarpName() + "\" from this " + block_type + ".");
 					else
 						player.sendMessage(ChatColor.GREEN + "You unlinked " + switches.get(index).getWarpOwner() + "'s \"" + switches.get(index).getWarpName()
 								+ "\" from this " + block_type + ".");
 					switches.remove(index);
 					if (autosave_switches)
 						saveTheSwitches(sender, false);
 				} else if (index == -1)
 					player.sendMessage(ChatColor.RED + "That " + block_type + " doesn't have a warp linked to it.");
 				else
 					player.sendMessage(ChatColor.RED + "You're not allowed to unlink other people's warps.");
 
 			} else
 				console.sendMessage(ChatColor.RED
 						+ "You need to specify the warp that you want to unlink all switches from because you can't point out a specific switch to me...'cause you're a console!");
 		} else if (player != null)
 			player.sendMessage(ChatColor.RED
 					+ "You can either point out a switch you want to unlink a warp from or specify a warp that I will unlink all switches from.");
 		else
 			console.sendMessage(ChatColor.RED
 					+ "You need to specify the warp that you want to unlink all switches from because you can't point out a specific switch to me...'cause you're a console!");
 	}
 
 	private void warp(CommandSender sender) {
 		Player player = (Player) sender;
 		UltraWarp warp = locateWarp(0, sender);
 		if (warp != null) {
 			boolean listed = false;
 			if (warp.getListedUsers() != null && warp.getListedUsers().length > 0)
 				for (String user : warp.getListedUsers())
 					if (player.getName().equalsIgnoreCase(user))
 						listed = true;
 			if (warp.getOwner().equals(player.getName()) || player.hasPermission("myultrawarps.warptowarp.other") || player.hasPermission("myultrawarps.admin")
 					|| (!warp.isRestricted() && !listed) || (warp.isRestricted() && listed)) {
 				// save the player's location before warping
 				String warp_name = warp.getName();
 				if (!warp.getOwner().equals(player.getName()))
 					warp_name = warp.getOwner() + "'s " + warp.getName();
 				teleport(player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you warped to " + warp_name + ".",
 						"", null, player.getLocation()), warp, true, null);
 			} else
 				player.sendMessage(colorCode(replaceAll(warp.getNoWarpMessage(), "[player]", player.getName())));
 		} else {
 			// tell the player the warp wasn't found
 			if (player.getName().equals(owner))
 				player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 			else
 				player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 		}
 	}
 
 	private void warpAll(int extra_param, CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		if (parameters[extra_param].equalsIgnoreCase("to"))
 			extra_param++;
 		if (parameters[extra_param].equalsIgnoreCase("here")) {
 			if (player != null) {
 				for (Player everyone : server.getOnlinePlayers()) {
 					if (player == null || !everyone.equals(player)) {
 						teleport(everyone, new UltraWarp(ChatColor.GREEN + "This is the spot you were at before " + player.getName()
 								+ " teleported everyone to them.", everyone.getLocation()), new UltraWarp(ChatColor.GREEN + "This is the spot where "
 								+ player.getName() + " teleported everyone to them.", player.getLocation()), false, player);
 						everyone.sendMessage(ChatColor.GREEN + player.getName() + " brought everyone to this location for something important.");
 					}
 				}
 				player.sendMessage(ChatColor.GREEN + "Everyone is present and accounted for.");
 			} else
 				console.sendMessage(ChatColor.RED + "I can't warp anyone to you! You have no location!");
 		} else if (parameters[extra_param].equalsIgnoreCase("there")) {
 			if (player != null) {
 				Block target_block = player.getTargetBlock(null, 1024);
 				if (target_block != null) {
 					Location target_location = target_block.getLocation();
 					target_location.setY(target_location.getY() + 1);
 					if (target_location.getBlock().getTypeId() != 0) {
 						target_location.getChunk().load();
 						for (Player everyone : server.getOnlinePlayers()) {
 							if (player == null || !everyone.equals(player)) {
 								teleport(everyone, new UltraWarp(ChatColor.GREEN + "This is the spot you were at before " + player.getName()
 										+ " teleported everyone elsewhere.", everyone.getLocation()), new UltraWarp(ChatColor.GREEN + "This is the spot where "
 										+ player.getName() + " teleported everyone.", target_location), false, player);
 								everyone.sendMessage(ChatColor.GREEN + player.getName() + " brought everyone to this location for something important.");
 							}
 						}
 						player.sendMessage(ChatColor.GREEN + "Everyone is present and accounted for.");
 					} else
 						player.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
 				} else
 					player.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
 			} else
 				console.sendMessage(ChatColor.RED
 						+ "Please point out the place you want to teleport everyone. Oh, yeah. You still can't. You're still a console.");
 		} else if (parameters[extra_param].equalsIgnoreCase("warp")) {
 			if (parameters.length >= extra_param + 2 && parameters[extra_param + 1] != null && !parameters[extra_param].equals("")) {
 				UltraWarp warp = locateWarp(extra_param + 1, sender);
 				if (warp != null) {
 					warp.getLocation().getChunk().load();
 					for (Player everyone : server.getOnlinePlayers()) {
 						if (player == null || !everyone.equals(player)) {
 							String sender_name = "someone";
 							if (player != null)
 								sender_name = player.getName();
 							teleport(everyone, new UltraWarp(ChatColor.GREEN + "This is the spot you were at before " + sender_name
 									+ " teleported everyone elsewhere.", everyone.getLocation()), new UltraWarp(ChatColor.GREEN + "This is the spot where "
 									+ sender_name + " teleported everyone.", warp.getLocation()), false, sender);
 							if (sender_name.equals("someone"))
 								sender_name = "Someone";
 							everyone.sendMessage(ChatColor.GREEN + sender_name + " brought everyone to this location for something important.");
 						}
 					}
 					if (player != null && warp.getOwner().equals(player.getName()))
 						player.sendMessage(ChatColor.GREEN + "I sent everyone to \"" + warp.getName() + ".\"");
 					else
 						sender.sendMessage(ChatColor.GREEN + "I sent everyone to " + warp.getOwner() + "'s \"" + warp.getName() + ".\"");
 				} else if (player != null && player.getName().equals(owner))
 					player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 				else if (owner != null)
 					sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 				else
 					sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 			} else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp you want to warp everyone to!");
 		} else if (parameters[extra_param].equalsIgnoreCase("player")) {
 			if (parameters.length >= extra_param + 2 && parameters[extra_param + 1] != null && !parameters[extra_param].equals("")) {
 				Player target_player = null;
 				for (Player online_player : server.getOnlinePlayers()) {
 					if (online_player.getName().toLowerCase().startsWith(parameters[extra_param + 1].toLowerCase()))
 						target_player = online_player;
 				}
 				if (target_player != null) {
 					// you don't need to load the chunk because the target player is already there!
 					for (Player everyone : server.getOnlinePlayers()) {
 						if (player == null || !everyone.equals(player)) {
 							String sender_name = "someone";
 							if (player != null)
 								sender_name = player.getName();
 							teleport(everyone, new UltraWarp(ChatColor.GREEN + "This is the spot you were at before " + sender_name
 									+ " teleported everyone elsewhere.", everyone.getLocation()), new UltraWarp(ChatColor.GREEN + "This is the spot where "
 									+ sender_name + " teleported everyone.", target_player.getLocation()), false, sender);
 							if (sender_name.equals("someone"))
 								sender_name = "Someone";
 							everyone.sendMessage(ChatColor.GREEN + sender_name + " brought everyone to this location for something important.");
 						}
 					}
 					player.sendMessage(ChatColor.GREEN + "I teleported everyone to " + target_player.getName() + ".");
 				} else
 					sender.sendMessage(ChatColor.RED + "\"" + parameters[extra_param + 1] + "\" is not online right now.");
 			} else
 				sender.sendMessage(ChatColor.RED + "You forgot to tell me which player you want me to warp everyone to!");
 		}
 	}
 
 	private void warpInfo(int extra_param, CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		UltraWarp warp = locateWarp(extra_param, sender);
 		if (warp != null
 				&& (player == null || player.getName().equals(owner) || player.hasPermission("myultrawarps.warpinfo.other") || player
 						.hasPermission("myultrawarps.admin"))) {
 			String info = warp.getSaveLine();
 			// insert ChatColor.WHITE at the end of warp and no warp messages
 			for (int i = 0; i < info.length(); i++) {
 				if (info.length() > i + warp.getWarpMessage().length()
 						&& info.substring(i, i + warp.getWarpMessage().length() + 1).equals(warp.getWarpMessage() + "\"")) {
 					String previous_text = info.substring(0, i + warp.getWarpMessage().length());
 					String next_text = info.substring(i + warp.getWarpMessage().length());
 					info = previous_text + "&f" + next_text;
 				} else if (info.length() > i + warp.getNoWarpMessage().length()
 						&& info.substring(i, i + warp.getNoWarpMessage().length() + 1).equals(warp.getNoWarpMessage() + "\"")) {
 					String previous_text = info.substring(0, i + warp.getNoWarpMessage().length());
 					String next_text = info.substring(i + warp.getNoWarpMessage().length());
 					info = previous_text + "&f" + next_text;
 				}
 			}
 			sender.sendMessage(colorCode(info));
 		} else if (warp != null)
 			player.sendMessage(ChatColor.RED + "You don't have permission to view information about this warp.");
 		else {
 			// tell the player the warp wasn't found
 			if (player != null && player.getName().equals(owner))
 				player.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 			else if (owner != null)
 				sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + "\" in " + owner + "'s warps.");
 			else
 				sender.sendMessage(ChatColor.RED + "I couldn't find \"" + name + ".\"");
 		}
 	}
 
 	private void warpList(CommandSender sender) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		String your_warps_output = "", other_warps_output = "";
 		if (player != null) {
 			for (UltraWarp warp : warps) {
 				if (warp.getOwner().equals(player.getName())) {
 					if (!your_warps_output.equals(""))
 						your_warps_output = your_warps_output + ChatColor.WHITE + ", " + warp.getColoredName();
 					else
 						your_warps_output = warp.getColoredName();
 				} else if (warp.isListed()) {
 					if (!other_warps_output.equals(""))
 						other_warps_output = other_warps_output + ChatColor.WHITE + ", " + warp.getColoredName();
 					else
 						other_warps_output = warp.getColoredName();
 				}
 			}
 			if (!your_warps_output.equals(""))
 				player.sendMessage(ChatColor.GREEN + "your warps: " + your_warps_output);
 			else
 				player.sendMessage(ChatColor.RED + "You don't have any warps yet!");
 			if (!other_warps_output.equals(""))
 				player.sendMessage(ChatColor.GREEN + "other warps: " + other_warps_output);
 			else
 				player.sendMessage(ChatColor.RED + "No one else has any listed warps yet!");
 		} else {
 			for (UltraWarp warp : warps)
 				if (warp.isListed())
 					if (!other_warps_output.equals(""))
 						other_warps_output = other_warps_output + ChatColor.WHITE + ", " + warp.getColoredName();
 					else
 						other_warps_output = warp.getColoredName();
 			if (!other_warps_output.equals(""))
 				console.sendMessage(ChatColor.GREEN + "listed warps: " + other_warps_output);
 			else
 				console.sendMessage(ChatColor.RED + "No one has made any listed warps yet!");
 		}
 	}
 
 	private void warpToCoordinate(CommandSender sender) {
 		// warp (world) [x] [y] [z] (world)
 		Player player = (Player) sender;
 		double x = 0, y = 0, z = 0;
 		String world_name = "";
 		World world = null;
 		int extra_param = 0;
 		boolean fail = true;
 		while (fail && extra_param + 2 < parameters.length) {
 			fail = false;
 			try {
 				x = Double.parseDouble(parameters[extra_param]);
 				y = Double.parseDouble(parameters[extra_param + 1]);
 				z = Double.parseDouble(parameters[extra_param + 2]);
 			} catch (NumberFormatException exception) {
 				fail = true;
 				if (!world_name.equals(""))
 					world_name = world_name + " " + parameters[extra_param];
 				else
 					world_name = parameters[extra_param];
 				extra_param++;
 			}
 		}
 		if (world_name.equals("") && parameters.length >= 4) {
 			for (int i = 3; i < parameters.length; i++)
 				world_name = world_name + parameters[i];
 		}
 		if (!world_name.equals("")) {
 			for (World my_world : server.getWorlds())
 				if (my_world.getWorldFolder().getName().toLowerCase().startsWith(world_name.toLowerCase()))
 					world = my_world;
 		} else
 			world = player.getWorld();
 		if (!fail && world != null) {
 			String message = ChatColor.GREEN + "Welcome to (" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ").";
 			if (!world.equals(player.getWorld())) {
 				world_name = world.getWorldFolder().getName();
 				if (world_name.endsWith("_nether"))
 					world_name = "The Nether";
 				else if (world_name.endsWith("_the_end"))
 					world_name = "The End";
 				message = ChatColor.GREEN + "&aWelcome to (" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ") in \"" + world_name + ".\"";
 			}
 			// save the player's current location before warping
 			teleport(player, new UltraWarp("&aThis is the spot you were at before you warped to (" + Math.round(x) + ", " + Math.round(y) + ", "
 					+ Math.round(z) + ")", player.getLocation()), new UltraWarp(message, new Location(world, (int) x + 0.5, (int) y, (int) z + 0.5, player
 					.getLocation().getYaw(), player.getLocation().getPitch())), true, null);
 		} else if (world == null) {
 			// if it couldn't find the specified world
 			player.sendMessage(ChatColor.RED + "The world you specified doesn't exist.");
 			if (server.getWorlds().size() == 1)
 				player.sendMessage(ChatColor.RED + "Your server only has one world called \"" + server.getWorlds().get(0).getWorldFolder().getName() + ".\"");
 			else if (server.getWorlds().size() == 2)
 				player.sendMessage(ChatColor.RED + "Your server has two worlds: \"" + server.getWorlds().get(0).getWorldFolder().getName() + "\" and \""
 						+ server.getWorlds().get(1).getWorldFolder().getName() + ".\"");
 			else if (server.getWorlds().size() > 2) {
 				String message = ChatColor.RED + "Your server has " + server.getWorlds().size() + " worlds: ";
 				for (int i = 0; i < server.getWorlds().size(); i++) {
 					if (i < server.getWorlds().size() - 1)
 						message = message + "\"" + server.getWorlds().get(i).getWorldFolder().getName() + "\", ";
 					else
 						message = message + " and \"" + server.getWorlds().get(i).getWorldFolder().getName() + ".\"";
 				}
 				player.sendMessage(message);
 			}
 		} else if (fail) {
 			player.sendMessage(ChatColor.RED + "Either you put too many parameters in for /warp or you put a letter in one of your coordinates.");
 		} else
 			player.sendMessage(ChatColor.RED + "I couldn't find \"" + world_name + ".\"");
 	}
 
 	private void warpsAround(int extra_param, CommandSender sender, String command_label) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		int radius = 20;
 		Location target_location = null;
 		String target_name = null;
 		if (parameters[extra_param].equalsIgnoreCase("player")) {
 			extra_param++;
 			if (parameters.length <= extra_param)
 				sender.sendMessage(ChatColor.RED + "You forgot which player you want the search centered around!");
 			else {
 				for (Player target_player : server.getOnlinePlayers())
 					if (target_player.getName().toLowerCase().startsWith(parameters[extra_param].toLowerCase())) {
 						target_name = target_player.getName();
 						target_location = target_player.getLocation();
 						break;
 					}
 				if (target_location == null)
 					sender.sendMessage(ChatColor.RED + "\"" + parameters[extra_param] + "\" is nowhere to be found.");
 			}
 		} else if (parameters[extra_param].equalsIgnoreCase("warp")) {
 			extra_param++;
 			if (parameters.length > extra_param && parameters[extra_param].toLowerCase().endsWith("'s"))
 				extra_param++;
 			if (parameters.length <= extra_param)
 				sender.sendMessage(ChatColor.RED + "You forgot which warp you want the search centered around!");
 			else {
 				UltraWarp warp = locateWarp(extra_param, sender);
 				if (warp != null) {
 					target_location = new Location(warp.getWorld(), warp.getX(), warp.getY(), warp.getZ());
 					if (player != null && owner.equals(player.getName()))
 						target_name = "\"" + warp.getName() + "\"";
 					else
 						target_name = warp.getOwner() + "'s \"" + warp.getName() + "\"";
 				} else if (player != null && player.getName().equals(owner))
 					sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find \"" + name + ".\"");
 				else
 					sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find " + owner + "'s \"" + name + ".\"");
 			}
 		} else if (parameters[extra_param].equalsIgnoreCase("there")) {
 			target_name = "that spot";
 			if (player == null)
 				sender.sendMessage(ChatColor.RED
 						+ "You want the search centered there? Where? Oh, wait. You're still a console and you still have no fingers to point out a location with.");
 			else
 				target_location = player.getTargetBlock(null, 1024).getLocation();
 			if (target_location.getBlock().getTypeId() == 0)
 				sender.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
 		} else if (parameters[extra_param].equalsIgnoreCase("here") || parameters[extra_param].equalsIgnoreCase("me")) {
 			target_name = "you";
 			if (player == null)
 				sender.sendMessage(ChatColor.RED + "You have no body. How can I center a search around you, huh? Silly...");
 			else
 				target_location = player.getLocation();
 		} else
 			sender.sendMessage(ChatColor.RED + "I don't understand what \"" + parameters[extra_param] + "\" means.");
 		if (target_location != null) {
 			if (parameters.length > extra_param + 1) {
 				try {
 					radius = Integer.parseInt(parameters[extra_param + 1]);
 				} catch (NumberFormatException exception) {
 					sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[extra_param + 1] + "\" an integer?");
 					return;
 				}
 			}
 			ArrayList<UltraWarp> nearby_warps = new ArrayList<UltraWarp>();
 			for (UltraWarp warp : warps)
 				if (warp.getX() >= target_location.getBlockX() - radius && warp.getX() <= target_location.getBlockX() + radius
 						&& warp.getY() >= target_location.getBlockY() - radius && warp.getY() <= target_location.getBlockY() + radius
 						&& warp.getZ() >= target_location.getBlockZ() - radius && warp.getZ() <= target_location.getBlockZ() + radius
 						&& warp.getWorld().equals(target_location.getWorld()))
 					nearby_warps.add(warp);
 			if (nearby_warps.size() > 0) {
 				String output = ChatColor.GREEN + "There are " + nearby_warps.size() + " warps within " + radius + " blocks of " + target_name + ": ";
 				if (nearby_warps.size() == 1)
 					output = ChatColor.GREEN + "There is one warp within " + radius + " blocks of " + target_name + ": ";
 				for (UltraWarp warp : nearby_warps) {
 					if (!output.endsWith(": "))
 						output = output + ChatColor.WHITE + ", ";
 					output = output + warp.getColoredOwner() + "'s " + warp.getName();
 				}
 				sender.sendMessage(output);
 			} else
 				sender.sendMessage(ChatColor.RED + "There are no warps within " + radius + " blocks of " + target_name + ".");
 		}
 	}
 }
