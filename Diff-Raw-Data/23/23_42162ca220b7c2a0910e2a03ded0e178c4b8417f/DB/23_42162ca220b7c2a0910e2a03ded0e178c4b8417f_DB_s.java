 import java.sql.*;
 import java.util.Random;
 
 public class DB {
 
     Connection conn;
 	Statement stmt;
 	Random random;
 
 
 
 	public void start() throws SQLException{
 		try {
 	    	DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
 		}
 		catch ( Exception e){
 	    	e.printStackTrace();
 		}
 		String strConn = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
 	    String strUsername = "cs174a_scottbishop";
 	    String strPassword = "3954237";
 		conn = DriverManager.getConnection(strConn,strUsername,strPassword);
 	    stmt = conn.createStatement();
 	    random = new Random();
 	}
 
 	public String getDate(){
 		return "3/11/13";
 	}
 
     public int login(String username, String password) throws SQLException {
 		String query = "select tax_ID from Customer where username = '" + username + "' and password = '" + password + "'";
 		ResultSet rs = stmt.executeQuery (query);
 		int id =0;
 		while (rs.next()){
 			id = rs.getInt("tax_ID");
 		}
 		rs.close();
 		return id;
     }
 
     public int loginAdmin(String username, String password) throws SQLException {
 		String query = "select tax_ID from Customer where username = '" + username + "' and password = '" + password + "' and isAdmin = 'y'";
 		ResultSet rs = stmt.executeQuery (query);
 		int id =0;
 		while (rs.next()){
 			id = rs.getInt("tax_ID");
 		}
 		rs.close();
 		return id;
     }
 
     public int newCustomer(String name, String username, String password, String address, String state, String phone, String email, String ssn) throws SQLException{
     	int account_ID = 0; int tax_ID = 0; Boolean notFound = true; ResultSet rs; String foundUsername = "";
     	String query = "";
     	while(notFound){
     		tax_ID = random.nextInt(9000) + 1000;
    		query = "select username from Customer where tax_ID = " + tax_ID;
     		rs = stmt.executeQuery (query);
     		while (rs.next()){
 				foundUsername = rs.getString("username");
 			}
			if(foundUsername == null){
 				notFound = false;
 			}
     	}
     	query = "select max(account_ID) from Customer";
     	rs = stmt.executeQuery (query);
     		while (rs.next()){
				account_ID = rs.getInt("actor_id");
 			}
 		account_ID++;
     	query = "insert into Customer values('" + username + "','" + password + "','" + name + "','" + state + "'," 
     					+ phone + ",'" + email + "'," + tax_ID + "," + ssn + "," + account_ID + ",'n')";
 		rs = stmt.executeQuery (query);
 		query = "insert into Market values(" + tax_ID + "," + account_ID + ",1000)";
 		rs = stmt.executeQuery (query);
 		rs.close();
 		return tax_ID;
     }
 
     public double getMarketBalance(int id) throws SQLException {
 		String query = "select balance from Market where tax_ID = '" + id + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		double balance=0;
 		while (rs.next()){
 			balance = rs.getDouble("balance");
 		}
 		rs.close();
 		return balance;
     }
 
     public double getCurrentStockPrice(String actorID) throws SQLException {
 		String query = "select current_price from Actors where actor_id = '" + actorID + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		double price=0;
 		while (rs.next()){
 			price = rs.getDouble("current_price");
 		}
 		rs.close();
 		return price;
     }
 
    public void deposit(double amount, int id) throws SQLException {
    		double startingAmount = amount;
 		String query = "select Balance from Market where tax_ID = '" + id + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		while (rs.next()){
 			amount += rs.getInt("Balance");
 		}
 		query = "update Market set Balance = " + amount + " where tax_ID = '" + id + "'";
 		rs = stmt.executeQuery(query);
 		query = "insert into Market_Transaction values(" + id + ",'" + getDate() + "'," + amount
 				+ ",'" + startingAmount + " was deposited on " + getDate() + "')";
 		rs = stmt.executeQuery(query);
 		rs.close();
     } 
 
     public void withdraw(double amount, int id) throws SQLException {
 		String query = "select Balance from Market where tax_ID = '" + id + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		int balance=0;
 		while (rs.next()){
 			balance = rs.getInt("Balance");
 		}
 		query = "update Market set Balance = " + (balance - amount) + " where tax_ID = '" + id + "'";
 		rs = stmt.executeQuery(query);
 		query = "insert into Market_Transaction values(" + id + ",'" + getDate() + "'," + (amount * -1)
 				+ ",'" + amount + " was withdrawn on " + getDate() + "')";
 		rs = stmt.executeQuery(query);
 		rs.close();
     }
 
     public void addStock(String stockID, int amount, int id, double price) throws SQLException {
 		String query = "select Shares from Stock where tax_ID = '" + id + "' and actor_id = '" + stockID + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		int currentAmount = 0;
 		while (rs.next()){
 			currentAmount = rs.getInt("Shares");
 		}
 		if(currentAmount != 0){
 			query = "update Stock set Shares = " + (currentAmount + amount) + " where tax_ID = '" + id + "' and actor_id = '" + stockID + "'";
 			rs = stmt.executeQuery(query);
 		}
 		else{
 			query = "insert into Stock values (" + id + "," + amount + ",'" + stockID + "')"; 
 			rs = stmt.executeQuery(query);
 		}
 		query = "insert into Stock_Transaction values(" + id + ",'" + getDate() + "'," + amount
 				+ "," + price + ",'" + amount + " shares were bought at " + price + " per share on " + getDate() + "')";
 		rs = stmt.executeQuery(query);
 		rs.close();
     }
 
     public void sellStock(String stockID, int amount, int id, double price) throws SQLException {
 		String query = "select Shares from Stock where tax_ID = '" + id + "' and actor_id = '" + stockID + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		int currentAmount = 0;
 		while (rs.next()){
 			currentAmount = rs.getInt("Shares");
 		}
 		if(currentAmount != 0){
 			query = "update Stock set Shares = " + (currentAmount - amount) + " where tax_ID = '" + id + "' and actor_id = '" + stockID + "'";
 			rs = stmt.executeQuery(query);
 		}
 		query = "insert into Stock_Transaction values(" + id + ",'" + getDate() + "'," + (amount * -1)
 				+ "," + price + ",'" + amount + " shares were sold at " + price + " per share on " + getDate() + "')";
 		rs = stmt.executeQuery(query);
 		rs.close();
     }
 
     public Boolean checkStockExists(String stockID) throws SQLException {
     	String query = "select name from Actors where actor_id = '" + stockID + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		String actorName = "";
 		while (rs.next()){
 			actorName = rs.getString("name");
 		}
 		if(actorName.equals("")){
 			rs.close();
 			return false;
 		}
 		else{
 			rs.close();
 			return true;
 		}
     }
 
     public double getNumShares(String stockID, int id) throws SQLException {
     	String query = "select Shares from Stock where tax_ID = '" + id + "' and actor_id = '" + stockID + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		double shares = 0;
 		while (rs.next()){
 			shares = rs.getDouble("Shares");
 		}
 		rs.close();
 		return shares;
     }
 
     public String getActorProfile(String stockID) throws SQLException {
     	String query = "select * from Actors where actor_id = '" + stockID + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		String actorId = ""; double price = 0; String name = ""; String dob = "";
 		String title = ""; String role = ""; int year = 0; double contract = 0;
 		while (rs.next()){
 			actorId = rs.getString("actor_id");
 			price = rs.getDouble("current_price");
 			name = rs.getString("name");
 			dob = rs.getString("dob");
 			title = rs.getString("movie_Title");
 			role = rs.getString("role");
 			year = rs.getInt("year");
 			contract = rs.getDouble("contract");
 		}
 		rs.close();
 		String result = "Actor ID: " + actorId + " Current Stock Price: " + price + " Name: " + name + " Date of Birth: " + dob + 
 						"\nMovie Title: " + title + " Role: " + role + " Year: " + year + " Contract Price: " + contract + "\n";
 		return result;
     }
 
     public Boolean checkTransactionHistory(int id) throws SQLException {
     	String market = ""; String stock = "";
     	String query = "select description from Market_Transaction where tax_ID = '" + id + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		while (rs.next()){
 			market = rs.getString("description");
 		}
 		query = "select description from Stock_Transaction where tax_ID = '" + id + "'";
 		rs = stmt.executeQuery(query);
 		while (rs.next()){
 			stock = rs.getString("description");
 		}
 		rs.close();
 		if(market == null && stock == null){
 			return false;
 		}
 		else{
 			return true;
 		}
     }
 
     public String getTransactionHistory(int id) throws SQLException {
     	String market = ""; String stock = ""; String result = "";
     	String query = "select description from Market_Transaction where tax_ID = '" + id + "'";
 		ResultSet rs = stmt.executeQuery(query);
 		while (rs.next()){
 			result = result + rs.getString("description") + "\n";
 		}
 		query = "select description from Stock_Transaction where tax_ID = '" + id + "'";
 		rs = stmt.executeQuery(query);
 		while (rs.next()){
 			result = result + rs.getString("description") + "\n";
 		}
 		rs.close();
 		return result;
     }
 }
