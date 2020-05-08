 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kwetter.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 import kwetter.domain.Tweet;
 import kwetter.domain.TweetUser;
 import kwetter.dao.KwetterService;
 
 /**
  *
  * @author grave
  */
 @Named
 @RequestScoped
 public class KwetterController {
 
     @EJB
     private KwetterService kws;
 
     /**
      * Creates a new instance of KwetterController
      */
     /**
      *
      * @param user
      * @return lijst van alle tweets van User user
      */
     public List<Tweet> getTweetsFromUser(TweetUser user) {
        if (user == null) {
            return new ArrayList<>();
        }
         if (user.getTweets() == null) {
             return new ArrayList<>();
         } else {
             return new ArrayList<>(user.getTweets());
         }
     }
 
     public List<Tweet> getTimeLine(TweetUser u) {
         if (u != null) {
             return kws.getTimeLine(u);
         } else {
             throw new NullPointerException("User is null in getTimeLine");
         }
     }
 
     public List<Tweet> getMentions(TweetUser u) {
         if (u != null) {
             return kws.getMentions(u);
         } else {
             throw new NullPointerException("geen user ingevoerd bij GetMentions()");
         }
     }
 
     public Map<String, Integer> getTrends() {
         if (kws.getTrends() != null) {
             return kws.getTrends();
         } else {
             throw new NullPointerException("Geen trends aanwezig");
         }
     }
 
     /**
      * aanmaken gebruiker
      *
      * @return succes msg
      */
     public String create(TweetUser u) {
         kws.createUser(u);
         return "user aangemaakt";
     }
 
     /**
      *
      * @param user
      */
     public void edit(TweetUser user) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /*
      * Haal alle tweets op van volgers
      */
     public List<Tweet> getTweetsFromFollowers(TweetUser u) {
         return kws.getTweetsFromFollowers(u);
     }
 
     /*
      * Haal alle tweets op van mensen die de user zelf volgt.
      */
     public List<Tweet> getTweetsFromFollowedBy(TweetUser u) {
         return kws.getTweetsFromFollowedBy(u);
     }
 
     /**
      *
      * @param id
      * @return
      */
     public TweetUser find(String username) {
         return kws.findUser(username);
     }
 
     /**
      *
      * @param user
      */
     public void remove(TweetUser user) {
         kws.remove(user);
     }
 
     public ArrayList<TweetUser> getAllUsersFollowedBy(TweetUser u) {
         return kws.getAllUsersFollowedBy(u);
     }
 
     /**
      *
      * @return
      */
     public List<TweetUser> findAll() {
         return kws.findAllTweetUsers();
     }
 
     /**
      *
      * @return
      */
     public int count() {
         return kws.tweetUserCount();
     }
 
     /**
      *
      * @param u
      * @return
      */
     public int getNrOfFollowing(TweetUser u) {
         return kws.getNrOfFollowing(u);
     }
 
     /**
      *
      * @param u
      * @return
      */
     public int getNrOfFollowedBy(TweetUser u) {
         return kws.getFollowedBy(u);
     }
 
     /**
      *
      * @return
      */
     public int getAllTweetsCount() {
         return kws.getAllTweetsCount();
     }
 
     /**
      * @return the selectedUser
      */
     public TweetUser getSelectedUser() {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         String username = request.getRemoteUser();
        System.err.println(username);

         TweetUser t = null;
         if (username != null) {
             t = kws.findUser(username);
         }
         return t;
     }
 }
