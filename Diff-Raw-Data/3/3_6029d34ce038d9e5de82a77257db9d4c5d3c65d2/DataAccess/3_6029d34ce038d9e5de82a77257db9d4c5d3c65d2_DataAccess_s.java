 package com.brif.nix.model;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 
 import com.brif.nix.notifications.EmptyNotificationHandler;
 import com.brif.nix.notifications.NotificationsHandler;
 import com.brif.nix.parse.Parse;
 import com.brif.nix.parse.ParseException;
 import com.brif.nix.parse.ParseObject;
 import com.brif.nix.parse.ParseQuery;
 import com.brif.nix.parse.UpdateCallback;
 import com.brif.nix.parser.MessageParser;
 
 /**
  * Data access is only allowed here, decoupling data access to single point
  * 
  * @author Roy, 2013
  */
 public class DataAccess {
 
 	// parse-related constants
 	private static final String MESSAGES_SCHEMA = "Messages";
 	private static final String GROUPS_SCHEMA = "Groups";
 	private static final String USERS_SCHEMA = "Users";
 	private static final String ACCESS_KEY = "NoVHzsTel7csA1aGoMBNyVz2mHzed4LaSb1d4lme";
 	private static final String APP = "mMS3oCiZOHC15v8OGTidsRgHI0idYut39QKrIhIH";
 
 	private NotificationsHandler notificationsHandler;
 
 	public DataAccess(NotificationsHandler notificationsHandler) {
 		this.notificationsHandler = notificationsHandler;
 		Parse.initialize(APP, ACCESS_KEY);
 	}
 
 	public DataAccess() {
 		this(new EmptyNotificationHandler());
 	}
 
 	public User findByEmail(String email) {
 		ParseQuery query1 = new ParseQuery(USERS_SCHEMA);
 		query1.whereEqualTo("email", email);
 		List<ParseObject> profiles;
 		try {
 			profiles = query1.find();
 		} catch (ParseException e) {
 			return null;
 		}
 		if (profiles.size() == 0) {
 			return null;
 		}
 		final ParseObject parseObject = profiles.get(0);
 		final Long next_uid = parseObject.getLong("next_uid", 1);
 
 		return new User(email, parseObject.getString("access_token"),
 				parseObject.getString("refresh_token"),
 				parseObject.getString("origin"), next_uid,
 				parseObject.getObjectId());
 	}
 
 	public void createMessage(User currentUser, MessageParser mp, String groupId)
 			throws IOException, MessagingException {
 		ParseObject parseMessage = new ParseObject(MESSAGES_SCHEMA);
 		parseMessage.put("message_id", mp.getMessageId());
 		parseMessage.put("user_id", currentUser.objectId);
 		parseMessage.put("group_id", groupId);
 		parseMessage.put("from", mp.getFrom());
 		if (mp.getCharset() != null) {
 			parseMessage.put("charset", mp.getCharset());	
 		}
		parseMessage.put("content", mp.getContent());
 		parseMessage.setCharset(mp.getCharset());
 		parseMessage.saveInBackground();
 	}
 
 	public String createGroup(User currentUser, MessageParser mp) {
 		try {
 			ParseQuery query = new ParseQuery(GROUPS_SCHEMA);
 			query.whereEqualTo("md5", mp.getGroupUnique());
 			ParseObject group = null;
 			List<ParseObject> groups = query.find();
 			for (ParseObject potentials : groups) {
 				if (potentials.getString("user_id").equals(currentUser.objectId)) {
 					group = potentials;
 				}
 			}
 
 			if (group == null) {
 				group = new ParseObject(GROUPS_SCHEMA);
 				group.put("user_id", currentUser.objectId);
 				group.put("recipients", mp.getGroup());
 				group.put("md5", mp.getGroupUnique());
 				group.save();
 
 				// send notification
 				notifyGroupAdded(currentUser, group);
 			} else {
 				notifyGroupModified(currentUser, mp, group);
 			}
 
 			return group.getObjectId();
 
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private void notifyGroupModified(final User currentUser, MessageParser mp,
 			final ParseObject group) {
 
 		group.incrementInBackground(new UpdateCallback() {
 
 			@Override
 			public void done(ParseException e) {
 				Group g = new Group(group.getObjectId(), group
 						.getString("recipients"), group.getString("md5"), group
 						.getLong("size", 1), group.getLong("unseen", 1));
 
 				notificationsHandler.notifyGroupsEvent(currentUser.email,
 						"modified", g.toMap(), Charset.defaultCharset()
 								.toString());
 
 				group.incremenetInBackground("size");
 			}
 		}, "unseen", 1);
 
 	}
 
 	private void notifyGroupAdded(User currentUser, ParseObject group)
 			throws MessagingException {
 
 		Group g = new Group(group.getObjectId(), group.getString("recipients"),
 				group.getString("md5"));
 		notificationsHandler.notifyGroupsEvent(currentUser.email, "added",
 				g.toMap(), Charset.defaultCharset().toString());
 	}
 
 	public void storeMessage(User currentUser, MessageParser mp)
 			throws IOException, MessagingException {
 		final String groupId = createGroup(currentUser, mp);
 		createMessage(currentUser, mp, groupId);
 	}
 
 	public void updateUserToken(final User currentUser) {
 		ParseObject user = new ParseObject(USERS_SCHEMA);
 		user.setObjectId(currentUser.objectId);
 		user.put("access_token", currentUser.access_token);
 		user.updateInBackground();
 	}
 
 	public void updateUserNextUID(final User currentUser) {
 		ParseObject user = new ParseObject(USERS_SCHEMA);
 		user.setObjectId(currentUser.objectId);
 		user.put("next_uid", currentUser.next_uid);
 		user.updateInBackground();
 	}
 
 	public void removeMessage(Message message) {
 		ParseObject parseMessage = new ParseObject(MESSAGES_SCHEMA);
 		parseMessage.put("message_id", message.getMessageNumber());
 		parseMessage.deleteInBackground();
 	}
 }
