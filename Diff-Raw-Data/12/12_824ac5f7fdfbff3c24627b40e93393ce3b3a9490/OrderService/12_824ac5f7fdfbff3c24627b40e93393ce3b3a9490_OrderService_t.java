 package org.jz.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jz.domain.CustomerId;
 import org.jz.domain.Order;
 import org.jz.domain.Product;
 import org.jz.domain.exception.OrderException;
 
 /**
  * @author Kristian Rosenvold
  */
 public class OrderService
 {
     private final AccountService accountService;
 
     private final InventoryService inventoryService;
 
     public OrderService( AccountService accountService, InventoryService inventoryService )
     {
         this.accountService = accountService;
         this.inventoryService = inventoryService;
     }
 
    public List<Product> orderItems( CustomerId authenticatedCustomer, List<Product> products, int maxItems )
     {
         final Order order = accountService.createOrder( authenticatedCustomer );
         List<Product> orderedProducts = new ArrayList<Product>();
 
         if ( products.size() > maxItems )
         {
             throw new OrderException(
                 "Too many items in order, " + products.size() + ", " + maxItems + " is current max" );
         }
         for ( Product product : products )
         {
             inventoryService.remove( product );
             accountService.orderItem( order, product );
             orderedProducts.add( product );
         }
         return orderedProducts;
     }
 }
