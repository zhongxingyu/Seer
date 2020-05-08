 package edu.grinnell.csc207.nikakath.hw4;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 public class Fraction {
 	private BigInteger numerator;
 	private BigInteger denominator;
 
 	/*
 	 * +--------------+ | Constructors | +--------------+
 	 */
 	
 	/*
 	 * Construct Fractions from a variety of input parameters
 	 */
 	
 	public Fraction(int num, int den) {
 		this.numerator = BigInteger.valueOf(num);
 		this.denominator = BigInteger.valueOf(den);
 		this.simplify();
 	} // Fraction(int, int)
 
 	public Fraction(int num) {
 		this.numerator = BigInteger.valueOf(num);
 		this.denominator = BigInteger.ONE;
 		this.simplify();
 	} // Fraction(int)
 
 	public Fraction(BigInteger num, BigInteger den) {
 		this.numerator = num;
 		this.denominator = den;
 		this.simplify();
 	} // Fraction(BigInteger,BigInteger)
 
 	public Fraction(BigInteger num) {
 		this.numerator = num;
 		this.denominator = BigInteger.ONE;
 		this.simplify();
 	} // Fraction(BigInteger)
 
 	public Fraction(long num, long den) {
 		this.numerator = BigInteger.valueOf(num);
 		this.denominator = BigInteger.valueOf(den);
 		this.simplify();
 	} // Fraction(long, long) 
 
 	public Fraction(long num) {
 		this.numerator = BigInteger.valueOf(num);
 		this.denominator = BigInteger.ONE;
 		this.simplify();
 	} // Fraction(long)
 
 	public Fraction(double num) {
 		int j = 0;
 		String doub = Double.toString(num);
 		boolean afterDot = false;
 
 		for (int i = 0; i < doub.length(); i++) {
 			if (doub.charAt(i) == '.') {
 				afterDot = true;
 			}
 
 			if (afterDot) {
 				j++;
 			}
 		}
 
 		this.numerator = BigInteger.valueOf((long) (num * Math.pow(10, j)));
 		this.denominator = BigInteger.valueOf((long) (Math.pow(10, j)));
 		this.simplify();
 	} // Fraction(double)
 
 	public Fraction(String fraction) {
 
 		if (!fraction.contains("/")) {
 			this.numerator = new BigInteger(fraction);
 			this.denominator = BigInteger.ONE;
 		} else {
 			int j = 0;
 			boolean afterSlash = false;
 
 			for (int i = 0; i < fraction.length(); i++) {
 				if (fraction.charAt(i) == '/') {
 					afterSlash = true;
 				} // if
 
 				if (afterSlash) {
 					j++;
 				} // if
 			} // for
 
 			this.numerator = new BigInteger(fraction.substring(0,
 					(fraction.length() - j)));
			this.denominator = new BigInteger(fraction.substring(j+1));
 			this.simplify();
 		} // if
 	} // Fraction(String)
 
 	/*
 	 * +----------------+ | Public methods | +----------------+
 	 */
 
 	/* Returns the numerator of the Fraction as a BigInt */
 	public BigInteger numerator() {
 		return this.numerator;
 	} // numerator()
 
 	/* Returns the denominator of the Fraction as a BigInt */
 	public BigInteger denominator() {
 		return this.denominator;
 	} // denominator()
 
 	/* Add a Fraction to another Fraction */
 	public Fraction add(Fraction other) {
 		BigInteger num = this.numerator().multiply(other.denominator())
 				.add(other.numerator().multiply(this.denominator()));
 		BigInteger den = this.denominator().multiply(other.denominator());
 		Fraction sum = new Fraction(num, den);
 		sum.simplify();
 		return sum;
 	} // add(Fraction)
 
 	/* Subtract a Fraction from another Fraction */
 	public Fraction subtract(Fraction other) {
 		BigInteger num = this.numerator().multiply(other.denominator())
 				.subtract(other.numerator().multiply(this.denominator()));
 		BigInteger den = this.denominator().multiply(other.denominator());
 		Fraction difference = new Fraction(num, den);
 		difference.simplify();
 		return difference;
 	} // subtract(Fraction)
 
 	/* Multiply a Fraction by another Fraction */
 	public Fraction multiply(Fraction other) {
 		BigInteger num = this.numerator().multiply(other.numerator());
 		BigInteger den = this.denominator().multiply(other.denominator());
 		Fraction product = new Fraction(num, den);
 		product.simplify();
 		return product;
 	} // multiply(Fraction)
 
 	/* Divide a Fraction by another Fraction */
 	public Fraction divide(Fraction other) {
 		if (other.numerator().intValue() != 0) {
 			BigInteger num = this.numerator().multiply(other.denominator());
 			BigInteger den = this.denominator().multiply(other.numerator());
 			Fraction quotient = new Fraction(num, den);
 			quotient.simplify();
 			return quotient;
 		} else {
 			throw new ArithmeticException("Division by 0");
 		}
 	} // divide(Fraction)
 
 	/* Raise a Fraction to an integer exponent */
 	public Fraction pow(int expt) {
 		Fraction result = this.clone();
 		for(int i=0; i<expt; i++){
 			result = result.multiply(this);
 		}
 		return result;
 	} // pow(int)
 
 	/* Return the reciprocal of a Fraction */
 	public Fraction reciprocal() {
 		BigInteger num = this.denominator();
 		BigInteger den = this.numerator();
 		Fraction recip = new Fraction(num, den);
 		recip.simplify();
 		return recip;
 	} // reciprocal()
 
 	/* Negate a Fraction */
 	public Fraction negate() {
 		Fraction neg = new Fraction(this.numerator().negate(),
 				this.denominator());
 		neg.simplify();
 		return neg;
 	} // negate()
 
 	/* Return a Fraction in double form */
 	public double doubleValue() {
 		return this.numerator().doubleValue()
 				/ this.denominator().doubleValue();
 	} // doubleValue()
 
 	/* Return a Fraction in BigDecimal form */
 	public BigDecimal bigDecimalValue() {
 		return new BigDecimal(this.numerator()).divide(new BigDecimal(this
 				.denominator()));
 	} // bigDecimalValue()
 
 	/* Return the fractional part of an improper fraction when represented
 	 * as a mixed number */
 	public Fraction fractionalPart() {
 		BigInteger num = this.numerator();
 		while (num.compareTo(this.denominator()) > 0) {
 			num.subtract(this.numerator());
 		}
 		Fraction frac = new Fraction(num, this.denominator());
 		frac.simplify();
 		return frac;
 	} // fractionalPart()
 
 	/* Return the whole number part of an improper fraction when represented
 	 * as a mixed number */
 	public BigInteger wholePart() {
 		Fraction whole = this.subtract(this.fractionalPart());
 		whole.simplify();
 		return whole.numerator();
 	} // wholePart()
 
 	/*
 	 * +------------------+ | Standard methods | +------------------+
 	 */
 	/* Creates a new identical Fraction*/
 	public Fraction clone() {
 		Fraction frac = new Fraction(this.numerator(), this.denominator());
 		return frac;
 	} // clone()
 
 	/* Converts a Fraction as a string of form "x/y" */
 	public String toString() {
 		if (denominator.intValue() == 1) {
 			return "" + this.numerator();
 		} else {
 			return (this.numerator + "/" + this.denominator);
 		}
 	} // toString()
 
 	/* Returns a relatively unique integer identifier */
 	public int hashCode() {
 		return numerator.hashCode() * denominator.hashCode();
 	} // hashCode()
 
 	/* Compares Fraction this to Fraction other. Return 1 if this is greater
 	 * than other, 0 if this is equal to other and -1 if is this is less than
 	 * other */
 	public int compareTo(Fraction other) {
 		Fraction f = this.subtract(other);
 		f.simplify();
 		return f.numerator().compareTo(BigInteger.ZERO);
 	} // compareTo(Fraction)
 
 	/* Compares a Fraction to an object. Returns true if the object is a 
 	 * Fraction of equal value; otherwise returns false */ 
 	public boolean equals(Object other) {
 		if (other instanceof Fraction) {
 			return this.equals((Fraction) other);
 		} else {
 			return false;
 		}
 	} // equals(Object)
 
 	/* Returns true if both Fractions have the same numerator and denominator;
 	 * otherwise return false */
 	public boolean equals(Fraction other) {
 		return this.numerator.equals(other.numerator)
 				&& this.denominator.equals(other.denominator);
 	} // equals(Fraction)
 
 	/*
 	 * +-----------------+ | Private methods | +-----------------+
 	 */
 
 	/* Simplifies a Fraction using its greatest common divisor and moves
 	 * the negative sign to the numerator if applicable */
 	private void simplify() {
 		// Find Greatest Common Divisor
 		BigInteger gcd = this.numerator().gcd(this.denominator());
 
 		// Simplify by dividing both parts by GCD
 		BigInteger num = this.numerator().divide(gcd);
 		BigInteger den = this.denominator().divide(gcd);
 
 		// Move negative sign to numerator
 		if (denominator().compareTo(BigInteger.ZERO) < 0) {
 			num = num.negate();
 			den = den.negate();
 		}
 
 		// Change values
 		this.numerator = num;
 		this.denominator = den;
 	} // simplify()
 } // Fraction
