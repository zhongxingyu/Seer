 package jobs;
 
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.api.services.plus.Plus;
 import com.google.api.services.plus.model.Activity;
 import com.google.api.services.plus.model.ActivityFeed;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import models.ActivitiesHelper;
 import models.ActivityWrapper;
 import models.domain.Article;
 import models.domain.Statistiques;
 import models.utils.HtmlUtils;
 import org.apache.commons.collections.Closure;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import play.jobs.Every;
 import play.jobs.Job;
 import play.jobs.On;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * .
  *
  * @author fsznajderman
  *         date :  01/05/12
  */
 @On(" 0/30 * * * * ?")
 public class RefreshDatasFromGPlusJobs extends Job {
 
     final static JsonFactory jsonFactory = new JacksonFactory();
     final static HttpTransport httpTransport = new NetHttpTransport();
 
     @Override
     public void doJob() throws Exception {
         System.out.println("start shedule job");
 
         final Plus plus = new Plus(httpTransport, jsonFactory);
         // When we do not specify access tokens, we must specify our API key
         // instead
         String key = HtmlUtils.getValueFromGplusConf("google.key");
 
         plus.setKey(key);
         final Plus.Activities.List listActivities = plus.activities.list(HtmlUtils.getValueFromGplusConf("google.id.page"), "public");
 
         // Fetch the first page of activities
 
         listActivities.setMaxResults(100L);
         // Pro tip: Use partial responses to improve response time considerably
         listActivities.setFields("nextPageToken,items(id,url,published,object(content,plusoners,resharers))");
 
         Set<Article> fromGPlus = new HashSet<Article>();
         final List<ActivityWrapper> activityWrappers = Lists.newArrayList();
         try {
 
             ActivityFeed feed = listActivities.execute();
             List<Activity> activities = feed.getItems();
             while (activities != null) {
                 System.out.println("turn " + activities.size());
                 // Execute and process the next page request
                 for (final Activity activity : activities) {
 
                     final ActivityWrapper wrapper = new ActivityWrapper(activity);
 
                     activityWrappers.add(wrapper);
                     Article a = new Article(wrapper.getAuthor(), wrapper.getContent(), wrapper.getGoogleId(), wrapper.getNbPlusOners(), wrapper.getPublicationDate().getValue(), wrapper.getNbReshared(), wrapper.getTitle(), new Date().getTime());
 
                     fromGPlus.add(a);
 
 
                 }
                 if (feed.getNextPageToken() == null) {
                     System.out.println("break ");
                     break;
                 }
 
                 listActivities.setPageToken(feed.getNextPageToken());
 
                 // Execute and process the next page request
                 feed = listActivities.execute();
                 activities = feed.getItems();
             }
         } catch (final IOException e) {
             e.printStackTrace();
         }
 
         List<Article> fromDBList = Article.findAll();
         //reset all articles by setting their current field to Boolean.FALSE.
         CollectionUtils.forAllDo(fromDBList, new Closure() {
             public void execute(Object o) {
 
                 ((Article) o).setCurrent(Boolean.FALSE);
                 ((Article) o).save();
             }
         });
 
         for (final Article a : fromGPlus) {
 
             if (!fromDBList.contains(a)) {
                 System.out.println("save " + a.getTitle());
                 a.setCurrent(Boolean.TRUE);
                 a.save();
 
             } else {
 
                 Article c = (Article) CollectionUtils.find(fromDBList, new Predicate() {
                     public boolean evaluate(Object o) {
                         Article article = (Article) o;
                         return a.equals(article);
                     }
                 });
                 c.setCurrent(Boolean.TRUE);
                 c.save();
             }
         }
         createStatistique(activityWrappers);
 
         System.out.println("End Job");
     }
 
     private void createStatistique(final List<ActivityWrapper> activityWrappers) {
         final Statistiques s = new Statistiques();
         final ActivitiesHelper ah = new ActivitiesHelper();
         ah.setActivityWrappers(activityWrappers);
 
         s.setBestNamePost(ah.getBestNamePost());
         s.setBestTitlePost(ah.getBestTitrePost());
         s.setPlusOneMatrix(ah.getPlusOneMatrix());
         s.setSharedMatrix(ah.getSharedMatrix());
         s.setSumPlusOne(ah.getCompileSumPlusOnePage());
         s.setSumShared(ah.getCompileSumShared());

         final List<Statistiques> stats = Statistiques.findAll();
         CollectionUtils.forAllDo(stats, new Closure() {
             public void execute(Object o) {
                 Statistiques currentStat = (Statistiques) o;
                 currentStat.setCurrent(Boolean.FALSE);
                 currentStat.save();
             }
         });
 
         if (!stats.contains(s)) {
             System.out.println(s.toString());
             s.setCurrent(Boolean.TRUE);
             s.save();
         } else {
 
             final Statistiques c = (Statistiques) CollectionUtils.find(stats, new Predicate() {
                 public boolean evaluate(Object o) {
                     Statistiques currentStat = (Statistiques) o;
                     return s.equals(currentStat);
                 }
             });
             c.setCurrent(Boolean.TRUE);
             c.save();
         }
 
 
     }
 }
