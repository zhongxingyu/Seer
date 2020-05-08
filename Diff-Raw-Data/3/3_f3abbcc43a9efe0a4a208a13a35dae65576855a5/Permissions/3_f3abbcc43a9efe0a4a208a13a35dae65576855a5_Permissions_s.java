 package to.joe.j2mc.core.permissions;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.PermissionAttachment;
 
 import to.joe.j2mc.core.Debug;
 import to.joe.j2mc.core.J2MC_Core;
 import to.joe.j2mc.core.J2MC_Manager;
 
 public class Permissions implements Listener {
 
     private final J2MC_Core plugin;
 
     /*
      * Flag documentation
      * 
      * p - teleport protection
      * t - trusted
      * d - donator
      * N - NSA
      * 
      */
 
     /*
      * groups: default, admin, srstaff
      */
 
     private HashMap<Character, HashMap<String, Boolean>> permissions;
     private HashMap<String, PermissionAttachment> attachments;
     private HashMap<String, HashSet<Character>> playerFlags;
     private HashMap<String, HashSet<Character>> groupFlags;
     private HashMap<String, String> playerGroup;
 
     public Permissions(J2MC_Core plugin) {
         this.plugin = plugin;
         this.loadGroupsAndPermissions();
         J2MC_Manager.getCore().getServer().getPluginManager().registerEvents(this, J2MC_Manager.getCore());
     }
     
     /**
      * Reload groups and permissions
      */
     public void loadGroupsAndPermissions() {
         this.permissions = new HashMap<Character, HashMap<String, Boolean>>();
        this.attachments = new HashMap<String, PermissionAttachment>();
         this.playerFlags = new HashMap<String, HashSet<Character>>();
         this.groupFlags = new HashMap<String, HashSet<Character>>();
         this.playerGroup = new HashMap<String, String>();
         try {
             final PreparedStatement statement = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `name`,`flags` FROM `groups` WHERE `server_id`=?");
             statement.setInt(1, J2MC_Manager.getServerID());
             final ResultSet result = statement.executeQuery();
             while (result.next()) {
                 final String flagList = result.getString("flags");
                 final HashSet<Character> flags = new HashSet<Character>();
                 for (final char flag : flagList.toCharArray()) {
                     flags.add(flag);
                 }
                 final String groupname = result.getString("name");
                 this.groupFlags.put(groupname, flags);
                 Debug.log(groupname + " " + flags);
             }
             if (!this.groupFlags.containsKey("default")) {
                 throw new Exception();
             }
             final PreparedStatement readPermissions = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `permission`, `flag`, `value` FROM `perms` WHERE `server_id`=?");
             readPermissions.setInt(1, J2MC_Manager.getServerID());
             final ResultSet readPermissionsResult = readPermissions.executeQuery();
             while (readPermissionsResult.next()) {
                 final String permission = readPermissionsResult.getString("permission");
                 final String flagString = readPermissionsResult.getString("flag");
                 final boolean value = readPermissionsResult.getBoolean("value");
                 final char flag = flagString.toCharArray()[0];
                 if (!this.permissions.containsKey(flag)) {
                     this.permissions.put(flag, new HashMap<String, Boolean>());
                 }
                 this.permissions.get(flag).put(permission, value);
                 Debug.log(flag + " " + permission + " " + value);
             }
         } catch (final Exception e) {
             e.printStackTrace();
             plugin.buggerAll("Could not load SQL groups");
         }
         for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
             if (player != null) {
                 this.initializePlayerPermissions(player.getName());
                 this.refreshPermissions(player);
             }
         }
     }
 
     /**
      * Temporarily add a flag to a player
      * 
      * @param player
      * @param flag
      */
     public void addFlag(Player player, char flag) {
         this.playerFlags.get(player.getName()).add(flag);
         this.refreshPermissions(player);
     }
 
     /**
      * Add a permenant flag to a player
      * 
      * @param player
      * @param flag
      */
     public void addPermanentFlag(Player player, char flag) {
         final HashSet<Character> newFlags = this.playerFlags.get(player);
         newFlags.add(flag);
         this.playerFlags.put(player.getName(), newFlags);
         String toAdd = "";
         for (final char derp : this.playerFlags.get(player)) {
             toAdd += derp;
         }
         try {
             final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE `users` SET flags=? WHERE name=?");
             ps.setString(1, toAdd);
             ps.setString(2, player.getName());
             ps.executeUpdate();
         } catch (final SQLException e) {
             e.printStackTrace();
         } catch (final ClassNotFoundException e) {
             e.printStackTrace();
         }
         this.refreshPermissions(player);
     }
 
     /**
      * Add a permenant flag to a player (use for offline players)
      * 
      * @param player
      * @param flag
      */
     public void addPermanentFlag(String player, char flag) {
         try {
             final PreparedStatement grab = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `flags` FROM `users` WHERE name=?");
             grab.setString(1, player);
             final ResultSet rs = grab.executeQuery();
             rs.next();
             final String toAdd = rs.getString("flags") + flag;
             final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE `users` SET flags=? WHERE name=?");
             ps.setString(1, toAdd);
             ps.setString(2, player);
             ps.executeUpdate();
         } catch (final SQLException e) {
             e.printStackTrace();
         } catch (final ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Remove a flag from a player, temporarily
      * 
      * @param player
      * @param flag
      */
     public void delFlag(Player player, char flag) {
         this.playerFlags.get(player.getName()).remove(flag);
         this.refreshPermissions(player);
     }
 
     /**
      * Check if player has flag
      * 
      * @param player
      *            - Player to check
      * @param flag
      *            - Flag to check
      * 
      * @return Returns true if player has flag, returns false if doesn't.
      */
     public boolean hasFlag(String player, char flag) {
         if (this.playerFlags.get(player) == null) {
             return false;
         }
         if (this.playerFlags.get(player).contains(flag)) {
             return true;
         } else {
             return false;
         }
     }
 
     public void initializePlayerPermissions(String name) {
         final HashSet<Character> flags = new HashSet<Character>();
         String group;
         try {
             final PreparedStatement userInfo = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `group`,`flags` FROM `users` WHERE `name`=?");
             userInfo.setString(1, name);
             final ResultSet result = userInfo.executeQuery();
             if (result.next()) {
                 group = result.getString("group");
                 final String flagList = result.getString("flags");
                 if (flagList != null) {
                     for (final char flag : flagList.toCharArray()) {
                         flags.add(flag);
                     }
                 }
             } else {
                 final PreparedStatement newPlayer = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO `users` (`name`, `group`, `flags`) VALUES ( ?, ? , ?)");
                 newPlayer.setString(1, name);
                 newPlayer.setString(2, "default");
                 newPlayer.setString(3, "");
                 newPlayer.executeUpdate();
                 group = "default";
             }
         } catch (final Exception e) {
             e.printStackTrace();
             group = "default";
         }
 
         this.playerGroup.put(name, group);
         this.playerFlags.put(name, flags);
     }
     
     /**
      * Returns player's flags
      * 
      * @param player
      * 
      */
     public HashSet<Character> getFlags(String player) {
         final HashSet<Character> flags = new HashSet<Character>();
         if (this.playerFlags.get(player) != null) {
             flags.addAll(this.playerFlags.get(player));
         }
         final String group = this.playerGroup.get(player);
         if (this.groupFlags.get(group) != null) {
             flags.addAll(this.groupFlags.get(group));
         }
         return flags;
     }
 
     /**
      * Called when a player joins the game.
      * Do not call this
      * 
      * @param player
      */
     @EventHandler(priority = EventPriority.LOWEST)
     public void playerLogin(PlayerLoginEvent event) {
         this.initializePlayerPermissions(event.getPlayer().getName());
         this.refreshPermissions(event.getPlayer());
     }
 
     /**
      * Called when the player quits
      * Do not call this.
      * 
      * @param player
      */
     @EventHandler(priority = EventPriority.HIGHEST)
     public void playerQuit(PlayerQuitEvent event) {
         final Player player = event.getPlayer();
         this.attachments.remove(player.getName());
         this.playerFlags.remove(player.getName());
     }
 
     /**
      * GOOD GOD MAN, call this before replacing this class or shutting down
      */
     public void shutdown() {
         for (final String playerName : this.attachments.keySet()) {
             final Player player = this.plugin.getServer().getPlayer(playerName);
             if (player != null) {
                 player.removeAttachment(this.attachments.get(playerName));
             }
         }
         this.plugin.getLogger().info("Unloaded all permissions");
     }
 
     private void refreshPermissions(Player player) {
         final String name = player.getName();
         if (this.attachments.containsKey(name)) {
             player.removeAttachment(this.attachments.remove(name));
         }
         final PermissionAttachment attachment = player.addAttachment(this.plugin);
 
         final HashSet<Character> flags = new HashSet<Character>();
         if (this.playerFlags.get(name) != null) {
             flags.addAll(this.playerFlags.get(name));
         }
         final String group = this.playerGroup.get(name);
         if (this.groupFlags.get(group) != null) {
             flags.addAll(this.groupFlags.get(group));
         }
         final HashSet<Character> completed = new HashSet<Character>();
         Debug.log("Joining: " + player.getName());
         for (final Character flag : flags) {
             Debug.log("Flag: " + flag);
             if (completed.contains(flag)) {
                 continue;
             }
             completed.add(flag);
             if (this.permissions.containsKey(flag)) {
                 final HashMap<String, Boolean> permissionsAndValue = this.permissions.get(flag);
                 for (Map.Entry<String, Boolean> entry : permissionsAndValue.entrySet()) {
                     Debug.log("Node: " + entry.getKey() + ", Value: " + entry.getValue());
                     attachment.setPermission(entry.getKey(), entry.getValue());
                 }
             }
         }
         this.attachments.put(name, attachment);
     }
 
 }
