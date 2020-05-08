 package com.nijiko.simpleshop;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.simpleshop.util.CLI;
 import com.nijiko.simpleshop.util.Misc;
 import com.nijiko.simpleshop.util.Messaging;
 import com.nijiko.simpleshop.util.Template;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * iListen.java
  * <br /><br />
  * Listens for calls from bukkit, and reacts accordingly.
  *
  * @author Nijikokun <nijikokun@gmail.com>
  */
 public class iListen extends PlayerListener {
 
     private static final Logger log = Logger.getLogger("Minecraft");
     /**
      * Miscellaneous object for various functions that don't belong anywhere else
      */
     public Misc Misc = new Misc();
     public static SimpleShop plugin;
     public CLI Commands;
     private Template Template = null;
 
     public iListen(SimpleShop instance) {
         plugin = instance;
 
         Template = new Template("plugins" + File.separator + "iConomy" + File.separator, "Messages.yml");
 
         Commands = new CLI();
 
         Commands.add("/shop -help|?");
         Commands.add("/shop -list|-l +page:1");
         Commands.add("/shop -buy|-b +item:-1 +who:nobodyreallylongname");
         Commands.add("/shop -sell|-s +item:-1");
         Commands.add("/shop -add|-a +item +buy:0 +sell:0 +stock:0");
         Commands.add("/shop -update|-u +item +buy:0 +sell:0 +stock:0 +type:-1");
         Commands.add("/shop -remove|-r +item:-1");
         Commands.add("/shop -check|-c +item:-1");
     }
 
     /**
      * Sends simple condensed help lines to the current player
      */
     private void showSimpleHelp() {
         Messaging.send("&e----------------------------------------------------");
         Messaging.send("&f " + plugin.name + " (&c" + plugin.codename + "&f)   ");
         Messaging.send("&e----------------------------------------------------");
         Messaging.send("&f [] Required, () Optional                            ");
         Messaging.send("&e----------------------------------------------------");
         Messaging.send("&f /shop check [id] - information about item           ");
         Messaging.send("&f /shop list - List items for sell / purchase ");
         Messaging.send("&f /shop sell [id]([:amount]) - Sell an item");
         Messaging.send("&f /shop buy [id]([:amount]) - Purchase an item ");
         Messaging.send("&f /shop remove [id] - information about item");
         Messaging.send("&e----------------------------------------------------");
     }
 
     private double getBalance(String name) {
         return iConomy.getBank().getAccount(name).getBalance();
     }
 
     private void setBalance(String name, double amount) {
         iConomy.getBank().getAccount(name).setBalance(amount);
     }
     
     private void subtract(String name, double amount) {
         iConomy.getBank().getAccount(name).subtract(amount);
         iConomy.getBank().getAccount(name).save();
     }
 
     private void add(String name, double amount) {
         iConomy.getBank().getAccount(name).add(amount);
         iConomy.getBank().getAccount(name).save();
     }
 
     /**
      * Shows the balance to the requesting player.
      *
      * @param name The name of the player we are viewing
      * @param viewing The player who is viewing the account
      * @param mine Is it the player who is trying to view?
      */
     public void showBalance(Player viewing) {
         Messaging.send(viewing, Template.color("tag") + Template.parse("personal.balance", new String[]{"+balance,+b"}, new String[]{iConomy.getBank().format(viewing.getName())}));
     }
 
     public void onPlayerCommand(Player player, String message) {
         double balance = getBalance(player.getName());
 
         // Save player.
         Messaging.save(player);
 
         // Commands
         Commands.save(message);
 
         // Parsing / Checks
         String base = Commands.base();
         String command = Commands.command();
         ArrayList<Object> variables = Commands.parse();
 
         if (base != null) {
             if (Misc.is(base, "shop")) {
                 if (command == null) {
                     showSimpleHelp();
                     return;
                 }
 
                 if (command.equals("check")) {
                     String item = Commands.getString("item");
                     int itemId = Items.validate(item);
                     int itemType = Items.validateGrabType(item);
 
                     if (itemId == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid item.");
                         return;
                     }
 
                     int[] data = SimpleShop.Database.data(itemId, itemType);
 
                     if (data[0] == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Item currently not for purchase.");
                         return;
                     }
 
                     Messaging.send(SimpleShop.shop_tag + "Item &d" + Items.name(itemId) + "&f info:");
 
                     if (data[2] != -1) {
                         Messaging.send("Purchasable: &d[&f" + data[4] + "&d]&f for &d" + iConomy.getBank().format(data[2]));
                     }
 
                     if (data[3] != -1) {
                         Messaging.send("Sellable: &d[&f" + data[4] + "&d]&f for &d" + iConomy.getBank().format(data[3]));
                     }
 
                     if (SimpleShop.utilize_stock) {
                         Messaging.send("Current Stock: &d" + data[5]);
                     }
 
                     return;
                 }
 
                 if (Misc.isEither(command, "help", "?")) {
                     showSimpleHelp();
                     return;
                 }
 
                 if (Misc.isEither(command, "list", "l")) {
                     ArrayList<int[]> list = SimpleShop.Database.list();
                     int per_page = 7;
                     int current_page = (Commands.getInteger("page") == 0) ? 1 : Commands.getInteger("page");
                     int size = 0;
 
                     for (int[] data : list) {
                         if (data[2] != -1 && data[3] != -1) {
                             size++;
                         }
                     }
 
                     int amount_pages = (int) Math.ceil(size / per_page) + 1;
                     int page_values = (current_page - 1) * per_page;
                     int page_show = (current_page - 1) * per_page + per_page;
 
                     if (list.isEmpty()) {
                         Messaging.send(SimpleShop.shop_tag + "&7No items available.");
                     } else if (current_page > amount_pages) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid page number.");
                     } else {
                         Messaging.send(SimpleShop.shop_tag + "&dPage &f#" + current_page + "&d of &f" + amount_pages + " &dpages.");
 
                         for (int i = page_values; i < page_show; i++) {
                             if (list.size() - 1 >= i) {
                                 int[] data = list.get(i);
 
                                 if (data[2] == -1 && data[3] == -1) {
                                     continue;
                                 }
 
                                 String buy = (data[2] != -1) ? " Buy &d[&f" + data[2] + "&d]&f" : "";
                                 String sell = (data[3] != -1) ? " Sell &d[&f" + data[3] + "&d]" : "";
                                 String stock = (SimpleShop.utilize_stock && data[5] != 0) ? " Stock &d[&f" + data[5] + "&d]" : "";
 
                                 Messaging.send("Item &d[&f" + Items.name(data[0]) + " &dx &f" + data[4] + "&d]&f" + buy + sell + stock);
                             } else {
                                 break;
                             }
                         }
                     }
 
                     return;
                 }
 
                 if (Misc.isEither(command, "buy", "b")) {
                     if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.buy")) {
                         return;
                     }
 
                     int[] itemData = Items.parse(Commands.getString("item"));
                     int itemId = itemData[1];
                     int itemType = itemData[2];
                     int amount = itemData[0];
 
                     String who = Commands.getString("who");
 
                     if (itemId == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid item.");
                         return;
                     }
 
                     int[] data = SimpleShop.Database.data(itemId, itemType);
                     int cost = (data[2] * amount);
                     int total = (amount * data[4]);
 
                     if (data[0] == -1 || data[2] == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Item currently not for sale."); return;
                     }
 
                     if (amount < 1 || total > SimpleShop.max_per_purchase) {
                         Messaging.send(SimpleShop.shop_tag + "&7Amount over max per purchase."); return;
                     }
 
                     if (balance < cost) {
                         Messaging.send(SimpleShop.shop_tag + "&7You do not have enough &f" + iConomy.getBank().getCurrency() + "&7 to do this."); return;
                     }
 
                     if (SimpleShop.utilize_stock && total < data[5]) {
                         Messaging.send(SimpleShop.shop_tag + "&7Currently &f" + Items.name(itemId) + "&7 is low in stock."); return;
                     }
                     
                     if(!who.equalsIgnoreCase("nobodyreallylongname")) {
                         if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.buy.gift")) {
                             return;
                         }
                         
                         Player gifted = Misc.playerMatch(who);
                         
                         if(gifted != null) {
                             subtract(player.getName(), cost);
                             Items.give(gifted, itemId, itemType, total);
 
                             Messaging.send(
                                 SimpleShop.shop_tag + "Gift Purchased &d[&f" + total + "&d]&f " +  Items.name(itemId) + " for " + who + " cost &d" + iConomy.getBank().format(total)
                             );
                             
                             Messaging.send(gifted,
                                 SimpleShop.shop_tag +  "Gift Recieved &d[&f" + total + "&d]&f " +  Items.name(itemId) + " from " + player.getName()
                             );
 
                             showBalance(player);
                         } else {
                             Messaging.send(SimpleShop.shop_tag + "&7Sorry, " + who + " does not exist.."); return;
                         }
                     } else {
                        subtract(player.getName(), cost);
                         Items.give(player, itemId, itemType, total);
 
                         Messaging.send(
                             SimpleShop.shop_tag +
                            "Purchased &d[&f" + total + "&d]&f " +  Items.name(itemId) + " for &d" + iConomy.getBank().format(total)
                         );
 
                         showBalance(player);
                     }
                 }
 
                 if (Misc.isEither(command, "sell", "s")) {
                     if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.sell")) {
                         return;
                     }
 
                     int[] itemData = Items.parse(Commands.getString("item"));
                     int itemId = itemData[1];
                     int itemType = itemData[2];
                     int amount = itemData[0];
 
                     int[] data = SimpleShop.Database.data(itemId, itemType);
 
                     if (data[0] == -1 || data[3] == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Item currently not for purchase.");
                         return;
                     }
 
                     if (amount < 1 || (amount * data[4]) > SimpleShop.max_per_sale) {
                         Messaging.send(SimpleShop.shop_tag + "&7Amount over max sell amount.");
                         return;
                     }
 
                     if (itemType != -1) {
                         if (!Items.has(player, itemId, (amount * data[4]))) {
                             Messaging.send(SimpleShop.shop_tag + "&7You do not have enough " + Items.name(itemId) + " to do this.");
                             return;
                         }
                     } else {
                         if (!Items.has(player, itemId, (amount * data[4]))) {
                             Messaging.send(SimpleShop.shop_tag + "&7You do not have enough " + Items.name(itemId) + " to do this.");
                             return;
                         }
                     }
 
                     if (SimpleShop.utilize_stock) {
                         SimpleShop.Database.update(data[0], data[1], data[1], data[2], data[3], data[4], (data[5] + (amount * data[4])));
                     }
 
                     Items.remove(player, itemId, (amount * data[4]));
                    add(player.getName(), (data[3] * amount));
 
                     Messaging.send(SimpleShop.shop_tag + "Sold &d[&f" + (amount * data[4]) + "&d]&f " + Items.name(itemId) + " for &d" + iConomy.getBank().format((data[3] * amount)));
 
                     if (SimpleShop.utilize_stock) {
                         Messaging.send(SimpleShop.shop_tag + "Current Stock: &f" + (data[5] + (amount * data[4])));
                     }
 
                     showBalance(player);
                 }
 
                 if (Misc.isEither(command, "reload", "r")) {
                     if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.items.reload")) {
                         return;
                     }
 
                     plugin.onEnable();
                     return;
                 }
 
                 if (Misc.isEither(command, "add", "a")) {
                     if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.items.add")) {
                         return;
                     }
 
                     int[] itemData = Items.parse(Commands.getString("item"));
                     int itemId = itemData[1];
                     int itemType = itemData[2];
                     int amount = itemData[0];
 
                     int buy = Commands.getInteger("buy");
                     int sell = Commands.getInteger("sell");
                     int stock = Commands.getInteger("stock");
 
                     if (itemId == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid item.");
                         return;
                     }
 
                     if (buy == -1 && sell == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Buy & Sell cannot both be -1.");
                         return;
                     }
 
                     if (amount < 1 || amount > SimpleShop.max_per_sale) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid amounts.");
                         return;
                     }
 
                     if (SimpleShop.utilize_stock && stock < 0) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid stock amount.");
                         return;
                     }
 
                     SimpleShop.Database.add(itemId, itemType, buy, sell, amount, stock);
                     Messaging.send(SimpleShop.shop_tag + "Item " + Items.name(itemId) + " added:");
 
                     if (buy != -1) {
                         Messaging.send("Purchasable: &d[&f" + amount + "&d]&f for &d" + iConomy.getBank().format(buy));
                     }
 
                     if (sell != -1) {
                         Messaging.send("Sellable: &d[&f" + amount + "&d]&f for &d" + iConomy.getBank().format(sell));
                     }
 
                     if (SimpleShop.utilize_stock) {
                         Messaging.send("Current Stock: &d" + stock);
                     }
 
                     return;
                 }
 
                 if (Misc.isEither(command, "update", "u")) {
                     if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.items.update")) {
                         return;
                     }
 
                     int[] itemData = Items.parse(Commands.getString("item"));
                     int itemId = itemData[1];
                     int itemType = itemData[2];
                     int amount = itemData[0];
 
                     int oldType = Commands.getInteger("type");
                     int buy = Commands.getInteger("buy");
                     int sell = Commands.getInteger("sell");
                     int stock = Commands.getInteger("stock");
 
                     if (itemId == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid item.");
                         return;
                     }
 
                     if (buy == -1 && sell == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Buy & Sell cannot both be -1.");
                         return;
                     }
 
                     if (amount < 1 || amount > SimpleShop.max_per_sale) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid amounts.");
                         return;
                     }
 
                     if (SimpleShop.utilize_stock && stock < 0) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid stock amount.");
                         return;
                     }
 
                     SimpleShop.Database.update(itemId, oldType, itemType, buy, sell, amount, stock);
                     Messaging.send(SimpleShop.shop_tag + "Item " + Items.name(itemId) + " updated:");
 
                     if (buy != -1) {
                         Messaging.send("Purchasable: &d[&f" + amount + "&d]&f for &d" + iConomy.getBank().format(buy));
                     }
 
                     if (sell != -1) {
                         Messaging.send("Sellable: &d[&f" + amount + "&d]&f for &d" + iConomy.getBank().format(sell));
                     }
 
                     if (SimpleShop.utilize_stock) {
                         Messaging.send("Current Stock: &d" + stock);
                     }
                     return;
                 }
 
                 if (Misc.isEither(command, "remove", "rm")) {
                     if (!SimpleShop.Permissions.Security.permission(player, "simpleshop.items.remove")) {
                         return;
                     }
 
                     String item = Commands.getString("item");
                     int itemId = Items.validate(item);
                     int itemType = Items.validateGrabType(item);
 
                     if (itemId == -1) {
                         Messaging.send(SimpleShop.shop_tag + "&7Invalid item.");
                         return;
                     }
 
                     SimpleShop.Database.remove(itemId);
                     Messaging.send(SimpleShop.shop_tag + "Item(s) " + Items.name(itemId) + " was removed.");
                     return;
                 }
             }
         }
     }
 }
