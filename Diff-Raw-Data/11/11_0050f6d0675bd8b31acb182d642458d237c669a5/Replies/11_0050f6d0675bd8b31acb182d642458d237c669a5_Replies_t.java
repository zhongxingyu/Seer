 package com.tuit.ar.models.timeline;
 
 import java.util.HashMap;
 
 import com.tuit.ar.api.TwitterAccount;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.Status;
 
 public class Replies extends com.tuit.ar.models.timeline.Status {
 	static private HashMap<TwitterAccount, Replies> instances = new HashMap<TwitterAccount, Replies>(); 
 
 	protected Replies(TwitterAccount account) {
 		super(account);
 		tweets = Status.select("is_reply = 1 AND belongs_to_user = ?", new String[] { String.valueOf(account.getUser().getId()) }, null, null, "id DESC", null);
 		if (tweets.size() > 0) {
 			newestTweet = tweets.get(0).getId();
 		}
 	}
 
 	@Override
 	protected Options getTimeline() {
 		return Options.REPLIES_TIMELINE;
 	}
 
 	public static com.tuit.ar.models.timeline.Status getInstance(TwitterAccount account) {
 		if (instances.containsKey(account) == false) instances.put(account, new Replies(account));
 		return instances.get(account);
 	}
 }
