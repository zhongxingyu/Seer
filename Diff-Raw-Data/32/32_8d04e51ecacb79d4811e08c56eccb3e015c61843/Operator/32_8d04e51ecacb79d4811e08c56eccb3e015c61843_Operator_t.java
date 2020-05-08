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
	 * @param string
	 *            String representation of the operator
 	 */
 	private Operator ( String string ) {
 		this.string = string;
 	}
 	
 	/**
	 * Applies this operator to the given operands
 	 * 
 	 * @param a
 	 *            First operand
 	 * @param b
	 *            Second operand
 	 * @return Result of the operation
 	 */
 	public abstract double apply ( double a, double b );
 	
	/**
	 * Applies this operator to the given operands.
	 * <p>
	 * This version of this method takes an <tt>int</tt> and a double and returns an <tt>int</tt> to accommodate to
	 * specific use cases that turn out to be common. Note that this method just performs a call to
	 * {@link #apply(double, double)} and rounds the result.
	 * 
	 * @param a
	 *            First operand
	 * @param b
	 *            Second operand
	 * @return Result of the operation
	 */
	public int apply ( int a, double b ) {
		return (int) Math.round( apply( (double) a, b ) );
 	}
 	
 	/**
	 * @param str
	 *            String representation of the operator
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
