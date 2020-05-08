 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 public class Store {
 	int id;
 	HashMap<Item, Integer> inventory;
 	double totalCash;
 	double cashPerCashier;
 	ArrayList<Cashier> cashiers;
 	ArrayList<Transaction> transactions;
 	Delivery currDelivery;
 	
 	public Store(int id, double startingCash)
 	{
 		totalCash = startingCash;
 	}
 	
 	public void startDeliveryBatch()
 	{
 		currDelivery = new Delivery(this);
 	}
 	
 	public void acceptDeliveryItem(Item accepted, int quantity, double pricePerUnit)
 	{
 		if(currDelivery == null)
 		{
 			
 		}
 		else
 		{
 			if(inventory.containsKey(accepted))
 				inventory.put(accepted, inventory.get(accepted) + quantity);
 			else
 				inventory.put(accepted, quantity);
 				
 			currDelivery.addDeliveryItem(new DeliveryItem(accepted, quantity, pricePerUnit));
 		}
 	}
 	
 	public Delivery endDeliveryBatch()
 	{
 		Delivery toReturn = currDelivery;
 		currDelivery = null;
 		return toReturn;
 	}
 	
 	public void addCashier(Cashier toAdd)
 	{
 		cashiers.add(toAdd);
 	}
 	
 	public void removeCashier(Cashier toRemove)
 	{
 		cashiers.remove(toRemove);
 	}
 	
 	public Iterator<Transaction> transactionIterator()
 	{
 		return transactions.iterator();
 	}
 
 	public Cashier getCashier(int cashierIndex) {
 		return cashiers.get(cashierIndex);
 	}
 }
