 package toctep.skynet.backend.bll;
 
 import toctep.skynet.backend.dal.domain.Keyword;
 import toctep.skynet.backend.dal.domain.Tweet;
 import toctep.skynet.backend.dal.domain.TweetKeyword;
 
 public class TweetIndexer {
 	
 	public void indexTweetKeywords(Tweet tweet) {
     	String filteredTweetBody = TweetFilter.filterTweet(tweet);
     	String[] keywordStrings = TweetSplitter.splitTweet(filteredTweetBody);
     	
     	for(String keywordString: keywordStrings) {
     		Keyword keyword = getKeyword(keywordString);
     		saveTweetKeyword(keywordString, keyword, tweet);
     	}
     }
     
     private Keyword getKeyword(String keywordString) {
     	Keyword keyword = new Keyword();
     	keyword.setKeyword(keywordString.toLowerCase());
     	keyword.save();
     	
     	return keyword;
     }
     
     private void saveTweetKeyword(String keywordString, Keyword keyword, Tweet tweet) {
     	TweetKeyword tweetKeyword = new TweetKeyword();
     	    	
     	tweetKeyword.setKeyword(keyword);
     	tweetKeyword.setTweet(tweet);
     	tweetKeyword.setTweetKeywordValue(keywordString);
    	tweetKeyword.save();
     }
 
 }
