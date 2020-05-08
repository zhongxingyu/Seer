 package org.wikipedia;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import javax.security.auth.login.FailedLoginException;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 public class WikiIT {
     private static final Logger logger = Logger.getLogger(WikiIT.class.getName());
     public static final String BOT_TEST = "bot";
     public static final String BOT_TEST_PW = "CENSURED";
     private static String WIKI_MD_HOST = "CENSURED";
     private static String WIKI_MD_SCRIPT_PATH  = "/wiki";
     // private static String WIKI_WP_HOST = "en.wikipedia.org";
     // private static String WIKI_WP_SCRIPT_PATH = "/w";
     private static String WIKI_HOST = WIKI_MD_HOST;
     private static String WIKI_SCRIPT_PATH = WIKI_MD_SCRIPT_PATH;
 
     @Test
     @Ignore("need credentials")
     public void should_read_wiki() throws IOException, FailedLoginException {
         Wiki wiki = new Wiki(WIKI_HOST, WIKI_SCRIPT_PATH);
         wiki.setUsingCompressedRequests(false);
 
         // wiki.setThrottle(5000); // set the edit throttle to 0.2 Hz
         wiki.login(BOT_TEST, BOT_TEST_PW.toCharArray());
         String mainPageText = wiki.getPageText("Main Page");
         logger.info("main page : " + mainPageText);
         wiki.logout();
     }
 }
