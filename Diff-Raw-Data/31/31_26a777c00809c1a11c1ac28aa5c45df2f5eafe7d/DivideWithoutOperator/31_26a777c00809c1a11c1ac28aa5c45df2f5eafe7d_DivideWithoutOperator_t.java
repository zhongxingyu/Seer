 /**
  * 
  */
 package com.beepscore.DivideWithoutOperator;
 
 /**
  * @author stevebaker
  *
  */
 public class DivideWithoutOperator {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}

 	// we can overload this method, changing the signature with different parameter lists
 	// http://download.oracle.com/javase/tutorial/java/javaOO/methods.html
 	public static long divideWithoutOperator(long dividend, long divisor)
	{	
		// can't divide by zero
		if (0 == divisor)
		{
			// throw error !!!!!!!!!!!!!!!!!!
			return Long.MAX_VALUE;
		}

		long increment;
		if ( -1 == (Math.signum(divisor) * Math.signum(dividend)) )
		{
			// quotient will be negative
			increment = -1;
		} else {
			// quotient will be zero or positive
			increment = 1;
		}
		long quotient;
		for ( quotient = 0; (Math.abs(quotient) < Math.abs(dividend)); quotient += increment)
		{
			if ( Math.abs(quotient*divisor) >= Math.abs(dividend) )
			{
				return quotient;
			}
		}
		// default fall through if loop condition doesn't work
 		return quotient;
 	}
 
 }
