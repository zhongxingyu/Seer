 package de.fhb.autobday.manager.account;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 import de.fhb.autobday.commons.CipherHelper;
 import de.fhb.autobday.commons.EMailValidator;
 import de.fhb.autobday.commons.PropertyLoader;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.*;
 import de.fhb.autobday.exception.account.AccountAlreadyExsistsException;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
 import de.fhb.autobday.exception.user.NoValidUserNameException;
 import de.fhb.autobday.exception.user.UserNotFoundException;
 import de.fhb.autobday.manager.connector.google.GoogleImporter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Properties;
 import org.easymock.EasyMock;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 /**
  * Test the AccountManager
  *
  * @author Andy Klay <klay@fh-brandenburg.de> Christoph Ott
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({AccountManager.class, EMailValidator.class, CipherHelper.class})
 public class AccountManagerTest {
 
 	private JavaEEGloss gloss;
 	private AccountManager managerUnderTest;
 	private AbdAccountFacade accountDAOMock;
 	private AbdUserFacade userDAOMock;
 	private AbdContactFacade contactDAOMock;
 	private GoogleImporter gImporter;
 	private PropertyLoader propLoader;
 
 	public AccountManagerTest() {
 	}
 
 	@Before
 	public void setUp() {
 		gloss = new JavaEEGloss();
 
 		//create Mocks
 		accountDAOMock = EasyMock.createMock(AbdAccountFacade.class);
 		userDAOMock = EasyMock.createMock(AbdUserFacade.class);
 		contactDAOMock = EasyMock.createMock(AbdContactFacade.class);
 		gImporter = EasyMock.createMock(GoogleImporter.class);
 
 
 		//set Objekts to inject
 		gloss.addEJB(accountDAOMock);
 		gloss.addEJB(userDAOMock);
 		gloss.addEJB(contactDAOMock);
 		gloss.addEJB(gImporter);
 
 		//create Manager with Mocks
 		managerUnderTest = gloss.make(AccountManager.class);
 		PowerMock.mockStatic(EMailValidator.class);
 		PowerMock.mockStatic(CipherHelper.class);
 	}
 
 	/**
 	 * Test of addAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testAddAccount() throws Exception {
 
 		System.out.println("testAddAccount");
 
 		propLoader = EasyMock.createMock(PropertyLoader.class);
 		managerUnderTest.setPropLoader(propLoader);
 
 		//prepare test variables
 		String password = "password";
 		String userName = "test@googlemail.com";
 		String type = "type";
 
 		Collection<AbdAccount> collection = new ArrayList<AbdAccount>();
 
 
 		//prepare a user object
 		int userId = 1;
 		AbdUser user = new AbdUser();
 		user.setFirstname("");
 		user.setName("");
 		user.setId(userId);
 		user.setPasswort("password");
 		user.setSalt("salt");
 		user.setUsername("mustermann");
 		user.setAbdAccountCollection(collection);
 
 		Properties masterPassword = new Properties();
 		masterPassword.setProperty("master", "sraeBrsc");
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(userId)).andReturn(user);
 		EasyMock.expect(EMailValidator.isGoogleMail(userName)).andReturn(true);
 
 		EasyMock.expect(propLoader.loadSystemProperty("/SystemCipherPassword.properties")).andReturn(masterPassword);
 		EasyMock.expect(CipherHelper.cipher(password, masterPassword.getProperty("master"))).andReturn(password);
 
 		accountDAOMock.create((AbdAccount) EasyMock.anyObject());
 		userDAOMock.refresh(user);
 
 		// Setup is finished need to activate the mock
 		PowerMock.replay(EMailValidator.class);
 		PowerMock.replay(CipherHelper.class);
 
 		EasyMock.replay(userDAOMock);
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(propLoader);
 
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 
 		// verify		
 		EasyMock.verify(userDAOMock);
 		EasyMock.verify(propLoader);
 		EasyMock.verify(accountDAOMock);
 		PowerMock.verify(EMailValidator.class);
 		PowerMock.verify(CipherHelper.class);
 	}
 
 	/**
 	 * Test of addAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testAddAccountWithSameNameButOtherType() throws Exception {
 
 		System.out.println("testAddAccountWithSameNameButOtherType");
 
 		propLoader = EasyMock.createMock(PropertyLoader.class);
 		managerUnderTest.setPropLoader(propLoader);
 
 		//prepare test variables
 		String password = "password";
 		String userName = "test@googlemail.com";
 		String type = "type";
 
 		Collection<AbdAccount> collection = new ArrayList<AbdAccount>();
 		AbdAccount existsAccount = new AbdAccount();
 		existsAccount.setUsername("test1234@googlemail.com");
 		existsAccount.setType(type);
 
 		//prepare a user object
 		int userId = 1;
 		AbdUser user = new AbdUser();
 		user.setFirstname("");
 		user.setName("");
 		user.setId(userId);
 		user.setPasswort("password");
 		user.setSalt("salt");
 		user.setUsername("mustermann");
 		user.setAbdAccountCollection(collection);
 		user.getAbdAccountCollection().add(existsAccount);
 
 		Properties masterPassword = new Properties();
 		masterPassword.setProperty("master", "sraeBrsc");
 
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(userId)).andReturn(user);
 		EasyMock.expect(EMailValidator.isGoogleMail(userName)).andReturn(true);
 
 		EasyMock.expect(propLoader.loadSystemProperty("/SystemCipherPassword.properties")).andReturn(masterPassword);
 		EasyMock.expect(CipherHelper.cipher(password, masterPassword.getProperty("master"))).andReturn(password);
 
 
 		accountDAOMock.create((AbdAccount) EasyMock.anyObject());
 		userDAOMock.refresh(user);
 
 		PowerMock.replay(EMailValidator.class);
 		PowerMock.replay(CipherHelper.class);
 
 		EasyMock.replay(userDAOMock);
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(propLoader);
 
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 
 		// verify		
 		EasyMock.verify(userDAOMock);
 		EasyMock.verify(propLoader);
 		EasyMock.verify(accountDAOMock);
 		PowerMock.verify(EMailValidator.class);
 		PowerMock.verify(CipherHelper.class);
 	}
 
 	/**
 	 * Test of addAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testAddAccountWithOtherNameButSameType() throws Exception {
 
 		System.out.println("testAddAccountWithSameNameButOtherType");
 
 		propLoader = EasyMock.createMock(PropertyLoader.class);
 		managerUnderTest.setPropLoader(propLoader);
 
 		//prepare test variables
 		String password = "password";
 		String userName = "test@googlemail.com";
 		String type = "type";
 
 		Collection<AbdAccount> collection = new ArrayList<AbdAccount>();
 		AbdAccount existsAccount = new AbdAccount();
 		existsAccount.setUsername(userName);
 		existsAccount.setType("type1234");
 
 		//prepare a user object
 		int userId = 1;
 		AbdUser user = new AbdUser();
 		user.setFirstname("");
 		user.setName("");
 		user.setId(userId);
 		user.setPasswort("password");
 		user.setSalt("salt");
 		user.setUsername("mustermann");
 		user.setAbdAccountCollection(collection);
 		user.getAbdAccountCollection().add(existsAccount);
 
 		Properties masterPassword = new Properties();
 		masterPassword.setProperty("master", "sraeBrsc");
 
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(userId)).andReturn(user);
 		EasyMock.expect(EMailValidator.isGoogleMail(userName)).andReturn(true);
 
 		EasyMock.expect(propLoader.loadSystemProperty("/SystemCipherPassword.properties")).andReturn(masterPassword);
 		EasyMock.expect(CipherHelper.cipher(password, masterPassword.getProperty("master"))).andReturn(password);
 
 
 		accountDAOMock.create((AbdAccount) EasyMock.anyObject());
 		userDAOMock.refresh(user);
 
 		// Setup is finished need to activate the mock
 		PowerMock.replay(EMailValidator.class);
 		PowerMock.replay(CipherHelper.class);
 
 		EasyMock.replay(userDAOMock);
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(propLoader);
 
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 
 		// verify		
 		EasyMock.verify(userDAOMock);
 		EasyMock.verify(propLoader);
 		EasyMock.verify(accountDAOMock);
 		PowerMock.verify(EMailValidator.class);
 		PowerMock.verify(CipherHelper.class);
 	}
 
 	/**
 	 * Test of addAccount method, of class AccountManager. This test provokes a
 	 * NoValidUserNameException!
 	 */
 	@Test(expected = NoValidUserNameException.class)
 	public void testAddAccountThrowsNoValidUserNameException() throws Exception {
 
 		System.out.println("testAddAccountThrowsNoValidUserNameException");
 
 		//prepare test variables
 		String password = "password";
 		String userName = "test@googlemail.com";
 		String type = "type";
 
 		Collection<AbdAccount> collection = new ArrayList<AbdAccount>();
 
 
 		//prepare a user object
 		int userId = 1;
 		AbdUser user = new AbdUser();
 		user.setFirstname("");
 		user.setName("");
 		user.setId(userId);
 		user.setPasswort("password");
 		user.setSalt("salt");
 		user.setUsername("mustermann");
 		user.setAbdAccountCollection(collection);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(userId)).andReturn(user);
 		EasyMock.expect(EMailValidator.isGoogleMail(userName)).andReturn(false);
 		accountDAOMock.create((AbdAccount) EasyMock.anyObject());
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 		EasyMock.replay(accountDAOMock);
 
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 
 		// verify		
 		EasyMock.verify(userDAOMock);
 		EasyMock.verify(accountDAOMock);
 		PowerMock.verify(EMailValidator.class);
 	}
 
 	/**
 	 * Test of addAccount method, of class AccountManager. This test provokes a
 	 * AccountAlreadyExsistsException!
 	 */
 	@Test(expected = AccountAlreadyExsistsException.class)
 	public void testAddAccountThrowsAccountAlreadyExsistsException() throws Exception {
 
 		System.out.println("testAddAccountThrowsAccountAlreadyExsistsException");
 
 		//prepare test variables
 		String password = "password";
 		String userName = "test@googlemail.com";
 		String type = "type";
 
 		Collection<AbdAccount> collection = new ArrayList<AbdAccount>();
 		AbdAccount existsAccount = new AbdAccount();
 		existsAccount.setUsername(userName);
 		existsAccount.setType(type);
 
 		//prepare a user object
 		int userId = 1;
 		AbdUser user = new AbdUser();
 		user.setFirstname("");
 		user.setName("");
 		user.setId(userId);
 		user.setPasswort("password");
 		user.setSalt("salt");
 		user.setUsername("mustermann");
 		user.setAbdAccountCollection(collection);
 		user.getAbdAccountCollection().add(existsAccount);
 
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(userId)).andReturn(user);
 		EasyMock.expect(EMailValidator.isGoogleMail(userName)).andReturn(true);
 		accountDAOMock.create((AbdAccount) EasyMock.anyObject());
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 		EasyMock.replay(accountDAOMock);
 
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 
 		// verify		
 		EasyMock.verify(userDAOMock);
 		EasyMock.verify(accountDAOMock);
 		PowerMock.verify(EMailValidator.class);
 	}
 
 	/**
 	 * Test of addAccount method, of class AccountManager. This test provokes a
 	 * UserNotFoundException!
 	 */
 	@Test(expected = UserNotFoundException.class)
 	public void testAddAccountShouldThrowUserNotFoundException() throws Exception {
 
 		System.out.println("testAddAccountShouldThrowUserNotFoundException");
 
 		//prepare test variables
 		int abduserId = EasyMock.anyInt();
 		String password = "password";
 		String userName = "mustermann";
 		String type = "type";
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(abduserId)).andReturn(null);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 
 		//call method to test
 		EasyMock.expect(EMailValidator.isEmail(userName)).andReturn(true);
 		managerUnderTest.addAccount(abduserId, password, userName, type);
 
 		// verify		
 		EasyMock.verify(userDAOMock);
 	}
 
 	/**
 	 * Test of removeAccount method, of class AccountManager.
 	 *
 	 */
 	@Test
 	public void testRemoveAccountWithClass() throws Exception {
 		System.out.println("testRemoveAccountWithClass");
 
 		//prepare test variables
 		int accountId = EasyMock.anyInt();
 		Collection<AbdGroup> groupCollection = new ArrayList<AbdGroup>();
 		Collection<AbdGroupToContact> groupToContactCollection = new ArrayList<AbdGroupToContact>();
 		AbdAccount account = new AbdAccount(accountId);
 		AbdContact contact = new AbdContact("testname");
 		AbdGroup group = new AbdGroup("testgroup");
 		AbdGroupToContact groupToContact = new AbdGroupToContact();
 
 		account.setAbdGroupCollection(groupCollection);
 		groupToContact.setAbdContact(contact);
 		groupToContact.setAbdGroup(group);
 		groupCollection.add(group);
 		groupToContactCollection.add(groupToContact);
 		group.setAbdGroupToContactCollection(groupToContactCollection);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(account);
 
 		accountDAOMock.remove(account);
 
 		contactDAOMock.edit(contact);
 		contactDAOMock.remove(contact);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(contactDAOMock);
 
 		//call method to test
 		managerUnderTest.removeAccount(account);
 
 		// verify		
 		EasyMock.verify(accountDAOMock);
 		EasyMock.verify(contactDAOMock);
 	}
 
 	/**
 	 * Test of removeAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testRemoveAccountWithInt() throws Exception {
 		System.out.println("testRemoveAccountWithInt");
 
 		//prepare test variables
 		int accountId = EasyMock.anyInt();
 		Collection<AbdGroup> groupCollection = new ArrayList<AbdGroup>();
 		Collection<AbdGroupToContact> groupToContactCollection = new ArrayList<AbdGroupToContact>();
 		AbdAccount account = new AbdAccount(accountId);
 		AbdContact contact = new AbdContact("testname");
 		AbdGroup group = new AbdGroup("testgroup");
 		AbdGroupToContact groupToContact = new AbdGroupToContact();
 
 		account.setAbdGroupCollection(groupCollection);
 		groupToContact.setAbdContact(contact);
 		groupToContact.setAbdGroup(group);
 		groupCollection.add(group);
 		groupToContactCollection.add(groupToContact);
 		group.setAbdGroupToContactCollection(groupToContactCollection);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(account);
 		accountDAOMock.remove(account);
 
 		contactDAOMock.edit(contact);
 		contactDAOMock.remove(contact);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(contactDAOMock);
 
 		//call method to test
 		managerUnderTest.removeAccount(accountId);
 
 		// verify		
 		EasyMock.verify(accountDAOMock);
 		EasyMock.verify(contactDAOMock);
 	}
 
 	/**
 	 * Test of removeAccount method, of class AccountManager.
 	 */
 	@Test(expected = AccountNotFoundException.class)
 	public void testRemoveAccountShouldThrowAccountNotFoundException() throws Exception {
 		System.out.println("testRemoveAccountShouldThrowAccountNotFoundException");
 
 		//prepare test variables
 		int accountId = 1;
 		AbdAccount account = new AbdAccount(1);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(null);
 		accountDAOMock.remove(account);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 
 		//call method to test
 		managerUnderTest.removeAccount(accountId);
 
 		// verify		
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of importGroupsAndContacts method, of class AccountManager.
 	 */
 	@Test
 	public void testImportGroupsAndContacts() throws Exception {
 		System.out.println("testImportGroupsAndContacts");
 
 		//prepare test variables
 		int accountId = 2;
 		AbdAccount account = new AbdAccount(accountId);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(account);
 		accountDAOMock.refresh(account);
 
 		gImporter.getConnection(account);
		gImporter.importContacts();
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(gImporter);
 
 		//call method to test
 		managerUnderTest.importGroupsAndContacts(accountId);
 
 		// verify
 		EasyMock.verify(gImporter);
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of importGroupsAndContacts method, of class AccountManager. This
 	 * test provokes a AccountNotFoundException!
 	 */
 	@Test(expected = AccountNotFoundException.class)
 	public void testImportGroupsAndContactsThrowAccountNotFoundException() throws Exception {
 		System.out.println("testImportGroupsAndContactsThrowAccountNotFoundException");
 
 		//prepare test variables
 		int accountId = 1;
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(null);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 
 		//call method to test
 		managerUnderTest.importGroupsAndContacts(accountId);
 
 		// verify		
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of getAllContactsFromGroup method, of class GroupManager.
 	 */
 	@Test
 	public void testgetAllGroupsFromAccountWithClass() throws Exception {
 		System.out.println("testgetAllGroupsFromAccountWithClass");
 
 		//prepare test variables
 		AbdGroup groupOne = new AbdGroup("1");
 		AbdGroup groupTwo = new AbdGroup("2");
 
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 
 		ArrayList<AbdGroup> outputCollection = new ArrayList<AbdGroup>();
 		outputCollection.add(groupOne);
 		outputCollection.add(groupTwo);
 
 		account.setAbdGroupCollection(outputCollection);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(account);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 
 		//call method to test
 		// verify	
 		assertEquals(outputCollection, managerUnderTest.getAllGroupsFromAccount(account));
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of getAllContactsFromGroup method, of class GroupManager.
 	 */
 	@Test
 	public void testGetAllGroupsFromAccountWithInt() throws Exception {
 		System.out.println("testGetAllGroupsFromAccountWithInt");
 
 		//prepare test variables
 		AbdGroup groupOne = new AbdGroup("1");
 		AbdGroup groupTwo = new AbdGroup("2");
 
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 
 		ArrayList<AbdGroup> outputCollection = new ArrayList<AbdGroup>();
 		outputCollection.add(groupOne);
 		outputCollection.add(groupTwo);
 
 		account.setAbdGroupCollection(outputCollection);
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(account);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 
 		//call method to test
 		// verify
 		assertEquals(outputCollection, managerUnderTest.getAllGroupsFromAccount(account.getId()));
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of getAllContactsFromGroup method, of class GroupManager.
 	 */
 	@Test(expected = AccountNotFoundException.class)
 	public void testGetAllGroupsFromAccountShouldThrowAccountNotFoundException() throws Exception {
 		System.out.println("testGetAllGroupsFromAccountShouldThrowAccountNotFoundException");
 
 		//prepare test variables
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(null);
 
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 
 		//call method to test
 		// verify
 		managerUnderTest.getAllGroupsFromAccount(account.getId());
 		EasyMock.verify(accountDAOMock);
 	}
 }
