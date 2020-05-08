 package com.omartech.tdg.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.omartech.tdg.exception.OrderItemsException;
 import com.omartech.tdg.mapper.OrderItemMapper;
 import com.omartech.tdg.mapper.OrderMapper;
 import com.omartech.tdg.mapper.SellerMapper;
 import com.omartech.tdg.model.ClaimItem;
 import com.omartech.tdg.model.Coinage;
 import com.omartech.tdg.model.Order;
 import com.omartech.tdg.model.OrderItem;
 import com.omartech.tdg.model.OrderRecord;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.utils.ClaimRelation;
 import com.omartech.tdg.utils.OrderRecordFactory;
 import com.omartech.tdg.utils.OrderStatus;
 
 @Service
 public class OrderService {
 	
 	@Autowired
 	private OrderMapper orderMapper;
 	@Autowired
 	private OrderItemMapper orderItemMapper;
 	@Autowired
 	private SellerMapper sellerMapper;
 	@Autowired
 	private OrderRecordService orderRecordService;
 	@Autowired
 	private ItemService itemService;
 	@Autowired
 	private ClaimService claimService;
 	
 	public void claimOrder(int orderId, int reasonId){
 		ClaimItem claimItem = new ClaimItem();
 		Order order = getOrderById(orderId);
 		int status = order.getOrderStatus();
 		claimItem.setPreviousStatus(status);
 		claimItem.setStatus(ClaimRelation.complain);
 		claimItem.setClaimType(ClaimRelation.Order);
 		claimItem.setClaimItemId(orderId);
 		claimItem.setSellerId(order.getSellerId());
 		claimItem.setCustomerId(order.getCustomerId());
 		claimItem.setClaimTypeId(reasonId);
 		claimService.insert(claimItem);
 		updateOrderStatus(OrderStatus.COMPLAIN, orderId);
 	}
 	
 	public List<Order> getOrdersByDateRange(Date begin, Date end){
 		return orderMapper.getOrdersByDateRange(begin, end);
 	}
 	
 	public List<Order> getCustomerOrdersByStatusAndPage(int customerId, int status, Page page){
 		List<Order> orders = new ArrayList<Order>();
 		if(status == 0 ){
 			orders =  getCustomerOrdersByPage(customerId, page);
 			
 		}else{
 			orders = orderMapper.getCustomerOrdersByStatusAndPage(customerId, status, page);
 		}
 		setOrderItemsToOrders(orders);
 		return orders;
 	}
 	
 	public void setOrderItemsToOrders(List<Order> originOrders){
 		for(Order order : originOrders){
 			int id = order.getId();
 			List<OrderItem> items = orderItemMapper.getOrderItemsByOrderId(id);
 			order.setOrderItems(items);
 		}
 	}
 	
 	public List<Order> getSellerOrdersByStatusAndPage(int sellerId, int status, Page page){
 		if(status == 0 ){
 			return getSellerOrdersByPage(sellerId, page);
 		}
 		return orderMapper.getSellerOrdersByStatusAndPage(sellerId, status, page);
 	}
 	public List<Order> getOrdersByStatusAndPage(int status, Page page){
 		return orderMapper.getOrdersByStatusAndPage(status, page);
 	}
 	
 	public List<Order> getCustomerOrdersByPage(int customerId, Page page){
 		return orderMapper.getCustomerOrdersByPage(customerId, page);
 	}
 	public List<Order> getSellerOrdersByPage(int sellerId, Page page){
 		return orderMapper.getSellerOrdersByPage(sellerId, page);
 	}
 	public List<Order> getOrdersByPage(Page page){
 		return orderMapper.getOrdersByPage(page);
 	}
 	
 	public Order getOrderById(int id){
 		Order order = orderMapper.getOrderById(id);
 		List<OrderItem> orderItems = orderItemMapper.getOrderItemsByOrderId(id);
 		order.setOrderItems(orderItems);
 		return order;
 	}
 	
 	public void updateOrderBySeller(Order order){
 		orderMapper.updateOrder(order);
 	}
 	
 	public void updateOrderStatus(int status, int orderId){
 		Order order = getOrderById(orderId);
 		order.setOrderStatus(status);
 		orderMapper.updateOrder(order);
 		if(order.getHasChildren() == 1){ //有子订单
 			List<Order> subOrders = orderMapper.getOrdersByParentId(orderId);
 			for(Order or : subOrders){
 				updateOrderStatus(status, or.getId());
 			}
 		}
 		OrderRecord record = OrderRecordFactory.createByStatus(order, status);
 		orderRecordService.insertOrderRecord(record);
 	}
 	
 	/**
 	 * 创建订单
 	 * 1. 判断是否均来自一个商家，否则需要切分订单
 	 * 2. 根据购买数量来扣掉对应的库存
 	 * 3. 发送邮件通知已经下单成功
 	 * @param order
 	 * @return
 	 */
 //	public int insertOrder(Order order){
 //		boolean needSplit = checkNeedSplit(order);
 //		float price = countPrice(order.getOrderItems());
 //		order.setPrice(price);
 //		orderMapper.insertOrder(order);
 //		orderRecordService.insertOrderRecord(OrderRecordFactory.createByStatus(order, order.getOrderStatus()));
 //		
 //		int orderId = order.getId();
 //		for(OrderItem item : order.getOrderItems()){
 //			item.setOrderId(orderId);
 //			orderItemMapper.insertOrderItem(item);
 //		}
 //		if(needSplit){
 //			List<Order> orders = splitOrder(order,orderId);
 //			for(Order subOrder : orders){
 //				float subPrice = countPrice(order.getOrderItems());
 //				order.setPrice(subPrice);
 //				orderMapper.insertOrder(subOrder);
 //				orderRecordService.insertOrderRecord(OrderRecordFactory.createByStatus(order, order.getOrderStatus()));
 //				for(OrderItem item : subOrder.getOrderItems()){
 //					item.setOrderId(subOrder.getId());
 //					orderItemMapper.insertOrderItem(item);
 //				}
 //			}
 //			order.setOrderStatus(OrderStatus.CUT);
 //			updateOrderStatus(OrderStatus.CUT, orderId);
 //		}
 //		return orderId;
 //	}
 	public int insertOrder(Order order){
 		boolean needSplit = checkNeedSplit(order);
 		if(order.getPrice() == 0){
 			countPrice(order);
 		}
 		orderMapper.insertOrder(order);
 		orderRecordService.insertOrderRecord(OrderRecordFactory.createByStatus(order, order.getOrderStatus()));
 		
 		int orderId = order.getId();
 		for(OrderItem item : order.getOrderItems()){
 			item.setOrderId(orderId);
 			orderItemMapper.insertOrderItem(item);
 		}
 		if(needSplit){
 			List<Order> orders = splitOrder(order,orderId);
 			for(Order subOrder : orders){
 				insertOrder(subOrder);
 			}
 			order.setOrderStatus(OrderStatus.CUT);
 			updateOrderStatus(OrderStatus.CUT, orderId);
 		}
 		return orderId;
 	}
 	private boolean checkNeedSplit(Order order) throws OrderItemsException{
 		List<OrderItem> orderItems = order.getOrderItems();
 		if(orderItems!=null){
 			int sellerId = orderItems.get(0).getSellerId();
 			for(OrderItem item : orderItems){
 				int tmpId = item.getSellerId();
 				if(tmpId != sellerId){
 					return true;
 				}
 			}
 			Seller seller = sellerMapper.getSellerById(sellerId);
 			String sellerName = seller.getBusinessName();
 			order.setSellerId(sellerId);
 			order.setSellerName(sellerName);
 			return false;
 		}else{
 			throw new OrderItemsException(order);
 		}
 	}
 	
 	private List<Order> splitOrder(Order order, int orderId){
 		List<OrderItem> orderItems = order.getOrderItems();
 		Map<Integer, List<OrderItem>> sellerMap = new HashMap<Integer, List<OrderItem>>();
 		
 		for(OrderItem item : orderItems){
 			int tmpId = item.getSellerId();
 			List<OrderItem> orderItemsTmp = null;
 			if(sellerMap.containsKey(tmpId)){
 				orderItemsTmp = sellerMap.get(tmpId);
 			}else{
 				orderItemsTmp = new ArrayList<OrderItem>();
 			}
 			orderItemsTmp.add(item);
 			sellerMap.put(tmpId, orderItemsTmp);
 		}
 		List<Order> orders = new ArrayList<Order>();
 		for(Entry<Integer, List<OrderItem>> entry :  sellerMap.entrySet()){
 			List<OrderItem> subOrderItems = entry.getValue();
 			int sellerId = entry.getKey();
 			
 			Order subOrder = new Order();
 			subOrder.setAddress(order.getAddress());
 			subOrder.setCity(order.getCity());
 			subOrder.setCountry(order.getCountry());
 			subOrder.setPostCode(order.getPostCode());
 			subOrder.setCustomerId(order.getCustomerId());
 			subOrder.setName(order.getName());
 			subOrder.setOrderItems(subOrderItems);
 			subOrder.setSellerId(sellerId);
 			String sellerName = sellerMapper.getSellerById(sellerId).getBusinessName();
 			subOrder.setSellerName(sellerName);
 			subOrder.setOrderStatus(OrderStatus.NOPAY);
 			subOrder.setParentId(orderId);
 			countPrice(subOrder);
 			orders.add(subOrder);
 		}
 		return orders;
 	}
 	/*
 	 * orderItem 的price为单价，priceRMB为对应的rmb价格
 	 * order中的price 为总价，priceRMB为对应的rmb总价
 	 */
 	private void countPrice(Order order){
 		List<OrderItem> orderItems = order.getOrderItems();
 		float price = 0f;
 		float priceRMB = 0f;
 		float transfeeAll = 0f;
 		float transfeeAllRMB = 0f;
 		float orderFeeAll = 0f;
 		float orderFeeAllRMB = 0f;
 		for(OrderItem orderItem : orderItems){
 			int coinage = orderItem.getCoinage();
 			order.setCoinage(coinage);
 			int count = orderItem.getNum();
 			
 			float origin = orderItem.getPrice();
 			float rmb = Coinage.compute(coinage, origin);
 			
 			float ifee = orderItem.getInternationalShippingFee();
 			float ifeeRMB = Coinage.compute(coinage, ifee);
 			
 			float op = origin * count;
 			float opRMB = rmb * count;
 			
 			float orderItemPriceRMB = opRMB + ifeeRMB;
 			float orderItemPrice = op + ifee;
 
 			price += orderItemPrice;
 			priceRMB += orderItemPriceRMB;
 			transfeeAll += ifee;
 			transfeeAllRMB += ifeeRMB;
 			orderFeeAll += op;
 			orderFeeAllRMB += opRMB;
 		}
 		order.setPrice(price);
 		order.setPriceRMB(priceRMB);
 		order.setTransferPrice(transfeeAll);
 		order.setTransferPriceRMB(transfeeAllRMB);
 		order.setOrderPrice(orderFeeAll);
 		order.setOrderPriceRMB(orderFeeAllRMB);
 	}
 	
 	public OrderMapper getOrderMapper() {
 		return orderMapper;
 	}
 	public void setOrderMapper(OrderMapper orderMapper) {
 		this.orderMapper = orderMapper;
 	}
 	public OrderItemMapper getOrderItemMapper() {
 		return orderItemMapper;
 	}
 	public void setOrderItemMapper(OrderItemMapper orderItemMapper) {
 		this.orderItemMapper = orderItemMapper;
 	}
 	public SellerMapper getSellerMapper() {
 		return sellerMapper;
 	}
 	public void setSellerMapper(SellerMapper sellerMapper) {
 		this.sellerMapper = sellerMapper;
 	}
 	public OrderRecordService getOrderRecordService() {
 		return orderRecordService;
 	}
 	public void setOrderRecordService(OrderRecordService orderRecordService) {
 		this.orderRecordService = orderRecordService;
 	}
 	public ItemService getItemService() {
 		return itemService;
 	}
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 	
 
 }
