 package com.norcode.bukkit.buildinabox;
 
 import com.norcode.bukkit.buildinabox.datastore.DataStore;
 import com.norcode.bukkit.buildinabox.datastore.YamlDataStore;
 import com.norcode.bukkit.buildinabox.listeners.BlockProtectionListener;
 import com.norcode.bukkit.buildinabox.listeners.ItemListener;
 import com.norcode.bukkit.buildinabox.listeners.PlayerListener;
 import com.norcode.bukkit.buildinabox.listeners.ServerListener;
 import com.norcode.bukkit.buildinabox.util.MessageFile;
 import com.norcode.bukkit.schematica.Session;
 import fr.neatmonster.nocheatplus.NoCheatPlus;
 import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
 import net.h31ix.anticheat.Anticheat;
 import net.h31ix.anticheat.api.AnticheatAPI;
 import net.h31ix.anticheat.manage.CheckType;
 import net.h31ix.updater.Updater;
 import net.h31ix.updater.Updater.UpdateType;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.ChatColor;
 
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.scheduler.BukkitTask;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 
 public class BuildInABox extends JavaPlugin implements Listener {
     public static String LORE_PREFIX = ChatColor.DARK_GREEN + "" + ChatColor.DARK_RED + "" + ChatColor.DARK_GRAY + "" + ChatColor.DARK_BLUE;
     public static String LORE_HEADER = ChatColor.GOLD + "Build-in-a-Box";
     private static BuildInABox instance;
     private DataStore datastore = null;
     public Updater updater = null;
     private BukkitTask inventoryScanTask;
     private MessageFile messages = null;
     private Economy economy = null;
     private Anticheat antiCheat;
     private NoCheatPlus NCP;
     private BuildManager buildManager;
     private BukkitTask buildManagerTask;
     public Permission wildcardGivePerm;
     public Permission wildcardPlacePerm;
     public Permission wildcardPickupPerm;
     public Permission wildcardLockPerm;
     public Permission wildcardUnlockPerm;
     public BIABConfig cfg;
     public Random random = new Random();
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
         getConfig().options().copyDefaults(true);
         reloadConfig();
         cfg = new BIABConfig(this);
         cfg.reload();
         enableEconomy();
         setupAntiCheat();
         loadMessages();
         LORE_HEADER = getMsg("display-name"); 
         doUpdater();
         new File(getDataFolder(), "schematics").mkdir();
         setupPermissions();
         if (initializeDataStore()) {
             getServer().getPluginCommand("biab").setExecutor(new BIABCommandExecutor(this));
             getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
             getServer().getPluginManager().registerEvents(new ItemListener(this), this);
             getServer().getPluginManager().registerEvents(new ServerListener(this), this);
             if (cfg.isBuildingProtectionEnabled()) {
                 getServer().getPluginManager().registerEvents(new BlockProtectionListener(), this);
             }
         }
         if (cfg.isCarryEffectEnabled()) {
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
                         if (p != null && p.isOnline() && !p.isDead()) {
                             checkCarrying(p);
                         }
                         listIdx++;
                     }
                     
                 }
             }, 20, 20);
         }
         buildManager = new BuildManager(this, cfg.getMaxBlocksPerTick());
         buildManagerTask = getServer().getScheduler().runTaskTimer(this, buildManager, 1, 1);
     }
 
     private void setupPermissions() {
         PluginManager pm = getServer().getPluginManager();
         Permission adminPerm = new Permission("biab.admin", "Default set of admin permissions", PermissionDefault.OP);
         pm.addPermission(adminPerm);
         wildcardGivePerm = new Permission("biab.give.*", "Permission to 'give' any BIAB.", PermissionDefault.OP);
         wildcardGivePerm.addParent(adminPerm, true);
         pm.addPermission(wildcardGivePerm);
 
         wildcardPlacePerm = new Permission("biab.place.*", "Permission to 'place' any BIAB.", PermissionDefault.OP);
         wildcardPlacePerm.addParent(adminPerm, true);
         pm.addPermission(wildcardPlacePerm);
 
         wildcardPickupPerm = new Permission("biab.pickup.*", "Permission to 'pickup' and BIAB.", PermissionDefault.OP);
         wildcardPickupPerm.addParent(adminPerm, true);
         pm.addPermission(wildcardPickupPerm);
 
         wildcardLockPerm = new Permission("biab.lock.*", "Permission to lock any BIAB.", PermissionDefault.OP);
         wildcardLockPerm.addParent(adminPerm, true);
         pm.addPermission(wildcardLockPerm);
 
         wildcardUnlockPerm = new Permission("biab.unlock.*", "Permission to lock any BIAB.", PermissionDefault.OP);
         wildcardUnlockPerm.addParent(adminPerm, true);
         pm.addPermission(wildcardUnlockPerm);
     }
 
     private void setupAntiCheat() {
         if(getServer().getPluginManager().getPlugin("AntiCheat") != null)
         {
             antiCheat = (Anticheat) getServer().getPluginManager().getPlugin("AntiCheat");
         }
         if (getServer().getPluginManager().getPlugin("NoCheatPlus") != null) {
             NCP = (NoCheatPlus) getServer().getPluginManager().getPlugin("NoCheatPlus");
         }
     }
 
     public void exemptPlayer(Player p) {
         if (antiCheat != null) {
             AnticheatAPI.exemptPlayer(p, CheckType.FAST_PLACE);
             AnticheatAPI.exemptPlayer(p, CheckType.LONG_REACH);
         }
         if (NCP != null) {
             NCPExemptionManager.exemptPermanently(p, fr.neatmonster.nocheatplus.checks.CheckType.BLOCKPLACE);
         }
     }
 
     public void unexemptPlayer(Player p) {
         if (antiCheat != null) {
             AnticheatAPI.unexemptPlayer(p, CheckType.FAST_PLACE);
             AnticheatAPI.unexemptPlayer(p, CheckType.LONG_REACH);
         }
         if (NCP != null) {
             NCPExemptionManager.unexempt(p, fr.neatmonster.nocheatplus.checks.CheckType.BLOCKPLACE);
         }
     }
 
 
     public void enableEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return;
         }
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
             BuildInABox.getInstance().debug("Found Vault!");
         }
     }
 
 
     public void disableEconomy() {
         economy = null;
     }
 
     public static Economy getEconomy() {
         return instance.economy;
     }
 
     public static boolean hasEconomy() {
         return instance.economy != null;
     }
 
     private void loadMessages() {
         String lang = cfg.getLanguage();
         File tDir = new File(getDataFolder(), "lang");
         if (!tDir.exists()) {
             tDir.mkdir();
         }
         messages = new MessageFile(this, "lang/" + lang + ".yml");
         messages.saveDefaultConfig();
         messages.reloadConfig();
         FileConfiguration cfg = messages.getConfig();
         cfg.options().copyDefaults(true);
         messages.saveConfig();
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
         return new MessageFormat(ChatColor.translateAlternateColorCodes('&', tpl)).format(args);
     }
 
     public void removeCarryEffect(Player p) {
         p.removeMetadata("biab-carryeffect", getInstance());
         p.removePotionEffect(cfg.getCarryEffect());
     }
 
     public boolean hasCarryEffect(Player p) {
         return p.hasMetadata("biab-carryeffect");
     }
 
     public void applyCarryEffect(Player p) {
         p.setMetadata("biab-carryeffect", new FixedMetadataValue(getInstance(), true));
         p.addPotionEffect(new PotionEffect(cfg.getCarryEffect(), 1200, 1));
     }
 
     public Session getPlayerSession(Player p) {
         Session session;
         if (p.hasMetadata("biab-selection-session")) {
             session = (Session) p.getMetadata("biab-selection-session").get(0).value();
         } else {
             session = new Session(p.getName());
             p.setMetadata("biab-selection-session", new FixedMetadataValue(this, session));
 
         }
         return session;
     }
 
 
     private boolean initializeDataStore() {
 
         if (cfg.getStorageBackend().equals(BIABConfig.StorageBackend.FILE)) {
             datastore = new YamlDataStore(this);
         } else {
             getLogger().severe("No datastore configured.");
             return false;
         }
         datastore.load();
         long now = System.currentTimeMillis();
         long expiry = cfg.getDataExpiry();
         long tooOldTime = now - expiry;// if the chest hasn't been touched in 90 days expire the data
         for (ChestData cd: new ArrayList<ChestData>(datastore.getAllChests())) {
 
             if (cd.getWorldName() == null) {
                 if (cd.getLastActivity() < tooOldTime) {
                     debug("Chest Data is too old: " + cd.getLastActivity() + " vs " + tooOldTime);
                     datastore.deleteChest(cd.getId());
                 }
                continue;
             }
         }
 
         return true;
     }
 
     public BuildManager getBuildManager() {
         return buildManager;
     }
 
     @Override
     public void onDisable() {
         this.buildManager.finishSafely();
         PluginManager pm = getServer().getPluginManager();
         pm.removePermission("biab.admin");
         pm.removePermission(wildcardGivePerm);
         pm.removePermission(wildcardPlacePerm);
         pm.removePermission(wildcardPickupPerm);
         pm.removePermission(wildcardLockPerm);
         pm.removePermission(wildcardUnlockPerm);
         for (BuildingPlan plan: getDataStore().getAllBuildingPlans()) {
             plan.unregisterPermissions();
         }
         getDataStore().save();
         if (inventoryScanTask != null) {
             inventoryScanTask.cancel();
         }
     }
 
 
 
     public void doUpdater() {
 
         if (cfg.getAutoUpdate().equals(BIABConfig.AutoUpdate.TRUE)) {
             updater = new Updater(this, "build-in-a-box", this.getFile(), UpdateType.DEFAULT, true);
         } else if (cfg.getAutoUpdate().equals(BIABConfig.AutoUpdate.FALSE)) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, "build-in-a-box", this.getFile(), UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public void debug(String s) {
         if (cfg.isDebugModeEnabled()) {
             getLogger().info(s);
         }
     }
 
     public DataStore getDataStore() {
         return datastore;
     }
 
     public static BuildInABox getInstance() {
         return instance;
     }
 
     public void checkCarrying(Player p) {
         if (!cfg.isCarryEffectEnabled()) return;
         ChestData data;
         boolean effect = false;
         for (ItemStack stack: p.getInventory().getContents()) {
             data = getDataStore().fromItemStack(stack);
             if (data != null) {
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
 
     public static String getNormalMsg(String key, Object... args) {
         return getMsg("message-prefix", LORE_HEADER) + ChatColor.GRAY + getMsg(key, args);
     }
 
     public static String getErrorMsg(String key, Object... args) {
         return getMsg("message-prefix", LORE_HEADER) + ChatColor.RED + getMsg(key, args);
     }
 
     public static String getSuccessMsg(String key, Object... args) {
         return getMsg("message-prefix", LORE_HEADER) + ChatColor.GREEN + getMsg(key, args);
     }
 
    public void installDDL() {
        super.installDDL();
    }
 
        @SuppressWarnings("incomplete-switch")
     public static int getRotationDegrees(BlockFace from, BlockFace to) {
         switch (from) {
             case NORTH:
                 switch (to) {
                     case NORTH:
                         return 0;
                     case EAST:
                         return 90;
                     case SOUTH:
                         return 180;
                     case WEST:
                         return 270;
                 }
                 break;
             case EAST:
                 switch (to) {
                     case NORTH:
                         return 270;
                     case EAST:
                         return 0;
                     case SOUTH:
                         return 90;
                     case WEST:
                         return 180;
                 }
                 break;
             case SOUTH:
                 switch (to) {
                     case NORTH:
                         return 180;
                     case EAST:
                         return 270;
                     case SOUTH:
                         return 0;
                     case WEST:
                         return 90;
                 }
                 break;
 
             case WEST:
                 switch (to) {
                     case NORTH:
                         return 90;
                     case EAST:
                         return 180;
                     case SOUTH:
                         return 270;
                     case WEST:
                         return 0;
                 }
                 break;
             default:
                 return 0;
         }
         return 0;
     }
 }
