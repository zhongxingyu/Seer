 /*
  * GLMotes is licensed under the FreeBSD (also known as the "Simplified BSD
  * License", which is the BSD license that omits the final "no-endoresment"
  * clause. The original BSD License can be found at
  * http://www.opensource.org/licenses/bsd-license.php with the
  * modified/simplified version found at
  * http://www.freebsd.org/copyright/freebsd-license.html
  *
  * Copyright (c) 2010, Elijah C. Menifee
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are
  * those of the authors and should not be interpreted as representing official
  * policies, either expressed or implied, of Elijah C. Menifee
  */
 
 
 /*
  * The methods in this class are based on the paper found at
  * http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm
  */
 package org.tonoplace.glmotes.utility;
 
 /**
  * Utility class for comparing doubles allowing for error of FP operations.
  * @author Elijah C. Menifee
  * @date Nov 6, 2010 5:39:28 PM
  */
 public class FloatingPointComparison {
   /**
    * To handle numbers close to zero +- with different signs as being close.
    * @param a the first double to test for equals
    * @param b the second double to test for equals
    * @param maxUlps number of bits difference, must be positive and less that
    * 4 * 1024 * 1024.
    * @return true if the IEEE values converted to twos-compliment of a and b
    * are within the difference given by maxUlps.
    * @throws IllegalArgumentException when the maxUlps is outside of the range
    * of 1 to 4 * 1024 * 1024.
    *
    * Translated for double precision, maxUlps not adjusted against doubles...
    */
   public static boolean AlmostEqual2sComplement(double a, double b, long maxUlps)
     throws IllegalArgumentException
   {
     if (maxUlps < 1)
       throw new IllegalArgumentException("maxUlps must be > 0");
     if (maxUlps > ( 4*1024*1024 -1))
       throw new IllegalArgumentException("maxUlps must be < 4 * 1024 * 1024");
 
     // quick test using normal compare
     if (a==b)
       return true;
     
     long aBits = Double.doubleToRawLongBits(a);
     long bBits = Double.doubleToRawLongBits(b);
 
     long intBitField = 0x80000000;
     long twoCbitSubField = intBitField << 32;
     // make aBits lexicographically ordered as a twos-complement int
     if (aBits < 0)
       aBits = twoCbitSubField - aBits;
     if (bBits < 0)
       bBits = twoCbitSubField - bBits;
 
     long diff = Math.abs(aBits - bBits);
     if (diff <= maxUlps)
       return true;
     return false;
   }
 
   /**
    * Copied per verbatim except for translation to Java from
    * http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm
    * @param a first float to compare as almost equal
    * @param b second float to compare as almost equal
    * @param maxUlps max different in number of floats between two values.
    * @return true if floats are almost equal within the maxUlps.
    * @throws IllegalArgumentException
    */
   public static boolean AlmostEqual2sComplement(float a, float b, int maxUlps)
     throws IllegalArgumentException
   {
     // Make sure maxUlps is non-negative ans small enought that the
     // default NAN won't compare as equal to anything.
     if (!(maxUlps>0 && maxUlps<4*1024*1024))
       throw new IllegalArgumentException("Invalid value for maxUlps: 0 < maxUlps < 4 * 1024 *1024");
     int aInt = Float.floatToRawIntBits(a);
     int bInt = Float.floatToRawIntBits(b);
     // Make aInt lexicographically ordered as a twos-complement int
     if (aInt < 0)
       aInt = 0x80000000 - aInt;
     // Make bInt lexicographically ordered as a twos-complement int
     if (bInt < 0)
       bInt = 0x80000000 - bInt;
     int intDiff = Math.abs(aInt - bInt);
     if (intDiff <= maxUlps)
       return true;
     return false;
   }
 }
