 
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
 
         if (split[0].equalsIgnoreCase("/sback") && player.canUseCommand("/sback")) {
             if (locations.popLocation(player))
                 updateWarpState(player);
             else
                 player.sendMessage(Colors.Rose + "No previous warps.");
             return true;
         }
 
         if (split[0].equalsIgnoreCase("/smove") && player.canUseCommand("/smove")) {
             if (split.length < 2) {
                 player.sendMessage(Colors.Rose + "Correct usage is: /smove +N or -N");
                 return true;
             }
 
             String param = split[1];
             if (param.charAt(0) == '+' || param.charAt(0) == '-') {
                 try {
                     int rotateBy;
 
                     if (param.length() == 1) 
                         rotateBy = 1;
                     else
                         rotateBy = Integer.parseInt(param.substring(1));
 
                     locations.rotate(player, rotateBy);
                     updateWarpState(player);
                     return true;
                 } catch (NumberFormatException e) { }
             }
         }
 
         return false;
     }
 }
