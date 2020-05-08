 package oving1;
 
 import java.util.ArrayList;
 
 public class ServingArea {
 	private static int capacity = SushiBar.capacity;
 	private static ArrayList<Customer> customers = new ArrayList<Customer>();
 	private static int orders = 0;
 	private static int eatenOrders = 0;
 	private static int takeAwayOrders = 0;
 	private static int id = 1;
 	
 	public ServingArea(int capacity){
 		this.capacity = capacity;
 	}
 	
 	public static Boolean isSpace(){
 		return customers.size() < capacity;
 	}
 	
 	public synchronized static Boolean getBool(){
 		return customers.size() < capacity;
 	}
 	
 	public synchronized static void handleCustomer(Customer customer, Boolean bool){
 		if (customers.size() < capacity && bool && customer.getId()==id){
 			id++;
 			customers.add(customer);
 			orders += customer.getOrders();
 			eatenOrders += customer.getEatenOrders();
 			takeAwayOrders += customer.getEatenOrders();
 			System.out.println("Before Add: "+customer.getId()+id);
 			System.out.println("Add: " + ServingArea.customers.size()+ " id "+customer.getId());
 		}
 		else if(!bool){
 			customers.remove(customer);
 			System.out.println("Remove: " + ServingArea.customers.size() + " id "+customer.getId());
 			Customer newCustomer = SushiBar.door.handleCustomer(null, false);
			new Thread(newCustomer).notify();
 		}
 		else{
 			System.out.println("Else "+customer.getId());
 			customer.waitMe();
 		}
 	}
 }
