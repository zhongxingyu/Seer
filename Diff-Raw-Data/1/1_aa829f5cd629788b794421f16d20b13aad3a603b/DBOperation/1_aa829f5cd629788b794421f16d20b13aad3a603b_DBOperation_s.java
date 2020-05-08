 package groupone;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpSession;
 import javax.swing.DefaultListModel;
 
 public class DBOperation {
 	
 	  
 	public static boolean isValidLogin(String email, String pass) {
 		email = email.toUpperCase();
 		boolean found = false;
 		Connection con = new DBConnection().getDBConnection();
 		String sqlCmd = "SELECT email FROM account "
 						+ "WHERE email = '" + email + "' "
 						+ "AND password = '" + pass + "'";
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			while (rs.next()) {
 				found = true;
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		
 		return found;
 	}
 	
 	public static boolean createAccount(String firstName, String lastName, String email, String pass, String type) {
 		firstName = firstName.toUpperCase();
 		lastName = lastName.toUpperCase();
 		email = email.toUpperCase();
 		type = type.toUpperCase();
 		
 		Connection con = new DBConnection().getDBConnection();
 		//INSERT INTO `groupone`.`account` (`email`, `password`, `firstName`, `lastName`, `acctType`) VALUES ('kvraymundo@gmail.com', '54321', 'Kev', 'Ray', 'C');
 		String sqlCmd = "INSERT INTO account (email, password, firstName, lastName, acctType) "
 						+ "VALUES ('" + email + "', '" + pass + "', '" + firstName + "', '" + lastName + "', '" + type + "')";
 		try {
 			Statement stmt = con.createStatement();
 			int rs = stmt.executeUpdate(sqlCmd);
 
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean isVendor(String email) {
 		email = email.toUpperCase();
 		boolean found = false;
 		Connection con = new DBConnection().getDBConnection();
 		String sqlCmd = "SELECT acctType FROM account "
 						+ "WHERE email = '" + email + "' "
 						+ "AND acctType = 'V'";
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			while (rs.next()) {
 				found = true;
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		
 		return found;
 	}
 
 	public static ArrayList<Coupon> getCouponList() {
 		Connection con = new DBConnection().getDBConnection();
 		ArrayList<Coupon> coupons = new ArrayList<Coupon>();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM coupon");
 			
 			// Get result set meta data
 		    ResultSetMetaData rsmd = rs.getMetaData();
 		    int numColumns = rsmd.getColumnCount();
 		    
 			while(rs.next()) {
 				Coupon coupon = new Coupon();
 				coupon.setId(rs.getString(1));
 				coupon.setTitle(rs.getString(2));
 				coupon.setCreateDate(rs.getString(3));
 				coupon.setExpireDate(rs.getString(4));
 				coupon.setQuantity(rs.getString(5));
 				coupon.setPrice(rs.getString(6));
 				coupon.setCategory(rs.getString(7));
 				coupon.setSold(rs.getString(8));
 				coupon.setVendor(rs.getString(9));
				coupon.setImage(rs.getString(10));
 				coupons.add(coupon);
 			}
 			
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {}
 		
 		return coupons;
 	}
 	
 	public static ArrayList<Account> getAccountList() {
 		Connection con = new DBConnection().getDBConnection();
 		ArrayList<Account> accounts = new ArrayList<Account>();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM account");
 			
 			// Get result set meta data
 		    ResultSetMetaData rsmd = rs.getMetaData();
 		    int numColumns = rsmd.getColumnCount();
 		    
 			while(rs.next()) {
 				Account account = new Account();
 				account.setId(rs.getString(1));
 				account.setEmail(rs.getString(2));
 				account.setPassword(rs.getString(3));
 				account.setFirstName(rs.getString(4));
 				account.setLastName(rs.getString(5));
 				
 				accounts.add(account);
 			}
 		} catch(SQLException e) {}
 		
 		return accounts;
 	}
 	
 	public static Account getAccount(String email, String pass) {
 		email = email.toUpperCase();
 		Connection con = new DBConnection().getDBConnection();
 		Account account = new Account();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM account WHERE email = '" + email + "' AND password = '" + pass + "'");
 			
 			// Get result set meta data
 		    ResultSetMetaData rsmd = rs.getMetaData();
 		    int numColumns = rsmd.getColumnCount();
 		    
 			while(rs.next()) {
 				account.setId(rs.getString(1));
 				account.setEmail(rs.getString(2));
 				account.setPassword(rs.getString(3));
 				account.setFirstName(rs.getString(4));
 				account.setLastName(rs.getString(5));
 			}
 		} catch(SQLException e) {}
 		
 		return account;
 	}
 	
 	public static void disableCoupon(String couponId) {
 		Connection con = new DBConnection().getDBConnection();
 		
 		try {
 			Statement stmt = con.createStatement();
 			
 			stmt.executeUpdate("UPDATE coupon SET quantity=0, idVendor=0 WHERE idCoupon = '" + couponId + "'");
 
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	public static void deleteCoupon(String couponId) {
 		Connection con = new DBConnection().getDBConnection();
 		Account account = new Account();
 		
 		try {
 			Statement stmt = con.createStatement();
 			int rs = stmt.executeUpdate("DELETE FROM coupon WHERE idCoupon = '" + couponId + "'");
 
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	public static Coupon searchCoupon(String couponId) {
 		Connection con = new DBConnection().getDBConnection();
 		Coupon coupon = new Coupon();
 		String sqlCmd = "SELECT * FROM coupon WHERE idCoupon = '" + couponId + "'";
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			while(rs.next()) {
 				coupon.setId(rs.getString(1));
 				coupon.setTitle(rs.getString(2));
 				coupon.setCreateDate(rs.getString(3));
 				coupon.setExpireDate(rs.getString(4));
 				coupon.setQuantity(rs.getString(5));
 				coupon.setPrice(rs.getString(6));
 				coupon.setCategory(rs.getString(7));
 				coupon.setSold(rs.getString(8));
 				coupon.setVendor(rs.getString(9));
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {}
 		
 		return coupon;
 	}
 	
 	public static ArrayList<Coupon> searchCoupon(String[] couponIds) {
 		Connection con = new DBConnection().getDBConnection();
 		ArrayList<Coupon> coupons = new ArrayList<Coupon>();
 		StringBuilder sqlCmd = new StringBuilder();
 		
 		try {
 			Statement stmt = con.createStatement();
 			
 			for(int i=0; i<couponIds.length; i++) {
 				sqlCmd.delete(0, sqlCmd.length());
 				sqlCmd.append("SELECT * FROM coupon WHERE idCoupon = '" + couponIds[i] + "'");
 				ResultSet rs = stmt.executeQuery(sqlCmd.toString());
 				
 				while(rs.next()) {
 					Coupon coupon = new Coupon();
 					
 					coupon.setId(rs.getString(1));
 					coupon.setTitle(rs.getString(2));
 					coupon.setCreateDate(rs.getString(3));
 					coupon.setExpireDate(rs.getString(4));
 					coupon.setQuantity(rs.getString(5));
 					coupon.setPrice(rs.getString(6));
 					coupon.setCategory(rs.getString(7));
 					coupon.setSold(rs.getString(8));
 					coupon.setVendor(rs.getString(9));
 					coupon.setImage(rs.getString(10));
 					coupons.add(coupon);
 				}
 			}
 			
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {}
 		
 		return coupons;
 	}
 	
 	public static void updateCoupon(String[] couponIds) {
 		Connection con = new DBConnection().getDBConnection();
 		StringBuilder sqlCmd = new StringBuilder();
 		StringBuilder sqlCmd2 = new StringBuilder();
 		
 		try {
 			Statement stmt = con.createStatement();
 			
 			for(int i=0; i<couponIds.length; i++) {
 				int quantity = Integer.parseInt(searchCoupon(couponIds[i]).getQuantity());
 				int sold = Integer.parseInt(searchCoupon(couponIds[i]).getSold());
 				Integer newQuantity = quantity - 1;
 				Integer newSold = sold + 1;
 				
 				sqlCmd.delete(0, sqlCmd.length());
 				sqlCmd2.delete(0, sqlCmd2.length());
 				sqlCmd.append("UPDATE coupon SET quantity = " + "'" + newQuantity.toString() + "'" +
 						"WHERE idCoupon = " + "'" + couponIds[i] + "'");
 				sqlCmd2.append("UPDATE coupon SET sold = " + "'" + newSold.toString() + "'" +
 						"WHERE idCoupon = " + "'" + couponIds[i] + "'");
 				stmt.executeUpdate(sqlCmd.toString());
 				stmt.executeUpdate(sqlCmd2.toString());
 			}
 			
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {}
 	}
 	
 	public static boolean createTransaction(String accountId, String userEmail, String[] couponIds, boolean gift) {
 		Connection con = new DBConnection().getDBConnection();
 		StringBuilder sqlCmd = new StringBuilder();
 		
 		accountId = accountId.toUpperCase();		
 		userEmail = userEmail.toUpperCase();
 		String type = "SELF";
 		
 		if(gift) {
 			type = "GIFT";
 		}
 		
 		try {
 			Statement stmt = con.createStatement();
 			
 			for(int i=0; i<couponIds.length; i++) {
 				sqlCmd.delete(0, sqlCmd.length());
 				sqlCmd.append("INSERT into transaction (idTransAcct, idTransCoup, type, email) VALUES ('"
 						 + accountId + "', '" + couponIds[i] + "', '" + type + "', '" + userEmail + "')");
 				stmt.executeUpdate(sqlCmd.toString());
 			}
 			
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static ArrayList<Transaction> getTransactionList() {
 		Connection con = new DBConnection().getDBConnection();
 		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM transaction");
 			
 			while(rs.next()) {
 				Transaction trans = new Transaction();
 				trans.setIdTransaction(rs.getString(1));
 				trans.setIdTransAcct(rs.getString(2));
 				trans.setIdTransCoup(rs.getString(3));
 				trans.setDate(rs.getString(4));
 				trans.setType(rs.getString(5));
 				trans.setEmail(rs.getString(6));
 				
 				transactions.add(trans);
 			}
 			
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {}
 		
 		return transactions;
 	}
 	
 	public static ArrayList<String> queryToArrayList(String sqlCmd) {
 		Connection con = new DBConnection().getDBConnection();
 		ArrayList<String> list = new ArrayList<String>();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			// Get result set meta data
 		    ResultSetMetaData rsmd = rs.getMetaData();
 		    int numColumns = rsmd.getColumnCount();
 		    
 			while(rs.next()) {
 				for (int i=1; i<numColumns+1; i++) {
 					list.add(rs.getString(i));
 			    }
 			}
 		} catch(SQLException e) {}
 		
 		return list;
 	}
 	
 	
 	
 	public static DefaultListModel<String> queryToDefaultListModel(String sqlCmd) {
 		Connection con = new DBConnection().getDBConnection();
 		DefaultListModel<String> list = new DefaultListModel<String>();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			// Get result set meta data
 		    ResultSetMetaData rsmd = rs.getMetaData();
 		    int numColumns = rsmd.getColumnCount();
 		    
 			while(rs.next()) {
 				for (int i=1; i<numColumns+1; i++) {
 					list.add(i - 1, rs.getString(i));
 			    }
 			}
 		} catch(SQLException e) {}
 		
 		return list;
 	}
 	
 	public static void updateQueryList(String query, DefaultListModel<String> list) {
 		Connection con = new DBConnection().getDBConnection();
 		list.clear();
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 			
 			// Get result set meta data
 		    ResultSetMetaData rsmd = rs.getMetaData();
 		    int numColumns = rsmd.getColumnCount();
 		    
 			while(rs.next()) {
 				for (int i=1; i<numColumns+1; i++)
 					list.add(i - 1, rs.getString(i));
 			}
 		} catch(SQLException e) {}
 	}
 
 	
 
 	public static int getRowCount(String query) {
 		Connection con = new DBConnection().getDBConnection();
 		int count = 0;
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM (" + query + ")");
 		    
 			while(rs.next()) {
 				count = Integer.parseInt(rs.getString(1));
 			}
 		} catch(SQLException e) {}
 		return count;
 	}
 
 
 	public static void insertData(String update) {
 		Connection con = new DBConnection().getDBConnection();
 		try {
 			Statement stmt = con.createStatement();
 			int rs = stmt.executeUpdate(update);
 			
 			stmt.close();
 		    con.close();
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	public static boolean addCoupon(String title, String date, String quantity, String price, String category, String vendorId)
 	{
 
 		
 		Connection con = new DBConnection().getDBConnection();
 		
 		String sqlCmd = "INSERT INTO coupon (title, expiredate, quantity, price, category, idVendor)"
 				+ "VALUES ('" + title + "', '" + date + "', '" + quantity + "', '" + price + "', '" + category + "', '" + vendorId + "')";
 		try 
 		{
 			Statement stmt = con.createStatement();
 			int rs = stmt.executeUpdate(sqlCmd);
 
 			stmt.close();
 			con.close();
 			
 		} 
 		catch(SQLException e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		return true;
 		
 	}
 	
 	public static boolean emailExists(String email)
 	{
 		Connection con = new DBConnection().getDBConnection();
 		boolean found = false;
 		String sqlCmd = "SELECT" + "'" + email + "' " + "FROM account WHERE email = '" + email + "'";
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			while (rs.next()) {
 				found = true;
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 			
 		} catch(SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		
 		return found;
 	}
 	
 	public static boolean resetPassword(String email, String newPassword)
 	{
 		Connection con = new DBConnection().getDBConnection();
 		
 		String sqlCmd = "UPDATE account SET password = '" + newPassword + "' WHERE email = '" + email + "'";
 		try 
 		{
 			Statement stmt = con.createStatement();
 			int rs = stmt.executeUpdate(sqlCmd);
 
 			stmt.close();
 			con.close();
 			
 		} 
 		catch(SQLException e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean changeEmail(String currentEmail, String newEmail)
 	{
 		Connection con = new DBConnection().getDBConnection();
 		
 		String sqlCmd = "UPDATE account SET email = " + "'" + newEmail + "'" +
 				"WHERE email = " + "'" + currentEmail + "'";
 		
 		try 
 		{
 			Statement stmt = con.createStatement();
 			int rs = stmt.executeUpdate(sqlCmd);
 
 			stmt.close();
 			con.close();
 			
 		} 
 		catch(SQLException e) 
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean checkPassword(String userEmail, String currentPassword)
 	{
 		Connection con = new DBConnection().getDBConnection();
 		boolean found = false;
 		String sqlCmd = "SELECT" + "'" + currentPassword + "' " + "FROM account WHERE email = '" + userEmail + "'";
 		
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sqlCmd);
 			
 			while (rs.next()) 
 			{
 				found = true;
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 			
 		} 
 		catch(SQLException e) 
 		{
 			System.out.println(e.getMessage());
 		}
 		
 		return found;
 	}
 	
 }
 
 
 
 
 
