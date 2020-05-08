 package db;
 import java.math.BigDecimal;
 import java.sql.*;
 import java.util.*;
 import javax.sql.*;
 
 import javax.naming.*;
 
 public class StockSimDB {
 
 	protected Connection con = null;
 	
 	static {
         try {
             Class.forName("org.postgresql.Driver");
         } catch (ClassNotFoundException e) {
             System.err.println("Error loading JDBC driver: " + e.getMessage());
             System.exit(1);
         }
     }
 	    // Use lots of prepared statements for performance!
 	    // Java enum provides a nice way of specifying a static collection
 	    // of objects (in this case prepared statements:
 	    protected enum PreparedStatementID {
 			CreateNewUser("INSERT INTO USERS VALUES(?,?,?)"),
 			CreateNewPortfolio("INSERT INTO Portfolio VALUES(?, ?, ?, ?);"),
 			AuthLogin("SELECT USERNAME FROM USERS WHERE USERNAME=? AND PASSWORD=?"),
 			getTransactionHistory("SELECT * FROM TRANSACTION WHERE PID=? AND time>? ORDER BY time DESC "),
 			getStock_Holdings("SELECT TICKER, NUM_SHARES, AVG_PRICE_BOUGHT FROM STOCK_HOLDINGS WHERE PID=?"),
 			getPortfolioNameByUsername("SELECT PORTFOLIO_NAME FROM PORTFOLIO WHERE USERNAME=?"),
 			getPortfolioInfo("SELECT NAME, TIME_CREATED, CASH FROM PORTFOLIO WHERE PID=?"),
 			PerformTransaction("INSERT INTO Transaction VALUES(?, ?, ?, ?, ?, now())");
 			
 	        public final String sql;
 	        PreparedStatementID(String sql) {
 	            this.sql = sql;
 	            return;
 	        }
 	    }
 	    
 	    protected EnumMap<PreparedStatementID, PreparedStatement> _preparedStatements =
 	            new EnumMap<PreparedStatementID, PreparedStatement>(PreparedStatementID.class);
 	    
 	public StockSimDB() throws NamingException, SQLException{
 		connect();
 	}
 	
 	public void connect() throws NamingException, SQLException {
         // Is this a reconnection?  If so, disconnect first.
         if (con != null) disconnect();
         try {
         	String url = "jdbc:postgresql://localhost/stocksim";
         	Properties props = new Properties();
             props.setProperty("user", "ubuntu");
             props.setProperty("password", "reverse");
             con = DriverManager.getConnection(url, props);
 
             // Prepare statements:
             for (PreparedStatementID i: PreparedStatementID.values()) {
                 PreparedStatement preparedStatement = con.prepareStatement(i.sql);
                 _preparedStatements.put(i, preparedStatement);
             }
         } catch (SQLException e) {
             if (con != null) disconnect();
             throw e;
         }
     }
 
 	    public void disconnect() {
 	        // Close all prepared statements:
 	        for (PreparedStatementID i: _preparedStatements.keySet()) {
 	            try { _preparedStatements.get(i).close(); } catch (SQLException ignore) {}
 	        }
 	        _preparedStatements.clear();
 	        // Close the database connection:
 	        try {con.close(); } catch (SQLException ignore) {}
 	        con = null;
 	        return;
 	    }
 
 	    public void CreateUser(Users user) throws SQLException{
 	    	 PreparedStatement ps;
 	         boolean oldAutoCommitState = con.getAutoCommit();
 	         con.setAutoCommit(false);
 	         try {
 	             ps = _preparedStatements.get(PreparedStatementID.CreateNewUser);
 	             ps.setString(1, user.username);
 	             ps.setString(2, user.password);
 	             ps.setString(3, user.email);
 	             ps.executeUpdate();
	          
	             ps.executeUpdate();
	             
 	             con.commit();
 	             return;
 	         } 
 	         catch (SQLException e) {
 	             try {con.rollback(); } catch (SQLException ignore) {}
 	             throw e;
 	         } finally {
 	             try {con.setAutoCommit(oldAutoCommitState); } catch (SQLException ignore) {}
 	         }
 	    }
 	    
 	    public boolean AuthLogin(String username, String password) throws SQLException{
 	    	PreparedStatement ps;
 	        ResultSet rs = null;
 	        // Get user:
 	        ps = _preparedStatements.get(PreparedStatementID.AuthLogin);
 	        ps.setString(1, username);
 	        ps.setString(2, password);
 	        rs = ps.executeQuery();
 	        if (! rs.next()) {
 	            // No such user.
 	            return false;
 	        }
 	        return true;
 	        
 	    }
 	    
 	    public void Perform_Transaction(String PID, String ticker, int num_shares, BigDecimal price, String type)
 	    					throws SQLException{
 	    	 PreparedStatement ps;
 	         boolean oldAutoCommitState = con.getAutoCommit();
 	         con.setAutoCommit(false);
 	         try {
 	             ps = _preparedStatements.get(PreparedStatementID.PerformTransaction);
 	             ps.setString(1, PID);
 	             ps.setString(2, ticker);
 	             ps.setInt(3, num_shares);
 	             ps.setBigDecimal(4, price);
 	             ps.setString(5, type);
 	          
 	             ps.executeUpdate();
 	             
 	             con.commit();
 	             return;
 	         } 
 	         catch (SQLException e) {
 	             try {con.rollback(); } catch (SQLException ignore) {}
 	             throw e;
 	         } finally {
 	             try {con.setAutoCommit(oldAutoCommitState); } catch (SQLException ignore) {}
 	         }
 	    }
 	    
 	    public ArrayList<Transaction> getTransactionHistory(String PID, Timestamp time) throws SQLException{
 	    	 PreparedStatement ps = null;
 	    	 ResultSet rs = null;
 	        
 	         try {
 	        	 
 	        	 ps = _preparedStatements.get(PreparedStatementID.getTransactionHistory);
 	             ps.setString(1, PID);
 	             ps.setTimestamp(2, time);
 	             rs = ps.executeQuery();
 	             ArrayList<Transaction> Transactions = new ArrayList<Transaction>();
 	             while (rs.next()) {
 	            	 Transaction t = new Transaction(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4), rs.getString(5), rs.getTimestamp(6));
 	                 Transactions.add(t);
 	             }
 	             return Transactions;
 	         } catch (SQLException e) {
 	             // Here, we could wrap e inside another custom-defined
 	             // exception that provides the catcher with more
 	             // information about the context of e (e.g., it happened
 	             // while listing all drinkers).  However, I got lazy here
 	             // by just re-throwing e, which actually makes this catch
 	             // block useless.
 	             throw e;
 	         } finally {
 	             // To conserve JDBC resources, be nice and call close().
 	             // Although JDBC is supposed to call close() when these
 	             // things get garbage-collected, the problem is that if
 	             // you ever use connection pooling, if close() is not called
 	             // explicitly, these resources won't be available for
 	             // reuse, which can cause the connection pool to run out
 	             // of its allocated resources.
 	             if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 	             if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 	         }
 	    }
 	    
 	    public Portfolio getStock_Holdings(String PID) throws SQLException{
 	    	 PreparedStatement ps = null;
 	    	 ResultSet rs = null;
 	    	 Portfolio p;
 
 	         try {
 	        	 ps = _preparedStatements.get(PreparedStatementID.getPortfolioInfo);
 	             ps.setString(1, PID);
 	             rs = ps.executeQuery();
 	             rs.next();
 	             p = new Portfolio(rs.getString(1), rs.getTimestamp(2), rs.getBigDecimal(3));
 	             
 	             rs=null;
 	             ps=null;
 	             
 	             ps = _preparedStatements.get(PreparedStatementID.getStock_Holdings);
 	             ps.setString(1, PID);
 	             rs = ps.executeQuery();
 	             
 	             while (rs.next()) {
 	            	 p.addStock(rs.getString(1), rs.getInt(2), rs.getBigDecimal(3));
 	             }
 	             return p;
 	         } catch (SQLException e) {
 	             throw e;
 	         } finally {
 
 	             if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 	             if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 	         }
 	    }
 	    
 	    public List<String> getPortfolioNames(String username) throws SQLException {
 			PreparedStatement ps = null;
 			ResultSet rs = null;
 			List<String> portfolioNames = new ArrayList<String>();
 			
 			try {
 				ps = _preparedStatements.get(PreparedStatementID.getPortfolioNameByUsername);
 			    ps.setString(1, username);
 			    rs = ps.executeQuery();
 			    while (rs.next()) {
 			    	portfolioNames.add(rs.getString(1));
 			    }
 			    return portfolioNames;
 			} catch (SQLException e) {
 				throw e;
 			} finally {
 				// To conserve JDBC resources, be nice and call close().
 				// Although JDBC is supposed to call close() when these
 				// things get garbage-collected, the problem is that if
 				// you ever use connection pooling, if close() is not called
 				// explicitly, these resources won't be available for
 				// reuse, which can cause the connection pool to run out
 				// of its allocated resources.
 			    if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 			    if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 			 }	    	
 	    }
 }
