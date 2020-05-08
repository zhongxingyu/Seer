 package models;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 @Entity 
 public class ReviewDetail {
 	@Id
 	private String reviewId;
 	private String reviewerId;
 	private String type;
 	private String severity;
 	private String fileName;
 	private String summery;
 	private String description;
 	private String phase;
 	private String assignedTo;
 	private String author;
 	private Date creationDate;
 	private String modifier;
 	private Date modificationDate;
 	
 	private String line;
 	private String revision;
 	private String status;
 	
 	private String comment;
 	private String group;
 	
 	
 	
 	public String getGroup() {
 	    return group;
 	}
 
 	public void setGroup(String group) {
 	    this.group = group;
 	}
 
 	public String getDatePassedCreation(){
 		long diff = System.currentTimeMillis() - creationDate.getTime() ;
 		return String.valueOf((diff / (1000 * 60 * 60 * 24)));
 	}
 	
 	public String getComment() {
 		return comment;
 	}
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 	public String getLine() {
 		return line;
 	}
 	public void setLine(String line) {
 		this.line = line;
 	}
 	public String getRevision() {
 		return revision;
 	}
 	public void setRevision(String revision) {
 		this.revision = revision;
 	}
 	public String getStatus() {
 		return status;
 	}
 	public void setStatus(String status) {
 		this.status = status;
 	}
 	public String getReviewId() {
 		return reviewId;
 	}
 	public void setReviewId(String reviewId) {
 		this.reviewId = reviewId;
 	}
 	public String getReviewerId() {
 		return reviewerId;
 	}
 	public void setReviewerId(String reviewerId) {
 		this.reviewerId = reviewerId;
 	}
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public String getSeverity() {
 		return severity;
 	}
 	public void setSeverity(String severity) {
 		this.severity = severity;
 	}
 	public String getSummery() {
 		return summery;
 	}
 	public void setSummery(String summery) {
 		this.summery = summery;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getPhase() {
 		return phase;
 	}
 	public void setPhase(String phase) {
 		this.phase = phase;
 	}
 	public String getAssignedTo() {
 		return assignedTo;
 	}
 	public void setAssignedTo(String assignedTo) {
 		this.assignedTo = assignedTo;
 	}
 	public String getAuthor() {
 		return author;
 	}
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	public Date getCreationDate() {
 		return creationDate;
 	}
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 	public String getModifier() {
 		return modifier;
 	}
 	public void setModifier(String modifier) {
 		this.modifier = modifier;
 	}
 	public Date getModificationDate() {
 		return modificationDate;
 	}
 	public void setModificationDate(Date modificationDate) {
 		this.modificationDate = modificationDate;
 	}
 	public String getFileName() {
 		return fileName;
 	}
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 
 
 
 	public static String getLastAddition(Connection connection, String userId){
 		String reviewId=null;
 		 try {
			PreparedStatement ps = connection.prepareStatement("select reviewId from (select reviewId from review_dtl where reviewerId=? order by creation_dt desc)res where rownum=1 ");
 
 			   ps.setString(1, userId);
 			   ps.execute();
 			   ResultSet rs =  ps.getResultSet();
 				 while(rs.next()){
 					 reviewId = rs.getString(1);
 				 }
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		 return reviewId;
 	}
 
 
 	public static ReviewDetail getReviewById(Connection connection, String id) {
 		//DataSource ds = DB.getDataSource();
 	//	  Connection connection = DB.getConnection();
 		 ReviewDetail review = new ReviewDetail();
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("select reviewId,reviewerId,type,severity,fileName,summery,description,phase,assignedTo,author,creation_dt,modification_dt,modifier,line,revision,status,comment,groupname from review_dtl where reviewId=? ");
 
 		   ps.setString(1, id);
 		 
 		  boolean result = ps.execute();
 		  ResultSet rs =  ps.getResultSet();
 	
 		  
 		  
 		 while(rs.next()){
 			
 			 review.setReviewId(rs.getString(1));
 			 review.setReviewerId(rs.getString(2));
 			 review.setType(rs.getString(3));
 			 review.setSeverity(rs.getString(4));
 			 review.setFileName(rs.getString(5));
 			 review.setSummery(rs.getString(6));
 			 review.setDescription(rs.getString(7));
 			 review.setPhase(rs.getString(8));
 			 review.setAssignedTo(rs.getString(9));
 			 review.setAuthor(rs.getString(10));
 			 review.setCreationDate(rs.getTimestamp(11));
 			 review.setModificationDate(rs.getTimestamp(12));
 			 review.setModifier(rs.getString(13));
 			 review.setLine(rs.getString(14));
 			 review.setRevision(rs.getString(15));
 			 review.setStatus(rs.getString(16));
 			 review.setComment(rs.getString(17));
 			 review.setGroup(rs.getString(18));
 			 
 		 }
 		  System.out.println("executed for select ["+id+"]" );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return review;
 	}
 
 	
 	public static ReviewDetail updateReview(Connection connection, ReviewDetail review, String username) {
 		System.out.println(review.getReviewId()+ " -- " + username);
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("update review_dtl set type =? , severity=?, phase=?, assignedTo=?, line=?, status=?, modification_dt=?, modifier=? , comment=?, summery=?, fileName=?,groupname=?,description=? where reviewId=? ");
 		  
 		  ps.setString(1, review.getType());
 		  ps.setString(2, review.getSeverity());
 		  ps.setString(3, review.getPhase());
 		  ps.setString(4, review.getAssignedTo());
 		  ps.setString(5, review.getLine());
 		  ps.setString(6, review.getStatus());
 		  ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
 		  ps.setString(8, username);
 		  ps.setString(9, review.getComment());
 		  ps.setString(10, review.getSummery());
 		  ps.setString(11, review.getFileName());
 		  ps.setString(12, review.getGroup());
 		  ps.setString(13, review.getDescription());
 		  ps.setString(14, review.getReviewId());
 		  
 		  boolean result = ps.execute();
 		  System.out.println("executed for update ["+result+"]");
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return review;
 	}
 
 	
 	
 
 }
