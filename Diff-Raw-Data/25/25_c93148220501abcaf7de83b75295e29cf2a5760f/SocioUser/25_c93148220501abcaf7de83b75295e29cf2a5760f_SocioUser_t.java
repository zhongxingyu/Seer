 /**
  * 
  */
 package net.mysocio.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.annotations.Join;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 
 import net.mysocio.connection.readers.ISource;
 
 /**
  * @author Aladdin
  * Although User is subclass of MySocio object, tags list is private list of tags usable by this user and not object tags.
  */
 @PersistenceCapable
 public class SocioUser extends Contact implements IUser {
 	public static final String ALL_SOURCES = "All";
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2886854604233072581L;
 	@Join
     @Persistent
 	private Map<String, List<IMessage>> unreadMessages = new HashMap<String, List<IMessage>>();
 	@Join
 	@Persistent(types={Account.class},mappedBy = "id")
 	private List<Account> accounts = new ArrayList<Account>();
 	@Join
 	@Persistent(types={SocioContact.class},mappedBy = "id")
 	private List<IContact> contacts = new ArrayList<IContact>();
 	@Persistent
 	private UserIdentifier userIdentifier;
 	@NotPersistent
 	private Map<String, List<ISource>> sortedSources;
 	@Persistent
	private Long lastUpdate = 0l;
 	public Long getLastUpdate() {
 		return lastUpdate;
 	}
 
 	public void setLastUpdate(Long lastUpdate) {
 		this.lastUpdate = lastUpdate;
 	}
 
 	public String getSelectedSource() {
 		return selectedSource;
 	}
 
 	public void setSelectedSource(String selectedSource) {
 		this.selectedSource = selectedSource;
 	}
 
 	@Persistent
 	private String selectedSource = ALL_SOURCES;
 	
 	public UserIdentifier getUserIdentifier() {
 		return userIdentifier;
 	}
 
 	public void setUserIdentifier(UserIdentifier userIdentifier) {
 		this.userIdentifier = userIdentifier;
 	}
 
 	/**
 	 * @return the sortedSources
 	 */
 	public Map<String, List<ISource>> getSortedSources() {
 		if (sortedSources == null){
 			sortedSources = new HashMap<String, List<ISource>>();
 			sortedSources = initSortedSources();
 		}
 		return sortedSources;
 	}
 
 	private Map<String, List<ISource>> initSortedSources() {
 		for (ISource source : getSources()) {
 			addSourceToSortedSources(source);
 		}
 		return sortedSources;
 	}
 
 	/**
 	 * @param source
 	 */
 	private void addSourceToSortedSources(ISource source) {
 		List<SocioTag> tags = source.getTags();
 		for (SocioTag tag : tags) {
 			List<ISource> sources = sortedSources.get(tag);
 			if (sources == null){
 				sources = new ArrayList<ISource>();
 				sortedSources.put(tag.getValue(), sources);
 			}
 			sources.add(source);
 		}
 		if (tags.isEmpty()){
 			List<ISource> sources = new ArrayList<ISource>();
 			sources.add(source);
 			sortedSources.put(source.getId(), sources);
 		}
 	}
 
 	private String locale;
 	
 	public List<IContact> getContacts(){
 		return contacts;
 	}
 
 	public List<IMessage> getUnreadMessages(List<ISource> sources){
 		List<IMessage> unreaddenMessages = new ArrayList<IMessage>();
 		for (ISource source : sources) {
 			List<IMessage> messages = this.unreadMessages.get(source.getId());
 			if (messages != null){
 				unreaddenMessages.addAll(messages);
 			}
 		}
 		return unreaddenMessages;
 	}
 	
 	public Integer getUnreadMessagesNum(String id){
 		List<IMessage> messages = this.unreadMessages.get(id);
 		if (messages == null || messages.isEmpty()){
 			return 0;
 		}
 		return messages.size();
 	}
 	
 	public void addUnreadMessages(String sourceId, List<IMessage> messages){
 		List<IMessage> list = unreadMessages.get(sourceId);
 		if (list == null){
 			list = new ArrayList<IMessage>();
 		}
 		list.addAll(messages);
 		unreadMessages.put(sourceId, list);
 	}
 	
 	public void addUnreadMessage(IMessage message){
 		String sourceId = message.getSourceId();
 		List<IMessage> list = unreadMessages.get(sourceId);
 		if (list == null){
 			list = new ArrayList<IMessage>();
 		}
 		list.add(message);
 		unreadMessages.put(sourceId, list);
 	}
 	
 	public void setMessageReadden(IMessage message) throws CorruptedDataException{
 		String sourceId = message.getSourceId();
 		List<IMessage> list = unreadMessages.get(sourceId);
 		if (list == null){
 			throw new CorruptedDataException();
 		}
 		list.remove(message);
 	}
 
 	/**
 	 * @return the accounts
 	 */
 	public List<Account> getAccounts() {
 		return accounts;
 	}
 
 	/**
 	 * @param accounts the accounts to set
 	 */
 	public void setAccounts(List<Account> accounts) {
 		this.accounts = accounts;
 	}
 	
 	public void addAccount(Account account) {
 		this.accounts.add(account);
 	}
 
 	/**
 	 * @param contacts the contacts to set
 	 */
 	public void setContacts(List<IContact> contacts) {
 		this.contacts = contacts;
 	}
 	public void addContact(IContact contact) {
 		this.contacts.add(contact);
 	}
 
 	public List<IMessage> getAllUnreadMessages() {
 		return getUnreadMessages(getSources());
 	}
 
 	public String getLocale() {
 		return locale;
 	}
 
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 
 	public List<IMessage> getUnreadMessages() {
 		if (selectedSource.equalsIgnoreCase(SocioUser.ALL_SOURCES)){
 			return getAllUnreadMessages();
 		}
 		
 		return getUnreadMessages(getSortedSources().get(selectedSource));
 	}
 }
