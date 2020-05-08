 package eu.socialsensor.sfc.streams.store;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import eu.socialsensor.framework.client.dao.ItemDAO;
 import eu.socialsensor.framework.client.dao.MediaItemDAO;
 import eu.socialsensor.framework.client.dao.StreamUserDAO;
 import eu.socialsensor.framework.client.dao.WebPageDAO;
 import eu.socialsensor.framework.client.dao.impl.ItemDAOImpl;
 import eu.socialsensor.framework.client.dao.impl.MediaItemDAOImpl;
 import eu.socialsensor.framework.client.dao.impl.StreamUserDAOImpl;
 import eu.socialsensor.framework.client.dao.impl.WebPageDAOImpl;
 import eu.socialsensor.framework.client.mongo.MongoHandler;
 import eu.socialsensor.framework.common.domain.Item;
 import eu.socialsensor.framework.common.domain.MediaItem;
 import eu.socialsensor.framework.common.domain.StreamUser;
 import eu.socialsensor.framework.common.domain.WebPage;
 import eu.socialsensor.framework.common.factories.ItemFactory;
 import eu.socialsensor.sfc.streams.StorageConfiguration;
 /**
  * Class for storing items in mongo db
  * 
  * @author manosetro
  * @email  manosetro@iti.gr
  * 
  * @author ailiakop
  * @email  ailiakop@iti.gr
  *
  */
 public class MongoDbStorage implements StreamUpdateStorage {
 
 	private static String HOST = "mongodb.host";
 	private static String DATABASE = "mongodb.database";
 	
 	private static String ITEMS_COLLECTION = "mongodb.items.collection";
 	private static String MEDIA_ITEMS_COLLECTION = "mongodb.mediaitems.collection";
 	private static String USERS_COLLECTION = "mongodb.streamusers.collection";
 	private static String WEBPAGES_COLLECTION = "mongodb.webpages.collection";
 	
 	private Logger  logger = Logger.getLogger(MongoDbStorage.class);
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 	 
 	}
 	
 	private String host;
 	private String dbName;
 	
 	private String itemsCollectionName;
 	private String mediaItemsCollectionName;
 	private String streamUsersCollectionName;
 	private String webPageCollectionName;
 	
 	
 	private ItemDAO itemDAO;
 	private MediaItemDAO mediaItemDAO;
 	private StreamUserDAO streamUserDAO;
 	private WebPageDAO webPageDAO;
 	
 	public MongoDbStorage(StorageConfiguration config) {	
 		this.host = config.getParameter(MongoDbStorage.HOST);
 		this.dbName = config.getParameter(MongoDbStorage.DATABASE);
 		this.itemsCollectionName = config.getParameter(MongoDbStorage.ITEMS_COLLECTION, "Items");
 		this.mediaItemsCollectionName = config.getParameter(MongoDbStorage.MEDIA_ITEMS_COLLECTION, "MediaItems");
 		this.streamUsersCollectionName = config.getParameter(MongoDbStorage.USERS_COLLECTION, "StreamUsers");
 		this.webPageCollectionName = config.getParameter(MongoDbStorage.WEBPAGES_COLLECTION, "WebPages");
 
 	}
 	
 	public MongoDbStorage(String hostname, String dbName, String itemsCollectionName,
 			String mediaItemsCollectionName, String streamUsersCollectionName, String webPageCollectionName) {	
 		this.host = hostname;
 		this.dbName = dbName;
 		this.itemsCollectionName = itemsCollectionName;
 		this.mediaItemsCollectionName = mediaItemsCollectionName;
 		this.streamUsersCollectionName = streamUsersCollectionName;
 		this.webPageCollectionName = webPageCollectionName; 
 	}
 	
 	@Override
 	public void close() {
 		
 	}
 
 	@Override
 	public boolean delete(String id) throws IOException {
 		return itemDAO.deleteItem(id);
 	}
 
 	@Override
 	public void open() throws IOException {
 		
 		logger.info("Open MongoDB storage <host: " + host + ", database: " + dbName + 
 				", items collection: " + itemsCollectionName +">");
 
 		this.itemDAO = new ItemDAOImpl(host, dbName, itemsCollectionName);
 		this.mediaItemDAO = new MediaItemDAOImpl(host, dbName, mediaItemsCollectionName);
 		this.streamUserDAO = new StreamUserDAOImpl(host, dbName, streamUsersCollectionName);
 		this.webPageDAO = new WebPageDAOImpl(host, dbName, webPageCollectionName);
 	}
 
 	@Override
 	public void store(Item item) throws IOException {
 		
 		// Handle Items
 		if(!itemDAO.exists(item.getId())) {
 			// save item
 			item.setLastUpdated(new Date());
 			itemDAO.insertItem(item);
 			
 			// Handle Stream Users
 			StreamUser user = item.getStreamUser();
 			if(user != null) {
 				if(!streamUserDAO.exists(user.getId())) {
 					// save stream user
 					streamUserDAO.insertStreamUser(user);
				}
				else {
					streamUserDAO.incStreamUserValue(user.getId(), "items");
					streamUserDAO.incStreamUserValue(user.getId(), "mentions");
 				}
 			}
 			
			if(item.getMentions() != null) {
 				String[] mentions = item.getMentions();
 				for(String mention : mentions) {
					streamUserDAO.incStreamUserValue(mention, "mentions");
 				}
 			}
 
			if(item.getReferencedUserId() != null) {
				String userid = item.getReferencedUserId();
				streamUserDAO.incStreamUserValue(userid, "shares");
			}
			
 			// Handle Media Items
 			for(MediaItem mediaItem : item.getMediaItems()) {
 				if(!mediaItemDAO.exists(mediaItem.getId())) {
 					// save media item
 					mediaItemDAO.addMediaItem(mediaItem);
 				}
 				else {
 					//mediaItemDAO.updateMediaItemPopularity(mediaItem);
 				}
 			}
 			
 			// Handle Web Pages
 			List<WebPage> webPages = item.getWebPages();
 			if(webPages != null) {
 				for(WebPage webPage : webPages) {
 					String webPageURL = webPage.getUrl();
 					if(!webPageDAO.exists(webPageURL)) {
 						webPageDAO.addWebPage(webPage);
 					}
 					else {
 						webPageDAO.updateWebPageShares(webPageURL);
 					}
 				}
 			}
 		}
 		else {
 			itemDAO.updateItem(item);
 			
 		}
 		
 	}
 
 	@Override
 	public void update(Item update) throws IOException {
 		// update item
 		store(update);
 	}
 	
 	@Override
 	public void updateTimeslot() {
 	}
 	
 	
 	/**
 	 * Read all items from a collection in mongo db
 	 * @param collection
 	 * @param mongoItems
 	 * @throws UnknownHostException
 	 */
 	public void readItemsFromMongo(String collection,List<Item> mongoItems) throws UnknownHostException{
 		MongoHandler mongo = new MongoHandler(host, dbName, collection, null);
 		List<String> jsonItems = mongo.findMany(-1);
 	
 		for(String json : jsonItems){
 			
 			Item item = ItemFactory.create(json);
 			
 			mongoItems.add(item);
 			
 		}
 		
 	}
 	
 }
