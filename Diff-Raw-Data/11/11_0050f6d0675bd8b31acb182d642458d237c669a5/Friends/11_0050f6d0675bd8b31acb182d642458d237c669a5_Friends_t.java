 package com.tuit.ar.models.timeline;
 
 import java.util.HashMap;
 
 import com.tuit.ar.api.TwitterAccount;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.Status;
 
 public class Friends extends com.tuit.ar.models.timeline.Status {
 	static private HashMap<TwitterAccount, Friends> instances = new HashMap<TwitterAccount, Friends>();
 
 	protected Friends(TwitterAccount account) {
 		super(account);
 		tweets = Status.select("is_home = 1 AND belongs_to_user = ?", new String[] { String.valueOf(account.getUser().getId()) }, null, null, "id DESC", null);
 		if (tweets.size() > 0) {
 			newestTweet = tweets.get(0).getId();
 		}
 	}
 
 	@Override
 	protected Options getTimeline() {
 		return Options.FRIENDS_TIMELINE;
 	}
 
 	public static com.tuit.ar.models.timeline.Status getInstance(TwitterAccount account) {
 		if (instances.containsKey(account) == false) instances.put(account, new Friends(account));
 		return instances.get(account);
 	}
 
 }
