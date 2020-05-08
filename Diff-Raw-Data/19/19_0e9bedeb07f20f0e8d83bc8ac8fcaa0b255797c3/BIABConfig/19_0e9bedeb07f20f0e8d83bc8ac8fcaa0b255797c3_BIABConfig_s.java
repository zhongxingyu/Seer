 package com.norcode.bukkit.buildinabox;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.potion.PotionEffectType;
 
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class BIABConfig {
     public static enum StorageBackend {
         FILE,
     }
     public static enum AutoUpdate {
         TRUE, FALSE, NOTIFY_ONLY
     }
     public static enum AnimationStyle {
         BREAK, SMOKE, NONE
     }
     public static class BIABConfigurationException extends Exception {
         public BIABConfigurationException(String msg) {
             super(msg);
         }
         public BIABConfigurationException(String msg, Throwable cause) {
             super(msg, cause);
         }
         public BIABConfigurationException(Throwable cause) {
             super(cause);
         }
     }
     private BuildInABox plugin;
     private String apiKey;
     private boolean debugModeEnabled;
     private AutoUpdate autoUpdate;
     private StorageBackend storageBackend;
     private String language;
     private long dataExpiry;
     private int chestBlockId;
     private int selectionWandId;
     private int doubleClickInterval;
     private int previewDuration;
     private boolean carryEffectEnabled;
     private PotionEffectType carryEffect;
     private boolean buildingProtectionEnabled;
     private boolean containerLockingEnabled;
     private boolean buildPermissionCheckEnabled;
     private boolean preventPlacingEnderchestsEnabled;
     private boolean pickupEnabled;
     private boolean lockingEnabled;
     private boolean unlockingOthersEnabled;
     private int unlockTime;
     private int unlockTimeOwn;
     private int lockTime;
     private double lockCost;
     private double unlockCost;
     private double pickupCost;
     private double buildCost;
     private int maxLockingDistance;
     private int maxBlocksPerTick;
 
     // Build Animation
     private boolean buildAnimationShuffled;
     private AnimationStyle buildAnimationStyle;
     private int buildBlocksPerTick;
     private int buildFireworks;
 
     // Pickup Animation
     private boolean pickupAnimationShuffled;
     private AnimationStyle pickupAnimationStyle;
     private int pickupBlocksPerTick;
     private int pickupFireworks;
 
 
     public BIABConfig(BuildInABox plugin) {
         this.plugin = plugin;
     }
 
     private Pattern periodPattern = Pattern.compile("(\\d+[dhms])", Pattern.CASE_INSENSITIVE);
     public long periodStringToMillis(String p) {
         Matcher m = periodPattern.matcher(p);
         String s;
         int num;
         String unit;
         long millis = 0;
         while (m.find()) {
             s = m.group();
             num = Integer.parseInt(s.substring(0,s.length()-1), 10);
             unit = s.substring(s.length()-1).toLowerCase();
             if (unit.equals("d")) {
                 millis += TimeUnit.MILLISECONDS.convert(num, TimeUnit.DAYS);
             } else if (unit.equals("h")) {
                 millis += TimeUnit.MILLISECONDS.convert(num, TimeUnit.HOURS);
             } else if (unit.equals("m")) {
                 millis += TimeUnit.MILLISECONDS.convert(num, TimeUnit.MINUTES);
             } else if (unit.equals("s")) {
                 millis += TimeUnit.MILLISECONDS.convert(num, TimeUnit.SECONDS);
             }
         }
         if (millis == 0) {
             millis = Long.MAX_VALUE;
         }
         return millis;
     }
 
     public void reload() {

         ConfigurationSection cfg = plugin.getConfig();
         apiKey = plugin.getConfig().getString("api-key");
         debugModeEnabled = cfg.getBoolean("debug");
         String au = cfg.getString("auto-update", "true");
         autoUpdate = AutoUpdate.valueOf(au.toUpperCase());
         if (autoUpdate == null) {
             autoUpdate = AutoUpdate.NOTIFY_ONLY;
         }
         storageBackend = StorageBackend.valueOf(cfg.getString("storage-backend", "file").toUpperCase());
         if (storageBackend == null) {
             storageBackend = StorageBackend.FILE;
         }
         language = cfg.getString("language", "english");
         dataExpiry = periodStringToMillis(cfg.getString("data-expiry", "90d"));
         chestBlockId = cfg.getInt("chest-block", 130);
         selectionWandId = cfg.getInt("selection-wand-id", 294);
         doubleClickInterval = cfg.getInt("double-click-interval", 2000);
         previewDuration = cfg.getInt("preview-duration", 3500);
         carryEffectEnabled = cfg.getBoolean("carry-effect", false);
         carryEffect = PotionEffectType.getByName(cfg.getString("carry-effect-type", "slow").toUpperCase());
         buildingProtectionEnabled = cfg.getBoolean("protect-buildings", true);
         containerLockingEnabled = cfg.getBoolean("lock-containers", true);
         buildPermissionCheckEnabled = cfg.getBoolean("check-build-permissions", true);
         preventPlacingEnderchestsEnabled = cfg.getBoolean("prevent-placing-enderchests", false);
         pickupEnabled = cfg.getBoolean("allow-pickup", true);
         lockingEnabled = cfg.getBoolean("allow-locking", true);
         unlockingOthersEnabled = cfg.getBoolean("allow-unlocking-others", true);
         unlockTime = cfg.getInt("unlock-time", 10);
         unlockTimeOwn = cfg.getInt("unlock-time-own", 5);
         lockTime = cfg.getInt("lock-time", 5);
         lockCost = cfg.getInt("lock-cost", 0);
         unlockCost = cfg.getInt("unlock-cost", 0);
         pickupCost = cfg.getInt("pickup-cost", 0);
         buildCost = cfg.getInt("build-cost", 0);
         maxLockingDistance = cfg.getInt("max-locking-distance", 5);
         maxBlocksPerTick = cfg.getInt("max-blocks-per-tick", 500);
         buildAnimationStyle = AnimationStyle.valueOf(cfg.getString("build-animation.style", "BREAK").toUpperCase());
         if (buildAnimationStyle == null) {
             buildAnimationStyle = AnimationStyle.NONE;
         }
         buildAnimationShuffled = cfg.getBoolean("build-animation.shuffle", true);
         buildBlocksPerTick = cfg.getInt("build-animation.blocks-per-tick", 5);
         buildFireworks = cfg.getInt("build-animation.fireworks", 3);
 
         pickupAnimationStyle = AnimationStyle.valueOf(cfg.getString("pickup-animation.style", "BREAK").toUpperCase());
         if (pickupAnimationStyle == null) {
             pickupAnimationStyle = AnimationStyle.NONE;
         }
         pickupAnimationShuffled = cfg.getBoolean("pickup-animation.shuffle", true);
         pickupBlocksPerTick = cfg.getInt("pickup-animation.blocks-per-tick", 20);
         pickupFireworks = cfg.getInt("pickup-animation.fireworks", 0);
     }
 
     public boolean isDebugModeEnabled() {
         return debugModeEnabled;
     }
 
     public void setDebugModeEnabled(boolean debugModeEnabled) {
         this.debugModeEnabled = debugModeEnabled;
     }
 
     public AutoUpdate getAutoUpdate() {
         return autoUpdate;
     }
 
     public void setAutoUpdate(AutoUpdate autoUpdate) {
         this.autoUpdate = autoUpdate;
     }
 
     public StorageBackend getStorageBackend() {
         return storageBackend;
     }
 
     public void setStorageBackend(StorageBackend storageBackend) {
         this.storageBackend = storageBackend;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public long getDataExpiry() {
         return dataExpiry;
     }
 
     public void setDataExpiry(long dataExpiry) {
         this.dataExpiry = dataExpiry;
     }
 
     public int getChestBlockId() {
         return chestBlockId;
     }
 
     public void setChestBlockId(int chestBlockId) {
         this.chestBlockId = chestBlockId;
     }
 
     public int getSelectionWandId() {
         return selectionWandId;
     }
 
     public void setSelectionWandId(int selectionWandId) {
         this.selectionWandId = selectionWandId;
     }
 
     public int getDoubleClickInterval() {
         return doubleClickInterval;
     }
 
     public void setDoubleClickInterval(int doubleClickInterval) {
         this.doubleClickInterval = doubleClickInterval;
     }
 
     public int getPreviewDuration() {
         return previewDuration;
     }
 
     public void setPreviewDuration(int previewDuration) {
         this.previewDuration = previewDuration;
     }
 
     public boolean isCarryEffectEnabled() {
         return carryEffectEnabled;
     }
 
     public void setCarryEffectEnabled(boolean carryEffectEnabled) {
         this.carryEffectEnabled = carryEffectEnabled;
     }
 
     public PotionEffectType getCarryEffect() {
         return carryEffect;
     }
 
     public void setCarryEffect(PotionEffectType carryEffect) {
         this.carryEffect = carryEffect;
     }
 
     public boolean isBuildingProtectionEnabled() {
         return buildingProtectionEnabled;
     }
 
     public void setBuildingProtectionEnabled(boolean buildingProtectionEnabled) {
         this.buildingProtectionEnabled = buildingProtectionEnabled;
     }
 
     public boolean isContainerLockingEnabled() {
         return containerLockingEnabled;
     }
 
     public void setContainerLockingEnabled(boolean containerLockingEnabled) {
         this.containerLockingEnabled = containerLockingEnabled;
     }
 
     public boolean isBuildPermissionCheckEnabled() {
         return buildPermissionCheckEnabled;
     }
 
     public void setBuildPermissionCheckEnabled(boolean buildPermissionCheckEnabled) {
         this.buildPermissionCheckEnabled = buildPermissionCheckEnabled;
     }
 
     public boolean isPreventPlacingEnderchestsEnabled() {
         return preventPlacingEnderchestsEnabled;
     }
 
     public void setPreventPlacingEnderchestsEnabled(boolean preventPlacingEnderchestsEnabled) {
         this.preventPlacingEnderchestsEnabled = preventPlacingEnderchestsEnabled;
     }
 
     public boolean isPickupEnabled() {
         return pickupEnabled;
     }
 
     public void setPickupEnabled(boolean pickupEnabled) {
         this.pickupEnabled = pickupEnabled;
     }
 
     public boolean isLockingEnabled() {
         return lockingEnabled;
     }
 
     public void setLockingEnabled(boolean lockingEnabled) {
         this.lockingEnabled = lockingEnabled;
     }
 
     public boolean isUnlockingOthersEnabled() {
         return unlockingOthersEnabled;
     }
 
     public void setUnlockingOthersEnabled(boolean unlockingOthersEnabled) {
         this.unlockingOthersEnabled = unlockingOthersEnabled;
     }
 
     public int getUnlockTime() {
         return unlockTime;
     }
 
     public void setUnlockTime(int unlockTime) {
         this.unlockTime = unlockTime;
     }
 
     public int getUnlockTimeOwn() {
         return unlockTimeOwn;
     }
 
     public void setUnlockTimeOwn(int unlockTimeOwn) {
         this.unlockTimeOwn = unlockTimeOwn;
     }
 
     public int getLockTime() {
         return lockTime;
     }
 
     public void setLockTime(int lockTime) {
         this.lockTime = lockTime;
     }
 
     public double getLockCost() {
         return lockCost;
     }
 
     public void setLockCost(double lockCost) {
         this.lockCost = lockCost;
     }
 
     public double getUnlockCost() {
         return unlockCost;
     }
 
     public void setUnlockCost(double unlockCost) {
         this.unlockCost = unlockCost;
     }
 
     public double getPickupCost() {
         return pickupCost;
     }
 
     public void setPickupCost(double pickupCost) {
         this.pickupCost = pickupCost;
     }
 
     public double getBuildCost() {
         return buildCost;
     }
 
     public void setBuildCost(double buildCost) {
         this.buildCost = buildCost;
     }
 
     public int getMaxLockingDistance() {
         return maxLockingDistance;
     }
 
     public void setMaxLockingDistance(int maxLockingDistance) {
         this.maxLockingDistance = maxLockingDistance;
     }
 
     public int getMaxBlocksPerTick() {
         return maxBlocksPerTick;
     }
 
     public void setMaxBlocksPerTick(int maxBlocksPerTick) {
         this.maxBlocksPerTick = maxBlocksPerTick;
     }
 
     public boolean isBuildAnimationShuffled() {
         return buildAnimationShuffled;
     }
 
     public void setBuildAnimationShuffled(boolean buildAnimationShuffled) {
         this.buildAnimationShuffled = buildAnimationShuffled;
     }
 
     public AnimationStyle getBuildAnimationStyle() {
         return buildAnimationStyle;
     }
 
     public void setBuildAnimationStyle(AnimationStyle buildAnimationStyle) {
         this.buildAnimationStyle = buildAnimationStyle;
     }
 
     public int getBuildBlocksPerTick() {
         return buildBlocksPerTick;
     }
 
     public void setBuildBlocksPerTick(int buildBlocksPerTick) {
         this.buildBlocksPerTick = buildBlocksPerTick;
     }
 
     public int getBuildFireworks() {
         return buildFireworks;
     }
 
     public void setBuildFireworks(int buildFireworks) {
         this.buildFireworks = buildFireworks;
     }
 
     public boolean isPickupAnimationShuffled() {
         return pickupAnimationShuffled;
     }
 
     public void setPickupAnimationShuffled(boolean pickupAnimationShuffled) {
         this.pickupAnimationShuffled = pickupAnimationShuffled;
     }
 
     public AnimationStyle getPickupAnimationStyle() {
         return pickupAnimationStyle;
     }
 
     public void setPickupAnimationStyle(AnimationStyle pickupAnimationStyle) {
         this.pickupAnimationStyle = pickupAnimationStyle;
     }
 
     public int getPickupBlocksPerTick() {
         return pickupBlocksPerTick;
     }
 
     public void setPickupBlocksPerTick(int pickupBlocksPerTick) {
         this.pickupBlocksPerTick = pickupBlocksPerTick;
     }
 
     public int getPickupFireworks() {
         return pickupFireworks;
     }
 
     public void setPickupFireworks(int pickupFireworks) {
         this.pickupFireworks = pickupFireworks;
     }
 
 }
