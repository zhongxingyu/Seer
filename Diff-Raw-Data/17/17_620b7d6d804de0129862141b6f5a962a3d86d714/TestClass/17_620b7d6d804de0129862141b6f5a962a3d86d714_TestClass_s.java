 package com.onb.orderingsystem.domain;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.onb.orderingsystem.exceptions.ProductException;
 import com.onb.orderingsystem.utils.Enumerators.OrderStatus;
 
 public class TestClass {
 	
 	@Test 
 	public void testOrderItemTotalPrice() {
 		Product product = new Product(1, "Apple" , 10, new BigDecimal(20.00));
 		OrderItem orderItem = new OrderItem(1, product, 4);
 		BigDecimal orderItemTotalPrice = product.getProductPrice().multiply(new BigDecimal(orderItem.getOrderItemQuantity()));
 		
 		assertEquals(orderItemTotalPrice, orderItem.computeTotalPrice());
 	}
 	
 	/*@Test
 	public void testOrderDate(){
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 		Product product = new Product(1, "Apple", 10, new BigDecimal(20.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(25.00));
 		OrderItem orderItem = new OrderItem(1, product, 4);
 		OrderItem orderItem2 = new OrderItem(2, product2, 5);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID, sdf.format(cal.getTime()));
 		
 		assertEquals(new String("2011/05/09"), order.getOrderDate());
 	}*/
 	
 	@Test
 	public void testDate(){
 		Product product = new Product(1, "Apple", 10, new BigDecimal(25.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(25.00));
 		OrderItem orderItem = new OrderItem(1, product, 1);
 		OrderItem orderItem2 = new OrderItem(2, product2, 1);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID,
				"2011/05/11");
 		Order order2 = new Order(1, orderItemList, OrderStatus.PAID,
				"2011/05/11");
 		List<Order> orderList = new ArrayList<Order>();
 		orderList.add(order);
 		orderList.add(order2);
 		
		assertEquals("2011/05/11", order.getOrderDate());
 	}
 	
 	@Test
 	public void testOrderTotalPrice(){
 		Product product = new Product(1, "Apple", 10, new BigDecimal(20.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(25.00));
 		OrderItem orderItem = new OrderItem(1, product, 4);
 		OrderItem orderItem2 = new OrderItem(2, product2, 5);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID, "2011/05/09");
 		
 		BigDecimal orderItemTotalPrice1 = orderItem.computeTotalPrice();
 		BigDecimal orderItemTotalPrice2 = orderItem2.computeTotalPrice();
 		BigDecimal sumOfOrderItemTotalPrices = orderItemTotalPrice1.add(orderItemTotalPrice2);
 		
 		assertEquals(sumOfOrderItemTotalPrices,order.computeOrderTotalPrice());
 	}
 	
 	@Test
 	public void testTotalPaidOrders(){
 		Product product = new Product(1, "Apple", 10, new BigDecimal(25.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(25.00));
 		OrderItem orderItem = new OrderItem(1, product, 1);
 		OrderItem orderItem2 = new OrderItem(2, product2, 1);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID, "2011/05/09");
 		Order order2 = new Order(1, orderItemList, OrderStatus.PAID, "2011/05/09");
 		List<Order> orderList = new ArrayList<Order>();
 		orderList.add(order);
 		orderList.add(order2);
 		Customer customer = new Customer(1, "Jollibee", orderList);
 		
 		assertEquals(new BigDecimal(50.00),customer.computeTotalPaidOrders());
 	}
 	
 	@Test
 	public void testTotalUnpaidOrders(){
 		Product product = new Product(1, "Apple", 10, new BigDecimal(150000.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(5.00));
 		OrderItem orderItem = new OrderItem(1, product, 1);
 		OrderItem orderItem2 = new OrderItem(2, product2, 1);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID, "2011/05/09");
 		Order order2 = new Order(2, orderItemList, OrderStatus.UNPAID, "2011/05/09");
 		Order order3 = new Order(3, orderItemList, OrderStatus.PAID, "2011/05/09");
 		List<Order> orderList = new ArrayList<Order>();
 		orderList.add(order);
 		orderList.add(order2);
 		orderList.add(order3);
 		Customer customer = new Customer(1, "Jollibee", orderList);
 		
 		assertEquals(new BigDecimal(300010.00), customer.computeTotalUnpaidOrders());
 	}
 	
 	@Test
 	public void testCustomerCreditLimit(){
 		Product product = new Product(1, "Apple", 10, new BigDecimal(150000.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(5.00));
 		OrderItem orderItem = new OrderItem(1, product, 1);
 		OrderItem orderItem2 = new OrderItem(2, product2, 1);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID, "2011/05/09");
 		Order order2 = new Order(2, orderItemList, OrderStatus.UNPAID, "2011/05/09");
 		Order order3 = new Order(3, orderItemList, OrderStatus.PAID, "2011/05/09");
 		List<Order> orderList = new ArrayList<Order>();
 		orderList.add(order);
 		orderList.add(order2);
 		orderList.add(order3);
 		Customer customer = new Customer(1, "Jollibee", orderList);
 		
 		assertEquals(new BigDecimal(456000.00), customer.computeCreditLimit());
 	}
 
 		@Test (expected = ProductException.class)
 	public void testTransWithSupplyConflict() throws ProductException {
 		
 		int qtyOrdered = 10;
 		Product product = new Product(1, "Apple", 2, new BigDecimal(20));
 		Order orderBasket = new Order();
 		OrderItem orderItem = new OrderItem(1, product, qtyOrdered);
 		orderBasket.addOrderItem(orderItem);
 		
 	}
 	
 	@Test 
 	public void testOrders() throws ProductException {
 
 		Product product = new Product(1, "Apple", 2, new BigDecimal(20));
 		OrderItem orderItem = new OrderItem(1, product, 2);
 		
 		
 		Order order = new Order();
 		order.setOrderID(1);
 		order.addOrderItem(orderItem);
 		
 		List <OrderItem> mockList = new ArrayList<OrderItem>();
 		mockList.add(orderItem);
 		
 		assertEquals(mockList,order.getOrderList());
 	
 	}
 	
 //	@Test (expected = ProductException.class)
 //	public void testOrderItemQtyConflict() throws ProductException {
 //		Product product = new Product(1, "Apple" ,10, 20);
 //		Order order = new Order();
 //		OrderItem orderItem = new OrderItem(1, product, 11);
 //		order.addOrderItem(orderItem);
 //		
 //	}
 	
 	@Test
 	public void testAddOrderItem() throws ProductException {	
 		Product product = new Product(1, "Apple", 10, new BigDecimal(20.00));
 		OrderItem orderItem = new OrderItem(1, product, 9);
 		Order order = new Order();
 		order.setOrderID(1);
 		order.addOrderItem(orderItem);
 	}
 	
 	@Test
 	public void testCheckDiscount() {
 		
 		//Make two orders
 		Product product1 = new Product(1, "Apple", 100001, new BigDecimal(20.00));
 		OrderItem orderItem1 = new OrderItem(1, product1, 100000);	
 		List<OrderItem> orderItemList1 = new ArrayList<OrderItem>();
 		orderItemList1.add(orderItem1);
 		Order order1 = new Order();
 		order1.setOrderID(1);
 		order1.setOrderList(orderItemList1);
 		order1.setOrderStatus(OrderStatus.PAID);
 		Product product2 = new Product(2, "Orange", 20, new BigDecimal(25.00));OrderItem orderItem2 = new OrderItem(2, product2, 9);
 		List<OrderItem> orderItemList2 = new ArrayList<OrderItem>();
 		orderItemList2.add(orderItem2);
 		Order order2 = new Order();
 		order2.setOrderID(2);
 		order2.setOrderList(orderItemList2);
 		order2.setOrderStatus(OrderStatus.UNPAID);
 		
 		//make the customer and add the two orders to it's list
 		Customer customer = new Customer();
 		customer.addOrder(order1);
 		customer.addOrder(order2);
 		
 		//make another order where discount can be applied
 		Product currentProduct = new Product(3, "Orange", 10, new BigDecimal(25.00));
 		OrderItem currentOrderItem = new OrderItem(3, currentProduct, 5);
 		List<OrderItem> currentOrderItemList = new ArrayList<OrderItem>();
 		currentOrderItemList.add(currentOrderItem);
 		Order currentOrder = new Order();
 		currentOrder.setOrderID(3);
 		currentOrder.setOrderList(currentOrderItemList);
 		currentOrder.setOrderCustomer(customer);
 		
 		assertTrue(currentOrder.getOrderCustomer().checkDiscount());	
 	}
 	
 	@Test
 	public void testApplyDiscount() {
 		
 		//Make two orders
 		Product product1 = new Product(1, "Apple", 100001, new BigDecimal(20.00));
 		OrderItem orderItem1 = new OrderItem(1, product1, 100000);	
 		List<OrderItem> orderItemList1 = new ArrayList<OrderItem>();
 		orderItemList1.add(orderItem1);
 		Order order1 = new Order();
 		order1.setOrderID(1);
 		order1.setOrderList(orderItemList1);
 		order1.setOrderStatus(OrderStatus.PAID);
 		Product product2 = new Product(2, "Orange", 20, new BigDecimal(25.00));OrderItem orderItem2 = new OrderItem(2, product2, 9);
 		List<OrderItem> orderItemList2 = new ArrayList<OrderItem>();
 		orderItemList2.add(orderItem2);
 		Order order2 = new Order();
 		order2.setOrderID(2);
 		order2.setOrderList(orderItemList2);
 		order2.setOrderStatus(OrderStatus.UNPAID);
 		
 		//make the customer and add the two orders to it's list
 		Customer customer = new Customer();
 		customer.addOrder(order1);
 		customer.addOrder(order2);
 		
 		//make another order (current order) where discount can be applied
 		Product currentProduct = new Product(3, "Orange", 10, new BigDecimal(25.00));
 		OrderItem currentOrderItem = new OrderItem(3, currentProduct, 5);
 		List<OrderItem> currentOrderItemList = new ArrayList<OrderItem>();
 		currentOrderItemList.add(currentOrderItem);
 		Order currentOrder = new Order();
 		currentOrder.setOrderID(3);
 		currentOrder.setOrderList(currentOrderItemList);
 		currentOrder.setOrderCustomer(customer);
 		
 		//compute current order's total price and set it
 		currentOrder.setOrderTotalPrice(currentOrder.computeOrderTotalPrice());
 		//System.out.println(currentOrder.getOrderTotalPrice());
 		
 		BigDecimal regularPrice = currentOrder.getOrderTotalPrice();
 		
 		//apply discount to the current order's total price
 		currentOrder.applyDiscount();
 		//System.out.println(currentOrder.getOrderTotalPrice());
 		
 		BigDecimal  discountedPrice = currentOrder.getOrderTotalPrice();
 		
 		assertTrue(discountedPrice != regularPrice);
 	}
 	
 	@Test
 	public void testSameOrderItem () throws ProductException {
 		
 		Product iPhone = new Product(1, "iPhone", 20, new BigDecimal(30000.50));
 		Product iPad = new Product(2, "iPad", 80, new BigDecimal(30000.50));
 		Product macAir = new Product(3, "MacAir", 80, new BigDecimal(30000.50));
 		
 		OrderItem orderItem = new OrderItem(1, iPhone, 2);
 		OrderItem orderItem2 = new OrderItem(2, iPhone, 5);
 		OrderItem orderItem3 = new OrderItem(3, iPad, 8);
 		OrderItem orderItem4 = new OrderItem(4, iPad, 5);
 		OrderItem orderItem5 = new OrderItem(5, macAir, 50);
 		OrderItem orderItem6 = new OrderItem(6, macAir, 2);		
 		
 		Order order = new Order();
 		order.setOrderID(1);
 		order.addOrderItem(orderItem);
 		order.addOrderItem(orderItem2);
 		order.addOrderItem(orderItem3);
 		order.addOrderItem(orderItem4);
 		order.addOrderItem(orderItem5);
 		order.addOrderItem(orderItem6);
 		
 		order.consolidateItems();
 		
 		List <OrderItem> list = new ArrayList<OrderItem>();
 		list = order.getOrderList(); 
 		
 		assertEquals(7,list.get(0).getOrderItemQuantity());
 		assertEquals(13,list.get(1).getOrderItemQuantity());
 		assertEquals(52,list.get(2).getOrderItemQuantity());		
 	}
 	
 	@Test
 	public void testCheckCreditLimit() throws Exception{
 		Product product = new Product(1, "Apple", 10, new BigDecimal(500.00));
 		Product product2 = new Product(2, "Orange", 10, new BigDecimal(500.00));
 		OrderItem orderItem = new OrderItem(1, product, 1);
 		OrderItem orderItem2 = new OrderItem(2, product2, 1);
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		orderItemList.add(orderItem);
 		orderItemList.add(orderItem2);
 		Order order = new Order(1, orderItemList, OrderStatus.UNPAID,
 				"2011/05/09");
 		Order order2 = new Order(2, orderItemList, OrderStatus.UNPAID,
 				"2011/05/09");
 		Order order3 = new Order(3, orderItemList, OrderStatus.PAID,
 				"2011/05/09");
 		List<Order> orderList = new ArrayList<Order>();
 		orderList.add(order);
 		orderList.add(order2);
 		orderList.add(order3);
 		Customer customer = new Customer(1, "Jollibee", orderList);
 		
 		assertEquals(new BigDecimal(8000),customer.checkCreditLimit());
 	}
 }
