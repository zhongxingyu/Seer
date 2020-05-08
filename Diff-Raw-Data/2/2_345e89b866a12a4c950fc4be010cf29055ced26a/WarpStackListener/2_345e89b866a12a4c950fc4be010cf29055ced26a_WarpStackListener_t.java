 
 import java.util.HashMap;
 import java.util.ArrayList;
 
 public class WarpStackListener extends PluginListener {
 
     private LocationDataStore locations = new LocationDataStore();
 
     private void updateWarpState(Player player) {
         player.teleportTo(locations.getLocation(player));
         player.sendMessage(Colors.Rose + "Woosh!");
     }
 
     private void noMoreSlots(Player player) {
         player.sendMessage(Colors.Rose + "You are at your maximum for warp slots.");
     }
 
     public Location getSpawn() {
         PropertiesFile props = new PropertiesFile("server.properties");
         if (props.keyExists("exact-spawn")) {
             String[] data = props.getString("exact-spawn").split(",");
             Location loc = new Location();
             loc.x = Double.parseDouble(data[0]);
             loc.y = Double.parseDouble(data[1]);
             loc.z = Double.parseDouble(data[2]);
             loc.rotX = Float.parseFloat(data[3]);
             loc.rotY = Float.parseFloat(data[4]);
             return loc;
         }
         return etc.getServer().getSpawnLocation();
     }
 
     public boolean pushWarpSpawn(Player player) {
         if (locations.pushLocation(player, getSpawn(), "spawn")) {
             updateWarpState(player);
             return true;
         } else {
             noMoreSlots(player);
             return false;
         }
     }
 
     public boolean pushWarp(Player player, Warp warp) {
         Location loc = new Location(warp.Location.x, warp.Location.y, warp.Location.z,
                                     warp.Location.rotX, warp.Location.rotY);
         if (locations.pushLocation(player, loc, warp.Name)) {
             updateWarpState(player);
             return true;
         } else {
             noMoreSlots(player);
             return false;
         }
     }
 
     public boolean pushWarp(Player player, Player warp) {
         if (locations.pushLocation(player, warp.getLocation(), "tp:" + warp.getName())) {
             updateWarpState(player);
             return true;
         } else {
             noMoreSlots(player);
             return false;
         }
     }
 
     public boolean popWarp(Player player) {
         if (locations.popLocation(player)) {
             updateWarpState(player);
             return true;
         } else {
             player.sendMessage(Colors.Rose + "No previous warps.");
             return false;
         }
     }
 
     public void rotateWarps(Player player, int rotateBy) {
         locations.rotate(player, rotateBy);
         updateWarpState(player);
     }
 
     public boolean hasCommand(Player player, String message, String command) {
         if (message.equalsIgnoreCase("/s" + command))
             return player.canUseCommand("/s" + command);
         return WarpStack.override && message.equalsIgnoreCase("/" + command)
             && player.canUseCommand("/" + command)
             && player.canUseCommand("/s" + command);
     }
 
     public boolean onCommand(Player player, String[] split) {
         String command = split[0];
         if (hasCommand(player, command, "warp")) {
             if (split.length < 2) {
                 player.sendMessage(Colors.Rose + "Correct usage is: "+command+" [warpname]");
                 return true;
             }
 
             Warp warp = etc.getDataSource().getWarp(split[1]);
 
             Player toWarp = player;
             if (split.length == 3 && player.canIgnoreRestrictions())
                 toWarp = etc.getServer().matchPlayer(split[2]);
             if (toWarp == null)
                 player.sendMessage(Colors.Rose + "Player not found.");
 
             if (warp == null || (!player.isInGroup(warp.Group) && !warp.Group.equals(""))) {
                 player.sendMessage(Colors.Rose + "Warp not found.");
                 return true;
             }
 
             pushWarp(toWarp, warp);
             return true;
         }
 
         if (hasCommand(player, command, "spawn")) {
             pushWarpSpawn(player);
             return true;
         }
 
         if (hasCommand(player, command, "home")) {
             Warp home = null;
             if (split.length > 1 && player.isAdmin()) {
                 home = etc.getDataSource().getHome(split[1]);
             } else {
                 home = etc.getDataSource().getHome(player.getName());
             }
 
             if (home != null)
                 pushWarp(player, home);
             else if (split.length > 1 && player.isAdmin())
                 player.sendMessage(Colors.Rose + "That player home does not exist");
             else
                 pushWarpSpawn(player);
 
             return true;
         }
 
         if (hasCommand(player, command, "tp")) {
             if (split.length < 2) {
                 player.sendMessage(Colors.Rose + "Correct usage is: "+command+" [player]");
                 return true;
             }
 
             Player other = etc.getServer().matchPlayer(split[1]);
 
             if (other != null) {
                 if (player.getName().equalsIgnoreCase(other.getName())) {
                     player.sendMessage(Colors.Rose + "You're already here!");
                     return true;
                 }
 
                 pushWarp(player, other);
             } else
                 player.sendMessage(Colors.Rose + "Can't find user " + split[1] + ".");
             return true;
         }
 
         if (hasCommand(player, command, "tphere")) {
             if (split.length < 2) {
                 player.sendMessage(Colors.Rose + "Correct usage is: "+command+" [player]");
                 return true;
             }
 
             Player other = etc.getServer().matchPlayer(split[1]);
 
             if (other != null) {
                 if (player.getName().equalsIgnoreCase(other.getName())) {
                     player.sendMessage(Colors.Rose + "Wow look at that! You teleported yourself to yourself!");
                     return true;
                 }
 
                 pushWarp(other, player);
             } else
                 player.sendMessage(Colors.Rose + "Can't find user " + split[1] + ".");
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/sback") && player.canUseCommand("/sback")) {
             popWarp(player);
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/swlist") && player.canUseCommand("/swlist")) {
             StringBuffer message = new StringBuffer(Colors.Rose + "Your stack: " + Colors.LightBlue); 
            int size = locations.getSize(player), active = locations.getActive(player);
             for (int i = 0; i < size; i ++) {
                 if (i == active) {
                     message.append(Colors.Yellow + locations.getName(player, i));
                     if (i < size-1) message.append("  " + Colors.LightBlue);
                 } else {
                     message.append(locations.getName(player, i));
                     if (i < size-1) message.append("  ");
                 }
             }
 
             player.sendMessage(message.toString());
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/smove") && player.canUseCommand("/smove")) {
             int rotateBy = 1;
 
             if (split.length > 1)
                 try {
                     rotateBy = Integer.parseInt(split[1]);
                 } catch (NumberFormatException e) { }
 
             rotateWarps(player, rotateBy);
             return true;
         }
 
         return false;
     }
 }
