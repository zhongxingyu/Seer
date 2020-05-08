 package restaurant;
 
 import agent.Agent;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.*;
 import restaurant.layoutGUI.*;
 import java.awt.Color;
 
 
 /** Cook agent for restaurant.
  *  Keeps a list of orders for waiters
  *  and simulates cooking them.
  *  Interacts with waiters only.
  */
 public class CookAgent extends Agent {
 
     //List of all the orders
     private List<Order> orders = new ArrayList<Order>();
     private Map<String,FoodData> inventory = new HashMap<String,FoodData>();
     public enum Status {pending, cooking, done}; // order status
     
     Set<MyMarket> markets = new HashSet<MyMarket>();
     CashierAgent cashier;
 
     //Name of the cook
     private String name;
 
     //Timer for simulation
     Timer timer = new Timer();
     Restaurant restaurant; //Gui layout
 
     /** Constructor for CookAgent class
      * @param name name of the cook
      */
     public CookAgent(String name, Restaurant restaurant) {
 	super();
 
 	this.name = name;
 	this.restaurant = restaurant;
 	//Create the restaurant's inventory.
 	inventory.put("Steak",new FoodData("Steak", 5));
 	inventory.put("Chicken",new FoodData("Chicken", 4));
 	inventory.put("Pizza",new FoodData("Pizza", 3));
 	inventory.put("Salad",new FoodData("Salad", 2));
     }
     /** Public class to store information about food.
      *  Contains the food type, its cooking time, and amount in inventory
      *  public to share with cashier and market when passing it back and forth.
      */
     public class FoodData {
 	String type; //kind of food
 	double cookTime;
 	int amount;
 	// other things ...
 	
 	public FoodData(String type, double cookTime){
 	    this.type = type;
 	    this.cookTime = cookTime;
 	    amount = 10;
 	}
     }
     enum MarketStatus {available, ordering, paying, paid, deadtome};
     /** Private class to store market information.
      * Contains the market, status, and order info
      */
     public class MyMarket {
    	public MarketAgent market;
    	public MarketStatus status;
    	public List<FoodData> currentOrder;
    	public double currentOrderCost;
     	
     	public MyMarket(MarketAgent mark) {
     		market = mark;
     		status = MarketStatus.available;
     		currentOrder = new ArrayList<FoodData>();
     		currentOrderCost = 0;
     	}
     	
     }
     /** Private class to store order information.
      *  Contains the waiter, table number, food item,
      *  cooktime and status.
      */
     private class Order {
 	public WaiterAgent waiter;
 	public int tableNum;
 	public String choice;
 	public Status status;
 	public Food food; //a gui variable
 
 	/** Constructor for Order class 
 	 * @param waiter waiter that this order belongs to
 	 * @param tableNum identification number for the table
 	 * @param choice type of food to be cooked 
 	 */
 	public Order(WaiterAgent waiter, int tableNum, String choice){
 	    this.waiter = waiter;
 	    this.choice = choice;
 	    this.tableNum = tableNum;
 	    this.status = Status.pending;
 	}
 
 	/** Represents the object as a string */
 	public String toString(){
 	    return choice + " for " + waiter ;
 	}
     }
 
 
     
 
 
     // *** MESSAGES ***
 
     /** Message from a waiter giving the cook a new order.
      * @param waiter waiter that the order belongs to
      * @param tableNum identification number for the table
      * @param choice type of food to be cooked
      */
     public void msgHereIsAnOrder(WaiterAgent waiter, int tableNum, String choice){
 	orders.add(new Order(waiter, tableNum, choice));
 	stateChanged();
     }
     
     /** Message for when a Market runs out of food
      * @param market the market the message belongs to
      */
     public void msgIHaveNoFood(MarketAgent market) {
     	for (MyMarket m:markets)
     		if (m.market == market)
     			m.status = MarketStatus.deadtome;
     }
     
     /** Message to denote the price of a market order
      * @param market the market the message belongs to
      * @param price the price of the order
      * @param curorder the order
      */
     public void msgHereIsPrice(MarketAgent market, double price, List<FoodData> curorder) {
     	for (MyMarket m:markets) {
     		if (m.market == market) {
     			m.currentOrderCost = price;
     			m.currentOrder = curorder;
     			m.status = MarketStatus.ordering;
     		}
     	}
     }
     
     /** Message to receive food from a market
      * @param market the market the food belongs to
      * @param curOrder the food being sent
      */
     public void msgTakeMyFood(MarketAgent market, List<FoodData> curOrder) {
     	for (MyMarket m:markets) {
     		if (m.market == market) {
     			m.currentOrder = curOrder;
     			m.status = MarketStatus.paid;
     		}
     	}
     }
 
 
     /** Scheduler.  Determine what action is called for, and do it. */
     protected boolean pickAndExecuteAnAction() {
 	
 	//If there exists an order o whose status is done, place o.
 	for(Order o:orders){
 	    if(o.status == Status.done){
 		placeOrder(o);
 		return true;
 	    }
 	}
 	//If there exists an order o whose status is pending, cook o.
 	for(Order o:orders){
 	    if(o.status == Status.pending){
 		cookOrder(o);
 		return true;
 	    }
 	}
 	
 	/*for (MyMarket m:markets) { //If you want to do something with the dead market
 		if (m.status == MarketStatus.deadtome) {
 			m.msgF***YOU();
 			return true;
 		}
 	}*/
 	
 	for (MyMarket m:markets) {
 		if (m.status == MarketStatus.ordering) {
 			DoPurchaseFood(m);
 			return true;
 		}
 	}
 	
 	for (MyMarket m:markets) {
 		if (m.status == MarketStatus.paid) {
 			DoStockFood(m);
 			return true;
 		}
 	}
 
 	//we have tried all our rules (in this case only one) and found
 	//nothing to do. So return false to main loop of abstract agent
 	//and wait.
 	return false;
     }
     
 
     // *** ACTIONS ***
     
     /** Starts a timer for the order that needs to be cooked. 
      * @param order
      */
     private void cookOrder(Order order){
 	DoCooking(order);
 	order.status = Status.cooking;
     }
 
     private void placeOrder(Order order){
 	DoPlacement(order);
 	order.waiter.msgOrderIsReady(order.tableNum, order.food);
 	orders.remove(order);
     }
     
     private void DoPurchaseFood(MyMarket m) {
     	cashier.msgBuyMeFood(m.currentOrder, m.currentOrderCost, m.market);
     	m.status = MarketStatus.paying;
     }
     
     private void DoStockFood(MyMarket m) {
     	for (FoodData f:m.currentOrder)
     		inventory.get(f.type).amount += f.amount;
     	m.status = MarketStatus.available;
     }
 
 
     // *** EXTRA -- all the simulation routines***
 
     /** Returns the name of the cook */
     public String getName(){
         return name;
     }
 
     private void DoCooking(final Order order){
 	print("Cooking:" + order + " for table:" + (order.tableNum+1));
 	//put it on the grill. gui stuff
 	order.food = new Food(order.choice.substring(0,2),new Color(0,255,255), restaurant);
 	order.food.cookFood();
 
 	timer.schedule(new TimerTask(){
 	    public void run(){//this routine is like a message reception    
 		order.status = Status.done;
 		stateChanged();
 	    }
 	}, (int)(inventory.get(order.choice).cookTime*1000));
     }
     public void DoPlacement(Order order){
 	print("Order finished: " + order + " for table:" + (order.tableNum+1));
 	order.food.placeOnCounter();
     }
 }
 
 
     
