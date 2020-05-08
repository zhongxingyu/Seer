 package com.javautil.stringhandler;
 
 import sun.misc.Unsafe;
 
 import java.lang.reflect.Field;
 
 /**
  * Created with TwinsFactory.
  * User: andysong
  * Date: 8/14/12
  * Time: 11:19 AM
  * New version of java string builder. It will faster than java StringBuilder in several factor;
  * @author      hqxsn
  * @version     0.1 beta
  */
 public class StringBuilderV1 {
 
     private static final Unsafe unsafe;
 
     static
     {
         try
         {
             Field field = Unsafe.class.getDeclaredField("theUnsafe");
             field.setAccessible(true);
             unsafe = (Unsafe)field.get(null);
         }
         catch (Exception e)
         {
             throw new RuntimeException(e);
         }
     }
 
     private static final long charArrayOffset = unsafe.arrayBaseOffset(char[].class);
 
     private char[] charArray;
     private int pos;
     private int capacity;
 
    private static final int SIZE_OF_CHAR = 2;
    private static final int SIZE_OF_INT = 4;
    //private static final int SIZE_OF_FLOAT = 4;
    //private static final int SIZE_OF_DOUBLE = 8;
    private static final int SIZE_OF_LONG = 8;

     public StringBuilderV1() {
         capacity = 16;
         charArray = new char[capacity];
         pos = 0;
     }
 
     void expandCapacity(int minimumCapacity) {
         int newCapacity = (charArray.length + 1) * 2;
         if (newCapacity < 0 || newCapacity == Integer.MAX_VALUE) {
             newCapacity = Integer.MAX_VALUE;
         } else if (minimumCapacity > newCapacity) {
             newCapacity = minimumCapacity;
         }
         char[] newCharArray = new char[newCapacity];
         unsafe.copyMemory(charArray, charArrayOffset, newCharArray, charArrayOffset, charArray.length << 1);
         charArray = newCharArray;
         capacity = newCapacity;
     }
 
     public StringBuilderV1 append(int i) {
 
         if (i == Integer.MIN_VALUE) {
             append("-2147483648");
             return this;
         }
         int appendedLength = (i < 0) ? Helper.stringSize(-i) + 1
                                      : Helper.stringSize(i);
         int spaceNeeded = pos + appendedLength;
         if (spaceNeeded > capacity)
             expandCapacity(spaceNeeded);
         Helper.getChars(i, spaceNeeded, charArray);
         pos = spaceNeeded;
         return this;
     }
 
     public StringBuilderV1 append(long l) {
 
 
         return this;
     }
 
     public StringBuilderV1 append(float f) {
 
 
         return this;
     }
 
     public StringBuilderV1 append(char[] str) {
         this.append(str, 0, str.length);
         return this;
     }
 
     public StringBuilderV1 append(char[] str, int offset, int len) {
         long bytesToCopy = len << 1;
 
          if ((pos + len) > capacity) {
             //long beginTime = System.nanoTime();
             expandCapacity(pos + len);
             //System.out.println("expand capacity: " + (System.nanoTime() - beginTime));
         }
 
         unsafe.copyMemory(str, charArrayOffset + offset, charArray, charArrayOffset + pos, bytesToCopy);
         pos = (int) (pos + len);
         return this;
     }
 
     public StringBuilderV1 append(String strVal) {
 
         /*long beginTime = System.nanoTime();
   char[] values = (char[])stringValField.get(strVal);
   System.out.println("get fields: " + (System.nanoTime() - beginTime));*/
 
         long bytesToCopy = strVal.length() << 1;
 
         if ((pos + strVal.length()) > capacity) {
             //long beginTime = System.nanoTime();
             expandCapacity(pos + strVal.length());
             //System.out.println("expand capacity: " + (System.nanoTime() - beginTime));
         }
         //beginTime = System.nanoTime();
         //unsafe.copyMemory(values, charArrayOffset, charArray, charArrayOffset + pos, bytesToCopy);
 
         //System.out.println("copy content: " + (System.nanoTime() - beginTime));
 
         strVal.getChars(0, strVal.length(), charArray, pos);
         pos = (int) (pos + strVal.length());
         return this;
     }
 
     public String toString() {
         return new String(charArray, 0, pos >> 1);
     }
 
 
 
     public static void main(String[] args) throws InstantiationException {
 
 
     }
 
 }
