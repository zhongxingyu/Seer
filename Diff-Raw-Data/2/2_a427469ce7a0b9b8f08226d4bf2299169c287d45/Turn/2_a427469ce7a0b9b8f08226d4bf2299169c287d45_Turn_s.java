 package edu.gatech.CS2340.GrandTheftPoke.backend;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Set;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Person;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Player;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Rocket;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Trader;
 
 /**
  * Represents a Turn
  * 
  * @author Team Rocket
  */
 public class Turn {
 	private GameMap theMap;
 	private ArrayList<Person> gameActors;
 	private Player thePlayer;
 	private Random rand;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param theMap
 	 *            the current map
 	 * @param gameActors
 	 *            the gameActors
 	 * @param thePlayer
 	 *            the currentPlayer
 	 */
 	public Turn(GameMap theMap, ArrayList<Person> gameActors, Player thePlayer) {
 		this.theMap = theMap;
 		this.gameActors = gameActors;
 		this.thePlayer = thePlayer;
 		rand = new Random();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param theMap
 	 *            the current map
 	 * @param player
 	 *            the current player
 	 * @param items
 	 *            the globalItemReference
 	 */
 	public Turn(GameMap theMap, Player player, GlobalItemReference items) {
 		gameActors = new ArrayList<Person>();
 		// String name, int strength, int agility, int trade, int stamina, int
 		// health, int range, int capacity, Float money, GameMap theMap,
 		// GlobalItemReference itemsInstance
 		gameActors.add(new Trader("Bob Waters", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));
 		gameActors.add(new Trader("Ajmal Kunnummal", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));
 		gameActors.add(new Trader("Drake Stephens", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));
 		gameActors.add(new Trader("Henry Tullis", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));
 		gameActors.add(new Trader("Griffin Asher", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));
 		gameActors.add(new Trader("Your Mother", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));
 		gameActors.add(new Trader("Rival", 2, 4, 6, 4, 100, 100, 20, 1000f,
 				theMap, items));
 		gameActors.add(new Trader("Ajmal's Evil Twin", 2, 4, 6, 4, 100, 100,
 				20, 1000f, theMap, items));
 		gameActors.add(new Rocket("Ben Nuttle V2", 6, 4, 2, 4, 100, 100, 20,
 				1000f, theMap));
 		gameActors.add(new Rocket("Ho Yin", 6, 4, 2, 4, 500, 100, 20, 1000f,
 				theMap));
 		gameActors.add(new Rocket("Jill Cagz", 6, 4, 2, 4, 100, 100, 20, 1000f,
 				theMap));
 		gameActors.add(new Rocket("Sagar Laud", 6, 4, 2, 4, 100, 100, 20,
 				1000f, theMap));
 		this.theMap = theMap;
 		this.thePlayer = player;
 		rand = new Random();
 	}
 
 	/**
 	 * runs a turn using the person
 	 * 
 	 * @return encounter
 	 */
 	public Person takeATurn() {
 		useAll();
 		moveAll();
 		trade();
 		return encounter(thePlayer);
 	}
 
 	/**
	 * testing method...uses everytihng
 	 */
 	public void useAll() {
 		/*
 		 * for(Person individual : gameActors) { Set<Item> myStuff =
 		 * individual.getBackpack().getContents().keySet(); for(Item theItem :
 		 * myStuff) { if(theItem instanceof Usable) { if(((Usable)
 		 * theItem).checkForUsage()) { ((Usable) theItem).use(individual); }
 		 * else if(((Usable) theItem).getTimer() <= 0) {
 		 * ((Usable)theItem).unUse(individual); } } } }
 		 */
 	}
 
 	/**
 	 * moves everyone
 	 */
 	public void moveAll() {
 		for (Person individual : gameActors) {
 			individual.move(theMap.getRandomTown());
 		}
 	}
 
 	/**
 	 * allows trade at random
 	 */
 	public void trade() {
 		MarketPlace currentMarket;
 		Set<Item> currentStock;
 		Item[] possibleItems = new Item[17];
 		for (Person individual : gameActors) {
 			if (individual instanceof Trader) {
 
 				if (rand.nextBoolean()) {
 					currentMarket = individual.getCurrent().getMarket();
 					currentStock = currentMarket.getStock().keySet();
 					currentStock.toArray(possibleItems);
 					int randomIndex = rand.nextInt(possibleItems.length);
 					int randomNum = rand.nextInt(10);
 
 					if (((MarketPlaceItem) (currentMarket.getStock()
 							.get(possibleItems[randomNum]))).getStock() != 0
 							&& randomNum != 0) {
 						individual.buy(currentMarket,
 								possibleItems[randomIndex], randomNum);
 					}
 
 				} else {
 					currentMarket = individual.getCurrent().getMarket();
 					currentStock = individual.getBackpack().getContents()
 							.keySet();
 					currentStock.toArray(possibleItems);
 					Item toBeSold = possibleItems[rand
 							.nextInt(possibleItems.length)];
 					if (individual.getBackpack().checkContents(toBeSold, 1)) {
 
 						int quantity = rand.nextInt(individual.getBackpack()
 								.getContents().get(toBeSold));
 						if (individual.getBackpack().checkContents(toBeSold,
 								quantity)) {
 							individual.sell(currentMarket, toBeSold, quantity);
 						}
 					}
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * represents an encounter
 	 * 
 	 * @param thePlayer
 	 *            the current player
 	 * @return Person who wins
 	 */
 	public Person encounter(Player thePlayer) {
 		for (Person individual : gameActors) {
 			if (individual.getCurrent().toString()
 					.equals(thePlayer.getCurrent().toString())) {
 				if (individual instanceof Trader) {
 					// && rand.nextDouble() <= thePlayer.getAgility() / 100) {
 					System.out.println(individual);
 					System.out.println(individual.getBackpack());
 					return individual;
 				} else if (individual instanceof Rocket
 						&& rand.nextDouble() >= thePlayer.getAgility() / 100) {
 					return individual;
 				}
 			}
 		}
 		return null;
 	}
 }
