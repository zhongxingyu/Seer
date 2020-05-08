 package ee.itcollege.borderproject.util;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class SetupDb {
 	
 	private static final String SQL_COMMENT = "--";
 	private static final String DATABASE_CHECK = "SELECT count(*) FROM information_schema.system_tables WHERE table_name = 'AMET';";
 	private static final String SQL_BUILD_SCRIPT = "D:/Java Workspace/Projekt_11/ProjektTest/WebContent/WEB-INF/resources/Piirivalve.script";
 		// Kalmer: 	"D:/Java Workspace/Projekt_11/ProjektTest/WebContent/WEB-INF/resources/Piirivalve.script";
 		// Martin:	C:/Code/Java/workspace_team11_project/ProjektTest/WebContent/WEB-INF/resources/Piirivalve.script
 	private static final String CONNECTION_URL = "jdbc:hsqldb:file:Team11BorderDb";
 	private static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";
 	
 	@PostConstruct
 	public void createTables() throws SQLException, ClassNotFoundException, IOException {
 		Connection connection = null;
 		
 		try {
 			connection = createConnection();
 			if( isDatabaseEmpty( connection )){
 				List<String> statements = statementList( SQL_BUILD_SCRIPT );
 				executeSqlStatements( statements );
 			}
 		}
 		finally {
 			if( connection != null ){
 				connection.close();
 			}
 		}
 	}
 	
 	public static String getScriptLocation()  {
 		try {
 			System.out.println(new java.io.File(".").getCanonicalPath());
 			return new java.io.File("/WEB-INF/resources/Piiripunkt.script").getCanonicalPath();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	public Connection createConnection() throws SQLException, ClassNotFoundException {
 		Class.forName( DRIVER_CLASS );
 		return DriverManager.getConnection( CONNECTION_URL );
 	}
 	
 	private boolean isDatabaseEmpty(Connection conn) throws SQLException {
 		Statement statement = conn.createStatement();
 		ResultSet result = statement.executeQuery( DATABASE_CHECK );
 		result.next();
 		int tableCount = result.getInt(1);
 		return tableCount == 0;
 	}
 	
 	private List<String> statementList(String fileName) throws IOException {
 		List<String> collectedStatements = new ArrayList<String>();
 		
 		BufferedReader reader = new BufferedReader(new FileReader(fileName));
 
 		try {
 			String statementLine ;
 			while ((statementLine = reader.readLine()) != null) {
 				if(statementLine.trim().length() > 1 && !statementLine.contains( SQL_COMMENT ))
 					collectedStatements.add( statementLine );
 			}
 		} finally {
 			reader.close();
 		}
 		
 		return collectedStatements;
 	}
 	
 	private void executeSqlStatements(List<String> statements) throws ClassNotFoundException, SQLException {
 		Statement statement = createConnection().createStatement();
 		
 		for(String line : statements ){
 			System.out.println( line );
 			statement.execute( line );
 		}
 	}
 	
 }
