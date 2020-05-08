 package knapsack;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 
 public class KnapsackTests {
 
     @Test
     public void testTotalWeight() throws Exception {
         Item item1 = new Item(1, 1);
         Item item2 = new Item(2, 2);
         Item[] items = {item1, item2};
         Knapsack knapsack = new Knapsack(10, items);
 
         assertEquals(3, knapsack.getTotalWeight());
     }
 
     @Test
     public void testOverweight() throws Exception {
         Item item1 = new Item(1, 5);
         Item item2 = new Item(1, 5);
         Item[] items = {item1, item2};
         Knapsack knapsack = new Knapsack(9, items);
 
         assertTrue(knapsack.isOverWeightLimit());
     }
 
     @Test
     public void factorialMaximumPriceTest() throws Exception {
         Item item1 = new Item(4, 4);
         Item item2 = new Item(2, 2);
         Item[] items = {item1, item2};
         Knapsack sack = new Knapsack(5);
 
         assertEquals(5, sack.getFactorialMaximumPrice(items, 0));
     }
 }
