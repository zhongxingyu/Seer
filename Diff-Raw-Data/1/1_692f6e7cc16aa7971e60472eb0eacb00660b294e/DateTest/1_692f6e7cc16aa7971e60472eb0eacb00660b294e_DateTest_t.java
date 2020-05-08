 package it.unibs.ing.fp.dates;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.Test;
 
 public class DateTest {
 	@Test
 	public void gettingSystemDateAndTime() throws Exception {
 		Date first = new Date();
 		Thread.sleep(50);
 		Date second = new Date();
 		assertTrue(first.before(second));
 		assertTrue(second.after(first));
 	}
 	
 	@Test
 	public void gettingCurrentTimeMillis() throws Exception {
 		long first = System.currentTimeMillis();
 		Thread.sleep(50);
 		long second = System.currentTimeMillis();
 		assertTrue(first < second);
 	}
 	
 	@Test
 	public void gettingCurrentTimeNanos() throws Exception {
 		long first = System.nanoTime();
		Thread.sleep(50);
 		long second = System.nanoTime();
 		assertTrue(first < second);
 	}
 }
