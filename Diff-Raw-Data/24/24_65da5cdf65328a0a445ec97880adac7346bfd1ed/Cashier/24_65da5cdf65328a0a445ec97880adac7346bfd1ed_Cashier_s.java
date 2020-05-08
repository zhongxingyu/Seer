 package system;
 
 import system.dao.CashierDao;
import system.dao.StoreDao;
 import system.dao.TransactionItemDao;
 
 public class Cashier 
 {
 	public static final double POINTS_PER_PESO = 500;
 	
 	private int index;
 	private Store store;
 	private double cash;
 	private TransactionE currentTransaction;
 	private boolean online;
 	private double rawCashDue;
 
 	public Cashier() { }
 	
 	public Cashier(Store store, int index)
 	{
 		this.store = store;
 		this.index = index;
 		online = false;
 	}
 
 	public int getIndex() { return index; }
 	public Store getStore() { return store; }
 	public double getCash(){ return cash; }
 	public boolean getOnline() { return online; }
 	public double getRawCashDue() { return rawCashDue; }
 	
 	public void setIndex(int index) { this.index = index; }
 	public void setStore(Store store) { this.store = store; }
 	public void setCash(double cash) { this.cash = cash; }
 	public void setOnline(boolean online) { this.online = online; }
 
 	
 	public void startDay()
 	{
 		this.cash = store.giveCashToCashier();
 		online = true;
 		CashierDao cashdao = new CashierDao();
 		cashdao.save(this);
 	}
 	
 	public TransactionE getCurrentTransaction()
 	{
 		return currentTransaction;
 	}
 	
 	public void startTransaction()
 	{
 		if(!online)
 		{
 			throw new IllegalStateException("Cashier is not online.");
 		}
 		currentTransaction = new TransactionE(store, this);
 		rawCashDue = 0;
 	}
 	
 	public void sell(Item currentItem, int quantity)
 	{
 		if(!online)
 		{
 			throw new IllegalStateException("Cashier is not online.");
 		}
 		if(currentTransaction == null)
 		{
 			throw new IllegalStateException("No active transaction.");
 		}
 		currentTransaction.addItemSold(currentItem, quantity, currentItem.getUnitPrice());
 		rawCashDue += quantity * currentItem.getUnitPrice();
 	}
 	
 	public TransactionE endTransaction(Customer loyalBuyer, int pointsUsed)
 	{
 		if(!online)
 		{
 			throw new IllegalStateException("Cashier is not online.");
 		}
 		if(currentTransaction == null)
 		{
 			throw new IllegalStateException("No active transaction.");
 		}
 		if(loyalBuyer != null && loyalBuyer.getUsablePoints() < pointsUsed)
 		{
 			throw new IllegalArgumentException("Customer does not have enough points to use.");
 		}
 		if(currentTransaction.getItemsSold().size() == 0)
 		{
 			throw new IllegalStateException("No items to sell!");
 		}
 		double cashDue = rawCashDue;
 		
 		TransactionItemDao transitemdao = new TransactionItemDao();
 		for(TransactionItem entry : currentTransaction.getItemsSold())
 		{
 			transitemdao.save(entry);
 			store.deductFromStock(entry.getItem(), entry.getQuantity());
 		}
 		if(loyalBuyer != null)
 		{
 			currentTransaction.setCustomer(loyalBuyer);
 			currentTransaction.setPointsUsed(pointsUsed);
 			loyalBuyer.addPointsRedeemed(pointsUsed);
 			cashDue -= pointsUsed;
 			loyalBuyer.addPointsEarned((int)(cashDue / POINTS_PER_PESO));
 		}
 		currentTransaction.setRevenue(cashDue);
 		cash += cashDue;
 		TransactionE toReturn = currentTransaction;
 		currentTransaction = null;
 		rawCashDue = 0;
 		CashierDao cashdao = new CashierDao();
 		cashdao.save(this);
 		return toReturn;
 	}
 	
 	public double endDay()
 	{
 		CashierDao cashdao = new CashierDao();
 		Cashier cas = cashdao.get(this.getIndex());
 		System.out.println(this.getIndex());
 		if(!cas.getOnline())
 		{
 			throw new IllegalStateException("Cashier is not online.");
 		}
 		cas.setOnline(false);
 		double toReturn = cash;
 		store.addCash(cash);
 		cash = 0;
 
 		
 		cashdao.save(this);
 		
 		return toReturn;
 	}
 
 	
 }
