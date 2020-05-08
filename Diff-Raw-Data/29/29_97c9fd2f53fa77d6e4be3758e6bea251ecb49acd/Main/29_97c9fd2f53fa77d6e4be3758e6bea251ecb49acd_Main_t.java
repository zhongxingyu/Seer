 package edu.gatech.CS2340.GrandTheftPoke.backend;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.GlobalItemReference;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.Town;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.TownFactoryImplementation;
 import java.util.Iterator;
 import java.util.Set;
 
 public class Main {
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		GlobalItemReference theReference = new GlobalItemReference();
 		GameMap testMap = Main.makeMap(theReference);
 		//testMap.Dijkstras();
		testMarketPlace(testMap, theReference);
 		//testMovement(testMap);
 	}
 
 	public static GameMap makeMap(GlobalItemReference theReference) {
 		GameMap map = new GameMap(theReference);
 		return map;
 	}
 
 	public static void testMarketPlace(GameMap map,
 			GlobalItemReference theReference) {
 		MarketPlace theMarket = map.getCurrent().getMarket();
 		Player testPlayer = new Player("Ben", 4, 4, 4, 4, 500, 100, 1000, map);
 		System.out.println(testPlayer.getBackpack());
 		System.out.println(testPlayer.getWallet());
		System.out.println(testPlayer.buy(theMarket, theReference.getCharizard(), 1));
 		System.out.println(testPlayer.getBackpack());
 		System.out.println(testPlayer.getWallet());
		System.out.println(testPlayer.sell(theMarket, theReference.getHealthPotion(), 3));
 		System.out.println(testPlayer.getBackpack());
 		System.out.println(testPlayer.getWallet());
 		testPlayer.buy(theMarket, theReference.getRepel(), 1);
 		System.out.println(testPlayer.getBackpack());
 		System.out.println(testPlayer.getWallet());
 	}
 
 	public static void testMovement(GameMap map) {
 		Player testPlayer = new Player("Ben", 4, 4, 4, 4, 500, 120, 10, map);
 		Set<Town> townSet = map.getTownSet();
 		for (Iterator<Town> it = townSet.iterator(); it.hasNext();) {
 			testPlayer.move((Town) it.next());
 			System.out.println(testPlayer.getCurrent());
 			map.Dijkstras();
 			System.out.println();
 		}
 	}
 }
