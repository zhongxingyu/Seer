 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.Select;
 
 import java.util.Date;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class OfferFunctionalTest {
 
     private WebDriver webDriver;
     private String username;
     private String password;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         username = "test.twu";
         password = "Th0ughtW0rks@12";
     }
 
     @Test
     public void shouldCheckExistenceOfCreateOfferLink() {
         logIn();
 
         WebElement clickButton = webDriver.findElement(By.id("createOffer"));
         assertNotNull(clickButton);
     }
 
     @Test
     public void shouldGoToCreateOfferPage() throws InterruptedException {
         logIn();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("createOffer")).click();
         String actualTitleText = getTitleTagFromPage();
 
         String expectedTitle = "Create Offer";
         assertThat(actualTitleText, is(expectedTitle));
 
     }
 
     private String getTitleTagFromPage() {
         WebElement actualTitle = webDriver.findElement(By.tagName("title"));
         return actualTitle.getText();
     }
 
     @Test
     public void shouldHaveAnElementToEnterTitle() {
         logIn();
 
         webDriver.findElement(By.id("createOffer")).click();
         WebElement titleTextBox = webDriver.findElement(By.name("title"));
 
         assertThat(titleTextBox.isDisplayed(), is(true));
     }
 
     @Test
     public void shouldHaveAnElementToSelectCategory() {
         logIn();
 
         webDriver.findElement(By.id("createOffer")).click();
         WebElement categoryList = webDriver.findElement(By.name("category"));
 
         assertThat(categoryList.isDisplayed(), is(true));
     }
 
     @Test
     public void shouldHaveAnElementToEnterDescription() {
         logIn();
 
         webDriver.findElement(By.id("createOffer")).click();
         WebElement titleTextBox = webDriver.findElement(By.name("description"));
 
         assertThat(titleTextBox.isDisplayed(), is(true));
     }
 
     @Test
     public void shouldGoToViewAnOfferPage() throws InterruptedException {
         logIn();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("createOffer")).click();
         webDriver.findElement(By.name("title")).sendKeys("TITLE IN TEST");
 
         Select select = new Select(webDriver.findElement(By.tagName("select")));
         select.selectByValue("Cars");
 
         webDriver.findElement(By.name("description")).sendKeys("To pass the test or not, this is a question");
         webDriver.findElement(By.name("submit")).click();
         Thread.sleep(1000);
 
         String actualTitleText = getTitleTagFromPage();
 
         String expectedTitle = "View An Offer";
         assertThat(expectedTitle, is(actualTitleText));
     }
 
     @Test
     public void shouldDisplayOfferDetailsAfterCreating() throws InterruptedException {
         logIn();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("createOffer")).click();
         webDriver.findElement(By.name("title")).sendKeys("TITLE IN TEST");
 
         Select select = new Select(webDriver.findElement(By.tagName("select")));
         select.selectByValue("Cars");
 
         webDriver.findElement(By.name("description")).sendKeys("To pass the test or not, this is a question");
         webDriver.findElement(By.name("submit")).click();
         Thread.sleep(1000);
         String offerOwnerEmail = username + "@thoughtworks.com";
         assertThat(webDriver.getPageSource().contains(offerOwnerEmail),is(true));
     }
 
     @Test
     public void shouldDisplayRequiredOfferDetails() throws InterruptedException{
         logIn();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("browse")).click();
 
         WebElement firstOffer = webDriver.findElement(By.id("offer1"));
         String firstOfferTitle = firstOffer.getText();
         firstOffer.click();
 
         assertThat(webDriver.getPageSource().contains(firstOfferTitle),is(true));
 
     }
 
     @Test
     public void shouldGoToViewAnOfferPageFromBrowse() throws InterruptedException {
         logIn();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("browse")).click();
 
         webDriver.findElement(By.id("offer1")).click();
         Thread.sleep(1000);
 
         String actualTitleText = getTitleTagFromPage();
 
         String expectedTitle = "View An Offer";
         assertThat(expectedTitle, is(actualTitleText));
     }
 
     @Test
     public void shouldCheckExistenceOfBrowseOfferLink() {
         logIn();
 
         WebElement clickButton = webDriver.findElement(By.id("browse"));
         assertNotNull(clickButton);
     }
 
 //    @Test
 //    public void shouldDisplayUsernameOnCreateOfferPage() {
 //        logIn();
 //
 //        webDriver.findElement(By.id("createOffer")).click();
 //        WebElement userNameOnCreatePage = webDriver.findElement(By.id("username"));
 //
 //        assertThat(userNameOnCreatePage.getText().contains(username), is(true));
 //    }
 
 //    @Test
 //    public void shouldDisplayUsernameOnBrowseOfferPage() {
 //        logIn();
 //
 //        webDriver.findElement(By.id("browse")).click();
 //        WebElement userNameOnBrowsePage = webDriver.findElement(By.id("username"));
 //
 //        assertThat(userNameOnBrowsePage.getText().contains(username), is(true));
 //    }
 
 //    @Test
 //    public void shouldDisplayListOfOffersOnTheBrowsePage() {
 //        logIn();
 //
 //        webDriver.findElement(By.id("createOffer")).click();
 //        WebElement titleTextBox = webDriver.findElement(By.name("description"));
 //
 //        assertThat(titleTextBox.isDisplayed(), is(true));
 //    }
 
     @Test
     public void shouldCreateOfferAndDisplayRecentOfferOnBrowseAllPage() throws InterruptedException {
         logIn();
         Date uniqueDate = new Date();
         String testTitle = uniqueDate.toString();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("createOffer")).click();
         webDriver.findElement(By.name("title")).sendKeys(testTitle);
 
         Select select = new Select(webDriver.findElement(By.tagName("select")));
         select.selectByValue("Cars");
 
         webDriver.findElement(By.name("description")).sendKeys("To pass the test or not, this is a question");
         webDriver.findElement(By.name("submit")).click();
         Thread.sleep(1000);
 
 
         webDriver.get("http://127.0.0.1:8080/twu/");
         webDriver.findElement(By.id("browse")).click();
         WebElement firstOffer = webDriver.findElement(By.id("offer1"));
 
         assertThat(firstOffer.getText(),is(testTitle));
     }
 
     @After
     public void tearDown() throws Exception {
         webDriver.close();
     }
 
     private void logIn() {
         webDriver.get("http://127.0.0.1:8080/twu/");
 
         webDriver.findElement(By.id("username")).sendKeys(username);
         webDriver.findElement(By.id("password")).sendKeys(password);
         webDriver.findElement(By.name("submit")).click();
     }
 }
