 package com.mbutlitsky.mk;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aristofun
  * Date: 02.03.13
  * Time: 17:19
  */
 public class EnsemblePolochka extends Ensemble {
     /**
      * polochka deepness in kT
      */
     public static double EPSILON = 4.0;
 
     private final double myEpsilon;
 
     public EnsemblePolochka(EOptions options) {
         super(options);
         myEpsilon = EPSILON;
         initialize();
     }
 
     @Override
     /**
      * e^2 = 23,07077154753849 * e-20
      * Bohr (cm) = 5,2917721092e-9
      * k (in SGS) = 1,3806488 e-16
      *
      * => converted potential coeff. = e^2 / 7,30607881244045 * (e+5) = 3,
      * 15775016117465 e+5 = e+6/ 3,1668116505709 = 315775,01611746440408
      * */
     protected final double getPotential(double r, boolean attraction) {
 
         if (attraction)   // ion-electron
         {
             if (r < (315775.01611746440408 / (T * myEpsilon)))
                return -1 * myEpsilon; // temperature;
             else {
                 return (-1 * 315775.01611746440408 / (T * r));
             }
         } else {
             if (r < 1)  // in Bor's radiuses
                 return getPotential(1, false);
             else
                 // The hard-coded Coloumb energy, always the same.
                 return (315775.01611746440408 / (T * r));
         }
     }
 }
