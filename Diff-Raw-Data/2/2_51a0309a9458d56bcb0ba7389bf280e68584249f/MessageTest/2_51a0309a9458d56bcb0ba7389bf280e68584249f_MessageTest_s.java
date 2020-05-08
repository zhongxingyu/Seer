 package org.logparser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Unit tests for {@link Message}.
  * 
  * @author jorge.decastro
  * 
  */
 public class MessageTest {
 	private Message message;
 	private static final String ORIGINAL_ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
 	private static final String URL = "statusCheck.do";
 	private static final String ANOTHER_URL = "edit.do";
 	private static final String MILLISECONDS = "2073";
 	private static Calendar calendar;
 	private Message x;
 	private Message y;
 	private Message z;
 	private Message notx;
 
 	static {
 		calendar = Calendar.getInstance();
 	}
 
 	@Before
 	public void setUp() {
		calendar.set(2008, 12, 15, 17, 15, 00);
 		Date date = calendar.getTime();
 		message = new Message(ORIGINAL_ENTRY, date, URL, MILLISECONDS);
 		x = new Message(ORIGINAL_ENTRY, date, URL, MILLISECONDS);
 		y = new Message(ORIGINAL_ENTRY, date, URL, MILLISECONDS);
 		z = new Message(ORIGINAL_ENTRY, date, URL, MILLISECONDS);
 		notx = new Message(ORIGINAL_ENTRY, date, ANOTHER_URL, MILLISECONDS);
 	}
 
 	@Test
 	@SuppressWarnings("deprecation")
 	public void testMessageImmutability() {
 		// only need to test mutability on Dates since the other arguments are immutable
 		Date d = message.getDate();
 		d.setMinutes(30);
 		assertFalse("External date change mutated object", d.equals(message.getDate()));
 		assertTrue(message.getDate().getMinutes() == 15);
 	}
 
 	@Test
 	public void testEqualsToSelf() {
 		assertTrue("Class equal to itself.", x.equals(x));
 	}
 
 	@Test
 	public void testIncompatibleTypeIsFalse() {
 		assertFalse("Passing incompatible object to equals should return false", x.equals("string"));
 	}
 
 	@Test
 	public void testNullReferenceIsFalse() {
 		assertFalse("Passing null to equals should return false", x.equals(null));
 	}
 
 	@Test
 	public void testEqualsIsReflexiveAndSymmetric() {
 		assertTrue("Reflexive test fail x,y", x.equals(y));
 		assertTrue("Symmetric test fail y", y.equals(x));
 
 	}
 
 	@Test
 	public void testEqualsIsTransitive() {
 		assertTrue("Transitive test fails x,y", x.equals(y));
 		assertTrue("Transitive test fails y,z", y.equals(z));
 		assertTrue("Transitive test fails x,z", x.equals(z));
 	}
 
 	@Test
 	public void testEqualsIsConsistent() {
 		assertTrue("Consistent test fail x,y", x.equals(y));
 		assertTrue("Consistent test fail x,y", x.equals(y));
 		assertTrue("Consistent test fail x,y", x.equals(y));
 		assertFalse(notx.equals(x));
 		assertFalse(notx.equals(x));
 		assertFalse(notx.equals(x));
 	}
 
 	@Test
 	public void testHashcodeIsConsistent() {
 		int hashcode = x.hashCode();
 		assertEquals("Consistent hashcode test fails", hashcode, x.hashCode());
 		assertEquals("Consistent hashcode test fails", hashcode, x.hashCode());
 	}
 
 	@Test
 	public void testTwoEqualObjectsProduceSameHashcode() {
 		int xhashcode = x.hashCode();
 		int yhashcode = y.hashCode();
 		assertEquals("Equal object, return equal hashcode test fails", xhashcode, yhashcode);
 	}
 
 	@Test
 	public void testTwoDifferentObjectsProduceDifferentHashcode() {
 		int xhashcode = x.hashCode();
 		int yhashcode = notx.hashCode();
 		assertTrue("Equal object, return unequal hashcode test fails", !(xhashcode == yhashcode));
 	}
 
 	@Test
 	public void testToCsvString() {
 		assertEquals(String.format("\"%s\", \"%s\", \"%s\"", message.getDate(), message.getUrl(), message.getMilliseconds()), message.toCsvString());
 	}
 }
