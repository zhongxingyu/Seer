 package test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import sort.Time;
 
 public class TestTime {
 	
 	@Test
 	public void testTimeDifference() {
 		Time t1 = new Time(10);
 		Time t2 = new Time(15);
 		Time df = new Time(5);
 		
 		assertEquals(t1.difference(t2), df);
 	}
 	
 	@Test
 	public void testTimeDifferenceOverMidnight() {
 		Time t1 = new Time(86000);
 		Time t2 = new Time(400);
 		Time df = new Time(800);
 		
 		assertEquals(t1.difference(t2), df);
 	}
 
 	@Test
 	public void testCreateObject() {
 		assertFalse(new Time(5000)==null);
 	}
 	
 	@Test
 	public void testConvertFromStringSimple() {
		assertEquals(new Time(new String("01.00.00")), new Time(3600));
 	}
 	
 	@Test
 	public void testConvertFromStringComplicated() {
		assertEquals(new Time("05.23.01"), new Time(19381));
 	}
 	
 	@Test
 	public void testConvertFromInvalidStringGives0() {
 		assertEquals(new Time("bajs"), new Time(0));
 	}
 	
 	@Test
 	public void testPrintSimpleTime() {
		assertEquals(new Time(3600).toString(), "01.00.00");
 	}
 	
 	@Test
 	public void testPrintComplicatedTime() {
		assertEquals(new Time(19381).toString(), "05.23.01");
 	}
 
 }
