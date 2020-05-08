 package me.cnaude.plugin.TrophyHeads;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.SkullType;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Skeleton.SkeletonType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.CraftingInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author cnaude
  */
 public class THMain extends JavaPlugin implements Listener {
 
     public static String LOG_HEADER;
     static final Logger log = Logger.getLogger("Minecraft");
     private static Random randomGenerator;
     private File pluginFolder;
     private File configFile;
     private static final ArrayList<String> deathTypes = new ArrayList<String>();
     private static boolean debugEnabled = false;
     private static boolean renameEnabled = false;
     private static boolean playerSkin = true;
     private static boolean sneakPunchInfo = true;
     private static boolean noBreak = true;
     private static final EnumMap<EntityType, List<String>> itemsRequired = new EnumMap<EntityType, List<String>>(EntityType.class);
     private static final EnumMap<EntityType, Integer> dropChances = new EnumMap<EntityType, Integer>(EntityType.class);
     private static final EnumMap<EntityType, String> customSkins = new EnumMap<EntityType, String>(EntityType.class);
     private static final EnumMap<EntityType, String> skullMessages = new EnumMap<EntityType, String>(EntityType.class);
     private static Material renameItem = Material.PAPER;
 
     @Override
     public void onEnable() {
         LOG_HEADER = "[" + this.getName() + "]";
         randomGenerator = new Random();
         pluginFolder = getDataFolder();
         configFile = new File(pluginFolder, "config.yml");
         createConfig();
         this.getConfig().options().copyDefaults(true);
         saveConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);
         getCommand("headspawn").setExecutor(this);
 
         if (renameEnabled) {
             ItemStack resultHead = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
             ShapelessRecipe shapelessRecipe = new ShapelessRecipe(resultHead);
             shapelessRecipe.addIngredient(1, Material.SKULL_ITEM, -1);
             shapelessRecipe.addIngredient(1, renameItem, -1);
             getServer().addRecipe(shapelessRecipe);
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (player.hasPermission("trophyheads.spawn")) {
                 String pName = player.getName();
                 int count = 1;
                 if (args.length >= 1) {
                     pName = args[0];
                     if (args.length == 2) {
                         if (args[1].matches("\\d+")) {
                             count = Integer.parseInt(args[1]);
                         }
                     }
                 }
                 ItemStack item = new ItemStack(Material.SKULL_ITEM, count, (byte) 3);
                 Location loc = player.getLocation().clone();
                 World world = loc.getWorld();
                 ItemMeta itemMeta = item.getItemMeta();
                 ((SkullMeta) itemMeta).setOwner(pName);
                 item.setItemMeta(itemMeta);
                 if (player.getInventory().firstEmpty() > -1) {
                    player.sendMessage("Placed " + ChatColor.GOLD 
                             + pName + "'s head " + ChatColor.RESET + " in your inventory.");
                     player.getInventory().setItem(player.getInventory().firstEmpty(), item);
                 } else {
                    player.sendMessage("Dropped " + ChatColor.GOLD 
                            + pName + "'s head" + ChatColor.RESET 
                             + " on the ground because your inventory was full.");
                     world.dropItemNaturally(loc, item);
                 }
 
             } else {
                 player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
 
             }
         } else {
             sender.sendMessage("Only a player can use this command!");
         }
         return true;
     }
 
     public EntityType getCustomSkullType(String name) {
         for (EntityType et : customSkins.keySet()) {
             if (customSkins.get(et).equals(name)) {
                 return et;
             }
         }
         return EntityType.UNKNOWN;
     }
 
     @EventHandler
     public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
         if (!renameEnabled) {
             return;
         }
         if (event.getRecipe() instanceof Recipe) {
             CraftingInventory ci = event.getInventory();
             ItemStack result = ci.getResult();
             if (result == null) {
                 return;
             }
             if (result.getType().equals(Material.SKULL_ITEM)) {
                 for (ItemStack i : ci.getContents()) {
                     if (i.getType().equals(Material.SKULL_ITEM)) {
                         if (i.getData().getData() != (byte) 3) {
                             ci.setResult(new ItemStack(0));
                             return;
                         }
                     }
                 }
                 for (ItemStack i : ci.getContents()) {
                     if (i.hasItemMeta() && i.getType().equals(renameItem)) {
                         ItemMeta im = i.getItemMeta();
                         if (im.hasDisplayName()) {
                             ItemStack res = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                             ItemMeta itemMeta = res.getItemMeta();
                             ((SkullMeta) itemMeta).setOwner(im.getDisplayName());
                             res.setItemMeta(itemMeta);
                             ci.setResult(res);
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerInteractEvent(PlayerInteractEvent event) {
         if (!sneakPunchInfo) {
             return;
         }
         Player player = event.getPlayer();
         if (!player.isSneaking()) {
             return;
         }
         if (!player.hasPermission("trophyheads.info")) {
             return;
         }
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             org.bukkit.block.Block block = event.getClickedBlock();
             if (block.getType() == Material.SKULL) {
                 BlockState bs = block.getState();
                 org.bukkit.block.Skull skull = (org.bukkit.block.Skull) bs;
                 String pName = "Unknown";
                 String message;
                 if (skull.getSkullType().equals(SkullType.PLAYER)) {
                     if (skull.hasOwner()) {
                         pName = skull.getOwner();
                         if (customSkins.containsValue(pName)) {
                             message = skullMessages.get(getCustomSkullType(pName));
                         } else {
                             message = skullMessages.get(EntityType.PLAYER);
                         }
                     } else {
                         message = skullMessages.get(EntityType.PLAYER);
                     }
                 } else if (skull.getSkullType().equals(SkullType.CREEPER)) {
                     message = skullMessages.get(EntityType.CREEPER);
                 } else if (skull.getSkullType().equals(SkullType.SKELETON)) {
                     message = skullMessages.get(EntityType.SKELETON);
                 } else if (skull.getSkullType().equals(SkullType.WITHER)) {
                     message = skullMessages.get(EntityType.WITHER);
                 } else if (skull.getSkullType().equals(SkullType.ZOMBIE)) {
                     message = skullMessages.get(EntityType.ZOMBIE);
                 } else {
                     message = skullMessages.get(EntityType.PLAYER);
                 }
                 message = message.replaceAll("%%NAME%%", pName);
                 message = ChatColor.translateAlternateColorCodes('&', message);
                 player.sendMessage(message);
                 event.setCancelled(noBreak);
             }
         }
     }
 
     public boolean isValidItem(EntityType et, Material mat) {
         if (et == null || mat == null) {
             return false;
         }
         if (itemsRequired.containsKey(et)) {
             if (itemsRequired.get(et).contains("ANY")) {
                 return true;
             }
             if (itemsRequired.get(et).contains(String.valueOf(mat.getId()))) {
                 return true;
             } else {
                 for (String s : itemsRequired.get(et)) {
                     if (s.toUpperCase().equals(mat.toString())) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     @EventHandler
     public void onPlayerDeathEvent(PlayerDeathEvent event) {
         Player player = (Player) event.getEntity();
         if (!player.hasPermission("trophyheads.drop")) {
             return;
         }
         if (randomGenerator.nextInt(100) >= dropChances.get(EntityType.PLAYER)) {
             return;
         }
 
         boolean dropOkay = false;
         DamageCause dc;
         if (player.getLastDamageCause() != null) {
             dc = player.getLastDamageCause().getCause();
             logDebug("DamageCause: " + dc.toString());
         } else {
             logDebug("DamageCause: NULL");
             return;
         }
 
         if (deathTypes.contains(dc.toString())) {
             dropOkay = true;
         }
         if (deathTypes.contains("ALL")) {
             dropOkay = true;
         }
 
         if (player.getKiller() instanceof Player) {
             if (deathTypes.contains("PVP")) {
                 dropOkay = isValidItem(EntityType.PLAYER, player.getKiller().getItemInHand().getType());
             }
         }
 
         if (dropOkay) {
             logDebug("Match: true");
             Location loc = player.getLocation().clone();
             World world = loc.getWorld();
             String pName = player.getName();
 
             ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
             ItemMeta itemMeta = item.getItemMeta();
             ArrayList<String> itemDesc = new ArrayList<String>();
             itemMeta.setDisplayName("Head of " + pName);
             itemDesc.add(event.getDeathMessage());
             itemMeta.setLore(itemDesc);
             if (playerSkin) {
                 ((SkullMeta) itemMeta).setOwner(pName);
             }
             item.setItemMeta(itemMeta);
             world.dropItemNaturally(loc, item);
         } else {
             logDebug("Match: false");
         }
     }
 
     @EventHandler
     public void onBlockBreakEvent(BlockBreakEvent event) {
         if (event.isCancelled()) {
             return;
         }
         org.bukkit.block.Block block = event.getBlock();
         if (event.getPlayer() instanceof Player) {
             if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                 return;
             }
         }
         if (block.getType() == Material.SKULL) {
             org.bukkit.block.Skull skull = (org.bukkit.block.Skull) block.getState();
             if (skull.getSkullType().equals(SkullType.PLAYER)) {
                 if (skull.hasOwner()) {
                     String pName = skull.getOwner();
                     if (customSkins.containsValue(pName)) {
                         Location loc = block.getLocation().clone();
                         event.setCancelled(true);
                         block.setType(Material.AIR);
                         ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                         ItemMeta itemMeta = item.getItemMeta();
                         ((SkullMeta) itemMeta).setOwner(pName);
                         itemMeta.setDisplayName(ChatColor.GREEN + getCustomSkullType(pName).getName() + " Head");
                         item.setItemMeta(itemMeta);
 
                         World world = loc.getWorld();
                         world.dropItemNaturally(loc, item);
                     }
 
                 }
             }
         }
     }
 
     public void setSkullName(ItemStack item, String name) {
         ItemMeta itemMeta = item.getItemMeta();
         ((SkullMeta) itemMeta).setOwner(name);
         itemMeta.setDisplayName(name + " Head");
         item.setItemMeta(itemMeta);
     }
 
     @EventHandler
     public void onEntityDeathEvent(EntityDeathEvent event) {
         EntityType et = event.getEntityType();
         Entity e = event.getEntity();
         int sti;
         boolean dropOkay;
 
         Player player;
         Material mat = Material.AIR;
         if (((LivingEntity) e).getKiller() instanceof Player) {
             player = (Player) ((LivingEntity) e).getKiller();
             mat = player.getItemInHand().getType();
         }
 
         dropOkay = isValidItem(et, mat);
 
         if (et.equals(EntityType.SKELETON)) {
             if (((Skeleton) e).getSkeletonType().equals(SkeletonType.NORMAL)) {
                 if (randomGenerator.nextInt(100) >= dropChances.get(et)) {
                     return;
                 }
                 sti = 0;
             } else {
                 return;
             }
         } else if (et.equals(EntityType.ZOMBIE)) {
             if (randomGenerator.nextInt(100) >= dropChances.get(et)) {
                 return;
             }
             sti = 2;
         } else if (et.equals(EntityType.CREEPER)) {
             if (randomGenerator.nextInt(100) >= dropChances.get(et)) {
                 return;
             }
             sti = 4;
         } else if (dropChances.containsKey(et)) {
             if (randomGenerator.nextInt(100) >= dropChances.get(et)) {
                 return;
             }
             sti = 3;
         } else {
             return;
         }
 
         if (!dropOkay) {
             return;
         }
 
         ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) sti);
 
         if (sti == 3 || customSkins.containsKey(et)) {
             if (customSkins.get(et).equalsIgnoreCase("@default")) {
                 logDebug("Dropping default head for " + et.getName());
             } else {
                 ItemMeta itemMeta = item.getItemMeta();
                 ((SkullMeta) itemMeta).setOwner(customSkins.get(et));
                 itemMeta.setDisplayName(et.getName() + " Head");
                 item.setItemMeta(itemMeta);
                 logDebug("Dropping " + customSkins.get(et) + " head for " + et.getName());
             }
         }
 
         Location loc = e.getLocation().clone();
         World world = loc.getWorld();
         world.dropItemNaturally(loc, item);
     }
 
     private void createConfig() {
         if (!pluginFolder.exists()) {
             try {
                 pluginFolder.mkdir();
             } catch (Exception e) {
                 logError(e.getMessage());
             }
         }
 
         if (!configFile.exists()) {
             try {
                 configFile.createNewFile();
             } catch (IOException e) {
                 logError(e.getMessage());
             }
         }
     }
 
     private void loadConfig() {
         debugEnabled = getConfig().getBoolean("debug-enabled");
         logDebug("Debug enabled");
 
         dropChances.put(EntityType.PLAYER, getConfig().getInt("drop-chance"));
         logDebug("Chance to drop head: " + dropChances.get(EntityType.PLAYER) + "%");
 
         playerSkin = getConfig().getBoolean("player-skin");
         logDebug("Player skins: " + playerSkin);
 
         sneakPunchInfo = getConfig().getBoolean("sneak-punch-info");
         logDebug("Sneak punch info: " + sneakPunchInfo);
        
         noBreak = getConfig().getBoolean("sneak-punch-no-break");
         logDebug("Sneak punch no break: " + noBreak);
 
         for (String entityName : getConfig().getConfigurationSection("custom-heads").getKeys(false)) {
             logDebug("Entity Name: " + entityName);
             EntityType et;
             if (entityName.equalsIgnoreCase("golem")) {
                 et = EntityType.IRON_GOLEM;
             } else if (entityName.equalsIgnoreCase("ocelot")) {
                 et = EntityType.OCELOT;
             } else {
                 et = EntityType.fromName(entityName);
             }
             if (et == null) {
                 logError("Invalid entity: " + entityName);
                 continue;
             }
             logDebug("  Type: " + et.getName());
             int dropChance = getConfig().getInt("custom-heads." + entityName + ".drop-chance", 0);
             List<String> items = getConfig().getStringList("custom-heads." + entityName + ".items-required");
             if (items.isEmpty()) {
                 items.add("ANY");
                 items.add("276");
             }
             String skin = getConfig().getString("custom-heads." + entityName + ".skin", "MHF_" + entityName);
             String message = getConfig().getString("custom-heads." + entityName + ".message", "&eThis head once belonged to a &e" + entityName + "&e.");
 
             dropChances.put(et, dropChance);
             logDebug("  Chance to drop head: " + dropChances.get(et) + "%");
 
             itemsRequired.put(et, items);
             logDebug("  Items required: " + itemsRequired.get(et));
 
             customSkins.put(et, skin);
             logDebug("  Skin: " + customSkins.get(et));
 
             skullMessages.put(et, message);
             logDebug("  Message: " + skullMessages.get(et));
 
         }
 
         skullMessages.put(EntityType.PLAYER, getConfig().getString("message"));
 
         renameEnabled = getConfig().getBoolean("rename-enabled");
         if (renameEnabled) {
             try {
                 renameItem = Material.getMaterial(getConfig().getInt("rename-item"));
             } catch (Exception e) {
                 renameItem = Material.PAPER;
             }
             logDebug("Rename recipe enabled: head + " + renameItem.toString());
         }
         deathTypes.addAll(getConfig().getStringList("death-types"));
 
     }
 
     public void logInfo(String _message) {
         log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
     }
 
     public void logError(String _message) {
         log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
     }
 
     public void logDebug(String _message) {
         if (debugEnabled) {
             log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
         }
     }
 }
