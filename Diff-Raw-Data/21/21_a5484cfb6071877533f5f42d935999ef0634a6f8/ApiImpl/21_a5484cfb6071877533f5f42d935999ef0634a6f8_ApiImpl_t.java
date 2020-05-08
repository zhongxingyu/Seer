 package jsfun.examples.twitter;
 
 import twitter4j.*;
 import twitter4j.Twitter;
 
 import java.util.List;
 import java.util.Date;
 
 
 public class ApiImpl implements Api {
 
 	private Twitter api = new Twitter();
 
 	public void login(String username, String password) {
 		api.setUserId(username);
 		api.setPassword(password);
 	}
 
 	public List<Status> getFriendsTimeline() throws TwitterException {
 		return api.getFriendsTimeline();
 	}
 
 	public List<Status> getFriendsTimeline(int page) throws TwitterException {
 		return api.getFriendsTimeline(page);
 	}
 
 	public List<Status> getFriendsTimeline(long sinceId, int page) throws TwitterException {
 		return api.getFriendsTimeline(sinceId, page);
 	}
 
 	public List<Status> getFriendsTimeline(String id) throws TwitterException {
 		return api.getFriendsTimeline(id);
 	}
 
 	public List<Status> getFriendsTimeline(String id, int page) throws TwitterException {
 		return api.getFriendsTimeline(id, page);
 	}
 
 	public List<Status> getFriendsTimeline(long sinceId, String id, int page) throws TwitterException {
 		return api.getFriendsTimeline(sinceId, id, page);
 	}
 
 	public List<Status> getFriendsTimeline(long sinceId) throws TwitterException {
 		return api.getFriendsTimeline(sinceId);
 	}
 
 	public List<Status> getFriendsTimeline(String id, long sinceId) throws TwitterException {
 		return api.getFriendsTimeline(id, sinceId);
 	}
 
 	public List<Status> getUserTimeline(String id, int count, long sinceId) throws TwitterException {
 		return api.getUserTimeline(id, count, sinceId);
 	}
 
 	public List<Status> getUserTimeline(String id, Date since) throws TwitterException {
 		return api.getUserTimeline(id, since);
 	}
 
 	public List<Status> getUserTimeline(String id, int count) throws TwitterException {
 		return api.getUserTimeline(id, count);
 	}
 
 	public List<Status> getUserTimeline(int count, Date since) throws TwitterException {
 		return api.getUserTimeline(count, since);
 	}
 
 	public List<Status> getUserTimeline(String id) throws TwitterException {
		System.out.println("ApiImpl.getUserTimeline(" + id + ")");
 		return api.getUserTimeline(id);
 	}
 
 	public List<Status> getUserTimeline() throws TwitterException {
 		return api.getUserTimeline();
 	}
 
 	public List<Status> getReplies() throws TwitterException {
 		return api.getReplies();
 	}
 
 	public List<Status> getReplies(long sinceId) throws TwitterException {
 		return api.getReplies(sinceId);
 	}
 
 	public List<Status> getReplies(int page) throws TwitterException {
 		return api.getReplies(page);
 	}
 
 	public List<Status> getReplies(long sinceId, int page) throws TwitterException {
 		return api.getReplies(sinceId, page);
 	}
 
 	public List<User> getFriends() throws TwitterException {
 		return api.getFriends();
 	}
 
 	public List<User> getFriends(int page) throws TwitterException {
 		return api.getFriends(page);
 	}
 
 	public List<User> getFriends(String id) throws TwitterException {
 		return api.getFriends(id);
 	}
 
 	public List<User> getFriends(String id, int page) throws TwitterException {
 		return api.getFriends(id, page);
 	}
 
 	public List<User> getFollowers() throws TwitterException {
 		return api.getFollowers();
 	}
 
 	public List<User> getFollowers(int page) throws TwitterException {
 		return api.getFollowers(page);
 	}
 
 	public List<User> getFollowers(String id) throws TwitterException {
 		return api.getFollowers(id);
 	}
 
 	public List<User> getFollowers(String id, int page) throws TwitterException {
 		return api.getFollowers(id, page);
 	}
 
 	@Override
 	public QueryResult search(Query query) throws TwitterException {
 		System.out.println("query = " + query);
 		return api.search(query);
 	}
 }
