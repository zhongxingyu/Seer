 package de.splashfish.flinky;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Flinky extends JavaPlugin {
 	
	public 	static 	String				version		= "2.18";
 	
 	private static	Logger 				pstream;
 	private	static	ConfigHandler		ch;
 	private			PluginManager		pm;
 	//private			FreezeNatureHandler fnh;
 	private			UpdateManager		um;
 	private			ChatListener		cl;
 	
 	public static void print(String arg) {
 		pstream.info(arg);
 	}
 	
 	public static void err(String arg) {
 		pstream.warning(arg);
 	}
 	
 	public static String globalVar(String key) {
 		return ch.getValue(key);
 	}
 	
 	@Override public void onEnable() {
 		//create DataFolder [ ..plugins/Flinky/ ] if not existing yet
 		if(!this.getDataFolder().exists()) {
 			this.getDataFolder().mkdir();
 		}
 		
 		//init pstream (caused by static reference)
 		pstream = this.getLogger();
 		
 		//init pluginmanager
 		pm = this.getServer().getPluginManager();
 		
 		//init confighandler
 		ch = new ConfigHandler(this.getDataFolder());
 		
 		//init tickethandler
 		th = new TicketHandler(this.getLogger(), this.getDataFolder());
 		
 		//init chatlistener
 		cl = new ChatListener(this.getDataFolder(), th);
 		
 		//register the chatlistener
 		pm.registerEvents(cl, this);
 		
 		//init naturehandler
 		//fnh = new FreezeNatureHandler(pm, this);
 		
 		//init updater
 		um = new UpdateManager(this.getDataFolder());
 		
 		//init cmds
 		cmds = new Commands(this, th, um, cl);
 		
 		print("SplashFish was here :D");
 	}
 	 
 	@Override public void onDisable() { 
 		this.th.disable();
 		//this.fnh.disable();
 	}
 	
 	private Commands cmds;
 	private TicketHandler th;
 	@Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("flinky")) {
 			return cmds.flinky(sender, label, args);
 		} else if(cmd.getName().equalsIgnoreCase("ticket")) {
 			return cmds.ticket(player, sender, label, args);
 		} else if(cmd.getName().equalsIgnoreCase("fban")) {
 			return cmds.ban(player, sender, label, args);
 		} else if(cmd.getName().equalsIgnoreCase("funban")) {
 			return cmds.unban(player, sender, label, args);
 		//} else if (cmd.getName().equalsIgnoreCase("freezetime") || cmd.getName().equalsIgnoreCase("foreversun")) {
 		//	return fnh.handleCommands(sender, cmd, label, args);
 		} else if(cmd.getName().equalsIgnoreCase("fkick")) {
 			return cmds.kick(player, sender, label, args);
 		} else {
 			return false;
 		}
 	}
 	
 }
