 package progAdmin.itemsToReview;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 import program.CurrentUser;
 //-----------------------------------------------------------------------------
 /**
  * 
  */
 public class ItemToReview {
 	/** the title of this ItemToReview object **/
 	String title;
 	/** the details associated with this ItemToReview object **/
 	String details;
 	/** whether or not this ItemToReview has been **/
 	boolean reviewed;
 	/** the name of the person who created this ItemToReview object **/
 	String creator;
 	/** the date this ItemToReview object was created **/
 	long dateCreated=System.currentTimeMillis();
 	/** the date this ItemToReview object was reviewed **/
 	long dateReviewed=0;
 	/** the unique ID associated with this item **/
 	Integer item_id=null;
 	/** the caneID of the person who created this <code>ItemToReview</code> **/
 	String createdBy;
 	/** the caneID of the person who reviewed this <code>ItemToReview</code> **/
 	String reviewedBy=null;
 //-----------------------------------------------------------------------------
 	public ItemToReview(){
 	}
 //-----------------------------------------------------------------------------
 	public ItemToReview(String createdBy){
 		this.createdBy = createdBy;
 	}
 //-----------------------------------------------------------------------------
 	public ItemToReview(String title, String details){
 		this.title=title;
 		this.details=details;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return title
 	 */
 	public String getTitle() {
 		return title;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param title - the title value to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return details
 	 */
 	public String getDetails() {
 		return details;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param details - the details value to set
 	 */
 	public void setDetails(String details) {
 		this.details = details;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return whether or not this item has been marked as read
 	 */
 	public boolean isReviewed() {
 		return reviewed;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param reviewed - the reviewed value to set
 	 */
 	public void setReviewed(boolean reviewed) {
 		this.reviewed = reviewed;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return creator - 
 	 */
 	public String getCreator() {
 		return creator;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param creator - the creator value to set
 	 */
 	public void setCreator(String creator) {
 		this.creator = creator;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return the user who reviewed this <code>ItemToReview</code>, if any
 	 */
 	public String getReviewedBy() {
 		return reviewedBy;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param reviewedBy - the reviewedBy value to set
 	 */
 	public void setReviewedBy(String reviewedBy) {
 		this.reviewedBy = reviewedBy;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return dateCreated - 
 	 */
 	public long getDateCreated() {
 		return dateCreated;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param dateCreated - the dateCreated value to set
 	 */
 	public void setDateCreated(long dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return dateReviewed - 
 	 */
 	public long getDateReviewed() {
 		return dateReviewed;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param dateReviewed - the dateReviewed value to set
 	 */
 	public void setDateReviewed(long dateReviewed) {
 		this.dateReviewed = dateReviewed;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @return item_id - 
 	 */
 	public Integer getItem_id() {
 		return item_id;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * @param item_id - the item_id value to set
 	 */
 	public void setItem_id(Integer item_id) {
 		this.item_id = item_id;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * <b> addToDB </b>
 	 * <pre>public void addToDB() throws Exception</pre> 
 	 * <blockquote> 
 	 * Adds this ItemToReview object to the 'items' table in the database.
 	 * </blockquote>
 	 * @throws Exception
 	 */
 	public void addToDB() throws Exception{
 		String photoPathName = null, videoPathName = null;
 		//Create the connection to the database
 		Class.forName("org.sqlite.JDBC");
 		//String dbFile = FileHelper.getDatabaseFile();
 		Path dbFilePath = Paths.get("Database", "umpd.db");
 		String dbFileName = dbFilePath.toString();
 		Connection conn = DriverManager.getConnection("jdbc:sqlite:Database/umpd.db");
 
 		//Create a prepared statement to add this bolo
 		PreparedStatement prep = conn.prepareStatement(
 				"REPLACE into items(item_id, title, details, dateCreated, dateReviewed," +
 				"createdBy, reviewedBy)" +
 				"VALUES (?, ?, ?, ?, ?, ?, ?);");
 
 		//Add the data to the prepared statement
 		if(this.item_id!=null){ prep.setInt(1, this.item_id); }
 		prep.setString(2, this.title);
 		prep.setString(3, this.details);
 		prep.setLong(4, this.dateCreated);
 		prep.setLong(5, this.dateReviewed);
 		prep.setString(6, this.createdBy);
 		prep.setString(7, this.reviewedBy);
 
 		prep.addBatch();
 
 		//Create new row in the table for the data
 		conn.setAutoCommit(false);
 		prep.executeBatch();
 		conn.setAutoCommit(true);
 
 		//Close the connection
 		conn.close();
 
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Deletes this <code>ItemToReview</code> object from the 'items' table in
 	 * the database.
 	 * @throws Exception
 	 */
 	public void deleteFromDB() throws Exception {
 		//check that this item is in the DB before attempting to delete it
 		if(item_id==null){
 			//do nothing, this item was never written to the database 
 			return;
 		}
 
 		//create the connection to the database
 		Class.forName("org.sqlite.JDBC");
 		Connection conn = DriverManager.getConnection("jdbc:sqlite:Database/umpd.db");	
 		Statement stat = conn.createStatement();
 
 		//perform delete
 		stat.executeUpdate("DELETE FROM items WHERE item_id = " + item_id);
 
 		//close the connection
 		conn.close();
 
 	}
 //-----------------------------------------------------------------------------
 	@Override 
 	public String toString() {
 	    StringBuilder result = new StringBuilder();//slightly faster
 
 	    result.append("{\n");
 	    result.append(" Title: " + this.title + "\n");
 	    result.append(" Details: " + this.details + "\n");
 	    result.append(" dateCreated: " + this.dateCreated + "\n");
 	    result.append(" dateReviewed: " + this.dateReviewed + "\n");
 	    result.append(" }");
 
 	    return (result.toString());
 	  }
 //-----------------------------------------------------------------------------
 }
