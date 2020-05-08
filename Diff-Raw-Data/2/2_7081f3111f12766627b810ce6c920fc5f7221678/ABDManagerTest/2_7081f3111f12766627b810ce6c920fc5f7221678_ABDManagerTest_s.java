 package de.fhb.autobday.manager;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 
 import javax.ejb.EJB;
 
 import org.easymock.EasyMock;
 import org.junit.*;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.dao.AbdGroupToContactFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdUser;
 import de.fhb.autobday.manager.account.AccountManager;
 import de.fhb.autobday.manager.group.GroupManager;
 import de.fhb.autobday.manager.mail.MailManager;
 import de.fhb.autobday.manager.mail.MailManagerLocal;
 
 /**
  *
  * @author Michael Koppen <koppen@fh-brandenburg.de>
  */
 public class ABDManagerTest {
 	
 	private JavaEEGloss gloss;
 	
 	private AbdManager managerUnderTest;
 	
 	private AbdUserFacade userDAOMock;	
 	private AbdGroupFacade groupDAOMock;
 	private AbdGroupToContactFacade grouptocontactDAOMock;
 	private AbdAccountFacade accountDAOMock;
 	private AbdContactFacade contactDAOMock;
 	private GroupManager groupManagerMock;
	private MailManagerLocal mailManager;
 	
 	public ABDManagerTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 	
 	@Before
 	public void setUp() {
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		userDAOMock = EasyMock.createMock(AbdUserFacade.class);
 		groupDAOMock = EasyMock.createMock(AbdGroupFacade.class);
 		grouptocontactDAOMock = EasyMock.createMock(AbdGroupToContactFacade.class);
 		accountDAOMock = EasyMock.createMock(AbdAccountFacade.class);
 		contactDAOMock = EasyMock.createMock(AbdContactFacade.class);
 		groupManagerMock = EasyMock.createMock(GroupManager.class);
 		mailManager = EasyMock.createMock(MailManager.class);
 		
 		//set Objekts to inject
 		gloss.addEJB(userDAOMock);
 		gloss.addEJB(groupDAOMock);
 		gloss.addEJB(grouptocontactDAOMock);
 		gloss.addEJB(accountDAOMock);
 		gloss.addEJB(contactDAOMock);
 		gloss.addEJB(groupManagerMock);
 		gloss.addEJB(mailManager);
 		
 		//create Manager with Mocks
 		managerUnderTest=gloss.make(AbdManager.class);
 	}
 	
 	@After
 	public void tearDown() {
 		
 	}
 
 	/**
 	 * Test of getAllUser method, of class ABDManager.
 	 */
 	@Test
 	public void testGetAllUser() throws Exception {
 		System.out.println("getAllUser");
 		
 		ArrayList<AbdUser> userList = new ArrayList<AbdUser>();
 		userList.add(new AbdUser(1));
 		userList.add(new AbdUser(2));
 		userList.add(new AbdUser(3));
 		
 		EasyMock.expect(userDAOMock.findAll()).andReturn(userList);
 		EasyMock.replay(userDAOMock);
 		assertEquals(userList, managerUnderTest.getAllUser());
 		EasyMock.verify(userDAOMock);
 	}
 
 	/**
 	 * Test of getAllGroups method, of class ABDManager.
 	 */
 	@Test
 	public void testGetAllGroups() throws Exception {
 		System.out.println("getAllGroups");
 		
 		ArrayList<AbdGroup> groupList = new ArrayList<AbdGroup>();
 		groupList.add(new AbdGroup("1"));
 		groupList.add(new AbdGroup("2"));
 		groupList.add(new AbdGroup("3"));
 		
 		EasyMock.expect(groupDAOMock.findAll()).andReturn(groupList);
 		EasyMock.replay(groupDAOMock);
 		assertEquals(groupList, managerUnderTest.getAllGroups());
 		EasyMock.verify(groupDAOMock);
 	}
 
 	/**
 	 * Test of getAllAccountdata method, of class ABDManager.
 	 */
 	@Test
 	public void testGetAllAccountdata() throws Exception {
 		System.out.println("getAllAccountdata");
 		
 		ArrayList<AbdAccount> accountList = new ArrayList<AbdAccount>();
 		accountList.add(new AbdAccount(1));
 		accountList.add(new AbdAccount(2));
 		accountList.add(new AbdAccount(3));
 		
 		EasyMock.expect(accountDAOMock.findAll()).andReturn(accountList);
 		EasyMock.replay(accountDAOMock);
 		assertEquals(accountList, managerUnderTest.getAllAccountdata());
 		EasyMock.verify(accountDAOMock);
 	}
 
 	/**
 	 * Test of getAllContacts method, of class ABDManager.
 	 */
 	@Test
 	public void testGetAllContacts() throws Exception {
 		System.out.println("getAllContacts");
 		
 		ArrayList<AbdContact> contactsList = new ArrayList<AbdContact>();
 		contactsList.add(new AbdContact("1"));
 		contactsList.add(new AbdContact("2"));
 		contactsList.add(new AbdContact("3"));
 		
 		EasyMock.expect(contactDAOMock.findAll()).andReturn(contactsList);
 		EasyMock.replay(contactDAOMock);
 		assertEquals(contactsList, managerUnderTest.getAllContacts());
 		EasyMock.verify(contactDAOMock);
 	}
 
 	/**
 	 * Test of hallo method, of class ABDManager.
 	 */
 	@Test
 	@Ignore
 	public void testHallo() throws Exception {
 		System.out.println("hallo");
 	}
 }
