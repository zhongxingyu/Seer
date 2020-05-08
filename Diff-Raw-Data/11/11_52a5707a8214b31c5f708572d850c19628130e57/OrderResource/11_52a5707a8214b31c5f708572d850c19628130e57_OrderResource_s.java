 package org.youfood.resources;
 
 import org.youfood.model.Order;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.List;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 @Path("/orders")
 public interface OrderResource {
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     List<Order> getAllOrders();
 
 
     @GET
     @Path("/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     Order getOrder(@PathParam("id") Long id);
 
 
     @PUT
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     Order updateOrder(Order order);
 
 }
