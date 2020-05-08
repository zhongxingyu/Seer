 package me.ellbristow.broker;
 
 import java.io.*;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import net.minecraft.server.InventoryLargeChest;
 import net.minecraft.server.TileEntityChest;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.DoubleChestInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Broker extends JavaPlugin {
     
     private static File itemDataFile;
     private static FileConfiguration itemAlts;
     private String[] tableColumns = {"id", "playerName", "orderType", "timeCode", "itemName", "enchantments", "damage", "price", "quant"};
     private String[] tableDims = {"INTEGER PRIMARY KEY ASC AUTOINCREMENT", "TEXT NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL", "TEXT NOT NULL", "TEXT", "INTEGER NOT NULL DEFAULT 0", "DOUBLE NOT NULL", "INTEGER NOT NULL"};
     protected vaultBridge vault;
     public BrokerDb brokerDb;
     public HashMap<String,HashMap<Integer,Double>> priceCheck = new HashMap<String, HashMap<Integer,Double>>();
     public HashMap<String,HashMap<ItemStack,Double>> pending = new HashMap<String, HashMap<ItemStack,Double>>();
 
     @Override
     public void onEnable() {
         itemAlts = getItemAlts();
         saveItemAlts();
         vault = new vaultBridge(this);
         if (vault.foundEconomy == false) {
             getLogger().severe("Could not find an Economy Plugin via [Vault]!");
             getServer().getPluginManager().disablePlugin(this);
         }
         brokerDb = new BrokerDb(this);
         brokerDb.getConnection();
         if (!brokerDb.checkTable("BrokerOrders")) {
             brokerDb.createTable("BrokerOrders", tableColumns, tableDims);
         }
         getServer().getPluginManager().registerEvents(new BrokerListener(this), this);
     }
 
     @Override
     public void onDisable() {
         brokerDb.close();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (commandLabel.equalsIgnoreCase("broker")) {
             
             if (!sender.hasPermission("broker.use")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use " + ChatColor.GOLD + "/broker");
                 return false;
             }
             
             if (args.length == 0) {
                 // Command Help
                 sender.sendMessage(ChatColor.GOLD + "== Broker v" + ChatColor.WHITE + getDescription().getVersion() + ChatColor.GOLD + " by " + ChatColor.WHITE + "ellbristow" + ChatColor.GOLD + " ==");
                 if (sender.hasPermission("broker.buy") || sender.hasPermission("broker.sell")) {
                     sender.sendMessage(ChatColor.GRAY + "{optional} [required]");
                 }
                 if (sender.hasPermission("broker.commands.buy")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker buy");
                     sender.sendMessage(ChatColor.GRAY + " Open the broker window to buy items");
                 }
                 if (sender.hasPermission("broker.commands.sell")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker sell {price per item}");
                     sender.sendMessage(ChatColor.GRAY + " List the item in your hand for sale");
                     sender.sendMessage(ChatColor.GRAY + " Either set a price or sell to the highest current bidder");
                     sender.sendMessage(ChatColor.GOLD + "/broker sell cancel");
                     sender.sendMessage(ChatColor.GRAY + " Cancel an existing sell order");
                 }
                 if (sender.hasPermission("broker.commands.query")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker query [itemId|itemName]");
                     sender.sendMessage(ChatColor.GRAY + " See the buy orders outstanding for the specified item");
                 }
                 return true;
             }
             
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.RED + "You cannot use broker from the console!");
                 return false;
             }
             
             Player player = (Player) sender;
             
             // MAIN PROCESS
             
             if (args.length >= 1) {
                 if (args[0].equalsIgnoreCase("buy")) {
                     // Buy help
                     if (!sender.hasPermission("broker.commands.buy")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use the buy command!");
                         return false;
                     }
                     switch (args.length) {
                         case 1:
                             player.openInventory(getBrokerInv("0", null));
                             player.sendMessage(ChatColor.GOLD + "<BROKER> Main Page");
                             player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
                         break;
                         case 2:
                             // TODO: Check item exists
                             // TODO: Check for sell orders
                             // TODO: Buy item if available
                         break;
                         case 3:
                             // TODO: Check item exists
                             // TODO: Check price is valid
                             // TODO: Check for sell orders
                             // TODO: Buy item if available
                             // TODO: List buy order if not available
                         break;
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("sell")) {
                     if (!sender.hasPermission("broker.commands.sell")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use the sell command!");
                         return false;
                     }
                     if (args[1] != null && args[1].equalsIgnoreCase("cancel")) {
                         player.sendMessage(ChatColor.GOLD + "Cancelling Sell Orders");
                         player.openInventory(getBrokerInv("0", player.getName()));
                         return true;
                     }
                     ItemStack itemInHand = player.getItemInHand();
                     if (itemInHand == null || itemInHand.getTypeId() == 0) {
                         sender.sendMessage(ChatColor.RED + "You're not holding anything to sell!");
                         return false;
                     }
                     switch (args.length) {
                         case 1:
                             if (!itemInHand.getEnchantments().isEmpty()) {
                                 // Item is enchanted
                                 sender.sendMessage(ChatColor.RED + "You must set a price for enchanted items!");
                                 sender.sendMessage(ChatColor.RED + "/broker sell [price per item]!");
                                 return false;
                             }
                             if (isDamageableItem(itemInHand) && itemInHand.getDurability() != 0) {
                                 // Item is damaged
                                 sender.sendMessage(ChatColor.RED + "You must set a price for damaged items!");
                                 sender.sendMessage(ChatColor.RED + "/broker sell [price per item]!");
                                 return false;
                             }
                             
                             // TODO: Check for buy orders
                             // TODO: Sell item or return 'No buyers'
                             sender.sendMessage(ChatColor.RED + "Um... sorry... this bit doesn't work yet!");
                         break;
                         case 2:
                             double price = 0;
                             try {
                                 price = Double.parseDouble(args[1]);
                             } catch (NumberFormatException nfe) {
                                 sender.sendMessage(ChatColor.RED + "Sale price must be a number!");
                                 sender.sendMessage(ChatColor.RED + "/broker sell [price per item]!");
                                 return false;
                             }
                             // TODO: Check for buy orders
                             // TODO: If not damaged or enchanted, fulfil buy order (if available)
                             // List item for sale
                             int quant = itemInHand.getAmount();
                             String itemName = itemInHand.getType().name();
                             String itemDisplayName = itemName;
                             short damage = itemInHand.getDurability();
                             Map<Enchantment, Integer> enchantments = itemInHand.getEnchantments();
                             String enchantmentString = "";
                             if (!enchantments.isEmpty()) {
                                 itemDisplayName += "(Enchanted)";
                                 Object[] enchs = enchantments.keySet().toArray();
                                 for (Object ench : enchs) {
                                     if (!"".equals(enchantmentString)) {
                                         enchantmentString += ";";
                                     }
                                     enchantmentString += ((Enchantment)ench).getId() + "@" + enchantments.get((Enchantment)ench);
                                 }
                             }
                             if (isDamageableItem(itemInHand) && damage != 0) {
                                 itemDisplayName += "(Used)";
                             }
                             String query = "INSERT INTO BrokerOrders (orderType, playerName, itemName, enchantments, damage, price, quant, timeCode) VALUES (0, '" + player.getName() + "', '" + itemName + "', '" + enchantmentString + "', " + damage + ", " + price + ", " + quant + ", " + new Date().getTime() + ")";
                             brokerDb.query(query);
                             player.setItemInHand(null);
                             player.sendMessage(quant + " x " + itemDisplayName);
                             player.sendMessage(ChatColor.GOLD + " listed for sale for " + ChatColor.GREEN + vault.economy.format(price) + ChatColor.GOLD + " each!");
                         break;
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("query")) {
                     sender.sendMessage(ChatColor.RED + "Um... sorry... this bit doesn't work yet!");
                     return true;
                 }
             }
         }
         sender.sendMessage(ChatColor.RED + "Command not recognised! Type " + ChatColor.GOLD + "/broker" + ChatColor.RED + " for help");
         return false;
     }
     
     @SuppressWarnings("CallToThreadDumpStack")
     protected DoubleChestInventory getBrokerInv(String page, String playerName) {
         DoubleChestInventory inv;
         if (playerName == null) {
             inv = (DoubleChestInventory)new CraftInventoryDoubleChest(new InventoryLargeChest("<Broker> Buy", new TileEntityChest(), new TileEntityChest()));
         } else {
             inv = (DoubleChestInventory)new CraftInventoryDoubleChest(new InventoryLargeChest("<Broker> Cancel", new TileEntityChest(), new TileEntityChest()));
         }
         inv.setMaxStackSize(64);
         for (int i = 45; i < 54; i++) {
             inv.setItem(i, new ItemStack(Material.ENDER_PORTAL));
         }
         int pageNo;
         boolean mainPage;
         if (!page.contains("::")) {
             pageNo = Integer.parseInt(page);
             mainPage = true;
         } else {
             pageNo = Integer.parseInt(page.split("::")[1]);
             mainPage = false;
         }
         if (!mainPage) {
             // Load Sub Page
             String[] pageSplit = page.split("::");
             HashMap<Integer, HashMap<String, Object>> sellOrders;
             String[] subSplit = pageSplit[0].split(":");
             if (isDamageableItem(new ItemStack(Material.getMaterial(subSplit[0])))) {
                 if (playerName == null) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = 0", "price, damage, enchantments", "price ASC, damage ASC");
                 } else {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price", "BrokerOrders", "playerName = '" + playerName + "' AND itemName = '" + subSplit[0] + "' AND orderType = 0", "price, damage, enchantments", "price ASC, damage ASC");
                 }
             } else {
                 if (playerName == null) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price","BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = 0 AND damage = "+subSplit[1], "price, damage, enchantments", "price ASC, damage ASC");
                 } else {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price","BrokerOrders", "playerName = '" + playerName + "' AND itemName = '" + subSplit[0] + "' AND orderType = 0 AND damage = "+subSplit[1], "price, damage, enchantments", "price ASC, damage ASC");
                 }
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                 int added = 0;
                 for (int i = 0; i < rows; i++) {
                     if (!isDamageableItem(new ItemStack(Material.getMaterial(sellOrders.get(i).get("itemName").toString())))) {
                         // All items the same (Show top 5 prices)
                         if (added < 5) {
                             ItemStack stack = new ItemStack(Material.getMaterial((String)sellOrders.get(i).get("itemName")));
                             stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                             if ((String)sellOrders.get(i).get("enchantments") != null && !"".equals((String)sellOrders.get(i).get("enchantments"))) {
                                 String[] enchSplit = ((String)sellOrders.get(i).get("enchantments")).split(";");
                                 for (String ench : enchSplit) {
                                     String[] enchantment = ench.split("@");
                                     stack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(enchantment[0])), Integer.parseInt(enchantment[1]));
                                 }
                             }
                             stack.setAmount((Integer)sellOrders.get(i).get("totquant"));
                             int column = 0;
                             while (stack.getAmount() > 0 && column < 9) {
                                 inv.setItem((added*9)+column, stack);
                                 stack.setAmount(stack.getAmount()-64);
                                 column++;
                             }
                             added++;
                         }
                     } else {
                         // Items may vary (Show top 45 prices)
                         if (added < 45) {
                             ItemStack stack = new ItemStack(Material.getMaterial((String)sellOrders.get(i).get("itemName")));
                             stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                             if ((String)sellOrders.get(i).get("enchantments") != null && !"".equals((String)sellOrders.get(i).get("enchantments"))) {
                                 String[] enchSplit = ((String)sellOrders.get(i).get("enchantments")).split(";");
                                 for (String ench : enchSplit) {
                                     String[] enchantment = ench.split("@");
                                     stack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(enchantment[0])), Integer.parseInt(enchantment[1]));
                                 }
                             }
                             stack.setAmount((Integer)sellOrders.get(i).get("totquant"));
                             inv.setItem(added, stack);
                             added++;
                         }
                     }
                     inv.setItem(45, new ItemStack(Material.BOOK));
                     if (rows > 45) {
                         int pageCount = 0;
                         while (rows > 0) {
                             pageCount++;
                             rows -= 45;
                         }
                         for (int j = 1; j < pageCount; j++) {
                             if (j<8) {
                                 inv.setItem(j+45, new ItemStack(Material.PAPER));
                             }
                         }
                     }
                 }
             }
         } else {
             // Load Main Page
             HashMap<Integer, HashMap<String, Object>> sellOrders;
             if (playerName == null) {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "orderType = 0", "itemName, damage", "itemName, damage ASC");
             } else {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "playerName = '" + playerName + "' AND orderType = 0", "itemName, damage", "itemName, damage ASC");
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                int checkedrows = 0;
                 int added = 0;
                 String lastItem = "";
                 int skipped = 0;
                 for (int i = 0; i < rows; i++) {
                     if (added < 45) {
                         ItemStack stack = new ItemStack(Material.getMaterial((String)sellOrders.get(i).get("itemName")));
                         if (!isDamageableItem(stack)) {
                             stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                         } else {
                             if (((String)sellOrders.get(i).get("itemName")).equals(lastItem)) {
                                 skipped++;
                             }
                         }
                        if (!inv.contains(stack) && checkedrows >= pageNo * (45 + skipped)) {
                             inv.addItem(stack);
                             added++;
                         }
                         lastItem = (String)sellOrders.get(i).get("itemName");
                        checkedrows++;
                     }
                 }
                 if (rows > 45) {
                     int pageCount = 0;
                     while (rows > 0) {
                         pageCount++;
                         rows -= 45;
                     }
                     for (int i = 0; i < pageCount; i++) {
                         if (i<9) {
                             inv.setItem(i+45, new ItemStack(Material.PAPER));
                         }
                     }
                 }
             }
         }
         return inv;
     }
     
     protected boolean isDamageableItem(ItemStack stack) {
         int typeId = stack.getTypeId();
         if ((typeId >= 256 && typeId <= 258) || typeId == 259 || typeId == 261 || (typeId >= 267 && typeId <= 279) || (typeId >= 283 && typeId <= 286) || (typeId >= 290 && typeId <= 294) || (typeId >= 298 && typeId <= 317) || typeId == 351 || typeId == 359 || typeId == 383) {
             return true;
         }
         return false;
     }
     
     private String removePlural(String plural) {
         String singular = plural;
         if (plural.lastIndexOf("S") == plural.length() - 1) {
             singular = plural.substring(0, plural.length() - 1);
         }
         return singular;
     }
     
     private String removeCommonErrors(String withErrors) {
         Object[] allItems = itemAlts.getKeys(false).toArray();
         for (Object item : allItems) {
             String altString = itemAlts.getString((String)item,"");
             if (!altString.equals("")) {
                 String[] alts = altString.split(",");
                 for (String alt : alts) {
                     if (withErrors.toLowerCase().equals(alt.toLowerCase())) {
                         return (String)item;
                     }
                 }
             }
         }
         return withErrors;
     }
     
     private void loadItemAlts() {
         if (itemDataFile == null) {
             itemDataFile = new File(getDataFolder(),"itemAlts.yml");
             if (!itemDataFile.exists()) {
                 itemDataFile.getParentFile().mkdirs();
                 InputStream in = getResource("itemAlts.yml");
                 try {
                     OutputStream out = new FileOutputStream(itemDataFile);
                     byte[] buf = new byte[1024];
                     int len;
                     while((len=in.read(buf))>0){
                         out.write(buf,0,len);
                     }
                     out.close();
                     in.close();
                 } catch (Exception e) {
                     String message = "Error creating file " + "itemAlts.yml";
                     getLogger().warning(message);
                 }
             }
         }
         itemAlts = YamlConfiguration.loadConfiguration(itemDataFile);
     }
     
     private FileConfiguration getItemAlts() {
         if (itemAlts == null) {
             loadItemAlts();
         }
         return itemAlts;
     }
 	
     private void saveItemAlts() {
         if (itemAlts == null || itemDataFile == null) {
             return;
         }
         try {
             itemAlts.save(itemDataFile);
         } catch (IOException ex) {
             String message = "Could not save " + itemDataFile + "!";
             getLogger().severe(message);
         }
     }
     
 }
