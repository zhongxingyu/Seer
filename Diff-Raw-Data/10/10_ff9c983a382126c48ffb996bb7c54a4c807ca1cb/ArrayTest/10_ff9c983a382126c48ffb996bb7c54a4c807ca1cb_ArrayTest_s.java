 package util;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import util.Array;
 
 public class ArrayTest {
     private Array<Integer> a;
     private Integer t1, t2, t3, t4;
     
     @Before
     public void setUp() throws Exception {
         a = new Array<Integer>();
         t1 = 1;
         t2 = 2;
         t3 = 3;
         t4 = 4;
     }
     
     @Test
     public void testLeeresArray() {
         assertEquals(0, a.size());
     }
     
     @Test
     public void testEinElement() {
         a.add(t1);
         assertEquals(1, a.size());
         assertSame(t1, a.get(0));
     }
     
     @Test
     public void testSpeichern() {
         fuelleA();
         assertEquals(4, a.size());
         assertSame(t1, a.get(0));
         assertSame(t2, a.get(1));
         assertSame(t3, a.get(2));
         assertSame(t4, a.get(3));
         assertEquals(4, a.size());
         assertSame(t3, a.get(2));
     }
     
     @Test
     public void testIndexOf() {
     	fuelleA();
     	assertEquals(0, a.indexOf(t1));
     	assertEquals(2, a.indexOf(t3));
     	assertEquals(-1, a.indexOf(7));
     	assertEquals(-1, a.indexOf(null));
     	a.add(null);
     	assertEquals(4, a.indexOf(null));
     }
     
     @Test
     public void testObEqualsGenutzt() {
     	Array<String> a = new Array<String>();
     	a.add("abcde");
     	a.add("abcde".substring(2));
     	assertEquals( "== ist verkehrt, equals() benutzen!",
     			1, a.indexOf("abcde".substring(2)));
     }
     
     @Test
     public void testReversed() {
     	fuelleA();
     	Array<Integer> r = a.reversed();
     	assertEquals(t1, r.get(3));
     	assertEquals(t2, r.get(2));
     	assertEquals(t3, r.get(1));
     	assertEquals(t4, r.get(0));
     	// TODO: Den Test vervollstaendigen!
     	assertEquals(t1, a.get(0));
     	assertEquals(t2, a.get(1));
     	assertEquals(t3, a.get(2));
     	assertEquals(t4, a.get(3));
     }
     
     @Test(expected = IndexOutOfBoundsException.class)
     public void leer() {
             a.get(0);
     }
     
     @Test(expected = IndexOutOfBoundsException.class)
     public void zugross() {
         fuelleA();
         a.get(5);
     }
     
     private void fuelleA() {
         a.add(t1);
         a.add(t2);
         a.add(t3);
         a.add(t4);
     }
 }
