 package cubrikproject.tud.likelines.util;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 /**
  * A class representing a YouTube comment
  * 
  * @author R. Vliegendhart
  */
 public class YouTubeComment {
 	
 	/** Regex for finding deep-links (should be updated to cope with the hour format) */
 	public final static Pattern RE_YOUTUBE_DEEPLINK = Pattern.compile("(?<!\\d|:)(\\d{1,2}:\\d{2}|\\d{1,2}m\\d{1,2}s)(?!\\d|:)");
 	
 	/** Message content */
 	public final String content;
 	
 	/** Message id */
 	public final String id;
 	
 	/** Deep-links */
 	public final List<? extends TimePoint> deeplinks;
 	
 	/**
 	 * Constructs a YouTube comment object
 	 * 
 	 * @param content The text the user wrote
 	 * @param id The comment ID
 	 * @param deeplinks A list of deep-links, if any
 	 */
 	private YouTubeComment(String content, String id, List<? extends TimePoint> deeplinks) {
 		this.content = content;
 		this.id = id;
 		this.deeplinks = deeplinks;
 	}
 	
 	/** Limit the number of requests for retrieving comments */
 	private static int MAX_REQUESTS = 4;
 	
 	/**
 	 * Returns the URL for retrieving comments of a given video
 	 * @param videoId The ID of the video
 	 * @return Comments URL for the given video
 	 */
 	private static String getFirstCommentsPage(String videoId) {
 		return "http://gdata.youtube.com/feeds/api/videos/" + videoId + "/comments?v=2&alt=json";
 	}
 	
 	/**
 	 * Retrieves deep-link comments for a given video.
 	 * It looks for these comments in the N=25*MAX_REQUESTS most recent comments of the video.
 	 * 
 	 * @param videoId The video ID to retrieve the deep-link comments for
 	 * @return A list of found deep-link comments
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public static List<YouTubeComment> retrieveDeepLinkComments(String videoId) throws MalformedURLException, IOException {
 		final List<YouTubeComment> res = new ArrayList<YouTubeComment>();
 		String currentUrl = getFirstCommentsPage(videoId);
 		
 		int requests = 0;
 		while (requests++ < MAX_REQUESTS && currentUrl != null) {
 			JsonObject doc = Ajax.getJSON(currentUrl).getAsJsonObject();
 			JsonObject feed = doc.get("feed").getAsJsonObject();
 			
 			String nextUrl = null;
 			for (JsonElement linkElement : feed.get("link").getAsJsonArray()) {
 				JsonObject link = linkElement.getAsJsonObject();
 				if (link.get("rel").equals("next")) {
 					nextUrl = link.get("href").getAsString();
 				}
 			}
 			
 			for (JsonElement entryElement : feed.get("entry").getAsJsonArray()) {
 				JsonObject entry = entryElement.getAsJsonObject();
 				
 				String ids = entry.get("id").getAsJsonObject().get("$t").getAsString();
 				String[] idsParts = ids.split(":");
 				int i = Arrays.binarySearch(idsParts, "comment");
 				final String commentId = idsParts[i+1];
 				
 				final String content = entry.get("content").getAsJsonObject().get("$t").getAsString();
 				List<TimePoint> deeplinks = extractTimePoints(content);
 				if (deeplinks.size() > 0)
 					res.add(new YouTubeComment(content, commentId, deeplinks));
 			}
 			
 			currentUrl = nextUrl;
 		}
 		return res;
 	}
 	
 	
 	/**
 	 * Extracts time-codes from a comment text.
 	 * 
 	 * @param text Comment text
 	 * @return List of time-codes found within the comment text
 	 */
 	public static List<TimePoint> extractTimePoints(String text) {
 		final List<TimePoint> timePoints = new ArrayList<TimePoint>();
 		
 		final Matcher matcher = RE_YOUTUBE_DEEPLINK.matcher(text);
 		
 		while (matcher.find()) {
 			TimePoint tp = new TimePoint(matcher.group());
 			timePoints.add(tp);
 		}
 			
 		return timePoints;
 	}
 	
 	/**	A simple data class to represent time-points in comments */
 	public static class TimePoint {
 		/** Regex for extracting ints */
 		private static Pattern RE_INT = Pattern.compile("\\d+");
 		
 		/** This time-point in seconds */
 		public final int inSeconds;
 		/** This time-point as it originally appeared in the comment */
 		public final String asText;
 		
 		/**
 		 * Constructs a time-point object.
 		 * 
 		 * @param timepoint The time-point as it appeared in a comment
 		 */
 		public TimePoint(String timepoint) {
 			asText = timepoint;
 			
 			final Matcher matcher = RE_INT.matcher(timepoint);
 			int s = 0;
 			while (matcher.find())
 				s = 60*s + Integer.parseInt(matcher.group(), 10);
 			inSeconds = s;
 		}
 		
 		@Override
 		public String toString() {
 			return "TimePoint<" + asText + ">";
 		}
 	}
 }
