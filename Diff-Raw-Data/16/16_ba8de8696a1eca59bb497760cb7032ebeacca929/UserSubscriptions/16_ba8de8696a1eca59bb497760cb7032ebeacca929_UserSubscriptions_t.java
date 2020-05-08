 package com.ubermind.internal.jenkinsnotifier.persistence;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.CompositeFilter;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Transaction;
 import com.ubermind.internal.jenkinsnotifier.jenkins.JenkinsNotification;
 import com.ubermind.internal.jenkinsnotifier.jenkins.JenkinsNotification.BuildStatus;
 import com.ubermind.internal.jenkinsnotifier.util.DatastoreUtil;
 
 public class UserSubscriptions {
 
 	public static boolean setUserSubscriptions(String userId, Map<String, SubscriptionLevel> subscriptionStates) {
 		if (userId == null || userId.isEmpty()) {
 			return false;
 		}
 
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
 		Transaction txn = datastore.beginTransaction();
 		try {
 			Entity userEntity = new Entity(getUserKey(userId));
 			datastore.put(userEntity);
 
 			// delete all subscriptions for this user
 			Query query = new Query(DsConst.KIND_SUBSCRIPTION, userEntity.getKey());
 			query.setKeysOnly();
 			List<Entity> oldSettings = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
 			List<Key> oldKeys = new ArrayList<Key>(oldSettings.size());
 			for (Entity oldSetting : oldSettings) {
 				oldKeys.add(oldSetting.getKey());
 			}
 			datastore.delete(oldKeys);
 
 			// insert new subscriptions for this user
 			for (String jobName : subscriptionStates.keySet()) {
 				SubscriptionLevel level = subscriptionStates.get(jobName);
 
 				Entity subscriptionEntity = new Entity(DsConst.KIND_SUBSCRIPTION, userEntity.getKey());
 				subscriptionEntity.setProperty(DsConst.PROP_LEVEL, Integer.valueOf(level.getLevelInt()));
 				subscriptionEntity.setProperty(DsConst.PROP_JOB_NAME, jobName);
 				subscriptionEntity.setUnindexedProperty(DsConst.PROP_USER, userId);
 
 				datastore.put(subscriptionEntity);
 			}
 
 			txn.commit();
 		}
 		finally {
 			if (txn.isActive()) {
 				txn.rollback();
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public static Map<String, SubscriptionLevel> getSubscriptionsForUser(String userId) {
 		Map<String, SubscriptionLevel> subscriptions = new HashMap<String, SubscriptionLevel>();
 
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Query query = new Query(DsConst.KIND_SUBSCRIPTION, getUserKey(userId));
 
 		List<Entity> entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
 		for (Entity entity : entities) {
 			String jobName = (String) entity.getProperty(DsConst.PROP_JOB_NAME);
 			int level = DatastoreUtil.getEntityPropertyInt(entity, DsConst.PROP_LEVEL, -1);
 
 			subscriptions.put(jobName, SubscriptionLevel.getLevel(level));
 		}
 
 		return subscriptions;
 	}
 
 	public static List<String> getSubscribersForBuild(JenkinsNotification buildInfo) {
 		String jobName = buildInfo.getJobName();
 		Integer level = SubscriptionLevel.getLowestMatchingLevel(buildInfo.getStatus());
 
 		// query for subscribers with the appropriate level
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Query query = new Query(DsConst.KIND_SUBSCRIPTION);
 		Filter jobFilter = new FilterPredicate(DsConst.PROP_JOB_NAME, FilterOperator.EQUAL, jobName);
		Filter levelFilter = new FilterPredicate(DsConst.PROP_LEVEL, FilterOperator.GREATER_THAN_OR_EQUAL, level);
 		query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(jobFilter, levelFilter)));
 
 		// do the query
 		Iterable<Entity> entities = datastore.prepare(query).asIterable();
 
 		// build the subscribers list
 		List<String> subscribers = new ArrayList<String>();
 		for (Entity entity : entities) {
 			String userId = (String) entity.getProperty(DsConst.PROP_USER);
 			subscribers.add(userId);
 		}
 
 		return subscribers;
 	}
 
 	private static Key getUserKey(String userId) {
 		return KeyFactory.createKey(DsConst.KIND_USER, userId);
 	}
 
 	public static enum SubscriptionLevel {
 		None("Not subscribed"),
 		Error("Errors"),
 		Unstable("Unstable"),
 		Success("Successful"), ;
 
 		private String title;
 
 		private SubscriptionLevel(String title) {
 			this.title = title;
 		}
 
 		public int getLevelInt() {
 			return ordinal();
 		}
 
 		public String getTitle() {
 			return title;
 		}
 
 		public static SubscriptionLevel getLevel(int level) {
 			try {
 				return values()[level];
 			}
 			catch (Exception e) {
 				// unkown
 			}
 
 			return None;
 		}
 
 		public static SubscriptionLevel parseString(String string) {
 			try {
 				return getLevel(Integer.parseInt(string));
 			}
 			catch (Exception e) {
 				// unknown
 			}
 
 			return SubscriptionLevel.None;
 		}
 
 		public static Integer getLowestMatchingLevel(BuildStatus status) {
 			int result;
 
 			switch (status) {
 			case FAILURE:
 				result = Error.getLevelInt();
 				break;
 			case UNSTABLE:
 				result = Unstable.getLevelInt();
 				break;
 			case SUCCESS:
 				result = Success.getLevelInt();
 				break;
 			default:
 				result = None.getLevelInt();
 			}
 
 			return Integer.valueOf(result);
 		}
 
 		public static int getCount() {
 			return values().length;
 		}
 	}
 
 	private interface DsConst {
 		public static final String KIND_USER = "user";
 		public static final String KIND_SUBSCRIPTION = "subscription";
 
 		public static final String PROP_JOB_NAME = "job_name";
 		public static final String PROP_LEVEL = "level";
 		public static final String PROP_USER = "user";
 	}
 
 }
