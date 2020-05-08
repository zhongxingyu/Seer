 package org.devilry.core.session.dao;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
 
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.devilry.core.dao.UserImpl;
 import org.devilry.core.daointerfaces.UserRemote;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UserImplTest extends AbstractDaoTst {
 	protected UserRemote userBean;
 	protected long testUser0, testUser1, testUser2;
 
 	ArrayList<String> names = new ArrayList<String>();
 	ArrayList<String> emails = new ArrayList<String>();
 	ArrayList<String> phoneNumbers = new ArrayList<String>();
 	
 	@Before
 	public void setUp() throws NamingException {
 		setupEjbContainer();
 		userBean = getRemoteBean(UserImpl.class);
 		
 		names.add("Bendik");
 		names.add("Espen");
 		names.add("Morten");
 		
 		emails.add("bendik@universe.com");
 		emails.add("xxx@pornstar.com");
 		emails.add("machoguy@gayparade.com");
 		
 		phoneNumbers.add("9999999");
 		phoneNumbers.add("6666666");
 		phoneNumbers.add("80012345");
 		
 		// Create some test users
 		testUser0 = userBean.create(names.get(0), emails.get(0), phoneNumbers.get(0));
 		testUser1 = userBean.create(names.get(1),  emails.get(1), phoneNumbers.get(1));
 		testUser2 = userBean.create(names.get(2), emails.get(2), phoneNumbers.get(2));
 	}
 
 	@After
 	public void tearDown() {
 		for(long userId: userBean.getUsers()) {
 			userBean.remove(userId);
 		}
 		
 		names.clear();
 		emails.clear();
 		phoneNumbers.clear();
 	}
 
 	@Test
 	public void getName() {
 		assertEquals(names.get(0), userBean.getName(testUser0));
 		assertEquals(names.get(1), userBean.getName(testUser1));
 		assertEquals(names.get(2), userBean.getName(testUser2));
 	}
 
 	@Test
 	public void getEmail() {
 		assertEquals(emails.get(0), userBean.getEmail(testUser0));
 		assertEquals(emails.get(1), userBean.getEmail(testUser1));
 		assertEquals(emails.get(2), userBean.getEmail(testUser2));
 	}
 
 	@Test
 	public void getPhoneNumber() {
 		assertEquals(phoneNumbers.get(0), userBean.getPhoneNumber(testUser0));
 		assertEquals(phoneNumbers.get(1), userBean.getPhoneNumber(testUser1));
 		assertEquals(phoneNumbers.get(2), userBean.getPhoneNumber(testUser2));
 	}
 
 	
 	@Test
 	public void setName() {
 		userBean.setName(testUser0, "newname");
 		assertEquals("newname", userBean.getName(testUser0));
 	}
 
 	@Test
 	public void setEmail() {
 		userBean.setEmail(testUser0, "newemail");
 		assertEquals("newemail", userBean.getEmail(testUser0));
 	}
 
 	@Test(expected=Exception.class)
 	public void setExistingEmail() {
 		userBean.setEmail(testUser0, "newemail");
 		userBean.setEmail(testUser1, "newemail");
 	}
 
 	@Test
 	public void setPhoneNumber() {
 		userBean.setPhoneNumber(testUser0, "newphone");
 		assertEquals("newphone", userBean.getPhoneNumber(testUser0));
 	}
 	
 	
 	@Test
 	public void remove() {
 		List<Long> users = userBean.getUsers();
 		assertTrue(users.size() == names.size()+1); // +1 because of the user created in super
 		
 		userBean.remove(testUser0);
 		
 		List<Long> users2 = userBean.getUsers();
 		assertTrue(users2.size() == names.size()); // not -1 because of the user created in super
 	}
 	
 	
 
 	@Test
 	public void addIdentity() {
 		userBean.addIdentity(testUser0, "laban");
 		
 		long id = userBean.findUser("laban");
 		assertTrue(testUser0 == id);
 	}
 	
 	@Test
 	public void removeIdentity() {
 		userBean.addIdentity(testUser0, "laban");
 		userBean.addIdentity(testUser0, "tull");
 		
 		userBean.removeIdentity("laban");
 		assertFalse(userBean.identityExists("laban"));
 		
 		userBean.removeIdentity("tull");
 		assertFalse(userBean.identityExists("tull"));
 		
 		List<String> identities = userBean.getIdentities(testUser0);
 		assertTrue(identities.size() == 0);
 	}
 	
 	
 	@Test
 	public void identityExists() {
 		userBean.addIdentity(testUser0, "laban");
 		userBean.addIdentity(testUser0, "tull");
 		
 		assertTrue(userBean.identityExists("laban"));
 		assertTrue(userBean.identityExists("tull"));
 		assertFalse(userBean.identityExists("nonexistantidentity"));
 	}
 	
 	
 	@Test
 	public void emailExists() {
 		assertTrue(userBean.emailExists(emails.get(0)));
 		assertTrue(userBean.emailExists(emails.get(1)));
 		assertFalse(userBean.emailExists("nonexistantemail"));
 	}
 
 	@Test
 	public void userExists() {
 		assertTrue(userBean.userExists(testUser0));
 		assertFalse(userBean.userExists(testUser0+testUser1+testUser2));
 	}
 
 	
 	@Test
 	public void getIdentities() {
 		userBean.addIdentity(testUser0, "laban");
 		userBean.addIdentity(testUser0, "tull");
 		
 		List<String> identities = userBean.getIdentities(testUser0);
 		
 		assertTrue(identities.size() == 2);
 		assertTrue(identities.contains("laban"));
 		assertTrue(identities.contains("tull"));
 	}
 	
 	
 	@Test
 	public void findUser() {
 		userBean.addIdentity(testUser0, "laban");
 
 		long id = userBean.findUser("laban");
 		assertTrue(testUser0 == id);
 	}
 
 	
 	@Test
 	public void getUsers() {
 		List<Long> users = userBean.getUsers();
 	
 		assertTrue(users.size() == names.size() + 1); // +1 because of the user created in super
 		assertTrue(users.contains(testUser0));
 		assertTrue(users.contains(testUser1));
 		assertTrue(users.contains(testUser2));
 	}
 
 	
 	@Test
 	public void getAuthenticatedUser() {
 		assertEquals(homerId, userBean.getAuthenticatedUser());
 	}
 
 	@Test
 	public void getAuthenticatedIdentity() {
 		assertEquals("homer", userBean.getAuthenticatedIdentity());
 	}
 }
