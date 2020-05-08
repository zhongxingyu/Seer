 package wiki_trac2redmine;
 
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest {
 
     @Test
     public void test() throws IOException, InterruptedException {
         ITracService tracService = new TracServiceImpl(System.getProperty("TRAC_URL"));
         IRedmineService redmineService = new RedmineServiceImpl(System.getProperty("REDMINE_PROJECT_URL"));
 
         System.out.println("Loading trac wiki pages ...");
         List<String> pages = tracService.getWikiPages();
 
         for (String page : pages) {
 
             System.out.println("Converting " + page + " ...");
            String tracWikiText = tracService.getWikiText(page);
            if(!page.startsWith("1")) {
                if(! redmineService.isPageExists(page)) {
                    try {
                        String redmineWikiText = redmineService.convertFromTracWikiText(tracWikiText);
                        redmineService.createPage(page, redmineWikiText);
                    } catch (IllegalStateException e) {
                        System.err.println("Error converting "+page);
                    }
                 }
             }
         }
     }
 }
