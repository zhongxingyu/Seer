 package com.chaseit;
 
 import java.util.List;
 
 import com.chaseit.models.CIUser;
 import com.chaseit.models.Hunt;
 import com.chaseit.models.Location;
 import com.chaseit.models.UserHunt;
 import com.chaseit.models.UserHunt.HuntStatus;
 import com.parse.DeleteCallback;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 public class ParseHelper {
 	// information retrieval
 	public static void getAllHuntsByCreateDate(FindCallback<Hunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Hunt> query = ParseQuery.getQuery(Hunt.class);
 		query.orderByDescending("createdAt");
 		query.findInBackground(callback);
 	}
 
 	public static void getAllHuntsByAvgRating(FindCallback<Hunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Hunt> query = ParseQuery.getQuery(Hunt.class);
 		query.orderByDescending("avgrating");
 		query.findInBackground(callback);
 	}
 
 	public static void getAllHuntsByNumRatings(FindCallback<Hunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Hunt> query = ParseQuery.getQuery(Hunt.class);
 		query.orderByDescending("numratings");
 		query.findInBackground(callback);
 	}
 
 	public static void getHuntByObjectId(String objectId,
 			GetCallback<Hunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Hunt> query = ParseQuery.getQuery(Hunt.class);
 		query.whereEqualTo("objectId", objectId);
 		query.getFirstInBackground(callback);
 	}
 
 	public static void getAllHuntsByProximity(ParseGeoPoint currentLocation,
 			FindCallback<Hunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Hunt> query = ParseQuery.getQuery(Hunt.class);
 		query.whereNear("startlocation", currentLocation);
 		query.findInBackground(callback);
 	}
 
 	public static void getMyHuntsCreated(FindCallback<Hunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Hunt> query = ParseQuery.getQuery(Hunt.class);
 		query.whereEqualTo("creator", CIUser.getCurrentUser());
 		query.findInBackground(callback);
 	}
 
 	public static void getMyHuntsCompleted(FindCallback<UserHunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<UserHunt> query = ParseQuery.getQuery(UserHunt.class);
		query.whereEqualTo("userobjectId", CIUser.getCurrentUser().getObjectId());
 		query.whereEqualTo("status", HuntStatus.COMPLETED.toString());
 		query.findInBackground(callback);
 	}
 
 	public static void getMyHuntsInProgress(FindCallback<UserHunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<UserHunt> query = ParseQuery.getQuery(UserHunt.class);
		query.whereEqualTo("userobjectId", CIUser.getCurrentUser().getObjectId());
		query.whereEqualTo("status", HuntStatus.IN_PROGRESS.toString());
 		query.findInBackground(callback);
 	}
 
	public static void getHuntsInProgressForUser(ParseUser user, FindCallback<UserHunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<UserHunt> query = ParseQuery.getQuery(UserHunt.class);
 		query.whereEqualTo("userobjectId", user.getObjectId());
 		query.whereEqualTo("status", HuntStatus.IN_PROGRESS.toString());
 		query.findInBackground(callback);
 	}
 
	public static void getHuntInProgressGivenHunt(Hunt hunt, FindCallback<UserHunt> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<UserHunt> query = ParseQuery.getQuery(UserHunt.class);
 		query.whereEqualTo("huntobjectId", hunt.getObjectId());
 		query.findInBackground(callback);
	}  
 
 	public static void getHuntInProgressGivenHuntAndUser(Hunt hunt,
 			ParseUser user, FindCallback<UserHunt> callback) {
 		if (callback == null || hunt == null)
 			return;
 		ParseQuery<UserHunt> query = ParseQuery.getQuery(UserHunt.class);
 		query.whereEqualTo("huntobjectId", hunt.getObjectId());
 		query.whereEqualTo("status", HuntStatus.IN_PROGRESS.toString());
 		query.whereEqualTo("userobjectId", user.getObjectId());
 		query.findInBackground(callback);
 	}
 
 	public static void getLocationByObjectId(String objectId,
 			FindCallback<Location> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
 		query.whereEqualTo("objectId", objectId);
 		query.findInBackground(callback);
 	}
 
 	public static void getLocationsforHunt(Hunt hunt,
 			FindCallback<Location> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
 		query.whereEqualTo("parenthunt", hunt);
 		query.findInBackground(callback);
 	}
 
 	public static void getLocationByHuntAndIndex(Hunt hunt, int index,
 			FindCallback<Location> callback) {
 		if (callback == null)
 			return;
 		ParseQuery<Location> query = ParseQuery.getQuery(Location.class);
 		query.whereEqualTo("parenthunt", hunt);
 		query.whereEqualTo("index", index);
 		query.findInBackground(callback);
 	}
 
 	// information create/update
 	public static void saveUser(ParseUser user, SaveCallback callback) {
 		if (callback == null) {
 			user.saveInBackground();
 		} else {
 			user.saveInBackground(callback);
 		}
 	}
 
 	public static void rateHunt(Hunt hunt, int rating, SaveCallback callback) {
 		double avgRating = Math.abs(hunt.getAvgRating());
 		int numRatings = Math.abs(hunt.getNumRatings());
 
 		double totalRating = avgRating * numRatings;
 		++numRatings;
 		avgRating = totalRating / numRatings;
 		hunt.setAvgRating(avgRating);
 		hunt.setNumRatings(numRatings);
 
 		if (callback == null) {
 			hunt.saveInBackground();
 		} else {
 			hunt.saveInBackground(callback);
 		}
 	}
 
 	public static void deleteHunt(final Hunt hunt, final DeleteCallback callback) {
 		getLocationsforHunt(hunt, new FindCallback<Location>() {
 			@Override
 			public void done(List<Location> huntPoints, ParseException e) {
 				if (e == null) {
 					// delete all hunt locations
 					for (Location location : huntPoints) {
 						location.deleteInBackground();
 					}
 					// delete hunt
 					if (callback == null) {
 						hunt.deleteInBackground();
 					} else {
 						hunt.deleteInBackground(callback);
 					}
 
 				} else {
 					e.printStackTrace();
 				}
 			}
 
 		});
 	}
 
 }
