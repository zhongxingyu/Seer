 package me.kantenkugel.serveress.serverqueue;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ServerQueue extends JavaPlugin {
 
 	//Defining Variables
 	int maxplayers, normalslots, reservedslots;
 	boolean reserveperm, kickperm, kickvip, kicknotice;
 	List<Object> vipplayers;															//List with the VIPs from the config
 	HashMap<String, Long> joinhash = new HashMap<String, Long>();						//if Notify is enabled, this stores the time of last attempt
 	public final Logger logger = Logger.getLogger("Minecraft");
 	PluginDescriptionFile pdf;
 	
 	public void onEnable() {
 		this.pdf = this.getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		this.maxplayers = getServer().getMaxPlayers();									//get the slots of the server
 		this.getConfig().options().copyDefaults(true);									//Write default values to config, if section is not present
 		this.saveDefaultConfig();
 		this.saveConfig();													
 		loadconfigfile();																//saves the config-values to the java-values
 		pm.registerEvents(new SQListener(this), this);									//register the EventListener
 		//Print Status to console
 		this.logger.info("[" + this.pdf.getName() + "] There are " + this.getServer().getOnlinePlayers().length + "/" + this.maxplayers + "(" + this.normalslots + "+" + this.reservedslots + ") Players online.");
 		this.logger.info("[" + this.pdf.getName() + "] v" + this.pdf.getVersion() + " is enabled!");
 	}
 	
 	public void onDisable() {
 		this.logger.info("[" + this.pdf.getName() + "] is disabled!");
 	}
 	
 	private void loadconfigfile() {														//reads the config-file and saves it to local variables
 		this.reloadConfig();															//if admin made changes, refresh the file
 		this.reservedslots = this.getConfig().getInt("ServerQueue.SlotsToReserve");		//slots to reserve
 		this.normalslots = this.maxplayers - this.reservedslots;						//calculate the normal slots (max-vip)
 		this.reserveperm = this.getConfig().getBoolean("ServerQueue.PermReserve");		//whether to reserve or not to reserve vipslots for permissionusers
 		this.kickperm = this.getConfig().getBoolean("ServerQueue.Kick.Perm");			//kick vor vip-permission
 		this.kickvip = this.getConfig().getBoolean("ServerQueue.Kick.VIP");				//kick vor vips in the config
 		this.kicknotice = this.getConfig().getBoolean("ServerQueue.Kick.Notice");		//notice joining vips before kicking
 		this.vipplayers = this.getConfig().getList("VIPs");								//load the vips from the file
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdlabel, String[] args) {
 		if(cmdlabel.equalsIgnoreCase("sq") || cmdlabel.equalsIgnoreCase("serverqueue")) {//just to be sure
 			//check if player is allowed to use commands
			if(sender != this.getServer().getConsoleSender() && !(sender.hasPermission("serverqueue.reload") || sender.hasPermission("serverqueue.status"))) return noperm((Player) sender);
 			if(args.length == 1) {
 				switch(args[0]) {														//switch between subcommands
 				case "reload":															//reloads the config-file (function loadconfig())
 					if(sender != this.getServer().getConsoleSender() && !(sender.hasPermission("serverqueue.reload"))) return noperm((Player) sender);
 					this.logger.info("["+this.pdf.getName()+"] Reloading Config-File!");
 					if(sender != this.getServer().getConsoleSender()) sender.sendMessage(ChatColor.GREEN+"["+this.pdf.getName()+"] Reloading Config-File!");
 					loadconfigfile();
 					break;
 				case "status":															//prints the status-message to the player or console
 					if(sender != this.getServer().getConsoleSender() && !(sender.hasPermission("serverqueue.status"))) return noperm((Player) sender);
 					if(sender == this.getServer().getConsoleSender()) this.logger.info("["+this.pdf.getName()+"] Status: Players online: "+this.getServer().getOnlinePlayers().length+"/"+this.maxplayers+"("+(this.maxplayers - this.reservedslots)+"+"+this.reservedslots+")");
 					else sender.sendMessage(ChatColor.GREEN+"["+this.pdf.getName()+"] Status: Players online: "+this.getServer().getOnlinePlayers().length+"/"+this.maxplayers+"("+(this.maxplayers - this.reservedslots)+"+"+this.reservedslots+")");
 					break;
 				default:
 					return false;
 				}
 				return true;
 				
 			} else if(args.length == 2) {
 				Player target;
 				switch(args[0]) {
 				case "addmaster":
 				case "am":
 					if((sender != this.getServer().getConsoleSender()) && !(sender.hasPermission("serverqueue.setmaster"))) return noperm((Player) sender);
 					target = this.getServer().getPlayer(args[1]);
 					if(target != null && !(this.vipplayers.contains(target.getName().toLowerCase()))) {
 						this.logger.info("["+this.pdf.getName()+"] Adding Player "+target.getName()+" to Master-VIPs");
 						if(sender instanceof Player) sender.sendMessage(ChatColor.GOLD+"Adding Player "+target.getName()+" to Master-VIPs");
 						this.vipplayers.add(target.getName().toLowerCase());
 						this.getConfig().set("VIPs", vipplayers);
 						this.saveConfig();
 					} else if(target != null) {
 						if(sender == this.getServer().getConsoleSender()) this.logger.info("["+this.pdf.getName()+"] Player "+target.getName()+" is already Master-VIP");
 						else sender.sendMessage(ChatColor.GREEN+"["+this.pdf.getName()+"] Player "+target.getName()+" is already Master-VIP");
 					} else {
 						if(sender == this.getServer().getConsoleSender()) this.logger.info("["+this.pdf.getName()+"] Player not found!");
 						else sender.sendMessage(ChatColor.RED+"["+this.pdf.getName()+"] Player not found");
 					}
 					break;
 				case "remmaster":
 				case "rm":
 					if((sender != this.getServer().getConsoleSender()) && !(sender.hasPermission("serverqueue.setmaster"))) return noperm((Player) sender);
 					if(this.vipplayers.contains(args[1].toLowerCase())) {
 						this.logger.info("["+this.pdf.getName()+"] Removing Player "+args[1]+" from Master-VIPs");
 						if(sender instanceof Player) sender.sendMessage(ChatColor.GOLD+"Removing Player "+args[1]+" from Master-VIPs");
 						this.vipplayers.remove((String) args[1].toLowerCase());
 						this.getConfig().set("VIPs", vipplayers);
 						this.saveConfig();
 					} else {
 						if(sender == this.getServer().getConsoleSender()) this.logger.info("["+this.pdf.getName()+"] Player "+args[1]+" is no Master-VIP");
 						else sender.sendMessage(ChatColor.RED+"["+this.pdf.getName()+"] Player"+args[1]+" is no Master-VIP");
 					}
 					break;
 				default:
 					return false;					
 				}
 				return true;
 			} else return false;
 			
 		} else return false;
 	}
 	
 	private boolean noperm(Player pl) {													//simple function to print a permission-error
 		pl.sendMessage(ChatColor.RED+"Your not allowed to do that");
 		return true;
 	}
 }
