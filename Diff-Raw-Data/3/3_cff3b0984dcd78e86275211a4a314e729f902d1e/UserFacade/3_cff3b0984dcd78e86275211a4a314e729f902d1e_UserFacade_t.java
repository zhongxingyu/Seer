 package net.sparkmuse.user;
 
 import com.google.inject.Inject;
 import com.google.inject.internal.Nullable;
 import com.google.common.collect.Maps;
 import net.sparkmuse.common.Cache;
 import net.sparkmuse.data.UserDao;
 import net.sparkmuse.data.util.AccessLevel;
 import net.sparkmuse.data.entity.*;
 import net.sparkmuse.ajax.InvalidRequestException;
 import net.sparkmuse.mail.MailService;
 import net.sparkmuse.mail.InvitationEmail;
 import net.sparkmuse.mail.EmailTemplate;
 
 import java.util.Set;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Jul 3, 2010
  */
 public class UserFacade {
 
   private final UserDao userDao;
   private final Cache cache;
   private final TwitterService twitterService;
   private final MailService mailService;
 
   @Inject
   public UserFacade(UserDao userDao, MailService mailService, TwitterService twitterService, Cache cache) {
     this.userDao = userDao;
     this.cache = cache;
     this.twitterService = twitterService;
     this.mailService = mailService;
   }
 
   public OAuthAuthenticationRequest beginAuthentication() {
     return twitterService.beginAuthentication();
   }
 
   /**
    * @param code  Invitation code
    * @return null if invitation does not exist
    */
   public Invitation verifyInvitationCode(String code) {
     return userDao.findInvitation(code);
   }
 
   public Invitation findInvitationBy(String groupName) {
     return userDao.findInvitationByGroup(groupName);
   }
 
   public UserVO registerAuthentication(OAuthAuthenticationResponse response, @Nullable String invitationCode) {
     UserVO user = userDao.findOrCreateUserBy(twitterService.registerAuthentication(response));
 
     if (user.isUnauthorized() && StringUtils.isNotBlank(invitationCode)) {
       Invitation invitation = verifyInvitationCode(invitationCode);
       if (Invitation.isValid(invitation)) {
         UserVO newUser = updateUser(user.getId(), AccessLevel.USER, 1);
         invitation.useInvite();
         userDao.store(invitation);
         return newUser;
       }
     }
     
     return user;
   }
 
   public List<UserProfile> getAllProfiles() {
     return userDao.getAllProfiles();
   }
 
   /**
    * People to display on User page
    *
    * @return
    */
   public List<UserProfile> getPeople() {
     return userDao.getPeopleProfiles();
   }
 
   public UserProfile createUser(String userName) {
     final UserProfile userProfile = getUserProfile(userName);
     return null == userProfile ? userDao.createUser(userName) : userProfile;
   }
 
   public UserVO updateUser(long userId, AccessLevel accessLevel, int invites) {
     final UserVO user = findUserBy(userId);
     user.setAccessLevel(accessLevel);
     userDao.store(user);
     UserProfile userProfile = getUserProfile(user.getUserName());
 
     //errors can occur where user is saved without profile
     if (null == userProfile) {
       userProfile = UserProfile.newProfile(user);
     }
    userProfile.setInvites(invites);
     userDao.store(userProfile);
 
     return user;
   }
 
   /**
    * Finds a user in the cache.  If not present, the db is queried and the cache is updated.
    *
    * @param id
    * @return
    */
   public UserVO findUserBy(final Long id) {
     return userDao.findUserBy(id);
   }
 
   public UserProfile getUserProfile(String userName) {
     return userDao.findUserProfileBy(userName);
   }
 
   public UserProfile updateProfile(UserProfile profile) {
     return userDao.update(profile);
   }
 
   public void applyForInvitation(final UserApplication userApplication) {
     userDao.saveApplication(userApplication);
   }
 
   public void recordUpVote(final Votable votable, final Long userId) {
     userDao.vote(votable, findUserBy(userId));
   }
 
   public void recordUpVote(String className, Long id, UserVO voter) {
     try {
       final Class clazz = Class.forName(className);
       if (Entity.class.isAssignableFrom(clazz)) {
         userDao.vote((Class<Entity>) clazz, id, voter);
       }
     } catch (ClassNotFoundException e) {
       return;
     }
   }
 
   public void recordNewSpark(UserVO user) {
     user.setSparks(user.getSparks() + 1);
     userDao.store(user);
   }
 
   public void recordNewPost(UserVO user) {
     user.setPosts(user.getPosts() + 1);
     userDao.store(user);
   }
 
   public UserVotes findUserVotesFor(Set<Votable> votables, UserVO user) {
     final Set<UserVote> userVotes = userDao.findVotesFor(votables, user);
     return new UserVotes(userVotes);
   }
 
   public int inviteFriend(UserVO inviter, String friend) {
     final UserProfile inviterProfile = getUserProfile(inviter.getUserName());
     if (inviterProfile.getInvites() > 0) {
 
       String friendUserName = friend.startsWith("@") ? friend.substring(1) : friend;
       final UserProfile newUserProfile = createUser(friendUserName);
       final UserVO newUser = newUserProfile.getUser();
 
       if (newUser.getAccessLevel().hasAuthorizationLevel(AccessLevel.USER)) {
         throw new InvalidRequestException("The user you invited is already a member, save that invite!");
       }
 
       //see if user already applied
       UserApplication app = userDao.findUserApplicationBy("@" + friendUserName);
 
       //give new user access
       newUser.setAccessLevel(AccessLevel.USER);
       userDao.store(newUser);
 
       //update remaining invites of inviter
       final int remainingInvites = inviterProfile.getInvites() - 1;
       inviterProfile.setInvites(remainingInvites);
       //update email if available
       if (null != app && StringUtils.isNotBlank(app.email)) newUserProfile.setEmail(app.email);
       updateProfile(inviterProfile);
 
       //email new user a notification
       if (null != app && StringUtils.isNotBlank(app.email)) {
         mailService.prepareAndSendMessage(new InvitationEmail(
             inviter.getUserName(),
             friendUserName,
             app.email
         ));
       }
 
       return remainingInvites;
     }
 
     return 0;
   }
 
   public void tweet(UserVO from, String message) {
     twitterService.tweet(from, message);
   }
 
   public void farewell(final String medicine) {
     Logger.info(medicine);
     mailService.prepareAndSendMessage(new EmailTemplate(){
       public String getToEmail() {
         return "dave@sparkmuse.com";
       }
 
       public String getSubject() {
         return "Farewell Message";
       }
 
       public String getUpdateeName() {
         return "Dave";
       }
 
       public Map<String, Object> getTemplateArguments() {
         final Map<String, Object> args = Maps.newHashMap();
         args.put("medicine", medicine);
         return args;
       }
 
       public String getTemplate() {
         return "Mail/Farewell.html";
       }
     });
   }
 }
