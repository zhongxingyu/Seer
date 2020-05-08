 package toctep.skynet.backend.bll;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import toctep.skynet.backend.Skynet;
 import twitter4j.Status;
 
 import com.cybozu.labs.langdetect.Detector;
 import com.cybozu.labs.langdetect.DetectorFactory;
 import com.cybozu.labs.langdetect.LangDetectException;
 
 public class UserBioTweetRetriever extends TweetRetriever {
 	
 	public static final String LANGDETECT_PROFILE_DIR = "lib/langdetect/profiles";
 	
 	public UserBioTweetRetriever() {
 		initialize();
 	}
 	
	@Override
	protected void initialize() {
		super.initialize();
		
 		try {
 			DetectorFactory.loadProfile(LANGDETECT_PROFILE_DIR);
 		} catch (LangDetectException e) {
 			Skynet.LOG.error(e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public void run() {
 		getTwitterStream().sample();
 	}
 	
 	@Override
 	public boolean isDutch(Status status) {
 		Detector detector = null;
 		String lang = null;
 		
 		try {
 			detector = DetectorFactory.create();
 			detector.append(status.getText());
 
 			lang = detector.detect();
 		} catch (LangDetectException e) {
 			Skynet.LOG.error(e.getMessage(), e);
 		}
 		
 		twitter4j.User user = status.getUser();
 		
 		if (isDutchLanguage(user, lang) || isDutchLocation(user) || isDutchTimeZone(user)) {
 			return true;
     	}
 		
 		return false;
 	}
 	
 	private boolean isDutchLocation(twitter4j.User user) {
 		List<String> locations = new ArrayList<String>();
 		locations.add("netherland");
 		locations.add("netherlands");
 		locations.add("nederland");
 		locations.add("holland");
 		
 		if (locations.contains(user.getLocation().toLowerCase())) {
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean isDutchLanguage(twitter4j.User user, String lang) {
 		if (user.getLang().equalsIgnoreCase("nl")) {
 			return true;
 		}
 		if(lang.equalsIgnoreCase("nl")) {
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean isDutchTimeZone(twitter4j.User user) {
 		if (user.getTimeZone().equalsIgnoreCase("amsterdam")) {
 			return true;
 		}
 		return false;
 	}
 
 }
