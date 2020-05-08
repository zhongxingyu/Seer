 package loyalitymonitor;
 
 import LoyalityMonitor.FixHadoopOnWindows;
 import org.apache.hadoop.fs.Path;
 import org.apache.pig.ExecType;
 import org.apache.pig.impl.PigContext;
 import org.apache.pig.pigunit.Cluster;
 import org.apache.pig.pigunit.PigTest;
 import org.apache.pig.pigunit.pig.PigServer;
 import org.apache.pig.tools.parameters.ParseException;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 public class PigBaseAggTest {
     private String[] args = {
             "input=null",
             "output=null",
             "parallel=2",
             "pathToElephantBird=batch-admin/src/main/resources/jars/elephant-bird-pig-3.0.0.jar",
             "pathToSimpleJson=batch-admin/src/main/resources/jars/json-simple-1.1.1.jar"
     };
 
     private PigTest test;
     private Cluster cluster;
 
     @Before
     public void setUp() throws Exception {
         PigServer pigServer = new PigServer(ExecType.LOCAL);
         test = new PigTest("batch-admin/src/main/resources/pig/baseAggJob.pig", args, pigServer, new Cluster(pigServer.getPigContext()));
 
         FixHadoopOnWindows.runFix();
         cluster = PigTest.getCluster();
     }
 
 
 
     @Test
     public void testBaseAggregation() throws IOException, ParseException {
         String[] input = {
                 "{\"filter_level\":\"low\",\"contributors\":null,\"text\":\"http://t.co/plqm772pZT: President Obama: Our prayers are with Oklahoma http://t.co/KA39nyo4L0 #Pinterest #news\",\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"possibly_sensitive\":false,\"truncated\":false,\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://OTGDailyNews.com\",\"indices\":[0,22],\"display_url\":\"OTGDailyNews.com\",\"url\":\"http://t.co/plqm772pZT\"},{\"expanded_url\":\"http://weotg.us/14wmY3f\",\"indices\":[71,93],\"display_url\":\"weotg.us/14wmY3f\",\"url\":\"http://t.co/KA39nyo4L0\"}],\"hashtags\":[{\"text\":\"Pinterest\",\"indices\":[94,104]},{\"text\":\"news\",\"indices\":[105,110]}],\"user_mentions\":[]},\"in_reply_to_status_id_str\":null,\"id\":337123856877748224,\"source\":\"<a href=\\\"http://twitterfeed.com\\\" rel=\\\"nofollow\\\">twitterfeed<\\/a>\",\"in_reply_to_user_id_str\":null,\"favorited\":false,\"in_reply_to_status_id\":null,\"retweet_count\":0,\"created_at\":\"Wed May 22 08:32:45 +0000 2013\",\"in_reply_to_user_id\":null,\"favorite_count\":0,\"id_str\":\"337123856877748224\",\"place\":null,\"user\":{\"location\":\"Philadelphia\",\"default_profile\":false,\"statuses_count\":67432,\"profile_background_tile\":true,\"lang\":\"en\",\"profile_link_color\":\"0084B4\",\"id\":212810156,\"following\":null,\"favourites_count\":119,\"protected\":false,\"profile_text_color\":\"333333\",\"description\":\"OFFICIAL TWITTER FOR OTG DAILY NEWS!\",\"verified\":false,\"contributors_enabled\":false,\"profile_sidebar_border_color\":\"C0DEED\",\"name\":\"OTGDailyNews\",\"profile_background_color\":\"C0DEED\",\"created_at\":\"Sun Nov 07 03:31:23 +0000 2010\",\"default_profile_image\":false,\"followers_count\":13814,\"profile_image_url_https\":\"https://si0.twimg.com/profile_images/3450766347/87c7a7b4d28a6eb5c7b0cc600ddf6fc8_normal.png\",\"geo_enabled\":false,\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/272058305/2467042606-1.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/272058305/2467042606-1.jpg\",\"follow_request_sent\":null,\"url\":\"http://www.OTGDAILYNEWS.COM\",\"utc_offset\":-21600,\"time_zone\":\"Central Time (US & Canada)\",\"notifications\":null,\"profile_use_background_image\":true,\"friends_count\":6794,\"profile_sidebar_fill_color\":\"DDEEF6\",\"screen_name\":\"OTGDailyNews\",\"id_str\":\"212810156\",\"profile_image_url\":\"http://a0.twimg.com/profile_images/3450766347/87c7a7b4d28a6eb5c7b0cc600ddf6fc8_normal.png\",\"listed_count\":61,\"is_translator\":false},\"coordinates\":null}",
         };
 
         String[] output = {
                "(http://t.co/plqm772pZT: President Obama: Our prayers are with Oklahoma http://t.co/KA39nyo4L0 #Pinterest #news,Wed May 22 08:32:45 +0000 2013)",
         };
 
         test.assertOutput("raw_line", input, "tweets", output);
     }
 
 
 
 }
