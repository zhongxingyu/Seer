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
 package com.excilys.spring.mom.client.impl;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.excilys.soja.client.StompClient;
 import com.excilys.soja.client.events.StompClientListener;
 import com.excilys.spring.mom.client.MOMClient;
import com.excilys.spring.mom.client.MOMClientListener;
 import com.excilys.spring.mom.client.MOMMethodHandler;
 
 /**
  * A concrete sub-class of {@link MOMClient MOMClient} for the <a
  * href="http://stomp.github.com/stomp-specification-1.1.html">STOMP protocol</a>.
  * 
  * @author dvilleneuve
  * @see MOMClient
  */
 public class MOMClientSoja extends MOMClient {
 
 	private final String username;
 	private final String password;
 	private final StompClient client;
 	private final HashMap<String, Long> subscriptionIds;
 	private boolean isConnected = false;
 
 	/**
 	 * Create an instance of MOMClientStomp for a specific {@code hostname} and {@code port}, which will auto-connect to
 	 * the server when an instance is created. {@code username} and {@code password} are used by STOMP server to
 	 * authenticate each client.
 	 * 
 	 * @param hostname
 	 * @param port
 	 * @param username
 	 * @param password
 	 * @see MOMClient
 	 */
 	public MOMClientSoja(String hostname, int port, String username, String password) {
 		this(hostname, port, username, password, true);
 	}
 
 	/**
 	 * Create an instance of MOMClientMqtt for a specific {@code hostname} and {@code port}. If {@code autoconnect}
 	 * parameter is true, the instance will auto-connect when it's created. {@code username} and {@code password} are
 	 * used by STOMP server to authenticate each client.
 	 * 
 	 * @param hostname
 	 * @param port
 	 * @param username
 	 * @param password
 	 * @param autoconnect
 	 * @see MOMClient
 	 */
 	public MOMClientSoja(String hostname, int port, String username, String password, boolean autoconnect) {
 		super(hostname, port, autoconnect);
 		this.username = username;
 		this.password = password;
 		this.client = new StompClient(hostname, port);
 		this.client.addListener(new StompClientInternalListener());
 		this.subscriptionIds = new HashMap<String, Long>();
 
 		if (autoconnect) {
 			connect();
 		}
 	}
 
 	@Override
 	public void connect() {
		LOGGER.info("Connecting to as {}...", username);
 		try {
 			client.connect(username, password);
 		} catch (Exception e) {
			LOGGER.error("Failed to connect to Stomp server as {} ({})", new String[] { username, e.getMessage() });

			for (MOMClientListener clientListener : getClientListeners()) {
				clientListener.connectionFailed();
			}
 		}
 	}
 
 	@Override
 	public void disconnect() {
 		if (!isConnected())
 			return;
 
 		LOGGER.info("Disconnect...");
 		client.disconnect();
 	}
 
 	@Override
 	public void subscribe(String topic, MOMMethodHandler momMethodHandler) {
 		if (!isConnected())
 			return;
 
 		super.subscribe(topic, momMethodHandler);
 		Long subscriptionId = client.subscribe(topic);
 		subscriptionIds.put(topic, subscriptionId);
 	}
 
 	@Override
 	public void unsubscribe(String topic) {
 		if (!isConnected())
 			return;
 
 		super.unsubscribe(topic);
 		Long subscriptionId = subscriptionIds.get(topic);
 		client.unsubscribe(subscriptionId);
 	}
 
 	/**
 	 * Publish a new message on a specific topic to the STOMP server. Each connected device which subscribed to this
 	 * topic will receive this message.
 	 * 
 	 * @param topic
 	 * @param message
 	 */
 	@Override
 	public void publish(String topic, String message) {
 		if (!isConnected())
 			return;
 
 		LOGGER.debug("Send to {} : {}...", topic, message);
 		client.send(topic, message);
 	}
 
 	@Override
 	public void publish(String topic, byte[] data) {
 		if (!isConnected())
 			return;
 
 		LOGGER.debug("Send {} bytes to {}...", topic, data.length);
 		client.send(topic, new String(data));
 	}
 
 	/**
 	 * Send a ping request to the STOMP server just to say 'Hey, I'm alive'.
 	 */
 	@Override
 	public void ping() {
 	}
 
 	@Override
 	public boolean isConnected() {
 		return isConnected;
 	}
 
 	/**
 	 * Handle the STOMP events and dispatch them to the messaging listener according to the concerned topic.
 	 * 
 	 * @author dvilleneuve
 	 * 
 	 */
 	private final class StompClientInternalListener implements StompClientListener {
 
 		@Override
 		public void connected() {
 			isConnected = true;
 			LOGGER.info("Connected.");

			for (MOMClientListener clientListener : getClientListeners()) {
				clientListener.connected();
			}
 		}
 
 		@Override
 		public void disconnected() {
 			isConnected = false;

			for (MOMClientListener clientListener : getClientListeners()) {
				clientListener.disconnected();
			}
 		}
 
 		@Override
 		public void receivedMessage(String topic, String message, Map<String, String> userHeaders) {
 			Set<MOMMethodHandler> methodHandlers = getTopicMethodHandlers().get(topic);
 
 			for (MOMMethodHandler methodHandler : methodHandlers) {
 				if (methodHandler != null && methodHandler.getMethod() != null && methodHandler.getInstance() != null) {
 					try {
 						methodHandler.invoke(message.getBytes());
 					} catch (Exception e) {
 						LOGGER.error("Can't invoke method", e);
 					}
 				}
 			}
 		}
 
 		@Override
 		public void receivedError(String shortMessage, String description) {
 			LOGGER.warn("Received a STOMP error : '{}'. Full description : {}", shortMessage, description);
 		}
 	}
 
 }
