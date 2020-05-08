 package ch.unibe.ese.shopnote.server.database;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 import ch.unibe.ese.shopnote.server.core.User;
 import ch.unibe.ese.shopnote.share.requests.RegisterRequest;
 import ch.unibe.ese.shopnote.share.requests.CreateSharedListRequest;
 import ch.unibe.ese.shopnote.share.requests.Request;
 import ch.unibe.ese.shopnote.share.requests.ShareListRequest;
 import ch.unibe.ese.shopnote.share.requests.UnShareListRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.EmptyListChangeRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.ListChangeRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.SetUnsharedRequest;
 
 /**
  * This Class organizes the database on the server.
  * It directly processes the requests on the database concerning Users and the IDs of the shared lists
  * <p>
  * Every Request that has to do with list or item updates goes to the {@link NeodatisDatabaseManager} (object database)
  */
 
 public class SQLiteDatabaseManager {
 	// Name definitions
 	
 	// User Table
 	// All users of the app are registered here
 	public static final String TABLE_USERS = "users";
 	public static final String COLUMN_USER_ID = "userId";
 	public static final String COLUMN_USER_PHONENUMBER = "phoneNumber";
 	
 	// Links local (not unique) list ids to unique server list ids using (userId, locallistid) as primary key
 	public static final String TABLE_LOCALTOSERVER_LIST_ID = "localtoserverlistid";
 	public static final String COLUMN_LOCAL_LIST_ID = "locallistid";
 	public static final String COLUMN_SERVER_LIST_ID = "serverlistId";
 	public static final String COLUMN_IS_RECIPE = "isrecipe";
 	// Links shared lists to users (unique server list id)
 	
 	public static final String TABLE_SHAREDLISTS = "sharedlists";
 	public static final String COLUMN_LIST_NAME = "listname";
 	
 	// Create statements
 	public static final String CREATE_TABLE_USERS = "create table if not exists " + TABLE_USERS + "(" + 
 			COLUMN_USER_ID + " integer primary key autoincrement, " +
 			COLUMN_USER_PHONENUMBER + " varchar(20)" +
 			");";
 	public static final String CREATE_TABLE_LOCALTOSERVER_LIST_ID = "create table if not exists " + TABLE_LOCALTOSERVER_LIST_ID + "(" +
 			COLUMN_USER_ID + " integer, " +
 			COLUMN_LOCAL_LIST_ID + " integer, " +
 			COLUMN_SERVER_LIST_ID + " integer default 0, " +
 			COLUMN_IS_RECIPE + " boolean default 0, " +
 			"primary key(" + COLUMN_USER_ID + ", " + COLUMN_LOCAL_LIST_ID + ", " + COLUMN_IS_RECIPE + ")" +
 			");";
 	public static final String CREATE_TABLE_SHAREDLISTS = "create table if not exists " + TABLE_SHAREDLISTS + "(" +
 			COLUMN_USER_ID + " integer, " +
 			COLUMN_SERVER_LIST_ID + " integer, " +
 			COLUMN_LIST_NAME + " varchar(30), " +
 			"primary key(" + COLUMN_USER_ID + ", " + COLUMN_SERVER_LIST_ID + ")" +
 			");";
 	// First dummy entry for localtoserver list id
 	public static final String INSERT_DUMMY = "insert or replace into " + TABLE_LOCALTOSERVER_LIST_ID + " values ( -1, -2, 0, 0);";
 	
 	// instance variables
 	private Connection c;
 	private NeodatisDatabaseManager odbManager;
 	
 	public SQLiteDatabaseManager() {
 		this.onCreate();
 	}
 	
 	public void setOdbManager(NeodatisDatabaseManager odbManager) {
 		this.odbManager = odbManager;
 	}
 
 	/**
 	 * Opens database file and 
 	 * creates all tables if they don't exist yet
 	 */
 	private void onCreate() {
 		Statement stmt = null;
 		try {
 			Class.forName("org.sqlite.JDBC");
 			this.c = DriverManager.getConnection("jdbc:sqlite:shoppinglist.db");
 			
 			stmt = this.c.createStatement();
 			stmt.executeUpdate(CREATE_TABLE_USERS);
 			stmt.executeUpdate(CREATE_TABLE_LOCALTOSERVER_LIST_ID);
 			stmt.executeUpdate(INSERT_DUMMY);
 			stmt.executeUpdate(CREATE_TABLE_SHAREDLISTS);
 			stmt.close();
 
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 			System.exit(0);
 		}
 		System.out.println("Opened SQLite database successfully");
 	}
 
 	/**
 	 * Add a user to the database if he isn't already in
 	 * @param phoneNumber
 	 */
 	public void addUser(Request request) {
 		String phoneNumber = request.getPhoneNumber();
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			String selectUserifExists = "select * from " + TABLE_USERS + " where " + COLUMN_USER_PHONENUMBER + "=\"" + phoneNumber + "\";";
 			ResultSet rs = stmt.executeQuery(selectUserifExists);
 			if(!rs.next()) {
 				String insertUser = "insert into " + TABLE_USERS + " (" + COLUMN_USER_PHONENUMBER + ") values (\"" + phoneNumber + "\");";			
 				stmt.executeUpdate(insertUser);
 				rs = stmt.executeQuery(selectUserifExists);
 				System.out.println("\t:Added user: " + rs.getString(COLUMN_USER_PHONENUMBER));
 				request.setSuccessful();
 				odbManager.addContainer(findUser(request));
 			} else {
				System.out.println("\t:User " + rs.getString(COLUMN_USER_PHONENUMBER) + " already existed");
 			}
 			request.setHandled();
 			
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	/**
 	 * returns UserId if it exists in database
 	 * @param request
 	 * @return
 	 */
 	public int findUser(Request request) {
 		String phoneNumber = request.getPhoneNumber();
 		if (phoneNumber.length()<=0)
 			return -1;
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			String selectUserifExists = "select * from " + TABLE_USERS + " where " + COLUMN_USER_PHONENUMBER + "=\"" + phoneNumber + "\";";
 			ResultSet rs = stmt.executeQuery(selectUserifExists);
 			if(rs.next()) {
 				request.setHandled();
 				return rs.getInt(COLUMN_USER_ID);
 			} else {
				System.out.println("\t:User " + phoneNumber + " does not exist in Database");
 				request.setHandled();
 			}
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		return -1;
 	}
 	
 	/**
 	 * Returns the phoneNumber of a user
 	 * @param userId
 	 * @return
 	 */
 	public String getNumberOfUser(int userId) {
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			String selectUserifExists = "select * from " + TABLE_USERS + " where " + COLUMN_USER_ID + "=\"" + userId + "\";";
 			ResultSet rs = stmt.executeQuery(selectUserifExists);
 			if(rs.next()) {
 				return rs.getString(COLUMN_USER_PHONENUMBER);
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		return ""+-1;
 		
 	}
 	
 
 	/**
 	 * Creates an entry in sharedlists
 	 * @param request
 	 */
 	public void shareList(ShareListRequest request) {
 		int userId = findUser(request);
 		int friendId = findUser(new RegisterRequest(request.getFriendNumber()));
 		// Cannot share a list with yourself
 		if(friendId == userId)
 			return;
 		String listname = request.getListName();
 		// Cannot share a list with inexistent user
 		if(userId == -1 || friendId == -1)
 			return;
 		long serverListId = createServerListIdIfNotExists(userId, request.getListId(), request.isRecipe());
 		// Cannot share a list without global list ID
 		if(serverListId == -1)
 			System.err.println("Failed to find/create global list id");
 		
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			String selectEntryifExists = "select * from " + TABLE_SHAREDLISTS + " where " + COLUMN_USER_ID + "=\"" + userId +
 					"\" and " + COLUMN_SERVER_LIST_ID + "=\"" + serverListId + "\";";
 			String selectEntryifExists2 = "select * from " + TABLE_SHAREDLISTS + " where " + COLUMN_USER_ID + "=\"" + friendId +
 					"\" and " + COLUMN_SERVER_LIST_ID + "=\"" + serverListId + "\";";
 			
 			// Check if author of request is in shared list
 			ResultSet rs = stmt.executeQuery(selectEntryifExists);
 			if(rs.next()) {
 				System.out.println("\t:List " + serverListId + " is not a new shared list");
 			} else {
 				String insertSharedList = "insert into " + TABLE_SHAREDLISTS + " values (" +
 						"\"" + userId + "\", " +
 						"\"" + serverListId + "\"," +
 						"\"" + listname + "\"" +
 						");";	
 				stmt.executeUpdate(insertSharedList);
 				System.out.println("\t:List " + serverListId + " was created by " + userId);
 			}
 			
 			// Check if requested friend is in shared list
 			ResultSet rs2 = stmt.executeQuery(selectEntryifExists2);
 			if(rs2.next()) {
 				System.out.println("\t:List " + serverListId + " is already shared with " + friendId);
 				request.setHandled();
 			} else {
 				String insertSharedList = "insert into " + TABLE_SHAREDLISTS + " values (" +
 						"\"" + friendId + "\", " +
 						"\"" + serverListId + "\"," +
 						"\"" + listname + "\"" +
 						");";	
 				stmt.executeUpdate(insertSharedList);
 				
 				// craft the CreateSharedListRequest for your friend
 				CreateSharedListRequest cslRequest = new CreateSharedListRequest(request.getFriendNumber(), serverListId, listname);
 				if(request.isRecipe()) {
 					System.out.println("\t:Recipe " + serverListId + " is now shared with user " + friendId);
 					cslRequest.isRecipe(true);
 				} else {
 					System.out.println("\t:List " + serverListId + " is now shared with user " + friendId);
 				}
 				odbManager.storeRequest(cslRequest);
 				request.setSuccessful();
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		
 	}
 	
 	/**
 	 * Removes an entry in sharedlists
 	 * @param request
 	 */
 	public void unShareList(UnShareListRequest request) {
 		int userId = findUser(request);
 		int friendId = findUser(new RegisterRequest(request.getFriendNumber()));
 		long serverListId = getServerListId(userId, request.getListId(), request.isRecipe());
 		// Cannot unshare a list which is shared between inexistent users
 		if(userId <= -1 || friendId <= -1) {
 			System.err.println("Cannot unshare List with non existent users");
 			return;
 		}
 		// Cannot unshare a list which is not existent
 		if(serverListId <= -1) {
 			System.err.println("Failed to find global list id");
 			return;
 		}
 		
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			
 			// Check shared table
 			// remove the entry if it is in table
 			String selectEntryifExists = "select * from " + TABLE_SHAREDLISTS + " where " +
 					COLUMN_USER_ID + "=\"" + friendId + "\" and " + COLUMN_SERVER_LIST_ID + "=\"" + serverListId + "\";";
 			ResultSet rs = stmt.executeQuery(selectEntryifExists);
 			if(rs.next()) {
 				String deleteFriendfromList = "delete from " + TABLE_SHAREDLISTS + " where " +
 						COLUMN_USER_ID + "=" + friendId + " and " +
 						COLUMN_SERVER_LIST_ID + "=" + serverListId + ";";
 				stmt.executeUpdate(deleteFriendfromList);
 			}
 			
 			// Check Local to server list id Table
 			// remove the entry if it is in table
 			String selectEntryifExists2 = "select * from " + TABLE_LOCALTOSERVER_LIST_ID + " where " +
 					COLUMN_USER_ID + "=\"" + friendId + "\" and " + COLUMN_SERVER_LIST_ID + "=\"" + serverListId + "\";";
 			ResultSet rs2 = stmt.executeQuery(selectEntryifExists2);
 			if(rs2.next()) {
 				long friendLocalListId = rs2.getLong(COLUMN_LOCAL_LIST_ID);
 				String deleteLocalToListId = "delete from " + TABLE_LOCALTOSERVER_LIST_ID + " where " +
 						COLUMN_USER_ID + "=" + friendId + " and " +
 						COLUMN_SERVER_LIST_ID + "=" + serverListId + ";";
 				stmt.executeUpdate(deleteLocalToListId);
 				
 				// Tell the friend that he has been removed
 				SetUnsharedRequest suRequest = new SetUnsharedRequest(request.getFriendNumber(), friendLocalListId);
 				if(request.isRecipe()) {
 					System.out.println("\t:Recipe " + serverListId + " is no longer shared with " + friendId);
 					suRequest.isRecipe(true);
 				} else {
 					System.out.println("\t:List " + serverListId + " is no longer shared with " + friendId);
 				}
 				odbManager.storeRequest(suRequest, friendId);
 				request.setSuccessful();
 				
 			} else {
 				System.out.println("\t:List " + serverListId + " is not shared with user " + friendId);
 				request.setHandled();
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		
 	}
 
 	/**
 	 * Converts an userID and local ListId to the global list id (which is used by the server)
 	 * @param userId
 	 * @param listId
 	 * @return serverListId
 	 */
 	private long createServerListIdIfNotExists(int userId, long listId, boolean isRecipe) {
 		if(userId <= -1)
 			throw new IllegalStateException("userId <=-1 not allowed");
 		long serverListId = getServerListId(userId, listId, isRecipe);
 		if (serverListId > -1) {
 			return serverListId;
 		} else {
 			try {
 				Statement stmt = this.c.createStatement();
 				String createEntry = "insert into "
 						+ TABLE_LOCALTOSERVER_LIST_ID + " values (\"" + userId
 						+ "\",\"" + listId + "\", (select max("
 						+ COLUMN_SERVER_LIST_ID + ")+1 from "
 						+ TABLE_LOCALTOSERVER_LIST_ID + "), " + (isRecipe? 1:0) + ");";
 				stmt.executeUpdate(createEntry);
 				
 			} catch (SQLException e) {
 				e.printStackTrace(System.err);
 			}
 		}
 		serverListId = getServerListId(userId, listId, isRecipe);
 		if(serverListId <=-1) {
 			throw new IllegalStateException();
 		}
 		return getServerListId(userId, listId, isRecipe);
 	}
 	
 	/**
 	 * Assign a Local list Id to a global server list id (as a response to a CreateShareListRequest)
 	 * @param request
 	 */
 	public void assignLocalToServerListId(CreateSharedListRequest request) {
 		int userId = findUser(request);
 		assignLocalToServerListId(userId, request.getLocalListId(), request.getServerListid(), request.isRecipe());
 	}
 	
 	/**
 	 * Assign a Local list Id to a global server list id (as a response to a CreateShareListRequest)
 	 * @param request
 	 */
 	public void assignLocalToServerListId(int userId, long localListId, long serverListId, boolean isRecipe) {
 		// Check if there's already an entry for this list (it shouldn't)
 		long maybeServerListId = getServerListId(userId, localListId, isRecipe);
 		if(maybeServerListId >-1) {
 			return;
 		}
 		try {
 			Statement stmt = this.c.createStatement();
 			String createEntry = "insert into "
 					+ TABLE_LOCALTOSERVER_LIST_ID + " values (\"" + userId
 					+ "\",\"" + localListId + "\",\"" + serverListId + "\",\"" + (isRecipe?1:0) + "\");";
 			stmt.executeUpdate(createEntry);
 			
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	/**
 	 * Gets the serverlistId that is identified by the userId and his local listId
 	 * @param userId
 	 * @param listId
 	 * @return serverListId
 	 */
 	private long getServerListId(int userId, long listId, boolean isRecipe) {
 		long serverId = -1;
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			String selectEntryifExists = "select * from " + TABLE_LOCALTOSERVER_LIST_ID + " where " + COLUMN_USER_ID + "=\"" + userId + "\" and " +
 					COLUMN_LOCAL_LIST_ID + "=\"" + listId + "\" and " + COLUMN_IS_RECIPE + "=\"" + (isRecipe?1:0) + "\";";
 			ResultSet rs = stmt.executeQuery(selectEntryifExists);
 			if(rs.next()) {
 				serverId = rs.getInt(COLUMN_SERVER_LIST_ID);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		return serverId;
 	}
 	
 	/**
 	 * Translates the server list id to a local list id on the users phone
 	 * @param userId
 	 * @param serverId
 	 * @return localListId
 	 */
 	private long getLocalListId(int userId, long serverId) {
 		long localId = -1;
 		Statement stmt;
 		try {
 			stmt = this.c.createStatement();
 			String selectEntryifExists = "select * from " + TABLE_LOCALTOSERVER_LIST_ID + " where " + COLUMN_USER_ID + "=\"" + userId + "\" and " +
 					COLUMN_SERVER_LIST_ID + "=\"" + serverId + "\";";
 			ResultSet rs = stmt.executeQuery(selectEntryifExists);
 			if(rs.next()) {
 				localId = rs.getInt(COLUMN_LOCAL_LIST_ID);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		return localId;
 	}
 
 	/**
 	 * Returns all userIds and their localListId
 	 * which belong to this shared list
 	 * @param userId
 	 * @param localListId
 	 * @return array if userIds
 	 */
 	public ArrayList<User> getSharedUsers(ListChangeRequest request) {
 		int userId = findUser(request);
 		long localListId = request.getLocalListId();
 		long serverListId = getServerListId(userId, localListId, request.isRecipe());
 		Statement stmt;
 		ArrayList<User> userList = new ArrayList<User>();
 		try {
 			stmt = this.c.createStatement();
 //			String selectUsers = "select " + COLUMN_USER_ID + "," + COLUMN_LOCAL_LIST_ID + " from " + TABLE_LOCALTOSERVER_LIST_ID +
 //					" where " + COLUMN_SERVER_LIST_ID + "=\"" + serverListId + "\";";
 			String selectUsers = "select " + COLUMN_USER_ID + "," + COLUMN_SERVER_LIST_ID + " from " + TABLE_SHAREDLISTS + " where " +
 					COLUMN_SERVER_LIST_ID + "=\"" + serverListId + "\";";
 			ResultSet rs = stmt.executeQuery(selectUsers);
 			while(rs.next()) {
 				User user = new User(rs.getInt(COLUMN_USER_ID));
 				long newLocalListId = getLocalListId(user.getUserId(), serverListId);
 				if(newLocalListId <= -1) {
 					user.setLocalListId(serverListId);
 					user.setIsPending(true);
 				} else {
 					user.setLocalListId(newLocalListId);
 					user.setIsPending(false);
 				}
 				userList.add(user);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 		return userList;
 	}
 
 }
