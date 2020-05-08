 package me.kantenkugel.serveress.whitelisted;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Whitelisted extends JavaPlugin {
 	//public boolean SqlOn, SqlEn;												//SqlOn -> No errors; SqlEn -> Config
 	//public boolean Customtable, SqlMode;										//SqlMode: 0->Own, 1->External
 	//public String Sqlname;
 	public String chatprefix;
 	public String whitelistmsg;
 	public boolean showconsolelog, notify;
 	public PluginDescriptionFile pdf;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public List<String> whitelisted, denylist;
 	
 	public void onEnable() {
 		pdf = this.getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new WlListener(this), this);
 		chatprefix = "["+pdf.getName()+"] ";
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 		logger.info(chatprefix + "v" + pdf.getVersion() + " is now enabled!");
 	}
 	
 	public void onDisable() {
 		logger.info(chatprefix + "is now disabled!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(sender instanceof Player && !(sender.hasPermission("whitelisted.admin"))) {
 			sender.sendMessage(ChatColor.RED + chatprefix + "You dont have the Permission to do that!");
 			return true;
 		} else if(args.length > 0) {
 			switch(args[0].toLowerCase()) {
 			case "list":
 				refreshlist();
 				if(whitelisted.isEmpty()) {
 					report(sender, "No Players are whitelisted!", ChatColor.YELLOW);
 					break;
 				}
 				String seplist = "";
 				for(String pl: whitelisted) {
 					seplist = seplist + pl + ", ";
 				}
 				report(sender, "Players in list: ", ChatColor.YELLOW);
 				report(sender, seplist, ChatColor.GREEN);
 				break;
 			case "add":
 				if(args.length != 2) {
 					report(sender, "You have to specify a player.", ChatColor.RED);
 					report(sender, "e.g. /whitelist add Steve", ChatColor.RED);
 				} else {
 					if(wladd(args[1].toLowerCase())) {
 						report(sender, "Player "+args[1]+" added to whitelist", ChatColor.GREEN);
 					} else {
 						report(sender, "That Player is already whitelisted", ChatColor.GOLD);
 					}
 				}
 				break;
 			case "deny":
 				if(args.length != 2) {
 					report(sender, "You have to specify a player.", ChatColor.RED);
					report(sender, "e.g. /whitelist deny Steve", ChatColor.RED);
 				} else {
 					if(wldeny(args[1].toLowerCase())) {
 						report(sender, "Player "+args[1]+" has been moved to the deny-list", ChatColor.GREEN);
 					} else {
 						report(sender, "That Player is already denied", ChatColor.GOLD);
 					}
 				}
 			case "rem":
 			case "remove":
 			case "rm":
 				if(args.length != 2) {
 					report(sender, "You have to specify a player.", ChatColor.RED);
 					report(sender, "e.g. /whitelist rem Steve", ChatColor.RED);
 				} else {
 					if(wlremove(args[1].toLowerCase())) {
 						report(sender, "Player removed from whitelist", ChatColor.GREEN);
 					} else {
 						report(sender, "That player is not whitelisted!", ChatColor.GOLD);
 					}
 					
 				}
 				break;
 			default:
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private void report(CommandSender sender, String msg, ChatColor color) {
 		if(sender == getServer().getConsoleSender()) {
 			logger.info(chatprefix + msg);
 		} else if(sender instanceof Player) {
 			sender.sendMessage(color + chatprefix + msg);
 		}	
 	}
 	
 	public void refreshlist() {
 		this.reloadConfig();
 		whitelisted = this.getConfig().getStringList("Whitelist");
 		denylist = this.getConfig().getStringList("Denylist");
 		showconsolelog = this.getConfig().getBoolean("Config.ShowLog", false);
 		notify = this.getConfig().getBoolean("Config.Notify");
 		whitelistmsg = this.getConfig().getString("Config.WhitelistMsg", "You are not whitelisted!");
 		
 	}
 	
 	public boolean wldeny(String player) {
 		this.refreshlist();
 		if(denylist.contains(player)) return false;
 		else {
 			if(whitelisted.contains(player)) {
 				whitelisted.remove(player);
 				this.getConfig().set("Whitelist", whitelisted);
 			}
 			denylist.add(player);
 			this.getConfig().set("Denylist", denylist);
 			this.saveConfig();
 			return true;
 		}
 	}
 	
 	public boolean wlremove(String player) {
 		this.refreshlist();
 		if(!(whitelisted.contains(player))) return false;
 		else {
 			whitelisted.remove(player);
 			this.getConfig().set("Whitelist", whitelisted);
 			this.saveConfig();
 			return true;
 		}
 	}
 	
 	public boolean wladd(String player) {
 		this.refreshlist();
 		if(whitelisted.contains(player)) return false;
 		else {
 			if(denylist.contains(player)) {
 				denylist.remove(player);
 				this.getConfig().set("Denylist", denylist);
 			}
 			whitelisted.add(player);
 			this.getConfig().set("Whitelist", whitelisted);
 			this.saveConfig();
 			return true;
 		}
 	}	
 
 }
