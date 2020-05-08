 package com.avricot.prediction.repository.tweet;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.avricot.prediction.model.tweet.Tweet;
 
 public class TweetRepositoryImpl implements TweetRepositoryCustom {
 	
 	@Inject
 	private MongoTemplate mongoTemplate;
 
 	@Override
 	public List<Tweet> getTweetNotChecked(int size) {
 		Query query = new Query();
		query.addCriteria(Criteria.where("checked").is(true));
 		query.limit(size);
         return mongoTemplate.find(query, Tweet.class);
 	}
 
 }
