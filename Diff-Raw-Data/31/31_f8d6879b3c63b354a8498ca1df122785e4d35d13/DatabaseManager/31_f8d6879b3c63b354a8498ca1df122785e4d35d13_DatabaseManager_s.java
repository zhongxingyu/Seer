 /**
  * 
  */
 package database;
 
 import config.ConfigManager;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
  */
 public class DatabaseManager {
     
     private static Logger log = Logger.getLogger("database");
 
     private final int maxTries = initMaxTries();
 
     private final DataSource dataSource;
 
 
 
 	protected DatabaseManager(DataSource dataSource) {
         this.dataSource = dataSource;
 	}
 
 
     private static int initMaxTries() {
         int maxTries = ConfigManager.getInstance().getEntryInt("maxdbtries", 2);
         if (maxTries > 0) {
             return maxTries;
         }
         return 2;
     }
 
 
 	/**
 	 * Tries to insert a new request dataset into the database. The inserted
 	 * dataset will have the status pending and have 0 cost.<br />SQL
 	 * command: {@value SqlStatementConstants#strAddNewRequest}
 	 * 
 	 * @param userID
 	 *            The id of the user, who has sent the request
 	 * @param algorithm
 	 *            The algorithm name
 	 * @param jsonRequest
 	 *            The request encoded as byte array
 	 * @return Returns the request id if the id could received
 	 *         from the database and the insert was successful, else an
 	 *         exception will be thrown. Request id should be > 0.
 	 * @throws SQLFeatureNotSupportedException
 	 *             Thrown if the id could not received or another function is
 	 *             not supported by driver.
 	 * @throws SQLException
 	 *             Thrown if the insertion failed.
 	 */
 	public int addNewRequest(int userID, String algorithm,
 			byte[] jsonRequest) throws SQLException {
 
 		/*
               id              INT           NOT NULL AUTO_INCREMENT,
               UserID          INT           NOT NULL REFERENCES Users (id),
               Algorithm       VARCHAR(255)  NOT NULL,
               JSONRequest     LONGBLOB,
               JSONResponse    LONGBLOB               DEFAULT NULL,
               Cost            INT           NOT NULL DEFAULT 0,
               RequestDate     DATETIME      NOT NULL,
               FinishedDate    DATETIME               DEFAULT NULL,
               CPUTime         BIGINT        NOT NULL DEFAULT 0,
               Status          ENUM ('ok','pending','failed')
                                             NOT NULL DEFAULT 'pending',
               PRIMARY KEY (ID),
               FOREIGN KEY (UserID) REFERENCES Users (id)
 		 */
 
 		ResultSet generatedKeyResultSet;
 
         boolean hasKey = false;
         int requestID = -1;
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstAddNewRequest = null;
 
             try {
 
                 pstAddNewRequest = SqlStatementConstants.AddNewRequest.createPreparedStatement(dataSource);
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstAddNewRequest.setInt(1, userID);
                 pstAddNewRequest.setString(2, algorithm);
                 pstAddNewRequest.setBytes(3, jsonRequest);
                 pstAddNewRequest.setTimestamp(4, stamp);
 
                 pstAddNewRequest.executeUpdate();
                 // if no exception occurred, request is added, so do not try again
                 tryAgain = 0;
 
                 hasKey = false;
                 generatedKeyResultSet = pstAddNewRequest.getGeneratedKeys();
                 if (generatedKeyResultSet.next()) {
                     requestID = generatedKeyResultSet.getInt(1);
                     hasKey = true;
                 }
                 generatedKeyResultSet.close();
 
                 log.fine("Database query successful");
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstAddNewRequest);
             }
 
         }
 
 
         if (!hasKey) {
             log.severe("Current database doesn't support java.sql.Statement.getGeneratedKeys()");
             throw new SQLFeatureNotSupportedException(
                     "Current database doesn't support "
                             + "java.sql.Statement.getGeneratedKeys()");
         }
 
 		return requestID;
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database. <br />SQL command:
 	 * {@value SqlStatementConstants#strAddNewUser}
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
      *            Last name of the user. Parameter will be trimmed from this
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
 			boolean isAdmin) throws SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.needs_verification, false);
 	}
 
 	/**
 	 * Tries to insert a new user dataset into the database, but request should
 	 * have legit admin authentication (will not be checked within this method).
 	 * New user will be verified. <br />SQL command: {@value SqlStatementConstants#strAddNewUser}
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
 			boolean isAdmin) throws SQLException {
 
 		return addNewUser(email, passwordhash, salt, firstName, lastName,
 				address, isAdmin, UserStatusEnum.verified, true);
 	}
 
     /**
      * <br />SQL command:
      * {@value SqlStatementConstants#strAddNewUser}
      * @param email email
      * @param passwordhash password hash
      * @param salt salt
      * @param firstName first name
      * @param lastName last name
      * @param address address
      * @param isAdmin isAdmin flag
      * @param status status
      * @param isVerified isVerified flag
      * @return user object
      * @throws SQLFeatureNotSupportedException
      *             Thrown if the id could not received or another function is
      *             not supported by driver.
      * @throws SQLException
      *            Thrown if other errors occurred than a duplicate email.
      *
      */
 	private UserDataset addNewUser(String email, String passwordhash,
 			String salt, String firstName, String lastName, String address,
 			boolean isAdmin, UserStatusEnum status, boolean isVerified)
 			throws SQLException {
 
 		/*
               id                INT           NOT NULL AUTO_INCREMENT,
               Email             VARCHAR(255)  NOT NULL UNIQUE,
               Passwordhash      TEXT          NOT NULL,
               Salt              TEXT          NOT NULL,
               AdminFlag         BOOL          NOT NULL DEFAULT 0,
               Status            ENUM ('needs_verification','verified','deleted')
                                               NOT NULL DEFAULT 'needs_verification',
               FirstName         TEXT          NOT NULL,
               LastName          TEXT          NOT NULL,
               Address           TEXT          NOT NULL,
               RegistrationDate  DATETIME      NOT NULL,
               VerifiedDate      DATETIME               DEFAULT NULL,
               PRIMARY KEY (ID)
 		 */
 
 
 		UserDataset user = null;
 
 		email = email.trim();
 		passwordhash = passwordhash.trim();
 		salt = salt.trim();
 		firstName = firstName.trim();
 		lastName = lastName.trim();
 		address = address.trim();
 
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstAddNewUser = null;
 
             try {
 
                 pstAddNewUser = SqlStatementConstants.AddNewUser.createPreparedStatement(dataSource);
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
 
                 try {
 
                     pstAddNewUser.executeUpdate();
                     // if no exception occurred, user is added, so do not try again
                     tryAgain = -1;
 
                    // catch if duplicate key error
                 } catch (SQLIntegrityConstraintViolationException ex) {
                     if (ex.getNextException() == null) {
                         return null;
                     }
                     throw ex;
                 }
 
 
                 user = new UserDataset(-1, email, passwordhash, salt, isAdmin,
                         status, firstName, lastName, address, registeredDate,
                         verifiedDate);
 
                 ResultSet generatedKeyResultSet;
 
                 generatedKeyResultSet = pstAddNewUser.getGeneratedKeys();
 
                 boolean hasKey = false;
                 if (generatedKeyResultSet.next()) {
                     user.userid = generatedKeyResultSet.getInt(1);
                     hasKey = true;
                 }
                 generatedKeyResultSet.close();
 
                 if (!hasKey) {
                     log.severe("Current database doesn't support java.sql.Statement.getGeneratedKeys()");
                     user = this.getUser(email);
                 }
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstAddNewUser);
             }
 
         }
 
 		
 		return user;
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>request.id</code></b>). All values within the given object will
 	 * be written into the database, so all old values within the row will be
 	 * overwritten. <b><code>request.id</code></b> has to be > 0 and must exists
 	 * within the database table. <br />SQL command:
 	 * {@value SqlStatementConstants#strUpdateRequest}
 	 * 
 	 * @param request
 	 *            The request object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int updateRequest(RequestDataset request) throws SQLException {
 
         int tryAgain = maxTries;
         int rowsAffected;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstUpdateRequest = null;
 
             try {
 
                 pstUpdateRequest = SqlStatementConstants.UpdateRequest.createPreparedStatement(dataSource);
 
                 pstUpdateRequest.setInt(1, request.userID);
                 pstUpdateRequest.setString(2, request.algorithm);
                 pstUpdateRequest.setBytes(3, request.jsonRequest);
                 pstUpdateRequest.setBytes(4, request.jsonResponse);
                 pstUpdateRequest.setInt(5, request.cost);
                 pstUpdateRequest.setTimestamp(6, dateToTimestamp(request.requestDate));
                 pstUpdateRequest.setTimestamp(7, dateToTimestamp(request.finishedDate));
                 pstUpdateRequest.setLong(8, request.duration);
                 pstUpdateRequest.setString(9, request.status.toString());
                 pstUpdateRequest.setInt(10, request.requestID);
 
                 rowsAffected = pstUpdateRequest.executeUpdate();
 
                 log.fine("Database query successful");
                 return rowsAffected;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstUpdateRequest);
             }
 
         }
         return 0;
 	}
 
 	/**
 	 * Updates the Requests table row with the id given through the parameter
 	 * (<b><code>requestID</code></b>). The given parameters will overwrite the
 	 * old values in the database row. FinishedDate will be set to the current
 	 * timestamp. Status will be set to ok. <b><code>requestID</code></b> has to be > 0 and must exists
 	 * within the database table. <br />SQL command:
 	 * {@value SqlStatementConstants#strUpdateRequestWithComputeResult}
 	 * 
 	 * @param requestID requestID
 	 * @param jsonResponse JSON response object
 	 * @param cost cost of request
 	 * @param cpuTime cpuTime
 	 * @throws SQLException
 	 *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int updateRequestWithComputeResult(int requestID,
 			byte[] jsonResponse, int cost, long cpuTime) throws SQLException {
 
         int rowsAffected;
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstUpdateRequestWithComputeResult = null;
 
             try {
 
                 pstUpdateRequestWithComputeResult
                         = SqlStatementConstants.UpdateRequestWithComputeResult.createPreparedStatement(dataSource);
 
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstUpdateRequestWithComputeResult.setBytes(1, jsonResponse);
                 pstUpdateRequestWithComputeResult.setInt(2, cost);
                 pstUpdateRequestWithComputeResult.setTimestamp(3, stamp);
                 pstUpdateRequestWithComputeResult.setLong(4, cpuTime);
                 pstUpdateRequestWithComputeResult.setString(5, RequestStatusEnum.ok.toString());
                 pstUpdateRequestWithComputeResult.setInt(6, requestID);
 
                 rowsAffected = pstUpdateRequestWithComputeResult.executeUpdate();
 
                 log.fine("Database query successful");
                 return rowsAffected;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstUpdateRequestWithComputeResult);
             }
 
         }
         return 0;
 	}
 
 
 
     /**
      * Updates the Requests table row with the id given through the parameter
      * <b><code>requestID</code></b>. The given parameter <b><code>jsonResponse</code></b>
      * will overwrite the old values in the database row. Cost and CPUTime will be set 0.
      * FinishedDate will be set to the current timestamp. Status will be set &quot;failed&quot;.
      * <b><code>requestID</code></b> has to be > 0 and must exists
      * within the database table. <br />SQL command:
      * {@value SqlStatementConstants#strUpdateRequestWithComputeResult}
      *
      * @param requestID requestID
      * @param jsonResponse JSON response object with error message
      * @throws SQLException
      *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
      */
     public int updateRequestAsFailed(int requestID, byte[] jsonResponse) throws SQLException {
 
         int rowsAffected;
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstUpdateRequestWithComputeResult = null;
 
             try {
 
                 pstUpdateRequestWithComputeResult
                         = SqlStatementConstants.UpdateRequestWithComputeResult.createPreparedStatement(dataSource);
 
                 Timestamp stamp = new Timestamp(System.currentTimeMillis());
 
                 pstUpdateRequestWithComputeResult.setBytes(1, jsonResponse);
                 pstUpdateRequestWithComputeResult.setInt(2, 0);
                 pstUpdateRequestWithComputeResult.setTimestamp(3, stamp);
                 pstUpdateRequestWithComputeResult.setLong(4, 0);
                 pstUpdateRequestWithComputeResult.setString(5, RequestStatusEnum.failed.toString());
                 pstUpdateRequestWithComputeResult.setInt(6, requestID);
 
                 rowsAffected = pstUpdateRequestWithComputeResult.executeUpdate();
 
                 log.fine("Database query successful");
                 return rowsAffected;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstUpdateRequestWithComputeResult);
             }
 
         }
         return 0;
     }
 
 
     /**
 	 * Updates the Users table row with the id given through the parameter (<b>
 	 * <code>request.id</code></b>). All values within the given object will be
 	 * written into the database, so all old values within the row will be
 	 * overwritten. <b><code>user.userid</code></b> has to be > 0 and must exists
 	 * within the database table. <br />SQL command: {@value SqlStatementConstants#strUpdateUser}
 	 * 
 	 * @param user
 	 *            The user object to write into the database.
 	 * @throws SQLException
 	 *             Thrown if update fails.
     * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int  updateUser(UserDataset user) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstUpdateUser = null;
 
             try {
 
                 pstUpdateUser = SqlStatementConstants.UpdateUser.createPreparedStatement(dataSource);
 
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
                 pstUpdateUser.setInt(11, user.userid);
 
                return pstUpdateUser.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstUpdateUser);
             }
 
         }
 
         return 0;
 	}
 
 
     /**
      * Changes the status of the user who has the given id to the status deleted. 
      * <b><code>userID</code></b> has to be >= 0 and must exists
      * within the database table. <br />SQL command: {@value SqlStatementConstants#strUpdateUserStatusToDeleted}
      *
      * @param userID
      *            The id of the user.
      * @throws SQLException
      *             Thrown if update fails.
      * @return number of database rows changed (1 if successful, else 0)
      */
     public int updateUserStatusToDeleted(int userID) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstUpdateUser = null;
 
             try {
 
                 pstUpdateUser
                         = SqlStatementConstants.UpdateUserStatusToDeleted.createPreparedStatement(dataSource);
 
                 pstUpdateUser.setInt(1, userID);
 
                 return pstUpdateUser.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstUpdateUser);
             }
 
         }
 
         return 0;
     }
     
     
 	/**
 	 * Deletes the Requests table row with the given request id. </br>SQL
 	 * command: {@value SqlStatementConstants#strDeleteRequestWithRequestId}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteRequest(int id) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstDeleteRequestWithRequestId = null;
 
             try {
 
                 pstDeleteRequestWithRequestId
                         = SqlStatementConstants.DeleteRequestWithRequestId.createPreparedStatement(dataSource);
 
                 pstDeleteRequestWithRequestId.setInt(1, id);
 
                 return pstDeleteRequestWithRequestId.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstDeleteRequestWithRequestId);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Requests table rows with the given user id. </br>SQL command:
 	 * {@value SqlStatementConstants#strDeleteRequestsOfUserWithUserId}
 	 * 
 	 * @param userId
 	 *            User id of the user, whose requests should be deleted.
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteRequestsOfUser(int userId) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstDeleteRequestsOfUserWithUserId = null;
 
             try {
 
                 pstDeleteRequestsOfUserWithUserId
                         = SqlStatementConstants.DeleteRequestsOfUserWithUserId.createPreparedStatement(dataSource);
 
                 pstDeleteRequestsOfUserWithUserId.setInt(1, userId);
 
                 return pstDeleteRequestsOfUserWithUserId.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstDeleteRequestsOfUserWithUserId);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Users table row with the given user id. Depending on the
 	 * database configuration(for example strict mode) maybe the corresponding
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command:
 	 * {@value SqlStatementConstants#strDeleteUserWithUserId}
 	 * 
 	 * @param userId
 	 *            User id
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteUser(int userId) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstDeleteUserWithUserId = null;
 
             try {
 
                 pstDeleteUserWithUserId
                         = SqlStatementConstants.DeleteUserWithUserId.createPreparedStatement(dataSource);
 
                 pstDeleteUserWithUserId.setInt(1, userId);
 
                 return pstDeleteUserWithUserId.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstDeleteUserWithUserId);
             }
 
         }
 
         return 0;
 	}
 
 	/**
 	 * Deletes the Users table row with the given user email. Depending on the
 	 * database configuration(for example strict mode) maybe the corresponding
 	 * requests could be deleted too because of the FOREIGN KEY UserId within
 	 * the Requests table. </br>SQL command: {@value SqlStatementConstants#strDeleteUserWithEmail}
 	 * 
 	 * @param email The email of the user
 	 * @throws SQLException
 	 *             Thrown if delete fails.
      * @return number of database rows changed (1 if successful, else 0)
 	 */
 	public int deleteUser(String email) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstDeleteUserWithEmail = null;
 
             try {
 
                 pstDeleteUserWithEmail
                         = SqlStatementConstants.DeleteUserWithEmail.createPreparedStatement(dataSource);
 
                 pstDeleteUserWithEmail.setString(1, email);
 
                 return pstDeleteUserWithEmail.executeUpdate();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstDeleteUserWithEmail);
             }
 
         }
 
         return 0;
 
 	}
 
 
     /**
      * Gets a list with all requests within the Requests table with regard to
      * the limit and offset constraints. If no requests are found with the given
      * constraints or the table is empty, an empty list will be returned.
      * The fields JSONRequest and JSONResponse will not get retrieved from database.
      * </br>SQL command: {@value SqlStatementConstants#strGetAllRequestsNoJsonWithLimitOffset}
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
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetAllRequests = null;
 
             try {
 
                 pstGetAllRequests
                         = SqlStatementConstants.GetAllRequestsNoJsonWithLimitOffset.createPreparedStatement(dataSource);
 
                 pstGetAllRequests.setInt(1, limit);
                 pstGetAllRequests.setInt(2, offset);
 
                 ResultSet resultSet = pstGetAllRequests.executeQuery();
                 ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new RequestDataset(
                             resultSet.getInt(1),
                             resultSet.getInt(2),
                             resultSet.getString(3),
                             null,
                             null,
                             resultSet.getInt(4),
                             timestampToDate(resultSet.getTimestamp(5)),
                             timestampToDate(resultSet.getTimestamp(6)),
                             resultSet.getLong(7),
                             RequestStatusEnum.valueOf(resultSet.getString(8))));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetAllRequests);
             }
 
         }
 
         return null;
     }
 
 	/**
 	 * Gets a request object of the Requests table with the given request id.
 	 * 
 	 * </br>SQL command: {@value SqlStatementConstants#strGetRequestWithRequestId}
 	 * 
 	 * @param id
 	 *            Request id
 	 * @return A request object if the request with the id exists, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public RequestDataset getRequest(int id) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetRequestWithRequestId = null;
 
             try {
 
                 pstGetRequestWithRequestId
                         = SqlStatementConstants.GetRequestWithRequestId.createPreparedStatement(dataSource);
 
                 pstGetRequestWithRequestId.setInt(1, id);
                 ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
                 RequestDataset request = null;
 
                 if (resultSet.next()) {
                     request = new RequestDataset(
                             resultSet.getInt(1),
                             resultSet.getInt(2),
                             resultSet.getString(3),
                             null,
                             null,
                             resultSet.getInt(4),
                             timestampToDate(resultSet.getTimestamp(5)),
                             timestampToDate(resultSet.getTimestamp(6)),
                             resultSet.getLong(7),
                             RequestStatusEnum.valueOf(resultSet.getString(8)));
                 }
                 resultSet.close();
 
                 return request;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetRequestWithRequestId);
             }
 
         }
 
         return null;
 	}
 
 
 
     /**
      * Gets a json request as byte array of the Requests table with the given request id.
      * The byte array and the corresponding user id will be stored together in the returned JSONObject.
      * If no entry is found for the given request id, null will be returned.
      *
      * </br>SQL command: {@value SqlStatementConstants#strGetJSONRequestWithRequestId}
      *
      * @param id
      *            Request id
      * @return A JSONObject with a byte array and user id if the request with the request id exists.
      *         The byte array can be null if the database field is NULL.
      *         The returned JSONObject itself is null if no entry is found for the given request id.
      * @throws SQLException
      *             Thrown if select fails.
      */
     public JSONObject getJsonRequest(int id) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetRequestWithRequestId = null;
 
             try {
 
                 pstGetRequestWithRequestId
                         = SqlStatementConstants.GetJSONRequestWithRequestId.createPreparedStatement(dataSource);
 
                 pstGetRequestWithRequestId.setInt(1, id);
                 ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
 
                 JSONObject jsonRequest = null;
 
                 if (resultSet.next()) {
                     jsonRequest = new JSONObject(
                             resultSet.getInt(1),
                             resultSet.getBytes(2));
                 }
                 resultSet.close();
 
                 return jsonRequest;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetRequestWithRequestId);
             }
 
         }
 
         return null;
     }
 
 
     /**
      * Gets a json response as byte array of the Requests table with the given request id.
      * The byte array and the corresponding user id will be stored together in the returned JSONObject.
      * If no entry is found for the given request id, null will be returned.
      *
      * </br>SQL command: {@value SqlStatementConstants#strGetJSONResponseWithRequestId}
      *
      * @param id
      *            Request id
      * @return A JSONObject with a byte array and user id if the request with the request id exists.
      *         The byte array can be null if the database field is NULL.
      *         The returned JSONObject itself is null if no entry is found for the given request id.
      * @throws SQLException
      *             Thrown if select fails.
      */
     public JSONObject getJsonResponse(int id) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetRequestWithRequestId = null;
 
             try {
 
                 pstGetRequestWithRequestId
                         = SqlStatementConstants.GetJSONResponseWithRequestId.createPreparedStatement(dataSource);
 
                 pstGetRequestWithRequestId.setInt(1, id);
                 ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
 
                 JSONObject jsonResponse = null;
 
                 if (resultSet.next()) {
                     jsonResponse =  new JSONObject(
                             resultSet.getInt(1),
                             resultSet.getBytes(2));
                 }
                 resultSet.close();
 
                 return jsonResponse;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetRequestWithRequestId);
             }
 
         }
 
         return null;
     }
 
 
     /**
      * Gets a list with all requests within the Requests table which have the
      * given user id with regard to the limit and offset constraints. If no
      * requests are found with the given user id and given constraints or the
      * table is empty, an empty list will be returned.
      * The fields JSONRequest and JSONResponse will not get retrieved from database.
      * <br />SQL command:
      * {@value SqlStatementConstants#strGetRequestsNoJsonWithUserIdLimitOffset}
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
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetRequests = null;
 
             try {
 
                 pstGetRequests
                         = SqlStatementConstants.GetRequestsNoJsonWithUserIdLimitOffset.createPreparedStatement(dataSource);
 
                 pstGetRequests.setInt(1, userId);
                 pstGetRequests.setInt(2, limit);
                 pstGetRequests.setInt(3, offset);
 
                 ResultSet resultSet = pstGetRequests
                         .executeQuery();
                 ArrayList<RequestDataset> list = new ArrayList<RequestDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new RequestDataset(
                             resultSet.getInt(1),
                             resultSet.getInt(2),
                             resultSet.getString(3),
                             null,
                             null,
                             resultSet.getInt(4),
                             timestampToDate(resultSet.getTimestamp(5)),
                             timestampToDate(resultSet.getTimestamp(6)),
                             resultSet.getLong(7),
                             RequestStatusEnum.valueOf(resultSet.getString(8))));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetRequests);
             }
 
         }
         return null;
     }
 
 
 	/**
 	 * Gets a list with all users within the Users table. If the table is empty,
 	 * an empty list will be returned. <br />SQL command:
 	 * {@value SqlStatementConstants#strGetAllUsers}
 	 * 
 	 * @return A list with all selected users. If no users selected, the list is
 	 *         empty, but not null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public List<UserDataset> getAllUsers() throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetAllUsers = null;
 
             try {
 
                 pstGetAllUsers = SqlStatementConstants.GetAllUsers.createPreparedStatement(dataSource);
 
                 ResultSet resultSet = pstGetAllUsers.executeQuery();
                 ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new UserDataset(
                             resultSet.getInt(1),
                             resultSet.getString(2),
                             resultSet.getString(3),
                             resultSet.getString(4),
                             resultSet.getBoolean(5),
                             UserStatusEnum.valueOf(resultSet.getString(6)),
                             resultSet.getString(7),
                             resultSet.getString(8),
                             resultSet.getString(9),
                             timestampToDate(resultSet.getTimestamp(10)),
                             timestampToDate(resultSet.getTimestamp(11))
                     ));
                 }
                 resultSet.close();
 
                 return list;
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetAllUsers);
             }
 
         }
         return null;
 	}
 
 	/**
 	 * Gets a list with all users within the Users table with regard to the
 	 * limit and offset constraints. If no users are found with the given
 	 * constraints or the table is empty, an empty list will be returned.
 	 * <br />SQL command: {@value SqlStatementConstants#strGetAllUsersWithLimitOffset}
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
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetAllUsersWithLimitOffset = null;
 
             try {
 
                 pstGetAllUsersWithLimitOffset
                         = SqlStatementConstants.GetAllUsersWithLimitOffset.createPreparedStatement(dataSource);
 
                 pstGetAllUsersWithLimitOffset.setInt(1, limit);
                 pstGetAllUsersWithLimitOffset.setInt(2, offset);
 
                 ResultSet resultSet = pstGetAllUsersWithLimitOffset.executeQuery();
                 ArrayList<UserDataset> list = new ArrayList<UserDataset>();
 
                 while (resultSet.next()) {
 
                     list.add(new UserDataset(
                             resultSet.getInt(1),
                             resultSet.getString(2),
                             resultSet.getString(3),
                             resultSet.getString(4),
                             resultSet.getBoolean(5),
                             UserStatusEnum.valueOf(resultSet.getString(6)),
                             resultSet.getString(7),
                             resultSet.getString(8),
                             resultSet.getString(9),
                             timestampToDate(resultSet.getTimestamp(10)),
                             timestampToDate(resultSet.getTimestamp(11))
                     ));
                 }
                 resultSet.close();
 
                 return list;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetAllUsersWithLimitOffset);
             }
 
         }
 
         return null;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given email. <br />SQL
 	 * command: {@value SqlStatementConstants#strGetUserWithEmail}
 	 * 
 	 * @param email The email of the user
 	 * @return The user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(String email) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetUserWithEmail = null;
 
             try {
 
                 pstGetUserWithEmail = SqlStatementConstants.GetUserWithEmail.createPreparedStatement(dataSource);
 
                 UserDataset user = null;
 
                 pstGetUserWithEmail.setString(1, email);
                 ResultSet resultSet = pstGetUserWithEmail.executeQuery();
 
                 while (resultSet.next()) {
 
                     user = new UserDataset(
                             resultSet.getInt(1),
                             resultSet.getString(2),
                             resultSet.getString(3),
                             resultSet.getString(4),
                             resultSet.getBoolean(5),
                             UserStatusEnum.valueOf(resultSet.getString(6)),
                             resultSet.getString(7),
                             resultSet.getString(8),
                             resultSet.getString(9),
                             timestampToDate(resultSet.getTimestamp(10)),
                             timestampToDate(resultSet.getTimestamp(11))
                     );
                 }
                 resultSet.close();
 
                 return user;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetUserWithEmail);
             }
 
         }
 
         return null;
 	}
 
 	/**
 	 * Gets a user object from the Users table with the given user id. <br />SQL
 	 * command: {@value SqlStatementConstants#strGetUserWithId}
 	 * 
 	 * @param id
 	 *            User id
 	 * @return The user object, if the user is found, else null.
 	 * @throws SQLException
 	 *             Thrown if select fails.
 	 */
 	public UserDataset getUser(int id) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstGetUserWithId = null;
 
             try {
 
                 pstGetUserWithId = SqlStatementConstants.GetUserWithId.createPreparedStatement(dataSource);
 
                 UserDataset user = null;
 
                 pstGetUserWithId.setInt(1, id);
                 ResultSet resultSet = pstGetUserWithId.executeQuery();
 
                 while (resultSet.next()) {
 
                     user = new UserDataset(
                             resultSet.getInt(1),
                             resultSet.getString(2),
                             resultSet.getString(3),
                             resultSet.getString(4),
                             resultSet.getBoolean(5),
                             UserStatusEnum.valueOf(resultSet.getString(6)),
                             resultSet.getString(7),
                             resultSet.getString(8),
                             resultSet.getString(9),
                             timestampToDate(resultSet.getTimestamp(10)),
                             timestampToDate(resultSet.getTimestamp(11))
                     );
                 }
                 resultSet.close();
 
                 return user;
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstGetUserWithId);
             }
 
         }
 
         return null;
 	}
 
 
     /**
      * Returns the number of data sets within the Requests table
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfRequests() throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstCountAllRequests = null;
 
             try {
 
                 pstCountAllRequests
                         = SqlStatementConstants.CountAllRequests.createPreparedStatement(dataSource);
 
                 ResultSet resultSet = pstCountAllRequests.executeQuery();
 
                 if (resultSet.next()) {
                     int count = resultSet.getInt(1);
                     resultSet.close();
                     return count;
                 }
 
                 log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
                 resultSet.close();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstCountAllRequests);
             }
 
         }
         return 0;
     }
 
     /**
      * Returns the number of data sets with a certain UserID within the Requests table    
      * @param userId The UserID from the user for which you want to get the number of data sets
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfRequestsWithUserId(int userId) throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstCountRequestsWithUserId = null;
 
             try {
 
                 pstCountRequestsWithUserId
                         = SqlStatementConstants.CountRequestsWithUserId.createPreparedStatement(dataSource);
 
                 pstCountRequestsWithUserId.setInt(1, userId);
                 ResultSet resultSet = pstCountRequestsWithUserId.executeQuery();
 
                 if (resultSet.next()) {
                     int count = resultSet.getInt(1);
                     resultSet.close();
                     return count;
                 }
 
                 log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
                 resultSet.close();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstCountRequestsWithUserId);
             }
 
         }
 
         return 0;
     }
 
     /**
      * Returns the number of data sets within the Users table
      * @return Number of data sets
      * @throws SQLException Thrown if sql query fails
      */
     public int getNumberOfUsers() throws SQLException {
 
         int tryAgain = maxTries;
 
         while (tryAgain > 0) {
             tryAgain--;
 
             PreparedStatement pstCountAllUsers = null;
 
             try {
 
                 pstCountAllUsers
                         = SqlStatementConstants.CountAllUsers.createPreparedStatement(dataSource);
 
                 ResultSet resultSet = pstCountAllUsers.executeQuery();
 
                 if (resultSet.next()) {
                     int count = resultSet.getInt(1);
                     resultSet.close();
                     return count;
                 }
 
                 log.severe("Failed SELECT COUNT(*) with empty result set, but no SQL Exception thrown.");
                 resultSet.close();
 
             } catch (SQLException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } catch (NullPointerException e) {
                 tryAgain = processTryAgainExceptionHandling(tryAgain, e);
             } finally {
                 SqlStatement.close(pstCountAllUsers);
             }
 
         }
 
         return 0;
     }
 
 
     /**
      *
      * @param tryAgain the old tryAgain value, specifies how many tries are left before this exception
      * @param exception Needs to be an Exception, not SQLException, because null pointer can be thrown
      * @return the new tryAgain value, specifies how many tries are left after this exception
      * @throws SQLException Thrown if could not be reestablished with allowed number of tries
      */
     private int processTryAgainExceptionHandling(int tryAgain, Exception exception) throws SQLException {
         if (tryAgain > 0) {
             log.log(Level.WARNING, "Database exception occurred after " + (maxTries - tryAgain) + ". attempt, " +
                     "thread will now reconnect database and send again the sql statement ", exception);
         } else {
             log.log(Level.SEVERE, "Database exception occurred after " + (maxTries - tryAgain) + ". attempt, " +
                     "thread will now give up executing the statement", exception);
             if (exception instanceof SQLException) {
                 throw (SQLException) exception;
             } else {
                 SQLException sqlEx = new SQLException("Exception within database method: "
                         + exception.getClass().getName() + ": " + exception.getMessage());
                 sqlEx.setStackTrace(exception.getStackTrace());
                 throw sqlEx;
             }
 
         }
         return tryAgain;
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
 
 }
