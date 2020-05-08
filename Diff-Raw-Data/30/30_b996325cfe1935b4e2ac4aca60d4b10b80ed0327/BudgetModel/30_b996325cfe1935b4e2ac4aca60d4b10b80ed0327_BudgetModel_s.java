 package budgetapp.models;
 
 /**
  * The Model for MrCashManager, controls the database and all logic and notifies Observers when changed
  */
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.Context;
 import budgetapp.util.BudgetBackup;
 import budgetapp.util.BudgetConfig;
 import budgetapp.util.BudgetEntry;
 import budgetapp.util.CategoryEntry;
 import budgetapp.util.IBudgetObserver;
 import budgetapp.util.DayEntry;
 import budgetapp.util.Money;
 import budgetapp.util.database.BudgetDataSource;
 import budgetapp.util.database.TransactionCommand;
 public class BudgetModel {
 	
 	private BudgetDataSource datasource;
 	
 	// The daily budget for the user
 	private Money dailyBudget;
 	
 	// List of transactions that have been done. Enables undo
 	private ArrayList<TransactionCommand> transactions;
 	
 	// The observers of this BudgetModel
 	private ArrayList<IBudgetObserver> observers;
 	
 	// Flag wether or not the model has been altered
 	private boolean stateChanged;
 	
 	// Class containing config values
 	private BudgetConfig config;
 	
 	// Class responsible for backups
 	private BudgetBackup backup;
 	
 	
 	public BudgetModel(Context context)
 	{
 		datasource = new BudgetDataSource(context);
 		config = new BudgetConfig(context);
 		Money.after = config.getBooleanValue(BudgetConfig.Fields.printCurrencyAfter);
 		Money.setCurrency(config.getStringValue(BudgetConfig.Fields.currency));
 		Money.setExchangeRate(config.getDoubleValue(BudgetConfig.Fields.exchangeRate));
 		dailyBudget = new Money(config.getDoubleValue(BudgetConfig.Fields.dailyBudget));
 		
 		backup = new BudgetBackup();
 		transactions = new ArrayList<TransactionCommand>();
 		observers = new ArrayList<IBudgetObserver>();
 		stateChanged = true;
 	}
 	
 	/**
 	 * Adds daily budget and adds a new transaction to the database and the transaction list.
 	 * Notifies observers.
 	 * @param entry - The BudgetEntry to create
 	 */
 	public void createTransaction(BudgetEntry entry)
 	{
 		addDailyBudget();
 		transactions.add(new TransactionCommand(datasource,entry));
 		transactions.get(transactions.size()-1).execute();
 		stateChanged = true;
 		notifyObservers();
 	}
 	
 	/**
 	 * Removes a transaction from the database.
 	 * Notifies observers.
 	 * @param entry
 	 */
 	public void removeTransaction(BudgetEntry entry)
 	{
 		datasource.removeTransactionEntry(entry);
 		stateChanged = true;
 		notifyObservers();
 	}
 	
 	/**
 	 * Edits a transaction in the database.
 	 * Notifies observers.
 	 * @param oldEntry - The entry to edit
 	 * @param newEntry - Entry with new information
 	 */
 	public void editTransaction(BudgetEntry oldEntry, BudgetEntry newEntry)
 	{
 		datasource.editTransactionEntry(oldEntry, newEntry);
 		stateChanged = true;
 		notifyObservers();
 	}
 	
 	/**
 	 * Unexecutes the latest TransactionCommand in the list.
 	 * Notifies observers.
 	 */
 	public void undoLatestTransaction()
 	{
 		if(transactions.size()>0){
 			transactions.get(transactions.size()-1).unexecute();
 			transactions.remove(transactions.size()-1);
 			stateChanged = true;
 			notifyObservers();
 		}
 	}
 	
 	/**
 	 * Gets the current available budget.
 	 * @return - The current budget
 	 */
 	public Money getCurrentBudget()
 	{
 		List<DayEntry> dayTotal = datasource.getSomeDaysTotal(1, BudgetDataSource.DESCENDING);
 		if(dayTotal.size()==1)
 		{
 			return new Money(dayTotal.get(0).getValue());
 		}
 		else
 			return new Money();
 	}
 	
 	/**
 	 * Gets the current daily budget.
 	 * @return - The models daily budget
 	 */
 	public Money getDailyBudget()
 	{
 		return dailyBudget;
 	}
 	
 	/**
 	 * Gets all category names
 	 * @return - A list of all category names
 	 */
 	public List<String> getCategoryNames()
 	{
 		return datasource.getCategoryNames();
 	}
 	
 	/**
 	 * Sets the daily budget and saves to config and config file.
 	 * Notifies observers.
 	 * @param budget
 	 */
 	public void setDailyBudget(Money budget)
 	{
 		dailyBudget = budget.multiply(Money.getExchangeRate());
 		config.writeValue(BudgetConfig.Fields.dailyBudget, dailyBudget.get());
 		config.saveToFile();
 		stateChanged = true;
 		notifyObservers();
 	}
 	
 	/**
 	 * Adds a category to the database.
 	 * Notifies observers.
 	 * @param category - The category name to add
 	 * @return - If the adding was successful
 	 */
 	public boolean addCategory(String category)
 	{
 		if(!category.equalsIgnoreCase(""))
 		{
 			boolean result = datasource.addCategory(category);
 			if(result == true)
 			{
 				stateChanged = true;
 				notifyObservers();
 				return true;
 			}
 			else
 				return false;
 		}
 		else
 			return false;
 	}
 	
 	/**
 	 * Removes a category name from the database.
 	 * Notifies observers.
 	 * @param category - The category to remove
 	 * @return - If the removal was successful
 	 */
 	public boolean removeCategory(String category)
 	{
 		boolean result = datasource.removeCategory(category);
 		if(result == true)
 		{
 			stateChanged = true;
 			notifyObservers();
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	
 	
 	public List<BudgetEntry> getSomeTransactions(int n, String orderBy)
 	{
 		return datasource.getSomeTransactions(n, orderBy);
 	}
 	
 	public List<DayEntry> getSomeDays(int n, String orderBy)
 	{
 		return datasource.getSomeDays(n, orderBy);
 	}
 	
 	public List<DayEntry> getSomeDaysTotal(int n, String orderBy)
 	{
 		return datasource.getSomeDaysTotal(n, orderBy);
 	}
 	
 	/**
 	 * Looks for last day with entries and adds the daily budget up to current date.
 	 * Returns number of daily budgets was added
 	 * @return - The number of times the daily budget was added
 	 */
 	public int addDailyBudget()
     {
     	List<DayEntry> lastDay = datasource.getSomeDays(1,BudgetDataSource.DESCENDING);
     	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		
     	int daysAdded = 0;
     	if(!lastDay.isEmpty())
     	{
     		SimpleDateFormat compareFormat = new SimpleDateFormat("yyyy/MM/dd");
     		
     		
 	    	String lastDayString = lastDay.get(0).getDate();
 	    	Calendar lastDayCalendar = Calendar.getInstance();
 	    	// Convert the string to a Calendar time. Subtract 1 from month because month 0 = January
 	    	// Set HH:mm to 00:00
 	    	lastDayCalendar.set(Integer.parseInt(lastDayString.substring(0, 4)),Integer.parseInt(lastDayString.substring(5, 7))-1,Integer.parseInt(lastDayString.substring(8, 10)),0,0);
 	    	
 	    	//System.out.println("Last day: " + dateFormat.format(lastDayCalendar.getTime()));
 	    	lastDayCalendar.add(Calendar.DAY_OF_MONTH, 1); // We want to start counting from the first day without transactions
 
 	    	// Step up to the day before tomorrow
 	    	Calendar nextDay = Calendar.getInstance();
 	    	nextDay.add(Calendar.DAY_OF_MONTH,1);
 	    	
 	    	//System.out.println("Next day: " + dateFormat.format(nextDay.getTime()));
 	    	Calendar tempDate = (Calendar)lastDayCalendar.clone();
 	    	
 	    	while(tempDate.before(nextDay))
 	    	{
 	    		if(!compareFormat.format(tempDate.getTime()).equalsIgnoreCase(compareFormat.format(nextDay.getTime())))
 	    		{
 	    			//System.out.println("Day to add: " + dateFormat.format(tempDate.getTime()));
 	    			BudgetEntry entry = new BudgetEntry(new Money(dailyBudget.divide(Money.getExchangeRate())), dateFormat.format(tempDate.getTime()),"Income");
 		        	datasource.createTransactionEntry(entry);
 		    		stateChanged = true;
 		    		daysAdded++;
 	    		}
 	    		tempDate.add(Calendar.DAY_OF_MONTH,1);	
 	    	}
 
     	}
     	else // Add a transaction of 0
     	{
     		Calendar tempDate = Calendar.getInstance();
     		
     		datasource.createTransactionEntry(new BudgetEntry(new Money(), dateFormat.format(tempDate.getTime()),"Income"));
     		stateChanged = true;
     		daysAdded = 1;
     	}
     	notifyObservers();
     	return daysAdded;
     }
 	
 	public List<CategoryEntry> getCategoriesSortedByNum() {
 		return datasource.getCategoriesSortedByNum();
 	}
 	public List<CategoryEntry> getCategoriesSortedByValue() {
 		return datasource.getCategoriesSortedByValue();
 	}
 	
 	public void saveConfig()
 	{
 		config.writeValue(BudgetConfig.Fields.currency,Money.getCurrency());
 		config.writeValue(BudgetConfig.Fields.printCurrencyAfter, Money.after);
 		config.writeValue(BudgetConfig.Fields.exchangeRate, Money.getExchangeRate());
 		config.saveToFile();
 	}
 	
 	public void saveBackup(String filename)
 	{
 		ArrayList<BudgetEntry> entries = (ArrayList<BudgetEntry>) getSomeTransactions(0,"desc");
 		backup.writeBackupFile(entries, filename);
 	}
 	
 	/**
 	 * Reads a backup file and merges it with the current database if successful
 	 * @param filename - Name of file to read
 	 */
 	public boolean readAndMergeBackup(String filename)
 	{
 		ArrayList<BudgetEntry> backupList = backup.readBackupFile(filename);
 		
		if(backupList.isEmpty())
 		{
 			return false;
 		}
 		// Save current entries
 		List<BudgetEntry> currentList = new ArrayList<BudgetEntry>();
		 currentList = datasource.getAllTransactions("asc");
 		
 		// Merge the two lists, sorting them 
 		currentList.addAll(backupList);
 		
 		// Sort the list
 		for(int i = 0; i < currentList.size() - 1; i++)
 		{
 			for(int j = i+1; j < currentList.size();j++)
 			{
 				if(currentList.get(i).getDate().compareTo(currentList.get(j).getDate())>0)
 				{	
 					BudgetEntry temp = new BudgetEntry(currentList.get(i));
 					currentList.set(i, new BudgetEntry(currentList.get(j)));
 					currentList.set(j, new BudgetEntry(temp));
 				}
 			}
 		}
 		
 		// Reset all the transaction tables
 		datasource.resetTransactionTables();
 		
 		for(int i = 0; i < currentList.size(); i++)
 		{
			System.out.println(currentList.get(i));
 			datasource.createTransactionEntry(currentList.get(i));
 		}
 		
 		return true;
 	}
 	
 	
 	public void addObserver(IBudgetObserver observer)
 	{
 		observers.add(observer);
 	}
 	
 	private void notifyObservers()
 	{
 		if(stateChanged)
 		{
 			for(int i = 0; i < observers.size(); i++)
 			{
 				observers.get(i).update();
 			}
 			stateChanged = false;
 		}
 		
 	}
 
 }
