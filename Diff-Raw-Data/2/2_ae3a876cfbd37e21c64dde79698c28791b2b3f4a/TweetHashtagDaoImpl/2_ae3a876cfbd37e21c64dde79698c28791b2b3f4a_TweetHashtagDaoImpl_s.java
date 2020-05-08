 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.Types;
 import java.util.List;
 
 import toctep.skynet.backend.dal.dao.TweetHashtagDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.hashtag.Hashtag;
 import toctep.skynet.backend.dal.domain.tweet.Tweet;
 import toctep.skynet.backend.dal.domain.tweet.TweetHashtag;
 
 public class TweetHashtagDaoImpl extends TweetHashtagDao {
 
 	@Override
 	public void insert(Domain<Integer> domain) {
 		TweetHashtag tweetHashtag = (TweetHashtag) domain;
 		
		String query = "INSERT INTO " + tableName + "(tweet_id, hastag_id) VALUES(?, ?)";
 		
 		Param[] params = new Param[] {
 			new Param(tweetHashtag.getTweet().getId(), Types.BIGINT),
 			new Param(tweetHashtag.getHashtag().getId(), Types.BIGINT)
 		};
 			
 		int id = MySqlUtil.getInstance().insert(query, params);
 		
 		tweetHashtag.setId(id);
 	}
 
 	@Override
 	public TweetHashtag select(Integer id) {
 		TweetHashtag tweetHashtag = new TweetHashtag();
 		
 		String query = "SELECT * FROM " + tableName + " WHERE id=?";
 		
 		Param[] params = new Param[] {
 			new Param(id, Types.BIGINT)
 		};
 		
 		List<Object> record = MySqlUtil.getInstance().select(query, params);
 		
 		tweetHashtag.setId(id);
 		tweetHashtag.setTweet(Tweet.select((Long) record.get(1)));
 		tweetHashtag.setHashtag(Hashtag.select((Integer) record.get(3)));
 		
 		return tweetHashtag;
 	}
 
 	@Override
 	public void update(Domain<Integer> domain) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void delete(Domain<Integer> domain) {
 		TweetHashtag tweetHashtag = (TweetHashtag) domain;	
 		MySqlUtil.getInstance().delete("DELETE FROM " + tableName + " WHERE id = " + tweetHashtag.getId());
 	}
 
 	@Override
 	public boolean exists(Domain<Integer> domain) {
 		TweetHashtag tweetHashtag = (TweetHashtag) domain;
 		return this.exists(tweetHashtag.getId());
 	}
 	
 	@Override
 	public boolean exists(Integer id) {
 		return MySqlUtil.getInstance().exists(tableName, "id=" + id);
 	}
 
 	@Override
 	public int count() {
 		return MySqlUtil.getInstance().count(tableName);
 	}
 
 }
