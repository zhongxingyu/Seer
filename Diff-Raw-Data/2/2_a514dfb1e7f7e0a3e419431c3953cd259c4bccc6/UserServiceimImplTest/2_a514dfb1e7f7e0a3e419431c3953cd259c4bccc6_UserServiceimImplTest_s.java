 package com.camilolopes.readerweb.services.impl;
 
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.camilolopes.readerweb.dbunit.DBUnitConfiguration;
 import com.camilolopes.readerweb.enums.StatusUser;
 import com.camilolopes.readerweb.model.bean.User;
 import com.camilolopes.readerweb.services.interfaces.UserService;
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:**/OrderPersistenceTests-context.xml"})
 @TransactionConfiguration(defaultRollback=true,transactionManager="transactionManager")
 @Transactional
 public class UserServiceimImplTest extends DBUnitConfiguration{
 	@Autowired
 	private UserService userServiceImpl;
 	private User user;
 
 	
 	@Before
 	public void setUp() throws Exception {
 		user = new User();
 		getSetUpOperation().execute(getConnection(), getDataSet());
 	}
 	
 	private void setupUserData() {
 		user.setEmail("use12@user.com");
 		user.setLastname("lopes");
 		user.setName("camilo");
 		user.setPassword("123456");
 		user.setRegisterDate(new Date());
 		user.setStatus(StatusUser.ATIVE);
 	}
 
 	@Test
 	public void testAddNewUserWithSucess() {
 		setupUserData();
 		try{
 			int expectedTotalUser = 1 + userServiceImpl.readAll().size();
 			userServiceImpl.saveOrUpdate(user);
 			assertNotNull(userServiceImpl.readAll());
 			assertEquals(expectedTotalUser ,userServiceImpl.readAll().size());
 		}catch (Exception e) {
 			fail("not expected result " + e.fillInStackTrace());
 		}
 	}
 
 
 	@Test
 	public void testDeletedUserById() throws Exception {
 		assertEquals(userServiceImpl.readAll().size(),getDataSet().getTable("USER").getRowCount());
 		User userFound = (User) getSessionFactory().getCurrentSession().get(User.class, new Long("1"));
 		assertNotNull(userFound);
 		userServiceImpl.delete(userFound);
 		userFound = (User) getSessionFactory().getCurrentSession().get(User.class, 1L);
 		assertNull(userFound);
 	}
 
 	@Test
 	public void testFindUserById() {
 		assertNotNull(userServiceImpl.searchById(1L));
 	}
 	@Test
 	public void testUserNotFoundById(){
 		assertNull(userServiceImpl.searchById(0L));
 	}
 
 	@Test
 	public void testReturnAllUsersExists() {
 		assertNotNull(userServiceImpl.readAll());
 		assertFalse(userServiceImpl.readAll().isEmpty());
 	}
 	
 	@Test
 	public void testUpdateEmailOfUser(){
 		User user = (User) getSessionFactory().getCurrentSession().get(User.class, 1L);
 		user.setEmail("novo@email.com.br");
 		userServiceImpl.saveOrUpdate(user);
 		User userFound = userServiceImpl.searchById(1L);
 		assertEquals(user.getEmail(), userFound.getEmail() );
 	}
 	@Test
 	public void testUpdateDataOfTheUser(){
 		User user = (User) getSessionFactory().getCurrentSession().get(User.class, 2L);
		String expectedNameUser = "joão";
 		assertEquals(expectedNameUser, user.getName());
 		user.setName("Pedro"); 
 		user.setLastname("Leão"); 
 		user.setRegisterDate(new Date());
 		user.setStatus(StatusUser.INACTIVE);
 		userServiceImpl.saveOrUpdate(user);
 		User userUpdated = userServiceImpl.searchById(2L);
 		assertEquals(user.getName(), userUpdated.getName());
 		assertFalse(user.getStatus()!= userUpdated.getStatus());
 	}
 }
