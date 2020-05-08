 /*
  * Copyright 2009-2011 Joel Hockey (joel.hockey@gmail.com).  All rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.java.jless.smartcard;
 
 public class SmartcardUtil {
 
     /**
      * Format APDU.
      * @param cla cla
      * @param ins ins
      * @param p1 p1
      * @param p2 p2
      * @param data data
      * @param le le
      * @return formatted APDU
      */
     public static byte[] formatAPDU(int cla, int ins, int p1, int p2, byte[] data, Integer le) {
         int lc = data == null ? 0 : data.length;
        byte[] lcbuf; // holds Lc - 1 or 3 bytes
        byte[] lebuf = null; // holds Le, 0, 1, or 2 bytes
        if (lc <= 255) {
             // single byte Lc and Le
             lcbuf = new byte[] {(byte) lc};
             if (le != null) {
                 lebuf = new byte[] {(byte) le.intValue()};
             }
         } else {
             // extended lengths (3 bytes for Lc, 2 bytes for Le)
             lcbuf = new byte[] {0, (byte) (lc >> 8), (byte) lc};
             if (le != null) {
                 lebuf = new byte[] {(byte) (le.intValue() >> 8), (byte) le.intValue()};
             }
         }
         return Buf.cat(new byte[] {(byte) cla, (byte) ins, (byte) p1, (byte) p2}, lcbuf, data, lebuf);
     }
 
     /**
      * Translate from {@link Smartcard#transmit(byte[])}
      * to {@link Smartcard#transmit(int, int, int, int, byte[], Integer)}.
      * <pre>
      * Command APDU encoding options:
      *
      * case 1:  |CLA|INS|P1 |P2 |                                 len = 4
      * case 2s: |CLA|INS|P1 |P2 |LE |                             len = 5
      * case 3s: |CLA|INS|P1 |P2 |LC |...BODY...|                  len = 6..260
      * case 4s: |CLA|INS|P1 |P2 |LC |...BODY...|LE |              len = 7..261
      * case 2e: |CLA|INS|P1 |P2 |00 |LE1|LE2|                     len = 7
      * case 3e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|          len = 8..65542
      * case 4e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|LE1|LE2|  len =10..65544
      *
      * LE, LE1, LE2 may be 0x00.
      * LC must not be 0x00 and LC1|LC2 must not be 0x00|0x00
      * </pre>
      * @param card card to call {@link Smartcard#transmit(int, int, int, int, byte[], Integer)}
      * @param apdu apdu
      * @return APDURes from invoking {@link Smartcard#transmit(byte[])}
      * @throws SmartcardException if error
      */
     public static APDURes transmit(Smartcard card, byte[] apdu) throws SmartcardException {
         if (apdu == null || apdu.length < 4) {
             throw new SmartcardException("APDU must be at least 4 bytes, got apdu: " + Hex.b2s(apdu));
         }
         int cla = apdu[0] & 0xff;
         int ins = apdu[1] & 0xff;
         int p1 = apdu[2] & 0xff;
         int p2 = apdu[3] & 0xff;
         // case 1:  |CLA|INS|P1 |P2 |                                 len = 4
         if (apdu.length == 4) {
             return card.transmit(cla, ins, p1, p2, null, null);
 
         // case 2s: |CLA|INS|P1 |P2 |LE |                             len = 5
         } else if (apdu.length == 5) {
             return card.transmit(cla, ins, p1, p2, null, apdu[4] & 0xff);
         }
 
         Integer le = null;
         int lc = apdu[4] & 0xff;
         if (lc > 0) {
             // case 3s: |CLA|INS|P1 |P2 |LC |...BODY...|                  len = 6..260
             if (apdu.length == 5 + lc) {
                 le = null;
 
             // case 4s: |CLA|INS|P1 |P2 |LC |...BODY...|LE |              len = 7..261
             } else if (apdu.length == 6 + lc) {
                 le = Integer.valueOf(apdu[apdu.length - 1] & 0xff);
 
             // error
             } else {
                 throw new  SmartcardException(String.format(
                     "Invalid APDU with single byte lc, lc=%d, expected apdu.length of lc + (5 or 6), got %d, apdu: %s",
                     lc, apdu.length, Hex.b2s(apdu)));
             }
             byte[] data = new byte[lc];
             System.arraycopy(apdu, 5, data, 0, data.length);
             return card.transmit(cla, ins, p1, p2, data, le);
         }
 
         // case 2e: |CLA|INS|P1 |P2 |00 |LE1|LE2|                     len = 7
         lc = (apdu[5] & 0xff) << 8 | (apdu[6] & 0xff);
         if (apdu.length == 7) {
             return card.transmit(cla, ins, p1, p2, null, lc);
 
         // case 3e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|          len = 8..65542
         } else if (apdu.length == lc + 8) {
             le = null;
 
         // case 4e: |CLA|INS|P1 |P2 |00 |LC1|LC2|...BODY...|LE1|LE2|  len =10..65544
         } else if (apdu.length == lc + 10) {
             le = (apdu[apdu.length - 2] & 0xff) << 8 | (apdu[apdu.length - 1] & 0xff);
 
         // error
         } else {
             throw new SmartcardException(String.format(
                 "Invalid APDU with double byte lc, lc=%d, expected apdu.length of lc + (8 or 10), got %d, apdu: %s",
                 lc, apdu.length, Hex.b2s(apdu)));
         }
         byte[] data = new byte[lc];
         java.lang.System.arraycopy(apdu, 7, data, 0, data.length);
         return card.transmit(cla, ins, p1, p2, data, le);
     }
 }
