 /*
  *  Instead of using a uniform distribution, we'll use
  *  an exponential distribution, which should better model
  *  the real-world system.
  *
  *  Independent events; autonomous processes
  *
  *  Exponential distribution: average arrival rate
  */
 
package com.austingulati.opsys.project1;

import java.lang.Math;

 public class ExponentialRandom
 {
     public int nextInt()
     {
         double lambda = 0.001;
         double r = Math.random();
         double x;
 
         while(true)
         {
             x = -Math.log(r) / lambda;
            if(x > 8000)
            {
                continue;
            }
             break;
         }
 
         return (int)Math.floor(x);
     }
 }
