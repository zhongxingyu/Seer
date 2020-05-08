 package com.github.davidmoten.et;
 
 import static com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService;
 import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
 import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
 import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
 import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
 
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TimeZone;
 
 import com.github.davidmoten.geo.Coverage;
 import com.github.davidmoten.geo.GeoHash;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PropertyContainer;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 /**
  * Encapsulates database access. GoogleAppEngine (BigTable) used for
  * persistence.
  * 
  * @author dxm
  * 
  */
 public class Database {
 
 	/**
 	 * Get the current {@link User}.
 	 * 
 	 * @return
 	 */
 	private static User getUser() {
 		UserService userService = UserServiceFactory.getUserService();
 		return userService.getCurrentUser();
 	}
 
 	public void saveReport(Date time, double lat, double lon,
 			Map<String, String> ids) {
 
 		User user = getUser();
 
 		// kind=db,name=schema,
 		Key entityTrackingKey = KeyFactory.createKey("EntityTracking",
 				"EntityTracking");
 		// kind=table,entity=row
 		Entity report = new Entity("Report", entityTrackingKey);
 		report.setProperty("user", user);
 		report.setProperty("lat", lat);
 		report.setProperty("lon", lon);
 		report.setProperty("time", time);
 		// set ids
 		for (Entry<String, String> entry : ids.entrySet()) {
 			report.setProperty(entry.getKey(), entry.getValue());
 		}
 		// set geohashes
 		for (int i = 1; i <= GeoHash.MAX_HASH_LENGTH; i++) {
 			report.setProperty("geohash" + i, GeoHash.encodeHash(lat, lon, i));
 		}
 
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		datastore.put(report);
 		System.out.println("saved");
 	}
 
 	public void writeReportsAsJson(String user, final double topLeftLat,
 			final double topLeftLon, final double bottomRightLat,
 			final double bottomRightLon, Date start, Date finish,
 			String idName, String idValue, PrintWriter out) {
 
 		DatastoreService datastore = getDatastoreService();
 		Filter startTimeFilter = new FilterPredicate("time",
 				GREATER_THAN_OR_EQUAL, start);
 		Filter finishTimeFilter = new FilterPredicate("time", LESS_THAN, finish);
 		Filter userFilter = new FilterPredicate("user", EQUAL, user);
 		Filter filter = and(userFilter, startTimeFilter, finishTimeFilter);
 		Coverage coverage = GeoHash.coverBoundingBox(topLeftLat, topLeftLon,
 				bottomRightLat, bottomRightLon);
 		for (String hash : coverage.getHashes()) {
 			filter = and(filter, new FilterPredicate("geohash" + hash.length(),
 					EQUAL, hash));
 		}
 		if (idName != null && idValue != null) {
 			filter = and(filter, new FilterPredicate(idName, EQUAL, idValue));
 		}
 		Query query = new Query("Report").setFilter(filter);
 		FetchOptions options = FetchOptions.Builder.withChunkSize(1000);
 		Iterable<Entity> it = datastore.prepare(query).asIterable(options);
 
 		// filter results within bounding box because geohash query inexact.
 		it = Iterables.filter(
 				it,
 				createBoundingBoxPredicate(topLeftLat, topLeftLon,
 						bottomRightLat, bottomRightLon));
 
 		writeReportsAsJson(it, out);
 	}
 
 	@VisibleForTesting
 	static void writeReportsAsJson(Iterable<? extends PropertyContainer> it,
 			PrintWriter out) {
		out.println("{ \"reports\" :[");
 		boolean first = true;
 		for (PropertyContainer ent : it) {
 			if (!first) {
 				out.println(",");
 			}
			out.print("  {");
 			boolean firstEntry = true;
 			for (Entry<String, Object> entry : ent.getProperties().entrySet()) {
 				String key = entry.getKey();
 				Object value = entry.getValue();
 				if (!firstEntry)
 					out.print(",");
 				if (value instanceof Date) {
 					DateFormat df = new SimpleDateFormat(
 							"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 					df.setTimeZone(TimeZone.getTimeZone("UTC"));
 					out.print("\"" + key + "\":\"" + df.format((Date) value)
 							+ "\"");
 				} else if (value instanceof Number)
 					out.print("\"" + key + "\":" + value + "");
 				else
 					out.print("\"" + key + "\":\"" + value + "\"");
 				firstEntry = false;
 			}
 			out.print("}");
 			first = false;
 		}
 		out.println("]}");
 		out.flush();
 	}
 
 	private static Predicate<Entity> createBoundingBoxPredicate(
 			final double topLeftLat, final double topLeftLon,
 			final double bottomRightLat, final double bottomRightLon) {
 		return new Predicate<Entity>() {
 			@Override
 			public boolean apply(Entity e) {
 				double lat = (double) e.getProperty("lat");
 				double lon = (double) e.getProperty("lon");
 				return lat >= bottomRightLat && lat <= topLeftLat
 						&& longitudeBetween(topLeftLon, bottomRightLon, lon);
 			}
 		};
 	}
 
 	private static boolean longitudeBetween(double a, double b, double c) {
 		a = GeoHash.to180(a);
 		b = GeoHash.to180(b);
 		c = GeoHash.to180(c);
 		if (b < a)
 			return longitudeBetween(a, b + 360, c);
 		else
 			return a <= c && c <= b;
 	}
 
 }
