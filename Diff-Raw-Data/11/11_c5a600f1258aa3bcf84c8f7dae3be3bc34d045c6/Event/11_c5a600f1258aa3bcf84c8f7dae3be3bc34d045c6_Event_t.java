 package com.bingo.eatime.core;
 
 import java.util.Comparator;
 import java.util.Date;
 import java.util.TreeSet;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 
 public final class Event {
 
 	public static final String KIND_EVENT = "event";
 	public static final String KIND_PERSONKEY = "person-key";
 
 	public static final String PROPERTY_NAME = "name";
 	public static final String PROPERTY_RESTAURANTKEY = "restaurant-id-key";
 	public static final String PROPERTY_CREATOR = "creator";
 	public static final String PROPERTY_TIME = "time";
 	public static final String PROPERTY_PERSONKEY = "person-id-key";
 
 	private Key key;
 	private String name;
 	private Key restaurantKey;
 	private Person creator;
 	private Date time;
 	private TreeSet<Person> invites;
 
 	private Event() {
 	}
 
 	public Key getKey() {
 		return key;
 	}
 
 	private Event setKey(Key key) {
 		this.key = key;
 
 		return this;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	private Event setName(String name) {
 		this.name = name;
 
 		return this;
 	}
 
 	public Key getRestaurantKey() {
 		return restaurantKey;
 	}
 
 	private Event setRestaurantKey(Key restaurantKey) {
 		this.restaurantKey = restaurantKey;
 
 		return this;
 	}
 
 	public Person getCreator() {
 		return creator;
 	}
 
 	private Event setCreator(Person creator) {
 		this.creator = creator;
 
 		return this;
 	}
 
 	public Date getTime() {
 		return time;
 	}
 
 	private Event setTime(Date time) {
 		this.time = time;
 
 		return this;
 	}
 
 	public TreeSet<Person> getInvites() {
 		return invites;
 	}
 
 	private Event setInvites(TreeSet<Person> invites) {
 		this.invites = invites;
 
 		return this;
 	}
 
 	protected Event addInvite(Person invite) {
 		if (this.invites == null) {
 			this.invites = Person.newPeople();
 		}
 
 		this.invites.add(invite);
 
 		return this;
 	}
 
 	protected Event addInvites(Iterable<Person> invites) {
 		if (this.invites == null) {
 			this.invites = Person.newPeople();
 		}
 
 		for (Person invite : invites) {
 			this.invites.add(invite);
 		}
 
 		return this;
 	}
 
 	protected static TreeSet<Event> newEvents() {
 		TreeSet<Event> events = new TreeSet<Event>(new Comparator<Event>() {
 			@Override
 			public int compare(Event o1, Event o2) {
 				return o1.getTime().compareTo(o2.getTime());
 			}
 		});
 
 		return events;
 	}
 
 	// Key will be null if created using this method.
 	public static Event createEvent(String name, Key restaurantKey,
 			Person creator, Date time) {
 		return createEvent(name, restaurantKey, creator, time, null);
 	}
 
 	// Key will be null if created using this method.
 	public static Event createEvent(String name, Key restaurantKey,
 			Key creatorKey, Date time) {
 		return createEvent(name, restaurantKey, creatorKey, time, null);
 	}
 
 	// Key will be null if created using this method.
 	public static Event createEvent(String name, Key restaurantKey,
 			Person creator, Date time, Iterable<Person> invites) {
 		Event event = new Event();
 		event.setName(name).setRestaurantKey(restaurantKey).setCreator(creator)
 				.setTime(time).addInvites(invites);
 
 		return event;
 	}
 
 	// Key will be null if created using this method.
 	public static Event createEvent(String name, Key restaurantKey,
 			Key creatorKey, Date time, Iterable<Person> invites) {
 		Event event = new Event();
 		event.setName(name).setRestaurantKey(restaurantKey).setTime(time)
 				.setInvites(null);
 
 		DatastoreService datastore = DatastoreServiceFactory
 				.getDatastoreService();
 
 		try {
 			Entity creatorEntity = datastore.get(creatorKey);
 			Person creator = Person.createPerson(creatorEntity);
 
 			event.setCreator(creator);
 		} catch (EntityNotFoundException e) {
 			return null;
 		}
 
 		return event;
 	}
 
 	public static Event createEvent(Entity entity) {
 		if (entity.getKind().equals(KIND_EVENT)) {
 			Event event = new Event();
 			event.setKey(entity.getKey());
 			event.setName((String) entity.getProperty(PROPERTY_NAME));
 			event.setRestaurantKey((Key) entity
 					.getProperty(PROPERTY_RESTAURANTKEY));
			event.setTime((Date) entity.getProperty(PROPERTY_TIME));
 
 			Key creatorKey = (Key) entity.getProperty(PROPERTY_CREATOR);
 			Person creator = PersonManager.getPerson(creatorKey);
 			event.setCreator(creator);
 
 			Iterable<Entity> inviteEntities = EventManager
 					.getInviteEntities(entity.getKey());
 			TreeSet<Person> invites = Person.createPeople(inviteEntities);
 			event.setInvites(invites);
 
 			return event;
 		} else {
 			throw new EntityKindNotMatchException(
 					"Entity Kind must be KIND_EVENT instead of "
 							+ entity.getKind() + ".");
 		}
 	}
 
 	public static TreeSet<Event> createEvents(Iterable<Entity> entities) {
 		TreeSet<Event> events = newEvents();
 		boolean empty = true;
 
 		for (Entity entity : entities) {
 			empty = false;
 			Event event = createEvent(entity);
 			events.add(event);
 		}
 
 		if (empty) {
 			return null;
 		} else {
 			return events;
 		}
 	}
 
 }
