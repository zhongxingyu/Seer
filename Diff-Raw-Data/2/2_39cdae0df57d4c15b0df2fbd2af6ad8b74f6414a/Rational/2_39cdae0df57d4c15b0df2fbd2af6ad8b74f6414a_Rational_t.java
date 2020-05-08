 package com.aconex.tutorial.tests;
 
 import com.google.common.base.Preconditions;
 
 public class Rational {
 
     private final int numer;
     private final int denom;
 
     public Rational(int numer, int denom) {
         Preconditions.checkArgument(denom != 0, "Denominator cannot be zero");
         int gcd = greatestCommonDenominator(numer, denom);
         this.numer = numer/ gcd;
         this.denom = denom / gcd;
     }
 
     public Rational(int numer) {
         this(numer, 1);
     }
 
     public Rational add(Rational other) {
         return new Rational(numer * other.denom + other.numer * denom, denom * other.denom);
     }
 
     public Rational subtract(Rational other) {
        return new Rational(numer * other.denom - other.numer * denom, denom * other.denom);
     }
 
     public Rational multiply(Rational other) {
         return new Rational(numer * other.numer, denom * other.denom);
     }
 
     public Rational divide(Rational other) {
         return new Rational(numer * other.denom, denom * other.numer);
     }
 
     public boolean isEqualTo(Rational other) {
         return numer * other.denom == denom * other.numer;
     }
 
     public boolean less(Rational other) {
         return numer * other.denom < other.numer * denom;
     }
 
     public Rational max(Rational other) {
         return less(other) ? other : this;
     }
 
     private int greatestCommonDenominator(int x, int y) {
         return y == 0 ? x : greatestCommonDenominator(y, x % y);
     }
 
     @Override
     public String toString() {
         return numer + "/" + denom;
     }
 
 }
