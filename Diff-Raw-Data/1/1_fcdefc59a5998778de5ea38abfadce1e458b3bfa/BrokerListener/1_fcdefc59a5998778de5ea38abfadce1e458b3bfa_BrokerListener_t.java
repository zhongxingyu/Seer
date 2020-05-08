 package me.ellbristow.broker;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Sign;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class BrokerListener implements Listener {
     
     private static Broker plugin;
     
     public BrokerListener (Broker instance) {
         plugin = instance;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClick(InventoryClickEvent event) {
         if (event.isCancelled()) return;
         Inventory inv = event.getView().getTopInventory();
         if (inv.getName().startsWith("<Broker>")) {
             String seller = inv.getName().split(" ")[1];
             if (!seller.equals("Cancel")) {
                 if (seller.equals("Buy")) seller = "";
                 Player player = (Player)event.getWhoClicked();
                 int slot = event.getRawSlot();
                 if (slot >= 45 && slot <54) {
                     event.setCancelled(true);
                     // Clicked navigation slot
                     plugin.priceCheck.remove(player.getName());
                     Material itemType = inv.getItem(slot).getType();
                     if (itemType == Material.BOOK) {
                         // Main Page
                         inv.setContents(plugin.getBrokerInv("0", player, seller).getContents());
                         player.sendMessage(ChatColor.GOLD + "Main Page");
                     } else if (itemType == Material.PAPER) {
                         // Change Page
                         if (inv.getItem(0).getType() != Material.BOOK) {
                             // On Main Page
                             inv.setContents(plugin.getBrokerInv((slot-45)+"", player, seller).getContents());
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         } else {
                             // On Sub Page
                             String itemName = inv.getItem(0).getType().name();
                             inv.setContents(plugin.getBrokerInv(itemName+"::"+(slot-45), player, seller).getContents());
                             player.sendMessage(ChatColor.GOLD + itemName);
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         }
                     }
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null && inv.getItem(45).getType() != Material.BOOK) {
                     // Clicked item on Main Page
                     event.setCancelled(true);
                     plugin.priceCheck.remove(player.getName());
                     Material itemType = inv.getItem(slot).getType();
                     String itemName = itemType.name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", player, seller).getContents());
                     player.sendMessage(ChatColor.GOLD + itemType.name());
                     player.sendMessage(ChatColor.GOLD + "Page 1");
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null) {
                     // Clicked item on sub-page
                     event.setCancelled(true);
                     if (!plugin.priceCheck.containsKey(player.getName())) {
                         String priceString = getPrice(inv,slot, null);
                         String[] priceSplit = priceString.split(":");
                         double price = Double.parseDouble(priceSplit[0]);
                         int perItems = Integer.parseInt(priceSplit[1]);
                         String each = "each";
                         if (perItems != 1) {
                             each = "for " + perItems;
                         }
                         if (price != 0.00) {
                             player.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.WHITE + plugin.vault.economy.format(price) + " ("+each+")");
                             HashMap<Integer,String> slotPrice = new HashMap<Integer,String>();
                             slotPrice.put(slot,price+":"+perItems);
                             plugin.priceCheck.put(player.getName(), slotPrice);
                         } else {
                             final String playerName = player.getName();
                             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                             @Override
                             public void run() {
                                     Player runPlayer = plugin.getServer().getPlayer(playerName);
                                     runPlayer.closeInventory();
                                 }
                             });
                             player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                             player.sendMessage(ChatColor.RED + "Please try again.");
                         }
                     } else {
                         HashMap<Integer,String> clickedSlotPrice = plugin.priceCheck.get(player.getName());
                         Object[] slotKeys = clickedSlotPrice.keySet().toArray();
                         int clickedSlot = (Integer)slotKeys[0];
                         String priceString = getPrice(inv,slot, null);
                         String[] priceSplit = priceString.split(":");
                         double price = Double.parseDouble(priceSplit[0]);
                         int perItems = Integer.parseInt(priceSplit[1]);
                         String each = "each";
                         if (perItems != 1) {
                             each = "for " + perItems;
                         }
                         if (clickedSlot != slot) {
                             if (price != 0.00) {
                                 player.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.WHITE + plugin.vault.economy.format(price) + " ("+each+")");
                                 HashMap<Integer,String> slotPrice = new HashMap<Integer,String>();
                                 slotPrice.put(slot,price+":"+perItems);
                                 plugin.priceCheck.put(player.getName(), slotPrice);
                             } else {
                                 plugin.priceCheck.remove(player.getName());
                                 final String playerName = player.getName();
                                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                 @Override
                                 public void run() {
                                         Player runPlayer = plugin.getServer().getPlayer(playerName);
                                         runPlayer.closeInventory();
                                     }
                                 });
                                 player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                                 player.sendMessage(ChatColor.RED + "Please try again.");
                             }
                         } else {
                             HashMap<ItemStack,String> pending = new HashMap<ItemStack,String>();
                             pending.put(inv.getItem(slot),price+":"+perItems);
                             plugin.pending.put(player.getName(), pending);
                             plugin.priceCheck.remove(player.getName());
                             player.sendMessage("Enter quantity to buy at this price");
                             player.sendMessage("(Enter 0 to cancel)");
                             final String playerName = player.getName();
                             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                 @Override
                                 public void run() {
                                     Player runPlayer = plugin.getServer().getPlayer(playerName);
                                     runPlayer.closeInventory();
                                 }
                             });
                             plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                                 @Override
                                 public void run () {
                                     if (plugin.pending.containsKey(playerName)) {
                                         Player runPlayer = plugin.getServer().getPlayer(playerName);
                                         if (runPlayer != null) {
                                             runPlayer.sendMessage(ChatColor.RED + "You took too long to specify a quantity. Order Cancelled!");
                                         }
                                         plugin.pending.remove(playerName);
                                     }
                                 }
                             }, 200);
                         }
                     }
                 } else if (event.isShiftClick() && event.isLeftClick()) {
                     event.setCancelled(true);
                     plugin.priceCheck.remove(player.getName());
                 } else if (slot >= 0 && slot < 54 && event.getCursor() != null) {
                     event.setCancelled(true);
                     plugin.priceCheck.remove(player.getName());
                 } else {
                     plugin.priceCheck.remove(player.getName());
                 }
             } else if ("<Broker> Cancel".equals(inv.getName())) {
                 Player player = (Player)event.getWhoClicked();
                 int slot = event.getRawSlot();
                 if (slot >= 45 && slot <54) {
                     event.setCancelled(true);
                     // Clicked nevigation slot
                     plugin.priceCheck.remove(player.getName());
                     Material itemType = inv.getItem(slot).getType();
                     if (itemType == Material.BOOK) {
                         // Main Page
                         inv.setContents(plugin.getBrokerInv("0", player, player.getName()).getContents());
                         player.sendMessage(ChatColor.GOLD + "Main Page");
                     } else if (itemType == Material.PAPER) {
                         // Change Page
                         if (inv.getItem(0).getType() != Material.BOOK) {
                             // On Main Page
                             inv.setContents(plugin.getBrokerInv((slot-45)+"", player, player.getName()).getContents());
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         } else {
                             // On Sub Page
                             String itemName = inv.getItem(0).getType().name();
                             inv.setContents(plugin.getBrokerInv(itemName+"::"+(slot-45), player, player.getName()).getContents());
                             player.sendMessage(ChatColor.GOLD + itemName);
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         }
                     }
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null && inv.getItem(45).getType() != Material.BOOK) {
                     // Clicked item on Main Page
                     event.setCancelled(true);
                     plugin.priceCheck.remove(player.getName());
                     Material itemType = inv.getItem(slot).getType();
                     String itemName = itemType.name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", player, player.getName()).getContents());
                     player.sendMessage(ChatColor.GOLD + itemType.name());
                     player.sendMessage(ChatColor.GOLD + "Page 1");
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null) {
                     // Clicked item on sub-page
                     // Cancel Order
                     event.setCancelled(true);
                     ItemStack stack = inv.getItem(slot);
                     Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                     String enchantmentString = "";
                     if (!enchantments.isEmpty()) {
                         enchantmentString = " AND enchantments = '";
                         Object[] enchs = enchantments.keySet().toArray();
                         for (Object ench : enchs) {
                             if (!" AND enchantments = '".equals(enchantmentString)) {
                                 enchantmentString += ";";
                             }
                             enchantmentString += ((Enchantment)ench).getId() + "@" + enchantments.get((Enchantment)ench);
                         }
                         enchantmentString += "'";
                     }
                     String priceString = getPrice(inv, slot, player.getName());
                     String[] priceSplit = priceString.split(":");
                     double price = Double.parseDouble(priceSplit[0]);
                     int perItems = Integer.parseInt(priceSplit[1]);
                     HashMap<Integer, HashMap<String, Object>> sellOrders = plugin.brokerDb.select("*","BrokerOrders","playerName = '" + player.getName() + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND damage = " + stack.getDurability() + enchantmentString + " AND price = " + price + " AND perItems = " + perItems,null,null);
                     int totQuant = 0;
                     for (int i = 0; i < sellOrders.size(); i++) {
                         int orderId = (Integer)sellOrders.get(i).get("id");
                         totQuant += (Integer)sellOrders.get(i).get("quant");
                         plugin.brokerDb.query("DELETE FROM BrokerOrders WHERE id = " + orderId);
                     }
                     stack.setAmount(totQuant);
                     String itemName = stack.getType().name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", player, player.getName()).getContents());
                     if (inv.getItem(0) == null) {
                         inv.setContents(plugin.getBrokerInv("0", player, player.getName()).getContents());
                     }
                     player.sendMessage(ChatColor.GOLD + "Sell Order Cancelled");
                     HashMap<Integer, ItemStack> dropped = player.getInventory().addItem(stack);
                     if (!dropped.isEmpty()) {
                         player.sendMessage(ChatColor.RED + "Not all items could fit in your Inventory!");
                         player.sendMessage(ChatColor.RED + "Look on the floor!");
                         for (int i = 0; i < dropped.size(); i++) {
                             player.getWorld().dropItem(player.getLocation(), dropped.get(i));
                         }
                     }
                 } else if (event.isShiftClick() && event.isLeftClick()) {
                     event.setCancelled(true);
                     plugin.priceCheck.remove(player.getName());
                 } else if (slot >= 0 && slot < 54 && event.getCursor() != null) {
                     event.setCancelled(true);
                     plugin.priceCheck.remove(player.getName());
                 } else {
                     plugin.priceCheck.remove(player.getName());
                 }
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClose(InventoryCloseEvent event) {
         if (event.getInventory().getName().startsWith("<Broker>")) {
             plugin.priceCheck.remove(event.getPlayer().getName());
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         if (!event.isCancelled()) {
             Player player = event.getPlayer();
             if (plugin.pending.containsKey(player.getName())) {
                 event.setCancelled(true);
                 int quantity = 0;
                 try {
                     quantity = Integer.parseInt(event.getMessage());
                 } catch (NumberFormatException nfe) {
                     plugin.pending.remove(player.getName());
                     player.sendMessage(ChatColor.RED + "Invalid quantity. Order Cancelled!");
                 }
                 if (quantity <= 0) {
                     plugin.pending.remove(player.getName());
                     player.sendMessage(ChatColor.RED + "Order Cancelled!");
                 } else {
                     HashMap<ItemStack, String> pending = plugin.pending.get(player.getName());
                     Object[] items = pending.keySet().toArray();
                     ItemStack stack = (ItemStack)items[0];
                     Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                     String enchantmentString = "";
                     String enchanted = "";
                     if (!enchantments.isEmpty()) {
                         enchantmentString = " AND enchantments = '";
                         enchanted = " (Enchanted)";
                         Object[] enchs = enchantments.keySet().toArray();
                         for (Object ench : enchs) {
                             if (!" AND enchantments = '".equals(enchantmentString)) {
                                 enchantmentString += ";";
                             }
                             enchantmentString += ((Enchantment)ench).getId() + "@" + enchantments.get((Enchantment)ench);
                         }
                         enchantmentString += "'";
                     }
                     if (plugin.isDamageableItem(stack) && stack.getDurability() != 0) {
                         enchanted += "(Damaged)";
                     }
                     String priceString = pending.get(stack);
                     String[] priceSplit = priceString.split(":");
                     double price = Double.parseDouble(priceSplit[0]);
                     int perItems = Integer.parseInt(priceSplit[1]);
                     quantity = (int)(quantity / perItems) * perItems;
                     if (quantity == 0) {
                         plugin.pending.remove(player.getName());
                         player.sendMessage(ChatColor.RED + "You must buy at least "+perItems+" items for this order!");
                         player.sendMessage(ChatColor.RED + "Order Cancelled!");
                         return;
                     }
                     HashMap<Integer, HashMap<String, Object>> sellOrders = plugin.brokerDb.select("SUM(quant) as totQuant","BrokerOrders", "orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND perItems = "+perItems+" AND damage = " + stack.getDurability() + enchantmentString, null, "timeCode ASC");
                     if (sellOrders != null) {
                         try {
                             int tot = 0;
                             for (int i = 0; i < sellOrders.size(); i++) {
                                 if (tot == 0) {
                                     tot = (Integer)sellOrders.get(i).get("totQuant");
                                 }
                             }
                             if (quantity > tot) {
                                 player.sendMessage(ChatColor.RED + "Only " + ChatColor.WHITE + tot + ChatColor.RED + " were available at this price!");
                                 quantity = tot;
                             }
                             double totPrice = quantity * price;
                             if (plugin.vault.economy.getBalance(player.getName()) < totPrice) {
                                 player.sendMessage(ChatColor.RED + "You cannot afford " + quantity + " of those!");
                                 player.sendMessage(ChatColor.RED + "Total Price: " + plugin.vault.economy.format(totPrice));
                                 player.sendMessage(ChatColor.RED + "Order Cancelled!");
                                 plugin.pending.remove(player.getName());
                             } else {
                                 stack.setAmount(quantity);
                                 plugin.vault.economy.withdrawPlayer(player.getName(), totPrice);
                                 HashMap<Integer, ItemStack> drop = player.getInventory().addItem(stack);
                                 if (!drop.isEmpty()) {
                                     player.sendMessage(ChatColor.YELLOW + "Some items did not fit in your inventory! Look on the floor!");
                                     for (int i = 0; i < drop.size(); i++) {
                                         ItemStack dropStack = drop.get(i);
                                         player.getWorld().dropItem(player.getLocation(), dropStack);
                                     }
                                 }
                                 player.sendMessage(ChatColor.GOLD + "You bought " + ChatColor.WHITE + quantity + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(totPrice));
                                 HashMap<Integer, HashMap<String, Object>> playerOrders = plugin.brokerDb.select("playerName, SUM(quant) AS quant, timeCode", "BrokerOrders", "orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString,"playerName", "timeCode ASC");
                                 int allocated = 0;
                                 HashMap<String,Integer> allSellers = new HashMap<String, Integer>();
                                 for (int i =0; i < playerOrders.size(); i++) {
                                     String playerName = (String)playerOrders.get(i).get("playerName");
                                     int playerQuant = (Integer)playerOrders.get(i).get("quant");
                                     allSellers.put(playerName,playerQuant);
                                 }
                                 Object[] sellers = allSellers.keySet().toArray();
                                 for (int i = 0; i < sellers.length; i++) {
                                     String sellerName = (String)sellers[i];
                                     int quant = allSellers.get(sellerName);
                                     OfflinePlayer seller = plugin.getServer().getOfflinePlayer(sellerName);
                                     if (quantity - allocated >= quant) {
                                         allocated += quant;
                                         double tax = 0;
                                         if (plugin.taxRate != 0 && quant * price >= plugin.taxMinimum) {
                                             if (plugin.taxIsPercentage) {
                                                 tax = (quant * price) / 100 * plugin.taxRate;
                                             } else {
                                                 tax = plugin.taxRate;
                                             }
                                         }
                                         plugin.vault.economy.depositPlayer(sellerName, (quant * price) - tax);
                                         String thisquery = "DELETE FROM BrokerOrders WHERE playername = '" + sellerName + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString;
                                         plugin.brokerDb.query(thisquery);
                                         if (seller.isOnline()) {
                                             seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " bought " + ChatColor.WHITE + quant + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(quant * price));
                                             if (tax >= 0.01) {
                                                 seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] You were charged sales tax of " + ChatColor.WHITE + plugin.vault.economy.format(tax));
                                             }
                                         }
                                     } else {
                                         int deduct = quantity - allocated;
                                         int selling = deduct;
                                         double tax = 0;
                                         if (plugin.taxRate != 0 && deduct * price >= plugin.taxMinimum) {
                                             if (plugin.taxIsPercentage) {
                                                 tax = (deduct * price) / 100 * plugin.taxRate;
                                             } else {
                                                 tax = plugin.taxRate;
                                             }
                                         }
                                         plugin.vault.economy.depositPlayer(sellerName, (deduct * price) - tax);
                                         HashMap<Integer, HashMap<String, Object>> sellerOrders = plugin.brokerDb.select("id, quant","BrokerOrders", "playername = '" + sellerName + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString, null, "timeCode ASC");
                                         Set<String> queries = new HashSet<String>();
                                         for (int j = 0; j < sellerOrders.size(); j++) {
                                             if (deduct != 0) {
                                                 int sellQuant = (Integer)sellerOrders.get(j).get("quant");
                                                 if (sellQuant <= deduct) {
                                                     queries.add("DELETE FROM BrokerOrders WHERE id = " + (Integer)sellerOrders.get(j).get("id"));
                                                     deduct -= sellQuant;
                                                 } else {
                                                     queries.add("UPDATE BrokerOrders SET quant = quant - " + deduct + " WHERE id = " + (Integer)sellerOrders.get(j).get("id"));
                                                     deduct = 0;
                                                 }
                                             }
                                         }
                                         Object[]queryStrings = queries.toArray();
                                         for (Object thisquery : queryStrings) {
                                             plugin.brokerDb.query((String)thisquery);
                                         }
                                         if (seller.isOnline()) {
                                             seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " bought " + ChatColor.WHITE + selling + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(selling * price));
                                             if (tax >= 0.01) {
                                                 seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] You were charged sales tax of " + ChatColor.WHITE + plugin.vault.economy.format(tax));
                                             }
                                         }
                                         allocated = quantity;
                                     }
                                 }
                             }
                         } catch(Exception e) {
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                         player.sendMessage(ChatColor.RED + "Please try again.");
                     }
                     plugin.pending.remove(player.getName());
                 }
                 plugin.pending.remove(player.getName());
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onSignChange(SignChangeEvent event) {
         if (event.isCancelled())
             return;
         String line0 = event.getLine(0);
         String line3 = event.getLine(3);
         if (line0.equalsIgnoreCase("[Broker]")) {
             Player player = event.getPlayer();
             if (!player.hasPermission("broker.sign") && !player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign.personal.others")) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to create broker signs!");
                 event.getBlock().breakNaturally();
                 return;
             } else if (player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign") && !player.hasPermission("broker.sign.personal.others")) {
                 event.setLine(3, player.getName());
             } else if ((player.hasPermission("broker.sign.personal") || player.hasPermission("broker.sign.personal.others")) && player.hasPermission("broker.sign") && !line3.equals("")) {
                 if (!player.hasPermission("broker.sign.personal.others")) {
                     event.setLine(3, player.getName());
                 } else {
                     OfflinePlayer target = plugin.getServer().getOfflinePlayer(line3);
                     if (!target.hasPlayedBefore()) {
                         player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + line3 + ChatColor.RED + " not found!");
                         event.getBlock().breakNaturally();
                         return;
                     }
                     event.setLine(3, target.getName());
                 }
             }
             event.setLine(0, "[Broker]");
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled())
             return;
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN)) && ((Sign)event.getClickedBlock().getState()).getLine(0).equalsIgnoreCase("[Broker]")) {
             Player player = event.getPlayer();
             if (!player.hasPermission("Broker.use")) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to use the broker!");
                 event.setCancelled(true);
                 return;
             }
             Sign sign = (Sign)event.getClickedBlock().getState();
             String sellerName = sign.getLine(3);
             if (sellerName.equals("")) {
                 player.openInventory(plugin.getBrokerInv("0", player, null));
             } else {
                 OfflinePlayer seller = plugin.getServer().getOfflinePlayer(sellerName);
                 if (!seller.hasPlayedBefore()) {
                     player.sendMessage(ChatColor.RED + "Sorry! This shop appears to be closed!");
                     event.setCancelled(true);
                     return;
                 }
                 player.openInventory(plugin.getBrokerInv("0", player, seller.getName()));
             }
             plugin.pending.remove(player.getName());
             player.sendMessage(ChatColor.GOLD + "<BROKER> Main Page");
             player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
             event.setCancelled(true);
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled())
             return;
         if ((event.getBlock().getType().equals(Material.SIGN_POST) || event.getBlock().getType().equals(Material.WALL_SIGN)) && ((Sign)event.getBlock().getState()).getLine(0).equalsIgnoreCase("[Broker]")) {
             String owner = ((Sign)event.getBlock().getState()).getLine(3);
             Player player = event.getPlayer();
             if (!player.hasPermission("broker.sign") && !player.hasPermission("broker.sign.personal")  && !player.hasPermission("broker.sign.personal.others")) {
                 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break Broker signs!");
                 event.setCancelled(true);
                 Sign sign = (Sign)event.getBlock().getState();
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             } else if (player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign.personal.others") && !owner.equalsIgnoreCase(player.getName())) {
                 event.getPlayer().sendMessage(ChatColor.RED + "This is not your sign to break!");
                 event.setCancelled(true);
                 Sign sign = (Sign)event.getBlock().getState();
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             }
         }
     }
     
     private String getPrice(Inventory inv, int slot, String sellerName) {
         double price = 0.00;
         int perItems = 1;
         ItemStack stack = inv.getItem(slot);
         String sellerString = "";
         if (sellerName != null && !sellerName.equals("")) {
             sellerString = " AND playerName = '" + sellerName + "'";
         }
         HashMap<Integer, HashMap<String, Object>> sellOrders = plugin.brokerDb.select("price, perItems", "BrokerOrders", "orderType = 0" + sellerString + " AND itemName = '" + stack.getType().name() + "'", "price, perItems, damage, enchantments", "price/perItems ASC, damage ASC");
         if (!sellOrders.isEmpty()) {
             int counter = 0;
             for (HashMap<String, Object> order : sellOrders.values()) {
                 if (counter == slot) {
                     price = Double.parseDouble(order.get("price")+"");
                     perItems = Integer.parseInt(order.get("perItems")+"");
                 }
                 counter++;
             }
         }
         return price+":"+perItems;
     }
     
 }
