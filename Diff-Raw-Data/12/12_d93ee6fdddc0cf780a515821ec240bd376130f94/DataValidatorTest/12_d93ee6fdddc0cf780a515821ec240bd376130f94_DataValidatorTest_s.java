 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 12, 2012
  */
 package edu.cmu.sv.arinc838.validation;
 
 import static org.testng.AssertJUnit.assertEquals;
 import static org.testng.AssertJUnit.fail;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import edu.cmu.sv.arinc838.builder.IntegrityDefinitionBuilder.IntegrityType;
 import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
 import edu.cmu.sv.arinc838.util.Converter;
 
 public class DataValidatorTest {
 
 	private String str64kMax = "";
 
 	@BeforeTest
 	public void beforeTest() {
 		for (int i = 0; i < 65535; i++) {
 			str64kMax += "X";
 		}
 	}
 
 	@Test
 	public void testValidateUint32() {
 
 		assertEquals((long) 0, DataValidator.validateUint32(0));
 		assertEquals((long) 1234, DataValidator.validateUint32(1234));
 		long max = (long) Math.pow(2, 32);
 		assertEquals(max, DataValidator.validateUint32(max));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateUint32Negative() {
 		DataValidator.validateUint32(-1);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateUint32OutOfRange() {
 		DataValidator.validateUint32(Long.MAX_VALUE);
 	}
 
 	@Test
 	public void testValidateStr64kBinary() {
 		String inputStr = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789 -_=+`~'\"[]{}\\|;:,./?!@#$%^*()";
 		assertEquals(inputStr, DataValidator.validateStr64kBinary(inputStr));
 		assertEquals(str64kMax, DataValidator.validateStr64kBinary(str64kMax));
 		assertEquals("", DataValidator.validateStr64kBinary(""));
 
 		assertEquals("&lt", DataValidator.validateStr64kBinary("&lt"));
 		assertEquals("hello&lt", DataValidator.validateStr64kBinary("hello&lt"));
 		assertEquals("&gt", DataValidator.validateStr64kBinary("&gt"));
 		assertEquals("&gtthere", DataValidator.validateStr64kBinary("&gtthere"));
 		assertEquals("&amp", DataValidator.validateStr64kBinary("&amp"));
 		assertEquals("hello&amp&ampthere",
 				DataValidator.validateStr64kBinary("hello&amp&ampthere"));
 
 	}
 
 	@Test
 	public void testValidateStr64kXmlMaxSizeWithEscapedChars() {
 		// this creates a max-sized string, with an escaped '&' as the last
 		// character.
 		// This will make the absolute length > than the max string length, but
 		// the
 		// true length will be = the max length, and should still be valid
 		String str64kMaxWithEscaped = str64kMax.substring(0,
 				str64kMax.length() - 1) + "&amp";
 		assertEquals(str64kMaxWithEscaped,
 				DataValidator.validateStr64kXml(str64kMaxWithEscaped));
 	}
 
 	@Test
 	public void testValidateStr64kXml() {
 		String inputStr = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789 -_=+`~'\"[]{}\\|;:,./?!@#$%^*()";
 		assertEquals(inputStr, DataValidator.validateStr64kXml(inputStr));
 		assertEquals(str64kMax, DataValidator.validateStr64kXml(str64kMax));
 		assertEquals("", DataValidator.validateStr64kXml(""));
 
 		assertEquals("&lt", DataValidator.validateStr64kXml("&lt"));
 		assertEquals("hello&lt", DataValidator.validateStr64kXml("hello&lt"));
 		assertEquals("&gt", DataValidator.validateStr64kXml("&gt"));
 		assertEquals("&gtthere", DataValidator.validateStr64kXml("&gtthere"));
 		assertEquals("&amp", DataValidator.validateStr64kXml("&amp"));
 		assertEquals("hello&amp&ampthere",
 				DataValidator.validateStr64kXml("hello&amp&ampthere"));
 	}
 
 	@Test
 	public void testValidateStr64kXmlWithNonEscapedChars() {
 
 		try {
 			DataValidator.validateStr64kXml("1 > 2");
 			fail("Did not throw IllegalArgumentException for non-escaped >");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateStr64kXml("1 < 2");
 			fail("Did not throw IllegalArgumentException for non-escaped <");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateStr64kXml("Mumford & Sons");
 			fail("Did not throw IllegalArgumentException for non-escaped &");
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateStr64kXmlTooLarge() {
 		DataValidator.validateStr64kXml(str64kMax + "Y");
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateStr64kBinaryTooLarge() {
 		DataValidator.validateStr64kBinary(str64kMax + "Y");
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateStr64kXmlNull() {
 		DataValidator.validateStr64kXml(null);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateStr64kBinaryNull() {
 		DataValidator.validateStr64kBinary(null);
 	}
 
 	@Test
 	public void testValidateFileFormatVersion() {
 		assertEquals(
 				SoftwareDefinitionFileBuilder.DEFAULT_FILE_FORMAT_VERSION,
 				DataValidator
						.validateFileFormatVersion(SoftwareDefinitionFileBuilder.DEFAULT_FILE_FORMAT_VERSION));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateFileFormatVersionInvalid() {
 		DataValidator.validateFileFormatVersion(new byte[] { 1, 2, 3, 4 });
 	}
 
 	@Test
 	public void testValidateIntegrityType() {
 		assertEquals(IntegrityType.CRC16.getType(),
 				DataValidator.validateIntegrityType(IntegrityType.CRC16
 						.getType()));
 		assertEquals(IntegrityType.CRC32.getType(),
 				DataValidator.validateIntegrityType(IntegrityType.CRC32
 						.getType()));
 		assertEquals(IntegrityType.CRC64.getType(),
 				DataValidator.validateIntegrityType(IntegrityType.CRC64
 						.getType()));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateIntegrityTypeInvalid() {
 		DataValidator.validateIntegrityType(-1);
 	}
 
 	@Test
 	public void testValidateIntegrityValue() {
 		assertEquals(Converter.hexToBytes("ABCD"),
 				DataValidator.validateIntegrityValue(Converter
 						.hexToBytes("ABCD")));
 		assertEquals(Converter.hexToBytes("ABCDEF01"),
 				DataValidator.validateIntegrityValue(Converter
 						.hexToBytes("ABCDEF01")));
 		assertEquals(Converter.hexToBytes("DEADBEEFDEADBEEF"),
 				DataValidator.validateIntegrityValue(Converter
 						.hexToBytes("DEADBEEFDEADBEEF")));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateIntegrityValueNull() {
 		DataValidator.validateIntegrityValue(null);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateIntegrityValueWrongSize() {
 		DataValidator.validateIntegrityValue(Converter.hexToBytes("ABC"));
 	}
 
 	@Test
 	public void testValidateList1() {
 		List<String> list1 = new ArrayList<String>();
 		list1.add("hello");
 		assertEquals(list1, DataValidator.validateList1(list1));
 
 		list1.add("world");
 		list1.add("!");
 
 		assertEquals(list1, DataValidator.validateList1(list1));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateList1Null() {
 		DataValidator.validateList1(null);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateList1EmptyList() {
 		DataValidator.validateList1(new ArrayList<String>());
 	}
 
 	@Test
 	public void testValidateSoftwarePartNumber() {
 		assertEquals("ACM47-1234-5678",
 				DataValidator.validateSoftwarePartNumber("ACM47-1234-5678"));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateSoftwarePartNumberNull() {
 		DataValidator.validateSoftwarePartNumber(null);
 	}
 
 	@Test
 	public void testValidateSoftwarePartNumberInvalidFormat() {
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM4-7-1234-5678");
 			fail("Did not fail on invalid format");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACm47-1234-5678");
 			fail("Did not fail on lower case letter");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("1CM37-1234-5678");
 		} catch (IllegalArgumentException e) {
 			fail("Failed incorrectly with a number first " + e.toString());
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM23-1234-5678");
 			fail("Did not fail on invalid check characters");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM47-12 34-5678");
 			fail("Did not fail on embedded spaces");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM47-123I-5678");
 			fail("Did not fail on illegal character I");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM47-123Q-5678");
 			fail("Did not fail on illegal character Q");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM47-O234-5678");
 			fail("Did not fail on illegal character O");
 		} catch (IllegalArgumentException e) {
 		}
 
 		try {
 			DataValidator.validateSoftwarePartNumber("ACM47-1234-5Z78");
 			fail("Did not fail on illegal character Z");
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	@Test
 	public void generateSoftwarePartNumber() {
 		assertEquals("ACM47-1234-5678",
 				DataValidator.generateSoftwarePartNumber("ACM??-1234-5678"));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void generateSoftwarePartNumberNull() {
 		DataValidator.generateSoftwarePartNumber(null);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void generateSoftwarePartNumberInvalid() {
 		DataValidator.generateSoftwarePartNumber("ACM47-O234-5678");
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateHexbin32Null() {
 		DataValidator.validateHexbin32((byte[]) null);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateHexbin32Not4Bytes() {
 		DataValidator.validateHexbin32(new byte[] { 1, 2, 3 });
 	}
 
 	@Test
 	public void testValidateHexbin32() {
 		byte[] hexBin32 = new byte[] { 1, 2, 3, 4 };
 		assertEquals(hexBin32, DataValidator.validateHexbin32(hexBin32));
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateHexbin64kNull() {
 		DataValidator.validateHexbin64k((byte[]) null);
 	}
 
 	@Test(expectedExceptions = IllegalArgumentException.class)
 	public void testValidateHexbin64kOutOfRange() {
 		DataValidator
 				.validateHexbin64k(new byte[DataValidator.HEXBIN64K_MAX_LENGTH + 1]);
 	}
 
 	@Test
 	public void testValidateHexbin64k() {
 		byte[] hexBin64k = new byte[DataValidator.HEXBIN64K_MAX_LENGTH];
 		Arrays.fill(hexBin64k, (byte) 0xAB);
 		assertEquals(hexBin64k, DataValidator.validateHexbin64k(hexBin64k));
 	}
 
 }
