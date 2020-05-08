 package grids.service;
 
 import grids.domain.TweetOnIdComparator;
 import grids.entity.Comment;
 import grids.entity.Follow;
 import grids.entity.Tag;
 import grids.entity.Tweet;
 import grids.repository.CommentRepository;
 import grids.repository.FollowRepository;
 import grids.repository.TweetRepository;
 import grids.transfer.TweetCard;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional(readOnly=true)
 public class TweetReadService {
 	private static final int FETCH_SIZE = 20;
 	@Autowired
 	private TransferService transferService;
 	@Autowired
 	private TweetRepository tweetRepos;
 	@Autowired
 	private FollowRepository followRepos;
 	@Autowired
 	private CommentRepository commentRepos;
 	
 	public List<TweetCard> istream(long userId) {
 		//XXX consider fetch_size limit
 		List<Tweet> tweets = new ArrayList<>();
 		tweets.addAll(tweetRepos.byAuthor(userId));
 		List<Follow> followings = followRepos.followings(userId);
 		for (Follow follow : followings) {
 			tweets.addAll(tweetRepos.byAuthorAndTags(
 					follow.getTarget().getId(), follow.getTags()));
 		}
 		Collections.sort(tweets, new TweetOnIdComparator());
 		
 		List<TweetCard> tcs = new ArrayList<>();
 
 		// Select the top items, for later's higher sort
 		List<Tweet> tops = (FETCH_SIZE < tweets.size())
				? tweets.subList(0, FETCH_SIZE) : tweets;
 		for (Tweet tweet : tops) {
 			// How to optimize the counting, by session cache?
 			tcs.add(transferService.getTweetCard(tweet));
 		}
 		return tcs;
 	}
 	
 	public List<Tweet> tweetsByTags(Collection<Tag> tags) {
 		return new ArrayList<>(tweetRepos.byTags(tags));
 	}
 
 	public TweetCard getTweetCard(long tweetId) {
 		return transferService.getTweetCard(tweetRepos.get(tweetId));
 	}
 
 	public Collection<Tweet> getForwards(long originId) {
 		return new ArrayList<>(tweetRepos.byOrigin(originId));
 	}
 
 	public Collection<Comment> getComments(long sourceId) {
 		return new ArrayList<>(tweetRepos.load(sourceId).getComments());
 	}
 	
 	/**
 	 * Experimental
 	 * @return a sequential list of connected tweets
 	 */
 	public List<TweetCard> connectTweets(long blogId) {
 		List<TweetCard> tcs = new ArrayList<>();
 		for (Tweet tweet : tweetRepos.connectTweets(blogId)) {
 			tcs.add(transferService.getTweetCard(tweet));
 		}
 		return tcs;
 	}
 }
