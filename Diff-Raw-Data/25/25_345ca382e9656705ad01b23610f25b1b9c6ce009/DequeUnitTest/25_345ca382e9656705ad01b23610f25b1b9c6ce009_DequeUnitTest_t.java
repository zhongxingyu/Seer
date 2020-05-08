 import junit.framework.Assert;
 import junit.framework.JUnit4TestAdapter;
 
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 
 public class DequeUnitTest extends Assert {
     @Test
     public void testConstruction() {
         Deque<Integer> foo = new Deque<Integer>();
         assertTrue(foo.isEmpty());
         assertTrue(foo.size() == 0);
     }
 
     @Test(expected = java.util.NoSuchElementException.class)
     public void testRemoveFirstFromEmptyDeque() {
         Deque<Integer> foo = new Deque<Integer>();
         foo.removeFirst();
     }
 
     @Test(expected = java.util.NoSuchElementException.class)
     public void testRemoveLastFromEmptyDeque() {
         Deque<Integer> foo = new Deque<Integer>();
         foo.removeLast();
     }
 
     @Test(expected = java.lang.NullPointerException.class)
     public void testAddFirstNullElement() {
         Deque<Integer> foo = new Deque<Integer>();
         foo.addFirst(null);
     }
 
     @Test(expected = java.lang.NullPointerException.class)
     public void testAddLastNullElement() {
         Deque<Integer> foo = new Deque<Integer>();
         foo.addLast(null);
     }
 
     @Test
     public void testWorkflow() {
         Deque<Integer> foo = new Deque<Integer>();
 
         for (int i = 0; i < 10; ++i) {
             foo.addLast(i);
         }
 
         assertEquals(10, foo.size());
         assertTrue(!foo.isEmpty());
 
         int i = 0;
         for (int elem : foo) {
             assertEquals((int) elem, i);
             ++i;
         }
 
         assertEquals(10, foo.size());
 
         for (i = 9; i >= 0; --i) {
             assertEquals(i, (int) foo.removeLast());
         }
 
         assertTrue(foo.size() == 0);
         assertTrue(foo.isEmpty());
 
         for (i = 0; i < 100; ++i) {
             foo.addFirst(i);
         }
 
         assertTrue(foo.size() == 100);
         assertTrue(!foo.isEmpty());
 
         i = 99;
         for (int elem : foo) {
             assertEquals((int) elem, i);
             --i;
         }
 
         for (i = 99; i >= 0; --i) {
             assertEquals((int) foo.removeFirst(), i);
         }
 
         assertTrue(foo.size() == 0);
         assertTrue(foo.isEmpty());
     }
 
     @Test
    public void testAddFirst100() {
        Deque<String> foo = new Deque<String>();
        for (int i = 0; i < 100; ++i) {
            foo.addFirst(String.valueOf(i));
        }

        int i = 99;
        for (String item : foo) {
            assertEquals(String.valueOf(i--), item);
        }
    }

    @Test
     public void testEmptyDeque() {
         Deque<Integer> foo = new Deque<Integer>();
 
         for (int i : foo) {
             fail();
         }
     }
 
     public static junit.framework.Test suite() {
         return new JUnit4TestAdapter(DequeUnitTest.class);
     }
 
     public static void main(String[] args) {
         new JUnitCore().main("DequeUnitTest");
     }
 }
