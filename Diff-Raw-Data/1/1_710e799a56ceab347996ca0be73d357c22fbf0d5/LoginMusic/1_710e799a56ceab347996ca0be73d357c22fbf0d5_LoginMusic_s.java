 package me.omlet.loginmusic;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
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
 		SpoutManager.getFileManager().addToPreLoginCache(this, this.getFilesToPreCache());
 
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
 		if (cmd.getName().equalsIgnoreCase("lm")) {
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
 				sender.sendMessage("[" + ChatColor.AQUA + this.getDescription().getName() + ChatColor.WHITE + "] Configuration reloaded!");
 
 				return true;
 			} else {
 				//show the user help :P
 				displayHelp(sender);
 			}
 		}
 		return false;
 	}
 
 	//Displays the help menu
 	private void displayHelp(CommandSender sender) {
 		if (sender instanceof Player) {
 			sender.sendMessage(ChatColor.GOLD + "-------------------------------");
 			sender.sendMessage(ChatColor.AQUA + "|       " + this.getDescription().getName() + ChatColor.WHITE + " (v" + this.getDescription().getVersion() + ")" + ChatColor.RED + " Help    |");
 			sender.sendMessage(ChatColor.GOLD + "-------------------------------");
 			sender.sendMessage(ChatColor.RED + "/lm" + ChatColor.WHITE + " - Shows the " + ChatColor.AQUA + "LoginMusic " + ChatColor.WHITE + "command help.");
 			sender.sendMessage(ChatColor.RED + "/lm reload" + ChatColor.WHITE + " - Reloads the " + ChatColor.AQUA + "LoginMusic " + ChatColor.WHITE + "config file.");
 			sender.sendMessage("");
 		} else {
 			sender.sendMessage("--------------------------------");
 			sender.sendMessage(" | " + this.getDescription().getName() + " (v" + this.getDescription().getVersion() + ") Help |");
 			sender.sendMessage("--------------------------------");
 			sender.sendMessage("lm reload - Reloads the LoginMusic config file.");
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
 	public List fixList(List target) {
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
 
 	//Random number generator
 	Random rand = new Random();
 
 	public LoginMusicSpoutListener(LoginMusic instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
 		//Grab the player associated with the event
 		this.player = SpoutManager.getPlayer(event.getPlayer());
 
 		//Lets play a global sound shall we? This is for everyone else on the server
 		if (this.plugin.getConfigInstance().getBoolean("general.play-global-music")) {
 			List temp = this.plugin.fixList(this.plugin.getConfigInstance().getList("global"));
 
 			//No reason to not try to play a sound that doesn't exist.
 			if (temp.isEmpty()) {
 				return;
 			}
 
 			//Grab a random number to choose the random skin (global).
 			int index = rand.nextInt(temp.size());
 
 			//Cycle through all the online players, check permissions, check config, play sounds based on results.
 			for (SpoutPlayer p : this.plugin.getSpoutServer().getOnlinePlayers()) {
 				if (!p.hasPermission("loginmusic.global")) {
 					this.playSoundPerPerson(p, temp, -1);
 				}
 
 				if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
 					this.playSoundPerPerson(player, temp, index);
 				} else {
 					this.playSoundPerPerson(player, temp, 0);
 				}
 			}
 		}
 
 		//Pass off a dummy list and the flag. This is a trick to just get the fallback working
 		//TODO Its hacky...lets try and do this better someday (eventhough it works fine...just not OOP).
 		this.playSoundPerPerson(player, Collections.emptyList(), -1);
 	}
 
 	private void playSoundPerPerson(SpoutPlayer s, List list, int flag) {
 		//This essentially means that this was a fall back to the if statements above and we should calculate group-based music playing
 		if (flag == -1) {
 			for (String name : this.plugin.getGroups()) {
 				//Continue if the player has no perms for the group in question.
 				if (!this.player.hasPermission("loginmusic." + name.toLowerCase())) {
 					continue;
 				}
 
 				//Make sure the list has no nulls
 				List temp = this.plugin.fixList(this.plugin.getConfigInstance().getList("groups." + name));
 
 				//No reason to continue if we have no links.
 				if (temp.isEmpty()) {
 					continue;
 				}
 
 				//Check to see if we should pull a random piece of music.
 				if (this.plugin.getConfigInstance().getBoolean("general.play-random-music")) {
 					int num = rand.nextInt(temp.size());
 					SpoutManager.getSoundManager().playCustomMusic(plugin, s, temp.get(num).toString(), false);
 				} else {
 					SpoutManager.getSoundManager().playCustomMusic(plugin, s, temp.get(0).toString(), false);
 				}
 			}
 		} else {
 			//Play sound for the person based on a link grabbed from the global list.
 			SpoutManager.getSoundManager().playCustomMusic(plugin, s, list.get(flag).toString(), false);
 		}
 	}
 }
