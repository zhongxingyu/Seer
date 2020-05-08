 package twitter.simplified.clone.domain;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Transient;
 import javax.persistence.TypedQuery;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.hibernate.Hibernate;
 import org.springframework.dao.DataAccessException;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 
 import flexjson.JSONSerializer;
 
 import twitter.simplified.clone.utils.RandomString;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord(finders = { "findUsersByUsernameLikeOrEmailAddressLikeOrFullNameLike", "findUsersByUsernameEquals" })
 @RooJson
 public class User implements UserDetailsService {
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "followed")
     private Set<Follow> followers = new HashSet<Follow>();
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "follower")
     private Set<Follow> follows = new HashSet<Follow>();
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "ownerUser")
     private Set<Tweet> ownedTweets = new HashSet<Tweet>();
 
     @NotNull
     @Size(min = 9, max = 9)
     private String randomSalt;
 
     @NotNull
     @Size(min = 64, max = 64)
     private String password;
 
     @NotNull
     @Size(min = 4, max = 100)
     private String fullName;
 
     @NotNull
     @Column(unique = true)
     @Size(min = 4, max = 16)
     private String username;
 
     @NotNull
     @Column(unique = true)
     @Size(min = 4, max = 254)
     private String emailAddress;
     
     @Transient
     private static ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder(256);
 
     @Transient
     private static RandomString randomSaltGenerator = new RandomString(9);
     
     public String getTempPasswordContainer() {
         return "";
     }
 
     public int getNumberFollowed() {
         Hibernate.initialize(getFollows());
         return getFollows().size();
     }
 
     public int getNumberFollowers() {
         Hibernate.initialize(getFollowers());
         return getFollowers().size();
     }
 
     public int getNumberOwnTweets() {
         Hibernate.initialize(getOwnedTweets());
         return getOwnedTweets().size();
     }
     
     public void setTempPasswordContainer(String tempPasswordContainer) {
         Assert.hasLength(tempPasswordContainer);
         Assert.state(tempPasswordContainer.length() > 6 && tempPasswordContainer.length() < 32, "Password must be bigger than 6 and less than 32 characters");
         String salt = randomSaltGenerator.nextString();
         setRandomSalt(salt);
         setPassword(passwordEncoder.encodePassword(tempPasswordContainer, salt));
     }
     
 //    /**
 //     * 
 //     * @return the first 10 tweets that should appear on the homepage
 //     */
 //    public Collection<Tweet> getTweetsForHomepage()
 //    {
 //    	return getTweetsForHomepage(10);
 //    }
     
     @Transactional(readOnly=true)
     public Collection<Tweet> getTweetsForHomepage()
     {
    	if (getFollows().isEmpty())
    		return getOwnedTweets();
     	 EntityManager em = User.entityManager();
          TypedQuery<Tweet> q = em.createQuery("SELECT t FROM Tweet AS t, Follow AS f WHERE (t.ownerUser) = (:user) OR f.follower = (:user) AND (t.ownerUser) = (f.followed)", Tweet.class);
          q.setParameter("user", this);
          return q.getResultList();
     	
     }
     
     @Transactional
     public void unFollow(User userToUnfollow)
     {
     	Hibernate.initialize(this);
     	
     	Hibernate.initialize(getFollows());
     	Follow followToRemove = null;
     	for(Follow follow : getFollows())
     	{
     		if (follow.getFollowed().equals(userToUnfollow))
     		{
     			followToRemove = follow;
     			break;
     		}
     	}
     	if (followToRemove != null)
     	{
     		getFollows().remove(followToRemove);
     		followToRemove.remove();
     	}
     	this.flush();
     }
     
     /**
      * 
      * @param collection
      * @return a json array without the useless details
      */
     public static String toJsonArrayWithoutDetails(Collection<User> collection)
     {
     	return new JSONSerializer().exclude("*.class", "password", "randomSalt", "emailAddress", "numberFollowed", "numberFollowers", "numberOwnTweets", "tempPasswordContainer", "version").serialize(collection);
     }
     
     public static String toJsonArray(Collection<User> collection)
     {
     	return new JSONSerializer().exclude("*.class", "password", "randomSalt", "emailAddress", "numberFollowed", "numberFollowers", "numberOwnTweets", "tempPasswordContainer", "version").serialize(collection);
     }
     
     public String toJson()
     {
     	return new JSONSerializer().exclude("*.class", "password", "randomSalt", "emailAddress", "numberFollowed", "numberFollowers", "numberOwnTweets", "tempPasswordContainer", "version").serialize(this);
     }
     
     @Override
     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         User singleResult = null;
         try {
             singleResult = User.findUsersByUsernameEquals(username).getSingleResult();
         } catch (NoResultException e) {
             throw new UsernameNotFoundException("no username found by TypedQuery.getSingleResult", e);
         }
         if (singleResult == null) throw new DataAccessException("error retrieving a User") {
         };
         org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(username, singleResult.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
         return user;
     }
 
 	public Collection<User> getFollowedUsers() {
 		return Collections2.transform(getFollows(), new Function<Follow, User>() {
 
 			@Override
 			public User apply(Follow follow) {
 				return follow.getFollowed();
 			}
 		});
 	}
     
 
 }
