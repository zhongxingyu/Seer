 package org.notificationengine.persistance;
 
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 import org.jongo.Jongo;
 import org.jongo.MongoCollection;
 import org.json.simple.JSONObject;
 import org.notificationengine.constants.Constants;
 import org.notificationengine.domain.DecoratedNotification;
 import org.notificationengine.domain.RawNotification;
 import org.notificationengine.domain.Topic;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.mongodb.DB;
 import com.mongodb.DBCursor;
 import com.mongodb.MongoClient;
 import com.mongodb.ServerAddress;
 
 @Component(value=Constants.PERSISTER)
 public class Persister implements InitializingBean {
 
 	private static Logger LOGGER = Logger.getLogger(Persister.class);
 	
 	@Autowired
 	private MongoDbSettings mongoDbSettings;
 	
 	private Boolean modeTest; 
 	
 	private MongoCollection rawNotifications;
 	
 	private MongoCollection decoratedNotifications;
 
 	private MongoCollection deletedDecoratedNotifications;
 
 	public Persister() {
 		
 		this(Boolean.FALSE);
 	}
 
 	// take care, only to be called by test class
 	public Persister(Boolean modeTest, MongoDbSettings mongoDbSettings) {
 		
 		this.modeTest = modeTest;
 		
 		this.mongoDbSettings = mongoDbSettings;
 		
 		this.init();
 	}
 	
 	public Persister(Boolean modeTest) {
 		
 		this.modeTest = modeTest;
 	}
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		
 		this.init();
 	}
 	
 	private void init() {
 		
 		try {
 			DB db = null;
 			
 			if (this.modeTest) {
 				db = new MongoClient().getDB(Constants.DATABASE_TEST);
 			}
 			else {
 				MongoClient mongoClient = null;
 				
 				if (!this.mongoDbSettings.getReplicaMode()) {
 					
 					String host = MongoDbUtils.getHostFromSingleServerUrl(this.mongoDbSettings.getUrl());
 					
 					int port = MongoDbUtils.getPortFromSingleServerUrl(this.mongoDbSettings.getUrl());
 					
 					ServerAddress addr = new ServerAddress(host, port);	
 					
 					mongoClient = new MongoClient(addr);
 				}
 				else {
 					List<ServerAddress> addrs = MongoDbUtils.getServerAddressListFromMultipleServerUrl(this.mongoDbSettings.getUrl());
 					
 					mongoClient = new MongoClient(addrs);
 				}
 				
 				db = mongoClient.getDB(this.mongoDbSettings.getDatabase());
 			}
 			
 			Jongo jongo = new Jongo(db);
 			
 			this.rawNotifications = jongo.getCollection(Constants.RAW_NOTIFICATIONS_COLLECTION);
 			
 			this.decoratedNotifications = jongo.getCollection(Constants.DECORATED_NOTIFICATIONS_COLLECTION);
 
             this.deletedDecoratedNotifications = jongo.getCollection(Constants.DELETED_DECORATED_NOTIFICATIONS_COLLECTION);
 		} 
 		catch (UnknownHostException e) {
 
 			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
 			
 			LOGGER.error("Unable to build Persister");
 		}
 	}	
 
 	public void createDecoratedNotification(
 			DecoratedNotification decoratedNotification) {
 		
 		this.decoratedNotifications.save(decoratedNotification);
 
 		LOGGER.debug("Inserted DecoratedNotification : " + decoratedNotification);
 	}
 	
 	public void createRawNotification(
 			RawNotification rawNotification) {
 		
 		this.rawNotifications.save(rawNotification);
 		
 		LOGGER.debug("Inserted RawNotification : " + rawNotification);
 	}
 	
 	public Collection<RawNotification> retrieveAllRawNotifications() {
 		
 		LOGGER.debug("Retrieve all RawNotifications");
 		
 		Collection<RawNotification> result = new ArrayList<>();
 		
 		Iterable<RawNotification> rawNotificationsForGetAll = this.rawNotifications.find("{}").as(RawNotification.class);
 		
 		for(RawNotification rawNotification : rawNotificationsForGetAll) {
 			
 			LOGGER.debug("Found RawNotification (get all): " + rawNotification);
 			
 			result.add(rawNotification);
 			
 		}
 		
 		LOGGER.debug("All RawNotifications found");
 		
 		return result;
 		
 	}
 
     public Collection<RawNotification> retrieveAllRawNotificationsForTopic(Topic topic) {
 
         LOGGER.debug("Retrieve not processed RawNotifications for Topic : " + topic);
 
         Collection<RawNotification> result = new ArrayList<>();
 
         JSONObject exactQueryJsonObject = new JSONObject();
         exactQueryJsonObject.put(Constants.TOPIC_NAME, topic.getName());
 
         String exactQuery = exactQueryJsonObject.toString();
 
         Iterable<RawNotification> rawNotificationsForExactQuery = this.rawNotifications.find(exactQuery).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForExactQuery) {
 
             LOGGER.debug("Found RawNotification (exact query) : " + rawNotification);
 
             result.add(rawNotification);
         }
 
 
         JSONObject likeQueryJsonObject = new JSONObject();
 
         JSONObject regularExpressionJsonObject = new JSONObject();
         regularExpressionJsonObject.put(Constants.REGEX, topic.getName() + "\\..*");
 
         likeQueryJsonObject.put(Constants.TOPIC_NAME, regularExpressionJsonObject);
 
         String likeQuery = likeQueryJsonObject.toString();
 
         Iterable<RawNotification> rawNotificationsForLikeQuery = this.rawNotifications.find(likeQuery).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForLikeQuery) {
 
             LOGGER.debug("Found RawNotification (like query) : " + rawNotification);
 
             result.add(rawNotification);
         }
 
         return result;
 
     }
 
     public Collection<RawNotification> retrieveNotProcessedRawNotifications() {
 
         LOGGER.debug("Retrieve not processed RawNotifications");
 
         Collection<RawNotification> result = new ArrayList<>();
 
         JSONObject exactQueryJsonObject = new JSONObject();
         exactQueryJsonObject.put(Constants.PROCESSED, Boolean.FALSE);
 
         String exactQuery = exactQueryJsonObject.toString();
 
         Iterable<RawNotification> rawNotificationsForExactQuery = this.rawNotifications.find(exactQuery).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForExactQuery) {
 
             LOGGER.debug("Found RawNotification (exact query) : " + rawNotification);
 
             result.add(rawNotification);
         }
 
         return result;
     }
 
 	public Collection<RawNotification> retrieveNotProcessedRawNotificationsForTopic(Topic topic) {
 		
 		LOGGER.debug("Retrieve not processed RawNotifications for Topic : " + topic);
 		
 		Collection<RawNotification> result = new ArrayList<>();
 		
 		JSONObject exactQueryJsonObject = new JSONObject();		
 		exactQueryJsonObject.put(Constants.PROCESSED, Boolean.FALSE);		
 		exactQueryJsonObject.put(Constants.TOPIC_NAME, topic.getName());
 		
 		String exactQuery = exactQueryJsonObject.toString();
 		
 		Iterable<RawNotification> rawNotificationsForExactQuery = this.rawNotifications.find(exactQuery).as(RawNotification.class);
 		
 		for(RawNotification rawNotification : rawNotificationsForExactQuery) {
 			
 			LOGGER.debug("Found RawNotification (exact query) : " + rawNotification);
 			
 			result.add(rawNotification);
 		}		
 		
 		JSONObject likeQueryJsonObject = new JSONObject();		
 		likeQueryJsonObject.put(Constants.PROCESSED, Boolean.FALSE);
 		
 		JSONObject regularExpressionJsonObject = new JSONObject();
 		regularExpressionJsonObject.put(Constants.REGEX, topic.getName() + "\\..*");
 		
 		likeQueryJsonObject.put(Constants.TOPIC_NAME, regularExpressionJsonObject);
 		
 		String likeQuery = likeQueryJsonObject.toString();
 		
 		Iterable<RawNotification> rawNotificationsForLikeQuery = this.rawNotifications.find(likeQuery).as(RawNotification.class);
 		
 		for(RawNotification rawNotification : rawNotificationsForLikeQuery) {
 			
 			LOGGER.debug("Found RawNotification (like query) : " + rawNotification);
 			
 			result.add(rawNotification);
 		}
 		
 		return result;
 	}
 
 	public void markRawNotificationAsProcessed(RawNotification rawNotification) {
 		
 		rawNotification.setProcessed(Boolean.TRUE);
         rawNotification.setProcessedAt(new Date());
 		
 		this.rawNotifications.save(rawNotification);
 	}
 
 	public void cleanRawNotifications() {
 		
 		this.rawNotifications.remove();
 	}
 	
 	public void cleanDecoratedNotifications() {
 		
 		this.decoratedNotifications.remove();
 	}
 
     public void cleanDeletedDecoratedNotifications() {
 
         this.deletedDecoratedNotifications.remove();
     }
 
 	public RawNotification retrieveRawNotificationById(ObjectId id) {
 
 		return this.rawNotifications.findOne(id).as(RawNotification.class);
 	}
 
 	public Collection<DecoratedNotification> retrieveNotSentDecoratedNotificationsForTopic(
 			Topic topic) {
 		
 		LOGGER.debug("Retrieve not sent DecoratedNotifications for Topic : " + topic);
 		
 		Collection<DecoratedNotification> result = new ArrayList<>();
 		
 		JSONObject exactQueryJsonObject = new JSONObject();		
 		exactQueryJsonObject.put(Constants.SENT, Boolean.FALSE);		
 		exactQueryJsonObject.put(Constants.RAW_NOTIFICATION_TOPIC_NAME, topic.getName());
 		
 		String exactQuery = exactQueryJsonObject.toString();
 		
 		Iterable<DecoratedNotification> decoratedNotificationsForExactQuery = this.decoratedNotifications.find(exactQuery).as(DecoratedNotification.class);
 		
 		for(DecoratedNotification decoratedNotification : decoratedNotificationsForExactQuery) {
 			
 			LOGGER.debug("Found DecoratedNotification (exact query) : " + decoratedNotification);
 			
 			result.add(decoratedNotification);
 		}		
 		
 		JSONObject likeQueryJsonObject = new JSONObject();		
 		likeQueryJsonObject.put(Constants.SENT, Boolean.FALSE);
 		
 		JSONObject regularExpressionJsonObject = new JSONObject();
 		regularExpressionJsonObject.put(Constants.REGEX, topic.getName() + "\\..*");
 		
 		likeQueryJsonObject.put(Constants.RAW_NOTIFICATION_TOPIC_NAME, regularExpressionJsonObject);
 		
 		String likeQuery = likeQueryJsonObject.toString();
 		
 		Iterable<DecoratedNotification> decoratedNotificationsForLikeQuery = this.decoratedNotifications.find(likeQuery).as(DecoratedNotification.class);
 		
 		for(DecoratedNotification decoratedNotification : decoratedNotificationsForLikeQuery) {
 			
 			LOGGER.debug("Found DecoratedNotification (like query) : " + decoratedNotification);
 			
 			result.add(decoratedNotification);
 		}
 		
 		return result;
 	}
 
     public Collection<DecoratedNotification> retrieveAllDecoratedNotifications() {
 
         LOGGER.debug("Retrieve all DecoratedNotifications");
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Iterable<DecoratedNotification> decoratedNotifications = this.decoratedNotifications.find("{}").as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotifications) {
 
             LOGGER.debug("Found DecoratedNotification (exact query) : " + decoratedNotification);
 
             result.add(decoratedNotification);
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveAllDecoratedNotificationsForTopic(Topic topic) {
 
         LOGGER.debug("Retrieve not sent DecoratedNotifications for Topic : " + topic);
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         JSONObject exactQueryJsonObject = new JSONObject();
         exactQueryJsonObject.put(Constants.RAW_NOTIFICATION_TOPIC_NAME, topic.getName());
 
         String exactQuery = exactQueryJsonObject.toString();
 
         LOGGER.debug("Exact query: " + exactQuery);
 
         Iterable<DecoratedNotification> decoratedNotificationsForExactQuery = this.decoratedNotifications.find(exactQuery).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForExactQuery) {
 
             LOGGER.debug("Found DecoratedNotification (exact query) : " + decoratedNotification);
 
             result.add(decoratedNotification);
         }
 
 
         JSONObject likeQueryJsonObject = new JSONObject();
 
         JSONObject regularExpressionJsonObject = new JSONObject();
         regularExpressionJsonObject.put(Constants.REGEX, topic.getName() + "\\..*");
 
         likeQueryJsonObject.put(Constants.RAW_NOTIFICATION_TOPIC_NAME, regularExpressionJsonObject);
 
         String likeQuery = likeQueryJsonObject.toString();
 
         Iterable<DecoratedNotification> decoratedNotificationsForLikeQuery = this.decoratedNotifications.find(likeQuery).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForLikeQuery) {
 
             LOGGER.debug("Found DecoratedNotification (like query) : " + decoratedNotification);
 
             result.add(decoratedNotification);
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveNotSentDecoratedNotifications() {
 
         LOGGER.debug("Retrieve not sent DecoratedNotifications");
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         JSONObject exactQueryJsonObject = new JSONObject();
         exactQueryJsonObject.put(Constants.SENT, Boolean.FALSE);
 
         String exactQuery = exactQueryJsonObject.toString();
 
         Iterable<DecoratedNotification> decoratedNotificationsForExactQuery = this.decoratedNotifications.find(exactQuery).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForExactQuery) {
 
             LOGGER.debug("Found DecoratedNotification (exact query) : " + decoratedNotification);
 
             result.add(decoratedNotification);
         }
 
         return result;
     }
 
     public Collection<RawNotification> retrieveRawNotificationsForDate(Date date) {
 
         Collection<RawNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{createdAt: {$gt: #, $lt: #}}";
 
         LOGGER.debug("Date query: " + exactQuery);
 
         Iterable<RawNotification> rawNotificationsForDate = this.rawNotifications.find(exactQuery, beginDate, endDate).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForDate) {
 
             result.add(rawNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<RawNotification> retrieveRawNotificationsForDateAndTopic(Date date, Topic topic) {
 
         LOGGER.debug("retrieveRawNotificationsForDate " + date.toString() + " with topic " + topic.getName());
 
         Collection<RawNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{createdAt: {$gt: #, $lt: #}, \"topic.name\": #}";
 
         Iterable<RawNotification> rawNotificationsForDateAndTopic = this.rawNotifications.find(exactQuery, beginDate, endDate, topic.getName()).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForDateAndTopic) {
 
             result.add(rawNotification);
 
         }
 
         String likeQuery = "{createdAt: {$gt: #, $lt: #}, \"topic.name\": {$regex: #}}";
 
         Iterable<RawNotification> rawNotificationsForDateAndTopicLike = this.rawNotifications.find(likeQuery, beginDate, endDate, topic.getName() + "\\..*").as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForDateAndTopicLike) {
 
             result.add(rawNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<RawNotification> retrieveProcessedRawNotificationsForDate(Date date) {
 
         Collection<RawNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{processedAt: {$gt: #, $lt: #}, processed: true}";
 
         LOGGER.debug("Date query: " + exactQuery);
 
         Iterable<RawNotification> rawNotificationsForDate = this.rawNotifications.find(exactQuery, beginDate, endDate).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForDate) {
 
             result.add(rawNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<RawNotification> retrieveProcessedRawNotificationsForDateAndTopic(Date date, Topic topic) {
 
         LOGGER.debug("retrieveProcessedRawNotificationsForDate " + date.toString() + " with topic " + topic.getName());
 
         Collection<RawNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{createdAt: {$gt: #, $lt: #}, processed: true, \"topic.name\": #}";
 
         Iterable<RawNotification> rawNotificationsForDateAndTopic =
                 this.rawNotifications.find(exactQuery, beginDate, endDate, topic.getName()).as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForDateAndTopic) {
 
             result.add(rawNotification);
 
         }
 
         String likeQuery = "{createdAt: {$gt: #, $lt: #}, processed: true, \"topic.name\": {$regex: #}}";
 
         Iterable<RawNotification> rawNotificationsForDateAndTopicLike =
                 this.rawNotifications.find(likeQuery, beginDate, endDate, topic.getName() + "\\..*").as(RawNotification.class);
 
         for(RawNotification rawNotification : rawNotificationsForDateAndTopicLike) {
 
             result.add(rawNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveSentDecoratedNotificationsForDate(Date date) {
 
         LOGGER.debug("retrieveSentDecoratedNotificationsForDate: " + date.toString() );
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{sentAt: {$gt: #, $lt: #}, sent: true}";
 
         LOGGER.debug("Date query: " + exactQuery);
 
         Iterable<DecoratedNotification> decoratedNotificationsForDate =
                 this.decoratedNotifications.find(exactQuery, beginDate, endDate).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDate) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveSentDecoratedNotificationsForDateAndTopic(Date date, Topic topic) {
 
         LOGGER.debug("retrieveSentDecoratedNotificationsForDate: " + date.toString() + " with topic " + topic.getName());
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
        String exactQuery = "{sentAt: {$gt: #, $lt: #}, sent: true, \"topic.name\": #}";
 
         Iterable<DecoratedNotification> decoratedNotificationsForDateAndTopic =
                 this.decoratedNotifications.find(exactQuery, beginDate, endDate, topic.getName()).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDateAndTopic) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
        String likeQuery = "{sentAt: {$gt: #, $lt: #}, sent: true, \"topic.name\": {$regex: #}}";
 
         Iterable<DecoratedNotification> decoratedNotificationsForDateAndLikeTopic =
                 this.decoratedNotifications.find(likeQuery, beginDate, endDate, topic.getName() + "\\..*").as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDateAndLikeTopic) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveDecoratedNotificationsForDate(Date date) {
 
         LOGGER.debug("retrieveDecoratedNotificationsForDate: " + date.toString() );
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{createdAt: {$gt: #, $lt: #}}";
 
         Iterable<DecoratedNotification> decoratedNotificationsForDate =
                 this.decoratedNotifications.find(exactQuery, beginDate, endDate).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDate) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveDecoratedNotificationsForDateAndTopic(Date date, Topic topic) {
 
         LOGGER.debug("retrieveDecoratedNotificationsForDate: " + date.toString() + " with topic " + topic.getName());
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{createdAt: {$gt: #, $lt: #}, \"rawNotification.topic.name\": #}";
 
         Iterable<DecoratedNotification> decoratedNotificationsForDateAndTopic =
                 this.decoratedNotifications.find(exactQuery, beginDate, endDate, topic.getName()).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDateAndTopic) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
         String likeQuery = "{createdAt: {$gt: #, $lt: #}, \"rawNotification.topic.name\": {$regex: #}}";
 
         Iterable<DecoratedNotification> decoratedNotificationsForDateAndLikeTopic =
                 this.decoratedNotifications.find(likeQuery, beginDate, endDate, topic.getName() + "\\..*").as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDateAndLikeTopic) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
         return result;
 
     }
 
     public Collection<Topic> retrieveAllTopics() {
 
         LOGGER.debug("Retrieve all Topics");
 
         Collection<Topic> result = new HashSet<>();
 
         Collection<RawNotification> rawNotifications = this.retrieveAllRawNotifications();
 
         for(RawNotification rowNotification : rawNotifications) {
 
             result.add(rowNotification.getTopic());
 
         }
 
         for(Topic topic : result) {
 
             LOGGER.debug("Topic found: " + topic);
         }
 
         return result;
 
     }
 
     public Collection<Topic> retrieveAllSubTopicsForTopic(Topic topic) {
 
         LOGGER.debug("Retrieve all subtopics of topic " + topic.getName());
 
         Collection<Topic> result = new HashSet<>();
 
         Collection<RawNotification> rawNotifications = this.retrieveAllRawNotificationsForTopic(topic);
 
         for(RawNotification rawNotification : rawNotifications) {
 
             result.add(rawNotification.getTopic());
         }
 
         for(Topic topicFound : result) {
             LOGGER.debug("Topic found " + topicFound);
         }
 
         return result;
 
     }
 
 	public void markDecoratedNotificationAsSent(
 			DecoratedNotification decoratedNotification) {
 		
 		decoratedNotification.setSent(Boolean.TRUE);
         decoratedNotification.setSentAt(new Date());
 		
 		this.decoratedNotifications.save(decoratedNotification);
 	}
 
 	public DecoratedNotification retrieveDecoratedNotificationById(
 			ObjectId id) {
 		
 		return this.decoratedNotifications.findOne(id).as(DecoratedNotification.class);
 	}
 
 	public Boolean getModeTest() {
 		return modeTest;
 	}
 
 	public void setModeTest(Boolean modeTest) {
 		this.modeTest = modeTest;
 	}
 	
 	public MongoDbSettings getMongoDbSettings() {
 		return mongoDbSettings;
 	}
 
 	public void setMongoDbSettings(MongoDbSettings mongoDbSettings) {
 		this.mongoDbSettings = mongoDbSettings;
 	}
 
 	public MongoCollection getRawNotifications() {
 		return rawNotifications;
 	}
 
 	public void setRawNotifications(MongoCollection rawNotifications) {
 		this.rawNotifications = rawNotifications;
 	}
 	
 	public MongoCollection getDecoratedNotifications() {
 		return decoratedNotifications;
 	}
 
 	public void setDecoratedNotifications(MongoCollection decoratedNotifications) {
 		this.decoratedNotifications = decoratedNotifications;
 	}
 
 
     public void moveFailedDecoratedNotification(DecoratedNotification decoratedNotificationToDelete) {
 
         ObjectId decoratedNotificationId = decoratedNotificationToDelete.get_id();
 
         this.decoratedNotifications.remove(decoratedNotificationId);
 
         //We store deleted decoratedNotifications in another collection to have some metrics
         this.saveDeletedDecoratedNotification(decoratedNotificationToDelete);
 
     }
 
     public void saveDeletedDecoratedNotification(DecoratedNotification decoratedNotification) {
 
         decoratedNotification.setDeletedAt(new Date());
 
         this.deletedDecoratedNotifications.save(decoratedNotification);
     }
 
 	public void saveDecoratedNotification(
 			DecoratedNotification decoratedNotificationToSave) {
 		
 		this.decoratedNotifications.save(decoratedNotificationToSave);
 	}
 
     public Collection<DecoratedNotification> retrieveAllDeletedDecoratedNotifications() {
 
         LOGGER.debug("Retrieve all DecoratedNotifications");
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Iterable<DecoratedNotification> decoratedNotifications = this.deletedDecoratedNotifications.find("{}").as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotifications) {
 
             LOGGER.debug("Found DeletedDecoratedNotification (exact query) : " + decoratedNotification);
 
             result.add(decoratedNotification);
         }
 
         return result;
 
     }
 
     public Collection<DecoratedNotification> retrieveDeletedDecoratedNotificationForDate(Date date) {
 
         LOGGER.debug("retrieveDeletedDecoratedNotificationForDate: " + date.toString() );
 
         Collection<DecoratedNotification> result = new ArrayList<>();
 
         Calendar cal = Calendar.getInstance();
 
         cal.setTime(date);
 
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Date beginDate = cal.getTime();
 
         cal.add(Calendar.DAY_OF_MONTH, 1);
 
         Date endDate = cal.getTime();
 
         // query created manually
         String exactQuery = "{deletedAt: {$gt: #, $lt: #}}";
 
         Iterable<DecoratedNotification> decoratedNotificationsForDate =
                 this.deletedDecoratedNotifications.find(exactQuery, beginDate, endDate).as(DecoratedNotification.class);
 
         for(DecoratedNotification decoratedNotification : decoratedNotificationsForDate) {
 
             LOGGER.debug("Decorated notification found: " + decoratedNotification);
 
             result.add(decoratedNotification);
 
         }
 
         return result;
 
     }
 }
