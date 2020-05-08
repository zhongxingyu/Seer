 package com.ese2013.mub.util.parseDatabase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.ese2013.mub.social.CurrentUser;
 import com.ese2013.mub.social.FriendRequest;
 import com.ese2013.mub.social.Invitation;
 import com.ese2013.mub.social.User;
 import com.ese2013.mub.util.parseDatabase.tables.FriendRequestTable;
 import com.ese2013.mub.util.parseDatabase.tables.FriendshipTable;
 import com.ese2013.mub.util.parseDatabase.tables.InvitationTable;
 import com.ese2013.mub.util.parseDatabase.tables.InvitationUserTable;
 import com.ese2013.mub.util.parseDatabase.tables.UserTable;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 
 /**
  * Handles connection to the "Social database", this means it handles Friends,
  * FriendRequests, Invitations, Invitation Responses. All the "get" calls are
  * synchronous and all the "save"/"send" calls are asynchronous.
  */
 public class SocialDBHandler {
 
 	/**
 	 * Tries to find the given user by it's email. The passed in user is updated
 	 * with id and nickname if he can be found on the Server.
 	 * 
 	 * @param user
 	 *            User to be searched. Only needs a valid email address, must
 	 *            not be null.
 	 * @return User (the same as passed in) with now id and nickname set if they
 	 *         could be found.
 	 * @throws ParseException
 	 *             if the user can't be found on the server.
 	 */
 	public User getUser(User user) throws ParseException {
 		ParseObject u = getUserByMail(user);
 		user.setId(u.getObjectId());
 		user.setNick(u.getString(UserTable.NICKNAME));
 		return user;
 	}
 
 	/**
 	 * Tries to find the given CurrentUser by it's email. The passed in
 	 * CurrentUser is updated with id, nickname and the list of menus he has
 	 * rated if he can be found on the server.
 	 * 
 	 * @param user
 	 *            CurrentUser to be searched. Only needs a valid email address,
 	 *            must not be null.
 	 * @return CurrentUser (the same as passed in) with id and nickname from the
 	 *         Server if they could be found.
 	 * @throws ParseException
 	 *             if the CurrentUser can't be found on the server.
 	 */
 	public CurrentUser getCurrentUser(CurrentUser user) throws ParseException {
 		ParseObject u = getUserByMail(user);
 		user.setId(u.getObjectId());
 		user.setNick(u.getString(UserTable.NICKNAME));
 
 		List<String> menuIds = new MensaDBHandler().getRatedMenus(user);
 		user.setRatedMenuIds(menuIds);
 		return user;
 	}
 
 	/**
 	 * Does the same as "getCurrentUser", except it creates the user if he can't
 	 * be found on the Server.
 	 * 
 	 * @param user
 	 *            CurrentUser to be searched or created. Needs a valid email
 	 *            address and nickname, must not be null.
 	 * @return CurrentUser (the same as passed in) with id from the Server
 	 *         (either new id if the User couldn't be found or existing id if
 	 *         the User was found).
 	 * @throws ParseException
 	 *             if the CurrentUser can't be found or created on the server.
 	 */
 	public CurrentUser registerIfNotExists(CurrentUser user) throws ParseException {
 		ParseObject u;
 		try {
 			u = getUserByMail(user);
 		} catch (ParseException e) {
 			u = new ParseObject(UserTable.TABLE_NAME);
 			u.put(UserTable.EMAIL, user.getEmail());
 			u.put(UserTable.NICKNAME, user.getNick());
 			u.save();
 		}
 		user.setId(u.getObjectId());
 		user.setNick(u.getString(UserTable.NICKNAME));
 		return user;
 	}
 
 	/**
 	 * Adds the User represented by "otherEmail" to the passed in CurrentUser as
 	 * friend. (only on the server, caller must locally update CurrentUser!).
 	 * 
 	 * @param user
 	 *            CurrentUser to add the other person as friend to.
 	 * @param otherEmail
 	 *            email as String of the other user to be added.
 	 * @throws ParseException
 	 *             if the server can't find the other user.
 	 */
 	public void addAsFriend(CurrentUser user, String otherEmail) throws ParseException {
 		addFriendship(user, getUserByMail(otherEmail));
 	}
 
 	private void addFriendship(User user1, ParseObject user2) {
 		ParseObject friendship = new ParseObject(FriendshipTable.TABLE_NAME);
 		friendship.put(FriendshipTable.USER_1, ParseObject.createWithoutData(UserTable.TABLE_NAME, user1.getId()));
 		friendship.put(FriendshipTable.USER_2, user2);
		friendship.saveInBackground();
 	}
 
 	private void addFriendship(User user1, User user2) {
 		ParseObject friendship = new ParseObject(FriendshipTable.TABLE_NAME);
 		friendship.put(FriendshipTable.USER_1, ParseObject.createWithoutData(UserTable.TABLE_NAME, user1.getId()));
 		friendship.put(FriendshipTable.USER_2, ParseObject.createWithoutData(UserTable.TABLE_NAME, user2.getId()));
		friendship.saveInBackground();
 	}
 
 	/**
 	 * Removes the friendship between user1 and user2 from the server.
 	 * 
 	 * @param user1
 	 *            User which is a friend of user2. Must not be null and must
 	 *            have a valid id from the Parse-Server.
 	 * @param user2
 	 *            User which is a friend of user1. Must not be null and must
 	 *            have a valid id from the Parse-Server.
 	 */
 	public void removeFriendship(User user1, User user2) {
 		ParseQuery<ParseObject> query = buildRemoveFriendshipQuery(user1, user2);
 		query.findInBackground(new FindCallback<ParseObject>() {
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null) {
 					for (ParseObject p : objects)
						p.deleteInBackground();
 				}
 			}
 		});
 	}
 
 	private ParseQuery<ParseObject> buildRemoveFriendshipQuery(User user1, User user2) {
 		List<ParseQuery<ParseObject>> or = new ArrayList<ParseQuery<ParseObject>>();
 		ParseObject user1Object = ParseObject.createWithoutData(UserTable.TABLE_NAME, user1.getId());
 		ParseObject user2Object = ParseObject.createWithoutData(UserTable.TABLE_NAME, user2.getId());
 
 		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(FriendshipTable.TABLE_NAME);
 		query1.whereEqualTo(FriendshipTable.USER_1, user1Object);
 		query1.whereEqualTo(FriendshipTable.USER_2, user2Object);
 
 		ParseQuery<ParseObject> query2 = ParseQuery.getQuery(FriendshipTable.TABLE_NAME);
 		query2.whereEqualTo(FriendshipTable.USER_2, user1Object);
 		query2.whereEqualTo(FriendshipTable.USER_1, user2Object);
 
 		or.add(query1);
 		or.add(query2);
 		ParseQuery<ParseObject> query = ParseQuery.or(or);
 		return query;
 	}
 
 	/**
 	 * Retrieves the List of Users which are friends of the given CurrentUser.
 	 * 
 	 * @param user
 	 *            CurrentUser to search friends of.
 	 * @return List of Users which are friends of the given CurrentUser. The
 	 *         Users in the List all have valid ids, emails and nicknames.
 	 * @throws ParseException
 	 *             if the List of friends can't be retrieved.
 	 */
 	public List<User> getFriends(CurrentUser user) throws ParseException {
 		List<ParseObject> parseRelationships = getFriendsQuery(user).find();
 		List<User> friends = new ArrayList<User>();
 		for (ParseObject parseRelationship : parseRelationships)
 			friends.add(getOtherUser(parseRelationship, user));
 		return friends;
 	}
 
 	private ParseQuery<ParseObject> getFriendsQuery(User user) {
 		List<ParseQuery<ParseObject>> or = new ArrayList<ParseQuery<ParseObject>>();
 		ParseObject currentUserObject = ParseObject.createWithoutData(UserTable.TABLE_NAME, user.getId());
 		or.add(ParseQuery.getQuery(FriendshipTable.TABLE_NAME).whereEqualTo(FriendshipTable.USER_1, currentUserObject));
 		or.add(ParseQuery.getQuery(FriendshipTable.TABLE_NAME).whereEqualTo(FriendshipTable.USER_2, currentUserObject));
 		ParseQuery<ParseObject> query = ParseQuery.or(or);
 		query.include(FriendshipTable.USER_1);
 		query.include(FriendshipTable.USER_2);
 		return query;
 	}
 
 	private User getOtherUser(ParseObject parseRelationship, User current) {
 		ParseObject user1 = parseRelationship.getParseObject(FriendshipTable.USER_1);
 		ParseObject user2 = parseRelationship.getParseObject(FriendshipTable.USER_2);
 		ParseObject otherUser = user1.getObjectId().equals(current.getId()) ? user2 : user1;
 		return parseUser(otherUser);
 	}
 
 	private User parseUser(ParseObject parseUser) {
 		return new User(parseUser.getObjectId(), parseUser.getString(UserTable.EMAIL),
 				parseUser.getString(UserTable.NICKNAME));
 	}
 
 	/**
 	 * Sends the given Invitation to the recipients. Also sends a push
 	 * notification to all recipients.
 	 * 
 	 * @param invitation
 	 *            Invitation to be sent. Must have a sender and at least one
 	 *            receiver. Also none of the other data should be null (i.e.
 	 *            message, time).
 	 */
 	public void sendInvitation(Invitation invitation) {
 		ParseObject i = new ParseObject(InvitationTable.TABLE_NAME);
 		i.put(InvitationTable.MESSAGE, invitation.getMessage());
 		i.put(InvitationTable.MENSA, invitation.getMensa());
 		i.put(InvitationTable.FROM, ParseObject.createWithoutData(UserTable.TABLE_NAME, invitation.getFrom().getId()));
 		i.put(InvitationTable.TIME, invitation.getTime());
		i.saveInBackground();
 
 		for (User u : invitation.getTo()) {
 			ParseObject p = new ParseObject(InvitationUserTable.TABLE_NAME);
 			p.put(InvitationUserTable.INVITEE, ParseObject.createWithoutData(UserTable.TABLE_NAME, u.getId()));
 			p.put(InvitationUserTable.RESPONSE, invitation.getResponseOf(u).ordinal());
 			p.put(InvitationUserTable.INVITATION, i);
			p.saveInBackground();
 		}
 		sendPushNotfication(invitation);
 	}
 
 	private void sendPushNotfication(Invitation invitation) {
 		LinkedList<String> channels = new LinkedList<String>();
 		for (User u : invitation.getTo())
 			channels.add(getPushChannelNameOf(u));
 
 		ParsePush push = new ParsePush();
 		push.setChannels(channels);
 		push.setMessage("New invitation from " + invitation.getFrom().getNick() + ": " + invitation.getMessage());
 		push.sendInBackground();
 	}
 
 	private static String getPushChannelNameOf(User user) {
 		return "user_" + user.getId();
 	}
 
 	private ParseObject getUserByMail(User user) throws ParseException {
 		return getUserByMail(user.getEmail());
 	}
 
 	private ParseObject getUserByMail(String email) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery(UserTable.TABLE_NAME);
 		query.whereMatches(UserTable.EMAIL, email);
 		return query.getFirst();
 	}
 
 	/**
 	 * Downloads the (upcoming) Invitations the given User has retrieved.
 	 * 
 	 * @param user
 	 *            User to get retrieved invitations. Must not be null and must
 	 *            have a valid id.
 	 * @return List of Invitations which have been sent to the given User.
 	 * @throws ParseException
 	 *             if the Invitations can't be retrieved.
 	 */
 	public List<Invitation> getRetrievedInvitations(User user) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery(InvitationUserTable.TABLE_NAME);
 		query.whereEqualTo(InvitationUserTable.INVITEE, ParseObject.createWithoutData(UserTable.TABLE_NAME, user.getId()));
 		query.include(InvitationUserTable.INVITATION);
 		query.include(InvitationUserTable.INVITATION + "." + InvitationTable.FROM);
 		List<ParseObject> parseInvitations = query.find();
 		List<Invitation> invitations = new ArrayList<Invitation>();
 		for (ParseObject invitation : parseInvitations) {
 			ParseObject parseInv = invitation.getParseObject(InvitationUserTable.INVITATION);
 			ParseObject parseFrom = parseInv.getParseObject(InvitationTable.FROM);
 			HashMap<User, Invitation.Response> responses = new HashMap<User, Invitation.Response>();
 			responses.put(user, Invitation.Response.values()[invitation.getInt(InvitationUserTable.RESPONSE)]);
 			invitations.add(new Invitation(parseInv.getObjectId(), parseUser(parseFrom), responses, parseInv
 					.getString(InvitationTable.MESSAGE), parseInv.getInt(InvitationTable.MENSA), parseInv
 					.getDate(InvitationTable.TIME))
 
 			);
 		}
 		return invitations;
 	}
 
 	/**
 	 * Downloads the (upcoming) Invitations the given User has sent.
 	 * 
 	 * @param user
 	 *            User to get sent invitations. Must not be null and must have a
 	 *            valid id.
 	 * @return List of Invitations which have been sent by the given User,
 	 *         containing also the (current) responses of the addressees.
 	 * @throws ParseException
 	 *             if the Invitations can't be retrieved.
 	 */
 	public List<Invitation> getSentInvitations(User user) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery(InvitationTable.TABLE_NAME);
 		query.whereEqualTo(InvitationTable.FROM, ParseObject.createWithoutData(UserTable.TABLE_NAME, user.getId()));
 		List<ParseObject> parseInvitations = query.find();
 		List<Invitation> invitations = new ArrayList<Invitation>();
 		for (ParseObject parseInvite : parseInvitations)
 			invitations.add(new Invitation(parseInvite.getObjectId(), user, getInviteesAndResponses(parseInvite),
 					parseInvite.getString(InvitationTable.MESSAGE), parseInvite.getInt(InvitationTable.MENSA), parseInvite
 							.getDate(InvitationTable.TIME)));
 
 		return invitations;
 	}
 
 	private HashMap<User, Invitation.Response> getInviteesAndResponses(ParseObject parseInvite) throws ParseException {
 		ParseQuery<ParseObject> inviteesQuery = ParseQuery.getQuery(InvitationUserTable.TABLE_NAME);
 		inviteesQuery.whereEqualTo(InvitationUserTable.INVITATION, parseInvite);
 		inviteesQuery.include(InvitationUserTable.INVITEE);
 		List<ParseObject> parseInvitees = inviteesQuery.find();
 		HashMap<User, Invitation.Response> invitees = new HashMap<User, Invitation.Response>();
 		for (ParseObject parseUser : parseInvitees) {
 			User u = parseUser(parseUser.getParseObject(InvitationUserTable.INVITEE));
 			invitees.put(u, Invitation.Response.values()[parseUser.getInt(InvitationUserTable.RESPONSE)]);
 		}
 
 		return invitees;
 	}
 
 	/**
 	 * Sends the given FriendRequest.
 	 * 
 	 * @param request
 	 *            FriendRequest to be sent. Must have valid sender and receiver,
 	 *            i.e. they both should have valid Parse-Server ids and must not
 	 *            be null.
 	 */
 	public void sendFriendRequest(FriendRequest request) {
 		ParseObject parseRequest = ParseObject.create(FriendRequestTable.TABLE_NAME);
 		parseRequest.put(FriendRequestTable.FROM,
 				ParseObject.createWithoutData(UserTable.TABLE_NAME, request.getFrom().getId()));
 		parseRequest
 				.put(FriendRequestTable.TO, ParseObject.createWithoutData(UserTable.TABLE_NAME, request.getTo().getId()));
		parseRequest.saveInBackground();
 	}
 
 	/**
 	 * Downloads the List of pending friend requests for the given User.
 	 * 
 	 * @param user
 	 *            User to download received FriendRequests. Must have a valid id
 	 *            and not be null.
 	 * @return List of FriendRequests the User has received and not yet
 	 *         answered.
 	 * @throws ParseException
 	 *             if the FriendRequests couldn't be retrieved.
 	 */
 	public List<FriendRequest> getFriendRequests(User user) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery(FriendRequestTable.TABLE_NAME);
 		query.whereEqualTo(FriendRequestTable.TO, ParseObject.createWithoutData(UserTable.TABLE_NAME, user.getId()));
 		query.include(FriendRequestTable.FROM);
 		List<ParseObject> parseRequests = query.find();
 		List<FriendRequest> requests = new ArrayList<FriendRequest>();
 		for (ParseObject parseRequest : parseRequests) {
 			requests.add(new FriendRequest(parseRequest.getObjectId(), parseUser(parseRequest
 					.getParseObject(FriendRequestTable.FROM)), user));
 		}
 		return requests;
 	}
 
 	/**
 	 * Answers a FriendRequest. This means the FriendRequest is removed from the
 	 * server and, depending on "acceptFriendship", the new Friendship is added
 	 * or not.
 	 * 
 	 * @param request
 	 *            FriendRequest to be answered. Must have a valid id and not be
 	 *            null.
 	 * @param acceptFriendship
 	 *            true if the FriendShip should be accepted, false otherwise.
 	 */
 	public void answerFriendRequest(FriendRequest request, boolean acceptFriendship) {
 		if (acceptFriendship)
 			addFriendship(request.getTo(), request.getFrom());
 		ParseObject.createWithoutData(FriendRequestTable.TABLE_NAME, request.getId()).deleteInBackground();
 	}
 
 	/**
 	 * Answers an invitation.
 	 * 
 	 * @param invitation
 	 *            Invitation to be answered. Must have a valid id and not be
 	 *            null.
 	 * @param response
 	 *            Response to the invitation. Must be either ACCEPT or DECLINE,
 	 *            not UNKNOWN and not null.
 	 * @param user
 	 *            User who answers the invitation. Must have a valid id and not
 	 *            be null.
 	 * @throws ParseException
 	 *             if the invitation can't be answered.
 	 */
 	public void answerInvitation(Invitation invitation, final Invitation.Response response, User user) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery(InvitationUserTable.TABLE_NAME);
 		query.whereEqualTo(InvitationUserTable.INVITATION,
 				ParseObject.createWithoutData(InvitationTable.TABLE_NAME, invitation.getId()));
 		query.whereEqualTo(InvitationUserTable.INVITEE, ParseObject.createWithoutData(UserTable.TABLE_NAME, user.getId()));
 		query.getFirstInBackground(new GetCallback<ParseObject>() {
 			@Override
 			public void done(ParseObject invitationUser, ParseException e) {
 				if (e == null) {
 					invitationUser.put(InvitationUserTable.RESPONSE, response.ordinal());
 					invitationUser.saveInBackground();
 				}
 			}
 		});
 	}
 }
