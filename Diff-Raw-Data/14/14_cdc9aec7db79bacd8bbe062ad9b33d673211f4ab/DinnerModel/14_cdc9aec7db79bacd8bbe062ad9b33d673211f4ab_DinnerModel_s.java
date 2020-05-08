 package se.kth.csc.iprog.dinnerplanner.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 public class DinnerModel implements IDinnerModel {
 	int guests;
 	public ArrayList<Dish> menu = new ArrayList<Dish>();
 	
 
 	
 	@Override
 	public int getNumberOfGuests(){
 		return guests;
 	};
 	
 	@Override
 	public void setNumberOfGuests(int numberOfGuests){
 		guests = numberOfGuests;
 	};
 	
 	/**
 	 * Returns the dish that is on the menu for selected type (1 = starter, 2 = main, 3 = desert).
 	 */
 	@Override
 	public Dish getSelectedDish(int type){		// OBS! Lr returnera null om menu r tom. Snyggare lsning behvs.
 		if(menu.get(0).getType() == type){
 			return menu.get(0);
 		}else if(menu.get(1).getType() == type){
 			return menu.get(1);
 		}else {
 			return menu.get(2);
 		}
 	};
 	
 	/**
 	 * Returns all the dishes on the menu.
 	 */
 	@Override
 	public Set<Dish> getFullMenu(){
 		Set<Dish> result = new HashSet<Dish>();
 		for(Dish d : menu){
 			result.add(d);
 		}
 		return result;
 	};
 	
 	/**
 	 * Returns all ingredients for all the dishes on the menu.
 	 */
 	@Override
 	public Set<Ingredient> getAllIngredients(){
 		Set<Ingredient> result = new HashSet<Ingredient>();
 		for(Dish d: menu){
 			Set<Ingredient> dishIngredients = d.getIngredients();
 			for(Ingredient i : dishIngredients){
<<<<<<< HEAD
				i.setQuantity(i.getQuantity());
=======
>>>>>>> Anooying
 				//for(Ingredient iInSet : result){
 				//	if(i.getName().equals(iInSet.getName())){ // ingredient already exists in the result
 				//		double quantity = i.getQuantity();
 				//		iInSet.setQuantity(iInSet.getQuantity()+quantity);
 				//	} else{
 				//		result.add(i);
 				//	}
 				//}
 				result.add(i); // testing, tar inte hnsyn till om ingrediens redan finns tillagd
 			}
 		}
 		return result;
 	};
 	
 	/**
 	 * Returns the total price of the menu (all the ingredients multiplied by number of guests).
 	 */
 	@Override
 	public float getTotalMenuPrice(){
 		Set<Ingredient> allIngredients = getAllIngredients();
 		double price = 0;
 		float totalprice = 0;
 		guests = getNumberOfGuests();
 		
 		for(Ingredient i : allIngredients){
 			price = i.getPrice();
 			totalprice += price;
 		}
 		totalprice = totalprice * guests;
 		return totalprice;
 	};
 
 	
 
 	Set<Dish> dishes = new HashSet<Dish>();
 	
 	/**
 	 * TODO: For Lab2 you need to implement the IDinnerModel interface.
 	 * When you do this you will have all the needed fields and methods
 	 * for the dinner planner (number of guests, selected dishes, etc.). 
 	 */
 	
 	
 	/**
 	 * The constructor of the overall model. Set the default values here
 	 */
 	public DinnerModel(){
 		
 		//Adding some example data, you can add more
 		Dish dish1 = new Dish("French toast",Dish.STARTER,"toast.jpg","In a large mixing bowl, beat the eggs. Add the milk, brown sugar and nutmeg; stir well to combine. Soak bread slices in the egg mixture until saturated. Heat a lightly oiled griddle or frying pan over medium high heat. Brown slices on both sides, sprinkle with cinnamon and serve hot.");
 		Ingredient dish1ing1 = new Ingredient("eggs",0.5,"",1);
 		Ingredient dish1ing2 = new Ingredient("milk",30,"ml",6);
 		Ingredient dish1ing3 = new Ingredient("brown sugar",7,"g",1);
 		Ingredient dish1ing4 = new Ingredient("ground nutmeg",0.5,"g",12);
 		Ingredient dish1ing5 = new Ingredient("white bread",2,"slices",2);
 		dish1.addIngredient(dish1ing1);
 		dish1.addIngredient(dish1ing2);
 		dish1.addIngredient(dish1ing3);
 		dish1.addIngredient(dish1ing4);
 		dish1.addIngredient(dish1ing5);
 		dishes.add(dish1);
 		
 		Dish dish2 = new Dish("Meat balls",Dish.MAIN,"meatballs.jpg","Preheat an oven to 400 degrees F (200 degrees C). Place the beef into a mixing bowl, and season with salt, onion, garlic salt, Italian seasoning, oregano, red pepper flakes, hot pepper sauce, and Worcestershire sauce; mix well. Add the milk, Parmesan cheese, and bread crumbs. Mix until evenly blended, then form into 1 1/2-inch meatballs, and place onto a baking sheet. Bake in the preheated oven until no longer pink in the center, 20 to 25 minutes.");
 		Ingredient dish2ing1 = new Ingredient("extra lean ground beef",115,"g",20);
 		Ingredient dish2ing2 = new Ingredient("sea salt",0.7,"g",3);
 		Ingredient dish2ing3 = new Ingredient("small onion, diced",0.25,"",2);
 		Ingredient dish2ing4 = new Ingredient("garlic salt",0.6,"g",3);
 		Ingredient dish2ing5 = new Ingredient("Italian seasoning",0.3,"g",3);
 		Ingredient dish2ing6 = new Ingredient("dried oregano",0.3,"g",3);
 		Ingredient dish2ing7 = new Ingredient("crushed red pepper flakes",0.6,"g",3);
 		Ingredient dish2ing8 = new Ingredient("Worcestershire sauce",16,"ml",7);
 		Ingredient dish2ing9 = new Ingredient("milk",20,"ml",4);
 		Ingredient dish2ing10 = new Ingredient("grated Parmesan cheese",5,"g",8);
 		Ingredient dish2ing11 = new Ingredient("seasoned bread crumbs",115,"g",4);
 		dish2.addIngredient(dish2ing1);
 		dish2.addIngredient(dish2ing2);
 		dish2.addIngredient(dish2ing3);
 		dish2.addIngredient(dish2ing4);
 		dish2.addIngredient(dish2ing5);
 		dish2.addIngredient(dish2ing6);
 		dish2.addIngredient(dish2ing7);
 		dish2.addIngredient(dish2ing8);
 		dish2.addIngredient(dish2ing9);
 		dish2.addIngredient(dish2ing10);
 		dish2.addIngredient(dish2ing11);
 		dishes.add(dish2);
 		
 		Dish dish3 = new Dish("Baked Brie", Dish.DESERT, "bakedbrie.jpg", "Savour with care!");
 		Ingredient dish3ing1 = new Ingredient("Brie cheese", 20, "g", 10);
 		Ingredient dish3ing2 = new Ingredient("sea salt",0.2,"g",3);
 		Ingredient dish3ing3 = new Ingredient("Crackers", 6, "", 0.5);
 		dish3.addIngredient(dish3ing1);
 		dish3.addIngredient(dish3ing2);
 		dish3.addIngredient(dish3ing3);
 		dishes.add(dish3);
 
 		
 		// Hrdkodad menu att hmta data ifrn
 		menu.add(dish1);
 		menu.add(dish2);
 		menu.add(dish3);
 		
 		guests = 1;
 		
 	}
 	
 	
 	
 	/**
 	 * Returns the set of dishes of specific type. (1 = starter, 2 = main, 3 = desert).
 	 */
 	public Set<Dish> getDishes(){
 		return dishes;
 	}
 	
 	/**
 	 * Returns the set of dishes of specific type. (1 = starter, 2 = main, 3 = desert).
 	 */
 	public Set<Dish> getDishesOfType(int type){
 		Set<Dish> result = new HashSet<Dish>();
 		for(Dish d : dishes){
 			if(d.getType() == type){
 				result.add(d);
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns the set of dishes of specific type, that contain filter in their name
 	 * or name of any ingredient. 
 	 */
 	public Set<Dish> filterDishesOfType(int type, String filter){
 		Set<Dish> result = new HashSet<Dish>();
 		for(Dish d : dishes){
 			if(d.getType() == type && d.contains(filter)){
 				result.add(d);
 			}
 		}
 		return result;
 	}
 	
 	
 
 }
