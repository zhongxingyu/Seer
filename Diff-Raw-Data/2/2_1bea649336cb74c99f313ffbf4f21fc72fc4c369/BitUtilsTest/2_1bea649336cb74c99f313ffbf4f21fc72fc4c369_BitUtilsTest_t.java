 /*
  * The MIT License
  *
  * Copyright 2013 Georgios Migdos <cyberpython@gmail.com>.
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
 package com.gmigdos.bitutils;
 
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author Georgios Migdos <cyberpython@gmail.com>
  */
 public class BitUtilsTest {
     
     /**
      * Test of toLong method, of class BitUtils.
      */
     @Test
     public void testToLong(){
         byte[] bytes = {(byte)0xFF};
         assertEquals(0xFFL, BitUtils.toLong(bytes));
         byte[] bytes2 = {(byte)0xFF, (byte)0x63};
         assertEquals(0xFF63L, BitUtils.toLong(bytes2));
         byte[] bytes3 = {(byte)0xFF, (byte)0x63, (byte)0x00};
         assertEquals(0xFF6300L, BitUtils.toLong(bytes3));
         byte[] bytes4 = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32};
         assertEquals(0xFF630032L, BitUtils.toLong(bytes4));
         byte[] bytes5 = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52};
         assertEquals(0xFF63003252L, BitUtils.toLong(bytes5));
         byte[] bytes6 = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3};
         assertEquals(0xFF63003252A3L, BitUtils.toLong(bytes6));
         byte[] bytes7 = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3, (byte)0x6D};
         assertEquals(0xFF63003252A36DL, BitUtils.toLong(bytes7));
         byte[] bytes8 = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3, (byte)0x6D, (byte)0x4C};
         assertEquals(0xFF63003252A36D4CL, BitUtils.toLong(bytes8));
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testToLongZeroLengthArray(){
         byte[] b = {};
         BitUtils.toLong(b);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testToLongLargeArray(){
         byte[] b = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3, (byte)0x6D, (byte)0x4C, (byte)0x1B};
         BitUtils.toLong(b);
     }
     
     /**
      * Test of toInt method, of class BitUtils.
      */
     @Test
     public void testToInt(){
         byte[] bytes = {(byte)0xFF};
         assertEquals(0xFF, BitUtils.toInt(bytes));
         byte[] bytes2 = {(byte)0xFF, (byte)0x63};
         assertEquals(0xFF63, BitUtils.toInt(bytes2));
         byte[] bytes3 = {(byte)0xFF, (byte)0x63, (byte)0x00};
         assertEquals(0xFF6300, BitUtils.toInt(bytes3));
         byte[] bytes4 = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32};
         assertEquals(0xFF630032, BitUtils.toInt(bytes4));
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testToIntZeroLengthArray(){
         byte[] b = {};
         BitUtils.toInt(b);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testToIntLargeArray(){
         byte[] b = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3};
         BitUtils.toInt(b);
     }
     
     /**
      * Test of toShort method, of class BitUtils.
      */
     @Test
     public void testToShort(){
         byte[] bytes = {(byte)0xFF};
         assertEquals(0xFF, BitUtils.toShort(bytes));
         byte[] bytes2 = {(byte)0xFF, (byte)0x63};
        assertEquals((short)0xFF63, BitUtils.toShort(bytes2));
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testToShortZeroLengthArray(){
         byte[] b = {};
         BitUtils.toShort(b);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testToShortLargeArray(){
         byte[] b = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3};
         BitUtils.toShort(b);
     }
     
     /**
      * Test of fromLong method, of class BitUtils.
      */
     @Test()
     public void testFromLong(){
         byte[] b = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32, (byte)0x52, (byte)0xA3, (byte)0x6D, (byte)0x4C};
         assertArrayEquals(b, BitUtils.fromLong(0xFF63003252A36D4CL));
     }
     
     /**
      * Test of fromInt method, of class BitUtils.
      */
     @Test()
     public void testFromInt(){
         byte[] b = {(byte)0xFF, (byte)0x63, (byte)0x00, (byte)0x32};
         assertArrayEquals(b, BitUtils.fromInt(0xFF630032));
     }
     
     /**
      * Test of fromShort method, of class BitUtils.
      */
     @Test()
     public void testFromShort(){
         byte[] b = {(byte)0xFF, (byte)0x63};
         assertArrayEquals(b, BitUtils.fromShort((short)0xFF63));
     }
     
     /**
      * Test of bytesToBinaryString method, of class BitUtils.
      */
     @Test
     public void testBytesToBinaryString() {
         byte[] bytes = {(byte)0xFF, (byte)0x63, (byte)0x00};
         String expResult = "11111111 01100011 00000000";
         String result = BitUtils.bytesToBinaryString(bytes);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of bytesToHexString method, of class BitUtils.
      */
     @Test
     public void testBytesToHexString() {
         byte[] bytes = {(byte)0xFF, (byte)0x63, (byte)0x00};
         String expResult = "FF 63 00";
         String result = BitUtils.bytesToHexString(bytes);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of setBit method, of class BitUtils.
      */
     @Test
     public void testSetBit() {
         byte[] b = new byte[1];
         byte[] expResult = {(byte)0x01};
         byte[] result = BitUtils.setBit(b, 0);
         assertArrayEquals(expResult, result);
         byte[] expResult2 = {(byte)0x03};
         result = BitUtils.setBit(b, 1);
         assertArrayEquals(expResult2, result);
         b = new byte[3];
         byte[] expResult3 = {(byte)0x00, (byte)0x01, (byte)0x00};
         result = BitUtils.setBit(b, 8);
         assertArrayEquals(expResult3, result);
         byte[] expResult4 = {(byte)0x00, (byte)0x81, (byte)0x00};
         result = BitUtils.setBit(b, 15);
         assertArrayEquals(expResult4, result);
     }
     
     @Test(expected=IndexOutOfBoundsException.class)
     public void testSetBitInvalidIndex() {
         byte[] b = new byte[1];
         BitUtils.setBit(b, 8);
     }
     
     @Test(expected=IndexOutOfBoundsException.class)
     public void testSetBitNegativeIndex() {
         byte[] b = new byte[1];
         BitUtils.setBit(b, -1);
     }
 
     /**
      * Test of unsetBit method, of class BitUtils.
      */
     @Test
     public void testUnsetBit() {
         byte[] b = {(byte)0x82, (byte)0xFF, (byte)0xA1};
         byte[] expResult = {(byte)0x82, (byte)0xDF, (byte)0xA1};
         byte[] result = BitUtils.unsetBit(b, 13);
         assertArrayEquals(expResult, result);
     }
     
     @Test(expected=IndexOutOfBoundsException.class)
     public void testUnsetBitInvalidIndex() {
         byte[] b = new byte[1];
         BitUtils.unsetBit(b, 8);
     }
     
     @Test(expected=IndexOutOfBoundsException.class)
     public void testUnsetBitNegativeIndex() {
         byte[] b = new byte[1];
         BitUtils.unsetBit(b, -1);
     }
 
     /**
      * Test of extract method, of class BitUtils.
      */
     @Test
     public void testExtract() {
         byte[] b = {(byte)0x31, (byte)0x6C, (byte)0x38};
         byte[] expResult = {(byte)0x31, (byte)0x6C};
         byte[] result = BitUtils.extract(b, 8, 16);
         assertArrayEquals(expResult, result);
         
         byte[] expResult2 = {(byte)0x11, (byte)0x6C};
         byte[] result2 = BitUtils.extract(b, 8, 13);
         assertArrayEquals(expResult2, result2);
         
         byte[] result3 = BitUtils.extract(b, 8, 15);
         assertArrayEquals(expResult, result3);
         
         byte[] expResult4 = {(byte)0x03, (byte)0x0E};
         byte[] result4 = BitUtils.extract(b, 2, 10);
         assertArrayEquals(expResult4, result4);
         
         byte[] b2 = {(byte)0x99, (byte)0x43, (byte)0xBA};
         
         byte[] expResult5 = {(byte)0x00, (byte)0x77};
         byte[] result5 = BitUtils.extract(b2, 3, 9);
         assertArrayEquals(expResult5, result5);
         
         byte[] expResult6 = {(byte)0x00, (byte)0xEE};
         byte[] result6 = BitUtils.extract(b2, 2, 10);
         assertArrayEquals(expResult6, result6);
         
         byte[] expResult7 = {(byte)0x99, (byte)0x43};
         byte[] result7 = BitUtils.extract(b2, 8, 16);
         assertArrayEquals(expResult7, result7);
         
         byte[] expResult8 = {(byte)0x03};
         byte[] result8 = BitUtils.extract(b2, 8, 4);
         assertArrayEquals(expResult8, result8);
         
         byte[] expResult9 = {(byte)0x43};
         byte[] result9 = BitUtils.extract(b2, 8, 8);
         assertArrayEquals(expResult9, result9);
         
         byte[] expResult10 = {(byte)0x02, (byte)0x65};
         byte[] result10 = BitUtils.extract(b2, 14, 10);
         assertArrayEquals(expResult10, result10);
         
         byte[] expResult11 = {(byte)0xBA};
         byte[] result11 = BitUtils.extract(b2, 0, 8);
         assertArrayEquals(expResult11, result11);
         
         byte[] expResult12 = {(byte)0x03};
         byte[] result12 = BitUtils.extract(b2, 7, 2);
         assertArrayEquals(expResult12, result12);
         
         byte[] expResult13 = {(byte)0x01};
         byte[] result13 = BitUtils.extract(b2, 7, 1);
         assertArrayEquals(expResult13, result13);
         
         byte[] expResult14 = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] result14 = BitUtils.extract(b2, 0, 24);
         assertArrayEquals(expResult14, result14);
         
         byte[] expResult15 = {(byte)0x16, (byte)0xC3};
         byte[] result15 = BitUtils.extract(b, 4, 15);
         assertArrayEquals(expResult15, result15);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testExtractZeroLength() {
         byte[] b = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] expected = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] result = BitUtils.extract(b, 0, 0);
         assertArrayEquals(expected, result);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testExtractNegativeLength() {
         byte[] b = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] expected = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] result = BitUtils.extract(b, 0, -1);
         assertArrayEquals(expected, result);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testExtractExceedingLength() {
         byte[] b = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] expected = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] result = BitUtils.extract(b, 14, 30);
         assertArrayEquals(expected, result);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testExtractExceedingStart() {
         byte[] b = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] expected = {(byte)0x99, (byte)0x43, (byte)0xBA};
         byte[] result = BitUtils.extract(b, 26, 12);
         assertArrayEquals(expected, result);
     }
 }
