 package com.bingo.eatime.core;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.TreeSet;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Transaction;
 
 public class PersonManager {
 
 	/**
 	 * Add Person to database.
 	 * 
 	 * @param person
 	 *            Person object.
 	 * @return Key of added person. Null if failed.
 	 */
 	public synchronized static Key addPerson(Person person) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Transaction txn = datastore.beginTransaction();
 		Key personKey;
 		try {
 			Entity personEntity = new Entity(Person.KIND_PERSON,
 					person.getUsername());
 			personEntity.setProperty(Person.PROPERTY_USERNAME,
 					person.getUsername());
 			personEntity.setProperty(Person.PROPERTY_FIRSTNAME,
 					person.getFirstName());
 			personEntity.setProperty(Person.PROPERTY_LASTNAME,
 					person.getLastName());
 			personEntity.setProperty(Person.PROPERTY_EMAIL, person.getEmail());
 
 			personKey = datastore.put(personEntity);
 			
 			if (person.getReadEventKeys() != null) {
 				for (Key eventKey : person.getReadEventKeys()) {
 					if (eventKey == null) {
 						txn.rollback();
 						throw new NullKeyException("Event Key is null.");
 					}
 					
 					Entity eventKeyEntity = createReadEventKeyEntity(eventKey, personKey);
 					datastore.put(eventKeyEntity);
 				}
 			}
 
 			txn.commit();
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 
 				return null;
 			}
 		}
 
 		return personKey;
 	}
 	
 	public synchronized static boolean addReadEvent(Key eventKey, Key personKey) {
 		HashSet<Key> eventKeys = new HashSet<Key>();
 		eventKeys.add(eventKey);
 		
 		return addReadEvents(eventKeys, personKey);
 	}
 	
 	public synchronized static boolean addReadEvents(Iterable<Key> eventKeys, Key personKey) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		
 		Transaction txn = datastore.beginTransaction();
 		try {
 			for (Key eventKey : eventKeys) {
 				if (eventKey == null) {
 					txn.rollback();
 					throw new NullKeyException("Event Key is null");
 				}
 				
 				Entity eventKeyEntity = createReadEventKeyEntity(eventKey, personKey);
 				datastore.put(eventKeyEntity);
 			}
 			txn.commit();
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Get Person Entity from Person Key.
 	 * 
 	 * @param personKey
 	 *            Key of Person.
 	 * @return Entity object. Null if not found.
 	 */
 	public synchronized static Entity getPersonEntity(Key personKey) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		try {
 			Entity personEntity = datastore.get(personKey);
 
 			return personEntity;
 		} catch (EntityNotFoundException e) {
 			return null;
 		}
 	}
 	
 	public synchronized static Entity getPersonEntity(String personKeyName) {
 		Key personKey = KeyFactory.createKey(Person.KIND_PERSON, personKeyName);
 		
 		return getPersonEntity(personKey);
 	}
 
 	/**
 	 * Get Person object from Person Key.
 	 * 
 	 * @param personKey
 	 *            Key of Person.
 	 * @return Person object. Null if not found.
 	 */
 	public synchronized static Person getPerson(Key personKey) {
 		Entity personEntity = getPersonEntity(personKey);
 		
 		if (personEntity != null) {
 			Person person = Person.createPerson(personEntity);
 
 			return person;
 		} else {
 			return null;
 		}
 	}
 	
 	public synchronized static Person getPerson(String personKeyName) {
 		Key personKey = KeyFactory.createKey(Person.KIND_PERSON, personKeyName);
 		
 		return getPerson(personKey);
 	}
 
 	/**
 	 * Get Person Entity by username.
 	 * 
 	 * @param username
 	 *            Username String.
 	 * @return Entity of person. Null if not found.
 	 * @throws MultipleEntityException
 	 *             Throws MultipleEntityException if multiple same usernames
 	 *             found.
 	 */
 	public synchronized static Entity getPersonEntityByUsername(String username) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Filter usernameFilter = new FilterPredicate(Person.PROPERTY_USERNAME,
 				Query.FilterOperator.EQUAL, username);
 		Query q = new Query(Person.KIND_PERSON).setFilter(usernameFilter);
 
 		PreparedQuery pq = datastore.prepare(q);
 
 		FetchOptions fo = FetchOptions.Builder.withLimit(2);
 		List<Entity> personEntities = pq.asList(fo);
 
 		if (personEntities.size() == 1) {
 			return personEntities.get(0);
 		} else if (personEntities.size() > 1) {
 			throw new MultipleEntityException("Multiple same usernames exists.");
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get Person Object by username.
 	 * 
 	 * @param username
 	 *            Username String.
 	 * @return Person Object. Null if not found.
 	 * @throws MultipleEntityException
 	 *             Throws MultipleEntityException if multiple same usernames
 	 *             found.
 	 */
 	public synchronized static Person getPersonByUsername(String username) {
 		Entity personEntity = getPersonEntityByUsername(username);
 		if (personEntity != null) {
 			Person person = Person.createPerson(personEntity);
 
 			return person;
 		} else {
 			return null;
 		}
 	}
 	
 	public synchronized static Iterable<Entity> getPersonEntitiesByUsername(Iterable<String> usernames) {
 		HashSet<Entity> personEntities = new HashSet<Entity>();
 		
 		for (String username : usernames) {
 			Entity personEntity = getPersonEntityByUsername(username);
 			if (personEntity != null) {
 				personEntities.add(personEntity);
 			}
 		}
 		
 		return personEntities;
 	}
 	
 	public synchronized static TreeSet<Person> getPeopleByUsername(Iterable<String> usernames) {
 		Iterable<Entity> personEntities = getPersonEntitiesByUsername(usernames);
 		
 		return Person.createPeople(personEntities);
 	}
 	
 	public synchronized static HashSet<Key> getReadEventKeys(Key personKey) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		
 		Query q = new Query(Person.KIND_READ_EVENTKEY, personKey);
 		
 		PreparedQuery pq = datastore.prepare(q);
 		
 		HashSet<Key> readEventKeys = null;
 		for (Entity entity : pq.asIterable()) {
 			if (readEventKeys == null) {
 				readEventKeys = new HashSet<Key>();
 			}
 			
 			Key eventKey = (Key) entity.getProperty(Person.PROPERTY_EVENTKEY);
 			readEventKeys.add(eventKey);
 		}
 		
 		return readEventKeys;
 	}
 	
 	public synchronized static Iterable<Entity> getReadEventEntities(Key personKey) {
 		HashSet<Key> readEventKeys = getReadEventKeys(personKey);
 		
 		HashSet<Entity> readEventEntities = null;
 		if (readEventKeys != null) {
 			for (Key eventKey : readEventKeys) {
 				if (readEventEntities == null) {
 					readEventEntities = new HashSet<Entity>();
 				}
 				
 				Entity readEventEntity = EventManager.getEventEntity(eventKey);
 				readEventEntities.add(readEventEntity);
 			}
 			
 			return readEventEntities;
 		} else {
 			return null;
 		}
 	}
 	
 	public synchronized static TreeSet<Event> getReadEvents(Key personKey) {
 		Iterable<Entity> readEventEntities = getReadEventEntities(personKey);
 		
 		if (readEventEntities != null) {
 			TreeSet<Event> readEvents = Event.createEvents(readEventEntities);
 			
 			return readEvents;
 		} else {
 			return null;
 		}
 	}
 
 	
 	public synchronized static Iterable<Entity> getInviteEventEntities(Key personKey, boolean filterReadEvent) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		
 		Query q = new Query(Event.KIND_PERSONKEY);
 		Filter personKeyFilter = new FilterPredicate(Event.PROPERTY_PERSONKEY, FilterOperator.EQUAL, personKey);
 		q.setFilter(personKeyFilter);
 		
 		PreparedQuery pq = datastore.prepare(q);
 		
 		HashSet<Key> readEventKeys = null;
 		if (filterReadEvent) {
			Iterable<Entity> readEventEntities = getReadEventEntities(personKey);
			if (readEventEntities != null) {
				readEventKeys = new HashSet<Key>();
				for (Entity entity : readEventEntities) {
					readEventKeys.add((Key) entity.getProperty(Person.PROPERTY_EVENTKEY));
				}
			}
 		}
 		
 		HashSet<Entity> inviteEventEntities = new HashSet<Entity>();
 		try {
 			for (Entity entity : pq.asIterable()) {
 				Key inviteEventKey = entity.getParent();
 				if (filterReadEvent && readEventKeys != null) {
 					if (!readEventKeys.contains(inviteEventKey)) {
 						Entity inviteEventEntity = datastore.get(inviteEventKey);
 						inviteEventEntities.add(inviteEventEntity);
 					}
 				} else {
 					Entity inviteEventEntity = datastore.get(inviteEventKey);
 					inviteEventEntities.add(inviteEventEntity);
 				}
 			}
 		} catch (EntityNotFoundException e) {
 			return null;
 		}
 		
 		return inviteEventEntities;
 	}
 	
 	public synchronized static TreeSet<Event> getInviteEvents(Key personKey, boolean filterReadEvent) {
 		Iterable<Entity> inviteEventEntities = getInviteEventEntities(personKey, filterReadEvent);
 		
 		if (inviteEventEntities != null) {
 			TreeSet<Event> inviteEvents = Event.createEvents(inviteEventEntities);
 			
 			return inviteEvents;
 		} else {
 			return null;
 		}
 	}
 	
 	public synchronized static TreeSet<Event> getInviteEvents(Key personKey) {
 		return getInviteEvents(personKey, false);
 	}
 	
 	public synchronized static Iterable<Entity> getJoinEventEntities(Key personKey) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 		
 		Query q = new Query(Event.KIND_JOIN_PERSONKEY);
 		Filter personKeyFilter = new FilterPredicate(Event.PROPERTY_PERSONKEY, FilterOperator.EQUAL, personKey);
 		q.setFilter(personKeyFilter);
 		
 		PreparedQuery pq = datastore.prepare(q);
 		
 		HashSet<Entity> joinEventEntities = new HashSet<Entity>();
 		try {
 			for (Entity entity : pq.asIterable()) {
 				Key joinEventKey = entity.getParent();
 				
 				Entity joinEventEntity = datastore.get(joinEventKey);
 				joinEventEntities.add(joinEventEntity);
 			}
 		} catch (EntityNotFoundException e) {
 			return null;
 		}
 		
 		return joinEventEntities;
 	}
 	
 	public synchronized static TreeSet<Event> getJoinEvents(Key personKey) {
 		Iterable<Entity> joinEventEntities = getJoinEventEntities(personKey);
 		
 		if (joinEventEntities != null) {
 			TreeSet<Event> joinEvents = Event.createEvents(joinEventEntities);
 			
 			return joinEvents;
 		} else {
 			return null;
 		}
 	}
 	
 	protected synchronized static Entity createReadEventKeyEntity(Key eventKey, Key personKey) {
 		Entity readEventKeyEntity = new Entity(Person.KIND_READ_EVENTKEY, eventKey.getId(), personKey);
 		readEventKeyEntity.setProperty(Person.PROPERTY_EVENTKEY, eventKey);
 		
 		return readEventKeyEntity;
 	}
 }
