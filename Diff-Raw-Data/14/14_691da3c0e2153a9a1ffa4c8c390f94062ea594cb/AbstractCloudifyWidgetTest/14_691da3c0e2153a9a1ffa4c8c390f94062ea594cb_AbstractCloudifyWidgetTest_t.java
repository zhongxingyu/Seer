 package org.cloudifysource.widget.test;
 
 
 import com.google.common.base.Predicate;
 import org.cloudifysource.widget.beans.JCloudsContext;
 import org.cloudifysource.widget.beans.TestContext;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.openqa.selenium.*;
 import org.openqa.selenium.support.ui.FluentWait;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * User: sagib
  * Date: 06/01/13
  * Time: 16:33
  */
 public class AbstractCloudifyWidgetTest {
     public static final int NUM_OF_SUITES = 2;
     protected static WebDriver webDriver;
 
     public static final String HOST = context().getTestConf().getHost();
     private static AtomicInteger counter = new AtomicInteger(0);
     private static final int random = (int)(Math.random() * 1000);
    protected static String PASSWORD = "testTest1" + random + counter.get();
    protected static String EMAIL = "test@test" + random + counter.get() + ".com";
    protected static String NAME = "test" + random + counter.get();
 
     private static Logger logger = LoggerFactory.getLogger(AbstractCloudifyWidgetTest.class);
 
     @BeforeClass
     public static void before(){
        int i = counter.incrementAndGet();
        logger.info("before class - starting webDriver counter is at: " + i);
         webDriver = context().getWebDriver();
         webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
         webDriver.get(HOST);
         dropSession();
        if(i == 1)
            subscribe(EMAIL, PASSWORD, NAME);
     }
 
     @Before
     public void beforeMethod(){
         logger.info("before method - log in if not logged in");
         if(!isLoggedIn(EMAIL)){
             String signinUrl = HOST + "/admin/signin";
             if(!signinUrl.equals(webDriver.getCurrentUrl())) {
                 webDriver.get(signinUrl);
             }
             logger.info("driver at " + webDriver.getCurrentUrl() + "trying to sign in");
             login(EMAIL, PASSWORD);
         }
     }
 
 
     @AfterClass
     public static void after(){
         logger.info("after class - counter at: " + counter.get());
         if(counter.get() >= NUM_OF_SUITES) {
             logger.info("after class - closing webDriver");
             webDriver.close();
         }
     }
 
     protected void waitForElement(final By by) {
         logger.info("driver at " + webDriver.getCurrentUrl() + " waiting for " + by);
         FluentWait<By> fw = new FluentWait<By>(by);
         fw.withTimeout(30, TimeUnit.SECONDS);
         try{
             fw.until(new Predicate<By>(){
                 @Override
                 public boolean apply(By input) {
                     try{
                         return webDriver.findElement(by).isDisplayed();
                     }catch(NoSuchElementException e){
                         return false;
                     }
                 }
             });
         }catch (TimeoutException e){
             logger.error("couldn't find " + by + " at "  + webDriver.getCurrentUrl(), e);
             throw e;
         }
     }
 
     public static TestContext context(){
         return TestContext.get();
     }
 
     protected void login(String username, String password) {
         logger.info("driver at " + webDriver.getCurrentUrl() + " trying to login with " + username + ":" + password);
         dropSession();
         By email = By.name("email");
         By passwordElem = By.name("password");
         By button = By.className("btn-primary");
         waitForElement(email);
         waitForElement(passwordElem);
         waitForElement(button);
         webDriver.findElement(email).sendKeys(username);
         webDriver.findElement(passwordElem).sendKeys(password);
         logger.info("about to login cookies are : {}", webDriver.manage().getCookies());
         webDriver.findElement(button).click();
     }
 
     protected static void subscribe(String user, String password, String name) {
         webDriver.get(HOST + "/admin/signup");
         logger.info("driver at " + webDriver.getCurrentUrl() + " trying to subscribe with " + user + ":" + password + " and " + name);
         webDriver.findElement(By.name("email")).sendKeys(user);
         webDriver.findElement(By.name("firstname")).sendKeys(name);
         webDriver.findElement(By.name("lastname")).sendKeys(name);
         webDriver.findElement(By.name("password")).sendKeys(password);
         webDriver.findElement(By.name("passwordConfirmation")).sendKeys(password);
         webDriver.findElement(By.className("btn-primary")).click();
 
     }
 
     protected void logout() {
         logger.info("driver at " + webDriver.getCurrentUrl() + " trying to logout");
         By logoutBy = By.id("logout");
         waitForElement(logoutBy);
         webDriver.findElement(logoutBy).click();
     }
 
     protected void assertUserIsLoggedIn() {
         assertUserIsLoggedIn(30);
     }
 
     protected void assertUserIsLoggedIn(int seconds) {
         logger.info("driver at " + webDriver.getCurrentUrl() + " asserting logged in");
         FluentWait<By> fw = new FluentWait<By>(By.id("username"));
         fw.withTimeout(seconds, TimeUnit.SECONDS);
         try{
             fw.until(new Predicate<By>() {
                 @Override
                 public boolean apply(By input) {
                     try {
                         return isLoggedIn(EMAIL);
                     } catch (NoSuchElementException e) {
                         return false;
                     }
                 }
             });
         }catch (TimeoutException e){
             logger.error("couldn't find username at "  + webDriver.getCurrentUrl(), e);
             throw e;
         }
     }
 
     protected boolean isLoggedIn(String email) {
         WebElement username = webDriver.findElement(By.id("username"));
         return username != null &&
                 username.isDisplayed() &&
                 email!= null &&
                 email.equals(username.getText());
     }
 
 
     protected void assertLoggedOut() {
         webDriver.get(HOST);
         logger.info("driver at " + webDriver.getCurrentUrl() + " asserting logged out");
 
         Set<Cookie> cookies = null;
         try{
             cookies = webDriver.manage().getCookies();
         }catch (IllegalArgumentException e){}
         if(cookies != null){
             Assert.assertFalse("authToken cookie is still defined", cookies.contains("authToken"));
             Assert.assertFalse("\"PLAY_SESSION cookie is still defined\"", cookies.contains("PLAY_SESSION"));
         }
     }
 
     protected String changePassword(String password, String newPassword) {
         logger.info("driver at " + webDriver.getCurrentUrl() + " trying to change password from: " + password + "to " + newPassword);
         webDriver.findElement(By.id("account")).click();
         logger.info("driver at " + webDriver.getCurrentUrl());
         waitForElement(By.id("oldPassword"));
         webDriver.findElement(By.id("oldPassword")).sendKeys(password);
         webDriver.findElement(By.id("newPassword")).sendKeys(newPassword);
         webDriver.findElement(By.id("confirmPassword")).sendKeys(newPassword);
         webDriver.findElement(By.className("btn-primary")).click();
 
         waitForElement(By.className("alert-success"));
         return newPassword;
     }
 
     protected static void dropSession(){
         webDriver.manage().deleteAllCookies();
     }
 }
