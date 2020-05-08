 package com.bitlimit.Tweaks;
 
 import org.bukkit.block.Chest;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.Plugin;
 import java.util.*;
 
 import org.bukkit.event.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.CreatureSpawnEvent.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.metadata.*;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.block.*;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.meta.SkullMeta;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
 
 public class TweaksListener implements Listener {
     private final Tweaks plugin; // Reference main plugin
 
     /*********************************************
      Initialization: TweaksListener(plugin)
      ----------- Designated Initializer ----------
      *********************************************/
 
     public TweaksListener(Tweaks plugin) {
         // Notify plugin manager that this plugin handles implemented events (block place, etc.)
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         this.plugin = plugin;
     }
 
     /*********************************************
      Event Handler: onCreatureSpawnEvent(Event)
      --------------- Event Handler --------------
      *********************************************/
 
     @EventHandler
     public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
         // CreatureSpawnEvent (Entity spawnee, CreatureType type, Location loc, SpawnReason reason
 
         FileConfiguration config = this.plugin.getConfig();
         if (!config.getConfigurationSection("preferences").getBoolean("slimes"))
             return;
 
         // Gather information to determine if these are the slimes we are looking for.
         EntityType entityType = event.getEntityType();
         SpawnReason reason = event.getSpawnReason();
         if (entityType == EntityType.SLIME && (reason == SpawnReason.NATURAL || reason == SpawnReason.SLIME_SPLIT))  {
             // Pseudo-randomly cancel slime spawns to reduce their numbers.
             boolean shouldCancel = getRandomBoolean();
             event.setCancelled(shouldCancel);
         }
     }
 
     /******************************************
      Event Handler: Block Place(BlockPlaceEvent)
      ----------- Core Event Listener -----------
      ******************************************/
 
     @EventHandler
     public void onBlockPlaceEvent(BlockPlaceEvent event) {
         // Event reference
         // BlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild) 
 
         boolean confinementEnabled = this.plugin.getConfig().getConfigurationSection("preferences").getBoolean("tnt");
 
         if (event.getItemInHand().getType() == Material.TNT && confinementEnabled) {
             WorldGuardPlugin worldGuard = getWorldGuard();
             Block block = event.getBlockPlaced();
             Vector pt = toVector(block.getLocation());
             LocalPlayer localPlayer = worldGuard.wrapPlayer(event.getPlayer());
 
             RegionManager regionManager = worldGuard.getRegionManager(event.getPlayer().getWorld());
             ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
 
             if (set.size() == 0)
                 event.setCancelled(true);
             else
                 event.setCancelled(!set.isOwnerOfAll(localPlayer));
 
             if (event.isCancelled()) {
                 displaySmokeInWorldAtLocation(block.getWorld(), block.getLocation());
                 event.getPlayer().sendMessage(ChatColor.RED + "You are not authorized to place TNT in this location.");
             }
         }
 
         if (event.getItemInHand().getType() == Material.SKULL_ITEM || event.getItemInHand().getType() == Material.SKULL) {
             if (event.getItemInHand().getItemMeta().getLore() == null)
                 return;
 
             if (event.getItemInHand().getItemMeta().getLore().size() == 0)
                 return;
 
             SkullMeta skullMeta = (SkullMeta)event.getItemInHand().getItemMeta();
 
             List<String> meta = event.getItemInHand().getItemMeta().getLore();
             StringBuilder builder = new StringBuilder();
             for (String value : meta) {
                 builder.append(value);
             }
 
             String builtString = builder.toString();
             String strippedString = ChatColor.stripColor(builtString);
             builtString = builtString.replaceFirst(strippedString.substring(0, 1), strippedString.substring(0, 1).toLowerCase());
 
             event.getBlockPlaced().setMetadata("com.bitlimit.Tweaks.display", new FixedMetadataValue(this.plugin, ChatColor.YELLOW + skullMeta.getOwner() + ChatColor.AQUA + " was " + builtString));
             event.getBlockPlaced().setMetadata("com.bitlimit.Tweaks.lore", new FixedMetadataValue(this.plugin, skullMeta.getLore()));
         }
     }
 
     @EventHandler
     public void onPlayerInteractEvent(PlayerInteractEvent event) {
 
         Block block = event.getClickedBlock();
         if (block == null)
             return;
 
         if (!block.hasMetadata("com.bitlimit.Tweaks.display"))
             return;
 
         List<MetadataValue> metadataValueList = event.getClickedBlock().getMetadata("com.bitlimit.Tweaks.display");
 
         if (metadataValueList.size() > 0) {
             Player player = event.getPlayer();
 
             for (MetadataValue metadataValue : metadataValueList) {
                 String metaString = metadataValue.asString();
                 player.sendMessage(metaString);
             }
         }
     }
 
     @EventHandler
     public void onBlockBreakEvent(BlockBreakEvent event) {
         Block block = event.getBlock();
         if (block.hasMetadata("com.bitlimit.Tweaks.display"))
             block.removeMetadata("com.bitlimit.Tweaks.display", this.plugin);
 
 
         if (block.hasMetadata("com.bitlimit.Tweaks.lore")) {
             List<MetadataValue> metadataValueList = block.getMetadata("com.bitlimit.Tweaks.lore");
             ItemStack itemStack = (ItemStack)block.getDrops().iterator().next();
 
             if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                 ArrayList<String> lore = new ArrayList<String>();
                 for (MetadataValue metadataValue : metadataValueList) {
                     lore.add(metadataValue.asString().substring(1, metadataValue.asString().length() - 1));
                 }
 
                 ItemMeta newMeta = itemStack.getItemMeta();
                 newMeta.setLore(lore);
                 itemStack.setItemMeta(newMeta);
             }
 
             block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
 
             event.setCancelled(true);
             block.setType(Material.AIR);
 
             block.removeMetadata("com.bitlimit.Tweaks.lore", this.plugin);
         }
     }
 
 
     /******************************************
      Event Handler: Player Head Kill-Drops
      ----------- Core Event Listener -----------
      ******************************************/
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
         if (event.getEntity().getKiller() == null)
             return;
 
         if (event.getEntity().getKiller() instanceof Player) {
            ItemStack skullStack = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
             SkullMeta meta = (SkullMeta)skullStack.getItemMeta();
             meta.setOwner(event.getEntity().getDisplayName());
 
             Player killer = event.getEntity().getKiller();
             if (killer.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                 ArrayList lore = new ArrayList();
                 lore.add(ChatColor.AQUA + "Slain by " + ChatColor.GOLD + event.getEntity().getKiller().getDisplayName() + ChatColor.AQUA + " on " + getFriendlyDate(Calendar.getInstance()));
                 meta.setLore(lore);
             }
 
             skullStack.setItemMeta(meta);
             event.getDrops().add(skullStack);
         }
     }
 
     /******************************************
      Event Handler: First Join Events
      --------------- Core Event ----------------
      *****************************************/
 
     @EventHandler
     public void onPlayerJoinEvent(PlayerJoinEvent event) {
         if (!event.getPlayer().hasPlayedBefore()) {
             if (!this.plugin.getConfig().getConfigurationSection("preferences").getBoolean("spawnItems"))
                 return;
 
             Location location = this.parseLocation(this.plugin.getConfig().getConfigurationSection("meta").getConfigurationSection("spawnItems").getConfigurationSection("location"));
             Block block = location.getWorld().getBlockAt(location);
 
             if (block.getType() == Material.CHEST) {
                 Chest chestBlock = (Chest)block.getState();
 
                 int index = 0;
                 for (ItemStack itemStack : chestBlock.getInventory().getContents()) {
                     event.getPlayer().getInventory().setItem(index, itemStack);
 
                     if (index < 27)
                         index++;
                 }
             }
         }
     }
 
     /******************************************
      External Getter: Returns World Guard Plugin
      ---------- Dependency Convenience ---------
      ******************************************/
 
     private WorldGuardPlugin getWorldGuard() {
         Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
 
         // WorldGuard may not be loaded
         if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
             return null; // Maybe you want throw an exception instead
         }
 
         return (WorldGuardPlugin) plugin;
     }
 
     /*********************************************
      ------------ Convenience Methods ------------
      *********************************************/
 
     public Location parseLocation(ConfigurationSection locationSection) {
         Location location = new Location(Bukkit.getWorld(locationSection.getString("world")), locationSection.getDouble("x"), locationSection.getDouble("y"), locationSection.getDouble("z"), Float.parseFloat(locationSection.getString("yaw")), Float.parseFloat(locationSection.getString("pitch")));
 
         return location;
     }
 
     public boolean getRandomBoolean() {
         Random random = new Random();
         int min = 3; // Bias it to be 5:2::true:false.
         int max = 10;
 
         // nextInt is normally exclusive of the top value,
         // so add 1 to make it inclusive
         int randomNum = random.nextInt(max - min + 1) + min;
 
         return randomNum > 5;
     }
 
     private void displaySmokeInWorldAtLocation(World world, Location location) {
         world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
     }
 
     // Overloaded function to cut down on calling arguments, no default parameters in Java =/
     private static String getFriendlyDate(Calendar theDate)
     {
         return getFriendlyDate(theDate, false);
     }
 
     // Function to get a human readable version of a Calendar object
     // If verbose is true we slightly expand the date wording
     private static String getFriendlyDate(Calendar theDate, boolean verbose)
     {
         int year       = theDate.get(Calendar.YEAR);
         int month      = theDate.get(Calendar.MONTH);
         int dayOfMonth = theDate.get(Calendar.DAY_OF_MONTH);
         int dayOfWeek  = theDate.get(Calendar.DAY_OF_WEEK);
 
         // Get the day of the week as a String.
         // Note: The Calendar DAY_OF_WEEK property is NOT zero-based, and Sunday is the first day of week.
         String friendly = "";
         switch (dayOfWeek)
         {
             case 1:
                 friendly = "Sunday";
                 break;
             case 2:
                 friendly = "Monday";
                 break;
             case 3:
                 friendly = "Tuesday";
                 break;
             case 4:
                 friendly = "Wednesday";
                 break;
             case 5:
                 friendly = "Thursday";
                 break;
             case 6:
                 friendly = "Friday";
                 break;
             case 7:
                 friendly = "Saturday";
                 break;
             default:
                 friendly = "BadDayValue";
                 break;
         }
 
         // Add padding and the prefix to the day of month
         if (verbose == true)
         {
             friendly += " the " + dayOfMonth;
         }
         else
         {
             friendly += ", " + dayOfMonth;
         }
 
         String dayString = String.valueOf(dayOfMonth);   // Convert dayOfMonth to String using valueOf
 
         // Suffix is "th" for day of day of month values ending in 0, 4, 5, 6, 7, 8, and 9
         if (dayString.endsWith("0") || dayString.endsWith("4") || dayString.endsWith("5") || dayString.endsWith("6") ||
                 dayString.endsWith("7") || dayString.endsWith("8") || dayString.endsWith("9") || dayString.equals("13"))
         {
             friendly += "th ";
         } else if (dayString.endsWith("1"))
         {
             friendly += "st ";
         } else if (dayString.endsWith("2"))
         {
             friendly += "nd ";
         } else if (dayString.endsWith("3"))
         {
             friendly += "rd ";
         }
 
         // Add more padding if we've been asked to be verbose
         if (verbose == true)
         {
             friendly += "of ";
         }
 
 
         // Get a friendly version of the month.
         // Note: The Calendar MONTH property is zero-based to increase the chance of developers making mistakes.
         switch (month)
         {
             case 0:
                 friendly += "January";
                 break;
             case 1:
                 friendly += "February";
                 break;
             case 2:
                 friendly += "March";
                 break;
             case 3:
                 friendly += "April";
                 break;
             case 4:
                 friendly += "May";
                 break;
             case 5:
                 friendly += "June";
                 break;
             case 6:
                 friendly += "July";
                 break;
             case 7:
                 friendly += "August";
                 break;
             case 8:
                 friendly += "September";
                 break;
             case 9:
                 friendly += "October";
                 break;
             case 10:
                 friendly += "November";
                 break;
             case 11:
                 friendly += "December";
                 break;
             default:
                 friendly += "BadMonthValue";
                 break;
         }
 
         // Tack on the year and we're done. Phew!
         friendly += " " + year;
 
         return friendly;
 
     } // End of getFriendlyDate function
 }
 
