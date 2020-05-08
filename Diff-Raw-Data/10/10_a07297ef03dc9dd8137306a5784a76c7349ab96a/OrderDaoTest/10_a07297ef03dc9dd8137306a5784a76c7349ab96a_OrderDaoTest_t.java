 package edu.dhbw.oodb.dao.test;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import edu.dhbw.oodb.dao.OrderDao;
 import edu.dhbw.oodb.entity.Order;
 
 public class OrderDaoTest extends AbstractDaoTests {
 
 	private OrderDao orderDao;
 
 	public void setOrderDao(OrderDao orderDao) {
 		this.orderDao = orderDao;
 	}
 
 	@Test
 	public void testFindById() {
 		Order order = orderDao.findById(1L);
 		assertTrue(order != null);
 	}
 
 	@Test
 	public void testGetOrder() {
 		Order order = orderDao.getOrder(new Long(1));
 		assertTrue(order != null);
 	}
 
 	@Test
 	public void testFindAll() {
 		List<Order> orders = orderDao.findAll();
		assertTrue(orders.size() == 1500000);
		orders = null;
	}

	@Test
	public void testGetAllOrders() {
		List<Order> orders = orderDao.getAllOrders();
		assertTrue(orders.size() == 1500000);
		orders = null;
 	}
 }
