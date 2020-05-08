 package toctep.skynet.backend.bll;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import toctep.skynet.backend.dal.domain.keyword.Keyword;
 import toctep.skynet.backend.dal.domain.tweet.Tweet;
 
 public class TweetIndexer {
 	
 	public List<Keyword> indexTweetKeywords(Tweet tweet) {
 		List<Keyword> tweetKeywords = new ArrayList<Keyword>();
 		List<String> keywordStrings = TweetSplitter.splitTweet(tweet.getText());
 		TweetFilter.filterTweet(keywordStrings);
 		
     	for(String keywordString: keywordStrings) {
    		Keyword keyword = new Keyword();
    		keyword.setKeyword(keywordString);
    		tweetKeywords.add(keyword);
     	}
     	
     	return tweetKeywords;
     }
     
 }
