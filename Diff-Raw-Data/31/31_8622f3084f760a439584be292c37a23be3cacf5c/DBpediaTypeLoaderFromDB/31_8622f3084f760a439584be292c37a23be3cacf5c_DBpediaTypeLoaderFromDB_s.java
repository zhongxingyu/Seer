 package executions;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import db.WikipediaConnector;
 import dbpedia.utils.DBpediaTypeLoader;
 import dbpedia.utils.ForbidenTableNameException;
 
 public class DBpediaTypeLoaderFromDB {
 
 	/**
 	 * @param args
 	 * @throws ClassNotFoundException 
 	 * @throws IOException 
 	 * @throws SQLException 
 	 * @throws ForbidenTableNameException 
 	 */
 	public static void main(String[] args) throws ForbidenTableNameException, SQLException, IOException, ClassNotFoundException {
		if(!(args.length==2)){
			System.out.println("You need two params:");
			System.out.println("<typesTableName> <ttl text file>");
 			return;
 		}
 		
 		String typesTableName = args[0];
 		String textFile = args[1];
		DBpediaTypeLoader.load(WikipediaConnector.getResultsConnection(), typesTableName, textFile);
		System.out.println("Compleated!");
 		System.exit(0);
 	}
 
 }
