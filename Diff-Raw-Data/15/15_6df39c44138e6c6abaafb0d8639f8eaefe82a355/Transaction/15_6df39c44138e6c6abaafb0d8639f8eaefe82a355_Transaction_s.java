import java.security.Timestamp;
 import java.util.*;
 
 
 public class Transaction {
 	private Store store;
 	private Cashier cashier;
 	private HashMap<Unit, Integer> unitsSold;
 	private Timestamp dateTime;
 	private Customer customer;
 	private int pointsUsed;
 	private double revenue;
 	
	public Transaction()
	{
 		unitsSold = new HashMap<Unit, Integer>();
 	}
	
 	public Cashier getCashier(){ return cashier; }
 	public Iterator<Map.Entry<Unit, Integer>> unitsSoldIterator(){
 		return unitsSold.entrySet().iterator();
 	}
 	public double getRevenue(){
 		return revenue;
 	}
 	public void setRevenue(double revenue)
 	{
 		this.revenue = revenue;
 	}
 	
 	public int getPointsUsed()
 	{
 		return pointsUsed;
 	}
 	
 	public void setPointsUsed(int pointsUsed)
 	{
 		pointsUsed = pointsUsed;
 	}
 	public Timestamp getDateTime(){ return dateTime; }
 	public Customer getCustomer(){ return customer; }
 	public void setCustomer(Customer customer)
 	{
 		this.customer = customer;
 	}
 	public void addUnitsSold(Unit unit, int quantity){
 		unitsSold.put(unit, quantity);
 	}
 }
