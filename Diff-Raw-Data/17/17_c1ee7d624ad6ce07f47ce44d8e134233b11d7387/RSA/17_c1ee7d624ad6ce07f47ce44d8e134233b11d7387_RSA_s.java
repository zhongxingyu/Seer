 package net.codefactor.spacefighters.utils;
 
 import java.math.BigInteger;
 import java.util.Properties;
 import java.util.Random;
 
 public class RSA {
   public BigInteger d;
   public BigInteger e;
   public BigInteger n;
 
   public static void main(String[] args) {
     BigInteger m = new BigInteger("ec6ec3c848f9c72d0a92a035c9aaa151", 16);
     RSA r = new RSA(
         "6732a04572354a7a15a33a4f14ca6d374db55e0334ec37566dd93b7c45c40499",
         "f0d8e95de637a499ec81638304ad15bbed9cd84055b8e2f75fa92a8cab1859a9",
         "74f6cb6923dac0cfbc524d363360cd4d3d784066b60817d86fd2be3672f5e713", 16);
     BigInteger c = r.encrypt(m);
     System.out.println(c.toString(16));
     System.out.println(r.decrypt(c).toString(16));
   }
 
   public RSA(int SIZE) {
     /* Step 1: Select two large prime numbers. Say p and q. */
     BigInteger p = new BigInteger(SIZE, 100, new Random());
     BigInteger q = new BigInteger(SIZE, 100, new Random());
 
     /* Step 2: Calculate n = p.q */
     n = p.multiply(q);
 
     /* Step 3: Calculate ø(n) = (p - 1).(q - 1) */
     BigInteger PhiN = p.subtract(BigInteger.valueOf(1));
     PhiN = PhiN.multiply(q.subtract(BigInteger.valueOf(1)));
 
     /* Step 4: Find e such that gcd(e, ø(n)) = 1 ; 1 < e < ø(n) */
     do {
       e = new BigInteger(2 * SIZE, new Random());
 
     } while ((e.compareTo(PhiN) != 1)
         || (e.gcd(PhiN).compareTo(BigInteger.valueOf(1)) != 0));
 
     /* Step 5: Calculate d such that e.d = 1 (mod ø(n)) */
     d = e.modInverse(PhiN);
   }
 
   public RSA(String sd, String se, String sn, int radix) {
     d = new BigInteger(sd, radix);
     e = new BigInteger(se, radix);
     n = new BigInteger(sn, radix);
   }
 
   public RSA(Properties props) {
     d = new BigInteger(props.getProperty("d"), 16);
     e = new BigInteger(props.getProperty("e"), 16);
     n = new BigInteger(props.getProperty("n"), 16);
   }
 
   public BigInteger encrypt(BigInteger plaintext) {
     return plaintext.modPow(e, n);
   }
 
   public BigInteger decrypt(BigInteger ciphertext) {
     return ciphertext.modPow(d, n);
   }
 
   public String toString() {
    return "d: " + d.toString(16) + "\ne: " + e.toString(16) + "\nn: "
        + n.toString(16);
   }
 }
