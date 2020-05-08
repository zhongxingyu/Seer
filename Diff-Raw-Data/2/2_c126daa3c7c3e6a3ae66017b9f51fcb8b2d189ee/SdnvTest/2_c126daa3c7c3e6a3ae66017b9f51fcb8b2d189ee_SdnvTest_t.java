 package com.ajj.robodtn.test;
 
 import junit.framework.TestCase;
 import com.ajj.robodtn.Sdnv;
 import java.util.Arrays;
 
 public class SdnvTest extends TestCase {
 
 	protected void setUp() {
 		
 	}
 	
 	public class SdnvTestPair {
 		public SdnvTestPair(long value, byte [] bytes) {
 			this.value = value;
 			this.bytes = bytes;
 		}
 		public long value;
 		public byte [] bytes;
 	}
 	
 	public final SdnvTestPair [] testpairs = {
 		new SdnvTestPair(0xABC,  new byte [] {(byte) 0x95, (byte) 0x3C}),				//RFC5050 test 1, SDNV i-d test 1
 		new SdnvTestPair(0x1234, new byte [] {(byte) 0xA4, (byte) 0x34}),				//RFC5050 test 2, SDNV i-d test 2
		new SdnvTestPair(0x4234, new byte [] {(byte) 0x81, (byte) 0x84, (byte) 0x34}),	//RFC5050 test 3, SDNV i-d test 3
 		new SdnvTestPair(0x01, new byte [] {(byte) 0x01}),								//SDNV i-d ex. 1
 		new SdnvTestPair(128, new byte [] {(byte) 0x81, (byte) 0x00}),					//SDNV i-d ex. 2
 		new SdnvTestPair(0x7F, new byte [] {(byte) 0x7F}),								//SDNV test 4
 		new SdnvTestPair(0x90, new byte [] {(byte) 0x81, (byte) 0x10})
 	};
 	
 	public void testSdnvs() {
 		// Verify SDNV conversions.
 		for(int i = 0; i < testpairs.length; i++) {
 			Sdnv fromBytes = new Sdnv(testpairs[i].bytes);
 			assertTrue(Arrays.equals(fromBytes.getBytes(), testpairs[i].bytes));
 			assertTrue(fromBytes.getValue() == testpairs[i].value);
 			
 			Sdnv fromValue = new Sdnv(testpairs[i].value);
 			assertTrue(Arrays.equals(fromValue.getBytes(), testpairs[i].bytes));
 			assertTrue(fromValue.getValue() == testpairs[i].value);
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
 	
 	public class SdnvIndexTestPair {
 		public SdnvIndexTestPair(long value, byte [] bytes, int index) {
 			this.value = value;
 			this.bytes = bytes;
 			this.index = index;
 		}
 		public long value;
 		public byte [] bytes;
 		public int index;
 	}
 	
 	private final SdnvIndexTestPair [] indextestpairs = {
 		new SdnvIndexTestPair(0xABC,  new byte [] {(byte) 0x13, (byte) 0x95, (byte) 0x3C}, 1),
 		new SdnvIndexTestPair(0x1234, new byte [] {(byte) 0x00, (byte) 0xFE, (byte) 0xA4, (byte) 0x34}, 2)
 	};
 	
 	public void testSdnvIndexes() {
 		for(int i = 0; i < indextestpairs.length; i++) {
 			Sdnv fromBytes = new Sdnv(indextestpairs[i].bytes, indextestpairs[i].index);
 			assertTrue(fromBytes.getValue() == indextestpairs[i].value);
 		}
 	}
 }
