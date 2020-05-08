 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
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
 package com.craftfire.commons.encryption;
 
 public class EncryptionUtil {
     public static String pack(String hex) {
        StringBuilder buf = new StringBuilder();
         for (int i = 0; i < hex.length(); i += 2) {
             char c1 = hex.charAt(i);
             char c2 = hex.charAt(i + 1);
             char packed = (char) (hexToInt(c1) * 16 + hexToInt(c2));
             buf.append(packed);
         }
         return buf.toString();
     }
 
     public static String bytesTohex(byte[] data)  {
        StringBuilder buf = new StringBuilder();
        for (byte aByte : data) {
            int halfbyte = (aByte >>> 4) & 0x0F;
             int two_halfs = 0;
             do {
                 if ((0 <= halfbyte) && (halfbyte <= 9)) {
                     buf.append((char) ('0' + halfbyte));
                 } else {
                     buf.append((char) ('a' + (halfbyte - 10)));
                 }
                halfbyte = aByte & 0x0F;
             }
            while (two_halfs++ < 1);
         }
         return buf.toString();
     }
 
     public static int hexToInt(char ch) {
         if (ch >= '0' && ch <= '9') {
             return ch - '0';
         }
         ch = Character.toUpperCase(ch);
         if (ch >= 'A' && ch <= 'F') {
             return ch - 'A' + 0xA;
         }
         throw new IllegalArgumentException("Not a hex character: " + ch);
     }
 
     public static String hexToString(String str) {
         char[] chars = str.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(Integer.toHexString((int) aChar));
         }
         return hex.toString();
     }
 }
