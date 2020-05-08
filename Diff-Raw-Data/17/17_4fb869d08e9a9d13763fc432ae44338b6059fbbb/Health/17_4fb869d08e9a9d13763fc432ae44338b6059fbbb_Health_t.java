 package com.fsscripts.bentzilla;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Health extends JavaPlugin {
     
 	private String HVersion = "1.0";
 	
     public final static HashMap<Player, String> chatHealthUsers = new HashMap<Player,String>(); 
     private final HPlayerChatListener ThePlayerListener = new HPlayerChatListener(this);
     //private final HEntityListener TheEntityListener = new HEntityListener(this);
     private Permissions HPermissions = null;
     
 	public void onDisable() {
 		System.out.println("HealthPlugin v" + HVersion + " Disabled");
 	}
 
 	public void onEnable() {
 		setupPermissions();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.ThePlayerListener, Event.Priority.Normal, this);
 		//pm.registerEvent(Event.Type.ENTITY_DAMAGED, this.TheEntityListener, Event.Priority.Highest, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 	}
 	
     private void setupPermissions() {
         Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
         PluginDescriptionFile pdfFile = this.getDescription();
 
         if (permissionsPlugin != null) {
            System.out.println(pdfFile.getName() + ": Using Nijikokun's permissions plugin for permissions");
             this.HPermissions = ((Permissions) permissionsPlugin);
         }
     }
     
     public Permissions getPermissions() {
         return this.HPermissions;
     }
 
 	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 			
 		 if (HPermissions == null) {
              sender.sendMessage("Cannot find Permissions plugin.");
              return false;
          }
 		 
 		 if (!(sender instanceof Player)) {
 				return false;
 		 }
 			
 		String command = "/" + commandLabel + " " + join(args, 0);
 		
 		System.out.println("Command = " + command);
     	System.out.println("chathealth, there are current: " + args.length + " args");
     	System.out.println(command.toLowerCase());
 		
 		if (!command.toLowerCase().contains("/hp ")) {
             return false;
         }
 
         if (args.length < 1) {
             return false;
         }
 
         Player user = (Player) sender;
         String subCommand = args[0].toLowerCase();
         System.out.println("Sibcom: " + subCommand);
 
        boolean hasPermission = false;
 
         if (subCommand.equals("chathealth")) {
        	
        	hasPermission = this.HPermissions == null ? false : Permissions.Security.permission(user, "hp.chathealth");	

            if (!hasPermission) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }
            
         	System.out.println("chathealth, there are current: " + args.length + " args");
         	String rpgstyle = "off";
             if (args.length == 2){
             	if (args[1].toLowerCase().equals("on")){
                 	rpgstyle = args[1].toLowerCase();
             	}else if(args[1].toLowerCase().equals("off")){
                 	rpgstyle = args[1].toLowerCase();
             	}else{
             		user.sendMessage("Example Usage: /hp chathealth on");
             	}
             }else if (args.length == 1){
             	toggleChatHealth(user, rpgstyle);
             	return true;
             }
         	if (!hpChatEnabled(user)){
         		toggleChatHealth(user, rpgstyle);
         	}
         	if (!chatHealthUsers.values().toString().contains(rpgstyle)){
         		Health.chatHealthUsers.remove(user);
         		Health.chatHealthUsers.put(user, rpgstyle);
         	}
 
         	return true;
         }
         return false;
 	 }
 	   
 	    public static boolean hpChatEnabled(Player player) {
 			return chatHealthUsers.containsKey(player);
 		}
 
 	    public boolean toggleChatHealth(Player player, String style){
 	    	if(hpChatEnabled(player)) {
 	    		Health.chatHealthUsers.remove(player);
 	    		player.sendMessage("Health in Chat DISABLED");
 	    	} else {
 	    		Health.chatHealthUsers.put(player, style);
 	    		player.sendMessage("Health in Chat ENABLED");
 	    	}		
 	    	return true;
 	    }
     
     public static String join(String[] arr, int offset) {
 		return join(arr, offset, " ");
 	}
 
 	/**
 	 * Join an array command into a String
 	 * @author Hidendra
 	 * @param arr
 	 * @param offset
 	 * @param delim
 	 * @return
 	 */
 	public static String join(String[] arr, int offset, String delim) {
 		String str = "";
 
 		if (arr == null || arr.length == 0) {
 			return str;
 		}
 
 		for (int i = offset; i < arr.length; i++) {
 			str += arr[i] + delim;
 		}
 
 		return str.trim();
 	}
 }
