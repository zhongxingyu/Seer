 package com.core.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.core.Customer;
 import com.database.CustomerBookDB;
 
 import android.content.Context;
 /**
  * Access to the customer's database
  */
 public class CustomerBook {
 	/**
 	 * Object of the customer in the database 
 	 */
 	private CustomerBookDB db;
 
 	/**
 	 * Received id in integer form and then find the customer that have that id
 	 * @param id the identity number of the customer
 	 * @return customer's name which in the database
 	 */
 	public Customer getCustomerByID(int id) {
 		return db.findBy(id);
 	}
 
 	/**
 	 * Received the customer and then add new customer to the database
 	 * @param con the initial name of the customer
 	 * @param customer is the customer info
 	 * @return customer is the customer
 	 */
 	public Customer addCutomer(Context con,Customer customer) {
 		db = new CustomerBookDB(con);
 		customer = db.insert(customer);
 		db.close();
 		return customer;
 	}
 
 	/**
 	 * Pull all the customer detail from the database
 	 * @param con is the context which receive from the user
 	 * @return i is the list of the customer
 	 */
 	public List<Customer> getAllCustomer(Context con) {
 		db = new CustomerBookDB(con);
 		List<Customer> i = db.findAll();
 		db.close();
 		return i;
 	}
 
 	/**
 	 * remove the exist customer from the database
 	 * @param con is the context that come from the user
 	 * @param customer is the customer that will be delete
 	 * @return true 
 	 */
 	public boolean remove(Context con,Customer customer) {
 		db = new CustomerBookDB(con);
 		db.deleteByID(customer.getID());
 		db.close();
 		return true;
 	}
 
 	/**
 	 * find that the customer is exist in the database or not
 	 * @param con is the context from the user
 	 * @param customer is the customer that will be searching for
 	 * @return true if there is the customer in database
 	 */
 	public boolean isContains(Context con,Customer customer) {
 		db = new CustomerBookDB(con);
 		Customer c = db.findBy(customer.getID());
 		db.close();
 		return c != null;
 	}
 	
 	/**
 	 * get the the number of the customer is the data base
	 * @param con is the context that receive from the user
 	 * @return the number of all customer in the database
 	 */
 	public int getAllCustomerQuantity(Context con){
 		return getAllCustomer(con).size();
 	}
 
 }
