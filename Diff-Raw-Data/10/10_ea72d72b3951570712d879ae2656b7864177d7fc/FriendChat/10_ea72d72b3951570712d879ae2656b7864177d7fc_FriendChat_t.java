 package org.rsbot.script.methods;
 
 import org.rsbot.script.wrappers.RSComponent;
 
 import java.util.regex.Pattern;
 
 /**
  * Friend chat related operations.
  *
  * @author Debauchery
  */
 public class FriendChat extends MethodProvider {
 
 	FriendChat(final MethodContext ctx) {
 		super(ctx);
 	}
 
 	private static final Pattern HTML_TAG = Pattern.compile("(^[^<]+>|<[^>]+>|<[^>]+$)");
 	public static final int INTERFACE_FRIEND_CHAT = 1109;
	public static final int INTERFACE_FRIEND_CHAT_JOIN_BUTTON = 27;
 	public static final int INTERFACE_FRIEND_CHAT_CHANNEL_INFO = 1;
 	public static final int INTERFACE_FRIEND_CHAT_USERS_LIST = 5;
 	public static final int INTERFACE_JOIN_FRIEND_CHAT = 752;
 	public static final int INTERFACE_JOIN_FRIEND_CHAT_LAST_CHANNEL = 3;
 	public static final String FRIEND_CHAT_STRING_TALK = "/";
 	private String lastCachedChannel = null;
 	public FriendsList friendsList = new FriendsList();
 
 	public enum ChatRank {
 		GUEST(-1), FRIEND(1004), RECRUIT(6226), CORPORAL(6225), SERGEANT(6224), LIEUTENANT(6232), CAPTAIN(6233),
 		GENERAL(6231), ADMIN(6228), DEPUTY_OWNER(6629), OWNER(6227);
 
 		private final int TEXTURE_ID;
 
 		private ChatRank(int textureId) {
 			TEXTURE_ID = textureId;
 		}
 
 		/**
 		 * Gets the texture id of this rank.
 		 *
 		 * @return the texture id of this rank
 		 */
 		public int getTextureId() {
 			return TEXTURE_ID;
 		}
 	}
 
 	public static class User implements Friend {
 
 		private String name;
 		private int worldNumber;
 		private boolean isInLobby;
 		private ChatRank rank = ChatRank.GUEST;
 
 		public User(String name, RSComponent rank, RSComponent world) {
 			this.name = name;
 			int textureId = rank.getBackgroundColor();
 			for (ChatRank chatRank : ChatRank.values()) {
 				if (chatRank.getTextureId() == textureId) {
 					this.rank = chatRank;
 					break;
 				}
 			}
 			String text = world.getText();
 			isInLobby = text.contains("Lo");
 			if (!text.endsWith(".")) {
 				worldNumber = Integer.parseInt(text.substring(text.indexOf(32) + 1));
 			} else {
 				worldNumber = -1;
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public String getName() {
 			return name;
 		}
 
 		/**
 		 * Gets this user's chat rank.
 		 *
 		 * @return this user's chat rank
 		 */
 		public ChatRank getRank() {
 			return rank;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public int getWorld() {
 			return worldNumber;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public boolean isInLobby() {
 			return isInLobby;
 		}
 
 	}
 
 
 	public interface Friend {
 		/**
 		 * Gets the name of this user.
 		 *
 		 * @return the name of this user
 		 */
 		public String getName();
 
 		/**
 		 * Gets the world number that this user is on.
 		 *
 		 * @return the world number or -1 if unavailable
 		 */
 		public int getWorld();
 
 		/**
 		 * Checks whether this user is in lobby.
 		 *
 		 * @return <tt>true</tt> if in lobby; otherwise <tt>false</tt>
 		 */
 		public boolean isInLobby();
 	}
 
 	/**
 	 * Joins the given channel.
 	 * If we are already in a channel, it will leave it.
 	 *
 	 * @param channel The channel to join
 	 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 	 */
 	public boolean join(final String channel) {
 		methods.game.openTab(Game.TAB_FRIENDS_CHAT);
 		if (isInChannel()) {
 			if (getName() == channel) {
 				return true;
 			}
 			if (!leave()) {
 				return false;
 			}
 		}
 		methods.interfaces.getComponent(INTERFACE_FRIEND_CHAT, INTERFACE_FRIEND_CHAT_JOIN_BUTTON).doClick();
 		sleep(random(500, 800));
 		if (methods.interfaces.get(INTERFACE_JOIN_FRIEND_CHAT).isValid()) {
 			final String lastChatCompText = methods.interfaces.getComponent(INTERFACE_JOIN_FRIEND_CHAT,
 					INTERFACE_JOIN_FRIEND_CHAT_LAST_CHANNEL).getComponent(0).getText();
 			lastCachedChannel = lastChatCompText.substring(lastChatCompText.indexOf(": ") + 2);
 			methods.keyboard.sendText(channel, true);
 			sleep(random(1550, 1800));
 			if (isInChannel()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 * Joins the given channel.
 	 * If we are already in a channel, it will leave it.
 	 *
 	 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 	 */
 	public boolean joinLastChannel() {
 		methods.game.openTab(Game.TAB_FRIENDS_CHAT);
 		if (isInChannel()) {
 			return true;
 		}
 		methods.interfaces.getComponent(INTERFACE_FRIEND_CHAT, INTERFACE_FRIEND_CHAT_JOIN_BUTTON).doClick();
 		sleep(random(500, 800));
 		if (methods.interfaces.get(INTERFACE_JOIN_FRIEND_CHAT).isValid()) {
 			final String lastChatCompText = methods.interfaces.getComponent(INTERFACE_JOIN_FRIEND_CHAT,
 					INTERFACE_JOIN_FRIEND_CHAT_LAST_CHANNEL).getComponent(0).getText();
 			lastCachedChannel = lastChatCompText.substring(lastChatCompText.indexOf(": ") + 2);
 			methods.interfaces.getComponent(INTERFACE_JOIN_FRIEND_CHAT,
 					INTERFACE_JOIN_FRIEND_CHAT_LAST_CHANNEL).doClick();
 			sleep(random(1550, 1800));
 			if (isInChannel()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Leaves the current channel.
 	 *
 	 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 	 */
 	public boolean leave() {
 		methods.game.openTab(Game.TAB_FRIENDS_CHAT);
 		if (isInChannel()) {
 			lastCachedChannel = getOwner();
 			methods.interfaces.getComponent(INTERFACE_FRIEND_CHAT, INTERFACE_FRIEND_CHAT_JOIN_BUTTON).doClick();
 			sleep(random(650, 900));
 			return isInChannel();
 		}
 		return true;
 	}
 
 	/**
 	 * Returns whether or not we're in a channel.
 	 *
 	 * @return <tt>true</tt> if in a channel; otherwise <tt>false</tt>
 	 */
 	public boolean isInChannel() {
 		methods.game.openTab(Game.TAB_FRIENDS_CHAT);
 		if (getName() != null) {
 			lastCachedChannel = getName();
 		}
 		return methods.interfaces.getComponent(INTERFACE_FRIEND_CHAT, INTERFACE_FRIEND_CHAT_JOIN_BUTTON).containsAction("Leave chat");
 	}
 
 	/**
 	 * Gets the first user matching with any of the provided names.
 	 *
 	 * @param names The names to look for.
 	 * @return an instance of <code>User</code>.
 	 */
 	public User getUser(String... names) {
 		if (isInChannel()) {
 			User[] users = getUsers();
 			for (String name : names) {
 				for (User user : users) {
 					if (name.equalsIgnoreCase(user.getName())) {
 						return user;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the users in the channel.
 	 *
 	 * @return The users in the channel or null if unavailable
 	 */
 	public User[] getUsers() {
 		if (isInChannel()) {
 			java.util.ArrayList<User> users = new java.util.ArrayList<User>();
 			RSComponent list = methods.interfaces.getComponent(FriendChat.INTERFACE_FRIEND_CHAT, FriendChat.INTERFACE_FRIEND_CHAT_USERS_LIST);
 			if (list != null) {
 				for (RSComponent c : list.getComponents()) {
 					if (c == null) {
 						continue;
 					}
 					String name = c.getText();
 					if (name != null && !name.isEmpty() && name.contains("..")) {
 						String[] actions = c.getActions();
 						if (actions != null) {
 							for (String action : actions) {
 								if (action != null) {
 									if (action.contains("Add") || action.contains("Remove")) {
 										name = action.substring(action.indexOf(32, action.indexOf(32) + 1) + 1);
 										break;
 									}
 								}
 							}
 						}
 					}
 					int componentIndex = c.getComponentIndex();
 					RSComponent rank = methods.interfaces.getComponent(FriendChat.INTERFACE_FRIEND_CHAT, 6);
 					rank = rank.getComponent(componentIndex);
 					RSComponent world = methods.interfaces.getComponent(FriendChat.INTERFACE_FRIEND_CHAT, 7);
 					world = world.getComponent(componentIndex);
 					users.add(new User(name, rank, world));
 				}
 				return users.toArray(new User[users.size()]);
 			}
 		}
 		return new User[0];
 	}
 
 
 	/**
 	 * Gets the name of the channel.
 	 *
 	 * @return The name of the channel or null if none
 	 */
 	public String getName() {
 		try {
 			methods.game.openTab(Game.TAB_FRIENDS_CHAT);
 			final String name = stripFormatting(methods.interfaces.getComponent(
 					INTERFACE_FRIEND_CHAT, INTERFACE_FRIEND_CHAT_CHANNEL_INFO).getText());
 			return name.substring(name.indexOf("Talking in: " + 12));
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the owner of the channel.
 	 *
 	 * @return The owner of the channel or null if none
 	 */
 	public String getOwner() {
 		try {
 			methods.game.openTab(Game.TAB_FRIENDS_CHAT);
 			if (getName() != null) {
 				lastCachedChannel = getName();
 			}
 			final String name = stripFormatting(methods.interfaces.getComponent(
 					INTERFACE_FRIEND_CHAT, INTERFACE_FRIEND_CHAT_CHANNEL_INFO).getText());
 			return name.substring(name.indexOf("Owner: ") + 7);
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	public String getLastCachedChannel() {
 		return lastCachedChannel;
 	}
 
 	/**
 	 * Strips HTML tags.
 	 *
 	 * @param input The string you want to parse.
 	 * @return The parsed {@code String}.
 	 */
 	private String stripFormatting(final String input) {
 		return HTML_TAG.matcher(input).replaceAll("");
 	}
 
 	/**
 	 * Sends the given message. It is not necessary to include the slash. This does not check if the user is on a channel.
 	 *
 	 * @param msg the message to send
 	 */
 	public void sendMessage(String msg) {
 		sendMessage(msg, false);
 	}
 
 	/**
 	 * Sends the given message. It is not necessary to include the slash. This does not check if the user is on a channel.
 	 *
 	 * @param msg     the message to send
 	 * @param instant if <tt>true</tt>, message will be sent instantly
 	 */
 	public void sendMessage(String msg, boolean instant) {
 		if (msg != null && !msg.isEmpty()) {
 			String message = FRIEND_CHAT_STRING_TALK.concat(msg);
 			if (instant) {
 				methods.keyboard.sendTextInstant(message, true);
 			} else {
 				methods.keyboard.sendText(message, true);
 			}
 		}
 	}
 
 
 	/**
 	 * Friends list related operations. Does not yet handle the lobby.
 	 *
 	 * @author Timer
 	 */
 	public class FriendsList {
 		public static final int FRIENDSLIST = 550;
 		public static final int FRIENDSLIST_BUTTON_ADD_FRIEND = 28;
 		public static final int FRIENDSLIST_BUTTON_REMOVE_FRIEND = 37;
 		public static final int FRIENDSLIST_LABEL_FRIENDS_COUNT = 17;
 		public static final int FRIENDSLIST_LIST_FRIENDS = 6;
 
 		public class Friend implements FriendChat.Friend {
 
 			private String name;
 			private int worldNumber;
 			private boolean isOffline;
 			private boolean isInLobby;
 
 			public Friend(String name, RSComponent world) {
 				this.name = name;
 				String text = world.getText();
 				isOffline = text.contains("Of");
 				isInLobby = text.contains("Lo");
 				if (!isOffline && !isInLobby && !text.endsWith(".")) {
 					worldNumber = Integer.parseInt(text);
 				} else {
 					worldNumber = -1;
 				}
 			}
 
 			/**
 			 * The name of your friend.
 			 *
 			 * @return Name of the friend.
 			 */
 			public String getName() {
 				return name;
 			}
 
 			public int getWorld() {
 				return worldNumber;
 			}
 
 			/**
 			 * Checks whether this friend is offline.
 			 *
 			 * @return <tt>true</tt> if offline; otherwise <tt>false</tt>
 			 */
 			public boolean isOffline() {
 				return isOffline;
 			}
 
 			public boolean isInLobby() {
 				return isInLobby;
 			}
 
 			/**
 			 * Checks whether this friend is online.
 			 *
 			 * @return <tt>true</tt> if online; otherwise <tt>false</tt>
 			 */
 			public boolean isOnline() {
 				return !isOffline && !isInLobby;
 			}
 		}
 
 		/**
 		 * Adds a friend.
 		 *
 		 * @param user the instance of <code>Friend</code> to add
 		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 		 */
 		public boolean add(final FriendChat.Friend user) {
 			if (user != null) {
 				Friend friend = getFriend(user.getName());
 				if (friend == null) {
 					return add(user.getName());
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Adds a friend.
 		 *
 		 * @param name the name of the friend to add
 		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 		 */
 		public boolean add(final String name) {
 			if (name != null && !name.isEmpty()) {
 				openTab();
 				RSComponent c = methods.interfaces.getComponent(FriendsList.FRIENDSLIST, FriendsList.FRIENDSLIST_BUTTON_ADD_FRIEND);
 				if (c != null) {
 					c.doClick();
 					sleep(random(300, 550));
 					methods.keyboard.sendText(name, true);
 					sleep(random(600, 800));
 					return getFriend(name) != null;
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Gets the first friend matching with any of the provided names.
 		 *
 		 * @param names the names to look for
 		 * @return an instance of <code>Friend</code> or <code>null</code> if no results
 		 */
 		public Friend getFriend(final String... names) {
 			Friend[] friends = getFriends();
 			for (String name : names) {
 				for (Friend friend : friends) {
 					if (name.equalsIgnoreCase(friend.getName())) {
 						return friend;
 					}
 				}
 			}
 			return null;
 		}
 
 		/**
 		 * Gets the end-user's friends from the friends list.
 		 *
 		 * @return an array instance of <code>Friend</code>
 		 */
 		public Friend[] getFriends() {
 			openTab();
 			RSComponent list = methods.interfaces.getComponent(FriendsList.FRIENDSLIST, FriendsList.FRIENDSLIST_LIST_FRIENDS);
 			if (list != null) {
 				java.util.ArrayList<Friend> friends = new java.util.ArrayList<Friend>();
 				for (RSComponent c : list.getComponents()) {
 					if (c == null) {
 						continue;
 					}
 					String name = c.getComponentName();
 					name = name.substring(name.indexOf(62) + 1);
 					RSComponent world = methods.interfaces.getComponent(FriendsList.FRIENDSLIST, 5);
 					world = world.getComponent(c.getIndex());
 					friends.add(new Friend(name, world));
 				}
 				return friends.toArray(new Friend[friends.size()]);
 			}
 			return new Friend[0];
 		}
 
 		/**
 		 * Gets all the friends matching with any of the provided names.
 		 *
 		 * @param names the names to look for
 		 * @return an array instance of <code>Friend</code>
 		 */
 		public Friend[] getFriends(final String... names) {
 			java.util.ArrayList<Friend> friends = new java.util.ArrayList<Friend>();
 			for (String name : names) {
 				for (Friend friend : getFriends()) {
 					if (name.equalsIgnoreCase(friend.getName())) {
 						friends.add(friend);
 						continue;
 					}
 				}
 			}
 			return friends.toArray(new Friend[friends.size()]);
 		}
 
 		/**
 		 * Opens the friends list tab if not already opened.
 		 */
 		public void openTab() {
 			if (methods.game.getCurrentTab() != Game.TAB_FRIENDS) {
 				methods.game.openTab(Game.TAB_FRIENDS);
 			}
 		}
 
 		/**
 		 * Removes a friend.
 		 *
 		 * @param name the name of the friend to remove
 		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 		 */
 		public boolean remove(String name) {
 			if (name != null && getFriend(name) != null) {
 				RSComponent c = methods.interfaces.getComponent(FriendsList.FRIENDSLIST, FriendsList.FRIENDSLIST_BUTTON_REMOVE_FRIEND);
 				if (c != null) {
 					c.doClick();
 					sleep(random(300, 550));
 					methods.keyboard.sendText(name, true);
 					sleep(random(600, 800));
 					return getFriend(name) != null;
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Removes a friend.
 		 *
 		 * @param user the instance of <code>Friend</code> to remove
 		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
 		 */
 		public boolean remove(FriendChat.Friend user) {
 			if (user != null) {
 				Friend friend = getFriend(user.getName());
 				if (friend != null) {
 					return remove(friend.getName());
 				}
 			}
 			return false;
 		}
 
 		/**
 		 * Gets the count of this end-user's friends.
 		 *
 		 * @return the count of this end-user's friends
 		 */
 		public int getCount() {
 			openTab();
 			RSComponent c = methods.interfaces.getComponent(FriendsList.FRIENDSLIST, FriendsList.FRIENDSLIST_LABEL_FRIENDS_COUNT);
 			if (c != null) {
 				String text = c.getText();
 				return Integer.parseInt(text.split(" ")[0]);
 			}
 			return -1;
 		}
 
 		/**
 		 * Checks whether the friends list of this end-user is full.
 		 *
 		 * @return <tt>true</tt> if full; otherwise <tt>false</tt>
 		 */
 		public boolean isFull() {
 			return getCount() == 200;
 		}
 	}
 }
