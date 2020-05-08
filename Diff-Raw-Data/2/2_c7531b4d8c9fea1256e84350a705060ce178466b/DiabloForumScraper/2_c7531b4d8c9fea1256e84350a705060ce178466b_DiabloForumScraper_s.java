 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package diabloforumscraper;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jsoup.Connection;
 import org.jsoup.Connection.Response;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author jwalto
  */
 public class DiabloForumScraper {
 
     //private static Logger log = Logger.getLogger(DiabloForumScraper.class);
     private static final String DIABLO_ROOT = "http://us.battle.net/d3/en/";
    private static final String GENERAL_DISCUSSION_ROOT = DIABLO_ROOT + "forum/3354739/?page=10";
     private static final String FORUM_TOPIC_ROOT = DIABLO_ROOT + "forum/topic/";
     private static final String USER_AGENT = "Mozilla";
     private DiabloData database;
 
     public DiabloForumScraper(DiabloData database) {
         this.database = database;
     }
 
     public Document getDocument(String url) {
         Document doc = null;
         Connection connection = Jsoup.connect(url);
         connection.userAgent(USER_AGENT);
         int attempts = 0;
         while (doc == null && attempts < 5) {
             try {
                 Response response = connection.execute();
                 if (response.statusCode() == 200) {
                     try {
 
                         doc = connection.get();
 
 
                     } catch (Exception e) {
                         e.printStackTrace();
                         attempts++;
                         try {
                             Thread.currentThread().sleep(5000);
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                     }
                 } else {
                     attempts = Integer.MAX_VALUE;
                     //do no try again, server error on page
                 }
             } catch (Exception e) {
                  System.out.println("Error trying to connect");
                 e.printStackTrace();
                 attempts = Integer.MAX_VALUE;
             }
 
         }
 
         return doc;
     }
 
     public List<String> getForumTopicUrls(Document doc) {
         List<String> topicIds = new ArrayList<String>();
         Elements forumTopicRows = doc.select("tbody tr");
         //System.out.println("Found " + forumTopicRows.size() + " topics");
         for (Element forumTopicRow : forumTopicRows) {
             //System.out.println("row html id = " + forumTopicRow.id());
             String topicId = forumTopicRow.id().replace("postRow", "");
             topicIds.add(topicId);
             //System.out.println("topicId = " + topicId);
 
         }
 
         return topicIds;
     }
 
     private String getNextPageLink(Document doc) {
         Elements nextPageSpans = doc.select("li.cap-item a span");
         // System.out.println("Found " + nextPageSpans.size() + " next page spans");
         String nextPageLink = null;
         for (Element nextPageSpan : nextPageSpans) {
             String spanText = nextPageSpan.text();
             //System.out.println("Span text = " + spanText);
             if (spanText.equalsIgnoreCase("Next")) {
                 //get parent which is the link tag
                 Element parent = nextPageSpan.parent();
                 nextPageLink = parent.attr("href");
                 //System.out.println("Next page link = " + nextPageLink);
                 break;
             }
         }
 
         return nextPageLink;
     }
 
     private TopicScraperResult processTopic(String topicId, String pageLink) {
         List<String> profiles = new ArrayList<String>();
         String topicPage = FORUM_TOPIC_ROOT + topicId;
         if (pageLink != null) {
             topicPage += pageLink;
         }
         System.out.println("Retrieving topic page: " + topicPage);
         Document topicDocument = getDocument(topicPage);
         if (topicDocument != null) {
             Elements profileLinks = topicDocument.select(".view-d3-profile");
             //System.out.println("Found " + profileLinks.size() + " profile links");
             for (Element profileLink : profileLinks) {
                 String link = profileLink.attr("href");
                 String profile = link.replace("/d3/en/profile/", "");
                 //remove ending forward slash
                 profile = profile.substring(0, profile.length() - 1);
                 //System.out.println("Found profile: " + profile);
                 profiles.add(profile);
             }
 
             String nextPageLink = getNextPageLink(topicDocument);
 
             TopicScraperResult result = new TopicScraperResult(profiles, nextPageLink);
 
             return result;
         } else {
              System.out.println("Document was null, encountered server error");
             //server error on page, so return empty results
             return new TopicScraperResult(new ArrayList<String>(), null);
         }
     }
 
     /*
      * process a forum page that has links to topics like page: http://us.battle.net/d3/en/forum/3354739/
      */
     public void processTopicListPage(Document topicListDoc) {
         List<String> topicIds = getForumTopicUrls(topicListDoc);
         String nextPageLink = null;
 
         for (String topicId : topicIds) {
 
             TopicScraperResult result = processTopic(topicId, nextPageLink);
             nextPageLink = result.nextPageLink;
             processProfiles(result.profiles);
             while (nextPageLink != null) {
                 result = processTopic(topicId, nextPageLink);
                 nextPageLink = result.nextPageLink;
                 processProfiles(result.profiles);
             }
         }
 
         String nextPage = getNextPageLink(topicListDoc);
         int topicPagesProcessed = 1;
         while (nextPage != null) {
             String nextTopicPage = GENERAL_DISCUSSION_ROOT + nextPage;
             System.out.println("Retrieving next topic page: " + nextTopicPage);
             Document nextPageDoc = getDocument(nextTopicPage);
             if (nextPageDoc == null) {
                 continue;
             }
             processTopicListPage(nextPageDoc);
 
             topicPagesProcessed++;
         }
     }
 
     public void processProfiles(List<String> profiles) {
         database.insertProfiles(profiles);
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         DiabloData database = new DiabloData();
         DiabloForumScraper scraper = new DiabloForumScraper(database);
         Document doc = scraper.getDocument(GENERAL_DISCUSSION_ROOT);
 
         scraper.processTopicListPage(doc);
 
     }
 
     private class TopicScraperResult {
 
         public List<String> profiles;
         public String nextPageLink;
 
         public TopicScraperResult(List<String> profiles, String nextPageLink) {
             this.profiles = profiles;
             this.nextPageLink = nextPageLink;
         }
     }
 }
