 /*-
  * Copyright 2012 Benedikt Meurer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.benediktmeurer.eui4j;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotEquals;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 /**
  * Unit tests for the {@link EUI48} class.
  * 
  * @author Benedikt Meurer
  * @see EUI48
  */
 public class EUI48Test {
     @DataProvider(name = "bitPairs")
     public Object[][] dataProviderBitPairs() {
         return new Object[][] {
                 { 0, (short) 0 },
                 { -1, (short) -1 },
                { 0x11223344, (short) 0x5566 }
         };
     }
 
     @DataProvider(name = "bitPairsAndEUI48Names")
     public Object[][] dataProviderBitPairsAndEUI48Names() {
         return new Object[][] {
                 { 0, (short) 0, "00:00:00:00:00:00" },
                 { -1, (short) -1, "ff:ff:ff:ff:ff:ff" },
                 { 0x0f0f0f0f, (short) 0x0f0f, "0f:0f:0f:0f:0f:0f" },
                { 0x11223344, (short) 0x5566, "11:22:33:44:55:66" }
         };
     }
 
     @DataProvider(name = "bitPairsAndOctets")
     public Object[][] dataProviderBitPairsAndOctets() {
         return new Object[][] {
                 { 0, (short) 0, new byte[] { 0, 0, 0, 0, 0, 0 } },
                 { -1, (short) -1, new byte[] { -1, -1, -1, -1, -1, -1 } },
                 { 0x55443322, (short) 0x1100, new byte[] { 0x55, 0x44, 0x33, 0x22, 0x11, 0x00 } }
         };
     }
 
     @DataProvider(name = "bitPairsAndAlternateNames")
     public Object[][] dataProviderBitsAndAlternateNames() {
         return new Object[][] {
                 { 0x987654, (short) 0x3210, "00-98-76-54-32-10" },
                { -1, (short) -1, "ff-FF-ff-FF-ff-FF" }
         };
     }
 
     @DataProvider(name = "invalidEUI48Names")
     public String[][] dataProviderInvalidEUI48Names() {
         return new String[][] {
                 { "invalid" },
                 { "111:22:55:66:77:88" },
                 { "11-22:55-66:77-88" },
                 { "ik:01:67:89:10:11" },
                 { "11:22:33:44:55:66:77:88" },
                 { "ff:ff:ff" }
         };
     }
 
     @Test(dataProvider = "bitPairsAndOctets")
     public void testGetOctets(int mostSignificantBits, short leastSignificantBits, byte[] octets) {
         assertEquals(new EUI48(mostSignificantBits, leastSignificantBits).getOctets(), octets);
     }
 
     @Test(dataProvider = "bitPairsAndOctets")
     public void testConstructorWithOctets(int mostSignificantBits, short leastSignificantBits, byte[] octets) {
         EUI48 val = new EUI48(octets);
         assertEquals(val.getMostSignificantBits(), mostSignificantBits);
         assertEquals(val.getLeastSignificantBits(), leastSignificantBits);
     }
 
     @Test(expectedExceptions = NullPointerException.class)
     public void testConstructorWithNullOctets() {
         new EUI48(null);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testConstructorWithEmptyOctets() {
         new EUI48(new byte[0]);
     }
 
     @Test(dataProvider = "bitPairs")
     public void testCompareTo(int mostSignificantBits, short leastSignificantBits) {
         assertEquals(new EUI48(mostSignificantBits, leastSignificantBits).compareTo(new EUI48(mostSignificantBits, leastSignificantBits)), 0);
     }
 
     @Test(dataProvider = "bitPairs")
     public void testCompareToInverted(int mostSignificantBits, short leastSignificantBits) {
         assertNotEquals(new EUI48(mostSignificantBits, leastSignificantBits).compareTo(new EUI48(~mostSignificantBits, (short) ~leastSignificantBits)), 0);
     }
 
     @Test(dataProvider = "bitPairs")
     public void testHashCode(int mostSignificantBits, short leastSignificantBits) {
         assertEquals(new EUI48(mostSignificantBits, leastSignificantBits).hashCode(), new EUI48(mostSignificantBits, leastSignificantBits).hashCode());
     }
 
     @Test(dataProvider = "bitPairs")
     public void testEquals(int mostSignificantBits, short leastSignificantBits) {
         assertEquals(new EUI48(mostSignificantBits, leastSignificantBits), new EUI48(mostSignificantBits, leastSignificantBits));
     }
 
     @Test(dataProvider = "bitPairs")
     public void testEqualsInverted(int mostSignificantBits, short leastSignificantBits) {
         assertNotEquals(new EUI48(mostSignificantBits, leastSignificantBits), new EUI48(~mostSignificantBits, (short) ~leastSignificantBits));
     }
 
     @Test(dataProvider = "bitPairsAndEUI48Names")
     public void testToString(int mostSignificantBits, short leastSignificantBits, String name) {
         assertEquals(new EUI48(mostSignificantBits, leastSignificantBits).toString(), name);
     }
 
     @Test(dataProvider = "bitPairsAndEUI48Names")
     public void testFromString(int mostSignificantBits, short leastSignificantBits, String name) {
         assertEquals(EUI48.fromString(name), new EUI48(mostSignificantBits, leastSignificantBits));
     }
 
     @Test(dataProvider = "bitPairsAndAlternateNames")
     public void testFromStringAlternate(int mostSignificantBits, short leastSignificantBits, String name) {
         assertEquals(EUI48.fromString(name), new EUI48(mostSignificantBits, leastSignificantBits));
     }
 
     @Test(dataProvider = "bitPairs")
     public void testFromStringToString(int mostSignificantBits, short leastSignificantBits) {
         EUI48 val = new EUI48(mostSignificantBits, leastSignificantBits);
         assertEquals(EUI48.fromString(val.toString()), val);
     }
 
     @Test(expectedExceptions = NullPointerException.class)
     public void testFromStringWithNull() {
         EUI48.fromString(null);
     }
 
     @Test(dataProvider = "invalidEUI48Names", expectedExceptions = IllegalArgumentException.class)
     public void testFromStringWithInvalidName(String name) {
         EUI48.fromString(name);
     }
 }
