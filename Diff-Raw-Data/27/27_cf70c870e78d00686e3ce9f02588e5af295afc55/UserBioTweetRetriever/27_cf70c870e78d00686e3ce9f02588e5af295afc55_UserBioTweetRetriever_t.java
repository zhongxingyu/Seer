 package toctep.skynet.backend.bll;
 
 import twitter4j.Status;
 
 public class UserBioTweetRetriever extends TweetRetriever {
 	
 	
 	public UserBioTweetRetriever() {
 		super();
 	}
 	
 	@Override
 	public void run() {
 		getTwitterStream().sample();
 	}
 	
 	@Override
 	public void process(Status status) {
 	
 		twitter4j.User user = status.getUser();
 		
 		if (TweetUtils.isDutchLanguage(status.getText(), user.getLang()) 
				|| TweetUtils.isDutchLocation(user.getLocation()) 
				|| TweetUtils.isDutchTimeZone(user.getTimeZone())) {
			TweetParser.getInstance().parse(status).save();
     	}
 	}
 }
