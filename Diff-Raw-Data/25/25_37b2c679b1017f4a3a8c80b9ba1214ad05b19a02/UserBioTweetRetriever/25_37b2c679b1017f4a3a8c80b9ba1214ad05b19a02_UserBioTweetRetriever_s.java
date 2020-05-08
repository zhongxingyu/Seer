 package toctep.skynet.backend.bll;
 
 import toctep.skynet.backend.Skynet;
 import twitter4j.Status;
 
 import com.cybozu.labs.langdetect.Detector;
 import com.cybozu.labs.langdetect.DetectorFactory;
 import com.cybozu.labs.langdetect.LangDetectException;
 
 public class UserBioTweetRetriever extends TweetRetriever {
 	
 	public static final String LANGDETECT_PROFILE_DIR = "lib/langdetect/profiles";
 	
 	@Override
 	public void initialize() {
 		super.initialize();
 		
 		try {
 			DetectorFactory.loadProfile(LANGDETECT_PROFILE_DIR);
 		} catch (LangDetectException e) {
 			Skynet.LOG.error(e.getMessage(), e);
 		}
 	}
 	
 	@Override
 	public void run() {
		twitterStream.sample();
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
 		
 		if (status.getUser().getLang().equalsIgnoreCase("nl")
 				|| status.getUser().getTimeZone().equalsIgnoreCase("amsterdam")
 				|| status.getUser().getLocation().toLowerCase().contains("netherland")
 				|| status.getUser().getLocation().toLowerCase().contains("nederland")
 				|| status.getUser().getLocation().toLowerCase().contains("holland")
 				|| lang.equalsIgnoreCase("nl")) {
 			return true;
     	}
 		
 		return false;
 	}
 
 }
