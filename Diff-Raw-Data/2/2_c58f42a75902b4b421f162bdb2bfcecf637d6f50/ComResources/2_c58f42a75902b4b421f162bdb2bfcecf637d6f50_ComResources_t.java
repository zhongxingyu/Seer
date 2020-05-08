 package comm;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import db.Resources;
 
 public class ComResources extends Resources
 {
 	public static final boolean DEBUG = true;
 	
 	public enum TrackerProject {
 		HIBERNATE, ECLIPSE, AGILEFANT
 	}
 	
 	public static final boolean LINK_FROM_COMMIT_MSGS = true;
 	public static final Pattern SHA1_REGEX = Pattern.compile("[0-9a-f]{5,40}");
 	public static final Pattern BUG_NUMBER_REGEX = Pattern.compile("([A-Z]{2,4}-[0-9]{2,4})|(\\[([A-Z]{2,4}-[0-9]{2,4})\\])"); // TODO add cases for bugzilla
 	public static final Pattern NUMBER = Pattern.compile("([0-9]+)");
 	public static final Pattern COMMIT_KEYWORDS = Pattern.compile("fix(e[ds])?|bugs?|defects|patch");
 	public static final Pattern BUG_NUMBER_BUGZILLA_REGEX = Pattern.compile("bug[#\\s]*([0-9]+{1})");
 	public static final int COM_QUEUE_WORKER_LIMIT = 1;
 	public static final int JIRA_MAX_RESULTS = 50;
	public static String ISSUE_NUMBER_KEY = "HHH";
 	public static final int COMMIT_DATE_MAX_RANGE = 7;
 	
 	public static final float STRING_MATCHING_THRESHOLD = 0.2f;
 	public static final float PATCH_MATCH_PERCENT = 0.7f;
 }
