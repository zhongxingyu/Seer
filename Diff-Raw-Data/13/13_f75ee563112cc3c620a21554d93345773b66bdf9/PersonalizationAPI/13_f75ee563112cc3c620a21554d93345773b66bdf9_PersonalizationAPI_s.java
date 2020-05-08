 package smartread;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import smartread.db.DBServeEvent;
 import smartread.db.DBStory;
 import smartread.db.DBUser;
 
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.LogManager;
 
 public class PersonalizationAPI {
     private static final Logger logger = LogManager.getLogger(PersonalizationAPI.class);
     
     public List<Story> getUserStory(String uid) {
         Long starttime = System.currentTimeMillis();
         List<Story> stories = DBStory.retrieveDefaultStory();
         User user = DBUser.retrieveUser(uid);
         Long endtime = System.currentTimeMillis();
         
        logger.error("Time(ms) taken to retrive user story: "+ String.valueOf(endtime-starttime));
         
         return calcualte(user, stories);
     }
 
     public void updateUserInterests(int lookbackMinute) {
         Long starttime = System.currentTimeMillis();
         Map<String, List<ServeEvent>> serves = DBServeEvent.QueryEvents(lookbackMinute);
         String freq;
         if(lookbackMinute==5)
             freq = "5m";
         else if(lookbackMinute==60*24)
             freq = "1d";
         else if(lookbackMinute==60*24*7)
             freq = "7d";
         else{
             return;
         }
         
         for (String uid : serves.keySet()) {
             List<String> tags = new ArrayList<String>();
             List<ServeEvent> userServes = serves.get(uid);
             for(ServeEvent s: userServes){
                 String storyID = s.getStoryID();
                 Story story = DBStory.getStory(storyID);
                 tags.addAll(story.getTags());
             }
             DBUser.updateUserInterest(uid, freq, tags);
         }
         Long endtime = System.currentTimeMillis();
        logger.error("Time(ms) taken to update user interests for "+lookbackMinute+" minutes: "+ String.valueOf(endtime-starttime));
     }
 
     private List<Story> calcualte(User user, List<Story> stories) {
         Long starttime = System.currentTimeMillis();
         Map<String, Double> interest = user.getInterests();
         if (interest.size() == 0)
             return stories;
 
         for (Story s : stories) {
             List<String> tags = s.getTags();
             s.setScore(s.getScore()*Utils.evaluateInterest(interest, tags));
         }
         Long endtime = System.currentTimeMillis();
        logger.error("Time(ms) taken to calculate story point for user "+user.getUid()+": "+ String.valueOf(endtime-starttime));
         return stories;
     }
 
     public static void main(String args[]) {
         logger.trace("Entering application.");
         
         PersonalizationAPI api = new PersonalizationAPI();
         api.updateUserInterests(5);
 
         List<Story> stories = api.getUserStory("test_user1");
         for (Story s : stories) {
            System.out.println(s);
         }
         logger.trace("Exiting application.");
     }
 }
