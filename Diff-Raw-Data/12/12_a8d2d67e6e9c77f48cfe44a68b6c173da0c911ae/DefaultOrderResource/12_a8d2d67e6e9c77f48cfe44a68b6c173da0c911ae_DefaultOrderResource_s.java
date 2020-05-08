 package org.youfood.resources.impl;
 
 import org.youfood.model.Order;
 import org.youfood.resources.OrderResource;
 import org.youfood.services.OrderService;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import java.util.List;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 @Stateless
 public class DefaultOrderResource implements OrderResource {
 
     @EJB
     private OrderService orderService;
 
     @Override
     public List<Order> getAllOrders() {
         return orderService.getAllOrder();
     }
 
     @Override
     public Order getOrder(Long id) {
         return orderService.getOrderById(id);
     }
 
     @Override
    public void updateOrder(Order order) {
         order.setStatus(order.getStatus()+1);
         orderService.updateOrder(order);
     }
 
 }
