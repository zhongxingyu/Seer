 
 
 
 package overwatch.db;
 
 import overwatch.util.Validator;
 
 
 
 
 
 /**
  * Sanity-check data for entry into the database.
  * 
  * @author  Lee Coakley
  * @author 	John Murphy
  * @version 2
  */
 
 
 
 
 
 public class DatabaseConstraints
 {
 	public static final int maxLengthName = 128;
 	
 	
 	
 	
 	
 	/**
 	 * Check if a name is valid.
 	 * Applies to names, logins, ranks, subjects, squadnames, etc
 	 * @param str
 	 * @return validity
 	 */
 	public static boolean isValidName( String str ) {
 		return Validator.isLengthRange( str, 1, maxLengthName );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if sex is valid (just one character)
 	 * @param str
 	 * @return validity
 	 */
 	public static boolean isValidSex( String str ) {
		return str.matches( "\\w" );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if salary is valid.
 	 * Format is: 1-9 digits . 1-2 digits 
 	 * @param str
 	 * @return
 	 */
 	public static boolean isValidSalary( String str ) {
 		return str.matches( "\\d{1,9}(\\.\\d{1,2})?" );
 	}
 	
 
 	
 	
 	
 	/**
 	 * Check if a rank exists with the given name.
 	 * Note: Ranks are case sensitive and start with a capital letter.
 	 * @param name
 	 * @return boolean
 	 */
 	public static boolean rankExists( String name )
 	{
 		EnhancedPreparedStatement eps = new EnhancedPreparedStatement(
 			"select rankNo         " +
 			"from Ranks            " +
 			"where name = <<name>> " +
 			"limit 1;"
 		);
 		
 		try {
 					 eps.set( "name", name );
 			return ! eps.query().isEmpty();
 		}
 		finally {
 			eps.close();
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Checks if a person exists.
 	 * @param name
 	 * @return boolean
 	 */
 	public static boolean personExists( String loginName )
 	{
 		EnhancedPreparedStatement eps = new EnhancedPreparedStatement(
 			"select personNo         " +
 			"from Personnel          " +
 			"where loginName = <<n>> " +
 			"limit 1;"
 		);
 		
 		try {
 					 eps.set( "n", loginName );
 			return ! eps.query().isEmpty();
 		}
 		finally {
 			eps.close();
 		}
 	}
 	
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
