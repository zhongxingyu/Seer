 package de.fhb.autobday.manager.account;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 
 import de.fhb.autobday.commons.EMailValidator;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdUser;
 import de.fhb.autobday.exception.account.AccountAlreadyExsistsException;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
 import de.fhb.autobday.exception.user.NoValidUserNameException;
 import de.fhb.autobday.exception.user.UserNotFoundException;
 import de.fhb.autobday.manager.connector.google.GoogleImporter;
 
 /**
  * Test the AccountManager
  *
  * @author 
  * Andy Klay <klay@fh-brandenburg.de>
  * Michael Koppen <koppen@fh-brandenburg.de>
  * Christoph Ott <>
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {AccountManager.class, EMailValidator.class} )
 public class AccountManagerTest {
 	
 	private JavaEEGloss gloss;
 	
 	private AccountManager managerUnderTest;
 	
 	private AbdAccountFacade accountDAOMock;
 	
 	private AbdUserFacade userDAOMock;
 	
 	private GoogleImporter gImporter;
 	
 	public AccountManagerTest() {
 	}
 	
 	@Before
 	public void setUp() {
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		accountDAOMock = EasyMock.createMock(AbdAccountFacade.class);
 		userDAOMock = EasyMock.createMock(AbdUserFacade.class);
 		gImporter = EasyMock.createMock(GoogleImporter.class);
 		
 		//set Objekts to inject
 		gloss.addEJB(accountDAOMock);
 		gloss.addEJB(userDAOMock);
 		gloss.addEJB(gImporter);
 		
 		//create Manager with Mocks
 		managerUnderTest=gloss.make(AccountManager.class);
 		PowerMock.mockStatic(EMailValidator.class);
 	}
 	
 	/**
 	 * Test of addAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testAddAccount() throws Exception {
 		
 		System.out.println("testAddAccount");
 		
 		//prepare test variables
 		String password="password";
 		String userName="test@googlemail.com";
 		String type="type";
 		
 		Collection<AbdAccount> collection=new ArrayList<AbdAccount>();
 		
 
 		//prepare a user object
 		int userId=1;
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
 		EasyMock.expect(EMailValidator.isGoogleMail(userName)).andReturn(true);
 		accountDAOMock.create((AbdAccount) EasyMock.anyObject());
 		userDAOMock.refresh(user);
 		
 		// Setup is finished need to activate the mock
 		PowerMock.replay(EMailValidator.class);
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
 	 * Test of addAccount method, of class AccountManager.
 	 * This test provokes a NoValidUserNameException!
 	 */
 	@Test(expected = NoValidUserNameException.class)
 	public void testAddAccountThrowsNoValidUserNameException() throws Exception {
 		
 		System.out.println("testAddAccountThrowsNoValidUserNameException");
 		
 		//prepare test variables
 		String password="password";
 		String userName="test@googlemail.com";
 		String type="type";
 		
 		Collection<AbdAccount> collection=new ArrayList<AbdAccount>();
 		
 
 		//prepare a user object
 		int userId=1;	
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
 	 * Test of addAccount method, of class AccountManager.
 	 * This test provokes a AccountAlreadyExsistsException!
 	 */
 	@Test(expected = AccountAlreadyExsistsException.class)
 	public void testAddAccountThrowsAccountAlreadyExsistsException() throws Exception {
 		
 		System.out.println("testAddAccountThrowsAccountAlreadyExsistsException");
 		
 		//prepare test variables
 		String password="password";
 		String userName="test@googlemail.com";
 		String type="type";
 		
 		Collection<AbdAccount> collection=new ArrayList<AbdAccount>();
 		AbdAccount existsAccount= new AbdAccount();
 		existsAccount.setUsername(userName);
 		existsAccount.setType(type);
 
 		//prepare a user object
 		int userId=1;	
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
 	 * Test of addAccount method, of class AccountManager.
 	 * This test provokes a UserNotFoundException!
 	 */
 	@Test(expected = UserNotFoundException.class)
 	public void testAddAccountShouldThrowUserNotFoundException() throws Exception {
 		
 		System.out.println("testAddAccountShouldThrowUserNotFoundException");
 		
 		//prepare test variables
 		int abduserId = EasyMock.anyInt();
 		String password="password";
 		String userName="mustermann";
 		String type="type";
 		
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
 	 */
 	@Test
 	public void testRemoveAccountWithClass() throws Exception {
 		System.out.println("testRemoveAccountWithClass");
 
 		//prepare test variables
 		int accountId = EasyMock.anyInt();
 		AbdAccount account = new AbdAccount(1);
 		
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(account);
 		accountDAOMock.remove(account);
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 		
 		//call method to test
 		managerUnderTest.removeAccount(account);
 		
 		// verify		
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of removeAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testRemoveAccountWithInt() throws Exception {
 		System.out.println("testRemoveAccountWithInt");
 
 		//prepare test variables
 		int accountId = EasyMock.anyInt();
 		AbdAccount account = new AbdAccount(1);
 		
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(accountDAOMock.find(accountId)).andReturn(account);
 		accountDAOMock.remove(account);
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(accountDAOMock);
 		
 		//call method to test
 		managerUnderTest.removeAccount(accountId);
 		
 		// verify		
 		EasyMock.verify(accountDAOMock);
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
 		
 		gImporter.getConnection(account);
 		gImporter.importContacts();		
 		
 		EasyMock.replay(accountDAOMock);
 		EasyMock.replay(gImporter);
 
 		managerUnderTest.importGroupsAndContacts(accountId);
 
 		EasyMock.verify(gImporter);
 		EasyMock.verify(accountDAOMock);
 	}
 	
 	
 	/**
 	 * Test of importGroupsAndContacts method, of class AccountManager.
 	 * This test provokes a AccountNotFoundException!
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
 	public void testGetAllContactsFromGroupWithClass() throws Exception {
 		System.out.println("testGetAllContactsFromGroupWithClass");
 		
 		AbdGroup groupOne = new AbdGroup("1");
 		AbdGroup groupTwo = new AbdGroup("2");
 		
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 		
 		ArrayList<AbdGroup> outputCollection=new ArrayList<AbdGroup>();
 		outputCollection.add(groupOne);
 		outputCollection.add(groupTwo);
 		
 		account.setAbdGroupCollection(outputCollection);
 		
 
 		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(account);
 
 		EasyMock.replay(accountDAOMock);
 		
 		assertEquals(outputCollection, managerUnderTest.getAllGroupsFromAccount(account));
 		EasyMock.verify(accountDAOMock);
 	}
 	
 	/**
 	 * Test of getAllContactsFromGroup method, of class GroupManager.
 	 */
 	@Test
 	public void testGetAllContactsFromGroupWithInt() throws Exception {
 		System.out.println("testGetAllContactsFromGroupWithInt");
 		
 		AbdGroup groupOne = new AbdGroup("1");
 		AbdGroup groupTwo = new AbdGroup("2");
 		
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 		
 		ArrayList<AbdGroup> outputCollection=new ArrayList<AbdGroup>();
 		outputCollection.add(groupOne);
 		outputCollection.add(groupTwo);
 		
 		account.setAbdGroupCollection(outputCollection);
 		
 
 		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(account);
 
 		EasyMock.replay(accountDAOMock);
 		
 		assertEquals(outputCollection, managerUnderTest.getAllGroupsFromAccount(account.getId()));
 		EasyMock.verify(accountDAOMock);
 	}
 	
 	/**
 	 * Test of getAllContactsFromGroup method, of class GroupManager.
 	 */
 	@Test(expected = AccountNotFoundException.class)
 	public void testGetAllContactsFromGroupShouldThrowAccountNotFoundException() throws Exception {
 		System.out.println("testGetAllContactsFromGroupShouldThrowAccountNotFoundException");
 		
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");		
 
 		EasyMock.expect(accountDAOMock.find(account.getId())).andStubReturn(null);
 		EasyMock.replay(accountDAOMock);
 		
 		managerUnderTest.getAllGroupsFromAccount(account.getId());
 		EasyMock.verify(accountDAOMock);
 	}
 	
 }
