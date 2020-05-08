 /*
  * Copyright (C) 2010 Daniel Jacobi
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.questmaster.fatremote.test;
 
 import java.net.UnknownHostException;
 
 import android.database.Cursor;
 import android.test.AndroidTestCase;
 import de.questmaster.fatremote.FatDevicesDbAdapter;
 import de.questmaster.fatremote.datastructures.FATDevice;
 
 /**
  * @author daniel
  *
  */
 public class FatDevicesDbAdapterTest extends AndroidTestCase {
 
 	private FatDevicesDbAdapter mTestObject = null;
 	
 	private String mExpectedName1 = "Name1";
 	private String mExpectedIp1 = "1.1.1.1";
 //	private int mExpectedPort1 = FATDevice.FAT_REMOTE_PORT;
 	private boolean mExpectedAutodetected1 = true;
 	private String mExpectedName2 = "Name2";
 	private String mExpectedIp2 = "2.2.2.2";
 	private int mExpectedPort2 = FATDevice.FAT_REMOTE_PORT;
 	private boolean mExpectedAutodetected2 = true;
 	
 	/* (non-Javadoc)
 	 * @see android.test.AndroidTestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		mTestObject = new FatDevicesDbAdapter(mContext);
 		
 		mTestObject.open();
 		
 		mTestObject.deleteAllFatDevices();
 
 		mTestObject.createFatDevice(new FATDevice(mExpectedName1, mExpectedIp1, mExpectedAutodetected1));
 		mTestObject.createFatDevice(new FATDevice(mExpectedName2, mExpectedIp2, mExpectedAutodetected2));
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see android.test.AndroidTestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 		mTestObject.close();
 
 		mTestObject = null;
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#FatDevicesDbAdapter(android.content.Context)}.
 	 */
 	public final void testFatDevicesDbAdapter() {
 		assertNotNull(mTestObject);
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#open()}.
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#isOpen()}.
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#close()}.
 	 */
 	public final void testOpenIsOpenClose() {
 		assertTrue(mTestObject.isOpen());
 		
 		mTestObject.close();
 		
 		assertFalse(mTestObject.isOpen());
 
 		mTestObject.open();
 
 		assertTrue(mTestObject.isOpen());
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#createFatDevice(java.lang.String, java.lang.String, int, boolean)}.
 	 */
 	public final void testCreateFatDevice() {
 		String expectedName3 = "Name3";
 		String expectedIp3 = "3.3.3.3";
 		int expectedPort3 = FATDevice.FAT_REMOTE_PORT;
 		boolean expectedAutodetected3f = false;
 		boolean expectedAutodetected3t = true;
 		int expectedCount = 4;
 		long id = -1;
 		long id2 = -1;
 
 		// create
 		try {
 			id = mTestObject.createFatDevice(new FATDevice(expectedName3, expectedIp3, expectedAutodetected3f));
 			id2 = mTestObject.createFatDevice(new FATDevice(expectedName3, expectedIp3, expectedAutodetected3t));
 		} catch (UnknownHostException e) {
 			fail(e.getLocalizedMessage());
 		}
 		
 		FATDevice dev = mTestObject.fetchFatDeviceTyp(id);
 		FATDevice dev2 = mTestObject.fetchFatDeviceTyp(id2);
 
 		// is it correctly created?
 		assertEquals(expectedCount, mTestObject.getAllFatDevicesCount());
 
 		assertTrue(expectedName3.equals(dev.getName()));
 		assertTrue(expectedIp3.equals(dev.getIp()));
 		assertEquals(expectedPort3, dev.getPort());
 		assertEquals(expectedAutodetected3f, dev.isAutoDetected());
 
 		assertTrue(expectedName3.equals(dev2.getName()));
 		assertTrue(expectedIp3.equals(dev2.getIp()));
 		assertEquals(expectedPort3, dev2.getPort());
 		assertEquals(expectedAutodetected3t, dev2.isAutoDetected());
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#deleteFatDevice(long)}.
 	 */
 	public final void testDeleteFatDevice() {
 		int count = mTestObject.getAllFatDevicesCount();
 		long rowId = mTestObject.fetchFatDeviceId(mExpectedIp2);
 		
 		// delete
 		boolean result = mTestObject.deleteFatDevice(rowId);
 		// delete nothing
 		boolean result2 = mTestObject.deleteFatDevice(-1);
 		
 		Cursor cur = mTestObject.fetchAllFatDevices();
 		cur.moveToFirst();
 
 		// one count less and is the right one still there?
 		assertTrue(result);
 		assertFalse(result2);
 		assertEquals(count - 1, mTestObject.getAllFatDevicesCount());
 		assertTrue(cur.getString(1).equals(mExpectedName1));
 		
 		cur.close();
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#deleteAllFatDevices()}.
 	 */
 	public final void testDeleteAllFatDevices() {
 		int expectedCount = 0;
 		
 		int count = mTestObject.getAllFatDevicesCount();
 		
 		// delete all
 		boolean result = mTestObject.deleteAllFatDevices();
 		// delete nothing
 		boolean result2 = mTestObject.deleteAllFatDevices();
 		
 		// there were items before, now they are gone
 		assertTrue(result);
 		assertFalse(result2);
 		assertTrue(count > 0);
 		assertEquals(expectedCount, mTestObject.getAllFatDevicesCount());
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#fetchAllFatDevices()}.
 	 */
 	public final void testFetchAllFatDevices() {
 		int expectedCount = 2;
 		
 		// get all
 		Cursor cur = mTestObject.fetchAllFatDevices();
 		cur.moveToFirst();
 				
 		// there are two items and they are the expected ones
 		assertNotNull(cur);
 		assertEquals(expectedCount, cur.getCount());
 		assertTrue(mExpectedName1.equals(cur.getString(1)));
 		cur.moveToNext();
 		assertTrue(mExpectedName2.equals(cur.getString(1)));
 
 		cur.close();
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#fetchFatDeviceTyp(long)}.
 	 */
 	public final void testFetchFatDeviceTyp() {
 		long rowId = mTestObject.fetchFatDeviceId(mExpectedIp2);
 		
 		// get device
 		FATDevice dev = mTestObject.fetchFatDeviceTyp(rowId);
 		// illegal/not used row id
 		FATDevice dev2 = mTestObject.fetchFatDeviceTyp(-1);
 
 		// retrieved correct device?
 		assertNotNull(dev);
 		assertNull(dev2);
 		assertTrue(mExpectedName2.equals(dev.getName()));
 		assertTrue(mExpectedIp2.equals(dev.getIp()));
 		assertEquals(mExpectedPort2, dev.getPort());
 		assertEquals(mExpectedAutodetected2, dev.isAutoDetected());
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#fetchFatDeviceId(java.lang.String)}.
 	 */
 	public final void testFetchFatDeviceId() {
 		String illegalIp = "murks";
 		
 		// get device row id
 		long rowId = mTestObject.fetchFatDeviceId(mExpectedIp2);
 		// illegal IP
 		long rowId2 = mTestObject.fetchFatDeviceId(illegalIp);
 		
 		FATDevice dev = mTestObject.fetchFatDeviceTyp(rowId);
 
 		// retrieved correct device?
 		assertTrue(rowId >= 0);
 		assertFalse(rowId2 >= 0);
 		assertTrue(mExpectedName2.equals(dev.getName()));
 		assertTrue(mExpectedIp2.equals(dev.getIp()));
 		assertEquals(mExpectedPort2, dev.getPort());
 		assertEquals(mExpectedAutodetected2, dev.isAutoDetected());
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#getAllFatDevicesCount()}.
 	 */
 	public final void testGetAllFatDevicesCount() {
 		int expectedCount = 2;
 		
 		assertEquals(expectedCount, mTestObject.getAllFatDevicesCount());
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.FatDevicesDbAdapter#updateFatDevice(long, java.lang.String, java.lang.String, int, boolean)}.
 	 */
 	public final void testUpdateFatDevice() {
 		String expectedName3 = "Name3";
 		String expectedIp3 = "3.3.3.3";
 		int expectedPort3 = 9999;
		boolean expectedAutodetected3 = false;
 		int expectedCount = 2;
 		boolean result = false;
 		boolean result2 = true;
 
 		long id = mTestObject.fetchFatDeviceId("1.1.1.1");
 				
 		try {
 			// update
			result = mTestObject.updateFatDevice(id, new FATDevice(expectedName3, expectedIp3, expectedAutodetected3));
 			// illlegal row id
			result2 = mTestObject.updateFatDevice(-1, new FATDevice(expectedName3, expectedIp3, expectedAutodetected3));
 		} catch (UnknownHostException e) {
 			fail(e.getLocalizedMessage());
 		}
 		
 		FATDevice dev = mTestObject.fetchFatDeviceTyp(id);
 
 		// is it correctly updated?
 		assertTrue(result);
 		assertFalse(result2);
 		assertEquals(expectedCount, mTestObject.getAllFatDevicesCount());
 		assertTrue(expectedName3.equals(dev.getName()));
 		assertTrue(expectedIp3.equals(dev.getIp()));
 		assertEquals(expectedPort3, dev.getPort());
		assertEquals(expectedAutodetected3, dev.isAutoDetected());
 	}
 
 }
