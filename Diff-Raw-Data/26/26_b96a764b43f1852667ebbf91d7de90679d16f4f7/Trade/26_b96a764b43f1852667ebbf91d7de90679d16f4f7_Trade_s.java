 package ru.vgTrade.Trade;
 
 import java.util.Arrays;
 import ru.vgTrade.Config.LanguageManager;
 import ru.vgTrade.Util.Log;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import ru.vgTrade.Util.InventoryUtils;
 
 /**
  * @author Oliver Brown (Arkel)
  *         Date: 30/10/11
  */
 public class Trade {
 
     private final TradePlayer initiator;
     private final TradePlayer target;
 
     private final Inventory inventory;
     public String chestID = Integer.toString(this.hashCode());
 
     private TradeManager manager;
 
     private int cancellerID;
 
     public Trade(TradeRequest request, TradeManager manager) {
         this.initiator = request.initiator;
         this.target = request.target;
         this.manager = manager;
         chestID = initiator.getPlayer().getName() + " - " + target.getPlayer().getName();
 
         inventory = InventoryUtils.createInventory(chestID);
                 //VirtualLargeChest(chestID);
 
         target.getPlayer().openInventory(inventory);
         initiator.getPlayer().openInventory(inventory);
 
         Log.trade(initiator.getName() + " began trading with " + target.getName());
     }
 
     private void scheduleCancellation() {
 
         cancellerID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(manager.vgTrade, new Runnable() {
 
             public void run() {
                 abort();
 
                 initiator.sendMessage(ChatColor.RED + LanguageManager.getString(LanguageManager.Strings.TIMED));
                 target.sendMessage(ChatColor.RED + LanguageManager.getString(LanguageManager.Strings.TIMED));
                 Log.trade("The trade between " + initiator.getName() + " and " + target.getName() + " timed out");
             }
         }, 600L);
 
     }
 
     private void unscheduleCancellation() {
         Bukkit.getServer().getScheduler().cancelTask(cancellerID);
     }
 
     public void abort() {
 
         if (!Bukkit.getServer().getScheduler().isCurrentlyRunning(cancellerID)) {
             unscheduleCancellation();
         }
 
         target.getPlayer().closeInventory();
         initiator.getPlayer().closeInventory();
 
         initiator.restore(InventoryUtils.getLeftContents(inventory));
         target.restore(InventoryUtils.getRightContents(inventory));
 
         manager.finish(this);
 
         Log.trade("The trade between " + initiator.getName() + " and " + target.getName() + " was aborted");
 
         sendMessage(LanguageManager.getString(LanguageManager.Strings.CANCELLED));
     }
 
     public void confirm(Player player) {
 
         if (player.equals(initiator.getPlayer())) {
             initiator.setState(TradeState.CONFIRMED);
             initiator.sendMessage(LanguageManager.getString(LanguageManager.Strings.CONFIRMED));
             inventory.setItem(InventoryUtils.P1_CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)5));
         } else {
             target.setState(TradeState.CONFIRMED);
             target.sendMessage(LanguageManager.getString(LanguageManager.Strings.CONFIRMED));
             inventory.setItem(InventoryUtils.P2_CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)5));
         }   
 
         if (target.getState().equals(TradeState.CONFIRMED) && initiator.getState().equals(TradeState.CONFIRMED)) {
             unscheduleCancellation();
             doTrade();
         }
     }
     
     public void cancel(Player player) {
         
         if (player.equals(initiator.getPlayer()) && initiator.getState() == TradeState.CONFIRMED) {
             initiator.sendMessage(LanguageManager.getString(LanguageManager.Strings.DECLINED));
             inventory.setItem(InventoryUtils.P1_CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)15));
             
             initiator.setState(TradeState.CHEST_OPEN);
         } else if (target.getState() == TradeState.CONFIRMED) {
             target.sendMessage(LanguageManager.getString(LanguageManager.Strings.DECLINED));
             inventory.setItem(InventoryUtils.P2_CONFIRM_SLOT, new ItemStack(Material.WOOL, 1, (short)1, (byte)15));
             
             initiator.setState(TradeState.CHEST_OPEN);
         }   
         
     }
 
     public Event.Result slotCheck(Player player, int slot, Inventory inventoryToCheck) {
 
         if (inventoryToCheck.getName().equals(chestID)) {
             if (Arrays.asList(InventoryUtils.BUTTON_SLOTS).contains(slot)) {
                 // Обработка кнопок
                 if (slot == InventoryUtils.CONFIRM_SLOT) {
                     if (target.getState() == TradeState.CHEST_OPEN || initiator.getState() == TradeState.CHEST_OPEN) {
 
                         if (getUsedCases(InventoryUtils.getLeftContents(inventoryToCheck)
                             ) > getEmptyCases(target.getInventory().getContents()) || 
                                 getUsedCases(InventoryUtils.getRightContents(inventoryToCheck)) > getEmptyCases(initiator.getInventory().getContents())) {
                             abort();
                             sendMessage(ChatColor.RED + LanguageManager.getString(LanguageManager.Strings.NOROOM));
                             return Event.Result.DENY;
                         }
                         //scheduleCancellation();
 
                         //initiator.requestConfirm(InventoryUtils.getLeftContents(inventoryToCheck), InventoryUtils.getRightContents(inventoryToCheck));
                         //target.requestConfirm(InventoryUtils.getRightContents(inventoryToCheck), InventoryUtils.getLeftContents(inventoryToCheck));
 
                         confirm(player);
                     }
                 }
                 
                 if (slot == InventoryUtils.DECLINE_SLOT) {
                     if (target.getState() == TradeState.CONFIRMED || initiator.getState() == TradeState.CONFIRMED) {
 
                         if (getUsedCases(InventoryUtils.getLeftContents(inventoryToCheck)
                             ) > getEmptyCases(target.getInventory().getContents()) || 
                                 getUsedCases(InventoryUtils.getRightContents(inventoryToCheck)) > getEmptyCases(initiator.getInventory().getContents())) {
                             abort();
                             sendMessage(ChatColor.RED + LanguageManager.getString(LanguageManager.Strings.NOROOM));
                             return Event.Result.DENY;
                         }
                         //scheduleCancellation();
 
                         //initiator.requestConfirm(InventoryUtils.getLeftContents(inventoryToCheck), InventoryUtils.getRightContents(inventoryToCheck));
                         //target.requestConfirm(InventoryUtils.getRightContents(inventoryToCheck), InventoryUtils.getLeftContents(inventoryToCheck));
 
                         cancel(player);
                     }
                 }
                 
                 return Event.Result.DENY;
             }
             
             if (player.equals(initiator.getPlayer()) && (Arrays.asList(InventoryUtils.LEFT_SLOTS).contains(slot) || slot > 35)) {
                 return Event.Result.DEFAULT;
             } else if (player.equals(target.getPlayer()) && (Arrays.asList(InventoryUtils.RIGHT_SLOTS).contains(slot) || slot > 35)) {
                 return Event.Result.DEFAULT;
             } else {
                 player.sendMessage(ChatColor.RED + LanguageManager.getString(LanguageManager.Strings.NOTYOURS));
                 return Event.Result.DENY;
             }
         }
 
         return Event.Result.DEFAULT;
     }
 
     public boolean canUseInventory() {
         return target.getState() == TradeState.CHEST_OPEN && initiator.getState() == TradeState.CHEST_OPEN;
     }
 
     private void doTrade() {
         initiator.tradeFinish(InventoryUtils.getRightContents(inventory), InventoryUtils.getLeftContents(inventory));
         target.tradeFinish(InventoryUtils.getLeftContents(inventory), InventoryUtils.getRightContents(inventory));
         
         initiator.getPlayer().closeInventory();
         target.getPlayer().closeInventory();
         
         initiator.doTrade(InventoryUtils.getRightContents(inventory));
         target.doTrade(InventoryUtils.getLeftContents(inventory));

         manager.finish(this);

        //sendMessage(LanguageManager.getString(LanguageManager.Strings.FINISHED));
         Log.trade("The trade between " + initiator.getName() + " and " + target.getName() + " was completed");
         if (Log.verbose) {
             String tradeList = "";
             int i=0;
             for (ItemStack is : InventoryUtils.getRightContents(inventory)) {
                 if (i == 0) tradeList += ",";
                 tradeList += is.getType().name() + "x"+is.getAmount();
                 i++;
             }
             tradeList = "";
             i = 0;
             Log.trade(initiator.getName()+" got:"+tradeList);
             for (ItemStack is : InventoryUtils.getLeftContents(inventory)) {
                 if (i == 0) tradeList += ",";
                 tradeList += is.getType().name() + "x"+is.getAmount();
             }
             Log.trade(target.getName()+" got:"+tradeList);
         }
     }
 
     private int getEmptyCases(ItemStack[] contents) {
         int count = 0;
         if (contents == null) {
             return 0;
         }
         for (ItemStack content : contents) {
             if (content == null || content.getType() == Material.AIR) {
                 count++;
             }
         }
         return count;
     }
 
     private int getUsedCases(ItemStack[] contents) {
         int count = 0;
         if (contents == null) {
             Log.info("Close contents is empty!");
             return 0;
         }
         for (ItemStack content : contents) {
             if (content != null && content.getType() != Material.AIR) {
                 count++;
             }
         }
         return count;
     }
 
     void sendMessage(String msg) {
         target.sendMessage(msg);
         initiator.sendMessage(msg);
     }
 
 }
