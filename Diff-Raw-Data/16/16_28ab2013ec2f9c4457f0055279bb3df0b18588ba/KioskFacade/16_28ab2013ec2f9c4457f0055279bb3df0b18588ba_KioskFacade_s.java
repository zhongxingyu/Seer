 package HW4;
 
 import java.util.ArrayList;
 
 public class KioskFacade {
 	private Kiosk theKiosk;
 	private Restaurant theRestaurant;
 	
 	public KioskFacade() {
 		theRestaurant = new Restaurant();
 		theRestaurant.initialization();
 	}
 	
 	public void save() {
 		theRestaurant.save();
 	}
 	
 	public Restaurant getRestaurant() {
 		return theRestaurant;
 	}
 	
 	public MenuItem getOrderItem(String name) {
 		return theRestaurant.getCurrentMenu().getItemOfName(name);
 	}
 	
 	public void addMenuItem(String name, double price) {
 		theRestaurant.addMenuItem(name, price);
 	}
 	
 	public String getMenuName() {
 		return theRestaurant.getCurrentMenuName();
 	}
 	
 	public void addNewMenu(String menuName, String managerName) {
 		theRestaurant.addMenu(new Menu(menuName, new Manager(managerName, theRestaurant)));
 	}
 	
 	public ArrayList<String> getCurrentMenuItemNames() {
 		return theRestaurant.getCurrentMenuItemNames();
 	}
 	
 	public ArrayList<Double> getCurrentMenuItemPrices() {
 		return theRestaurant.getCurrentMenuItemPrices();
 	}
 	
 	public ArrayList<String> getIngredients() {
 		ArrayList<String> ingredientList = new ArrayList<String>();
 		ArrayList<Ingredient> restIngredients = new ArrayList<Ingredient>();
 		for(int i = 0; i < restIngredients.size(); ++i) {
 			ingredientList.add(restIngredients.get(i).getName());
 		}
 		return ingredientList;
 	}
 }
