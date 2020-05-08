 package me.slaps.DMWrapper;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class DMWrapperPlayerListener extends PlayerListener {
 	
 	protected HashMap<String, Boolean> inShopMap = new HashMap<String, Boolean>();
 
 	public DMWrapperPlayerListener(DMWrapper plug) {
 	    plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Monitor, plug);
 	    if ( DMWrapper.debugMode )
 	        plug.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Priority.Highest, plug);
 	}
 
     @Override
     public void onPlayerCommandPreprocess ( PlayerChatEvent event ) {
         if (DMWrapper.debugMode)
             DMWrapper.info("DMWrapperPlayerListener.onPlayerCommandPreprocess(): Player: " + 
                            event.getPlayer().getName() + " msg: " + event.getMessage() + 
                            " Canceled? " + ( event.isCancelled()? "Yes": "No" ) );
       
     }	
 	
 	@Override
 	public void onPlayerMove(PlayerMoveEvent event) {
         if ( !DMWrapper.locMgr.shopLocationsEnabled ) return;
 	    
 	    String playerName = event.getPlayer().getName();
 	    
 	    if (!inShopMap.containsKey(playerName)) inShopMap.put(playerName,false);
 	    
 		Location to = event.getTo();
 		Location blockTo = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ() );
 		
 		if ( DMWrapper.locMgr.inShopLoc(blockTo) ) {
 		    if ( !inShopMap.get(playerName) ) {
 		        event.getPlayer().sendMessage("Entered the shopping area.");
 		        inShopMap.put(playerName, true);
 		    }
 		} else {
 		    if ( inShopMap.get(playerName) ) {
                 event.getPlayer().sendMessage("Leaving the shopping area.");
                 inShopMap.put(playerName, false);
 		    }
 		}
 	}	
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         // debug output
         String debug = "DMWrapper.onCommand(): " + ((sender instanceof Player) ? "Player " + ((Player) sender).getName() : "Console") + " Command " + cmd.getName() + " args: ";
         for (int i = 0; i < args.length; i++) 
             debug += args[i] + " ";
         DMWrapper.info(debug);
 
         // location-based features only available to Players
         if (!(sender instanceof Player))
             return DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);
 
         // only intercept shop command
         if (cmd.getName().toLowerCase().equals("shop") || cmd.getName().toLowerCase().equals("dshop")) {
             if (DMWrapper.debugMode) DMWrapper.info("Is a /shop or /dshop command");
 
             // pass commands to DynamicMarket or intercept
 
             // just '/shop'
             if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
                 if (DMWrapper.debugMode) DMWrapper.info("command: /shop help");
                 DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);
                 sender.sendMessage("/shop location");
                 return true;
 
             // is location based shopping enabled?
             } else if (!DMWrapper.locMgr.shopLocationsEnabled && (args.length > 0) && (!args[0].equalsIgnoreCase("location"))) {
                 if (DMWrapper.debugMode) DMWrapper.info("locations disabled and not a /shop location command");
                 return DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);
 
             // locations enabled, intercept commands
             } else {
                 // not a '/shop location' command
                 if (!args[0].equalsIgnoreCase("location")) {
                     if (DMWrapper.debugMode) DMWrapper.info("not a /shop location command");
 
                     // in a shop location? or admin?
                     if (DMWrapper.hasPermission(sender, "admin") || DMWrapper.locMgr.inShopLoc(((Player) sender).getLocation())) {
                         return DMWrapper.dm.wrapperCommand(sender, cmd.getName(), args);
                     } else {
                         sender.sendMessage("Not in the shopping area!");
                         return true;
                     }
 
                     // a '/shop location' command
                 } else {
                     if (DMWrapper.debugMode) DMWrapper.info("a /shop location command");
 
                     if (!DMWrapper.hasPermission(sender, "location")) {
                         sender.sendMessage("Not allowed to use the /shop location command!");
                         return true;
                     }
 
                     String pname = ((Player) sender).getName();
 
                     if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
                         sender.sendMessage("please right click the 1st corner");
                         DMWrapper.cmdMap.put(pname, "location set");
                         return true;
                     } else if (args.length == 2 && args[1].equalsIgnoreCase("cancel")) {
                         if (DMWrapper.cmdMap.get(pname) == null) {
                             sender.sendMessage("No operation to cancel");
                         } else {
                             sender.sendMessage("Operation canceled");
                             DMWrapper.cmdMap.remove(pname);
                         }
                         return true;
                     } else if (args.length == 2 && args[1].equalsIgnoreCase("check")) {
                         Integer id = DMWrapper.locMgr.getShopID(((Player) sender).getLocation());
                         if (id > 0) {
                             sender.sendMessage("Shop ID: " + id);
                         } else {
                             sender.sendMessage("No shop location found here");
                         }
                         return true;
                     } else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
                         if (DMWrapper.locMgr.removeShopByID(Integer.parseInt(args[2]))) {
                             sender.sendMessage("Shop Location removed");
                         } else {
                             sender.sendMessage("Could not remove Shop ID: " + Integer.parseInt(args[2]));
                         }
                         return true;
                     } else if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
                         sender.sendMessage("Shop Locations enabled");
                         DMWrapper.locMgr.enableShopLocations();
                         return true;
                     } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
                         sender.sendMessage("Shop Locations disabled");
                         DMWrapper.locMgr.disableShopLocations();
                         return true;
                     } else if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                         sender.sendMessage("Shop IDs: " + DMWrapper.locMgr.listShops());
                         return true;
                     } else if (args.length == 3 && args[1].equalsIgnoreCase("tp")) {
                         if (sender instanceof Player) {
                             Location dest = DMWrapper.locMgr.getCenterOfShop(Integer.parseInt(args[2]));
                             if (dest == null) {
                                 sender.sendMessage("Could not find shop.");
                             } else {
                                 ((Player) sender).teleportTo(dest);
                             }
                         }
                         return true;
                     } else {
                         // send player message on how to use /shop loc
                         sender.sendMessage("Usage: /shop location set - starts the shop setup process");
                         sender.sendMessage("         /shop location cancel - cancels setting a shop location");
                         sender.sendMessage("         /shop location check - checks ID of current location");
                         sender.sendMessage("         /shop location remove <ID> - removes the shop location");
                         sender.sendMessage("         /shop location enable - enables location based shops");
                         sender.sendMessage("         /shop location disable - disable location based shops");
                         sender.sendMessage("         /shop location list - lists shop IDs");
                         sender.sendMessage("         /shop location tp <ID> - teleports to shop");
                         return true;
                     }
 
                 }
             }
 
         } else {
             return false;
         }
 
     }
     
 
 }
