 package edu.dhbw.oodb.dao;
 
 import java.util.List;
 
 import edu.dhbw.oodb.entity.Customer;
 import edu.dhbw.oodb.entity.Order;
 
 public interface CustomerDao  extends GenericDao<Customer> {
 
 	public Customer getCustomer(Long id);
 
 	public List<Customer> getAllCustomer();
 
 	public List<Customer> getAllCustomerAndOrdersDao();
 	
 	public List<Customer> getAllCustomerBatchFetchExists();
 	
 	public List<Customer> getAllCustomerBatchFetchJoin();
	
	public List<Order> getAllCustomersJoinFetch();
 
 }
