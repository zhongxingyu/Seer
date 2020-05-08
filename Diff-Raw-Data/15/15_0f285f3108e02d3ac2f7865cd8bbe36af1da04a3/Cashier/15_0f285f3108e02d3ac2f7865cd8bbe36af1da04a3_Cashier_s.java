 import java.util.*;
 
 
 public class Cashier {
 
 	public static final double POINTS_PER_PESO = 500;
 	private Store store;
 	private double cash;
 	private Transaction currentTransaction;
 	
 	public Cashier(Store store)
 	{
 		this.store = store;
 	}
 	
 	public void startDay()
 	{
 		this.cash = store.giveCashToCashier();
 	}
 	
 	public double getCash(){ return cash; }
 	public void startTransaction(){
 		currentTransaction = new Transaction();
 	}
 	public Iterator<Unit> unitsIterator(Item toSell){
 		return toSell.unitIterator();
 	}
 	public void sell(Unit tosell, int quantity){
 		currentTransaction.addUnitsSold(tosell, quantity);
 	}
 	
 	public Transaction endTransaction(Customer loyalBuyer, int pointsUsed)
 	{
 		Transaction toReturn = null;
 		double cashDue = 0;
		while(currentTransaction.unitsSoldIterator().hasNext())
 		{
			Map.Entry<Unit, Integer> entry = currentTransaction.unitsSoldIterator().next();
 			cashDue += entry.getKey().getUnitPrice() * entry.getValue();
 		}
 		if(loyalBuyer != null)
 		{
 			currentTransaction.setCustomer(loyalBuyer);
 			currentTransaction.setPointsUsed(pointsUsed);
 			loyalBuyer.addPointsRedeemed(pointsUsed);
 			cashDue -= pointsUsed;
 			loyalBuyer.addPointsEarned((int)(cashDue / POINTS_PER_PESO));
 			toReturn = currentTransaction;
 		}
 		currentTransaction.setRevenue(cashDue);
 		cash += cashDue;
 		currentTransaction = null;
 		return toReturn;
 	}
 	
 	public double endDay()
 	{
 		store.getCashFromCashier(cash);
 		return cash;
 	}
 }
