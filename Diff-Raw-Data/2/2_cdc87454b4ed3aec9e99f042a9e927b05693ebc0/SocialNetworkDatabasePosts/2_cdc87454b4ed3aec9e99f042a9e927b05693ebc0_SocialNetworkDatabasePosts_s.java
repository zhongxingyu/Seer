 package database;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class SocialNetworkDatabasePosts {
 	private static String specialStrPostable = "*";
 	private static String specialStrCreatedPost = "**";
 	
 	/**
 	 * Verifies whether a post exists in the given board (and region).
 	 */
 	public static Boolean postExists(Connection conn, String boardName, String regionName, int postNum) {
 		PreparedStatement pstmt = null;
 		ResultSet postResult = null;
 		String getPost = "";
 		if (boardName.equals("freeforall")) {
 			getPost = "SELECT * FROM freeforall.posts " +
 			"WHERE pid = ?";
 		}
 		else {
 			getPost = "SELECT * FROM " + boardName + ".posts " +
 					"WHERE pid = ? AND rname = ?";
 		}
 		
 		Boolean postExists = null;
 		try {
 			pstmt = conn.prepareStatement(getPost);
 			pstmt.setInt(1, postNum);
 			if (!boardName.equals("freeforall")) {
 				pstmt.setString(2, regionName);
 			}
 			postResult = pstmt.executeQuery();
 			postExists = new Boolean(postResult.next());
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println(e.getSQLState());
 		}
 		finally {
 			DBManager.closePreparedStatement(pstmt);
 		}
 		return postExists;
 	}
 	
 	/**
 	 * Returns the list of posts within the Free For All board that the
 	 * specified user can see.
 	 * 
 	 * A User can see a post if they are:
 	 *   - the creator of the post (username == postedBy)
 	 *   - granted view privilege in PostsPrivileges
 	 * 
 	 * Different from a regular board because you must check each post
 	 * one by one to ensure that the user has the privilege for it.
 	 */
 	public static String getPostListFreeForAll(Connection conn, String username) {
 		String posts = "print Posts:;";
 		
 		/*Retrieves all posts, joined with their most recent reply*/
 		Statement getPosts = null;
 		String getPostsFreeForAll = "SELECT pid, P.postedBy, P.datePosted, R.repliedBy, MAX(R.dateReplied) " +
 			"FROM freeforall.posts AS P LEFT OUTER JOIN " + 
 			"freeforall.replies as R USING (pid) " +
 			"GROUP BY pid ORDER BY R.dateReplied DESC, P.datePosted DESC";
 		ResultSet postsResults = null;
 		
 		/*Retrieves the privilege for a given post and user*/
 		PreparedStatement getPrivs = null;
 		String getPostPrivileges = "SELECT privilege " +
 				"FROM freeforall.postprivileges " +
 				"WHERE pid = ? AND username = ?";
 		ResultSet privsResult = null;
 		
 		boolean sqlex = false;
 		try {
 			getPrivs = conn.prepareStatement(getPostPrivileges);
 			getPosts = conn.createStatement();
 			postsResults = getPosts.executeQuery(getPostsFreeForAll);
 			
 			int pid;
 			String postedBy;
 			while (postsResults.next()) {
 				pid = postsResults.getInt("pid");
 				postedBy = postsResults.getString("P.postedBy");
 				if (!postedBy.equals(username)) {
 					getPrivs.setInt(1, pid);
 					getPrivs.setString(2, username);
 					privsResult = getPrivs.executeQuery();
 					/*Only expect one result set*/
 					if (privsResult.next()) { //user has view or viewpost priv
 						posts += "print \t" + 
 						(privsResult.getString("privilege").equals("viewpost")? specialStrPostable : "") +
 						"Post#" + pid + "[" + postsResults.getString("P.postedBy") + "];";
 						if (postsResults.getTimestamp("MAX(R.dateReplied)") != null) {
 							posts += "print \t" +
 							"Most Recent Reply: [" + postsResults.getString("R.repliedBy") + "]" +
 							postsResults.getTimestamp("MAX(R.dateReplied)") + ";";
 						}
 					}
 				}
 				else { //the user is the creator of the post
 					posts += "print \t" + specialStrCreatedPost +
 					"Post#" + pid + "[" + postsResults.getString("P.postedBy") + "];";
 					if (postsResults.getTimestamp("MAX(R.dateReplied)") != null) {
 						posts += "print \t" +
 						"Most Recent Reply: [" + postsResults.getString("R.repliedBy") + "]" +
 						postsResults.getTimestamp("MAX(R.dateReplied)") + ";";
 					}
 				}
 			}
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			sqlex = true;
 		}
 		finally {
 			DBManager.closeStatement(getPosts);
 			DBManager.closeResultSet(postsResults);
 			DBManager.closePreparedStatement(getPrivs);
 			DBManager.closeResultSet(privsResult);
 		}
 		if (posts.equals("print Posts:;") && !sqlex) {
 			return "print No posts for this board.";
 		}
 		else if (sqlex) {
 			return "print Error: Database Error while querying viewable posts. Contact an admin.";
 		}
 		else {
 			return posts;
 		}
 	}
 	
 	/** 
 	 * Gets the post list for the given region.
 	 * The board is assumed not to be the free for all board.
 	 * Assumes all parameters are valid (boardName and regionName especially)
 	 * TODO (author) ensure that the user can view the posts for this region
 	 */
 	public static String getPostList(Connection conn, String username, String boardName, String regionName) {
 		PreparedStatement pstmt = null;
 		String posts = "print Posts:;";
 		String getPosts = "SELECT rname, pid, P.postedBy, P.datePosted, R.repliedBy, MAX(R.dateReplied) " +
 			"FROM " + boardName +  ".posts AS P LEFT OUTER JOIN " + 
 			boardName + ".replies as R USING (rname, pid) " +
 			"WHERE rname = ? " +
 			"GROUP BY pid ORDER BY R.dateReplied DESC, P.datePosted DESC ";
 		ResultSet postsResults = null;
 		boolean sqlex = false;
 		try {
 			pstmt = conn.prepareStatement(getPosts);
 			pstmt.setString(1, regionName);
 			postsResults = pstmt.executeQuery();
 			while (postsResults.next()) {
 				posts += "print \tPost#" + postsResults.getInt("pid") + 
 				"[" + postsResults.getString("P.postedBy") + "]; print \t" +
 				"Most Recent Reply: [" + postsResults.getString("R.repliedBy") + "] " +
 				postsResults.getTimestamp("MAX(R.dateReplied)") + ";";
 			}
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			sqlex = true;
 		}
 		finally {
 			DBManager.closePreparedStatement(pstmt);
 			DBManager.closeResultSet(postsResults);
 		}
 		if (posts.equals("print Posts:;") && !sqlex) { //board and region assumed to be valid
 			return "print No Posts for this Region";
 		}
 		else if (sqlex) {
 			return "print Error: Database Error while querying viewable posts. Contact an admin.";
 		}
 		else return posts;
 	}
 	
 	//TODO need a way to add post privileges for the free for all board.
 	public static String createPostFreeForAll(Connection conn, String username, String content) {
 		return createPost(conn, username, content, "freeforall", null);
 	}
 	
 	/**
 	 * Inserts the post into the database, then tries its best to
 	 * return the pid that contains the post.
 	 * Assumes the board and region are correct (unless the board is freeforall)
 	 * Does NOT do "Post Privileges" processing.
 	 * 
 	 * TODO (author) For regular boards and regions, ensure the user can post under it
 	 */
 	public static String createPost(Connection conn, String username, String content, 
 			String boardName, String regionName) {
 		PreparedStatement createPstmt = null;
 		String createPost = "";
 		
 		/*Have to retrieve the pid that is generated for the post*/
 		PreparedStatement getPstmt = null;
 		String getPost = "";
 		ResultSet getResult = null;
 		
 		if (boardName.equals("freeforall")) {
 			createPost = "INSERT INTO freeforall.posts " +
 					"VALUES (null, ?, NOW(), ?)";
 			getPost = "SELECT pid, MAX(datePosted) FROM freeforall.posts " +
 					"WHERE postedBy = ? AND content = ?";
 		}
 		else {
 			createPost = "INSERT INTO " + boardName + ".posts " +
					"VALUES (?, null, ?. NOW(), ?)";
 			getPost = "SELECT pid, MAX(datePosted) FROM " + boardName + ".posts " +
 					"WHERE rname = ? postedBy = ? AND content = ?";
 		}
 		
 		boolean sqlex = false;
 		boolean success = false;
 		try {
 			createPstmt = conn.prepareStatement(createPost);
 			if (boardName.equals("freeforall")) {
 				createPstmt.setString(1, username);
 				createPstmt.setString(2, content);
 			}
 			else {
 				createPstmt.setString(1, regionName);
 				createPstmt.setString(2, username);
 				createPstmt.setString(3, content);
 			}
 			success = (createPstmt.executeUpdate() == 1);
 			
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			sqlex = true;
 		}
 		finally {
 			DBManager.closePreparedStatement(createPstmt);
 		}
 		if (sqlex) {
 			return "print Error: Database error while inserting the post. Contact an admin.";
 		}
 		else if (success) {
 			/*Try to retrieve the pid for the user to reference*/
 			Integer pid = null;
 			try {
 				getPstmt = conn.prepareStatement(getPost);
 				if (boardName.equals("freeforall")) {
 					getPstmt.setString(1, username);
 					getPstmt.setString(2, content);
 				}
 				else {
 					getPstmt.setString(1, regionName);
 					getPstmt.setString(2, username);
 					getPstmt.setString(3, content);
 				}
 				getResult = getPstmt.executeQuery();
 				if (getResult.next()) { //There should be at most one result... just inserted!
 					pid = new Integer(getResult.getInt("pid"));
 				}
 			}
 			catch (SQLException e) {
 				e.printStackTrace();
 				sqlex = true;
 			}
 			finally {
 				DBManager.closePreparedStatement(getPstmt);
 				DBManager.closeResultSet(getResult);
 			}
 			if (pid == null || sqlex) {
 				return "print Post successfully added (post num cannot be retrieved).;" +
 						"print Don't forget to give people permission to view/reply to it!";
 			}
 			else {
 				return "print Post#" + pid + " successfully added.;" +
 						"print Don't forget to give people permission to view/reply to it!";
 			}
 		}
 		else { //not successful
 			return "print Error: Post could not be uploaded. If this problem persists, contact an admin.";
 		}
 	}
 	
 	public static String createReplyFreeForAll(Connection conn, String username, String content, 
 			int postNum) {
 		return createReply(conn, username, content, "freeforall", null, postNum);
 	}
 	
 	/**
 	 * Inserts the reply for the given post.
 	 * Assumes the board, the region, and the post are valid.
 	 */
 	public static String createReply(Connection conn, String username, String content, 
 			String boardName, String regionName, int postNum) {
 		PreparedStatement createPstmt = null;
 		String createReply = ""; 
 		if (boardName.equals("freeforall")) {
 			createReply = "INSERT INTO freeforall.replies " +
 			"VALUES (?, null, ?, NOW(), ?)";
 		}
 		else {
 			createReply = "INSERT INTO " + boardName + ".replies " +
 			"VALUES (?, ?, null, ?, NOW(), ?)";
 		}
 		boolean success = false;
 		boolean sqlex = false;
 		try {
 			createPstmt = conn.prepareStatement(createReply);
 			if (boardName.equals("freeforall")) {
 				createPstmt.setInt(1, postNum);
 				createPstmt.setString(2, username);
 				createPstmt.setString(3, content);
 			}
 			else {
 				createPstmt.setString(1 , regionName);
 				createPstmt.setInt(2, postNum);
 				createPstmt.setString(3, username);
 				createPstmt.setString(4, content);
 			}
 			success = (createPstmt.executeUpdate() == 1);
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println(e.getSQLState());
 			sqlex = true;
 		}
 		finally {
 			DBManager.closePreparedStatement(createPstmt);
 		}
 		if (!success || sqlex) {
 			return "print Error: Database error while inserting reply. Contact an admin";
 		}
 		else if (success) {
 			return "print Reply successfully added. Refresh the post to view";
 		}
 		else {
 			return "print Error: Reply could not be uploaded. If this problem persists, contact an admin";
 		}
 	}
 	
 	/** Gets a post from the free for all board designated
 	 * by the post number.
 	 * postNum is assumed to be an accurate post number.
 	 */
 	//TODO (author) ensure that the user has access to this post.
 	public static String getPostFreeForAll(Connection conn, String username, int postNum) {
 		return getPost(conn, username, "freeforall", "", postNum);
 	}
 	
 	/** Gets a post from the designated board and region
 	 *  with the given post number.
 	 *  ASSUMES that the board, region, and post are all valid.
 	 */
 	//TODO (author) ensure that the user has access to the encapsulating region.
 	public static String getPost(Connection conn, String username, String boardName, 
 			String regionName, int postNum) {
 		String getOriginalPost = "";
 		String getReplies = "";
 		String postAndReplies = "";
 		
 		/*No joining of results because of redundancy of data returned*/
 		if (boardName.equals("freeforall")) {
 			getOriginalPost = "SELECT * FROM freeforall.posts " +
 			"WHERE pid = ?";
 			getReplies = "SELECT * FROM freeforall.replies " +
 			"WHERE pid = ? ORDER BY dateReplied ASC";
 		}
 		else {
 			getOriginalPost = "SELECT * FROM " + boardName + ".posts " +
 			"WHERE pid = ? AND rname = ?";
 			getReplies = "SELECT * FROM " + boardName + ".replies " +
 			"WHERE pid = ? AND rname = ? ORDER BY dateReplied";
 		}
 		
 		PreparedStatement originalPost = null;
 		ResultSet postResult = null;
 		
 		PreparedStatement replies = null;
 		ResultSet repliesResult = null;
 		
 		boolean sqlex = false;
 		try {
 			originalPost = conn.prepareStatement(getOriginalPost);
 			replies = conn.prepareStatement(getReplies);
 			originalPost.setInt(1, postNum);
 			replies.setInt(1, postNum);
 			if (!boardName.equals("freeforall")) {
 				originalPost.setString(2, regionName);
 				replies.setString(2, regionName);
 			}
 			
 			postResult = originalPost.executeQuery();
 			if (postResult.next()) { /*Only expect one post result*/
 				postAndReplies += 
 					"print ----- Post# " + postNum + "[" + postResult.getString("postedBy") + "]----- " +
 					postResult.getTimestamp("datePosted").toString() + ";print \t" +
 					postResult.getString("content") + ";";
 				
 				repliesResult = replies.executeQuery();
 				while (repliesResult.next()) { //Print out all replies
 					postAndReplies += "print ----- Reply[" + repliesResult.getString("repliedBy") + "] ----- " +
 					repliesResult.getTimestamp("dateReplied").toString() + ";print \t" +
 					repliesResult.getString("content") + ";";
 				}
 			}
 			// if there's no postResult, the post DNE.
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			sqlex = true;
 		}
 		if (postAndReplies.equals("") && !sqlex) {
 			return "print Error: Post does not exist. Refresh. If the problem persists, contact an admin.";
 		}
 		else if (sqlex) {
 			return "print Error: Database error while querying post and replies. Contact an admin.";
 		}
 		else return postAndReplies;
 	}
 }
