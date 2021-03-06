 package org.bazhenov.logging.web.tags;
 
 import com.farpost.timepoint.Date;
 import org.bazhenov.logging.AggregatedLogEntry;
 import org.bazhenov.logging.Cause;
 
 import static java.lang.Math.abs;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class EntryTag {
 
 	public static final int MAX_LENGTH = 80;
 	private AggregatedLogEntry entry;
 	public static DateFormat shortFormat = new DateTimeFormat();
 	public static DateFormat fullFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm:ss zz");
 	public static final String JIRA_LINK_FORMAT = "http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa?pid=10000&issuetype=1&summary={0}&description={1}&priority=3";
 
 	public static String formatCause(Cause rootCause) {
 		if ( rootCause == null ) {
 			return "";
 		}
 		StringBuilder prefix = new StringBuilder();
 		StringBuilder stackTrace = new StringBuilder();
 
 		if ( rootCause != null ) {
 			Cause cause = rootCause;
 			while ( cause != null ) {
 				if ( cause != rootCause ) {
 					stackTrace.append("\n\n").append(prefix).append("Caused by ");
 				}
 				String iStack = cause.getStackTrace().replaceAll("\n", "\n" + prefix);
 				stackTrace.append(cause.getType())
 					.append(": ")
 					.append(cause.getMessage())
 					.append("\n")
 					.append(prefix)
 					.append(iStack);
 				cause = cause.getCause();
 				prefix.append("  ");
 			}
 		}
 		return stackTrace.toString();
 	}
 
 	public static Cause rootCause(Cause cause) {
 		if ( cause == null ) {
 			return null;
 		}
 		while ( cause.getCause() != null ) {
 			cause = cause.getCause();
 		}
 		return cause;
 	}
 
 	public static String trim(String string, int limit) {
 		if ( string.length() > limit ) {
			return string.substring(0, limit) + "...";
 		}
 		return string;
 	}
 
 	public static String pluralize(int number, String titles) {
 		int abs = abs(number);
 		int[] cases = new int[]{2, 0, 1, 1, 1, 2};
 		String[] strings = titles.split(" ");
 		String result = strings[(abs % 100 > 4 && abs % 100 < 20)
 			? 2
 			: cases[Math.min(abs % 10, 5)]];
 		return number + " " + result;
 	}
 
 	public static int thousands(int number) {
 		return number/1000;
 	}
 
 	public static int magnitude(int number) {
 		return (int) Math.log10(number);
 	}
 
 	public static java.util.Date date(Date date) {
 		return date.asDate();
 	}
 }
