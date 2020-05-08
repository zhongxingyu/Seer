 
 
 
 package overwatch.db;
 
 import java.math.BigInteger;
 import java.sql.Connection;
 import overwatch.util.Util;
 
 
 
 
 
 /**
  * Functions which are applicable to multiple types of data.
  * 
  * @author  Lee Coakley
  * @version 2
  */
 
 
 
 
 
 public class Common
 {
 	
 	/**
 	 * Get 8 random capital letters inside [brackets].
 	 * @return String
 	 */
 	public static String randomNamePart() {
 		return "[" + Util.randomAlphaString( 8 ) + "]";
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Unique creator for ranks, personnel etc
 	 * @param table Table to apply the insert to
 	 * @param values Values you would put in the SQL insert values() part.  The string "<?>" will be replaced with a random unique string.
 	 * @throws DatabaseException Thrown if it fails.
 	 */
 	public static void createWithUnique( String table, String...values )
 	{
 		DatabaseException lastEx = null;
 		int maxRetries = 5;
 		
 		String pre  = "insert into " + table + " ";
 		String vals = "values(" + Util.concatWithCommas(values) + ");";
 		String cmd  = pre + vals;
 		
 		for (int r=0; r<maxRetries; r++) {
 			try {
 				Database.update(  cmd.replaceAll("\\<\\?\\>",randomNamePart())  );
 				return;
 			}
 			catch (DatabaseException ex) {
 				ex.printStackTrace(); // TODO remove debug statement
 				lastEx = ex;
 			}
 		}
 		
 		throw new DatabaseException( "createWithUnique failed after " + maxRetries + " attempts.", lastEx );		
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Unique creator for ranks, personnel etc
 	 * WARNING: This locks the database table.
 	 * @param table Table to insert into
 	 * @param select What to select after inserting.  For auto_increment columns use "LAST_INSERT_ID()".
 	 * @param values Values you would put in the SQL insert values() part.  The string "<?>" will be replaced with a random unique string.
 	 * @throws DatabaseException Thrown if it fails.
 	 * @return EnhancedResultSet
 	 */
 	public static EnhancedResultSet createWithUniqueLockingSelect( String table, String select, String...values )
 	{  
 	    Connection conn = Database.getConnection();
 	    
 	    DatabaseException lastEx = null;
 	    int maxRetries = 3;
 	  	
 	  	String pre  = "insert into " + table + " ";
 	  	String vals = "values(" + Util.concatWithCommas(values) + ");";
 	  	String cmd  = pre + vals;
 	    
 	    try {
	    	Database.lock( conn, table, "WRITE" );
 	  		
 	  		for (int r=1; r<=maxRetries; r++) {
 	  			boolean lastAttempt = (r == maxRetries);
 	  			
   				try {
   					Database.update( conn, cmd.replaceAll("\\<\\?\\>",randomNamePart()) );
   					break;
   				}
   				catch (DatabaseException ex) {
   					ex.printStackTrace(); // TODO remove debug statement
   					lastEx = ex;
   				}
 
 	  			if (lastAttempt) {
 	  				throw new DatabaseException( 
 	  					"createWithUniqueLockingReturn failed after " + maxRetries + " attempts.",
 	  					lastEx
 	  				);
 	  			}
 	  		}
 	  		
 	  		EnhancedResultSet ers = Database.query( conn,
 		  	    "select " + select +
 		  	    "from   " + table  + ";"
 		    );
 	  		
 	  		return ers;
 	    }
 	    finally {
 	    	try     { Database.unlock( conn );           }
 	    	finally { Database.returnConnection( conn ); }
 	    }
 	} 
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static void main( String[] args ) {
 		for (int i=0; i++<64;)
 			System.out.println( randomNamePart() );
 	}
 }
