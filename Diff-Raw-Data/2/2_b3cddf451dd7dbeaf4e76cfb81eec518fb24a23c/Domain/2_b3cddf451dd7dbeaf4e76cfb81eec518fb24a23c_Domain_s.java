 import java.util.Vector;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Domain {
 	Date date = new Date();
 
 	int crawlDelay = 0;
 	String userAgent = "*";
 	Vector<String> disallowed = new Vector<String>();
 	long lastAccess;
 	Pattern p = Pattern
 			.compile("(Disallow|Crawl-delay|User-Agent):\\s*([^#\n]*)\\s*");
 
 	// <field>:<optionalspace><value><optionalspace>
 	public Domain(String robotsFile) {
 		Matcher m = p.matcher(robotsFile);
 		String field;
 		String value;
 
 		while (m.find()) {
 			field = m.group(1);
 			value = m.group(2);
 
 			if (field.equalsIgnoreCase("disallow"))
 				disallowed.add(value);
 			else if (field.equalsIgnoreCase("crawl-delay")) {
 				String clean;
 				crawlDelay = Integer.valueOf(value.trim());
 			} else if (field.equalsIgnoreCase("user-agent")) {
 				userAgent = value;
 			}
 		}
 	}
 
 	public String toString(){
 		String outStr = "User-Agent: " + userAgent + "\n" 
 		+ "Crawl-Delay: " + crawlDelay;
 		
 		for (String s : disallowed)
 			outStr += "\nDisallowed: "+s;
 		return outStr;
 	}
 
 	public void setLastAccessTime() {
 		lastAccess = date.getTime();
 	}
 
 	public long checkTimeRemaining() {
		if (crawlDelay > 0)
 			return 0;
 		return crawlDelay - ((date.getTime() - lastAccess) / 1000);
 	}
 }
