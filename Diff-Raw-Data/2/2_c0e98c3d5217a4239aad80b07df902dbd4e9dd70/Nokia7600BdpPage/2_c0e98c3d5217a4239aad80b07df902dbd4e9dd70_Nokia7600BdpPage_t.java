 package au.com.sensis.mobile.crf.showcase.selenium.fixture;
 
 import static junit.framework.Assert.assertTrue;
 
 import com.thoughtworks.selenium.Selenium;
 
 /**
  * Assertions and actions for the BDP Page.
  *
  * @author Adrian.Koh2@sensis.com.au (based on Heather's work in Whereis Mobile)
  */
 public class Nokia7600BdpPage extends BdpPage {
 
     /**
      * Default constructor.
      *
      * @param selenium Selenium instance to use.
      */
     public Nokia7600BdpPage(final Selenium selenium) {
         super(selenium);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doAssertPageStructure() {
         assertNokia7600Css();
         assertNokia7600Script();
         assertNokia7600Jsp();
         assertNokia7600Img();
     }
 
     private void assertNokia7600Css() {
         final int expectedNumNokia7600CssLinks = 2;
         assertNumCssLinks(expectedNumNokia7600CssLinks);
         assertCssLink("main.css link not found", "default/common/main.css");
         assertCssLink("results.css link not found", "default/results/results.css");
     }
 
     private void assertNokia7600Script() {
         final int expectedNumNokia7600Scripts = 7;
         assertNumScripts(expectedNumNokia7600Scripts);
 
         assertNokia7600ScriptByNameNoBundling();
         assertNokia7600ScriptByAllNoBundling();
     }
 
     private void assertNokia7600ScriptByAllNoBundling() {
         assertScript("default/fielddecorators/decorator2.js script not found",
                 "default/fielddecorators/decorator2.js");
         assertScript("default/fielddecorators/decorator1.js script not found",
                 "default/fielddecorators/decorator1.js");
         assertScript("default/fielddecorators/decorator3.js script not found",
                 "default/fielddecorators/decorator3.js");
 
         assertScript("default/component/map/map1.js script not found",
                 "default/component/map/map1.js");
         assertScript("default/component/map/map2.js script not found",
                 "default/component/map/map2.js");
     }
 
     private void assertNokia7600ScriptByNameNoBundling() {
         assertScript("main.js script not found", "default/common/main.js");
         assertScript("results.js script not found", "default/results/results.js");
     }
 
     private void assertNokia7600Jsp() {
         assertTrue(getBrowser().isTextPresent("[default] bdp.jsp"));
     }
 
     private void assertNokia7600Img() {
         assertNumImgElements(1);
         assertImg("unmetered img not found",
                 "unmeteredImg", "Unmetered", "Unmetered",
                "nokia7600/common/unmetered.gif");
     }
 }
