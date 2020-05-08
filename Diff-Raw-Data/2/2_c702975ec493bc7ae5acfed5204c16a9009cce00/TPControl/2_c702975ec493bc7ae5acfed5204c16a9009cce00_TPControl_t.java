 package nu.nerd.tpcontrol;
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TPControl extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	
 	
 	//private final TPControlListener cmdlistener = new TPControlListener(this);
 	public final Configuration config = new Configuration(this);
 	
 	private HashMap<String,User> user_cache = new HashMap<String, User>();
 	
 	
 	
 	
 	
 	public void onEnable(){
 		log = this.getLogger();
 		
 		//Load config
 		File config_file = new File(getDataFolder(), "config.yml");
 		if(!config_file.exists()) {
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}
 		config.load();
 		
 		//TODO: Can we get away with async?
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
             	for(User u : user_cache.values()) {
             		u.save();
             	}
             }
 		}, config.SAVE_INTERVAL*20, config.SAVE_INTERVAL*20);
 		
 		log.info("TPControl has been enabled!");
 	}
 	
 	public void onDisable(){
 		log.info("TPControl has been disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
 		//
 		// /tp
 		//
         if (command.getName().equalsIgnoreCase("tp")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
         	Player p1 = (Player)sender;
         	if(!canTP(p1)) {
         		messagePlayer(p1, "You do not have permission.");
         		return true;
         	}
         	
         	if(args.length != 1) {
         		messagePlayer(p1, "Usage: /tp <player>");
         		return true;
         	}
         	
         	Player p2 = getServer().getPlayer(args[0]);
         	if(p2 == null) {
         		messagePlayer(p1, "Couldn't find player "+ args[0]);
         		return true;
         	}
         	
         	User u2 = getUser(p2);
         	String mode;
         	
         	if(canOverride(p1, p2)) {
         		mode = "allow";
         	} else {
         		mode = u2.getCalculatedMode(p1); //Get the mode to operate under (allow/ask/deny)
         	}
         	
         	
         	if (mode.equals("allow")) {
         		messagePlayer(p1, "Teleporting you to " + p2.getName() + ".");
         		p1.teleport(p2);
         	} 
         	else if (mode.equals("ask")) {
         		u2.lodgeRequest(p1);
         	} 
         	else if (mode.equals("deny")) {
         		messagePlayer(p1, p2.getName() + " has teleportation disabled.");
         	}
         	return true;
         }
         //
         // /tpmode allow|ask|deny
         //
         else if (command.getName().equalsIgnoreCase("tpmode")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
         	
         	if(args.length != 1 || (!args[0].equals("allow") &&
         							!args[0].equals("ask") &&
         							!args[0].equals("deny"))) {
         		messagePlayer(p2, "Usage: /tpmode allow|ask|deny");
         		messagePlayer(p2, "Your are currently in *" + u2.getMode() + "* mode.");
         	} else {
         		u2.setMode(args[0]);
         		messagePlayer(p2, "You are now in *"+args[0]+"* mode.");
         	}
         	return true;
         }
         //
 		// /tpallow
 		//
         else if (command.getName().equalsIgnoreCase("tpallow")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
 
         	//Check the field exists...
         	if(u2.last_applicant == null) {
         		messagePlayer(p2, "Error: No one has attempted to tp to you lately!");
         		return true;
         	}
         	
         	//Check it hasn't expired
         	Date t = new Date();
     		if(t.getTime() > u2.last_applicant_time + 1000L*config.ASK_EXPIRE) {
     			messagePlayer(p2, "Error: /tp request has expired!");
     			return true;
     		}
             
         	
         	Player p1 = getServer().getPlayer(u2.last_applicant);
         	
         	if(p1 == null) {
         		messagePlayer(p2, "Error: "+u2.last_applicant+" is no longer online.");
         		return true;
         	}
         	
         	u2.last_applicant = null;
         	messagePlayer(p1, "Teleporting you to " + p2.getName() + ".");
         	messagePlayer(p2, "Teleporting " + p1.getName() + " to you.");
         	p1.teleport(p2);
         	
         	
         	return true;
         }
         //
 		// /tpdeny
 		//
         else if (command.getName().equalsIgnoreCase("tpdeny")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
         	
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
 
         	if(u2.last_applicant == null) {
         		messagePlayer(p2, "Error: No one has attempted to tp to you lately!");
         		return true;
         	}
         	
         	
         	messagePlayer(p2, "Denied a request from "+u2.last_applicant+".");
         	messagePlayer(p2, "Use '/tpblock "+u2.last_applicant+"' to block further requests");
         	u2.last_applicant = null;
         	
         	return true;
         }
         //
 		// /tpfriend <player>
 		//
         else if (command.getName().equalsIgnoreCase("tpfriend")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
         	if(args.length != 1) {
        		messagePlayer(p2, "Usage: /tpfriend <player>");
         		return true;
         	}
         	if(u2.addFriend(args[0])) {
         		messagePlayer(p2, args[0] + " added as a friend.");
         	} else {
         		messagePlayer(p2, "Error: " + args[0] + " is already a friend.");
         	}
         	return true;
         }
         //
 		// /tpunfriend <player>
 		//
         else if (command.getName().equalsIgnoreCase("tpunfriend")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
 
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
         	if(args.length != 1) {
         		messagePlayer(p2, "Usage: /tpunfriend <player>");
         		return true;
         	}
         	if(u2.delFriend(args[0])) {
         		messagePlayer(p2, args[0] + " removed from friends.");
         	} else {
         		messagePlayer(p2, "Error: " + args[0] + " not on friends list.");
         	}
         	return true;
         }
         //
 		// /tpblock <player>
 		//
         else if (command.getName().equalsIgnoreCase("tpblock")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
         	if(args.length != 1) {
         		messagePlayer(p2, "Usage: /tpblock <player>");
         		return true;
         	}
         	if(u2.addBlocked(args[0])) {
         		messagePlayer(p2, args[0] + " was blocked from teleporting to you.");
         	} else {
         		messagePlayer(p2, "Error: " + args[0] + " is already blocked.");
         	}
         	return true;
         }
         //
 		// /tpunblock <player>
 		//
         else if (command.getName().equalsIgnoreCase("tpunblock")) {
         	if (!(sender instanceof Player)) {
         		sender.sendMessage("This cannot be run from the console!");
         		return true;
         	}
 
         	Player p2 = (Player)sender;
         	
         	if(!canTP(p2)) {
         		messagePlayer(p2, "You do not have permission.");
         		return true;
         	}
         	
         	User u2 = getUser(p2);
         	if(args.length != 1) {
         		messagePlayer(p2, "Usage: /tpunblock <player>");
         		return true;
         	}
         	if(u2.delBlocked(args[0])) {
         		messagePlayer(p2, args[0] + " was unblocked from teleporting to you.");
         	} else {
         		messagePlayer(p2, "Error: " + args[0] + " is not blocked.");
         	}
         	return true;
         }
         return false;
 	}
 	
 	//Pull a user from the cache, or create it if necessary
 	private User getUser(Player p) {
 		User u = user_cache.get(p.getName().toLowerCase());
 		if(u == null) {
 			u = new User(this, p);
 			user_cache.put(p.getName().toLowerCase(), u);
 		}
 		return u;
 	}
 	
 	//Checks if p1 can override p2
 	private boolean canOverride(Player p1, Player p2) {
 		for(int j = config.GROUPS.size() - 1; j >= 0; j--){
 			String g = config.GROUPS.get(j);
 			if(p2.hasPermission("tpcontrol.level."+g)) {
 				return false;
 			}
 			else if (p1.hasPermission("tpcontrol.level."+g)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean canTP(Player p1) {
 		for(String g : config.GROUPS) {
 			if(g.equals(config.MIN_GROUP)) {
 				return true;
 			}
 			else if(p1.hasPermission("tpcontrol.level."+g)) {
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	public void messagePlayer(Player p, String m) {
 		p.sendMessage(ChatColor.GRAY + "[TP] " + ChatColor.WHITE + m);
 	}
 }
