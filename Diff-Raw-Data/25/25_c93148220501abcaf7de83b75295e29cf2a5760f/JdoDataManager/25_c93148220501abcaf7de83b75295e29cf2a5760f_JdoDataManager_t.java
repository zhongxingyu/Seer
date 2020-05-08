 /**
  * 
  */
 package net.mysocio.data.management;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.jdo.Extent;
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import net.mysocio.connection.readers.ISource;
 import net.mysocio.connection.readers.Source;
 import net.mysocio.data.Contact;
 import net.mysocio.data.GeneralMessage;
 import net.mysocio.data.IMessage;
 import net.mysocio.data.ISocioObject;
 import net.mysocio.data.IUiObject;
 import net.mysocio.data.SocioTag;
 import net.mysocio.data.SocioUser;
 import net.mysocio.data.UiObject;
 import net.mysocio.data.UserIdentifier;
 import net.mysocio.data.UserUiObjects;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Aladdin
  *
  */
 public class JdoDataManager extends AbstractDataManager {
 	private static final Logger logger = LoggerFactory.getLogger(JdoDataManager.class);
 	private static PersistenceManager pm;
 	private static AbstractDataManager instance = new JdoDataManager();
 	
 	public static AbstractDataManager getInstance() {
 		if (pm == null){
 			// Create a PersistenceManagerFactory for this datastore
 	        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("transactions-optional");
 	        pm = pmf.getPersistenceManager();
 		}
 		return instance;
 	}
 	
 	private JdoDataManager(){}
 	
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#createUser(java.lang.String, java.lang.String)
 	 */
 	public SocioUser createUser(String identifier, String identifierValue, Locale locale){
 		logger.debug("Creating user" + identifier + "==\"" + identifierValue + "\"");
 		SocioUser user = new SocioUser();
 		setUserIdentifier(user, identifier, identifierValue);
 		user.setName(getNameFromIdentifier(identifier, identifierValue));
 		user.setLocale(locale.getLanguage());
 		user = createUniqueObject(SocioUser.class, identifier + " == \"" + identifierValue + "\"", user);
 		logger.debug("User created");
 		return user;
 	}
 
 	private String getNameFromIdentifier(String identifier,
 			String identifierValue) {
 		UserIdentifier userIdentifier = UserIdentifier.valueOf(identifier);
 		String name = "";
 		switch (userIdentifier) {
 		case email:
 			name = identifierValue.split("@")[0];
 			break;
 		default:
 			break;
 		}
 		return name;
 	}
 	
 	public IUiObject getUiObject(String category, String name){
         return getUniqueObject(UiObject.class, "category == \"" + category + "\" and name == \"" + name + "\"");
 	}
 	
 	public<T> T getUniqueObject(Class T, String query){
 		Transaction tx = pm.currentTransaction();
 		T object;
         try
         {
             tx.begin();
             Query q=pm.newQuery(T, query);
             q.setUnique(true);
             object = (T)q.execute();
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
         return object;
 	}
 	
 	public<T> List<T> getObjects(Class T){
 		Transaction tx = pm.currentTransaction();
 		List<T> objects;
         try
         {
             tx.begin();
             Query q=pm.newQuery(T);
             objects = (List<T>)q.execute();
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
         return objects;
 	}
 	
 	public void saveUiObject(UiObject uiObject) throws DuplicateMySocioObjectException{
 		String category = uiObject.getCategory();
 		String name = uiObject.getName();
 		if (getUiObject(category, name) == null){
 			throw new DuplicateMySocioObjectException("UI object with name " + name + " already exists in category " + category);
 		}
 		saveObject(uiObject);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#saveObjects(java.util.List)
 	 */
 	public void saveObjects(List<? extends ISocioObject> objects) {
 		for (ISocioObject iSocioObject : objects) {
 			saveObject(iSocioObject);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#saveObject(net.mysocio.data.ISocioObject)
 	 */
 	public void saveObject(ISocioObject object) {
 		Transaction tx=pm.currentTransaction();
         try
         {
             tx.begin();
             pm.makePersistent(object);
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
 	}
 	
 	public void deleteObject(ISocioObject object) {
 		Transaction tx=pm.currentTransaction();
         try
         {
             tx.begin();
             pm.deletePersistent(object);
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
 	}
 	
 	public<T> T createUniqueObject(Class T, String query, T object){
 		Transaction tx=pm.currentTransaction();
         try
         {
             tx.begin();
             Extent<T> extent = pm.getExtent (T, true);
             Query q=pm.newQuery(extent, query);
             q.setUnique(true);
             T objectT = (T)q.execute();
             if (objectT == null){
             	objectT = pm.makePersistent(object);
             }else{
             	logger.info("Duplicate object of type: " + T.toString() + " for query: " + query);
             }
             tx.commit();
             return objectT;
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
 	}
 
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#saveSource(net.mysocio.connection.readers.ISource)
 	 */
 	public Source createSource(Source source){
 		return createUniqueObject(source.getClass(), "url == \"" + source.getUrl() + "\"", source);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#saveContact(net.mysocio.data.Contact)
 	 */
 	public void createContact(Contact contact) {
 		saveObject(contact);
 	}
 
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#getMessages(net.mysocio.connection.readers.ISource, java.lang.Long)
 	 */
 	public List<IMessage> getMessages(ISource source, Long date) {
 		Transaction tx = pm.currentTransaction();
 		List<IMessage> messages = new ArrayList<IMessage>();
         try
         {
             tx.begin();
             Query q=pm.newQuery(GeneralMessage.class, "sourceId == \"" + source.getId() + "\" and date > " + date);
             q.setOrdering("date ascending");
             messages = (List<IMessage>)q.execute();
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
 		return messages;
 	}
 	
 	public List<IMessage> getMessages(List<ISource> sources, Long date) {
 		List<IMessage> messages = new ArrayList<IMessage>();
		if (sources.isEmpty()){
			return messages;
		}
		Transaction tx = pm.currentTransaction();
         try
         {
         	List<String> ids = new ArrayList<String>();
     		for (ISource source : sources) {
     			ids.add(source.getId());
     		}
             tx.begin();
             Query q=pm.newQuery(GeneralMessage.class);
            q.declareImports("import java.util.Collection");
             q.declareParameters("Collection sourcesIds");
    		q.setFilter("sourcesIds.contains(sourceId) and date >= " + (date != null ? date : 0));
             q.setOrdering("date ascending");
             messages = (List<IMessage>)q.execute(ids);
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
 		return messages;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.mysocio.data.management.IDataManager#getUser(java.lang.String, java.lang.String)
 	 */
 	public SocioUser getUser(String identifier, String identifierValue) {
 		Transaction tx = pm.currentTransaction();
 		SocioUser user;
         try
         {
             tx.begin();
             Query q=pm.newQuery(SocioUser.class, identifier + " == \"" + identifierValue + "\"");
             q.setUnique(true);
             user = (SocioUser)q.execute();
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
 		return user;
 	}
 
 	public Map<String, IUiObject> getUserUiObjects(SocioUser user) {
 		UserUiObjects objects;
 		Transaction tx = pm.currentTransaction();
         try
         {
             tx.begin();
             Query q=pm.newQuery(UserUiObjects.class, "userId == \"" + user.getId()+"\"");
             q.setUnique(true);
             objects = (UserUiObjects)q.execute();
             tx.commit();
         }
         finally
         {
             if (tx.isActive())
             {
                 tx.rollback();
             }
         }
         if (objects == null){
         	return new HashMap<String, IUiObject>();
         }
 		return objects.getUserUiObjects();
 	}
 
 	@Override
 	public SocioTag createTag(SocioTag tag) {
 		return createUniqueObject(tag.getClass(), "value == \"" + tag.getValue() + "\"", tag);
 	}
 }
