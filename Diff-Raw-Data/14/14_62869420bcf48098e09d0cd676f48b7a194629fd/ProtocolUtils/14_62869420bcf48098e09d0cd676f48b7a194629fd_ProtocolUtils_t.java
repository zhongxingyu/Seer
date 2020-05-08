 /*
  * @(#)ProtocolUtils.java	1.00 17-Oct-1998
  */
 
 package de.tubs.cs.iti.jcrypt.protokoll;
 
 
 import java.math.BigInteger;
 import java.util.Random;
 import java.lang.String;
 
import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
 
 
 /**
  * This class provides some special methods for BigInteger arithmetic
  * that are useful for certain cryptographic algorithms.
  *
  * @author  <a href="mailto:M.Willers@tu-bs.de">Martin Willers</a>
  * @author  <a href="mailto:M.Seemann@tu-bs.de">Markus Seemann</a>
  * @version 1.00, 17-Oct-1998
  */
public class ProtocolUtils extends BigIntegerUtil {
 
   /* Don't let anyone instantiate this class */
   private ProtocolUtils() {
   }
 
   /**
    * Hashes an identification string I and an integer j to a BigInteger
    * of [0, max - 1], i.e. [0, 2^(max.bitLength () - 1) - 1]
    * where max / 2 < 2^(max.bitLength () - 1) = 2^( [lb(max)] ) <= max
    *
    * @param   I   an identification string
    * @param   j   an integer
    * @param   max a BigInteger
    * @return  a hash value of [0, max - 1]
    */
   public static BigInteger ID (String I, int j, BigInteger max)
   // Maps (I,j) to [0, max - 1], i.e. [0, 2^(max.bitLength () - 1) - 1]
   // where max / 2 < 2^(max.bitLength () - 1) = 2^( [lb(max)] ) <= max
   {
 	  return (new BigInteger (max.bitLength (), 
 		        new Random ((long) (I + String.valueOf (j)).hashCode ()))).remainder (max);
   }
 
   /**
    * This routine yields a solution to the Chinese Remainder Theorem (CRT),
    * as described in Algorithmus 3.4.
    * This special case describes the application with t=2.
    *
    * @param   d1  a BigInteger
    * @param   d2  a BigInteger
    * @param   x1  a BigInteger
    * @param   x2  a BigInteger
    * @return  x of [0, (d1*d2)-1] solving x mod di = xi for i=1,2
    */
   public static BigInteger crt(BigInteger d1, BigInteger d2, BigInteger x1, BigInteger x2) 
   {
     BigInteger y1, y2, n = d1.multiply (d2);
     y1 = (d2.mod(d1)).modInverse(d1);
     y2 = (d1.mod(d2)).modInverse(d2);
     return (d2.multiply(y1).multiply(x1).mod(n).add(d1.multiply(y2).multiply(x2)).mod(n));
   }
 
 
   /**
    * See ALGORITHMS 5.2.1.
    * Calculate the square root of <code>val</code> modulo <code>modulus</code>.
    * <code>modulus</code> must be a prime number, and <code>val</code> a quadratic remainder.
    * Then, an x out of [1, ..., modulus-1] is calculated with 
    * x**2 mod <code>modulus</code> = a mod <code>modulus</code>.
    *
    * @param   val      a BigInteger that is to be square rooted.
    * @param   modulus  a BigInteger.
    * @return  a square root of <code>val</code>
    */
   public static BigInteger sqrtMod (BigInteger val, BigInteger modulus) 
   {
     BigInteger b, x, y, exp, exp2;
     BigInteger[] lm;
     int i, n;
     int K[] = new int[300];
 
     val = val.mod (modulus);
     if (val.equals (ZERO))
       return ZERO;
     if (modulus.equals (TWO))
       return ONE;
     do 
     {
      b = randomBetween(ONE, modulus);	
       // Choose random number b with 1 <= b < p.
     } 
     while ((b.modPow(modulus.subtract(ONE).shiftRight(1), modulus)).compareTo(ONE) == 0);
     /* Erfolg hat der Algorithmus bei Nichterfuellung der while-Bedingung */
 
     // Im Erfolgsfalle bestimme l, m aus N, m ungerade, mit "p-1 = (2^l)*m"
     lm = sucheLM(modulus);
     y = val;
     n = 1; 
     // Suche das kleinste K[n] >= 0 mit y^((2^k[n])*m) mod modulus = 1
     K[n] = sucheKN(y, lm[1], modulus);
     while (K[n] != 0) {
       exp = ONE.shiftLeft((lm[0].subtract(BigInteger.valueOf(K[n]))).intValue());
       y = (y.multiply(b.modPow(exp, modulus))).mod(modulus);
       K[++n] = sucheKN(y, lm[1], modulus);
     }
     x = y.modPow(lm[1].add(ONE).shiftRight(1), modulus);
     for (i=n; i>1; i--) {
       exp = ONE.shiftLeft((lm[0].subtract(BigInteger.valueOf(K[i-1])).subtract(ONE)).intValue());
       exp2 = b.pow(exp.intValue());
       x = (x.multiply(exp2.modInverse(modulus))).mod(modulus);
     }
 
     return x;
   }
 
   /*
    * Die folgende Routine ist ein Teil von Algorithmus 5.2.1.
    * Die Zahl p wird als ungerade vorausgesetzt.
    * Der Algorithmus isoliert die ungeraden Faktoren. (u.a.)
    * Die Endbedingung ist:  p-1 = m*2^l, l maximal.
    */
   private static BigInteger[] sucheLM(BigInteger p) {
     BigInteger[] lm = new BigInteger[2];
     lm[0] = ZERO;
     lm[1] = p.subtract(ONE);
    while (isEven(lm[1])) {
       lm[1] = lm[1].shiftRight(1);
       lm[0] = lm[0].add(ONE);
     }
     return lm;
   }
 
   /*
    * Diese Routine ist ein Teil von Algorithmus 5.2.1.
    * Sie liefert: min { k >= 0 | y^((2^k)*m) mod p = 1 }
    */
   private static int sucheKN(BigInteger y, BigInteger m, BigInteger p) {
     int k = 0;
     BigInteger exp = m;
 
     while ((y.modPow(exp, p)).compareTo(ONE) != 0)
     {
       k++;
       exp = exp.shiftLeft (1);
     }
     return k;
   }
 
   /*
    * Liefert ein x mit 1 <= x < n und ggT(x,n) = 1.
    */
   private static BigInteger sucheXkleinerN(BigInteger n) {
     BigInteger x = BigInteger.valueOf(0);
     do {
      x = randomBetween(ONE, n.subtract(ONE));
     } while ((x.gcd(n)).compareTo(ONE) != 0);
     return x;
   }
 }
