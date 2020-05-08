 package it.chalmers.mufasa.android_budget_app.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.text.format.DateFormat;
 
 public class GraphViewModel {
 
 	Account account;
 	List<BudgetItem> budgetItems;
 
 	PropertyChangeSupport pcs;
 
 	Category currentMainCategory;
 
 	boolean editMode = false;
 
 	public GraphViewModel(Context context) {
 		this.pcs = new PropertyChangeSupport(this);
 		this.account = Account.getInstance(context);
 		this.currentMainCategory = account.getCategory(1);
 	}
 	
 	public List<Double> getAccountBalanceListForGraph(int number) {
 		
 		Date from = new Date(2012,10,1);
 		Date to = new Date(2012,10,31);
 		
 		return getAccountBalanceOverPeriod(from,to);
 	}
 
 	/**
 	 * Gets change of account balance each day over a specific time period. Handy for
 	 * graph views.
 	 * 
 	 * @param from
 	 *            from date
 	 * @param to
 	 *            to date
 	 * @return 
 	 */
 	public List<Double> getAccountBalanceOverPeriod(Date from, Date to) {
 
 		List<Double> accountBalances = new ArrayList<Double>();
 		
 //		Double currentBalance = account.getBalance();
 
 		//Gets array of Dates for each day between from and to
 		List<Date> dates = new ArrayList<Date>();
 		long interval = 24*1000 * 60 * 60; //1 day
 		long endTime = to.getTime() ;
 		long curTime = from.getTime();
 		while (curTime <= endTime) {
 		    dates.add(new Date(curTime));
 		    curTime += interval;
 		}
 
 		Double temp = 0.0;
 		for(Date day : dates) {
 			System.out.println("Get balance for day"+DateFormat.format("YY/MM/DD HH:MM", day));
			//TODO account.getBalanceAtDate(day);
			accountBalances.add(10000.0-temp);
			temp = temp + 100.0;
 		}
 		
 //		for (Date day : dates) { //Loop backwards since we only know the current balance.
 //			for (Transaction transaction : account.getTransactions(day,day,null)) {
 //				if(transaction.getCategory().getId() == 1) { //Income
 //					
 //				}
 //			}
 //		}
 		
 		return accountBalances;
 	}
 	
 	public Double getActualSpendingsInCategoryOverPeriod(Category category, Date from, Date to) {
 		Double result;
 		
 //TODO		
 //		for(Transaction transaction : account.getTransactions(from, to, category)) {
 //			result += transaction.getAmount();
 //		}
 		
 		return 2000.0;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 }
