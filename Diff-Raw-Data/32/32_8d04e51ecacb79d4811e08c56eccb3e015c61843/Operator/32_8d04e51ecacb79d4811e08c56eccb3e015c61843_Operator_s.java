 package simumatch.common;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /** An operator used to calculate bonuses in <tt>Effect</tt>s */
 public enum Operator {
 	/** Arithmetic addition */
 	ADDITION( "+" ) {
 		
 		@Override
 		public double apply ( double a, double b ) {
 			return a + b;
 		}
 		
 	},
 	
 	/** Arithmetic subtraction */
 	SUBTRACTION( "-" ) {
 		
 		@Override
 		public double apply ( double a, double b ) {
 			return a - b;
 		}
 	},
 	
 	/** Arithmetic product */
 	PRODUCT( "*" ) {
 		
 		@Override
 		public double apply ( double a, double b ) {
 			return a * b;
 		}
 	};
 	
 	private static final Map<String,Operator> OPERATORS;
 	static {
 		Map<String,Operator> ops = new HashMap<String,Operator>();
 		for ( Operator op : Operator.values() ) {
 			ops.put( op.string, op );
 		}
 		
 		OPERATORS = Collections.unmodifiableMap( ops );
 	}
 	
 	/** String representation of the operator */
 	private final String string;
 	
 	/**
	 * @param string String representation of the operator
 	 */
 	private Operator ( String string ) {
 		this.string = string;
 	}
 	
 	/**
	 * Applies the operator to the given operands
 	 * 
 	 * @param a
 	 *            First operand
 	 * @param b
	 *            Second operant
 	 * @return Result of the operation
 	 */
 	public abstract double apply ( double a, double b );
 	
	public int apply ( int a, double b){
		//added to eliminate Castings
		return Math.round((float)apply((double)a, b));
 	}
 	
 	/**
	 * @param str String represetation of the operator
 	 * @return The corresponding operator
 	 */
 	public static Operator get ( String str ) {
 		return OPERATORS.get( str );
 	}
 	
 	@Override
 	public String toString () {
 		return string;
 	}
 }
