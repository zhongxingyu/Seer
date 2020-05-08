 package HW4;
 /**
  * This should be the event dispatch thread. Handling other threads.
  * Should also be the manager's view GUI.
  */
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 
 public class Restaurant {
 	public String restaurantName ;
 	public String restaurantLocation ;
 	public String restaurantPhoneNum ;
 	private ArrayList<Menu> menuList;
 	private ArrayList<Manager> managerList;
 	private ArrayList<Cashier> cashierList;
 	private ArrayList<Chef> chefList;
 	private ArrayList<Ingredient> ingredients;
 	
 	public Restaurant() {
 		menuList = new ArrayList<Menu>();
 		managerList = new ArrayList<Manager>();
 		ingredients = new ArrayList<Ingredient>();
 	}
 	
 	
 	public void testInitialization() {
 //		Manager bob = new Manager("Bob", this);
 //		managerList.add(bob);
 //		Menu menu = new Menu("Breakfast", bob) ;
 //
 //		MenuItem eggs = new MenuItem("Eggs", 5.99) ;
 //		MenuItem pancakes = new MenuItem("Pancakes", 6.99) ;
 //		MenuItem toast = new MenuItem("Toast and Jam", 3.99) ;
 //		MenuItem burrito = new MenuItem("Burrito", 6.99) ;
 //		MenuItem frenchToast = new MenuItem("French Toast", 5.99) ;
 //		MenuItem omelet = new MenuItem("Omelet", 7.99) ;
 //		menu.addMenuItem(eggs) ;
 //		menu.addMenuItem(pancakes) ;
 //		menu.addMenuItem(toast) ;
 //		menu.addMenuItem(burrito) ;
 //		menu.addMenuItem(frenchToast) ;
 //		menu.addMenuItem(omelet) ;
 //		addMenu(menu);
 		loadMenu();
 	}
 	
	public void setMenuList(ArrayList<Menu> list) {
		menuList = list;
	}
	
 	private void loadMenu() {
 		String line;
 		int lineNumber = 0;
 		try {
 			FileInputStream inFile = new FileInputStream("menus.POS_MENU");
 			BufferedReader content = new BufferedReader(new InputStreamReader(inFile));
 			Menu loadMenu = null;
 			while((line = content.readLine()) != null){
 				if(lineNumber == 0) {
 					String elements[] = line.split("-");
 					loadMenu = new Menu(elements[0], new Manager(elements[1], this));
 					menuList.add(loadMenu);
 					lineNumber ++;
 				}else {
 					if(!line.equals("NEXT")){
 						String elements[] = line.split("-");
 						MenuItem item = new MenuItem(elements[0], Double.parseDouble(elements[1]));
 						loadMenu.addMenuItem(item);
 					} else {
 						lineNumber = 0;
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println("Error opening menu");
 		}
 
 	}
 	
 	public ArrayList<String> getCurrentMenuItemNames() {
 		ArrayList<String> names = new ArrayList<String>();
 		if(menuList.size() == 0)
 			return names;
 		Menu currentMenu = menuList.get((menuList.size()-1));
 		for(int i = 0; i < currentMenu.getNumberOfItems(); ++i) {
 			names.add(currentMenu.getItem(i).getName());
 		}
 		return names;
 	}
 	
 	public ArrayList<Double> getCurrentMenuItemPrices() {
 		ArrayList<Double> prices = new ArrayList<Double>();
 		if(menuList.size() == 0)
 			return prices;
 		Menu currentMenu = menuList.get((menuList.size()-1));
 		for(int i = 0; i < currentMenu.getNumberOfItems(); ++i) {
 			prices.add(currentMenu.getItem(i).getPrice());
 		}
 		return prices;
 	}
 	
 	public String getCurrentMenuName() {
 		if(menuList.size() != 0)
 			return menuList.get(menuList.size()-1).getName();
 		return "No Menu";
 	}
 	
 	public void addMenu(Menu menu) {
 		menuList.add(menu);
 	}
 	
 	public ArrayList<Menu> getMenuList() {
 		return this.menuList;
 	}
 }
