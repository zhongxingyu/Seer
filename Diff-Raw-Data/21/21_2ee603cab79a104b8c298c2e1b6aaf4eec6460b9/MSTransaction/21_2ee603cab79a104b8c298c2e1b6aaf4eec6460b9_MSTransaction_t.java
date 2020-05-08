 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dev.xyzcraft.net.xyzshop.dataObject;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Joey
  */
 public class MSTransaction {
     public Integer quantity;
     public MSItem price;
     public MSTransactionType type;
     public Player player;
     public MSTransaction(Integer q, MSItem p, MSTransactionType t, Player p1) {
         quantity = q;
         price = p;
         type = t;
         player = p1;
     }
     public ItemStack items() {
         return new ItemStack((int)price.itemID,(int)quantity, price.itemDataValue);
     }
     private Float getBuyPrice() {
        float q1 = price.itemBuyPrice;
        float q2 = price.quantity;
        float price2 = q1/q2;
        return price2*quantity;
     }
     private Float getSellPrice() {
        float q1 = price.itemSellPrice;
        float q2 = price.quantity;
        float price2 = q1/q2;
        return price2*quantity;
     }
     public Float getPrice() {
         return (type == MSTransactionType.BUY) ? getBuyPrice() : getSellPrice();
     }
 }
