 package mad3.muxie.feed;
 
 public class RSSWordpress extends RSS {
 
 	@Override
 	protected String toUri(String uid) {
 		if (uid != null) {
			return "http://" + uid + "/?feed=rss2";
 		}
 		return null;
 	}
 }
