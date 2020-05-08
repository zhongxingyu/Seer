 package restaurant;
 
 import restaurant.WaiterAgent.CustomerState;
 import restaurant.gui.RestaurantGui;
 import restaurant.layoutGUI.*;
 import agent.Agent;
 import java.util.*;
 import java.awt.Color;
 
 /** Restaurant customer agent. 
  * Comes to the restaurant when he/she becomes hungry.
  * Randomly chooses a menu item and simulates eating 
  * when the food arrives. 
  * Interacts with a waiter only */
 public class CustomerAgent extends Agent {
 	private String name;
 	private int hungerLevel = 5;  // Determines length of meal
 	double money = 50;
 	double bill = 0;
 	int kidneys = 2;
 	double change = 0;
 	boolean isLawbreaker = false;
 	boolean isPatient = true;
 	private RestaurantGui gui;
 
 	// ** Agent connections **
 	private HostAgent host;
 	private WaiterAgent waiter;
 	private CashierAgent cashier;
 	Restaurant restaurant;
 	private Menu menu;
 	Timer timer = new Timer();
 	GuiCustomer guiCustomer; //for gui
 	// ** Agent state **
 	private boolean isHungry = false; //hack for gui
 	public enum AgentState
 	{DoingNothing, WaitingInRestaurant, SeatedWithMenu, WaiterCalled, WaitingForFood, Eating, Paying};
 	//{NO_ACTION,NEED_SEATED,NEED_DECIDE,NEED_ORDER,NEED_EAT,NEED_LEAVE};
 	private AgentState state = AgentState.DoingNothing;//The start state
 	public enum AgentEvent 
 	{gotHungry, beingSeated, decidedChoice, waiterToTakeOrder, foodDelivered, doneEating, gotUnpayableBill, gotBill, gotChange, lostKidney, pleaseReorder, pleaseWait};
 	List<AgentEvent> events = new ArrayList<AgentEvent>();
 
 	/** Constructor for CustomerAgent class 
 	 * @param name name of the customer
 	 * @param gui reference to the gui so the customer can send it messages
 	 */
 	public CustomerAgent(String name, RestaurantGui gui, Restaurant restaurant) {
 		super();
 		this.gui = gui;
 		this.name = name;
 		this.restaurant = restaurant;
 		guiCustomer = new GuiCustomer(name.substring(0,2), new Color(0,255,0), restaurant);
 	}
 	public CustomerAgent(String name, Restaurant restaurant) {
 		super();
 		this.gui = null;
 		this.name = name;
 		this.restaurant = restaurant;
 		guiCustomer = new GuiCustomer(name.substring(0,1), new Color(0,255,0), restaurant);
 	}
 	// *** MESSAGES ***
 	/** Sent from GUI to set the customer as hungry */
 	public void setHungry() {
 		events.add(AgentEvent.gotHungry);
 		isHungry = true;
 		print("I'm hungry");
 		stateChanged();
 	}
 	/** Sent from GUI to set if the customer can break the law or not*/
 	public void setLawBreaker(boolean breaker) {
 		isLawbreaker = true;
 	}
 	/** Waiter sends this message so the customer knows to sit down 
 	 * @param waiter the waiter that sent the message
 	 * @param menu a reference to a menu */
 	public void msgFollowMeToTable(WaiterAgent waiter, Menu menu) {
 		this.menu = menu;
 		this.waiter = waiter;
 		print("Received msgFollowMeToTable from" + waiter);
 		events.add(AgentEvent.beingSeated);
 		stateChanged();
 	}
 	/** Waiter sends this message to take the customer's order */
 	public void msgDecided(){
 		events.add(AgentEvent.decidedChoice);
 		stateChanged(); 
 	}
 	/** Waiter sends this message to take the customer's order */
 	public void msgWhatWouldYouLike(){
 		events.add(AgentEvent.waiterToTakeOrder);
 		stateChanged(); 
 	}
 
 	/** Waiter sends this when the food is ready 
 	 * @param choice the food that is done cooking for the customer to eat */
 	public void msgHereIsYourFood(String choice) {
 		events.add(AgentEvent.foodDelivered);
 		stateChanged();
 	}
 	/** Timer sends this when the customer has finished eating */
 	public void msgDoneEating() {
 		events.add(AgentEvent.doneEating);
 		stateChanged(); 
 	}
 
 	/** Waiter sends this when the bill is ready
 	 * @param bill The cost of the meal
 	 */
 	public void msgHereIsBill(CashierAgent cashier, double bill) {
 		this.cashier = cashier;
 		this.bill = bill;
 		if (bill % 5 * 5 > money)
 			this.bill = bill % 5 * 5; //Pays with the nearest multiple of 5 if possible
 		if (bill > money)
 			events.add(AgentEvent.gotUnpayableBill);
 		else
 			events.add(AgentEvent.gotBill);
 		stateChanged();
 	}
 
 	/** Cashier sends this after the customer has paid
 	 * @param change The change the customer is due. Leaves it all as tip
 	 */
 	public void msgTakeYourChange(double change) {
 		events.add(AgentEvent.gotChange);
 		this.change = change;
 		stateChanged();
 	}
 
 	/** Cashier calls this if the customer cannot afford what he has ordered - he pays with a kidney
 	 */
 	public void removeKidney() {
 		events.add(AgentEvent.lostKidney);
 		stateChanged();
 	}
 
 	/** Waiter sends this when what the customer orders is out of stock
 	 * @param menu the updated menu without the out of stock items
 	 */
 	public void msgOrderAgain(Menu menu) {
 		this.menu = menu;
 		events.add(AgentEvent.pleaseReorder);
 		stateChanged();
 	}
 
 	public void msgYoullHaveToWait() {
 		events.add(AgentEvent.pleaseWait);
 		stateChanged();
 	}
 
 
 	/** Scheduler.  Determine what action is called for, and do it. */
 	protected boolean pickAndExecuteAnAction() {
 		if (events.isEmpty()) return false;
 		AgentEvent event = events.remove(0); //pop first element
 		print(event.name());
 		//Simple finite state machine
 		if (state == AgentState.DoingNothing){
 			if (event == AgentEvent.gotHungry)	{
 				goingToRestaurant();
 				state = AgentState.WaitingInRestaurant;
 				return true;
 			}
 			// elseif (event == xxx) {}
 		}
 		if (state == AgentState.WaitingInRestaurant) {
 			if (event == AgentEvent.beingSeated)	{
 				makeMenuChoice();
 				state = AgentState.SeatedWithMenu;
 				return true;
 			}
 			else if (event == AgentEvent.pleaseWait) {
 				doProcessWaiting();
 				return true;
 			}
 		}
 		if (state == AgentState.SeatedWithMenu) {
 			if (event == AgentEvent.decidedChoice)	{
 				callWaiter();
 				state = AgentState.WaiterCalled;
 				return true;
 			}
 		}
 		if (state == AgentState.WaiterCalled) {
 			if (event == AgentEvent.waiterToTakeOrder)	{
 				orderFood();
 				state = AgentState.WaitingForFood;
 				return true;
 			}
 		}
 		if (state == AgentState.WaitingForFood) {
 			if (event == AgentEvent.foodDelivered)	{
 				eatFood();
 				state = AgentState.Eating;
 				return true;
 			}
 			else if (event == AgentEvent.pleaseReorder) {
 				doReorderFood();
 				return true;
 			}
 		}
 		if (state == AgentState.Eating) {
 			if (event == AgentEvent.doneEating)	{
 				doneEating();
 				state = AgentState.Paying;
 				return true;
 			}
 		}
 		if (state == AgentState.Paying) {
 			if (event == AgentEvent.gotUnpayableBill) {
 				doICantPay();
 				return true;
 			}
 			else if (event == AgentEvent.gotBill) {
 				doPayBill();
 				return true;
 			}
 			else if (event == AgentEvent.gotChange) {
 				leaveRestaurant(change);
 				state = AgentState.DoingNothing;
 				return true;
 			}
 			else if (event == AgentEvent.lostKidney) {
 				doLoseKidney();
 				state = AgentState.DoingNothing;
 				return true;
 			}
 		}
 
 		print("No scheduler rule fired, should not happen in FSM, event="+event+" state="+state);
 		return false;
 	}
 
 	// *** ACTIONS ***
 
 	/** Goes to the restaurant when the customer becomes hungry */
 	private void goingToRestaurant() {
 		print("Going to restaurant");
 		guiCustomer.appearInWaitingQueue();
 		host.msgIWantToEat(this);//send him our instance, so he can respond to us
 		stateChanged();
 	}
 
 	/** Starts a timer to simulate the customer thinking about the menu */
 	private void makeMenuChoice(){
 		print("Deciding menu choice...(3000 milliseconds)");
 		timer.schedule(new TimerTask() {
 			public void run() {  
 				msgDecided();	    
 			}},
 			3000);//how long to wait before running task
 		stateChanged();
 	}
 	private void callWaiter(){
 		print("I decided!");
 		waiter.msgImReadyToOrder(this);
 		stateChanged();
 	}
 
 	/** Picks a random choice from the menu and sends it to the waiter */
 	private void orderFood(){
		int choicenum = (int)Math.random()*menu.choices.length;
 		String choice = menu.choices[choicenum];
 		for (int i = 0; money < menu.getPrice(choice) && !isLawbreaker; i++) {
 			if (i == menu.choices.length) {
 				leaveRestaurant(0);
 				return;
 			}
 			choice = menu.choices[(choicenum + i) % menu.choices.length];
 		}
 		//String choice = menu.choices[0];
 		print("Ordering the " + choice);
 		waiter.msgHereIsMyChoice(this, choice);
 		stateChanged();
 	}
 
 	/** Starts a timer to simulate eating */
 	private void eatFood() {
 		print("Eating for " + hungerLevel*1000 + " milliseconds.");
 		timer.schedule(new TimerTask() {
 			public void run() {
 				msgDoneEating();    
 			}},
 			getHungerLevel() * 1000);//how long to wait before running task
 		stateChanged();
 	}
 
 	/** Tells the waiter he's done*/
 	private void doneEating() {
 		print("I finished eating");
 		waiter.msgDoneEating(this);
 		stateChanged();
 	}
 	/** Pays the bill*/
 	private void doPayBill() {
 		money -= bill;
 		cashier.msgTakeMyMoney(this, bill);
 		stateChanged();
 	}
 
 	/** When the customer is done eating, he leaves the restaurant */
 	private void leaveRestaurant(double tip) {
 		print("Leaving the restaurant");
 		guiCustomer.leave(); //for the animation
 		if (waiter != null)
 			waiter.msgLeaving(this, tip);
 		isHungry = false;
 		state = AgentState.DoingNothing;
 		stateChanged();
 		gui.setCustomerEnabled(this); //Message to gui to enable hunger button
 
 		//hack to keep customer getting hungry. Only for non-gui customers
 		if (gui==null) becomeHungryInAWhile();//set a timer to make us hungry.
 	}
 
 	/** Alerts cashier he can't afford what he just ate*/
 	private void doICantPay() {
 		cashier.msgICantPay(this);
 		stateChanged();
 	}
 
 	/** Pays for meal with kidney. For now, can go into negative kidneys until I figure out what I want to do with a kidneyless customer*/
 	private void doLoseKidney() {
 		kidneys--;
 		//if (kidneys <= 0)
 		//do whatever
 		stateChanged();
 	}
 
 	/** Reorders food if necessary*/
 	private void doReorderFood() {
 		if (Math.random() >= 1.0/(menu.choices.length+1)) {
 			orderFood();
 			stateChanged();
 		}
 		else {
 			leaveRestaurant(0);
 		}
 	}
 
 	private void doProcessWaiting() {
 		//if (Math.random() < 50.5) {
 		if (!isPatient) {
 			host.msgIHateWaiting(this);
 			leaveRestaurant(0);
 		}
 		else
 			state = AgentState.WaitingInRestaurant;
 		stateChanged();
 	}
 
 	/** This starts a timer so the customer will become hungry again.
 	 * This is a hack that is used when the GUI is not being used */
 	private void becomeHungryInAWhile() {
 		timer.schedule(new TimerTask() {
 			public void run() {  
 				setHungry();		    
 			}},
 			15000);//how long to wait before running task
 	}
 
 	// *** EXTRA ***
 
 	/** establish connection to host agent. 
 	 * @param host reference to the host */
 	public void setHost(HostAgent host) {
 		this.host = host;
 	}
 	/** establish connection to cashier agent
 	 * @param cashier reference to the cashier
 	 */
 	public void setCashier(CashierAgent cashier) {
 		this.cashier = cashier;
 	}
 
 	/** Returns the customer's name
 	 *@return name of customer */
 	public String getName() {
 		return name;
 	}
 
 	/** @return true if the customer is hungry, false otherwise.
 	 ** Customer is hungry from time he is created (or button is
 	 ** pushed, until he eats and leaves.*/
 	public boolean isHungry() {
 		return isHungry;
 	}
 
 	/** @return the hungerlevel of the customer */
 	public int getHungerLevel() {
 		return hungerLevel;
 	}
 
 	/** Sets the customer's hungerlevel to a new value
 	 * @param hungerLevel the new hungerlevel for the customer */
 	public void setHungerLevel(int hungerLevel) {
 		this.hungerLevel = hungerLevel; 
 	}
 	public GuiCustomer getGuiCustomer(){
 		return guiCustomer;
 	}
 
 	/** @return the string representation of the class */
 	public String toString() {
 		return "customer " + getName();
 	}
 	 /** A hack to set the money of the customer
 	  * @param money the amount of money the customer has
 	  */
 	public void setMoney(double money) {
 		this.money = money;
 	}
 	
 	/** A hack to set the patience of the customer
 	 * @param patient if the customer is patient or not
 	 */
 	public void setPatient(boolean patient) {
 		this.isPatient = patient;
 	}
 
 
 }
 
