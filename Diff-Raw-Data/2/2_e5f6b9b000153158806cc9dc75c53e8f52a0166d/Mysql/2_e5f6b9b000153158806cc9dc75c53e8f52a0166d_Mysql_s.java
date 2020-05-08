 package ajeetmurty.reference.java.db;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
 
 public class Mysql {
 	private final Logger logp = LoggerFactory.getLogger(this.getClass().getName());
 	// public mysql server - http://genome.ucsc.edu/goldenPath/help/mysql.html
 	private final String hostname = "genome-mysql.cse.ucsc.edu";
 	private final int port = 3306;
 	private final String username = "genomep";
 	private final String password = "password";
 	private final String sql = "SELECT SYSDATE()";
 
 	public static void main(String[] args) {
 		new Mysql();
 	}
 
 	public Mysql() {
 		logp.info("start");
 		connectToDb();
 		logp.info("stop");
 	}
 
 	private void connectToDb() {
 		try {
			logp.info(String.format("conn params: hostname|port|username|password : %1$s|%2$s|%3$s|%3$s", hostname, port, username, password));
 
 			MysqlDataSource dataSource = new MysqlDataSource();
 			dataSource.setServerName(hostname);
 			dataSource.setPort(port);
 			dataSource.setUser(username);
 			dataSource.setPassword(password);
 
 			Connection conn = (Connection) dataSource.getConnection();
 			PreparedStatement pstmt = conn.prepareStatement(sql);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				logp.info("result date: " + rs.getTimestamp(1).toString());
 			}
 
 			rs.close();
 			pstmt.close();
 			conn.close();
 		} catch (Exception e) {
 			logp.error(e.getMessage(), e);
 		}
 	}
 }
