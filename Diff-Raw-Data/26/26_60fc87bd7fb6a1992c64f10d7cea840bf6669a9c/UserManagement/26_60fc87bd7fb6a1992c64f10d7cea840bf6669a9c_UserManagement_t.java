 package de.enwida.web;
 
 import static org.junit.Assert.assertEquals;
 
import java.io.FileNotFoundException;
import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.List;
 
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import de.enwida.web.controller.AdminController;
 import de.enwida.web.dao.implementation.GroupDaoImpl;
 import de.enwida.web.dao.implementation.RightsDaoImpl;
 import de.enwida.web.dao.implementation.RoleDaoImpl;
 import de.enwida.web.dao.implementation.UserDaoImpl;
 import de.enwida.web.model.Group;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.User;
import de.enwida.web.service.implementation.MailServiceImpl;
  
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/root-context-test.xml")
 public class UserManagement {
 
 	@Autowired
 	private DriverManagerDataSource datasource;
 	
 	@Autowired
	private UserDaoImpl userDao;

    @Autowired
    private MailServiceImpl mailService;
 	   
     @Autowired
     private GroupDaoImpl groupDao;    
     
     @Autowired
     private RoleDaoImpl roleDao;   
     
     @Autowired
     private RightsDaoImpl rightsDao;     
 
     private static org.apache.log4j.Logger logger = Logger.getLogger(AdminController.class);
 
 	
 	private User user;
 	
 	@Before
 	public void testUser() {
 	    user=new User(100,"test1","test","test","test",false);
 	    user.setJoiningDate(new Date(Calendar.getInstance().getTimeInMillis()));
 	    user.setCompanyName("enwida.de");
         User existingUser = userDao.getUserByName(user.getUserName());
 	    if(existingUser==null){
 	        user.setUserID(userDao.save(user));
 	    }else{
 	        user=existingUser;
 	    }
 	    userDao.enableDisableUser(user.getUserID(), false);
 	    userDao.enableDisableUser(user.getUserID(), true);
 	    userDao.deleteUser(user);
 	}
 	
    @Test
     public void saveUserGroup() {
        Group group = groupDao.getAllGroups().get(0);
        //save user in any group
        userDao.assignUserToGroup(user.getUserID(),group.getGroupID());
        userDao.deassignUserFromGroup(user.getUserID(), group.getGroupID());
        //save user in anonymous group
        group =groupDao.getGroupByName("anonymous");
        userDao.assignUserToGroup(user.getUserID(),group.getGroupID());
        userDao.deassignUserFromGroup(user.getUserID(), group.getGroupID());
     }
    
    @Test
    public void updateUser() {
       userDao.save(user);
       user.setCompanyName("test");
       userDao.updateUser(user);
       User user2=userDao.getUserByName(user.getUserName());
       assertEquals("test", user2.getCompanyName());
       userDao.deleteUser(user);
       assertEquals(null,userDao.getUserByName(user.getUserName()));
    }
    
    @Test
   public void testMail() throws Exception {
       mailService.SendEmail("olcaytarazan@gmail.com", "User Management Test", "Ignore");
   }
   
   @Test
    public void testRightsDao() {
       List<Right> rights=  rightsDao.findAll();
       rightsDao.enableDisableAspect(rights.get(0).getRightID(), false);
       List<Right> rights2=  rightsDao.findAll();
       assertEquals(false,rights2.get(0).isEnabled());
       rightsDao.enableDisableAspect(rights.get(0).getRightID(), true);
        List<Right> rights3=  rightsDao.findAll();
       assertEquals(true,rights3.get(0).isEnabled());
    }
 
 	@Test
 	public void SpringSecurtyLoginSQLCheck() throws Exception {
 		
 	    String sql = getSpringSecurityQuery("//authentication-manager/authentication-provider/jdbc-user-service/@users-by-username-query");
          
 		Connection conn = null;
  
 		try {
 			conn = datasource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
             ps.setString(1, user.getUserName());
 			ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
                 } catch (SQLException e) {
                     logger.error(e.getMessage());
                 }
 			}
 		}
 	}
 	
 	@Test
 	public void SpringSecurtyAuthoritySQLCheck() throws Exception {
 		
 	    String sql = getSpringSecurityQuery("//authentication-manager/authentication-provider/jdbc-user-service/@authorities-by-username-query");
 		 
 		Connection conn = null;
  
 		try {
 			conn = datasource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
             ps.setString(1, user.getUserName());
 			ps.executeQuery();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
                 } catch (SQLException e) {
                     logger.error(e.getMessage());
                 }
 			}
 		}
 	}
 	
 	
 	private String getSpringSecurityQuery(String xPathInFile) throws Exception{
 	    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document dDoc = builder.parse("src/main/webapp/WEB-INF/spring/security-app-context.xml");
 
         XPath xPath = XPathFactory.newInstance().newXPath();
         NodeList nodes = (NodeList) xPath.evaluate(xPathInFile, dDoc, XPathConstants.NODESET);
         return nodes.item(0).getTextContent();
 	}
 
 	
 	@Test
 	public void testEnabledDisableAspect() {
         rightsDao.enableDisableAspect(1, true);
         rightsDao.enableDisableAspect(1, false);
 	}
 
 }
