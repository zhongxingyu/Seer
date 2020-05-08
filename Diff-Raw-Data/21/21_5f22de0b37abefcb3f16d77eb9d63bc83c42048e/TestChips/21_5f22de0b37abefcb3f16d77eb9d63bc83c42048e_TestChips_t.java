 package testParticipant;
 
 import participant.*;
 import junit.framework.TestCase;
 
 public class TestChips extends TestCase {
 	private int expectedValue;
 	private int actualValue;
 	private boolean failed;
 	
 	public void testGetChipsWithPlayer() {
 		Player player = new Player("abc", "123");
 		Chips chips = new Chips(player.getChips(), 1);
 		
 		expectedValue = player.getChips();
 		actualValue = chips.getChips();
 		
 		assertEquals(expectedValue, actualValue);
 	}
 	
 	public void testGetBetWithPlayer() {
 		Player player = new Player("emily", "abc");
 		Chips chips = new Chips(player.getChips(), 4);
 		
 		expectedValue = 4;
 		actualValue = chips.getBet();
 		
 		assertEquals(expectedValue, actualValue);
 	}
 	
 	public void testConstructorWithCorrectBet() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testConstructorWithNegativeBet() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, -5);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testConstructorWithPositiveBetGreaterThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(50, 60);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testConstructorWithNegativeBetGreaterThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, -1000);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testConstructorWithPositiveBetEqualToChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 500);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testSetChipsWithPositiveAmount() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 			chips.setChips(player, 500);
 			
 			expectedValue = 500;
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testSetChipsWithNegativeAmount() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 			chips.setChips(player, -500);
 			
 			expectedValue = 500;
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testAddChipsPositiveAmount() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 			
 			expectedValue = player.getChips() + 100;
 			chips.addChips(player, 100);
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testAddChipsNegativeAmount() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 			
 			expectedValue = player.getChips() - 100;
 			chips.addChips(player, -100);
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testAddChipsPositiveAmountGreaterThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 			
 			expectedValue = player.getChips() + 500;
 			chips.addChips(player, 500);
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testAddChipsNegativeAmountGreaterThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 		
 			expectedValue = player.getChips();
 			chips.addChips(player, -100500);
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testAddChipsNegativeAmountEqualToChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			Player player = new Player("jack", "Black");
 		
 			expectedValue = player.getChips() - player.getChips();
 			chips.addChips(player, -player.getChips());
 			actualValue = player.getChips();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testSetBetWithPositiveAmountLessThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 			
 			chips.setBet(10);
 			expectedValue = 10;
 			actualValue = chips.getBet();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, false);
 	}
 	
 	public void testSetBetWithPositiveAmountGreaterThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 		
 			chips.setBet(700);
 			expectedValue = 700;
 			actualValue = chips.getBet();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testSetBetWithNegativeAmountLessThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 		
 			chips.setBet(-50);
 			expectedValue = 100;
 			actualValue = chips.getBet();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testSetBetWithNegativeAmountGreaterThanChips() {
 		failed = false;
 		try {
 			Chips chips = new Chips(500, 100);
 		
 			chips.setBet(-700);
 			expectedValue = 100;
 			actualValue = chips.getBet();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 		
 		assertEquals(failed, true);
 	}
 	
 	public void testSetBetPositiveAmountEqualToChips() {
 		failed = false;
 		try {
			Chips chips = new Chips(500, 0);
 		
 			chips.setBet(500);
 			expectedValue = 500;
 			actualValue = chips.getBet();
 			
 			assertEquals(expectedValue, actualValue);
 		} catch (IllegalArgumentException e) {
 			failed = true;
 		}
 	}
 }
