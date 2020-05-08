 package com.oreilly.common.interaction.text.validator;
 
 import com.oreilly.common.interaction.text.InteractionPage;
 import com.oreilly.common.interaction.text.error.InterfaceDependencyError;
 import com.oreilly.common.interaction.text.error.ValidationFailedError;
 
 
 public class DoubleValidator extends Validator {
 	
 	@Override
 	protected Object validate( Object object, InteractionPage page ) throws ValidationFailedError,
 			InterfaceDependencyError {
 		if ( object instanceof Double )
 			return object;
 		if ( Double.class.isAssignableFrom( object.getClass() ) ) {
 			return object;
 		}
 		String asString = object.toString().toLowerCase();
 		boolean isPercentage = false;
 		if ( asString.charAt( asString.length() - 1 ) == '%' ) {
 			asString = asString.substring( 0, asString.length() - 1 );
 			isPercentage = true;
 		}
 		try {
 			Double parsed = Double.parseDouble( asString );
 			if ( isPercentage )
				parsed /= 100;
 			return parsed;
 		} catch ( NumberFormatException error ) {
 			throw new ValidationFailedError( this, "Unable to convert " + object.toString() + " to a number" );
 		}
 	}
 	
 }
