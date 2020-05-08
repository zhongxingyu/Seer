 package com.algo.webshop.common.domainimpl;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 
 import com.algo.webshop.common.domain.Order;
 
 public interface IOrder {
 	public Order getOrderById(int id);
 
 	public Order getOrderByNumber(String number);
 
 	public Set<Order> getOrderUser(int users_id);
 
 	public List<Order> getOrders(int confirmStatus, int canselStatus);
 	
 	public List<Order> getOrdersList(int confirmStatus, int canselStatus, Calendar date);
 
 	public void addOrder(Order order);
 
 	public void updateOrder(Order order);
 
 	public String getLastOrderNumber();
 
 	public int getOrderIdByNumber(String number);
	
	public List<Order> getOrdersByUserId(int user_id);
 
 }
