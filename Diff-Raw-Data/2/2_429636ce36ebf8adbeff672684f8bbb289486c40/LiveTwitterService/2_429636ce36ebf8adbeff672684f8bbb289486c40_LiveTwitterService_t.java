 package nz.co.searchwellington.twitter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.unto.twitter.Api;
 import net.unto.twitter.TwitterProtos.Status;
 import net.unto.twitter.methods.RepliesRequest;
 
 import org.apache.log4j.Logger;
 
 
 public class LiveTwitterService implements TwitterService {
 	
     private static final int REPLY_PAGES_TO_FETCH = 1;
 
 	Logger log = Logger.getLogger(LiveTwitterService.class);
 
     String username;
     String password;
 
 	public LiveTwitterService() {		
 	}
 
 		
 	public List<Status> getReplies() {
 		log.info("Getting twitter replies from live api for " + username);
         Api api = new Api.Builder().username(username).password(password).build();
         List<Status> all = new ArrayList<Status>();
         
		for (int i = 1; i <= REPLY_PAGES_TO_FETCH; i++) {
 			RepliesRequest repliesRequest = api.replies().page(i).build();
 			repliesRequest.get();
 			all.addAll(repliesRequest.get());
 		}
 		return all;
 	}
 
 	
 	@Override
 	public Status getTwitById(long twitterId) {
 		log.info("Getting tweet: " + twitterId);
         Api api = new Api.Builder().username(username).password(password).build();
 		return api.showStatus(twitterId).build().get();
 	}
 
 
 	public String getPassword() {
         return password;
     }
 
 
     public void setPassword(String password) {
         this.password = password;
     }
 
 
     public String getUsername() {
         return username;
     }
 
 
     public void setUsername(String username) {
         this.username = username;
     }
 
 
 	public boolean isConfigured() {
 		return this.username != null && !this.username.equals("") && this.password != null && !this.password.equals("");
 	}
 		
 }
