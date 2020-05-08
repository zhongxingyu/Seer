 package me.Sk8r2K10.sGift;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class sGiftCommandExecutor implements CommandExecutor {
 
     private sGift plugin;
     ArrayList<Gift> gifts = new ArrayList<Gift>();
     ArrayList<Trade> trades = new ArrayList<Trade>();
     ArrayList<Sender> senders = new ArrayList<Sender>();
 
     public sGiftCommandExecutor(sGift instance) {
         plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
         Player player = null;
         String prefix = ChatColor.WHITE + "[" + ChatColor.GREEN + "sGift" + ChatColor.WHITE + "] ";
         String prefix2 = ChatColor.WHITE + "[" + ChatColor.GOLD + "sGift" + ChatColor.WHITE + "] ";
         String prefix3 = ChatColor.DARK_RED + "[" + ChatColor.RED + "sGift" + ChatColor.DARK_RED + "] ";
         Logger log = Logger.getLogger("Minecraft");
 
         if (sender instanceof Player) {
 
             player = (Player) sender;
         }
         if (commandLabel.equalsIgnoreCase("gift") && sender.hasPermission("sgift.gift")) {
             if (plugin.getConfig().getBoolean("enable-gift")) {
                 if (player == null) {
 
                     log.warning(prefix + ChatColor.RED + "Don't send sGift commands through console!");
 
                 } else if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("help") && player.hasPermission("sgift.gift.help")) {
 
                         player.sendMessage(ChatColor.DARK_GRAY + "----------------[" + ChatColor.GREEN + "sGift - Gift Help Menu" + ChatColor.DARK_GRAY + "]-----------------");
                         player.sendMessage(plugin.getConfig().getString("Help.Gift.Gift"));
                         player.sendMessage(plugin.getConfig().getString("Help.Gift.Example"));
                         player.sendMessage(plugin.getConfig().getString("Help.Gift.Accept"));
                         player.sendMessage(plugin.getConfig().getString("Help.Gift.Deny"));
                         player.sendMessage(plugin.getConfig().getString("Help.Gift.Cancel"));
                         player.sendMessage(plugin.getConfig().getString("Help.Gift.Help"));
 
                         if (player.hasPermission("sgift.admin")) {
 
                             player.sendMessage(plugin.getConfig().getString("Help.Gift.Stop"));
                         }
 
                     } else if (args[0].equalsIgnoreCase("accept")) {
 
                         Gift gift = null;
                         Sender Sender1 = null;
 
                         for (Gift g : gifts) {
 
                             if (g.Victim == player) {
 
                                 gift = g;
 
                                 for (Sender s : senders) {
 
                                     if (s.Sender == g.playerSender) {
 
                                         Sender1 = s;
                                     }
                                 }
                             }
                         }
 
                         if (gift == null) {
 
                             player.sendMessage(prefix + ChatColor.RED + "No Gifts to accept!");
                         } else {
 
                             Player playerSendingItems = gift.playerSender;
                             Player Victim = gift.Victim;
                             ItemStack items = gift.itemStack;
 
                             if (player.getInventory().firstEmpty() == -1) {
                                 Location playerloc = player.getLocation();
                                 player.getWorld().dropItemNaturally(playerloc, items);
                                 player.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                             } else {
                                 Victim.getInventory().addItem(items);
                             }
 
                             playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + "!");
                             Victim.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerSendingItems.getDisplayName() + ChatColor.WHITE + "!");
                             log.info(prefix + Victim.getDisplayName() + " recieved " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName());
 
                             gifts.remove(gift);
                             senders.remove(Sender1);
                         }
 
                     } else if (args[0].equalsIgnoreCase("deny")) {
 
                         Gift gift = null;
                         Sender Sender1 = null;
 
                         for (Gift g : gifts) {
 
                             if (g.Victim == player) {
 
                                 gift = g;
 
                                 for (Sender s : senders) {
 
                                     if (s.Sender == g.playerSender) {
 
                                         Sender1 = s;
                                     }
                                 }
                             }
                         }
 
                         if (gift == null) {
 
                             player.sendMessage(prefix + ChatColor.RED + "No Gifts to deny!");
                         } else {
 
                             Player playerSendingItems = gift.playerSender;
                             Player Victim = gift.Victim;
                             ItemStack items = gift.itemStack;
 
                             if (playerSendingItems.getInventory().firstEmpty() == -1) {
                                 Location playerloc = playerSendingItems.getLocation();
                                 playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                                 playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                             }
                             if (!playerSendingItems.isOnline()) {
                                 player.sendMessage(prefix + ChatColor.RED + "Player sending items is not Online!");
                                 player.sendMessage(prefix + ChatColor.RED + "Please wait for " + playerSendingItems.getName() + " to come back online!");
 
                             } else {
                                 playerSendingItems.getInventory().addItem(items);
                                 playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Gift request!");
                                 playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                                 Victim.sendMessage(prefix + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Gift!");
                                 log.info(prefix + Victim.getDisplayName() + " denied " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName());
 
                                 gifts.remove(gift);
                                 senders.remove(Sender1);
                             }
                         }
 
                     } else if (args[0].equalsIgnoreCase("stop") && player.hasPermission("sgift.admin")) {
                         while (gifts.size() > 0) {
 
                             Gift gift = null;
                             Sender Sender1 = null;
 
                             for (Gift g : gifts) {
 
                                 if (g.itemStack != null) {
                                     gift = g;
 
                                     for (Sender s : senders) {
 
                                         if (s.Sender != null) {
 
                                             Sender1 = s;
                                         }
                                     }
                                 }
                             }
 
                             if (gift == null) {
 
                                 player.sendMessage(prefix + ChatColor.RED + "No Gifts to stop!");
                             } else {
 
                                 Player playerSendingItems = gift.playerSender;
                                 Player Victim = gift.Victim;
                                 ItemStack items = gift.itemStack;
 
                                 if (playerSendingItems.getInventory().firstEmpty() == -1) {
                                     Location playerloc = playerSendingItems.getLocation();
                                     playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                                     playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                                 } else {
                                     playerSendingItems.getInventory().addItem(items);
                                 }
 
                                 playerSendingItems.sendMessage(prefix + ChatColor.RED + "Your Gift has been cancelled by an Admin!");
                                 playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " has been returned to you.");
                                 Victim.sendMessage(prefix + ChatColor.RED + "Admin cancelled your Gift.");
                                 log.info(prefix + "stopped a gift of " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName());
 
                                 gifts.remove(gift);
                                 senders.remove(Sender1);
                             }
                         }
                         player.sendMessage(prefix + ChatColor.GREEN + "Cancelled all Gifts safely.");
 
                     } else if (args[0].equalsIgnoreCase("cancel")) {
 
                         Gift gift = null;
                         Sender Sender1 = null;
 
                         for (Gift g : gifts) {
 
                             if (g.playerSender == player) {
 
                                 gift = g;
                             }
                         }
 
                         for (Sender s : senders) {
 
                             if (s.Sender == player) {
 
                                 Sender1 = s;
                             }
 
                         }
 
                         if (gift == null) {
 
                             player.sendMessage(prefix + ChatColor.RED + "No Gifts to cancel!");
                         } else {
 
                             Player playerSendingItems = gift.playerSender;
                             Player Victim = gift.Victim;
                             ItemStack items = gift.itemStack;
 
                             if (playerSendingItems.getInventory().firstEmpty() == -1) {
                                 Location playerloc = playerSendingItems.getLocation();
                                 playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                                 playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                             } else {
                                 playerSendingItems.getInventory().addItem(items);
                             }
 
                             playerSendingItems.sendMessage(prefix + ChatColor.RED + "Cancelled gift!");
                             playerSendingItems.sendMessage(prefix + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                             Victim.sendMessage(prefix + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Gift!");
 
                             senders.remove(Sender1);
                             gifts.remove(gift);
                         }
 
                     } else if (Bukkit.getServer().getPlayer(args[0]) == null) {
 
                         player.sendMessage(prefix + ChatColor.RED + "Player not Online.");
 
                     } else if (Bukkit.getServer().getPlayer(args[0]) == player) {
 
                         player.sendMessage(prefix + ChatColor.RED + "Don't gift Items to yourself!");
 
                     } else {
 
                         player.sendMessage(prefix + ChatColor.RED + "Too few arguments!");
                         player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
 
                     }
                 } else if (args.length == 2) {
 
                     player.sendMessage(prefix + ChatColor.RED + "Too Few arguments!");
                     player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
 
                 } else if (args.length == 3) {
                     if (Bukkit.getServer().getPlayer(args[0]) != player) {
                         if (Bukkit.getServer().getPlayer(args[0]) != null) {
 
                             Player Victim = Bukkit.getServer().getPlayer(args[0]);
                             int amount = plugin.getInt(args[2]);
 
                             if (Items.parse(args[1], amount) != null) {
 
                                 ItemStack Item = new ItemStack(Items.parse(args[1], amount));
 
                                 if (amount != 0) {
                                     if (plugin.inventoryContains(player.getInventory(), Item)) {
                                         if (Item.getEnchantments().isEmpty()) {
 
                                             gifts.add(new Gift(Victim, player, Item));
                                             senders.add(new Sender(player));
 
                                             player.getInventory().removeItem(Item);
 
                                             player.sendMessage(prefix + ChatColor.WHITE + "Now Gifting " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item) + ChatColor.WHITE + " with " + ChatColor.YELLOW + Victim.getName());
                                             player.sendMessage(prefix + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
                                             Victim.sendMessage(prefix + ChatColor.WHITE + "New Gift from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item));
                                             Victim.sendMessage(prefix + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/Gift accept" + ChatColor.WHITE + " to accept this Gift or " + ChatColor.YELLOW + "/Gift deny" + ChatColor.WHITE + " to deny this Gift!");
 
                                         } else {
 
                                             player.sendMessage(prefix + ChatColor.RED + "You can't Gift enchanted Items! (Yet)");
                                         }
 
                                     } else {
 
                                         player.sendMessage(prefix + ChatColor.RED + "You don't have enough " + Items.name(Item) + ", or Item is partially Used!");
                                     }
 
                                 } else {
 
                                     player.sendMessage(prefix + ChatColor.RED + "Amount provided is Invalid!");
                                 }
                             } else {
                                 player.sendMessage(prefix + ChatColor.RED + "Material provided is Invalid!");
                             }
 
                         } else {
 
                             player.sendMessage(prefix + ChatColor.RED + "Player not Online!");
                         }
 
                     } else {
 
                         player.sendMessage(prefix + ChatColor.RED + "You can't Gift yourself!");
                     }
 
                 } else if (args.length == 0) {
 
                     player.sendMessage(prefix + ChatColor.RED + "By Sk8r2K9. /gift help for more info.");
 
                 } else if (args.length >= 4) {
 
                     player.sendMessage(prefix + ChatColor.RED + "Too many arguments!");
                     player.sendMessage(prefix + ChatColor.GRAY + "Correct usage: /gift <Player> <Item> <Amount>");
                 }
             } else {
 
                 player.sendMessage(prefix + ChatColor.RED + "Gifting is currently disabled!");
             }
 
         } else if (commandLabel.equalsIgnoreCase("trade") && sender.hasPermission("sgift.trade")) {
             if (plugin.getConfig().getBoolean("enable-trade")) {
                 if (player == null) {
 
                     log.warning(prefix2 + ChatColor.RED + "Don't send sGift commands through console!");
 
                 } else if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("help") && player.hasPermission("sgift.trade.help")) {
 
                         player.sendMessage(ChatColor.DARK_GRAY + "---------------[" + ChatColor.GOLD + "sGift - Trade Help Menu" + ChatColor.DARK_GRAY + "]----------------");
                         player.sendMessage(plugin.getConfig().getString("Help.Trade.Trade"));
                         player.sendMessage(plugin.getConfig().getString("Help.Trade.Example"));
                         player.sendMessage(plugin.getConfig().getString("Help.Trade.Accept"));
                         player.sendMessage(plugin.getConfig().getString("Help.Trade.Deny"));
                         player.sendMessage(plugin.getConfig().getString("Help.Trade.Cancel"));
                         player.sendMessage(plugin.getConfig().getString("Help.Trade.Help"));
 
                         if (player.hasPermission("sgift.admin")) {
 
                             player.sendMessage(plugin.getConfig().getString("Help.Trade.Stop"));
                         }
 
                     } else if (args[0].equalsIgnoreCase("accept")) {
 
                         Trade trade = null;
                         Sender Sender1 = null;
 
                         for (Trade t : trades) {
 
                             if (t.Victim == player) {
 
                                 trade = t;
 
                                 for (Sender s : senders) {
 
                                     if (s.Sender == t.playerSender) {
 
                                         Sender1 = s;
                                     }
                                 }
                             }
                         }
 
                         if (trade == null) {
 
                             player.sendMessage(prefix2 + ChatColor.RED + "No Trades to accept!");
                         } else {
 
                             Player playerSendingItems = trade.playerSender;
                             Player Victim = trade.Victim;
                             ItemStack items = trade.itemStack;
                             int price = trade.price;
 
                             if (player.getInventory().firstEmpty() == -1) {
                                 Location playerloc = player.getLocation();
                                 player.getWorld().dropItemNaturally(playerloc, items);
                                 player.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                             } else {
                                 player.getInventory().addItem(items);
                             }
 
 
                             plugin.getEcon().withdrawPlayer(Victim.getName(), price);
                             plugin.getEcon().depositPlayer(playerSendingItems.getName(), price);
 
                             playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Delivered to " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.GOLD + price + plugin.getEcon().currencyNameSingular() + "(s)");
                             Victim.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.WHITE + " Recieved from " + ChatColor.YELLOW + playerSendingItems.getDisplayName() + ChatColor.WHITE + " for " + ChatColor.GOLD + price + plugin.getEcon().currencyNameSingular() + "(s)");
                             log.info(prefix2 + Victim.getDisplayName() + " recieved " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName() + " for " + price + plugin.getEcon().currencyNameSingular() + "(s)");
 
                             trades.remove(trade);
                             senders.remove(Sender1);
                         }
 
                     } else if (args[0].equalsIgnoreCase("deny")) {
 
                         Trade trade = null;
                         Sender Sender1 = null;
 
                         for (Trade t : trades) {
 
                             if (t.Victim == player) {
 
                                 trade = t;
 
                                 for (Sender s : senders) {
 
                                     if (s.Sender == t.playerSender) {
 
                                         Sender1 = s;
                                     }
                                 }
                             }
                         }
 
                         if (trade == null) {
 
                             player.sendMessage(prefix2 + ChatColor.RED + "No Trades to deny!");
                         } else {
 
                             Player playerSendingItems = trade.playerSender;
                             Player Victim = trade.Victim;
                             ItemStack items = trade.itemStack;
                             int price = trade.price;
 
                             if (playerSendingItems.getInventory().firstEmpty() == -1) {
                                 Location playerloc = playerSendingItems.getLocation();
                                 playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                                 playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                             }
                             if (!playerSendingItems.isOnline()) {
                                 player.sendMessage(prefix + ChatColor.RED + "Player sending items is not Online!");
                                 player.sendMessage(prefix + ChatColor.RED + "Please wait for " + playerSendingItems.getName() + " to come back online!");
                             } else {
                                 playerSendingItems.getInventory().addItem(items);
                                 playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + Victim.getDisplayName() + ChatColor.RED + " has Denied your Trade request!");
                                 playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                                 Victim.sendMessage(prefix2 + ChatColor.RED + "You denied " + playerSendingItems.getName() + "'s Trade!");
                                 log.info(prefix2 + Victim.getDisplayName() + " denied " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName() + " for " + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
 
                                 trades.remove(trade);
                                 senders.remove(Sender1);
                             }
                         }
 
                     } else if (args[0].equalsIgnoreCase("stop") && player.hasPermission("sgift.admin")) {
                         while (trades.size() > 0) {
 
                             Trade trade = null;
                             Sender Sender1 = null;
 
                             for (Trade t : trades) {
 
                                 if (t.itemStack != null) {
 
                                     trade = t;
 
                                     for (Sender s : senders) {
 
                                         if (s.Sender != null) {
 
                                             Sender1 = s;
                                         }
                                     }
                                 }
                             }
                             if (trade == null) {
 
                                 player.sendMessage(prefix2 + ChatColor.RED + "No Trades to stop!");
                             } else {
 
                                 Player playerSendingItems = trade.playerSender;
                                 Player Victim = trade.Victim;
                                 ItemStack items = trade.itemStack;
                                 int price = trade.price;
 
                                 if (playerSendingItems.getInventory().firstEmpty() == -1) {
                                     Location playerloc = playerSendingItems.getLocation();
                                     playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                                     playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                                 } else {
                                     playerSendingItems.getInventory().addItem(items);
                                 }
 
                                 playerSendingItems.sendMessage(prefix2 + ChatColor.RED + "Your Trade has been cancelled by an Admin!");
                                 playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " has been returned to you.");
                                 Victim.sendMessage(prefix2 + ChatColor.RED + "Admin cancelled your Trade.");
                                 log.info(prefix2 + "stopped a trade of " + items.getAmount() + " " + Items.name(items) + " from " + playerSendingItems.getDisplayName() + " for " + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
 
                                 trades.remove(trade);
                                 senders.remove(Sender1);
                             }
                         }
                         player.sendMessage(prefix2 + ChatColor.GREEN + "Cancelled all Trades safely.");
 
                     } else if (args[0].equalsIgnoreCase("cancel")) {
 
                         Trade trade = null;
                         Sender Sender1 = null;
 
                         for (Trade t : trades) {
 
                             if (t.playerSender == player) {
 
                                 trade = t;
                             }
                         }
 
                         for (Sender s : senders) {
 
                             if (s.Sender == player) {
 
                                 Sender1 = s;
                             }
 
                         }
 
                         if (trade == null) {
 
                             player.sendMessage(prefix2 + ChatColor.RED + "No Trades to cancel!");
                         } else {
 
                             Player playerSendingItems = trade.playerSender;
                             Player Victim = trade.Victim;
                             ItemStack items = trade.itemStack;
                             int price = trade.price;
 
                             if (playerSendingItems.getInventory().firstEmpty() == -1) {
                                 Location playerloc = playerSendingItems.getLocation();
                                 playerSendingItems.getWorld().dropItemNaturally(playerloc, items);
                                 playerSendingItems.sendMessage(prefix2 + "Inventory full! Dropped Items at your feet!");
 
                             } else {
                                 playerSendingItems.getInventory().addItem(items);
                             }
 
                             playerSendingItems.sendMessage(prefix2 + ChatColor.RED + "Cancelled trade!");
                             playerSendingItems.sendMessage(prefix2 + ChatColor.YELLOW + items.getAmount() + " " + Items.name(items) + ChatColor.RED + " Has been returned to you.");
                             Victim.sendMessage(prefix2 + ChatColor.YELLOW + playerSendingItems.getName() + ChatColor.RED + " Cancelled the Trade!");
 
                             trades.remove(trade);
                             senders.remove(Sender1);
                         }
 
 
                     } else if (Bukkit.getServer().getPlayer(args[0]) == null) {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "Player not Online.");
 
                     } else if (Bukkit.getServer().getPlayer(args[0]) == player) {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "Don't trade Items with yourself!");
 
                     } else {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "Too few arguments!");
                         player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
 
                     }
                 } else if (args.length == 2) {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "Too Few arguments!");
                     player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
 
                 } else if (args.length == 3) {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "Too Few arguments!");
                     player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
 
                 } else if (args.length == 4) {
                     if (Bukkit.getServer().getPlayer(args[0]) != player) {
                         if (Bukkit.getServer().getPlayer(args[0]) != null) {
 
                             int price = plugin.getInt(args[3]);
                             Player Victim = Bukkit.getServer().getPlayer(args[0]);
                             int amount = plugin.getInt(args[2]);
 
                             if (Items.parse(args[1], amount) != null) {
 
                                 ItemStack Item = new ItemStack(Items.parse(args[1], amount));
 
                                 if (amount != 0) {
                                     if (price != 0) {
                                         if (plugin.inventoryContains(player.getInventory(), Item)) {
                                             if (Item.getEnchantments().isEmpty()) {
                                                 if (plugin.getEcon().getBalance(Victim.getName()) >= price) {
 
                                                     trades.add(new Trade(Victim, player, Item, price));
                                                     senders.add(new Sender(player));
 
                                                     player.getInventory().removeItem(Item);
 
                                                     player.sendMessage(prefix2 + ChatColor.WHITE + "Now Trading " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item) + ChatColor.WHITE + " with " + ChatColor.YELLOW + Victim.getName() + ChatColor.WHITE + " for " + ChatColor.GOLD + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
                                                     player.sendMessage(prefix2 + ChatColor.YELLOW + "Waiting for " + Victim.getName() + " to accept...");
                                                     Victim.sendMessage(prefix2 + ChatColor.WHITE + "New Trade from " + ChatColor.YELLOW + player.getDisplayName() + ChatColor.WHITE + " of " + ChatColor.YELLOW + Item.getAmount() + " " + Items.name(Item) + ChatColor.WHITE + " for " + ChatColor.GOLD + price + " " + plugin.getEcon().currencyNameSingular() + "(s)");
                                                     Victim.sendMessage(prefix2 + ChatColor.WHITE + "Do " + ChatColor.YELLOW + "/trade accept" + ChatColor.WHITE + " to accept this Trade or " + ChatColor.YELLOW + "/trade deny" + ChatColor.WHITE + " to deny this trade!");
                                                 } else {
 
                                                     player.sendMessage(prefix2 + ChatColor.RED + "That player doesn't have enough money!");
                                                 }
 
                                             } else {
 
                                                 player.sendMessage(prefix2 + ChatColor.RED + "You can't trade enchanted Items! (Yet)");
                                             }
 
                                         } else {
 
                                             player.sendMessage(prefix2 + ChatColor.RED + "You don't have enough " + Items.name(Item) + ", or Item is partially Used!");
                                         }
 
 
                                     } else {
                                         player.sendMessage(prefix2 + ChatColor.RED + "Price provided is Invalid!");
                                     }
 
                                 } else {
 
                                     player.sendMessage(prefix2 + ChatColor.RED + "Amount provided is Invalid!");
                                 }
                             } else {
                                 player.sendMessage(prefix2 + ChatColor.RED + "Material provided is Invalid!");
                             }
 
                         } else {
 
                             player.sendMessage(prefix2 + ChatColor.RED + "Player not Online!");
                         }
 
                     } else {
 
                         player.sendMessage(prefix2 + ChatColor.RED + "You can't Trade with yourself!");
                     }
 
                 } else if (args.length == 0) {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "By Sk8r2K9. /trade help for more info");
 
                 } else if (args.length >= 5) {
 
                     player.sendMessage(prefix2 + ChatColor.RED + "Too many arguments!");
                     player.sendMessage(prefix2 + ChatColor.GRAY + "Correct usage: /trade <Player> <Item> <Amount> <Price>");
                 }
             } else {
 
                 player.sendMessage(prefix2 + ChatColor.RED + "Trading is Currently disabled!");
             }
 
 
         } else if (commandLabel.equalsIgnoreCase("sgift") && sender.hasPermission("sgift.sgift")) {
             if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("info")) {
 
                     String info1 = Boolean.toString(plugin.getConfig().getBoolean("use-vault"));
                     String info2 = Boolean.toString(plugin.getConfig().getBoolean("enable-gift"));
                     String info3 = Boolean.toString(plugin.getConfig().getBoolean("enable-trade"));
 
                     player.sendMessage(ChatColor.DARK_RED + "-----------------[" + ChatColor.RED + "sGift - Information" + ChatColor.DARK_RED + "]------------------");
                     player.sendMessage(ChatColor.RED + "Vault: " + ChatColor.AQUA + info1);
                     player.sendMessage(ChatColor.RED + "Gifts: " + ChatColor.AQUA + info2);
                     player.sendMessage(ChatColor.RED + "Trade: " + ChatColor.AQUA + info3);
 
                     StringBuilder senderList = new StringBuilder();
 
                     for (Sender s : senders) {
                         if (senderList.length() > 0) {
 
                             senderList.append(ChatColor.RED + ", " + ChatColor.AQUA);
 
                         }
                         senderList.append(s.Sender.getName());
                     }
                     player.sendMessage(ChatColor.RED + "Senders: " + ChatColor.AQUA + senderList);
 
 
                 } else if (args[0].equalsIgnoreCase("halt") && sender.hasPermission("sgift.halt")) {
 
                     player.sendMessage(prefix3 + ChatColor.RED + "Abruptly halted all Gifts and Trades!");
                     player.sendMessage(prefix3 + ChatColor.RED + "No items have been refunded to players!");
 
                     trades.clear();
                     gifts.clear();
 
                 } else if (args[0].equalsIgnoreCase("help")) {
 
                     player.sendMessage(ChatColor.DARK_RED + "---------------[" + ChatColor.RED + "sGift - sGift Help Menu" + ChatColor.DARK_RED + "]----------------");
                     player.sendMessage(plugin.getConfig().getString("Help.sGift.Info"));
                     player.sendMessage(plugin.getConfig().getString("Help.sGift.Halt"));
                     player.sendMessage(plugin.getConfig().getString("Help.sGift.Set"));
                     player.sendMessage(plugin.getConfig().getString("Help.sGift.Example"));
 
                 } else {
 
                     player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                     player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
                 }
             } else if (args.length == 2) {
 
                 player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                 player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
 
             } else if (args.length == 3 && args[0].equalsIgnoreCase("set") && sender.hasPermission("sgift.set")) {
                 if (args[1].equalsIgnoreCase("vault")) {
                     if (args[2].equalsIgnoreCase("true")) {
                         if (!plugin.getConfig().getBoolean("use-vault")) {
                             if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
 
                                 plugin.getConfig().set("use-vault", true);
                                 plugin.saveConfig();
 
                                 player.sendMessage(prefix3 + ChatColor.AQUA + "Vault has been set to true in Config");
 
                             } else {
 
                                 player.sendMessage(prefix3 + ChatColor.RED + "Vault could not be found, Vault remains disabled.");
                             }
                         } else {
 
                             player.sendMessage(prefix3 + ChatColor.RED + "Vault is already enabled!");
                         }
                     } else if (args[2].equalsIgnoreCase("false")) {
                         if (plugin.getConfig().getBoolean("use-vault")) {
 
                             plugin.getConfig().set("use-vault", false);
                             plugin.getConfig().set("enable-trade", false);
                             plugin.saveConfig();
 
                             player.sendMessage(prefix3 + ChatColor.AQUA + "Vault has been set to false in Config");
                             player.sendMessage(prefix3 + ChatColor.RED + "Subsequently, Trading has been disabled!");
 
                         } else {
 
                             player.sendMessage(prefix3 + ChatColor.RED + "Vault is already disabled!");
                         }
 
                     } else {
 
                         player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                         player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
                     }
                 } else if (args[1].equalsIgnoreCase("gifts")) {
                     if (args[2].equalsIgnoreCase("true")) {
                         if (!plugin.getConfig().getBoolean("enable-gift")) {
 
                             plugin.getConfig().set("enable-gift", true);
                             plugin.saveConfig();
 
                             player.sendMessage(prefix3 + ChatColor.AQUA + "Gifting has been set to true in Config");
 
                         } else {
 
                             player.sendMessage(prefix3 + ChatColor.RED + "Gifting is already enabled!");
                         }
                     } else if (args[2].equalsIgnoreCase("false")) {
                         if (plugin.getConfig().getBoolean("enable-gift")) {
 
                             plugin.getConfig().set("enable-gift", false);
                             plugin.saveConfig();
 
                             player.sendMessage(prefix3 + ChatColor.AQUA + "Gifting has been set to false in Config");
 
                         } else {
 
                             player.sendMessage(prefix3 + ChatColor.RED + "Gifting is already disabled!");
                         }
                     } else {
 
                         player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                         player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
                     }
 
                 } else if (args[1].equalsIgnoreCase("trade")) {
                     if (args[2].equalsIgnoreCase("true")) {
                         if (!plugin.getConfig().getBoolean("enable-trade")) {
 
                             plugin.getConfig().set("enable-trade", true);
                             plugin.saveConfig();
 
                             player.sendMessage(prefix3 + ChatColor.AQUA + "Trading has been set to true in Config");
 
                         } else {
 
                             player.sendMessage(prefix3 + ChatColor.RED + "Trading is already enabled!");
                         }
                     } else if (args[2].equalsIgnoreCase("false")) {
                         if (plugin.getConfig().getBoolean("enable-trade")) {
 
                             plugin.getConfig().set("enable-trade", false);
                             plugin.saveConfig();
 
                             player.sendMessage(prefix3 + ChatColor.AQUA + "Trading has been set to false in Config");
 
                         } else {
 
                             player.sendMessage(prefix3 + ChatColor.RED + "Trading is already disabled!");
                         }
                     } else {
 
                         player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                         player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
                     }
                 } else {
 
                     player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                     player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
                 }
             } else if (args.length == 0) {
 
                 player.sendMessage(prefix2 + ChatColor.RED + "By Sk8r2K9. /sgift info|halt|set <Option> [true|false]");
             } else if (args.length > 3) {
 
                 player.sendMessage(prefix3 + ChatColor.RED + "Invalid command usage!");
                 player.sendMessage(prefix3 + ChatColor.GRAY + "/sgift info|halt|set <Option> [true|false]");
             }
 
         } else if (!sender.hasPermission("sgift.trade") || !sender.hasPermission("sgift.trade.help") || !sender.hasPermission("sgift.gift") || !sender.hasPermission("sgift.gift.help") || !sender.hasPermission("sgift.admin") || !sender.hasPermission("sgift.sgift") || !sender.hasPermission("sgift.halt") || !sender.hasPermission("sgift.set")) {
 
             player.sendMessage(prefix + ChatColor.RED + "You don't have permission for that command!");
 
         }
         return false;
     }
 }
