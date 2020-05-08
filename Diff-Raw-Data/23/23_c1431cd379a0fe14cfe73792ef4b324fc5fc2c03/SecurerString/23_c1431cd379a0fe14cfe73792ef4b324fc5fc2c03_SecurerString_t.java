 /* Copyright (c) 2013-, Simon Lundström <simmel@soy.se>
 
 Permission to use, copy, modify, and/or distribute this software for any
 purpose with or without fee is hereby granted, provided that the above
 copyright notice and this permission notice appear in all copies.
 
 THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 PERFORMANCE OF THIS SOFTWARE.
 */
 package se.soy.securerstring;
 import java.lang.reflect.*;
 import java.security.SecureRandom;
 
 public class SecurerString implements AutoCloseable {
 
   public char[] value;
 
   @Override
   public void close() {
     secureEraseInstance(this.value);
   }
 
   public static void secureErase(char extinct[]) {
     if (null == extinct) {
       return;
     }
     try (SecurerString c = new SecurerString(extinct)) {}
   }
 
   public static void secureErase(String extinct) {
     if (null == extinct) {
       return;
     }
     try (SecurerString c = new SecurerString(extinct)) {}
   }
 
   public void secureEraseInstance(char extinct[]) {
     byte bytes[] = new byte[extinct.length];
     SecureRandom random = new SecureRandom();
     random.nextBytes(bytes);
     for (int i = 0; i < extinct.length; i = i + 1) {
       extinct[i] = (char)(bytes[i]);
     }
     extinct = null;
   }
 
   public void secureEraseInstance(String extinct) {
     Field valueField = null;
     try {
       valueField = String.class.getDeclaredField("value");
     }
    // This will never happen, FLW...
    catch (NoSuchFieldException e) {}
 
     valueField.setAccessible(true);
     try {
       secureEraseInstance((char[])valueField.get(extinct));
     }
    // This will never happen, FLW...
    catch (IllegalAccessException e) {}
     extinct = null;
   }
 
   public int nearestInt(int minLength) {
     int length = 0;
     while (length < minLength) {
       length += 1024;
     }
     return length;
   }
 
   public SecurerString() {
   }
 
   public SecurerString(String str) {
     Field valueField = null;
     try {
       valueField = String.class.getDeclaredField("value");
     }
    // This will never happen, FLW...
    catch (NoSuchFieldException e) {}
     valueField.setAccessible(true);
 
     try {
       this.value = new char[nearestInt(((char[])valueField.get(str)).length)];
       System.arraycopy((char[])valueField.get(str), 0, this.value, 0, ((char[])valueField.get(str)).length);
       secureEraseInstance((char[])valueField.get(str));
     }
    // This will never happen, FLW...
    catch (IllegalAccessException e) {}
   }
 
   public SecurerString(char[] chr) {
     this.value = new char[nearestInt(chr.length)];
     System.arraycopy(chr, 0, this.value, 0, chr.length);
     secureEraseInstance(chr);
   }
 }
