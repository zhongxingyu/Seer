 package toctep.skynet.backend.test.dal;
 
 import java.sql.Timestamp;
 
 import toctep.skynet.backend.dal.domain.geo.IGeo;
 import toctep.skynet.backend.dal.domain.geo.NullGeo;
 import toctep.skynet.backend.dal.domain.place.IPlace;
 import toctep.skynet.backend.dal.domain.place.NullPlace;
 import toctep.skynet.backend.dal.domain.tweet.ISourceType;
 import toctep.skynet.backend.dal.domain.tweet.ITweet;
 import toctep.skynet.backend.dal.domain.tweet.NullSourceType;
 import toctep.skynet.backend.dal.domain.tweet.NullTweet;
 import toctep.skynet.backend.dal.domain.tweet.Tweet;
 import toctep.skynet.backend.dal.domain.user.IUser;
 import toctep.skynet.backend.dal.domain.user.NullUser;
 
 public class TweetTest extends DomainTest {
 
 	private Tweet tweet;
 	
 	private long id;
 	private String text;
 	private IGeo geo;
 	private boolean truncated;
 	private long twitterId;
 	private ISourceType sourceType;
 	private boolean favorited;
 	private ITweet inReplyToTweetTwitter;
 	private IUser inReplyToUserTwitter;
 	private long retweetCount;
 	private Timestamp createdAt;
 	private IPlace place;
 	private IUser user;
 	private String coordinates;
 	
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		tweet = new Tweet();
 		
 		id = 0L;
 		tweet.setId(id);
 		
 		text = "test";
 		tweet.setText(text);
 		
		geo = NullGeo.getInstance();
 		tweet.setGeo(geo);
 		
 		truncated = false;
 		tweet.setTruncated(truncated);
 		
 		twitterId = 0L;
 		tweet.setTwitterId(twitterId);
 		
 		sourceType = new NullSourceType();
 		tweet.setSourceType(sourceType);
 		
 		favorited = false;
 		tweet.setFavorited(favorited);
 		
 		inReplyToTweetTwitter = NullTweet.getInstance();
 		tweet.setInReplyToTweetTwitter(inReplyToTweetTwitter);
 		
 		inReplyToUserTwitter = new NullUser();
 		tweet.setInReplyToUserTwitter(inReplyToUserTwitter);
 		
 		retweetCount = 0L;
 		tweet.setRetweetCount(retweetCount);
 		
 		createdAt = new Timestamp(0);
 		tweet.setCreatedAt(createdAt);
 				
 		place = new NullPlace();
 		tweet.setPlace(place);
 		
 		user = new NullUser();
 		tweet.setUser(user);
 		
 		coordinates = "test";
 		tweet.setCoordinates(coordinates);
 	}
 	
 	@Override
 	public void testCreate() {
 		assertNotNull(tweet);
 		assertTrue(new Long(id).equals(tweet.getId()));
 		assertTrue(text.equals(tweet.getText()));
 		assertTrue(geo.equals(tweet.getGeo()));
 		assertTrue(truncated == tweet.isTruncated());
 		assertEquals(twitterId, tweet.getTwitterId());
 		assertTrue(sourceType.equals(tweet.getSourceType()));
 		assertTrue(favorited == tweet.isFavorited());
 		assertEquals(inReplyToTweetTwitter, tweet.getInReplyToTweetTwitter());
 		assertEquals(inReplyToUserTwitter, tweet.getInReplyToUserTwitter());
 		assertEquals(retweetCount, tweet.getRetweetCount());
 		assertTrue(createdAt.equals(tweet.getCreatedAt()));
 		assertTrue(place.equals(tweet.getPlace()));
 		assertTrue(user.equals(tweet.getUser()));
 		assertTrue(coordinates.equals(tweet.getCoordinates()));
 	}
 	
 	@Override
 	public void testInsert() {
 		tweet.save();
 		assertEquals(1, tweetDao.count());
 	}
 	
 	@Override
 	public void testSelect() {
 		tweet.save();
 		
 		Tweet postTweet = (Tweet) tweetDao.select(tweet.getId());
 		
 		assertTrue(postTweet.getText().equals(tweet.getText()));
 		assertEquals(postTweet.getGeo().getId(), tweet.getGeo().getId());
 		assertTrue(postTweet.isTruncated() == tweet.isTruncated());
 		assertEquals(postTweet.getTwitterId(), tweet.getTwitterId());
 		assertEquals(postTweet.getSourceType().getId(), tweet.getSourceType().getId());
 		assertTrue(postTweet.isFavorited() == tweet.isFavorited());
 		assertTrue(postTweet.getInReplyToTweetTwitter().equals(tweet.getInReplyToTweetTwitter()));
 		assertTrue(postTweet.getInReplyToUserTwitter().equals(tweet.getInReplyToUserTwitter()));
 		assertEquals(postTweet.getRetweetCount(), tweet.getRetweetCount());
 		assertTrue(postTweet.getCreatedAt().equals(tweet.getCreatedAt()));
 		assertTrue(postTweet.getPlace().getId().equals(tweet.getPlace().getId()));
 		assertEquals(postTweet.getUser().getId(), tweet.getUser().getId());
 		assertTrue(postTweet.getCoordinates().equals(tweet.getCoordinates()));
 	}
 	
 	@Override
 	public void testUpdate() {
 		// TODO
 	}
 	
 	@Override
 	public void testDelete() {
 		tweet.save();
 		assertEquals(1, tweetDao.count());
 		tweet.delete();
 		assertEquals(0, tweetDao.count());
 	}
 
 }
