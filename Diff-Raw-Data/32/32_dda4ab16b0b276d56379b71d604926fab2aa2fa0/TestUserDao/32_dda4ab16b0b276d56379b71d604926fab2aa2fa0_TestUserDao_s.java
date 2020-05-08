 package com.parq.server.dao;
 
 import com.parq.server.dao.model.object.User;
 
 import junit.framework.TestCase;
 
 public class TestUserDao extends TestCase {
 
 	private UserDao userDao;
 
	private String userName = "Test_User_1";
	private String password = "Password123";
	private String eMail = "TestUser12@testCorp.com";
 
 	@Override
 	protected void setUp() throws Exception {
 		userDao = new UserDao();
 	}
 
 	public void testCreateUser() {
 		User newUser = new User();
 		newUser.setUserName(userName);
 		newUser.setPassword(password);
 		newUser.setEmail(eMail);
 
 		boolean userCreationSuccessful = userDao.createNewUser(newUser);
 		assertTrue(userCreationSuccessful);
 	}
 
 	public void testGetUserByUserName() {
 		User user = userDao.getUserByUserName(userName);
 
 		assertNotNull(user);
 		assertEquals(user.getUserName(), userName);
 		assertEquals(user.getPassword(), password);
 		assertEquals(user.getEmail(), eMail);
 		assertTrue(user.getUserID() > 0);
 	}
 	
 	public void testGetUserById() {
 		User userTemp = userDao.getUserByUserName(userName);
 		User user = userDao.getUserById(userTemp.getUserID());
 
 		assertNotNull(user);
 		assertEquals(user.getUserName(), userName);
 		assertEquals(user.getPassword(), password);
 		assertEquals(user.getEmail(), eMail);
 		assertTrue(user.getUserID() > 0);
 	}
 	
 	public void testGetUserByEmail() {
 		User user = userDao.getUserByEmail(eMail);
 
 		assertNotNull(user);
 		assertEquals(user.getUserName(), userName);
 		assertEquals(user.getPassword(), password);
 		assertEquals(user.getEmail(), eMail);
 		assertTrue(user.getUserID() > 0);
 	}
 
 	public void testCaching() {
 		
 		User user = userDao.getUserByUserName(userName);
 		User user1 = userDao.getUserByUserName(userName);
 		User user2 = userDao.getUserByUserName(userName);
 		User user3 = userDao.getUserByUserName(userName);
 		assertSame(user, user1);
 		assertSame(user, user2);
 		assertSame(user, user3);
 		
		User userId = userDao.getUserById(user.getUserID());
		User userId1 = userDao.getUserById(user.getUserID());
		User userId2 = userDao.getUserById(user.getUserID());
		User userId3 = userDao.getUserById(user.getUserID());
		assertSame(userId, userId1);
		assertSame(userId, userId2);
		assertSame(userId, userId3);
 		
 		User userEmail = userDao.getUserByEmail(eMail);
 		User userEmail1 = userDao.getUserByEmail(eMail);
 		User userEmail2 = userDao.getUserByEmail(eMail);
 		User userEmail3 = userDao.getUserByEmail(eMail);
 		assertSame(userEmail, userEmail1);
 		assertSame(userEmail, userEmail2);
 		assertSame(userEmail, userEmail3);
 	}
 
 	public void testDeleteUser() {
 		User user = userDao.getUserByUserName(userName);
 		boolean deleteSuccessful = userDao.deleteUserById(user.getUserID());
 		assertTrue(deleteSuccessful);
 	}
 }
