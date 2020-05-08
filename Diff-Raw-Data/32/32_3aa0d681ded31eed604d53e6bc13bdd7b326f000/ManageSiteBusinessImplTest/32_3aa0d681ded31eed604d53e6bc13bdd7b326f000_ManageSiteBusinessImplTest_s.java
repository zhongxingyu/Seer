 package edu.cmu.square.server.business.implementation;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.server.base.AbstractSpringBase;
 import edu.cmu.square.server.business.interfaces.ManageSiteBusiness;
 import edu.cmu.square.server.dao.model.User;
 
 
 public class ManageSiteBusinessImplTest extends AbstractSpringBase
 {
 	
 	@Resource
 	private ManageSiteBusiness manageSiteBusiness;
 	
 	@Before
 	public void setup() 
 	{
 		super.createUserProjectforRole();
 		userDao.addUserToProject(testUser, testProject, testRole);
 		manageSiteBusiness.setProjectName(testProject.getName());
 		manageSiteBusiness.setUserName(testUser.getUserName());
 
 	}
 	
 	@Test
 	public void testGetUserInfo()
 	{
 		try{
 			int testUserID = 1;
 			GwtUser gwtUsers = manageSiteBusiness.getUserInfo(testUserID);
 			
			assertTrue(gwtUsers.getUserName().equals("marco"));;
 		}
 		catch (SquareException e){
 			e.printStackTrace();
 			fail("Error " + e);
 		}		
 	}
 	@Test
 	public void testUpdateUserProfile()
 	{
 		try{
 			
 			GwtUser gwtUser = new GwtUser();
 			gwtUser.setUserId(1);
 			gwtUser.setFullName("Marco Len");
 			gwtUser.setEmailAddress("marcolen@gmail.com");
 			gwtUser.setIsAdmin(true);
 			
 			manageSiteBusiness.updateUserNameEmailRole(gwtUser);
 			
 			User user = new User();
 			user = userDao.fetch(gwtUser.getUserId());
 
 			assertEquals(user.getEmail(), "marcolen@gmail.com");
 			assertEquals(user.getFullName(), "Marco Len");
 			assertEquals(user.getIsAdmin(), gwtUser.getIsAdmin());
 
 
 		}catch (SquareException e){
 			e.printStackTrace();
 			fail("Error " + e);
 		}
 	}
 	
 	@Test
 	public void testUpdateUserPassword()
 	{
 		GwtUser user = testUser.createGwtUser();
 		try
 		{
 			manageSiteBusiness.updateUserPassword(user, "123", false);
 			User u = userDao.fetch(user.getUserId());
 			assertEquals(u.getPassword(), "123");
 		}
 		catch (SquareException e)
 		{
 			fail("error updating user " + e);
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 
 }
