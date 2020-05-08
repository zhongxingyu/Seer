 package edu.sjsu.videolibrary.db;
 
 import java.sql.*;
 import java.util.Date;
 import java.util.LinkedList;
 
 import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
 
 import edu.sjsu.videolibrary.model.Transaction;
 import edu.sjsu.videolibrary.db.SimpleAdminDAO;
 import edu.sjsu.videolibrary.exception.InternalServerException;
 import edu.sjsu.videolibrary.exception.InvalidCreditCardException;
 import edu.sjsu.videolibrary.exception.UserAlreadyExistsException;
 import edu.sjsu.videolibrary.model.StatementInfo;
 import edu.sjsu.videolibrary.model.User;
 import edu.sjsu.videolibrary.util.Utils;
 
 public class SimpleUserDAO extends BaseUserDAO {
 
 	public SimpleUserDAO() {
 		super();
 	}
 
 	public SimpleUserDAO(VideoLibraryDAO dao) {
 		super(dao);
 	}
 
 	public String signUpUser(String userId, String password, String memType,
 			String firstName, String lastName, String address, String city,
 			String state, String zipCode, String ccNumber) throws SQLException, UserAlreadyExistsException {
 		System.out.println("In signUp of userDAO");
 		String result = "false";
 		// System.out.println("State:"+state);
 		//boolean userExists = checkUserId(userId);
 		System.out.println("Returned from checkUserId");
 		
 			System.out.println("User does not exist. So creating new");
 			String sql = "INSERT INTO user (userId,password,membershipType,startDate,firstName,lastName,"
 				+ "address,city,state,zip,creditCardNumber,latestPaymentDate) VALUES ('"
 				+ userId
 				+ "','"
 				+ Utils.encryptPassword(password)
 				+ "','"
 				+ memType
 				+ "',NOW(),'"
 				+ firstName
 				+ "','"
 				+ lastName
 				+ "','"
 				+ address
 				+ "','"
 				+ city
 				+ "','"
 				+ state
 				+ "','"
 				+ zipCode
 				+ "','" + ccNumber + "'," + null + ")";
 
 		try {
 			int rc = stmt.executeUpdate(sql);
 
 			if (rc > 0) {
 				result = "true";
 			} 
 		} catch (MySQLIntegrityConstraintViolationException e) {
 			throw new UserAlreadyExistsException("User already exists");
 		}
 		catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		return result;
 	}
 	
 	public String signUpAdmin(String userId, String password, String firstName,
 			String lastName) throws SQLException, UserAlreadyExistsException {
 		String result = null;
 		String sql = "INSERT INTO admin (userId,password,firstName,lastName) VALUES ('"
 				+ userId
 				+ "','"
 				+ Utils.encryptPassword(password)
 				+ "','"
 				+ firstName + "','" + lastName + "')";
 		try {
 			int rc = stmt.executeUpdate(sql);
 			if (rc > 0) {
 				result = "true";
 			} else {
 				result = "false";
 			}
 		} catch (MySQLIntegrityConstraintViolationException e) {
 			throw new UserAlreadyExistsException("Admin user already exists");
 		}
 		return result;
 	}
 
 	public User signInUser(String userId, String password) throws SQLException {
 
 		User user = new User();
 		String encryptedPasswrd = Utils.encryptPassword(password);
 		String sql = "SELECT membershipId,firstName,lastName,address,city,creditCardNumber,membershipType,state,zip,startDate,latestPaymentDate,userId, password FROM user WHERE userId = '"
 				+ userId + "'" + " AND password = '" + encryptedPasswrd + "'";
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		if (rs.next()) {
 			user.setMembershipId(rs.getInt("membershipId"));
 			user.setFirstName(rs.getString("firstName"));
 			user.setLastName(rs.getString("lastName"));
 			user.setAddress(rs.getString("address"));
 			user.setCity(rs.getString("city"));
 			String ccNumber = rs.getString("creditCardNumber");
 			if (ccNumber != null) {
 				user.setCreditCardNumber(ccNumber);
 			}
 			user.setMembershipType(rs.getString("membershipType"));
 			user.setState(rs.getString("state"));
 			user.setZip(rs.getString("zip"));
 			Date startDate = rs.getDate("startDate");
 			if (startDate != null) {
 				user.setStartDate(startDate.toString());
 			} else {
 				user.setStartDate(null);
 			}
 			Date latestPaymentDate = rs.getDate("latestPaymentDate");
 			if (latestPaymentDate != null) {
 				user.setStartDate(latestPaymentDate.toString());
 			} else {
 				user.setLatestPaymentDate(null);
 			}
 			user.setUserId(rs.getString("userId"));
 			user.setPassword(rs.getString("password"));
 		} else {
 			user = null;
 		}
 		return user;
 	}
 
 	public String signInAdmin(String userId, String password)
 			throws SQLException {
 		String result = null;
 		String sql = "SELECT userId, password FROM admin where userId = '"
 				+ userId + "'" + " AND password = '"
 				+ Utils.encryptPassword(password) + "'";
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		if (rs.next()) {
 			result = "true";
 		} else {
 			result = "false";
 		}
 		return result;
 	}
 
 	public LinkedList<Transaction> viewAccountTransactions(int membershipId)
 			throws Exception {
 		LinkedList<Transaction> ac = new LinkedList<Transaction>();
 
 		String query1 = "select Movie.MovieName,pymnt.RentDate,rnt.ReturnDate,User.MembershipType from "
 				+ " VideoLibrary.RentMovieTransaction rnt,VideoLibrary.PaymentTransaction pymnt, "
 				+ " VideoLibrary.Movie,VideoLibrary.User  where User.MembershipId = "
 				+ membershipId
 				+ " and pymnt.TransactionId = rnt.TransactionId "
 				+ " and rnt.MovieId = Movie.MovieId and pymnt.MembershipId = User.MembershipId";
 
 		ResultSet result1 = stmt.executeQuery(query1);
 
 		while (result1.next()) {
 			Transaction trans = new Transaction();
 			trans.setMovieName(result1.getString("MovieName"));
 			String membershipType = result1.getString("MembershipType");
 			if (membershipType.equalsIgnoreCase("Simple")) {
 				trans.setPerMovieAmount(1.5);
 			} else {
 				trans.setPerMovieAmount(0.0);
 			}
 			trans.setPurchaseDate((result1.getDate("RentDate").toString()));
 			Date returnDate = result1.getDate("ReturnDate");
 			if (returnDate != null) {
 				trans.setReturnDate((returnDate).toString());
 			} else {
 				trans.setReturnDate("Not Returned");
 			}
 
 			ac.add(trans);
 		}
 
 		return ac;
 	}
 
 	public String makeMonthlyPayment(int membershipId) throws Exception {
 		String result = null;
 
 		String query1 = "UPDATE VideoLibrary.User SET latestPaymentDate = NOW() "
 				+ "WHERE User.membershipId = " + membershipId;
 		int rowcount1 = stmt.executeUpdate(query1);
 		if (rowcount1 < 0) {
 			result = "false";
 			return result;
 		}
 
 		SimpleAdminDAO adminDAO = new SimpleAdminDAO();
 		double monthlyFees = adminDAO.getMonthlyFeesForPremiumMember();
 
 		String query2 = "INSERT INTO VideoLibrary.PaymentTransaction(rentDate,totalDueAmount,membershipId)"
 				+ " VALUES (NOW()," + monthlyFees + "," + membershipId + ")";
 		int rowcount2 = stmt.executeUpdate(query2);
 
 		if (rowcount2 > 0) {
 			result = "true";
 			System.out.println("Payment Successful");
 		} else {
 			System.out.println("Payment could not be proceeded.");
 			result = "false";
 		}
 
 		return result;
 	}
 
 	public String updateUserInfo(int membershipId, String userId,
 			String firstName, String lastName, String address, String city,
 			String state, String zipCode, String membershipType,
 			String creditCardNumber) throws Exception {
 
 		String result = null;
 
 		String query1 = "update VideoLibrary.User set userId = '" + userId
 				+ "' ,membershipType = '" + membershipType + " ',firstName = '"
 				+ firstName + "' ,lastName = '" + lastName + "',address = '"
 				+ address + "',city = '" + city + "',state = '" + state
 				+ "',zip = '" + 98567 + "' ,creditCardNumber = '"
 				+ creditCardNumber + " ' where membershipId = " + membershipId;
 
 		int rowcount = stmt.executeUpdate(query1);
 
 		if (rowcount > 0) {
 			result = "true";
 			System.out.println("Update Successful");
 		} else {
 			System.out.println("Update unsuccessful.");
 			result = "false";
 		}
 
 		return result;
 
 	}
 
 	public String updatePassword(int membershipId, String oldPassword,
 			String newPassword) throws Exception {
 		String result = null;
 
 		String query1 = "UPDATE VideoLibrary.User SET password = '"
 				+ Utils.encryptPassword(newPassword)
 				+ "' WHERE membershipId = " + membershipId
 				+ " AND password = '" + Utils.encryptPassword(oldPassword)
 				+ "'";
 
 		int rowcount = stmt.executeUpdate(query1);
 
 		if (rowcount > 0) {
 			result = "true";
 			System.out.println("Update Successful");
 		} else {
 			System.out.println("Update unsuccessful.");
 			result = "invalidOldPassword";
 		}
 
 		return result;
 	}
 
 	public LinkedList<StatementInfo> viewStatement(int membershipId, int month,
 			int year) throws Exception {
 		LinkedList<StatementInfo> statementRows = new LinkedList<StatementInfo>();
 
 		// TODO: Need to check the month is not earlier than user join date
 
 		String query1 = "SELECT statementId FROM VideoLibrary.Statement WHERE statement.month = "
 				+ month
 				+ " AND statement.year = "
 				+ year
 				+ " AND statement.membershipId = " + membershipId;
 		ResultSet result1 = stmt.executeQuery(query1);
 		if (result1.next()) {
 			result1.getInt("statementId");
 		} else {
 			SimpleAdminDAO admin = new SimpleAdminDAO();
 			admin.generateMonthlyStatement(membershipId, month, year);
 		}
 
 		String query2 = "SELECT pymnt.rentDate,pymnt.totaldueAmount,movie.movieName,rnt.returnDate"
 				+ " FROM VideoLibrary.RentMovieTransaction rnt,VideoLibrary.PaymentTransaction pymnt,VideoLibrary.Movie,"
 				+ " VideoLibrary.StatementTransactions,VideoLibrary.Statement WHERE StatementTransactions.transactionId = "
 				+ " pymnt.transactionId AND pymnt.transactionId = rnt.transactionId AND rnt.movieId = Movie.movieId "
 				+ " AND Statement.statementId = StatementTransactions.statementId AND month = "
 				+ month
 				+ " AND year = "
 				+ year
 				+ " and Statement.membershipId = " + membershipId;
 
 		// System.out.println(query2);
 
 		ResultSet result2 = stmt.executeQuery(query2);
 
 		while (result2.next()) {
 			StatementInfo stmnt = new StatementInfo();
 			stmnt.setMovieName(result2.getString("movieName"));
 			stmnt.setTotalDueAmount(result2.getDouble("totalDueAmount"));
 			stmnt.setRentDate(result2.getDate("rentDate").toString());
 			Date returnDate = result2.getDate("returnDate");
 			if (returnDate == null) {
 				stmnt.setReturnDate("Not returned");
 			} else {
 				stmnt.setReturnDate(returnDate.toString());
 			}
 			statementRows.add(stmnt);
 
 		}
 		return statementRows;
 	}
 
 	public User queryMembershipType(int membershipId) {
 		String query = "SELECT membershipType, rentedMovies,creditCardNumber FROM videolibrary.user WHERE membershipId = "
 				+ membershipId;
 		Statement stmt = null;
 		User user = new User();
 		try {
 			stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 
 			while (rs.next()) {
 				user.setMembershipType(rs.getString("membershipType"));
 				user.setRentedMovies(rs.getInt("rentedMovies"));
 				user.setCreditCardNumber(rs.getString("creditCardNumber"));
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		return user;
 	}
 
 	public void updateRentedMoviesForUser(int membershipId, int rentedMovies)
 			throws InternalServerException {
 		String query = "UPDATE videoLibrary.user SET rentedMovies = "
 				+ rentedMovies + " WHERE membershipId = " + membershipId;
 		System.out.println("Update query for updateRentedMovies = " + query);
 		Statement stmt = null;
 		try {
 			stmt = con.createStatement();
 			int rowCount = stmt.executeUpdate(query);
 			if (rowCount > 0) {
 				System.out
 						.println("Update of rentedMovies in user table successful");
 			} else {
 				throw new InternalServerException(
 						"Unsuccessful Update for rented movies for this user");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String queryCreditCardNumber(int membershipId)
 			throws InvalidCreditCardException {
 		User user = queryMembershipType(membershipId);
		if (user.getMembershipType().equals("Premium")) {
 			user.getCreditCardNumber();
 		} else {
 			throw new InvalidCreditCardException("Invalid credit card");
 		}
 		return user.getCreditCardNumber();
 	}
 
 }
