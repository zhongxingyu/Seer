 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.utils;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Dimension;
 import java.awt.Point;
 
 import net.lmxm.ute.utils.testimpl.TestPreferences;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * The Class AbstractPreferencesTest.
  */
 public class AbstractPreferencesTest {
 
 	private static final int DEFAULT_INT = Integer.MIN_VALUE;
 
 	/** The Constant KEY. */
 	private static final String KEY = "key";
 
 	/** The Constant TEST_PREFERENCES. */
 	private static final TestPreferences TEST_PREFERENCES = new TestPreferences();
 
 	/** The Constant VALUE. */
 	private static final String VALUE = "value";
 
 	/**
	 * Removes the all preferences.
 	 */
 	@AfterClass
	public static void removeAllPreferences() {
 		TEST_PREFERENCES.removeAllPreferences();
 	}
 
 	/**
	 * Removes all preferences.
	 */
	@Before
	public void setUp() {
		removeAllPreferences();
	}

	/**
 	 * Test do all keys exist.
 	 */
 	@Test
 	public void testDoAllKeysExist() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test get all keys.
 	 */
 	@Test
 	public void testGetAllKeys() {
 		String[] keys;
 
 		keys = TEST_PREFERENCES.getAllKeys();
 		assertNotNull(keys);
 		assertTrue(keys.length == 0);
 
 		TEST_PREFERENCES.setString(KEY, VALUE);
 
 		keys = TEST_PREFERENCES.getAllKeys();
 		assertNotNull(keys);
 		assertTrue(keys.length == 1);
 		assertTrue(keys[0].equals(KEY));
 	}
 
 	/**
 	 * Test get dimension.
 	 */
 	@Test
 	public void testGetDimension() {
 		Dimension dimension;
 
 		dimension = TEST_PREFERENCES.getDimension(KEY);
 		assertNull(dimension);
 
 		TEST_PREFERENCES.setDimension(KEY, new Dimension(42, 84));
 
 		dimension = TEST_PREFERENCES.getDimension(KEY);
 		assertNotNull(dimension);
 		assertEquals(42, dimension.getWidth(), 0.0);
 		assertEquals(84, dimension.getHeight(), 0.0);
 	}
 
 	/**
 	 * Test get int.
 	 */
 	@Test
 	public void testGetInt() {
 		int intValue;
 
 		intValue = TEST_PREFERENCES.getInt(KEY, DEFAULT_INT);
 		assertEquals(intValue, DEFAULT_INT);
 
 		TEST_PREFERENCES.setInt(KEY, 42);
 
 		intValue = TEST_PREFERENCES.getInt(KEY, DEFAULT_INT);
 		assertEquals(42, intValue);
 	}
 
 	/**
 	 * Test get point.
 	 */
 	@Test
 	public void testGetPoint() {
 		Point point;
 
 		point = TEST_PREFERENCES.getPoint(KEY);
 		assertNull(point);
 
 		TEST_PREFERENCES.setPoint(KEY, new Point(42, 84));
 
 		point = TEST_PREFERENCES.getPoint(KEY);
 		assertNotNull(point);
 		assertEquals(42, point.getX(), 0.0);
 		assertEquals(84, point.getY(), 0.0);
 	}
 
 	/**
 	 * Test get string.
 	 */
 	@Test
 	public void testGetString() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test has preference.
 	 */
 	@Test
 	public void testHasPreference() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test remove all preferences.
 	 */
 	@Test
 	public void testRemoveAllPreferences() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test remove preference.
 	 */
 	@Test
 	public void testRemovePreference() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test set dimension.
 	 */
 	@Test
 	public void testSetDimension() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test set int.
 	 */
 	@Test
 	public void testSetInt() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test set point.
 	 */
 	@Test
 	public void testSetPoint() {
 		// Not yet implemented
 	}
 
 	/**
 	 * Test set string.
 	 */
 	@Test
 	public void testSetString() {
 		// Not yet implemented
 	}
 
 }
