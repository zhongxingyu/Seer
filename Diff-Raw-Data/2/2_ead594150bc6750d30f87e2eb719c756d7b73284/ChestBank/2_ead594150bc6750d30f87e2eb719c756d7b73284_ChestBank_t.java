 package me.ellbristow.ChestBank;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
         
 public class ChestBank extends JavaPlugin {
 	
     protected static ChestBank plugin;
     protected FileConfiguration banksConfig;
     protected FileConfiguration config;
     private File bankFile = null;
     protected HashMap<String, Inventory> chestAccounts;
     protected int[] limits = {10,25,35};
     protected final ChestBankListener playerListener = new ChestBankListener(this);
     protected HashMap<String, String> openInvs = new HashMap<String, String>();
     protected boolean useWhitelist = false;
     protected boolean useBlacklist = false;
     protected String[] whitelist = new String[]{"41","264","266","371"};
     protected String[] blacklist = new String[]{"8","9","10","11","51"};
     protected boolean gotVault = false;
     protected boolean gotEconomy = false;
     protected boolean useNetworkPerms = false;
     protected vaultBridge vault;
     protected double createFee;
     protected double useFee;
     protected boolean useEnderChests;
 
     @Override
     public void onDisable () {
     }
 
     @Override
     public void onEnable () {
         PluginManager pm = getServer().getPluginManager();
         config = getConfig();
         limits[0] = 10;
         limits[0] = config.getInt("normal_limit");
         config.set("normal_limit", limits[0]);
         limits[1] = 25;
         limits[1] = config.getInt("elevated_limit");
         config.set("elevated_limit", limits[1]);
         limits[2] = 25;
         limits[2] = config.getInt("vip_limit");
         config.set("vip_limit", limits[2]);
         useWhitelist = config.getBoolean("use_whitelist", false);
         useBlacklist = config.getBoolean("use_blacklist", false);
         useNetworkPerms = config.getBoolean("use_network_perms", false);
         config.set("use_whitelist", useWhitelist);
         config.set("use_blacklist", useBlacklist);
         config.set("use_network_perms", useNetworkPerms);
         String whitelistString = config.getString("whitelist", "41,264,266,371");
         if (useWhitelist) {
             whitelist = whitelistString.split(",");
             whitelistString = "";
             if (whitelist.length != 0) {
                 for(String item : whitelist) {
                     if (!"".equals(whitelistString)) {
                         whitelistString += ",";
                     }
                     whitelistString += item;
                 }
             }
         }
         config.set("whitelist", whitelistString);
         String blacklistString = config.getString("blacklist", "8,9,10,11,51");
         if (useBlacklist) {
             blacklist = blacklistString.split(",");
             blacklistString = "";
             if (blacklist.length != 0) {
                 for(String item : blacklist) {
                     if (!"".equals(blacklistString)) {
                         blacklistString += ",";
                     }
                     blacklistString += item;
                 }
             }
         }
         config.set("blacklist", blacklistString);
         if (getServer().getPluginManager().isPluginEnabled("Vault")) {
             gotVault = true;
             getLogger().info("[Vault] found and hooked!");
             vault = new vaultBridge(this);
             gotEconomy = vault.foundEconomy;
             createFee = config.getDouble("creation_fee", 0.0);
             useFee = config.getDouble("transaction_fee", 0.0);
             config.set("creation_fee", createFee);
             config.set("transaction_fee", useFee);
         }
         useEnderChests = config.getBoolean("use_ender_chests", false);
         config.set("use_ender_chests", useEnderChests);
         saveConfig();
         pm.registerEvents(playerListener, this);
         banksConfig = getChestBanks();
         chestAccounts = getAccounts();
         if (useNetworkPerms) {
             registerNetworkPerms();
         }
     }
 	
     @Override
     public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (!(sender instanceof Player)) {
                 sender.sendMessage("Sorry! The console can't use this command!");
                 return true;
         }
         
         Player player = (Player) sender;
         
         /* CHEST */
         
         if (commandLabel.equalsIgnoreCase("chest")) {
             if (args.length == 0) {
                 boolean allowed = true;
                 if (gotVault && gotEconomy && useFee != 0) {
                     if (vault.economy.getBalance(player.getName()) < useFee && !player.hasPermission("chestbank.free.use")) {
                         player.sendMessage(ChatColor.RED + "You cannot afford the transaction fee of " + ChatColor.WHITE + vault.economy.format(useFee) + ChatColor.RED + "!");
                         allowed = false;
                     }
                 }
                 if (allowed) {
                     Inventory inv = chestAccounts.get(player.getName());
                     if (inv != null && inv.getContents().length != 0) {
                         openInvs.put(player.getName(), "");
                         player.openInventory(inv);
                     } else {
                         inv = Bukkit.createInventory(player, 54, player.getName());
                         chestAccounts.put(player.getName(), inv);
                         setAccounts(chestAccounts);
                         openInvs.put(player.getName(), "");
                         player.openInventory(inv);
                     }
                 }
             } else {
                 boolean allowed = true;
                 String network = args[0];
                 if (!isNetwork(network)) {
                     player.sendMessage(ChatColor.RED + "There is no ChestBank network called " + ChatColor.WHITE + args[0]);
                     return false;
                 }
                 if (useNetworkPerms == true && (!player.hasPermission("chestbank.use.networks." + network.toLowerCase()) && !player.hasPermission("chestbank.use.networks.*")))
                 {
                     player.sendMessage(ChatColor.RED + "You are not allowed to use ChestBanks on the " + ChatColor.WHITE + network + ChatColor.RED + " network!");
                     allowed = false;
                 } else if (!useNetworkPerms && !player.hasPermission("chestbank.use.networks")) {
                     player.sendMessage(ChatColor.RED + "You are not allowed to use ChestBanks on named networks!");
                     allowed = false;
                 } else if (gotVault && gotEconomy && useFee != 0 && !player.hasPermission("chestbank.free.use.networks")) {
                     if (vault.economy.getBalance(player.getName()) < useFee) {
                         player.sendMessage(ChatColor.RED + "You cannot afford the transaction fee of " + ChatColor.WHITE + vault.economy.format(useFee) + ChatColor.RED + "!");
                         allowed = false;
                     }
                 }
                 if (allowed) {
                     Inventory inv = chestAccounts.get(network + ">>" + player.getName());
                     if (inv != null && inv.getContents().length != 0) {
                         openInvs.put(player.getName(), network);
                         player.openInventory(inv);
                     } else {
                         inv = Bukkit.createInventory(player, 54, player.getName());
                         chestAccounts.put(network + ">>" + player.getName(), inv);
                         setAccounts(chestAccounts);
                         openInvs.put(player.getName(), network);
                         player.openInventory(inv);
                     }
                 }
             }
             return true;
         }
         
         /* CHESTBANK */
         
         if (args.length == 0) {
             // Command list requested
             PluginDescriptionFile pdfFile = this.getDescription();
             player.sendMessage(ChatColor.GOLD + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
             boolean found = false;
             if (player.hasPermission("chestbank.info")) {
                 player.sendMessage(ChatColor.GOLD + "  /chestbank info " + ChatColor.GRAY + ": Get targetted ChestBank's info.");
                 found = true;
             }
             if (player.hasPermission("chestbank.list")) {
                 player.sendMessage(ChatColor.GOLD + "  /chestbank list " + ChatColor.GRAY + ": List all existing ChestBank networks.");
                 found = true;
             }
             if (player.hasPermission("chestbank.create")) {
                 player.sendMessage(ChatColor.GOLD + "  /chestbank create " + ChatColor.GRAY + ": Make targetted chest a ChestBank.");
                 found = true;
             }
             if ((!useNetworkPerms && player.hasPermission("chestbank.create.networks")) || useNetworkPerms) {
                 player.sendMessage(ChatColor.GOLD + "  /chestbank create {network}" + ChatColor.GRAY + ": Create a Chestbank on the");
                 player.sendMessage(ChatColor.GRAY + "                                        named network.");
                 found = true;
             }
             if (player.hasPermission("chestbank.remove")) {
                 player.sendMessage(ChatColor.GOLD + "  /chestbank remove " + ChatColor.GRAY + ": Make targetted ChestBank a chest.");
                 found = true;
             }
             if (player.hasPermission("chestbank.see")) {
                 player.sendMessage(ChatColor.GOLD + "  /chestbank see [player] {network}" + ChatColor.GRAY + ": View player's ChestBank account.");
                 found = true;
             }
             if (!found) {
                 player.sendMessage(ChatColor.GOLD + "There are no ChestBank commands you can use!");
             }
             return true;
         }
         else if (args.length == 1 || (args.length == 2 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("remove")))) {
             if (args[0].equalsIgnoreCase("see")) {
                 player.sendMessage(ChatColor.RED + "Please specify a player!");
                 return false;
             }
             // Create, Remove, List, Info
             else if (args[0].equalsIgnoreCase("create")) {
                 if (!player.hasPermission("chestbank.create")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to create a ChestBank!");
                     return true;
                 }
                 if (args.length == 2 && useNetworkPerms && (!player.hasPermission("chestbank.create.networks." + args[1].toLowerCase()) && !player.hasPermission("chestbank.create.networks.*"))) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to create a ChestBank on the");
                     player.sendMessage(ChatColor.WHITE + args[1].toLowerCase() + ChatColor.RED + " network!");
                     return true;
                 } else if (args.length == 2 && !useNetworkPerms && !player.hasPermission("chestbank.create.networks")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to create a ChestBank on named   networks!");
                     return true;
                 }
                 Block block = player.getTargetBlock(null, 4);
                 if (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST) {
                     player.sendMessage(ChatColor.RED + "You're not looking at a chest!");
                     return true;
                 }
                 if (isBankBlock(block)) {
                     player.sendMessage(ChatColor.RED + "That is already a ChestBank!");
                     return true;
                 }
                 Block doubleChest = getDoubleChest(block);
                 if (useEnderChests && doubleChest != null) {
                     player.sendMessage(ChatColor.RED + "You cannot turn a double chest into and Ender ChestBank!");
                     return true;
                 }
                 if (gotVault && gotEconomy && createFee != 0) {
                     if ((args.length == 2 && !player.hasPermission("chestbank.free.create.networks")) || (args.length == 1 && !player.hasPermission("chestbank.free.create"))) {
                         if (vault.economy.getBalance(player.getName()) < createFee) {
                             player.sendMessage(ChatColor.RED + "You cannot afford the ChestBank creation fee of");
                             player.sendMessage(ChatColor.WHITE + vault.economy.format(createFee) + ChatColor.RED + "!");
                             return true;
                         }
                     }
                 }
                 if (args.length == 2) {
                     // Network specified
                     String network = args[1];
                     String bankNames = banksConfig.getString("networks.names", "");
                     if (bankNames.equals("")) {
                         bankNames = args[1];
                     } else {
                         String[] bankNamesArray = bankNames.split(":");
                         boolean exists = false;
                         for (String bankName : bankNamesArray) {
                             if (bankName.equals(args[1])) {
                                 exists = true;
                             }
                         }
                         if (!exists) {
                             bankNames += ":" + args[1];
                         }
                     }
                     banksConfig.set("networks.names", bankNames);
                     ConfigurationSection networkBank = banksConfig.getConfigurationSection("networks." + args[1]);
                     String locsList = "";
                     if (networkBank != null) {
                         locsList = networkBank.getString("locations", "");
                         if (!locsList.equals("")) {
                             locsList += ";";
                         }
                     }
                     locsList += block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
                     if (doubleChest != null) {
                         locsList += ":" + doubleChest.getX() + ":" + doubleChest.getY() + ":" + doubleChest.getZ();
                     }
                     banksConfig.set("networks." + args[1] + ".locations", locsList);
                     saveChestBanks();
                     if (useEnderChests) {
                         byte data = block.getData();
                         block.setType(Material.AIR);
                         block.setType(Material.ENDER_CHEST);
                         block.setData(data);
                     }
                     player.sendMessage(ChatColor.GOLD + "ChestBank created on " + ChatColor.WHITE + network + ChatColor.GOLD + " Network!");
                     if (gotVault && gotEconomy && createFee != 0 && !player.hasPermission("chestbank.free.create.networks")) {
                         vault.economy.withdrawPlayer(player.getName(), createFee);
                         player.sendMessage(ChatColor.GOLD + "You were charged " + ChatColor.WHITE + vault.economy.format(createFee) + ChatColor.GOLD + " for ChestBank creation!");
                     }
                     return true;
                 }
                 String bankList = banksConfig.getString("banks", "");
                 if (bankList.equals("")) {
                     bankList += block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
                 }
                 else {
                     bankList += ";" + block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
                 }
                 if (doubleChest != null) {
                     bankList += ":" + doubleChest.getX() + ":" + doubleChest.getY() + ":" + doubleChest.getZ();
                 }
                 banksConfig.set("banks", bankList);
                 saveChestBanks();
                 if (useEnderChests) {
                     byte data = block.getData();
                     block.setType(Material.AIR);
                     block.setType(Material.ENDER_CHEST);
                     block.setData(data);
                 }
                 player.sendMessage(ChatColor.GOLD + "ChestBank created!");
                 if (gotVault && gotEconomy && createFee != 0 && !player.hasPermission("chestbank.free.create")) {
                     vault.economy.withdrawPlayer(player.getName(), createFee);
                     player.sendMessage(ChatColor.GOLD + "You were charged " + ChatColor.WHITE + vault.economy.format(createFee) + ChatColor.GOLD + " for ChestBank creation!");
                 }
                 return true;
             }
             else if (args[0].equalsIgnoreCase("remove")) {
                 if (!player.hasPermission("chestbank.remove")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to remove a ChestBank!");
                     return true;
                 }
                 Block block = player.getTargetBlock(null, 4);
                 if (isNetworkBank(block) && useNetworkPerms && (!player.hasPermission("chestbank.remove.networks." + getNetwork(block).toLowerCase()) && !player.hasPermission("chestbank.remove.networks.*"))) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to remove a ChestBank on the");
                     player.sendMessage(ChatColor.WHITE + getNetwork(block) + ChatColor.RED + " network!");
                     return true;
                 } else if (isNetworkBank(block) && !useNetworkPerms && !player.hasPermission("chestbank.remove.networks")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to remove a ChestBank on named networks!");
                     return true;
                 }
                 if (!isBankBlock(block)) {
                     player.sendMessage(ChatColor.RED + "You're not looking at a ChestBank!");
                     return true;
                 }
                 if (isNetworkBank(block)) {
                     String networkName = getNetwork(block);
                     String networkLocs = banksConfig.getString("networks." + networkName + ".locations", "");
                     String newNetworkLocs = "";
                     for (String location : networkLocs.split(";")) {
                         String[] loc = location.split(":");
                         if (loc.length == 4) {
                             String bankWorld = loc[0];
                             int bankX = Integer.parseInt(loc[1]);
                             int bankY = Integer.parseInt(loc[2]);
                             int bankZ = Integer.parseInt(loc[3]);
                             if (!bankWorld.equals(block.getWorld().getName()) || bankX != block.getX() || bankY != block.getY() || bankZ != block.getZ()) {
                                 if (!newNetworkLocs.equals("")) {
                                     newNetworkLocs += ";";
                                 }
                                 newNetworkLocs += bankWorld + ":" + bankX + ":" + bankY + ":" + bankZ;
                             }
                         } else if (loc.length == 7) {
                             String bankWorld = loc[0];
                             int bankX = Integer.parseInt(loc[1]);
                             int bankY = Integer.parseInt(loc[2]);
                             int bankZ = Integer.parseInt(loc[3]);
                             int bankA = Integer.parseInt(loc[4]);
                             int bankB = Integer.parseInt(loc[5]);
                             int bankC = Integer.parseInt(loc[6]);
                             if (!bankWorld.equals(block.getWorld().getName()) || (!(bankX == block.getX() && bankY == block.getY() && bankZ == block.getZ()) && !(bankA == block.getX() && bankB == block.getY() && bankC == block.getZ()) )) {
                                 if (!newNetworkLocs.equals("")) {
                                     newNetworkLocs += ";";
                                 }
                                 newNetworkLocs += bankWorld + ":" + bankX + ":" + bankY + ":" + bankZ + ":" + bankA + ":" + bankB + ":" + bankC;
                             }
                         }
                     }
                     banksConfig.set("networks." + networkName + ".locations", newNetworkLocs);
                     saveChestBanks();
                     player.sendMessage(ChatColor.GOLD + "ChestBank removed from " + ChatColor.WHITE + networkName + ChatColor.GOLD + " network!");
                     return true;
                 }
                 String bankList = banksConfig.getString("banks");
                 String[] bankSplit = bankList.split(";");
                 if (bankSplit.length == 0 || bankSplit.length == 1) {
                     banksConfig.set("banks", "");
                 }
                 else {
                     String newBankList = "";
                     for (String chestBank : bankSplit) {
                         String[] bankLoc = chestBank.split(":");
                         if (bankLoc.length == 4) {
                             String blockWorld = bankLoc[0];
                             int blockX = Integer.parseInt(bankLoc[1]);
                             int blockY = Integer.parseInt(bankLoc[2]);
                             int blockZ = Integer.parseInt(bankLoc[3]);
                             if (!blockWorld.equals(block.getWorld().getName()) || blockX != block.getX() || blockY != block.getY() || blockZ != block.getZ()) {
                                 if (!newBankList.equals("")) {
                                     newBankList += ";";
                                 }
                                 newBankList += blockWorld + ":" + blockX + ":" + blockY + ":" + blockZ;
                             }
                         }
                         else {
                             String blockWorld = bankLoc[0];
                             int blockX = Integer.parseInt(bankLoc[1]);
                             int blockY = Integer.parseInt(bankLoc[2]);
                             int blockZ = Integer.parseInt(bankLoc[3]);
                             int blockA = Integer.parseInt(bankLoc[4]);
                             int blockB = Integer.parseInt(bankLoc[5]);
                             int blockC = Integer.parseInt(bankLoc[6]);
                             if (!(blockX == block.getX() && blockY == block.getY() && blockZ == block.getZ()) && !(blockA == block.getX() && blockB == block.getY() && blockC == block.getZ())) {
                                 if (!newBankList.equals("")) {
                                     newBankList += ";";
                                 }
                                 newBankList += blockWorld + ":" + blockX + ":" + blockY + ":" + blockZ + ":" + blockA + ":" + blockB + ":" + blockC;
                             }
                         }
                     }
                     banksConfig.set("banks", newBankList);
                 }
                 saveChestBanks();
                 player.sendMessage(ChatColor.GOLD + "ChestBank removed!");
                 return true;
             }
             else if (args[0].equalsIgnoreCase("info")) {
                 if (!player.hasPermission("chestbank.info")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to get ChestBank info!");
                     return true;
                 }
                 Block block = player.getTargetBlock(null, 4);
                 if (!isBankBlock(block)) {
                     player.sendMessage(ChatColor.RED + "This block is not a ChestBank!");
                     return true;
                 } else {
                     if (!isNetworkBank(block)) {
                         player.sendMessage(ChatColor.GOLD + "This ChestBank is on the main network!");
                         return true;
                     } else {
                         String network = getNetwork(block);
                         player.sendMessage(ChatColor.GOLD + "This ChestBank is on the " + ChatColor.WHITE + network + ChatColor.GOLD + " network!");
                         return true;
                     }
                 }
             }
             else if (args[0].equalsIgnoreCase("list")) {
                 String bankLocs = banksConfig.getString("banks", "");
                 player.sendMessage(ChatColor.GOLD + "ChestBank Networks:");
                 int banks = 0;
                 if (!bankLocs.equals("")) {
                     banks = bankLocs.split(";").length;
                 }
                 player.sendMessage(ChatColor.GOLD + "  Main Network: " + ChatColor.WHITE + banks + " Location(s)");
                 String networkNames = banksConfig.getString("networks.names", "");
                 if (!networkNames.equals("")) {
                     String[] networks = networkNames.split(":");
                     for (String network : networks) {
                         bankLocs = banksConfig.getString("networks." + network + ".locations", "");
                         banks = 0;
                         if (!bankLocs.equals("")) {
                             banks = bankLocs.split(";").length;
                         }
                         player.sendMessage(ChatColor.GOLD + "  " + network + " Network: " + ChatColor.WHITE + banks + " Location(s)");
                     }
                 }
                 return true;
             }
         }
         else if (args.length >= 2) {
             if (!args[0].equalsIgnoreCase("see")) {
                 return false;
             }
             if (!player.hasPermission("chestbank.see")) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to access other players' accounts!");
                 return true;
             }
             OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
             String account = "";
             String accountFail = "";
             if (args.length == 3) {
                 String network = args[2];
                 if (!player.hasPermission("chestbank.see.networks")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to access other players' " + ChatColor.WHITE + args[2] + ChatColor.GOLD + " accounts!");
                     return true;
                 }
                 if (!isNetwork(network)) {
                     player.sendMessage(ChatColor.RED + "There is no ChestBank network called " + ChatColor.WHITE + args[2]);
                     return false;
                 }
                 account = args[2] + ">>" + target.getName();
                 accountFail = " in the " + ChatColor.WHITE + args[2] + ChatColor.RED + " network";
             }
             if ("".equals(account)) {
                 account = target.getName();
             }
             if (chestAccounts.get(account) != null) {
                 Inventory lc = chestAccounts.get(account);
                 player.openInventory(lc);
             } else {
                 player.sendMessage(ChatColor.RED + target.getName() + " does not have a ChestBank account"+accountFail+"!");
             }
             return true;
         }
         return false;
     }
 	
     public boolean isBankBlock (Block block) {
         // Check if the block is a ChestBank
         String bankList = banksConfig.getString("banks", "");
         if (!bankList.equals("")) {
             String[] bankSplit = bankList.split(";");
             for (String bank : bankSplit) {
                 if (!bank.isEmpty() && !bank.equals("")) {
                     String[] bankCoords = bank.split(":");
                     String blockWorld = bankCoords[0];
                     int blockX = Integer.parseInt(bankCoords[1]);
                     int blockY = Integer.parseInt(bankCoords[2]);
                     int blockZ = Integer.parseInt(bankCoords[3]);
                     if (block.getWorld().getName().equals(blockWorld) && block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
                         return true;
                     }
                     if (bankCoords.length > 4) {
                         blockX = Integer.parseInt(bankCoords[4]);
                         blockY = Integer.parseInt(bankCoords[5]);
                         blockZ = Integer.parseInt(bankCoords[6]);
                         if (block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
                             return true;
                         }
                     }
                 }
             }
         }
         if (isNetworkBank(block)) {
             return true;
         }
         return false;
     }
     
     public boolean isNetwork(String networkName) {
         String networks = banksConfig.getString("networks.names", "");
         if (!"".equals(networks)) {
             String[] netSplit = networks.split(":");
             for (String network : netSplit) {
                 if (network.equalsIgnoreCase(networkName)) {
                     return true;
                 }
             }
         }
         return false;
     }
         
     public boolean isNetworkBank(Block block) {
         ConfigurationSection bankList = banksConfig.getConfigurationSection("networks");
         if (bankList != null) {
             String bankNames = bankList.getString("names", "");
             if (!bankNames.equals("")) {
                 String[] bankNamesArray = bankNames.split(":");
                 for (String bankName : bankNamesArray) {
                     String bankLocs = bankList.getString(bankName + ".locations", "");
                     if (!bankLocs.equals("")) {
                         String[] bankLocations = bankLocs.split(";");
                         for (String bankLoc : bankLocations) {
                             String[] bankCoords = bankLoc.split(":");
                             String bankWorld = bankCoords[0];
                             int bankX = Integer.parseInt(bankCoords[1]);
                             int bankY = Integer.parseInt(bankCoords[2]);
                             int bankZ = Integer.parseInt(bankCoords[3]);
                             if (block.getWorld().getName().equals(bankWorld) && block.getX()== bankX && block.getY() == bankY && block.getZ() == bankZ) {
                                 return true;
                             }
                             if (bankCoords.length == 7) {
                                 bankX = Integer.parseInt(bankCoords[4]);
                                 bankY = Integer.parseInt(bankCoords[5]);
                                 bankZ = Integer.parseInt(bankCoords[6]);
                                 if (block.getWorld().getName().equals(bankWorld) && block.getX()== bankX && block.getY() == bankY && block.getZ() == bankZ) {
                                     return true;
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return false;
     }
         
     public String getNetwork(Block block) {
         String network = "";
         String networkNames = banksConfig.getString("networks.names");
         String[] networkNamesArray = networkNames.split(":");
         for (String networkName : networkNamesArray) {
             String networkLocs = banksConfig.getString("networks." + networkName + ".locations", "");
             for (String location : networkLocs.split(";")) {
                 String[] loc = location.split(":");
                 if (loc.length == 4) {
                     String bankWorld = loc[0];
                     int bankX = Integer.parseInt(loc[1]);
                     int bankY = Integer.parseInt(loc[2]);
                     int bankZ = Integer.parseInt(loc[3]);
                     if (bankWorld.equals(block.getWorld().getName()) && bankX == block.getX() && bankY == block.getY() && bankZ == block.getZ()) {
                         network = networkName;
                     }
                 } else if (loc.length == 7) {
                     String bankWorld = loc[0];
                     int bankX = Integer.parseInt(loc[1]);
                     int bankY = Integer.parseInt(loc[2]);
                     int bankZ = Integer.parseInt(loc[3]);
                     int bankA = Integer.parseInt(loc[4]);
                     int bankB = Integer.parseInt(loc[5]);
                     int bankC = Integer.parseInt(loc[6]);
                     if (bankWorld.equals(block.getWorld().getName()) && ((bankX == block.getX() && bankY == block.getY() && bankZ == block.getZ()) || (bankA == block.getX() && bankB == block.getY() && bankC == block.getZ()))) {
                         network = networkName;
                     }
                 }
             }
         }
         return network;
     }
 	
     public Block getDoubleChest(Block block) {
         if (block.getType() == Material.ENDER_CHEST) {
             return null;
         } else {
         int blockX = block.getX();
         int blockY = block.getY();
         int blockZ = block.getZ();
         if (block.getWorld().getBlockAt(blockX + 1, blockY, blockZ).getType().equals(Material.CHEST)) {
             return block.getWorld().getBlockAt(blockX + 1, blockY, blockZ);
         }
         if (block.getWorld().getBlockAt(blockX - 1, blockY, blockZ).getType().equals(Material.CHEST)) {
             return block.getWorld().getBlockAt(blockX - 1, blockY, blockZ);
         }
         if (block.getWorld().getBlockAt(blockX , blockY, blockZ + 1).getType().equals(Material.CHEST)) {
             return block.getWorld().getBlockAt(blockX, blockY, blockZ + 1);
         }
         if (block.getWorld().getBlockAt(blockX , blockY, blockZ - 1).getType().equals(Material.CHEST)) {
             return block.getWorld().getBlockAt(blockX, blockY, blockZ - 1);
         }
         return null;
         }
     }
 	
     public HashMap<String, Inventory> getAccounts() {
         HashMap<String, Inventory> chests = new HashMap<String, Inventory>();
         ConfigurationSection chestSection = banksConfig.getConfigurationSection("accounts");
         if (chestSection != null) {
             Set<String> fileChests = chestSection.getKeys(false);
             if (fileChests != null) {
                 for (String playerName : fileChests) {
                     String account;
                     if (playerName.contains(">>")) {
                         account = playerName.split(">>")[1];
                     } else {
                         account = playerName;
                     }
                     Player player = getServer().getOfflinePlayer(account).getPlayer();
                     Inventory returnInv = Bukkit.createInventory(player, 54, account);
                     String[] chestInv = banksConfig.getString("accounts." + playerName).split(";");
                     int i = 0;
                     for (String items : chestInv) {
                         String[] item = items.split(":");
                         int i0 = Integer.parseInt(item[0]);
                         int i1 = Integer.parseInt(item[1]);
                         short i2 = Short.parseShort(item[2]);
                         if(i0 != 0) {
                             ItemStack stack = new ItemStack(i0, i1, i2);
                             if (item.length == 4) {
                                 String[] enchArray = item[3].split(",");
                                 for (String ench : enchArray) {
                                     String[] bits = ench.split("~");
                                     int enchId = Integer.parseInt(bits[0]);
                                     int enchLvl = Integer.parseInt(bits[1]);
                                     stack.addUnsafeEnchantment(Enchantment.getById(enchId), enchLvl);
                                 }
                             }
                             returnInv.setItem(i, stack);
                         }
                         i++;
                     }
                     chests.put(playerName, returnInv);
                 }
             }
         }
         return chests;
     }
 	
     public void setAccounts(HashMap<String, Inventory> chests) {
         Set<String> chestKeys = chests.keySet();
         for (String key : chestKeys) {
             Inventory chest = chests.get(key);
             String chestInv = "";
             for (ItemStack item : chest.getContents()) {
                 chestInv += ";";
                 if (item != null) {
                     int itemID = item.getTypeId();
                     int itemCount = item.getAmount();
                     int itemDamage = item.getDurability();
                     chestInv += itemID + ":" + itemCount + ":" + itemDamage;
                     Map<Enchantment, Integer> enchantments = item.getEnchantments();
                     if (!enchantments.isEmpty()) {
                         chestInv += ":";
                         String enchList = "";
                         Object[] keys = enchantments.keySet().toArray();
                         Object[] levels = enchantments.values().toArray();
                         for (int i = 0; i < enchantments.size(); i++) {
                             enchList += "," + ((Enchantment)keys[i]).getId() + "~" + levels[i];
                         }
                         chestInv += enchList.replaceFirst(",", "");
                     }
                 }
                 else {
                     chestInv += "0:0:0";
                 }
             }
             banksConfig.set("accounts." + key, chestInv.replaceFirst(";", ""));
         }
         saveChestBanks();
     }
     
     private void registerNetworkPerms() {
         String networksString = this.banksConfig.getString("networks.names", "");
         if (!"".equals(networksString)) {
             String[] networks = networksString.split(",");
             for (String network : networks) {
                 getServer().getPluginManager().addPermission(new Permission("chectbank.use.networks." + network.toLowerCase()));
                 getServer().getPluginManager().addPermission(new Permission("chectbank.create.networks." + network.toLowerCase()));
                 getServer().getPluginManager().addPermission(new Permission("chectbank.remove.networks." + network.toLowerCase()));
             }
         }
     }
 	
     public void loadChestBanks() {
         if (bankFile == null) {
             bankFile = new File(getDataFolder(),"chests.yml");
         }
         banksConfig = YamlConfiguration.loadConfiguration(bankFile);
     }
 	
     public FileConfiguration getChestBanks() {
         if (banksConfig == null) {
             loadChestBanks();
         }
         return banksConfig;
     }
 	
     public void saveChestBanks() {
         if (banksConfig == null || bankFile == null) {
             return;
         }
         try {
             banksConfig.save(bankFile);
         } catch (IOException ex) {
             getLogger().severe("Could not save " + bankFile);
             ex.printStackTrace();
         }
     }
 }
