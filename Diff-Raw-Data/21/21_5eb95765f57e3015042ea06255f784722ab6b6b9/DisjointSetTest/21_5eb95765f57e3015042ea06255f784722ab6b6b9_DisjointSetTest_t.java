 package test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import structures.DisjointSet;
 
 public class DisjointSetTest {
     
     @Test(expected=IndexOutOfBoundsException.class)
     public void testIndexOutOfBoundsException() {
         DisjointSet set = new DisjointSet(1,1);
         set.isConnected(2,2,2,2);
     }
     
     @Test(expected=IndexOutOfBoundsException.class)
     public void testIndexOutOfBoundsException2() {
         DisjointSet set = new DisjointSet(0,0);
     }
     
     @Test
     public void testConnected1() {
         DisjointSet set = new DisjointSet(1,1);
        assertTrue(set.isConnected(0,0,0,0));
     }
     
     @Test
     public void testConnected2() {
         DisjointSet set = new DisjointSet(2,2);
         set.union(0, 0, 0, 1);
         set.union(0, 0, 1, 0);
        assertTrue(set.isConnected(0, 0, 0, 1));
        assertTrue(set.isConnected(0, 0, 1, 0));
        assertFalse(set.isConnected(0, 0, 1, 1));
     }
     
     @Test
     public void testConnected3() {
         DisjointSet set = new DisjointSet(3,3);
         set.union(0, 0, 0, 1);
         set.union(0, 1, 0, 2);
         set.union(0, 2, 1, 2);
        assertTrue(set.isConnected(0, 0, 1, 2));
        assertFalse(set.isConnected(0, 1, 2, 0));
     }
 }
