 package models;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * The thread model
  * 
  * Represents a thread record from the database
  * 
  * @author David Knezic <davidknezic@gmail.com>
  */
 public class ThreadModel {
 
 	/**
 	 * The threadId
 	 */
 	private int threadId;
 
 	/**
 	 * The parent boardId
 	 */
 	private int boardId;
 
 	/**
 	 * The userId
 	 */
 	private int userId;
 
 	/**
 	 * The thread title
 	 */
 	private String title;
 
 	/**
 	 * The thread content
 	 */
 	private String content;
 
 	/**
 	 * The creation date
 	 */
 	private Timestamp createdOn;
 
 	/**
 	 * Initialize an empty thread
 	 */
 	public ThreadModel() {
 		// Indicate that this object is not yet saved
 		this.threadId = 0;
 	}
 
 	/**
 	 * Initialize the given thread
 	 * 
 	 * @param threadId
 	 * @throws Throwable
 	 */
 	public ThreadModel(int threadId) throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT boardId, userId, title, content, createdOn FROM thread WHERE threadId = ?");
 		stmt.setInt(1, threadId);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Assign the retrieved information
 			this.threadId = threadId;
 			this.boardId = res.getInt(1);
 			this.userId = res.getInt(2);
 			this.title = res.getString(3);
 			this.content = res.getString(4);
 			this.createdOn = res.getTimestamp(5);
 		} else {
 			// No thread with given id found
 			throw new Exception("Could not find thread");
 		}
 	}
 
 	/**
 	 * Either inserts a new record or updates an existing one depending on the
 	 * threadId
 	 * 
 	 * @throws Throwable
 	 */
 	public void save() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		PreparedStatement stmt;
 
 		if (this.threadId == 0) {
 			// Insert a new record
 			stmt = conn
 					.prepareStatement(
 							"INSERT INTO thread (boardId, userId, title, content, createdOn) VALUES (?, ?, ?, ?, NOW())",
 							Statement.RETURN_GENERATED_KEYS);
 		} else {
 			// Update record
 			stmt = conn
 					.prepareStatement(
 							"UPDATE thread SET boardId = ?, userId = ?, title = ?, content = ?, createdOn = ? WHERE threadId = ?",
 							Statement.RETURN_GENERATED_KEYS);
 			stmt.setTimestamp(5, this.createdOn);
 			stmt.setInt(6, this.threadId);
 		}
 
 		// Set global variables
 		stmt.setInt(1, this.boardId);
 		stmt.setInt(2, this.userId);
 		stmt.setString(3, this.title);
 		stmt.setString(4, this.content);
 
 		// Execute query
 		stmt.executeUpdate();
 
 		// Get the generated key if we made an insert
 		if (this.threadId == 0) {
 			ResultSet set = stmt.getGeneratedKeys();
 
 			if (set.next()) {
 				this.threadId = set.getInt(1);
 			}
 		}
 	}
 	
 	/**
 	 * Deletes the current row
 	 * 
 	 * @throws Throwable
 	 */
 	public void delete() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		PreparedStatement stmt;
 		
 		stmt = conn.prepareStatement("DELETE FROM thread WHERE threadId = ?");
 		stmt.setInt(1, this.threadId);
 		
 		stmt.executeUpdate();
 	}
 
 	/**
 	 * Get the threadId
 	 * 
 	 * @return int
 	 */
 	public int getThreadId() {
 		return threadId;
 	}
 
 	/**
 	 * Get the parent boardId
 	 * 
 	 * @return int
 	 */
 	public int getBoardId() {
		return this.boardId;
 	}
 
 	/**
 	 * Set the parent boardId
 	 * 
 	 * @param boardId
 	 */
 	public void setBoardId(int boardId) {
 		this.boardId = boardId;
 	}
 
 	/**
 	 * Get the userId
 	 * 
 	 * @return int
 	 */
 	public int getUserId() {
 		return userId;
 	}
 
 	/**
 	 * Set the userId
 	 * 
 	 * @param userId
 	 */
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	/**
 	 * Get the title
 	 * 
 	 * @return String
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * Set the title
 	 * 
 	 * @param title
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * Get the content
 	 * 
 	 * @return String
 	 */
 	public String getContent() {
 		return content;
 	}
 
 	/**
 	 * Set the content
 	 * 
 	 * @param content
 	 */
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	/**
 	 * Get the creation timestamp as a standard java Date
 	 * 
 	 * @return Date
 	 */
 	public Date getCreatedOn() {
 		return (Date) createdOn;
 	}
 
 	/**
 	 * Set the creation timestamp as a standard java Date
 	 * 
 	 * @param createdOn
 	 */
 	public void setCreatedOn(Date createdOn) {
 		this.createdOn = new Timestamp(createdOn.getTime());
 	}
 
 	/**
 	 * Get parent board
 	 * 
 	 * @return BoardModel
 	 * @throws Throwable
 	 */
 	public BoardModel getBoard() throws Throwable {
 		return new BoardModel(this.boardId);
 	}
 
 	/**
 	 * Get user
 	 * 
 	 * @return UserModel
 	 * @throws Throwable
 	 */
 	public UserModel getUser() throws Throwable {
 		return new UserModel(this.userId);
 	}
 
 	/**
 	 * Get dependent replies
 	 * 
 	 * @return ArrayList<ReplyModel>
 	 * @throws Throwable
 	 */
 	public ArrayList<ReplyModel> getReplies(int off, int max) throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 		ArrayList<ReplyModel> threads = new ArrayList<ReplyModel>();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT replyId FROM reply WHERE threadId = ? ORDER BY createdOn ASC LIMIT ?, ?");
 		stmt.setInt(1, this.threadId);
 		stmt.setInt(2, off);
 		stmt.setInt(3, max);
 		ResultSet res = stmt.executeQuery();
 
 		// For each reply
 		while (res.next()) {
 			// Add current reply object to array
 			threads.add(new ReplyModel(res.getInt(1)));
 		}
 
 		return threads;
 	}
 
 	/**
 	 * Get the last reply
 	 * 
 	 * @return ReplyModel
 	 * @throws Throwable
 	 */
 	public ReplyModel getLastReply() throws Throwable {
 		ArrayList<ReplyModel> replies = this.getReplies(0, 1);
 
 		if (replies.size() > 0) {
 			return replies.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get number of replies in thread
 	 * 
 	 * @return int
 	 * @throws Throwable
 	 */
 	public int getReplyCount() throws Throwable {
 		Connection conn = DBConnection.getInstance().getConnection();
 
 		PreparedStatement stmt = conn
 				.prepareStatement("SELECT COUNT(*) FROM reply WHERE threadId = ?");
 		stmt.setInt(1, this.threadId);
 		ResultSet res = stmt.executeQuery();
 
 		if (res.first()) {
 			// Return the reply count
 			return res.getInt(1);
 		} else {
 			// An error occured, return zero
 			return 0;
 		}
 	}
 
 }
