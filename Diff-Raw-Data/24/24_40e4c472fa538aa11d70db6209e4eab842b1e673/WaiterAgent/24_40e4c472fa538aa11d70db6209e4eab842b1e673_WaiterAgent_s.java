 package restaurant;
 import java.awt.Color;
 import restaurant.gui.RestaurantGui;
 import restaurant.layoutGUI.*;
 import agent.Agent;
 import java.util.Timer;
 import java.util.TimerTask;
 import astar.*;
 import java.util.*;
 
 /** Restaurant Waiter Agent.
  * Sits customers at assigned tables and takes their orders.
  * Takes the orders to the cook and then returns them 
  * when the food is done.  Cleans up the tables after the customers leave.
  * Interacts with customers, host, and cook */
 public class WaiterAgent extends Agent {
 
 	//State variables for Waiter
 	private BreakState breakstate = BreakState.working;
 
	enum BreakState {working, breakRequested, breakPending, onBreak};
 
 	//State constants for Customers
 
 	public enum CustomerState 
 	{NEED_SEATED, READY_TO_ORDER, ORDER_PENDING, ORDER_READY, NEEDS_REORDER, IS_DONE, PAYING, BILL_ARRIVED, NO_ACTION};
 
 	Timer timer = new Timer();
 
 	/** Private class to hold information for each customer.
 	 * Contains a reference to the customer, his choice, 
 	 * table number, and state */
 	private class MyCustomer {
 		public CustomerState state;
 		public CustomerAgent cmr;
 		public String choice;
 		public int tableNum;
 		public Menu menu;
 		public Food food; //gui thing
 		double bill;
 
 		/** Constructor for MyCustomer class.
 		 * @param cmr reference to customer
 		 * @param num assigned table number */
 		public MyCustomer(CustomerAgent cmr, int num){
 			this.cmr = cmr;
 			tableNum = num;
 			state = CustomerState.NO_ACTION;
 			bill = 0;
 		}
 	}
 
 	//Name of waiter
 	private String name;
 	double tipMoney = 0;
 
 	//All the customers that this waiter is serving
 	private List<MyCustomer> customers = new ArrayList<MyCustomer>();
 
 	private HostAgent host;
 	private CookAgent cook;
 	private CashierAgent cashier;
 
 	//Animation Variables
 	AStarTraversal aStar;
 	Restaurant restaurant; //the gui layout
 	GuiWaiter guiWaiter; 
 	Position currentPosition; 
 	Position originalPosition;
 	Table[] tables; //the gui tables
 
 
 	/** Constructor for WaiterAgent class
 	 * @param name name of waiter
 	 * @param gui reference to the gui */
 	public WaiterAgent(String name, AStarTraversal aStar,
 			Restaurant restaurant, Table[] tables) {
 		super();
 
 		this.name = name;
 
 		//initialize all the animation objects
 		this.aStar = aStar;
 		this.restaurant = restaurant;//the layout for astar
 		guiWaiter = new GuiWaiter(name.substring(0,2), new Color(255, 0, 0), restaurant);
 		currentPosition = new Position(guiWaiter.getX(), guiWaiter.getY());
 		currentPosition.moveInto(aStar.getGrid());
 		originalPosition = currentPosition;//save this for moving into
 		this.tables = tables;
 	} 
 
 	// *** MESSAGES ***
 
 	/** Host sends this to give the waiter a new customer.
 	 * @param customer customer who needs seated.
 	 * @param tableNum identification number for table */
 	public void msgSitCustomerAtTable(CustomerAgent customer, int tableNum){
 		MyCustomer c = new MyCustomer(customer, tableNum);
 		c.state = CustomerState.NEED_SEATED;
 		customers.add(c);
 		stateChanged();
 	}
 
 	/** Customer sends this when they are ready.
 	 * @param customer customer who is ready to order.
 	 */
 	public void msgImReadyToOrder(CustomerAgent customer){
 		//print("received msgImReadyToOrder from:"+customer);
 		for(int i=0; i < customers.size(); i++){
 			//if(customers.get(i).cmr.equals(customer)){
 			if (customers.get(i).cmr == customer){
 				customers.get(i).state = CustomerState.READY_TO_ORDER;
 				stateChanged();
 				return;
 			}
 		}
 		System.out.println("msgImReadyToOrder in WaiterAgent, didn't find him?");
 	}
 
 	/** Customer sends this when they have decided what they want to eat 
 	 * @param customer customer who has decided their choice
 	 * @param choice the food item that the customer chose */
 	public void msgHereIsMyChoice(CustomerAgent customer, String choice){
 		for(MyCustomer c:customers){
 			if(c.cmr.equals(customer)){
 				c.choice = choice;
 				c.state = CustomerState.ORDER_PENDING;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Cook sends this when the order is ready.
 	 * @param tableNum identification number of table whose food is ready
 	 * @param f is the guiFood object */
 	public void msgOrderIsReady(int tableNum, Food f){
 		for(MyCustomer c:customers){
 			if(c.tableNum == tableNum){
 				c.state = CustomerState.ORDER_READY;
 				c.food = f; //so that later we can remove it from the table.
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Customer sends this when they are done eating.
 	 * @param customer customer who is done eating. */
 	public void msgDoneEating(CustomerAgent customer){
 		print(customer + " finished eating");
 		for(MyCustomer c:customers){
 			if(c.cmr.equals(customer)){
 				c.state = CustomerState.PAYING;
 				print("I found the customer");
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Customer sends this when he is ready to leave
 	 * @param customer The customer that is leaving
 	 * @param tip The tip for the waiter
 	 */
 	public void msgLeaving(CustomerAgent customer, double tip) { //I LIKE TIPS
 		for (MyCustomer c:customers) {
 			if (c.cmr.equals(customer)) {
 				c.state = CustomerState.IS_DONE;
 				tipMoney += tip;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Waiter receives this from the Cashier with the bill for the customer
 	 * @param customer the customer with the bill
 	 * @param bill the bill
 	 */
 	public void msgHereIsBill(CustomerAgent customer, double bill) {
 		for (MyCustomer c:customers) {
 			if (c.cmr.equals(customer)) {
 				c.state = CustomerState.BILL_ARRIVED;
 				c.bill = bill;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** The same as setBreakStatus essentially
 	 */
 	public void goOnBreak() {
 		breakstate = BreakState.breakRequested;
 		stateChanged();
 	}
 
 	/** Sent from GUI to control breaks 
 	 * @param state true when the waiter should go on break and 
 	 *              false when the waiter should go off break
 	 *              Is the name onBreak right? What should it be?*/
 	public void setBreakStatus(boolean state){
 		if (breakstate != BreakState.working && !state) {
 			host.ImOffBreak(this);
 		}
 		breakstate = state ? BreakState.breakRequested : BreakState.working;
 		stateChanged();
 	}
 
 	/** Allows the waiter to go on break - from host
 	 */
 	public void msgBreakApproved() {
 		breakstate = BreakState.onBreak;
 		stateChanged();
 	}
 
 	/** Disallows the waiter to go on break - from host
 	 */
 	public void msgBreakDenied() {
 		breakstate = BreakState.working;
 		stateChanged();
 	}
 
 	/** Cashier sends this to alert the waiter that the customer left without paying money
 	 * @param customer The customer that left
 	 */
 	public void msgCustomerPaidWithBody(CustomerAgent customer) {
 		for (MyCustomer c:customers) {
 			if (c.cmr.equals(customer)) {
 				c.state = CustomerState.IS_DONE;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Cook sends this when inventory is out of stock
 	 * @param tableNum the table of the customer whose order is out of stock
 	 */
 	public void msgOutOfStock(int tableNum) {
 		for (MyCustomer c:customers) {
 			if (c.tableNum == tableNum) {
 				c.state = CustomerState.NEEDS_REORDER;
 				stateChanged();
 				return;
 			}
 		}
 	}
 
 	/** Host sends this to the waiter to end his break
 	 */
 	public void msgBreakTimesOver() {
 		breakstate = BreakState.working;
 		stateChanged();
 	}
 
 	/** Scheduler.  Determine what action is called for, and do it. */
 	protected boolean pickAndExecuteAnAction() {
 		//print("in waiter scheduler");
 
 		if (breakstate == BreakState.breakRequested) {
 			doRequestBreak();
 			return true;
 		}
 		//Runs through the customers for each rule, so 
 		//the waiter doesn't serve only one customer at a time
 		if(!customers.isEmpty()){
 			//System.out.println("in scheduler, customers not empty:");
 			//Tells a customer his selection is out
 			for (MyCustomer c:customers) {
 				if (c.state == CustomerState.NEEDS_REORDER) {
 					doRequestReorder(c);
 					return true;
 				}
 			}
 			//Gives food to customer if the order is ready
 			for(MyCustomer c:customers){
 				if(c.state == CustomerState.ORDER_READY) {
 					giveFoodToCustomer(c);
 					return true;
 				}
 			}
 			//Clears the table if the customer has left
 			for(MyCustomer c:customers){
 				if(c.state == CustomerState.IS_DONE) {
 					clearTable(c);
 					return true;
 				}
 			}
 
 			//Seats the customer if they need it
 			for(MyCustomer c:customers){
 				if(c.state == CustomerState.NEED_SEATED){
 					seatCustomer(c);
 					return true;
 				}
 			}
 
 			//Gives all pending orders to the cook
 			for(MyCustomer c:customers){
 				if(c.state == CustomerState.ORDER_PENDING){
 					giveOrderToCook(c);
 					return true;
 				}
 			}
 
 			//Takes new orders for customers that are ready
 			for(MyCustomer c:customers){
 				//print("testing for ready to order"+c.state);
 				if(c.state == CustomerState.READY_TO_ORDER) {
 					takeOrder(c);
 					return true;
 				}
 			}	 
 
 			//Gets bills for customers that are done eating
 			for(MyCustomer c:customers) {
 				if (c.state == CustomerState.PAYING) {
 					getBill(c);
 					return true;
 				}
 			}
 
 			//Gives bill to customer
 			for(MyCustomer c:customers) {
 				if (c.state == CustomerState.BILL_ARRIVED) {
 					giveBill(c);
 					return true;
 				}
 			}
 		}
 		else if (breakstate == BreakState.onBreak) {
 			doGoOnBreak();
 			return true;
 		}
 		if (!currentPosition.equals(originalPosition)) {
 			DoMoveToOriginalPosition();//Animation thing
 			return true;
 		}
 
 		//we have tried all our rules and found nothing to do. 
 		// So return false to main loop of abstract agent and wait.
 		//print("in scheduler, no rules matched:");
 		return false;
 	}
 
 	// *** ACTIONS ***
 
 
 	/** Seats the customer at a specific table 
 	 * @param customer customer that needs seated */
 	private void seatCustomer(MyCustomer customer) {
 		DoSeatCustomer(customer); //animation	
 		customer.state = CustomerState.NO_ACTION;
 		customer.cmr.msgFollowMeToTable(this, new Menu());
 		customer.menu = new Menu();
 		stateChanged();
 	}
 	/** Takes down the customers order 
 	 * @param customer customer that is ready to order */
 	private void takeOrder(MyCustomer customer) {
 		DoTakeOrder(customer); //animation
 		customer.state = CustomerState.NO_ACTION;
 		customer.cmr.msgWhatWouldYouLike();
 		stateChanged();
 	}
 
 	/** Gives any pending orders to the cook 
 	 * @param customer customer that needs food cooked */
 	private void giveOrderToCook(MyCustomer customer) {
 		//In our animation the waiter does not move to the cook in
 		//order to give him an order. We assume some sort of electronic
 		//method implemented as our message to the cook. So there is no
 		//animation analog, and hence no DoXXX routine is needed.
 		print("Giving " + customer.cmr + "'s choice of " + customer.choice + " to cook");
 
 
 		customer.state = CustomerState.NO_ACTION;
 		cook.msgHereIsAnOrder(this, customer.tableNum, customer.choice);
 		stateChanged();
 
 		//Here's a little animation hack. We put the first two
 		//character of the food name affixed with a ? on the table.
 		//Simply let's us see what was ordered.
 		tables[customer.tableNum].takeOrder(customer.choice.substring(0,2)+"?");
 		restaurant.placeFood(tables[customer.tableNum].foodX(),
 				tables[customer.tableNum].foodY(),
 				new Color(255, 255, 255), customer.choice.substring(0,2)+"?");
 	}
 
 	/** Gives food to the customer 
 	 * @param customer customer whose food is ready */
 	private void giveFoodToCustomer(MyCustomer customer) {
 		DoGiveFoodToCustomer(customer);//Animation
 		customer.state = CustomerState.NO_ACTION;
 		customer.cmr.msgHereIsYourFood(customer.choice);
 		stateChanged();
 	}
 	/** Starts a timer to clear the table 
 	 * @param customer customer whose table needs cleared */
 	private void clearTable(MyCustomer customer) {
 		DoClearingTable(customer);
 		customer.state = CustomerState.NO_ACTION;
 		stateChanged();
 	}
 
 	/** Requests bill from cashier for customer
 	 * @param customer the customer that needs the bill
 	 */
 	private void getBill(MyCustomer customer) {
 		print("Getting the bill for " + customer.cmr);
 		cashier.msgCustomerDone(this, customer.cmr, customer.choice);
 		customer.state = CustomerState.NO_ACTION;
 		stateChanged();
 	}
 
 	/** Gives bill to customer
 	 * @param customer the customer that needs the bill
 	 */
 	private void giveBill(MyCustomer customer) {
 		customer.cmr.msgHereIsBill(cashier, customer.bill);
 		customer.state = CustomerState.NO_ACTION;
 	}
 	/** Requests a break from the host
 	 */
 	private void doRequestBreak() {
 		host.msgIWantABreak(this);
 		breakstate = BreakState.breakPending;
 		stateChanged();
 	}
 
 	/** Alerts host of breakiness
 	 */
 	private void doGoOnBreak() {
 		host.ImOnBreakNow(this);
 	}
 
 	/** Lets a customer know if they need to reorder again
 	 * @param customer the customer that needs to reorder
 	 */
 	private void doRequestReorder(MyCustomer customer) {
 		customer.menu.removeItem(customer.choice);
 		customer.cmr.msgOrderAgain(customer.menu);
 		customer.state = CustomerState.NO_ACTION;
 	}
 
 	// Animation Actions
 	void DoSeatCustomer (MyCustomer customer){
 		print("Seating " + customer.cmr + " at table " + (customer.tableNum+1));
 		//move to customer first.
 		GuiCustomer guiCustomer = customer.cmr.getGuiCustomer();
 		guiMoveFromCurrentPostionTo(new Position(guiCustomer.getX()+1,guiCustomer.getY()));
 		guiWaiter.pickUpCustomer(guiCustomer);
 		Position tablePos = new Position(tables[customer.tableNum].getX()-1,
 				tables[customer.tableNum].getY()+1);
 		guiMoveFromCurrentPostionTo(tablePos);
 		guiWaiter.seatCustomer(tables[customer.tableNum]);
 	}
 	void DoTakeOrder(MyCustomer customer){
 		print("Taking " + customer.cmr +"'s order.");
 		Position tablePos = new Position(tables[customer.tableNum].getX()-1,
 				tables[customer.tableNum].getY()+1);
 		guiMoveFromCurrentPostionTo(tablePos);
 	}
 	void DoGiveFoodToCustomer(MyCustomer customer){
 		print("Giving finished order of " + customer.choice +" to " + customer.cmr);
 		Position inFrontOfGrill = new Position(customer.food.getX()-1,customer.food.getY());
 		guiMoveFromCurrentPostionTo(inFrontOfGrill);//in front of grill
 		guiWaiter.pickUpFood(customer.food);
 		Position tablePos = new Position(tables[customer.tableNum].getX()-1,
 				tables[customer.tableNum].getY()+1);
 		guiMoveFromCurrentPostionTo(tablePos);
 		guiWaiter.serveFood(tables[customer.tableNum]);
 	}
 	void DoClearingTable(final MyCustomer customer){
 		print("Clearing table " + (customer.tableNum+1) + " (1500 milliseconds)");
 		timer.schedule(new TimerTask(){
 			public void run(){		    
 				endCustomer(customer);
 			}
 		}, 1500);
 	}
 	/** Function called at the end of the clear table timer
 	 * to officially remove the customer from the waiter's list.
 	 * @param customer customer who needs removed from list */
 	private void endCustomer(MyCustomer customer){ 
 		print("Table " + (customer.tableNum+1) + " is cleared!");
 		customer.food.remove(); //remove the food from table animation
 		host.msgTableIsFree(customer.tableNum);
 		customers.remove(customer);
 		stateChanged();
 	}
 	private void DoMoveToOriginalPosition(){
 		print("Nothing to do. Moving to original position="+originalPosition);
 		guiMoveFromCurrentPostionTo(originalPosition);
 	}
 
 	//this is just a subroutine for waiter moves. It's not an "Action"
 	//itself, it is called by Actions.
 	void guiMoveFromCurrentPostionTo(Position to){
 		//System.out.println("[Gaut] " + guiWaiter.getName() + " moving from " + currentPosition.toString() + " to " + to.toString());
 
 		AStarNode aStarNode = (AStarNode)aStar.generalSearch(currentPosition, to);
 		List<Position> path = aStarNode.getPath();
 		Boolean firstStep   = true;
 		Boolean gotPermit   = true;
 
 		for (Position tmpPath: path) {
 			//The first node in the path is the current node. So skip it.
 			if (firstStep) {
 				firstStep   = false;
 				continue;
 			}
 
 			//Try and get lock for the next step.
 			int attempts    = 1;
 			gotPermit       = new Position(tmpPath.getX(), tmpPath.getY()).moveInto(aStar.getGrid());
 
 			//Did not get lock. Lets make n attempts.
 			while (!gotPermit && attempts < 3) {
 				//System.out.println("[Gaut] " + guiWaiter.getName() + " got NO permit for " + tmpPath.toString() + " on attempt " + attempts);
 
 				//Wait for 1sec and try again to get lock.
 				try { Thread.sleep(1000); }
 				catch (Exception e){}
 
 				gotPermit   = new Position(tmpPath.getX(), tmpPath.getY()).moveInto(aStar.getGrid());
 				attempts ++;
 			}
 
 			//Did not get lock after trying n attempts. So recalculating path.            
 			if (!gotPermit) {
 				//System.out.println("[Gaut] " + guiWaiter.getName() + " No Luck even after " + attempts + " attempts! Lets recalculate");
 				guiMoveFromCurrentPostionTo(to);
 				break;
 			}
 
 			//Got the required lock. Lets move.
 			//System.out.println("[Gaut] " + guiWaiter.getName() + " got permit for " + tmpPath.toString());
 			currentPosition.release(aStar.getGrid());
 			currentPosition = new Position(tmpPath.getX(), tmpPath.getY ());
 			guiWaiter.move(currentPosition.getX(), currentPosition.getY());
 		}
 		/*
 	boolean pathTaken = false;
 	while (!pathTaken) {
 	    pathTaken = true;
 	    //print("A* search from " + currentPosition + "to "+to);
 	    AStarNode a = (AStarNode)aStar.generalSearch(currentPosition,to);
 	    if (a == null) {//generally won't happen. A* will run out of space first.
 		System.out.println("no path found. What should we do?");
 		break; //dw for now
 	    }
 	    //dw coming. Get the table position for table 4 from the gui
 	    //now we have a path. We should try to move there
 	    List<Position> ps = a.getPath();
 	    Do("Moving to position " + to + " via " + ps);
 	    for (int i=1; i<ps.size();i++){//i=0 is where we are
 		//we will try to move to each position from where we are.
 		//this should work unless someone has moved into our way
 		//during our calculation. This could easily happen. If it
 		//does we need to recompute another A* on the fly.
 		Position next = ps.get(i);
 		if (next.moveInto(aStar.getGrid())){
 		    //tell the layout gui
 		    guiWaiter.move(next.getX(),next.getY());
 		    currentPosition.release(aStar.getGrid());
 		    currentPosition = next;
 		}
 		else {
 		    System.out.println("going to break out path-moving");
 		    pathTaken = false;
 		    break;
 		}
 	    }
 	}
 		 */
 	}
 
 	// *** EXTRA ***
 
 	/** @return name of waiter */
 	public String getName(){
 		return name;
 	}
 
 	/** @return string representation of waiter */
 	public String toString(){
 		return "waiter " + getName();
 	}
 
 	/** Hack to set the cook for the waiter */
 	public void setCook(CookAgent cook){
 		this.cook = cook;
 	}
 
 	/** Hack to set the host for the waiter */
 	public void setHost(HostAgent host){
 		this.host = host;
 	}
 
 	/** Hack to set the cashier for the waiter */
 	public void setCashier(CashierAgent cashier) {
 		this.cashier = cashier;
 	}
 
 	/** @return true if the waiter is on break, false otherwise */
 	public boolean isOnBreak(){
		return breakstate == BreakState.onBreak;
 	}
 
 }
 
