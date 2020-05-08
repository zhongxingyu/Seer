 package net.sparkmuse.activity;
 
 import net.sparkmuse.common.Cache;
 import net.sparkmuse.common.Orderings;
 import net.sparkmuse.data.ActivityDao;
 import net.sparkmuse.data.entity.Activity;
 import net.sparkmuse.data.entity.UserVO;
 
 import java.util.List;
 import java.util.TreeSet;
 import java.io.Serializable;
 
 import org.joda.time.DateTime;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.google.common.collect.Lists;
 import com.google.inject.internal.ImmutableList;
 
 /**
  * @author neteller
  * @created: Feb 12, 2011
  */
 public class ActivityStream implements Serializable {
 
   private final ImmutableList<Activity> activities;
 
   /**
    * @param activities  should be ordered by created descending
    */
   ActivityStream(List<Activity> activities) {
     this.activities = ImmutableList.copyOf(activities);
   }
 
   public static ActivityStream.Builder builder(ActivityDao activityDao) {
     return new ActivityStream.Builder(activityDao);
   }
 
  public ImmutableList<Activity> getActivities() {
    return activities;
   }
 
   public DateTime getOldestTime() {
     if (this.activities.size() == 0) return new DateTime();
     return Iterables.getLast(this.activities).getCreated();
   }
 
   /**
    * Overlays the passed in activity stream on top of the existing one
    * and returns a new one.  Any duplicates (as defined by Activity.compareTo)
    * are taken from the parameterized stream.
    *
    * @param userActivity
    * @return
    */
   public ActivityStream overlay(ActivityStream userActivity) {
     TreeSet<Activity> set = Sets.newTreeSet(userActivity.activities);
     //apply this instance afterwards, duplicates will fail to be added to set
     set.addAll(activities);
     return new ActivityStream(Lists.newArrayList(set));
   }
 
   static class Builder {
 
     private final ActivityDao activityDao;
 
     private UserVO user;
     private DateTime after;
 
     private Builder(ActivityDao activityDao) {
       this.activityDao = activityDao;
     }
 
     public Builder forUser(UserVO user) {
       this.user = user;
       return this;
     }
 
     public Builder after(DateTime dateTime) {
       this.after = dateTime;
       return this;
     }
 
     public ActivityStream build() {
       if (null == user) {
         return new ActivityStream(activityDao.findEveryone(50));
       }
       else {
         Preconditions.checkNotNull(after);
         return new ActivityStream(activityDao.findUser(user, after));
       }
     }
 
   }
 
 }
