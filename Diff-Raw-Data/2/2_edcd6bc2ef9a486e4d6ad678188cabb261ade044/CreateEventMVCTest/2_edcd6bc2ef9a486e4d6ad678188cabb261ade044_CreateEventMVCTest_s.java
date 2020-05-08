 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Brett
  * Date: 17/03/12
  * Time: 8:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CreateEventMVCTest {
 
     private  WebDriver driver;
 
     @Before
     public void setUp()
     {
         this.driver = new FirefoxDriver();
     }
 
     @Test
     public void shouldBeAbleToViewPageFromURI() throws Exception {
 
         // Given
 
         // When
        driver.get("http://localhost:8080/karboom-webapp/karboom/createEvent");
 
         // Then
         assertThat(driver.findElement(By.tagName("title")).getText(), is(equalTo("Create a new event")));
     }
 
     @After
     public void closeDown()
     {
         this.driver.close();
     }
 }
