 import static org.junit.Assert.*;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UploadTester {
 	private Connection connection;
 	private Statement stmt;
 	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
 	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
 
 	static final String USER = "vcf_user";
 	static final String PASS = "vcf";
 
 	@Before
 	public void setUpTables() throws ClassNotFoundException, SQLException {
 		Class.forName(JDBC_DRIVER);
 		this.connection=DriverManager.getConnection(DB_URL, USER, PASS);
 		this.stmt=this.connection.createStatement();
 		this.stmt.executeUpdate("TRUNCATE TABLE `Annotation`");
 		this.stmt.executeUpdate("TRUNCATE TABLE `Divergence`");
 	}
 
 	@Test
 	public void testUploadAnnotation() throws SQLException {
 		Command uploadAnnotCommand=new UploadAnnotationCommand("Examples/annot.txt","","");
 		uploadAnnotCommand.execute();
 		ResultSet rs=this.stmt.executeQuery("Select * from `Annotation`");
 		while (rs.next()){
 			System.out.println(rs.getString(1)+rs.getString(2)+rs.getString(3)+rs.getString(4)+rs.getString(rs.getString(5))+rs.getString(6)+rs.getString(7));
 		}
 		rs.close();
 		fail();
 		
 	}
 		
 	@After
 	public void cleanUpConnection() throws SQLException {
 		if (this.connection != null) {
 			this.connection.close();
 		}
 		if (this.stmt != null) {
 			this.stmt.close();
 		}
 	}
 }
