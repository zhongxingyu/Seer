 package internalformatting;
 
 import java.io.Serializable;
 
 /** Term is a class to represent a term consisting of a coefficient, variable, and exponent
  * @author Michelle Len
  * @version 02/18/2012 for CS 48 Project, W12
  */
 
 public class Term implements Serializable{
 	
	private double coefficient; // a coefficient represented as an int
 	private Variable x; // a variable object
 	private int exponent; // an exponent represented as an int
 	private Unit unit;
 	final private static long serialVersionUID = 5752165L;
 
 	/** 
 	 * Default Constructor with nulls and zeroes
 	 */
 	
 	public Term () {
		this.coefficient = 0.0;
 		this.x = null;
 		this.exponent = 0;
 		this.unit = null;
 	}
 	
 	/** Constructor
 	 * 
	 * @param coefficient A double value of coefficient
 	 * @param x What the variable is
 	 * @param exponent An int value of exponent
 	 * @param unit A Unit object associated with the term, see Unit class
 	 */
 	
	public Term (double coefficient, Variable x, int exponent, Unit unit) {
 		// assign attributes from parameters
 		this.coefficient = coefficient;
 		this.x = x;
 		this.exponent = exponent;
 		this.unit = unit;
 	}
 	
 	/**
 	 * Get methods for coefficient, variable, exponent, and term
 	 */
 	
	public double getCoefficient() { return this.coefficient; }
 	public Variable getVariable() { return this.x; }
 	public int getExponent() { return this.exponent; }
 	public Unit getUnit() { return this.unit; }
 	
 	/** 
 	 * Set methods for coefficient, variable, exponent, and term
 	 */
 	
	public void setCoefficient (double coefficient) { this.coefficient = coefficient; }
 	public void setVariable (Variable x) { this.x = x; }
 	public void setExponent (int exponent) { this.exponent = exponent; }
 	public void setUnit (Unit unit) { this.unit = unit; }
 	
 	/**
 	 * Returns object as a string
 	 */
 	
 	public String toString() {
		if (this.coefficient == 1 && this.exponent == 1)
			return "" + x;
		else if (this.coefficient == 1)
			return "" + x + "^" + exponent;
		else if (this.exponent == 1)
			return "" + coefficient + x;
		else 
			return "" + coefficient + x + "^" + exponent;
 	} // Do we need a toString method?
 	
 } // class Term
