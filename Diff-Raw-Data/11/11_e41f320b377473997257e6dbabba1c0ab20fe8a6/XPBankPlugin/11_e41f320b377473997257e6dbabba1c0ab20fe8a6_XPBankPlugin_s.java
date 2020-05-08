 package com.agmmaverick.bukkit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class XPBankPlugin extends JavaPlugin {
     // Pre-compute these values since they are static
     private static final int TOTAL_XP_LVL_15 = calcTotalXPForLevel(15);
     private static final int TOTAL_XP_LVL_30 = calcTotalXPForLevel(30);
     
     protected Logger log = Logger.getLogger("Minecraft");
     private HashMap<String, Integer> bank = new HashMap<String, Integer>();
     private Location bankLocation = null;
     private int bankRadius = 0;
     
     public void onDisable() {
         saveBank();
         
         log.info("XPBank Disabled");
     }
     
     public void onEnable() {
         loadBank();
         log.info("XPBank Enabled");
     }
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!command.getName().equals("xpbank")) {
             return true;
         }
         
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Only players are able to run that command.");
         }
         
         if (args.length == 0) {
             return false;
         }
         
         Player player  = (Player)sender;
         
         if (args[0].equals("deposit")) {
             doDeposit(player);
         } else if (args[0].equals("withdraw")) {
             Integer amount = null;
             if (args.length >= 2) {
                 try {
                     amount = Integer.parseInt(args[1]);
                 } catch (NumberFormatException e) {
                     player.sendMessage(ChatColor.RED + "'" + args[1] + " is not a valid integer");
                     return false;
                 }
             }
             doWithdraw(player, amount);
         } else if (args[0].equals("balance")) {
             doDisplayBalance(player);
         } else if (args[0].equals("current")) {
             doDisplayCurrent(player);
         } else if (args[0].equals("set")) {
             return doBankSet(player, args);
         }
         
         return true;
     }
 
     private void doDeposit(Player player) {
         if (!player.hasPermission("xpbank.deposit")) {
             player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
         }
         
         if (!isInsideBank(player)) {
             player.sendMessage(ChatColor.RED + "You must be inside of the bank to deposit XP.");
             return;
         }
         
         int xp = calcTotalXPForPlayer(player);
         
         addToPlayerBalance(player, xp);
         clearPlayerXP(player);
         
         player.sendMessage(ChatColor.BLUE + "Deposited: " + xp );
         displayBalance(player);
         
         saveBank();
     }
 
     private void doWithdraw(Player player, Integer amount) {
         if (!player.hasPermission("xpbank.withdraw")) {
             player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
         }
         
         if (!isInsideBank(player)) {
             player.sendMessage(ChatColor.RED + "You must be inside of the bank to withdraw XP.");
             return;
         }
         
         int balance = getPlayerBalance(player);
         int xpToWithdraw = balance;
         if (amount != null && amount < balance) {
             xpToWithdraw = amount;
         }
         
         addXPToPlayer(player, xpToWithdraw);
        clearPlayersBalance(player);
                 
         player.sendMessage(ChatColor.BLUE + "Withdrew: " + xpToWithdraw );
         displayCurrentXP(player);
         
         saveBank();
     }
     
     private void doDisplayCurrent(Player player) {
         if (!player.hasPermission("xpbank.current")) {
             player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
         }
         
         displayCurrentXP(player);
     }
 
     private void displayCurrentXP(Player player) {
         int xp = calcTotalXPForPlayer(player);
         player.sendMessage(ChatColor.BLUE + "Current XP: " + xp);
     }
 
     
     private void doDisplayBalance(Player player) {
         if (!player.hasPermission("xpbank.balance")) {
             player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
         }
                 
         displayBalance(player);
     }
     
     private boolean doBankSet(Player player, String[] args) {
         if (!player.hasPermission("xpbank.admin")) {
             player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
         }
         
         if (args.length <= 1) {
             return false;
         }
         
         try {
             int radius = Integer.parseInt(args[1]);
             
             bankLocation = player.getLocation();
             bankRadius = radius;
          
             player.sendMessage(ChatColor.DARK_GREEN + "Bank Location set at (" + bankLocation.getBlockX() + "," 
                     + bankLocation.getBlockY() + "," + bankLocation.getBlockZ() + ") in world '" 
                     + bankLocation.getWorld().getName() + "'");
             
             saveBank();
         } catch (NumberFormatException e) {
             player.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
         }
         
         return true;
     }
 
     private void displayBalance(Player player) {
         int balance = getPlayerBalance(player);
         player.sendMessage(ChatColor.BLUE + "Balance: " + balance);
     }
     
     private boolean isInsideBank(Player player) {
         if (bankLocation == null) {
             return false;
         }
         
         Location currentLocation = player.getLocation();
         if (!currentLocation.getWorld().equals(bankLocation.getWorld())) {
             return false;
         }
         
         return isCoordWithinRadius(currentLocation.getBlockX(), bankLocation.getBlockX(), bankRadius) &&
                 isCoordWithinRadius(currentLocation.getBlockY(), bankLocation.getBlockY(), bankRadius) &&
                 isCoordWithinRadius(currentLocation.getBlockZ(), bankLocation.getBlockZ(), bankRadius);
     }
     
     private boolean isCoordWithinRadius(int actual, int expected, int radius) {
         int min = expected - radius;
         int max = expected + radius;
         
         return actual >= min && actual <= max;
     }
     
     private int getPlayerBalance(Player player) {
         String playerName = player.getName().toLowerCase();
         
         int oldXp = 0;
         if (bank.containsKey(playerName)) {
             oldXp = bank.get(playerName);
         }
         
         return oldXp;
     }
     
     private void addToPlayerBalance(Player player, int xp) {
         String playerName = player.getName().toLowerCase();
         
         int newXp = getPlayerBalance(player) + xp;
         bank.put(playerName, newXp);
     }
     
    private void clearPlayersBalance(Player player) {
         String playerName = player.getName().toLowerCase();
         
        bank.put(playerName, 0);
     }
        
     private void addXPToPlayer(Player player, int xp) {
         CalculateLevelResult levelResult = calcLevelFromXP(calcTotalXPForPlayer(player) + xp);
         
         player.setLevel(levelResult.level);
         player.setExp(levelResult.exp);
     }
     
     private void clearPlayerXP(Player player) {
         player.setLevel(0);
         player.setExp(0.0f);
     }
     
     public void loadBank() {
         File bankYml = new File(this.getDataFolder(), "bank.yml");
         if (!bankYml.exists()) {
             return;
         }
         
         bankLocation = null;
         bank.clear();
         YamlConfiguration config = YamlConfiguration.loadConfiguration(bankYml);
         
         if (config.contains("bank")) {
             int x = config.getInt("bank.x");
             int y = config.getInt("bank.y");
             int z = config.getInt("bank.z");
             UUID worldUuid = UUID.fromString(config.getString("bank.world"));
             World world = Bukkit.getServer().getWorld(worldUuid);
             
             if (world == null) {
                 log.log(Level.SEVERE, "XPBank: The world with UUID " + worldUuid + " does not exist.  "
                         + "The bank location will needs to be set again.");
             }
             
             bankLocation = new Location(world, x, y, z);
             bankRadius = config.getInt("bank.radius");
         }
         
         ConfigurationSection playersSection = config.getConfigurationSection("players");
         if (playersSection != null) {
             for (String playerName : playersSection.getKeys(false)) {
                 bank.put(playerName, playersSection.getInt(playerName));
             }
         }
     }
     
     public void saveBank() {
         try {
             File bankYml = new File(this.getDataFolder(), "bank.yml");
             YamlConfiguration config = new YamlConfiguration();
             
             if (bankLocation != null) {
                 config.set("bank.x", bankLocation.getBlockX());
                 config.set("bank.y", bankLocation.getBlockY());
                 config.set("bank.z", bankLocation.getBlockZ());
                 config.set("bank.world", bankLocation.getWorld().getUID().toString());
                 config.set("bank.radius", bankRadius);
             }
             
             for (Map.Entry<String, Integer> bankEntry : bank.entrySet()) {
                 config.set("players." + bankEntry.getKey(), bankEntry.getValue());
             }
             
             config.save(bankYml);
         } catch (IOException e) {
             log.log(Level.SEVERE, "Failed to save the XPBank state", e);
         }
     }
     
     protected static int calcXPForNextLevel(int level) {
         if (level < 0) {
             return 0;
         }
         
         if (level >= 31) {
             return 62 + ((level - 30) * 7);
         } else if (level >= 16) {
             return 17 + ((level - 15) * 3);
         } else {
             return 17;
         }
     }
     
     protected static int calcTotalXPForLevel(int level) {
         if (level == 0) {
             return 0;
         }
         
         if (level < 16) {
             return 17 * level;
         } else if (level < 31) {
             int expLevel = level - 16;
             return TOTAL_XP_LVL_15 + (17 * (expLevel+1)) + (int)(1.5 * (expLevel * expLevel + expLevel));
         } else {
             int expLevel = level - 31;
             return TOTAL_XP_LVL_30 + (62 * (expLevel+1)) + (int)(3.5 * (expLevel * expLevel + expLevel));
         }
     }
     
     protected static int calcTotalXPForPlayer(Player player) {
         return calcTotalXPForLevel(player.getLevel()) 
                 + (int)(player.getExp() * calcXPForNextLevel(player.getLevel()));
     }
     
     protected static CalculateLevelResult calcLevelFromXP(int xp) {
         double result;
         if (xp <= TOTAL_XP_LVL_15) {
             result = xp / 17.0;
         } else if (xp <= TOTAL_XP_LVL_30) {
             // (3 * (n^2 + n) / 2) + (17 * (n+1)) + TOTAL_XP_LVL_15 = xp
             // 2 * xp / 3 = n^2 + n + (2 * 17 / 3 * n) + ( 2 * 17 / 3) + (2 * TOTAL_XP_LVL_15 / 3)
             result = 16 + solveQuadraticPositive(1, (37 / 3), ((34 / 3) + ( 2 * TOTAL_XP_LVL_15 / 3) - (2 * xp / 3)));
         } else {
             // (7 * (n^2 + n) / 2) + (62 * (n+1)) + TOTAL_XP_LVL_30 = xp
             // 2 * xp / 7 = n^2 + n + (2 * 62 / 7 * n) + ( 2 * 62 / 7) + (2 * TOTAL_XP_LVL_30 / 7)
             result = 31 + solveQuadraticPositive(1, (131 / 7), ((124 / 7) + ( 2 * TOTAL_XP_LVL_30 / 7) - (2 * xp / 7)));
         }
         
         int level = (int)result;
         int nextLevel = calcXPForNextLevel(level);
         int remainingXP = xp - calcTotalXPForLevel(level);
                 
         return new CalculateLevelResult(level, (float)(remainingXP) / nextLevel);
     }
     
     protected static double solveQuadraticPositive(double a, double b, double c) {
         return ((-b) + Math.sqrt( (b * b) - (4 * a * c))) / (2 * a);
     }
     
     public static class CalculateLevelResult {
         public int level;
         public float exp;
         
         protected CalculateLevelResult(int level, float exp) {
             super();
             this.level = level;
             this.exp = exp;
         }
         
         public boolean equals(Object other) {
             if (other != null && other instanceof CalculateLevelResult) {
                 CalculateLevelResult otherResult = (CalculateLevelResult)other;
                 
                 return level == otherResult.level && exp == otherResult.exp;
             }
             
             return false;
         }
         
         public String toString() {
             return "CalculateLevelResult[level=" + level + ", exp=" + exp + "]";
         }
     }
 }
