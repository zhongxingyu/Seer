 package smartread;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import smartread.db.DBServeEvent;
 import smartread.db.DBStory;
 import smartread.db.DBUser;
 import smartread.db.DBUserStoryIndex;
 
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.LogManager;
 
 public class PersonalizationAPI {
     private static final Logger logger = LogManager.getLogger(PersonalizationAPI.class);  
     
     public List<Story> getUserStory(String uid) {
         Long starttime = System.currentTimeMillis();
         List<Story> stories = DBStory.retrieveDefaultStory();
         User user = DBUser.retrieveUser(uid);
         Long endtime = System.currentTimeMillis();
         
         logger.debug("Time(ms) taken to retrive user story: "+ String.valueOf(endtime-starttime));
         
         return calcualte(user, stories);
     }
 
     public void updateUserInterests(String freq) {
         if(freq.equalsIgnoreCase("1h")){
             DBUser.updateUserInterest("1h","5m");
         }else if(freq.equalsIgnoreCase("1d")){
             DBUser.updateUserInterest("1d","1h");            
         }else if(freq.equalsIgnoreCase("7d")){
             DBUser.updateUserInterest("7d","1d");
         }else{
             logger.error("Invalid update frequency");
         }
     }
     
     public Set<String> updateUserInterestsRaw(int lookbackMinute) {
         Long starttime = System.currentTimeMillis();
         String freq;
         if(lookbackMinute==5||lookbackMinute==10)
             freq = "5m";
         else{
             logger.error("Only query raw events for freq 5m");
             return null;
         }
         
         Map<String, List<ServeEvent>> serves = DBServeEvent.QueryEvents(lookbackMinute);
         
         for (String uid : serves.keySet()) {
             Map<List<String>, Double> tags = new HashMap<List<String>, Double>();
             
             
             List<ServeEvent> userServes = serves.get(uid);
             for(ServeEvent s: userServes){
                 String storyID = s.getStoryID();
                 Story story = DBStory.getStory(storyID);
                 List<String> temp = new ArrayList<String>(story.getTags());
                 if(tags.containsKey(temp)){
                     tags.put(temp, s.getTimespend()/300.0+tags.get(temp));
                 }else{
                     tags.put(temp, s.getTimespend()/300.0);
                 }
             }
             DBUser.updateUserInterest(uid, freq, tags);
         }
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to update user interests for "+lookbackMinute+" minutes: "+ String.valueOf(endtime-starttime));
         return serves.keySet();
     }
 
     private List<Story> calcualte(User user, List<Story> stories) {
         Long starttime = System.currentTimeMillis();
         Map<String, Double> interest = user.getInterests();
         if (interest.size() == 0)
             return stories;
 
         for (Story s: stories) {
             List<String> tags = s.getTags();
             s.setNScore(s.getBScore()*Utils.evaluateInterest(interest, tags));
         }
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to calculate story point for user "+user.getUid()+": "+ String.valueOf(endtime-starttime));
         return stories;
     }
 
     public static void main(String args[]) throws InterruptedException {
         logger.trace("Entering application.");
 
         if(args.length!=1){
             logger.error("Please input the update freq in minuts");
             return;
         }
         int lookbackMinute = Integer.parseInt(args[0]);
         
         PersonalizationAPI api = new PersonalizationAPI();
         Set<String> updatedUsers = null;
         switch(lookbackMinute){
             case 5: case 10: updatedUsers = api.updateUserInterestsRaw(lookbackMinute);
                 api.updateUserStoryForAll();
                 //if (updatedUsers != null) {
                 //    for (String uid : updatedUsers) {
                 //        List<Story> stories = api.getUserStory(uid);
                 //         api.storeUserStory(uid, stories);
                 //    }
                 //}
                 break;
             case 60: api.updateUserInterests("1h");
                 break;
             case 60*24: api.updateUserInterests("1d");
                 break;
             case 60*24*7: api.updateUserInterests("7d");
                 break;
             default: logger.error("Invalid update frequency");
                 break;
         }
 
     }
 
     private void updateUserStoryForAll() {
         Long starttime = System.currentTimeMillis();
         List<Story> stories = DBStory.retrieveDefaultStory();
         timelineFactor(stories);
         List<User> users = DBUser.retrieveAllUser();
         for(User u: users){
             storeUserStory(u.getUid(), calcualte(u, stories));
         }
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to refresh all users' story score: "+ String.valueOf(endtime-starttime));        
     }
 
     private void storeUserStory(String uid, List<Story> stories) {
         DBUserStoryIndex.storeUserStory(uid, stories);
     }
     
     private void timelineFactor(List<Story> stories){
         Long starttime = System.currentTimeMillis();
 
         Date date = new Date(System.currentTimeMillis());
         for(Story s: stories){
             Date sDate = s.getPubDate();
            double factor = 1-(date.getTime()-sDate.getTime())*0.05/1000/60/60/24;
            logger.trace("Timeline factor for story "+s.getStoryID()+"is "+factor);
             s.setBScore(s.getBScore()*factor);
         }
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to update story score based on timeline: "+ String.valueOf(endtime-starttime));                
     }
 }
