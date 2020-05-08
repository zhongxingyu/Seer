 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.Types;
 import java.util.List;
 
 import toctep.skynet.backend.dal.dao.TweetKeywordDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.tweet.Keyword;
 import toctep.skynet.backend.dal.domain.tweet.Tweet;
 import toctep.skynet.backend.dal.domain.tweet.TweetKeyword;
 
 public class TweetKeywordDaoImpl extends TweetKeywordDao {
 
 	@Override
 	public void insert(Domain<Long> domain) {
 		TweetKeyword tweetKeyword = (TweetKeyword) domain;
 		
 		String query = "INSERT INTO " + tableName + "(tweet_id, value, keyword_id) VALUES(?, ?, ?)";
 		
 		Param[] params = new Param[] {
 			new Param(tweetKeyword.getTweet().getId(), Types.BIGINT),
 			new Param(tweetKeyword.getTweetKeywordValue(), Types.VARCHAR),
 			new Param(tweetKeyword.getKeyword().getId(), Types.BIGINT)
 		};
 			
 		Long id = MySqlUtil.getInstance().insert(query, params);
 		
 		tweetKeyword.setId(id);
 	}
 
 	@Override
 	public TweetKeyword select(Long id) {
 		TweetKeyword tweetKeyword = new TweetKeyword();
 		
 		String query = "SELECT * FROM " + tableName + " WHERE id=?";
 		
 		Param[] params = new Param[] {
			new Param(id, Types.BIGINT)
 		};
 		
 		List<Object> record = MySqlUtil.getInstance().select(query, params);
 		
 		tweetKeyword.setId(id);
 		tweetKeyword.setTweet(Tweet.select((Long) record.get(1)));
 		tweetKeyword.setTweetKeywordValue((String) record.get(2));
 		tweetKeyword.setKeyword(Keyword.select((Long) record.get(3)));
 		
 		return tweetKeyword;
 	}
 
 	@Override
 	public void update(Domain<Long> domain) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void delete(Domain<Long> domain) {
 		TweetKeyword tweetkeyword = (TweetKeyword) domain;	
 		MySqlUtil.getInstance().delete("DELETE FROM " + tableName + " WHERE id = " + tweetkeyword.getId());
 	}
 
 	@Override
 	public boolean exists(Domain<Long> domain) {
 		TweetKeyword tweetkeyword = (TweetKeyword) domain;
 		return this.exists(tweetkeyword.getId());
 	}
 	
 	@Override
 	public boolean exists(Long id) {
 		return MySqlUtil.getInstance().exists(tableName, "id=" + id);
 	}
 
 	@Override
 	public int count() {
 		return MySqlUtil.getInstance().count(tableName);
 	}
 
 }
