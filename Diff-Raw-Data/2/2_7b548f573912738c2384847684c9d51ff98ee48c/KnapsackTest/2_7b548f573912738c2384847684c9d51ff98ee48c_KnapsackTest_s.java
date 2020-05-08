 package de.mh4j.examples.maxknapsack.model;
 
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.AssertJUnit.assertEquals;
 
 import org.testng.annotations.Test;
 
 public class KnapsackTest {
 
     @Test
     public void testCreate() {
         int capacity = 100;
         Knapsack knapsack = new Knapsack(capacity);
         assertEquals(capacity, knapsack.getCapacity());
        assertEquals("There shpuld be no items in a new knapsack", 0, knapsack.getNumberOfItems());
         assertEquals("Costs should be zero for a new knapsack", 0, knapsack.getCosts());
         assertEquals(capacity, knapsack.getRemainingCapacity());
     }
 
     @Test
     public void testAddItem() {
         int capacity = 100;
         Knapsack knapsack = new Knapsack(capacity);
         int price = 6;
         int volume = 10;
         knapsack.addItem(new Item("Foobar", price, volume));
 
         assertEquals(1, knapsack.getNumberOfItems());
         assertEquals(price, knapsack.getCosts());
         assertEquals(capacity - volume, knapsack.getRemainingCapacity());
     }
 
     @Test
     public void testExceedCapacityLimit() {
         Knapsack knapsack = new Knapsack(10);
 
         assertEquals(true, knapsack.addItem(new Item("Foobar0", 0, 3)));
         assertEquals(true, knapsack.addItem(new Item("Foobar1", 0, 3)));
         assertEquals(true, knapsack.addItem(new Item("Foobar2", 0, 4)));
         assertEquals(false, knapsack.addItem(new Item("Foobar3", 0, 3)));
         assertEquals(3, knapsack.getNumberOfItems());
         assertEquals(0, knapsack.getRemainingCapacity());
     }
 
     @Test
     public void testRemoveItem() {
         Knapsack knapsack = new Knapsack(10);
         knapsack.addItem(new Item("Foo1", 1, 1));
         knapsack.addItem(new Item("Foo2", 2, 3));
         knapsack.addItem(new Item("Foo3", 4, 5));
 
         assertEquals(1, knapsack.getRemainingCapacity());
 
         Item removedItem = knapsack.removeItem(0);
         assertEquals("Foo1", removedItem.name);
         assertEquals(6, knapsack.getCosts());
         assertEquals(2, knapsack.getNumberOfItems());
         assertEquals(2, knapsack.getRemainingCapacity());
 
         removedItem = knapsack.removeItem(1);
         assertEquals("Foo3", removedItem.name);
         assertEquals(2, knapsack.getCosts());
         assertEquals(1, knapsack.getNumberOfItems());
         assertEquals(7, knapsack.getRemainingCapacity());
 
         removedItem = knapsack.removeItem(0);
         assertEquals("Foo2", removedItem.name);
         assertEquals(0, knapsack.getCosts());
         assertEquals(0, knapsack.getNumberOfItems());
         assertEquals(10, knapsack.getRemainingCapacity());
     }
 
     @Test
     public void testIsFull() {
         Knapsack knapsack = new Knapsack(10);
         assertFalse(knapsack.isFull());
 
         knapsack.addItem(new Item("Foo1", 0, 5));
         knapsack.addItem(new Item("Foo1", 0, 2));
         assertFalse(knapsack.isFull());
 
         knapsack.addItem(new Item("Foo1", 0, 3));
         assertTrue(knapsack.isFull());
 
         knapsack.removeItem(0);
         assertFalse(knapsack.isFull());
     }
 
     @Test
     public void testCopyConstructor() {
         Knapsack original = new Knapsack(10);
         Knapsack copy = new Knapsack(original);
 
         assertEquals(original, copy);
     }
 
     @Test
     public void testEquals() {
         Knapsack knapsack01 = new Knapsack(10);
         Knapsack knapsack02 = new Knapsack(10);
         assertTrue(knapsack01.equals(knapsack02));
 
         knapsack01.addItem(new Item("Foo1", 0, 5));
         assertFalse(knapsack01.equals(knapsack02));
 
         knapsack02.addItem(new Item("Foo1", 0, 5));
         assertTrue(knapsack01.equals(knapsack02));
 
         assertFalse(knapsack01.equals("String Test"));
     }
     
     @Test
     public void testIsBetterThanOtherKnapsack() {
     	Knapsack highProfitKnapsack = new Knapsack(10);
     	Knapsack lowProfitKnapsack = new Knapsack(10);
     	
     	highProfitKnapsack.addItem(new Item("expensiveMilk", 3, 1));
     	highProfitKnapsack.addItem(new Item("expensiveChocolate", 4, 1));
     	
     	lowProfitKnapsack.addItem(new Item("cheapMilk", 1, 1));
     	lowProfitKnapsack.addItem(new Item("cheapChocolate", 2, 1));
     	lowProfitKnapsack.addItem(new Item("cheapCoke", 3, 1));
     	
     	assertTrue(highProfitKnapsack.isBetterThan(lowProfitKnapsack));
     	assertFalse(lowProfitKnapsack.isBetterThan(highProfitKnapsack));
     	
     	lowProfitKnapsack.addItem(new Item("cheapJuice", 1, 1));
     	
     	// both have now the same profit
     	assertFalse(highProfitKnapsack.isBetterThan(lowProfitKnapsack));
     	assertFalse(lowProfitKnapsack.isBetterThan(highProfitKnapsack));
     }
 }
