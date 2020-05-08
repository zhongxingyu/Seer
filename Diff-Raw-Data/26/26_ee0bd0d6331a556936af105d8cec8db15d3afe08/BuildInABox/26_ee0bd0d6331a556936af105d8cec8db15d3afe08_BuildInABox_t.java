 package com.norcode.bukkit.buildinabox;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.h31ix.updater.Updater;
 import net.h31ix.updater.Updater.UpdateType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.norcode.bukkit.buildinabox.BuildChest.UnlockingTask;
 import com.sk89q.worldedit.bukkit.BukkitWorld;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 
 public class BuildInABox extends JavaPlugin implements Listener {
     public static final String LORE_HEADER = ChatColor.GOLD + "Build-in-a-Box";
     private static BuildInABox instance;
     private DataStore datastore = null;
     private Updater updater = null;
     private long lastClicked = -1;
     private Action lastClickType = null;
     private boolean debugMode = false;
     private BukkitTask inventoryScanTask = null;
     private ConfigAccessor messages = null;
     @Override
     public void onLoad() {
         instance = this;
     }
 
     public void onUnload() {
         instance = null;
     }
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         reloadConfig();
         loadMessage();
         doUpdater();
         new File(getDataFolder(), "schematics").mkdir();
         if (initializeDataStore()) {
             getServer().getPluginCommand("biab").setExecutor(new BIABCommandExecutor(this));
             getServer().getPluginManager().registerEvents(this, this);
             getServer().getPluginManager().registerEvents(new ItemListener(this), this);
             if (getConfig().getBoolean("protect-buildings")) {
                 getServer().getPluginManager().registerEvents(new BlockProtectionListener(), this);
             }
         }
         if (getConfig().getBoolean("carry-effect", true)) {
             inventoryScanTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
                 List<String> playerNames = null;
                 int listIdx = 0;
                 public void run() {
                     if (playerNames == null || listIdx >= playerNames.size()) {
                         playerNames = new ArrayList<String>();
                         for (Player p: getServer().getOnlinePlayers()) {
                             playerNames.add(p.getName());
                             listIdx = 0;
                         }
                     }
                     if (listIdx < playerNames.size()) {
                         Player p = getServer().getPlayer(playerNames.get(listIdx));
                         ChestData data = null;
                         boolean effect = false;
                         if (p.isOnline() && !p.isDead()) {
                             checkCarrying(p);
                         }
                         listIdx++;
                     }
                     
                 }
             }, 20, 20);
         }
     }
 
     private void loadMessage() {
         String lang = getConfig().getString("language", "english").toLowerCase();
         File tDir = new File(getDataFolder(), "lang");
         if (!tDir.exists()) {
             tDir.mkdir();
         }
         File cfgFile = new File("lang", lang + ".yml");
         debug("Using translation file: " + cfgFile.getPath());
         messages = new ConfigAccessor(this, cfgFile.getPath());
         messages.reloadConfig();
     }
 
     public static String getMsg(String key, Object... args) {
         String tpl = instance.messages.getConfig().getString(key);
         if (tpl == null) {
             tpl = "[" + key + "] ";
             for (int i=0;i< args.length;i++) {
                 tpl += "{"+i+"}, ";
             }
         }
         return new MessageFormat(tpl).format(args);
     }
 
     public void removeCarryEffect(Player p) {
         p.removeMetadata("biab-carryeffect", getInstance());
         p.removePotionEffect(PotionEffectType.getByName(getConfig().getString("carry-effect-type")));
     }
     public boolean hasCarryEffect(Player p) {
         return p.hasMetadata("biab-carryeffect");
     }
     public void applyCarryEffect(Player p) {
         p.setMetadata("biab-carryeffect", new FixedMetadataValue(getInstance(), true));
         p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(getConfig().getString("carry-effect-type")), 1200, 1));
     }
 
     private boolean initializeDataStore() {
         String storageType = getConfig().getString("storage-backend", "file").toLowerCase();
         if (storageType.equals("file")) {
             datastore = new YamlDataStore(this);
             datastore.load();
             long now = System.currentTimeMillis();
             long expiry = getConfig().getLong("data-expiry", 1000*60*60*24*90L);
             long tooOldTime = now - expiry;// if the chest hasn't been touched in 90 days expire the data
             for (ChestData cd: new ArrayList<ChestData>(datastore.getAllChests())) {
                 debug("Checking Chest: " + cd.getId());
                 if (cd.getLastActivity() < tooOldTime) {
                     debug("Chest Data is too old: " + cd.getLastActivity() + " vs " + tooOldTime);
                     datastore.deleteChest(cd.getId());
                 } else {
                     if (cd.getLocation() != null) {
                         BuildChest bc = new BuildChest(cd);
                         if (bc.getBlock().getType().equals(Material.ENDER_CHEST)) {
                             bc.getBlock().setMetadata("buildInABox", new FixedMetadataValue(this, bc));
                             if (getConfig().getBoolean("protect-buildings")) {
                                 debug("Protecting Building: " + bc);
                                 bc.protectBlocks();
                             }
                         }
                     }
                 }
             }
             return true;
         }
         return false;
     }
 
     @Override
     public void onDisable() {
         getDataStore().save();
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerLogin(PlayerLoginEvent event) {
 
         if (event.getPlayer().hasPermission("biab.admin")) {
             final String playerName = event.getPlayer().getName();
             getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                 public void run() {
                     Player player = getServer().getPlayer(playerName);
                     if (player != null && player.isOnline()) {
                         getLogger().info("Updater Result: " + updater.getResult());
                         switch (updater.getResult()) {
                         case UPDATE_AVAILABLE:
                             player.sendMessage(ChatColor.GOLD + "[Build-in-a-Box] " + ChatColor.WHITE + getMsg("update-available", "http://dev.bukkit.org/server-mods/build-in-a-box/"));
                             break;
                         case SUCCESS:
                             player.sendMessage(ChatColor.GOLD + "[Build-in-a-Box] " + ChatColor.WHITE + getMsg("update-downloaded"));
                             break;
                         default:
                             // nothing
                         }
                     }
                 }
             }, 20);
         }
     }
 
     public void doUpdater() {
         String autoUpdate = getConfig().getString("auto-update", "notify-only").toLowerCase();
         if (autoUpdate.equals("true")) {
             updater = new Updater(this, "build-in-a-box", this.getFile(), UpdateType.DEFAULT, true);
         } else if (autoUpdate.equals("false")) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, "build-in-a-box", this.getFile(), UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public WorldEditPlugin getWorldEdit() {
         return (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
     }
 
     public void debug(String s) {
         if (debugMode) {
             getLogger().info(s);
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getClickedBlock().getType().equals(Material.ENDER_CHEST)) {
             if (event.getClickedBlock().hasMetadata("buildInABox")) {
                 BuildChest bc = (BuildChest) event.getClickedBlock().getMetadata("buildInABox").get(0).value();
                if (bc.isBuilding()) {
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    return;
                }
                 bc.updateActivity();
                 if (bc.isPreviewing()) {
                     if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                         // Cancel
                         bc.endPreview(event.getPlayer());
                     } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                         // build
                         bc.build(event.getPlayer());
                     }
                 } else {
                     long now = System.currentTimeMillis();
                     if (bc.isLocking()) {
                         if (!bc.getLockingTask().lockingPlayer.equals(event.getPlayer().getName())) {
                             String msgKey = "lock-attempt-cancelled";
                             if (bc.getLockingTask() instanceof UnlockingTask) {
                                 msgKey = "un" + msgKey;
                             }
                             event.getPlayer().sendMessage(ChatColor.GOLD + "[Build-in-a-Box] " + ChatColor.GRAY + getMsg(msgKey, bc.getLockingTask().lockingPlayer));
                         }
                         bc.getLockingTask().cancel();
                     } else if (now - lastClicked < 2000 && lastClickType.equals(event.getAction())) {
                         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                             // pick up
                             if (!bc.isLocked()) {
                                 bc.pickup(event.getPlayer());
                                 
                             }
                         } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                             // lock/unlock
                             if (bc.isLocked()) {
                                 bc.unlock(event.getPlayer());
                             } else {
                                 bc.lock(event.getPlayer());
                             }
                         }
                         lastClicked = -1;
                         lastClickType = null;
                     } else {
                         lastClicked = now;
                         lastClickType = event.getAction();
                         event.getPlayer().sendMessage(bc.getDescription());
                     }
                 }
                 event.setCancelled(true);
                 event.setUseInteractedBlock(Result.DENY);
                 event.setUseItemInHand(Result.DENY);
             }
         }
     }
 
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
     public void onBlockPlace(final BlockPlaceEvent event) {
         ChestData data = getDataStore().fromItemStack(event.getItemInHand());
         if (data != null) {
             data.setLocation(event.getBlock().getLocation());
             data.setLastActivity(System.currentTimeMillis());
             final BuildChest bc = new BuildChest(data);
             event.getBlock().setMetadata("buildInABox", new FixedMetadataValue(this, bc));
             event.getPlayer().getInventory().setItemInHand(null);
             getServer().getScheduler().runTaskLater(this, new Runnable() {
                 public void run() {
                     if (event.getPlayer().isOnline()) {
                         bc.preview(event.getPlayer());
                         checkCarrying(event.getPlayer());
                     }
                 }
             }, 1);
         } else {
             debug("Chest From Itemstack Failed.");
         }
     }
 
     public DataStore getDataStore() {
         return datastore;
     }
 
     public static BuildInABox getInstance() {
         return instance;
     }
 
     public void checkCarrying(Player p) {
         ChestData data;
         boolean effect = false;
         for (ItemStack stack: p.getInventory().getContents()) {
             data = getDataStore().fromItemStack(stack);
             if (data != null) {
                 applyCarryEffect(p);
                 effect = true;
                 break;
             }
         }
         if (effect) {
             applyCarryEffect(p);
         } else if (hasCarryEffect(p)) {
             removeCarryEffect(p);
         }
     }
 }
