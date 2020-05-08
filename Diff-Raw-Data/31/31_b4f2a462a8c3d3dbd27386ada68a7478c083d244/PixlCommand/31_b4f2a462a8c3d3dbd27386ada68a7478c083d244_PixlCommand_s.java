 package com.hackhalo2.creative;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PixlCommand implements CommandExecutor {
     private final String[] wool = { "white", "orange", "magenta", "lightblue", "yellow", "lightgreen", "pink",
 	    "gray", "lightgray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
     private final String name = ChatColor.AQUA+"P"+ChatColor.DARK_AQUA+"i"+ChatColor.BLUE+"x"+ChatColor.DARK_BLUE+"l";
     boolean isPlayer = false;
     boolean isPlayerAdmin = false;
     private final Pixl plugin;
 
     public PixlCommand(Pixl p) { this.plugin = p; }
 
    @Override
     public boolean onCommand(CommandSender cs, Command c, String l, String[] args) {
 	if(cs instanceof Player) { //check to see if the commandsender is a player and has the correct permissions
 	    if(plugin.checkPermissions((Player)(cs), "pixl.use", true)) {
 		if(plugin.checkPermissions((Player)(cs), "pixl.admin", true)) { isPlayerAdmin = true; }
 		isPlayer = true;
 	    } else {
 		cs.sendMessage(ChatColor.RED+"You don't have permission to use Pixl");
 		return false;
 	    }
 	}
 	if(args.length == 0) {
 	    if(isPlayer) {
 		if(plugin.breakMode((Player)(cs))) { plugin.setBreak((Player)(cs), false); }
 		plugin.setToggle((Player)(cs), (plugin.isToggled((Player)(cs)) ? false : true));
 		cs.sendMessage(ChatColor.AQUA+"Pixl "+(plugin.isToggled((Player)(cs)) ? "Enabled" : "Disabled"));
 	    }
 	} else if(args.length == 1) {
 	    if(args[0].equalsIgnoreCase("version")) {
 		if(isPlayer) {
 		    if(isPlayerAdmin) {
 			cs.sendMessage(ChatColor.WHITE+"com.hackhalo2.creative."+name);
 			cs.sendMessage(ChatColor.AQUA+"Version: "+plugin.version);
 			cs.sendMessage(ChatColor.GOLD+"Permissions "+(plugin.permissionsEnabled ? ChatColor.GREEN+"Enabled ("+plugin.permissionsType+")" : ChatColor.RED+"Disabled"));
 			//cs.sendMessage(ChatColor.GOLD+"Advanced Help "+(plugin.helpEnabled ? ChatColor.GREEN+"Enabled" : ChatColor.RED+"Disabled"));
 		    }
 		} else {
 		    cs.sendMessage("com.hackhalo2.creative.Pixl");
 		    cs.sendMessage("Version: "+plugin.version);
 		    cs.sendMessage("Permissions "+(plugin.permissionsEnabled ? "Enabled ("+plugin.permissionsType+")" : "Disabled"));
 		    //cs.sendMessage("Advanced Help "+(plugin.helpEnabled ? "Enabled" : "Disabled"));
 		}
 	    } else if(args[0].equalsIgnoreCase("help")) {
 		if(isPlayer) {
 		    /*if(plugin.helpEnabled) {
 		    cs.sendMessage(ChatColor.AQUA+"Please use /help Pixl for commands");
 		} else {*/
 		    cs.sendMessage(name+ChatColor.AQUA+" Version "+plugin.version);
 		    cs.sendMessage(ChatColor.AQUA+"/pixl | "+ChatColor.DARK_AQUA+"Toggles Pixl on/off");
 		    cs.sendMessage(ChatColor.AQUA+"/pixl set <value> | "+ChatColor.DARK_AQUA+"Set wool color to byte <value>");
 		    cs.sendMessage(ChatColor.AQUA+"/pixl clear | "+ChatColor.DARK_AQUA+"Clears the value set by /pixl set");
 		    cs.sendMessage(ChatColor.AQUA+"/pixl help | "+ChatColor.DARK_AQUA+"Displays this menu");
 		    if(isPlayerAdmin) { cs.sendMessage(ChatColor.AQUA+"/pixl version | "+ChatColor.DARK_AQUA+"Detailed version info"); }
 		    if(isPlayerAdmin) { cs.sendMessage(ChatColor.AQUA+"/pixl break | "+ChatColor.DARK_AQUA+"Toggles PixlBreak on/off"); }
 		    //}
 		} else {
 		    cs.sendMessage("pixl version | Detailed version info");
 		    cs.sendMessage("More console commands comming soon!");
 		}
 	    } else if(args[0].equalsIgnoreCase("break")) {
 		//this is currently very fucking dangerous
 		if(isPlayerAdmin) {
 		    if(plugin.isToggled((Player)(cs))) { plugin.setToggle((Player)(cs), false); }
 		    plugin.setBreak((Player)(cs), (plugin.breakMode((Player)(cs)) ? false : true));
 		    cs.sendMessage(ChatColor.AQUA+"Pixl Break Mode"+(plugin.breakMode((Player)(cs)) ? "Enabled" : "Disabled"));
 		} else {
 		    cs.sendMessage(ChatColor.RED+"You don't have permission to use this command");
 		}
 	    } else if(args[0].equalsIgnoreCase("clear")) {
 		if(isPlayer) {
 		    if(plugin.isSet((Player)(cs)) == null) { return true; }
 		    else { plugin.removeValue((Player)(cs)); }
 		}
 	    }
 	} else if(args.length == 2) {
 	    if(args[0].equalsIgnoreCase("set")) {
 		try {
 		    int i = Integer.parseInt(args[1].trim());
 		    if(i > 16) { i = 15; } if(i < 0) { i = 0; }
 		    plugin.setValue((Player)(cs), i);
 		    cs.sendMessage(ChatColor.AQUA+"Hard value set to "+wool[i]);
 		} catch(NumberFormatException e) {
 		    for(int i=0; i < wool.length; i++) {
 			if(args[1].equalsIgnoreCase(wool[i])) {
 			    plugin.setValue((Player)(cs), i);
 			    cs.sendMessage(ChatColor.AQUA+"Hard value set to "+wool[i]);
 			    return true;
 			}
 		    }
 		    cs.sendMessage(ChatColor.RED+"Error with Parsing!");
 		    cs.sendMessage(ChatColor.RED+"Please use numbers or the name of the color.");
 		    cs.sendMessage(ChatColor.RED+"Examples: 14 (for red), magenta or lightblue");
 		}
 	    }
 	}
 	return true;
     }
 }
