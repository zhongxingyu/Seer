 package pizzaProgram.gui.EventHandlers;
 
 import java.util.ArrayList;
 
 import pizzaProgram.dataObjects.Customer;
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.UnaddedOrder;
 import pizzaProgram.dataObjects.UnaddedOrderDish;
 import pizzaProgram.database.databaseUtils.DataCleaner;
 
 public class OrderGUI_TemporaryOrderDataStorage {
 	private Customer currentCustomer;
 	private ArrayList<UnaddedOrderDish> dishList = new ArrayList<UnaddedOrderDish>();
 	private String orderComments;
 	private String deliveryMethod;
 	
 	public OrderGUI_TemporaryOrderDataStorage(){
 		
 	}
 	
 	public UnaddedOrder convertToOrderObjectAndReset(){
 		String comments = DataCleaner.cleanDbData(this.orderComments);
 		UnaddedOrder order = new UnaddedOrder(currentCustomer, this.deliveryMethod, comments);
 		for(UnaddedOrderDish dish : this.dishList){
 			order.addOrderDish(dish);
 		}
 		
 		this.reset();
 		return order;
 	}
 	
 	public void reset(){
 		this.currentCustomer = null;
 		this.orderComments = null;
 		this.dishList = new ArrayList<UnaddedOrderDish>();
 		this.deliveryMethod = null;
 	}
 	
 	public void setDeliveryMethod(String method){
 		this.deliveryMethod = method;
 	}
 	
 	public void setCustomer(Customer customer){
 		this.currentCustomer = customer;
 	}
 	
 	public void setOrderComments(String comments){
 		this.orderComments = comments;
 	}
 	
 	public void addDish(Dish dish, Extra[] extrasToBeAddedToDish){
 		UnaddedOrderDish orderDish = new UnaddedOrderDish(dish);
 		for(Extra extra : extrasToBeAddedToDish){
 			orderDish.addExtra(extra);
 		}
 		this.dishList.add(orderDish);
 	}
 	
 	public void removeDishFromOrder(int indexInTable){
 		this.dishList.remove(indexInTable);
 	}
 	
 	public void duplicateDishInOrder(int indexInTable){
 		this.dishList.add(this.dishList.get(indexInTable));
 	}
 	
 }//END
