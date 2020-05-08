 package se.umu.cs.umume.util;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import se.umu.cs.umume.PersonBean;
import se.umu.cs.umume.rest.resources.SearchResource;
 import twitter4j.Paging;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 public class TwitterUtils {
    private static final Logger logger = LoggerFactory.getLogger(TwitterUtils.class);
     public static List<PersonBean> getTweets(List<PersonBean> persons) {
         for (PersonBean person : persons) {
             String userName = person.getTwitterName();
             if (userName != null) {
                 Twitter twitter = new Twitter("umume", "soasoa");
                 List<Status> statuses = new ArrayList<Status>();
                 try {
                     statuses = twitter.getUserTimeline(userName, new Paging(1));
 

                    // Convert from Status to String
                    List<String> stringStatuses = new ArrayList<String>();
                    for(Status status : statuses) {
                        Date hej = status.getCreatedAt();
                        stringStatuses.add(status.getText());
                    }
                    person.setTweets(stringStatuses);
                } catch (TwitterException e) {
                    logger.info("Twitter for '{}': {}", userName, e.getMessage());
                 }
             }
         }
         return persons;
     }
 }
