 package restaurant;
 
 import agent.Agent;
 import java.util.*;
 
 
 /** Host agent for restaurant.
  *  Keeps a list of all the waiters and tables.
  *  Assigns new customers to waiters for seating and 
  *  keeps a list of waiting customers.
  *  Interacts with customers and waiters.
  */
 public class HostAgent extends Agent {
 
 	/** Private class storing all the information for each table,
 	 * including table number and state. */
 	private class Table {
 		public int tableNum;
 		public boolean occupied;
 
 		/** Constructor for table class.
 		 * @param num identification number
 		 */
 		public Table(int num){
 			tableNum = num;
 			occupied = false;
 		}	
 	}
 	
 	private class MyCustomer{
 		public CustomerAgent customer;
 		public CustomerStatus state;
 		
 		public MyCustomer(CustomerAgent cust) {
 			customer = cust;
 			state = CustomerStatus.waiting;
 		}
 	}
 	
 	enum CustomerStatus {waiting};
 
 	/** Private class to hold waiter information and state */
 	private class MyWaiter {
 		public WaiterAgent wtr;
 		public WaiterState state;
 
 		/** Constructor for MyWaiter class
 		 * @param waiter
 		 */
 		public MyWaiter(WaiterAgent waiter){
 			wtr = waiter;
 			state = WaiterState.working;
 		}
 	}
 
 	enum WaiterState {working, breakrequested, breakapproved, onbreak}; //A change from the boolean because I don't want to take a waiter off break before they actually get on break because they're busy waiting customers
 	//Although it's not in the requirements, but I want to be a nice employer
 
 
 	//List of all the customers that need a table
 	private List<CustomerAgent> waitList =
 			Collections.synchronizedList(new ArrayList<CustomerAgent>());
 
 	//List of all waiter that exist.
 	private List<MyWaiter> waiters =
 			Collections.synchronizedList(new ArrayList<MyWaiter>());
 	private int nextWaiter =0; //The next waiter that needs a customer
 
 	//List of all the tables
 	int nTables;
 	private Table tables[];
 
 	//Name of the host
 	private String name;
 
 	/** Constructor for HostAgent class 
 	 * @param name name of the host */
 	public HostAgent(String name, int ntables) {
 		super();
 		this.nTables = ntables;
 		tables = new Table[nTables];
 
 		for(int i=0; i < nTables; i++){
 			tables[i] = new Table(i);
 		}
 		this.name = name;
 	}
 
 	// *** MESSAGES ***
 
 	/** Customer sends this message to be added to the wait list 
 	 * @param customer customer that wants to be added */
 	public void msgIWantToEat(CustomerAgent customer){
 		print(customer + " wants to eat");
 		waitList.add(customer);
 		stateChanged();
 	}
 
 	/** Waiter sends this message after the customer has left the table 
 	 * @param tableNum table identification number */
 	public void msgTableIsFree(int tableNum){
 		print(tableNum + " is now free");
 		tables[tableNum].occupied = false;
 		stateChanged();
 	}
 
 	/** Waiter requests a break
 	 * @param waiter the waiter requesting a break
 	 */
 	public void msgIWantABreak(WaiterAgent waiter) {
 		print(waiter + " wants a break");
 		for (MyWaiter w:waiters) {
 			if (w.wtr.equals(waiter)) {
 				w.state = WaiterState.breakrequested;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Starts the waiter's break
 	 * @param waiter the waiter on break
 	 */
 	public void ImOnBreakNow(WaiterAgent waiter) {
 		print(waiter + " is now on break");
 		for (MyWaiter w:waiters) {
 			if (w.wtr.equals(waiter)) {
 				w.state = WaiterState.onbreak;
 				stateChanged();
 				return;
 			}
 		}
 	}
 	
 	public void ImOffBreak(WaiterAgent waiter) {
 		print(waiter + " is now off break");
 		for (MyWaiter w:waiters) {
 			if (w.wtr.equals(waiter)) {
 				w.state = WaiterState.working;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** The customer can't wait and leaves the restaurant
 	 * @param customer The customer that leaves
 	 */
 	public void msgIHateWaiting(CustomerAgent customer) {
 		print(customer + " hates waiting");
 		//TODO: Synchronized error here
 		waitList.remove(customer);
 		stateChanged();
 	}
 
 	/** Scheduler.  Determine what action is called for, and do it. */
 	protected boolean pickAndExecuteAnAction() {
 
 		if(!waitList.isEmpty() && !waiters.isEmpty()){
 			synchronized(waiters){
 				//Finds the next waiter that is working
 				while(!(waiters.get(nextWaiter).state == WaiterState.working)){
 					nextWaiter = (nextWaiter+1)%waiters.size();
 				}
 			}
 			print("picking waiter number:"+nextWaiter);
 			//Then runs through the tables and finds the first unoccupied 
 			//table and tells the waiter to sit the first customer at that table
 
 			synchronized(waitList){
 				for(int i=0; i < nTables; i++){
 					if(!tables[i].occupied){
 						tellWaiterToSitCustomerAtTable(waiters.get(nextWaiter), waitList.get(0), i);
 						return true;
 					}
 				}
 				//At this point, no tables are open, message customer that he'll have to wait
 				//Will do this more elegantly in a bit
 				//Manage customers in waiting status
 				//waitList.get(0).msgYoullHaveToWait();
 				//return true;
 			}
 		}
 
 		for (MyWaiter w:waiters) {
 			if (w.state == WaiterState.breakrequested) {
 				DoManageBreak(w);
 				return true;
 			}
 		}
 
 		for (MyWaiter w:waiters) {
 			if (w.state == WaiterState.onbreak) {
				if (Math.random() <= 0.3) {
 					w.wtr.msgBreakTimesOver(); //A hack to ensure they get back to work at some time
					w.state = WaiterState.working;
				}
 			}
 		}
 
 		//we have tried all our rules (in this case only one) and found
 		//nothing to do. So return false to main loop of abstract agent
 		//and wait.
 		return false;
 	}
 
 	// *** ACTIONS ***
 
 	/** Assigns a customer to a specified waiter and 
 	 * tells that waiter which table to sit them at.
 	 * @param waiter
 	 * @param customer
 	 * @param tableNum */
 	private void tellWaiterToSitCustomerAtTable(MyWaiter waiter, CustomerAgent customer, int tableNum){
 		print("Telling " + waiter.wtr + " to sit " + customer +" at table "+(tableNum+1));
 		waiter.wtr.msgSitCustomerAtTable(customer, tableNum);
 		tables[tableNum].occupied = true;
 		waitList.remove(customer);
 		nextWaiter = (nextWaiter+1)%waiters.size();
 		stateChanged();
 	}
 
 	private void DoManageBreak(MyWaiter waiter) {
 		print("Managing break of " + waiter.wtr);
 		for (MyWaiter w:waiters) {
 			if (w != waiter && w.state == WaiterState.working) {
 				waiter.wtr.msgBreakApproved();
 				waiter.state = WaiterState.breakapproved;
 				return;
 			}
 		}
 		waiter.wtr.msgBreakDenied();
 		waiter.state = WaiterState.working;
 		stateChanged();
 	}
 
 
 	// *** EXTRA ***
 
 	/** Returns the name of the host 
 	 * @return name of host */
 	public String getName(){
 		return name;
 	}    
 
 	/** Hack to enable the host to know of all possible waiters 
 	 * @param waiter new waiter to be added to list
 	 */
 	public void setWaiter(WaiterAgent waiter){
 		waiters.add(new MyWaiter(waiter));
 		stateChanged();
 	}
 
 	//Gautam Nayak - Gui calls this when table is created in animation
 	public void addTable() {
 		nTables++;
 		Table[] tempTables = new Table[nTables];
 		for(int i=0; i < nTables - 1; i++){
 			tempTables[i] = tables[i];
 		}  		  			
 		tempTables[nTables - 1] = new Table(nTables - 1);
 		tables = tempTables;
 	}
 }
