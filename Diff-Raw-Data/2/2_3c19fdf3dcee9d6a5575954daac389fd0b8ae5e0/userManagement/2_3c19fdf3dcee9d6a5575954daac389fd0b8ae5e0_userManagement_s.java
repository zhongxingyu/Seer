 package de.enwida.web;
 
 import static org.junit.Assert.*;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 import de.enwida.web.dao.implementation.UserDao;
 import de.enwida.web.model.User;
 import de.enwida.web.model.UserPermission;
 
 
 public class userManagement {
 
 	@Autowired
 	private DriverManagerDataSource datasource;
 	
 	@Autowired
 	UserDao userDao;
 
 	@Test
 	public void AddUser() {
 		User user=new User(100,"test","test","test","test",false);
 		userDao.createUser(user);
 		
 	}
 
 	
 	@Test
 	public void SpringSecurtyLoginSQLCheck() {
 		
 	    String sql = "select user_name,user_password, enabled from users where user_name='test'";
 		 
 		Connection conn = null;
  
 		try {
 			conn = datasource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	@Test
 	public void SpringSecurtyAuthoritySQLCheck() {
 		
 	    String sql = "select user_name, authority from users INNER JOIN user_roles ON users.user_id=user_roles.user_id INNER JOIN roles ON roles.role_id=user_roles.role_id WHERE user_name='test'";
 		 
 		Connection conn = null;
  
 		try {
 			conn = datasource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 
 	@Test
 	public void GetAllUser() {
		List<User> users= userDao.findAll();
 		assertEquals(true,!users.isEmpty());
 	}
 	
 
 	@Test
 	public void GetAllUserFromDBWithPermissions() {
 		List<User> users= userDao.findAllUsersWithPermissions();
 		assertEquals(true,!users.isEmpty());
 	}
 	
 	@Test
 	public void LoadUserFromDB() {
 		User user=new User(100,"test","test","test","test",false);
 		User u=userDao.loadUserFromDB(user);
 		assertEquals(true,u.getLastName()!=null);
 	}
 	
 	@Test
 	public void AddPermission() {
 		userDao.addPermission(1, 1);
 	}
 	
 	@Test
 	public void removePermission() {
 		userDao.removePermission(1, 1);
 	}
 	
 	@Test
 	public void enableUser() {
 		assertEquals(true,userDao.enableDisableUser(true, 1));
 	}
 	
 	@Test
 	public void disbleUser() {
 		assertEquals(true,userDao.enableDisableUser(false, 1));
 	}
 	
 	@Test
 	public void DeleteUser() {
 		User user=new User(100,"test","test","test","test",false);
 		user.getUserPermissionCollection().implies(new UserPermission("admin"));
 		assertEquals(true,userDao.deleteUser(user));
 	}
 }
