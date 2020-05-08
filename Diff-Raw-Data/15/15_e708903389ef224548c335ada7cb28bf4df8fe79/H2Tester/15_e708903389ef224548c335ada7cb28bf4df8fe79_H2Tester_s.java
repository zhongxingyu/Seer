 package test.db.h2;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import org.junit.Test;
 
 public class H2Tester {
 
 	@Test
	public void testConnectionIsValid() throws SQLException, ClassNotFoundException {
 
		//Class.forName("org.h2.Driver");
 		final Connection connection = DriverManager
				.getConnection("jdbc:h2:test");
 
 		connection.createStatement();
 		// dataSource.setUsername("sa");
 		// dataSource.setPassword("");
 	}
 }
