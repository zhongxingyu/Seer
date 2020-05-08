 package UnitTest;
 import static org.junit.Assert.*;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import repository.DbManager;
 import repository.UserDAO;
 import domain.Project;
 import domain.User;
 
 public class UserDAOTest {
 
 	protected void setUp() {
 		
 	}
 	@Test
 	public void testAddUser() throws SQLException{
 		ArrayList<String> before = new ArrayList<String>();
 		ArrayList<String> after = new ArrayList<String>();
 		before.add("newtestuser");
 		before.add("1234");
 		before.add("newuser@email.com");
 		before.add("What's your pet's name");
 		before.add("mypetname");
 		
 		
 		String username = "newtestuser";
		User user = new User("newtestuser","1234","newuser@email.com","What's your pet's name","mypetname");
 		UserDAO.addUser(user);
 		Connection conn = null;
 		ResultSet rs = null;
 		//ResultSet rsDelete = null;
 		PreparedStatement pstmt = null;
 		PreparedStatement pstmtDelete = null;
 		try {
 		    conn = DbManager.getConnection();
 			pstmt = conn.prepareStatement("SELECT * FROM user where userName = ?;");
 			pstmt.setString(1, username);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				after.add(rs.getString("username"));
 				after.add(rs.getString("password"));
 				after.add(rs.getString("email"));
 				after.add(rs.getString("securityQ"));
				after.add(rs.getString("securityA"));				
 			}
 			pstmtDelete = conn.prepareStatement("DELETE FROM user where userId = ?;");
 			pstmtDelete.setString(1, rs.getString("userId"));
 			pstmtDelete.executeUpdate();		
 			
 		} catch (SQLException e) {
 			
 		} finally {
 			rs.close();
 			//rsDelete.close();
 			pstmt.close();
 			pstmtDelete.close();
 			conn.close();
 		}
 		
 		assertArrayEquals(before.toArray(),after.toArray());
 	}
 	
 	@Test
 	public void testGetProjects() throws SQLException {
 		/*
 		 * For successfully running this test, make sure you have a user whose uerId=1 and this user has 2 projects
 		 */
 		ArrayList<Project> projects;
 		projects = UserDAO.getProjects(1);
 		assertTrue(projects.size() == 2);
 	}
 	
 /*
 	@Test
 	public void testGetUserStringString() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetUserString() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetUserInt() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testRemoveUser() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testUpdateUser() {
 		fail("Not yet implemented");
 	}
 */
 	
 }
