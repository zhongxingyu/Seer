 package controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import twitter4j.Category;
 import twitter4j.DirectMessage;
 import twitter4j.PagableResponseList;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
 import twitter4j.Trends;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import twitter4j.Query;
 import twitter4j.conf.ConfigurationBuilder;
 import model.Tweet;
 import model.TableModel;
 import model.TrendModel;
 import model.UserModel;
 import model.MessageModel;
 
 /**
  * This class represents the engine that connects to the Twitter Service.
  * 
  * @version 2.0
  * @author Cameron Ohrt & Danny Selgo
  */
 public class TwitterEngine {
 	/** represents the Twitter service. */
 	private Twitter engine;
 
 	/** represents the table in the GUI. */
 	private TableModel table;
 
 	/** represents the Trends JList in the GUI. */
 	private TrendModel trendList;
 
 	/** represents the Followers JLists in the GUI. */
 	private UserModel userList, friendsList;
 
 	/** represents the Messages JList in the GUI */
 	private MessageModel messageList;
 
 	/**
 	 * Constructor for the TwitterEngine class - uses the twitter4j.properties
 	 * file for authentication values.
 	 */
 	public TwitterEngine() {
 		engine = TwitterFactory.getSingleton();
 		table = new TableModel();
 	}
 	
 	public boolean isAuthenticated(){
 		return engine.getAuthorization().isEnabled();
 	}
 
 	/**
 	 * This method updates the status of the current user and modifies the table
 	 * model.
 	 * 
 	 * @param status
 	 *            the status to be posted.
 	 * @throws RuntimeException
 	 *             if Twitter fails to update the status.
 	 */
 	public final void updateStatus(final String status) {
 		try {
 			Status result = engine.updateStatus(status);
 			table.clear();
 			table.add(new Tweet(result.getId(), result.getCreatedAt(), result
 					.getUser().getScreenName(), result.getUser().getName(),
 					result.getText(), result.getUser().getFriendsCount(),
 					result.getUser().getFollowersCount()));
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to update status: "
 					+ ex.getMessage());
 		}
 	}
 	
 	public final void updateStatus(final String message, final String pathname) {
 		try {
 			StatusUpdate status = new StatusUpdate(message);
 			File file = new File(pathname);
 			status.setMedia(file);
 			Status result = engine.updateStatus(status);
 			table.clear();
 			table.add(new Tweet(result.getId(), result.getCreatedAt(), result
 					.getUser().getScreenName(), result.getUser().getName(),
 					result.getText(), result.getUser().getFriendsCount(),
 					result.getUser().getFollowersCount()));
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to update status: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method retweets a status selected by the user and modifies the table
 	 * model.
 	 * 
 	 * @param sID
 	 *            the status to be retweeted.
 	 * @throws RuntimeException
 	 *             if Twitter fails to retweet the status.
 	 */
 	public final void retweet(final long sID) {
 		try {
 			if (sID == 0) {
 				throw new IllegalArgumentException("Unable to retweet "
 						+ "this status.");
 			}
 			Status result = engine.retweetStatus(sID);
 			table.clear();
 			table.add(new Tweet(result.getId(), result.getCreatedAt(), result
 					.getUser().getScreenName(), result.getUser().getName(),
 					result.getText(), result.getUser().getFriendsCount(),
 					result.getUser().getFollowersCount()));
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to retweet: " + ex.getMessage());
 		} catch (IllegalArgumentException ex) {
 			throw new RuntimeException("Failed to retweet: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method searches Twitter for the requested users and updates the
 	 * table model.
 	 * 
 	 * @param query
 	 *            the users to be searched.
 	 * @throws RuntimeException
 	 *             if Twitter fails to search for the user.
 	 */
 	public final void searchPeople(final String query) {
 		try {
 			ResponseList<User> results = engine.searchUsers(query, 1);
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				if (results.get(i).getStatus() == null) {
 					table.add(new Tweet(0, null,
 							results.get(i).getScreenName(), results.get(i)
 									.getName(), "", results.get(i)
 									.getFriendsCount(), results.get(i)
 									.getFollowersCount()));
 				} else {
 					table.add(new Tweet(results.get(i).getStatus().getId(),
 							results.get(i).getStatus().getCreatedAt(), results
 									.get(i).getScreenName(), results.get(i)
 									.getName(), results.get(i).getStatus()
 									.getText(), results.get(i)
 									.getFriendsCount(), results.get(i)
 									.getFollowersCount()));
 				}
 			}
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Search failed: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method searches Twitter for the requested tweets and updates the
 	 * table model.
 	 * 
 	 * @param query
 	 *            the tweets to be searched.
 	 * @throws RuntimeException
 	 *             if Twitter fails search for tweets.
 	 */
 	public final void searchTweets(final String query) {
 		try {
 			Query toSearch = new Query(query);
 			List<Status> results = engine.search(toSearch).getTweets();
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				table.add(new Tweet(results.get(i).getId(), results.get(i)
 						.getCreatedAt(), results.get(i).getUser()
 						.getScreenName(), results.get(i).getUser().getName(),
 						results.get(i).getText(), results.get(i).getUser()
 								.getFriendsCount(), results.get(i).getUser()
 								.getFollowersCount()));
 			}
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Search failed: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method updates the table model with the most recent tweets of the
 	 * current user logged in.
 	 * 
 	 * @throws RuntimeException
 	 *             if Twitter fails to show the users timeline.
 	 */
 	public final void showTimeLine() {
 		try {
 			ResponseList<Status> results = engine.getUserTimeline();
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				table.add(new Tweet(results.get(i).getId(), results.get(i)
 						.getCreatedAt(), results.get(i).getUser()
 						.getScreenName(), results.get(i).getUser().getName(),
 						results.get(i).getText(), results.get(i).getUser()
 								.getFriendsCount(), results.get(i).getUser()
 								.getFollowersCount()));
 			}
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to find user timeline: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method updates the table model with the most recent tweets of the
 	 * user with the given screenName.
 	 * 
 	 * @param screenName
 	 *            screen name of the desired timeline.
 	 * @throws RuntimeException
 	 *             if Twitter fails to show the desired timeline.
 	 */
 	public void showTimeLine(final String screenName) {
 		try {
 			ResponseList<Status> results = engine.getUserTimeline(screenName);
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				table.add(new Tweet(results.get(i).getId(), results.get(i)
 						.getCreatedAt(), results.get(i).getUser()
 						.getScreenName(), results.get(i).getUser().getName(),
 						results.get(i).getText(), results.get(i).getUser()
 								.getFriendsCount(), results.get(i).getUser()
 								.getFollowersCount()));
 			}
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to find user timeline: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method deletes a selected tweet. The tweet selected must be owned by
 	 * the authenticated user.
 	 * 
 	 * @param sID
 	 *            the id of the tweet to delete.
 	 * @throws RuntimeException
 	 *             if Twitter fails to delete the tweet.
 	 */
 	public final void deleteTweet(final long sID) {
 		try {
 			engine.destroyStatus(sID);
 			// table.remove(tweet);
 			// or just clear table?
 			showTimeLine();
 
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to delete tweet: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method follows a selected user requested by the logged in user.
 	 * 
 	 * @param screenName
 	 *            id of the selected user to follow
 	 * @throws RuntimeException
 	 *             if Twitter fails to follow the user.
 	 */
 	public final void followUser(final String screenName) {
 		try {
 			engine.createFriendship(screenName);
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to follow: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * Generates a list of users that Twitter suggests you follow. Adds this
 	 * list to the UserModel.
 	 * 
 	 * @return userList - the current UserModel.
 	 * @throws RuntimeException
 	 *             if Twitter fails to generate a who to follow list.
 	 */
 	public UserModel generateSuggestedUsers() {
 		try {
 			if(isAuthenticated()){
 				ResponseList<Category> categories = engine
 						.getSuggestedUserCategories();
 				ResponseList<User> users = engine.getUserSuggestions(categories
 						.get(0).getSlug());
 				userList = new UserModel(users);
 				return userList;
 			} else {
 				return userList = new UserModel();
 			}
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to generate suggested users"
 					+ "to follow: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * Generates a list of current worldwide trending topics. Adds this list to
 	 * the TrendModel.
 	 * 
 	 * @return trendList - the current TrendModel.
 	 * @throws RuntimeException
 	 *             if Twitter generate a trending topics list.
 	 */
 	public TrendModel generateTrendingTopics() {
 		try {
 			if(isAuthenticated()){
 				Trends trends = engine.getPlaceTrends(1);
 				trendList = new TrendModel(trends);
 				return trendList;
 			} else {
 				return trendList = new TrendModel();
 			}
 			
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to generate trending topics: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method returns the current TableModel.
 	 * 
 	 * @return table - the current TableModel.
 	 */
 	public final TableModel getTable() {
 		return table;
 	}
 
 	/**
 	 * This method returns the current TrendModel.
 	 * 
 	 * @return trendList - the current TrendModel.
 	 */
 	public TrendModel getTrends() {
 		return trendList;
 	}
 
 	/**
 	 * This method returns the current UserModel.
 	 * 
 	 * @return userList - the current UserModel.
 	 */
 	public UserModel getUsers() {
 		return userList;
 	}
 
 	/**
 	 * This method returns the current MessageModel.
 	 */
 	public MessageModel getMessageModel() {
 		return messageList;
 	}
 
 	/**
 	 * This method returns the current FriendsList UserModel.
 	 */
 	public UserModel getFriendsList() {
 		return friendsList;
 	}
 
 	/**
 	 * This method returns the logged in user information to be displayed.
 	 * 
 	 * @return an array of strings that holds the logged in user information.
 	 * @throws RuntimeException
 	 *             if Twitter fails to get the desired information.
 	 */
 	public final String[] getUserInformation() {
 		String[] info = new String[5];
 		if(isAuthenticated()){
 			try {
 				User user = engine.showUser(engine.getId());
 				info[0] = user.getName();
 				info[1] = user.getScreenName();
 				info[2] = "" + user.getFriendsCount();
 				info[3] = "" + user.getFollowersCount();
 				info[4] = "" + user.getStatusesCount();
 			} catch (TwitterException ex) {
 				throw new RuntimeException(
 						"Failed to accumulate user information: " + ex.getMessage());
 			}
 		} else {
 			info[0] = "<null>";
 			info[1] = "<null>";
 			info[2] = "<null>";
 			info[3] = "<null>";
 			info[4] = "<null>";
 		}
 		
 		return info;
 	}
 	
 	public final void signUp(final String consumerKey,
 			final String consumerSecret, final String accessToken,
 			final String accessTokenSecret, final String username, final String password) {
 		try {
 			Properties prop = new Properties();
 
 			prop.setProperty("oauth.consumerKey", consumerKey);
 			prop.setProperty("oauth.consumerSecret", consumerSecret);
 			prop.setProperty("oauth.accessToken", accessToken);
 			prop.setProperty("oauth.accessTokenSecret", accessTokenSecret);
 			
 			prop.setProperty(username + ".consumerKey", consumerKey);
 			prop.setProperty(username + ".consumerSecret", consumerSecret);
 			prop.setProperty(username + ".accessToken", accessToken);
 			prop.setProperty(username + ".accessTokenSecret", accessTokenSecret);
 			prop.setProperty(username + ".password", password);
 
 			prop.store(new FileOutputStream("src/twitter4j.properties", true), null);
 
 			ConfigurationBuilder cb = new ConfigurationBuilder();
 			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
 					.setOAuthConsumerSecret(consumerSecret)
 					.setOAuthAccessToken(accessToken)
 					.setOAuthAccessTokenSecret(accessTokenSecret);
 			TwitterFactory tf = new TwitterFactory(cb.build());
 			engine = tf.getInstance();
 		} catch (FileNotFoundException ex) {
 			try {
 				File file = new File("src/twitter4j.properties");
 				file.createNewFile();
 			} catch (IOException exc) {
 				throw new RuntimeException("Cannot create .properties file: "
 						+ exc.getMessage());
 			}
 		} catch (IOException ex) {
 			throw new RuntimeException("Failed to write to .properties file: "
 					+ ex.getMessage());
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to switch account: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method allows the logged in user to switch accounts. It also updates
 	 * the twitter4j.properties file to allow the new user to be automatically
 	 * logged in on startup.
 	 * 
 	 * @param consumerKey
 	 *            the consumerKey of the account to switch to
 	 * @param consumerSecret
 	 *            the consumerSecret of the account to switch to
 	 * @param accessToken
 	 *            the accessToken of the account to switch to
 	 * @param accessTokenSecret
 	 *            the accessTokenSecret of the account to switch to
 	 * @throws RuntimeException
 	 *             if Twitter fails to switch the accounts.
 	 */
 	public final void switchAccount(final String username, final String password) {
 		try {
 			Properties prop = new Properties();
 			prop.load(new FileInputStream("src/twitter4j.properties"));
 
 			if(prop.getProperty(username + ".password").compareTo(password) != 0){
 				throw new IllegalArgumentException("Username or password is incorrect.");
 			}
 			
 			final String consumerKey = prop.getProperty(username + ".consumerKey");
 			final String consumerSecret = prop.getProperty(username + ".consumerSecret");
 			final String accessToken = prop.getProperty(username + ".accessToken");
 			final String accessTokenSecret = prop.getProperty(username + ".accessTokenSecret");
 			
 			prop.setProperty("oauth.consumerKey", consumerKey);
 			prop.setProperty("oauth.consumerSecret", consumerSecret);
 			prop.setProperty("oauth.accessToken", accessToken);
 			prop.setProperty("oauth.accessTokenSecret", accessTokenSecret);
 
 			prop.store(new FileOutputStream("src/twitter4j.properties"), null);
 
 			ConfigurationBuilder cb = new ConfigurationBuilder();
 			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
 					.setOAuthConsumerSecret(consumerSecret)
 					.setOAuthAccessToken(accessToken)
 					.setOAuthAccessTokenSecret(accessTokenSecret);
 			TwitterFactory tf = new TwitterFactory(cb.build());
 			engine = tf.getInstance();
 		} catch (IllegalArgumentException ex) {
 			throw new RuntimeException(ex.getMessage());
 		} catch (IOException ex) {
 			throw new RuntimeException("Failed to load from .properties file: "
 					+ ex.getMessage());
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to switch account: "
 					+ ex.getMessage());
 		}
 	}
 	
 	public void signOut(){
 		try {
 			engine.shutdown();
 			Properties prop = new Properties();
 			prop.load(new FileInputStream("src/twitter4j.properties"));
 			
 			prop.remove("oauth.consumerKey");
 			prop.remove("oauth.consumerSecret");
 			prop.remove("oauth.accessToken");
 			prop.remove("oauth.accessTokenSecret");
 			
 		} catch (IOException ex) {
 			throw new RuntimeException("Failed to load from .properties file: "
 					+ ex.getMessage());
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to switch account: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method adds a status to the logged in user's favorites
 	 * 
 	 * @param id
 	 *            the status to be favorited
 	 */
 	public final void addFavorite(final long id) {
 		try {
 			engine.createFavorite(id);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to add status as a favorite.");
 		}
 	}
 
 	/**
 	 * This method deletes a status from the logged in user's favorites
 	 * 
 	 * @param id
 	 *            the status to delete from favorites
 	 */
 	public final void deleteFavorite(final long id) {
 		try {
 			engine.destroyFavorite(id);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to delete favorited status.");
 		}
 	}
 
 	/**
 	 * This method gets the list of favorites of the logged in user
 	 * 
 	 * 
 	 */
 	public final void showUserFavorites() {
 		try {
 			ResponseList<Status> results = engine.getFavorites();
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				table.add(new Tweet(results.get(i).getId(), results.get(i)
 						.getCreatedAt(), results.get(i).getUser()
 						.getScreenName(), results.get(i).getUser().getName(),
 						results.get(i).getText(), results.get(i).getUser()
 								.getFriendsCount(), results.get(i).getUser()
 								.getFollowersCount()));
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to retrieve favorites.");
 		}
 	}
 
 	/**
 	 * This method gets the list of favorites of the requested user
 	 * 
 	 * @param screenName
 	 *            the user to get list of favorites from
 	 */
 	public final void showUserFavorites(final String screenName) {
 		try {
 			ResponseList<Status> results = engine.getFavorites(screenName);
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				table.add(new Tweet(results.get(i).getId(), results.get(i)
 						.getCreatedAt(), results.get(i).getUser()
 						.getScreenName(), results.get(i).getUser().getName(),
 						results.get(i).getText(), results.get(i).getUser()
 								.getFriendsCount(), results.get(i).getUser()
 								.getFollowersCount()));
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException(
 					"Failed to retrieve selected user's favorites.");
 		}
 	}
 
 	/**
 	 * This method updates the table model to show the logged in user's home
 	 * time line
 	 */
 	public final void showHomeTimeline() {
 		try {
 			ResponseList<Status> results = engine.getHomeTimeline();
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				table.add(new Tweet(results.get(i).getId(), results.get(i)
 						.getCreatedAt(), results.get(i).getUser()
 						.getScreenName(), results.get(i).getUser().getName(),
 						results.get(i).getText(), results.get(i).getUser()
 								.getFriendsCount(), results.get(i).getUser()
 								.getFollowersCount()));
 			}
 		} catch (TwitterException ex) {
 			throw new RuntimeException("Failed to find home timeline: "
 					+ ex.getMessage());
 		}
 	}
 
 	/**
 	 * This method gets all direct messages that the logged in user has sent to
 	 * them.
 	 */
 	public final void getInbox() {
 		try {
 			ResponseList<DirectMessage> list = engine.getDirectMessages();
 			messageList.clear();
 			messageList.add(list);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to retrieve messages.");
 		}
 	}
 
 	/**
 	 * This method gets all the sent messages by the logged in user
 	 */
 	public final void getSentMessages() {
 		try {
 			ResponseList<DirectMessage> list = engine.getSentDirectMessages();
 			messageList.clear();
 			messageList.add(list);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to retrieve sent messages.");
 		}
 	}
 
 	/**
 	 * This method shows the requested message
 	 * 
 	 * @param id
 	 *            the message to show
 	 */
 	public final DirectMessage showMessage(final long id) {
 		try {
 			return engine.showDirectMessage(id);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to show message.");
 		}
 	}
 
 	/**
 	 * THis method sends a message to a user from the logged in user
 	 * 
 	 * @param id
 	 *            the user id to receive the message
 	 * @param text
 	 *            the message itself
 	 */
 	public final void sendMessage(final long id, final String text) {
 		try {
 			engine.sendDirectMessage(id, text);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to send message.");
 		}
 	}
 
 	/**
 	 * This method deletes the requested message
 	 * 
 	 * @param id
 	 *            the message to delete
 	 */
 	public final void deleteMessage(final long id) {
 		try {
 			engine.destroyDirectMessage(id);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to delete message.");
 		}
 	}
 
 	/**
 	 * This method retrives the messages sent and received between the logged in
 	 * user and the requested user and orders them by date created.
 	 * 
 	 * @param screenName
 	 *            the requested user to see the conversation between
 	 */
 	public final void showConversation(final String screenName) {
 		try {
 			ResponseList<DirectMessage> sent = engine.getSentDirectMessages();
 			ResponseList<DirectMessage> received = engine.getDirectMessages();
 			ArrayList<DirectMessage> conversation = new ArrayList<DirectMessage>();
 			for (DirectMessage m : sent) {
 				if (m.getRecipientScreenName().equals(screenName)) {
 					conversation.add(m);
 				}
 			}
 			for (DirectMessage m : received) {
 				if (m.getSenderScreenName().equals(screenName)) {
 					conversation.add(m);
 				}
 			}
 
 			Collections.sort(conversation, new MessageComparator());
 			messageList.clear();
 			messageList.add(conversation);
 
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to retrieve conversation.");
 		}
 
 	}
 
 	/**
 	 * This method reports the requested user as a spammer
 	 * 
 	 * @param screenName
 	 *            the user to report as a spammer
 	 */
 	public final void reportSpam(final String screenName) {
 		try {
 			engine.reportSpam(screenName);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to report spam.");
 		}
 	}
 
 	/**
 	 * This method blocks the requested user from the logged in user
 	 * 
 	 * @param screenName
 	 *            the user to block
 	 */
 	public final void blockUser(final String screenName) {
 		try {
 			engine.createBlock(screenName);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to block user.");
 		}
 	}
 
 	/**
 	 * This method unblocks the requested user from the logged in user
 	 * 
 	 * @param screenName
 	 *            the user to unblock
 	 */
 	public final void unblockUser(final String screenName) {
 		try {
 			engine.destroyBlock(screenName);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to unblock user.");
 		}
 	}
 
 	/**
 	 * This method shows the users blocked by the logged in user
 	 */
 	public final void showBlockedUsers() {
 		try {
 			PagableResponseList<User> results = engine.getBlocksList();
 			table.clear();
 			for (int i = 0; i < results.size(); i++) {
 				if (results.get(i).getStatus() == null) {
 					table.add(new Tweet(0, null,
 							results.get(i).getScreenName(), results.get(i)
 									.getName(), "", results.get(i)
 									.getFriendsCount(), results.get(i)
 									.getFollowersCount()));
 				} else {
 					table.add(new Tweet(results.get(i).getStatus().getId(),
 							results.get(i).getStatus().getCreatedAt(), results
 									.get(i).getScreenName(), results.get(i)
 									.getName(), results.get(i).getStatus()
 									.getText(), results.get(i)
 									.getFriendsCount(), results.get(i)
 									.getFollowersCount()));
 				}
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to show blocked users.");
 		}
 	}
 
 	/**
 	 * This method shows the users who follow the logged in user
 	 */
 	public final void showFollowers() {
 		try {
 			PagableResponseList<User> result = engine.getFriendsList(
 					engine.getScreenName(), 20);
 			friendsList = new UserModel(result);
 		} catch (Exception ex) {
 			throw new RuntimeException("Failed to show users that follow you.");
 		}
 	}
 }
