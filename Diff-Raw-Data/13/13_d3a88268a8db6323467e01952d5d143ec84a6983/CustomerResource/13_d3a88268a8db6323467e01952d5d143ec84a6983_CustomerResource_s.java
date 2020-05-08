 package lab.api;
 
 import java.net.URI;
 import java.util.concurrent.TimeUnit;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import lab.domain.Customer;
 import lab.domain.Customers;
 import lab.service.CustomerService;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.yammer.metrics.annotation.ExceptionMetered;
 import com.yammer.metrics.annotation.Timed;
 
 @Component
 @Path("/api/customers")
 public class CustomerResource {
 
     private static final Log LOG = LogFactory.getLog(CustomerResource.class);
 
     private CustomerService service;
 
     @Autowired
     public CustomerResource(final CustomerService service) {
         this.service = service;
     }
 
     public CustomerResource() {
     }
 
     @GET
     @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     @Timed(name = "get-customers", rateUnit = TimeUnit.MILLISECONDS)
     @ExceptionMetered(name = "get-customers-failures", rateUnit = TimeUnit.MILLISECONDS)
     public Response getCustomers(@QueryParam("vulnerable") final boolean vulnerableOnly) {
 
        LOG.info("*** Customers Requested");
 
         final Customers customers = service.allCustomers(vulnerableOnly);
 
         if (customers == null || customers.getCustomers() == null || customers.getCustomers().isEmpty()) {
             return Response.status(Status.NOT_FOUND).entity("Customers not found").build();
         }
 
         return Response.ok(customers).build();
     }
 
     @GET
     @Path("/{customerId}")
     @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     @Timed(name = "get-customer", rateUnit = TimeUnit.MILLISECONDS)
     @ExceptionMetered(name = "get-customer-failures", rateUnit = TimeUnit.MILLISECONDS)
     public Response getCustomer(@PathParam("customerId") final String customerId) {
 
         LOG.info("*** Customer Requested With Id: " + customerId);
 
         final Customer customer = service.findCustomer(customerId);
 
         if (customer == null) {
             return Response.status(Status.NOT_FOUND).entity("Customer not found").build();
         }
 
         return Response.ok(customer).build();
     }
 
     @POST
     @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
     @Timed(name = "create-customer", rateUnit = TimeUnit.MILLISECONDS)
     @ExceptionMetered(name = "create-customer-failures", rateUnit = TimeUnit.MILLISECONDS)
     public Response createCustomer(final Customer customer) throws Exception {
 
         LOG.info("*** Customer Creation Requested: " + customer);
 
         final Customer createdCustomer = service.newCustomer(customer);
 
         return Response.created(new URI("/customer/" + customer.getId())).entity(createdCustomer).build();
     }
 
 }
