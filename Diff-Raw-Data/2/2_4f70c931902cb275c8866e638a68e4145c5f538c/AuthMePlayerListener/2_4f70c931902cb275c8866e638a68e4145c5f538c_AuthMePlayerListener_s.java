 package de.fgtech.pomo4ka.AuthMe.Listener;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.*;
 
 
 import de.fgtech.pomo4ka.AuthMe.AuthMe;
 import de.fgtech.pomo4ka.AuthMe.InventoryCache.InventoryArmour;
 import de.fgtech.pomo4ka.AuthMe.LoginTimeout.LoginTimeout;
 import de.fgtech.pomo4ka.AuthMe.MessageHandler.MessageHandler;
 
 /**
  * Handle events for all player related events
  *
  * @author pomo4ka
  */
 public class AuthMePlayerListener extends PlayerListener {
 
     private final AuthMe plugin;
 
     // private long lastMove;
     public AuthMePlayerListener(AuthMe instance) {
         plugin = instance;
     }
 
     @Override
     public void onPlayerLogin(PlayerLoginEvent event) {
         if(event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
         String playername = player.getName();
 
         // Kicks non registered player before they could join
         if(plugin.settings.KickNonRegistered()) {
             if(!plugin.datacontroller.isPlayerRegistered(playername)) {
                 event.disallow(Result.KICK_OTHER,
                         plugin.messages.getMessage("Kick.NotRegistered"));
             }
         }
 
         // If there is another player logged in with the same name, disallow
         // connection
         if(plugin.playercache.isPlayerAuthenticated(player)) {
             event.disallow(Result.KICK_OTHER,
                     plugin.messages.getMessage("Kick.OtherUserLoggedIn"));
         }
 
         // If the player contains invalid characters, disallow connection
         if((!playername.matches(plugin.settings.PlayerNameRegex()))
            || (playername.length() > plugin.settings.PlayerNameMaxLength())
            || (playername.length() < plugin.settings.PlayerNameMinLength())
            || (playername.equalsIgnoreCase("Player"))) {
             event.disallow(Result.KICK_OTHER, plugin.messages.getMessage(
                     "Kick.DisallowedCharacters", playername));
         }
     }
 
     @Override
     public void onPlayerKick(PlayerKickEvent event) {
         if(event.isCancelled() || event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
 
         // Just prevent kicks, if the player get this message and is logged in
         if(event.getReason().equals("Logged in from another location.")) {
             if(plugin.playercache.isPlayerAuthenticated(player)) {
                 event.setCancelled(true);
             }
         }
     }
 
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         if(event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
 
         // Is player really registered?
         boolean regged = plugin.datacontroller.isPlayerRegistered(
                 player.getName());
 
         // Create PlayerCache
         plugin.playercache.createCache(player, regged, false);
 
         // Check if the player has unrestricted access due a config setting
         if(plugin.checkUnrestrictedAccess(player)) {
             plugin.playercache.setPlayerAuthenticated(player, true);
         }
 
         // Inform the player for the need or possibility of registration
         if(!plugin.playercache.isPlayerRegistered(player)) {
             if(plugin.settings.ForceRegistration()) {
                 player.sendMessage(plugin.messages.getMessage(
                         "JoinMessage.ForceRegistration"));
                 player.sendMessage(plugin.messages.getMessage(
                         "JoinMessage.Command"));
                 return;
             } else {
                 player.sendMessage(plugin.messages.getMessage(
                         "JoinMessage.FreeRegistration"));
                 player.sendMessage(plugin.messages.getMessage(
                         "JoinMessage.Command"));
                 return;
             }
         }
         //plugin.playercache.setPlayerAuthenticated(player, true);
         // --The following section is only executed for registered players!--
 
         // Session Login
         if(plugin.settings.LoginSessionsEnabled()) {
 
             if(plugin.sessionhandler.isSessionValid(player)) {
                 // perform session login
                 plugin.performPlayerLogin(player);
                 player.sendMessage(plugin.messages.getMessage("Sessions.Hint"));
                 MessageHandler.showInfo(
                         "Player " + player.getName()
                         + " was automatically logged in by session.");
                 return;
 
             }
         }
 
         // Make a backup of an inventory if it doesn't exist
         if(!plugin.invcache.doesCacheExist(player.getName())) {
             ItemStack[] inv = player.getInventory().getContents();
             ItemStack[] arm = player.getInventory().getArmorContents();
             InventoryArmour invarm = new InventoryArmour(inv, arm);
 
             plugin.invcache.createCache(player.getName(), invarm);
         }
 
         // if the player is dead, delete all possible inventory backups and
         // teleport him to spawn
         if(player.getHealth() <= 0) {
             plugin.invcache.removeCache(player.getName());
 
             Location spawn = player.getWorld().getSpawnLocation();
 
             player.teleport(spawn);
         }
 
         player.getInventory().clear();
         player.getInventory().setHelmet(null);
         player.getInventory().setChestplate(null);
         player.getInventory().setLeggings(null);
         player.getInventory().setBoots(null);
 
         // Create LoginTimeout Timer
         LoginTimeout.createLoginTimeout(plugin, player);
 
         player.sendMessage(plugin.messages.getMessage("Alert.Login"));
     }
 
     @Override
     public void onPlayerQuit(PlayerQuitEvent event) {
         if(event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
 
         /*
          * if (plugin.registrationlist.isPlayerAuthenticated(player.getName()))
          * { if (plugin.cache.doesCacheExist(player)) {
          * plugin.cache.removeInventoryWearCache(player); } ItemStack[] inv =
          * player.getInventory().getContents(); ItemStack[] arm =
          * player.getInventory().getArmorContents(); InventoryArmour invarm =
          * new InventoryArmour(inv, arm);
          *
          * plugin.cache.createInventoryArmourCache(player, invarm); }
          */
 
         // Set new session
         if(plugin.settings.LoginSessionsEnabled()) {
             if(plugin.playercache.isPlayerAuthenticated(player)) {
                 plugin.sessionhandler.createSession(player);
             }
         }
 
         // Remove PlayerCache
         plugin.playercache.removeCache(player);
 
         // Remove LoginTimeout Timer
         LoginTimeout.removeLoginTimeout(plugin, player);
     }
 
     @Override
     public void onPlayerMove(PlayerMoveEvent event) {
         if(event.isCancelled() || event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
 
         // Disables continuous falling down
         Location fromLoc = event.getFrom();
         Location toLoc = event.getTo();
 
         if(fromLoc.getX() == toLoc.getX() && fromLoc.getZ() == toLoc.getZ()) {
             if(fromLoc.getY() > toLoc.getY()) {
                 return;
             }
         }
 
         if(!plugin.checkAuth(player)) {
 
             if(plugin.settings.WalkAroundSpawnEnabled()) {
                 if(!plugin.playercache.isPlayerRegistered(player)) {
                     Location spawn = player.getWorld().getSpawnLocation();
 
                     int xDiff = spawn.getBlockX()
                                 - player.getLocation().getBlockX();
                     xDiff = Math.abs(xDiff);
                     int zDiff = spawn.getBlockZ()
                                 - player.getLocation().getBlockZ();
                     zDiff = Math.abs(zDiff);
 
                     int maxRadius = plugin.settings.WalkAroundSpawnRadius();
 
                     if(xDiff <= maxRadius && zDiff <= maxRadius) {
                         // allow walk
                     } else {
                         // disallow walk
                         Location newLoc;
                         if(xDiff > (maxRadius + 3) || zDiff > (maxRadius + 3)) {
                             // teleport to spawn
                             newLoc = player.getWorld().getSpawnLocation();
                         } else {
                             // teleport to old location
                             newLoc = event.getFrom();
                         }
                         player.teleport(newLoc.clone());
                         event.setCancelled(true);
                     }
                     return;
                 }
             }
 
             event.setCancelled(true);
             player.teleport(fromLoc);
         }
     }
 
     @Override
     public void onPlayerChat(PlayerChatEvent event) {
         if(event.isCancelled() || event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
 
         if(plugin.settings.AllowUnregisteredChat()) {
             if(!plugin.playercache.isPlayerRegistered(player)) {
                 return;
             }
         }
 
         if(!plugin.checkAuth(player)) {
             event.setMessage("");
             event.setCancelled(true);
         }
     }
 
     @Override
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if(event.isCancelled() || event.getPlayer() == null) {
             return;
         }
 
         Player player = event.getPlayer();
         String commandLabel = event.getMessage().split(" ")[0];
 
         // Allow logged-in players all commands
         if(plugin.checkAuth(player)) {
             return;
         }
 
         // Enable AuthMe integrated commands
         if(commandLabel.equalsIgnoreCase("/register")) {
             return;
         }
 
         if(commandLabel.equalsIgnoreCase("/login")) {
             return;
         }
 
         if(commandLabel.equalsIgnoreCase("/l")) {
             return;
         }
 
         //BukkitContrib fix; Those faggots can not code and should be lined up and shot
        if(event.getMessage().equals("/0.1.3")) {
             return;
         }
 
         // Enable commands specified per config
         List<Object> cmdList = new ArrayList<Object>();
         if(plugin.playercache.isPlayerRegistered(player)) {
             if(!plugin.settings.AllowAllowNonLoggedInCommand().isEmpty()) {
                 cmdList = plugin.settings.AllowAllowNonLoggedInCommand();
             }
         } else {
             if(!plugin.settings.AllowAllowNonRegisteredCommand().isEmpty()) {
                 cmdList = plugin.settings.AllowAllowNonRegisteredCommand();
             }
         }
         if(!cmdList.isEmpty()) {
             for(Object key : cmdList) {
                 String cmd = (String) key;
                 if(commandLabel.startsWith("/" + cmd)) {
                     return;
                 }
             }
         }
 
         // if no command was an exception, block it
         event.setMessage("/notloggedin");
         event.setCancelled(true);
     }
 
     @Override
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if(event.isCancelled() || event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
         if(!plugin.checkAuth(player)) {
             event.setCancelled(true);
         }
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if(event.isCancelled() || event.getPlayer() == null) {
             return;
         }
         Player player = event.getPlayer();
         if(!plugin.checkAuth(player)) {
             event.setCancelled(true);
         }
     }
 }
