 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011 CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.ip;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
import com.craftfire.commons.ip.IPAddress;
import com.craftfire.commons.ip.IPv4Address;
import com.craftfire.commons.ip.IPv6Address;

 public class TestIPAddress {
     private static IPv4Address ipv4 = new IPv4Address(192, 168, 1, 1);
     private static IPv6Address ipv6 = new IPv6Address(0xfe80, 0, 0, 0, 0, 0x1015, 0xc0a8, 1);
 
     @Test
     public void testValueOfStringV4() {
         IPAddress addr = IPAddress.valueOf("192.168.1.1");
         System.out.println("testValueOfStringV4: " + addr);
         assertTrue(addr.isIPv4());
         assertTrue(addr.equals(ipv4));
     }
 
     @Test
     public void testValueOfStringV6() {
         IPAddress addr = IPAddress.valueOf("fe80::1015:c0a8:1");
         System.out.println("testValueOfStringV6: " + addr);
         assertTrue(addr.isIPv6());
         assertTrue(addr.equals(ipv6));
     }
 
     @Test
     public void testValueOfStringLocalhost() {
         IPAddress addr = IPAddress.valueOf("localhost");
         System.out.println("testValueOfStringLocalhost: " + addr);
         assertTrue(addr.getInetAddress().isLoopbackAddress());
     }
 
     @Test
     public void testValueOfStringHostname() {
        IPAddress addr = IPAddress.valueOf("craftfire.com");
         System.out.println("testValueOfStringHostname: " + addr);
        assertTrue(addr.equals(new IPv4Address(204, 232, 175, 78)));
     }
 
     @Test
     public void testValueOfStringInvalid() {
         IPAddress addr = IPAddress.valueOf("AliceHasACat;AndTheCatHas~~TheAlice~&**(:-)");
         System.out.println("testValueOfStringInvalid: " + addr);
         assertNull(addr);
     }
 
     @Test
     public void testValueOfInetAddressV4() {
         IPAddress addr = IPAddress.valueOf(ipv4.getInetAddress());
         System.out.println("testValueOfInetAddressV4: " + addr);
         assertTrue(addr.isIPv4());
         assertTrue(addr.equals(ipv4));
     }
 
     @Test
     public void testValueOfInetAddressV6() {
         IPAddress addr = IPAddress.valueOf(ipv6.getInetAddress());
         System.out.println("testValueOfInetAddressV6: " + addr);
         assertTrue(addr.isIPv6());
         assertTrue(addr.equals(ipv6));
     }
 
     @Test
     public void testValueOfInetAddressNull() {
         IPAddress addr = IPAddress.valueOf((InetAddress) null);
         assertNull(addr);
     }
 
     @Test
     public void testIPv4() {
         System.out.println("testIPv4: " + ipv4);
         assertTrue(ipv4.isIPv4());
         assertFalse(ipv4.isIPv6());
         assertTrue(ipv4.toIPv4() == ipv4);
         assertTrue(Arrays.equals(ipv4.getInetAddress().getAddress(), ipv4.getAddress()));
         assertTrue(Arrays.equals(ipv4.getBytes(), ipv4.getAddress()));
         assertEquals(0xc0a80101, ipv4.getInt());
     }
 
     @Test
     public void testIPv6() {
         System.out.println("testIPv6: " + ipv6);
         assertTrue(ipv6.isIPv6());
         assertFalse(ipv6.isIPv4());
         assertTrue(ipv6.toIPv6() == ipv6);
         assertTrue(Arrays.equals(ipv6.getInetAddress().getAddress(), ipv6.getBytes()));
         assertTrue(Arrays.equals(ipv6.getAddress(), new short[] { (short) 0xfe80, 0, 0, 0, 0, 0x1015, (short) 0xc0a8, 1 }));
     }
 
     @Test
     public void testIPv4InvalidConstructorCall() {
         try {
             IPAddress addr = new IPv4Address(new byte[] { 127, 0, 1 });
             fail("Shoudl be not able to create: " + addr);
         } catch (IllegalArgumentException e) {
         }
         try {
             IPAddress addr = new IPv4Address(192, 168, 1, 255, 8);
             fail("Shoudl be not able to create: " + addr);
         } catch (IllegalArgumentException e) {
         }
     }
 
     @Test
     public void testIPv6InvalidConstructorCall() {
         try {
             IPAddress addr = new IPv6Address(new byte[] { 127, 0, 1, 60, 90 });
             fail("Shoudl be not able to create: " + addr);
         } catch (IllegalArgumentException e) {
         }
         try {
             IPAddress addr = new IPv6Address(new short[] { 127, 0, 1, 60, 90, 1200 });
             fail("Shoudl be not able to create: " + addr);
         } catch (IllegalArgumentException e) {
         }
         try {
             IPAddress addr = new IPv6Address(192, 168, 1, 255, 8, 1200, 91, 88, 1228);
             fail("Shoudl be not able to create: " + addr);
         } catch (IllegalArgumentException e) {
         }
     }
 
     @Test
     public void testAnycastConversion() {
         IPAddress ipv4new = new IPv4Address(0, 0, 0, 0);
         IPv6Address addr = ipv4new.toIPv6();
         System.out.println("testAnycastIPv4to6: " + addr);
         assertTrue(addr.equals(new IPv6Address(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })));
         assertTrue(addr.toIPv4().equals(ipv4new));
     }
 
     @Test
     public void testLoopbackConversion() {
         IPAddress ipv6new = IPAddress.valueOf("0::1");
         IPv4Address addr = ipv6new.toIPv4();
         System.out.println("testLoopbackIPv6to4: " + addr);
         assertTrue(addr.equals(new IPv4Address(127, 0, 0, 1)));
         assertTrue(addr.toIPv6().equals(ipv6new));
     }
 
     @Test
     public void testIPv4to6() {
         IPv6Address addr = ipv4.toIPv6();
         System.out.println("testIPv4to6: " + addr);
         assertTrue(addr.equals(new IPv6Address(0xfe80, 0, 0, 0, 0, 0, 0xc0a8, 0x0101)));
         assertTrue(addr.toIPv4().equals(ipv4));
     }
 
     @Test
     public void testIPv6to4() {
         IPAddress ipv6new = IPAddress.valueOf("2002:0:0:0:0:0:cdfd:c7b6");
         IPv4Address addr = ipv6new.toIPv4();
         System.out.println("testIPv6to4: " + addr);
         assertTrue(addr.equals(new IPv4Address(205, 253, 199, 182)));
         assertTrue(addr.toIPv6().equals(ipv6new));
     }
 
     @Test
     public void testWrongConversion() {
         IPAddress ipv6new = IPAddress.valueOf("2001::1");
         IPv4Address addr = ipv6new.toIPv4();
         assertNull(addr);
     }
 
     @Test
     public void testIPv4HashAndEquals() {
         IPAddress addr = IPAddress.valueOf("192.168.1.1");
         IPv4Address addr1 = new IPv4Address(192, 168, 2, 1);
         assertTrue(ipv4.equals(ipv4));
         assertTrue(ipv4.hashCode() == ipv4.hashCode());
         assertTrue(ipv4.equals(addr));
         assertTrue(addr.equals(ipv4));
         assertTrue(ipv4.hashCode() == addr.hashCode());
         assertFalse(ipv4.equals(addr1));
         assertFalse(addr1.equals(ipv4));
         assertFalse(ipv4.equals(ipv6));
         assertFalse(addr.equals(ipv6));
         assertFalse(ipv6.equals(addr));
         assertFalse(ipv4.equals(ipv4.toIPv6()));
         assertFalse(ipv4.toIPv6().equals(ipv4));
         Map<IPv4Address, String> map = new HashMap<IPv4Address, String>();
         map.put(ipv4, "alice");
         map.put(addr1, "bob");
         assertEquals("alice", map.get(addr));
     }
 
     @Test
     public void testIPv6HashAndEquals() {
         IPAddress addr = IPAddress.valueOf("fe80::1015:c0a8:1");
         IPv6Address addr1 = new IPv6Address(0x2002, 0, 0, 0, 0, 0, 0xcdfd, 0xc7b6);
         assertTrue(ipv6.equals(ipv6));
         assertTrue(ipv6.hashCode() == ipv6.hashCode());
         assertTrue(ipv6.equals(addr));
         assertTrue(addr.equals(ipv6));
         assertTrue(ipv6.hashCode() == addr.hashCode());
         assertFalse(ipv6.equals(addr1));
         assertFalse(addr1.equals(ipv6));
         assertFalse(ipv6.equals(ipv4));
         assertFalse(addr.equals(ipv4));
         assertFalse(ipv4.equals(addr));
         assertFalse(ipv6.equals(ipv6.toIPv4()));
         assertFalse(ipv6.toIPv4().equals(ipv6));
         Map<IPv6Address, String> map = new HashMap<IPv6Address, String>();
         map.put(ipv6, "alice");
         map.put(addr1, "bob");
         assertEquals("alice", map.get(addr));
     }
 
     @Test
     public void testIPHashMaps() {
         IPAddress v4 = IPAddress.valueOf("192.168.1.1");
         IPv4Address v41 = new IPv4Address(192, 168, 2, 1);
         IPAddress v6 = IPAddress.valueOf("fe80::1015:c0a8:1");
         IPv6Address v61 = new IPv6Address(0x2002, 0, 0, 0, 0, 0, 0xcdfd, 0xc7b6);
         Map<IPAddress, String> map = new HashMap<IPAddress, String>();
         map.put(ipv4, "alice");
         map.put(v61, "bob");
         map.put(v41, "charlie");
         map.put(v6, "dan");
         assertEquals("alice", map.get(v4));
         assertEquals("dan", map.get(ipv6));
     }
 }
