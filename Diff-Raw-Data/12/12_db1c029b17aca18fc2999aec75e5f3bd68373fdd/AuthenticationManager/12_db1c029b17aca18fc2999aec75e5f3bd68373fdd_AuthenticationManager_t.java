 package edu.berkeley.cs.cs162.Server;
 
 /**
  * Manager class for authenticating Clients.
  * 
  * This should be able to handle registering clients, authenticating clients, 
  * and changing client's passwords.
  * 
  * NOTE: All of these methods should be synchronized the Database connection!
  * 		 do not lock the methods in the manager.
  */
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.sun.corba.se.spi.activation.ServerAlreadyActive;
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 
 public class AuthenticationManager {
 	@SuppressWarnings("unused")
 	private DatabaseConnection connection;
 	private String salt;
 	
 	public static class ServerAuthenticationException extends Exception{
 		private static final long serialVersionUID = -1052874230279909816L;
 	}
 
 	//load this at the start and increment this to get client ids.
 	protected int clientIds;
 	
 	/**
 	 * Constructs an authentication manager with a connection to the database and a salt.
 	 * @param connection Connection to the database.
 	 * @param salt Salt string for password hashing.
 	 */
 	public AuthenticationManager(DatabaseConnection connection, String salt) {
 		this.connection = connection;
 		this.salt = salt;
 	}
 	
 	/**
 	 * Registers a client with the specified info and password hash. 
 	 * 
 	 * The hash should be salted before stored in the database.
 	 * 
 	 * The return value should be true if the client was successfully registered,
 	 * or false if another client with the same name is already registered.
 	 * 
 	 * @param cInfo
 	 * @param passwordHash
 	 * @return success
 	 */
 	public boolean registerClient(ClientInfo cInfo, String passwordHash) {
         String clientName = cInfo.getName();
         byte clientType = cInfo.getPlayerType();
         String finalPass = Security.computeHashWithSalt(passwordHash, salt);
 
        ResultSet results = connection.executeReadQuery("SELECT clientId FROM clients WHERE name=\"" + clientName + "\"");

        try { if(results.next()) return false; }
        catch (SQLException e) { /* Do nothing */ }
        catch (NullPointerException e) { /* Do nothing */ }
 
         connection.startTransaction();
         try {
             connection.executeWriteQuery("INSERT INTO clients (name, type, passwordHash) VALUES (" + "\'" + clientName + "\', " + Byte.toString(clientType) + ", \'" + finalPass + "\'" + ")");
             connection.finishTransaction();
             return true;
         }
         catch(SQLException e){
             connection.abortTransaction();
             return false;
         }
 	}
 	
 	/**
 	 * Authenticates a client.
 	 * 
 	 * Note that you should salt the password hash before comparing to the one stored in the database.
 	 * 
 	 * Returns true if password and player type matches the one logged in the database.
 	 * 
 	 * @param cInfo
 	 * @param passwordHash
 	 * @return client id of the new client.
      * @throws ServerAuthenticationException in the case of authentication failure
 	 */
 	public int authenticateClient(ClientInfo cInfo, String passwordHash) throws ServerAuthenticationException {
         String clientName = cInfo.getName();
         String finalPass = Security.computeHashWithSalt(passwordHash, salt);
 
         try {
            ResultSet results = connection.executeReadQuery("SELECT passwordHash, clientId FROM clients WHERE name=\'" + clientName + "\'");
 
             if(results == null)
                 throw new ServerAuthenticationException();
             else if (!results.next())
                 throw new ServerAuthenticationException();
             else if (results.getString("passwordHash").equals(finalPass))
                 return results.getInt("clientId");
             else
                 throw new ServerAuthenticationException();
         }
         catch (SQLException e) {
             e.printStackTrace();
             throw new ServerAuthenticationException();
         }
 	}
 	
 	/**
 	 * Changes a client's password 
 	 * 
 	 * This method assumes that the authentication and client info already has been checked.
 	 * 
 	 * This will change the password of a client to a salted version of newPasswordHash.
 	 * 
 	 * @param cInfo
 	 * @param newPasswordHash
 	 */
 	public void changePassword(ClientInfo cInfo, String newPasswordHash) {
         String clientName = cInfo.getName();
 
         try {
             String cidQuery = "SELECT clientId FROM clients WHERE name=" + clientName;
             ResultSet result = connection.executeReadQuery(cidQuery);
             int clientID = result.getInt(1);
             String query = "UPDATE clients SET passwordHash=" + Security.computeHashWithSalt(newPasswordHash, salt) + " WHERE clientId=" + Integer.toString(clientID);
 
             connection.startTransaction();
             connection.executeWriteQuery(query);
             connection.finishTransaction();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 	}
 }
