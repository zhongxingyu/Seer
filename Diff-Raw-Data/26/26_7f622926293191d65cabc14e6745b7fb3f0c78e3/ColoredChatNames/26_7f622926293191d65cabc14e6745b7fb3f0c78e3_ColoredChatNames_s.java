 package com.gmail.emertens.coloredchatnames;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Simple plug-in for replacing player's names in chat messages with their "display names" automatically
  *
  * @author Eric Mertens
  */
 public final class ColoredChatNames extends JavaPlugin {
 
     private static final String DISABLED_USERS_CONFIG_SECTION = "disabled-users";
     private static final String PLAYER_NOT_FOUND = ChatColor.RED + "Player not found";
     private static final String NO_TOGGLE_PERMISSION = ChatColor.RED + "You don't have permission to change other players.";
     private static final String CONSOLE_NOT_PLAYER = ChatColor.RED + "Console does not generate chat events.";
     private static final String TOO_MANY_ARGUMENTS = ChatColor.RED + "Too many arguments.";
     private static final String TOGGLE_COMMAND = "coloredchatnames";
     private static final String TOGGLE_OTHER_PERMISSION = "coloredchatnames.toggle.other";
     private static final String COLORED_PERMISSION = "coloredchatnames";
     private static final String TARGET_PERMISSION = "coloredchatnames.target";
 
     /**
      * Set of users who will not have automatic coloring even if they have permission for it.
      */
     private final Set<String> disabledUsers = new HashSet<String>();
 
     /**
      * Listener for asynchronous chat message. When appropriate the messages are colored.
      */
    final Listener listener = new Listener() {
         @EventHandler(ignoreCancelled = true)
         public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
 
             final Player player = event.getPlayer();
            if (!playerEnabled(player)) return;

            String msg = event.getMessage();
            for (final Player online : getServer().getOnlinePlayers()) {
                // Perform a case-insensitive match on the whole-word
                if (online.hasPermission(TARGET_PERMISSION)) {
                    msg = msg.replaceAll("(?i)\\b" + online.getName() + "\\b", online.getDisplayName());
                 }
            }
 
            event.setMessage(msg);
         }
     };
 
     /**
      * Listen for the command to toggle player's preferences about their chat messages being colored.
      */
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
 
         if (command.getName().equalsIgnoreCase(TOGGLE_COMMAND)) {
             // Determine target
             final String targetName = determineTarget(sender, args);
             if (targetName != null) {
                 // Determine the effect of the toggle
                 final boolean enabled = togglePlayerStatus(targetName);
                 final String changeMsg = enabled ? "enabled" : "disabled";
                 sender.sendMessage(ChatColor.GREEN + "Colored name chat " + changeMsg + " for " + targetName);
 
                 updateConfig();
             }
 
             return true;
         }
 
         return false;
     }
 
     /**
      * Compute the name of the player to be affected by a particular command.
      *
      * @param sender Sender of the command
     * @param args Arguments to the command
      * @return Name of player to target, null on failure
      */
     private String determineTarget(final CommandSender sender, final String[] args) {
 
         switch (args.length) {
             case 0: // Player unspecified - target self
 
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(CONSOLE_NOT_PLAYER);
                     return null;
                 }
 
                 return sender.getName();
 
             case 1: // Player specified
 
                 if (!sender.hasPermission(TOGGLE_OTHER_PERMISSION)) {
                     sender.sendMessage(NO_TOGGLE_PERMISSION);
                     return null;
                 }
 
                 final Player target = getServer().getPlayer(args[0]);
                 if (target == null) {
                     sender.sendMessage(PLAYER_NOT_FOUND);
                     return null;
                 }
                 return target.getName();
 
             default: // Player over-specified
                 sender.sendMessage(TOO_MANY_ARGUMENTS);
                 return null;
         }
     }
 
     /**
      * Toggle whether a player's chat messages should be colored.
      *
      * @param targetName Player's name to toggle
      * @return New preference: true if player is now enabled, false if player is now disabled
      */
     private boolean togglePlayerStatus(final String targetName) {
         final boolean oldDisabled = disabledUsers.contains(targetName);
         if (oldDisabled) {
             disabledUsers.remove(targetName);
         } else {
             disabledUsers.add(targetName);
         }
         return oldDisabled;
     }
 
     /**
      * Register for chat events and load plugin state from configuration files
      */
     @Override
     public void onEnable() {
 
         // Saves the config if and only if one does not exist
         saveDefaultConfig();
 
         // Start listening for chat events
         getServer().getPluginManager().registerEvents(listener, this);
 
         loadConfig();
     }
 
     /**
      * Restore plug-in state from configuration file.
      */
     private void loadConfig() {
         disabledUsers.clear();
 
         final List<?> configDisabledUsers = getConfig().getList(DISABLED_USERS_CONFIG_SECTION, new ArrayList<String>());
         for (final Object disabledUser : configDisabledUsers) {
             if (disabledUser instanceof String) {
                 disabledUsers.add((String) disabledUser);
             }
         }
     }
 
     /**
      * Store plug-in state to configuration file
      */
     private void updateConfig() {
         getConfig().set(DISABLED_USERS_CONFIG_SECTION, disabledUsers.toArray());
         saveConfig();
     }
 
     /**
      * Test if player's chat messages should be colored
      *
      * @param player Player who is sending the chat message
      * @return true when the player's messages should be colored
      */
     private boolean playerEnabled(final Player player) {
         return player.hasPermission(COLORED_PERMISSION)
                 && !disabledUsers.contains(player.getName());
     }
 }
