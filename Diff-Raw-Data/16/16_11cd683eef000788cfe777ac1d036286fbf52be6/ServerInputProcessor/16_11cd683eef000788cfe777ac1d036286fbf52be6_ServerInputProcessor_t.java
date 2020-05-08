 package server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 
 import shared.InputProcessor;
 import shared.ProjectConfig;
 
 import comm.CommManager;
 
 import crypto.Hash;
 import database.DBManager;
 import database.DatabaseAdmin;
 import database.SocialNetworkDatabaseBoards;
 
 public class ServerInputProcessor extends InputProcessor {
 	private OutputStream os;
 	private InputStream is;
 	private Cipher c;
 	private SecretKey sk;
 	
 	private static final boolean DEBUG = ProjectConfig.DEBUG;
 
 	private String user = null;
 	private String[] currentPath;
 
 	public void processCommand(String inputLine) throws IOException {
 		if (inputLine.matches("^login .+")) {
 			System.out.println("I'm at processCommand login!");
 			if (user == null) {
 				processLogin(inputLine);
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^register$")) {
 			if (user == null) {
 				processRegistration();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^regRequests$")) {
 			if (user != null) {
 				processRegRequests();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^addFriend.*")) {
 			if (user != null) {
 				processAddFriend(inputLine);
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^createBoard .+")) {
 			if (user != null) {
 				processCreateBoard(inputLine);
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^refresh$")) {
 			if (user != null) {
 				processRefresh();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^goto .+")) {
 			if (user != null) {
 				processGoto(inputLine);
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^createRegion .+")) {
 			if (user != null) {
 				processCreateRegion(inputLine);
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^post$")) {
 			if (user != null) {
 				processPost();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^reply$")) {
 			if (user != null) {
 				processReply();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^friendRequests$")) {
 			if (user != null) {
 				processFriendRequests();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^deleteUser$")) {
 			if (user != null) {
 				processDeleteUser();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^showFriends$")) {
 			if (user != null) {
 				processShowFriends();
 			} else {
 				CommManager.send("", os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^changeUserRole$")) {
 			if (user != null) {
 				processChangeUserRole();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^transferSA$")) {
 			if (user != null) {
 				processTransferSA();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^logout$")) {
 			if (user != null) {
 				processLogout();
 			} else {
 				CommManager.send("" , os, c, sk);
 			}
 			return;
 		}
 		if (inputLine.matches("^help$")) {
 			if (user != null) {
 				CommManager.send("help", os, c, sk);
 			} else {
 				CommManager.send("", os, c, sk);
 			}
 			return;
 		}
 		/*
 		 * if (inputLine.matches(COMMANDS[15])) { if (user != null) {
 		 * processParticipants(); } else { out.println(); } return; } if
 		 * (inputLine.matches(COMMANDS[16])) { if (user != null) {
 		 * processAddParticipants(); } else { out.println(); } return; } if
 		 * (inputLine.matches(COMMANDS[17])) { if (user != null) {
 		 * processRemoveParticipants(); } else { out.println(); } return; } if
 		 * (inputLine.matches(COMMANDS[18])) { if (user != null) {
 		 * processEditParticipants(); } else { out.println(); } return; }
 		 */
 		CommManager.send("", os, c, sk);
 	}
 
 	public ServerInputProcessor(OutputStream os, InputStream is, 
 			Cipher c, SecretKey sk) {
 		this.os = os;
 		this.is = is;
 		this.c = c;
 		this.sk = sk;
 		this.currentPath = new String[3];
 		for (int i = 0; i < currentPath.length; i++) {
 			currentPath[i] = null;
 		}
 	}
 
 	private void processLogin(String inputLine) {
 		Connection conn = DBManager.getConnection();
 		String username = getValue(inputLine);
 
 		boolean userExist = false;
 		boolean pwMatch = false;
 		
 		String pwhash = "", aname = "", role = "";
 		String command = "";
 		
 		// check username existence
 		String[] userInfo = DatabaseAdmin.getUserInfo(conn, username);
 		if (userInfo != null) {
 			userExist = true;
 			pwhash = userInfo[1];
 			aname = userInfo[2];
 			role = userInfo[3];
 		}
 		if (userExist) {
 			command = "setSalt "+pwhash.substring(0, Hash.SALT_STRING_LENGTH) + ";";
 		}
 		// ask for password
 		command += "print Input password:;getPassword";
 		CommManager.send(command, os, c, sk);
 		String enteredPwdHash = CommManager.receive(is, c, sk);
 		
 		// check password
 		if (userExist) {
 			pwMatch = Hash.comparePwd(pwhash, enteredPwdHash);
 		}
 
 		// Output for Client
 		if (userExist && pwMatch) {
 			user = username;
 			command = "setLoggedIn true;" + "print Logged in as: " + username
 					+ ";" + "print Role: " + role.toUpperCase() + ";"
 					+ "print A Cappella Group: " + aname + ";print ;";
 
 			// Get friend requests
 			command += SocialNetworkAdmin.friendReqNotification(conn, username);
 
 			// if admin or SA, get pending registration requests
 			if (role.equals("admin") || role.equals("sa")) {
 				String regReqCommand = SocialNetworkAdmin.regReqNotification(conn, username);
 				command += regReqCommand + ";";
 			}
 
 			String hr = getHR(80);
 			command += hr + "print ;";
 
 			// printing out boards
 			CommManager.send(command + SocialNetworkNavigation.printPath(currentPath)
 					+ SocialNetworkBoards.viewBoards(user), os, c, sk);
 		} else {
 			CommManager.send("print username does not exist or invalid password.", os, c, sk);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void processRegistration(){
 		String newUser = "";
 		CommManager.send("print Choose a username:;askForInput;", os, c, sk);
 
 		boolean userExist = true;
 		Connection conn = DBManager.getConnection();
 
 		// check if username already exist
 		while (userExist) {
 			newUser = CommManager.receive(is, c, sk);
 			if (newUser.equals("cancel")) {
 				CommManager.send("", os, c, sk);
 				return;
 			}
 			String[] userInfo = DatabaseAdmin.getUserInfo(conn, newUser);
 			if (userInfo == null)
 				userExist = false;
 
 			// TODO: check that username is legal and isn't keywords like cancel
 
 			if (userExist) {
 				CommManager.send("print Username already exist. Choose a different one.;"
 						+ "askForInput;", os, c, sk);
 			}
 		}
 
 		// username isn't already in the DB
 		boolean groupExist = false;
 		String command = "";
 		Map<Integer, String> groupList = DatabaseAdmin.getGroupList(conn);
 		
 		String groupNum = "";
 		int aid = 0;
 		
 		// check if chosen group exist
 		while (!groupExist) {
 			command += SocialNetworkAdmin.displayGroupList(conn, groupList, newUser);
 			CommManager.send(command, os, c, sk);
 			
 			groupNum = CommManager.receive(is, c, sk);
 			if (groupNum.equals("cancel")) {
 				CommManager.send("", os, c, sk);
 				return;
 			}
 			
 			aid = Integer.parseInt(groupNum);
 			if (!groupList.containsKey(aid)) {
 				command = "print Please choose a group from the list.;";
 			} else {
 				groupExist = true;
 			}
 		}
 		
 		// create password
 		CommManager.send("createPassword", os, c, sk);
 		String pwdStore = CommManager.receive(is, c, sk);
 		
 		CommManager.send(SocialNetworkAdmin.insertRegRequest(conn, newUser, aid, pwdStore), os, c, sk);
 		DBManager.closeConnection(conn);
 	}
 
 	private void processRegRequests() throws IOException {
 		Connection conn = DBManager.getConnection();
 		String[] currentUser = DatabaseAdmin.getUserInfo(conn, user);
 		if (currentUser[3].equals("admin") || currentUser[3].equals("sa")) {
 			String command = SocialNetworkAdmin.regRequests(conn, user);
 			CommManager.send(command, os, c, sk);
 			if (command.endsWith("askForInput;")) {
 				regApproval(conn, CommManager.receive(is, c, sk));
 			}
 		} else {
 			CommManager.send("", os, c, sk);
 		}
 		DBManager.closeConnection(conn);
 	}
 
 	private void regApproval(Connection conn, String input) {
 		if (input.equals("cancel")) {
 			CommManager.send("", os, c, sk);
 			return;
 		}
 		String[] currentUser = DatabaseAdmin.getUserInfo(conn, user);
 		if (currentUser[3].equals("admin") || currentUser[3].equals("sa")) {
 			String command = "";
 			if (input.matches("^approve.+")) {
 				String value = getValue(input);
 				String delim = " *, *";
 				String[] approvedUsers = value.split(delim);
 				for (String u: approvedUsers) {
 					command += SocialNetworkAdmin.regApprove(conn, u);
 				}
 				CommManager.send(command, os, c, sk);
 				return;
 			}
 			if (input.matches("^remove.+")) {
 				String value = getValue(input);
 				String delim = " *, *";
 				String[] deletingUsers = value.split(delim);
 				for (String u: deletingUsers) {
 					command += SocialNetworkAdmin.regRemove(conn, u);
 				}
 				CommManager.send(command, os, c, sk);
 				return;
 			}
 		}
 	}
 
 	private void processAddFriend(String input) throws IOException {
 		Connection conn = DBManager.getConnection();
 		// Stores a list of users that is not the current user, who is not a
 		// friend of the
 		// current user, and has not requested current user as friend
 		// Users[0]: username. Users[1]: group name
 		List<String[]> friendableUsers = DatabaseAdmin.getFriendableUsers(conn, user);
 
 		// input of format "addFriend" or "addFriend b"
 		boolean userExist = false;
 		String toFriend = "";
 		String command = "";
 		while (!userExist) {
 			String prefix = "";
 			if (!input.equals("addFriend")) {
 				prefix = getValue(input);
 			}
 			command += SocialNetworkAdmin.displayFriendableUsers(conn, user, prefix, friendableUsers);
 			CommManager.send(command, os, c, sk);
 
 			toFriend = CommManager.receive(is, c, sk);
 			if (toFriend.equals("cancel")) {
 				CommManager.send("", os, c, sk);
 				return;
 			}
 			for (String[] userInfo : friendableUsers) {
 				if (userInfo[0].equals(toFriend)) {
 					userExist = true;
 					break;
 				}
 			}
 			if (!userExist) {
 				command = "print Cannot friend " + toFriend + ";";
 			}
 		}
 
 		// Gets back name of person to add as friend
 		addFriend(conn, toFriend);
 		DBManager.closeConnection(conn);
 	}
 
 	private void addFriend(Connection conn, String username) throws IOException {
 		// username exists in the system.
 		CommManager.send("print Are you sure you want to add " + username
 				+ " as a friend? (y/n);askForInput", os, c, sk);
 		String input = CommManager.receive(is, c, sk);
 		String command = "";
 		if (input.equals("y")) {
 			command = SocialNetworkAdmin.insertFriendRequest(conn, username, user);
 		} else if (input.equals("n")) {
 			command = "print Canceled.;";
 		} else if (input.equals("cancel")) {
 			command = "";
 		}
 		CommManager.send(command, os, c, sk);
 	}
 
 		private void processFriendRequests() throws IOException {
 			Connection conn = DBManager.getConnection();
 			String command = SocialNetworkAdmin.friendRequests(conn, user);
 			CommManager.send(command, os, c, sk);
 			if (command.endsWith("askForInput;")) {
 				friendApproval(conn, CommManager.receive(is, c, sk));
 			}
 			DBManager.closeConnection(conn);
 		}
 
 	private void friendApproval(Connection conn, String input) {
 		if (input.equals("cancel")) {
 			CommManager.send("", os, c, sk);
 			return;
 		}
 		String command = "";
 		if (input.matches("^approve.+")) {
 			String value = getValue(input);
 			String delim = " *, *";
 			String[] approvedFriends = value.split(delim);
 			for (String u: approvedFriends) {
 				command += SocialNetworkAdmin.friendApprove(conn, u, user);
 			}
 			CommManager.send(command, os, c, sk);
 			return;
 		}
 		if (input.matches("^remove.+")) {
 			String value = getValue(input);
 			String delim = " *, *";
 			String[] usersToDelete = value.split(delim);
 			for (String u: usersToDelete) {
 				command += SocialNetworkAdmin.friendReqRemove(conn, u, user);
 			}
 			CommManager.send(command, os, c, sk);
 			return;
 		}
 	}
 
 	//----------------------cleaning----------------------------------------
 	// XXX working here KILL ME!
 	private void processDeleteUser() throws IOException {
 		// TODO: check to see if user is actually a SA
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		ResultSet usersResult = null;
 
 		// Stores a list of deletable users
 		ArrayList<String> deletableUsers = new ArrayList<String>();
 		try {
 			stmt = conn.createStatement();
 
 			String query = "SELECT username FROM main.users "
 					+ "WHERE username != '" + user + "' AND aid = "
 					+ "(SELECT aid FROM main.users WHERE username = '" + user
 					+ "')";
 
 			if (DEBUG) {
 				System.out
 						.println("Delete User deletable user query: " + query);
 			}
 
 			usersResult = stmt.executeQuery(query);
 			while (usersResult.next()) {
 				deletableUsers.add(usersResult.getString("username"));
 			}
 
 			boolean userDeletable = false;
 			String toDelete = "";
 
 			while (!userDeletable) {
 				String command = "print Users in your A Cappella group that you can delete:;";
 				for (String userInfo : deletableUsers) {
 					command += "print " + userInfo + ";";
 				}
 				command += "print ;print Type the name of the user you wish to delete:;"
 						+ "askForInput";
 				CommManager.send(command, os, c, sk);
 
 				toDelete = CommManager.receive(is, c, sk);
 				if (toDelete.equals("cancel")) {
 					CommManager.send("", os, c, sk);
 					return;
 				}
 				if (deletableUsers.contains(toDelete)) {
 					userDeletable = true;
 				}
 				if (!userDeletable) {
 					CommManager.send("print Cannot delete " + toDelete + ";", os, c, sk);
 				}
 			}
 
 			// toDelete is deletable
 			deleteUser(toDelete);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBManager.closeResultSet(usersResult);
 			DBManager.closeStatement(stmt);
 			DBManager.closeConnection(conn);
 		}
 
 	}
 
 	private void deleteUser(String username) throws IOException {
 		// username is a deletable user
 		CommManager.send("print User deletions cannot be undone.;"
 				+ "print Are you sure you want to delete this user? (y/n);askForInput", os, c, sk);
 		String input = CommManager.receive(is, c, sk);
 		Connection conn = null;
 		Statement stmt = null;
 		if (input.equals("y")) {
 			try {
 				conn = DBManager.getConnection();
 				String query = "DELETE FROM main.users WHERE username = '"
 						+ username + "'";
 				stmt = conn.createStatement();
 				stmt.executeUpdate(query);
 
 				// print out confirmation
 				CommManager.send("print " + username
 						+ " has been deleted from the system.", os, c, sk);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} finally {
 				DBManager.closeStatement(stmt);
 				DBManager.closeConnection(conn);
 			}
 
 		} else if (input.equals("n")) {
 			CommManager.send("print Canceled.", os, c, sk);
 		} else if (input.equals("cancel")) {
 			CommManager.send("", os, c, sk);
 		}
 	}
 
 	private void processShowFriends() {
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		ResultSet friendsResult = null;
 
 		// get the list of friends for the current user
 		ArrayList<String> existingFriends = new ArrayList<String>();
 		try {
 			String query = "SELECT * FROM main.friends WHERE username1 = '"
 					+ user + "' OR " + "username2 = '" + user + "'";
 			stmt = conn.createStatement();
 			friendsResult = stmt.executeQuery(query);
 			while (friendsResult.next()) {
 				if (friendsResult.getString("username1").equals(user)) {
 					existingFriends.add(friendsResult.getString("username2"));
 				} else {
 					existingFriends.add(friendsResult.getString("username1"));
 				}
 			}
 
 			// printing out friend list
 			String command = "print Your friends:;";
 			if (existingFriends.size() == 0) {
 				command = "print You have no friends right now.;"
 						+ "print To add a friend: type addFriend";
 			} else {
 				for (String friend : existingFriends) {
 					command += "print " + friend + ";";
 				}
 			}
 			CommManager.send(command, os, c, sk);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBManager.closeResultSet(friendsResult);
 			DBManager.closeStatement(stmt);
 			DBManager.closeConnection(conn);
 		}
 
 	}
 
 	private void processChangeUserRole() throws IOException {
 		// TODO: check to see if user is actually a SA
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		ResultSet usersResult = null;
 		try {
 			stmt = conn.createStatement();
 
 			// Stores a list of changeable users
 			// Users[0]: username, Users[1]: role
 			ArrayList<String[]> changeableUsers = new ArrayList<String[]>();
 
 			String query = "SELECT username, role FROM main.users "
 					+ "WHERE username != '" + user + "' AND aid = "
 					+ "(SELECT aid FROM main.users WHERE username = '" + user
 					+ "')";
 
 			if (DEBUG) {
 				System.out.println("role: changeable role query: " + query);
 			}
 
 			usersResult = stmt.executeQuery(query);
 			while (usersResult.next()) {
 				String[] userInfo = { usersResult.getString("username"),
 						usersResult.getString("role").toUpperCase() };
 				changeableUsers.add(userInfo);
 			}
 
 			boolean userChangeable = false;
 			String toChange = "";
 			String role = "";
 
 			while (!userChangeable) {
 				String command = "print Users in your A Cappella group that you can change roles for:;";
 				for (String[] userInfo : changeableUsers) {
 					command += "print " + userInfo[0] + " (" + userInfo[1]
 							+ ");";
 				}
 				command += "print ;print Type the name of the user you wish to change role for:;"
 						+ "askForInput";
 				CommManager.send(command, os, c, sk);
 
 				toChange = CommManager.receive(is, c, sk);
 				if (toChange.equals("cancel")) {
 					CommManager.send("", os, c, sk);
 					return;
 				}
 				for (String[] userInfo : changeableUsers) {
 					if (toChange.equals(userInfo[0])) {
 						if (userInfo[1].equals("ADMIN")) {
 							role = "member";
 						} else {
 							role = "admin";
 						}
 						userChangeable = true;
 						break;
 					}
 				}
 				if (!userChangeable) {
 					CommManager.send("print Cannot change role for " + toChange + ";", os, c, sk);
 				}
 			}
 
 			// toChange is changeable
 			changeRole(toChange, role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBManager.closeResultSet(usersResult);
 			DBManager.closeStatement(stmt);
 			DBManager.closeConnection(conn);
 		}
 
 	}
 
 	private void changeRole(String toChange, String role) {
 		// change user's role in the DB to 'role'
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		String query = "UPDATE main.users SET role = '" + role
 				+ "' WHERE username = '" + toChange + "'";
 		try {
 			stmt = conn.createStatement();
 			stmt.executeUpdate(query);
 
 			// print out confirmation on client.
 			String from;
 			if (role.equals("admin")) {
 				from = "MEMBER";
 			} else {
 				from = "ADMIN";
 			}
 			CommManager.send("print Role for " + toChange
 					+ " has been changed from " + from + " to "
 					+ role.toUpperCase(), os, c, sk);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBManager.closeStatement(stmt);
 			DBManager.closeConnection(conn);
 		}
 
 	}
 
 	private void processTransferSA() throws IOException {
 		// TODO: check to see if user is actually a SA
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		ResultSet usersResult = null;
 		try {
 			stmt = conn.createStatement();
 
 			// Stores a list of changeable users
 			// Users[0]: username
 			ArrayList<String> groupAdmins = new ArrayList<String>();
 
 			String query = "SELECT username FROM main.users "
 					+ "WHERE username != '" + user + "' AND aid = "
 					+ "(SELECT aid FROM main.users WHERE username = '" + user
 					+ "') AND " + "role = 'admin'";
 
 			if (DEBUG) {
 				System.out.println("transfer: adminUser query: " + query);
 			}
 
 			usersResult = stmt.executeQuery(query);
 			while (usersResult.next()) {
 				groupAdmins.add(usersResult.getString("username"));
 			}
 
 			boolean transferableUser = false;
 			String toChange = "";
 
 			while (!transferableUser) {
 				String command = "print Users in your A Cappella group that you can transfer SA tole to:;";
 				for (String userInfo : groupAdmins) {
 					command += "print " + userInfo + " (ADMIN);";
 				}
 				command += "print ;print Type the name of the user you wish to transfer SA role to:;"
 						+ "askForInput";
 				CommManager.send(command, os, c, sk);
 
 				toChange = CommManager.receive(is, c, sk);
 				if (toChange.equals("cancel")) {
 					CommManager.send("", os, c, sk);
 					return;
 				}
 				if (groupAdmins.contains(toChange)) {
 					transferableUser = true;
 				}
 				if (!transferableUser) {
 					CommManager.send("print Cannot transfer SA role to " + toChange
 							+ ";", os, c, sk);
 				}
 			}
 
 			// toChange is an admin that can have SA transferred to
 			transferSA(toChange);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			DBManager.closeResultSet(usersResult);
 			DBManager.closeStatement(stmt);
 			DBManager.closeConnection(conn);
 		}
 
 	}
 
 	private void transferSA(String toChange) {
 		// transfer SA-ship
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		String promoteQuery = "UPDATE main.users SET role = 'sa' WHERE username = '"
 				+ toChange + "'";
 		String demoteQuery = "UPDATE main.users SET role = 'admin' WHERE username = '"
 				+ user + "';";
 		try {
 			conn.setAutoCommit(false);
 			stmt = conn.createStatement();
 			stmt.executeUpdate(promoteQuery);
 			stmt.executeUpdate(demoteQuery);
 			conn.commit();
 
 			// Print confirmation to client
 			CommManager.send("print SA role has been transferred to " + toChange, os, c, sk);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DBManager.rollback(conn);
 		} finally {
 			DBManager.closeStatement(stmt);
 			DBManager.closeConnection(conn);
 		}
 
 	}
 
 	private void processLogout() {
 		user = null;
 		for (int i = 0; i < currentPath.length; i++) {
 			currentPath[i] = null;
 		}
 		CommManager.send("print Logged out.;setLoggedIn false", os, c, sk);
 	}
 
 	private void processParticipants() {
 		Connection conn = DBManager.getConnection();
 		String board = currentPath[0];
 		ArrayList<String> admins = SocialNetworkDatabaseBoards.getBoardAdmins(conn, board);
 		if (admins.contains(user)) {
 			// print displaying participants
 			String region = currentPath[1];
 			String command = "print Displaying participants:;";
 
 			// print a list of participants of the region
 			Statement stmt = null;
 			try {
 				stmt = conn.createStatement();
 				String query = "SELECT username, privilege FROM " + board
 						+ ".regionprivileges WHERE rname = '" + region + "'";
 				ResultSet partResult = stmt.executeQuery(query);
 				while (partResult.next()) {
 					command += "print " + partResult.getString("username");
 					if (partResult.getString("privilege").equals("view")) {
 						command += " (view only);";
 					} else {
 						command += ";";
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			// print Commands: addParticipants, removeParticipants,
 			// editParticipants, addAdmin
 			command += "print ;print Other Commands: addParticipants, removeParticipants";
 			// TODO: add edit and addAdmin to this list of commands
 			CommManager.send(command, os, c, sk);
 		} else {
 			CommManager.send("print You do not have permission to view participants.", os, c, sk);
 		}
 	}
 
 	private void processAddParticipants() throws IOException {
 		// check if user is admin
 		Connection conn = DBManager.getConnection();
 		String board = currentPath[0];
 		ArrayList<String> admins = SocialNetworkDatabaseBoards.getBoardAdmins(conn, board);
 		if (admins.contains(user)) {
 			// get a list of the user's friends
 			List<String> usersFriends = DatabaseAdmin.getFriends(conn, user);
 
 			// get a list of participants
 			String region = currentPath[1];
 			ArrayList<String> participants = new ArrayList<String>();
 			Statement stmt = null;
 			String query = "SELECT username, privilege FROM " + board
 					+ ".regionprivileges WHERE rname = '" + region + "'";
 			try {
 				stmt = conn.createStatement();
 				ResultSet partResult = stmt.executeQuery(query);
 				while (partResult.next()) {
 					participants.add(partResult.getString("username"));
 				}
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			ArrayList<String> addableUsers = new ArrayList<String>();
 			for (String friend : usersFriends) {
 				if (!participants.contains(friend)) {
 					addableUsers.add(friend);
 				}
 			}
 			boolean userAddable = false;
 			ArrayList<String[]> addInfo = new ArrayList<String[]>();
 
 			while (!userAddable) {
 				String command = "print List of people you could add:;";
 				for (String user : addableUsers) {
 					command += "print " + user + ";";
 				}
 				command += "print ;print [To add user: (<user1>, <privilege>), "
 						+ "(<user2>, <privilege>) where <privilege> = view or viewpost];askForInput";
 				CommManager.send(command, os, c, sk);
 				String input = CommManager.receive(is, c, sk);
 				if (input.equals("cancel")) {
 					CommManager.send("", os, c, sk);
 					return;
 				}
 				addInfo = parseAddUserInfo(input);
 				String notOkUsers = "";
 				userAddable = true;
 				for (String[] userInfo : addInfo) {
 					if (!addableUsers.contains(userInfo[0])) {
 						userAddable = false;
 						notOkUsers += userInfo[0] + ", ";
 					}
 				}
 				if (!userAddable) {
 					notOkUsers = notOkUsers.substring(0,
 							notOkUsers.length() - 2);
 					CommManager.send("print Cannot add " + notOkUsers
 							+ " as participants of this region.;", os, c, sk);
 				}
 			}
 
 			// toAdd is addable
 			addParticipant(addInfo);
 		} else {
 			CommManager.send("print You do not have permission to add participants to this region.", os, c, sk);
 		}
 	}
 
 	private ArrayList<String[]> parseAddUserInfo(String input) {
 		ArrayList<String[]> addInfo = new ArrayList<String[]>();
 		String[] infoPairs = input.split(" *, *");
 		for (String pair : infoPairs) {
 			pair = pair.trim();
 			String[] userInfo = pair.substring(1, pair.length() - 1).trim()
 					.split(" *, *");
 			addInfo.add(userInfo);
 		}
 
 		return addInfo;
 	}
 
 	private void addParticipant(ArrayList<String[]> addInfo) {
 		// add everything to the database
 		String region = currentPath[1];
 		Connection conn = DBManager.getConnection();
 		String query = "INSERT INTO board.regionprivileges (rname, username, privilege, grantedBy) VALUES ";
 		for (String[] userInfo : addInfo) {
 			query += "('" + region + "', '" + userInfo[0] + "', '"
 					+ userInfo[1] + "', '" + user + "'), ";
 		}
 		query = query.substring(0, query.length() - 2);
 
 		if (DEBUG) {
 			System.out.println("addParticipants query: " + query);
 		}
 
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(query);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// print confirmation
 		CommManager.send("print Participants added.", os, c, sk);
 	}
 
 	private void processRemoveParticipants() throws IOException {
 		// TODO: What's the policy for deleting admins from a board?
 
 		// check if user is admin
 		Connection conn = DBManager.getConnection();
 		String board = currentPath[0];
 		ArrayList<String> admins = SocialNetworkDatabaseBoards.getBoardAdmins(conn, board);
 		if (admins.contains(user)) {
 			// get a list of participants
 			String region = currentPath[1];
 			ArrayList<String> participants = new ArrayList<String>();
 			Statement stmt = null;
 			String query = "SELECT username, privilege FROM " + board
 					+ ".regionprivileges WHERE rname = '" + region + "'";
 			try {
 				stmt = conn.createStatement();
 				ResultSet partResult = stmt.executeQuery(query);
 				while (partResult.next()) {
 					participants.add(partResult.getString("username"));
 				}
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			boolean userRemovable = false;
 			String[] toRemove = null;
 
 			while (!userRemovable) {
 				String command = "print List of people you could remove:;";
 				for (String user : participants) {
 					command += "print " + user + ";";
 				}
 				command += "print ;print [To remove participants: <user1>, <user2>];askForInput";
 				CommManager.send(command, os, c, sk);
 				String input = CommManager.receive(is, c, sk);
 				if (input.equals("cancel")) {
 					CommManager.send("", os, c, sk);
 					return;
 				}
 				toRemove = input.trim().split(" *, *");
 
 				String notOkUsers = "";
 				userRemovable = true;
 				for (String userInfo : toRemove) {
 					if (!participants.contains(userInfo)) {
 						userRemovable = false;
 						notOkUsers += userInfo + ", ";
 					}
 				}
 				if (!userRemovable) {
 					notOkUsers = notOkUsers.substring(0,
 							notOkUsers.length() - 2);
 					CommManager.send("print Cannot remove " + notOkUsers
 							+ " from this region.;", os, c, sk);
 				}
 			}
 
 			// toRemove is removable
 			removeParticipant(toRemove);
 		} else {
 			CommManager.send("print You do not have permission to add participants to this region.", os, c, sk);
 		}
 	}
 
 	private void removeParticipant(String[] toRemove) {
 		// add everything to the database
 		String region = currentPath[1];
 		String board = currentPath[0];
 		Connection conn = DBManager.getConnection();
 		String query = "DELETE FROM " + board
 				+ ".regionprivileges WHERE rname = '" + region + "' AND (";
 		for (int i = 0; i < toRemove.length; i++) {
 			query += "username = '" + toRemove[i] + "'";
 			if (i != toRemove.length - 1) {
 				query += " OR ";
 			}
 		}
 		query += ")";
 
 		if (DEBUG) {
 			System.out.println("remove participant query: " + query);
 		}
 
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(query);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// print confirmation
 		CommManager.send("print Participants removed.", os, c, sk);
 	}
 
 	private void processEditParticipants() throws IOException {
 		Connection conn = DBManager.getConnection();
 		Statement stmt = null;
 		try {
 			stmt = conn.createStatement();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		String board = currentPath[0];
 		String region = currentPath[1];
 
 		// Stores a list of changeable users
 		// Users[0]: username, Users[1]: permission
 		ArrayList<String[]> changeableUsers = new ArrayList<String[]>();
 		try {
 			String query = "SELECT username, privilege FROM " + board
 					+ ".regionprivileges " + "WHERE rname = '" + region + "'";
 
 			if (DEBUG) {
 				System.out.println("Edit participant query: " + query);
 			}
 
 			ResultSet usersResult = stmt.executeQuery(query);
 			while (usersResult.next()) {
 				String[] userInfo = { usersResult.getString("username"),
 						usersResult.getString("privilege") };
 				changeableUsers.add(userInfo);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		boolean userChangeable = false;
 		String input = "";
 		ArrayList<String[]> changingUsersInfo = new ArrayList<String[]>();
 		String[] usersToChange = null;
 
 		while (!userChangeable) {
 			String command = "print List of people you can edit permission for:;";
 			for (String[] userInfo : changeableUsers) {
 				String permission = null;
 				if (userInfo[1].equals("view")) {
 					permission = "View Only";
 				} else {
 					permission = "View and Post";
 				}
 				command += "print " + userInfo[0] + " (" + permission + ");";
 			}
 			command += "print ;print [To toggle permission status: <user1>, <user2>];"
 					+ "askForInput";
 			CommManager.send(command, os, c, sk);
 
 			input = CommManager.receive(is, c, sk);
 			if (input.equals("cancel")) {
 				CommManager.send("", os, c, sk);
 				return;
 			}
 			String toChange = "";
 			usersToChange = input.trim().split(" *, *");
 			userChangeable = true;
 			for (String user : usersToChange) {
 				boolean userChangeable2 = false;
 				for (String[] userInfo : changeableUsers) {
 					if (user.equals(userInfo[0])) {
 						changingUsersInfo.add(userInfo);
 						userChangeable2 = true;
 						break;
 					}
 				}
 				if (!userChangeable2) {
 					toChange += "user, ";
 					userChangeable = false;
 				}
 			}
 			if (!userChangeable) {
 				toChange = toChange.substring(0, toChange.length() - 2);
 				CommManager.send("print Cannot change permission for " + toChange
 						+ ";", os, c, sk);
 			}
 		}
 
 		// toChange is changeable
 		changePermission(usersToChange);
 	}
 
 	private void changePermission(String[] usersToChange) {
 		// TODO: not done
 		// add everything to the database
 		String board = currentPath[0];
 		String region = currentPath[1];
 		Connection conn = DBManager.getConnection();
 
 		String query = "UPDATE " + board + ".regionprivileges WHERE rname = '"
 				+ region + "' AND (";
 		// (username = 'userInfo' OR username = 'userInfo')
 		for (int i = 0; i < usersToChange.length; i++) {
 			query += "username = '" + usersToChange[i] + "'";
 			if (i != usersToChange.length - 1) {
 				query += " OR ";
 			}
 		}
 		query += ")";
 
 		if (DEBUG) {
 			System.out.println("remove participant query: " + query);
 		}
 
 		try {
 			Statement stmt = conn.createStatement();
 			stmt.executeUpdate(query);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// print confirmation
 		CommManager.send("print Participants removed.", os, c, sk);
 	}
 
 	/**
 	 * Creates a board. MUST be in the home directory.
 	 */
 	private void processCreateBoard(String input) throws IOException {
 		/*
 		 * Ensure the person is in the right place to create a board (on the
 		 * homepage)
 		 */
 		if (currentPath[0] != null) {
 			CommManager.send("print Must be at Home to create a board", os, c, sk);
 		} else {
 			String boardname = input.substring(("createBoard ").length());
 			CommManager.send(SocialNetworkBoards.createBoard(user, boardname), os, c, sk);
 		}
 	}
 
 	/**
 	 * Depending on where the user is, fetches the correct view of information.
 	 * The user prints their current path and the information associated with
 	 * it.
 	 */
 	private void processRefresh() {
 		String boardName = currentPath[0];
 		if (boardName == null) {
 			CommManager.send(SocialNetworkNavigation.printPath(currentPath)
 					+ "print ;" + SocialNetworkBoards.viewBoards(user), os, c, sk);
 		} else if (boardName.equals("freeforall")) {
 			/* No regions */
 			String postNum = currentPath[1];
 			if (postNum == null) { // Merely in the board
 				CommManager.send(SocialNetworkNavigation.printPath(currentPath)
 						+ "print ;"
 						+ SocialNetworkPosts
 								.viewPostList(user, boardName, null), os, c, sk);
 			} else { // Inside the post
 				CommManager.send(SocialNetworkNavigation.printPath(currentPath)
 						+ "print ;"
 						+ SocialNetworkPosts.viewPost(user, boardName, null,
 								Integer.parseInt(postNum)), os, c, sk);
 			}
 		} else { // a regular board
 			String regionName = currentPath[1];
 			if (regionName == null) { // Merely in the board
 				CommManager.send(SocialNetworkNavigation.printPath(currentPath)
 						+ "print ;"
 						+ SocialNetworkRegions.viewRegions(user, boardName), os, c, sk);
 			} else {
 				String postNum = currentPath[2];
 				if (postNum == null) { // Merely in the region
 					CommManager.send(SocialNetworkNavigation.printPath(currentPath)
 							+ "print ;"
 							+ SocialNetworkPosts.viewPostList(user, boardName,
 									regionName), os, c, sk);
 				} else { // Inside the post
 					CommManager.send(SocialNetworkNavigation.printPath(currentPath)
 							+ "print ;"
 							+ SocialNetworkPosts.viewPost(user, boardName,
 									regionName, Integer.parseInt(postNum)), os, c, sk);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Depending on where the user is, processes where the user should go.
 	 */
 	private void processGoto(String inputLine) {
 		String destination = inputLine.substring(("goto ").length());
 		int validDest = SocialNetworkNavigation.validDestination(currentPath,
 				destination);
 		switch (validDest) {
 		case -1: /* Go backwards */
 			SocialNetworkNavigation.goBack(currentPath);
 			processRefresh();
 			break;
 		case 2: /* Go immediately home */
 			for (int i = 0; i < currentPath.length; i++) {
 				currentPath[i] = null;
 			}
 			processRefresh();
 			break;
 		case 1: /* Go forward in the hierarchy to destination */
 			/* Have different cases depending on the current path */
 			if (currentPath[0] == null) {
 				CommManager.send(SocialNetworkNavigation.goToBoard(user,
 						currentPath, destination), os, c, sk);
 			} else if (currentPath[0].equals("freeforall")) {
 				Integer postNum = null;
 				try {
 					postNum = Integer.parseInt(destination);
 				} catch (NumberFormatException e) {
 					CommManager.send("print You entered an invalid post number. Type \"goto ###\", or \"goto ..\" to "
 							+ "go backwards", os, c, sk);
 				}
 				if (postNum != null) {
 					CommManager.send(SocialNetworkNavigation.goToPost(user,
 							currentPath, postNum.intValue()), os, c, sk);
 				}
 			} else {
 				if (currentPath[1] != null) {
 					Integer postNum = null;
 					try {
 						postNum = Integer.parseInt(destination);
 					} catch (NumberFormatException e) {
 						CommManager.send("print You entered an invalid post number. Type \"goto ###\", or \"goto ..\" to "
 								+ "go backwards", os, c, sk);
 					}
 					if (postNum != null) {
 						CommManager.send(SocialNetworkNavigation.goToPost(user,
 								currentPath, postNum), os, c, sk);
 					}
 				} else {
 					CommManager.send(SocialNetworkNavigation.goToRegion(user,
 							currentPath, destination), os, c, sk);
 				}
 			}
 			break;
 		default:
 			CommManager.send("print Invalid destination given your current path: "
 					+ SocialNetworkNavigation.printPath(currentPath) + ".; "
 					+ "print You can go backwards by typing \"..\" ", os, c, sk);
 
 		}
 	}
 
 	/**
 	 * Creates a region for the user. The user must be in a board (except
 	 * freeforall) to execute the command.
 	 */
 	private void processCreateRegion(String inputLine) {
 		String boardName = currentPath[0];
 		String regionName = inputLine.substring(("createRegion ").length());
 		if (boardName == null) {
 			CommManager.send("print Must be in the desired board in order to create the region.", os, c, sk);
 		} else if (boardName.equals("freeforall")) {
 			CommManager.send("print Cannot create regions in the freeforall board.", os, c, sk);
 		} else if (currentPath[1] != null) {
 			CommManager.send("print Must be exactly in the desired board (i.e., not inside a region in the board) "
 					+ "in order to create the region", os, c, sk);
 		} else {
 			CommManager.send(SocialNetworkRegions.createRegion(user, currentPath[0],
 					regionName), os, c, sk);
 		}
 	}
 
 	private void processPost() throws IOException {
 		/* Verify the user is in the right place to create a post */
 		String boardName = currentPath[0];
 		boolean canPost = false;
 		if (boardName == null) {
 			CommManager.send("print Must be within a board's region or in the freeforall board to create a post", os, c, sk);
 		} else if (boardName.equals("freeforall")) {
 			String postNum = currentPath[1];
 			if (postNum == null) {
 				canPost = true;
 			} else {
 				CommManager.send("print Must go back to the board page to create a post (not inside a post)", os, c, sk);
 			}
 		} else { // in a regular board
 			String regionName = currentPath[1];
 			if (regionName == null) {
 				CommManager.send("print Must be within a board's region or in the freeforall board to create a post", os, c, sk);
 			} else {
 				String postNum = currentPath[2];
 				if (postNum == null) { // in a board, region, not in a post
 					canPost = true;
 				} else {
 					CommManager.send("print Must go back to the region page to create a post (not inside a post)", os, c, sk);
 				}
 			}
 		}
 		if (canPost) {
 			CommManager.send("print Start typing your content. Type 'cancel' after any new line to cancel.;print "
 					+ "Press enter once to insert a new line.;print Press enter twice to submit.;askForInput ", os, c, sk);
 			String content = CommManager.receive(is, c, sk);
 			while (content.equals("")) {
 				CommManager.send("print Content is empty. Please try again. Type 'cancel' to cancel.;askForInput ", os, c, sk);
 				content = CommManager.receive(is, c, sk);
 			}
 			boolean cancelled = content.trim().equals("cancel");
 			String additionalContent = "";
 			while (!cancelled) {
 				CommManager.send("print ;askForInput ", os, c, sk);
 				additionalContent = CommManager.receive(is, c, sk);
 				if (additionalContent.equals("")) {
 					break;
 				} else if (additionalContent.trim().equals("cancel")) {
 					cancelled = true;
 				} else {
 					content += ";print \t" + additionalContent;
 				}
 			}
 			if (cancelled) {
 				CommManager.send("print Post Creation cancelled", os, c, sk);
 			} else {
 				CommManager.send(SocialNetworkPosts.createPost(user, content,
 						currentPath[0], currentPath[1]), os, c, sk);
 			}
 		}
 	}
 
 	/**
 	 * Similar to processPost basically... except that you must be in a post
 	 * 
 	 * @throws IOException
 	 */
 	private void processReply() throws IOException {
 		/* Verify the user is in the right place to create a post */
 		String boardName = currentPath[0];
 		String postNum = "";
 		boolean canReply = false;
 		if (boardName == null) {
 			CommManager.send("print Must be within a post to create a reply", os, c, sk);
 		} else if (boardName.equals("freeforall")) {
 			postNum = currentPath[1];
 			if (postNum == null) {
 				CommManager.send("print Must be within a post to create a reply", os, c, sk);
 			} else {
 				canReply = true;
 			}
 		} else { // in a regular board
 			String regionName = currentPath[1];
 			if (regionName == null) {
 				CommManager.send("print Must be within a post to create a reply", os, c, sk);
 			} else {
 				postNum = currentPath[2];
 				if (postNum == null) { // in a board, region, not in a post
 					CommManager.send("print Must be within a post to create a reply", os, c, sk);
 				} else {
 					canReply = true;
 				}
 			}
 		}
 		if (canReply) {
 			CommManager.send("print Start typing your content. Type 'cancel' after any new line to cancel.;print "
 					+ "Press enter once to insert a new line.;print Press enter twice to submit.;askForInput ", os, c, sk);
 			String content = CommManager.receive(is, c, sk);
 			while (content.equals("")) {
 				CommManager.send("print Content is empty. Please try again. Type 'cancel' to cancel.;askForInput ", os, c, sk);
 				content = CommManager.receive(is, c, sk);
 			}
 			boolean cancelled = content.trim().equals("cancel");
 			String additionalContent = "";
 			while (!cancelled) {
 				CommManager.send("print ;askForInput ", os, c, sk);
 				additionalContent = CommManager.receive(is, c, sk);
 				if (additionalContent.equals("")) {
 					break;
 				} else if (additionalContent.trim().equals("cancel")) {
 					cancelled = true;
 				} else {
 					content += ";print \t" + additionalContent;
 				}
 			}
 			if (cancelled) {
 				CommManager.send("print Reply Creation cancelled", os, c, sk);
 
 			} else {
 				CommManager.send(SocialNetworkPosts.createReply(user, content,
 						currentPath[0], currentPath[1],
 						Integer.parseInt(postNum)), os, c, sk);
 			}
 		}
 	}
 }
