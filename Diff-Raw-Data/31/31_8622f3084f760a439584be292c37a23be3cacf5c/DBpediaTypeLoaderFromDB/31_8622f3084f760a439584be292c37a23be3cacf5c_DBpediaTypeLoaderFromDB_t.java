 package executions;
 
 import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
		if(!(args.length==5)){
			System.out.println("You need the following params:");
			System.out.println("<typesTableName> <ttl text file> <database> <user> <pass>");
 			return;
 		}
 		
 		String typesTableName = args[0];
 		String textFile = args[1];
		String database = args[2];
		String user = args[3];
		String pass = args[4];
		Connection con = DriverManager.getConnection("jdbc:mysql://"+database+"?user="+user+"&password="+pass+"&characterEncoding=utf8");
		DBpediaTypeLoader.load(con, typesTableName, textFile);
		System.out.println("Completed!");
 		System.exit(0);
 	}
 
 }
