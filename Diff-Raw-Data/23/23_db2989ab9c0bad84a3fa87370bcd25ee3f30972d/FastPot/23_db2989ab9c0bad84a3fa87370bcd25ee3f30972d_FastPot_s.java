 /*
  * Copyright (C) 2011 Kilian Gaertner
  * 
  * Dieser Quelltext ist Open Source und kann von jedem verwendet werden, der 
  * folgende Bedingung einhlt:
  * Jeder, der den Quelltext, ob in Teilen oder komplett,nutzt, muss dem Inhabenden
  * des Copyrights eine Pizza spendieren, sollte derjenige dem Inhabenden des Copyrights
  * begegnen.
  */
 
 package training7;
 
 /*
  * a)
  * x = 2, n = 13
  * 2^13 = 2 *   2^12
  *              2^12 = 4^6
  *              4^6  = 16^3
  *                     16 * 16^2
  *                          256^1
  *                          256*256^0
  *                              1
  *                          256*1
  *                     16 * 256
  *        2 * 16 * 256
  *       = 8192
  */
 
 public class FastPot {
 
     private static int counterRek = 0;
     private static int counterIter = 0;
 
     public static double fastPotRek(double x, int n) {
         ++counterRek;
         if (n == 0)
             return 1;
         else if (n % 2 == 0)
             return fastPotRek(x * x, n / 2);
         else
             return x * fastPotRek(x, --n);
     }
 
     public static double fastPotIter(double x, int n) {
         if (n == 0)
             return 1;
         double temp = 1;
         counterIter = 0;
         while (n > 1) {
             if (n % 2 == 0) {
                 x *= x;
                 n = n / 2;
             }
             else {
                 temp *= x;
                 --n;
             }
             ++counterIter;
         }
         return x * temp;
     }
 
     public static void test() {
 
        for (int i = 0; i <= 31; ++i) {
             System.out.println(i + " = iter(" + fastPotIter(2, i) + ") with "
                     + counterIter + " loops \t rek(" + fastPotRek(2, i)
                     + ")with deepth of " + counterRek);
             counterRek = 0;
         }
     }
 }
