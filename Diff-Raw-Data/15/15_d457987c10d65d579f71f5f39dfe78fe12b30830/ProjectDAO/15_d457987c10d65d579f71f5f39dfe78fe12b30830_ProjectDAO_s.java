 package repository;
 
 /**
  * @author Xuesong Meng&Yidu Liang
  * @author Joanne Zhuo
  * @author Ying Gan
  */
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import domain.Project;
 import domain.User;
 
 public class ProjectDAO {
 
     public final static String PROJECT_NAME = "ClubUML";
     public final static String PROJECT_Desc = "ClubUML First Project";
     public final static byte PROJECT_Achived = 0;
 
     /**
      * Get projectID
      * 
      * @return projectId int
      */
     public static int getProjectId() {
 	int projectId = -1;
 	try {
 	    Connection conn = DbManager.getConnection();
 	    projectId = retrieveProject(conn);
 	    if (projectId == 0) {
 		// If we don't have the project existed in DB, then create one first
 		addProject(conn);
 		// retrieve the newly added project
 		projectId = retrieveProject(conn);
 	    }
 	    conn.close();
 	} catch (SQLException e) {
 	    System.out.println("Using default model.");
 	}
 	return projectId;
     }
 
     /**
      * Retrieve projectID from DB
      * 
      * @param conn
      * 			Established connection
      * @return projectId int
      *          return 0 if no exist 
      */
     public static int retrieveProject(Connection conn) {
 	int projectId = 0;
 	try {
 	    PreparedStatement pstmt = conn.prepareStatement(
 		    "SELECT * FROM project where projectName = ? ;");
 	    pstmt.setString(1, PROJECT_NAME);
 
 	    // Execute the SQL statement and store result into the ResultSet
 	    ResultSet rs = pstmt.executeQuery();
 	    if (rs.next()) {
 		projectId = rs.getInt("project_Id");
 	    }
 	    rs.close();
 	    pstmt.close();
 
 	} catch (SQLException e) {
 	    System.out.println("Using default model.");
 	}
 	return projectId;
     }
 
     /**
      * Add our default project into DB (project name, description, archived)
      * 			
      * @param conn
      * 			Established connection
      * @return true if success; false if fail
      */
     public static boolean addProject(Connection conn) {
 	try {
 		// modified by Xuesong Meng
 	    PreparedStatement pstmt = conn.prepareStatement(
 		    "INSERT into project(projectName, starDate ,description) VALUES(?,NOW(),?);");
 	    pstmt.setString(1, PROJECT_NAME);
 	    pstmt.setString(2, PROJECT_Desc);
 	    //pstmt.setByte(3, PROJECT_Achived);
 
 	    // Execute the SQL statement and update database accordingly.
 	    pstmt.executeUpdate();
 	    pstmt.close();
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	return true;
     }
 
     /**
      * Remove a project from DB
      * 
      * @return true if success; false if fail  
      */
     public static boolean removeProject() {
 	try {
 	    Connection conn = DbManager.getConnection();
 	    PreparedStatement pstmt = conn.prepareStatement(
 		    "DELETE FROM project where projectName = ?;");
 	    pstmt.setString(1, PROJECT_NAME);
 
 	    // Execute the SQL statement and update database accordingly.
 	    pstmt.executeUpdate();
 
 	    pstmt.close();
 	    conn.close();
 	    return true;
 	} catch (SQLException e) {
 	    throw new IllegalArgumentException(e.getMessage(), e);
 	}
     }
     
     /**
      * Get the users list in the specfic project
      * 
      * @author Ying Gan
      * 
      * @param projectId
      * 			The projectId of a project
      * @return ArrayList<User>
      * 			A user ArrayList contains the users in the project
      */
     public static ArrayList<User> getUsers(int projectId) throws SQLException {
     	ArrayList<User> users = new ArrayList<User>();
     	
     	Connection conn = null;
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DbManager.getConnection();
 			pstmt = conn.prepareStatement("SELECT user.userId, projectId, username, email, securityQ, securityA, userType" + 
 							" FROM userproject join user on userproject.userId = user.userId" +
 							" where projectId = ?;");
 			pstmt.setInt(1, projectId);
 			// Execute the SQL statement and store result into the ResultSet
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 				User u = new User(rs.getInt("userId"),rs.getString("userName"),
						"", rs.getString("securityQ"),rs.getString("securityA"), rs.getString("userType"));
 				users.add(u);
 			}
 			rs.close();
 			pstmt.close();
 			conn.close();
 			return users;						
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		} finally {
 			if(rs != null) {rs.close();}
 			if(pstmt != null) {pstmt.close();}
 			if(conn != null) {conn.close();}
 		}
 		return null;
     }
 }
