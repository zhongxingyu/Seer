 /**
  * Copyright 2010-2011 eBusiness Information, Groupe Excilys (www.excilys.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.excilys.spring.mom.client;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 /**
  * Represents a client MOM. This class implements the listener system of subscribing and unsubscribing methods.
  * <p>
  * <b>NOTE:</b> Concret sub-classes of {@link MOMClient MOMClient} has to override these two methods to actually
  * subscribe and unsubscribe to the MOM server.
  * 
  * @author dvilleneuve
  */
 @Service
 public abstract class MOMClient {
 	protected static final Logger LOGGER = LoggerFactory.getLogger(MOMClient.class);
 
 	private final List<MOMClientListener> clientListeners;
 	private final Map<String, Set<MOMMethodHandler>> topicMethodHandlers;
 	private final String hostname;
 	private final int port;
 
 	/**
 	 * Create an instance of MOMClient for a specific {@code hostname} and {@code port}, which will auto-connect to the
 	 * server when an instance is created.
 	 * 
 	 * @param hostname
 	 *            of the mom server
 	 * @param port
 	 *            of the mom server
 	 */
 	public MOMClient(String hostname, int port) {
 		this(hostname, port, true);
 	}
 
 	/**
 	 * Create an instance of MOMClient for a specific {@code hostname} and {@code port}. If {@code autoconnect}
 	 * parameter is true, the instance will auto-connect when it's created.
 	 * 
 	 * @param hostname
 	 *            of the mom server
 	 * @param port
 	 *            of the mom server
 	 * @param autoconnect
 	 */
 	public MOMClient(String hostname, int port, boolean autoconnect) {
 		this.clientListeners = new LinkedList<MOMClientListener>();
 		this.topicMethodHandlers = new HashMap<String, Set<MOMMethodHandler>>();
 		this.hostname = hostname;
 		this.port = port;
 	}
 
 	/**
 	 * Connect to the MOM server with {@code hostname} and {@code port} given in the constructor.
 	 */
 	public abstract void connect();
 
 	/**
 	 * Disconnect from the MOM server.
 	 */
 	public abstract void disconnect();
 
 	/**
 	 * Subscribe to a topic by registering a listener.
 	 * 
 	 * @param topic
 	 */
 	public void subscribe(String topic, MOMMethodHandler momMethodHandler) {
 		Set<MOMMethodHandler> methodHandlers = topicMethodHandlers.get(topic);
 
 		if (methodHandlers == null) {
 			methodHandlers = new TreeSet<MOMMethodHandler>();
 			topicMethodHandlers.put(topic, methodHandlers);
 		}
 
 		methodHandlers.add(momMethodHandler);
 	}
 
 	/**
 	 * Unsubscribe from a specific topic by unregistering all listeners.
 	 * 
 	 * @param topic
 	 */
 	public void unsubscribe(String topic) {
 		topicMethodHandlers.remove(topic);
 	}
 
 	/**
 	 * Publish a new message on a specific topic to the MOM server. Each connected device which subscribed to this topic
 	 * will receive this message.
 	 * 
 	 * @param topic
 	 * @param message
 	 */
 	public abstract void publish(String topic, String message);
 
 	/**
 	 * Publish a stream on a specific topic to the MOM server. Each connected device which subscribed to this topic will
 	 * receive this message.
 	 * 
 	 * @param topic
 	 * @param data
 	 */
 	public abstract void publish(String topic, byte[] data);
 
 	/**
 	 * Send a ping request to the MOM server, just to say 'Hey, I'm alive'.
 	 */
 	public abstract void ping();
 
 	/**
 	 * Check if the client is connected to the server.
 	 */
 	public abstract boolean isConnected();
 
 	/**
 	 * Add a MOM listener to the client. This is used to notify and client has been disconnected.
 	 * 
 	 * @param clientListener
 	 * @see MOMClientListener
 	 */
 	public void addClientListener(MOMClientListener clientListener) {
 		clientListeners.add(clientListener);
 	}
 
 	/**
 	 * Remove a MOM listener from the client.
 	 * 
 	 * @param clientListener
 	 */
 	public void removeClientListener(MOMClientListener clientListener) {
 		clientListeners.remove(clientListener);
 	}
 
	public List<MOMClientListener> getClientListeners() {
 		return clientListeners;
 	}
 
 	public Map<String, Set<MOMMethodHandler>> getTopicMethodHandlers() {
 		return topicMethodHandlers;
 	}
 
 	String getHostname() {
 		return hostname;
 	}
 
 	int getPort() {
 		return port;
 	}
 
 }
