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
 
 package de.questmaster.fatremote.datastructures.test;
 
 import junit.framework.Assert;
 import android.test.AndroidTestCase;
 import de.questmaster.fatremote.datastructures.FATRemoteEvent;
 
 /**
  * @author daniel
  *
  */
 public class FATRemoteEventTest extends AndroidTestCase {
 
 	private FATRemoteEvent mRemoteEvent = null;
 	
 	private short expectedFirst = 0x48;
 	private short expectedSecond = 0x12;
 	private short expectedThird = 3;
 	private short expectedFourth = 4;
 	private int expectedCodeLength = 4;
 	private short[] expectedPayload = new short[] { 0, 1 , 2, 3, 4, 5};
 	
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		mRemoteEvent = new FATRemoteEvent(expectedThird, expectedFourth, expectedPayload);
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 		mRemoteEvent = null;
 	}
 
 	/**
 	 * Check for correct setup.
 	 */
 	public void testAndroidTestCaseSetupProperly() {
 		Assert.assertNotNull(mRemoteEvent);
 	}
 		
 	/**
 	 * Test method for {@link de.questmaster.fatremote.datastructures.FATRemoteEvent#FATRemoteEvent(short, short)}.
 	 */
 	public void testFATRemoteEvent() {
 		FATRemoteEvent remoteEvent = new FATRemoteEvent(expectedThird, expectedFourth);
 		Assert.assertNotNull(remoteEvent);
 		Assert.assertNotNull(remoteEvent.getCodePayload());
 
 		short[] code = remoteEvent.getRemoteCode();
 		Assert.assertEquals(expectedCodeLength, code.length);
 		Assert.assertEquals(mRemoteEvent, remoteEvent);
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.datastructures.FATRemoteEvent#getRemoteCode()}.
 	 */
 	public void testGetRemoteCode() {
 		short[] code = mRemoteEvent.getRemoteCode();
 		
 		Assert.assertEquals(expectedCodeLength, code.length);
 		Assert.assertEquals(expectedFirst, code[0]);
 		Assert.assertEquals(expectedSecond, code[1]);
 		Assert.assertEquals(expectedThird, code[2]);
 		Assert.assertEquals(expectedFourth, code[3]);
 	}
 
 	/**
 	 * Test method for {@link de.questmaster.fatremote.datastructures.FATRemoteEvent#getCodePayload()}.
 	 */
 	public void testGetCodePayload() {
 		short[] payload = mRemoteEvent.getCodePayload();
 		
 		Assert.assertNotNull(payload);
 		Assert.assertEquals(expectedPayload.length, payload.length);
 		for (int i = 0; i < payload.length; i++) {
 			Assert.assertEquals(expectedPayload[i], payload[i]);
 		}
 	}
 
 	public void testToString() {
		String expected = expectedFirst + "," + expectedSecond + "," + expectedThird + "," + expectedFourth;
 		
 		assertEquals(expected, mRemoteEvent.toString());
 	}
 	
 	public void testEquals() {
 		FATRemoteEvent remoteEvent = new FATRemoteEvent(expectedThird, expectedFourth, expectedPayload);
 		
 		assertEquals(mRemoteEvent, mRemoteEvent);
 		assertEquals(mRemoteEvent, remoteEvent);
 		assertEquals(remoteEvent, mRemoteEvent);
 		
 	    assertFalse(mRemoteEvent.equals(null));
 	    assertFalse(mRemoteEvent.equals(new Object()));
 	}
 
 	public void testHashCode() {
 		FATRemoteEvent remoteEvent = new FATRemoteEvent(expectedThird, expectedFourth, expectedPayload);
 
 		assertTrue(mRemoteEvent.hashCode() == mRemoteEvent.hashCode());
 	    assertTrue(mRemoteEvent.hashCode() == remoteEvent.hashCode());
 	}
 
 }
