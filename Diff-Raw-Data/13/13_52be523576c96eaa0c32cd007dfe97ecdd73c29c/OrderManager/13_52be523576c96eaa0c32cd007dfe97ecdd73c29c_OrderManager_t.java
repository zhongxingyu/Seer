 package edu.buet.cse.eia.ch02.v2.persistence;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import edu.buet.cse.eia.ch02.v2.model.Order;
 
 public class OrderManager {
   private static final TreeMap<Long, Order> orderMap = new TreeMap<>();
   private static long counter = 0L;
   
   public synchronized static Long addOrder(Order order) { 
    if (order == null) {
      return null;
    }
    
     order.setId(++counter);
     orderMap.put(order.getId(), order);
     
     return order.getId();
   }
   
   public synchronized static List<Order> getOrders() {
     return new ArrayList<>(orderMap.values());
   }
   
   public synchronized static Order getOrder(Long id) {
     return orderMap.get(id);
   }
   
   private OrderManager() {
   }
 }
