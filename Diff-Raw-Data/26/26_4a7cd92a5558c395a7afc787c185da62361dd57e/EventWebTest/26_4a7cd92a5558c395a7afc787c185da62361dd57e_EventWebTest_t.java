 package no.bekk.webdriver;
 
 import no.bekk.webdriver.pages.EditEventPage;
 import no.bekk.webdriver.pages.EventListPage;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
 
 import static junit.framework.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 
 public class EventWebTest {
 
     private WebDriver driver;
 
     @Before
     public void setUp() {

        driver = new HtmlUnitDriver();


 //        driver = new ChromeDriver();
//        driver = new FirefoxDriver();

        // driver = new SlowDriver(driver);
 
         driver.get("http://localhost:8080/javazonebook/events");
     }
 
     @Test
     public void shouldAddEventToEventlist() {
         String eventName = "Vorspiel";
         EventListPage eventListPage = new EventListPage(driver);
         EditEventPage editEventPage = eventListPage.chooseAddEvent();
         eventListPage = editEventPage.addEvent(eventName, "Bekk Stand", "Bekk");
         assertTrue(eventListPage.isEventWithNameListed(eventName));
     }
 
     @Test
     public void shouldUpdateEventWithLocation() {
         String eventName = "Stand-up festival";
         String location = "Cafe con Bar";
         EventListPage eventListPage = new EventListPage(driver);
         EditEventPage editEventPage = eventListPage.chooseEditEventWithName(eventName);
         eventListPage = editEventPage.updateLocation(location);
         assertTrue(eventListPage.eventWithNameHasLocation(eventName, location));
     }
 
     @Test
     public void shouldDeleteEvent() {
         String eventName = "DJ Mario";
         EventListPage eventListPage = new EventListPage(driver);
         eventListPage.chooseDeleteEventWithName(eventName);
         assertFalse(eventListPage.isEventWithNameListed(eventName));
     }
 
     @After
    public void tearDown() throws InterruptedException {
//        Thread.sleep(SlowDriver.DELAY);
         driver.quit();
     }


 }
