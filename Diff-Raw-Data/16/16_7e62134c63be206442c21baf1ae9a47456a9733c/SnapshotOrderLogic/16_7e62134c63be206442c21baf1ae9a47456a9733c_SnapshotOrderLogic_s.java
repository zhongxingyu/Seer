 package de.tud.in.middleware.snapshot;
 
 import java.util.List;
 import java.util.Map;
 
 import de.tud.in.middleware.order.CustomerOrder;
 import de.tud.in.middleware.order.OrderManagementRemote;
 import de.tud.in.middleware.order.OrderState;
 import de.tud.in.middleware.products.ProductInstance;
 
 public final class SnapshotOrderLogic implements OrderManagementRemote {
 
 	private final Map<Integer, CustomerOrder> orderMap;
 
 	public SnapshotOrderLogic(final Map<Integer, CustomerOrder> orderMap) {
 		this.orderMap = orderMap;
 	}
 
 	/**
 	 * Unsupported by snapshot.
 	 */
 	@Override
	public long addOrderForCustomer(final List<ProductInstance> products, final Integer customerId) {
 		throw new UnsupportedOperationException("Snapshot is read only.");
 	}
 
 	/**
 	 * Unsupported by snapshot.
 	 */
 	@Override
 	public void changeOrderState(final Integer orderId, final OrderState newState) {
 		throw new UnsupportedOperationException("Snapshot is read only.");
 	}
 
 	@Override
 	public OrderState getOrderState(final Integer orderId) {
 		return orderMap.get(orderId).getState();
 	}
 
 	@Override
 	public String getOrderStateAsString(final Integer orderId) {
 		return getOrderState(orderId).toString();
 	}
 
 }
