 package org.cloudifysource.widget.test;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.cloudifysource.widget.beans.JCloudsContext;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.concurrent.TimeUnit;
 
 
 /**
  * User: sagib
  * Date: 13/01/13
  * Time: 15:41
  */
 public class LoadTest extends AbstractCloudifyWidgetTest{
     public static final String SERVICE = context().getTestConf().getService();
     public static final String SERVICE_URL_POSTFIX = context().getTestConf().getServiceUrlPostFix();
     private final int N = context().getTestConf().getNumOfMachines();
     private static Logger logger = LoggerFactory.getLogger(LoadTest.class);
     private static HttpClient client = new HttpClient();
     private static final JCloudsContext jClouds = new JCloudsContext();
 
     @Override
     @Before
     public void beforeMethod(){
        jClouds.waitForMinMachines(3, 5 * 60 * 1000);
     }
 
     @After
     public void afterMethod(){
         jClouds.killNodes();
         jClouds.close();
     }
 
     @Test(timeout = 30 * 60 * 1000)
     public void loadTest() throws Exception{
         logger.info("running test on [{}]", context().getTestConf().getHost());
         String apiKey = createWidget();
         logout();
         logger.info("got Api key: " + apiKey);
         for (int i = 0; i < N; i++){
             logger.info("starting iteration [{}]", i);
             invokeWidget(apiKey);
         }
     }
 
     private void invokeWidget(String apiKey) throws Exception {
         logger.info("logging in");
         login(EMAIL,PASSWORD);
         logger.info("user is logged in - just checking");
         assertUserIsLoggedIn(2);
 
         logger.info("invoking widget");
         webDriver.get(HOST + "/widget/previewWidget?apiKey=" + apiKey);
         webDriver.switchTo().frame( webDriver.findElement(By.cssSelector("iframe")));
         By startBtn = By.id("start_btn");
         waitForElement(startBtn);
         webDriver.findElement(startBtn).click();
         By stopBtn = By.id("stop_btn");
         waitForElement(stopBtn);
         Assert.assertTrue("stop button is no enabled", webDriver.findElement(stopBtn).isDisplayed());
         By logBy = By.id("log");
         waitForElement(logBy);
         WebElement log = webDriver.findElement(logBy);
         long start = System.currentTimeMillis();
         boolean started = false;
         while (!started && System.currentTimeMillis() - start <= 120000){
             try {
                 assertServiceStarted(log);
                 started = true;
             }catch(AssertionError e){
                 Utils.sleep(TimeUnit.SECONDS, 1);
             }
         }
         assertServiceStarted(log);
 
         WebElement custom_link = webDriver.findElement(By.id("custom_link"));
         WebElement a = custom_link.findElement(By.tagName("a"));
         String href = a.getAttribute("href");
         HeadMethod head = new HeadMethod(href);
         Assert.assertEquals(200, client.executeMethod(head));
 
         webDriver.switchTo().defaultContent();
         logger.info("logging out");
         logout();
         assertLoggedOut();
     }
 
     private void assertServiceStarted(WebElement log) {
         Assert.assertTrue("service has not started", log.getText().contains("Service \"" + SERVICE + "\" successfully installed"));
     }
 
 
     private String createWidget() {
         By createButton = By.className("btn-primary");
         waitForElement(createButton);
         webDriver.findElement(createButton).click();
         By productName = By.name("productName");
         By rootpath = By.id("rootpath");
         waitForElement(productName);
         waitForElement(rootpath);
         webDriver.findElement(productName).sendKeys(NAME);
         webDriver.findElement(By.id("productVersion")).sendKeys(NAME);
         webDriver.findElement(By.id("title")).sendKeys(NAME);
         webDriver.findElement(By.id("providerURL")).sendKeys(NAME + ".com");
         webDriver.findElement(rootpath).sendKeys("cloudify-recipes-master/services/" + SERVICE + "/");
         webDriver.findElement(By.id("recipeURL")).sendKeys("https://github.com/CloudifySource/cloudify-recipes/archive/master.zip");
         webDriver.findElement(By.id("consolename")).sendKeys(SERVICE);
         webDriver.findElement(By.id("consoleurl")).sendKeys("http://$HOST" + SERVICE_URL_POSTFIX);
         webDriver.findElement(By.xpath("/html/body/div[@id='new_widget_modal']/form[@id='new_widget_form']/div[@class='modal-footer']/input[@class='btn btn-primary']")).click();
         By widget = By.className("enabled-widget");
         waitForElement(widget);
         return webDriver.findElement(widget).getAttribute("data-api_key");
     }
 }
