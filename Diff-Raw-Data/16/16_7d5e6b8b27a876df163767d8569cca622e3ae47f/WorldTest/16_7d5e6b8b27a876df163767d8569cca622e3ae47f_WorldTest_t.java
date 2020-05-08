 package galapagos.tests;
 
 import galapagos.*;
 import junit.framework.*;
 import java.util.*;
 
 public class WorldTest extends TestCase {
 
     public List<Integer> toIntegerList(List<World<Integer>.Place> list)
     {
         LinkedList<Integer> result = new LinkedList<Integer>();
         for(World<Integer>.Place place : list)
         {
             result.add(place.getElement());
         }
         return result;
     }
     
     public void containsTheSame (List<?> list1, List<?> list2)
     {
         assertTrue("list2 doesn't contain all elements of list1" , list2.containsAll(list1));
         assertTrue("list1 doesn't contain all elements of list2" , list1.containsAll(list2));
         assertEquals("The lists are different in size", list2.size(), list1.size());
     }
     
     public World<Integer> world;
     public List<Integer> list;
     
     public void setUp() {
         world = new World<Integer>(3, 3);
         list = new LinkedList<Integer>();
     }
     
     /**
      * Fills the world with integers so it looks like:
      *      0 3 6
      *      1 4 7
      *      2 5 8
      */
     public void fillWorld()
     {
         for (int x = 0; x < world.width(); x++)
             for (int y = 0; y < world.height(); y++)
                 world.setAt(x, y, x * world.width() + y);
     }
     
     /**
      * Get the 8 integers surrounding "a"
      * 
      * Example:
      *   Input world:    a == 4
      *            012
      *            345
      *            678
      *   Output list: 0 1 2 3 5 6 7 8
      * 
      * @param a
      * @return A List of the neighbours numbers
      
     public LinkedList<Integer> getNeighbours(int a) {
         LinkedList<Integer> neighbours = new LinkedList<Integer>();
         addWrapped(neighbours, a - world.width() - 1);
         addWrapped(neighbours, a - world.width());
         addWrapped(neighbours, a - world.width() + 1);
         addWrapped(neighbours, a - 1);
         addWrapped(neighbours, a + 1);
         addWrapped(neighbours, a + world.width() - 1);
         addWrapped(neighbours, a + world.width());
         addWrapped(neighbours, a + world.width() + 1);
         
         return neighbours;
     }
     
     public void addWrapped(List<Integer> list, int a) {
             list.add(a);
     }*/
     
     public void testfilledNeighbours()
     {
         fillWorld();
         list.add(0);
         list.add(1);
         list.add(2);
         list.add(3);
         list.add(5);
         list.add(6);
         list.add(7);
         list.add(8);
         
         World.Place place = world.getAt(1,1);
         assertEquals(4, (int)(Integer)place.getElement());
         
         containsTheSame(toIntegerList(world.getAt(1,1).filledNeighbours()), list);
     }
     
     /**
      * Test that RandomIterator goes through all elements
      *
      */
     public void testRandomIterator() {
         fillWorld();
         //stores the elements it has passed
         LinkedList<Integer> found = new LinkedList<Integer>();
         
        for (Iterator<World<Integer>.Place> i = world.randomIterator(); i.hasNext(); ) {
            World<Integer>.Place place =  i.next();
             found.add(place.getElement());
         }
         
         list.add(0);
         list.add(1);
         list.add(2);
         list.add(3);
         list.add(4);
         list.add(5);
         list.add(6);
         list.add(7);
         list.add(8);
         
         containsTheSame(found, list);
     }
     
     
     /**
      * When there are no elements in the world, no neighbours should
      * be found.
      */
     public void testNoNeighboursInEmptyWorld() {
         for(World<Integer>.Place place : world)
         {
             assertEquals(0, place.filledNeighbours().size());
         }
     }
 
     public void testMutablePlaces () {
         fillWorld();
         
        World<Integer>.Place p = world.getAt(2,2);
 
        assertEquals(8, (int)p.getElement());
 
         world.setAt(2,2,42);
        assertEquals(42, (int)p.getElement());
 
         p.setElement(39);
        assertEquals(39, (int)p.getElement());;
     }
 }
