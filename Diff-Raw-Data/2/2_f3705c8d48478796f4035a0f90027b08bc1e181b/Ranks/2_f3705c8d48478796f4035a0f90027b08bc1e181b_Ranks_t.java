 
 
 
 package overwatch.db;
 
 
 
 
 
 /**
  * Database <-> Rank interactions
  * 
  * @author  Lee Coakley
  * @author  John Murphy
  * @version 2
  */
 
 
 
 
 
 public class Ranks
 {
 	
 	/**
 	 * Creates a new Rank
 	 * Locks the table.
 	 * @return rankNo
 	 */
 	public static Integer create()
 	{	
 		return Common.createWithUniqueLockingAutoInc(
 			"Ranks",
 			"DEFAULT",
 			"'new Rank <?>'",
 			"0"
 		);	
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if a rank exists.
 	 * @param rankNo
 	 * @return exists
 	 */
 	public static boolean exists( Integer rankNo )
 	{
 		EnhancedResultSet ers = Database.query(
 			"select rankNo  " +
 		    "from Ranks     " +
 		    "where rankNo = " + rankNo + ";"
 		);
 		
 		return ! ers.isEmpty();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Save a rank
 	 * @param rankNo
 	 * @param rankName
 	 * @param rankLevel
 	 */
 	public static boolean save( Integer rankNo, String rankName, String rankLevel )
 	{
 		EnhancedPreparedStatement eps = new EnhancedPreparedStatement(
 		  	"update Ranks                       " +
 		  	"set name           = <<name>>,     " +
 		  	"	 privilegeLevel = <<level>>     " +
		  	"where rankNo = <<num>>;            "
 		);
 		
 		try {
 			eps.set( "num",   rankNo    );
 			eps.set( "name",  rankName  );
 			eps.set( "level", rankLevel );
 			return (0 != eps.update());
 		}
 		finally {
 			eps.close();
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Delete a rank
 	 * @param rankNo
 	 */
 	public static void delete( Integer rankNo )
 	{
 		Database.update(
 			"DELETE         " +
 			"FROM Ranks     " +
 			"WHERE rankNo = " + rankNo + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if a rank is one of the four hardcoded into the database.
 	 * These can't be edited.
 	 * @param rankNo
 	 * @return boolean
 	 */
 	public static boolean isHardcoded( Integer rankNo ) {
 		return (rankNo <= 4);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get a rank's name.
 	 * Returns null if no such rank exists.
 	 * @param rankNo
 	 * @return name or null
 	 */
 	public static String getName( Integer rankNo )
 	{
 		EnhancedResultSet ers = Database.query(
 			"select name    " +
 		    "from Ranks     " +
 		    "where rankNo = " + rankNo + ";"
 		);
 		
 		if (ers.isEmpty())
 			 return null;
 		else return ers.getElemAs( "name", String.class ); 
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get a person's number based on their name.
 	 * Returns null if no such rank exists.
 	 * @param name
 	 * @return rankNo or null
 	 */
 	public static Integer getNumber( String name )
 	{
 		EnhancedPreparedStatement eps = new EnhancedPreparedStatement(
 			"select rankNo          " +
 		    "from Ranks             " +
 		    "where name = <<name>>; "
 		);
 		
 		try {
 			eps.set( "name",  name );
 			EnhancedResultSet ers = eps.query();
 			
 			if (ers.isEmpty())
 				 return null;
 			else return ers.getElemAs( "rankNo", Integer.class );
 		}
 		finally {
 			eps.close();
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get privLevel for a rank, or null of it doesn't exist.
 	 * @param rankNo
 	 * @return
 	 */
 	public static Integer getLevel( Integer rankNo )
 	{
 		return Database.querySingle( Integer.class,
 			"select privilegeLevel " +
 		    "from Ranks            " +
 		    "where rankNo =        " + rankNo + ";"
 		); 
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
