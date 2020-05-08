 package com.omartech.tdg.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.omartech.tdg.exception.OrderItemsException;
 import com.omartech.tdg.exception.OutOfStockException;
 import com.omartech.tdg.mapper.OrderItemMapper;
 import com.omartech.tdg.mapper.OrderMapper;
 import com.omartech.tdg.mapper.SellerMapper;
 import com.omartech.tdg.model.ClaimItem;
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
 	@Autowired
 	private EmailService emailService;
 	@Autowired
 	private FinanceService financeService;
 	
 	/**
 	 * 插入投诉项，同时给卖家和买家发送邮件
 	 * @param orderId
 	 * @param reasonId
 	 */
 	public void claimOrder(int orderId, int reasonId, String comment){
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
		claimItem.setComment(comment);
 		claimService.insert(claimItem);
 		updateOrderStatus(OrderStatus.COMPLAIN, orderId);
 	}
 	/**
 	 * 某个卖家在某段时间内的订单
 	 * @param begin
 	 * @param end
 	 * @param sellerId
 	 * @return
 	 */
 	public List<Order> getOrdersByDateRangeAndSellerId(Date begin, Date end, int sellerId){
 		return orderMapper.getOrdersByDateRangeAndSellerId(begin, end, sellerId);
 	}
 	/**
 	 * 某时间段内的所有订单
 	 * @param begin
 	 * @param end
 	 * @return
 	 */
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
 	
 	private void setOrderItemsToOrders(List<Order> originOrders){
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
 		if(order == null){
 			return null;
 		}
 		List<OrderItem> orderItems = orderItemMapper.getOrderItemsByOrderId(id);
 		order.setOrderItems(orderItems);
 		return order;
 	}
 	
 	public void updateOrderBySeller(Order order){
 		orderMapper.updateOrder(order);
 	}
 	/**
 	 * 变更到某订单状态
 	 * @param status
 	 * @param orderId
 	 * @throws OutOfStockException
 	 */
 	@Transactional(rollbackFor = Exception.class)
 	public void updateOrderStatus(int status, int orderId)throws OutOfStockException{
 		updateOrderStatus(status, orderId, null, null);
 	}
 	/**
 	 * 变更到某状态
 	 * 给商家留言的地方
 	 * @param status
 	 * @param orderId
 	 * @param comment
 	 * @throws OutOfStockException
 	 */
 	@Transactional(rollbackFor = Exception.class)
 	public void updateOrderStatus(int status, int orderId, String cancelComment, Integer cancelReason)throws OutOfStockException{
 		Order order = getOrderById(orderId);
 		/**
 		 * 判断要变化到的状态，然后做 相应的处理
 		 * 不同的原订单状态 要求 新状态的设定时间不一样
 		 * 	付款项只需要设定
 		 */
 		if(status == OrderStatus.PAID){//当付款的时候才会减到库存
 			reduceStock(order);//减少订单中货物的库存
 		}else if(status == OrderStatus.CANCELBYSELLER){//若是商家取消订单，则需要标注原因
 			orderCancelledBySeller(order, cancelComment, cancelReason);
 		}else if(status == OrderStatus.COMPLAIN){//某订单被投诉
 			
 		}
 		financeService.insertOrderFinance(order, status);//根据先前状态来判读是否需要插入新的款项
 		order.setOrderStatus(status);
 		orderMapper.updateOrder(order);
 		OrderRecord record = OrderRecordFactory.createByStatus(order, status);
 		orderRecordService.insertOrderRecord(record);
 	}
 	
 	/**
 	 * 若由商家取消
 	 * 1. 判断是否已经付款；
 	 * 		若未付，无所谓了
 	 * 		若已付，先退款
 	 * 2. 设置原因
 	 * 3. 发送邮件
 	 * @param order
 	 * @param cancelComment
 	 * @param cancelReason
 	 */
 	private void orderCancelledBySeller(Order order ,String cancelComment, Integer cancelReason){
 		order.setCancelComment(cancelComment);
 		order.setCancelReason(cancelReason);
 		emailService.sendEmailWhenSellerCancelOrder(order);
 	}
 	
 	
 	/**
 	 * 创建订单
 	 * 1. 判断是否均来自一个商家，否则需要切分订单
 	 * 2. 根据购买数量来扣掉对应的库存
 	 * 3. 发送邮件通知已经下单成功
 	 * @param order
 	 * @return
 	 */
 	@Transactional(rollbackFor=Exception.class)
 	public int insertOrder(Order order){
 		boolean needSplit = checkNeedSplit(order);
 		if(order.getPrice() == 0){
 			countPrice(order);
 		}
 		orderMapper.insertOrder(order);//插入订单
 		orderRecordService.insertOrderRecord(OrderRecordFactory.createByStatus(order, order.getOrderStatus()));//插入订单记录
 		
 		emailService.sendEmailWhenMakeOrderOk(order);//给卖家发邮件
 		
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
 	/**
 	 * 减少订单中对应货物的库存
 	 */
 	public void reduceStock(Order order) throws OutOfStockException{
 		List<OrderItem> orderItems = order.getOrderItems();
 		for(OrderItem orderItem : orderItems){
 			int id = orderItem.getItemId();
 			int count = orderItem.getNum();
 			itemService.reduceStock(id, count);
 		}
 	}
 	/**
 	 * 检测是否需要切分订单
 	 * @param order
 	 * @return
 	 * @throws OrderItemsException
 	 */
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
 	/**
 	 * 将一个订单切分为多个订单
 	 * @param order
 	 * @param orderId
 	 * @return
 	 */
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
 			subOrder.setCreateAt(new Date());
 			countPrice(subOrder);
 			orders.add(subOrder);
 		}
 		return orders;
 	}
 	/**
 	 * orderItem 的price为单价，priceRMB为对应的rmb价格
 	 * order中的price 为总价，priceRMB为对应的rmb总价
 	 */
 	private void countPrice(Order order){
 		List<OrderItem> orderItems = order.getOrderItems();
 		float price = 0f;//该订单的总价
 		float priceRMB = 0f;
 		float transfeeAll = 0f;//该订单的运费总价
 		float transfeeAllRMB = 0f;
 		float orderFeeAll = 0f;//订单中货物价格
 		float orderFeeAllRMB = 0f;
 		float discountFee = 0f;//总折扣费用
 		float discountFeeRMB = 0f;
 		float originOrderFee = 0f;
 		float originOrderFeeRMB = 0f;
 		for(OrderItem orderItem : orderItems){
 			float tmpTotal = orderItem.getSumPrice() + orderItem.getInternationalShippingFee();
 			float tmpTotalRMB = orderItem.getSumPriceRMB() + orderItem.getIfeeRMB();
 			price += tmpTotal;
 			priceRMB += tmpTotalRMB;
 			
 			transfeeAll += orderItem.getInternationalShippingFee();
 			transfeeAllRMB += orderItem.getIfeeRMB();
 			
 			orderFeeAll += orderItem.getSumPrice();
 			orderFeeAllRMB += orderItem.getSumPriceRMB();
 			
 			discountFee += (orderItem.getDiscountFee() * orderItem.getNum());
 			discountFeeRMB += (orderItem.getDiscountFeeRMB() * orderItem.getNum());
 			
 			float originTmp = orderItem.getPrice() * orderItem.getNum();
 			float originTmpRMB = orderItem.getPriceRMB() * orderItem.getNum();
 			
 			originOrderFee += originTmp;
 			originOrderFeeRMB += originTmpRMB;
 			
 			
 			
 		}
 		order.setPrice(price);
 		order.setPriceRMB(priceRMB);
 		order.setTransferPrice(transfeeAll);
 		order.setTransferPriceRMB(transfeeAllRMB);
 		order.setOrderPrice(orderFeeAll);
 		order.setOrderPriceRMB(orderFeeAllRMB);
 		order.setDiscountFee(discountFee);
 		order.setDiscountFeeRMB(discountFeeRMB);
 		order.setOriginPrice(originOrderFee);
 		order.setOriginPriceRMB(originOrderFeeRMB);
 		float tmpOriginTotal = originOrderFee + transfeeAll;
 		float tmpOriginTotalRMB = originOrderFeeRMB + transfeeAllRMB;
 		order.setOriginTotal(tmpOriginTotal);
 		order.setOriginTotalRMB(tmpOriginTotalRMB);
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
