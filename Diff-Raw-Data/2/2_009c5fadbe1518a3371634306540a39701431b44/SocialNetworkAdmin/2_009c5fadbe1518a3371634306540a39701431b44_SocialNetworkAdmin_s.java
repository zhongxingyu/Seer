 package server;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import shared.ProjectConfig;
 
 import database.DBManager;
 import database.DatabaseAdmin;
 
 public class SocialNetworkAdmin {
 	private static final boolean DEBUG = ProjectConfig.DEBUG;
 	
 	public static String friendReqNotification(Connection conn, String username) {
 		String command = "";
 		int requestCount = DatabaseAdmin.getFriendReqCount(conn, username);
 		if (requestCount != 0) {
 			command = ";print Pending Friend Requests (" + requestCount
 				+ ") [To view: friendRequests]";
 		}
 		return command;
 	}
 	
 	public static String regReqNotification(Connection conn, String username) {
 		String command = "";
 		int requestCount = DatabaseAdmin.getRegReqCount(conn, username);
 		if (requestCount != 0) {
 			command = ";print Pending User Registration Requests ("
 					+ requestCount + ") [To view: regRequests]";
 		}
 		return command;
 	}
 	
 	public static String insertRegRequest(Connection conn, String newUser, int aid, String pwdStore) {
 		String command = "";
 		int success = DatabaseAdmin.insertRegRequest(conn, newUser, aid, pwdStore);
 		if (success == 1) {
 			command = "print Registration request for " + newUser
 					+ " has been sent.;print Once an admin from your group "
 					+ "approves, you will be added to the system.;print ;";
 		} else if (success == -1) {
 			command = "print User already pending registration approval. Try again with a different username.;print ;";
 		} else {
 			command = "print Registration failed due to database error. " +
 					"Please try again or contact System Admin.;print ;";
 		}
 		return command;
 	}
 	
 	public static String regRequests(Connection conn, String username) {
 		String command = "";
 		List<String> pendingUsers = DatabaseAdmin.getRegRequestList(conn, username);
 		if (pendingUsers == null) {
 			command = "print Database error. Please contact System Admin.;";
 		} else if (pendingUsers.size() == 0) {
 			command = "print No pending registration requests at the moment.;";
 		} else {
 			command = "print Pending User Registration Requests ("
 					+ pendingUsers.size() + "):;";
 			for (String u: pendingUsers) {
 				command = command + "print " + u + ";";
 			}
 			command += "print ;print [To approve: approve "
 					+ "<username1>, <username2>];print [To remove: "
 					+ "remove <username1>, <username2>];askForInput;";
 		}
 		return command;
 	}
 	
 	public static String friendRequests(Connection conn, String username) {
 		String command = "";
 		List<String> pendingFriends = DatabaseAdmin.getFriendRequestList(conn, username);
 		if (pendingFriends == null) {
 			command = "print Database error. Please contact System Admin.;";
 		} else if (pendingFriends.size() == 0) {
 			command = "print No pending friend requests at the moment.;";
 		} else {
 			command = "print Pending Friend Requests (" + pendingFriends.size() + "):;";
 			for (String f: pendingFriends) {
 				command = command + "print " + f + ";";
 			}
 			command += ";print ;print [To approve: approve "
 					+ "<username1>, <username2>];print [To remove: "
					+ "remove <username1>, <username2>];askForInput";
 		}
 		return command;
 	}
 
 	public static String regApprove(Connection conn, String username) {
 		String success = "print "+username+" has been added to the system.;";
 		String error = "print Database error occurred while approving registration for " + 
 				username + ". Please try again or contact the System Admin.;";
 		String[] userInfo = DatabaseAdmin.getRegUserInfo(conn, username);
 		if (userInfo == null) {
 			if (DEBUG) System.err.println("regApprove: userInfo returned null");
 			return error;
 		}
 		String pwhash = userInfo[1];
 		int aid = Integer.parseInt(userInfo[2]);
 		try {
 			conn.setAutoCommit(false);
 		} catch (SQLException e) {
 			if (DEBUG) System.err.println("regApprove: turning off auto commit failed.");
 			return error;
 		}
 		
 		int deleteStatus = DatabaseAdmin.deleteFromReg(conn, username);
 		int addStatus = DatabaseAdmin.addUser(conn, username, pwhash, aid);
 		System.out.println("Going into addFriendsFromGroup");
 		int addFriendStatus = DatabaseAdmin.addFriendsFromGroup(conn, username, aid);
 		
 		if (deleteStatus != 1 || addStatus != 1 || addFriendStatus <= 0) {
 			DBManager.rollback(conn);
 			DBManager.trueAutoCommit(conn);
 			if (DEBUG) System.err.printf("regApprove: DB operations failed. " +
 					"deleteStatus: %d, addStatus: %d, addFriendStatus: %d\n", deleteStatus, addStatus, addFriendStatus);
 			return error;
 		} else {
 			try {
 				conn.commit();
 				DBManager.trueAutoCommit(conn);
 				return success;
 			} catch (SQLException e) {
 				DBManager.trueAutoCommit(conn);
 				if (DEBUG) e.printStackTrace();
 				return error;
 			}
 		}
 	}
 	
 	public static String friendApprove(Connection conn, String requester, String requestee) {
 		String success = "print "+requester+" has been added as your friend.;";
 		String error = "print Database error occurred while friending " + 
 				requester + ". Please try again or contact the System Admin.;";
 		try {
 			conn.setAutoCommit(false);
 		} catch (SQLException e) {
 			if (DEBUG) System.err.println("friendApprove: turning off auto commit failed.");
 			return error;
 		}
 		
 		int deleteStatus = DatabaseAdmin.deleteFriendRequest(conn, requester, requestee);
 		int addStatus = DatabaseAdmin.addFriend(conn, requester, requestee);
 		
 		if (deleteStatus != 1 || addStatus != 1) {
 			DBManager.rollback(conn);
 			DBManager.trueAutoCommit(conn);
 			if (DEBUG) System.err.printf("friendApprove: DB operations failed. " +
 					"deleteStatus: %d, addStatus: %d\n", deleteStatus, addStatus);
 			return error;
 		} else {
 			try {
 				conn.commit();
 				DBManager.trueAutoCommit(conn);
 				return success;
 			} catch (SQLException e) {
 				DBManager.trueAutoCommit(conn);
 				if (DEBUG) e.printStackTrace();
 				return error;
 			}
 		}
 	}
 
 	public static String regRemove(Connection conn, String username) {
 		String success = "print "+username+" has been deleted from the system.;";
 		String error = "print Database error occurred while removing registration for " + 
 				username + ". Please try again or contact the System Admin.;";
 		int status = DatabaseAdmin.deleteFromReg(conn, username);
 		if (status == 1) {
 			return success;
 		} else {
 			return error;
 		}
 	}
 	
 	public static String friendReqRemove(Connection conn, String requester, String requestee) {
 		String success = "print Friend request from " + requester + " has been deleted.;";
 		String error = "print Database error occurred while removing friend request from " + 
 				requester + ". Please try again or contact the System Admin.;";
 		int status = DatabaseAdmin.deleteFriendRequest(conn, requester, requestee);
 		if (status == 1) {
 			return success;
 		} else {
 			return error;
 		}
 	}
 
 	public static String displayFriendableUsers(Connection conn, String username, String prefix, List<String[]> friendableUsers) {
 		String command;
 		if (prefix == "") {
 			command = "print Users in the system:;";
 		} else {
 			command = "print Usernames starting with '" + prefix + "';";
 		}
 		for (String[] userInfo : friendableUsers) {
 			prefix = prefix.toLowerCase();
 			if (userInfo[0].toLowerCase().startsWith(prefix)) {
 				command += "print " + userInfo[0] + " (" + userInfo[1]
 						+ ");";
 			}
 		}
 		command += "print ;print Type the name of the user you wish to friend:;"
 				+ "askForInput";
 		return command;
 	}
 	
 	public static String displayGroupList(Connection conn, Map<Integer, String> groupList, String newUser) {
 		Iterator<Entry<Integer, String>> it = groupList.entrySet().iterator();
 		String list = "";
 		while (it.hasNext()) {
 	        Map.Entry<Integer, String> pairs = (Map.Entry<Integer, String>)it.next();
 	        list += "print " + pairs.getKey() + " " + pairs.getValue() + ";";
 	    }
 		return "print Choose a cappella group for " + newUser
 				+ " by entering the group number:;" + list + "askForInput;";
 	}
 	
 	public static String insertFriendRequest(Connection conn, String requestee, String requester) {
 		String command = "";
 		int status = DatabaseAdmin.insertFriendRequest(conn, requestee, requester);
 		if (status == 1) {
 			command = "print Friend request sent to " + requestee + ".;";
 		} else {
 			command = "print Database Error while sending friend request. Please try again or contact the System Admin.;";
 		}
 		return command;
 	}
 }
 
 
 
 
 
 
 
