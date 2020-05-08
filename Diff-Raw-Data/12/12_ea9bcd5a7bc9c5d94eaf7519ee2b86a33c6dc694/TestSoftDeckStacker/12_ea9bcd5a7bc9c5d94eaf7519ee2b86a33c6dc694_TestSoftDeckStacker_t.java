 package com.blackjack.testcases;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.blackjack.cards.Shoe;
 import com.blackjack.deckstackers.SoftDeckStacker;
 
 public class TestSoftDeckStacker {
 	
 	
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void test() {
 		
 		
 		Shoe shoe = new Shoe(4, new SoftDeckStacker()); 
 		assertTrue(true);
 		
 
 	}
	
	@Test
	public void testNineteenAgainstSix()
	{
		Shoe shoe = new Shoe(1, new SoftDeckStacker());
	}
 
 }
