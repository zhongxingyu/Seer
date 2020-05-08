 package com.df.http.order.resources;
 
 import java.util.List;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.codehaus.enunciate.jaxrs.TypeHint;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.df.core.rs.TenantResource;
 import com.df.order.entity.Order;
 import com.df.order.exception.OrderException;
 import com.df.order.service.contract.OrderService;
 
 @Path("/tenant/{tenantCode}/store/{storeCode}/order")
 @Produces("application/json;charset=UTF-8")
 @Component
 public class OrderResource extends TenantResource {
 
 	@Autowired
 	private OrderService orderService;
 
 	/**
 	 * Pagination retrieve orders from a store
 	 * 
 	 * @param tenantCode
 	 *            The tenant code
 	 * @param storeCode
 	 *            The store code
 	 * @param from
 	 *            The offset to begin to retrieve
 	 * @param to
 	 *            The offset to end to retrieve
 	 * @return the orders between <code>from</code> and <code>to</code>.
 	 */
 	@GET
 	@TypeHint(Order.class)
 	public List<Order> getOrders(@PathParam("tenantCode") String tenantCode, @PathParam("storeCode") String storeCode,
 	        @QueryParam("from") @DefaultValue("0") int from, @QueryParam("to") @DefaultValue("100") int to) {
 		return null;
 	}
 
 	@GET
 	@TypeHint(Order.class)
 	@Path("/sc")
 	public List<Order> getOrdersWithServiceCard(@PathParam("tenantCode") String tenantCode,
	        @PathParam("storeCode") String storeCode, @QueryParam("user") long userId) {
 		this.injectTenantContext(tenantCode);
		return orderService.getOrdersWithServiceCard(storeCode, userId);
 	}
 
 	/**
 	 * Create a order
 	 * 
 	 * @param tenantCode
 	 *            The tenant code
 	 * @param storeCode
 	 *            The store code
 	 * @param order
 	 *            The order to be created
 	 * @return the refreshed order.
 	 */
 	@POST
 	public Order createOrder(@PathParam("tenantCode") String tenantCode, @PathParam("storeCode") String storeCode,
 	        Order order) {
 		this.injectTenantContext(tenantCode);
 		orderService.createOrder(storeCode, 0, order);
 		return order;
 	}
 
 	/**
 	 * Get order by table.
 	 * 
 	 * @param tenantCode
 	 *            The tenant code
 	 * @param storeCode
 	 *            The store code
 	 * @param tableCode
 	 *            The table code
 	 * @return the order which is associated with the table.
 	 */
 	@GET
 	@Path("/table/{tableCode}")
 	public Order getOrderByTable(@PathParam("tenantCode") String tenantCode, @PathParam("storeCode") String storeCode,
 	        @PathParam("tableCode") String tableCode) {
 		this.injectTenantContext(tenantCode);
 		Order found = orderService.getOrderByTable(storeCode, tableCode);
 		if (found == null) {
 			throw OrderException.orderNotFound("No order is associated with table " + tableCode);
 		}
 		return found;
 	}
 
 	/**
 	 * Get order by order Id.
 	 * 
 	 * @param tenantCode
 	 *            The tenant code
 	 * @param storeCode
 	 *            The store code
 	 * @param tableNumber
 	 *            The table number
 	 * @return the corresponding order
 	 */
 	@GET
 	@Path("/{orderId}")
 	public Order getOrderById(@PathParam("tenantCode") String tenantCode, @PathParam("storeCode") String storeCode,
 	        @PathParam("orderId") long orderId) {
 		this.injectTenantContext(tenantCode);
 		Order found = orderService.getOrderById(storeCode, orderId);
 		if (found == null) {
 			throw OrderException.orderNotFound("Order " + orderId + " is not found");
 		}
 		return found;
 	}
 }
