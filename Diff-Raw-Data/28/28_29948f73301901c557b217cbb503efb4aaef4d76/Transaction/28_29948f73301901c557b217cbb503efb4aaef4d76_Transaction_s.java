 	package edu.nyu.cs.adb;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 import edu.nyu.cs.adb.Operation.Opcode;
 
 /**
  * A data structure defining a transaction. 
  * <br>Instance of Transaction will be created by the Transaction Manager 
  * <br>Each time the Transaction Manager will read from the Script ‘Begin Ti’, 
  * <br>it will create an instance of the transactions.
  * @author dandelarosa
  */
 public final class Transaction {
 	
 	/**
 	 * A transaction is said IDLE, when it starts but has no operation on going
 	 * After a transaction is asked to read or write, a transaction becomes active
 	 */
 	public enum Status{
 		IDLE,
 		ACTIVE, 
 		WAIT,
 		ABORTED, 
 		END
 	}
 	
 	
 	private ArrayList <Operation> operations = new ArrayList <Operation> ();
 	private HashMap <String, ArrayList <Integer>> variableMap; 
 	private String transactionID; 
 	private int operationIndex;  
 	private int age; 
 	private int timeout = 0; 
 	final int TIMEOUT_DELAY = 30;
 	private Status status; 
 	private ArrayList <Integer> sitesUp; 
 	private ArrayList <Integer> sitesConcerned = new ArrayList <Integer>(); 
 	
 	//Variables use to know what lock the transaction is holding 
 	//and waiting for 
 	private Set <Lock> locksHold = new HashSet <Lock>();
 	private Set <Lock> locksWait = new HashSet <Lock>();
 	
 	//Variables used in sendResponse
 	private boolean isFailed = false;
 	private boolean isLocked = false;
 	private boolean isEndedSuccessfully = false;
 	private boolean isResponse = false;
 	
 	//variables used when needs to abort a Transaction: 
 	boolean isNotAborted = true; 
 	
 	private boolean isTransactionOver = false;
 	
 	/**
 	 * @param variableMap
 	 * @param transactionID
 	 * @param sitesUp can only send a message to the sites that were up when the 
 	 * Transaction instance was created
 	 */
 	Transaction (HashMap <String, ArrayList <Integer>> variableMap, String transactionID, ArrayList <Integer> sitesUp, int age) {
 
 		this.variableMap = variableMap; 
 		this.transactionID = transactionID; 
 		this.sitesUp = (ArrayList<Integer>) sitesUp.clone(); 
 		//sitesConcerned = (ArrayList<Integer>) this.sitesUp.clone();
 		operationIndex = -1; 
 		status = Status.IDLE; 
 		this.age = age;
 	}
 
 	
 	/**
 	 * @param operation Operation
 	 */
 	void addOperations (Operation operation) {
 	
 		//Operation types can be W, R, End
 		//Prevent adding operation to a Transaction that ended
 		//Status is ACTIVE or IDLE
 		if (status != Status.END && status != Status.ABORTED){
 			
 			operations.add(operation);
 			
 			//if this is the first operation added: 
 			//Init the pointer operationIndex
 			if (operations.size() == 1)
 				operationIndex = 0; 
 						
 			//ON R or W: 
 			if (operation.getOperationID() == Operation.Opcode.READ || operation.getOperationID() == Operation.Opcode.WRITE){
 				status = Status.ACTIVE;
 			}
 			
 			//Nothing on FINISH operation
 		}
 		//On abort: 
 		else if (status == Status.ABORTED && isNotAborted){
 			operations.clear();
 			Operation.Builder builder = new Operation.Builder(Opcode.ABORT);
 			builder.setTransactionID(transactionID);
 			Operation abort = builder.build();
 			operations.add(abort);
 			operationIndex = 0;
 			
 			//release all locks: 
 			locksHold.clear();
 			locksWait.clear();
 		}
 		
 		//On end, no operation can be added
 	}
 	
 	/**
 	 * 1. If response is success, increment the counter
 	 * 2. If response is lock, wait, keep the operation for the next cycle 
 	 * 3. If response is failure, set status to aborted, no more operation from that transaction can be sent
 	 * @param response
 	 */
 	void sendResponse(Response response){
 		
 		isResponse = true;
 		
 		if (response.getStatus() == Response.Status.LOCKED){
 			isLocked = true; 
 		}
 		
 		else if (response.getStatus() == Response.Status.FAILURE){
 			isFailed = true; 
 		}
 		
 		else if (response.getStatus() == Response.Status.SUCCESS && status == Status.END){
 			isEndedSuccessfully = true;
 		}
 		
 		else if (response.getStatus() == Response.Status.SUCCESS && status == Status.ABORTED){
 			isEndedSuccessfully = true;
 		}
 	}
 	
 	/**
 	 * This function is called by the TM at the end of each cycle. 
 	 * It re-init some variables
 	 */
 	void reinit(){
 				
 		//Operation ended succesfully: 
 		 if (isEndedSuccessfully && !isLocked && !isFailed && isResponse){
 			isTransactionOver = true;	
 		}
 		
 		//Operation was a success: 
 		 else if (!isFailed && !isLocked && isResponse && !isEndedSuccessfully){
 			//re-init the timeout
 			 timeout = 0;
 			//acquire locks
 			if (operationIndex > 0 && (operations.get(operationIndex).getOperationID() == Operation.Opcode.READ ||
 					operations.get(operationIndex).getOperationID() == Operation.Opcode.WRITE)){
 				 String lockType = operations.get(operationIndex).getOperationID().toString();
 				 String variableID = operations.get(operationIndex).getVariableID();
 				 locksHold.add(new Lock(variableID, lockType));
 			}
 			operationIndex++; 
 			locksWait.clear();
 			if (status == Status.WAIT)
 				status = Status.ACTIVE;
 		}
 		
 		//Operation could not succeed because of a lock
 		else if (isLocked && !isFailed && isResponse){
 			timeout++;
 			
 			status = Status.WAIT; 
 			
 			//Add a lock to locksWait
 			boolean isLock = false; 
 			String variableID = operations.get(operationIndex).getVariableID();
 			String lockType = operations.get(operationIndex).getOperationID().toString();
 			Lock lock = new Lock (variableID, lockType); 
 			for (Lock l: locksWait){
 				if (l.equals(lock)){
 					isLock = true;
 				}
 			}
 			if (!isLock)
 				locksWait.add(new Lock (variableID, lockType));
 		}
 		
 		//Operation failed: 
 		//Release all locks
 		else if (isFailed && isResponse){
 			System.out.println("operation failed flag");
 			status = Status.ABORTED;
 			locksWait.clear();
 			locksHold.clear();
 			timeout = 0;
 		}
 		
 		//No operation was sent and operation is not over: 
 		else if (!isFailed && !isLocked && !isResponse && !isEndedSuccessfully){
 			timeout++;
 			//if the status is idle, init the operation index
 			if (timeout>TIMEOUT_DELAY && status == Status.IDLE)
 				operationIndex = 0;
 		}
 		
 		
 		//re-init the variables: 
 		isFailed = false; 
 		isLocked = false; 
 		isEndedSuccessfully = false;
 		isResponse = false;		
 	}
 	
 	/**
 	 * 1. If the site that failed hold a variable with Write Lock
 	 * Then the transaction becomes incorrect
 	 * 2. If the site that failed hold a unique variable used by the Transaction
 	 * @param siteID
 	 */
 	void siteFailure (int siteID) {
 		
 		//Only check for operations that were already executed
 		//So all operations before operationIndex
 		
 		// if T is Active or Wait: 
 		if (status == Status.ACTIVE || status == Status.WAIT){
 			for (int i = 0; i<operationIndex; i++){
 				//Check if site that failed was used previously 
 				if (operations.get(i).getOperationID() != Operation.Opcode.BEGIN && operations.get(i).getSiteID().contains(siteID)){
 					//If var was updated on any kind of site
 					if (operations.get(i).getOperationID() == Opcode.WRITE){
 						status = Status.ABORTED; 
 					}
 					String variableID = operations.get(i).getVariableID(); 
 					int var = Integer.parseInt(variableID.substring(1));
 					//If var was read or write on non-replicated site
 					if (var %2 == 1){ 
 						//if var was just read, then lock disapear
 						if (operations.get(i).getOperationID() == Opcode.READ){
 							Lock lock = new Lock (variableID, "READ");
 							for (Lock l: locksHold){
 								if (lock.equals(l))
 									locksHold.remove(l);				
 							}
 						}
 					}
 				}
 			}			
 		}
 		
 		//2. Update the sitesUP: 
 		sitesUp.remove(sitesUp.indexOf(siteID));
 	}
 	
 	/**
 	 * Case 1. If no operations ever wrote, then site is added to the list
 	 * @param siteID
 	 */
 	void siteRecover(int siteID){
 		
 		boolean isWriteOperations = false; 
 		//iter on all operations before the operation index: 
 		for (int i=0; i<operationIndex; i++){
 			//Check if there exist a WRITE operation: 
 			if (operations.get(i).getOperationID() == Operation.Opcode.WRITE){
 				isWriteOperations = true;
 			}
 		}
 		
 		//then add the site to the list of the sites up: 
 		if (!isWriteOperations){
 			sitesUp.add(siteID);
 			Collections.sort(sitesUp);
 		}
 	}
 	
 	/**
 	 * Function called by TM at the end of each cycle. 
 	 * Return the operation to be executed next
 	 * @return operation   
 	 */
 	Operation getnextOperation() {	
 		
 		if (operations.get(operationIndex).getOperationID() == Operation.Opcode.FINISH){
 			status = Status.END;
 			//release all locks: 
 			locksHold.clear();
 			locksWait.clear();
 		}
 		return operations.get(operationIndex);
 	}
 
 	
 	//GETTER: 
 	public ArrayList <Integer> getSitesConcerned(){
 		//if no operation was added, return null
 		//No sites are concerned by the operation
 		//Then no site will ask for the next operation
 		if (operationIndex >= operations.size()){
 			return new ArrayList <Integer>();
 		}
 		
 		//All sites are concerned
 		else if (status == Status.IDLE){
 			return sitesUp;
 		}
 		
 		//Aborted status: 
 		else if (status == Status.ABORTED && isNotAborted){
 			isNotAborted = false;
 			return sitesUp;
 		}
 		else if  (status == Status.END){
 			return sitesUp;
 		}
 		
 		//Transaction is currently Active: 
 		else{
 			if (operations.get(operationIndex).getOperationID() == Operation.Opcode.FINISH){
 				return sitesUp;
 			}
 			//See if variable is replicated or not
 			Integer variableID = Integer.parseInt(operations.get(operationIndex).getVariableID().substring(1));
 			if (variableID %2 != 0){
 				//make sure that the sitesConcerned are up: 
 				sitesConcerned = (variableMap.get(operations.get(operationIndex).getVariableID()));
 				int siteID = sitesConcerned.get(0);
 				if (sitesUp.contains(siteID))
 					return sitesConcerned;
 				//Site for commit is down
 				else{
 					status = Status.WAIT;
 					return new ArrayList <Integer>();
 				}
 			}
 
 			else{
 				return sitesUp;
 			}
 		}
 	}
 	
 	public String getTransactionID(){
 		return transactionID; 
 	}
 
 	public ArrayList <Integer> getSitesUp(){
 		return sitesUp; 
 	}
 	
 	public int getOperationIndex(){
 		return operationIndex; 
 	}
 	
 	public Status getStatus(){
 		return status;
 	}
 	
 	public int getAge(){
 		return age;
 	}
 
 	public Set <Lock> getLocksHold(){
 		return locksHold;
 	}
 	
 	public Set <Lock> getLocksWait(){
 		return locksWait;
 	}
 	
 	//TO BE REMOVED
 	public void addLocksHold(Lock lock){
 		locksHold.add(lock);
 	}
 
 	//TO BE REMOVED
 	public void addLocksWait(Lock lock){
 		locksWait.add(lock);
 	}
 	
 	//TO BE REMOVED
 	public void setStatus(Status status){
 		this.status = status;
 	}
 	
 	public int getTimeout(){
 		return timeout;
 	}
 	
 	public boolean getIsTransactionOver(){
 		return isTransactionOver;
 	}
 	
 	@Override
 	public String toString()
 	{
 		
 		String s =  "Transaction ID: "+transactionID+"\nStatus "+status+"\nFollowing Operations:\n";
 		for (Operation operation: operations){
 			s+=operation+"\n";
 		}
 		s+="lock hold "+locksHold;
 		s+="\nlock wait "+locksWait;
 		s+="\nSites Up: "+sitesUp+"\n";
 		return s;
 	}
 
 	
 	
 }
