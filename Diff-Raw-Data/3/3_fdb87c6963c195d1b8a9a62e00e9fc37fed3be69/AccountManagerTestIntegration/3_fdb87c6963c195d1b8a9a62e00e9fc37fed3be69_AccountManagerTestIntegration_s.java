 package de.fhb.autobday.manager.account;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
import de.fhb.autobday.commons.CipherHelper;
 import de.fhb.autobday.commons.PropertyLoader;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.*;
 import de.fhb.autobday.manager.connector.google.GoogleImporter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Properties;
 import javax.persistence.EntityManager;
 import org.easymock.EasyMock;
 import static org.easymock.EasyMock.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
 
 
 /**
  * Integrationtest
  * 
  * @author 
  * Andy Klay <klay@fh-brandenburg.de>,
  * Christoph Ott
  */
 public class AccountManagerTestIntegration {
 	
 	private static JavaEEGloss gloss;
 	
 	private static AccountManager managerUnderTest;
 	
 	private static AbdAccountFacade accountDAO;
 	
 	private static AbdUserFacade userDAO;
 	
 	private static AbdContactFacade contactDAO;
 	
 	private static GoogleImporter gImporterMock;
 	
 	private EntityManager emMock;
 	
 	private PropertyLoader propLoader;
 	
 	@BeforeClass
 	public static void setUpClass(){
 		accountDAO = new AbdAccountFacade();
 		contactDAO = new AbdContactFacade();
 		userDAO = new AbdUserFacade();
 		gImporterMock = new GoogleImporter();
 	}
 	
 	@Before
 	public void setUp() {
 		
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		emMock = createMock(EntityManager.class);
 		gImporterMock = EasyMock.createMock(GoogleImporter.class);
 		
 		//set EntityManagers
 		accountDAO.setEntityManager(emMock);
 		userDAO.setEntityManager(emMock);
 		contactDAO.setEntityManager(emMock);
 		
 		//set Objekts to inject
 		gloss.addEJB(accountDAO);
 		gloss.addEJB(userDAO);
 		gloss.addEJB(contactDAO);
 		gloss.addEJB(gImporterMock);
 		
 		//create Manager with Mocks
 		managerUnderTest=gloss.make(AccountManager.class);
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
 		String password="password";
 		String userName="test@googlemail.com";
 		String type="type";	
 		
 		Collection<AbdAccount> abdAccountCollection = new ArrayList<AbdAccount>();
 
 		//prepare a user object
 		int userId=1;
 		AbdUser user = new AbdUser(userId, "mustermann", "password", "salt", "mustermann", "max");
 		user.setAbdAccountCollection(abdAccountCollection);
 		
 		Properties masterPassword = new Properties();
 		masterPassword.setProperty("master", "sraeBrsc");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(emMock.find(AbdUser.class, user.getId())).andReturn(user);
 		expect(propLoader.loadSystemchiperPasswordProperty("/SystemChiperPassword.properties")).andReturn(masterPassword);
 		
 		emMock.persist((AbdAccount)anyObject());
 		emMock.refresh((AbdUser)anyObject());
 		
 		// Setup is finished need to activate the mock
 		replay(propLoader);
 		replay(emMock);
 		
 		// testing Methodcall
 		managerUnderTest.addAccount(userId, password, userName, type);
 		
 		//verify
 		verify(propLoader);
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * Test of removeAccount method, of class AccountManager.
 	 */
 	@Test
 	public void testRemoveAccount() throws Exception {
 		System.out.println("testRemoveAccountWithClass");
 
 		//prepare test variables
 		int accountId = 5;
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
 		expect(emMock.find(AbdAccount.class, account.getId())).andReturn(account);
 		expect(emMock.merge(account)).andReturn(account);
 		expect(emMock.merge(contact)).andReturn(contact);
 
 		emMock.remove(account);
 		
 		expect(emMock.merge(contact)).andReturn(contact);
 		emMock.remove(contact);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		// call method to test
 		managerUnderTest.removeAccount(account);
 		
 		//verify
 		verify(emMock);
 	}
 	
 	/**
 	 * Test of getAllContactsFromGroup method, of class GroupManager.
 	 */
 	@Test
 	public void testGetAllGroupsFromAccount() throws Exception {
 		System.out.println("testGetAllGroupsFromAccount");
 		
 		//prepare test variables
 		AbdGroup groupOne = new AbdGroup("1");
 		AbdGroup groupTwo = new AbdGroup("2");
 		
 		AbdAccount account = new AbdAccount(22, "itsme", "itsme", "type");
 		
 		ArrayList<AbdGroup> outputCollection=new ArrayList<AbdGroup>();
 		outputCollection.add(groupOne);
 		outputCollection.add(groupTwo);
 		account.setAbdGroupCollection(outputCollection);
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(emMock.find(AbdAccount.class, account.getId())).andReturn(account);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		// testing Methodcall
 		managerUnderTest.getAllGroupsFromAccount(account.getId());
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * Test of testImportGroupsAndContacts method, of class GroupManager.
 	 * 
 	 */
 	@Test
 	@Ignore
 	public void testImportGroupsAndContacts()throws Exception {
 		System.out.println("testImportGroupsAndContacts");
 		
 		//prepare test variables
 		int accountId = 2;
 		AbdAccount account = new AbdAccount(accountId);
 		
 		// Setting up the expected value of the method call of Mockobject
 		expect(emMock.find(AbdAccount.class, account.getId())).andReturn(account);
 		
 		gImporterMock.getConnection(account);
 		gImporterMock.importContacts();		
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		replay(gImporterMock);
 		
 		//call method to test
 		managerUnderTest.importGroupsAndContacts(accountId);
 		
 		// verify
 		verify(gImporterMock);
 		verify(emMock);
 	}
 
 }
