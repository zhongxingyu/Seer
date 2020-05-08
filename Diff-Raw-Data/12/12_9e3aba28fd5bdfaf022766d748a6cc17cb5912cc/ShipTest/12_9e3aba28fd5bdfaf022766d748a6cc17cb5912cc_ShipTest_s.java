 package model;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class ShipTest {
 Ship ship;
 	
 	/**
 	 * Creates a ship with 10 items already in the cargo (max cargo is 15)
 	 */
 	@Before
 	public void setup() {
 		ship = new Ship(ShipType.MOSQUITO, 10, 10, new int[]{0, 1, 2, 3, 4, 0, 0, 0, 0, 0}, 10);
 	}
 	
 	/**
 	 * Tests adding 5 goods to the cargo, which should pass
 	 */
 	@Test
 	public void testAddGood() {
 		try{
 			ship.addGood(TradeGood.FOOD);
 			ship.addGood(TradeGood.NARCOTICS);
 			ship.addGood(TradeGood.ROBOTICS);
 			ship.addGood(TradeGood.FIREARMS);
 			ship.addGood(TradeGood.GAMES);
 			assertTrue("15 items in cargo", ship.getCurrentCargoHold() == 15);
			assertTrue("3 food", ship.getCargo()[2] == 3);
			assertTrue("1 narcotic", ship.getCargo()[8] == 1);
 		} catch(Exception e) {
 			fail("Shouldn't fail");
 		}
 	}
 
 	/**
 	 * Tests that adding 6 goods causes a cargo hull full exception
 	 */
 	@Test
 	public void testAddGoodLimit() {
 		try{
 			ship.addGood(TradeGood.FOOD);
 			ship.addGood(TradeGood.NARCOTICS);
 			ship.addGood(TradeGood.ROBOTICS);
 			ship.addGood(TradeGood.FIREARMS);
 			ship.addGood(TradeGood.GAMES);
 			ship.addGood(TradeGood.FOOD);
 			fail("Should have thrown an exception for adding more than the cargo can hold");
 		} catch(Exception e) {
 			assertTrue("Threw an exception like expected", true);
 		}
 	}
 
 }
