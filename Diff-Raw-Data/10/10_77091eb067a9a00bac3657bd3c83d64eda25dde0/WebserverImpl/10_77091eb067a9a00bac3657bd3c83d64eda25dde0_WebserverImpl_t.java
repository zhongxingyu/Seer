 package at.fhv.popya.application.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriInfo;
 
 import at.fhv.popya.application.service.UserException;
 import at.fhv.popya.application.service.helper.LocationHelper;
 import at.fhv.popya.application.transfer.LocationTO;
 import at.fhv.popya.application.transfer.MessageTO;
 import at.fhv.popya.application.transfer.MessagesTO;
 import at.fhv.popya.application.transfer.UserTO;
 import at.fhv.popya.application.ws.IWebserver;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.sun.jersey.spi.resource.Singleton;
 
 @Singleton
 @Path("/popya")
 public class WebserverImpl implements IWebserver {
 	private static final int CLEAN_INTERVAL_MINUTES = 10;
 	private static ConcurrentMap<UserTO, List<MessageTO<Object>>> _messages;
 	private final UriInfo _uriInfo;
 
 	static {
 		// list with all users and their received messages
 		Cache<UserTO, List<MessageTO<Object>>> cache = CacheBuilder
 				.newBuilder().concurrencyLevel(20)
 				.expireAfterAccess(CLEAN_INTERVAL_MINUTES, TimeUnit.MINUTES)
 				.build();
 		_messages = cache.asMap();
 	}
 
 	public WebserverImpl(@Context UriInfo info) {
 		_uriInfo = info;
 	}
 
 	@POST
 	@Path("/connect")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Override
 	public void connect(UserTO user) {
 		if (user != null) {
 			// register user
 			if (!_messages.containsKey(user)) {
 				List<MessageTO<Object>> messageList = new ArrayList<MessageTO<Object>>();
 				_messages.put(user, messageList);
 			} else {
 				throw new UserException(_uriInfo.getBaseUriBuilder()
 						.path("/connect").build(), "User name already in use.");
 			}
 		} else {
 			throw new UserException(_uriInfo.getBaseUriBuilder()
 					.path("/connect").build(), "User may not be null.");
 		}
 	}
 
 	@POST
 	@Path("/getMessages")
 	@Produces({ MediaType.APPLICATION_JSON })
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Override
 	public MessagesTO<Object> getMessages(UserTO user) {
 		if (user != null) {
 			if (!_messages.containsKey(user)) {
 				throw new UserException(_uriInfo.getBaseUriBuilder()
 						.path("/getMessages").build(), "User is not connected.");
 			}
 
 			MessagesTO<Object> out = new MessagesTO<Object>();
 			out.setMessages(new ArrayList<MessageTO<Object>>(_messages
 					.get(user)));
 
 			// reset the message list
 			_messages.get(user).clear();
 
 			// update user location
 			updateUserLocation(user);
 
 			return out;
 		}
 		throw new UserException(_uriInfo.getBaseUriBuilder()
 				.path("/getMessages").build(), "User may not be null.");
 	}
 
 	@POST
 	@Path("/sendMessage")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Override
 	public void sendMessage(MessageTO<Object> message) {
 		if (message != null) {
 
 			// update users location
 			updateUserLocation(message.getUser());
 
 			for (UserTO receiver : _messages.keySet()) {
 
 				// check if the partners can communicate
 				// if the partner and the receiver are the same person also add
 				// the message
 				if (message.getUser().equals(receiver)
 						|| canCommunicate(receiver, message.getUser())) {
 
 					// special handling for generic message type
					String txtMessage = "";
					if (message.getMessage() instanceof com.sun.org.apache.xerces.internal.dom.ElementNSImpl) {
						txtMessage = ((com.sun.org.apache.xerces.internal.dom.ElementNSImpl) message
								.getMessage()).getFirstChild().getTextContent();
					} else {
						txtMessage = message.getMessage().toString();
					}

 					message.setMessage(txtMessage);
 
 					_messages.get(receiver).add(message);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Update the user location for a specific user
 	 * 
 	 * @param user
 	 *            The user
 	 */
 	private void updateUserLocation(UserTO user) {
 		if (_messages.containsKey(user)) {
 			// find the right user
 			for (UserTO key : _messages.keySet()) {
 
 				// update the user
 				if (key.equals(user)) {
 					if (user.getCurrentLocation() != null) {
 						LocationTO loc = new LocationTO();
 						loc.setLatitude(user.getCurrentLocation().getLatitude());
 						loc.setLongitude(user.getCurrentLocation()
 								.getLongitude());
 						loc.setAltitude(user.getCurrentLocation().getAltitude());
 						key.setCurrentLocation(loc);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determine if two users can communicate with each other
 	 * 
 	 * @param receiver
 	 *            The potential receiver
 	 * @param sender
 	 *            The sender of the message
 	 * @return True if receiver and sender can communicate
 	 */
 	private boolean canCommunicate(UserTO receiver, UserTO sender) {
 		if (receiver.getPreferences() != null
 				&& sender.getPreferences() != null) {
 			// calculate the distance
 			double distance = LocationHelper.getDistanceInMeters(
 					receiver.getCurrentLocation(), sender.getCurrentLocation());
 
 			// check if they can communicate
 			return (receiver.getPreferences().getMaxReceiveDistance() <= distance)
 					&& (sender.getPreferences().getMaxBroadcastDistance() >= distance);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Get all messages.
 	 * 
 	 * @return The messages
 	 */
 	public static Map<UserTO, List<MessageTO<Object>>> getMessages() {
 		return _messages;
 	}
 
 }
