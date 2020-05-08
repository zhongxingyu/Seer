 package org.atlasapi.remotesite.bbc;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class BbcFeeds {
 
 	private static final String SLASH_PROGRAMMES_BASE = "http://www.bbc.co.uk/programmes/";
 	
	private static final Pattern PID_FINDER = Pattern.compile("([bp]\\d\\d[a-z0-9]+)");
 	
 	public static String pidFrom(String uri) {
 		Matcher matcher = PID_FINDER.matcher(uri);
 		if (matcher.find()) {
 			return matcher.group(1);
 		}
 		return null;
 	}
 
 	public static String slashProgrammesUriForPid(String pid) {
 		return SLASH_PROGRAMMES_BASE + pid;
 	}
 	
 }
