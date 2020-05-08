 package de.fhb.autobday.manager.connector.google;
 
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.contacts.Gender.Value;
 import com.google.gdata.data.contacts.*;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.data.extensions.FamilyName;
 import com.google.gdata.data.extensions.GivenName;
 import com.google.gdata.data.extensions.Name;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 import de.fhb.autobday.commons.CipherHelper;
 import de.fhb.autobday.commons.PropertyLoader;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.dao.AbdGroupToContactFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdGroupToContact;
 import de.fhb.autobday.exception.commons.CouldNotDecryptException;
 import de.fhb.autobday.exception.commons.CouldNotLoadMasterPasswordException;
 import de.fhb.autobday.exception.connector.ConnectorCouldNotLoginException;
 import de.fhb.autobday.exception.connector.ConnectorInvalidAccountException;
 import de.fhb.autobday.exception.connector.ConnectorNoConnectionException;
 import de.fhb.autobday.exception.connector.ConnectorRequestFailedException;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import org.easymock.EasyMock;
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 /**
  *
  * @author Tino Reuschel <reuschel@fh-brandenburg.de>
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({CipherHelper.class})
 public class GoogleImporterTest {
 
 	private JavaEEGloss gloss;
 	private ContactsService contactsServiceMock;
 	private ContactEntry contactEntry;
 	private GoogleImporter gImporterUnderTest;
 	private PropertyLoader propLoader;
 
 	public GoogleImporterTest() {
 	}
 
 	@Before
 	public void setUp() {
 
 		gloss = new JavaEEGloss();
 
 		//create Mocks
 		contactsServiceMock = createMock(ContactsService.class);
 
 		//set Objekts to inject
 		gImporterUnderTest = gloss.make(GoogleImporter.class);
 		PowerMock.mockStatic(CipherHelper.class);
 
 		//prepare test variables
 		Name name = new Name();
 		gImporterUnderTest = new GoogleImporter();
 
 
 		contactEntry = new ContactEntry();
 
 		name.setGivenName(new GivenName("Hans", ""));
 		name.setFamilyName(new FamilyName("Peter", ""));
 		contactEntry.setId("1");
 		contactEntry.setName(name);
 		contactEntry.setGender(new Gender(Value.FEMALE));
 		contactEntry.setBirthday(new Birthday("1990-05-22"));
 	}
 
 	/**
 	 * Test of getConnection method, of class GoogleImporter.
 	 *
 	 * @throws AuthenticationException
 	 * @throws ConnectorInvalidAccountException
 	 * @throws ConnectorCouldNotLoginException
 	 */
 	@Test
 	public void testGetConnection() throws Exception {
 		System.out.println("getConnection");
 
 		propLoader = EasyMock.createMock(PropertyLoader.class);
 		gImporterUnderTest.setPropLoader(propLoader);
 
 		AbdAccount data = new AbdAccount(1, "fhbtestacc@googlemail.com", "TestGoogle123", null);
 
 		Properties masterPassword = new Properties();
 		masterPassword.setProperty("master", "sraeBrsc");
 
 		EasyMock.expect(propLoader.loadSystemProperty("/SystemCipherPassword.properties")).andReturn(masterPassword);
 		EasyMock.expect(CipherHelper.decipher(data.getPasswort(), masterPassword.getProperty("master"))).andReturn("TestGoogle123");
 
 
 		// Setup is finished need to activate the mock
 		replay(contactsServiceMock);
 		PowerMock.replay(CipherHelper.class);
 		replay(propLoader);
 		gImporterUnderTest.getConnection(data);
 
 		//call method to test and verify
 		assertEquals(true, gImporterUnderTest.isConnectionEtablished());
 		verify(contactsServiceMock);
 		verify(propLoader);
 		PowerMock.verify(CipherHelper.class);
 	}
 
 	/**
 	 * Test of getConnection method, of class GoogleImporter.
 	 *
 	 * @throws AuthenticationException
 	 * @throws ConnectorInvalidAccountException
 	 * @throws ConnectorCouldNotLoginException
 	 * @throws CouldNotLoadMasterPasswordException 
 	 * @throws CouldNotDecryptException 
 	 */
 	@Test(expected = ConnectorInvalidAccountException.class)
 	public void testGetConnectionCouldThrowConnectorInvalidAccountException() throws AuthenticationException, ConnectorCouldNotLoginException, ConnectorInvalidAccountException, CouldNotDecryptException, CouldNotLoadMasterPasswordException {
 		System.out.println("getConnection");
 
 		// Setup is finished need to activate the mock
 		replay(contactsServiceMock);
 
 		//call method to test and verify
 		gImporterUnderTest.getConnection(null);
 		verify(contactsServiceMock);
 	}
 
 	/**
 	 * Test of getAllGroups method, of class GoogleImporter.
 	 *
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	@Test
 	public void testGetAllGroupsWithNull() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("getAllGroups");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(null);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		instance.myService = myServiceMock;
 		assertEquals(null, instance.getAllGroups());
 	}
 
 	/**
 	 * Test of getAllGroups method, of class GoogleImporter.
 	 *
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	@Test
 	public void testGetAllGroupsWithGroupFeed() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("getAllGroups");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		ContactGroupFeed resultFeed = new ContactGroupFeed();
 		List<ContactGroupEntry> contactGroupList = new ArrayList<ContactGroupEntry>();
 		ContactGroupEntry contactGroup = new ContactGroupEntry();
 		contactGroup.setId("hsfsfd");
 		contactGroupList.add(contactGroup);
 		resultFeed.setEntries(contactGroupList);
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(resultFeed);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		assertEquals(contactGroupList, instance.getAllGroups());
 	}
 
 	@Test (expected = ConnectorRequestFailedException.class)
 	public void testGetAllGroupsByThrowingIOException() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("testGetAllGroupsByThrowingIOException");
 
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andThrow(new IOException());
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		instance.getAllGroups();
 	}
 
 	@Test (expected = ConnectorRequestFailedException.class)
 	public void testGetAllGroupsByThrowingServiceException() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("testGetAllGroupsByThrowingServiceException");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andThrow(new ServiceException(""));
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		instance.getAllGroups();
 	}
 
 	/**
 	 * Test of getAllContacts method, of class GoogleImporter.
 	 *
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	@Test
 	public void testGetAllContactsReturnNull() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("getAllContacts");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(null);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		assertEquals(null, instance.getAllContacts());
 	}
 
 	/**
 	 * Test of getAllContacts method, of class GoogleImporter.
 	 *
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	@Test
 	public void testGetAllContactsReturnList() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("getAllContacts");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		List<ContactEntry> contactEntryList = new ArrayList<ContactEntry>();
 		contactEntryList.add(contactEntry);
 		ContactFeed resultFeed = new ContactFeed();
 		resultFeed.setEntries(contactEntryList);
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(resultFeed);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		assertEquals(contactEntryList, instance.getAllContacts());
 	}
 
 	@Test (expected = ConnectorRequestFailedException.class)
 	public void testGetAllContactsByThrowingIOException() throws IOException, ServiceException, ConnectorRequestFailedException {
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andThrow(new IOException());
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		instance.getAllContacts();
 	}
 
 	@Test (expected = ConnectorRequestFailedException.class)
 	public void testGetAllContactsByThrowingServiceException() throws IOException, ServiceException, ConnectorRequestFailedException {
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andThrow(new ServiceException(""));
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		instance.myService = myServiceMock;
 		instance.getAllContacts();
 	}
 
 	/**
 	 * Test of importContacts method, of class GoogleImporter.
 	 *
 	 * @throws ConnectorNoConnectionException
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	@Test
 	public void testImportContacts() throws ConnectorNoConnectionException, IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("importContacts");
 
 		GoogleImporter instance = new GoogleImporter();
 		ContactGroupFeed resultFeedGroup = new ContactGroupFeed();
 		ContactFeed resultFeedContact = new ContactFeed();
 		AbdAccountFacade accountDAOMock = createMock(AbdAccountFacade.class);
 		AbdAccount abdAccount = new AbdAccount();
 		abdAccount.setAbdGroupCollection(new ArrayList<AbdGroup>());
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		instance.connectionEtablished = true;
 		instance.accdata = abdAccount;
 
 		// Setting up the expected value of the method call of Mockobject
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(resultFeedGroup);
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(resultFeedContact);
 		accountDAOMock.flush();
 		accountDAOMock.refresh(abdAccount);
 		accountDAOMock.edit(abdAccount);
 
 		replay(accountDAOMock);
 		replay(myServiceMock);
 		instance.myService = myServiceMock;
 		instance.accountDAO = accountDAOMock;
 
 		instance.importContacts();
 
 	}
 
 	/**
 	 * Test of importContacts method, of class GoogleImporter.
 	 *
 	 * @throws ConnectorNoConnectionException
 	 */
 	@Test(expected = ConnectorNoConnectionException.class)
 	public void testImportContactsThrowConnectorNoConnectionExceptionBecouseAccountNull() throws ConnectorNoConnectionException, ConnectorRequestFailedException {
 		System.out.println("importContacts");
 
 		GoogleImporter instance = new GoogleImporter();
 		instance.connectionEtablished = true;
 		instance.accdata = null;
 
 		instance.importContacts();
 
 	}
 
 	/**
 	 * Test of importContacts method, of class GoogleImporter.
 	 *
 	 * @throws ConnectorNoConnectionException
 	 */
 	@Test(expected = ConnectorNoConnectionException.class)
 	public void testImportContactsThrowConnectorNoConnectionExceptionBecouseConnectionFalse() throws ConnectorNoConnectionException, ConnectorRequestFailedException {
 		System.out.println("importContacts");
 
 		GoogleImporter instance = new GoogleImporter();
 		instance.connectionEtablished = false;
 		instance.accdata = new AbdAccount();
 
 		instance.importContacts();
 
 	}
 
 	@Test
 	public void testgetGContactFirstMailAdressWithOneAdress() {
 		System.out.println("getGContactFirstMailAdress");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		contactEntry.addEmailAddress(mail);
 
 		//call method to test and verify
 		assertEquals("test@aol.de", instance.getGContactFirstMailAdress(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactFirstMailAdressWithManyAdress() {
 		System.out.println("getGContactFirstMailAdress");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		contactEntry.addEmailAddress(mail);
 		mail.setAddress("test@fhb.de");
 		contactEntry.addEmailAddress(mail);
 		mail.setAddress("test@cks.de");
 		contactEntry.addEmailAddress(mail);
 
 		//call method to test and verify
 		assertEquals(contactEntry.getEmailAddresses().get(0).getAddress(), instance.getGContactFirstMailAdress(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactFirstMailAdressWithNoAdress() {
 		System.out.println("getGContactFirstMailAdress");
 
 		//call method to test and verify
 		GoogleImporter instance = new GoogleImporter();
 		assertEquals("", instance.getGContactFirstMailAdress(contactEntry));
 	}
 
 	@SuppressWarnings("deprecation")
 	@Test
 	public void testgetGContactBirthdayWithABirthday() {
 		System.out.println("getGContactBirthday");
 
 		GoogleImporter instance = new GoogleImporter();
 
 		//call method to test and verify
 		assertEquals(new Date(90, 4, 22), instance.getGContactBirthday(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactBirthdayWithoutABirthday() {
 		System.out.println("getGContactBirthday");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		contactEntry.setBirthday(new Birthday("---"));
 
 		//call method to test and verify
 		assertEquals(null, instance.getGContactBirthday(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactBirthdayThrowNullPointer() {
 		System.out.println("getGContactBirthday");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		contactEntry.setBirthday(null);
 
 		//call method to test and verify
 		assertEquals(null, instance.getGContactBirthday(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactFamilyname() {
 		GoogleImporter instance = new GoogleImporter();
 
 		//call method to test and verify
 		assertEquals("Peter", instance.getGContactFamilyname(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactFamilynameThrowNullPointer() {
 		GoogleImporter instance = new GoogleImporter();
 
 		contactEntry.setName(null);
 
 		//call method to test and verify
 		assertEquals("", instance.getGContactFamilyname(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactFirstname() {
 		GoogleImporter instance = new GoogleImporter();
 
 		//call method to test and verify
 		assertEquals("Hans", instance.getGContactFirstname(contactEntry));
 	}
 
 	@Test
 	public void testgetGContactFirstnameThrowNullPointer() {
 		GoogleImporter instance = new GoogleImporter();
 
 		contactEntry.setName(null);
 
 		//call method to test and verify
 		assertEquals("", instance.getGContactFirstname(contactEntry));
 	}
 
 	@Test
 	public void testMapGContacttoContactFemale() {
 		System.out.println("mapGContacttoContact");
 
 		//prepare test variables
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntry.addEmailAddress(mail);
 		@SuppressWarnings("deprecation")
 		AbdContact exptected = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		exptected.setFirstname("Hans");
 		exptected.setName("Peter");
 		exptected.setSex('w');
 		exptected.setUpdated(new Date(dateTime.getValue()));
 
 		//call method to test and verify
 		assertEquals(exptected, gImporterUnderTest.mapGContactToContact(contactEntry));
 	}
 
 	@Test
 	public void testMapGContacttoContactMale() {
 		System.out.println("mapGContacttoContact");
 
 		//prepare test variables
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntry.addEmailAddress(mail);
 		contactEntry.setGender(new Gender(Value.MALE));
 		@SuppressWarnings("deprecation")
 		AbdContact exptected = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		exptected.setFirstname("Hans");
 		exptected.setName("Peter");
 		exptected.setSex('m');
 		exptected.setUpdated(new Date(dateTime.getValue()));
 
 		//call method to test and verify
 		assertEquals(exptected, gImporterUnderTest.mapGContactToContact(contactEntry));
 	}
 
 	@Test
 	public void testMapGContacttoContactWithoutFirstname() {
 		System.out.println("mapGContacttoContact");
 
 		//prepare test variables
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntry.addEmailAddress(mail);
 		contactEntry.getName().setGivenName(null);
 
 		AbdContact exptected = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		exptected.setFirstname("");
 		exptected.setName("Peter");
 		exptected.setSex('w');
 		exptected.setUpdated(new Date(dateTime.getValue()));
 
 		//call method to test and verify
 		assertEquals(null, gImporterUnderTest.mapGContactToContact(contactEntry));
 	}
 
 	@Test
 	public void testMapGContacttoContactWithoutFamilyname() {
 		System.out.println("mapGContacttoContact");
 
 		//prepare test variables
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntry.addEmailAddress(mail);
 		contactEntry.getName().setFamilyName(null);
 
 		@SuppressWarnings("deprecation")
 		AbdContact exptected = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		exptected.setFirstname("Hans");
 		exptected.setName("");
 		exptected.setSex('w');
 		exptected.setUpdated(new Date(dateTime.getValue()));
 
 		//call method to test and verify
 		assertEquals(null, gImporterUnderTest.mapGContactToContact(contactEntry));
 	}
 
 	@Test
 	public void testMapGContacttoContactWithoutGender() {
 		System.out.println("mapGContacttoContact");
 
 		//prepare test variables
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntry.addEmailAddress(mail);
 		contactEntry.setGender(null);
 
 		@SuppressWarnings("deprecation")
 		AbdContact exptected = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		exptected.setFirstname("Hans");
 		exptected.setName("Peter");
 		exptected.setSex(null);
 		exptected.setUpdated(new Date(dateTime.getValue()));
 
 		//call method to test and verify
 		assertEquals(exptected, gImporterUnderTest.mapGContactToContact(contactEntry));
 	}
 
 	@Test
 	public void testMapGContacttoContactWithoutBirthday() {
 		System.out.println("mapGContacttoContact");
 
 		//prepare test variables
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntry.addEmailAddress(mail);
 		contactEntry.setBirthday(null);
 
 		//call method to test and verify
 		assertEquals(null, gImporterUnderTest.mapGContactToContact(contactEntry));
 	}
 
 	@Test
 	public void testGetGroupname() {
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		PlainTextConstruct title = new PlainTextConstruct();
 		title.setText("Dies ist der Titel");
 		contactGroupEntry.setTitle(title);
 
 		//call method to test and verify
 		assertEquals("Dies ist der Titel", gImporterUnderTest.getGroupName(contactGroupEntry));
 	}
 
 	@Test
 	public void testGetGroupnameThrowNullPoointer() {
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		contactGroupEntry.setTitle(null);
 
 		//call method to test and verify
 		assertEquals("", gImporterUnderTest.getGroupName(contactGroupEntry));
 	}
 
 	@Test
 	public void testMapGgroupToGroup() {
 		System.out.println("testMapGgroupToGroup");
 
 		//prepare test variables
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		AbdGroup abdGroup = new AbdGroup();
 		AbdAccount abdAccount = new AbdAccount();
 		PlainTextConstruct title = new PlainTextConstruct();
 		title.setText("Dies ist der Titel");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactGroupEntry.setTitle(title);
 		contactGroupEntry.setId("id");
 		contactGroupEntry.setUpdated(dateTime);
 		abdGroup.setActive(true);
 		abdGroup.setId("id");
 		abdGroup.setName("Dies ist der Titel");
 		abdGroup.setTemplate("Hier soll das Template rein");
 		abdGroup.setUpdated(new Date(dateTime.getValue()));
 		abdGroup.setAccount(abdAccount);
 		gImporterUnderTest.accdata = abdAccount;
 
 		//call method to test and verify
 		assertEquals(abdGroup, gImporterUnderTest.mapGGroupToGroup(contactGroupEntry));
 	}
 
 	@Test
 	public void testupdateGroups() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("testupdateGroups");
 
 		//prepare test variables
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		ContactGroupFeed resultFeed = new ContactGroupFeed();
 		List<ContactGroupEntry> contactGroupList = new ArrayList<ContactGroupEntry>();
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		AbdAccount accdata = new AbdAccount();
 		AbdAccount exceptedAccount = new AbdAccount();
 		AbdGroup abdGroup = new AbdGroup();
 
 		PlainTextConstruct title = new PlainTextConstruct();
 		title.setText("Dies ist der Titel");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactGroupEntry.setTitle(title);
 		contactGroupEntry.setId("id");
 		contactGroupEntry.setUpdated(dateTime);
 		contactGroupList.add(contactGroupEntry);
 		resultFeed.setEntries(contactGroupList);
 
 		abdGroup.setActive(true);
 		abdGroup.setId("id");
 		abdGroup.setName("Dies ist der Titel");
 		abdGroup.setTemplate("Hier soll das Template rein");
 		abdGroup.setUpdated(new Date(dateTime.getValue()));
 		abdGroup.setAccount(accdata);
 		accdata.setAbdGroupCollection(new ArrayList<AbdGroup>());
 
 		gImporterUnderTest.accdata = accdata;
 		ArrayList<AbdGroup> groupList = new ArrayList<AbdGroup>();
 		groupList.add(abdGroup);
 		exceptedAccount.setAbdGroupCollection(groupList);
 
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(resultFeed);
 		replay(myServiceMock);
 		gImporterUnderTest.myService = myServiceMock;
 
 		gImporterUnderTest.updateGroups();
 
 		//call method to test and verify
 		assertEquals(exceptedAccount, gImporterUnderTest.accdata);
 	}
 
 	@Test
 	public void testupdateGroupsWithANewGroup() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("testupdateGroupsWithANewGroup");
 
 		//prepare test variables
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		AbdGroupFacade groupDAOMock = createMock(AbdGroupFacade.class);
 		URL feedUrl;
 		ContactGroupFeed resultFeed = new ContactGroupFeed();
 		List<ContactGroupEntry> contactGroupList = new ArrayList<ContactGroupEntry>();
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		AbdAccount accdata = new AbdAccount();
 		AbdAccount exceptedAccount = new AbdAccount();
 		AbdGroup abdGroup = new AbdGroup();
 		AbdGroup abdGroupOld = new AbdGroup();
 		ArrayList<AbdGroup> groupList = new ArrayList<AbdGroup>();
 
 		PlainTextConstruct title = new PlainTextConstruct();
 		title.setText("Dies ist der Titel");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactGroupEntry.setTitle(title);
 		contactGroupEntry.setId("id");
 		contactGroupEntry.setUpdated(dateTime);
 		contactGroupList.add(contactGroupEntry);
 		resultFeed.setEntries(contactGroupList);
 
 		abdGroup.setActive(true);
 		abdGroup.setId("id1");
 		abdGroup.setName("Dies ist der Titel");
 		abdGroup.setTemplate("Hier soll das Template rein");
 		abdGroup.setUpdated(new Date(dateTime.getValue()));
 		abdGroup.setAccount(accdata);
 
 		abdGroupOld.setActive(true);
 		abdGroupOld.setId("id1");
 		abdGroupOld.setName("Dies ist der Titel");
 		abdGroupOld.setTemplate("Hier soll das Template rein");
 		abdGroupOld.setUpdated(new Date(dateTime.getValue()));
 		abdGroupOld.setAccount(accdata);
 
 		groupList.add(abdGroupOld);
 		accdata.setAbdGroupCollection(groupList);
 
 		gImporterUnderTest.accdata = accdata;
 		groupList = new ArrayList<AbdGroup>();
 		groupList.add(abdGroup);
 		groupList.add(abdGroupOld);
 		exceptedAccount.setAbdGroupCollection(groupList);
 
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(resultFeed);
 		groupDAOMock.remove((AbdGroup) EasyMock.anyObject());
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		replay(groupDAOMock);
 
 		//call method to test and verify
 		gImporterUnderTest.myService = myServiceMock;
 		gImporterUnderTest.groupDAO = groupDAOMock;
 		gImporterUnderTest.updateGroups();
 		assertEquals(exceptedAccount, gImporterUnderTest.accdata);
 	}
 
 	@Test
 	public void testupdateGroupsWithNoUpdates() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("testupdateGroupsWithNoUpdates");
 
 		//prepare test variables
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		ContactGroupFeed resultFeed = new ContactGroupFeed();
 		List<ContactGroupEntry> contactGroupList = new ArrayList<ContactGroupEntry>();
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		AbdAccount accdata = new AbdAccount();
 		AbdAccount exceptedAccount = new AbdAccount();
 		AbdGroup abdGroup = new AbdGroup();
 		ArrayList<AbdGroup> groupList = new ArrayList<AbdGroup>();
 
 		PlainTextConstruct title = new PlainTextConstruct();
 		title.setText("Dies ist der Titel");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactGroupEntry.setTitle(title);
 		contactGroupEntry.setId("id");
 		contactGroupEntry.setUpdated(dateTime);
 		contactGroupList.add(contactGroupEntry);
 		resultFeed.setEntries(contactGroupList);
 
 		abdGroup.setActive(true);
 		abdGroup.setId("id");
 		abdGroup.setName("Dies ist der Titel");
 		abdGroup.setTemplate("Hier soll das Template rein");
 		abdGroup.setUpdated(new Date(dateTime.getValue()));
 		abdGroup.setAccount(accdata);
 
 		groupList.add(abdGroup);
 		accdata.setAbdGroupCollection(groupList);
 
 		gImporterUnderTest.accdata = accdata;
 		groupList = new ArrayList<AbdGroup>();
 		groupList.add(abdGroup);
 		exceptedAccount.setAbdGroupCollection(groupList);
 
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(resultFeed);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 
 		//call method to test and verify
 		gImporterUnderTest.myService = myServiceMock;
 		gImporterUnderTest.updateGroups();
 		assertEquals(exceptedAccount, gImporterUnderTest.accdata);
 	}
 
 	@Test
 	public void testupdateGroupsWithAUpdateGroup() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("testupdateGroupsWithAUpdateGroup");
 
 		//prepare test variables
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		AbdGroupFacade groupDAO = createMock(AbdGroupFacade.class);
 		URL feedUrl;
 		ContactGroupFeed resultFeed = new ContactGroupFeed();
 		List<ContactGroupEntry> contactGroupList = new ArrayList<ContactGroupEntry>();
 		ContactGroupEntry contactGroupEntry = new ContactGroupEntry();
 		AbdAccount accdata = new AbdAccount();
 		AbdAccount exceptedAccount = new AbdAccount();
 		AbdGroup abdGroup = new AbdGroup();
 		AbdGroup abdGroupOld = new AbdGroup();
 		ArrayList<AbdGroup> groupList = new ArrayList<AbdGroup>();
 
 		PlainTextConstruct title = new PlainTextConstruct();
 		title.setText("Dies ist der Titel");
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactGroupEntry.setTitle(title);
 		contactGroupEntry.setId("id");
 		contactGroupEntry.setUpdated(dateTime);
 		contactGroupList.add(contactGroupEntry);
 		resultFeed.setEntries(contactGroupList);
 
 		abdGroup.setActive(true);
 		abdGroup.setId("id");
 		abdGroup.setName("Dies ist der Titel");
 		abdGroup.setTemplate("Hier soll das Template rein");
 		abdGroup.setUpdated(new Date(dateTime.getValue()));
 		abdGroup.setAccount(accdata);
 
 		abdGroupOld.setActive(true);
 		abdGroupOld.setId("id");
 		abdGroupOld.setName("Dies ist der Titel23");
 		abdGroupOld.setTemplate("Hier soll das Template rein");
 		abdGroupOld.setUpdated(new Date(dateTime.getValue() - 10));
 		abdGroupOld.setAccount(accdata);
 
 		groupList.add(abdGroupOld);
 		accdata.setAbdGroupCollection(groupList);
 
 		gImporterUnderTest.accdata = accdata;
 		groupList = new ArrayList<AbdGroup>();
 		groupList.add(abdGroup);
 		exceptedAccount.setAbdGroupCollection(groupList);
 
 		feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactGroupFeed.class)).andReturn(resultFeed);
 		groupDAO.edit(abdGroup);
 		groupDAO.flush();
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		replay(groupDAO);
 
 		//call method to test and verify
 		gImporterUnderTest.myService = myServiceMock;
 		gImporterUnderTest.groupDAO = groupDAO;
 		gImporterUnderTest.updateGroups();
 		assertEquals(exceptedAccount, gImporterUnderTest.accdata);
 	}
 
 	@Test
 	public void updateContactWithExistContact() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("updateContactWithExistContact");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		AbdContactFacade contactDAOMock = createMock(AbdContactFacade.class);
 		AbdGroupFacade groupDAO = createMock(AbdGroupFacade.class);
 		AbdGroupToContactFacade groupTOContactDAO = createMock(AbdGroupToContactFacade.class);
 		URL feedUrl;
 		List<ContactEntry> contactEntryList = new ArrayList<ContactEntry>();
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		contactEntry.addEmailAddress(mail);
 		contactEntryList.add(contactEntry);
 		ContactFeed resultFeed = new ContactFeed();
 		resultFeed.setEntries(contactEntryList);
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		@SuppressWarnings("deprecation")
 		AbdContact abdContactInDB = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		abdContactInDB.setFirstname("Hans");
 		abdContactInDB.setName("Peter");
 		abdContactInDB.setSex('w');
 		abdContactInDB.setUpdated(new Date(dateTime.getValue()));
 		abdContactInDB.setAbdGroupToContactCollection(new ArrayList<AbdGroupToContact>());
 
 		System.out.println(abdContactInDB);
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(resultFeed);
 		expect(contactDAOMock.find("1")).andReturn(abdContactInDB);
 		contactDAOMock.flush();
 		groupDAO.flush();
 		groupTOContactDAO.flush();
 		contactDAOMock.edit(abdContactInDB);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		replay(contactDAOMock);
 		replay(groupTOContactDAO);
 		instance.contactDAO = contactDAOMock;
 		instance.myService = myServiceMock;
 		instance.groupToContactDAO = groupTOContactDAO;
 		instance.updateContacts();
 	}
 
 	@Test
	@Ignore
 	public void updateContactWithEditContact() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("updateContactWithEditContact");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		AbdContactFacade contactDAOMock = createMock(AbdContactFacade.class);
 		URL feedUrl;
 		List<ContactEntry> contactEntryList = new ArrayList<ContactEntry>();
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		contactEntry.addEmailAddress(mail);
 		contactEntryList.add(contactEntry);
 		ContactFeed resultFeed = new ContactFeed();
 		resultFeed.setEntries(contactEntryList);
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		@SuppressWarnings("deprecation")
 		AbdContact abdContactInDB = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		abdContactInDB.setFirstname("Hans");
 		abdContactInDB.setName("Peter");
 		abdContactInDB.setSex('w');
 		abdContactInDB.setUpdated(new Date(dateTime.getValue() - 10));
 
 		System.out.println(abdContactInDB);
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(resultFeed);
 		expect(contactDAOMock.find("1")).andReturn(abdContactInDB);
 		contactDAOMock.edit(abdContactInDB);
 		contactDAOMock.flush();
 		contactDAOMock.flush();
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		replay(contactDAOMock);
 		instance.contactDAO = contactDAOMock;
 		instance.myService = myServiceMock;
 		instance.updateContacts();
 	}
 
 	@Test
 	public void updateContactWithNewContact() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("updateContactWithNewContact");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		GroupMembershipInfo membership = new GroupMembershipInfo();
 		AbdGroup abdGroup = new AbdGroup();
 		AbdAccount accdata = new AbdAccount();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		AbdContactFacade contactDAOMock = createMock(AbdContactFacade.class);
 		AbdGroupToContactFacade groupToContactDAOMock = createMock(AbdGroupToContactFacade.class);
 		AbdGroupFacade groupDAOMock = createMock(AbdGroupFacade.class);
 		List<AbdGroup> abdGroups = new ArrayList<AbdGroup>();
 		URL feedUrl;
 		List<ContactEntry> contactEntryList = new ArrayList<ContactEntry>();
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		Email mail = new Email();
 		mail.setAddress("test@aol.de");
 		contactEntry.addEmailAddress(mail);
 		membership.setHref("1");
 		contactEntry.addGroupMembershipInfo(membership);
 		contactEntryList.add(contactEntry);
 		ContactFeed resultFeed = new ContactFeed();
 		resultFeed.setEntries(contactEntryList);
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 		abdGroup.setActive(true);
 		abdGroup.setId("1");
 		abdGroup.setName("Dies ist der Titel");
 		abdGroup.setTemplate("Hier soll das Template rein");
 		abdGroup.setUpdated(new Date(dateTime.getValue()));
 		abdGroup.setAccount(accdata);
 		abdGroup.setAbdGroupToContactCollection(new ArrayList<AbdGroupToContact>());
 		abdGroups.add(abdGroup);
 		accdata.setAbdGroupCollection(abdGroups);
 
 		@SuppressWarnings("deprecation")
 		AbdContact exptected = new AbdContact("1", "test@fhb.de", new Date(90, 4, 22), "");
 		exptected.setFirstname("Hans");
 		exptected.setName("Peter");
 		exptected.setSex('w');
 		exptected.setUpdated(new Date(dateTime.getValue()));
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(resultFeed);
 		expect(contactDAOMock.find(contactEntry.getId())).andReturn(null);
 		expect(groupDAOMock.find("1")).andReturn(new AbdGroup("1"));
 		contactDAOMock.create((AbdContact) EasyMock.anyObject(AbdContact.class));
 		contactDAOMock.edit((AbdContact) EasyMock.anyObject());
 		contactDAOMock.flush();
 		contactDAOMock.flush();
 		groupToContactDAOMock.create((AbdGroupToContact) EasyMock.anyObject());
 		groupToContactDAOMock.edit((AbdGroupToContact) EasyMock.anyObject());
 		groupToContactDAOMock.flush();
 		groupToContactDAOMock.flush();
 		groupDAOMock.edit((AbdGroup) EasyMock.anyObject());
 		groupDAOMock.flush();
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		replay(contactDAOMock);
 		replay(groupToContactDAOMock);
 		replay(groupDAOMock);
 		instance.contactDAO = contactDAOMock;
 		instance.myService = myServiceMock;
 		instance.groupToContactDAO = groupToContactDAOMock;
 		instance.groupDAO = groupDAOMock;
 		instance.accdata = accdata;
 		instance.updateContacts();
 	}
 
 	@Test
 	public void updateContactMappedObjectNull() throws IOException, ServiceException, ConnectorRequestFailedException {
 		System.out.println("updateContactMappedObjectNull");
 
 		//prepare test variables
 		GoogleImporter instance = new GoogleImporter();
 		ContactsService myServiceMock = createMock(ContactsService.class);
 		URL feedUrl;
 		List<ContactEntry> contactEntryList = new ArrayList<ContactEntry>();
 		DateTime dateTime = new DateTime();
 		dateTime = DateTime.now();
 		contactEntry.setUpdated(dateTime);
 		contactEntryList.add(contactEntry);
 		ContactFeed resultFeed = new ContactFeed();
 		resultFeed.setEntries(contactEntryList);
 		feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 
 		// Setting up the expected value of the method call of Mockobject
 		expect(myServiceMock.getFeed(feedUrl, ContactFeed.class)).andReturn(resultFeed);
 
 		// Setup is finished need to activate the mock
 		replay(myServiceMock);
 		instance.myService = myServiceMock;
 		instance.updateContacts();
 	}
 }
