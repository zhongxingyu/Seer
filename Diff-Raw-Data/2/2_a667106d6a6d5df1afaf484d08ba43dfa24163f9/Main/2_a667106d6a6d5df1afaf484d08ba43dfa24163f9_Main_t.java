 //First Java Program
 import java.sql.*;
 import javax.sql.*;
 import java.util.Properties;
 import org.python.core.*;
 import org.python.util.PythonInterpreter;
 
 class Main {
 	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
 	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
 	
 	static final String USER = "vcf_user";
 	static final String PASS = "vcf";
 	
 	public static void main(String[] args) throws PyException {
 		PythonHandler python = new PythonHandler();
		python.invokeParser("blah");
 	}
 	
 /*	public static void main(String[] args) throws ClassNotFoundException, SQLException, PyException {
 		Connection conn = null;
 		Statement stmt = null;
 
 		PythonInterpreter interp = new PythonInterpreter();
 		interp.exec("import sys");
 		interp.exec("print 'From Python'");
 		System.out.println("From Java");
 
 		try {
 			Class.forName(JDBC_DRIVER);
 			
 			System.out.println("Connecting to database...");
 			conn = DriverManager.getConnection(DB_URL, USER, PASS);
 			
 			System.out.println("Creating statement...");
 			stmt = conn.createStatement();
 			String sql;
 			sql = "SELECT name FROM test";
 			ResultSet rs = stmt.executeQuery(sql);
 			
 			while(rs.next()) {
 				String name = rs.getString("name");
 				
 				System.out.println("Name: " + name);
 			}
 			
 			rs.close();
 			stmt.close();
 			conn.close();
 		} catch(SQLException se) {
 			se.printStackTrace();
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (conn!=null) {
 					conn.close();
 				}
 			} catch(SQLException se) {
 				se.printStackTrace();
 			}
 			System.out.println("Goodbye!");
 		}
 	} */
 }
 
