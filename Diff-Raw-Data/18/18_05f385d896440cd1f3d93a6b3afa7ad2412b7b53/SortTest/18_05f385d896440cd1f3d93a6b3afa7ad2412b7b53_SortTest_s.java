 package com.swtanalytics.math;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.junit.Assert;
 
 @RunWith(JUnit4.class)
 public class SortTest {
 
     Term[] TERMS = {
        new Term(1,1),
        new Term(1,2),
        new Term(1,3),
        new Term(1,4),
        new Term(1,5),
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
        int lastExp = 0;
         boolean firstTerm = true;
         for (Term t : mf.terms) {
             if (firstTerm) {
                 firstTerm = false;
             } else {
               Assert.assertTrue(BAD_SORT_MSG, lastExp >= t.exponent);
             }
             lastExp = t.exponent;
         }
     }
 }
