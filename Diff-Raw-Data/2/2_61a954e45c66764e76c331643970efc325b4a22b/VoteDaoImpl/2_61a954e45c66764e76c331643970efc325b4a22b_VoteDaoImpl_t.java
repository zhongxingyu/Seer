 package org.mashupmedia.dao;
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.mashupmedia.model.vote.DislikeVote;
 import org.mashupmedia.model.vote.LikeVote;
 import org.mashupmedia.model.vote.Vote;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class VoteDaoImpl extends BaseDaoImpl implements VoteDao {
 
 	@Override
 	public Vote getLatestVote(long userId, long mediaItemId) {
 		Query query = sessionFactory.getCurrentSession().createQuery(
 				"from Vote where user.id = :userId and mediaItem.id = :mediaItemId "
						+ "and createdOn = (select max(tv.createdOn) from Vote tv where tv.user.id = :userId and tv.mediaItem.id = mediaItem.id)");
 		query.setLong("userId", userId);
 		query.setLong("mediaItemId", mediaItemId);
 		query.setCacheable(true);
 		Vote vote = (Vote) query.uniqueResult();
 		return vote;
 
 	}
 
 	@Override
 	public void saveDislikeVote(DislikeVote dislikeVote) {
 		saveOrUpdate(dislikeVote);
 
 	}
 
 	@Override
 	public void saveLikeVote(LikeVote likeVote) {
 		saveOrUpdate(likeVote);
 	}
 
 	@Override
 	public void deleteVote(Vote vote) {
 		sessionFactory.getCurrentSession().delete(vote);
 	}
 
 	@Override
 	public List<Vote> getVotesForMediaItem(long mediaItemId) {
 		Query query = sessionFactory.getCurrentSession().createQuery("from Vote where mediaItem.id = :mediaItem");
 		query.setLong("mediaItemId", mediaItemId);
 		query.setCacheable(true);
 		@SuppressWarnings("unchecked")
 		List<Vote> votes = query.list();
 		return votes;
 	}
 
 }
