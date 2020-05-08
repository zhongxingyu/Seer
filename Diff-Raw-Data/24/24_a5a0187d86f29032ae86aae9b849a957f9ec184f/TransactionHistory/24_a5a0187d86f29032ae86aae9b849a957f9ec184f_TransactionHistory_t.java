 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics;
 
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import edu.wpi.cs.wpisuitetng.janeway.config.ConfigManager;
 import edu.wpi.cs.wpisuitetng.modules.core.models.User;
 
 /**
  * History of transactions for a requirement
  * @author Kevin Kell 
  * 3/25/13
  * get work done
  */
 
 /**
  * @author Raphael
  *
  */
 public class TransactionHistory {
 	
 	private LinkedList<Transaction> history;
 	private long TimeStamp;
 	
 	/**
 	 * Constructs an empty transaction history
 	 */
 	public TransactionHistory()
 	{
 		this.history = new LinkedList<Transaction>();
 	}
 	
 	/**
 	 * use this function to get a list iterator that you can use to cycle through the elements of the list
 	 * @param index the index of the list that you want the iterator to start on
 	 * @return the iterator containing all the elements of the list
 	 */
 	public ListIterator<Transaction> getIterator(int index){
 		return this.history.listIterator(index);
 	}
 	
 	/**
 	 * getter for the linked list of transaction history
 	 * @return the linked list of transactions
 	 */
 	public LinkedList<Transaction> getHistory(){
 		return this.history;
 	}
 	
 	/**
 	 * allows you to add to the records of transactions
 	 * always adds to the new transaction to the end of the list
 	 * @param msg the message of the transaction to be added
 	 * @return the transaction that was just added to the history
 	 */
 	
 	public Transaction add(String msg){
 		long time = this.TimeStamp;
 		String user = ConfigManager.getConfig().getUserName();
 		Transaction newTransaction = new Transaction(user, time, msg);
 		history.add(newTransaction);
 		return newTransaction;
 	}
 	
 	/**
 	 * allows you to get the item at the given index in the list
 	 * @param index the index at which the desired Transaction resides
 	 * @return the Transaction at the index given by the parameter
 	 */
 	public Transaction getItem(int index){
 		return this.history.get(index);
 	}
 	
 	/**
	 * getter for Transaction History time stamp
	 * @return TimeStamp
	 */
	public long getTimestamp() {
		return this.TimeStamp;
	}	
	
	/**
 	 * set the timestamp
 	 * @param ts
 	 */
 	public void setTimestamp (long ts) {
 		this.TimeStamp = ts;
 	}	
 }
