 package galapagos.tests;
 
 import junit.framework.TestCase;
import galapagos.*;
 
 /**
  * Tests the GalapagosFinch class  by making a GalapagosFinch and
  * checking that the correct values are returned when using each of the
  * public queries, and that we get the right values after using each of the
  * public commands.
  */
 public class GalapagosFinchTest extends TestCase {
 	GalapagosFinch finch;
 	public void setUp () {
 		finch = new GalapagosFinch(5, 10, 2, new Samaritan());
 	}
 	
 	/**
 	 * Test that the finch is initialized with the correct values.
 	 */
 	public void testConstructor () {
 		assertEquals(0, finch.age());
 		assertEquals(5, finch.hitpoints());
 		assertEquals(new Samaritan(), finch.behavior());
 		assertEquals(FinchStatus.ALIVE, finch.status());
 	}
 	
 	/**
 	 * Tests makeOlder and age()
 	 */
 	public void testMakeOlder () {
 		finch.makeOlder();
 		assertEquals(1, finch.age());
 		
 		finch.makeOlder();
 		assertEquals(2, finch.age());
 	}
 	
 	/**
 	 * Tests that the finch indicates correctly when its dead by age. 
 	 */
 	public void testStatusDeadByAge() {
 		assertEquals(FinchStatus.ALIVE, finch.status());
 		finch.makeOlder();
 		assertEquals(FinchStatus.ALIVE, finch.status());
 		finch.makeOlder();
 		assertEquals(FinchStatus.DEAD_AGE, finch.status());
 	}
 	
 	/**
 	 * Tests that the finch indicates correctly when its dead by ticks. 
 	 */
 	public void testStatusDeadByTicks() {
 		finch.changeHitpoints(-4);
 		assertEquals(FinchStatus.ALIVE, finch.status());
 		
 		finch.changeHitpoints(-1);
 		assertEquals(FinchStatus.DEAD_TICKS, finch.status());
 	}
 	
 	/**
 	 * Tests changeHitpoints
 	 */
 	public void testChangeHitpoints() {
 		finch.changeHitpoints(0);
 		assertEquals(5, finch.hitpoints());
 		
 		finch.changeHitpoints(-2);
 		assertEquals(3, finch.hitpoints());
 		
 		finch.changeHitpoints(2);
 		assertEquals(5, finch.hitpoints());
 		
 		// Checking that the maximal number of hitpoints indeed is 10.
 		finch.changeHitpoints(12);
 		assertEquals(10, finch.hitpoints());
 	}
 	
 	/**
 	 * Tests that decide() just gives the same as its behaviors decide-method.
 	 */
 	public void testDecide () {
 
 		GalapagosFinch opponent = new GalapagosFinch(5, 10, 2, new Cheater());
 		
 		// Checking decide.
 		assertEquals(finch.behavior().decide(opponent), finch.decide(opponent));
 		assertEquals(opponent.behavior().decide(finch), opponent.decide(finch));
 	}
 	
 }
