 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dev.xyzcraft.net.xyzshop;
 
import dev.xyzcraft.net.xyzshop.dataObject.MSItem;
 import dev.xyzcraft.net.xyzshop.dataObject.MSTransaction;
import dev.xyzcraft.net.xyzshop.dataObject.MSTransactionType;
 import dev.xyzcraft.net.xyzshop.util.MacCommandStatus;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  *
  * @author Joey
  */
 public class Shop {
     private XYZShop plugin;
     public Shop(XYZShop pl) {
         plugin = pl;
     }
     public boolean canOccur(MSTransaction ts) {
         switch (ts.type) {
             case SELL:
                 Player pl = ts.player;
                 return pl.getInventory().containsAtLeast(ts.items(),ts.items().getAmount());
             case BUY:
                 double price = ts.getPrice();
                 double balance = plugin.econ.getBalance(ts.player.getName());
                 return balance > price;
         }
         return false;
     }
     public boolean commit(MSTransaction ts) {
          if (canOccur(ts)) {
              switch (ts.type) {
                  case SELL:
                      PlayerInventory inv = ts.player.getInventory();
                      inv.removeItem(ts.items());
                      double price = (double)ts.getPrice();
                      plugin.econ.depositPlayer(ts.player.getName(), price);
                      break;
                  case BUY:
                      double price2 = (double)ts.getPrice();
                      plugin.econ.withdrawPlayer(ts.player.getName(), price2);
                      PlayerInventory inv2 = ts.player.getInventory();
                      HashMap rv = inv2.addItem(ts.items());
                      try {
                         if (!rv.isEmpty()) {
                             for (Object iso : rv.values()) {
                                 ItemStack is = null;
                                 if (iso instanceof ItemStack) {
                                     is = (ItemStack)iso;
                                 }
                                 if (is == null) {
                                     return false;
                                 }
                                 ts.player.getWorld().dropItem(ts.player.getLocation(),is);
                             }
                         }
                      } catch (NullPointerException ex) {
                          //Do nothing, ignore it.
                      }
                      
              }
              return true;
          }
          else {
              return false;
          }
     }
     public MacCommandStatus handleBuyCommand(String[] args, Player cs) {
         return MacCommandStatus.SUCESSFUL;
     }
     public MacCommandStatus handleSellCommand(String[] args, Player cs) {
         return MacCommandStatus.SUCESSFUL;
     }
     public MacCommandStatus handleShopCommand(String[] args, Player cs) {
         return MacCommandStatus.SUCESSFUL;
     }
 }
