 package com.dynacrongroup.webtest.rule;
 
 import com.dynacrongroup.webtest.conf.SauceLabsCredentials;
 import com.dynacrongroup.webtest.driver.TargetWebDriver;
 import com.dynacrongroup.webtest.sauce.SauceREST;
 import com.google.common.annotations.VisibleForTesting;
 import org.junit.rules.ExternalResource;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 import org.openqa.selenium.*;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 import java.util.Set;
 
 /**
  * User: yurodivuie
  * Date: 5/23/13
  * Time: 9:12 AM
  */
 public class DriverClassRule extends ExternalResource implements WebDriver, JavascriptExecutor, HasInputDevices {
 
     private static final Logger LOG = LoggerFactory.getLogger(DriverClassRule.class);
 
     /**
      * Stored as a threadLocal instead of local so that this can be added to a base class and create separate drivers
      * for each thread.  ClassRules are static, so a local variable would create a single driver for all test classes
      * that inherited from the base.
      */
     private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
     private static ThreadLocal<Boolean> failed = new ThreadLocal<Boolean>();
     private static ThreadLocal<Class> testClass = new ThreadLocal<Class>();
     private static ThreadLocal<TargetWebDriver> targetWebDriver = new ThreadLocal<TargetWebDriver>();
     private static ThreadLocal<String> jobId = new ThreadLocal<String>();
 
    private SauceREST sauceREST = new SauceREST(SauceLabsCredentials.getUser(), SauceLabsCredentials.getKey());
 
     public DriverClassRule() {
     }
 
     @Override
     public Statement apply(Statement base, Description description) {
         initialize(description.getTestClass());
         return super.apply(base, description);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     protected void before() throws Throwable {
 
         super.before();    //To change body of overridden methods use File | Settings | File Templates.
 
         setFailed(false);
 
         if (getDriver() == null) {
             buildDriver();
         }
     }
 
     @Override
     protected void after() {
         super.after();    //To change body of overridden methods use File | Settings | File Templates.
 
         if (getTargetWebDriver().isRemote()) {
             reportFinalStatus();
         }
 
         destroyDriver();
     }
 
     public WebDriver unwrapDriver() {
         return getDriver();
     }
 
     public void rebuildDriver() {
         destroyDriver();
         buildDriver();
     }
 
     public void markAsFailed() {
         setFailed(true);
     }
 
     public void logInContext(String s) {
         if (getTargetWebDriver().isRemote()) {
             try {
                 ((JavascriptExecutor) getDriver()).executeScript("sauce:context=// " + s);
             } catch (WebDriverException exception) {
                 LOG.warn("Failed to update sauce labs context: {}", exception.getMessage());
             }
         }
     }
 
     @Override
     public void get(String s) {
         getDriver().get(s);
     }
 
     @Override
     public String getCurrentUrl() {
         return getDriver().getCurrentUrl();
     }
 
     @Override
     public String getTitle() {
         return getDriver().getTitle();
     }
 
     @Override
     public List<WebElement> findElements(By by) {
         return getDriver().findElements(by);
     }
 
     @Override
     public WebElement findElement(By by) {
         return getDriver().findElement(by);
     }
 
     @Override
     public String getPageSource() {
         return getDriver().getPageSource();
     }
 
     @Override
     public void close() {
         getDriver().close();
     }
 
     @Override
     public void quit() {
         getDriver().quit();
     }
 
     @Override
     public Set<String> getWindowHandles() {
         return getDriver().getWindowHandles();
     }
 
     @Override
     public String getWindowHandle() {
         return getDriver().getWindowHandle();
     }
 
     @Override
     public TargetLocator switchTo() {
         return getDriver().switchTo();
     }
 
     @Override
     public Navigation navigate() {
         return getDriver().navigate();
     }
 
     @Override
     public Options manage() {
         return getDriver().manage();
     }
 
     @Override
     public Keyboard getKeyboard() {
         return ((HasInputDevices)getDriver()).getKeyboard();
     }
 
     @Override
     public Mouse getMouse() {
         return ((HasInputDevices)getDriver()).getMouse();
     }
 
 
     @Override
     public Object executeScript(String s, Object... objects) {
         return ((JavascriptExecutor)getDriver()).executeScript(s, objects);
     }
 
     @Override
     public Object executeAsyncScript(String s, Object... objects) {
         return ((JavascriptExecutor)getDriver()).executeAsyncScript(s, objects);
     }
 
     public Boolean hasFailed() {
         return failed.get();
     }
 
     public String getJobUrl() {
         String jobUrl = null;
         if (getTargetWebDriver().isRemote()) {
             jobUrl = String.format("https://saucelabs.com/jobs/%s", getJobId());
         }
         else {
             jobUrl = "Local driver: request for jobUrl is inapplicable";
         }
         return jobUrl;
     }
 
     public String getJobId() {
         return jobId.get();
     }
 
     public TargetWebDriver getTargetWebDriver() {
         return targetWebDriver.get();
     }
 
     @VisibleForTesting
     protected void initialize(Class testClass) {
         setTestClass(testClass);
         setTargetWebDriver(new TargetWebDriver(testClass));
     }
 
     @VisibleForTesting
     protected void setFailed(Boolean hasFailed) {
         failed.set(hasFailed);
     }
     
     @VisibleForTesting
     protected WebDriver getDriver() {
         return driver.get();
     }
 
     @VisibleForTesting
     protected void setDriver(WebDriver newDriver) {
        driver.set(newDriver);
     }
 
     @VisibleForTesting
     protected void setTestClass(Class newTestClass) {
         testClass.set(newTestClass);
     }
 
     @VisibleForTesting
     protected Class getTestClass() {
         return testClass.get();
     }
 
     @VisibleForTesting
     protected void setTargetWebDriver(TargetWebDriver target) {
         targetWebDriver.set(target);
     }
 
     @VisibleForTesting
     protected void setJobId(String newJobId) {
         jobId.set(newJobId);
     }
 
     @VisibleForTesting
     protected void reportFailure() {
         failed.set(true);
     }
 
     @VisibleForTesting
     protected void buildDriver() {
         setDriver(getTargetWebDriver().build());
         recordJobId();
         reportURL();
     }
 
     private void recordJobId() {
         String newJobId = "";
         if (getTargetWebDriver().isRemote() && getDriver() != null) {
             newJobId = ((RemoteWebDriver) getDriver()).getSessionId().toString();
         }
         setJobId(newJobId);
     }
 
     private void reportURL() {
         if (getTargetWebDriver().isRemote()) {
             LOG.info("Remote job url: {}", getJobUrl());
         }
     }
 
     @VisibleForTesting
     protected void destroyDriver() {
         try {
             getDriver().quit();
             setDriver(null);
         }
         catch (WebDriverException exception) {
             LOG.warn("Exception while quitting driver during driver rebuild.", exception);
         }
     }
 
     private void reportFinalStatus() {
         if ( hasFailed() ) {
             sauceREST.jobFailed(getJobId());
         }
         else {
             sauceREST.jobPassed(getJobId());
         }
     }
 }
