 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.StringTokenizer;
 
 import android.content.res.Resources;
 
 /**
  * Class to abstract the FoodItem generation procedure to an iterator-style interface.
  * Successive calls to the nextFood method will return all of the food items in the resources
  * directory in random order.
  * @author Paul M. Gurniak
  * @version 1.0
  *
  */
 public class FoodGenerator {
 	
 	private LinkedList<FoodItem> unseenItems;
 	private LinkedList<FoodItem> seenItems;
 	private Random rand;
 	
 	/**
 	 * Create a new instance of FoodGenerator using all of the resources
 	 * in this application.  Initializes all FoodItems as unseen.
 	 * @param res Reference to the Resources object for this application
 	 */
 	public FoodGenerator(Resources res) {
 		rand = new Random();
 		unseenItems = new LinkedList<FoodItem>();
 		seenItems = new LinkedList<FoodItem>();
 		FoodItem fries1 = new FoodItem(res.getDrawable(R.drawable.mcfries),
 				res.getString(R.string.mcfries),
 				475, 525, 25);
 		fries1.setShortName(res.getString(R.string.mcfries_simple));
 		FoodItem fries2 = new FoodItem(res.getDrawable(R.drawable.bkfries),
 				res.getString(R.string.bkfries),
 				325, 375, 25);
 		fries2.setShortName(res.getString(R.string.bkfries_simple));
 		FoodItem apple = new FoodItem(res.getDrawable(R.drawable.redapple),
 				res.getString(R.string.redapple),
 				50, 90, 10);
 		apple.setShortName(res.getString(R.string.redapple_simple));
 		FoodItem banana = new FoodItem(res.getDrawable(R.drawable.banana),
 				res.getString(R.string.banana),
 				80, 110, 15);
 		banana.setShortName(res.getString(R.string.banana_simple));
 		FoodItem milkglass = new FoodItem(res.getDrawable(R.drawable.milkglass),
 				res.getString(R.string.milkglass),
 				80, 120, 20);
 		milkglass.setShortName(res.getString(R.string.milkglass_simple));
 		FoodItem waterglass = new FoodItem(res.getDrawable(R.drawable.waterglass),
 				res.getString(R.string.waterglass),
 				0, 0, 0);
 		waterglass.setShortName(res.getString(R.string.waterglass_simple));
 		FoodItem cokecan = new FoodItem(res.getDrawable(R.drawable.cokecan),
 				res.getString(R.string.cokecan),
 				110, 130, 10);
 		cokecan.setShortName(res.getString(R.string.cokecan_simple));
 		FoodItem tacobellburrito = new FoodItem(res.getDrawable(R.drawable.tacobellburrito),
 				res.getString(R.string.tacobellburrito),
 				525, 575, 25);
 		tacobellburrito.setShortName(res.getString(R.string.tacobellburrito_simple));
 		
 		FoodItem sliceofbread = new FoodItem(res.getDrawable(R.drawable.sliceofbread),
 				res.getString(R.string.sliceofbread),
 				40,70,10);//make sure this is correct 80cal
 		sliceofbread.setShortName(res.getString(R.string.sliceofbread_simple));
 
 		FoodItem broccoli = new FoodItem(res.getDrawable(R.drawable.broccoli),
 				res.getString(R.string.broccoli),
 				50,90,10);//make sure this is right 90 cal
 		broccoli.setShortName(res.getString(R.string.broccoli_simple));
 
 		FoodItem cupofrice = new FoodItem(res.getDrawable(R.drawable.cupofrice),
 				res.getString(R.string.cupofrice),
				250,200,20);
 		cupofrice.setShortName(res.getString(R.string.cupofrice_simple));
 
 					
 		unseenItems.add(fries1);
 		unseenItems.add(fries2);
 		unseenItems.add(apple);
 		unseenItems.add(banana);
 		unseenItems.add(milkglass);
 		unseenItems.add(waterglass);
 		unseenItems.add(cokecan);
 		unseenItems.add(tacobellburrito);
 		unseenItems.add(sliceofbread);
 		unseenItems.add(broccoli);
 		unseenItems.add(cupofrice);
 	}
 	
 	
 	/**
 	 * Checks to see if there are additional food items that are yet to be provided by nextFood
 	 * @return True if there are still food items remaining, false if not
 	 */
 	public boolean hasNextFood() {
 		return !unseenItems.isEmpty();
 	}
 	
 	/**
 	 * Returns the next food item (determined randomly from those remaining), or null if there aren't any left
 	 * @return The next FoodItem
 	 */
 	public FoodItem nextFood() {
 		if(unseenItems.isEmpty()) {
 			return null;
 		}
 		int selection = rand.nextInt(unseenItems.size());
 		FoodItem nextFood = unseenItems.remove(selection);
 		seenItems.add(nextFood);
 		return nextFood;
 	}
 	
 	/**
 	 * Resets the generator to consider all food items as unseen.
 	 */
 	public void reset() {
 		unseenItems.addAll(seenItems);
 		seenItems.clear();
 	}
 	
 	/**
 	 * Generates a persistence string for the list of foods
 	 * @param foods List of foods to preserve in-order
 	 * @return the persistence string for the given list of foods
 	 */
 	public String getStateString(List<FoodItem> foods) {
 		if(foods == null) {
 			return null;
 		}
 		StringBuffer state = new StringBuffer();
 		for(int i = 0; i < foods.size(); i++) {
 			state.append(foods.get(i).getShortName());
 			state.append(';');
 		}
 		return state.toString();
 	}
 	
 	/**
 	 * Returns an ArrayList representing the state specified by the provided persistence string
 	 * @param state The persistence string generated by this class
 	 * @return
 	 */
 	public ArrayList<FoodItem> restoreState(String state) {
 		StringTokenizer tokens = new StringTokenizer(state, ";");
 		ArrayList<FoodItem> result = new ArrayList<FoodItem>();
 		while(tokens.hasMoreTokens()) {
 			FoodItem food = getFood(tokens.nextToken());
 			if(food == null) {
 				return null;
 			}
 			result.add(food);
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns the FoodItem corresponding to the given short name
 	 * @param name the String representing the "short name" of this food
 	 * @return the FoodItem requested, or null if no item matches
 	 */
 	public FoodItem getFood(String name) {
 		for(int i = 0; i < unseenItems.size(); i++) {
 			if(name.equals(unseenItems.get(i).getShortName())) {
 				return unseenItems.get(i);
 			}
 		}
 		for(int i = 0; i < seenItems.size(); i++) {
 			if(name.equals(seenItems.get(i).getShortName())) {
 				return seenItems.get(i);
 			}
 		}
 		return null;
 	}
 
 }
