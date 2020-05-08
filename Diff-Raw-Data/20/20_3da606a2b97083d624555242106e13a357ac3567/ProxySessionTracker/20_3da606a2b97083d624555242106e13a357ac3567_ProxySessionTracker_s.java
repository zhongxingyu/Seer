 /**
  * ************************************************************************
  * 
  *    server-objects - a contrib to the Qooxdoo project that makes server 
  *    and client objects operate seamlessly; like Qooxdoo, server objects 
  *    have properties, events, and methods all of which can be access from
  *    either server or client, regardless of where the original object was
  *    created.
  * 
  *    http://qooxdoo.org
  * 
  *    Copyright:
  *      2010 Zenesis Limited, http://www.zenesis.com
  * 
  *    License:
  *      LGPL: http://www.gnu.org/licenses/lgpl.html
  *      EPL: http://www.eclipse.org/org/documents/epl-v10.php
  *      
  *      This software is provided under the same licensing terms as Qooxdoo,
  *      please see the LICENSE file in the Qooxdoo project's top-level directory 
  *      for details.
  * 
  *    Authors:
  *      * John Spackman (john.spackman@zenesis.com)
  * 
  * ************************************************************************
  */
 package com.zenesis.qx.remote;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.apache.log4j.Logger;
 
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonSerializable;
 import com.fasterxml.jackson.databind.SerializerProvider;
 import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
 import com.zenesis.qx.event.EventManager;
 
 
 /**
  * This class tracks the uses of Proxies and ProxyTypes for a particular session; types
  * are only transmitted if not previously sent (in that session), and a mapping between
  * server and client instances/proxies is maintained.
  * 
  * This corresponds to a ProxyTracker on the client which can do the reverse of everything
  * done here.
  * 
  * NOTE about sessions: ProxyTracker tracks objects and types delivered for the current
  * instance of an application's session on the client; note that if the user refreshes 
  * the page the application reloads and starts a new session but the HTTP session maintained
  * by the servlet container does not reset.  You'll probably keep an instance of ProxyTracker
  * in the HttpSession for the application, which means that when the application restarts
  * it has to tell the server to clear down and start again; when this happens, the method
  * resetSession() is called, the state is lost, and the ProxyTracker instance is reused.
  * 
  * If you want more control over session resets you can override resetSession(); if you
  * want control over how the bootstrap object is created you can override createBootstrap().
  * 
  * @author John Spackman
  *
  */
 public class ProxySessionTracker {
 	
 	private static final Logger log = Logger.getLogger(ProxySessionTracker.class);
 	
 	/*
 	 * This class encapsulates data that needs to be sent to the server
 	 */
 	public static final class Proxy implements JsonSerializable {
 		public final int serverId;
 		public final Proxied proxied;
 		public final ProxyType proxyType;
 		public final HashSet<ProxyType> extraTypes;
 		public final boolean sendProperties;
 
 		/**
 		 * Constructor, used for existing objects
 		 * @param serverId
 		 */
 		public Proxy(Proxied proxied, int serverId, ProxyType proxyType, boolean sendProperties) {
 			super();
 			this.proxied = proxied;
 			this.serverId = serverId;
 			this.proxyType = proxyType;
 			this.extraTypes = null;
 			this.sendProperties = sendProperties;
 		}
 
 		/**
 		 * @param serverId
 		 * @param proxyType
 		 * @param createNew
 		 */
 		public Proxy(Proxied proxied, int serverId) {
 			super();
 			this.proxied = proxied;
 			this.serverId = serverId;
 			this.proxyType = null;
 			this.extraTypes = null;
 			this.sendProperties = false;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.codehaus.jackson.map.JsonSerializable#serialize(org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
 		 */
 		@Override
 		public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
 			jgen.writeStartObject();
 			jgen.writeNumberField("serverId", serverId);
 			if (extraTypes != null)
 				jgen.writeObjectField("classes", extraTypes);
 			
 			// If we have a proxyType, it also means that this is the first time the object is sent to the server
 			if (sendProperties) {
 				jgen.writeObjectField("clazz", proxyType);
 				if (!proxyType.isInterface()) {
 					// Write property values
 					boolean sentValues = false;
 					ArrayList<String> order = new ArrayList<String>();
 					for (ProxyType type = proxyType; type != null; type = type.getSuperType()) {
 						Collection<ProxyProperty> props = type.getProperties().values();
 						for (ProxyProperty prop : props) {
 							if (prop.isOnDemand())
 								continue;
 							if (!sentValues) {
 								jgen.writeObjectFieldStart("values");
 								sentValues = true;
 							}
 							try {
 								Object value = prop.getValue(proxied);
 								jgen.writeObjectField(prop.getName(), value);
 								order.add(prop.getName());
 							}catch(ProxyException e) {
 								throw new IllegalStateException(e.getMessage(), e);
 							}
 						}
 					}
 					if (sentValues)
 						jgen.writeEndObject();
 					if (!order.isEmpty())
 						jgen.writeObjectField("order", order);
 
 					// Write prefetch values
 					boolean prefetch = false;
 					for (ProxyType type = proxyType; type != null; type = type.getSuperType()) {
 						ProxyMethod[] methods = type.getMethods();
 						for (ProxyMethod method : methods) {
 							if (!method.isPrefetchResult())
 								continue;
 							if (!prefetch) {
 								jgen.writeObjectFieldStart("prefetch");
 								prefetch = true;
 							}
 							jgen.writeObjectField(method.getName(), method.getPrefetchValue(proxied));
 						}
 					}
 					if (prefetch)
 						jgen.writeEndObject();
 				}
 			}
 			jgen.writeEndObject();
 		}
 
 		/* (non-Javadoc)
 		 * @see com.fasterxml.jackson.databind.JsonSerializable#serializeWithType(com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.jsontype.TypeSerializer)
 		 */
 		@Override
 		public void serializeWithType(JsonGenerator gen, SerializerProvider sp, TypeSerializer ts)
 				throws IOException, JsonProcessingException {
 			serialize(gen, sp);
 		}
 		
 	}
 
 	/*
 	 * This encapsulates a POJO to distinguish it from a Proxied definition
 	 */
 	public static final class POJO {
 		public final Object pojo;
 
 		public POJO(Object pojo) {
 			super();
 			this.pojo = pojo;
 		}
 	}
 	
 	/*
 	 * Encapsulates a return value
 	 */
 	public static final class ReturnValue implements JsonSerializable {
 		public final Object value;
 
 		/**
 		 * @param value
 		 */
 		public ReturnValue(Object value) {
 			super();
 			this.value = value;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.codehaus.jackson.map.JsonSerializable#serialize(org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
 		 */
 		@Override
 		public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
 			jgen.writeStartObject();
 			jgen.writeStringField("type", "return-value");
 			jgen.writeObjectField("value", value);
 			if (value instanceof Proxied)
 				jgen.writeBooleanField("isProxy", true);
 			jgen.writeEndObject();
 		}
 
 		/* (non-Javadoc)
 		 * @see com.fasterxml.jackson.databind.JsonSerializable#serializeWithType(com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.jsontype.TypeSerializer)
 		 */
 		@Override
 		public void serializeWithType(JsonGenerator gen, SerializerProvider sp, TypeSerializer ts)
 				throws IOException, JsonProcessingException {
 			serialize(gen, sp);
 		}
 		
 	}
 	
 	/*
 	 * Class used to identify a property
 	 */
 	private static final class PropertyId {
 		private final Proxied proxied;
 		private final String propertyName;
 		
 		public PropertyId(Proxied proxied, String propertyName) {
 			super();
 			this.proxied = proxied;
 			this.propertyName = propertyName;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return proxied.hashCode() ^ propertyName.hashCode();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			PropertyId that = (PropertyId)obj;
 			return that.proxied == proxied && propertyName.equals(that.propertyName);
 		}
 	}
 
 	// All ProxyTypes which have already been sent to the client
 	private final HashSet<ProxyType> deliveredTypes = new HashSet<ProxyType>();
 	
 	// Mapping all objects that the client knows about against the ID we assigned to them
 	private final HashMap<Integer, Proxied> objectsById = new HashMap<Integer, Proxied>();
 	private final HashMap<Proxied, Integer> objectIds = new HashMap<Proxied, Integer>();
 	private final HashSet<Proxied> invalidObjects = new HashSet<Proxied>();
 	private final HashSet<PropertyId> knownOnDemandProperties = new HashSet<ProxySessionTracker.PropertyId>();
 
 	// The Object mapper
 	private ProxyObjectMapper objectMapper;
 
 	// Server IDs are assigned incrementally from 0
 	private int nextServerId;
 	
 	// Queue for properties and events
	private ThreadLocal<CommandQueue> queues;
 	
 	// Bootstrap object
 	private final Class<? extends Proxied> bootstrapClass;
 	private Proxied bootstrap;
 
 	/**
 	 * Creates a tracker for a session; if bootstrapClass is null you must override
 	 * createBootstrap() 
 	 * @param bootstrapClass
 	 */
 	public ProxySessionTracker(Class<? extends Proxied> bootstrapClass) {
 		super();
 		this.bootstrapClass = bootstrapClass;
 		objectMapper = new ProxyObjectMapper(this);
		queues = new ThreadLocal();
 	}
 	
 	/**
 	 * Creates a tracker for a session; if bootstrapClass is null you must override
 	 * createBootstrap() 
 	 * @param bootstrapClass
 	 */
 	public ProxySessionTracker(Class<? extends Proxied> bootstrapClass, File rootDir) {
 		super();
 		this.bootstrapClass = bootstrapClass;
 		objectMapper = new ProxyObjectMapper(this, false, rootDir);
		queues = new ThreadLocal();
 	}
 	
 	/**
 	 * Resets the session, called when the application restarts
 	 */
 	/*package*/ void resetSession() {
 		resetBootstrap();
		queues.remove();
 		deliveredTypes.clear();
 		objectsById.clear();
 		objectIds.clear();
 		nextServerId = 0;
 	}
 	
 	/**
 	 * Called to create a new instance of the bootstrap class 
 	 * @return
 	 */
 	protected Proxied createBootstrap() {
 		try {
 			return bootstrapClass.newInstance();
 		}catch(IllegalAccessException e) {
 			throw new IllegalStateException("Cannot create bootstrap instance from " + bootstrapClass + ": " + e.getMessage());
 		}catch(InstantiationException e) {
 			Throwable t = (Throwable)e;
 			throw new IllegalStateException("Cannot create bootstrap instance from " + bootstrapClass + ": " + t.getMessage(), t);
 		}
 	}
 	
 	/**
 	 * Called to initialise a new Bootstrap object after it has been set; this allows
 	 * initialisation of bootstrap to call getBootstrap.
 	 * @param boot
 	 */
 	protected void initialiseBootstrap(Proxied bootstrap) {
 		// Nothing
 	}
 
 	/**
 	 * Called to reset the bootstrap instance for a new session
 	 */
 	protected void resetBootstrap() {
 		bootstrap = null;
 	}
 	
 	/**
 	 * Returns the bootstrap, creating one if necessary
 	 * @return
 	 */
 	public Proxied getBootstrap() {
 		if (bootstrap == null) {
 			bootstrap = createBootstrap();
 			if (bootstrap == null)
 				throw new IllegalStateException("createBootstrap returned null");
 			initialiseBootstrap(bootstrap);
 		}
 		return bootstrap;
 	}
 	
 	/**
 	 * Creates an object which can be serialised by Jackson JSON and passed to the
 	 * client ProxyTracker to convert into a suitable client object 
 	 * @param obj
 	 * @return
 	 */
 	public synchronized Object getProxy(Proxied obj) {
 		if (obj == null)
 			return null;
 		
 		// See if it's an object the client already knows about
 		Integer serverId = objectIds.get(obj);
 		if (serverId != null) {
 			if (invalidObjects.remove(obj)) {
 				ProxyType type = getProxyType(obj);
 				return new Proxy(obj, serverId, type, true);
 			}
 			return new Proxy(obj, serverId);
 		}
 		
 		// See if the client already knows about the type
 		ProxyType type = getProxyType(obj);
 		
 		// Get an ID
 		serverId = nextServerId++;
 		
 		// Store mappings for ID and Proxied object
 		objectsById.put(serverId, obj);
 		objectIds.put(obj, serverId);
 		
 		// Return the information for the client
 		return new Proxy(obj, serverId, type, true);
 	}
 	
 	/**
 	 * Returns the ProxyType to use for a specific object
 	 * @param obj
 	 * @return
 	 */
 	protected ProxyType getProxyType(Object obj) {
 		ProxyType type = null;
 		if (obj instanceof DynamicTypeProvider)
 			type = ((DynamicTypeProvider)obj).getProxyType();
 		if (type == null)
 			type = ProxyTypeManager.INSTANCE.getProxyType((Class<Proxied>)obj.getClass());
 		return type;
 	}
 	
 	/**
 	 * Marks an object as invalid so that the next time it's sent to the client, all of the
 	 * property values will be resent
 	 * @param obj
 	 */
 	public synchronized void invalidateCache(Proxied proxied) {
 		if (objectIds.containsKey(proxied))
 			invalidObjects.add(proxied);
 	}
 	
 	/**
 	 * Causes the tracker to forget about the Proxied object
 	 * @param proxied
 	 */
 	public synchronized void forget(Proxied proxied) {
 		Integer id = objectIds.get(proxied);
 		if (id != null) {
 			objectIds.remove(proxied);
 			objectsById.remove(id);
 			invalidObjects.remove(proxied);
 		}
 	}
 	
 	/**
 	 * Causes the tracker to forget about the Proxied object
 	 * @param proxied
 	 */
 	public synchronized void forget(int serverId) {
 		Proxied proxied = objectsById.get(serverId);
 		if (proxied != null) {
 			objectIds.remove(proxied);
 			objectsById.remove(serverId);
 			invalidObjects.remove(proxied);
 		}
 	}
 	
 	/**
 	 * When the client creates an instance of a Proxied class addClientObject is used
 	 * to obtain an ID for it and add it to the lists of objects 
 	 * @param proxied
 	 * @return the new ID for the object
 	 */
 	public synchronized int addClientObject(Proxied proxied) {
 		if (objectIds.containsKey(proxied))
 			throw new IllegalArgumentException("Cannot add an existing server object as a client object");
 		
 		// Get an ID
 		int serverId = nextServerId++;
 		
 		// Store mappings for ID and Proxied object
 		objectsById.put(serverId, proxied);
 		objectIds.put(proxied, serverId);
 		
 		return serverId;
 	}
 	
 	/**
 	 * Returns the Proxied object that corresponds to a given value from the
 	 * client
 	 * @param serverId the ID that was originally passed to the client
 	 * @return
 	 */
 	public synchronized Proxied getProxied(int serverId) {
 		Proxied proxied = objectsById.get(serverId);
 		if (proxied == null)
 			throw new IllegalArgumentException("Cannot find Proxied instance for invalid serverId " + serverId);
 		return proxied;
 	}
 	
 	/**
 	 * Detects whether the Proxied object is tracked on the client
 	 * @param proxied
 	 * @return
 	 */
 	public synchronized boolean hasProxied(Proxied proxied) {
 		Integer serverId = objectIds.get(proxied);
 		return serverId != null;
 	}
 	
 	/**
 	 * Tests whether a ProxyType has already been sent to the client
 	 * @param type
 	 * @return
 	 */
 	public boolean isTypeDelivered(ProxyType type) {
 		return deliveredTypes.contains(type);
 	}
 	
 	/**
 	 * Registers a ProxyType as delivered to the client
 	 * @param type
 	 * @return
 	 */
 	public void setTypeDelivered(ProxyType type) {
 		if (deliveredTypes.contains(type))
 			throw new IllegalArgumentException("ProxyType " + type + " has already been sent to the client");
 		deliveredTypes.add(type);
 	}
 	
 	/**
 	 * Registers that a property has changed; this also fires a server event for
 	 * the property if an event is defined
 	 * @param proxied
 	 * @param propertyName
 	 * @param oldValue
 	 * @param newValue
 	 */
 	public void propertyChanged(Proxied keyObject, ProxyProperty property, Object newValue, Object oldValue) {
 		CommandQueue queue = getQueue();
 		if (!doesClientHaveObject(keyObject))
 			return;
 		if (property.isOnDemand() && !doesClientHaveValue(keyObject, property))
 			return; //queue.queueCommand(CommandId.CommandType.EXPIRE, keyObject, propertyName, null);
 		else
 			queue.queueCommand(CommandId.CommandType.SET_VALUE, keyObject, property.getName(), property.serialize(keyObject, newValue));
 		if (property.getEvent() != null) {
 			EventManager.fireDataEvent(keyObject, property.getEvent().getName(), newValue);
 		}
 	}
 	
 	/**
 	 * Forces the value of an on demand property to be sent to the client 
 	 * @param keyObject
 	 * @param propertyName
 	 * @param value
 	 */
 	public void preloadProperty(Proxied keyObject, ProxyProperty property, Object value) {
 		CommandQueue queue = getQueue();
 		if (!property.isOnDemand())
 			return;
 		queue.queueCommand(CommandId.CommandType.SET_VALUE, keyObject, property.getName(), property.serialize(keyObject, value));
 	}
 	
 	/**
 	 * Forces the value of an on demand property to be sent to the client 
 	 * @param keyObject
 	 * @param propertyName
 	 * @param value
 	 */
 	public void sendProperty(Proxied keyObject, ProxyProperty property) {
 		CommandQueue queue = getQueue();
 		if (!property.isOnDemand())
 			return;
 		try {
 			Object value = property.getValue(keyObject);
 			queue.queueCommand(CommandId.CommandType.SET_VALUE, keyObject, property.getName(), property.serialize(keyObject, value));
 		}catch(ProxyException e) {
 			throw new IllegalStateException(e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Helper static method to register that an on-demand property has changed and it's value should be
 	 * expired on the client, so that the next attempt to access it causes a refresh
 	 * @param proxied
 	 * @param propertyName
 	 * @param oldValue
 	 * @param newValue
 	 */
 	public void expireProperty(Proxied keyObject, ProxyProperty property) {
 		if (!doesClientHaveObject(keyObject))
 			return;
 		CommandQueue queue = getQueue();
 		if (property.isOnDemand())
 			queue.queueCommand(CommandId.CommandType.EXPIRE, keyObject, property.getName(), null);
 	}
 	
 	/**
 	 * Expires the on-demand property value
 	 * @param proxied
 	 * @param propertyName
 	 * @return
 	 */
 	public boolean expireOnDemandProperty(Proxied proxied, String propertyName) {
 		boolean existed = knownOnDemandProperties.remove(new PropertyId(proxied, propertyName));
 		return existed;
 	}
 	
 	/**
 	 * Loads a proxy type onto the client
 	 * @param clazz
 	 */
 	public void loadProxyType(Class<? extends Proxied> clazz) {
 		ProxyType type = ProxyTypeManager.INSTANCE.getProxyType(clazz);
 		if (type == null || isTypeDelivered(type))
 			return;
 		CommandQueue queue = getQueue();
 		queue.queueCommand(CommandId.CommandType.LOAD_TYPE, type, null, null);
 	}
 	
 	/**
 	 * Detects whether the client has a value for the given property of an object; this
 	 * returns true if the object has been sent and either the property is not ondemand
 	 * or the ondemand value has already been requested and sent.
 	 * @param proxied
 	 * @param prop
 	 * @return
 	 */
 	public boolean doesClientHaveValue(Proxied proxied, ProxyProperty prop) {
 		if (!objectIds.containsKey(proxied))
 			return false;
 		if (!prop.isOnDemand())
 			return true;
 		boolean existed = knownOnDemandProperties.contains(new PropertyId(proxied, prop.getName()));
 		return existed;
 	}
 	
 	/**
 	 * Records that the client has received an on-demand property value
 	 * @param proxied
 	 * @param prop
 	 */
 	public void setClientHasValue(Proxied proxied, ProxyProperty prop) {
 		if (!objectIds.containsKey(proxied) || !prop.isOnDemand())
 			return;
 		knownOnDemandProperties.add(new PropertyId(proxied, prop.getName()));
 	}
 	
 	/**
 	 * Detects whether the client has a value for the given property of an object; this
 	 * returns true if the object has been sent and either the property is not ondemand
 	 * or the ondemand value has already been requested and sent.
 	 * @param proxied
 	 * @param prop
 	 * @return
 	 */
 	public boolean doesClientHaveObject(Proxied proxied) {
 		return objectIds.containsKey(proxied);
 	}
 	
 	/**
 	 * Called to create a new instance of Queue; @see <code>getQueue</code>
 	 * @return
 	 */
 	protected CommandQueue createQueue() {
 		return new SimpleQueue();
 	}
 	
 	/**
 	 * Returns the Queue, creating one if necessary
 	 * @return
 	 */
 	public CommandQueue getQueue() {
		CommandQueue queue = queues.get();
		if (queue == null) {
 			queue = createQueue();
			queues.set(queue);
		}
 		return queue;
 	}
 	
 	/**
 	 * Detects whether there is any data to flush
 	 * @return
 	 */
 	public boolean hasDataToFlush() {
		CommandQueue queue = queues.get();
 		return queue != null && queue.hasDataToFlush();
 	}
 	
 	/**
 	 * Detects whether the queue needs to be "urgently" flushed
 	 * @return
 	 */
 	public boolean needsFlush() {
		CommandQueue queue = queues.get();
 		return queue != null && queue.needsFlush();
 	}
 	
 	/**
 	 * Writes an object and any required class definitions etc out to a JSON String
 	 * @param obj
 	 * @return
 	 */
 	public String toJSON(Object obj) {
 		StringWriter strWriter = new StringWriter();
 		try {
 			toJSON(obj, strWriter);
 		}catch(IOException e) {
 			throw new IllegalArgumentException(e);
 		}
 		return strWriter.toString();
 	}
 
 	/**
 	 * Writes an object and any required class definitions etc
 	 * @param obj
 	 * @return
 	 */
 	public void toJSON(Object obj, Writer writer) throws IOException {
 		if (!(obj instanceof Proxied))
 			obj = new POJO(obj);
 		
 		objectMapper.writeValue(writer, obj);
 	}
 	
 	/**
 	 * Parses JSON and returns a suitable object
 	 * @param str
 	 * @return
 	 */
 	public Object fromJSON(String str) {
 		try {
 			return fromJSON(new StringReader(str));
 		} catch (IOException e) {
 			log.error("Error while parsing: " + e.getClass() + ": " + e.getMessage() + "; code was: " + str + "\n");
 			throw new IllegalArgumentException(e);
 		}
 	}
 	
 	/**
 	 * Parses JSON and returns a suitable object
 	 * @param reader
 	 * @return
 	 * @throws IOException
 	 */
 	public Object fromJSON(Reader reader) throws IOException {
 		try {
 			Object obj = objectMapper.readValue(reader, Object.class);
 			return obj;
 		}catch(JsonParseException e) {
 			throw new IOException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Returns the Jackson JSON ObjectMapper
 	 * @return
 	 */
 	public ProxyObjectMapper getObjectMapper() {
 		return objectMapper;
 	}
 
 }
