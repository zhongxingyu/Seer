 package me.ellbristow.greylistVote;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
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
 		this.config.set("griefer_votes", this.config.getInt("griefer_votes"));
 		this.saveConfig();
 		this.usersConfig = this.getUsersConfig();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("greylist") || commandLabel.equalsIgnoreCase("gl")) {
 			if (args.length != 1) {
 				// No player specified or too many arguments
 				return false;
 			}
 			else {
 				Player target = getServer().getOfflinePlayer(args[0]).getPlayer();
 				if (target == null) {
 					// Player not online
 					sender.sendMessage(args[0] + ChatColor.RED + " not found!");
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
 				String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 				if (voteList == null) {
 					// No votes received for this target player
 					if (reqVotes <= 1) {
 						// Enough votes received
 						this.setApproved(target);
 						return true;
 					}
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", sender.getName());
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
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", voteList + "," + sender.getName());
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
 		else if (commandLabel.equalsIgnoreCase("griefer")) {
 			if (args.length != 1) {
 				// No player specified or too many arguments
 				return false;
 			}
 			else {
 				Player target = getServer().getOfflinePlayer(args[0]).getPlayer();
 				if (target == null) {
 					// Player not online
 					sender.sendMessage(args[0] + ChatColor.RED + " not found!");
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
 				int reqVotes = this.config.getInt("griefer_votes");
 				String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 				if (voteList == null) {
 					// No votes received for this target player
 					if (reqVotes <= 1) {
 						// Enough votes received
 						this.setGriefer(target);
 						return true;
 					}
 					this.usersConfig.set(target.getName().toLowerCase() + ".griefer", sender.getName());
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", null);
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
 					sender.sendMessage(ChatColor.GOLD + "Your griefer vote for " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + " has been accepted!");
 					Player[] onlinePlayers = getServer().getOnlinePlayers();
 					for (Player chatPlayer : onlinePlayers) {
 						if (chatPlayer.getName() != target.getName() && chatPlayer.getName() != sender.getName()) {
 							chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " voted for " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + " to be " + ChatColor.BLACK + "black-balled" + ChatColor.GOLD + " for griefing!");
 						}
 						else if (chatPlayer.getName() != sender.getName()) {
 							chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " voted for you to be " + ChatColor.BLACK + " black-balled" + ChatColor.GOLD + " for " + ChatColor.RED + "griefing" + ChatColor.GOLD + "!");
 						}
 					}
 					this.usersConfig.set(target.getName().toLowerCase() + ".griefer", voteList + "," + sender.getName());
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", null);
 					if (voteArray.length + 1 >= reqVotes) {
 						// Enough votes received
 						this.setGriefer(target);
 						return true;
 					}
 				}
 				this.saveUsersConfig();
 				return true;
 			}
 		}
 		else if (commandLabel.equalsIgnoreCase("votelist") || commandLabel.equalsIgnoreCase("glvlist")) {
 			if (args.length == 0) {
 				String voteList = this.usersConfig.getString(sender.getName().toLowerCase() + ".votes", null);
 				String griefList = this.usersConfig.getString(sender.getName().toLowerCase() + ".griefer", null);
 				if (voteList == null && griefList == null) {
 					sender.sendMessage(ChatColor.GOLD + "You have not received any votes.");
 				}
 				else {
 					sender.sendMessage(ChatColor.GOLD + "You have received votes from:");
					String[] voteArray = voteList.split(",");
					if (voteArray.length != 0) {
						String votes = ChatColor.GREEN + "  Approvals: " + ChatColor.GOLD;
						for (String vote : voteArray) {
							votes = votes + vote + " ";
 						}
						sender.sendMessage(votes);
 					}
					voteArray = griefList.split(",");
					if (voteArray.length != 0) {
						String votes = ChatColor.BLACK + "  Black-Balls: " + ChatColor.GOLD;
						for (String vote : voteArray) {
							votes = votes + vote + " ";
 						}
						sender.sendMessage(votes);
 					}
 				}
 				return true;
 			}
 			else {
 				OfflinePlayer checktarget = getServer().getOfflinePlayer(args[0]);
 				String DN = null;
 				String target = null;
 				if (checktarget.isOnline()) {
 					target = checktarget.getPlayer().getName();
 					DN = checktarget.getPlayer().getDisplayName();
 				}
 				else {
 					if (checktarget != null) {
 						target = checktarget.getName();
 						DN = checktarget.getName();
 					}
 				}
 				if (target == null) {
 					// Player not found
 					sender.sendMessage(args[0] + ChatColor.RED + " not found!");
 					return false;
 				}
 				String voteList = this.usersConfig.getString(target.toLowerCase() + ".votes", null);
 				if (voteList == null) {
 					sender.sendMessage(DN + ChatColor.GOLD + " has not received any votes.");
 				}
 				else {
 					sender.sendMessage(DN + ChatColor.GOLD + " has received votes from:");
 					String[] voteArray = voteList.split(",");
 					for (String vote : voteArray) {
 						sender.sendMessage(ChatColor.GOLD + "  " + vote);
 					}
 				}
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
 	
 	public void setGriefer(Player target) {
 		PermissionAttachment attachment = target.addAttachment(plugin);
 		attachment.setPermission("greylistvote.approved", false);
 		Player[] onlinePlayers = getServer().getOnlinePlayers();
 		for (Player chatPlayer : onlinePlayers) {
 			if (chatPlayer.getName() != target.getName()) {
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + " has been " + ChatColor.BLACK + "black-balled" + ChatColor.GOLD + " for " + ChatColor.RED + " griefing" + ChatColor.GOLD + "!");
 			}
 			else {
 				chatPlayer.sendMessage(ChatColor.RED + "You have been " + ChatColor.BLACK + "black-balled" + ChatColor.RED + " for griefing!");
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
