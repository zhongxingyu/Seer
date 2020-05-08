 package mobisocial.cocoon.server;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import javapns.Push;
 import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
 import javapns.notification.PushNotificationPayload;
 import javapns.notification.transmission.PushQueue;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import mobisocial.cocoon.model.Listener;
 import mobisocial.cocoon.util.Database;
 import net.vz.mongodb.jackson.JacksonDBCollection;
 
 import org.apache.commons.codec.binary.Base64;
 import org.json.JSONException;
 
 import com.mongodb.DBCollection;
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.AMQP.Queue.DeclareOk;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.DefaultConsumer;
 import com.rabbitmq.client.Envelope;
 import com.sun.jersey.spi.resource.Singleton;
 
 @Singleton
 @Path("/api/0/")
 public class AMQPush {
 	
 	HashMap<String, String> mQueues = new HashMap<String, String>();
 	HashMap<String, String> mConsumers = new HashMap<String, String>();
 	HashMap<String, HashSet<String>> mNotifiers = new HashMap<String, HashSet<String>>();
 	HashMap<String, Listener> mListeners = new HashMap<String, Listener>();
 	LinkedBlockingDeque<Runnable> mJobs = new LinkedBlockingDeque<Runnable>();
 	String encodeAMQPname(String prefix, byte[] key) {
 		return prefix + Base64.encodeBase64(key, false, true) + "\n";
 	}
     byte[] decodeAMQPname(String prefix, String name) {
     	if(!name.startsWith(prefix))
     		return null;
     	//URL-safe? automatically, no param necessary?
     	return Base64.decodeBase64(name.substring(prefix.length()));
 	}
     
     AMQPushThread mPushThread = new AMQPushThread();
     
     class AMQPushThread extends Thread{
     	Channel mIncomingChannel;
 		private DefaultConsumer mConsumer;
 		@Override
 		public void run() {
         	try {
 				amqp();
 			} catch (Throwable e) {
 				throw new RuntimeException(e);
 			}
 		}
 		void amqp() throws Throwable {
 	        PushQueue appleQueue = null;
 			appleQueue = Push.queue("push.p12", "pusubi", false, 2);
 			appleQueue.start();
 			final PushQueue queue = appleQueue;
 
 	        
 	        ConnectionFactory connectionFactory = new ConnectionFactory();
 			connectionFactory.setHost("bumblebee.musubi.us");
 			connectionFactory.setConnectionTimeout(30 * 1000);
 			connectionFactory.setRequestedHeartbeat(30);
 			Connection connection = connectionFactory.newConnection();
 			mIncomingChannel = connection.createChannel();
 			
 			mConsumer = new DefaultConsumer(mIncomingChannel) {
 				@Override
 				public void handleDelivery(final String consumerTag, final Envelope envelope,
 						final BasicProperties properties, final byte[] body) throws IOException 
 				{
 					HashSet<String> threadDevices = new HashSet<String>();
 					synchronized (mNotifiers) {
 						String identity = mConsumers.get(consumerTag);
 						if(identity == null)
 							return;
 						HashSet<String> devices = mNotifiers.get(identity);
 						if(devices == null)
 							return;
 						threadDevices.addAll(devices);
 					}
					PushNotificationPayload payload = PushNotificationPayload.complex();
				    try {
						payload.addAlert("Musubis!!!!");
					    for(String device : threadDevices) {
							try {
								queue.add(payload, device);
							} catch (InvalidDeviceTokenFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
 						}
					} catch (JSONException e1) {
						//throw new RuntimeException("failed to build alert", e1);
 					}
 				}
 			};
 			
 			System.out.println("doing registrations");
 			Set<String> notifiers = new HashSet<String>();
 			synchronized(mNotifiers) {
 				notifiers.addAll(mNotifiers.keySet());
 			}
 			for(String identity : notifiers) {
 				DeclareOk x = mIncomingChannel.queueDeclare();
 				System.out.println("listening " + identity);
 				mIncomingChannel.exchangeDeclare(identity, "fanout", true);
 				mIncomingChannel.queueBind(x.getQueue(), identity, "");
 				String consumerTag = mIncomingChannel.basicConsume(x.getQueue(), true, mConsumer);
 				synchronized(mNotifiers) {
 					mQueues.put(identity, x.getQueue());
 					mConsumers.put(consumerTag, identity);
 				}
 			}
 			System.out.println("done registrations");
 			
 			for(;;) {
 				Runnable job = mJobs.poll();
 				if(job == null)
 					continue;
 				job.run();
 			}
 		}
 	};
     
 	public AMQPush() {
 		loadAll();
 		mPushThread.start();
 	}
 	
     private void loadAll() {
         DBCollection rawCol = Database.dbInstance().getCollection(Listener.COLLECTION);
         JacksonDBCollection<Listener, String> col = JacksonDBCollection.wrap(rawCol,
         		Listener.class, String.class);
 
         for(Listener l : col.find()) {
         	mListeners.put(l.deviceToken, l);
 
         	//add all registrations
         	for(String ident : l.identityExchanges) {
         		HashSet<String> listeners = mNotifiers.get(ident);
         		if(listeners == null) {
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
         synchronized(mNotifiers) {
         	Listener existing = mListeners.get(l.deviceToken);
         	if(existing != null && existing.identityExchanges.size() == l.identityExchanges.size()) { 
         		needs_update = false;
         		Iterator<String> a = existing.identityExchanges.iterator();
         		Iterator<String> b = l.identityExchanges.iterator();
         		while(a.hasNext()) {
         			String aa = a.next();
         			String bb = b.next();
         			if(!aa.equals(bb)) {
         				needs_update = true;
         				break;
         			}
         		}
         	}
         	if(!needs_update)
         		return "ok";
         	
         	mListeners.put(l.deviceToken, l);
         	
         	//TODO: set intersection to not wasteful tear up and down
 
         	if(existing != null) {
 	        	//remove all old registrations
 	        	for(String ident : existing.identityExchanges) {
 	        		HashSet<String> listeners = mNotifiers.get(ident);
 	        		assert(listeners != null);
 	        		listeners.remove(l.deviceToken);
 	        		if(listeners.size() == 0) {
 	        			amqpUnregister(ident);
 	        			mNotifiers.remove(ident);
 	        		}
 	        	}
         	}
         	
         	//add all new registrations
         	for(String ident : l.identityExchanges) {
         		HashSet<String> listeners = mNotifiers.get(ident);
         		if(listeners == null) {
         			listeners = new HashSet<String>();
         			mNotifiers.put(ident, listeners);
         			amqpRegister(ident);
         		}
         		listeners.add(l.deviceToken);
         	}
         }
         DBCollection rawCol = Database.dbInstance().getCollection(Listener.COLLECTION);
         JacksonDBCollection<Listener, String> col = JacksonDBCollection.wrap(rawCol,
         		Listener.class, String.class);
         Listener match = new Listener();
         match.deviceToken = l.deviceToken;
         col.update(match, l, true, false);
         return "ok";
     }
 	void amqpRegister(final String identity) {
 		mJobs.push(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					DeclareOk x = mPushThread.mIncomingChannel.queueDeclare();
 					System.out.println("listening " + identity);
 					mPushThread.mIncomingChannel.exchangeDeclare(identity, "fanout", true);
 					mPushThread.mIncomingChannel.queueBind(x.getQueue(), identity, "");
 					String consumerTag = mPushThread.mIncomingChannel.basicConsume(x.getQueue(), true, mPushThread.mConsumer);
 					synchronized(mNotifiers) {
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
 		mJobs.push(new Runnable() {
 			@Override
 			public void run() {
 				String queue = null;
 				synchronized(mNotifiers) {
 					queue = mQueues.get(identity);
 					//probably an error
 					if(queue == null)
 						return;
 					mQueues.remove(identity);
 					//TODO: update consumers
 				}
 				System.out.println("stop listening " + identity);
 				try {
 					mPushThread.mIncomingChannel.queueUnbind(queue, identity, "");
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
         synchronized(mNotifiers) {
         	Listener existing = mListeners.get(deviceToken);
         	if(existing == null)
         		return "ok";
         	
         	mListeners.remove(deviceToken);
 
         	//remove all old registrations
         	for(String ident : existing.identityExchanges) {
         		HashSet<String> listeners = mNotifiers.get(ident);
         		assert(listeners != null);
         		listeners.remove(deviceToken);
         		if(listeners.size() == 0) {
         			amqpUnregister(ident);
         			mNotifiers.remove(ident);
         		}
         	}
         }
         DBCollection rawCol = Database.dbInstance().getCollection(Listener.COLLECTION);
         JacksonDBCollection<Listener, String> col = JacksonDBCollection.wrap(rawCol,
         		Listener.class, String.class);
         Listener match = new Listener();
         match.deviceToken = deviceToken;
         col.remove(match);
         return "ok";
     }
 }
