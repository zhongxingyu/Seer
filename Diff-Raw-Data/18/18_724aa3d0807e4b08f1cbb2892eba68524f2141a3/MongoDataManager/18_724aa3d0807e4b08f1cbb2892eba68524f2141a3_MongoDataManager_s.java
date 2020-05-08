 /**
  * 
  */
 package net.mysocio.data.management;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import net.mysocio.connection.readers.Source;
 import net.mysocio.data.IDataManager;
 import net.mysocio.data.ISocioObject;
 import net.mysocio.data.IUniqueObject;
 import net.mysocio.data.SocioTag;
 import net.mysocio.data.SocioUser;
 import net.mysocio.data.UserAccount;
 import net.mysocio.data.UserContact;
 import net.mysocio.data.UserSource;
 import net.mysocio.data.accounts.Account;
 import net.mysocio.data.contacts.Contact;
 import net.mysocio.data.messages.GeneralMessage;
 import net.mysocio.data.messages.UnreaddenMessage;
 import net.mysocio.data.ui.UiObject;
 import net.mysocio.data.ui.UserPage;
 
 import org.bson.types.ObjectId;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.query.Query;
 
 /**
  * @author Aladdin
  * 
  */
 public class MongoDataManager implements IDataManager {
 	private static final Logger logger = LoggerFactory.getLogger(MongoDataManager.class);
 	private Datastore ds;
 	
 
 	public MongoDataManager(Datastore ds) {
 		this.ds = ds; 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.mysocio.data.management.IDataManager#createUser(java.lang.String,
 	 * java.lang.String)
 	 */
 	public SocioUser getUser(Account account, Locale locale) throws Exception {
 		String userName = account.getUserName();
 		logger.debug("Getting user " + userName + " for "
 				+ account.getAccountType());
 		SocioUser user = account.getUser();
 		if (user != null) {
 			logger.debug("Account found");
 			return user;
 		}
 		user = createUser(account, locale, userName);
 		return user;
 	}
 
 	private SocioUser createUser(Account account, Locale locale, String userName)
 			throws Exception {
 		String userId;
 		logger.debug("Creating user");
 		SocioUser user = new SocioUser();
 		user.setName(userName);
 		user.setLocale(locale.getLanguage());
 		saveObject(user);
 		userId = user.getId().toString();
 		account.setUser(user);
 		saveObject(account);
 		user.setMainAccount(account);
 		saveObject(user);
 		addAccountToUser(account, userId);
 		
 		DefaultUserMessagesProcessor processor = new DefaultUserMessagesProcessor();
 		processor.setUserId(userId);
 		CamelContextManager.addRoute("activemq:" + userId + ".newMessage", processor, null, 0l);
 		MarkMessageReaddenProcessor readdenProcessor = new MarkMessageReaddenProcessor();
 		readdenProcessor.setUserId(userId);
 		CamelContextManager.addRoute("activemq:" + userId + ".messageReaden", readdenProcessor, null, 0l);
 		logger.debug("User created");
 		return user;
 	}
 
 	/**
 	 * @param account
 	 * @param user
 	 * @throws Exception 
 	 */
 	public void addAccountToUser(Account account, String userId) throws Exception {
 		UserAccount userAccount = new UserAccount();
 		userAccount.setUserId(userId);
 		userAccount.setAccount(account);
 		saveObject(userAccount);
 		List<Source> sources = account.getSources();
 		for (Source source : sources) {
 			addSourceToUser(userId, source);
 		}
 		List<Contact> contacts = account.getContacts();
 		for (Contact contact : contacts) {
 			UserContact userContact = new UserContact();
 			userContact.addContact(contact);
 			userContact.setUserId(userId);
 			saveObject(userContact);
 		}
 	}
 	
 	public void addSourceToUser(String userId, Source source) throws Exception{
 		saveObject(source);
 		source.createRoute("activemq:" + userId  + ".newMessage");
 		UserSource userSource = new UserSource();
 		userSource.setUserId(userId);
 		userSource.setSource(source);
 		saveObject(userSource);
 	}
 
 	/**
 	 * @param account
 	 * @return
 	 */
 	public Account getAccount(String accountUniqueId) {
 		Query<Account> q = ds.createQuery(Account.class).field("accountUniqueId").equal(accountUniqueId);
 		return q.get();
 	}
 
 	public<T> T getObject(Class<T> T, String id) {
 		Query<T>  q = (Query<T>)ds.createQuery(T).field("id").equal(new ObjectId(id));
 		return q.get();
 	}
 
 	public UiObject getUiObject(String category, String name) {
 		Query<UiObject>  q = ds.createQuery(UiObject.class).field("category").equal(category)
 				.field("name").equal(name);
 		return q.get();
 	}
 
 	public <T extends ISocioObject> List<T> getObjects(Class<T> T) {
 		return ds.find(T).asList();
 	}
 
 	public void saveUiObject(UiObject uiObject) throws DuplicateMySocioObjectException{
 		String category = uiObject.getCategory();
 		String name = uiObject.getName();
 		if (getUiObject(category, name) != null) {
 			logger.warn("UI object with name " + name + " already exists in category " + category);
 			return;
 		}
 		saveObject(uiObject);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.mysocio.data.management.IDataManager#saveObjects(java.util.List)
 	 */
 	public <T extends ISocioObject> void saveObjects(Class<T> T, List<T> objects) throws DuplicateMySocioObjectException {
 		for (T object : objects) {
 			saveObject(object);
 		}
 	}
 
 	public void deleteObject(Object object) {
 		ds.delete(object);
 	}
 
 	public <T extends ISocioObject> void saveObject(T object) throws DuplicateMySocioObjectException {
 		if (object instanceof IUniqueObject){
 			IUniqueObject uniqueObject = (IUniqueObject)object;
 			Query<T> q = (Query<T>)ds.createQuery(object.getClass()).field(uniqueObject.getUniqueFieldName()).equal(uniqueObject.getUniqueFieldValue());
 			T objectT = (T) q.get();
 			if (objectT != null) {
 				logger.info("Duplicate object of type: " + object.getClass() + " for query: " + q.toString());
 				throw new DuplicateMySocioObjectException("Duplicate object of type: " + object.getClass() + " for query: " + q.toString());
 			}
 		}
 		ds.save(object);
 	}
 
 	public void setMessageReadden(String messageId) {
		Query<UnreaddenMessage> q = ds.createQuery(UnreaddenMessage.class).field("message.uniqueId").equal(new ObjectId(messageId));
 		ds.delete(q);
 	}
 
 	public List<GeneralMessage> getUnreadMessages(SocioUser user, String tagId) {
 		Query<UnreaddenMessage> q = ds.createQuery(UnreaddenMessage.class).field("userId").equal(user.getId().toString());
 		if (!tagId.equals(SocioUser.ALL_TAGS)){
 			q.field("tag.id").equal(new ObjectId(tagId));
 		}
 		if (user.getOrder().equals(SocioUser.ASCENDING_ORDER)){
 			q.order("messageDate");
 		}else{
 			q.order("-messageDate");
 		}
 		q.limit(user.getRange());
 		List<UnreaddenMessage> messagesList = q.asList();
 		List<GeneralMessage> messages = new ArrayList<GeneralMessage>();
 		for (UnreaddenMessage unreaddenMessage : messagesList) {
 			if (!messages.contains(unreaddenMessage.getMessage())){
 				messages.add(unreaddenMessage.getMessage());
 			}
 		}
 		return messages;
 	}
 	
 	public Long countUnreadMessages(String tagId) {
 		return ds.createQuery(UnreaddenMessage.class).field("tag.id").equal(new ObjectId(tagId)).countAll();
 	}
 
 	public String getPage(String userId, String pageKey) {
 		Query<UserPage> q = ds.createQuery(UserPage.class).field("userId").equal(userId).field("pageKey").equal(pageKey);
 		UserPage userPage = q.get();
 		if (userPage == null){
 			return null;
 		}
 		return userPage.getPageHTML();
 	}
 
 	public SocioTag getTag(String userId, String value) {
 		Query<SocioTag> q = ds.createQuery(SocioTag.class).field("userId").equal(userId).field("value").equal(value);
 		return q.get();
 	}
 	
 	public Collection<SocioTag> getUserTags(String userId) {
 		Query<SocioTag> q = ds.createQuery(SocioTag.class).field("userId").equal(userId);
 		return q.asList();
 	}
 
 
 	public List<UserAccount> getAccounts(String userId) {
 		Query<UserAccount> q = ds.createQuery(UserAccount.class).field("userId").equal(userId);
 		return q.asList();
 	}
 
 	public void removeSource(String userId, String sourceId) {
 		Query<UserSource> q = ds.createQuery(UserSource.class).field("userId").equal(userId);
 		List<UserSource> sources = q.asList();
 		for (UserSource userSource : sources) {
 			if (userSource.getSource().getId().toString().equals(sourceId)){
 				ds.delete(userSource.getSource());
 			}
 		}
 	}
 
 	public List<UserContact> getContacts(String userId) {
 		Query<UserContact> q = ds.createQuery(UserContact.class).field("userId").equal(userId);
 		return q.asList();
 	}
 
 	public List<UserSource> getSources(String userId) {
 		Query<UserSource> q = ds.createQuery(UserSource.class).field("userId").equal(userId);
 		return q.asList();
 	}
 }
