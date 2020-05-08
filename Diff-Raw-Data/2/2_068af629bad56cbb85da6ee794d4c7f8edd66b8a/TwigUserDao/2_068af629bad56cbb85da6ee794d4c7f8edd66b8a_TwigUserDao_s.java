 package net.sparkmuse.data.twig;
 
 import net.sparkmuse.data.UserDao;
 import net.sparkmuse.user.Votable;
 import net.sparkmuse.user.Votables;
 import net.sparkmuse.user.UserLogin;
 import net.sparkmuse.data.entity.*;
 import com.google.inject.Inject;
 import static com.google.appengine.api.datastore.Query.FilterOperator.*;
 import com.google.common.collect.Sets;
 import com.google.common.collect.Iterables;
 import com.google.common.base.Predicate;
 
 import java.util.Set;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Sep 19, 2010
  */
 public class TwigUserDao extends TwigDao implements UserDao {
 
   @Inject
   public TwigUserDao(DatastoreService service) {
     super(service);
   }
 
   public UserVO findOrCreateUserBy(UserLogin login) {
     final UserVO userVO = helper.only(datastore.find()
         .type(UserVO.class)
         .addFilter("authProviderUserId", EQUAL, login.getAuthProviderUserId()));
 
     if (null == userVO) {
       UserVO newUser = UserVO.newUser(login);
       final UserVO storedNewUser = helper.store(newUser);
       final UserProfile profile = UserProfile.newProfile(storedNewUser);
       helper.store(profile);
       return storedNewUser;
     }
     else {
       userVO.updateUserDuring(login);
      return helper.store(userVO);
     }
   }
 
   public UserVO findUserBy(Long id) {
     return helper.getUser(id);
   }
 
   public UserProfile findUserProfileBy(String userName) {
     final UserVO user = helper.only(datastore.find()
         .type(UserVO.class)
         .addFilter("userNameLowercase", EQUAL, userName.toLowerCase()));
 
     return helper.only(datastore.find()
         .type(UserProfile.class)
         .ancestor(user)
     );
   }
 
   public Map<Long, UserVO> findUsersBy(Set<Long> ids) {
     return helper.getUsers(ids);
   }
 
   public UserVO update(UserVO user) {
     return helper.update(user);
   }
 
   public void saveApplication(String userName, String url) {
     UserApplication app = new UserApplication();
     app.userName = userName;
     app.url = url;
     datastore.store(app);
   }
 
   /**
    * Stores a record of the vote for the given user, upvotes the votable, stores
    * it to the datastore, and adjusts the author's reputation.
    *
    * @param votable
    * @param voter
    */
   public void vote(Votable votable, UserVO voter) {
     helper.associate(voter);
 
     final UserVote voteModel = datastore.load()
         .type(UserVote.class)
         .id(Votables.newKey(votable))
         .parent(voter)
         .now();
 
     //check for existing vote
     if (null == voteModel) {
       //store vote later so we can check if user has voted on whatever
       datastore.store().instance(UserVote.newUpVote(votable, voter)).parent(voter).later();
 
       //record aggregate vote count on entity
       if (votable instanceof Entity) {
         votable.upVote();
         helper.update((Entity) votable);
       }
 
       //adjust reputation
       final UserVO author = votable.getAuthor();
       author.setReputation(author.getReputation() + 1);
       helper.update(author);
     }
   }
 
   public <T extends Entity<T>> void vote(Class<T> entityClass, Long id, UserVO voter) {
     T entity = helper.load(entityClass, id);
     if (entity instanceof Votable) {
       vote((Votable) entity, voter);
     }
   }
 
   public Set<UserVote> findVotesFor(Set<Votable> votables, UserVO user) {
     if (CollectionUtils.size(votables) == 0) return Sets.newHashSet();
 
     Set<String> ids = Sets.newHashSet();
     for (Votable votable: votables) {
       ids.add(Votables.newKey(votable));
     }
 
     helper.associate(user);
     final Map<String, UserVote> voteMap = datastore.load()
         .type(UserVote.class)
         .ids(ids)
         .parent(user)
         .now();
 
     //filter out nulls
     final Iterable<UserVote> votes = Iterables.filter(voteMap.values(), new Predicate<UserVote>(){
       public boolean apply(UserVote voteModel) {
         return null != voteModel;
       }
     });
 
     return Sets.newHashSet(votes);
   }
 
 }
