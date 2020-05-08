 package de.fhb.autobday.manager.connector.google;
 
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.contacts.Gender.Value;
 import com.google.gdata.data.contacts.*;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.util.ServiceException;
 import de.fhb.autobday.commons.GoogleBirthdayConverter;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.dao.AbdGroupToContactFacade;
 import de.fhb.autobday.data.*;
 import de.fhb.autobday.exception.CanNotConvetGoogleBirthdayException;
 import de.fhb.autobday.exception.connector.ConnectorCouldNotLoginException;
 import de.fhb.autobday.exception.connector.ConnectorInvalidAccountException;
 import de.fhb.autobday.exception.connector.ConnectorNoConnectionException;
 import de.fhb.autobday.manager.LoggerInterceptor;
 import de.fhb.autobday.manager.connector.AImporter;
 
 import java.io.IOException;
 import java.net.URL;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.interceptor.Interceptors;
 
 /**
  * class to import the contact information and map the contacts to us contacts
  * write the contact information in the database
  *
  * variablename if the variable start with abd this is an Auto-B-Day model
  *
  * @author Tino Reuschel <reuschel@fh-brandenburg.de>
  */
 @Stateless
 @LocalBean
 @Interceptors(LoggerInterceptor.class)
 public class GoogleImporter extends AImporter {
 
 	private final static Logger LOGGER = Logger.getLogger(GoogleImporter.class.getName());
 	protected boolean connectionEtablished;
 	protected AbdAccount accdata;
 	protected ContactsService myService;
 	@EJB
 	protected AbdContactFacade contactDAO;
 	@EJB
 	protected AbdGroupFacade groupDAO;
 	@EJB
 	protected AbdGroupToContactFacade groupToContactDAO;
 	@EJB
 	private AbdAccountFacade accountDAO;
 
 	public GoogleImporter() {
 		connectionEtablished = false;
 		accdata = null;
 		myService = null;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 *
 	 * @see
 	 * de.fhb.autobday.manager.connector.AImporter#getConnection(de.fhb.autobday.data.AbdAccount)
 	 * create connection to google contact api
 	 */
 	@Override
 	public void getConnection(AbdAccount data)
 			throws ConnectorCouldNotLoginException,
 			ConnectorInvalidAccountException {
 
 		connectionEtablished = false;
 
 		System.out.println("Account: " + data);
 
 		if (data == null) {
 			throw new ConnectorInvalidAccountException();
 		}
 		accdata = data;
 		// testausgabe
 		System.out.println("Username: " + accdata.getUsername());
 		System.out.println("Passwort: " + accdata.getPasswort());
 
 		// connect to google
 		try {
 			myService = new ContactsService("BDayReminder");
 			myService.setUserCredentials(accdata.getUsername(),
 					accdata.getPasswort());
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			throw new ConnectorCouldNotLoginException(
 					"Importer cant connect to the account!");
 		}
 		connectionEtablished = true;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 *
 	 * @see de.fhb.autobday.manager.connector.AImporter#importContacts()
 	 */
 	@Override
 	public void importContacts() throws ConnectorNoConnectionException {
 
 		// if we have a connection and a valid accounddata then import the
 		// contacts and groups
 		// else throw an exception
 		if (connectionEtablished && accdata != null) {
 			accountDAO.flush();
 
 			updateGroups();
 			updateContacts();
 
 			accountDAO.edit(accdata);
 
 		} else {
 			throw new ConnectorNoConnectionException();
 		}
 	}
 
 	/**
 	 * Methode that update the Contact of an Account
 	 */
 	protected void updateContacts() {
 		AbdContact abdContact, abdContactInDB;
 		List<ContactEntry> contacts = getAllContacts();
 		List<GroupMembershipInfo> groupMembershipInfos;
 		AbdGroupToContact abdGroupToContact;
 		AbdGroupToContactPK gtcPK;
 
 		int membershipCounter;
 
 		for (ContactEntry contactEntry : contacts) {
 			abdContact = mapGContactToContact(contactEntry);
 			if (abdContact != null) {
 				System.out.println("Mapped Contact: " + abdContact);
 
 				abdContactInDB = contactDAO.find(abdContact.getId());
 				System.out.println("Contact in db: " + abdContactInDB);
 				if (abdContactInDB == null) {
 					groupMembershipInfos = contactEntry.getGroupMembershipInfos();
 					membershipCounter = 0;
 					for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
 						membershipCounter++;
 						for (AbdGroup abdGroup : accdata.getAbdGroupCollection()) {
 							System.out.println("testbla");
 							if (abdGroup.getId().equals(groupMembershipInfo.getHref())) {
 								System.out.println("bla2");
 								contactDAO.create(abdContact);
 								contactDAO.flush();
 								System.out.println("bla34");
 								abdGroupToContact = new AbdGroupToContact();
 
 								gtcPK = new AbdGroupToContactPK(
 										abdGroup.getId(),
 										abdContact.getId());
 								abdGroupToContact.setAbdGroupToContactPK(gtcPK);
 
 								abdGroupToContact.setAbdContact(abdContact);
 								if (membershipCounter > 1) {
 									abdGroupToContact.setActive(false);
 								} else {
 									abdGroupToContact.setActive(true);
 								}
 
 								abdGroupToContact.setAbdGroup(abdGroup);
 
 								System.out.println("Contact: "
 										+ abdGroupToContact.getAbdContact());
 								System.out.println("Group: "
 										+ abdGroupToContact.getAbdGroup());
 								System.out.println("Active: "
 										+ abdGroupToContact.getActive());
 								System.out.println("ContactPK: "
 										+ abdGroupToContact.getAbdGroupToContactPK().getContact());
 								System.out.println("GroupPK: "
 										+ abdGroupToContact.getAbdGroupToContactPK().getGroup());
 
 								groupToContactDAO.create(abdGroupToContact);
 								groupToContactDAO.flush();
 
 								abdGroup.getAbdGroupToContactCollection().add(abdGroupToContact);
 
 								groupDAO.edit(abdGroup);
 								groupDAO.flush();
 							}
 						}
 					}
 				} else {
 					if (abdContact.getUpdated().after(abdContactInDB.getUpdated())) {
 						contactDAO.edit(abdContact);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method that update the Groups of an Account
 	 */
 	protected void updateGroups() {
 		AbdGroup abdGroup;
 		List<ContactGroupEntry> groups = getAllGroups();
 		List<AbdGroup> abdGroups = new ArrayList<AbdGroup>(
 				accdata.getAbdGroupCollection());
 
 		boolean foundMatch = false;
 		for (ContactGroupEntry contactGroupEntry : groups) {
 			abdGroup = mapGGroupToGroup(contactGroupEntry);
 			System.out.println("Found GroupID: " + abdGroup + " Name: "+ abdGroup.getName());
 			// iterare over old groups
 			for (AbdGroup abdGroupOld : abdGroups) {
 				foundMatch = false;
 				// if new group == old group
 				if (abdGroup.getId().equals(abdGroupOld.getId())) {
 					foundMatch = true;
 					// and if new group newer than the old group
 					if (abdGroup.getUpdated().after(abdGroupOld.getUpdated())) {
 						abdGroups.remove(abdGroupOld);
 						abdGroups.add(abdGroup);
 					}
 
 				}
 			}
 			if (!foundMatch) {
 				abdGroups.add(abdGroup);
 			}
 
 		}
 		accdata.setAbdGroupCollection(abdGroups);
 	}
 
 	/**
 	 * get all groups from google from the connected account
 	 *
 	 * if dont get information from google return null else a list of Google
 	 * ContactGroupEntrys
 	 *
 	 */
 	protected List<ContactGroupEntry> getAllGroups() {
 
 		URL feedUrl;
 		try {
 			// url to get all groups
 			feedUrl = new URL(
 					"https://www.google.com/m8/feeds/groups/default/full");
 			ContactGroupFeed resultFeed = myService.getFeed(feedUrl,
 					ContactGroupFeed.class);
 			if (resultFeed == null) {
 				return null;
 			}
 			return resultFeed.getEntries();
 
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return null;
 	}
 
 	/**
 	 * get all contacts from the connected acoount
 	 *
 	 * if dont get information from google return null else a list of Google
 	 * ContactEntrys
 	 */
 	protected List<ContactEntry> getAllContacts() {
 
 		URL feedUrl;
 		try {
 			feedUrl = new URL(
 					"https://www.google.com/m8/feeds/contacts/default/full");
 			ContactFeed resultFeed = myService.getFeed(feedUrl,
 					ContactFeed.class);
 			if (resultFeed == null) {
 				return null;
 			}
 			return resultFeed.getEntries();
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return null;
 	}
 
 	/**
 	 * methode to map a google contact to a auto-b-day contact
 	 *
 	 * @param ContactEntry contactEntry
 	 * @return AbdContact
 	 */
 	protected AbdContact mapGContactToContact(ContactEntry contactEntry) {
 
 		AbdContact abdContact;
 		String firstname;
 		String name;
 		Date birthday;
 		String mailadress;
 		String id;
 		Date updated;
 
 		abdContact = new AbdContact();
 		System.out.println("-------------------------------------------------");
 		abdContact.setHashid("12");
 
 		id = contactEntry.getId();
 		System.out.println("ID: " + id);
 		abdContact.setId(id);
 
 		firstname = getGContactFirstname(contactEntry);
 		System.out.println("Firstname: " + firstname);
		if (firstname.equals("")) {
 			abdContact.setFirstname(firstname);
 		} else {
 			System.err.println("Skipping current Contact: No Firstname");
 			return null;
 		}
 
 		name = getGContactFamilyname(contactEntry);
 		System.out.println("Name: " + name);
		if (name.equals("")) {
 			abdContact.setName(name);
 		} else {
 			System.err.println("Skipping current Contact: No Name");
 			return null;
 		}
 
 		birthday = getGContactBirthday(contactEntry);
 		System.out.println("birthday: " + birthday);
 		if (birthday != null) {
 			abdContact.setBday(birthday);
 		} else {
 			System.err.println("Skipping current Contact: No Bday");
 			return null;
 		}
 
 		if (!contactEntry.getEmailAddresses().isEmpty()) {
 			mailadress = getGContactFirstMailAdress(contactEntry);
 			abdContact.setMail(mailadress);
 		} else {
 			System.err.println("Skipping current Contact: No Mail");
 			return null;
 		}
 
 		try {
 			if (contactEntry.getGender().getValue() == Value.FEMALE) {
 				abdContact.setSex('w');
 			} else {
 				abdContact.setSex('m');
 			}
 		} catch (NullPointerException ex) {
 			abdContact.setSex(null);
 		}
 
 		updated = new Date(contactEntry.getUpdated().getValue());
 		abdContact.setUpdated(updated);
 		return abdContact;
 	}
 
 	/**
 	 * Method that map a GoogleGroup to a Auto-B-Day-Group
 	 *
 	 * @param contactGroupEntry
 	 * @return AbdGroup
 	 */
 	protected AbdGroup mapGGroupToGroup(ContactGroupEntry contactGroupEntry) {
 		AbdGroup abdGroupEntry;
 		// TODO Template einbinden
 		String template = "Herzlichen Glückwunsch zum Geburtstag ${firstname}.";
 
 		abdGroupEntry = new AbdGroup();
 		abdGroupEntry.setId(contactGroupEntry.getId());
 		abdGroupEntry.setName(getGroupName(contactGroupEntry));
 		abdGroupEntry.setAccount(accdata);
 		abdGroupEntry.setActive(true);
 		abdGroupEntry.setTemplate(template);
 		abdGroupEntry.setUpdated(new Date(contactGroupEntry.getUpdated().getValue()));
 
 		return abdGroupEntry;
 	}
 
 	/**
 	 * methode that return the firstname of a given contact
 	 *
 	 * @param ContactEntry contactEntry
 	 *
 	 * @return String
 	 */
 	protected String getGContactFirstname(ContactEntry contactEntry) {
 		String firstname = "";
 		try {
 			firstname = contactEntry.getName().getGivenName().getValue();
 		} catch (NullPointerException ex) {
 			// LOGGER.log(Level.SEVERE, null, ex);
 		}
 
 		return firstname;
 
 	}
 
 	/**
 	 * methode that return the familyname of a given Contact
 	 *
 	 * @param ContactEntry contactEntry
 	 *
 	 * @return String
 	 */
 	protected String getGContactFamilyname(ContactEntry contactEntry) {
 		String familyname = "";
 		try {
 			familyname = contactEntry.getName().getFamilyName().getValue();
 		} catch (NullPointerException ex) {
 			// LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return familyname;
 
 	}
 
 	/**
 	 * methode that return the birthday of a given Contact
 	 *
 	 * @param ContactEntry contactEntry
 	 *
 	 * @ return Date
 	 */
 	protected Date getGContactBirthday(ContactEntry contactEntry) {
 		String gContactBirthday = null;
 		Date bday = null;
 		try {
 			gContactBirthday = contactEntry.getBirthday().getValue();
 			bday = GoogleBirthdayConverter.convertBirthday(gContactBirthday);
 		} catch (CanNotConvetGoogleBirthdayException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 		} catch (NullPointerException ex) {
 			// LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return bday;
 	}
 
 	/**
 	 * Methode that return a mailadress of a given Contact
 	 *
 	 * @param ContactEntry contactEntry
 	 *
 	 * @return String
 	 */
 	protected String getGContactFirstMailAdress(ContactEntry contactEntry) {
 		List<Email> mailadresses;
 		String mailadress;
 		mailadresses = contactEntry.getEmailAddresses();
 		if (mailadresses.size() > 0) {
 			mailadress = mailadresses.get(0).getAddress();
 			return mailadress;
 		}
 		return "";
 	}
 
 	/**
 	 * Methode that return the Groupname from a GoogleGroup
 	 *
 	 * @param contactGroupEntry
 	 * @return String
 	 */
 	protected String getGroupName(ContactGroupEntry contactGroupEntry) {
 		String groupName = "";
 
 		try {
 			groupName = contactGroupEntry.getTitle().getPlainText();
 		} catch (NullPointerException ex) {
 			// LOGGER.log(Level.SEVERE, null, ex);
 		}
 		return groupName;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 *
 	 * @see de.fhb.autobday.manager.connector.AImporter#isConnectionEtablished()
 	 */
 	@Override
 	public boolean isConnectionEtablished() {
 		return connectionEtablished;
 	}
 }
