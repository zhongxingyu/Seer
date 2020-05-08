 import com.thoughtworks.selenium.DefaultSelenium;
 import com.thoughtworks.selenium.FlashSelenium;
 import com.thoughtworks.selenium.Selenium;
 import junit.extensions.TestSetup;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.openqa.selenium.server.SeleniumServer;
 
 import java.util.ResourceBundle;
 
 public class ParentalGuidanceTest extends TestCase {
 
     private FlashSelenium flashApp;
     private Selenium browser;
     private SeleniumServer server;
     private static String playerURL;
     
     public void setUp() throws Exception {
         server = new SeleniumServer();
         server.start();
         pauseForMillis( 1000);
        browser = new DefaultSelenium("localhost", 4444, "*firefoxproxy", playerURL);
         pauseForMillis( 1000);
         browser.start();
         pauseForMillis( 1000);
         flashApp = new FlashSelenium( browser , "plugin" );
         pauseForMillis( 1000);
         browser.open(playerURL);
     }
 
     private void pauseForMillis(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
     }
 
     public void testAcceptGuidance() throws Exception  {
         pauseForMillis(3000);
         flashApp.call( "proceed" );
         pauseForMillis(1000);
         flashApp.call( "guidancePanelAccept"  );
         Thread.sleep(1000);
         assertEquals ( "PLAYING", flashApp.call( "playState" ) );
     }
 
     public void testDeclineGuidance() throws Exception {
         pauseForMillis(3000);
         flashApp.call( "proceed" );
         pauseForMillis(1000);
         flashApp.call( "guidancePanelDecline" );
         pauseForMillis(1000);
         assertEquals ( "NOT_STARTED", flashApp.call( "playState" ) );
     }
 
     public void tearDown(){
         browser.stop();
         server.stop();
     }
 
     public static Test suite(){
         TestSetup setup = new TestSetup(new TestSuite(ParentalGuidanceTest.class)) {
             protected void setUp() throws Exception {
                ResourceBundle bundle = ResourceBundle.getBundle( "testing" );
                playerURL = bundle.getString( "player.url" );
                assertNotNull( playerURL );
             }
             protected void tearDown(  ) throws Exception {
 
             }
         };
 
         return setup;
     }
 }
