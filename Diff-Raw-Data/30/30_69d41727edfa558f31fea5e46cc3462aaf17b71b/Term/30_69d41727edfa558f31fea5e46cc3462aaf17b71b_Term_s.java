 package com.swtanalytics.math;
 
 public class Term implements Comparable<Term> {
 
	protected static final Fraction ZERO = new Fraction(0, 1);

 	protected Fraction coefficient;
 	protected Fraction exponent;
 		
 	public Term (Fraction c, Fraction e) {
 		this.coefficient = c;
 		this.exponent = e;
 	}
 
     public String prettyPrint(boolean isFirstTerm) {
             // Not smart about string construction. Just dumb manipulation
 
            String result = this.toString();
            if (isFirstTerm) {
            	// Strip a positive sign from the first term
            	if (ZERO.compareTo(coefficient) > 0) {
                	result = result.substring(1, result.length());            		
            	}
            }
            else {
                 result = result.substring(0,1) + ' '
                          + result.substring(1, result.length());
             }
             
             return result;
     }
 	
	public String toString() {
         String exp = this.exponent.formatString(true);
		return String.format("%sx^%s", this.coefficient, exp);
 	}
 
     public int compareTo(Term t) {
         return this.exponent.compareTo(t.exponent);
     }
 }
 
