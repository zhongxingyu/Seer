 /*
  * Int34.java
  *
  * Created on May 7, 2007, 5:04 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Donghwan
  */
 import java.lang.String ;
 import java.lang.Integer;
 import java.lang.Long;
 import java.math.BigInteger;
 import java.util.*;
 
 public class Int34 {
     BigInteger bigValue;
 
     /**
      * Int34 constructor will construct a 34 bit value. It
      * will correctly convert the 34 bit to its two bit
      * compliment value.
      */
     public Int34(long value) {
     	if (Long.signum(value) == 1) {
     	    //If bit 33 (counting from zero) is 1
     	    //calculate two's compliment
     		if (value >= 0x200000000L)
     			create(-1, ~value + 1);
     		else
     		    create(1, value);
     	} else {
     		create(-1, -1 * value);
     	}
     }
 
     /**
      * Int34 constructor will construct a 34 bit value.
      * sign specifies weather number is negative or
      * positive.
      */
     public Int34(int sign, long magnitude) {
     	create(sign, magnitude);
     }
 
     /**
      * create(int sign, long magnitude) creates a BigInteger
      * from a byte array containing the magnitude and a sign
      * that specifies whether the byte is positive (1),
      * negative (-1), or zero (0)
      */
     public void create(int sign, long magnitude) {
     	  byte byteValue[] = new byte[5];
     	  //grab bit 32
     	  byteValue[0] = (byte)((magnitude & 0x0300000000L) >> 32);
     	  //byteValue[0] = (byte)((magnitude & 0x0100000000L) >> 32);
     	  //grab bits 31-24
         byteValue[1] = (byte)((magnitude & 0xff000000L) >> 24);
         //grab bits 23-16
         byteValue[2] = (byte)((magnitude & 0xff0000L) >> 16);
         //grab bits 15-8
         byteValue[3] = (byte)((magnitude & 0xff00L)   >> 8);
         //grab bits 7-0
         byteValue[4] = (byte)((magnitude & 0xffL));
         //bit 33 is the sign bit ... :)
         bigValue = new BigInteger(sign, byteValue);
     }
 
     /**
      * longValue() will return a 64 bit value
      * formed from the lower 64 bits of the bigValue.
      */
     public long longValue() {
         return bigValue.longValue();
     }
 
     /**
      * toByteArray() returns a byteArray
      * representing the BigValue
      */
     public byte[] toByteArray() {
     	byte[] res = bigValue.toByteArray();
     	if (bigValue.signum() == -1) {
     		 res[0] |= 0x20;
     		 res[0] &= 0x1F;
     	}
     	return res;
     }
 
     public String toString() {
 
     	String temp = Long.toHexString(bigValue.longValue());
     	//less than or equal to 32 bits
     	if (temp.length() <= 8)
     		return temp;
     	//determine if this is a negative 34 bit or positive 33 bit value.
     	else {
     		String sub = temp.substring(temp.length() - 8);
     		char highest = temp.charAt(temp.length() - 9);
     		byte top = Byte.parseByte(String.valueOf(highest), 16);
     		top &= 0x3;
     		// int sign = bigValue.signum();
     		// if (sign == -1)
     		//	top |= 0x4;
 
     		return String.valueOf(top) + sub;
 
     	}
     }
 
 
     // converts BigInteger to Int34
     public Int34 toInt34(BigInteger src) {
         return new Int34(src.signum(), src.abs().longValue());
     }
 
 
     // add another Int34 to this and return the result
     public Int34 add(Int34 data) {
         return toInt34(data.bigValue.add(bigValue));
        // return new Int34(data.bigValue.add(bigValue));
     }
 
     // subtract another Int34 from this and return the result
     public Int34 subtract(Int34 data) {
         return toInt34(bigValue.subtract(data.bigValue));
     }
 
     // arithmetic left shift
     public Int34 shiftLeft(int n) {
       //  System.out.println("bit " + (33 - n) + " is set: " +bigValue.testBit(33-n));
        if (bigValue.testBit(33 - n)) {
          return new Int34(-1, bigValue.shiftLeft(n).abs().longValue());
        }
        
         return toInt34(bigValue.shiftLeft(n));
     }
 
     // arithmetic right shift
     public Int34 shiftRight(int n) {
         return toInt34(bigValue.shiftRight(n));
     }
 
     // returns ~bigValue
     public Int34 not() {
         return toInt34(bigValue.not());
     }
 
     public Int34 xor(Int34 data) {
         return toInt34(bigValue.xor(data.bigValue));
     }
 
     public Int34 or(Int34 data) {
         return toInt34(bigValue.or(data.bigValue));
     }
 
     public Int34 and(Int34 data) {
         return toInt34(bigValue.and(data.bigValue));
     }
 
     boolean equals(Int34 o) {
         return bigValue.equals(o.bigValue);
     }
 
     /**
      * signum() return the sign of teh bigValue
      */
     int signum() {
         return bigValue.signum();
     }
 
     /**
      * main() some tests for sanity's sake
      */
     public static void main(String[] args) {
         Int34 a = new Int34(0x3ffffffffL);
         Int34 b = new Int34(0x111111111L);
         Int34 c = new Int34(Long.MAX_VALUE);
 
         System.out.println(a.longValue());
         System.out.println(b.longValue());
         System.out.println(c.longValue());
 
         System.out.println(a);
         System.out.println(b);
         System.out.println(c);
 
         System.out.println("a = " + a.longValue());
         System.out.println("b = " + b.longValue());
         System.out.println("c = " + c.longValue());
 
         System.out.println("a: " + a + " " + a.shiftLeft(3) + " " + a.shiftRight(3) + " " + a.not());
         System.out.println("b: " + b + " " + b.shiftLeft(3) + " " + b.shiftRight(3) + " " + b.not());
         System.out.println("c: " + c + " " + c.shiftLeft(3) + " " + c.shiftRight(3) + " " + c.not());
 
         System.out.println("a + b =  " + a.add(b));
         System.out.println("a - b =  " + a.subtract(b));
         System.out.println("a & b =  " + a.and(b));
         System.out.println("a | b =  " + a.or(b));
         System.out.println("a xor b =  " + a.xor(b));
 
         Int34 r1 = new Int34(10);
         Int34 r2 = new Int34(37);
         System.out.println("Answer: " + r1.longValue());
         System.out.println("Answer: " + r2.longValue());
         System.out.println("Answer: " + r1.add(r2));
         System.out.println("Answer: " + r1.add(r2).longValue());
 
         int int32 = 1;
         int shiftedLeft = 1 << 31;
         System.out.println(shiftedLeft);
 
         int test = -14;
         System.out.println("-14 >> 1: " + (test >> 1));
 
         Long ref = -1L << 33;
         System.out.println("-1 << 33 Correct: " + ref);
         System.out.println("-1 << 33 Correct: " + Long.toHexString(ref));
 
         Int34 shiftTest = new Int34(0x1L);
 
         shiftTest = shiftTest.shiftLeft(33);
         System.out.println("-1 << 33 int34: " + shiftTest);
         System.out.println("-1 << 33 as long (int34): " + shiftTest.longValue());
 
         shiftTest = shiftTest.shiftRight(1);
         System.out.println("0x2 0000 0000 >> 1: " + shiftTest);
         System.out.println("0x2 0000 0000 >> 1: " + shiftTest.longValue());
 
         System.out.println("-1: " + new Int34(-1));
         System.out.println("-1: " + new Int34(-1).longValue());
     }
 }
