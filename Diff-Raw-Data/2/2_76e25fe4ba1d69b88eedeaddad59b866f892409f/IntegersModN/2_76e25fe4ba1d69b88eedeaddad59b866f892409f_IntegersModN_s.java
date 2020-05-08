 package org.stardust.math.group;
 
 import java.math.BigInteger;
 
 import static java.math.BigInteger.ZERO;
 import static java.math.BigInteger.valueOf;
 
 /**
  * Created with IntelliJ IDEA.
  * User: evadrone
  * Date: 10/18/13
  * Time: 9:21 PM
  */
 public class IntegersModN implements Group<BigInteger> {
 
    private BigInteger modulus;
 
     public IntegersModN(Integer modulus) {
         this(valueOf(modulus));
     }
 
     public IntegersModN(BigInteger modulus) {
         this.modulus = modulus;
     }
 
     public BigInteger operate(Integer a, Integer b) {
         return operate(valueOf(a), valueOf(b));
     }
 
     public BigInteger operateN(Integer a, Integer n) {
         return operateN(valueOf(a), valueOf(n));
     }
 
     public BigInteger getInverse(Integer a) {
         return getInverse(valueOf(a));
     }
 
     public boolean contains(Integer a) {
         return contains(valueOf(a));
     }
 
     @Override
     public BigInteger operate(BigInteger a, BigInteger b) {
         return a.add(b).mod(modulus);
     }
 
     @Override
     public BigInteger operateN(BigInteger a, BigInteger n) {
         return a.multiply(n).mod(modulus);
     }
 
     @Override
     public BigInteger getIdentity() {
         return ZERO;
     }
 
     @Override
     public BigInteger getInverse(BigInteger a) {
         return a.negate();
     }
 
     @Override
     public boolean contains(BigInteger a) {
         return ZERO.compareTo(a) <= 0 && modulus.compareTo(a) > 0;
     }
 }
