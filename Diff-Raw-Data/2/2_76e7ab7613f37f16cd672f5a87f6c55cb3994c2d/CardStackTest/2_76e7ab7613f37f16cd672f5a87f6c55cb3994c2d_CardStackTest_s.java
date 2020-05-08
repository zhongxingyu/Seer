 package de.htwg.se.dog.models;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
import de.htwg.se.dog.models.*;

 public class CardStackTest {
 	
 	CardStack cardStack;
 	Card wrong;
 	Card[] cardArray;
 	
 	@Before
 	public void setUp() throws Exception{
 		cardStack = new CardStack();
 		cardArray = CardStack.generateCardArray(); 
 	}
 	
 	@Test
 	public void testgenerateCardArray() {
 		for(int i = 0; i <=51; i++){
 		System.out.println("I: " + i + ", Karte: " + cardArray[i].getValue());	
 		assertEquals(i % 13 + 1, cardArray[i].getValue());
 		}
 		
 		assertEquals(14, cardArray[52].getValue());
 		assertEquals(14, cardArray[53].getValue());
 		assertEquals(14, cardArray[54].getValue());
 	}
 
 }
