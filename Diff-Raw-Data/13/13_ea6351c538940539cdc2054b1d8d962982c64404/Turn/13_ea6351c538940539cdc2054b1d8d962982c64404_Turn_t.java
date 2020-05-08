 /**
  * Turn.java
  * @version 1.0
  */
 
 package edu.gatech.CS2340.GrandTheftPoke.backend;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Set;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Usable;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.Town;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Person;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Player;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Rocket;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Trader;
 
 /**
  * Represents a Turn
  * 
  * @author Team Rocket
  * @version 1.0
  */
 public class Turn {
 	/**
 	 * Method toString.
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return "Turn [theMap=" + theMap + ", gameActors=" + gameActors
 				+ ", thePlayer=" + thePlayer + ", rand=" + rand + "]";
 	}
 
 	/**
 	 * Field theMap.
 	 */
 	private final GameMap theMap;
 
 	/**
 	 * Field gameActors.
 	 */
 	private final ArrayList<Person> gameActors;
 
 	/**
 	 * Field thePlayer.
 	 */
 	private final Player thePlayer;
 
 	/**
 	 * Field rand.
 	 */
 	private final Random rand;
 
 	/**
 	 * Field items
 	 */
 	private final GlobalItemReference items;
 
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
 		this.items = null;
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
 		gameActors.add(new Trader("Bob Waters", 5, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));// stats for Bob Waters
 		gameActors.add(new Trader("Ajmal Kunnummal", 8, 4, 6, 2, 100, 100, 20,
 				1000f, theMap, items));// Stats for Ajmal
 		gameActors.add(new Trader("Misty", 2, 4, 6, 4, 100, 100, 20, 1000f,
 				theMap, items));// stats for Drake
 		gameActors.add(new Trader("Henry Tullis", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));// Stats for Henry
 		gameActors.add(new Trader("Griffin Asher", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));// Stats for Griffin
 		gameActors.add(new Trader("Your Mother", 2, 4, 6, 4, 100, 100, 20,
 				1000f, theMap, items));// stats for your mother
 		gameActors.add(new Trader("Rival", 2, 4, 6, 4, 100, 100, 20, 1000f,
 				theMap, items));// stats for rival
 		gameActors.add(new Trader("Ajmal's Evil Twin", 2, 4, 6, 4, 100, 100,
 				20, 1000f, theMap, items));// stats for ajmal's evil twin
 		gameActors.add(new Rocket("Giovanni", 6, 4, 2, 4, 100, 100, 20, 1000f,
 				theMap));// stats for Giovanni
 		gameActors.add(new Rocket("Ho Yin", 6, 4, 2, 4, 100, 100, 20, 1000f,
 				theMap));// stats for Ho Yin
 		gameActors.add(new Rocket("Jill Cagz", 6, 4, 2, 4, 100, 100, 20, 1000f,
 				theMap));// stats for Jill
 		gameActors.add(new Rocket("Sagar Laud", 6, 4, 2, 4, 100, 100, 20,
 				1000f, theMap));// stats for Sagar
 		this.theMap = theMap;
 		this.thePlayer = player;
 		this.items = items;
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
 //		for(Town town: theMap.getTownSet()){
 //			town.getMarket().update();
 //		}
 		return encounter(thePlayer);
 	}
 
 	/**
 	 * testing method...uses everything
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
 	 * String name, int strength, int agility, int trade, int stamina, int
 	 * health, int range, int capacity, Float money, GameMap theMap,
 	 * GlobalItemReference itemsInstance) moves everyone
 	 */
 	public void moveAll() {
 		Person toBeReincarnated = null;
 		for (Person individual : gameActors) {
 			if (individual.getHealth() > 0) {
 				individual.move(theMap.getRandomTown());
 			} else {
 				toBeReincarnated = individual;
 				break;
 			}
 		}
 		if (toBeReincarnated != null) {
 			Person individual = toBeReincarnated;
 			gameActors.remove(individual);
 			if (individual instanceof Trader) {
 				individual = new Trader(individual.getName(),
 						individual.getStrength(), individual.getAgility(),
 						individual.getTrade(), individual.getStamina(),
 						individual.getMaxHealth(), individual.getBackpack()
 								.getMaxRange(), individual.getBackpack()
 								.getCapacity(), 1000f, theMap, items);
 			}
 			if (individual instanceof Rocket) {
 				individual = new Rocket(individual.getName(),
 						individual.getStrength(), individual.getAgility(),
 						individual.getTrade(), individual.getStamina(),
 						individual.getMaxHealth(), individual.getBackpack()
 								.getMaxRange(), individual.getBackpack()
 								.getCapacity(), 1000f, theMap);
 			}
 			gameActors.add(individual);
 		}
 	}
 
 	/**
 	 * allows trade at random
 	 */
 	public void trade() {
 		MarketPlace currentMarket;
 		Set<Item> currentStock;
		final Item[] possibleItems = new Item[18];
 		for (Person individual : gameActors) {
 			if (individual instanceof Trader) {
 
 				if (rand.nextBoolean()) {
 					currentMarket = individual.getCurrent().getMarket();
 					currentStock = currentMarket.getStock().keySet();
 					currentStock.toArray(possibleItems);
 					int randomIndex = rand.nextInt(possibleItems.length);
 					int randomNum = rand.nextInt(10);
 
					System.out.println(individual.getCurrent());
 					if (((MarketPlaceItem) (currentMarket.getStock()
							.get(possibleItems[randomIndex]))).getStock() != 0
 							&& randomNum != 0) {
 						individual.buy(currentMarket,
 								possibleItems[randomIndex], randomNum);
 					}
 
 				} else if (rand.nextBoolean()) {
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
 				} else {
 					currentStock = individual.getBackpack().getContents()
 							.keySet();
 					currentStock.toArray(possibleItems);
 					Item toBeConsumed = possibleItems[rand
 							.nextInt(possibleItems.length)];
 					if (individual.getBackpack().checkContents(toBeConsumed, 1)
 							&& (toBeConsumed instanceof Usable)) {
 						((Usable) toBeConsumed).use(individual);
 						individual.getBackpack().remove(toBeConsumed, 1);
 
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
 	 * 
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
