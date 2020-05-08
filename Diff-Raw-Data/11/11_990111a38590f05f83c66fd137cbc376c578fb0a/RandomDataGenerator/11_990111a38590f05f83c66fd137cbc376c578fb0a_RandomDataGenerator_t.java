 /**
  * 
  */
 package net.frontlinesms.debug;
 
 import java.io.*;
 import java.util.*;
 
 import net.frontlinesms.FrontlineSMS;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.*;
 import net.frontlinesms.data.repository.*;
 
 /**
  * Generator of random application data.
  * @author aga
  */
 public class RandomDataGenerator {
 	private static final String LOCAL_PHONE_NUMBER = "000";
 
 	/** {@link FrontlineSMS} instance the data will be attached to */
 	private FrontlineSMS frontlineController;
 	
 	/** Random number generator used for random things */
 	private Random randy = new Random();
 
 	/** Possible group names */
 	private List<String> groupNames;
 	/** Possible contact names */
 	private List<String> contactNames;
 	/** Possible message words */
 	private List<String> messageWords;
 	
 	private List<String> contactPhoneNumbers = new LinkedList<String>();
 	
 	/** Initialise the random source data from the classpath resources. */
 	public void initFromClasspath() throws IOException {
 		contactNames = loadFromClassPath("contactNames");
 		groupNames = loadFromClassPath("groupNames");
 		messageWords = loadFromClassPath("messageWords");
 	}
 
 	/**
 	 * Sets the frontline controller that this generates data for.
 	 * @param frontlineController
 	 */
 	public void setFrontlineController(FrontlineSMS frontlineController) {
 		this.frontlineController = frontlineController;
 	}
 
 	/**
 	 * Generate a number of random objects.
 	 * @param count
 	 */
 	public void generate(int count) {
 		// Generate contacts
 		List<Contact> contacts = generateContacts(count);
 		persistContacts(contacts);
 		
 		// Generate groups
 		List<Group> groups = generateAndPersistGroups(count);
 		
 		// Add the contacts to the groups
 		populateGroups(groups, contacts);
 		
 		// Generate keywords
 		List<Keyword> keywords = generateKeywords(count);
 		persistKeywords(keywords);
 		
 		// Generate keyword actions
 		generateKeywordActions(keywords);
 		
 		// Generate messages
 		generateIncomingMessages(count);
 		generateOutgoingMessages(count);
 		
 		// Generate email accounts
 		List<EmailAccount> emailAccounts = generateEmailAccounts((int) Math.ceil(count / 10.0));
 		
 		// Generate emails
 		generateEmails(emailAccounts, count);
 	}
 	
 	private void persistKeywords(List<Keyword> keywords) {
 		KeywordDao dao = this.frontlineController.getKeywordDao();
 		for (Iterator iterator = keywords.iterator(); iterator.hasNext();) {
 			Keyword keyword = (Keyword) iterator.next();
 			try {
 				dao.saveKeyword(keyword);
 			} catch (DuplicateKeyException e) {
 				// Duplicate keyword - remove it
 				iterator.remove();
 			}
 		}
 	}
 
 	private void generateEmails(List<EmailAccount> emailAccounts, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private List<EmailAccount> generateEmailAccounts(int count) {
 		ArrayList<EmailAccount> accounts = new ArrayList<EmailAccount>(count);
 		// TODO Auto-generated method stub
 		return accounts;
 	}
 
 	private void generateIncomingMessages(int count) {
 		MessageDao messageDao = frontlineController.getMessageDao();
 		while(--count >= 0) {
 			String messageContent = generateMessageContent();
 			Message message = Message.createIncomingMessage(randy.nextLong(), getRandomContactPhoneNumber(), LOCAL_PHONE_NUMBER, messageContent);
 			messageDao.saveMessage(message);
 		}
 	}
 
 	private void generateOutgoingMessages(int count) {
 		MessageDao messageDao = frontlineController.getMessageDao();
 		while(--count >= 0) {
 			String messageContent = generateMessageContent();
 			Message message = Message.createOutgoingMessage(randy.nextLong(), LOCAL_PHONE_NUMBER, getRandomContactPhoneNumber(), messageContent);
 			messageDao.saveMessage(message);
 		}
 	}
 	
 	private String generateMessageContent() {
 		int wordCount = randy.nextInt(100);
 		String messageContent = "";
 		while(--wordCount >= 0) {
 			String nextWord = getRandomMessageWord();
 			if((messageContent + nextWord).length() <=480)
 				messageContent += nextWord;
 			else break;
 			
 			if(wordCount > 1) {
 				if(messageContent.length() < 480)
 					messageContent += " ";
 				else break;
 			}
 		}
 		return messageContent;
 	}
 
 	private void generateKeywordActions(List<Keyword> keywords) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private List<Keyword> generateKeywords(int count) {
 		ArrayList<Keyword> keywords = new ArrayList<Keyword>(count);
 		while(--count >= 0) {
 			String keywordString = "";
 			// Make keywords up to 5 words long
 			int wordCount = 1 + randy.nextInt(4);
 			while(--wordCount>=0) keywordString+=" "+getRandomMessageWord();
 			Keyword keyword = new Keyword(keywordString.substring(1), "Keyword string generated for debug.");
 			keywords.add(keyword);
 		}
 		return keywords;
 	}
 
 	private void persistContacts(List<Contact> contacts) {
 		ContactDao contactDao = this.frontlineController.getContactDao();
 		for (Iterator<Contact> iterator = contacts.iterator(); iterator.hasNext();) {
 			Contact contact = (Contact) iterator.next();
 			try {
 				contactDao.saveContact(contact);
 			} catch (DuplicateKeyException e) {
 				// Remove duplicates
 				iterator.remove();
 			}
 		}
 	}
 
 	/** Randomly populate the generated groups with generated contacts */
 	private void populateGroups(List<Group> groups, List<Contact> contacts) {
 		GroupDao groupDao = this.frontlineController.getGroupDao();
		GroupMembershipDao memDao = this.frontlineController.getGroupMembershipDao();
 		for (Iterator<Contact> iterator = contacts.iterator(); iterator.hasNext();) {
 			Contact contact = (Contact) iterator.next();
 			int groupCount = randy.nextInt(7);
 			while(--groupCount >= 0) {
 				Group g = groups.get(randy.nextInt(groups.size()));
				memDao.addMember(g, contact);
 				groupDao.updateGroup(g);
 			}
 		}
 	}
 
 	/**
 	 * Generate some random groups
 	 * @param count the number of groups to generate
 	 * @return the random groups
 	 */
 	private List<Group> generateAndPersistGroups(int count) {
 		ArrayList<Group> groups = new ArrayList<Group>(count);
		Group rootGroup = new Group(null, null);
		Group lastGroup = rootGroup;
 		GroupDao groupDao = this.frontlineController.getGroupDao();
 		while(--count >= 0) {
 			String name = getRandomGroupName() + " " + getRandomGroupName();
 			boolean isOrphan = randy.nextBoolean();
			Group newGroup = new Group(isOrphan ? rootGroup : lastGroup, name);
 			try {
 				groupDao.saveGroup(newGroup);
 				groups.add(newGroup);
 				lastGroup = newGroup;
 			} catch (DuplicateKeyException e) {
 				// Discard duplicates
 			}
 		}
 		return groups;
 	}
 
 	private String getRandomGroupName() {
 		return groupNames.get(randy.nextInt(groupNames.size()));
 	}
 
 	private String getRandomMessageWord() {
 		return messageWords.get(randy.nextInt(messageWords.size()));
 	}
 	private String getRandomContactPhoneNumber() {
 		// TODO Auto-generated method stub
 		return contactPhoneNumbers.get(randy.nextInt(contactPhoneNumbers.size()));
 	}
 
 	private List<Contact> generateContacts(int count) {
 		ArrayList<Contact> contacts = new ArrayList<Contact>(count);
 		while(--count >= 0) {
 			String name = getRandomContactName();
 			name += " " + getRandomContactName();
 			name += " " + getRandomContactName();
 			String phoneNumber = "+" + randy.nextLong();
 			contactPhoneNumbers.add(phoneNumber);
 			String otherPhoneNumber = randy.nextBoolean() ? null : "+" + randy.nextLong();
 			String emailAddress = randy.nextBoolean() ? null : generateRandomEmailAddress();
 			Contact newContact = new Contact(name, phoneNumber, otherPhoneNumber, emailAddress, null, randy.nextBoolean());
 			contacts.add(newContact);
 		}
 		return contacts;
 	}
 
 	private String generateRandomEmailAddress() {
 		return getRandomContactName() + "@" + getRandomContactName() + ".com";
 	}
 
 	private String getRandomContactName() {
 		return contactNames.get(randy.nextInt(contactNames.size()));
 	}
 	
 	private List<String> loadFromClassPath(String fileId) throws IOException {
 		InputStream inputStream = null;
 		InputStreamReader isr = null;
 		BufferedReader in = null;
 		try {
 			String filename = this.getClass().getSimpleName() + "." + fileId;
 			inputStream = getClass().getResourceAsStream(filename);
 			isr = new InputStreamReader(inputStream);
 			in = new BufferedReader(isr);
 			
 			ArrayList<String> list = new ArrayList<String>();
 			String line;
 			while((line=in.readLine()) != null) {
 				if((line=line.trim()).length() > 0) {
 					list.add(line);
 				}
 			}
 			return list;
 		} finally {
 			if(in != null) try { in.close(); } catch(IOException ex) {}
 			if(isr != null) try { isr.close(); } catch(IOException ex) {}
 			if(inputStream != null) try { inputStream.close(); } catch(IOException ex) {}
 		}
 	}
 }
