 package com.alexrnl.commons.time;
 
 import static org.hamcrest.core.IsNot.not;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test suite for the {@link Time} class.
  * @author Alex
  */
 public class TimeTest {
 	
 	/** The time at midnight */
 	private Time		timeMidnight;
 	/** The time in the morning */
 	private Time		timeMorning;
 	/** The time at noon */
 	private Time		timeNoon;
 	/** The time in the afternoon */
 	private Time		timeAfterNoon;
 	/** The time in the night */
 	private Time		timeNight;
 	/** Array with all the times */
 	private List<Time>	times;
 	
 	/**
 	 * Set up the attributes required for the tests.
 	 */
 	@Before
 	public void setUp () {
 		timeMidnight = new Time();
 		timeMorning = new Time(8, 28);
 		timeNoon = new Time(12);
 		timeAfterNoon = new Time(15, 68);
 		timeNight = new Time(23, -66);
 		
 		times = new ArrayList<>(5);
 		times.add(timeMidnight);
 		times.add(timeMorning);
 		times.add(timeNoon);
 		times.add(timeAfterNoon);
 		times.add(timeNight);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#hashCode()}.
 	 */
 	@Test
 	public void testHashCode () {
 		for (final Time time : times) {
 			// Checking other times
 			for (final Time other : times) {
 				if (time != other) {
 					assertThat(other.hashCode(), not(time.hashCode()));
 				}
 			}
 			
 			// Other checks
 			final Time otherM = new Time(time.getHours(), time.getMinutes() + 1);
 			final Time otherH = new Time(time.getHours() + 1, time.getMinutes());
 			assertThat(otherM.hashCode(), not(time.hashCode()));
 			assertThat(otherH.hashCode(), not(time.hashCode()));
 			assertEquals(time.hashCode(), new Time(time.getHours(), time.getMinutes()).hashCode());
 		}
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#get(java.lang.String)}.
 	 */
 	@Test
 	public void testGet () {
 		assertEquals(timeMidnight, Time.get("0   0"));
 		assertEquals(timeMorning, Time.get("8:28"));
 		assertEquals(timeNoon, Time.get("12"));
 		assertEquals(timeAfterNoon, Time.get("16h08m"));
 		assertEquals(timeNight, Time.get("21.54"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#getCurrent()}.
 	 */
 	@Test
 	public void testGetCurrent () {
 		final Time time = Time.getCurrent();
		final Calendar cal = Calendar.getInstance(Locale.getDefault());
 		assertEquals(cal.get(Calendar.MINUTE), time.getMinutes());
		assertEquals(cal.get(Calendar.HOUR_OF_DAY), time.getHours());
		System.out.println("This should display the current time: " + time);
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#getHours()}.
 	 */
 	@Test
 	public void testGetHours () {
 		assertEquals(0, timeMidnight.getHours());
 		assertEquals(8, timeMorning.getHours());
 		assertEquals(12, timeNoon.getHours());
 		assertEquals(16, timeAfterNoon.getHours());
 		assertEquals(21, timeNight.getHours());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#getMinutes()}.
 	 */
 	@Test
 	public void testGetMinutes () {
 		assertEquals(0, timeMidnight.getMinutes());
 		assertEquals(28, timeMorning.getMinutes());
 		assertEquals(0, timeNoon.getMinutes());
 		assertEquals(8, timeAfterNoon.getMinutes());
 		assertEquals(54, timeNight.getMinutes());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#add(com.alexrnl.commons.time.Time)}.
 	 */
 	@Test
 	public void testAdd () {
 		// Basic check for midnight time
 		for (final Time time : times) {
 			assertEquals(time, timeMidnight.add(time));
 		}
 		assertEquals(timeAfterNoon, timeMorning.add(Time.get("7h40")));
 		assertEquals(timeNight, timeNoon.add(new Time(9, 54)));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#sub(com.alexrnl.commons.time.Time)}.
 	 */
 	@Test
 	public void testSub () {
 		// Basic check for midnight time
 		for (final Time time : times) {
 			assertEquals(time, time.sub(timeMidnight));
 		}
 		assertEquals(timeMorning, timeNight.sub(Time.get("13h26")));
 		assertEquals(timeNoon, timeAfterNoon.sub(new Time(4, 8)));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#compareTo(com.alexrnl.commons.time.Time)}.
 	 */
 	@Test
 	public void testCompareTo () {
 		for (final Time time : times) {
 			assertEquals(0, time.compareTo(time));
 			assertTrue(time.compareTo(null) > 0);
 		}
 		
 		// The list is ordered so testing that is possible
 		for (int index = 0; index < times.size() - 1; ++index) {
 			assertTrue(times.get(index + 1).compareTo(times.get(index)) > 0);
 			assertTrue(times.get(index).compareTo(times.get(index + 1)) < 0);
 		}
 		assertTrue(Time.get("3h01").compareTo(Time.get("3h02")) < 0);
 		assertTrue(Time.get("21h03").compareTo(Time.get("21h02")) > 0);
 		
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#after(com.alexrnl.commons.time.Time)}.
 	 */
 	@Test
 	public void testAfter () {
 		for (int index = 0; index < times.size() - 1; ++index) {
 			assertTrue(times.get(index + 1).after(times.get(index)));
 			assertFalse(times.get(index).after(times.get(index + 1)));
 		}
 		assertTrue(Time.get("16h1").after(Time.get("4h02")));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#before(com.alexrnl.commons.time.Time)}.
 	 */
 	@Test
 	public void testBefore () {
 		for (int index = 0; index < times.size() - 1; ++index) {
 			assertTrue(times.get(index).before(times.get(index + 1)));
 			assertFalse(times.get(index + 1).before(times.get(index)));
 		}
 		assertTrue(Time.get("2h08m").before(Time.get("4h02")));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#toString()}.
 	 */
 	@Test
 	public void testToString () {
 		assertEquals("00:00", timeMidnight.toString());
 		assertEquals("08:28", timeMorning.toString());
 		assertEquals("12:00", timeNoon.toString());
 		assertEquals("16:08", timeAfterNoon.toString());
 		assertEquals("21:54", timeNight.toString());
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#equals(java.lang.Object)}.
 	 */
 	@Test
 	public void testEqualsObject () {
 		for (final Time time : times) {
 			// Check all other times are different
 			for (final Time other : times) {
 				if (time != other) {
 					assertThat(time, not(other));
 				}
 			}
 			
 			// Other checks
 			assertThat(new Time(time.getHours(), time.getMinutes() + 1), not(time));
 			assertThat(new Time(time.getHours() + 1, time.getMinutes()), not(time));
 			assertEquals(time, new Time(time.getHours(), time.getMinutes()));
 			assertEquals(time, time);
 			assertFalse(time.equals(null));
 			assertFalse(time.equals(new Object()));
 		}
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.time.Time#clone()}.<br />
 	 * It also test the {@link com.alexrnl.commons.time.Time#Time(Time)}.
 	 */
 	@Test
 	public void testClone () {
 		try {
 			for (final Time time : times) {
 				final Time clone = time.clone();
 				final Time copy = new Time(time);
 				assertEquals(time, clone);
 				assertTrue(time != clone);
 				assertEquals(time, copy);
 				assertTrue(time != copy);
 			}
 		} catch (final CloneNotSupportedException e) {
 			fail(e.getMessage());
 		}
 	}
 }
