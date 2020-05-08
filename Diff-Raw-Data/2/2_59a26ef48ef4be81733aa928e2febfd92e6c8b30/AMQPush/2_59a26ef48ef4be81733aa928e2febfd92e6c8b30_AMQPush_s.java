 package mobisocial.cocoon.server;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import javapns.Push;
 import javapns.devices.Device;
 import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
 import javapns.notification.PushNotificationPayload;
 import javapns.notification.PushedNotification;
 import javapns.notification.PushedNotifications;
 import javapns.notification.ResponsePacket;
 import javapns.notification.transmission.PushQueue;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import mobisocial.cocoon.model.Listener;
 import mobisocial.cocoon.util.Database;
 import mobisocial.crypto.CorruptIdentity;
 import mobisocial.crypto.IBHashedIdentity;
 import mobisocial.musubi.protocol.Message;
 import net.vz.mongodb.jackson.JacksonDBCollection;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang3.tuple.Pair;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.json.JSONException;
 
 import com.google.android.gcm.server.Constants;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 import com.mongodb.DBCollection;
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.AMQP.Queue.DeclareOk;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.DefaultConsumer;
 import com.rabbitmq.client.Envelope;
 import com.sun.jersey.spi.resource.Singleton;
 
 import de.undercouch.bson4jackson.BsonFactory;
 import de.undercouch.bson4jackson.BsonParser.Feature;
 
 @Singleton
 @Path("/api/0/")
 public class AMQPush {
 
 	static class BadgeData {
 		public int amqp;
 		public int local;
 		public Date last;
 	}
 
 	ObjectMapper mMapper = new ObjectMapper(
 			new BsonFactory().enable(Feature.HONOR_DOCUMENT_LENGTH));
 	String mGoogleKey;
 
 	HashMap<String, BadgeData> mCounts = new HashMap<String, BadgeData>();
 	HashMap<String, String> mQueues = new HashMap<String, String>();
 	HashMap<String, String> mConsumers = new HashMap<String, String>();
 	HashMap<String, HashSet<String>> mNotifiers = new HashMap<String, HashSet<String>>();
 	HashMap<String, Listener> mListeners = new HashMap<String, Listener>();
 	LinkedBlockingDeque<Runnable> mJobs = new LinkedBlockingDeque<Runnable>();
 
 	String encodeAMQPname(String prefix, byte[] key) {
 		// TODO: WTF doesnt this put the = at the end automatically?
 		int excess = (key.length * 8 % 6);
 		String pad = "";
 		int equals = (6 - excess) / 2;
 		for (int i = 0; i < equals; ++i)
 			pad += "=";
 		return prefix + Base64.encodeBase64URLSafeString(key) + pad + "\n";
 	}
 
 	byte[] decodeAMQPname(String prefix, String name) {
 		if (!name.startsWith(prefix))
 			return null;
 		// URL-safe? automatically, no param necessary?
 		return Base64.decodeBase64(name.substring(prefix.length()));
 	}
 
 	private LinkedBlockingQueue<Pair<com.google.android.gcm.server.Message, String>> mGooglePushes = new LinkedBlockingQueue<Pair<com.google.android.gcm.server.Message, String>>();
 
 	class GooglePushThread extends Thread {
 		Sender mSender;
 
 		public void run() {
 			for (;;) {
 				try {
 					mSender = new Sender(mGoogleKey);
 					push();
 				} catch (Throwable e) {
 					throw new RuntimeException(e);
 				}
 				try {
 					Thread.sleep(30000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 		void push() throws IOException {
 			Pair<com.google.android.gcm.server.Message, String> msg = mGooglePushes
 					.poll();
 			String device_token = msg.getRight();
 			String push_token = device_token;
 			synchronized (mNotifiers) {
 				Listener existing = mListeners.get(device_token);
 				if (existing.canonicalToken != null)
 					push_token = existing.canonicalToken;
 			}
 			Result result = mSender.sendNoRetry(msg.getLeft(), push_token);
 			if (result.getMessageId() != null) {
 				// same device has more than on registration ID: update database
 				String canonicalRegId = result.getCanonicalRegistrationId();
 				if (canonicalRegId != null) {
 					synchronized (mNotifiers) {
 						Listener existing = mListeners.get(device_token);
 						existing.canonicalToken = canonicalRegId;
 						DBCollection rawCol = Database.dbInstance()
 								.getCollection(Listener.COLLECTION);
 						JacksonDBCollection<Listener, String> col = JacksonDBCollection
 								.wrap(rawCol, Listener.class, String.class);
 						Listener match = new Listener();
 						match.deviceToken = existing.deviceToken;
 						col.update(match, existing, true, false);
 					}
 				}
 			} else {
 				String error = result.getErrorCodeName();
 				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
 					// application has been removed from device - unregister
 					// database
 					System.err.println("droid unregistering invalid token "
 							+ device_token);
 					unregister(device_token);
 				}
 			}
 		}
 	}
 
 	AMQPushThread mPushThread = new AMQPushThread();
 
 	class AMQPushThread extends Thread {
 		Channel mIncomingChannel;
 		private DefaultConsumer mConsumer;
 
 		@Override
 		public void run() {
 			for (;;) {
 				try {
 					amqp();
 				} catch (Throwable e) {
 					throw new RuntimeException(e);
 				}
 				try {
 					Thread.sleep(30000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 		void amqp() throws Throwable {
 			final PushQueue dev_queue = Push.queue("push.p12", "pusubi", false,
 					1);
 			dev_queue.start();
 			final PushQueue prod_queue = Push.queue("pushprod.p12", "pusubi",
 					true, 1);
 			prod_queue.start();
 
 			for (;;) {
 
 				ConnectionFactory connectionFactory = new ConnectionFactory();
 				connectionFactory.setHost("bumblebee.musubi.us");
 				connectionFactory.setConnectionTimeout(30 * 1000);
 				connectionFactory.setRequestedHeartbeat(30);
 				Connection connection = connectionFactory.newConnection();
 				mIncomingChannel = connection.createChannel();
 
 				mConsumer = new DefaultConsumer(mIncomingChannel) {
 					@Override
 					public void handleDelivery(final String consumerTag,
 							final Envelope envelope,
 							final BasicProperties properties, final byte[] body)
 							throws IOException {
 						HashSet<String> threadDevices = new HashSet<String>();
 						synchronized (mNotifiers) {
 							String identity = mConsumers.get(consumerTag);
 							if (identity == null)
 								return;
 							HashSet<String> devices = mNotifiers.get(identity);
 							if (devices == null)
 								return;
 							threadDevices.addAll(devices);
 						}
 						Message m = null;
 						try {
 							m = mMapper.readValue(body, Message.class);
 						} catch (IOException e) {
 							new RuntimeException(
 									"Failed to parse BSON of outer message", e)
 									.printStackTrace();
 							return;
 						}
 						// don't notify for blind (profile/delete/like msgs)
 						if (m.l)
 							return;
 						String sender_exchange;
 						try {
 							sender_exchange = encodeAMQPname("ibeidentity-",
 									new IBHashedIdentity(m.s.i).at(0).identity_);
 						} catch (CorruptIdentity e) {
 							e.printStackTrace();
 							return;
 						}
 						Date now = new Date();
 						for (String device : threadDevices) {
 							Listener l;
 							synchronized (mNotifiers) {
 								l = mListeners.get(device);
 							}
 							// no self notify
 							if (l.identityExchanges.contains(sender_exchange))
 								continue;
 							int new_value = 0;
 							int amqp = 0;
 							int local = 0;
 							Date last;
 							synchronized (mCounts) {
 								BadgeData bd = mCounts.get(device);
 								if (bd == null) {
 									bd = new BadgeData();
 									mCounts.put(device, bd);
 								}
 								bd.amqp++;
 								amqp = bd.amqp;
 								local = bd.local;
 								new_value = bd.amqp + bd.local;
 								last = bd.last;
 								if (bd.last == null) {
 									bd.last = now;
 								} else if (bd.last != null
 										&& now.getTime() - bd.last.getTime() > 3 * 60 * 1000) {
 									bd.last = now;
 									last = null;
 								}
 							}
							if (l.platform.equals("android")) {
 								com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
 										.addData("amqp", Integer.toString(amqp))
 										.build();
 								mGooglePushes.add(Pair.of(message, device));
 							} else {
 								try {
 									boolean production = l.production != null
 											&& l.production != false;
 									PushNotificationPayload payload = PushNotificationPayload
 											.complex();
 									try {
 										if (last == null) {
 											payload.addAlert("New message");
 											payload.addSound("default");
 										}
 										payload.addBadge(new_value);
 										payload.addCustomDictionary("local",
 												local);
 										payload.addCustomDictionary("amqp",
 												amqp);
 									} catch (JSONException e) {
 										// logic error, not runtime
 										e.printStackTrace();
 										System.exit(1);
 									}
 									if (!production)
 										dev_queue.add(payload, device);
 									else
 										prod_queue.add(payload, device);
 								} catch (InvalidDeviceTokenFormatException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
 						}
 					}
 				};
 
 				System.out.println("doing registrations");
 				Set<String> notifiers = new HashSet<String>();
 				synchronized (mNotifiers) {
 					notifiers.addAll(mNotifiers.keySet());
 				}
 				for (String identity : notifiers) {
 					DeclareOk x = mIncomingChannel.queueDeclare();
 					System.out.println("listening " + identity);
 					mIncomingChannel.exchangeDeclare(identity, "fanout", true);
 					mIncomingChannel.queueBind(x.getQueue(), identity, "");
 					String consumerTag = mIncomingChannel.basicConsume(
 							x.getQueue(), true, mConsumer);
 					synchronized (mNotifiers) {
 						mQueues.put(identity, x.getQueue());
 						mConsumers.put(consumerTag, identity);
 					}
 				}
 				System.out.println("done registrations");
 
 				// TODO: don't do all the feedback stuff on one thread
 				long last = new Date().getTime();
 				for (;;) {
 					Runnable job = mJobs.poll(60, TimeUnit.SECONDS);
 					long current = new Date().getTime();
 					if (current - last > 60 * 1000) {
 						PushedNotifications ps = dev_queue
 								.getPushedNotifications();
 						for (PushedNotification p : ps) {
 							if (p.isSuccessful())
 								continue;
 							String invalidToken = p.getDevice().getToken();
 							System.err.println("unregistering invalid token "
 									+ invalidToken);
 							unregister(invalidToken);
 
 							/* Find out more about what the problem was */
 							Exception theProblem = p.getException();
 							theProblem.printStackTrace();
 
 							/*
 							 * If the problem was an error-response packet
 							 * returned by Apple, get it
 							 */
 							ResponsePacket theErrorResponse = p.getResponse();
 							if (theErrorResponse != null) {
 								System.out.println(theErrorResponse
 										.getMessage());
 							}
 						}
 						last = new Date().getTime();
 
 						List<Device> inactiveDevices = Push.feedback(
 								"push.p12", "pusubi", false);
 						for (Device d : inactiveDevices) {
 							String invalidToken = d.getToken();
 							System.err
 									.println("unregistering feedback failed token token "
 											+ invalidToken);
 							unregister(invalidToken);
 						}
 					}
 					if (job == null)
 						continue;
 					job.run();
 				}
 			}
 		}
 	};
 
 	public AMQPush() throws IOException {
 		Properties props = new Properties();
 		props.load(new FileInputStream("google.properties"));
 		mGoogleKey = props.getProperty("server.key");
 		loadAll();
 		mPushThread.start();
 	}
 
 	private void loadAll() {
 		DBCollection rawCol = Database.dbInstance().getCollection(
 				Listener.COLLECTION);
 		JacksonDBCollection<Listener, String> col = JacksonDBCollection.wrap(
 				rawCol, Listener.class, String.class);
 
 		for (Listener l : col.find()) {
 			mListeners.put(l.deviceToken, l);
 
 			// add all registrations
 			for (String ident : l.identityExchanges) {
 				HashSet<String> listeners = mNotifiers.get(ident);
 				if (listeners == null) {
 					listeners = new HashSet<String>();
 					mNotifiers.put(ident, listeners);
 				}
 				listeners.add(l.deviceToken);
 			}
 		}
 	}
 
 	@POST
 	@Path("register")
 	@Produces("application/json")
 	public String register(Listener l) throws IOException {
 		boolean needs_update = true;
 		synchronized (mNotifiers) {
 			System.out.println(new Date() + "Registering device: "
 					+ l.deviceToken + " for identities "
 					+ Arrays.toString(l.identityExchanges.toArray()));
 
 			// clear pending message count on registration (e.g. amqp connected
 			// to drain messages)
 			// TODO: this is not really right if the client fails to download
 			// all messages
 			// before disconnecting
 			synchronized (mCounts) {
 				BadgeData bd = mCounts.get(l.deviceToken);
 				if (bd == null) {
 					bd = new BadgeData();
 					mCounts.put(l.deviceToken, bd);
 				}
 				if (l.localUnread != null)
 					bd.local = l.localUnread;
 			}
 			Listener existing = mListeners.get(l.deviceToken);
 			if (existing != null
 					&& existing.production == l.production
 					&& existing.identityExchanges.size() == l.identityExchanges
 							.size()) {
 				needs_update = false;
 				Iterator<String> a = existing.identityExchanges.iterator();
 				Iterator<String> b = l.identityExchanges.iterator();
 				while (a.hasNext()) {
 					String aa = a.next();
 					String bb = b.next();
 					if (!aa.equals(bb)) {
 						needs_update = true;
 						break;
 					}
 				}
 			}
 			if (!needs_update)
 				return "ok";
 
 			mListeners.put(l.deviceToken, l);
 
 			// TODO: set intersection to not wasteful tear up and down
 
 			if (existing != null) {
 				// remove all old registrations
 				for (String ident : existing.identityExchanges) {
 					HashSet<String> listeners = mNotifiers.get(ident);
 					assert (listeners != null);
 					listeners.remove(l.deviceToken);
 					if (listeners.size() == 0) {
 						amqpUnregister(ident);
 						mNotifiers.remove(ident);
 					}
 				}
 			}
 
 			// add all new registrations
 			for (String ident : l.identityExchanges) {
 				HashSet<String> listeners = mNotifiers.get(ident);
 				if (listeners == null) {
 					listeners = new HashSet<String>();
 					mNotifiers.put(ident, listeners);
 					amqpRegister(ident);
 				}
 				listeners.add(l.deviceToken);
 			}
 		}
 		DBCollection rawCol = Database.dbInstance().getCollection(
 				Listener.COLLECTION);
 		JacksonDBCollection<Listener, String> col = JacksonDBCollection.wrap(
 				rawCol, Listener.class, String.class);
 		Listener match = new Listener();
 		match.deviceToken = l.deviceToken;
 		col.update(match, l, true, false);
 		return "ok";
 	}
 
 	@POST
 	@Path("clearunread")
 	@Produces("application/json")
 	public String clearUnread(String deviceToken) throws IOException {
 		System.out.println(new Date() + "Clear unread " + deviceToken);
 		synchronized (mCounts) {
 			BadgeData bd = mCounts.get(deviceToken);
 			if (bd == null) {
 				bd = new BadgeData();
 				mCounts.put(deviceToken, bd);
 			}
 			bd.amqp = 0;
 		}
 		return "ok";
 	}
 
 	public static class ResetUnread {
 		public String deviceToken;
 		public int count;
 		public Boolean background;
 	}
 
 	@POST
 	@Path("resetunread")
 	@Produces("application/json")
 	public String resetUnread(ResetUnread ru) throws IOException {
 		System.out.println(new Date() + "reset unread " + ru.deviceToken
 				+ " to " + ru.count);
 		synchronized (mCounts) {
 			BadgeData bd = mCounts.get(ru.deviceToken);
 			if (bd == null) {
 				bd = new BadgeData();
 				mCounts.put(ru.deviceToken, bd);
 			}
 			if (ru.background == null || !ru.background) {
 				bd.last = null;
 			}
 			bd.local = ru.count;
 		}
 		return "ok";
 	}
 
 	void amqpRegister(final String identity) {
 		mJobs.add(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					DeclareOk x = mPushThread.mIncomingChannel.queueDeclare();
 					System.out.println("listening " + identity);
 					mPushThread.mIncomingChannel.exchangeDeclare(identity,
 							"fanout", true);
 					mPushThread.mIncomingChannel.queueBind(x.getQueue(),
 							identity, "");
 					String consumerTag = mPushThread.mIncomingChannel
 							.basicConsume(x.getQueue(), true,
 									mPushThread.mConsumer);
 					synchronized (mNotifiers) {
 						mQueues.put(identity, x.getQueue());
 						mConsumers.put(consumerTag, identity);
 					}
 				} catch (Throwable t) {
 					throw new RuntimeException("failed to register", t);
 				}
 			}
 		});
 	}
 
 	void amqpUnregister(final String identity) {
 		mJobs.add(new Runnable() {
 			@Override
 			public void run() {
 				String queue = null;
 				synchronized (mNotifiers) {
 					queue = mQueues.get(identity);
 					// probably an error
 					if (queue == null)
 						return;
 					mQueues.remove(identity);
 					// TODO: update consumers
 				}
 				System.out.println("stop listening " + identity);
 				try {
 					mPushThread.mIncomingChannel.queueUnbind(queue, identity,
 							"");
 				} catch (Throwable t) {
 					throw new RuntimeException("removing queue dynamically", t);
 				}
 			}
 		});
 	}
 
 	@POST
 	@Path("unregister")
 	@Produces("application/json")
 	public String unregister(String deviceToken) throws IOException {
 		synchronized (mNotifiers) {
 			Listener existing = mListeners.get(deviceToken);
 			if (existing == null)
 				return "ok";
 
 			mListeners.remove(deviceToken);
 			synchronized (mCounts) {
 				mListeners.remove(deviceToken);
 			}
 
 			// remove all old registrations
 			for (String ident : existing.identityExchanges) {
 				HashSet<String> listeners = mNotifiers.get(ident);
 				assert (listeners != null);
 				listeners.remove(deviceToken);
 				if (listeners.size() == 0) {
 					amqpUnregister(ident);
 					mNotifiers.remove(ident);
 				}
 			}
 		}
 		DBCollection rawCol = Database.dbInstance().getCollection(
 				Listener.COLLECTION);
 		JacksonDBCollection<Listener, String> col = JacksonDBCollection.wrap(
 				rawCol, Listener.class, String.class);
 		Listener match = new Listener();
 		match.deviceToken = deviceToken;
 		col.remove(match);
 		return "ok";
 	}
 }
