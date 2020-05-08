 package jbeer.dev.twittermonitor.data.dao.impl;
 
 import static jbeer.dev.twittermonitor.ConnectionInfo.PAGE_NUMBER;
 import static jbeer.dev.twittermonitor.ConnectionInfo.PAGE_PARAM;
 import static jbeer.dev.twittermonitor.ConnectionInfo.QUERY_PARAM;
 import static jbeer.dev.twittermonitor.ConnectionInfo.RESULT_TYPE_PARAM;
 import static jbeer.dev.twittermonitor.ConnectionInfo.SEARCH_PARAM_DEFAULT;
 import static jbeer.dev.twittermonitor.ConnectionInfo.SEARCH_TWEETS_URL;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 
 import android.content.Context;
import android.util.Log;
 
 import jbeer.dev.twittermonitor.data.dao.TweetDao;
 import jbeer.dev.twittermonitor.data.domain.Tweet;
 import jbeer.dev.twittermonitor.data.domain.TweetMonitor;
 import jbeer.dev.twittermonitor.parser.json.TweetsParser;
 import jbeer.dev.twittermonitor.service.impl.WebServiceImpl;
 
 public class TweetDaoWebImpl implements TweetDao{
 
 	private static String nextPageInfo;
 	
 	@Override
 	public List<Tweet> retrieveRecentTweets() {
 		
 		WebServiceImpl webservice = new WebServiceImpl(SEARCH_TWEETS_URL);
 		Map<String,String> params = new HashMap<String, String>();
 		params.put(QUERY_PARAM, SEARCH_PARAM_DEFAULT);
 		params.put(RESULT_TYPE_PARAM, "recent");
 		params.put(PAGE_PARAM, String.valueOf(PAGE_NUMBER));
 		String webResult = webservice.webGet(params);
 		
 		TweetMonitor tweetMonitor = new TweetMonitor();
 		TweetsParser tweetParser = new TweetsParser();
		
		Log.e("RESULT","RESULT "+webResult);
		
 		try {
 			tweetMonitor = tweetParser.parse(webResult);
 			nextPageInfo = tweetMonitor.getNextPage();
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		return tweetMonitor.getTweetList();
 	}
 
 }
