 package tzer0.Money2XP;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import tzer0.Money2XP.Money2XP.TrainingArea;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The is a listener interface for receiving PlayerCommandPreprocessEvent events.
  * 
  */
 public class Money2XPPlayerListener extends PlayerListener  {
     Money2XP plugin;
     HashMap<Player, TrainingArea> activity;
 
     public Money2XPPlayerListener (Money2XP plugin) {        
         this.plugin = plugin;
         activity = new HashMap<Player, TrainingArea>();
     }
     public void startTask() {
         plugin.getServer().getScheduler().cancelTasks(plugin);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new PositionChecker(), 0L, 30L);
     }
     /**
      * Sets the pointers so that they can be referenced later in the code
      *
      * @param config Plugin-config
      * @param plugin Money2XP
      * @param permissions Permissions-handler (if available)
      */
     public void onPlayerJoin(PlayerJoinEvent event) {
         activity.remove(event.getPlayer());
     }
     public void onPlayerQuit(PlayerQuitEvent event) {
         activity.remove(event.getPlayer().getName());
     }
     /**
      * Checks where players are located and updates their status accordingly (and what skills they may train) 
      *
      *
      */
     public class PositionChecker implements Runnable {
         public void run() {
             if (!plugin.trainingzones) {
                 return;
             }
             for (Player player : plugin.getServer().getOnlinePlayers()) {
                 boolean inarea = false;
                 if (!activity.containsKey(player)) {
                     activity.put(player, null);
                 }
                 TrainingArea area = activity.get(player);
                 for (TrainingArea test : plugin.areas) {
                     if (test.isInArea(player.getLocation())) {
                         inarea = true;
                         if (!(area == test)) {
                             if (test.skills.size() != 0) {
                                 player.sendMessage(ChatColor.GREEN + "You feel the presence of a training ground.");
                                 player.sendMessage(ChatColor.GREEN + "You may train the following skills here:");
                                 for (Integer i : test.skills) {
                                     player.sendMessage(ChatColor.GREEN + plugin.names[i]);
                                 }
                             } else {
                                 player.sendMessage(ChatColor.YELLOW + "You feel the presense of an unused training ground");
                             }
                             activity.put(player, test);
                             break;
                         }
                     }
                 }
                 if (area != null && !inarea) {
                     activity.put(player, null);
                     player.sendMessage(ChatColor.YELLOW + "You have left a training ground.");
                 }
             }
         }
     }
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
         Player pl = event.getPlayer();

         if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST) {
             Sign sign = (Sign) event.getClickedBlock().getState();
             String lines[] = sign.getLines();
             if (lines[0].equalsIgnoreCase(ChatColor.DARK_GREEN + "[m2x]")) {
                if ((plugin.permissions != null && !plugin.permissions.has(pl, "money2xp.player"))) {
                    pl.sendMessage(ChatColor.RED + "You do not have permissions to do this.");
                    return;
                }
                 if (plugin.updateAndCheckSign(sign, false, pl)) {
                    plugin.xpMod(lines[1], lines[2], pl, false, true, true);
                 }
             }
         }
     }
 }
