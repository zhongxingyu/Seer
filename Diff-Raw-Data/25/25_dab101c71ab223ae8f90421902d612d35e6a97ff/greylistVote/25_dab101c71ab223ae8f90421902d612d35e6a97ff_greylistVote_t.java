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
 		this.logger.info("[" + pdfFile.getName() + "] is now disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled.");
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
 		if (commandLabel.equalsIgnoreCase("glv")) {
 			if (args.length == 0) {
 				PluginDescriptionFile pdfFile = this.getDescription();
 				sender.sendMessage(ChatColor.GOLD + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
 				sender.sendMessage(ChatColor.GOLD + "Commands: {optional} [required]");
 				sender.sendMessage(ChatColor.GOLD + "  /glv " + ChatColor.GRAY + ": View all GreylistVote commands");
 				sender.sendMessage(ChatColor.GOLD + "  /greylist [player] " + ChatColor.GRAY + "Increase [player]s reputation");
 				sender.sendMessage(ChatColor.GOLD + "  /gl [player] " + ChatColor.GRAY + "Same as /greylist");
 				sender.sendMessage(ChatColor.GOLD + "  /griefer [player] " + ChatColor.GRAY + "Decrease [player]s reputation");
 				sender.sendMessage(ChatColor.GOLD + "  /votelist {player} " + ChatColor.GRAY + "View your (or {player}s) reputation");
 				sender.sendMessage(ChatColor.GOLD + "  /glvlist {player} " + ChatColor.GRAY + "Same as /votelist");
 				if (sender.hasPermission("greylistvote.admin")) {
 					sender.sendMessage(ChatColor.GOLD + "Admin Commands:");
 					sender.sendMessage(ChatColor.GOLD + "  /glv setrep [req. votes] " + ChatColor.GRAY + ": Set required reputation");
 				}
 				return true;
 			}
 			else if (args.length == 2) {
 				if (!sender.hasPermission("greylistvote.admin")) {
 					sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
 					return false;
 				}
 				int reqVotes = config.getInt("required_votes", 2);
 				if (!args[0].equalsIgnoreCase("setrep")) {
 					sender.sendMessage(ChatColor.RED + "Command not recognised!");
 					return false;
 				}
 				try {
 					reqVotes = Integer.parseInt(args[1]);
 				}
 				catch(NumberFormatException nfe) {
 					// Failed. Number not an integer
 					sender.sendMessage(ChatColor.RED + "[req. votes] must be a number!" );
 					return false;
 				}
 				this.config.set("required_votes", reqVotes);
 				this.saveConfig();
 				sender.sendMessage(ChatColor.GOLD + "Reputation requirement now set to " + ChatColor.WHITE + args[1]);
 				sender.sendMessage(ChatColor.GOLD + "Player approval will not be updated until they receive their next vote.");
 				return true;
 			}
 			return false;
 		}
 		else if (commandLabel.equalsIgnoreCase("greylist") || commandLabel.equalsIgnoreCase("gl")) {
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
 				int reqVotes = this.config.getInt("required_votes");
 				String griefList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 				String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 				String[] voteArray = null;
 				String[] griefArray = null;
 				if (voteList != null) {
 					voteArray = voteList.split(",");
 				}
 				if (griefList != null) {
 					griefArray = griefList.split(",");
 				}
 				if (!(sender instanceof Player)) {
 					// Voter is the console
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", "Server");
 					this.usersConfig.set(target.getName().toLowerCase() + ".griefer", null);
 					this.setApproved(target);
 					this.saveUsersConfig();
 					sender.sendMessage(args[0] + ChatColor.GOLD + " has been greylisted!");
 					return true;
 				}
 				if (sender.getName() == target.getName()) {
 					// Player voting for self
 					sender.sendMessage(ChatColor.RED + "You cannot vote for yourself!");
 					return true;
 				}
 				// Target has votes already
 				boolean found = false;
 				for (String vote : voteArray) {
					if (vote.equalsIgnoreCase(sender.getName())) {
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
				int rep = 0;
				if (voteArray != null) {
					rep += voteArray.length + 1;
				}
				if (griefArray != null) {
					rep -= griefArray.length;
				}
				if (rep >= reqVotes && !target.hasPermission("greylistvote.approved")) {
 					// Enough votes received
 					this.setApproved(target);
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
 				int reqVotes = this.config.getInt("required_votes");
 				String griefList = this.usersConfig.getString(target.getName().toLowerCase() + ".griefer", null);
 				String voteList = this.usersConfig.getString(target.getName().toLowerCase() + ".votes", null);
 				String[] voteArray = null;
 				String[] griefArray = null;
 				if (voteList != null) {
 					voteArray = voteList.split(",");
 				}
 				if (griefList != null) {
 					griefArray = griefList.split(",");
 				}
 				if (!(sender instanceof Player)) {
 					// Voter is the console
 					this.usersConfig.set(target.getName().toLowerCase() + ".griefer", "Server");
 					this.usersConfig.set(target.getName().toLowerCase() + ".votes", null);
 					this.setGriefer(target);
 					this.saveUsersConfig();
 					sender.sendMessage(args[0] + ChatColor.GOLD + " has been " + ChatColor.DARK_GRAY +"Black-Balled!");
 					return true;
 				}
 				if (sender.getName() == target.getName()) {
 					// Player voting for self
 					sender.sendMessage(ChatColor.RED + "You cannot vote for yourself!");
 					return true;
 				}
 				boolean found = false;
 				for (String vote : griefArray) {
					if (vote.equalsIgnoreCase(sender.getName())) {
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
 						chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " voted for " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + " to be " + ChatColor.DARK_GRAY + "black-balled" + ChatColor.GOLD + " for griefing!");
 					}
 					else if (chatPlayer.getName() != sender.getName()) {
 						chatPlayer.sendMessage(sender.getName() + ChatColor.GOLD + " voted for you to be " + ChatColor.DARK_GRAY + " black-balled" + ChatColor.GOLD + " for " + ChatColor.RED + "griefing" + ChatColor.GOLD + "!");
 					}
 				}
 				this.usersConfig.set(target.getName().toLowerCase() + ".griefer", voteList + "," + sender.getName());
				int rep = 0;
				if (voteArray != null) {
					rep += voteArray.length;
				}
				if (griefArray != null) {
					rep -= griefArray.length + 1;
				}
				if (rep < reqVotes) {
 					// Enough votes received
 					this.setGriefer(target);
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
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation: " + ChatColor.WHITE + "0");
 				}
 				else {
 					sender.sendMessage(ChatColor.GOLD + "You have received votes from:");
 					String[] voteArray = null;
 					String[] griefArray = null;
 					if (voteList != null) {
 						voteArray = voteList.split(",");
 						if (voteArray.length != 0) {
 							String votes = ChatColor.GREEN + "  Approvals: " + ChatColor.GOLD;
 							for (String vote : voteArray) {
 								votes = votes + vote + " ";
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					if (griefList != null) {
 						griefArray = griefList.split(",");
 						if (griefArray.length != 0) {
 							String votes = ChatColor.DARK_GRAY + "  Black-Balls: " + ChatColor.GOLD;
 							for (String vote : griefArray) {
 								votes = votes + vote + " ";
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					int reputation = 0;
 					if (voteArray != null) {
 						reputation += voteArray.length;
 					}
 					if (griefArray != null) {
 						reputation -=  griefArray.length;
 					}
 					int reqVotes = config.getInt("required_votes");
 					String repText = "";
 					if (reputation >= reqVotes) {
 						repText = " " + ChatColor.GREEN + reputation;
 					}
 					else {
 						repText = " " + ChatColor.RED + reputation;
 					}
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation:" + repText);
 					sender.sendMessage(ChatColor.GOLD + "Required Reputation: " + ChatColor.WHITE + reqVotes);
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
 				String griefList = this.usersConfig.getString(target.toLowerCase() + ".griefer", null);
 				if (voteList == null && griefList == null) {
 					sender.sendMessage(DN + ChatColor.GOLD + " has not received any votes.");
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation: " + ChatColor.WHITE + "0");
 				}
 				else {
 					sender.sendMessage(DN + ChatColor.GOLD + " has received votes from:");
 					String[] voteArray = null;
 					String[] griefArray = null;
 					if (voteList != null) {
 						voteArray = voteList.split(",");
 						if (voteArray.length != 0) {
 							String votes = ChatColor.GREEN + "  Approvals: " + ChatColor.GOLD;
 							for (String vote : voteArray) {
 								votes = votes + vote + " ";
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					if (griefList != null) {
 						griefArray = griefList.split(",");
 						if (griefArray.length != 0) {
 							String votes = ChatColor.BLACK + "  Black-Balls: " + ChatColor.GOLD;
 							for (String vote : griefArray) {
 								votes = votes + vote + " ";
 							}
 							sender.sendMessage(votes);
 						}
 					}
 					int reputation = 0;
 					if (voteArray != null) {
 						reputation += voteArray.length;
 					}
 					if (griefArray != null) {
 						reputation -=  griefArray.length;
 					}
 					int reqVotes = config.getInt("required_votes");
 					String repText = "";
 					if (reputation >= reqVotes) {
 						repText = " " + ChatColor.GREEN + reputation;
 					}
 					else {
 						repText = " " + ChatColor.RED + reputation;
 					}
 					sender.sendMessage(ChatColor.GOLD + "Current Reputation:" + repText);
 					sender.sendMessage(ChatColor.GOLD + "Required Reputation: " + ChatColor.WHITE + reqVotes);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void setApproved(Player target) {
 		target.addAttachment(this, "greylistvote.approved", true);
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
 		target.addAttachment(this, "greylistvote.approved",false);
 		Player[] onlinePlayers = getServer().getOnlinePlayers();
 		for (Player chatPlayer : onlinePlayers) {
 			if (chatPlayer.getName() != target.getName()) {
 				chatPlayer.sendMessage(target.getName() + ChatColor.GOLD + " has been " + ChatColor.DARK_GRAY + "black-balled" + ChatColor.GOLD + " for " + ChatColor.RED + " griefing" + ChatColor.GOLD + "!");
 			}
 			else {
 				chatPlayer.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_GRAY + "black-balled" + ChatColor.RED + " for griefing!");
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
