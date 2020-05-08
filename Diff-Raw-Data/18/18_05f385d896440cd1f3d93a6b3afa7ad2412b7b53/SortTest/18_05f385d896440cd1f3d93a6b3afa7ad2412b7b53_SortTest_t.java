 package com.swtanalytics.math;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.junit.Assert;
 
 @RunWith(JUnit4.class)
 public class SortTest {
 
     Term[] TERMS = {
        new Term(new Fraction(1,1), new Fraction(1,1)),
        new Term(new Fraction(1,1), new Fraction(2,1)),
        new Term(new Fraction(1,1), new Fraction(3,1)),
        new Term(new Fraction(1,1), new Fraction(4,1)),
        new Term(new Fraction(1,1), new Fraction(5,1)),
        new Term(new Fraction(1,1), new Fraction(6,1)),
     };
 
     static final String BAD_SORT_MSG = "Terms sorted incorrectly by MathFucntion";
 
     @Test
     public void testSort() {
 
         // Create a new math function and add the test terms to it in order
         MathFunction mf = new MathFunction();
         for (Term t : TERMS) {
             mf.addTerm(t);
         }
 
         // Programatically run through the MathFunction and check to see
         // that each exponent is equal to or smaller than the last.
        Fraction lastExp = null;
         boolean firstTerm = true;
         for (Term t : mf.terms) {
             if (firstTerm) {
                 firstTerm = false;
             } else {
               Assert.assertTrue(BAD_SORT_MSG,
                                 lastExp.doubleValue() >= t.exponent.doubleValue());
             }
             lastExp = t.exponent;
         }
     }
 }
