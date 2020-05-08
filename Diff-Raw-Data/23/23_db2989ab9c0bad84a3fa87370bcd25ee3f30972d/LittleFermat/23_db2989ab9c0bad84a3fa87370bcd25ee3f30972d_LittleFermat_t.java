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
 
 import java.math.BigInteger;
 import java.util.Random;
 
 public class LittleFermat {
 
     private static Random rand = new Random();
 
     public static BigInteger getRandom(BigInteger n) {
        return new BigInteger(n.bitCount()-1, rand);
     }
 
     public static boolean primeTest(BigInteger p) {
 
         BigInteger test = null;
         for (int i = 0; i < 10; ++i) {
             test = getRandom(p);
            if (!test.modPow(p, p).equals(test))
                 return false;
         }
         return true;
 
     }
 
     public static void test() {
         for (int i = 2; i <= 1000; ++i) {
             if (primeTest(BigInteger.valueOf(i))) {
                 System.out.println(i + " is prime");
             }
         }
     }
 }
