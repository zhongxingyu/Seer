 package it.com.atlassian.aui.javascript.selenium;
 
 import com.atlassian.selenium.SeleniumAssertions;
 import com.atlassian.selenium.SeleniumClient;
 import com.atlassian.selenium.SeleniumConfiguration;
 import junit.framework.TestCase;
 
 import static com.atlassian.selenium.browsers.AutoInstallClient.assertThat;
 import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumClient;
 import static com.atlassian.selenium.browsers.AutoInstallClient.seleniumConfiguration;
 
 public abstract class AbstractAUISeleniumTestCase extends TestCase
 {
     protected static SeleniumAssertions assertThat;
     protected static SeleniumClient client;
     protected static SeleniumConfiguration config;
 
     static
     {
         if (System.getProperty("baseurl") == null)
         {
             System.setProperty("baseurl", "http://localhost:9999/ajs");
         }
         assertThat = assertThat();
         client = seleniumClient();
         config = seleniumConfiguration();
     }
 
     public void openTestPage(String page)
     {
         client.open("plugins/servlet/ajstest/" + page);
     }
 
     public int getWindowHeight(){
         return Integer.valueOf(client.getEval("window.AJS.$(window).height()"));
     }
 
     public int getWindowWidth(){
         return Integer.valueOf(client.getEval("window.AJS.$(window).width()"));
     }
 
    public String getCss(String attribute, String element){
        return client.getEval("window.AJS.$('"+ element +"').css('"+ attribute +"')");
    }

 }
