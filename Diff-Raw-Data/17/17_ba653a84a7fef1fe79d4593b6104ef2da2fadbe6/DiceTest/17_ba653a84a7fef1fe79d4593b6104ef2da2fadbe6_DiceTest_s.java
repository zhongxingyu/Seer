 package de.htwg.monopoly.enteties.impl;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htwg.monopoly.entities.impl.Dice;
 
 public class DiceTest {
 
 	Dice dice;
 
 	@Before
 	public void setUp() throws Exception {
		
 	}
 
 	/**
 	 * only needed if the getDice method is public and not private
 	 */
 //	@Test
 //	public void testGetDice() {
 //		dice.setDice(1, 1);
 //		assertEquals(1, dice.setDice(1,1));
 //		dice.setDice(6, 6);
 //		assertEquals(6, dice.setDice(6,6));
 //	}
 	
 	@Test
 	public void testThrowDice() {
		Dice.throwDice();
		assertTrue(Dice.getDice1()<=6);
		assertFalse(Dice.getDice1()>6);
		assertFalse(Dice.getDice2()>6);
		assertTrue(Dice.getDice2()<=6);
		assertTrue(Dice.getDice1()>0);
		assertFalse(Dice.getDice1()<=0);
 		
 	}
 
 }
