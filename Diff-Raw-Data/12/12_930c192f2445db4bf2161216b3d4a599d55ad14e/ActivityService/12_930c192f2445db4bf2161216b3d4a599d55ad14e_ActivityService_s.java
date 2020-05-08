 package org.exoplatform.addons.populator.services;
 
 import org.exoplatform.social.common.RealtimeListAccess;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.ActivityManager;
 import org.exoplatform.social.core.manager.IdentityManager;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 @Named("activityService")
 @ApplicationScoped
 public class ActivityService {
 
   Logger log = Logger.getLogger("ActivityService");
 
   ActivityManager activityManager_;
   IdentityManager identityManager_;
 
   Random random = new Random();
 
   @Inject
   public ActivityService(ActivityManager activityManager, IdentityManager identityManager)
   {
     activityManager_ = activityManager;
     identityManager_ = identityManager;
   }
 
   public void pushActivities(String username)
   {
     pushActivity("user", username, "I'm with @mary at Paris");
   }
 
   private void pushActivity(String type, String from, String body)
   {
     String provider = OrganizationIdentityProvider.NAME;
     if ("space".equals(type))
     {
       provider = SpaceIdentityProvider.NAME;
     }
     Identity identity = identityManager_.getOrCreateIdentity(provider, from, false);
     Identity identJohn = identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
     Identity identMary = identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     activity.setBody(body);
     activity.setTitle(body);
     activity = activityManager_.saveActivity(identity, activity);
    activityManager_.saveLike(activity, identity);
    activityManager_.saveLike(activity, identJohn);
 
     ExoSocialActivity comment = new ExoSocialActivityImpl();
     comment.setTitle("Nice coffee");
     comment.setUserId(identMary.getId());
     activityManager_.saveComment(activity, comment);
 
     likeRandomActivities("mary");
     likeRandomActivities("james");
   }
 
   private void sleep(long ms)
   {
     try {
       Thread.sleep(ms);
     } catch (InterruptedException e) {
     }
   }
 
   private void likeRandomActivities (String username)
   {
     Identity identity = identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
     RealtimeListAccess rtla = activityManager_.getActivitiesWithListAccess(identity);
     ExoSocialActivity[] la = (ExoSocialActivity[])rtla.load(0, rtla.getSize());
     for (int iam = 0; iam<rtla.getSize() ; iam++)
     {
       ExoSocialActivity activityMary = la[iam];
       boolean like = random.nextBoolean();
       if (like)
       {
         activityManager_.saveLike(activityMary, identity);
       }
     }
     rtla = activityManager_.getActivitiesOfUserSpacesWithListAccess(identity);
     la = (ExoSocialActivity[])rtla.load(0, rtla.getSize());
     for (int iam = 0; iam<rtla.getSize() ; iam++)
     {
       ExoSocialActivity activityMary = la[iam];
       boolean like = random.nextBoolean();
       if (like)
       {
         activityManager_.saveLike(activityMary, identity);
       }
     }
 
   }
 
 }
