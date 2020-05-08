 package TransactionManager;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.transaction.InvalidTransactionException;
 
 import LockManager.DeadlockException;
 import LockManager.LockManager;
 import LockManager.TrxnObj;
 import ResImpl.Customer;
 import ResImpl.MiddlewareImpl;
 import ResImpl.RMHashtable;
 import ResInterface.ResourceManager;
 
 public class Operation {
 
 	private ResourceManager rm;
 	private OP_CODE operation;
 	private HashMap<String, Object> arguments;
 	private LockManager lm;
 	private int transaction_id;
 	private static int operation_count = 0;
 	private final int OP_ID;
 	private ArrayList<String> keys;
 	
 	public Operation(int id, ResourceManager r, OP_CODE op, HashMap<String,Object> args, LockManager l)
 	{
 		transaction_id = id;
 		rm = r;
 		operation = op;
 		arguments = args;
 		lm = l;
 		OP_ID = operation_count;
 		operation_count++;
 		keys = new ArrayList<String>();
 	}
 	
 	public Operation(int id, ResourceManager r, OP_CODE op, HashMap<String,Object> args, LockManager l, int specified_id)
 	{
 		transaction_id = id;
 		rm = r;
 		operation = op;
 		arguments = args;
 		lm = l;
 		OP_ID = specified_id;
 		keys = new ArrayList<String>();
 	}
 	
 	/**
 	 * Method used to execute functions that return a boolean value. The result of these
 	 * executions are not persistent until they are committed.
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public boolean execute()
 	{
 		try 
 		{
 			boolean to_return = false;
 			
 			switch(operation)
 			{
 				case ADD_FLIGHT:
 				
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));				
 					//call rm to create a non-committed RMItem
 					return rm.addFlight(OP_ID, (Integer)arguments.get("flightNum"), (Integer)arguments.get("flightSeats"), (Integer)arguments.get("flightPrice"));
 
 				case ADD_CARS:
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					//call rm to create a non-committed RMItem
 					return rm.addCars(OP_ID, (String)arguments.get("location"), (Integer)arguments.get("numCars"), (Integer)arguments.get("price"));
 						
 				case ADD_ROOMS:
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					//call rm to create a non-committed RMItem
 					return rm.addRooms(OP_ID, (String)arguments.get("location"), (Integer)arguments.get("numRooms"), (Integer)arguments.get("price"));
 				
 				case NEW_CUSTOMER_ID:
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					//call rm to create a non-committed RMItem
 					if ((rm.newCustomerExecute(OP_ID, (Integer)arguments.get("cid")))!=-1)
 					{
 						return true;
 					}
 					else
 					{
 						return false;
 					}
 					
 				case DELETE_FLIGHT:
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					//call rm to delete flight (not in a persistent way until committed
 					return rm.deleteFlight(OP_ID, (Integer)arguments.get("flightNum"));
 
 				case DELETE_CARS:
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					//call rm to delete car (not in a persisitent wayuntil committed)
 					return rm.deleteCars(OP_ID, (String)arguments.get("location"));
 					
 				case DELETE_ROOMS:
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					//call rm to delete car (not in a persisitent wayuntil committed)
 					return rm.deleteRooms(OP_ID, (String)arguments.get("location"));
 					
 				case DELETE_CUSTOMER:
 					
 					//acquire write lock for customer, as well as for all the reservations they have
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("key"));
 
 					Customer cust = (Customer) arguments.get("customer_object");
 					RMHashtable reserved_items = cust.getReservations();
 
 					//obtain locks for all the reserverations they have - if any of these fail, return false;
 					for (Enumeration e = reserved_items.keys(); e.hasMoreElements();) {        
 		                String reservedkey = (String) (e.nextElement());
 			
 						if(!(lm.Lock(transaction_id, reservedkey, TrxnObj.WRITE)))
 						{
 							return false;
 						}
 					}
 					
 				case RESERVE_FLIGHT:
 					
 					//acquire write lock for customer, as well as the flight
 					lm.Lock(transaction_id, (String) arguments.get("customer_key"), TrxnObj.WRITE);
 					lm.Lock(transaction_id, (String) arguments.get("flight_key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("customer_key"));
 					keys.add((String)arguments.get("flight_key"));
 
 					if (rm instanceof MiddlewareImpl)
 					{
 						to_return = ((MiddlewareImpl) rm).reserveFlightExecute(OP_ID, (Integer)arguments.get("cid"), (Integer)arguments.get("flightNum"));
 					}
 					return to_return;
 					
 				case RESERVE_CAR:
 					
 					//acquire write lock for customer, as well as the car
 					lm.Lock(transaction_id, (String) arguments.get("customer_key"), TrxnObj.WRITE);
 					lm.Lock(transaction_id, (String) arguments.get("car_key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("customer_key"));
 					keys.add((String)arguments.get("car_key"));
 
 					if (rm instanceof MiddlewareImpl)
 					{
 						to_return = ((MiddlewareImpl) rm).reserveCarExecute(OP_ID, (Integer)arguments.get("cid"), (String)arguments.get("location"));
 					}
 					return to_return;
 					
 				case RESERVE_ROOM:
 					
 					//acquire write lock for customer, as well as the room
 					lm.Lock(transaction_id, (String) arguments.get("customer_key"), TrxnObj.WRITE);
 					lm.Lock(transaction_id, (String) arguments.get("room_key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("customer_key"));
 					keys.add((String)arguments.get("room_key"));
 
 					if (rm instanceof MiddlewareImpl)
 					{
 						to_return = ((MiddlewareImpl) rm).reserveRoomExecute(OP_ID, (Integer)arguments.get("cid"), (String)arguments.get("location"));
 					}
 					return to_return;
 			
 				case ITINERARY:
 					
 					//acquire write lock for customer, as well as all the other things to be booked
 					lm.Lock(transaction_id, (String) arguments.get("customer_key"), TrxnObj.WRITE);
 					keys.add((String)arguments.get("customer_key"));
 
 					//if room is desired, lock room key
 					if ((Boolean)arguments.get("room_boolean"))
 					{
 						lm.Lock(transaction_id, (String) arguments.get("room_key"), TrxnObj.WRITE);
 						keys.add((String)arguments.get("room_key"));
 					}
 					//if car is desired, lock car key
 					if ((Boolean)arguments.get("car_boolean"))
 					{
 						lm.Lock(transaction_id, (String) arguments.get("car_key"), TrxnObj.WRITE);
 						keys.add((String)arguments.get("car_key"));
 					}
 					
 					//lock all flight keys
 					Vector flightNumbers = (Vector)arguments.get("flightNumbers");
 					Iterator it = flightNumbers.iterator();
 					while (it.hasNext())
 					{
 						String temp_key = (String)it.next();
 						lm.Lock(transaction_id, temp_key, TrxnObj.WRITE);
 					}
 					
 					if (rm instanceof MiddlewareImpl)
 		  			{
 						to_return = ((MiddlewareImpl) rm).itineraryExecute(OP_ID, (Integer)arguments.get("cid"), 
 																			(Vector)arguments.get("flightNumbers"),
 																			(String)arguments.get("location"),
 																			(Boolean)arguments.get("car_boolean"),
 																			(Boolean)arguments.get("car_boolean"));
 					}
 					return to_return;
 					
 				default:
 					
 					return false;
 			}
 		}
 		catch (DeadlockException e)
 		{
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	/**
 	 * Method used to execute functions that return a boolean value. The result of these
 	 * executions are not persistent until they are committed.
 	 * @return
 	 */
 	public int executeIntReturn()
 	{
 		try 
 		{
 			switch(operation)
 			{
 				case NEW_CUSTOMER:
 				
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.WRITE);
 					
 					//call rm to create a non-committed RMItem
 					return rm.newCustomerExecute(OP_ID, (Integer)arguments.get("cid"));
 					
 				case QUERY_FLIGHTS:
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.READ);
 					return rm.queryFlight(OP_ID, (Integer)arguments.get("flightNum"));
 					
 				case QUERY_CARS:
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.READ);
 					return rm.queryCars(OP_ID, (String) arguments.get("location"));
 					
 				case QUERY_ROOMS:
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.READ);
 					return rm.queryRooms(OP_ID, (String) arguments.get("location"));
 					
 				case QUERY_FLIGHT_PRICE:
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.READ);
 					return rm.queryFlightPrice(OP_ID, (Integer)arguments.get("flightNum"));
 					
 				case QUERY_CAR_PRICE:
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.READ);
 					return rm.queryCarsPrice(OP_ID, (String) arguments.get("location"));
 					
 				case QUERY_ROOM_PRICE:
 					lm.Lock(transaction_id, (String) arguments.get("key"), TrxnObj.READ);
 					return rm.queryRoomsPrice(OP_ID, (String) arguments.get("location"));					
 					
 				default:
 					
 					return -1;
 			}
 		}
 		catch (DeadlockException e)
 		{
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	/**
 	 * Method used to execute functions that return a boolean value. The result of these
 	 * executions are not persistent until they are committed.
 	 * @return
 	 */
 	public String executeStringReturn()
 	{
 		try 
 		{
 			switch(operation)
 			{
 				case QUERY_CUSTOMER_INFO:
 					
 					
 					//acquire write lock
 					lm.Lock(transaction_id, (String)arguments.get("key"), TrxnObj.READ);
 					if (rm instanceof MiddlewareImpl)
 					{
 						return ((MiddlewareImpl) rm).queryCustomerInfoExecute(OP_ID, (Integer) arguments.get("cid"));					
 					}
 					else
 					{
 						return null;
 					}
 
 				default:
 					
 					return null;
 			}
 		}
 		catch (DeadlockException e)
 		{
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Commits this operation
 	 * @return True if the operation was successfully committed; otherwise, returns false
 	 */
 	public boolean commit()
 	{
 		try {			
 			
 			boolean result;
 			if (rm instanceof MiddlewareImpl)
 			{
 				result = ((MiddlewareImpl) rm).commitOperation(OP_ID);
 			}
 			else
 			{
 				result = rm.commit(OP_ID);
 			}
 			return result;
 		
 		} catch (InvalidTransactionException e) {
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (TransactionAbortedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return false;
 	}
 	
 	/**
 	 * Aborts this operation by committing the undo operation
 	 */
 	public void abort()
 	{
 		try {
 			if (rm instanceof MiddlewareImpl)
 			{
 				((MiddlewareImpl) rm).abortOperation(OP_ID);
 			}
 			else
 			{
 				rm.abort(OP_ID);
 			}
 		} catch (InvalidTransactionException e) {
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (TransactionAbortedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Get OP_CODE for this operation
 	 * @return
 	 */
 	public OP_CODE getOPCODE()
 	{
 		return operation;
 	}
 	
 	/**
 	 * get key on which this operation depends
 	 * @return
 	 */
 	public ArrayList<String> getKeys()
 	{
 		return keys;
 	}
 	
 	/**
 	 * Get this operation's OP_ID
 	 * @return
 	 */
 	public int getOpID()
 	{
 		return OP_ID;
 	}
 	
 	/**
 	 * Two phase commit voting protocol. Each operation votes whether it wants to commit or not
 	 * @return right now always vote yes and return
 	 */
 	public Vote requestVoteFromRM()
 	{
		try {
			return rm.vote(this.OP_ID, this.operation);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
 	}
 }
