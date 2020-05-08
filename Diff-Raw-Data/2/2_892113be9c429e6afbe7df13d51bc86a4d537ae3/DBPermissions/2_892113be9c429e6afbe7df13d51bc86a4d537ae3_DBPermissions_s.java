 package com.minecarts.dbpermissions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.text.MessageFormat;
 
 import com.minecarts.dbquery.DBQuery;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.*;
 
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionAttachment;
 
 
 public class DBPermissions extends org.bukkit.plugin.java.JavaPlugin implements Listener {
     private DBQuery dbq;
 
     protected HashMap<Player,PermissionAttachment> attachments = new HashMap<Player, PermissionAttachment>();
 
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerLogin(PlayerLoginEvent event) {
         registerPlayer(event.getPlayer());
         calculatePermissions(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent event) {
         unregisterPlayer(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerKick(PlayerKickEvent event) {
         unregisterPlayer(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
         calculatePermissions(event.getPlayer(), event.getPlayer().getWorld());
     }
     
     
     @Override
     public void onEnable() {
         dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
 
         
         getCommand("perm").setExecutor(new CommandExecutor() {
             public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                 if(!sender.hasPermission("permission.admin")) return true; // "hide" command output for non-ops
 
                 if(args[0].equalsIgnoreCase("refresh")) {
                     if(args.length == 1){
                         for(Player p : getServer().getOnlinePlayers()){
                             calculatePermissions(p,p.getWorld());
                         }
                         sender.sendMessage(ChatColor.GRAY + "Refreshing permissions for all online players (async).");
                     } else if(args.length == 2){
                         List<Player> players = Bukkit.matchPlayer(args[1]);
                         if(players.size() == 0){
                             sender.sendMessage(ChatColor.GRAY + "No players matched query: " + args[1]);
                             return true;
                         }
                         for(Player p : players){
                             sender.sendMessage(ChatColor.GRAY + "Refreshing permissions for " + p.getName() + " (async).");
                             calculatePermissions(p);
                         }
                     } else {
                         return false;
                     }
                     return true;
                 }
                 if(args[0].equalsIgnoreCase("reload")) {
                     DBPermissions.this.reloadConfig();
                     sender.sendMessage("DBPermissions config reloaded.");
                     return true;
                 }
 
                 if(args[0].equalsIgnoreCase("check")) {
                     if(args.length != 3) return false;
                     Player p = Bukkit.getPlayer(args[1]);
                     String permission = args[2];
                     sender.sendMessage(p.getName() + " has " + permission + " set to " + p.hasPermission(permission));
                     return true;
                 }
 
                 return false;
             }
         });
 
         getServer().getPluginManager().registerEvents(this, this);
 
         // calculate permissions for any online players
         for(Player p : getServer().getOnlinePlayers()){
             registerPlayer(p);
             calculatePermissions(p);
         }
         
         log("Version {0} enabled with logger \"{1}\"", getDescription().getVersion(), getLogger().getName());
     }
     
     @Override
     public void onDisable() {
         for(Player p : getServer().getOnlinePlayers()) {
             unregisterPlayer(p);
         }
     }
 
 
     
     public void registerPlayer(Player player) {
         if(attachments.containsKey(player)) {
             debug("Warning while registering:" + player.getName() + " already had an attachment");
             unregisterPlayer(player);
         }
         PermissionAttachment attachment = player.addAttachment(this);
         attachments.put(player, attachment);
         debug("Added attachment for " + player.getName());
     }
     
     public void unregisterPlayer(Player player) {
         if(attachments.containsKey(player)) {
             try {
                 player.removeAttachment(attachments.get(player));
             }
             catch (IllegalArgumentException ex) {
                 debug("Unregistering for " + player.getName() + " failed: No attachment");
             }
             this.attachments.remove(player);
             debug("Attachment unregistered for " + player.getName());
         } else {
             debug("Unregistering for " + player + " failed: No stored attachment");
         }
     }
 
     public void calculatePermissions(final Player player) {
         calculatePermissions(player, player.getWorld());
     }
     public void calculatePermissions(final Player player, final World world) {
         debug("Calculating player permissions from database for {0}", player.getName());
                 
         // get this player's attachment
         final PermissionAttachment attachment = attachments.get(player);
         
         //Set the defualt permissions here to "fix" some plugins who cannot listen to the
         //  PermissionsCalculated event
         for(String key : getConfig().getConfigurationSection("default_permissions").getKeys(false)){
            attachment.setPermission(key,getConfig().getBoolean("defualt_permissions."+key));
         }
         
         // find the group permissions (and any default groups), and assign those permissions
         new Query("SELECT `permissions`.* FROM `permissions`, `groups` WHERE `groups`.`group` = ? AND `permissions`.`identifier` = `groups`.`group` AND `permissions`.`type` = 'group'"
                + " UNION SELECT `permissions`.* FROM `permissions`, `player_groups` WHERE `player_groups`.`player` = ? AND `permissions`.`identifier` = `player_groups`.`group` AND `permissions`.`type` = 'group'"
                + " UNION SELECT `permissions`.* FROM `permissions` WHERE `permissions`.`identifier` = ? AND `permissions`.`type` = 'player'") {
             
             @Override
             public void onBeforeCallback() {
                 debug("Unsetting player permissions for {0}", player.getName());
                 // unset old player permissions
                 if(attachment != null) {
                     for(String key : attachment.getPermissions().keySet()) {
                         attachment.unsetPermission(key);
                     }
                 }
             }
             
             @Override
             public void onFetch(ArrayList<HashMap> rows) {
                 debug("Setting new player permissions for {0}", player.getName());
                 // set new player permissions
                 for(HashMap row : rows) {
                     String w = (String) row.get("world");
                     if(world.getName().equalsIgnoreCase(w) || w.equals("*")) {
                         attachment.setPermission((String) row.get("permission"), (Integer) row.get("value") == 1);
                         debug("Set [" + row.get("type") + ":" + row.get("identifier") + "][W:" + row.get("world") + "] " + row.get("permission") + " for " + player.getName() + " to " + row.get("value"));
                     }
                 }
             }
             
             @Override
             public void onAfterCallback() {
                 debug("Calling event PermissionsCalculated for {0}", player.getName());
                 getServer().getPluginManager().callEvent(new PermissionsCalculated(player));
             }
             
         }.fetch(getConfig().getString("default_group"),
             player.getName(),
             player.getName());
         
     }
     
     
     class Query extends com.minecarts.dbquery.Query {
         public Query(String sql) {
             super(DBPermissions.this, dbq.getProvider(getConfig().getString("db.provider")), sql);
         }
         @Override
         public void onBeforeCallback(FinalQuery query) {
             if(query.elapsed() > 500) {
                 log(MessageFormat.format("Slow query took {0,number,#} ms", query.elapsed()));
             }
         }
         @Override
         public void onException(Exception x, FinalQuery query) {
             try { throw x; }
             catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
     
     
     public void log(String message) {
         log(Level.INFO, message);
     }
     public void log(Level level, String message) {
         getLogger().log(level, message);
     }
     public void log(String message, Object... args) {
         log(MessageFormat.format(message, args));
     }
     public void log(Level level, String message, Object... args) {
         log(level, MessageFormat.format(message, args));
     }
 
     public void debug(String message) {
         log(Level.FINE, message);
     }
     public void debug(String message, Object... args) {
         debug(MessageFormat.format(message, args));
     }
 }
