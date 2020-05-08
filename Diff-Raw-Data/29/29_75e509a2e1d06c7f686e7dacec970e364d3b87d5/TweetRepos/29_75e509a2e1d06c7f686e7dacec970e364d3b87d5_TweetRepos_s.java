 package grids.repos;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Query;
 import org.springframework.stereotype.Repository;
 
 import grids.entity.Tag;
 import grids.entity.Tweet;
 
 @Repository
 public class TweetRepos extends BaseRepos<Tweet> {
 
 	public List<Tweet> tweets(long authorId) {
 		Query query = session().createQuery("from Tweet t where t.author.id=:authorId")
 				.setLong("authorId", authorId);
 		return query.list();
 	}
 	
 	public List<Tweet> tweets(long authorId, Collection<Tag> tags) {
 		List<Tweet> tweets = tweets(authorId);
 		if (tags.isEmpty()) {
 			return tweets;
 		}
 		else {
 			Collection<Tag> queryTags = getQueryTags(tags);
 			
 			Iterator<Tweet> iter = tweets.iterator();
 			while (iter.hasNext()) {
 				if (noMatch(iter.next(), queryTags)) {
 					iter.remove();
 				}
 			}
 			return tweets;
 		}
 	}
 	
 	private Set<Tag> getQueryTags(Collection<Tag> tags) {
 		Set<Tag> queryTags = new HashSet<>();
 		for (Tag tag : tags) {
 			queryTags.add(tag);
			queryTags.addAll(tag.descandants());
 		}
 		return queryTags;
 	}
 	
 	private boolean noMatch(Tweet tweet, Collection<Tag> queryTags) {
 		for (Tag queryTag : queryTags) {
 			if (tweet.getTags().contains(queryTag)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	protected Class<Tweet> getEntityClass() {
 		return Tweet.class;
 	}
 }
