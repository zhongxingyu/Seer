 package de.tud.in.middleware.order;
 
 import java.util.List;
 
 import javax.ejb.Remote;
 
 import de.tud.in.middleware.products.ProductInstance;
 
 @Remote
 public interface OrderManagementRemote {
 
 	/**
 	 * @return Returns ID of added order.
 	 */
	public long addOrderForCustomer(List<ProductInstance> products, Integer customerId);
 
 	public void changeOrderState(Integer orderId, OrderState newState);
 
 	public OrderState getOrderState(Integer orderId);
 
 	public String getOrderStateAsString(Integer orderId);
 
 }
