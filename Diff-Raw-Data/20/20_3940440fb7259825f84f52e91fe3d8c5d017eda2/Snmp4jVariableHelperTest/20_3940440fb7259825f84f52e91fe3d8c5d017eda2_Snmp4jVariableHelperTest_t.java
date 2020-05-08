 //Snmp4jVariableHelperTest.java ----
 // History: 2010/02/09 - Create
 package jp.co.acroquest.tool.snmp.toolkit.helper;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.junit.Test;
 import org.snmp4j.smi.Integer32;
 import org.snmp4j.smi.IpAddress;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.TimeTicks;
 
 /**
  * Snmp4jVariableHelperNX̃eXgP[XB
 *
  * @author akiba
  */
 public class Snmp4jVariableHelperTest
 {
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(java.lang.String)}.
      */
     @Test
     public void testCreateOctetStringString_normal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OctetString octets = (OctetString) helper.createOctetString("abc");
         assertEquals("abc", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(java.lang.String)}.
      */
     @Test
     public void testCreateOctetStringString_empty()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OctetString octets = (OctetString) helper.createOctetString("");
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(java.lang.String)}.
      */
     @Test
     public void testCreateOctetStringString_null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OctetString octets = (OctetString) helper.createOctetString((String)null);
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(char[])}.
      */
     @Test
     public void testCreateOctetStringCharArray_normal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         char[] chars = new char[] { 'a', 'b', 'c' };
         OctetString octets = (OctetString) helper.createOctetString(chars);
         assertEquals("abc", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(char[])}.
      */
     @Test
     public void testCreateOctetStringCharArray_empty()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         char[] chars = new char[0];
         OctetString octets = (OctetString) helper.createOctetString(chars);
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(char[])}.
      */
     @Test
     public void testCreateOctetStringCharArray_null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         char[] chars = null;
         OctetString octets = (OctetString) helper.createOctetString(chars);
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(byte[])}.
      */
     @Test
     public void testCreateOctetStringByteArray_normal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         byte[] bytes = new byte[] { 0x61, 0x62, 0x63 };
         OctetString octets = (OctetString) helper.createOctetString(bytes);
         assertEquals("abc", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(byte[])}.
      */
     @Test
     public void testCreateOctetStringByteArray_empty()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         byte[] bytes = new byte[0];
         OctetString octets = (OctetString) helper.createOctetString(bytes);
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createOctetString(byte[])}.
      */
     @Test
     public void testCreateOctetStringByteArray_null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         byte[] bytes = null;
         OctetString octets = (OctetString) helper.createOctetString(bytes);
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createString(java.lang.String)}.
      */
     @Test
     public void testCreateString_normal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OctetString octets = (OctetString) helper.createString("abc");
         assertEquals("abc", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createString(java.lang.String)}.
      */
     @Test
     public void testCreateString_empty()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OctetString octets = (OctetString) helper.createString("");
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createString(java.lang.String)}.
      */
     @Test
     public void testCreateString_null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OctetString octets = (OctetString) helper.createString((String)null);
         assertEquals("", octets.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createInteger(int)}.
      */
     @Test
     public void testCreateInteger_zero()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         Integer32 integer = (Integer32) helper.createInteger(0);
         assertEquals(0, integer.toInt());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createInteger(int)}.
      */
     @Test
     public void testCreateInteger_plus1()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         Integer32 integer = (Integer32) helper.createInteger(1);
         assertEquals(1, integer.toInt());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createInteger(int)}.
      */
     @Test
     public void testCreateInteger_minus1()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         Integer32 integer = (Integer32) helper.createInteger(-1);
         assertEquals(-1, integer.toInt());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createInteger(int)}.
      */
     @Test
     public void testCreateInteger_intmin()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         Integer32 integer = (Integer32) helper.createInteger(Integer.MIN_VALUE);
         assertEquals(Integer.MIN_VALUE, integer.toInt());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createInteger(int)}.
      */
     @Test
     public void testCreateInteger_intmax()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         Integer32 integer = (Integer32) helper.createInteger(Integer.MAX_VALUE);
         assertEquals(Integer.MAX_VALUE, integer.toInt());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_zero()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(0);
         System.out.println("testCreateTimeticks_zero: " + ticks.toString());
         assertEquals(new TimeTicks(0), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_plus1()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(1L);
         System.out.println("testCreateTimeticks_plus1: " + ticks.toString());
         assertEquals(new TimeTicks(0), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_plus9()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(9L);
         System.out.println("testCreateTimeticks_plus9: " + ticks.toString());
         assertEquals(new TimeTicks(0), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_plus10()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(10L);
         System.out.println("testCreateTimeticks_plus10: " + ticks.toString());
         assertEquals(new TimeTicks(1), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_plus100()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(100L);
         System.out.println("testCreateTimeticks_plus100: " + ticks.toString());
         assertEquals(new TimeTicks(10), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_minus1()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(-1L);
         System.out.println("testCreateTimeticks_minus1: " + ticks.toString());
         assertEquals(new TimeTicks(0), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_minus9()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(-9L);
         System.out.println("testCreateTimeticks_minus9: " + ticks.toString());
         assertEquals(new TimeTicks(0), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_minus10()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(-10L);
         System.out.println("testCreateTimeticks_minus10: " + ticks.toString());
         assertEquals(new TimeTicks(Integer.MAX_VALUE), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateTimeticks_minus100()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createTimeticks(-100L);
         System.out.println("testCreateTimeticks_minus100: " + ticks.toString());
         assertEquals(new TimeTicks(Integer.MAX_VALUE - 9), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateAsnObject_Timeticks0()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createAsnObject("1970/01/01 09:00:00.000", SnmpVariableHelper.TIMETICKS);
         assertEquals(new TimeTicks(0), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createTimeticks(long)}.
      */
     @Test
     public void testCreateAsnObject_Timeticks1()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         TimeTicks ticks = (TimeTicks) helper.createAsnObject("1970/01/01 09:00:00.010", SnmpVariableHelper.TIMETICKS);
         assertEquals(new TimeTicks(1), ticks);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createIpAddress(java.net.InetAddress)}.
     * @throws UnknownHostException
      */
     @Test
     public void testCreateIpAddress_normalmin() throws UnknownHostException
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         IpAddress addr = (IpAddress) helper.createIpAddress(InetAddress.getByName("0.0.0.0"));
         assertEquals("0.0.0.0", addr.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createIpAddress(java.net.InetAddress)}.
     * @throws UnknownHostException
      */
     @Test
     public void testCreateIpAddress_normalmax() throws UnknownHostException
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         IpAddress addr = (IpAddress) helper.createIpAddress(InetAddress.getByName("255.255.255.255"));
         assertEquals("255.255.255.255", addr.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createIpAddress(java.net.InetAddress)}.
     * @throws UnknownHostException
      */
     @Test
     public void testCreateIpAddress_localhost() throws UnknownHostException
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         IpAddress addr = (IpAddress) helper.createIpAddress(InetAddress.getByName("127.0.0.1"));
         assertEquals("127.0.0.1", addr.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createIpAddress(java.net.InetAddress)}.
      */
     @Test
     public void testCreateIpAddress_illegal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         try
         {
             IpAddress addr = (IpAddress) helper.createIpAddress(InetAddress.getByName("a"));
             fail("unexpectly succeeded. addr=" + addr.toString());
         }
         catch (UnknownHostException ex)
         {
         }
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createIpAddress(java.net.InetAddress)}.
      */
     @Test
     public void testCreateIpAddress_null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         try
         {
             IpAddress addr = (IpAddress) helper.createIpAddress(null);
             fail("unexpectly succeeded. addr=" + addr.toString());
         }
         catch (NullPointerException ex)
         {
         }
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createObjectId(java.lang.String)}.
      */
     @Test
     public void testCreateObjectId_normal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OID oid = (OID) helper.createObjectId("1.3.6");
         assertEquals("1.3.6", oid.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createObjectId(java.lang.String)}.
      */
     @Test
     public void testCreateObjectId_normalmin()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OID oid = (OID) helper.createObjectId("1");
         assertEquals("1", oid.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createObjectId(java.lang.String)}.
      */
     @Test
     public void testCreateObjectId_empty()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         OID oid = (OID) helper.createObjectId("");
         assertEquals("", oid.toString());
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#createObjectId(java.lang.String)}.
      */
     @Test
     public void testCreateObjectId_null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         try
         {
             OID oid = (OID) helper.createObjectId(null);
             fail("unexpectly succeeded. oid=" + oid.toString());
         }
         catch (NullPointerException ex)
         {
         }
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#convertToString(java.lang.String, java.lang.Object)}.
      */
     @Test
     public void testConvertToString_HexString_NormalAscii()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         String str = helper.convertToString(SnmpVariableHelper.HEX_STRING, new OctetString("abc"));
         assertEquals("61:62:63", str);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#convertToString(java.lang.String, java.lang.Object)}.
      */
     @Test
     public void testConvertToString_HexString_NormalLocal()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         String str = helper.convertToString(SnmpVariableHelper.HEX_STRING, new OctetString(""));
        assertEquals("3f:3f", str);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#convertToString(java.lang.String, java.lang.Object)}.
      */
     @Test
     public void testConvertToString_HexString_Empty()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         String str = helper.convertToString(SnmpVariableHelper.HEX_STRING, new OctetString(""));
         assertEquals("", str);
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#convertToString(java.lang.String, java.lang.Object)}.
      */
     @Test
     public void testConvertToString_HexString_Null()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         try
         {
             helper.convertToString(SnmpVariableHelper.HEX_STRING, null);
             fail("expected thrown NullPointerException.");
         }
         catch (NullPointerException ex)
         {
         }
     }
 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#convertToString(java.lang.String, java.lang.Object)}.
      */
     @Test
     public void testConvertToString_HexString_IllegalType()
     {
         SnmpVariableHelper helper = new Snmp4jVariableHelper();
         String str = helper.convertToString(SnmpVariableHelper.HEX_STRING, new Integer32(100));
         assertEquals("31:30:30", str);
     }

 
     /**
      * Test method for {@link jp.co.acroquest.tool.snmp.toolkit.helper.Snmp4jVariableHelper#convertToString(java.lang.String, java.lang.Object)}.
      */
     @Test
     public void testConvertToString_TimeTicks_normal()
     {
        //fail("not implemented.");
     }
 }
