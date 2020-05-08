 
 import java.util.HashMap;
 
 public class WarpStackListener extends PluginListener {
 
     private LocationDataStore locations = new LocationDataStore();
 
     private void updateWarpState(Player player) {
         player.teleportTo(locations.getLocation(player));
         player.sendMessage(Colors.Rose + "Woosh!");
     }
 
     public boolean onCommand(Player player, String[] split) {
         if (split[0].equalsIgnoreCase("/swarp") && player.canUseCommand("/warp") && player.canUseCommand("/swarp")) {
             if (split.length < 2) {
                 player.sendMessage(Colors.Rose + "Correct usage is: /swarp [warpname]");
                 return true;
             }
 
             Warp warp = etc.getDataSource().getWarp(split[1]);
             if (warp == null || (!player.isInGroup(warp.Group) && !warp.Group.equals(""))) {
                 player.sendMessage(Colors.Rose + "Warp not found.");
                 return true;
             }
 
             if (locations.pushLocation(player, warp.Location))
                 updateWarpState(player);
             else
                 player.sendMessage(Colors.Rose + "You are at your maximum for warp slots.");
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/sspawn") && player.canUseCommand("/spawn") && player.canUseCommand("/sspawn")) {
             if (locations.pushLocation(player, etc.getServer().getSpawnLocation()))
                 updateWarpState(player);
 
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/shome") && player.canUseCommand("/home") && player.canUseCommand("/shome")) {
             Warp home = null;
             if (split.length > 1 && player.isAdmin()) {
                 home = etc.getDataSource().getHome(split[1]);
             } else {
                 home = etc.getDataSource().getHome(player.getName());
             }
 
             if (home != null) {
                 if (locations.pushLocation(player, home.Location))
                     updateWarpState(player);
             } else if (split.length > 1 && player.isAdmin()) {
                 player.sendMessage(Colors.Rose + "That player home does not exist");
             } else {
                 if (locations.pushLocation(player, etc.getServer().getSpawnLocation()))
                     updateWarpState(player);
             }
 
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/stp") && player.canUseCommand("/tp") && player.canUseCommand("/stp")) {
             if (split.length < 2) {
                 player.sendMessage(Colors.Rose + "Correct usage is: /stp [player]");
                 return true;
             }
 
             Player other = etc.getServer().matchPlayer(split[1]);
 
             if (other != null) {
                 if (player.getName().equalsIgnoreCase(other.getName())) {
                     player.sendMessage(Colors.Rose + "You're already here!");
                     return true;
                 }
 
                 if (locations.pushLocation(player, other.getLocation()))
                     updateWarpState(player);
 
             } else
                 player.sendMessage(Colors.Rose + "Can't find user " + split[1] + ".");
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/sback") && player.canUseCommand("/sback")) {
             if (locations.popLocation(player))
                 updateWarpState(player);
             else
                 player.sendMessage(Colors.Rose + "No previous warps.");
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/smove") && player.canUseCommand("/smove")) {
            int rotateBy = 1;
 
            if (split.length > 1)
                 try {
                    rotateBy = Integer.parseInt(split[1]);
                 } catch (NumberFormatException e) { }

            locations.rotate(player, rotateBy);
            updateWarpState(player);
            return true;
         }
 
         return false;
     }
 }
