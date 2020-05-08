 package eu.neq.mais.request.comet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.cometd.bayeux.Message;
 import org.cometd.bayeux.server.BayeuxServer;
 import org.cometd.bayeux.server.ConfigurableServerChannel;
 import org.cometd.bayeux.server.ServerChannel;
 import org.cometd.bayeux.server.ServerMessage;
 import org.cometd.bayeux.server.ServerSession;
 import org.cometd.java.annotation.Configure;
 import org.cometd.java.annotation.Listener;
 import org.cometd.java.annotation.Service;
 import org.cometd.java.annotation.Session;
 import org.cometd.server.AbstractService;
 import org.cometd.server.authorizer.GrantAuthorizer;
 
 import com.google.gson.Gson;
 
 import eu.neq.mais.domain.ChartCoordinateFactory;
 
 @Service("echoService")
 public final class EchoService {
 
 	@Inject
 	private BayeuxServer server;
 	@Session
 	private ServerSession serverSession;
 
 	private HashMap<String, Thread> request = new HashMap<String, Thread>();
 
 	@PostConstruct
 	void init() {
 		server.addListener(new BayeuxServer.SessionListener() {
 
 			public void sessionRemoved(ServerSession session, boolean arg1) {
 				// System.out.println("SESSION REMOVED");
 			}
 
 			public void sessionAdded(ServerSession session) {
 				// System.out.println("SESSION ADDED");
 			}
 		});
 
 		server.addListener(new BayeuxServer.ChannelListener() {
 
 			public void configureChannel(ConfigurableServerChannel arg0) {
 				
 			}
 
 			public void channelRemoved(String arg0) {
 				
 			}
 
 			public void channelAdded(ServerChannel arg0) {
 
 			}
 		});
 		
 		server.addListener(new BayeuxServer.SubscriptionListener() {
 			
 			public void unsubscribed(ServerSession arg0, ServerChannel arg1) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void subscribed(ServerSession arg0, ServerChannel arg1) {
 				System.out.println("Subscribe: "+arg1.getId());
 			}
 		});
 	}
 
 	@Configure("/**")
 	void any(ConfigurableServerChannel channel) {
 		channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
 	}
 
 	@Configure("/cometd/echo")
 	void echo(ConfigurableServerChannel channel) {
 		channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
 	}
 
 	@Configure("/cometd/pulse")
 	void pulse(ConfigurableServerChannel channel) {
 		channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
 	}
 
 	@Listener("/cometd/echo")
 	void echo(ServerSession remote, ServerMessage.Mutable message) {
 		remote.deliver(serverSession, message.getChannel(), message.getData(),
 				null);
 
 	}
 
 	@Listener("/cometd/pulse/*")
 	void pulse(final ServerSession remote, final ServerMessage.Mutable message) {
 		
 		if (request.containsKey(message.getChannel())) {
 			request.get(message.getChannel()).stop();
 			request.remove(message.getChannel());
 			System.out.println("pulse request on channel:" + message.getChannel()+" | receivers: "+request.size() + " - REMOVED");
 		} else {
 			System.out.println("pulse request on channel:" + message.getChannel()+" | receivers: "+request.size() + " - STARTED");
 			final Thread t = new Thread() {
 				
 				public void run() {
 					ChartCoordinateFactory ccf = new ChartCoordinateFactory();
 					Heartbeat hb = new Heartbeat();
 					int packet_id = 0;
 					
 					System.out.println("run");
 					
 					double x = 0;
 					while (x < 30000) {
 						double y = hb.getNextY();
 						String json = new Gson().toJson(ccf.getChartCoordinate(System.currentTimeMillis(), y));
						//System.out.println(y);
 						remote.deliver(serverSession, message.getChannel(),
 								json, String.valueOf(packet_id++));
 						x += 0.1;
 						try {
 							sleep(100);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 
 				}
 			};
 
 			request.put(message.getChannel(), t);
 
 			t.start();
 
 			//remote.disconnect();
 		}
 
 	}
 
 }
