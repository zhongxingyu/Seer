 package com.swtanalytics.math;
 
 public class Term implements Comparable<Term> {
 
 	protected Fraction coefficient;
 	protected Fraction exponent;
 		
 	public Term (Fraction c, Fraction e) {
 		this.coefficient = c;
 		this.exponent = e;
 	}
 
     public String prettyPrint(boolean isFirstTerm) {
             // Not smart about string construction. Just dumb manipulation
    	    // TODO: consider short-circuiting printing of this term entirely when it is 0
    	    // ex: 0/48x^74/8 = 0 so maybe don't print it?
 
    	    // If first term, strip the positive
    		boolean stripPositiveFromCoefficient = isFirstTerm;
            String result = this.formatString(stripPositiveFromCoefficient);
            if (!isFirstTerm){
                 result = result.substring(0,1) + ' '
                          + result.substring(1, result.length());
             }
             
             return result;
     }
 	
    protected String formatString(boolean stripPositiveFromCoefficient) {
		String coef = this.coefficient.formatString(stripPositiveFromCoefficient);
         String exp = this.exponent.formatString(true);
		return String.format("%sx^%s", coef, exp);
    }

    public String toString() {
    	// Calling this will not strip the sign from a positive coefficient
    	return formatString(false);
 	}
 
     public int compareTo(Term t) {
         return this.exponent.compareTo(t.exponent);
     }
 }
 
