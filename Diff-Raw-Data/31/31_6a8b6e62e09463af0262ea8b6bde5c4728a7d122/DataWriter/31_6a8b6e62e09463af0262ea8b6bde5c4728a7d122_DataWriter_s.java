 package dk.aersoe.jobfinder.crud.helpers;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.util.logging.Logger;
 
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import dk.aersoe.jobfinder.model.JobEntry;
 
 /**
  * Database helper that enables write functionality
  * 
  * @author morten@aersoe.dk
  *
  */
 public class DataWriter {
 
 	/**
 	 * This method creates or modifies a JobEntry record into the database
 	 * 
 	 * @param entry The jobentry that should be inserted into the database.
 	 * @param modify
 	 * @return
 	 */
	public boolean writeJobEntry(JobEntry entry, boolean modify){
 		boolean result = false;
 		//Logger log = Logger.getRootLogger();
 		// TODO: Find the best suitable (with respect to the App Server) Logger 
 		Connection con = null;
 		try{
 			String dsName = "JobFinderResource";
 			DataSource ds = (javax.sql.DataSource)new InitialContext().lookup(dsName);
 			con = ds.getConnection();
 			if (modify){
 				// Update requested
 				String createStmt = "update JobEntries set creation_date=?, modify_date=?, foreign_date=?, status=?, deadline=?, title=?, company=?, description=?, url=?, source?) where id=?";
 				con.setAutoCommit(false);
 				PreparedStatement pstmt = con.prepareStatement(createStmt);
				pstmt.setDate(1, (Date) entry.getCreationDate());
				pstmt.setDate(2, (Date) entry.getModifyDate());
				pstmt.setDate(3, (Date) entry.getForeignDate());
 				pstmt.setInt(4, entry.getStatus());
				pstmt.setDate(5, (Date) entry.getDeadline());
 				pstmt.setString(6, entry.getTitle());
 				pstmt.setString(7, entry.getCompany());
 				pstmt.setString(8, entry.getDescription());
 				pstmt.setString(9,  entry.getUrl());
 				pstmt.setString(10, entry.getSource());
 				pstmt.setInt(11, entry.getId());
 				pstmt.executeUpdate();
 				con.commit();
 				result = true;
 			}
 			else{
 				// Create requested
 				String createStmt = "insert into JobEntries (creation_date, modify_date, foreign_date, status, deadline, title, company, description, url, source) values(?,?,?,?,?,?,?,?,?,?)";
 				con.setAutoCommit(false);
 				PreparedStatement pstmt = con.prepareStatement(createStmt);
				pstmt.setDate(1, (Date) entry.getCreationDate());
				pstmt.setDate(2, (Date) entry.getModifyDate());
				pstmt.setDate(3, (Date) entry.getForeignDate());
 				pstmt.setInt(4, entry.getStatus());
				pstmt.setDate(5, (Date) entry.getDeadline());
 				pstmt.setString(6, entry.getTitle());
 				pstmt.setString(7, entry.getCompany());
 				pstmt.setString(8, entry.getDescription());
 				pstmt.setString(9,  entry.getUrl());
 				pstmt.setString(10, entry.getSource());
 				pstmt.executeUpdate();
 				con.commit();
 				result = true;
 			}
 		}	
 		catch (Exception e){
 			// TODO: Something intelligent should be said here
 		}
 		finally{
 			if (con != null){
 				try{
 					con.setAutoCommit(true);
 				}
 				catch(Exception e){
 					// TODO: Add some error handling here
 				}
 			}
 		}
 		return result;
 	}
 }
