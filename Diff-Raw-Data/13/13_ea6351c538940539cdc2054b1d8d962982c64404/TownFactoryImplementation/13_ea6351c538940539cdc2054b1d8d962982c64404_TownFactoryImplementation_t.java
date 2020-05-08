 /**
  * TownFactoryImplementation.java
  * @version 1.0
  */
 package edu.gatech.CS2340.GrandTheftPoke.backend.Towns;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.GameMap;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlace;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlaceItem;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.GymLeader;
 
 /**
  * Implementation of Town Factory
  * 
  * @author Team Rocket
  * @version 1.0
  */
 public class TownFactoryImplementation implements TownFactory {
 
 	/**
 	 * toString method
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return "TownFactoryImplementation";
 	}
 
 	/**
 	 * itemsInstance field
 	 */
 	private GlobalItemReference itemsInstance;
 	private GameMap theMap;
 
 	/**
 	 * @param theReference
 	 *            globalItemReference for use
 	 */
 	public TownFactoryImplementation(GlobalItemReference theReference, GameMap theMap) {
 		itemsInstance = theReference;
 		this.theMap = theMap;
 	}
 
 	/**
 	 * Method makePalletTown.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makePalletTown()
 	 */
 	@Override
 	public Town makePalletTown() {
 		Map<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(500f,
 				3f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				500f, 3f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				650f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 3f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				3f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(125f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(3000f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 0f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getJigglyPuff(),
 				new MarketPlaceItem(500f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(),
 				new MarketPlaceItem(2000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 4, 4, 0, 4, 100, 0, 0, 500f, theMap);
 		Town myTown = new Town("Pallet Town", myMarket, leader);
 		myTown.setImage("pallet-town");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeViridianCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeViridianCity()
 	 */
 	@Override
 	public Town makeViridianCity() {
 		Map<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		// myStock.put(new Pokeball(), new MarketPlaceItem());
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2500f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 10f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 8, 8, 0, 8, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Viridian City", myMarket, leader);
 		myTown.setImage("viridian-city");
 
 		return myTown;
 
 	}
 
 	/**
 	 * Method makePewterCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makePewterCity()
 	 */
 	@Override
 	public Town makePewterCity() {
 		Map<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		// myStock.put(new Pokeball(), new MarketPlaceItem());
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2500f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 10f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		
 		GymLeader leader = new GymLeader("", 8, 8, 0, 8, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Pewter City", myMarket, leader);
 		myTown.setImage("pewter-city");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeCeruleanCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeCeruleanCity()
 	 */
 	@Override
 	public Town makeCeruleanCity() {
 		Map<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		// myStock.put(new Pokeball(), new MarketPlaceItem());
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2500f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 10f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 8, 8, 0, 8, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Cerulean City", myMarket, leader);
 		myTown.setImage("cerulean-city");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeSaffronCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeSaffronCity()
 	 */
 	@Override
 	public Town makeSaffronCity() {
 		Map<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(30f,
 				30f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(
 				1000f, 30f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				1100f, 30f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1250f, 30f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				750f, 30f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(850f,
 				30f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(450f, 15f));// starting
 																				// price
 																				// and
 																				// quantity
 		// myStock.put(new Pokeball(), new MarketPlaceItem());
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2750f, 15f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3250f,
 				15f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4750f, 15f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(750f,
 				15f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2250f, 15f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(3000f,
 				15f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2750f,
 				15f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3250f,
 				15f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2250f,
 				15f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 1f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 8, 8, 0, 8, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Saffron City", myMarket, leader);
 		myTown.setImage("saffron-city");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeCeladonCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeCeladonCity()
 	 */
 	@Override
 	public Town makeCeladonCity() {
 		Map<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2500f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 10f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 8, 8, 0, 8, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Celadon City", myMarket, leader);
 		myTown.setImage("celadon-city");
 		return myTown;
 	}
 
 	/**
 	 * Method makeLavenderTown.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeLavenderTown()
 	 */
 	@Override
 	public Town makeLavenderTown() {
 		HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(500f,
 				3f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				500f, 3f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				650f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 3f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				3f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(125f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 0f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getJigglyPuff(),
 				new MarketPlaceItem(500f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(),
 				new MarketPlaceItem(2000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 4, 4, 0, 4, 100, 0, 0, 500f, theMap);
 		Town myTown = new Town("Lavender Town", myMarket, leader);
 		myTown.setImage("lavender-town");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeVermillionCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeVermillionCity()
 	 */
 	@Override
 	public Town makeVermillionCity() {
 		HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2500f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 10f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 8, 8, 0, 8, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Vermillion City", myMarket, leader);
 		myTown.setImage("vermillion-city");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeFuchsiaCity.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeFuchsiaCity()
 	 */
 	@Override
 	public Town makeFuchsiaCity() {
 		HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(20f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 20f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				20f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(350f, 10f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getSquirtle(),
 				new MarketPlaceItem(2500f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 10f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getJigglyPuff(), new MarketPlaceItem(500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(),
 				new MarketPlaceItem(2000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(), new MarketPlaceItem(2000f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 10f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 10, 4, 0, 10, 200, 0, 0, 2000f, theMap);
 		Town myTown = new Town("Fuchsia City", myMarket, leader);
 		myTown.setImage("fuchsia-city");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makeCinnabarIsland.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makeCinnabarIsland()
 	 */
 	@Override
 	public Town makeCinnabarIsland() {
 		HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(250f, 5f));// starting
 																				// price
 																				// and
 																				// quantity
 		// myStock.put(new Pokeball(), new MarketPlaceItem());
 		myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 0f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getJigglyPuff(),
 				new MarketPlaceItem(500f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(),
 				new MarketPlaceItem(2000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 16, 16, 0, 16, 400, 0, 0, 5000f, theMap);
 		Town myTown = new Town("Cinnabar Island", myMarket, leader);
 		myTown.setImage("cinnabar-island.png");
 
 		return myTown;
 	}
 
 	/**
 	 * Method makePowerPlant.
 	 * 
 	 * 
 	 * @return Town * @see
 	 *         edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactory
 	 *         #makePowerPlant()
 	 */
 	@Override
 	public Town makePowerPlant() {
 		HashMap<Item, MarketPlaceItem> myStock = new HashMap<Item, MarketPlaceItem>();
 		myStock.put(itemsInstance.getHealthPotion(), new MarketPlaceItem(10f,
 				10f));// starting price and quantity
 		myStock.put(itemsInstance.getHealthVitamin(), new MarketPlaceItem(750f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityVitamin(), new MarketPlaceItem(
 				850f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthVitamin(), new MarketPlaceItem(
 				1000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getStaminaVitamin(), new MarketPlaceItem(
 				500f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeVitamin(), new MarketPlaceItem(600f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getRepel(), new MarketPlaceItem(250f, 5f));// starting
 																				// price
 																				// and
 																				// quantity
 		// myStock.put(new Pokeball(), new MarketPlaceItem());
 		myStock.put(itemsInstance.getSquirtle(), new MarketPlaceItem(2500f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getCharmander(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getPikachu(), new MarketPlaceItem(4500f, 0f));// starting
 																				// price
 																				// and
 																				// quantity
 		myStock.put(itemsInstance.getJigglyPuff(),
 				new MarketPlaceItem(500f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getArcanine(), new MarketPlaceItem(2000f, 0f));// starting
 																					// price
 																					// and
 																					// quantity
 		myStock.put(itemsInstance.getHealthRoid(), new MarketPlaceItem(2750f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getAgilityRoid(), new MarketPlaceItem(2500f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getStrengthRoid(), new MarketPlaceItem(3000f,
 				0f));// starting price and quantity
 		myStock.put(itemsInstance.getTradeRoid(),
 				new MarketPlaceItem(2000f, 0f));// starting price and quantity
 		myStock.put(itemsInstance.getBulbasaur(),
 				new MarketPlaceItem(3000f, 0f));// starting price and quantity
		myStock.put(itemsInstance.getDistancePack(), new MarketPlaceItem(4000f, 0f));
 		MarketPlace myMarket = new MarketPlace(myStock);
 		GymLeader leader = new GymLeader("", 6, 12, 0, 10, 200, 0, 0, 1000f, theMap);
 		Town myTown = new Town("Power Plant", myMarket, leader);
 
 		return myTown;
 	}
 }
