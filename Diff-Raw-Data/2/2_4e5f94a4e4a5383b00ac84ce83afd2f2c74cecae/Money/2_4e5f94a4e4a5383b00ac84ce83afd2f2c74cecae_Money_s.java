 package com.thepastimers.Money;
 
 import com.thepastimers.Database.Database;
 import com.thepastimers.ItemName.ItemName;
 import com.thepastimers.Permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.EventHandler;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/5/12
  * Time: 11:52 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Money extends JavaPlugin implements Listener {
     Database database;
     Permission permission;
     ItemName itemName;
 
     Map<String,Integer> prices;
 
     @Override
     public void onEnable() {
         getLogger().info("Money init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
 
         if (database == null) {
             getLogger().warning("Unable to connect to Database plugin. Critical error!");
             getServer().broadcastMessage(ChatColor.RED + "Money plugin was unable to connect to Database.");
             getServer().broadcastMessage(ChatColor.RED + "Any server function involving money will not be available.");
         }
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
 
         if (permission == null) {
             getLogger().warning("Unable to connection to Permission plugin. Critical error!");
             getServer().broadcastMessage(ChatColor.RED + "Money plugin was unable to connect to Permission plugin.");
             getServer().broadcastMessage(ChatColor.RED + "Any money commands (such as /bal) will be unavailable");
         }
 
         itemName = (ItemName)getServer().getPluginManager().getPlugin("ItemName");
 
         if (itemName == null) {
             getLogger().warning("Unable to connection to ItemName plugin. Critical error!");
             getServer().broadcastMessage(ChatColor.RED + "Money plugin was unable to connect to ItemName plugin.");
             getServer().broadcastMessage(ChatColor.RED + "Commands such as /sell will be unavailable");
         }
 
         getLogger().info("Table info");
         getLogger().info(MoneyData.getTableInfo());
         getLogger().info(Price.getTableInfo());
 
         prices = new HashMap<String,Integer>();
 
         getLogger().info("Money init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Money disable");
     }
 
     @EventHandler
     public void onLogin(PlayerLoginEvent event) {
         if (database == null) {
             return;
         }
         String name = event.getPlayer().getName();
         if (getMoneyObject(name) == null) {
             getLogger().info("Creating money object for " + name);
 
             MoneyData data = new MoneyData();
             data.setPlayer(name);
             data.setBalance(10000);
             if (!data.save(database)) {
                 getLogger().warning("Unable to create new money object.");
             }
         }
     }
 
     public double getBalance(String player) {
         if (database == null) {
             return 0.0;
         }
 
         List<MoneyData> data = (List<MoneyData>)database.select(MoneyData.class,"player = '"
                 + database.makeSafe(player) + "'");
 
         if (data.size() == 0) {
             getLogger().warning("No money entry for " + player);
             return 0.0;
         }
 
         double val = data.get(0).getBalance();
 
         return val/100;
     }
 
     private MoneyData getMoneyObject(String player) {
         if (database == null) {
             return null;
         }
 
         List<MoneyData> data = (List<MoneyData>)database.select(MoneyData.class,"player = '"
                 + database.makeSafe(player) + "'");
 
         if (data.size() == 0) {
             getLogger().warning("No money entry for " + player);
             return null;
         }
 
         return data.get(0);
     }
 
     public boolean setBalance(String player, double amount) {
         return setBalance(player,(int)(amount*100));
     }
 
     public boolean setBalance(String player, int amount) {
         MoneyData md = getMoneyObject(player);
 
         if (md == null) {
             return false;
         }
 
         // never set a negative balance
         if (amount < 0) {
             return false;
         }
 
 
 
         md.setBalance(amount);
 
         return md.save(database);
     }
 
     public boolean give(String player, double amount) {
         MoneyData md = getMoneyObject(player);
 
         if (md == null) {
             return false;
         }
 
         return setBalance(player,getBalance(player) + amount);
     }
 
     public int getPrice(String name) {
         ItemStack is = itemName.getItemFromName(name);
         int price = -1;
 
         if (is == null) {
             return price;
         }
 
         if (prices.keySet().contains(name)) {
             price = prices.get(name);
         }
 
         if (price == -1 && database != null) {
             List<Price> priceList = (List<Price>)database.select(Price.class,"name = '" + database.makeSafe(name) + "'");
 
             if (priceList.size() != 0) {
                 price = priceList.get(0).getPrice();
             }
         }
 
         if (price == -1) {
             getLogger().info("Attempting to generate price for " + name);
             List<Recipe> recipes = getServer().getRecipesFor(is);
             if (recipes.size() > 1) {
                 getLogger().info("Can't generate price for " + name + ", more then one recipe.");
             } else if (recipes.size() == 0) {
                 getLogger().info("Can't generate price for " + name + ", it is a base item");
             } else {
                 List<ItemStack> itemList = itemName.recipeContents(recipes.get(0));
                 if (itemList.size() == 0) {
                     getLogger().info("Got a recipe for " + name + " but it has no items in it. WTF.");
                 } else {
                     for (ItemStack item : itemList) {
                         if (item == null || item.getType() == null) continue;
                        int tPrice = getPrice(item.getType().name());
                         if (tPrice == -1) {
                             return -1;
                         } else {
                             price += tPrice;
                         }
                     }
 
                     ItemStack item = recipes.get(0).getResult();
                     BigDecimal priceDec = new BigDecimal(price);
                     BigDecimal amount = new BigDecimal(item.getAmount());
                     priceDec = priceDec.divide(amount,BigDecimal.ROUND_UP);
                     price = priceDec.intValue();
                 }
             }
         }
 
         if (!prices.keySet().contains(name) && price != -1) {
             prices.put(name,price);
             getLogger().info("Adding to price map-" + name + ": " + price);
         }
 
         return price;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("bal")) {
             if (permission == null || !permission.hasPermission(playerName,"money_bal")) {
                 sender.sendMessage("You do not have permission to use this command (money_bal)");
                 return true;
             }
 
             String player = "";
 
             if (args.length == 0) {
                 if (playerName.equalsIgnoreCase("CONSOLE")) {
                     sender.sendMessage("You do not have permission to do this (you are console).");
                     return true;
                 }
                 player = playerName;
             } else {
                 if (!permission.hasPermission(playerName,"money_balother")) {
                     sender.sendMessage("You do not have permission to do this. (money_balother)");
                     return true;
                 }
                 player = args[0];
             }
 
             double bal = getBalance(player);
 
             sender.sendMessage("Balance for " + player + " is $" + bal);
         } else if (command.equalsIgnoreCase("pay")) {
             if (permission == null || !permission.hasPermission(playerName,"money_pay") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command (money_pay)");
                 return true;
             }
 
             if (args.length > 1) {
                 String player = args[0];
                 double amt;
                 try {
                     amt = Double.parseDouble(args[1]);
                 } catch(NumberFormatException e) {
                     sender.sendMessage("Invalid amount");
                     return true;
                 }
 
                 if (amt < 0) {
                     sender.sendMessage("You cannot pay a negative amount.");
                     return true;
                 }
 
                 MoneyData md = getMoneyObject(player);
 
                 if (md == null) {
                     sender.sendMessage("Could not find " + player);
                     return true;
                 }
 
                 double bal = getBalance(playerName);
 
                 if (!give(playerName,-amt)) {
                     sender.sendMessage("You do not have that much.");
                 } else {
                     if (!give(player,amt)) {
                         sender.sendMessage("Unable to give money to " + player);
                         getLogger().warning("Unable to give " + amt + " " + playerName + " -> " + player);
                     } else {
                         sender.sendMessage(ChatColor.GREEN + "You have paid $" + amt + " to " + player);
                         Player p = getServer().getPlayer(player);
                         if (p != null) {
                             p.sendMessage(ChatColor.GREEN + playerName + " has paid you $" + amt);
                         }
 
                         getLogger().info(playerName + " paid " + player + " $" + amt);
                     }
                 }
             } else {
                 sender.sendMessage("/pay <player> <amount>");
             }
         } else if ("sell".equalsIgnoreCase(command)) {
             if (itemName == null) {
                 sender.sendMessage("This command is not currently available");
                 return true;
             }
             if (permission == null || !permission.hasPermission(playerName,"money_pay") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command (money_pay)");
                 return true;
             }
 
             Player p = getServer().getPlayer(playerName);
 
             if (p.getGameMode() == GameMode.CREATIVE) {
                 p.sendMessage(ChatColor.RED + "You cannot use /sell while in creative mode");
                 return true;
             }
 
             if (args.length > 0) {
                 String from = args[0];
                 int amount = -1;
                 if (args.length > 1) {
                     try {
                         amount = Integer.parseInt(args[1]);
                     } catch (NumberFormatException e) {
                         sender.sendMessage("Amount must be a number.");
                         return true;
                     }
                 }
 
                 if ("hand".equalsIgnoreCase(from)) {
                     PlayerInventory pi = p.getInventory();
                     from = itemName.getItemName(pi.getItemInHand());
                 }
 
                 ItemStack is = itemName.getItemFromName(from);
 
                 if (is == null) {
                     sender.sendMessage("That item does not exist");
                     return true;
                 }
 
                 if (amount == -1) {
                     amount = itemName.countInInventory(from,playerName);
                 }
 
                 int inHand = itemName.countInInventory(from,playerName);
                 if (inHand < amount) {
                     sender.sendMessage("You don't have " + amount + " of " + from);
                     return true;
                 }
 
                 if (amount <= 0) {
                     sender.sendMessage("You can't sell " + amount + " of an item");
                     return true;
                 }
 
                 int costPer = getPrice(from);
                 if (costPer < 0) {
                     sender.sendMessage("You cannot sell " + from);
                     return true;
                 }
 
                 int total = amount*costPer;
 
                 double price = ((double)total)/100;
                 DecimalFormat df = new DecimalFormat("#.##");
                 String priceStr = df.format(price);
 
                 sender.sendMessage("Selling " + amount + " of " + from + " for $" + priceStr);
                 if (!give(playerName,price)) {
                     sender.sendMessage("Unable to sell.");
                 } else {
                     if (!itemName.takeItem(p,from,amount)) {
                         sender.sendMessage("Unable to sell.");
                     } else {
                         sender.sendMessage("Sale complete. Balance: $" + getBalance(playerName));
                         Sale sale = new Sale();
                         sale.setItem(from);
                         sale.setAmount(amount);
                         sale.setPrice(total);
                         sale.setPlayer(playerName);
                         sale.save(database);
                     }
                 }
             } else {
                 sender.sendMessage("/sell <item name|hand> <amount>");
             }
         } else if ("worth".equalsIgnoreCase(command)) {
             if (itemName == null) {
                 sender.sendMessage("This command is not currently available");
                 return true;
             }
             if (permission == null || !permission.hasPermission(playerName,"money_pay") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You do not have permission to use this command (money_pay)");
                 return true;
             }
 
             if (args.length > 0) {
                 Player p = getServer().getPlayer(playerName);
                 String from = args[0];
                 int amount = -1;
                 if (args.length > 1) {
                     try {
                         amount = Integer.parseInt(args[1]);
                     } catch (NumberFormatException e) {
                         sender.sendMessage("Amount must be a number.");
                         return true;
                     }
                 }
 
                 if ("hand".equalsIgnoreCase(from)) {
                     PlayerInventory pi = p.getInventory();
                     from = itemName.getItemName(pi.getItemInHand());
                 }
 
                 ItemStack is = itemName.getItemFromName(from);
 
                 if (is == null) {
                     sender.sendMessage("That item does not exist");
                     return true;
                 }
 
                 if (amount == -1) {
                     amount = itemName.countInInventory(from,p.getName());
                 }
 
                 if (amount <= 0) {
                     sender.sendMessage("You can't sell " + amount + " of an item");
                     return true;
                 }
 
                 int costPer = getPrice(from);
                 if (costPer < 0) {
                     sender.sendMessage("You cannot sell " + from);
                     return true;
                 }
 
                 int total = amount*costPer;
 
                 double price = ((double)total)/100;
                 DecimalFormat df = new DecimalFormat("#.##");
                 String priceStr = df.format(price);
 
                 sender.sendMessage(amount + " of " + from + " is worth $" + priceStr);
             } else {
                 sender.sendMessage("/worth <item name|hand> <amount>");
             }
         } else if ("price".equalsIgnoreCase(command)) {
             if (itemName == null) {
                 sender.sendMessage("This command is not currently available");
                 return true;
             }
             if (permission == null || !permission.hasPermission(playerName,"money_price")) {
                 sender.sendMessage("You do not have permission to use this command (money_price)");
                 return true;
             }
 
             if (args.length > 0) {
                 String item = args[0];
 
                 if (itemName.getItemFromName(item) == null) {
                     sender.sendMessage(item + " is not a real item");
                     return true;
                 }
 
                 if (args.length > 1) {
                     if (permission == null || !permission.hasPermission(playerName,"money_setprice")) {
                         sender.sendMessage("You do not have permission to use this command (money_setprice)");
                         return true;
                     }
                     int amount = -1;
                     try {
                         amount = Integer.parseInt(args[1]);
                     } catch (NumberFormatException e) {
                         sender.sendMessage("Price must be a number (no decimals, so 10.05 would be 1005)");
                         return true;
                     }
 
                     Price price = null;
                     List<Price> priceList = (List<Price>)database.select(Price.class,"name = '" + item + "'");
                     if (priceList.size() != 0) {
                         price = priceList.get(0);
                     } else {
                         price = new Price();
                         price.setName(item);
                     }
                     price.setPrice(amount);
                     price.save(database);
                 }
 
                 double price = ((double)getPrice(item))/100;
                 if (price < 0) {
                     sender.sendMessage("Item does not currently have a set price");
                 } else {
                     DecimalFormat df = new DecimalFormat("#.##");
                     String priceStr = df.format(price);
                     sender.sendMessage("Price for " + item + " is $" + priceStr);
                 }
             } else {
                 sender.sendMessage("/price <item> <price>");
             }
         } else {
             return false;
         }
 
         return true;
     }
 }
