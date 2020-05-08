 package quiz.meal.simple;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import quiz.meal.OrderMaker;
 import quiz.meal.SimpleMenu;
 import quiz.meal.model.Item;
 import quiz.meal.model.Meal;
 
/**
 * 
 * @author Jacky See
 * Making orders arranged by worthies meal
 */
 public class PlainOrderMaker implements OrderMaker {
 	
 	private static SimpleMenu menu = new SimpleMenu();
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Item> order(Item... wantedItems) {
 		
 		List<Item> resultItems = new ArrayList<Item>();
 		List<Item> remainingFoods = (List<Item>) new ArrayList<Item>(Arrays.asList(wantedItems)).clone();
 		
 		Meal meal = getWorthiestMeal(remainingFoods);
 		while(meal != null){
 			resultItems.add(meal);
			subtractMealFoodFromItemList(meal, remainingFoods);
 			meal = getWorthiestMeal(remainingFoods);
 		}
 		
 		resultItems.addAll(remainingFoods);
 		return resultItems;
 	}
 	
 	
 	public Meal getWorthiestMeal(List<Item> wantedItems){
 		Meal worthiestMeal = null;
 		for (Item item : menu.getAllItems().values()) {
 			if(item instanceof Meal){
 				Meal currentMeal = (Meal) item;
 				if(currentMeal.matchItems(wantedItems)){
 					if(worthiestMeal == null || 
 							(currentMeal.getMoneySaved() > worthiestMeal.getMoneySaved())){
 						worthiestMeal = currentMeal;
 					}
 				}
 			}
 		}
 		return worthiestMeal;
 	}
 	
	public void subtractMealFoodFromItemList(Meal meal, List<Item> items){
 		for (Item item : meal.getFood()) {
 			items.remove(item);
 		}
 	}
 	
 }
