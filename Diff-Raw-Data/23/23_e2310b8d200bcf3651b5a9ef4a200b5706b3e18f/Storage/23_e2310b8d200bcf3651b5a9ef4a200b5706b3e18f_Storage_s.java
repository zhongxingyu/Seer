package com.otanga;
 
 import com.google.appengine.api.datastore.*;
 import com.google.appengine.api.datastore.Query.*;
 
 public final class Storage {
 	
 	private final static DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
 		
 	private Storage() {}
 	
 	public static boolean userProfileExists(String userId) {
 		if (userId == null || userId.length() == 0)
 			throw new IllegalArgumentException();
 		
 		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);		
 		Query q = new Query("Profile").setFilter(userIdFilter).setKeysOnly();
 		
 		PreparedQuery pq = dataStore.prepare(q);
 		
 		FetchOptions fetchOption = FetchOptions.Builder.withLimit(1);
 		
 		return (pq.countEntities(fetchOption) > 0);
 	}
 	
 	public static Profile getUserProfile(String userId) {
 		if (userId == null || userId.length() == 0)
 			throw new IllegalArgumentException();
 		
 		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);		
 		Query q = new Query("Profile").setFilter(userIdFilter);
 		
 		PreparedQuery pq = dataStore.prepare(q);
 		
 		for (Entity entity : pq.asIterable()) {
 			return new DatastoreEntityProfile(entity);
 		}
 		
 		return null;
 	}
 	
 	public static Profile createUserProfile(String userId) {
 		if (userId == null || userId.length() == 0)
 			throw new IllegalArgumentException();
 
 		Profile userProfile = getUserProfile(userId);
 		if (userProfile != null)
 			return userProfile;
 		
 		Entity entity = new Entity("Profile");
 		entity.setProperty("userId", userId);
 		
 		//TODO: add more properties to the entity
 		
 		Key entityKey = dataStore.put(entity);
 		if (entityKey == null)
 			return null;
 
 		return new DatastoreEntityProfile(entity);
 	}
 	
 	public static boolean updateUserProfile(Profile userProfile) {
 		if (userProfile == null)
 			throw new IllegalArgumentException();
 		
 		if (!(userProfile instanceof DatastoreEntityProfile))
 			throw new IllegalArgumentException();
 		
 		Entity entity = ((DatastoreEntityProfile)userProfile).getEntity();
 		return (dataStore.put(entity) != null);
 	}
 	
 	public static void deleteUserProfile(Profile userProfile) {
 		if (userProfile != null)
 			throw new IllegalArgumentException();
 
 		if (!(userProfile instanceof DatastoreEntityProfile))
 			throw new IllegalArgumentException();
 		
 		Entity entity = ((DatastoreEntityProfile)userProfile).getEntity();
 		dataStore.delete(entity.getKey());
 	}
 	
 	public static void deleteUserProfile(String userId) {
 		if (userId == null || userId.length() == 0)
 			throw new IllegalArgumentException();
 
 		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);		
 		Query q = new Query("Profile").setFilter(userIdFilter).setKeysOnly();
 		
 		PreparedQuery pq = dataStore.prepare(q);
 		
 		for (Entity entity : pq.asIterable()) {
 			dataStore.delete(entity.getKey());
 		}
 	}
 	
 
 
 	
 	
 	
  	public static Entity getUser(String userId) {
 		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);		
 		Query q = new Query("Profile").setFilter(userIdFilter);
 		
 		PreparedQuery pq = dataStore.prepare(q);
 		
 		for (Entity entity : pq.asIterable()) {
 			return entity;
 		}
 		
 		return null;
 	}
 	
 	public static long storeUser(String userId) {
 		Entity entity = new Entity("Profile");
 		entity.setProperty("userId", userId);
 		
 		//TODO: add more properties to the entity
 		
 		Key entityKey = dataStore.put(entity);
 		return (entityKey != null) ? entityKey.getId() : -1;
 	}
 	
 	public static boolean updateUser(String userId, String nickname) {
 		
 		Entity entity = getUser(userId);
 		if (entity == null) return false;
 		
 		//TODO: update the entity's properties
 		entity.setProperty("nickname", nickname);
 
 
 		return (dataStore.put(entity) != null);
 	}
 
 }
