 package uk.frequency.glance.server.data_access;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Query;
 
 import uk.frequency.glance.server.model.user.EventGenerationInfo;
 import uk.frequency.glance.server.model.user.Friendship;
 import uk.frequency.glance.server.model.user.User;
 
 @SuppressWarnings("unchecked")
 public class UserDAL extends GenericDAL<User>{
 	
 	public EventGenerationInfo makePersistent(EventGenerationInfo entity) {
     	getSession().saveOrUpdate(entity);
         return entity;
     }
 	
 	//FIXME temporary workaround
 	public EventGenerationInfo merge(EventGenerationInfo entity) {
     	getSession().merge(entity);
         return entity;
     }
 	
 	public Friendship makePersistent(Friendship entity) {
 		
 		{//TODO use @PrePersist in the GenericEntity instead
 	    	if(entity.getCreationTime() == null){
 	    		entity.setCreationTime(new Date());
 	    	}
 	    	entity.setUpdateTime(new Date());
	    	entity.setDeleted(false);
     	}
 		
     	getSession().saveOrUpdate(entity);
         return entity;
     }
 	
 	public List<Friendship> findFriendships(User user) {
 		Query q = getSession().createQuery("from Friendship f where " +
 				"f.user = :user")
 			.setParameter("user", user);
         return (List<Friendship>)q.list();
     }
 	
 	public Friendship findFriendship(User user, User friend) {
 		Query q = getSession().createQuery("from Friendship f where " +
 				"f.user = :user " +
 				"and f.friend = :friend")
 			.setParameter("user", user)
 			.setParameter("friend", friend);
         return (Friendship)q.uniqueResult();
     }
 	
 	public Friendship findReciprocal(Friendship friendship){
         return findFriendship(friendship.getFriend(), friendship.getUser());
 	}
 	
 	public List<Long> findFriendsIds(User user, Friendship.Status status){
 		Query q = getSession().createQuery("select f.friend.id from Friendship f where " +
 				"f.user = :user " +
 				"and f.status = " + status.ordinal() + " " +
 				"order by f.friend.id")
 			.setParameter("user", user);
         return (List<Long>)q.list(); 
 	}
 	
 }
