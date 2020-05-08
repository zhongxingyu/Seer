import java.sql.Timestamp;
 import java.util.*;
 
 
 public class Transaction {
 	private Store store;
 	private Cashier cashier;
 	private HashMap<Unit, Integer> unitsSold;
 	private Timestamp dateTime;
 	private Customer customer;
 	
	public Transaction(){
		dateTime = new Timestamp(System.currentTimeMillis());
		unitsSold = new HashMap<Unit, Integer>();
	}
 	public Cashier getCashier(){ return cashier; }
 	public Iterator<Map.Entry<Unit, Integer>> unitsSoldIterator(){
 		return unitsSold.entrySet().iterator();
 	}
 	public double getRevenue(){
 		double revenue = 0;
 		for(Map.Entry<Unit, Integer> entry : unitsSold.entrySet())
 		{
 			revenue += entry.getKey().getUnitPrice() * entry.getValue();
 		}
 		return revenue;
 	}
 	public Timestamp getDateTime(){ return dateTime; }
 	public Customer getCustomer(){ return customer; }
 	public void addUnitsSold(Unit unit, int quantity){
 		unitsSold.put(unit, quantity);
 	}
 }
