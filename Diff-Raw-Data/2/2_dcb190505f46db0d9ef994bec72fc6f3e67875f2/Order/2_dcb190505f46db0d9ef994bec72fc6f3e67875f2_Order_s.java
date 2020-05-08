 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package order;
 
 import java.util.Collection;
 import java.util.TreeMap;
 
 /**
  *
  * @author Bill
  */
 public class Order {
     private TreeMap<String, OrderLine> items;
     
     public Order()
     {
         items = new TreeMap<String, OrderLine>();
         
     }
     
         public void addItem(Item.Item item, Integer quantity)
     {
         if (items.containsKey(item.getItemID()) && quantity > 0)
         {
            Integer oldQuantity = items.get(item.code).getQuantity();
             quantity += oldQuantity;
             items.get(item.getItemID()).setQuantity(quantity);
         } else {
             OrderLine orderLine = new OrderLine(item, quantity);
             items.put(item.getItemID(), orderLine);
         }
     }
     
     public void updateItem(Item.Item item, Integer quantity)
     {
         if (items.containsKey(item.getItemID()))
         {
             if (quantity > 0)
             {
                 items.get(item.getItemID()).setQuantity(quantity);
             } else {
                 items.remove(item.getItemID());
             }
         } else {
             if (quantity > 0)
             {
                 OrderLine orderLine = new OrderLine(item, quantity);
                 items.put(item.getItemID(), orderLine);
             }
         }
     }
     
     public Collection<OrderLine> getItems()
     {
         return items.values();
     }
 
 }
