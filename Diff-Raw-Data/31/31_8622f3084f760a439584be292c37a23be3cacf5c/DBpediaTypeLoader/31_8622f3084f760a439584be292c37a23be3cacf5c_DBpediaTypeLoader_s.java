 package dbpedia.utils;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DBpediaTypeLoader {
 
 	public static void load(Connection dbConnection, String typesTableName,
 			String textFile) throws ForbidenTableNameException, SQLException, IOException {
 		
 		 createTableIfNotExists(dbConnection,typesTableName);
 		 FileInputStream fstream = new FileInputStream(textFile);
 		  // Get the object of DataInputStream
 		  DataInputStream in = new DataInputStream(fstream);
 		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		  String strLine;
		  int count = 0;
 		  //Read File Line By Line
 		  while ((strLine = br.readLine()) != null)   {
 		  // Print the content on the console
 		  saveLine(strLine, dbConnection, typesTableName);
		  if(count % 1000 == 0){
 			  System.out.print(".");
		  }if(count % 10000 == 0){
 			  System.out.print(" "+count);
 			  System.out.println("");
 		  }
 		  count++;
 		  }
 		  //Close the input stream
 		  in.close();
 		  
 		
 	}
 
 	private static void createTableIfNotExists(Connection dbConnection,
 			String typesTableName) throws ForbidenTableNameException, SQLException {
 		
 		if(typesTableName.equalsIgnoreCase("category") || typesTableName.equalsIgnoreCase("page") || 
 				typesTableName.equalsIgnoreCase("pagelinks") || typesTableName.equalsIgnoreCase("categorylinks")){
 			throw new ForbidenTableNameException();
 		}else{
 			String query = "CREATE TABLE IF NOT EXISTS `"+typesTableName+"`" +
 					" (`resource` BLOB NOT NULL ," +
 					"`type` BLOB NOT NULL ," +
 					"`id` INT NOT NULL AUTO_INCREMENT ," +
 					" PRIMARY KEY (`id`) )";
 			dbConnection.createStatement().executeUpdate(query);
 		}
 			
 		
 	}
 
 	private static void saveLine(String strLine, Connection dbConnection,
 			String typesTableName) {
 		//"<http://dbpedia.org/resource/Autism> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Disease> ."
 		String[] subStrings =strLine.split(" ");
 		if(subStrings.length>=3 && !subStrings[0].equals("#")){
 			String subject = subStrings[0];
 			String pred = subStrings[1];
 			String object = subStrings[2];
 			
 			subject = subject.substring(1);
 			subject = subject.substring(0, subject.length()-1);
 			subject = subject.substring(28);
 			
 			if(pred.endsWith("rdf-syntax-ns#type>")){
 				try {
 					PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO "+typesTableName+" (`resource`, `type`) VALUES(?,?)");
 					statement.setString(1, subject);
 					statement.setString(2, object);
 					statement.executeUpdate();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 			
 		}
 		
 		
 	}
 
 	/**
 	 * Returns a list with all the types of the resource.
 	 * @param resource
 	 * @return List of types
 	 * @throws SQLException 
 	 */
 	public static List<String> getTypes(String resource, Connection connection, String tableName){
 		List<String> results = new ArrayList<String>();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement("SELECT distinct `type` from `"+tableName+"` where `resource` = ?");
 		statement.setString(1, resource);
 		ResultSet resultSet = statement.executeQuery();
 		while(resultSet.next()){
 			results.add(resultSet.getString("type"));
 		}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return results;
 		
 	}
 
 }
