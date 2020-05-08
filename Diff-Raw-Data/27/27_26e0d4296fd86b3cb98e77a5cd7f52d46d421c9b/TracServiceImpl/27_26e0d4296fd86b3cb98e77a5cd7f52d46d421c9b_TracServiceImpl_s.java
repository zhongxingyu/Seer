 package wiki_trac2redmine;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * Implmenetation of ITraceService
  */
 public class TracServiceImpl implements ITracService {
 
     public TracServiceImpl(String aTitleIndexTracUrl) {
         theTracUrl = aTitleIndexTracUrl;
     }
 
     @Override
     public List<String> getWikiPages() {
 
 
         FirefoxProfile profile = new FirefoxProfile();
         profile.setPreference("network.http.phishy-userpass-length", 255);
 
         WebDriver driver = new FirefoxDriver(profile);
         try {
             driver.get(theTracUrl + "/wiki/TitleIndex");
 
             WebDriverUtils.waitForId(driver, "footer");
 
             // 2. get all links
             List<WebElement> allLinks = driver.findElements(By.tagName("a"));
 
             // 3. filters only page links
             List<String> ret = new LinkedList<String>();
             for (WebElement link : allLinks) {
                 String url = link.getAttribute("href");
                 int position = url.indexOf("/wiki/");
                 if(position > 0) {
                     String pageName = url.substring(position + 6);
                    if(!pageName.contains("?") && !pageName.startsWith("Trac")) {
                         ret.add(pageName);
                     }
                 }
             }
             return ret;
         } finally {
             driver.quit();
         }
     }
 
     @Override
     public String getWikiText(String aPageName) throws IOException {
         HttpClient httpClient = new HttpClient();
 
         httpClient.getState().setCredentials(AuthScope.ANY, createCredentialsFromUrl(theTracUrl));
 
         GetMethod get = new GetMethod(theTracUrl+"/wiki/"+aPageName+"?format=txt");
         get.setDoAuthentication(true);
 
         int resultCode = httpClient.executeMethod(get);
         if(resultCode==200) {
             return get.getResponseBodyAsString();
         } else {
             throw new IllegalStateException("bad return status "+resultCode+" "+get.getStatusText());
         }
     }
 
     /**
      * creates UsernamePasswordCredentials from url
      * @param aUrl url like http://USERNAME:PASSWORD@trac.com/trac/trac.cgi
      * @return
      */
     private UsernamePasswordCredentials createCredentialsFromUrl(String aUrl) {
         StringTokenizer st = new StringTokenizer(aUrl, "/:@");
         st.nextToken(); // skip http
         String username = st.nextToken();
         String pasword = st.nextToken();
 
         return new UsernamePasswordCredentials(username, pasword);
     }
 
     private final String theTracUrl;
 }
