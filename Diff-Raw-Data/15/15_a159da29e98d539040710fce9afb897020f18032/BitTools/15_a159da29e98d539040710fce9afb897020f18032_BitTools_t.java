/*
  * BitTools.java
  *
 * Created on 27. März 2011, 18:42
  *
  * This class provides methods to encode and decode characters in UTF-8 on bit
  * level using a stack to store the bits.
  * The bits are pushed on the stack backwards so that Stack.pop() returns them
  * in the right order.
  */
 
 package main;
 
 import java.util.Stack;
 
 /**
  * @author Raphael Brandis
  * These methods are based on the bits2Ints() and toBinary() methods from
  * http://www.java2s.com/Code/Java/Collections-Data-Structure/Convertbitsettointarrayandstring.htm
  *
 static int[] bits2Ints(BitSet bs) {
     int[] temp = new int[bs.size() / 32];
     
     for (int i = 0; i < temp.length; i++)
         for (int j = 0; j < 32; j++)
             if (bs.get(i * 32 + j))
                 temp[i] |= 1 << j;
     
     return temp;
 }
 
 static String toBinary(int num) {
     StringBuffer sb = new StringBuffer();
     
     for (int i = 0; i < 32; i++) {
         sb.append(((num & 1) == 1) ? '1' : '0');
         num >>= 1;
     }
     
     return sb.reverse().toString();
 }
  */
 public class BitTools {
     public static void encodeUTF8Character(int num, Stack<Boolean> reversedBits) {
         int i, size = 1;
         
         if (num > 65535) {
             // 4 Bytes
             size = 4;
         } else if (num > 2047) {
             // 3 Bytes
             size = 3;
         } else if (num > 127) {
             // 2 Bytes
             size = 2;
         }
         
         // write continuation bytes starting with 10
         for (i = 0; i < size-1; i++) {
             for (int j = 0; j < 6; j++) {
                 reversedBits.push(((num & 1) == 1) ? true : false);
                 num >>= 1;
             }
             reversedBits.push(false);
             reversedBits.push(true);
         }
         
         // write the starting byte
         // one-byte characters start with 0, two-byte characters with 110,
         // three-byte characters with 1110 and four-byte characters 1110
         for (i = 0; i < (size == 1 ? 7 : 7 - size); i++) {
             reversedBits.push(((num & 1) == 1) ? true : false);
             num >>= 1;
         }
         reversedBits.push(false);
         for (i = i + 1; i < 8; i++) {
             reversedBits.push(true);
         }
     }
     
     public static int decodeUTF8Character(Stack<Boolean> bits) {
         int i, j, size, temp = 0;
         
         // determine the number of bytes
         switch (bits.size()) {
             case 32:
                 size = 4;
                 break;
             case 24:
                 size = 3;
                 break;
             case 16:
                 size = 2;
                 break;
             default:
                 size = 1;
                 break;
         }
         
         // handle the continuation bytes (10xx xxxx)
         for (i = 0; i < size - 1; i++) {
             // the first six bits are relevant here
             for (j = 0; j < 6; j++) {
                 if (bits.pop()) {
                     temp |= 1 << (i * 6 + j);
                 }
             }
             // delete the two bits at the beginning
             bits.pop();
             bits.pop();
         }
         
         // handle the starting byte
         for (j = 0; j < (size == 1 ? 7 : 7 - size); j++) {
             if (bits.pop()) {
                 temp |= 1 << (i * 6) + j;
             }
         }
         return temp;
     }
 }
