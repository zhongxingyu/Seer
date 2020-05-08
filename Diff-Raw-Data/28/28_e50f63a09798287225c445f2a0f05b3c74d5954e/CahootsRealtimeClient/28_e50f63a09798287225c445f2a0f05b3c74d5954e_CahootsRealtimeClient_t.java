 package com.cahoots.connection.websocket;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 
 import com.cahoots.connection.ConnectionDetails;
 import com.cahoots.connection.serialize.Collaborator;
 import com.cahoots.connection.serialize.MessageBase;
 import com.cahoots.connection.serialize.receive.ChatReceiveMessage;
 import com.cahoots.connection.serialize.receive.CollaboratorJoinedMessage;
 import com.cahoots.connection.serialize.receive.CollaboratorLeftMessage;
 import com.cahoots.connection.serialize.receive.CollaboratorsListMessage;
 import com.cahoots.connection.serialize.receive.OpDeleteMessage;
 import com.cahoots.connection.serialize.receive.OpInsertMessage;
 import com.cahoots.connection.serialize.receive.OpReplaceMessage;
 import com.cahoots.connection.serialize.receive.ShareDocumentMessage;
 import com.cahoots.connection.serialize.receive.UserChangeMessage;
 import com.cahoots.connection.serialize.receive.UserListMessage;
 import com.cahoots.eclipse.indigo.misc.TextEditorTools;
 import com.cahoots.event.ChatReceivedEventListener;
 import com.cahoots.event.CollaboratorJoinedEventListener;
 import com.cahoots.event.CollaboratorLeftEventListener;
 import com.cahoots.event.CollaboratorsListEventListener;
 import com.cahoots.event.ConnectEvent;
 import com.cahoots.event.ConnectEventListener;
 import com.cahoots.event.DisconnectEvent;
 import com.cahoots.event.DisconnectEventListener;
 import com.cahoots.event.GenericEventListener;
 import com.cahoots.event.OpDeleteEventListener;
 import com.cahoots.event.OpInsertEventListener;
 import com.cahoots.event.OpReplaceEventListener;
 import com.cahoots.event.ShareDocumentEventListener;
 import com.cahoots.event.UserChangeEventListener;
 import com.cahoots.util.Log;
 import com.google.gson.Gson;
 
 @SuppressWarnings("unchecked")
 public class CahootsRealtimeClient extends CahootsWebSocket {
 
 	static final Log logger = Log.getLogger(CahootsRealtimeClient.class);
 
 	/**
 	 * Due to java's type erasure, it is not possible to make this listener
 	 * collection type safe
 	 */
 	@SuppressWarnings({ "serial", "rawtypes" })
 	private final Map<Class<? extends GenericEventListener>, List> listeners = new HashMap<Class<? extends GenericEventListener>, List>() {
 		{
 			put(ConnectEventListener.class,
 					new ArrayList<ConnectEventListener>());
 			put(DisconnectEventListener.class,
 					new ArrayList<DisconnectEventListener>());
 			put(ShareDocumentEventListener.class,
 					new ArrayList<ShareDocumentEventListener>());
 			put(OpInsertEventListener.class,
 					new ArrayList<OpInsertEventListener>());
 			put(OpReplaceEventListener.class,
 					new ArrayList<OpReplaceEventListener>());
 			put(OpDeleteEventListener.class,
 					new ArrayList<OpDeleteEventListener>());
 			put(UserChangeEventListener.class,
 					new ArrayList<UserChangeEventListener>());
 			put(ChatReceivedEventListener.class,
 					new ArrayList<ChatReceivedEventListener>());
 			put(CollaboratorLeftEventListener.class,
 					new ArrayList<CollaboratorLeftEventListener>());
 			put(CollaboratorJoinedEventListener.class,
 					new ArrayList<CollaboratorJoinedEventListener>());
 			put(CollaboratorsListEventListener.class,
 					new ArrayList<CollaboratorsListEventListener>());
 		}
 	};
 
 	@Inject
 	public CahootsRealtimeClient(final ConnectionDetails connectionDetails,
 			final WebSocketClientFactory factory,
 			final TextEditorTools editorTools) {
 		super(connectionDetails);
 
 		try {
 			factory.setBufferSize(4096);
 			factory.start();
 		} catch (final Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * TODO: Replace with real check
 	 */
 	@SuppressWarnings("deprecation")
 	public boolean isConnected() {
 		return connectionDetails.isAuthenticated();
 	}
 
 	/**
 	 * WHAT THEF ADSJFKLASDCU ASDFJKLASD;FJAKSLDF ASDF ASDLKFJADLS;KFJASDF
 	 * 
 	 * @!#@!$!@#!@#
 	 * @param eventType
 	 * @param eventListenerClazz
 	 * @param eventClazz
 	 * @param base
 	 * @param message
 	 * @param gson
 	 */
 	<K, T> void fireEvents(final String eventType,
 			final Class<K> eventListenerClazz, final Class<T> eventClazz,
 			final MessageBase base, final String message, final Gson gson) {
 
 		if (eventType.equals(base.getType())) {
 			final List<? extends GenericEventListener<T>> listeners = this.listeners
 					.get(eventListenerClazz);
 			final T msg = gson.fromJson(message, eventClazz);
 			for (final GenericEventListener<T> listener : listeners) {
 				listener.onEvent(msg);
 			}
 		}
 	}
 
	@Override
	public void onSuccessfulConnect() {
 		super.onSuccessfulConnect();
 
 		final List<ConnectEventListener> listeners = this.listeners
 				.get(ConnectEventListener.class);
 		for (final ConnectEventListener listener : listeners) {
 			listener.onEvent(new ConnectEvent());
 		}
 	}
 
 	@Override
 	public void onSuccessfulDisconnect() {
 		super.onSuccessfulDisconnect();
 
 		for (final Object listener : listeners
 				.get(DisconnectEventListener.class)) {
 			if (listener instanceof DisconnectEventListener) {
 				final DisconnectEventListener disconnectEventListener = (DisconnectEventListener) listener;
 				try {
 					disconnectEventListener.onEvent(new DisconnectEvent());
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				throw new IllegalStateException(
 						"Non DisconnectEventListener in listeners");
 			}
 		}
 	}
 
 	/**
 	 * WARNING: For Testing purposes only
 	 * 
 	 * RESPONSES ARE NOT TYPE SAFE!! TODO: Add Guice and move this to a mock
 	 * 
 	 * @return
 	 */
 	@Deprecated
 	public <T extends GenericEventListener<? extends Object>, K> K sendAndWaitForResponse(
 			final Object sendMessage, final Class<K> messageClazz,
 			final Class<T> eventClazz) {
 		try {
 			final Object lock = new Object();
 			synchronized (lock) {
 				final List<K> events = new ArrayList<K>();
 				final Thread thread = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						listeners.get(eventClazz).add(
 								new GenericEventListener<K>() {
 									@Override
 									public void onEvent(final K msg) {
 										synchronized (lock) {
 											events.add(msg);
 											lock.notifyAll();
 										}
 									}
 								});
 					}
 				});
 				thread.start();
 				Thread.sleep(32);
 				send(sendMessage);
 				lock.wait();
 
 				if (events.size() > 0) {
 					return events.get(0);
 				} else {
 					throw new RuntimeException("No responses received");
 				}
 			}
 		} catch (final InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void onMessage(final String message) {
 		super.onMessage(message);
 
 		final Gson gson = new Gson();
 		final MessageBase base = gson.fromJson(message, MessageBase.class);
 
 		CahootsRealtimeClient.logger.debug("Message received");
 		CahootsRealtimeClient.logger.debug(message);
 
 		if ("users".equals(base.getService())) {
 			if ("all".equals(base.getType())) {
 				final UserListMessage msg = gson.fromJson(message,
 						UserListMessage.class);
 				for (final Collaborator user : msg.getUsers()) {
 					fireEvents("all", UserChangeEventListener.class,
 							UserChangeMessage.class, base,
 							gson.toJson(new UserChangeMessage(user)), gson);
 				}
 			} else {
 				fireEvents("status", UserChangeEventListener.class,
 						UserChangeMessage.class, base, message, gson);
 			}
 		} else if ("op".equals(base.getService())) {
 			fireEvents("collaborators", CollaboratorsListEventListener.class,
 					CollaboratorsListMessage.class, base, message, gson);
 			fireEvents("joined", CollaboratorJoinedEventListener.class,
 					CollaboratorJoinedMessage.class, base, message, gson);
 			fireEvents("shared", ShareDocumentEventListener.class,
 					ShareDocumentMessage.class, base, message, gson);
 			fireEvents("insert", OpInsertEventListener.class,
 					OpInsertMessage.class, base, message, gson);
 			fireEvents("replace", OpReplaceEventListener.class,
 					OpReplaceMessage.class, base, message, gson);
 			fireEvents("delete", OpDeleteEventListener.class,
 					OpDeleteMessage.class, base, message, gson);
 			fireEvents("left", CollaboratorLeftEventListener.class,
 					CollaboratorLeftMessage.class, base, message, gson);
 		} else if ("chat".equals(base.getService())) {
 			fireEvents("receive", ChatReceivedEventListener.class,
 					ChatReceiveMessage.class, base, message, gson);
 		}
 	}
 
 	// Event Listeners
 	public void addUserLoginEventListener(final UserChangeEventListener listener) {
 		listeners.get(UserChangeEventListener.class).add(listener);
 	}
 
 	public void addDisconnectEventListener(
 			final DisconnectEventListener listener) {
 		listeners.get(DisconnectEventListener.class).add(listener);
 	}
 
 	public void addConnectEventListener(final ConnectEventListener listener) {
 		listeners.get(ConnectEventListener.class).add(listener);
 	}
 
 	public void addOpInsertEventListener(final OpInsertEventListener listener) {
 		listeners.get(OpInsertEventListener.class).add(listener);
 	}
 
 	public void addOpReplaceEventListener(final OpReplaceEventListener listener) {
 		listeners.get(OpReplaceEventListener.class).add(listener);
 	}
 
 	public void addOpDeleteEventListener(final OpDeleteEventListener listener) {
 		listeners.get(OpDeleteEventListener.class).add(listener);
 	}
 
 	public void removeOpInsertEventListener(final OpInsertEventListener listener) {
 		listeners.get(OpInsertEventListener.class).remove(listener);
 	}
 
 	public void removeOpReplaceEventListener(
 			final OpReplaceEventListener listener) {
 		listeners.get(OpReplaceEventListener.class).remove(listener);
 	}
 
 	public void removeOpDeleteEventListener(final OpDeleteEventListener listener) {
 		listeners.get(OpDeleteEventListener.class).remove(listener);
 	}
 
 	public void addShareDocumentEventListener(
 			final ShareDocumentEventListener listener) {
 		listeners.get(ShareDocumentEventListener.class).add(listener);
 	}
 
 	public void addChatReceivedEventListener(
 			final ChatReceivedEventListener listener) {
 		listeners.get(ChatReceivedEventListener.class).add(listener);
 	}
 
 	public void addCollaboratorLeftEventLisener(
 			final CollaboratorLeftEventListener listener) {
 		listeners.get(CollaboratorLeftEventListener.class).add(listener);
 	}
 
 	public void addCollaboratorJoinedEventListener(
 			final CollaboratorJoinedEventListener listener) {
 		listeners.get(CollaboratorJoinedEventListener.class).add(listener);
 	}
 
 	public void addCollaboratorsListEventListener(
 			final CollaboratorsListEventListener listener) {
 		listeners.get(CollaboratorsListEventListener.class).add(listener);
 	}
 }
