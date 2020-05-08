 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class FilterCreatorTest {
 	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
 	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
 	
 	static final String USER = "vcf_user";
 	static final String PASS = "vcf";
 
 	public static void main(String[] args) throws ClassNotFoundException, SQLException {
 		Connection conn = null;
 		Statement stmt = null;
 		
 		try {
 	
 			Class.forName(JDBC_DRIVER);
 	
 			conn = DriverManager.getConnection(DB_URL, USER, PASS);
 			stmt = conn.createStatement();
 		}
 		catch (Exception e) {
 			throw new SQLException(e.getMessage());
 		}
 		
 		String sql = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilter'";
 		
 		try {
 			stmt.executeUpdate(sql);
 
 		} catch (SQLException se) {
 			throw new SQLException(se.getMessage());
 		}
 		
 		String filterName = "testFilter";
		String[] operands = {"ind AC>1", "ind AF=0", "entry AD=4", 
				"ent DP > 10", "entry GLE between 2 10", "ind QD between 10 15", 
				"entry bogus < 55", "ind AC greater than 55.55"};
 		FilterCreator testCreator = new FilterCreator(filterName, operands);
 		testCreator.uploadEntries();
 	}
 
 }
