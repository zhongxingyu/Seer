 /*******************************************************************************
  * Copyright 2011 Andrew Jenkins
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.ajj.robodtn.sdnvlibTest;
 
 import junit.framework.TestCase;
 import java.util.Arrays;
 import com.ajj.robodtn.sdnvlib.Sdnv;
 
 public class SdnvTest extends TestCase {
 
 	protected void setUp() {
 		
 	}
 	
 	public static class SdnvTestPair {
 		public SdnvTestPair(long value, byte [] bytes, String byteString) {
 			this.value = value;
 			this.bytes = bytes;
 			this.byteString = byteString;
 		}
 		public long value;
 		public byte [] bytes;
 		public String byteString;
 	}
 	
 	public static final SdnvTestPair [] testpairs = new SdnvTestPair [] {
 		//RFC5050 test 1, SDNV i-d test 1
 		new SdnvTestPair(0xABC,  
 						 new byte [] {(byte) 0x95, (byte) 0x3C},
 						 "953c"),
 
 		//RFC5050 test 2, SDNV i-d test 2
 		new SdnvTestPair(0x1234, 
 						 new byte [] {(byte) 0xA4, (byte) 0x34},
 						 "a434"),
 						 
 		//RFC5050 test 3, SDNV i-d test 3
 		new SdnvTestPair(0x4234,
 						 new byte [] {(byte) 0x81, (byte) 0x84, (byte) 0x34},
 						 "818434"),
 						 
 		//SDNV i-d ex. 1
 		new SdnvTestPair(0x01, 
 						 new byte [] {(byte) 0x01},
 						 "1"),
 
 		//SDNV i-d ex. 2
 		new SdnvTestPair(128,
 						 new byte [] {(byte) 0x81, (byte) 0x00},
 						 "8100"),
 						 
 		//SDNV test 4						 
 		new SdnvTestPair(0x7F, 
 						 new byte [] {(byte) 0x7F},
 					 	 "7f"),
 					 	 
 		//Other test pairs.
 		new SdnvTestPair(0x90,
 						 new byte [] {(byte) 0x81, (byte) 0x10},
						 "8110")
 	};
 	
 
 	
 	public void testSdnvs() {
 		// Verify SDNV conversions.
 		for(int i = 0; i < testpairs.length; i++) {
 			Sdnv fromBytes = new Sdnv(testpairs[i].bytes);
 			byte [] fromBytesArray = fromBytes.getBytes();
 			String asHexString = fromBytes.getBytesAsHexString();
 			assertTrue(Arrays.equals(fromBytesArray, testpairs[i].bytes));
 			assertTrue(fromBytes.getValue() == testpairs[i].value);
 			assertEquals(testpairs[i].byteString, asHexString);
 			fromBytes.setByBytesString(testpairs[i].byteString, 16);
 			assertEquals(testpairs[i].value, fromBytes.getValue());
 			
 			Sdnv fromValue = new Sdnv(testpairs[i].value);
 			assertTrue(Arrays.equals(fromValue.getBytes(), testpairs[i].bytes));
 			assertTrue(fromValue.getValue() == testpairs[i].value);
 			assertEquals(testpairs[i].byteString, fromValue.getBytesAsHexString());
 		}
 		
 		// Verify that updating an SDNV works.
 		Sdnv fromBytes = new Sdnv(testpairs[0].bytes);
 		assertTrue(Arrays.equals(fromBytes.getBytes(), testpairs[0].bytes));
 		assertTrue(fromBytes.getValue() == testpairs[0].value);
 		fromBytes.setByBytes(testpairs[1].bytes);
 		assertTrue(Arrays.equals(fromBytes.getBytes(), testpairs[1].bytes));
 		assertTrue(fromBytes.getValue() == testpairs[1].value);
 		fromBytes.setByValue(testpairs[2].value);
 		assertTrue(Arrays.equals(fromBytes.getBytes(), testpairs[2].bytes));
 		assertTrue(fromBytes.getValue() == testpairs[2].value);
 	}
 	
 	public static class SdnvIndexTestPair {
 		public SdnvIndexTestPair(long value, byte [] bytes, int index) {
 			this.value = value;
 			this.bytes = bytes;
 			this.index = index;
 		}
 		public long value;
 		public byte [] bytes;
 		public int index;
 	}
 	
 	private static final SdnvIndexTestPair [] indextestpairs = {
 		new SdnvIndexTestPair(0xABC,  new byte [] {(byte) 0x13, (byte) 0x95, (byte) 0x3C}, 1),
 		new SdnvIndexTestPair(0x1234, new byte [] {(byte) 0x00, (byte) 0xFE, (byte) 0xA4, (byte) 0x34}, 2)
 	};
 	
 	public void testSdnvIndexes() {
 		for(int i = 0; i < indextestpairs.length; i++) {
 			Sdnv fromBytes = new Sdnv(indextestpairs[i].bytes, indextestpairs[i].index);
 			assertTrue(fromBytes.getValue() == indextestpairs[i].value);
 		}
 	}
 	
 	public void testSdnvStrings() {
 		Sdnv s = new Sdnv(0xABC);
 		String str = s.getBytesAsHexString();
 		
 		assertEquals("953c", str);
 	}
 }
