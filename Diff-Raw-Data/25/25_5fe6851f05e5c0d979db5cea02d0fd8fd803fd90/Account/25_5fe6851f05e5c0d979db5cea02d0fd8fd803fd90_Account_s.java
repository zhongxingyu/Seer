  /*
   * Copyright  2012 Mufasa developer unit
   *
   * This file is part of Mufasa Budget.
   *
   *	Mufasa Budget is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * Mufasa Budget is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with Mufasa Budget.  If not, see <http://www.gnu.org/licenses/>.
   */
 package it.chalmers.mufasa.android_budget_app.model;
 
 import it.chalmers.mufasa.android_budget_app.model.database.DataAccessor;
 import it.chalmers.mufasa.android_budget_app.model.database.DataAccessor.SortBy;
 import it.chalmers.mufasa.android_budget_app.model.database.DataAccessor.SortByOrder;
 import it.chalmers.mufasa.android_budget_app.settings.Constants;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 
 /**
  * A class that holds almost all data that user has stored in the app. This
  * class keeps the database updated This is a singleton class.
  * 
  * @author Slurpo
  */
 public class Account {
 
 	private int id;
 	private String name;
 	private double balance;
 	private List<Category> categoryList;
 	private List<Transaction> transactionList;
 	private List<BudgetItem> budgetItemList;
 	private DataAccessor dataAccessor;
 	// The number of transactions that is to be retrieved from the database.
 	private int nbrOfTransactions;
 
 	private static Account instance = null;
 
 	private PropertyChangeSupport pcs;
 
 	private Account(Context context) {
 		dataAccessor = new DataAccessor(context);
 
 		// If the database has an account the retrieves the data from that one.
 		if (dataAccessor.accountExists()) {
 			setId(dataAccessor.getAccountId());
 			setName(dataAccessor.getAccountName());
 			setBalance(dataAccessor.getAccountBalance());
 
 			// If it doesn't a new account is stored in the database.
 		} else {
 			setId(Constants.ACCOUNT_ID);
 			setName("account");
 			setBalance(0.0);
 			dataAccessor.addAccount(getName(), getBalance());
 		}
 		categoryList = new ArrayList<Category>();
 		transactionList = new ArrayList<Transaction>();
 		budgetItemList = new ArrayList<BudgetItem>();
 
 		pcs = new PropertyChangeSupport(this);
 
 	}
 
 	/**
 	 * Returns the only instance of this class.
 	 */
 	public static Account getInstance(Context context) {
 		if (instance == null) {
 			instance = new Account(context);
 		}
 		return instance;
 	}
 
 	private void setId(int id) {
 		this.id = id;
 		if (dataAccessor.accountExists()) {
 			dataAccessor.updateAccount(this);
 		}
 
 	}
 
 	private void setName(String name) {
 		this.name = name;
 		if (dataAccessor.accountExists()) {
 			dataAccessor.updateAccount(this);
 		}
 	}
 
 	/**
 	 * Sets balance to the given amount.
 	 */
 	public void setBalance(double balance) {
 		this.balance = balance;
 		if (dataAccessor.accountExists()) {
 			dataAccessor.updateAccount(this);
 		}
 	}
 
 	/**
 	 * Returns current balance.
 	 */
 	public double getBalance() {
 		return this.balance;
 	}
 
 	/**
 	 * Returns the name of the account.
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Returns the id of the account.
 	 */
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * Returns the budget item that the user have saved.
 	 */
 	public List<BudgetItem> getBudgetItems() {
 		return getBudgetItems(null);
 	}
 	
 	public List<BudgetItem> getBudgetItems(Category parent) {
 		updateBudgetItemList(parent);
 		return budgetItemList;
 	}
 
 	private void updateBudgetItemList(Category parent) {
 		budgetItemList.clear();
 		budgetItemList.addAll(dataAccessor.getBudgetItems(parent));
 	}
 
 	/**
 	 * Stores a budget item in the list of budget items.
 	 */
 	public void addBudgetItem(Category category, Double value) {
 		dataAccessor.addBudgetItem(category, value);
 		pcs.firePropertyChange("BudgetItems Updated", null, null);
 	}
 
 	/**
 	 * Removes the given budget item from the list of budget items.
 	 */
 	public void removeBudgetItem(BudgetItem budgetItem) {
 		dataAccessor.removeBudgetItem(budgetItem);
 		pcs.firePropertyChange("BudgetItems Updated", null, null);
 	}
 	
 	/**
 	 * Returns a single saved category.
 	 */
 	public Category getCategory(int id) {
 		return dataAccessor.getCategory(id);
 	}
 
 	/**
 	 * Returns the categories that the user have saved.
 	 */
 	public List<Category> getCategories() {
 		updateCategoryList();
 		return categoryList;
 	}
 
 	private void updateCategoryList() {
 		categoryList.clear();
 		categoryList.addAll(dataAccessor.getCategories());
 		pcs.firePropertyChange("Categories Updated", null, null);
 
 	}
 
 	/**
 	 * Adds a category to the list of categories.
 	 */
 	public void addCategory(String name, Category parent) {
 		dataAccessor.addCategory(name, parent);
 		updateCategoryList();
 	}
 
 	/**
 	 * Renames a category in the list of categories.
 	 */
 	public void editCategory(Category category, String newName) {
 		dataAccessor.editCategory(category, newName);
 		updateCategoryList();
 	}
 	
 	
 	/**
 	 * Removes the given category from the list of categories.
 	 */
 	public void removeCategory(Category category) {
 		dataAccessor.removeCategory(category);
 		updateCategoryList();
 	}
 
 	/**
 	 * Returns the transactions that the users have saved.
 	 */
 	public List<Transaction> getTransactions(int nbrOfTransactions) {
 		this.nbrOfTransactions = nbrOfTransactions;
 		updateTransactionList();
 		return transactionList;
 	}
 
 	private void updateTransactionList() {
 		transactionList.clear();
 		transactionList.addAll(dataAccessor.getTransactions(this, SortBy.DATE,
 				SortByOrder.DESC, 0, nbrOfTransactions));
 	}
 	
 
 	/**
 	 * Adds a transaction to the list of transactions.
 	 */
 	public void addTransaction(double amount, Date date, String name,
 			Category category) {
 		dataAccessor.addTransaction(amount, date, name, category);
 		updateBalance();
 		pcs.firePropertyChange("Transactions Updated", null, null);
 
 	}
 
 	/**
 	 * Removes the given transaction from the list of transactions.
 	 */
 	public void removeTransaction(Transaction transaction) {
 		dataAccessor.removeTransaction(transaction);
 		updateBalance();
 		pcs.firePropertyChange("Transactions Updated", null, null);
 	}
 
 	private void updateBalance() {
 		setBalance(dataAccessor.getAccountBalance());
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener l) {
 		pcs.addPropertyChangeListener(l);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener l) {
 		pcs.removePropertyChangeListener(l);
 	}
 
 	public List<Category> getCategories(Category currentParentCategory) {
 		categoryList = dataAccessor.getCategories(currentParentCategory);
 		return categoryList;
 	}
 
	



 }
