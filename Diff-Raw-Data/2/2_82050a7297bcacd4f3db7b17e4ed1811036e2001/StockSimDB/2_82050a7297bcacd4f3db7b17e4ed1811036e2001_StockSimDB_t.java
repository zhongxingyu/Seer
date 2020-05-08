 package db;
 
 import java.math.BigDecimal;
 import java.sql.*;
 import java.util.*;
 
 import javax.sql.*;
 
 import javax.naming.*;
 
 import org.postgresql.util.PGInterval;
 
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
 	
     protected enum PreparedStatementID {
 		CREATE_NEW_USER("INSERT INTO Users VALUES(?,?,?)"),
 		CREATE_NEW_PORTFOLIO("INSERT INTO Portfolio VALUES(?, ?, ?, ?, ?);"),
		DELETE_PORTFOLIO("DELETE FROM PORTFOLIO WHERE username=? AND portfolio_name=?"),
 		AUTH_LOGIN("SELECT username FROM Users WHERE username=? AND password=?"),
 		PERFORM_TRANSACTION("INSERT INTO Transaction VALUES(?, ?, ?, ?, ?, now())"),
 		GET_TRANSACTION_HISTORY_BY_TIME("SELECT * FROM Transaction WHERE PID=? AND time>=(now()-?) ORDER BY time DESC"),
 		GET_TRANSACTION_HISTORY_BY_NUMBER("SELECT * FROM Transaction WHERE PID=? ORDER BY time DESC LIMIT ?"),
 		GET_STOCK_HOLDINGS("SELECT ticker, num_shares, avg_price_bought FROM Stock_Holdings WHERE PID=?"),
 		GET_PORTFOLIO_NAMES("SELECT portfolio_name FROM Portfolio WHERE username=?"),
 		GET_PORTFOLIO_INFO("SELECT portfolio_name, time_created, cash FROM Portfolio WHERE PID=?"),
 		GET_PID("SELECT PID FROM Portfolio WHERE username=? AND portfolio_name=?"),
 		GET_ALL_PORTFOLIOS("SELECT PID, portfolio_name, username, time_created, cash FROM Portfolio WHERE username=?"),
 		GET_PORTFOLIO_NAME_BY_PID("SELECT portfolio_name FROM Portfolio WHERE PID=?"),
 		getStockTickers("SELECT DISTINCT ticker FROM Stock_Prices"),
 		updatePrices("UPDATE Stock_Prices SET price=? WHERE ticker=?"),
 		getLeaderBoard("Select portfolio.PID, portfolio.username, portfolio.portfolio_name, portfolio.cash + stock_value.values as Mkt_Value " +
 			"from (select PID, Sum(Stock_Prices.price*num_shares) as values from stock_holdings, Stock_Prices where stock_holdings.ticker=Stock_Prices.ticker " +
 			"Group By PID) as stock_value, portfolio where portfolio.PID = stock_value.PID ORDER BY Mkt_Value DESC LIMIT 10"),
 		AuthenticatePID("SELECT EXISTS (SELECT PID FROM Portfolio WHERE username=? and PID=?)"),
 		getRank("SELECT COUNT(portfolio.PID)+1 AS Rank FROM (SELECT PID, SUM(Stock_Prices.price*num_shares) AS values FROM stock_holdings, " +
 				"Stock_Prices WHERE stock_holdings.ticker=Stock_Prices.ticker GROUP BY PID) AS stock_value, portfolio WHERE portfolio.PID = " +
 				"stock_value.PID AND (portfolio.cash+stock_value.values)> (SELECT cash+SUM(Mkt_Value.values) FROM (SELECT PID, " +
 				"Stock_Prices.Price*num_shares as values from stock_holdings, Stock_Prices WHERE PID=? AND stock_holdings.ticker=" +
 				"Stock_Prices.ticker) AS Mkt_Value, portfolio WHERE portfolio.PID=Mkt_Value.PID GROUP BY portfolio.PID)"),
 		getTotalPortfolioNum("SELECT COUNT(PID) FROM Portfolio"); 
 		
         public final String sql;
         
         PreparedStatementID(String sql) {
             this.sql = sql;
         }
     }
     
     protected EnumMap<PreparedStatementID, PreparedStatement> _preparedStatements =
             new EnumMap<PreparedStatementID, PreparedStatement>(PreparedStatementID.class);
 	    
 	public StockSimDB() throws NamingException, SQLException {
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
              ps = _preparedStatements.get(PreparedStatementID.CREATE_NEW_USER);
              ps.setString(1, user.username);
              ps.setString(2, user.password);
              ps.setString(3, user.email);
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
 	    
     public void createPortfolio(Portfolio portfolio) throws SQLException{
     	 PreparedStatement ps;
          boolean oldAutoCommitState = con.getAutoCommit();
          con.setAutoCommit(false);
          try {
         	 // Generate unique PID
         	 StringBuilder PID = new StringBuilder("P");
         	 PID.append(System.nanoTime());
         	 
         	 // Generate timestamp
              java.util.Date date = new java.util.Date(System.currentTimeMillis()); 
              java.sql.Timestamp timeCreated = new java.sql.Timestamp(date.getTime());
              
              ps = _preparedStatements.get(PreparedStatementID.CREATE_NEW_PORTFOLIO);
              ps.setString(1, PID.toString());
              ps.setString(2, portfolio.getName());
              ps.setString(3, portfolio.getUsername());
              ps.setTimestamp(4, timeCreated);
              ps.setBigDecimal(5, portfolio.getCash());
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
     
     public void deletePortfolio(String username, String portfolioName) throws SQLException{
    	 PreparedStatement ps;
         boolean oldAutoCommitState = con.getAutoCommit();
         con.setAutoCommit(false);
         try {
             ps = _preparedStatements.get(PreparedStatementID.DELETE_PORTFOLIO);
             ps.setString(1, username);
             ps.setString(2, portfolioName);
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
         ps = _preparedStatements.get(PreparedStatementID.AUTH_LOGIN);
         ps.setString(1, username);
         ps.setString(2, password);
         rs = ps.executeQuery();
         if (!rs.next()) {
             // No such user.
             return false;
         }
         return true;
     }
     
     public BigDecimal performTransaction(String PID, String type, int num_shares, String ticker) throws Exception {
     	 PreparedStatement ps;
          boolean oldAutoCommitState = con.getAutoCommit();
          con.setAutoCommit(false);
          try {
         	 BigDecimal price = YAPI_Reader.getPrice(ticker);
         	 
         	 ps = _preparedStatements.get(PreparedStatementID.PERFORM_TRANSACTION);
              ps.setString(1, PID);
              ps.setString(2, ticker);
              ps.setInt(3, num_shares);
              ps.setBigDecimal(4, price);
              ps.setString(5, type);
              ps.executeUpdate();
              
              con.commit();
              return price;
          } 
          catch (Exception e) {
              try {con.rollback(); } catch (SQLException ignore) {}
              throw e;
          } finally {
              try {con.setAutoCommit(oldAutoCommitState); } catch (SQLException ignore) {}
          }
     }
     
     public List<Transaction> getTransactionHistoryByNumber(String PID, int numTransactions) throws SQLException{
     	 PreparedStatement ps = null;
     	 ResultSet rs = null;
          try {
         	 
         	 ps = _preparedStatements.get(PreparedStatementID.GET_TRANSACTION_HISTORY_BY_NUMBER);
              ps.setString(1, PID);
              ps.setInt(2, numTransactions);
              rs = ps.executeQuery();
              List<Transaction> transactions = new ArrayList<Transaction>();
              while (rs.next()) {
             	 Transaction t = new Transaction(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4), rs.getString(5), rs.getTimestamp(6));
             	 transactions.add(t);
              }
              return transactions;
          } catch (SQLException e) {
              throw e;
          } finally {
 //	             if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 //	             if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
          }
     }
     
     public List<Transaction> getTransactionHistoryByTime(String PID, int numDays) throws SQLException{
     	PreparedStatement ps = null;
    	 	ResultSet rs = null;
         try {
        	 	ps = _preparedStatements.get(PreparedStatementID.GET_TRANSACTION_HISTORY_BY_TIME);
             ps.setString(1, PID);
             PGInterval interval = new PGInterval();
             interval.setDays(numDays);
             ps.setObject(2, interval);
             rs = ps.executeQuery();
             List<Transaction> transactions = new ArrayList<Transaction>();
             while (rs.next()) {
            	 	Transaction t = new Transaction(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4), rs.getString(5), rs.getTimestamp(6));
            	 	transactions.add(t);
             }
             return transactions;
         } catch (SQLException e) {
             throw e;
         } finally {
 //	             if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 //	             if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
         }
    }
     
     public Portfolio getStock_Holdings(String PID) throws SQLException{
     	 PreparedStatement ps = null;
     	 ResultSet rs = null;
     	 Portfolio p = null;
 
          try {
         	 ps = _preparedStatements.get(PreparedStatementID.GET_PORTFOLIO_INFO);
              ps.setString(1, PID);
              rs = ps.executeQuery();
              while (rs.next()) {
 	             p = new Portfolio(rs.getString(1), rs.getTimestamp(2), rs.getBigDecimal(3));
              }
              
              ps = _preparedStatements.get(PreparedStatementID.GET_STOCK_HOLDINGS);
              ps.setString(1, PID);
              rs = ps.executeQuery();
              while (rs.next()) {
             	 p.addStock(rs.getString(1), rs.getInt(2), rs.getBigDecimal(3));
              }
              return p;
          } catch (SQLException e) {
              throw e;
          } finally {
 //	             if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 //	             if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
          }
     }
     
     public List<String> getPortfolioNames(String username) throws SQLException {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		List<String> portfolioNames = new ArrayList<String>();
 		
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.GET_PORTFOLIO_NAMES);
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
 //			    if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 //			    if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		 }	    	
     }
     
     public String getPID(String username, String portfolioName) throws SQLException {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String PID = null;
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.GET_PID);
 		    ps.setString(1, username);
 		    ps.setString(2, portfolioName);
 		    rs = ps.executeQuery();
 		    while (rs.next()) {
 		    	PID = rs.getString(1);
 		    }
 		    return PID;
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
 		    //if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 		    //if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		 }	    	
     }
     
     public List<Portfolio> getAllPortfolios(String username) throws SQLException {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		List<Portfolio> allPortfolios = new ArrayList<Portfolio>();
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.GET_ALL_PORTFOLIOS);
 		    ps.setString(1, username);
 		    rs = ps.executeQuery();
 		    while (rs.next()) {
 		        Portfolio p = new Portfolio(rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4), rs.getBigDecimal(5));
 		    	allPortfolios.add(p);
 		    }
 		    return allPortfolios;
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
 		    //if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 		    //if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		 }	    	
     }
     
     public String getPortfolioName(String PID) throws SQLException {
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String portfolioName = null;
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.GET_PORTFOLIO_NAME_BY_PID);
 		    ps.setString(1, PID);
 		    rs = ps.executeQuery();
 		    while (rs.next()) {
 		    	portfolioName = rs.getString(1);
 		    }
 		    return portfolioName;
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
 		    //if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 		    //if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		 }	    	
     }
     public List<LeaderBoard> getLeaderBoards() throws Exception{
     	updateStockPrices();
     	PreparedStatement ps = null;
 		ResultSet rs = null;
 		List<LeaderBoard> leaders = new ArrayList<LeaderBoard>();
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.getLeaderBoard);
 		    rs = ps.executeQuery();
 		    while (rs.next()){
 		    	LeaderBoard lb = new LeaderBoard(rs.getString(2), rs.getString(3), rs.getBigDecimal(4));
 		    	leaders.add(lb);
 		    }
 		    return leaders;
 		    
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 //		    if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 //		    if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		}
     }
     
     public void updateStockPrices() throws Exception{
     	PreparedStatement ps = null;
 		ResultSet rs = null;
 		List<String> tickers = new ArrayList<String>();
         boolean oldAutoCommitState = con.getAutoCommit();
         con.setAutoCommit(false);
         //Gets List of Stock Tickers
         try {
 			ps = _preparedStatements.get(PreparedStatementID.getStockTickers);
 		    rs = ps.executeQuery();
 		    while (rs.next()) {
 		    	tickers.add(rs.getString(1));
 		    }
         }catch (SQLException e) {
 			throw e;
 		}finally {
 //		    if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 //		    if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		}
         
         //Updates Prices for the List of Stock Tickers
 		try {
 		  	ps = null;
 		   	rs = null;
 		   	List<BigDecimal> prices = YAPI_Reader.getPrices(tickers);
 		   	for (int i = 0; i < tickers.size(); i++){
 		   		ps = _preparedStatements.get(PreparedStatementID.updatePrices);
 		   		ps.setBigDecimal(1, prices.get(i));
 		   		ps.setString(2, tickers.get(i));
 		   		ps.executeUpdate();
 		   	}
             con.commit();
 		} catch (SQLException e) {
 		   	con.rollback();
 		}
         finally {
             try {con.setAutoCommit(oldAutoCommitState); } catch (SQLException ignore) {}
         }
     }
     
     public List<Integer> getRanking(String PID) throws Exception{
     	
     	List<Integer> rank = new ArrayList<Integer>();
     	updateStockPrices();
     	
     	PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.getRank);
 		    ps.setString(1, PID);
 		    rs = ps.executeQuery();
 		    rs.next();
 		    rank.add(rs.getInt(1));
 		    
 		    ps = null;
 		    rs = null;
 		    ps = _preparedStatements.get(PreparedStatementID.getTotalPortfolioNum);
 		    rs = ps.executeQuery();
 		    rs.next();
 		    rank.add(rs.getInt(1));
 		    
 		    return rank;
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 		    //if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 		    //if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		 }	    
     }
     
     public boolean isAuthorized(String username, String PID) throws SQLException{
     	PreparedStatement ps = null;
 		ResultSet rs = null;
 		boolean answer = false;
 		try {
 			ps = _preparedStatements.get(PreparedStatementID.AuthenticatePID);
 			ps.setString(1, username);
 			ps.setString(2, PID);
 		    rs = ps.executeQuery();
 		    rs.next();
 		    answer = rs.getBoolean(1);
 		    return answer;
 		} catch (SQLException e) {
 			throw e;
 		} finally {
 		    //if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
 		    //if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
 		 }	    	
     }
     
     public static void main(String args[]){
     	//StockSimDB db = new StockSimDB();
     	java.util.Date date = new java.util.Date(System.currentTimeMillis()); 
     	java.sql.Timestamp timeCreated = new java.sql.Timestamp(date.getTime());
     	System.out.println(timeCreated);
     }
 }
