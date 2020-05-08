 package com.bingo.eatime.core;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Transaction;
 
 public class RestaurantManager {
 
 	/**
 	 * Add restaurant to database. It is necessary to check whether the
 	 * operation is successful or not.
 	 * 
 	 * @param restaurant
 	 *            Restaurant object, can be created by calling
 	 *            Restaurant.createRestaurant.
 	 * @return true if succeed, false if failed.
 	 */
 	public static boolean addRestaurant(Restaurant restaurant) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Transaction txn = datastore.beginTransaction();
 		try {
 			Entity restaurantEntity = new Entity(Restaurant.KIND_RESTAURANT,
					restaurant.getKey());
 			restaurantEntity.setProperty(Restaurant.PROPERTY_NAME,
 					restaurant.getName());
 			restaurantEntity.setProperty(Restaurant.PROPERTY_ADDRESS,
 					restaurant.getLocation());
 			restaurantEntity.setProperty(Restaurant.PROPERTY_PHONENUMBER,
 					restaurant.getPhoneNumber());
 
 			for (Category category : restaurant.getCategories()) {
 				Entity restaurantKeyEntity = CategoryManager
 						.createRestaurantKeyEntity(restaurant.getKey(),
 								category.getKey());
 				datastore.put(restaurantKeyEntity);
 			}
 
 			datastore.put(restaurantEntity);
 			txn.commit();
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 
 				return false;
 			}
 		}
 
 		return true;
 	}
 }
