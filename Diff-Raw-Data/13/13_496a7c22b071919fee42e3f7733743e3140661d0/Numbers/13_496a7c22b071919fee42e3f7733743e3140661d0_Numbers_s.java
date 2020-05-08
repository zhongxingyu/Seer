 package com.oreilly.common.io;
 
 public class Numbers {
 	
 	public static String doubleAsPercentage( double value ) {
 		return doubleAsPercentage( value, 0 );
 	}
 	
 	
 	public static String doubleAsPercentage( double value, int decimalCount ) {
		String beforeDecimalPoint = Integer.toString( (int)( value * 100 ) );
		String decimalOnwards = "";
		if ( decimalCount > 0 )
			decimalOnwards = "." +
					Integer.toString( (int)( ( value - Math.floor( value ) ) * Math.pow( 10, decimalCount ) ) );
		return beforeDecimalPoint + decimalOnwards + "%";
 	}
 	
 	
 	public static Double doubleFromString( String data ) {
 		// if the string ends with "%", we go with a percentage..
 		data = data.trim();
 		boolean isDouble = data.endsWith( "%" );
 		if ( isDouble )
 			data = data.replace( "%", "" );
 		try {
 			Double value = Double.parseDouble( data );
 			if ( isDouble )
 				value /= 100;
 			return value;
 		} catch ( NumberFormatException error ) {
 			return null;
 		}
 	}
 }
