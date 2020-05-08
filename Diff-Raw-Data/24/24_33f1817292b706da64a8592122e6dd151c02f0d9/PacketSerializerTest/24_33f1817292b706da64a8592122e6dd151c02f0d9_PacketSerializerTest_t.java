 /*
  * Copyright 2010-2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package com.googlecode.jcimd;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.googlecode.jcimd.ApplicationPacketSequenceNumberGenerator;
 import com.googlecode.jcimd.Packet;
 import com.googlecode.jcimd.PacketSerializer;
 import com.googlecode.jcimd.Parameter;
 
 public class PacketSerializerTest {
 
 	public static final char STX = 0x02;
 	public static final char TAB = '\t'; // ASCII value is 9
 	public static final char COLON = ':'; // ASCII value is 58
 	public static final char ETX = 0x03;
 	
 	private PacketSerializer serializer;
 
 	@Before
 	public void setUp() throws Exception {
 		serializer = new PacketSerializer();
 		serializer.setSequenceNumberGenerator(
 				new ApplicationPacketSequenceNumberGenerator());
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	protected int calculateCheckSum(byte[] bytes) {
 		int sum = 0;
 		for (byte b : bytes) {
 			sum += b;
 			sum &= 0xFF;
 		}
 		return sum;
 	}
 	
 	@Test
 	public void serializeLoginCommand() throws Exception {
 		Packet command = new Packet(1,
 				new Parameter(10, "username"),
 				new Parameter(11, "password"));
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		byte[] expected = concat(
 				("" + STX + "01" + COLON + "001" + TAB).getBytes(),
 				("010" + COLON + "username" + TAB).getBytes(),
 				("011" + COLON + "password" + TAB).getBytes(),
 				("CS"/* two-bytes for checksum */ + ETX).getBytes());
 		int checksum = calculateCheckSum(Arrays.copyOfRange(expected, 0, expected.length - 3));
 		// check sum needs to be 0-9 A-F (upper-case)
 		String checkSumHexString = String.format("%02X", checksum);
 		expected[expected.length - 3] = (byte) checkSumHexString.charAt(0);
 		expected[expected.length - 2] = (byte) checkSumHexString.charAt(1);
 		serializer.serialize(command, outputStream);
 		assertArrayEquals(expected, outputStream.toByteArray());
 	}
 
 	@Test
 	public void deserializeLoginCommand() throws Exception {
 		byte[] bytes = concat(
 				("" + STX + "01" + COLON + "001" + TAB).getBytes(),
 				("010" + COLON + "username" + TAB).getBytes(),
 				("011" + COLON + "password" + TAB).getBytes(),
 				("CS"/* two-bytes for checksum */ + ETX).getBytes());
 		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
 		String checkSumHexString = String.format("%02X", checksum);
 		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
 		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 		Packet command = new Packet(1, 1,
 				new Parameter(10, "username"),
 				new Parameter(11, "password"));
 		Packet actual = serializer.deserialize(inputStream);
 		assertEquals(command, actual);
 	}
 
  @Test
  public void deserializeLoginCommandWithoutChkSum() throws Exception {
    PacketSerializer ser = new PacketSerializer("ser", false);
    byte[] bytes = concat(
        ("" + STX + "01" + COLON + "001" + TAB).getBytes(),
        ("010" + COLON + "username" + TAB).getBytes(),
        ("011" + COLON + "password" + TAB).getBytes(),
        ("" + ETX).getBytes());
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    Packet command = new Packet(1, 1,
        new Parameter(10, "username"),
        new Parameter(11, "password"));
    Packet actual = ser.deserialize(inputStream);
    assertEquals(command, actual);
  }
  
 	@Test
 	public void deserializeLoginCommandWithExtraDataBetweenPackets() throws Exception {
 		byte[] bytes = concat(
 				("noise" + STX + "01" + COLON + "001" + TAB).getBytes(),
 				("010" + COLON + "username" + TAB).getBytes(),
 				("011" + COLON + "password" + TAB).getBytes(),
 				("CS"/* two-bytes for checksum */ + ETX + "noise").getBytes());
 		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 5, bytes.length - 8));
 		String checkSumHexString = String.format("%02X", checksum);
 		bytes[bytes.length - 8] = (byte) checkSumHexString.charAt(0);
 		bytes[bytes.length - 7] = (byte) checkSumHexString.charAt(1);
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 		Packet command = new Packet(1, 1,
 				new Parameter(10, "username"),
 				new Parameter(11, "password"));
 		Packet actual = serializer.deserialize(inputStream);
 		assertEquals(command, actual);
 	}
 
 	@Test(expected=IOException.class)
 	public void deserializeCommandWithMissingColon() throws Exception {
 		byte[] bytes = concat(
 				("" + STX + "01" + COLON + "001" + TAB).getBytes(),
 				("010" + /*COLON +*/ "username" + TAB).getBytes(),
 				("011" + COLON + "password" + TAB).getBytes(),
 				("CS"/* two-bytes for checksum */ + ETX).getBytes());
 		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
 		String checkSumHexString = String.format("%02X", checksum);
 		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
 		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 		try {
 			serializer.deserialize(inputStream);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	@Test(expected=IOException.class)
 	public void deserializeCommandWithFourByteParameterType() throws Exception {
 		byte[] bytes = concat(
 				("" + STX + "01" + COLON + "001" + TAB).getBytes(),
 				("0100" + COLON + "username" + TAB).getBytes(),
 				("011" + COLON + "password" + TAB).getBytes(),
 				("CS"/* two-bytes for checksum */ + ETX).getBytes());
 		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
 		String checkSumHexString = String.format("%02X", checksum);
 		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
 		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 		try {
 			serializer.deserialize(inputStream);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	@Test(expected=IOException.class)
 	public void deserializeCommandWithNonHexCheckSum() throws Exception {
 		byte[] bytes = concat(
 				("" + STX + "01" + COLON + "001" + TAB).getBytes(),
 				("010" + COLON + "username" + TAB).getBytes(),
 				("011" + COLON + "password" + TAB).getBytes(),
 				("CS"/* two-bytes for checksum */ + ETX).getBytes());
 		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
 		bytes[bytes.length - 3] = (byte) (checksum & 0xFF00 >> 8);
 		bytes[bytes.length - 2] = (byte) (checksum & 0x00FF);
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 		try {
 			serializer.deserialize(inputStream);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	private static byte[] concat(byte[]... arrays) {
 		if (arrays.length == 0) {
 			throw new IllegalArgumentException("No arrays to concat");
 		}
 		if (arrays.length == 1) {
 			return arrays[0];
 		}
 		int totalLength = 0;
 		for (int i = 0; i < arrays.length; i++) {
 			totalLength += arrays[i].length;
 		}
 	    final byte[] result = (byte[]) java.lang.reflect.Array.
 	            newInstance(arrays[0].getClass().getComponentType(), totalLength);
 	    int offset = 0;
 	    for (byte[] a : arrays) {
 		    System.arraycopy(a, 0, result, offset, a.length);
 		    offset += a.length;
 		}
 	    return result;
 	}
 
 	@Ignore("Escaping characters should be done via UserData implementation")
 	@Test
 	public void deserializeUserDataParameter() throws Exception {
 		byte[] bytes = concat(
 				("" + STX + "03:001" + TAB).getBytes("ASCII"),
 				("033:_XX( curly braces _XX) _XX< square brackets _XX>" + TAB).getBytes("ASCII"),
 				("CS"/* two-bytes for checksum */ + ETX).getBytes("ASCII"));
 		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
 		String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
 		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
 		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
 		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 		Packet command = new Packet(3, 1,
 				new Parameter(33, "{ curly braces } [ square brackets ]"));
 		Packet actual = serializer.deserialize(inputStream);
 		assertEquals(command, actual);
 	}
 
 }
