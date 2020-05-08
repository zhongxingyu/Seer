 /*******************************************************************************
  * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.RoundingMode;
 
 public class TCFNumberFormat {
 
     public static String isValidHexNumber(String s) {
         int l = s.length();
         if (l == 0) return "Need at least one digit";
         for (int i = 0; i < l; i++) {
             char ch = s.charAt(i);
             if (ch >= '0' && ch <= '9') continue;
             if (ch >= 'A' && ch <= 'F') continue;
             if (ch >= 'a' && ch <= 'f') continue;
             return "Hex digit expected";
         }
         return null;
     }
 
     public static String isValidDecNumber(boolean fp, String s) {
         int i = 0;
         int l = s.length();
         if (l == 0) return "Need at least one digit";
         char ch = s.charAt(i++);
         if (ch == '-' || ch == '+') {
             if (i >= l) return "Need at least one digit";
             ch = s.charAt(i++);
         }
         if (fp) {
             String n = s.substring(i - 1);
             if (n.equals("NaN")) return null;
             if (n.equals("Infinity")) return null;
         }
         while (ch >= '0' && ch <= '9') {
             if (i >= l) return null;
             ch = s.charAt(i++);
         }
         if (fp) {
             if (ch == '.') {
                 if (i >= l) return null;
                 ch = s.charAt(i++);
                 while (ch >= '0' && ch <= '9') {
                     if (i >= l) return null;
                     ch = s.charAt(i++);
                 }
             }
             if (ch == 'e' || ch == 'E') {
                 if (i >= l) return "Invalid exponent: need at least one digit";
                 ch = s.charAt(i++);
                 if (ch == '-' || ch == '+') {
                     if (i >= l) return "Invalid exponent: need at least one digit";
                     ch = s.charAt(i++);
                 }
                 while (ch >= '0' && ch <= '9') {
                     if (i >= l) return null;
                     ch = s.charAt(i++);
                 }
                 return "Invalid exponent: decimal digit expected";
             }
         }
         return "Decimal digit expected";
     }
 
     public static byte[] toByteArray(String s, int radix, boolean fp, int size, boolean signed, boolean big_endian) throws Exception {
         byte[] bf = null;
         if (!fp) {
             bf = new BigInteger(s, radix).toByteArray();
         }
         else if (size == 4) {
             int n = Float.floatToIntBits(Float.parseFloat(s));
             bf = new byte[size];
             for (int i = 0; i < size; i++) {
                 bf[i] = (byte)((n >> ((size - 1 - i) * 8)) & 0xff);
             }
         }
         else if (size == 8) {
             long n = Double.doubleToLongBits(Double.parseDouble(s));
             bf = new byte[size];
             for (int i = 0; i < size; i++) {
                 bf[i] = (byte)((n >> ((size - 1 - i) * 8)) & 0xff);
             }
         }
         else if (size == 2 || size == 10 || size == 16) {
             BigDecimal d = new BigDecimal(s);
             int n = 0;
             int bin_scale = 0;
             for (n = 0; n < 1000; n++) {
                 d = d.stripTrailingZeros();
                 int scale = d.scale();
                 if (scale > 0) {
                     int x = d.precision();
                     if (x > 36) {
                         x -= 36;
                         if (x > scale) x = scale;
                         d = d.setScale(scale - x, RoundingMode.HALF_DOWN);
                         continue;
                     }
                 }
                 if (scale < 0) {
                     d = d.divide(BigDecimal.valueOf(2).pow(-scale));
                     bin_scale += scale;
                 }
                 else if (scale > 0) {
                     d = d.multiply(BigDecimal.valueOf(2).pow(scale));
                     bin_scale += scale;
                 }
                 else {
                     break;
                 }
             }
             BigInteger man = d.unscaledValue();
             int cmp = man.compareTo(BigInteger.ZERO);
             bf = new byte[size];
             if (cmp != 0) {
                 boolean sign = cmp < 0;
                 if (sign) man = man.negate();
                 int man_bits = man.bitLength();
                 int man_offs = 0;
                 int exp = 0;
                 for (;;) {
                     if (size == 2) {
                         exp = man_bits - bin_scale + 14;
                         if (exp <= 0) {
                             man_bits += 1 - exp;
                             exp = 0;
                         }
                         if (exp > 0x1f) exp = 0x1f;
                         man_offs = 5;
                     }
                     else {
                         exp = man_bits - bin_scale + 16382;
                         if (exp <= 0) {
                             man_bits += 1 - exp;
                             exp = 0;
                         }
                         else if (size == 10) {
                             man_bits++;
                         }
                         if (exp > 0x7fff) exp = 0x7fff;
                         man_offs = 15;
                     }
                     // Rounding
                     int rb = man_offs + man_bits - size * 8 - 1;
                     if (rb >= 0 && man.testBit(rb)) {
                         man = man.add(BigInteger.ONE.shiftLeft(rb));
                         man_bits = man.bitLength();
                     }
                     else {
                         break;
                     }
                 }
                 if (sign) bf[0] |= 0x80;
                 for (int i = 1; i <= man_offs; i++) {
                     if (((1 << (man_offs - i)) & exp) != 0) {
                         bf[i / 8] |= (1 << (7 - i % 8));
                     }
                 }
                 for (int i = 0; i < man_bits; i++) {
                     int j = man_offs + i; // bit pos in bf
                     int k = man_bits - i - 1; // bit pos in man
                     if (j / 8 >= bf.length) break;
                     if (i == 0) {
                         assert man.testBit(k) == (exp > 0 && size != 10);
                     }
                     else if (man.testBit(k)) {
                         bf[j / 8] |= (1 << (7 - j % 8));
                     }
                 }
             }
         }
         else {
             throw new Exception("Unsupported floating point format");
         }
         byte[] rs = new byte[size];
         if (signed && rs.length > bf.length && (bf[0] & 0x80) != 0) {
             // Sign extension
             for (int i = 0; i < rs.length; i++) rs[i] = (byte)0xff;
         }
         for (int i = 0; i < bf.length; i++) {
             // i == 0 -> least significant byte
             byte b = bf[bf.length - i - 1];
             int j = big_endian ? rs.length - i - 1 : i;
             if (j >= 0 && j < rs.length) rs[j] = b;
         }
         return rs;
     }
 
     public static String toFPString(byte[] data, boolean big_endian) {
         return toFPString(data, 0, data.length, big_endian);
     }
 
     public static String toFPString(byte[] data, int offs, int size, boolean big_endian) {
         assert offs + size <= data.length;
         byte[] arr = new byte[size];
         if (big_endian) {
             System.arraycopy(data, offs, arr, 0, size);
         }
         else {
             for (int i = 0; i < size; i++) {
                 arr[arr.length - i - 1] = data[offs + i];
             }
         }
 
         boolean neg = (arr[0] & 0x80) != 0;
         arr[0] &= 0x7f;
 
         int precision = 0;
         int exponent = 0;
         boolean nan = false;
         switch (size) {
         case 2:
             precision = 3;
             exponent = (arr[0] & 0x7c) >> 2;
             nan = exponent == 0x1f;
             arr[0] &= 0x03;
             if (exponent == 0) exponent = 1;
            else if (!nan) arr[0] |= 0x04;
             exponent -= 10; // Significand
             exponent -= 15; // Exponent bias
             break;
         case 4:
             precision = 7;
             exponent = ((arr[0] & 0x7f) << 1) | ((arr[1] & 0x80) >> 7);
             nan = exponent == 0xff;
             arr[0] = 0;
             arr[1] &= 0x7f;
             if (exponent == 0) exponent = 1;
            else if (!nan) arr[1] |= 0x80;
             exponent -= 23; // Significand
             exponent -= 127; // Exponent bias
             break;
         case 8:
             precision = 16;
             exponent = ((arr[0] & 0x7f) << 4) | ((arr[1] & 0xf0) >> 4);
             nan = exponent == 0x7ff;
             arr[0] = 0;
             arr[1] &= 0x0f;
             if (exponent == 0) exponent = 1;
            else if (!nan) arr[1] |= 0x10;
             exponent -= 52; // Significand
             exponent -= 1023; // Exponent bias
             break;
         case 10:
         case 16:
             precision = 34;
             exponent = ((arr[0] & 0x7f) << 8) | (arr[1] & 0xff);
             nan = exponent == 0x7fff;
             arr[0] = arr[1] = 0;
             if (size == 10) {
                 exponent -= 63; // Significand
                if (nan) arr[2] &= 0x7f;
             }
             else {
                 if (exponent == 0) exponent = 1;
                else if (!nan) arr[1] = 1;
                 exponent -= 112; // Significand
             }
             exponent -= 16383; // Exponent bias
             break;
         default:
             return null;
         }
         if (nan) {
             for (int i = 0; i < arr.length; i++) {
                if (arr[i] != 0) return neg ? "-NaN" : "+NaN";
             }
             return neg ? "-Infinity" : "+Infinity";
         }
         BigDecimal a = new BigDecimal(new BigInteger(arr), 0);
         if (a.signum() != 0 && exponent != 0) {
             BigDecimal p = new BigDecimal(BigInteger.valueOf(2), 0);
             if (exponent > 0) {
                 a = a.multiply(p.pow(exponent));
             }
             else {
                 BigDecimal b = p.pow(-exponent);
                 a = a.divide(b, b.precision(), RoundingMode.HALF_DOWN);
             }
             if (precision != 0 && a.precision() > precision) {
                 int scale = a.scale() - a.precision() + precision;
                 a = a.setScale(scale, RoundingMode.HALF_DOWN);
             }
         }
         String s = a.toString();
         if (neg) s = "-" + s;
         return s;
     }
 
     public static BigInteger toBigInteger(byte[] data, boolean big_endian, boolean sign_extension) {
         return toBigInteger(data, 0, data.length, big_endian, sign_extension);
     }
 
     public static BigInteger toBigInteger(byte[] data, int offs, int size, boolean big_endian, boolean sign_extension) {
         assert offs + size <= data.length;
         byte[] temp = null;
         if (sign_extension) {
             temp = new byte[size];
         }
         else {
             temp = new byte[size + 1];
             temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
         }
         if (big_endian) {
             System.arraycopy(data, offs, temp, sign_extension ? 0 : 1, size);
         }
         else {
             for (int i = 0; i < size; i++) {
                 temp[temp.length - i - 1] = data[i + offs];
             }
         }
         return new BigInteger(temp);
     }
 }
