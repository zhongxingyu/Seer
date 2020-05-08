 package quiz.meal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import quiz.meal.model.Food;
 import quiz.meal.model.Item;
 import quiz.meal.model.Meal;
 
 public class OrderHelper {
     /**
      * Return the actual food represented by the input order items.
      * 
      * If input Items contains Food, add the food to return list. If input items
      * contains Meal, return the Food inside the meal in the return list.
      * 
     * @param List of order item, could be Food or Msal
      * @return the actual food represented by the input order items.
      */
     public static List<Food> getItemAsFoodList(List<? extends Item> items) {
         List<Food> itemList = new ArrayList<Food>();
         for (Item i : items) {
             if (i instanceof Food) {
                 itemList.add((Food) i);
             } else if (i instanceof Meal) {
                 List<Food> mealFood = ((Meal) i).getFood();
                 itemList.addAll(getItemAsFoodList(mealFood));
             } else {
                 throw new AssertionError("Unexpected type");
             }
         }
         return itemList;
     }
 
     /**
      * Count all items in the input list
      * 
      * @param items
      * @return
      */
     public static Map<Item, Integer> getItemCount(List<? extends Item> items) {
         Map<Item, Integer> itemCount = new HashMap<Item, Integer>();
         for (Item i : items) {
             if (!itemCount.containsKey(i)) {
                 itemCount.put(i, 1);
             } else {
                 itemCount.put(i, itemCount.get(i) + 1);
             }
         }
         return itemCount;
     }
 }
