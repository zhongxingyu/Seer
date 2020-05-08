 package ee.alkohol.juks.sirvid.containers;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class DaoKalenderJDBCSqlite {
     
     public Connection dbConnection;
     public String errorMsg;
     public String jdbcConnect;
     
     public DaoKalenderJDBCSqlite(String jdbcc) {
         
         dbConnection = null;
         errorMsg = null;
         jdbcConnect = jdbcc;
         
         try {
             Class.forName("org.sqlite.JDBC");
             dbConnection = DriverManager.getConnection(jdbcc);
         }
         catch(Exception e) {
             errorMsg = e.getMessage();
         }
         
     }
     
     public ResultSet fetchQueryResults(String query) {
   
         Statement statement;
         try {
             statement = dbConnection.createStatement();
             statement.setQueryTimeout(30);  // set timeout to 30 sec.
             ResultSet rs = statement.executeQuery(query);
             return rs;
         }
         catch(SQLException e) {
             // TODO Auto-generated catch block
             errorMsg = e.getMessage();
         }
         return null;
         
     }
     
     public ResultSet getAnniversaries(int start, int end, boolean maausk) {
     	StringBuilder query = new StringBuilder("select * from events where ");
     	if(start == end) {
     		query.append("id = ");
     		query.append(start);
     	} else {
     		query.append("id >= ");
     		query.append(start);
     		query.append(" and id <= ");
     		query.append(end);
     	}
     	if(maausk) {
    		query.append(" maausk is not null and trim(maausk) <> ''");
     	}
         return fetchQueryResults(query.toString());
     }
     
 }
