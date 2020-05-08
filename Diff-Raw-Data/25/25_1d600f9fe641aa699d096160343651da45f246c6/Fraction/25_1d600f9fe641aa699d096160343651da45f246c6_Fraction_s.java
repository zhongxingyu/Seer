 package com.swtanalytics.math;
 
 public class Fraction implements Comparable<Fraction> {
     protected int numerator;
     protected int denominator;
 
     public Fraction(int n, int d) {
         if (d == 0) {
             throw new IllegalArgumentException("Argument 'd' is 0");
         }
 
         // Short circuit gcf if we have a 0 numerator
         if (n != 0) {
             int g = gcf(n, d);
             this.numerator = n / g;
             this.denominator = d / g;
         } else {
             this.numerator = n;
             this.denominator = d;
         }
 
         // Fix the sign of the numerator and denominator if the gcf is
         // negative. We may only need to check and fix the denominator
         // sign here...
         if (this.numerator > 0 && this.denominator < 0) {
             this.numerator *= -1;
             this.denominator *= -1;
         }
     }
 
     public String toString() {
         int n = this.numerator;
         int d = Math.abs(this.denominator);
         if (this.denominator < 0) {
             n *= -1;
         }
 
         String result = String.format("%+d", n);
 
         // Collapse into integer
         if (d != 1) {
             result = String.format("%s/%d", result, d);
         }
 
         return result;
     }
 
     public String formatString(boolean stripSignIfZeroOrGreater, boolean spaceAfterCoefficientSign) {
         int n = this.numerator;
         int d = Math.abs(this.denominator);
 
         boolean isZeroOrGreater = this.numerator * this.denominator >= 0;
         String sign = isZeroOrGreater
                 ? "+"
                 : "-";
 
         String numeratorPart;
         if (isZeroOrGreater && stripSignIfZeroOrGreater) {
             numeratorPart = String.format("%d", Math.abs(n));
         } else if (spaceAfterCoefficientSign) {
             numeratorPart = String.format("%s %d", sign, Math.abs(n));
         } else {
             numeratorPart = String.format("%s%d", sign, Math.abs(n));
         }
 
         // Collapse into integer
         if (d != 1) {
             return String.format("%s/%d", numeratorPart, d);
         }
 
         return numeratorPart;
     }
 
     public double doubleValue() {
         return new Double(this.numerator) / this.denominator;
     }
 
     public int compareTo(Fraction f) {
        return new Double(f.doubleValue()).compareTo(this.doubleValue());
     }
 
     public Fraction add(Fraction toAdd) {
         int n = this.numerator * toAdd.denominator + toAdd.numerator * this.denominator;
         int d = this.denominator * toAdd.denominator;
         return new Fraction(n, d);
     }
 
     public Fraction subtract(Fraction f) {
         int n = this.numerator * f.denominator - f.numerator * this.denominator;
         int d = this.denominator * f.denominator;
         return new Fraction(n, d);
     }
 
     public Fraction multiply(Fraction f) {
         return new Fraction(this.numerator * f.numerator, this.denominator * f.denominator);
     }
 
     private int gcf(int a, int b) {
         // TODO: In theory, gcf behavior should be undefined when one of the operands is 0.
         // Should this throw an error?
         int rem = 0;
         int gcf = 0;
         do {
             rem = a % b;
             if (rem == 0)
                 gcf = b;
             else {
                 a = b;
                 b = rem;
             }
         } while (rem != 0);
 
         return gcf;
     }
 
     @Override
     public boolean equals(Object o) {
         return compareTo((Fraction) o) == 0;
     }
 
     @Override
     public int hashCode() {
         return new Double(doubleValue()).hashCode();
     }
 }
