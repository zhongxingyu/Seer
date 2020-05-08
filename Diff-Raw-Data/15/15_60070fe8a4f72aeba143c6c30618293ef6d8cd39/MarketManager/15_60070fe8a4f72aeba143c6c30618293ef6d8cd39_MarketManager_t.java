 package com.example.freshfoodfinder;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class MarketManager {
 
 	private ArrayList<Food> superMarketFoods;
 	private ArrayList<Food> wawaFoods;
 	private ArrayList<Food> cornerStoreFoods;
 	private ArrayList<Food> farmerMarketFoods;
 	private ArrayList<Food> alDiFoods;
 	
 	private ArrayList<Market> superMarkets;
 	private ArrayList<Market> wawas;
 	private ArrayList<Market> cornerStores;
 	private ArrayList<Market> farmerMarkets;
 	
 	//We'll still instiate Aldi as a list so we dont have to treat it specially
 	private ArrayList<Market> aldi;
 	
 	public MarketManager() {
 		superMarketFoods = new ArrayList<Food>();
 		wawaFoods = new ArrayList<Food>();
 		cornerStoreFoods = new ArrayList<Food>();
 		farmerMarketFoods = new ArrayList<Food>();
 		alDiFoods = new ArrayList<Food>();
 		
 		superMarkets = new ArrayList<Market>();
 		wawas = new ArrayList<Market>();
 		cornerStores = new ArrayList<Market>();
 		farmerMarkets = new ArrayList<Market>();
 		aldi = new ArrayList<Market>();
 		
 		
 		instantiatesuperMarketFoods();
 		instantiateWawaFoods();
 		instantiateCornerStoreFoods();
 		instantiateFarmerMarketFoods();
 		instantiateAldiFoods();
 		
 		instantiateSuperMarkets();
 		instantiateWawas();
 		instantiateCornerStores();
 		instantiateFarmerMarkets();
 		instantiateAldi();
 
 	}
 	
 	//This method will be called by UserMap
 	public ArrayList<Market> getAllMarkets() {
 		ArrayList<Market> allMarkets = new ArrayList<Market>();
 		allMarkets.addAll(superMarkets);
 		allMarkets.addAll(wawas);
 		allMarkets.addAll(cornerStores);
 		allMarkets.addAll(farmerMarkets);
 		
 		return allMarkets;
 	}
 	private void instantiatesuperMarketFoods() {
 
 		Food apple = new Food("Apple",1, Calendar.JANUARY, Calendar.DECEMBER);
 		superMarketFoods.addAll(apple.getAllFoods());
 	}
 	
 	private void instantiateWawaFoods() {
 		Food banana = new Food("Banana",1, Calendar.JANUARY, Calendar.DECEMBER);
 		wawaFoods.add(banana);
 	}
 
 	private void instantiateCornerStoreFoods() {
		cornerStoreFoods.addAll(Food.getAllFoods());
 	}
 	
 	private void instantiateFarmerMarketFoods() {
 		
 	}
 	
 	private void instantiateAldiFoods() {
 	
 		alDiFoods.add(new Food("Asparagus", Food.FOOD_VEG, Calendar.MAY, Calendar.JUNE));
 		alDiFoods.add(new Food("Arugula", Food.FOOD_VEG, Calendar.MAY, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Bell pepper", Food.FOOD_VEG, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Broccoli", Food.FOOD_VEG, Calendar.JUNE, Calendar.NOVEMBER));
 		alDiFoods.add(new Food("Cabbage", Food.FOOD_VEG, Calendar.JUNE, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Carrot", Food.FOOD_VEG, Calendar.JUNE, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Cauliflower", Food.FOOD_VEG, Calendar.AUGUST, Calendar.NOVEMBER));
 		alDiFoods.add(new Food("Celery", Food.FOOD_VEG, Calendar.AUGUST, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Cucumber", Food.FOOD_VEG, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Garlic", Food.FOOD_VEG, Calendar.JULY, Calendar.OCTOBER));//STORED YEAR ROUND THOUGH
 		alDiFoods.add(new Food("Green Beans", Food.FOOD_VEG, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Lettuce", Food.FOOD_VEG, Calendar.MAY, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Onion", Food.FOOD_VEG, Calendar.JULY, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Pumpkin", Food.FOOD_VEG, Calendar.SEPTEMBER, Calendar.NOVEMBER));
 		alDiFoods.add(new Food("Potato", Food.FOOD_VEG, Calendar.JULY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Spinach", Food.FOOD_VEG, Calendar.MAY, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Sweet corn", Food.FOOD_VEG, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Sweet pepper", Food.FOOD_VEG, Calendar.JULY, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Sweet potato", Food.FOOD_VEG, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Radish", Food.FOOD_VEG, Calendar.MAY, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Tomato", Food.FOOD_VEG, Calendar.JULY, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Yam", Food.FOOD_VEG, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Zucchini", Food.FOOD_VEG, Calendar.JULY, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Apple", Food.FOOD_FRUIT, Calendar.JULY, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Avocado", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Banana", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Blackberry", Food.FOOD_FRUIT, Calendar.JULY, Calendar.AUGUST));
 		alDiFoods.add(new Food("Blueberry", Food.FOOD_FRUIT, Calendar.JULY, Calendar.AUGUST));
 		alDiFoods.add(new Food("Cantaloupe", Food.FOOD_FRUIT, Calendar.AUGUST, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Clementine", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Grape", Food.FOOD_FRUIT, Calendar.SEPTEMBER, Calendar.OCTOBER));
 		alDiFoods.add(new Food("Grapefruit", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Kiwi fruit", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Lemon", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Lime", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Mango", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Nectarine", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Orange", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Peach", Food.FOOD_FRUIT, Calendar.JULY, Calendar.SEPTEMBER));
 		alDiFoods.add(new Food("Pear", Food.FOOD_FRUIT, Calendar.AUGUST, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Plum", Food.FOOD_FRUIT, Calendar.AUGUST, Calendar.SEPTEMBER));	
 		alDiFoods.add(new Food("Pineapple", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Raspberry", Food.FOOD_FRUIT, Calendar.JANUARY, Calendar.DECEMBER));
 		alDiFoods.add(new Food("Strawberry", Food.FOOD_FRUIT, Calendar.JUNE, Calendar.JUNE));
 	}
 	
 	private void instantiateSuperMarkets() {
 		
 		//19104 -no shoprite, bottom dollar
 		LatLng freshGrocerLocation40thWalnut = new LatLng(39.954499,-75.202864);
 		LatLng supreme43rdWalnutLocation = new LatLng(39.954792,-75.208733);
 		
 		Market freshGrocer40thWalnut = new Market("FreshGrocer40thWalnut",superMarketFoods,freshGrocerLocation40thWalnut);
 		Market supreme43rdWalnut = new Market("Supreme43rdWalnut",superMarketFoods,supreme43rdWalnutLocation);
 		
 		superMarkets.add(freshGrocer40thWalnut);
 		superMarkets.add(supreme43rdWalnut);
 			
 		//19143 -no shoprite, bottomdollar		
 		//19142 - no shoprite, bottomdollar
 		//19153
 		LatLng shoprite2946IslandAvenueLocation = new LatLng(39.902948,-75.239991); 
 		LatLng bottomdollar7627LindberghBlvdLocation = new LatLng(40.034204,-75.210819); 
 		
 		Market shoprite2946IslandAvenue = new Market("shoprite2946IslandAvenue",superMarketFoods,shoprite2946IslandAvenueLocation);
 		Market bottomdollar7627LindberghBlvd = new Market("bottomdollar7627LindberghBlvd",superMarketFoods,bottomdollar7627LindberghBlvdLocation);
 
 		superMarkets.add(shoprite2946IslandAvenue);
 		superMarkets.add(bottomdollar7627LindberghBlvd);
 		
 		//19145
 		LatLng shoprite24thOregonAveLocation = new LatLng(39.919672,-75.188367);
 		
 		Market shoprite24thOregonAve = new Market("shoprite24thOregonAve",superMarketFoods,shoprite24thOregonAveLocation);
 		
 		superMarkets.add(shoprite24thOregonAve);
 		
 		//19112 		
 		//19146 
 		//19130 - no shoprite
 		LatLng bottomdollarNorth31thGirardAveLocation = new LatLng(39.97495,-75.186679);
 		
 		Market bottomdollarNorth31thGirardAve = new Market("bottomdollarNorth31thGirardAve",superMarketFoods,bottomdollarNorth31thGirardAveLocation);
 		
 		superMarkets.add(bottomdollarNorth31thGirardAve);
 		
 		//19121 - 
 		
 		//19131 - no bottomdollar
 		LatLng shoprite575North52ndLocation = new LatLng(39.96906,-75.225295);
 		
 		Market shoprite575North52nd = new Market("shoprite575North52nd",superMarketFoods,shoprite575North52ndLocation);
 		
 		superMarkets.add(shoprite575North52nd);
 		
 		//19139 - 
 	}
 	
 	private void instantiateWawas() {
 		LatLng f46thandChestnutWawaLocation = new LatLng(39.954687,-75.194799);
 		Market f46thandChestnutWawa = new Market("f46thandChestnutWawa",wawaFoods,f46thandChestnutWawaLocation );
 		wawas.add(f46thandChestnutWawa);
 		LatLng f37thandSpruceWawaLocation = new LatLng(39.951124,-75.198862);
 		Market f37thandSpruceWawa = new Market("f37thandSpruceWawa",wawaFoods,f37thandSpruceWawaLocation );
 		wawas.add(f37thandSpruceWawa);
 		LatLng bartramAveWawaLocation = new LatLng(39.894628,-75.236559);
 		Market bartramAveWawa = new Market("bartramAveWawa",wawaFoods,bartramAveWawaLocation );
 		wawas.add(f37thandSpruceWawa);
 
 	}
 	
 	private void instantiateCornerStores() {
 		
 		//all stores from 19139
 		LatLng toresCornerStoreLocation = new LatLng(39.956242,-75.21777);
 		Market toresCornerStore = new Market("toresCornerStore",cornerStoreFoods,toresCornerStoreLocation );
 		cornerStores.add(toresCornerStore);
 		LatLng conniesCornerStoreLocation = new LatLng(39.957013,-75.22506);
 		Market conniesCornerStore = new Market("conniesCornerStore",cornerStoreFoods,conniesCornerStoreLocation );
 		cornerStores.add(conniesCornerStore);
 		LatLng MontesinosCornerStoreLocation = new LatLng(39.959126,-75.228789);
 		Market MontesinosCornerStore = new Market("MontesinosCornerStore",cornerStoreFoods,MontesinosCornerStoreLocation );
 		cornerStores.add(MontesinosCornerStore);
 		LatLng RodriguezCornerStoreLocation = new LatLng(39.961881,-75.22627);
 		Market RodriguezCornerStore = new Market("RodriguezCornerStore",cornerStoreFoods,RodriguezCornerStoreLocation );
 		cornerStores.add(RodriguezCornerStore);
 		LatLng GregoryCornerStoreLocation = new LatLng(39.961399,-75.221662);
 		Market GregoryCornerStore = new Market("GregoryCornerStore",cornerStoreFoods,GregoryCornerStoreLocation );
 		cornerStores.add(GregoryCornerStore);
 		LatLng fiftyOneRaceCornerStoreLocation = new LatLng(39.963041,-75.222338);
 		Market fiftyOneRaceCornerStore = new Market("fiftyOneRaceCornerStore",cornerStoreFoods,fiftyOneRaceCornerStoreLocation );
 		cornerStores.add(fiftyOneRaceCornerStore);
 		LatLng PorfiCornerStoreLocation = new LatLng(39.967172,-75.220698);
 		Market PorfiOneRaceCornerStore = new Market("PorfiOneRaceCornerStore",cornerStoreFoods,PorfiCornerStoreLocation );
 		cornerStores.add(PorfiOneRaceCornerStore);
 		//end stores from 19139
 	}
 	
 	private void instantiateFarmerMarkets() {
 		
 	}
 	
 	private void instantiateAldi() {
 		LatLng aldi46thandMarketLocation = new LatLng(39.959169,-75.213727);
 		Market aldi46thandMarket = new Market("aldi46thandMarket",alDiFoods,aldi46thandMarketLocation );
 		aldi.add(aldi46thandMarket);
 
 	}
 }
