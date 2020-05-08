 package me.thelunarfrog.frogannounce;
 
 import me.thelunarfrog.frogannounce.eventhandlers.PlayerJoinListener;
 import me.thelunarfrog.frogannounce.events.AnnouncementEvent;
 import me.thelunarfrog.frogannounce.listeners.AnnouncementListener;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import javax.swing.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * The FrogAnnounce core. Handles loops, grabbing configuration values from
  * ConfigurationManager, commands, and all announcements. API, such as
  * AnnouncementListener registration, will be found here, too.
  *
  * @author Dan | TheLunarFrog
  * @version 2.3.0.0
  */
 public class FrogAnnounce extends JavaPlugin{
 	private Permission permission;
 	private String tag;
 	private String joinMessage;
 	private int interval;
 	private int taskId;
 	private int counter;
 	private boolean running;
 	private boolean random;
 	private boolean usingPerms;
 	private boolean showJoinMessage;
 	private boolean showConsoleAnnouncements;
 	private List<Announcement> announcements;
 	private ArrayList<String> ignoredPlayers;
 	private ConfigurationHandler cfg;
 	private ArrayList<AnnouncementListener> asyncListeners;
 	private ArrayList<AnnouncementListener> syncListeners;
 	private Random r;
 	/**
 	 * Static accessor
 	 */
 	private static FrogAnnounce p;
 
 	public static String colourizeText(String announce){
 		announce = announce.replaceAll("&AQUA;", ChatColor.AQUA.toString());
 		announce = announce.replaceAll("&BLACK;", ChatColor.BLACK.toString());
 		announce = announce.replaceAll("&BLUE;", ChatColor.BLUE.toString());
 		announce = announce.replaceAll("&DARK_AQUA;", ChatColor.DARK_AQUA.toString());
 		announce = announce.replaceAll("&DARK_BLUE;", ChatColor.DARK_BLUE.toString());
 		announce = announce.replaceAll("&DARK_GRAY;", ChatColor.DARK_GRAY.toString());
 		announce = announce.replaceAll("&DARK_GREEN;", ChatColor.DARK_GREEN.toString());
 		announce = announce.replaceAll("&DARK_PURPLE;", ChatColor.DARK_PURPLE.toString());
 		announce = announce.replaceAll("&RED;", ChatColor.RED.toString());
 		announce = announce.replaceAll("&DARK_RED;", ChatColor.DARK_RED.toString());
 		announce = announce.replaceAll("&GOLD;", ChatColor.GOLD.toString());
 		announce = announce.replaceAll("&GRAY;", ChatColor.GRAY.toString());
 		announce = announce.replaceAll("&GREEN;", ChatColor.GREEN.toString());
 		announce = announce.replaceAll("&LIGHT_PURPLE;", ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&PURPLE;", ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&PINK;", ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&WHITE;", ChatColor.WHITE.toString());
 		announce = announce.replaceAll("&b;", ChatColor.AQUA.toString());
 		announce = announce.replaceAll("&0;", ChatColor.BLACK.toString());
 		announce = announce.replaceAll("&9;", ChatColor.BLUE.toString());
 		announce = announce.replaceAll("&3;", ChatColor.DARK_AQUA.toString());
 		announce = announce.replaceAll("&1;", ChatColor.DARK_BLUE.toString());
 		announce = announce.replaceAll("&8;", ChatColor.DARK_GRAY.toString());
 		announce = announce.replaceAll("&2;", ChatColor.DARK_GREEN.toString());
 		announce = announce.replaceAll("&5;", ChatColor.DARK_PURPLE.toString());
 		announce = announce.replaceAll("&4;", ChatColor.DARK_RED.toString());
 		announce = announce.replaceAll("&6;", ChatColor.GOLD.toString());
 		announce = announce.replaceAll("&7;", ChatColor.GRAY.toString());
 		announce = announce.replaceAll("&a;", ChatColor.GREEN.toString());
 		announce = announce.replaceAll("&d;", ChatColor.LIGHT_PURPLE.toString());
 		announce = announce.replaceAll("&c;", ChatColor.RED.toString());
 		announce = announce.replaceAll("&f;", ChatColor.WHITE.toString());
 		announce = announce.replaceAll("&e;", ChatColor.YELLOW.toString());
 		announce = announce.replaceAll("&k;", ChatColor.MAGIC.toString());
 		announce = announce.replaceAll("&MAGIC;", ChatColor.MAGIC.toString());
 		announce = announce.replaceAll("&BOLD;", ChatColor.BOLD.toString());
 		announce = announce.replaceAll("&ITALIC;", ChatColor.ITALIC.toString());
 		announce = announce.replaceAll("&STRIKE;", ChatColor.STRIKETHROUGH.toString());
 		announce = announce.replaceAll("&UNDERLINE;", ChatColor.UNDERLINE.toString());
 		announce = announce.replaceAll("&RESET;", ChatColor.RESET.toString());
 		return announce;
 	}
 
 	/**
 	 * Gets the current instance of FrogAnnounce.
 	 *
 	 * @return The running instance of this plugin.
 	 */
 	public static FrogAnnounce getInstance(){
 		return FrogAnnounce.p;
 	}
 
 	public static void main(final String[] args){
 		try{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}catch(final ClassNotFoundException e){
 			e.printStackTrace();
 		}catch(final InstantiationException e){
 			e.printStackTrace();
 		}catch(final IllegalAccessException e){
 			e.printStackTrace();
 		}catch(final UnsupportedLookAndFeelException e){
 			e.printStackTrace();
 		}finally{
 			JOptionPane.showMessageDialog(null, "Sorry, but FrogAnnounce is a Bukkit plugin, and cannot be run directly like you've attempted.\nTo use the plugin, download and set up a Bukkit Minecraft server, and in the root directory, create a folder called\n\"plugins\" (no quotes, and assuming it hasn't already been created for you), and put this JAR file (FrogAnnounce.jar) there.\nWhen you've done that, start the Bukkit server using the command line java -jar \"path to Bukkit.jar\",\nor if it's already running, type \"reload\" (no quotes) into the command-line.", "FrogAnnounce", JOptionPane.ERROR_MESSAGE);
 		}
 		System.exit(0);
 	}
 
 	/**
 	 * Announces one of the announcements from FrogAnnounce's configuration.
 	 * Overload of a private method.
 	 *
 	 * @param index - The index of the announcement to announce.
 	 */
 	public void announce(final int index){
 		this.announce(index, false);
 	}
 
 	private void announce(int index, final boolean auto){
 		if(auto){
 			if(this.random){
 				this.announcements.get(index = this.r.nextInt(this.announcements.size())).execute();
 			}else{
 				this.announcements.get(index = this.counter++).execute();
 				if(this.counter >= this.announcements.size()){
 					this.counter = 0;
 				}
 			}
 		}else{
 			this.announcements.get(index).execute();
 		}
 		this.notifySyncAnnouncementListeners(this.announcements.get(index), auto, index);
 		this.notifyAsyncAnnouncementListeners(this.announcements.get(index), auto, index);
 	}
 
 	/**
 	 * This method broadcasts a message to the players applicable to such an
 	 * announcement. Only announces the announcements in FrogAnnounce's
 	 * configuration. If you have the index of the announcement you want to
 	 * force, use <b>FrogAnnounce.announce(int, boolean)</b> instead.
 	 *
 	 * @param s      - The index of the announcement, as a string.
 	 * @param player - The CommandSender to display the result to.
 	 */
 	public void broadcastMessage(final String s, final CommandSender player){
 		int i;
 		try{
 			i = Integer.parseInt(s);
 			if(i > (this.announcements.size() - 1)){
 				this.sendMessage(player, Severity.WARNING, "You specified a number that does not correspond to any of the announcements in the file. Remember: it starts at 0! Operation aborted.");
 			}else{
 				this.announce(i, false);
 			}
 		}catch(final NumberFormatException e){
 			this.sendMessage(player, Severity.WARNING, "Only numbers can be entered as an index. Remember to start counting at 0.");
 		}
 	}
 
 	private void checkPermissionsVaultPlugins(){
 		final Plugin vault = this.getServer().getPluginManager().getPlugin("Vault");
 		if(vault != null){
 			if(this.setupPermissions() != null){
 				this.sendConsoleMessage(Severity.INFO, "Vault hooked successfully.");
 				this.usingPerms = true;
 			}else if(this.setupPermissions() == null){
 				this.sendConsoleMessage(Severity.INFO, "Vault wasn't found. Defaulting to OP/Non-OP system.");
 				this.usingPerms = false;
 			}
 		}else{
 			this.sendConsoleMessage(Severity.WARNING, "Vault is not in your plugins directory! This plugin has a soft dependency of Vault, so if you don't have it, this will still work (you just can't use permission-based stuff).");
 		}
 	}
 
 	/**
 	 * Gets the array of Strings that the announcer is announcing to players.
 	 *
 	 * @return The messages that players will see, in form of an array.
 	 */
 	public String[] getAnnouncements(){
 		final String[] announcements = new String[this.announcements.size()];
 		for(int i = 0; i < this.announcements.size(); i++){
 			for(final String s : this.announcements.get(i).getText()){
 				announcements[i] = s;
 			}
 		}
 		return announcements;
 	}
 
 	private ArrayList<AnnouncementListener> getAsyncAnnouncementListeners(){
 		return this.asyncListeners;
 	}
 
 	/**
 	 * Gets an arraylist of player names of the players who are not receiving
 	 * announcements.
 	 *
 	 * @return The names of players who aren't getting announcements, as a
 	 *         String arraylist.
 	 */
 	public ArrayList<String> getIgnoredPlayers(){
 		return this.ignoredPlayers;
 	}
 
 	/**
 	 * Gets the return message for this instance of FrogAnnounce. Will return
 	 * null if the plugin isn't even showing the join message.
 	 *
 	 * @return The join message, or null, if this instance isn't showing it.
 	 */
 	public String getJoinMessage(){
 		return this.isShowingJoinMessage() ? this.joinMessage : null;
 	}
 
 	private ArrayList<AnnouncementListener> getSyncAnnouncementListeners(){
 		return this.syncListeners;
 	}
 
 	/**
 	 * Gets the tag for this instance of FrogAnnounce.
 	 *
 	 * @return The tag which appears in front of every announcement.
 	 */
 	public String getTag(){
 		return this.tag;
 	}
 
 	/**
 	 * Makes FrogAnnounce ignore the specified player when announcing. Overload
 	 * of <b>ignorePlayer(CommandSender, String)</b>.
 	 *
 	 * @param player - The CommandSender to ignore.
 	 */
 	public void ignorePlayer(final CommandSender player){
 		this.ignorePlayer(player, player.getName());
 	}
 
 	/**
 	 * Makes FrogAnnounce not announce to a certain player.
 	 *
 	 * @param player - The player to relay output, as if they had done this to
 	 *               their target.
 	 * @param other  - The target player for FrogAnnounce to no longer announce to.
 	 */
 	public void ignorePlayer(final CommandSender player, final String other){
 		Player otherPlayer;
 		if(other.equals(player.getName())){
 			otherPlayer = (Player)player;
 		}else{
 			otherPlayer = this.getServer().getPlayer(other);
 		}
 		if((otherPlayer != null) && (otherPlayer == player)){
 			if(this.permit(player, "frogannounce.ignore")){
 				if(!this.ignoredPlayers.contains(player.getName())){
 					this.ignoredPlayers.add(otherPlayer.getName());
 					try{
 						this.cfg.updateConfiguration("ignoredPlayers", this.ignoredPlayers);
 						this.sendMessage(otherPlayer, Severity.INFO, ChatColor.GRAY + "You are now being ignored by FrogAnnounce. You will no longer receive announcements from it until you opt back in.");
 					}catch(Exception e){
 						this.sendMessage(otherPlayer, Severity.SEVERE, e.getMessage());
 					}
 				}else{
 					this.sendMessage(player, Severity.WARNING, "That player is already being ignored.");
 				}
 			}else{
 				this.sendMessage(player, Severity.WARNING, "You don't have sufficient permission to opt another player out of FrogAnnounce's announcements. Sorry!");
 			}
 		}else if((otherPlayer != null)){
 			if(this.permit(player, "frogannounce.ignore.other")){
 				if(!this.ignoredPlayers.contains(otherPlayer.getName())){
 					this.ignoredPlayers.add(otherPlayer.getName());
 					try{
 						this.cfg.updateConfiguration("ignoredPlayers", this.ignoredPlayers);
 						this.sendMessage(player, Severity.INFO, "Success! The player has been added to FrogAnnounce's ignore list and will no longer see its announcements until he/she opts back in.");
 						this.sendMessage(otherPlayer, Severity.INFO, ChatColor.GRAY + "You are now being ignored by FrogAnnounce. You will no longer receive announcements from it until you opt back in.");
 					}catch(Exception e){
 						this.sendMessage(player, Severity.SEVERE, e.getMessage());
 					}
 				}else{
 					this.sendMessage(player, Severity.WARNING, "You're already being ignored by FrogAnnounce.");
 				}
 			}else{
 				this.sendMessage(player, Severity.WARNING, "You don't have sufficient permission to opt another player out of FrogAnnounce's announcements. Sorry!");
 			}
 		}else{
 			this.sendMessage(player, Severity.WARNING, "That player isn't online right now.");
 		}
 	}
 
 	/**
 	 * Gets whether or not the plugin is announcing in a random order.
 	 *
 	 * @return Whether or not the plugin is announcing randomly.
 	 */
 	public boolean isRandom(){
 		return this.random;
 	}
 
 	/**
 	 * Gets whether or not the plugin's announcer module is running.
 	 *
 	 * @return Whether or not the plugin is announcing.
 	 */
 	public boolean isRunning(){
 		return this.running;
 	}
 
 	/**
 	 * Gets whether or not the plugin is displaying the joinMessage to players
 	 * when they join the server.
 	 *
 	 * @return The setting of showMessageOnJoin in the configuration.
 	 */
 	public boolean isShowingJoinMessage(){
 		return this.showJoinMessage;
 	}
 
 	/**
 	 * Whether FrogAnnounce is using OP/Non-OP or permissions.
 	 *
 	 * @return True if using permissions, false if non-op/op
 	 */
 	public boolean isUsingPermissions(){
 		return this.usingPerms;
 	}
 
 	private void notifyAsyncAnnouncementListeners(final Announcement announcement, final boolean automatic, final int index){
 		Bukkit.getPlayer("TheLunarFrog");
 		final AnnouncementEvent evt = new AnnouncementEvent(announcement, automatic, index);
 		for(final AnnouncementListener listener : FrogAnnounce.this.getAsyncAnnouncementListeners()){
 			if(listener != null){
 				new Thread(new Runnable(){
 					@Override
 					public void run(){
 						listener.onAnnounceEvent(evt);
 					}
 				}).start();
 			}
 		}
 	}
 
 	private void notifySyncAnnouncementListeners(final Announcement announcement, final boolean automatic, final int index){
 		Bukkit.getPlayer("TheLunarFrog");
 		final AnnouncementEvent evt = new AnnouncementEvent(announcement, automatic, index);
 		for(final AnnouncementListener listener : FrogAnnounce.this.getSyncAnnouncementListeners()){
 			if(listener != null){
 				listener.onAnnounceEvent(evt);
 			}
 		}
 	}
 
 	@Override
 	public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args){
 		if(commandLabel.equalsIgnoreCase("fa") || commandLabel.equalsIgnoreCase("frogannounce")){
 			if(this.permit(sender, "frogannounce.admin") || this.permit(sender, "frogannounce.*")){
 				try{
 					if(args.length == 0){
 						this.sendMessage(sender, Severity.INFO, "FrogAnnounce version: " + super.getDescription().getVersion());
 						this.sendMessage(sender, Severity.INFO, "For help, use /fa help.");
 					}else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
 						if(args.length == 2){
 							this.returnHelp(sender, args[1]);
 						}else{
 							this.returnHelp(sender, "0");
 						}
 					}else if(args[0].equalsIgnoreCase("on")){
 						this.turnOn(sender);
 					}else if(args[0].equalsIgnoreCase("off")){
 						this.turnOff(sender);
 					}else if(args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")){
 						this.sendMessage(sender, Severity.INFO, "Current version: " + super.getDescription().getVersion());
 					}else if(args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("optout") || args[0].equalsIgnoreCase("opt-out")){
 						if(args.length == 2){
 							this.ignorePlayer(sender, args[1]);
 						}else{
 							this.ignorePlayer(sender, sender.getName());
 						}
 					}else if(args[0].equalsIgnoreCase("unignore") || args[0].equalsIgnoreCase("optin") || args[0].equalsIgnoreCase("opt-in")){
 						if(args.length == 2){
 							this.unignorePlayer(sender, args[1]);
 						}else{
 							this.unignorePlayer(sender, sender.getName());
 						}
 					}else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("int")){
 						this.setInterval(args, sender);
 					}else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("rand")){
 						this.setRandom(args, sender);
 					}else if(args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("bc")){
 						this.broadcastMessage(args[1], sender);
 					}else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload")){
 						this.reloadPlugin(sender);
 						this.reloadConfig();
 					}else if(args[0].equalsIgnoreCase("list")){
 						this.sendMessage(sender, Severity.INFO, "Loaded announcements:");
 						for(final Announcement a : this.announcements){
 							final StringBuilder ann = new StringBuilder(this.announcements.indexOf(a) + ". ");
 							for(int i = 0; i < a.getText().length; i++){
 								ann.append(a.getText()[i]);
 							}
 							this.sendMessage(sender, Severity.INFO, ann.toString());
 						}
 					}else if(args[0].equalsIgnoreCase("add")){
 						final StringBuilder sb = new StringBuilder();
 						for(int i = 1; i < args.length; i++){
 							sb.append(args[i] + " ");
 						}
 						this.announcements.add(new Announcement(sb.toString().trim(), null, null, null));
 						try{
 							this.cfg.updateConfiguration("Announcer.Strings", this.announcements);
 							this.sendMessage(sender, Severity.INFO, "Successfully added the announcement \"" + sb.toString().trim() + "\" to the configuration. Reloading config...");
 							this.reloadPlugin(sender);
 						}catch(Exception e){
 							this.sendMessage(sender, Severity.SEVERE, e.getMessage());
 						}
 					}else if(args[0].equalsIgnoreCase("manualbroadcast") || args[0].equalsIgnoreCase("mbc")){
 						final StringBuilder sb = new StringBuilder();
 						for(int i = 1; i < args.length; i++){
 							sb.append(args[i] + " ");
 						}
 						if(this.tag.isEmpty()){
 							this.getServer().broadcastMessage(FrogAnnounce.colourizeText(sb.toString().trim()));
 						}else{
 							this.getServer().broadcastMessage(this.tag + " " + FrogAnnounce.colourizeText(sb.toString().trim()));
 						}
 					}else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("del")){
 						int i;
 						if(args.length == 2){
 							try{
 								i = Integer.parseInt(args[1]);
 								try{
 									this.sendMessage(sender, Severity.INFO, "Removing announcement " + i + " (" + this.announcements.get(i) + ")...");
 									this.announcements.remove(i);
 									try{
 										this.cfg.updateConfiguration("Announcer.Strings", this.announcements);
 										this.sendMessage(sender, Severity.INFO, "Announcement " + i + " successfully removed. Reloading configuration...");
 										this.reloadPlugin(sender);
 									}catch(Exception e){
 										this.sendMessage(sender, Severity.SEVERE, e.getMessage());
 									}
 								}catch(final IndexOutOfBoundsException e){
 									this.sendMessage(sender, Severity.WARNING, "Error: There are only " + this.announcements.size() + " announcements. You must count from 0!");
 								}
 							}catch(final NumberFormatException e){
 								this.sendMessage(sender, Severity.WARNING, "Please enter an announcement index.");
 							}
 						}else{
 							this.sendMessage(sender, Severity.WARNING, "You must specify an index to remove.");
 						}
 					}else{
 						this.sendMessage(sender, Severity.WARNING, "That didn't seem like a valid command. Here's some help...");
 						if(args.length == 2){
 							this.returnHelp(sender, args[1]);
 						}else{
 							this.returnHelp(sender, "0");
 						}
 					}
 				}catch(final ArrayIndexOutOfBoundsException e){
 					return false;
 				}
 				return true;
 			}else if(args.length >= 1){
 				if(args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("optout") || args[0].equalsIgnoreCase("opt-out")){
 					if(args.length == 2){
 						if(this.permit(sender, "frogannounce.optout.other")){
 							this.ignorePlayer(sender, args[1]);
 						}else{
 							this.sendMessage(sender, Severity.WARNING, "You don't have permission to access that command.");
 						}
 					}else if(this.permit(sender, "frogannounce.optout")){
 						this.ignorePlayer(sender, sender.getName());
 					}else{
 						this.sendMessage(sender, Severity.WARNING, "You don't have permission to access that command.");
 					}
 					return true;
 				}else if(args[0].equalsIgnoreCase("unignore") || args[0].equalsIgnoreCase("optin") || args[0].equalsIgnoreCase("opt-in")){
 					if(args.length == 2){
 						if(this.permit(sender, "frogannounce.optin.other")){
 							this.unignorePlayer(sender, args[1]);
 						}else{
 							this.sendMessage(sender, Severity.WARNING, "You don't have permission to access that command.");
 						}
 					}else if(this.permit(sender, "frogannounce.optin")){
 						this.unignorePlayer(sender, sender.getName());
 					}else{
 						this.sendMessage(sender, Severity.WARNING, "You don't have permission to access that command.");
 					}
 					return true;
 				}
 			}else{
 				this.sendMessage(sender, Severity.WARNING, ChatColor.RED + "Sorry, but you don't have access to that command.");
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onDisable(){
 		this.turnOff(null);
 		this.unregisterAllAnnouncementListeners();
 		this.sendConsoleMessage(Severity.INFO, "Version " + super.getDescription().getVersion() + " has been disabled.");
 	}
 
 	@Override
 	public void onEnable(){
 		FrogAnnounce.p = this;
 		this.cfg = new ConfigurationHandler();
 		this.updateConfigurationFile();
 		this.updateConfiguration();
 		this.asyncListeners = new ArrayList<AnnouncementListener>();
 		this.syncListeners = new ArrayList<AnnouncementListener>();
 		if(this.usingPerms){
 			this.checkPermissionsVaultPlugins();
 		}
 		if(this.showJoinMessage){
 			super.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
 		}
 		this.sendConsoleMessage(Severity.INFO, "Settings loaded " + this.announcements.size() + " announcements!");
 		this.r = new Random();
 		this.turnOn(null);
 		this.sendConsoleMessage(Severity.INFO, "Version " + super.getDescription().getVersion() + " by TheLunarFrog has been enabled!");
 	}
 
 	private boolean permit(final CommandSender sender, final String perm){
 		return sender.hasPermission(perm);
 	}
 
 	/**
 	 * This method gets the Vault Permission module. It is package-private intentionally.
 	 *
 	 * @return The permission module of Vault, if present. Otherwise, null.
 	 */
 	Permission getVaultPerms(){
 		return this.permission;
 	}
 
 	public boolean isShowingConsoleAnnouncements(){
 		return this.showConsoleAnnouncements;
 	}
 
 	/**
 	 * This method allows you to register an announcement listener to be
 	 * notified by FrogAnnounce when an announcement ticks. Listeners must be
 	 * classes which implement the superinterface <b>AnnouncementListener</b>
 	 * and override the <b>onAnnounceEvent(AnnouncementEvent)</b> method from
 	 * this superinterface. <br/>
 	 * <br/>
 	 * This method will register the listener asynchronously, meaning any
 	 * thread-unsafe calls should <b>NOT</b> be used within the listener being
 	 * registered. For thread-unsafe calls, use registerSyncAnnouncementListener
 	 * instead.
 	 *
 	 * @param listener The listener, a class which implements my
 	 *                 <b>AnnouncementListener</b> interface as a superinterface, and
 	 *                 implements and overrides the necessary parent methods from
 	 *                 such superinterface.
 	 *
 	 * @return The ID of the listener that you registered. You should keep this
 	 *         ID, as it is used by the
 	 *         <b>unregisterAnnouncementListener(int)</b> method, which
 	 *         unregisters your listener.
 	 *
 	 * @see #registerAsyncAnnouncementListener(AnnouncementListener)
 	 */
 	public int registerAsyncAnnouncementListener(final AnnouncementListener listener){
 		if(!this.getAsyncAnnouncementListeners().contains(listener)){
 			this.asyncListeners.add(listener);
 		}
 		return this.getAsyncAnnouncementListeners().indexOf(listener);
 	}
 
 	/**
 	 * This method allows you to register an announcement listener to be
 	 * notified by FrogAnnounce when an announcement ticks. Listeners must be
 	 * classes which implement the superinterface <b>AnnouncementListener</b>
 	 * and override the <b>onAnnounceEvent(AnnouncementEvent)</b> method from
 	 * this superinterface. <br/>
 	 * <br/>
 	 * This method will register the listener synchronously, meaning any
 	 * thread-unsafe calls can be used in the listener, as everything will be
 	 * executed from the main thread.
 	 *
 	 * @param listener The listener, a class which implements my
 	 *                 <b>AnnouncementListener</b> interface as a superinterface, and
 	 *                 implements and overrides the necessary parent methods from
 	 *                 such superinterface.
 	 *
 	 * @return The ID of the listener that you registered. You should keep this
 	 *         ID, as it is used by the
 	 *         <b>unregisterAnnouncementListener(int)</b> method, which
 	 *         unregisters your listener.
 	 *
 	 * @see #registerSyncAnnouncementListener(AnnouncementListener)
 	 */
 	public int registerSyncAnnouncementListener(final AnnouncementListener listener){
 		if(!this.getAsyncAnnouncementListeners().contains(listener)){
 			this.syncListeners.add(listener);
 		}
 		return this.getSyncAnnouncementListeners().indexOf(listener);
 	}
 
 	/**
 	 * Reloads the plugin, sending messages about the reload to the specified CommandSender.
 	 *
 	 * @param sender The CommandSender to send messages to about the reload.
 	 */
 	public void reloadPlugin(final CommandSender sender){
 		if(this.running){
 			this.turnOff(null);
 			this.updateConfiguration();
 			this.turnOn(sender);
 			this.sendMessage(sender, Severity.INFO, "FrogAnnounce has been successfully reloaded!");
 			this.sendMessage(sender, Severity.INFO, "Settings loaded " + this.announcements.size() + " announcements!");
 		}else{
 			this.sendMessage(sender, Severity.SEVERE, "No announcements running!");
 		}
 	}
 
 	/**
 	 * This method shows the FrogAnnounce specified help page to the specified
 	 * CommandSender.
 	 *
 	 * @param sender     The CommandSender object to send the help to.
 	 * @param pageString The page to send.
 	 */
 	public void returnHelp(final CommandSender sender, final String pageString){
 		final String or = ChatColor.WHITE.toString() + "|";
 		final String heading = ChatColor.DARK_GREEN.toString();
 		final String main = ChatColor.GOLD.toString();
 		final String command = ChatColor.AQUA.toString();
 		final String obligatory = ChatColor.DARK_RED.toString();
 		final String optional = ChatColor.GRAY.toString();
 		try{
 			int page;
 			page = Integer.parseInt(pageString);
 			if((page == 1) || (page == 0)){
 				this.sendMessage(sender, Severity.INFO, main + "*" + heading + "Help for FrogAnnounce " + super.getDescription().getVersion() + " (1/3)" + main + "*");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <help" + or + command + "?>" + main + " - Show this message.");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <on" + or + command + "off>" + main + " - Start or stop FrogAnnounce.");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <restart" + or + command + "reload>" + main + " - Restart FrogAnnounce.");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <interval" + or + command + "int>" + obligatory + " <minutes>" + main + " - Set the time between each announcement.");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <random" + or + command + "rand>" + obligatory + " <on" + or + obligatory + "off>" + main + " - Set random or consecutive.");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <broadcast" + or + command + "bc>" + obligatory + " <AnnouncementIndex>" + main + " - Announces the announcement specified by the index immediately. Will not interrupt the normal order/time. Please note that this starts at 0.");
 				this.sendMessage(sender, Severity.INFO, ChatColor.GOLD + "Use /fa help 2 to see the next page.");
 			}else if(page == 2){
 				this.sendMessage(sender, Severity.INFO, main + "*" + heading + "Help for FrogAnnounce " + super.getDescription().getVersion() + " (2/3)" + main + "*");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <add " + or + command + "add> " + obligatory + "<announcement message>" + main + " - Adds an announcement to the list. (Command /faadd or /fa-add is not a typo; technical restrictions forced this.)");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <remove " + or + command + "delete" + or + command + "rem" + or + command + "del> " + obligatory + "<announcementIndex>" + main + " - Removes the specified announcement (announcementIndex = announcement number from top to bottom in the file; starts at 0).");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <manualbroadcast" + or + command + "mbc" + obligatory + "<Message>" + main + " - Announces a message to the entire server. Ignores groups in the config.");
 				this.sendMessage(sender, Severity.INFO, ChatColor.GOLD + "Use /fa help 3 to see the next page.");
 			}else if(page == 3){
 				this.sendMessage(sender, Severity.INFO, main + "*" + heading + "Help for FrogAnnounce " + super.getDescription().getVersion() + " (3/3)" + main + "*");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <ignore" + or + command + "optout> " + optional + "[playerName] " + main + " - Ignore announcements. As long as you are ignored, you will not receive announcements. Specifying another player's name will force them to ignore announcements. Saves through disconnect.");
 				this.sendMessage(sender, Severity.INFO, command + "/fa <unignore" + or + command + "optin> " + optional + "[playerName] " + main + " - Unignore announcements. You will receive announcements as normal again. Specifying another player's name will force them to see announcements again. Saves through disconnect.");
 				this.sendMessage(sender, Severity.INFO, ChatColor.GRAY + "There are no more pages of help.");
 			}else{
 				this.sendMessage(sender, Severity.INFO, "There's no page " + page + ".");
 			}
 		}catch(final NumberFormatException e){
 			this.sendMessage(sender, Severity.INFO, "You must specify a page - positive integers only.");
 		}
 	}
 
 	private void sendMessage(CommandSender sender, final Severity severity, final String message){
 		if(sender == null)
 			sender = Bukkit.getConsoleSender();
 		switch(severity){
 			case INFO:
 				sender.sendMessage(ChatColor.DARK_GREEN + "[FrogAnnounce] " + ChatColor.GREEN + message);
 				break;
 			case WARNING:
 				sender.sendMessage(ChatColor.DARK_GREEN + "[FrogAnnounce] " + ChatColor.RED + message);
 				break;
 			case SEVERE:
 				sender.sendMessage(ChatColor.DARK_GREEN + "[FrogAnnounce] " + ChatColor.DARK_RED + message);
 				break;
 		}
 	}
 
 	private void sendMessage(final Player player, final Severity severity, final String message){
 		this.sendMessage((CommandSender)player, severity, message);
 	}
 
 	/**
 	 * This method sets the announcement interval of FrogAnnounce. Will take
 	 * effect immediately.
 	 *
 	 * @param cmdArgs - Based on command structure. To set the interval without the
 	 *                base of /fa interval, you should pass new
 	 *                String[]{<b>null</b>, <b>"new interval"</b>}.
 	 * @param player  - The CommandSender object to send output to.
 	 */
 	public void setInterval(final String[] cmdArgs, final CommandSender player){
 		final int newInterval = Integer.parseInt(cmdArgs[1]);
 		if(newInterval != this.interval){
 			this.interval = newInterval;
 			try{
 				this.cfg.updateConfiguration("Settings.Interval", this.interval);
 				this.sendMessage(player, Severity.INFO, "Announcement interval has successfully been changed to " + this.interval + ". Reloading configuration...");
 				this.reloadPlugin(player);
 			}catch(Exception e){
 				this.sendMessage(player, Severity.SEVERE, e.getMessage());
 			}
 		}else{
 			this.sendMessage(player, Severity.WARNING, "The announcement interval is already set to " + this.interval + "! There's no need to change it!");
 		}
 	}
 
 	/**
 	 * This method sets whether or not FrogAnnounce is announcing in random
 	 * order. Will take effect immediately.
 	 *
 	 * @param args   - Command arguments. To set it without a command base, you
 	 *               should pass new String[]{<b>null</b>, <b>"true"</b> (for
 	 *               random order) <i>or</i> <b>"false"</b> (for sequential
 	 *               order)}.
 	 * @param player - The CommandSender object to send output results to.
 	 */
 	public void setRandom(final String[] args, final CommandSender player){
 		final boolean s = Boolean.parseBoolean(args[1]);
 		if(s != this.random){
 			this.random = s;
 			try{
 				this.cfg.updateConfiguration("Settings.Random", s);
 				if(s){
 					this.sendMessage(player, Severity.INFO, "Announcer has been successfully changed to announce randomly. Reloading configuration...");
 				}else{
 					this.sendMessage(player, Severity.INFO, "Announcer has been successfully changed to announce in sequence. Reloading configuration...");
 				}
 				this.reloadPlugin(player);
 			}catch(Exception e){
 				this.sendMessage(player, Severity.SEVERE, e.getMessage());
 			}
 		}else if(this.random){
 			this.sendMessage(player, Severity.WARNING, "The announcer is already set to announce randomly! There's no need to change it!");
 		}else{
 			this.sendMessage(player, Severity.WARNING, "The announcer is already set to not announce randomly! There's no need to change it!");
 		}
 	}
 
 	private Boolean setupPermissions(){
 		final RegisteredServiceProvider<Permission> permissionProvider = super.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if(permissionProvider != null){
 			this.permission = permissionProvider.getProvider();
 		}
 		return this.permission != null;
 	}
 
 	/**
 	 * This method disables the announcement module of FrogAnnounce. It will
 	 * stop any further announcements for occurring.
 	 *
 	 * @param player The CommandSender object to send result messages to.
 	 *
 	 * @return Whether or not the announcer core was successfully disabled. Will
 	 *         return false if already disabled.
 	 */
 	public boolean turnOff(CommandSender player){
 		if(this.running){
 			this.getServer().getScheduler().cancelTask(this.taskId);
 			this.sendMessage(player, Severity.INFO, "Announcer disabled!");
 			this.running = false;
 			return true;
 		}else{
 			this.sendMessage(player, Severity.SEVERE, "The announcer is not running!");
 			return false;
 		}
 	}
 
 	/**
 	 * This method enables the announcement module of FrogAnnounce when it is
 	 * disabled.
 	 *
 	 * @param player The CommandSender object to send result messages to.
 	 *
 	 * @return Whether or not the announcer was able to start. Will not
 	 *         automatically restart the announcer. Will return false if it was
 	 *         already running.
 	 */
 	public boolean turnOn(CommandSender player){
 		if(!this.running){
 			if(this.announcements.size() > 0){
 				this.taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Announcer(this), this.interval * 1200, this.interval * 1200);
 				if(this.taskId == -1){
 					this.sendMessage(player, Severity.SEVERE, "The announcer module has failed to start! Please check your configuration. If this does not fix it, then submit a support ticket on the BukkitDev page for FrogAnnounce.");
 					return false;
 				}else{
 					this.counter = 0;
 					this.sendMessage(player, Severity.INFO, "Success! Now announcing every " + this.interval + " minute(s)!");
 					this.running = true;
 					return true;
 				}
 			}else{
 				this.sendMessage(player, Severity.SEVERE, "The announcer failed to start! There are no announcements!");
 				return false;
 			}
 		}else{
 			this.sendMessage(player, Severity.SEVERE, ChatColor.DARK_RED + "Announcer is already running.");
 			return false;
 		}
 	}
 
 	/**
 	 * Makes FrogAnnounce announce to a certain player after they were ignored.
 	 *
 	 * @param player - The player to relay output, as if they had done this to
 	 *               their target.
 	 * @param other  - The target player for FrogAnnounce to once again announce
 	 *               to.
 	 */
 	public void unignorePlayer(final CommandSender player, final String other){
 		Player otherPlayer;
 		if(other.isEmpty()){
 			otherPlayer = (Player)player;
 		}else{
 			otherPlayer = this.getServer().getPlayer(other);
 		}
 		if((otherPlayer != null) && (otherPlayer == player)){
 			if(this.permit(player, "frogannounce.unignore")){
 				if(this.ignoredPlayers.contains(player.getName())){
 					try{
 						this.ignoredPlayers.remove(otherPlayer.getName());
 						this.cfg.updateConfiguration("ignoredPlayers", this.ignoredPlayers);
 						this.sendMessage(otherPlayer, Severity.INFO, ChatColor.GRAY + "You are no longer being ignored by FrogAnnounce. You will receive announcements until you opt out of them again.");
 					}catch(Exception e){
 						this.sendMessage(otherPlayer, Severity.SEVERE, e.getMessage());
 					}
 				}else{
 					this.sendMessage(player, Severity.WARNING, "You're already not being ignored.");
 				}
 			}else{
 				this.sendMessage(player, Severity.WARNING, "You don't have sufficient permission to opt another player back into FrogAnnounce's announcements. Sorry!");
 			}
 		}else if((otherPlayer != null)){
 			if(this.permit(player, "frogannounce.unignore.other")){
 				if(this.ignoredPlayers.contains(otherPlayer.getName())){
 					try{
 						this.ignoredPlayers.remove(otherPlayer.getName());
 						this.cfg.updateConfiguration("ignoredPlayers", this.ignoredPlayers);
 						this.sendMessage(player, Severity.INFO, "Success! The player has been removed from FrogAnnounce's ignore list and will see its announcements again until he/she opts out again.");
 						this.sendMessage(otherPlayer, Severity.INFO, ChatColor.GRAY + "You are no longer being ignored by FrogAnnounce. You will receive announcements until you opt out of them again.");
 					}catch(Exception e){
 						this.sendMessage(player, Severity.SEVERE, e.getMessage());
 					}
 				}else{
 					this.sendMessage(player, Severity.WARNING, "That player is already not being ignored.");
 				}
 			}
 		}else{
 			this.sendMessage(player, Severity.WARNING, "That player isn't online right now!");
 		}
 	}
 
 	private void unregisterAllAnnouncementListeners(){
 		this.unregisterAllAsyncAnnouncementListeners();
 		this.unregisterAllSyncAnnouncementListeners();
 	}
 
 	private void unregisterAllAsyncAnnouncementListeners(){
 		for(int i = 0; i < this.getAsyncAnnouncementListeners().size(); i++){
 			this.asyncListeners.remove(i);
 		}
 	}
 
 	private void unregisterAllSyncAnnouncementListeners(){
 		for(int i = 0; i < this.getSyncAnnouncementListeners().size(); i++){
 			this.syncListeners.remove(i);
 		}
 	}
 
 	/**
 	 * This method unregisters a synchronous announcement listener from
 	 * FrogAnnounce's observer list, making your
 	 * AnnouncementListener-implementing child no longer be notified of
 	 * AnnouncementEvents. Any code currently running will continue to run until
 	 * it reaches the end of its execution path, as this method does <b>NOT</b>
 	 * destroy the thread.
 	 *
 	 * @param id The id of the AnnouncementListener in FrogAnnounce's observer
 	 *           list to remove. This is returned by the registration method.
 	 */
 	public void unregisterAsyncAnnouncementListener(final int id){
 		this.asyncListeners.set(id, null);
 	}
 
 	/**
 	 * This method unregisters an asynchronous announcement listener from
 	 * FrogAnnounce's observer list, making your
 	 * AnnouncementListener-implementing child no longer be notified of
 	 * AnnouncementEvents.
 	 *
 	 * @param id The id of the AnnouncementListener in FrogAnnounce's observer
 	 *           list to remove. This is returned by the registration method.
 	 */
 	public void unregisterSyncAnnouncementListener(final int id){
 		this.syncListeners.set(id, null);
 	}
 
 	public YamlConfiguration getConfig(){
 		try{
 			return new ConfigurationHandler().getConfig();
 		}catch(Exception e){
 			this.sendConsoleMessage(Severity.SEVERE, "An exception occurred while getting the configuration: "+e.getMessage());
 		}
 		return null;
 	}
 
 	public void reloadConfig(){
 		this.updateConfiguration();
 	}
 
 	private void updateConfiguration(){
 		try{
 			final YamlConfiguration config = new ConfigurationHandler().getConfig();
 			List<String> groups, worlds;
 			this.interval = config.getInt("Settings.Interval", 5);
 			this.random = config.getBoolean("Settings.Random", false);
 			this.usingPerms = config.getBoolean("Settings.Permission", true);
 			this.tag = FrogAnnounce.colourizeText(config.getString("Announcer.Tag", "&GOLD;[FrogAnnounce]"));
 			groups = config.getStringList("Announcer.GlobalGroups");
 			worlds = config.getStringList("Announcer.GlobalWorlds");
 			this.ignoredPlayers = (ArrayList<String>)config.getStringList("ignoredPlayers");
 			this.showJoinMessage = config.getBoolean("Settings.displayMessageOnJoin", false);
 			this.joinMessage = config.getString("Announcer.joinMessage", "Welcome to the server! Use /help for assistance.");
 			this.showConsoleAnnouncements = config.getBoolean("Settings.showConsoleAnnouncements", false);
 			this.announcements = new ArrayList<Announcement>();
 			int i = 0;
 			while(config.contains("Announcer.Announcements." + (++i))){
 				if(config.getBoolean("Announcer.Announcements." + i + ".Enabled", true)){
 					List<String> effectiveWorlds = config.getStringList("Announcer.Announcements." + i + ".Worlds"), effectiveGroups = config.getStringList("Announcer.Announcements." + i + ".Groups");
 					if(effectiveWorlds == null){
 						effectiveWorlds = worlds;
 					}else if(worlds != null){
 						for(final String world : worlds){
 							if(!effectiveWorlds.contains(world)){
 								effectiveWorlds.add(world);
 							}
 						}
 					}
 					if(effectiveGroups == null){
 						effectiveGroups = groups;
 					}else if(groups != null){
 						for(final String group : groups){
 							if(!effectiveGroups.contains(group)){
 								effectiveGroups.add(group);
 							}
 						}
 					}
 					this.announcements.add(new Announcement(config.getString("Announcer.Announcements." + i + ".Text"), effectiveGroups, effectiveWorlds, config.getStringList("Announcer.Announcements." + i + ".Commands")));
 				}
 			}
 			if(this.announcements.isEmpty()){
 				this.announcements.add(new Announcement("This plugin may be improperly configured. Please ensure all announcements have matching quotation marks around them. See plugin help pages for more info.", null, null, null));
 				this.interval = 5;
 			}
 		}catch(Exception e){
			e.printStackTrace();
//			this.sendConsoleMessage(Severity.SEVERE, e.getMessage());
 		}
 	}
 
 	public void sendConsoleMessage(Severity severity, String message){
 		this.sendMessage(Bukkit.getConsoleSender(), severity, message);
 	}
 
 	public void updateConfigurationFile(){
 		YamlConfiguration yml = this.getConfig();
 		if(!yml.contains("ConfigVersion")){
 			this.updateConfigFromV1(yml);
 		}else if(yml.getInt("ConfigVersion") < 2){
 			this.updateConfigFromV1(yml);
 		}
 	}
 
 	private void updateConfigFromV1(YamlConfiguration old){
 		YamlConfiguration yml = new YamlConfiguration();
 
 		yml.set("Settings.Interval", old.get("Settings.Interval"));
 		yml.set("Settings.Random", old.get("Settings.Random"));
 		yml.set("Settings.Permission", old.get("Settings.Permission"));
 		yml.set("Settings.displayMessagesOnJoin", old.get("Settings.displayMessagesOnJoin"));
 		yml.set("Settings.showConsoleAnnouncements", old.get("Settings.showConsoleAnnouncements"));
 
 		yml.set("Announcer.Tag", old.get("Announcer.Tag"));
 		yml.set("Announcer.joinMessage", old.getString("Announcer.joinMessage", ""));
		if(old.getBoolean("ToGroups", false))
			yml.set("Announcer.GlobalGroups", old.get("Announcer.Groups"));
		else
			yml.set("Announcer.GlobalGroups", new ArrayList<String>());
 		yml.set("Announcer.GlobalWorlds", new ArrayList<String>());
		for(int i = 1; i < old.getList("Announcer.Strings").size()+1; i++){
			List<String> z;
			String[] c;
			String s;

 			yml.set("Announcer.Announcements." + i + ".Enabled", true);
 
 			s = (String)old.getList("Announcer.Strings").get(i - 1);
 
 			yml.set("Announcer.Announcements." + i + ".Text", this.stripAnnouncementTags(s));
 
 			c = s.split("&GROUPS;");
 			if(c.length > 1){
				z = new ArrayList<String>();
 				if(c[1].contains("&USE-CMD;")){
 					s = c[1].split("&USE-CMD;")[0];
 				}else
 					s = c[1];
 
 				c = s.split(",");
 				for(String b : c)
 					z.add(b.trim());
 
 				yml.set("Announcer.Announcements." + i + ".Groups", z);
 			}
 
 			c = s.split("&USE-CMD;");
 			if(c.length > 1){
				z = new ArrayList<String>();
 				if(c[1].contains("&GROUPS;")){
					s = c[1].split("&GROUPS;")[1];
 				}else
 					s = c[1];
 
 				c = s.split(";");
 				for(String b : c)
 					z.add(b.trim());
 
 				yml.set("Announcer.Announcements."+i+".Commands", z);
 			}
 		}
 		yml.set("ignoredPlayers", old.get("ignoredPlayers"));
 		yml.set("ConfigVersion", 2);
 		try{
 			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("FrogAnnounce").getDataFolder(), "Configuration.yml");
 			if(!f.delete())
 				throw new IOException("Unable to delete old configuration file.");
 			yml.save(f);
 		}catch(IOException e){
 			this.sendConsoleMessage(Severity.SEVERE, "An exception occurred while trying to save the configuration: "+e.getMessage());
 		}
 	}
 
 	private String stripAnnouncementTags(String announcement){
 		if(!announcement.contains("&GROUPS;") && !announcement.contains("&USE-CMD;"))
 			return announcement;
 		String[] m = announcement.split("&USE-CMD;");
 
 		if(!m[0].contains("&GROUPS;"))
 			return m[0];
 		return m[0].split("&GROUPS;")[0];
 	}
 
 	class Announcer implements Runnable{
 		private final FrogAnnounce plugin;
 
 		@Override
 		public void run(){
 			this.plugin.announce(-1, true);
 		}
 
 		protected Announcer(final FrogAnnounce plugin){
 			this.plugin = plugin;
 		}
 	}
 
 	enum Severity{
 		INFO,
 		WARNING,
 		SEVERE
 	}
 }
