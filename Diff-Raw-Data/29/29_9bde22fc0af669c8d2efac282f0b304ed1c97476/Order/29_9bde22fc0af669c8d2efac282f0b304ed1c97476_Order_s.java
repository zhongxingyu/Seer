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
     
        public void addItem(Item item, Integer quantity)
     {
        if (items.containsKey(item.code) && quantity > 0)
         {
             Integer oldQuantity = items.get(item.code).getQuantity();
             quantity += oldQuantity;
            items.get(item.code).setQuantity(quantity);
         } else {
             OrderLine orderLine = new OrderLine(item, quantity);
            items.put(item.code, orderLine);
         }
     }
     
    public void updateItem(Item item, Integer quantity)
     {
        if (items.containsKey(item.code))
         {
             if (quantity > 0)
             {
                items.get(item.code).setQuantity(quantity);
             } else {
                items.remove(item.code);
             }
         } else {
             if (quantity > 0)
             {
                 OrderLine orderLine = new OrderLine(item, quantity);
                items.put(item.code, orderLine);
             }
         }
     }
     
    public Collection<orderLine> getItems()
     {
         return items.values();
     }
 
 }
