 package server;
 
 import java.sql.Connection;
 
 import database.SocialNetworkDatabasePosts;
 import database.DBManager;
 
 /**
  * Look at corresponding SQL.java file for specs.
  * These functions are merely wrappers, containing
  * some server logic.
  * @author kchen
  *
  */
 public class SocialNetworkPosts {
 	
 	public static Boolean postExists(String boardName, String regionName, int postNum) {
 		Connection dbconn = DBManager.getConnection();
 		Boolean exists = SocialNetworkDatabasePosts.postExists(dbconn, boardName, regionName, postNum);
 		DBManager.closeConnection(dbconn);
 		return exists;
 	}
 	
 	public static String createReply(String username, String content, 
 			String boardName, String regionName, int postNum) {
 		Connection dbconn = DBManager.getConnection();
 		String bname = boardName.trim().toLowerCase();
 		String rname = regionName.trim().toLowerCase();
 		if (bname.equals("freeforall")) {
 			Boolean postExists = postExists("freeforall", null, postNum);
 			if (postExists == null) {
 				return "print Error: Database error while verifying existence of post. " +
 				"If the problem persists, contact an admin.";
 			}
 			else if (postExists.booleanValue()) {
 				String msg = SocialNetworkDatabasePosts.createReplyFreeForAll(dbconn, username, content, postNum);
 				DBManager.closeConnection(dbconn);
 				return msg;
 			}
 			else {
 				return "print Error: Post does not exist. Refresh. " +
 						"If the problem persists, contact an admin.";
 			}
 		}
 		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
 		if (boardExists == null) {
 			return "print Error: Database error while verifying existence of board. " +
 					"If the problem persists, contact an admin.";
 		}
 		else if (boardExists.booleanValue()) {
 			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
 			if (regionExists == null) {
 				return "print Error: Database error while verifying existence of region. " +
 						"If the problem persists, contact an admin.";
 			}
 			else if (regionExists.booleanValue()) {
 				Boolean postExists = postExists(bname, rname, postNum);
 				if (postExists == null) {
 					return "print Error: Database error while verifying existence of post. " +
 					"If the problem persists, contact an admin.";
 				}
 				else if (postExists.booleanValue()) {
 					String msg = SocialNetworkDatabasePosts.createReply(dbconn, username, content,
 							bname, rname, postNum);
 					DBManager.closeConnection(dbconn);
 					return msg;
 				}
 				else {
 					return "print Error: Post does not exist. Refresh. " +
 					"If the problem persists, contact an admin.";
 				}
 			}
 			else {
 				return "print Error: Encapsulating Region does not exist. Refresh. " +
 				"If the problem persists, contact an admin.";
 			}
 		}
 		else {
 			return "print Error: Encapsulating Board does not exist. Refresh. " +
 			"If the problem persists, contact an admin.";
 		}
 	}
 	
 	public static String viewPost(String username, String boardName, 
 			String regionName, int postNum) {
 		Connection dbconn = DBManager.getConnection();
 		String bname = boardName.trim().toLowerCase();
		String rname = regionName.trim().toLowerCase();
 		if (bname.equals("freeforall")) {
 			Boolean postExists = postExists("freeforall", null, postNum);
 			if (postExists == null) {
 				return "print Error: Database error while verifying existence of post. " +
 				"If the problem persists, contact an admin.";
 			}
 			else if (postExists.booleanValue()) {
 				String msg = SocialNetworkDatabasePosts.getPostFreeForAll(dbconn, username, postNum);
 				DBManager.closeConnection(dbconn);
 				return msg;
 			}
 			else {
 				return "print Error: Post does not exist. Refresh. " +
 				"If the problem persists, contact an admin.";
 			}
 		}
 		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
 		if (boardExists == null) {
 			return "print Error: Database error while verifying existence of board. " +
 					"If the problem persists, contact an admin.";
 		}
 		else if (boardExists.booleanValue()) {
 			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
 			if (regionExists == null) {
 				return "print Error: Database error while verifying existence of region. " +
 						"If the problem persists, contact an admin.";
 			}
 			else if (regionExists.booleanValue()) {
 				Boolean postExists = postExists(bname, rname, postNum);
 				if (postExists == null) {
 					return "print Error: Database error while verifying existence of post. " +
 					"If the problem persists, contact an admin.";
 				}
 				else if (postExists.booleanValue()) {
 					String msg = SocialNetworkDatabasePosts.getPost(dbconn, username,
 							bname, rname, postNum);
 					DBManager.closeConnection(dbconn);
 					return msg;
 				}
 				else {
 					return "print Error: Post does not exist. Refresh. " +
 					"If the problem persists, contact an admin.";
 				}
 			}
 			else {
 				return "print Error: Encapsulating Region does not exist. Refresh. " +
 				"If the problem persists, contact an admin.";
 			}
 		}
 		else {
 			return "print Error: Encapsulating Board does not exist. Refresh. " +
 			"If the problem persists, contact an admin.";
 		}
 	}
 	
 	public static String createPost(String username, String content, 
 			String boardName, String regionName) {
 		String bname = boardName.trim().toLowerCase();
 		Connection dbconn = DBManager.getConnection();
 		if (bname.equals("freeforall")) { //regionName might be null
 			String msg = SocialNetworkDatabasePosts.createPostFreeForAll(dbconn, username, content);
 			DBManager.closeConnection(dbconn);
 			return msg;
 		}
 		//regionName should not be null
 		String rname = regionName.trim().toLowerCase();
 		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
 		if (boardExists == null) {
 			return "print Error: Database error while verifying existence of board. " +
 					"If the problem persists, contact an admin.";
 		}
 		else if (boardExists.booleanValue()) {
 			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
 			if (regionExists == null) {
 				return "print Error: Database error while verifying existence of region. " +
 						"If the problem persists, contact an admin.";
 			}
 			else if (regionExists.booleanValue()) {
 				String msg = SocialNetworkDatabasePosts.createPost(dbconn, username, content,
 						bname, rname);
 				DBManager.closeConnection(dbconn);
 				return msg;
 			}
 			else {
 				return "print Error: Encapsulating Region does not exist. Refresh. " +
 				"If the problem persists, contact an admin.";
 			}
 		}
 		else {
 			return "print Error: Encapsulating Board does not exist. Refresh. " +
 			"If the problem persists, contact an admin.";
 		}
 	}
 	
 	public static String viewPostList(String username, String boardName, String regionName) {
 		if (boardName == null || (!("freeforall").equals(boardName) && regionName == null)) {
 			return "Invalid Call to Function";
 		}
 		String bname = boardName.trim().toLowerCase();
 		Connection dbconn = DBManager.getConnection();
 		if (bname.equals("freeforall")) { //regionName might be null
 			String msg = SocialNetworkDatabasePosts.getPostListFreeForAll(dbconn, username);
 			DBManager.closeConnection(dbconn);
 			return msg;
 		}
 		//regionName is NOT null
 		String rname = regionName.trim().toLowerCase();
 		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
 		if (boardExists == null) {
 			return "print Error: Database error while verifying existence of board. " +
 					"If the problem persists, contact an admin.";
 		}
 		else if (boardExists.booleanValue()) {
 			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
 			if (regionExists == null) {
 				return "print Error: Database error while verifying existence of region. " +
 						"If the problem persists, contact an admin.";
 			}
 			else if (regionExists.booleanValue()) {
 				String msg = SocialNetworkDatabasePosts.getPostList(dbconn, username,
 						bname, rname);
 				DBManager.closeConnection(dbconn);
 				return msg;
 			}
 			else {
 				return "print Error: Encapsulating Region does not exist. Refresh. " +
 				"If the problem persists, contact an admin.";
 			}
 		}
 		else {
 			return "print Error: Encapsulating Board does not exist. Refresh. " +
 			"If the problem persists, contact an admin.";
 		}
 	}
 }
