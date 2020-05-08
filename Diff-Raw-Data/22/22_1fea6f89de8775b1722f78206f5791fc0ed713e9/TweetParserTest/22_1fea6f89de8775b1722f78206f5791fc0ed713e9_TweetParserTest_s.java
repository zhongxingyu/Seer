 package toctep.skynet.backend.test.bll;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
import java.sql.ResultSet;
import java.sql.SQLException;

 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import toctep.skynet.backend.bll.TweetIndexer;
 import toctep.skynet.backend.dal.dao.impl.mysql.MySqlUtil;
 import toctep.skynet.backend.dal.domain.tweet.Keyword;
 import toctep.skynet.backend.dal.domain.tweet.Tweet;
 
import com.mysql.jdbc.Connection;

 public class TweetParserTest {
 	
 	@Before
 	public void setup() {
		Connection conn = MySqlUtil.getInstance("mysql_test.properties").getConnection();
 	}
 
 	@Test
 	public void indexTweetKeywordsText() {
 		TweetIndexer indexer = new TweetIndexer();
 		Tweet tweet = new Tweet();
 		
 		tweet.setId(new Long(1001));
 		tweet.setText("wilfried elfstedentocht jager braam grietje horse");
 		
 		assertEquals("keyword count: ", 0, MySqlUtil.getInstance().count("twitter_keyword"));
 		
 		Keyword keyword = new Keyword();
 		keyword.setKeyword("elfstedentocht");
 		keyword.save();
 		assertEquals("keyword count: ", 1, MySqlUtil.getInstance().count("twitter_keyword"));
 		assertTrue(MySqlUtil.getInstance().exists("twitter_keyword", "keyword = 'elfstedentocht';"));
 		
 		keyword = new Keyword();
 		keyword.setKeyword("elfstedentocht");
 		keyword.save();
 		assertEquals("keyword count: ", 1, MySqlUtil.getInstance().count("twitter_keyword"));
 		
 		keyword = new Keyword();
 		keyword.setKeyword("elfSTEDENtocht");
 		keyword.save();
 		assertEquals("keyword count: ", 1, MySqlUtil.getInstance().count("twitter_keyword"));
 		
 		keyword = new Keyword();
 		keyword.setKeyword("wilfried");
 		keyword.save();
 		assertEquals("keyword count: ", 2, MySqlUtil.getInstance().count("twitter_keyword"));
 		
 		indexer.indexTweetKeywords(tweet);
 		
		assertEquals("keyword count: ", 6, MySqlUtil.getInstance().count("twitter_keyword"));
		
		long wilfried_id = keyword.getId();
		ResultSet rs = MySqlUtil.getInstance().select("SELECT * FROM twitter_tweet_keywords WHERE keyword_id=" + wilfried_id);
		try {
			while(rs.next()) {
				System.out.println(rs.getString("tweet_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
 	}
 	
 	@After
 	public void tearDown() {
 		MySqlUtil.getInstance().truncateDatabase();
 	}
 
 }
