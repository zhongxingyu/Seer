 package models;
 
 import com.google.common.collect.Lists;
 import siena.*;
 import utils.Marshalling;
 import utils.cache.Caches;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User's Social Graph.
  *
  * @author Clement Pang (clement@pipoog.com)
  */
 @Table("userSocialGraphs")
 public class UserSocialGraph extends Model implements Serializable, Comparable<UserSocialGraph> {
 
   @Override
   public String toString() {
     return "UserSocialGraph for user=" + user;
   }
 
   @Id(Generator.AUTO_INCREMENT)
   public Long id;
 
   @Column("user")
   public User user;
 
   @Column("friends")
   public List<Long> friends = Lists.newArrayList();
 
   @Column("activeFriends")
   public List<Long> activeFriends = Lists.newArrayList();
 
   @Column("creationTime")
   @DateTime
   public Date creationTime = new Date();
 
   @Column("updateTime")
   @DateTime
   public Date updateTime = new Date();
 
   public UserSocialGraph() {
   }
 
   public UserSocialGraph(User user) {
     this.user = user;
   }
 
   public List<Long> getFriends() {
    return friends == null ? Lists.<Long>newArrayList() : friends;
   }
 
   public List<Long> getActiveFriends() {
    return activeFriends == null ? Lists.<Long>newArrayList() : activeFriends;
   }
 
   public org.joda.time.DateTime getCreationTimeAsJodaDateTime() {
     return new org.joda.time.DateTime(creationTime);
   }
 
   public org.joda.time.DateTime getUpdateTimeAsJodaDateTime() {
     return new org.joda.time.DateTime(updateTime);
   }
 
   @Override
   public void save() {
     super.save();
     Caches.ACTIVE_FRIENDS_ASYNC.put(user.id, Marshalling.serialize(getActiveFriends()));
   }
 
   @Override
   public int compareTo(UserSocialGraph userSocialGraph) {
     if (updateTime.getTime() > userSocialGraph.updateTime.getTime()) {
       // we are newer, appear earlier.
       return -1;
     } else if (updateTime.getTime() < userSocialGraph.updateTime.getTime()) {
       // we are older, appear latter.
       return 1;
     }
     return 0;
   }
 }
