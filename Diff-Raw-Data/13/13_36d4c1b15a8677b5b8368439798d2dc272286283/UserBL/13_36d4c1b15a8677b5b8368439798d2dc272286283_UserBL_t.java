 package uk.frequency.glance.server.business;
 
 import java.util.List;
 
 import org.hibernate.TransientObjectException;
 import org.hibernate.exception.ConstraintViolationException;
 
 import uk.frequency.glance.server.business.exception.WrongStateException;
 import uk.frequency.glance.server.business.remote.Facebook;
 import uk.frequency.glance.server.data_access.UserDAL;
 import uk.frequency.glance.server.model.user.EventGenerationInfo;
 import uk.frequency.glance.server.model.user.Friendship;
 import uk.frequency.glance.server.model.user.User;
 import uk.frequency.glance.server.model.user.UserProfile;
 
 import static uk.frequency.glance.server.model.user.FriendshipStatus.*;
 
 public class UserBL extends GenericBL<User>{
 
 	UserDAL userDal;
 
 	public UserBL() {
 		super(new UserDAL());
 		userDal = (UserDAL)dal;
 	}
 	
 	public User facebookLogin(String facebookId, String accessToken){
 		User user = userDal.findByFacebookId(facebookId);
 		if(user == null){
 			user = new User();
 			user.setFacebookId(facebookId);
			user.setFbAccessToken(accessToken);
 			UserProfile profile = new Facebook(accessToken).requestUserData();
 			user.setProfile(profile);
 			return create(user);
 		}else{
 			return user;
 		}
 	}
 	
 	@Override
 	public User create(User user) throws ConstraintViolationException, TransientObjectException {
 		user = super.create(user);
 		if(user.getEventGenerationInfo() == null){
 			EventGenerationInfo gen = new EventGenerationInfo();
 			gen.setUser(user);
 			userDal.save(gen);
 			user.setEventGenerationInfo(gen);
 		}
 		return user;
 	}
 	
 	public List<Friendship> findFriendships(long userId){
 		User user = new User();
 		user.setId(userId);
 		return userDal.findFriendships(user);
 	}
 	
 
 	public List<User> findFriends(long userId){
 		User user = new User();
 		user.setId(userId);
 		return userDal.findFriends(user, ACCEPTED);
 	}
 	
 	public List<User> findFriendshipRequestsReceived(long userId){
 		User user = new User();
 		user.setId(userId);
 		return userDal.findFriends(user, REQUEST_RECEIVED);
 	}
 	
 	public Friendship createFriendshipRequest(long userId, long friendId){
 		User user = new User();
 		user.setId(userId);
 		User friend = new User();
 		friend.setId(friendId);
 		
 		if(userId == friendId){
 			throw new WrongStateException("Can't send a friendship request to yourself.");
 		}
 		Friendship existing = userDal.findFriendship(user, friend);
 		if(existing != null){
 			throw new WrongStateException("Friendship request already exists. Status is " + existing.getStatus());
 		}
 		
 		Friendship f = new Friendship();
 		f.setUser(user);
 		f.setFriend(friend);
 		f.setStatus(REQUEST_SENT);
 		userDal.saveOrUpdate(f);
 		
 		Friendship f2 = new Friendship();
 		f2.setUser(friend);
 		f2.setFriend(user);
 		f2.setStatus(REQUEST_RECEIVED);
 		userDal.saveOrUpdate(f2);
 		
 		return f;
 	}
 	
 	public Friendship acceptFriendshipRequest(long userId, long friendId){
 		User user = new User();
 		user.setId(userId);
 		User friend = new User();
 		friend.setId(friendId);
 		
 		Friendship f = userDal.findFriendship(user, friend);
 		
 		if(f == null){
 			throw new WrongStateException("Friendship request doesn't exist.");
 		}else if(f.getStatus() == REQUEST_SENT){
 			throw new WrongStateException("Friendship request can't be accepted by the same user who sent it.");
 		}else if(f.getStatus() != REQUEST_RECEIVED){
 			throw new WrongStateException("Friendship state is: " + f.getStatus() + ". Can only accept in state: " + REQUEST_RECEIVED + ".");
 		}
 		
 		f.setStatus(ACCEPTED);
 		userDal.saveOrUpdate(f);
 		
 		Friendship f2 = userDal.findReciprocal(f);
 		f2.setStatus(ACCEPTED);
 		userDal.saveOrUpdate(f2);
 		
 		return f;
 	}
 	
 	public Friendship declineFriendshipRequest(long userId, long friendId){
 		User user = new User();
 		user.setId(userId);
 		User friend = new User();
 		friend.setId(friendId);
 		
 		Friendship f = userDal.findFriendship(user, friend);
 		
 		if(f == null){
 			throw new WrongStateException("Friendship request doesn't exist.");
 		}else if(f.getStatus() == REQUEST_SENT){
 			throw new WrongStateException("Friendship request can't be denied by the same user who sent it.");
 		}else if(f.getStatus() != REQUEST_RECEIVED){
 			throw new WrongStateException("Friendship state is: " + f.getStatus() + ". Can only deny in state: " + REQUEST_RECEIVED + ".");
 		}
 		
 		f.setStatus(DECLINED);
 		userDal.saveOrUpdate(f);
 		
 		Friendship f2 = userDal.findReciprocal(f);
 		f2.setStatus(DECLINED);
 		userDal.saveOrUpdate(f2);
 		
 		return f;
 	}
 	
 	public Friendship removeFriendship(long userId, long friendId){
 		User user = new User();
 		user.setId(userId);
 		User friend = new User();
 		friend.setId(friendId);
 		
 		Friendship f = userDal.findFriendship(user, friend);
 		
 		if(f == null){
 			throw new WrongStateException("Friendship request doesn't exist.");
 		}
 		
 		userDal.removeFriendship(f);
 		
 		Friendship f2 = userDal.findReciprocal(f);
 		userDal.removeFriendship(f2);
 		
 		return f;
 	}
 
 }
