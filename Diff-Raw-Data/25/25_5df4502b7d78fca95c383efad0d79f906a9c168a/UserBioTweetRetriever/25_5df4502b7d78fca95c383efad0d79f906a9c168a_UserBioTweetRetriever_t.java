 package toctep.skynet.backend.bll;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import toctep.skynet.backend.log.Log;
 import twitter4j.Status;
 
 import com.cybozu.labs.langdetect.Detector;
 import com.cybozu.labs.langdetect.DetectorFactory;
 import com.cybozu.labs.langdetect.LangDetectException;
 
 public class UserBioTweetRetriever extends TweetRetriever {
 	
 	public static final String LANGDETECT_PROFILE_DIR = "lib/langdetect/profiles";
 	
 	public UserBioTweetRetriever() {
 		super();
 		
 		initialize();
 	}
 	
 	private void initialize() {
 		try {
 			DetectorFactory.loadProfile(LANGDETECT_PROFILE_DIR);
 		} catch (LangDetectException e) {
 			Log.error(e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public void run() {
 		getTwitterStream().sample();
 	}
 	
 	@Override
 	public void process(Status status) {
 		Detector detector = null;
 		String lang = null;
 		
 		try {
 			detector = DetectorFactory.create();
 			detector.append(status.getText());
 
 			lang = detector.detect();
 		} catch (LangDetectException e) {
 			Log.error(e.getMessage(), e);
 		}
 		
 		twitter4j.User user = status.getUser();
 		
 		if (isDutchLanguage(user, lang) || isDutchLocation(user) || isDutchTimeZone(user)) {
			getTweetParser().parse(status).save();
     	}
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
