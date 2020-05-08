 package me.ellbristow.broker;
 
 import java.sql.ResultSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class BrokerListener implements Listener {
     
     private static Broker plugin;
     
     public BrokerListener (Broker instance) {
         plugin = instance;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClick(InventoryClickEvent event) {
         if (!event.isCancelled()) {
             Inventory inv = event.getView().getTopInventory();
             if ("<Broker> Buy".equals(inv.getName())) {
                 Player player = (Player)event.getWhoClicked();
                 int slot = event.getRawSlot();
                 if (slot >= 45 && slot <54) {
                     event.setCancelled(true);
                     // Clicked nevigation slot
                     plugin.priceCheck.remove(player.getName());
                     Material itemType = inv.getItem(slot).getType();
                     if (itemType == Material.BOOK) {
                         // Main Page
                         inv.setContents(plugin.getBrokerInv("0", null).getContents());
                         player.sendMessage(ChatColor.GOLD + "Main Page");
                     } else if (itemType == Material.PAPER) {
                         // Change Page
                         if (inv.getItem(0).getType() != Material.BOOK) {
                             // On Main Page
                             inv.setContents(plugin.getBrokerInv((slot-45)+"", null).getContents());
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         } else {
                             // On Sub Page
                             String itemName = inv.getItem(0).getType().name();
                             inv.setContents(plugin.getBrokerInv(itemName+"::"+(slot-45), null).getContents());
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
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", null).getContents());
                     player.sendMessage(ChatColor.GOLD + itemType.name());
                     player.sendMessage(ChatColor.GOLD + "Page 1");
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null) {
                     // Clicked item on sub-page
                     event.setCancelled(true);
                     if (!plugin.priceCheck.containsKey(player.getName())) {
                         double price = getPrice(inv,slot);
                         if (price != 0.00) {
                             player.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.WHITE + plugin.vault.economy.format(price) + " (each)");
                             HashMap<Integer,Double> slotPrice = new HashMap<Integer,Double>();
                             slotPrice.put(slot,price);
                             plugin.priceCheck.put(player.getName(), slotPrice);
                         } else {
                             player.closeInventory();
                             player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                             player.sendMessage(ChatColor.RED + "Please try again.");
                         }
                     } else {
                         HashMap<Integer,Double> clickedSlotPrice = plugin.priceCheck.get(player.getName());
                         Object[] slotKeys = clickedSlotPrice.keySet().toArray();
                         int clickedSlot = (Integer)slotKeys[0];
                         double price = getPrice(inv,slot);
                         if (clickedSlot != slot) {
                             if (price != 0.00) {
                                 player.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.WHITE + plugin.vault.economy.format(price) + " (each)");
                                 HashMap<Integer,Double> slotPrice = new HashMap<Integer,Double>();
                                 slotPrice.put(slot,price);
                                 plugin.priceCheck.put(player.getName(), slotPrice);
                             } else {
                                 plugin.priceCheck.remove(player.getName());
                                 player.closeInventory();
                                 player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                                 player.sendMessage(ChatColor.RED + "Please try again.");
                             }
                         } else {
                             HashMap<ItemStack,Double> pending = new HashMap<ItemStack,Double>();
                             pending.put(inv.getItem(slot),price);
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
                             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
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
                        inv.setContents(plugin.getBrokerInv("0", player.getName()).getContents());
                         player.sendMessage(ChatColor.GOLD + "Main Page");
                     } else if (itemType == Material.PAPER) {
                         // Change Page
                         if (inv.getItem(0).getType() != Material.BOOK) {
                             // On Main Page
                            inv.setContents(plugin.getBrokerInv((slot-45)+"", player.getName()).getContents());
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         } else {
                             // On Sub Page
                             String itemName = inv.getItem(0).getType().name();
                            inv.setContents(plugin.getBrokerInv(itemName+"::"+(slot-45), player.getName()).getContents());
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
                    inv.setContents(plugin.getBrokerInv(itemName+"::0", player.getName()).getContents());
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
                     HashMap<Integer, HashMap<String, Object>> sellOrders = plugin.brokerDb.select("*","BrokerOrders","playerName = '" + player.getName() + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND damage = " + stack.getDurability() + enchantmentString + " AND quant = " + stack.getAmount() + " AND price = " + getPrice(inv, slot),null,null);
                     int orderId = (Integer)sellOrders.get(0).get("id");
                     plugin.brokerDb.query("DELETE FROM BrokerOrders WHERE id = " + orderId);
                     String itemName = stack.getType().name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", player.getName()).getContents());
                     if (inv.getItem(0) == null) {
                         inv.setContents(plugin.getBrokerInv("0", player.getName()).getContents());
                     }
                     player.sendMessage(ChatColor.GOLD + "Sell Order Cancelled");
                     HashMap<Integer, ItemStack> dropped = player.getInventory().addItem(stack);
                     if (!dropped.isEmpty()) {
                         player.sendMessage(ChatColor.RED + "Not all items coudl fit in your Inventory!");
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
     public void onPlayerChat(PlayerChatEvent event) {
         if (!event.isCancelled()) {
             Player player = event.getPlayer();
             if (plugin.pending.containsKey(player.getName())) {
                 event.setCancelled(true);
                 try {
                     int quantity = Integer.parseInt(event.getMessage());
                     if (quantity == 0) {
                         plugin.pending.remove(player.getName());
                         player.sendMessage(ChatColor.RED + "Order Cancelled!");
                     } else {
                         HashMap<ItemStack, Double> pending = plugin.pending.get(player.getName());
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
                         if (stack.getDurability() != 0) {
                             enchanted += "(Damaged)";
                         }
                         double price = pending.get(stack);
                         HashMap<Integer, HashMap<String, Object>> sellOrders = plugin.brokerDb.select("SUM(quant) as totQuant","BrokerOrders", "orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString, null, "timeCode ASC");
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
                                             plugin.vault.economy.depositPlayer(sellerName, quant * price);
                                             String thisquery = "DELETE FROM BrokerOrders WHERE playername = '" + sellerName + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString;
                                             plugin.brokerDb.query(thisquery);
                                             if (seller.isOnline()) {
                                                 seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " bought " + ChatColor.WHITE + quant + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(quant * price));
                                             }
                                         } else {
                                             int deduct = quantity - allocated;
                                             int selling = deduct;
                                             plugin.vault.economy.depositPlayer(sellerName, deduct * price);
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
                 } catch (NumberFormatException nfe) {
                     plugin.pending.remove(player.getName());
                     player.sendMessage(ChatColor.RED + "Invalid quantity. Order Cancelled!");
                 }
                 plugin.pending.remove(player.getName());
             }
         }
     }
     
     private double getPrice(Inventory inv, int slot) {
         double price = 0.00;
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
         ResultSet sellOrders = plugin.brokerDb.query("SELECT price FROM BrokerOrders WHERE orderType = 0 AND itemName = '" + stack.getType().name() + "' AND damage = " + stack.getDurability() + enchantmentString + " GROUP BY price, damage, enchantments ORDER BY price ASC, damage ASC");
         if (sellOrders != null) {
             if (!plugin.isDamageableItem(stack)) {
                 int counter = 0;
                 while (counter <= Math.floor(slot/9)) {
                     try {
                         sellOrders.next();
                         price = sellOrders.getDouble("price");
                     } catch (Exception e) {
                     }
                     counter++;
                 }
             } else {
                 try {
                     price = sellOrders.getDouble("price");
                 } catch (Exception e) {
                 }
             }
         }
         return price;
     }
     
 }
