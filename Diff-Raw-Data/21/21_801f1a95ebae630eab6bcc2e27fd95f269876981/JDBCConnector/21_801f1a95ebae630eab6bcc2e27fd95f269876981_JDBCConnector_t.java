 package ass2;
 
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.LinkedList;
 
 import beans.AlertBean;
 import beans.AlertListBean;
 import beans.AuctionBean;
 import beans.AuctionListBean;
 import beans.BidBean;
 import beans.BidListBean;
 import beans.UserBean;
 import beans.UserListBean;
 import beans.WinningAuctionBean;
 import beans.WinningAuctionListBean;
 
 public class JDBCConnector {
 
 	//Possibly store these in a file rather than in code
 	//private static final String postgreConn = "jdbc:postgresql://127.0.0.1:5432/ass2";
 	//private static final String user = "test";
 	//private static final String pass = "test";
 	
 	//derby connection
 	private static final String postgreConn = "jdbc:derby://localhost:1527/ass2";
 	private static final String user = "root";
 	private static final String pass = "root";
 	
 	private static Connection connect() {
 		try {
 			//Class.forName("org.postgresql.Driver");
 			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
 		} catch (ClassNotFoundException e) {
 			System.out.println("No postgresql driver.");
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			return DriverManager.getConnection(postgreConn, user, pass);
 		} catch (SQLException e) {
 			System.out.println("Error JDBC can't connect.");
 		}
 		
 		return null;
 	}
 	
 	private static void close(Connection c) {
		/* doesnt work with derby
 		try {
 			if (!c.isClosed())
 				c.close();
 		} catch (SQLException e) {
 			System.out.println("Cannot close connection.");
 		}*/
 	}
 	
 	public static void addUser(String username, String password, String email, String nickname, String firstName, String lastName, int yearOfBirth, String postalAddress, int CCNumber, boolean banned, int hash) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("INSERT INTO username VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 
 			ps.setString(1, username);
 			ps.setString(2, password);
 			ps.setString(3, email);
 			ps.setString(4, nickname);
 			ps.setString(5, firstName);
 			ps.setString(6, lastName);
 			ps.setInt(7, yearOfBirth);
 			ps.setString(8, postalAddress);
 			ps.setInt(9, CCNumber);
 			ps.setBoolean(10, banned);
 			ps.setBoolean(11, false);
 			ps.setBoolean(12, false);
 			ps.setInt(13, hash);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not add user.\n" + e.getMessage());
 		}
 		close(c);
 	}
 	
 	public static boolean checkVerification(String username, int hash) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT username FROM username WHERE username=? AND hash=?");
 			ps.setString(1, username);
 			ps.setInt(2, hash);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				return true;
 		} catch (SQLException e) {
 			System.out.println("Could not check verification.");
 		}
 		close(c);
 		return false;
 	}
 	
 	public static void verify(String username) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("UPDATE username SET verified=TRUE WHERE username=?");
 			ps.setString(1, username);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not update verification.");
 		}
 		close(c);
 	}
 	
 	public static boolean isUserExists(String username) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT COUNT(username) AS total FROM username WHERE username=?");
 			ps.setString(1, username);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				return (rs.getInt("total") > 0) ? true : false;
 			}
 		} catch (SQLException e) {
 			System.out.println("Could not check user.");
 		}
 		close(c);
 		return false;
 	}
 	
 	public static boolean hasLogin(String username, String password) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT banned FROM username WHERE username=? AND password=? AND verified=TRUE");
 			ps.setString(1, username);
 			ps.setString(2, password);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				return true;
 			}
 		} catch (SQLException e) {
 			System.out.println("Could not check user login.");
 		}
 		close(c);
 		return false;
 	}
 	
 	public static boolean hasAdminLogin(String username, String password) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT banned FROM username WHERE username=? AND password=? AND admin=TRUE AND verified=TRUE");
 			ps.setString(1, username);
 			ps.setString(2, password);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				return true;
 			}
 		} catch (SQLException e) {
 			System.out.println("Could not check user login.");
 		}
 		close(c);
 		return false;
 	}
 	
 	//TODO update account update the bean too
 	public static boolean updateAccount(UserBean ub) {
 		Connection c = null;
 		String query = "UPDATE username SET ";
 		if (ub.getPassword() != null && !ub.getPassword().equals("")) query += "password=?, ";
 		if (ub.getEmail() != null && !ub.getEmail().equals("")) query += "email_address=?, ";
 		if (ub.getNickname() != null && !ub.getNickname().equals("")) query += "nickname=?, ";
 		if (ub.getFirstName() != null && !ub.getFirstName().equals("")) query += "first_name=?, ";
 		if (ub.getLastName() != null && !ub.getLastName().equals("")) query += "last_name=?, ";
 		if (ub.getYearOfBirth() != 0) query += "year_of_birth=?, ";
 		if (ub.getPostalAddress() != null && !ub.getPostalAddress().equals("")) query += "postal_address=?, ";
 		if (ub.getCcNumber() != 0) query += "cc_number=?, ";
 		query = query.substring(0, query.length() - 2);
 		query += " WHERE username=?";
 		
 		try {
 			c = connect();
 			int i = 1;
 			PreparedStatement ps = c.prepareStatement(query);
 			if (ub.getPassword() != null && !ub.getPassword().equals("")) ps.setString(i++, ub.getPassword());
 			if (ub.getEmail() != null && !ub.getEmail().equals("")) ps.setString(i++, ub.getEmail());
 			if (ub.getNickname() != null && !ub.getNickname().equals("")) ps.setString(i++, ub.getNickname());
 			if (ub.getFirstName() != null && !ub.getFirstName().equals("")) ps.setString(i++, ub.getFirstName());
 			if (ub.getLastName() != null && !ub.getLastName().equals("")) ps.setString(i++, ub.getLastName());
 			if (ub.getYearOfBirth() != 0) ps.setInt(i++, ub.getYearOfBirth());
 			if (ub.getPostalAddress() != null && !ub.getPostalAddress().equals("")) ps.setString(i++, ub.getPostalAddress());
 			if (ub.getCcNumber() != 0) ps.setInt(i++, ub.getCcNumber());
 			if (ub.getUsername() != null && !ub.getUsername().equals("")) ps.setString(i++, ub.getUsername());
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not update account.");
 		}
 		close(c);
 		return false;
 	}
 	
 	public static void banUser(String username) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("UPDATE username SET banned=TRUE WHERE username=?");
 			ps.setString(1, username);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could notban user.");
 		}
 		close(c);
 	}
 	
 	public static UserBean getUserBean(String username, boolean isAdminLogin) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT username, email_address, nickname, first_name, last_name, year_of_birth, postal_address, banned, admin FROM username WHERE username=?");
 			ps.setString(1, username);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				close(c);
 				UserBean ub = new UserBean();
 				ub.setUsername(rs.getString("username"));
 				ub.setEmail(rs.getString("email_address"));
 				ub.setNickname(rs.getString("nickname"));
 				ub.setFirstName(rs.getString("first_name"));
 				ub.setLastName(rs.getString("last_name"));
 				ub.setYearOfBirth(rs.getInt("year_of_birth"));
 				ub.setPostalAddress(rs.getString("postal_address"));
 				ub.setIsBanned(rs.getBoolean("banned"));
 				if (isAdminLogin) ub.setIsAdmin(rs.getBoolean("admin"));
 				else ub.setIsAdmin(false);
 				return ub;
 			}
 		} catch (SQLException e) {
 			System.out.println("Could not get userbean.");
 		}
 		close(c);
 		return null;
 	}
 	
 	public static UserListBean getUsers() {
 		Connection c = null;
 		UserListBean ulb = new UserListBean();
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT username FROM username WHERE banned=FALSE AND admin=FALSE");
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				close(c);
 				UserBean ub = new UserBean();
 				ub.setUsername(rs.getString("username"));
 				ulb.addUser(ub);
 			}
 			return ulb;
 		} catch (SQLException e) {
 			System.out.println("Could not get userbeans.");
 		}
 		close(c);
 		return ulb;
 	}
 	
 	public static int addAuction(String title, String author, String category, String picture, String description, String postageDetails, float reservePrice, float startPrice, float biddingIncrements, Timestamp date, boolean halt) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("INSERT INTO Auction VALUES (DEFAULT, ?, (SELECT username FROM Username WHERE username=?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
 			ps.setString(1, title);
 			ps.setString(2, author);
 			ps.setString(3, category);
 			ps.setString(4, picture);
 			ps.setString(5, description);
 			ps.setString(6, postageDetails);
 			ps.setFloat(7, reservePrice);
 			ps.setFloat(8, startPrice);
 			ps.setFloat(9, biddingIncrements);
 			ps.setTimestamp(10, date);
 			ps.setBoolean(11, halt);
 			ps.setBoolean(12, false);
 			ps.executeUpdate();
 			
 			ResultSet gk = ps.getGeneratedKeys();
 			gk.next();
 			return (int) gk.getLong(1);
 		} catch (SQLException e) {
 			System.out.println("Could not add auction.");
 		}
 		close(c);
 		return 0;
 	}
 	
 	public static AuctionListBean getAuction(int id, String author, String title, boolean getNotFinished) {
 		Connection c = null;
 		
 		if (author != null)author = author.toLowerCase();
 		if (title != null) title = title.toLowerCase();
 		
 		String query = "SELECT * FROM Auction WHERE 1=1 ";
 		if (id > 0) query += "AND id=? ";
 		if (author != null) query += "AND LOWER(author) LIKE ? ";
 		if (title != null) query += "AND LOWER(title) LIKE ? ";
 		if (getNotFinished) query += "AND finished=FALSE ";
 		query += "ORDER BY end_of_auction DESC";
 				
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement(query);
 			
 			//TODO fix this so it looks better
 			int incre = 1;
 			if (id > 0) ps.setInt(incre++, id);
 			if (author != null) ps.setString(incre++, "%" + author + "%");
 			if (title != null) ps.setString(incre++, "%" + title + "%");
 
 			ResultSet rs = ps.executeQuery();
 			AuctionListBean alb = new AuctionListBean();
 			while (rs.next()) {
 				close(c);	
 				alb.addAuction(new AuctionBean(
 						rs.getInt("id"),
 						rs.getString("title"),
 						rs.getString("author"),
 						rs.getString("category"),
 						rs.getString("picture"),
 						rs.getString("description"),
 						rs.getString("postage_details"),
 						rs.getFloat("reserve_price"),
 						rs.getFloat("start_price"),
 						rs.getFloat("bidding_increments"),
 						rs.getTimestamp("end_of_auction"),
 						rs.getBoolean("halt"),
 						rs.getBoolean("finished"))
 				);
 			}
 			return alb;
 		} catch (SQLException e) {
 			System.out.println("Could not get auctions.");
 		}
 		close(c);
 		return null;
 	}
 	
 	public static boolean isOwnerWinningAuction(int id, String bidder) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT b.author FROM winningauction wa INNER JOIN bidding b ON wa.bid=b.id AND wa.id=? AND b.author=?");
 			ps.setInt(1, id);
 			ps.setString(2, bidder);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				return true;
 		} catch (SQLException e) {
 			System.out.println("Could not check owner of winning auction.");
 		}
 		close(c);
 		return false;
 	}
 	
 	//make transaction?
 	public static void deleteWinningAuction(int id) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("DELETE FROM winningauction WHERE id=?");
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not delete winning auction.");
 		}
 		close(c);
 	}
 	
 	/*
 	 * 	//make transaction?
 	public static void deleteWinningAuction(int id) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT auction FROM winningauction WHERE id=?");
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			int auctionID = 0;
 			while (rs.next())
 				auctionID = rs.getInt("auction");
 			ps = c.prepareStatement("DELETE FROM winningauction WHERE id=?");
 			ps.setInt(1, id);
 			ps.execute();
 			ps = c.prepareStatement("DELETE FROM bidding WHERE auction=?");
 			ps.setInt(1, auctionID);
 			ps.execute();
 			ps = c.prepareStatement("DELETE FROM auction WHERE id=?");
 			ps.setInt(1, auctionID);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not clean auction.");
 		}
 		close(c);
 	}
 	 */
 	
 	//make transaction?
 	public static void deleteAuction(int id) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("DELETE FROM winningauction WHERE auction=?");
 			ps.setInt(1, id);
 			ps.execute();
 			ps = c.prepareStatement("DELETE FROM bidding WHERE auction=?");
 			ps.setInt(1, id);
 			ps.execute();
 			ps = c.prepareStatement("DELETE FROM alert WHERE auction=?");
 			ps.setInt(1, id);
 			ps.execute();
 			ps = c.prepareStatement("DELETE FROM auction WHERE id=?");
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not hard delete auction.");
 		}
 		close(c);
 	}
 	
 	public static void haltAuction(int id) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("UPDATE auction SET halt=TRUE WHERE id=?");
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could notban user.");
 		}
 		close(c);
 	}
 	
 	public static LinkedList<Integer> haltAllAuctions(String author) {
 		Connection c = null;
 		LinkedList<Integer> li = new LinkedList<Integer>();
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("UPDATE auction SET halt=TRUE WHERE author=?", Statement.RETURN_GENERATED_KEYS);
 			ps.setString(1, author);
 			ps.execute();
 			
 			ResultSet gk = ps.getGeneratedKeys();
 			while (gk.next())
 				li.add(gk.getInt(1));
 			return li;
 		} catch (SQLException e) {
 			System.out.println("Could notban user.");
 		}
 		close(c);
 		return li;
 	}
 	
 	public static void addBidding(String author, int auctionID, float price, Timestamp bidDate) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("INSERT INTO Bidding VALUES (DEFAULT, (SELECT username FROM Username WHERE username=?), (SELECT id FROM Auction WHERE id=?), ?, ?)");
 			ps.setString(1, author);
 			ps.setInt(2, auctionID);
 			ps.setFloat(3, price);
 			ps.setTimestamp(4, bidDate);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not add bidding.");
 		}
 		close(c);
 	}
 	
 	public static void finishAuction(int id) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("UPDATE auction SET finished=TRUE WHERE id=?");
 			ps.setInt(1, id);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not finish auction.");
 		}
 		close(c);
 	}
 	
 	public static BidListBean getBiddings(int id, boolean topBid) {
 		Connection c = null;
 		String query = "SELECT * FROM Bidding WHERE auction=(SELECT id FROM Auction WHERE id=?) ORDER BY price DESC";
 		if (topBid) query += " LIMIT 1";
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			BidListBean blb = new BidListBean();
 			while (rs.next()) {	
 				blb.addBid(new BidBean(
 						rs.getInt("id"),
 						rs.getString("author"),
 						rs.getFloat("price"))
 				);
 			}
 			close(c);
 			return blb;
 		} catch (SQLException e) {
 			System.out.println("Could not get biddings.");
 		}
 		close(c);
 		return null;
 	}
 	
 	public static void addWinningAuction(int auctionID, int bidID) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("INSERT INTO WinningAuction VALUES (DEFAULT, (SELECT id FROM Auction WHERE id=?), (SELECT id FROM Bidding WHERE id=?))");
 			ps.setInt(1, auctionID);
 			ps.setInt(2, bidID);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not add winning auction.");
 		}
 		close(c);
 	}
 	
 	public static WinningAuctionListBean getWinningAuctions(String user) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("select wa.id as wid, a.id as aid, a.title as atitle, a.reserve_price as areserve_price, b.id as bid, b.price as bprice from winningauction wa inner join auction AS a on wa.auction=a.id inner join bidding b on wa.bid=b.id and b.author=(SELECT username FROM username WHERE username=?)");;
 			ps.setString(1, user);
 			ResultSet rs = ps.executeQuery();
 			WinningAuctionListBean walb = new WinningAuctionListBean();
 			while (rs.next()) {	
 				walb.addAuction(new WinningAuctionBean(
 						rs.getInt("wid"),
 						rs.getInt("aid"),
 						rs.getString("atitle"),
 						rs.getFloat("areserve_price"),
 						rs.getInt("bid"),
 						rs.getFloat("bprice"))
 				);
 			}
 			close(c);
 			return walb;
 		} catch (SQLException e) {
 			System.out.println("Could not get winning auctions.");
 		}
 		close(c);
 		return null;
 	}
 	
 	public static void clearDB() {
 		Connection c = null;
 		try {
 			c = connect();
 			Statement s = c.createStatement();
 			s.execute("DELETE FROM Bidding WHERE 1=1");
 			s.execute("DELETE FROM Auction WHERE 1=1");
 			s.execute("DELETE FROM Username WHERE 1=1");
 		} catch (SQLException e) {
 			System.out.println("Could not clear DB (does not revert changes).");
 		}
 		close(c);
 	}
 	
 	public static void addAlert(String author, int auctionID, String msg) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("INSERT INTO alert VALUES (DEFAULT, (SELECT username FROM username WHERE username=?), ?, ?)");
 			ps.setString(1, author);
 			ps.setInt(2, auctionID);
 			ps.setString(3, msg);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not add alert.");
 		}
 		close(c);
 	}
 	
 	public static AlertListBean getAlerts(String author) {
 		Connection c = null;
 
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("SELECT * FROM alert WHERE author=(SELECT username FROM username WHERE username=?)");
 			ps.setString(1, author);
 			ResultSet rs = ps.executeQuery();
 			AlertListBean alb = new AlertListBean();
 			while (rs.next()) {
 				close(c);	
 				alb.addAlert(new AlertBean(
 						rs.getInt("id"),
 						rs.getString("author"),
 						rs.getInt("auction"),
 						rs.getString("message")));
 			}
 			return alb;
 		} catch (SQLException e) {
 			System.out.println("Could not get auctions.");
 		}
 		close(c);
 		return null;
 	}
 	
 	public static void removeAlert(int id, String author) {
 		Connection c = null;
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement("DELETE FROM alert WHERE id=? AND author=?");
 			ps.setInt(1, id);
 			ps.setString(2, author);
 			ps.execute();
 		} catch (SQLException e) {
 			System.out.println("Could not remove alert.");
 		}
 		close(c);
 	}
 	
 	public static String[] getUserEmailsFromWA(int id) {
 		Connection c = null;
 		String query = 	"SELECT au.email_address as aemail, bu.email_address as bemail FROM winningauction wa " +
 						"INNER JOIN auction a ON wa.auction=a.id " +
 						"INNER JOIN username au ON au.username=a.author " +
 						"INNER JOIN bidding b ON wa.bid=b.id " +
 						"INNER JOIN username bu ON bu.username=b.author AND wa.id=?";
 		String[] emailArr = new String[2];
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				emailArr[0] = rs.getString("aemail");
 				emailArr[1] = rs.getString("bemail");
 			}
 		} catch (SQLException e) {
 			System.out.println("Could not get userbean.");
 		}
 		close(c);
 		return emailArr;
 	}
 	
 	public static String getOwnerEmailFromAuction(int id) {
 		Connection c = null;
 		String query = 	"SELECT u.email_address AS email FROM auction a " +
 						"INNER JOIN username u ON a.author=u.username AND a.id=?";
 		try {
 			c = connect();
 			PreparedStatement ps = c.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				return rs.getString("email");
 		} catch (SQLException e) {
 			System.out.println("Could not get email from auction.");
 		}
 		close(c);
 		return "";
 	}
 	
 }
