 // Copyright (C) 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.testutility;
 
 import junit.framework.Assert;
 
 import HTTPClient.NVPair;
 
 
 /**
  * Stuff missing from JUnit.
  *
  * @author    Philip Aston
  */
 public class AssertUtilities extends Assert {
 
   public static void assertArraysEqual(Object[] a, Object[] b) {
     assertArraysEqual("", a, b);
   }
 
   public static void assertArraysEqual(String message, Object[] a,
                                        Object[] b) {
     if (message != "") {
       message = message + ": ";
     }
 
     if (a != null || b != null) {
       assertNotNull(message + "first is not null", a);
      assertNotNull(message + "second is not null", b);
 
       assertEquals(message + "arrays of equal length", a.length, b.length);
 
       for (int i = 0; i < a.length; ++i) {
        assertEquals(message + "element " + i + " types equal",
                     a[i].getClass(), b[i].getClass());
         assertEquals(message + "element " + i + " equal", a[i], b[i]);
       }
     }
   }
 
   public static void assertArraysEqual(byte[] a, byte[] b) {
     assertArraysEqual("", a, b);
   }
 
   public static void assertArraysEqual(String message, byte[] a, byte[] b) {
 
     if (message != "") {
       message = message + ": ";
     }
 
     if (a != null || b != null) {
       assertNotNull(message + "first is not null", a);
      assertNotNull(message + "second is not null", b);
 
 
       assertEquals(message + "arrays of equal length", a.length, b.length);
 
       for (int i = 0; i < a.length; ++i) {
         assertEquals(message + "element " + i + " equal", a[i], b[i]);
       }
     }
   }
 
   public static void assertArraysEqual(NVPair[] a, NVPair[] b) {
     assertArraysEqual("", a, b);
   }
 
   public static void assertArraysEqual(String message, NVPair[] a,
                                        NVPair[] b) {
 
     if (message != "") {
       message = message + ": ";
     }
 
     if (a != null || b != null) {
       assertNotNull(message + "first is not null", a);
      assertNotNull(message + "second is not null", b);
 
       assertEquals(message + "arrays of equal length", a.length, b.length);
  
       for (int i=0; i<a.length; ++i) {
         assertEquals(message + "NVPair " + i + " name matches", 
                      a[i].getName(), b[i].getName());
         assertEquals(message + "NVPair " + i + " value matches",
                      a[i].getValue(), b[i].getValue());
       }
     }
   }
 }
