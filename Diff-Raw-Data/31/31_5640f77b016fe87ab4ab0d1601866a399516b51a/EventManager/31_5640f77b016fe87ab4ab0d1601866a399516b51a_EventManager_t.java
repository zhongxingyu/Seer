 package com.bingo.eatime.core;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Transaction;
 
 public class EventManager {
 
 	public static Key addEvent(Event event) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Transaction txn = datastore.beginTransaction();
 		Key key;
 		try {
			Key restaurantKey = event.getRestaurantKey();
			if (restaurantKey == null) {
				throw new NullKeyException("Restaurant Key is null.");
			}

			Entity eventEntity = new Entity(Event.KIND_EVENT, restaurantKey);
 			eventEntity.setProperty(Event.PROPERTY_NAME, event.getName());
 			eventEntity.setProperty(Event.PROPERTY_RESTAURANTKEY,
 					event.getRestaurantKey());
 			eventEntity.setProperty(Event.PROPERTY_TIME, event.getTime());
 
			key = datastore.put(eventEntity);

 			for (Person person : event.getInvites()) {
				
 			}
 
 			txn.commit();
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 
 				return null;
 			}
 		}
 
 		return key;
 	}
 
 	protected static Entity createPersonKeyEntity(Key personKey, Key eventKey) {
 		Entity personKeyEntity = new Entity(Event.KIND_PERSONKEY, eventKey);
 		personKeyEntity.setProperty(Event.PROPERTY_PERSONKEY, personKey);
 
 		return personKeyEntity;
 	}
 
 }
