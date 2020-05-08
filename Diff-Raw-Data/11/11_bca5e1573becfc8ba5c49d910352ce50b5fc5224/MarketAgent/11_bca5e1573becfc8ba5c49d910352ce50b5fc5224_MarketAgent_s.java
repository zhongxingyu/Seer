 package restaurant;
 
 import agent.Agent;
 import java.util.*;
 import restaurant.CookAgent.FoodData;
 
 
 /** Host agent for restaurant. //TODO: UPDATE THIS DESCRIPTION
  *  Keeps a list of all the waiters and tables.
  *  Assigns new customers to waiters for seating and 
  *  keeps a list of waiting customers.
  *  Interacts with customers and waiters.
  */
 public class MarketAgent extends Agent {
 
     /** Private class to hold cook information and state */
 	private class MyCook {
 		public CookAgent cook;
 		public CookStatus status;
 		public List<FoodData> currentOrder;
 		public int currentCost;
 		
 		public MyCook() {
 			cook = null;
 			status = CookStatus.nothing;
 			currentOrder = new ArrayList<FoodData>();
 			currentCost = 0;
 		}
 	}
 	public enum CookStatus {nothing, ordering, paying, paid};
 	private MyCook cook;
 	
 	Map<String, FoodData> inventory;
 
     //Name of the market
     private String name;
 
     /** Constructor for MarketAgent class 
      * @param name name of the market */
     public MarketAgent(String name) {
 	super();
 	this.name = name;
 	cook = new MyCook();
 	inventory = new HashMap<String, FoodData>();
 	
     }
 
     // *** MESSAGES ***
     
     /** Cook sends this message to request food. Assumes choices and amounts are of same length.
      * @param cook the cook that wants food
      * @param choices the list of names of foods
      * @param amounts the list of amounts of foods
     */
 	public void msgINeedFood(CookAgent cook, List<FoodData> currentOrder) {
 		this.cook.cook = cook;
 		for (int i = 0; i < currentOrder.size(); i++) {
 			this.cook.currentOrder.add(currentOrder.get(i));
 		}
 		stateChanged();
 	}
 	
 	/** Cashier sends money to the market. Market just eats money.
 	 * We assume that the price is sufficient for the order and that they order the same thing.
 	 * No requirements to assume otherwise, and if they don't, it gets too complex
 	 * In my design doc, I allow for a change in order, but upon implementation, I realize this is too convoluted
 	 * and I can't expect the cashier or cook to know what they want to change. I'll just let them be in debt if they need to.
 	 * 
 	*/
 	public void msgTakeMyMoney(CashierAgent cashier, double price) {
 		cook.status = CookStatus.paid;
 		if (price == 0) //If they pay nothing, they get nothing. A small hack.
 			this.cook.currentOrder = new ArrayList<FoodData>();
 	}
 
     /** Scheduler.  Determine what action is called for, and do it. */
     protected boolean pickAndExecuteAnAction() {
 	
 	if (cook.status == CookStatus.ordering) {
 		DoCalculateOrder();
 		return true;
 	}
 	if (cook.status == CookStatus.paid) {
 		DoFulfilOrder();
 		return true;
 	}
 
 	//we have tried all our rules and found
 	//nothing to do. So return false to main loop of abstract agent
 	//and wait.
 	return false;
     }
     
     // *** ACTIONS ***
     
     /** Calculates the price of an order and returns what can be sold to the cook and at what price.
      * As of present, there is nothing in the inventory of the market, so all is bad.
     */
     private void DoCalculateOrder() {
     	boolean outOfStock = true;
 		for (FoodData o:cook.currentOrder) {
 			if (inventory.get(o.type) == null) {
 				o.amount = 0;
 			}
 			else if (o.amount > inventory.get(o.type).amount) {
				o.amount = inventory.get(o.type).amount);
 				if (o.amount > 0)
 					outOfStock = false;
 			}
 		}
 	
 		if (outOfStock) {//for all orders in cook.currentOrder, amount = 0
			cook.cook.msgIHaveNoFood();
 			cook.status = CookStatus.nothing;
 		}
 		
 		else {
			cook.cook.msgHereIsPrice(DoCalculatePrice(cook.currentOrder), cook.currentOrder);
 			cook.status = CookStatus.paying;
 		}
     }
 	
     /** Gives the food to the cook
      *
     */
     private void DoFulfilOrder() {
     	cook.status = CookStatus.nothing;
 		
    	cook.cook.TakeMyFood(cook.currentOrder);
     }
 
     // *** EXTRA ***
 
     /** Returns the name of the market 
      * @return name of market */
     public String getName(){
         return name;
     }    
     
     /** Returns the total price of an order
      * This is an int in the design document, but now I decided to allow for decimal prices.
      * @param currentOrder the order to calculate the price of
      * @return the price of the order
      */
     double DoCalculatePrice(List<FoodData> currentOrder) {
     	double total = 0;
     	for (FoodData o:currentOrder)
     		total += 100 * o.amount;
     	return total;
     }
 
 }
