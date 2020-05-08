 package com.koletar.jj.chestkeeper;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import static com.koletar.jj.chestkeeper.ChestKeeper.trace;
 import static com.koletar.jj.chestkeeper.Phrases.phrase;
 
 /**
  * @author jjkoletar
  */
 public class CKFacilitator implements CommandExecutor, Listener {
     private ChestKeeper plugin;
     private Map<String, CKUser> openChests;
 
     public CKFacilitator(ChestKeeper plugin) {
         this.plugin = plugin;
         openChests = new HashMap<String, CKUser>();
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("chestkeeper")) {
             if (args.length == 0 || (args.length >= 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")))) {
                 for (int i = 1; i <= 18; i++) {
                     sender.sendMessage(phrase("help" + i));
                 }
                 return true;
             }
             if (args.length >= 1) {
                 if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("o") || args[0].equalsIgnoreCase("open")) {
                     if (!validatePlayer(sender)) {
                         return true;
                     }
                     if (!sender.hasPermission("chestkeeper.use") || !sender.hasPermission("chestkeeper.use.anywhere")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (args.length == 1) {
                         openDefaultChest((Player) sender);
                         return true;
                     } else if (args.length == 2) {
                         if (args[1].contains(":")) {
                             if (!sender.hasPermission("chestkeeper.use.anyone")) {
                                 sender.sendMessage(phrase("noPermission"));
                                 return true;
                             }
                             String[] bits = args[1].split(":");
                             if (bits.length != 2) {
                                 sender.sendMessage(phrase("badSyntax"));
                                 return true;
                             }
                             CKUser user = plugin.matchUser(bits[0]);
                             if (user == null) {
                                 sender.sendMessage(phrase("unknownUser", bits[0]));
                                 return true;
                             }
                             Inventory chest = user.openChest(bits[1]);
                             if (chest == null) {
                                 sender.sendMessage(phrase("specificUnknownChest", user, bits[1]));
                                 return true;
                             }
                             sendChest((Player) sender, user, chest);
                             return true;
                         } else {
                             openChest((Player) sender, args[1]);
                             return true;
                         }
                     }
                     sender.sendMessage(phrase("badArgs"));
                     return true;
                 } else if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (args.length > 2) {
                         sender.sendMessage(phrase("badSyntax"));
                         return true;
                     }
                     if (args.length == 1) {
                         if (!validatePlayer(sender)) {
                             return true;
                         }
                         Player p = (Player) sender;
                         CKUser user = plugin.getUser(p);
                         p.sendMessage(phrase("chestListPrefix", StringTools.buildList(user.getChestNames(), "&c", "&6, ")));
                         return true;
                     } else if (args.length == 2) {
                         if (!sender.hasPermission("chestkeeper.use.anyone")) {
                             sender.sendMessage(phrase("noPermission"));
                             return true;
                         }
                         CKUser user = plugin.matchUser(args[1]);
                         if (user == null) {
                             sender.sendMessage(phrase("unknownUser", args[1]));
                             return true;
                         }
                         sender.sendMessage(phrase("specificChestListPrefix", user.getUsername(), StringTools.buildList(user.getChestNames(), "&c", "&6, ")));
                         return true;
                     }
                 } else if (args[0].equalsIgnoreCase("b") || args[0].equalsIgnoreCase("buy")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (!validatePlayer(sender)) {
                         return true;
                     }
                     Player p = (Player) sender;
                     CKUser user = plugin.getUser(p);
                     if (user.getNumberOfChests() + 1 > ChestKeeper.Config.getMaxNumberOfChests() && ChestKeeper.Config.getMaxNumberOfChests() != -1 && !sender.hasPermission("chestkeeper.override")) {
                         p.sendMessage(phrase("youHitTheLimit", ChestKeeper.Config.getMaxNumberOfChests()));
                         return true;
                     }
                    if (args.length > 3 || args.length < 2) {
                         p.sendMessage(phrase("badArgs"));
                         return true;
                     }
                     String name = null;
                     if (args.length == 3) {
                         name = args[2];
                     }
                     if (args[1].equalsIgnoreCase("small") || args[1].equalsIgnoreCase("normal")) {
                         if (ChestKeeper.Config.getNormalChestPrice() > 0) {
                             EconomyResponse er = plugin.getEconomy().withdrawPlayer(p.getName(), ChestKeeper.Config.getNormalChestPrice());
                             if (!er.transactionSuccess()) {
                                 p.sendMessage(phrase("youreTooPoor"));
                                 return true;
                             } else {
                                 buyChest(p, name, false, ChestKeeper.Config.getNormalChestPrice());
                             }
                         } else {
                             buyChest(p, name, false, 0);
                         }
                         return true;
                     } else if (args[1].equalsIgnoreCase("large") || args[1].equalsIgnoreCase("double")) {
                         if (ChestKeeper.Config.getLargeChestPrice() > 0) {
                             EconomyResponse er = plugin.getEconomy().withdrawPlayer(p.getName(), ChestKeeper.Config.getLargeChestPrice());
                             if (!er.transactionSuccess()) {
                                 p.sendMessage(phrase("youreTooPoor"));
                                 return true;
                             } else {
                                 buyChest(p, name, true, ChestKeeper.Config.getLargeChestPrice());
                             }
                         } else {
                             buyChest(p, name, true, 0);
                         }
                         return true;
                     }
                     sender.sendMessage(phrase("badArgs"));
                     return true;
                 } else if (args[0].equalsIgnoreCase("empty") || args[0].equalsIgnoreCase("e")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (args.length < 2) {
                         sender.sendMessage(phrase("needToSpecifyChest"));
                         return true;
                     }
                     CKUser user = findChest(args[1], sender);
                     if (user != null) {
                         boolean mine = args[1].split(":").length == 1;
                         CKChest chest = user.getChest(mine ? args[1] : args[1].split(":")[1]);
                         chest.empty();
                         plugin.queueUser(user);
                         sender.sendMessage(phrase("chestEmptied", chest));
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("nuke")) {
                     if (!sender.hasPermission("chestkeeper.use") || !sender.hasPermission("chestkeeper.use.anyone")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (args.length > 2 || args.length == 1) {
                         sender.sendMessage(phrase("badArgs"));
                         return true;
                     }
                     CKUser user = plugin.matchUser(args[1]);
                     if (user == null) {
                         sender.sendMessage(phrase("unknownUser", args[1]));
                         return true;
                     }
                     Set<String> names = user.getChestNames();
                     final Set<String> myNames = new HashSet<String>(names);
                     for (String chest : myNames) {
                         user.removeChest(chest);
                     }
                     plugin.queueUser(user);
                     sender.sendMessage(phrase("playerNuked", user.getUsername()));
                     return true;
                 } else if (args[0].equalsIgnoreCase("rm") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (args.length > 2) {
                         sender.sendMessage(phrase("badArgs"));
                         return true;
                     }
                     if (args.length == 1) {
                         sender.sendMessage(phrase("needToSpecifyChest"));
                         return true;
                     }
                     if (args[1].contains(":")) {
                         if (!sender.hasPermission("chestkeeper.use.anyone")) {
                             sender.sendMessage(phrase("noPermission"));
                             return true;
                         }
                         String[] bits = args[1].split(":");
                         if (bits.length != 2) {
                             sender.sendMessage(phrase("badSyntax"));
                             return true;
                         }
                         CKUser user = plugin.matchUser(bits[0]);
                         if (user == null) {
                             sender.sendMessage(phrase("unknownUser", bits[0]));
                             return true;
                         }
                         boolean result = user.removeChest(bits[1]);
                         if (!result) {
                             sender.sendMessage(phrase("specificUnknownChest", user, bits[1]));
                             return true;
                         }
                         sender.sendMessage(phrase("removedTheirChest", user, args[1]));
                         plugin.queueUser(user);
                         return true;
                     } else {
                         if (!validatePlayer(sender)) {
                             return true;
                         }
                         boolean result = plugin.getUser((Player) sender).removeChest(args[1]);
                         if (!result) {
                             sender.sendMessage(phrase("unknownChest", args[1]));
                             return true;
                         } else {
                             sender.sendMessage(phrase("removedYourChest", args[1]));
                             plugin.queueUser(plugin.getUser((Player) sender));
                             return true;
                         }
                     }
                 } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("default") || args[0].equalsIgnoreCase("d")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (!validatePlayer(sender)) {
                         return true;
                     }
                     Player p = (Player) sender;
                     if (args.length == 1) {
                         p.sendMessage(phrase("needToSpecifyChest"));
                         return true;
                     }
                     CKUser user = plugin.getUser(p);
                     boolean result = user.setDefaultChest(args[1]);
                     if (!result) {
                         p.sendMessage(phrase("unknownChest", args[1]));
                     } else {
                         p.sendMessage(phrase("setDefaultChest", args[1].toLowerCase()));
                     }
                     plugin.queueUser(user);
                     return true;
                 } else if (args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("mv")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (!validatePlayer(sender)) {
                         return true;
                     }
                     Player p = (Player) sender;
                     CKUser user = plugin.getUser(p);
                     if (args.length != 3) {
                         p.sendMessage(phrase("badArgs"));
                         return true;
                     }
                     if (!user.hasChest(args[1])) {
                         p.sendMessage(phrase("unknownChest", args[1]));
                         return true;
                     }
                     if (user.hasChest(args[2]) || args[2].equalsIgnoreCase("defaultChest") || args[2].equalsIgnoreCase("username") || args[2].equalsIgnoreCase("magic")) {
                         p.sendMessage(phrase("nameInUse", args[2]));
                         return true;
                     }
                     user.mv(args[1], args[2]);
                     p.sendMessage(phrase("chestRenamed", args[1], args[2]));
                     plugin.queueUser(user);
                     return true;
                 } else if (args[0].equalsIgnoreCase("u") || args[0].equalsIgnoreCase("upgrade")) {
                     if (!sender.hasPermission("chestkeeper.use")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     if (!validatePlayer(sender)) {
                         return true;
                     }
                     Player p = (Player) sender;
                     if (args.length == 1) {
                         sender.sendMessage(phrase("needToSpecifyChest"));
                         return true;
                     }
                     CKChest chest = plugin.getUser(p).getChest(args[1]);
                     upgradeChest(p, chest, plugin.getUser(p));
                     return true;
                 } else if (args[0].equalsIgnoreCase("convert")) {
                     if (!sender.hasPermission("chestkeeper.convert")) {
                         sender.sendMessage(phrase("noPermission"));
                         return true;
                     }
                     File vcDir = new File(plugin.getDataFolder(), "../VirtualChest");
                     if (!vcDir.exists()) {
                         sender.sendMessage(phrase("noVCData"));
                         return true;
                     }
                     File players = new File(vcDir, "Players");
                     Map<String, String> defaultChests = new HashMap<String, String>();
                     for (File playerFile : players.listFiles(new ChestKeeper.YMLFilter())) {
                         YamlConfiguration conf = YamlConfiguration.loadConfiguration(playerFile);
                         if (conf.contains("DefaultChest")) {
                             String username = playerFile.getName().replace(".yml", "");
                             defaultChests.put(username, conf.getString("DefaultChest"));
                         }
                     }
                     File chests = new File(vcDir, "Chests");
                     YamlConfiguration conf = new YamlConfiguration();
                     for (File chestFile : chests.listFiles(new ChestKeeper.ChestYMLFilter())) {
                         String username = chestFile.getName().replace(".chestYml", "");
                         CKUser user = plugin.getUser(username);
                         try {
                             FileReader fr = new FileReader(chestFile);
                             BufferedReader br = new BufferedReader(fr);
                             user.fromVC(br, defaultChests.get(username));
                             br.close();
                             File out = new File(new File(plugin.getDataFolder(), "data"), ChestKeeper.getFileName(user.getUsername()));
                             conf.set("user", user);
                             conf.save(out);
                         } catch (FileNotFoundException e) {
                             e.printStackTrace();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                     sender.sendMessage(phrase("converted"));
                     return true;
                 } else if (args[0].equalsIgnoreCase("about") || args[0].equalsIgnoreCase("version")) {
                     sender.sendMessage(phrase("about1"));
                     sender.sendMessage(phrase("about2"));
                     sender.sendMessage(phrase("about3", plugin.getDescription().getVersion()));
                     return true;
                 }
             }
             sender.sendMessage(phrase("unknownCommand"));
             return true;
         }
         return false;
     }
 
     private void upgradeChest(Player p, CKChest chest, CKUser user) {
         if (chest == null) {
             chest = plugin.getUser(p).getChest();
         }
         if (chest == null) {
             p.sendMessage(phrase("noChests"));
             return;
         }
         if (chest.isLargeChest()) {
             p.sendMessage(phrase("alreadyLarge", chest));
             return;
         }
         if (ChestKeeper.Config.getLargeChestPrice() > 0 && plugin.hasEconomy()) {
             double price = ChestKeeper.Config.getLargeChestPrice() - ChestKeeper.Config.getNormalChestPrice();
             if (price < 0) {
                 price = 0;
             }
             EconomyResponse er = plugin.getEconomy().withdrawPlayer(p.getName(), price);
             if (!er.transactionSuccess()) {
                 p.sendMessage(phrase("youreTooPoor"));
                 return;
             }
             p.sendMessage(phrase("upgradedFor", chest, plugin.getEconomy().format(price)));
         } else {
             p.sendMessage(phrase("upgraded", chest));
         }
         chest.upgrade();
         plugin.queueUser(user);
     }
 
     private CKUser findChest(String name, CommandSender sender) {
         if (name.contains(":")) {
             if (!sender.hasPermission("chestkeeper.use.anyone")) {
                 sender.sendMessage(phrase("noPermission"));
                 return null;
             }
             String[] bits = name.split(":");
             if (bits.length != 2) {
                 sender.sendMessage(phrase("badSyntax"));
                 return null;
             }
             CKUser user = plugin.matchUser(bits[0]);
             if (user == null) {
                 sender.sendMessage(phrase("unknownUser", bits[0]));
                 return null;
             }
             CKChest chest = user.getChest(bits[1]);
             if (chest == null) {
                 sender.sendMessage(phrase("specificUnknownChest", user, bits[1]));
                 return null;
             }
             return user;
         } else {
             if (sender instanceof Player) {
                 CKChest chest = plugin.getUser((Player) sender).getChest(name);
                 if (chest == null) {
                     sender.sendMessage(phrase("unknownChest"));
                     return null;
                 } else {
                     return plugin.getUser((Player) sender);
                 }
             }
             sender.sendMessage(phrase("unknownChest"));
             return null;
         }
     }
 
     private void openDefaultChest(Player p) {
         InventoryType openInventory = p.getOpenInventory().getType();
         if (!InventoryType.CRAFTING.equals(openInventory) && !InventoryType.PLAYER.equals(openInventory) && !InventoryType.CREATIVE.equals(openInventory)) {
             return;
         }
         openDefaultChest(p, plugin.getUser(p));
     }
 
     private void openDefaultChest(Player p, CKUser user) {
         if (user.getNumberOfChests() == 0) {
             p.sendMessage(phrase("noChests"));
             return;
         }
         sendChest(p, user, user.openChest());
     }
 
     private void openChest(Player p, String chestName) {
         openChest(p, plugin.getUser(p), chestName);
     }
 
     private void openChest(Player p, CKUser user, String chestName) {
         Inventory chest = user.openChest(chestName);
         if (chest == null) {
             p.sendMessage(phrase("unknownChest", chestName));
             return;
         }
         sendChest(p, user, chest);
     }
 
     private void sendChest(Player p, CKUser user, Inventory chest) {
         trace("Sending player " + p.getName() + " " + user.getUsername() + "'s " + chest.getName());
         openChests.put(chest.getTitle(), user);
         p.openInventory(chest);
     }
 
     @EventHandler
     public void onInventoryClose(InventoryCloseEvent event) {
         Inventory inventory = event.getInventory();
         boolean isOurs = openChests.containsKey(inventory.getTitle());
         boolean isOutOfView = inventory.getViewers().size() - 1 == 0;
         trace("Inventory closed: " + inventory.getName() + ", isOurs: " + isOurs + ", isOutOfView: " + isOutOfView);
         if (isOurs && isOutOfView) {
             CKUser user = openChests.get(inventory.getTitle());
             openChests.remove(user);
             if (user.save(inventory)) {
                 trace("Save successful, queueing");
                 plugin.queueUser(user);
             } else {
                 trace("Save failed");
             }
         }
     }
 
     private void buyChest(Player p, String name, boolean isLargeChest, double price) {
         CKUser user = plugin.getUser(p.getName());
         if (name == null) {
             name = phrase(isLargeChest ? "large" : "normal") + (plugin.getUser(p).getNumberOfChests() + 1);
         }
         user.createChest(name, isLargeChest);
         p.sendMessage(phrase(price == 0 ? "youBoughtAChest" : "youBoughtAChestFor", phrase(isLargeChest ? "large" : "normal"), name, plugin.hasEconomy() ? plugin.getEconomy().format(price) : "0"));
         plugin.queueUser(user);
     }
 
     private static boolean validatePlayer(CommandSender sender) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Only players may use this command.");
             return false;
         }
         return true;
     }
 
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         String[] lines = event.getLines();
         boolean setTemplate = false;
         if (lines[0].equalsIgnoreCase(phrase("keeperSignReader"))) {
             if (!event.getPlayer().hasPermission("chestkeeper.sign.keeper.place")) {
                 lines[0] = phrase("signNoPerms");
                 for (int i = 1; i < lines.length; i++) {
                     lines[i] = "";
                 }
             } else {
                 lines[0] = phrase("keeperSignReader");
                 boolean empty = true;
                 for (int i = 1; i < lines.length; i++) {
                     if (lines[i] != null && !lines[i].equals("")) {
                         empty = false;
                     }
                 }
                 if (empty) {
                     setTemplate = true;
                     lines[2] = phrase("keeperSign");
                 }
             }
         } else if (lines[0].equalsIgnoreCase(phrase("upgradeSignReader"))) {
             if (!event.getPlayer().hasPermission("chestkeeper.sign.upgrade.place")) {
                 lines[0] = phrase("signNoPerms");
                 for (int i = 1; i < lines.length; i++) {
                     lines[i] = "";
                 }
             } else {
                 lines[0] = phrase("upgradeSignReader");
                 boolean empty = true;
                 for (int i = 1; i < lines.length; i++) {
                     if (lines[i] != null && !lines[i].equals("")) {
                         empty = false;
                     }
                 }
                 if (empty) {
                     setTemplate = true;
                     lines[2] = phrase("upgradeSign");
                 }
             }
         } else if (lines[0].equalsIgnoreCase(phrase("buySignReader"))) {
             if (!event.getPlayer().hasPermission("chestkeeper.sign.buy.place")) {
                 lines[0] = phrase("signNoPerms");
                 for (int i = 1; i < lines.length; i++) {
                     lines[i] = "";
                 }
             } else {
                 lines[0] = phrase("buySignReader");
                 boolean empty = true;
                 for (int i = 1; i < lines.length; i++) {
                     if (lines[i] != null && !lines[i].equals("")) {
                         empty = false;
                     }
                 }
                 if (empty) {
                     setTemplate = true;
                     lines[2] = phrase("buySign");
                 }
             }
         }
         if (setTemplate) {
             lines[1] = phrase("sign1");
             lines[3] = phrase("sign3");
         }
         for (int i = 0; i < lines.length; i++) {
             event.setLine(i, lines[i]);
         }
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
             if (event.getClickedBlock() != null && (event.getClickedBlock().getType().equals(Material.WALL_SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST))) {
                 BlockState state = event.getClickedBlock().getState();
                 if (state instanceof Sign) {
                     Sign sign = (Sign) state;
                     String[] lines = sign.getLines();
                     if (lines.length == 0) {
                         return;
                     }
                     Player p = event.getPlayer();
                     if (lines[0].equals(phrase("keeperSignReader"))) {
                         event.setCancelled(true);
                         if (!p.hasPermission("chestkeeper.use")) {
                             p.sendMessage(phrase("noPermission"));
                             return;
                         }
                         openDefaultChest(p);
                     } else if (lines[0].equals(phrase("upgradeSignReader"))) {
                         event.setCancelled(true);
                         if (!p.hasPermission("chestkeeper.use")) {
                             p.sendMessage(phrase("noPermission"));
                             return;
                         }
                         upgradeChest(p, null, plugin.getUser(p));
                     } else if (lines[0].equals(phrase("buySignReader"))) {
                         event.setCancelled(true);
                         if (!p.hasPermission("chestkeeper.use")) {
                             p.sendMessage(phrase("noPermission"));
                             return;
                         }
                         CKUser user = plugin.getUser(p);
                         if (user.getNumberOfChests() + 1 > ChestKeeper.Config.getMaxNumberOfChests() && ChestKeeper.Config.getMaxNumberOfChests() != -1 && !p.hasPermission("chestkeeper.override")) {
                             p.sendMessage(phrase("youHitTheLimit", ChestKeeper.Config.getMaxNumberOfChests()));
                             return;
                         }
                         if (ChestKeeper.Config.getNormalChestPrice() > 0 && plugin.hasEconomy()) {
                             EconomyResponse er = plugin.getEconomy().withdrawPlayer(p.getName(), ChestKeeper.Config.getNormalChestPrice());
                             if (!er.transactionSuccess()) {
                                 p.sendMessage(phrase("youreTooPoor"));
                                 return;
                             } else {
                                 buyChest(p, null, false, ChestKeeper.Config.getNormalChestPrice());
                             }
                         } else {
                             buyChest(p, null, false, 0);
                         }
                     }
                 }
             }
         } else if (event.getAction().equals(Action.LEFT_CLICK_AIR)) {
             if (ChestKeeper.Config.getWandItemId() != 0) {
                 if (event.getItem() != null && event.getItem().getTypeId() == ChestKeeper.Config.getWandItemId() && event.getPlayer().hasPermission("chestkeeper.use.wand")) {
                     openDefaultChest(event.getPlayer());
                 }
             }
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (checkBlock(event.getBlock())) {
             event.setCancelled(!event.getPlayer().hasPermission("chestkeeper.sign.break"));
         }
     }
 
     private boolean checkBlock(Block block) {
         if (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
             Sign sign = (Sign) block.getState();
             String line1 = sign.getLine(0);
             if (line1.equals(phrase("keeperSignReader")) || line1.equals(phrase("upgradeSignReader")) || line1.equals(phrase("buySignReader"))) {
                 return true;
             }
         }
         return false;
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         if (event.getPlayer().hasPermission("chestkeeper.updates") && plugin.needsUpdate()) {
             final Player player = event.getPlayer();
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                 public void run() {
                     player.sendMessage(phrase("updateWarning1"));
                     player.sendMessage(phrase("updateWarning2"));
                     if (plugin.isUpdateCritical()) {
                         player.sendMessage(phrase("criticalUpdateWarningDecoration"));
                         player.sendMessage(phrase("criticalUpdateWarning"));
                         player.sendMessage(phrase("criticalUpdateWarningDecoration"));
                     }
                 }
             });
         }
     }
 }
