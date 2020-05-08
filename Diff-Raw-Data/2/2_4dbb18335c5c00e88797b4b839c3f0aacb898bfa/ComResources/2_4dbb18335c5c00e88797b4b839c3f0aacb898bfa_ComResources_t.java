 package comm;
 
 import db.Resources;
 
 public class ComResources extends Resources
 {
 	public enum CommType {
		EMAIL, BUGZILLA, JIRA, ISSUE, GITHUB, FORUM
 	}
 	
 	public static int DB_LIMIT = 2000;
 }
