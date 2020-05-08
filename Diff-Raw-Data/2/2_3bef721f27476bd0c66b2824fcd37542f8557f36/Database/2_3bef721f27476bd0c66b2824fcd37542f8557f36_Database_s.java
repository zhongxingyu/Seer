 package com.github.davidmoten.timesheet;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.TimeZone;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Query.SortDirection;
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.common.base.Preconditions;
 
 /**
  * Encapsulates database access. GoogleAppEngine (BigTable) used for
  * persistence.
  * 
  * @author dxm
  * 
  */
 public class Database {
 
 	/**
 	 * Saves a single period for a date to the database.
 	 * 
 	 * @param id
 	 * @param start
 	 * @param durationMs
 	 */
 	public void saveTime(String id, Date start, long durationMs) {
 		Preconditions.checkNotNull(id);
 		Preconditions.checkNotNull(start);
 
 		User user = getUser();
 
 		// kind=db,name=schema,
 		Key timesheetKey = KeyFactory.createKey("Timesheet", "Timesheet");
 		// kind=table,entity=row
 		Entity entry = new Entity("Entry", timesheetKey);
 		entry.setProperty("user", user);
 		entry.setProperty("startTime", start);
 		entry.setProperty("durationMs", durationMs);
 		entry.setProperty("entryId", id);
 
 		// TODO allow addition of tags to an entry which might then be the basis
 		// of queries in reports.
 
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		datastore.put(entry);
 		System.out.println("saved " + start + " " + durationMs);
 
 	}
 
 	/**
 	 * Returns the entries where the startTime is > now - n days in JSON format.
 	 * 
 	 * @param n
 	 * @return
 	 */
 	public String getTimesJson(int n) {
 		Preconditions.checkArgument(n >= 0, "n must be non-negative");
 		User user = getUser();
 		long t = toUtc(System.currentTimeMillis() - n * 24 * 3600 * 1000L);
 		Filter sinceFilter = new FilterPredicate("startTime",
 				FilterOperator.GREATER_THAN_OR_EQUAL, new Date(t));
 		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL,
 				user);
 		Filter userAndSinceFilter = CompositeFilterOperator.and(userFilter,
 				sinceFilter);
 		Query q = new Query("Entry").setFilter(userAndSinceFilter).addSort(
 				"startTime", SortDirection.ASCENDING);
 
 		return toJson(getEntities(q));
 	}
 
 	/**
 	 * Returns the entries where the startTime is >= start and < finish in JSON
 	 * format.
 	 * 
 	 * @param start
 	 * @param finish
 	 * @return
 	 */
 	public String getTimeRangeJson(Date start, Date finish) {
 		Preconditions.checkNotNull(start, "start cannot be null");
 		Preconditions.checkNotNull(finish, "finish cannot be null");
 		User user = getUser();
 		Filter afterFilter = new FilterPredicate("startTime",
 				FilterOperator.GREATER_THAN_OR_EQUAL, start);
 		Filter beforeFilter = new FilterPredicate("startTime",
 				FilterOperator.LESS_THAN, finish);
 		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL,
 				user);
 		Filter userAndTimeFilter = CompositeFilterOperator.and(userFilter,
 				afterFilter, beforeFilter);
 		Query q = new Query("Entry").setFilter(userAndTimeFilter).addSort(
 				"startTime", SortDirection.ASCENDING);
 
 		return toJson(getEntities(q));
 	}
 
 	/**
 	 * Deletes all Entry objects in the database with entryId = id.
 	 * 
 	 * @param id
 	 */
 	public void deleteEntry(String id) {
 		Preconditions.checkNotNull(id);
 		// todo add user filter for security
 		Filter idFilter = new FilterPredicate("entryId", FilterOperator.EQUAL,
 				id);
 		Query q = new Query("Entry").setFilter(idFilter);
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		System.out.println("deleting");
 		for (Entity entity : getEntities(q)) {
 			datastore.delete(entity.getKey());
 			System.out.println("deleted " + entity.getKey());
 		}
 	}
 
 	/**
 	 * Get the current {@link User}.
 	 * 
 	 * @return
 	 */
 	private static User getUser() {
 		UserService userService = UserServiceFactory.getUserService();
 		return userService.getCurrentUser();
 	}
 
 	/**
 	 * Returns the epoch ms of the given time t where the time zone only is
 	 * switched to UTC.
 	 * 
 	 * @param t
 	 * @return
 	 */
 	private static long toUtc(long t) {
 		return t + TimeZone.getDefault().getOffset(t);
 	}
 
 	/**
 	 * Returns the results of a query as an {@link Iterable}<Entity>.
 	 * 
 	 * @param q
 	 * @return
 	 */
 	private static Iterable<Entity> getEntities(Query q) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		FetchOptions options = FetchOptions.Builder.withChunkSize(1000);
 		return datastore.prepare(q).asIterable(options);
 	}
 
 	/**
 	 * Returns a list of {@link Entity} in JSON format. Each Entity is assumed
 	 * to be of type Entry (as in time entry).
 	 * 
 	 * @param entities
 	 * @return
 	 */
 	private static String toJson(Iterable<Entity> entities) {
 		StringBuilder s = new StringBuilder();
 		s.append("{\n  \"entries\":[\n");
 		boolean first = true;
 		for (Entity entity : entities) {
 			Date startTime = (Date) entity.getProperty("startTime");
 			Long durationMs = (Long) entity.getProperty("durationMs");
 			String id = (String) entity.getProperty("entryId");
 			SimpleDateFormat df = new SimpleDateFormat(
 					"yyyy-MM-dd'T'HH:mm:00.000'Z'");
 			df.setTimeZone(TimeZone.getTimeZone("UTC"));
 			if (!first)
 				s.append(",\n");
 
 			s.append("      {\"startTime\" : \"").append(df.format(startTime))
 					.append("\"").append(",");
 			s.append("\"durationMs\" : ").append("\"").append(durationMs)
 					.append("\"").append(",");
 			s.append("\"id\" : ").append("\"").append(id).append("\"")
 					.append("}");
 			first = false;
 		}
 		s.append("\n]}");
 		System.out.println(s.toString());
 		return s.toString();
 	}
 
 	public String getTimesTabDelimited() {
 		User user = getUser();
 		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL,
 				user);
 		Query q = new Query("Entry").setFilter(userFilter).addSort("startTime",
 				SortDirection.ASCENDING);
 		StringBuilder s = new StringBuilder();
 		for (Entity entity : getEntities(q)) {
 			if (s.length() > 0)
 				s.append("\n");
 			Date startTime = (Date) entity.getProperty("startTime");
 			Long durationMs = (Long) entity.getProperty("durationMs");
 			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
 			df.setTimeZone(TimeZone.getTimeZone("UTC"));
 			SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
 			tf.setTimeZone(TimeZone.getTimeZone("UTC"));
 			s.append(df.format(startTime));
 			s.append("\t");
 			s.append(tf.format(startTime));
 			Date finishTime = new Date(startTime.getTime() + durationMs);
 			s.append("\t");
 			s.append(tf.format(finishTime));
 		}
 		return s.toString();
 	}
 
 	public void setSetting(String key, String value) {
 		Preconditions.checkNotNull(key);
 
 		// delete the key,value pair for the user
 		User user = getUser();
 		Filter keyFilter = new FilterPredicate("key", FilterOperator.EQUAL, key);
 		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL,
 				user);
 		Filter userAndKeyFilter = CompositeFilterOperator.and(userFilter,
 				keyFilter);
 		Query q = new Query("Setting").setFilter(userAndKeyFilter);
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		for (Entity entity : getEntities(q)) {
 			datastore.delete(entity.getKey());
 			System.out.println("deleted " + entity.getKey());
 		}
 
 		// add the key,value pair for the user
 		Key timesheetKey = KeyFactory.createKey("Timesheet", "Timesheet");
 		// kind=table,entity=row
 		Entity setting = new Entity("Setting", timesheetKey);
 		setting.setProperty("user", user);
 		setting.setProperty("key", key);
 		setting.setProperty("value", new Text(value));
 
 		datastore.put(setting);
 		System.out.println("setSetting " + key + "=" + value);
 	}
 
 	public String getSetting(String key) {
 		User user = getUser();
 		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL,
 				user);
 		Filter keyFilter = new FilterPredicate("key", FilterOperator.EQUAL, key);
 		Filter userKeyFilter = CompositeFilterOperator.and(userFilter,
 				keyFilter);
 		Query q = new Query("Setting").setFilter(userKeyFilter);
 		Iterable<Entity> results = getEntities(q);
 		Iterator<Entity> it = results.iterator();
 		String result;
 		if (!it.hasNext())
 			result = "";
 		else {
			String value = (String) it.next().getProperty("value");
 			if (value == null)
 				result = "";
 			else
 				result = value;
 		}
 		System.out.println("getSetting " + key + "=" + result);
 		return result;
 	}
 }
