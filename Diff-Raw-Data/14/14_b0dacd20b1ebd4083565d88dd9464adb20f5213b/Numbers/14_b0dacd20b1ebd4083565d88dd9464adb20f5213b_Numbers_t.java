 package com.oreilly.mmogroup.bukkitTools.io;
 
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;


 public class Numbers {
 	
 	public static String doubleAsPercentage( double value ) {
 		return doubleAsPercentage( value, 0 );
 	}
 	
 	
 	public static String doubleAsPercentage( double value, int decimalCount ) {
		DecimalFormat f = new DecimalFormat( "#,##0." + StringUtils.repeat( "0", decimalCount ) + "%" );
		return f.format( value );
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
