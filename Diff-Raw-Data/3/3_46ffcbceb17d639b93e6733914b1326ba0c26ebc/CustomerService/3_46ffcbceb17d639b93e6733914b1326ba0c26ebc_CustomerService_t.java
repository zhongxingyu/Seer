 package com.cxf.demo;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 @Path("/customerservice/")
 public class CustomerService {
     long currentId = 123;
     Map<Long, Customer> customers = new HashMap<Long, Customer>();
     Map<Long, Order> orders = new HashMap<Long, Order>();
 
     public CustomerService() {
         init();
     }
 
     @GET
     @Path("/customers/{id}/")
     @Produces({"application/json"})
     public Customer getCustomer(@PathParam("id") String id) {
         System.out.println("----invoking getCustomer, Customer id is: " + id);
         long idNumber = Long.parseLong(id);
         Customer c = customers.get(idNumber);
 
         return c;
     }
 
     @PUT
     @Path("/customers/")
     public Response updateCustomer(Customer customer) {
         System.out.println("----invoking updateCustomer, Customer name is: " + customer.getName());
         Customer c = customers.get(customer.getId());
         Response r;
         if (c != null) {
             customers.put(customer.getId(), customer);
             r = Response.ok().build();
         } else {
             r = Response.notModified().build();
         }
 
         return r;
     }
 
     @POST
     @Path("/customers/")
     public Response addCustomer(Customer customer) {
         System.out.println("----invoking addCustomer, Customer name is: " + customer.getName());
         customer.setId(++currentId);
 
         customers.put(customer.getId(), customer);
 
         return Response.ok(customer).build();
     }
 
     @DELETE
     @Path("/customers/{id}/")
     public Response deleteCustomer(@PathParam("id") String id) {
         System.out.println("----invoking deleteCustomer, Customer id is: " + id);
         long idNumber = Long.parseLong(id);
         Customer c = customers.get(idNumber);
 
         Response r;
         if (c != null) {
             r = Response.ok().build();
             customers.remove(idNumber);
         } else {
             r = Response.notModified().build();
         }
 
         return r;
     }
 
    @GET
     @Path("/orders/{orderId}/")
    @Produces({"application/json"})
     public Order getOrder(@PathParam("orderId") String orderId) {
         System.out.println("----invoking getOrder, Order id is: " + orderId);
         long idNumber = Long.parseLong(orderId);
         Order c = orders.get(idNumber);
         return c;
     }
 
     final void init() {
         Customer c = new Customer();
         c.setName("John");
         c.setId(123);
         customers.put(c.getId(), c);
 
         Order o = new Order();
         o.setDescription("order 223");
         o.setId(223);
         orders.put(o.getId(), o);
     }
 
 }
