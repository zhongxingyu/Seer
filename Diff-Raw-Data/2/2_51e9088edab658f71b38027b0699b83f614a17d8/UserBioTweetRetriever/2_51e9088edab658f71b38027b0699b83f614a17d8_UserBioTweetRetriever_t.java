 package toctep.skynet.backend.bll;
 
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
 		
 		if (user.getLang().equalsIgnoreCase("nl")
 				|| user.getTimeZone().equalsIgnoreCase("amsterdam")
 				|| user.getLocation().toLowerCase().contains("netherland")
 				|| user.getLocation().toLowerCase().contains("nederland")
 				|| user.getLocation().toLowerCase().contains("holland")
 				|| lang.equalsIgnoreCase("nl")) {
 			return true;
     	}
 		
 		return false;
 	}
 
 }
