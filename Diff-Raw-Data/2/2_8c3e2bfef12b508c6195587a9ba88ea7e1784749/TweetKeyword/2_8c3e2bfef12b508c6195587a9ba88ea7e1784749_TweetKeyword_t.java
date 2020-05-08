 package toctep.skynet.backend.dal.domain;
 
public class TweetKeyword extends DomainLongPk {
 	
 	private int tweetId;
 	private String tweetKeywordValue;
 	private int keywordId;
 	
 	public int getTweetId() {
 		return tweetId;
 	}
 	public void setTweetId(int tweetId) {
 		this.tweetId = tweetId;
 	}
 	
 	public String getTweetKeywordValue() {
 		return tweetKeywordValue;
 	}
 	public void setTweetKeywordValue(String tweetKeywordValue) {
 		this.tweetKeywordValue = tweetKeywordValue;
 	}
 	
 	public int getKeywordId() {
 		return keywordId;
 	}
 	public void setKeywordId(int keywordId) {
 		this.keywordId = keywordId;
 	}
 
 }
