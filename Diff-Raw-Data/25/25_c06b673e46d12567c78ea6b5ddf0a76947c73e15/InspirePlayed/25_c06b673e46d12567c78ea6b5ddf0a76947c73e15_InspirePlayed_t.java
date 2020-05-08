 package com.gmail.melvinchien.InspirePlayed;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * Plugin for Bukkit
  * 
  * @author VeiN
  */
 
 public class InspirePlayed extends JavaPlugin{
 	private final InspirePlayedPlayerListener playerListener = new InspirePlayedPlayerListener(this);
 	public static HashMap<String, ArrayList<Integer>> mapTimes = new HashMap<String, ArrayList<Integer>>();
 	private ArrayList<Integer> arrayTimes = new ArrayList<Integer>();
 	static String mainDirectory = "plugins/InspirePlayed/";
 	static File Playtimes = new File(mainDirectory + "playtimes.txt");
 	Logger log = Logger.getLogger("Minecraft");
 
 
 	public void onEnable() {
 		// Load properties
 		new File(mainDirectory).mkdir();
 		if (!Playtimes.exists()) {
 			try{
 				Playtimes.createNewFile();
 			}catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			mapTimes = loadData(Playtimes);
 		}
 
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Highest, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName() + "] " + pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled.");
 	}
 
 	public void onDisable() {
 		saveData(mapTimes, Playtimes);
 		log.info("[InspirePlayed] InspirePlayed is disabled.");
 	}
 
 	// Save data file
 	public void saveData(HashMap<String, ArrayList<Integer>> playtimes, File file) {
 		try {
			FileOutputStream fileOut = new FileOutputStream(file, false);
 			PrintWriter out = new PrintWriter(fileOut);
 			Iterator it = playtimes.entrySet().iterator();
 			while (it.hasNext()) {
 				Map.Entry<String, ArrayList<Integer>> pairs = (Map.Entry<String, ArrayList<Integer>>)it.next();
 				out.println(pairs.getKey() + "=" + pairs.getValue().get(0));
 			}
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Load data file
 	@SuppressWarnings("unchecked")
 	public HashMap<String, ArrayList<Integer>> loadData(File file) {
 		HashMap<String, ArrayList<Integer>> oldMapTimes = new HashMap<String, ArrayList<Integer>> ();
 		try {
 			FileReader fileIn = new FileReader(file);
 			BufferedReader in = new BufferedReader(fileIn);
 			String inText = "";
 			while (in.ready()) {
				ArrayList<Integer> oldArrayTimes = new ArrayList<Integer>();
 				inText = in.readLine();
 				StringTokenizer st = new StringTokenizer(inText, "=");
 				String player = st.nextToken();
 				oldArrayTimes.add(0, Integer.parseInt(st.nextToken()));
 				oldArrayTimes.add(1, 0);
 				oldMapTimes.put(player, oldArrayTimes);
 			}
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return oldMapTimes;
 	}
 
 	// Check for /played command and display playtime to sender
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("played")) {
 			String player;
 			String message = "";
 			if (args.length == 0) {
 				player = ((Player)sender).getName().toLowerCase();
 				message = "You have played for ";
 			} else if (args[0].equalsIgnoreCase("help")) {
 				return false;
 			} else {
 				player = args[0];
 				message = player + " has played for ";
 			}
 			player = player.toLowerCase();
 			if (!mapTimes.containsKey(player)) {
 				sender.sendMessage(player + " does not exist!");
 				return true;
 			}
 			message += formatPlaytime(getPlaytime(player));
			sender.sendMessage(message);
 			return true;
 		}
 		return false;
 	}
 
 	// Login player and update login time
 	// Time in minutes
 	public void login(String player) {
 		int loginTime = (int)(System.currentTimeMillis() / 1000L);
 		arrayTimes = new ArrayList<Integer>(2);
 		arrayTimes.add(0);
 		arrayTimes.add(0);
 		if (!mapTimes.containsKey(player)) {
 			mapTimes.put(player, arrayTimes);
 		} else {
 			arrayTimes = mapTimes.get(player);			
 		}
 		arrayTimes.set(1, loginTime);
 	}
 
 	// Logout Player
 	public void logout(String player) {
 		logout(player, 0);
 	}
 
 	// Logout player and update stats
 	// Time in minutes
 	public void logout(String player, int currentTime) {
 		int logoutTime = (int)(System.currentTimeMillis() / 1000L);
 		arrayTimes = mapTimes.get(player);
 		if (arrayTimes.get(1).equals(0)) {
 			return; // Do not update player time if they are offline
 		} else {
 			int loginTime = arrayTimes.get(1);
 			int currentPlaytime = (int)((logoutTime - loginTime) / 60);
 			int oldPlaytime = arrayTimes.get(0);
 			int newPlaytime = oldPlaytime + currentPlaytime;
 			arrayTimes.set(0, newPlaytime);
 			arrayTimes.set(1, currentTime);
 			saveData(mapTimes, Playtimes);
 		}
 	}
 
 	// Update playtime
 	public void updatePlaytime(String player) {
 		int currentTime = (int)(System.currentTimeMillis() / 1000L);
 		logout(player, currentTime);
 	}
 
 	// Get a player's playtime in minutes
 	public int getPlaytime(String player) {
 		// Update player's playertime before retrieving
 		updatePlaytime(player);
 		arrayTimes = mapTimes.get(player);
 		int playtime = arrayTimes.get(0);
 		return playtime;
 	}
 
 	// Format playtime into days, hours, minutes, given playtime in minutes
 	public String formatPlaytime (int time) {
 		// Calculate days, hours, and minutes
 		int timeDays = (int)(time / 60 / 24);
 		time -= timeDays * 24 * 60;
 		int timeHours = (int)(time / 60);
 		time -= timeHours * 60;
 		int timeMinutes = time;
 
 		// Determine output text format, empty strings if 0
 		String days = "";
 		String hours = "";
 		String minutes = "";
 		if (timeDays == 1)
 			days = " day, ";
 		else if (timeDays > 1)
 			days = " days, ";
 		if (timeHours == 1)
 			hours = " hour, ";
 		else if (timeHours > 1)
 			hours = " hours, ";
 		if (timeMinutes == 1)  // Special case for minutes
 			minutes = " minute."; 
 		else
 			minutes = " minutes.";  // Will output N minutes or 0 minutes.
 		return (timeDays > 0 ? timeDays : "") + days + 
 		(timeHours > 0 ? timeHours : "") + hours
 		+ timeMinutes + minutes;
 	}
 
 }
