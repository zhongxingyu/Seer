 package name.admitriev.spsl.numbers;
 
 import java.math.BigInteger;
 
 public class Rational implements Comparable<Rational> {
     public BigInteger numerator, denominator;
 
     public Rational(BigInteger numerator, BigInteger denominator) {
         if (denominator.equals(BigInteger.ZERO))
             throw new IllegalArgumentException();
         BigInteger gcd = numerator.gcd(denominator);
         this.numerator = numerator.divide(gcd);
         this.denominator = denominator.divide(gcd);
         if (denominator.compareTo(BigInteger.ZERO) < 0) {
             this.numerator = numerator.negate();
             this.denominator = denominator.negate();
         }
     }
 
 	public Rational(BigInteger numerator) {
 		this.numerator = numerator;
 		this.denominator = BigInteger.ONE;
 	}
 
 
 	public Rational(long numerator) {
 		this(BigInteger.valueOf(numerator));
 	}
 
 	public Rational(long numerator, long denominator) {
 		this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator)) ;
 	}
 
 
 	public Rational add(Rational r) {
         BigInteger y = denominator.multiply(r.denominator).divide(denominator.gcd(r.denominator));
         BigInteger x = numerator.multiply(y.divide(denominator)).add(r.numerator.multiply(y.divide(r.denominator)));
         return new Rational(x, y);
     }
 
     public Rational subtract(Rational r) {
         return add(r.negate());
     }
 
     public Rational multiply(Rational r) {
         BigInteger y = denominator.multiply(r.denominator);
         BigInteger x = numerator.multiply(r.numerator);
         return new Rational(x, y);
     }
 
     public Rational revert() {
         return new Rational(denominator, numerator);
     }
 
     public Rational divide(Rational r) {
         return multiply(r.revert());
     }
 
     public Rational negate() {
         return new Rational(numerator.negate(), denominator);
     }
 
     @Override
     public String toString() {
         return numerator + "/" + denominator;
     }
 
     @Override
     public int compareTo(Rational other) {
         return numerator.multiply(other.denominator).compareTo(denominator.multiply(other.numerator));
     }
 
     @Override
     public int hashCode() {
         int result = numerator.hashCode();
         return 31 * result + denominator.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this)
             return true;
         if (obj == null || obj.getClass() != getClass())
             return false;
         Rational r = (Rational) obj;
         return numerator.equals(r.numerator) && denominator.equals(r.denominator);
     }
 
 	public BigInteger ceil() {
 		BigInteger[] dr = numerator.divideAndRemainder(denominator);
		if(!dr[1].equals(BigInteger.ZERO))
			dr[0] = dr[0].add(BigInteger.ONE);
 		return dr[0];
 	}
 
 	public BigInteger floor() {
		return numerator.divide(denominator);
 	}
 }
