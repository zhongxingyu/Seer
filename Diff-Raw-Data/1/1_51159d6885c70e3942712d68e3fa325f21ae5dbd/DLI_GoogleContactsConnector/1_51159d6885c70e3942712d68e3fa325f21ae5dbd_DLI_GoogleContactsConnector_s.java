 package google_connector;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gdata.client.Query;
 import com.google.gdata.client.Service;
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.contacts.ContactEntry;
 import com.google.gdata.data.contacts.ContactFeed;
 import com.google.gdata.data.contacts.ContactGroupEntry;
 import com.google.gdata.data.contacts.ContactGroupFeed;
 import com.google.gdata.data.contacts.GroupMembershipInfo;
 import com.google.gdata.data.extensions.City;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.data.extensions.ExtendedProperty;
 import com.google.gdata.data.extensions.FamilyName;
 import com.google.gdata.data.extensions.GivenName;
 import com.google.gdata.data.extensions.Im;
 import com.google.gdata.data.extensions.Name;
 import com.google.gdata.data.extensions.PhoneNumber;
 import com.google.gdata.data.extensions.PostCode;
 import com.google.gdata.data.extensions.Street;
 import com.google.gdata.data.extensions.StructuredPostalAddress;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 import dli_contacts.Contact;
 import dli_contacts.Contact.ContactType;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class DLI_GoogleContactsConnector {
 	private static String sapId = "SAP-ID";
 	private static String customerGroupURL = "http://www.google.com/m8/feeds/groups/dli.ides.api%40gmail.com/base/3abf361e0913da63";
 	private static String supplierGroupURL = "http://www.google.com/m8/feeds/groups/dli.ides.api%40gmail.com/base/2aada2220eaad8d4";
 	private static String employeeGroupURL = "http://www.google.com/m8/feeds/groups/dli.ides.api%40gmail.com/base/587c880e884cdacb";
 
 	private static String customer = "Customer";
 	private static String supplier = "Supplier";
 	private static String employee = "Employee";
 	private static String company = "Company";
 
 	private final String username = "dli.ides.api@gmail.com";
 	private final String password = "DLIP455w0rd!";
 	private final String servicename = "dli-google-connector";
 	private final String contactsURL = "https://www.google.com/m8/feeds/contacts/dli.ides.api@gmail.com/full";
 	private final String groupsURL = "https://www.google.com/m8/feeds/groups/dli.ides.api@gmail.com/base";
 
 	private ContactsService myService;
 
 	public DLI_GoogleContactsConnector() throws AuthenticationException {
 		authenticateId();
 	}
 
 	public ContactEntry createContact(Contact contactInfo) throws IOException,
 			ServiceException {
 		return createContact(contactsURL, contactInfo, myService);
 
 	}
 
 	public static ContactEntry createContact(String contactsURL,
 			Contact contactInfo, Service myService) throws IOException,
 			ServiceException {
 		if (!(contactInfo.validate().isEmpty())) {
 			return null;
 		}
 		// Kopieren mit null statt ""
 		Contact contactInfoCopy = nullCopy(contactInfo);
 
 		// Create the entry to insert
 		ContactEntry contact = new ContactEntry();
 		contact.setTitle(new PlainTextConstruct(contactInfoCopy.getFirstname()
 				+ contactInfoCopy.getLastname()));
 
 		// Name
 		Name name = new Name();
 		name.setFamilyName(new FamilyName(contactInfoCopy.getLastname(), null));
 		name.setGivenName(new GivenName(contactInfoCopy.getFirstname(), null));
 		contact.setName(name);
 
 		// Email >> es kann NICHT NUR eine geben
 		if (contactInfoCopy.getEmail() != null) {
 			Email primaryMail = new Email();
 			primaryMail.setAddress(contactInfoCopy.getEmail());
 			primaryMail.setRel("http://schemas.google.com/g/2005#home");/*
 																		 * Email
 																		 * Typ
 																		 */
 			primaryMail.setPrimary(true);
 			contact.addEmailAddress(primaryMail);
 		}
 		// Telefon >> es kann NICHT NUR eine geben
 		if (contactInfoCopy.getPhone() != null) {
 			PhoneNumber phoneNumber = new PhoneNumber();
 			phoneNumber.setPhoneNumber(contactInfoCopy.getPhone());
 			phoneNumber.setPrimary(true);
 			phoneNumber.setLabel("Primaer");
 			contact.addPhoneNumber(phoneNumber);
 		}
 		// Adresse >> es kann NICHT NUR eine geben
 		if ((contactInfoCopy.getCity() != null)
 				|| (contactInfoCopy.getStreet() != null)
 				|| (contactInfoCopy.getZipcode() != null)) {
 			StructuredPostalAddress adresse = new StructuredPostalAddress();
 			adresse.setCity(new City(contactInfoCopy.getCity()));
 			adresse.setPostcode(new PostCode(contactInfoCopy.getZipcode()));
 			adresse.setStreet(new Street(contactInfoCopy.getStreet()));
 			adresse.setPrimary(true);
 			adresse.setLabel("Primaer");
 			contact.addStructuredPostalAddress(adresse);
 		}
 
 		// Firma
 		if (contactInfoCopy.getCompany() != null) {
 			ExtendedProperty company = new ExtendedProperty();
 			company.setName(DLI_GoogleContactsConnector.company);
 			company.setValue(contactInfoCopy.getCompany());
 			contact.addExtendedProperty(company);
 		}
 
 		// SAPID
 		if (contactInfoCopy.getSapId() != null) {
 			ExtendedProperty sapId = new ExtendedProperty();
 			sapId.setName(DLI_GoogleContactsConnector.sapId);
 			sapId.setValue(contactInfoCopy.getSapId());
			contact.addExtendedProperty(sapId);
 		}
 
 		// Gruppe setzen
 		String groupURL = null;
 		switch (contactInfoCopy.getType()) {
 		case CUSTOMER:
 			groupURL = customerGroupURL;
 			break;
 		case SUPPLIER:
 			groupURL = supplierGroupURL;
 			break;
 		case EMPLOYEE:
 			groupURL = employeeGroupURL;
 			break;
 
 		default:
 			break;
 		}
 		contact.addGroupMembershipInfo(new GroupMembershipInfo(false, groupURL));
 
 		// Ask the service to insert the new entry
 		URL postUrl = null;
 		postUrl = new URL(contactsURL);
 		return myService.insert(postUrl, contact);
 
 	}
 
 	private static Contact nullCopy(Contact contact) {
 		Contact copy = new Contact();
 
 		// Name
 		String cfname = contact.getFirstname();
 		copy.setFirstname(cfname != null && cfname.contentEquals("") ? null
 				: cfname);
 		String clname = contact.getLastname();
 		copy.setLastname(clname.contentEquals("") ? null : clname);
 
 		// Mail
 		String cmail = contact.getEmail();
 		copy.setEmail(cmail != null && cmail.contentEquals("") ? null : cmail);
 
 		// Telefon
 		String cphone = contact.getPhone();
 		copy.setPhone(cphone != null && cphone.contentEquals("") ? null
 				: cphone);
 
 		// Adresse
 		String ccity = contact.getCity();
 		copy.setCity(ccity != null && ccity.contentEquals("") ? null : ccity);
 		String cstreet = contact.getStreet();
 		copy.setStreet(cstreet != null && cstreet.contentEquals("") ? null
 				: cstreet);
 		String czip = contact.getZipcode();
 		copy.setZipcode(czip != null && czip.contentEquals("") ? null : czip);
 
 		// Firma
 		String ccompany = contact.getCompany();
 		copy.setCompany(ccompany != null && ccompany.contentEquals("") ? null
 				: ccompany);
 
 		// SapId
 		String csapid = contact.getSapId();
 		copy.setSapId(csapid != null && csapid.contentEquals("") ? null
 				: csapid);
 
 		// GoogleId
 		String cgoogle = contact.getGoogleId();
 		copy.setGoogleId(cgoogle != null && cgoogle.contentEquals("") ? null
 				: cgoogle);
 		// Gruppe
 		copy.setType(contact.getType());
 
 		return copy;
 	}
 
 	public ContactsService authenticateId() {
 		myService = authenticateId(username, password, servicename);
 		return myService;
 
 	}
 
 	/**
 	 * This method will authenticate the user credentials passed to it and
 	 * returns an instance of ContactService class.
 	 * 
 	 * @throws AuthenticationException
 	 */
 	public static ContactsService authenticateId(String username,
 			String password, String servicename) {
 		ContactsService myService;
 		myService = new ContactsService(servicename);
 		try {
 			myService.setUserCredentials(username, password);
 		} catch (AuthenticationException e) {
 			e.printStackTrace();
 		}
 		return myService;
 
 	}
 
 	public List<Contact> fetchContacts(Contact filter) throws ServiceException,
 			IOException {
 
 		return fetchContacts(contactsURL, filter, myService);
 
 	}
 
 	/**
 	 * Sucht die Kontakte, die mit dem filter übereinstimmen
 	 * 
 	 * @param filter
 	 * @param myService
 	 * @return
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	public static List<Contact> fetchContacts(String contactsURL,
 			Contact filter, ContactsService myService) throws ServiceException,
 			IOException {
 		// Create query and submit a request
 		URL feedUrl = null;
 		feedUrl = new URL(contactsURL);
 		Query myQuery = new Query(feedUrl);
 		ContactFeed resultFeed = null;
 
 		Contact filterCopy = nullCopy(filter);
 
 		// TODO noch anstaendig mit Querys machen/besprechen
 
 		// Gruppe
 		String groupId = null;
 		switch (filterCopy.getType()) {
 		case CUSTOMER:
 			groupId = customerGroupURL;
 			break;
 		case SUPPLIER:
 			groupId = supplierGroupURL;
 			break;
 		case EMPLOYEE:
 			groupId = employeeGroupURL;
 			break;
 
 		default:
 			break;
 		}
 		myQuery.setStringCustomParameter("group", groupId);
 
 		// submit request
 		if (!myQuery.isValidState()) {
 			resultFeed = myService.getFeed(feedUrl, ContactFeed.class);
 		} else {
 			resultFeed = myService.query(myQuery, ContactFeed.class);
 		}
 		// sort out
 		List<ContactEntry> ceResults = resultFeed.getEntries();
 		List<Contact> results = new ArrayList<Contact>();
 		for (ContactEntry ce : ceResults) {
 			Contact accepted = makeContact(ce);
 			if (filterContact(filterCopy, accepted)) {
 				results.add(accepted);
 			}
 		}
 		return results;
 	}
 
 	private static boolean filterContact(Contact filter, Contact accepted) {
 		boolean city = (filter.getCity() == null);
 		if (!city)
 			city = accepted.getCity().toLowerCase()
 					.contains(filter.getCity().toLowerCase());
 
 		boolean email = (filter.getEmail() == null);
 		if (!email)
 			email = accepted.getEmail().toLowerCase()
 					.contains(filter.getEmail().toLowerCase());
 
 		boolean firstname = (filter.getFirstname() == null);
 		if (!firstname)
 			firstname = accepted.getFirstname().toLowerCase()
 					.contains(filter.getFirstname().toLowerCase());
 
 		boolean lastname = (filter.getLastname() == null);
 		if (!lastname)
 			lastname = accepted.getLastname().toLowerCase()
 					.contains(filter.getLastname().toLowerCase());
 
 		boolean phone = (filter.getPhone() == null);
 		if (!phone)
 			phone = accepted.getPhone().toLowerCase()
 					.contains(filter.getPhone().toLowerCase());
 
 		boolean street = (filter.getStreet() == null);
 		if (!street)
 			street = accepted.getStreet().toLowerCase()
 					.contains(filter.getStreet().toLowerCase());
 
 		boolean sapId = (filter.getSapId() == null);
 		if (!sapId)
 			sapId = accepted.getSapId().contentEquals(filter.getSapId());
 
 		boolean googleId = (filter.getGoogleId() == null);
 		if (!googleId)
 			googleId = accepted.getGoogleId().contains(filter.getGoogleId());
 
 		boolean company = (filter.getCompany() == null);
 		if (!company)
 			company = accepted.getCompany().toLowerCase()
 					.contains(filter.getCompany().toLowerCase());
 
 		boolean zipcode = (filter.getZipcode() == null);
 		if (!zipcode)
 			zipcode = accepted.getZipcode().toLowerCase()
 					.contains(filter.getZipcode().toLowerCase());
 
 		return city && email && firstname && lastname && phone && street
 				&& sapId && googleId && company && zipcode;
 	}
 
 	private static Contact makeContact(ContactEntry ce) {
 		Contact result = new Contact();
 
 		// Name (Vorname, Nachname)
 		if (ce.hasName()) {
 			Name name = ce.getName();
 			if (name.hasGivenName())
 				result.setFirstname(name.getGivenName().getValue());
 			if (name.hasFamilyName())
 				result.setLastname(name.getFamilyName().getValue());
 		}
 
 		// E-Mail
 		for (Email email : ce.getEmailAddresses()) {
 			if (email.getPrimary()) {
 				result.setEmail(email.getAddress());
 			}
 
 		}
 
 		// Adresse (Strasse, Stadt, PLZ)
 		for (StructuredPostalAddress adress : ce.getStructuredPostalAddresses()) {
 			if (adress.getPrimary()) {
 				if (adress.getCity() != null)
 					result.setCity(adress.getCity().getValue());
 				if (adress.getStreet() != null)
 					result.setStreet(adress.getStreet().getValue());
 				if (adress.getPostcode() != null)
 					result.setZipcode(adress.getPostcode().getValue());
 			}
 		}
 
 		// Phone
 		for (PhoneNumber phone : ce.getPhoneNumbers()) {
 			if (phone.getPrimary()) {
 				result.setPhone(phone.getPhoneNumber());
 			}
 		}
 
 		// Google-ID
 		result.setGoogleId(ce.getId());
 
 		if (ce.hasExtendedProperties()) {
 			for (ExtendedProperty ep : ce.getExtendedProperties()) {
 				// SAP-ID
 				if (ep.getName().contentEquals(sapId)) {
 					result.setSapId(ep.getValue());
 				}
 				// Firma
 				if (ep.getName().contentEquals(company)) {
 					result.setCompany(ep.getValue());
 				}
 			}
 		}
 
 		// Gruppe
 		for (GroupMembershipInfo group : ce.getGroupMembershipInfos()) {
 			if (group.getHref().contentEquals(customerGroupURL)) {
 				result.setType(ContactType.CUSTOMER);
 			}
 			if (group.getHref().contentEquals(supplierGroupURL)) {
 				result.setType(ContactType.SUPPLIER);
 			}
 			if (group.getHref().contentEquals(employeeGroupURL)) {
 				result.setType(ContactType.EMPLOYEE);
 			}
 		}
 		return result;
 	}
 
 	private static String toStringWithContact(Contact c) {
 		if (c == null) {
 			return "Contact ist null\n";
 		}
 
 		String vorname = "first name:\t";
 		if (c.getFirstname() == null) {
 			vorname += "null" + "\n";
 		} else {
 			vorname += c.getFirstname() + "\n";
 		}
 
 		String nachname = "last name: \t";
 		if (c.getLastname() == null) {
 			nachname += "null" + "\n";
 		} else {
 			nachname += c.getLastname() + "\n";
 		}
 
 		String email = "email: \t\t";
 		if (c.getEmail() == null) {
 			email += "null" + "\n";
 		} else {
 			email += c.getEmail() + "\n";
 		}
 
 		String phone = "phone: \t\t";
 		if (c.getPhone() == null) {
 			phone += "null" + "\n";
 		} else {
 			phone += c.getPhone() + "\n";
 		}
 
 		String street = "street: \t";
 		if (c.getStreet() == null) {
 			street += "null" + "\n";
 		} else {
 			street += c.getStreet() + "\n";
 		}
 
 		String postal = "zipcode: \t";
 		if (c.getZipcode() == null) {
 			postal += "null" + "\n";
 		} else {
 			postal += c.getZipcode() + "\n";
 		}
 
 		String city = "city: \t\t";
 		if (c.getCity() == null) {
 			city += "null" + "\n";
 		} else {
 			city += c.getCity() + "\n";
 		}
 
 		String type = "type: \t\t";
 		if (c.getType() == null) {
 			type += "null" + "\n";
 		} else {
 			switch (c.getType()) {
 			case CUSTOMER:
 				type += customer + "\n";
 				break;
 			case SUPPLIER:
 				type += supplier + "\n";
 				break;
 			case EMPLOYEE:
 				type += employee + "\n";
 				break;
 			}
 		}
 
 		String googleid = "googleid: \t";
 		if (c.getGoogleId() == null) {
 			googleid += "null" + "\n";
 		} else {
 			googleid += c.getGoogleId() + "\n";
 		}
 
 		String sapId = "sapId: \t\t";
 		if (c.getSapId() == null) {
 			sapId += "null" + "\n";
 		} else {
 			sapId += c.getSapId() + "\n";
 		}
 
 		String company = "company: \t";
 		if (c.getCompany() == null) {
 			company += "null" + "\n";
 		} else {
 			company += c.getCompany() + "\n";
 		}
 
 		String result = vorname + nachname + email + phone + street + postal
 				+ city + type + googleid + sapId + company;
 
 		return result;
 	}
 
 	/*
 	 * This method will print details of all the contacts available in that
 	 * particular Google account.
 	 */
 	public static void printAllContacts(ContactsService myService)
 			throws ServiceException, IOException {
 		// Request the feed
 		URL feedUrl = new URL(
 				"https://www.google.com/m8/feeds/contacts/default/full");
 		ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);
 		// Print the results
 		System.out.println(resultFeed.getTitle().getPlainText());
 		for (ContactEntry entry : resultFeed.getEntries()) {
 			if (entry.hasName()) {
 				Name name = entry.getName();
 				if (name.hasFullName()) {
 					String fullNameToDisplay = name.getFullName().getValue();
 					if (name.getFullName().hasYomi()) {
 						fullNameToDisplay += " ("
 								+ name.getFullName().getYomi() + ")";
 					}
 					System.out.println("\\\t\\\t" + fullNameToDisplay);
 				} else {
 					System.out.println("\\\t\\\t (no full name found)");
 				}
 				if (name.hasNamePrefix()) {
 					System.out.println("\\\t\\\t"
 							+ name.getNamePrefix().getValue());
 				} else {
 					System.out.println("\\\t\\\t (no name prefix found)");
 				}
 				if (name.hasGivenName()) {
 					String givenNameToDisplay = name.getGivenName().getValue();
 					if (name.getGivenName().hasYomi()) {
 						givenNameToDisplay += " ("
 								+ name.getGivenName().getYomi() + ")";
 					}
 					System.out.println("\\\t\\\t" + givenNameToDisplay);
 				} else {
 					System.out.println("\\\t\\\t (no given name found)");
 				}
 				if (name.hasAdditionalName()) {
 					String additionalNameToDisplay = name.getAdditionalName()
 							.getValue();
 					if (name.getAdditionalName().hasYomi()) {
 						additionalNameToDisplay += " ("
 								+ name.getAdditionalName().getYomi() + ")";
 					}
 					System.out.println("\\\t\\\t" + additionalNameToDisplay);
 				} else {
 					System.out.println("\\\t\\\t (no additional name found)");
 				}
 				if (name.hasFamilyName()) {
 					String familyNameToDisplay = name.getFamilyName()
 							.getValue();
 					if (name.getFamilyName().hasYomi()) {
 						familyNameToDisplay += " ("
 								+ name.getFamilyName().getYomi() + ")";
 					}
 					System.out.println("\\\t\\\t" + familyNameToDisplay);
 				} else {
 					System.out.println("\\\t\\\t (no family name found)");
 				}
 				if (name.hasNameSuffix()) {
 					System.out.println("\\\t\\\t"
 							+ name.getNameSuffix().getValue());
 				} else {
 					System.out.println("\\\t\\\t (no name suffix found)");
 				}
 			} else {
 				System.out.println("\t (no name found)");
 			}
 			System.out.println("Email addresses:");
 			for (Email email : entry.getEmailAddresses()) {
 				System.out.print(" " + email.getAddress());
 				if (email.getRel() != null) {
 					System.out.print(" rel:" + email.getRel());
 				}
 				if (email.getLabel() != null) {
 					System.out.print(" label:" + email.getLabel());
 				}
 				if (email.getPrimary()) {
 					System.out.print(" (primary) ");
 				}
 				System.out.print("\n");
 			}
 			System.out.println("IM addresses:");
 			for (Im im : entry.getImAddresses()) {
 				System.out.print(" " + im.getAddress());
 				if (im.getLabel() != null) {
 					System.out.print(" label:" + im.getLabel());
 				}
 				if (im.getRel() != null) {
 					System.out.print(" rel:" + im.getRel());
 				}
 				if (im.getProtocol() != null) {
 					System.out.print(" protocol:" + im.getProtocol());
 				}
 				if (im.getPrimary()) {
 					System.out.print(" (primary) ");
 				}
 				System.out.print("\n");
 			}
 			System.out.println("Groups:");
 			for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
 				String groupHref = group.getHref();
 				System.out.println("  Id: " + groupHref);
 			}
 			System.out.println("Extended Properties:");
 			for (ExtendedProperty property : entry.getExtendedProperties()) {
 				if (property.getValue() != null) {
 					System.out.println("  " + property.getName() + "(value) = "
 							+ property.getValue());
 				} else if (property.getXmlBlob() != null) {
 					System.out.println("  " + property.getName()
 							+ "(xmlBlob)= " + property.getXmlBlob().getBlob());
 				}
 			}
 			Link photoLink = entry.getContactPhotoLink();
 			String photoLinkHref = photoLink.getHref();
 			System.out.println("Photo Link: " + photoLinkHref);
 			if (photoLink.getEtag() != null) {
 				System.out.println("Contact Photo's ETag: "
 						+ photoLink.getEtag());
 			}
 			System.out.println("Contact's ETag: " + entry.getEtag());
 		}
 
 	}
 
 	/* This method will add a contact to that particular Google account */
 
 	public static void main(String ar[]) {
 		System.out.println("main gestartet!");
 		test();
 	}
 
 	/**
 	 * 
 	 */
 	private static void test() {
 		try {
 
 			DLI_GoogleContactsConnector googleContactsAccess = new DLI_GoogleContactsConnector();
 			System.out
 					.println("DLI_GoogleContactsConnector erstellt und authentifiziert");
 
 			// System.out.println("printAllContacts");
 			// DLI_GoogleContactsConnector
 			// .printAllContacts(googleContactsAccess.myService);
 			// printAllGroups(googleContactsAccess.groupsURL,
 			// googleContactsAccess.myService);
 
 			// Contact contact = new Contact();
 			// contact.setFirstname("Muster");
 			// contact.setLastname("Muster");
 			// contact.setEmail("zabc@def.gh");
 			// contact.setPhone("123456789");
 			// contact.setStreet("Musterstr. 123");
 			// contact.setType(ContactType.SUPPLIER);
 			// contact.setCompany("Firma");
 			// contact.setSapId("sapMuster01");
 			// System.out.println("Contact erstellt\n\n"
 			// + toStringWithContact(contact));
 			// googleContactsAccess.createContact(contact);
 
 			System.out.println("Contact hinzugefuegt");
 			Contact filter = new Contact();
 			filter.setType(ContactType.SUPPLIER);
 			filter.setFirstname("Dominic");
 
 			System.out.println("Filter erstellt");
 			System.out.println(toStringWithContact(filter));
 			List<Contact> contacts = googleContactsAccess.fetchContacts(filter);
 			System.out.println(contacts.size() + " Kontakte runtergeladen");
 			for (Contact c : contacts) {
 				System.out.println(toStringWithContact(c));
 			}
 
 		} catch (Exception ex) {
 			System.out.println(ex);
 		}
 	}
 
 	/**
 	 * Beispielcode für die Suche in Google
 	 * 
 	 * @param myService
 	 * @param startTime
 	 * @throws ServiceException
 	 * @throws IOException
 	 */
 	public static void printAllGroups(String groupsURL,
 			ContactsService myService) {
 		// Request the feed
 		URL feedUrl = null;
 		try {
 			feedUrl = new URL(groupsURL);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		ContactGroupFeed resultFeed = null;
 		try {
 			resultFeed = myService.getFeed(feedUrl, ContactGroupFeed.class);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ServiceException e) {
 			e.printStackTrace();
 		}
 
 		for (ContactGroupEntry groupEntry : resultFeed.getEntries()) {
 			System.out.println("Atom Id: " + groupEntry.getId());
 			System.out.println("Group Name: "
 					+ groupEntry.getTitle().getPlainText());
 			System.out.println("Last Updated: " + groupEntry.getUpdated());
 
 			System.out.println("Extended Properties:");
 			for (ExtendedProperty property : groupEntry.getExtendedProperties()) {
 				if (property.getValue() != null) {
 					System.out.println("  " + property.getName() + "(value) = "
 							+ property.getValue());
 				} else if (property.getXmlBlob() != null) {
 					System.out.println("  " + property.getName()
 							+ "(xmlBlob) = " + property.getXmlBlob().getBlob());
 				}
 			}
 			System.out.println("Self Link: "
 					+ groupEntry.getSelfLink().getHref());
 			if (!groupEntry.hasSystemGroup()) {
 				// System groups do not have an edit link
 				System.out.println("Edit Link: "
 						+ groupEntry.getEditLink().getHref());
 				System.out.println("ETag: " + groupEntry.getEtag());
 			}
 			if (groupEntry.hasSystemGroup()) {
 				System.out.println("System Group Id: "
 						+ groupEntry.getSystemGroup().getId());
 			}
 		}
 	}
 }
