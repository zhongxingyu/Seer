 package de.fhb.autobday.manager.connector.google;
 
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.contacts.Gender.Value;
 import com.google.gdata.data.contacts.*;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.util.ServiceException;
 import de.fhb.autobday.commons.CipherHelper;
 import de.fhb.autobday.commons.GoogleBirthdayConverter;
 import de.fhb.autobday.commons.PropertyLoader;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.dao.AbdGroupToContactFacade;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdGroupToContact;
 import de.fhb.autobday.exception.commons.CanNotConvetGoogleBirthdayException;
 import de.fhb.autobday.exception.commons.CouldNotDecryptException;
 import de.fhb.autobday.exception.commons.CouldNotLoadMasterPasswordException;
 import de.fhb.autobday.exception.connector.ConnectorCouldNotLoginException;
 import de.fhb.autobday.exception.connector.ConnectorInvalidAccountException;
 import de.fhb.autobday.exception.connector.ConnectorNoConnectionException;
 import de.fhb.autobday.manager.LoggerInterceptor;
 import java.io.IOException;
 import java.net.URL;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.interceptor.Interceptors;
 
 /**
  * class to import the contact information and map the contacts to us contacts
  * write the contact information in the database
  *
  * variablename if the variable start with abd this is an Auto-B-Day model
  *
  * @author Tino Reuschel mail: reuschel@fh-brandenburg.de
  */
 @Stateless
 @Local
 @Interceptors(LoggerInterceptor.class)
 public class GoogleImporter implements GoogleImporterLocal {
 
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
 	protected AbdAccountFacade accountDAO;
 	
 	private PropertyLoader propLoader;
 	private List<String> errorStack = new ArrayList();
 
 	public GoogleImporter() {
 		connectionEtablished = false;
 		accdata = null;
 		myService = null;
 		propLoader = new PropertyLoader();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @param data
	 * @throws CouldNotDecryptException 
	 * @throws CouldNotLoadMasterPasswordException 
 	 * @see
 	 * de.fhb.autobday.manager.connector.AImporter#getConnection(de.fhb.autobday.data.AbdAccount)
 	 * create connection to google contact api
 	 */
 	@Override
 	public void getConnection(AbdAccount data)
 			throws ConnectorCouldNotLoginException,
 			ConnectorInvalidAccountException, CouldNotDecryptException, CouldNotLoadMasterPasswordException {
 
 		String password = "";
 		Properties masterPassword;
 		connectionEtablished = false;
 
 		LOGGER.log(Level.INFO, "Account: {0}", data);
 
 		if (data == null) {
 			throw new ConnectorInvalidAccountException();
 		}
 		accdata = data;
 		// testausgabe
 		LOGGER.log(Level.INFO, "Username: {0}", accdata.getUsername());
 		LOGGER.log(Level.INFO, "Passwort: {0}", accdata.getPasswort());
 
 		//load master password
 		try {
 			masterPassword = propLoader.loadSystemProperty("/SystemCipherPassword.properties");
 			password = CipherHelper.decipher(accdata.getPasswort(), masterPassword.getProperty("master"));
 		} catch (IOException e) {
			throw new CouldNotLoadMasterPasswordException();//TODO Message + Logger
 		} catch (InvalidKeyException e) {
			throw new CouldNotDecryptException();//TODO Message + Logger
 		} catch (NoSuchAlgorithmException e) {
			throw new CouldNotDecryptException();//TODO Message + Logger
 		} catch (NoSuchPaddingException e) {
			throw new CouldNotDecryptException();//TODO Message + Logger
 		} catch (IllegalBlockSizeException e) {
			throw new CouldNotDecryptException();//TODO Message + Logger
 		} catch (BadPaddingException e) {
			throw new CouldNotDecryptException();//TODO Message + Logger
 		}
 
 		// connect to google
 		try {
 			myService = new ContactsService("BDayReminder");
 
 			myService.setUserCredentials(accdata.getUsername(),
 					password);
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());
 			throw new ConnectorCouldNotLoginException(
 					"Importer cant connect to the account!");
 		}
 		connectionEtablished = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @return errorStack
 	 * @see de.fhb.autobday.manager.connector.AImporter#importContacts()
 	 */
 	@Override
 	public List<String> importContacts() throws ConnectorNoConnectionException {
 		errorStack = new ArrayList<String>();
 		// if we have a connection and a valid accounddata then import the
 		// contacts and groups
 		// else throw an exception
 		if (connectionEtablished && accdata != null) {
 			accountDAO.flush();
 
 			updateGroups();
 			accountDAO.edit(accdata);
 			accountDAO.refresh(accdata);
 			updateContacts();
 
 
 		} else {
 			throw new ConnectorNoConnectionException("Connection to Google failed");
 		}
 		return errorStack;
 	}
 
 	/**
 	 * Methode that update the Contact of an Account
 	 */
 	protected void updateContacts() {
 		AbdContact abdContact, abdContactInDB;
 		List<ContactEntry> contacts = getAllContacts();
 		int counter = 0;
 
 		LOGGER.log(Level.INFO, "Updating Contacts!");
 		LOGGER.log(Level.INFO, "{0} Contacts in queue!", contacts.size());
 		for (ContactEntry contactEntry : contacts) {
 			counter++;
 			abdContact = mapGContactToContact(contactEntry);
 			if (abdContact != null) {
 				LOGGER.log(Level.INFO, "Mapped Contact {0}: {1}", new Object[]{counter, abdContact});
 
 				abdContactInDB = contactDAO.find(abdContact.getId());
 
 				if (abdContactInDB == null) {
 					LOGGER.log(Level.INFO, "Adding Contact to db!");
 					contactDAO.create(abdContact);
 					contactDAO.flush();
 				} else {
 					LOGGER.log(Level.INFO, "Contact already in db: {0}", abdContactInDB);
 					abdContact.setAbdGroupToContactCollection(abdContactInDB.getAbdGroupToContactCollection());
 					if (abdContact.getUpdated().after(abdContactInDB.getUpdated())) {
 						LOGGER.log(Level.INFO, "Updating Contact in db!");
 						contactDAO.edit(abdContact);
 						contactDAO.flush();
 					}
 				}
 				contactDAO.flush();
 				updateMembership(abdContact, contactEntry);
 			} else {
 				LOGGER.log(Level.INFO, "Failed to map Contact {0}!", counter);
 			}
 		}
 
 	}
 
 	/**
 	 * Method that update the Groups of an Account
 	 */
 	protected void updateGroups() {
 		AbdGroup abdGroup;
 		AbdGroup abdGroupOld;
 		List<ContactGroupEntry> groups = getAllGroups();
 		List<AbdGroup> abdGroups = new ArrayList<AbdGroup>(
 				accdata.getAbdGroupCollection());
 		List<Integer> toRemove = new ArrayList<Integer>();
 
 		boolean foundMatch = false;
 
 		for (ContactGroupEntry contactGroupEntry : groups) {
 			abdGroup = mapGGroupToGroup(contactGroupEntry);
 			LOGGER.log(Level.INFO, "Found GroupID: {0} Name: {1}", new Object[]{abdGroup, abdGroup.getName()});
 			// iterare over old groups
 			foundMatch = false;
 			for (int i = 0; i < abdGroups.size(); i++) {
 				abdGroupOld = abdGroups.get(i);
 				// if new group == old group
 				if (abdGroup.getId().equals(abdGroupOld.getId())) {
 					LOGGER.log(Level.INFO, "Group already in DB!");
 					foundMatch = true;
 					// and if new group newer than the old group
 					if (abdGroup.getUpdated().after(abdGroupOld.getUpdated())) {
 						LOGGER.log(Level.INFO, "Updating Group!");
 						groupDAO.edit(abdGroup);
 						groupDAO.flush();
 						abdGroups.remove(i);
 						abdGroups.add(i, abdGroup);
 					}
 
 				}
 			}
 			if (!foundMatch) {
 				LOGGER.log(Level.INFO, "Creating new Group!");
 				abdGroups.add(abdGroup);
 
 			}
 
 		}
 
 		for (AbdGroup abdGroupInDB : abdGroups) {
 			foundMatch = false;
 			for (ContactGroupEntry groupEntry : groups) {
 				if (abdGroupInDB.getId().equals(groupEntry.getId())) {
 					foundMatch = true;
 				}
 			}
 			if (!foundMatch) {
 				toRemove.add(0, abdGroups.indexOf(abdGroupInDB));
 			}
 		}
 
 		for (Integer index : toRemove) {
 			groupDAO.remove(abdGroups.get(index.intValue()));
 			abdGroups.remove(index.intValue());
 		}
 
 		accdata.setAbdGroupCollection(abdGroups);
 	}
 
 	/**
 	 * methode that update the relationship between contact and group
 	 * 
 	 * @param abdContact
 	 * @param contactEntry 
 	 */
 	public void updateMembership(AbdContact abdContact, ContactEntry contactEntry) {
 
 		List<GroupMembershipInfo> groupMembershipInfos = contactEntry.getGroupMembershipInfos();
 		List<AbdGroupToContact> abdMemberships = new ArrayList<AbdGroupToContact>(abdContact.getAbdGroupToContactCollection());
 		List<Integer> toRemove = new ArrayList<Integer>();
 		AbdGroupToContact abdMembership;
 		AbdGroup abdGroup;
 
 		boolean match = false;
 
 		for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
 			match = false;
 			for (AbdGroupToContact abdGroupToContact : abdMemberships) {
 
 				if (abdGroupToContact.getAbdGroup().getId().equals(groupMembershipInfo.getHref())) {
 					match = true;
 				}
 			}
 			if (!match) {
 				abdGroup = groupDAO.find(groupMembershipInfo.getHref());
 				abdMembership = new AbdGroupToContact(groupMembershipInfo.getHref(), abdContact.getId());
 				abdMembership.setAbdGroup(abdGroup);
 				abdMembership.setAbdContact(abdContact);
 				abdMemberships.add(abdMembership);
 			}
 		}
 
 		for (AbdGroupToContact abdGroupToContact : abdMemberships) {
 			match = false;
 			for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
 				if (abdGroupToContact.getAbdGroup().getId().equals(groupMembershipInfo.getHref())) {
 					match = true;
 				}
 			}
 			if (!match) {
 				toRemove.add(0, abdMemberships.indexOf(abdGroupToContact));
 			}
 		}
 		
 		groupToContactDAO.flush();
 		for (Integer index : toRemove) {
 			LOGGER.log(Level.INFO, "remove {0}", index.intValue());
 			abdMembership = abdMemberships.get(index.intValue());
 			if (groupDAO.find(abdMembership.getAbdGroup().getId()) != null) {
 				groupToContactDAO.remove(abdMemberships.get(index.intValue()));
 			}
 			abdMemberships.remove(index.intValue());
 		}
 
 		match = false;
 		for (AbdGroupToContact abdGroupToContact : abdMemberships) {
 			if (abdGroupToContact.getActive()) {
 				match = true;
 			}
 		}
 		if (!match) {
 			abdMemberships.get(0).setActive(true);
 			groupToContactDAO.edit(abdMemberships.get(0));
 			groupToContactDAO.flush();
 		}
 
 		abdContact.setAbdGroupToContactCollection(abdMemberships);
 		contactDAO.edit(abdContact);
 
 	}
 
 	/**
 	 * get all groups from google from the connected account
 	 *
 	 * if dont get information from google return null else a list of Google
 	 * ContactGroupEntrys
 	 *
 	 *
 	 * @return List<ContactGroupEntry>
 	 */
 	protected List<ContactGroupEntry> getAllGroups() {
 
 		URL feedUrl;
 		try {
 			// url to get all groups
 			feedUrl = new URL(
 					"https://www.google.com/m8/feeds/groups/default/full?max-results=500");
 			ContactGroupFeed resultFeed = myService.getFeed(feedUrl,
 					ContactGroupFeed.class);
 			if (resultFeed == null) {
 				return null;
 			}
 			return resultFeed.getEntries();
 
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());//TODO Add Message
 			//TODO Exception
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());//TODO Add Message
 			//TODO Exception
 		}
 		return null;
 	}
 
 	/**
 	 * get all contacts from the connected acoount
 	 *
 	 * if dont get information from google return null else a list of Google
 	 * ContactEntrys
 	 *
 	 * @return List<ContactEntry>
 	 */
 	protected List<ContactEntry> getAllContacts() {
 
 		URL feedUrl;
 		try {
 			feedUrl = new URL(
 					"https://www.google.com/m8/feeds/contacts/default/full?max-results=500");
 			ContactFeed resultFeed = myService.getFeed(feedUrl,
 					ContactFeed.class);
 			if (resultFeed == null) {
 				return null;
 			}
 			
 			return resultFeed.getEntries();
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());//TODO Add Message
 			//TODO Exception
 		} catch (ServiceException ex) {
 			LOGGER.log(Level.SEVERE, null, ex.getMessage());//TODO Add Message
 			//TODO Exception
 		}
 		return null;
 	}
 
 	/**
 	 * methode to map a google contact to a auto-b-day contact
 	 *
 	 * @param contactEntry
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
 		LOGGER.log(Level.INFO, "-------------------------------------------------");
 		abdContact.setHashid("12");
 
 		id = contactEntry.getId();
 		LOGGER.log(Level.INFO, "ID: {0}", id);
 		abdContact.setId(id);
 
 		firstname = getGContactFirstname(contactEntry);
 		LOGGER.log(Level.INFO, "Firstname: {0}", firstname);
 		if (!firstname.equals("")) {
 			abdContact.setFirstname(firstname);
 		} else {
 			errorStack.add("Skipping Contact " + abdContact.getFirstname() + " " + abdContact.getName() + ": No Firstname");
 			LOGGER.log(Level.SEVERE, "Skipping current Contact: No Firstname");
 			return null;
 		}
 
 		name = getGContactFamilyname(contactEntry);
 		LOGGER.log(Level.INFO, "Name: {0}", name);
 		if (!name.equals("")) {
 			abdContact.setName(name);
 		} else {
 			errorStack.add("Skipping Contact " + abdContact.getFirstname() + " " + abdContact.getName() + ": No Name");
 			LOGGER.log(Level.SEVERE, "Skipping current Contact: No Name");
 			return null;
 		}
 
 		birthday = getGContactBirthday(contactEntry);
 		LOGGER.log(Level.INFO, "birthday: {0}", birthday);
 		if (birthday != null) {
 			abdContact.setBday(birthday);
 		} else {
 			errorStack.add("Skipping Contact " + abdContact.getFirstname() + " " + abdContact.getName() + ": No Bday");
 			LOGGER.log(Level.SEVERE, "Skipping current Contact: No Bday");
 			return null;
 		}
 
 		if (!contactEntry.getEmailAddresses().isEmpty()) {
 			mailadress = getGContactFirstMailAdress(contactEntry);
 			abdContact.setMail(mailadress);
 		} else {
 			errorStack.add("Skipping Contact " + abdContact.getFirstname() + " " + abdContact.getName() + ": No Mail");
 			LOGGER.log(Level.SEVERE, "Skipping current Contact: No Mail");
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
 	 * @param contactEntry
 	 *
 	 * @return String
 	 */
 	protected String getGContactFirstname(ContactEntry contactEntry) {
 		String firstname = "";
 		try {
 			firstname = contactEntry.getName().getGivenName().getValue();
 		} catch (NullPointerException ex) {
 		}
 
 		return firstname;
 
 	}
 
 	/**
 	 * methode that return the familyname of a given Contact
 	 *
 	 * @param contactEntry
 	 *
 	 * @return String
 	 */
 	protected String getGContactFamilyname(ContactEntry contactEntry) {
 		String familyname = "";
 		try {
 			familyname = contactEntry.getName().getFamilyName().getValue();
 		} catch (NullPointerException ex) {
 		}
 		return familyname;
 
 	}
 
 	/**
 	 * methode that return the birthday of a given Contact
 	 *
 	 * @param contactEntry
 	 *
 	 * @return Date
 	 */
 	protected Date getGContactBirthday(ContactEntry contactEntry) {
 		String gContactBirthday;
 		Date bday = null;
 		try {
 			gContactBirthday = contactEntry.getBirthday().getValue();
 			bday = GoogleBirthdayConverter.convertBirthday(gContactBirthday);
 		} catch (CanNotConvetGoogleBirthdayException ex) {
 		} catch (NullPointerException ex) {
 		}
 		return bday;
 	}
 
 	/**
 	 * Methode that return a mailadress of a given Contact
 	 *
 	 * @param contactEntry
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
 		}
 		return groupName;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see de.fhb.autobday.manager.connector.AImporter#isConnectionEtablished()
 	 */
 	@Override
 	public boolean isConnectionEtablished() {
 		return connectionEtablished;
 	}
 
 	public PropertyLoader getPropLoader() {
 		return propLoader;
 	}
 
 	public void setPropLoader(PropertyLoader propLoader) {
 		this.propLoader = propLoader;
 	}
 }
