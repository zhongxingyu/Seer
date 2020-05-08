 package com.example.freshfoodfinder;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.location.Location;
 
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
 	
 	private Food activeFood;
 	private Location userLocation;
 	private int searchRadius;
 	
 	//We'll still instiate Aldi as a list so we dont have to treat it specially
 	private ArrayList<Market> aldi;
 	
 	public MarketManager(Food food, Location location,int radius) {
 		activeFood = food;
 		userLocation = location;
 		searchRadius = radius;
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
 		
 		wawas = filterMarkets(wawas);
 		aldi = filterMarkets(aldi);
 		superMarkets = filterMarkets(superMarkets);
 		farmerMarkets = filterMarkets(farmerMarkets);
 		cornerStores = filterMarkets(cornerStores);
 
 	}
 	
 	private ArrayList<Market> filterMarkets(ArrayList<Market> markets) {
 		markets = searchMarkets(markets);
 		markets = filterMarketsByDistance(markets);
 		return markets;
 	}
 	
 	//Returns list of markets with the food
 		private ArrayList<Market> searchMarkets(ArrayList<Market> markets) {
 			ArrayList<Market> appropriateMarkets = new ArrayList<Market>();
 			for (Market m : markets) {
 				if (m.hasFood(activeFood)) {
 					appropriateMarkets.add(m);
 				}
 			}
 			return appropriateMarkets;
 		}
 		
 		private ArrayList<Market> filterMarketsByDistance(ArrayList<Market> markets) {
 			//Don't filter if default value zero is given, which indicates no radius limit
 			if (searchRadius == 0) {
 				return markets;
 			}
 			//1609 is 1 mile
 			ArrayList<Market> appropriateMarkets = new ArrayList<Market>();
 			for (Market m : markets) {
 				
 				//1609 is 1 mile
 				if (userLocation.distanceTo(m.getLocation())<searchRadius) {
 
 					appropriateMarkets.add(m);
 				}
 			}
 			return appropriateMarkets;
 	}
 	
 	public ArrayList<Market> getCornerStores() {
 		return cornerStores;
 	}
 	
 	public ArrayList<Market> getWawas() {
 		return wawas;
 	}
 	
 	public ArrayList<Market> getSuperMarkets() {
 		return superMarkets;
 	}
 	
 	public ArrayList<Market> getAldis() {
 		return aldi;
 	}
 	
 	public ArrayList<Market> getFarmerMarkets() {
 		return farmerMarkets;
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
 		superMarketFoods.add(Food.asparagus); 
 		superMarketFoods.add(Food.artichoke); 
 		superMarketFoods.add(Food.arugula ); 
 		superMarketFoods.add(Food.beets ); 
 		superMarketFoods.add(Food.beetroot ); 
 		superMarketFoods.add(Food.bellPepper);  
 		superMarketFoods.add(Food.blackEyedPeas ); 
 		superMarketFoods.add(Food.broccoli ); 
 		superMarketFoods.add(Food.brusselsSprouts); 
 		superMarketFoods.add(Food.cabbage ); 
 		superMarketFoods.add(Food.carrot); 
 		superMarketFoods.add(Food.cauliflower ); 
 		superMarketFoods.add(Food.celery);  
 		superMarketFoods.add(Food.collardGreens); 
 		superMarketFoods.add(Food.cucumber); 
 		superMarketFoods.add(Food.daikon); 
 		superMarketFoods.add(Food.eggplant); 
 		superMarketFoods.add(Food.florenceFennel); 
 		superMarketFoods.add(Food.garlic); 
 		superMarketFoods.add(Food.garbanzo); 
 		superMarketFoods.add(Food.ginger);
 		superMarketFoods.add(Food.greenBeans); 
 		superMarketFoods.add(Food.kale); 
 		superMarketFoods.add(Food.leek);
 		superMarketFoods.add(Food.lentil); 
 		superMarketFoods.add(Food.lettuce); 
 		superMarketFoods.add(Food.limaBean);
 		superMarketFoods.add(Food.mustard); 
 		superMarketFoods.add(Food.okra); 
 		superMarketFoods.add(Food.onion); 
 		superMarketFoods.add(Food.parsnip); 
 		superMarketFoods.add(Food.peas); 
 		superMarketFoods.add(Food.pumpkin);
 		superMarketFoods.add(Food.potato);
 		superMarketFoods.add(Food.scallion);
 		superMarketFoods.add(Food.shallot); 
 		superMarketFoods.add(Food.snapPea); 
 		superMarketFoods.add(Food.snowPea);
 		superMarketFoods.add(Food.soybean);
 		superMarketFoods.add(Food.spinach);
 		superMarketFoods.add(Food.squash); 
 		superMarketFoods.add(Food.sweetCorn); 
 		superMarketFoods.add(Food.sweetPepper);
 		superMarketFoods.add(Food.sweetPotato); 
 		superMarketFoods.add(Food.swissChard);
 		superMarketFoods.add(Food.radish); 
 		superMarketFoods.add(Food.tomato); 
 		superMarketFoods.add(Food.turnip); 
 		superMarketFoods.add(Food.turnipGreens); 
 		superMarketFoods.add(Food.yam); 
 		superMarketFoods.add(Food.zucchini);
 		
 		//Fruits
 		superMarketFoods.add(Food.apple);
 		superMarketFoods.add(Food.apricot);
 		superMarketFoods.add(Food.avocado);
 		superMarketFoods.add(Food.banana);
 		superMarketFoods.add(Food.blackberry);
 		superMarketFoods.add(Food.blueberry);
 		superMarketFoods.add(Food.cantaloupe);
 		superMarketFoods.add(Food.cherry );
 		superMarketFoods.add(Food.clementine);
 		superMarketFoods.add(Food.coconut );
 		superMarketFoods.add(Food.date );
 		superMarketFoods.add(Food.fig );
 		superMarketFoods.add(Food.grape);
 		superMarketFoods.add(Food.grapeFruit);
 		superMarketFoods.add(Food.honeydew);
 		superMarketFoods.add(Food.kiwi);
 		superMarketFoods.add(Food.lemon);
 		superMarketFoods.add(Food.lime);
 		superMarketFoods.add(Food.mango);
 		superMarketFoods.add(Food.nectarine);
 		superMarketFoods.add(Food.orange );
 		superMarketFoods.add(Food.peach);
 		superMarketFoods.add(Food.pear);
 		superMarketFoods.add(Food.plum );	
 		superMarketFoods.add(Food.pineapple );
 		superMarketFoods.add(Food.pomegranate);
 		superMarketFoods.add(Food.raspberry);
 		superMarketFoods.add(Food.strawberry);
 		superMarketFoods.add(Food.tangerine);
 		superMarketFoods.add(Food.watermelon);
 		//superMarketFoods.addAll(Food.getAllFoods());
 	}
 	
 	private void instantiateWawaFoods() {
 		//Food banana = new Food("Banana",1, Calendar.JANUARY, Calendar.DECEMBER);
 		wawaFoods.add(Food.banana);
 	}
 
 	private void instantiateCornerStoreFoods() {
 		cornerStoreFoods.add(Food.banana);
 		cornerStoreFoods.add(Food.kiwi);
 		cornerStoreFoods.add(Food.lemon);
 		cornerStoreFoods.add(Food.lime);
 		cornerStoreFoods.add(Food.orange);
 		cornerStoreFoods.add(Food.beets);
 		cornerStoreFoods.add(Food.bellPepper);
 		cornerStoreFoods.add(Food.blackEyedPeas);
 		cornerStoreFoods.add(Food.brusselsSprouts);
 		cornerStoreFoods.add(Food.collardGreens);
 		cornerStoreFoods.add(Food.cucumber);
 		cornerStoreFoods.add(Food.eggplant);
 		cornerStoreFoods.add(Food.leek);
 		cornerStoreFoods.add(Food.lettuce);
 		cornerStoreFoods.add(Food.mustard);
 		cornerStoreFoods.add(Food.onion);
 		cornerStoreFoods.add(Food.potato);
 		cornerStoreFoods.add(Food.spinach);
 		cornerStoreFoods.add(Food.squash);
 		cornerStoreFoods.add(Food.sweetCorn);
 		cornerStoreFoods.add(Food.sweetPepper);
 		cornerStoreFoods.add(Food.sweetPotato);
 		cornerStoreFoods.add(Food.radish);
 		cornerStoreFoods.add(Food.tomato);
 		cornerStoreFoods.add(Food.yam);
 		cornerStoreFoods.add(Food.zucchini);
 	}
 	
 	private void instantiateFarmerMarketFoods() {
 		//Supermarket foods is the entire list - we will filter seasons from this list.
		farmerMarketFoods = Food.getSeasonalFoods();
 	}
 	
 	private void instantiateAldiFoods() {
 	
 		alDiFoods.add(Food.asparagus);
 		alDiFoods.add(Food.arugula);
 		alDiFoods.add(Food.bellPepper);
 		alDiFoods.add(Food.broccoli);
 		alDiFoods.add(Food.cabbage);
 		alDiFoods.add(Food.carrot);
 		alDiFoods.add(Food.cauliflower);
 		alDiFoods.add(Food.celery);
 		alDiFoods.add(Food.cucumber);
 		alDiFoods.add(Food.garlic);
 		alDiFoods.add(Food.greenBeans);
 		alDiFoods.add(Food.lettuce);
 		alDiFoods.add(Food.onion);
 		alDiFoods.add(Food.pumpkin);
 		alDiFoods.add(Food.potato);
 		alDiFoods.add(Food.spinach);
 		alDiFoods.add(Food.sweetCorn);
 		alDiFoods.add(Food.sweetPepper);
 		alDiFoods.add(Food.sweetPotato);
 		alDiFoods.add(Food.radish);
 		alDiFoods.add(Food.tomato);
 		alDiFoods.add(Food.yam);
 		alDiFoods.add(Food.zucchini);
 		alDiFoods.add(Food.apple);
 		alDiFoods.add(Food.avocado);
 		alDiFoods.add(Food.banana);
 		alDiFoods.add(Food.blackberry);
 		alDiFoods.add(Food.blueberry);
 		alDiFoods.add(Food.cantaloupe);
 		alDiFoods.add(Food.clementine);
 		alDiFoods.add(Food.grape);
 		alDiFoods.add(Food.grapeFruit);
 		alDiFoods.add(Food.kiwi);
 		alDiFoods.add(Food.lemon);
 		alDiFoods.add(Food.lime);
 		alDiFoods.add(Food.mango);
 		alDiFoods.add(Food.nectarine);
 		alDiFoods.add(Food.orange);
 		alDiFoods.add(Food.peach);
 		alDiFoods.add(Food.pear);
 		alDiFoods.add(Food.plum);
 		alDiFoods.add(Food.pineapple);
 		alDiFoods.add(Food.raspberry);
 		alDiFoods.add(Food.strawberry);
 	}
 	
 	private void instantiateSuperMarkets() {
 		
 		//19104 -no shoprite, bottom dollar
 		LatLng freshGrocerLocation40thWalnut = new LatLng(39.954499,-75.202864);
 		LatLng supreme43rdWalnutLocation = new LatLng(39.954792,-75.208733);
 		
 		Market freshGrocer40thWalnut = new Market("Fresh Grocer",superMarketFoods,freshGrocerLocation40thWalnut);
 		freshGrocer40thWalnut.setPhoneNumber("(215) 222-9200");
 		Map<String,String> freshGrocerHours = new HashMap<String,String>();
 		freshGrocerHours.put("Sunday","12:00 AM - 12:00 AM");
 		freshGrocerHours.put("Monday","12:00 AM - 12:00 AM");
 		freshGrocerHours.put("Tuesday","12:00 AM - 12:00 AM");
 		freshGrocerHours.put("Wednesday","12:00 AM - 12:00 AM");
 		freshGrocerHours.put("Thursday","12:00 AM - 12:00 AM");
 		freshGrocerHours.put("Friday","12:00 AM - 12:00 AM");
 		freshGrocerHours.put("Saturday","12:00 AM - 12:00 AM");
 		freshGrocer40thWalnut.setHours(freshGrocerHours);
 		
 		Market supreme43rdWalnut = new Market("Supreme Shop N Bag",superMarketFoods,supreme43rdWalnutLocation);
 		Map<String,String> supreme43rdWalnutHours = new HashMap<String,String>();
 		supreme43rdWalnut.setPhoneNumber("(215) 387-3070");
 		supreme43rdWalnutHours.put("Sunday","8:00 AM - 10:00 PM");
 		supreme43rdWalnutHours.put("Monday","7:00 AM - 11:00 PM");
 		supreme43rdWalnutHours.put("Tuesday","7:00 AM - 11:00 PM");
 		supreme43rdWalnutHours.put("Wednesday","7:00 AM - 11:00 PM");
 		supreme43rdWalnutHours.put("Thursday","7:00 AM - 11:00 PM");
 		supreme43rdWalnutHours.put("Friday","7:00 AM - 11:00 PM");
 		supreme43rdWalnutHours.put("Saturday","7:00 AM - 11:00 PM");
 		supreme43rdWalnut.setHours(supreme43rdWalnutHours);
 		
 		superMarkets.add(freshGrocer40thWalnut);
 		superMarkets.add(supreme43rdWalnut);
 			
 		//19143 -no shoprite, bottomdollar		
 		//19142 - no shoprite, bottomdollar
 		//19153
 		LatLng shoprite2946IslandAvenueLocation = new LatLng(39.902948,-75.239991); 
 		LatLng bottomdollar7627LindberghBlvdLocation = new LatLng(40.034204,-75.210819); 
 		
 		Market shoprite2946IslandAvenue = new Market("ShopRite",superMarketFoods,shoprite2946IslandAvenueLocation);
 		Market bottomdollar7627LindberghBlvd = new Market("bottomdollar7627LindberghBlvd",superMarketFoods,bottomdollar7627LindberghBlvdLocation);
 
 		shoprite2946IslandAvenue.setPhoneNumber("(215) 937-0327");
 		Map<String,String> shoprite2946Hours = new HashMap<String,String>();
 		shoprite2946Hours.put("Sunday","9:00 AM - 4:00 PM");
 		shoprite2946Hours.put("Monday","9:00 AM - 9:00 PM");
 		shoprite2946Hours.put("Tuesday","9:00 AM - 9:00 PM");
 		shoprite2946Hours.put("Wednesday","9:00 AM - 9:00 PM");
 		shoprite2946Hours.put("Thursday","9:00 AM - 9:00 PM");
 		shoprite2946Hours.put("Friday","9:00 AM - 9:00 PM");
 		shoprite2946Hours.put("Saturday","9:00 AM - 9:00 PM");
 		shoprite2946IslandAvenue.setHours(shoprite2946Hours);
 		
 		superMarkets.add(shoprite2946IslandAvenue);
 		superMarkets.add(bottomdollar7627LindberghBlvd);
 		
 		//19145
 		LatLng shoprite24thOregonAveLocation = new LatLng(39.919672,-75.188367);
 		
 		Market shoprite24thOregonAve = new Market("ShopRite",superMarketFoods,shoprite24thOregonAveLocation);
 		
 		superMarkets.add(shoprite24thOregonAve);
 		
 		//19112 		
 		//19146 
 		//19130 - no shoprite
 		LatLng bottomdollarNorth31thGirardAveLocation = new LatLng(39.97495,-75.186679);
 		
 		Market bottomdollarNorth31thGirardAve = new Market("Bottom Dollar",superMarketFoods,bottomdollarNorth31thGirardAveLocation);
 		
 		superMarkets.add(bottomdollarNorth31thGirardAve);
 		
 		//19121 - 
 		
 		//19131 - no bottomdollar
 		LatLng shoprite575North52ndLocation = new LatLng(39.96906,-75.225295);
 		
 		Market shoprite575North52nd = new Market("ShopRite",superMarketFoods,shoprite575North52ndLocation);
 		
 		superMarkets.add(shoprite575North52nd);
 		
 		//19139 - 
 	}
 	
 	private void instantiateWawas() {
 		LatLng f46thandChestnutWawaLocation = new LatLng(39.954687,-75.194799);
 		Market f46thandChestnutWawa = new Market("Wawa",wawaFoods,f46thandChestnutWawaLocation );
 		wawas.add(f46thandChestnutWawa);
 		LatLng f37thandSpruceWawaLocation = new LatLng(39.951124,-75.198862);
 		Market f37thandSpruceWawa = new Market("Wawa",wawaFoods,f37thandSpruceWawaLocation );
 		wawas.add(f37thandSpruceWawa);
 		LatLng bartramAveWawaLocation = new LatLng(39.894628,-75.236559);
 		Market bartramAveWawa = new Market("Wawa",wawaFoods,bartramAveWawaLocation );
 		wawas.add(bartramAveWawa);
 
 	}
 	
 	private void instantiateCornerStores() {
 		//19145
 		LatLng 	MiPalsDeliCornerStoreLocation = new LatLng(39.911401,-75.174959);
 		Market 	MiPalsDeliCornerStore = new Market("MiPals Deli Corner Store",cornerStoreFoods,MiPalsDeliCornerStoreLocation );
 		cornerStores.add(MiPalsDeliCornerStore);
 		LatLng pastificioCornerStoreLocation = new LatLng(39.92225,-75.173702);
 		Market pastificioCornerStore = new Market("Pastificio Corner Store",cornerStoreFoods,pastificioCornerStoreLocation );
 		cornerStores.add(pastificioCornerStore);
 		LatLng BarbaroCornerStoreLocation = new LatLng(39.922046,-75.180535);
 		Market BarbaroCornerStore = new Market("Barbaro Corner Store",cornerStoreFoods,BarbaroCornerStoreLocation );
 		cornerStores.add(BarbaroCornerStore);
 		LatLng NutsAboutYouCornerStoreLocation = new LatLng(39.924072,-75.170111);
 		Market NutsAboutYouCornerStore = new Market("Nuts About You",cornerStoreFoods,NutsAboutYouCornerStoreLocation );
 		cornerStores.add(NutsAboutYouCornerStore);
 		LatLng _16thStreetFoodMarketCornerStoreLocation = new LatLng(39.926087,-75.172585);
 		Market _16thStreetFoodMarketCornerStore = new Market("16th Street Food Market CornerStore",cornerStoreFoods,_16thStreetFoodMarketCornerStoreLocation );
 		cornerStores.add(_16thStreetFoodMarketCornerStore);
 		LatLng SnyderSupermarketCornerStoreLocation = new LatLng(39.927104,-75.191017);
 		Market SnyderSupermarketCornerStore = new Market("Snyder Supermarket",cornerStoreFoods,SnyderSupermarketCornerStoreLocation );
 		cornerStores.add(SnyderSupermarketCornerStore);
 		LatLng _28andSnyderSupermarketCornerStoreLocation = new LatLng(39.92714,-75.192751);
 		Market _28andSnyderSupermarketCornerStore = new Market("Snyder Supermarket",cornerStoreFoods,_28andSnyderSupermarketCornerStoreLocation );
 		cornerStores.add(_28andSnyderSupermarketCornerStore);
 		//LatLng _16thStreetFoodMarketCornerStoreLocation = new LatLng(39.926087,-75.172585);
 		//Market _16thStreetFoodMarketCornerStore = new Market("_16thStreetFoodMarketCornerStore",cornerStoreFoods,_16thStreetFoodMarketCornerStoreLocation );
 		//cornerStores.add(_16thStreetFoodMarketCornerStore);
 		LatLng TcGroceryLlc1CornerStoreLocation = new LatLng(39.926688,-75.177303);
 		Market TcGroceryLlc1CornerStore = new Market("TcGroceryLlc1CornerStore",cornerStoreFoods,TcGroceryLlc1CornerStoreLocation );
 		cornerStores.add(TcGroceryLlc1CornerStore);		
 		LatLng CruzBrotherMiniMarketCornerStoreLocation = new LatLng(39.926891,-75.178874);
 		Market CruzBrotherMiniMarketCornerStore = new Market("CruzB rothers Mini Market",cornerStoreFoods,CruzBrotherMiniMarketCornerStoreLocation );
 		cornerStores.add(CruzBrotherMiniMarketCornerStore);	
 		LatLng MartinezGroceryCornerStoreLocation = new LatLng(39.927138,-75.18208);
 		Market MartinezGroceryCornerStore = new Market("Martinez Grocery Corner Store",cornerStoreFoods,MartinezGroceryCornerStoreLocation );
 		cornerStores.add(MartinezGroceryCornerStore);		
 		LatLng CruzsGroceryCornerStoreLocation = new LatLng(39.927572,-75.175518);
 		Market CruzsGroceryCornerStore = new Market("Cruz's Grocery Corner Store",cornerStoreFoods,CruzsGroceryCornerStoreLocation );
 		cornerStores.add(CruzsGroceryCornerStore);		
 		LatLng RamosFoodMarketCornerStoreLocation = new LatLng(39.928183,-75.176867);
 		Market RamosFoodMarketCornerStore = new Market("Ramos Food Market Corner Store",cornerStoreFoods,RamosFoodMarketCornerStoreLocation );
 		cornerStores.add(RamosFoodMarketCornerStore);	
 		LatLng FernandezMiniMarketCornerStoreLocation = new LatLng(39.928801,-75.180638);
 		Market FernandezMiniMarketCornerStore = new Market("FernandezMiniMarketCornerStore",cornerStoreFoods,FernandezMiniMarketCornerStoreLocation );
 		cornerStores.add(FernandezMiniMarketCornerStore);		
 		LatLng CruzandFGroceryCornerStoreLocation = new LatLng(39.928633,-75.182403);
 		Market CruzandFGroceryCornerStore = new Market("Cruz&FGroceryCornerStore",cornerStoreFoods,CruzandFGroceryCornerStoreLocation );
 		cornerStores.add(CruzandFGroceryCornerStore);	
 		LatLng RanacaroGroceryStoreCornerStoreLocation = new LatLng(39.928684,-75.184317);
 		Market RanacaroGroceryStoreCornerStore = new Market("RanacaroGroceryStoreCornerStore",cornerStoreFoods,RanacaroGroceryStoreCornerStoreLocation );
 		cornerStores.add(RanacaroGroceryStoreCornerStore);		
 		LatLng MohamedMarketCornerStoreLocation = new LatLng(39.928715,-75.173332);
 		Market MohamedMarketCornerStore = new Market("MohamedMarketCornerStore",cornerStoreFoods,MohamedMarketCornerStoreLocation );
 		cornerStores.add(MohamedMarketCornerStore);		
 		LatLng NuMiniMarketCornerStoreLocation = new LatLng(39.929497,-75.171801);
 		Market NuMiniMarketCornerStore = new Market("NuMiniMarketCornerStore",cornerStoreFoods,NuMiniMarketCornerStoreLocation );
 		cornerStores.add(NuMiniMarketCornerStore);	
 		LatLng DuranMiniMarketCornerStoreLocation = new LatLng(39.930391,-75.178109);
 		Market DuranMiniMarketCornerStore = new Market("DuranMiniMarketCornerStore",cornerStoreFoods,DuranMiniMarketCornerStoreLocation );
 		cornerStores.add(DuranMiniMarketCornerStore);	
 		LatLng SantosGroceryCornerStoreLocation = new LatLng(39.930848,-75.169896);
 		Market SantosGroceryCornerStore = new Market("SantosGroceryCornerStore",cornerStoreFoods,SantosGroceryCornerStoreLocation );
 		cornerStores.add(SantosGroceryCornerStore);	
 		LatLng SanLucaGroceryIncCornerStoreLocation = new LatLng(39.930735,-75.171004);
 		Market SanLucaGroceryIncCornerStore = new Market("SanLucaGroceryIncCornerStore",cornerStoreFoods,SanLucaGroceryIncCornerStoreLocation );
 		cornerStores.add(SanLucaGroceryIncCornerStore);
 		
 		//19146
 		LatLng CastroGroceryCornerStoreLocation = new LatLng(39.932059,-75.171474);
 		Market CastroGroceryCornerStore = new Market("CastroGroceryCornerStore",cornerStoreFoods,CastroGroceryCornerStoreLocation );
 		cornerStores.add(CastroGroceryCornerStore);
 		
 		LatLng GilGrocery2CornerStoreLocation = new LatLng(39.933248,-75.176178);
 		Market GilGrocery2CornerStore = new Market("GilGrocery#2CornerStore",cornerStoreFoods,GilGrocery2CornerStoreLocation );
 		cornerStores.add(GilGrocery2CornerStore);	
 		LatLng _21stFoodMarketandDeliCornerStoreLocation = new LatLng(39.933009,-75.179489);
 		Market _21stFoodMarketandDeliCornerStore = new Market("21stFoodMarket&DeliCornerStore",cornerStoreFoods,_21stFoodMarketandDeliCornerStoreLocation );
 		cornerStores.add(_21stFoodMarketandDeliCornerStore);
 		LatLng GandNGroceryDeliCornerStoreLocation = new LatLng(39.933714,-75.182992);
 		Market GandNGroceryDeliCornerStore = new Market("G&NGroceryDeliCornerStore",cornerStoreFoods,GandNGroceryDeliCornerStoreLocation );
 		cornerStores.add(GandNGroceryDeliCornerStore);
 		LatLng TaskerFoodMarketCornerStoreLocation = new LatLng(39.933976,-75.194927);
 		Market TaskerFoodMarketCornerStore = new Market("TaskerFoodMarketCornerStore",cornerStoreFoods,TaskerFoodMarketCornerStoreLocation );
 		cornerStores.add(TaskerFoodMarketCornerStore);
 		LatLng BlackAngelFoodMarketCornerStoreLocation = new LatLng(39.935156,-75.195718);
 		Market BlackAngelFoodMarketCornerStore = new Market("BlackAngelFoodMarketCornerStore",cornerStoreFoods,BlackAngelFoodMarketCornerStoreLocation );
 		cornerStores.add(BlackAngelFoodMarketCornerStore);
 		LatLng PhongGroceryCornerStoreLocation = new LatLng(39.933336,-75.170263);
 		Market PhongGroceryCornerStore = new Market("PhongGroceryCornerStore",cornerStoreFoods,PhongGroceryCornerStoreLocation );
 		cornerStores.add(PhongGroceryCornerStore);
 		LatLng JuanCarlosGomezGroceryCornerStoreLocation = new LatLng(39.933868,-75.174417);
 		Market JuanCarlosGomezGroceryCornerStore = new Market("JuanCarlosGomezGroceryCornerStore",cornerStoreFoods,JuanCarlosGomezGroceryCornerStoreLocation );
 		cornerStores.add(JuanCarlosGomezGroceryCornerStore);
 		LatLng YoungsHarvestMarketCornerStoreLocation = new LatLng(39.93385,-75.180254);
 		Market YoungsHarvestMarketCornerStore = new Market("YoungsHarvestMarketCornerStore",cornerStoreFoods,YoungsHarvestMarketCornerStoreLocation );
 		cornerStores.add(YoungsHarvestMarketCornerStore);
 		LatLng ReedMiniMarketCornerStoreLocation = new LatLng(39.934657,-75.180662);
 		Market ReedMiniMarketCornerStore = new Market("ReedMiniMarketCornerStore",cornerStoreFoods,ReedMiniMarketCornerStoreLocation );
 		cornerStores.add(ReedMiniMarketCornerStore);
 		LatLng LaMecaFoodMarketCornerStoreLocation = new LatLng(39.93444,-75.169163);
 		Market LaMecaFoodMarketCornerStore = new Market("LaMecaFoodMarketCornerStore",cornerStoreFoods,LaMecaFoodMarketCornerStoreLocation );
 		cornerStores.add(LaMecaFoodMarketCornerStore);
 		LatLng LaFuerzaCornerStoreLocation = new LatLng(39.934405,-75.170753);
 		Market LaFuerzaCornerStore = new Market("LaFuerzaCornerStore",cornerStoreFoods,LaFuerzaCornerStoreLocation );
 		cornerStores.add(LaFuerzaCornerStore);
 		LatLng OlivaresGroceryCornerStoreLocation = new LatLng(39.934809,-75.173126);
 		Market OlivaresGroceryCornerStore = new Market("OlivaresGroceryCornerStore",cornerStoreFoods,OlivaresGroceryCornerStoreLocation );
 		cornerStores.add(OlivaresGroceryCornerStore);
 		LatLng LandMFoodMarketCornerStoreLocation = new LatLng(39.935896,-75.180363);
 		Market LandMFoodMarketCornerStore = new Market("L&MFoodMarketCornerStore",cornerStoreFoods,LandMFoodMarketCornerStoreLocation );
 		cornerStores.add(LandMFoodMarketCornerStore);
 		LatLng BaezGroceryLlcCornerStoreLocation = new LatLng(39.936141,-75.182214);
 		Market BaezGroceryLlcCornerStore = new Market("BaezGroceryLlcCornerStore",cornerStoreFoods,BaezGroceryLlcCornerStoreLocation );
 		cornerStores.add(BaezGroceryLlcCornerStore);
 		LatLng UrenaFoodMartCornerStoreLocation = new LatLng(39.937013,-75.190693);
 		Market UrenaFoodMartCornerStore = new Market("UrenaFoodMartCornerStore",cornerStoreFoods,UrenaFoodMartCornerStoreLocation );
 		cornerStores.add(UrenaFoodMartCornerStore);
 		LatLng ArhenyFoodMarketCornerStoreLocation = new LatLng(39.937206,-75.182092);
 		Market ArhenyFoodMarketCornerStore = new Market("ArhenyFoodMarketCornerStore",cornerStoreFoods,ArhenyFoodMarketCornerStoreLocation );
 		cornerStores.add(ArhenyFoodMarketCornerStore);
 		LatLng AandHFoodMarketCornerStoreLocation = new LatLng(39.937197,-75.176637);
 		Market AandHFoodMarketCornerStore = new Market("A&HFoodMarketCornerStore",cornerStoreFoods,AandHFoodMarketCornerStoreLocation );
 		cornerStores.add(AandHFoodMarketCornerStore);
 		LatLng RightTriangleCornerStoreLocation = new LatLng(39.936924,-75.17508);
 		Market RightTriangleCornerStore = new Market("RightTriangleCornerStore",cornerStoreFoods,RightTriangleCornerStoreLocation );
 		cornerStores.add(RightTriangleCornerStore);
 		LatLng ArianaGroceryIICornerStoreLocation = new LatLng(39.935904,-75.169133);
 		Market ArianaGroceryIICornerStore = new Market("ArianaGroceryIICornerStore",cornerStoreFoods,ArianaGroceryIICornerStoreLocation );
 		cornerStores.add(ArianaGroceryIICornerStore);
 		
 		LatLng NunezGroceryCornerStoreLocation = new LatLng(39.940318,-75.169519);
 		Market NunezGroceryCornerStore = new Market("NunezGroceryCornerStore",cornerStoreFoods,NunezGroceryCornerStoreLocation );
 		cornerStores.add(NunezGroceryCornerStore);
 		
 		LatLng HarrysHouseCornerStoreLocation = new LatLng(39.941114,-75.174162);
 		Market HarrysHouseCornerStore = new Market("HarrysHouseCornerStore",cornerStoreFoods,HarrysHouseCornerStoreLocation );
 		cornerStores.add(HarrysHouseCornerStore);
 		
 		LatLng NameKimVansCafCornerStoreLocation = new LatLng(39.94145,-75.179268);
 		Market NameKimVansCafCornerStore = new Market("NameKimVansCafCornerStore",cornerStoreFoods,NameKimVansCafCornerStoreLocation );
 		cornerStores.add(NameKimVansCafCornerStore);
 		
 		LatLng ChristianFoodMarketCornerStoreLocation = new LatLng(39.941997,-75.180783);
 		Market ChristianFoodMarketCornerStore = new Market("ChristianFoodMarketCornerStore",cornerStoreFoods,ChristianFoodMarketCornerStoreLocation );
 		cornerStores.add(ChristianFoodMarketCornerStore);
 		
 		LatLng KennedyGroceryCornerStoreLocation = new LatLng(39.942895,-75.178879);
 		Market KennedyGroceryCornerStore = new Market("KennedyGroceryCornerStore",cornerStoreFoods,KennedyGroceryCornerStoreLocation );
 		cornerStores.add(KennedyGroceryCornerStore);
 		
 		LatLng AdabelDeliIncCornerStoreLocation = new LatLng(39.944477,-75.171559);
 		Market AdabelDeliIncCornerStore = new Market("BarbaroCornerStore",cornerStoreFoods,AdabelDeliIncCornerStoreLocation );
 		cornerStores.add(AdabelDeliIncCornerStore);
  
 		//19112 -none
 		//19153 -none
 				
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
 		
 
 		LatLng  genesisspruceCornerStoreLocation = new LatLng(39.955583,-75.2321);
 		Market genesisspruceCornerStore = new Market("genesisspruceCornerStore",cornerStoreFoods,genesisspruceCornerStoreLocation );
 		cornerStores.add(genesisspruceCornerStore);
 		
 
 		LatLng  nadiasvineCornerStoreLocation = new LatLng(39.966849,-75.243654);
 		Market nadiasvineCornerStore = new Market("nadiasvineCornerStore",cornerStoreFoods,nadiasvineCornerStoreLocation );
 		cornerStores.add(nadiasvineCornerStore);
 		
 		LatLng  yalneypineCornerStoreLocation = new LatLng(39.954732,-75.234321);
 		Market yalneypineCornerStore = new Market("yalneypineCornerStore",cornerStoreFoods,yalneypineCornerStoreLocation );
 		cornerStores.add(yalneypineCornerStore);
 		
 		LatLng westphilaproduce62ndCornerStoreLocation = new LatLng(39.961824,-75.24498);
 		Market westphilaproduce62ndCornerStore = new Market("westphilaproduce62ndCornerStore",cornerStoreFoods,westphilaproduce62ndCornerStoreLocation );
 		cornerStores.add(westphilaproduce62ndCornerStore);
 		
 		LatLng unidollarplusspruceCornerStoreLocation = new LatLng(39.953424,-75.217311);
 		Market unidollarplusspruceCornerStore = new Market("unidollarplusspruceCornerStore",cornerStoreFoods,unidollarplusspruceCornerStoreLocation );
 		cornerStores.add(unidollarplusspruceCornerStore);
 		
 		LatLng mohhallal45walnutCornerStoreLocation = new LatLng(39.955688,-75.213183);
 		Market mohhallal45walnutCornerStore = new Market("mohhallal45walnutCornerStore",cornerStoreFoods,mohhallal45walnutCornerStoreLocation );
 		cornerStores.add(mohhallal45walnutCornerStore);
 		
 		LatLng pm52ndCornerStoreLocation = new LatLng(39.961465,-75.224434);
 		Market pm52ndCornerStore = new Market("pm52ndCornerStore",cornerStoreFoods,pm52ndCornerStoreLocation );
 		cornerStores.add(pm52ndCornerStore);
 		
 		LatLng amanMarketstCornerStoreLocation = new LatLng(39.960417,-75.22857);
 		Market amanMarketstCornerStore = new Market("amanMarketstCornerStore",cornerStoreFoods,amanMarketstCornerStoreLocation );
 		cornerStores.add(amanMarketstCornerStore);
 		
 		LatLng syed52ndCornerStoreLocation = new LatLng(39.955555,-75.225955);
 		Market syed52ndCornerStore = new Market("syed52ndCornerStore",cornerStoreFoods,syed52ndCornerStoreLocation );
 		cornerStores.add(syed52ndCornerStore);
 		
 		LatLng forty9stopwalnutCornerStoreLocation = new LatLng(39.956443,-75.219391);
 		Market forty9stopwalnutCornerStore = new Market("forty9stopwalnutCornerStore",cornerStoreFoods,forty9stopwalnutCornerStoreLocation );
 		cornerStores.add(forty9stopwalnutCornerStore);
 		
 		LatLng delanceyfoodCornerStoreLocation = new LatLng(39.954271,-75.227971);
 		Market delanceyfoodCornerStore = new Market("delanceyfoodCornerStore",cornerStoreFoods,delanceyfoodCornerStoreLocation );
 		cornerStores.add(delanceyfoodCornerStore);
 		//end stores from 19139
 
 		//begin 19143
 		LatLng NigerianAfricanFoodMarketLocation = new LatLng(39.949106,-75.213747);
 		Market NigerianAfricanFoodMarket = new Market("Nigerian African Food Market",cornerStoreFoods,NigerianAfricanFoodMarketLocation);
 		cornerStores.add(NigerianAfricanFoodMarket);
 		
 		LatLng AminGasMiniMartLocation = new LatLng(39.948629,-75.21444);
 		Market AminGasMiniMart = new Market("Amin Gas & Mini Mart",cornerStoreFoods,AminGasMiniMartLocation);
 		cornerStores.add(AminGasMiniMart);
 		
 		LatLng 	DavisPharmacyLocation = new LatLng(39.949082,-75.214206);
 		Market 	DavisPharmacy = new Market("Davis Pharmacy",cornerStoreFoods,DavisPharmacyLocation);
 		cornerStores.add(DavisPharmacy);
 		
 		LatLng 	ValentinoAfricanFoodStoreLocation = new LatLng(39.948871,-75.213596);
 		Market 	ValentinoAfricanFoodStore = new Market("Valentino African Food Store",cornerStoreFoods,ValentinoAfricanFoodStoreLocation);
 		cornerStores.add(ValentinoAfricanFoodStore);
 		
 		LatLng 	MariposaIncLocation = new LatLng(39.948374,-75.217761);
 		Market 	MariposaInc = new Market("Mariposa Inc",cornerStoreFoods,MariposaIncLocation);
 		cornerStores.add(MariposaInc);
 		
 		LatLng 	BoostMarketLocation = new LatLng(39.947806,-75.222276);
 		Market 	BoostMarket = new Market("Boost Market",cornerStoreFoods,BoostMarketLocation);
 		cornerStores.add(BoostMarket);
 		
 		LatLng 	ChesterFoodPlazaLocation = new LatLng(39.945732,-75.213935);
 		Market 	ChesterFoodPlaza = new Market("Chester Food Plaza",cornerStoreFoods,ChesterFoodPlazaLocation);
 		cornerStores.add(ChesterFoodPlaza);
 		
 		LatLng 	JenniferGroceryLocation = new LatLng(39.944306,-75.216378);
 		Market 	JenniferGrocery = new Market("Jennifer Grocery",cornerStoreFoods,JenniferGroceryLocation);
 		cornerStores.add(JenniferGrocery);
 		
 		LatLng 	AnaGroceryLocation = new LatLng(39.941897,-75.214143);
 		Market 	AnaGrocery = new Market("Ana Grocery",cornerStoreFoods,AnaGroceryLocation);
 		cornerStores.add(AnaGrocery);
 		
 		LatLng 	AzconaSupermarketLocation = new LatLng(39.941887,-75.220246);
 		Market 	AzconaSupermarket = new Market("Azcona Supermarket",cornerStoreFoods,AzconaSupermarketLocation);
 		cornerStores.add(AzconaSupermarket);
 		
 		LatLng 	GoodNeighborMarketLocation = new LatLng(39.94116,-75.212609);
 		Market 	GoodNeighborMarket = new Market("Good Neighbor Market",cornerStoreFoods,GoodNeighborMarketLocation);
 		cornerStores.add(GoodNeighborMarket);
 		
 		LatLng 	StreetMiniMarket1Location = new LatLng(39.940126,-75.216702);
 		Market 	StreetMiniMarket1 = new Market("51 Street Mini Market #1",cornerStoreFoods,StreetMiniMarket1Location);
 		cornerStores.add(StreetMiniMarket1);
 		
 		LatLng 	StopOneSupermarketLocation = new LatLng(39.934933,-75.22146);
 		Market 	StopOneSupermarket = new Market("Stop One Supermarket",cornerStoreFoods,StopOneSupermarketLocation);
 		cornerStores.add(StopOneSupermarket);
 		
 		LatLng 	MDAGoceryLocation = new LatLng(39.936381,-75.218779);
 		Market 	MDAGocery = new Market("MDA Gocery",cornerStoreFoods,MDAGoceryLocation);
 		cornerStores.add(MDAGocery);
 		
 		LatLng 	SamichesDeliIncLocation = new LatLng(39.936215,-75.219437);
 		Market 	SamichesDeliInc = new Market("Samiches Deli Inc",cornerStoreFoods,SamichesDeliIncLocation);
 		cornerStores.add(SamichesDeliInc);
 		
 		LatLng 	RaysGroceryLocation = new LatLng(39.934353,-75.221415);
 		Market 	RaysGrocery = new Market("Ray's Grocery",cornerStoreFoods,RaysGroceryLocation);
 		cornerStores.add(RaysGrocery);
 		
 		LatLng 	KingssesingFoodMarketLocation = new LatLng(39.937629,-75.223308);
 		Market 	KingssesingFoodMarket = new Market("Kingssesing Food Market",cornerStoreFoods,KingssesingFoodMarketLocation);
 		cornerStores.add(KingssesingFoodMarket);
 		
 		LatLng 	SisterInternationalFoodMarketLocation = new LatLng(39.938148,-75.225434);
 		Market 	SisterInternationalFoodMarket = new Market("Sister International Food Market",cornerStoreFoods,SisterInternationalFoodMarketLocation);
 		cornerStores.add(SisterInternationalFoodMarket);
 		
 		LatLng 	ArredondoAngelaMiniMarketLocation = new LatLng(39.93652,-75.22715);
 		Market 	ArredondoAngelaMiniMarket = new Market("Arredondo/Angela Mini Market",cornerStoreFoods,ArredondoAngelaMiniMarketLocation);
 		cornerStores.add(ArredondoAngelaMiniMarket);
 		
 		LatLng 	SamanaFoodMarketLocation = new LatLng(39.936771,-75.2294);
 		Market 	SamanaFoodMarket = new Market("Samana Food Market",cornerStoreFoods,SamanaFoodMarketLocation);
 		cornerStores.add(SamanaFoodMarket);
 		
 		LatLng 	JoseJimenezGroceryLocation = new LatLng(39.937967,-75.230825);
 		Market 	JoseJimenezGrocery = new Market("Jose Jimenez Grocery",cornerStoreFoods,JoseJimenezGroceryLocation);
 		cornerStores.add(JoseJimenezGrocery);
 		
 		LatLng 	PapisGroceryLocation = new LatLng(39.939003,-75.230098);
 		Market 	PapisGrocery = new Market("Papi's Grocery",cornerStoreFoods,PapisGroceryLocation);
 		cornerStores.add(PapisGrocery);
 		
 		LatLng 	TwoBrothersFoodMarketLocation = new LatLng(39.943908,-75.228093);
 		Market 	TwoBrothersFoodMarket = new Market("Two Brothers Food Market",cornerStoreFoods,TwoBrothersFoodMarketLocation);
 		cornerStores.add(TwoBrothersFoodMarket);
 		
 		LatLng 	EightBrothersSupermarketLocation = new LatLng(39.943876,-75.232554);
 		Market 	EightBrothersSupermarket = new Market("Eight Brothers Supermarket",cornerStoreFoods,EightBrothersSupermarketLocation);
 		cornerStores.add(EightBrothersSupermarket);
 		
 		LatLng 	Narciso2Location = new LatLng(39.941364,-75.232929);
 		Market 	Narciso2 = new Market("Narciso #2",cornerStoreFoods,Narciso2Location);
 		cornerStores.add(Narciso2);
 		
 		LatLng 	RiftValleyGroceryStoretLocation = new LatLng(39.948388,-75.226999);
 		Market 	RiftValleyGroceryStore = new Market("Rift Valley Grocery Store",cornerStoreFoods,RiftValleyGroceryStoretLocation);
 		cornerStores.add(RiftValleyGroceryStore);
 		
 		LatLng 	AngelicaSupermarketLocation = new LatLng(39.947535,-75.233453);
 		Market 	AngelicaSupermarket = new Market("Angelica Supermarket",cornerStoreFoods,AngelicaSupermarketLocation);
 		cornerStores.add(AngelicaSupermarket);
 		
 		LatLng 	JDMiniMarketLocation = new LatLng(39.948354,-75.235425);
 		Market 	JDMiniMarket = new Market("J&D Mini Market",cornerStoreFoods,JDMiniMarketLocation);
 		cornerStores.add(JDMiniMarket);
 		
 		LatLng 	DiosPruebaelHombreGroceryLocation = new LatLng(39.947281,-75.239682);
 		Market 	DiosPruebaelHombreGrocery = new Market("Dios Prueba el Hombre Grocery",cornerStoreFoods,DiosPruebaelHombreGroceryLocation);
 		cornerStores.add(DiosPruebaelHombreGrocery);
 		
 		LatLng 	DiosPruebaelHombre2Location = new LatLng(39.948643,-75.238712);
 		Market 	DiosPruebaelHombre2 = new Market("Dios Prueba el Hombre #2",cornerStoreFoods,DiosPruebaelHombre2Location);
 		cornerStores.add(DiosPruebaelHombre2);
 		
 		LatLng 	EnaFoodsIncLocation = new LatLng(39.952058,-75.243091);
 		Market 	EnaFoodsInc = new Market("Ena Foods, Inc",cornerStoreFoods,EnaFoodsIncLocation);
 		cornerStores.add(EnaFoodsInc);
 		
 		LatLng 	DelanceyFoodMarketLocation = new LatLng(39.954154,-75.227995);
 		Market 	DelanceyFoodMarket = new Market("Delancey Food Market",cornerStoreFoods,DelanceyFoodMarketLocation);
 		cornerStores.add(DelanceyFoodMarket);
 		
 		LatLng 	Sisters2Location = new LatLng(39.954363,-75.229749);
 		Market 	Sisters2 = new Market("3 Sisters #2",cornerStoreFoods,Sisters2Location);
 		cornerStores.add(Sisters2);
 		
 		LatLng 	AngeloFoodMarketLocation = new LatLng(39.956232,-75.24195);
 		Market 	AngeloFoodMarket = new Market("Angelo's Food Market",cornerStoreFoods,AngeloFoodMarketLocation);
 		cornerStores.add(AngeloFoodMarket);
 		
 		LatLng 	thStDelanceyFoodMarketLocation = new LatLng(39.95541,-75.238221);
 		Market 	thStDelanceyFoodMarket = new Market("58th St. & Delancey Food Market",cornerStoreFoods,thStDelanceyFoodMarketLocation);
 		cornerStores.add(thStDelanceyFoodMarket);
 		
 		LatLng 	MezquitaGroceryLocation = new LatLng(39.954517,-75.226198);
 		Market 	MezquitaGrocery = new Market("Mezquita Grocery",cornerStoreFoods,MezquitaGroceryLocation);
 		cornerStores.add(MezquitaGrocery);
 		//end 19143
 		
 		//begin 19142
 		
 		LatLng 	SPGroceryIncLocation = new LatLng(39.932334,-75.230159);
 		Market 	SPGroceryInc = new Market("S&P Grocery Inc",cornerStoreFoods,SPGroceryIncLocation);
 		cornerStores.add(SPGroceryInc);
 		
 		LatLng 	BrothersSupermarketLocation = new LatLng(39.928145,-75.237719);
 		Market 	BrothersSupermarket = new Market("8 Brothers Supermarket",cornerStoreFoods,BrothersSupermarketLocation);
 		cornerStores.add(BrothersSupermarket);
 		
 		LatLng 	LibertyMiniMarketLocation = new LatLng(39.925999,-75.242676);
 		Market 	LibertyMiniMarket = new Market("Liberty Mini Market",cornerStoreFoods,LibertyMiniMarketLocation);
 		cornerStores.add(LibertyMiniMarket);
 		
 		LatLng 	UplandMiniMarketLocation = new LatLng(39.924198,-75.240431);
 		Market 	UplandMiniMarket = new Market("Upland Mini Market",cornerStoreFoods,UplandMiniMarketLocation);
 		cornerStores.add(UplandMiniMarket);
 		
 		LatLng 	NinaGroceryVarietyLocation = new LatLng(39.927054,-75.231765);
 		Market 	NinaGroceryVariety = new Market("Nina's Grocery & Variety",cornerStoreFoods,NinaGroceryVarietyLocation);
 		cornerStores.add(NinaGroceryVariety);
 		
 		LatLng 	BainHalalMarketAndGroceryLocation = new LatLng(39.926731,-75.233196);
 		Market 	BainHalalMarketAndGrocery = new Market("Bain'S Halal Market And Grocery",cornerStoreFoods,BainHalalMarketAndGroceryLocation);
 		cornerStores.add(BainHalalMarketAndGrocery);
 		
 		LatLng 	DynamicDollarLocation = new LatLng(39.92692,-75.232196);
 		Market 	DynamicDollar = new Market("Dynamic Dollar",cornerStoreFoods,DynamicDollarLocation);
 		cornerStores.add(DynamicDollar);
 		
 		LatLng 	ExecutiveAfricanFoodFashioLocation = new LatLng(39.92508,-75.235468);
 		Market 	ExecutiveAfricanFoodFashio = new Market("Executive African Food & Fashion",cornerStoreFoods,ExecutiveAfricanFoodFashioLocation);
 		cornerStores.add(ExecutiveAfricanFoodFashio);
 		
 		LatLng 	SitaTAfricanGroceryStoreLocation = new LatLng(39.924608,-75.236084);
 		Market 	SitaTAfricanGroceryStore = new Market("Sita T African Grocery Store",cornerStoreFoods,SitaTAfricanGroceryStoreLocation);
 		cornerStores.add(SitaTAfricanGroceryStore);
 		
 		LatLng 	LibertyAfricanGroceryLocation = new LatLng(39.924474,-75.236278);
 		Market 		LibertyAfricanGrocery = new Market("Liberty African Grocery",cornerStoreFoods,LibertyAfricanGroceryLocation);
 		cornerStores.add(LibertyAfricanGrocery);
 		
 		LatLng 	UcetaGroceryLocation = new LatLng(39.926296,-75.224927);
 		Market 		UcetaGrocery = new Market("Uceta Grocery",cornerStoreFoods,UcetaGroceryLocation);
 		cornerStores.add(UcetaGrocery);
 		
 		LatLng 	BuistFoodMarketLocation = new LatLng(39.9251,-75.223429);
 		Market 			BuistFoodMarket = new Market("Buist Food Market",cornerStoreFoods,BuistFoodMarketLocation);
 		cornerStores.add(BuistFoodMarket);
 		
 		LatLng 	JaquezGroceryLocation = new LatLng(39.925494,-75.226911);
 		Market 			JaquezGrocery = new Market("Jaquez Grocery",cornerStoreFoods,JaquezGroceryLocation);
 		cornerStores.add(JaquezGrocery);
 		
 		LatLng 	AladdinFoodMarketLocation = new LatLng(39.924293,-75.2278);
 		Market 		AladdinFoodMarket = new Market("Aladdin Food Market",cornerStoreFoods,AladdinFoodMarketLocation);
 		cornerStores.add(AladdinFoodMarket);
 		
 		LatLng 	DeniaASanchezPadillaGroceryLocation = new LatLng(39.924327,-75.230754);
 		Market 		DeniaASanchezPadillaGrocery = new Market("Denia A Sanchez Padilla Grocery",cornerStoreFoods,DeniaASanchezPadillaGroceryLocation);
 		cornerStores.add(DeniaASanchezPadillaGrocery);
 		
 		LatLng 	ShemaConvenienceStoreLocation = new LatLng(39.921969,-75.23108);
 		Market 		ShemaConvenienceStore = new Market("Shema's Convenience Store",cornerStoreFoods,ShemaConvenienceStoreLocation);
 		cornerStores.add(ShemaConvenienceStore);
 		
 		LatLng 	BJSupermarketLocation = new LatLng(39.921315,-75.231313);
 		Market 		BJSupermarket = new Market("B & J Supermarket",cornerStoreFoods,BJSupermarketLocation);
 		cornerStores.add(BJSupermarket);
 		
 		LatLng 	PirelaFoodMarketLocation = new LatLng(39.918342,-75.228287);
 		Market 		PirelaFoodMarket = new Market("Pirela Food Market",cornerStoreFoods,PirelaFoodMarketLocation);
 		cornerStores.add(PirelaFoodMarket);
 		
 		LatLng 	RomanoGroceryLocation = new LatLng(39.915401,-75.234283);
 		Market 		RomanoGrocery = new Market("Romano Grocery",cornerStoreFoods,RomanoGroceryLocation);
 		cornerStores.add(RomanoGrocery);
 		
 		LatLng 	VJDeliGroceryLocation = new LatLng(39.91572,-75.239092);
 		Market 	VJDeliGrocery = new Market("V & J Deli Grocery",cornerStoreFoods,VJDeliGroceryLocation);
 		cornerStores.add(VJDeliGrocery);
 		
 		LatLng 	KDInternationalMarketLocation = new LatLng(39.914879,-75.240338);
 		Market 	KDInternationalMarket = new Market("K&D International Market",cornerStoreFoods,KDInternationalMarketLocation);
 		cornerStores.add(KDInternationalMarket);
 		
 		LatLng 	BensaleLocation = new LatLng(39.914209,-75.241249);
 		Market 	Bensale = new Market("Bensale",cornerStoreFoods,BensaleLocation);
 		cornerStores.add(Bensale);
 		
 		LatLng 	ZamZamMarketLocation = new LatLng(39.913852,-75.242611);
 		Market 	ZamZamMarket = new Market("Zam Zam Market",cornerStoreFoods,ZamZamMarketLocation);
 		cornerStores.add(ZamZamMarket);
 		
 		LatLng 	ElmwoodMarketLocation = new LatLng(39.919978,-75.233855);
 		Market 	ElmwoodMarket = new Market("Elmwood Market",cornerStoreFoods,ElmwoodMarketLocation);
 		cornerStores.add(ElmwoodMarket);
 		
 		LatLng 	SekpehProsperity1Location = new LatLng(39.920237,-75.233459);
 		Market 	SekpehProsperity1 = new Market("Sekpeh Prosperity #1",cornerStoreFoods,SekpehProsperity1Location);
 		cornerStores.add(SekpehProsperity1);
 		
 		//end 19142
 		
 		//begin 19104
 		
 		LatLng 	CityFoodMarketLocation = new LatLng(39.960522,-75.201848);
 		Market 	CityFoodMarket = new Market("City Food Market",cornerStoreFoods,CityFoodMarketLocation);
 		cornerStores.add(CityFoodMarket);
 		
 		LatLng 	HamiltonExpressDeliLLCLocation = new LatLng(39.962336,-75.191548);
 		Market 	HamiltonExpressDeliLLC = new Market("Hamilton Express Deli LLC",cornerStoreFoods,HamiltonExpressDeliLLCLocation);
 		cornerStores.add(VJDeliGrocery);
 		
 		LatLng 	PeraltaFoodMarketLocation = new LatLng(39.964277,-75.191228);
 		Market 	PeraltaFoodMarket = new Market("Peralta Food Market",cornerStoreFoods,PeraltaFoodMarketLocation);
 		cornerStores.add(PeraltaFoodMarket);
 		
 		LatLng 	RubenGroceriesLocation = new LatLng(39.966111,-75.192673);
 		Market 	RubenGroceries = new Market("Ruben Groceries",cornerStoreFoods,RubenGroceriesLocation);
 		cornerStores.add(RubenGroceries);
 		
 		LatLng 	GoodNeighborSupermarketLocation = new LatLng(39.965956,-75.196519);
 		Market 	GoodNeighborSupermarket = new Market("Good Neighbor Supermarket",cornerStoreFoods,GoodNeighborSupermarketLocation);
 		cornerStores.add(GoodNeighborSupermarket);
 		
 		LatLng 	JRFoodMarketLocation = new LatLng(39.972817,-75.201303);
 		Market 	JRFoodMarket = new Market("J & R Food Market",cornerStoreFoods,JRFoodMarketLocation);
 		cornerStores.add(JRFoodMarket);
 		
 		LatLng 	LaSuperBigMannyGroceryStorLocation = new LatLng(39.972632,-75.203722);
 		Market 	LaSuperBigMannyGroceryStor = new Market("La Super Big Manny Grocery Store",cornerStoreFoods,LaSuperBigMannyGroceryStorLocation);
 		cornerStores.add(LaSuperBigMannyGroceryStor);
 		
 		//end 19104
 
 		//begin stores from 19131
 		LatLng MaximinoCornerStoreLocation = new LatLng(39.973884,-75.231066);
 		Market MaximinoCornerStore = new Market("MaximinoCornerStore",cornerStoreFoods,MaximinoCornerStoreLocation );
 		cornerStores.add(MaximinoCornerStore);
 		LatLng fiftyeightReyesCornerStoreLocation = new LatLng(39.973101,-75.237898);
 		Market fiftyeightReyesCornerStore = new Market("fiftyeightReyesCornerStore",cornerStoreFoods,fiftyeightReyesCornerStoreLocation );
 		cornerStores.add(fiftyeightReyesCornerStore);
 		LatLng fiftysixMastersReyesCornerStoreLocation = new LatLng(39.973634,-75.233276);
 		Market fiftysixMastersReyesCornerStore = new Market("fiftysixMastersReyesCornerStore",cornerStoreFoods,fiftysixMastersReyesCornerStoreLocation );
 		cornerStores.add(fiftysixMastersReyesCornerStore);
 		LatLng EconomicCornerStoreLocation = new LatLng(32.968041,-81.767068);
 		Market EconomicCornerStore = new Market("EconomicCornerStore",cornerStoreFoods,EconomicCornerStoreLocation );
 		cornerStores.add(EconomicCornerStore);
 		LatLng papiCornerStoreLocation = new LatLng(40.819802,-96.644509);
 		Market papiCornerStore = new Market("papiCornerStore",cornerStoreFoods,papiCornerStoreLocation );
 		cornerStores.add(papiCornerStore);
 
 		LatLng fifty4WyalusingCornerStoreLocation = new LatLng(39.967172,-75.220698);
 		Market fifty4WyalusingCornerStore = new Market("fifty4WyalusingCornerStore",cornerStoreFoods,fifty4WyalusingCornerStoreLocation );
 		cornerStores.add(fifty4WyalusingCornerStore);
 
 		LatLng vinasCornerStoreLocation = new LatLng(39.97117,-75.228826);
 		Market vinasCornerStore = new Market("vinasCornerStore",cornerStoreFoods,vinasCornerStoreLocation );
 		cornerStores.add(vinasCornerStore);
 
 		LatLng crestCornerStoreLocation = new LatLng(39.987849,-75.238558);
 		Market crestCornerStore = new Market("crestCornerStore",cornerStoreFoods,crestCornerStoreLocation );
 		cornerStores.add(crestCornerStore);
 
 		LatLng AngelinasCornerStoreLocation = new LatLng(39.98592,-75.229181);
 		Market AngelinasCornerStore = new Market("AngelinasCornerStore",cornerStoreFoods,AngelinasCornerStoreLocation );
 		cornerStores.add(AngelinasCornerStore);
 
 		LatLng ZabalaCornerStoreLocation = new LatLng(39.984749,-75.231301);
 		Market ZabalaCornerStore = new Market("ZabalaCornerStore",cornerStoreFoods,ZabalaCornerStoreLocation );
 		cornerStores.add(ZabalaCornerStore);
 		
 
 		LatLng CommunityCornerStoreLocation = new LatLng(39.984607,-75.22936);
 		Market CommunityCornerStore = new Market("CommunityCornerStore",cornerStoreFoods,CommunityCornerStoreLocation );
 		cornerStores.add(CommunityCornerStore);
 		
 
 		LatLng iandsCornerStoreLocation = new LatLng(39.980105,-75.227326);
 		Market iandsCornerStore = new Market("iandsCornerStore",cornerStoreFoods,iandsCornerStoreLocation );
 		cornerStores.add(iandsCornerStore);
 
 		LatLng valeriosCornerStoreLocation = new LatLng(39.977099,-75.231691);
 		Market valeriosCornerStore = new Market("valeriosCornerStore",cornerStoreFoods,valeriosCornerStoreLocation );
 		cornerStores.add(valeriosCornerStore);
 
 		LatLng CabreraCornerStoreLocation = new LatLng(39.978064,-75.235849);
 		Market CabreraCornerStore = new Market("CabreraCornerStore",cornerStoreFoods,CabreraCornerStoreLocation );
 		cornerStores.add(CabreraCornerStore);
 
 		LatLng MartinezCornerStoreLocation = new LatLng(39.975047,-75.234747);
 		Market MartinezCornerStore = new Market("MartinezCornerStore",cornerStoreFoods,MartinezCornerStoreLocation );
 		cornerStores.add(MartinezCornerStore);
 
 
 		LatLng morgans54thCornerStoreLocation = new LatLng(39.986537,-75.231913);
 		Market morgans54thCornerStore = new Market("morgans54thCornerStore",cornerStoreFoods,morgans54thCornerStoreLocation );
 		cornerStores.add(morgans54thCornerStore);
 
 
 		LatLng rodriguez60055thstCornerStoreLocation = new LatLng(39.975047,-75.234747);
 		Market rodriguez60055thstCornerStore = new Market("rodriguez60055thstCornerStore",cornerStoreFoods,rodriguez60055thstCornerStoreLocation );
 		cornerStores.add(rodriguez60055thstCornerStore);
 
 		
 		//end stores in 19131
 		//begin stores in 19130
 
 		LatLng trustCornerStoreLocation = new LatLng(39.970372,-75.17953);
 		Market trustCornerStore = new Market("trustCornerStore",cornerStoreFoods,trustCornerStoreLocation );
 		cornerStores.add(trustCornerStore);
 
 		LatLng museumCornerStoreLocation = new LatLng(39.962571,-75.168772);
 		Market museumCornerStore = new Market("museumCornerStore",cornerStoreFoods,museumCornerStoreLocation );
 		cornerStores.add(museumCornerStore);
 
 		LatLng garden22fairmountCornerStoreLocation = new LatLng(39.967145,-75.175321);
 		Market garden22fairmountCornerStore = new Market("garden22fairmountCornerStore",cornerStoreFoods,garden22fairmountCornerStoreLocation );
 		cornerStores.add(garden22fairmountCornerStore);
 
 		LatLng sals676nbroadCornerStoreLocation = new LatLng(39.96656,-75.160842);
 		Market sals676nbroadCornerStore = new Market("sals676nbroadCornerStore",cornerStoreFoods,sals676nbroadCornerStoreLocation );
 		cornerStores.add(sals676nbroadCornerStore);
 
 		LatLng hotspotN15thCornerStoreLocation = new LatLng(39.965856,-75.162115);
 		Market hotspotN15thCornerStore = new Market("hotspotN15thCornerStore",cornerStoreFoods,hotspotN15thCornerStoreLocation );
 		cornerStores.add(hotspotN15thCornerStore);
 
 		LatLng aa1700wallaceCornerStoreLocation = new LatLng(39.965719,-75.165782);
 		Market aa1700wallaceCornerStore = new Market("aa1700wallaceCornerStore",cornerStoreFoods,aa1700wallaceCornerStoreLocation );
 		cornerStores.add(aa1700wallaceCornerStore);
 
 		LatLng jk2001greenstCornerStoreLocation = new LatLng(39.975047,-75.234747);
 		Market jk2001greenstCornerStore = new Market("jk2001greenstCornerStore",cornerStoreFoods,jk2001greenstCornerStoreLocation );
 		cornerStores.add(jk2001greenstCornerStore);
 
 		LatLng wallaceCornerStoreLocation = new LatLng(39.96615,-75.170542);
 		Market wallaceCornerStore = new Market("wallaceCornerStore",cornerStoreFoods,wallaceCornerStoreLocation );
 		cornerStores.add(wallaceCornerStore);
 
 		LatLng alejoMtvernonCornerStoreLocation = new LatLng(39.964996,-75.165721);
 		Market alejoMtvernonCornerStore = new Market("alejoMtvernonCornerStore",cornerStoreFoods,alejoMtvernonCornerStoreLocation );
 		cornerStores.add(alejoMtvernonCornerStore);
 
 		LatLng almontepoplarCornerStoreLocation = new LatLng(39.971489,-75.169156);
 		Market almontepoplarCornerStore = new Market("almontepoplarCornerStore",cornerStoreFoods,almontepoplarCornerStoreLocation );
 		cornerStores.add(almontepoplarCornerStore);
 		//end 19130 stores
 		//begin 19121 stores
 		LatLng karenN30thCornerStoreLocation = new LatLng(39.977893,-75.184618);
 		Market karenN30thCornerStore = new Market("karenN30thCornerStore",cornerStoreFoods,karenN30thCornerStoreLocation );
 		cornerStores.add(karenN30thCornerStore);
 		LatLng jm1400NetingCornerStoreLocation = new LatLng(39.977251,-75.18017);
 		Market jm1400NetingCornerStore = new Market("jm1400NetingCornerStore",cornerStoreFoods,jm1400NetingCornerStoreLocation );
 		cornerStores.add(jm1400NetingCornerStore);
 		LatLng yasminwjeffersonCornerStoreLocation = new LatLng(39.977656,-75.172951);
 		Market yasminwjeffersonCornerStore = new Market("yasminwjeffersonCornerStore",cornerStoreFoods,yasminwjeffersonCornerStoreLocation );
 		cornerStores.add(yasminwjeffersonCornerStore);
 		LatLng paulinoswoxfordCornerStoreLocation = new LatLng(39.979131,-75.171306);
 		Market paulinoswoxfordCornerStore = new Market("paulinoswoxfordCornerStore",cornerStoreFoods,paulinoswoxfordCornerStoreLocation );
 		cornerStores.add(paulinoswoxfordCornerStore);
 		LatLng rodriguezturnerstCornerStoreLocation = new LatLng(39.979976,-75.175834);
 		Market rodriguezturnerstCornerStore = new Market("rodriguezturnerstCornerStore",cornerStoreFoods,rodriguezturnerstCornerStoreLocation );
 		cornerStores.add(rodriguezturnerstCornerStore);
 		LatLng martinezn20thstCornerStoreLocation = new LatLng(39.98135,-75.167195);
 		Market martinezn20thstCornerStore = new Market("martinezn20thstCornerStore",cornerStoreFoods,martinezn20thstCornerStoreLocation );
 		cornerStores.add(martinezn20thstCornerStore);
 		LatLng carolinaRidgeCornerStoreLocation = new LatLng(39.987697,-75.182126);
 		Market carolinaRidgeCornerStore = new Market("carolinaRidgeCornerStore",cornerStoreFoods,carolinaRidgeCornerStoreLocation );
 		cornerStores.add(carolinaRidgeCornerStore);
 		LatLng espinaln23rdstCornerStoreLocation = new LatLng(39.985002,-75.171447);
 		Market espinaln23rdstCornerStore = new Market("espinaln23rdstCornerStore",cornerStoreFoods,espinaln23rdstCornerStoreLocation );
 		cornerStores.add(espinaln23rdstCornerStore);
 		LatLng torresNringoldCornerStoreLocation = new LatLng(39.983987,-75.174101);
 		Market torresNringoldCornerStore = new Market("torresNringoldCornerStore",cornerStoreFoods,torresNringoldCornerStoreLocation );
 		cornerStores.add(torresNringoldCornerStore);
 		LatLng duranN27thCornerStoreLocation = new LatLng(39.982862,-75.178763);
 		Market duranN27thCornerStore = new Market("duranN27thCornerStore",cornerStoreFoods,duranN27thCornerStoreLocation );
 		cornerStores.add(duranN27thCornerStore);
 		LatLng cecilMooreCornerStoreLocation = new LatLng(39.98104,-75.177562);
 		Market cecilMooreCornerStore = new Market("cecilMooreCornerStore",cornerStoreFoods,cecilMooreCornerStoreLocation );
 		cornerStores.add(cecilMooreCornerStore);
 		LatLng delvalley21stCornerStoreLocation = new LatLng(39.981497,-75.168959);
 		Market delvalley21stCornerStore = new Market("delvalley21stCornerStore",cornerStoreFoods,delvalley21stCornerStoreLocation );
 		cornerStores.add(delvalley21stCornerStore);
 		LatLng pelteras21stCornerStoreLocation = new LatLng(39.987677,-75.167611);
 		Market pelteras21stCornerStore = new Market("pelteras21stCornerStore",cornerStoreFoods,pelteras21stCornerStoreLocation );
 		cornerStores.add(pelteras21stCornerStore);
 		LatLng jmsesquehanaCornerStoreLocation = new LatLng(39.987366,-75.164478);
 		Market jmsesquehanaCornerStore = new Market("jmsesquehanaCornerStore",cornerStoreFoods,jmsesquehanaCornerStoreLocation );
 		cornerStores.add(jmsesquehanaCornerStore);
 		LatLng lorasesquehanaCornerStoreLocation = new LatLng(39.987438,-75.165238);
 		Market lorasesquehanaCornerStore = new Market("lorasesquehanaCornerStore",cornerStoreFoods,lorasesquehanaCornerStoreLocation );
 		cornerStores.add(lorasesquehanaCornerStore);
 		LatLng adandiamondCornerStoreLocation = new LatLng(39.985559,-75.16121);
 		Market adandiamondCornerStore = new Market("adandiamondCornerStore",cornerStoreFoods,adandiamondCornerStoreLocation );
 		cornerStores.add(adandiamondCornerStore);
 		LatLng bristia16thCornerStoreLocation = new LatLng(39.984461,-75.159886);
 		Market bristia16thCornerStore = new Market("bristia16thCornerStore",cornerStoreFoods,bristia16thCornerStoreLocation );
 		cornerStores.add(bristia16thCornerStore);
 		LatLng  estevezoxfordCornerStoreLocation = new LatLng(39.977897,-75.164493);
 		Market estevezoxfordCornerStore = new Market("estevezoxfordCornerStore",cornerStoreFoods,estevezoxfordCornerStoreLocation );
 		cornerStores.add(estevezoxfordCornerStore);
 		LatLng city19CornerStoreLocation = new LatLng(39.976862,-75.16655);
 		Market city19CornerStore = new Market("city19CornerStore",cornerStoreFoods,city19CornerStoreLocation );
 		cornerStores.add(city19CornerStore);
 		LatLng brothersridgeCornerStoreLocation = new LatLng(39.975585,-75.168714);
 		Market brothersridgeCornerStore = new Market("brothersridgeCornerStore",cornerStoreFoods,brothersridgeCornerStoreLocation );
 		cornerStores.add(brothersridgeCornerStore);
 		LatLng rodriguezthompsonCornerStoreLocation = new LatLng(39.97533,-75.176787);
 		Market rodriguezthompsonCornerStore = new Market("rodriguezthompsonCornerStore",cornerStoreFoods,rodriguezthompsonCornerStoreLocation );
 		cornerStores.add(rodriguezthompsonCornerStore);
 		LatLng threesistersMasterCornerStoreLocation = new LatLng(39.976606,-75.176676);
 		Market threesistersMasterCornerStore = new Market("threesistersMasterCornerStore",cornerStoreFoods,threesistersMasterCornerStoreLocation );
 		cornerStores.add(threesistersMasterCornerStore);
 		LatLng communityoxfordCornerStoreLocation = new LatLng(39.980521,-75.182341);
 		Market communityoxfordCornerStore = new Market("communityoxfordCornerStore",cornerStoreFoods,communityoxfordCornerStoreLocation );
 		cornerStores.add(communityoxfordCornerStore);
 		LatLng bravoMarketCornerStoreLocation = new LatLng(39.981712,-75.182175);
 		Market bravoMarketCornerStore = new Market("bravoMarketCornerStore",cornerStoreFoods,bravoMarketCornerStoreLocation );
 		cornerStores.add(bravoMarketCornerStore);
 		LatLng rightchoiceberksCornerStoreLocation = new LatLng(39.983554,-75.170913);
 		Market rightchoiceberksCornerStore = new Market("rightchoiceberksCornerStore",cornerStoreFoods,rightchoiceberksCornerStoreLocation );
 		cornerStores.add(rightchoiceberksCornerStore);
 		LatLng cfoodmorrisCornerStoreLocation = new LatLng(39.985431,-75.174609);
 		Market cfoodmorrisCornerStore = new Market("cfoodmorrisCornerStore",cornerStoreFoods,cfoodmorrisCornerStoreLocation );
 		cornerStores.add(cfoodmorrisCornerStore);
 		LatLng popexcecilemooreornerStoreLocation = new LatLng(39.981048,-75.174726);
 		Market popexcecilemooreornerStore = new Market("popexcecilemooreornerStore",cornerStoreFoods,popexcecilemooreornerStoreLocation );
 		cornerStores.add(popexcecilemooreornerStore);
 		LatLng twentysixthfoodCornerStoreLocation = new LatLng(39.98283,-75.177131);
 		Market twentysixthfoodCornerStore = new Market("twentysixthfoodCornerStore",cornerStoreFoods,twentysixthfoodCornerStoreLocation );
 		cornerStores.add(twentysixthfoodCornerStore);
 		
 		//end stores 19121 
 		
 
 	}
 	
 	private void instantiateFarmerMarkets() {
 		
 //		19104
 		LatLng LancasterAveLocation = new LatLng(39.959566,-75.197051);
 		Market LancasterAve = new Market("Lancaster Ave", farmerMarketFoods, LancasterAveLocation);
 		farmerMarkets.add(LancasterAve);
 		
 		LatLng UniversitySquareLocation = new LatLng(39.953147,-75.194782);
 		Market UniversitySquare = new Market("University Square", farmerMarketFoods, UniversitySquareLocation);
 		farmerMarkets.add(UniversitySquare);
 		
 		LatLng DrexelUniversityLocation = new LatLng(39.955612,-75.189485);
 		Market DrexelUniversity = new Market("Drexel University", farmerMarketFoods, DrexelUniversityLocation);
 		farmerMarkets.add(DrexelUniversity);
 		
 		LatLng ThePorchAt30thLocation = new LatLng(39.954608,-75.185049);
 		Market ThePorchAt30th = new Market("The Porch at 30th St Station", farmerMarketFoods, ThePorchAt30thLocation);
 		farmerMarkets.add(ThePorchAt30th);
 		
 //		19143
 		LatLng _58thandChesterLocation = new LatLng(39.935732,-75.228533);
 		Market _58thandChester = new Market("58th & Chester (Kingsessing)", farmerMarketFoods, _58thandChesterLocation);
 		farmerMarkets.add(_58thandChester);
 		
 		LatLng Farm51Location = new LatLng(39.942425,-75.219051);
 		Market Farm51 = new Market("Farm 51", farmerMarketFoods, Farm51Location);
 		farmerMarkets.add(Farm51);
 		
 		LatLng ClarkParkLocation = new LatLng(39.949573,-75.20924);
 		Market ClarkPark = new Market("Clark Park (both days)", farmerMarketFoods, ClarkParkLocation);
 		farmerMarkets.add(ClarkPark);
 		
 		
 //		19142 - none
 //		19153 - none
 //		19145 - none
 //		19112 - none
 //		19146
 		LatLng _22ndandTaskarLocation = new LatLng(39.939628,-75.18664);
 		Market _22ndandTaskar = new Market("22nd & Taskar (Point Breeze)", farmerMarketFoods, _22ndandTaskarLocation);
 		farmerMarkets.add(_22ndandTaskar);
 		
 		LatLng _29thandWhartonLocation = new LatLng(39.93729,-75.192046);
 		Market _29thandWharton = new Market("29th & Wharton (Grays Ferry)", farmerMarketFoods, _29thandWhartonLocation);
 		farmerMarkets.add(_29thandWharton);
 		
 //		19130
 		LatLng Girardand27thLocation = new LatLng(39.974189,-75.180434);
 		Market Girardand27th = new Market ("Girard & 27th", farmerMarketFoods, Girardand27thLocation);
 		farmerMarkets.add(Girardand27th);
 		
 		LatLng FairmountLocation = new LatLng(39.967307,-75.17375);
 		Market Fairmount = new Market("Fairmount", farmerMarketFoods, FairmountLocation);
 		farmerMarkets.add(Fairmount);
 
 		LatLng FrancisvilleLocation = new LatLng(39.966664,-75.160521);
 		Market Francisville = new Market("Francisville", farmerMarketFoods, FrancisvilleLocation);
 		farmerMarkets.add(Francisville);		
 		
 //		19121
 		LatLng _33rdandDiamondLocation = new LatLng(39.984092,-75.180803);
 		Market _33rdandDiamond = new Market("33rd & Diamond (Strawberry Mansion)", farmerMarketFoods, _33rdandDiamondLocation);
 		farmerMarkets.add(_33rdandDiamond);
 
 		LatLng NewGrowthProjectLocation = new LatLng(39.977188,-75.179779);
 		Market NewGrowthProject = new Market("New Growth Project", farmerMarketFoods, NewGrowthProjectLocation);
 		farmerMarkets.add(NewGrowthProject);
 		
 		
 //		19131
 		LatLng MSFPHeritageFarmLocation = new LatLng(39.997332,-75.216695);
 		Market MSFPHeritageFarm = new Market("MSFP Heritage Farm", farmerMarketFoods, MSFPHeritageFarmLocation);
 		farmerMarkets.add(MSFPHeritageFarm);
 		
 		LatLng UrbanTreeHarvestFarmstandLocation = new LatLng(39.969937,-75.230496);
 		Market UrbanTreeHarvestFarmstand = new Market("Urban Tree Harvest Farmstand", farmerMarketFoods, UrbanTreeHarvestFarmstandLocation);
 		farmerMarkets.add(UrbanTreeHarvestFarmstand);
 		
 //		19139
 		LatLng HaddingtonLocation = new LatLng(39.96498,-75.224606);
 		Market Haddington = new Market ("Haddington - both days", farmerMarketFoods, HaddingtonLocation);
 		farmerMarkets.add(Haddington);
 		
 		LatLng MillCreekFarmLocation = new LatLng(39.966121,-75.2185);
 		Market MillCreekFarm = new Market("Mill Creek Farm", farmerMarketFoods, MillCreekFarmLocation);
 		farmerMarkets.add(MillCreekFarm);
 		
 		LatLng WalnutHillFarmstandLocation = new LatLng(39.958301,-75.213523);
 		Market WalnutHillFarmstand = new Market("Walnut Hill Farmstand", farmerMarketFoods, WalnutHillFarmstandLocation);
 		farmerMarkets.add(WalnutHillFarmstand);
 	
 		
 	}
 	
 	private void instantiateAldi() {
 		LatLng aldi46thandMarketLocation = new LatLng(39.959169,-75.213727);
 		Market aldi46thandMarket = new Market("aldi46thandMarket",alDiFoods,aldi46thandMarketLocation );
 		aldi.add(aldi46thandMarket);
 
 	}
 }
