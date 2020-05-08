 package edu.gatech.CS2340.GrandTheftPoke.backend.Towns;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlace;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlaceItem;
 import java.util.HashMap;
 
 public class TownFactoryImplementation implements TownFactory {
 
     private GlobalItemReference itemsInstance;
 
     public TownFactoryImplementation(GlobalItemReference theReference) {
         itemsInstance = theReference;
     }
 
     @Override
     public Town makePalletTown() {
         HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10,10));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 0));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 0));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 0));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 0));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(250, 5));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 0));
        myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 0));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 0));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 0));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 0));
         System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Pallet Town", myMarket);
         myTown.setImage("images//icons//PalletTownBack.png");
 
         return myTown;
     }
 
     @Override
     public Town makeViridianCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20,20));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 20));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 20));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 20));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 20));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 20));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350, 10));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 10));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 10));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 10));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 10));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 10));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Viridian City", myMarket);
         myTown.setImage("images//icons//ViridianCityBack.png");
 
         return myTown;
 
     }
 
     @Override
     public Town makePewterCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20,20));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 20));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 20));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 20));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 20));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 20));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350, 10));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 10));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 10));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 10));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 10));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 10));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Pewter City", myMarket);
         myTown.setImage("images//icons//PewterCityBack.png");
 
 
         return myTown;
     }
 
     @Override
     public Town makeCeruleanCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20,20));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 20));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 20));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 20));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 20));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 20));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350, 10));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 10));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 10));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 10));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 10));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 10));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Cerulean City", myMarket);
         myTown.setImage("images//icons//CeruleanCityBack.png");
 
 
         return myTown;
     }
 
     @Override
     public Town makeSaffronCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(30,30));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(1000, 30));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(1100, 30));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1250, 30));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(750, 30));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(850, 30));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(450, 15));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2750, 15));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3250, 15));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4750, 15));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(750, 15));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2250, 15));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(3000, 15));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2750, 15));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3250, 15));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2250, 15));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Saffron City", myMarket);
 
 
         return myTown;
     }
 
     @Override
     public Town makeCeladonCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20,20));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 20));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 20));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 20));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 20));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 20));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350, 10));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 10));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 10));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 10));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 10));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 10));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Celadon City", myMarket);
         return myTown;
     }
 
     @Override
     public Town makeLavenderTown() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10,10));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 0));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 0));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 0));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 0));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(250, 5));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 0));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 0));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 0));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 0));
         System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Lavender Town", myMarket);
 
 
         return myTown;
     }
 
     @Override
     public Town makeVermillionCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20,20));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 20));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 20));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 20));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 20));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 20));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350, 10));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 10));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 10));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 10));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 10));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 10));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Vermillion City", myMarket);
 
 
         return myTown;
     }
 
     @Override
     public Town makeFuchsiaCity() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20,20));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 20));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 20));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 20));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 20));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 20));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350, 10));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 10));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 10));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 10));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 10));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 10));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 10));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 10));
         //System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Fuchsia City", myMarket);
 
 
         return myTown;
     }
 
     @Override
     public Town makeCinnabarIsland() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10,10));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 0));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 0));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 0));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 0));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(250, 5));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 0));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 0));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 0));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 0));
         System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Cinnabar Island", myMarket);
 
 
         return myTown;
     }
 
     @Override
 	public Town makePowerPlant() {
     	HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
         myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10,10));
         myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750, 0));
         myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(850, 0));
         myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(1000, 0));
         myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600, 0));
         myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(250, 5));
         //myStock.put(new Pokeball(), new MarketPlaceItem());
         myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getCharizard(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500, 0));
         myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500, 0));
         myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000, 0));
         myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750, 0));
         myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500, 0));
         myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000, 0));
         myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000, 0));
         System.out.println(myStock.containsKey(itemsInstance.getHealthPotion()));
         MarketPlace myMarket = new MarketPlace(myStock);
         //System.out.println(myStock);
         Town myTown = new Town("Power Plant", myMarket);
 
         return myTown;
     }
 }
