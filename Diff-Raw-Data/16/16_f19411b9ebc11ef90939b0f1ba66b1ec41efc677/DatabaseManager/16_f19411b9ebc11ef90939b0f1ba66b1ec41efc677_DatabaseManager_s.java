 /**
  * 
  */
 package database;
 
 import com.mysql.jdbc.Statement;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Sascha Meusel
  * 
  */
 public class DatabaseManager {
     
     private static Logger log = Logger.getLogger("database");
 
 	private static final int maxCacheSize = 20;
 	private static final HashMap<String, UserDataset> userDatasetCache_mail = new HashMap<String, UserDataset>(
 			maxCacheSize);
 	private static final HashMap<Integer, UserDataset> userDatasetCache_id = new HashMap<Integer, UserDataset>(
 			maxCacheSize);
 
 	private Connection con = null;
     
     private final String url;
     private final String dbName;
     private final String userName;
     private final String password;
     
 	private PreparedStatement pstAddNewRequest;
 	private PreparedStatement pstAddNewUser;
 	private PreparedStatement pstGetAllRequests;
 	private PreparedStatement pstGetAllUsers;
 	private PreparedStatement pstGetUserWithEmail;
 	private PreparedStatement pstGetUserWithId;
 	private PreparedStatement pstUpdateRequest;
 	private PreparedStatement pstUpdateRequestWithComputeResult;
 	private PreparedStatement pstUpdateUser;
 	private PreparedStatement pstDeleteRequestWithRequestId;
 	private PreparedStatement pstDeleteRequestsOfUserWithUserId;
 	private PreparedStatement pstDeleteUserWithUserId;
 	private PreparedStatement pstDeleteUserWithEmail;
 	private PreparedStatement pstGetAllRequestsWithLimitOffset;
 	private PreparedStatement pstGetRequestWithRequestId;
 	private PreparedStatement pstGetRequestsWithUserId;
 	private PreparedStatement pstGetRequestsWithUserIdLimitOffset;
 	private PreparedStatement pstGetAllUsersWithLimitOffset;
     private PreparedStatement pstCountAllRequests;
     private PreparedStatement pstCountRequestsWithUserId;
     private PreparedStatement pstCountAllUsers;
 
 
     /*
        INSERT statements
      */
 	private final static String strAddNewRequest = "INSERT INTO Requests "
 			+ "(UserID, Algorithm, JSONRequest, RequestDate) VALUES(?, ?, ?, ?)";
 
 	private final static String strAddNewUser = "INSERT INTO Users "
 			+ "(Email, Passwordhash, Salt, FirstName, LastName, Address, "
 			+ "AdminFlag, Status, RegistrationDate, VerifiedDate)"
 			+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 
     /*
        SELECT COUNT(*) statements
      */
     private final static String strCountAllRequests = "SELECT COUNT(*) FROM Requests";
 
     private final static String strCountRequestsWithUserId = "SELECT COUNT(*) FROM Requests WHERE UserID = ?";
 
     private final static String strCountAllUsers = "SELECT COUNT(*) FROM Users";
 
 
     /*
        SELECT statements for table Requests
      */
     
 	private final static String strGetAllRequests = "SELECT id, UserID, "
             + "Algorithm, JSONRequest, JSONResponse, PendingFlag, Costs, PaidFlag, "
             + "RequestDate, FinishedDate, CPUTime, FailedFlag, "
             + "FailDescription FROM Requests";
 
     private final static String strGetAllRequestsWithLimitOffset = strGetAllRequests
             + " LIMIT ? OFFSET ?";
 
     private final static String strGetRequestsWithUserId = strGetAllRequests
             + " WHERE UserID = ?";
 
     private final static String strGetRequestsWithUserIdLimitOffset = strGetRequestsWithUserId
             + " LIMIT ? OFFSET ?";
 
     // single result
     private final static String strGetRequestWithRequestId = strGetAllRequests
             + " WHERE id = ?";
 
 
     /*
        SELECT statements for table Users
      */
 	private final static String strGetAllUsers = "SELECT id, Email, "
 			+ "Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, "
 			+ "Address, RegistrationDate, VerifiedDate, DeleteRequestDate "
 			+ "FROM Users";
 
     private final static String strGetAllUsersWithLimitOffset = strGetAllUsers
             + " LIMIT ? OFFSET ?";
 
     // single result
 	private final static String strGetUserWithEmail = strGetAllUsers + " WHERE Email = ?";
 
     // single result
 	private final static String strGetUserWithId = strGetAllUsers + " WHERE id = ?";
 
 
 
     /*
        UPDATE statements
      */
 	private final static String strUpdateRequest = "UPDATE Requests SET "
 			+ "UserID = ?, Algorithm, JSONRequest = ?, JSONResponse = ?, "
 			+ "PendingFlag = ?, Costs = ?, PaidFlag = ?, RequestDate = ?, "
 			+ "FinishedDate = ?, CPUTime = ?, FailedFlag = ?, "
 			+ "FailDescription = ? WHERE id = ?";
 
 	private final static String strUpdateRequestWithComputeResult = "UPDATE Requests SET JSONResponse = ?, "
 			+ "PendingFlag = ?, Costs = ?, "
 			+ "FinishedDate = ?, CPUTime = ?, FailedFlag = ?, "
 			+ "FailDescription = ? WHERE id = ?";
 
 	private final static String strUpdateUser = "UPDATE Users SET "
 			+ "Email = ?, Passwordhash = ?, Salt = ?, AdminFlag = ?, "
 			+ "Status = ?, FirstName = ?, LastName = ?, Address = ?, "
 			+ "RegistrationDate = ?, VerifiedDate = ?, DeleteRequestDate = ? "
 			+ "WHERE id = ?";
 
 
     /*
        DELETE statements
      */
 	private final static String strDeleteRequestWithRequestId = "DELETE FROM Requests WHERE id = ?";
 
 	private final static String strDeleteRequestsOfUserWithUserId = "DELETE FROM Requests WHERE UserID = ?";
 
 	private final static String strDeleteUserWithUserId = "DELETE FROM Users WHERE id = ?";
 
 	private final static String strDeleteUserWithEmail = "DELETE FROM Users WHERE Email = ?";
 
 
 
 	/**
 	 * Tries to establish a database connection and upholds the connection until
 	 * the garbage collection deletes the object or the connection is
 	 * disconnected. Also creates prepared statements.
 	 * 
 	 * @param url
 	 *            A database driver specific url of the form
 	 *            <i>jdbc:subprotocol:subnamewithoutdatabase</i> where
 	 *            <i>subnamewithoutdatabase</i> is the server address without
 	 *            the database name but with a slash at the end</br> Example:
 	 *            "jdbc:mysql://localhost:3306/"
 	 * @param dbName
 	 *            The database name Example: "tourenplaner"
 	 * @param userName
 	 *            User name of the database user account
 	 * @param password
 	 *            To the user according password
 	 * @throws SQLException
 	 *             Thrown if connection could not be established or if errors
 	 *             occur while creating prepared statements
 	 * @see java.sql.DriverManager##getConnection(java.lang.String,java.lang.String, java.lang.String)
 	 */
 	public DatabaseManager(String url, String dbName, String userName,
 			String password) throws SQLException {
         this.url = url;
         this.dbName = dbName;
         this.userName = userName;
         this.password = password;
         
 		this.init();
 	}
 
 
     private void init() throws SQLException {
         con = DriverManager.getConnection(url + dbName, userName, password);
 
         pstAddNewRequest = con.prepareStatement(strAddNewRequest,
                 Statement.RETURN_GENERATED_KEYS);
         pstAddNewUser = con.prepareStatement(strAddNewUser,
                 Statement.RETURN_GENERATED_KEYS);
         pstGetAllRequests = con.prepareStatement(strGetAllRequests);
         pstGetAllUsers = con.prepareStatement(strGetAllUsers);
         pstGetUserWithEmail = con.prepareStatement(strGetUserWithEmail);
         pstGetUserWithId = con.prepareStatement(strGetUserWithId);
         pstUpdateRequest = con.prepareStatement(strUpdateRequest);
         pstUpdateRequestWithComputeResult = con
                 .prepareStatement(strUpdateRequestWithComputeResult);
         pstUpdateUser = con.prepareStatement(strUpdateUser);
         pstDeleteRequestWithRequestId = con
                 .prepareStatement(strDeleteRequestWithRequestId);
         pstDeleteRequestsOfUserWithUserId = con
                 .prepareStatement(strDeleteRequestsOfUserWithUserId);
         pstDeleteUserWithUserId = con
                 .prepareStatement(strDeleteUserWithUserId);
         pstDeleteUserWithEmail = con
                 .prepareStatement(strDeleteUserWithEmail);
         pstGetAllRequestsWithLimitOffset = con
                 .prepareStatement(strGetAllRequestsWithLimitOffset);
         pstGetRequestWithRequestId = con
                 .prepareStatement(strGetRequestWithRequestId);
         pstGetRequestsWithUserId = con
                 .prepareStatement(strGetRequestsWithUserId);
         pstGetRequestsWithUserIdLimitOffset = con
                 .prepareStatement(strGetRequestsWithUserIdLimitOffset);
         pstGetAllUsersWithLimitOffset = con
                 .prepareStatement(strGetAllUsersWithLimitOffset);
         pstCountAllRequests = con.prepareStatement(strCountAllRequests);
         pstCountRequestsWithUserId = con.prepareStatement(strCountRequestsWithUserId);
         pstCountAllUsers = con.prepareStatement(strCountAllUsers);
     }
 
 	/**
 	 * Tries to insert a new request dataset into the database. The inserted
 	 * dataset will be pending, have no costs and will be unpaid.</br>SQL
 	 * command: {@value #strAddNewRequest}
 	 * 
 	 * @param userID
 	 *            The id of the user, who has sent the request
 	 * @param algorithm
 	 *            The algorithm name
 	 * @param jsonRequest
 	 *            The request encoded as byte array
 	 * @return Returns the inserted request object only if the id could received
 	 *         from the database and the insert was successful, else an
 	 *         exception will be thrown.
 	 * @throws SQLFeatureNotSupportedException
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if the insertion failed.
 	 */
 	public RequestDataset addNewRequest(int userID, String algorithm,
 			byte[] jsonRequest) throws SQLFeatureNotSupportedException,
 			SQLException {
 
 		/*
 		 * id INT NOT NULL AUTO_INCREMENT, UserID INT NOT NULL REFERENCES Users
 		 * (id), Algorithm VARCHAR(255) NOT NULL, JSONRequest LONGBLOB,
 		 * JSONResponse LONGBLOB DEFAULT NULL, PendingFlag BOOL NOT NULL DEFAULT
 		 * 1, Costs INT NOT NULL DEFAULT 0, PaidFlag BOOL NOT NULL DEFAULT 0,
 		 * RequestDate DATETIME NOT NULL, FinishedDate DATETIME DEFAULT NULL,
 		 * CPUTime BIGINT NOT NULL DEFAULT 0, FailedFlag BOOL NOT NULL DEFAULT
 		 * 0, FailDescription TEXT DEFAULT NULL, PRIMARY KEY (ID), FOREIGN KEY
 		 * (UserID) REFERENCES Users (id)
 		 */
 
 		RequestDataset request = null;
 		ResultSet generatedKeyResultSet = null;
 
         boolean hasKey = false;
 
         int tryAgain = 2;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstAddNewRequest.setInt(1, userID);
                 pstAddNewRequest.setString(2, algorithm);
                 pstAddNewRequest.setBytes(3, jsonRequest);
                 pstAddNewRequest.setTimestamp(4, stamp);
 
                 pstAddNewRequest.executeUpdate();
 
                 request = new RequestDataset(-1, userID, algorithm, jsonRequest, null,
                         true, 0, false, new Date(stamp.getTime()), null, 0, false, null);
 
                 hasKey = false;
                 generatedKeyResultSet = pstAddNewRequest.getGeneratedKeys();
                 if (generatedKeyResultSet.next()) {
                     request.requestID = generatedKeyResultSet.getInt(1);
                     hasKey = true;
                 }
                 generatedKeyResultSet.close();
                 tryAgain = 0;
 
 
             } catch (Exception e) {
                if (tryAgain >= 0) {
                     log.log(Level.WARNING, "Before last try", e);
                     try {
                         Thread.sleep(3000);
                         con.close();
                         init();
                     } catch (InterruptedException e1) {
                     }
                 } else {
                     log.log(Level.SEVERE, "After last try", e);
                 }
             }
 
         }
 
 
         if (!hasKey) {
             log.severe("Current database doesn't support java.sql.Statement.getGeneratedKeys()");
             throw new SQLFeatureNotSupportedException(
                     "Current database doesn't support "
                             + "java.sql.Statement.getGeneratedKeys()");
         }
 
 		return request;
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database. </br>SQL command:
 	 * {@value #strAddNewUser}
 	 * 
 	 * @param email
 	 *            Have to be unique, that means another user must not have the
 	 *            same email. Parameter will be trimmed from this method.
 	 * @param passwordhash
 	 *            Hash of the user password. Parameter will be trimmed from this
 	 *            method.
 	 * @param salt
 	 *            Salt for the user password hash. Parameter will be trimmed
 	 *            from this method.
 	 * @param firstName
 	 *            First name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param address
 	 *            Address of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param isAdmin
 	 *            True, if user is an admin, else false.
 	 * @return Returns the inserted user object only if the id could received
 	 *         from the database and the insert was successful. If the insert
 	 *         was not successful because the email already existed, null will
 	 *         be returned.
 	 * @throws SQLFeatureNotSupportedException
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if other errors occurred than a duplicate email.
 	 */
 	public UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin) throws SQLFeatureNotSupportedException,
 			SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.NeedsVerification, false);
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database, but request should
 	 * have legit admin authentication (will not be checked within this method).
 	 * New user will be verified. </br>SQL command: {@value #strAddNewUser}
 	 * 
 	 * @param email
 	 *            Have to be unique, that means another user must not have the
 	 *            same email. Parameter will be trimmed from this method.
 	 * @param passwordhash
 	 *            Hash of the user password. Parameter will be trimmed from this
 	 *            method.
 	 * @param salt
 	 *            Salt for the user password hash. Parameter will be trimmed
 	 *            from this method.
 	 * @param firstName
 	 *            First name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param lastName
 	 *            Last Name of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param address
 	 *            Address of the user. Parameter will be trimmed from this
 	 *            method.
 	 * @param isAdmin
 	 *            True, if user is an admin, else false.
 	 * @return Returns the inserted user object only if the id could received
 	 *         from the database and the insert was successful. If the insert
 	 *         was not successful because the email already existed, null will
 	 *         be returned.
 	 * @throws SQLFeatureNotSupportedException
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if other errors occurred than a duplicate email.
 	 */
 	public UserDataset addNewVerifiedUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin) throws SQLFeatureNotSupportedException,
 			SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.Verified, true);
 	}
 
 	private UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin, UserStatusEnum status, boolean isVerified)
 			throws SQLFeatureNotSupportedException, SQLException {
 
 		/*
 		 * id INT NOT NULL AUTO_INCREMENT, Email VARCHAR(255) NOT NULL UNIQUE,
 		 * Passwordhash TEXT NOT NULL, Salt TEXT NOT NULL, AdminFlag BOOL NOT
 		 * NULL DEFAULT 0, Status ENUM ('NeedsVerification',
 		 * 'VerificationFailed', 'Verified', 'Delete') NOT NULL DEFAULT
 		 * 'NeedsVerification', FirstName TEXT NOT NULL, LastName TEXT NOT NULL,
 		 * Address TEXT NOT NULL, RegistrationDate DATETIME NOT NULL,
 		 * VerifiedDate DATETIME DEFAULT NULL, DeleteRequestDate DATETIME
 		 * DEFAULT NULL, PRIMARY KEY (ID)
 		 */
 
 		UserDataset user = null;
 
 		email = email.trim();
 		passwordhash = passwordhash.trim();
 		salt = salt.trim();
 		firstName = firstName.trim();
 		lastName = lastName.trim();
 		address = address.trim();
 
 		try {
 
 			Timestamp registeredStamp = new Timestamp(
 					System.currentTimeMillis());
 			Timestamp verifiedStamp = null;
 			Date registeredDate = timestampToDate(registeredStamp);
 			Date verifiedDate = null;
 
 			if (isVerified) {
 				verifiedStamp = registeredStamp;
 				verifiedDate = registeredDate;
 			}
 
 			pstAddNewUser.setString(1, email);
 			pstAddNewUser.setString(2, passwordhash);
 			pstAddNewUser.setString(3, salt);
 			pstAddNewUser.setString(4, firstName);
 			pstAddNewUser.setString(5, lastName);
 			pstAddNewUser.setString(6, address);
 			pstAddNewUser.setBoolean(7, isAdmin);
 			pstAddNewUser.setString(8, status.toString());
 			pstAddNewUser.setTimestamp(9, registeredStamp);
 			pstAddNewUser.setTimestamp(10, verifiedStamp);
 
 			pstAddNewUser.executeUpdate();
 
 			user = new UserDataset(-1, email, passwordhash, salt, isAdmin,
 					status, firstName, lastName, address, registeredDate,
 					verifiedDate, null);
 
 			// try {
 			ResultSet generatedKeyResultSet = null;
 
 			generatedKeyResultSet = pstAddNewUser.getGeneratedKeys();
 
 			boolean hasKey = false;
 			if (generatedKeyResultSet.next()) {
 				user.id = generatedKeyResultSet.getInt(1);
 				hasKey = true;
 			}
 			generatedKeyResultSet.close();
 
 			if (!hasKey) {
                 log.severe("Current database doesn't support java.sql.Statement.getGeneratedKeys()");
 				user = this.getUser(email);
 			}
 			/*
 			 * } catch (SQLFeatureNotSupportedException ex) { if
 			 * (ex.getNextException() != null) { throw ex; } }
 			 */
 			// catch if duplicate key error
 		} catch (SQLIntegrityConstraintViolationException ex) {
 			if (ex.getNextException() == null) {
 				return null;
 			}
 			throw ex;
 		}
 		return user;
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>request.id</code></b>). All values within the given object will
 	 * be written into the database, so all old values within the row will be
 	 * overwritten. <b><code>request.id</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command:
 	 * {@value #strUpdateRequest}
 	 * 
 	 * @param request
 	 *            The request object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
 	 */
 	public void updateRequest(RequestDataset request) throws SQLException {
         int tryAgain = 2;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 pstUpdateRequest.setInt(1, request.userID);
                 pstUpdateRequest.setBytes(2, request.jsonRequest);
                 pstUpdateRequest.setBytes(3, request.jsonResponse);
                 pstUpdateRequest.setBoolean(4, request.isPending);
                 pstUpdateRequest.setInt(5, request.costs);
                 pstUpdateRequest.setBoolean(6, request.isPaid);
                 pstUpdateRequest.setTimestamp(7, dateToTimestamp(request.requestDate));
                 pstUpdateRequest.setTimestamp(8, dateToTimestamp(request.finishedDate));
                 pstUpdateRequest.setLong(9, request.duration);
                 pstUpdateRequest.setBoolean(10, request.hasFailed);
                 pstUpdateRequest.setString(11, request.failDescription);
                 pstUpdateRequest.setInt(12, request.requestID);
 
                 pstUpdateRequest.executeUpdate();
 
                 tryAgain = 0;
 
             } catch (Exception e) {
                if (tryAgain >= 0) {
                     log.log(Level.WARNING, "Before last try", e);
                     try {
                         Thread.sleep(3000);
                         con.close();
                         init();
                     } catch (InterruptedException e1) {
                     }
                 } else {
                     log.log(Level.SEVERE, "After last try", e);
                 }
             }
 
         }
 
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>requestID</code></b>). The given parameters will overwrite the
 	 * old values in the database row. FinishedDate will be set to the current
 	 * timestamp. <b><code>requestID</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command:
 	 * {@value #strUpdateRequestWithComputeResult}
 	 * 
 	 * @param requestID
 	 * @param jsonResponse
 	 * @param isPending
 	 * @param costs
 	 * @param cpuTime
 	 * @param hasFailed
 	 * @param failDescription
 	 * @throws SQLException
 	 *             Thrown if update fails.
 	 */
 	public void updateRequestWithComputeResult(int requestID,
 			byte[] jsonResponse, boolean isPending, int costs, long cpuTime,
 			boolean hasFailed, String failDescription) throws SQLException {
 
         int tryAgain = 2;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             try {
 
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstUpdateRequestWithComputeResult.setBytes(1, jsonResponse);
                 pstUpdateRequestWithComputeResult.setBoolean(2, isPending);
                 pstUpdateRequestWithComputeResult.setInt(3, costs);
                 pstUpdateRequestWithComputeResult.setTimestamp(4, stamp);
                 pstUpdateRequestWithComputeResult.setLong(5, cpuTime);
                 pstUpdateRequestWithComputeResult.setBoolean(6, hasFailed);
                 pstUpdateRequestWithComputeResult.setString(7, failDescription);
                 pstUpdateRequestWithComputeResult.setInt(8, requestID);
 
                 pstUpdateRequestWithComputeResult.executeUpdate();
 
                 tryAgain = 0;
 
             } catch (Exception e) {
                if (tryAgain >= 0) {
                     log.log(Level.WARNING, "Before last try", e);
                     try {
                         Thread.sleep(3000);
                         con.close();
                         init();
                     } catch (InterruptedException e1) {
                     }
                 } else {
                     log.log(Level.SEVERE, "After last try", e);
                 }
             }
 
         }
 	}
 
 	/**
 	 * Updates the Users table row with the id given through the parameter (<b>
 	 * <code>request.id</code></b>). All values within the given object will be
 	 * written into the database, so all old values within the row will be
 	 * overwritten. <b><code>user.id</code></b> has to be > 0 and must exists
 	 * within the database table. </br>SQL command: {@value #strUpdateUser}
 	 * 
 	 * @param user
 	 *            The user object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
 	 */
 	public void updateUser(UserDataset user) throws SQLException {
 		pstUpdateUser.setString(1, user.email);
 		pstUpdateUser.setString(2, user.passwordhash);
 		pstUpdateUser.setString(3, user.salt);
 		pstUpdateUser.setBoolean(4, user.admin);
 		pstUpdateUser.setString(5, user.status.toString());
 		pstUpdateUser.setString(6, user.firstName);
 		pstUpdateUser.setString(7, user.lastName);
 		pstUpdateUser.setString(8, user.address);
 		pstUpdateUser.setTimestamp(9, dateToTimestamp(user.registrationDate));
 		pstUpdateUser.setTimestamp(10, dateToTimestamp(user.verifiedDate));
 		pstUpdateUser.setTimestamp(11, dateToTimestamp(user.deleteRequestDate));
 		pstUpdateUser.setInt(12, user.id);
 
 		pstUpdateUser.executeUpdate();
 
 		// do this only if database command succeeds (sql exception throws us
 		// out before this block)
 		if (user != null) {
 			userDatasetCache_mail.put(user.email, user);
 			userDatasetCache_id.put(user.id, user);
 		}
 	}
 
 	/**
 	 * Deletes the Requests table row with the given request id. </br>SQL
 	 * command: {@value #strDeleteRequestWithRequestId}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteRequest(int id) throws SQLException {
 		pstDeleteRequestWithRequestId.setInt(1, id);
 
 		pstDeleteRequestWithRequestId.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Requests table rows with the given user id. </br>SQL command:
 	 * {@value #strDeleteRequestsOfUserWithUserId}
 	 * 
 	 * @param userId
 	 *            User id of the user, whose requests should be deleted.
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteRequestsOfUser(int userId) throws SQLException {
 		pstDeleteRequestsOfUserWithUserId.setInt(1, userId);
 
 		pstDeleteRequestsOfUserWithUserId.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Users table row with the given user id. Depending on the
 	 * database configuration(for example strict mode) maybe the according
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command:
 	 * {@value #strDeleteUserWithUserId}
 	 * 
 	 * @param userId
 	 *            User id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteUser(int userId) throws SQLException {
 		pstDeleteUserWithUserId.setInt(1, userId);
 
 		pstDeleteUserWithUserId.executeUpdate();
 	}
 
 	/**
 	 * Deletes the Users table row with the given user email. Depending on the
 	 * database configuration(for example strict mode) maybe the according
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command: {@value #strDeleteUserWithEmail}
 	 * 
 	 * @param email
 	 * @throws SQLException
 	 *             Thrown if delete fails.
 	 */
 	public void deleteUser(String email) throws SQLException {
 		pstDeleteUserWithEmail.setString(1, email);
 
 		pstDeleteUserWithEmail.executeUpdate();
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table. If no requests
 	 * are within the table, an empty list will be returned. </br>SQL command:
 	 * {@value #strGetAllRequests}
 	 * 
 	 * @return A list with all requests. If no requests exists, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getAllRequests() throws SQLException {
 		ResultSet resultSet = pstGetAllRequests.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1), resultSet
 					.getInt(2), resultSet.getString(3), resultSet.getBytes(4),
 					resultSet.getBytes(5), resultSet.getBoolean(6), resultSet
 							.getInt(7), resultSet.getBoolean(8),
 					timestampToDate(resultSet.getTimestamp(9)),
 					timestampToDate(resultSet.getTimestamp(10)), resultSet
 							.getLong(11), resultSet.getBoolean(12), resultSet
 							.getString(13)));
 		}
 
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table with regard to
 	 * the limit and offset constraints. If no requests are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * </br>SQL command: {@value #strGetAllRequestsWithLimitOffset}
 	 * 
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected requests. If no requests selected, the
 	 *         list is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getAllRequests(int limit, int offset)
 			throws SQLException {
 
 		pstGetAllRequestsWithLimitOffset.setInt(1, limit);
 		pstGetAllRequestsWithLimitOffset.setInt(2, offset);
 
 		ResultSet resultSet = pstGetAllRequestsWithLimitOffset.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1), resultSet
 					.getInt(2), resultSet.getString(3), resultSet.getBytes(4),
 					resultSet.getBytes(5), resultSet.getBoolean(6), resultSet
 							.getInt(7), resultSet.getBoolean(8),
 					timestampToDate(resultSet.getTimestamp(9)),
 					timestampToDate(resultSet.getTimestamp(10)), resultSet
 							.getLong(11), resultSet.getBoolean(12), resultSet
 							.getString(13)));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a request object of the Requests table with the given request id.
 	 * 
 	 * </br>SQL command: {@value #strGetRequestWithRequestId}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @return A request object if the request with the id exists, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public RequestDataset getRequest(int id) throws SQLException {
 
 		pstGetRequestWithRequestId.setInt(1, id);
 		ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
 		RequestDataset request = null;
 
 		while (resultSet.next()) {
 			request = new RequestDataset(resultSet.getInt(1),
 					resultSet.getInt(2), resultSet.getString(3),
 					resultSet.getBytes(4), resultSet.getBytes(5),
 					resultSet.getBoolean(6), resultSet.getInt(7),
 					resultSet.getBoolean(8),
 					timestampToDate(resultSet.getTimestamp(9)),
 					timestampToDate(resultSet.getTimestamp(10)),
 					resultSet.getLong(11), resultSet.getBoolean(12),
 					resultSet.getString(13));
 		}
 		resultSet.close();
 
 		return request;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table which have the
 	 * given user id. If no requests are found with the given user id or the
 	 * table is empty, an empty list will be returned. </br>SQL command:
 	 * {@value #strGetRequestsWithUserId}
 	 * 
 	 * @param userId
 	 * @return A list with all selected requests. If no requests selected, the
 	 *         list is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getRequests(int userId) throws SQLException {
 		pstGetRequestsWithUserId.setInt(1, userId);
 
 		ResultSet resultSet = pstGetRequestsWithUserId.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1), resultSet
 					.getInt(2), resultSet.getString(3), resultSet.getBytes(4),
 					resultSet.getBytes(5), resultSet.getBoolean(6), resultSet
 							.getInt(7), resultSet.getBoolean(8),
 					timestampToDate(resultSet.getTimestamp(9)),
 					timestampToDate(resultSet.getTimestamp(10)), resultSet
 							.getLong(11), resultSet.getBoolean(12), resultSet
 							.getString(13)));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all requests within the Requests table which have the
 	 * given user id with regard to the limit and offset constraints. If no
 	 * requests are found with the given user id and given constraints or the
 	 * table is empty, an empty list will be returned. </br>SQL command:
 	 * {@value #strGetRequestsWithUserIdLimitOffset}
 	 * 
 	 * @param userId
 	 *            User id
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected requests. If no requests selected, the
 	 *         list is empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<RequestDataset> getRequests(int userId, int limit, int offset)
 			throws SQLException {
 		pstGetRequestsWithUserIdLimitOffset.setInt(1, userId);
 		pstGetRequestsWithUserIdLimitOffset.setInt(2, limit);
 		pstGetRequestsWithUserIdLimitOffset.setInt(3, offset);
 
 		ResultSet resultSet = pstGetRequestsWithUserIdLimitOffset
 				.executeQuery();
 		ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new RequestDataset(resultSet.getInt(1), resultSet
 					.getInt(2), resultSet.getString(3), resultSet.getBytes(4),
 					resultSet.getBytes(5), resultSet.getBoolean(6), resultSet
 							.getInt(7), resultSet.getBoolean(8),
 					timestampToDate(resultSet.getTimestamp(9)),
 					timestampToDate(resultSet.getTimestamp(10)), resultSet
 							.getLong(11), resultSet.getBoolean(12), resultSet
 							.getString(13)));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all users within the Users table. If the table is empty,
 	 * an empty list will be returned. </br>SQL command:
 	 * {@value #strGetAllUsers}
 	 * 
 	 * @return A list with all selected users. If no users selected, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers() throws SQLException {
 		ResultSet resultSet = pstGetAllUsers.executeQuery();
 		ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new UserDataset(resultSet.getInt(1), resultSet
 					.getString(2), resultSet.getString(3), resultSet
 					.getString(4), resultSet.getBoolean(5), UserStatusEnum
 					.valueOf(resultSet.getString(6)), resultSet.getString(7),
 					resultSet.getString(8), resultSet.getString(9),
 					timestampToDate(resultSet.getTimestamp(10)),
 					timestampToDate(resultSet.getTimestamp(11)),
 					timestampToDate(resultSet.getTimestamp(12))));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a list with all users within the Users table with regard to the
 	 * limit and offset constraints. If no users are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * </br>SQL command: {@value #strGetAllUsersWithLimitOffset}
 	 * 
 	 * @param limit
 	 *            How many rows should maximal selected.
 	 * @param offset
 	 *            How many rows should be skimmed.
 	 * @return A list with all selected users. If no users selected, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers(int limit, int offset)
 			throws SQLException {
 		pstGetAllUsersWithLimitOffset.setInt(1, limit);
 		pstGetAllUsersWithLimitOffset.setInt(2, offset);
 
 		ResultSet resultSet = pstGetAllUsersWithLimitOffset.executeQuery();
 		ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
 		while (resultSet.next()) {
 
 			list.add(new UserDataset(resultSet.getInt(1), resultSet
 					.getString(2), resultSet.getString(3), resultSet
 					.getString(4), resultSet.getBoolean(5), UserStatusEnum
 					.valueOf(resultSet.getString(6)), resultSet.getString(7),
 					resultSet.getString(8), resultSet.getString(9),
 					timestampToDate(resultSet.getTimestamp(10)),
 					timestampToDate(resultSet.getTimestamp(11)),
 					timestampToDate(resultSet.getTimestamp(12))));
 		}
 		resultSet.close();
 
 		return list;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given email. </br>SQL
 	 * command: {@value #strGetUserWithEmail}
 	 * 
 	 * @param email
 	 * @return The user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(String email) throws SQLException {
 		UserDataset user = null;
 
 		if ((user = userDatasetCache_mail.get(email)) != null) {
 			log.fine("Retrieved user " + user.email + " with id "
 					+ user.id + " from cache");
 			return user;
 		}
 
 		pstGetUserWithEmail.setString(1, email);
 		ResultSet resultSet = pstGetUserWithEmail.executeQuery();
 
 		while (resultSet.next()) {
 
 			user = new UserDataset(resultSet.getInt(1), resultSet.getString(2),
 					resultSet.getString(3), resultSet.getString(4),
 					resultSet.getBoolean(5), UserStatusEnum.valueOf(resultSet
 							.getString(6)), resultSet.getString(7),
 					resultSet.getString(8), resultSet.getString(9),
 					timestampToDate(resultSet.getTimestamp(10)),
 					timestampToDate(resultSet.getTimestamp(11)),
 					timestampToDate(resultSet.getTimestamp(12)));
 		}
 		resultSet.close();
 
 		if (user != null) {
 			userDatasetCache_mail.put(email, user);
 			userDatasetCache_id.put(user.id, user);
 		}
 
 		return user;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given user id. </br>SQL
 	 * command: {@value #strGetUserWithId}
 	 * 
 	 * @param id
 	 *            User id
 	 * @returnThe user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(int id) throws SQLException {
 		UserDataset user = null;
 
 		if ((user = userDatasetCache_mail.get(id)) != null) {
 			// TODO: debug output
 			log.fine("Retrieved user " + user.email + " with id "
 					+ user.id + " from cache");
 			return user;
 		}
 
 		pstGetUserWithId.setInt(1, id);
 		ResultSet resultSet = pstGetUserWithId.executeQuery();
 
 		while (resultSet.next()) {
 
 			user = new UserDataset(resultSet.getInt(1), resultSet.getString(2),
 					resultSet.getString(3), resultSet.getString(4),
 					resultSet.getBoolean(5), UserStatusEnum.valueOf(resultSet
 							.getString(6)), resultSet.getString(7),
 					resultSet.getString(8), resultSet.getString(9),
 					timestampToDate(resultSet.getTimestamp(10)),
 					timestampToDate(resultSet.getTimestamp(11)),
 					timestampToDate(resultSet.getTimestamp(12)));
 		}
 		resultSet.close();
 
 		if (user != null) {
 			userDatasetCache_mail.put(user.email, user);
 			userDatasetCache_id.put(id, user);
 		}
 
 		return user;
 	}
 
 
     /**
      * Returns the number of data sets within the Requests table
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfRequests() throws SQLException {
         ResultSet resultSet = pstCountAllRequests.executeQuery();
 
         if (resultSet.next()) {
             int count = resultSet.getInt(1);
             resultSet.close();
             return count;
         }
 
         log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
         resultSet.close();
         return 0;
     }
 
     /**
      * Returns the number of data sets with a certain UserID within the Requests table    
      * @param userId The UserID from the user for which you want to get the number of data sets
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfRequestsWithUserId(int userId) throws SQLException {
 
         pstCountRequestsWithUserId.setInt(1, userId);
         ResultSet resultSet = pstCountRequestsWithUserId.executeQuery();
 
         if (resultSet.next()) {
             int count = resultSet.getInt(1);
             resultSet.close();
             return count;
         }
 
         log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
         resultSet.close();
         return 0;
     }
 
     /**
      * Returns the number of data sets within the Users table
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfUsers() throws SQLException {
         ResultSet resultSet = pstCountAllUsers.executeQuery();
 
         if (resultSet.next()) {
             int count = resultSet.getInt(1);
             resultSet.close();
             return count;
         }
 
         log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
         resultSet.close();
         return 0;
     }
 
 
 
 	/**
 	 * Converts a sql timestamp to a java date
 	 * 
 	 * @param stamp
 	 *            The sql timestamp
 	 * @return The generated jave date
 	 */
 	private Date timestampToDate(Timestamp stamp) {
 		if (stamp == null) {
 			return null;
 		}
 		return new Date(stamp.getTime());
 	}
 
 	/**
 	 * Converts a java date to a sql timestamp
 	 * 
 	 * @param date
 	 *            The java date
 	 * @return The generated sql timestamp
 	 */
 	private Timestamp dateToTimestamp(Date date) {
 		if (date == null) {
 			return null;
 		}
 		return new Timestamp(date.getTime());
 	}
 
 	/**
 	 * Yet only for testing purpose
 	 */
 	public void printDatabaseInformation() {
 		try {
 			addNewUser("sascha@tourenplaner", "xyz", "pepper", "sascha",
 					"verratinet", "", true);
 			// addNewRequest(10,
 			// ("EinTestBlob mit falscher UserID").getBytes());
 		} catch (SQLIntegrityConstraintViolationException e) {
 
 			SQLException ex = e;
 
 			do {
 
 				System.out.println("errorcode: " + ex.getErrorCode());
 				System.out.println("sqlstate: " + ex.getSQLState());
 				try {
 					System.out.println("stateType: "
 							+ con.getMetaData().getSQLStateType());
 				} catch (SQLException e1) {
 					System.out.println("someConnectionError:");
 					e1.printStackTrace();
 				}
 				System.out.println(ex.getLocalizedMessage());
 
 				ex = ex.getNextException();
 			} while (ex != null);
 
 			System.out.println("xopen: " + DatabaseMetaData.sqlStateXOpen);
 			System.out.println("sql: " + DatabaseMetaData.sqlStateSQL);
 			System.out.println("sql99: " + DatabaseMetaData.sqlStateSQL99);
 
 		} catch (SQLException ex) {
 			System.out.println("anotherConnectionError:");
 			ex.printStackTrace();
 		}
 
 	}
 
 }
