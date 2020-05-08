 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.raoxunrong.check.firefox.FirefoxSpellCheckerPluginChecker;
 import org.raoxunrong.domain.item.CheckedItem;
 import org.raoxunrong.domain.page.CheckablePage;
 import org.raoxunrong.profile.FirefoxProfileHandle;
 import org.raoxunrong.utils.CheckedItemStatistic;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: twer
  * Date: 5/21/13
  * Time: 5:04 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class SpellCheckerDemoTest {
 
     public static void main(String[] args) throws IOException {
         FirefoxProfile firefoxProfile = new FirefoxProfile();
         FirefoxProfileHandle firefoxProfileHandle = new FirefoxProfileHandle();
         FirefoxSpellCheckerPluginChecker spellingCheckerFirefoxPluginChecker = new FirefoxSpellCheckerPluginChecker();
 
         firefoxProfile = firefoxProfileHandle.installSpellChecker(firefoxProfile);
         FirefoxDriver firefoxDriver = new FirefoxDriver(firefoxProfile);
        firefoxDriver.get("http://www.baidu.com/");
 
         GooglePage googlePage = new GooglePage(firefoxDriver);
         spellingCheckerFirefoxPluginChecker.doCheck(googlePage);
 
         Collection<CheckedItem> statisticInfo = CheckedItemStatistic.getStatisticInfo();
         Iterator<CheckedItem> iterator = statisticInfo.iterator();
         while(iterator.hasNext()){
             CheckedItem checkedItem = iterator.next();
             System.out.println(checkedItem.getItemName());
             System.out.println(checkedItem.getAdditionalInfo());
             System.out.println("-------------");
         }
 
     }
 
 }
 
 class GooglePage implements CheckablePage {
     private WebDriver webDriver;
 
     public GooglePage(WebDriver webDriver){
         this.webDriver = webDriver;
     }
 
     @Override
     public String getPageId() {
        return "wrapper";  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public WebDriver getWebDriver() {
         return webDriver;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public String getPageName() {
         return "Google Home Page";  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
