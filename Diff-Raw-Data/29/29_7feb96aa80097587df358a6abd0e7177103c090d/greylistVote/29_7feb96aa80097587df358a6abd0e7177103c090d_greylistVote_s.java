 package me.ellbristow.greylistVote;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class greylistVote extends JavaPlugin {
 	
 	public static greylistVote plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public final greyBlockListener blockListener = new greyBlockListener(this);
 	protected FileConfiguration config;
 	private FileConfiguration usersConfig = null;
 	private File usersFile = null;
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " is now disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled.");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, this.blockListener, Event.Priority.Normal, this);
 		this.config = this.getConfig();
 		this.config.set("required_votes", this.config.getInt("required_votes"));
 		this.saveConfig();
 		this.usersConfig = this.getUsersConfig();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("greylist")) {
 			if (args.length != 1) {
 				// No player specified or too many arguments
 				return false;
 			}
 			else {
 				Player target = getServer().getPlayer(args[0]);
 				if (target == null) {
 					// Player not online
 					sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + args[0] + ChatColor.RED + " not found or not online!");
 					return false;
 				}
 				if (!(sender instanceof Player)) {
 					// Voter is the console
 					sender.sendMessage(ChatColor.RED + "Sorry! The console can't vote!");
 					return true;
 				}
 				if (sender.getName() == target.getName()) {
 					// Player voting for self
 					sender.sendMessage(ChatColor.RED + "You cannot vote for yourself!");
 					return true;
 				}
 				if (target.hasPermission("greylistvote.approved")) {
 					// Target already approved
 					sender.sendMessage(ChatColor.WHITE + target.getName() + ChatColor.RED + " has already been approved!");
 					return true;
 				}
 				int reqVotes = this.config.getInt("required_votes");
				String voteList = this.usersConfig.getString(target + ".votes", null);
 				if (voteList == null) {
 					// No votes received for this target player
 					if (reqVotes <= 1) {
 						// Enough votes received
 						this.setApproved(target);
 						return true;
 					}
 					this.usersConfig.set(target.getName() + ".votes", sender.getName());
 				}
 				else {
 					// Target has votes already
 					String[] voteArray = voteList.split(",");
 					boolean found = false;
 					for (String vote : voteArray) {
 						if (vote == sender.getName()) {
 							found = true;
 						}
 					}
 					if (found) {
 						// Voter has already voted for this target player
 						sender.sendMessage(ChatColor.RED + "You have already voted for " + ChatColor.WHITE + target.getName());
 						return true;
 					}
 					sender.sendMessage(ChatColor.GOLD + "Your greylist vote for " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + " has been accepted!");
 					Player[] onlinePlayers = getServer().getOnlinePlayers();
 					for (Player chatPlayer : onlinePlayers) {
 						if (chatPlayer.getName() != target.getName() && chatPlayer.getName() != sender.getName()) {
 							chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " voted for " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + " to be greylisted!");
 						}
 						else if (chatPlayer.getName() != sender.getName()) {
 							chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " voted for you to be greylisted!");
 						}
 					}
					this.usersConfig.set(target.getName() + ".votes", voteList + ", " + sender.getName());
 					if (voteArray.length + 1 >= reqVotes) {
 						// Enough votes received
 						this.setApproved(target);
 						return true;
 					}
 				}
 				this.saveUsersConfig();
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void setApproved(Player target) {
 		PermissionAttachment attachment = target.addAttachment(plugin);
 		attachment.setPermission("greylistvote.approved", true);
 		Player[] onlinePlayers = getServer().getOnlinePlayers();
 		for (Player chatPlayer : onlinePlayers) {
 			if (chatPlayer.getName() != target.getName()) {
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + " has been greylisted!");
 			}
 			else {
 				chatPlayer.sendMessage("You have been greylisted! Go forth and buildify!");
 			}
 		}
 	}
 	
 	public void loadUsersConfig() {
 		if (this.usersFile == null) {
 			this.usersFile = new File(getDataFolder(),"users.yml");
 		}
 		this.usersConfig = YamlConfiguration.loadConfiguration(this.usersFile);
 	}
 	
 	public FileConfiguration getUsersConfig() {
 		if (this.usersConfig == null) {
 			this.loadUsersConfig();
 		}
 		return this.usersConfig;
 	}
 	
 	public void saveUsersConfig() {
 		if (this.usersConfig == null || this.usersFile == null) {
 			return;
 		}
 		try {
 			this.usersConfig.save(this.usersFile);
 		} catch (IOException ex) {
 			this.logger.log(Level.SEVERE, "Could not save " + this.usersFile, ex );
 		}
 	}
 }
