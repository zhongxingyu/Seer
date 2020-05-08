 package com.forum.web.page.tests;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: twu
  * Date: 23/8/12
  * Time: 4:59 PM
  * To change this template use File | Settings | File Templates.
  */
 public class JoinTest {
     private WebDriver webDriver;
 
     @Before
     public void setUp() throws Exception {
         webDriver = new FirefoxDriver();
         webDriver.get("http://10.10.5.107:8080/forum/join");
     }
 
     @Test
     public void shouldVisitJoinPage() throws Exception {
 
         assertThat(webDriver.getCurrentUrl(), is("http://10.10.5.107:8080/forum/join"));
     }
 
//    @Test
//    public void shouldVisitTermsPage() {
//        WebElement tosLink = webDriver.findElement(By.name("tos"));
//        tosLink.click();
//        assertThat(webDriver.getCurrentUrl(), is("http://10.10.5.107:8080/forum/terms"));
//    }
 }
