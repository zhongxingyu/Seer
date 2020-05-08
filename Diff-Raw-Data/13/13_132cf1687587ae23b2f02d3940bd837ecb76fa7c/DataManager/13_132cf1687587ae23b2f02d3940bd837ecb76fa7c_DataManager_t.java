 package edu.nyu.cs.adb;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.nyu.cs.adb.Response.Status;
 
 /**
  * The representation of a database site: contains a map for stored variable 
  * values as well as  lock data for its own site to see which transactions 
  * have a lock on each variable
  * @author dandelarosa
  */
 public final class DataManager {
 	//private final TransactionManager transactionManager;
 	private ArrayList <Response> responses = new ArrayList <Response>();
 	private final int siteID;
 	private int currentTime = 0;
 	private int lastRecoveryTime = 0;
 	private boolean isActive = true;
 	
 	private Message currentMessage = null;
 	
 	private Map<String, List<CommittedValue>> stableStorage 
 		= new HashMap<String, List<CommittedValue>>();
 	private Map<String, Integer> unstableStorage 
 		= new HashMap<String, Integer>();
 	
 	private Map<String, Integer> readOnlyTransactions 
 		= new HashMap<String, Integer>();
 	private Set<String> readWriteTransactions = new HashSet<String>();
 	
 	/**
 	 * Lock information
 	 * @author dandelarosa
 	 */
 	private static final class Lock {
 		private final String transactionID;
 		private final String variableID;
 		
 		/**
 		 * Constructor
 		 * @param transactionID
 		 * @param variableID
 		 */
 		private Lock (String transactionID, String variableID) {
 			this.transactionID = transactionID;
 			this.variableID = variableID;
 		}
 		
 		/**
 		 * Get the transaction ID
 		 * @return
 		 */
 		private String getTransactionID () {
 			return transactionID;
 		}
 		
 		/**
 		 * Get the variable ID
 		 * @return
 		 */
 		private String getVariableID () {
 			return variableID;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((transactionID == null) ? 0 : transactionID.hashCode());
 			result = prime * result
 					+ ((variableID == null) ? 0 : variableID.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Lock other = (Lock) obj;
 			if (transactionID == null) {
 				if (other.transactionID != null)
 					return false;
 			} else if (!transactionID.equals(other.transactionID))
 				return false;
 			if (variableID == null) {
 				if (other.variableID != null)
 					return false;
 			} else if (!variableID.equals(other.variableID))
 				return false;
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return "Lock on " + variableID + " granted to " + transactionID;
 		}
 	}
 	private Set<Lock> readLocks = new HashSet<Lock>();
 	
 	// For Write locks:
 	// Key is the VariableID
 	// Value is the TransactionID
 	// Use a Map since only one transaction can have a lock on a variable
 	// at a time
 	private Map<String, String> writeLocks = new HashMap<String, String>();
 
 	private Map<String, Map<String, Integer>> beforeImages 
 		= new HashMap<String, Map<String, Integer>>();	
 	/**
 	 * Constructor
 	 * Ideally we'd actually do initialization of variables via a builder
 	 * object, but we're doing it in here instead for the purposes of the
 	 * project.
 	 * @param siteID
 	 */
 	DataManager (int siteID) {
 		//this.transactionManager = transactionManager;
 		this.siteID = siteID;
 		
 		// Even indexed variables are at all sites
 		for (int i = 2; i <= 20; i += 2) {
 			String variableID = "x" + i;
 			int value = 10 * i;
 			stableStorage.put(
 					variableID, new LinkedList<CommittedValue>());
 			stableStorage.get(variableID)
 				.add(new CommittedValue(value, currentTime));
 			// Don't forget to initialize unstable storage too
 			unstableStorage.put(variableID, value);
 		}
 		
 		// Odd indexed variables are at one site each (1 + index mod 10)
 		for (int i = 1; i < 20; i +=2) {
 			if (siteID == 1 + (i % 10)) {
 				String variableID = "x" + i;
 				int value = 10 * i;
 				stableStorage.put(
 					variableID, new LinkedList<CommittedValue>());
 				stableStorage.get(variableID)
 					.add(new CommittedValue(value, currentTime));
 				// Don't forget to initialize unstable storage too
 				unstableStorage.put(variableID, value);
 			}
 		}
 	}
 	
 	/**
 	 * Committed value/timestamp pair
 	 * @author dandelarosa
 	 */
 	private static final class CommittedValue {
 		private final int value;
 		private final int timestamp;
 		
 		/**
 		 * Constructor
 		 * @param value
 		 * @param timestamp
 		 */
 		private CommittedValue (int value, int timestamp) {
 			this.value = value;
 			this.timestamp = timestamp;
 		}
 
 		/**
 		 * Gets the committed value
 		 * @return
 		 */
 		private int getValue () {
 			return value;
 		}
 
 		/**
 		 * Gets the time when the value was committed
 		 * @return
 		 */
 		private int getTimestamp () {
 			return timestamp;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + timestamp;
 			result = prime * result + value;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			CommittedValue other = (CommittedValue) obj;
 			if (timestamp != other.timestamp)
 				return false;
 			if (value != other.value)
 				return false;
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return "CommittedValue [value=" + value + ", timestamp="
 					+ timestamp + "]";
 		}
 	}
 	
 	/**
 	 * Sends a message to the Data Manager
 	 * @param message The message object
 	 */
 	void sendMessage (Message message) {
 		
 		responses.clear();
 		
 		if (isActive) {
 			currentMessage = message;
 		}
 	}
 	
 	/**
 	 * Processes the message in the message buffer. To be called at every 
 	 * timestep.
 	 */
 	ArrayList<Response> update () {
 		// Don't process any messages if the site has failed
 		if (!isActive) return responses;
 
 		// Don't process any messages if there isn't any
 		if (currentMessage == null) return null;
 		
 		for (Operation operation : currentMessage) {
 			switch (operation.getOperationID()) {
 				case ABORT:
 				{
 					String transactionID = operation.getTransactionID();
 					
 					// Check if the transaction is active
 					boolean isReadOnly = 
 						readOnlyTransactions.containsKey(transactionID);
 					boolean isReadWrite = 
 						readWriteTransactions.contains(transactionID);
 					if (!isReadOnly && !isReadWrite) {
 						throw new AssertionError(
 								transactionID + " not active");
 					}
 					
 					// Release read locks
 					Iterator<Lock> rit = readLocks.iterator();
                     while (rit.hasNext()) {
                     	Lock lock = rit.next();
                     	if (transactionID.equals(lock.getTransactionID())) {
                     		rit.remove();
                     	}
 					}
 					
 					// Release write locks
 					Set<String> variableIDs = 
 						new HashSet<String>(writeLocks.keySet());
 					for (String variableID : variableIDs) {
 						if (writeLocks.get(variableID).equals(transactionID)) {
 							writeLocks.remove(variableID);
 						}
 					}
 					
 					// Apply before image to the database
 					Map<String, Integer> beforeImage 
 						= beforeImages.get(transactionID);
 					variableIDs = beforeImage.keySet();
 					for (String variableID : variableIDs) {
 						unstableStorage.put(variableID, 
 								beforeImage.get(variableID));
 					}
 					
 					// Discard the before image
 					beforeImages.remove(transactionID);
 					
 					// Send success message back to the transaction manager
 					Response.Builder responseBuilder = 
 						new Response.Builder(siteID, Status.SUCCESS);
 					responseBuilder.setTransactionID(transactionID);
 					Response success = responseBuilder.build();
 					//transactionManager.sendResponse(success);
 					responses.add(success);
 					break;
 				}
 				case BEGIN:
 				{
 					String transactionID = operation.getTransactionID();
 					
 					// Throw an exception if the transaction already started
 					if (readWriteTransactions.contains(transactionID)) {
 						throw new IllegalStateException("Transaction " 
 								+ transactionID + " already started");
 					}
 					if (readOnlyTransactions.containsKey(transactionID)) {
 						throw new IllegalStateException("Transaction " 
 								+ transactionID + " already started");
 					}
 					
 					// Safely add transaction
 					readWriteTransactions.add(transactionID);
 
 					// Send success message back to the transaction manager
 					Response.Builder responseBuilder = 
 						new Response.Builder(siteID, Status.SUCCESS);
 					responseBuilder.setTransactionID(transactionID);
 					Response success = responseBuilder.build();
 					//transactionManager.sendResponse(success);
 					responses.add(success);
 					break;
 				}
 				case BEGIN_READONLY:
 				{
 					String transactionID = operation.getTransactionID();
 					
 					// Throw an exception if the transaction already started
 					if (readWriteTransactions.contains(transactionID)) {
 						throw new IllegalStateException("Transaction " 
 								+ transactionID + " already started");
 					}
 					if (readOnlyTransactions.containsKey(transactionID)) {
 						throw new IllegalStateException("Transaction " 
 								+ transactionID + " already started");
 					}
 					
 					// Safely add transaction
 					readOnlyTransactions.put(transactionID, currentTime);
 
 					// Send success message back to the transaction manager
 					Response.Builder responseBuilder = 
 						new Response.Builder(siteID, Status.SUCCESS);
 					responseBuilder.setTransactionID(transactionID);
 					Response success = responseBuilder.build();
 					//transactionManager.sendResponse(success);
 					responses.add(success);
 					break;
 				}
 				case FINISH:
 				{
 					String transactionID = operation.getTransactionID();
 					
 					// Check if the transaction is active
 					boolean isReadOnly = 
 						readOnlyTransactions.containsKey(transactionID);
 					boolean isReadWrite = 
 						readWriteTransactions.contains(transactionID);
 					if (!isReadOnly && !isReadWrite) {
 						throw new AssertionError(
 								transactionID + " not active");
 					}
 					
 					// Release read locks
 					Iterator<Lock> rit = readLocks.iterator();
                     while (rit.hasNext()) {
                     	Lock lock = rit.next();
                     	if (transactionID.equals(lock.getTransactionID())) {
                     		rit.remove();
                     	}
 					}
 					
 					// Release write locks, commit their values
 					Set<String> variableIDs = 
 						new HashSet<String>(writeLocks.keySet());
 					for (String variableID : variableIDs) {
 						if (writeLocks.get(variableID).equals(transactionID)) {
 							Integer value = unstableStorage.get(variableID);
 							CommittedValue commit = 
 								new CommittedValue(value, currentTime);
 							List<CommittedValue> history = 
 								stableStorage.get(variableID);
 							// Add new value to the head of the list
 							history.add(0, commit);
 							writeLocks.remove(variableID);
 						}
 					}
 					
 					// Discard the before image
 					beforeImages.remove(transactionID);
 					
 					// Send success message back to the transaction manager
 					Response.Builder responseBuilder = 
 						new Response.Builder(siteID, Status.SUCCESS);
 					responseBuilder.setTransactionID(transactionID);
 					Response success = responseBuilder.build();
 					//transactionManager.sendResponse(success);
 					responses.add(success);
 					break;
 				}
 				case READ:
 				{
 					String transactionID = operation.getTransactionID();
 					String variableID = operation.getVariableID();
 					
 					// Fail if variable is not written in unstable storage
 					if (!unstableStorage.containsKey(variableID)) {
 						// Create and send failure response
 						Response.Builder responseBuilder = 
 							new Response.Builder(siteID, Status.FAILURE);
 						responseBuilder.setTransactionID(transactionID);
 						Response failure = responseBuilder.build();
 						//transactionManager.sendResponse(failure);
 						// Don't do anything else with this operation
 						responses.add(failure);
 						break;
 					}
 					
 					// Check if the transaction is active
 					boolean isReadOnly = 
 						readOnlyTransactions.containsKey(transactionID);
 					boolean isReadWrite = 
 						readWriteTransactions.contains(transactionID);
 					System.out.println("Transaction debug: "+readOnlyTransactions+" \n"+readWriteTransactions);
 //					if (!isReadOnly && !isReadWrite) {
 //						throw new AssertionError(
 //								transactionID + " not active");
 //					}
 					
 					// If RO read from stable storage history
 					if (readOnlyTransactions.containsKey(transactionID)) {
 						List<CommittedValue> history = 
 							stableStorage.get(variableID);
 						for (CommittedValue committedValue : history) {
 							int startTime = 
 								readOnlyTransactions.get(transactionID);
 							if (committedValue.getTimestamp() <= startTime) {
 								// Create and send response
 								int value = committedValue.getValue();
 								Response.Builder builder = 
 									new Response.Builder(siteID, 
 										Status.SUCCESS);
 								builder.setTransactionID(transactionID);
 								builder.setReadValue(value);
 								Response success = builder.build();
 								//transactionManager.sendResponse(success);
 								// Ignore the remaining history
 								responses.add(success);
 								break;
 							}
 						}
 						// Don't do anything else for this transaction
 						break;
 					}
 					
 					// If RW ask for read lock
 					if (readWriteTransactions.contains(transactionID)) {
 						boolean isWriteLockOnVariable =
 							writeLocks.containsKey(variableID);
 						boolean doesTransactionHaveWriteLock = 
 							transactionID.equals(writeLocks.get(variableID));
 						if (!isWriteLockOnVariable || 
 								doesTransactionHaveWriteLock) {
 							// Lock the variable
 							Lock readLock = new Lock(transactionID, variableID);
 							readLocks.add(readLock);
 							// Send back the read value
 							Integer readValue = unstableStorage.get(variableID);
 							Response.Builder builder = 
 								new Response.Builder(siteID, Status.SUCCESS);
 							builder.setTransactionID(transactionID);
 							builder.setReadValue(readValue);
 							Response readSuccess = builder.build();
 							//transactionManager.sendResponse(readSuccess);
 							// Don't do anything else with this operation
 							responses.add(readSuccess);
 							break;
 						}
 						else {
 							// Tell the Transaction Manager that the 
 							// variable is locked
 							Response.Builder builder = 
 								new Response.Builder(siteID, Status.LOCKED);
 							builder.setTransactionID(transactionID);
 							Response locked = builder.build();
 							//transactionManager.sendResponse(locked);
 							// Don't do anything else with this operation
 							responses.add(locked);
 							break;
 						}
 					}
 					break;
 				}
 				case WRITE:
 				{
 					String transactionID = operation.getTransactionID();
 					String variableID = operation.getVariableID();
 					
 					boolean isReadLocked = false;
 					// See if there are any read locks
 					for (Lock lock : readLocks) {
						if (lock.getVariableID().equals(variableID) 
								&& !lock.getTransactionID()
									.equals(transactionID)) {
 							// Tell the Transaction Manager that the 
 							// variable is locked
 							Response.Builder builder = 
 								new Response.Builder(siteID, Status.LOCKED);
 							builder.setTransactionID(transactionID);
 							Response locked = builder.build();
 							//transactionManager.sendResponse(locked);
 							
 							isReadLocked = true;
 							responses.add(locked);
 							break;
 						}
 					}
 					// If locked, don't do anything else with this operation
 					if (isReadLocked) break;
 					
 					// Ask for write lock
 					boolean isWriteLockOnVariable =
 						writeLocks.containsKey(variableID);
 					boolean doesTransactionHaveWriteLock = 
 						transactionID.equals(writeLocks.get(variableID));
 					if (!isWriteLockOnVariable || 
 							doesTransactionHaveWriteLock) {
 						// Lock the variable
 						writeLocks.put(variableID, transactionID);
 						Integer writeValue = operation.getWriteValue();
 						
 						// If this is the first write, create before image
 						if (!beforeImages.containsKey(transactionID)) {
 							Map<String, Integer> beforeImage 
 								= new HashMap<String, Integer>();
 							Set<String> variableIDs = unstableStorage.keySet();
 							for (String variable : variableIDs) {
 								int value = unstableStorage.get(variable);
 								beforeImage.put(variable, value);
 							}
 							beforeImages.put(transactionID, beforeImage);
 						}
 						
 						// Write to the variable
 						unstableStorage.put(variableID, writeValue);
 						
 						// Send success message
 						Response.Builder builder = 
 							new Response.Builder(siteID, Status.SUCCESS);
 						builder.setTransactionID(transactionID);
 						Response success = builder.build();
 						//transactionManager.sendResponse(success);
 						
 						// Don't do anything else with this operation
 						responses.add(success);
 						break;
 					}
 					else {
 						// Tell the Transaction Manager that the 
 						// variable is locked
 						Response.Builder builder = 
 							new Response.Builder(siteID, Status.LOCKED);
 						builder.setTransactionID(transactionID);
 						Response locked = builder.build();
 						//transactionManager.sendResponse(locked);
 						
 						// Don't do anything else with this operation
 						responses.add(locked);
 						break;
 					}
 				}
 			}
 		}
 		currentMessage = null;
 		currentTime++;
 		return responses;
 	}
 	
 	/**
 	 * Tells the site to fail. The site loses all volatile data and lock 
 	 * information. The transaction manager won’t be able to send messages to 
 	 * this site until this site recovers.
 	 */
 	void fail () {
 		isActive = false;
 		System.out.println("FAILRE");
 		// Clear locks and unstable storage
 		readLocks.clear();
 		writeLocks.clear();
 		unstableStorage.clear();
 	}
 	
 	/**
 	 * Tells the site to recover
 	 */
 	void recover () {
 		isActive = true;
 		lastRecoveryTime = currentTime;
 	}
 	
 	/**
 	 * Gets the time when the site last recovered
 	 * @return
 	 */
 	int getLastRecoveryTime () {
 		return lastRecoveryTime;
 	}
 	
 	/**
 	 * Returns the committed values at this site.
 	 * @return A string listing the variables and their value
 	 */
 	String dump () {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Contents of site ");
 		sb.append(siteID);
 		sb.append(":\n");
 		for (int i = 1; i <= 20; i++) {
 			String variableID = "x" + i;
 			if (stableStorage.containsKey(variableID)) {
 				sb.append(variableID);
 				sb.append("=");
 				sb.append(stableStorage.get(variableID).get(0).getValue());
 				sb.append("\n");
 			}
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Returns the committed value for variable xj at this site.
 	 * @param xj The variable index
 	 * @return A string containing the value of the variable
 	 */
 	String dump (String variableID) {
 		if (stableStorage.containsKey(variableID)) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("Site ");
 			sb.append(siteID);
 			sb.append(": ");
 			sb.append(stableStorage.get(variableID).get(0).getValue());
 			sb.append("\n");
 			return sb.toString();
 		}
 		else return "";
 	}
 
 	@Override
 	public String toString() {
 		return "DataManager [siteID=" + siteID + "]";
 	}
 }
