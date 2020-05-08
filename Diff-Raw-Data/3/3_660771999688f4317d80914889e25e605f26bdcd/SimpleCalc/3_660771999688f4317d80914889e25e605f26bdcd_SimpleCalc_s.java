 /***************************************************************************
  * Copyright (C) 2011 Philippe Leipold
  *
  * SimpleCalc is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleCalc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleCalc. If not, see <http://www.gnu.org/licenses/>.
  *
  ***************************************************************************/
 
 package de.Lathanael.SimpleCalc;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import de.Lathanael.SimpleCalc.Exceptions.MathSyntaxMismatch;
 import de.Lathanael.SimpleCalc.Listeners.SCInputListener;
 import de.Lathanael.SimpleCalc.Listeners.SCPluginListener;
 import de.Lathanael.SimpleCalc.Listeners.SCPlayerListener;
 import de.Lathanael.SimpleCalc.Listeners.SCSpoutScreenListener;
 import de.Lathanael.SimpleCalc.Parser.MathExpParser;
 import de.Lathanael.SimpleCalc.Tools.Functions;
 import de.Lathanael.SimpleCalc.Tools.VariableKeys;
 import de.Lathanael.SimpleCalc.gui.CalcWindow;
 
 /**
 * @author Lathanael (aka Philippe Leipold)
 * https://github.com/Lathanael
 **/
 public class SimpleCalc extends JavaPlugin {
 
 	public static Logger log;
 	public static PluginManager pm;
 	private static SimpleCalc instance;
 	private static Map<SpoutPlayer, CalcWindow> popups = new HashMap<SpoutPlayer, CalcWindow>();
 	public static Map<String, Double> answer = new HashMap<String, Double>();
 	public static Map<VariableKeys, Double> variables = new HashMap<VariableKeys, Double>();
 	public static List<String> alphabet = new ArrayList<String>();
 	public static List<String> locs = new ArrayList<String>();
 	private static SCPluginListener SCPluginListener = new SCPluginListener();
 	private static SCPlayerListener SCPlayerListener;
 	private static DecimalFormat format = new DecimalFormat("#0.00");
 	private YamlConfiguration config;
 	public static String backgroundURL;
 	public static boolean keysEnabled = false;
 
 	public void onDisable() {
 		log.info("Version " + this.getDescription().getVersion() + " disabled.");
 	}
 
 	public void onEnable() {
 		instance = this;
 		log = getLogger();
 		createLists();
 		SCPlayerListener = new SCPlayerListener(this);
 		pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(SCPlayerListener, this);
 		pm.registerEvents(SCPluginListener, this);
 		loadConfigurationFile();
 		loadConfig(config);
 		SCPluginListener.spoutHook(pm);
 		if (SCPluginListener.spout != null) {
 			pm.registerEvents(new SCSpoutScreenListener(this), this);
 		}
 		if (keysEnabled) {
 			log.info("Listening to keystrokes while CalcWindow is open enabled");
 			pm.registerEvents(new SCInputListener(), this);
 		}
 		log.info("Version " + this.getDescription().getVersion() + " enabled.");
 	}
 
 	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
 
 		// args was initialised as null
 		if (args == null)
 			return false;
 		// No arguments given open clac window if Spout is enabled
 		if (args.length == 0) {
 			if (sender instanceof ConsoleCommandSender)
 				return false;
 			if (SCPluginListener.spout != null) {
 				openWindow((SpoutPlayer) sender);
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else if (args.length >=1) {
 			// Lets set a variable!
 			if (args.length >= 3 && args[0].equalsIgnoreCase("set")) {
 				String var = args[1];
 				if (var.equals("e")) {
 					if (sender instanceof ConsoleCommandSender)
 						sender.sendMessage("You cant set e as a varialbe as it is used for the Euler constant!");
 					else
 						sender.sendMessage(ChatColor.RED + "You cant set e as a varialbe as it is used for the Euler constant!");
 					return true;
 				}
 				String name;
 				if (sender instanceof ConsoleCommandSender) {
 					name = "Admin";
 				} else {
 					name = ((Player) sender).getName();
 				}
 				VariableKeys key = new VariableKeys(name, var);
 				double value;
 				try {
 					value = Double.parseDouble(args[2]);
 				} catch (NumberFormatException e) {
 					if (sender instanceof ConsoleCommandSender)
 						log.info("Could not parse your input as a number!");
 					else
 						sender.sendMessage(ChatColor.RED + "Could not parse your input as a number!");
 					return false;
 				}
 				variables.put(key, value);
 				if (sender instanceof ConsoleCommandSender)
 					log.info("Successfully stored: " + value + " into variable: " + var);
 				else
 					sender.sendMessage(ChatColor.GREEN + "Successfully stored: " + ChatColor.GOLD + value
 							+ ChatColor.GREEN + " into variable: " + ChatColor.GOLD + var);
 				return true;
 			}
 			// Cocatenate the String Array if user did place whitespaces in it and remove them if needed.
 			String calc = Functions.arrayConcat(args);
 			// Create a new parser object and let itparse the input String
 			try {
 				MathExpParser equation = null;
 				double result = 0;
 				if (sender instanceof ConsoleCommandSender) {
 					equation = new MathExpParser(calc, "Admin");
 					result = equation.compute();
 					answer.put("Admin", result);
 				}
 				else {
 					equation = new MathExpParser(calc, ((Player) sender).getName());
 					result = equation.compute();
 					answer.put(((Player) sender).getName(), result);
 				}
 
 				if (sender instanceof ConsoleCommandSender){
 					log.info("The result of your expression is: " + format.format(result));
 				}
 				else {
 					sender.sendMessage(ChatColor.GREEN + "The result of your expression is: " + format.format(result));
 				}
 			}
 			// The equation given is incorrect!
 			catch(MathSyntaxMismatch mismatch){
 				if (sender instanceof ConsoleCommandSender){
 					log.info(" " + mismatch.getMessage());
 				}
 				else {
 					sender.sendMessage(ChatColor.RED + mismatch.getMessage());
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static SimpleCalc getInstance() {
 		return instance;
 	}
 
 	public void openWindow(SpoutPlayer player) {
 		CalcWindow popup = null;
 		if (!popups.containsKey(player)) {
 			popups.put(player, new CalcWindow(player, getInstance()));
 		}
 		popup = popups.get(player);
 		popup.open();
 	}
 
 	public void removePopup(SpoutPlayer player) {
 		popups.remove(player);
 	}
 
 	public void closeWindow(SpoutPlayer player, boolean remove) {
 		if (!popups.containsKey(player)) {
 			log.info("No window for " + player.getName() + " was found!");
 			return;
 		}
 		player.getMainScreen().closePopup();
 		if (remove)
 			removePopup(player);
 	}
 
 	/**
 	 * @author Balor (aka Antoine Aflalo)
 	 * @author Lathanael (aka Philippe Leipold)
 	 */
 	private void loadConfigurationFile() {
 		File folder = new File(getDataFolder().getPath());
 		File file = new File(getDataFolder().getPath() + File.separator + "config.yml");
 		if (!folder.exists()) {
 			folder.mkdirs();
 		}
 		if (!file.exists()) {
 			try {
 				file.createNewFile();
 				InputStream in = getResource("config.yml");
 				FileWriter writer = new FileWriter(file);
 				for (int i = 0; (i = in.read()) > 0;) {
 					writer.write(i);
 				}
 				writer.flush();
 				writer.close();
 				in.close();
 				config = YamlConfiguration.loadConfiguration(file);
 				config.save(file);
 			} catch (IOException e) {
 				log.info("Failed to create config.yml!");
 				e.printStackTrace();
 			}
 		} else {
 			config = YamlConfiguration.loadConfiguration(file);
 		}
 	}
 
 	private void loadConfig(YamlConfiguration config) {
 		backgroundURL = config.getString("backgroundURL", "http://dl.dropbox.com/u/42731731/CalcBackground.png");
 		keysEnabled = config.getBoolean("EnableKeys", false);
 	}
 
 	public void createLists() {
 		for (int i = 0; i < 26; i++) {
 			String upper = String.valueOf((char) ('A' + i));
 			String lower = String.valueOf((char) ('a' + i));
 			alphabet.add(upper);
 			if (lower.charAt(0) == 'e')
 				continue;
 			alphabet.add(lower);
 		}
 		locs.add("LocX()");
 		locs.add("LocY()");
 		locs.add("LocZ()");
 		locs.add("SpawnX()");
 		locs.add("SpawnY()");
 		locs.add("SpawnZ()");
 	}
 }
