 
 package seleniumTests;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/front-controller-servlet.xml"})
public class ProcessTests {
 
     private WebDriver driver;
     private String port;
     private String baseUrl;
 
     @Autowired
     private ApplicationContext applicationContext;
 
     @Before
     public void setUp() throws Throwable {
         driver = new HtmlUnitDriver();
         port = System.getProperty("jetty.port", "8090");
         baseUrl = "http://localhost:" + port + "/app";
     }
     
  
     @Test
     public void someSiteIsUp(){
        fail(baseUrl);
        driver.get(baseUrl+"/list/");
        fail(driver.getPageSource());
       
     }
 }
