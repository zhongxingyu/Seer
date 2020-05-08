 package edu.sjsu.videolibrary.db;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import edu.sjsu.videolibrary.exception.*;
 import edu.sjsu.videolibrary.model.Admin;
 import edu.sjsu.videolibrary.model.Movie;
 import edu.sjsu.videolibrary.model.PaymentForPremiumMemInfo;
 import edu.sjsu.videolibrary.model.User;
 import edu.sjsu.videolibrary.util.Utils;
 
 public class SimpleAdminDAO extends BaseAdminDAO {
 
 	public SimpleAdminDAO() {
 		super();
 	}
 
 	public User displayUserInformation (int membershipId){
 		try{
 
 			String query1 = "select user.FirstName,user.LastName,user.StartDate,"+
 					"user.MembershipType,user.MembershipId,user.userId,user.Password,user.Address,user.City,"+
 					"user.State,user.Zip,user.CreditCardNumber,user.latestPaymentDate from VideoLibrary.user"+
 					" where user.MembershipId = "+ membershipId;
 
 			rs = stmt.executeQuery(query1);
 			User user = new User();
 			
 			if(rs.next()){
 				user.setFirstName(rs.getString("FirstName"));
 				user.setLastName(rs.getString("LastName"));
 				user.setUserId(rs.getString("UserId"));
 				user.setPassword(rs.getString("Password"));
 				user.setMembershipType(rs.getString("MembershipType"));
 				user.setMembershipId(rs.getInt("MembershipId"));
 				user.setAddress(rs.getString("Address"));
 				user.setCity(rs.getString("City"));
 				user.setState(rs.getString("State"));
 				user.setZip(rs.getString("Zip"));
 				user.setCreditCardNumber(rs.getString("CreditCardNumber"));
 				Date latestPaymentDate = rs.getDate("latestPaymentDate");
 				if(latestPaymentDate !=null){
 					user.setLatestPaymentDate(rs.getDate("latestPaymentDate").toString());
 				}
 				else{
 					user.setLatestPaymentDate(null);
 				}
 			
 
 				String query2 = "select Movie.MovieName from VideoLibrary.RentMovieTransaction rm,VideoLibrary.Movie, VideoLibrary.PaymentTransaction pymnt "+
						"where pymnt.TransactionId = rm.TransactionId and ReturnDate is null and rm.MovieId = Movie.MovieId and rm.MembershipId = "+membershipId;
 				ResultSet result2 = stmt.executeQuery(query2);
 	
 				LinkedList<String> movies = new LinkedList<String>();
 	
 				while(result2.next()){				
 					movies.add(result2.getString("MovieName"));
 				}	
 				String movies1[] = movies.toArray(new String[0]);
 				user.setMovieList(movies1);
 				return user;
 			}
 			else{
 				user = null;
 				return user;
 			}
 
 		}
 		catch(SQLException e){
 			e.getMessage();
 			return null;
 		}
 		catch(Exception e){
 			e.getMessage();
 			return null;
 		}
 	}
 
 	public Movie displayMovieInformation (int movieId){
 
 		try{
 			double rentAmount = getRentAmountforMovie();
 			String query1 = "SELECT Movie.MovieId,Movie.MovieName,Movie.MovieBanner,Movie.ReleaseDate, "+
 					" Movie.AvailableCopies,Category.CategoryName FROM VideoLibrary.Movie,VideoLibrary.Category" + 
 					" WHERE Movie.MovieId = "+movieId+" AND Movie.CategoryId = Category.CategoryId";
 
 			ResultSet result1 = stmt.executeQuery(query1);
 			Movie movie = new Movie();
 
 			if(result1.next()){
 				movie.setMovieId(result1.getInt("MovieId"));
 				movie.setMovieName(result1.getString("MovieName"));
 				movie.setMovieBanner(result1.getString("MovieBanner"));
 				movie.setReleaseDate(result1.getDate("ReleaseDate").toString());
 				movie.setCatagory(result1.getString("CategoryName"));
 				movie.setAvailableCopies(result1.getInt("AvailableCopies"));
 				movie.setRentAmount(rentAmount);
 			
 
 				String query2 = "select User.FirstName,User.LastName from VideoLibrary.User,VideoLibrary.PaymentTransaction,"+
 						" VideoLibrary.RentMovieTransaction where RentMovieTransaction.MovieId = 1 and  RentMovieTransaction.ReturnDate "+
 						"is null and  RentMovieTransaction.TransactionId = PaymentTransaction.TransactionId"+
 						" and PaymentTransaction.MembershipId = User.MembershipId";
 	
 				ResultSet result2 = stmt.executeQuery(query2);
 				LinkedList<String> buyerList = new LinkedList<String>();
 	
 				while(result2.next()){
 					String fName = result2.getString("FirstName");
 					String lName = result2.getString("LastName");
 					String fullName = fName+" "+lName;				
 					buyerList.add(fullName);				
 				}
 	
 				String buyerList1[] = buyerList.toArray(new String[0]);
 				movie.setBuyerList(buyerList1);
 				
 			}
 			else
 				movie=null;
 			return movie;
 		}
 		catch(SQLException e){
 			e.getMessage();
 			return null;
 		}
 		catch(Exception e){
 			e.getMessage();
 			return null;
 		}
 	}
 
 	public double getRentAmountforMovie(){
 		double rentAmount = 0.0;
 		try{
 
 			String query1 = "select amount from VideoLibrary.AmountDetails where membershipType = 'simple' "+
 					" order by feesUpdateDate desc limit 1";
 
 			ResultSet result1 = stmt.executeQuery(query1);
 
 			if(result1.next()){
 				rentAmount = result1.getDouble("amount");
 			}
 			
 		}
 		catch(SQLException e){
 			e.getMessage();
 			rentAmount = -1.0;
 		}
 
 		catch(Exception e){
 			e.getMessage();
 			rentAmount = -1.0;
 		}
 		return rentAmount;
 	}
 
 	public double getMonthlyFeesForPremiumMember(){
 
 		double monthlyFees = 0.0;
 		try{
 
 			String query1 = "select amount from VideoLibrary.AmountDetails where membershipType = 'premium' "+
 					"order by feesUpdateDate desc limit 1";
 
 			ResultSet result1 = stmt.executeQuery(query1);
 
 			if(result1.next()){
 				monthlyFees = result1.getDouble("amount");
 			}
 		}
 		catch(SQLException e){
 			e.getMessage();
 			monthlyFees = -1.0;
 		}
 
 		catch(Exception e){
 			e.getMessage();
 			monthlyFees = -1.0;
 		}
 		return monthlyFees;
 	}
 
 	public String updateMovieInfo(int movieId,String movieName, String movieBanner, String releaseDate, int availableCopies, int categoryId){
 		String result = null;
 		try{
 			String query1 = "update VideoLibrary.Movie set movieName = '"+movieName+"',movieBanner = '"+movieBanner+"',releaseDate = '"+releaseDate+
 					"', availableCopies = "+availableCopies+",categoryId = "+categoryId+ " where movieId = "+movieId;
 
 			int rowcount = stmt.executeUpdate(query1);
 
 			if(rowcount>0){
 				result = "true";
 				System.out.println("Update Successful");
 			}
 			else{
 				System.out.println("Update unsuccessful.");
 				result = "false";
 			}
 		}
 		catch(SQLException e){
 			e.getMessage();
 			result = "false";
 		}
 		catch(Exception e){
 			e.getMessage();
 			result = "false";
 		}
 		return result;
 	}
 
 	public String generateMonthlyStatement(int membershipId,int month,int year) throws SQLException{
 		String result = null;
 		int statementId = 0;
 		boolean processComplete = false;
 		// TODO: Need to check the month is not earlier than user join date
 		try {
 			con.setAutoCommit(false);
 			String query1 = "select pymnt.transactionId from VideoLibrary.PaymentTransaction pymnt "+
 					" where extract(month from pymnt.rentDate) = "+month+" and extract(year from pymnt.rentDate) = "+year+
 					" and pymnt.membershipId = "+membershipId;
 
 			ResultSet result1 = stmt.executeQuery(query1);
 			LinkedList<Integer> listOfTransId = new LinkedList<Integer>();
 			while(result1.next()){
 				listOfTransId.add(result1.getInt("transactionId"));
 			}
 			String query2 = "insert into VideoLibrary.Statement(month,year,membershipId) "+
 					" value("+month+","+year+","+membershipId+")";
 			int rowCount = stmt.executeUpdate(query2, Statement.RETURN_GENERATED_KEYS);
 			if(rowCount>0){
 				ResultSet result2 = stmt.getGeneratedKeys();
 				result2.next();
 				statementId = result2.getInt(1);
 			}
 			else{
 				result = null;
 				return result;
 			}
 			for(Integer lst: listOfTransId){
 				String query = "insert into VideoLibrary.StatementTransactions(statementId,TransactionId)"+
 						" value("+statementId+","+lst+")";
 				int rowcount = stmt.executeUpdate(query);
 				if(rowcount<0){
 					result = "false";
 					return result;
 				}
 			}
 			processComplete = true;
 		} finally {
 			if ( processComplete ) {
 				con.commit();
 			} else {
 				con.rollback();
 			}
 			con.setAutoCommit(true);
 		}
 		result = "true";
 		return result;
 	}
 
 	//Moved from UserDAO 
 	public String deleteUser (String userId) {
 		String result = ""; 
 		try {
 			String sql = "DELETE from user WHERE membershipId  ="+userId;
   			int rowcount = stmt.executeUpdate(sql);
 
 			if(rowcount>0){
 				result = "delete true";
 				System.out.println("Delete Successful");
 			}
 			else{
 				System.out.println("Delete unsuccessful.");
 				result = "delete false";
 			}
 		} catch (SQLException e) { result = "error"; } 
 		return result;		
 	}
 
 	public PaymentForPremiumMemInfo generateMonthlyBillForPremiumMember(int membershipId,int month,int year){
 		PaymentForPremiumMemInfo pymnt = new PaymentForPremiumMemInfo();
 		try{
 			String query1 = "select * from  VideoLibrary.PaymentTransaction pymnt where pymnt.membershipId = "+
 		membershipId+" AND pymnt.transactionId not in( select rnt.transactionid from VideoLibrary.RentMovieTransaction rnt group by rnt.transactionid )";
 		
 		ResultSet result = stmt.executeQuery(query1);
 		
 		if(result.next()){
 			Date paymentDate = result.getDate("rentDate");
 			if(paymentDate == null){
 				pymnt.setPaymentDate("None");				
 			}
 			else{
 				pymnt.setPaymentDate(paymentDate.toString());
 			}
 			pymnt.setMonthlyPaymentAmount(result.getDouble("totalDueAmount"));
 			pymnt.setPaymentStatus("Payment Received");
 		}
 		else{
 			pymnt.setPaymentStatus("Payment not received");
 			pymnt = null;
 		}
 		}
 		catch(SQLException e){
 			e.getMessage();
 			pymnt = null;
 		}
 		catch(Exception e){
 			e.getMessage();
 			pymnt = null;
 		}
 		return pymnt;
 	}
 
 	public String deleteAdmin (String userId) {
 
 		//SuperAdmin should not be removed from the Database
 		if (!userId.equals("Admin")) {
 			//if (Integer.parseInt(userId) != 1) {	
 			try {
 				String sql = "DELETE FROM admin WHERE userId = '" + userId + "'";
 				System.out.println(sql);
 				
 				int rowcount = stmt.executeUpdate(sql);
 				if (rowcount > 0) {
 					return "delete true";
 				}    
 			} catch (SQLException e) { e.printStackTrace(); } 
 		} else { 
 			return "superadmin"; 
 		}
 		return "false";		
 	}
 
 
 	public List <User> listMembers (String type){		
 		
 		List <User> members = new ArrayList<User>();
 		
 		String query = ""; 
 
 		if (type.equals("all")) {
 			query = "SELECT user.membershipId, user.userId, user.firstName, user.lastName FROM user";
 		} else { 
 			query = "SELECT user.membershipId, user.userId, user.firstName, user.lastName FROM user WHERE user.membershipType = '" + type + "'"; 
 		}
 
 		try {
 			//preparedStmt = con.prepareStatement(query);
 			//System.out.println(preparedStmt.toString());
 			ResultSet rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				User member = new User();
 				member.setMembershipId(rs.getInt("membershipId"));
 				member.setUserId(rs.getString("userId"));
 				member.setFirstName(rs.getString("firstName"));
 				member.setLastName(rs.getString("lastName"));
 				members.add(member);
 			}
 		} catch (SQLException e) {
 			members = null;
 			e.printStackTrace();
 		}
 		//User test = (User) members.toArray()[0];
 		//System.out.println(test.getFirstName());
 		return members;
 	}
 	
 	
 	
 	public User[] searchUser(String membershipId, String userId,
 			String membershipType, String startDate, String firstName,
 			String lastName, String address, String city, String state,
 			String zipCode, int start, int stop) throws NoUserFoundException {
 
 		ArrayList<User> searchList = new ArrayList<User>();
 		User[] userArray = null;
 		Map<String, String> queryParameters = new HashMap<String, String>();
 		if (membershipId != null) {
 			queryParameters.put("membershipId", membershipId);
 		}
 		if (userId != null) {
 			queryParameters.put("userId", userId);
 		}
 		if (membershipType != null) {
 			queryParameters.put("membershipType", membershipType);
 		}
 		if (startDate != null) {
 			queryParameters.put("startDate", startDate);
 		}
 		if (firstName != null) {
 			queryParameters.put("firstName", firstName);
 		}
 		if (lastName != null) {
 			queryParameters.put("lastName", lastName);
 		}
 		if (address != null) {
 			queryParameters.put("address", address);
 		}
 		if (city != null) {
 			queryParameters.put("city", city);
 		}
 		if (state != null) {
 			queryParameters.put("state", state);
 		}
 		if (zipCode != null) {
 			queryParameters.put("zipCode", zipCode);
 		}
 
 		StringBuilder query = new StringBuilder("SELECT membershipId, userId, password, membershipType, startDate, firstName, lastName, address, city, state, zip, creditCardNumber, latestPaymentDate FROM videolibrary.user WHERE ");
 
 		Iterator<Entry<String, String>> paramIter = queryParameters.entrySet().iterator();
 		while (paramIter.hasNext()) {
 			Entry<String, String> entry = paramIter.next();
 			query.append(entry.getKey());
 			query.append(" LIKE '%").append(entry.getValue()).append("%'");
 			if (paramIter.hasNext()) {
 				query.append(" AND ");
 			}
 		}
 
 		Statement stmt = null;
 	//	System.out.println(" My search Query : " + query);
 		try {
 			query.append( " LIMIT " + start + "," + stop);
 			stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query.toString());
 			if (!rs.isBeforeFirst()) {
 				throw new NoUserFoundException(
 						"No users found with the given search criteria");
 			}
 			while (rs.next()) {
 				User user = new User();
 				user.setMembershipId(rs.getInt(1));
 				user.setUserId(rs.getString(2));
 				user.setPassword(rs.getString(3));
 				user.setMembershipType(rs.getString(4));
 				user.setStartDate(rs.getDate(5).toString());
 				user.setFirstName(rs.getString(6));
 				user.setLastName(rs.getString(7));
 				user.setAddress(rs.getString(8));
 				user.setCity(rs.getString(9));
 				user.setState(rs.getString(10));
 				user.setZip(rs.getString(11));
 				user.setCreditCardNumber(rs.getString(12));
 				Date paymentDate = rs.getDate(13); 
 				if (paymentDate != null) {
 					user.setLatestPaymentDate(paymentDate.toString()); 
 				}
 			//	System.out.println(user.getMembershipId());
 				searchList.add(user);
 			}
 			rs.close();
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		userArray = searchList.toArray(new User[searchList.size()]);
 		return userArray;
 	}
 	
 	//Added recently
 		public String updateAdminInfo(String adminId,String firstName, String lastName, String password){
 			String result = "false";
 			try {
 				String query1 = "UPDATE VideoLibrary.admin SET firstName = '"+firstName+"' ,lastName = '"+lastName +"' WHERE userId = '" + adminId + "'";
 			//	System.out.println(query1);
 				int rowcount = stmt.executeUpdate(query1);
 
 				if(rowcount>0){
 					result = "true";
 				}
 			}
 			catch(Exception e){ e.printStackTrace(); }
 			return result;
 		}
 		
 		public Admin displayAdminInformation (String adminId) {
 			Admin admin = new Admin();
 			try {
 				String query = "SELECT admin.firstName, admin.lastName, admin.password FROM admin WHERE userId = '" + adminId + "'";
 				ResultSet rs = stmt.executeQuery(query);
 				
 				if(rs.next()){
 					admin.setAdminId(adminId);
 					admin.setFirstName(rs.getString("firstName"));
 					admin.setLastName(rs.getString("lastName"));
 					admin.setPassword(rs.getString("password"));
 				}
 				else
 					admin = null;
 			} catch (SQLException e) { 
 				admin= null;
 			}
 			
 			return admin; 
 		}
 		
 		public String updateUserPassword(int membershipId,String newPassword){
 			String result = null;
 
 			try{
 				String query1 = "UPDATE VideoLibrary.User SET password = '"+Utils.encryptPassword(newPassword) + "' WHERE membershipId = "+membershipId;
 
 				int rowcount = stmt.executeUpdate(query1);
 
 				if(rowcount > 0){
 					result = "true";
 	 			}
 				else{
 	 				result = "invalidID";
 				}
 			}
 			catch(Exception e){
 	 			result = "false";
 			}
 			return result;		
 		}
 		
 		public Admin signInAdminObject (String userId, String password)  {
 			Admin bean = new Admin(); 
 			bean.setAdminId(userId);
 			bean.setPassword(password);
 			String sql = "SELECT userId, password, firstName, lastName FROM admin WHERE userId = '" + userId + "'" + " AND password = '" + Utils.encryptPassword(password) + "'";
 			System.out.println(sql);
 			try { 
 			
 				Statement stmt = con.createStatement();
 				ResultSet rs = stmt.executeQuery(sql);
 				if(rs.next())
 				{
 			        String firstName = rs.getString("firstName");
 			        String lastName = rs.getString("lastName");
 					
 					bean.setFirstName(firstName);
 					bean.setLastName(lastName);
  
 					bean.setValid(true);
 					return bean;
 				} else {
 					System.out.println("else");
 					bean.setValid(false);
 					bean=null;
 				}
 			} catch (SQLException e) { bean = null;} 
 			return bean;
 		}
 		
 		public List <Admin> listAdmins () {
 			List <Admin> admins = new ArrayList<Admin>();
 			String query = "SELECT admin.userId, admin.firstName, admin.lastName FROM admin ORDER BY userId";
 			try {
 				ResultSet rs = stmt.executeQuery(query);
 				while (rs.next()) {
 					Admin admin = new Admin();
 					admin.setAdminId(rs.getString("userId"));
 					admin.setFirstName(rs.getString("firstName"));
 					admin.setLastName(rs.getString("lastName"));
 					admins.add(admin);
 				}
 			} catch (SQLException e) {
 				admins=null;
 				e.printStackTrace();
 			}
 
 			return admins; 
 			
 		}	
 }
 	
 	
