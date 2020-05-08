 /**
  * Created on 	11.05.2010
  * @author  	Brm / MD nach Vorlagen von Huw / Sid / Weigend
 * Zweck:		JUnit Test fr Interval (Comparable, Polymorphismus)
  */
 package ro.inf.p2.uebung08;
 
 import java.util.logging.Logger;
 import ro.inf.p2.uebung08.Interval;
 import junit.framework.TestCase;
 
 public class TestInterval extends TestCase {
 //	Initialisierung Logger
 	private static Logger logger =
 		Logger.getLogger(TestInterval.class.getName());
 
 
    private final Integer i3 = new Integer(3);
    private final Integer i2 = new Integer(2);
    private final Integer i4 = new Integer(4);
    private final Integer i5 = new Integer(5);
    private final Integer i6 = new Integer(6);
    private final Integer i7 = new Integer(7);
    private final Integer i8 = new Integer(8);
    private final Integer i9 = new Integer(9);
 
     public TestInterval(String name) {
         super(name);
     }
 
     public void testIntIntervalIntersection() {
 
         Interval v = new Interval(i3, i6);
         assertTrue(v.contains(i3));
         assertTrue(!v.contains(i6));
 
         Interval w = new Interval(i7, i7);
         assertTrue(!w.contains(i4));
 
         Interval z = new Interval(i9, i7);
         assertTrue(!z.contains(i4));
 
         Interval s;    // v = [3, 6)
         s = v.intersection(new Interval(i6, i8));
         System.out.println(s.toString());
         assertEquals(s, Interval.EMPTY);
         s = v.intersection(new Interval(i4, i8));
         assertEquals(s, new Interval(i4, i6));
 
         s = v.intersection(new Interval(i4, i5));
         assertEquals(s, new Interval(i4, i5));
 
         s = new Interval(i6, i8).intersection(v);
         assertEquals(s, Interval.EMPTY);
 
         s = new Interval(i6, i8).intersection(v);
 
         s = new Interval(i4, i8).intersection(v);
         assertEquals(s, new Interval(i4, i6));
         s = new Interval(i4, i5).intersection(v);
         assertEquals(s, new Interval(i4, i5));
 		logger.info("Test testIntervalIntersection erfolgreich!");
     }
 
     public void testHashCode(){
         assertEquals(new Interval(i3, i6).hashCode(),
                      new Interval(i3, i6).hashCode());
         int hash1 = new Interval(i3,i4).hashCode();
         int hash2 = new Interval(i3,i4).hashCode();
         assertTrue(hash1 == hash2);
         hash2 = new Interval(i4,i5).hashCode();
         assertFalse(hash1 == hash2);
         Comparable<Integer>[] is = new Integer[10];
         for(int i=0; i<10;++i)
         	is[i] = new Integer(i);
         for(int i=0; i<1000; ++i) {
         	Interval iv1 = new Interval(is[(int)(Math.random()*10)], is[(int)(Math.random()*10)]);
         	Interval iv2 = new Interval(is[(int)(Math.random()*10)], is[(int)(Math.random()*10)]);
         	if(iv1.equals(iv2))
         		assertEquals(iv1.hashCode(), iv2.hashCode());
         }
 		logger.info("Test testHashCode erfolgreich!");
     }
 
     public void testIsEmpty(){
         assertTrue(new Interval(i3,i3).isEmpty());
         assertTrue(new Interval(i4,i3).isEmpty());
         assertFalse(new Interval(i3,i4).isEmpty());
 		logger.info("Test testIsEmpty erfolgreich!");
     }
 
     public void testContains(){
         assertTrue(new Interval(i3,i6).contains(i4));
         assertTrue(new Interval(i3,i6).contains(i4));
         assertTrue(new Interval(i3,i6).contains(i3));
         assertTrue(new Interval(i3,i6).contains(i3));
         assertFalse(new Interval(i4,i7).contains(i9));
 		logger.info("Test testContains erfolgreich!");
     }
 
 	public void testContainsInterval()
 	{
 		assertTrue(new Interval(i3, i7).contains(new Interval(i4, i6)));
 		assertTrue(new Interval(i3, i7).contains(new Interval(i3, i7)));
 		assertFalse(new Interval(i3, i7).contains(new Interval(i2, i6)));
 		assertFalse(new Interval(i3, i6).contains(new Interval(i4, i7)));
 		assertFalse(new Interval(i2, i4).contains(new Interval(i5, i7)));
 		assertTrue(new Interval(i3, i7).contains(new Interval(i3, i2)));
 		logger.info("Test testContainsInterval erfolgreich!");
 	}
  
 	
 }
