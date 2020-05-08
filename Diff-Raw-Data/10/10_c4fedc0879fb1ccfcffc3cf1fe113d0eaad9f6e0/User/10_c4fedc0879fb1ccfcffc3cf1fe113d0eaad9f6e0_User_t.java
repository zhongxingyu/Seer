 package ca.awesome;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Collection;
 
 
 /*
  * methods like findBy____() get users from the DB.
  * methods like update____() update data in the DB.
  *
  * TODO: user preparedStatements to avoid SQLInjection
  * 	attacks.
  */
 public class User {
 	// different types (classes) of users
 	public static final int ADMINISTRATOR_T = 1;
 	public static final int PATIENT_T = 2;
 	public static final int DOCTOR_T = 4;
 	public static final int RADIOLOGIST_T = 8;
 	
 	// info from users table
 	private String userName;
 	private String password;
 	private Integer type;
 	private Date dateRegistered;
 
 	// info from persons table
 	private String firstName;
 	private String lastName;
 	private String address;
 	private String email;
 	private String phone;
 
 	public User(String name, String password, Integer type, Date registered) {
 		this.userName = name;
 		this.password = password;
 		this.type = type;
		this.dateRegistered = registered;
 	}
 
 	public String getUserName() {
 		if (userName == null) {
 			throw new MissingFieldException("userName", this);
 		}
 		return userName;
 	}
 
 	public String getPassword() {
 		if (password == null) {
 			throw new MissingFieldException("password", this);
 		}
 		return password;
 	}
 
 	public int getType() {
 		if (type == null) {
 			throw new MissingFieldException("type", this);
 		}
 		return type.intValue();
 	}
 
	public Date getDateRegistered() {
		if (dateRegistered == null) {
			throw new MissingFieldException("dateRegistered", this);
		}
		return dateRegistered;
	}

 	// info from persons table
 
 	public String getFirstName() {
 		if (firstName == null) {
 			throw new MissingFieldException("firstName", this);
 		}
 		return firstName;
 	}
 
 	public String getLastName() {
 		if (lastName == null) {
 			throw new MissingFieldException("lastName", this);
 		}
 		return lastName;
 	}
 
 	public String getAddress() {
 		if (address == null) {
 			throw new MissingFieldException("address", this);
 		}
 		return address;
 	}
 
 	public String getEmail() {
 		if (email == null) {
 			throw new MissingFieldException("email", this);
 		}
 		return email;
 	}
 
 	public String getPhone() {
 		if (phone == null) {
 			throw new MissingFieldException("phone", this);
 		}
 		return phone;
 	}
 
 	public void loadPersonalInfo(DatabaseConnection connection) {
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select first_name, last_name, address, email, phone"
 				+ " from persons where user_name = '" + userName + "'"
 			);
 			
 			if (results == null || !results.next()) {
 				throw new RuntimeException("failed to loadPersonalInfo()");
 			}
 
 			this.firstName = results.getString(1);
 			this.lastName = results.getString(2);
 			this.address = results.getString(3);
 			this.email = results.getString(4);
 			this.phone = results.getString(5);
 
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to loadPersonalInfo()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	/*
 	 * Updates info for this user in the persons table.
 	 *
 	 * TODO: handle inserts also.
 	 */
 	public boolean updatePersonalInfo(String newFirstName, String newLastName,
 		String newAddress, String newEmail, String newPhone,
 		DatabaseConnection connection) {
 
 		// TODO: handle duplicate email problems.
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			int modified = statement.executeUpdate(
 				"UPDATE persons SET"
 				+ " first_name = '" + newFirstName + "',"
 				+ " last_name = '" + newLastName + "',"
 				+ " address = '" + newAddress + "',"
 				+ " email = '" + newEmail + "',"
 				+ " phone = '" + phone + "'"
 				+ " where user_name = '" + getUserName() + "'"
 			);
 			
 			if (modified == 0) {
 				throw new RuntimeException("no result from update!");
 			} else {
 				firstName = newFirstName;
 				lastName = newLastName;
 				address = newAddress;
 				email = newEmail;
 				phone = newPhone;
 			}
 
 			return true;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to updatePersonalInfo()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	/*
 	 * Load a user from the database with the username provided.
 	 */
 	static public User findUserByName(String name,
 		DatabaseConnection connection) {
 
 		ResultSet results = null;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"select user_name, password, class, date_registered"
 				+ " from users where user_name = '" + name + "'"
 			);
 			
 			if (results == null || !results.next()) {
 				return null;
 			}
 
 			String password = results.getString(2);
 			String type = results.getString(3);
 			Date registered = results.getDate(4);
 
 			return new User(name, password, getTypeFromString(type),
 							registered);
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to findUserByName()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	static Collection<User> findUsersByType(int type,
 			DatabaseConnection connection) {
 		ResultSet results = null;
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(
 				"select user_name, password, date_registered"
 				+ " from users where class=?"
 			);
 			statement.setString(1, getStringFromType(type));
 			results = statement.executeQuery();
 			
 			ArrayList<User> users = new ArrayList<User>();
 
 			while (results != null && results.next()) {
 				String userName = results.getString(1);
 				String password = results.getString(2);
 				Date registered = results.getDate(3);
 
 				users.add(new User(userName, password, type,
 							registered));
 			}
 
 			return users;
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to findUsersByType()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	/*
 	 * Set the user's password in the db.
 	 */
 	public static void updateUserPassword(User user, String password,
 		DatabaseConnection connection) {
 
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			int modified = statement.executeUpdate(
 				"UPDATE users SET password='" + password + "'"
 				+ " where user_name = '" + user.getUserName() + "'"
 			);
 			
 			if (modified == 0) {
 				throw new RuntimeException("no result from update!");
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException("failed to updateUserPassword()", e);
 		} finally {
 			connection.close();
 		}
 	}
 
 	/*
 	 * Translate a string (from the db) into a user type.
 	 */
 	private static Integer getTypeFromString(String type) {
 		if (type.equals("a")) {
 			return new Integer(ADMINISTRATOR_T);
 		} else if (type.equals("p")) {
 			return new Integer(PATIENT_T);
 		} else if (type.equals("d")) {
 			return new Integer(DOCTOR_T);
 		} else if (type.equals("r")) {
 			return new Integer(RADIOLOGIST_T);
 		}
 
 		return null;
 	}
 
 	private static String getStringFromType(int type) {
 		if (ADMINISTRATOR_T == type) {
 			return "a";
 		} else if (PATIENT_T == type) {
 			return "p";
 		} else if (DOCTOR_T == type) {
 			return "d";
 		} else if (RADIOLOGIST_T == type) {
 			return "r";
 		}
 
 		return null;
 	}
 };
