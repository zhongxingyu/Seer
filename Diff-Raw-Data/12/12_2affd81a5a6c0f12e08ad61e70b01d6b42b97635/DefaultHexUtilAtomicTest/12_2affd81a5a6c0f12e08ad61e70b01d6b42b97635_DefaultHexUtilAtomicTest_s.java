 package au.net.netstorm.boost.util.format;
 
 import au.net.netstorm.boost.test.core.BoooostCase;
 
 public final class DefaultHexUtilAtomicTest extends BoooostCase {
    private static final String HEX_STRING1 = "0123456789ABCDEFFEDCBA9876543210";
     private static final String HEX_STRING2 = "00000000";
    private static final String HEX_STRING3 = "FFFFFFFFFF";
     private static final String INVALID_HEX_STRING = "12abGXWTY";
     private static final String INVALID_HEX_CHAR_EXCEPTION = "Invalid hex character: G";
     // DEBT LineLength {
     private static final byte[] HEX_BYTES1 = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98, (byte) 0x76, (byte) 0x54, (byte) 0x32, (byte) 0x10};
     private static final byte[] HEX_BYTES2 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
     private static final byte[] HEX_BYTES3 = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
     // } DEBT LineLength
     private HexUtil subject = new DefaultHexUtil();
 
     public void testStringToBytes() {
         checkBytes(HEX_STRING1, HEX_BYTES1);
         checkBytes(HEX_STRING2, HEX_BYTES2);
         checkBytes(HEX_STRING3, HEX_BYTES3);
     }
 
     public void testBytesToString() {
         checkString(HEX_BYTES1, HEX_STRING1);
         checkString(HEX_BYTES2, HEX_STRING2);
         checkString(HEX_BYTES3, HEX_STRING3);
     }
 
     public void testInvalidHexString() {
         try {
             subject.toBytes(INVALID_HEX_STRING);
             fail();
         } catch (IllegalArgumentException expected) {
             String actual = expected.getMessage();
             assertEquals(INVALID_HEX_CHAR_EXCEPTION, actual);
         }
     }
 
     private void checkString(byte[] bytes, String expected) {
         String actual = subject.toString(bytes);
         assertEquals(expected, actual);
     }
 
     private void checkBytes(String string, byte[] expected) {
         byte[] actual = subject.toBytes(string);
         assertEquals(expected, actual);
     }
 }
