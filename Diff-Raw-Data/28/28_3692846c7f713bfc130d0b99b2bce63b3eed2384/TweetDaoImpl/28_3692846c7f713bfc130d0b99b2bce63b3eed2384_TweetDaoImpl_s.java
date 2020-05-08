 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import toctep.skynet.backend.dal.dao.TweetDao;
 import toctep.skynet.backend.dal.domain.Domain;
import toctep.skynet.backend.dal.domain.Place;
 import toctep.skynet.backend.dal.domain.Tweet;
 
 public class TweetDaoImpl extends TweetDao {
 	
 	@Override
 	public void insert(Domain domain) {
 		Tweet tweet = (Tweet) domain;
 		
 		String query = "INSERT INTO " + tableName +	"VALUES ("
 		+ tweet.getId() + ", '"
 		+ MySqlUtil.escape(tweet.getText()) + ", '"
 		+ tweet.getGeo().getId() + ", '"
 		+ tweet.isTruncated() + ", '"
 		+ tweet.getSourceType().getId() + ", '"
 		+ tweet.isFavorited() + ", '"
 		+ tweet.getInReplyToTweetTwitterId() + ", '"
 		+ tweet.getInReplyToUserTwitterId() + ", '"
 		+ tweet.getRetweetCount() + ", '"
 		+ MySqlUtil.escape(tweet.getCreatedAt().toString()) + ", '"
 		+ MySqlUtil.escape(tweet.getPlace().getId()) + ", '"
 		+ tweet.getUser() + ", '"
 		+ MySqlUtil.escape(tweet.getCoordinates()) + "')";
 		
 		MySqlUtil.getInstance().insert(query);
 	}
 
 	@Override
 	public Tweet select(long id) {
 		Tweet tweet = new Tweet();
 		
 		ResultSet rs = MySqlUtil.getInstance().select("SELECT id, text FROM " + tableName + " WHERE id = " + id);
 		
 		tweet.setId(id);
 		
 		try {
 			tweet.setText(rs.getString("text"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return tweet;
 	}
 
 	@Override
 	public void update(Domain domain) {
 		// TODO
 	}
 	
 	@Override
 	public void delete(Domain domain) {
 		Tweet tweet = (Tweet) domain;
 		MySqlUtil.getInstance().delete("DELETE FROM " + tableName + " WHERE id = " + tweet.getId());	
 	}
 
 	@Override
 	public boolean exists(Domain domain) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int count() {
 		return MySqlUtil.getInstance().count(tableName);
 	}
 
 }
