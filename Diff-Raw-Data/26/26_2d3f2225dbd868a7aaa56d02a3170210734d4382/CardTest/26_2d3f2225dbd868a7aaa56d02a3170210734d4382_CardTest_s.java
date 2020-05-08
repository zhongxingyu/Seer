 package de.htwg.wzzrd.model.tableComponent.impl;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class CardTest extends TestCase {
     @Test
     public void testCardName() throws Exception {
         Card card = new Card(0, 0);
         assertTrue(CardNames.getName(card).equals("Human Fool"));
         assertTrue(card.toString().equals("Human Fool"));
     }
 
     @Test
     public void test() {
         Card card = new Card(0, 0);
         assertFalse(card.equals(new Object()));
         assertFalse(card.equals(new Card(0, 1)));
 
         assertTrue(card.equals(new Card(0, 0)));
 
         assertEquals(47974, card.hashCode());
     }
 
     @Test
     public void testOrder() {
         // Narr liegt
         // neue Karte macht automatisch den Stich, wenn sie keine Narrenkarte ist.
         assertFalse(new Card(0, 0).isLessThan(new Card(0, 0), 0));
         assertFalse(new Card(0, 0).isLessThan(new Card(1, 0), 0));
         assertFalse(new Card(0, 0).isLessThan(new Card(2, 0), 0));
         assertFalse(new Card(0, 0).isLessThan(new Card(3, 0), 0));
         assertTrue(new Card(0, 0).isLessThan(new Card(0, 1), 0));
         assertTrue(new Card(0, 0).isLessThan(new Card(1, 1), 0));
 
         // Trumpf liegt
         // höchster Trumpf oder Zauberer gewinnt
         assertFalse(new Card(0, 1).isLessThan(new Card(1, 13), 0));
         assertFalse(new Card(0, 13).isLessThan(new Card(2, 13), 0));
         assertFalse(new Card(0, 5).isLessThan(new Card(1, 13), 0));
 
         assertTrue(new Card(0, 5).isLessThan(new Card(0, 6), 0));
         assertTrue(new Card(0, 13).isLessThan(new Card(1, 14), 0));
 
         // Normale Karte liegt
         // nächst höhere trumpf oder Zauberer gewinnt
         assertFalse(new Card(1, 2).isLessThan(new Card(1, 1), 0));
         assertFalse(new Card(1, 13).isLessThan(new Card(1, 13), 0));
 
         assertTrue(new Card(1, 2).isLessThan(new Card(1, 3), 0));
         assertTrue(new Card(1, 13).isLessThan(new Card(0, 1), 0));
         assertTrue(new Card(1, 13).isLessThan(new Card(0, 14), 0));
         assertTrue(new Card(1, 13).isLessThan(new Card(0, 13), 0));
 
         // Zauberer liegt
         // keiner kann höher
         assertFalse(new Card(0, 14).isLessThan(new Card(0, 13), 0));
         assertFalse(new Card(1, 14).isLessThan(new Card(1, 13), 0));
         assertFalse(new Card(2, 14).isLessThan(new Card(2, 13), 0));
         assertFalse(new Card(3, 14).isLessThan(new Card(3, 13), 0));
 
         // Zusatz
        assertTrue(new Card(3, 13).isLessThan(new Card(0, 0), 0));
 
     }
 }
