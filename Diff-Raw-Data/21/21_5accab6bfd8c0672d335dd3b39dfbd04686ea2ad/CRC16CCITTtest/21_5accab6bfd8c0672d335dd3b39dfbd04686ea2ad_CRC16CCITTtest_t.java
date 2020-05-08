 package org.hbird.core.commons.util.crc;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CRC16CCITTtest {
 
 	private static final String TEST_INPUT = "123456789";

 	private static final int EXPECTED_CRC = 0x29B1;

 	private static final int EXPECTED_RESET_CRC = 0xFFFF;
 
 	private CRC16CCITT crc;
 
 	@Before
 	public void setUp() throws Exception {
 		crc = new CRC16CCITT();
 	}
 
	@Test
	public void testUpdateInt() {
 		for (byte b : TEST_INPUT.getBytes()) {
 			crc.update(b);
 		}
 		assertEquals(EXPECTED_CRC, crc.getValue());
 	}
 
 	@Test
 	public void testUpdateAndReset() {
 		testUpdateInt();
 		crc.reset();
 		assertEquals(EXPECTED_RESET_CRC, crc.getValue());
 	}
 
	@Test
 	public void testUpdateByteWithOffset() {
		crc.update(TEST_INPUT.getBytes(), 0, TEST_INPUT.getBytes().length);
		assertEquals(EXPECTED_CRC, crc.getValue());
 	}
 
 }
