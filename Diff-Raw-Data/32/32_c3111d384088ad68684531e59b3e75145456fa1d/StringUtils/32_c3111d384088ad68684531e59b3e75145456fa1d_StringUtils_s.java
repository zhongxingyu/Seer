 package harris.GiantBomb;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StringUtils {
 	
 	public static List<String> removeEmbeds(String in) {
 		List<String> results = new ArrayList<String>();
 		
 		Pattern youtubePattern = Pattern.compile("<div[^>]+?rel=\"video\".*?>.+?src=\"(http://www.youtube.com.+?)\".+?div>", Pattern.DOTALL);
 		Matcher youtubeMatcher = youtubePattern.matcher(in);
 		
 		while (youtubeMatcher.find()) {
 			results.add(youtubeMatcher.group(1));
 		}
 		
		Pattern gbPattern = Pattern.compile("<div[^>]+?rel=\"(video|embed)\".*?>.+?paramsURI=(http%3A//www.giantbomb.com.+?)\".+?div>", Pattern.DOTALL);
 		Matcher gbMatcher = gbPattern.matcher(youtubeMatcher.replaceAll("<i>Video removed, open Menu to view</i>"));
 		
 		while (gbMatcher.find()) {
			results.add(gbMatcher.group(2).replace("%3A", ":"));
 		}
 		
 		results.add(0, gbMatcher.replaceAll("<i>Video removed, open Menu to view</i>"));		
 		
 		return results;
 		
 	}
 
 }
