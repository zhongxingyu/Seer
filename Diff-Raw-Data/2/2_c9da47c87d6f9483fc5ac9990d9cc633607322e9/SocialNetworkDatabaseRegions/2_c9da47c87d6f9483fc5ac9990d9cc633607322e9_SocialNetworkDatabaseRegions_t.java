 package database;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class SocialNetworkDatabaseRegions {
 	//private static int numPostsPerBoard = 2;
 	private static String specialStrPostable = "*";
 	
 	/**
 	 * Determine whether the region exists in this board.
 	 * ASSUMES THE BOARD EXISTS.
 	 */
 	public static Boolean regionExists(Connection conn, String boardName, String regionName) {
 		String getRegion = "SELECT * FROM " + boardName + ".regions WHERE rname = ?";
 		PreparedStatement pstmt = null;
 		ResultSet regionResult = null;
 		Boolean regionExists = null;
 		try {
 			pstmt = conn.prepareStatement(getRegion);
 			pstmt.setString(1, regionName);
 			regionResult = pstmt.executeQuery();
 			regionExists = new Boolean(regionResult.next());
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		finally {
 			DBManager.closePreparedStatement(pstmt);
 			DBManager.closeResultSet(regionResult);
 		}
 		return regionExists;
 	}
 	
 	/**
 	 * Returns whether the user is authorized to go to this region
 	 * Equivalent checking as in GetRegionList
 	 * For Admins: Must be within the "admin" list of the board (admins can go to any region)
 	 * For Users: Must be within the "RegionPrivileges" list of the board for this region.
 	 * Assumes the board and region exist, and that user is already authorized to be in the board.
 	 * Board =/= 'freeforall'
 	 */
 	public static Boolean authorizedGoToRegion(Connection conn, String username, String boardName, String regionName) {
 		PreparedStatement regionPstmt = null;
 		String fetchPrivMember = "SELECT privilege FROM " +
 		boardName + ".regionprivileges " +
 		"WHERE username = ? AND rname = ?";
 
 		String fetchPrivAdmin = "SELECT * FROM main.boardadmins WHERE username = ? AND bname = ?";
 		
 		ResultSet result = null;
 		
 		Boolean authorized = null;
 		try {
 			String role = DatabaseAdmin.getUserRole(conn, username);
			if (role.equals("member")) {
 				regionPstmt = conn.prepareStatement(fetchPrivMember);
 				regionPstmt.setString(2, regionName);
 			}
 			else if (!role.equals("")) { //user is an admin
 				regionPstmt = conn.prepareStatement(fetchPrivAdmin);
 				regionPstmt.setString(2, boardName);
 			}
 			else { //error occurred while acquiring role
 				return authorized; // null
 			}
 			regionPstmt.setString(1, username);
 			result = regionPstmt.executeQuery();
 			authorized = new Boolean(result.next()); //if theres a valid entry, you are authorized!
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		finally {
 			DBManager.closePreparedStatement(regionPstmt);
 			DBManager.closeResultSet(result);
 		}
 		return authorized;
 	}
 	
 	/**
 	 * Creates a region under the given board with the given region name.
 	 * ASSUMES that the boardName is valid.
 	 */
 	public static String createRegion(Connection conn, String username, String boardName, String regionName) {
 		/** AUTHORIZATION CHECK **/
 		/** User must be an admin, and more specifically, a board admin **/
 		Boolean isAd = DatabaseAdmin.isAdmin(conn, username);
 		Boolean isBoardAd = SocialNetworkDatabaseBoards.isBoardAdmin(conn, username, boardName);
 		if (isAd == null || isBoardAd == null) {
 			return "print Error: Database error while creating the region (Verifying Authorizations). Contact the admin.";
 		}
 		if (!isAd || !isBoardAd) {
 			return "print Error: User is not authorized to create this region (Make this msg more ambig l8r!)";
 		}
 		PreparedStatement regionPstmt = null;
 		String createRegion = "INSERT INTO " + boardName + ".regions VALUES (?)";
 		boolean success = false;
 		String sqlexmsg = "";
 		try {
 			regionPstmt = conn.prepareStatement(createRegion);
 			regionPstmt.setString(1, regionName);
 			success =  (regionPstmt.executeUpdate() == 1);
 		}
 		catch (SQLException e) {
 			if (e.getErrorCode() == DBManager.DUPLICATE_KEY_CODE) {
 				sqlexmsg = "print A region in this board already exists with that name. Try a different name";
 			}
 			else {
 				e.printStackTrace();
 				sqlexmsg = "print Error: Database error while creating the region. Contact the admin.";
 			}
 		}
 		finally {
 			DBManager.closePreparedStatement(regionPstmt);
 		}
 		if (success) {
 			return "print Region \"" + regionName + "\" successfully created.;print Don't forget to add users to the region privileges list!";
 		}
 		else {
 			return sqlexmsg;
 		}
 	}
 	
 	/**
 	 * Gets a list of regions that the user has access to.
 	 * If the user is an admin, the user can see all regions.
 	 * Also returns, with each region, the most recently posted posts.
 	 * Assumes boardName is not null and is a valid board, and that the user
 	 * has already been authorized to access this board.
 	 * */
 	public static String getRegionListRecentPosts(Connection conn, String username, String boardName){
 		String regionsAndPosts = "print Regions:;";
 		
 		PreparedStatement regionPstmt = null;
 		String fetchRegionsMember = "SELECT rname, privilege FROM " +
 				boardName + ".regionprivileges " +
 				"WHERE username = ?";
 		
 		Statement regionStmt = null;
 		String fetchRegionsAdmin = "SELECT * FROM " + boardName + ".regions";
 		
 		PreparedStatement recentPostsPstmt = null;
 		String fetchRecentPosts = "SELECT rname, pid, P.postedBy, P.datePosted, MAX(P.dateLastUpdated), MAX(R.dateReplied)" +
 				"FROM " + boardName + ".posts AS P LEFT OUTER JOIN " +
 				boardName + ".replies AS R USING (rname, pid) " +
 				"WHERE rname = ? GROUP BY pid ORDER BY P.dateLastUpdated DESC";
 		ResultSet regionsResults = null;
 		ResultSet recentPostsResults = null;
 		
 		boolean sqlex = false;
 		try {
 			String role = DatabaseAdmin.getUserRole(conn, username);
 			if (role.equals("member")) {
 				regionPstmt = conn.prepareStatement(fetchRegionsMember);
 				regionPstmt.setString(1, username);
 				regionsResults = regionPstmt.executeQuery();
 			}
 			else if (!role.equals("")) { //user is an admin
 				regionStmt = conn.createStatement();
 				regionsResults = regionStmt.executeQuery(fetchRegionsAdmin);
 			}
 			else { //error occurred while acquiring role
 				return "print Error: Database Error while querying viewable regions. Contact an admin.;";
 			}
 			recentPostsPstmt = conn.prepareStatement(fetchRecentPosts);
 			while (regionsResults.next()) {
 				/*For each region, fetch the two most recent posts*/
 				if (role.equals("member")) {
 					regionsAndPosts += "print \t" + 
 						(regionsResults.getString("privilege").equals("viewpost") ? specialStrPostable : "") +
 						regionsResults.getString("rname") + ";";
 				}
 				else {
 					regionsAndPosts += "print \t" + specialStrPostable + regionsResults.getString("rname") + ";";
 				}
 				recentPostsPstmt.setString(1, regionsResults.getString("rname"));
 				recentPostsResults = recentPostsPstmt.executeQuery();
 				if (recentPostsResults.next()) {
 					if (recentPostsResults.getTimestamp("P.datePosted") != null) {
 						regionsAndPosts += "print \t\tMost Recent Activity | Post#" + recentPostsResults.getInt("pid") +
 						"[" + recentPostsResults.getString("P.postedBy") + "];";
 						if (recentPostsResults.getTimestamp("MAX(R.dateReplied)") != null) {
 							regionsAndPosts += "print \t\t   " +
 							"Most Recent Reply at " +
 							recentPostsResults.getTimestamp("MAX(R.dateReplied)").toString() + ";";
 						}
 					}
 					else { //LEFT INNER JOIN can return a null row as an answer.
 						regionsAndPosts += "print \t\tNo Posts for this Region;";
 					}
 				}
 				else {
 					regionsAndPosts += "print \t\tNo Posts for this Region;";
 				}
 			}
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			sqlex = true;
 		}
 		finally {
 			DBManager.closePreparedStatement(regionPstmt);
 			DBManager.closeStatement(regionStmt);
 			DBManager.closePreparedStatement(recentPostsPstmt);
 			DBManager.closeResultSet(regionsResults);
 			DBManager.closeResultSet(recentPostsResults);
 		}
 		if (regionsAndPosts.equals("print Regions:;") && !sqlex) { //boardName assumed to be valid.
 			return "print No Regions for this Board";
 		}
 		else if (sqlex) {
 			return "print Error: Database Error while querying viewable regions. Contact an admin.;";
 		}
 		else return regionsAndPosts;
 	}
 	
 	public static boolean isRegionManager(Connection conn, String username, String board, String region) {
 		boolean isManager = false;
 		String query = "SELECT managedby FROM " + board + ".regions " +
 				"WHERE rname = ?";
 		PreparedStatement pstmt = null;
 		ResultSet result = null;
 		try {
 			pstmt = conn.prepareStatement(query);
 			pstmt.setString(1, region);
 			result = pstmt.executeQuery();
 			if (result.next()) {
 				isManager = username.equals(result.getString("managedby"));
 			}
 		} catch (SQLException e) {
 			isManager = false;
 		} finally {
 			DBManager.closeResultSet(result);
 			DBManager.closePreparedStatement(pstmt);
 		}
 		return isManager;
 	}
 	
 	public static int addParticipant(Connection conn, String board, String region, 
 			String user, String priv, String grantedBy) {
 		int status = 0;
 		String query = "INSERT INTO " + board + ".regionprivileges " +
 				"(rname, username, privilege, grantedBy) " +
 				"VALUES (?, ?, ?, ?)";
 		PreparedStatement pstmt = null;
 		try {
 			pstmt = conn.prepareStatement(query);
 			pstmt.setString(1, region);
 			pstmt.setString(2, user);
 			pstmt.setString(3, priv);
 			pstmt.setString(4, grantedBy);
 			status = pstmt.executeUpdate();
 		} catch (SQLException e) {
 			status = 0;
 		} finally {
 			DBManager.closePreparedStatement(pstmt);
 		}
 		return status;
 	}
 }
