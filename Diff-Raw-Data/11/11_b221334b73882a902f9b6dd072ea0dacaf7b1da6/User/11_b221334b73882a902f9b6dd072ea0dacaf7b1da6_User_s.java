 package models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import play.data.validation.Email;
 import play.data.validation.Required;
 import siena.Filter;
 import siena.Id;
 import siena.Model;
 import siena.NotNull;
 import siena.Query;
 
 public class User extends Model {
 	
 	@Id
 	public Long id;
 	
 	/** Display name */
 	@Required
 	@NotNull
 	public String name;
 	
 	/** Email identifying the user */
 	@Required @Email
 	@NotNull
 	public String email;
 	
 	/** User TimeZone */
 	@Required
 	@NotNull
 	public String tz;
 	
 	/** User preference */
	public boolean exploreUnreadPosts;
 	
 	/** List of threads followed by the user */
 	@Filter("user")
 	public Query<Following> followedThreads;
 	
 	@Filter("user")
 	public Query<Reading> readPosts;
 	
 	/**
 	 * Ideas of profile settings:
 	 *  - Hide or show all unread posts
 	 *  - Automatically follow replied threads
 	 *  - Number of posts by page
 	 */
 	
 	public User(String name, String email, String tz) {
 		this.name = name;
 		this.email = email;
 		this.tz = tz;
 		this.exploreUnreadPosts = false;
 	}
 	
 	public static Query<User> all() {
 		return Model.all(User.class);
 	}
 	
 	public static User findByEmail(String email) {
 		return User.all().filter("email", email).get();
 	}
 	
 	public List<Following> followedThreads() {
 		List<Following> followings = new ArrayList<Following>();
 		followings.addAll(followedThreads.fetch());
 		
 		Collections.sort(followings, new Comparator<Following>() {
 			@Override
 			public int compare(Following o1, Following o2) {
 				return -o1.thread.lastPostDate().compareTo(o2.thread.lastPostDate());
 			}
 		});
 		
 		return followings;
 	}
 	
 	/**
 	 * Update user attributes
 	 */
 	public void update(Update update) {
 		name = update.name;
 		tz = update.tzId;
		exploreUnreadPosts = update.exploreUnreadPosts;
 		update();
 	}
 	
 	/**
 	 * Follow a given thread
 	 * @param thread Thread to be followed
 	 */
 	public void follow(Thread thread) {
 		if (!isFollowing(thread)) {
 			Following following = new Following(thread, this);
 			following.insert();
 		}
 	}
 	
 	/**
 	 * Remove a given thread from the followed threads set
 	 * @param thread
 	 */
 	public void doNotFollow(Thread thread) {
 		Following following = followedThreads.filter("thread", thread).get();
 		if (following != null) {
 			following.delete();
 		}
 	}
 	
 	/**
 	 * @return true if this user is following the given thread
 	 */
 	public boolean isFollowing(Thread thread) {
 		return followedThreads.filter("thread", thread).count() != 0;
 	}
 	
 	
 	/**
 	 * Mark the given post as read
 	 * @param post
 	 */
 	public void read(Post post) {
 		if (!hasRead(post)) {
 			Reading reading = new Reading(post, this);
 			reading.insert();
 		}
 	}
 	
 	/**
 	 * Mark the given post as unread
 	 * @param post
 	 */
 	public void unread(Post post) {
 		Reading reading = readPosts.filter("post", post).get();
 		if (reading != null) {
 			reading.delete();
 		}
 	}
 	
 	/**
 	 * @return true if this user has read the given post
 	 */
 	public boolean hasRead(Post post) {
 		return readPosts.filter("post", post).count() != 0;
 	}
 	
 	/**
 	 * Get the number of posts unread by this user in a given thread
 	 * @param thread
 	 * @return
 	 */
 	public long getUnreadPostCount(Thread thread) {
 		return Post.all().filter("thread", thread).count() - readPosts.filter("thread", thread).count();
 	}
 	
 	
 	public String toString() {
 		return name;
 	}
 
 	public static class Update {
 		@Required public String name;
 		@Required public String tzId;
		@Required public boolean exploreUnreadPosts;
 		
 		public Update(User user) {
 			name = user.name;
 			tzId = user.tz;
			exploreUnreadPosts = user.exploreUnreadPosts;
 		}
 	}
 }
