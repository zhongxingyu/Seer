 package com.github.kpacha.jkata.pokerhand.test;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class PokerHandTest extends TestCase {
 
     @Test
     public void testGetNumericValue() {
 	PokerCard card = new PokerCard("5H");
 	assertEquals(5, card.getNumericValue());
     }
 }
