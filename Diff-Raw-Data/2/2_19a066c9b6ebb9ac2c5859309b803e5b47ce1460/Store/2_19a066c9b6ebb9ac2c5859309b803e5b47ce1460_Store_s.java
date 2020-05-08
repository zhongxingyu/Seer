 package com.example.coverflow.producealmanac;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 
 public class Store {
 	/**
 	 * Each instance holds data for an individual grocery store or market.
 	 * Holds information about the store's location and 
 	 **/
 	
 	public static HashMap<String,Store> storeMap;
 	public static HashMap<Store,ArrayList<Item>> activeMap;
 	public static HashMap<Store,ArrayList<Item>> inactiveMap;
 	private static boolean populated=false;
 	private String storeName;
 	private ArrayList<Item> active; 
 	private String address;
 	private ArrayList<Item> inactive;
 	
 	
 	
 	
 	public Store(String storeName){
 		if (!populated){
 			populated=true;
 			storeMap = new HashMap<String,Store>();
 			activeMap = new HashMap<Store,ArrayList<Item>>();
 			inactiveMap = new HashMap<Store,ArrayList<Item>>(); 
 		}
 		this.storeName = storeName;
 		this.active = new ArrayList<Item>();
 		this.inactive = new ArrayList<Item>();
 		Store.storeMap.put(storeName, this);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void sort(){
 		Collections.sort(active);
 		Collections.sort(inactive);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ArrayList<Item> getAllActiveItems(){
 		this.sort();
 		return (ArrayList<Item>) active.clone();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ArrayList<Item> getAllInactiveItems(){
 		this.sort();
 		return (ArrayList<Item>) inactive.clone();
 	}
 	
 	private void addActiveItem(String itemName){
 		/**Adds Item with itemName to active if it 
 		 * is not already in the list. 
 		 */
 		Item item = Item.itemMap.get(itemName);
 		if ( ! this.active.contains(item)){
 			this.active.add(item);
 		}
 	}
 	
 	
 	private void addActiveItems(ArrayList<String> items){
 		/** Adds a list of items to active using addActiveItem() **/
 		for (String name : items){
 			this.addActiveItem(name);
 		}
 	}
 	
 	private void addInactiveItem(String name){
 		/**Adds Item with "name" to inactive if it 
 		 * is not already in the list. 
 		 */
 		Item item = Item.itemMap.get(name);
 		if ( ! this.inactive.contains(item)){
 			this.inactive.add(item);
 		}
 
 	}
 	
 	private void addInactiveItems(ArrayList<String> items){
 		/** Adds a list of items to inactive using addInactiveItem() **/
 		for (String name : items){
 			this.addInactiveItem(name);
 		}
 	}
 	
 	
 	public void setActiveItems(ArrayList<String> items){
 		/** Completely replaces current list of active with this list **/
 		this.active = new ArrayList<Item>();
 		addActiveItems(items);
 		this.sort();
 	}
 	
 	
 			
 	
 	public void setInactiveItems(ArrayList<String> items){
 		/** Completely replaces inactive list of items with this list **/
 		this.inactive = new ArrayList<Item>();
 		addInactiveItems(items);
 		this.sort();
 	}
 	
 	public void putInSeason(ArrayList<String> names){
 		/**Puts the current list of items "in season" for this store.
 		 * Calls the overridden putInSeason(String) method for each name.
 		 */
 		for (String name: names){
 			putInSeason(name);
 		}
 	}
 	
 	public void putInSeason(String name){
 		/**Puts the the item with "name" in season.
 		 * If the item is already in the active list, no changes are made.
 		 * If it is in the inactive list, it is moved to the active list.
 		 * If it is in neither, it is added to the active list.
 		 */
 		Item item = Item.itemMap.get(name);
 		if ( ! this.active.contains(item)){
 			this.active.add(item);
 		}
 		this.inactive.remove(item);
 	}
 	
 	public void takeOutOfSeason(ArrayList<String> names){
 		/**Puts the current list of items in the inactive list for this store.
 		 * Calls the overridden takeOutOfSeason(String) method for each name.
 		 */
 		for (String name: names){
 			takeOutOfSeason(name);
 		}
 	}
 	
 	public void takeOutOfSeason(String name){
 		/**Takes the item with "name" out of season.
 		 * If the item is already in the inactive list, no changes are made.
 		 * If it is in the active list, it is moved to the inactive list.
 		 * If it is in neither, it is added to the inactive list.
 		 */
 		Item item = Item.itemMap.get(name);
 		if ( ! this.inactive.contains(item)){
 			this.inactive.add(item);
 		}
 		this.active.remove(item);
 	}
 	
 	public void removeFromInventory(String name){
 		/**Removes item with name from both active and inactie lists**/
 		Item item = Item.itemMap.get(name);
 		this.active.remove(item);
 		this.inactive.remove(item);
 	}
 	
 	public void removeFromInventory(ArrayList<String> names){
 		/**Removes all items with name in "names" from both 
 		 * active and inactive lists using removeFromInventory(name).
 		 */
 		for(String name : names){
 			removeFromInventory(name);
 		}
 	}
 	
 	
 	
 	public void setAddress(String address){
 		this.address=address;
 	}
 	
 	public String getAddress(){
 		return this.address;
 	}
 	
 	public void setName(String name){
 		this.storeName=name;
 	}
 	
 	public String getName(){
 		return this.storeName;
 	}
 	
 	public boolean hasActive(Item item){
 		/* Returns true if the item is in the active list, false otherwise*/
 		return active.contains(item);
 	}
 	
 	public boolean hasInactive(Item item){
 		/* Returns true if the item is in the inactive list, false otherwise*/
 		return inactive.contains(item);
 	}
 
 	
 	public static void buildMaps(){
 	
 		Store current = Store.storeMap.get(CoverFlowTestingActivity.BERKELEYBOWL);
 		ArrayList<Item> activeItems = new ArrayList<Item>();
 		ArrayList<Item> inactiveItems = new ArrayList<Item>();
 		//String[] berkeleyBowlActive = {"garlic", "carrots", "onion", "parsnip", "broccoli", "cauliflower", "asian greens","lettuce", "spinach", "eggplant", "summer squash", "cucumber", "bell peppers", "asparagus", "celery", "kiwifruit"};//, "strawberries", "blueberries"};
 		String[] berkeleyBowlActive = {"eggplant", "summer squash", "kiwifruit", "strawberries", "blueberries", "cherries", "nectarines", "peaches", "plums", "pluots", "apricots", "onion", "parsnip"};
 		String[] berkeleyBowlInactive = {"garlic", "carrots", "broccoli", "cauliflower", "asian greens", "lettuce", "spinach"};
 		for (String name : berkeleyBowlActive){
 			activeItems.add(Item.itemMap.get(name));			
 		}
 		for (String name : berkeleyBowlInactive){
 			inactiveItems.add(Item.itemMap.get(name));			
 		}
 		
 		Store.activeMap.put(current,activeItems);
 		Store.inactiveMap.put(current,inactiveItems);
 		
 		activeItems = new ArrayList<Item>();
 		inactiveItems = new ArrayList<Item>();
 		
 		current = Store.storeMap.get(CoverFlowTestingActivity.YASAIMARKET);
 		
		String[] yasaiActive ={"garlic", "cauliflower", "asian greens", "lettuce", "spinach", "eggplant",  "asparagus", "celery", "kiwifruit", "strawberries", "blueberries", "cherries", "nectarines", "peaches", "plums", "pluots", "apricots", "onion", "parsnip"}
 ;
 		//String[] yasaiActive = {"garlic", "carrots", "broccoli", "cauliflower", "asian greens"};
 		String[] yasaiInactive = { "carrots", "broccoli","summer squash", };
 		
 		for (String name : yasaiActive){
 			activeItems.add(Item.itemMap.get(name));			
 		}
 		for (String name : yasaiInactive){
 			inactiveItems.add(Item.itemMap.get(name));			
 		}
 		
 		Store.activeMap.put(current,activeItems);
 		Store.inactiveMap.put(current,inactiveItems);
 		
 		activeItems = new ArrayList<Item>();
 		inactiveItems = new ArrayList<Item>();
 		
 		current = Store.storeMap.get(CoverFlowTestingActivity.SAFEWAY);
 		
 		String[] safewayActive= {"garlic", "carrots", "onion","plums"};
 		//String[] safewayActive = {"garlic", "carrots", "broccoli", "cauliflower", "asian greens"};
 		String[] safewayInactive = {"summer squash"};
 		
 		for (String name : safewayActive){
 			activeItems.add(Item.itemMap.get(name));			
 		}
 		for (String name : safewayInactive){
 			inactiveItems.add(Item.itemMap.get(name));			
 		}
 		
 		Store.activeMap.put(current,activeItems);
 		Store.inactiveMap.put(current,inactiveItems);
 		
 		activeItems = new ArrayList<Item>();
 		inactiveItems = new ArrayList<Item>();
 									 
 		current = Store.storeMap.get(CoverFlowTestingActivity.TRADERJOES);
 		
 		//String[] traderJoesActive 	= {"garlic", "carrots", "onion"};
 		String[] traderJoesActive = { "broccoli", "cauliflower", "asian greens","lettuce", "spinach", "eggplant",  "peaches", "plums","nectarines"};
 		String[] traderJoesInactive = {"garlic", "carrots",};
 		
 		for (String name : traderJoesActive){
 			activeItems.add(Item.itemMap.get(name));			
 		}
 		for (String name : traderJoesInactive){
 			inactiveItems.add(Item.itemMap.get(name));			
 		}
 		System.out.println("Trader Joes: " + current);
 		System.out.println("ActiveJoes " + activeItems.size());
 		System.out.println("InactiveJoes " + inactiveItems.size());
 		
 		Store.activeMap.put(current,activeItems);
 		Store.inactiveMap.put(current,inactiveItems);
 		
 		activeItems = new ArrayList<Item>();
 		inactiveItems = new ArrayList<Item>();
 		
 		current = Store.storeMap.get(CoverFlowTestingActivity.WHOLEFOODS);
 		
 		String[] wholeFoodsActive = {"carrots", "onion", "strawberries", "blueberries", "cherries", "nectarines"};
 		//String[] wholeFoodsActive = {"garlic", "carrots", "broccoli", "cauliflower", "asian greens"};
 		String[] wholeFoodsInactive = {"lettuce", "spinach", "eggplant"};
 		
 		for (String name : wholeFoodsActive){
 			activeItems.add(Item.itemMap.get(name));			
 		}
 		for (String name : wholeFoodsInactive){
 			inactiveItems.add(Item.itemMap.get(name));			
 		}
 		
 		Store.activeMap.put(current,activeItems);
 		Store.inactiveMap.put(current,inactiveItems);
 		
 		
 		System.out.println("SIZE: " + activeItems.size());
 		System.out.println("Stores: " + Store.storeMap.values());
 		//Set each store's internal lists
 		ArrayList<Item> currentItems;
 		Collection<Store> stores = Store.storeMap.values();
 		System.out.println("Stores: " + stores.size());
 		for (Store store : stores){
 			currentItems = Store.activeMap.get(store);	
 			System.out.println("CURRENT = " + currentItems);
 			System.out.println("Store: " + store);
 			if (currentItems!=null){
 			for (Item i : currentItems){
 				store.addActiveItem(i.name);
 			}
 			}
 			currentItems = Store.inactiveMap.get(store);
 			if (currentItems!=null){
 			for (Item i : currentItems){
 				store.addInactiveItem(i.name);
 			}
 			}
 		}
 
 		
 		
 	}
 	
 	public String toString(){
 		return this.getName();
 	}
 }
