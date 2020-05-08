 package org.jz.services;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.jz.domain.PersonCustomer;
 import org.jz.domain.PersonName;
 import org.jz.domain.Product;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * @author Kristian Rosenvold
  */
 public class OrderServiceTest
 {
     private final InventoryService inventoryService = new InventoryService();
 
     private final AccountService accountService = new AccountService();
 
     private final CustomerService customerService = new CustomerService();
 
     @Test
     public void orderVeggies()
     {
         final PersonCustomer customer = customerService.findCustomer( new PersonName( "Kristian", "Rosenvold" ) );
         OrderService orderService = new OrderService( accountService, inventoryService );
         List<Product> products = inventoryService.findProductsByName("Broccoli", "Carrot");
        int max = accountService.getMaxItemsForOrder( customer.getCustomerId());
        final List<Product> orderedProducts = orderService.orderItems( customer.getCustomerId(), products, max );
         assertEquals(2, orderedProducts.size());
     }
 }
