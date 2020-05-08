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
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.Filter;
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
 	public static Key addPerson(Person person) {
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
 
 			txn.commit();
 		} finally {
 			if (txn.isActive()) {
 				txn.rollback();
 
 				return null;
 			}
 		}
 
 		return personKey;
 	}
 
 	/**
 	 * Get Person Entity from Person Key.
 	 * 
 	 * @param personKey
 	 *            Key of Person.
 	 * @return Entity object. Null if not found.
 	 */
 	public static Entity getPersonEntity(Key personKey) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		try {
 			Entity personEntity = datastore.get(personKey);
 
 			return personEntity;
 		} catch (EntityNotFoundException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Get Person object from Person Key.
 	 * 
 	 * @param personKey
 	 *            Key of Person.
 	 * @return Person object. Null if not found.
 	 */
 	public static Person getPerson(Key personKey) {
 		Entity personEntity = getPersonEntity(personKey);
 		if (personEntity != null) {
 			Person person = Person.createPerson(personEntity);
 
 			return person;
 		} else {
 			return null;
 		}
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
 	public static Entity getPersonEntity(String username) {
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		Filter usernameFilter = new FilterPredicate(Person.PROPERTY_USERNAME,
 				Query.FilterOperator.EQUAL, username);
 		Query q = new Query(Person.KIND_PERSON).setFilter(usernameFilter);
 
 		PreparedQuery pq = datastore.prepare(q);
 
 		FetchOptions fq = FetchOptions.Builder.withLimit(2);
 		List<Entity> personEntities = pq.asList(fq);
 
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
 	public static Person getPerson(String username) {
 		Entity personEntity = getPersonEntity(username);
 		if (personEntity != null) {
 			Person person = Person.createPerson(personEntity);
 
 			return person;
 		} else {
 			return null;
 		}
 	}
 	
 	public static Iterable<Entity> getPersonEntitiesByUsername(Iterable<String> usernames) {
 		HashSet<Entity> personEntities = new HashSet<Entity>();
 		
 		for (String username : usernames) {
			personEntities.add(getPersonEntity(username));
 		}
 		
 		return personEntities;
 	}
 	
 	public static TreeSet<Person> getPersonByUsername(Iterable<String> usernames) {
 		Iterable<Entity> personEntities = getPersonEntitiesByUsername(usernames);
 		
 		return Person.createPeople(personEntities);
 	}
 }
