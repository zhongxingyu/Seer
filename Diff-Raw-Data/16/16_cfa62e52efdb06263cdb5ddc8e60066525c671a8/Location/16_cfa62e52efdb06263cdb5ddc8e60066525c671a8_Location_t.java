 package models;
 
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import javax.annotation.Nullable;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.Logger;
 import play.cache.Cache;
 import play.db.ebean.Model;
 import play.db.ebean.Transactional;
 import util.LocationFilterJava;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.SqlQuery;
 import com.avaje.ebean.SqlRow;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 import controllers.Application;
 
 @Entity
 public class Location extends Model {
 
 	private static final long serialVersionUID = 1973445298197201545L;
 
 	@Id
 	private Long id;
 
 	@Column
 	private boolean isStart = false;
 
 	@Column(nullable = false)
 	private Date serverDate = null;
 
 	@Embedded
 	private User user = null;
 
 	@Column(nullable = false)
 	protected Double lat = 0.0d;
 
 	@Column(nullable = false)
 	protected Double lon = 0.0d;
 
 	public Location() {
 
 		this.serverDate = new Date();
 	}
 
 	public Location(final User aUser, final Double aLat, final Double aLon,
 			final Boolean aIsStart) {
 		this.user = aUser;
 		// this.lat = LocationFilterJava.formatDouble(aLat);
 		// this.lon = LocationFilterJava.formatDouble(aLon);
 		this.lat = aLat;
 		this.lon = aLon;
 		this.isStart = aIsStart;
 		this.serverDate = new Date();
 	}
 
 	public boolean isStart() {
 		return isStart;
 	}
 
 	public void setStart(final boolean isStart) {
 		this.isStart = isStart;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(final User user) {
 		this.user = user;
 	}
 
 	public Date getServerDate() {
 		return serverDate;
 	}
 
 	public void setServerDate(final Date serverDate) {
 		this.serverDate = serverDate;
 	}
 
 	public Double getLat() {
 		return lat;
 	}
 
 	public void setLat(final Double x) {
 		this.lat = x;
 	}
 
 	public Double getLon() {
 		return lon;
 	}
 
 	public void setLon(final Double y) {
 		this.lon = y;
 	}
 
 	private static Finder<Long, Location> finder = new Finder<Long, Location>(
 			Long.class, Location.class);
 
 	public static final String CACHE_KEY_SEPARATOR = ":";
 
 	public static void saveLocation(final Location l) {
 		l.save();
 	}
 
 	public static int locationsCount() {
 		return finder.findRowCount();
 	}
 
 	public static List<User> getUsers() {
 
 		SqlQuery query = Ebean
 				.createSqlQuery("SELECT user_id, server_date FROM location AS l WHERE server_date = "
 						+ "(SELECT MIN(server_date) FROM location WHERE user_id = l.user_id)"
 						+ "ORDER BY server_date");
 		List<SqlRow> locations = query.findList();
 
 		return Lists.transform(locations, new Function<SqlRow, User>() {
 
 			@Override
 			public User apply(@Nullable SqlRow arg0) {
 				return new User(arg0.getString("user_id"));
 			}
 
 		});
 
 	}
 
 	public static void removeLocationsForUser(String id) {
 
 		List<Location> locations = finder.where().eq("user_id", id).findList();
 		for (Location loc : locations) {
 			loc.delete();
 		}
 
 	}
 
 	public static List<Location> getLocationsForArea(final Float minLat,
 			final Float maxLat, final Float minLon, final Float maxLon) {
 
 		SqlQuery query = Ebean
 				.createSqlQuery("select * from location where user_id in (select distinct l.user_id from location l where l.lat >= "
 						+ minLat
 						+ " and l.lat <="
 						+ maxLat
 						+ " and l.lon >= "
 						+ minLon
 						+ " and l.lon <= "
 						+ maxLon
 						+ ") order by server_date");
 		// query.setMaxRows(arg0)
 		// query.setFirstRow(arg0)
 		List<SqlRow> locations = query.findList();
 		// int size = locations.size();
 		// Logger.info("locations : "+size);
 		return Lists.transform(locations, new Function<SqlRow, Location>() {
 
 			@Override
 			public Location apply(@Nullable SqlRow row) {
 				Location loc = new Location(new User(row.getString("user_id")),
 						row.getDouble("lat"), row.getDouble("lon"), row
 								.getBoolean("is_start"));
 				loc.id = row.getLong("id");
 				loc.serverDate = new Date(row.getLong("server_date"));
 				return loc;
 			}
 
 		});
 
 	}
 
 	@Transactional
 	public static void formatAll() {
 		Date start = new Date();
 		Logger.info("formatAll begin ...");
 		List<Location> all = finder.all();
 		for (Location l : all) {
 			l.setLat(LocationFilterJava.formatDouble(l.getLat()));
 			l.setLon(LocationFilterJava.formatDouble(l.getLon()));
 			l.save();
 		}
 		Date end = new Date();
 		long delta = end.getTime() - start.getTime();
 		Logger.info("formatAll in :" + delta + " ms");
 	}
 
 	public static List<Location> getBoundedLocations(final Float minLat,
 			final Float maxLat, final Float minLon, final Float maxLon,
 			final String givenUserId, final Integer zoom) {
 
 		Date start = new Date();
 		List<Location> result = null;
 
 		if (result == null) {
 			// mobile
 			if (givenUserId != null && !givenUserId.isEmpty()
 					&& !givenUserId.equals(Application.FULLSCREEN_MAP_ID)) {
 
 				String key = getCacheKey(minLat, maxLat, minLon, maxLon,
 						givenUserId, zoom);
 
 				try {
 					result = Cache.getOrElse(key, new Callable<List<Location>>() {
 						@Override
 						public List<Location> call() throws Exception {
 							Logger.info("gettLocationsForArea for mobile "+minLat+" : "+maxLat+" : "+ minLon+" : "+ maxLon);
 							return finder.where().eq("user.id", givenUserId)
 									.orderBy("serverDate").findList();
 						}
 					}, 1 * 60);
 				} catch (Exception e) {
 					Logger.error("getBoundedLocations error "+e);
 				}
 
 			}
 			// train station
 			else if (givenUserId.equals(Application.FULLSCREEN_MAP_ID)) {
 				String key = Application.FULLSCREEN_MAP_ID;
 
 				try {
 					result = Cache.getOrElse(key, new Callable<List<Location>>() {
 						@Override
 						public List<Location> call() throws Exception {
 							Logger.info("getLocationsForArea for fullscreen "+minLat+" : "+maxLat+" : "+ minLon+" : "+ maxLon);
 							return filterPoints(
 									zoom,
 									getLocationsForArea(minLat, maxLat, minLon,
 											maxLon));
 						}
 					}, 1 * 60);
 				} catch (Exception e) {
 					Logger.error("getBoundedLocations for fullscreen error "+e);
 				}
 			}
 			// map
 			else {
 				try {
 					result = Cache.getOrElse("default", new Callable<List<Location>>() {
 						@Override
 						public List<Location> call() throws Exception {
 							Logger.info("getDefaultLocationsForArea map");
 							return filterPoints(
									zoom,getAllLocationsForArea());
 						}
 					}, 10 * 60);
 				} catch (Exception e) {
 					Logger.error("getDefaultLocationsForArea error "+e);
 				}
 			}
 		}
 
 		Date end = new Date();
 		long delta = end.getTime() - start.getTime();
 		Logger.info("getBoundedLocations result:" + result.size() + " in "
 				+ delta + " ms");
 		return result;
 
 	}
 
	protected static List<Location> getAllLocationsForArea() {
		return finder.where().orderBy("serverDate").findList();
	}

 	protected static List<Location> getDefaultLocationsForArea() {
 		return getLocationsForArea(43.532745f, 43.690254f, 1.2011197f, 1.6008803f);
 	}
 
 	private static List<Location> filterPoints(Integer zoom,
 			List<Location> result) {
 		Date filterD = new Date();
 		Float coef = (1 / (float) zoom) * 100;
 		Double c = Math.pow(coef, 1.5) - 30;
 		List<Location> finalList = LocationFilterJava.filterNearLocations(
 				result, c);
 		result = finalList;
 
 		Date end = new Date();
 		long deltaFilter = end.getTime() - filterD.getTime();
 		Logger.info("filtered in :" + deltaFilter + " ms");
 		return result;
 	}
 
 	private static String getCacheKey(Float minLat, Float maxLat, Float minLon,
 			Float maxLon, String givenUserId, Integer zoom) {
 		return minLat + CACHE_KEY_SEPARATOR + maxLat + CACHE_KEY_SEPARATOR
 				+ minLon + CACHE_KEY_SEPARATOR + maxLon + CACHE_KEY_SEPARATOR
 				+ givenUserId + CACHE_KEY_SEPARATOR + zoom;
 	}
 
 	@Override
 	public String toString() {
 		return "[" + id + "] lat:" + lat + " lon:" + lon + " start:" + isStart;
 	}
 
 }
