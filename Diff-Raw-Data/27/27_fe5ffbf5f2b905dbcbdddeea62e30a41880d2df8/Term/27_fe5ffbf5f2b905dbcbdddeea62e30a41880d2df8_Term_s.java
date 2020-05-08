 package internalformatting;
 
 import java.io.Serializable;
 
 /** Term is a class to represent a term consisting of a coefficient, variable, and exponent
  * @author Michelle Len
  * @version 02/18/2012 for CS 48 Project, W12
  */
 
 public class Term implements Serializable{
 	
	private int coefficient; // a coefficient represented as an int
 	private Variable x; // a variable object
 	private int exponent; // an exponent represented as an int
 	private Unit unit;
 	final private static long serialVersionUID = 5752165L;
 
 	/** 
 	 * Default Constructor with nulls and zeroes
 	 */
 	
 	public Term () {
		this.coefficient = 0;
 		this.x = null;
 		this.exponent = 0;
 		this.unit = null;
 	}
 	
 	/** Constructor
 	 * 
	 * @param coefficient An int value of coefficient
 	 * @param x What the variable is
 	 * @param exponent An int value of exponent
 	 * @param unit A Unit object associated with the term, see Unit class
 	 */
 	
	public Term (int coefficient, Variable x, int exponent, Unit unit) {
 		// assign attributes from parameters
 		this.coefficient = coefficient;
 		this.x = x;
 		this.exponent = exponent;
 		this.unit = unit;
 	}
 	
 	/**
 	 * Get methods for coefficient, variable, exponent, and term
 	 */
 	
	public int getCoefficient() { return this.coefficient; }
 	public Variable getVariable() { return this.x; }
 	public int getExponent() { return this.exponent; }
 	public Unit getUnit() { return this.unit; }
 	
 	/** 
 	 * Set methods for coefficient, variable, exponent, and term
 	 */
 	
	public void setCoefficient (int coefficient) { this.coefficient = coefficient; }
 	public void setVariable (Variable x) { this.x = x; }
 	public void setExponent (int exponent) { this.exponent = exponent; }
 	public void setUnit (Unit unit) { this.unit = unit; }
 	
 	/**
 	 * Returns object as a string
 	 */
 	
 	public String toString() {
		return "" + coefficient + x + "^" + exponent;
 	} // Do we need a toString method?
 	
 } // class Term
