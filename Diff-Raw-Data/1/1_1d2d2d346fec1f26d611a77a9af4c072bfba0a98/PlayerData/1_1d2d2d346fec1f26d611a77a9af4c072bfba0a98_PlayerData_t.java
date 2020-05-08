 package me.limebyte.battlenight.core.util.player;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.util.Teleporter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.util.Vector;
 
 public class PlayerData {
     public static BattleNightAPI api;
     private static Map<String, PlayerData> storage = new HashMap<String, PlayerData>();
 
     private Set<String> vanishedPlayers = new HashSet<String>();
 
     private Collection<PotionEffect> potionEffects;
 
     private boolean allowFlight;
 
     private Location compassTarget;
 
     private String displayName;
 
     private ItemStack[] enderItems;
     private float exaustion;
     private float exp;
     private float fallDistance;
     private int fireTicks;
     private float flySpeed;
     private int foodLevel;
     private GameMode gameMode;
     private double health;
     private ItemStack[] invItems;
     private ItemStack[] invArmour;
     private int level;
     private Location location;
     private double maxHealth;
     private String playerListName;
     private long playerTimeOffset;
     private int remainingAir;
     private float saturation;
     private int ticksLived;
     private Vector velocity;
     private float walkSpeed;
     private boolean flying;
     private boolean playerTimeRelative;
     private boolean sleepingIgnored;
     private boolean sneaking;
     private boolean sprinting;
 
     private PlayerData() {
 
     }
 
     public static Location getSavedLocation(Player player) {
         String name = player.getName();
         if (!storage.containsKey(name)) return null;
         return storage.get(name).location;
     }
 
     public static void reset(Player player) {
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
             if (!player.canSee(p)) {
                 player.showPlayer(p);
             }
         }
 
         for (PotionEffect effect : player.getActivePotionEffects()) {
             player.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
         }
 
         player.setFlying(false);
         player.setAllowFlight(false);
         player.getEnderChest().clear();
         player.setExhaustion(0);
         player.setExp(0);
         player.setFallDistance(0);
         player.setFireTicks(0);
         player.setFlySpeed(0.1F);
         player.setFoodLevel(20);
         if (player.getGameMode() != GameMode.SURVIVAL) {
             player.setGameMode(GameMode.SURVIVAL);
         }
        player.setMaxHealth(20);
         player.setHealth(20);
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
         player.setLevel(0);
         player.setMaxHealth(20);
         String pListName = ChatColor.GRAY + "[BN] " + player.getName();
         player.setPlayerListName(pListName.length() < 16 ? pListName : pListName.substring(0, 16));
         player.resetPlayerTime();
         player.setRemainingAir(player.getMaximumAir());
         player.setSaturation(20);
         player.setTicksLived(1);
         player.setVelocity(new Vector());
         player.setWalkSpeed(0.2F);
         player.setSleepingIgnored(true);
         player.setSneaking(false);
         player.setSprinting(false);
 
         // TODO if (DEFAULT !=null) player.setTexturePack(DEFAULT);
     }
 
     public static boolean restore(Player player, boolean teleport, boolean keepInMemory) {
         String name = player.getName();
         if (!storage.containsKey(name)) {
             api.getMessenger().debug(Level.SEVERE, "Failed to restore " + name + "!");
             return false;
         }
 
         PlayerData data = storage.get(name);
 
         if (teleport) {
             Waypoint wp = api.getArenaManager().getExit();
             if (ConfigManager.get(Config.MAIN).getBoolean("ExitWaypoint", false) && wp.isSet()) {
                 Teleporter.tp(player, wp.getLocation());
             } else {
                 Teleporter.tp(player, data.location);
             }
         }
 
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
             if (data.vanishedPlayers.contains(p.getName())) {
                 player.hidePlayer(p);
             } else {
                 player.showPlayer(p);
             }
         }
 
         for (PotionEffect effect : player.getActivePotionEffects()) {
             player.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
         }
 
         player.addPotionEffects(data.potionEffects);
         player.setAllowFlight(data.allowFlight);
 
         player.setCompassTarget(data.compassTarget);
         player.setDisplayName(data.displayName);
         player.getEnderChest().setContents(data.enderItems);
         player.setExhaustion(data.exaustion);
         player.setExp(data.exp);
         player.setFallDistance(data.fallDistance);
         player.setFireTicks(data.fireTicks);
         player.setFlySpeed(data.flySpeed);
         player.setFoodLevel(data.foodLevel);
 
         GameMode gamemode = data.gameMode;
         if (player.getGameMode() != gamemode) {
             player.setGameMode(gamemode);
         }
 
         player.setMaxHealth(data.maxHealth);
         player.setHealth(data.health);
         player.getInventory().setContents(data.invItems);
         player.getInventory().setArmorContents(data.invArmour);
         player.setLevel(data.level);
         player.setPlayerListName(data.playerListName);
 
         if (!data.playerTimeRelative) {
             player.setPlayerTime(data.playerTimeOffset, true);
         } else {
             player.resetPlayerTime();
         }
 
         player.setRemainingAir(data.remainingAir);
         player.setSaturation(data.saturation);
         player.setTicksLived(data.ticksLived);
         player.setVelocity(data.velocity);
         player.setWalkSpeed(data.walkSpeed);
         player.setFlying(data.flying);
         player.setSleepingIgnored(data.sleepingIgnored);
         player.setSneaking(data.sneaking);
         player.setSprinting(data.sprinting);
 
         if (!keepInMemory) {
             storage.remove(name);
         }
         return true;
     }
 
     public static boolean storageContains(Player player) {
         return storage.containsKey(player.getName());
     }
 
     public static void store(Player player) {
         PlayerData data = new PlayerData();
 
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
             if (!player.canSee(p)) {
                 data.vanishedPlayers.add(p.getName());
             }
         }
 
         data.potionEffects = player.getActivePotionEffects();
         data.allowFlight = player.getAllowFlight();
         data.compassTarget = player.getCompassTarget();
         data.displayName = player.getDisplayName();
         data.enderItems = player.getEnderChest().getContents();
         data.exaustion = player.getExhaustion();
         data.exp = player.getExp();
         data.fallDistance = player.getFallDistance();
         data.fireTicks = player.getFireTicks();
         data.flySpeed = player.getFlySpeed();
         data.foodLevel = player.getFoodLevel();
         data.gameMode = player.getGameMode();
         data.health = player.getHealth();
         data.invItems = player.getInventory().getContents();
         data.invArmour = player.getInventory().getArmorContents();
         data.level = player.getLevel();
         data.location = player.getLocation().clone();
         data.maxHealth = player.getMaxHealth();
         data.playerListName = player.getPlayerListName();
         data.playerTimeOffset = player.getPlayerTimeOffset();
         data.remainingAir = player.getRemainingAir();
         data.saturation = player.getSaturation();
         data.ticksLived = player.getTicksLived();
         data.velocity = player.getVelocity();
         data.walkSpeed = player.getWalkSpeed();
         data.flying = player.isFlying();
         data.playerTimeRelative = player.isPlayerTimeRelative();
         data.sleepingIgnored = player.isSleepingIgnored();
         data.sneaking = player.isSneaking();
         data.sprinting = player.isSprinting();
 
         storage.put(player.getName(), data);
     }
 }
