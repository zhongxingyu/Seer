 package com.maienm.FlooNetwork;
 
 import com.m0pt0pmatt.menuservice.api.MenuInstance;
 import com.m0pt0pmatt.menuservice.api.MenuService;
 import com.m0pt0pmatt.menuservice.api.Renderer;
 import com.m0pt0pmatt.menuservice.api.ActionEvent;
 import com.m0pt0pmatt.menuservice.api.ActionListener;
 import com.maienm.FlooNetwork.Fireplace;
 import com.maienm.FlooNetwork.PlayerMenu;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.LazyMetadataValue.CacheStrategy;
 import org.bukkit.metadata.LazyMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class FlooNetwork extends JavaPlugin implements Listener, ActionListener
 {
     protected static FlooNetwork plugin;
     protected FileConfiguration config;
     private File portalFile;
 
     /**
      * The material used to travel.
      */
     private int TRAVALCATALYST = -1;
 
     /**
      * The metadata key for fire invulnerability.
      */
     final private String FIREINV_METADATA_KEY = "floonetworkFireInvulnerability";
 
     /**
      * The duration for fire invulnerability.
      */
     final private int FIREINV_DURATION = 1;
 
     /**
      * The list of all DamageCause types we want to ignore when in a fireplace.
      */
     private static final Set<DamageCause> IGNORED_DAMAGECAUSES = new HashSet<DamageCause>(Arrays.asList(
         new DamageCause[] {DamageCause.FIRE, DamageCause.FIRE_TICK}
     ));
 
     /**
      * The list of all Action types we want to accept to travel.
      */
     private static final Set<Action> ACCEPTED_ACTIONS = new HashSet<Action>(Arrays.asList(
         new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}
     ));
 
     /**
      * Map of all fireplaces.
      */
     private HashMap<OfflinePlayer, HashMap<String, Fireplace>> fireplaces = new HashMap<OfflinePlayer, HashMap<String, Fireplace>>();
  
     /**
      * On plugin load.
      */
     @Override
     public void onEnable() 
     {
         // Load the config.
         reloadConfigCustom();
 
         // Call init on the other classess that need it.
         Fireplace.init();
         PlayerMenu.init();
 
         // Register all event handlers.
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     /**
      * On plugin unload.
      */
     @Override
     public void onDisable()
     {
         // Save the config.
         saveConfigCustom();
     }
 
     /**
      * (Re)loads the config file.
      */
     private void reloadConfigCustom()
     {
         System.out.println("Loading config.");
 
         // Copy over the default config if needed.
         saveDefaultConfig();
 
         // Reload the config.
         reloadConfig();
         config = getConfig();
 
         // Load the material from the config.
         TRAVALCATALYST = config.getInt("flooPowderID");
         System.out.println(String.format("Set floo powder ID to %d.", TRAVALCATALYST));
 
         // Load the list of fireplaces from the config.
         ConfigurationSection cfgFireplaces = config.getConfigurationSection("fireplaces");
         fireplaces.clear();
         if (cfgFireplaces != null)
         {
             OfflinePlayer player;
             ConfigurationSection fpConfig;
             List<Integer> coords;
             World world;
             Location location;
             Fireplace fp;
 
             // Loop over all players.
             for (Map.Entry<String, Object> playerEntry : cfgFireplaces.getValues(false).entrySet())
             {
                 // Get the player object.
                 player = getServer().getOfflinePlayer(playerEntry.getKey());
                 fireplaces.put(player, new HashMap<String, Fireplace>());
 
                 // Loop over this users fireplaces.
                 for (Map.Entry<String, Object> fpEntry : ((ConfigurationSection)playerEntry.getValue()).getValues(false).entrySet())
                 {
                     // Get the ConfigurationSection.
                     fpConfig = (ConfigurationSection) fpEntry.getValue();
 
                     // Read out the location.
                     coords = fpConfig.getIntegerList("coordinates");
                     world = getServer().getWorld(fpConfig.getString("world"));
                     location = new Location(world, coords.get(0), coords.get(1), coords.get(2));
 
                    // Make sure the chunk is loaded.
                    world.loadChunk(location.getChunk());

                     // Create the fireplace.
                     fp = Fireplace.detect(location);
                     if (fp == null)
                     {
                         System.out.println(String.format("Fireplace %s of player %s is invalid; it has been ignored.", fpEntry.getKey(), playerEntry.getKey()));
                         continue;
                     }
 
                     // Store the fireplace.
                     fp.owner = player;
                     fp.name = fpEntry.getKey();
                     Sign sign = (Sign)fp.getSignLocation().getBlock().getState();
                     fp.item = parseItemID(sign.getLine(2));
                     if (fp.item == null)
                     {
                         fp.item = new ItemStack(1);
                     }
                     fireplaces.get(player).put(fpEntry.getKey(), fp);
                 }
             }
         }
 
         System.out.println("Done config.");
     }
 
     /**
      * Saves the config file.
      */
     private void saveConfigCustom()
     {
         // Rewrite the fireplaces section.
         ConfigurationSection cfgFireplaces = config.createSection("fireplaces");
         ConfigurationSection cfgPlayer;
         ConfigurationSection cfgFireplace;
         Location location;
 
         for (Map.Entry<OfflinePlayer, HashMap<String, Fireplace>> playerEntry : fireplaces.entrySet())
         {
             // Create the section for this player.
             cfgPlayer = cfgFireplaces.createSection(playerEntry.getKey().getName());
 
             for (Map.Entry<String, Fireplace> fpEntry : playerEntry.getValue().entrySet())
             {
                 // Create the section for this fireplace.
                 cfgFireplace = cfgPlayer.createSection(fpEntry.getKey());
 
                 // Set the data.
                 Fireplace fp = fpEntry.getValue();
                 location = fp.getSignLocation();
                 cfgFireplace.set("world", location.getWorld().getName());
                 cfgFireplace.set("coordinates", Arrays.asList(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
             }
         }
 
         // Save the config.
         saveConfig();
     }
 
     /**
      * Plugin name.
      */
     @Override
     public String getPlugin() 
     {
         return "FlooNetwork";
     }
 
     /**
      * Dummy methods.
      */
     @Override
     public void playerAdded(MenuInstance instance, String playerName) {}
     @Override
     public void playerRemoved(MenuInstance instance, String playerName) {}
     @Override
     public void playerCountZero(MenuInstance instance, String playerName) {}
 
     /**
      * Command event.
      *
      * Handles the /fn command.
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] arguments)
     {
         // Convert the arguments to a list.
         ArrayList<String> args = new ArrayList<String>(Arrays.asList(arguments));
 
         // No arguments => usage.
         if (args.size() == 0)
         {
             String version = getDescription().getVersion();
             sender.sendMessage(ChatColor.GOLD + "FlooNetwork " + ChatColor.BLUE + version + ChatColor.GOLD + " by MaienM");
             sender.sendMessage("--------------------");
             if (sender.hasPermission("floonetwork.command.list"))
             {
                 sender.sendMessage("/fn list: List your own fireplaces.");
                 sender.sendMessage("/fn list <sender>: List the fireplaces of <player> you have access to.");
                 sender.sendMessage("/fn listall: List all fireplaces you have access to.");
             }
             if (sender.hasPermission("floonetwork.command.warp"))
                 sender.sendMessage("/fn warpto <sender> <fireplace>: Warp self to <fireplace> of <player>.");
             if (sender.hasPermission("floonetwork.command.warp.other"))
                 sender.sendMessage("/fn warpto <sender> <fireplace> <target>: Warp <target> to <fireplace> of <player>.");
             if (sender.hasPermission("floonetwork.command.reload"))
                 sender.sendMessage("/fn reload: Reload the config.");
             return true;
         }
         else 
         {
             String command = args.remove(0).toLowerCase();
             OfflinePlayer subject;
 
             if (command.equals("list"))
             {
                 // Get the subject.
                 subject = getSubject(sender, args);
                 if (subject == null)
                 {
                     return false;
                 }
 
                 // Check permission.
                 if (!requirePermission(sender, "floonetwork.command.list"))
                 {
                     return false;
                 }
 
                 // If more arguments => error.
                 if (args.size() > 0)
                 {
                     return sendError(sender, "Invalid number of arguments.");
                 }
 
                 // Check if user has any fireplaces.
                 if (!fireplaces.containsKey(subject))
                 {
                     return sendError(sender, "No fireplaces found.");
                 }
 
                 // List all fireplaces.
                 sender.sendMessage(ChatColor.BLUE + "Fireplaces of " + subject.getName());
                 for (Map.Entry<String, Fireplace> entry : fireplaces.get(subject).entrySet())
                 {
                     Fireplace fp = entry.getValue();
                     if (!(sender instanceof Player) || fp.hasAccess((Player)sender))
                     {
                         sender.sendMessage(entry.getKey());
                     }
                 }
             }
 
             else if (command.equals("listall"))
             {
                 // Check permission.
                 if (!requirePermission(sender, "floonetwork.command.list"))
                 {
                     return false;
                 }
 
                 // If more arguments => error.
                 if (args.size() > 0)
                 {
                     return sendError(sender, "Invalid number of arguments.");
                 }
 
                 // List all fireplaces.
                 for (Map.Entry<OfflinePlayer, HashMap<String, Fireplace>> playerEntry : fireplaces.entrySet())
                 {
                     sender.sendMessage(ChatColor.BLUE + "Fireplaces of " + playerEntry.getKey().getName());
                     for (Map.Entry<String, Fireplace> fpEntry : playerEntry.getValue().entrySet())
                     {
                         Fireplace fp = fpEntry.getValue();
                         if (!(sender instanceof Player) || fp.hasAccess((Player)sender))
                         {
                             sender.sendMessage(fpEntry.getKey());
                         }
                     }
                 }
             }
 
             else if (command.equals("reload"))
             {
                 if (!requirePermission(sender, "floonetwork.command.reload"))
                 {
                     return false;
                 }
 
                 reloadConfigCustom();
                 sender.sendMessage(ChatColor.BLUE + "Reloaded config.");
             }
 
             else if (command.equals("warpto") || command.equals("tp"))
             {
                 // Get the fireplace.
                 if (args.size() < 2)
                 {
                     return sendError(sender, "Invalid number of arguments.");
                 }
                 Fireplace fp = getFireplace(getServer().getOfflinePlayer(args.remove(0)), args.remove(0));
                 if (fp == null)
                 {
                     return sendError(sender, "Unable to find fireplace.");
                 }
 
                 // Get the subject.
                 subject = getSubject(sender, args);
                 if (subject == null)
                 {
                     return false;
                 }
 
                 // Check permission.
                 if (!requirePermission(sender, "floonetwork.command.warp" + (subject.equals(sender) ? "" : ".other") + (sender instanceof Player && fp.isOwner((Player)sender) ? "" : ".anywhere")))
                 {
                     return false;
                 }
                 if (sender instanceof Player && !fp.hasAccess((Player)sender))
                 {
                     return sendError(sender, "You do not have access to that fireplace.");
                 }
 
                 // If more arguments => error.
                 if (args.size() > 0)
                 {
                     return sendError(sender, "Invalid number of arguments.");
                 }
 
                 // Warp to the fireplace.
                 Player player = subject.getPlayer();
                 if (player == null)
                 {
                     return sendError(sender, "Unable to find target player.");
                 }
                 fp.warpTo(player);
             }
 
             else 
             {
                 return sendError(sender, "Unknown command.");
             }
         }
         return false;
     }
 
     /**
      * Convenience method to get the subject of a command. The subject can be either the first remaining argument, the current player, or none.
      */
     private OfflinePlayer getSubject(CommandSender sender, List<String> args)
     {
         // If no arguments => current player.
         if (args.size() == 0)
         {
             // If the sender is not a player (console), give an error.
             if (sender instanceof Player)
             {
                 return (OfflinePlayer)sender;
             }
             else
             {
                 sendError(sender, "You must specify a player when using this from the console.");
                 return null;
             }
         }
 
         // Else => get player by name.
         else 
         {
             OfflinePlayer player = getServer().getOfflinePlayer(args.remove(0));
 
             if (player == null)
             {
                 sendError(sender, "Unknown user.");
                 return null;
             }
 
             return player;
         }
     }
 
     /**
      * Block break event.
      *
      * Protects the fireplace when needed.
      */
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event)
     {
         // Check if the block belongs to a fireplace.
         Fireplace fp = getFireplace(event.getBlock().getLocation(), false, true, true, false);
         if (fp == null)
         {
             return;
         }
 
         // Get the player that triggered the event.
         Player player = event.getPlayer();
 
         // If the event was not triggered by a player, block it.
         if (player == null)
         {
             event.setCancelled(true);
             return;
         }
 
         // Check the permissions.
         if (!requirePermission(player, "floonetwork.destroy" + (fp.isOwner(player) ? "" : ".other")))
         {
             event.setCancelled(true);
             return;
         }
 
         // Destroy the fireplace.
         fireplaces.get(fp.owner).remove(fp.name);
         player.sendMessage(ChatColor.BLUE + String.format("Destroyed fireplace %s%s.", fp.name, fp.isOwner(player) ? "" : " of " + fp.owner.getName()));
 
         // Mark the sign as deactivated.
         Sign sign = (Sign)fp.getSignLocation().getBlock().getState();
         sign.setLine(0, ChatColor.RED + sign.getLine(0));
         sign.setLine(1, ChatColor.RED + sign.getLine(1));
         sign.setLine(2, ChatColor.RED + sign.getLine(2));
         sign.setLine(3, ChatColor.RED + "DEACTIVATED");
         sign.update();
     }
 
     /**
      * Sign placement/change event.
      *
      * Handles detection of new fireplaces.
      */
     @EventHandler(ignoreCancelled = true)
     public void onSignChange(SignChangeEvent event) 
     {
         // Check if the sign matches the criterea.
         if (!ChatColor.stripColor(event.getLine(0)).equals("[fn]"))
         {
             return;
         }
 
         // Get the player that triggered the event.
         Player player = event.getPlayer();
 
         // Check whether the user has the required permissions.
         if (!requirePermission(player, "floonetwork.create"))
         {
             rejectSign(event);
             return;
         }
 
         // Check whether the the second line is valid.
         String name = ChatColor.stripColor(event.getLine(1));
         if (name.equals(""))
         {
             sendError(player, "You need to give the fireplace a name.");
             rejectSign(event);
             return;
         }
         if (getFireplace(player, name) != null)
         {
             sendError(player, "You already have a fireplace with this name.");
             rejectSign(event);
             return;
         }
 
         // Check whether the third line is valid.
         String itemIDText = ChatColor.stripColor(event.getLine(2));
         ItemStack itemID = new ItemStack(1);
         if (!itemIDText.equals(""))
         {
             itemID = parseItemID(itemIDText);
             if (itemID == null)
             {
                 sendError(player, "That does not seem to be a valid item id.");
                 rejectSign(event);
                 return;
             }
         }
 
         // Check whether we can find a valid fireplace here.
         Fireplace fireplace = Fireplace.detect(event.getBlock().getLocation());
         if (fireplace == null) 
         {
             sendError(player, "That does not seem to be a valid fireplace layout.");
             rejectSign(event);
             return;
         }
 
         // Set all values of the fireplace.
         fireplace.owner = player;
         fireplace.name = name;
         fireplace.item = itemID;
 
         // Store the fireplace.
         if (!fireplaces.containsKey(player))
             fireplaces.put(player, new HashMap<String, Fireplace>());
         fireplaces.get(player).put(name, fireplace);
 
         // Save the config.
         saveConfigCustom();
 
         // Notify the player.
         player.sendMessage(ChatColor.BLUE + "Created fireplace");
         System.out.println(fireplace.toString());
     }
 
     /**
      * Reject an SignChangeEvent, dropping the sign.
      */
     private void rejectSign(SignChangeEvent event)
     {
         // Get the sign.
         Block sign = event.getBlock();
 
         // Replace the sign by air, drop as resource.
         sign.setTypeId(0);
         sign.getWorld().dropItem(sign.getLocation(), new ItemStack(Material.SIGN, 1));
 
         // Mark event as cancelled.
         event.setCancelled(true);
     }
 
     /**
      * Player damage event.
      *
      * Handles protecting a player from fire while in a fireplace.
      */
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerDamage(EntityDamageEvent event)
     {
         // Check if the entity is a player.
         if (event.getEntityType() != EntityType.PLAYER)
         {
             return;
         }
 
         // Check if the damage is due to fire.
         if (!IGNORED_DAMAGECAUSES.contains(event.getCause()))
         {
             return;
         }
 
         // Check if the player is still marked as invulnerable.
         boolean invulnerable = false;
         boolean valid = false;
         final Player player = (Player) event.getEntity();
         for (MetadataValue val : player.getMetadata(FIREINV_METADATA_KEY))
         {
             if (val.getOwningPlugin().equals(this))
             {
                 boolean[] res = (boolean[])val.value();
                 invulnerable = res[0];
                 valid = res[1];
             }
         }
 
         // If the result is no longer valid, refresh it.
         if (!valid)
         {
             invulnerable = getFireplace(player.getLocation(), true, false, false, true) != null;
 
             // Cache the result.
             final Plugin plug = this;
             final Date validTo = new Date();
             final boolean invuln = invulnerable;
             validTo.setTime(validTo.getTime() + FIREINV_DURATION);
             player.setMetadata(FIREINV_METADATA_KEY, new LazyMetadataValue(this, CacheStrategy.NEVER_CACHE, new Callable<Object>() 
             {
                 public Object call()
                 {
                     return new boolean[]{invuln, validTo.after(new Date())};
                 }
             }));
         }
 
         // Cancel the damage.
         if (invulnerable)
         {
             event.setCancelled(true);
             event.getEntity().setFireTicks(0);
         }
     }
 
     /**
      * Item use event.
      *
      * Handles a player using a fireplace.
      * Handler a player right-clicking a sign to reactivate a fireplace.
      */
     @EventHandler(ignoreCancelled = true)
     public void onPlayerUse(PlayerInteractEvent event)
     {
         // Check whether the user is right-clicking.
         if (!ACCEPTED_ACTIONS.contains(event.getAction()))
         {
             return;
         }
 
         // If the player is right clicking on a sign, process that.
         if (event.getClickedBlock().getType() == Material.WALL_SIGN)
         {
             onPlayerUseSign(event);
         }
 
         // If the player is right-clicking with floo powder, process that.
         if (event.getMaterial().getId() == TRAVALCATALYST)
         {
             onPlayerUsePowder(event);
         }
     }
 
     /**
      * Called when the user clicks a sign.
      */
     public void onPlayerUseSign(PlayerInteractEvent event)
     {
         // Check if the sign is part of a valid fireplace. If so, ignore it,
         Location loc = event.getClickedBlock().getLocation();
         for (Fireplace fireplace : getAllFireplaces())
         {
             if (fireplace.getSignLocation().equals(loc))
             {
                 return;
             }
         }
 
         // Trigger the sign event.
         Sign sign = (Sign)event.getClickedBlock().getState();
         getServer().getPluginManager().callEvent(new SignChangeEvent(event.getClickedBlock(), event.getPlayer(), sign.getLines()));
         
         // Check if the sign has been reactivated. If so, fix it.
         for (Fireplace fireplace : getAllFireplaces())
         {
             if (fireplace.getSignLocation().equals(loc))
             {
                 sign.setLine(0, ChatColor.stripColor(sign.getLine(0)));
                 sign.setLine(1, ChatColor.stripColor(sign.getLine(1)));
                 sign.setLine(2, ChatColor.stripColor(sign.getLine(2)));
                 sign.setLine(3, "");
                 sign.update();
                 break;
             }
         }
     }
 
     /**
      * Called when the user uses floo powder.
      */
     public void onPlayerUsePowder(PlayerInteractEvent event)
     {
         // Check whether the player is in a fireplace.
         Player player = event.getPlayer();
         Fireplace fp = getFireplace(player.getLocation(), true, false, false, true);
         if (fp == null)
         {
             return;
         }
 
         // Block the event, in case the catalyst is set to something placeable/useable (such as redstone).
         event.setCancelled(true);
 
         // Check whether the user has the required permissions.
         if (!requirePermission(player, "floonetwork.use" + (fp.isOwner(player) ? "" : ".other")))
         {
             return;
         }
 
         // Check whether the fireplace is lighted.
         if (!fp.isLighted())
         {
             sendError(player, "The fireplace is not burning. What a poor excuse for a fireplace it is.");
             return;
         }
 
         // Show  menu.
         OfflinePlayer oplayer = player;
         PlayerMenu playerMenu = new PlayerMenu(player);
         playerMenu.show();
     }
 
     /**
      * Handle a menu action.
      *
      * This will be triggered once a user has chosen the fireplace to travel to.
      */
     @Override
     public void handleAction(ActionEvent event)
     {
         // Get the player.
         Player player = Bukkit.getPlayer(event.getAction().getPlayerName());
         player.getOpenInventory().close();
         if (player == null)
         {
             return;
         }
 
         // Get the travel catalyst.
         ItemStack item = player.getInventory().getItemInHand();
         if (item.getType().getId() != TRAVALCATALYST)
         {
             sendError(player, "You do not seem to be holding Floo Powder.");
             return;
         }
 
         // Get the fireplace.
         Fireplace fp = getFireplace(event.getAction().getTag());
         if (fp == null)
         {
             sendError(player, "That fireplace no longer exists.");
             return;
         }
 
         // Check permission.
         if (!fp.hasAccess(player))
         {
             sendError(player, "You do not have access to that fireplace.");
             return;
         }
 
         // Check whether the user still is in a fireplace.
         Fireplace currentFP = getFireplace(player.getLocation(), true, false, false, true);
         if (currentFP == null)
         {
             sendError(player, "You have to be in a fireplace to travel to a fireplace.");
             return;
         }
         if (currentFP.equals(fp))
         {
             sendError(player, "You're already there!");
             return;
         }
 
         // Consume item.
         if (item.getAmount() > 1)
         {
             item.setAmount(item.getAmount() - 1);
         }
         else
         {
             player.getInventory().setItemInHand(null);
         }
 
         // Teleport player.
         currentFP.playEffect();
         fp.playEffect();
         fp.warpTo(player);
         
         // Extinguish player.
         player.setFireTicks(0);
     }
 
     /**
      * Convenience method to parse an item id.
      */
     private ItemStack parseItemID(String item)
     {
         String[] parts = item.split(":", 2);
 
         try
         {
             if (parts.length > 1)
             {
                 return new ItemStack(Integer.parseInt(parts[0]), 1,  (byte)Integer.parseInt(parts[1]));
             }
             else
             {
                 return new ItemStack(Integer.parseInt(parts[0]));
             }
         }
         catch  (NumberFormatException e)
         {
             return null;
         }
     }
 
     /**
      * Convenience method to send an error message to the user.
      */
     private boolean sendError(CommandSender sender, String error)
     {
         sender.sendMessage(ChatColor.RED + error);
         return false;
     }
 
     /**
      * Convenience method to check for permission.
      */
     private boolean requirePermission(CommandSender sender, String permission)
     {
         if (sender.hasPermission(permission))
         {
             return true;
         }
 
         sendError(sender, "You do not have the required permission to do this: " + ChatColor.BLUE + permission);
         return false;
     }
 
     /**
      * Convenience method to list all fireplaces.
      */
     public List<Fireplace> getAllFireplaces()
     {
         ArrayList<Fireplace> fpList = new ArrayList<Fireplace>();
         for (HashMap<String, Fireplace> userFireplaces : fireplaces.values())
         {
             for (Fireplace fireplace : userFireplaces.values())
             {
                 fpList.add(fireplace);
             }
         }
         return fpList;
     }
 
     /**
      * Convenience methods to find a fireplace.
      *
      * @param location The location to check.
      * @param fuzzyLookup If true, the given location will be checked in a less strict way. This is useful for if the position is not an exact position (such as a block has). For example, the position from an entity.
      * @param includeMaterial See Fireplace#contains.
      * @param includeSign See Fireplace#contains.
      * @param includeAir See Fireplace#contains.
      */
     private Fireplace getFireplace(Location location, boolean fuzzyLookup, boolean includeMaterial, boolean includeSign, boolean includeAir)
     {
         // Build the list of locations.
         ArrayList<Location> locations = new ArrayList<Location>();
         if (fuzzyLookup)
         {
             locations.add(location.clone().add(0.5,  0, 0));
             locations.add(location.clone().add(-0.5, 0, 0.5));
             locations.add(location.clone().add(-0.5, 0, -0.5));
             locations.add(location.clone().add(0,    0, -0.5));
         }
         else
         {
             locations.add(location.clone());
         }
 
         // Loop over the fireplaces.
         for (HashMap<String, Fireplace> userFireplaces : fireplaces.values())
         {
             for (Fireplace fireplace : userFireplaces.values())
             {
                 // Loop over the locations.
                 for (Location loc : locations)
                 {
                     if (fireplace.contains(loc, includeMaterial, includeSign, includeAir))
                     {
                         return fireplace;
                     }
                 }
             }
         }
         return null;
     }
     private Fireplace getFireplace(OfflinePlayer player, String name)
     {
         if (fireplaces.containsKey(player) && fireplaces.get(player).containsKey(name))
         {
             return fireplaces.get(player).get(name);
         }
         return null;
     }
     private Fireplace getFireplace(int id)
     {
         for (Fireplace fireplace : getAllFireplaces())
         {
             if (fireplace.id == id)
             {
                 return fireplace;
             }
         }
         return null;
     }
 }
