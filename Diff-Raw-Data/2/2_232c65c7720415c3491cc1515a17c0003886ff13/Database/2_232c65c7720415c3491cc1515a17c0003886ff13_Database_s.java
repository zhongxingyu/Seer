 package database;
 
 import items.Activity;
 import items.ActivitySubType;
 import items.ActivityType;
 import items.Role;
 import items.TimeReport;
 import items.User;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Denna klassen innehåller länken till databasen. Klassen innehåller den
  * funktionalitet som systemet behöver för att hämta ut och lägga in information
  * i databasen. Klassen finns tillgänglig i ServletBase och därmed även i de
  * servlets som behöver den.
  */
 public class Database {
 
 	public static final String ADMIN = "admin";
 	public static final String ADMIN_PW = "adminpw";
 
 	private static Database instance;
 
 	private Connection conn = null;
 
 	private Database() throws SQLException, ClassNotFoundException {
 		Class.forName("com.mysql.jdbc.Driver");
 		conn = DriverManager.getConnection("jdbc:mysql://vm26.cs.lth.se/puss1301?"
 				+ "user=puss1301&password=8jh398fs");
 		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
 	}
 
 	/**
 	 * Hämtar singletoninstansen av Database
 	 * 
 	 * @throws ClassNotFoundException
 	 */
 	public static Database getInstance() throws SQLException, ClassNotFoundException {
 		if (instance == null) {
 			instance = new Database();
 		}
 		return instance;
 	}
 
 	/**
 	 * 
 	 * Hämtar tidrapporten med det specifika id från databasen.
 	 * 
 	 * @return null om den inte hittar någon.
 	 * 
 	 */
 	public TimeReport getTimeReport(int id) {
 		Statement stmt;
 		TimeReport tr = null;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM TimeReports WHERE Id='" + id + "'");
 		    while (rs.next()) {
 			    User u = getUser(rs.getString("Username"));
 			    boolean signed = rs.getBoolean("Signed");
 			    String projectGroup = rs.getString("GroupName");
 			    Date date = rs.getDate("Date");
 			    List<Activity> activities = new ArrayList<Activity>();
 			    Statement stmt2 = conn.createStatement();
 			    ResultSet rs2 = stmt2.executeQuery("SELECT * FROM Activity WHERE Id='" + id + "'");
 			    while (rs2.next()) {
 			    	ActivityType tp = ActivityType.valueOf(rs2.getString("ActivityName"));
 			    	int worked = rs2.getInt("MinutesWorked");
 			    	ActivitySubType tsp;
 			    	try {
 			    		tsp = ActivitySubType.valueOf(rs2.getString("Type"));
 			    	} catch(IllegalArgumentException e){
 			    		tsp = ActivitySubType.noSubType;
 			    	}
 			    	activities.add(new Activity(tp, worked, tsp));
 			    }
 			    
 			    tr = new TimeReport(u, activities, signed, rs.getInt("Id"), rs.getInt("WeekNumber") , projectGroup, date);
 			    rs2.close();
 			    stmt2.close();
 		    }
 		    rs.close();
 		    stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		return tr;
 	}
 
 	/**
 	 * 
 	 * @param username
 	 *            anv��ndarens id som tidrapporterna ��r kopplade till.
 	 * @param projectGroup
 	 *            projektgruppen som tidrapporterna är kopplade till.
 	 * @return en lista med tidrepporter eller null om något går fel.
 	 */
 	public List<TimeReport> getTimeReports(String username, String projectGroup) {
 		List<TimeReport> reports = new ArrayList<TimeReport>();
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM TimeReports WHERE " + "Username='"
 					+ username + "' AND GroupName='" + projectGroup + "'");
 			while (rs.next()) {
 				int id = rs.getInt("id");
 				reports.add(getTimeReport(id));
 			}
 			rs.close();
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		return reports;
 	}
 
 	/**
 	 * Försöker skapa en ny tidrapport.
 	 * 
 	 * @param timereport
 	 *            Tidrapporten som ska skapas. Skapar en tidrapport.
 	 * @return true om det lyckas, annars false.
 	 * 
 	 */
 	public boolean createTimeReport(TimeReport timereport) {
 		// Check if the time report already exists
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM TimeReports WHERE WeekNumber="
 					+ timereport.getWeek() + " AND Username='" + timereport.getUser().getUsername()
 					+ "' AND Groupname='" + timereport.getProjectGroup() + "'");
 			if (rs.next()) {
 				return false;
 			}
 
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		// Extract and save into table:TimeReports
 		try {
 			Statement stmt = conn.createStatement();
 			String statement = "INSERT INTO TimeReports (Username, Groupname, WeekNumber, Date, Signed) VALUES('"
 
 					+ timereport.getUser().getUsername()
 					+ "','"
 					+ timereport.getProjectGroup()
 					+ "',"
 					+ timereport.getWeek()
 					+ ", NOW(),"
 					+ (timereport.getSigned() ? 1 : 0)
 					+ ")";
 			stmt.executeUpdate(statement);
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 
 		// Extract and save into table:Activity
 		if (timereport.getActivities() != null) {
 			// Activities will be null if we ar ecreating a new timereport
 			try {
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT Id FROM TimeReports WHERE WeekNumber="
 						+ timereport.getWeek() + " AND Username='" + timereport.getUser().getUsername()
 						+ "'");
 				rs.next();
 				int id = rs.getInt("Id");
 				stmt.close();
 				for (Activity a : timereport.getActivities()) {
 					stmt = conn.createStatement();
 					String statement = "INSERT INTO Activity (Id, ActivityName, ActivityNumber, MinutesWorked, Type) VALUES("
 							+ id + ",'" + a.getType().toString() + "', 0," + a.getLength() + ", '" + a.getSubType().toString()+"')";
 					stmt.executeUpdate(statement);
 					stmt.close();
 				}
 			} catch (SQLException ex) {
 				System.out.println("SQLException: " + ex.getMessage());
 				System.out.println("SQLState: " + ex.getSQLState());
 				System.out.println("VendorError: " + ex.getErrorCode());
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	/**
 	 * 
 	 * Försöker ta bort en tidrapport.
 	 * 
 	 * @param id
 	 *            id för tidrapport att ta bort. Tar bort en tidrapport.
 	 * @return true om det lyckas, annars false.
 	 * 
 	 */
 	public boolean deleteTimeReport(int id) {
 		try {
 			Statement stmt = conn.createStatement();
 			String statement = "DELETE FROM Activity WHERE Id=" + id;
 			stmt.executeUpdate(statement);
 			stmt.close();
 			stmt = conn.createStatement();
 			statement = "DELETE FROM TimeReports WHERE Id=" + id;
 			stmt.executeUpdate(statement);
 			stmt.close();
 
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Försöker att uppdatera en tidrapport.
 	 * 
 	 * @param timereport
 	 *            tidrapporten att uppdatera. Uppdaterar en tidrapport.
 	 * 
 	 * @return true om det lyckas, annars false.
 	 */
 	public boolean updateTimeReport(TimeReport timereport) {
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM TimeReports WHERE Id='" + timereport.getID() + "'");
 			if (!rs.next()) {
 				System.out.println("Id:"+timereport.getID());
 				return false;
 			}
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		// Extract and save into table:TimeReports
 		try {
 			stmt = conn.createStatement();
 			String statement = "UPDATE TimeReports SET WeekNumber = '"+timereport.getWeek()+"',"
 					+ "Date = NOW(), SIGNED=0 WHERE Id='"+timereport.getID()+"'";
 			stmt.executeUpdate(statement);
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 
 		// Extract and save into table:Activity
 		try {
 			stmt = conn.createStatement();
 			String removeAll = "DELETE FROM Activity WHERE Id='"+timereport.getID()+"'";
 			stmt.executeUpdate(removeAll);
 			for (Activity a : timereport.getActivities()) {
 				stmt = conn.createStatement();				
 				String insertNew = "INSERT INTO Activity (Id, ActivityName, ActivityNumber, MinutesWorked, Type) VALUES("
 						+ timereport.getID() + ",'" + a.getType().toString() + "', 0," + a.getLength() + ", '"+a.getSubType().toString()+"')";
 				stmt.executeUpdate(insertNew);
 				stmt.close();
 			}
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Sätter en användares roll för ett visst projekt.
 	 * 
 	 * @param userID
 	 *            användaren vars roll ska manipuleras.
 	 * @param project
 	 *            projektnamnet att ändra i.
 	 * @param role
 	 *            rollen som ska sättas på användaren.
 	 * 
 	 */
 	public void setUserRole(String userID, String project, Role role) {
 		try {
 			conn.createStatement().execute(
 					"update Memberships set Role='" + (role != null ? role.toString(): null) + "' where Groupname='"
 							+ project + "' and username='" + userID + "'");
 		} catch (SQLException e) {
 			System.out.println("");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * Signerar en tidsrapport
 	 * 
 	 * @param timereport
 	 *            tidrapporten att signera. Signerar en tidrapport.
 	 * 
 	 */
 	public boolean signTimeReport(TimeReport timereport) {
 		return signReport(timereport, true);
 	}
 
 	private boolean signReport(TimeReport timereport, boolean sign) {
 		try {
 			return 1 == conn.createStatement().executeUpdate("update TimeReports set Signed = " + (sign ? 1 : 0) +" where id = " +timereport.getID());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Avsignerar en tidrapport.
 	 * 
 	 * @param timereport
 	 *            tidrapporten att avsignera. Avsignerar en tidrapport.
 	 * 
 	 */
 	public boolean unsignTimeReport(TimeReport timereport) {
 		return signReport(timereport, false);
 	}
 
 	/**
 	 * 
 	 * Försöker hämta en lista av alla användare i systemet från databasen
 	 * 
 	 */
 	public List<User> getUsers() {
 		List<User> users = new ArrayList<User>();
 
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM Users");
 			while (rs.next()) {
 				String name = rs.getString("username");
 				String password = rs.getString("password");
 				users.add(new User(name, password));
 			}
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		return users;
 	}
 
 	/**
 	 * 
 	 * Försöker hämta en lista av alla projekt i systemet från databasen
 	 * 
 	 */
 	public List<String> getProjects() {
 		List<String> list = new ArrayList<>();
 		ResultSet rs;
 		try {
 			rs = conn.createStatement().executeQuery("select * from ProjectGroups;");
 			while (rs.next()) {
 				list.add(rs.getString("Groupname"));
 			}
 			return list;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * Försöker hämta alla användare från ett projekt.
 	 * 
 	 * @param projectName
 	 *            projektet att hämta ifrån. Hämtar en lista av alla användare i
 	 *            ett projekt
 	 * 
 	 * @return Returnerar null om projektet inte finns.
 	 * 
 	 * 
 	 */
 	public List<User> getUsersInProject(String projectName) {
 		List<User> list = new ArrayList<>();
 		try {
 			ResultSet rs = conn.createStatement().executeQuery(
 					"select Username from Memberships where Groupname = '" + projectName + "';");
 			while (rs.next()) {
 				list.add(getUser(rs.getString("Username")));
 			}
 			return list;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * Försöker hämta alla projektledare från ett projekt.
 	 * 
 	 * @param projectName
 	 *            projektet att hämta ifrån. Hämtar en lista av alla användare i
 	 *            ett projekt som är projektledare.
 	 * 
 	 * @return Returnerar null om projektet inte finns.
 	 * 
 	 * 
 	 */
 	public List<User> getProjectManagersInProject(String projectName) {
 		List<User> managers = new ArrayList<User>();
 		
 		try {
 			ResultSet rs = conn.createStatement().executeQuery(
 					"SELECT * FROM Memberships WHERE Groupname='" + 
 					projectName + "' AND Role='" + Role.Manager.toString() + "'");
 			
 			while(rs.next()) {
 				managers.add(new User(rs.getString("Username"), ""));
 			}
 			
 			return managers;
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	
 	}
 
 	/**
 	 * Försöker lägga till en användare till ett projekt.
 	 * 
 	 * @param projectName
 	 *            projektet att lägga till användaren till.
 	 * @param username
 	 *            användaren som ska läggas till. Lägger till en användare till
 	 *            ett projekt.
 	 * @return true om den lyckas annars false.
 	 * 
 	 */
 	public boolean addUserToProject(String projectName, String username) {
 		try {
 			String realname = getUser(username).getUsername();
 			return conn.createStatement().executeUpdate(
 					"insert into Memberships (Username, Groupname) values ('" + realname + "', '"
 							+ projectName + "')") == 1;
 		} catch (SQLException e) {
 			System.out.println("");
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	/**
 	 * Försöker ta bort en användare från ett projekt.
 	 * 
 	 * @param projectName
 	 *            projektet att ta bort användaren från.
 	 * @param userName
 	 *            användaren som ska tas bort. Tar bort en användare från ett
 	 *            projekt.
 	 * @return true om den lyckas annars false.
 	 * 
 	 */
 	public boolean deleteUserFromProject(String projectName, String userName) {
 		try {
 			return conn.createStatement().executeUpdate(
 					"delete from Memberships where Groupname='" + projectName + "' AND Username='"
 							+ userName + "'") == 1;
 		} catch (SQLException e) {
 			System.out.println("");
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	/**
 	 * Försöker skapa en projektgrupp.
 	 * 
 	 * @param projectName
 	 *            namn på projektgruppen som ska skapas. Skapar en projektgrupp.
 	 * 
 	 @return true om den lyckas annars false.
 	 */
 	public boolean createProjectGroup(String projectName) {
 		try {
 			Statement stmt = conn.createStatement();
 			String statement = "INSERT INTO ProjectGroups (Groupname) VALUES('" + projectName
 					+ "')";
 			stmt.executeUpdate(statement);
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Försöker ta bort en projektgrupp.
 	 * 
 	 * @param projectName
 	 *            name på projektgruppen som ska tas bort. Tar bort en
 	 *            projektgrupp.
 	 * @return true om den lyckas annars false.
 	 * 
 	 */
 	public boolean deleteProjectGroup(String projectName) {
 		try {
 			conn.createStatement().executeUpdate(
 					"DELETE FROM Memberships WHERE Groupname='" + projectName + "'");
 			return conn.createStatement().executeUpdate(
 					"DELETE FROM ProjectGroups WHERE Groupname='" + projectName + "'") == 1;
 		} catch (SQLException e) {
 			System.out.println("");
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	/**
 	 * Försöker lägga till en användare.
 	 * 
 	 * @param username
 	 *            användarnamnet som ska läggas till i systemet. Lägger till en
 	 *            användare i systemet.
 	 * @param password
 	 *            användarlösenordet som ska läggas till i systemet.
 	 * @return true om den lyckas annars false.
 	 * 
 	 */
 	public boolean addUser(String username, String password) {
 
 		try {
 			Statement stmt = conn.createStatement();
 			String statement = "INSERT INTO Users (username, password) VALUES('" + username
 					+ "', '" + password + "')";
 			stmt.executeUpdate(statement);
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Försöker ta bort en användare.
 	 * 
 	 * @param username
 	 *            användarnamnet som ska tas bort ur systemet. Tar bort en
 	 *            användare ur systemet.
 	 * @return true om den lyckas annars false.
 	 * 
 	 */
 	public boolean deleteUser(String username) {
 		int result = 0;
 		try {
 
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT Id FROM TimeReports WHERE Username='"
 					+ username + "'");
 			int id;
 			while (rs.next()) {
 				id = rs.getInt("Id");
 				deleteTimeReport(id);
 			}
 			rs.close();
 			stmt.close();
 			
 			stmt = conn.createStatement();
 			stmt.executeUpdate("DELETE FROM Memberships WHERE Username='" + username + "'");
 			stmt.close();
 
 			stmt = conn.createStatement();
 			String statement = "DELETE FROM Users WHERE username='" + username + "'";
 			result = stmt.executeUpdate(statement);
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			return false;
 		}
 		return result == 1;
 	}
 
 	/**
 	 * Hämtar en användare
 	 * 
 	 * @param username
 	 *            anvädarnamnet som ska hämtas från databasen. Hämtar en
 	 *            användare från databasen.
 	 * @return ett User-objekt om användare finns annars null?
 	 * 
 	 */
 	public User getUser(String username) {
 		User user = null;
 
 		Statement stmt;
 		try {
 			if (username.equals(ADMIN)) {
 				return new User(ADMIN, ADMIN_PW);
 			}
 
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE username='" + username
 					+ "'");
 			while (rs.next()) {
 				String name = rs.getString("username");
 				String password = rs.getString("password");
 				user = new User(name, password);
 			}
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 
 		return user;
 	}
 
 	/**
 	 * Försöker logga in i systemet.
 	 * 
 	 * @param username
 	 *            användarnamnet som man försöker logga in som.
 	 * @param password
 	 *            lösenordet för användaren. Försöker logga in med användarnamn
 	 *            och lösenord.
 	 * 
 	 * @return true om inloggningen lyckades annars false.
 	 * 
 	 */
 	public boolean login(String username, String password) {
 		if (ADMIN.equals(username) && password != null) {
 			try {
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT Password FROM Administrator");
 				rs.next();
 				String adminPass = rs.getString("Password");
 				return password.equals(adminPass);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		User user = getUser(username);
 		return user != null && user.getPassword().equals(password);
 	}
 
 	/**
 	 * Start a transaction to the database
 	 * 
 	 * @throws SQLException
 	 */
 	public void startTransaction() throws SQLException {
 		conn.setAutoCommit(false);
 	}
 
 	/**
 	 * Rollback current transactions from the database
 	 * 
 	 * @throws SQLException
 	 */
 	public void rollback() throws SQLException {
 		conn.rollback();
 		conn.setAutoCommit(true);
 	}
 
 	/**
 	 * Returns the role of the given username in the given project.
 	 * 
 	 * @param username
 	 * @param projectgroup
 	 * @return
 	 */
 	public Role getRole(String username, String projectgroup) {
 		String role = "";
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM Memberships WHERE username='"
 					+ username + "' AND groupname='" + projectgroup + "'");
 			while (rs.next()) {
 				role = rs.getString("role");
 			}
 		} catch (SQLException e) {
 			System.out.println("fel i getRole() i Database.java");
 			e.printStackTrace();
 		}
 		try {
 			Role r = Role.valueOf(role);
 			return r;
 		} catch (Exception e) {
 			return Role.NoRole;
 		}
 
 	}
 
 	/**
 	 * Hämtar alla projekt som den specifiserade användaren är medlem i.
 	 * 
 	 * @param user
 	 *            en snäll söt liten användare
 	 * @return en lista med projektnamn
 	 */
 	public List<String> getProjects(User user) throws NullPointerException {
 		List<String> list = new ArrayList<>();
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("select Groupname from Memberships where Username = '"
 					+ user.getUsername() + "';");
 			while (rs.next()) {
 				list.add(rs.getString("Groupname"));
 			}
 			return list;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	* Hämtar för en specifik användare tiden för en viss aktivitet under en given vecka.
 	*
 	* @param user
 	*		användaren som man försöker hämta tiden från.
 	* @param type
 	*		aktiviteten som man försöker hämta tiden från.
 	* @param week
 	*		vecka som man försöker hämta tiden från.
 	*
 	* @return Tiden som användaren spenderat på aktiviteten.
 	*
 	*/
 	public int getTimeForActivity(User user, ActivityType type, int week) {
 		int id = 0;
 		try {
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT Id FROM TimeReports WHERE Username='" + user.getUsername() + "' AND WeekNumber=" + week);
 			if (rs.next()) {
 				id = rs.getInt("Id");
 				stmt.close();
 				stmt = conn.createStatement();
 				rs = stmt.executeQuery("SELECT MinutesWorked FROM Activity WHERE Id=" + id + " AND ActivityName='" + type.toString() + "'");
 				if (rs.next()) {
 					return rs.getInt("MinutesWorked");
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 
 	public List<TimeReport> getAllTimeReports(String projectGroup) {
 		List<TimeReport> reports = new ArrayList<TimeReport>();
 		Statement stmt;
 		try {
 			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM TimeReports WHERE AND GroupName='" + projectGroup + "'");
 			while (rs.next()) {
 				int id = rs.getInt("id");
 				reports.add(getTimeReport(id));
 			}
 			rs.close();
 			stmt.close();
 		} catch (SQLException ex) {
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		return reports;
 	}
 }
