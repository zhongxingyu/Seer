 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.tweet.Keyword;
 import toctep.skynet.backend.dal.domain.tweet.Tweet;
 import toctep.skynet.backend.dal.domain.tweet.TweetKeyword;
 
 public class TweetKeywordTest extends DomainTest {
 	private TweetKeyword tweetKeyword;
 	private Tweet tweet;
 	private String keywordValue;
 	private Keyword keyword;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		tweetKeyword = new TweetKeyword();
 		
 		tweet = new Tweet();
 		tweet.setId(new Long(0));
 		
 		keywordValue = "testValue";
 		keyword = new Keyword();
 		
 		tweetKeyword.setKeyword(keyword);
 		tweetKeyword.setTweet(tweet);
 		tweetKeyword.setTweetKeywordValue(keywordValue);
 	}
 
 	@Override
 	public void testCreate() {
 		assertNotNull(tweetKeyword);
 		
 		assertEquals("getKeywordId: ", keyword, tweetKeyword.getKeyword());
 		assertEquals("getKeywordValue: ", keywordValue, tweetKeyword.getTweetKeywordValue());
 		assertEquals("getTweetId: ", tweet, tweetKeyword.getTweet());
 	}
 
 	@Override
 	public void testInsert() {
 		tweetKeyword.save();
 		assertEquals(1, tweetKeywordDao.count());
 	}
 
 	@Override
 	public void testSelect() {
 		tweetKeyword.save();
 		
 		TweetKeyword postTweetKeyword = (TweetKeyword) tweetKeywordDao.select(tweetKeyword.getId());
 		
 		assertTrue(postTweetKeyword.getTweet().equals(tweetKeyword.getTweet()));
 		assertTrue(postTweetKeyword.getKeyword().equals(tweetKeyword.getKeyword()));
 	}
 
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void testDelete() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
