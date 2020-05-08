 package edu.upenn.cis350;
 
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 
 public abstract class PushUtils {
 
 	private PushUtils() {}
 
 	/**
 	 * Adds an entry to the LazySubscription data store that this userId should be
 	 * subscribed to this event the next time the user opens the app. Also pushes
 	 * a notification to tell them they have been listed as a Primary Contact
 	 * 
 	 * @param context Context of activity where subscribe is called
 	 * @param event The event to be subscribed to
 	 * @param userId The userId to subscribe and notify
 	 */
 	public static boolean lazySubscribeContact(Context context, ParseObject event, String userId) {
 		ParseObject lazySub = new ParseObject("LazySubscription");
 		lazySub.put("userID", userId);
 		lazySub.put("eventID", event.getObjectId());
 		lazySub.saveEventually();
 		ParsePush userPush = new ParsePush();
 		userPush.setChannel("user_" + userId);
 		String msgText = ParseUser.getCurrentUser().getString("fullName") + " has set you as a Primary Contact for"
 				+ " the event \"" + event.getString("title") +"\"";
 		userPush.setMessage(msgText);
 		// expire after 5 minutes
 		userPush.setExpirationTimeInterval(300);
 		userPush.sendInBackground();
 
 		ParseObject notification = new ParseObject("Notification");
 		notification.put("user", userId);
 		notification.put("type", "event");
 		notification.put("id", event.getObjectId());
 		notification.put("text", msgText);
 		notification.put("isRead", false);
 		notification.saveEventually();
 		return true;
 	}
 
 	/**
 	 * Called when user logs in to check if any lazy subscriptions exist
 	 * If so, the user is then subscribed to the subscriptions, and deletes
 	 * them from the database
 	 * 
 	 * @param context Context of activity where user is active
 	 */
 	public static void lazySubscribeToEvents(final Context context) {
 		ParseQuery query = new ParseQuery("LazySubscription");
 		query.whereEqualTo("userID", ParseUser.getCurrentUser().getObjectId());
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> subscriptions, ParseException e) {
 				if (e == null) {
 					Set<String> mySub = PushService.getSubscriptions(context);
 					for (ParseObject sub : subscriptions) {
 						if (!mySub.contains("push_" + sub.getString("eventID"))) {
 							PushService.subscribe(context, "push_" + sub.getString("eventID"), ShowNotifications.class);
 						}
 						sub.deleteEventually();
 					}
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * Generic utility method to send push notifications
 	 * 
 	 * @param channel The channel on which to broadcast the notification, must start with letter
 	 * @param message The message to be broadcast in the notification
 	 * @param expireIn The number of seconds this notification should last before expiring
 	 */
 	public static void createPush(String channel, String message, Long expireIn) {
 		ParsePush push = new ParsePush();
 		push.setChannel(channel);
 		push.setMessage(message);
 		push.setExpirationTimeInterval(expireIn);
 		push.sendInBackground();
 	}
 
 	/**
 	 * Creates a push notification for Posting a Comment to a Message
 	 * 
 	 * @param messageId The messageID that this comment is posted on
 	 * @param comment The comment parse object.
 	 */
 	public static void createCommentPush(ParseObject message, ParseObject comment) {
 		ParsePush pushMessage = new ParsePush();
 		ParseUser user = ParseUser.getCurrentUser();
 		pushMessage.setChannel("push_" + message.getObjectId());
 		String msgText = user.getString("fullName") + " commented: " +
 				"\"" + comment.getString("text") + "\" on the message \"" +
 				message.getString("text") + "\"";
 		pushMessage.setMessage(msgText);
 		// expire after 5 minutes
 		pushMessage.setExpirationTimeInterval(300);
 		pushMessage.sendInBackground();
 		createNotification("message", comment.getObjectId(), msgText);
 	}
 
 	/**
 	 * Creates a push notification for Posting a Message to an Event
 	 * 
 	 * @param eventId The eventID of event that this message is posted on
 	 * @param message The message parse object.
 	 */
 	public static void createMessagePush(ParseObject event, ParseObject message) {
 		ParsePush pushMessage = new ParsePush();
 		ParseUser user = ParseUser.getCurrentUser();
 		pushMessage.setChannel("push_" + event.getObjectId());
 		String msgText = user.getString("fullName") + " posted: \"" + message.getString("text") + "\" on " +
 				"the event \"" + event.getString("title") + "\"";
 		pushMessage.setMessage(msgText);
 		// expire after 5 days
 		pushMessage.setExpirationTimeInterval(432000);
 		pushMessage.sendInBackground();
 		createNotification("event", event.getObjectId(), msgText);
 	}	
 
 	/**
 	 * Creates a push notification for Editing an Event
 	 * 
 	 * @param context Context for activity calling this method
 	 * @param eventId Event ID for event that has been changed
 	 * @param event Event ParseObject that is changed
 	 */
 	public static void createEventChangedPush(Context context, String eventId, ParseObject event) {
 		ParseUser user = ParseUser.getCurrentUser();
 		if (!PushService.getSubscriptions(context).contains("push_" + event.getObjectId())) {
 			PushService.subscribe(context, "push_" + eventId, ShowNotifications.class);
 
 			ParseObject subscription = new ParseObject("Subscription");
 			subscription.put("userId", user.getObjectId());
 			subscription.put("subscriptionId", eventId);
 			subscription.saveEventually();
 		}
 		ParsePush pushMessage = new ParsePush();
 		pushMessage.setChannel("push_" + eventId);
 		String msgText = user.getString("fullName") + " updated the event \"" + event.getString("title") + "\"";
 		pushMessage.setMessage(msgText);
 		// expire after 5 days
 		pushMessage.setExpirationTimeInterval(432000);
 		pushMessage.sendInBackground();
 		createNotification("event", event.getObjectId(), msgText);
 	}
 
 	/**
 	 * Creates a push notification for Marking an Event as Resolved
 	 * 
 	 * @param eventId Event ID for event that is marked Resolved
 	 * @param event Event that is marked Resolved
 	 */
 	public static void createEventResolvedPush(String eventId, ParseObject event) {
 		ParsePush pushMessage = new ParsePush();
 		ParseUser user = ParseUser.getCurrentUser();
 		pushMessage.setChannel("push_" + eventId);
 		String msgText = user.getString("fullName") + " marked \"" + event.getString("title") + "\" as Resolved.";
 		pushMessage.setMessage(msgText);
 		// expire after 5 days
 		pushMessage.setExpirationTimeInterval(432000);
 		pushMessage.sendInBackground();
 		createNotification("event", event.getObjectId(), msgText);
 	}
 	
 	/**
 	 * Creates a notification to be displayed in the application
 	 * 
	 * @param type The type of Activity this notification will forward to
	 * @param id The ID of the activity to be forwarded to
	 * @param message The messag to be displayed in the notification
 	 */
 	private static void createNotification(final String type, final String id,
 			final String message) {
 		ParseQuery query = new ParseQuery("Subscription");
 		query.whereEqualTo("subscriptionId", id);
 		query.findInBackground(new FindCallback() {
 
 			@Override
			public void done(List<ParseObject> subscriptions, ParseException e) {
 				if (e != null) {
 					Log.d("createNotification", e.getMessage());
 				} else {
					for (ParseObject subscription : subscriptions) {
 						ParseObject notification = new ParseObject("Notification");
						notification.put("user", subscription.getString("userId"));
 						notification.put("type", type);
 						notification.put("id", id);
 						notification.put("text", message);
 						notification.put("isRead", false);
 						notification.saveEventually();
 					}
 				}
 			}
 			
 		});
 
 	}
 
 }
