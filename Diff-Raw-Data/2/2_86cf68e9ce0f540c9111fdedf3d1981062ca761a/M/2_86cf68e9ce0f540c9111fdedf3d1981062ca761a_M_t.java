 /*
  Copyright (C) 2012 William James Dyce
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package wjd.math;
 
 import java.util.Random;
 
 /**
  * Static mathematical functions missing form Java's Math class.
  *
  * @author wdyce
  * @since Dec 6, 2012
  */
 public abstract class M 
 {
   /* CONSTANTS */
   
   /**
    * Phi, the "golden ratio".
    */
   public static final double PHI = 1.61803398875;
   
   /* FUNCTIONS */
   
   public static int ipow2(int n)
   {
     return (1 << n);
   }
   
   /**
    * Calculate the square of a value.
    * 
    * @param x the value to square.
    * @return x squared.
    */
   public static double sqr(double x)
   {
     return x*x;
   }
   
   /**
    * Calculate the integral square-root of a value, that is the highest integer
    * which, squared, is smaller than the value in question.
    * 
    * @param x the value to get the integral square-root of.
    * @return the integral square-root of x.
    */
   public static int isqrt(double x)
   {
     if(x < 1)
       return 0;
     
     int i = 0;
     while(i*i <= x) i++;
     return (i-1);
   }
   
   /**
    * Calculate the integral binary (base-2) logarithm of a value, that is the 
    * highest integer value 'i' such that 2^i is less than or equal to the 
    * value in question. 
    * @param x the value to get the integral binary logarithm of.
   * @return return integral binary logarithm of x.
    */
   public static int ilog2(double x)
   {
     if(x < 1)
       return 0;
     
     int i = 0;
     while(x >= 1)
     {
       i++;
       x *= 0.5f;
     }
     return (i-1);
   }
   
   /**
    * Generate a value with a random sign and a capped abosolute value.
    * 
    * @param value the maximum/minimum value to generate.
    * @param r the Random object to use to generate the pseudo-random number.
    * @return a double between -value and +value
    */
   public static double signedRand(double value, Random r)
   {
     double x = (r != null) ? r.nextDouble() : Math.random();
     return (x < 0.5f) ? value*2*x : value*2*(x-0.5f);
   }
 }
