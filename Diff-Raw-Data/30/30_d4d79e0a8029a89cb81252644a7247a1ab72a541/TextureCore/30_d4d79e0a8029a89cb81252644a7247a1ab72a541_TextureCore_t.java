 package net.krinsoft.texturesuite;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.command.RemoteConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author krinsdeath
  */
 public class TextureCore extends JavaPlugin {
     private final Map<String, String> packs = new HashMap<String, String>();
     private boolean debug = false;
     private FileConfiguration players;
 
     public void onEnable() {
         if (!new File(getDataFolder(), "config.yml").exists()) {
             saveDefaultConfig();
         }
         try {
             debug = getConfig().getBoolean("debug");
            setupPacks();
         } catch (RuntimeException e) {
             getLogger().info(e.getLocalizedMessage());
         }
         getPlayers();
         getServer().getPluginManager().registerEvents(new Listener() {
             @EventHandler(priority = EventPriority.MONITOR)
             void playerJoin(final PlayerJoinEvent event) {
                 final String pack = getPlayerPack(event.getPlayer().getName());
                 if (pack != null) {
                     getServer().getScheduler().scheduleSyncDelayedTask(TextureCore.this, new Runnable() {
                         @Override
                         public void run() {
                             setPack(event.getPlayer(), pack);
                         }
                     }, 10L);
                 }
             }
         }, this);
         // Add SimpleNotice messenger
         getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
     }
 
     public void onDisable() {
         packs.clear();
         saveConfig();
         savePlayers();
     }
 
     private final String[] subCmds = new String[] { "list", "get", "reset", "add", "remove", "reload" };
 
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         List<String> tabbed = new ArrayList<String>();
         if (args.length > 0 && args[0].equalsIgnoreCase("get")) {
             String toComplete = args[args.length-1];
             for (String pack : packs.keySet()) {
                 if (pack.startsWith(toComplete)) {
                     tabbed.add(pack);
                 }
             }
             return tabbed;
         } else if (args.length > 0) {
             for (String cmd : subCmds) {
                 if (cmd.startsWith(args[0]) && hasPermission(sender, cmd)) {
                     tabbed.add(cmd);
                 }
             }
             return tabbed;
         }
         return null;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args.length < 1 || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
             return false;
         }
         List<String> arguments = new ArrayList<String>(Arrays.asList(args));
         String subCmd = arguments.remove(0);
         if (hasPermission(sender, subCmd)) {
             if (subCmd.equalsIgnoreCase("list")) {
                 StringBuilder message = new StringBuilder();
                 message.append(String.format("To get a pack, use " + ChatColor.GREEN + "/%s get [pack]\n" + ChatColor.RESET, label));
                 message.append("Available texture packs: \n");
                 Set<String> keys = packs.keySet();
                 if (keys.size() == 0) {
                     message.append("None.");
                 } else {
                     for (String pack : keys) {
                         message.append(pack).append("\n");
                     }
                 }
                 sender.sendMessage(message.toString());
             } else if (subCmd.equalsIgnoreCase("get")) {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "To get a texture pack, you must be a player!");
                     return true;
                 }
                 if (arguments.size() == 0) {
                     sender.sendMessage(ChatColor.RED + "You must supply a pack name!");
                     return true;
                 }
                 setPack((Player) sender, arguments.get(0));
             } else if (subCmd.equalsIgnoreCase("reset")) {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage(ChatColor.RED + "To reset a texture pack, you must be a player!");
                     return true;
                 }
                 if (getPlayerPack(sender.getName()) == null) {
                     sender.sendMessage(ChatColor.RED + "You are already using a default texture pack.");
                     return true;
                 }
                 setPack((Player) sender, null);
                 sender.sendMessage(ChatColor.GREEN + "Texture pack reset to default.");
                 sender.sendMessage(ChatColor.GREEN + "Please " + ChatColor.GOLD + "relog " + ChatColor.GREEN + "to apply the change.");
             } else if (subCmd.equalsIgnoreCase("add")) {
                 if (arguments.size() < 2) {
                     sender.sendMessage(ChatColor.RED + "Must supply a pack name and a URL!");
                     return true;
                 }
                 if (addPack(arguments.get(0), arguments.get(1))) {
                     sender.sendMessage(ChatColor.GREEN + String.format("Texture pack '%s' added successfully.", ChatColor.AQUA + arguments.get(0) + ChatColor.GREEN));
                 } else {
                     sender.sendMessage(ChatColor.RED + "Failed to add texture pack.");
                 }
             } else if (subCmd.equalsIgnoreCase("remove")) {
                 if (arguments.size() < 1) {
                     sender.sendMessage(ChatColor.RED + "Must supply a pack name and a URL!");
                     return true;
                 }
                 if (removePack(arguments.get(0))) {
                     sender.sendMessage(ChatColor.GREEN + String.format("Texture pack '%s' removed successfully.", ChatColor.AQUA + arguments.get(0) + ChatColor.GREEN));
                 } else {
                     sender.sendMessage(ChatColor.RED + "Failed to remove texture pack.");
                 }
             } else if (subCmd.equalsIgnoreCase("reload")) {
                 reloadConfig();
                setupPacks();
                 sender.sendMessage("TextureSuite's config file has been reloaded.");
             }
             return true;
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
             return true;
         }
     }
 
    private void setupPacks() {
        try {
            packs.clear();
            for (String key : getConfig().getConfigurationSection("packs").getKeys(false)) {
                String url = getConfig().getString("packs." + key);
                if (url.equalsIgnoreCase("insert URL here!")) {
                    throw new RuntimeException("Check config.yml to customize the texture pack list.");
                }
                packs.put(key, url);
            }
            debug(packs.size() + " pack" + (packs.size() > 1 ? "s": "") + " loaded.");
        } catch (NullPointerException e) {
            warn("No packs are defined in the config.yml!");
        }
    }

     public boolean hasPermission(CommandSender sender, String subCmd) {
         return sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender || sender.hasPermission("texturesuite." + subCmd);
     }
 
     public void debug(String message) {
         if (debug) {
             getLogger().info("[Debug] " + message);
         }
     }
 
     public void warn(String message) {
         getLogger().warning(message);
     }
 
     /**
      * Add a texture pack to the pack list
      * @param pack The name of the pack
      * @param url The url where the pack is hosted
      * @return true if the pack was added successfully, otherwise false
      */
     public boolean addPack(String pack, String url) {
         String exists = getConfig().getString("packs." + pack);
         if (exists != null) {
             return false;
         }
         getConfig().set("packs." + pack, url);
         packs.put(pack, url);
         saveConfig();
         return true;
     }
 
     /**
      * Remove a texture pack from the pack list.
      * @param pack The name of the pack
      * @return true if the pack removal succeeded, otherwise false
      */
     public boolean removePack(String pack) {
         String exists = getConfig().getString("packs." + pack);
         if (exists == null) {
             return false;
         }
         getConfig().set("packs." + pack, null);
         packs.remove(pack);
         saveConfig();
         return true;
     }
 
     /**
      * Sets the player's current texture pack
      * @param player The player whose texture pack is being set
      * @param pack The name of the texture pack
      */
     public void setPack(Player player, String pack) {
         try {
             if (pack == null) {
                 getPlayers().set(player.getName().toLowerCase(), null);
                 savePlayers();
                 return;
             }
             player.setTexturePack(packs.get(pack));
             String message = "You have selected the " + ChatColor.GREEN + pack + ChatColor.RESET + " pack.";
             if (player.getListeningPluginChannels().contains("SimpleNotice")) {
                 player.sendPluginMessage(this, "SimpleNotice", message.getBytes(java.nio.charset.Charset.forName("UTF-8")));
             } else {
                 player.sendMessage(message);
             }
             getPlayers().set(player.getName().toLowerCase(), pack);
             savePlayers();
         } catch (IllegalArgumentException e) {
             player.sendMessage(ChatColor.RED + "The supplied pack name was invalid.");
             warn(e.getLocalizedMessage());
         }
     }
 
     /**
      * Get's the player's persisted texture pack
      * @param player The player's name
      * @return the name of the texture pack this player has set
      */
     public String getPlayerPack(String player) {
         return getPlayers().getString(player.toLowerCase());
     }
 
     /**
      * Gets the configuration file containing the player data
      * @return The yaml object containing the player data
      */
     public FileConfiguration getPlayers() {
         if (players == null) {
             players = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "players.yml"));
         }
         return players;
     }
 
     public void savePlayers() {
         try {
             players.save(new File(getDataFolder(), "players.yml"));
         } catch (IOException e) {
             warn("An error occurred while saving the players.yml file! " + e.getLocalizedMessage());
 
         }
     }
 
 }
