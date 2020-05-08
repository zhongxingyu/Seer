 package CalculatorD;
 import java.math.BigInteger;
 import java.math.BigDecimal;
 
 /**
  * Represents a fraction, and includes various operations (addition,
  * subtraction, and the like) to assist in mathematical computations.
  * Some technical details:
  * <ul>
  * <li>
  * <code>Fractions</code> are represented by a numerator and a denominator
  * </li>
  * <li>
  * Numerators, denominators are stored as BigIntegers.
  * </li>
  * <li>
  * Fractions are immutable (much like Java's <code>String</code> class)
  * </li>
  * </ul>
  * @author Dustin Leavins
  */
 public class Fraction {
 
     private static String DIV_BY_ZERO_MSG = "Denominator cannot equal 0.";
     private BigInteger numerator;
     private BigInteger denominator;
 
     /**
      * Takes a string in the format of "X.Y" (where X and Y are
      * base-10 numbers) and gives a <code>Fraction</code> in return.
      * Technical note:  unlike the other constructor available for this
      * class, this does not throw ArithmeticException.  You can't
      * specify a denominator of 0 using a string.
      * @param stringContainingNumber the input string!
      */
     public Fraction(String stringContainingNumber) {
         char tempCharacter;
         boolean readingValue = true;
         boolean readingFractionalPart = false;
         StringBuffer sbuffer = new StringBuffer(stringContainingNumber);
         long num = 0;
         long denom = 1;
         boolean negativeNumber = false;
 
         // Many strings don't end properly.  In these cases, the
         // following while loop throws an ArrayOutOfBounds exception.
         // This is a quick fix for those cases.
         sbuffer.append("\n");
 
         while (readingValue) {
             tempCharacter = sbuffer.charAt(0);
             sbuffer.deleteCharAt(0);
 
             switch (tempCharacter) {
             case('0'):
             case('1'):
             case('2'):
             case('3'):
             case('4'):
             case('5'):
 
             case('6'):
             case('7'):
             case('8'):
             case('9'):
                 num = (num * 10) + (tempCharacter - '0');
                 if (readingFractionalPart) {
                     denom = (denom * 10);
                 }
                 
                 break;
             case('.'):
                 readingFractionalPart = true;
                 break;
             case('-'):
                 if ((num == 0) || (denom == 1)) {
                     negativeNumber = true;  
                 }
 
                 break;
             default:
                 readingValue = false;
                 break;
             }
         }
 
         if (negativeNumber) {
             num *= -1;
         }
 
         this.numerator = BigInteger.valueOf(num);
         this.denominator = BigInteger.valueOf(denom);
     }
 
     /**
      * Takes a numerator and denominator (as <code>int</code>s)
      * and returns a <code>Fraction</code>.
      * @param numerator
      * @param denominator
      * @throws ArithmeticException 
      *  When (denominator == 0) is <code>true</code>!
      */
     public Fraction(int numerator, int denominator) 
             throws ArithmeticException {
         if (denominator == 0) {
             throw new ArithmeticException(DIV_BY_ZERO_MSG);
         }
         else if (denominator < 0) {
             denominator *= -1;
             numerator *= -1;
         }
         
         this.numerator = BigInteger.valueOf(numerator);
         this.denominator = BigInteger.valueOf(denominator);
     }
     /**
      * Takes a numerator and denominator (as <code>long</code>s)  
      * and returns a <code>Fraction</code>.
      * @param numerator
      * @param denominator
      * @throws ArithmeticException 
      *  When (denominator == 0) is <code>true</code>!
      */
     public Fraction(long numerator, long denominator) 
             throws ArithmeticException {
         if (denominator == 0) {
             throw new ArithmeticException(DIV_BY_ZERO_MSG);
         }
         else if (denominator < 0) {
             denominator *= -1;
             numerator *= -1;
         }
         
         this.numerator = BigInteger.valueOf(numerator);
         this.denominator = BigInteger.valueOf(denominator);
     }
 
     /**
      * Constructs a <code>Fraction</code> from two BigInteger objects.
      * @param numerator
      * @param denominator
      */
    private Fraction(BigInteger numerator, BigInteger denominator) 
            throws ArithmeticException {
         this.numerator = numerator;
         this.denominator = denominator;

        if (denominator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException(DIV_BY_ZERO_MSG);
        }
        else if (denominator.compareTo(BigInteger.ZERO) < 1) {
            this.numerator = numerator.negate();
            this.denominator = denominator.negate();
        }
        else {
            this.numerator = numerator;
            this.denominator = denominator;
        }
     }
  
 
     /**
      * :-)
      * @return numerator of this <code>Fraction</code>
      */
     public BigInteger getNumerator() {
         return numerator;
     }
 
     /**
      * (-:
      * @return denominator of this <code>Fraction</code>
      */
     public BigInteger getDenominator() {
         return denominator;
     }
     
     /**
      * Returns the equivalent floating point value of this
      * <code>Fraction</code>.
      * @return double value
      */
     public double doubleValue() {
         double n = numerator.doubleValue();
        double d = denominator.doubleValue();
        double result = n / d;
 
        return result;
     }
     
     /**
      * Returns the sum of <code>f</code> and </code>this</code>.
      * @param f <code>Fraction</code> to add.
      * @return sum
      */
     public Fraction add(Fraction f) {
         BigInteger returnDenominator = this.denominator.multiply(f.getDenominator());
         BigInteger x = this.numerator.multiply(f.getDenominator());
         BigInteger y = f.getNumerator().multiply(this.denominator);
         BigInteger returnNumerator = x.add(y);
 
         return new Fraction(returnNumerator, returnDenominator);
     }
     
     /**
      * Negative of <code>this</code>.
      * @return negative
      */
     public Fraction negative() {
         return new Fraction(this.numerator.negate(), this.denominator);
     }
     
     /**
      * Returns the difference of </code>this</code> and<code>f</code>.
      * @param f <code>Fraction</code> to subtract from <code>this</code>
      * @return difference
      */
     public Fraction subtract(Fraction f) {
         return this.add(f.negative());
     }
     
     /**
      * Returns the product of </code>this</code> and<code>f</code>.
      * @param f <code>Fraction</code> to multiply
      * @return product
      */
     public Fraction multiply(Fraction f) {
         BigInteger returnNumerator = this.numerator.multiply(f.getNumerator());
         BigInteger returnDenominator = this.denominator.multiply(f.getDenominator());
         return new Fraction(returnNumerator, returnDenominator);
     }
     
     /**
      * Multiplicative inverse of <code>this</code>.
      * @return multiplicative inverse
      */
     public Fraction inverse() throws ArithmeticException {
         return new Fraction(this.denominator, this.numerator);
     }
     
     /**
      * Returns the quotient of </code>this</code> (dividend) 
      * and<code>f</code> (divisor).
      * @param f <code>Fraction</code> to divide by
      * @return quotient
      */
     public Fraction divide(Fraction f) throws ArithmeticException {
         return this.multiply(f.inverse());
     }
     
     /**
      * Returns the string representation of <code>this</code>.
      * @return string representation
      */
     public String toString() {
         String returnString;
         double n = 0.0;
         double d = 0.0;
         double dValueOfFraction = 0.0;
 
         if (denominator.equals(BigInteger.ONE)) {
             returnString = numerator.toString();
         }
         else {
             n = this.numerator.doubleValue();
             d = this.denominator.doubleValue();
             dValueOfFraction = n / d;
             returnString = String.valueOf(n / d);
         }
 
         // Error-checking and Identification
         if (dValueOfFraction > 0.0) {
             if (this.numerator.compareTo(BigInteger.ZERO) != 1) {
                 returnString = "Error";
             }
         }
         else if (dValueOfFraction < 0.0) {
             if (this.numerator.compareTo(BigInteger.ZERO) != -1) {
                 returnString = "Error";
             }
 
         }
 
         return returnString;
     }
 
     /**
      * Compares <code>this</code> to <code>o</code>.
      * @return <code>true</code> if o is a Fraction with the same
      * numerator and denominator as <code>this</code>, <code>false</code>
      * otherwise.
      */
     public boolean equals(Object o){
         Fraction f;
         boolean numEquals;
         boolean denomEquals;
 
         if (this == o) {
             return true;
         }
         else if (!(o instanceof Fraction)) {
             return false;
         }
 
         f = (Fraction) o;
 
         numEquals = this.numerator.equals(f.getNumerator());
         denomEquals = this.denominator.equals(f.getDenominator());
 
         return numEquals && denomEquals;
     }
 
     /**
      * Returns a hash code value for <code>this</code> based on hash codes
      * for the numerator and denominator.
      * @return hash code
      */
     public int hashCode() {
         int returnCode = 5;
         returnCode = (31 * returnCode) + numerator.hashCode();
         returnCode = (31 * returnCode) + denominator.hashCode();
 
         return returnCode;
     }
 
     /**
      * The value 0.
      */
     public static final Fraction ZERO = new Fraction(0,1);
 }
