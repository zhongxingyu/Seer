 package me.omlet.loginmusic;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.plugin.SpoutPlugin;
 import org.getspout.spoutapi.SpoutManager;
 
 public class LoginMusic extends SpoutPlugin {
 	private final LoginMusicSpoutListener spoutListener;
 	private FileConfiguration config;
 
 	public LoginMusic() {
 		this.spoutListener = new LoginMusicSpoutListener(this);
 	}
 
 	//Plugin disabled
 	public void onDisable() {
 		//Let the admin know the plugin is enabled.
 		this.log("v" + this.getVersion() + " (by Omlet) has been disabled!");
 	}
 
 	public void onEnable() {
 		//Is Spout installed?
 		if (!this.getServer().getPluginManager().isPluginEnabled("Spout")) {
 			this.getLogger().severe("Spout not found; plugin disabled!");
 			this.getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 
 		//Check to see if the builtin config exists in data folder, if not create it
 		if (!new File(this.getDataFolder(), "config.yml").exists()) {
 			this.saveDefaultConfig();
 		}
 
 		//Grab the builtin config.
 		this.config = this.getConfig();
 
 		//Pre-Cache the files
 		SpoutManager.getFileManager().addToCache(this, this.getFilesToPreCache());
 
 		//Register event(s)
 		this.registerCustomEvent(spoutListener, Event.Priority.Normal);
 
 		//Let the admin know the plugin is enabled.
 		this.log("v" + this.getVersion() + " (by Omlet) has been enabled!");
 	}
 
 	/**
 	 * This method gets the instance of our configuration handler.
 	 *
 	 * @return config instance
 	 */
 	public FileConfiguration getConfigInstance() {
 		return this.config;
 	}
 
 	/**
 	 * This method gets a listing of all groups specified in the config file.
 	 *
 	 * @return entire list of group names
 	 */
 	public ArrayList<String> getGroups() {
 		ArrayList<String> temp = new ArrayList<String>();
 
 		for (Object key : this.config.getKeys(true)) {
 			if (this.config.isList(key.toString())) {
 				temp.add(key.toString());
 			}
 		}
 		return temp;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("spc")) {
 			if (args.length < 1) {
 				displayHelp(sender);
 				return true;
 			} else if (args[0].equals("reload")) {
 				//Reload the config from disk.
 				this.reloadConfig();
 
 				//Reload the config instance.
 				this.config = this.getConfig();
 
 				//Add the new textures to the cache.
 				SpoutManager.getFileManager().addToCache(this, this.getFilesToPreCache());
 
 				//Finally let the user know the config has been reloaded.
 				sender.sendMessage("[" + ChatColor.AQUA + this.getDescription().getName() + ChatColor.WHITE + "] Configuration reloaded.");
 
 				return true;
 			} else {
 				//show the user help :P
 				displayHelp(sender);
 			}
 		}
 		return false;
 	}
 
 	//TODO Omlet...make a nice help menu for yourself as I copy/paste it from SpoutCreatures.
 	private void displayHelp(CommandSender sender) {
 		if (sender instanceof Player) {
 			sender.sendMessage(ChatColor.GOLD + "-------------------------------");
 			sender.sendMessage(ChatColor.AQUA + "|       " + this.getDescription().getName() + ChatColor.WHITE + " (v" + this.getDescription().getVersion() + ")" + ChatColor.RED + " Help    |");
 			sender.sendMessage(ChatColor.GOLD + "-------------------------------");
 			sender.sendMessage(ChatColor.RED + "/lm" + ChatColor.WHITE + " - Shows the " + ChatColor.AQUA + "LoginMusic " + ChatColor.WHITE + "command help.");
 			sender.sendMessage(ChatColor.RED + "/lm reload" + ChatColor.WHITE + " - Reloads the  " + ChatColor.AQUA + "SpoutCreatures " + ChatColor.WHITE + "config file.");
 			sender.sendMessage("");
 		} else {
 			sender.sendMessage("--------------------------------");
 			sender.sendMessage("| " + this.getDescription().getName() + " (v" + this.getDescription().getVersion() + ") Help |");
 			sender.sendMessage("--------------------------------");
 			sender.sendMessage("spc - Shows the SpoutCreatures command help.");
 			sender.sendMessage("spc reload - Reloads the SpoutCreatures config file.");
 			sender.sendMessage("");
 		}
 	}
 
 	/**
 	 * This method goes through the config file, grabs all lists and compiles it
 	 * into a list of objects for the Pre-Cacher.
 	 *
 	 * @return an entire list of objects within lists found in the config file.
 	 */
 	private ArrayList<String> getFilesToPreCache() {
 		ArrayList<String> allFiles = new ArrayList<String>();
 
 		//Check to see if the entry in keys is a subkey and not a parent. If an entry is, simply ignore it and move on.
 		for (Object key : this.config.getKeys(true)) {
 			Object obj = this.config.get(key.toString());
 			if (obj instanceof List) {
 				List temp = (List) obj;
 				if (temp.isEmpty()) {
 					continue;
 				}
 				allFiles.addAll(fixList(temp));
 			}
 		}
 
 		return allFiles;
 	}
 
 	/**
 	 * This method takes a list and removes all nulls from it
 	 *
 	 * @param target list to fix
 	 * @return newly constructed list that has all nulls removed
 	 */
 	private List fixList(List target) {
 		List temp = new ArrayList();
 		for (Object value : target) {
 			if (value == null) {
 				continue;
 			}
 			temp.add(value);
 		}
 		return temp;
 	}
 }
 
 //Plays music upon player entering
 class LoginMusicSpoutListener extends SpoutListener {
 	private LoginMusic plugin;
 	private SpoutPlayer player;
 
 	public LoginMusicSpoutListener(LoginMusic instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
 		this.player = SpoutManager.getPlayer(event.getPlayer());
 
 		for (String name : this.plugin.getGroups()) {
 			List temp = this.plugin.getConfigInstance().getList(name);
 			int index;
 
 			if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
 				Random rand = new Random();
 				index = rand.nextInt(temp.size());
 
 				if (!this.player.hasPermission("logicmusic." + name.toLowerCase())) {
 					continue;
 				}
 
 				SpoutManager.getSoundManager().playCustomMusic(plugin, player, temp.get(index).toString(), true);
 
 				break;
 			} else {
 				index = 0;
 
 				if (!this.player.hasPermission("logicmusic." + name.toLowerCase())) {
 					continue;
 				}
 
 				SpoutManager.getSoundManager().playCustomMusic(plugin, player, temp.get(index).toString(), true);
 
 				break;
 			}
 		}
 	}
 }
