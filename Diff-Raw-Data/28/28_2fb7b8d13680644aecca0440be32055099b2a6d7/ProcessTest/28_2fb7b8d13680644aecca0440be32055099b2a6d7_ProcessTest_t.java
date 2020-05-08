 
 package seleniumTests;
 
import ohtu.domain.Reference;
import ohtu.service.ReferenceServiceImpl;
import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/front-controller-servlet.xml"})
public class ProcessTest {
 
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
  public void testContextBeans(){
        ReferenceServiceImpl bean = applicationContext.getBean(ohtu.service.ReferenceServiceImpl.class);
        bean.add(new Reference("iluaP", "Pauli"));
        assertTrue(bean.listAll().get(0).getAuthor().equals("iluaP"));
  }
    
    
     @Test
     public void someSiteIsUp(){
        String url = baseUrl+"/list";
        driver.get(url);
        assertTrue(driver.getPageSource().contains("JSP"));
     }
 }
