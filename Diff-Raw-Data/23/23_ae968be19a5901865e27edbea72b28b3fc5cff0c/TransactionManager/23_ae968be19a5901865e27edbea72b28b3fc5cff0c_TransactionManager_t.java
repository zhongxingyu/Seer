 package serversrc.resInterface;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import serversrc.resImpl.InvalidTransactionException;
 import serversrc.resImpl.TransactionAbortedException;
 
 public interface TransactionManager extends Remote {
 	
 	/**
 	 * Informs transaction manager that a transaction is starting
 	 * @return The id associated with this transaction. 
 	 *   
 	 */
 	public int start() throws RemoteException;
 	
 	/**
 	 * Commits the transaction with transactionID
 	 * @param transactionID is the transaction to be committed
 	 * @return true if commit is a success. 
 	 * @throws RemoteException -rmi
 	 * @throws TransactionAbortedException 
 	 * @throws InvalidTransactionException 
 	 */
 	public boolean commit(int transactionID) throws RemoteException, InvalidTransactionException, TransactionAbortedException;
 	
 	
 	/**
 	 * Aborts the transaction with transactionID
 	 * @param transactionID
 	 * @throws RemoteException -rmi
 	 * @throws InvalidTransactionException 
 	 */
 	public void abort(int transactionID) throws RemoteException, InvalidTransactionException;
 	
 	/**
 	 * Adds RM into transactionID list of RMs. 
 	 * @param transactionID the id of the transaction
 	 * @param rm the resource manager being used. 
	 * @throws InvalidTransactionException 
 	 */
	public void enlist(int transactionID, RMType rm) throws InvalidTransactionException;
 	
 	public boolean shutdown() throws RemoteException;
 
	public void lives(int id) throws InvalidTransactionException;
 }
